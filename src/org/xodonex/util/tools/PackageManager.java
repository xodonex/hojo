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
package org.xodonex.util.tools;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xodonex.util.os.OsInterface;
import org.xodonex.util.vm.VmUtils;

/**
 * Interface to the system class loader's set of packages.
 *
 * The implementation is slow, which is bad. It also relies on Java platform
 * internals, which is worse. But there is no way to provide this access through
 * the official APIs.
 */
public class PackageManager {

    private static class JarFileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            return (name.endsWith(".jar") || name.endsWith(".zip"));
        }
    }

    public class _Package {
        private _Package parent;
        private String name;
        private File source;
        private HashMap subpackages = null; // unresolved
        private HashSet contents = null; // unresolved

        private _Package(_Package parent, String name, File source) {
            this.parent = parent;
            this.name = name;
            this.source = source;
            String full = getFullName();

            if (parent != null) {
                // link to the parent
                parent.validate();
                parent.subpackages.put(name, this);
            }

            // add to the package info
            PackageManager.this.packageMap.put(full, this);
        }

        // construct an initialized, empty package
        private _Package(_Package parent, String name) {
            this(parent, name, null);
            subpackages = new HashMap();
            contents = new HashSet();
        }

        public String getName() {
            return name;
        }

        public String getFullName() {
            return (parent == null || parent.name.equals("")) ? name
                    : parent.getFullName() + '.' + name;
        }

        @Override
        public synchronized String toString() {
            return "Package " + getFullName();
        }

        public synchronized _Package getPackage(String name) {
            validate();
            return (_Package)subpackages.get(name);
        }

        public synchronized boolean isMember(String name) {
            validate();
            return contents.contains(name);
        }

        public synchronized String[] getMembers() {
            validate();
            return (String[])contents.toArray(new String[contents.size()]);
        }

        public synchronized _Package[] getPackages() {
            validate();
            return (_Package[])subpackages.values()
                    .toArray(new _Package[subpackages.size()]);
        }

        private _Package getOrCreate(File src, String name) {
            _Package result = (_Package)subpackages.get(name);
            if (result == null) {
                result = new _Package(this, name, src);
            }
            return result;
        }

        private synchronized void validate() {
            if (subpackages == null) {
                update(source);
            }
        }

        private void update(File source) {
            if (subpackages == null) {
                // indicate that this package has been updated
                subpackages = new HashMap();
                contents = new HashSet();
            }
            if (source == null) {
                // illegal source
                return;
            }

            try {
                String[] names = source.list();
                File f;
                String s, name;
                _Package p;
                boolean check = isClassCheckEnabled();

                for (int i = 0; i < names.length; i++) {
                    s = names[i];

                    if (s.endsWith(".class")) {
                        name = s.substring(0, s.length() - 6);
                        if (!contents.contains(name) && checkImportName(s)
                                && (!check ||
                                        checkPublicClass(new FileInputStream(
                                                new File(source, s))))) {
                            contents.add(name);
                        }
                    }
                    else {
                        f = new File(source, s);
                        if (f.isDirectory()) {
                            p = getOrCreate(f, s);
                            if (!f.equals(p.source)) {
                                // reused a package initiated from a different
                                // source - update
                                // it
                                p.update(f);
                            }
                        } // isDirectory()
                    } // else
                } // for
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // the single instance of this class
    private static PackageManager instance;

    // whether classes should be checked
    private boolean _doCheck = false;

    // full package name -m-> _Package (sync is necessary,
    // must be initialized before any _Package is instantiated)
    private Map packageMap = new Hashtable();

    // root package
    private _Package packages = new _Package(null, "");

    private PackageManager() {
        this(false);
    }

    private PackageManager(boolean checkClasses) {
        _doCheck = checkClasses;
        reload();
    }

    /**
     * Ensure that the package manager has been initialized.
     */
    public static void init() {
        init(false);
    }

    /**
     * Ensure that the package manager has been initialized. If so, ensure that
     * the manager is at least as restrictive as specified.
     *
     * @param checkClasses
     *            whether the manager only consider classes which are public.
     */
    public synchronized static void init(boolean checkClasses) {
        if (instance == null) {
            instance = new PackageManager(checkClasses);
        }
        else {
            if (checkClasses && !instance._doCheck) {
                instance._doCheck = true;
                instance.reload();
            }
        }
    }

    /**
     * Ensure that the package manager is initialized to the initial state.
     *
     * @param checkClasses
     *            whether the manager only consider classes which are public.
     */
    public synchronized static void reload(boolean checkClasses) {
        if (instance == null) {
            instance = new PackageManager(checkClasses);
        }
        else {
            instance._doCheck = checkClasses;
            instance.reload();
        }
    }

    public synchronized static PackageManager getInstance() {
        return instance == null ? instance = new PackageManager() : instance;
    }

    public synchronized static boolean isInited() {
        return instance != null;
    }

    public static boolean isClassCheckEnabled() {
        return instance != null && instance._doCheck;
    }

    /**
     * Add the jar files respectively directories from which the system classes
     * are found, in the order searched by the VM.
     *
     * @return the system classpath, as a list of paths
     * @throws IOException
     *             on I/O error
     */
    public static List getClassPath() throws IOException {
        ArrayList l = new ArrayList();
        HashSet s = new HashSet();

        String bootClassPath = System.getProperty("sun.boot.class.path");
        if (bootClassPath != null) {
            addToList(l, s, null, OsInterface.splitPath(bootClassPath));
        }

        String extDirsPath = System.getProperty("java.ext.dirs");
        if (extDirsPath != null) {
            String[] extDirs = OsInterface.splitPath(extDirsPath);
            File f;
            FilenameFilter flt = new JarFileFilter();
            for (int i = 0; i < extDirs.length; i++) {
                f = new File(extDirs[i]);
                if (f.isDirectory() && f.exists()) {
                    addToList(l, s, f, f.list(flt));
                }
            }
        }

        String javaClassPath = System.getProperty("java.class.path");
        if (javaClassPath != null) {
            addToList(l, s, null, OsInterface.splitPath(javaClassPath));
        }
        return l;
    }

    private static void addToList(List l, Set s, File parent, String[] names)
            throws IOException {
        File f;

        for (int i = 0; i < names.length; i++) {
            f = new File(parent, names[i]).getCanonicalFile();
            if (s.add(f) && f.exists()) {
                l.add(f);
            }
        }
    }

    protected void reload() {
        try {
            String s;
            File f;

            for (Iterator i = getClassPath().iterator(); i.hasNext();) {
                f = (File)i.next();
                s = f.getPath();

                if (s.endsWith(".jar") || s.endsWith(".zip")) {
                    installJar(f);
                }
                else if (f.isDirectory()) {
                    // load subdirectories as top-level packages
                    packages.update(f);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkPublicClass(InputStream istream) {
        int check = VmUtils.getAccessModifiers(new DataInputStream(
                new BufferedInputStream(istream)));
        return check != VmUtils.ACC_INVALID
                && (check & VmUtils.ACC_PUBLIC) != 0;
    }

    private static boolean checkImportName(String className) {
        int len = className.length();
        int idx = 0;
        while ((idx = className.indexOf('$', idx)) >= 0) {
            if (idx == len - 1) {
                // disallow classes ending with '$'
                return false;
            }

            idx++;
            if (!Character.isJavaIdentifierStart(className.charAt(idx))) {
                // disallow anonymous classes
                return false;
            }
        }
        return true;
    }

    public void installJar(File jar) throws IOException {
        ZipFile z = new ZipFile(jar);
        ZipEntry e;
        Enumeration en = z.entries();
        String s, pck, name;
        _Package p;
        int idx;
        boolean check = isClassCheckEnabled();

        try {
            while (en.hasMoreElements()) {
                e = (ZipEntry)en.nextElement();
                s = e.toString();
                if (s.endsWith(".class")) {
                    idx = s.lastIndexOf('/');
                    if (idx < 0) {
                        pck = "";
                        name = s.substring(0, s.length() - 6);
                    }
                    else {
                        pck = s.substring(0, idx).replace('/', '.');
                        name = s.substring(idx + 1, s.length() - 6);
                    }

                    // check that entry is a valid, public .class, and include
                    // it if it
                    // is.
                    if (checkImportName(name) &&
                            (!check || checkPublicClass(z.getInputStream(e)))) {
                        // create (or return) the package. This will be
                        // initialized
                        p = createPackage(pck);
                        p.validate();
                        p.contents.add(name);
                    }
                } // endsWith(".class")
            } // while
        }
        finally {
            z.close();
        }
    }

    private _Package createPackage(String pck) {
        _Package p = (_Package)packageMap.get(pck);
        if (p != null) {
            return p;
        }

        int idx = pck.lastIndexOf('.');
        if (idx < 0) {
            return new _Package(packages, pck);
        }
        else {
            return new _Package(createPackage(pck.substring(0, idx)),
                    pck.substring(idx + 1));
        }
    }

    public _Package getPackage(String name) {
        int idx1 = 0;
        int idx2 = name.indexOf('.');
        _Package p = packages;

        while (idx2 > idx1) {
            if ((p = p.getPackage(name.substring(idx1, idx2))) == null) {
                // invalid package
                return null;
            }
            idx1 = idx2 + 1;
            idx2 = name.indexOf('.', idx1);
        }

        return p.getPackage(name.substring(idx1));
    }

    public boolean isClass(String _package, String name) {
        _Package p = getPackage(_package);
        return p != null && p.isMember(name);
    }

    public String[] getClasses(String _package) {
        _Package p = getPackage(_package);
        return p == null ? new String[0] : p.getMembers();
    }

    /*
     * public String[] getAllClasses() { ArrayList l = new ArrayList();
     *
     * Iterator it = packages.keySet().iterator(); String pck; Iterator it2;
     * while (it.hasNext()) { pck = (String)it.next(); it2 =
     * ((HashSet)packages.get(pck)).iterator(); while (it2.hasNext()) {
     * l.add(pck + '.' + it2.next()); } }
     *
     * String[] result = (String[])l.toArray(new String[l.size()]);
     * Arrays.sort(result, java.text.Collator.getInstance()); return result; }
     */
}
