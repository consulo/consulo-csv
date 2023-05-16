package net.seesharpsoft.intellij.plugins.csv.highlighter;

import consulo.codeEditor.Editor;
import consulo.document.util.TextRange;
import consulo.ide.impl.idea.codeInsight.highlighting.HighlightUsagesHandler;
import consulo.externalService.statistic.FeatureUsageTracker;
import consulo.codeEditor.markup.RangeHighlighter;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import consulo.language.editor.highlight.usage.HighlightUsagesHandlerBase;

public class CsvHighlightUsagesHandlerTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "./src/test/resources/highlighter";
    }

    private void assertHighlightedText(RangeHighlighter rangeHighlighter, String text) {
        assertEquals(text, rangeHighlighter.getDocument().getText(TextRange.create(rangeHighlighter.getStartOffset(), rangeHighlighter.getEndOffset())));
    }

    private RangeHighlighter[] testHighlightUsages(String... fileNames) {
        myFixture.configureByFiles(fileNames);

        HighlightUsagesHandlerBase
          handler = consulo.ide.impl.idea.codeInsight.highlighting.HighlightUsagesHandler.createCustomHandler(myFixture.getEditor(), myFixture.getFile());

        String featureId = handler.getFeatureId();
        if (featureId != null) {
            FeatureUsageTracker.getInstance().triggerFeatureUsed(featureId);
        }

        handler.highlightUsages();

        Editor editor = myFixture.getEditor();
        return editor.getMarkupModel().getAllHighlighters();
    }

    public void testHighlightUsages01() {
        RangeHighlighter[] rangeHighlighters = testHighlightUsages("HighlightUsagesTestData01.csv");

        assertSize(2, rangeHighlighters);
        assertHighlightedText(rangeHighlighters[0], " Header 2");
        assertHighlightedText(rangeHighlighters[1], " Value 2");
    }

    public void testHighlightUsages02() {
        RangeHighlighter[] rangeHighlighters = testHighlightUsages("HighlightUsagesTestData02.csv");

        assertSize(2, rangeHighlighters);
        assertHighlightedText(rangeHighlighters[0], " Header 2");
        assertHighlightedText(rangeHighlighters[1], " Value 2");
    }

    public void testHighlightUsages03() {
        RangeHighlighter[] rangeHighlighters = testHighlightUsages("HighlightUsagesTestData03.csv");

        assertSize(2, rangeHighlighters);
        assertHighlightedText(rangeHighlighters[0], "Header 1");
        assertHighlightedText(rangeHighlighters[1], "Value 1");
    }

    public void testHighlightUsages04() {
        RangeHighlighter[] rangeHighlighters = testHighlightUsages("HighlightUsagesTestData04.csv");

        assertSize(1, rangeHighlighters);
        assertHighlightedText(rangeHighlighters[0], " Value 3");
    }

    public void testHighlightUsages05() {
        RangeHighlighter[] rangeHighlighters = testHighlightUsages("HighlightUsagesTestData05.csv");

        assertSize(2, rangeHighlighters);
        assertHighlightedText(rangeHighlighters[0], " Header 2");
        assertHighlightedText(rangeHighlighters[1], " Value 2");
    }

    public void testHighlightUsages06() {
        RangeHighlighter[] rangeHighlighters = testHighlightUsages("HighlightUsagesTestData06.csv");

        assertSize(1, rangeHighlighters);
        assertHighlightedText(rangeHighlighters[0], " Value 3");
    }

    public void testHighlightUsages07() {
        RangeHighlighter[] rangeHighlighters = testHighlightUsages("HighlightUsagesTestData07.csv");

        assertSize(0, rangeHighlighters);
    }

    public void testHighlightUsages08() {
        RangeHighlighter[] rangeHighlighters = testHighlightUsages("HighlightUsagesTestData08.csv");

        assertSize(0, rangeHighlighters);
    }
}
