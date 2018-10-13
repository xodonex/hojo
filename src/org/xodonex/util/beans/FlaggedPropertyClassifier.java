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

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Iterator;

/**
 * A property classifier which uses information in the property descriptor to
 * determine the class of the property.
 *
 * @see BeanUtils#PROPERTY_KEY_CLASS
 *
 * @author Henrik Lauritzen
 */
public class FlaggedPropertyClassifier implements PropertyClassifier {

    private String[] _names;
    private boolean _useExpert;

    public FlaggedPropertyClassifier(String[] classNames, boolean useExpert) {
        if ((_names = classNames) == null) {
            throw new NullPointerException();
        }
        _useExpert = useExpert;
    }

    @Override
    public int countClasses() {
        return _names.length;
    }

    @Override
    public String[] getClasses() {
        return _names.clone();
    }

    @Override
    public void classify(Iterator pds, Collection[] classes) {
        while (pds.hasNext()) {
            PropertyDescriptor pd = (PropertyDescriptor)pds.next();

            if (pd.isHidden()) {
                continue;
            }

            Integer I = (Integer)pd.getValue(BeanUtils.PROPERTY_KEY_CLASS);
            if (I != null) {
                for (int i = 0, flags = I.intValue(), mask = 1, max = _useExpert
                        ? _names.length - 1
                        : _names.length; i < max; i++, mask <<= 1) {
                    if ((flags & mask) != 0) {
                        classes[i].add(pd);
                    }
                }
            }

            if (_useExpert && pd.isExpert()) {
                classes[classes.length - 1].add(pd);
            }
        }
    }

}
