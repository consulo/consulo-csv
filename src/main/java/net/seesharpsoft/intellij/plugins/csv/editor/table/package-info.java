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

/**
 * The table editor of comma separated values, on the data grid of the platform. The text of the file is parsed with the lexer of the
 * plugin into records ({@link net.seesharpsoft.intellij.plugins.csv.editor.table.CsvTableParser}), shown through a document data
 * source ({@link net.seesharpsoft.intellij.plugins.csv.editor.table.CsvTableDocumentDataHookUp}), and every edit of the grid is
 * written back as a minimal change of the document, with the quoting rules of the plugin.
 * <p/>
 * The settings and the per-file state the table depends on are taken as one immutable snapshot,
 * {@link net.seesharpsoft.intellij.plugins.csv.editor.table.CsvTableFormat}, on the UI thread; the parse thread reads nothing else.
 *
 * @since 2026-10-04
 */
@NullMarked
package net.seesharpsoft.intellij.plugins.csv.editor.table;

import org.jspecify.annotations.NullMarked;
