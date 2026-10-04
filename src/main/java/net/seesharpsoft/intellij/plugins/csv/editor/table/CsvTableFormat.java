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

import consulo.project.Project;
import consulo.ui.ex.grid.csv.CsvFormat;
import consulo.ui.ex.grid.csv.CsvRecordFormat;
import consulo.virtualFileSystem.VirtualFile;
import net.seesharpsoft.intellij.plugins.csv.CsvEscapeCharacter;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.CsvValueSeparator;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettings;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * An immutable snapshot of everything the table needs from the file, the settings and the per-file state of the editor: how the text
 * is split and quoted, and how the table shows it. It is taken on the UI thread and read on the parse thread, which reads nothing else.
 *
 * @since 2026-10-04
 */
public final class CsvTableFormat {
    /**
     * Pixels of the old width settings per character of the grid font.
     */
    public static final int PX_PER_CHAR = 8;
    /**
     * The narrowest column width a pixel setting converts to, in characters.
     */
    public static final int MIN_COLUMN_WIDTH_CHARS = 4;

    private static final String NAME = "CSV";
    private static final String ID = "net.seesharpsoft.intellij.plugins.csv.table";
    private static final String QUOTE = "\"";
    private static final String RECORD_SEPARATOR = "\n";

    private final CsvValueSeparator mySeparator;
    private final CsvEscapeCharacter myEscapeCharacter;
    private final String myCommentIndicator;
    private final boolean myFixedHeaders;
    private final boolean myFileEndLineBreak;
    private final boolean myQuotingEnforced;
    private final boolean myZeroBasedColumnNumbering;
    private final boolean myRainbowColoring;
    private final int myRowLines;
    private final int myDefaultColumnWidth;
    private final int myMaxColumnWidth;

    private final CsvFormat myCsvFormat;

    private CsvTableFormat(CsvValueSeparator separator,
                           CsvEscapeCharacter escapeCharacter,
                           String commentIndicator,
                           boolean fixedHeaders,
                           boolean fileEndLineBreak,
                           boolean quotingEnforced,
                           boolean zeroBasedColumnNumbering,
                           boolean rainbowColoring,
                           int rowLines,
                           int defaultColumnWidth,
                           int maxColumnWidth) {
        mySeparator = separator;
        myEscapeCharacter = escapeCharacter;
        myCommentIndicator = commentIndicator;
        myFixedHeaders = fixedHeaders;
        myFileEndLineBreak = fileEndLineBreak;
        myQuotingEnforced = quotingEnforced;
        myZeroBasedColumnNumbering = zeroBasedColumnNumbering;
        myRainbowColoring = rainbowColoring;
        myRowLines = clampRowLines(rowLines);
        myDefaultColumnWidth = defaultColumnWidth;
        myMaxColumnWidth = maxColumnWidth;
        myCsvFormat = createCsvFormat();
    }

    /**
     * Takes the snapshot. Call on the UI thread: it may read the document text, to auto-detect the separator.
     *
     * @param fixedHeaders the per-file "first row is header" flag
     * @param rowLines     the per-file text lines per row, 0 = auto, clamped to 0..10
     */
    public static CsvTableFormat create(Project project, VirtualFile file, boolean fixedHeaders, int rowLines) {
        CsvEditorSettings settings = CsvEditorSettings.getInstance();
        String commentIndicator = settings.getCommentIndicator();
        return new CsvTableFormat(
            CsvHelper.getValueSeparator(project, file),
            CsvHelper.getEscapeCharacter(project, file),
            commentIndicator == null ? "" : commentIndicator,
            fixedHeaders,
            settings.isFileEndLineBreak(),
            settings.isQuotingEnforced(),
            settings.isZeroBasedColumnNumbering(),
            settings.getValueColoring() == CsvEditorSettings.ValueColoring.RAINBOW,
            rowLines,
            Math.max(MIN_COLUMN_WIDTH_CHARS, pxToChars(settings.getTableDefaultColumnWidth())),
            pxToChars(settings.getTableAutoMaxColumnWidth()));
    }

    /**
     * @return {@code 0} (no width, no cap) for {@code px <= 0}; otherwise the width in characters of the grid font, at least
     * {@link #MIN_COLUMN_WIDTH_CHARS}
     */
    public static int pxToChars(int px) {
        if (px <= 0) {
            return 0;
        }
        return Math.max(MIN_COLUMN_WIDTH_CHARS, Math.round(px / (float) PX_PER_CHAR));
    }

    private static int clampRowLines(int rowLines) {
        return Math.max(CsvEditorSettings.TABLE_EDITOR_ROW_HEIGHT_MIN, Math.min(CsvEditorSettings.TABLE_EDITOR_ROW_HEIGHT_MAX, rowLines));
    }

    /**
     * @return the value separator of the file: its own, the auto-detected or the default one; fixed for tab and pipe separated files
     */
    public CsvValueSeparator getSeparator() {
        return mySeparator;
    }

    public CsvEscapeCharacter getEscapeCharacter() {
        return myEscapeCharacter;
    }

    /**
     * @return the text which starts a comment line, or an empty text when the file has no comments
     */
    public String getCommentIndicator() {
        return myCommentIndicator;
    }

    /**
     * @return whether the first record holds the names of the columns
     */
    public boolean isFixedHeaders() {
        return myFixedHeaders;
    }

    /**
     * @return whether a final line break ends the last record, instead of starting an empty one
     */
    public boolean isFileEndLineBreak() {
        return myFileEndLineBreak;
    }

