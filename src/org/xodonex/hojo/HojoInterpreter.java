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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.xodonex.hojo.lang.Const;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.Variable;
import org.xodonex.hojo.lang.env.BaseEnv;
import org.xodonex.hojo.lang.stm.BlockStatement;
import org.xodonex.hojo.lang.stm.NOP;
import org.xodonex.hojo.lib.StdLib;
import org.xodonex.hojo.util.ClassLoaderAction;
import org.xodonex.hojo.util.ReturnException;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;
import org.xodonex.util.os.AsyncProcess;
import org.xodonex.util.os.OsInterface;
import org.xodonex.util.tools.PackageManager;

/**
 * An interpreter instance, capable of interpreting the full Hojo language: the
 * interpreter handles the meta language (include/macro processing etc.),
 * delegates to the {@link HojoCompiler} for the remaining (primary) language,
 * and directly interprets (executes) the compiler-generated ASTs.
 *
 */
public class HojoInterpreter implements PragmaListener, HojoConst {

    public final static String PRAGMA_S_STRICT_TYPES = "strictTypes",
            PRAGMA_S_KILL_DELAY = "killDelay",
            PRAGMA_S_TERM_MSG = "termMsg";

    // Pragma directive lookup
    private final static int PRAGMA_STRICT_TYPES = 0,
            PRAGMA_KILL_DELAY = 1,
            PRAGMA_TERM_MSG = 2,
            PRAGMA_DEBUG = 3;
    private final static String PRAGMA_S_DEBUG = "debug";

    private final static HashMap pragma = new HashMap(11);
    static {
        pragma.put(PRAGMA_S_STRICT_TYPES, new Integer(PRAGMA_STRICT_TYPES));
        pragma.put(PRAGMA_S_KILL_DELAY, new Integer(PRAGMA_KILL_DELAY));
        pragma.put(PRAGMA_S_TERM_MSG, new Integer(PRAGMA_TERM_MSG));
        pragma.put(PRAGMA_S_DEBUG, new Integer(PRAGMA_DEBUG));
    }

    private final HojoSyntax stx;
    private final HojoLexer lex;
    private final HojoCompiler comp;
    private final OsInterface os;
    private final StdLib lib;

    // the runtime environment in which the interpretation is run
    private HojoRuntime runtime;

    // a compiler environment for the runtime
    private BaseEnv baseEnv;

    // the observer which receives notifications
    private HojoObserver obs = null;

    // The base URL from which the script was started.
    private URL baseURL;

    // the last command result
    private Object lastResult = null;

    // state variables configurable by pragma directives
    private long killDelay = 1000;
    private String termMsg = "Process terminated. Exit code: {0}";

    public HojoInterpreter() {
        this(null, null);
    }

    public HojoInterpreter(HojoSyntax stx) {
        this(stx, null);
    }

    public HojoInterpreter(HojoSyntax stx, String[] args) {
        this.comp = new HojoCompiler(stx);
        this.stx = comp.getSyntax();
        lex = comp.getLexer();
        this.stx.configureMetaSyntax(lex, this);
        lib = this.stx.getStdLib();

        // Set the args literal
        this.stx.setArgs(lex, args == null ? new String[0] : args);

        // Get the OsInterface, or create a new one if
        // no interface has been configured in the syntax.
        Object[] cfg = lex.getConfig(this.stx.standardLiterals[3]);

        OsInterface os_ = null;
        if (cfg != null) {
            os_ = (cfg[1] instanceof Const)
                    ? (OsInterface)((Const)cfg[1]).xeq(null)
                    : (OsInterface)cfg[1];
        }
        os = (os_ == null) ? new OsInterface() : os_;

        setObserver(new DefaultHojoObserver(
                (StringUtils.Format)comp.getStandardFormat().clone(),
                OsInterface.SYSOUT, OsInterface.SYSERR, OsInterface.SYSOUT));
        setup(new HojoRuntime());
    }

    public HojoCompiler getCompiler() {
        return comp;
    }

    public OsInterface getOsInterface() {
        return os;
    }

    public synchronized BaseEnv getBaseEnv() {
        return baseEnv;
    }

    public synchronized Object getLastResult() {
        return lastResult;
    }

