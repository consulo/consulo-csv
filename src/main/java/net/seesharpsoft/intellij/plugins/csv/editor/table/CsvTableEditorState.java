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

import consulo.fileEditor.FileEditorState;
import consulo.fileEditor.FileEditorStateLevel;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettings;
import org.jdom.Element;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The per-file state of the table editor: column widths in characters of the grid font, the header row flag, the text lines per row
 * and whether the columns fit their content on open. A value which was never set falls back to the global CSV editor settings.
 *
 * @since 2026-10-04
 */
public class CsvTableEditorState implements FileEditorState {
    private static final String FIXED_HEADERS_ATTRIBUTE = "fixedHeaders";
    private static final String AUTO_COLUMN_WIDTH_ON_OPEN_ATTRIBUTE = "autoColumnWidthOnOpen";
    private static final String ROW_LINES_ATTRIBUTE = "rowLines";
    private static final String COLUMN_ELEMENT = "column";
    private static final String COLUMN_INDEX_ATTRIBUTE = "index";
    private static final String COLUMN_CHARS_ATTRIBUTE = "chars";
    /**
     * Written only by the former editor, always: it tells its states apart.
     */
    private static final String LEGACY_SHOW_INFO_PANEL_ATTRIBUTE = "showInfoPanel";

    /**
     * Stored widths of columns at this index or beyond are ignored on read.
     */
    private static final int MAX_STORED_COLUMNS = 10_000;

    private int[] myColumnWidths = new int[0];
    private @Nullable Boolean myAutoColumnWidthOnOpen;
    private @Nullable Boolean myFixedHeaders;
    private @Nullable Integer myRowLines;

    public CsvTableEditorState() {
    }

    /**
     * @return the width of each column in characters, by column index; {@code 0} means no stored width
     */
    public int[] getColumnWidths() {
        return myColumnWidths;
    }

    public void setColumnWidths(int[] widths) {
        myColumnWidths = widths.clone();
    }

    public boolean getFixedHeaders() {
        Boolean fixedHeaders = myFixedHeaders;
        return fixedHeaders == null ? CsvEditorSettings.getInstance().isHeaderRowFixed() : fixedHeaders;
    }

    public void setFixedHeaders(boolean fixedHeaders) {
        myFixedHeaders = fixedHeaders;
    }

    public boolean getAutoColumnWidthOnOpen() {
        Boolean autoColumnWidthOnOpen = myAutoColumnWidthOnOpen;
        return autoColumnWidthOnOpen == null ? CsvEditorSettings.getInstance().isTableAutoColumnWidthOnOpen() : autoColumnWidthOnOpen;
    }

    /**
     * @param autoColumnWidthOnOpen the per-file value, or {@code null} to follow the global setting
     */
    public void setAutoColumnWidthOnOpen(@Nullable Boolean autoColumnWidthOnOpen) {
        myAutoColumnWidthOnOpen = autoColumnWidthOnOpen;
    }

    /**
     * @return the text lines per row, {@code 0} for auto
     */
    public int getRowLines() {
        Integer rowLines = myRowLines;
        return clampRowLines(rowLines == null ? CsvEditorSettings.getInstance().getTableEditorRowHeight() : rowLines);
    }

    public void setRowLines(int rowLines) {
        myRowLines = clampRowLines(rowLines);
    }

    @Override
    public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
        return false;
    }

    /**
     * Writes the values which were set for this file; a value which follows the global setting is left out.
     */
    public void write(Project project, Element element) {
        Boolean fixedHeaders = myFixedHeaders;
        if (fixedHeaders != null) {
            element.setAttribute(FIXED_HEADERS_ATTRIBUTE, String.valueOf(fixedHeaders));
        }
        Boolean autoColumnWidthOnOpen = myAutoColumnWidthOnOpen;
        if (autoColumnWidthOnOpen != null) {
            element.setAttribute(AUTO_COLUMN_WIDTH_ON_OPEN_ATTRIBUTE, String.valueOf(autoColumnWidthOnOpen));
        }
        Integer rowLines = myRowLines;
        if (rowLines != null) {
            element.setAttribute(ROW_LINES_ATTRIBUTE, String.valueOf(rowLines));
        }
        for (int i = 0; i < myColumnWidths.length; i++) {
            int chars = myColumnWidths[i];
            if (chars > 0) {
                Element columnElement = new Element(COLUMN_ELEMENT);
                columnElement.setAttribute(COLUMN_INDEX_ATTRIBUTE, String.valueOf(i));
                columnElement.setAttribute(COLUMN_CHARS_ATTRIBUTE, String.valueOf(chars));
                element.addContent(columnElement);
            }
        }
    }

    /**
     * Reads a state written by {@link #write}. Attributes of older versions - the info panel flag and widths in pixels - are ignored,
     * and a missing attribute leaves the value unset.
     * <p/>
     * The row lines of a state of the former editor are ignored as well: it always wrote them - the per-file choice it offered, or
     * only the global default of the time - and the editor has no per-file choice of them any more, so the global setting applies.
     */
    public static CsvTableEditorState create(Element element, Project project, VirtualFile file) {
        CsvTableEditorState state = new CsvTableEditorState();

        @Nullable String fixedHeaders = element.getAttributeValue(FIXED_HEADERS_ATTRIBUTE);
        if (fixedHeaders != null) {
            state.setFixedHeaders(Boolean.parseBoolean(fixedHeaders));
        }

        @Nullable String autoColumnWidthOnOpen = element.getAttributeValue(AUTO_COLUMN_WIDTH_ON_OPEN_ATTRIBUTE);
        if (autoColumnWidthOnOpen != null) {
            state.setAutoColumnWidthOnOpen(Boolean.parseBoolean(autoColumnWidthOnOpen));
        }

        if (element.getAttributeValue(LEGACY_SHOW_INFO_PANEL_ATTRIBUTE) == null) {
            int rowLines = StringUtil.parseInt(element.getAttributeValue(ROW_LINES_ATTRIBUTE), -1);
            if (rowLines >= 0) {
                state.setRowLines(rowLines);
            }
        }

        state.myColumnWidths = readColumnWidths(element.getChildren(COLUMN_ELEMENT));
        return state;
    }

    private static int[] readColumnWidths(List<Element> columnElements) {
        int count = 0;
        for (Element columnElement : columnElements) {
            int index = StringUtil.parseInt(columnElement.getAttributeValue(COLUMN_INDEX_ATTRIBUTE), -1);
            int chars = StringUtil.parseInt(columnElement.getAttributeValue(COLUMN_CHARS_ATTRIBUTE), 0);
            if (index >= 0 && index < MAX_STORED_COLUMNS && chars > 0) {
                count = Math.max(count, index + 1);
            }
        }

        int[] widths = new int[count];
        for (Element columnElement : columnElements) {
            int index = StringUtil.parseInt(columnElement.getAttributeValue(COLUMN_INDEX_ATTRIBUTE), -1);
            int chars = StringUtil.parseInt(columnElement.getAttributeValue(COLUMN_CHARS_ATTRIBUTE), 0);
            if (index >= 0 && index < count && chars > 0) {
                widths[index] = chars;
            }
        }
        return widths;
    }

    private static int clampRowLines(int rowLines) {
        return Math.max(CsvEditorSettings.TABLE_EDITOR_ROW_HEIGHT_MIN, Math.min(CsvEditorSettings.TABLE_EDITOR_ROW_HEIGHT_MAX, rowLines));
    }
}
