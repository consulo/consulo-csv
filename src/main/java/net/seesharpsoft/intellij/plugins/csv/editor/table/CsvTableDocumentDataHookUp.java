/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.seesharpsoft.intellij.plugins.csv.editor.table;

import consulo.document.Document;
import consulo.grid.editor.CsvDocumentDataHookUp;
import consulo.project.Project;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.grid.csv.CsvFormat;
import consulo.ui.grid.GridRequestSource;
import consulo.undoRedo.BasicUndoableAction;
import consulo.undoRedo.ProjectUndoManager;

import java.lang.ref.WeakReference;

/**
 * The data source of the table: the whole document, parsed with {@link CsvTableParser} from the {@link CsvTableFormat} snapshot, and
 * changed by minimal range edits ({@link CsvTableMarkup}).
 * <p/>
 * While the text has a syntax error, the grid shows the records before it, and the data source is read-only.
 * <p/>
 * The format of the platform ({@link #getFormat()}) always follows the snapshot ({@link #getTableFormat()}): the grid actions read
 * whether the records have a header from it.
 *
 * @since 2026-10-04
 */
public class CsvTableDocumentDataHookUp extends CsvDocumentDataHookUp {
    /**
     * What the data source tells its owner. Both methods are called on the UI thread, never inside a document command.
     */
    public interface Listener {
        /**
         * A parse of the text is done: once per parse, from the first one on, for own edits as well as for changes made elsewhere.
         * It may run just before the grid shows the rows of that parse, so it must not read the grid or
         * {@link CsvTableDocumentDataHookUp#isReadOnly()}/{@link CsvTableDocumentDataHookUp#hasErrors()} - the arguments are the state
         * of that parse. Never called after {@link CsvTableDocumentDataHookUp#dispose()}.
         *
         * @param hasErrors whether the text has a syntax error; the grid then shows the records before it, read-only
         * @param errorLine the 1-based document line of the first error, 0 when there is none
         */
        @RequiredUIAccess
        default void parsed(boolean hasErrors, int errorLine) {
        }

        /**
         * The data source changed the table format itself, as a side effect of a request: renaming a column of a file without a
         * header row writes one and turns fixed headers on; undoing that request turns them off again, and redoing it on. Also called
         * when someone calls {@link CsvTableDocumentDataHookUp#setFormat(CsvFormat, GridRequestSource)} from outside. Not called for
         * the owner's own {@link CsvTableDocumentDataHookUp#setTableFormat} calls. The owner stores
         * {@link CsvTableFormat#isFixedHeaders()} in its state, and must not call {@link CsvTableDocumentDataHookUp#setTableFormat} in
         * response.
         */
        @RequiredUIAccess
        default void tableFormatChanged(CsvTableFormat format) {
        }
    }

    private final Listener myListener;
    /**
     * Read by {@link #buildMarkup} on the parse thread.
     */
    private volatile CsvTableFormat myTableFormat;
    private volatile boolean myDisposed;

    /**
     * @param uiAccess the UI thread of the grid
     * @param listener the owner's listener
     */
    public CsvTableDocumentDataHookUp(Project project, Document document, CsvTableFormat format, UIAccess uiAccess, Listener listener) {
        super(project, format.toCsvFormat(), document, null, uiAccess);
        myTableFormat = format;
        myListener = listener;
    }

    /**
     * @return the current snapshot; may be read on any thread
     */
    public CsvTableFormat getTableFormat() {
        return myTableFormat;
    }

    /**
     * Takes a new snapshot. It is always stored. When it does not parse like the current one ({@link CsvTableFormat#parsesLike}), the
     * text is parsed again, and the source is answered once the grid shows the new markup. Otherwise the source is answered at once,
     * without a parse. The document never changes.
     */
    @RequiredUIAccess
    public void setTableFormat(CsvTableFormat format, GridRequestSource source) {
        CsvTableFormat oldFormat = myTableFormat;
        myTableFormat = format;
        if (format.parsesLike(oldFormat)) {
            source.requestComplete(true);
        }
        else {
            super.setFormat(format.toCsvFormat(), source);
        }
    }

    /**
     * @return whether the markup the grid shows now has errors; {@code false} before the first parse
     */
    @RequiredUIAccess
    public boolean hasErrors() {
        return getCurrentMarkup() instanceof CsvTableMarkup markup && markup.hasErrors();
    }

    /**
     * @return the 1-based line of the first error of the markup the grid shows now, {@code 0} when there is none
     */
    @RequiredUIAccess
    public int getErrorLine() {
        return getCurrentMarkup() instanceof CsvTableMarkup markup ? markup.getErrorLine() : 0;
    }

    /**
     * Read-only as well while the markup the grid shows has errors.
     */
    @Override
    public boolean isReadOnly() {
        return super.isReadOnly() || getCurrentMarkup() instanceof CsvTableMarkup markup && markup.hasErrors();
    }

