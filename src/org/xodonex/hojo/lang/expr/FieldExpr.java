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
package org.xodonex.hojo.lang.expr;

import java.lang.reflect.Field;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.LValue;

/**
 *
 * @author Henrik Lauritzen
 */
public class FieldExpr extends FinalFieldExpr implements LValue {

    private static final long serialVersionUID = 1L;

    public FieldExpr(Expression base, Field f) {
        super(base, f);
    }

    @Override
    public Object resolve(Environment env) {
        return base.xeq(env);
    }

    @Override
    public Object get(Object resolvent) {
        try {
            return f.get(resolvent);
        }
        catch (Exception e) {
            throw HojoException.wrap(e);
        }
    }

    @Override
    public Object set(Object resolvent, Object value) {
        try {
            f.set(resolvent, value);
            return value;
        }
        catch (Exception e) {
            throw HojoException.wrap(e);
        }
    }

}
