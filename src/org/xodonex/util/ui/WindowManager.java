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
package org.xodonex.util.ui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.WeakHashMap;

import javax.swing.SwingConstants;

public class WindowManager implements SwingConstants {

    private final static int screenWidth;
    private final static int screenHeight;
    static {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenWidth = screenSize.width;
        screenHeight = screenSize.height;
    }

    private final static Point screenCenter = new Point(screenWidth / 2,
            screenHeight / 2);

    // cascading offset
    private final static int OFFSET_X = 32;
    private final static int OFFSET_Y = 32;

    // Origins for new windows
    private static int originX = screenWidth / 6;
    private static int originY = screenHeight / 6;

    // window -m-> Glue
    private static WeakHashMap glueMaster = new WeakHashMap();

    // window -m-> Glue
    private static WeakHashMap glueSlave = new WeakHashMap();

    private static Dimension tempDim = new Dimension();

    private WindowManager() {
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    public static void centerOnScreen(Window c) throws NullPointerException {
        centerRelativeTo(c, screenCenter);
    }

    public static void centerRelativeTo(Window c, Point p)
            throws NullPointerException {
        int w = c.getWidth();
        int h = c.getHeight();

        if (screenHeight < h) {
            h = screenHeight;
            if (screenWidth < w) {
                w = screenWidth;
            }
            c.setSize(w, h);
        }
        else if (screenWidth < w) {
            w = screenWidth;
            if (screenHeight < h) {
                h = screenHeight;
            }
            c.setSize(w, h);
        }

        c.setLocation(p.x - (w >> 1), p.y - (h >> 1));
    }

    public static void showCentered(Window c) throws NullPointerException {
        c.pack();
        centerRelativeTo(c, screenCenter);
        c.setVisible(true);
    }

    public static synchronized void showResizedWindow(Window c)
            throws NullPointerException {
        c.getSize(tempDim);

        if ((originX + tempDim.width >= screenWidth) ||
                (originY + tempDim.height >= screenHeight)) {
            // start at the top-left corner of the screen, if the window won't
            // fit
            // at the current origin
            originX = 0;
            originY = 0;
        }

        c.setLocation(originX, originY);
        originX += OFFSET_X;
        originY += OFFSET_Y;

        c.setVisible(true);
    }

    public static void showWindow(Window c, double widthFactor,
            double heightFactor)
            throws IllegalArgumentException {
        showWindow(c, widthFactor, heightFactor, true);
    }

    public static void showWindow(Window c, double widthFactor,
            double heightFactor,
            boolean centered) throws IllegalArgumentException {
        showWindow(c,
                (int)((widthFactor > 1.0 ? 1.0 : screenWidth) * widthFactor),
                (int)((heightFactor > 1.0 ? 1.0 : screenHeight)
                        * heightFactor),
                centered);
    }

    public static void showWindow(Window c, int width, int height)
            throws IllegalArgumentException {
        showWindow(c, width, height, true);
    }

    public static void showWindow(Window c, int width, int height,
            boolean centered)
            throws IllegalArgumentException {
        if ((width <= 0) || (height <= 0)) {
            throw new IllegalArgumentException(
                    "(" + width + ", " + height + ")");
        }

        c.pack();
        c.setSize(width, height);
        c.validate();
        if (centered) {
            centerRelativeTo(c, screenCenter);
            c.setVisible(true);
        }
        else {
            showResizedWindow(c);
        }
    }

    public static void alignWindow(Window w, Window alignTo, int pos,
            boolean resize) {
        if (alignTo == null) {
            // align to screen boundaries
            switch (pos) {
            case BOTTOM:
                if (resize) {
                    w.setSize(screenWidth, w.getHeight());
                    w.setLocation(0, screenHeight - w.getHeight());
                }
                else {
                    w.setLocation(w.getX(), screenHeight - w.getHeight());
                }
                return;
            case TOP:
                if (resize) {
                    w.setSize(screenWidth, w.getHeight());
                    w.setLocation(0, 0);
                }
                else {
                    w.setLocation(w.getX(), 0);
                }
                return;
            case LEFT:
                if (resize) {
                    w.setSize(w.getWidth(), screenHeight);
                    w.setLocation(0, 0);
                }
                else {
                    w.setLocation(0, w.getY());
                }
                return;
            case RIGHT:
                if (resize) {
                    w.setSize(w.getWidth(), screenHeight);
                    w.setLocation(screenWidth - w.getWidth(), 0);
                }
                else {
                    w.setLocation(screenWidth - w.getWidth(), w.getY());
                }
                return;
            case CENTER:
                if (resize) {
                    w.setSize(screenWidth, screenHeight);
                    w.setLocation(0, 0);
                }
                else {
                    w.setLocation(screenCenter.x - w.getWidth() / 2,
                            screenCenter.y - w.getHeight() / 2);
                }
                return;
            default:
                throw new IllegalArgumentException("Invalid position " + pos);
            }
        } // alignTo == null

        switch (pos) {
        case BOTTOM:
        case TOP:
            int h = w.getHeight();
            if (resize) {
                w.setSize(alignTo.getWidth(), h);
            }
            if (pos == TOP) {
                w.setLocation(alignTo.getX(), alignTo.getY() - h);
            }
            else {
                w.setLocation(alignTo.getX(),
                        alignTo.getY() + alignTo.getHeight());
            }
            return;
        case LEFT:
        case RIGHT:
            int width = w.getWidth();
            if (resize) {
                w.setSize(width, alignTo.getHeight());
            }
            if (pos == LEFT) {
                w.setLocation(alignTo.getX() - width, alignTo.getY());
            }
            else {
                w.setLocation(alignTo.getX() + alignTo.getWidth(),
                        alignTo.getY());
            }
            return;
        default:
            throw new IllegalArgumentException("Invalid position " + pos);
        }
    }

    public static void unglue(Frame wnd) {
        Glue g = (Glue)glueSlave.get(wnd);
        if (g != null) {
            g.unglue(wnd);
        }
    }

    public static void glue(Frame wnd, Frame glueTo, int pos, boolean resize) {
        if (wnd == null || glueTo == null) {
            throw new NullPointerException();
        }
        else if (wnd == glueTo || glueSlave.containsKey(glueTo)) {
            throw new IllegalArgumentException("window " + glueTo);
        }

        switch (pos) {
        case LEFT:
        case RIGHT:
        case TOP:
        case BOTTOM:
            break;
        default:
            throw new IllegalArgumentException("position " + pos);
        }

        unglue(wnd);
        Glue g = (Glue)glueMaster.get(glueTo);
        if (g == null) {
            g = new Glue(glueTo);
        }

        g.glue(wnd, pos, resize);
    }

    private static class Glue implements WindowListener, ComponentListener {
        private boolean isAdjusting = false;
        private WeakReference master;
        private ArrayList slaves, slavePos, slaveResize;

        Glue(Frame master) {
            this.master = new WeakReference(master);
            slaves = new ArrayList(4);
            slavePos = new ArrayList(4);
            slaveResize = new ArrayList(4);

            glueMaster.put(master, this);
            master.addWindowListener(this);
            master.addComponentListener(this);
        }

        @Override
        public String toString() {
            return "" + master + "\n" + slaves + "\n" + slavePos + "\n"
                    + slaveResize;
        }

        public void unglue(Frame w) {
            for (int i = 0; i < slaves.size();) {
                // check all references
                WeakReference ref = (WeakReference)slaves.get(i);
                Frame wnd = (Frame)ref.get();
                if (wnd == null || wnd == w) {
                    // clear the specified window, and any otherwise finalized
                    // windows
                    glueSlave.remove(wnd);
                    slaves.remove(i);
                    slavePos.remove(i);
                    slaveResize.remove(i);
                    continue;
                }
                i++;
            }

            if (slaves.size() == 0) {
                // no more slaves : clear this glue, if the master is not
                // already cleared
                Window m = (Window)master.get();
                if (m != null) {
                    m.removeWindowListener(this);
                    m.removeComponentListener(this);
                    glueMaster.remove(m);
                }
            }
        }

        public boolean glue(Frame w, int pos, boolean resize) {
            Frame m = (Frame)master.get();
            if (m == null) {
                // master is finalized: remove all slaves, return false
                for (Iterator i = slaves.iterator(); i.hasNext();) {
                    glueSlave.remove(((WeakReference)i.next()).get());
                }
                return false;
            }

            int idx = slaves.indexOf(w);
            if (idx >= 0) {
                // change of config
                slavePos.set(idx, new Integer(pos));
                slaveResize.set(idx, resize ? Boolean.TRUE : Boolean.FALSE);
            }
            else {
                // add a new slave
                glueSlave.put(w, this);
                slaves.add(new WeakReference(w));
                slavePos.add(new Integer(pos));
                slaveResize.add(resize ? Boolean.TRUE : Boolean.FALSE);
            }

            // align the glued window immediately
            if (m.isVisible() && w.isVisible()) {
                if (m.getState() == Frame.NORMAL) {
                    w.setState(Frame.NORMAL);
                    alignWindow(w, m, pos, resize);
                }
                else {
                    w.setState(Frame.ICONIFIED);
                }
            }

            return true;
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            updateAll(-1, false);
        }

        @Override
        public void componentResized(ComponentEvent e) {
            componentMoved(null);
        }

        @Override
        public void componentShown(ComponentEvent e) {
            windowActivated(null);
        }

        @Override
        public void windowActivated(WindowEvent e) {
            if (isAdjusting) {
                // ignore the event generated by reactivating the frame after
                // all
                // slaves have been activated.
                isAdjusting = false;
                return;
            }

            isAdjusting = true;

            for (Iterator i = slaves.iterator(); i.hasNext();) {
                Frame f = (Frame)((WeakReference)i.next()).get();
                if (f != null) {
                    f.toFront();
                }
            }

            ((Frame)master.get()).toFront();
        }

        @Override
        public void windowClosed(WindowEvent e) {
        }

        @Override
        public void windowClosing(WindowEvent e) {
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
            updateAll(Frame.NORMAL, false);
        }

        @Override
        public void windowIconified(WindowEvent e) {
            updateAll(Frame.ICONIFIED, false);
        }

        @Override
        public void windowOpened(WindowEvent e) {
            updateAll(Frame.NORMAL, true);
        }

        private void updateAll(int iconStatus, boolean setVisible) {
            Frame m = (Frame)master.get();
            for (int i = slaves.size() - 1; i >= 0; i--) {
                Frame f = (Frame)((WeakReference)slaves.get(i)).get();
                if (f != null) {
                    if (iconStatus >= 0) {
                        f.setState(iconStatus);
                    }
                    if (setVisible) {
                        f.setVisible(true);
                    }
                    alignWindow(f, m, ((Integer)slavePos.get(i)).intValue(),
                            ((Boolean)slaveResize.get(i)).booleanValue());
                }
            }
        }
    }

}
