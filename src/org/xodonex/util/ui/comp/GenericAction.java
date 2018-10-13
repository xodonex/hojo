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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * A GenericAction allows the performed action to be defined at run time.
 *
 * @author Henrik Lauritzen
 */
public class GenericAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    /**
     * The key which is used for storing a String ID for this action.
     */
    public final static String ID = "ID";

    /**
     * The key which is used for storing a Runnable defining the operation of
     * this action.
     */
    public static final String ACTION = "Action";

    public GenericAction() {
        super();
    }

    public GenericAction(Runnable r) {
        super();
        putValue(ACTION, r);
    }

    public GenericAction(String id, Runnable r) {
        super();
        putValue(ID, id);
        putValue(ACTION, r);
    }

    /**
     * Executes the assigned action, if any.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Runnable action = (Runnable)getValue(ACTION);
        if (action != null) {
            action.run();
        }
    }

}
