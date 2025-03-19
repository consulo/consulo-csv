package consulo.csv;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.AttributesFlyweightBuilder;
import consulo.colorScheme.EditorColorSchemeExtender;
import consulo.colorScheme.EditorColorsScheme;
import consulo.colorScheme.TextAttributesKey;
import consulo.ui.color.RGBColor;
import jakarta.annotation.Nonnull;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvColorSettings;

/**
 * @author VISTALL
 * @since 16/05/2023
 */
@ExtensionImpl
public class CsvDarkAdditionalTextAttributes implements EditorColorSchemeExtender {
    @Nonnull
    @Override
    public String getColorSchemeId() {
        return EditorColorsScheme.DARCULA_SCHEME_NAME;
    }

    @Override
    public void extend(Builder builder) {
        RGBColor[] colors = new RGBColor[]{
            new RGBColor(0xFF, 0xA0, 0x00),
            new RGBColor(0xD3, 0x2F, 0x2F),
            new RGBColor(0xCE, 0x93, 0xD8),
            new RGBColor(0x00, 0x89, 0x7B),
            new RGBColor(0xBC, 0xAA, 0xA4),
            new RGBColor(0x8B, 0xC3, 0x4A),
            new RGBColor(0x80, 0xD8, 0xFF),
            new RGBColor(0xE6, 0xEE, 0x9C),
            new RGBColor(0xCF, 0xD8, 0xDC),
            new RGBColor(0xB3, 0x88, 0xFF),
        };

        for (int i = 0; i < colors.length; i++) {
            TextAttributesKey key = CsvColorSettings.COLUMN_COLORING_ATTRIBUTES.get(i);

            builder.add(key, AttributesFlyweightBuilder.create().withForeground(colors[i]).build());
        }
    }
}
