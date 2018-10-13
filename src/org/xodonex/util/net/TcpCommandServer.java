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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xodonex.util.StringUtils;
import org.xodonex.util.log.Log;
import org.xodonex.util.log.LogEntry;
import org.xodonex.util.thread.StoppableThread;

/**
 * Base implementation of a TCP-communicating server process.
 */
public abstract class TcpCommandServer {

    public final static String SOURCE = "TCP";

    // the server socket
    private ServerSocket _socket;

    // a thread which listens for new connections
    private StoppableThread _ctrl;

    // the currently active service threads and their sockets
    private Map _clients;

    // the log (may be null)
    private Log _log;

    // ID counter
    private int _id = 0;

    public TcpCommandServer(ServerSocket socket, Log log) {
        if ((_socket = socket) == null) {
            throw new NullPointerException();
        }

        _log = log;
        try {
            _socket.setSoTimeout(0); // disable timeouts in accept()
        }
        catch (SocketException e) {
            log(new LogEntry(SOURCE,
                    _log.getLocalizedMessage("tcp.error.setTimeout"), e));
        }

        _clients = new HashMap(16);

        _ctrl = new StoppableThread() {
            @Override
            public void run() {
                log(new LogEntry(SOURCE,
                        _log.getLocalizedMessage("tcp.info.serverStarted"),
                        (String)null));

                while (true) {
                    try {
                        if (isKilled()) {
                            break;
                        }

                        Socket s = _socket.accept();
                        Thread t = null;

                        try {
                            if ((t = createConnection(_id, s)) == null) {
                                s.close();
                                log(new LogEntry(SOURCE,
                                        LogEntry.SEVERITY_WARNING,
                                        _log.getLocalizedMessage(
                                                "tcp.warning.refusedConnection"),
                                        showAddr(s)));
                            }
                            else {
                                // add the thread to the client threads
                                synchronized (_clients) {
                                    _clients.put(t, s);
                                }

                                log(new LogEntry(SOURCE,
                                        _log.getLocalizedMessage(
                                                "tcp.info.acceptedConnection"),
                                        showAddr(s) + " = [" + _id++ + "]"));
                            }
                        }
                        catch (Throwable thr) {
                            s.close();
                            log(new LogEntry(SOURCE, MessageFormat.format(
                                    _log.getLocalizedMessage(
                                            "tcp.error.general"),
                                    new Object[] { showAddr(s) }), thr));
                        }
                    }
                    catch (InterruptedIOException e) {
                        // stop listening
                        break;
                    }
                    catch (IOException e) {
                        // ignore
                    }
                } // while

                log(new LogEntry(SOURCE,
                        _log.getLocalizedMessage("tcp.info.serverTerminated"),
                        (String)null));
            }

            @Override
            public boolean kill() {
                if (super.kill()) {
                    // terminate all the client threads, then close the
                    // server socket
                    synchronized (_clients) {
                        for (Iterator i = _clients.entrySet().iterator(); i
                                .hasNext();) {
                            Map.Entry e = (Map.Entry)i.next();
                            try {
                                ((StoppableThread)e.getKey()).kill();
                                ((Socket)e.getValue()).close();
                            }
                            catch (IOException _e) {
                                // ignore
                            }
                        }
                        _clients.clear();
                    }

                    try {
                        _socket.close();
                    }
                    catch (IOException e) {
                        // ignore
                    }

                    return true;
                }
                else {
                    return false;
                }
            }

        };
    }

    /**
     * This method should be invoked from a client thread once its connection
     * has been terminated.
     *
     * @param t
     *            the client thread which is closing its connection
     */
    public void connectionClosed(StoppableThread t) {
        synchronized (_clients) {
            _clients.remove(t);
        }
    }

    /**
     * Starts the command server. This can only be done once.
     */
    public void start() {
        try {
            _ctrl.start();
        }
        catch (Exception e) {
            // do nothing
        }
    }

    /**
     * Stops this command server, closing all active connections
     */
    public void stop() {
        _ctrl.kill();
    }

    /**
     * @return the log used for this command server
     */
    public Log getLog() {
        return _log;
    }

    /**
     * Log an entry to this server's log.
     *
     * @param e
     *            the log entry
     */
    public void log(LogEntry e) {
        if (_log == null) {
            return;
        }

        _log.log(e);
    }

    /**
     * Creates and returns a thread which is responsible for maintaining the
     * connection. If the return value is null, this indicates that the
     * connection should be refused.
     *
     * @param id
     *            an ID for the connection
     * @param socket
     *            the socket for the connection
     * @return a thread handling the new connection
     */
    protected abstract StoppableThread createConnection(int id, Socket socket);

    /**
     * @return The server socket
     */
    protected ServerSocket getSocket() {
        return _socket;
    }

    private static String showAddr(Socket s) {
        return s.getInetAddress() + ":" +
                StringUtils.expandLeft(
                        Integer.toHexString(s.getPort()), '0', 4);
    }

}
