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

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.Border;

import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.WindowManager;

/**
 * A basic window.
 */
public abstract class BasicWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    protected final Object _lock = new Object();
    protected Box _buttonBox;
    protected boolean _active = false;
    protected GuiResource _guiResource;

    public BasicWindow() {
        this(null, null);
    }

    public BasicWindow(GuiResource rsrc) {
        this(rsrc == null ? null : rsrc.getMainFrame(), rsrc);
    }

    public BasicWindow(JFrame owner, GuiResource rsrc) {
        super();
        if (owner != null) {
            setIconImage(owner.getIconImage());
        }
        _guiResource = rsrc == null ? GuiResource.getDefaultInstance() : rsrc;
        setup();
    }

    /**
     * Show the window.
     */
    public void activate() {
        synchronized (_lock) {
            if (!_active) {
                setVisible(true);
                toFront();
            }
        }
    }

    /**
     * Hide the window.
     */
    public void deactivate() {
        synchronized (_lock) {
            if (_active) {
                setVisible(false);
            }
        }
    }

    @Override
    public boolean isActive() {
        synchronized (_lock) {
            return _active;
        }
    }

    /**
     * Wait until the window closes.
     * 
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    public void waitFor() throws InterruptedException {
        synchronized (_lock) {
            if (_active) {
                _lock.wait();
            }
        }
    }

    /**
     * This method is invoked by the constructor, and is responsible for
     * configuration of the dialog. The default implementation must be called by
     * subclasses.
     */
    protected void setup() {
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
    }

    /**
     * @return true iff the window is allowed to be closed. The default
     *         implementation returns true.
     */
    protected boolean checkClose() {
        return true;
    }

    /**
     * Create a Box containing the specified buttons, add it to this dialog and
     * return it.
     *
     * @param buttons
     *            the buttons to be added
     * @param bdr
     *            the border for the bottons
     * @param defBtn
     *            the default button
     */
    protected void addButtons(JButton[] buttons, Border bdr, int defBtn) {
        _buttonBox = new Box(BoxLayout.X_AXIS);

        for (int i = 0; i < buttons.length; i++) {
            _buttonBox.add(buttons[i]);
            if (i < buttons.length - 1) {
                _buttonBox.add(Box.createHorizontalStrut(10));
            }
        }

        JPanel p1 = new JPanel();
        if (bdr != null) {
            p1.setBorder(bdr);
        }
        JPanel p = new JPanel();
        // p.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        p.add(_buttonBox);
        p1.add(p);
        getContentPane().add(p1, BorderLayout.SOUTH);

        if (defBtn >= 0) {
            getRootPane().setDefaultButton(buttons[defBtn]);
        }
    }

    @Override
    public void setVisible(boolean visible) {
        trySetVisible(visible);
    }

    public boolean trySetVisible(boolean visible) {
        synchronized (_lock) {
            _active = visible;
            if (visible) {
                pack();
                setSize(getPreferredSize());
                WindowManager.centerOnScreen(this);
                super.setVisible(true);
                return true;
            }
            else {
                if (!checkClose()) {
                    return false;
                }
                super.setVisible(false);
                _lock.notifyAll();
                return true;
            }
        }
    }

}
