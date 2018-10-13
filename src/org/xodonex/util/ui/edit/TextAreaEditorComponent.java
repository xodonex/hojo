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

import java.awt.Dimension;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.xodonex.util.ui.ResourceUtils;

/**
 * An EditorComponent which uses a JTextArea as user interface.
 *
 * @author Henrik Lauritzen
 */
public final class TextAreaEditorComponent extends AbstractEditorComponent {

    public TextAreaEditorComponent(DataEditor editor, ResourceBundle rsrc,
            int config) {
        super(editor);

        _ui = new JTextArea(20, 3);
        _ui.setWrapStyleWord(true);
        _ui.setText(editor.getAsText());
        JScrollPane pane = new JScrollPane(_ui);

        Object[] cfg = ResourceUtils.decodeString(rsrc.getString(
                getResourceId(editor.getID())), 0);
        List l = null;
        if (cfg.length > 3 && cfg[3] instanceof List) {
            l = (List)cfg[3];
        }

        if (l != null) {
            int cols = Integer.parseInt(l.get(0).toString());
            int rows = Integer.parseInt(l.get(1).toString());
            int policy;

            if (cols < 0) {
                cols = -cols;
                policy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
            }
            else if (cols == 0) {
                policy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
            }
            else {
                policy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
            }
            _ui.setColumns(cols == 0 ? 20 : cols);
            pane.setHorizontalScrollBarPolicy(policy);

            if (rows < 0) {
                rows = -rows;
                policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
            }
            else if (rows == 0) {
                policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
            }
            else {
                policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
            }
            _ui.setRows(rows == 0 ? 2 : rows);
            pane.setVerticalScrollBarPolicy(policy);
        }

        Dimension d = _ui.getPreferredSize();
        _ui.setSize(d);
        _ui.setMinimumSize(d);

        init(pane, _ui, cfg, config);
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

    private JTextArea _ui;

}
