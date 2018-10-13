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
package org.xodonex.hojo;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.GenericArray;
import org.xodonex.hojo.lang.HObject;
import org.xodonex.hojo.lang.Operator;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.func.CompositeFunction;
import org.xodonex.hojo.lang.func.MethodFunction;
import org.xodonex.hojo.lang.ops.AbsOp;
import org.xodonex.hojo.lang.ops.AddOp;
import org.xodonex.hojo.lang.ops.AndOp;
import org.xodonex.hojo.lang.ops.ComposeOp;
import org.xodonex.hojo.lang.ops.ConsOp;
import org.xodonex.hojo.lang.ops.CountSeqOp;
import org.xodonex.hojo.lang.ops.DivOp;
import org.xodonex.hojo.lang.ops.ElemOp;
import org.xodonex.hojo.lang.ops.EqOp;
import org.xodonex.hojo.lang.ops.GeOp;
import org.xodonex.hojo.lang.ops.GtOp;
import org.xodonex.hojo.lang.ops.HexOp;
import org.xodonex.hojo.lang.ops.IEqOp;
import org.xodonex.hojo.lang.ops.INeOp;
import org.xodonex.hojo.lang.ops.IdOp;
import org.xodonex.hojo.lang.ops.IofOp;
import org.xodonex.hojo.lang.ops.IsectOp;
import org.xodonex.hojo.lang.ops.LNotOp;
import org.xodonex.hojo.lang.ops.LeOp;
import org.xodonex.hojo.lang.ops.LtOp;
import org.xodonex.hojo.lang.ops.MaxOp;
import org.xodonex.hojo.lang.ops.MinOp;
import org.xodonex.hojo.lang.ops.ModOp;
import org.xodonex.hojo.lang.ops.MulOp;
import org.xodonex.hojo.lang.ops.NeOp;
import org.xodonex.hojo.lang.ops.NegOp;
import org.xodonex.hojo.lang.ops.NotOp;
import org.xodonex.hojo.lang.ops.OrOp;
import org.xodonex.hojo.lang.ops.PowOp;
import org.xodonex.hojo.lang.ops.SeqOp;
import org.xodonex.hojo.lang.ops.ShlOp;
import org.xodonex.hojo.lang.ops.ShrOp;
import org.xodonex.hojo.lang.ops.ShraOp;
import org.xodonex.hojo.lang.ops.SubOp;
import org.xodonex.hojo.lang.ops.SubsetOp;
import org.xodonex.hojo.lang.ops.XorOp;
import org.xodonex.hojo.lang.type.ArrayType;
import org.xodonex.hojo.lang.type.BigDecimalType;
import org.xodonex.hojo.lang.type.BigIntegerType;
import org.xodonex.hojo.lang.type.BooleanType;
import org.xodonex.hojo.lang.type.ByteType;
import org.xodonex.hojo.lang.type.CharSequenceType;
import org.xodonex.hojo.lang.type.CharacterType;
import org.xodonex.hojo.lang.type.ClassType;
import org.xodonex.hojo.lang.type.CollectionType;
import org.xodonex.hojo.lang.type.DateType;
import org.xodonex.hojo.lang.type.DoubleType;
import org.xodonex.hojo.lang.type.FileType;
import org.xodonex.hojo.lang.type.FloatType;
import org.xodonex.hojo.lang.type.FunctionType;
import org.xodonex.hojo.lang.type.GenericArrayType;
import org.xodonex.hojo.lang.type.GenericCharSequenceType;
import org.xodonex.hojo.lang.type.GenericCollectionType;
import org.xodonex.hojo.lang.type.GenericFunctionType;
import org.xodonex.hojo.lang.type.GenericIteratorType;
import org.xodonex.hojo.lang.type.GenericListType;
import org.xodonex.hojo.lang.type.GenericMapType;
import org.xodonex.hojo.lang.type.GenericNumberType;
import org.xodonex.hojo.lang.type.GenericSetType;
import org.xodonex.hojo.lang.type.GenericType;
import org.xodonex.hojo.lang.type.HClassType;
import org.xodonex.hojo.lang.type.HObjectType;
import org.xodonex.hojo.lang.type.IntegerType;
import org.xodonex.hojo.lang.type.IteratorType;
import org.xodonex.hojo.lang.type.ListType;
import org.xodonex.hojo.lang.type.LongType;
import org.xodonex.hojo.lang.type.MapType;
import org.xodonex.hojo.lang.type.NullType;
import org.xodonex.hojo.lang.type.NumberType;
import org.xodonex.hojo.lang.type.ObjectType;
import org.xodonex.hojo.lang.type.PatternType;
import org.xodonex.hojo.lang.type.SetType;
import org.xodonex.hojo.lang.type.ShortType;
import org.xodonex.hojo.lang.type.StringBufferType;
import org.xodonex.hojo.lang.type.StringType;
import org.xodonex.hojo.lang.type.URLType;
import org.xodonex.hojo.lang.type.VoidType;
import org.xodonex.hojo.lang.type._booleanType;
import org.xodonex.hojo.lang.type._byteType;
import org.xodonex.hojo.lang.type._charType;
import org.xodonex.hojo.lang.type._doubleType;
import org.xodonex.hojo.lang.type._floatType;
import org.xodonex.hojo.lang.type._intType;
import org.xodonex.hojo.lang.type._longType;
import org.xodonex.hojo.lang.type._shortType;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;

/**
 * The runtime library for the Hojo compiler and interpreter, providing the
 * primitive operations used by e.g. operators.
 * 
 * The core part of the code is concerned with value conversions, which is quite
 * complex due to both to the advanced features (including bignum arithmetic,
 * functions as first-class values etc.), but also due to Java's schism between
 * primitive types and proper object instances.
 *
 * Even without any language/interpeter, this library can be useful as an
 * auxiliary for data processing.
 */
public class HojoLib extends ConvertUtils {

    public final static ObjectType OBJ_TYPE = ObjectType.getInstance();
    public final static NullType NULL_TYPE = (NullType)NullType.getInstance();
    public final static VoidType VOID_TYPE = (VoidType)VoidType.getInstance();
    public final static BooleanType BOOLEAN_TYPE = (BooleanType)BooleanType
            .getInstance();
    public final static CharacterType CHARACTER_TYPE = (CharacterType)CharacterType
            .getInstance();
    public final static CharSequenceType CHAR_SEQUENCE_TYPE = (CharSequenceType)CharSequenceType
            .getInstance();
    public final static StringType STRING_TYPE = (StringType)StringType
            .getInstance();
    public final static StringBufferType STRING_BUFFER_TYPE = (StringBufferType)StringBufferType
            .getInstance();
    public final static PatternType PATTERN_TYPE = (PatternType)PatternType
            .getInstance();
    public final static NumberType NUMBER_TYPE = (NumberType)NumberType
            .getInstance();
    public final static IteratorType ITERATOR_TYPE = (IteratorType)IteratorType
            .getInstance();
    public final static CollectionType COLLECTION_TYPE = (CollectionType)CollectionType
            .getInstance();
    public final static ListType LIST_TYPE = (ListType)ListType.getInstance();
    public final static _intType INT_TYPE = (_intType)_intType.getInstance();
    public final static MapType MAP_TYPE = (MapType)MapType.getInstance();
    public final static FunctionType FUNCTION_TYPE = (FunctionType)FunctionType
            .getInstance();
    public final static GenericArrayType TUPLE_TYPE = new GenericArrayType(
            Object[].class, Object.class);
    public final static HObjectType HOBJ_TYPE = (HObjectType)HObjectType
            .getInstance();
    public final static HClassType HC_TYPE = (HClassType)HClassType
            .getInstance();

