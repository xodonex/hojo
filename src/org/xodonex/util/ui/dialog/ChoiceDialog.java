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
package org.xodonex.util.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ui.GuiResource;

public class ChoiceDialog extends BasicDialog {

    private static final long serialVersionUID = 1L;

    private JButton[] _buttons;
    private JCheckBox[] _checkBoxes;
    private JPanel _choicePanel;
    private JScrollPane _scroll;

    /*
     * -----------------------------------------------------------------------
     * Constructors
     * -----------------------------------------------------------------------
     */

    public ChoiceDialog() {
        this((JFrame)null, null);
    }

    public ChoiceDialog(GuiResource rsrc) {
        super(rsrc);
    }

    public ChoiceDialog(JDialog owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    public ChoiceDialog(JFrame owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    @Override
    protected void setup() {
        super.setup();

        _buttons = new JButton[] {
                _guiResource.createButton("btn.Ok"),
                _guiResource.createButton("btn.Cancel"),
                _guiResource.createButton("btn.SelectAll"),
                _guiResource.createButton("btn.DeselectAll"),
        };

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == _buttons[0]) {
                    close(null);
                }
                else {
                    cancel();
                }
            }
        };
        _buttons[0].addActionListener(listener);
        _buttons[1].addActionListener(listener);
        _buttons[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = _checkBoxes.length - 1; i >= 0;) {
                    _checkBoxes[i--].setSelected(true);
                }
            }
        });
        _buttons[3].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = _checkBoxes.length - 1; i >= 0;) {
                    _checkBoxes[i--].setSelected(false);
                }
            }
        });

        _choicePanel = new JPanel();
        _scroll = new JScrollPane(_choicePanel);
        getContentPane().add(_scroll, BorderLayout.CENTER);
        addButtons(_buttons, null, 0);
    }

    /*
     * -----------------------------------------------------------------------
     * New methods
     * -----------------------------------------------------------------------
     */

    public Object choose(String title, Collection c)
            throws IllegalStateException {
        return choose(title, c, null, null);
    }

    public Object choose(String title, Collection c, boolean[] selections)
            throws IllegalStateException {
        return choose(title, c, selections, null);
    }

    public Object choose(String title, Collection c, boolean[] selections,
            String[] titles)
            throws IllegalStateException, IllegalArgumentException {

        Object[] choices = c.toArray();
        if ((selections != null) && (choices.length != selections.length)) {
            throw new IllegalArgumentException();
        }
        if ((titles != null) && (choices.length != titles.length)) {
            throw new IllegalArgumentException();
        }

        setTitle(title);

        _checkBoxes = new JCheckBox[choices.length];

        // create the choice titles
        if (titles == null) {
            titles = new String[choices.length];
            for (int i = 0; i < choices.length; i++) {
                titles[i] = ConvertUtils.toString(choices[i]);
            }
        }

        // set the default selection
        if (selections == null) {
            for (int i = 0; i < choices.length; i++) {
                _choicePanel
                        .add(_checkBoxes[i] = new JCheckBox(titles[i], true));
            }
        }
        else {
            for (int i = 0; i < choices.length; i++) {
                _choicePanel.add(_checkBoxes[i] = new JCheckBox(titles[i],
                        selections[i]));
            }
        }

        // Force a layout having an aspect ratio of 4:3
        _choicePanel.doLayout();
        Dimension d = _choicePanel.getPreferredSize();
        Dimension d2 = _buttonBox.getPreferredSize();
        Insets isets = _choicePanel.getInsets();
        int h = isets.left + isets.right;
        int v = isets.top + isets.bottom;
        int rowSize = d.height - v;
        double ar = (d.width - h) * (d.height - v);
        double x = Math.sqrt((4.0 / 3.0) * ar);

        if ((int)x + h < d2.width) {
            // the buttons use up more space - set the width to the width
            // of the buttons
            d.width = d2.width;
            d.height = (int)(ar / (d2.width - h));
        }
        else {
            // set the preferred 4:3 aspect ratio
            d.width = (int)x;
            d.height = (int)(0.75 * x);
        }

        if ((h = d.height % rowSize) > 0) {
            // ensure that a whole number of rows can be seen
            d.height += rowSize - h;
        }
        // add the vertical border size
        d.height += v;

        _choicePanel.setPreferredSize(d);

        // show the dialog
        xeq();

        _choicePanel.removeAll();
        if (isCancelled()) {
            // canceled - do nothing
            _checkBoxes = null;
            return null;
        }
        else {
            // remove all unselected choices from the collection
            Iterator it = c.iterator();
            for (int i = 0; i < choices.length; i++) {
                it.next();
                if (!_checkBoxes[i].isSelected()) {
                    it.remove();
                }
            }

            // return the updated selection, if one was given
            if (selections != null) {
                for (int i = selections.length - 1; i >= 0;) {
                    selections[i] = _checkBoxes[i--].isSelected();
                }
                _checkBoxes = null;
                return selections;
            }
            else {
                // return the update collection
                _checkBoxes = null;
                return c;
            }
        }
    }

}