    public synchronized Object setLastResult(Object result) {
        Object o = lastResult;
        lastResult = result;
        return o;
    }

    public synchronized HojoObserver getObserver() {
        return obs;
    }

    public synchronized HojoObserver setObserver(HojoObserver obs) {
        if (obs == null) {
            obs = new SilentHojoObserver();
        }

        if (obs != this.obs) {
            HojoObserver result = this.obs;
            lex.setObserver(obs);
            this.obs = obs;
            return result;
        }
        else {
            return obs;
        }
    }

    public synchronized void reset() {
        comp.reset();
        runtime = null;
        obs = null;
        baseEnv.clear();
        baseEnv = null;
        baseURL = null;
        lastResult = null;
    }

    @Override
    public boolean pragmaDirective(String name, Object value)
            throws HojoException {
        return setPragma(true, name, value);
    }

    @Override
    public int listDirectives(Collection names, Collection types,
            Collection comments) {
        return listDirectives(names, types, comments,
                new String[] {
                        PRAGMA_S_STRICT_TYPES,
                        PRAGMA_S_KILL_DELAY,
                        PRAGMA_S_TERM_MSG },
                new Class[] {
                        Integer.class,
                        Long.class,
                        String.class
                });
    }

    private int listDirectives(Collection names, Collection types,
            Collection comments, String[] defNames, Class[] defTypes) {
        ResourceBundle rsrc = ResourceBundle.getBundle(
                "org/xodonex/hojo/resource/HojoInterpreter",
                Locale.getDefault());
        for (int i = 0; i < defNames.length; i++) {
            names.add("HOJO " + defNames[i]);
            types.add(defTypes[i]);
            comments.add(rsrc.getString(defNames[i]));
        }

        return defNames.length;
    }

    private boolean noPragma(String name, boolean isRemoveWarning) {
        if (obs != null) {
            obs.handleWarning(
                    new HojoException(null,
                            isRemoveWarning ? HojoException.WARN_NOTREMOVED
                                    : HojoException.WARN_IGNORED,
                            new String[] { name }, lex.currentLocation()));
            obs.commandResult();
        }
        return false;
    }

    // execute a pragma directive
    public synchronized boolean setPragma(boolean internal, String name,
            Object value) {
        if (internal) {
            Integer code = (Integer)pragma.get(name);
            if (code == null) {
                // unknown directive
                if (obs != null) {
                    obs.handleWarning(new HojoException(null,
                            HojoException.WARN_IGNORED, new String[] { name },
                            lex.currentLocation()));
                    obs.commandResult();
                }
                return false;
            }

            try {
                switch (code.intValue()) {
                case PRAGMA_STRICT_TYPES:
                    comp.strictTypeCheck(ConvertUtils.toInt(value));
                    obs.commandResult();
                    return true;
                case PRAGMA_KILL_DELAY:
                    long l = ConvertUtils.toLong(value);
                    killDelay = l < 0 ? 0 : l;
                    obs.commandResult();
                    return true;
                case PRAGMA_TERM_MSG:
                    termMsg = ConvertUtils.toString(value);
                    obs.commandResult();
                    return true;
                case PRAGMA_DEBUG:
                    Writer w = (Writer)value;
                    w = lex.setDebugWriter(w);
                    if (w != null) {
                        w.close();
                    }
                    obs.commandResult(w);
                    return true;
                default:
                    return noPragma(name, false);
                }
            }
            catch (Throwable t) {
                throw new HojoException(t, HojoException.ERR_VALUE,
                        new String[] { "" + value }, lex.currentLocation());
            }
        }
        else {
            try {
                if (obs != null) {
                    if (!obs.pragmaDirective(name, value)) {
                        return noPragma(name, false);
                    }
                    else {
                        obs.commandResult();
                        return true;
                    }
                }
                return false;
            }
            catch (Throwable t) {
                throw new HojoException(t, HojoException.ERR_VALUE,
                        new String[] { "" + value }, lex.currentLocation());
            }
        }
    }

