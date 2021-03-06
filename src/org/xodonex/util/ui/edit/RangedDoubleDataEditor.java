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

import java.util.Map;

/**
 * An editor for ranged double-value properties backed by a map.
 */
public class RangedDoubleDataEditor extends DoubleDataEditor {

    /**
     * Constructs a new editor which only accepts integers in a given range.
     *
     * @param data
     *            the backing data for the value
     * @param key
     *            the key for the edited value in the backing data
     * @param lo
     *            the lower bound (inclusive)
     * @param hi
     *            the upper bound (exclusive)
     */
    public RangedDoubleDataEditor(Map data, String key, double lo, double hi) {
        this(data, key, null, lo, hi);
    }

    /**
     * Constructs a new editor which only accepts integers in a given range.
     *
     * @param data
     *            the backing data for the value
     * @param key
     *            the key for the edited value in the backing data
     * @param fmt
     *            the format used for editing
     * @param lo
     *            the lower bound (inclusive)
     * @param hi
     *            the upper bound (exclusive)
     */
    public RangedDoubleDataEditor(Map data, String key,
            java.text.DecimalFormat fmt,
            double lo, double hi) {
        super(data, key, fmt);
        _lo = lo;
        _hi = hi;
    }

    @Override
    public boolean validateValue(Object obj) {
        if (obj == null) {
            return true;
        }
        else if (!(obj instanceof Double)) {
            return false;
        }
        double d = ((Double)obj).doubleValue();
        return d >= _lo && d <= _hi;
    }

    // the range
    private double _lo, _hi;

}
