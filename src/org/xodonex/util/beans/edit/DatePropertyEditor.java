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
package org.xodonex.util.beans.edit;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 *
 * @author Henrik Lauritzen
 */
public class DatePropertyEditor extends PropertyEditorSupport {

    private DateFormat _fmt;

    public DatePropertyEditor() {
        this(null);
    }

    public DatePropertyEditor(DateFormat fmt) {
        super();
        _fmt = fmt == null ? DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT) : fmt;
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            super.setValue(null);
        }
        else {
            if (!(value instanceof Date)) {
                throw new IllegalArgumentException(value.toString());
            }
            super.setValue(((Date)value).clone());
        }
    }

    @Override
    public String getJavaInitializationString() {
        Date d = (Date)getValue();
        return d == null ? "null" : "new Date(" + d.getTime() + "L)";
    }

    @Override
    public String getAsText() {
        Date d = (Date)super.getValue();
        if (d == null) {
            return null;
        }

        return _fmt.format(d);
    }

    @Override
    public void setAsText(String text)
            throws java.lang.IllegalArgumentException {
        try {
            super.setValue(_fmt.parse(text));
        }
        catch (ParseException e) {
            throw new IllegalArgumentException(text);
        }
    }

}
