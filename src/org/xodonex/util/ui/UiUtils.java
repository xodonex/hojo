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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;

/**
 * @author Henrik Lauritzen
 */
public class UiUtils {

    public final static Metric METRIC_ACTUAL = new Metric() {
        @Override
        public Dimension measure(Component c) {
            return c.getSize();
        }

        @Override
        public Dimension measure(Component c, Dimension d) {
            return c.getSize(d);
        }

        @Override
        public void setMeasure(Component c, Dimension d) {
            c.setSize(d);
        }
    };

    public final static Metric METRIC_MINIMUM = new Metric() {
        @Override
        public Dimension measure(Component c) {
            return c.getMinimumSize();
        }

        @Override
        public Dimension measure(Component c, Dimension d) {
            return c.getMinimumSize();
        }

        @Override
        public void setMeasure(Component c, Dimension d) {
            ((JComponent)c).setMinimumSize(d);
        }
    };

    public final static Metric METRIC_PREFERRED = new Metric() {
        @Override
        public Dimension measure(Component c) {
            return c.getPreferredSize();
        }

        @Override
        public Dimension measure(Component c, Dimension d) {
            return c.getPreferredSize();
        }

        @Override
        public void setMeasure(Component c, Dimension d) {
            ((JComponent)c).setPreferredSize(d);
        }
    };

    public final static Metric METRIC_MAXIMUM = new Metric() {
        @Override
        public Dimension measure(Component c) {
            return c.getMaximumSize();
        }

        @Override
        public Dimension measure(Component c, Dimension d) {
            return c.getMaximumSize();
        }

        @Override
        public void setMeasure(Component c, Dimension d) {
            ((JComponent)c).setMaximumSize(d);
        }
    };

    private UiUtils() {
    }

    public static Component findNarrowestComponent(Collection cs, Metric m) {
        int minW = Integer.MAX_VALUE;
        Component result = null;
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            int w = m.measure(c, d).width;
            if (w < minW) {
                minW = w;
                result = c;
            }
        }

