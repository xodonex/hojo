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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * This class contains various utility methods used for string operations.
 */
public final class StringUtils {

    public final static int FORMAT_NONE = 0;
    public final static int FORMAT_JAVA = 1;
    public final static int FORMAT_UUENCODE = 2;
    public final static int FORMAT_QUOTE_MASK = 3;
    public final static int FORMAT_TYPED = 4;
    public final static int FORMAT_FULL_TYPED = 8;
    public final static int FORMAT_JAVA_TYPED = FORMAT_JAVA | FORMAT_TYPED;

    public final static int ENCODE_FILTER = -1;
    public final static int ENCODE_UNCHANGED = 0;
    public final static int ENCODE_UNICODE = 1;
    public final static int ENCODE_HTML = 2;

    public final static int LENGTH_MAX_STRING = 2000;
    public final static int LENGTH_LOBOUND_STRING = 1;
    public final static int LENGTH_MAX_ARRAY = 100;
    public final static int LENGTH_LOBOUND_ARRAY = 1;

    public final static Format defaultFormat = new Format();

    /*
     * The Format is used as parameter for any2String and its internal methods
     */
    public static class Format implements Cloneable, java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private boolean typed;
        private boolean allTyped;
        private int quoteFmt;
        private int stringMax, arrayMax;
        private boolean useSizeLimit, formatRepeats;

        private char charQuote, stringQuote;
        private final String[] listChars = new String[2];
        private final String[] mapChars = new String[2];
        private final String[] arrayChars = new String[2];

        private String newline;
        private String indent;
        private String delimiter;
        @SuppressWarnings("unused")
        private String separator;

        private String varPrefix, varInfix, typeIndicator, classIndicator,
                idIndicator, _null;

        private transient WeakHashMap visited = new WeakHashMap();

        public Format() {
            setFmt(FORMAT_JAVA);
            setSizeLimits(LENGTH_MAX_STRING, LENGTH_MAX_ARRAY, true);
            setDefaultChars();
        }

        public Format(int fmt) {
            setFmt(fmt);
            setSizeLimits(LENGTH_MAX_STRING, LENGTH_MAX_ARRAY, true);
            setDefaultChars();
        }

        public Format(int fmt, int stringMax) {
            setFmt(fmt);
            setSizeLimits(stringMax, LENGTH_MAX_ARRAY, true);
            setDefaultChars();
        }

        public Format(int fmt, int stringMax, int arrayMax) {
            setFmt(fmt);
            setSizeLimits(stringMax, arrayMax, true);
            setDefaultChars();
        }

        public void setDefaultChars() {
            newline = "\n";
            indent = "  ";
            charQuote = '\'';
            stringQuote = '"';
            delimiter = ", ";
            separator = ";";
            listChars[0] = "[";
            listChars[1] = "]";
            mapChars[0] = "[.";
            mapChars[1] = ".]";
            arrayChars[0] = "{";
            arrayChars[1] = "}";
            _null = "null";
            varPrefix = "";
            varInfix = " = ";
            typeIndicator = " : ";
            classIndicator = ".class";
            idIndicator = "@";
        }

        public synchronized void setFmt(int fmt) {
            quoteFmt = fmt & FORMAT_QUOTE_MASK;
            typed = (fmt & FORMAT_TYPED) > 0;
            allTyped = (fmt & FORMAT_FULL_TYPED) > 0;
        }

        public synchronized void setTypeFmt(int fmt) {
            typed = (fmt & FORMAT_TYPED) > 0;
            allTyped = (fmt & FORMAT_FULL_TYPED) > 0;
        }

        public synchronized int getFmt() {
            return quoteFmt | getTypeFmt();
        }

        public synchronized int getTypeFmt() {
            return (typed ? FORMAT_TYPED : 0)
                    | (allTyped ? FORMAT_FULL_TYPED : 0);
        }

        public synchronized void setStringLimit(int stringMax) {
            this.stringMax = (stringMax < LENGTH_LOBOUND_STRING)
                    ? LENGTH_LOBOUND_STRING
                    : stringMax;
        }

