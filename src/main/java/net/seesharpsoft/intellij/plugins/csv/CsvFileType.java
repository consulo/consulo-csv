package net.seesharpsoft.intellij.plugins.csv;

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CsvFileType extends LanguageFileType {
    public static final CsvFileType INSTANCE = new CsvFileType();

    private CsvFileType() {
        super(CsvLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getId() {
        return "CSV";
    }

    @NotNull
    @Override
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("CSV file");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "csv";
    }

    @Nullable
    @Override
    public Image getIcon() {
        return CsvIconProvider.FILE;
    }
}