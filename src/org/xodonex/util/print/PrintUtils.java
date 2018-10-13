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
package org.xodonex.util.print;

import java.lang.reflect.Field;

import javax.print.attribute.Attribute;

/**
 * @author Henrik Lauritzen
 */
public class PrintUtils {

    private PrintUtils() {
    }

    public static Attribute parseAttribute(String def) {
        try {
            int idx = def.indexOf('=');
            if (idx < 0) {
                return null;
            }

            String clsName = "javax.print.attribute.standard."
                    + def.substring(0, idx);
            def = def.substring(idx + 1);

            idx = def.indexOf('.');
            if (idx >= 0) {
                clsName = clsName + '$' + def.substring(0, idx);
                def = def.substring(idx + 1);
            }

            Class cls = Class.forName(clsName);
            Field f = cls.getField(def);
            return (Attribute)f.get(null);
        }
        catch (Exception e) {
            return null;
        }
    }

}
