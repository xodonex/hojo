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
package org.xodonex.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * This class primarily contains methods to circumvent the shortcomings of
 * {@link java.lang.Class#getConstructor(Class[])} and
 * {@link java.lang.Class#getMethod(String, Class[])}: in the current
 * implementation (JDK-1.2.1-A), <code>getConstructor()</code> cannot properly
 * match interfaces and subclasses. Additionally, both
 * <code>getConstructor()</code> and <code>
 * getMethod()</code> fail to match a primitive type to a wrapper class.
 *
 * @author Henrik Lauritzen
 * @version 1.00, 20/05/1999
 */
public final class ReflectUtils {

    // Number type conversion priorities
    public final static int NUM_PRI_BAD = -1;
    public final static int NUM_PRI_BYTE = 0;
    public final static int NUM_PRI_SHORT = 1;
    public final static int NUM_PRI_INT = 2;
    public final static int NUM_PRI_LONG = 3;
    public final static int NUM_PRI_FLOAT = 4;
    public final static int NUM_PRI_DOUBLE = 5;
    public final static int NUM_PRI_BINT = 6;
    public final static int NUM_PRI_BDEC = 7;

    // Comparator for argument lists
    public final static Comparator ARG_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            Class[] a1 = (Class[])o1;
            Class[] a2 = (Class[])o2;
            int result;

            if ((result = compInt(a1.length, a2.length)) != 0) {
                return result;
            }

            for (int i = 0; i < a1.length; i++) {
                if ((result = compInt(a1[i].hashCode(),
                        a2[i].hashCode())) != 0) {
                    return result;
                }
            }
            return 0;
        }

        int compInt(int i1, int i2) {
            return i1 < i2 ? -1 : i1 > i2 ? 1 : 0;
        }
    };

    private final static int collateModifier(int mod) {
        int result = 0;

        if ((mod & Modifier.PUBLIC) != 0) {
            result += 32;
        }
        else if ((mod & Modifier.PROTECTED) != 0) {
            result += 16;
        }
        else if ((mod & Modifier.PRIVATE) != 0) {
            result += 4;
        }
        else {
            result += 8;
        }

        if ((mod & Modifier.STATIC) != 0) {
            result += 2;
        }
        if ((mod & Modifier.ABSTRACT) != 0) {
            result += 1;
        }

        return result;
    }

    private final static int compareMod(Member m1, Member m2) {
        int m1m = collateModifier(m1.getModifiers());
        int m2m = collateModifier(m2.getModifiers());
        return m1m < m2m ? -1 : m1m > m2m ? 1 : 0;
    }

    // Comparator for methods
    public final static Comparator METHOD_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            Method m1 = (Method)o1;
            Method m2 = (Method)o2;
            int result = compareMod(m1, m2);
            if (result != 0) {
                return result;
            }

            Collator c = Collator.getInstance();
            if ((result = c.compare(m1.getName(), m2.getName())) != 0) {
                return result;
            }
            else {
                return ARG_COMPARATOR.compare(m1.getParameterTypes(),
                        m2.getParameterTypes());
            }
        }
    };

    // Comparator for constructors
    public final static Comparator CONSTRUCTOR_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            Constructor c1 = (Constructor)o1;
            Constructor c2 = (Constructor)o2;
            int r = compareMod(c1, c2);
            if (r != 0) {
                return r;
            }
            return ARG_COMPARATOR.compare(c1.getParameterTypes(),
                    c2.getParameterTypes());
        }
    };

    // Comparator for fields
    public final static Comparator FIELD_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            Field f1 = (Field)o1;
            Field f2 = (Field)o2;
            int r = compareMod(f1, f2);

            if (r != 0) {
                return r;
            }
            Collator c = Collator.getInstance();
            return c.compare(f1.getName(), f2.getName());
        }
    };

    // Object.class
    private final static Class OBJECT_CLASS = Object.class;

    // primitive class -m-> wrapper class
    private final static HashMap wrapperClasses = new HashMap();

    // wrapper class -m-> primitive class
    private final static HashMap unwrapClasses = new HashMap();

    // primitive class -m-> single character name
    private final static HashMap primitiveNames = new HashMap();

    // primitive class name -m-> primitive class
    private final static HashMap primitiveClasses = new HashMap();

    // Class -> conversion priority
    private final static HashMap priorities = new HashMap();

    // Conversion priority -> Class
    private final static Class[] numberClasses = {
            Byte.class, Short.class, Integer.class, Long.class,
            Float.class, Double.class, BigInteger.class, BigDecimal.class
    };

    // Conversion priority -> decimal or integral
    @SuppressWarnings("unused")
    private final static boolean[] isDecimal = {
            false, false, false, false, true, true, false, true
    };

    // The primitive number types
    private final static Class[] primitiveNumbers = {
            Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE,
            Double.TYPE
    };

    // Weights for matchArguments()
    private final static int WT_NONE = Integer.MAX_VALUE;
    private final static int WT_EQUAL = 0;
    private final static int WT_WRAPPER = 1;
    private final static int WT_WIDENING = 2;
    private final static int WT_ASSIGNABLE = 5;

    static {
        Class c = Boolean.class;
        wrapperClasses.put(Boolean.TYPE, c);
        unwrapClasses.put(c, Boolean.TYPE);
        primitiveNames.put(Boolean.TYPE, "Z");

        c = Byte.class;
        wrapperClasses.put(Byte.TYPE, c);
        unwrapClasses.put(c, Byte.TYPE);
        primitiveNames.put(Byte.TYPE, "B");
        Integer i = new Integer(NUM_PRI_BYTE);
        priorities.put(Byte.TYPE, i);
        priorities.put(c, i);

        c = Short.class;
        wrapperClasses.put(Short.TYPE, c);
        unwrapClasses.put(c, Short.TYPE);
        primitiveNames.put(Short.TYPE, "S");
        i = new Integer(NUM_PRI_SHORT);
        priorities.put(Short.TYPE, i);
        priorities.put(c, i);

        c = Character.class;
        wrapperClasses.put(Character.TYPE, c);
        unwrapClasses.put(c, Character.TYPE);
        primitiveNames.put(Character.TYPE, "C");

        c = Integer.class;
        wrapperClasses.put(Integer.TYPE, c);
        unwrapClasses.put(c, Integer.TYPE);
        primitiveNames.put(Integer.TYPE, "I");
        i = new Integer(NUM_PRI_INT);
        priorities.put(Integer.TYPE, i);
        priorities.put(c, i);

        c = Long.class;
        wrapperClasses.put(Long.TYPE, c);
        unwrapClasses.put(c, Long.TYPE);
        primitiveNames.put(Long.TYPE, "J");
        i = new Integer(NUM_PRI_LONG);
        priorities.put(Long.TYPE, i);
        priorities.put(c, i);

        c = Float.class;
        wrapperClasses.put(Float.TYPE, c);
        unwrapClasses.put(c, Float.TYPE);
        primitiveNames.put(Float.TYPE, "F");
        i = new Integer(NUM_PRI_FLOAT);
        priorities.put(Float.TYPE, i);
        priorities.put(c, i);

        c = Double.class;
        wrapperClasses.put(Double.TYPE, c);
        unwrapClasses.put(c, Double.TYPE);
        primitiveNames.put(Double.TYPE, "D");
        i = new Integer(NUM_PRI_DOUBLE);
        priorities.put(Double.TYPE, i);
        priorities.put(c, i);

        c = BigInteger.class;
        i = new Integer(NUM_PRI_BINT);
        priorities.put(c, i);

        c = BigDecimal.class;
        i = new Integer(NUM_PRI_BDEC);
        priorities.put(c, i);

        for (int j = primitiveNumbers.length - 1; j >= 0; j--) {
            primitiveClasses.put(primitiveNumbers[j].getName(),
                    primitiveNumbers[j]);
        }
        primitiveClasses.put(Boolean.TYPE, "boolean");
        primitiveClasses.put(Character.TYPE, "char");
        primitiveClasses.put(Void.TYPE, "void");
    }

    private ReflectUtils() {
    }

    /**
     * Converts the given array to a string representation of an argument list,
     * ie. as the class names separated by commas contained within parentheses.
     *
     * @param args
     *            The argument list
     * @param delimiter
     *            the delimiter for types
     * @return A string representation of the argument list
     */
    public static String typeList2String(Class[] args, String delimiter) {
        if (args == null) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                result.append(delimiter);
            }
            result.append((args[i] == null) ? null : className2Java(args[i]));
        }

        return result.toString();
    }

    // returns true, iff the type list and argument list match
    private static int matchArguments(Class[] types, Class[] args) {
        if (types.length != args.length) {
            return WT_NONE;
        }

        int weight = 0;
        int t_pri;
        Class t_c, a_c;

        loop: for (int i = 0; i < args.length; i++) {
            t_c = types[i];
            a_c = args[i];

            if (a_c == t_c) {
                // Exact match
                weight += WT_EQUAL;
                continue;
            }

            // null matches any reference type value
            if (a_c == null) {
                if (t_c.isPrimitive()) {
                    return WT_NONE;
                }
                weight += WT_EQUAL;
                continue;
            }

            if (t_c.isPrimitive()) {
                if (a_c.isPrimitive()) {
                    // Both types are primitive but not equal - check whether a
                    // widening
                    // conversion is possible.
                    if ((t_c == Boolean.TYPE) || (t_c == Character.TYPE)) {
                        return WT_NONE; // incompatible
                    }

                    t_pri = ((Integer)priorities.get(t_c)).intValue();
                    if ((t_pri >= NUM_PRI_INT) && (a_c == Character.TYPE)) {
                        // char can be converted to int, long, float or double
                        weight += WT_WIDENING * (t_pri - NUM_PRI_SHORT);
                        continue;
                    }
                    for (int j = 0; j < t_pri; j++) {
                        if (primitiveNumbers[j] == a_c) {
                            // A widening conversion is possible
                            weight += WT_WIDENING * (t_pri - j) + WT_WRAPPER;
                            continue loop;
                        }
                    }

                    // a narrowing conversion is necessary
                    return WT_NONE;
                }
                else {
                    // Get the wrapper class for the primitive type
                    t_c = (Class)wrapperClasses.get(t_c);
                    // t_name = t_c.getName();

                    if (t_c == a_c) {
                        weight += WT_WRAPPER;
                        continue;
                    }
                    else if ((t_c == Boolean.class)
                            || (t_c == Character.class)) {
                        // Can't convert to boolean or char
                        return WT_NONE;
                    }

                    // Check whether the a widening conversion is possible
                    t_pri = ((Integer)priorities.get(t_c)).intValue();
                    if ((t_pri >= NUM_PRI_INT) && (a_c == Character.class)) {
                        // Character can be converted to int, long, float or
                        // double
                        weight += WT_WIDENING * (t_pri - NUM_PRI_SHORT)
                                + WT_WRAPPER;
                        continue;
                    }

                    for (int j = 0; j < t_pri; j++) {
                        if (numberClasses[j] == a_c) {
                            // A widening conversion is possible
                            weight += WT_WIDENING * (t_pri - j) + WT_WRAPPER;
                            continue loop;
                        }
                    }

                    // A widening conversion is not possible
                    return WT_NONE;
                }
            }
            else if (t_c.isAssignableFrom(a_c)) {
                weight += WT_ASSIGNABLE;
            }
            else if (a_c.isPrimitive()) {
                a_c = (Class)wrapperClasses.get(a_c);
                if (t_c == a_c) {
                    weight += WT_WRAPPER;
                    continue;
                }
                else if (t_c.isAssignableFrom(a_c)) {
                    weight += WT_ASSIGNABLE + WT_WRAPPER;
                }
                else {
                    return WT_NONE;
                }
            }
            else {
                // Not compatible
                return WT_NONE;
            }
        }

        return weight;
    }

    /**
     * Returns the Java class object representing the given fully qualified
     * class name or primitive type name. The class name is not permitted to
     * contain brackets, use {@link #getArrayClass(String, int)} to convert
     * array class names.
     *
     * @exception ClassNotFoundException
     *                if the given name does not reference a Java class.
     * @param name
     *            the clas name
     * @return the class for the given name
     * @throws ClassNotFoundException
     *             if the class cannot be resolved
     */
    public static Class getClass(String name) throws ClassNotFoundException {
        Class result;
        if ((result = (Class)primitiveClasses.get(name)) != null) {
            return result;
        }
        return Class.forName(name);
    }

    /**
     * Returns the class name for the given class and array dimension.
     *
     * @param base
     *            The class/type whose name should be converted
     * @param dim
     *            The number of array indices to use in the class name.
     * @return The class name for an array of dimension <code>dim</code>, if
     *         <code>dim</code> is greater than 0, <code>cls.getName()</code>
     *         otherwise.
     * @exception NullPointerException
     *                if <code>cls</code> is <code>null</code>.
     */
    public static String getArrayClassName(Class base, int dim)
            throws NullPointerException {

        if (dim <= 0) {
            return base.getName();
        }

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < dim; i++) {
            result.append('[');
        }
        String n = (String)primitiveNames.get(base);
        result.append((n == null) ? 'L' + base.getName() + ';' : n);
        return result.toString();
    }

    public static Class getBaseClass(Class array) {
        Class c = null;
        while (array != null) {
            c = array;
            array = array.getComponentType();
        }
        return c == null ? OBJECT_CLASS : c;
    }

    public static int getArrayDimensions(Class array)
            throws NullPointerException,
            IllegalArgumentException {
        if (!array.isArray()) {
            throw new IllegalArgumentException("" + array);
        }

        char[] nameChars = array.getName().toCharArray();
        int result = 0;
        int i = 0;

        while (nameChars[i++] == '[') {
            // no class name can be all ['s, so no boundary check is necessary
            result++;
        }

        return result;
    }

    public static Class getArrayClass(String base, int dim)
            throws ClassNotFoundException, NullPointerException {
        return getArrayClass(getClass(base), dim);
    }

    public static Class getArrayClass(Class base, int dim)
            throws NullPointerException {

        if (dim <= 0) {
            return base;
        }

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < dim; i++) {
            result.append('[');
        }

        if (base.isArray()) {
            result.append(base.getName());
        }
        else {
            String n = (String)primitiveNames.get(base);
            result.append((n == null) ? 'L' + base.getName() + ';' : n);
        }
        try {
            return Class.forName(result.toString());
        }
        catch (Exception e) {
            // This should never happen
            return null;
        }
    }

    public static String className2Java(Class cls) throws NullPointerException {
        String s = cls.getName();

        // Non-array class?
        if (s.charAt(0) != '[') {
            return s;
        }

        // Get the dimensions
        StringBuffer dim = new StringBuffer(4);
        int classPos = 0;
        while (s.charAt(classPos) == '[') {
            classPos++;
            dim.append("[]");
        }

        // Get the base class name
        StringBuffer result = new StringBuffer();
        switch (s.charAt(classPos)) {
        case 'Z':
            result.append("boolean");
            break;
        case 'B':
            result.append("byte");
            break;
        case 'S':
            result.append("short");
            break;
        case 'C':
            result.append("char");
            break;
        case 'I':
            result.append("int");
            break;
        case 'J':
            result.append("long");
            break;
        case 'F':
            result.append("float");
            break;
        case 'D':
            result.append("double");
            break;
        default: // 'L'
            result.append(s.substring(classPos + 1, s.length() - 1));
            break;
        }

        return result.append(dim).toString();
    }

    /**
     * Returns the primitive class corresponding to the given wrapper class.
     *
     * @param wrapper
     *            a wrapper class The wrapper <code>Class</code> for a primitive
     *            data type
     * @return the <code>Class</code> for the primitive type corresponding to
     *         <code>wrapper</code>, or <code>null</code>, if
     *         <code>wrapper</code> is not a valid wrapper <code>Class</code>.
     */
    public static Class getPrimitive(Class wrapper) {
        return (wrapper == null) ? null : (Class)unwrapClasses.get(wrapper);
    }

    /**
     * @param wrapper
     *            a wrapper class
     * @return the primitive class corresponding to the given wrapper class, or
     *         the given class itself, if this is not a wrapper class.
     */
    public static Class unwrap(Class wrapper) {
        Class c = (Class)unwrapClasses.get(wrapper);
        return c == null ? wrapper : c;
    }

    /**
     * Return the default value for the given class. The default value is null
     * for all reference types (and void), and is different from null otherwise.
     *
     * @param c
     *            the class
     * @return the class' default value
     */
    public static Object getDefaultValue(Class c) {
        if (c == Void.TYPE || !c.isPrimitive()) {
            return null;
        }
        else if (c == Boolean.TYPE) {
            return Boolean.FALSE;
        }
        else if (c == Character.TYPE) {
            return ConvertUtils.ZERO_CHAR;
        }
        else {
            switch (getPriority(c)) {
            case NUM_PRI_BYTE:
                return ConvertUtils.ZERO_BYTE;
            case NUM_PRI_SHORT:
                return ConvertUtils.ZERO_SHORT;
            case NUM_PRI_INT:
                return ConvertUtils.ZERO_INT;
            case NUM_PRI_LONG:
                return ConvertUtils.ZERO_LONG;
            case NUM_PRI_FLOAT:
                return ConvertUtils.ZERO_FLOAT;
            default:
                return ConvertUtils.ZERO_DOUBLE;
            }
        }
    }

    /**
     * Returns the wrapper class corresponding to the given primitive class.
     *
     * @param primitive
     *            the <code>Class</code> for a primitive data type
     * @return the wrapper <code>Class</code> corresponding to <code>
     * primitive</code>, or <code>null</code>, if <code>primitive</code> is not
     *         a valid <code>Class</code> for a primitive type.
     */
    public static Class getWrapper(Class primitive) {
        return (primitive == null) ? null
                : (Class)wrapperClasses.get(primitive);
    }

    /**
     * @param primitive
     *            a primitive class
     * @return the wrapper class corresponding to the given primitive class, or
     *         the given class itself, if it is not a primitive class for which
     *         a wrapper exists.
     */
    public static Class wrap(Class primitive) {
        Class c = (Class)wrapperClasses.get(primitive);
        return c == null ? primitive : c;
    }

    /**
     * @param c
     *            a class
     * @return whether the given class is a Number class
     */
    public static boolean isNumberClass(Class c) {
        return (c == null) ? false : priorities.containsKey(c);
    }

    /**
     * @param n
     *            a number
     * @return the numeric conversion priority of the given number
     */
    public static int getPriority(Number n) {
        if (n == null) {
            return NUM_PRI_BAD;
        }
        else {
            return getPriority(n.getClass());
        }
    }

    public static Class getResultClass(Number n1, Number n2) {
        return getNumberClass(getResultPriority(n1, n2));
    }

    public static int getResultPriority(Number n1, Number n2) {
        return getResultPriority(getPriority(n1), getPriority(n2));
    }

    public static Class getNumberClass(int pri) {
        if (pri < 0 || pri > NUM_PRI_BDEC) {
            return null;
        }
        else {
            return numberClasses[pri];
        }
    }

    public static int getPriority(Class c) {
        Integer i;
        i = (Integer)priorities.get(c);
        return (i == null) ? NUM_PRI_BAD : i.intValue();
    }

    public static int getResultPriority(int pri1, int pri2) {
        if ((pri1 | pri2) == NUM_PRI_BAD) {
            return NUM_PRI_BAD;
        }
        else {
            return (pri1 > pri2) ? pri1 : pri2;
        }
    }

    public static Number convertTo(Number n, int result_pri) {
        if ((n == null) || (result_pri < NUM_PRI_BYTE) ||
                (result_pri > NUM_PRI_BDEC)) {
            return n;
        }

        int start_pri = getPriority(n);
        if (start_pri == result_pri) {
            return n;
        }

        switch (result_pri) {
        case NUM_PRI_BYTE:
            return new Byte(n.byteValue());
        case NUM_PRI_SHORT:
            return new Short(n.shortValue());
        case NUM_PRI_INT:
            return new Integer(n.intValue());
        case NUM_PRI_LONG:
            return new Long(n.longValue());
        case NUM_PRI_FLOAT:
            return new Float(n.floatValue());
        case NUM_PRI_DOUBLE:
            return new Double(n.doubleValue());
        case NUM_PRI_BINT:
            if (start_pri <= NUM_PRI_LONG) {
                return BigInteger.valueOf(n.longValue());
            }
            else if (start_pri == NUM_PRI_BDEC) {
                return ((BigDecimal)n).toBigInteger();
            }
            else {
                return new BigDecimal(n.doubleValue()).toBigInteger();
            }
        case NUM_PRI_BDEC:
            if (start_pri <= NUM_PRI_LONG) {
                return new BigDecimal(n.longValue());
            }
            else if (start_pri == NUM_PRI_BINT) {
                return new BigDecimal((BigInteger)n);
            }
            else {
                return new BigDecimal(n.doubleValue());
            }
        default:
            throw new NullPointerException();
        }
    }

    public static Number convertTo(long l, int result_pri) {
        if ((result_pri < NUM_PRI_BYTE) || (result_pri > NUM_PRI_BDEC)) {
            return null;
        }
        switch (result_pri) {
        case NUM_PRI_BYTE:
            return new Byte((byte)l);
        case NUM_PRI_SHORT:
            return new Short((short)l);
        case NUM_PRI_INT:
            return new Integer((int)l);
        case NUM_PRI_LONG:
            return new Long(l);
        case NUM_PRI_FLOAT:
            return new Float(l);
        case NUM_PRI_BAD:
        case NUM_PRI_DOUBLE:
            return new Double(l);
        case NUM_PRI_BINT:
            return BigInteger.valueOf(l);
        default: // NUM_PRI_BDEC
            return BigDecimal.valueOf(l);
        }
    }

    public static Number convertTo(double d, int result_pri) {
        if ((result_pri < NUM_PRI_BYTE) || (result_pri > NUM_PRI_BDEC)) {
            return null;
        }
        switch (result_pri) {
        case NUM_PRI_BYTE:
            return new Byte((byte)d);
        case NUM_PRI_SHORT:
            return new Short((short)d);
        case NUM_PRI_INT:
            return new Integer((int)d);
        case NUM_PRI_LONG:
            return new Long((long)d);
        case NUM_PRI_FLOAT:
            return new Float((float)d);
        case NUM_PRI_BAD:
        case NUM_PRI_DOUBLE:
            return new Double(d);
        case NUM_PRI_BINT:
            return new BigDecimal(d).toBigInteger();
        default: // NUM_PRI_BDEC
            return new BigDecimal(d);
        }
    }

    /**
     * Searches through all the public constructors of <code>cls</code> until a
     * constructor matching the given argument list is found. <i>This method
     * should be deprecated when the implementation of
     * </i><code>java.lang.Class.getConstructor()</code> <i>improves.</i>
     *
     * @param cls
     *            The <code>Class</code> of the object whose constructor is to
     *            be returned.
     * @param args
     *            An array of <code>Class</code>es reflecting the constructor's
     *            argument types (<code>{}</code> for <code>void</code>). A
     *            <code>null</code> value matches any object/interface (but
     *            might cause an exception when the constructor is used).
     * @return the <i>first found</i> declared public constructor matching the
     *         argument list (the search order is arbitrary)
     * @exception NoSuchMethodException
     *                if no matching constructor can be found
     * @see java.lang.Class#getConstructor
     */
    public static Constructor getMatchingConstructor(Class cls, Class[] args)
            throws NoSuchMethodException {
        Constructor result = findMatchingConstructor(cls, args);
        if (result == null) {
            throw new NoSuchMethodException(cls.getName() + "(" +
                    typeList2String(args, ", ") + ")");
        }
        else {
            return result;
        }
    }

    public static Constructor findMatchingConstructor(Class cls, Class[] args) {
        Constructor[] cnst = cls.getConstructors();
        Constructor result = null;
        int weight = WT_NONE;
        int tmp;

        for (int i = 0; i < cnst.length; i++) {
            tmp = matchArguments(cnst[i].getParameterTypes(), args);
            if (tmp <= WT_WRAPPER) {
                return cnst[i];
            }
            else if (tmp < weight) {
                result = cnst[i];
                weight = tmp;
            }
        }

        return (weight < WT_NONE) ? result : null;
    }

    /**
     * Searches through all the public methods of <code>cls</code> until a
     * method matching the given argument list is found. <i>This method should
     * be deprecated when the implementation of
     * </i><code>java.lang.Class.getMethod()</code> <i>improves.</i>
     *
     * @param cls
     *            The <code>Class</code> of the object whose method is to be
     *            returned.
     * @param name
     *            The name of the method to search for
     * @param args
     *            An array of <code>Class</code>es reflecting the method's
     *            argument types (<code>{}</code> for <code>void</code>). A
     *            <code>null</code> value matches any object/interface (but
     *            might cause an exception when the methods is invoked).
     * @return the <i>first found</i> declared public method matching the
     *         argument list (the search order is arbitrary).
     * @exception NoSuchMethodException
     *                if no matching method can be found
     * @see java.lang.Class#getMethod
     */
    public static Method getMatchingMethod(Class cls, String name, Class[] args)
            throws NoSuchMethodException {
        Method result = findMatchingMethod(cls, name, args);
        if (result == null) {
            throw new NoSuchMethodException(cls.getName() + "." + name + "(" +
                    typeList2String(args, ", ") + ")");
        }
        else {
            return result;
        }
    }

    public static Method findMatchingMethod(Class cls, String name,
            Class[] args) {
        if (cls == null) {
            // no match
            return null;
        }
        else if (cls.isArray()) {
            // match array classes as objects - reflection doesn't do this
            return findMatchingMethod(OBJECT_CLASS, name, args);
        }

        int mod = cls.getModifiers();
        Method result = null;

        if ((mod & Modifier.PUBLIC) == 0) {
            // Try any interfaces
            Class[] intf = cls.getInterfaces();
            for (int i = 0; i < intf.length; i++) {
                if ((result = tryMatchingMethod(intf[i], name, args)) != null) {
                    return result;
                }
            }

            // Try the superclass
            return findMatchingMethod(cls.getSuperclass(), name, args);
        }
        else {
            // The class is public - do an ordinary search for the method
            if ((result = tryMatchingMethod(cls, name, args)) != null) {
                return result;
            }
            // Try the methods of Object.class, if an interface was given and no
            // method was found
            return ((mod & Modifier.INTERFACE) != 0)
                    ? tryMatchingMethod(OBJECT_CLASS, name, args)
                    : null;
        }
    }

    private static Method tryMatchingMethod(Class cls, String name,
            Class[] args) {
        Method[] methods = cls.getMethods();
        Method result = null;
        int weight = WT_NONE;
        int tmp;

        for (int i = 0; i < methods.length; i++) {
            if (!methods[i].getName().equals(name)) {
                continue;
            }

            tmp = matchArguments(methods[i].getParameterTypes(), args);
            if (tmp <= WT_WRAPPER) {
                return methods[i];
            }
            else if (tmp < weight) {
                result = methods[i];
                weight = tmp;
            }
        }

        return (weight < WT_NONE) ? result : null;
    }

    /**
     * Converts an array of values to an argument list, ie. an array of
     * <code>Class</code>es reflecting the types of the values. A
     * <code>null</code> value is treated as <code>
     * java.lang.Object</code>.
     *
     * @param arguments
     *            the argument values
     * @return the argument types
     */
    public static Class[] getTypeList(Object[] arguments) {
        Class[] result = new Class[arguments.length];

        for (int i = 0; i < arguments.length; i++) {
            result[i] = (arguments[i] == null) ? null : arguments[i].getClass();
        }

        return result;
    }

    /**
     * Show the signature of a field in Java syntax
     *
     * @param f
     *            the field
     * @return the field's signature
     */
    public static StringBuffer showSignature(Field f) {
        // don't show implicit 'public final' modifiers on interface fields,
        // don't show transient or volatile modifiers
        int classMod = f.getDeclaringClass().getModifiers();
        int filter = ~(((classMod & Modifier.INTERFACE) != 0
                ? Modifier.FINAL | Modifier.STATIC
                : 0) |
                Modifier.TRANSIENT | Modifier.VOLATILE);

        int mod = f.getModifiers();
        Class fc = f.getType();

        StringBuffer result = new StringBuffer(Modifier.toString(mod & filter));
        result.append(' ').append(className2Java(fc)).append(' ')
                .append(f.getName());
        if ((mod & (Modifier.STATIC & Modifier.FINAL)) == (Modifier.STATIC
                & Modifier.FINAL)) {
            try {
                // add the value of final static fields of primitive types,
                // or of type String.
                if (fc.isPrimitive()) {
                    Object o = f.get(null);

                    if (fc == Character.TYPE) {
                        // format as a character literal
                        result.append(" = '").append(StringUtils.toJavaChar(
                                ((Character)o).charValue(), true))
                                .append('\'');
                    }
                    else {
                        result.append(" = ").append(o);
                    }
                }
                else if (fc == String.class) {
                    String val = (String)f.get(null);
                    if (val == null) {
                        result.append(" = null");
                    }
                    else {
                        result.append(" = \"").append(StringUtils.toJavaString(
                                val, true)).append('\"');
                    }
                }
            }
            catch (Exception e) {
            }
        }

        return result;
    }

    /**
     * Show the signature of a constructor in Java syntax.
     *
     * @param c
     *            the constructor
     * @return the concstuctor's signature
     */
    public static StringBuffer showSignature(Constructor c) {
        StringBuffer result = new StringBuffer();
        return result.append(Modifier.toString(c.getModifiers())).append(' ')
                .append(c.getName()).append('(')
                .append(typeList2String(c.getParameterTypes(), ", "))
                .append(')');
    }

    /**
     * Show the signature of a method in Java syntax.
     *
     * @param m
     *            the method
     * @return the method's signature
     */
    public static StringBuffer showSignature(Method m) {
        // don't show abstract modifiers on interface members, and don't show
        // native or synchronized modifiers
        int classMod = m.getDeclaringClass().getModifiers();
        int filter = ~(((classMod & Modifier.INTERFACE) != 0 ? Modifier.ABSTRACT
                : 0) | Modifier.NATIVE | Modifier.SYNCHRONIZED);
        int mod = m.getModifiers();

        StringBuffer result = new StringBuffer();
        result.append(Modifier.toString(mod & filter)).append(' ')
                .append(className2Java(m.getReturnType())).append(' ')
                .append(m.getName()).append('(')
                .append(typeList2String(m.getParameterTypes(), ", "))
                .append(')');

        Class[] exTypes = m.getExceptionTypes();
        if (exTypes.length > 0) {
            result.append(" throws ").append(typeList2String(exTypes, ", "));
        }

        return result;
    }

    /**
     * Show the signature of a Class in Java syntax.
     *
     * @param c
     *            the class
     * @return the class's signature
     */
    public static StringBuffer showSignature(Class c) {
        StringBuffer result = new StringBuffer();

        int mod = c.getModifiers();
        if (c.isInterface()) {
            result.append(Modifier.toString(mod & ~Modifier.ABSTRACT))
                    .append(' ');
        }
        else {
            result.append(Modifier.toString(mod)).append(" class ");
        }
        result.append(c.getName());

        Class cSuper = c.getSuperclass();
        if (cSuper != null && cSuper != OBJECT_CLASS) {
            result.append(" extends " + cSuper.getName());
        }

        Class[] ifces = c.getInterfaces();
        if (ifces.length > 0) {
            result.append(" implements ");
            for (int i = 0; i < ifces.length; i++) {
                result.append(ifces[i].getName());
                if (i < ifces.length - 1) {
                    result.append(", ");
                }
            }
        }

        return result;
    }

    private static void classifyMembers(Member[] members, Set target,
            Matcher discriminator, int visibility, int required, int excluded) {
        for (int i = 0; i < members.length; i++) {
            Member m = members[i];
            int mod = m.getModifiers();
            if ((mod & required) != required) {
                continue;
            }
            if ((mod & excluded) != 0) {
                continue;
            }

            int v = mod
                    & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE);
            switch (visibility) {
            case 0:
                if (v != Modifier.PUBLIC) {
                    continue;
                }
                break;
            case 1:
                if (v != Modifier.PUBLIC && v != Modifier.PROTECTED) {
                    continue;
                }
                break;
            case 2:
                if (v != 0) {
                    continue;
                }
                break;
            }

            if (discriminator != null) {
                discriminator.reset(m.getName());
                if (!discriminator.matches()) {
                    continue;
                }
            }

            target.add(m);
        }
    }

    /**
     * List the members of a given class
     *
     * @param c
     *            the class whose members should be listed
     * @param result
     *            an array of 3 elemens whose contents will be modified by the
     *            method to hold the relevant data. The array contents are
     *            interpreted as follows:
     *            <ol>
     *            <li value="0">Fields
     *            <li value="1">Constructors
     *            <li value="2">Methods
     *            </ol>
     *            If either set has the value null, then the corresponding
     *            members will not be listed
     * @param searchDepth
     *            determines how far back in the class hierarchy the listing
     *            should progress. A value of 0 lists only the declared members
     *            of the given class, a value of 1 also lists members of the
     *            interfaces and superclass of the given class, etc.
     * @param discriminator
     *            when the value is not null, only members having a name
     *            matching the pattern will be listed
     * @param visibility
     *            determines which member visibilies should be listed. The value
     *            is interpreted as follows:
     *            <ol>
     *            <li value="0">List only public members
     *            <li value="1">List public and protected members
     *            <li value="2">List public, protected and package-private
     *            members
     *            <li value="3">List all members
     *            </ol>
     * @param required
     *            Determines the modifiers which must be found on all listed
     *            members
     * @param excluded
     *            Determines the modifiers which cannot be found on any listed
     *            members
     * @return the modified result set
     */
    public static Set[] listMembers(Class c, Set[] result, int searchDepth,
            Matcher discriminator, int visibility, int required, int excluded) {
        if (searchDepth < 0 || c == null) {
            // max recursion depth - done
            return result;
        }

        // find the subordinate results first
        if (searchDepth > 0) {
            // prevent subclass constructors from being listed
            Set tmpConstr = result[1];
            result[1] = null;

            // list the superclass' members
            listMembers(c.getSuperclass(), result, searchDepth - 1,
                    discriminator, visibility, required, excluded);

            // prevent methods specified by interfaces from being listed
            Set tmpMeth = result[2];
            result[2] = null;

            Class[] ifs = c.getInterfaces();
            for (int i = 0; i < ifs.length; i++) {
                listMembers(ifs[i], result, searchDepth - 1,
                        discriminator, visibility, required, excluded);
            }

            result[1] = tmpConstr;
            result[2] = tmpMeth;
        }

        // check the declared fields in the class
        if (result[0] != null) {
            classifyMembers(c.getDeclaredFields(), result[0], discriminator,
                    visibility, required, excluded);
        }
        if (result[1] != null) {
            classifyMembers(c.getConstructors(), result[1], discriminator,
                    visibility, required, excluded);
        }
        if (result[2] != null) {
            classifyMembers(c.getDeclaredMethods(), result[2], discriminator,
                    visibility, required, excluded);
        }

        return result;
    }

    /**
     * Serialize a method.
     *
     * @param m
     *            the method
     * @param out
     *            the target stream
     * @throws IOException
     *             on I/O error
     */
    public static void writeMethod(Method m, ObjectOutputStream out)
            throws IOException {
        if (m == null) {
            out.write(null);
            return;
        }

        out.writeObject(m.getDeclaringClass());
        out.writeObject(m.getName());
        out.writeObject(m.getParameterTypes());
    }

    /**
     * Deserialize a method serialized by
     * {@link #writeMethod(Method, ObjectOutputStream)}.
     *
     * @param in
     *            the source stream
     * @return the deserialized method
     * @throws IOException
     *             on I/O error
     * @throws ClassNotFoundException
     *             on deserialization error
     */
    public static Method readMethod(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        Class base = (Class)in.readObject();
        if (base == null) {
            return null;
        }

        String name = (String)in.readObject();
        Class[] paramTypes = (Class[])in.readObject();

        try {
            return base.getDeclaredMethod(name, paramTypes);
        }
        catch (NoSuchMethodException e) {
            throw new IOException(e.getMessage());
        }
    }

}
