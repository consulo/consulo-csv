// This is a generated file. Not intended for manual editing.
package net.seesharpsoft.intellij.plugins.csv.psi.impl;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.Nonnull;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import net.seesharpsoft.intellij.plugins.csv.psi.*;

public class CsvRecordImpl extends ASTWrapperPsiElement implements CsvRecord {

  public CsvRecordImpl(ASTNode node) {
    super(node);
  }

  public void accept(@Nonnull CsvVisitor visitor) {
    visitor.visitRecord(this);
  }

  public void accept(@Nonnull PsiElementVisitor visitor) {
    if (visitor instanceof CsvVisitor) accept((CsvVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nonnull
  public List<CsvField> getFieldList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CsvField.class);
  }

  @Override
  @Nullable
  public PsiElement getComment() {
    return findChildByType(COMMENT);
  }

}
