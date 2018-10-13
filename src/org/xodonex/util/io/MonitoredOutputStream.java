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
 *
 * @author Henrik Lauritzen
 */
public class MonitoredOutputStream extends OutputStream {

    private OutputStream ostream;
    private long outputSize = 0l; // total input size
    private long nextBatch = 0l; // bytes read since last action was invoked
    private long batchSize = Long.MAX_VALUE; // minimum size of each batch
    protected Runnable monitor = null;

    public MonitoredOutputStream(OutputStream ostream) {
        this(ostream, null, Long.MAX_VALUE);
    }

    public MonitoredOutputStream(OutputStream ostream, Runnable monitor,
            long batchSize) {
        if (ostream == null) {
            throw new NullPointerException();
        }

        this.ostream = ostream;
        this.monitor = monitor;
        this.batchSize = batchSize < 0l ? 1l : batchSize;
    }

    @Override
    public void close() throws IOException {
        ostream.close();
    }

    @Override
    public void flush() throws IOException {
        ostream.flush();
    }

    @Override
    public void write(int b) throws IOException {
        synchronized (ostream) {
            ostream.write(b);
            update(1);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (b.length == 0) {
            return;
        }
        synchronized (ostream) {
            ostream.write(b);
            update(b.length);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        synchronized (ostream) {
            ostream.write(b, off, len);
            update(len);
        }
    }

    public void setMonitor(Runnable monitor, long batchSize) {
        synchronized (ostream) {
            this.monitor = monitor;
            this.batchSize = batchSize < 0l ? 1l : batchSize;
        }
    }

    public Runnable getMonitor() {
        synchronized (ostream) {
            return monitor;
        }
    }

    public long getBatchSize() {
        synchronized (ostream) {
            return batchSize;
        }
    }

    public long getOutputSize() {
        synchronized (ostream) {
            return outputSize;
        }
    }

    private void update(int written) {
        outputSize += written;
        if ((nextBatch += written) >= batchSize) {
            nextBatch = 0l;
            if (monitor != null) {
                monitor.run();
            }
        }
    }
}
