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
package org.xodonex.util.ui.comp;

import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.xodonex.util.ui.WindowManager;

/**
 * A splash window.
 */
public class Splash {

    private SplashThread st;

    public Splash(Image image) {
        this(image, 0);
    }

    public Splash(Image image, long delay) {
        this(image, delay, true);
    }

    public Splash(Image image, long delay, boolean hideOnClick) {
        st = new SplashThread(image, delay, hideOnClick);
    }

    public synchronized boolean isActive() {
        return st != null;
    }

    public boolean remove() {
        SplashThread _st;
        synchronized (this) {
            if ((_st = st) == null) {
                return false;
            }
        }
        return _st.deactivate();
    }

    private class SplashThread extends Thread {
        private JWindow wnd;
        private JLabel lbl;
        private Image img;
        private MediaTracker tracker;
        private boolean shown = true;
        private long delay;

        public SplashThread(Image image, long delay, boolean hideOnClick) {
            super();
            wnd = new JWindow();

            img = image;
            lbl = new JLabel(new ImageIcon(img));
            tracker = new MediaTracker(lbl);
            tracker.addImage(img, 0);
            this.delay = delay;

            if (hideOnClick) {
                wnd.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        deactivate();
                    }
                });
            }

            start();
        }

        @Override
        public synchronized void run() {
            try {
                if (tracker == null) {
                    // already deactivated!
                    return;
                }

                tracker.waitForID(0);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        wnd.getContentPane().add(lbl);
                        WindowManager.showCentered(wnd);
                    }
                });
                wait(delay);
            }
            catch (Exception e) {
                if (!(e instanceof InterruptedException)) {
                    e.printStackTrace();
                }
            }
            deactivate();
        }

        public synchronized boolean deactivate() {
            if (!shown) {
                return false;
            }
            synchronized (Splash.this) {
                st = null;
            }
            shown = false;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    wnd.setVisible(false);
                    wnd.dispose();
                    wnd = null;
                    lbl = null;
                    img.flush();
                    img = null;
                    tracker = null;
                }
            });
            interrupt();
            return true;
        }

    }
}