    // Type cast method for op(...)
    final static Method CAST_METHOD;
    static {
        try {
            CAST_METHOD = Type.class.getMethod("typeCast",
                    new Class[] { Object.class });
        }
        catch (Exception e) {
            throw new HojoException(e, HojoException.ERR_INTERNAL, null, null);
        }
    }

    /*
     * ******************** INTERNAL VALUES (some are used by the parser
     * ********************
     */

    // Built-in types
    final static Type[] types = {
            OBJ_TYPE, FUNCTION_TYPE, VOID_TYPE,
            MAP_TYPE, ArrayType.getInstance(), LIST_TYPE,
            SetType.getInstance(), COLLECTION_TYPE, ITERATOR_TYPE,
            _booleanType.getInstance(), BOOLEAN_TYPE,
            _charType.getInstance(), CHARACTER_TYPE, CHAR_SEQUENCE_TYPE,
            STRING_TYPE, STRING_BUFFER_TYPE, PATTERN_TYPE,
            ClassType.getInstance(),
            DateType.getInstance(), FileType.getInstance(),
            URLType.getInstance(),
            NumberType.getInstance(),
            _byteType.getInstance(), ByteType.getInstance(),
            _shortType.getInstance(), ShortType.getInstance(),
            INT_TYPE, IntegerType.getInstance(), _longType.getInstance(),
            LongType.getInstance(),
            _floatType.getInstance(), FloatType.getInstance(),
            _doubleType.getInstance(), DoubleType.getInstance(),
            BigIntegerType.getInstance(), BigDecimalType.getInstance()
    };

    // Type lookup table
    private final static HashMap typeTbl = new HashMap(types.length << 1);
    static {
        Type t;
        for (int i = 0; i < types.length; i++) {
            t = types[i];
            typeTbl.put(t.toClass(), t);
        }
        typeTbl.put(HOBJ_TYPE.toClass(), HOBJ_TYPE);
        typeTbl.put(Object[].class, TUPLE_TYPE);
    }

    // Built-in operators
    final static Operator[] operators = { // new Operator[OP_COUNT];
            null, null,
            NegOp.getInstance(), NotOp.getInstance(), LNotOp.getInstance(),
            AbsOp.getInstance(), null, null,
            HexOp.getInstance(), null, IdOp.getInstance(),

            PowOp.getInstance(),

            MulOp.getInstance(), DivOp.getInstance(), ModOp.getInstance(),

            AddOp.getInstance(), SubOp.getInstance(), IsectOp.getInstance(),

            ShlOp.getInstance(), ShrOp.getInstance(), ShraOp.getInstance(),

            LtOp.getInstance(), LeOp.getInstance(), GeOp.getInstance(),
            GtOp.getInstance(),
            MinOp.getInstance(), MaxOp.getInstance(), ElemOp.getInstance(),
            SubsetOp.getInstance(),

            EqOp.getInstance(), NeOp.getInstance(), IEqOp.getInstance(),
            INeOp.getInstance(),
            IofOp.getInstance(),

            ConsOp.getInstance(), ComposeOp.getInstance(),

            AndOp.getInstance(), OrOp.getInstance(),
            XorOp.getInstance(),

            null,
            null,

            null, null, SeqOp.getInstance(), CountSeqOp.getInstance(),

            null, null, null, null,
            null, null, null, null, null,
            null, null, null,

            null, null
    };

    /* ******************** CONSTRUCTOR ******************** */

    private HojoLib() {
    }

    /* ******************** CONVERSION FUNCTIONS ******************** */

    public static Type typeOf(Object obj) {
        if (obj == null) {
            return NullType.getInstance();
        }
        else if (obj instanceof Function) {
            Function f = (Function)obj;
            return new GenericFunctionType(f.getClass(),
                    f.getParameterTypes(), f.getReturnType());
        }
        else {
            return typeOf(obj.getClass());
        }
    }

    public static Type typeOf(Class type) {
        if (type == null) {
            return OBJ_TYPE;
        }

        Type typ = (Type)typeTbl.get(type);
        if (typ != null) {
            return typ;
        }
        else if (type.isArray()) {
            return new GenericArrayType(type, type.getComponentType());
        }
        else if (Number.class.isAssignableFrom(type)) {
            return new GenericNumberType(type);
        }
        else if (Collection.class.isAssignableFrom(type)) {
            if (List.class.isAssignableFrom(type)) {
                return new GenericListType(type, Object.class);
            }
            else if (Set.class.isAssignableFrom(type)) {
                return new GenericSetType(type, Object.class);
            }
            else {
                return new GenericCollectionType(type, Object.class);
            }
        }
        else if (Map.class.isAssignableFrom(type)) {
            return new GenericMapType(type, Object.class);
        }
        else if (Function.class.isAssignableFrom(type)) {
            return FunctionType.getInstance();
        }
        else if (CharSequence.class.isAssignableFrom(type)) {
            return new GenericCharSequenceType(type);
        }
        else if (Iterator.class.isAssignableFrom(type)) {
            return new GenericIteratorType(type, Object.class);
        }
        else {
            return new GenericType(type);
        }
    }

    public static Function toFunction(Object o) {
        try {
            if (o == null) {
                return null;
            }
            else if (o instanceof Function) {
                return ((Function)o);
            }
            else if (o instanceof Method) {
                return new MethodFunction((Method)o);
            }
            else {
                return (Function)o;
            }
        }
        catch (Exception e) {
            throw HojoException.wrap(e);
        }
    }

