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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.UiUtils;

/**
 * A generic dialog box with standard layouts.
 */
public class GenericDialog extends JDialog implements DialogConstants {

    public static Icon getIconFor(int style) {
        switch (style) {
        case INFORMATION_MESSAGE:
            return UIManager.getIcon("OptionPane.informationIcon");
        case QUESTION_MESSAGE:
            return UIManager.getIcon("OptionPane.questionIcon");
        case WARNING_MESSAGE:
            return UIManager.getIcon("OptionPane.warningIcon");
        case ERROR_MESSAGE:
            return UIManager.getIcon("OptionPane.errorIcon");
        default:
            return null;
        }
    }

    private static final long serialVersionUID = 1L;

    public GenericDialog() {
        this((Frame)null, null);
    }

    public GenericDialog(GuiResource rsrc) {
        this((Frame)null, rsrc);
    }

    public GenericDialog(Dialog owner, GuiResource rsrc) {
        super(owner, true);
        if (owner != null) {
            owner.toFront();
        }
        init(rsrc);
    }

    public GenericDialog(Frame owner, GuiResource rsrc) {
        super(owner == null && rsrc != null ? rsrc.getMainFrame() : owner,
                true);
        if (owner == null && rsrc != null) {
            owner = rsrc.getMainFrame();
        }
        if (owner != null) {
            owner.toFront();
        }
        init(rsrc);
    }

    public Icon getIcon() {
        return _iconLabel.getIcon();
    }

    public GuiResource getResource() {
        return _guiResource;
    }

    protected void setIcon(Icon icon) {
        Container cp = getContentPane();
        cp.remove(_iconLabel);

        if (icon == null) {
            _iconLabel.setIcon(null);
        }
        else {
            _iconLabel.setIcon(icon);
            cp.add(_iconLabel, BorderLayout.WEST);
        }
    }

    protected void setIcon(int style) {
        setIcon(getIconFor(style));
    }

    protected void clearContents() {
        if (_contents != null) {
            getContentPane().remove(_contents);
        }
    }

    protected void setContents(Component contents) {
        clearContents();
        if ((_contents = contents) == null) {
            return;
        }

        Dimension d = contents.getPreferredSize();
        Rectangle r = getGraphicsConfiguration().getBounds();

        if (d.width > 0.75 * r.width || d.height > 0.75 * r.height &&
                !(contents instanceof JScrollPane)) {
            // wrap the contents in a scroll pane, if necessary
            JScrollPane jsp = new JScrollPane(contents);
            jsp.setBorder(BorderFactory.createLoweredBevelBorder());
            _contents = jsp;
        }
        else {
            JPanel p = new JPanel();
            p.add(contents);
            _contents = p;
        }

        getContentPane().add(_contents, BorderLayout.CENTER);
    }

    protected void clearButtons() {
        _buttonPane.removeAll();
    }

    protected int addButton(JButton btn) {
        if (btn == null) {
            throw new NullPointerException();
        }

        int result = _buttonPane.getComponentCount();
        _buttonPane.add(btn);
        return result;
    }

    protected int addButton(String rsrc, ActionListener l) {
        JButton btn = _guiResource.createButton(rsrc);

        if (l != null) {
            btn.addActionListener(l);
        }
        else {
            boolean cancel = ("btn.Close".equals(rsrc)
                    || "btn.Cancel".equals(rsrc));
            btn.addActionListener(new ButtonListener(
                    _buttonPane.getComponentCount(), cancel));
        }

        return addButton(btn);
    }

    public void setDefaultButton(int button) {
        JButton btn = (JButton)_buttonPane.getComponent(button);
        getRootPane().setDefaultButton(btn);
    }

    /**
     * Cancels the dialog
     *
     * @param code
     *            identifies how the dialog was cancelled (BUTTON_xxx).
     * @return true
     */
    protected boolean cancel(int code) {
        _resultCode = code;
        setVisible(false);
        return true;
    }

    /**
     * Attempts to close the dialog.
     *
     * @param code
     *            identifies how the dialog was closed (BUTTON_xxx).
     * @return true iff the dialog was successfully closed
     */
    protected boolean acknowledge(int code) {
        if (!UiUtils.verifyInput(_contents, true)) {
            return false;
        }
        _resultCode = code;
        setVisible(false);
        return true;
    }

    /**
     * @return the result code indicating how the dialog was closed
     */
    protected int getResultCode() {
        return _resultCode;
    }

    @Override
    public void setSize(Dimension d) {
        setSize(d.width, d.height);
    }

    @Override
    public void setSize(int w, int h) {
        Rectangle r = getGraphicsConfiguration().getBounds();
        double fac = 0.9;

        if (w > fac * r.width) {
            w = (int)(fac * r.width);
        }
        if (h > fac * r.height) {
            h = (int)(fac * r.height);
        }

        super.setSize(w, h);
    }

    private GuiResource _guiResource;
    private JLabel _iconLabel;
    private Box _buttonPane;
    private Component _contents;
    private int _resultCode = BUTTON_NONE;

    // set up the basic dialog contents
    private void init(GuiResource rsrc) {
        _guiResource = rsrc == null ? GuiResource.getDefaultInstance() : rsrc;

        Container cp = getContentPane();
        if (cp instanceof JComponent) {
            ((JComponent)cp).setBorder(
                    BorderFactory.createEmptyBorder(5, 10, 0, 10));
            cp.setLayout(new BorderLayout());
        }
        else {
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
            setContentPane(p);
            cp = p;
        }

        _iconLabel = new JLabel();
        _iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        _buttonPane = new Box(BoxLayout.X_AXIS);
        JPanel p = new JPanel();
        p.add(_buttonPane);
        cp.add(p, BorderLayout.SOUTH);

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        addWindowListener(new CancelWindowListener());
    }

    private class CancelWindowListener extends WindowAdapter {
        @Override
        public void windowOpened(WindowEvent e) {
            // bring the dialog up as the front window
            toFront();
        }

        @Override
        public void windowClosing(WindowEvent e) {
            // close the dialog with the BUTTON_NONE code
            cancel(BUTTON_NONE);
        }
    }

    private class ButtonListener implements ActionListener {
        private int _code;
        private boolean _cancel;

        public ButtonListener(int code, boolean cancel) {
            _code = code;
            _cancel = cancel;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (_cancel) {
                cancel(_code);
            }
            else {
                acknowledge(_code);
            }
        }
    }
}
