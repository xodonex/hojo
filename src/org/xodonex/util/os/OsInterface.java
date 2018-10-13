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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;
import org.xodonex.util.io.BitBucket;
import org.xodonex.util.io.MonitoredOutputStream;
import org.xodonex.util.struct.ByteBuffer;
import org.xodonex.util.tools.HexEditor;
import org.xodonex.util.ui.ButtonConstants;
import org.xodonex.util.ui.DialogUtils;
import org.xodonex.util.ui.GuiResource;

/**
 * A high-level OS interface, inspired by the basic GNU/Linux shell commands.
 */
public class OsInterface implements Cloneable {

    public final static String NAME = "" + System.getProperty("os.name");
    public final static String ARCH = "" + System.getProperty("os.arch");
    public final static String VERSION = "" + System.getProperty("os.version");
    public final static String DEFAULT_ENCODING;

    public final static File HOME;
    public final static URL HOME_URL;

    private final static File EMPTY = new File("");

    static {
        File f;
        URL u = null;
        try {
            f = new File(System.getProperty("user.dir"));
            try {
                u = f.toURI().toURL();
            }
            catch (Exception e) {
            }
        }
        catch (SecurityException e) {
            f = EMPTY;
        }

        HOME = f;
        HOME_URL = u;

        InputStreamReader r = new InputStreamReader(
                new ByteArrayInputStream(new byte[0]));
        DEFAULT_ENCODING = r.getEncoding();
    }

    public final static PrintWriter SYSOUT = new PrintWriter(System.out) {
        @Override
        public void close() {
            // do nothing
        }

        @Override
        public String toString() {
            return "SYSOUT";
        }
    };

    public final static PrintWriter SYSERR = new PrintWriter(System.err) {
        @Override
        public void close() {
            // do nothing
        }

        @Override
        public String toString() {
            return "SYSERR";
        }
    };

    public final static BufferedReader SYSIN = new BufferedReader(
            new InputStreamReader(System.in)) {
        @Override
        public void close() {
            // do nothing
        }

        @Override
        public String toString() {
            return "SYSIN";
        }
    };

    public final static PrintWriter NULL = new PrintWriter(
            new OutputStreamWriter(new BitBucket())) {
        @Override
        public String toString() {
            return "NULL";
        }
    };

    public final static char CHAR_SWITCH = '-';
    public final static char CHAR_EXT = '.';

    private final static int BUF_SIZE = 16384;
    private final static int SBUF_SIZE = 1024;
    private static Collator collator = Collator.getInstance();

    // switch ID definitions for ls()
    private final static int SW_NAME = 0;
    private final static Integer SW_NAME_ = new Integer(SW_NAME);
    private final static int SW_EXT = 1;
    private final static Integer SW_EXT_ = new Integer(SW_EXT);
    private final static int SW_SIZE = 2;
    private final static Integer SW_SIZE_ = new Integer(SW_SIZE);
    private final static int SW_TIME = 3;
    private final static Integer SW_TIME_ = new Integer(SW_TIME);
    private final static int SW_REVERSE = 4;
    private final static Integer SW_REVERSE_ = new Integer(SW_REVERSE);

    private final static int SW_FILE = 5;
    private final static Integer SW_FILE_ = new Integer(SW_FILE);
    private final static int SW_HIDE = 6;
    private final static Integer SW_HIDE_ = new Integer(SW_HIDE);
    private final static int SW_DIR = 7;
    private final static Integer SW_DIR_ = new Integer(SW_DIR);
    private final static int SW_LONG = 8;
    private final static Integer SW_LONG_ = new Integer(SW_LONG);
    private final static int SW_RECURSIVE = 9;
    private final static Integer SW_RECURSIVE_ = new Integer(SW_RECURSIVE);
    private final static int SW_ABSOLUTE = 10;
    private final static Integer SW_ABSOLUTE_ = new Integer(SW_ABSOLUTE);
    private final static int SW_NOCASE = 11;
    private final static Integer SW_NOCASE_ = new Integer(SW_NOCASE);

    // indices to array arguments for ls0()
    private final static int LSIDX_SW_FILTER = 0;
    private final static int LSIDX_SW_COMP = 1;
    private final static int LSIDX_SW_RECURSIVE = 2;
    private final static int LSIDX_SW_LONG = 3;
    private final static int LSIDX_SW_ABSOLUTE = 4;
    private final static int LSIDX_MAX = 4;

