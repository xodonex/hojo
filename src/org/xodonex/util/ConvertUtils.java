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

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.xodonex.util.os.OsInterface;
import org.xodonex.util.struct.iterator.ArrayIterator;
import org.xodonex.util.struct.iterator.CharSequenceIterator;
import org.xodonex.util.struct.iterator.EnumerationIterator;
import org.xodonex.util.struct.iterator.PrimitiveArrayIterator;

/**
 * A library on data-type conversion routines.
 */
public class ConvertUtils {

    public final static Byte ZERO_BYTE = new Byte((byte)0);
    public final static Byte FALSE_BYTE = ZERO_BYTE;
    public final static Byte TRUE_BYTE = new Byte((byte)-1);

    public final static Short ZERO_SHORT = new Short((short)0);
    public final static Short FALSE_SHORT = ZERO_SHORT;
    public final static Short TRUE_SHORT = new Short((short)-1);

    public final static Character ZERO_CHAR = new Character('\0');
    public final static Character FALSE_CHAR = ZERO_CHAR;
    public final static Character TRUE_CHAR = new Character('\uffff');

    public final static Integer ZERO_INT = new Integer(0);
    public final static Integer ONE_INT = new Integer(1);
    public final static Integer FALSE_INT = ZERO_INT;
    public final static Integer TRUE_INT = new Integer(-1);

    public final static Long ZERO_LONG = new Long(0L);
    public final static Long FALSE_LONG = ZERO_LONG;
    public final static Long TRUE_LONG = new Long(-1L);

    public final static Date FALSE_DATE = new Date(0L);
    public final static Date TRUE_DATE = new Date(-1L);

    public final static Float ZERO_FLOAT = new Float(0.0f);
    public final static Float FALSE_FLOAT = ZERO_FLOAT;
    public final static Float TRUE_FLOAT = new Float(-1.0f);

    public final static Double ZERO_DOUBLE = new Double(0.0);
    public final static Double FALSE_DOUBLE = ZERO_DOUBLE;
    public final static Double TRUE_DOUBLE = new Double(-1.0);

    public final static BigInteger ZERO_BINT = BigInteger.valueOf(0); // BigInteger.ZERO;
    public final static BigInteger ONE_BINT = BigInteger.valueOf(1); // BigInteger.ONE;
    public final static BigInteger FALSE_BINT = ZERO_BINT;
    public final static BigInteger TRUE_BINT = BigInteger.valueOf(-1);
    public final static BigInteger NEG_ONE_BINT = TRUE_BINT;

    public final static BigDecimal ZERO_BDEC = new BigDecimal(ZERO_BINT);
    public final static BigDecimal ONE_BDEC = BigDecimal.valueOf(1);
    public final static BigDecimal FALSE_BDEC = ZERO_BDEC;
    public final static BigDecimal TRUE_BDEC = new BigDecimal(TRUE_BINT);
    public final static BigDecimal NEG_ONE_BDEC = TRUE_BDEC;

    // indicates that no direct number conversion is possible
    public final static Number NaN = new Integer(-1);

    public final static Class DEFAULT_COLLECTION_CLASS;
    public final static Class DEFAULT_LIST_CLASS;
    public final static Class DEFAULT_SET_CLASS;
    public final static Class DEFAULT_MAP_CLASS;

    static {
        Class coll, list, map, set;

        coll = list = ArrayList.class;

        try {
            String ver = System.getProperty("java.vm.version");
            if (ver.charAt(0) > '1' || ver.charAt(2) >= '4') {
                // running 1.4 or later?
                map = Class.forName("java.util.LinkedHashMap");
                set = Class.forName("java.util.LinkedHashSet");
            }
            else {
                map = HashMap.class;
                set = HashSet.class;
            }
        }
        catch (Throwable t) {
            map = HashMap.class;
            set = HashSet.class;
        }

        DEFAULT_COLLECTION_CLASS = coll;
        DEFAULT_LIST_CLASS = list;
        DEFAULT_MAP_CLASS = map;
        DEFAULT_SET_CLASS = set;
    }

    public final static class ConversionException extends ClassCastException {
        private static final long serialVersionUID = 1L;

        Exception target;

        public ConversionException(Exception target) {
            super(target.getMessage());
            this.target = target;
        }

        public Exception getTargetException() {
            return target;
        }

        @Override
        public String toString() {
            return target.toString();
        }

        public void PrintStackTrace(PrintStream str) {
            target.printStackTrace(str);
        }

        public void PrintStackTrace(PrintWriter wr) {
            target.printStackTrace(wr);
        }

        public void PrintStackTrace() {
            target.printStackTrace();
        }
    }

    private final static DateFormat DATE_FMT = DateFormat.getDateTimeInstance();

    protected ConvertUtils() {
    }

