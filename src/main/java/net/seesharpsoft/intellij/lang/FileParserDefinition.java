package net.seesharpsoft.intellij.lang;

import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.PsiFile;

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
