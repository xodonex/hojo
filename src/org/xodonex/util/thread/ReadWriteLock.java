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
package org.xodonex.util.thread;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class implements a lock for read/write synchronization.
 */
public class ReadWriteLock extends Object {

    /**
     * This thread has writer access.
     */
    protected Thread writer;

    /**
     * The number of times the writer has taken the lock.
     */
    protected int writeOwnerCount = 0;

    /**
     * Table of readers (map of Thread to index).
     */
    protected HashMap readers = new HashMap();

    /**
     * Write requests.
     */
    protected LinkedList writeRequests = new LinkedList();

    /**
     * Indicates whether the lock is empty.
     */
    protected boolean isEmpty = true;

    /**
     * Indicates whether writers are preferred to readers.
     */
    protected boolean preferWriters;

    /**
     * Constructor.
     */
    public ReadWriteLock() {
        this(true);
    }

    /**
     * Constructor.
     *
     * @param preferWriters
     *            Indicator of reader/writer priority.
     */
    public ReadWriteLock(boolean preferWriters) {
        this.preferWriters = preferWriters;
    }

    /**
     * Decides whether a thread is allowed to write.
     *
     * @param t
     *            The thread that should be considered.
     * @return <code>true</code> if the thread can write, <code>false</code>
     *         otherwise.
     */
    private boolean canWrite(Thread t) {
        return isEmpty || writer == t ||
                (readers.size() == 1 && readers.containsKey(t));
    }

    /**
     * Try to get write access.
     *
     * @exception InterruptedException
     *                if interrupted while waiting
     */
    public synchronized void getWriteAccess() throws InterruptedException {
        getWriteAccess(0);
    }

    /**
     * Try to get write access.
     *
     * @param timeout
     *            The maximum time to wait for write access.
     * @exception InterruptedException
     *                if interrupted while waiting
     * @exception IllegalMonitorStateException
     *                if write access is not achieved within the timeout
     */
    public synchronized void getWriteAccess(long timeout)
            throws InterruptedException, IllegalMonitorStateException {
        Thread t = Thread.currentThread();
        long tm;

        // enqueue the request and wait until it can be fulfilled
        writeRequests.addLast(t);
        while (!canWrite(t) || writeRequests.getFirst() != t) {
            if (timeout < 0) {
                writeRequests.remove(t);
                throw new IllegalMonitorStateException();
            }
            if (timeout == 0) {
                wait();
            }
            else {
                tm = System.currentTimeMillis();
                wait(timeout);
                timeout -= System.currentTimeMillis() - tm;
                if (timeout == 0) {
                    timeout--;
                }
            }
        }
        writeRequests.removeFirst();

        if (t == writer) {
            // the current thread is already owner
            writeOwnerCount++;
        }
        else {
            isEmpty = false;
            writer = t;
            writeOwnerCount = 1;
        }
    }

    /**
     * Try to get read access.
     *
     * @exception InterruptedException
     *                if interrupted while waiting
     */
    public synchronized void getReadAccess() throws InterruptedException {
        getReadAccess(0);
    }

    /**
     * Try to get read access.
     *
     * @param timeout
     *            The maximum time to wait for read access.
     * @exception InterruptedException
     *                if interrupted while waiting
     * @exception IllegalMonitorStateException
     *                if read access is not obtained within the timeout
     */
    public synchronized void getReadAccess(long timeout)
            throws InterruptedException, IllegalMonitorStateException {
        Thread t = Thread.currentThread();
        long tm;

        // wait until there are no writers (or write requests)
        while (((writer != t) && (writer != null))
                || (preferWriters && !writeRequests.isEmpty())) {
            if (timeout < 0) {
                throw new IllegalMonitorStateException();
            }
            if (timeout == 0) {
                wait();
            }
            else {
                tm = System.currentTimeMillis();
                wait(timeout);
                timeout -= System.currentTimeMillis() - tm;
                if (timeout == 0) {
                    timeout--;
                }
            }
        }

        isEmpty = false;
        int[] ownerCount = (int[])readers.get(t);
        if (ownerCount == null) {
            readers.put(t, new int[] { 1 });
        }
        else {
            ownerCount[0]++;
        }
    }