    public static Boolean toBoolObj(Object o, boolean convertNull) {
        if (o == null) {
            return (convertNull) ? Boolean.FALSE : null;
        }
        else if (o instanceof Boolean) {
            return (Boolean)o;
        }
        else if (o instanceof Number) {
            return (((Number)o).doubleValue() != 0.0) ? Boolean.TRUE
                    : Boolean.FALSE;
        }
        else if (o instanceof Character) {
            return (((Character)o).charValue() != '\0') ? Boolean.TRUE
                    : Boolean.FALSE;
        }
        else if (o instanceof Date) {
            return (((Date)o).getTime() != 0) ? Boolean.TRUE : Boolean.FALSE;
        }
        else {
            String s = "" + o;
            return s.equals("true") ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    public static boolean toBool(Object o) {
        if (o == null) {
            return false;
        }
        else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue();
        }
        else if (o instanceof Number) {
            return (((Number)o).doubleValue() != 0.0);
        }
        else if (o instanceof Character) {
            return (((Character)o).charValue() != '\0');
        }
        else if (o instanceof Date) {
            return (((Date)o).getTime() != 0);
        }
        else {
            String s = "" + o;
            return s.equals("true");
        }
    }

    public static Byte toByteObj(Object o, boolean convertNull) {
        if (o == null) {
            return convertNull ? ZERO_BYTE : null;
        }
        else if (o instanceof Byte) {
            return (Byte)o;
        }
        else if (o instanceof Number) {
            return new Byte(((Number)o).byteValue());
        }
        else if (o instanceof byte[]) {
            return (Byte)fromByteArray((byte[])o, ReflectUtils.NUM_PRI_BYTE);
        }
        else if (o instanceof Character) {
            return new Byte((byte)((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return (((Boolean)o).booleanValue()) ? TRUE_BYTE : FALSE_BYTE;
        }
        else if (o instanceof Date) {
            return new Byte((byte)((Date)o).getTime());
        }
        else {
            return (Byte)valueOf(toString(o), ReflectUtils.NUM_PRI_BYTE);
        }
    }

    public static byte toByte(Object o) {
        if (o == null) {
            return (byte)0;
        }
        else if (o instanceof Number) {
            return (((Number)o).byteValue());
        }
        else if (o instanceof byte[]) {
            return fromByteArray((byte[])o, ReflectUtils.NUM_PRI_BYTE)
                    .byteValue();
        }
        else if (o instanceof Character) {
            return (byte)((Character)o).charValue();
        }
        else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue() ? (byte)-1 : (byte)0;
        }
        else if (o instanceof Date) {
            return (byte)((Date)o).getTime();
        }
        else {
            return ((Byte)valueOf(toString(o), ReflectUtils.NUM_PRI_BYTE))
                    .byteValue();
        }
    }

    public static Character toCharObj(Object o, boolean convertNull) {
        if (o == null) {
            return convertNull ? ZERO_CHAR : null;
        }
        else if (o instanceof Character) {
            return (Character)o;
        }
        else if (o instanceof Byte) {
            return new Character((char)((Byte)o).byteValue());
        }
        else if (o instanceof Number) {
            return new Character((char)((Number)o).intValue());
        }
        else if (o instanceof Boolean) {
            return (((Boolean)o).booleanValue()) ? TRUE_CHAR : FALSE_CHAR;
        }
        else if (o instanceof Date) {
            return new Character((char)((Date)o).getTime());
        }
        else {
            String s = toString(o);
            return (s.length() == 0) ? FALSE_CHAR : new Character(s.charAt(0));
        }
    }

    public static char toChar(Object o) {
        if (o == null) {
            return '\0';
        }
        else if (o instanceof Character) {
            return ((Character)o).charValue();
        }
        else if (o instanceof Byte) {
            return (char)((Byte)o).byteValue();
        }
        else if (o instanceof Number) {
            return (char)((Number)o).intValue();
        }
        else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue() ? '\uffff' : '\0';
        }
        else if (o instanceof Date) {
            return (char)((Date)o).getTime();
        }
        else {
            String s = toString(o);
            return (s.length() == 0) ? '\0' : s.charAt(0);
        }
    }

    public static Short toShortObj(Object o, boolean convertNull) {
        if (o == null) {
            return (convertNull) ? ZERO_SHORT : null;
        }
        else if (o instanceof Short) {
            return (Short)o;
        }
        else if (o instanceof Number) {
            return new Short(((Number)o).shortValue());
        }
        else if (o instanceof byte[]) {
            return (Short)fromByteArray((byte[])o,
                    ReflectUtils.NUM_PRI_SHORT);
        }
        else if (o instanceof Character) {
            return new Short((short)((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return (((Boolean)o).booleanValue()) ? TRUE_SHORT : FALSE_SHORT;
        }
        else if (o instanceof Date) {
            return new Short((short)((Date)o).getTime());
        }
        else {
            return (Short)valueOf(toString(o), ReflectUtils.NUM_PRI_SHORT);
        }
    }

    public static short toShort(Object o) {
        if (o == null) {
            return (short)0;
        }
        else if (o instanceof Character) {
            return ((short)((Character)o).charValue());
        }
        else if (o instanceof Number) {
            return (((Number)o).shortValue());
        }
        else if (o instanceof byte[]) {
            return fromByteArray((byte[])o, ReflectUtils.NUM_PRI_SHORT)
                    .shortValue();
        }
        else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue() ? (short)-1 : (short)0;
        }
        else if (o instanceof Date) {
            return (short)((Date)o).getTime();
        }
        else {
            return ((Short)valueOf(toString(o), ReflectUtils.NUM_PRI_SHORT))
                    .shortValue();
        }
    }

    public static Integer toIntObj(Object o, boolean convertNull) {
        if (o == null) {
            return (convertNull) ? ZERO_INT : null;
        }
        else if (o instanceof Integer) {
            return (Integer)o;
        }
        else if (o instanceof Number) {
            return new Integer(((Number)o).intValue());
        }
        else if (o instanceof byte[]) {
            return (Integer)fromByteArray((byte[])o,
                    ReflectUtils.NUM_PRI_INT);
        }
        else if (o instanceof Character) {
            return new Integer(((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return (((Boolean)o).booleanValue()) ? TRUE_INT : FALSE_INT;
        }
        else if (o instanceof Date) {
            return new Integer((int)((Date)o).getTime());
        }
        else {
            return (Integer)valueOf(toString(o), ReflectUtils.NUM_PRI_INT);
        }
    }

    public static int toInt(Object o) {
        if (o == null) {
            return 0;
        }
        else if (o instanceof Number) {
            return (((Number)o).intValue());
        }
        else if (o instanceof byte[]) {
            return fromByteArray((byte[])o, ReflectUtils.NUM_PRI_INT)
                    .intValue();
        }
        else if (o instanceof Character) {
            return (((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue() ? -1 : 0;
        }
        else if (o instanceof Date) {
            return (int)((Date)o).getTime();
        }
        else {
            return ((Integer)valueOf(toString(o), ReflectUtils.NUM_PRI_INT))
                    .intValue();
        }
    }

    public static Long toLongObj(Object o, boolean convertNull) {
        if (o == null) {
            return (convertNull) ? ZERO_LONG : null;
        }
        else if (o instanceof Long) {
            return (Long)o;
        }
        else if (o instanceof Number) {
            return new Long(((Number)o).longValue());
        }
        else if (o instanceof byte[]) {
            return (Long)fromByteArray((byte[])o, ReflectUtils.NUM_PRI_LONG);
        }
        else if (o instanceof Character) {
            return new Long(((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return (((Boolean)o).booleanValue()) ? TRUE_LONG : FALSE_LONG;
        }
        else if (o instanceof Date) {
            return new Long(((Date)o).getTime());
        }
        else {
            return (Long)valueOf(toString(o), ReflectUtils.NUM_PRI_LONG);
        }
    }

    public static long toLong(Object o) {
        if (o == null) {
            return 0L;
        }
        else if (o instanceof Number) {
            return ((Number)o).longValue();
        }
        else if (o instanceof byte[]) {
            return fromByteArray((byte[])o, ReflectUtils.NUM_PRI_LONG)
                    .longValue();
        }
        if (o instanceof Character) {
            return ((Character)o).charValue();
        }
        else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue() ? -1L : 0L;
        }
        else if (o instanceof Date) {
            return ((Date)o).getTime();
        }
        else {
            return ((Long)valueOf(toString(o), ReflectUtils.NUM_PRI_LONG))
                    .longValue();
        }
    }

    public static Float toFloatObj(Object o, boolean convertNull) {
        if (o == null) {
            return (convertNull) ? ZERO_FLOAT : null;
        }
        else if (o instanceof Float) {
            return (Float)o;
        }
        else if (o instanceof Number) {
            return new Float(((Number)o).floatValue());
        }
        else if (o instanceof byte[]) {
            return (Float)fromByteArray((byte[])o,
                    ReflectUtils.NUM_PRI_FLOAT);
        }
        else if (o instanceof Character) {
            return new Float(((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return (((Boolean)o).booleanValue()) ? TRUE_FLOAT : FALSE_FLOAT;
        }
        else if (o instanceof Date) {
            return new Float(((Date)o).getTime());
        }
        else {
            return (Float)valueOf(toString(o), ReflectUtils.NUM_PRI_FLOAT);
        }
    }

    public static float toFloat(Object o) {
        if (o == null) {
            return 0.0f;
        }
        else if (o instanceof Number) {
            return (((Number)o).floatValue());
        }
        else if (o instanceof byte[]) {
            return fromByteArray((byte[])o, ReflectUtils.NUM_PRI_FLOAT)
                    .floatValue();
        }
        else if (o instanceof Character) {
            return (((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue() ? -1.0f : 0.0f;
        }
        else if (o instanceof Date) {
            return ((Date)o).getTime();
        }
        else {
            return ((Float)valueOf(toString(o), ReflectUtils.NUM_PRI_FLOAT))
                    .floatValue();
        }
    }

    public static Double toDoubleObj(Object o, boolean convertNull) {
        if (o == null) {
            return (convertNull) ? ZERO_DOUBLE : null;
        }
        else if (o instanceof Double) {
            return (Double)o;
        }
        else if (o instanceof Number) {
            return new Double(((Number)o).doubleValue());
        }
        else if (o instanceof byte[]) {
            return (Double)fromByteArray((byte[])o,
                    ReflectUtils.NUM_PRI_DOUBLE);
        }
        else if (o instanceof Character) {
            return new Double(((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return (((Boolean)o).booleanValue()) ? TRUE_DOUBLE : FALSE_DOUBLE;
        }
        else if (o instanceof Date) {
            return new Double(((Date)o).getTime());
        }
        else {
            return (Double)valueOf(toString(o), ReflectUtils.NUM_PRI_DOUBLE);
        }
    }

    public static double toDouble(Object o) {
        if (o == null) {
            return 0.0;
        }
        else if (o instanceof Number) {
            return ((Number)o).doubleValue();
        }
        else if (o instanceof byte[]) {
            return fromByteArray((byte[])o, ReflectUtils.NUM_PRI_DOUBLE)
                    .doubleValue();
        }
        else if (o instanceof Character) {
            return ((Character)o).charValue();
        }
        else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue() ? -1.0 : 0.0;
        }
        else if (o instanceof Date) {
            return ((Date)o).getTime();
        }
        else {
            return ((Double)valueOf(toString(o), ReflectUtils.NUM_PRI_DOUBLE))
                    .doubleValue();
        }
    }

    public static BigInteger toBigInteger(Object o, boolean convertNull) {
        Number n = toNumberOpt(o, false);

        if (n == null) {
            return convertNull ? ZERO_BINT : null;
        }
        else if (n instanceof BigInteger) {
            return (BigInteger)n;
        }
        else if (n instanceof BigDecimal) {
            return ((BigDecimal)n).toBigInteger();
        }
        else if (n == NaN) {
            return (BigInteger)valueOf(toString(o), ReflectUtils.NUM_PRI_BINT);
        }
        else {
            return BigInteger.valueOf(n.longValue());
        }
    }

    public static BigDecimal toBigDecimal(Object o, boolean convertNull) {
        Number n = toNumberOpt(o, false);

        if (n == null) {
            return convertNull ? ZERO_BDEC : null;
        }
        else if (n instanceof BigDecimal) {
            return (BigDecimal)n;
        }
        else if (n instanceof BigInteger) {
            return new BigDecimal((BigInteger)n);
        }
        else if (n == NaN) {
            return (BigDecimal)valueOf(toString(o), ReflectUtils.NUM_PRI_BDEC);
        }
        else {
            return new BigDecimal(n.doubleValue());
        }
    }

    /**
     * Convert, if possible, an arbitrary value to a numeric value.
     *
     * @param o
     *            the value to be converted
     * @param convertNull
     *            whether to convert the <code>null</code> value
     * @return <code>NaN</code> if the conversion is not possible, otherwise
     *         return the converted value.
     */
    public static Number toNumberOpt(Object o, boolean convertNull) {
        if (o == null) {
            return (convertNull) ? ZERO_INT : null;
        }
        else if (o instanceof Number) {
            return (Number)o;
        }
        else if (o instanceof byte[]) {
            return fromByteArray((byte[])o, ReflectUtils.NUM_PRI_BAD);
        }
        else if (o instanceof Character) {
            return new Integer(((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return (((Boolean)o).booleanValue()) ? TRUE_INT : FALSE_INT;
        }
        else if (o instanceof Date) {
            return new Long(((Date)o).getTime());
        }
        else {
            return NaN;
        }
    }

    public static Number valueOf(String s, int fmtCode, boolean hasDec,
            boolean hasExp) {
        if (fmtCode == ReflectUtils.NUM_PRI_BAD) {
            // No format code is specified - find a suitable representation and
            // return it
            Double D = Double.valueOf(s);
            double d = D.doubleValue();

            if (hasDec) {
                return D;
            }
            else {
                if ((int)d == d) {
                    // Use an Integer, if it can represent the value
                    return new Integer((int)d);
                }
                else if ((d < Long.MIN_VALUE) || (d > Long.MAX_VALUE)) {
                    // Use a double, if the value is out of the range allowed by
                    // a Long
                    return D;
                }
                else if (hasExp) {
                    // Use a Long, if the value can be represented as such
                    if ((long)d == d) {
                        return new Long((long)d);
                    }
                    else {
                        return D;
                    }
                }
                else {
                    // A Long has greater precision than a Double - try to parse
                    // the
                    // Long directly
                    try {
                        return Long.valueOf(s);
                    }
                    catch (Exception e) {
                        // Not a valid Long - return the double representation
                        return D;
                    }
                }
            }
        }
        else {
            // Convert to the specified representation
            switch (fmtCode) {
            case ReflectUtils.NUM_PRI_BDEC:
                return new BigDecimal(s);
            case ReflectUtils.NUM_PRI_BINT:
                if (hasDec || hasExp) {
                    return new BigDecimal(s).toBigInteger();
                }
                else {
                    return new BigInteger(s);
                }
            case ReflectUtils.NUM_PRI_DOUBLE:
                return Double.valueOf(s);
            case ReflectUtils.NUM_PRI_FLOAT:
                return Float.valueOf(s);
            case ReflectUtils.NUM_PRI_LONG:
                if (hasDec || hasExp) {
                    return new Long((long)Double.parseDouble(s));
                }
                else {
                    return Long.valueOf(s);
                }
            default:
                int i = (hasDec || hasExp) ? (int)Double.parseDouble(s)
                        : Integer.parseInt(s);
                switch (fmtCode) {
                case ReflectUtils.NUM_PRI_BYTE:
                    return new Byte((byte)i);
                case ReflectUtils.NUM_PRI_SHORT:
                    return new Short((short)i);
                default: // ReflectUtils.NUM_PRI_INT
                    return new Integer(i);
                }
            }
        }
    }

    public static Number valueOf(String s, int prefFmt) {
        boolean hasDec = s.indexOf('.') >= 0;
        boolean hasExp;
        if ((s.length() > 2) && (Character.toLowerCase(s.charAt(1)) == 'x')
                && (s.charAt(0) == '0')) {
            // It is a hexadecimal string - convert it as such
            return fromHexString(s.substring(2), prefFmt);
        }
        else {
            hasExp = (s.lastIndexOf('e') >= 0) || (s.lastIndexOf('E') >= 0);
            return valueOf(s, prefFmt, hasDec, hasExp);
        }
    }

    public static Number toNumber(Object o, boolean convertNull) {
        Number result = toNumberOpt(o, convertNull);
        if (result == NaN) {
            return valueOf(toString(o), ReflectUtils.NUM_PRI_BAD);
        }
        else {
            return result;
        }
    }

    public static Number toBits(Object o) {
        if (o == null) {
            return ZERO_INT;
        }
        else if (o instanceof Character) {
            return new Integer(((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return ((Boolean)o).booleanValue() ? TRUE_INT : FALSE_INT;
        }
        else {
            Number n = (o instanceof Number) ? (Number)o
                    : valueOf(toString(o), ReflectUtils.NUM_PRI_BAD);

            int pri = ReflectUtils.getPriority(n);
            switch (pri) {
            case ReflectUtils.NUM_PRI_FLOAT:
                return new Integer(
                        Float.floatToIntBits(((Float)o).floatValue()));
            case ReflectUtils.NUM_PRI_DOUBLE:
                return new Long(
                        Double.doubleToLongBits(((Double)o).doubleValue()));
            case ReflectUtils.NUM_PRI_BDEC:
                return ((BigDecimal)n).toBigInteger();
            case ReflectUtils.NUM_PRI_BINT:
                return n;
            case ReflectUtils.NUM_PRI_LONG:
                return n;
            case ReflectUtils.NUM_PRI_INT:
                return n;
            case ReflectUtils.NUM_PRI_BAD:
                return new Long(n.longValue());
            default:
                return ReflectUtils.convertTo(n, ReflectUtils.NUM_PRI_INT);
            }
        }
    }

    public static String toString(Object o) {
        if (o == null) {
            return null;
        }
        else if (o instanceof String) {
            return (String)o;
        }
        else if (o instanceof byte[]) {
            return new String((byte[])o);
        }
        else if (o instanceof char[]) {
            return new String((char[])o);
        }
        else if (o instanceof Date) {
            return DATE_FMT.format((Date)o);
        }
        else if (o instanceof File) {
            return ((File)o).getPath();
        }
        else {
            return o.toString();
        }
    }

    public static CharSequence toCharSequence(Object o) {
        return o == null ? null
                : (o instanceof CharSequence) ? (CharSequence)o
                        : (CharSequence)toString(o);
    }

    public static StringBuffer toStringBuffer(Object o) {
        return o == null ? null
                : (o instanceof StringBuffer) ? (StringBuffer)o
                        : new StringBuffer(toString(o));
    }

    public static Pattern toPattern(Object o) {
        if (o == null) {
            return null;
        }
        else if (o instanceof Pattern) {
            return (Pattern)o;
        }
        else {
            return Pattern.compile(toString(o),
                    Pattern.DOTALL | Pattern.MULTILINE);
        }
    }

    public static URL toURL(Object o, URL baseURL) {
        try {
            if (o == null) {
                return null;
            }
            else if (o instanceof URL) {
                return (URL)o;
            }
            else if (o instanceof File) {
                return ((File)o).toURI().toURL();
            }
            else {
                return new URL(OsInterface.HOME_URL, toString(o));
            }
        }
        catch (MalformedURLException e) {
            throw new ConversionException(e);
        }
    }

    public static File toFile(Object o) {
        if (o == null) {
            return null;
        }
        else if (o instanceof File) {
            return (File)o;
        }
        else if (o instanceof URL) {
            URL tmp = (URL)o;
            String p = tmp.getProtocol();
            if (!p.equals("file")) {
                throw new IllegalArgumentException("Invalid protocol " + p);
            }
            return new File(tmp.getFile());
        }
        else {
            String file = toString(o);
            if ((file.length() > 5) && (file.charAt(4) == ':') &&
                    (file.substring(0, 4).equals("file"))) {
                file = file.substring(5);
            }
            return new File(file);
        }
    }

    public static Class toClass(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Class) {
            return (Class)o;
        }
        else {
            try {
                return Class.forName(toString(o));
            }
            catch (ClassNotFoundException e) {
                throw new ConversionException(e);
            }
        }
    }

    public static Class classOf(Object o) {
        if (o == null) {
            return Object.class;
        }
        else if (o instanceof Class) {
            return (Class)o;
        }
        else {
            return o.getClass();
        }
    }

    public static Date toDate(Object o) {
        if (o == null) {
            return null;
        }
        else if (o instanceof Date) {
            return (Date)o;
        }
        else if (o instanceof Number) {
            return new Date(((Number)o).longValue());
        }
        else if (o instanceof Character) {
            return new Date(((Character)o).charValue());
        }
        else if (o instanceof Boolean) {
            return (((Boolean)o).booleanValue()) ? TRUE_DATE : FALSE_DATE;
        }
        else if (o instanceof Calendar) {
            return ((Calendar)o).getTime();
        }
        else {
            try {
                return DATE_FMT.parse(toString(o));
            }
            catch (ParseException e) {
                throw new ConversionException(e);
            }
        }
    }

    public static Iterator toIterator(Object o) {
        if (o == null) {
            return null;
        }
        else if (o instanceof Iterator) {
            return (Iterator)o;
        }
        else if (o instanceof List) {
            return ((List)o).listIterator();
        }
        else if (o instanceof Collection) {
            return ((Collection)o).iterator();
        }
        else if (o instanceof Object[]) {
            return new ArrayIterator((Object[])o);
        }
        else if (o instanceof CharSequence) {
            return new CharSequenceIterator((CharSequence)o);
        }
        else if (o instanceof Enumeration) {
            return new EnumerationIterator((Enumeration)o);
        }
        else {
            return new PrimitiveArrayIterator(o);
        }
    }

    public static Collection newCollection() {
        try {
            return (Collection)DEFAULT_COLLECTION_CLASS.newInstance();
        }
        catch (Throwable t) {
            return new ArrayList();
        }
    }

    public static Collection toCollection(Object o) {
        return toCollection(o, Collection.class, DEFAULT_COLLECTION_CLASS);
    }

    public static Collection toCollection(Object o, Class repr) {
        return toCollection(o, repr, DEFAULT_COLLECTION_CLASS);
    }

    public static Collection toCollection(Object o, Class cls,
            Class defaultClass) {
        try {
            if (o == null) {
                return null;
            }
            else if (o instanceof Collection) {
                Collection coll = (Collection)o;
                Class c = coll.getClass();

                if (cls.isAssignableFrom(c)) {
                    return coll;
                }
                else if (!cls.isAssignableFrom(defaultClass)) {
                    throw new ClassCastException(defaultClass.getName());
                }
                else {
                    Collection result = (Collection)defaultClass.newInstance();
                    result.addAll(coll);
                    return result;
                }
            }
            else {
                Iterator it = toIterator(o);
                Collection result = (Collection)defaultClass.newInstance();
                while (it.hasNext()) {
                    result.add(it.next());
                }
                return result;
            }
        }
        catch (InstantiationException e) {
            throw new ConversionException(e);
        }
        catch (IllegalAccessException e) {
            throw new ConversionException(e);
        }
    }

    public static List newList() {
        try {
            return (List)DEFAULT_COLLECTION_CLASS.newInstance();
        }
        catch (Throwable t) {
            return new ArrayList();
        }
    }

    public static List toList(Object o) {
        return (List)toCollection(o, List.class, DEFAULT_LIST_CLASS);
    }

    public static List toList(Object o, Class repr, Class defaultClass) {
        return (List)toCollection(o, repr, defaultClass);
    }

    public static Set newSet() {
        try {
            return (Set)DEFAULT_SET_CLASS.newInstance();
        }
        catch (Throwable t) {
            return new HashSet();
        }
    }

    public static Set toSet(Object o) {
        return (Set)toCollection(o, Set.class, DEFAULT_SET_CLASS);
    }

    public static Set toSet(Object o, Class repr, Class defaultClass) {
        return (Set)toCollection(o, repr, defaultClass);
    }

    public static Map newMap() {
        try {
            return (Map)DEFAULT_MAP_CLASS.newInstance();
        }
        catch (Throwable t) {
            return new HashMap();
        }
    }

    public static Map toMap(Object o) {
        return toMap(o, Map.class, DEFAULT_MAP_CLASS);
    }

    public static Map toMap(Object o, Class repr, Class defaultClass) {
        try {
            if (o == null) {
                return null;
            }
            else if (o instanceof Map) {
                Map m = (Map)o;
                if (repr.isAssignableFrom(m.getClass())) {
                    return m;
                }
                if (!repr.isAssignableFrom(defaultClass)) {
                    throw new ClassCastException(defaultClass.getName());
                }

                Map result = (Map)defaultClass.newInstance();
                result.putAll(m);
                return result;
            }
            return (Map)o;
        }
        catch (InstantiationException e) {
            throw new ConversionException(e);
        }
        catch (IllegalAccessException e) {
            throw new ConversionException(e);
        }
    }

    public static Object toArray(Object o) {
        return toArray(o, Object[].class, Object.class, false);
    }

    public static Object[] toObjectArray(Object o) {
        return (Object[])toArray(o, Object[].class, Object.class, true);
    }

    public static Object toArray(Object o, Class c) {
        Class elemType = c.getComponentType();
        return toArray(o, c, (elemType == null) ? Object.class : elemType,
                true);
    }

    public static Object toArray(Object o, Class container, Class contained,
            boolean strict) {
        if (o == null) {
            return null;
        }

        Class c = o.getClass();
        c.getName();

        container.getName();
        contained.getName();
        if (container.isAssignableFrom(c)) {
            return o;
        }
        else if (c.isArray()) {
            if (!strict) {
                return o;
            }

            int l = Array.getLength(o);
            Object result = Array.newInstance(contained, l);

            for (int i = 0; i < l; i++) {
                Array.set(result, i, Array.get(o, i));
            }

            return result;
        }

        if (o instanceof Collection) {
            Collection coll = (Collection)o;

            if (contained == Object.class) {
                return coll.toArray();
            }
            else if (contained.isPrimitive()) {
                int sz = coll.size();
                Iterator it = coll.iterator();
                Object result = Array.newInstance(contained, sz);
                for (int i = 0; i < sz; i++) {
                    Array.set(result, i, it.next());
                }
                return result;
            }
            else {
                return coll.toArray((Object[])Array.newInstance(contained, 0));
            }
        }
        else if (c == String.class) {
            if (contained == Byte.TYPE) {
                return ((String)o).getBytes();
            }
            else if (!strict || (contained == Character.TYPE)) {
                return ((String)o).toCharArray();
            }
            else {
                return iteratorToArray(toIterator(o), contained);
            }
        }
        else if (c == StringBuffer.class) {
            if (!strict || (contained == Character.TYPE)) {
                StringBuffer sb = (StringBuffer)o;
                char[] result = new char[sb.length()];
                sb.getChars(0, sb.length(), result, 0);
                return result;
            }
            else {
                return iteratorToArray(toIterator(o), contained);
            }
        }
        else {
            return iteratorToArray(toIterator(o), contained);
        }
    }

    public static Object iteratorToArray(Iterator it, Class elemType) {
        ArrayList l = new ArrayList();
        while (it.hasNext()) {
            l.add(it.next());
        }

        if (elemType == Object.class) {
            return l.toArray();
        }

        int sz = l.size();
        if (elemType.isPrimitive()) {
            it = l.iterator();
            Object result = Array.newInstance(elemType, sz);
            for (int i = 0; i < sz; i++) {
                Array.set(result, i, it.next());
            }
            return result;
        }
        else {
            return l.toArray((Object[])Array.newInstance(elemType, sz));
        }
    }

    private static String getLastDigits(String s, int digits) {
        int start = s.length() - digits;
        if (start <= 0) {
            return s;
        }
        return s.substring(start);
    }

    /**
     * Convert a byte array (in 2's complement) to a number.
     *
     * @param arr
     *            the array value
     * @param prefFmt
     *            the preferred number format (minimum size).
     * @return the converted number
     * @throws NumberFormatException
     *             on empty input
     */
    public static Number fromByteArray(byte[] arr, int prefFmt)
            throws NumberFormatException {
        if ((arr == null) || (arr.length == 0)) {
            throw new NumberFormatException("" + arr);
        }

        // determine the resulting format, if none is given
        if ((prefFmt < ReflectUtils.NUM_PRI_BYTE) ||
                (prefFmt > ReflectUtils.NUM_PRI_BDEC)) {
            prefFmt = (arr.length <= 8) ? ReflectUtils.NUM_PRI_INT
                    : (arr.length <= 16) ? ReflectUtils.NUM_PRI_LONG
                            : ReflectUtils.NUM_PRI_BINT;
        }

        // create a new BigInteger, if the result should not be of a
        // variable-length type.
        if (prefFmt >= ReflectUtils.NUM_PRI_BINT) {
            BigInteger result = new BigInteger(arr);
            return (prefFmt == ReflectUtils.NUM_PRI_BDEC)
                    ? (Number)new BigDecimal(result)
                    : (Number)result;
        }

        // determine the highest index of the array to be used
        int max;
        switch (prefFmt) {
        case ReflectUtils.NUM_PRI_BYTE:
            max = 1;
            break;
        case ReflectUtils.NUM_PRI_SHORT:
            max = 2;
            break;
        case ReflectUtils.NUM_PRI_INT:
        case ReflectUtils.NUM_PRI_FLOAT:
            max = 4;
            break;
        default: // ReflectUtils.NUM_PRI_LONG / ReflectUtils.NUM_PRI_DOUBLE
            max = 8;
        }
        max = arr.length - max;
        if (max < 0) {
            max = 0;
        }

        // convert the value to a long; begin by choosing the correct sign
        long l = (arr[max] < 0) ? -1L : 0L;
        for (int i = max; i < arr.length;) {
            l = (l << 8) | (arr[i++] & 0xff);
        }

        // Return the number in the required format
        switch (prefFmt) {
        case ReflectUtils.NUM_PRI_BYTE:
            return new Byte((byte)l);
        case ReflectUtils.NUM_PRI_SHORT:
            return new Short((short)l);
        case ReflectUtils.NUM_PRI_INT:
            return new Integer((int)l);
        case ReflectUtils.NUM_PRI_LONG:
            return new Long(l);
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Float(Float.intBitsToFloat((int)l));
        default: // ReflectUtils.NUM_PRI_DOUBLE
            return new Double(Double.longBitsToDouble(l));
        }
    }

    /**
     * Converts a hexadecimal string to a number that holds the value of the
     * string. The least suitable integer format is selected (based on the
     * string length, <i>including</i> leading zeros, unless a preferred format
     * is given.
     *
     * @param s
     *            the string
     * @param prefFmt
     *            the preferred number format (minimum size) of the result
     * @return the converted value
     */
    public static Number fromHexString(String s, int prefFmt)
            throws NumberFormatException {
        if ((s == null) || (s.length() == 0)) {
            throw new NumberFormatException(s);
        }

        // Convert the number directly, if no preference is given
        int l;

        if ((prefFmt < ReflectUtils.NUM_PRI_BYTE) ||
                (prefFmt > ReflectUtils.NUM_PRI_BDEC)) {

            l = s.length();

            if (l <= 8) {
                return new Integer((int)StringUtils.hex2Long(s));
            }
            else if (l <= 16) {
                return new Long(StringUtils.hex2Long(s));
            }
            else {
                return new BigInteger(StringUtils.hex2bytes(s));
            }
        }

        // Convert the number to the preferred format
        switch (prefFmt) {
        case ReflectUtils.NUM_PRI_BYTE:
            return new Byte((byte)StringUtils.hex2Long(getLastDigits(s, 2)));
        case ReflectUtils.NUM_PRI_SHORT:
            return new Short((short)StringUtils.hex2Long(getLastDigits(s, 4)));
        case ReflectUtils.NUM_PRI_INT:
            return new Integer((int)StringUtils.hex2Long(getLastDigits(s, 8)));
        case ReflectUtils.NUM_PRI_LONG:
            return new Long(StringUtils.hex2Long(getLastDigits(s, 16)));
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Float(Float.intBitsToFloat(
                    (int)StringUtils.hex2Long(getLastDigits(s, 8))));
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Double(Double.longBitsToDouble(
                    StringUtils.hex2Long(getLastDigits(s, 16))));
        default:
            BigInteger bi = new BigInteger(StringUtils.hex2bytes(s));
            if (prefFmt == ReflectUtils.NUM_PRI_BINT) {
                return bi;
            }
            else {
                return new BigDecimal(bi);
            }
        }
    }

}
