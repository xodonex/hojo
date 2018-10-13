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
package org.xodonex.util.ui.edit;

/**
 * A generic DataEditor implementation which stores the edited value directly in
 * an instance variable.
 *
 * @author Henrik Lauritzen
 */
public class BasicDataEditor extends AbstractDataEditor {

    public BasicDataEditor(String key) {
        this(key, null, null);
    }

    public BasicDataEditor(String key, Object value) {
        this(key, value, null);
    }

    public BasicDataEditor(String key, Object value, String[] tags) {
        super(key);
        _data = value;
        _tags = tags;
    }

    @Override
    public Object getValue() {
        return _data;
    }

    @Override
    public Object convertFromString(String s) {
        return s;
    }

    @Override
    public Class getValueClass() {
        return Object.class;
    }

    @Override
    protected Object replaceValue(Object obj) {
        Object result = _data;
        _data = obj;
        return result;
    }

    @Override
    public String[] getTags() {
        return _tags;
    }

    // the edited data
    private Object _data;

    // optional tags
    private String[] _tags;

}
