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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.env.DummyEnv;
import org.xodonex.hojo.lang.env.Env;
import org.xodonex.util.StringUtils;

public class CatchClause extends BlockStatement {

    private static final long serialVersionUID = 1L;

    protected Class matchClass;
    protected short size;
    protected String matchName;

    public CatchClause(Statement[] stms, short size, Class matchClass,
            String matchName) {
        super(stms);
        this.size = size;
        this.matchClass = matchClass;
        this.matchName = matchName;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        return super.linkVars(new DummyEnv(env), maxLvl);
    }

    public boolean isCatch(Throwable t) {
        return matchClass.isAssignableFrom(t.getClass());
    }

    public Object run(Environment base, Throwable matched) throws Throwable {
        Environment newEnv = new Env(base, size);
        newEnv.alloc((short)0, true, matchName, HojoLib.typeOf(matchClass),
                matched);
        return run(newEnv);
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        result.append(stx.reserved[RES_CATCH - RES_BASE_ID]).append(' ');
        result.append(stx.punctuators[PCT_IDX_LPAREN]);
        result.append(matchClass.getName()).append(' ').append(matchName);
        result.append(stx.punctuators[PCT_IDX_RPAREN]).append(' ');
        result.append(stx.punctuators[PCT_IDX_BLOCKSTART]).append('\n');

        String blockIndent = indent + fmt.getIndent();
        toString(result, stx, fmt, blockIndent);
        result.append('\n').append(indent)
                .append(stx.punctuators[PCT_IDX_BLOCKEND]);
        return result.append('\n').toString();
    }

}
