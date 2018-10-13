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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public final class CopyFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static CopyFunction INSTANCE = new CopyFunction();

    private final ClipboardOwner CLIP_OWNER = new ClipboardOwner() {
        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
        }
    };

    private CopyFunction() {
    }

    public static CopyFunction getInstance() {
        return INSTANCE;
    }

    @Override
    public Class[] getParameterTypes() {
        return STRING_ARG;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "value" };
    }

    @Override
    public Class getReturnType() {
        return Void.TYPE;
    }

    @Override
    public Object invoke(Object[] arguments) throws HojoException {
        String s = ConvertUtils.toString(arguments[0]);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(s == null ? "" : s), CLIP_OWNER);
        return null;
    }

}