    /**
     * Interpret the given source.
     *
     * @param source
     *            the source to be interpreted (may be a file, an URL, reader,
     *            input stream or string).
     * @return false if the interpreter terminates prematurely, either due to an
     *         error or because the observer rejects the source.
     */
    public synchronized boolean run(Object source) throws HojoException {
        synchronized (comp) {
            // Create an appropriate input stream
            Reader sourceReader;
            try {
                // Get the source, and set the base location
                if (source instanceof File) {
                    source = ((File)source).toURI().toURL();
                }
                if (source instanceof URL) {
                    baseURL = (URL)source;
                    if (obs.includeStart(baseURL)) {
                        try {
                            lex.include(baseURL);
                        }
                        catch (Exception e) {
                            obs.includeEnd(baseURL);
                            throw e;
                        }
                        sourceReader = lex.getInput().getActiveReader();
                    }
                    else {
                        return false;
                    }
                }
                else {
                    baseURL = OsInterface.HOME_URL;
                    if (source instanceof Reader) {
                        sourceReader = (Reader)source;
                    }
                    else if (source instanceof InputStream) {
                        sourceReader = new InputStreamReader(
                                (InputStream)source);
                    }
                    else {
                        sourceReader = new StringReader(
                                ConvertUtils.toString(source)
                                        + comp.getSyntax().punctuators[0]);
                    }
                    lex.include(sourceReader);
                }

                // Indicate that the interpretation is in progress
                if (!obs.started(sourceReader)) {
                    lex.getInput().closeAll();
                    return false;
                }
            }
            catch (Throwable t) {
                // Clean up and rethrow the exception
                try {
                    lex.getInput().closeAll();
                }
                catch (IOException e) {
                }
                throw HojoException.wrap(t);
            }

            // interpret!
            Statement stm;
            boolean result = true;
            try {
                while (lex.ttype != TT_EOF) {
                    if ((stm = interpret()) != null) {
                        execute(stm);
                    }
                }
            }
            catch (HojoException e) {
                // terminated by a fatal error
                result = false;
            }

            baseEnv.clear();
            stm = null;
            try {
                lex.getInput().closeAll();
            }
            catch (IOException e) {
            }
            obs.finished();
            return result;
        } // synchronized
    }

    /**
     * Initializes this interpreter to use the given runtime environment. This
     * must be done prior to any call to {@link #interpret()} or
     * {@link #execute(Statement)}.
     *
     * @param runtime
     *            the runtime data to be used.
     */
    public synchronized void setup(HojoRuntime runtime) {
        if (this.runtime == runtime) {
            return;
        }

        this.runtime = runtime;
        baseEnv = new BaseEnv(runtime);
    }

    /**
     * @return the currently used runtime object.
     */
    public synchronized HojoRuntime getRuntime() {
        return runtime;
    }

    /**
     * @return whether more input is available to the interpreter.
     */
    public synchronized boolean hasMoreInput() {
        return lex.ttype != TT_EOF;
    }

    /**
     * Interprets one directive or statement.
     *
     * @return the compiled statement. If the return value is <code>null</code>
     *         this implies that a directive was interpreted, that an an error
     *         was encountered or that the end of the input was reached. Use
     *         {@link #hasMoreInput()} to determine whether the end of the input
     *         has been reached.
     * @exception HojoException
     *                If the error handler determines that an error is fatal, it
     *                will be rethrown from this method
     * @see #setup(HojoRuntime)
     */
    public synchronized Statement interpret() throws HojoException {
        synchronized (comp) {
            return interpret(0);
        }
    }

    // skip one block until the next #elif / #else / #endif
    private void skipBlock() {
        while (true) {
            if (lex.nextToken(TT_ANY) == TT_META) {
                switch (lex.id) {
                case META_IF:
                    skipBlock();
                    continue;
                // case META_ELIF:
                // case META_ELSE:
                case META_ENDIF:
                    // lex.pushBack();
                    return;
                }
            }
            else if (lex.ttype == TT_EOF) {
                throw new HojoException(null, HojoException.ERR_EOF,
                        null, lex.currentLocation());
            }
            lex.nextLine();
        }
    }

