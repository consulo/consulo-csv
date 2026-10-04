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

import consulo.application.ReadAction;
import consulo.codeEditor.EditorFactory;
import consulo.dataContext.DataSink;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorManager;
import consulo.fileEditor.FileEditorState;
import consulo.fileEditor.FileEditorStateLevel;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.fileEditor.structureView.StructureViewBuilderProvider;
import consulo.grid.editor.TableFileEditor;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.Hyperlink;
import consulo.ui.Label;
import consulo.ui.Space;
import consulo.ui.UIAccess;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.grid.GridUtil;
import consulo.ui.ex.grid.csv.CsvFormatEditor;
import consulo.ui.grid.DataAccessType;
import consulo.ui.grid.DataGrid;
import consulo.ui.grid.DataGridAppearance;
import consulo.ui.grid.DataGridRequestPlace;
import consulo.ui.grid.GridColumn;
import consulo.ui.grid.GridDataHookUp;
import consulo.ui.grid.GridRequestSource;
import consulo.ui.grid.ModelIndex;
import consulo.ui.grid.ThrowableInfo;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.HorizontalLayout;
import consulo.virtualFileSystem.VirtualFile;
import net.seesharpsoft.intellij.plugins.csv.editor.CsvFileEditorProvider;
import org.jspecify.annotations.Nullable;

/**
 * The "Table Editor" of a CSV file: the file as a data grid whose edits are written back as small changes of the document, so the
 * document undo works in both editors.
 * <p/>
 * The grid is built when the editor is first shown. While the text has a syntax error, a banner above the grid names the line and
 * links to the text editor, and the grid shows the records before the error, read-only.
 *
 * @since 2026-10-04
 */
public class CsvTableEditor extends TableFileEditor implements CsvFormatEditor, CsvTableDocumentDataHookUp.Listener {
    public static final String EDITOR_NAME = "Table Editor";
    public static final String ROW_POPUP_GROUP_ID = "CsvTableEditorRowContextMenu";
    public static final String COLUMN_POPUP_GROUP_ID = "CsvTableEditorColumnContextMenu";

    private CsvTableEditorState myState = new CsvTableEditorState();

    private volatile @Nullable CsvTableDocumentDataHookUp myHookUp;
    private @Nullable DataGrid myGrid;
    private boolean myFirstLoadDone;
    /**
     * The first load had a syntax error: the widths wait for a load without one.
     */
    private boolean myFirstLoadWaitsForFix;

    private @Nullable Component myBanner;
    private @Nullable Label myBannerLabel;
    private boolean myHasErrors;
    private int myErrorLine;

    public CsvTableEditor(Project project, VirtualFile file) {
        super(project, file);
    }

    @Override
    @RequiredUIAccess
    public DataGrid getDataGrid() {
        DataGrid grid = myGrid;
        if (grid == null) {
            grid = buildDataGrid();
            myGrid = grid;

            // the first load may have finished while the grid was built
            CsvTableDocumentDataHookUp hookUp = myHookUp;
            if (hookUp != null && hookUp.getCurrentMarkup() != null) {
                onFirstLoad(grid);
            }
        }
        return grid;
    }

    @RequiredUIAccess
    private DataGrid buildDataGrid() {
        Project project = getProject();
        VirtualFile file = getFile();

        @Nullable Document fileDocument = ReadAction.compute(() -> FileDocumentManager.getInstance().getDocument(file));
        Document document = fileDocument != null ? fileDocument : createEmptyReadOnlyDocument();

        CsvTableFormat format = CsvTableFormat.create(project, file, myState.getFixedHeaders(), myState.getRowLines());
        CsvTableDocumentDataHookUp hookUp = new CsvTableDocumentDataHookUp(project, document, format, UIAccess.current(), this);
        myHookUp = hookUp;
        hookUp.addRequestListener(new GridDataHookUp.RequestListener<>() {
            @Override
            public void error(GridRequestSource source, ThrowableInfo errorInfo) {
            }

            @Override
            public void updateCountReceived(GridRequestSource source, int updateCount) {
            }

            @Override
            @RequiredUIAccess
            public void requestFinished(GridRequestSource source, boolean success) {
                DataGrid grid = myGrid;
                if (!success || grid == null) {
                    return;
                }
                if (myFirstLoadDone) {
                    // a column the request added
                    applyDefaultColumnWidths(grid);
                }
                else {
                    onFirstLoad(grid);
                }
            }
        }, this);

        return createDataGrid(hookUp, ROW_POPUP_GROUP_ID, COLUMN_POPUP_GROUP_ID);
    }

