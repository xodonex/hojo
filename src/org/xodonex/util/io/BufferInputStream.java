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
import java.io.InterruptedIOException;

/**
 * This implements an input stream, which reads from a dynamically expandable
 * buffer, blocking the reading process when the buffer is empty.
 */
public class BufferInputStream extends InputStream {

    // buffer size, head buffer index, tail buffer index
    private int size, hd, tl;

    // the buffer
    private byte[] buf;

    // closed status
    private boolean isClosed = false;

    // synchronization lock
    private Object lock;

    // notification object lock
    private Object notify;

    /**
     * This is equivalent to {@link #BufferInputStream(Object, Object, int)
     * BufferInputStream(null, null, 32)}.
     */
    public BufferInputStream() {
        this(null, null, 32);
    }

    /**
     * This is equivalent to {@link #BufferInputStream(Object, Object, int)
     * BufferInputStream(null, null, intialCapacity)}.
     *
     * @param initialCapacity
     *            the initial buffer size.
     */
    public BufferInputStream(int initialCapacity) {
        this(null, null, initialCapacity);
    }

    /**
     * Constructs a new <code>BufferInputStream</code>.
     *
     * @param lock
     *            object which is used as synchronization lock for all
     *            operations. If the value is <code>null</code>, then
     *            <code>this</code> will be used.
     * @param initialCapacity
     *            the initial buffer size.
     * @param notify
     *            an object which is {@link Object#notifyAll() notified} every
     *            time data is placed in the empty buffer. This may be
     *            <code>null</code>, but <i>may not be equal to
     *            <code>lock</code></i>.
     */
    public BufferInputStream(Object lock, Object notify, int initialCapacity) {
        super();
        buf = new byte[initialCapacity];
        size = hd = tl = 0;
        this.lock = (lock == null ? this : lock);
        if (this.lock == notify) {
            throw new IllegalArgumentException();
        }
        this.notify = notify;
    }

    // ensure a certain buffer capacity
    private void ensureCapacity(int newSize) {
        if (buf.length > newSize) {
            return;
        }

        int nSize = buf.length * 2;
        if (nSize > newSize) {
            newSize = nSize;
        }
        else {
            newSize++;
        }

        byte[] newBuf = new byte[newSize];
        if (hd <= tl) {
            System.arraycopy(buf, hd, newBuf, 0, size);
        }
        else {
            int split = buf.length - hd;
            System.arraycopy(buf, hd, newBuf, 0, split);
            System.arraycopy(buf, 0, newBuf, split, tl);
        }
        buf = newBuf;
        hd = 0;
        tl = size;
    }

    /**
     * @return the synchronization lock.
     */
    public Object getLock() {
        return lock;
    }

