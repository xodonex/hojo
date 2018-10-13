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
package org.xodonex.hojo;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

import org.xodonex.util.Application;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;
import org.xodonex.util.os.OsInterface;
import org.xodonex.util.thread.ThreadUtils;
import org.xodonex.util.tools.PackageManager;

/**
 * Main application. Launches a {@link HojoInterpreter} with or without a
 * {@link HojoShell}.
 */
public class Hojo extends JApplet {

    private static final long serialVersionUID = 1L;

    public final static char CHAR_CODE = '#';
    public final static char CHAR_FILE = '@';
    public final static char CHAR_URL = '$';

    private final static int PM_NORMAL = 0, PM_NONE = 1, PM_EXT = 2;

    // only used when run as an applet
    private HojoShell shell;
    private boolean started = false;

    public Hojo() {
    }

    /**
     * For applet use only.
     */
    @Override
    public void init() {
        shell = new HojoShell(null, null, null, getContentPane(), true);
    }

    /**
     * For applet use only.
     */
    @Override
    public void start() {
        if (!started) {
            shell.start();
        }
    }

    /**
     * For applet use only.
     */
    @Override
    public void destroy() {
        shell.stop();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getContentPane().removeAll();
            }
        });
    }

    public static void main(String[] args) {
        new HojoApp().run(args);
    }

    // the application
    private static class HojoApp extends Application {

        // configuration switches
        private boolean useInput = true;
        private boolean useShell = true;
        private boolean useSwing = true;
        private int PM = PM_NORMAL;
        @SuppressWarnings("unused")
        private boolean sysOut = false;
        private ResourceBundle resource = null;
        private boolean quiet = false;

        // cached input arguments
        private ArrayList input = new ArrayList(4);

        HojoApp() {
            // register the configuration switches
            registerSwitch(new BooleanSwitch("batch", BOOL_YES_NO) {
                @Override
                public boolean doConfig(String value) {
                    if (toBoolean(value)) {
                        quiet = true;
                        useInput = false;
                    }
                    else {
                        quiet = false;
                        useInput = true;
                    }
                    return true;
                }
            });

            registerSwitch(new BooleanSwitch("shell", BOOL_ON_OFF) {
                @Override
                public boolean doConfig(String value) {
                    useShell = toBoolean(value);
                    return true;
                }
            });

            registerSwitch(new BooleanSwitch("swing", BOOL_ON_OFF) {
                @Override
                public boolean doConfig(String value) {
                    useSwing = toBoolean(value);
                    return true;
                }
            });

            registerSwitch(new SimpleSwitch("help") {
                @Override
                public boolean doConfig(String value) {
                    help(null, null);
                    System.exit(0);
                    return false; // dummy
                }
            });

            registerSwitch(new BooleanSwitch("sysOut", BOOL_YES_NO) {
                @Override
                public boolean doConfig(String value) {
                    sysOut = toBoolean(value);
                    return true;
                }
            });

            registerSwitch(new OptionSwitch("PM",
                    new String[] { "on", "off", "ext" }) {
                @Override
                protected boolean doConfig(int idx) {
                    PM = idx;
                    return true;
                }
            });

            registerSwitch(new StringSwitch("resource", "rsrc") {
                @Override
                public boolean doConfig(String value) {
                    try {
                        resource = value == null ? null
                                : new PropertyResourceBundle(
                                        new FileInputStream(value));
                        return true;
                    }
                    catch (Exception e) {
                        return false;
                    }
                }
            });

            registerSwitch(new SimpleSwitch("minimal") {
                @Override
                public boolean doConfig(String value) {
                    PM = PM_NONE;
                    useSwing = false;
                    useShell = false;
                    return true;
                }
            });

            registerSwitch(new SimpleSwitch("quiet") {
                @Override
                public String describe() {
                    return null; // undocumented
                }

                @Override
                public boolean doConfig(String value) {
                    return quiet = true;
                }
            });
        }

        @Override
        protected boolean init(String arg) {
            try {
                switch (arg.charAt(0)) {
                case CHAR_CODE:
                    // Raw input string
                    input.add(arg.substring(1));
                    break;
                case CHAR_FILE:
                    // file input
                    input.add(new File(arg.substring(1)));
                    break;
                case CHAR_URL:
                    // URL input
                    input.add(new URL(arg.substring(1)));
                    break;
                default:
                    // unknown argument
                    return false;
                }
            }
            catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void run0(String[] args) throws Exception {
            try {
                // Ensure proper termination of the VM
                if (useSwing || useShell) {
                    ThreadUtils.startSwing();
                }

                // Initialize the package manager, if not disabled
                if (PM >= PM_NORMAL) {
                    PackageManager.init(PM == PM_EXT);
                }

                // Instantiate the interpreter
                HojoInterpreter ipret = new HojoInterpreter(null, args);

                // initially set the observer to supress input indications and
                // result
                // output. Also, force a high warning level.
                HojoObserver obs = ipret.getObserver();
                obs.pragmaDirective(DefaultHojoObserver.PRAGMA_S_SHOW_INPUT,
                        Boolean.FALSE);
                obs.pragmaDirective(DefaultHojoObserver.PRAGMA_S_SHOW_OUTPUT,
                        Boolean.FALSE);
                obs.pragmaDirective(DefaultHojoObserver.PRAGMA_S_WARN_LEVEL,
                        new Integer(3));

                // Execute each of the input arguments in turn
                Iterator it = input.iterator();
                while (it.hasNext()) {
                    ipret.run(it.next());
                }

                // Continue only if user input should be used
                if (useInput) {
                    if (useShell) {
                        // Start a new shell and wait for it to terminate
                        HojoShell shell = new HojoShell(ipret,
                                ipret.getRuntime(), resource);
                        shell.QUIET = quiet;
                        shell.start();
                        try {
                            shell.waitFor();
                        }
                        catch (InterruptedException e) {
                        }
                    }
                    else {
                        // Set up the observer to show input and output
                        // indications, and to
                        // use the default warning level (1).
                        obs.pragmaDirective(
                                DefaultHojoObserver.PRAGMA_S_SHOW_INPUT,
                                Boolean.TRUE);
                        obs.pragmaDirective(
                                DefaultHojoObserver.PRAGMA_S_SHOW_OUTPUT,
                                Boolean.TRUE);
                        obs.pragmaDirective(
                                DefaultHojoObserver.PRAGMA_S_WARN_LEVEL,
                                new Integer(1));
                        ipret.pragmaDirective(
                                HojoInterpreter.PRAGMA_S_STRICT_TYPES,
                                new Integer(0));

                        // run the interpreter on System.in
                        if (!quiet) {
                            System.out.println(Version.COPYRIGHT);
                        }
                        ipret.run(OsInterface.SYSIN);
                    }
                }
            }
            catch (Throwable t) {
                t.printStackTrace();
            }

            // Wait until anything but Swing has finished
            if (useSwing || useShell) {
                ThreadUtils.waitForSwing();
                System.exit(0);
            }
        }

        @Override
        public void help(String sw, String value) {
            if (sw != null || value != null) {
                super.help(sw, value);
            }
            else {
                System.out.println(createHelpMessage());
            }
        }

        private static String makeDescr(ResourceBundle rsrc, String rsrcName,
                int width, String sw, String indent) {
            return StringUtils.addLineIndent(MessageFormat.format(
                    rsrc.getString(rsrcName),
                    new Object[] { StringUtils.expandRight(sw, ' ', width) }),
                    indent);
        }

        private String createHelpMessage() {
            ResourceBundle rsrc = ResourceBundle.getBundle(
                    "org/xodonex/hojo/resource/Hojo", Locale.getDefault());
            int width = ConvertUtils.toInt(rsrc.getString("width"));
            String indent = new String(StringUtils.fill(' ', width));

            java.util.List args_ = mkHelp(rsrc, "sw.", width, indent);
            args_.add(0, Version.COPYRIGHT);

            args_.add(makeDescr(rsrc, "codeArg.text", width,
                    "" + CHAR_CODE + rsrc.getString("codeArg.name"), indent));
            args_.add(makeDescr(rsrc, "fileArg.text", width,
                    "" + CHAR_FILE + rsrc.getString("fileArg.name"), indent));
            args_.add(makeDescr(rsrc, "urlArg.text", width,
                    "" + CHAR_URL + rsrc.getString("urlArg.name"), indent));
            String[] args = (String[])args_.toArray(new String[args_.size()]);

            // N.B.: MessageFormat.format does not support more than 10
            // arguments!
            return StringUtils.simpleMessageFormat(
                    null, rsrc.getString("help"), args).toString();
        }

    }
}
