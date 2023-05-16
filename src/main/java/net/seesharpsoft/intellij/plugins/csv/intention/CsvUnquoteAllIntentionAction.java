package net.seesharpsoft.intellij.plugins.csv.intention;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.ast.TokenType;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
@IntentionMetaData(ignoreId = "csv.CsvUnquoteAllIntentionAction", fileExtensions = "csv", categories = "CSV/TSV/PSV")
public class CsvUnquoteAllIntentionAction extends CsvIntentionAction {

    public CsvUnquoteAllIntentionAction() {
        super("Unquote All");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {
        if (!super.isAvailable(project, editor, element)) {
            return false;
        }

        return !CsvIntentionHelper.getAllElements(element.getContainingFile()).stream()
                .anyMatch(psiElement -> CsvHelper.getElementType(psiElement) == TokenType.ERROR_ELEMENT);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiElement element) throws IncorrectOperationException {
        CsvIntentionHelper.unquoteAll(project, element.getContainingFile());
    }

}