        return result;
    }

    public static Component findWidestComponent(Collection cs, Metric m) {
        int maxW = 0;
        Component result = null;
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            int w = m.measure(c, d).width;
            if (w > maxW) {
                maxW = w;
                result = c;
            }
        }

        return result;
    }

    public static Component findLowestComponent(Collection cs, Metric m) {
        int minH = Integer.MAX_VALUE;
        Component result = null;
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            int h = m.measure(c, d).height;
            if (h < minH) {
                minH = h;
                result = c;
            }
        }

        return result;
    }

    public static Component findHighestComponent(Collection cs, Metric m) {
        int maxH = 0;
        Component result = null;
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            int h = m.measure(c, d).height;
            if (h > maxH) {
                maxH = h;
                result = c;
            }
        }

        return result;
    }

    public static Dimension findJointMinimum(Collection cs, Metric m) {
        Dimension d = new Dimension(),
                result = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            d = m.measure(c, d);
            if (d.width < result.width) {
                result.width = d.width;
            }
            if (d.height < result.height) {
                result.height = d.height;
            }
        }

        return result;
    }

    public static Dimension findJointMaximum(Collection cs, Metric m) {
        Dimension d = new Dimension(), result = new Dimension(0, 0);

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            d = m.measure(c, d);
            if (d.width > result.width) {
                result.width = d.width;
            }
            if (d.height > result.height) {
                result.height = d.height;
            }
        }

        return result;
    }

    public static void ensureWidth(Collection cs, Metric m, int w) {
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            d = m.measure(c, d);
            if (d.width < w) {
                d.width = w;
                m.setMeasure(c, d);
            }
        }
    }

    public static void ensureHeight(Collection cs, Metric m, int h) {
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            d = m.measure(c, d);
            if (d.height < h) {
                d.height = h;
                m.setMeasure(c, d);
            }
        }
    }

    public static void ensureSize(Collection cs, Metric m, Dimension size) {
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            d = m.measure(c, d);
            boolean update = false;

            if (d.width < size.width) {
                d.width = size.width;
                update = true;
            }
            if (d.height < size.height) {
                d.height = size.height;
                update = true;
            }

            if (update) {
                m.setMeasure(c, d);
            }
        }
    }

    public static void restrictWidth(Collection cs, Metric m, int w) {
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            d = m.measure(c, d);
            if (d.width > w) {
                d.width = w;
                m.setMeasure(c, d);
            }
        }
    }

    public static void restrictHeight(Collection cs, Metric m, int h) {
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            d = m.measure(c, d);
            if (d.height > h) {
                d.height = h;
                m.setMeasure(c, d);
            }
        }
    }

    public static void restrictSize(Collection cs, Metric m, Dimension size) {
        Dimension d = new Dimension();

        for (Iterator i = cs.iterator(); i.hasNext();) {
            Component c = (Component)i.next();
            d = m.measure(c, d);
            boolean update = false;

            if (d.width > size.width) {
                d.width = size.width;
                update = true;
            }
            if (d.height > size.height) {
                d.height = size.height;
                update = true;
            }

            if (update) {
                m.setMeasure(c, d);
            }
        }
    }

    public static void enforceAspectRatio(Component comp, int rx, int ry) {
        enforceAspectRatio(comp, null, rx, ry);
    }

    /**
     * Resizes a component to a given x/y aspect ratio, while preserving its
     * area, as determined by a given metric.
     *
     * @param comp
     *            the component to be resized
     * @param m
     *            the metric to be used. If null is provided,
     *            {@link #METRIC_ACTUAL} will be used.
     * @param rx
     *            the numerator of the x/y aspect ratio
     * @param ry
     *            the denominator of the x/y aspect ratio
     */
    public static void enforceAspectRatio(Component comp, Metric m, int rx,
            int ry) {
        if (rx <= 0) {
            throw new IllegalArgumentException("" + rx);
        }
        if (ry <= 0) {
            throw new IllegalArgumentException("" + ry);
        }
        if (m == null) {
            m = METRIC_ACTUAL;
        }

        Dimension d = m.measure(comp);
        int area = d.width * d.height;

        double sx = Math.sqrt(area * (double)rx / ry);
        d.width = (int)sx;
        d.height = (int)(sx * ry / rx);

        m.setMeasure(comp, d);
    }

    public static boolean verifyInput(Component c) {
        return verifyInput(c, false);
    }

    /**
     * Verifies all components contained in the given component. A component is
     * verified iff
     * <ol>
     * <li value="1">It is an instance of <code>javax.swing.JComponent</code>
     * <li value="2">It has an associated {@link JComponent#getInputVerifier()}
     * input verifier.
     * </ol>
     *
     * @param c
     *            the component to be verified
     * @param requireFocus
     *            if true, the component will only be verified if it has focus.
     * @return true iff the component, and every contained component, were
     *         verified
     */
    public static boolean verifyInput(Component c, boolean requireFocus) {
        if (c == null || !c.isVisible() || !(c instanceof Container)) {
            return true;
        }

        if ((requireFocus ? c.isFocusOwner() : true)
                && c instanceof JComponent) {
            // ensure that all contents are verified without focus restraints
            requireFocus = false;

            JComponent jc = (JComponent)c;

            InputVerifier iv = jc.getInputVerifier();
            if (iv != null && !iv.verify(jc)) {
                jc.requestFocus();
                LookAndFeel lnf = UIManager.getLookAndFeel();
                if (lnf != null) {
                    lnf.provideErrorFeedback(c);
                }
                return false;
            }
        }

        Container ct = (Container)c;
        int cc = ct.getComponentCount();

        for (int i = 0; i < cc; i++) {
            if (!verifyInput(ct.getComponent(i), requireFocus)) {
                return false;
            }
        }
        return true;
    }

    public interface Metric {
        public Dimension measure(Component c);

        public Dimension measure(Component c, Dimension d);

        public void setMeasure(Component c, Dimension d);
    }

}
