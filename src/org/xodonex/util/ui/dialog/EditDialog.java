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
package org.xodonex.util.ui.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.UiUtils;
import org.xodonex.util.ui.edit.EditorPane;

/**
 *
 * @author Henrik Lauritzen
 */
public class EditDialog extends BasicDialog {

    private static final long serialVersionUID = 1L;

    private JScrollPane _panel;
    private EditorPane _pane;

    public EditDialog() {
        super();
    }

    public EditDialog(GuiResource rsrc) {
        super(rsrc);
    }

    public EditDialog(JDialog owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public EditDialog(JFrame owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public Map edit(Map data) {
        EditorPane pane = new EditorPane(null, data);
        return edit(data, pane);
    }

    public Map edit(Map data, EditorPane pane) {
        if (isVisible()) {
            throw new IllegalStateException();
        }
        // _panel.removeAll();
        _panel.setViewportView(_pane = pane);
        Map result = (Map)xeq(data);
        _pane = null;
        return result;
    }

    @Override
    protected void setup() {
        super.setup();
        getContentPane().add(_panel = new JScrollPane(), BorderLayout.CENTER);

        JButton ok = _guiResource.createButton("btn.Ok");
        JButton cancel = _guiResource.createButton("btn.Cancel");

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!UiUtils.verifyInput(_pane)) {
                    return;
                }
                close(getResult());
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        addButtons(new JButton[] { ok, cancel }, null, 0);
    }

}
