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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.xodonex.util.ui.layout.RowLayout;

/**
 * The StatusBar is a horizontal row of labels, whose contents can be changed.
 */
public class StatusBar extends JPanel {

    private static final long serialVersionUID = 1L;

    // the labels
    private JLabel[] _labels;

    /**
     * Creates a new status bar containing one label and having an unlimited
     * vertical size.
     */
    public StatusBar() {
        this(null, 32767, null, null, null);
    }

    /**
     * Creates a new status bar.
     *
     * @param sizeDefs
     *            defines the size of each component of the status bar. The
     *            values are interpreted as follows:
     *            <ul>
     *            <li>The value 0 indicates that the component will receive its
     *            preferred size, and that no border will be used (this is
     *            suitable for icons).</li>
     *            <li>A positive value, say n, indicates that the component
     *            should have a fixed size of n units, the unit size being the
     *            dimensions of the letter 'M' in the chosen font. The component
     *            will receive a border (this is suitable for texts of a
     *            limited, fixed length)</li>
     *            <li>A negative value indicates that the component is willing
     *            to be enlarged infinitely, while the negated value is used as
     *            above (this is suitable for longer texts or texts of unknown
     *            length)</li>
     *            </ul>
     * @param maxHeight
     *            (optional) a limit on the maximal height of the whole status
     *            bar.
     * @param font
     *            (optional) the font to be used for the components' texts
     * @param foreground
     *            (optional) the foreground color for the texts
     * @param background
     *            (optional the background color for the components
     */
    public StatusBar(int[] sizeDefs, int maxHeight, Font font,
            Color foreground, Color background) {
        super();

        // validate the arguments
        if (sizeDefs == null) {
            sizeDefs = new int[] { -1 };
        }

        // determine the dimensions of the letter 'M' in the specified font.
        Border bdr = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        Border bdr2 = BorderFactory.createEmptyBorder(0, 2, 0, 2);

        if (maxHeight < 0) {
            maxHeight = 32767;
        }

        // use a row layout having no gap, using vertical centering and having
        // a mimimum height suitable for a label showing the letter 'M'
        JLabel l = new JLabel("M");
        l.validate();
        Dimension d = l.getPreferredSize();
        Insets insets = bdr.getBorderInsets(l);
        int h = d.height + insets.top + insets.bottom;
        int em = d.width;
        setLayout(new RowLayout(0, RowLayout.CENTER, h, maxHeight));

        _labels = new JLabel[sizeDefs.length];

        for (int i = 0; i < sizeDefs.length; i++) {
            int s = sizeDefs[i];
            _labels[i] = l = new JLabel();

            if (font != null) {
                l.setFont(font);
            }
            if (foreground != null) {
                l.setForeground(foreground);
            }
            if (background != null) {
                l.setBackground(background);
            }

            if (s == 0) {
                // add the label without a border
                l.setBorder(bdr2);
                add(l);
            }
            else {
                // set the maximum size, if necessary
                if (s < 0) {
                    l.setMaximumSize(new Dimension(32767, h));
                    s = -s;
                }

                // set the minimum size
                l.setMinimumSize(new Dimension(s * em, h));

                // add the label including a border
                l.setBorder(bdr);
                add(l);
            }
        } // for
    }

    public void setIcon(int idx, Icon icon, String tooltip) {
        JLabel l = _labels[idx];
        l.setText(null);
        l.setIcon(icon);
        l.setToolTipText(tooltip);
    }

    public void addIcon(int idx, Icon icon, String tooltip) {
        JLabel l = _labels[idx];
        l.setIcon(icon);
        l.setToolTipText(tooltip);
    }

    public Icon getIcon(int idx) {
        return _labels[idx].getIcon();
    }

    public String getTooltip(int idx) {
        return _labels[idx].getToolTipText();
    }

    public void setText(int idx, String text) {
        JLabel l = _labels[idx];
        l.setIcon(null);
        l.setToolTipText(null);
        l.setText(text);
    }

    public void addText(int idx, String text) {
        _labels[idx].setText(text);
    }

    public String getText(int idx) {
        return _labels[idx].getText();
    }

}
