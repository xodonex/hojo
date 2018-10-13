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
package org.xodonex.hojo.lang.env;

import org.xodonex.hojo.HojoException;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.Variable;

public class DummyEnv implements Environment {

    private static final long serialVersionUID = 1L;

    protected Environment parent;

    public DummyEnv(Environment parent) {
        this.parent = parent;
    }

    @Override
    public Environment getParent() {
        return parent;
    }

    @Override
    public Variable get(short index) {
        throw new HojoException();
    }

    @Override
    public Variable alloc(short index, boolean isFinal, String name,
            Type type, Object initialValue) {
        throw new HojoException();
    }

}
