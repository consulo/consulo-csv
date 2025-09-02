package net.seesharpsoft.intellij.plugins.tsv;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.todo.PlainTextTodoIndexerBase;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 16/05/2023
 */
@ExtensionImpl
public class TsvTodoIndexer extends PlainTextTodoIndexerBase {
    @Nonnull
    @Override
    public FileType getFileType() {
        return TsvFileType.INSTANCE;
    }
}
