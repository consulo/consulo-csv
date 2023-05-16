package net.seesharpsoft.intellij.plugins.csv.highlighter;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiFile;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerFactory;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
public class CsvHighlightUsagesHandlerFactory implements HighlightUsagesHandlerFactory {
    @Nullable
    @Override
    public HighlightUsagesHandlerBase createHighlightUsagesHandler(@NotNull Editor editor, @NotNull PsiFile psiFile) {
        if (psiFile instanceof CsvFile) {
            return new CsvHighlightUsagesHandler(editor, (CsvFile) psiFile);
        }
        return null;
    }
}
