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
package org.xodonex.util.log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.xodonex.util.thread.CommandQueue;

/**
 *
 * @author Henrik Lauritzen
 */
public class DefaultLog implements Log {

    public final static LogListener SYSOUT_LISTENER = new LogListener() {
        @Override
        public void logged(LogEntry e) {
            System.out.println(e);
        }
    };

    public final static LogListener SYSERR_LISTENER = new LogListener() {
        @Override
        public void logged(LogEntry e) {
            System.err.println(e);
        }
    };

    // the encoding used for messages
    private final static String ENCODING = "UTF-16LE";

    // the command used to terminate the dispatcher
    private final static Object[] TERMINATE = new Object[0];

    // the identification used in log entries originating from the log
    private final static String SOURCE = "LOG";

    // the resource bundle used for local messages
    private ResourceBundle _rsrc;

    // a set of the keys contained in the resource file
    // (this is redundant, but prevents exceptions from being thrown and
    // caught every time a localized string is to be returned
    private Set _rsrcKeys = new HashSet();

    // the log entries
    private LinkedList _log = new LinkedList();

    // the destination file
    private String _destination = null;

    // whether the next call to flush() should append to the log file
    private boolean _appendFlush;

    // the maximal log size, if any
    private int _maxSize = -1;

    // the severity threshold
    private int _threshold;

    // the log listeners and their thresholds
    private List _listeners = new ArrayList(2);
    private List _thresholds = new ArrayList(2);

    // the queue of listener events to be dispatched
    private CommandQueue _dispatchQueue = new CommandQueue();

    // whether the log has been closed
    private boolean _closed = false;

    public DefaultLog() {
        _threshold = LogEntry.SEVERITY_MIN;
        addLogListener(SYSOUT_LISTENER, LogEntry.SEVERITY_INFO,
                LogEntry.SEVERITY_ERROR - 1);
        addLogListener(SYSERR_LISTENER, LogEntry.SEVERITY_ERROR,
                LogEntry.SEVERITY_MAX);
        createDispatchThread();
    }

    public DefaultLog(File destination) throws IOException {
        this(destination.getPath());
    }

    public DefaultLog(String destination) throws IOException {
        if ((_destination = destination) == null) {
            throw new NullPointerException();
        }

        _threshold = LogEntry.SEVERITY_MIN;
        reload(Long.MIN_VALUE);
        createDispatchThread();
    }

    public synchronized String getDestination() {
        return _destination;
    }

    public synchronized void setDestination(String destination) {
        _destination = destination;
    }

    public synchronized int getMaxSize() {
        return _maxSize;
    }

    public synchronized void setMaxSize(int max) {
        _maxSize = max;
    }

    public void log(String s) {
        log(new LogEntry(s));
    }

    public void logWarning(String s) {
        log(new LogEntry(s, true));
    }

    public void logError(String s, Throwable t) {
        log(new LogEntry(s, t));
    }

    public synchronized void restart() throws IOException {
        restart(_destination);
    }

    public synchronized void restart(String destination) throws IOException {
        flush();

        if ((_destination = destination) == null) {
            return;
        }
        if (!new File(destination).exists()) {
            flush();
        }
        else {
            reload(Long.MIN_VALUE);
        }
    }

    public synchronized LogEntry getEntry(int index) {
        return (LogEntry)_log.get(index);
    }

    public synchronized LogEntry[] getFirstEntries(int count) {
        return (LogEntry[])_log.subList(0, count).toArray(new LogEntry[count]);
    }

    public synchronized LogEntry[] getLastEntries(int count) {
        int size = _log.size();
        return (LogEntry[])_log.subList(size - count, size).toArray(
                new LogEntry[count]);
    }

    public void setResource(ResourceBundle rsrc) {
        synchronized (_rsrcKeys) {
            if ((_rsrc = rsrc) == null) {
                _rsrcKeys.clear();
            }
            else {
                Enumeration e = _rsrc.getKeys();
                while (e.hasMoreElements()) {
                    _rsrcKeys.add(e.nextElement());
                }
            }
        }
    }

