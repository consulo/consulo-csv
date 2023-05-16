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

    protected Lexer createLexer(@NotNull CsvValueSeparator separator, @NotNull CsvEscapeCharacter escapeCharacter) {
        if (separator.requiresCustomLexer() || !CsvEditorSettings.getInstance().getCommentIndicator().isEmpty()) {
            return new CsvSharpLexer(new CsvSharpLexer.Configuration(
                    separator.getCharacter(),
                    "\n",
                    escapeCharacter.getCharacter(),
                    "\"",
                    CsvEditorSettings.getInstance().getCommentIndicator()));
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
