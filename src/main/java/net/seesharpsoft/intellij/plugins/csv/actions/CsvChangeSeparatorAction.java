package net.seesharpsoft.intellij.plugins.csv.actions;

import consulo.document.util.FileContentUtilCore;
import consulo.fileEditor.FileEditor;
import consulo.ide.ServiceManager;
import consulo.language.Language;
import consulo.language.editor.PlatformDataKeys;
import consulo.ui.ex.action.ToggleAction;
import consulo.language.psi.PsiFile;
import consulo.language.editor.CommonDataKeys;
import consulo.ui.ex.action.AnActionEvent;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.CsvSeparatorHolder;
import net.seesharpsoft.intellij.plugins.csv.CsvValueSeparator;
import net.seesharpsoft.intellij.plugins.csv.components.CsvFileAttributes;
import org.jetbrains.annotations.NotNull;

public class CsvChangeSeparatorAction extends ToggleAction {
    private CsvValueSeparator mySeparator;

    CsvChangeSeparatorAction(CsvValueSeparator separator) {
        super(separator.getDisplay());
        mySeparator = separator;
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (!CsvHelper.isCsvFile(psiFile)) {
            return false;
        }
        return CsvHelper.hasValueSeparatorAttribute(psiFile) && CsvHelper.getValueSeparator(psiFile).equals(mySeparator);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (!CsvHelper.isCsvFile(psiFile)) {
            return;
        }
        Language language = psiFile.getLanguage();
        if (language instanceof CsvSeparatorHolder) {
            return;
        }
        CsvFileAttributes csvFileAttributes = ServiceManager.getService(psiFile.getProject(), CsvFileAttributes.class);
        csvFileAttributes.setFileSeparator(psiFile, this.mySeparator);
        FileContentUtilCore.reparseFiles(psiFile.getVirtualFile());

        FileEditor fileEditor = anActionEvent.getData(PlatformDataKeys.FILE_EDITOR);
        if (fileEditor != null) {
            fileEditor.selectNotify();
        }
    }
}
