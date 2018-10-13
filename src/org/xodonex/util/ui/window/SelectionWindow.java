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
package org.xodonex.util.ui.window;

import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.xodonex.util.ui.GuiResource;

/**
 *
 * @author Henrik Lauritzen
 */
public class SelectionWindow extends BasicWindow {

    private static final long serialVersionUID = 1L;

    private JList _list;

    private Object[] _data;
    private SelectionWindowListener _listener;

    public SelectionWindow() {
        super();
    }

    public SelectionWindow(GuiResource rsrc) {
        super(rsrc);
    }

    public SelectionWindow(JFrame owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public void select(String title, Object[] options,
            SelectionWindowListener l) {
        startSelecting(title, options, l);
        setVisible(true);
    }

    public void startSelecting(String title, Object[] options,
            SelectionWindowListener l) {
        if ((_data = options) == null) {
            throw new NullPointerException();
        }
        else if (options.length == 0) {
            throw new IllegalArgumentException();
        }

        setTitle(title);
        _listener = l;

        _list.setListData(options);
    }

    @Override
    protected void setup() {
        super.setup();

        _list = new JList();
        _list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        _list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (_listener != null && e.getClickCount() == 2 &&
                        (e.getModifiers()
                                & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    int index = _list.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        _listener.itemSelected(_data, index);
                    }
                }
            }
        });
        _list.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (_listener != null && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    int index = _list.getSelectedIndex();
                    if (index >= 0) {
                        _listener.itemSelected(_data, index);
                    }
                }
            }
        });

        getContentPane().add(new JScrollPane(_list),
                java.awt.BorderLayout.CENTER);
    }

    public static interface SelectionWindowListener {

        public void itemSelected(Object[] data, int index);

    }
}
