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

import java.util.Collection;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.HojoSyntax;
import org.xodonex.util.StringUtils;

/**
 * Interface for operator implementations.
 */
public interface Operator extends Function {

    public String toString(HojoSyntax stx, StringUtils.Format fmt);

    /**
     * Infer the type of the value resulting from the application of this
     * <code>Operator</code> to to arguments of the given type.
     *
     * @param argTypes
     *            The argument types.
     * @param warnings
     *            A collection to which warnings (instances of
     *            {@link HojoException}) may be added, if it can be foreseen
     *            that an application of the given argument types may not
     *            succeed.
     * @return The inferred type. If the type is <code>null</code>, this implies
     *         that the argument types are known to be illegal.
     */
    public Type inferType(Type[] argTypes, Collection warnings);

}
