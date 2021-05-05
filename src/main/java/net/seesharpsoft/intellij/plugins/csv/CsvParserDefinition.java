package net.seesharpsoft.intellij.plugins.csv;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.lang.LanguageVersion;
import net.seesharpsoft.intellij.lang.FileParserDefinition;
import net.seesharpsoft.intellij.plugins.csv.parser.CsvParser;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFileElementType;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;
import org.jetbrains.annotations.NotNull;

public class CsvParserDefinition implements FileParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);

    public static final IFileElementType FILE = new CsvFileElementType(CsvLanguage.INSTANCE);

    @NotNull
    @Override
    public Lexer createLexer(LanguageVersion version) {
        throw new UnsupportedOperationException("use 'createLexer(PsiFile file)' instead");
    }

    @Override
    @NotNull
    public TokenSet getWhitespaceTokens(LanguageVersion version) {
        return WHITE_SPACES;
    }

    @Override
    @NotNull
    public TokenSet getCommentTokens(LanguageVersion version) {
        return TokenSet.EMPTY;
    }

    @Override
    @NotNull
    public TokenSet getStringLiteralElements(LanguageVersion version) {
        return TokenSet.EMPTY;
    }

    @Override
    @NotNull
    public PsiParser createParser(LanguageVersion version) {
        return new CsvParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new CsvFile(viewProvider, CsvFileType.INSTANCE);
    }

    @Override
    @NotNull
    public PsiElement createElement(ASTNode node) {
        return CsvTypes.Factory.createElement(node);
    }

    @Override
    public Lexer createLexer(@NotNull PsiFile file) {
        return CsvLexerFactory.getInstance().createLexer(file);
    }

    @Override
    public PsiParser createParser(@NotNull PsiFile file) {
        return createParser(file.getLanguageVersion());
    }
}
