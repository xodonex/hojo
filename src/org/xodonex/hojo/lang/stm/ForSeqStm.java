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

import java.util.Iterator;

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.UnreachableStatementException;
import org.xodonex.hojo.lang.Variable;
import org.xodonex.hojo.lang.env.DummyEnv;
import org.xodonex.hojo.lang.env.Env;
import org.xodonex.hojo.util.BreakException;
import org.xodonex.hojo.util.ContinueException;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;

public class ForSeqStm extends Statement {

    private static final long serialVersionUID = 1L;

    protected short envSize;
    protected String varName, countName;
    protected Type varType;
    protected Expression sequence;
    protected Statement[] body;

    public ForSeqStm(Expression seq, String name, String countName, Type t,
            Statement[] body, short size) {
        this.varName = name;
        this.countName = countName;
        this.varType = t;
        this.sequence = seq;
        this.body = body;
        this.envSize = size;
    }

    @Override
    public Statement optimize(int level) {
        sequence = sequence.optimize(level);
        for (int i = body.length - 1; i >= 0; i--) {
            body[i] = body[i].optimize(level);
        }
        return this;
    }

    @Override
    public Statement linkVars(Environment env, short maxLvl) {
        boolean modified = false;

        Environment env2 = new DummyEnv(env);
        Expression sequence_ = sequence.linkVars(env, maxLvl);

        Statement[] body_ = new Statement[body.length];
        for (int i = body.length - 1; i >= 0; i--) {
            if ((body_[i] = body[i].linkVars(env2, maxLvl)) != body[i]) {
                modified = true;
            }
        }

        if (modified || sequence_ != sequence) {
            return new ForSeqStm(sequence_, varName, countName, varType,
                    body_, envSize);
        }
        else {
            return this;
        }
    }

    @Override
    public Object run(Environment env) throws Throwable {
        Object seqObj = sequence.xeq(env);
        Iterator seq = ConvertUtils.toIterator(seqObj);

        if (seq == null || !seq.hasNext()) {
            // empty sequence - done
            return null;
        }

        Thread t = Thread.currentThread();
        Environment env2 = new Env(env, envSize);
        Variable var = env2.alloc((short)0, true, varName, varType, null);
        Variable count = null;
        int counter = 0;
        if (countName != null) {
            count = env2.alloc((short)1, true, countName, HojoLib.INT_TYPE,
                    ConvertUtils.ZERO_INT);
        }

        while (true) {
            try {
                var.set(Variable.MODIFY_FINAL, varType.typeCast(seq.next()));
                for (int i = 0; i < body.length; i++) {
                    body[i].run(env2);
                }
            }
            catch (ContinueException e) {
                // continue the loop execution
            }
            catch (BreakException e) {
                break;
            }

            if (t.isInterrupted()) {
                throw new InterruptedException();
            }

            if (count != null) {
                // increase the (final) counter variable
                count.set(Variable.MODIFY_FINAL, new Integer(++counter));
            }
            if (!seq.hasNext()) {
                break;
            }
        }

        return null;
    }

    @Override
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        StringBuffer result = new StringBuffer(indent);
        result.append(stx.reserved[RES_FOR - RES_BASE_ID]).append(' ');
        result.append(varType.toString(stx)).append(' ');
        result.append(varName);
        if (countName != null) {
            result.append(stx.punctuators[PCT_IDX_DELIMITER]).append(' ')
                    .append(countName);
        }
        result.append(' ').append(stx.operators[OP_IDX_ELEM]).append(' ');
        result.append(sequence.toString(stx, fmt, ""));
        result.append(' ').append(stx.punctuators[PCT_IDX_BLOCKSTART])
                .append('\n');

        String indent_ = indent + fmt.getIndent();
        for (int i = 0; i < body.length; i++) {
            result.append(body[i].toString(stx, fmt, indent_)).append('\n');
        }

        result.append(indent).append(stx.punctuators[PCT_IDX_BLOCKEND])
                .append('\n');
        return result.toString();
    }

    @Override
    public Type checkCode(Type rt) throws UnreachableStatementException {
        return checkBlock(body, rt);
    }

}
