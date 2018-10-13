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
package org.xodonex.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Abstract base class for stand-alone applications with command-line parsing.
 */
public abstract class Application {

    protected final static int BOOL_ON_OFF = 1,
            BOOL_YES_NO = 2,
            BOOL_TRUE_FALSE = 0;

    /**
     * Obtain a boolean value from a string.
     *
     * @param value
     *            the input value
     * @return the converted value
     */
    public static boolean toBoolean(String value) {
        if (value == null) {
            return true;
        }
        else if (value.equals("yes") || value.equals("on")) {
            return true;
        }
        else {
            return Boolean.valueOf(value).booleanValue();
        }
    }

    /**
     * Remove a leading backslash escape character from a string.
     *
     * @param argument
     *            the argument
     * @return the argument with escape-characters removed.
     */
    public static String decodeEscaped(String argument) {
        if (argument.length() > 0 && argument.charAt(0) == '\\') {
            return argument.substring(1);
        }
        else {
            return argument;
        }
    }

    private SortedMap _switches = new TreeMap(java.text.Collator.getInstance());

    /**
     * Constructor.
     */
    public Application() {
    }

    /**
     * The main routine for this application.
     *
     * @param args
     *            the command-line arguments
     */
    public final synchronized void run(String[] args) {
        try {
            List extraParams = new ArrayList(args.length);

            int i;
            String optPrefix = getOptionPrefix(),
                    valIndicator = getOptionValueIndicator();

            // parse the switches
            for (i = 0; i < args.length; i++) {
                if (!args[i].startsWith(optPrefix)) {
                    break;
                }
                String s = args[i].substring(optPrefix.length());

                String value;
                int idx = s.indexOf(valIndicator);
                if (idx >= 0) {
                    value = s.substring(idx + valIndicator.length());
                    s = s.substring(0, idx);
                }
                else {
                    value = null;
                }

                Switch sw = (Switch)_switches.get(s);
                if (sw == null || !sw.doConfig(value)) {
                    help(s, value);
                    return;
                }
            }

            // parse any additional argument which is accepted by the init
            // routine
            for (; i < args.length; i++) {
                if (!init(args[i])) {
                    break;
                }
            }

            // any additional parameters are delivered to run()
            for (; i < args.length; i++) {
                extraParams.add(decodeEscaped(args[i]));
            }

            // run the application
            run0((String[])extraParams
                    .toArray(new String[extraParams.size()]));
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Generate the command-line help when an invalid command-line switch is
     * used.
     *
     * @param sw
     *            the unknown/invalid command-line switch
     * @param value
     *            the value of the switch
     */
    public void help(String sw, String value) {
        System.err.println("invalid switch " + getOptionPrefix() + sw +
                (value == null ? "" : getOptionValueIndicator() + value));
    }

    /**
     * This method is invoked from {@link #run(String[])} after the original
     * argument list has been decoded.
     *
     * @param args
     *            the excess arguments from the command line
     * @throws Exception
     *             if a runtime error occurs
     */
    protected abstract void run0(String[] args) throws Exception;

    /**
     * @return the command-line switches
     */
    protected SortedMap getSwitches() {
        return _switches;
    }

    /**
     * Add a new command-line switch.
     *
     * @param s
     *            the switch to be added
     */
    protected void registerSwitch(Switch s) {
        _switches.put(s.getName(), s);
    }

    /**
     * @return the prefix that indicates an argument is an option (switch)
     */
    protected String getOptionPrefix() {
        return "--";
    }

    /**
     * @return the separator between option (switch) names and values
     */
    protected String getOptionValueIndicator() {
        return "=";
    }

    /**
     * Generate a command-line help description.
     *
     * @param rsrc
     *            the resource bundle for localization
     * @param prefix
     *            the prefix (header) for the generated output
     * @param expandWidth
     *            the width (number of spaces) used in the first column
     * @param lineIndent
     *            the indent used for each line of description
     * @return a list of text lines of the help descrption.
     */
    protected List mkHelp(ResourceBundle rsrc, String prefix,
            int expandWidth, String lineIndent) {
        ArrayList result = new ArrayList(_switches.size());

        for (Iterator i = _switches.values().iterator(); i.hasNext();) {
            Switch sw = (Switch)i.next();
            String descr = sw.describe();
            if (descr == null) {
                continue;
            }
            descr = MessageFormat.format(rsrc.getString(prefix + sw.getName()),
                    new Object[] {
                            StringUtils.expandRight(descr, ' ', expandWidth) });

            if (lineIndent != null) {
                result.add(StringUtils.addLineIndent(descr, lineIndent));
            }
            else {
                result.add(descr);
            }
        }

        return result;
    }

    /**
     * This is invoked from {@link #run(String[])} on the arguments following
     * the recognized switches. The first argument for which this method returns
     * <code>false</code> - if any - will be the first argument to
     * {@link #run0(String[])}.
     *
     * @param arg
     *            the argument
     * @return true if this has special significance to the application, false
     *         if not
     */
    protected boolean init(String arg) {
        return false;
    }

    /**
     * Representation for command-line switches.
     */
    public static interface Switch {
        /**
         * Configure this switch based on an input value.
         *
         * @param value
         *            the input value
         * @return whether the input value was acceptable
         */
        public boolean doConfig(String value);

        /**
         * @return the name (syntax) of this switch.
         */
        public String getName();

        /**
         * @return the help description for this switch.
         */
        public String describe();
    }

    /**
     * Base class for switches.
     */
    public abstract class SimpleSwitch implements Switch {
        private String _name;

        public SimpleSwitch(String name) {
            _name = name;
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public String describe() {
            return getOptionPrefix() + getName();
        }
    }

    /**
     * A switch accepting boolean values.
     */
    public abstract class BooleanSwitch extends SimpleSwitch {
        private int _format;

        public BooleanSwitch(String name, int format) {
            super(name);
            _format = format;
        }

        @Override
        public String describe() {
            String options;
            switch (_format) {
            case BOOL_ON_OFF:
                options = "on|off";
                break;
            case BOOL_YES_NO:
                options = "yes|no";
                break;
            default: // BOOL_TRUE_FALSE
                options = "true|false";
            }

            return super.describe() + getOptionValueIndicator() + "[" + options
                    + "]";
        }
    }

    /**
     * A switch accepting one out of a fixed number of options.
     */
    public abstract class OptionSwitch extends SimpleSwitch {
        private String[] _options;

        public OptionSwitch(String name, String[] options) {
            super(name);
            _options = options;
        }

        @Override
        public String describe() {
            StringBuffer buf = new StringBuffer(super.describe());

            if (_options.length > 0) {
                buf.append(getOptionValueIndicator()).append('[');
                for (int i = 0; i < _options.length; i++) {
                    buf.append(_options[i]);
                    if (i < _options.length - 1) {
                        buf.append('|');
                    }
                }
                buf.append(']');
            }

            return buf.toString();
        }

        @Override
        public final boolean doConfig(String value) {
            if (value == null) {
                return doConfig(0);
            }

            for (int i = _options.length - 1; i >= 0; i--) {
                if (value.equals(_options[i])) {
                    return doConfig(i);
                }
            }
            return false;
        }

        protected abstract boolean doConfig(int option);
    }

    /**
     * A switch which accepts an arbitrary string.
     */
    public abstract class StringSwitch extends SimpleSwitch {
        private String _optionName;

        public StringSwitch(String name, String optionName) {
            super(name);
            _optionName = optionName;
        }

        @Override
        public String describe() {
            return super.describe() + getOptionValueIndicator() + "<"
                    + _optionName + ">";
        }
    }

}
