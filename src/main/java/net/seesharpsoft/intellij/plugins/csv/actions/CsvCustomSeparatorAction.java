package net.seesharpsoft.intellij.plugins.csv.actions;

import consulo.document.util.FileContentUtilCore;
import consulo.fileEditor.FileEditor;
import consulo.ide.ServiceManager;
import consulo.language.editor.CommonDataKeys;
import consulo.ui.ex.action.AnActionEvent;
import consulo.language.editor.PlatformDataKeys;
import consulo.ui.ex.action.ToggleAction;
import consulo.language.psi.PsiFile;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.CsvValueSeparator;
import net.seesharpsoft.intellij.plugins.csv.components.CsvFileAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CsvCustomSeparatorAction extends ToggleAction {
    CsvCustomSeparatorAction() {
        super("Custom");
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (!CsvHelper.isCsvFile(psiFile)) {
            return false;
        }
        return CsvHelper.getValueSeparator(psiFile).isCustom();
    }

    @Override
    public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (!CsvHelper.isCsvFile(psiFile)) {
            return;
        }

        FileEditor fileEditor = anActionEvent.getData(PlatformDataKeys.FILE_EDITOR);
        CsvValueSeparator currentSeparator = CsvHelper.getValueSeparator(psiFile);
        String customValueSeparator = JOptionPane.showInputDialog(fileEditor == null ? null : fileEditor.getComponent(),
                "Value separator",
                currentSeparator.getCharacter());

        if (customValueSeparator == null) {
            return;
        }
        if (customValueSeparator.length() == 0 || customValueSeparator.contains(" ")) {
            JOptionPane.showMessageDialog(fileEditor == null ? null : fileEditor.getComponent(), "Value separator must have at least one character and no spaces!");
            return;
        }

        CsvFileAttributes csvFileAttributes = ServiceManager.getService(psiFile.getProject(), CsvFileAttributes.class);
        csvFileAttributes.setFileSeparator(psiFile, new CsvValueSeparator(customValueSeparator));
        FileContentUtilCore.reparseFiles(psiFile.getVirtualFile());

        if (fileEditor != null) {
            fileEditor.selectNotify();
        }
    }
}