    private abstract static class FileComparator
            implements Cloneable, Comparator {
        protected abstract int comp(File f1, File f2);

        protected abstract boolean eq(File f1, File f2);

        protected abstract int ID();

        protected abstract int REVERSE_ID();

        @Override
        public int compare(Object o1, Object o2) {
            try {
                File f1 = ConvertUtils.toFile(o1);
                File f2 = ConvertUtils.toFile(o2);
                boolean isF1 = f1.isFile();
                boolean isF2 = f2.isFile();
                if (isF1 ^ isF2) {
                    // sort directories and files separately, files last
                    return isF1 ? 1 : -1;
                }
                return comp(f1, f2);
            }
            catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                else {
                    throw new IllegalArgumentException(
                            e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }

        @SuppressWarnings("unused")
        public boolean equals(Object o1, Object o2) {
            try {
                File f1 = ConvertUtils.toFile(o1);
                File f2 = ConvertUtils.toFile(o2);
                return eq(f1, f2);
            }
            catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                else {
                    throw new IllegalArgumentException(
                            e.getClass().getName() + ": " + e.getMessage());
                }
            }
        }

    }

    public final static FileComparator COMP_NAME_ASC = new FileComparator() {
        @Override
        protected int ID() {
            return SW_NAME;
        }

        @Override
        protected int REVERSE_ID() {
            return SW_NAME + SW_REVERSE;
        }

        @Override
        protected int comp(File f1, File f2) {
            return collator.compare(f1.getPath(), f2.getPath());
        }

        @Override
        protected boolean eq(File f1, File f2) {
            return collator.equals(f1.getPath(), f2.getPath());
        }
    };

    public final static FileComparator COMP_NAME_DESC = new FileComparator() {
        @Override
        protected int ID() {
            return SW_NAME + SW_REVERSE;
        }

        @Override
        protected int REVERSE_ID() {
            return SW_NAME;
        }

        @Override
        protected int comp(File f1, File f2) {
            return collator.compare(f1.getPath(), f2.getPath());
        }

        @Override
        protected boolean eq(File f1, File f2) {
            return collator.equals(f1.getPath(), f2.getPath());
        }
    };

    private abstract static class ExtensionComparator extends FileComparator {
        protected String[] split(File f1) {
            String s = f1.getName();
            String[] result = new String[2];
            int splitIdx = s.lastIndexOf(CHAR_EXT);

            switch (splitIdx) {
            case -1:
                result[0] = s;
                result[1] = null;
                return result;
            case 0:
                result[0] = "";
                result[1] = s.substring(1);
                return result;
            default:
                result[0] = s.substring(0, splitIdx);
                result[1] = s.substring(splitIdx + 1);
                return result;
            }
        }

        @Override
        protected int comp(File f1, File f2) {
            String[] split1 = split(f1);
            String[] split2 = split(f2);
            String ext1 = split1[1];
            String ext2 = split2[1];

            if (ext1 == null) {
                // files without extension first
                return (ext2 == null) ? collator.compare(split1[0], split2[0])
                        : -1;
            }
            else if (ext2 == null) {
                // f1 has extension and f2 hasn't.
                return 1;
            }
            else {
                int tmp = collator.compare(ext1, ext2);
                if (tmp != 0) {
                    // different extensions
                    return tmp;
                }
                // compare the prefix names
                return collator.compare(split1[0], split2[0]);
            }
        }

        @Override
        protected boolean eq(File f1, File f2) {
            return collator.equals(f1.getName(), f2.getName());
        }
    }

    public final static FileComparator COMP_EXT_ASC = new ExtensionComparator() {
        @Override
        protected int ID() {
            return SW_EXT;
        }

        @Override
        protected int REVERSE_ID() {
            return SW_EXT + SW_REVERSE;
        }
    };

    public final static FileComparator COMP_EXT_DESC = new ExtensionComparator() {
        @Override
        protected int ID() {
            return SW_EXT + SW_REVERSE;
        }

        @Override
        protected int REVERSE_ID() {
            return SW_EXT;
        }

        @Override
        protected int comp(File f1, File f2) {
            return super.comp(f2, f1);
        }
    };

    public final static FileComparator COMP_SIZE_ASC = new FileComparator() {
        @Override
        protected int ID() {
            return SW_SIZE;
        }

        @Override
        protected int REVERSE_ID() {
            return SW_SIZE + SW_REVERSE;
        }

        @Override
        protected int comp(File f1, File f2) {
            long l = f1.length() - f2.length();
            return (l == 0) ? 0 : (l < 0) ? -1 : 1;
        }

        @Override
        protected boolean eq(File f1, File f2) {
            return f1.length() == f2.length();
        }
    };

    public final static FileComparator COMP_SIZE_DESC = new FileComparator() {
        @Override
        protected int ID() {
            return SW_SIZE + SW_REVERSE;
        }

        @Override
        protected int REVERSE_ID() {
            return SW_SIZE;
        }

        @Override
        protected int comp(File f1, File f2) {
            long l = f1.length() - f2.length();
            return (l == 0) ? 0 : (l < 0) ? -1 : 1;
        }

        @Override
        protected boolean eq(File f1, File f2) {
            return f1.length() == f2.length();
        }
    };

    public final static FileComparator COMP_TIME_ASC = new FileComparator() {
        @Override
        protected int ID() {
            return SW_TIME;
        }

        @Override
        protected int REVERSE_ID() {
            return SW_TIME + SW_REVERSE;
        }

        @Override
        protected int comp(File f1, File f2) {
            long l = f1.lastModified() - f2.lastModified();
            return (l == 0) ? 0 : (l < 0) ? -1 : 1;
        }

        @Override
        protected boolean eq(File f1, File f2) {
            return f1.lastModified() == f2.lastModified();
        }
    };

    public final static FileComparator COMP_TIME_DESC = new FileComparator() {
        @Override
        protected int ID() {
            return SW_TIME + SW_REVERSE;
        }

        @Override
        protected int REVERSE_ID() {
            return SW_TIME;
        }

        @Override
        protected int comp(File f1, File f2) {
            long l = f1.lastModified() - f2.lastModified();
            return (l == 0) ? 0 : (l < 0) ? -1 : 1;
        }

        @Override
        protected boolean eq(File f1, File f2) {
            return f1.lastModified() == f2.lastModified();
        }
    };

    // comparators matching the SW_NAME ... switch IDs,
    // corresponding to FileComparator.ID() and FileComparator.REVERSE_ID()
    private final static FileComparator[] comps = {
            COMP_NAME_ASC, COMP_EXT_ASC, COMP_SIZE_ASC, COMP_TIME_ASC,
            COMP_NAME_DESC, COMP_EXT_DESC, COMP_SIZE_DESC, COMP_TIME_DESC
    };

    // switch lookup table
    private final static Map lsSwitches = new HashMap();

    static {
        lsSwitches.put(new Character('N'), SW_NAME_);
        lsSwitches.put("--sort=name", SW_NAME_);
        lsSwitches.put(new Character('X'), SW_EXT_);
        lsSwitches.put("--sort=ext", SW_EXT_);
        lsSwitches.put(new Character('S'), SW_SIZE_);
        lsSwitches.put("--sort=size", SW_SIZE_);
        lsSwitches.put(new Character('T'), SW_TIME_);
        lsSwitches.put("--sort=time", SW_TIME_);
        lsSwitches.put(new Character('R'), SW_REVERSE_);
        lsSwitches.put("--reverse", SW_REVERSE_);

        lsSwitches.put(new Character('f'), SW_FILE_);
        lsSwitches.put("--file", SW_FILE_);
        lsSwitches.put(new Character('h'), SW_HIDE_);
        lsSwitches.put("--all", SW_HIDE_);
        lsSwitches.put(new Character('d'), SW_DIR_);
        lsSwitches.put("--directory", SW_DIR_);
        lsSwitches.put(new Character('l'), SW_LONG_);
        lsSwitches.put("--long", SW_LONG_);
        lsSwitches.put(new Character('r'), SW_RECURSIVE_);
        lsSwitches.put("--recursive", SW_RECURSIVE_);
        lsSwitches.put(new Character('a'), SW_ABSOLUTE_);
        lsSwitches.put("--absolute", SW_ABSOLUTE_);
        lsSwitches.put(new Character('c'), SW_NOCASE_);
        lsSwitches.put("--nocase", SW_NOCASE_);
    }

    private final static String[] CONV_STRING = new String[0];
    private final static File[] CONV_FILE = new File[0];
    private final static File[] CONV_ATTRIBFILE = new AttributeFile[0];

    // directory history
    private final File homeDir = HOME;
    private File lastDir = HOME, currentDir = HOME;

    // the file encoding to be used
    private String encoding = DEFAULT_ENCODING;

    // the GUI resource and main frame to be used creating dialogs
    private GuiResource _guiResource;

    // the environment to be used when executing processes
    private HashMap env;
    private String[] envValues;

    public OsInterface() {
        this(null);
    }

    public OsInterface(GuiResource rsrc) {
        env = new HashMap();
        envValues = CONV_STRING;
        _guiResource = rsrc == null ? GuiResource.getDefaultInstance() : rsrc;
    }

    public OsInterface(File cwd, GuiResource rsrc) throws IOException {
        this(rsrc);
        cd0(null, cwd);
    }

    public OsInterface(String cwd, GuiResource rsrc) throws IOException {
        this((cwd == null) ? null : new File(cwd), rsrc);
    }

    public static boolean isWildcardFile(String s) {
        return (s == null) ? false
                : (s.lastIndexOf(WildcardFileFilter.MATCH_ANY) >= 0) ||
                        (s.lastIndexOf(WildcardFileFilter.MATCH_ONE) >= 0);
    }

    private void storeEnv() {
        Set entries = env.entrySet();
        int size = env.size();
        Iterator it = entries.iterator();
        envValues = new String[size];

        Map.Entry e;
        for (int i = 0; i < size;) {
            e = (Map.Entry)it.next();
            envValues[i++] = "" + e.getKey() + "=" + e.getValue();
        }
    }

    private File resolveName(String file) throws IOException {
        int l;
        if (file == null || (l = file.length()) == 0) {
            return homeDir;
        }
        else if (File.separatorChar == '/') {
            if (file.charAt(0) == '/') {
                // absolute file; new File(homeDir, file) might not work(!)
                return new File(file).getCanonicalFile();
            }
        }
        else {
            // always allow forward slashes
            file = file.replace('/', File.separatorChar);
        }

        if (l >= 2 && file.charAt(1) == ':') {
            // drive description
            return new File(file).getAbsoluteFile();
            // return new File(file + File.separatorChar);
        }
        else {
            return new File(currentDir, file).getCanonicalFile();
        }
    }

    private Collection listSubdir(File dir, String dirName,
            WildcardFileFilter flt, Collection result,
            boolean attribs, boolean recursive, boolean absolute)
            throws IOException {

        File[] contents = dir.listFiles();
        if (contents == null) {
            // system files may return null here!
            return result;
        }

        File f;
        String name;
        String newName;
        boolean accept;

        for (int i = 0; i < contents.length; i++) {
            f = contents[i];
            name = f.getName();
            accept = flt.accept(name, f);

            newName = null;
            if (accept) {
                newName = absolute ? f.getCanonicalPath()
                        : (dirName.length() != 0)
                                ? dirName + File.separatorChar + name
                                : name;
                result.add(
                        attribs ? (Object)new AttributeFile(dir, name, newName)
                                : (Object)new File(newName));
            }
            if (recursive && f.isDirectory()) {
                if (newName == null) {
                    newName = absolute ? f.getCanonicalPath()
                            : (dirName.length() != 0)
                                    ? dirName + File.separatorChar + name
                                    : name;
                }
                listSubdir(f, newName, flt, result, attribs, true, absolute);
            }
        }

        return result;
    }

    // update result[0] if sort command, update flt, return descending state
    private boolean updateSwitch(int code, boolean desc, Object[] result,
            WildcardFileFilter flt) {
        if (code < SW_REVERSE) {
            result[LSIDX_SW_COMP] = comps[desc ? code + SW_REVERSE : code];
            return desc;
        }
        else if (code == SW_REVERSE) {
            FileComparator comp = (FileComparator)result[LSIDX_SW_COMP];
            result[LSIDX_SW_COMP] = (comp == null) ? COMP_NAME_ASC
                    : comps[comp.REVERSE_ID()];
            return true;
        }

        switch (code) {
        case SW_HIDE:
            flt.setHiddenAllowed(false);
            break;
        case SW_DIR:
            flt.setDirsAllowed(true);
            flt.setFilesAllowed(false);
            break;
        case SW_FILE:
            flt.setFilesAllowed(true);
            flt.setDirsAllowed(false);
            break;
        case SW_LONG:
            result[LSIDX_SW_LONG] = Boolean.TRUE;
            break;
        case SW_NOCASE:
            flt.setCaseSensitive(false);
            break;
        case SW_ABSOLUTE:
            result[LSIDX_SW_ABSOLUTE] = Boolean.TRUE;
            break;
        default: // case SW_RECURSIVE:
            result[LSIDX_SW_RECURSIVE] = Boolean.TRUE;
        }

        return desc;
    }

    // store the specified filter and comparator, then return the first token
    // that is not a switch
    private String parseSwitches(String cmd, Object[] _default)
            throws IllegalArgumentException {
        StringTokenizer tok = new StringTokenizer(cmd);

        // avoid type casts for each call to updateSwitch
        WildcardFileFilter flt = (WildcardFileFilter)_default[LSIDX_SW_FILTER];

        String s;
        Object code;
        boolean descending = false;

        while (tok.hasMoreTokens()) {
            s = tok.nextToken();
            code = lsSwitches.get(s);
            if (code != null) {
                // long-name switch
                descending = updateSwitch(((Integer)code).intValue(),
                        descending, _default, flt);
            }
            else {
                char[] sw = s.toCharArray();
                if ((sw.length < 2) || (sw[0] != CHAR_SWITCH)) {
                    return s;
                }

                for (int i = 1; i < sw.length; i++) {
                    code = lsSwitches.get(new Character(sw[i]));
                    if (code == null) {
                        throw new IllegalArgumentException(s);
                    }
                    descending = updateSwitch(((Integer)code).intValue(),
                            descending, _default, flt);
                }
            }
        }

        return null;
    }

    // ensure that the file exists, return true iff successful
    private boolean createNew(File f) throws IOException {
        if (f.isDirectory()) {
            return f.mkdirs();
        }
        else {
            File parent = f.getParentFile();
            if (!parent.exists()) {
                return parent.mkdirs() && f.createNewFile();
            }
            else {
                return f.createNewFile();
            }
        }
    }

    private InputStream istream(Object o, boolean[] newStream)
            throws IOException {
        newStream[0] = false;

        if (o == null) {
            return System.in;
        }
        else if (o instanceof InputStream) {
            return (InputStream)o;
        }
        else if (o instanceof byte[]) {
            return new ByteArrayInputStream((byte[])o);
        }
        else if (o instanceof File) {
            newStream[0] = true;
            return new BufferedInputStream(
                    new FileInputStream(resolve((File)o)));
        }
        else if (o instanceof URL) {
            newStream[0] = true;
            return new BufferedInputStream(((URL)o).openStream());
        }
        else if (o instanceof URLConnection) {
            return ((URLConnection)o).getInputStream();
        }
        else {
            return null;
        }
    }

    private OutputStream ostream(Object o, boolean[] newStream, boolean append,
            boolean createDirs) throws IOException {
        newStream[0] = true;

        if (o == null) {
            newStream[0] = false;
            return System.out;
        }
        if (o instanceof OutputStream) {
            newStream[0] = false;
            return (OutputStream)o;
        }
        else if (o instanceof File) {
            File dest = resolve((File)o);
            if (createDirs) {
                File d = dest.isDirectory() ? dest : dest.getParentFile();
                if (d != null) {
                    d.mkdirs();
                }
            }
            return new BufferedOutputStream(
                    new FileOutputStream(dest.getPath(), append));
        }
        else if (o instanceof URLConnection) {
            newStream[0] = false;
            return ((URLConnection)o).getOutputStream();
        }
        else {
            String s = ConvertUtils.toString(o);
            return new BufferedOutputStream(
                    new FileOutputStream(resolve(s).getPath(), append));
        }
    }

    private Reader reader(Object o, boolean[] newStream) throws IOException {
        newStream[0] = false;

        if (o == null) {
            return SYSIN;
        }
        else if (o instanceof Reader) {
            return (Reader)o;
        }
        else {
            InputStream is = istream(o, newStream);
            if (is == System.in) {
                return SYSIN;
            }
            else if (is == null) {
                newStream[0] = true;
                return new StringReader(ConvertUtils.toString(o));
            }
            else {
                return new InputStreamReader(is, encoding);
            }
        }
    }

    private Writer writer(Object o, boolean[] newStream, boolean append,
            boolean createDirs) throws IOException {
        newStream[0] = false;

        if (o == null) {
            return SYSOUT;
        }
        if (o instanceof Writer) {
            return (Writer)o;
        }
        else {
            OutputStream os = ostream(o, newStream, append, createDirs);
            return (os == System.out) ? (Writer)SYSOUT
                    : (os == System.err) ? (Writer)SYSERR
                            : (Writer)new OutputStreamWriter(os, encoding);
        }
    }

    @Override
    public synchronized Object clone() {
        try {
            OsInterface result = (OsInterface)super.clone();
            result.env = (HashMap)env.clone();
            result.envValues = envValues.clone();
            return result;
        }
        catch (CloneNotSupportedException e) {
            return null; // won't happen
        }
    }

    public static String[] splitPath(String path) {
        int len = path.length();
        int startIdx = 0, newIdx;

        ArrayList l = new ArrayList();

        while (startIdx < len) {
            newIdx = path.indexOf(File.pathSeparatorChar, startIdx);
            if (newIdx >= 0) {
                l.add(path.substring(startIdx, newIdx));
                startIdx = newIdx + 1;
            }
            else {
                l.add(path.substring(startIdx));
                break;
            }
        }

        return (String[])l.toArray(new String[l.size()]);
    }

    public static int transport(InputStream in, OutputStream out)
            throws IOException {
        return transport(in, out, -1, new byte[BUF_SIZE]);
    }

    public static int transport(InputStream in, OutputStream out, int req)
            throws IOException {
        return transport(in, out, req, new byte[BUF_SIZE]);
    }

    public static int transport(InputStream in, OutputStream out, int req,
            byte[] buffer)
            throws IOException {

        int total = 0;
        int actual, nextRead;

        if (req < 0) {
            req = Integer.MAX_VALUE;
        }

        while (req > 0) {
            nextRead = (req > buffer.length) ? buffer.length : req;
            req -= nextRead;

            actual = in.read(buffer, 0, nextRead);
            if (actual < 0) {
                break;
            }
            out.write(buffer, 0, actual);
            total += actual;
        }

        out.flush();
        return total;
    }

    public static int transport(Reader in, Writer out)
            throws IOException {
        return transport(in, out, -1, new char[BUF_SIZE]);
    }

    public static int transport(Reader in, Writer out, int req)
            throws IOException {
        return transport(in, out, req, new char[BUF_SIZE]);
    }

    public static int transport(Reader in, Writer out, int req, char[] buffer)
            throws IOException {

        int total = 0;
        int actual, nextRead;

        if (req < 0) {
            req = Integer.MAX_VALUE;
        }

        while (req > 0) {
            nextRead = (req > buffer.length) ? buffer.length : req;
            req -= nextRead;

            actual = in.read(buffer, 0, nextRead);
            if (actual < 0) {
                break;
            }
            out.write(buffer, 0, actual);
            total += actual;
        }

        out.flush();
        return total;
    }

    public StringBuffer read(Object obj) throws IOException {
        return read(null, obj);
    }

    public synchronized StringBuffer read(StringBuffer buf, Object obj)
            throws IOException {
        boolean[] doClose = new boolean[1];
        Reader r = reader(obj instanceof String ? new File((String)obj) : obj,
                doClose);
        if (buf == null) {
            buf = new StringBuffer();
        }

        try {
            read(buf, r, -1);
        }
        finally {
            if (doClose[0]) {
                r.close();
            }
        }
        return buf;
    }

    public HexEditor edit(Object obj) throws IOException {
        return (HexEditor)readBytes(new HexEditor(), obj);
    }

    public ByteBuffer readBytes(Object obj) throws IOException {
        return readBytes(null, obj);
    }

    public synchronized ByteBuffer readBytes(ByteBuffer buf, Object obj)
            throws IOException {
        boolean[] doClose = new boolean[1];
        InputStream in = istream(
                obj instanceof String ? new File((String)obj) : obj, doClose);
        if (buf == null) {
            buf = new ByteBuffer();
        }

        try {
            read(buf, in, -1);
        }
        finally {
            if (doClose[0]) {
                in.close();
            }
        }

        return buf;
    }

    public static int read(ByteBuffer buf, InputStream in) throws IOException {
        return read(buf, in, -1);
    }

    public static int read(ByteBuffer buf, InputStream in, int req)
            throws IOException {
        int total = 0;
        int actual, nextRead;

        byte[] buffer = new byte[SBUF_SIZE];

        if (req < 0) {
            req = Integer.MAX_VALUE;
        }

        while (req > 0) {
            nextRead = (req > SBUF_SIZE) ? SBUF_SIZE : req;
            req -= nextRead;

            actual = in.read(buffer, 0, nextRead);
            if (actual < 0) {
                break;
            }
            buf.append(buffer, 0, actual);
            total += actual;
        }

        return total;
    }

    public static int read(StringBuffer buf, InputStream in)
            throws IOException {
        Reader r = new InputStreamReader(in);
        return read(buf, r, -1);
    }

    public static int read(StringBuffer buf, InputStream in, int req)
            throws IOException {
        Reader r = new InputStreamReader(in);
        return read(buf, r, req);
    }

    public static int read(StringBuffer buf, Reader in) throws IOException {
        return read(buf, in, -1);
    }

    public static int read(StringBuffer buf, Reader in, int req)
            throws IOException {
        int total = 0;
        int actual, nextRead;

        char[] buffer = new char[SBUF_SIZE];
        if (buf == null) {
            buf = new StringBuffer();
        }

        if (req < 0) {
            req = Integer.MAX_VALUE;
        }

        while (req > 0) {
            nextRead = (req > SBUF_SIZE) ? SBUF_SIZE : req;
            req -= nextRead;

            actual = in.read(buffer, 0, nextRead);
            if (actual < 0) {
                break;
            }
            buf.append(buffer, 0, actual);
            total += actual;
        }

        return total;
    }

    public synchronized File resolve(String s) throws IOException {
        return resolveName(s);
    }

    public synchronized File resolve(File f) throws IOException {
        return (f == null) ? currentDir : resolveName(f.getPath());
    }

    public synchronized String[] set() {
        return envValues.clone();
    }

    public synchronized String set(String name, String contents) {
        String result = (String)env.put(name, contents);
        if (result != name) {
            storeEnv();
        }

        return result;
    }

    public synchronized String get(String name) {
        return (String)env.get(name);
    }

    public synchronized GuiResource getGuiResource() {
        return _guiResource;
    }

    public synchronized void setGuiResource(GuiResource rsrc) {
        _guiResource = rsrc == null ? GuiResource.getDefaultInstance() : rsrc;
    }

    public synchronized String getEncoding() {
        return encoding;
    }

    public synchronized void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public synchronized AsyncProcess exec(String cmd) {
        return new AsyncProcess(cmd, envValues);
    }

    public synchronized AsyncProcess exec(String[] cmds) {
        return new AsyncProcess(cmds, envValues);
    }

    public synchronized AsyncProcess exec(String cmd, Writer out, Writer err) {
        return new AsyncProcess(cmd, envValues, out, err);
    }

    public synchronized AsyncProcess exec(String[] cmds, Writer out,
            Writer err) {
        return new AsyncProcess(cmds, envValues, out, err);
    }

    public synchronized File pwd() {
        return currentDir;
    }

    private boolean touch0(File f, long time) throws IOException {
        if (!f.exists()) {
            return createNew(f) && f.setLastModified(time);
        }
        else {
            return f.setLastModified(time);
        }
    }

    public synchronized boolean touch(File f) throws IOException {
        return touch0(resolve(f), System.currentTimeMillis());
    }

    public synchronized boolean touch(File f, long time) throws IOException {
        return touch0(resolve(f), time);
    }

    public synchronized boolean touch(String s) throws IOException {
        return touch0(resolve(s), System.currentTimeMillis());
    }

    public synchronized boolean touch(String s, long time) throws IOException {
        return touch0(resolve(s), time);
    }

    public boolean[] touch(Object[] obj) throws IOException {
        return touch(Arrays.asList(obj), System.currentTimeMillis());
    }

    public boolean[] touch(Object[] obj, long time) throws IOException {
        return touch(Arrays.asList(obj), time);
    }

    public boolean[] touch(Collection c) throws IOException {
        return touch(c, System.currentTimeMillis());
    }

    public synchronized boolean[] touch(Collection c, long time)
            throws IOException {
        Iterator it = c.iterator();
        boolean[] result = new boolean[c.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = touch0(resolve(ConvertUtils.toString(it.next())), time);
        }
        return result;
    }

    private File[] ls0(String cmd, String path, Pattern pattern,
            boolean caseSensitive, boolean _long)
            throws IOException, IllegalArgumentException {

        // parse the command string
        Object[] setup = new Object[LSIDX_MAX + 1];
        WildcardFileFilter flt = new WildcardFileFilter();

        setup[LSIDX_SW_FILTER] = flt;
        flt.setCaseSensitive(caseSensitive);
        setup[LSIDX_SW_LONG] = _long ? Boolean.TRUE : Boolean.FALSE;
        setup[LSIDX_SW_RECURSIVE] = Boolean.FALSE;
        setup[LSIDX_SW_ABSOLUTE] = Boolean.FALSE;

        String illegal = (cmd == null) ? null : parseSwitches(cmd, setup);
        if (illegal != null) {
            throw new IllegalArgumentException(illegal);
        }

        // retreive the parsed attributes
        boolean absolute = ((Boolean)setup[LSIDX_SW_ABSOLUTE]).booleanValue();
        _long = ((Boolean)setup[LSIDX_SW_LONG]).booleanValue();
        boolean recursive = ((Boolean)setup[LSIDX_SW_RECURSIVE])
                .booleanValue();

        // always allow / as separator
        path = path.replace('/', File.separatorChar);

        // determine the search directory, search pattern and name to be used
        // for the root directory.
        File dir;
        String dirName;

        if (pattern == null) {
            int split = path.lastIndexOf(File.separatorChar);
            if (split == path.length() - 1) {
                // last char is the separator
                dir = resolve(path);
                dirName = absolute ? dir.getCanonicalPath() : "";
                pattern = WildcardFileFilter.ACCEPT_ALL;
            }
            else if ((split == 2) && (path.charAt(1) == ':')) {
                // drive specification
                dirName = path.substring(0, split + 1);
                dir = new File(dirName);
                pattern = WildcardFileFilter.createPattern(
                        path.substring(split + 1), caseSensitive);
            }
            else if (!isWildcardFile(path)) {
                // no wildcard
                pattern = WildcardFileFilter.ACCEPT_ALL;
                dir = resolve(path);

                if (dir.isDirectory()) {
                    // single directory
                    dirName = absolute ? dir.getCanonicalPath() : path;
                }
                else if (split < 0) {
                    // single file, current directory
                    dir = currentDir;
                    dirName = absolute ? new File(currentDir, path)
                            .getCanonicalFile().getParent() : "";
                }
                else {
                    // single file, given directory
                    pattern = WildcardFileFilter.createPattern(
                            path.substring(split + 1), caseSensitive);
                    dir = dir.getParentFile();
                    dirName = absolute ? dir.getCanonicalPath()
                            : dir.toString();
                }
            }
            else {
                // wildcard
                if (split < 0) {
                    // wildcard only (current dir)
                    dir = currentDir;
                    pattern = WildcardFileFilter.createPattern(path,
                            caseSensitive);
                    dirName = absolute ? dir.getCanonicalPath() : "";
                }
                else {
                    // wildcard and given dir
                    dirName = path.substring(0, split);
                    dir = resolve(dirName);
                    pattern = WildcardFileFilter.createPattern(
                            path.substring(split + 1), caseSensitive);
                }
            }
        }
        else {
            // pattern already given
            dir = resolve(path);
            dirName = absolute ? dir.getCanonicalFile().getPath() : "";
        }

        // verify that the given directory is valid
        if (!dir.isDirectory()) {
            return new File[0];
        }

        // set the pattern to be used by the filter, and do the listing
        flt.setPattern(pattern);
        ArrayList tmp = new ArrayList();
        listSubdir(dir, dirName, flt, tmp, _long, recursive, absolute);
        File[] result = (File[])tmp.toArray(
                _long ? (Object[])CONV_ATTRIBFILE : (Object[])CONV_FILE);

        // sort the result, if required
        FileComparator c = (FileComparator)setup[LSIDX_SW_COMP];
        if (c != null) {
            Arrays.sort(result, c);
        }

        return result;
    }

    public synchronized File[] ls() throws IOException {
        return ls0(null, currentDir.getPath(), WildcardFileFilter.ACCEPT_ALL,
                true, false);
    }

    public synchronized File[] ls(String mask) throws IOException {
        return ls0(null, mask, null, true, false);
    }

    public synchronized File[] ls(String directory, Pattern mask)
            throws IOException {
        return ls0(null, directory, mask, true, false);
    }

    public synchronized File[] ls(String switches, String mask)
            throws IOException, IllegalArgumentException {
        return ls0(switches, mask, null, true, false);
    }

    public synchronized File[] ls(String switches, String dir, Pattern mask)
            throws IOException, IllegalArgumentException {
        return ls0(switches, dir, mask, true, false);
    }

    public synchronized File[] dir() throws IOException {
        return ls0(null, currentDir.getPath(), WildcardFileFilter.ACCEPT_ALL,
                false, false);
    }

    public synchronized File[] dir(String mask) throws IOException {
        return ls0(null, mask, null, false, false);
    }

    public synchronized File[] dir(String switches, String mask)
            throws IOException, IllegalArgumentException {
        return ls0(switches, mask, null, false, false);
    }

    public synchronized File[] dir(String switches, String dir, Pattern mask)
            throws IOException, IllegalArgumentException {
        return ls0(switches, dir, mask, false, false);
    }

    public synchronized AttributeFile[] ll() throws IOException {
        return (AttributeFile[])ls0(null, currentDir.getPath(),
                WildcardFileFilter.ACCEPT_ALL, true, true);
    }

    public synchronized AttributeFile[] ll(String mask) throws IOException {
        return (AttributeFile[])ls0(null, mask, null, true, true);
    }

    public synchronized AttributeFile[] ll(String switches, String mask)
            throws IOException, IllegalArgumentException {
        return (AttributeFile[])ls0(switches, mask, null, true, true);
    }

    public synchronized AttributeFile[] ll(String switches, String dir,
            Pattern mask)
            throws IOException, IllegalArgumentException {
        return (AttributeFile[])ls0(switches, dir, mask, true, true);
    }

    public synchronized AttributeFile[] dirAll() throws IOException {
        return (AttributeFile[])ls0(null, currentDir.getPath(),
                WildcardFileFilter.ACCEPT_ALL, false, true);
    }

    public synchronized AttributeFile[] dirAll(String mask) throws IOException {
        return (AttributeFile[])ls0(null, mask, null, false, true);
    }

    public synchronized AttributeFile[] dirAll(String switches, String mask)
            throws IOException, IllegalArgumentException {
        return (AttributeFile[])ls0(switches, mask, null, false, true);
    }

    public synchronized AttributeFile[] dirAll(String switches, String dir,
            Pattern mask)
            throws IOException, IllegalArgumentException {
        return (AttributeFile[])ls0(switches, dir, mask, false, true);
    }

    public synchronized InputStream toInputStream(Object o) throws IOException {
        return istream(o, new boolean[1]);
    }

    public synchronized Reader toReader(Object o) throws IOException {
        return reader(o, new boolean[1]);
    }

    public synchronized OutputStream toOutputStream(Object o,
            boolean appendFile) throws IOException {
        return ostream(o, new boolean[1], appendFile, false);
    }

    public synchronized Writer toWriter(Object o, boolean appendFile)
            throws IOException {
        return writer(o, new boolean[1], appendFile, false);
    }

    private int cat0(Object source, OutputStream dest) throws IOException {
        int result = -1;
        boolean[] close = new boolean[1];
        InputStream in = istream(source, close);
        if (in == null) {
            if (source == null) {
                return 0;
            }
            in = new ByteArrayInputStream(
                    ConvertUtils.toString(source).getBytes());
        }

        try {
            result = transport(in, dest);
        }
        finally {
            if (close[0] && (in != System.in)) {
                in.close();
            }
        }

        return result;
    }

    private int cat0(Object source, Writer dest) throws IOException {
        int result = -1;
        boolean[] close = new boolean[1];
        Reader in = reader(source, close);

        try {
            result = transport(in, dest);
        }
        finally {
            if (close[0]) {
                in.close();
            }
        }

        return result;
    }

    public int cat(Object o) throws IOException {
        return cat(o, SYSOUT);
    }

    public synchronized int cat(Object source, OutputStream dest)
            throws IOException {
        if (source instanceof Object[]) {
            source = Arrays.asList((Object[])source);
        }
        if (source instanceof Collection) {
            Iterator i = ((Collection)source).iterator();
            int result = 0;
            while (i.hasNext()) {
                result += cat0(i.next(), dest);
            }
            return result;
        }
        else {
            return cat0(source, dest);
        }
    }

    public synchronized int cat(Object source, Writer dest) throws IOException {
        if (source instanceof Object[]) {
            source = Arrays.asList((Object[])source);
        }
        if (source instanceof Collection) {
            Iterator i = ((Collection)source).iterator();
            int result = 0;
            while (i.hasNext()) {
                result += cat0(i.next(), dest);
            }
            return result;
        }
        else {
            return cat0(source, dest);
        }
    }

    public int cat(Object source, File dest) throws IOException {
        return cat(source, dest, true);
    }

    public int cat(Object source, String dest) throws IOException {
        return cat(source, dest, true);
    }

    public synchronized int cat(Object source, Object dest, boolean append)
            throws IOException {
        int result = 0;
        boolean[] close = new boolean[1];
        OutputStream out = ostream(dest, close, append, true);
        Writer w = null;
        if (out == null) {
            w = writer(dest, close, append, true);
        }

        try {
            if (out != null) {
                result = cat(source, out);
            }
            else {
                result = cat(source, w);
            }
        }
        finally {
            if (close[0]) {
                if (out != null) {
                    out.close();
                }
                else {
                    w.close();
                }
            }
        }

        return result;
    }

    public synchronized long save(Serializable obj, Object dest)
            throws IOException {
        boolean[] newStream = new boolean[1];
        OutputStream ostr = ostream(dest, newStream, true, false);
        MonitoredOutputStream mostr = new MonitoredOutputStream(ostr);
        ObjectOutputStream oostr = new ObjectOutputStream(mostr);
        try {
            oostr.writeObject(obj);
        }
        finally {
            if (newStream[0]) {
                oostr.close();
            }
        }
        return mostr.getOutputSize();
    }

    public synchronized Object load(Object src)
            throws ClassNotFoundException, IOException {
        boolean[] newStream = new boolean[1];
        InputStream istr = istream(src, newStream);
        if (istr == null) {
            newStream[0] = true;
            istr = new FileInputStream(resolve(ConvertUtils.toString(src)));
        }
        ObjectInputStream iistr = new ObjectInputStream(istr);
        Object result = null;
        try {
            result = iistr.readObject();
        }
        finally {
            if (newStream[0]) {
                iistr.close();
            }
        }
        return result;
    }

    private File cd0(String s, File force)
            throws IOException, FileNotFoundException {
        File f;
        if (force != null) {
            f = force;
        }
        else {
            f = resolveName(s);
            if (!f.isDirectory()) {
                throw new IOException("" + f + " is not a directory");
            }
            else if (!f.exists()) {
                throw new FileNotFoundException("" + f);
            }
        }

        lastDir = currentDir;
        return currentDir = f.getCanonicalFile();
    }

    public synchronized File cd() {
        try {
            return cd0(null, null);
        }
        catch (IOException e) {
            // won't happen
            return null;
        }
    }

    public synchronized File cd(File f) throws IOException {
        return cd0((f == null) ? null : f.getPath(), null);
    }

    public synchronized File cd(String s) throws IOException {
        return cd0(s, null);
    }

    public synchronized File revert() throws IOException {
        return cd0(null, lastDir);
    }

    // confirm[0] determines the behaviour when confirmation is needed, and
    // should
    // initially contain the desired BTN(S)_xxxx constant.
    // the return value determines whether the copy succeeded
    private boolean cp0(String source, String dest, boolean append,
            int[] confirm)
            throws IOException {
        // resolve the file names
        File src = resolve(source);
        File dst = resolve(dest);

        if (dst.isDirectory()) {
            // copy to a directory - create the full destination file
            dst = new File(dest + File.separatorChar + src.getName());
        }

        if (src.isDirectory()) {
            // copy a directory - create the directory and return
            return dst.mkdirs();
        }

        if (dst.exists()) {
            // check for confirmation, if the destination already exists
            if (confirm[0] != 0) {
                switch (DialogUtils.showConfirmationDialog(
                        MessageFormat.format(_guiResource.getString(
                                (append ? "msg._Os.ConfirmAppend"
                                        : "msg._Os.ConfirmOverwrite")),
                                new Object[] { dst.getPath() }),
                        confirm[0],
                        _guiResource)) {
                case ButtonConstants.BTN_CANCEL:
                case ButtonConstants.BTN_CLOSE:
                    // cancel : indicate cancellation
                    confirm[0] = ButtonConstants.BTN_CLOSE;
                    return false;
                case ButtonConstants.BTN_ALL:
                    // confirm all: prevent future confirmations
                    confirm[0] = 0;
                    break;
                case ButtonConstants.BTN_NO:
                    // keep the current question message, but don't copy
                    return false;
                // default: // BTN_YES received. Continue
                }
            }
        } // dst.exists()

        // copy the file
        InputStream in = new BufferedInputStream(new FileInputStream(src));
        try {
            @SuppressWarnings("resource") // False positive, suppress warning.
            OutputStream out = new BufferedOutputStream(
                    append ? new FileOutputStream(dst.getPath(), true)
                            : new FileOutputStream(dst));
            try {
                transport(in, out);
            }
            finally {
                out.close();
            }
        }
        finally {
            in.close();
        }

        return true;
    }

    public synchronized boolean cp(File source, File dest) throws IOException {
        return cp0(source.getPath(), dest.getPath(), false,
                new int[] { ButtonConstants.BTNS_YES_NO });
    }

    public synchronized boolean cp(String source, String dest)
            throws IOException {
        return cp0(source, dest, false,
                new int[] { ButtonConstants.BTNS_YES_NO });
    }

    public int cp(Object[] source, String dest) throws IOException {
        return cp(Arrays.asList(source), dest);
    }

    public int cp(Object[] source, File dest) throws IOException {
        return cp(Arrays.asList(source), dest.getPath());
    }

    public int cp(Collection source, File dest) throws IOException {
        return cp(source, dest.getPath());
    }

    public synchronized int cp(Collection source, String dest)
            throws IOException {
        Iterator it = source.iterator();
        int totalCopied = 0;

        int[] confirm = new int[] { ButtonConstants.BTNS_YES_ALL_NO_CANCEL };

        while (it.hasNext()) {
            if (cp0(ConvertUtils.toString(it.next()), dest, false, confirm)) {
                totalCopied++;
            }
            if (confirm[0] == ButtonConstants.BTN_CLOSE) {
                // cancel the copy
                break;
            }
        }

        return totalCopied;
    }

    private boolean[] mv0(Collection source, File dest) throws IOException {
        if (!dest.isDirectory()) {
            throw new IllegalArgumentException("" + dest.getPath());
        }

        boolean[] result = new boolean[source.size()];
        Iterator it = source.iterator();
        String dst = dest.getPath() + File.separatorChar;
        File next;

        for (int i = 0; i < result.length;) {
            next = resolve(ConvertUtils.toString(it.next()));
            result[i++] = next.renameTo(new File(dst + next.getName()));
        }
        return result;
    }

    private boolean mv0(File source, File dest) throws IOException {
        if (dest.isDirectory()) {
            return source.renameTo(new File(
                    dest.getPath() + File.separatorChar + source.getName()));
        }
        else {
            return source.renameTo(dest);
        }
    }

    public synchronized boolean mv(File source, File dest) throws IOException {
        return mv0(resolve(source), resolve(dest));
    }

    public synchronized boolean mv(String source, String dest)
            throws IOException {
        return mv0(resolve(source), resolve(dest));
    }

    public synchronized boolean[] mv(Object[] source, File dest)
            throws IOException {
        return mv0(Arrays.asList(source), resolve(dest));
    }

    public synchronized boolean[] mv(Object[] source, String dest)
            throws IOException {
        return mv0(Arrays.asList(source), resolve(dest));
    }

    public synchronized boolean[] mv(Collection source, File dest)
            throws IOException {
        return mv0(source, resolve(dest));
    }

    public synchronized boolean[] mv(Collection source, String dest)
            throws IOException {
        return mv0(source, resolve(dest));
    }

    // confirm[0] determines the behaviour when confirmation is needed, and
    // should
    // initially contain the desired BTN(S)_xxxx constant.
    // the return value determines whether the user confirmed
    private boolean confirm0(File f, int[] confirm) throws IOException {
        if (!f.exists()) {
            // can't delete a nonexistent file
            throw new FileNotFoundException("" + f.getPath());
        }

        if (confirm[0] == 0) {
            // no user interaction necessary - confirm
            return true;
        }

        switch (DialogUtils.showConfirmationDialog(
                MessageFormat.format(_guiResource.getString(
                        "msg._Os.ConfirmDelete"),
                        new Object[] { f.getPath() }),
                confirm[0], _guiResource)) {
        case ButtonConstants.BTN_CANCEL:
        case ButtonConstants.BTN_CLOSE:
            // cancel : indicate cancellation
            confirm[0] = ButtonConstants.BTN_CLOSE;
            return false;
        case ButtonConstants.BTN_ALL:
            // confirm all: prevent future confirmations
            confirm[0] = 0;
            return true;
        case ButtonConstants.BTN_NO:
            // keep the current question message, but don't copy
            return false;
        default:
            // BTN_YES received. confirmed
            return true;
        }
    }

    public synchronized boolean rm(File f) throws IOException {
        f = resolve(f);
        if (!confirm0(f, new int[] { ButtonConstants.BTNS_YES_NO })) {
            return false;
        }
        return f.delete();
    }

    public synchronized boolean rm(String f) throws IOException {
        File _f = resolve(f);
        if (!confirm0(_f, new int[] { ButtonConstants.BTNS_YES_NO })) {
            return false;
        }
        return _f.delete();
    }

    public synchronized boolean[] rm(Object[] source) throws IOException {
        return rm(Arrays.asList(source));
    }

    public synchronized boolean[] rm(Collection fs) throws IOException {
        boolean[] result = new boolean[fs.size()];
        Iterator it = fs.iterator();

        int[] confirm = new int[] { ButtonConstants.BTNS_YES_ALL_NO_CANCEL };
        File f;

        for (int i = 0; i < result.length; i++) {
            f = resolve(ConvertUtils.toString(it.next()));
            if (confirm0(f, confirm)) {
                result[i] = f.delete();
            }
            else {
                if (confirm[0] == ButtonConstants.BTN_CLOSE) {
                    break;
                }
            }
        }

        return result;
    }

    public int split(Object obj, int size, String prefix, String suffix)
            throws IOException {
        return split(obj, size, currentDir, prefix, suffix);
    }

    /**
     * Split a file into a number of files having (at most) the given size. All
     * files are placed in destDir, and are named prefix + n + suffix, where n
     * is a 3-digit number starting from 0.
     *
     * @param obj
     *            the input object
     * @param size
     *            the chunk size
     * @param destDir
     *            the destination directory
     * @param prefix
     *            the prefix used for file names of chunks written
     * @param suffix
     *            the suffix used for file names of chunks written
     * @return the number of files produced.
     * @throws IOException
     *             on I/O error
     */
    public int split(Object obj, int size, File destDir,
            String prefix, String suffix) throws IOException {
        if (!destDir.exists()) {
            throw new FileNotFoundException(destDir.getPath());
        }
        else if (size <= 0) {
            throw new IllegalArgumentException("" + size);
        }

        boolean[] doClose = new boolean[1];
        InputStream i = istream(obj, doClose);

        int result = -1;
        byte[] buf = new byte[size > BUF_SIZE ? BUF_SIZE : size];

        try {
            int moved = 0;
            while (true) {
                File dest = new File(destDir.getPath() + File.separator +
                        prefix + StringUtils.expandLeft("" + ++result, '0', 3) +
                        suffix);
                OutputStream o = new BufferedOutputStream(
                        new FileOutputStream(dest));
                try {
                    moved = transport(i, o, size, buf);
                }
                finally {
                    o.close();
                }

                if (moved == 0) {
                    // remove the empty file
                    dest.delete();
                    return result;
                }
            }
        }
        finally {
            if (doClose[0]) {
                i.close();
            }
        }
    }

}
