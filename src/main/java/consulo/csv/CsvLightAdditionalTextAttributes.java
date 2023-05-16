package consulo.csv;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.AdditionalTextAttributesProvider;
import consulo.colorScheme.EditorColorsScheme;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 16/05/2023
 */
@ExtensionImpl
public class CsvLightAdditionalTextAttributes implements AdditionalTextAttributesProvider {
    @Nonnull
    @Override
    public String getColorSchemeName() {
        return EditorColorsScheme.DEFAULT_SCHEME_NAME;
    }

    @Nonnull
    @Override
    public String getColorSchemeFile() {
        return "/misc/column_coloring_default.xml";
    }
}
