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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.xodonex.util.ui.GuiResource;

public class PasswordDialog extends BasicDialog {

    private static final long serialVersionUID = 1L;

    private JPasswordField _pf;
    private JLabel _label;
    private JButton _ok;
    private JButton _cancel;

    /*
     * -----------------------------------------------------------------------
     * Constructors
     * -----------------------------------------------------------------------
     */

    public PasswordDialog() {
        super();
    }

    public PasswordDialog(GuiResource rsrc) {
        super(rsrc);
    }

    public PasswordDialog(JDialog owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public PasswordDialog(JFrame owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    @Override
    protected void setup() {
        super.setup();

        _ok = _guiResource.createButton("btn.Ok");
        _cancel = _guiResource.createButton("btn.Cancel");

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
        _cancel.addActionListener(listener);

        _pf = new JPasswordField();
        _label = new JLabel();
        _label.setLabelFor(_pf);

        Box b = new Box(BoxLayout.Y_AXIS);
        b.add(_label);
        b.add(_pf);
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        p.add(b);

        getContentPane().add(p, BorderLayout.CENTER);
        addButtons(new JButton[] { _ok, _cancel }, null, 0);
    }

    /*
     * -----------------------------------------------------------------------
     * New methods
     * -----------------------------------------------------------------------
     */

    public char[] edit(String title, String label, int columns)
            throws IllegalStateException {
        setTitle(title);
        _label.setText(label);
        _pf.setColumns(columns);

        xeq();
        char[] res = isCancelled() ? null : _pf.getPassword();
        _pf.setText("");

        return res;
    }

}
