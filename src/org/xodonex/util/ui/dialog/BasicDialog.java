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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.UiUtils;
import org.xodonex.util.ui.WindowManager;

/**
 * A basic dialog class.
 */
public class BasicDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    protected Box _buttonBox;
    protected GuiResource _guiResource;

    private transient Object _result;
    private transient boolean _cancelled;

    public BasicDialog() {
        this(null);
    }

    public BasicDialog(GuiResource rsrc) {
        this(rsrc == null ? null : rsrc.getMainFrame(), rsrc);
    }

    public BasicDialog(JDialog owner, GuiResource rsrc) {
        super(owner, true);
        _guiResource = rsrc == null ? GuiResource.getDefaultInstance() : rsrc;
        setup();
    }

    public BasicDialog(JFrame owner, GuiResource rsrc) {
        super(owner, true);
        _guiResource = rsrc == null ? GuiResource.getDefaultInstance() : rsrc;
        setup();
    }

    /**
     * Execute this dialog, return null if the dialog was canceled.
     * <strong>N.B.:</strong> if the dialog is not modal, then this will always
     * yield a return value of null, and the method will not block until the
     * dialog has been hidden.
     *
     * @return the result value from executing the dialog
     */
    public Object xeq() {
        return xeq(null);
    }

    /**
     * Close the dialog, and result in the given value.
     *
     * @param result
     *            the result value
     * @return whether the dialog could be closed
     */
    public boolean close(Object result) {
        if (!UiUtils.verifyInput(this)) {
            return false;
        }
        _result = result;
        _cancelled = false;
        setVisible(false);
        return true;
    }

    /**
     * Cancel the dialog.
     */
    public void cancel() {
        _result = null;
        _cancelled = true;
        setVisible(false);
    }

    /**
     * @return whether this dialog was cancelled.
     */
    public boolean isCancelled() {
        return _cancelled;
    }

    /**
     * @return the last result value
     */
    public Object getResult() {
        return _result;
    }

    /**
     * Clear the last result value
     */
    public void clearResult() {
        _result = null;
    }

    protected Object xeq(Object result) {
        if (isVisible()) {
            throw new IllegalStateException();
        }
        _result = result;
        _cancelled = true;
        setVisible(true);
        return _result;
    }

    /**
     * This method is invoked by the constructor, and is responsible for
     * configuration of the dialog. The default implementation must be called by
     * subclasses.
     */
    protected void setup() {
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // bring the dialog up as the front window
                toFront();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        getContentPane().setLayout(new BorderLayout());
    }

    /**
     * Create a Box containing the specified buttons, add it to this dialog and
     * return it.
     *
     * @param buttons
     *            the buttons to be added
     * @param bdr
     *            an optional border for the buttons
     * @param defBtn
     *            the default button
     * @return a component holding the buttons
     */
    protected Box addButtons(JButton[] buttons, Border bdr, int defBtn) {
        if (_buttonBox == null) {
            _buttonBox = new Box(BoxLayout.X_AXIS);

            JPanel p = new JPanel();
            p.add(_buttonBox);
            getContentPane().add(p, BorderLayout.SOUTH);
        }
        else {
            _buttonBox.removeAll();
        }

        for (int i = 0; i < buttons.length; i++) {
            _buttonBox.add(buttons[i]);
            if (i < buttons.length - 1) {
                _buttonBox.add(Box.createHorizontalStrut(10));
            }
        }

        if (bdr != null) {
            ((JPanel)_buttonBox.getParent()).setBorder(bdr);
        }

        if (defBtn >= 0) {
            getRootPane().setDefaultButton(buttons[defBtn]);
        }

        return _buttonBox;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            pack();
            setSize(getPreferredSize());
            setLocationRelativeTo(null);
            WindowManager.centerOnScreen(this);
            super.setVisible(true);
            // toFront();
        }
        else {
            super.setVisible(false);
        }
    }

}
