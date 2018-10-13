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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * @author Henrik Lauritzen
 */
public class DateDataEditor extends AbstractMapDataEditor {

    public DateDataEditor(Map data, String name) {
        this(data, name, null);
    }

    public DateDataEditor(Map data, String name, DateFormat fmt) {
        super(data, name);
        _fmt = fmt == null ? DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT) : fmt;
    }

    @Override
    public Class getValueClass() {
        return Date.class;
    }

    @Override
    public Object convertFromString(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        try {
            return _fmt.parse(s);
        }
        catch (ParseException e) {
            return ERROR;
        }
    }

    @Override
    public String convertToString(Object v) {
        return v == null ? "" : _fmt.format(v);
    }

    // the format used to convert dates
    private DateFormat _fmt;

}
