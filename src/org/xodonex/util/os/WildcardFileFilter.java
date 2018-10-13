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
import java.io.FilenameFilter;
import java.io.Serializable;
import java.util.regex.Pattern;

public class WildcardFileFilter
        implements FilenameFilter, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    public final static char MATCH_ANY = '*';
    public final static char MATCH_ONE = '?';

    public final static Pattern ACCEPT_ALL = Pattern.compile(".*");

    private boolean caseSensitive = true;
    private boolean allowFiles = true;
    private boolean allowHidden = true;
    private boolean allowDirectory = true;

    private Pattern pattern;

    public WildcardFileFilter() {
        this(null);
    }

    public WildcardFileFilter(String matchString) {
        this(matchString, true);
    }

    public WildcardFileFilter(String matchString, boolean caseSensitive) {
        pattern = matchString == null ? ACCEPT_ALL
                : createPattern(matchString,
                        this.caseSensitive = caseSensitive);
    }

    public WildcardFileFilter(String name, Pattern match) {
        if (name == null || match == null) {
            throw new NullPointerException();
        }
        this.pattern = match;
    }

    public static Pattern createPattern(String simpleWildcardString,
            boolean caseSensitive) {
        return Pattern.compile(createRE(simpleWildcardString),
                caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
    }

    public static String createRE(String simpleWildcardString) {
        int l = simpleWildcardString.length();
        StringBuffer buf = new StringBuffer(l);

        for (int i = 0; i < l; i++) {
            char c = simpleWildcardString.charAt(i);
            switch (c) {
            case MATCH_ONE:
                buf.append(".?");
                break;
            case MATCH_ANY:
                buf.append(".*");
                break;
            case '.':
            case '[':
            case ']':
            case '\\':
            case '+':
            case '{':
            case '}':
            case '$':
            case '^':
            case '|':
            case '(':
            case ')':
                buf.append('\\');
                // fall through
            default:
                buf.append(c);
            }
        }

        return buf.toString();
    }

    private boolean acceptFile(File f) {
        return ((allowFiles && f.isFile()) || (allowHidden && f.isHidden())
                || (allowDirectory && f.isDirectory()));
    }

    public boolean accept(File f) throws SecurityException {
        if (acceptFile(f)) {
            return accept(f.getName());
        }
        else {
            return false;
        }
    }

    public boolean accept(String name, File f) throws SecurityException {
        if (acceptFile(f)) {
            return accept(name);
        }
        else {
            return false;
        }
    }

    @Override
    public boolean accept(File p1, String p2) throws SecurityException {
        if (allowFiles && allowDirectory && allowHidden) {
            return accept(p2);
        }
        else {
            File f = new File(p1, p2);
            if (acceptFile(f)) {
                return accept(p2);
            }
            else {
                return false;
            }
        }
    }

    public boolean accept(String name) {
        return pattern.matcher(name).matches();
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return pattern.pattern();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WildcardFileFilter)) {
            return false;
        }

        WildcardFileFilter w = (WildcardFileFilter)o;
        return (caseSensitive == w.caseSensitive) &&
                (allowDirectory == w.allowDirectory) &&
                (allowFiles == w.allowFiles) &&
                (allowHidden == w.allowHidden) &&
                (pattern.pattern().equals(w.pattern.pattern()));
    }

    public boolean getFilesAllowed() {
        return allowFiles;
    }

    public void setFilesAllowed(boolean b) {
        allowFiles = b;
    }

    public boolean getDirsAllowed() {
        return allowDirectory;
    }

    public void setDirsAllowed(boolean b) {
        allowDirectory = b;
    }

    public boolean getHiddenAllowed() {
        return allowHidden;
    }

    public void setHiddenAllowed(boolean b) {
        allowHidden = b;
    }

    public boolean getCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean b) {
        caseSensitive = b;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern p) {
        pattern = p == null ? ACCEPT_ALL : p;
    }

}
