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

import consulo.ui.ex.grid.csv.CsvFormatter;
import net.seesharpsoft.intellij.plugins.csv.CsvEscapeCharacter;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.CsvValueSeparator;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Writes values and records with the quoting rules of the plugin ({@link CsvHelper#quoteCsvField}): a value is quoted when it holds the
 * separator, a quote, a line break or the escape character, when it starts or ends with a space, or always with enforced quoting. A
 * value which starts with the comment indicator is quoted too: as the first value of a record it would make the record a comment line.
 * A missing value is written as an empty text - quoted with enforced quoting.
 * <p/>
 * The separators come from {@link CsvTableFormat#toCsvFormat()}: the separator of the file between values, a line break between
 * records.
 *
 * @since 2026-10-04
 */
public final class CsvTableFormatter extends CsvFormatter {
    private static final String QUOTED_EMPTY_VALUE = "\"\"";

    private final CsvValueSeparator mySeparator;
    private final CsvEscapeCharacter myEscapeCharacter;
    private final String myCommentIndicator;
    private final boolean myQuotingEnforced;

    public CsvTableFormatter(CsvTableFormat format) {
        super(format.toCsvFormat());
        mySeparator = format.getSeparator();
        myEscapeCharacter = format.getEscapeCharacter();
        myCommentIndicator = format.getCommentIndicator();
        myQuotingEnforced = format.isQuotingEnforced();
    }

    @Override
    public String formatValue(@Nullable Object value) {
        if (value == null) {
            return myQuotingEnforced ? QUOTED_EMPTY_VALUE : "";
        }
        String text = String.valueOf(value);
        boolean quoted = myQuotingEnforced || startsWithCommentIndicator(text, myCommentIndicator);
        return CsvHelper.quoteCsvField(text, myEscapeCharacter, mySeparator, quoted);
    }

    @Override
    public String formatHeaderValue(@Nullable Object value) {
        return formatValue(value);
    }

    @Override
    public String formatRecord(List<?> values) {
        StringBuilder sb = new StringBuilder();
        String separator = valueSeparator();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(formatValue(values.get(i)));
        }
        return sb.toString();
    }

    @Override
    public String formatHeader(List<?> values) {
        StringBuilder sb = new StringBuilder();
        String separator = headerValueSeparator();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(formatHeaderValue(values.get(i)));
        }
        return sb.toString();
    }

    /**
     * Whether the text, written unquoted as the first value of a record, makes the record a comment line: after the spaces the lexer
     * skips, it starts with the comment indicator.
     *
     * @param commentIndicator the comment indicator, {@code ""} when there are no comments
     */
    static boolean startsWithCommentIndicator(String text, String commentIndicator) {
        if (commentIndicator.isEmpty()) {
            return false;
        }
        int start = 0;
        while (start < text.length() && (text.charAt(start) == ' ' || text.charAt(start) == '\f')) {
            start++;
        }
        return text.startsWith(commentIndicator, start);
    }
}
