package net.seesharpsoft.intellij.plugins.csv.formatter;

import consulo.language.codeStyle.*;
import consulo.language.impl.ast.ASTFactory;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenType;
import consulo.language.ast.IElementType;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static net.seesharpsoft.intellij.plugins.csv.psi.CsvElementType.DOCUMENT_START;

public class CsvBlock extends AbstractBlock {
    protected CsvFormattingInfo myFormattingInfo;

    protected CsvBlock(@NotNull ASTNode node, CsvFormattingInfo formattingInfo) {
        super(node, Wrap.createWrap(WrapType.NONE, false), Alignment.createAlignment());
        this.myFormattingInfo = formattingInfo;
    }

    private void validateBlocks(List<Block> blocks) {
        if (blocks.isEmpty()) {
            return;
        }
        blocks.add(0, new CsvBlockElement(ASTFactory.leaf(DOCUMENT_START, ""), myFormattingInfo));
    }

    @Override
    protected List<Block> buildChildren() {
        List<ASTNode> todoNodes = new ArrayList<>();
        List<Block> blocks = new ArrayList<>();
        todoNodes.add(getNode().getFirstChildNode());
        CsvBlockField currentField = null;
        while (todoNodes.size() > 0) {
            ASTNode node = todoNodes.remove(todoNodes.size() - 1);
            if (node == null) {
                continue;
            }

            IElementType elementType = node.getElementType();
            todoNodes.add(node.getTreeNext());
            if (elementType == CsvTypes.RECORD) {
                todoNodes.add(node.getFirstChildNode());
            } else if (elementType == CsvTypes.FIELD) {
                currentField = new CsvBlockField(node, myFormattingInfo);
                if (currentField.getTextLength() > 0) {
                    blocks.add(currentField);
                }
            } else if (elementType == CsvTypes.COMMA || elementType == CsvTypes.CRLF) {
                blocks.add(new CsvBlockElement(node, myFormattingInfo, currentField));
            } else if (elementType != TokenType.WHITE_SPACE && node.getTextLength() > 0) {
                blocks.add(new CsvDummyBlock(node, myFormattingInfo));
            }
        }
        validateBlocks(blocks);
        return blocks;
    }

    @Override
    public Indent getIndent() {
        return null;
    }

    @Nullable
    @Override
    public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
        Spacing spacing = null;
        if (child1 != null && !(child1 instanceof CsvDummyBlock) && !(child2 instanceof CsvDummyBlock)) {
            CsvBlockElement block1 = (CsvBlockElement) child1;
            CsvBlockElement block2 = (CsvBlockElement) child2;
            if (myFormattingInfo.getCsvCodeStyleSettings().TABULARIZE && isTabularizeSpacingRequired(block1, block2)) {
                int spaces = block2.getColumnInfo().getMaxLength() - block2.getField().getTextLength() + getAdditionalSpaces(block1, block2);
                spacing = Spacing.createSpacing(spaces, spaces, 0, true, 0);
            } else {
                spacing = myFormattingInfo.getSpacingBuilder().getSpacing(this, child1, child2);
            }
        }
        return spacing;
    }

    private boolean isTabularizeSpacingRequired(@NotNull CsvBlockElement block1, @NotNull CsvBlockElement block2) {
        return isAnyBlockASpacingSeparator(block1, block2) &&
                (myFormattingInfo.getCsvCodeStyleSettings().WHITE_SPACES_OUTSIDE_QUOTES ||
                        (!CsvFormatHelper.isQuotedField(block1) && !CsvFormatHelper.isQuotedField(block2)));
    }

    private boolean isAnyBlockASpacingSeparator(@NotNull CsvBlockElement block1, @NotNull CsvBlockElement block2) {
        return ((block1.getElementType() != CsvTypes.COMMA && block1.getElementType() != CsvTypes.CRLF) || block2.getElementType() != CsvTypes.CRLF) &&
                (
                        (myFormattingInfo.getCsvCodeStyleSettings().LEADING_WHITE_SPACES &&
                                (block1.getElementType() == DOCUMENT_START || block1.getElementType() == CsvTypes.COMMA || block1.getElementType() == CsvTypes.CRLF)
                        ) ||
                                (!myFormattingInfo.getCsvCodeStyleSettings().LEADING_WHITE_SPACES &&
                                        (block2.getElementType() == CsvTypes.COMMA && block2.getElementType() != CsvTypes.CRLF)
                                )
                );
    }

    protected int getAdditionalSpaces(@Nullable CsvBlockElement child1, @NotNull CsvBlockElement child2) {
        int spaces = 0;
        if (myFormattingInfo.getCsvCodeStyleSettings().SPACE_AFTER_SEPARATOR && child1.getElementType() == CsvTypes.COMMA) {
            ++spaces;
        }
        if (myFormattingInfo.getCsvCodeStyleSettings().SPACE_BEFORE_SEPARATOR && child2.getElementType() == CsvTypes.COMMA) {
            ++spaces;
        }
        return spaces;
    }

    @Override
    public boolean isLeaf() {
        return getNode().getFirstChildNode() == null;
    }

    public final IElementType getElementType() {
        return getNode().getElementType();
    }

    public int getTextLength() {
        return getTextRange().getLength();
    }

}
