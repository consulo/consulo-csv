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

import consulo.ui.ex.grid.csv.ValueRange;
import net.seesharpsoft.intellij.plugins.csv.CsvEscapeCharacter;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;

/**
 * The range of one field: the whole text between its separators, the whitespace around the value and the quotes included. Its value
 * is read like the text editor reads it: trimmed, without the quotes, unescaped.
 *
 * @since 2026-10-04
 */
public final class CsvTableValueRange extends ValueRange {
    private final CsvEscapeCharacter myEscapeCharacter;

    public CsvTableValueRange(int startOffset, int endOffset, CsvEscapeCharacter escapeCharacter) {
        super(startOffset, endOffset);
        myEscapeCharacter = escapeCharacter;
    }

    /**
     * {@link CsvHelper#unquoteCsvValue}: trims, strips the quotes, unescapes.
     */
    @Override
    public CharSequence value(CharSequence s) {
        String text = subSequence(s).toString();
        String trimmed = text.trim();
        if (trimmed.indexOf('"') < 0 && !trimmed.contains(myEscapeCharacter.getCharacter())) {
            // nothing to strip or unescape: the same result without the regular expressions
            return trimmed;
        }
        return CsvHelper.unquoteCsvValue(text, myEscapeCharacter);
    }
}
