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
package org.xodonex.util.text.lexer;

import java.net.URL;

/**
 * A Location contains a line number and code excerpt at a given input location.
 */
public class Location implements java.io.Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    public final static String START_MARKER = "! ";
    public final static char ERROR_MARKER = '^';

    protected URL location;
    protected int lineNumber;
    protected String input;
    protected int[] pos;
    protected String startMarker = START_MARKER;
    protected char errMarker = ERROR_MARKER;

    public Location(URL location, int lineNumber, String input, int[] pos) {
        this.location = location;
        this.lineNumber = lineNumber;
        this.input = input;
        this.pos = pos;
    }

    public String printLocation(String emptyLoc) {
        StringBuffer result = new StringBuffer(startMarker);
        result.append(
                (location == null) ? (Object)emptyLoc : (Object)location);
        result.append('(').append(lineNumber).append("):");
        return result.toString();
    }

    public String printInput() {
        return startMarker + input;
    }

    public char[] printMarker() {
        char[] marker = startMarker.toCharArray();
        char[] result = new char[marker.length + input.length()];
        int i;

        for (i = 0; i < marker.length; i++) {
            result[i] = marker[i];
        }

        i = 0;
        for (int j = marker.length; j < result.length; i++) {
            if ((i >= pos[0]) && (i <= pos[1])) {
                result[j++] = errMarker;
            }
            else {
                result[j++] = ' ';
            }
        }

        return result;
    }

    public String printError(Throwable t) {
        return toString() + "\n" + startMarker + t;
    }

    @Override
    public String toString() {
        return printLocation("<main input>") + "\n" +
                printInput() + "\n" + new String(printMarker());
    }

    @Override
    public Object clone() {
        try {
            Location result = (Location)super.clone();
            result.pos = pos.clone();
            return result;
        }
        catch (CloneNotSupportedException e) {
            throw new RuntimeException();
        }
    }
}