    /**
     * Release the current thread.
     */
    public synchronized void release() {
        Thread t = Thread.currentThread();
        int[] ownerCount;

        if ((ownerCount = (int[])readers.get(t)) != null) {
            if (--ownerCount[0] == 0) {
                readers.remove(t);
                if (writer == null && readers.isEmpty()) {
                    isEmpty = true;
                }
            }
        }
        else if (writer == t) {
            if (--writeOwnerCount == 0) {
                writer = null;
                isEmpty = true; // readers.isEmpty() holds here
            }
        }

        if (isEmpty) {
            notifyAll();
        }
    }

    /**
     * Release all threads.
     */
    public void releaseAll() {
        releaseAll(Thread.currentThread());
    }

    /**
     * Release all threads locking on a thread.
     *
     * @param t
     *            The thread that holds the (possible) locks.
     */
    public synchronized void releaseAll(Thread t) {
        if (writer == t) {
            writeOwnerCount = 0;
            writer = null;
        }
        if (readers.containsKey(t)) {
            readers.remove(t);
        }

        isEmpty = (writer == null) && readers.isEmpty();
        if (isEmpty) {
            notifyAll();
        }
    }

    /**
     * Sets reader/writer preference.
     *
     * @param preferWriters
     *            Indicates whether writers are preferred.
     */
    public synchronized void setWriterPreference(boolean preferWriters) {
        this.preferWriters = preferWriters;
    }

    /**
     * @return The value of {@link #preferWriters}.
     */
    public synchronized boolean getWriterPreference() {
        return preferWriters;
    }

    /**
     * Perform a read/write operation while having the lock.
     *
     * @param readWriteOp
     *            The read/write operation
     * @exception UncheckedInterruptedException
     *                if interrupted while waiting
     * @exception IllegalMonitorStateException
     *                if access is not achieved within the (infinite!) timeout
     */
    public void readWriteOperation(Runnable readWriteOp)
            throws UncheckedInterruptedException, IllegalMonitorStateException {
        readWriteOperation(readWriteOp, 0);
    }

    /**
     * Perform a read/write operation while having the lock.
     *
     * @param readWriteOp
     *            The read/write operation
     * @param timeout
     *            The maximum time to wait for write access
     * @exception UncheckedInterruptedException
     *                if interrupted while waiting
     * @exception IllegalMonitorStateException
     *                if access is not achieved within the timeout
     */
    public void readWriteOperation(Runnable readWriteOp, long timeout)
            throws UncheckedInterruptedException, IllegalMonitorStateException {
        try {
            getWriteAccess(timeout);
        }
        catch (InterruptedException e) {
            throw new UncheckedInterruptedException();
        }

        try {
            readWriteOp.run();
        }
        finally {
            release();
        }
    }

    /**
     * Perform a read-only operation while having the lock.
     *
     * @param readOp
     *            The read-only operation
     * @exception UncheckedInterruptedException
     *                if interrupted while waiting
     * @exception IllegalMonitorStateException
     *                if access is not achieved within the (infinite!) timeout
     */
    public void readOnlyOperation(Runnable readOp)
            throws UncheckedInterruptedException, IllegalMonitorStateException {
        readOnlyOperation(readOp, 0);
    }

    /**
     * Perform a read-only operation while having the lock.
     *
     * @param readOp
     *            The read-only operation
     * @param timeout
     *            The maximum time to wait for read access
     * @exception UncheckedInterruptedException
     *                if interrupted while waiting
     * @exception IllegalMonitorStateException
     *                if access is not achieved within the (infinite!) timeout
     */
    public void readOnlyOperation(Runnable readOp, long timeout)
            throws UncheckedInterruptedException, IllegalMonitorStateException {
        try {
            getReadAccess(timeout);
        }
        catch (InterruptedException e) {
            throw new UncheckedInterruptedException();
        }

        try {
            readOp.run();
        }
        finally {
            release();
        }
    }

}