    @Override
    public synchronized int getThreshold() {
        return _threshold;
    }

    @Override
    public synchronized void setThreshold(int threshold) {
        _threshold = threshold;
    }

    @Override
    public void addLogListener(LogListener l, int min, int max) {
        if (l == null) {
            throw new NullPointerException();
        }

        if (min > max) {
            removeLogListener(l);
            return;
        }

        synchronized (_listeners) {
            int idx = _listeners.indexOf(l);
            if (idx >= 0) {
                // change the mask
                _thresholds.set(idx, new int[] { min, max });
            }
            else {
                // add a new listener
                _listeners.add(l);
                _thresholds.add(new int[] { min, max });
            }
        }
    }

    @Override
    public void removeLogListener(LogListener l) {
        synchronized (_listeners) {
            int idx = _listeners.indexOf(l);
            if (idx < 0) {
                return;
            }

            _listeners.remove(idx);
            _thresholds.remove(idx);
        }
    }

    @Override
    public void configureLogListener(LogListener l, int type, int min,
            int max) {
        if (min > max) {
            removeLogListener(l);
            return;
        }

        synchronized (_listeners) {
            int idx = _listeners.indexOf(l);
            if (idx < 0) {
                _listeners.add(l);
                _thresholds.add(new int[] { min, max });
                return;
            }

            int[] cfg = (int[])_thresholds.get(idx);
            if (type == 0) {
                cfg[0] = min;
                cfg[1] = max;
            }
            else if (type > 0) {
                cfg[0] = Math.min(cfg[0], min);
                cfg[1] = Math.max(cfg[1], max);
            }
            else {
                cfg[0] = Math.max(cfg[0], min);
                cfg[1] = Math.min(cfg[1], max);
            }
        }
    }

    @Override
    public synchronized void log(LogEntry e) {
        // log the event
        if (!logSilently(e)) {
            return;
        }

        // notify all listeners
        fireEvent(e);
    }

    @Override
    public synchronized int size() {
        return _log.size();
    }

    public synchronized void clear() {
        _log.clear();
    }

    @Override
    public synchronized boolean flush() {
        if (_closed || _destination == null) {
            return true;
        }

        try {
            OutputStream o = new BufferedOutputStream(
                    new FileOutputStream(_destination, _appendFlush));

            try {
                Iterator it = _log.iterator();
                for (int i = 0, size = _log.size(); i < size; i++) {
                    LogEntry.writeEntry((LogEntry)it.next(), ENCODING, o);
                    it.remove();
                }
                return true;
            }
            catch (Throwable t) {
                return false;
            }
            finally {
                _appendFlush = true;
                o.close();
            }
        }
        catch (IOException e) {
            return false;
        }
    }

    @Override
    public void close() {
        synchronized (this) {
            if (_closed) {
                // already closed
                return;
            }

            // flush the log
            flush();

            // mark the end of the dispatcher queue
            _dispatchQueue.put(TERMINATE);

            // flag the log as closed
            _closed = true;

            // free up some memory
            _log.clear();
        }
        synchronized (_rsrcKeys) {
            _rsrcKeys.clear();
        }
        synchronized (_listeners) {
            _listeners.clear();
            _thresholds.clear();
        }
    }

    @Override
    public synchronized void reload(long since) throws IOException {
        if (_destination == null) {
            return;
        }

        InputStream i = new BufferedInputStream(
                new FileInputStream(_destination));
        try {
            _log.clear();
            boolean check = true;
            while (true) {
                LogEntry e = LogEntry.readEntry(i, ENCODING);
                if (e == null) {
                    // EOF
                    break;
                }
                if (check) {
                    if (e.getTime() >= since) {
                        check = false;
                    }
                    else {
                        continue;
                    }
                }

                _log.addLast(e);
            }
        }
        finally {
            i.close();
        }

        // don't append to the log file on the next flush
        _appendFlush = false;
    }

