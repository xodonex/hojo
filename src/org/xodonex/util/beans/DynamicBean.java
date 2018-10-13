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
package org.xodonex.util.beans;

/**
 * The DynamicBean interface must be implemented by a bean which changes its
 * properties dynamically, if the bean is to be customized in a
 * {@link PropertySheet}.
 */
public interface DynamicBean {

    /**
     * This method must be invoked prior to a property get or set operation,
     * which will be performed by invoking a (reflected) accessor method of the
     * bean itself.
     *
     * @param name
     *            the name of the property to be selected.
     */
    public void selectProperty(String name);

}
