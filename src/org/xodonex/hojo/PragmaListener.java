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
package org.xodonex.hojo;

import java.util.Collection;

/**
 * Notification interface for extending the interpreter with user-defined
 * pragmas.
 * 
 */
public interface PragmaListener {

    /**
     * Invoked by the interpreter every time a pragma directive is not handled
     * by the interpreter itself.
     *
     * @param id
     *            The name (ID) of the directive.
     * @param value
     *            The value for that directive.
     * @exception HojoException
     *                if the given value is illegal.
     * @return <code>true</code> if the directive was recognized and thus
     *         handled.
     */
    public boolean pragmaDirective(String id, Object value)
            throws HojoException;

    /**
     * Lists the pragma directives which are handled by this listener. The exact
     * same number of values must be added to each of the three given
     * collections.
     *
     * @param names
     *            The names of the directives recognized by this listener are
     *            added to this container. The names must be instances of
     *            {@link java.lang.String}.
     * @param types
     *            The expected argument types for the recognized directives are
     *            stored in this container. These values must be instances of
     *            {@link java.lang.Class}.
     * @param comments
     *            A human-readable description of the effects of the recognized
     *            directives are placed in this container. The value may be
     *            <code>null</code>, if no description is provided. The values
     *            must otherwise be instances of {@link java.lang.String}. <br>
     * @return The number of recognized directives. The size of each of the
     *         argument collections should each have increased by this amount
     *         after the call.
     */
    public int listDirectives(Collection names, Collection types,
            Collection comments);

}
