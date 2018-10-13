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

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

/**
 * A <code>StackedReader</code> implements a Reader whose input stream can be
 * changed dynamically. The <code>StackedReader</code> holds a stack of <code>
 * Reader</code>s, whose topmost element (the active reader) will be used to
 * perform any <code>Reader</code> operation. If the {@link #getAutoClose()
 * autoClose} option is set, a {@link #read() read} operation can span multiple
 * input streams, as the active reader is automatically closed and {@link #pop()
 * pop}ped off the stack,
 * <p>
 * Additionally, the <code>StackedReader</code> supports operations to ensure
 * that a <code>Reader</code> is not {@link #push(java.io.Reader, Object)
 * push}ed twice, to monitor the resulting data stream (if the
 * {@link #getMonitoring() monitoring} option allows it), and to issue a
 * notification when the active reader is automatically closed.
 */
public class StackedReader extends Reader {

    // The reader that is currently operated upon.
    private Reader activeReader = null;

    // The stack of readers that will be read after the active reader
    // has been closed.
    private Stack stack = new Stack();

    // The total number of bytes read since last call to resetTotal()
    private long total = 0;

    // Whether a reader should be closed automatically when it has reached EOF
    private boolean autoClose = true;

    // Whether the monitorBuffer should be cleared when the active reader
    // is popped.
    private boolean autoFlush = true;

    // The observer routine
    private Observer observer = null;

    // Whether read data should be monitored.
    private boolean monitoring = false;

    // This string buffer will receive all read data, if monitor is true.
    private StringBuffer monitorBuffer = new StringBuffer(MONITOR_SIZE);

    // These tables map readers to IDs and IDs to readers
    // (to check for unique readers)
    private HashMap reader2ID = new HashMap();
    private HashMap idCounts = new HashMap();

    /**
     * The initial size for the monitor buffer
     *
     * @see #setMonitoring
     * @see #getMonitorBuffer
     */
    public static int MONITOR_SIZE = 512;

    /**
     *
     */
    public static interface Observer {
        public void readerPopped(Reader r, Object id);
    }

    /**
     * Constructs a new <code>StackedReader</code> that is empty. This results
     * in any <code>read()</code> operation to act as if the end of the input
     * has been reached, while other <code>Reader</code> operatons will return
     * their the active reader is <code>null</code>. No operation can be
     * performed before some Reader has been {@link #push(java.io.Reader)
     * pushed}.
     */
    public StackedReader() {
        super();
    }

    /**
     * Constructs a new <code>StackedReader</code> from a single <code>Reader
     * </code> (which will become the active reader).
     *
     * @param in
     *            The <code>Reader</code> that is to be used
     */
    public StackedReader(Reader in) {
        super();
        activeReader = in;
    }

    /**
     * Returns the active reader.
     *
     * @return The active reader, ie. the topmost <code>Reader</code> on the
     *         stack. If no reader is on the stack, the return value will be
     *         <code>null</code>.
     */
    public Reader getActiveReader() {
        return activeReader;
    }

    /**
     * Returns the previously specified unique ID for the active reader (if
     * any).
     *
     * @return <code>null</code>, if no unique ID was specified, or no active
     *         reader exists. Otherwise the ID will be returned.
     * @see #push(java.io.Reader, java.lang.Object)
     */
    public synchronized Object getActiveID() {
        return (activeReader == null) ? null : reader2ID.get(activeReader);
    }

    /**
     * @return the unique IDs for all the readers currently in the stack
     */
    public synchronized Object[] getIDs() {
        if (activeReader == null) {
            return new Object[0];
        }

        Object[] result = new Object[1 + stack.size()];
        result[0] = reader2ID.get(activeReader);

        // Save top elements first, the reverse order of the stack
        Iterator it = stack.iterator();
        for (int i = result.length - 1; i > 0;) {
            result[i--] = reader2ID.get(it.next());
        }

        return result;
    }

    /**
     * @return the readers that are currently in the stack
     */
    public synchronized Reader[] getReaders() {
        if (activeReader == null) {
            return new Reader[0];
        }

        Reader[] result = new Reader[1 + stack.size()];
        result[0] = activeReader;

        Iterator it = stack.iterator();
        for (int i = 1; i < result.length; i++) {
            result[i] = (Reader)it.next();
        }
        return result;
    }

    /**
     * Determines the number of readers on the stack.
     *
     * @return The length of the reader stack.
     */
    public synchronized int size() {
        return (activeReader == null) ? 0 : stack.size() + 1;
    }

