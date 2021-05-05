package net.seesharpsoft.intellij.plugins.csv.editor.table.swing;

import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.impl.FontFallbackIterator;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.UIUtil;
import consulo.awt.TargetAWT;
import consulo.util.dataholder.UserDataHolder;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvColorSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.geom.Rectangle2D;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class MultiLineCellRenderer extends JBScrollPane implements TableCellRenderer, TableCellEditor {

    private Set<CellEditorListener> cellEditorListenerSet = new CopyOnWriteArraySet<>();
    private final UserDataHolder myUserDataHolder;

    private JTextArea myTextArea;

    public MultiLineCellRenderer(CsvTableEditorKeyListener keyListener, UserDataHolder userDataHolderParam) {
        this.myUserDataHolder = userDataHolderParam;
        myTextArea = new JTextArea();
        myTextArea.setLineWrap(true);
        myTextArea.setWrapStyleWord(true);
        myTextArea.setOpaque(true);
        myTextArea.setRequestFocusEnabled(true);
        myTextArea.addKeyListener(keyListener);
        this.setOpaque(true);
        this.setBorder(null);
        this.setViewportView(myTextArea);
    }

    private TextAttributes getColumnTextAttributes(int column) {
        return CsvColorSettings.getTextAttributesOfColumn(column, myUserDataHolder);
    }

    private Color getColumnForegroundColor(int column, Color fallback) {
        TextAttributes textAttributes = getColumnTextAttributes(column);
        return textAttributes == null || textAttributes.getForegroundColor() == null ? fallback : TargetAWT.to(textAttributes.getForegroundColor());
    }

    private Color getColumnBackgroundColor(int column, Color fallback) {
        TextAttributes textAttributes = getColumnTextAttributes(column);
        return textAttributes == null || textAttributes.getBackgroundColor() == null ? fallback : TargetAWT.to(textAttributes.getBackgroundColor());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        EditorColorsScheme editorColorsScheme = EditorColorsManager.getInstance().getGlobalScheme();

        if (isSelected) {
            myTextArea.setForeground(table.getSelectionForeground());
            myTextArea.setBackground(table.getSelectionBackground());
        } else {
            myTextArea.setForeground(getColumnForegroundColor(column, table.getForeground()));
            myTextArea.setBackground(getColumnBackgroundColor(column, table.getBackground()));
        }
        if (hasFocus) {
            myTextArea.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            if (table.isCellEditable(row, column)) {
                myTextArea.setForeground(UIManager.getColor(editorColorsScheme.getDefaultForeground()));
                myTextArea.setBackground(UIManager.getColor(editorColorsScheme.getDefaultBackground()));
            }
        } else {
            myTextArea.setBorder(new EmptyBorder(1, 2, 1, 2));
        }

        this.setText(value == null ? "" : value.toString());

        final int columnWidth = table.getColumnModel().getColumn(column).getWidth();
        final int rowHeight = table.getRowHeight(row);

        this.setSize(columnWidth, rowHeight);
        this.validate();
        myTextArea.setSize(columnWidth, rowHeight);
        myTextArea.validate();

        return this;
    }

    @Override
    public Dimension getPreferredSize() {
        try {
            final Rectangle2D rectangle = myTextArea.modelToView2D(myTextArea.getDocument().getLength());
            if (rectangle != null) {
                return new Dimension(this.getWidth(),
                        (int)(this.getInsets().top + rectangle.getY() + rectangle.getHeight() + this.getInsets().bottom));
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return super.getPreferredSize();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return getTableCellRendererComponent(table, value, isSelected, true, row, column);
    }

    @Override
    public Object getCellEditorValue() {
        return myTextArea.getText();
    }

    protected void setText(@NotNull String text) {
        Font font = determineFont(text);
        this.setFont(font);
        myTextArea.setFont(font);
        myTextArea.setText(text);
    }

    protected Font determineFont(@NotNull String text) {
        Font finalFont = UIUtil.getFontWithFallback(EditorColorsManager.getInstance().getGlobalScheme().getFont(EditorFontType.PLAIN));

        FontFallbackIterator it = new FontFallbackIterator();
        it.setPreferredFont(finalFont.getFamily(), finalFont.getSize());
        it.setFontStyle(finalFont.getStyle());
        it.start(text, 0, text.length());
        for (; !it.atEnd(); it.advance()) {
            Font font = it.getFont();
            if (!font.getFamily().equals(finalFont.getFamily())) {
                finalFont = font;
                break;
            }
        }

        return finalFont;
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        fireStopCellEditing();
        return true;
    }

    @Override
    public void cancelCellEditing() {
        fireCancelCellEditing();
    }

    protected void fireStopCellEditing() {
        ChangeEvent changeEvent = new ChangeEvent(this);
        synchronized (cellEditorListenerSet) {
            Iterator<CellEditorListener> it = cellEditorListenerSet.iterator();
            while (it.hasNext()) {
                it.next().editingStopped(changeEvent);
            }
        }
    }

    protected void fireCancelCellEditing() {
        ChangeEvent changeEvent = new ChangeEvent(this);
        synchronized (cellEditorListenerSet) {
            Iterator<CellEditorListener> it = cellEditorListenerSet.iterator();
            while (it.hasNext()) {
                it.next().editingCanceled(changeEvent);
            }
        }
    }

    @Override
    public void addCellEditorListener(CellEditorListener cellEditorListener) {
        synchronized (cellEditorListenerSet) {
            cellEditorListenerSet.add(cellEditorListener);
        }
    }

    @Override
    public void removeCellEditorListener(CellEditorListener cellEditorListener) {
        synchronized (cellEditorListenerSet) {
            cellEditorListenerSet.remove(cellEditorListener);
        }
    }

    @Override
    protected void processFocusEvent(FocusEvent focusEvent) {
        super.processFocusEvent(focusEvent);
        myTextArea.grabFocus();
    }
}