    /**
     * The text of a file which has no document: an empty grid which cannot be edited.
     */
    private static Document createEmptyReadOnlyDocument() {
        Document document = EditorFactory.getInstance().createDocument("");
        document.setReadOnly(true);
        return document;
    }

    /**
     * The rows of the file reached the grid for the first time: the columns fit their content when the state asks for it; otherwise
     * each column gets its stored width, or else the default column width.
     * <p/>
     * While the text has a syntax error the grid shows only the records before it, so the widths wait for a load without one.
     */
    @RequiredUIAccess
    private void onFirstLoad(DataGrid grid) {
        CsvTableDocumentDataHookUp hookUp = myHookUp;
        if (myFirstLoadDone || hookUp == null) {
            return;
        }
        if (hookUp.hasErrors()) {
            myFirstLoadWaitsForFix = true;
            return;
        }
        myFirstLoadDone = true;
        myFirstLoadWaitsForFix = false;

        if (myState.getAutoColumnWidthOnOpen()) {
            adjustColumnWidths();
        }
        else {
            applyInitialColumnWidths(grid);
        }
    }

    /**
     * Gives each column its stored width, or else the default column width.
     */
    @RequiredUIAccess
    private void applyInitialColumnWidths(DataGrid grid) {
        int[] widths = myState.getColumnWidths();
        int defaultWidth = getTableFormat().getDefaultColumnWidth();
        int count = getColumnCount(grid);
        for (int i = 0; i < count; i++) {
            int width = i < widths.length && widths[i] > 0 ? widths[i] : defaultWidth;
            grid.setColumnWidth(ModelIndex.forColumn(grid, i), width);
        }
    }

    /**
     * Applies the widths of the state to the columns the grid has.
     */
    @RequiredUIAccess
    private void applyStoredColumnWidths(DataGrid grid) {
        int[] widths = myState.getColumnWidths();
        int count = Math.min(widths.length, getColumnCount(grid));
        for (int i = 0; i < count; i++) {
            if (widths[i] > 0) {
                grid.setColumnWidth(ModelIndex.forColumn(grid, i), widths[i]);
            }
        }
    }

    /**
     * Gives the default column width to each column which has no width yet - a column added after the file was opened.
     */
    @RequiredUIAccess
    private void applyDefaultColumnWidths(DataGrid grid) {
        int defaultWidth = getTableFormat().getDefaultColumnWidth();
        int count = getColumnCount(grid);
        for (int i = 0; i < count; i++) {
            ModelIndex<GridColumn> column = ModelIndex.forColumn(grid, i);
            if (grid.getColumnWidth(column) <= 0) {
                grid.setColumnWidth(column, defaultWidth);
            }
        }
    }

    @Override
    protected void configure(DataGrid grid, DataGridAppearance appearance) {
        CsvTableFormat format = getTableFormat();
        GridUtil.configureCsvTable(grid, appearance);
        appearance.setRowLines(format.getRowLines());
        grid.putUserData(GridUtil.DELETE_CLEARS_CELLS, Boolean.TRUE);
        CsvTableColumnColorLayer.apply(grid, format.isRainbowColoring());
    }

