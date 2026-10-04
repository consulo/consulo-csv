package net.seesharpsoft.intellij.plugins.csv;

import consulo.language.psi.PsiFile;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.lexer.Lexer;
import consulo.project.Project;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettings;
import org.jetbrains.annotations.NotNull;

public class CsvLexerFactory {
    protected static CsvLexerFactory INSTANCE = new CsvLexerFactory();

    public static CsvLexerFactory getInstance() {
        return INSTANCE;
    }

    public Lexer createLexer(@NotNull CsvValueSeparator separator, @NotNull CsvEscapeCharacter escapeCharacter) {
        return createLexer(separator, escapeCharacter, CsvEditorSettings.getInstance().getCommentIndicator());
    }

    /**
     * Creates the lexer for the given comment indicator, without reading any settings - so it may be called on any thread.
     *
     * @param commentIndicator the text which starts a comment line, or an empty text for no comments
     */
    public Lexer createLexer(@NotNull CsvValueSeparator separator,
                             @NotNull CsvEscapeCharacter escapeCharacter,
                             @NotNull String commentIndicator) {
        if (separator.requiresCustomLexer() || !commentIndicator.isEmpty()) {
            return new CsvSharpLexer(new CsvSharpLexer.Configuration(
                    separator.getCharacter(),
                    "\n",
                    escapeCharacter.getCharacter(),
                    "\"",
                    commentIndicator));
        }
        return new CsvLexerAdapter(separator, escapeCharacter);
    }

    public Lexer createLexer(Project project, VirtualFile file) {
        return createLexer(CsvHelper.getValueSeparator(project, file), CsvHelper.getEscapeCharacter(project, file));
    }

    public Lexer createLexer(@NotNull PsiFile file) {
        return createLexer(CsvHelper.getValueSeparator(file), CsvHelper.getEscapeCharacter(file));
    }
}
