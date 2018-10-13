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

/**
 * A StoppableThread is a thread whose execution can be stopped in a safe manner
 * by invoking the {@link #kill()} method.
 */
public abstract class StoppableThread extends Thread {

    // whether this thread has been killed
    private boolean _killed = false;

    public StoppableThread() {
        super();
    }

    public StoppableThread(String name) {
        super(name);
    }

    public StoppableThread(ThreadGroup group, String name) {
        super(group, name);
    }

    /**
     * Determines whether this thread has been killed. Note that this implies
     * that the thread may still be running, but, if so, will be terminating as
     * soon as possible.
     *
     * @return whether this thread has been killed.
     */
    public final boolean isKilled() {
        synchronized (getControlLock()) {
            return _killed;
        }
    }

    /**
     * Kills the thread. This implementation ensures that
     * <ol>
     * <li>Any subsequent invocation of {@link #isKilled()} will return
     * <code>true</code>.
     * <li>The thread will be {@link Thread#interrupt() interrupted} exactly
     * once.
     * </ol>
     *
     * @return true iff the thread was not killed before
     */
    public boolean kill() {
        synchronized (getControlLock()) {
            if (_killed) {
                // already killed - do nothing
                return false;
            }

            // set the killed status and interrupt the thread
            _killed = true;
            interrupt();
            return true;
        }
    }

    /**
     * @return the object instance which is used as a lock for start/stop
     *         operations on this thread. The default imlementation returns
     *         <code>this</code>.
     */
    protected Object getControlLock() {
        return this;
    }

}
