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

import consulo.document.util.TextRange;
import consulo.language.ast.IElementType;
import consulo.language.ast.StandardTokenTypes;
import consulo.language.lexer.Lexer;
import consulo.ui.ex.grid.csv.CsvParserResult;
import consulo.ui.ex.grid.csv.CsvRecord;
import consulo.ui.ex.grid.csv.ValueRange;
import net.seesharpsoft.intellij.plugins.csv.CsvLexerFactory;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Splits a text into records with the lexer of the plugin - the tokens the text editor gets - and checks the token sequence against the
 * grammar of the plugin:
 * <ul>
 * <li>a quoted field is a quote, quoted text, and a closing quote, then only whitespace up to the separator, the line break or the end
 * of the text;</li>
 * <li>a quote inside an unquoted field, escaped text outside quotes, a bad character, and the end of the text inside quotes are
 * errors;</li>
 * <li>a comment token at the start of a record makes the whole line a comment, which is neither a record nor part of one.</li>
 * </ul>
 * On the first error the parse stops: the records before the record which holds the error are kept, the rest of the text is not shown.
 * <p/>
 * Ranges:
 * <ul>
 * <li>a field covers the whole text between its separators, the whitespace around the value included - an edit replaces all of it;</li>
 * <li>a record covers its fields and its line break; the last record of a text which does not end with a line break has none.</li>
 * </ul>
 * A final line break ends the last record when {@link CsvTableFormat#isFileEndLineBreak()} is on; otherwise it starts one more, empty
 * record. A text without any record and without a header - empty, or only comments - gives one empty record at its end.
 * <p/>
 * Runs on the parse thread, without a read action: it reads only the {@link CsvTableFormat} snapshot.
 *
 * @since 2026-10-04
 */
public final class CsvTableParser {
    private final CsvTableFormat myFormat;

    public CsvTableParser(CsvTableFormat format) {
        myFormat = format;
    }

    /**
     * Parse thread, no read action. Never throws for malformed text.
     */
    public Result parse(CharSequence sequence) {
        Walker walker = new Walker(sequence);
        try {
            Lexer lexer = CsvLexerFactory.getInstance()
                .createLexer(myFormat.getSeparator(), myFormat.getEscapeCharacter(), myFormat.getCommentIndicator());
            walker.walk(lexer);
        }
        catch (RuntimeException e) {
            // a lexer which fails: an error where it stopped
            walker.error(walker.myOffset);
        }
        return walker.finish();
    }

    /**
     * The parsed records, and the first error of the text.
     */
    public static final class Result {
        private final CsvParserResult myParserResult;
        private final int myErrorOffset;
        private final int myErrorLine;
        private final boolean myRecordLineMissing;

        private Result(CsvParserResult parserResult, int errorOffset, int errorLine, boolean recordLineMissing) {
            myParserResult = parserResult;
            myErrorOffset = errorOffset;
            myErrorLine = errorLine;
            myRecordLineMissing = recordLineMissing;
        }

        /**
         * @return the records, the header and the number of columns, in the format of {@link CsvTableFormat#toCsvFormat()}
         */
        public CsvParserResult getParserResult() {
            return myParserResult;
        }

        public boolean hasErrors() {
            return myErrorOffset >= 0;
        }

        /**
         * @return the offset of the first error, or {@code -1} when there is none
         */
        public int getErrorOffset() {
            return myErrorOffset;
        }

        /**
         * @return the 1-based line of the first error, or {@code 0} when there is none
         */
        public int getErrorLine() {
            return myErrorLine;
        }

        /**
         * @return whether the only record is the empty one of a text which ends inside a comment line, without a line break: the record
         * has no line of its own yet, so a change which writes it has to end the comment line first
         */
        public boolean isRecordLineMissing() {
            return myRecordLineMissing;
        }
    }

    private enum FieldState {
        /**
         * Only whitespace so far.
         */
        START,
        UNQUOTED,
        IN_QUOTES,
        AFTER_QUOTE
    }

    private final class Walker {
        private final CharSequence mySequence;
        /**
         * The completed records, the header included.
         */
        private final List<CsvRecord> myRecords = new ArrayList<>();

        private List<ValueRange> myValues = new ArrayList<>();
        private FieldState myState = FieldState.START;
        private int myRecordStart;
        private int myFieldStart;
        private int myQuoteStart = -1;
        /**
         * The current record has a token other than whitespace.
         */
        private boolean myRecordStarted;
        /**
         * A comment line is skipped up to its line break.
         */
        private boolean myInComment;
        /**
         * Where the next token starts: the end of the last accepted token.
         */
        private int myOffset;
        private int myErrorOffset = -1;
        /**
         * The empty record after a final line break, when the line break does not end the last record.
         */
        private @Nullable CsvRecord myTrailingRecord;

        private Walker(CharSequence sequence) {
            mySequence = sequence;
        }

        private void walk(Lexer lexer) {
            lexer.start(mySequence);
            while (true) {
                IElementType type = lexer.getTokenType();
                if (type == null) {
                    break;
                }
                int start = lexer.getTokenStart();
                int end = lexer.getTokenEnd();
                if (end <= start) {
                    // a lexer which does not move on
                    error(start);
                    return;
                }
                if (!accept(type, start, end)) {
                    return;
                }
                myOffset = end;
                lexer.advance();
            }
            endOfText();
        }

        /**
         * @return {@code false} when the token is an error: the walk stops
         */
        private boolean accept(IElementType type, int start, int end) {
            if (myInComment) {
                if (type == CsvTypes.CRLF) {
                    myInComment = false;
                    startRecord(end);
                }
                return true;
            }

            if (type == StandardTokenTypes.WHITE_SPACE) {
                return true;
            }
            if (type == CsvTypes.COMMENT) {
                if (myRecordStarted) {
                    return error(start);
                }
                myInComment = true;
                return true;
            }
            if (type == CsvTypes.TEXT || type == CsvTypes.ESCAPE_CHARACTER) {
                return acceptText(start);
            }
            if (type == CsvTypes.QUOTE) {
                return acceptQuote(start);
            }
            if (type == CsvTypes.ESCAPED_TEXT) {
                myRecordStarted = true;
                return myState == FieldState.IN_QUOTES || error(start);
            }
            if (type == CsvTypes.COMMA) {
                if (myState == FieldState.IN_QUOTES) {
                    return error(start);
                }
                myRecordStarted = true;
                endField(start);
                myFieldStart = end;
                return true;
            }
            if (type == CsvTypes.CRLF) {
                if (myState == FieldState.IN_QUOTES) {
                    return error(start);
                }
                endField(start);
                myRecords.add(new CsvRecord(new TextRange(myRecordStart, end), myValues, true));
                startRecord(end);
                return true;
            }
            // a bad character, or a token the grammar does not know
            return error(start);
        }

        private boolean acceptText(int start) {
            myRecordStarted = true;
            switch (myState) {
                case START:
                case UNQUOTED:
                    myState = FieldState.UNQUOTED;
                    return true;
                case IN_QUOTES:
                    return true;
                default:
                    // text after the closing quote
                    return error(start);
            }
        }

        private boolean acceptQuote(int start) {
            myRecordStarted = true;
            switch (myState) {
                case START:
                    myState = FieldState.IN_QUOTES;
                    myQuoteStart = start;
                    return true;
                case IN_QUOTES:
                    myState = FieldState.AFTER_QUOTE;
                    return true;
                default:
                    // a quote inside an unquoted field, or after the closing quote
                    return error(start);
            }
        }

        private void endOfText() {
            int length = mySequence.length();
            if (myInComment) {
                return;
            }
            if (myState == FieldState.IN_QUOTES) {
                // the quote is never closed: the error is where the field starts to be quoted
                error(myQuoteStart);
                return;
            }
            if (myRecordStarted) {
                endField(length);
                myRecords.add(new CsvRecord(new TextRange(myRecordStart, length), myValues, false));
            }
            else if (!myFormat.isFileEndLineBreak() && myRecordStart > 0) {
                // after a final line break, which does not end the last record
                myTrailingRecord = emptyRecord(myRecordStart, length);
            }
        }

        private void startRecord(int offset) {
            myRecordStart = offset;
            myFieldStart = offset;
            myValues = new ArrayList<>();
            myState = FieldState.START;
            myRecordStarted = false;
        }

        private void endField(int endOffset) {
            myValues.add(new CsvTableValueRange(myFieldStart, endOffset, myFormat.getEscapeCharacter()));
            myState = FieldState.START;
        }

        private boolean error(int offset) {
            if (myErrorOffset < 0) {
                myErrorOffset = Math.max(0, Math.min(offset, mySequence.length()));
            }
            return false;
        }

        private CsvRecord emptyRecord(int startOffset, int endOffset) {
            ValueRange value = new CsvTableValueRange(startOffset, endOffset, myFormat.getEscapeCharacter());
            return new CsvRecord(new TextRange(startOffset, endOffset), Collections.singletonList(value), false);
        }

        private Result finish() {
            boolean hasErrors = myErrorOffset >= 0;
            boolean recordLineMissing = false;
            List<CsvRecord> records = new ArrayList<>(myRecords);
            CsvRecord header = null;
            if (myFormat.isFixedHeaders() && !records.isEmpty()) {
                header = records.remove(0);
            }
            if (!hasErrors) {
                CsvRecord trailingRecord = myTrailingRecord;
                if (trailingRecord != null) {
                    records.add(trailingRecord);
                }
                else if (records.isEmpty() && header == null) {
                    // nothing but comments or whitespace: one empty cell at the end of the text
                    int length = mySequence.length();
                    recordLineMissing = myInComment;
                    records.add(myInComment ? emptyRecord(length, length) : emptyRecord(myRecordStart, length));
                }
            }

            int columnsCount = header != null ? header.values.size() : 0;
            for (CsvRecord record : records) {
                columnsCount = Math.max(columnsCount, record.values.size());
            }

            CsvParserResult parserResult = new CsvParserResult(myFormat.toCsvFormat(), mySequence, records, header, columnsCount);
            return new Result(parserResult, myErrorOffset, hasErrors ? lineOf(myErrorOffset) : 0, recordLineMissing);
        }

        private int lineOf(int offset) {
            int line = 1;
            for (int i = 0; i < offset; i++) {
                if (mySequence.charAt(i) == '\n') {
                    line++;
                }
            }
            return line;
        }
    }
}
