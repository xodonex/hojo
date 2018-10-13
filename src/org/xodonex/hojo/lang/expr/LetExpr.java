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

import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.env.DummyEnv;
import org.xodonex.hojo.lang.env.Env;
import org.xodonex.hojo.lang.stm.BlockStatement;
import org.xodonex.hojo.lang.stm.NOP;
import org.xodonex.hojo.util.ReturnException;
import org.xodonex.util.StringUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class LetExpr extends Expression {

    private static final long serialVersionUID = 1L;

    protected short size;
    protected Type retType;
    protected Statement block;

    public LetExpr(short size, Type retType, Statement[] block) {
        this(size, retType, (block.length == 0) ? NOP.NOP
                : (block.length == 1) ? block[0] : new BlockStatement(block));
    }

    protected LetExpr(short size, Type retType, Statement block) {
        this.size = size;
        this.retType = retType;
        this.block = block;
    }

    @Override
    public Expression optimize(int level) {
        block = block.optimize(level);
        return this;
    }

    @Override
    public Expression linkVars(Environment env, short maxLvl) {
        Statement block_ = block.linkVars(new DummyEnv(env), maxLvl);
        if (block_ != block) {
            return new LetExpr(size, retType, block_);
        }
        else {
            return this;
        }
    }

    @Override
    public Object xeq(Environment env) {
        Env env2 = new Env(env, size);
        try {
            block.xeq(env2);
        }
        catch (ReturnException e) {
            return retType.typeCast(e.getValue());
        }
        return null; // return without value
    }

    @Override
    public boolean isJavaStatement() {
        return getType0().kind() == Type.TYP_VOID;
    }

    @Override
    protected Type getType0() {
        return retType;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);

        result.append(stx.punctuators[PCT_IDX_LPAREN]);
        result.append(stx.reserved[RES_LET - RES_BASE_ID]).append(' ');
        result.append(stx.punctuators[PCT_IDX_BLOCKSTART]).append('\n');
        result.append(block.toString(stx, fmt, indent + fmt.getIndent()))
                .append('\n');
        result.append(indent).append(stx.punctuators[PCT_IDX_BLOCKEND]);
        result.append(stx.punctuators[PCT_IDX_RPAREN]);
        return result.toString();
    }

}
