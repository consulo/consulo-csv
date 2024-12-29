package net.seesharpsoft.intellij.plugins.csv.highlighter;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.EditorColorsScheme;
import consulo.codeEditor.EditorHighlighter;
import consulo.language.editor.highlight.EditorHighlighterFactory;
import consulo.language.editor.highlight.EditorHighlighterProvider;
import consulo.virtualFileSystem.VirtualFile;
import consulo.project.Project;
import consulo.virtualFileSystem.fileType.FileType;
import net.seesharpsoft.intellij.plugins.csv.CsvFileType;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class CsvEditorHighlighterProvider implements EditorHighlighterProvider {
    @Override
    public EditorHighlighter getEditorHighlighter(@Nullable Project project, @NotNull FileType fileType, @Nullable VirtualFile virtualFile, @NotNull EditorColorsScheme colors) {
        return EditorHighlighterFactory.getInstance().createEditorHighlighter(CsvSyntaxHighlighterFactory.getSyntaxHighlighter(CsvLanguage.INSTANCE, project, virtualFile), colors);
    }

    @Nonnull
    @Override
    public FileType getFileType() {
        return CsvFileType.INSTANCE;
    }
}
