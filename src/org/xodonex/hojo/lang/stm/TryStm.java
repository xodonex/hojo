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
import org.xodonex.hojo.util.FlowException;
import org.xodonex.util.StringUtils;

public class TryStm extends Statement {

    private static final long serialVersionUID = 1L;

    protected Statement tryBlock;
    protected CatchClause[] catchBlock;
    protected Statement finallyBlock;

    public TryStm(Statement tryBlock, CatchClause[] catchBlock,
            Statement finallyBlock) {
        this.tryBlock = tryBlock;
        this.catchBlock = catchBlock;
        this.finallyBlock = finallyBlock;
    }

    @Override
    public Statement optimize(int level) {
        tryBlock = tryBlock.optimize(level);
        if (catchBlock != null) {
            for (int i = catchBlock.length - 1; i >= 0; i--) {
                catchBlock[i] = (CatchClause)catchBlock[i].optimize(level);
            }
        }
        if (finallyBlock != null) {
            finallyBlock = finallyBlock.optimize(level);
        }
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        Statement tryBlock_ = tryBlock.linkVars(env, maxLvl);
        Statement finallyBlock_ = (finallyBlock == null) ? null
                : finallyBlock.linkVars(env, maxLvl);
        boolean modified = (tryBlock_ != tryBlock
                || finallyBlock_ != finallyBlock);
        CatchClause[] catchBlock_ = null;

        if (catchBlock != null) {
            catchBlock_ = new CatchClause[catchBlock.length];
            for (int i = catchBlock.length - 1; i >= 0; i--) {
                if ((catchBlock_[i] = (CatchClause)catchBlock[i].linkVars(env,
                        maxLvl)) != catchBlock_[i]) {
                    modified = true;
                }
            }
        }

        if (modified) {
            return new TryStm(tryBlock_, catchBlock_, finallyBlock_);
        }
        else {
            return this;
        }
    }

    @Override
    public Object run(Environment env) throws Throwable {
        // the result to throw from the finally-clause, or null if none should
        // be
        // thrown.
        Throwable rethrow = null;

        try {
            // execute the try-clause
            tryBlock.run(env);
        }
        catch (FlowException e) {
            // abrupt completion because of break/continue/return : save the
            // exception
            // to be rethrown after the finally-clause has been executed.
            rethrow = e;
        }
        catch (Throwable t) {
            boolean matched = false;

            // try to match each of the catch clauses
            for (int i = 0; i < catchBlock.length; i++) {
                matched = catchBlock[i].isCatch(t);
                if (matched) {
                    try {
                        // execute the catch clause
                        catchBlock[i].run(env, t);
                    }
                    catch (Throwable _t) {
                        // rethrow _t from the finally clause
                        rethrow = _t;
                    }
                    break;
                }
            }

            if (!matched) {
                // the exception was not matched - set t to be rethrown from the
                // finally block
                rethrow = t;
            }
        } // catch-clause

        // execute the finally-clause, if any; if this terminates abruptly, then
        // the
        // whole statement terminates abruptly for the same reason.
        if (finallyBlock != null) {
            finallyBlock.run(env);
        }

        // rethrow any exeption from the try- or catch clause
        if (rethrow != null) {
            throw rethrow;
        }

        // terminate normally
        return null;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        String indent2 = indent + fmt.getIndent();
        result.append(stx.reserved[RES_TRY - RES_BASE_ID]).append(' ');
        result.append(tryBlock.toString(stx, fmt, indent2)).append('\n');
        for (int i = 0; i < catchBlock.length; i++) {
            result.append(catchBlock[i].toString(stx, fmt, indent));
        }
        if (finallyBlock != null) {
            result.append('\n').append(stx.reserved[RES_FINALLY - RES_BASE_ID]);
            result.append(' ').append(finallyBlock.toString(stx, fmt, indent2));
        }
        return result.toString();
    }

    @Override
    public Type checkCode(Type rt) throws UnreachableStatementException {
        rt = tryBlock.checkCode(rt);
        rt = checkBlock(catchBlock, rt);
        return (finallyBlock == null) ? rt : finallyBlock.checkCode(rt);
    }
}