    private Statement interpret(int ifDepth) {
        String s = null;
        Expression expr = null;
        Object obj = null;

        // Resynchronize baseEnv with runtime,
        // ie. remove any compiler-created variables in env.
        baseEnv.clear();
        ArrayList stms = new ArrayList();

        while (true) {
            try {
                // ignore all leading separators
                while (lex.nextToken(TT_ANY) == PCT_SEPARATOR) {
                    ;
                }

                // Interpret the next metacommand or statement
                if (lex.ttype == TT_META) {
                    // metacommand
                    obs.commandRead();
                    switch (lex.id) {
                    case META_IF:
                        lex.nextToken(PCT_LPAREN);
                        expr = comp.compileExpr(baseEnv, obs);
                        lex.nextToken(PCT_RPAREN);
                        if (ConvertUtils.toBool(expr.xeq(baseEnv.getLink()))) {
                            ifDepth++;
                            continue;
                        }
                        else {
                            skipBlock();
                        }
                        if (ifDepth == 0) {
                            obs.commandResult();
                        }
                        break;
                    case META_ENDIF:
                        if (ifDepth == 0) {
                            obs.handleError(new HojoException(null,
                                    HojoException.ERR_METASYNTAX, new String[] {
                                            "" + stx.META
                                                    + stx.metaSyntax[META_ENDIF
                                                            - META_BASE_ID]
                                    }, lex.currentLocation()));
                        }
                        else {
                            ifDepth--;
                        }
                        break;
                    case META_DEFINE:
                        lex.parseToken(TT_WORD, true);
                        s = lex.sval;
                        expr = comp.compileExpr(baseEnv, obs);
                        String contents = ConvertUtils
                                .toString(expr.xeq(baseEnv.getLink()));
                        if (s.equals(contents)) {
                            throw new HojoException(null,
                                    HojoException.ERR_CIRCULAR_MACRO,
                                    new String[] { s }, lex.currentLocation());
                        }

                        // if s is contained in contents, then this will be
                        // caught when the
                        // macro is substituted
                        comp.addMacro(s, contents);
                        lex.nextToken(PCT_SEPARATOR);
                        obs.commandResult(lastResult = contents);
                        break;
                    case META_UNDEF:
                        lex.parseToken(TT_WORD, true);
                        s = lex.sval;
                        lex.nextToken(PCT_SEPARATOR);
                        if (!comp.removeMacro(s)) {
                            warnRemove(s);
                        }
                        obs.commandResult();
                        break;
                    case META_IMPORT:
                        obs.commandResult(lastResult = doImport());
                        break;
                    case META_EXPORT:
                        obs.commandResult(lastResult = doExport());
                        break;
                    case META_PACKAGE:
                        lex.parseToken(TT_WORD, true);
                        s = lex.sval;
                        lex.nextToken(PCT_SEPARATOR);
                        comp.addPrefix(s);
                        obs.commandResult();
                        break;
                    case META_NOPACKAGE:
                        obs.commandResult(
                                lastResult = removeSymbol(false, TT_NOTHING));
                        break;
                    case META_DECLARE:
                        s = lex.nextRawToken(false, false);
                        expr = comp.compileExpr(baseEnv, obs);
                        lex.nextToken(PCT_SEPARATOR);
                        obj = expr.xeq(baseEnv.getLink());
                        comp.addLiteral(s, obj);
                        obs.commandResult(lastResult = obj);
                        break;
                    case META_UNDECLARE:
                        obs.commandResult(
                                lastResult = removeSymbol(true, TT_LITERAL));
                        break;
                    case META_LOAD:
                        expr = comp.compileExpr(baseEnv, obs);
                        lex.nextToken(PCT_SEPARATOR);
                        obj = expr.xeq(baseEnv.getLink());
                        obs.commandResult(lastResult = comp.doLoad(obj));
                        break;
                    case META_UNLOAD:
                        expr = comp.compileExpr(baseEnv, obs);
                        lex.nextToken(PCT_SEPARATOR);
                        obj = expr.xeq(baseEnv.getLink());
                        obs.commandResult(lastResult = comp.doUnload(obj));
                        break;
                    case META_LEFT:
                    case META_RIGHT:
                    case META_OP:
                        boolean leftAssoc = lex.id == META_LEFT;
                        boolean rightAssoc = lex.id == META_RIGHT;
                        int prio;
                        lex.nextToken(TT_NUMBER);
                        if (lex.oval == null) {
                            // parse the number, if it has not been parsed
                            // already
                            prio = lex.parseNumber('\0', false).intValue();
                        }
                        else {
                            prio = (int)lex.nval;
                        }
                        obs.commandResult(lastResult = addOperator(baseEnv,
                                prio, leftAssoc, rightAssoc));
                        break;
                    case META_NOP:
                        obs.commandResult(
                                lastResult = removeSymbol(true, TT_OPERATOR));
                        break;
                    case META_REMOVE:
                        lex.nextToken(TT_WORD, true);
                        s = lex.sval;
                        lex.nextToken(PCT_SEPARATOR);
                        lastResult = removeVar(s);
                        obs.commandResult(lastResult);
                        break;
                    case META_PRAGMA:
                        lex.parseToken(TT_WORD, true);
                        boolean internal = (s = lex.sval).equals("HOJO");
                        if (internal) {
                            lex.parseToken(TT_WORD, true);
                            s = lex.sval;
                        }
                        expr = comp.compileExpr(baseEnv, obs);
                        lex.nextToken(PCT_SEPARATOR);
                        setPragma(internal, s, expr.xeq(baseEnv.getLink()));
                        // setPragma already calls commandResult()
                        break;
                    case META_INCLUDE:
                        expr = comp.compileExpr(baseEnv, obs);
                        lex.nextToken(PCT_SEPARATOR);

                        obj = expr.xeq(baseEnv.getLink());
                        if (obj instanceof File) {
                            obj = ((File)obj).toURI().toURL();
                        }
                        else if (obj instanceof InputStream) {
                            obj = new InputStreamReader((InputStream)obj);
                        }

                        if (obj instanceof URL) {
                            lex.include((URL)obj);
                        }
                        else if (obj instanceof Reader) {
                            lex.include((Reader)obj);
                        }
                        else {
                            lex.include(ConvertUtils.toString(obj), true);
                        }
                        continue;
                    case META_EXIT:
                        lex.nextToken(PCT_SEPARATOR);
                        lex.exit();
                        continue;
                    default:
                        // won't happen
                        throw new HojoException();
                    }
                } // TT_META
                else if (lex.ttype == stx.META) {
                    if (lex.peek() == '?') {
                        // help command
                        lex.resync();
                        String clsId = lex.nextRawToken();
                        String pattern = lex.nextRawToken(false, true);

                        Class cls;
                        Object[] cfg = lex.getConfig(clsId);
                        int ttyp = cfg != null ? ((Integer)cfg[0]).intValue()
                                : TT_UNDEFINED;

                        if (ttyp == TT_TYPE) {
                            // type identifier - use the denoted class
                            if (cfg[2] instanceof ClassLoaderAction) {
                                ((ClassLoaderAction)cfg[2]).invoke(lex);
                                cls = (Class)lex.oval;
                            }
                            else {
                                cls = (Class)cfg[1];
                            }
                        }
                        else if (ttyp == TT_LITERAL) {
                            // literal - use the class of the value
                            obj = cfg[1] instanceof Expression
                                    ? ((Expression)cfg[1]).xeq(null)
                                    : cfg[1];
                            cls = obj == null ? Object.class : obj.getClass();
                        }
                        else {
                            Type[] typ = new Type[1];
                            if (runtime.findVar(clsId, typ) != null) {
                                // variable - use the defined type
                                cls = typ[0].toClass();
                            }
                            else {
                                // convert to class
                                cls = ConvertUtils.toClass(clsId);
                            }
                        }

                        lex.nextToken(PCT_SEPARATOR);
                        StringBuffer sbuf = (StringBuffer)lib.format.invoke(
                                new Object[] { lib.help.invoke(
                                        new Object[] { cls,
                                                ConvertUtils.toPattern(pattern),
                                                new Integer(Integer.MAX_VALUE),
                                                ConvertUtils.ZERO_INT,
                                                ConvertUtils.ZERO_INT,
                                                ConvertUtils.ZERO_INT }),
                                        "\n", "" });

                        Writer w = obs.getOutputWriter();
                        w.write(sbuf.toString());
                        w.flush();
                        obs.commandResult();
                    }
                    else {
                        // OS command
                        ArrayList l = new ArrayList(5);
                        while ((s = lex.nextRawToken(true, true)) != null) {
                            l.add(s);
                        }
                        String[] cmds = (String[])l
                                .toArray(new String[l.size()]);
                        if (obs.commandExecute(cmds)) {
                            final AsyncProcess p = os.exec(cmds,
                                    obs.getOutputWriter(),
                                    obs.getErrorWriter());
                            p.setEndObserver(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Writer w = obs.getOutputWriter();
                                        w.write("\n" + MessageFormat
                                                .format(termMsg, new Object[] {
                                                        "" + p.waitAndGetResult() })
                                                + '\n');
                                        w.flush();
                                    }
                                    catch (Exception e) {
                                    }
                                }
                            });
                            while ((s = lex.nextLine()).length() > 0) {
                                p.inputln(s.charAt(0) == '\\' ? s.substring(1)
                                        : s);
                            }
                            p.closeInput();
                            if (!p.isFinished()) {
                                // EOF might not be detected by the input
                                // process. Give it
                                // some to terminate on its own, then kill it!
                                try {
                                    p.join(killDelay);
                                }
                                catch (Exception e) {
                                }
                                p.kill();
                            }
                            obs.commandResult(
                                    lastResult = p.waitAndGetOutput());
                        }
                    }
                } // stx.META
                else if (lex.ttype == TT_EOF) {
                    // no more input - done
                    break;
                } // TT_EOF
                else { // ordinary statement.
                       // compile the statement and return it
                    obs.commandRead();
                    lex.pushBack();
                    Statement stm = comp.compileStm(baseEnv, obs);
                    if (stm != null && stm != NOP.NOP) {
                        stms.add(stm);
                    }
                }
            }
            catch (Throwable t) {
                if (t != HojoLexer.ALREADY_HANDLED) {
                    // handle errors which are not already handled by the
                    // lexer.
                    HojoException de = HojoException.wrap(t);
                    if (!obs.handleError(de)) {
                        // fatal error
                        throw de;
                    }
                }

                // try to recover from the syntax error
                if (!obs.doRecovery(lex.asRecovery())) {
                    comp.doRecovery();
                }
                obs.recovered();
            }

            if (ifDepth == 0) {
                break;
            }
        } // while

        int sz;
        switch (sz = stms.size()) {
        case 0:
            return null;
        case 1:
            return (Statement)stms.get(0);
        default:
            return new BlockStatement(
                    (Statement[])stms.toArray(new Statement[sz]));
        }
    }

    public synchronized void execute(Statement stm) {
        // execute the compiled statement. Update the last result, if the
        // statement has a value
        try {
            if (obs.commandExecute(stm)) {
                if (stm.hasValue()) {
                    obs.commandResult(lastResult = stm.xeq(runtime));
                }
                else {
                    stm.xeq(runtime);
                    obs.commandResult();
                }
            }
        }
        catch (ReturnException e) {
            // return statement - close the current input
            lex.exit();
            lex.resync();
        }
        catch (Throwable t) {
            // runtime error - handle it but do not use error recovery
            if (!obs.handleError(HojoException.wrap(t))) {
                // fatal error
                throw HojoException.wrap(t);
            }
            obs.recovered();
        }
    }

    public synchronized Object removeVar(String name) {
        if (name.equals(stx.reserved[RES_THIS - RES_BASE_ID])) {
            Map m = new HashMap(2 * runtime.size());
            m.putAll(runtime.asMap());
            baseEnv.clear();
            runtime.clear();
            return m;
        }
        else {
            Variable v = runtime.delete(name);
            return (v == null) ? null : v.getValue();
        }
    }

    // issue a warning saying that the given symbol was not removed
    private void warnRemove(String symbol) {
        obs.handleWarning(new HojoException(null, HojoException.WARN_NOTREMOVED,
                new String[] { symbol }, lex.currentLocation()));
    }

    // read a symbol and remove it; the symbol should have type ttype and not be
    // present in illegalRemoves
    private Object removeSymbol(boolean isRaw, int ttype) {
        String symbol;
        boolean removed;
        Object result = null;

        if (isRaw) {
            symbol = lex.nextRawToken(false, false);
        }
        else {
            lex.parseToken(TT_WORD, true);
            symbol = lex.sval;
        }
        lex.nextToken(PCT_SEPARATOR);

        switch (ttype) {
        case TT_NOTHING:
            // TT_NOTHING is used for package prefixes
            removed = comp.removePrefix(symbol);
            break;
        case TT_OPERATOR:
            removed = (result = comp.removeOperator(symbol)) != null;
            break;
        case TT_TYPE:
            removed = (result = comp.doExport(symbol)) != null;
            break;
        case TT_LITERAL:
            removed = (result = comp.removeLiteral(symbol)) != null;
            break;
        default:
            throw new HojoException();
        }

        if (!removed) {
            warnRemove(symbol);
        }
        return result;
    }

    // read an operator and add it to the syntax config
    private Object addOperator(BaseEnv env, int priority, boolean left,
            boolean right) {
        String syntax = lex.nextRawToken(false, false);
        Expression expr = comp.compileExpr(env, obs);
        lex.nextToken(PCT_SEPARATOR);
        Function op = HojoLib.toFunction(expr.xeq(env.getLink()));
        comp.addOperator(syntax, priority, left, right, op);
        return op;
    }

    private Class getClass(String name) {
        try {
            return ReflectUtils.getClass(name);
        }
        catch (ClassNotFoundException e) {
            throw new HojoException(e, HojoException.ERR_CLASSNAME,
                    new String[] { name }, lex.currentLocation());
        }
    }

    // read a import / export directive and execute it
    private Object doImport() {
        return doImportExport(true);
    }

    private Object doExport() {
        return doImportExport(false);
    }

    private Object doImportExport(boolean isImport) {
        String alias;

        // parse a sequence of TT_WORD . TT_WORD, and save the last identifier
        // as the alias
        lex.nextToken(TT_WORD, true);
        StringBuffer buf = new StringBuffer(alias = lex.sval);
        String _package = null;
        String[] names = null;

        while (lex.nextToken(TT_ANY, true) == TT_OPERATOR && lex.id == OP_DOT) {
            if (lex.nextToken(TT_ANY, true) != TT_WORD
                    && "*".equals(lex.sval)) {
                // full package import - look up the package contents and exit
                // the loop
                _package = buf.toString();
                names = PackageManager.getInstance().getClasses(_package);
                break;
            }
            else if (lex.ttype == PCT_LISTSTART || lex.ttype == PCT_IDXSTART) {
                // specific package subset import - parse the list and exit the
                // loop
                _package = buf.toString();
                HashSet s = new HashSet();

                do {
                    lex.nextToken(TT_WORD, true);
                    s.add(lex.sval);
                    if (lex.nextToken(TT_ANY) == PCT_LISTEND
                            || lex.ttype == PCT_IDXEND) {
                        break;
                    }
                    lex.pushBack();
                } while (lex.nextToken(PCT_DELIMITER) == PCT_DELIMITER);

                names = (String[])s.toArray(new String[s.size()]);
                break;
            }
            else {
                // next identifier in the sequence
                lex.pushBack();
                lex.nextToken(TT_WORD);
                buf.append('.');
                buf.append(alias = lex.sval);
            }
        } // while

        // check for explicit alias, and get the delimiter
        if (names == null) {
            if (lex.ttype != PCT_SEPARATOR) {
                // explicit alias
                lex.pushBack();
                lex.nextToken(TT_WORD);
                alias = lex.sval;
                lex.nextToken(PCT_SEPARATOR);
            }
        }
        else {
            lex.nextToken(PCT_SEPARATOR);
        }

        // do the import / export
        if (isImport) {
            if (names == null) {
                // simple import - load the class, import it and return that
                // class as result
                Class c = getClass(buf.toString());
                comp.doImport(alias, c);
                return c;
            }
            else {
                // mulitple import
                return comp.doImport(_package, names);
            }
        }
        else {
            return (names == null) ? (Object)comp.doExport(alias)
                    : (Object)comp.doExport(names);
        }
    }

}
