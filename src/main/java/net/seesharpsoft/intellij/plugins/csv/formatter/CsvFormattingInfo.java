package net.seesharpsoft.intellij.plugins.csv.formatter;

import consulo.language.ast.ASTNode;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.SpacingBuilder;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfo;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfoMap;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvCodeStyleSettings;

import java.util.Map;

public class CsvFormattingInfo extends CsvColumnInfoMap<ASTNode> {

    private SpacingBuilder mySpacingBuilder;
    private CodeStyleSettings myCodeStyleSettings;

    public SpacingBuilder getSpacingBuilder() {
        return mySpacingBuilder;
    }

    public CsvCodeStyleSettings getCsvCodeStyleSettings() {
        return myCodeStyleSettings.getCustomSettings(CsvCodeStyleSettings.class);
    }

    public CodeStyleSettings getCodeStyleSettings() {
        return myCodeStyleSettings;
    }

    public CsvFormattingInfo(CodeStyleSettings codeStyleSettings, SpacingBuilder spacingBuilder, Map<Integer, CsvColumnInfo<ASTNode>> infoColumnMap) {
        super(infoColumnMap);
        this.mySpacingBuilder = spacingBuilder;
        this.myCodeStyleSettings = codeStyleSettings;
    }
}
