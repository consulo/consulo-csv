package net.seesharpsoft.intellij.plugins.psv;

import consulo.language.Language;
import consulo.localize.LocalizeValue;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import net.seesharpsoft.intellij.plugins.csv.CsvSeparatorHolder;
import net.seesharpsoft.intellij.plugins.csv.CsvValueSeparator;

public final class PsvLanguage extends Language implements CsvSeparatorHolder {
    public static final PsvLanguage INSTANCE = new PsvLanguage();

    private PsvLanguage() {
        super(CsvLanguage.INSTANCE, "psv");
    }

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("PSV");
    }

    @Override
    public CsvValueSeparator getSeparator() {
        return CsvValueSeparator.PIPE;
    }
}
