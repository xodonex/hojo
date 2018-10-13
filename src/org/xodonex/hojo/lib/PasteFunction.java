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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public final class PasteFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static PasteFunction INSTANCE = new PasteFunction();
    private final static Class[] P_TYPES = { String.class, Class.class };

    private PasteFunction() {
    }

    public static PasteFunction getInstance() {
        return INSTANCE;
    }

    @Override
    public Class[] getParameterTypes() {
        return P_TYPES;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "MIMEtype", "reprClass" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 0:
        case 1:
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
    public Object invoke(Object[] arguments) throws HojoException {
        String mimeType = ConvertUtils.toString(arguments[0]);
        Class reprClass = ConvertUtils.toClass(arguments[1]);
        if (reprClass == Object.class) {
            reprClass = null;
        }
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard()
                .getContents(null);

        if (t == null) {
            // no contents
            return null;
        }
        try {
            // get the available data flavors
            DataFlavor[] flavors = t.getTransferDataFlavors();

            // find the flavors having a suitable MIME type
            List options = new ArrayList();
            for (int i = 0; i < flavors.length; i++) {
                if (mimeType == null) {
                    if (reprClass == null && flavors[i].isMimeTypeEqual(
                            "application/x-java-serialized-object")) {
                        // prefer serialized objects when no specific mimetype
                        // and representation class is given
                        options.clear();
                        options.add(flavors[i]);
                        break;
                    }
                    // else include all flavors
                    options.add(flavors[i]);
                }
                else {
                    if (flavors[i].isMimeTypeEqual(mimeType)) {
                        options.add(flavors[i]);
                    }
                }
            }

            // find the best flavor based on the preferred representation class
            DataFlavor flavor = null;
            for (Iterator i = options.iterator(); i.hasNext();) {
                DataFlavor tmp = (DataFlavor)i.next();
                if (reprClass == null
                        || reprClass == tmp.getRepresentationClass()) {
                    // perfect match
                    flavor = tmp;
                    break;
                }
            }

            if (flavor == null) {
                // no suitable flavor was found
                return null;
            }
            else {
                // return the data
                return t.getTransferData(flavor);
            }
        }
        catch (Exception e) {
            throw HojoException.wrap(e);
        }
    }

}
