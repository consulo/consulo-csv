package net.seesharpsoft.intellij.lang;

import consulo.language.lexer.Lexer;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiFile;
import consulo.language.parser.ParserDefinition;

/**
 * Support for file specific parser definition.
 */
public interface FileParserDefinition extends ParserDefinition {
    default Lexer createLexer(PsiFile file) {
        return createLexer(file.getLanguageVersion());
    }

    default PsiParser createParser(PsiFile file) {
        return createParser(file.getLanguageVersion());
    }
}
