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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

public class AutoScrollPane extends JScrollPane implements ActionListener {

    private static final long serialVersionUID = 1L;

    public AutoScrollPane() {
        super();
    }

    public AutoScrollPane(Component view) {
        super(view);
    }

    public AutoScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
    }

    public AutoScrollPane(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        scrollToMax((Component)e.getSource());
    }

    public void scrollToMax(Component c) {
        JScrollBar vert = getVerticalScrollBar();
        c.validate();
        vert.getParent().validate();

        int pos = c.getMinimumSize().height - vert.getHeight();
        int min = vert.getMinimum();
        int max = vert.getMaximum();
        vert.setValue((pos < min) ? min : ((pos > max) ? max : pos));

    }

}
