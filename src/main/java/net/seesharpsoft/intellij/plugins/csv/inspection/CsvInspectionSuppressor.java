package net.seesharpsoft.intellij.plugins.csv.inspection;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.inspection.InspectionSuppressor;
import consulo.language.editor.inspection.SuppressQuickFix;
import consulo.language.psi.PsiElement;
import net.seesharpsoft.intellij.plugins.csv.CsvLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

@ExtensionImpl
public class CsvInspectionSuppressor implements InspectionSuppressor {

    private static final Set<String> GENERAL_SUPPRESSED_INSPECTIONS;

    static {
        GENERAL_SUPPRESSED_INSPECTIONS = new HashSet<>();
        // todo GENERAL_SUPPRESSED_INSPECTIONS.add(InspectionProfileEntry.getShortName(ProblematicWhitespaceInspection.class.getSimpleName()));
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement psiElement, @NotNull String s) {
        return GENERAL_SUPPRESSED_INSPECTIONS.contains(s);
    }

    @NotNull
    @Override
    public SuppressQuickFix[] getSuppressActions(@Nullable PsiElement psiElement, @NotNull String s) {
        return new SuppressQuickFix[0];
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CsvLanguage.INSTANCE;
    }
}
