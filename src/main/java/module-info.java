/**
 * @author VISTALL
 * @since 16/05/2023
 */
open module net.seesharpsoft.intellij.plugins.csv {
    requires consulo.application.api;
    requires consulo.base.icon.library;
    requires consulo.code.editor.api;
    requires consulo.color.scheme.api;
    requires consulo.component.api;
    requires consulo.configurable.api;
    requires consulo.disposer.api;
    requires consulo.document.api;
    requires consulo.file.editor.api;
    requires consulo.grid.editor.api;
    requires consulo.ide.api;
    requires consulo.language.api;
    requires consulo.language.code.style.api;
    requires consulo.language.code.style.ui.api;
    requires consulo.language.editor.api;
    requires consulo.language.impl;
    requires consulo.language.spellchecker.api;
    requires consulo.localize.api;
    requires consulo.logging.api;
    requires consulo.navigation.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.ui.ex.awt.api;
    requires consulo.undo.redo.api;
    requires consulo.util.collection;
    requires consulo.util.dataholder;
    requires consulo.util.io;
    requires consulo.util.lang;
    requires consulo.util.xml.serializer;
    requires consulo.virtual.file.system.api;

    requires sharping.commons;

    // TODO remove in future
    requires java.desktop;
    requires forms.rt;
}
