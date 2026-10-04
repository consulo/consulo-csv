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
import consulo.grid.editor.CsvDocumentDataHookUp;
import consulo.grid.editor.DocumentDataHookUp;
import consulo.ui.ex.grid.csv.CsvFormatter;
import consulo.ui.ex.grid.csv.CsvRecord;
import consulo.ui.ex.grid.csv.ValueRange;
import consulo.ui.grid.GridColumn;
import consulo.ui.grid.GridRow;
import consulo.ui.grid.ModelIndex;
import consulo.ui.grid.RowMutation;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * The markup of a parsed text: the records of {@link CsvTableParser}, written back with {@link CsvTableFormatter} and named with
 * {@link CsvTableFormat#columnName(int)}. Every change is the minimal range change of the platform markup; the comment lines between
 * the records are never touched.
 * <p/>
 * What differs from the platform markup:
 * <ul>
 * <li>appending keeps a final line break: when the last record ends with one and {@link CsvTableFormat#isFileEndLineBreak()} is on, or
 * comment lines follow it, every new record goes right after it with a line break of its own;</li>
 * <li>comment lines belong to the record which follows them: rows inserted before a record go right after the record before it -
 * the header for the first record, the start of the text when there is none - so they come before the comment lines of the record;
 * inserting a row after another one is inserting it before the next one, and goes there too;</li>
 * <li>without a final line break ({@link CsvTableFormat#isFileEndLineBreak()} off), deleting the rows which end the text deletes the
 * line break before them too, so no empty row is left at the end;</li>
 * <li>deleting or moving columns writes a value which becomes the first value of a record quoted when it starts with the comment
 * indicator - unquoted, it would make the record a comment line;</li>
 * <li>the empty record of a text which ends inside a comment line gets a line of its own before it is first written.</li>
 * </ul>
 *
 * @since 2026-10-04
 */
public final class CsvTableMarkup extends CsvDocumentDataHookUp.CsvMarkup {
    private final boolean myFileEndLineBreak;
    private final String myCommentIndicator;
    private final boolean myRecordLineMissing;
    private final int myErrorOffset;
    private final int myErrorLine;

    public CsvTableMarkup(CsvTableParser.Result result, CsvTableFormat format) {
        super(result.getParserResult(), new CsvTableFormatter(format), format::columnName);
        myFileEndLineBreak = format.isFileEndLineBreak();
        myCommentIndicator = format.getCommentIndicator();
        myRecordLineMissing = result.isRecordLineMissing();
        myErrorOffset = result.getErrorOffset();
        myErrorLine = result.getErrorLine();
    }

    /**
     * @return whether the text has a syntax error: the markup then holds only the records before it
     */
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
     * The one append seam: appending a row, inserting a row after the last one, cloning a row and appending several rows all come
     * here.
     */
    @Override
    protected boolean appendRecords(DocumentDataHookUp.UpdateSession session, List<? extends List<?>> records) {
        List<CsvRecord> dataRecords = getRecords();
        CsvRecord last = dataRecords.isEmpty() ? getHeader() : dataRecords.get(dataRecords.size() - 1);
        // comment lines after the last record: the new records go before them, so they do not run into them
        boolean textAfterLast = last != null && last.range.getEndOffset() < getSequence().length();
        if (last != null && last.hasRecordSeparator && (myFileEndLineBreak || textAfterLast)) {
            CsvFormatter formatter = getFormatter();
            StringBuilder sb = new StringBuilder();
            for (List<?> values : records) {
                sb.append(formatter.formatRecord(values)).append(formatter.recordSeparator());
            }
            session.insert(sb, last.range.getEndOffset());
            return true;
        }
        if (!records.isEmpty()) {
            startRecordLine(session);
        }
        return super.appendRecords(session, records);
    }

    /**
     * The one seam of inserting before a record: inserting a row before or after another one, and inserting several rows, all come
     * here. The new records go in one piece at {@link #insertOffset(int)}, not at the start of the record, which follows its comment
     * lines.
     */
    @Override
    protected boolean insertRows(DocumentDataHookUp.UpdateSession session, @Nullable GridRow before, int amount) {
        int index = before != null ? GridRow.toRealIdx(before) : -1;
        if (amount <= 0 || index < 0 || index >= getRecords().size()) {
            // without a record to insert before, the rows are appended, which starts the line itself
            return super.insertRows(session, before, amount);
        }

        CsvFormatter formatter = getFormatter();
        String record = formatter.formatRecord(Arrays.asList(new @Nullable Object[columns.size()])) + formatter.recordSeparator();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            sb.append(record);
        }
        session.insert(sb, insertOffset(index));
        // the end of the text comes after the offset of the new records
        startRecordLine(session);
        return true;
    }

    /**
     * Comment lines belong to the record which follows them: records inserted before the record {@code index} go right after the end of
     * the record before it - after its line break, before the comment lines of the record {@code index}. The first record has the
     * header before it, or, without a header, the start of the text.
     */
    private int insertOffset(int index) {
        CsvRecord previous = index > 0 ? getRecords().get(index - 1) : getHeader();
        return previous != null ? previous.range.getEndOffset() : 0;
    }

    @Override
    protected boolean update(DocumentDataHookUp.UpdateSession session, List<RowMutation> mutations) {
        if (!mutations.isEmpty()) {
            startRecordLine(session);
        }
        return super.update(session, mutations);
    }

    @Override
    protected boolean insertColumn(DocumentDataHookUp.UpdateSession session, @Nullable GridColumn before, @Nullable String name) {
        startRecordLine(session);
        return super.insertColumn(session, before, name);
    }

    @Override
    protected boolean cloneColumn(DocumentDataHookUp.UpdateSession session, GridColumn column) {
        startRecordLine(session);
        return super.cloneColumn(session, column);
    }

    /**
     * Without a final line break, the rows which end the text go with the line break before them: otherwise the text would end with a
     * line break again, which starts one more, empty row - and the empty row itself could not be deleted at all.
     */
    @Override
    protected boolean deleteRows(DocumentDataHookUp.UpdateSession session, List<GridRow> sortedRows) {
        List<CsvRecord> records = getRecords();
        int lastIndex = records.size() - 1;
        if (myFileEndLineBreak || lastIndex < 0 || records.get(lastIndex).hasRecordSeparator) {
            return super.deleteRows(session, sortedRows);
        }

        boolean[] deleted = new boolean[records.size()];
        for (GridRow row : sortedRows) {
            int index = GridRow.toRealIdx(row);
            if (index >= 0 && index <= lastIndex) {
                deleted[index] = true;
            }
        }
        if (!deleted[lastIndex]) {
            return super.deleteRows(session, sortedRows);
        }

        // the deleted records which end the text, one right after the other
        int head = lastIndex;
        while (head > 0 && deleted[head - 1]
            && records.get(head - 1).range.getEndOffset() == records.get(head).range.getStartOffset()) {
            head--;
        }
        int headStart = records.get(head).range.getStartOffset();
        boolean lineBreakBefore = headStart > 0 && getSequence().charAt(headStart - 1) == '\n';

        for (int i = 0; i <= lastIndex; i++) {
            if (!deleted[i]) {
                continue;
            }
            TextRange range = records.get(i).range;
            if (i == head && lineBreakBefore) {
                session.delete(new TextRange(headStart - 1, range.getEndOffset()));
            }
            else {
                session.delete(range);
            }
        }
        return true;
    }

    /**
     * The values go by ranges, as in the platform markup. When the first column goes, the value which becomes the first one of a
     * record is written quoted if it starts with the comment indicator.
     */
    @Override
    protected boolean deleteColumnRanges(DocumentDataHookUp.UpdateSession session, List<GridColumn> sortedColumns) {
        if (myCommentIndicator.isEmpty()
            || getFormatter().requiresRowNumbers()
            || sortedColumns.isEmpty()
            || sortedColumns.get(0).getColumnNumber() != 0) {
            return super.deleteColumnRanges(session, sortedColumns);
        }

        boolean[] deletedColumns = new boolean[columns.size()];
        int[] indices = new int[sortedColumns.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = sortedColumns.get(i).getColumnNumber();
            if (indices[i] >= 0 && indices[i] < deletedColumns.length) {
                deletedColumns[indices[i]] = true;
            }
        }
        boolean columnLeft = false;
        for (boolean deletedColumn : deletedColumns) {
            columnLeft |= !deletedColumn;
        }
        if (!columnLeft) {
            // every column goes: the records themselves are deleted
            return false;
        }

        CsvRecord header = getHeader();
        if (header != null) {
            deleteValues(session, header.values, indices, true);
        }
        for (CsvRecord record : getRecords()) {
            deleteValues(session, record.values, indices, false);
        }
        return true;
    }

    /**
     * Deletes the values of a record, each run of adjacent values with one separator next to it, as the platform markup does.
     */
    private void deleteValues(DocumentDataHookUp.UpdateSession session, List<ValueRange> values, int[] sortedIndices, boolean header) {
        int count = values.size();
        int i = 0;
        while (i < sortedIndices.length && sortedIndices[i] < count) {
            int first = sortedIndices[i];
            int last = first;
            while (i + 1 < sortedIndices.length && sortedIndices[i + 1] == last + 1 && sortedIndices[i + 1] < count) {
                i++;
                last++;
            }
            i++;

            if (last < count - 1) {
                ValueRange next = values.get(last + 1);
                if (first == 0 && startsWithCommentIndicator(next)) {
                    // the value after the run becomes the first one: quoted, so the record does not turn into a comment line
                    session.replace(new TextRange(values.get(0).getStartOffset(), next.getEndOffset()), quoted(next, header));
                }
                else {
                    // the run and the separator after it
                    session.delete(new TextRange(values.get(first).getStartOffset(), next.getStartOffset()));
                }
            }
            else if (first > 0) {
                // the run ends the record: the separator before it
                session.delete(new TextRange(values.get(first - 1).getEndOffset(), values.get(last).getEndOffset()));
            }
            else {
                // every value of a short record
                session.delete(new TextRange(values.get(0).getStartOffset(), values.get(count - 1).getEndOffset()));
            }
        }
    }

    /**
     * The values move by ranges, as in the platform markup. When a value moves to the first place of a record, it is written quoted if
     * it starts with the comment indicator.
     */
    @Override
    protected boolean moveColumn(DocumentDataHookUp.UpdateSession session, GridColumn fromColumn, ModelIndex<GridColumn> toColumn) {
        int shift = getFormatter().requiresRowNumbers() ? 1 : 0;
        int from = fromColumn.getColumnNumber() + shift;
        int to = toColumn.asInteger() + shift;
        if (myCommentIndicator.isEmpty() || from == to || Math.min(from, to) != 0) {
            return super.moveColumn(session, fromColumn, toColumn);
        }

        CsvRecord header = getHeader();
        if (header != null) {
            moveValues(session, header.values, from, to, true);
        }
        for (CsvRecord record : getRecords()) {
            moveValues(session, record.values, from, to, false);
        }
        return true;
    }

    private void moveValues(DocumentDataHookUp.UpdateSession session, List<ValueRange> values, int from, int to, boolean header) {
        boolean fromBeforeTo = from < to;
        int left = Math.min(from, to);
        int right = Math.max(from, to);
        if (values.size() <= right) {
            return;
        }

        CharSequence sequence = getSequence();
        StringBuilder text = new StringBuilder();
        for (int i = left; i <= right; i++) {
            int oldPos = i == to ? from : fromBeforeTo ? i + 1 : i - 1;
            ValueRange value = values.get(oldPos);
            if (i == 0 && startsWithCommentIndicator(value)) {
                text.append(quoted(value, header));
            }
            else {
                text.append(value.subSequence(sequence));
            }
            if (i < right) {
                text.append(sequence, values.get(i).getEndOffset(), values.get(i + 1).getStartOffset());
            }
        }
        session.replace(new TextRange(values.get(left).getStartOffset(), values.get(right).getEndOffset()), text);
    }

    private boolean startsWithCommentIndicator(ValueRange value) {
        return CsvTableFormatter.startsWithCommentIndicator(value.subSequence(getSequence()).toString(), myCommentIndicator);
    }

    /**
     * @return the value written anew, which quotes a value that starts with the comment indicator
     */
    private String quoted(ValueRange value, boolean header) {
        String text = value.value(getSequence()).toString();
        CsvFormatter formatter = getFormatter();
        return header ? formatter.formatHeaderValue(text) : formatter.formatValue(text);
    }

    /**
     * The empty record of a text which ends inside a comment line has no line of its own: a change which writes it ends the comment
     * line first. The line break goes at the end of the text, before every other change there - a change of such a markup writes either
     * there, or, for rows inserted before the record, at the start of the text, which comes first in the session.
     */
    private void startRecordLine(DocumentDataHookUp.UpdateSession session) {
        if (myRecordLineMissing) {
            session.insert(getFormatter().recordSeparator(), getSequence().length());
        }
    }
}
