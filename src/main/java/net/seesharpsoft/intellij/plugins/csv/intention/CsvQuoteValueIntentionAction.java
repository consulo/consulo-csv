package net.seesharpsoft.intellij.plugins.csv.intention;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvField;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
@IntentionMetaData(ignoreId = "csv.CsvQuoteValueIntentionAction", fileExtensions = "csv", categories = "CSV/TSV/PSV")
public class CsvQuoteValueIntentionAction extends CsvIntentionAction {

    public CsvQuoteValueIntentionAction() {
        super("Quote");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable final PsiElement psiElement) {
        if (!super.isAvailable(project, editor, psiElement)) {
            return false;
        }

        PsiElement element = psiElement == null ? null : CsvHelper.getParentFieldElement(psiElement);
        return element instanceof CsvField &&
                element.getFirstChild() != null &&
                (CsvHelper.getElementType(element.getFirstChild()) != CsvTypes.QUOTE ||
                        CsvHelper.getElementType(element.getLastChild()) != CsvTypes.QUOTE);
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        CsvIntentionHelper.quoteValue(project, CsvHelper.getParentFieldElement(element));
    }

}