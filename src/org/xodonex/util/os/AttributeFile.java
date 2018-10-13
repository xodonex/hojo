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
import java.text.DateFormat;
import java.util.Date;

import org.xodonex.util.StringUtils;

/**
 * NOT SYNCHRONIZED
 */

public class AttributeFile extends File {

    private static final long serialVersionUID = 1L;

    public final static int WIDTH_ATTRIBS = 5;
    public final static int WIDTH_SIZE = 15;
    public final static int WIDTH_DATETIME = 30;

    private final static int ATTRIB_SYS = 0;
    private final static int ATTRIB_DIR = 1;
    private final static int ATTRIB_FILE = 2;
    private final static int ATTRIB_READ = 3;
    private final static int ATTRIB_WRITE = 4;
    private final static int ATTRIB_HIDDEN = 5;

    private final static int ATTRIB_COUNT = 6;

    private final static char CHAR_NOATTRIB = '-';
    // private final static String SIZE_NONE = "<N/A>";
    // private final static String SIZE_DIR = "<DIR>";
    // private final static String SIZE_SYS = "<SYS>";

    private final static char[] attribChars = { 's', 'd', CHAR_NOATTRIB, 'r',
            'w', 'h' };

    /*
     * private long timestamp; private long length; private boolean exists;
     * private boolean sysfile;
     */
    private String printName;
    private boolean[] attribs = new boolean[ATTRIB_COUNT];

    public AttributeFile(File parent, String child) {
        super(parent, child);
        printName = child;
        update();
    }

    public AttributeFile(String pathname) {
        super(pathname);
        printName = pathname;
        update();
    }

    public AttributeFile(String parent, String child) {
        super(parent, child);
        printName = child;
        update();
    }

    public AttributeFile(File parent, String child, String printName) {
        super(parent, child);
        this.printName = printName;
        update();
    }

    /*
     * private void invalidate() { exists = false; for (int i = 0; i <
     * ATTRIB_COUNT; ) { attribs[i++] = false; } timestamp = 0L; length = 0L; }
     */

    private void update() {
        if (!super.exists()) {
            for (int i = 0; i < ATTRIB_COUNT; i++) {
                attribs[i] = false;
            }
        }
        else {
            attribs[ATTRIB_DIR] = super.isDirectory();
            attribs[ATTRIB_FILE] = super.isFile();
            attribs[ATTRIB_SYS] = !(attribs[ATTRIB_DIR]
                    || attribs[ATTRIB_FILE]);
            attribs[ATTRIB_READ] = super.canRead();
            attribs[ATTRIB_WRITE] = super.canWrite();
            attribs[ATTRIB_HIDDEN] = super.isHidden();
        }
    }

    /*
     * public boolean isDirectory() { return attribs[ATTRIB_DIR]; }
     *
     * public boolean isFile() { return attribs[ATTRIB_FILE]; }
     *
     * public boolean canRead() { return attribs[ATTRIB_READ]; }
     *
     * public boolean canWrite() { return attribs[ATTRIB_WRITE]; }
     *
     * public boolean isHidden() { return attribs[ATTRIB_HIDDEN]; }
     *
     * public long length() { return length; }
     *
     * public long lastModified() { return timestamp; }
     *
     * public boolean createNewFile() throws IOException, SecurityException { if
     * (super.createNewFile()) { update(); return true; } else { return false; }
     * }
     *
     * public boolean delete() throws SecurityException { if (super.delete()) {
     * invalidate(); return true; } else { return false; } }
     *
     * public boolean renameTo(File dest) throws SecurityException { if
     * (super.renameTo(dest)) { update(); return true; } else { return false; }
     * }
     *
     * public boolean setLastModified(long time) throws
     * IllegalArgumentException, SecurityException { if
     * (super.setLastModified(time)) { timestamp = time; return true; } else {
     * return false; } }
     *
     */

    public String toOrdinaryString() {
        return super.toString();
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        if (!exists()) {
            for (int i = 1; i < WIDTH_ATTRIBS; i++) {
                result.append(CHAR_NOATTRIB);
            }
            result.append(
                    StringUtils.fill(' ', 3 + WIDTH_SIZE + WIDTH_DATETIME));
            return result.append(printName).toString();
        }

        for (int i = ATTRIB_SYS; i <= ATTRIB_FILE; i++) {
            if (attribs[i]) {
                // exactly one of ATTRIB_SYS, ATTRIB_FILE and ATTRIB_DIR is true
                result.append(attribChars[i]);
                break;
            }
        }
        for (int i = ATTRIB_READ; i < ATTRIB_COUNT; i++) {
            result.append(attribs[i] ? attribChars[i] : CHAR_NOATTRIB);
        }

        if (attribs[ATTRIB_FILE] | attribs[ATTRIB_DIR]) {
            if (attribs[ATTRIB_FILE]) {
                result.append(StringUtils.expandLeft(
                        StringUtils.addSeparators(null,
                                ("" + super.length()).toCharArray(), " ", 3),
                        ' ', WIDTH_SIZE + 1));
            }
            else {
                result.append(StringUtils.expandRight("", ' ', WIDTH_SIZE + 1));
            }
            DateFormat fmt = DateFormat.getDateTimeInstance();

            result.append(' ').append(StringUtils.expandLeft(
                    fmt.format(new Date(super.lastModified())), ' ',
                    WIDTH_DATETIME + 1));
        }
        else {
            result.append(StringUtils.expandRight(" ", ' ',
                    3 + WIDTH_SIZE + WIDTH_DATETIME));
        }

        return result.append(' ').append(printName).toString();
    }

    /*
     * public int compareTo(Object o) throws ClassCastException { return
     * compareTo((AttributeFile)o); }
     *
     * public int compareTo(AttributeFile f) { // Check for different attributes
     * for (int i = ATTRIB_SYS; i < ATTRIB_READ; i++) { if (attribs[i] !=
     * f.attribs[i]) { // SYS < DIR < FILE return (attribs[i] ? -1 : 1); } }
     *
     * int cmp = printName.compareTo(f.printName); return (cmp == 0) ?
     * super.compareTo(f) : cmp; }
     */

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AttributeFile)) {
            return false;
        }

        AttributeFile af = (AttributeFile)o;
        return af.printName.equals(printName) && super.equals(af);
    }

    @Override
    public int hashCode() {
        return super.hashCode() ^ printName.hashCode();
    }
}
