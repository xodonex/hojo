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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Utilities for threading.
 */
public final class ThreadUtils {

    private ThreadUtils() {
    }

    private static ThreadGroup swingGroup;
    private static Collection initialSwingThreads;

    /**
     * Start the persistent AWT/Swing EventQueue, PostEventQueue, Windows and
     * Screen updater threads, such that the VM can be safely terminated once
     * these threads are the only threads running. N.B.: This must be invoked
     * before any Swing or AWT components are instantiated to have any effect.
     *
     * @return the thread group containing the threads started during this
     *         operation.
     */
    public static ThreadGroup startSwing() {
        swingGroup = new ThreadGroup("Swing");
        Thread d = new Thread(swingGroup, "starter") {
            @Override
            public void run() {
                // determine whether the active VM is 1.4 or later
                boolean isVer14 = false;
                try {
                    String ver = System.getProperty("java.vm.version");
                    if (ver.charAt(0) > '1' || ver.charAt(2) >= '4') {
                        isVer14 = true;
                    }
                }
                catch (Exception e) {
                }

                // HACK: execute some dummy operations to ensure that the most
                // commonly
                // used persistent Swing threads are started. This is necessary
                // in order to circumvent Java bug 4248088
                javax.swing.JWindow wnd = new javax.swing.JWindow();
                wnd.setLocation(32000, 32000);
                // if (!isVer14) {
                // wnd.getContentPane().add(new javax.swing.JButton(new
                // javax.swing.ImageIcon(new byte[] {-1, -40, -1, -32, 0, 16,
                // 74, 70, 73, 70, 0, 1, 1, 1, 0, 72, 0, 72, 0, 0, -1, -37, 0,
                // 67, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                // -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                // -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                // -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                // -1, -1, -1, -1, -1, -1, -1, -64, 0, 11, 8, 0, 2, 0, 2, 1, 1,
                // 17, 0, -1, -60, 0, 31, 0, 0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0,
                // 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, -1, -60, 0,
                // -75, 16, 0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 125, 1,
                // 2, 3, 0, 4, 17, 5, 18, 33, 49, 65, 6, 19, 81, 97, 7, 34, 113,
                // 20, 50, -127, -111, -95, 8, 35, 66, -79, -63, 21, 82, -47,
                // -16, 36, 51, 98, 114, -126, 9, 10, 22, 23, 24, 25, 26, 37,
                // 38, 39, 40, 41, 42, 52, 53, 54, 55, 56, 57, 58, 67, 68, 69,
                // 70, 71, 72, 73, 74, 83, 84, 85, 86, 87, 88, 89, 90, 99, 100,
                // 101, 102, 103, 104, 105, 106, 115, 116, 117, 118, 119, 120,
                // 121, 122, -125, -124, -123, -122, -121, -120, -119, -118,
                // -110, -109, -108, -107, -106, -105, -104, -103, -102, -94,
                // -93, -92, -91, -90, -89, -88, -87, -86, -78, -77, -76, -75,
                // -74, -73, -72, -71, -70, -62, -61, -60, -59, -58, -57, -56,
                // -55, -54, -46, -45, -44, -43, -42, -41, -40, -39, -38, -31,
                // -30, -29, -28, -27, -26, -25, -24, -23, -22, -15, -14, -13,
                // -12, -11, -10, -9, -8, -7, -6, -1, -38, 0, 8, 1, 1, 0, 0, 63,
                // 0, 43, -1, -39})));
                // }
                wnd.pack();
                wnd.setVisible(true);

                if (!isVer14) {
                    // These are not necessary in Java 1.4
                    javax.swing.Timer t = new javax.swing.Timer(
                            Integer.MAX_VALUE, null);
                    t.start();
                    t.stop();
                }

                wnd.dispose();
                wnd = null;
            }
        };

        d.start();
        try {
            d.join();
        }
        catch (InterruptedException e) {
        }

        // under Java 1.4.0 there should be 3 threads here:
        // AWT-Shutdown, AWT-Windows (daemon) and AWT-EventQueue.
        // under Java 1.4.1 the Java2D Disposer is also present.
        // prior to Java 1.4, include the timer thread instead.
        Thread[] ts = new Thread[swingGroup.activeCount() + 2];
        int size = swingGroup.enumerate(ts);

        initialSwingThreads = new ArrayList(size);
        for (int i = 0; i < ts.length; i++) {
            if (ts[i] != null) {
                initialSwingThreads.add(ts[i]);
            }
        }

        return swingGroup;
    }

    public static void killSwing() {
        if (swingGroup != null) {
            Thread[] ts = new Thread[swingGroup.activeCount()];
            swingGroup.enumerate(ts);
            for (int i = 0; i < ts.length; i++) {
                if (ts[i] == null) {
                    break;
                }
                else {
                    ts[i].interrupt();
                }
            }
        }
    }

    public static void waitForSwing() {
        if (swingGroup == null) {
            return;
        }

        waitForThreads(null, initialSwingThreads, 100);
    }

    public static boolean waitForThreads(Collection threads) {
        return waitForThreads(null, threads, 100);
    }

    /**
     * Wait until a thread group only contains threads which are either
     * <ul>
     * <li>The current thread
     * <li>Daemon threads
     * <li>Threads contained in the given collection.
     * </ul>
     *
     * @param tg
     *            the thread group
     * @param threads
     *            see description
     * @param pollInterval
     *            the number of milliseconds between each check.
     * @return false if interrupted, true if the operation succeeded
     */
    public static boolean waitForThreads(ThreadGroup tg,
            Collection threads, long pollInterval) {
        Thread current = Thread.currentThread();
        if (tg == null) {
            tg = current.getThreadGroup();
        }

        HashSet checkSet = new HashSet();
        Thread[] checkThreads = new Thread[tg.activeCount() + 5];

        while (true) {
            checkSet.clear();
            for (int i = 0, max = tg.enumerate(checkThreads); i < max; i++) {
                checkSet.add(checkThreads[i]);
            }

            checkSet.removeAll(threads);
            checkSet.remove(current);
            boolean ok = true;
            for (Iterator i = checkSet.iterator(); i.hasNext();) {
                Thread t = (Thread)i.next();
                if (!t.isDaemon()) {
                    ok = false;
                    break;
                }
            }

            if (ok) {
                break;
            }

            try {
                Thread.sleep(pollInterval);
            }
            catch (InterruptedException e) {
                return false;
            }
        }

        return true;
    }

    public static String listThreadGroup() {
        return listThreadGroup(Thread.currentThread().getThreadGroup());
    }

    public static String listThreadGroup(ThreadGroup tg) {
        int active;
        Thread[] ts = new Thread[(active = tg.activeCount()) + 4];
        int max = tg.enumerate(ts);
        StringBuffer buf = new StringBuffer();

        buf.append("active: " + active + ". max: " + max + "\n");
        for (int i = 0; i < ts.length; i++) {
            if (ts[i] != null) {
                buf.append(ts[i]).append("(").append(ts[i].getClass().getName())
                        .append("= : daemon=").append(ts[i].isDaemon())
                        .append('\n');
            }
        }

        return buf.toString();
    }
}