        public synchronized void setArrayLimit(int arrayMax) {
            this.arrayMax = (arrayMax < LENGTH_LOBOUND_ARRAY)
                    ? LENGTH_LOBOUND_ARRAY
                    : arrayMax;
        }

        public synchronized void setSizeLimits(int stringMax, int arrayMax,
                boolean useLimit) {
            this.stringMax = (stringMax < LENGTH_LOBOUND_STRING)
                    ? LENGTH_LOBOUND_STRING
                    : stringMax;
            this.arrayMax = (arrayMax < LENGTH_LOBOUND_ARRAY)
                    ? LENGTH_LOBOUND_ARRAY
                    : arrayMax;
            this.useSizeLimit = useLimit;
        }

        public synchronized boolean useSizeLimit(boolean use) {
            boolean result = useSizeLimit;
            useSizeLimit = use;
            return result;
        }

        public synchronized int getStringLimit() {
            return stringMax;
        }

        public synchronized int getArrayLimit() {
            return arrayMax;
        }

        public synchronized boolean isSizeLimitUsed() {
            return useSizeLimit;
        }

        public synchronized boolean isRepeatsFormatted() {
            return formatRepeats;
        }

        public synchronized void formatRepeats(boolean isFormatted) {
            formatRepeats = isFormatted;
        }

        public synchronized String getIndent() {
            return indent;
        }

        public synchronized void setQuotes(char charQuote, char stringQuote) {
            this.charQuote = charQuote;
            this.stringQuote = stringQuote;
        }

        public synchronized void setBlank(String newline, String indent) {
            this.newline = newline;
            this.indent = indent;
        }

        public synchronized void setDelimiter(String delim) {
            delimiter = delim;
        }

        public synchronized void setSeparator(String sep) {
            separator = sep;
        }

        public synchronized void setArrayParentheses(String left,
                String right) {
            arrayChars[0] = left;
            arrayChars[1] = right;
        }

        public synchronized void setListParentheses(String left, String right) {
            listChars[0] = left;
            listChars[1] = right;
        }

        public synchronized void setMapParentheses(String left, String right) {
            mapChars[0] = left;
            mapChars[1] = right;
        }

        public synchronized void setVariableFormat(String prefix,
                String infix) {
            varPrefix = prefix;
            varInfix = infix;
        }

        public synchronized void setTypeIndicator(String ti) {
            typeIndicator = ti;
        }

        public synchronized void setIdIndicator(String id) {
            idIndicator = id;
        }

        public synchronized void setClassIndicator(String ci) {
            classIndicator = ci;
        }

        public synchronized void setNull(String _null) {
            this._null = _null;
        }

        @Override
        public synchronized Object clone() {
            try {
                return super.clone();
            }
            catch (CloneNotSupportedException e) {
                return null;
            }
        }

