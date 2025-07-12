package net.seesharpsoft.intellij.plugins.csv;

import consulo.language.Language;
import consulo.language.plain.PlainTextLanguage;
import consulo.localize.LocalizeValue;

public final class CsvLanguage extends Language {
    public static final CsvLanguage INSTANCE = new CsvLanguage();

    private CsvLanguage() {
        super(PlainTextLanguage.INSTANCE, "csv");
    }

    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("CSV");
    }
}