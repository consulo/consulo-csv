package net.seesharpsoft.intellij.plugins.csv;

import consulo.language.Language;
import consulo.language.plain.PlainTextLanguage;

public final class CsvLanguage extends Language {
    public static final CsvLanguage INSTANCE = new CsvLanguage();

    private CsvLanguage() {
        super(PlainTextLanguage.INSTANCE, "csv");
    }

    @Override
    public String getDisplayName() {
        return "CSV";
    }
}