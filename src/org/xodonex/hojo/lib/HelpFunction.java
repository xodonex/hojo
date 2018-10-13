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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;

public final class HelpFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    public final static int VIS_PUBLIC = 0;
    public final static int VIS_PROTECTED = 1;
    public final static int VIS_PACKAGE_PRIVATE = 2;
    public final static int VIS_PRIVATE = 3;

    private final static Class[] pTypes = { Class.class, Pattern.class,
            Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE };

    private final static HelpFunction instance = new HelpFunction();

    private HelpFunction() {
    }

    public static HelpFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "cls", "discriminator", "depth", "visibility",
                "required", "excluded" };
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
            return null;
        case 2:
        case 3:
        case 4:
            return ConvertUtils.ZERO_INT;
        case 5:
            return new Integer(Modifier.ABSTRACT);
        default:
            return NO_ARG;
        }
    }

    @Override
    public Class getReturnType() {
        return String.class;
    }

    @Override
    public Object invoke(Object[] arguments) {
        Class c = ConvertUtils.toClass(arguments[0]);
        Pattern discriminator = ConvertUtils.toPattern(arguments[1]);
        int depth = ConvertUtils.toInt(arguments[2]);
        int visibility = ConvertUtils.toInt(arguments[3]);
        int required = ConvertUtils.toInt(arguments[4]);
        int excluded = ConvertUtils.toInt(arguments[5]);
        if (c.isInterface()) {
            excluded &= ~Modifier.ABSTRACT;
        }

        // generate the class signature
        StringBuffer result = ReflectUtils.showSignature(c);

        if (c.isArray() || c.isPrimitive()) {
            return result.append(";\n").toString();
        }
        result.append(" {\n");

        // find all suitable members
        Set[] data = { new HashSet(), new HashSet(), new HashSet() };
        ReflectUtils.listMembers(c, data, depth,
                discriminator == null ? null : discriminator.matcher(""),
                visibility, required, excluded);

        // list the fields in sorted order
        Field[] fs = (Field[])data[0].toArray(new Field[data[0].size()]);
        Arrays.sort(fs, ReflectUtils.FIELD_COMPARATOR);
        if (fs.length > 0) {
            result.append("\n");
        }
        for (int i = 0; i < fs.length; i++) {
            result.append("    ").append(ReflectUtils.showSignature(fs[i]))
                    .append(";\n");
        }

        // list the constructors in sorted order
        Constructor[] cs = (Constructor[])data[1].toArray(
                new Constructor[data[1].size()]);
        Arrays.sort(cs, ReflectUtils.CONSTRUCTOR_COMPARATOR);
        if (cs.length > 0) {
            result.append("\n");
        }
        for (int i = 0; i < cs.length; i++) {
            result.append("    ").append(ReflectUtils.showSignature(cs[i]))
                    .append(" {\n    }\n");
        }

        // list the methods in sorted order
        Method[] ms = (Method[])data[2].toArray(new Method[data[2].size()]);
        Arrays.sort(ms, ReflectUtils.METHOD_COMPARATOR);
        if (ms.length > 0) {
            result.append("\n");
        }
        for (int i = 0; i < ms.length; i++) {
            result.append("    ").append(ReflectUtils.showSignature(ms[i]));
            if ((ms[i].getModifiers() & Modifier.ABSTRACT) != 0) {
                result.append(";\n");
            }
            else {
                result.append(" {\n    }\n");
            }
        }

        return result.append("\n}\n").toString();
    }

}
