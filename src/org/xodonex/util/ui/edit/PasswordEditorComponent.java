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

import java.util.Arrays;
import java.util.ResourceBundle;

import javax.swing.JPasswordField;

import org.xodonex.util.security.PwdUtils;

/**
 * An EditorComponent which uses a JPasswordField as user interface.
 *
 * @author Henrik Lauritzen
 */
public final class PasswordEditorComponent extends AbstractEditorComponent {

    public PasswordEditorComponent(DataEditor editor, ResourceBundle rsrc,
            int labelPlacement) {
        super(editor);

        _ui = new JPasswordField(8);
        Object cfg = init(_ui, rsrc, labelPlacement);

        if (cfg != null) {
            _ui.setColumns(Integer.parseInt(cfg.toString()));
        }
        _ui.setMaximumSize(_ui.getPreferredSize());
    }

    @Override
    public void updateFromEditor() {
        _ui.setText("");
        _ui.repaint();
    }

    @Override
    public boolean updateFromUI() {
        char[] pwd = _ui.getPassword();
        String cpwd = PwdUtils.xcrypt(pwd);
        Arrays.fill(pwd, '\0');

        return getEditor().trySetAsText(cpwd);
    }

    private JPasswordField _ui;

}
