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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

/**
 * The GenericBean is a bean whose properties can be added, deleted or
 * configured dynamically. <BR>
 * <em>N.B.</em>The GenericBean does not directly follow the Beans
 * specification, because this prohibits dynamically creating beans due to the
 * use of reflected methods. However, the only exceptions from the specification
 * are the extra methods declared by {@link IntrospectiveBean} and
 * {@link DynamicBean}.
 */
public class GenericBean implements IntrospectiveBean, DynamicBean {

    /**
     * A format for converting the contained values.
     */
    public final static StringUtils.Format FORMAT = new StringUtils.Format(
            StringUtils.FORMAT_JAVA, Integer.MAX_VALUE, Integer.MAX_VALUE);
    static {
        FORMAT.formatRepeats(true);
    }

    // map of property names to their values
    private Map _map;

    // a map of property names to their descriptors.
    private Map _descriptors;

    // the bean info and bean descriptors
    private BeanDescriptor _beanDescriptor = null;
    private BeanInfo _beanInfo = null;

    // the last value given to selectProperty()
    private String _selected = null;

    public GenericBean() {
        _map = createLinkedMap();
        _descriptors = createLinkedMap();
    }

    public GenericBean(Map m) throws IntrospectionException {
        this(m, true);
    }

    public GenericBean(Map m, boolean init) throws IntrospectionException {
        _descriptors = createLinkedMap();
        _map = m == null ? createLinkedMap() : m;

        if (!init) {
            return;
        }

        for (Iterator i = _map.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            addProperty((String)e.getKey(), e.getValue());
        }
    }

    /**
     * Add a new read-only property to this bean. This is equivalent to
     * {@link #addProperty(String, Class, Object, boolean) addProperty(name,
     * value.getClass(), value, true)}.
     *
     * @param name
     *            the name of the property
     * @param value
     *            the value determining the type of the property. If this is an
     *            array, then the property will be indexed. The primitive types
     *            will automatically be used in case the type is a wrapper
     *            class.
     * @return the newly created property's descriptor
     * @throws IntrospectionException
     *             on error
     */
    public PropertyDescriptor addReadOnlyProperty(String name, Object value)
            throws IntrospectionException {
        return addProperty(name, value.getClass(), value, true);
    }

    /**
     * Add a new read-only property to this bean. This is equivalent to
     * {@link #addProperty(String, Class, Object, boolean) addProperty(name,
     * type, value, true)}.
     *
     * @param name
     *            the name of the property
     * @param type
     *            the type of the property. If this is an array, then the
     *            property will be indexed. The primitive types will
     *            automatically be used in case the type is a wrapper class.
     * @param value
     *            the default value of the property. This must match the
     *            specified type, or an IllegalArgumentException will be thrown
     * @return the newly created property's descriptor
     * @throws IntrospectionException
     *             on error
     */
    public PropertyDescriptor addReadOnlyProperty(String name, Class type,
            Object value) throws IntrospectionException {
        return addProperty(name, type, value, true);
    }

    /**
     * Add a new property to this bean. This is equivalent to
     * {@link #addProperty(String, Class, Object, boolean) addProperty(name,
     * value.getClass(), value, false)}.
     *
     * @param name
     *            the name of the property
     * @param value
     *            the default value of the property. This must match the
     *            specified type, or an IllegalArgumentException will be thrown
     * @return the newly created property's descriptor
     * @throws IntrospectionException
     *             on error
     */
    public PropertyDescriptor addProperty(String name, Object value)
            throws IntrospectionException {
        return addProperty(name,
                value == null ? Object.class : value.getClass(),
                value, false);
    }

    /**
     * Add a new property to this bean. This is equivalent to
     * {@link #addProperty(String, Class, Object, boolean) addProperty(name,
     * type, value, false)}.
     *
     * @param name
     *            the name of the property
     * @param type
     *            the type of the property. If this is an array, then the
     *            property will be indexed. The primitive types will
     *            automatically be used in case the type is a wrapper class.
     * @param value
     *            the default value of the property. This must match the
     *            specified type, or an IllegalArgumentException will be thrown
     * @return the newly created property's descriptor
     * @throws IntrospectionException
     *             on error
     *
     */
    public PropertyDescriptor addProperty(String name, Class type,
            Object value) throws IntrospectionException {
        return addProperty(name, type, value, false);
    }

