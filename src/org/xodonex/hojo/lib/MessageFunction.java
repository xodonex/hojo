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
package org.xodonex.hojo.lib;

import javax.swing.JOptionPane;

import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ui.ButtonConstants;
import org.xodonex.util.ui.DialogUtils;
import org.xodonex.util.ui.GuiResource;

public class MessageFunction extends StandardFunction
        implements ButtonConstants {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = {
            String.class, Integer.TYPE, String.class, GuiResource.class
    };
    private final static MessageFunction instance = new MessageFunction();

    public static MessageFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "message", "buttons", "dlgTitle", "resource" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
            return new Integer(BTN_OK);
        case 2:
            return "";
        case 3:
            return null;
        default:
            return NO_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        return Integer.TYPE;
    }

    @Override
    public Object invoke(Object[] arguments) {
        String contents = ConvertUtils.toString(arguments[0]);
        int buttons = ConvertUtils.toInt(arguments[1]);
        String title = ConvertUtils.toString(arguments[2]);
        GuiResource rsrc = (GuiResource)arguments[3];

        JOptionPane p = DialogUtils.buildDialog(JOptionPane.INFORMATION_MESSAGE,
                buttons, null, contents, rsrc);
        return new Integer(DialogUtils.showDialog(
                p, rsrc == null ? null : rsrc.getMainFrame(), title));
    }

}
