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
package org.xodonex.hojo;

import org.xodonex.hojo.lang.Const;
import org.xodonex.hojo.lang.Function;
import org.xodonex.util.ArrayUtils;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

/**
 * Abstract base class for implementing {@link Function} classes, which are the
 * basis for realizing functions-as-values in the Hojo language.
 */
public abstract class StandardFunction
        implements Function, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public Class[] STRING_ARG = { String.class };
    public Class[] NUMBER_ARG = { Number.class };
    public Class[] NUMBER_ARGS = { Number.class, Number.class };
    public Class[] BOOLEAN_ARG = { Boolean.class };
    public Class[] BOOLEAN_ARGS = { Boolean.class, Boolean.class };
    public Class[] LISTFUNC_ARGS = { Function.class, java.util.Iterator.class };

    protected StandardFunction() {
    }

    @Override
    public abstract Class[] getParameterTypes();

    @Override
    public String[] getParameterNames() {
        return null;
    }

    @Override
    public abstract Class getReturnType();

    @Override
    public int getArity() {
        return getParameterTypes().length;
    }

    @Override
    public String getExtraParameterName() {
        return null;
    }

    @Override
    public Object getDefaultValue(int arg) {
        return NO_ARG;
    }

    @Override
    public Object[] validateArgs(Object[] args) {
        Class[] argTypes = getParameterTypes();
        int arity = argTypes.length;
        boolean allowExtra = getExtraParameterName() != null;
        int len = arity + (allowExtra ? 1 : 0);
        Object noArgIndicator = NO_ARG;

        if (args == null) {
            // create a default argument list
            args = new Object[len];
            if (allowExtra) {
                args[arity] = UNIT;
            }
            // force getDefaultValue() to be called in the loop below
            noArgIndicator = null;
        }
        else if (args.length < arity) {
            int l = args.length;
            args = ArrayUtils.enlarge(args, len - args.length);
            for (; l < arity; l++) {
                args[l] = NO_ARG;
            }
            if (allowExtra) {
                args[arity] = UNIT;
            }
        }
        else if (allowExtra) {
            // wrap additional arguments into one parameter, if extra parameters
            // are
            // allowed
            if (args.length == arity) {
                args = ArrayUtils.enlarge(args, 1);
                args[arity] = UNIT;
            }
            else {
                args[arity] = arity == 0 ? args.clone()
                        : ArrayUtils.removeRange(args, 0, arity);
            }
        }
        else if (args.length > arity) {
            throw new HojoException(null, HojoException.ERR_RUNTIME_ARG_COUNT,
                    new String[] {
                            "" + arity, "" + args.length },
                    null);
        }

        // fill in default arguments, if necessary
        for (int i = 0; i < arity; i++) {
            Object arg = args[i];
            if (arg == noArgIndicator) {
                if ((arg = getDefaultValue(i)) == NO_ARG) {
                    String[] pnames = getParameterNames();
                    throw new HojoException(null,
                            HojoException.ERR_RUNTIME_ARG_MISSING,
                            new String[] {
                                    "" + i,
                                    pnames == null ? "" : pnames[i]
                            }, null);
                    // args[i] = ReflectUtils.getDefaultValue(argTypes[i]);
                }
                else {
                    args[i] = arg;
                }
            }
        }

        return args;
    }

    @Override
    public void run() {
        invoke(validateArgs(null));
    }

    @Override
    public abstract Object invoke(Object[] arguments) throws HojoException;

    @Override
    public String toString() {
        return toString(HojoSyntax.DEFAULT, StringUtils.defaultFormat);
    }

    public final String toStandardString() {
        return toStandardString(HojoSyntax.DEFAULT, StringUtils.defaultFormat);
    }

    public String toString(HojoSyntax stx, StringUtils.Format fmt) {
        return toStandardString(stx, fmt);
    }

    private String toStandardString(HojoSyntax stx, StringUtils.Format fmt) {
        StringBuffer result = new StringBuffer(
                stx.reserved[HojoConst.RES_LAMBDA -
                        HojoConst.RES_BASE_ID]);
        result.append(stx.punctuators[HojoConst.PCT_IDX_LPAREN]);

        Class[] paramTypes = getParameterTypes();
        String[] paramNames = getParameterNames();

        for (int i = 0; i < paramTypes.length; i++) {
            result.append(ReflectUtils.className2Java(paramTypes[i]));
            if (paramNames != null) {
                result.append(' ').append(paramNames[i]);
            }
            Object val = getDefaultValue(i);
            if (val != NO_ARG) {
                result.append(' ')
                        .append(stx.operators[HojoConst.OP_IDX_ASSIGN])
                        .append(' ');
                result.append(Const.toString(val, stx, fmt));
            }
            if (i < paramTypes.length - 1) {
                result.append(stx.punctuators[HojoConst.PCT_IDX_DELIMITER])
                        .append(' ');
            }
        }
        String ename = getExtraParameterName();
        if (ename != null) {
            if (paramTypes.length > 0) {
                result.append(stx.punctuators[HojoConst.PCT_IDX_DELIMITER])
                        .append(' ');
            }
            result.append('*').append(' ').append(ename);
        }

        result.append(stx.punctuators[HojoConst.PCT_IDX_RPAREN]).append(' ');
        result.append(stx.operators[HojoConst.OP_IDX_FUNC]).append(' ');
        result.append(ReflectUtils.className2Java(getReturnType()));
        return result.toString();
    }

}
