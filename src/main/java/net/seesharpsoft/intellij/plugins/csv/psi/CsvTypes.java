// This is a generated file. Not intended for manual editing.
package net.seesharpsoft.intellij.plugins.csv.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import net.seesharpsoft.intellij.plugins.csv.psi.impl.*;

public interface CsvTypes {

  IElementType FIELD = new CsvElementType("FIELD");
  IElementType RECORD = new CsvElementType("RECORD");

  IElementType COMMA = new CsvTokenType("COMMA");
  IElementType COMMENT = new CsvTokenType("COMMENT");
  IElementType CRLF = new CsvTokenType("CRLF");
  IElementType ESCAPED_TEXT = new CsvTokenType("ESCAPED_TEXT");
  IElementType ESCAPE_CHARACTER = new CsvTokenType("ESCAPE_CHARACTER");
  IElementType QUOTE = new CsvTokenType("QUOTE");
  IElementType TEXT = new CsvTokenType("TEXT");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == FIELD) {
        return new CsvFieldImpl(node);
      }
      else if (type == RECORD) {
        return new CsvRecordImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