    /**
     * A call from outside: only whether the format has a header record is taken over, as fixed headers. The records are parsed again
     * when that changes, and the listener is told.
     */
    @Override
    @RequiredUIAccess
    public void setFormat(CsvFormat format, GridRequestSource source) {
        CsvTableFormat newFormat = myTableFormat.withFixedHeaders(format.headerRecord != null);
        setTableFormat(newFormat, source);
        if (!myDisposed) {
            myListener.tableFormatChanged(newFormat);
        }
    }

    /**
     * Inside the command of a rename which wrote a header row: fixed headers follow the new format before the text is parsed again; the
     * listener is told after the command. The change of the flag is part of the undo step of the command.
     */
    @Override
    protected void formatChangedAfterUpdate(CsvFormat format) {
        boolean oldFixedHeaders = myTableFormat.isFixedHeaders();
        CsvTableFormat newFormat = myTableFormat.withFixedHeaders(format.headerRecord != null);
        myTableFormat = newFormat;
        super.formatChangedAfterUpdate(newFormat.toCsvFormat());
        if (oldFixedHeaders != newFormat.isFixedHeaders()) {
            registerFixedHeadersUndo(oldFixedHeaders, newFormat.isFixedHeaders());
        }
        getUIAccess().give(() -> {
            if (!myDisposed) {
                myListener.tableFormatChanged(newFormat);
            }
        });
    }

    /**
     * Undoing the command - which deletes the header row it wrote - turns the flag back, and redoing it turns the flag on again; otherwise
     * the first data record would become the header after the undo.
     */
    private void registerFixedHeadersUndo(boolean oldFixedHeaders, boolean newFixedHeaders) {
        Project project = getProject();
        if (project == null) {
            return;
        }
        ProjectUndoManager.getInstance(project)
            .undoableActionPerformed(new FixedHeadersUndoableAction(this, oldFixedHeaders, newFixedHeaders));
    }

    /**
     * Undo or redo, inside its command: the flag changes before the undone or redone text is parsed. After the command the text is
     * parsed again - a parse of the redone text may have started before the flag changed - and the listener is told.
     */
    private void restoreFixedHeaders(boolean fixedHeaders) {
        CsvTableFormat newFormat = myTableFormat.withFixedHeaders(fixedHeaders);
        myTableFormat = newFormat;
        super.formatChangedAfterUpdate(newFormat.toCsvFormat());
        getUIAccess().give(() -> {
            if (myDisposed) {
                return;
            }
            // while the grid is hidden, the change of the text is parsed when it is shown again
            if (isActive()) {
                getLoader().reloadCurrentPage(new GridRequestSource(null));
            }
            myListener.tableFormatChanged(newFormat);
        });
    }

    /**
     * Parse thread. Reads only the snapshot. Never returns {@code null}, and never throws for malformed text.
     */
    @Override
    protected CsvTableMarkup buildMarkup(CharSequence sequence, GridRequestSource source) {
        CsvTableFormat format = myTableFormat;
        // the lexer reads the text char by char: a string is faster than the snapshot of the document
        CsvTableParser.Result result = new CsvTableParser(format).parse(sequence.toString());
        CsvTableMarkup markup = new CsvTableMarkup(result, format);

        boolean hasErrors = result.hasErrors();
        int errorLine = result.getErrorLine();
        if (!myDisposed) {
            // queued before the platform applies the markup
            getUIAccess().give(() -> {
                if (!myDisposed) {
                    myListener.parsed(hasErrors, errorLine);
                }
            });
        }
        return markup;
    }

    /**
     * Stops the listener calls, then disposes the data source.
     */
    @Override
    public void dispose() {
        myDisposed = true;
        super.dispose();
    }

    /**
     * The change of the header flag in the undo step of a request. The undo stack outlives the editor, so it does not keep the data
     * source: once that is gone, undo and redo change only the text.
     */
    private static final class FixedHeadersUndoableAction extends BasicUndoableAction {
        private final WeakReference<CsvTableDocumentDataHookUp> myHookUp;
        private final boolean myOldFixedHeaders;
        private final boolean myNewFixedHeaders;

        private FixedHeadersUndoableAction(CsvTableDocumentDataHookUp hookUp, boolean oldFixedHeaders, boolean newFixedHeaders) {
            super(hookUp.getDocument());
            myHookUp = new WeakReference<>(hookUp);
            myOldFixedHeaders = oldFixedHeaders;
            myNewFixedHeaders = newFixedHeaders;
        }

        @Override
        public void undo() {
            restore(myOldFixedHeaders);
        }

        @Override
        public void redo() {
            restore(myNewFixedHeaders);
        }

        private void restore(boolean fixedHeaders) {
            CsvTableDocumentDataHookUp hookUp = myHookUp.get();
            if (hookUp != null) {
                hookUp.restoreFixedHeaders(fixedHeaders);
            }
        }
    }
}
