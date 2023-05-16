package net.seesharpsoft.intellij.plugins.csv.editor;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileEditor.*;
import consulo.language.impl.file.SingleRootFileViewProvider;
import consulo.virtualFileSystem.VirtualFile;
import consulo.application.dumb.DumbAware;
import consulo.codeEditor.EditorSettings;
import consulo.fileEditor.text.TextEditorProvider;
import consulo.project.Project;
import net.seesharpsoft.intellij.plugins.csv.CsvHelper;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

@ExtensionImpl
public class CsvFileEditorProvider implements AsyncFileEditorProvider, DumbAware {

    public static final String EDITOR_TYPE_ID = "csv-text-editor";

    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override
    public FileEditorPolicy getPolicy() {
        switch (CsvEditorSettings.getInstance().getEditorPrio()) {
            case TEXT_FIRST:
            case TEXT_ONLY:
                return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
            case TABLE_FIRST:
                return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
            default:
                throw new IllegalArgumentException("unhandled EditorPrio: " + CsvEditorSettings.getInstance().getEditorPrio());
        }
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return CsvHelper.isCsvFile(project, file) && !SingleRootFileViewProvider.isTooLargeForContentLoading(file);
    }

    protected void applySettings(EditorSettings editorSettings, CsvEditorSettings csvEditorSettings) {
        if (editorSettings == null || csvEditorSettings == null) {
            return;
        }
        editorSettings.setCaretRowShown(csvEditorSettings.isCaretRowShown());
        editorSettings.setUseSoftWraps(csvEditorSettings.isUseSoftWraps());
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return createEditorAsync(project, virtualFile).build();
    }

    @Override
    public FileEditorState readState(@NotNull Element sourceElement, @NotNull Project project, @NotNull VirtualFile file) {
        return TextEditorProvider.getInstance().readState(sourceElement, project, file);
    }

    @Override
    public void writeState(@NotNull FileEditorState state, @NotNull Project project, @NotNull Element targetElement) {
        TextEditorProvider.getInstance().writeState(state, project, targetElement);
    }

    @Override
    public void disposeEditor(@NotNull FileEditor editor) {
        TextEditorProvider.getInstance().disposeEditor(editor);
    }

    @NotNull
    @Override
    public Builder createEditorAsync(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return new Builder() {
            @Override
            public FileEditor build() {
                TextEditor textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, virtualFile);
                applySettings(textEditor.getEditor().getSettings(), CsvEditorSettings.getInstance());
                return textEditor;
            }
        };
    }
}
