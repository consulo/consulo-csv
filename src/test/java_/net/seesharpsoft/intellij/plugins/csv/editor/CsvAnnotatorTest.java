package net.seesharpsoft.intellij.plugins.csv.editor;

import consulo.language.cacheBuilder.CacheManager;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.disposer.Disposable;
import consulo.ide.ServiceManager;
import consulo.language.impl.ast.FileElement;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.ide.impl.idea.openapi.util.Disposer;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.ExpectedHighlightingData;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import net.seesharpsoft.intellij.plugins.csv.editor.table.ExpectedHighlightingDataWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static net.seesharpsoft.intellij.plugins.csv.editor.CsvAnnotator.CSV_COLUMN_INFO_SEVERITY;

public class CsvAnnotatorTest extends BasePlatformTestCase {

    @Override
    protected String getTestDataPath() {
        return "./src/test/resources";
    }

    public void testAnnotator() {
        myFixture.configureByFile("AnnotatorTestData.csv");
        this.collectAndCheckHighlighting();
    }

    private long collectAndCheckHighlighting() {
        ExpectedHighlightingDataWrapper data = new ExpectedHighlightingDataWrapper(myFixture.getEditor().getDocument(), false, false, true);
        data.registerHighlightingType("csv_column_info", new ExpectedHighlightingData.ExpectedHighlightingSet(CSV_COLUMN_INFO_SEVERITY, false, true));
        data.init();
        return this.collectAndCheckHighlighting(data);
    }

    private PsiFile getHostFile() {
        return myFixture.getFile();
    }

    private long collectAndCheckHighlighting(@NotNull ExpectedHighlightingDataWrapper data) {
        Project project = myFixture.getProject();
        EdtTestUtil.runInEdtAndWait(() -> {
            PsiDocumentManager.getInstance(project).commitAllDocuments();
        });
        PsiFileImpl file = (PsiFileImpl)this.getHostFile();
        FileElement hardRefToFileElement = file.calcTreeElement();
        if (!DumbService.isDumb(project)) {
            ServiceManager.getService(project, CacheManager.class).getFilesWithWord("XXX", (short)2, GlobalSearchScope.allScope(project), true);
        }

        long start = System.currentTimeMillis();
        Disposable disposable = consulo.ide.impl.idea.openapi.util.Disposer.newDisposable();

        List<HighlightInfo> infos;
        try {
            infos = myFixture.doHighlighting();
            this.removeDuplicatedRangesForInjected(infos);
        } finally {
            Disposer.dispose(disposable);
        }

        long elapsed = System.currentTimeMillis() - start;
        data.checkResultWrapper(file, infos, file.getText());
        hardRefToFileElement.hashCode();
        return elapsed;
    }

    private static void removeDuplicatedRangesForInjected(@NotNull List<HighlightInfo> infos) {
        Collections.sort(infos, (o1, o2) -> {
            int i = o2.startOffset - o1.startOffset;
            return i != 0 ? i : o1.getSeverity().myVal - o2.getSeverity().myVal;
        });
        HighlightInfo prevInfo = null;

        HighlightInfo info;
        for(Iterator it = infos.iterator(); it.hasNext(); prevInfo = info.type == HighlightInfoType.INJECTED_LANGUAGE_FRAGMENT ? info : null) {
            info = (HighlightInfo)it.next();
            if (prevInfo != null && info.getSeverity() == HighlightInfoType.SYMBOL_TYPE_SEVERITY && info.getDescription() == null && info.startOffset == prevInfo.startOffset && info.endOffset == prevInfo.endOffset) {
                it.remove();
            }
        }

    }

}
