package net.seesharpsoft.intellij.plugins.csv.actions;

import consulo.document.util.FileContentUtilCore;
import consulo.fileEditor.FileEditor;
import consulo.language.editor.CommonDataKeys;
import consulo.language.editor.PlatformDataKeys;
import consulo.language.psi.PsiFile;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.ToggleAction;
import net.seesharpsoft.intellij.plugins.csv.CsvEscapeCharacter;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.components.CsvFileAttributes;
import org.jetbrains.annotations.NotNull;

public class CsvChangeEscapeCharacterAction extends ToggleAction {
    private CsvEscapeCharacter myEscapeCharacter;

    CsvChangeEscapeCharacterAction(CsvEscapeCharacter escapeCharacter) {
        super(escapeCharacter.getDisplay());
        myEscapeCharacter = escapeCharacter;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (!CsvHelper.isCsvFile(psiFile)) {
            return false;
        }
        return CsvHelper.hasEscapeCharacterAttribute(psiFile) && CsvHelper.getEscapeCharacter(psiFile).equals(myEscapeCharacter);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (!CsvHelper.isCsvFile(psiFile)) {
            return;
        }
        CsvFileAttributes.getInstance(psiFile.getProject()).setEscapeCharacter(psiFile, this.myEscapeCharacter);
        FileContentUtilCore.reparseFiles(psiFile.getVirtualFile());

        FileEditor fileEditor = anActionEvent.getData(PlatformDataKeys.FILE_EDITOR);
        if (fileEditor != null) {
            fileEditor.selectNotify();
        }
    }
}
