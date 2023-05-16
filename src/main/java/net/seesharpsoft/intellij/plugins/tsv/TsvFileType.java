package net.seesharpsoft.intellij.plugins.tsv;

import consulo.csv.icon.CsvIconGroup;
import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TsvFileType extends LanguageFileType {
    public static final TsvFileType INSTANCE = new TsvFileType();

    public static final Image ICON = CsvIconGroup.tsv_icon();

    private TsvFileType() {
        super(TsvLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getId() {
        return "TSV";
    }

    @NotNull
    @Override
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("TSV file");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "tsv";
    }

    @Nullable
    @Override
    public Image getIcon() {
        return ICON;
    }
}