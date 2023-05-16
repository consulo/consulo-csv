package net.seesharpsoft.intellij.plugins.csv.intention;

import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class CsvIntentionAction extends PsiElementBaseIntentionAction implements IntentionAction {

    protected CsvIntentionAction(String text) {
        setText(text);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {
        return element != null && element.getContainingFile().getLanguage().isKindOf(CsvLanguage.INSTANCE);
    }
}
