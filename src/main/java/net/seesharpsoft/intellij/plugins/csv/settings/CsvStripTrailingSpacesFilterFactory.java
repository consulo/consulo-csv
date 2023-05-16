package net.seesharpsoft.intellij.plugins.csv.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.component.ComponentManager;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.document.StripTrailingSpacesFilter;
import consulo.document.StripTrailingSpacesFilterFactory;
import consulo.language.file.LanguageFileType;
import consulo.virtualFileSystem.VirtualFile;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;

import javax.annotation.Nonnull;

@ExtensionImpl
public class CsvStripTrailingSpacesFilterFactory extends StripTrailingSpacesFilterFactory {
    @Nonnull
    @Override
    public StripTrailingSpacesFilter createFilter(@javax.annotation.Nullable ComponentManager project, @Nonnull Document document) {
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (project != null &&
                virtualFile != null &&
                virtualFile.getFileType() instanceof LanguageFileType &&
                ((LanguageFileType) virtualFile.getFileType()).getLanguage().isKindOf(CsvLanguage.INSTANCE) &&
                CsvEditorSettings.getInstance().getKeepTrailingSpaces()) {
            return StripTrailingSpacesFilter.NOT_ALLOWED;
        }
        return StripTrailingSpacesFilter.ALL_LINES;
    }
}
