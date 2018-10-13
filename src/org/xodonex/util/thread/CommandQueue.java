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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A FIFO command queue.
 */
public class CommandQueue implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The command queue.
     */
    protected LinkedList q;

    /**
     * Constructor
     */
    public CommandQueue() {
        q = new LinkedList();
    }

    /**
     * Constructor
     *
     * @param c
     *            Initial collection.
     */
    public CommandQueue(Collection c) {
        q = new LinkedList(c);
    }

    /**
     * Commands must be accepted by this method before they are added to the
     * queue.
     *
     * @param obj
     *            the element to be accepted
     * @exception IllegalArgumentException
     *                if the object is not a valid command.
     */
    protected void accept(Object obj) throws IllegalArgumentException {
    }

    /**
     * Adds a single command to the queue
     *
     * @param cmd
     *            The command to be added
     */
    public void put(Object cmd) throws NullPointerException {
        if (cmd == null) {
            throw new NullPointerException();
        }
        synchronized (q) {
            accept(cmd);
            int size = q.size();
            q.addLast(cmd);
            if (size == 0) {
                q.notifyAll();
            }
        }
    }

    /**
     * Atomically adds a collection of commands to the queue.
     *
     * @param cmds
     *            The commands to be added
     */
    public void putAll(Collection cmds) throws NullPointerException {
        boolean notify;
        int added = 0;
        Object obj;

        if (cmds.size() == 0) {
            return;
        }
        synchronized (q) {
            notify = q.size() == 0;
            Iterator it = cmds.iterator();

            try {
                while (it.hasNext()) {
                    added++;
                    if ((obj = it.next()) == null) {
                        throw new NullPointerException();
                    }
                    accept(obj);
                    q.addLast(obj);
                }
            }
            finally {
                if (notify && added > 0) {
                    q.notifyAll();
                }
            }
        }
    }

    /**
     * Get the next command, or suspend until one is available.
     *
     * @return The next command
     * @exception InterruptedException
     *                If the executing thread is interrupted while waiting for a
     *                command.
     */
    public Object get() throws InterruptedException {
        synchronized (q) {
            while (q.size() == 0) {
                q.wait();
            }

            return q.removeFirst();
        }
    }

    /**
     * Get a number of commands without blocking.
     *
     * @param result
     *            The array into which the resulting commands are stored
     * @param offset
     *            The offset into <code>result</code> at which the commands are
     *            stored.
     * @param count
     *            The requested number of commands. The actual number of
     *            returned commands will be the minimum of <code>count</code>
     *            and the actual number of available commands at the time of the
     *            call.
     * @return The number of commands that were stored into the
     *         <code>result</code>.
     * @exception ArrayIndexOutOfBoundsException
     *                If the specified <code>offset</code> or <code>count</code>
     *                do not match the length of the <code>result</code>.
     */
    public int get(Object[] result, int offset, int count)
            throws ArrayIndexOutOfBoundsException {
        synchronized (q) {
            int c = q.size();
            if (c < count) {
                count = c;
            }
            else {
                c = count;
            }

            for (; count > 0; count--) {
                result[offset++] = q.removeFirst();
            }
            return c;
        }
    }

    /**
     * Return the first command in the queue, if any, without removing it.
     *
     * @return The next command, if one is availabe. Otherwise, the result is
     *         <code>null</code>.
     */
    public Object peek() {
        synchronized (q) {
            if (q.size() == 0) {
                return null;
            }
            else {
                return q.get(0);
            }
        }
    }

    /**
     * @return the number of commands in the queue
     */
    public int size() {
        synchronized (q) {
            return q.size();
        }
    }

    /**
     * Clear the command queue.
     *
     * @return The number of commands removed from the queueu
     */
    public int clear() {
        synchronized (q) {
            int result = q.size();
            q.clear();
            return result;
        }
    }

}
