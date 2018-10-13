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

import org.xodonex.hojo.HojoLib;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.StandardFunction;
import org.xodonex.util.StringUtils;

/**
 * Base class for code representing expressions.
 */
public abstract class Expression extends Code {

    private static final long serialVersionUID = 1L;

    protected transient Type t = null;

    public static String toString(Function f, HojoSyntax stx,
            StringUtils.Format fmt) {
        if (f instanceof Operator) {
            return ((Operator)f).toString(stx, fmt);
        }
        else if (f instanceof StandardFunction) {
            return ((StandardFunction)f).toString(stx, fmt);
        }
        else {
            return stx.punctuators[PCT_IDX_LPAREN] + f +
                    stx.punctuators[PCT_IDX_RPAREN];
        }
    }

    /**
     * @return whether the represented expression has a constant (immutable)
     *         value.
     */
    public boolean isConst() {
        return false;
    }

    /**
     * @return whether the represented expression is a type.
     */
    public boolean isType() {
        return false;
    }

    /**
     * @return whether the expression can be used as a statement
     */
    public boolean isJavaStatement() {
        return false;
    }

    /**
     * Optimize the code represented by this <code>Expression</code>.
     *
     * @param level
     *            the optimization level, which indicates how much effort should
     *            be spent on the optimization.
     * @return an equivalent <code>Statement</code> which will
     *         {@link Code#xeq(Environment) execute} at least as efficiently as
     *         this <code>Expression</code>.
     */
    public Expression optimize(int level) {
        return this;
    }

    /**
     * Dereferences all contained references to variables declared at or above
     * the given function nesting level.
     *
     * @param env
     *            the enclosing environment.
     * @param maxLvl
     *            the maximum function nesting level
     * @return a <code>Expression</code> in which all non-local variables are
     *         referenced directly, such that a parent environment of the
     *         expression is no longer necessary for the expression to execute.
     */
    public Expression linkVars(Environment env, short maxLvl) {
        return this;
    }

    /**
     * @return the (basic) type of the <code>Expression</code>, as represented
     *         by its base Class.
     */
    public Class getTypeC() {
        return getType().toClass();
    }

    /**
     * @return the (exact) type of the <code>Expression</code>.
     */
    public final Type getType() {
        if (t == null) {
            if ((t = getType0()) == null) {
                // might get null from inferType()
                t = HojoLib.OBJ_TYPE;
            }
        }
        return t;
    }

    /**
     * Calculate the type of this <code>Expression</code>. This is called by
     * {@link #getType()} once and buffered in the variable {@link #t}.
     *
     * @return the inferred type of this expression.
     */
    protected abstract Type getType0();

}
