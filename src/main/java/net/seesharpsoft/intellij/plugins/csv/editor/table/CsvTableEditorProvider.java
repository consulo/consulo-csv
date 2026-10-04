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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.fileEditor.AsyncFileEditorProvider;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorPolicy;
import consulo.fileEditor.FileEditorState;
import consulo.language.impl.file.SingleRootFileViewProvider;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.VirtualFile;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettings;
import org.jdom.Element;

/**
 * The "Table Editor" of CSV files, next to the text editor. Which of the two comes first, and whether the table is offered at all,
 * follows the editor usage setting.
 *
 * @since 2026-10-04
 */
@ExtensionImpl
public class CsvTableEditorProvider implements AsyncFileEditorProvider, DumbAware {
    public static final String EDITOR_TYPE_ID = "csv-table-editor";

    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override
    public FileEditorPolicy getPolicy() {
        return switch (CsvEditorSettings.getInstance().getEditorPrio()) {
            case TEXT_FIRST, TEXT_ONLY -> FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
            case TABLE_FIRST -> FileEditorPolicy.HIDE_DEFAULT_EDITOR;
        };
    }

    @Override
    public boolean accept(Project project, VirtualFile file) {
        return CsvEditorSettings.getInstance().getEditorPrio() != CsvEditorSettings.EditorPrio.TEXT_ONLY
            && CsvHelper.isCsvFile(project, file)
            && !SingleRootFileViewProvider.isTooLargeForIntelligence(file);
    }

    @Override
    @RequiredUIAccess
    public FileEditor createEditor(Project project, VirtualFile file) {
        return createEditorAsync(project, file).build();
    }

    @Override
    public FileEditorState readState(Element sourceElement, Project project, VirtualFile file) {
        return CsvTableEditorState.create(sourceElement, project, file);
    }

    @Override
    public void writeState(FileEditorState state, Project project, Element targetElement) {
        if (state instanceof CsvTableEditorState tableEditorState) {
            tableEditorState.write(project, targetElement);
        }
    }

    @Override
    public Builder createEditorAsync(Project project, VirtualFile file) {
        return new Builder() {
            @Override
            public FileEditor build() {
                return new CsvTableEditor(project, file);
            }
        };
    }
}
