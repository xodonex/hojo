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

import java.io.IOException;

/**
 * A logger.
 */
public interface Log {

    /**
     * Add the given entry to the log, if it is {@link #setThreshold(int)
     * severe} enough. Before placing the entry in the log, the implementation
     * should call {@link LogEntry#setTimeStamp()} to ensure a consistent
     * ordering between logged entries.
     *
     * @param e
     *            the entry to be logged
     */
    public void log(LogEntry e);

    /**
     * @return the severity threshold.
     * @see #setThreshold(int)
     */
    public int getThreshold();

    /**
     * Set the severity threshold. The log should completely ignore entries
     * which have a lower severity than the specified threshold when
     * {@link #log(LogEntry)} is invoked.
     *
     * @param severity
     *            the threshold severity
     */
    public void setThreshold(int severity);

    /**
     * @return the number of <em>cached entries</em>
     */
    public int size();

    /**
     * @return all <em>cached entries</em>
     */
    public LogEntry[] getEntries();

    /**
     * @return all <em>cached entries</em> stamped in the given time interval
     *
     * @param from
     *            the earliest time (inclusive)
     * @param to
     *            the latest time (exclusive)
     */
    public LogEntry[] getEntries(long from, long to);

    /**
     * Clear all <em>cached entries</em> originating from a date prior to the
     * given date.
     *
     * @param before
     *            the cutoff date
     */
    public void clear(long before);

    /**
     * Update the persistent storage such that its contents are exactly the
     * <em>cached entries</em> present at the time of the call.
     *
     * @return true iff the persistent storage update succeeded.
     */
    public boolean flush();

    /**
     * Closes this log, freeing all resources held. The log will be
     * {@link #flush() flushed} first.
     */
    public void close();

    /**
     * Load all entries which originate from a time later than the given date
     * into the cache.
     *
     * @param since
     *            the cutoff date
     * @throws IOException
     *             on I/O error
     */
    public void reload(long since) throws IOException;

    /**
     * Add the specified {@link LogListener} to this log.
     *
     * @param l
     *            the log listener to be added.
     * @param min
     *            the minimal severity of logged items which should be sent to
     *            the listener.
     * @param max
     *            the maximal severity of logged items which should be sent to
     *            the listener
     * @see LogListener#logged(LogEntry)
     */
    public void addLogListener(LogListener l, int min, int max);

    /**
     * Ensures that the specified {@link LogListener} is set for this log.
     *
     * @param l
     *            the log listener. If it is already present, it will not be
     *            added again.
     * @param type
     *            determines how min and max should be interpreted: if type &lt;
     *            0, then the limits may be narrowed, but not widened if type =
     *            0, then the limits will be set directly if type &gt; 0, then
     *            the limits may be widened, but not narrowed
     * @param min
     *            the least severity setting for the listener.
     * @param max
     *            the highest severity setting for the listener.
     * @see LogListener#logged(LogEntry)
     */
    public void configureLogListener(LogListener l, int type, int min, int max);

    /**
     * Removes a previously registered log listener from this log.
     *
     * @param l
     *            the listener to be removed.
     */
    public void removeLogListener(LogListener l);

    /**
     * This method should be used to create localized log messages, if possible.
     *
     * @param key
     *            a key for the localized message
     * @return a String instance which, if the key is recognized, contains a
     *         locale-specific string representing the log message stored under
     *         the key. If the key is not recognized, the given key should be
     *         returned unmodified. Thus, getLocalizedMessage(s) == s should
     *         hold iff s is not recognized as a key.
     */
    public String getLocalizedMessage(String key);

}
