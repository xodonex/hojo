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
package org.xodonex.util.tools;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.xodonex.util.StringUtils;
import org.xodonex.util.struct.ByteBuffer;

/**
 *
 * @author Henrik Lauritzen
 */
public class HexEditor extends ByteBuffer {

    private static final long serialVersionUID = 1L;

    protected int _ofs = 0;
    protected int _dumpRows = 16;
    protected Match _lastMatch = null;

    /*
     * ------------------------------------------------------------------------
     * Constructors
     * -----------------------------------------------------------------------
     */

    public HexEditor() {
        super();
    }

    /*
     * ------------------------------------------------------------------------
     * Public methods, overriden or specified by interface
     * -----------------------------------------------------------------------
     */

    @Override
    public String toString() {
        return createDump(_ofs, _dumpRows, null).toString();
    }

    /*
     * ------------------------------------------------------------------------
     * private methods, functionality
     * -----------------------------------------------------------------------
     */

    /*
     * ------------------------------------------------------------------------
     * public methods, functionality
     * -----------------------------------------------------------------------
     */

    public char show(byte b) {
        char c = (char)(b & 0xff);
        return Character.isISOControl(c) ? '.' : c;
    }

    public StringBuffer createDump(int ofs, int rows, StringBuffer addTo) {
        if (addTo == null) {
            addTo = new StringBuffer(rows * 77); // line size = 76 + '\n'
        }
        char[] cs = new char[16];
        int l, b;

        loop: for (int i = rows; i > 0; i--) {
            if (ofs >= _length) {
                break loop;
            }

            addTo.append(StringUtils.expandLeft(Integer.toHexString(ofs >> 16),
                    '0', 4));
            addTo.append(':');
            addTo.append(StringUtils
                    .expandLeft(Integer.toHexString(ofs & 0xffff), '0', 4));
            addTo.append("  ");

            if ((l = _length - ofs) > 16) {
                l = 16;
            }
            for (int j = 0; j < 16; j++) {
                if (j < l) {
                    cs[j] = show(_buf[ofs]);
                    if (((b = _buf[ofs] & 0xff)) < 0x10) {
                        addTo.append('0');
                    }
                    addTo.append(Integer.toHexString(b));
                }
                else {
                    addTo.append("  ");
                }
                if (j < 15) {
                    addTo.append(j == 7 ? '-' : ' ');
                }
                ofs++;
            }

            addTo.append("  ");
            addTo.append(cs, 0, l);
            addTo.append('\n');
        }

        return addTo;
    }

    /*
     * ------------------------------------------------------------------------
     */

    public int getOffset() {
        return _ofs;
    }

    public int o() {
        return getOffset();
    }

    public void setOffset(int ofs) {
        _ofs = ofs > _length ? _length : ofs;
    }

    public void o(int ofs) {
        setOffset(ofs);
    }

    public int getDumpSize() {
        return _dumpRows;
    }

    public void setDumpSize(int dumpSize) {
        _dumpRows = dumpSize < 0 ? 0 : dumpSize;
    }

    /*
     * ------------------------------------------------------------------------
     */

    public StringBuffer dump(StringBuffer addTo) {
        StringBuffer result = createDump(_ofs, _dumpRows, addTo);
        _ofs += _dumpRows * 16;
        if (_ofs > _length) {
            _ofs = _length;
        }
        return result;
    }

    public StringBuffer d() {
        return dump(null);
    }

    public StringBuffer dump(int ofs, StringBuffer addTo) {
        return createDump(ofs, _dumpRows, addTo);
    }

    public StringBuffer d(int ofs) {
        return createDump(ofs, _dumpRows, null);
    }

    /*
     * ------------------------------------------------------------------------
     */

    public HexEditor e(int ofs, byte b) {
        return (HexEditor)replace(ofs, b);
    }

    public HexEditor e(int ofs, char c) {
        return (HexEditor)replace(ofs, c);
    }

