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

import java.beans.BeanInfo;
import java.beans.FeatureDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.xodonex.util.ReflectUtils;
import org.xodonex.util.beans.edit.DatePropertyEditor;

/**
 * Utility class for Java Beans.
 */
public class BeanUtils {

    /**
     * The key under which a {@link PropertyDescriptor} should store a String[]
     * array of keys, if the property editor should use these as tags.
     */
    public final static String PROPERTY_KEY_TAGS = "Tags";

    /**
     * The key under which a {@link PropertyDescriptor} should store an Integer
     * instance signifying to a {@link TabbedPropertySheet} which to class the
     * property belongs.
     */
    public final static String PROPERTY_KEY_CLASS = "Class";

    /**
     * A special class which is interpreted as a hidden property
     */
    public final static int PROPERTY_CLASS_HIDDEN = 0x80000000;

    /**
     * A special class which is interpreted as an expert property
     */
    public final static int PROPERTY_CLASS_EXPERT = 0x40000000;

    // the empty argument list
    private final static Object[] UNIT = new Object[0];

    // for creation of property descriptors
    private static Method DUMMY_METHOD;

    // register a proper date editor on startup, if necessary
    static {
        PropertyEditor pe = PropertyEditorManager.findEditor(Date.class);
        if (pe == null) {
            PropertyEditorManager.registerEditor(Date.class,
                    DatePropertyEditor.class);
        }
    }

