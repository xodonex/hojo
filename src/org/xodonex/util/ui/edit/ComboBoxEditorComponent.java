// Copyright 1998,1999,2000,2001,2018, Henrik Lauritzen.
/*
    This file is part of the Hojo interpreter & toolkit.

    The Hojo interpreter & toolkit is free software: you can redistribute it
    and/or modify it under the terms of the GNU Affero General Public License
    as published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    The Hojo interpreter & toolkit is distributed in the hope that it will
    be useful or (at least have historical interest),
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this file.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.xodonex.util.ui.edit;

import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ComboBoxEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;

/**
 * An EditorComponent which uses a JComboBox as user interface.
 *
 * @author Henrik Lauritzen
 */
public final class ComboBoxEditorComponent extends AbstractEditorComponent {

    public ComboBoxEditorComponent(DataEditor editor,
            ResourceBundle rsrc, int labelPlacement,
            boolean editable, boolean indexed) {
        super(editor);
        _indexed = indexed & !editable;
        _ui = new JComboBox();
        _ui.setEditable(editable);
        _editor = editable ? _ui.getEditor() : null;

        Object cfg = init(_ui,
                editable ? (JComponent)_editor.getEditorComponent() : _ui,
                rsrc, labelPlacement);
        if (cfg instanceof List) {
            _ui.setModel(new DefaultComboBoxModel(((List)cfg).toArray()));
        }
        else {
            String[] tags = editor.getTags();
            if (tags != null) {
                _ui.setModel(new DefaultComboBoxModel(tags));
            }
        }

        _ui.setMaximumSize(_ui.getPreferredSize());
        updateFromEditor();
    }

    @Override
    public void updateFromEditor() {
        DataEditor e = getEditor();
        if (_indexed) {
            Integer i = (Integer)e.getValue();
            _ui.setSelectedIndex(i == null ? 0 : i.intValue());
        }
        else {
            String text = e.getAsText();
            if (text == null) {
                _ui.setSelectedIndex(0);
            }
            else {
                _ui.setSelectedItem(text);
            }
        }
        _ui.repaint();
    }

    @Override
    public boolean updateFromUI() {
        DataEditor ed = getEditor();

        if (_editor != null) {
            // ensure that the edited value is set
            _ui.setSelectedItem(_editor.getItem());
        }

        if (_indexed) {
            return ed.trySetValue(new Integer(_ui.getSelectedIndex()));
        }
        else {
            return ed.trySetValue(_ui.getSelectedItem());
        }
    }

    private JComboBox _ui;
    private ComboBoxEditor _editor;
    private boolean _indexed;

}