    @Override
    public synchronized void clear(long before) {
        Iterator i = _log.iterator();

        while (i.hasNext()) {
            if (((LogEntry)i.next()).getTime() < before) {
                i.remove();
            }
            else {
                break;
            }
        }
    }

    @Override
    public synchronized LogEntry[] getEntries() {
        return (LogEntry[])_log.toArray(new LogEntry[_log.size()]);
    }

    @Override
    public synchronized LogEntry[] getEntries(long from, long to) {
        ArrayList entries = new ArrayList();
        Iterator it = _log.iterator();

        for (int i = 0, size = _log.size(); i < size; i++) {
            LogEntry e = (LogEntry)it.next();
            long t = e.getTime();

            if (t < from) {
                continue;
            }
            else if (t > to) {
                break;
            }

            entries.add(e);
        }

        return (LogEntry[])entries.toArray(new LogEntry[entries.size()]);
    }

    @Override
    public String getLocalizedMessage(String key) {
        synchronized (_rsrcKeys) {
            // check whether the key exists in order to avoid that the
            // resource bundle throws exceptions.
            if (_rsrcKeys.contains(key)) {
                return _rsrc.getString(key);
            }
        }

        // return the unmodified key as required
        return key;
    }

    @Override
    public String toString() {
        if (_log.size() == 0) {
            return "[ , ] : 0";
        }

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.MEDIUM);
        return "[" + df.format(new Date(((LogEntry)_log.getFirst()).getTime()))
                +
                ", "
                + df.format(new Date(((LogEntry)_log.getLast()).getTime())) +
                "] : " + _log.size();
    }

    private boolean logSilently(LogEntry e) {
        int severity = e.getSeverity();

        synchronized (this) {
            // ignore the entry ?
            if (severity < _threshold) {
                return false;
            }

            // set the time at which this log received the event
            e.setTimeStamp();

            // add the entry to this log
            _log.add(e);

            // ensure that the log does not exceed the size limit
            int s = _log.size();
            if (_maxSize > 0 && s > _maxSize) {
                // exceeded the size: save the log and clear the cache
                flush();
            }
        }
        return true;
    }

    private void fireEvent(LogEntry e) {
        int severity = e.getSeverity();
        List ls;

        synchronized (_listeners) {
            ls = new ArrayList(_listeners.size() / 2);

            for (int i = 0, size = _listeners.size(); i < size; i++) {
                int[] th = (int[])_thresholds.get(i);

                if (severity < th[0] || severity > th[1]) {
                    continue;
                }
                ls.add(_listeners.get(i));
            }
        }

        _dispatchQueue.put(new Object[] { ls, e });
    }

    private void createDispatchThread() {
        new Thread("DefaultLog@" + Integer.toString(
                System.identityHashCode(this)) + " dispatcher") {
            @Override
            public void run() {
                while (true) {
                    // get the next command
                    Object next;
                    try {
                        next = _dispatchQueue.get();
                    }
                    catch (InterruptedException e) {
                        next = TERMINATE;
                    }

                    if (next == TERMINATE) {
                        // done!
                        break;
                    }

                    // get the data
                    Object[] cmd = (Object[])next;
                    List ls = (List)cmd[0];
                    LogEntry e = (LogEntry)cmd[1];

                    for (int i = 0, max = ls.size(); i < max; i++) {
                        try {
                            ((LogListener)ls.get(i)).logged(e);
                        }
                        catch (Throwable t) {
                            // dispatcher error - create a log entry if the
                            // log was not already closed
                            synchronized (DefaultLog.this) {
                                logSilently(new LogEntry(
                                        SOURCE,
                                        getLocalizedMessage(
                                                "log.error.dispatch"),
                                        t));
                            }
                        }
                    } // for
                } // while
            } // run()
        }.start();
    }

}
