package net.seesharpsoft.intellij.plugins.csv;

import consulo.project.Project;
import consulo.util.dataholder.Key;
import consulo.util.io.PathUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFilePathUtil;

import java.io.File;
import java.util.regex.Pattern;

public final class CsvStorageHelper {
    public static final String CSV_STATE_STORAGE_FILE = "csv-plugin.xml";

    public static final Key<String> RELATIVE_FILE_URL = Key.create("CSV_PLUGIN_RELATIVE_URL");

    public static String getRelativeFilePath(Project project, VirtualFile virtualFile) {
        if (project == null || virtualFile == null) {
            return null;
        }
        String filePath = virtualFile.getUserData(RELATIVE_FILE_URL);
        if (filePath == null && project.getBasePath() != null) {
            String projectDir = VirtualFilePathUtil.getLocalPath(project.getBasePath());
            filePath = VirtualFilePathUtil.getLocalPath(virtualFile)
                    .replaceFirst("^" + Pattern.quote(projectDir), "");
            virtualFile.putUserData(RELATIVE_FILE_URL, filePath);
        }
        return filePath;
    }

    public static boolean csvFileExists(Project project, String fileName) {
        if (fileName == null) {
            return false;
        }
        String filePath = VirtualFilePathUtil.getLocalPath(fileName);
        if (filePath == null ||
                !CsvHelper.isCsvFile(PathUtil.getFileExtension(filePath))) {
            return false;
        }
        if (project != null && new File(VirtualFilePathUtil.getLocalPath(project.getBasePath()) + filePath).exists()) {
            return true;
        }
        return new File(filePath).exists();
    }

    private CsvStorageHelper() {
        // static
    }
}
