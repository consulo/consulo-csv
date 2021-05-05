package net.seesharpsoft.intellij.plugins.csv;

import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csv.icon.CsvIconGroup;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdater;
import consulo.ui.image.Image;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvField;

import javax.annotation.Nonnull;

public class CsvIconProvider implements IconDescriptorUpdater {

    public static final Image FILE = CsvIconGroup.csv_icon();

    public static final Image HEADER = CsvIconGroup.csv_header_icon();

    public static final Image FIELD = CsvIconGroup.csv_field_icon();

    @RequiredReadAction
    @Override
    public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement element, int flags) {
        if (element instanceof CsvField) {
            iconDescriptor.setMainIcon(FIELD);
        }
    }
}