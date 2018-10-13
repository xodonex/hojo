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
package org.xodonex.util.print;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Collection;

/**
 * The PageRenderer is a Printable which prints itself from a series of
 * commands. The supported commands are the following:
 * <ul>
 * <li>A Font instance sets the current font.
 * <li>A Color instance changes the current color.
 * <li>A Shape instance is drawn
 * <li>An Object[] instance containing two elements specifies the location and
 * contents of a paragraph or single line of text. The location should be a
 * Point instance for a single line (which is a String instance), and a
 * Rectangle instance for a paragraph (which is a String[] instance).
 * </ul>
 */
public class PageRenderer implements Printable {

    public static final int ALIGN_TOP = 0;
    public static final int ALIGN_BOTTOM = 1;

    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;

    public static final int ALIGN_CENTER = 2;

    // contains the relevant bits from the ALIGN_xxx constants
    private final static int ALIGN_MASK = 3;

    /**
     * Convert millimeters to points.
     *
     * @param mm
     *            the number of millimeters to be converted
     * @return the corresponding number of points
     */
    public static double mm2point(double mm) {
        return mm * (72.0 / 25.4);
    }

    /**
     * Convert points to millimeters
     *
     * @param point
     *            the number of points to be converted
     * @return the corresponding number of millimeters
     */
    public static double point2mm(double point) {
        return point * (25.4 / 72.0);
    }

    /**
     * Convert millimeters to inches.
     *
     * @param mm
     *            the number of millimeters to be converted
     * @return the corresponding number of inches
     */
    public static double mm2inch(double mm) {
        return mm / 25.4;
    }

    /**
     * Convert inches to millimeters.
     *
     * @param inch
     *            the number of inches to be converted
     * @return the corresponding number of inches
     */
    public static double inch2mm(double inch) {
        return inch * 25.4;
    }

    /**
     * Convert points to inches.
     *
     * @param point
     *            the number of points to be converted
     * @return the corresponding number of inches
     */
    public static double point2inch(double point) {
        return point / 72.0;
    }

    /**
     * Convert inches to points.
     *
     * @param inch
     *            the number of inches to be converted
     * @return the corresponding number of points
     */
    public static double inch2point(double inch) {
        return inch * 72.0;
    }

    /**
     * Print a paragraph of texts on a graphics.
     *
     * @param g
     *            the graphics device used for printing
     * @param texts
     *            the paragraph of text, each element containing a single line
     * @param bounds
     *            the bounds of the placement of the paragraph, in user space
     *            coordinats
     * @param halign
     *            the horzontal alignment of the text
     * @param valign
     *            the vertical alignment of the text
     */
    public static void printText(Graphics2D g, String[] texts,
            Rectangle2D bounds, int halign, int valign) {

        // calculate the dimensions of each text, and save the largest width
        // and the total height
        TextLayout[] tls = new TextLayout[texts.length];
        Rectangle2D[] bs = new Rectangle2D[texts.length];

        double wMax = 0.0;
        double h = 0.0;

        Font f = g.getFont();
        FontRenderContext frc = g.getFontRenderContext();

        for (int i = 0; i < texts.length; i++) {
            String s = texts[i];
            if (s == null || s.length() == 0) {
                s = " ";
            }
            TextLayout tl = tls[i] = new TextLayout(texts[i], f, frc);
            Rectangle2D b = bs[i] = tl.getBounds();
            b.add(b.getX(), b.getY() + b.getHeight() + 4);

            h += b.getHeight();
            wMax = max(wMax, b.getWidth());
        }

        // calculate the y coordinate at which to place the top of the
        // first text, and print the texts from there
        double startY;
        switch (valign & ALIGN_MASK) {
        case ALIGN_CENTER:
            startY = bounds.getY() + bounds.getHeight() / 2 - h / 2;
            break;
        case ALIGN_BOTTOM:
            startY = bounds.getY() + bounds.getHeight() - h;
            break;
        default: // ALIGN_TOP:
            startY = bounds.getY();
            break;
        }

        double l = bounds.getX();
        double c = l + bounds.getWidth() / 2;
        double r = l + bounds.getWidth();

        for (int i = 0; i < texts.length; i++) {
            TextLayout tl = tls[i];
            Rectangle2D b = bs[i];

            double y = startY + b.getHeight();
            double x;
            switch (halign & ALIGN_MASK) {
            case ALIGN_CENTER:
                x = c - b.getWidth() / 2;
                break;
            case ALIGN_RIGHT:
                x = r - b.getWidth();
                break;
            default: // ALIGN_LEFT:
                x = l;
            }

            tl.draw(g, (float)x, (float)y);
            startY = y;
        }
    }

    private Object[] _cmds;

    public PageRenderer(Collection cmds) {
        this(cmds.toArray());
    }

    public PageRenderer(Object[] cmds) {
        _cmds = cmds;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int page)
            throws PrinterException {
        if (!(graphics instanceof Graphics2D)) {
            throw new PrinterException(graphics.getClass().getName());
        }
        if (page != 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g = (Graphics2D)graphics;

        for (int i = 0; i < _cmds.length; i++) {
            Object obj = _cmds[i];

            if (obj instanceof Font) {
                g.setFont((Font)obj);
            }
            else if (obj instanceof Color) {
                g.setColor((Color)obj);
            }
            else if (obj instanceof Shape) {
                g.draw((Shape)obj);
            }
            else if (obj instanceof Object[]) {
                Object[] cmd = (Object[])obj;
                if (cmd.length < 2) {
                    continue;
                }

                if (cmd[0] instanceof Point2D) {
                    Point2D p = (Point2D)cmd[0];
                    if (cmd[1] instanceof String[]) {
                        Rectangle2D bounds = new Rectangle2D.Double(
                                p.getX(), p.getY(), 1, 1);
                        printText(g, (String[])cmd[1], bounds, ALIGN_LEFT,
                                ALIGN_TOP);
                    }
                    else {
                        g.drawString("" + cmd[1], (float)p.getX(),
                                (float)p.getY());
                    }
                }
                else {
                    Rectangle2D bounds = (Rectangle2D)cmd[0];
                    int halign, valign;
                    String[] par;
                    if (cmd.length < 4) {
                        halign = ALIGN_LEFT;
                        valign = ALIGN_TOP;
                        par = (String[])cmd[1];
                    }
                    else {
                        halign = ((Integer)cmd[1]).intValue();
                        valign = ((Integer)cmd[2]).intValue();
                        par = (String[])cmd[3];
                    }
                    printText(g, par, bounds, halign, valign);
                }
            }
            // else do nothing
        }

        return PAGE_EXISTS;
    }

    private static double max(double d1, double d2) {
        return d1 > d2 ? d1 : d2;
    }

}
