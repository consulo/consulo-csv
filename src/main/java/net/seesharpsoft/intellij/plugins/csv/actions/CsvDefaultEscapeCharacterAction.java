package net.seesharpsoft.intellij.plugins.csv.actions;

import consulo.document.util.FileContentUtilCore;
import consulo.fileEditor.FileEditor;
import consulo.ui.ex.action.AnActionEvent;
import consulo.language.editor.CommonDataKeys;
import consulo.language.editor.PlatformDataKeys;
import consulo.ui.ex.action.ToggleAction;
import consulo.language.psi.PsiFile;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.components.CsvFileAttributes;
import org.jetbrains.annotations.NotNull;

public class CsvDefaultEscapeCharacterAction extends ToggleAction {
    CsvDefaultEscapeCharacterAction() {
        super("Project Default");
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (!CsvHelper.isCsvFile(psiFile)) {
            return false;
        }
        return !CsvHelper.hasEscapeCharacterAttribute(psiFile);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (!CsvHelper.isCsvFile(psiFile)) {
            return;
        }
        CsvFileAttributes.getInstance(psiFile.getProject()).resetEscapeSeparator(psiFile);
        FileContentUtilCore.reparseFiles(psiFile.getVirtualFile());

        FileEditor fileEditor = anActionEvent.getData(PlatformDataKeys.FILE_EDITOR);
        if (fileEditor != null) {
            fileEditor.selectNotify();
        }
    }
}
