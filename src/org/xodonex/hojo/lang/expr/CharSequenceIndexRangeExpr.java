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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ConvertUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class CharSequenceIndexRangeExpr extends IndexRangeExpr {

    private static final long serialVersionUID = 1L;

    public CharSequenceIndexRangeExpr(Expression s, Expression lo,
            Expression hi) {
        super(s, lo, hi);
    }

    @Override
    public Object xeq(Environment env) {
        CharSequence cs = ConvertUtils.toCharSequence(e1.xeq(env));
        int lo = ConvertUtils.toInt(e2.xeq(env));
        int hi = ConvertUtils.toInt(e3.xeq(env));
        return (lo > hi) ? cs.subSequence(lo, cs.length())
                : cs.subSequence(lo, hi);
    }

    @Override
    public Class getTypeC() {
        return CharSequence.class;
    }

    @Override
    protected Type getType0() {
        return HojoLib.CHAR_SEQUENCE_TYPE;
    }

}
