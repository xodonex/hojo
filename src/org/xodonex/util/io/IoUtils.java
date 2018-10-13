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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.xodonex.util.struct.ByteBuffer;

/**
 * Contains methods for input/output of simple data types
 *
 * @author Henrik Lauritzen
 */
public class IoUtils {

    public static byte[] marshal(Object obj) throws IOException {
        return marshal(obj, null);
    }

    public static byte[] marshal(Object obj, Deflater def) throws IOException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo;
        if (def != null) {
            oo = new ObjectOutputStream(new DeflaterOutputStream(bo, def));
        }
        else {
            oo = new ObjectOutputStream(bo);
        }
        oo.writeObject(obj);
        oo.close();
        return bo.toByteArray();
    }

    public static Object unmarshal(byte[] enc)
            throws IOException, ClassNotFoundException {
        return unmarshal(enc, null);
    }

    public static Object unmarshal(byte[] enc, Inflater inf)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bi = new ByteArrayInputStream(enc);
        ObjectInputStream oi;
        if (inf != null) {
            oi = new ObjectInputStream(new InflaterInputStream(bi, inf));
        }
        else {
            oi = new ObjectInputStream(bi);
        }
        return oi.readObject();
    }

    public static int readByte(InputStream in) throws IOException {
        int result = in.read();
        if (result < 0) {
            throw new EOFException();
        }
        return result & 0xff;
    }

    public static int readShort(InputStream in) throws IOException {
        return (readByte(in) << 8) | readByte(in);
    }

    public static int readInt(InputStream in) throws IOException {
        return (readShort(in) << 16) | readShort(in);
    }

    public static long readLong(InputStream in) throws IOException {
        return ((long)readInt(in) << 32) | (readInt(in) & 0xffffffffL);
    }

    public static String readString(InputStream in) throws IOException {
        return readString(in, null);
    }

    public static String readString(InputStream in, String enc)
            throws IOException {
        int size = readInt(in);

        if (size < 0) {
            return null;
        }

        byte[] bs = new byte[size];
        size = readFully(in, bs);
        if (size < bs.length) {
            throw new EOFException();
        }

        if (enc != null) {
            return new String(bs, enc);
        }
        else {
            return new String(bs);
        }
    }

    public static ByteBuffer readFully(InputStream in) throws IOException {
        ByteBuffer result = new ByteBuffer();
        byte[] rbuf = new byte[512];

        int read;

        do {
            read = in.read(rbuf);
            if (read > 0) {
                result.append(rbuf, 0, read);
            }
        } while (read > 0);

        return result;
    }

    public static int readFully(InputStream in, byte[] bs) throws IOException {
        return readFully(in, bs, 0, bs.length);
    }

    public static int readFully(InputStream in, byte[] bs, int off, int len)
            throws IOException {

        int read = 0;
        while (len > 0) {
            int last = in.read(bs, off, len);
            if (last < 0) {
                // EOF
                return read;
            }

            read += last;
            off += last;
            len -= last;
        }

        return read;
    }

    public static void writeByte(OutputStream out, int data)
            throws IOException {
        out.write(data);
    }

    public static void writeShort(OutputStream out, int data)
            throws IOException {
        out.write((data >>> 8) & 0xff);
        out.write(data & 0xff);
    }

    public static void writeInt(OutputStream out, int data) throws IOException {
        out.write((data >>> 24) & 0xff);
        out.write((data >>> 16) & 0xff);
        out.write((data >>> 8) & 0xff);
        out.write(data & 0xff);
    }

    public static void writeLong(OutputStream out, long data)
            throws IOException {
        out.write((int)(data >>> 56));
        out.write((int)(data >>> 48));
        out.write((int)(data >>> 40));
        out.write((int)(data >>> 32));
        out.write((int)(data >>> 24));
        out.write((int)(data >>> 16));
        out.write((int)(data >>> 8));
        out.write((int)data);
    }

    public static int writeString(OutputStream out, String data)
            throws IOException {
        return writeString(out, data, null);
    }

    public static int writeString(OutputStream out, String data, String enc)
            throws IOException {
        if (data == null) {
            writeInt(out, -1);
            return 4;
        }

        byte[] bs = data.getBytes(enc);
        writeInt(out, bs.length);
        out.write(bs);
        return 4 + bs.length;
    }

    private IoUtils() {
    }

}
