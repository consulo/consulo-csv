package net.seesharpsoft.intellij.plugins.csv.psi;

import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IFileElementType;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.version.LanguageVersion;
import consulo.project.Project;
import net.seesharpsoft.intellij.lang.FileParserDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CsvFileElementType extends IFileElementType {
    public CsvFileElementType(@Nullable Language language) {
        super(language);
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
        PsiFile file = (PsiFile) psi;
        Project project = file.getProject();
        Language languageForParser = this.getLanguageForParser(file);
        LanguageVersion version = psi.getLanguageVersion();
        FileParserDefinition parserDefinition = (FileParserDefinition) ParserDefinition.forLanguage(languageForParser);
        PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, parserDefinition.createLexer(file), languageForParser, version, chameleon.getChars());
        PsiParser parser = parserDefinition.createParser(file);
        ASTNode node = parser.parse(this, builder, version);
        return node.getFirstChildNode();
    }
}
