package net.seesharpsoft.intellij.plugins.csv.formatter;

import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenType;
import consulo.language.codeStyle.Block;
import consulo.language.codeStyle.Spacing;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfo;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CsvBlockField extends CsvBlockElement {

    protected CsvColumnInfo columnInfo;

    public CsvBlockField(ASTNode node, CsvFormattingInfo formattingInfo) {
        super(node, formattingInfo, null);
        columnInfo = formattingInfo.getColumnInfo(node);
    }

    @Override
    public CsvColumnInfo getColumnInfo() {
        return columnInfo;
    }

    @Override
    public CsvBlockField getField() {
        return this;
    }

    @Override
    protected List<Block> buildChildren() {
        ASTNode node = this.getNode().getFirstChildNode();
        List<Block> blocks = new ArrayList<>();
        while (node != null) {
            if (node.getElementType() != TokenType.WHITE_SPACE) {
                CsvBlockElement block = new CsvBlockElement(node, myFormattingInfo, this);
                blocks.add(block);
            }
            node = node.getTreeNext();
        }
        return blocks;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        if (!myFormattingInfo.getCsvCodeStyleSettings().TABULARIZE || myFormattingInfo.getCsvCodeStyleSettings().WHITE_SPACES_OUTSIDE_QUOTES || child1 == null) {
            return null;
        }
        int spaces = 0;
        CsvBlockElement block1 = (CsvBlockElement) child1;
        CsvBlockElement block2 = (CsvBlockElement) child2;
        if ((block1.getElementType() == CsvTypes.QUOTE && myFormattingInfo.getCsvCodeStyleSettings().LEADING_WHITE_SPACES) ||
                (block2.getElementType() == CsvTypes.QUOTE && !myFormattingInfo.getCsvCodeStyleSettings().LEADING_WHITE_SPACES)) {
            spaces = getColumnInfo().getMaxLength() - getTextLength();
        } else {
            return myFormattingInfo.getSpacingBuilder().getSpacing(this, child1, child2);
        }
        return Spacing.createSpacing(spaces, spaces, 0, true, 0);
    }

    @Override
    public int getTextLength() {
        return CsvFormatHelper.getTextLength(getNode(), myFormattingInfo.getCodeStyleSettings());
    }
}
