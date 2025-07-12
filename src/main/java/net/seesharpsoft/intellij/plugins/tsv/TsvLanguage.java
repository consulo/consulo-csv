package net.seesharpsoft.intellij.plugins.tsv;

import consulo.language.Language;
import consulo.localize.LocalizeValue;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import net.seesharpsoft.intellij.plugins.csv.CsvSeparatorHolder;
import net.seesharpsoft.intellij.plugins.csv.CsvValueSeparator;

public final class TsvLanguage extends Language implements CsvSeparatorHolder {
    public static final TsvLanguage INSTANCE = new TsvLanguage();

    private TsvLanguage() {
        super(CsvLanguage.INSTANCE, "tsv");
    }

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("TSV");
    }

    @Override
    public CsvValueSeparator getSeparator() {
        return CsvValueSeparator.TAB;
    }
}
