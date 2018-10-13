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

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

import org.xodonex.hojo.lang.Statement;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.StringUtils;
import org.xodonex.util.os.OsInterface;
import org.xodonex.util.text.lexer.GenericLexer;

/**
 *
 * @author Henrik Lauritzen
 */
public class DefaultHojoObserver
        implements HojoObserver, GenericLexer.Observer {

    public final static String PRAGMA_S_DO_RECOVERY = "doRecovery";
    public final static String PRAGMA_S_TRACE_LEVEL = "traceLevel";
    public final static String PRAGMA_S_WARN_LEVEL = "warnLevel";
    public final static String PRAGMA_S_WARN_AS_ERROR = "warnAsError";
    public final static String PRAGMA_S_HALT_ON_ERROR = "haltOnError";
    public final static String PRAGMA_S_SHOW_INPUT = "showInput";
    public final static String PRAGMA_S_SHOW_CODE = "showCode";
    public final static String PRAGMA_S_SHOW_OUTPUT = "showOutput";
    public final static String PRAGMA_S_SHOW_TYPES = "showTypes";
    public final static String PRAGMA_S_MAX_STRING = "maxString";
    public final static String PRAGMA_S_MAX_ELEMS = "maxElems";

    protected final static int PRAGMA_DO_RECOVERY = 0;
    protected final static int PRAGMA_TRACE_LEVEL = 1;
    protected final static int PRAGMA_WARN_LEVEL = 2;
    protected final static int PRAGMA_WARN_AS_ERROR = 3;
    protected final static int PRAGMA_HALT_ON_ERROR = 4;
    protected final static int PRAGMA_SHOW_INPUT = 5;
    protected final static int PRAGMA_SHOW_CODE = 6;
    protected final static int PRAGMA_SHOW_OUTPUT = 7;
    protected final static int PRAGMA_SHOW_TYPES = 8;
    protected final static int PRAGMA_MAX_STRING = 9;
    protected final static int PRAGMA_MAX_ELEMS = 10;

    protected final static HashMap pragma = new HashMap(21);
    static {
        pragma.put(PRAGMA_S_DO_RECOVERY, new Integer(PRAGMA_DO_RECOVERY));
        pragma.put(PRAGMA_S_TRACE_LEVEL, new Integer(PRAGMA_TRACE_LEVEL));
        pragma.put(PRAGMA_S_WARN_LEVEL, new Integer(PRAGMA_WARN_LEVEL));
        pragma.put(PRAGMA_S_WARN_AS_ERROR, new Integer(PRAGMA_WARN_AS_ERROR));
        pragma.put(PRAGMA_S_HALT_ON_ERROR, new Integer(PRAGMA_HALT_ON_ERROR));
        pragma.put(PRAGMA_S_SHOW_INPUT, new Integer(PRAGMA_SHOW_INPUT));
        pragma.put(PRAGMA_S_SHOW_CODE, new Integer(PRAGMA_SHOW_CODE));
        pragma.put(PRAGMA_S_SHOW_OUTPUT, new Integer(PRAGMA_SHOW_OUTPUT));
        pragma.put(PRAGMA_S_SHOW_TYPES, new Integer(PRAGMA_SHOW_TYPES));
        pragma.put(PRAGMA_S_MAX_STRING, new Integer(PRAGMA_MAX_STRING));
        pragma.put(PRAGMA_S_MAX_ELEMS, new Integer(PRAGMA_MAX_ELEMS));
    }

    /**
     * Determines whether a warning should be shown. The warning levels are:
     * <ol>
     * <li value="0">No warnings are shown.
     * <li value="1">Warnings for failed constructor, method or field lookup are
     * shown.
     * <li value="2">In addtion to the above, warnings for failed static type
     * check are shown.
     * <li value="3">In addition to the above, warnings for ignored syntax
     * configuration directives are shown.
     * <li value="4">In addtion to the above, warnings for ignored pragma
     * directives are shown.
     * </ol>
     */
    protected int optWarnLevel = 1;

    /**
     * Determines whether the full stack trace of a handled exception or warning
     * will be shown. The trace levels are:
     * <ol>
     * <li value="0">No traces are shown.
     * <li value="1">Traces for runtime errors are shown.
     * <li value="2">Traces for all errors are shown.
     * <li value="3">Traces for all errors and warnings are shown.
     * <li value="4">Traces for all errors and warnings are shown, as well as
     * the target exception.
     * </ol>
     */
    protected int optTraceLevel = 1;

    protected boolean optDoRecovery = false;
    protected boolean optWarnAsError = false;
    protected boolean optHaltOnError = false;
    protected boolean optShowInput = true;
    protected boolean optShowCode = false;
    protected boolean optShowOutput = true;
    protected boolean optShowTypes = true;

    protected Writer out;
    protected PrintWriter warn;
    protected PrintWriter err;

    protected StringUtils.Format fmt;
    protected String inputIndicator = "\n\n> ";
    protected String outputIndent = "";

    protected boolean active = false;
    protected int errors;
    protected int warnings;
    protected int lastWarnings;

    public DefaultHojoObserver(StringUtils.Format fmt) {
        this(fmt, OsInterface.SYSOUT, OsInterface.SYSERR, null);
    }

    public DefaultHojoObserver(StringUtils.Format fmt, Writer out,
            PrintWriter err, PrintWriter warn) {
        this.fmt = (fmt == null)
                ? (StringUtils.Format)StringUtils.defaultFormat.clone()
                : fmt;
        fmt.setFmt(StringUtils.FORMAT_JAVA_TYPED);
        this.out = (out == null) ? OsInterface.NULL : out;
        this.err = (err == null) ? OsInterface.NULL : err;
        this.warn = (warn == null) ? this.err : warn;
    }

    protected void checkNotActive() {
        if (active) {
            throw new HojoException(null, HojoException.ERR_STATE,
                    new String[] { "" }, null);
        }
    }

    @Override
    public synchronized boolean started(Reader in) {
        checkNotActive();
        indicateInput();
        return active = true;
    }

    @Override
    public boolean includeStart(URL url) {
        return true;
    }

    @Override
    public void includeEnd(URL url) {
    }

    @Override
    public void handleWarning(HojoException w) {
        // exit, if the warning level discriminates the warning
        if (optWarnLevel <= 0) {
            return;
        }
        int code = w.getCode();

        int maxLevel;
        switch (optWarnLevel) {
        case 1:
            maxLevel = HojoException.getCode(HojoException.WARN_NONVOID_EXPR);
            break;
        case 2:
            maxLevel = HojoException.getCode(HojoException.WARN_NOTREMOVED);
            break;
        case 3:
            maxLevel = HojoException.getCode(HojoException.WARN_IGNORED);
            break;
        case 4:
            maxLevel = Integer.MAX_VALUE;
            break;
        default:
            maxLevel = 0;
        }

        if (code <= maxLevel) {
            // increase the warning counter and handle the warning
            handleWarning0(w);
        }
    }

    protected void handleWarning0(HojoException w) {
        warnings++;
        lastWarnings++;
        if (optTraceLevel >= 3) {
            w.printStackTrace(warn);
            if (optTraceLevel >= 4) {
                Throwable t = w.getError();
                if (t != null) {
                    t.printStackTrace(warn);
                }
            }
        }
        else {
            warn.println(w);
        }
        warn.println();
        warn.flush();
    }

    @Override
    public boolean handleError(HojoException e) {
        errors++;

        int code = e.getFullCode();
        boolean isRuntime = code == HojoException.ERR_RUNTIME ||
                code == HojoException.ERR_RUNTIME_HOJO;

        if (optTraceLevel >= 2 || (optTraceLevel == 1 && isRuntime)
                || e.isInternal()) {
            if (isRuntime) {
                if (optTraceLevel > 0) {
                    if (optTraceLevel < 4) {
                        err.println(e);
                    }
                    else {
                        e.printStackTrace(err);
                    }
                }
            }
            else {
                e.printStackTrace(err);
            }

            if (optTraceLevel >= 4 || isRuntime || e.isInternal()) {
                Throwable t = e.getError();
                if (t != null) {
                    t.printStackTrace(err);
                }
            }
        }
        else {
            err.println(e);
        }
        err.println();
        err.flush();
        return !optHaltOnError;
    }

    @Override
    public boolean doRecovery(GenericLexer.Recovery rec) {
        if (optDoRecovery) {
            while (rec.exit()) {
                ;
            }
            rec.resync();
        }
        return optDoRecovery;
    }

    @Override
    public void recovered() {
        indicateInput();
    }

    @Override
    public void commandResult() {
        indicateInput();
    }

    @Override
    public void commandResult(Object result) {
        showOutput(result);
        indicateInput();
    }

    @Override
    public boolean pragmaDirective(String id, Object value) {
        Integer ID = (Integer)pragma.get(id);
        if (ID == null) {
            return false;
        }

        switch (ID.intValue()) {
        case PRAGMA_DO_RECOVERY:
            optDoRecovery = ConvertUtils.toBool(value);
            break;
        case PRAGMA_TRACE_LEVEL:
            optTraceLevel = ConvertUtils.toInt(value);
            break;
        case PRAGMA_WARN_LEVEL:
            optWarnLevel = ConvertUtils.toInt(value);
            break;
        case PRAGMA_WARN_AS_ERROR:
            optWarnAsError = ConvertUtils.toBool(value);
            break;
        case PRAGMA_HALT_ON_ERROR:
            optHaltOnError = ConvertUtils.toBool(value);
            break;
        case PRAGMA_SHOW_INPUT:
            optShowInput = ConvertUtils.toBool(value);
            break;
        case PRAGMA_SHOW_CODE:
            optShowCode = ConvertUtils.toBool(value);
            break;
        case PRAGMA_SHOW_OUTPUT:
            optShowOutput = ConvertUtils.toBool(value);
            break;
        case PRAGMA_SHOW_TYPES:
            optShowTypes = ConvertUtils.toBool(value);
            fmt.setTypeFmt(optShowTypes ? StringUtils.FORMAT_TYPED : 0);
            break;
        case PRAGMA_MAX_STRING:
            fmt.setStringLimit(ConvertUtils.toInt(value));
            break;
        case PRAGMA_MAX_ELEMS:
            fmt.setArrayLimit(ConvertUtils.toInt(value));
            break;
        default:
            throw new HojoException();
        }
        return true;
    }

    @Override
    public int listDirectives(Collection names, Collection types,
            Collection comments) {
        ResourceBundle rsrc = ResourceBundle.getBundle(
                "org/xodonex/hojo/resource/DefaultHojoObserver",
                Locale.getDefault());
        return listDirectives(names, types, comments, rsrc,
                new String[] {
                        PRAGMA_S_DO_RECOVERY,
                        PRAGMA_S_TRACE_LEVEL,
                        PRAGMA_S_WARN_LEVEL,
                        PRAGMA_S_WARN_AS_ERROR,
                        PRAGMA_S_HALT_ON_ERROR,
                        PRAGMA_S_SHOW_INPUT,
                        PRAGMA_S_SHOW_CODE,
                        PRAGMA_S_SHOW_OUTPUT,
                        PRAGMA_S_SHOW_TYPES,
                        PRAGMA_S_MAX_STRING,
                        PRAGMA_S_MAX_ELEMS
                },
                new Class[] {
                        Boolean.class,
                        Integer.class,
                        Integer.class,
                        Boolean.class,
                        Boolean.class,
                        Boolean.class,
                        Boolean.class,
                        Boolean.class,
                        Boolean.class,
                        Integer.class,
                        Integer.class
                });
    }

    protected int listDirectives(Collection names, Collection types,
            Collection comments, ResourceBundle rsrc, String[] defNames,
            Class[] defTypes) {
        for (int i = 0; i < defNames.length; i++) {
            names.add(defNames[i]);
            types.add(defTypes[i]);
            comments.add(rsrc.getString(defNames[i]));
        }
        return defNames.length;
    }

    @Override
    public void commandRead() {
    }

    @Override
    public boolean commandExecute(Statement stm) {
        if (optWarnAsError && lastWarnings > 0) {
            lastWarnings = 0;
            return false;
        }
        else {
            lastWarnings = 0;
            commandExecute0(stm);
            return true;
        }
    }

    protected void commandExecute0(Statement stm) {
        if (optShowCode) {
            try {
                fmt.setTypeFmt(0);
                out.write(stm.toString(HojoSyntax.DEFAULT, fmt, "") + '\n');
                out.flush();
            }
            catch (Throwable t) {
            }
            finally {
                if (optShowTypes) {
                    fmt.setTypeFmt(StringUtils.FORMAT_TYPED);
                }
            }
        }
    }

    @Override
    public boolean commandExecute(String[] cmds) {
        if (optWarnAsError && lastWarnings > 0) {
            lastWarnings = 0;
            return false;
        }
        else {
            lastWarnings = 0;
            commandExecute0(cmds);
            return true;
        }
    }

    protected void commandExecute0(String[] cmds) {
    }

    @Override
    public synchronized void finished() {
        active = false;
        notifyAll();
    }

    /**
     * @return the formatting object that is used for output.
     */
    public StringUtils.Format getFormat() {
        return fmt;
    }

    public void setFormat(StringUtils.Format format) {
        if (format == null) {
            throw new NullPointerException();
        }
        this.fmt = format;
    }

    public void setOutputWriter(Writer writer) {
        if (writer == null) {
            writer = OsInterface.NULL;
        }
        out = writer;
    }

    @Override
    public Writer getOutputWriter() {
        return out;
    }

    public void setErrorWriter(PrintWriter writer) {
        if (writer == null) {
            writer = OsInterface.NULL;
        }
        err = writer;
    }

    @Override
    public PrintWriter getErrorWriter() {
        return err;
    }

    public void setWarningWriter(PrintWriter writer) {
        if (writer == null) {
            writer = err;
        }
        warn = writer;
    }

    @Override
    public PrintWriter getWarningWriter() {
        return warn;
    }

    public void setInputIndicator(String indicator) {
        inputIndicator = indicator;
    }

    public void setOutputIndent(String indent) {
        outputIndent = indent;
    }

    public void indicateInput() {
        if (optShowInput) {
            try {
                out.write(inputIndicator);
                out.flush();
            }
            catch (Throwable t) {
            }
            ;
        }
    }

    public void showOutput(Object output) {
        if (!optShowOutput || out == OsInterface.NULL) {
            return;
        }

        String s = formatOutput(output);
        try {
            out.write(s);
            out.flush();
        }
        catch (Throwable t) {
        }
        ;
    }

    public String formatOutput(Object output) {
        return StringUtils.any2String(output, fmt, outputIndent);
    }

    public synchronized boolean isFinished() {
        return !active;
    }

    public synchronized void waitForFinished() throws InterruptedException {
        if (active) {
            wait();
        }
    }

    public int getErrorCount() {
        return errors;
    }

    public int getWarningCount() {
        return warnings;
    }

    @Override
    public void reset() {
        errors = warnings = lastWarnings = 0;
    }

}
