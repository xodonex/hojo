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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import org.xodonex.util.ui.GuiResource;

/**
 *
 * @author Henrik Lauritzen
 */
public class DisplayDialog extends BasicDialog {

    private static final long serialVersionUID = 1L;

    private JScrollPane _panel;
    private Component _comp;

    public DisplayDialog() {
        super();
    }

    public DisplayDialog(GuiResource rsrc) {
        super(rsrc);
    }

    public DisplayDialog(JDialog owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public DisplayDialog(JFrame owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public Component getComponent() {
        return _comp;
        // return _panel.getComponent(0);
    }

    public void setComponent(Component c) {
        /*
         * _panel.removeAll(); if (c != null) { _panel.add(c); }
         */
        _panel.setViewportView(_comp = c);
        repaint();
    }

    public void show(Component c) {
        setComponent(c);
        setVisible(true);
    }

    @Override
    protected void setup() {
        super.setup();
        getContentPane().add(_panel = new JScrollPane(), BorderLayout.CENTER);
        /*
         * ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
         * ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS)
         */
        JButton close = _guiResource.createButton("btn.Ok");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close(null);
            }
        });
        addButtons(new JButton[] { close }, null, 0);
    }

}
