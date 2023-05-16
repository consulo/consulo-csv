package net.seesharpsoft.intellij.plugins.tsv;

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
public class TsvParserDefinition extends CsvParserDefinition {
    public static final IFileElementType TSV_FILE = new CsvFileElementType(TsvLanguage.INSTANCE);

    @Nonnull
    @Override
    public Language getLanguage() {
        return TsvLanguage.INSTANCE;
    }

    @Override
    public IFileElementType getFileNodeType() {
        return TSV_FILE;
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new CsvFile(viewProvider, TsvFileType.INSTANCE);
    }
}
