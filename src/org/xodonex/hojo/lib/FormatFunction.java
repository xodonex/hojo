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
import java.util.Iterator;

import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;
import org.xodonex.util.struct.iterator.ArrayIterator;

public final class FormatFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = new Class[] {
            Object.class, String.class, String.class };

    private StringUtils.Format fmt;

    public FormatFunction(StringUtils.Format fmt) {
        if (fmt == null) {
            throw new NullPointerException();
        }
        this.fmt = fmt;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "value", "delimiter", "indent" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
            return "\n";
        case 2:
            return "";
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
        Object obj = arguments[0];
        String delim = ConvertUtils.toString(arguments[1]);
        String indent = ConvertUtils.toString(arguments[2]);
        Iterator it = null;

        if (obj instanceof Collection) {
            it = ((Collection)obj).iterator();
        }
        else if (obj instanceof Iterator) {
            it = (Iterator)obj;
        }
        else if (obj instanceof Object[]) {
            it = new ArrayIterator((Object[])obj);
        }

        StringBuffer buf;
        if (it != null) {
            buf = new StringBuffer();
            while (it.hasNext()) {
                buf.append(StringUtils.any2String(it.next(), fmt, indent))
                        .append(delim);
            }
        }
        else {
            buf = new StringBuffer(
                    StringUtils.any2String(obj, fmt, indent) + delim);
        }

        return buf;
    }

}
