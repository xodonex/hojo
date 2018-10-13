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
 * This class implements a thread which performs some operation at a regular
 * interval which is modifiable. By setting the delay to 0, the operation of the
 * thread is paused until the interval is reset to a positive value.
 */
public abstract class PausableThread extends StoppableThread {

    // the current delay interval
    private long _delay;

    // the last delay interval
    private long _lastDelay;

    /**
     * Constructor.
     *
     * @param delay
     *            The default delay.
     */
    public PausableThread(long delay) {
        super();
        _lastDelay = _delay = delay < 1 ? 1 : delay;
    }

    /**
     * Constructor.
     *
     * @param name
     *            The name of the thread.
     * @param delay
     *            The default delay.
     */
    public PausableThread(String name, long delay) {
        super(name);
        _lastDelay = _delay = delay < 1 ? 1 : delay;
    }

    /**
     * Constructor.
     *
     * @param group
     *            The thread group.
     * @param name
     *            The name of the thread.
     * @param delay
     *            The default delay.
     */
    public PausableThread(ThreadGroup group, String name, long delay) {
        super(group, name);
        _lastDelay = _delay = delay < 1 ? 1 : delay;
    }

    /**
     * Sets the next delay to be used by the thread. A value of 0 will pause the
     * thread, while a negative value is used to reset the last used delay. If a
     * positive delay
     *
     * @param delay
     *            The new delay.
     */
    public void setDelay(long delay) {
        // whether the thread is on pause and should be restarted
        boolean wakeUp;
        Object lock;

        synchronized (lock = getControlLock()) {
            if (!(wakeUp = _delay == 0)) {
                if (delay == 0) {
                    // already on pause - do nothing
                    return;
                }

                // update the last delay
                _lastDelay = _delay;
            }
            if (delay < 0) {
                // revert to the last delay on a negative delay
                _delay = _lastDelay;
            }
            else {
                // set the current delay to the indicated delay
                _delay = delay;
            }

            // wake up the thread if it was on pausenecessary
            if (wakeUp) {
                lock.notify();
            }
        }
    }

    /**
     * Pause the thread. This is equivalent to <code>setDelay(0)</code>
     */
    public void pause() {
        setDelay(0);
    }

    /**
     * Set the last delay and wake up the thread if paused. This is equivalent
     * to <code>setDelay(-1)</code>
     */
    public void unpause() {
        setDelay(-1);
    }

    /**
     * This method should be invoked from the main loop of the thread. The
     * method will block until the current delay has expired, or until
     * explicitly unpaused, if the current delay is 0.
     *
     * @return <code>true</code> iff the delay expired normally. If the return
     *         value is false, this means that either
     *         <ol>
     *         <li>The thread was interrupted during the wait. This should only
     *         be done from {@link StoppableThread#kill()}.
     *         <li>The thread was already killed when the method was invoked.
     *         </ol>
     *
     *         A PausableThread should thus be implemented as <code>
     * public void run() {
     *     while (delay()) {
     *         // update the thread as necessary here, ensuring that
     *         // interruptions result in termination.
     *         // The object returned by getControlLock must not be used
     *         // by the implementation for synchronization purposes.
     *    }
     * }
     * </code> ensuring that interruption
     */
    protected boolean delay() {
        Object lock;

        try {
            synchronized (lock = getControlLock()) {
                if (isKilled()) {
                    // already killed
                    return false;
                }

                // wait for the duration of the current delay
                lock.wait(_delay);
                return true;
            }
        }
        catch (InterruptedException e) {
            return false;
        }
    }

}
