package net.seesharpsoft.intellij.plugins.csv.inspection;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.document.Document;
import consulo.language.Language;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenType;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.Project;
import jakarta.annotation.Nullable;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import net.seesharpsoft.intellij.plugins.csv.CsvValueSeparator;
import net.seesharpsoft.intellij.plugins.csv.intention.CsvIntentionHelper;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author max
 */
@ExtensionImpl
public class CsvValidationInspection extends LocalInspectionTool {
    private static final Logger LOG = Logger.getInstance("#net.seesharpsoft.intellij.plugins.csv.inspection.CsvSyntaxInspection");

    private final LocalQuickFix fixUnescapedSequence = new UnescapedSequenceFix();
    private final LocalQuickFix fixSeparatorMissing = new SeparatorMissingFix();
    private final LocalQuickFix fixClosingQuoteMissing = new ClosingQuoteMissingFix();

    private static final String UNESCAPED_SEQUENCE = "Unescaped sequence";
    private static final String SEPARATOR_MISSING = "Separator missing";
    private static final String CLOSING_QUOTE_MISSING = "Quote missing";

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Propose possible fixes");
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return CsvLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @Nullable
    @Override
    public String getStaticDescription() {
        return "Propose possible fixes to invalid syntax in CSV files.";
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return LocalizeValue.localizeTODO("General");
    }

    @Nonnull
    @Override
    public String getShortName() {
        return "CsvValidation";
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            @RequiredReadAction
            public void visitElement(PsiElement element) {
                if (element == null || !holder.getFile().getLanguage().isKindOf(CsvLanguage.INSTANCE)) {
                    return;
                }

                IElementType elementType = CsvHelper.getElementType(element);
                PsiElement firstChild = element.getFirstChild();
                PsiElement nextSibling = element.getNextSibling();
                if (elementType == TokenType.ERROR_ELEMENT && firstChild != null && element.getText().equals(firstChild.getText())) {
                    CsvValidationInspection.this.registerError(holder, element, UNESCAPED_SEQUENCE, fixUnescapedSequence);
                    if (!"\"".equals(firstChild.getText())) {
                        CsvValidationInspection.this.registerError(holder, element, SEPARATOR_MISSING, fixSeparatorMissing);
                    }
                }
                else if ((elementType == CsvTypes.TEXT || elementType == CsvTypes.ESCAPED_TEXT)
                    && CsvHelper.getElementType(nextSibling) == TokenType.ERROR_ELEMENT
                    && nextSibling.getFirstChild() == null) {
                    CsvValidationInspection.this.registerError(holder, element, CLOSING_QUOTE_MISSING, fixClosingQuoteMissing);
                }
            }
        };
    }

    private boolean registerError(
        @Nonnull final ProblemsHolder holder,
        @Nonnull PsiElement element,
        @Nonnull String descriptionTemplate,
        @Nullable LocalQuickFix fix
    ) {
        if (this.isSuppressedFor(element)) {
            return false;
        }

        holder.newProblem(LocalizeValue.of(descriptionTemplate))
            .range(element)
            .withFixes(fix)
            .create();
        return true;
    }

    private static class UnescapedSequenceFix implements LocalQuickFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return LocalizeValue.localizeTODO("Surround with quotes");
        }

        @Override
        @RequiredWriteAction
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            try {
                PsiElement element = descriptor.getPsiElement();
                Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
                List<Integer> quotePositions = new ArrayList<>();

                int quotePosition = CsvIntentionHelper.getOpeningQuotePosition(element);
                if (quotePosition != -1) {
                    quotePositions.add(quotePosition);
                }
                PsiElement
                    endSeparatorElement = CsvIntentionHelper.findQuotePositionsUntilSeparator(element, quotePositions, true);
                if (endSeparatorElement == null) {
                    quotePositions.add(document.getTextLength());
                }
                else {
                    quotePositions.add(endSeparatorElement.getTextOffset());
                }
                String text = CsvIntentionHelper.addQuotes(document.getText(), quotePositions);
                document.setText(text);
            }
            catch (IncorrectOperationException e) {
                LOG.error(e);
            }
        }
    }

    private static class SeparatorMissingFix implements LocalQuickFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return LocalizeValue.localizeTODO("Add separator");
        }

        @Override
        @RequiredWriteAction
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            try {
                PsiElement element = descriptor.getPsiElement();
                Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
                CsvValueSeparator separator = CsvHelper.getValueSeparator(element.getContainingFile());
                String text = document.getText();
                document.setText(
                    text.substring(0, element.getTextOffset()) + separator.getCharacter() + text.substring(element.getTextOffset())
                );
            }
            catch (IncorrectOperationException e) {
                LOG.error(e);
            }
        }
    }

    private static class ClosingQuoteMissingFix implements LocalQuickFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return LocalizeValue.localizeTODO("Add closing quote");
        }

        @Override
        @RequiredReadAction
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            try {
                PsiElement element = descriptor.getPsiElement();
                Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
                document.setText(document.getText() + "\"");
            }
            catch (IncorrectOperationException e) {
                LOG.error(e);
            }
        }
    }
}