package net.seesharpsoft.intellij.plugins.psv;

import com.intellij.openapi.fileTypes.LanguageFileType;
import consulo.csv.icon.CsvIconGroup;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PsvFileType extends LanguageFileType {
    public static final PsvFileType INSTANCE = new PsvFileType();

    public static final Image ICON = CsvIconGroup.psv_icon();

    private PsvFileType() {
        super(PsvLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getId() {
        return "PSV";
    }

    @NotNull
    @Override
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("PSV file");
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "psv";
    }

    @Nullable
    @Override
    public Image getIcon() {
        return ICON;
    }
}