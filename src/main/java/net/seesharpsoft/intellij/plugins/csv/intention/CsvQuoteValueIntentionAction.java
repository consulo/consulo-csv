package net.seesharpsoft.intellij.plugins.csv.intention;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvField;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;

@ExtensionImpl
@IntentionMetaData(ignoreId = "csv.CsvQuoteValueIntentionAction", fileExtensions = "csv", categories = "CSV/TSV/PSV")
public class CsvQuoteValueIntentionAction extends CsvIntentionAction {
    public CsvQuoteValueIntentionAction() {
        super(LocalizeValue.localizeTODO("Quote"));
    }

    @Override
    @RequiredReadAction
    public boolean isAvailable(@Nonnull Project project, Editor editor, @Nullable final PsiElement psiElement) {
        if (!super.isAvailable(project, editor, psiElement)) {
            return false;
        }

        PsiElement element = psiElement == null ? null : CsvHelper.getParentFieldElement(psiElement);
        return element instanceof CsvField field
            && field.getFirstChild() != null
            && (CsvHelper.getElementType(field.getFirstChild()) != CsvTypes.QUOTE
                || CsvHelper.getElementType(field.getLastChild()) != CsvTypes.QUOTE);
    }

    @Override
    @RequiredWriteAction
    public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
        CsvIntentionHelper.quoteValue(project, CsvHelper.getParentFieldElement(element));
    }
}