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
package org.xodonex.util.ui;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A CompositeResourceBundle merges two different resource bundles into one,
 * giving precedence to the first of the resource bundles in case the same key
 * is found in both bundles.
 *
 * @author Henrik Lauritzen
 */
public class CompositeResourceBundle extends ResourceBundle {

    // the two bundles
    private ResourceBundle _bnd1, _bnd2;

    // map a property key into a Boolean instance which holds the value
    // true iff the key should be looked up in the first bundle
    private Map _keyMap = new HashMap(16, 0.9f);

    public CompositeResourceBundle(ResourceBundle primary,
            ResourceBundle secondary) {
        super();
        _bnd1 = primary;
        _bnd2 = secondary;

        // build the keyMap
        for (Enumeration e = _bnd2.getKeys(); e.hasMoreElements();) {
            _keyMap.put(e.nextElement(), Boolean.FALSE);
        }
        for (Enumeration e = _bnd1.getKeys(); e.hasMoreElements();) {
            _keyMap.put(e.nextElement(), Boolean.TRUE);
        }
    }

    @Override
    public Enumeration getKeys() {
        return new Enumeration() {
            Iterator i = _keyMap.keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return i.hasNext();
            }

            @Override
            public Object nextElement() {
                return i.next();
            }
        };
    }

    @Override
    protected Object handleGetObject(String key) {
        Boolean b = (Boolean)_keyMap.get(key);
        if (b == null) {
            return null;
        }

        return (b.booleanValue() ? _bnd1 : _bnd2).getObject(key);
    }

}