    /**
     * @return the total number of bytes read since last call to
     *         {@link #resetTotal()}.
     */
    public synchronized long getTotalRead() {
        return total;
    }

    /**
     * Resets the byte counter.
     */
    public synchronized void resetTotal() {
        total = 0;
    }

    /**
     * Determines the stacked <code>Reader</code> that has the given unique ID.
     *
     * @param ID
     *            the unique ID to search for
     * @return the <code>Reader</code> having the <code>ID</code>, or
     *         <code>null</code> if none is found.
     */
    public synchronized int getIDCount(Object ID) {
        Object id;
        if (ID == null) {
            return -1;
        }
        else if ((id = idCounts.get(ID)) == null) {
            return 0;
        }
        else {
            return ((Integer)id).intValue();
        }
    }

    /**
     * Searches for the first stacked <code>Reader</code> that has a unique ID
     * of the given <code>Class</code>.
     *
     * @param cls
     *            the <code>Class</code> for which to search.
     * @return the topmost stacked <code>Reader</code> that has a uniuqe ID of
     *         class <code>cls</code>, or <code>null</code> if none is found.
     */
    public synchronized Reader getFirstOfClass(Class cls) {
        if (cls == null) {
            return null;
        }
        if (activeReader == null) {
            return null;
        }
        Object id = reader2ID.get(activeReader);
        if (cls.isInstance(id)) {
            return activeReader;
        }

        Reader r;
        Iterator it = stack.iterator();
        while (it.hasNext()) {
            r = (Reader)it.next();
            id = reader2ID.get(r);
            if (cls.isInstance(id)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Determines the unique ID for the given <code>Reader</code>.
     *
     * @param r
     *            the <code>Reader</code> whose unique ID should be returned.
     * @return the unique ID for <code>r</code>, or <code>null</code> if none is
     *         found.
     */
    public synchronized Object getIDFor(Reader r) {
        return reader2ID.get(r);
    }

    /**
     * Updates the current observer for this <code>StackedReader</code>. The
     * observer (if any) will be {@link Observer#readerPopped(Reader, Object)
     * informed} just <i>before</i> the active reader is closed a result of a
     * {@link #read()} or {@link #skip(long)} - operation.
     *
     * @param observer
     *            the observer to use. <code>null</code> disables observation.
     */
    public synchronized void setObserver(Observer observer) {
        this.observer = observer;
    }

    /**
     * This method (logically) inserts the contents of the given reader's input
     * stream into the input stream of this reader. In effect, the given reader
     * is put onto the stack and so becomes the active reader.
     *
     * @param r
     *            The new active reader
     * @param ID
     *            An unique ID for the <code>reader</code>. If the value is not
     *            <code>null</code>, the ID will matched against the ID's of all
     *            readers currently found in the stack (using the
     *            {@link java.lang.Object#equals(Object) equals()} method on the
     *            ID's). If the ID is already found, an <code>
     * IllegalStateException</code> will be thrown, otherwise the ID is stored
     *            for comparison at a later time ({@link #pop()} automatically
     *            removes the ID).
     * @param maxIDs
     *            the maximum number of IDs to accepe
     * @exception NullPointerException
     *                if <code>r</code> is <code>null</code>.
     * @exception IllegalStateException
     *                if another Reader with same <code>
     * uniqueID</code> is already found in the stack, or if the number of
     *                readers on the stack exceed maxIDs
     */
    public synchronized void push(Reader r, Object ID, int maxIDs)
            throws NullPointerException, IllegalStateException {

        if (r == null) {
            throw new NullPointerException();
        }

        if (ID != null) {
            // Check that the max # of IDs is not exceeded, and save the IDs
            int count = getIDCount(ID);
            if (count >= maxIDs) {
                throw new IllegalStateException(
                        "" + ID + " was activated more than " + maxIDs
                                + " times");
            }
            reader2ID.put(r, ID);
            idCounts.put(ID, new Integer(count + 1));
        }

        // Save the active reader, if any
        if (activeReader != null) {
            stack.push(activeReader);
        }

        // Set r as the new active reader
        activeReader = r;
    }

    /**
     * This method is equivalent to {@link #push(Reader, Object, int) push(r,
     * ID, 1)}.
     * 
     * @param r
     *            The new active reader
     * @param ID
     *            An unique ID for the <code>reader</code>. If the value is not
     *            <code>null</code>, the ID will matched against the ID's of all
     *            readers currently found in the stack (using the
     *            {@link java.lang.Object#equals(Object) equals()} method on the
     *            ID's). If the ID is already found, an <code>
     * IllegalStateException</code> will be thrown, otherwise the ID is stored
     *            for comparison at a later time ({@link #pop()} automatically
     *            removes the ID).
     * @exception NullPointerException
     *                if <code>r</code> is <code>null</code>.
     * @exception IllegalStateException
     *                if another Reader with same <code>
     * uniqueID</code> is already found in the stack.
     */
    public synchronized void push(Reader r, Object ID)
            throws NullPointerException, IllegalStateException {
        push(r, ID, 1);
    }

    /**
     * This method is equivalent to {@link #push(Reader, Object, int) push(r,
     * null, 0)}.
     * 
     * @param r
     *            The new active reader
     * @exception NullPointerException
     *                if <code>r</code> is <code>null</code>.
     */
    public synchronized void push(Reader r) throws NullPointerException {
        try { // (no IllegalStateException will be thrown)
            push(r, null, 0);
        }
        catch (IllegalStateException e) {
            // won't happen
        }
    }

    /**
     * Returns a string representation of this StackedReader
     */
    @Override
    public synchronized String toString() {
        return ("" + activeReader + ", " + stack);
    }

    /**
     * Specifies whether <code>read()</code> operations cause the active reader
     * to be removed from the stack when it has reached the end of its input, or
     * it will be left on the top of the stack. In addition, this value is used
     * as the default argument in {@link #pop(boolean) pop()}.
     *
     * @param value
     *            <code>true</code> will activate automatic {@link #pop()
     *            pop(true)} and {@link java.io.Reader#close() close()} of the
     *            active reader when the end of its input is reached (default),
     *            <code>false</code> prevents it.
     * @see #getAutoClose()
     */
    public synchronized void setAutoClose(boolean value) {
        autoClose = value;
    }

    /**
     * Returns the value previously set by {@link #setAutoClose(boolean)
     * setAutoClose()}.
     *
     * @return <code>true</code>, if <code>autoClose</code> is enabled,
     *         <code>false</code> otherwise (default).
     */
    public synchronized boolean getAutoClose() {
        return autoClose;
    }

    /**
     * Specifies whether the {@link #getMonitorBuffer() monitor buffer} should
     * be flushed when {@link #pop(boolean) pop(true)} is called.
     *
     * @param value
     *            <code>true</code> will activate automatic flush,
     *            <code>false</code> prevents it.
     */
    public synchronized void setAutoFlush(boolean value) {
        autoFlush = value;
    }

    /**
     * Returns the value previously set by {@link #setAutoFlush(boolean)
     * setAutoFlush()}.
     *
     * @return <code>true</code>, if <code>autoFlush</code> is enabled,
     *         (default), <code>false</code> otherwise.
     */
    public synchronized boolean getAutoFlush() {
        return autoFlush;
    }

    /**
     * Specifies whether <code>read()</code> operations cause the monitor buffer
     * to receive the read data.
     *
     * @param value
     *            <code>true</code> will activate monitoring of read data,
     *            <code>false</code> disables it. Be careful using this option,
     *            as the monitor buffer will not automatically be emptied.
     * @see #getMonitoring
     * @see #getMonitorBuffer
     */
    public synchronized void setMonitoring(boolean value) {
        monitoring = value;
    }

    /**
     * Returns the value previously set by <code>setMonitorBuffer()</code>.
     *
     * @return <code>true</code>, if monitoring is enabled, <code>false</code>
     *         otherwise (default).
     * @see #setMonitoring
     */
    public synchronized boolean getMonitoring() {
        return monitoring;
    }

    /**
     * Returns the monitor buffer (which is initially empty and at a capacity of
     * <code>MONITOR_SIZE</code>). Any <code>read()</code> operation will append
     * its result to this buffer, if allowed by <code>setMonitoring()</code>.
     *
     * @return the <code>StringBuffer</code> that is used to monitor read data
     * @see #setMonitoring
     * @see #MONITOR_SIZE
     */
    public StringBuffer getMonitorBuffer() {
        return monitorBuffer;
    }

    /**
     * This operation empties the monitor buffer.
     *
     * @see #setMonitoring
     * @see #getMonitorBuffer
     */
    public synchronized void flushMonitorBuffer() {
        monitorBuffer.delete(0, monitorBuffer.length());
    }

    /**
     * @return the number of characters in the monitor buffer.
     *
     * @see #setMonitoring(boolean)
     * @see #getMonitorBuffer()
     */
    public synchronized int getMonitorBufferSize() {
        return monitorBuffer.length();
    }

    /**
     * Returns the last character in the monitor buffer. If the buffer is empty,
     * <code>-1</code> will be returned.
     *
     * @return <code>-1</code> if the monitor buffer is empty, otherwise its
     *         last character is returned.
     * @see #setMonitoring(boolean)
     * @see #getMonitorBuffer()
     */
    public synchronized int getLastRead() {
        int l = monitorBuffer.length();
        return (l == 0) ? -1 : monitorBuffer.charAt(l - 1);
    }

    /**
     * This removes the active reader from the stack, leaving the next stacked
     * reader as the active reader (if any). The value of <code>doClose</code>
     * determines whether the Reader will be closed before it is returned. In
     * any case, if an ID has been specified for the Reader, it will be removed.
     *
     * @param doClose
     *            whether to close the reader after popping it
     * @return The active reader, or <code>null</code> if none is available.
     * @throws IOException
     *             on I/O error
     * @see #setAutoClose(boolean)
     * @see #push(java.io.Reader, java.lang.Object)
     * @see #push(java.io.Reader, java.lang.Object)
     */
    public synchronized Reader pop(boolean doClose) throws IOException {
        IOException error = null;
        Reader result = activeReader;

        // Empty stack ?
        if (result != null) {
            // Remove the ID, if any
            Object id = reader2ID.get(result);
            if (id != null) {
                reader2ID.remove(id);
                int i = getIDCount(id);
                if (i == 1) {
                    // Remove the number to allow for garbage collection
                    idCounts.remove(id);
                }
                else {
                    // Decrease the count
                    idCounts.put(id, new Integer(i - 1));
                }
            }

            // Close, if required
            if (doClose) {
                try {
                    result.close();
                }
                catch (IOException e) {
                    error = e;
                }
            }

            // Flush, if required
            if (autoFlush && doClose) {
                flushMonitorBuffer();
            }
        }

        activeReader = (stack.size() == 0) ? null : (Reader)stack.pop();

        if (error != null) {
            throw error;
        }
        return result;
    }

    /**
     * This method is equivalent to <code>pop(getAutoClose())</code>
     *
     * @return the popped reader
     * @throws IOException
     *             on I/O error
     */
    public Reader pop() throws IOException {
        return pop(autoClose);
    }

    /**
     * Overrides <code>read()</code> in <code>java.io.Reader</code>. This will
     * read a single character from the active reader, <code>pop</code>ping if
     * necessary and allowed, until a character is read or all Readers are
     * <code>pop</code>ed.
     *
     * @see java.io.Reader#read
     * @see #setAutoClose
     */
    @Override
    public synchronized int read() throws IOException {
        if (activeReader == null) {
            return -1;
        }

        int result = activeReader.read();
        while (result == -1) {
            // Get the previous Reader, if allowed
            if (autoClose || observer != null) {
                if (observer != null) {
                    observer.readerPopped(activeReader,
                            reader2ID.get(activeReader));
                    pop(false);
                }
                else if (autoClose) {
                    pop(true);
                }
            }
            else {
                return -1;
            }

            // The last character has been read, and the stack is empty
            if (activeReader == null) {
                return -1;
            }

            // Try reading a character again
            result = activeReader.read();
        }
        if (monitoring && (result != -1)) {
            monitorBuffer.append((char)result);
        }

        if (result >= 0) {
            total++;
        }
        return result;
    }

    /**
     * Overrides <code>read(char[] cbuf)</code> in <code>java.io.Reader</code>.
     * The operation is influenced by the <code>autoClose</code> option in the
     * same way as <code>read()</code>.
     *
     * @see java.io.Reader#read(char[])
     * @see #read
     * @see #setAutoClose
     */
    @Override
    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    /**
     * Implements <code>read(char[] cbuf, int off, int len)</code> in <code>
     * java.io.Reader</code>. The operation is influenced by the
     * <code>autoClose</code> option in the same way as <code>read()</code>.
     *
     * @see java.io.Reader#read(char[], int, int)
     * @see #read
     * @see #setAutoClose
     */
    @Override
    public synchronized int read(char[] cbuf, int off, int len)
            throws IOException {
        int remaining = len;
        int bytesRead = 0;
        int lastRead = 0;

        if (len <= 0) {
            return len;
        }

        while (true) {
            if (activeReader == null) {
                break;
            }

            lastRead = activeReader.read(cbuf, off, remaining);

            if (lastRead > 0) {
                off += lastRead;
                bytesRead += lastRead;
                remaining -= lastRead;
            }

            if (remaining > 0) {
                // close and pop, if allowed
                if (observer != null) {
                    observer.readerPopped(activeReader,
                            reader2ID.get(activeReader));
                    pop(false);
                }
                else if (autoClose) {
                    pop(true);
                }
                else {
                    break;
                }
            }
            else {
                break;
            }
        }

        // indicate EOF
        if (bytesRead == 0) {
            bytesRead = -1;
        }

        // add to monitor buffer, if allowed
        if (monitoring && (bytesRead > 0)) {
            monitorBuffer.append(cbuf, 0, bytesRead);
        }

        if (bytesRead > 0) {
            total += bytesRead;
        }
        return bytesRead;
    }

    /**
     * Overrides <code>skip(long n)</code> in <code>java.io.Reader</code>. The
     * operation is influenced by <code>autoClose</code> in the same way as
     * <code>read()</code>.
     *
     * @see java.io.Reader#skip(long)
     * @see #read
     * @see #setAutoClose
     */
    @Override
    public synchronized long skip(long n) throws IOException {
        long remaining = n;
        long bytesSkipped = 0;
        long lastSkipped = 0;

        while (true) {
            if (activeReader == null) {
                return ((bytesSkipped > 0) ? bytesSkipped : -1);
            }

            lastSkipped = activeReader.skip(remaining);
            if (lastSkipped > 0) {
                bytesSkipped += lastSkipped;
                remaining -= lastSkipped;
            }

            if (remaining > 0) {
                // Get the previous Reader, if allowed
                if (observer != null) {
                    observer.readerPopped(activeReader,
                            reader2ID.get(activeReader));
                    pop(false);
                }
                else if (autoClose) {
                    pop(true);
                }
                else {
                    return bytesSkipped;
                }
            }
            else {
                return bytesSkipped;
            }
        }
    }

    /**
     * Overrides <code>ready()</code> in <code>java.io.Reader</code>. The
     * current reader's <code>ready()</code> return value is returned, or
     * <code>null</code> if no active reader exists.
     *
     * @see java.io.Reader#ready
     */
    @Override
    public synchronized boolean ready() throws IOException {
        return ((activeReader == null) ? false : activeReader.ready());
    }

    /**
     * Overrides <code>markSupported()</code> in <code>java.io.Reader
     * </code>. The current reader's <code>markSupported()</code> return value
     * is returned, or <code>null</code> if no active reader exists.
     *
     * @see java.io.Reader#markSupported
     */
    @Override
    public synchronized boolean markSupported() {
        return ((activeReader == null) ? false : activeReader.markSupported());
    }

    /**
     * Overrides <code>mark(int readAheadLimit)</code> in <code>
     * java.io.Reader</code>. The current reader's <code>ready()</code> return
     * value is returned.
     *
     * @exception EOFException
     *                if no active reader exists
     * @see java.io.Reader#mark
     */
    @Override
    public synchronized void mark(int readAheadLimit) throws IOException {
        if (activeReader != null) {
            activeReader.mark(readAheadLimit);
        }
        else {
            throw new EOFException();
        }
    }

    /**
     * Overrides <code>reset()</code> in <code>java.io.Reader</code>. This
     * operates on the active reader only.
     *
     * @exception EOFException
     *                if no active reader exists
     * @see java.io.Reader#reset
     */
    @Override
    public synchronized void reset() throws IOException {
        if (activeReader != null) {
            activeReader.reset();
        }
        else {
            throw new IOException();
        }
    }

    /**
     * Closes the <i>topmost</i> reader, if one exists. Use {@link #closeAll()}
     * to close the entier reader stack.
     */
    @Override
    public synchronized void close() throws IOException {
        if (activeReader != null) {
            activeReader.close();
        }
    }

    /**
     * Closes all stacked readers, and leaves an empty stack.
     *
     * @exception IOException
     *                if a stacked reader's {@link java.io.Reader#close()
     *                close()} method throws an exception (if multiple
     *                exceptions were thrown, only the last one will be thrown).
     * @see #close()
     */
    public synchronized void closeAll() throws IOException {
        IOException error = null;
        while (activeReader != null) {
            try {
                pop(true);
            }
            catch (IOException e) {
                error = e;
            }
        }
        if (error != null) {
            throw error;
        }
    }

}
