package net.seesharpsoft.intellij.plugins.csv.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.component.ComponentManager;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.document.StripTrailingSpacesFilter;
import consulo.document.StripTrailingSpacesFilterFactory;
import consulo.language.file.LanguageFileType;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;

@ExtensionImpl
public class CsvStripTrailingSpacesFilterFactory extends StripTrailingSpacesFilterFactory {
    @Nonnull
    @Override
    public StripTrailingSpacesFilter createFilter(@Nullable ComponentManager project, @Nonnull Document document) {
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
