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
package org.xodonex.hojo.lib;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.xodonex.hojo.StandardFunction;

public final class CaptureFunction extends StandardFunction {

    private static final long serialVersionUID = 1L;

    private final static Class[] pTypes = new Class[] {
            Component.class, Rectangle.class };

    private final static CaptureFunction instance = new CaptureFunction();

    private CaptureFunction() {
    }

    public static CaptureFunction getInstance() {
        return instance;
    }

    @Override
    public Class[] getParameterTypes() {
        return pTypes;
    }

    @Override
    public String[] getParameterNames() {
        return new String[] { "comp", "area" };
    }

    @Override
    public Class getReturnType() {
        return BufferedImage.class;
    }

    @Override
    public Object getDefaultValue(int arg) {
        switch (arg) {
        case 1:
            return null;
        default:
            return NO_ARG;
        }
    }

    @Override
    public Object invoke(Object[] args) {
        Component c = (Component)args[0];
        Dimension d = c.getPreferredSize();
        Rectangle cBounds = new Rectangle(0, 0, d.width, d.height);
        Rectangle view = args[1] == null ? cBounds
                : cBounds.intersection((Rectangle)args[1]);

        if (cBounds.width <= 0 || cBounds.height <= 0) {
            return null;
        }

        BufferedImage img = new BufferedImage(cBounds.width, cBounds.height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.setClip(cBounds.x, cBounds.y, cBounds.width, cBounds.height);

        c.paint(g);
        return cBounds.equals(view) ? img
                : img.getSubimage(view.x, view.y, view.width, view.height);
    }

}
