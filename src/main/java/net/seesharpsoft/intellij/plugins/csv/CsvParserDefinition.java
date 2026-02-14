package net.seesharpsoft.intellij.plugins.csv;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.ast.TokenSet;
import consulo.language.ast.TokenType;
import consulo.language.file.FileViewProvider;
import consulo.language.lexer.Lexer;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import jakarta.annotation.Nonnull;
import net.seesharpsoft.intellij.lang.FileParserDefinition;
import net.seesharpsoft.intellij.plugins.csv.parser.CsvParser;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFileElementType;
import net.seesharpsoft.intellij.plugins.csv.psi.impl.CsvTypesFactory;
import org.jetbrains.annotations.NotNull;

@ExtensionImpl
public class CsvParserDefinition implements FileParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);

    public static final IFileElementType FILE = new CsvFileElementType(CsvLanguage.INSTANCE);

    @Nonnull
    @Override
    public Language getLanguage() {
        return CsvLanguage.INSTANCE;
    }

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
        return CsvTypesFactory.createElement(node);
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
