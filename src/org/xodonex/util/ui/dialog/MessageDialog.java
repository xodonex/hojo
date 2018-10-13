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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.xodonex.util.log.LogEntry;
import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.WindowManager;

/**
 * Provides various messages in a form similar to
 * {@link javax.swing.JOptionPane}. However, the MessageDialog provides
 * functionality to localize titles and button texts, and also allows an
 * optional detailed message to be shown
 */
public class MessageDialog extends GenericDialog {

    private static final long serialVersionUID = 1L;

    public MessageDialog() {
        super();
    }

    public MessageDialog(GuiResource rsrc) {
        super(rsrc);
    }

    public MessageDialog(Dialog owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public MessageDialog(Frame owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public final int showPlainMessage(String msg) {
        return showPlainMessage(null, msg, null, null);
    }

    public final int showPlainMessage(String msg, String[] buttons) {
        return showPlainMessage(null, msg, null, buttons);
    }

    public final int showPlainMessage(String msg, String details,
            String[] buttons) {
        return showPlainMessage(null, msg, details, buttons);
    }

    public final int showPlainMessage(String msg, String details) {
        return showPlainMessage(null, msg, details, null);
    }

    public int showPlainMessage(String title, String msg, String details,
            String[] buttons) {
        if (title == null) {
            title = getResource().getString("ttl.Message");
        }
        return showMessage(title, msg, details, getIconFor(PLAIN_MESSAGE),
                buttons);
    }

    public final int showInformationMessage(String msg) {
        return showInformationMessage(null, msg, null, null);
    }

    public final int showInformationMessage(String msg, String[] buttons) {
        return showInformationMessage(null, msg, null, buttons);
    }

    public final int showInformationMessage(String msg, String details,
            String[] buttons) {
        return showInformationMessage(null, msg, details, buttons);
    }

    public final int showInformationMessage(String msg, String details) {
        return showInformationMessage(null, msg, details, null);
    }

    public int showInformationMessage(String title, String msg, String details,
            String[] buttons) {
        if (title == null) {
            title = getResource().getString("ttl.Information");
        }
        return showMessage(title, msg, details, getIconFor(INFORMATION_MESSAGE),
                buttons);
    }

    public final int showQuestionMessage(String msg) {
        return showQuestionMessage(null, msg, null, null);
    }

    public final int showQuestionMessage(String msg, String[] buttons) {
        return showQuestionMessage(null, msg, null, buttons);
    }

    public final int showQuestionMessage(String msg, String details,
            String[] buttons) {
        return showQuestionMessage(null, msg, details, buttons);
    }

    public final int showQuestionMessage(String msg, String details) {
        return showQuestionMessage(null, msg, details, null);
    }

    public int showQuestionMessage(String title, String msg, String details,
            String[] buttons) {
        if (title == null) {
            title = getResource().getString("ttl.Question");
        }
        if (buttons == null) {
            buttons = new String[] { "btn.Yes", "btn.No" };
        }
        return showMessage(title, msg, details, getIconFor(QUESTION_MESSAGE),
                buttons);
    }

    public final int showWarningMessage(String msg) {
        return showWarningMessage(null, msg, null, null);
    }

    public final int showWarningMessage(String msg, String[] buttons) {
        return showWarningMessage(null, msg, null, buttons);
    }

    public final int showWarningMessage(String msg, String details,
            String[] buttons) {
        return showWarningMessage(null, msg, details, buttons);
    }

    public final int showWarningMessage(String msg, String details) {
        return showWarningMessage(null, msg, details, null);
    }

    public int showWarningMessage(String title, String msg, String details,
            String[] buttons) {
        if (title == null) {
            title = getResource().getString("ttl.Warning");
        }
        return showMessage(title, msg, details, getIconFor(WARNING_MESSAGE),
                buttons);
    }

    public final int showErrorMessage(String msg) {
        return showErrorMessage(null, msg, null, null);
    }

    public final int showErrorMessage(String msg, String[] buttons) {
        return showErrorMessage(null, msg, null, buttons);
    }

    public final int showErrorMessage(String msg, String details,
            String[] buttons) {
        return showErrorMessage(null, msg, details, buttons);
    }

    public final int showErrorMessage(String msg, String details) {
        return showErrorMessage(null, msg, details, null);
    }

    public int showErrorMessage(String title, String msg, String details,
            String[] buttons) {
        if (title == null) {
            title = getResource().getString("ttl.Error");
        }
        return showMessage(title, msg, details, getIconFor(ERROR_MESSAGE),
                buttons);
    }

    public final int showMessage(int type, String msg) {
        return showMessage(type, null, msg, null, null);
    }

    public final int showMessage(int type, String msg, String[] buttons) {
        return showMessage(type, null, msg, null, buttons);
    }

    public final int showMessage(int type, String msg, String details,
            String[] buttons) {
        return showMessage(type, null, msg, details, buttons);
    }

    public final int showMessage(int type, String msg, String details) {
        return showMessage(type, null, msg, details, null);
    }

    public int showMessage(int type, String title, String msg, String details,
            String[] buttons) {
        switch (type) {
        case INFORMATION_MESSAGE:
            return showInformationMessage(title, msg, details, buttons);
        case QUESTION_MESSAGE:
            return showQuestionMessage(title, msg, details, buttons);
        case WARNING_MESSAGE:
            return showWarningMessage(title, msg, details, buttons);
        case ERROR_MESSAGE:
            return showErrorMessage(title, msg, details, buttons);
        default:
            return showPlainMessage(title, msg, details, buttons);
        }
    }

    public final int showMessage(LogEntry e) {
        return showMessage(e, null);
    }

    public final int showMessage(LogEntry e, String[] buttons) {
        int options;
        int severity = e.getSeverity();
        if (severity < LogEntry.SEVERITY_WARNING) {
            options = severity < LogEntry.SEVERITY_INFO ? PLAIN_MESSAGE
                    : INFORMATION_MESSAGE;
        }
        else if (severity < LogEntry.SEVERITY_ERROR) {
            options = WARNING_MESSAGE;
        }
        else {
            options = ERROR_MESSAGE;
        }

        return showMessage(options, null, e.getMessage(), e.getDetails(),
                buttons);
    }

    public int showMessage(String title, String msg, String details, Icon icon,
            String[] buttons) {
        if (isVisible()) {
            throw new IllegalStateException();
        }

        // set the title
        setTitle(title == null ? "" : title);

        // set the icon
        setIcon(icon);

        // create the button panel
        clearButtons();
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
                addButton(buttons[i], null);
            }
            if (buttons.length > 0) {
                setDefaultButton(buttons.length - 1);
            }
        }
        else {
            addButton("btn.Ok", null);
            setDefaultButton(0);
        }

        // create the main view
        Component cs;
        String[] lines = java.util.regex.Pattern.compile("(\\r?\\n)+")
                .split(msg);
        if (lines.length == 1) {
            setContents(cs = new JLabel(msg));
        }
        else {
            Box b = new Box(BoxLayout.Y_AXIS);
            for (int i = 0; i < lines.length; i++) {
                b.add(new JLabel(lines[i]));
            }
            setContents(cs = b);
        }

        // create the detail view and button, if necessary
        if (details != null && details.length() > 0) {
            JTextArea dt = new JTextArea(details);
            dt.setEditable(false);
            dt.setWrapStyleWord(false);
            dt.setFont(Font.decode("monospaced"));
            dt.setBackground(getContentPane().getBackground());

            addButton("btn.ShowDetails", new DetailsListener(cs, dt));
        }

        // show the message and return the result
        pack();
        WindowManager.showCentered(this);
        return getResultCode();
    }

    public static MessageDialog createDialog(Component parent,
            GuiResource rsrc) {
        while (parent != null) {
            if (parent instanceof Dialog) {
                return new MessageDialog((Dialog)parent, rsrc);
            }
            else if (parent instanceof Frame) {
                break;
            }

            parent = parent.getParent();
        }

        return new MessageDialog((Frame)parent, rsrc);
    }

    public static int showMessage(Component parent, GuiResource rsrc,
            LogEntry e) {
        return createDialog(parent, rsrc).showMessage(e);
    }

    private class DetailsListener implements ActionListener {
        private boolean _showingDetails = false;
        private Component _message, _details;

        public DetailsListener(Component msg, Component details) {
            _message = msg;
            _details = details;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            String newName;
            Component newComp;

            if (_showingDetails) {
                newName = "btn.ShowDetails";
                newComp = _message;
            }
            else {
                newName = "btn.HideDetails";
                newComp = _details;
            }
            _showingDetails = !_showingDetails;

            setContents(newComp);

            JButton source = (JButton)evt.getSource();
            getResource().configureButton(source, newName);
            pack();
            WindowManager.centerOnScreen(MessageDialog.this);
            repaint();
        }
    }

}
