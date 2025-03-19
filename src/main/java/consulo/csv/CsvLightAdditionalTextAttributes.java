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
public class CsvLightAdditionalTextAttributes implements EditorColorSchemeExtender {
    @Nonnull
    @Override
    public String getColorSchemeId() {
        return EditorColorsScheme.DEFAULT_SCHEME_NAME;
    }

    @Override
    public void extend(Builder builder) {
        RGBColor[] colors = new RGBColor[]{
            new RGBColor(0x19, 0x76, 0xD2),
            new RGBColor(0xE6, 0x51, 0x00),
            new RGBColor(0x00, 0xC8, 0x53),
            new RGBColor(0xB3, 0x88, 0xFF),
            new RGBColor(0xF9, 0xA8, 0x25),
            new RGBColor(0x1B, 0x5E, 0x20),
            new RGBColor(0xFF, 0x57, 0x22),
            new RGBColor(0x5E, 0x35, 0xB1),
            new RGBColor(0x19, 0x76, 0xD2),
            new RGBColor(0x82, 0x77, 0x17),
        };

        for (int i = 0; i < colors.length; i++) {
            TextAttributesKey key = CsvColorSettings.COLUMN_COLORING_ATTRIBUTES.get(i);
            
            builder.add(key, AttributesFlyweightBuilder.create().withForeground(colors[i]).build());
        }
    }
}
