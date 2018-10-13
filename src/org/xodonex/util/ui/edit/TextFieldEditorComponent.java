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

import java.util.ResourceBundle;

import javax.swing.JTextField;

/**
 * An EditorComponent which uses a JTextField as user interface.
 *
 * @author Henrik Lauritzen
 */
public final class TextFieldEditorComponent extends AbstractEditorComponent {

    public TextFieldEditorComponent(DataEditor editor,
            ResourceBundle rsrc, int labelPlacement) {
        super(editor);

        String txt = editor.getAsText();
        int defaultColumns;

        if (txt == null) {
            defaultColumns = 15;
        }
        else {
            defaultColumns = txt.length();
            if (defaultColumns < 3) {
                defaultColumns = 3;
            }
        }

        _ui = new JTextField(txt, defaultColumns);
        Object cfg = init(_ui, rsrc, labelPlacement);

        if (cfg != null) {
            _ui.setColumns(Integer.parseInt(cfg.toString()));
        }

        _ui.setMaximumSize(_ui.getPreferredSize());
    }

    @Override
    public void updateFromEditor() {
        String txt = getEditor().getAsText();
        _ui.setText(txt == null ? "" : txt);
        _ui.repaint();
    }

    @Override
    public boolean updateFromUI() {
        String txt = _ui.getText();
        return getEditor().trySetAsText("".equals(txt) ? null : txt);
    }

    private JTextField _ui;

}
