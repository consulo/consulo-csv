package net.seesharpsoft.intellij.plugins.tsv;

import consulo.language.Language;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import net.seesharpsoft.intellij.plugins.csv.CsvSeparatorHolder;
import net.seesharpsoft.intellij.plugins.csv.CsvValueSeparator;

public final class TsvLanguage extends Language implements CsvSeparatorHolder {
    public static final TsvLanguage INSTANCE = new TsvLanguage();

    private TsvLanguage() {
        super(CsvLanguage.INSTANCE, "tsv");
    }

    @Override
    public String getDisplayName() {
        return "TSV";
    }

    @Override
    public CsvValueSeparator getSeparator() {
        return CsvValueSeparator.TAB;
    }
}
