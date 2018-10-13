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

import java.util.Collection;
import java.util.Iterator;

/**
 * Classifies a set of properties into a set of classes.
 */
public interface PropertyClassifier {

    /**
     * Default class ID for unrecognized properties.
     */
    public int CLASS_NONE = -1;

    /**
     * @return the number of classes used by this classifier.
     */
    public int countClasses();

    /**
     * @return an array of class names (or textual IDs) corresponding to the
     *         classes used by this classifier. The length of the array must be
     *         equal to the value returned by {@link #countClasses()}
     */
    public String[] getClasses();

    /**
     * Classify the given sequence of property descriptors into the given
     * classes. It is legal to place the same property descriptor in any number
     * of classes, but only once in each class.
     *
     * @param pds
     *            an Iterator of PropertyDescriptors
     * @param classes
     *            the property classes. The number of classes should correspond
     *            to the {@link #countClasses() class count}.
     */
    public void classify(Iterator pds, Collection[] classes);

}
