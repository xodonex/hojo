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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import org.xodonex.util.StringUtils;
import org.xodonex.util.io.IoUtils;

/**
 * A single entry in a log.
 */
public class LogEntry implements Comparable, Serializable {

    private static final long serialVersionUID = 1L;

    public final static int SEVERITY_MIN = Integer.MIN_VALUE;
    public final static int SEVERITY_DEBUG = (int)(Integer.MIN_VALUE * 0.75);
    public final static int SEVERITY_INFO = Integer.MIN_VALUE / 2;
    public final static int SEVERITY_WARNING = 0;
    public final static int SEVERITY_ERROR = Integer.MAX_VALUE / 2;
    public final static int SEVERITY_MAX = Integer.MAX_VALUE;

    // the ID for the next entry
    private static int ID = 0;

    /**
     * Write a LogEntry to an output stream.
     *
     * @param e
     *            the entry to be written
     * @param encoding
     *            the encoding to be used
     * @param out
     *            the target output stream
     * @throws IOException
     *             on I/O error
     */
    public static void writeEntry(LogEntry e, String encoding, OutputStream out)
            throws IOException {
        IoUtils.writeLong(out, e._time);
        IoUtils.writeInt(out, e._id);
        IoUtils.writeInt(out, e._severity);
        IoUtils.writeString(out, e._source, encoding);
        IoUtils.writeString(out, e._message, encoding);
        IoUtils.writeString(out, e._details, encoding);
    }

    /**
     * Read a LogEntry from an input stream.
     *
     * @param in
     *            the source input stream
     * @param encoding
     *            the encoding to be used
     * @return the log entry read from the stream
     * @throws IOException
     *             on I/O error
     */
    public static LogEntry readEntry(InputStream in, String encoding)
            throws IOException {
        long time;

        try {
            time = IoUtils.readLong(in);
        }
        catch (EOFException e) {
            // no more items in the input stream
            return null;
        }

        int id = IoUtils.readInt(in);
        int severity = IoUtils.readInt(in);
        String source = IoUtils.readString(in, encoding);
        String message = IoUtils.readString(in, encoding);
        String details = IoUtils.readString(in, encoding);

        return new LogEntry(time, id, source, severity, message, details);
    }

    /**
     * Create a dummy (empty) log entry which is stamped at the given time.
     *
     * @param time
     *            the time-stamp
     * @return the created log entry
     */
    public static LogEntry createDummyEntry(long time) {
        LogEntry e = new LogEntry("");
        e._time = time;
        return e;
    }

    // the severity of the entry
    private int _severity;

    // an ID for the entry's source
    private String _source;

    // the message
    private String _message;

    // the message details, if any
    private String _details = null;

    // the time stamp for the entry
    private long _time;

    // identification number to distinguish entries having the same time stamp
    private int _id;

    public LogEntry(String message) {
        this(null, SEVERITY_INFO, message, null);
    }

    public LogEntry(String message, String details) {
        this(null, SEVERITY_INFO, message, details);
    }

    public LogEntry(String source, String message, String details) {
        this(source, SEVERITY_INFO, message, details);
    }

    public LogEntry(String message, boolean warning) {
        this(null, message, warning);
    }

    public LogEntry(String source, String message, boolean warning) {
        this(source, warning ? SEVERITY_WARNING : SEVERITY_INFO, message, null);
    }

    public LogEntry(String message, Throwable error) {
        this(null, message, error);
    }

    public LogEntry(String source, String message, Throwable error) {
        this(source, SEVERITY_ERROR, message,
                error == null ? null : StringUtils.createTrace(error));
    }

    public LogEntry(int severity, String message, String details) {
        this(null, severity, message, details);
    }

    public LogEntry(String source, int severity, String message,
            String details) {
        _time = 0L; // System.currentTimeMillis();
        if ((_message = message) == null) {
            throw new NullPointerException();
        }
        _source = source;
        _severity = severity;
        _details = details;
    }

    private LogEntry(long time, int id, String source, int severity,
            String msg, String details) {
        _time = time;
        _id = id;
        _source = source;
        _severity = severity;
        _message = msg;
        _details = details;
    }

    public String getSource() {
        return _source;
    }

    public String getMessage() {
        return _message;
    }

    public String getDetails() {
        return _details;
    }

    public String getFullMessage() {
        if (_details == null) {
            return _message;
        }
        else {
            return _message + '\n' + _details;
        }
    }

    public long getTime() {
        return _time;
    }

    public int getSeverity() {
        return _severity;
    }

    public boolean isError() {
        return _severity >= SEVERITY_ERROR;
    }

    public boolean isWarning() {
        return _severity >= SEVERITY_WARNING && _severity < SEVERITY_ERROR;
    }

    /**
     * This should only be called from within {@link Log#log(LogEntry)}.
     */
    void setTimeStamp() {
        _time = System.currentTimeMillis();
        _id = getNextId();
    }

    @Override
    public int compareTo(Object obj) {
        LogEntry e = ((LogEntry)obj);
        return _time < e._time ? -1
                : _time > e._time ? 1 : _id < e._id ? -1 : _id > e._id ? 1 : 0;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
                DateFormat.MEDIUM);

        buf.append(df.format(new Date(getTime())));
        buf.append('.')
                .append(StringUtils.expandLeft("" + _time % 1000, '0', 3));

        int s = getSeverity();
        if (s >= SEVERITY_ERROR) {
            buf.append(" !!! ");
        }
        else if (s >= SEVERITY_WARNING) {
            buf.append(" (!) ");
        }
        else if (s < SEVERITY_INFO) {
            buf.append(" ... ");
        }
        else {
            buf.append("     ");
        }

        String src = getSource();
        if (src != null) {
            buf.append('(').append(src).append(")  ");
        }

        buf.append(getMessage());

        String details = getDetails();
        if (details != null) {
            buf.append('\n').append(details);
        }

        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LogEntry)) {
            return false;
        }

        LogEntry e = (LogEntry)obj;
        return (e._severity == _severity && e._time == _time && e._id == _id &&
                (_source == null ? e._source == null
                        : _source.equals(e._source))
                &&
                _message.equals(e._message) &&
                (_details == null ? e._details == null
                        : _details.equals(e._details)));
    }

    @Override
    public int hashCode() {
        int hc = ((int)(_time >>> 32)) ^ ((int)_time);

        hc = hc * _id;
        hc = hc * 31 + _severity;
        hc = hc * 31 + (_source == null ? 0 : _source.hashCode());
        hc = hc * 31 + _message.hashCode();
        return hc * 31 + (_details == null ? 0 : _details.hashCode());
    }

    private static synchronized int getNextId() {
        return ID++;
    }

}
