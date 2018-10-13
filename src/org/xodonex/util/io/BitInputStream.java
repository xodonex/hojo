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
package org.xodonex.util.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * A BitInputStream reads single bits from an input stream
 *
 * @author Henrik Lauritzen
 */
public class BitInputStream extends InputStream {

    private static int[] MASK_BIT = { 0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20,
            0x40, 0x80 };

    // true iff the stream is closed
    private boolean _closed = false;

    // the underlying input stream
    private InputStream _istream;

    // the current number of buffered bits
    private int _bufSize = 0;

    // the current bit buffer
    private int _buf = 0;

    public BitInputStream(InputStream istream) {
        if (istream == null) {
            throw new NullPointerException();
        }

        _istream = istream;
    }

    public int align() {
        _buf >>= 8 - _bufSize;
        _bufSize = 0;
        return _buf;
    }

    @Override
    public int available() throws IOException {
        synchronized (_istream) {
            return _bufSize + _istream.available() << 3;
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (_istream) {
            if (!_closed) {
                _closed = true;
                _istream.close();
            }
        }
    }

    @Override
    public void mark(int readlimit) {
        // do nothing
    }

    /**
     * @return false
     */
    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        synchronized (_istream) {
            if (_bufSize == 0) {
                if (_closed || (_buf = _istream.read()) < 0) {
                    return _buf;
                }
                _bufSize = 8;
            }

            int result = _buf & MASK_BIT[_bufSize--];
            return result != 0 ? 1 : 0;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        synchronized (_istream) {
            int result = 0;
            for (; len > 0; len--, off++) {
                if ((b[off] = (byte)read()) < 0) {
                    break;
                }
                result++;
            }
            return result;
        }
    }

    @Override
    public void reset() throws IOException {
        throw new IOException();
    }

    @Override
    public long skip(long n) throws IOException {
        return _istream.skip(n);
    }

}
