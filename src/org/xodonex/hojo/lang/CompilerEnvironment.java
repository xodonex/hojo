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

/**
 * Compile-time representation of a lexical scope.
 */
public interface CompilerEnvironment {

    public int ADDR_LOCAL = 0;
    public int ADDR_DEPTH = 1;
    public int ADDR_LEVEL = 2;
    public int ADDR_MODIFIERS = 3;

    /**
     * Determine how to access the variable named <code>name</code> from this
     * environment.
     *
     * @param name
     *            The name of the variable to be found.
     * @param type
     *            The 0th element of this array will be updated with the type of
     *            the variable, if it is found.
     * @return The location of the variable. The 0th element is the local
     *         address within its environment; the nesting depth of that
     *         environment relative to this is stored in the 1st element while
     *         the level of that environment is stored in the 2nd element. The
     *         3rd element contains the modifiers declared for that variable. If
     *         the variable is not found, then the return value is
     *         <code>null</code>.
     */
    public short[] findVar(String name, Type[] type);

    public CompilerEnvironment getParent(short chainLength);

    public short getModifiers(String name);

    public Type getType(String name);

    public short getAddress(String name);

    public short getLevel();

    public short alloc(String name, Type type, short modifiers);

    public int size();

    public void remove(String name);

    /**
     * Marks the given identifier as being assigned.
     *
     * @param name
     *            the identifier name.
     * @return true iff the assignment is legal.
     */
    public boolean doAssign(String name);

    public boolean isAssigned(String name);
}
