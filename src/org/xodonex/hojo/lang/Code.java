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

import org.xodonex.hojo.HojoConst;
import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.hojo.lang.stm.NOP;
import org.xodonex.util.StringUtils;

/**
 * A <code>Code</code> instance represents a piece of executable Hojo code, and
 * is the base class for the type hierarchy of the generated code (AST).
 */
public abstract class Code
        implements Cloneable, java.io.Serializable, HojoConst {

    private static final long serialVersionUID = 1L;

    /**
     * Executes this <code>Code</code>.
     *
     * @param env
     *            the environment for evaluation
     * @return the result of the evaluation. If no result is generated, then the
     *         return value is {@link NOP#NOP NOP}.
     * @exception HojoException
     *                if a run-time error occurs during the execution.
     */
    public abstract Object xeq(Environment env) throws HojoException;

    /**
     * Returns a hash code for this object.
     *
     * @see Object#hashCode()
     */
    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Determines whether the given object is equal to this <code>Code</code>.
     *
     * @see Object#equals(Object)
     */
    @Override
    public final boolean equals(Object o) {
        return o == this;
    }

    /**
     * Pretty-prints this object as Hojo source code.
     *
     * @param stx
     *            The syntax in which the code should be output.
     * @param fmt
     *            The format used for output (this should be
     *            {@link HojoSyntax#createFormat() pre-generated} from the
     *            syntax config).
     * @param indent
     *            The preceding blank characters to be used.
     * @return the pretty-printed code.
     */
    public String toString(HojoSyntax stx, StringUtils.Format fmt,
            String indent) {
        return StringUtils.any2String(this, fmt, indent);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            // won't happen
            throw new HojoException();
        }
    }
}
