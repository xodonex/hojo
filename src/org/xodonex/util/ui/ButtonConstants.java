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

/**
 *
 * @author Henrik Lauritzen
 */
public interface ButtonConstants {

    /**
     * internal
     */
    public int BTN_FAC = 10;

    /**
     * Indicator value for the 'Close' button.
     */
    public int BTN_CLOSE = BTN_FAC;

    /**
     * Indicator value for the 'Cancel' button.
     */
    public int BTN_CANCEL = BTN_CLOSE + BTN_FAC;

    /**
     * Indicator value for the 'Ok' button.
     */
    public int BTN_OK = BTN_CANCEL + BTN_FAC;

    /**
     * Indicator value for the 'No' button.
     */
    public int BTN_NO = BTN_OK + BTN_FAC;

    /**
     * Indicator value for the 'Yes' button.
     */
    public int BTN_YES = BTN_NO + BTN_FAC;

    /**
     * Indicator value for the 'Yes to All' button.
     */
    public int BTN_ALL = BTN_YES + BTN_FAC;

    /**
     * Indicator value for a custom button
     */
    public int BTN_CUSTOM = (BTN_FAC - 1) * BTN_FAC;

    /**
     * Indicator value for the 'Ok' and 'Cancel' buttons.
     */
    public int BTNS_OK_CANCEL = BTN_FAC * BTN_OK + BTN_CANCEL;

    /**
     * Indicator value for the 'Yes' and 'No' buttons.
     */
    public int BTNS_YES_NO = BTN_FAC * BTN_YES + BTN_NO;

    /**
     * Indicator value for the 'Yes', 'No' and 'Cancel' buttons.
     */
    public int BTNS_YES_NO_CANCEL = BTNS_YES_NO * BTN_FAC + BTN_CANCEL;

    /**
     * Indicator value for the 'Yes', 'Yes to All', 'No' and 'Cancel' buttons.
     */
    public int BTNS_YES_ALL_NO_CANCEL = ((BTN_FAC * BTN_YES + BTN_ALL) * BTN_FAC
            + BTN_NO) * BTN_FAC + BTN_CANCEL;

}
