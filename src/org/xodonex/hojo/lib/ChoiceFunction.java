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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.dialog.ChoiceDialog;

public class ChoiceFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = { Collection.class, String[].class,
            boolean[].class, String.class,
            GuiResource.class };
    private final static ChoiceFunction instance = new ChoiceFunction();

    public static ChoiceFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public Class getReturnType() {
        return Object.class;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "options", "labels", "default", "dlgTitle",
                "resource" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
        case 2:
            return null;
        case 3:
            return "";
        case 4:
            return null;
        default:
            return NO_ARG;
        }
    }

    @Override
    public Object invoke(Object[] arguments) {
        Collection contents = ConvertUtils.toCollection(arguments[0]);
        String[] titles = (String[])HojoLib.toArray(
                arguments[1], String[].class, String.class, true);
        boolean[] selected = (boolean[])HojoLib.toArray(
                arguments[2], boolean[].class, Boolean.TYPE, true);
        String title = ConvertUtils.toString(arguments[3]);
        GuiResource rsrc = (GuiResource)arguments[4];

        if (contents.size() == 0) {
            return null;
        }
        else {
            return new ChoiceDialog(rsrc).choose(title, contents, selected,
                    titles);
        }
    }
}
