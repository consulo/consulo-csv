package net.seesharpsoft.intellij.plugins.csv.settings;

import consulo.annotation.component.ExtensionImpl;
import consulo.configurable.Configurable;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.codeStyle.CodeStyleSettings;
import consulo.language.codeStyle.CustomCodeStyleSettings;
import consulo.language.codeStyle.setting.CodeStyleSettingsProvider;
import consulo.language.codeStyle.setting.LanguageCodeStyleSettingsProvider;
import consulo.language.psi.PsiFile;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractConfigurable;
import consulo.language.codeStyle.ui.setting.CodeStyleAbstractPanel;
import consulo.language.codeStyle.ui.setting.TabbedLanguageCodeStylePanel;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ExtensionImpl
public class CsvCodeStyleSettingsProvider extends CodeStyleSettingsProvider {
    @Override
    public CustomCodeStyleSettings createCustomSettings(CodeStyleSettings settings) {
        return new CsvCodeStyleSettings(settings);
    }

    @Nonnull
    @Override
    public LocalizeValue getConfigurableDisplayName() {
        return LocalizeValue.localizeTODO("CSV/TSV/PSV");
    }

    @NotNull
    @Override
    public Configurable createSettingsPage(CodeStyleSettings settings, CodeStyleSettings originalSettings) {
        return new CodeStyleAbstractConfigurable(settings, originalSettings, getConfigurableDisplayName().get()) {
            @Override
            protected CodeStyleAbstractPanel createPanel(CodeStyleSettings settings) {
                return new CsvCodeStyleMainPanel(getCurrentSettings(), settings);
            }

            @Nullable
            @Override
            public String getHelpTopic() {
                return null;
            }
        };
    }

    private static class CsvCodeStyleMainPanel extends TabbedLanguageCodeStylePanel {
        CsvCodeStyleMainPanel(CodeStyleSettings currentSettings, CodeStyleSettings settings) {
            super(CsvLanguage.INSTANCE, currentSettings, settings);
        }

        @Override
        protected void initTabs(CodeStyleSettings settings) {
            addTab(new CsvCodeStyleOptionTreeWithPreviewPanel(settings));
            addTab(new CsvWrappingPanel(settings));
        }

        protected class CsvWrappingPanel extends MyWrappingAndBracesPanel {
            public CsvWrappingPanel(CodeStyleSettings settings) {
                super(settings);
            }

            @Override
            public String getTabTitle() {
                return "Wrapping";
            }
        }

        protected class CsvCodeStyleOptionTreeWithPreviewPanel extends MyWrappingAndBracesPanel {
            public CsvCodeStyleOptionTreeWithPreviewPanel(CodeStyleSettings settings) {
                super(settings);
            }

            @Override
            protected String getTabTitle() {
                return "Settings";
            }

            @Override
            public LanguageCodeStyleSettingsProvider.SettingsType getSettingsType() {
                return LanguageCodeStyleSettingsProvider.SettingsType.LANGUAGE_SPECIFIC;
            }

            @Override
            protected PsiFile doReformat(Project project, PsiFile psiFile) {
                CodeStyleManager.getInstance(project).reformatText(psiFile, 0, psiFile.getTextLength());
                return psiFile;
            }
        }
    }
}