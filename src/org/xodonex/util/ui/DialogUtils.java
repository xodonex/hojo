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
package org.xodonex.util.ui;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Utilities for dialog boxes.
 */
public class DialogUtils implements ButtonConstants {

    // pre-calculate log(BTN_FAC)
    private final static double BTN_FAC_LOG = Math.log(BTN_FAC);

    // property name lookup table
    private final static String[] NAMES = {
            null,
            "btn.Close", "btn.Cancel", "btn.Ok", "btn.No", "btn.Yes",
            "btn.YesToAll"
    };

    // map a JOptionPane to the int[] instance defining the translation of
    // the selected button index into a BTN_xxx code.
    private static Map _translateTbl = Collections
            .synchronizedMap(new WeakHashMap());

    /**
     * A generic method to show a dialog previously built by
     * {@link #buildDialog}
     *
     * @param pane
     *            the JOptionPane defining the dialog to be shown
     * @param title
     *            the title of the dialog
     * @param parent
     *            the (optional) parent frame of the dialog
     * @return the code for the button which was used to dismiss the dialog.
     *         This will be one of the BTN_xxx constants, or BTN_CUSTOM + i if
     *         the button used was the ith custom button. If the dialog was
     *         dismissed without using a button, the return value will be
     *         BTN_CLOSE.
     */
    public static int showDialog(JOptionPane pane, JFrame parent,
            String title) {
        // show the dialog
        JDialog dlg = pane.createDialog(parent, title);
        pane.selectInitialValue();
        dlg.setVisible(true);
        dlg.dispose();

        // get the selected value
        Object selectedValue = pane.getValue();
        if (selectedValue == null) {
            // dismissed - return BTN_CLOSED.
            return BTN_CLOSE;
        }

        // get the selected button to button code translation
        Object[] cfg = (Object[])_translateTbl.get(pane);
        if (cfg == null) {
            // the dialog was not created by buildDialog() - return the
            // JOptionPane default

            // If there is not an array of option buttons:
            if (selectedValue instanceof Integer) {
                return ((Integer)selectedValue).intValue();
            }
            else {
                return JOptionPane.CLOSED_OPTION;
            }
        }

        // retreive the translation and decode the result
        Object[] choices = (Object[])cfg[0];
        int[] xlat = (int[])cfg[1];

        for (int i = 0; i < choices.length; i++) {
            if (choices[i].equals(selectedValue)) {
                return xlat[i];
            }
        }
        return BTN_CLOSE;
    }

    /**
     * Generic method to build a dialog.
     *
     * @param type
     *            the type of icon (one of the xxx_MESSAGE constants in
     *            {@link JOptionPane}).
     * @param buttons
     *            a button selection code. This should be one of the BTN_xxx
     *            constants.
     * @param customButtons
     *            this optional parameter should hold the texts of the custom
     *            buttons, if any, which are specified in the buttons parameter.
     * @param message
     *            the message to be shown in the dialog
     * @param rsrc
     *            the GUI resource used to create the standard buttons.
     * @return the generated dialog
     */
    public static JOptionPane buildDialog(int type, int buttons,
            String[] customButtons, String message, GuiResource rsrc) {
        if (buttons <= BTN_FAC) {
            throw new IllegalArgumentException();
        }
        if (rsrc == null) {
            rsrc = GuiResource.getDefaultInstance();
        }

        // determine the number of buttons specified and the default button
        int defaultButton = buttons % BTN_FAC;
        buttons /= BTN_FAC;
        int size = buttons == 1 ? 1
                : (int)Math.ceil(Math.log(buttons) / BTN_FAC_LOG);

        // create the button descriptions and the translation
        int[] xlat = new int[size];
        String[] btns = new String[size];
        Object def = null;

        for (int i = size - 1; i >= 0; i--) {
            // decode the next button code
            int btn = buttons % BTN_FAC;
            buttons /= BTN_FAC;
            if (btn == 0) {
                throw new IllegalArgumentException();
            }

            // save the button text
            if (btn >= BTN_FAC) {
                btns[i] = customButtons[btn - BTN_FAC];
            }
            else {
                btns[i] = rsrc.getSimpleString(NAMES[btn]);
            }

            if (btn == defaultButton) {
                // save the default button
                def = btns[i];
            }

            // store the translation
            xlat[i] = btn * BTN_FAC;
        }

        // create the dialog
        JOptionPane p = new JOptionPane(message, type, 0, null, btns, def);

        // store the translation for use later.
        _translateTbl.put(p, new Object[] { btns, xlat });

        return p;
    }

    public static int showPlainDialog(
            String message, int buttons, GuiResource rsrc) {
        return showDialog(buildDialog(JOptionPane.PLAIN_MESSAGE, buttons, null,
                message, rsrc), rsrc.getMainFrame(),
                rsrc.getString("ttl.Message"));
    }

    public static int showMessageDialog(String message,
            int buttons, GuiResource rsrc) {
        return showDialog(buildDialog(JOptionPane.INFORMATION_MESSAGE, buttons,
                null, message, rsrc), rsrc.getMainFrame(),
                rsrc.getString("ttl.Message"));
    }

    public static int showConfirmationDialog(String message,
            int buttons, GuiResource rsrc) {
        return showDialog(buildDialog(JOptionPane.QUESTION_MESSAGE, buttons,
                null, MessageFormat.format(rsrc.getString("msg.Confirm"),
                        new Object[] { message }),
                rsrc),
                rsrc.getMainFrame(), rsrc.getString("ttl.Question"));
    }

    public static int showWarningDialog(String message,
            int buttons, GuiResource rsrc) {
        return showDialog(buildDialog(JOptionPane.WARNING_MESSAGE, buttons,
                null, message, rsrc), rsrc.getMainFrame(),
                rsrc.getString("ttl.Warning"));
    }

    public static int showErrorDialog(String message,
            int buttons, GuiResource rsrc) {
        return showDialog(buildDialog(JOptionPane.ERROR_MESSAGE, buttons,
                null, message, rsrc), rsrc.getMainFrame(),
                rsrc.getString("ttl.Error"));
    }

    private DialogUtils() {
    }

}
