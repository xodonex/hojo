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

import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.dialog.TextDialog;

public class EditFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = { StringBuffer.class, String.class,
            GuiResource.class };

    private final static EditFunction instance = new EditFunction();

    public static EditFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "editBuffer", "dlgTitle", "resource" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
            return "";
        case 2:
            return null;
        default:
            return NO_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        return StringBuffer.class;
    }

    @Override
    public Object invoke(Object[] arguments) {
        Object tmp = arguments[0];
        StringBuffer result = ((tmp == null) || (tmp instanceof StringBuffer)
                ? (StringBuffer)tmp
                : new StringBuffer(ConvertUtils.toString(tmp)));
        String title = ConvertUtils.toString(arguments[1]);
        GuiResource rsrc = (GuiResource)arguments[2];
        return new TextDialog(rsrc).edit(result, title);
    }

}
