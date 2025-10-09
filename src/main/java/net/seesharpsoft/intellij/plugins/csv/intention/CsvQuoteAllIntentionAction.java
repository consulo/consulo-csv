package net.seesharpsoft.intellij.plugins.csv.intention;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.ast.TokenType;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;

@ExtensionImpl
@IntentionMetaData(ignoreId = "csv.CsvQuoteAllIntentionAction", fileExtensions = "csv", categories = "CSV/TSV/PSV")
public class CsvQuoteAllIntentionAction extends CsvIntentionAction {
    public CsvQuoteAllIntentionAction() {
        super(LocalizeValue.localizeTODO("Quote All"));
    }

    @Override
    @RequiredReadAction
    public boolean isAvailable(@Nonnull Project project, Editor editor, @Nullable PsiElement element) {
        if (!super.isAvailable(project, editor, element)) {
            return false;
        }

        return !CsvIntentionHelper.getAllElements(element.getContainingFile()).stream()
            .anyMatch(psiElement -> CsvHelper.getElementType(psiElement) == TokenType.ERROR_ELEMENT);
    }

    @Override
    @RequiredWriteAction
    public void invoke(@Nonnull Project project, Editor editor, PsiElement element) throws IncorrectOperationException {
        CsvIntentionHelper.quoteAll(project, element.getContainingFile());
    }
}