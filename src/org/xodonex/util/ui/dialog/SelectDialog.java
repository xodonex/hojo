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
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.xodonex.util.ui.GuiResource;

public class SelectDialog extends BasicDialog {

    private static final long serialVersionUID = 1L;

    private JButton[] _buttons;

    private boolean _singleSelection;
    private JList _list;
    private JScrollPane _scroll;
    private boolean _allowDelete;

    private ListFilterModel _model;

    /*
     * -----------------------------------------------------------------------
     * Constructors
     * -----------------------------------------------------------------------
     */

    public SelectDialog() {
        super();
    }

    public SelectDialog(GuiResource rsrc) {
        super(rsrc);
    }

    public SelectDialog(JDialog owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public SelectDialog(JFrame owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    @Override
    protected void setup() {
        super.setup();

        _buttons = new JButton[] {
                _guiResource.createButton("btn.Ok"),
                _guiResource.createButton("btn.Cancel"),
        };

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == _buttons[0]) {
                    close(null);
                }
                else {
                    cancel();
                }
            }
        };
        _buttons[0].addActionListener(listener);
        _buttons[1].addActionListener(listener);

        _model = new ListFilterModel();
        _list = new JList(_model);
        _list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (_allowDelete &&
                        (e.getModifiers()
                                & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                    int idx = _list.locationToIndex(e.getPoint());
                    if (idx >= 0) {
                        if (_list.getCellBounds(idx, idx)
                                .contains(e.getPoint())) {
                            _model.removeData(idx);
                        }
                    }
                }
                else if (_singleSelection && e.getClickCount() == 2 &&
                        (e.getModifiers()
                                & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    int index = _list.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        close(null);
                    }
                }
            }
        });
        /*
         * _list.addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent
         * e) { if (!_allowDelete) { return; } if (e.getKeyCode() ==
         * e.VK_DELETE) { _model.removeData(_list.getSelectedIndex()); } } });
         */

        _scroll = new JScrollPane(_list);

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        p.add(_scroll);
        getContentPane().add(p, BorderLayout.CENTER);
        addButtons(_buttons, null, 0);
    }

    /*
     * -----------------------------------------------------------------------
     * New methods
     * -----------------------------------------------------------------------
     */

    public Object select(String title, Collection c, boolean singleSelection)
            throws IllegalStateException {
        return select(title, c, singleSelection, false);
    }

    public Object select(String title, Collection c,
            boolean singleSelection, boolean allowDelete)
            throws IllegalStateException {
        if (c.size() == 0) {
            throw new IllegalArgumentException();
        }

        setTitle(title);
        _model.setData(c instanceof List ? (List)c : new ArrayList(c));
        _allowDelete = allowDelete;

        _singleSelection = singleSelection;
        _list.setSelectionMode(singleSelection
                ? ListSelectionModel.SINGLE_SELECTION
                : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        ListSelectionModel lsm = _list.getSelectionModel();
        lsm.setSelectionInterval(0, 0);

        xeq();

        _model.setData(null);

        Object result;
        if (isCancelled()) {
            result = null;
        }
        else if (singleSelection) {
            int sel = lsm.getMinSelectionIndex();
            result = sel < 0 ? null : new Integer(sel);
        }
        else {
            int i = 0;
            Iterator it = c.iterator();
            while (it.hasNext()) {
                it.next();
                if (!lsm.isSelectedIndex(i++)) {
                    it.remove();
                }
            }
            result = c;
        }

        return result;
    }

    private class ListFilterModel implements ListModel {
        private List _data;
        private Collection _listeners = new ArrayList(2);

        ListFilterModel() {
        }

        private void fireChange() {
            ListDataEvent e = new ListDataEvent(this,
                    ListDataEvent.CONTENTS_CHANGED, 0,
                    _data == null ? 0 : _data.size());
            for (Iterator i = _listeners.iterator(); i.hasNext();) {
                ((ListDataListener)i.next()).contentsChanged(e);
            }
        }

        private void fireRemoval(int idx1, int idx2) {
            ListDataEvent e = new ListDataEvent(this,
                    ListDataEvent.INTERVAL_REMOVED, idx1, idx2);
            for (Iterator i = _listeners.iterator(); i.hasNext();) {
                ((ListDataListener)i.next()).intervalRemoved(e);
            }
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            if (l != null) {
                _listeners.add(l);
            }
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            _listeners.remove(l);
        }

        @Override
        public Object getElementAt(int index) {
            return _data.get(index);
        }

        @Override
        public int getSize() {
            return _data.size();
        }

        public void setData(List l) {
            _data = l;
            fireChange();
        }

        public void removeData(int idx) {
            _data.remove(idx);
            fireRemoval(idx, idx);
        }
    }

}
