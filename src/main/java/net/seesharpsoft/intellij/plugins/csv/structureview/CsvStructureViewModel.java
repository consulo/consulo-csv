package net.seesharpsoft.intellij.plugins.csv.structureview;

import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.language.editor.structureView.StructureViewModelBase;
import consulo.fileEditor.structureView.tree.Sorter;
import consulo.language.psi.PsiFile;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import org.jetbrains.annotations.NotNull;

public class CsvStructureViewModel extends StructureViewModelBase
        implements StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {

    public CsvStructureViewModel(PsiFile psiFile) {
        super(psiFile, new CsvStructureViewElement.File(psiFile));
    }

    @Override
    @NotNull
    public Sorter[] getSorters() {
        return Sorter.EMPTY_ARRAY;
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element instanceof CsvFile;
    }

    @Override
    public boolean isAutoExpand(@NotNull StructureViewTreeElement element) {
        return false;
    }

    @Override
    public boolean isSmartExpand() {
        return true;
    }
}