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
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.UnreachableStatementException;
import org.xodonex.util.StringUtils;

public class BlockStatement extends Statement {

    private static final long serialVersionUID = 1L;

    protected Statement[] stms;

    public BlockStatement(Statement[] stms) {
        this.stms = stms;
    }

    @Override
    public Statement optimize(int level) {
        for (int i = stms.length - 1; i >= 0; i--) {
            stms[i] = stms[i].optimize(level);
        }
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        Statement[] stms_ = new Statement[stms.length];
        boolean modified = false;

        for (int i = stms.length - 1; i >= 0; i--) {
            if ((stms_[i] = stms[i].linkVars(env, maxLvl)) != stms[i]) {
                modified = true;
            }
        }

        if (modified) {
            BlockStatement result = (BlockStatement)clone();
            result.stms = stms_;
            return result;
        }
        else {
            return this;
        }
    }

    @Override
    public Object run(Environment env) throws Throwable {
        Object result = null;
        for (int i = 0; i < stms.length; i++) {
            result = stms[i].run(env);
        }
        return result;
    }

    public StringBuffer toString(StringBuffer sb, HojoSyntax stx,
            StringUtils.Format fmt, String indent) {
        for (int i = 0; i < stms.length; i++) {
            sb.append(stms[i].toString(stx, fmt, indent));
            if (i < stms.length - 1) {
                sb.append('\n');
            }
        }
        return sb;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return toString(new StringBuffer(), stx, fmt, indent).toString();
    }

    @Override
    public Type checkCode(Type rt) throws UnreachableStatementException {
        return checkBlock(stms, rt);
    }

}
