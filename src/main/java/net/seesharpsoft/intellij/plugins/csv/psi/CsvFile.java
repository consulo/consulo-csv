package net.seesharpsoft.intellij.plugins.csv.psi;

import consulo.document.util.FileContentUtilCore;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.language.file.LanguageFileType;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiElement;
import consulo.language.impl.psi.PsiFileBase;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfoMap;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import org.jetbrains.annotations.NotNull;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CsvFile extends PsiFileBase {

    private final LanguageFileType myFileType;
    private CsvColumnInfoMap<PsiElement> myColumnInfoMap;
    private long myColumnInfoMapModifiedStamp;

    private class CsvEditorSettingsPropertyChangeListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case "defaultEscapeCharacter":
                case "defaultValueSeparator":
                case "commentIndicator":
                case "valueColoring":
                    FileContentUtilCore.reparseFiles(CsvFile.this.getVirtualFile());
                    break;
                default:
                    // does not influence file
                    break;
            }
        }
    }

    public CsvFile(@NotNull FileViewProvider viewProvider, LanguageFileType fileType) {
        super(viewProvider, fileType.getLanguage());
        myFileType = fileType;
        // leak CsvEditorSettings.getInstance().addPropertyChangeListener(new CsvEditorSettingsPropertyChangeListener());
    }

    public CsvColumnInfoMap<PsiElement> getColumnInfoMap() {
        if (myColumnInfoMap == null || this.getModificationStamp() != myColumnInfoMapModifiedStamp) {
            myColumnInfoMap = CsvHelper.createColumnInfoMap(this);
            myColumnInfoMapModifiedStamp = getModificationStamp();
        }
        return myColumnInfoMap;
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return myFileType;
    }

    @Override
    public String toString() {
        return String.format("%s File", myFileType.getName());
    }
}