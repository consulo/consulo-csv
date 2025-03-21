package net.seesharpsoft.intellij.plugins.csv;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.icon.IconDescriptorUpdater;
import consulo.language.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csv.icon.CsvIconGroup;
import consulo.language.icon.IconDescriptor;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvField;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class CsvIconProvider implements IconDescriptorUpdater {

    public static final Image FILE = CsvIconGroup.csv_icon();

    public static final Image HEADER = CsvIconGroup.csv_header_icon();

    @RequiredReadAction
    @Override
    public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement element, int flags) {
        if (element instanceof CsvField) {
            iconDescriptor.setMainIcon(PlatformIconGroup.nodesField());
        }
    }
}