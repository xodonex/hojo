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

import java.io.File;

import javax.swing.JFileChooser;

import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;

public final class FchooseFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    public final static int SELECT_ONE = 0;
    public final static int SELECT_MANY = 1;
    public final static int SELECT_DIRS = 2;

    private final static Class[] pTypes = { File.class, String.class,
            Integer.class };
    private final static FchooseFunction instance = new FchooseFunction();

    private JFileChooser fchoose;

    private FchooseFunction() {
    }

    public static FchooseFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "dir", "dlgTitle", "flags" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 0:
        case 1:
            return null;
        case 2:
            return new Integer(SELECT_ONE);
        default:
            return NO_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        return File[].class;
    }

    @Override
    public Object invoke(Object[] arguments) {
        File dir = ConvertUtils.toFile(arguments[0]);
        String ttl = ConvertUtils.toString(arguments[1]);
        int flags = ConvertUtils.toInt(arguments[2]);

        JFileChooser fc = getChooser();
        if (dir != null) {
            fc.setCurrentDirectory(dir);
        }
        fc.setDialogTitle(ttl == null ? "" : ttl);
        fc.setFileSelectionMode((flags & SELECT_DIRS) == SELECT_DIRS
                ? JFileChooser.DIRECTORIES_ONLY
                : JFileChooser.FILES_AND_DIRECTORIES);
        boolean multiSelect = (flags & SELECT_MANY) == SELECT_MANY;
        fc.setMultiSelectionEnabled(multiSelect);

        if (fc.showDialog(null, null) == JFileChooser.APPROVE_OPTION) {
            return (multiSelect) ? fc.getSelectedFiles()
                    : new File[] { fc.getSelectedFile() };
        }
        else {
            return null;
        }
    }

    private JFileChooser getChooser() {
        if (fchoose == null) {
            fchoose = new JFileChooser();
            fchoose.setApproveButtonText("Ok");
        }
        return fchoose;
    }
}
