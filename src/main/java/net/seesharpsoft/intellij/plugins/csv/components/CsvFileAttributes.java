package net.seesharpsoft.intellij.plugins.csv.components;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.document.Document;
import consulo.document.FileDocumentManager;
import consulo.ide.ServiceManager;
import consulo.language.Language;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiFile;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.util.xml.serializer.XmlSerializerUtil;
import consulo.util.xml.serializer.annotation.OptionTag;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFilePathUtil;
import jakarta.inject.Singleton;
import net.seesharpsoft.commons.collection.Pair;
import net.seesharpsoft.intellij.plugins.csv.*;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@State(
        name = "CsvFileAttributes",
        storages = {@Storage(CsvStorageHelper.CSV_STATE_STORAGE_FILE)}
)
@SuppressWarnings("all")
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public class CsvFileAttributes implements PersistentStateComponent<CsvFileAttributes> {
    private final static Logger LOG = Logger.getInstance(CsvFileAttributes.class);

    public Map<String, Attribute> attributeMap = new HashMap<>();

    static class Attribute {
        @OptionTag(converter = CsvValueSeparator.CsvValueSeparatorConverter.class)
        public CsvValueSeparator separator;
        @OptionTag(converter = CsvEscapeCharacter.CsvEscapeCharacterConverter.class)
        public CsvEscapeCharacter escapeCharacter;
    }

    public static CsvFileAttributes getInstance(Project project) {
        CsvFileAttributes csvFileAttributes = project != null ? ServiceManager.getService(project, CsvFileAttributes.class) : null;
        return csvFileAttributes == null ? new CsvFileAttributes() : csvFileAttributes;
    }

    @Nullable
    @Override
    public CsvFileAttributes getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull CsvFileAttributes state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void cleanupAttributeMap(@NotNull Project project) {
        List<String> faultyFiles = new ArrayList<>();
        final String projectBasePath = VirtualFilePathUtil.getLocalPath(project.getBasePath());
        attributeMap.forEach((fileName, attribute) -> {
            if (!CsvStorageHelper.csvFileExists(project, fileName)) {
                LOG.debug(fileName + " not found or not CSV file");
                faultyFiles.add(fileName);
            }
        });
        faultyFiles.forEach(attributeMap::remove);
    }

    public void reset() {
        attributeMap.clear();
    }

    protected String generateMapKey(@NotNull PsiFile psiFile) {
        return generateMapKey(psiFile.getProject(), psiFile.getOriginalFile().getVirtualFile());
    }

    protected String generateMapKey(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return CsvStorageHelper.getRelativeFilePath(project, virtualFile);
    }

    @Nullable
    private Attribute getFileAttribute(@NotNull Project project, @NotNull VirtualFile virtualFile, boolean createIfMissing) {
        String key = generateMapKey(project, virtualFile);
        if (key == null) {
            return null;
        }
        Attribute attribute = key != null ? attributeMap.get(key) : null;
        if (attribute == null && createIfMissing) {
            attribute = new Attribute();
            if (!CsvHelper.isCsvFile(project, virtualFile)) {
                LOG.error("CSV file attribute requested for non CSV file: " + virtualFile.toString());
            }
            else {
                attributeMap.put(key, attribute);
            }
        }
        return attribute;
    }

    @Nullable
    private Attribute getFileAttribute(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        return getFileAttribute(project, virtualFile, false);
    }

    public boolean canChangeValueSeparator(@NotNull PsiFile psiFile) {
        Language language = psiFile.getLanguage();
        return language.isKindOf(CsvLanguage.INSTANCE) && !(language instanceof CsvSeparatorHolder);
    }

    private void setFileSeparator(@NotNull Project project, @NotNull VirtualFile virtualFile, @NotNull CsvValueSeparator separator) {
        Attribute attribute = getFileAttribute(project, virtualFile, true);
        if (attribute != null) {
            attribute.separator = separator;
        }
    }

    public void setFileSeparator(@NotNull PsiFile psiFile, @NotNull CsvValueSeparator separator) {
        if (!canChangeValueSeparator(psiFile)) {
            return;
        }
        setFileSeparator(psiFile.getProject(), psiFile.getOriginalFile().getVirtualFile(), separator);
    }

    public void resetValueSeparator(@NotNull PsiFile psiFile) {
        if (!canChangeValueSeparator(psiFile)) {
            return;
        }
        Attribute attribute = getFileAttribute(psiFile.getProject(), psiFile.getOriginalFile().getVirtualFile());
        if (attribute != null) {
            attribute.separator = null;
        }
    }

    @NotNull
    private CsvValueSeparator autoDetectOrGetDefaultValueSeparator(Project project, VirtualFile virtualFile) {
        return CsvEditorSettings.getInstance().isAutoDetectValueSeparator() ?
                autoDetectSeparator(project, virtualFile) :
                CsvEditorSettings.getInstance().getDefaultValueSeparator();
    }

    @NotNull
    private CsvValueSeparator autoDetectSeparator(Project project, VirtualFile virtualFile) {
        final Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        final String text = document == null ? "" : document.getText();
        final List<CsvValueSeparator> applicableValueSeparators = new ArrayList(Arrays.asList(CsvValueSeparator.values()));
        final CsvValueSeparator defaultValueSeparator = CsvEditorSettings.getInstance().getDefaultValueSeparator();
        if (defaultValueSeparator.isCustom()) {
            applicableValueSeparators.add(defaultValueSeparator);
        }
        Pair<CsvValueSeparator, Integer> separatorWithCount =
                applicableValueSeparators.parallelStream()
                        // count
                        .map(separator -> {
                            String character = separator.getCharacter();
                            return Pair.of(separator, StringUtil.countChars(text, character.charAt(0)));
                        })
                        // ignore non-matched separators
                        .filter(p -> p.getSecond() > 0)
                        // get the one with most hits
                        .max((p1, p2) -> p1.getSecond() - p2.getSecond())
                        // failsafe (e.g. empty document)
                        .orElse(null);

        CsvValueSeparator valueSeparator = separatorWithCount != null ?
                separatorWithCount.getFirst() :
                defaultValueSeparator;

        setFileSeparator(project, virtualFile, valueSeparator);
        return valueSeparator;
    }

    @NotNull
    public CsvValueSeparator getValueSeparator(Project project, VirtualFile virtualFile) {
        if (!CsvHelper.isCsvFile(project, virtualFile)) {
            return CsvEditorSettings.getInstance().getDefaultValueSeparator();
        }
        Language language = ((LanguageFileType) virtualFile.getFileType()).getLanguage();
        if (language instanceof CsvSeparatorHolder) {
            return ((CsvSeparatorHolder) language).getSeparator();
        }
        Attribute attribute = getFileAttribute(project, virtualFile);
        return attribute == null || attribute.separator == null || attribute.separator.getCharacter().isEmpty() ?
                autoDetectOrGetDefaultValueSeparator(project, virtualFile) :
                attribute.separator;
    }

    public boolean hasValueSeparatorAttribute(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        Attribute attribute = getFileAttribute(project, virtualFile);
        return attribute != null && attribute.separator != null;
    }

    public void setEscapeCharacter(@NotNull PsiFile psiFile, @NotNull CsvEscapeCharacter escapeCharacter) {
        Attribute attribute = getFileAttribute(psiFile.getProject(), psiFile.getOriginalFile().getVirtualFile(), true);
        if (attribute != null) {
            attribute.escapeCharacter = escapeCharacter;
        }
    }

    public void resetEscapeSeparator(@NotNull PsiFile psiFile) {
        Attribute attribute = getFileAttribute(psiFile.getProject(), psiFile.getOriginalFile().getVirtualFile());
        if (attribute != null) {
            attribute.escapeCharacter = null;
        }
    }

    @NotNull
    public CsvEscapeCharacter getEscapeCharacter(Project project, VirtualFile virtualFile) {
        if (!CsvHelper.isCsvFile(project, virtualFile)) {
            return CsvEditorSettings.getInstance().getDefaultEscapeCharacter();
        }
        Attribute attribute = getFileAttribute(project, virtualFile);
        return attribute == null || attribute.escapeCharacter == null ?
                CsvEditorSettings.getInstance().getDefaultEscapeCharacter() :
                attribute.escapeCharacter;
    }

    public boolean hasEscapeCharacterAttribute(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        Attribute attribute = getFileAttribute(project, virtualFile);
        return attribute != null && attribute.escapeCharacter != null;
    }
}
