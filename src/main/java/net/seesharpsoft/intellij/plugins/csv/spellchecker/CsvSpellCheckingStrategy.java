package net.seesharpsoft.intellij.plugins.csv.spellchecker;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.spellcheker.SpellcheckingStrategy;
import consulo.language.spellcheker.tokenizer.Tokenizer;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvField;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class CsvSpellCheckingStrategy extends SpellcheckingStrategy {
    @Override
    public Tokenizer getTokenizer(PsiElement element) {
        if (element instanceof CsvField) {
            return TEXT_TOKENIZER;
        }
        return EMPTY_TOKENIZER;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CsvLanguage.INSTANCE;
    }
}
