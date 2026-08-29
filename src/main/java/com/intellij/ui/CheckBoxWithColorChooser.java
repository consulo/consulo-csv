// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ui;

import consulo.application.util.SystemInfo;
import consulo.localize.LocalizeValue;
import consulo.ui.ColorBox;
import consulo.ui.ColorPickerBuilder;
import consulo.ui.color.ColorValue;
import consulo.ui.ex.awt.ClickListener;
import consulo.ui.ex.awt.UIUtil;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import consulo.ui.style.StandardColors;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author Konstantin Bulenkov
 */
public class CheckBoxWithColorChooser extends JPanel {
    private ColorBox myColorSelectedBox;
    private final JCheckBox myCheckbox;

    public CheckBoxWithColorChooser(String text, boolean selected, ColorValue color) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        myCheckbox = new JCheckBox(text, selected);
        add(myCheckbox);
        myColorSelectedBox = ColorBox.create(color);
        add(TargetAWT.to(myColorSelectedBox));
    }

    public CheckBoxWithColorChooser(String text, boolean selected) {
        this(text, selected, StandardColors.WHITE);
    }

    public CheckBoxWithColorChooser(String text) {
        this(text, false);
    }

    public void setMnemonic(char c) {
        myCheckbox.setMnemonic(c);
    }

    public ColorValue getColor() {
        return myColorSelectedBox.getBackgroundColor();
    }

    public void setColor(ColorValue color) {
        myColorSelectedBox.setValue(color);
    }

    public void setSelected(boolean selected) {
        myCheckbox.setSelected(selected);
    }

    public boolean isSelected() {
        return myCheckbox.isSelected();
    }
}
