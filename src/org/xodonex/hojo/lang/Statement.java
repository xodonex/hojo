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
package org.xodonex.hojo.lang;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.util.FlowException;

/**
 * Abstract base class for code that is a statement.
 */
public abstract class Statement extends Code {

    private static final long serialVersionUID = 1L;

    /**
     * Optimizes the code represented by this <code>Statement</code>.
     *
     * @param level
     *            the optimization level, which indicates how much effort should
     *            be spent on the optimization.
     * @return an equivalent <code>Statement</code> which will
     *         {@link Code#xeq(Environment) execute} at least as efficiently as
     *         this <code>Statement</code>.
     */
    public Statement optimize(int level) {
        return this;
    }

    /**
     * Dereferences all contained references to non-local variables at the
     * current function nesting level.
     *
     * @param env
     *            the enclosing environment.
     * @param maxLvl
     *            the function nesting level
     * @return a <code>Statement</code> in which all non-local variables are
     *         referenced directly, i.e. such that the parent environment is no
     *         longer necessary.
     */
    public Statement linkVars(Environment env, short maxLvl) {
        return this;
    }

    @Override
    public Object xeq(Environment env) throws HojoException {
        try {
            return run(env);
        }
        catch (FlowException e) {
            throw e;
        }
        catch (Throwable t) {
            throw HojoException.wrap(t, this);
        }
    }

    /**
     * @return whether this <code>Statement</code> results in a value.
     */
    public boolean hasValue() {
        return false;
    }

    /**
     * This code is run every time a statement is executed.
     *
     * @param env
     *            the evaluation environment
     * @return the expression value, if this statement is an expression
     *         statement. Otherwise the return value should be
     *         <code>null</code>, but is not used.
     * @throws Throwable
     *             any code can be invoked in this way, hence any exception may
     *             result
     */
    public abstract Object run(Environment env) throws Throwable;

    /**
     * Determine whether this statement always terminates abruptly.
     *
     * @return <code>true</code> iff this statement is guaranteed to terminate
     *         apruptly.
     */
    public boolean isControlTransfer() {
        return false;
    }

    /**
     * Determine the return type of this statement and verifiy that no
     * unreachable statement is contained in this code.
     *
     * @param rt
     *            The return type relative to which the return type should be
     *            determined.
     * @exception UnreachableStatementException
     *                if some statement is unreachable.
     * @return the inferred type of this statement (which may hold an
     *         expression).
     */
    public Type checkCode(Type rt) throws UnreachableStatementException {
        return rt;
    }

    /**
     * Calculate the most specific return type of this statement relative to the
     * given return type, and verify that no unreachable statement is contained
     * in this code.
     *
     * @param block
     *            the block of code to be checked
     * @param rt
     *            The return type relative to which the return type should be
     *            determined.
     * @return The return type of this statement, or <code>rt</code> if this
     *         statement cannot return a value.
     * @exception UnreachableStatementException
     *                if some statement is unreachable.
     */
    public static Type checkBlock(Statement[] block, Type rt)
            throws UnreachableStatementException {
        for (int i = 0; i < block.length; i++) {
            rt = block[i].checkCode(rt);
            if ((i < block.length - 1) && block[i].isControlTransfer()) {
                throw new UnreachableStatementException(block[i + 1]);
            }
        }
        return rt;
    }

}
