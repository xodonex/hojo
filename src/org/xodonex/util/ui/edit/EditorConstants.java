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

/**
 * @author Henrik Lauritzen
 */
public interface EditorConstants {

    /**
     * Indiciates that the label should be placed above the control
     */
    public int LABEL_NORTH = 0;

    /**
     * Indiciates that the label should be placed to the left of the control
     */
    public int LABEL_WEST = 1;

    /**
     * Indiciates that the label should be placed to the south of the control
     */
    public int LABEL_SOUTH = 2;

    /**
     * Indiciates that the label should be placed to the right of the control
     */
    public int LABEL_EAST = 3;

    /**
     * Mask value for the LABEL_xxx constants
     */
    public int MASK_LABEL = 3;

    /**
     * Indicates an enabled component
     */
    public int STATE_ENABLED = 0;

    /**
     * Indicates a disabled component
     */
    public int STATE_DISABLED = 4;

    /**
     * Mask value for the STATE_xxx constants
     */
    public int MASK_STATE = STATE_DISABLED;

    /**
     * Indicates a check box editor
     */
    public int EDITOR_CHECK_BOX = 65536 * 1;

    /**
     * Indicates a combo box editor
     */
    public int EDITOR_COMBO_BOX = 65536 * 2;

    /**
     * Indicates an editable combo box editor
     */
    public int EDITOR_COMBO_BOX_EDITABLE = 65536 * 3;

    /**
     * Indicates a text area editor
     */
    public int EDITOR_TEXT_AREA = 65536 * 4;

    /**
     * Indicates a text field editor
     */
    public int EDITOR_TEXT_FIELD = 65536 * 5;

    /**
     * Indicates a password field editor
     */
    public int EDITOR_PASSWORD_FIELD = 65536 * 6;

    /**
     * Indicates a list data editor
     */
    public int EDITOR_LIST = 65536 * 7;

    /**
     * Indicates a multi-selction list data editor
     */
    public int EDITOR_LIST_MULTIPLE = 65536 * 8;

    /**
     * Mask value to obtain one of the LABEL_xxx values
     */
    public int MASK_EDITOR = 0xffff0000;

}
