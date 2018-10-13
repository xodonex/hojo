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

import java.beans.PropertyChangeListener;

/**
 * A DataEditor supports editing of a single value.
 */
public interface DataEditor {

    /**
     * Represents an error value in a conversion
     */
    public Object ERROR = new Object() {
        @Override
        public String toString() {
            return "ERROR";
        }
    };

    /**
     * @return an ID for the data item edited by this editor
     */
    public String getID();

    /**
     * @return the most narrow, common class which can represent all values
     *         edited by this editor
     */
    public Class getValueClass();

    /**
     * Convert the given string to a value which is represented in an acceptable
     * form.
     *
     * @param s
     *            the input string
     * @return the converted value, or {@link #ERROR} iff the given string is
     *         invalid. No exceptions may be thrown.
     */
    public Object convertFromString(String s);

    /**
     * Converts the given value to a string representation.
     *
     * @param v
     *            the input value
     * @return the converted value
     */
    public String convertToString(Object v);

    /**
     * @return the edited value as a string
     */
    public String getAsText();

    /**
     * Updates the edited value from a string representation
     *
     * @param str
     *            the string value
     * @exception IllegalArgumentException
     *                if the conversion failed
     */
    public void setAsText(String str) throws IllegalArgumentException;

    /**
     * Validates that a given value is acceptable by this editor
     *
     * @param obj
     *            the input value
     * @return true iff the value is acceptable. No exceptions may be thrown,
     *         even if the operation is not successful
     */
    public boolean validateValue(Object obj);

    /**
     * Tries to convert the given value to an internal representation, and
     * {@link #setValue(Object) updates} the edited value if successful.
     *
     * @param str
     *            the input value as text
     * @return true iff the conversion succeeded. No exceptions may be thrown,
     *         regardless of whether the operation succeeded.
     */
    public boolean trySetAsText(String str);

    /**
     * Updates the edited value. If successful, all registered
     * {@link #addPropertyChangeListener(PropertyChangeListener) listeners} will
     * be notified accordingly.
     *
     * @param obj
     *            the new value
     * @exception IllegalArgumentException
     *                if the given value is illegal
     */
    public void setValue(Object obj) throws IllegalArgumentException;

    /**
     * Updates the edited value. If successful, all registered
     * {@link #addPropertyChangeListener(PropertyChangeListener) listeners} will
     * be notified accordingly.
     *
     * @param obj
     *            the new value
     * @return true iff the update succeeded. No exceptions may be thrown
     */
    public boolean trySetValue(Object obj);

    /**
     * @return the current value held by this editor
     */
    public Object getValue();

    /**
     * @return a set of tag values for this data editor, or null, if no tags are
     *         necessary. Note that {@link #convertFromString(String)} and
     *         {@link #convertToString(Object)} may be independent of these tags
     *         - in some implementations the tags are the default options rather
     *         than the only options.
     */
    public String[] getTags();

    /**
     * Registers a listener on this editor, such that it will receive
     * notifications when the edited value changes
     *
     * @param l
     *            the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Removes a previously registered listener from this editor
     *
     * @param l
     *            the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener l);

}
