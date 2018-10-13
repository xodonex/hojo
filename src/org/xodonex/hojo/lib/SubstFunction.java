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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public final class SubstFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static SubstFunction INSTANCE = new SubstFunction();

    private SubstFunction() {
    }

    public static SubstFunction getInstance() {
        return INSTANCE;
    }

    @Override
    public Class[] getParameterTypes() {
        return new Class[] { CharSequence.class, Pattern.class, String.class };
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "text", "searchString", "replacement" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        return NO_ARG;
    }

    @Override
    public Class getReturnType() {
        return StringBuffer.class;
    }

    @Override
    public Object invoke(Object[] arguments) throws HojoException {
        StringBuffer buf = new StringBuffer();

        CharSequence cs = ConvertUtils.toCharSequence(arguments[0]);
        Pattern pattern = ConvertUtils.toPattern(arguments[1]);
        String subst = ConvertUtils.toString(arguments[2]);

        Matcher m = pattern.matcher(cs);

        boolean success = m.find();
        while (success) {
            m.appendReplacement(buf, subst);
            success = m.find();
        }
        m.appendTail(buf);

        return buf;
    }

}
