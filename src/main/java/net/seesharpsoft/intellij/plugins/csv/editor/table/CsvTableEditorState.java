package net.seesharpsoft.intellij.plugins.csv.editor.table;

import consulo.fileEditor.FileEditorState;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.fileEditor.FileEditorStateLevel;
import net.seesharpsoft.intellij.plugins.csv.settings.CsvEditorSettings;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CsvTableEditorState implements FileEditorState {

    private int[] columnWidths;
    private Boolean autoColumnWidthOnOpen;
    private Boolean showInfoPanel;
    private Boolean fixedHeaders;
    private Integer rowLines;

    public CsvTableEditorState() {
        columnWidths = new int[0];
    }

    public int[] getColumnWidths() {
        return this.columnWidths;
    }

    public void setColumnWidths(int[] widths) {
        this.columnWidths = widths;
    }

    public boolean showInfoPanel() {
        return showInfoPanel == null ? CsvEditorSettings.getInstance().showTableEditorInfoPanel() : showInfoPanel;
    }

    public void setShowInfoPanel(boolean showInfoPanelArg) {
        showInfoPanel = showInfoPanelArg;
    }

    public boolean getFixedHeaders() {
        return fixedHeaders == null ? CsvEditorSettings.getInstance().isHeaderRowFixed() : fixedHeaders;
    }

    public void setFixedHeaders(boolean fixedHeadersArg) {
        fixedHeaders = fixedHeadersArg;
    }

    public boolean getAutoColumnWidthOnOpen() {
        return autoColumnWidthOnOpen == null ? CsvEditorSettings.getInstance().isTableAutoColumnWidthOnOpen() : autoColumnWidthOnOpen;
    }

    public void setAutoColumnWidthOnOpen(Boolean autoColumnWidthOnOpenArg) {
        autoColumnWidthOnOpen = autoColumnWidthOnOpenArg;
    }

    public int getRowLines() {
        if (rowLines == null) {
            rowLines = CsvEditorSettings.getInstance().getTableEditorRowHeight();
        }
        return rowLines;
    }

    public void setRowLines(int rowLinesArg) {
        rowLines = rowLinesArg;
    }

    @Override
    public boolean canBeMergedWith(FileEditorState fileEditorState, FileEditorStateLevel fileEditorStateLevel) {
        return false;
    }

    public void write(@NotNull Project project, @NotNull Element element) {
        element.setAttribute("showInfoPanel", "" + showInfoPanel());
        element.setAttribute("fixedHeaders", "" + getFixedHeaders());
        if (autoColumnWidthOnOpen != null) {
            element.setAttribute("autoColumnWidthOnOpen", "" + autoColumnWidthOnOpen);
        }
        element.setAttribute("rowLines", "" + getRowLines());
        for (int i = 0; i < columnWidths.length; ++i) {
            Element cwElement = new Element("column");

            cwElement.setAttribute("index", "" + i);
            cwElement.setAttribute("width", "" + getColumnWidths()[i]);

            element.addContent(cwElement);
        }

    }

    public static CsvTableEditorState create(@NotNull Element element, @NotNull Project project, @NotNull VirtualFile file) {
        CsvTableEditorState state = new CsvTableEditorState();

        Attribute attribute = element.getAttribute("showInfoPanel");
        state.setShowInfoPanel(
                attribute == null ? CsvEditorSettings.getInstance().showTableEditorInfoPanel() : Boolean.parseBoolean(attribute.getValue())
        );

        attribute = element.getAttribute("fixedHeaders");
        state.setFixedHeaders(
                attribute == null ? false : Boolean.parseBoolean(attribute.getValue())
        );

        attribute = element.getAttribute("autoColumnWidthOnOpen");
        if (attribute != null) {
            state.setAutoColumnWidthOnOpen(Boolean.parseBoolean(attribute.getValue()));
        }

        state.setRowLines(
          StringUtil.parseInt(element.getAttributeValue("rowLines"), CsvEditorSettings.getInstance().getTableEditorRowHeight())
        );

        List<Element> columnWidthElements = element.getChildren("column");
        int[] columnWidths = new int[columnWidthElements.size()];
        int defaultColumnWidth = CsvEditorSettings.getInstance().getTableDefaultColumnWidth();
        for (int i = 0; i < columnWidthElements.size(); ++i) {
            Element columnElement = columnWidthElements.get(i);
            int index = StringUtil.parseInt(columnElement.getAttributeValue("index"), i);
            columnWidths[index] = StringUtil.parseInt(columnElement.getAttributeValue("width"), defaultColumnWidth);
        }
        state.setColumnWidths(columnWidths);

        return state;
    }

}