    /**
     * Updates the notification lock and returns the old notification lock. The
     * notification lock may not be the same as the synchronization lock.
     *
     * @param notify
     *            the new notification lock
     * @return the old notification lock
     */
    public Object getNotify(Object notify) {
        if (notify == lock) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            Object result = this.notify;
            this.notify = notify;
            return result;
        }
    }

    /**
     * Places the given byte at the end of the buffer.
     *
     * @param b
     *            the input byte
     * @throws IOException
     *             on I/O error
     */
    public void put(byte b) throws IOException {
        boolean nfy;
        Object notify = null;

        synchronized (lock) {
            if (isClosed) {
                // stream is closed
                throw new IOException();
            }

            nfy = size == 0;
            ensureCapacity(size + 1);

            buf[tl] = b;
            tl = (tl + 1) % buf.length;
            size++;

            if (nfy) {
                notify = this.notify;
                lock.notifyAll();
            }
        }

        if (nfy && notify != null) {
            synchronized (notify) {
                notify.notifyAll();
            }
        }
    }

    public void put(byte[] bs) throws IOException {
        put(bs, 0, bs.length);
    }

    public void put(byte[] bs, int ofs, int len) throws IOException {
        boolean nfy;
        Object notify = null;

        if (len <= 0) {
            return;
        }
        else if (ofs < 0 || ofs + len > bs.length) {
            throw new ArrayIndexOutOfBoundsException(
                    "" + (ofs < 0 ? ofs : ofs + len));
        }

        synchronized (lock) {
            if (isClosed) {
                // stream is closed
                throw new IOException();
            }

            nfy = size == 0;
            ensureCapacity(size + len);
            if (tl + len < buf.length) {
                System.arraycopy(bs, ofs, buf, tl, len);
                tl = (tl + len) % buf.length;
            }
            else {
                for (int i = len; i > 0; i--) {
                    buf[tl] = bs[ofs++];
                    tl = (tl + 1) % buf.length;
                }
            }
            size += len;

            if (nfy) {
                notify = this.notify;
                lock.notifyAll();
            }
        }
        if (nfy && notify != null) {
            synchronized (notify) {
                notify.notifyAll();
            }
        }
    }

    public void clear() {
        synchronized (lock) {
            size = hd = tl = 0;
            if (isClosed) {
                lock.notifyAll();
            }
        }
    }

    @Override
    public String toString() {
        synchronized (lock) {
            if (size == 0) {
                return "";
            }
            else if (hd < tl) {
                return new String(buf, hd, size);
            }
            else {
                byte[] bbuf = new byte[size];
                System.arraycopy(buf, hd, bbuf, 0, buf.length - hd);
                System.arraycopy(buf, 0, bbuf, bbuf.length - hd, tl);
                return new String(bbuf);
            }
        }
    }

    @Override
    public int available() {
        synchronized (lock) {
            return size;
        }
    }

    @Override
    public int read() throws IOException {
        synchronized (lock) {
            while (size == 0) {
                if (isClosed) {
                    return -1;
                }
                try {
                    lock.wait();
                }
                catch (InterruptedException e) {
                    throw new InterruptedIOException(e.getMessage());
                }
            }

            byte result = buf[hd];
            hd = (hd + 1) % buf.length;
            size--;
            if (isClosed && size == 0) {
                lock.notifyAll();
            }
            return result;
        }
    }

    @Override
    public int read(byte bbuf[]) throws IOException {
        return read(bbuf, 0, bbuf.length);
    }

    @Override
    public int read(byte bbuf[], int off, int len) throws IOException {
        if (len <= 0) {
            return 0;
        }
        else if (off < 0 || off + len > bbuf.length) {
            throw new ArrayIndexOutOfBoundsException(
                    "" + (off < 0 ? off : off + len));
        }

        synchronized (lock) {
            while (size == 0) {
                if (isClosed) {
                    return -1;
                }
                try {
                    lock.wait();
                }
                catch (InterruptedException e) {
                    throw new InterruptedIOException(e.getMessage());
                }
            }

            int res = size > len ? len : size;

            if (hd <= tl) {
                System.arraycopy(buf, hd, bbuf, off, res);
            }
            else {
                int splitSize = size - tl;
                if (splitSize > res) {
                    splitSize = res;
                }
                else {
                }

                System.arraycopy(buf, hd, bbuf, off, splitSize);
                System.arraycopy(buf, 0, bbuf, off + splitSize,
                        res - splitSize);
            }

            return (int)skip(res);
        }
    }

    @Override
    public long skip(long n) {
        if (n <= 0) {
            return 0;
        }

        synchronized (lock) {
            int res = n > size ? size : (int)n;
            hd = (hd + res) % buf.length;
            size -= res;

            if (isClosed && size == 0) {
                lock.notifyAll();
            }
            return res;
        }
    }

    @Override
    public final boolean markSupported() {
        return false;
    }

    @Override
    public final void mark(int readAheadLimit) {
    }

    @Override
    public final void reset() throws IOException {
        throw new IOException();
    }

    @Override
    public void close() {
        synchronized (lock) {
            isClosed = true;
            if (size == 0) {
                lock.notifyAll();
            }
        }
    }

    public String toDebugString() {
        return toDebugString(true);
    }

    public String toDebugString(boolean useValue) {
        return ("hd= " + hd + ", tl=" + tl + ", size=" + size + ", capacity="
                + buf.length + (useValue ? ", value=" + toString() : ""));
    }

}