    /**
     * Read the value of a given bean property
     *
     * @param bean
     *            the bean
     * @param property
     *            the property to retrieve
     * @param index
     *            optional index for indexed properties
     * @return the property value
     * @throws IllegalArgumentException
     *             on error
     */
    public static Object getPropertyValue(Object bean,
            PropertyDescriptor property,
            int index)
            throws IllegalArgumentException {
        boolean indexed = property instanceof IndexedPropertyDescriptor;
        Method m = indexed
                ? ((IndexedPropertyDescriptor)property).getIndexedReadMethod()
                : property.getReadMethod();
        if (m == null) {
            // write - only property!
            return null;
        }

        if (bean instanceof DynamicBean) {
            ((DynamicBean)bean).selectProperty(property.getName());
        }

        try {
            return m.invoke(bean,
                    indexed ? new Object[] { new Integer(index) } : UNIT);
        }
        catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            else {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    /**
     * Set the value of a given bean property.
     *
     * @param bean
     *            the bean
     * @param property
     *            the property to set
     * @param index
     *            optional index for indexed values
     * @param newValue
     *            the value to be set
     * @throws IllegalArgumentException
     *             on error
     */
    public static void setPropertyValue(Object bean,
            PropertyDescriptor property,
            int index, Object newValue) throws IllegalArgumentException {

        boolean indexed = property instanceof IndexedPropertyDescriptor;
        Method m = indexed
                ? ((IndexedPropertyDescriptor)property).getIndexedWriteMethod()
                : property.getWriteMethod();
        if (m == null) {
            throw new IllegalArgumentException();
        }

        if (bean instanceof DynamicBean) {
            ((DynamicBean)bean).selectProperty(property.getName());
        }

        try {
            m.invoke(bean,
                    indexed ? new Object[] { new Integer(index), newValue }
                            : new Object[] { newValue });
        }
        catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            else {
                throw new IllegalArgumentException(e.getMessage());
            }
        }
    }

    /**
     * Obtain a BeanInfo for the given bean. This merely checks whether an
     * {@link IntrospectiveBean} has been delivered and, if so, allows it to
     * provide its own BeanInfo. Otherwise, the {@link Introspector} is used to
     * provide the BeanInfo.
     *
     * @param bean
     *            the bean
     * @return a BeanInfo for the bean
     * @throws IntrospectionException
     *             on error
     */
    public static BeanInfo getBeanInfo(Object bean)
            throws IntrospectionException {
        if (bean instanceof IntrospectiveBean) {
            return ((IntrospectiveBean)bean).getBeanInfo();
        }
        else {
            return Introspector.getBeanInfo(bean.getClass());
        }
    }

    /**
     * @param bean
     *            a bean
     * @return a collection of all the properties for some bean. Properties
     *         whose accessor methods are declared in non-public classes will
     *         not be returned.
     * @throws IntrospectionException
     *             on error
     */
    public static Collection createPropertyDescriptors(Object bean)
            throws IntrospectionException {
        Collection result = new ArrayList();
        Set names = new HashSet();

        addPropertyDescriptors(getBeanInfo(bean), names, result);
        return result;
    }

    // create a collection of property descriptors traversing the beaninfo's
    // property descriptors in a depth-first order
    private static void addPropertyDescriptors(BeanInfo binf, Set names,
            Collection result) throws IntrospectionException {

        // get the property descriptors
        PropertyDescriptor[] pds = binf.getPropertyDescriptors();

        // add them to the collections
        for (int i = 0; i < pds.length; i++) {
            PropertyDescriptor pd = pds[i];

            if (!names.add(pd.getName())) {
                // ignore hidden properties as well as properties which are
                // already present
                continue;
            }

            // check whether the declaring class for the read method is public
            Method m;
            m = (pd instanceof IndexedPropertyDescriptor)
                    ? ((IndexedPropertyDescriptor)pd).getIndexedReadMethod()
                    : pd.getReadMethod();
            if (m == null) {
                // fixme: write-only method
                continue;
            }
            if ((m.getDeclaringClass().getModifiers() & Modifier.PUBLIC) == 0) {
                continue;
            }

            result.add(pd);
        }

        // add any additional info
        BeanInfo[] extras = binf.getAdditionalBeanInfo();
        if (extras != null) {
            for (int i = 0; i < extras.length; i++) {
                addPropertyDescriptors(extras[i], names, result);
            }
        }
    }

    /**
     * @return a new property descriptor which can be used as the base for
     *         {@link #readPropertyDescriptor deserialization}.
     */
    public synchronized static PropertyDescriptor createEmptyPropertyDescriptor() {
        if (DUMMY_METHOD == null) {
            try {
                DUMMY_METHOD = Object.class.getDeclaredMethod(
                        "toString", new Class[0]);
            }
            catch (Exception e) {
                // won't happen
                throw new Error(e.getMessage());
            }
        }

        try {
            return new PropertyDescriptor(".", DUMMY_METHOD, null);
        }
        catch (IntrospectionException e) {
            // won't happen
            throw new Error(e.getMessage());
        }
    }

    /**
     * Serialize the common properties of a feature descriptor, but not the
     * class information itself.
     *
     * @param fd
     *            the feature descriptor whose properties should be serialized
     * @param out
     *            the output stream which should receive the serialized data
     * @throws IOException
     *             on I/O error
     */
    public static void writeFeatureDescriptor(
            FeatureDescriptor fd, ObjectOutputStream out) throws IOException {
        // determine the attributes set for the property descriptor
        ArrayList propNames = new ArrayList();
        for (Enumeration e = fd.attributeNames(); e.hasMoreElements();) {
            propNames.add(e.nextElement());
        }

        // write the attributes
        int size = propNames.size();
        out.writeInt(size);
        for (int i = 0; i < size; i++) {
            String s = (String)propNames.get(i);
            out.writeObject(s);
            out.writeObject(fd.getValue(s));
        }

        // write the simple properties
        out.writeObject(fd.getDisplayName());
        out.writeBoolean(fd.isExpert());
        out.writeBoolean(fd.isHidden());
        out.writeObject(fd.getName());
        out.writeBoolean(fd.isPreferred());
        out.writeObject(fd.getShortDescription());
    }

    /**
     * Deserialize the common properties of a previously serialized feature
     * descriptor
     *
     * @param fd
     *            the feature descriptor whose properties should be set from the
     *            serialized data
     * @param in
     *            the input stream which contains the serialized data
     * @throws IOException
     *             on I/O error
     * @throws ClassNotFoundException
     *             on deserialization error
     */
    public static void readFeatureDescriptor(FeatureDescriptor fd,
            ObjectInputStream in) throws IOException, ClassNotFoundException {
        // read the attributes and store them in the descriptor
        int count = in.readInt();
        while (count-- > 0) {
            fd.setValue((String)in.readObject(), in.readObject());
        }

        // read the simple properties
        fd.setDisplayName((String)in.readObject());
        fd.setExpert(in.readBoolean());
        fd.setHidden(in.readBoolean());
        fd.setName((String)in.readObject());
        fd.setPreferred(in.readBoolean());
        fd.setShortDescription((String)in.readObject());
    }

    /**
     * Serialize the common properties of a property descriptor, but not the
     * class information itself.
     *
     * @param pd
     *            the property descriptor whose properties should be serialized
     * @param out
     *            the output stream which should receive the serialized data
     * @throws IOException
     *             on I/O error
     */
    public static void writePropertyDescriptor(
            PropertyDescriptor pd, ObjectOutputStream out) throws IOException {
        out.writeBoolean(pd.isBound());
        out.writeBoolean(pd.isConstrained());
        out.writeObject(pd.getPropertyEditorClass());

        ReflectUtils.writeMethod(pd.getReadMethod(), out);
        ReflectUtils.writeMethod(pd.getWriteMethod(), out);

        writeFeatureDescriptor(pd, out);
    }

    /**
     * Deserialize the common properties of a previously serialized property
     * descriptor
     *
     * @param pd
     *            the feature descriptor whose properties should be set from the
     *            serialized data
     * @param in
     *            the input stream which contains the serialized data
     * @throws IOException
     *             on I/O error
     * @throws ClassNotFoundException
     *             on deserialization error
     */
    public static void readPropertyDescriptor(PropertyDescriptor pd,
            ObjectInputStream in) throws IOException, ClassNotFoundException {
        pd.setBound(in.readBoolean());
        pd.setConstrained(in.readBoolean());
        pd.setPropertyEditorClass((Class)in.readObject());

        // set the write method to null just in case
        try {
            pd.setWriteMethod(null);
        }
        catch (IntrospectionException e) {
        }

        try {
            pd.setReadMethod(ReflectUtils.readMethod(in));
            pd.setWriteMethod(ReflectUtils.readMethod(in));
        }
        catch (IntrospectionException e) {
            throw new IOException(e.getMessage());
        }

        readFeatureDescriptor(pd, in);
    }

    private BeanUtils() {
    }

}
