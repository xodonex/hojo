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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.xodonex.util.ReflectUtils;

/**
 * An abstract DataEditor implementation which lets subclasses determine how to
 * store and retreive the edited value.
 */
public abstract class AbstractDataEditor implements DataEditor {

    public AbstractDataEditor(String key) {
        _key = key;
    }

    /**
     * Modifies the edited value
     *
     * @param newValue
     *            the new value
     * @return the previous edited value
     */
    protected abstract Object replaceValue(Object newValue);

    /**
     * Fires an appropriate property change event to all registered listeners.
     *
     * @param old
     *            the old property value
     * @param current
     *            the new property value
     */
    protected void firePropertyChange(Object old, Object current) {
        if (old == null ? current == null : old.equals(current)) {
            return;
        }

        Map listeners = _listeners;
        if (listeners == null) {
            return;
        }
        synchronized (_listeners) {
            int size = _listeners.size();
            if (size == 0) {
                return;
            }

            PropertyChangeEvent e = new PropertyChangeEvent(this, _key,
                    old, current);
            for (Iterator i = _listeners.keySet().iterator(); i.hasNext();) {
                ((PropertyChangeListener)i.next()).propertyChange(e);
            }
        }
    }

    @Override
    public abstract Object getValue();

    @Override
    public String getID() {
        return _key;
    }

    @Override
    public String convertToString(Object v) {
        return v == null ? null
                : v instanceof String ? (String)v : v.toString();
    }

    @Override
    public boolean validateValue(Object obj) {
        Class c = getValueClass();

        if (obj == null) {
            return !c.isPrimitive();
        }

        if (c.isPrimitive()) {
            return ReflectUtils.getWrapper(c) == obj.getClass();
        }
        else {
            return c.isAssignableFrom(obj.getClass());
        }
    }

    @Override
    public boolean trySetAsText(String str) {
        Object v;

        try {
            v = convertFromString(str);
            if (!validateValue(v)) {
                return false;
            }
        }
        catch (RuntimeException e) {
            return false;
        }
        if (v == ERROR) {
            return false;
        }

        Object prev = replaceValue(v);
        firePropertyChange(prev, v);
        return true;
    }

    @Override
    public boolean trySetValue(Object v) {
        if (!validateValue(v)) {
            return false;
        }

        Object prev = replaceValue(v);
        firePropertyChange(prev, v);
        return true;
    }

    @Override
    public void setValue(Object obj) {
        if (!validateValue(obj)) {
            throw new IllegalArgumentException();
        }
        Object prev = replaceValue(obj);
        firePropertyChange(prev, obj);
    }

    @Override
    public String getAsText() {
        return convertToString(getValue());
    }

    @Override
    public void setAsText(String str) throws IllegalArgumentException {
        Object v = convertFromString(str);
        if (v == ERROR) {
            throw new IllegalArgumentException(str);
        }

        Object prev = replaceValue(v);
        firePropertyChange(prev, v);
    }

    @Override
    public String[] getTags() {
        return null;
    }

    @Override
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener l) {
        if (_listeners != null) {
            _listeners.remove(l);
        }
    }

    @Override
    public synchronized void addPropertyChangeListener(
            PropertyChangeListener l) {
        if (_listeners == null) {
            _listeners = new WeakHashMap();
        }
        _listeners.put(l, null);
    }

    // the key under which data are stored
    private String _key;

    // the (weak!) list of property change listeners
    private WeakHashMap _listeners = null;

}