    /**
     * @return whether every value written is quoted
     */
    public boolean isQuotingEnforced() {
        return myQuotingEnforced;
    }

    /**
     * @return whether the columns the header does not name are numbered from 0, instead of 1
     */
    public boolean isZeroBasedColumnNumbering() {
        return myZeroBasedColumnNumbering;
    }

    /**
     * @return whether every column has a text color of its own
     */
    public boolean isRainbowColoring() {
        return myRainbowColoring;
    }

    /**
     * @return the text lines per row: {@code 0} for auto, otherwise 1..10
     */
    public int getRowLines() {
        return myRowLines;
    }

    /**
     * @return the width of a column without a width of its own, in characters of the grid font, at least
     * {@link #MIN_COLUMN_WIDTH_CHARS}
     */
    public int getDefaultColumnWidth() {
        return myDefaultColumnWidth;
    }

    /**
     * @return the most characters of the grid font a column gets when the widths fit the content, or {@code 0} for no cap
     */
    public int getMaxColumnWidth() {
        return myMaxColumnWidth;
    }

    public CsvTableFormat withFixedHeaders(boolean fixedHeaders) {
        if (fixedHeaders == myFixedHeaders) {
            return this;
        }
        return new CsvTableFormat(mySeparator, myEscapeCharacter, myCommentIndicator, fixedHeaders, myFileEndLineBreak, myQuotingEnforced,
            myZeroBasedColumnNumbering, myRainbowColoring, myRowLines, myDefaultColumnWidth, myMaxColumnWidth);
    }

    public CsvTableFormat withRowLines(int rowLines) {
        if (clampRowLines(rowLines) == myRowLines) {
            return this;
        }
        return new CsvTableFormat(mySeparator, myEscapeCharacter, myCommentIndicator, myFixedHeaders, myFileEndLineBreak, myQuotingEnforced,
            myZeroBasedColumnNumbering, myRainbowColoring, rowLines, myDefaultColumnWidth, myMaxColumnWidth);
    }

    /**
     * Whether both snapshots give the same markup: separator, escape character, comment indicator, fixed headers, file end line break,
     * enforced quoting and zero-based numbering are equal. Row lines, coloring and widths are display-only.
     */
    public boolean parsesLike(CsvTableFormat other) {
        return mySeparator.equals(other.mySeparator)
            && myEscapeCharacter.equals(other.myEscapeCharacter)
            && myCommentIndicator.equals(other.myCommentIndicator)
            && myFixedHeaders == other.myFixedHeaders
            && myFileEndLineBreak == other.myFileEndLineBreak
            && myQuotingEnforced == other.myQuotingEnforced
            && myZeroBasedColumnNumbering == other.myZeroBasedColumnNumbering;
    }

    /**
     * @param index the index of the column, starting from 0
     * @return the name of a column the header does not name: its number, counted from 0 or from 1
     */
    public String columnName(int index) {
        return myZeroBasedColumnNumbering ? String.valueOf(index) : String.valueOf(index + 1);
    }

    /**
     * The platform format of this snapshot. It is deterministic, so equal snapshots give equal formats.
     * <ul>
     * <li>No value reads as a missing one: the text of a missing value is {@code null}.</li>
     * <li>Values are not trimmed by the format; the plugin trims them when it reads them, and quotes them when it writes them.</li>
     * <li>The first column never holds the names of the rows.</li>
     * </ul>
     */
    public CsvFormat toCsvFormat() {
        return myCsvFormat;
    }

    private CsvFormat createCsvFormat() {
        String escapedQuote = myEscapeCharacter.getCharacter() + QUOTE;
        CsvRecordFormat record = new CsvRecordFormat("",
            "",
            null,
            List.of(new CsvRecordFormat.Quotes(QUOTE, QUOTE, escapedQuote, escapedQuote)),
            myQuotingEnforced ? CsvRecordFormat.QuotationPolicy.ALWAYS : CsvRecordFormat.QuotationPolicy.AS_NEEDED,
            mySeparator.getCharacter(),
            RECORD_SEPARATOR,
            false);
        return new CsvFormat(NAME, record, myFixedHeaders ? record : null, ID, false);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CsvTableFormat other)) {
            return false;
        }
        return parsesLike(other)
            && myRainbowColoring == other.myRainbowColoring
            && myRowLines == other.myRowLines
            && myDefaultColumnWidth == other.myDefaultColumnWidth
            && myMaxColumnWidth == other.myMaxColumnWidth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mySeparator,
            myEscapeCharacter,
            myCommentIndicator,
            myFixedHeaders,
            myFileEndLineBreak,
            myQuotingEnforced,
            myZeroBasedColumnNumbering,
            myRainbowColoring,
            myRowLines,
            myDefaultColumnWidth,
            myMaxColumnWidth);
    }

    @Override
    public String toString() {
        return "CsvTableFormat{" +
            "separator=" + mySeparator.getName() +
            ", escapeCharacter=" + myEscapeCharacter.getName() +
            ", commentIndicator='" + myCommentIndicator + '\'' +
            ", fixedHeaders=" + myFixedHeaders +
            ", fileEndLineBreak=" + myFileEndLineBreak +
            ", quotingEnforced=" + myQuotingEnforced +
            ", zeroBasedColumnNumbering=" + myZeroBasedColumnNumbering +
            ", rainbowColoring=" + myRainbowColoring +
            ", rowLines=" + myRowLines +
            ", defaultColumnWidth=" + myDefaultColumnWidth +
            ", maxColumnWidth=" + myMaxColumnWidth +
            '}';
    }
}
