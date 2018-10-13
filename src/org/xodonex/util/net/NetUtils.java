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
package org.xodonex.util.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.xodonex.util.io.IoUtils;
import org.xodonex.util.thread.CountDownTimer;

/**
 * Utilities for network socket-based communication.
 */
public class NetUtils {

    public final static Object EOF = new Object() {
        @Override
        public String toString() {
            return "EOF";
        }
    };

    public final static Object NOTHING = new Object() {
        @Override
        public String toString() {
            return "NOTHING";
        }
    };

    private final static HashMap timers = new HashMap(8);

    private static long _sendTotal = 0L;
    private static long _receiveTotal = 0L;

    /**
     * Set the timeout to be used in {@link #receive(Socket)} by the current
     * thread. A timeout less than 1 will remove the timeout.
     *
     * @param timeout
     *            the timeout, in milliseconds
     */
    public static void registerTimeout(long timeout) {
        Thread t = Thread.currentThread();

        synchronized (timers) {
            if (timeout <= 0) {
                CountDownTimer cdt = (CountDownTimer)timers.remove(t);
                if (cdt != null) {
                    cdt.kill();
                }
                return;
            }

            CountDownTimer cdt = (CountDownTimer)timers.get(t);
            if (cdt == null) {
                cdt = new CountDownTimer();
                timers.put(t, cdt);
            }
            cdt.setInterval(timeout);
            System.err
                    .println("Setting timeout " + timeout + " for thread " + t);
        }
    }

    public static int send(Object obj, Socket s) throws IOException {
        return send(obj, s, null);
    }

    /**
     * Send the given object as a datagram over a network connection. The data
     * sent will be a 4-byte little-endian integer specifying the size of the
     * datagram, followed by the datagram itself.
     *
     * @param obj
     *            an object which will be serialized into the datagram
     * @param s
     *            the socket on which the datagram should be sent
     * @param dfl
     *            the deflater which should optionally be used to compress data
     * @return the size of the datagram, including the 4-byte header
     * @exception IOException
     *                if an I/O exception occurs
     */
    public static int send(Object obj, Socket s, Deflater dfl)
            throws IOException {
        byte[] data = IoUtils.marshal(obj, dfl);
        OutputStream out = s.getOutputStream();
        synchronized (out) {
            IoUtils.writeInt(out, data.length);
            addSent(4);
            out.write(data);
            addSent(data.length);
            out.flush();
        }
        return data.length + 4;
    }

    public static Object receive(Socket s)
            throws IOException, ClassNotFoundException {
        return receive(s, null);
    }

    /**
     * Receive a datagram from a network connection. The datagram should have
     * the format produced by {@link #send(Object, Socket)}. The operation will
     * be aborted after the {@link #registerTimeout(long) timeout period}, if
     * one is registered.
     *
     * @param s
     *            the socket from which the datagram should be obtained.
     * @param infl
     *            the inflater which should optionally be used to uncompress the
     *            received data
     * @return the deserialized datagram, if no error occurs. If the datagram
     *         could not be read in full, the returned value is {@link #EOF}. If
     *         the operation was aborted due to a timeout, the returned value is
     *         {@link #NOTHING}, and the socked will be closed (this is
     *         necessary because a simple {@link Thread#interrupt()} won't
     *         release a thread which is reading from a socket).
     * @exception IOException
     *                if an I/O exception occurs
     * @exception ClassNotFoundException
     *                if the subsequent deserialization of the datagram fails.
     */
    public static Object receive(final Socket s, Inflater infl)
            throws IOException, ClassNotFoundException {
        InputStream in = s.getInputStream();
        byte[] data;

        CountDownTimer cdt;
        synchronized (timers) {
            cdt = (CountDownTimer)timers.get(Thread.currentThread());
        }

        synchronized (in) {
            try {
                if (cdt != null) {
                    cdt.setAction(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.err.println("Timed out!");
                                s.close();
                            }
                            catch (Throwable t) {
                                t.printStackTrace();
                            }
                        }
                    });
                    cdt.start();
                }
                int size = in.read();

                if (size < 0) {
                    return EOF;
                }
                size = (((size << 8) | IoUtils.readByte(in)) << 16) |
                        IoUtils.readShort(in);

                data = new byte[size];
                int read = IoUtils.readFully(in, data);
                addReceived(4 + read);
                if (read < size) {
                    throw new EOFException("Datagram contained " + read +
                            " instead of " + size + " bytes");
                }
            }
            finally {
                if (cdt != null) {
                    if (cdt.stop()) {
                        return NOTHING;
                    }
                }
            }
        }
        return IoUtils.unmarshal(data, infl);
    }

    public synchronized static long getTotalBytesSent() {
        return _sendTotal;
    }

    public synchronized static long getTotalBytesReceived() {
        return _receiveTotal;
    }

    private NetUtils() {
    }

    private synchronized static void addReceived(int received) {
        _receiveTotal += received;
    }

    private synchronized static void addSent(int sent) {
        _sendTotal += sent;
    }

}
