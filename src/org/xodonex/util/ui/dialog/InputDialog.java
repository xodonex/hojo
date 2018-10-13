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
import java.awt.Frame;

import javax.swing.Icon;

import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.WindowManager;

/**
 * A modal dialog which displays a component.
 *
 * @author Henrik Lauritzen
 */
public class InputDialog extends GenericDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InputDialog() {
        super();
    }

    public InputDialog(GuiResource rsrc) {
        super(rsrc);
    }

    public InputDialog(Dialog owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public InputDialog(Frame owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public final int show(Component c) {
        return show(c, null, null);
    }

    public final int show(Component c, String[] buttons) {
        return show(c, buttons, null);
    }

    public int show(Component c, String[] buttons, Icon icon) {
        if (isVisible()) {
            throw new IllegalStateException();
        }

        // set the icon
        setIcon(icon);

        // create the button panel
        clearButtons();
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
                addButton(buttons[i], null);
            }
            if (buttons.length > 0) {
                setDefaultButton(0);
            }
        }
        else {
            addButton("btn.Ok", null);
            addButton("btn.Cancel", null);
            setDefaultButton(0);
        }

        // set the contents
        setContents(c);

        // show the dialog and return the result
        pack();
        WindowManager.showCentered(this);
        return getResultCode();
    }

    public static InputDialog createDialog(Component parent, GuiResource rsrc) {
        while (parent != null) {
            if (parent instanceof Dialog) {
                return new InputDialog((Dialog)parent, rsrc);
            }
            else if (parent instanceof Frame) {
                break;
            }

            parent = parent.getParent();
        }

        return new InputDialog((Frame)parent, rsrc);
    }

}
