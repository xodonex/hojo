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
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.UnreachableStatementException;
import org.xodonex.hojo.lang.env.DummyEnv;
import org.xodonex.hojo.lang.env.Env;
import org.xodonex.hojo.util.BreakException;
import org.xodonex.hojo.util.ContinueException;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;

public class ForStm extends Statement {

    private static final long serialVersionUID = 1L;

    protected Statement init;
    protected Expression cond;
    protected Statement update;
    protected Statement[] body;
    protected short envSize;

    public ForStm(Statement init, Expression cond, Statement update,
            Statement[] body, short size) {
        this.init = init;
        this.cond = cond;
        this.update = update;
        this.body = body;
        this.envSize = size;
    }

    @Override
    public Statement optimize(int level) {
        if (init != null) {
            init = init.optimize(level);
        }
        if (cond != null) {
            cond = cond.optimize(level);
        }
        if (update != null) {
            update = update.optimize(level);
        }
        for (int i = body.length - 1; i >= 0; i--) {
            body[i] = body[i].optimize(level);
        }
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        boolean modified = false;

        Environment env2 = new DummyEnv(env);
        Statement init_ = (init == null) ? null : init.linkVars(env2, maxLvl);
        Expression cond_ = (cond == null) ? null : cond.linkVars(env2, maxLvl);
        Statement update_ = (update == null) ? null
                : update.linkVars(env2, maxLvl);

        Statement[] body_ = new Statement[body.length];
        for (int i = body.length - 1; i >= 0; i--) {
            if ((body_[i] = body[i].linkVars(env2, maxLvl)) != body[i]) {
                modified = true;
            }
        }

        if (modified || init_ != init || cond_ != cond || update_ != update) {
            return new ForStm(init_, cond_, update_, body_, envSize);
        }
        else {
            return this;
        }
    }

    @Override
    public Object run(Environment env) throws Throwable {
        Environment env2 = new Env(env, envSize);

        if (init != null) {
            init.run(env2);
        }

        Thread t = Thread.currentThread();
        try {
            while (cond == null || ConvertUtils.toBool(cond.xeq(env2))) {
                try {
                    for (int i = 0; i < body.length; i++) {
                        body[i].run(env2);
                    }
                }
                catch (ContinueException e) {
                }

                if (t.isInterrupted()) {
                    throw new InterruptedException();
                }
                if (update != null) {
                    update.run(env2);
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
        StringBuffer result = new StringBuffer(indent);
        result.append(stx.reserved[RES_FOR - RES_BASE_ID]).append(' ');
        result.append(stx.punctuators[PCT_IDX_LPAREN]);

        if (init != null) {
            result.append(init.toString(stx, fmt, ""));
        }
        else {
            result.append(stx.punctuators[PCT_IDX_SEPARATOR]);
        }
        if (cond != null) {
            result.append(cond.toString(stx, fmt, ""));
        }
        result.append(stx.punctuators[PCT_IDX_SEPARATOR]);

        if (update != null) {
            result.append(update.toString(stx, fmt, ""));
        }
        else {
            result.append(stx.punctuators[PCT_IDX_SEPARATOR]);
        }

        result.append(stx.punctuators[PCT_IDX_RPAREN]).append(' ');
        result.append(stx.punctuators[PCT_IDX_BLOCKSTART]).append('\n');

        String indent2 = indent + fmt.getIndent();
        for (int i = 0; i < body.length; i++) {
            result.append(body[i].toString(stx, fmt, indent2)).append('\n');
        }

        result.append(indent).append(stx.punctuators[PCT_IDX_BLOCKEND]);
        return result.toString();
    }

    @Override
    public Type checkCode(Type rt) throws UnreachableStatementException {
        return checkBlock(body, rt);
    }

}
