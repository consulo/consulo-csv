// This is a generated file. Not intended for manual editing.
package net.seesharpsoft.intellij.plugins.csv.psi;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import com.intellij.psi.PsiElement;

public interface CsvRecord extends PsiElement {

  @Nonnull
  List<CsvField> getFieldList();

  @Nullable
  PsiElement getComment();

}