    public HexEditor e(int ofs, short s) {
        return (HexEditor)replace(ofs, s);
    }

    public HexEditor e(int ofs, int i) {
        return (HexEditor)replace(ofs, i);
    }

    public HexEditor e(int ofs, long l) {
        return (HexEditor)replace(ofs, l);
    }

    public HexEditor e(int ofs, float f) {
        return (HexEditor)replace(ofs, f);
    }

    public HexEditor e(int ofs, double d) {
        return (HexEditor)replace(ofs, d);
    }

    public HexEditor e(int ofs, byte[] values) {
        return (HexEditor)replace(ofs, values);
    }

    public HexEditor e(int ofs, String s) throws UnsupportedEncodingException {
        return (HexEditor)replace(ofs, s);
    }

    /*
     * ------------------------------------------------------------------------
     */

    public HexEditor i(int ofs, byte b) {
        return (HexEditor)insert(ofs, b);
    }

    public HexEditor i(int ofs, char c) {
        return (HexEditor)insert(ofs, c);
    }

    public HexEditor i(int ofs, short s) {
        return (HexEditor)insert(ofs, s);
    }

    public HexEditor i(int ofs, int i) {
        return (HexEditor)insert(ofs, i);
    }

    public HexEditor i(int ofs, long l) {
        return (HexEditor)insert(ofs, l);
    }

    public HexEditor i(int ofs, float f) {
        return (HexEditor)insert(ofs, f);
    }

    public HexEditor i(int ofs, double d) {
        return (HexEditor)insert(ofs, d);
    }

    public HexEditor i(int ofs, byte[] values) {
        return (HexEditor)insert(ofs, values);
    }

    public HexEditor i(int ofs, String s) throws UnsupportedEncodingException {
        return (HexEditor)insert(ofs, s);
    }

    /*
     * ------------------------------------------------------------------------
     */

    public int find(int start, int end, String pattern) {
        return find(start, end, encode(pattern));
    }

    public int find(int start, int end, byte[] pattern) {
        if (pattern.length == 0) {
            return -1;
        }
        _lastMatch = new Match(start, end, pattern);
        return _lastMatch.find();
    }

    public int find() {
        return _lastMatch == null ? -1 : _lastMatch.find();
    }

    public int f(String pattern) {
        return find(_ofs, _length, encode(pattern));
    }

    public int f(byte[] pattern) {
        return find(_ofs, _length, pattern);
    }

    public int f(byte pattern) {
        return find(_ofs, _length, new byte[] { pattern });
    }

    public int f(int start, int end, String pattern) {
        return find(start, end, encode(pattern));
    }

    public int f(int start, int end, byte[] pattern) {
        return find(start, end, pattern);
    }

    public int f(int start, int end, byte pattern) {
        return find(start, end, new byte[] { pattern });
    }

    public int f() {
        return find();
    }

    public int[] findAll(int start, int end, String pattern) {
        return findAll(start, end, encode(pattern));
    }

    public int[] findAll(int start, int end, byte[] pattern) {
        ArrayList l = new ArrayList();
        Match m = new Match(start, end, pattern);
        int i;
        while ((i = m.find()) >= 0) {
            l.add(new Integer(i));
        }

        int[] result = new int[l.size()];
        for (i = 0; i < result.length; i++) {
            result[i] = ((Integer)l.get(i)).intValue();
        }
        return result;
    }

    public int[] F(int start, int end, String pattern) {
        return findAll(start, end, encode(pattern));
    }

    public int[] F(int start, int end, byte[] pattern) {
        return findAll(start, end, pattern);
    }

    public int[] F(int start, int end, byte pattern) {
        return findAll(start, end, new byte[] { pattern });
    }

    public int[] F(String pattern) {
        return findAll(_ofs, _length, encode(pattern));
    }

    public int[] F(byte[] pattern) {
        return findAll(_ofs, _length, pattern);
    }

    public int[] F(byte pattern) {
        return findAll(_ofs, _length, new byte[] { pattern });
    }
}
