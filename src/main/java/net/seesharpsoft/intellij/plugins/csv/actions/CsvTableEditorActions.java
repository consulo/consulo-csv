package net.seesharpsoft.intellij.plugins.csv.actions;

import consulo.fileEditor.FileEditor;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.language.editor.PlatformDataKeys;
import net.seesharpsoft.intellij.plugins.csv.editor.table.CsvTableEditor;
import net.seesharpsoft.intellij.plugins.csv.editor.table.api.TableActions;
import org.jetbrains.annotations.NotNull;

public abstract class CsvTableEditorActions extends AnAction {

    public static class AddRowBefore extends CsvTableEditorActions {
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            getTableActions(anActionEvent).addRow(getTableEditor(anActionEvent), true);
        }
    }

    public static class AddRowAfter extends CsvTableEditorActions {
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            getTableActions(anActionEvent).addRow(getTableEditor(anActionEvent), false);
        }
    }

    public static class AddColumnBefore extends CsvTableEditorActions {
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            getTableActions(anActionEvent).addColumn(getTableEditor(anActionEvent), true);
        }
    }

    public static class AddColumnAfter extends CsvTableEditorActions {
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            getTableActions(anActionEvent).addColumn(getTableEditor(anActionEvent), false);
        }
    }

    public static class DeleteSelectedRows extends CsvTableEditorActions {
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            getTableActions(anActionEvent).deleteSelectedRows(getTableEditor(anActionEvent));
        }
    }

    public static class DeleteSelectedColumns extends CsvTableEditorActions {
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            getTableActions(anActionEvent).deleteSelectedColumns(getTableEditor(anActionEvent));
        }
    }

    public static class AdjustColumnWidths extends CsvTableEditorActions {
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            getTableActions(anActionEvent).adjustColumnWidths(getTableEditor(anActionEvent));
        }
    }

    public static class ResetColumnWidths extends CsvTableEditorActions {
        @Override
        public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            getTableActions(anActionEvent).resetColumnWidths(getTableEditor(anActionEvent));
        }
    }

    public static CsvTableEditor getTableEditor(@NotNull AnActionEvent anActionEvent) {
        FileEditor fileEditor = anActionEvent.getData(PlatformDataKeys.FILE_EDITOR);
        if (!(fileEditor instanceof CsvTableEditor)) {
            return null;
        }
        return (CsvTableEditor) fileEditor;
    }

    public static TableActions getTableActions(@NotNull AnActionEvent anActionEvent) {
        return getTableEditor(anActionEvent).getActions();
    }
}
