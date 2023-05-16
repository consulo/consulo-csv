package net.seesharpsoft.intellij.plugins.csv.editor;

import consulo.colorScheme.TextAttributes;
import consulo.document.util.TextRange;
import consulo.language.ast.IElementType;
import consulo.language.editor.annotation.*;
import consulo.language.psi.PsiElement;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.util.dataholder.Key;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfo;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.CsvValueSeparator;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvColorSettings;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettings;
import org.jetbrains.annotations.NotNull;

import static consulo.language.editor.annotation.HighlightSeverity.INFORMATION;

@SuppressWarnings("MagicNumber")
public class CsvAnnotator implements Annotator {

    protected static final Key<TextAttributes> TAB_SEPARATOR_HIGHLIGHT_COLOR_KEY = Key.create("CSV_PLUGIN_TAB_SEPARATOR_HIGHLIGHT_COLOR");
    protected static final Key<Boolean> TAB_SEPARATOR_HIGHLIGHT_COLOR_DETERMINED_KEY = Key.create("CSV_PLUGIN_TAB_SEPARATOR_HIGHLIGHT_COLOR_DETERMINED");
    protected static final Key<Boolean> SHOW_INFO_BALLOON_KEY = Key.create("CSV_PLUGIN_SHOW_INFO_BALLOON");

    public static final HighlightSeverity CSV_COLUMN_INFO_SEVERITY =
            new HighlightSeverity("CSV_COLUMN_INFO_SEVERITY", INFORMATION.myVal - 1);

    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull final AnnotationHolder holder) {
        IElementType elementType = CsvHelper.getElementType(element);
        if ((elementType != CsvTypes.FIELD && elementType != CsvTypes.COMMA) || !(element.getContainingFile() instanceof CsvFile)) {
            return;
        }

        CsvFile csvFile = (CsvFile) element.getContainingFile();
        if (handleSeparatorElement(element, holder, elementType, csvFile)) {
            return;
        }

        CsvColumnInfo<PsiElement> columnInfo = csvFile.getColumnInfoMap().getColumnInfo(element);

        if (columnInfo != null) {
            PsiElement headerElement = columnInfo.getHeaderElement();
            String message = consulo.util.lang.xml.XmlStringUtil.escapeString(headerElement == null ? "" : headerElement.getText(), true);
            String tooltip = null;
            if (showInfoBalloon(holder.getCurrentAnnotationSession())) {
                tooltip = consulo.util.lang.xml.XmlStringUtil.wrapInHtml(
                        String.format("%s<br /><br />Header: %s<br />Index: %d",
                                consulo.util.lang.xml.XmlStringUtil.escapeString(element.getText(), true),
                                message,
                                columnInfo.getColumnIndex() + (CsvEditorSettings.getInstance().isZeroBasedColumnNumbering() ? 0 : 1)
                        )
                );
            }
            TextRange textRange = columnInfo.getRowInfo(element).getTextRange();
            if (textRange.getStartOffset() - csvFile.getTextLength() == 0 && textRange.getStartOffset() > 0) {
                textRange = TextRange.from(textRange.getStartOffset() - 1, 1);
            }

            final Annotation
              annotation = holder.createAnnotation(CSV_COLUMN_INFO_SEVERITY, textRange, message/*, tooltip*/);
            annotation.setEnforcedTextAttributes(
                    CsvEditorSettings.getInstance().getValueColoring() == CsvEditorSettings.ValueColoring.RAINBOW ?
                            CsvColorSettings.getTextAttributesOfColumn(columnInfo.getColumnIndex(), holder.getCurrentAnnotationSession()) :
                            null
            );
            annotation.setNeedsUpdateOnTyping(false);
        }
    }

    protected boolean showInfoBalloon(@NotNull AnnotationSession annotationSession) {
        Boolean showInfoBalloon = annotationSession.getUserData(SHOW_INFO_BALLOON_KEY);
        if (showInfoBalloon == null) {
            showInfoBalloon = CsvEditorSettings.getInstance().isShowInfoBalloon();
            annotationSession.putUserData(SHOW_INFO_BALLOON_KEY, showInfoBalloon);
        }
        return showInfoBalloon;
    }

    protected boolean handleSeparatorElement(@NotNull PsiElement element, @NotNull AnnotationHolder holder, IElementType elementType, CsvFile csvFile) {
        if (elementType == CsvTypes.COMMA) {
            TextAttributes textAttributes = holder.getCurrentAnnotationSession().getUserData(TAB_SEPARATOR_HIGHLIGHT_COLOR_KEY);
            if (!Boolean.TRUE.equals(holder.getCurrentAnnotationSession().getUserData(TAB_SEPARATOR_HIGHLIGHT_COLOR_DETERMINED_KEY))) {
                CsvValueSeparator separator = CsvHelper.getValueSeparator(csvFile);
                if (CsvEditorSettings.getInstance().isHighlightTabSeparator() && separator.equals(CsvValueSeparator.TAB)) {
                    textAttributes = new TextAttributes(null,
                                                                            TargetAWT.from(CsvEditorSettings.getInstance().getTabHighlightColor()),
                                                                            null, null, 0);
                    holder.getCurrentAnnotationSession().putUserData(TAB_SEPARATOR_HIGHLIGHT_COLOR_KEY, textAttributes);
                    holder.getCurrentAnnotationSession().putUserData(TAB_SEPARATOR_HIGHLIGHT_COLOR_DETERMINED_KEY, Boolean.TRUE);
                }
            }
            if (textAttributes != null) {
                final TextRange textRange = element.getTextRange();
                final Annotation
                  annotation = holder.createAnnotation(CSV_COLUMN_INFO_SEVERITY, textRange, showInfoBalloon(holder.getCurrentAnnotationSession()) ? "↹" : null);
                annotation.setEnforcedTextAttributes(textAttributes);
                annotation.setNeedsUpdateOnTyping(false);
            }
            return true;
        }
        return false;
    }
}
