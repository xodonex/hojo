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
package org.xodonex.util.ui.edit;

import java.beans.PropertyChangeListener;

/**
 * A component for editing.
 */
public interface EditorComponent extends PropertyChangeListener {

    /**
     * @return the editor used to hold and verify values
     */
    public DataEditor getEditor();

    /**
     * Updates the state of the UI to hold the value stored in the editor
     */
    public void updateFromEditor();

    /**
     * Tries to update the state of the editor to match the state of the UI
     *
     * @return true iff the update was successful
     */
    public boolean updateFromUI();

    /**
     * @return the component used to represent the editor UI
     */
    public javax.swing.JComponent getUI();

    /**
     * @return the editor UI
     */
    public javax.swing.JComponent getEditorUI();

    /**
     * Toggles the enabled state of the editor UI
     *
     * @param enabled
     *            whether the editor should be enabled.
     */
    public void enableUI(boolean enabled);

    /**
     * Retrieves the label of the editor UI
     *
     * @return the label used, or null if no label was used
     */
    public javax.swing.JLabel getLabel();

}