    public static Object toArray(Object o, Class container, Class contained,
            boolean strict) {
        try {
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

                Class ct = c.getComponentType();
                boolean subCompatible = contained.isAssignableFrom(ct);
                int l = Array.getLength(o);
                Object result = Array.newInstance(contained, l);

                if (subCompatible) {
                    for (int i = 0; i < l; i++) {
                        Array.set(result, i, Array.get(o, i));
                    }
                }
                else {
                    Type t = typeOf(contained);
                    for (int i = 0; i < l; i++) {
                        Array.set(result, i, t.typeCast(Array.get(o, i)));
                    }
                }
                return result;
            }

            if (o instanceof Collection) {
                Collection coll = (Collection)o;

                if (contained == Object.class) {
                    return coll.toArray();
                }

                Type t = typeOf(contained);
                int sz = coll.size();
                Iterator it = coll.iterator();

                if (contained.isPrimitive()) {
                    Object result = Array.newInstance(contained, sz);
                    for (int i = 0; i < sz; i++) {
                        Array.set(result, i, t.typeCast(it.next()));
                    }
                    return result;
                }
                else {
                    Object[] result = (Object[])Array.newInstance(contained,
                            sz);
                    for (int i = 0; i < sz; i++) {
                        result[i] = t.typeCast(it.next());
                    }
                    return result;
                    // return
                    // coll.toArray((Object[])Array.newInstance(contained, 0));
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
        catch (Exception e) {
            throw HojoException.wrap(e);
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
        Type t = typeOf(elemType);
        it = l.iterator();
        Object result = Array.newInstance(elemType, sz);
        for (int i = 0; i < sz; i++) {
            Array.set(result, i, t.typeCast(it.next()));
        }
        return result;
    }

    // OP_NEG

    /**
     * @param n
     *            the numeric argument
     * @return <code>-n</code>
     */
    public static Number neg(Number n) {
        int pri = ReflectUtils.getPriority(n);

        switch (pri) {
        case ReflectUtils.NUM_PRI_BDEC:
            return ((BigDecimal)n).negate();
        case ReflectUtils.NUM_PRI_BINT:
            return ((BigInteger)n).negate();
        case ReflectUtils.NUM_PRI_BAD:
            // treat as a double
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Double(-(n.doubleValue()));
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Float(-(n.floatValue()));
        case ReflectUtils.NUM_PRI_LONG:
            return new Long(-(n.longValue()));
        default:
            return new Integer(-(n.intValue()));
        }
    }

    // OP_NOT

    /**
     * @param o
     *            the numeric argument
     * @return <code>~o</code>
     */
    public static Number not(Number o) {
        Number n = toBits(o);
        int pri = ReflectUtils.getPriority(n);

        if (pri <= ReflectUtils.NUM_PRI_INT) {
            return new Integer(~n.intValue());
        }
        else if (pri <= ReflectUtils.NUM_PRI_LONG ||
                pri == ReflectUtils.NUM_PRI_BAD) {
            return new Long(~n.longValue());
        }
        else {
            BigInteger bi = (BigInteger)ReflectUtils.convertTo(n,
                    ReflectUtils.NUM_PRI_BINT);
            return bi.not();
        }
    }

    // OP_LNOT

    /**
     * @param o
     *            the boolean argument
     * @return <code>!o</code>.
     */
    public static Boolean lNot(Boolean o) {
        return toBool(o) ? Boolean.FALSE : Boolean.TRUE;
    }

    // OP_ABS

    /**
     * @param n
     *            the numeric argument
     * @return the absolute value of the argument.
     */
    public static Number abs(Number n) {
        int pri = ReflectUtils.getPriority(n);

        switch (pri) {
        case ReflectUtils.NUM_PRI_BDEC:
            return ((BigDecimal)n).abs();
        case ReflectUtils.NUM_PRI_BINT:
            return ((BigInteger)n).abs();
        case ReflectUtils.NUM_PRI_BAD:
            // treat as a double
        case ReflectUtils.NUM_PRI_DOUBLE:
            double d = n.doubleValue();
            return (d <= 0.0) ? new Double(0.0 - d) : n;
        case ReflectUtils.NUM_PRI_FLOAT:
            float f = ((Float)n).floatValue();
            return (f <= 0.0f) ? new Float(0.0f - f) : n;
        case ReflectUtils.NUM_PRI_LONG:
            long l = n.longValue();
            return (l < 0) ? new Long(-l) : n;
        case ReflectUtils.NUM_PRI_INT:
            int i = n.intValue();
            return (i < 0) ? new Integer(-i) : n;
        default:
            int _i = toInt(n);
            return new Integer((_i < 0) ? -_i : _i);
        }
    }

    // OP_HEX

    /**
     * Converts a number to a hexadecimal string that represents the number
     * (Float and Double are represented by their bit pattern, while a
     * BigDecimal is converted to a BigInteger)
     *
     * @param n
     *            the number to be converted
     * @return the hexadecimal string
     */
    public static String toHexString(Number n) {
        switch (ReflectUtils.getPriority(n)) {
        case ReflectUtils.NUM_PRI_BYTE:
            return StringUtils.toHexString(n.longValue(), 2);
        case ReflectUtils.NUM_PRI_SHORT:
            return StringUtils.toHexString(n.longValue(), 4);
        case ReflectUtils.NUM_PRI_INT:
            return StringUtils.toHexString(n.longValue(), 8);
        case ReflectUtils.NUM_PRI_BAD:
            // treat as a long
        case ReflectUtils.NUM_PRI_LONG:
            return StringUtils.toHexString(n.longValue(), 16);
        case ReflectUtils.NUM_PRI_FLOAT:
            return StringUtils.toHexString(
                    Float.floatToIntBits(((Float)n).floatValue()), 8);
        case ReflectUtils.NUM_PRI_DOUBLE:
            return StringUtils.toHexString(
                    Double.doubleToLongBits(((Double)n).doubleValue()), 16);
        case ReflectUtils.NUM_PRI_BDEC:
            // Convert to BigInteger and fall through to the next case
            n = ((BigDecimal)n).toBigInteger();
        default: // NUM_PRI_BINT
            return StringUtils.toHexString(((BigInteger)n).toByteArray());
        }
    }

    // OP_ID

    /**
     * Identity operation - returns the argument value
     *
     * @param arg
     *            the argument
     * @return <code>arg</code>
     */
    public static Object id(Object arg) {
        return arg;
    }

    // OP_POW

    /**
     * Exponentiation operation.
     *
     * @param n1
     *            the base number
     * @param n2
     *            the exponent
     * @return the base number raised to the exponent
     */
    public static Number pow(Number n1, Number n2) {
        int pri1 = ReflectUtils.getPriority(n1);
        int pri2 = ReflectUtils.getPriority(n2);
        int pri;

        switch (pri1) {
        case ReflectUtils.NUM_PRI_BDEC:
            pri = ReflectUtils.NUM_PRI_DOUBLE;
            break;
        case ReflectUtils.NUM_PRI_BINT:
            return ((BigInteger)n1).pow(n2.intValue());
        default:
            pri = (pri1 > pri2) ? pri1 : pri2;
            if (pri > ReflectUtils.NUM_PRI_DOUBLE) {
                pri = ReflectUtils.NUM_PRI_DOUBLE;
            }
        }

        return ReflectUtils.convertTo(
                Math.pow(n1.doubleValue(), n2.doubleValue()), pri);
    }

    // OP_MUL

    /**
     * Multiplication.
     *
     * @param n1
     *            the first operand
     * @param n2
     *            the second operand
     * @return <code>n1 * n2</code>
     */
    public static Number mul(Number n1, Number n2) {
        int resultPri = ReflectUtils.getResultPriority(n1, n2);

        switch (resultPri) {
        case ReflectUtils.NUM_PRI_BDEC:
            BigDecimal bd1 = (BigDecimal)ReflectUtils.convertTo(n1, resultPri);
            BigDecimal bd2 = (BigDecimal)ReflectUtils.convertTo(n2, resultPri);
            return bd1.multiply(bd2);
        case ReflectUtils.NUM_PRI_BINT:
            BigInteger bi1 = (BigInteger)ReflectUtils.convertTo(n1, resultPri);
            BigInteger bi2 = (BigInteger)ReflectUtils.convertTo(n2, resultPri);
            return bi1.multiply(bi2);
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Float(n1.floatValue() * n2.floatValue());
        case ReflectUtils.NUM_PRI_BAD:
            // treat as a double
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Double(n1.doubleValue() * n2.doubleValue());
        default:
            return ReflectUtils.convertTo(
                    n1.longValue() * n2.longValue(),
                    resultPri == ReflectUtils.NUM_PRI_BAD
                            ? ReflectUtils.NUM_PRI_LONG
                            : resultPri);
        }
    }

    /**
     * Cartesian product
     *
     * @param o1
     *            the first operand
     * @param o2
     *            the second operand
     * @return <code>n1 x n2</code>
     */
    public static Object[][] mul(Object[] o1, Object[] o2) {
        Object[][] result = new Object[o1.length * o2.length][];
        for (int i = 0; i < o1.length; i++) {
            for (int j = 0; j < o2.length; j++) {
                result[i * o2.length + j] = new Object[] { o1[i], o2[j] };
            }
        }
        return result;
    }

    // OP_DIV

    /**
     * Division.
     *
     * @param n1
     *            divisor
     * @param n2
     *            dividend
     * @return <code>n1 / n2</code>
     */
    public static Number div(Number n1, Number n2) {
        int resultPri = ReflectUtils.getResultPriority(n1, n2);

        switch (resultPri) {
        case ReflectUtils.NUM_PRI_BDEC:
            BigDecimal bd1 = (BigDecimal)ReflectUtils.convertTo(n1, resultPri);
            BigDecimal bd2 = (BigDecimal)ReflectUtils.convertTo(n2, resultPri);
            return bd1.divide(bd2, BigDecimal.ROUND_HALF_UP);
        case ReflectUtils.NUM_PRI_BINT:
            BigInteger bi1 = (BigInteger)ReflectUtils.convertTo(n1, resultPri);
            BigInteger bi2 = (BigInteger)ReflectUtils.convertTo(n2, resultPri);
            return bi1.divide(bi2);
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Float(n1.floatValue() / n2.floatValue());
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Double(n1.doubleValue() / n2.doubleValue());
        default:
            return ReflectUtils.convertTo(
                    n1.longValue() / n2.longValue(),
                    resultPri == ReflectUtils.NUM_PRI_BAD
                            ? ReflectUtils.NUM_PRI_LONG
                            : resultPri);
        }
    }

    // OP_MOD

    /**
     * Modulo.
     *
     * @param n1
     *            the base
     * @param n2
     *            the modulo
     * @return <code>n1 % n2</code>
     */
    public static Number mod(Number n1, Number n2) {
        int resultPri = ReflectUtils.getResultPriority(n1, n2);

        switch (resultPri) {
        case ReflectUtils.NUM_PRI_BDEC:
        case ReflectUtils.NUM_PRI_BINT:
            resultPri = ReflectUtils.NUM_PRI_BINT;
            BigInteger bi1 = (BigInteger)ReflectUtils.convertTo(n1, resultPri);
            BigInteger bi2 = (BigInteger)ReflectUtils.convertTo(n2, resultPri);
            return bi1.mod(bi2);
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Double(n1.doubleValue() % n2.doubleValue());
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Float(n1.floatValue() % n1.floatValue());
        default:
            return ReflectUtils.convertTo(
                    n1.longValue() % n2.longValue(), resultPri);
        }
    }

    // OP_ADD

    /**
     * Addition.
     *
     * @param n1
     *            the first operand
     * @param n2
     *            the second operand
     * @return <code>n1 + n2</code>
     */
    public static Number add(Number n1, Number n2) {
        int resultPri = ReflectUtils.getResultPriority(n1, n2);

        switch (resultPri) {
        case ReflectUtils.NUM_PRI_BDEC:
            BigDecimal bd1 = (BigDecimal)ReflectUtils.convertTo(n1, resultPri);
            BigDecimal bd2 = (BigDecimal)ReflectUtils.convertTo(n2, resultPri);
            return bd1.add(bd2);
        case ReflectUtils.NUM_PRI_BINT:
            BigInteger bi1 = (BigInteger)ReflectUtils.convertTo(n1, resultPri);
            BigInteger bi2 = (BigInteger)ReflectUtils.convertTo(n2, resultPri);
            return bi1.add(bi2);
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Float(n1.floatValue() + n2.floatValue());
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Double(n1.doubleValue() + n2.doubleValue());
        default:
            return ReflectUtils.convertTo(
                    n1.longValue() + n2.longValue(), resultPri);
        }
    }

    /**
     * String concatenation.
     *
     * @param s1
     *            the first operand
     * @param o2
     *            the second operand
     * @return <code>s1 + o2</code>
     */
    public static String add(String s1, Object o2) {
        return s1 + toString(o2);
    }

    /**
     * String concatenation.
     *
     * @param s1
     *            the first operand
     * @param o2
     *            the second operand
     * @return <code>s1 + o2</code>
     */
    public static StringBuffer add(StringBuffer s1, Object o2) {
        return s1.append(o2);
    }

    /**
     * Collection addition.
     *
     * @param c
     *            the collection
     * @param o2
     *            the element to be added
     * @return the updated collection
     */
    public static Collection add(Collection c, Object o2) {
        if (o2 instanceof Collection) {
            c.addAll((Collection)o2);
        }
        else {
            c.add(o2);
        }
        return c;
    }

    /**
     * Map union.
     *
     * @param m
     *            the map to be updated
     * @param o2
     *            the map of updates
     * @return the updated map
     */
    public static Map add(Map m, Map o2) {
        m.putAll(o2);
        return m;
    }

    // OP_SUB

    /**
     * Subtraction.
     *
     * @param n1
     *            the first operand
     * @param n2
     *            the second operand
     * @return <code>n1 - n2</code>
     */
    public static Number sub(Number n1, Number n2) {
        int resultPri = ReflectUtils.getResultPriority(n1, n2);

        switch (resultPri) {
        case ReflectUtils.NUM_PRI_BDEC:
            BigDecimal bd1 = (BigDecimal)ReflectUtils.convertTo(n1, resultPri);
            BigDecimal bd2 = (BigDecimal)ReflectUtils.convertTo(n2, resultPri);
            return bd1.subtract(bd2);
        case ReflectUtils.NUM_PRI_BINT:
            BigInteger bi1 = (BigInteger)ReflectUtils.convertTo(n1, resultPri);
            BigInteger bi2 = (BigInteger)ReflectUtils.convertTo(n2, resultPri);
            return bi1.subtract(bi2);
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Float(n1.floatValue() - n2.floatValue());
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Double(n1.doubleValue() - n2.doubleValue());
        default:
            return ReflectUtils.convertTo(
                    n1.longValue() - n2.longValue(), resultPri);
        }
    }

    /**
     * Collection difference.
     *
     * @param c1
     *            the collection to be modified
     * @param obj
     *            the element(s) to be removed
     * @return the modified collection
     */
    public static Collection sub(Collection c1, Object obj) {
        if (obj instanceof Collection) {
            c1.removeAll((Collection)obj);
        }
        else {
            c1.remove(obj);
        }
        return c1;
    }

    // OP_ISECT

    /**
     * Collection intersection.
     *
     * @param c1
     *            The collection to be updated
     * @param c2
     *            The collection of elements to be retained
     * @return the updated collection
     */
    public static Collection isect(Collection c1, Collection c2) {
        c1.retainAll(c2);
        return c1;
    }

    // OP_SHL

    /**
     * Bitwise shift left.
     *
     * @param n
     *            the base number
     * @param bits
     *            the number of positions to shift the base number
     * @return <code>n &lt;&lt; bits</code>
     */
    public static Number shl(Number n, int bits) {
        if (bits == 0) {
            return n;
        }
        else if (bits < 0) {
            return shr(n, -bits);
        }

        int pri = ReflectUtils.getPriority(n);

        switch (pri) {
        case ReflectUtils.NUM_PRI_BDEC:
        case ReflectUtils.NUM_PRI_BINT:
            BigInteger bi = (BigInteger)ReflectUtils.convertTo(n,
                    ReflectUtils.NUM_PRI_BINT);
            return bi.shiftLeft(bits);
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Long(Double.doubleToLongBits(n.doubleValue()) << bits);
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Integer(Float.floatToIntBits(n.floatValue()) << bits);
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_LONG:
            return new Long(n.longValue() << bits);
        default: // ReflectUtils.NUM_PRI_INT or below:
            return new Integer(n.intValue() << bits);
        }
    }

    // OP_SHR

    /**
     * Bitwise shift right, with sign extension.
     *
     * @param n
     *            the base number
     * @param bits
     *            the number of positions to shift the base number
     * @return <code>n &gt;&gt; bits</code>
     */
    public static Number shr(Number n, int bits) {
        if (bits == 0) {
            return n;
        }
        else if (bits < 0) {
            return shl(n, -bits);
        }

        int pri = ReflectUtils.getPriority(n);

        switch (pri) {
        case ReflectUtils.NUM_PRI_BDEC:
        case ReflectUtils.NUM_PRI_BINT:
            BigInteger bi = (BigInteger)ReflectUtils.convertTo(n,
                    ReflectUtils.NUM_PRI_BINT);
            return bi.shiftRight(bits);
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Long(Double.doubleToLongBits(n.doubleValue()) >> bits);
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Integer(Float.floatToIntBits(n.floatValue()) >> bits);
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_LONG:
            return new Long(n.longValue() >> bits);
        default: // ReflectUtils.NUM_PRI_INT or below:
            return new Integer(n.intValue() >> bits);
        }
    }

    // OP_SHRA

    /**
     * Bitwise shift right, without sign extension.
     *
     * @param n
     *            the base number
     * @param bits
     *            the number of positions to shift the base number
     * @return <code>n &gt;&gt;&gt; bits</code>
     */
    public static Number shra(Number n, int bits) {
        if (bits == 0) {
            return n;
        }
        else if (bits < 0) {
            return shl(n, -bits);
        }

        int pri = ReflectUtils.getPriority(n);

        switch (pri) {
        case ReflectUtils.NUM_PRI_BDEC:
        case ReflectUtils.NUM_PRI_BINT:
            BigInteger bi = (BigInteger)ReflectUtils.convertTo(n,
                    ReflectUtils.NUM_PRI_BINT);
            return bi.shiftRight(bits);
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Long(Double.doubleToLongBits(n.doubleValue()) >>> bits);
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Integer(Float.floatToIntBits(n.floatValue()) >>> bits);
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_LONG:
            return new Long(n.longValue() >>> bits);
        default: // ReflectUtils.NUM_PRI_INT or below:
            return new Integer(n.intValue() >>> bits);
        }
    }

    // OP_LT

    /**
     * Comparison, less-than.
     *
     * @param o1
     *            the left-hand value
     * @param o2
     *            the right-hand value
     * @return <code>o1 &lt; o2</code>
     */
    public static boolean lt(Comparable o1, Comparable o2) {
        return (compareTo(o1, o2) < 0);
    }

    // OP_LE

    /**
     * Comparison, less-than-or-equal.
     *
     * @param o1
     *            the left-hand value
     * @param o2
     *            the right-hand value
     * @return <code>o1 &lt;= o2</code>
     */
    public static boolean le(Comparable o1, Comparable o2) {
        return (compareTo(o1, o2) <= 0);
    }

    // OP_GE

    /**
     * Comparison, greater-than-or-equal.
     *
     * @param o1
     *            the left-hand value
     * @param o2
     *            the right-hand value
     * @return <code>o1 &gt;= o2</code>
     */
    public static boolean ge(Comparable o1, Comparable o2) {
        return (compareTo(o1, o2) >= 0);
    }

    // OP_GT

    /**
     * Comparison, greater-than.
     *
     * @param o1
     *            the left-hand value
     * @param o2
     *            the right-hand value
     * @return <code>o1 &gt; o2</code>
     */
    public static boolean gt(Comparable o1, Comparable o2) {
        return (compareTo(o1, o2) > 0);
    }

    // OP_MIN

    /**
     * Minimum value.
     *
     * @param o1
     *            the first value
     * @param o2
     *            the second value
     * @return the smallest of the two values
     */
    public static Comparable min(Comparable o1, Comparable o2) {
        // Try to convert to numbers
        Number n1 = toNumberOpt(o1, true);
        Number n2 = toNumberOpt(o2, true);
        if ((n1 == NaN) || (n2 == NaN)) {
            // Both operands could not be converted to numbers - use
            // .compareTo()
            return (o1.compareTo(o2) <= 0) ? o1 : o2;
        }

        // Perform number type promotion - otherwise .equals() will return false
        int r_pri = ReflectUtils.getResultPriority(n1, n2);
        switch (r_pri) {
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_DOUBLE:
            // Perform extended comparison (handle NaN etc.)
            return new Double(Math.min(toDouble(n1), toDouble(n2)));
        case ReflectUtils.NUM_PRI_FLOAT:
            // Perform extended comparison (handle NaN etc.)
            return new Float(Math.min(toFloat(n1), toFloat(n2)));
        default:
            n1 = ReflectUtils.convertTo(n1, r_pri);
            n2 = ReflectUtils.convertTo(n2, r_pri);
            return (compareTo((Comparable)n1, (Comparable)n2) <= 0)
                    ? (Comparable)n1
                    : (Comparable)n2;
        }
    }

    // OP_MAX

    /**
     * Maximum value.
     *
     * @param o1
     *            the first value
     * @param o2
     *            the second value
     * @return the largest of the two values
     */
    public static Comparable max(Comparable o1, Comparable o2) {
        // Try to convert to numbers
        Number n1 = toNumberOpt(o1, true);
        Number n2 = toNumberOpt(o2, true);
        if ((n1 == NaN) || (n2 == NaN)) {
            // Both operands could not be converted to numbers - use
            // .compareTo()
            return (o1.compareTo(o2) > 0) ? o1 : o2;
        }

        // Perform number type promotion - otherwise .equals() will return false
        int r_pri = ReflectUtils.getResultPriority(n1, n2);
        switch (r_pri) {
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_DOUBLE:
            // Perform extended comparison (handle NaN etc.)
            return new Double(Math.max(toDouble(n1), toDouble(n2)));
        case ReflectUtils.NUM_PRI_FLOAT:
            // Perform extended comparison (handle NaN etc.)
            return new Float(Math.max(toFloat(n1), toFloat(n2)));
        default:
            n1 = ReflectUtils.convertTo(n1, r_pri);
            n2 = ReflectUtils.convertTo(n2, r_pri);
            return (compareTo((Comparable)n1, (Comparable)n2) >= 0)
                    ? (Comparable)n1
                    : (Comparable)n2;
        }
    }

    /**
     * General comparison.
     *
     * @param o1
     *            the first value
     * @param o2
     *            the second value
     * @return -1 if <code>o1 &lt; o2</code>, 1 if <code>o1 &gt; o2</code> else
     *         0.
     */
    public static int compareTo(Comparable o1, Comparable o2) {
        // Try to convert to numbers
        Number n1 = toNumberOpt(o1, true);
        Number n2 = toNumberOpt(o2, true);
        if ((n1 == NaN) || (n2 == NaN)) {
            // Both operands could not be converted to numbers - use
            // .compareTo()
            return o1.compareTo(o2);
        }

        // Perform number type promotion - otherwise .equals() will return false
        int r_pri = ReflectUtils.getResultPriority(n1, n2);

        switch (r_pri) {
        case ReflectUtils.NUM_PRI_BDEC:
            return ((BigDecimal)ReflectUtils.convertTo(n1, r_pri))
                    .compareTo((BigDecimal)ReflectUtils.convertTo(n2, r_pri));
        case ReflectUtils.NUM_PRI_BINT:
            return ((BigInteger)ReflectUtils.convertTo(n1, r_pri))
                    .compareTo((BigInteger)ReflectUtils.convertTo(n2, r_pri));
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_DOUBLE:
        case ReflectUtils.NUM_PRI_FLOAT:
            double d1 = n1.doubleValue();
            double d2 = n2.doubleValue();
            return (d1 == d2) ? 0 : (d1 < d2) ? -1 : 1;
        default:
            long l1 = n1.longValue();
            long l2 = n2.longValue();
            return (l1 == l2) ? 0 : (l1 < l2) ? -1 : 1;
        }
    }

    // OP_ELEM

    /**
     * Collection membership.
     *
     * @param e
     *            the element to test for
     * @param c
     *            the collection
     * @return whether e is an element of c.
     */
    public static boolean elem(Object e, Collection c) {
        return c.contains(e);
    }

    // OP_SUBSET

    /**
     * Collection inclusion.
     *
     * @param c1
     *            the first collection
     * @param c2
     *            the second collection
     * @return whether c1 is a subset of c2
     */
    public static boolean subset(Collection c1, Collection c2) {
        return c2.containsAll(c1);
    }

    // OP_EQ

    /**
     * Equality comparison.
     *
     * @param o1
     *            the first value
     * @param o2
     *            the second value
     * @return whether the first and second value are semantically equal. This
     *         is an extended, recursive {@link Object#equals(Object)} including
     *         numeric promotion.
     */
    public static boolean eq(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }

        Class c1 = o1 == null ? Object.class : o1.getClass();
        Class c2 = o2 == null ? Object.class : o2.getClass();

        // Use an element-by-element comparison, if the left-hand object is an
        // array
        if (c1.isArray()) {
            if (o1 == o2) {
                return true;
            }
            else if (o1 == null || o2 == null) {
                return false;
            }
            if (!c2.isArray()) {
                return false;
            }

            c1 = c1.getComponentType();
            c2 = c2.getComponentType();
            Object e1, e2;
            if (c1 != c2) {
                return false;
            }
            if (c1.isPrimitive()) {
                int l = Array.getLength(o1);
                if (Array.getLength(o2) != l) {
                    return false;
                }
                for (int i = 0; i < l; i++) {
                    e1 = Array.get(o1, i);
                    e2 = Array.get(o2, i);
                    if ((e1 == null && e2 != null)
                            || (e1 != null && !eq(e1, e2))) {
                        return false;
                    }
                }
                return true;
            }
            else {
                Object[] ar1 = (Object[])o1;
                Object[] ar2 = (Object[])o2;
                if (ar1.length != ar2.length) {
                    return false;
                }
                for (int i = 0; i < ar1.length; i++) {
                    if ((ar1[i] == null && ar2[i] != null) ||
                            (ar1[i] != null && !eq(ar1[i], ar2[i]))) {
                        return false;
                    }
                }
                return true;
            }
        }

        // Try simple comparisons, if the types are equal and not numeric
        if (c1 == c2 && !Number.class.isAssignableFrom(c1)) {
            return o1 == null ? o2 == null : o1.equals(o2);
        }

        // Try to convert to numbers
        Number n1 = toNumberOpt(o1, true);
        Number n2 = (n1 == NaN) ? NaN : toNumberOpt(o2, true);
        if ((n1 == NaN) || (n2 == NaN)) {
            // Both operands could not be converted to numbers - use .equals()
            // on the original objects
            return o1.equals(o2);
        }

        // Perform number type promotion - otherwise .equals() will return false
        int r_pri = ReflectUtils.getResultPriority(n1, n2);
        switch (r_pri) {
        case ReflectUtils.NUM_PRI_BAD:
        case ReflectUtils.NUM_PRI_DOUBLE:
            return (n1 == null ? 0.0 : n1.doubleValue()) == (n2 == null ? 0.0
                    : n2.doubleValue());
        case ReflectUtils.NUM_PRI_FLOAT:
            return (n1 == null ? 0.0f : n1.floatValue()) == (n2 == null ? 0.0f
                    : n2.floatValue());
        case ReflectUtils.NUM_PRI_BYTE:
        case ReflectUtils.NUM_PRI_SHORT:
        case ReflectUtils.NUM_PRI_INT:
        case ReflectUtils.NUM_PRI_LONG:
            return (n1 == null ? 0L : n1.longValue()) == (n2 == null ? 0L
                    : n2.longValue());
        default:
            n1 = ReflectUtils.convertTo(n1, r_pri);
            n2 = ReflectUtils.convertTo(n2, r_pri);
            return n1.equals(n2);
        }
    }

    // OP_NE

    /**
     * Inequality.
     * 
     * @param o1
     *            the first value
     * @param o2
     *            the second value
     * @return <code>!eq(o1, o2)</code>
     * @see #eq(Object, Object)
     */
    public static boolean ne(Object o1, Object o2) {
        return !eq(o1, o2);
    }

    // OP_IOF

    /**
     * Instance test.
     *
     * @param o
     *            the object
     * @param c
     *            the class
     * @return whether the object is an instance of the class.
     */
    public static boolean iof(Object o, Class c) {
        if (c == null) {
            throw HojoException.wrap(new NullPointerException());
        }
        else if (o == null) {
            return false;
        }
        else if (c == GenericArray.class) {
            return o.getClass().isArray();
        }
        else {
            return c.isInstance(o);
        }
    }

    // OP_CONS

    /**
     * List construction "cons".
     *
     * @param hd
     *            the list head
     * @param tl
     *            the list tail
     * @return the tail extended with the head
     */
    public static List cons(Object hd, List tl) {
        tl.add(0, hd);
        return tl;
    }

    // OP_COMPOSE

    /**
     * Function composition
     *
     * @param l
     *            the left-hand function
     * @param r
     *            the right-hand function
     * @return the functional composite of l and r (l o r)
     */
    public static Function compose(Function l, Function r) {
        return new CompositeFunction(l, r);
    }

    // OP_AND

    /**
     * Logical AND.
     *
     * @param o1
     *            the first condition
     * @param o2
     *            the second condition
     * @return <code>o1 &amp;&amp; o2</code>
     */
    public static Boolean and(Boolean o1, Object o2) {
        return o1.booleanValue() && toBool(o2) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Numeric AND.
     *
     * @param o1
     *            the first number
     * @param o2
     *            the second number
     * @return <code>o1 &amp; o2</code>
     */
    public static Number and(Number o1, Number o2) {
        Number n1 = toBits(o1);
        Number n2 = toBits(o2);
        int pri = ReflectUtils.getResultPriority(n1, n2);

        if (pri <= ReflectUtils.NUM_PRI_LONG) {
            long res = n1.longValue() & n2.longValue();
            return (pri <= ReflectUtils.NUM_PRI_INT)
                    ? (Number)(new Integer((int)res))
                    : (Number)(new Long(res));
        }
        else {
            pri = ReflectUtils.NUM_PRI_BINT;
            BigInteger bi1 = (BigInteger)ReflectUtils.convertTo(n1, pri);
            BigInteger bi2 = (BigInteger)ReflectUtils.convertTo(n2, pri);
            return bi1.and(bi2);
        }
    }

    // OP_OR

    /**
     * Logical OR.
     *
     * @param o1
     *            the first condition
     * @param o2
     *            the second condition
     * @return <code>o1 || o2</code>
     */
    public static Boolean or(Boolean o1, Object o2) {
        return (o1.booleanValue() || toBool(o2)) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Numeric OR.
     *
     * @param o1
     *            the first number
     * @param o2
     *            the second number
     * @return <code>o1 | o2</code>
     */
    public static Number or(Number o1, Number o2) {
        Number n1 = toBits(o1);
        Number n2 = toBits(o2);
        int pri = ReflectUtils.getResultPriority(n1, n2);

        if (pri <= ReflectUtils.NUM_PRI_LONG) {
            long res = n1.longValue() | n2.longValue();
            return (pri <= ReflectUtils.NUM_PRI_INT)
                    ? (Number)(new Integer((int)res))
                    : (Number)(new Long(res));
        }
        else {
            pri = ReflectUtils.NUM_PRI_BINT;
            BigInteger bi1 = (BigInteger)ReflectUtils.convertTo(n1, pri);
            BigInteger bi2 = (BigInteger)ReflectUtils.convertTo(n2, pri);
            return bi1.or(bi2);
        }
    }

    // OP_XOR

    /**
     * Logical XOR.
     *
     * @param o1
     *            the first condition
     * @param o2
     *            the second condition
     * @return <code>o1 ^ o2</code>
     */
    public static Boolean xor(Boolean o1, Object o2) {
        return (o1.booleanValue() ^ toBool(o2)) ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Numeric XOR.
     *
     * @param o1
     *            the first number
     * @param o2
     *            the second number
     * @return <code>o1 ^ o2</code>
     */
    public static Number xor(Number o1, Number o2) {
        Number n1 = toBits(o1);
        Number n2 = toBits(o2);
        int pri = ReflectUtils.getResultPriority(n1, n2);

        if (pri <= ReflectUtils.NUM_PRI_LONG) {
            long res = n1.longValue() ^ n2.longValue();
            return (pri <= ReflectUtils.NUM_PRI_INT)
                    ? (Number)(new Integer((int)res))
                    : (Number)(new Long(res));
        }
        else {
            pri = ReflectUtils.NUM_PRI_BINT;
            BigInteger bi1 = (BigInteger)ReflectUtils.convertTo(n1, pri);
            BigInteger bi2 = (BigInteger)ReflectUtils.convertTo(n2, pri);
            return bi1.xor(bi2);
        }
    }

    // OP_SEQ

    private static Function ADD_1 = new StandardFunction() {
        private static final long serialVersionUID = 1L;

        @Override
        public Object invoke(Object[] args) {
            return add(toNumber(args[0], true), ONE_INT);
        }

        @Override
        public Class getReturnType() {
            return Number.class;
        }

        private final Class[] ATYPES = { Number.class };

        @Override
        public Class[] getParameterTypes() {
            return ATYPES;
        }
    };

    private static Function SUB_1 = new StandardFunction() {
        private static final long serialVersionUID = 1L;

        @Override
        public Object invoke(Object[] args) {
            return sub(toNumber(args[0], true), ONE_INT);
        }

        @Override
        public Class getReturnType() {
            return Number.class;
        }

        private final Class[] ATYPES = { Number.class };

        @Override
        public Class[] getParameterTypes() {
            return ATYPES;
        }
    };

    final static Comparable NO_VALUE = new Integer(0);

    private final static class Seq implements Iterator {
        Comparable lastValue;
        Comparable finalValue;
        boolean endOnLower;
        Function generator;
        Type conv;

        Boolean hasNext = null; // the result of the last hasNext() evaluation

        public Seq(Comparable lastValue, Comparable finalValue,
                boolean endOnLower, Function generator) {
            this.lastValue = lastValue;
            this.finalValue = finalValue;
            this.endOnLower = endOnLower;
            if ((this.generator = generator) == null) {
                throw new NullPointerException();
            }
            conv = typeOf(lastValue);
        }

        @Override
        public boolean hasNext() {
            if (hasNext != null) {
                return hasNext.booleanValue();
            }

            int cmp = lastValue.compareTo(finalValue);
            if (endOnLower ? cmp <= 0 : cmp >= 0) {
                // lastValue is the final value
                finalValue = NO_VALUE;

                if (cmp != 0) {
                    hasNext = Boolean.FALSE;
                    return false;
                }
            }
            hasNext = Boolean.TRUE;
            return true;
        }

        @Override
        public Object next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Object result = lastValue;
            if (finalValue == NO_VALUE) {
                hasNext = Boolean.FALSE;
            }
            else {
                hasNext = null;
                lastValue = (Comparable)conv.typeCast(
                        generator.invoke(new Object[] { lastValue }));
            }
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private final static class CountSeq implements Iterator {
        int count;
        Function generator;
        Object value;
        boolean needGenerate = false;

        public CountSeq(Object initial, int count, Function generator) {
            value = initial;
            this.count = count;
            if ((this.generator = generator) == null) {
                throw new NullPointerException();
            }
        }

        @Override
        public boolean hasNext() {
            return (count > 0);
        }

        @Override
        public Object next() {
            if (count <= 0) {
                throw new NoSuchElementException();
            }

            // generate the next value, if necessary
            if (needGenerate) {
                value = generator.invoke(new Object[] { value });
            }
            needGenerate = true;

            if (--count == 0) {
                // clean up - done
                Object result = value;
                value = generator = null;
                return result;
            }
            else {
                return value;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Sequence generator.
     *
     * @param from
     *            the lower boundary value
     * @param to
     *            the higher boundary value
     * @param generate
     *            the function generating the next value in sequence from the
     *            prior.
     * @return an iterator for the described sequence.
     *         <strong>N.B:</strong>Divergent behaviour is possible, depending
     *         on choice of generator function and boundary values.
     */
    public static Iterator seq(Comparable from, Comparable to,
            Function generate) {
        boolean descending;
        if (from.compareTo(to) < 0) {
            descending = false;
            if (generate == null) {
                generate = ADD_1;
            }
        }
        else {
            descending = true;
            if (generate == null) {
                generate = SUB_1;
            }
        }

        return new Seq(from, to, descending, generate);
    }

    /**
     * Countable (bounded) sequence generator.
     *
     * @param initial
     *            the initial value
     * @param count
     *            the number of values to generate
     * @param generate
     *            the function generating the next value in sequence from the
     *            prior.
     * @return an iterator for the described sequence.
     */
    public static Iterator countSeq(Object initial, int count,
            Function generate) {
        return new CountSeq(initial, count,
                generate == null ? ADD_1 : generate);
    }

    /**
     * Calculate <code>n + 1</code> without changing the type of <code>n</code>.
     *
     * @param n
     *            the number
     * @param increase
     *            whether to increase the number or decrease it.
     * @return <code>n + 1</code> on increase, <code>n - 1</code> on decrease.
     */
    public static Number incDec(Number n, boolean increase) {
        int pri = ReflectUtils.getPriority(n);
        int change = increase ? 1 : -1;

        switch (pri) {
        case ReflectUtils.NUM_PRI_BDEC:
            if (increase) {
                return ((BigDecimal)n).add(ONE_BDEC);
            }
            else {
                return ((BigDecimal)n).subtract(ONE_BDEC);
            }
        case ReflectUtils.NUM_PRI_BINT:
            if (increase) {
                return ((BigInteger)n).add(ONE_BINT);
            }
            else {
                return ((BigInteger)n).subtract(ONE_BINT);
            }
        case ReflectUtils.NUM_PRI_DOUBLE:
            return new Double(((Double)n).doubleValue() + change);
        case ReflectUtils.NUM_PRI_FLOAT:
            return new Float(((Float)n).floatValue() + change);
        case ReflectUtils.NUM_PRI_LONG:
            return new Long(((Long)n).longValue() + change);
        case ReflectUtils.NUM_PRI_INT:
            return new Integer(((Integer)n).intValue() + change);
        case ReflectUtils.NUM_PRI_SHORT:
            return new Short((short)(((Short)n).shortValue() + change));
        default: // NUM_PRI_BYTE
            return new Byte((byte)(((Byte)n).byteValue() + change));
        }
    }

    /**
     * Determine the resulting numeric type of an unary operation on a value of
     * the specified type.
     * 
     * @param arg
     *            the operand type
     * @return the type resulting from an operation on an argument of type
     *         <code>arg</code>.
     */
    public final static NumberType numericOpType(Type arg) {
        return numericOpType(arg, false);
    }

    /**
     * Determine the resulting numeric type of an unary operation on a value of
     * the specified type.
     * 
     * @param arg
     *            the operand type
     * @param integral
     *            whether to limit the result to integral types even when
     *            floating-point types are involved.
     * @return the type resulting from an unary operation on an argument of type
     *         <code>arg</code>.
     */
    public static NumberType numericOpType(Type arg, boolean integral) {
        int pri;

        switch (arg.kind()) {
        case Type.TYP_NULL:
        case Type.TYP_BOOLEAN:
        case Type.TYP_CHAR:
            pri = ReflectUtils.NUM_PRI_INT;
            break;
        case Type.TYP_NUMBER:
            pri = ((NumberType)arg).numberType();
            break;
        case Type.TYP_DATE:
            pri = ReflectUtils.NUM_PRI_LONG;
            break;
        case Type.TYP_ARRAY:
            if (arg.toClass() == byte[].class) {
                pri = ReflectUtils.NUM_PRI_BAD;
            }
            else {
                // cannot cast value
                return null;
            }
            break;
        default:
            return null;
        }

        if (pri == ReflectUtils.NUM_PRI_BAD) {
            return NUMBER_TYPE;
        }

        if (pri < ReflectUtils.NUM_PRI_INT) {
            pri = ReflectUtils.NUM_PRI_INT;
        }
        if (integral) {
            switch (pri) {
            case ReflectUtils.NUM_PRI_FLOAT:
                pri = ReflectUtils.NUM_PRI_INT;
                break;
            case ReflectUtils.NUM_PRI_DOUBLE:
                pri = ReflectUtils.NUM_PRI_LONG;
                break;
            case ReflectUtils.NUM_PRI_BDEC:
                pri = ReflectUtils.NUM_PRI_BINT;
                break;
            }
        }

        return (NumberType)typeOf(ReflectUtils.getNumberClass(pri));
    }

    /**
     * Determine the resulting numeric type of a binary operation on values of
     * the specified types.
     * 
     * @param arg1
     *            the left-hand operand type
     * @param arg2
     *            the right-hand operand type
     * @return the type resulting from a binary operation on values of types
     *         <code>arg1</code> and <code>arg2</code>.
     */
    public final static NumberType numericOpType(Type arg1, Type arg2) {
        return numericOpType(arg1, arg2, false);
    }

    /**
     * Determine the resulting numeric type of a binary operation on values of
     * the specified types.
     * 
     * @param arg1
     *            the left-hand operand type
     * @param arg2
     *            the right-hand operand type
     * @param integral
     *            whether to limit the result to integral types even when
     *            floating-point types are involved.
     * @return the type resulting from a binary operation on values of types
     *         <code>arg1</code> and <code>arg2</code>.
     */
    public static NumberType numericOpType(Type arg1, Type arg2,
            boolean integral) {
        NumberType t1 = numericOpType(arg1, integral);
        if (t1 == null || t1 == NUMBER_TYPE) {
            return t1;
        }

        NumberType t2 = numericOpType(arg2, integral);
        return (t2 == null) ? t1
                : (t2 == NUMBER_TYPE) ? t2
                        : (t1.numberType() >= t2.numberType()) ? t1 : t2;
    }

    /**
     * Return a built-in operator.
     * 
     * @param index
     *            the ID (internal index) of the operator.
     * @return the specified operator, or <code>null</code> if no such operator
     *         exists.
     */
    public static Operator getOperator(int index) {
        return (index < 0 || index >= operators.length) ? null
                : operators[index];
    }

    /**
     * Generate a "library" (a map of names-to-members) for a specified object.
     * If the object is a Map or null, it is returned unaltered. Otherwise, the
     * type (class) of the object will be searched for matching methods,
     * Otherwise, if the object is an instance of {@link java.lang.Class}, no
     * the library is generated from class methods on that class.
     * 
     * @param obj
     *            the base object
     * @param p
     *            the filter pattern for limiting methods (may be null)
     * @return a map of the matching members (methods or constants), indexed by
     *         name. When methods are overloaded on name, the one with the most
     *         parameters is preferred.
     */
    public static Map mkLib(Object obj, Pattern p) {
        if (obj == null) {
            return null;
        }
        else if (obj instanceof Map) {
            return (Map)obj;
        }
        else if (obj instanceof HObject) {
            return ((HObject)obj).getMembers(null);
        }

        // the base class from which the library should be made
        boolean allowInstanceFields;
        Class c;

        if (obj instanceof Class) {
            c = (Class)obj;
            allowInstanceFields = false;
        }
        else {
            c = obj.getClass();
            allowInstanceFields = true;
        }

        // sort methods by their name, preferring the highest parameter count
        Comparator methodComp = new Comparator() {
            @Override
            public int compare(Object obj1, Object obj2) {
                Method m1 = (Method)obj1;
                Method m2 = (Method)obj2;

                int result = java.text.Collator.getInstance().compare(
                        m1.getName(), m2.getName());
                if (result == 0) {
                    // select methods with longer names first
                    int p1 = m1.getParameterTypes().length;
                    int p2 = m2.getParameterTypes().length;
                    return p1 < p2 ? -1 : p1 > p2 ? 1 : 0;
                }
                else {
                    return result;
                }
            }
        };

        // create a new matcher
        Matcher matcher = p == null ? null : p.matcher("");

        // retrieve all declared instance methods
        Set[] data = { null, null, new TreeSet(methodComp) };
        ReflectUtils.listMembers(c, data, 0, matcher,
                0, 0, Modifier.STATIC | Modifier.ABSTRACT);

        // retrieve all declared or inherited static methods
        ReflectUtils.listMembers(c, data, Integer.MAX_VALUE, matcher,
                0, Modifier.STATIC, 0);

        Set tmpSet = data[2];
        data[2] = null;
        data[0] = new TreeSet(ReflectUtils.FIELD_COMPARATOR);

        // retrieve all declared or inherited final fields
        ReflectUtils.listMembers(c, data, Integer.MAX_VALUE, matcher, 0,
                (allowInstanceFields ? 0 : Modifier.STATIC) | Modifier.FINAL,
                0);
        data[2] = tmpSet;

        LinkedHashMap result = new LinkedHashMap();

        // add all the fields, in sorted order
        for (Iterator i = data[0].iterator(); i.hasNext();) {
            Field f = (Field)i.next();
            String name = f.getName();

            if (!result.containsKey(name)) {
                try {
                    result.put(name, f.get(obj));
                }
                catch (Exception e) {
                }
            }
        }

        // add all the methods (preferring the one with most arguments in case
        // of an overloaded operation)
        for (Iterator i = data[2].iterator(); i.hasNext();) {
            Method m = (Method)i.next();
            String name = m.getName();

            if (!result.containsKey(name)) {
                result.put(name, new MethodFunction(m));
            }
        }

        return result;
    }

}