        synchronized void reset() {
            if (visited == null) {
                visited = new WeakHashMap();
            }
            else {
                visited.clear();
            }
        }
    }

    public static String toIDString(Object o) {
        if (o == null) {
            return "" + o;
        }
        return o.getClass().getName() + "@" +
                Integer.toHexString(System.identityHashCode(o));
    }

    // o must be different from null
    private static String typeDesc(Object o, Format fmt) {
        return fmt.typeIndicator + ReflectUtils.className2Java(o.getClass());
    }

    // o must be different from null
    private static String printIdentity(Object o, boolean typed, int ID,
            Format fmt) {
        if (typed) {
            return fmt.idIndicator + Integer.toHexString(ID) + typeDesc(o, fmt);
        }
        else {
            return o.getClass().getName() + fmt.idIndicator
                    + Integer.toHexString(ID);
        }
    }

    private StringUtils() {
    }

    /**
     * Converts the given array to a string representation.
     *
     * @param array
     *            the array to convert
     * @param indent
     *            the starting indentation for each line
     * @param quote
     *            Whether <code>String</code> and <code>char</code> values
     *            should be quoted.
     * @return a string representation of the <code>array</code>.
     * @exception IllegalArgumentException
     *                if the <code>array</code> is not an array.
     */
    private static String array2String(Object array, boolean typed,
            String indent, Format fmt) throws IllegalArgumentException {

        if (array == null) {
            return fmt._null;
        }

        // Use the default (identity) hash code to determine which arrays that
        // are
        // already visited
        Integer idHash = new Integer(System.identityHashCode(array));
        if (fmt.visited.containsKey(idHash)) {
            return printIdentity(array, typed, idHash.intValue(), fmt);
        }
        else if (!fmt.formatRepeats) {
            fmt.visited.put(idHash, idHash);
        }

        // Format the array/Collection as an ID code, if it is too large.
        // Otherwise, select the appropriate parentheses and convert a list to
        // an array.
        Object original = array;
        String[] parens;
        int size;
        if (array instanceof Collection) {
            if ((size = ((Collection)array).size()) > fmt.arrayMax
                    && fmt.useSizeLimit) {
                return printIdentity(original, typed, idHash.intValue(), fmt);
            }
            else {
                parens = fmt.listChars;
                array = ((Collection)array).toArray();
            }
        }
        else {
            if ((size = Array.getLength(array)) > fmt.arrayMax
                    && fmt.useSizeLimit) {
                return printIdentity(original, typed, idHash.intValue(), fmt);
            }
            else {
                parens = fmt.arrayChars;
            }
        }

        // Print every contained value
        Object o = null;
        StringBuffer result = new StringBuffer();

        result.append(parens[0]);
        for (int i = 0; i < size; i++) {
            // try {
            o = Array.get(array, i);
            // }
            // catch (ArrayIndexOutOfBoundsException e) {
            /* won't happen */
            // }

            result.append(any2String(o, fmt.allTyped, indent, fmt));
            if (i < size - 1) {
                result.append(fmt.delimiter);
            }
        }

        return result.append(parens[1])
                .append(typed ? typeDesc(original, fmt) : "").toString();
    }

    /**
     * Converts the given <code>Map</code> to a string representation. The first
     * line of the result contains a '{'. The following lines each represent a
     * mapping of the <code>Map</code>. These lines will consist of an
     * indentation, the key, <code>MAP</code> and the value, all converted using
     * <code>any2String(quote)</code>.
     *
     * @param map
     *            The <code>Map</code> that should be converted to a string
     *            value.
     * @param indent
     *            The indentation for the entire <code>Map</code>
     * @param quote
     *            Whether <code>String</code> and <code>char</code> values
     *            should be quoted.
     * @return a string representation of the given <code>Map</code>.
     * @see java.lang.Object#toString
     * @see #any2String(Object, Format, String)
     */
    private static String map2String(Map map, boolean typed,
            String indent, Format fmt) {
        if (map == null) {
            return fmt._null;
        }

        // Use the default (identity) hash code to determine which maps that are
        // already visited
        Integer idHash = new Integer(System.identityHashCode(map));
        if (fmt.visited.containsKey(idHash)) {
            return printIdentity(map, typed, idHash.intValue(), fmt);
        }
        else if (!fmt.formatRepeats) {
            fmt.visited.put(idHash, idHash);
        }

        StringBuffer result = new StringBuffer().append(fmt.mapChars[0])
                .append(fmt.newline);
        Iterator i = map.entrySet().iterator();
        Map.Entry entry;
        Object value;
        String indent_ = indent + fmt.indent;

        while (i.hasNext()) {
            entry = (Map.Entry)i.next();
            result.append(indent_).append(fmt.varPrefix).append(entry.getKey())
                    .append(fmt.varInfix);
            value = entry.getValue();

            result.append(any2String(value, typed,
                    (value instanceof Map) ? indent_ : "", fmt) + fmt.newline);
        }
        return result.append(indent).append(fmt.mapChars[1])
                .append((typed ? typeDesc(map, fmt) : "")).toString();
    }

    /**
     * This method is an extended <code>toString()</code>: objects that are
     * <code>Map</code>s or arrays are converted using <code>map2String()
     * </code> or <code>array2String</code>;
     *
     * @param o
     *            The object that should be converted to a <code>
     * String</code>
     * @param indent
     *            The starting indentation for the result ( only used, if the
     *            result is a <code>Map</code> or array)
     * @param quote
     *            Whether <code>String</code> and <code>char</code> values
     *            should be quoted.
     * @return a string representation of <code>o</code>
     */
    private static String any2String(Object o, boolean typed, String indent,
            Format fmt) {
        if (o == null) {
            return fmt._null;
        }
        else if (o instanceof Map) {
            return map2String((Map)o, typed, indent, fmt);
        }

        // Convert List -> Array in order to leave the list iterator position
        // unchanged.
        if ((o instanceof Collection) || (o.getClass().isArray())) {
            try {
                return array2String(o, typed, indent, fmt);
            }
            catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                else {
                    return "" + e; // won't happen
                }
            }
        }

        String s;
        int pri;

        if (o instanceof String) {
            switch (fmt.quoteFmt) {
            case FORMAT_JAVA:
            case FORMAT_UUENCODE:
                if (((String)o).length() >= fmt.stringMax) {
                    // don't convert strings whose size exceeds the max
                    s = null;
                }
                else {
                    s = "" + fmt.stringQuote +
                            toJavaString((String)o,
                                    fmt.quoteFmt == FORMAT_UUENCODE)
                            +
                            fmt.stringQuote;
                }
                break;
            default:
                s = ((String)o);
            }
            ;
        }
        else if (o instanceof Character) {
            char c = ((Character)o).charValue();
            switch (fmt.quoteFmt) {
            case FORMAT_JAVA:
            case FORMAT_UUENCODE:
                s = "" + fmt.charQuote +
                        toJavaChar(c, fmt.quoteFmt == FORMAT_UUENCODE)
                        + fmt.charQuote;
                break;
            default:
                s = "" + c;
            }
            ;
        }
        else if (o instanceof Class) {
            s = ReflectUtils.className2Java((Class)o) + fmt.classIndicator;
        }
        else if ((o instanceof Number) &&
                ((pri = ReflectUtils
                        .getPriority((Number)o)) != ReflectUtils.NUM_PRI_BAD)
                &&
                (pri <= ReflectUtils.NUM_PRI_DOUBLE)) {
            // Always print numbers that are of fixed length
            s = o.toString();
        }
        else {
            // Print arbitrary-length numbers or unknown objects once only.
            Integer _i = new Integer(System.identityHashCode(o));
            if (fmt.visited.containsKey(_i)) {
                return printIdentity(o, typed, _i.intValue(), fmt);
            }
            else if (!fmt.formatRepeats) {
                fmt.visited.put(_i, _i);
            }

            // avoid converting objects that are known to be too big
            if (o instanceof BigInteger) {
                BigInteger b = (BigInteger)o;
                if (fmt.useSizeLimit && (b.bitLength() >> 2) > fmt.stringMax) {
                    // indicates that s exceeds the limit
                    s = null;
                }
                else {
                    s = o.toString();
                }
            }
            else if (o instanceof StringBuffer) {
                if (((StringBuffer)o).length() >= fmt.stringMax) {
                    s = null;
                }
                else {
                    s = o.toString();
                }
            }
            else {
                s = o.toString();
            }
        }

        if (s == null || (fmt.useSizeLimit && s.length() > fmt.stringMax)) {
            return printIdentity(o, typed, System.identityHashCode(o), fmt);
        }
        else {
            return s + ((typed) ? typeDesc(o, fmt) : "");
        }
    }

    /**
     * This method is equivalent to <code>any2String(o, "", false)</code>
     *
     * /** Format an object of any type as a string.
     *
     * @param o
     *            the object to be formatted
     * @return the formatted string
     * @see #any2String(Object, Format, String)
     */
    public static String any2String(Object o) {
        return any2String(o, "");
    }

    /**
     * Format an object of any type as a string.
     *
     * @param o
     *            the object to be formatted
     * @param indent
     *            the indent used in formatting
     * @return the formatted value
     * @throws NullPointerException
     *             if fmt is null
     * @see #any2String(Object, Format, String)
     */
    public static String any2String(Object o, String indent) {
        String result = any2String(o, defaultFormat.typed, indent,
                defaultFormat);
        defaultFormat.reset();
        return result;
    }

    /**
     * Format an object of any type as a string.
     *
     * @param o
     *            the object to be formatted
     * @param fmt
     *            the format description
     * @return the formatted value
     * @throws NullPointerException
     *             if fmt is null
     * @see #any2String(Object, Format, String)
     */
    public static String any2String(Object o, Format fmt)
            throws NullPointerException {
        return any2String(o, fmt.typed, "", fmt);
    }

    /**
     * Format an object of any type as a string.
     *
     * @param o
     *            the object to be formatted
     * @param fmt
     *            the format description
     * @param indent
     *            the indent used in formatting
     * @return the formatted value
     * @throws NullPointerException
     *             if fmt is null
     */
    public static String any2String(Object o, Format fmt, String indent)
            throws NullPointerException {
        String result = null;

        synchronized (fmt) {
            fmt.reset();
            result = any2String(o, fmt.typed, indent, fmt);
        }
        return result;
    }

    public static String toOctalEscape(char c) {
        if (c > '\u00FF') {
            return "" + c;
        }

        String s = Integer.toString(c, 8);
        switch (s.length()) {
        case 1:
            return "\\00" + s;
        case 2:
            return "\\0" + s;
        default:
            return "\\" + s;
        }
    }

    public static String toUnicodeEscape(char c) {
        String s = Integer.toString(c, 16);
        switch (s.length()) {
        case 1:
            return "\\u000" + s;
        case 2:
            return "\\u00" + s;
        case 3:
            return "\\u0" + s;
        default:
            return "\\u" + s;
        }
    }

    private static String[] charEscapes = new String[256];
    static {
        for (int i = 0; i < ' '; i++) {
            charEscapes[i] = toOctalEscape((char)i);
        }
        for (int i = ' '; i < '\u007F'; i++) {
            charEscapes[i] = "" + (char)i;
        }
        for (int i = '\u007F'; i < '\u00A0'; i++) {
            charEscapes[i] = toOctalEscape((char)i);
        }
        for (int i = '\u00A0'; i < '\u00FF'; i++) {
            charEscapes[i] = toOctalEscape((char)i); // "" + (char)i;
        }
        // charEscapes[0] = "\\0";
        charEscapes[8] = "\\b";
        charEscapes[9] = "\\t";
        charEscapes[10] = "\\n";
        charEscapes[12] = "\\f";
        charEscapes[13] = "\\r";
        charEscapes[34] = "\\\"";
        charEscapes[39] = "\\'";
        charEscapes[92] = "\\\\";
    }

    private static char[] escapeChars = new char[128];
    static {
        for (int i = 0; i < 128; i++) {
            escapeChars[i] = '\uFFFF';
        }
        escapeChars['0'] = '\0';
        escapeChars['b'] = '\b';
        escapeChars['t'] = '\t';
        escapeChars['n'] = '\n';
        escapeChars['f'] = '\f';
        escapeChars['r'] = '\r';
        escapeChars['"'] = '\"';
        escapeChars['\''] = '\'';
    }

    public static String toJavaChar(char c) {
        return toJavaChar(c, true);
    }

    public static String toJavaChar(char c, boolean useUnicode) {
        if (c <= '\u00FF') {
            return charEscapes[c];
        }
        if (useUnicode) {
            return toUnicodeEscape(c);
        }
        else {
            return "" + c;
        }
    }

    public static String toJavaString(String s) {
        return toJavaString(s, true);
    }

    public static String toJavaString(String s, boolean useUnicode) {
        if (s == null) {
            return null;
        }
        return toJavaString(s.toCharArray(), useUnicode);
    }

    public static String toJavaString(char[] cs) {
        return toJavaString(cs, true);
    }

    public static String toJavaString(char[] chars, boolean useUnicode) {
        if (chars == null) {
            return null;
        }

        StringBuffer result = new StringBuffer(chars.length);

        for (int i = 0; i < chars.length; i++) {
            result.append(toJavaChar(chars[i], useUnicode));
        }
        return result.toString();
    }

    // Decode unicode escapes in a string
    public static String uuDecode(String s)
            throws IllegalArgumentException {
        if (s == null) {
            return null;
        }

        char[] chars = s.toCharArray();
        char tmp = '\0';
        int t3, t2, t1, t0 = 0;
        StringBuffer result = new StringBuffer(chars.length);

        for (int i = 0; i < chars.length; i++) {
            if ((tmp = chars[i]) == '\\') {
                if (++i >= chars.length) {
                    throw new IllegalArgumentException();
                }
                if ((tmp = chars[i]) == 'u') {
                    if (i + 4 >= chars.length) {
                        throw new IllegalArgumentException();
                    }
                    t3 = Character.digit(chars[i + 1], 16);
                    t2 = Character.digit(chars[i + 2], 16);
                    t1 = Character.digit(chars[i + 3], 16);
                    t0 = Character.digit(chars[i + 4], 16);

                    if ((t3 | t2 | t1 | t0) == -1) {
                        throw new IllegalArgumentException();
                    }
                    tmp = (char)((t3 << 12) + (t2 << 8) + (t1 << 4) + t0);
                    result.append(tmp);
                    i += 4;
                }
                else {
                    result.append('\\').append(tmp);
                }
            }
            else {
                result.append(tmp);
            }
        }
        return result.toString();
    }

    // Decode Java escapes in a string
    public static String fromJavaString(String s)
            throws IllegalArgumentException {
        if (s == null) {
            return null;
        }

        char[] chars = s.toCharArray();
        char tmp = '\0';
        StringBuffer result = new StringBuffer(chars.length);

        for (int i = 0; i < chars.length; i++) {
            // Escape sequence ?
            if (chars[i] == '\\') {
                if (++i >= chars.length) {
                    throw new IllegalArgumentException();
                }
                tmp = chars[i];

                if ((tmp >= '0') && (tmp <= '7')) {
                    // Octal escape
                    StringBuffer octal = new StringBuffer(); // .append(tmp);
                    for (int max = (tmp > '3') ? 1 : 2; max >= 0; max--) {
                        if (i >= chars.length) {
                            i++;
                            break;
                        }
                        if (((tmp = chars[i]) < '0') || (tmp > '7')) {
                            break;
                        }
                        octal.append(tmp);
                        i++;
                    }

                    i--;

                    tmp = (char)Integer.valueOf(octal.toString(), 8)
                            .intValue();
                    result.append(tmp);
                }
                else if (tmp < '\u00FF') {
                    // Other escape - look it up in the table
                    if ((tmp = escapeChars[tmp]) == '\uFFFF') {
                        throw new IllegalArgumentException(
                                "Unknown escape character '" +
                                        toJavaString("" + tmp) + "'");
                    }
                    else {
                        result.append(tmp);
                    }
                }
                else {
                    throw new IllegalArgumentException();
                }
            }
            else {
                // Ordinary character
                result.append(chars[i]);
            }
        }
        return result.toString();
    }

    public static StringBuffer replace(String s, String original, String _new)
            throws IllegalArgumentException {
        int index = 0, oldIndex = 0;
        int orgLen = original.length();

        if (orgLen < 1) {
            throw new IllegalArgumentException();
        }

        StringBuffer result = new StringBuffer();

        while (true) {
            oldIndex = index;
            index = s.indexOf(original, index);
            if (index < 0) {
                if (oldIndex == 0) {
                    return result.append(s);
                }
                else {
                    return result.append(s.substring(oldIndex));
                }
            }

            result.append(s.substring(oldIndex, index));
            result.append(_new);
            index += orgLen;
        }
    }

    public static String[] split(String s, char split) {
        ArrayList l = new ArrayList();

        int index1 = 0, index2;
        while (index1 < s.length()) {
            if ((index2 = s.indexOf(split, index1)) < 0) {
                l.add(s.substring(index1));
                break;
            }

            l.add(s.substring(index1, index2));
            index1 = index2 + 1;
        }

        return (String[])l.toArray(new String[l.size()]);
    }

    public static String[] split(String s, String split) {
        ArrayList l = new ArrayList();

        int index1 = 0, index2;
        while (index1 < s.length()) {
            if ((index2 = s.indexOf(split, index1)) < 0) {
                l.add(s.substring(index1));
                break;
            }

            l.add(s.substring(index1, index2));
            index1 = index2 + split.length();
        }

        return (String[])l.toArray(new String[l.size()]);
    }

    public static String replace(String s, char original, String _new)
            throws IllegalArgumentException {
        int index = 0, oldIndex = 0;

        StringBuffer result = new StringBuffer();

        while (true) {
            oldIndex = index;
            index = s.indexOf(original, index);
            if (index < 0) {
                if (oldIndex == 0) {
                    return s;
                }
                else {
                    result.append(s.substring(oldIndex));
                    return result.toString();
                }
            }

            result.append(s.substring(oldIndex, index));
            result.append(_new);
            index++;
        }
    }

    public static String addLineIndent(String original, String indent) {
        return replace(original, '\n', "\n" + indent);
    }

    public static char[] fill(char c, int len) {
        char[] result = new char[len];
        if (c != '\0') {
            for (int i = len - 1; i >= 0;) {
                result[i--] = c;
            }
        }
        return result;
    }

    public static String expandLeft(String s, char c, int len) {
        int l = s.length();
        if (l >= len) {
            return s;
        }
        else {
            return (new String(fill(c, len - l))) + s;
        }
    }

    public static String expandRight(String s, char c, int len) {
        int l = s.length();
        if (l >= len) {
            return s;
        }
        else {
            return s + (new String(fill(c, len - l)));
        }
    }

    public static String expandCenter(String s, char c, int len) {
        int l = len - s.length();
        if (l <= 0) {
            return s;
        }
        else {
            return (new String(fill(c, l >> 1))) + s +
                    (new String(fill(c, l >> 1 + l & 1)));
        }
    }

    public static StringBuffer expandLeft(StringBuffer s, char c, int len) {
        int l = s.length();
        if (l >= len) {
            return s;
        }
        else {
            return s.insert(0, fill(c, len - l));
        }
    }

    public static StringBuffer expandRight(StringBuffer s, char c, int len) {
        int l = s.length();
        if (l >= len) {
            return s;
        }
        else {
            return s.append(fill(c, len - l));
        }
    }

    public static StringBuffer expandCenter(StringBuffer s, char c, int len) {
        int l = len - s.length();
        if (l <= 0) {
            return s;
        }
        else {
            return s.insert(0, fill(c, l >> 1)).append(fill(c, l >> 1 + l & 1));
        }
    }

    public static String[] createHTMLReplacementList() {
        String[] result = new String[128];
        result['\t'] = "&nbsp;&nbsp;&nbsp;&nbsp;";
        result['\n'] = "<br>";
        result['\r'] = "";
        result['"'] = "&quot;";
        result['&'] = "&amp;";
        result['<'] = "&lt;";
        result['>'] = "&gt;";
        return result;
    }

    public static String toHTML(String s) {
        return replaceChars(s, createHTMLReplacementList(), ENCODE_HTML);
    }

    public static String replaceChars(String s, String[] replaceList, int enc) {
        char[] conv = s.toCharArray();
        StringBuffer result = new StringBuffer(s.length() * 110 / 100);
        char c;
        String _s;

        for (int i = 0; i < conv.length;) {
            if ((c = conv[i++]) < replaceList.length) {
                if ((_s = replaceList[c]) == null) {
                    result.append(c);
                }
                else {
                    result.append(_s);
                }
            }
            else {
                switch (enc) {
                case ENCODE_FILTER:
                    break;
                case ENCODE_UNICODE:
                    result.append("\\u" + toHexString(c, 4));
                    break;
                case ENCODE_HTML:
                    result.append("&#").append((int)c).append(";");
                    break;
                default:
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    private final static char[] hexDigits = new char[16];
    static {
        for (int i = 0; i < 16; i++) {
            hexDigits[i] = Character.forDigit(i, 16);
        }
    }

    public static String toHexString(long l, int digits) {
        char[] result = new char[digits + 2];
        result[0] = '0';
        result[1] = 'x';

        for (int i = digits + 1; i > 1; i--) {
            result[i] = (hexDigits[(int)(l & 0xf)]);
            l >>= 4;
        }

        return new String(result);
    }

    public static String toHexString(byte[] digits) {
        StringBuffer result = new StringBuffer(digits.length + 2).append("0x");

        byte b;
        for (int i = 0; i < digits.length; i++) {
            b = digits[i];
            result.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
        }

        return result.toString();
    }

    public static byte[] hex2bytes(String s) {
        // Skip leading zeros
        int l = s.length();
        int i = 0;
        while ((i < l) && (s.charAt(i) == '0')) {
            i++;
        }

        if (i >= l) {
            // All zeros
            return new byte[1];
        }

        // Convert to byte array (big-endian)
        l -= i;
        char[] chars = s.substring(i).toCharArray();
        byte[] result;
        int cIdx, rIdx;

        // Handle an odd number of digits
        if ((l & 1) == 1) {
            l = (l >> 1) + 1;
            result = new byte[l];
            result[0] = (byte)Character.digit(chars[0], 16);
            cIdx = rIdx = 1;
        }
        else {
            l >>= 1;
            result = new byte[l];
            cIdx = rIdx = 0;
        }

        while (rIdx < l) {
            result[rIdx++] = (byte)((Character.digit(chars[cIdx++], 16) << 4) +
                    Character.digit(chars[cIdx++], 16));
        }

        return result;
    }

    // Convert a long to string as BITS (ie. -1 is expressed as fff....)
    public static long hex2Long(String s) {
        if (s.length() == 16) {
            long l = Long.parseLong(s.substring(1), 16);
            return l | ((long)Character.digit(s.charAt(0), 16) << 60);
        }
        else {
            return Long.parseLong(s, 16);
        }
    }

    public static String argumentList2String(Object[] args, String delimiter) {
        if (args == null) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                result.append(delimiter);
            }
            result.append(any2String(args[i]));
        }

        return result.toString();
    }

    public static String createTrace(Throwable t) {
        if (t == null) {
            return "";
        }
        StringWriter traceWriter = new StringWriter(256);
        PrintWriter pw = new PrintWriter(traceWriter);
        t.printStackTrace(pw);
        String result = traceWriter.toString();
        pw.close();
        return result;
    }

    /**
     * A simple message formatter, which translates the substrings "{0}", "{1}",
     * ... into the string at the corresponding index in the arguments. This can
     * be used as a substitute for {@link java.text.MessageFormat}, if more than
     * 10 arguments are necessary. <strong>N.B.</strong>All argument
     * descriptions must be present and in order.
     *
     * @param addTo
     *            the buffer to be used (a new one is instantiated if null)
     * @param message
     *            the message format
     * @param args
     *            the formatting arguments
     * @return the modified buffer
     */
    public static StringBuffer simpleMessageFormat(StringBuffer addTo,
            String message, String[] args) {
        if (addTo == null) {
            addTo = new StringBuffer(message.length() + args.length * 5);
        }

        int fromIdx = 0;
        int toIdx;
        for (int i = 0; i < args.length; i++) {
            toIdx = message.indexOf("{" + i + "}", fromIdx);
            if (toIdx < 0) {
                throw new IllegalArgumentException(message);
            }

            addTo.append(message.substring(fromIdx, toIdx)).append(args[i]);
            fromIdx = toIdx + 3 + (i >= 10 ? 1 : 0);
        }
        addTo.append(message.substring(fromIdx));
        return addTo;
    }

    public static String listColumns(StringBuffer addTo, String[] args,
            String leadString, boolean javaEncode, int colWidth, int width) {
        if (addTo == null) {
            addTo = new StringBuffer();
        }
        int w = 0;
        String s;

        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                s = new String(fill(' ', colWidth));
            }
            else {
                s = expandRight(leadString +
                        (javaEncode ? toJavaString(args[i]) : args[i]),
                        ' ', colWidth - 1) + ' ';
            }
            if ((w += s.length()) > width) {
                w = s.length();
                addTo.append('\n');
            }
            addTo.append(s);
        }
        return addTo.toString();
    }

    public static StringBuffer addSeparators(StringBuffer addTo,
            char[] original, String separator, int groupSize) {
        int len = original.length;
        int finalSize = len + len / groupSize;

        if (addTo == null) {
            addTo = new StringBuffer(finalSize);
        }

        int idx = 0;
        int groupCount = len % groupSize;
        if (groupCount > 0) {
            groupCount = groupSize - groupCount;
        }

        while (idx < len) {
            addTo.append(original[idx++]);
            if (++groupCount == groupSize && idx < len) {
                addTo.append(separator);
                groupCount = 0;
            }
        }
        return addTo;
    }

}
