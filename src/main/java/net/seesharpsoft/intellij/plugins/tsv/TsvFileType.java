package net.seesharpsoft.intellij.plugins.tsv;

import com.intellij.openapi.fileTypes.LanguageFileType;
import consulo.csv.icon.CsvIconGroup;
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
    public String getName() {
        return "TSV";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "TSV file";
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