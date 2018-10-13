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

import java.util.Collection;

import javax.swing.JOptionPane;

import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.dialog.SelectDialog;

public class SelectFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = {
            Collection.class, Boolean.class, String.class, GuiResource.class
    };
    private final static SelectFunction instance = new SelectFunction();

    public static SelectFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "options", "singleSelection", "dlgTitle",
                "resource" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
            return Boolean.FALSE;
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
        return Object.class;
    }

    @Override
    public Object invoke(Object[] arguments) {
        Collection contents = ConvertUtils.toCollection(arguments[0]);
        boolean single = ConvertUtils.toBool(arguments[1]);
        String title = ConvertUtils.toString(arguments[2]);
        GuiResource rsrc = (GuiResource)arguments[3];

        if (contents == null) {
            return JOptionPane.showInputDialog(
                    rsrc == null ? null : rsrc.getMainFrame(),
                    null, title, JOptionPane.QUESTION_MESSAGE);
        }
        else {
            if (contents.size() == 0) {
                return null;
            }
            else {
                return new SelectDialog(rsrc).select(title, contents, single);
            }
        }
    }

}
