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
package org.xodonex.hojo.lang.type;

import java.net.URL;

import org.xodonex.hojo.lang.Type;
import org.xodonex.util.ConvertUtils;

public final class URLType extends Type {

    private static final long serialVersionUID = 1L;

    private final static Type INSTANCE = new URLType();

    private URLType() {
    }

    public static Type getInstance() {
        return INSTANCE;
    }

    @Override
    public Class toClass() {
        return URL.class;
    }

    @Override
    public boolean contains(Type t) {
        return t == this || t.kind() == TYP_NULL;
    }

    @Override
    protected Object convert(Object o) {
        return ConvertUtils.toURL(o, null);
    }

}