    /**
     * Add a new property to this bean.
     *
     * @param name
     *            the name of the property
     * @param type
     *            the type of the property. If this is an array, then the
     *            property will be indexed. The primitive types will
     *            automatically be used in case the type is a wrapper class.
     * @param value
     *            the default value of the property. This must match the
     *            specified type, or an IllegalArgumentException will be thrown
     * @param readOnly
     *            whether the property should be read-only.
     * @return the newly created property's descriptor
     * @throws IntrospectionException
     *             on error
     */
    public PropertyDescriptor addProperty(String name, Class type,
            Object value, boolean readOnly) throws IntrospectionException {
        if (name == null) {
            throw new NullPointerException();
        }

        final Class pType = ReflectUtils.unwrap(type);
        boolean indexed = type.isArray();

        Class cls = getClass();
        // Method getter = getGetter(cls, type, false);
        // Method setter = readOnly ? null : getSetter(cls, type, false);

        PropertyDescriptor pd;
        if (indexed) {
            final Class iType = pType.getComponentType();

            pd = new IndexedPropertyDescriptor(name,
                    null, null, getGetter(cls, type, true),
                    readOnly ? null : getSetter(cls, type, true)) {
                @Override
                public Class getPropertyType() {
                    return pType;
                }

                @Override
                public Class getIndexedPropertyType() {
                    return iType;
                }
            };
        }
        else {
            pd = new PropertyDescriptor(name, getGetter(cls, type, false),
                    readOnly ? null : getSetter(cls, type, false)) {
                @Override
                public Class getPropertyType() {
                    return pType;
                }
            };
        }

        if (value == null && type.isPrimitive()) {
            value = ReflectUtils.getDefaultValue(type);
        }
        _map.put(name, value);
        _descriptors.put(name, pd);
        return pd;
    }

    /**
     * Delete a property from this bean
     *
     * @param name
     *            the name of the property
     * @return the value of the deleted property, or null in case the property
     *         does not exist.
     */
    public Object deleteProperty(String name) {
        _descriptors.remove(name);
        return _map.remove(name);
    }

    /**
     * @return the names of the properties of this bean.
     */
    public String[] getPropertyNames() {
        return (String[])_descriptors.keySet()
                .toArray(new String[_descriptors.size()]);
    }

    /**
     * @param property
     *            a property
     * @return the property descriptor for the given property, of null if the
     *         property does not exist
     */
    public PropertyDescriptor getPropertyDescriptor(String property) {
        return (PropertyDescriptor)_descriptors.get(property);
    }

    /**
     * @return an array of desriptors for all the properties of this bean.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return (PropertyDescriptor[])_descriptors.values().toArray(
                new PropertyDescriptor[_descriptors.size()]);
    }

    /**
     * @return the BeanDescriptor for this bean.
     */
    public BeanDescriptor getBeanDesriptor() {
        if (_beanDescriptor == null) {
            return _beanDescriptor = new BeanDescriptor(getClass());
        }
        else {
            return _beanDescriptor;
        }
    }

    /**
     * Override this method if a icon is to be provided.
     *
     * @param iconKind
     *            the the kind of icon
     * @return the default implementation returns null.
     */
    public java.awt.Image getIcon(int iconKind) {
        return null;
    }

    /**
     * Override this method to provide additional BeanInfo.
     *
     * @return the default implementation returns null.
     */
    public BeanInfo[] getAdditionalBeanInfo() {
        return null;
    }

    /**
     * @return the value of the {@link #selectProperty(String) selected
     *         property}.
     */
    public Object getValue() {
        PropertyDescriptor pd = getPropertyDescriptor(_selected);
        return pd != null ? getValue(pd, -1) : null;
    }

    /**
     * @param index
     *            the property index
     * @return the specified element of the (indexed)
     *         {@link #selectProperty(String) selected property}.
     */
    public Object getValue(int index) {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("" + index);
        }

