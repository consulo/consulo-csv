package net.seesharpsoft.intellij.plugins.csv.formatter;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.*;
import consulo.language.psi.PsiElement;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class CsvFormattingModelBuilder implements FormattingModelBuilder {
    @Nonnull
    @Override
    public FormattingModel createModel(@Nonnull FormattingContext context) {
        PsiElement element = context.getPsiElement();
        CodeStyleSettings settings = context.getCodeStyleSettings();
        ASTNode root = CsvFormatHelper.getRoot(element.getNode());
        CsvFormattingInfo formattingInfo = new CsvFormattingInfo(
                settings,
                CsvFormatHelper.createSpaceBuilder(settings),
                CsvFormatHelper.createColumnInfoMap(root, settings)
        );

        return FormattingModelProvider.createFormattingModelForPsiFile(
                element.getContainingFile(),
                new CsvBlock(root, formattingInfo),
                settings
        );
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CsvLanguage.INSTANCE;
    }
}