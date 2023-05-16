package net.seesharpsoft.intellij.plugins.psv;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.IFileElementType;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiFile;
import net.seesharpsoft.intellij.plugins.csv.CsvParserDefinition;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFileElementType;

import javax.annotation.Nonnull;

@ExtensionImpl
public class PsvParserDefinition extends CsvParserDefinition {
    public static final IFileElementType PSV_FILE = new CsvFileElementType(PsvLanguage.INSTANCE);

    @Override
    public IFileElementType getFileNodeType() {
        return PSV_FILE;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return PsvLanguage.INSTANCE;
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new CsvFile(viewProvider, PsvFileType.INSTANCE);
    }
}
