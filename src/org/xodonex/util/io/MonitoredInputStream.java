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
 *
 * @author Henrik Lauritzen
 */
public class MonitoredInputStream extends InputStream {

    private InputStream istream;
    private long inputSize = 0l; // total input size
    private long nextBatch = 0l; // bytes read since last action was invoked
    private long batchSize = Long.MAX_VALUE; // minimum size of each batch
    protected Runnable monitor = null;

    public MonitoredInputStream(InputStream istream) {
        this(istream, null, Long.MAX_VALUE);
    }

    public MonitoredInputStream(InputStream istream, Runnable monitor,
            long batchSize) {
        if (istream == null) {
            throw new NullPointerException();
        }

        this.istream = istream;
        this.monitor = monitor;
        this.batchSize = batchSize < 0l ? 1l : batchSize;
    }

    @Override
    public int available() throws IOException {
        return istream.available();
    }

    @Override
    public void close() throws IOException {
        istream.close();
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
        synchronized (istream) {
            int result = istream.read();
            if (result >= 0) {
                update(1);
            }
            return result;
        }
    }

    @Override
    public int read(byte[] b) throws IOException {
        synchronized (istream) {
            int result = istream.read(b);
            if (result >= 0) {
                update(result);
            }
            return result;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        synchronized (istream) {
            int result = istream.read(b, off, len);
            if (result >= 0) {
                update(result);
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
        return istream.skip(n);
    }

    public void setMonitor(Runnable monitor, long batchSize) {
        synchronized (istream) {
            this.monitor = monitor;
            this.batchSize = batchSize < 0l ? 1l : batchSize;
        }
    }

    public Runnable getMonitor() {
        synchronized (istream) {
            return monitor;
        }
    }

    public long getBatchSize() {
        synchronized (istream) {
            return batchSize;
        }
    }

    public long getInputSize() {
        synchronized (istream) {
            return inputSize;
        }
    }

    private void update(int read) {
        inputSize += read;
        if ((nextBatch += read) >= batchSize) {
            nextBatch = 0l;
            if (monitor != null) {
                monitor.run();
            }
        }
    }
}
