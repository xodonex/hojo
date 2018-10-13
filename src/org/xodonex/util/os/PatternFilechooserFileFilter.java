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
package org.xodonex.util.os;

import java.io.File;
import java.io.Serializable;
import java.util.regex.Pattern;

public class PatternFilechooserFileFilter
        extends javax.swing.filechooser.FileFilter
        implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    public final static int DIR_ACCEPT = 0;
    public final static int DIR_REJECT = -1;
    public final static int DIR_MATCH = 1;

    private String _description;
    private Pattern _pattern;
    private int _dirMatchMode;

    public PatternFilechooserFileFilter(String description, Pattern pattern) {
        this(description, pattern, DIR_ACCEPT);
    }

    public PatternFilechooserFileFilter(String description, Pattern pattern,
            int dirMatchMode) {
        super();
        if (description == null || pattern == null) {
            throw new NullPointerException();
        }
        _description = description;
        _pattern = pattern;
        _dirMatchMode = dirMatchMode;
    }

    @Override
    public boolean accept(File f) {
        if (_dirMatchMode != DIR_MATCH && f.isDirectory()) {
            return (_dirMatchMode == DIR_ACCEPT);
        }
        return _pattern.matcher(f.toString()).matches();
    }

    @Override
    public String getDescription() {
        return _description;
    }

}
