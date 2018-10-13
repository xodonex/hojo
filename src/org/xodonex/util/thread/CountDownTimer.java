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
 * This class implements a restartable timer which at a given interval can
 * perform actions.
 */
public class CountDownTimer {

    private Timer timer;

    private Runnable action = null;

    private long lastInterval = 1000;
    private long timerInterval = 0;

    // true iff the timer has terminated
    private boolean isTerminated = false;

    // false iff the timer thread has not yet started
    private boolean isStarted = false;

    // true iff a new start (stop) command has been accepted, but not finished
    private boolean command = false;

    // true indicates that the timer should automatically restart.
    private boolean autoRestart = false;

    /**
     * Constructor.
     */
    public CountDownTimer() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param name
     *            The name of the thread.
     */
    public CountDownTimer(String name) {
        this(null, null);
    }

    /**
     * Constructor.
     *
     * @param g
     *            The thread group.
     * @param name
     *            The name of the thread.
     */
    public CountDownTimer(ThreadGroup g, String name) {
        timer = new Timer(g, name);
    }

    /**
     * Checks whether the timer has expired.
     *
     * @return <code>true</code> if the timer has expired, <code>false</code>
     *         otherwise.
     */
    public boolean isExpired() {
        synchronized (timer) {
            return timerInterval == 0;
        }
    }

    /**
     * Checks whether the timer has terminated.
     *
     * @return <code>true</code> if the timer has terminated, <code>false</code>
     *         otherwise.
     */
    public boolean isTerminated() {
        synchronized (timer) {
            return isTerminated;
        }
    }

    /**
     * Checks whether the timer automatically restarts.
     *
     * @return <code>true</code> if the timer automatically restarts,
     *         <code>false</code> otherwise.
     */
    public boolean isAutoRestart() {
        synchronized (timer) {
            return autoRestart;
        }
    }

    /**
     * Specifies whether the timer should automatically restart.
     *
     * @param autoRestart
     *            The value which indicates automatic restart.
     */
    public void setAutoRestart(boolean autoRestart) {
        synchronized (timer) {
            this.autoRestart = autoRestart;
        }
    }

    public void setInterval(long interval) throws IllegalStateException {
        if (interval <= 0) {
            throw new IllegalArgumentException("" + interval);
        }

        synchronized (timer) {
            if (!isExpired()) {
                throw new IllegalStateException();
            }

            lastInterval = interval;
        }
    }

    /**
     * Sets the action to be performed when the timer expires.
     *
     * @param action
     *            The action. If the value is <code>null</code>, then the timer
     *            will not perform any action.
     * @return The previous action.
     */
    public Runnable setAction(Runnable action) {
        synchronized (timer) {
            Runnable result = this.action;
            this.action = action;
            return result;
        }
    }

    /**
     * Stops the timer.
     *
     * @return <code>true</code> if the timer was already stopped,
     *         <code>false</code> otherwise.
     */
    public boolean stop() {
        return start(0);
    }

    /**
     * Starts the timer.
     *
     * @return <code>true</code> if the timer was expired , <code>false</code>
     *         otherwise.
     * @exception IllegalThreadStateException
     *                if the timer has already been terminated
     */
    public boolean start() throws IllegalThreadStateException {
        return start(-1);
    }

    /**
     * Starts the timer.
     *
     * @param delay
     *            the expiration delay, in milliseconds.
     * @return <code>true</code> if the timer was expired, <code>false</code>
     *         otherwise.
     * @exception IllegalThreadStateException
     *                if the timer has already been terminated
     */
    public boolean start(long delay) throws IllegalThreadStateException {
        synchronized (timer) {
            if (isTerminated) {
                throw new IllegalThreadStateException();
            }
            else if (!isStarted) {
                // this will only happen if start() is called before the thread
                // has
                // started
                try {
                    timer.wait();
                }
                catch (InterruptedException e) {
                }
            }

            // the result value
            boolean expired = timerInterval == 0;
            if (delay < 0) {
                // reuse the last timer interval
                delay = lastInterval;
            }
            if (expired && delay == 0) {
                // the timer has already expired - do nothing
                return expired;
            }

            // interrupt the timer to (re)start it, set the command flag and
            // update
            // the timer interval
            timer.interrupt();
            command = true;

            if (delay != 0) {
                lastInterval = delay;
            }
            timerInterval = delay;

            // wait until the timer has reacted
            try {
                timer.wait();
            }
            catch (InterruptedException e) {
            }

            return expired;
        }
    }

    /**
     * Kill the timer thread.
     */
    public void kill() {
        synchronized (timer) {
            if (isTerminated) {
                return;
            }
            else if (!isStarted) {
                // this will only happen if the timer is killed right after
                // being
                // constructed!
                try {
                    timer.wait();
                }
                catch (InterruptedException e) {
                }
            }

            isTerminated = true;
            action = null;
            timer.interrupt();
        }
    }

    private class Timer extends Thread {
        Timer(ThreadGroup g, String name) {
            super(g, (name == null) ? "Timer@" + Integer.toHexString(
                    System.identityHashCode(CountDownTimer.this)) : name);
            super.start();
        }

        @Override
        public synchronized void run() {
            // Indicate that the timer has started
            isStarted = true;
            notify();

            while (true) {
                try {
                    // Count down (or wait until the timer is restarted)
                    wait(timerInterval);
                }
                catch (InterruptedException e) {
                }

                if (isTerminated) {
                    return;
                }
                else if (command) {
                    // The wait was terminated due to a new command - restart
                    command = false;
                    notify();
                    continue;
                }
                // The timer expired - execute the action and update the timer
                // interval to 0 (expired)
                try {
                    if (action != null) {
                        action.run();
                    }
                }
                finally {
                    if (!autoRestart) {
                        timerInterval = 0;
                    }
                }
            } // while
        } // run()
    }

}
