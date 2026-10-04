/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.seesharpsoft.intellij.plugins.csv.editor.table;

import consulo.ui.color.ColorValue;
import consulo.ui.grid.DataGrid;
import consulo.ui.grid.GridColumn;
import consulo.ui.grid.GridRow;
import consulo.ui.grid.ModelIndex;
import consulo.ui.grid.color.ColorLayer;
import consulo.ui.grid.color.GridColorModel;
import consulo.ui.grid.color.GridColorModelImpl;
import consulo.ui.style.ComponentColors;
import org.jspecify.annotations.Nullable;

/**
 * The rainbow coloring of the table: every column takes a text color of the theme palette of grid columns, by its position in the file.
 * The text editor colors its columns with the color scheme keys of the plugin instead, which the user may edit.
 *
 * @since 2026-10-04
 */
public final class CsvTableColumnColorLayer implements ColorLayer {
    /**
     * After the layer of pending changes.
     */
    public static final int PRIORITY = 10;

    private static final ColorValue[] PALETTE = {
        ComponentColors.GRID_COLUMN_FOREGROUND_1,
        ComponentColors.GRID_COLUMN_FOREGROUND_2,
        ComponentColors.GRID_COLUMN_FOREGROUND_3,
        ComponentColors.GRID_COLUMN_FOREGROUND_4,
        ComponentColors.GRID_COLUMN_FOREGROUND_5,
        ComponentColors.GRID_COLUMN_FOREGROUND_6,
        ComponentColors.GRID_COLUMN_FOREGROUND_7,
        ComponentColors.GRID_COLUMN_FOREGROUND_8,
        ComponentColors.GRID_COLUMN_FOREGROUND_9,
        ComponentColors.GRID_COLUMN_FOREGROUND_10
    };

    public CsvTableColumnColorLayer() {
    }

    /**
     * Installs the layer on the color model of the grid when {@code enabled} is {@code true}, and removes it otherwise; it is never
     * installed twice. Safe to call from the configurator of the grid and later. A grid whose color model is not a
     * {@link GridColorModelImpl} is left alone.
     */
    public static void apply(DataGrid grid, boolean enabled) {
        GridColorModel model;
        try {
            model = grid.getColorModel();
        }
        catch (UnsupportedOperationException e) {
            // a grid without a color model of its own
            return;
        }
        if (!(model instanceof GridColorModelImpl colorModel)) {
            return;
        }
        colorModel.removeLayer(CsvTableColumnColorLayer.class);
        if (enabled) {
            colorModel.addLayer(new CsvTableColumnColorLayer());
        }
    }

    @Override
    public @Nullable ColorValue getCellBackground(ModelIndex<GridRow> row,
                                                  ModelIndex<GridColumn> column,
                                                  DataGrid grid,
                                                  @Nullable ColorValue color) {
        return color;
    }

    /**
     * @return the palette entry of the column: {@code column % 10 + 1}
     */
    @Override
    public @Nullable ColorValue getCellForeground(ModelIndex<GridRow> row,
                                                  ModelIndex<GridColumn> column,
                                                  DataGrid grid,
                                                  @Nullable ColorValue color) {
        int index = column.asInteger();
        return index < 0 ? color : PALETTE[index % PALETTE.length];
    }

    @Override
    public @Nullable ColorValue getRowHeaderBackground(ModelIndex<GridRow> row, DataGrid grid, @Nullable ColorValue color) {
        return color;
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
