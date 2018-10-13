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

import javax.swing.JCheckBox;

/**
 * An EditorComponent which uses a JCheckBox as user interface.
 *
 * @author Henrik Lauritzen
 */
public final class CheckBoxEditorComponent extends AbstractEditorComponent {

    public CheckBoxEditorComponent(DataEditor editor, ResourceBundle rsrc,
            int type) {
        super(editor);

        _ui = new JCheckBox();
        init(_ui, rsrc, type);
        updateFromEditor();
    }

    @Override
    public void updateFromEditor() {
        Object ev = getEditor().getValue();
        boolean selected = ev instanceof Boolean ? ((Boolean)ev).booleanValue()
                : false;
        _ui.setSelected(selected);
        _ui.repaint();
    }

    @Override
    public boolean updateFromUI() {
        DataEditor ed = getEditor();
        Object item = _ui.isSelected() ? Boolean.TRUE : Boolean.FALSE;
        return ed.trySetValue(item);
    }

    private JCheckBox _ui;

}
