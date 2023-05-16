package net.seesharpsoft.intellij.plugins.csv.editor;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;

/**
 * @author VISTALL
 * @since 15/05/2023
 */
@ExtensionImpl
public class CsvAnnotatorFactory implements AnnotatorFactory {
    @Override
    public Annotator createAnnotator() {
        return new CsvAnnotator();
    }

    @Override
    public Language getLanguage() {
        return CsvLanguage.INSTANCE;
    }
}