    @Override
    @RequiredUIAccess
    protected Component createUIComponent() {
        DataGrid grid = getDataGrid();

        Label bannerLabel = Label.create(LocalizeValue.empty());
        bannerLabel.setImage(PlatformIconGroup.generalError());
        Hyperlink textEditorLink = Hyperlink.create(LocalizeValue.localizeTODO("Open file in text editor"), event -> openTextEditor());

        HorizontalLayout banner = HorizontalLayout.create(Space.MEDIUM);
        banner.add(bannerLabel);
        banner.add(textEditorLink);
        banner.paddingBuilder().allSet(Space.SMALL).apply();

        myBanner = banner;
        myBannerLabel = bannerLabel;
        updateBanner();

        DockLayout layout = DockLayout.create(Space.NONE);
        layout.top(banner);
        layout.center(grid);
        return layout;
    }

    @RequiredUIAccess
    private void updateBanner() {
        Component banner = myBanner;
        Label bannerLabel = myBannerLabel;
        if (banner == null || bannerLabel == null) {
            return;
        }
        if (myHasErrors) {
            bannerLabel.setText(LocalizeValue.localizeTODO("Error while parsing content at line " + myErrorLine
                + " - fix it in the text editor"));
        }
        banner.setVisible(myHasErrors);
    }

    @RequiredUIAccess
    private void openTextEditor() {
        FileEditorManager.getInstance(getProject()).setSelectedEditor(getFile(), CsvFileEditorProvider.EDITOR_TYPE_ID);
    }

    @Override
    protected void uiDataSnapshot(DataSink sink) {
        sink.set(FileEditor.KEY, this);
        sink.set(VirtualFile.KEY, getFile());
        sink.set(Project.KEY, getProject());
        sink.set(CsvFormatEditor.CSV_FORMAT_EDITOR_KEY, this);
    }

    @Override
    public String getName() {
        return LocalizeValue.localizeTODO(EDITOR_NAME).get();
    }

    @Override
    public FileEditorState getState(FileEditorStateLevel level) {
        storeColumnWidths();
        return myState;
    }

    /**
     * Copies the widths of the grid into the state. Before the first load the grid has none, and while the text has a syntax error it
     * shows only some of the columns: the stored widths stay then.
     */
    private void storeColumnWidths() {
        DataGrid grid = myGrid;
        if (grid == null || !myFirstLoadDone || myHasErrors) {
            return;
        }
        int count = getColumnCount(grid);
        if (count == 0) {
            return;
        }
        int[] widths = new int[count];
        for (int i = 0; i < count; i++) {
            widths[i] = Math.max(0, grid.getColumnWidth(ModelIndex.forColumn(grid, i)));
        }
        myState.setColumnWidths(widths);
    }

    @Override
    @RequiredUIAccess
    public void setState(FileEditorState state) {
        myState = state instanceof CsvTableEditorState tableEditorState ? tableEditorState : new CsvTableEditorState();

        DataGrid grid = myGrid;
        if (grid != null) {
            refreshTableFormat();
            if (myFirstLoadDone) {
                applyStoredColumnWidths(grid);
            }
        }
    }

    /**
     * The new snapshot is taken before the data source becomes active: a parse of the changes made while the editor was hidden then
     * already splits the text with it.
     */
    @Override
    @RequiredUIAccess
    public void selectNotify() {
        refreshTableFormat();
        super.selectNotify();
    }

    @Override
    @RequiredUIAccess
    public void deselectNotify() {
        super.deselectNotify();
    }

    /**
     * Takes a new snapshot of the settings of the file - separator, escape character, the CSV editor settings and the state - and
     * hands it to the data source when it differs. The separator and escape character actions reach this through
     * {@link #selectNotify()}. The text is parsed again only when the new snapshot splits it differently.
     */
    @RequiredUIAccess
    private void refreshTableFormat() {
        CsvTableDocumentDataHookUp hookUp = myHookUp;
        DataGrid grid = myGrid;
        if (hookUp == null || grid == null) {
            return;
        }

        CsvTableFormat current = hookUp.getTableFormat();
        CsvTableFormat fresh = CsvTableFormat.create(getProject(), getFile(), myState.getFixedHeaders(), myState.getRowLines());
        if (fresh.equals(current)) {
            return;
        }

        hookUp.setTableFormat(fresh, newRequestSource(grid));
        if (fresh.getRowLines() != current.getRowLines()) {
            grid.getAppearance().setRowLines(fresh.getRowLines());
        }
        if (fresh.isRainbowColoring() != current.isRainbowColoring()) {
            CsvTableColumnColorLayer.apply(grid, fresh.isRainbowColoring());
        }
    }

