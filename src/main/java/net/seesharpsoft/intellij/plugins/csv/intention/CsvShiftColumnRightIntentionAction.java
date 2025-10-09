package net.seesharpsoft.intellij.plugins.csv.intention;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfo;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfoMap;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;

@ExtensionImpl
@IntentionMetaData(ignoreId = "csv.CsvShiftColumnRightIntentionAction", fileExtensions = "csv", categories = "CSV/TSV/PSV")
public class CsvShiftColumnRightIntentionAction extends CsvShiftColumnIntentionAction {
    public CsvShiftColumnRightIntentionAction() {
        super(LocalizeValue.localizeTODO("Shift Column Right"));
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement psiElement) throws IncorrectOperationException {
        CsvFile csvFile = (CsvFile) psiElement.getContainingFile();

        PsiElement element = CsvHelper.getParentFieldElement(psiElement);

        CsvColumnInfoMap<PsiElement> columnInfoMap = csvFile.getColumnInfoMap();
        CsvColumnInfo<PsiElement> leftColumnInfo = columnInfoMap.getColumnInfo(element);

        // column must be at least index 1 to be shifted left
        if (leftColumnInfo == null || leftColumnInfo.getColumnIndex() + 1 >= columnInfoMap.getColumnInfos().size()) {
            return;
        }

        CsvColumnInfo<PsiElement> rightColumnInfo = columnInfoMap.getColumnInfo(leftColumnInfo.getColumnIndex() + 1);

        changeLeftAndRightColumnOrder(project, csvFile, leftColumnInfo, rightColumnInfo);
    }
}
