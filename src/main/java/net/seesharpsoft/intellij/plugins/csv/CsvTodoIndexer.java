package net.seesharpsoft.intellij.plugins.csv;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.plain.psi.stub.todo.PlainTextTodoIndexerBase;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class CsvTodoIndexer extends PlainTextTodoIndexerBase {
    @Nonnull
    @Override
    public FileType getFileType() {
        return CsvFileType.INSTANCE;
    }
}
