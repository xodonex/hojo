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
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import javax.swing.undo.UndoManager;

import org.xodonex.util.ui.GuiResource;

public class TextDialog extends BasicDialog {

    /*
     * -----------------------------------------------------------------------
     * Instance fields
     * -----------------------------------------------------------------------
     */

    private static final long serialVersionUID = 1L;

    private JTextComponent _edit;
    private UndoManager _undo;
    private JButton _ok;
    private JButton _cancel;

    /*
     * -----------------------------------------------------------------------
     * Constructors
     * -----------------------------------------------------------------------
     */

    public TextDialog() {
        this(null, null, null);
    }

    public TextDialog(GuiResource rsrc) {
        this(rsrc == null ? null : rsrc.getMainFrame(), rsrc, null);
    }

    public TextDialog(JFrame owner, GuiResource rsrc) {
        this(owner, rsrc, null);
    }

    public TextDialog(JDialog owner, GuiResource rsrc) {
        super(owner, rsrc);
        setup0();
    }

    public TextDialog(JFrame owner, GuiResource rsrc, JTextComponent edit) {
        super(owner, rsrc);
        _edit = edit;
        setup0();
    }

    private void setup0() {
        if (_edit == null) {
            _edit = createTextComponent();
        }

        JScrollPane scroll = new JScrollPane(_edit);
        getContentPane().add(scroll, BorderLayout.CENTER);

        _ok = _guiResource.createButton("btn.Ok");
        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == _ok) {
                    close(null);
                }
                else {
                    cancel();
                }
            }
        };
        _ok.addActionListener(listener);

        _cancel = _guiResource.createButton("btn.Cancel");
        _cancel.addActionListener(listener);

        Keymap keymap = JTextComponent.addKeymap(
                getClass().getName() + "@" + Integer.toHexString(hashCode()),
                _edit.getKeymap());
        String s = _guiResource.getString("key.Undo");

        if (s.length() > 0) {
            keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(s),
                    new AbstractAction() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (_undo.canUndo()) {
                                try {
                                    _undo.undo();
                                    return;
                                }
                                catch (Exception _e) {
                                }
                            }
                            Toolkit.getDefaultToolkit().beep();
                        }
                    });
        }

        s = _guiResource.getString("key.Redo");
        if (s.length() > 0) {
            keymap.addActionForKeyStroke(KeyStroke.getKeyStroke(s),
                    new AbstractAction() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (_undo.canRedo()) {
                                try {
                                    _undo.redo();
                                    return;
                                }
                                catch (Exception _e) {
                                }
                            }
                            Toolkit.getDefaultToolkit().beep();
                        }
                    });
        }
        _edit.setKeymap(keymap);

        _undo = new UndoManager();
        _undo.setLimit(200);
        _edit.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                _undo.addEdit(e.getEdit());
            }
        });

        addButtons(new JButton[] { _ok, _cancel }, null, 0);
    }

    /*
     * -----------------------------------------------------------------------
     * New methods
     * -----------------------------------------------------------------------
     */

    public void show(String text, String title) throws IllegalStateException {
        xeq(new StringBuffer(text), title, false);
    }

    public StringBuffer edit(StringBuffer sb, String title)
            throws IllegalStateException {
        return xeq(sb, title, true);
    }

    public StringBuffer xeq(StringBuffer sb, String title, boolean editable)
            throws IllegalStateException {
        setTitle(title);
        _edit.setEditable(editable);
        _cancel.setVisible(editable);
        _edit.setText((sb == null) ? "" : sb.toString());
        _edit.setSize(_edit.getPreferredSize());

        xeq();

        if (isCancelled() || !editable) {
            sb = null;
        }
        else if (sb == null) {
            sb = new StringBuffer(_edit.getText());
        }
        else {
            sb.setLength(0);
            sb.append(_edit.getText());
        }

        _undo.discardAllEdits();
        _edit.setText("");

        return sb;
    }

    public JTextComponent getEditor() {
        return _edit;
    }

    protected JTextComponent createTextComponent() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("monospaced", Font.PLAIN, 12));
        area.setWrapStyleWord(true);
        return area;
    }

}
