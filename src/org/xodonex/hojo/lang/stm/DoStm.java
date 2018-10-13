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
package org.xodonex.hojo.lang.stm;

import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.util.BreakException;
import org.xodonex.hojo.util.ContinueException;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;

public class DoStm extends ExprBlockStm {

    private static final long serialVersionUID = 1L;

    public DoStm(Expression expr, Statement block) {
        super(expr, block);
    }

    @Override
    public Object run(Environment env) throws Throwable {
        Object result = null;
        Thread t = Thread.currentThread();
        try {
            do {
                try {
                    result = block.run(env);
                    if (t.isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
                catch (ContinueException e) {
                    continue;
                }
            } while (ConvertUtils.toBool(expr.xeq(env)));
        }
        catch (BreakException e) {
        }
        return result;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer sb = toString(stx.reserved[RES_DO - RES_BASE_ID], false,
                stx, fmt, indent);
        sb.append(' ').append(stx.reserved[RES_WHILE - RES_BASE_ID])
                .append(' ');
        sb.append(stx.punctuators[PCT_IDX_LPAREN]);
        sb.append(expr.toString(stx, fmt, indent));
        sb.append(stx.punctuators[PCT_IDX_RPAREN]);
        return sb.toString();
    }

}
