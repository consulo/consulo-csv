package net.seesharpsoft.intellij.plugins.csv.psi;

import consulo.language.ast.IElementType;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CsvTokenType extends IElementType {
    public CsvTokenType(@NotNull @NonNls String debugName) {
        super(debugName, CsvLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "CsvTokenType." + super.toString();
    }
}