    @Override
    public @Nullable StructureViewBuilder getStructureViewBuilder() {
        if (!isValid()) {
            return null;
        }
        Project project = getProject();
        VirtualFile file = getFile();
        for (StructureViewBuilderProvider provider : project.getApplication().getExtensionList(StructureViewBuilderProvider.class)) {
            StructureViewBuilder builder = provider.getStructureViewBuilder(file.getFileType(), file, project);
            if (builder != null) {
                return builder;
            }
        }
        return null;
    }

    @Override
    public boolean firstRowIsHeader() {
        CsvTableDocumentDataHookUp hookUp = myHookUp;
        return hookUp != null ? hookUp.getTableFormat().isFixedHeaders() : myState.getFixedHeaders();
    }

    @Override
    @RequiredUIAccess
    public void setFirstRowIsHeader(boolean value) {
        myState.setFixedHeaders(value);

        CsvTableDocumentDataHookUp hookUp = myHookUp;
        DataGrid grid = myGrid;
        if (hookUp != null && grid != null) {
            hookUp.setTableFormat(hookUp.getTableFormat().withFixedHeaders(value), newRequestSource(grid));
        }
    }

    @Override
    @RequiredUIAccess
    public void parsed(boolean hasErrors, int errorLine) {
        myHasErrors = hasErrors;
        myErrorLine = errorLine;
        updateBanner();

        CsvTableDocumentDataHookUp hookUp = myHookUp;
        DataGrid grid = myGrid;
        if (!hasErrors && myFirstLoadWaitsForFix && hookUp != null && grid != null) {
            // the error is gone; a parse of a change made elsewhere answers no request, so a load with one completes the first load
            myFirstLoadWaitsForFix = false;
            hookUp.getLoader().reloadCurrentPage(newRequestSource(grid));
        }
    }

    @Override
    @RequiredUIAccess
    public void tableFormatChanged(CsvTableFormat format) {
        myState.setFixedHeaders(format.isFixedHeaders());
    }

    /**
     * Fits every column to its content, up to the maximum column width setting.
     */
    @RequiredUIAccess
    public void adjustColumnWidths() {
        CsvTableDocumentDataHookUp hookUp = myHookUp;
        DataGrid grid = myGrid;
        if (hookUp != null && grid != null) {
            grid.fitColumnWidths(hookUp.getTableFormat().getMaxColumnWidth());
        }
    }

    /**
     * Gives every column the default column width setting.
     */
    @RequiredUIAccess
    public void resetColumnWidths() {
        CsvTableDocumentDataHookUp hookUp = myHookUp;
        DataGrid grid = myGrid;
        if (hookUp == null || grid == null) {
            return;
        }
        int width = hookUp.getTableFormat().getDefaultColumnWidth();
        int count = getColumnCount(grid);
        for (int i = 0; i < count; i++) {
            grid.setColumnWidth(ModelIndex.forColumn(grid, i), width);
        }
    }

    /**
     * The snapshot the grid shows; before the data source exists, one taken now.
     */
    private CsvTableFormat getTableFormat() {
        CsvTableDocumentDataHookUp hookUp = myHookUp;
        if (hookUp != null) {
            return hookUp.getTableFormat();
        }
        return CsvTableFormat.create(getProject(), getFile(), myState.getFixedHeaders(), myState.getRowLines());
    }

    private static int getColumnCount(DataGrid grid) {
        return grid.getDataModel(DataAccessType.DATA_WITH_MUTATIONS).getColumnCount();
    }

    private static GridRequestSource newRequestSource(DataGrid grid) {
        return new GridRequestSource(new DataGridRequestPlace(grid));
    }
}
