package net.seesharpsoft.intellij.plugins.csv.highlighter;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighterFactory;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

@ExtensionImpl
public class CsvSyntaxHighlighterFactory extends SyntaxHighlighterFactory {
    @NotNull
    @Override
    public SyntaxHighlighter getSyntaxHighlighter(Project project, VirtualFile virtualFile) {
        return new CsvSyntaxHighlighter(project, virtualFile);
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CsvLanguage.INSTANCE;
    }
}