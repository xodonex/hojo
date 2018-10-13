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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public final class GrepFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static GrepFunction INSTANCE = new GrepFunction();

    private GrepFunction() {
    }

    public static GrepFunction getInstance() {
        return INSTANCE;
    }

    @Override
    public Class[] getParameterTypes() {
        return new Class[] { CharSequence.class, Pattern.class,
                Integer.class, Boolean.TYPE };
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "searchString", "pattern", "offset", "findAll" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 2:
            return ConvertUtils.ZERO_INT;
        case 3:
            return Boolean.TRUE;
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
        CharSequence cs = ConvertUtils.toCharSequence(arguments[0]);
        Pattern pattern = ConvertUtils.toPattern(arguments[1]);
        int index = ConvertUtils.toInt(arguments[2]);
        boolean findAll = ConvertUtils.toBool(arguments[3]);

        if (cs == null || index >= cs.length()) {
            return findAll ? Collections.EMPTY_LIST : null;
        }

        Matcher m = pattern.matcher(cs);
        List result = new ArrayList(findAll ? 4 : 1);

        if (m.find(index)) {
            result.add(m.group());
            if (!findAll) {
                return result.get(0);
            }
        }

        while (m.find()) {
            result.add(m.group());
        }

        return findAll ? result : null;
    }

}
