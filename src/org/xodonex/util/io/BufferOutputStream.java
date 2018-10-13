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
import java.io.OutputStream;

/**
 * This implements an output stream, which uses the buffer of a
 * {@link BufferInputStream} as storage, thus creating a pipe.
 *
 * @author Henrik Lauritzen
 */
public class BufferOutputStream extends OutputStream {

    protected BufferInputStream target;

    public BufferOutputStream(BufferInputStream target) {
        if (target == null) {
            throw new NullPointerException();
        }
        this.target = target;
    }

    public BufferInputStream getTarget() {
        return target;
    }

    @Override
    public void close() {
        target.close();
    }

    @Override
    public void flush() {
    }

    @Override
    public void write(byte[] cbuf) throws IOException {
        target.put(cbuf);
    }

    @Override
    public void write(byte[] cbuf, int off, int len) throws IOException {
        target.put(cbuf, off, len);
    }

    @Override
    public void write(int c) throws IOException {
        target.put((byte)c);
    }

    public void write(String str) throws IOException {
        target.put(str.getBytes());
    }

}
