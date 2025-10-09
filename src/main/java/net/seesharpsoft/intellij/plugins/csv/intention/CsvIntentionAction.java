package net.seesharpsoft.intellij.plugins.csv.intention;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;

public abstract class CsvIntentionAction extends PsiElementBaseIntentionAction implements IntentionAction {
    protected CsvIntentionAction(@Nonnull LocalizeValue text) {
        setText(text);
    }

    @Override
    @RequiredReadAction
    public boolean isAvailable(@Nonnull Project project, Editor editor, @Nullable PsiElement element) {
        return element != null && element.getContainingFile().getLanguage().isKindOf(CsvLanguage.INSTANCE);
    }
}
