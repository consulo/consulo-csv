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
package net.seesharpsoft.intellij.plugins.csv.actions;

import consulo.application.dumb.DumbAware;
import consulo.fileEditor.FileEditor;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.AnActionWithSyncUpdate;
import net.seesharpsoft.intellij.plugins.csv.editor.table.CsvTableEditor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The column width actions of the table editor. The row and column actions of its context menus are the grid's own.
 *
 * @since 2026-10-04
 */
@NullMarked
public abstract class CsvTableEditorActions extends AnAction implements DumbAware, AnActionWithSyncUpdate {
    /**
     * Fits every column to its content, up to the maximum column width setting.
     */
    public static class AdjustColumnWidths extends CsvTableEditorActions {
        @Override
        @RequiredUIAccess
        public void actionPerformed(AnActionEvent e) {
            CsvTableEditor editor = getTableEditor(e);
            if (editor != null) {
                editor.adjustColumnWidths();
            }
        }
    }

    /**
     * Gives every column the default column width setting.
     */
    public static class ResetColumnWidths extends CsvTableEditorActions {
        @Override
        @RequiredUIAccess
        public void actionPerformed(AnActionEvent e) {
            CsvTableEditor editor = getTableEditor(e);
            if (editor != null) {
                editor.resetColumnWidths();
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(getTableEditor(e) != null);
    }

    public static @Nullable CsvTableEditor getTableEditor(AnActionEvent e) {
        return e.getData(FileEditor.KEY) instanceof CsvTableEditor editor ? editor : null;
    }
}
