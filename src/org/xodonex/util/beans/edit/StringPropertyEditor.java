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

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.util.Map;

import org.xodonex.util.beans.BeanUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class StringPropertyEditor extends PropertyEditorSupport {

    private String[] _tags;

    public StringPropertyEditor(PropertyDescriptor pd) {
        this(getTagsOf(pd));
    }

    public StringPropertyEditor(String[] tags) {
        super();
        _tags = tags;
    }

    @Override
    public String[] getTags() {
        return _tags;
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value == null ? "" : value);
    }

    @Override
    public void setAsText(String text) {
        super.setAsText(text == null ? "" : text);
    }

    private static String[] getTagsOf(PropertyDescriptor pd) {
        Object ts = pd.getValue(BeanUtils.PROPERTY_KEY_TAGS);
        if (ts instanceof Map) {
            Map m = (Map)ts;
            return (String[])m.keySet().toArray(new String[m.size()]);
        }
        else {
            return (String[])ts;
        }
    }

}
