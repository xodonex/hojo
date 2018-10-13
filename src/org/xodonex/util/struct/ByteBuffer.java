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
package org.xodonex.util.struct;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.xodonex.util.ArrayUtils;

/**
 *
 * @author Henrik Lauritzen
 */
public class ByteBuffer implements Cloneable, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    protected byte[] _buf;
    protected int _length;
    protected boolean _bigEndian;
    protected String _encoding;

    /** Creates new ByteBuffer */
    public ByteBuffer() {
        this(16, true, null);
    }

    public ByteBuffer(int initialCapacity) {
        this(initialCapacity, true, null);
    }

    public ByteBuffer(int initialCapacity, boolean bigEndian) {
        this(initialCapacity, bigEndian, null);
    }

    public ByteBuffer(int initialCapacity, boolean bigEndian, String encoding) {
        _length = 0;
        _buf = new byte[initialCapacity];
        _bigEndian = bigEndian;
        _encoding = encoding;
    }

    protected class Match {
        byte[] P; // the search pattern
        int[] pi; // prefix function
        int start, end; // search start and end offset

        public Match(int start, int end, byte[] P) {
            this.P = P;
            this.start = (start > _length) ? _length : start;
            this.end = (end <= start || end > _length) ? _length : end;
            if (this.start < this.end) {
                computePrefixFunction();
            }
        }

        // Compute-Prefix-Function, CLR p. 871
        private void computePrefixFunction() {
            pi = new int[P.length];
            int k = 0;

            for (int q = 2; q <= P.length; q++) {
                while (k > 0 && P[k] != P[q - 1]) {
                    k = pi[k - 1];
                }
                if (P[k] == P[q - 1]) {
                    k++;
                }
                pi[q - 1] = k;
            }
        }

        // ~ KMP-Matcher, CLR p. 871
        // maintain start, return -1 if not found (start >= end).
        public int find() {
            int q = 0;
            while (start < end) {
                while (q > 0 && P[q] != _buf[start]) {
                    q = pi[q - 1];
                }
                if (P[q] == _buf[start]) {
                    q++;
                }

                if (q == P.length) {
                    return start++ - P.length + 1;
                }

                start++;
            }
            // not found!
            return -1;
        }
    }

    public static byte[] encode(char c, boolean bigEndian) {
        return encode((short)c, bigEndian);
    }

    public static byte[] encode(short s, boolean bigEndian) {
        byte[] result = new byte[2];
        encode(s, result, 0, bigEndian);
        return result;
    }

    public static byte[] encode(int i, boolean bigEndian) {
        byte[] result = new byte[4];
        encode(i, result, 0, bigEndian);
        return result;
    }

    public static byte[] encode(long l, boolean bigEndian) {
        byte[] result = new byte[8];
        encode(l, result, 0, bigEndian);
        return result;
    }

    public static byte[] encode(float f, boolean bigEndian) {
        return encode(Float.floatToIntBits(f), bigEndian);
    }

    public static byte[] encode(double d, boolean bigEndian) {
        return encode(Double.doubleToLongBits(d), bigEndian);
    }

    public static byte[] encode(String s, String encoding) {
        if (encoding != null) {
            try {
                return s.getBytes(encoding);
            }
            catch (UnsupportedEncodingException e) {
            }
        }
        return s.getBytes();
    }

    public static void encode(char c, byte[] buf, int ofs, boolean bigEndian) {
        encode((short)c, buf, ofs, bigEndian);
    }

    public static void encode(short s, byte[] buf, int ofs, boolean bigEndian) {
        if (bigEndian) {
            buf[ofs++] = (byte)(s & 0xff);
            buf[ofs] = (byte)(s >>> 8);
        }
        else {
            buf[ofs++] = (byte)(s >>> 8);
            buf[ofs] = (byte)(s & 0xff);
        }
    }

    public static void encode(int i, byte[] buf, int ofs, boolean bigEndian) {
        if (bigEndian) {
            for (int j = 3; j >= 0; j--) {
                buf[ofs++] = (byte)(i & 0xff);
                i >>>= 8;
            }
        }
        else {
            ofs += 3;
            for (int j = 3; j >= 0; j--) {
                buf[ofs--] = (byte)(i & 0xff);
                i >>>= 8;
            }
        }
    }

    public static void encode(long l, byte[] buf, int ofs, boolean bigEndian) {
        if (bigEndian) {
            for (int j = 7; j >= 0; j--) {
                buf[ofs++] = (byte)(l & 0xff);
                l >>>= 8;
            }
        }
        else {
            ofs += 7;
            for (int j = 7; j >= 0; j--) {
                buf[ofs--] = (byte)(l & 0xff);
                l >>>= 8;
            }
        }
    }

    public static void encode(float f, byte[] buf, int ofs, boolean bigEndian) {
        encode(Float.floatToIntBits(f), buf, ofs, bigEndian);
    }

    public static void encode(double d, byte[] buf, int ofs,
            boolean bigEndian) {
        encode(Double.doubleToLongBits(d), buf, ofs, bigEndian);
    }

    public static char decodeChar(byte[] buf, int ofs, boolean bigEndian) {
        return (char)decodeShort(buf, ofs, bigEndian);
    }

    public static short decodeShort(byte[] buf, int ofs, boolean bigEndian) {
        return (bigEndian) ? (short)((buf[ofs + 1] << 8) | buf[ofs])
                : (short)((buf[ofs] << 8) | buf[ofs + 1]);
    }

    public static int decodeInt(byte[] buf, int ofs, boolean bigEndian) {
        int result = 0;
        if (bigEndian) {
            ofs += 3;
            for (int i = 2; i >= 0; i--) {
                result |= (buf[ofs--] & 0xff);
                result <<= 8;
            }
        }
        else {
            for (int i = 2; i >= 0; i--) {
                result |= (buf[ofs++] & 0xff);
                result <<= 8;
            }
        }
        return result | (buf[ofs] & 0xff);
    }

    public static long decodeLong(byte[] buf, int ofs, boolean bigEndian) {
        long result = 0;
        if (bigEndian) {
            ofs += 7;
            for (int i = 6; i >= 0; i--) {
                result |= (buf[ofs--] & 0xff);
                result <<= 8;
            }
        }
        else {
            for (int i = 6; i >= 0; i--) {
                result |= (buf[ofs++] & 0xff);
                result <<= 8;
            }
        }
        return result | (buf[ofs] & 0xff);
    }

    public static float decodeFloat(byte[] buf, int ofs, boolean bigEndian) {
        return Float.intBitsToFloat(decodeInt(buf, ofs, bigEndian));
    }

    public static double decodeDouble(byte[] buf, int ofs, boolean bigEndian) {
        return Double.doubleToLongBits(decodeLong(buf, ofs, bigEndian));
    }

    @Override
    public boolean equals(final java.lang.Object obj) {
        if (!(obj instanceof ByteBuffer)) {
            return false;
        }
        ByteBuffer b = (ByteBuffer)obj;
        if (_length != b._length || _bigEndian != b._bigEndian ||
                (_encoding == null ? b._encoding != null
                        : !_encoding.equals(b._encoding))) {
            return false;
        }
        for (int i = 0; i < _length; i++) {
            if (_buf[i] != b._buf[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode() ^ ArrayUtils.hashCode(_buf, 0, _length);
    }

    @Override
    public String toString() {
        return new String(_buf, 0, _length);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    // make room for an insertion/replacement at index idx of size sz.
    private void checkInsert(int idx, int sz, boolean replace) {
        if (idx < 0 || idx > _length) {
            throw new IndexOutOfBoundsException("" + idx);
        }
        if (replace) {
            if ((sz = idx + sz) > _length) {
                ensureCapacity(sz);
                _length = sz;
            }
        }
        else {
            ensureCapacity(_length + sz);
            if (idx < _length) {
                System.arraycopy(_buf, idx, _buf, idx + sz, _length - idx);
            }
            _length += sz;
        }
    }

    private ByteBuffer insRepl(byte b, int index, boolean replace) {
        checkInsert(index, 1, replace);
        _buf[index] = b;
        return this;
    }

    private ByteBuffer insRepl(short s, int index, boolean replace) {
        checkInsert(index, 2, replace);
        encode(s, _buf, index, _bigEndian);
        return this;
    }

    private ByteBuffer insRepl(int i, int index, boolean replace) {
        checkInsert(index, 4, replace);
        encode(i, _buf, index, _bigEndian);
        return this;
    }

    private ByteBuffer insRepl(long l, int index, boolean replace) {
        checkInsert(index, 8, replace);
        encode(l, _buf, index, _bigEndian);
        return this;
    }

    private ByteBuffer insRepl(byte[] bs, int index, boolean replace) {
        return insRepl(bs, 0, bs.length, index, replace);
    }

    private ByteBuffer insRepl(byte[] bs, int ofs, int len, int index,
            boolean replace) {
        if (ofs < 0 || len < 0 || ofs + len > bs.length) {
            throw new IndexOutOfBoundsException();
        }
        checkInsert(index, len, replace);
        System.arraycopy(bs, ofs, _buf, index, len);
        return this;
    }

    public boolean isBigEndian() {
        return _bigEndian;
    }

    public void setBigEndian(boolean bigEndian) {
        _bigEndian = bigEndian;
    }

    public String getEncoding() {
        return _encoding;
    }

    public void setEncoding(String encoding) {
        _encoding = encoding;
    }

    public void ensureCapacity(int capacity) {
        int newSize = _buf.length;
        while (newSize < capacity) {
            newSize <<= 1;
        }
        if (newSize <= _buf.length) {
            return;
        }
        _buf = ArrayUtils.enlarge(_buf, newSize - _buf.length);
    }

    public int length() {
        return _length;
    }

    public void setLength(int length) {
        if (length < 0) {
            throw new IndexOutOfBoundsException("" + length);
        }
        if (length <= _length) {
            _length = length;
        }
        else {
            ensureCapacity(length);
            ArrayUtils.fill(_buf, _length, length, (byte)0);
            _length = length;
        }
    }

    public ByteBuffer reverse() {
        return reverse(0, _length);
    }

    public ByteBuffer reverse(int start, int end) {
        ArrayUtils.reverseRange(_buf, start, end);
        return this;
    }

    public ByteBuffer delete(int start, int end) {
        if (start < 0 || start > end || end > _length) {
            throw new IndexOutOfBoundsException();
        }
        if (start != end) {
            if (end != _length) {
                System.arraycopy(_buf, end, _buf, start, _length - end);
            }
            _length -= (end - start);
        }
        return this;
    }

    public byte getByte(int index) {
        if (index >= _length) {
            throw new IndexOutOfBoundsException("" + index);
        }
        return _buf[index];
    }

    public short getShort(int index) {
        if (index + 1 >= _length) {
            throw new IndexOutOfBoundsException("" + index);
        }
        return decodeShort(_buf, index, _bigEndian);
    }

    public char getChar(int index) {
        return (char)getShort(index);
    }

    public int getInt(int index) {
        if (index + 3 >= _length) {
            throw new IndexOutOfBoundsException("" + index);
        }
        return decodeInt(_buf, index, _bigEndian);
    }

    public float getFloat(int index) {
        return Float.intBitsToFloat(getInt(index));
    }

    public long getLong(int index) {
        if (index + 7 >= _length) {
            throw new IndexOutOfBoundsException("" + index);
        }
        return decodeLong(_buf, index, _bigEndian);
    }

    public double getDouble(int index) {
        return Double.longBitsToDouble(getLong(index));
    }

    public byte[] getBytes() {
        return getBytes(null, 0, 0, _length);
    }

    public byte[] getBytes(byte[] addTo, int fromOfs, int toOfs, int len) {
        if (len > _length) {
            len = _length;
        }
        if (addTo == null) {
            addTo = new byte[len];
        }
        System.arraycopy(_buf, fromOfs, addTo, toOfs, len);
        return addTo;
    }

    public void writeBytes(OutputStream out) throws IOException {
        writeBytes(out, 0, _length);
    }

    public void writeBytes(OutputStream out, int ofs, int len)
            throws IOException {
        if (ofs < 0) {
            throw new IndexOutOfBoundsException("" + ofs);
        }
        else if (ofs + len > _length) {
            throw new IndexOutOfBoundsException("" + (ofs + len));
        }

        out.write(_buf, ofs, len);
    }

    public String getString(int ofs, int len)
            throws UnsupportedEncodingException {
        if (_encoding == null) {
            return new String(_buf, ofs, len);
        }
        else {
            return new String(_buf, ofs, len, _encoding);
        }
    }

    public ByteBuffer append(byte b) {
        return insRepl(b, _length, false);
    }

    public ByteBuffer append(char c) {
        return insRepl((short)c, _length, false);
    }

    public ByteBuffer append(short s) {
        return insRepl(s, _length, false);
    }

    public ByteBuffer append(int i) {
        return insRepl(i, _length, false);
    }

    public ByteBuffer append(long l) {
        return insRepl(l, _length, false);
    }

    public ByteBuffer append(float f) {
        return insRepl(Float.floatToIntBits(f), _length, false);
    }

    public ByteBuffer append(double d) {
        return insRepl(Double.doubleToLongBits(d), _length, false);
    }

    public ByteBuffer append(byte[] bs) {
        return insRepl(bs, _length, false);
    }

    public ByteBuffer append(byte[] bs, int ofs, int len) {
        return insRepl(bs, ofs, len, _length, false);
    }

    public ByteBuffer append(String s) throws UnsupportedEncodingException {
        return insRepl(s.getBytes(_encoding), _length, false);
    }

    public ByteBuffer insert(int index, byte b) {
        return insRepl(b, index, false);
    }

    public ByteBuffer insert(int index, char c) {
        return insRepl((short)c, index, false);
    }

    public ByteBuffer insert(int index, short s) {
        return insRepl(s, index, false);
    }

    public ByteBuffer insert(int index, int i) {
        return insRepl(i, index, false);
    }

    public ByteBuffer insert(int index, long l) {
        return insRepl(l, index, false);
    }

    public ByteBuffer insert(int index, float f) {
        return insRepl(Float.floatToIntBits(f), index, false);
    }

    public ByteBuffer insert(int index, double d) {
        return insRepl(Double.doubleToLongBits(d), index, false);
    }

    public ByteBuffer insert(int index, byte[] bs) {
        return insRepl(bs, index, false);
    }

    public ByteBuffer insert(int index, byte[] bs, int ofs, int len) {
        return insRepl(bs, ofs, len, index, false);
    }

    public ByteBuffer insert(int index, String s)
            throws UnsupportedEncodingException {
        return insRepl(s.getBytes(_encoding), index, false);
    }

    public ByteBuffer replace(int index, byte b) {
        return insRepl(b, index, true);
    }

    public ByteBuffer replace(int index, char c) {
        return insRepl((short)c, index, true);
    }

    public ByteBuffer replace(int index, short s) {
        return insRepl(s, index, true);
    }

    public ByteBuffer replace(int index, int i) {
        return insRepl(i, index, true);
    }

    public ByteBuffer replace(int index, long l) {
        return insRepl(l, index, true);
    }

    public ByteBuffer replace(int index, float f) {
        return insRepl(Float.floatToIntBits(f), index, true);
    }

    public ByteBuffer replace(int index, double d) {
        return insRepl(Double.doubleToLongBits(d), index, true);
    }

    public ByteBuffer replace(int index, byte[] bs) {
        return insRepl(bs, index, true);
    }

    public ByteBuffer replace(int index, byte[] bs, int ofs, int len) {
        return insRepl(bs, ofs, len, index, true);
    }

    public ByteBuffer replace(int index, String s)
            throws UnsupportedEncodingException {
        return insRepl(s.getBytes(_encoding), index, true);
    }

    public byte[] encode(char c) {
        return encode((short)c, _bigEndian);
    }

    public byte[] encode(short s) {
        return encode(s, _bigEndian);
    }

    public byte[] encode(int i) {
        return encode(i, _bigEndian);
    }

    public byte[] encode(long l) {
        return encode(l, _bigEndian);
    }

    public byte[] encode(float f) {
        return encode(f, _bigEndian);
    }

    public byte[] encode(double d) {
        return encode(d, _bigEndian);
    }

    public byte[] encode(String s) {
        return encode(s, _encoding);
    }

    public int indexOf(byte b) {
        return indexOf(b, 0);
    }

    public int indexOf(char c) {
        return indexOf(c, 0);
    }

    public int indexOf(short s) {
        return indexOf(s, 0);
    }

    public int indexOf(int i) {
        return indexOf(i, 0);
    }

    public int indexOf(long l) {
        return indexOf(l, 0);
    }

    public int indexOf(float f) {
        return indexOf(f, 0);
    }

    public int indexOf(double d) {
        return indexOf(d, 0);
    }

    public int indexOf(byte[] bs) {
        return indexOf(bs, 0);
    }

    public int indexOf(String s) {
        return indexOf(s, 0);
    }

    public int indexOf(byte b, int index) {
        for (int i = index; i < _length; i++) {
            if (_buf[index] == b) {
                return index;
            }
        }
        return -1;
    }

    public int indexOf(char c, int index) {
        return indexOf(encode(c), index);
    }

    public int indexOf(short s, int index) {
        return indexOf(encode(s), index);
    }

    public int indexOf(int i, int index) {
        return indexOf(encode(i), index);
    }

    public int indexOf(long l, int index) {
        return indexOf(encode(l), index);
    }

    public int indexOf(float f, int index) {
        return indexOf(encode(f), index);
    }

    public int indexOf(double d, int index) {
        return indexOf(encode(d), index);
    }

    public int indexOf(byte[] bs, int index) {
        switch (bs.length) {
        case 0:
            return -1;
        case 1:
            return indexOf(bs[0], index);
        default:
            return new Match(index, _length, bs).find();
        }
    }

    public int indexOf(String s, int index) {
        return indexOf(encode(s), index);
    }
}
