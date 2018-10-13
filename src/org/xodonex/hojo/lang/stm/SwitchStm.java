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
import org.xodonex.hojo.lang.Const;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.UnreachableStatementException;
import org.xodonex.hojo.util.BreakException;
import org.xodonex.util.StringUtils;

public class SwitchStm extends Statement {

    private static final long serialVersionUID = 1L;

    protected Expression expr;
    protected Statement[] blocks;
    protected Expression[] guards;

    public SwitchStm(Expression expr, Statement[] blocks, Expression[] guards) {
        this.expr = expr;
        this.blocks = blocks;
        this.guards = guards;
    }

    @Override
    public Statement optimize(int level) {
        expr = expr.optimize(level);
        for (int i = blocks.length - 1; i >= 0; i--) {
            blocks[i] = blocks[i].optimize(level);
            guards[i] = guards[i].optimize(level);
        }
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        Expression expr_ = expr.linkVars(env, maxLvl);
        boolean modified = expr_ != expr;
        Statement[] blocks_ = new Statement[blocks.length];
        Expression[] guards_ = new Expression[guards.length];

        for (int i = blocks.length - 1; i >= 0; i--) {
            if ((blocks_[i] = blocks[i].linkVars(env, maxLvl)) != blocks[i]) {
                modified = true;
            }
            if ((guards_[i] = guards[i].linkVars(env, maxLvl)) != guards[i]) {
                modified = true;
            }
        }

        if (modified) {
            return new SwitchStm(expr_, blocks_, guards_);
        }
        else {
            return this;
        }
    }

    @Override
    public Object run(Environment env) throws Throwable {
        boolean isMatched = false;
        Object cmp = expr.xeq(env);
        Object guard = null;

        try {
            for (int i = 0; i < blocks.length; i++) {
                // try to match the guard, if one has not been matched already
                if (!isMatched) {
                    // the default guard always matches
                    if (guards[i] != Const.DEFAULT) {
                        guard = guards[i].xeq(env);
                        if (!HojoLib.eq(cmp, guard)) {
                            continue;
                        }
                    }
                    isMatched = true;
                }
                // run the block and continue until a break is encountered
                if (isMatched) {
                    blocks[i].run(env);
                }
            }
        }
        catch (BreakException e) {
        }
        return null;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer sb = new StringBuffer(indent);
        String indent2 = indent + fmt.getIndent();

        sb.append(stx.reserved[RES_SWITCH - RES_BASE_ID]).append(' ');
        sb.append(stx.punctuators[PCT_IDX_LPAREN]);
        sb.append(expr.toString(stx, fmt, ""));
        sb.append(stx.punctuators[PCT_IDX_RPAREN]).append(' ');
        sb.append(stx.punctuators[PCT_IDX_BLOCKSTART]).append('\n');
        for (int i = 0; i < blocks.length; i++) {
            sb.append(indent);
            if (guards[i] == Const.DEFAULT) {
                sb.append(stx.reserved[RES_DEFAULT - RES_BASE_ID]);
                sb.append(stx.punctuators[PCT_IDX_CASELABEL]);
            }
            else {
                sb.append(indent).append(stx.reserved[RES_CASE - RES_BASE_ID]);
                sb.append(' ').append(guards[i].toString(stx, fmt, ""));
                sb.append(stx.punctuators[PCT_IDX_CASELABEL]);
            }
            sb.append('\n').append(blocks[i].toString(stx, fmt, indent2))
                    .append('\n');
        }
        sb.append(indent).append(stx.punctuators[PCT_IDX_BLOCKEND]);
        return sb.toString();
    }

    @Override
    public Type checkCode(Type rt) throws UnreachableStatementException {
        for (int i = 0; i < blocks.length; i++) {
            rt = blocks[i].checkCode(rt);
        }
        return rt;
    }

}
