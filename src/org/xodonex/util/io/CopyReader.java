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
import java.io.Reader;
import java.io.Writer;

/**
 * A <code>CopyReader</code> acts as a normal <code>Reader</code>, but has the
 * side-effect of writing the data being read to a separate writer.
 */
public class CopyReader extends Reader {

    protected Reader in;
    protected Writer out;

    public CopyReader(Reader in, Writer out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public int read() throws IOException {
        int i = in.read();
        if (i >= 0) {
            out.write(i);
        }
        return i;
    }

    @Override
    public int read(char cbuf[]) throws IOException {
        int res = in.read(cbuf, 0, cbuf.length);
        out.write(cbuf, 0, res);
        return res;
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        int res = in.read(cbuf, off, len);
        out.write(cbuf, off, res);
        return res;
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    @Override
    public boolean ready() throws IOException {
        return in.ready();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        in.mark(readAheadLimit);
    }

    @Override
    public void reset() throws IOException {
        in.reset();
        out.flush();
    }

    @Override
    public void close() throws IOException {
        IOException err = null;
        try {
            in.close();
        }
        catch (IOException e) {
            err = e;
        }
        try {
            out.close();
        }
        catch (IOException e) {
            err = e;
        }

        if (err != null) {
            throw err;
        }
    }

}