        PropertyDescriptor pd = getPropertyDescriptor(_selected);
        return pd != null ? getValue(pd, index) : null;
    }

    /**
     * Update the value of the {@link #selectProperty(String) selected
     * property}.
     *
     * @param obj
     *            the value to be set
     * @throws IllegalArgumentException
     *             if the given value is not legal for the property.
     */
    public void setValue(Object obj) throws IllegalArgumentException {
        PropertyDescriptor pd = getPropertyDescriptor(_selected);
        if (pd != null) {
            setValue(pd, -1, obj);
        }
    }

    public void setValue(int index, Object obj)
            throws IllegalArgumentException {
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("" + index);
        }

        PropertyDescriptor pd = getPropertyDescriptor(_selected);
        if (pd != null) {
            setValue(pd, index, obj);
        }
    }

    // - - - - - - - - - - - - - boolean - - - - - - - - - - - - - - - - - -

    public boolean getBooleanValue() {
        return ConvertUtils.toBool(getValue());
    }

    public boolean getBooleanValue(int index) {
        return ConvertUtils.toBool(getValue(index));
    }

    public void setValue(boolean b) throws IllegalArgumentException {
        setValue(b ? Boolean.TRUE : Boolean.FALSE);
    }

    public void setValue(int index, boolean b) throws IllegalArgumentException {
        setValue(index, b ? Boolean.TRUE : Boolean.FALSE);
    }

    // - - - - - - - - - - - - - byte - - - - - - - - - - - - - - - - - -

    public byte getByteValue() {
        return ConvertUtils.toByte(getValue());
    }

    public byte getByteValue(int index) {
        return ConvertUtils.toByte(getValue(index));
    }

    public void setValue(byte b) throws IllegalArgumentException {
        setValue(new Byte(b));
    }

    public void setValue(int index, byte b) throws IllegalArgumentException {
        setValue(index, new Byte(b));
    }

    // - - - - - - - - - - - - - short - - - - - - - - - - - - - - - - -

    public short getShortValue() {
        return ConvertUtils.toShort(getValue());
    }

    public short getShortValue(int index) {
        return ConvertUtils.toShort(getValue(index));
    }

    public void setValue(short s) throws IllegalArgumentException {
        setValue(new Short(s));
    }

    public void setValue(int index, short s) throws IllegalArgumentException {
        setValue(index, new Short(s));
    }

    // - - - - - - - - - - - - - char - - - - - - - - - - - - - - - - - -

    public char getCharValue() {
        return ConvertUtils.toChar(getValue());
    }

    public char getCharValue(int index) {
        return ConvertUtils.toChar(getValue(index));
    }

    public void setValue(char c) throws IllegalArgumentException {
        setValue(new Character(c));
    }

    public void setValue(int index, char c) throws IllegalArgumentException {
        setValue(index, new Character(c));
    }

    // - - - - - - - - - - - - - int - - - - - - - - - - - - - - - - - - -

    public int getIntValue() {
        return ConvertUtils.toInt(getValue());
    }

    public int getIntValue(int index) {
        return ConvertUtils.toInt(getValue(index));
    }

    public void setValue(int i) throws IllegalArgumentException {
        setValue(new Integer(i));
    }

    public void setValue(int index, int i) throws IllegalArgumentException {
        setValue(index, new Integer(i));
    }

    // - - - - - - - - - - - - - long - - - - - - - - - - - - - - - - - -

    public long getLongValue() {
        return ConvertUtils.toLong(getValue());
    }

    public long getLongValue(int index) {
        return ConvertUtils.toLong(getValue(index));
    }

    public void setValue(long l) throws IllegalArgumentException {
        setValue(new Long(l));
    }

    public void setValue(int index, long l) throws IllegalArgumentException {
        setValue(index, new Long(l));
    }

    // - - - - - - - - - - - - - float - - - - - - - - - - - - - - - - - -

    public float getFloatValue() {
        return ConvertUtils.toFloat(getValue());
    }

    public float getFloatValue(int index) {
        return ConvertUtils.toFloat(getValue(index));
    }

    public void setValue(float f) throws IllegalArgumentException {
        setValue(new Float(f));
    }

    public void setValue(int index, float f) throws IllegalArgumentException {
        setValue(index, new Float(f));
    }

    // - - - - - - - - - - - - - double - - - - - - - - - - - - - - - - - -

    public double getDoubleValue() {
        return ConvertUtils.toDouble(getValue());
    }

    public double getDoubleValue(int index) {
        return ConvertUtils.toDouble(getValue(index));
    }

    public void setValue(double d) throws IllegalArgumentException {
        setValue(new Double(d));
    }

    public void setValue(int index, double d) throws IllegalArgumentException {
        setValue(index, new Double(d));
    }

    @Override
    public void selectProperty(String name) {
        _selected = name;
    }

    @Override
    public BeanInfo getBeanInfo() {
        if (_beanInfo != null) {
            return _beanInfo;
        }

        return _beanInfo = new SimpleBeanInfo() {
            @Override
            public java.beans.BeanInfo[] getAdditionalBeanInfo() {
                return GenericBean.this.getAdditionalBeanInfo();
            }

            @Override
            public java.beans.BeanDescriptor getBeanDescriptor() {
                return GenericBean.this.getBeanDesriptor();
            }

            @Override
            public java.awt.Image getIcon(int iconKind) {
                return GenericBean.this.getIcon(iconKind);
            }

            @Override
            public PropertyDescriptor[] getPropertyDescriptors() {
                return GenericBean.this.getPropertyDescriptors();
            }
        };
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("[.\n");
        for (Iterator i = _descriptors.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            String name = (String)e.getKey();
            PropertyDescriptor pd = (PropertyDescriptor)e.getValue();

            String typeName = ReflectUtils.className2Java(pd.getPropertyType());
            int idx = typeName.lastIndexOf('.');

            buf.append(idx < 0 ? typeName : typeName.substring(idx + 1))
                    .append(' ').append(name).append(" = ")
                    .append(StringUtils.any2String(_map.get(name), FORMAT, ""))
                    .append(";\n");
        }

        return buf.append(".]").toString();
    };

    // attempt to create a LinkedHashMap, default to a HashMap in case it
    // does not exist
    private static Map createLinkedMap() {
        try {
            return (Map)ConvertUtils.DEFAULT_MAP_CLASS.newInstance();
        }
        catch (Throwable t) {
            return new HashMap();
        }
    }

    private static Method getGetter(Class base, Class type, boolean indexed)
            throws IntrospectionException {
        try {
            if (!type.isPrimitive()) {
                return base.getMethod("getValue",
                        indexed ? new Class[] { Integer.TYPE } : new Class[0]);
            }

            String name = type.getName();
            name = "get" + Character.toUpperCase(name.charAt(0)) +
                    name.substring(1) + "Value";
            return base.getMethod(name,
                    indexed ? new Class[] { Integer.TYPE } : new Class[0]);
        }
        catch (Exception e) {
            throw new IntrospectionException(e.getMessage());
        }
    }

    private static Method getSetter(Class base, Class type, boolean indexed)
            throws IntrospectionException {
        try {
            if (!type.isPrimitive()) {
                return base.getMethod("setValue",
                        indexed ? new Class[] { Integer.TYPE, Object.class }
                                : new Class[] { Object.class });
            }
            else {
                return base.getMethod("setValue",
                        indexed ? new Class[] { Integer.TYPE, type }
                                : new Class[] { type });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IntrospectionException(e.getMessage());
        }
    }

    // return the property value for the specified descriptor and optional index
    private Object getValue(PropertyDescriptor pd, int index) {
        if (index >= 0) {
            if (!(pd instanceof IndexedPropertyDescriptor)) {
                throw new IllegalArgumentException("Property " + pd.getName() +
                        " is not indexed");
            }
            return Array.get(_map.get(pd.getName()), index);
        }
        else {
            return _map.get(pd.getName());
        }
    }

    // set the property value for the specified descriptor and optional index
    private void setValue(PropertyDescriptor pd, int index, Object value) {
        // type check the property
        IndexedPropertyDescriptor ipd = pd instanceof IndexedPropertyDescriptor
                ? (IndexedPropertyDescriptor)pd
                : null;
        Class type = (ipd != null && index >= 0) ? ipd.getIndexedPropertyType()
                : pd.getPropertyType();

        if (type.isPrimitive() ? value == null ||
                ReflectUtils.getPrimitive(value.getClass()) != type
                : value != null && !(type.isAssignableFrom(value.getClass()))) {
            throw new IllegalArgumentException("Value is not of type " +
                    type.getName());
        }

        // verify that the property can be written
        if (pd.getWriteMethod() == null &&
                (ipd != null ? ipd.getIndexedWriteMethod() == null : true)) {
            throw new IllegalArgumentException("Property " + pd.getName() +
                    " is read-only");
        }

        if (index >= 0) {
            // set an indexed property
            if (ipd == null) {
                throw new IllegalArgumentException("Property " + pd.getName() +
                        " is not indexed");
            }

            Object array = _map.get(pd.getName());
            Array.set(array, index, value);
        }
        else {
            _map.put(pd.getName(), value);
        }
    }

}
