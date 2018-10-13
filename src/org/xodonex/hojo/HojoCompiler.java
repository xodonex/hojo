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

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xodonex.hojo.lang.CompilerEnvironment;
import org.xodonex.hojo.lang.Const;
import org.xodonex.hojo.lang.Environment;
import org.xodonex.hojo.lang.Expression;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lang.HObject;
import org.xodonex.hojo.lang.LValue;
import org.xodonex.hojo.lang.Operator;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.Type;
import org.xodonex.hojo.lang.UnreachableStatementException;
import org.xodonex.hojo.lang.Variable;
import org.xodonex.hojo.lang.env.BaseEnv;
import org.xodonex.hojo.lang.env.CompilerEnv;
import org.xodonex.hojo.lang.env.NoEnv;
import org.xodonex.hojo.lang.expr.ApplyExpr;
import org.xodonex.hojo.lang.expr.ArrayCloneExpr;
import org.xodonex.hojo.lang.expr.ArrayCreateExpr;
import org.xodonex.hojo.lang.expr.ArrayIndexExpr;
import org.xodonex.hojo.lang.expr.ArrayIndexRangeExpr;
import org.xodonex.hojo.lang.expr.ArrayInitExpr;
import org.xodonex.hojo.lang.expr.ArrayLengthExpr;
import org.xodonex.hojo.lang.expr.AssignCompoundOp;
import org.xodonex.hojo.lang.expr.AssignOp;
import org.xodonex.hojo.lang.expr.AssignXchgOp;
import org.xodonex.hojo.lang.expr.BeforeExpr;
import org.xodonex.hojo.lang.expr.BinaryOp;
import org.xodonex.hojo.lang.expr.CharSequenceIndexExpr;
import org.xodonex.hojo.lang.expr.CharSequenceIndexRangeExpr;
import org.xodonex.hojo.lang.expr.CollectionInitExpr;
import org.xodonex.hojo.lang.expr.CondAndExpr;
import org.xodonex.hojo.lang.expr.CondOrExpr;
import org.xodonex.hojo.lang.expr.ConstructorExpr;
import org.xodonex.hojo.lang.expr.DelayedConstructorExpr;
import org.xodonex.hojo.lang.expr.DelayedFieldExpr;
import org.xodonex.hojo.lang.expr.DelayedInvokeExpr;
import org.xodonex.hojo.lang.expr.FieldExpr;
import org.xodonex.hojo.lang.expr.FinalFieldExpr;
import org.xodonex.hojo.lang.expr.GenericIndexExpr;
import org.xodonex.hojo.lang.expr.HClassMemberExpr;
import org.xodonex.hojo.lang.expr.HObjectCreateExpr;
import org.xodonex.hojo.lang.expr.IfThenElseExpr;
import org.xodonex.hojo.lang.expr.InvokeExpr;
import org.xodonex.hojo.lang.expr.LambdaExpr;
import org.xodonex.hojo.lang.expr.LetExpr;
import org.xodonex.hojo.lang.expr.ListIndexExpr;
import org.xodonex.hojo.lang.expr.ListIndexRangeExpr;
import org.xodonex.hojo.lang.expr.MapInitExpr;
import org.xodonex.hojo.lang.expr.MapMemberExpr;
import org.xodonex.hojo.lang.expr.PostDecOp;
import org.xodonex.hojo.lang.expr.PostIncOp;
import org.xodonex.hojo.lang.expr.PostfixOp;
import org.xodonex.hojo.lang.expr.PreDecOp;
import org.xodonex.hojo.lang.expr.PreIncOp;
import org.xodonex.hojo.lang.expr.ScopeRef;
import org.xodonex.hojo.lang.expr.SequenceExpr;
import org.xodonex.hojo.lang.expr.TernaryOp;
import org.xodonex.hojo.lang.expr.ThenExpr;
import org.xodonex.hojo.lang.expr.TypeExpr;
import org.xodonex.hojo.lang.expr.TypecastExpr;
import org.xodonex.hojo.lang.expr.UnaryOp;
import org.xodonex.hojo.lang.expr.VarExpr;
import org.xodonex.hojo.lang.expr.VoidExpr;
import org.xodonex.hojo.lang.func.AllocatorFunction;
import org.xodonex.hojo.lang.func.ConstructorFunction;
import org.xodonex.hojo.lang.func.FieldFunction;
import org.xodonex.hojo.lang.func.IndexFunction;
import org.xodonex.hojo.lang.func.MethodFunction;
import org.xodonex.hojo.lang.func.TypeCastFunction;
import org.xodonex.hojo.lang.ops.DotOp;
import org.xodonex.hojo.lang.ops.IdOp;
import org.xodonex.hojo.lang.ops.SourceOp;
import org.xodonex.hojo.lang.stm.BlockEnvStatement;
import org.xodonex.hojo.lang.stm.BlockStatement;
import org.xodonex.hojo.lang.stm.BreakStm;
import org.xodonex.hojo.lang.stm.CatchClause;
import org.xodonex.hojo.lang.stm.ClassDeclStm;
import org.xodonex.hojo.lang.stm.ContinueStm;
import org.xodonex.hojo.lang.stm.DoStm;
import org.xodonex.hojo.lang.stm.ExprStm;
import org.xodonex.hojo.lang.stm.ForSeqStm;
import org.xodonex.hojo.lang.stm.ForStm;
import org.xodonex.hojo.lang.stm.FuncCreateStm;
import org.xodonex.hojo.lang.stm.FuncDeclStm;
import org.xodonex.hojo.lang.stm.IfStm;
import org.xodonex.hojo.lang.stm.NOP;
import org.xodonex.hojo.lang.stm.ReturnStm;
import org.xodonex.hojo.lang.stm.ShortIfStm;
import org.xodonex.hojo.lang.stm.SwitchStm;
import org.xodonex.hojo.lang.stm.SyncStm;
import org.xodonex.hojo.lang.stm.ThrowStm;
import org.xodonex.hojo.lang.stm.TryStm;
import org.xodonex.hojo.lang.stm.VarDeclStm;
import org.xodonex.hojo.lang.stm.WhileStm;
import org.xodonex.hojo.lang.type.FunctionType;
import org.xodonex.hojo.lang.type.GenericArrayType;
import org.xodonex.hojo.lang.type.GenericCollectionType;
import org.xodonex.hojo.lang.type.GenericFunctionType;
import org.xodonex.hojo.lang.type.GenericMapType;
import org.xodonex.hojo.lang.type.NumberType;
import org.xodonex.hojo.lang.type.ObjectType;
import org.xodonex.hojo.lang.type.VoidType;
import org.xodonex.hojo.util.ClassLoaderAction;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;
import org.xodonex.util.tools.PackageManager;

/**
 * The compiler for the Hojo language - the <em>primary language</em> that is
 * (the meta language is handled by the {@link HojoInterpreter}.
 *
 * Lexical processing is performed by means of the {@link HojoLexer}, which can
 * be re-configured at runtime, thus resulting in a compiler with dynamic
 * lexical syntax, permitting e.g. user-defined literals and operators. The
 * compiler instance acts as a control front-end to the lexer.
 *
 * The primary task of the compiler is generate abstract syntax trees (ASTs).
 * The AST is actually the final compilation stage - it holds enough
 * information/methods to be directly executable (aka interpreted) by a
 * {@link HojoInterpreter}.
 *
 * A byte-code generating back-end could be implemented (which would enable
 * introduction of new classes to the JVM, which the current implementation
 * cannot do), but so far this extension is purely theoretical.
 *
 * @see HojoLexer
 * @see HojoInterpreter
 */
public class HojoCompiler implements HojoConst {

    // context flags
    private final static int CTXT_MAIN = 0x60000000, // main environment - allow
                                                     // only declarations
            CTXT_CLASS = 0, // class - allow only variable/function declarations
            CTXT_RETURN = 0x20000000, // allow return statement
            // CTXT_RETURN_ALL = 0x30000000, // allow return statement, and
            // allow
            // return ();
            CTXT_BREAK = 0x08000000, // allow break statement
            CTXT_CONTINUE = 0x04000000, // allow continue statement
            CTXT_NODECL = 0x02000000, // disallow declarations
            CTXT_SEQ = 0x01000000, // sequence: don't read the separator,
                                   // disallow
            // block statements
            CTXT_MASK = 0xffff0000, // mask for non-modifier context
            // CTXT_M_PUBLIC = MOD_PUBLIC, // indicates that the 'public'
            // modifier
            // was used
            // CTXT_M_FINAL = MOD_FINAL, // indicates that the 'final' modifier
            // was
            // used
            // CTXT_M_SYNCHRONIZED = MOD_SYNCHRONIZED, // indicates that the
            // 'synchronized' modifier
            // was used
            CTXT_MODIFIER = 0x0000ffff, // modifier mask

            CTXT_NOFUNCDECL = CTXT_NODECL | CTXT_SEQ, // functions declarations
                                                      // are not allowed
            CTXT_ALLOW_DECL = ~CTXT_NODECL, // allow declarations
            CTXT_LOOP = CTXT_BREAK | CTXT_CONTINUE; // loop: allow break and
                                                    // continue statements

    // dummy class for cExprList()
    private final static Expression ERR_EXPR = new Expression() {
        private static final long serialVersionUID = 1L;

        @Override
        public Object xeq(Environment env) {
            return null;
        }

        @Override
        protected Type getType0() {
            return null;
        }
    };

    // the system-wide shared instance
    private static HojoCompiler sharedInstance = null;

    // mimimum ID value for custom operators
    private final static int MIN_OP_ID = 16;

    // token config
    private final int TT_MAPSTART, TT_MAPEND, TT_ARRAYSTART, TT_ARRAYEND,
            TT_LISTSTART, TT_LISTEND, TT_CASELABEL, TT_OP_NEG;

    private HojoSyntax stx;
    private final HojoLexer lex = new HojoLexer();
    private final StringUtils.Format standardFormat;

    private final HashSet packagePrefixes = new HashSet(11);

    // next ID for user-defined operators
    private int operatorID = MIN_OP_ID;

    // LUT for operator code (String -m-> Function)
    private final HashMap operators = new HashMap(2 * OP_COUNT);
    private final DotOp dot;

    // LUTs for the dynamic syntax config
    private final HashSet macros = new HashSet(11);
    private final HashSet coreTypes = new HashSet(67);
    private final HashSet customTypes = new HashSet(17);
    private final HashSet coreOperators = new HashSet(117);
    private final HashSet customOperators = new HashSet(17);
    private final HashSet coreLiterals = new HashSet(11);
    private final HashSet customLiterals = new HashSet(17);

    // used by cClassName()
    private ArrayList arrayDimensions = new ArrayList();
    private char dotChar1;

    // compiler options
    private boolean optStrictArgTypes = false; // enforce static type match in
                                               // argument lists
    private boolean optStrictFieldTypes = false; // enforce static type match in
                                                 // field access
    private boolean optStrictIndexTypes = false; // enforce static type match in
                                                 // index expressions
    private boolean optStrictConv = false; // enforce static type match in
                                           // assignments and compound ops

    // observer (for warnings only)
    private HojoObserver obs = null;

    public HojoCompiler() {
        this(null, 2);
    }

    public HojoCompiler(HojoSyntax stx) {
        this(stx, 2);
    }

    public HojoCompiler(HojoSyntax stx, int strictness) {
        this.stx = (stx == null) ? new HojoSyntax() : (HojoSyntax)stx.clone();
        standardFormat = this.stx.createFormat();

        // configure the lexer syntax and save the values of token types which
        // might
        // be ambiguously defined.
        int[] ttypes = this.stx.configure(lex, standardFormat);
        TT_MAPSTART = ttypes[0];
        TT_MAPEND = ttypes[1];
        TT_ARRAYSTART = ttypes[2];
        TT_ARRAYEND = ttypes[3];
        TT_LISTSTART = ttypes[4];
        TT_LISTEND = ttypes[5];
        TT_CASELABEL = ttypes[6];
        TT_OP_NEG = ttypes[7];
        dotChar1 = this.stx.operators[OP_IDX_DOT].charAt(0);
        dot = new DotOp(this.stx.reserved[RES_CLASS - RES_BASE_ID]);

        // save the core syntax config
        addConfig(this.stx.types, coreTypes);
        addConfig(this.stx.standardLiterals, coreLiterals);
        addConfig(this.stx.customLiteralNames, coreLiterals);
        addConfig(this.stx.operators, coreOperators);

        // build the operator lookup table, and import java.lang.*;
        resetSyntax(false, true);

        // set the strictness
        strictTypeCheck(strictness);
    }

    private static void addConfig(String[] names, Set config) {
        String s;
        for (int i = names.length - 1; i >= 0; i--) {
            if ((s = names[i]) != null) {
                config.add(s);
            }
        }
    }

    /* ******************** PUBLIC METHODS ******************** */

    public synchronized static HojoCompiler getSharedInstance() {
        return (sharedInstance == null) ? sharedInstance = new HojoCompiler()
                : sharedInstance;
    }

    public HojoLexer getLexer() {
        return lex;
    }

    public HojoSyntax getSyntax() {
        return (HojoSyntax)stx.clone();
    }

    public StringUtils.Format getStandardFormat() {
        return standardFormat;
    }

    public synchronized void strictTypeCheck(int level) {
        optStrictArgTypes = optStrictFieldTypes = optStrictIndexTypes = level > 0;
        optStrictConv = level > 1;
    }

    public synchronized void addMacro(String name, String value) {
        if (macros.contains(name)) {
            throw new HojoException(null,
                    HojoException.ERR_REDEFINED_SYMBOL, new String[] { name },
                    lex.currentLocation());
        }
        lex.addMacro(name, value);
        macros.add(name);
    }

    public synchronized boolean removeMacro(String name) {
        macros.remove(name);
        return lex.removeMacro(name);
    }

    public synchronized boolean removePrefix(String prefix) {
        return packagePrefixes.remove(prefix);
    }

    public synchronized boolean addPrefix(String prefix) {
        if (packagePrefixes.contains(prefix)) {
            return false;
        }
        packagePrefixes.add(prefix);
        return true;
    }

    public synchronized void doImport(String alias, Class cls) {
        if (cls == null) {
            throw new NullPointerException();
        }

        lex.addSymbol(alias, TT_TYPE, cls, null);
        customTypes.add(alias);
    }

    public synchronized String[] doImport(String _package, String[] names) {
        Object[] config;
        ArrayList result = new ArrayList();
        String name;

        for (int i = 0; i < names.length; i++) {
            config = lex.getConfig(name = names[i]);
            if (config != null) {
                continue;
            }

            lex.addSymbol(name, TT_TYPE, null,
                    new ClassLoaderAction(_package + "." + name));
            customTypes.add(name);
            result.add(name);
        }

        return (String[])result.toArray(new String[result.size()]);
    }

    public synchronized String[] doExport(String[] names) {
        ArrayList result = new ArrayList();
        Object[] cfg;
        String name;

        for (int i = 0; i < names.length; i++) {
            cfg = lex.getConfig(name = names[i]);
            if (cfg == null || ((Integer)cfg[0]).intValue() != TT_TYPE ||
                    !customTypes.contains(name)) {
                continue;
            }

            lex.removeSymbol(name);
            customTypes.remove(name);
            result.add(name);
        }

        return (String[])result.toArray(new String[result.size()]);
    }

    public synchronized String[] doLoad(Object lib) throws HojoException {
        Object[] cfg;
        ArrayList result = new ArrayList();
        String name;
        Object value;
        Map.Entry mentry;

        Map values = HojoLib.mkLib(lib, null);

        for (Iterator it = values.entrySet().iterator(); it.hasNext();) {
            mentry = (Map.Entry)it.next();
            name = ConvertUtils.toString(mentry.getKey());
            if ((value = mentry.getValue()) == null) {
                value = Const.NULL;
            }
            else if (value instanceof Variable
                    && ((Variable)value).isFinal()) {
                // convert final variables to constants
                value = new Const(((Variable)value).getValue());
            }

            cfg = lex.getConfig(name);
            if (cfg != null) {
                continue;
            }

            lex.addSymbol(name, TT_LITERAL, value, null);
            customLiterals.add(name);
            result.add(name);
        }

        return (String[])result.toArray(new String[result.size()]);
    }

    public synchronized String[] doUnload(Object lib) throws HojoException {
        Object[] cfg;
        ArrayList result = new ArrayList();
        String name;
        Collection values;

        if (lib instanceof HObject) {
            values = ((HObject)lib).getMemberNames(null);
        }
        else if (lib instanceof Map) {
            values = ((Map)lib).values();
        }
        else {
            values = ConvertUtils.toCollection(lib);
        }

        Iterator it = values.iterator();
        while (it.hasNext()) {
            name = ConvertUtils.toString(it.next());

            cfg = lex.getConfig(name);
            if (cfg == null || ((Integer)cfg[0]).intValue() != TT_LITERAL ||
                    !customLiterals.contains(name)) {
                continue;
            }

            lex.removeSymbol(name);
            customLiterals.remove(name);
            result.add(name);
        }

        return (String[])result.toArray(new String[result.size()]);
    }

    public synchronized Object doExport(String alias) {
        if (coreTypes.contains(alias)) {
            throw new HojoException(null, HojoException.ERR_REMOVED_SYMBOL,
                    new String[] { alias }, lex.currentLocation());
        }

        Object[] cfg = lex.getConfig(alias);
        if (cfg == null || ((Integer)cfg[0]).intValue() != TT_TYPE) {
            return null;
        }

        lex.removeSymbol(alias);
        customTypes.remove(alias);
        if (cfg[2] instanceof ClassLoaderAction) {
            return ((ClassLoaderAction)cfg[2]).getValue();
        }
        else {
            return cfg[1];
        }
    }

    public synchronized void addOperator(String syntax, int priority,
            boolean leftAssoc, boolean rightAssoc, Function op)
            throws HojoException {
        if (priority > 15 || priority < 0) {
            throw new HojoException(null, HojoException.ERR_VALUE,
                    new String[] { "" + priority }, lex.currentLocation());
        }

        int arity;
        int assoc;
        switch (priority) {
        case 15:
            arity = 1;
            assoc = OP_LEFTASSOC;
            break;
        case 14:
            arity = 1;
            assoc = OP_RIGHTASSOC;
            break;
        case 3:
            arity = 3;
            assoc = OP_NONASSOC;
            break;
        default:
            arity = 2;
            assoc = (leftAssoc ^ rightAssoc)
                    ? leftAssoc ? OP_LEFTASSOC : OP_RIGHTASSOC
                    : OP_NONASSOC;
        }

        if (op.getArity() != arity) {
            String ops = op instanceof Operator
                    ? ((Operator)op).toString(stx, StringUtils.defaultFormat)
                    : op.toString();
            throw new HojoException(null, HojoException.ERR_VALUE,
                    new String[] { ops }, lex.currentLocation());
        }

        Integer ID = new Integer((priority << OP_SHIFT_PRIO) | assoc |
                (arity << OP_SHIFT_ARITY) | operatorID);
        lex.addSymbol(syntax, TT_OPERATOR, ID, null);
        operators.put(ID, op);
        customOperators.add(syntax);
        operatorID++;
    }

    public synchronized Function removeOperator(String syntax) {
        if (coreOperators.contains(syntax)) {
            throw new HojoException(null, HojoException.ERR_REMOVED_SYMBOL,
                    new String[] { syntax }, lex.currentLocation());
        }

        Object[] cfg = lex.getConfig(syntax);
        if (cfg == null || ((Integer)cfg[0]).intValue() != TT_OPERATOR) {
            return null;
        }

        lex.removeSymbol(syntax);
        customOperators.remove(syntax);
        return (Function)operators.remove(cfg[1]);
    }

    public synchronized void addLiteral(String name, Object value) {
        lex.addSymbol(name, TT_LITERAL, value, null);
        customLiterals.add(name);
    }

    public synchronized Object[] removeLiteral(String syntax) {
        if (coreLiterals.contains(syntax)) {
            throw new HojoException(null, HojoException.ERR_REMOVED_SYMBOL,
                    new String[] { syntax }, lex.currentLocation());
        }

        Object[] cfg = lex.getConfig(syntax);
        if (cfg == null || ((Integer)cfg[0]).intValue() != TT_LITERAL) {
            return null;
        }

        lex.removeSymbol(syntax);
        customLiterals.remove(syntax);
        return new Object[] { cfg[1] };
    }

    private static Collection addCollection(Collection dst, Collection src) {
        if (dst == null) {
            dst = new ArrayList(src.size());
        }
        dst.addAll(src);
        return dst;
    }

    public synchronized Collection listMacros(Collection c) {
        return addCollection(c, macros);
    }

    public synchronized Collection listPackagePrefixes(Collection c) {
        return addCollection(c, packagePrefixes);
    }

    public synchronized Collection listCustomTypeIDs(Collection c) {
        return addCollection(c, customTypes);
    }

    public synchronized Collection listCustomLiterals(Collection c) {
        return addCollection(c, customLiterals);
    }

    public synchronized Collection listCustomOperators(Collection c) {
        return addCollection(c, customOperators);
    }

    public Statement compileBlock(CompilerEnvironment env)
            throws HojoException {
        return compileBlock(env, null);
    }

    public synchronized Statement compileBlock(Reader block)
            throws HojoException {
        while (lex.exit()) {
            ;
        }
        lex.include(block);
        try {
            return compileBlock(null, null);
        }
        finally {
            lex.exit();
        }
    }

    public synchronized Statement compileBlock(CompilerEnvironment env,
            HojoObserver obs)
            throws HojoException {
        if (obs == null) {
            if (this.obs != null) {
                throw new NullPointerException("No observer has been set");
            }
        }
        else {
            lex.setObserver(this.obs = obs);
        }

        try {
            return getBlock(cBlock(env, HojoLib.VOID_TYPE, CTXT_MAIN, TT_EOF));
        }
        catch (Throwable t) {
            throw HojoException.wrap(t);
        }
    }

    public Statement compileStm(CompilerEnvironment env) throws HojoException {
        return compileStm(env, null);
    }

    public synchronized Statement compileStm(Reader stm) throws HojoException {
        while (lex.exit()) {
            ;
        }
        lex.include(stm);
        try {
            return compileStm(null, null);
        }
        finally {
            lex.exit();
        }
    }

    public synchronized Statement compileStm(CompilerEnvironment env,
            HojoObserver obs) throws HojoException {
        if (obs == null) {
            if (this.obs != null) {
                throw new NullPointerException("No observer has been set");
            }
        }
        else {
            lex.setObserver(this.obs = obs);
        }

        try {
            return cStm(env, HojoLib.VOID_TYPE, CTXT_MAIN);
        }
        catch (Throwable t) {
            throw HojoException.wrap(t);
        }
    }

    public Expression compileExpr(CompilerEnvironment env)
            throws HojoException {
        return compileExpr(env, null);
    }

    public synchronized Expression compileExpr(Reader expr)
            throws HojoException {
        while (lex.exit()) {
            ;
        }
        lex.include(expr);
        try {
            return compileExpr(null, null);
        }
        finally {
            lex.exit();
        }
    }

    public synchronized Expression compileExpr(
            CompilerEnvironment env, HojoObserver obs) throws HojoException {
        if (obs == null) {
            if (this.obs != null) {
                throw new NullPointerException("No observer has been set");
            }
        }
        else {
            lex.setObserver(this.obs = obs);
        }

        try {
            return cExpr(env, HojoLib.OBJ_TYPE);
        }
        catch (Throwable t) {
            throw HojoException.wrap(t);
        }
    }

    public synchronized void doRecovery() {
        try {
            if (lex.ttype == PCT_BLOCKEND) {
                // the error was at an end-of-block: simply drop the token
                lex.dropToken();
            }
            else {
                // skip any tokens until the next separator, or until
                // end-of-file
                while (lex.ttype != PCT_SEPARATOR && lex.ttype != TT_EOF) {
                    lex.skipToken();
                }
            }
        }
        catch (Throwable t) {
        }
    }

    /* ******************** ADMINISTRATIVE METHODS ******************** */

    public synchronized void reset() {
        obs = null;
        lex.reset();
        resetSyntax(true, true);
    }

    public synchronized void resetSyntax() {
        resetSyntax(true, true);
    }

    private void resetSyntax(boolean fullReset, boolean addDefaultExtensions) {
        // a full reset is not necessary in the constructor
        if (fullReset) {
            // restore the core syntax
            lex.clearMacros();
            ArrayList l = new ArrayList(128);
            l.addAll(customOperators);
            l.addAll(customLiterals);
            l.addAll(customTypes);
            for (int i = l.size() - 1; i >= 0; i--) {
                lex.removeSymbol((String)l.get(i));
            }

            // reset the custom syntax lookup tables
            operatorID = 16;
            macros.clear();
            customTypes.clear();
            customLiterals.clear();
            customOperators.clear();
        }

        // create the default package prefixes
        packagePrefixes.clear();
        for (int i = stx.packagePrefixes.length - 1; i >= 0;) {
            packagePrefixes.add(stx.packagePrefixes[i--]);
        }

        // create an operator lookup table
        Operator op;
        operators.clear();
        operators.put(new Integer(OP_DOT), dot);
        for (int i = OP_COUNT - 1; i >= 0; i--) {
            if ((op = HojoLib.operators[i]) != null) {
                operators.put(new Integer(OP_CODES[i]), op);
            }
        }

        // create a new OP_SOURCE from the defined syntax
        operators.put(new Integer(OP_SOURCE), new SourceOp(stx));

        // reset the ID counter for custom operators.
        operatorID = MIN_OP_ID;

        // import the standard library, if required
        if (addDefaultExtensions) {
            if (stx.standardLiterals[4] != null) {
                Object[] cfg = lex.getConfig(stx.standardLiterals[4]);
                if (cfg != null) {
                    doLoad(((Const)cfg[1]).getValue());
                }
            }
        }

        // import java.lang.*, if required
        if (addDefaultExtensions) {
            doImport("java.lang",
                    PackageManager.getInstance().getClasses("java.lang"));
        }
    }

    /* ******************** STATEMENTS ******************** */

    private Statement cStm(CompilerEnvironment env, Type typ, int ctxt)
            throws HojoException {
        Expression expr = null;
        String name = null;

        // skip consecutive empty statements
        while (lex.nextToken(TT_ANY) == PCT_SEPARATOR) {
            ;
        }

        switch (lex.ttype) {
        case TT_EOF:
            // indicate EOF
            return null;
        case RES_BREAK:
            if ((ctxt & CTXT_BREAK) != CTXT_BREAK) {
                throw new HojoException(null,
                        HojoException.ERR_ILLEGAL_STATEMENT, null,
                        lex.currentLocation());
            }
            lex.nextToken(PCT_SEPARATOR);
            return BreakStm.BREAK;
        case RES_CONTINUE:
            if ((ctxt & CTXT_CONTINUE) != CTXT_CONTINUE) {
                throw new HojoException(null,
                        HojoException.ERR_ILLEGAL_STATEMENT, null,
                        lex.currentLocation());
            }
            lex.nextToken(PCT_SEPARATOR);
            return ContinueStm.CONTINUE;
        case RES_RETURN:
            if ((ctxt & CTXT_RETURN) != CTXT_RETURN) {
                throw new HojoException(null,
                        HojoException.ERR_ILLEGAL_STATEMENT, null,
                        lex.currentLocation());
            }

            if (typ != null && typ.isVoid()) {
                // no value may be returned
                expr = null;
                if (lex.nextToken(TT_ANY) == PCT_LPAREN) {
                    // allow return (); for return;
                    lex.nextToken(PCT_RPAREN);
                }
                else {
                    lex.pushBack();
                }
            }
            else {
                // parse the returned value (this may be ())
                expr = cExpr(env, typ == null ? HojoLib.OBJ_TYPE : typ);
                if (typ != null && expr.getType().kind() == Type.TYP_VOID) {
                    // cannot return void when a value is expected
                    throw new HojoException(null, HojoException.ERR_TYPE,
                            new String[] {
                                    (typ == null ? HojoLib.OBJ_TYPE : typ)
                                            .toString(stx),
                                    HojoLib.VOID_TYPE.toString(stx)
                            }, lex.currentLocation());
                }
                expr = forceType(expr, typ);
            }
            lex.nextToken(PCT_SEPARATOR);
            return new ReturnStm(expr);
        case RES_IF:
            checkNotSeq(ctxt);
            return cIfStm(env, typ, ctxt);
        case RES_DO:
            checkNotSeq(ctxt);
            return cDoStm(env, typ, ctxt);
        case RES_WHILE:
            checkNotSeq(ctxt);
            return cWhileStm(env, typ, ctxt);
        case RES_FOR:
            checkNotSeq(ctxt);
            return cForStm(env, typ, ctxt);
        case RES_SWITCH:
            checkNotSeq(ctxt);
            return cSwitchStm(env, typ, ctxt);
        case RES_THROW:
            checkNotSeq(ctxt);
            expr = cExpr(env, HojoLib.OBJ_TYPE);
            lex.nextToken(PCT_SEPARATOR);
            Type t = expr.getType();
            if (t.isNull()) {
                throw new HojoException(null, HojoException.ERR_SYNTAX, null,
                        lex.currentLocation());
            }
            else if (!Throwable.class.isAssignableFrom(t.toClass())) {
                throw new HojoException(null, HojoException.ERR_TYPE,
                        new String[] { Throwable.class.getName(),
                                t.toString(stx) },
                        lex.currentLocation());
            }
            return new ThrowStm(expr);
        case RES_TRY:
            checkNotSeq(ctxt);
            return cTryStm(env, typ, ctxt);
        case RES_PUBLIC:
        case RES_FINAL:
        case RES_SYNCHRONIZED:
            // update the context with the given modifiers, and fall through
            // (cModifiers reads one token ahead)
            lex.pushBack();
            ctxt |= cModifiers(ctxt);

            if ((ctxt & MOD_ALL) == MOD_SYNCHRONIZED
                    && lex.ttype == PCT_LPAREN) {
                // handle synchronized statements
                checkNotSeq(ctxt);
                lex.pushBack();
                return cSyncStm(env, typ, ctxt & ~MOD_SYNCHRONIZED);
            }
        default:
            Statement result;

            if (lex.ttype == RES_CLASS) {
                return cClassDecl(env, (short)ctxt);
            }
            if (lex.ttype == RES_VAR) {
                // signify an unknown type
                expr = null;
            }
            else {
                // compile an expression, if the 'var' keyword was not used
                lex.pushBack();
                expr = cInfixExpr(env, HojoLib.OBJ_TYPE, OP_PRIO_0);
            }

            // determine whether the expression was a type (or var); if so, then
            // this must be a declaration statement.
            if (expr == null || expr.isType()) {
                if ((ctxt & CTXT_NODECL) == CTXT_NODECL) {
                    throw new HojoException(null,
                            HojoException.ERR_ILLEGAL_DECLARATION,
                            null, lex.currentLocation());
                }
                lex.nextToken(TT_WORD);
                name = lex.sval;
                if (lex.nextToken(TT_ANY) == PCT_LPAREN && expr != null &&
                        (ctxt & CTXT_NOFUNCDECL) == 0) {
                    // function declaration
                    result = cFuncDecl(env, expr.getType(), name, ctxt);
                }
                else {
                    // variable declaration
                    if ((ctxt & MOD_SYNCHRONIZED) != 0) {
                        throw new HojoException(null,
                                HojoException.ERR_ILLEGAL_MODIFIER,
                                new String[] {
                                        Modifier.toString(MOD_SYNCHRONIZED) },
                                lex.currentLocation());
                    }
                    lex.pushBack();
                    result = cDecl(env, (TypeExpr)expr, name, ctxt);
                }
            }
            else if ((ctxt & CTXT_MODIFIER) != 0) {
                // Illegal expression statement
                throw new HojoException(null,
                        HojoException.ERR_ILLEGAL_MODIFIER,
                        new String[] { Modifier.toString((short)ctxt) },
                        lex.currentLocation());
            }
            else {
                // Expression statement.
                result = new ExprStm(expr);
                if ((ctxt & CTXT_SEQ) != CTXT_SEQ) {
                    // get the separator iff not in an expression sequence
                    lex.nextToken(PCT_SEPARATOR);
                }
            }
            return result;
        }
    }

    /* ******************** STATEMENT HELP METHODS ******************** */

    private int cModifiers(int ctxt) {
        int result = 0;

        HashSet done = new HashSet(5);
        Integer i;

        loop: do {
            switch (lex.nextToken(TT_ANY)) {
            case RES_PUBLIC:
                if ((ctxt & CTXT_MASK) != CTXT_CLASS) {
                    throw new HojoException(null,
                            HojoException.ERR_ILLEGAL_MODIFIER,
                            new String[] {
                                    stx.reserved[lex.ttype - RES_BASE_ID] },
                            lex.currentLocation());
                }
                result |= MOD_PUBLIC;
                break;
            case RES_FINAL:
                result |= MOD_FINAL;
                break;
            case RES_SYNCHRONIZED:
                result |= MOD_SYNCHRONIZED;
                break;
            default:
                break loop;
            }

            i = new Integer(lex.ttype);
            if (done.contains(i)) {
                throw new HojoException(null,
                        HojoException.ERR_DUPLICATE_MODIFIER,
                        new String[] { stx.reserved[lex.ttype - RES_BASE_ID] },
                        lex.currentLocation());
            }
            done.add(i);
        } while (true);

        // N.B: no push back necessary!
        return result;
    }

    private final void checkNotSeq(int ctxt) {
        if ((ctxt & CTXT_SEQ) == CTXT_SEQ) {
            throw new HojoException(null, HojoException.ERR_ILLEGAL_STATEMENT,
                    null,
                    lex.currentLocation());
        }
    }

    private static final Statement getBlock(Statement[] stms) {
        switch (stms.length) {
        case 0:
            return NOP.NOP;
        case 1:
            return stms[0];
        default:
            return new BlockStatement(stms);
        }
    }

    // assumes that the class name is the next token of the lexer
    private ClassDeclStm cClassDecl(CompilerEnvironment env, short modifiers) {
        lex.nextToken(TT_WORD);
        String name = lex.sval;
        if (env.getAddress(name) >= 0) {
            throw new HojoException(null, HojoException.ERR_DUPLICATE_ID,
                    new String[] { name, env.getType(name).toString(stx) },
                    lex.currentLocation());
        }
        lex.nextToken(PCT_BLOCKSTART);
        CompilerEnv env2 = new CompilerEnv(env, true);
        Statement body = getBlock(cBlock(env2, HojoLib.OBJ_TYPE,
                CTXT_CLASS, PCT_BLOCKEND));
        return new ClassDeclStm(name, modifiers,
                env.alloc(name, HojoLib.HC_TYPE, (short)0),
                body, (short)env2.size(), env.getLevel());
    }

    // assumes that the argument list left parenthesis is the next token of the
    // lexer
    private Statement cFuncDecl(CompilerEnvironment env, Type retType,
            String name,
            int ctxt) {
        ArrayList names_ = new ArrayList(8);
        ArrayList types_ = new ArrayList(8);
        ArrayList defaults_ = new ArrayList(8);
        ArrayList modifiers_ = new ArrayList(8);

        // get the argument declarations
        boolean variableArgs = cArgumentDecl(types_, names_, defaults_,
                modifiers_);

        // extract the name of the variable arguments, if any
        String extraName;
        if (variableArgs) {
            int idx = names_.size() - 1;
            extraName = (String)names_.remove(idx);
            types_.remove(idx);
            defaults_.remove(idx);
            modifiers_.remove(idx);
        }
        else {
            extraName = null;
        }

        // create the declared type
        Class[] types = (Class[])types_.toArray(new Class[types_.size()]);
        FunctionType funcType = new GenericFunctionType(Function.class,
                types, retType.toClass());

        // verify that the declaration does not conflict with a previous
        // declaration
        Type prevType = env.getType(name);
        boolean redef = prevType != null;
        short prevMod = redef ? env.getModifiers(name) : 0;
        boolean checkMod = redef ? prevMod == (short)ctxt : true;
        if (!checkMod || (prevType != null && !funcType.equals(prevType))) {
            int ecode;
            String[] args;
            if (!checkMod) {
                ecode = HojoException.ERR_REDEF_MODIFIER;
                args = new String[] { name, Modifier.toString(prevMod) };
            }
            else {
                ecode = HojoException.ERR_DUPLICATE_ID;
                args = new String[] { name, prevType.toString(stx) };
            }
            throw new HojoException(null, ecode, args, lex.currentLocation());
        }

        if (lex.nextToken(TT_ANY) != PCT_BLOCKSTART) {
            // forward declaration - return NOP if this is a duplicate
            // definition,
            // else create the declaration
            lex.pushBack();

            // verify that the function is not final or synchronized
            if ((ctxt & MOD_SYNCHRONIZED) != 0) {
                throw new HojoException(null,
                        HojoException.ERR_ILLEGAL_MODIFIER,
                        new String[] {
                                stx.reserved[RES_SYNCHRONIZED - RES_BASE_ID] },
                        lex.currentLocation());
            }

            if (prevType != null) {
                return NOP.NOP;
            }
            else {
                // update the environment and create a declaration statement
                short addr = env.alloc(name, funcType, (short)ctxt);
                return new FuncDeclStm(name, funcType, (short)ctxt, addr,
                        retType, null);
            }
        }

        // create a new environment for the body expression
        String[] names = (String[])names_.toArray(new String[names_.size()]);
        Object[] defaults = defaults_.toArray();
        short[] modifiers = (short[])HojoLib.toArray(modifiers_, short[].class,
                short.class, true);
        CompilerEnv lEnv = new CompilerEnv(env, names, types, modifiers, true);
        if (extraName != null) {
            lEnv.alloc(extraName, HojoLib.TUPLE_TYPE, (short)MOD_FINAL);
        }

        // Allocate a new, empty variable or get the adress assigned by a
        // preceding forward declaration. This allows for recursive function
        // declarations.
        short addr = env.alloc(name, funcType, (short)ctxt);
        Statement body;

        // catch exceptions in order to remove the allocated function on a
        // syntax error
        try {
            // compile the function body in the new environment
            body = getBlock(cBlock(lEnv, retType, CTXT_RETURN, PCT_BLOCKEND));
        }
        catch (RuntimeException e) {
            if (redef) {
                try {
                    env.remove(name);
                }
                catch (Throwable t) {
                }
            }
            throw e;
        }

        // indicate that the function has been assigned, and throw an exception
        // if a reassignment is not legal
        if (!env.doAssign(name)) {
            throw new HojoException(null, HojoException.ERR_FINAL,
                    null, lex.currentLocation());
        }

        // verify that the code does not contain unreachable blocks, and that
        // a return statement is present (cStm() checks for bad return types)
        Type rt = null;
        try {
            rt = body.checkCode(null);
        }
        catch (HojoException e) {
            e.setLocation(lex.currentLocation());
            throw e;
        }
        catch (UnreachableStatementException e) {
            throw new HojoException(e, HojoException.ERR_UNREACHABLE,
                    new String[] { e.getStatement().toString(stx,
                            StringUtils.defaultFormat, "")
                    }, lex.currentLocation());
        }

        if (rt == null && retType.kind() != Type.TYP_VOID) {
            throw new HojoException(null, HojoException.ERR_MISSING_RETURN_TYPE,
                    new String[] {
                            body.toString(stx, StringUtils.defaultFormat, "") },
                    lex.currentLocation());
        }

        return new FuncCreateStm(name, funcType, (short)ctxt, addr, names,
                types,
                defaults, extraName, retType, body, (short)lEnv.size(),
                env.getLevel());
    }

    // assumes that the type has been compiled, and that the first identifier is
    // the previous token of the lexer.
    private Statement cDecl(CompilerEnvironment env, TypeExpr type,
            String name1,
            int ctxt) {
        // isSeq determines whether this statement is part of a statement
        // sequence,
        // ie. that the following separator should not be read.
        boolean isSeq = (ctxt & CTXT_SEQ) == CTXT_SEQ;
        ArrayList names = new ArrayList(4);
        ArrayList initials = new ArrayList(4);
        // typ: the variable's declared type; null implies var declaration.
        Type typ = (type == null ? null : type.getType()),
                t = typ, // t : the declared type to be used
                ctxtType = (type == null ? HojoLib.OBJ_TYPE : typ); // ctxtType:
                                                                    // the
                                                                    // context
                                                                    // type for
                                                                    // the value
        Expression expr;
        String name;
        short startAddress = -1;
        short addr;

        if (type != null && type.getType().kind() == Type.TYP_VOID) {
            throw new HojoException(null, HojoException.ERR_EXPECTED_TOKEN,
                    new String[] { stx.punctuators[PCT_IDX_LPAREN] },
                    lex.currentLocation());
        }

        do {
            // get the new identifier name - if not at the start - and verify
            // that the
            // name is unique
            if (name1 != null) {
                name = name1;
                name1 = null;
            }
            else {
                lex.nextToken(TT_WORD);
                name = lex.sval;
            }

            if (env.getAddress(name) >= 0) {
                throw new HojoException(null, HojoException.ERR_DUPLICATE_ID,
                        new String[] { name, env.getType(name).toString(stx) },
                        lex.currentLocation());
            }

            if (lex.nextToken(TT_ANY) == PCT_DELIMITER
                    || lex.ttype == PCT_SEPARATOR) {
                // no initializer - use the default value
                expr = ctxtType.defaultCode();
            }
            else {
                // ensure that an assignment operator was given
                if (lex.ttype != TT_OPERATOR || lex.id != OP_ASSIGN) {
                    throw new HojoException(null,
                            HojoException.ERR_EXPECTED_TOKEN,
                            new String[] { stx.operators[OP_IDX_ASSIGN] },
                            lex.currentLocation());
                }

                // parse the initializer
                expr = cExpr(env, ctxtType);

                // check that the type is compatible, and generate type cast
                if (typ != null) {
                    expr = forceType(expr, typ);
                }

                // read one token ahead
                lex.nextToken(TT_ANY);
            }
            names.add(name);
            initials.add(expr);

            // determine the type of the new variable, and update the
            // environment with
            // that information. Save the address of the first variable.
            if (typ == null) {
                t = expr.getType();
                int k = t.kind();
                if (k == Type.TYP_NULL || k == Type.TYP_VOID) {
                    // use the type Object for the null or unit expression
                    t = HojoLib.OBJ_TYPE;
                }
            }
            // else t == typ holds
            addr = env.alloc(name, t, (short)ctxt);
            if (startAddress < 0) {
                startAddress = addr;
            }
        } // continue as long as a delimiter is next.
        while (lex.ttype == PCT_DELIMITER);

        // get the separator iff not in an expression sequence
        lex.pushBack();
        if (!isSeq) {
            lex.nextToken(PCT_SEPARATOR);
        }

        // create the appropriate code and return it
        String[] names_ = (String[])names.toArray(new String[names.size()]);
        Expression[] initials_ = (Expression[])initials.toArray(
                new Expression[initials.size()]);
        return new VarDeclStm(names_, initials_, typ, (short)ctxt,
                startAddress);
    }

    // compile a block of statements in the given context. If end !=
    // PCT_BLOCKEND
    // this implies that PCT_DELIMITER can be used to delimit the sequence and
    // that
    // the delimiter has to be read explicitly <=> (ctxt & CTXT_SEQ) ==
    // CTXT_SEQ.
    private Statement[] cBlock(CompilerEnvironment env, Type typ, int ctxt,
            int end) {
        ArrayList stms = new ArrayList();
        Statement stm;
        boolean readDelim = (ctxt & CTXT_SEQ) == CTXT_SEQ;

        while (lex.nextToken(TT_ANY) != TT_EOF) {
            while (lex.ttype == PCT_SEPARATOR && end == PCT_BLOCKEND) {
                // skip leading separators
                lex.nextToken(TT_ANY);
            }

            if (lex.ttype == end) {
                break;
            }
            lex.pushBack();

            if ((stm = cStm(env, typ, ctxt)) == null) {
                break;
            }

            stms.add(checkNonvoidExpr(stm));

            if (readDelim && lex.ttype != end) {
                if (lex.ttype != PCT_DELIMITER) {
                    throw new HojoException(null,
                            HojoException.ERR_EXPECTED_TOKEN,
                            new String[] { stx.punctuators[PCT_IDX_DELIMITER] },
                            lex.currentLocation());
                }
                lex.nextToken(TT_ANY);
            } // while
        }
        return (Statement[])stms.toArray(new Statement[stms.size()]);
    }

    // compile a block or single statement such that the block has its own
    // environment
    private Statement cBlockOpt(CompilerEnvironment env, Type typ, int ctxt) {
        if (lex.nextToken(TT_ANY) == PCT_BLOCKSTART) {
            CompilerEnv blockEnv = new CompilerEnv(env);
            return new BlockEnvStatement(cBlock(blockEnv,
                    typ, ctxt & CTXT_ALLOW_DECL, PCT_BLOCKEND),
                    (short)blockEnv.size());
        }
        else if (lex.ttype == PCT_SEPARATOR) {
            return NOP.NOP;
        }
        else {
            lex.pushBack();
            return checkNonvoidExpr(cStm(env, typ, ctxt | CTXT_NODECL));
        }
    }

    private Statement cIfStm(CompilerEnvironment env, Type typ, int ctxt) {
        lex.nextToken(PCT_LPAREN);
        Expression cond = cExpr(env, HojoLib.BOOLEAN_TYPE);
        lex.nextToken(PCT_RPAREN);

        Statement block = cBlockOpt(env, typ, ctxt);
        if (lex.nextToken(TT_ANY) != RES_ELSE) {
            lex.pushBack();
            return new ShortIfStm(cond, block);
        }

        return new IfStm(cond, block, cBlockOpt(env, typ, ctxt));
    }

    private Statement cDoStm(CompilerEnvironment env, Type typ, int ctxt) {
        Statement block = cBlockOpt(env, typ, ctxt | CTXT_LOOP);
        lex.nextToken(RES_WHILE);
        lex.nextToken(PCT_LPAREN);
        Expression cond = cExpr(env, HojoLib.BOOLEAN_TYPE);
        lex.nextToken(PCT_RPAREN);
        lex.nextToken(PCT_SEPARATOR);
        return new DoStm(cond, block);
    }

    private Statement cWhileStm(CompilerEnvironment env, Type typ, int ctxt) {
        lex.nextToken(PCT_LPAREN);
        Expression cond = cExpr(env, HojoLib.BOOLEAN_TYPE);
        lex.nextToken(PCT_RPAREN);
        Statement block = cBlockOpt(env, typ, ctxt | CTXT_LOOP);
        return new WhileStm(cond, block);
    }

    private Statement cForStm(CompilerEnvironment env, Type typ, int ctxt) {
        if (lex.nextToken(TT_ANY) == TT_WORD || lex.ttype == RES_VAR ||
                lex.ttype == TT_TYPE) {
            // for [Type] name [, counter] in e { ... }

            // get the type specifier, if any
            Type varType = null;
            if (lex.ttype == RES_VAR) {
                lex.nextToken(TT_WORD);
            }
            else if (lex.ttype != TT_WORD) {
                lex.pushBack();
                varType = HojoLib.typeOf(cClassName(env, null, false));
                lex.nextToken(TT_WORD);
            }

            // get the variable and optional counter name
            String name = lex.sval;
            String countName = null;
            if (lex.nextToken(TT_ANY) == PCT_DELIMITER) {
                lex.nextToken(TT_WORD);
                countName = lex.sval;
                lex.nextToken(TT_ANY);
            }

            // ensure that the OP_ELEM is next
            if (lex.ttype != TT_OPERATOR || lex.id != OP_ELEM) {
                throw new HojoException(null, HojoException.ERR_EXPECTED_TOKEN,
                        new String[] { stx.operators[OP_IDX_ELEM] },
                        lex.currentLocation());
            }

            // compile the sequence expression
            Expression seq = cExpr(env, HojoLib.OBJ_TYPE);

            // infer the type of the variable, if necessary
            if (varType == null) {
                varType = seq.getType();
                switch (varType.kind()) {
                case Type.TYP_COLLECTION:
                case Type.TYP_SET:
                case Type.TYP_LIST:
                case Type.TYP_ARRAY:
                    varType = HojoLib.typeOf(varType.elementClass());
                    break;
                case Type.TYP_ITERATOR:
                    if (seq instanceof SequenceExpr) {
                        varType = ((SequenceExpr)seq).getElementType();
                    }
                    else {
                        varType = HojoLib.typeOf(varType.elementClass());
                    }
                    break;
                case Type.TYP_CHAR_SEQUENCE:
                case Type.TYP_STRINGBUFFER:
                case Type.TYP_STRING:
                    varType = HojoLib.CHARACTER_TYPE;
                    break;
                default:
                    varType = HojoLib.OBJ_TYPE;
                }
            }

            // create a new environment for the body
            CompilerEnv env2 = new CompilerEnv(env);
            env2.alloc(name, varType, (short)MOD_FINAL);
            if (countName != null) {
                env2.alloc(countName, HojoLib.INT_TYPE, (short)MOD_FINAL);
            }

            // compile the block in the new environment
            lex.nextToken(PCT_BLOCKSTART);
            Statement[] body = cBlock(env2, HojoLib.OBJ_TYPE,
                    ctxt | CTXT_LOOP, PCT_BLOCKEND);

            // return the result
            return new ForSeqStm(seq, name, countName, varType,
                    body, (short)env2.size());
        }

        // ordinary (Java-style) for-statement
        lex.pushBack();
        lex.nextToken(PCT_LPAREN);
        CompilerEnv env2 = new CompilerEnv(env);

        Statement init = getBlock(
                cBlock(env2, HojoLib.OBJ_TYPE, CTXT_SEQ, PCT_SEPARATOR));

        Expression cond;
        if (lex.nextToken(TT_ANY) == PCT_SEPARATOR) {
            cond = null;
        }
        else {
            lex.pushBack();
            cond = cExpr(env2, HojoLib.BOOLEAN_TYPE);
            lex.nextToken(PCT_SEPARATOR);
        }

        Statement update = getBlock(
                cBlock(env2, HojoLib.OBJ_TYPE, CTXT_SEQ | CTXT_NODECL,
                        PCT_RPAREN));

        Statement[] stms;
        if (lex.nextToken(TT_ANY) == PCT_BLOCKSTART) {
            stms = cBlock(env2, typ, ctxt | CTXT_LOOP, PCT_BLOCKEND);
        }
        else if (lex.ttype == PCT_SEPARATOR) {
            stms = new Statement[0];
        }
        else {
            lex.pushBack();
            stms = new Statement[] { cStm(env2, typ, ctxt | CTXT_LOOP) };
        }

        return new ForStm(init, cond, update, stms, (short)env2.size());
    }

    // compile a block in a switch statement.
    // It is assumed that ctxt holds CTXT_BREAK as well as CTXT_NODECL.
    // The arraylist is (re)used to store statements, the 0th element of the
    // guard
    // will be the block's guard; the default clause has Const.DEFAULT as guard.
    private Statement cSwitchBlock(CompilerEnvironment env, Type typ, int ctxt,
            ArrayList tmp, Expression[] guard) {
        tmp.clear();
        if (lex.nextToken(TT_ANY) == RES_CASE) {
            guard[0] = cExpr(env, HojoLib.OBJ_TYPE);
        }
        else if (lex.ttype == RES_DEFAULT) {
            guard[0] = Const.DEFAULT;
        }
        else {
            lex.pushBack();
            lex.nextToken(PCT_BLOCKEND);
            return null;
        }

        // get the case label / else operator:
        if (lex.nextToken(TT_ANY) != TT_CASELABEL ||
                (TT_CASELABEL == TT_OPERATOR && lex.id != OP_ELSE)) {
            throw new HojoException(null, HojoException.ERR_EXPECTED_TOKEN,
                    new String[] { stx.punctuators[PCT_IDX_CASELABEL] },
                    lex.currentLocation());
        }

        // compile each statement in the block
        loop: while (true) {
            switch (lex.nextToken(TT_ANY)) {
            case RES_CASE:
            case RES_DEFAULT:
            case PCT_BLOCKEND:
                break loop;
            default:
                lex.pushBack();
                tmp.add(cStm(env, typ, ctxt));
            }
        }

        lex.pushBack();
        Statement[] stms = (Statement[])tmp.toArray(new Statement[tmp.size()]);
        return getBlock(stms);
    }

    private Statement cSwitchStm(CompilerEnvironment env, Type typ, int ctxt) {
        lex.nextToken(PCT_LPAREN);
        Expression expr = cExpr(env, HojoLib.OBJ_TYPE);
        lex.nextToken(PCT_RPAREN);
        lex.nextToken(PCT_BLOCKSTART);

        ArrayList blocks = new ArrayList();
        ArrayList guards = new ArrayList();
        ArrayList tmp = new ArrayList();
        Expression[] guard = new Expression[1];
        int ctxtBlock = ctxt | CTXT_BREAK | CTXT_NODECL;
        Statement s;

        // compile one block at a time, until PCT_BLOCKEND is reached
        while ((s = cSwitchBlock(env, typ, ctxtBlock, tmp, guard)) != null) {
            blocks.add(s);
            guards.add(guard[0]);
            if (guard[0] == Const.DEFAULT) {
                // ensure that the block ends after the default clause
                lex.nextToken(PCT_BLOCKEND);
                break;
            }
        }

        return new SwitchStm(expr,
                (Statement[])blocks.toArray(new Statement[blocks.size()]),
                (Expression[])guards.toArray(new Expression[guards.size()]));
    }

    private Statement cSyncStm(CompilerEnvironment env, Type typ, int ctxt) {
        lex.nextToken(PCT_LPAREN);
        Expression lock = cExpr(env, HojoLib.OBJ_TYPE);
        if (lock == Const.NULL) {
            throw new HojoException(null, HojoException.ERR_SYNTAX, null,
                    lex.currentLocation());
        }
        lex.nextToken(PCT_RPAREN);
        Statement block = cBlockOpt(env, typ, ctxt);
        return new SyncStm(lock, block);
    }

    private Statement cTryStm(CompilerEnvironment env, Type typ, int ctxt) {
        // use cBlockOpt instead of cBlock to generate a BlockEnvStatement
        lex.nextToken(PCT_BLOCKSTART);
        lex.pushBack();
        Statement tryBlock = cBlockOpt(env, typ, ctxt);

        ArrayList catchBlocks = new ArrayList();
        CompilerEnvironment env2 = null;
        Class matchType = null;
        String name = null;
        Statement[] catchBlock;

        while (lex.nextToken(TT_ANY) == RES_CATCH) {
            lex.nextToken(PCT_LPAREN);
            matchType = cClassName(null, HojoLib.OBJ_TYPE, false);

            if (!Throwable.class.isAssignableFrom(matchType)) {
                throw new HojoException(null, HojoException.ERR_CATCH_TYPE,
                        new String[] { ReflectUtils.className2Java(matchType) },
                        lex.currentLocation());
            }

            lex.nextToken(TT_WORD);
            name = lex.sval;
            lex.nextToken(PCT_RPAREN);
            lex.nextToken(PCT_BLOCKSTART);

            env2 = new CompilerEnv(env);
            env2.alloc(name, HojoLib.typeOf(matchType), (short)MOD_FINAL);
            catchBlock = cBlock(env2, typ, ctxt, PCT_BLOCKEND);
            catchBlocks.add(new CatchClause(catchBlock, (short)env2.size(),
                    matchType, name));
        }

        lex.pushBack();

        Statement finallyBlock = null;
        if (lex.nextToken(catchBlocks.size() == 0 ? RES_FINALLY
                : TT_ANY) == RES_FINALLY) {
            lex.nextToken(PCT_BLOCKSTART);
            lex.pushBack();
            finallyBlock = cBlockOpt(env, typ, ctxt);
        }
        else {
            lex.pushBack();
        }

        return new TryStm(tryBlock, (CatchClause[])catchBlocks.toArray(
                new CatchClause[catchBlocks.size()]), finallyBlock);
    }

    /* ******************** EXPRESSIONS ******************** */

    // compile an expression
    private Expression cExpr(CompilerEnvironment env, Type typ) {
        Expression e = cInfixExpr(env, typ, OP_PRIO_0);
        if (e.isType()) {
            throw new HojoException(null, HojoException.ERR_EXPECTED_TOKEN,
                    new String[] { stx.operators[OP_IDX_DOT] },
                    lex.currentLocation());
        }
        return e;
    }

    // compile an infix operation expression at the given operator priority
    private Expression cInfixExpr(CompilerEnvironment env, Type typ, int prio) {
        if (prio >= OP_PRIO_PREFIX) {
            return cPrefixExpr(env, typ);
        }
        else if (prio == OP_PRIO_TERNARY) {
            return cTernaryExpr(env, typ);
        }

        // compile the first operand expression, at the next priority
        Expression e1 = cInfixExpr(env, typ, prio + OP_PRIO_1);
        Function op;
        Expression e2;
        long id;
        int assoc;
        boolean isAssignment;

        // repeat as long as an operator of this priority follows
        while (lex.nextToken(TT_ANY) == TT_OPERATOR
                && ((lex.id & OP_PRIO_MASK) == prio)) {
            // get the compiled code for the operator
            op = (Function)operators.get(lex.oval);
            id = ((Number)lex.oval).longValue();

            // ensure that assignments are valid
            isAssignment = prio == OP_PRIO_ASSIGN;
            if (isAssignment) {
                checkLValue(e1);
                checkCompoundOp(e1, (int)(id >> 32));
            }

            if ((assoc = (lex.id & OP_ASSOC_MASK)) == OP_RIGHTASSOC) {
                // right-associative operator - parse the rhs and return the
                // code
                // use the lhs type contex in assignments
                e2 = cInfixExpr(env,
                        isAssignment ? e1.getType() : HojoLib.OBJ_TYPE, prio);
                return mkBinExpr(e1, id, op, e2);
            }

            // left-associative operator - parse the next operand, and create
            // code
            e2 = cInfixExpr(env, HojoLib.OBJ_TYPE, prio + OP_PRIO_1);
            e1 = mkBinExpr(e1, id, op, e2);

            // exit the loop, if the operator does not associate
            if (assoc == OP_NONASSOC) {
                lex.nextToken(TT_ANY);
                break;
            }
        }
        lex.pushBack();
        return e1;
    }

    // compile a ternary expression
    private Expression cTernaryExpr(CompilerEnvironment env, Type typ) {
        // compile the first expression
        Expression e1 = cInfixExpr(env, typ, OP_PRIO_COND_OR);

        // return e1 if no ternary operator follows
        if (lex.nextToken(TT_ANY) != TT_OPERATOR || lex.id == OP_ELSE ||
                ((lex.id & OP_ARITY_MASK) != OP_TERNARY)) {
            lex.pushBack();
            return e1;
        }

        // determine which operator to use between the second and third operand.
        int id = lex.id;
        int id2;
        String endToken;

        if (id == OP_IFTHEN) {
            id2 = OP_ELSE;
            endToken = stx.operators[OP_IDX_ELSE];
        }
        else {
            id2 = id;
            endToken = lex.sval;
        }

        Function op = (Function)operators.get(lex.oval);

        // compile the second operand
        Expression e2 = (id == OP_IFTHEN) ? cTernaryExpr(env, typ)
                : cInfixExpr(env, typ, OP_PRIO_TERNARY + OP_PRIO_1);

        // look for the second operator - if id == id2 then this is optional.
        // Compile the third operand or use null, if the optional operator was
        // not found.
        Expression e3 = null;
        if (lex.nextToken((id == id2) ? TT_ANY : TT_OPERATOR) == TT_OPERATOR) {
            if (lex.id != id2) {
                throw new HojoException(null, HojoException.ERR_EXPECTED_TOKEN,
                        new String[] { endToken }, lex.currentLocation());
            }
            e3 = cTernaryExpr(env, typ);
        }
        else {
            lex.pushBack();
        }

        // create the code and return it
        switch (id) {
        case OP_IFTHEN:
            try {
                return new IfThenElseExpr(e1, e2, e3);
            }
            catch (HojoException e) {
                e.setLocation(lex.currentLocation());
                throw e;
            }
        case OP_SEQ:
            return new SequenceExpr(e1, e2, e3);
        default:
            return new TernaryOp(op, e1, e2, e3);
        }
    }

    // compile a prefix expression
    private Expression cPrefixExpr(CompilerEnvironment env, Type typ) {
        switch (lex.nextToken(TT_ANY)) {
        case TT_OPERATOR:
            if (((lex.id & OP_PRIO_MASK) != OP_PRIO_PREFIX)
                    && lex.id != TT_OP_NEG) {
                // not a prefix operator
                break;
            }
            if (lex.id == TT_OP_NEG) {
                // handle unary minus specially in order to produce negative
                // numeric
                // literals
                if (lex.isDigit(lex.peek())) {
                    // negative literal
                    int idx = (typ.kind() == Type.TYP_NUMBER)
                            ? ((NumberType)typ).numberType()
                            : NUM_PRI_BAD;
                    return new Const(lex.parseNumber(
                            (idx == NUM_PRI_BAD) ? '\0' : stx.suffixes[idx],
                            true));
                }
                else {
                    Operator op = HojoLib.operators[OP_IDX_NEG];
                    return new UnaryOp(op, cPrefixExpr(env,
                            HojoLib.typeOf(op.getParameterTypes()[0])));
                }
            }
            else if (lex.id == OP_INC || lex.id == OP_DEC) {
                // handle prefix increment/decrement seperately
                boolean inc = lex.id == OP_INC;
                Expression e = cPrefixExpr(env, typ);
                checkLValue(e);
                checkCompoundOp(e, 0);
                return inc ? (Expression)new PreIncOp(e)
                        : (Expression)new PreDecOp(e);
            }
            else {
                // vanilla operator
                return new UnaryOp((Function)operators.get(lex.oval),
                        cPrefixExpr(env, typ));
            }
        case PCT_LPAREN:
            // parenthesized expression, type cast expression or void
            // expression.
            // Parse a full expression and see whether the result is a type and
            // is
            // not terminated by a right parenthesis, a delimiter, separator or
            // by an
            // operator of a priority lower than OP_PRIO_PREFIX.
            if (lex.nextToken(TT_ANY) == PCT_RPAREN) {
                // void expression
                return cPostfixExpr(VoidExpr.getInstance(), env);
            }
            else {
                lex.pushBack();
            }

            Expression e = cInfixExpr(env, HojoLib.OBJ_TYPE, OP_PRIO_0);
            if (lex.nextToken(TT_ANY) == PCT_DELIMITER && e.isType()) {
                // extended type cast expression
                Class base = e.getTypeC();
                Class elem = cClassName(env, HojoLib.OBJ_TYPE, false);
                lex.nextToken(PCT_RPAREN);

                Type t;
                if (base.isArray()) {
                    t = new GenericArrayType(base, elem);
                }
                else if (Collection.class.isAssignableFrom(base)) {
                    t = new GenericCollectionType(base, elem);
                }
                else if (Map.class.isAssignableFrom(base)) {
                    t = new GenericMapType(base, elem);
                }
                else {
                    throw new HojoException(null, HojoException.ERR_TYPECAST,
                            null, lex.currentLocation());
                }
                return TypecastExpr.mkTypecast(t, cPrefixExpr(env, t));
            }
            lex.pushBack();
            lex.nextToken(PCT_RPAREN);
            lex.nextToken(TT_ANY);
            if (e.isType()) {
                switch (lex.ttype) {
                case PCT_SEPARATOR:
                case PCT_DELIMITER:
                case PCT_RPAREN:
                case PCT_BLOCKEND:
                case PCT_IDXEND:
                case PCT_MAPEND:
                case PCT_ARRAYEND:
                case PCT_LISTEND:
                    // not a typecast expression
                    break;
                case TT_OPERATOR:
                    if (((lex.id & OP_PRIO_MASK) < OP_PRIO_PREFIX)
                            && (lex.id != TT_OP_NEG)) {
                        // not a typecast expression
                        break;
                    }
                    // fall through:
                default:
                    // vanilla type cast expression
                    lex.pushBack();
                    Type t = e.getType();
                    return TypecastExpr.mkTypecast(t, cPrefixExpr(env, t));
                }
            } // isType()
              // parenthesized expression
            lex.pushBack();
            return cPostfixExpr(e, env);
        } // switch
        lex.pushBack();
        return cPostfixExpr(cPrimaryExpr(env, typ), env);
    }

    // compile a postfix expression given the compiled primary expression. It is
    // assumed that the last token of the expression directly precedes the
    // current
    // input token.
    private Expression cPostfixExpr(Expression primary,
            CompilerEnvironment env) {
        loop: while (true) {
            switch (lex.nextToken()) {
            case PCT_LPAREN:
                if (primary instanceof ScopeRef) {
                    break loop;
                }

                // function application / constructor invocation
                Type t = primary.getType();
                Class[] argTypes = FunctionType.VARIABLE_ARGS;
                Class cls = t.toClass();
                int kind = t.kind();

                if (kind == Type.TYP_HCLASS) {
                    // Hojo class constructor
                    lex.nextToken(PCT_RPAREN);
                    return new HObjectCreateExpr(primary);
                }

                if (kind == Type.TYP_FUNCTION) {
                    argTypes = ((FunctionType)t).getParameterTypes();
                }
                else if (!primary.isType() && (kind == Type.TYP_NULL ||
                        !(Function.class.isAssignableFrom(cls) ||
                                cls.isAssignableFrom(Function.class)))) {
                    throw new HojoException(null, HojoException.ERR_FUNCTION,
                            new String[] { t.toString(stx) },
                            lex.currentLocation());
                }

                Expression[] argList = cExprList(env, ObjectType.getInstance(),
                        argTypes, primary.isType() ? ERR_EXPR : Const.NO_ARG,
                        PCT_RPAREN);

                if (primary.isType()) {
                    Constructor constr = getConstructor(cls, typesOf(argList),
                            optStrictArgTypes);
                    primary = (constr == null)
                            ? (Expression)new DelayedConstructorExpr(cls,
                                    argList)
                            : (Expression)new ConstructorExpr(constr, argList);
                }
                else {
                    if (primary.isConst()) {
                        // verify missing arguments, if the function is known
                        // at compile time
                        Function f = (Function)primary.xeq(null);
                        if (f != null) {
                            int arity = f.getArity();
                            if ((f.getExtraParameterName() == null &&
                                    argList.length > arity) ||
                                    argList.length < arity) {
                                throw new HojoException(null,
                                        HojoException.ERR_ARG_COUNT,
                                        new String[] { "" + arity,
                                                "" + argList.length
                                        },
                                        lex.currentLocation());
                            }
                            else {
                                for (int i = 0; i < arity; i++) {
                                    if (argList[i] == Const.NO_ARG &&
                                            f.getDefaultValue(
                                                    i) == Function.NO_ARG) {
                                        String[] pnames = f.getParameterNames();
                                        throw new HojoException(null,
                                                HojoException.ERR_ARG_MISSING,
                                                new String[] { "" + i,
                                                        pnames == null ? ""
                                                                : pnames[i]
                                                }, lex.currentLocation());
                                    }
                                }
                            }
                        }
                    }
                    primary = new ApplyExpr(primary, argList);
                }
                break;
            case PCT_IDXSTART:
                if (primary instanceof ScopeRef) {
                    break loop;
                }
                // index expression
                Expression idx = cExpr(env, HojoLib.INT_TYPE);
                Expression idx2 = null;
                if (lex.nextToken(TT_ANY) == PCT_DELIMITER) {
                    if (lex.nextToken(TT_ANY) == PCT_IDXEND) {
                        lex.pushBack();
                        idx2 = Const.ELLIPSIS;
                    }
                    else {
                        lex.pushBack();
                        idx2 = cExpr(env, HojoLib.INT_TYPE);
                    }
                }
                else {
                    lex.pushBack();
                }
                lex.nextToken(PCT_IDXEND);

                Class c = primary.getTypeC();
                if (CharSequence.class.isAssignableFrom(c)) {
                    primary = (idx2 == null)
                            ? (Expression)new CharSequenceIndexExpr(primary,
                                    idx)
                            : (Expression)new CharSequenceIndexRangeExpr(
                                    primary, idx, idx2);
                }
                else if (c.isArray()) {
                    primary = (idx2 == null)
                            ? (Expression)new ArrayIndexExpr(primary, idx)
                            : (Expression)new ArrayIndexRangeExpr(primary, idx,
                                    idx2);
                }
                else if (List.class.isAssignableFrom(c)) {
                    primary = (idx2 == null)
                            ? (Expression)new ListIndexExpr(primary, idx)
                            : (Expression)new ListIndexRangeExpr(primary, idx,
                                    idx2);
                }
                else if (optStrictIndexTypes || primary.isConst()) {
                    throw new HojoException(null, HojoException.ERR_INDEX,
                            new String[] { ReflectUtils.className2Java(c) },
                            lex.currentLocation());
                }
                else {
                    if (obs != null) {
                        obs.handleWarning(new HojoException(null,
                                HojoException.WARN_INDEX,
                                new String[] { ReflectUtils.className2Java(c) },
                                lex.currentLocation()));
                    }
                    primary = new GenericIndexExpr(primary, idx);
                }
                break;
            case TT_OPERATOR:
                boolean isScopeRef = primary instanceof ScopeRef;
                if (lex.id != OP_DOT && isScopeRef) {
                    break loop;
                }

                switch (lex.id) {
                case OP_FUNC:
                case OP_DOT:
                    // member access expression - ensure that the lhs is not
                    // constant null,
                    // and that it is a type, in case the operator is OP_DOT
                    int ID = lex.id;
                    Type typ = primary.getType();
                    kind = typ.kind();
                    boolean isType = primary.isType();

                    if (kind == Type.TYP_NULL || kind == Type.TYP_VOID) {
                        throw new HojoException(null, HojoException.ERR_SYNTAX,
                                null, lex.currentLocation());
                    }
                    if (ID == OP_FUNC && !isType) {
                        throw new HojoException(null, HojoException.ERR_FUNC_OP,
                                null, lex.currentLocation());
                    }

                    // check for OP_FUNC syntax without identifier name
                    // (constructor function). Also, allow any identifiers
                    // at this point
                    lex.nextToken(TT_ANY, true);
                    if (ID == OP_FUNC && lex.ttype == PCT_LPAREN) {
                        Class cl = typ.toClass();
                        if (cl.isArray()) {
                            // array allocator
                            lex.nextToken(PCT_RPAREN);
                            primary = new Const(new AllocatorFunction(cl));
                        }
                        else {
                            primary = new Const(new ConstructorFunction(
                                    getConstructor(cl,
                                            cTypeList(HojoLib.OBJ_TYPE,
                                                    PCT_RPAREN),
                                            true)));
                        }
                        continue;
                    }
                    // get the field/method name, and allow any identifier here
                    lex.pushBack();
                    lex.nextToken(TT_WORD, true);
                    String name = lex.sval;

                    // handle scope references
                    if (isScopeRef) {
                        if (name.equals(
                                stx.reserved[RES_SUPER - RES_BASE_ID])) {
                            // .super on a scope ref
                            ((ScopeRef)primary).addLink();
                        }
                        else {
                            // .name on a scope ref - find the variable and
                            // substitute
                            if ((primary = ((ScopeRef)primary).link(env,
                                    name)) == null) {
                                throw new HojoException(null,
                                        HojoException.ERR_UNKNOWN_ID,
                                        new String[] { name },
                                        lex.currentLocation());
                            }
                        }
                        continue;
                    }

                    Class base = typ.toClass();

                    // check for method invocation / function application
                    // (enforce member access + application combo, if the lhs is
                    // a
                    // HObject, even when the following token is a left
                    // parenthesis)
                    if (lex.nextToken(TT_ANY) == PCT_LPAREN &&
                            !(kind == Type.TYP_HOBJECT && ID == OP_DOT)) {
                        if (ID == OP_DOT) {
                            Expression[] exprs = cExprList(env,
                                    HojoLib.OBJ_TYPE,
                                    FunctionType.VARIABLE_ARGS, ERR_EXPR,
                                    PCT_RPAREN);

                            if (kind == Type.TYP_ARRAY && exprs.length == 0
                                    && name.equals("clone")) {
                                primary = new ArrayCloneExpr(primary);
                            }
                            else {
                                Method m = getMethod(base, name, typesOf(exprs),
                                        isType, isType || optStrictArgTypes);
                                primary = (m == null)
                                        ? (Expression)new DelayedInvokeExpr(
                                                primary, name, exprs)
                                        : (Expression)new InvokeExpr(
                                                primary.isType() ? null
                                                        : primary,
                                                m, exprs);
                            }
                        }
                        else {
                            primary = new Const(new MethodFunction(
                                    getMethod(base, name,
                                            cTypeList(HojoLib.OBJ_TYPE,
                                                    PCT_RPAREN),
                                            false, true)));
                        }
                    }
                    else {
                        lex.pushBack();
                        // check for special field names, or a map lhs
                        if (ID == OP_DOT) {
                            if (name.equals(
                                    stx.reserved[RES_CLASS - RES_BASE_ID])) {
                                // class literal
                                primary = new Const(base);
                                continue;
                            }
                            else if (kind == Type.TYP_MAP) {
                                // map member access
                                primary = new MapMemberExpr(primary, name);
                                continue;
                            }
                            else if (kind == Type.TYP_HOBJECT) {
                                // Hojo class member - create a new constant
                                // (variable) if
                                // possible, else create a class member
                                // expression
                                if (primary instanceof Const) {
                                    HObject obj = (HObject)primary.xeq(null);
                                    Variable v;
                                    try {
                                        v = obj.get(name);
                                    }
                                    catch (NoSuchFieldException e) {
                                        throw new HojoException(e,
                                                HojoException.ERR_FIELD,
                                                new String[] { obj.toString(),
                                                        name },
                                                lex.currentLocation());
                                    }
                                    if (v.isFinal()) {
                                        primary = new Const(v.getValue());
                                    }
                                    else {
                                        primary = v;
                                    }
                                }
                                else {
                                    primary = new HClassMemberExpr(primary,
                                            name);
                                }
                                continue;
                            }
                            else if (kind == Type.TYP_ARRAY
                                    && name.equals("length")) {
                                // array length pseudo-field
                                primary = new ArrayLengthExpr(primary);
                                continue;
                            }
                        }

                        // field access
                        Field f = getField(base, name, isType,
                                optStrictFieldTypes || ID == OP_FUNC || isType);
                        if (ID == OP_DOT) {
                            primary = (f == null)
                                    ? (Expression)new DelayedFieldExpr(primary,
                                            name)
                                    : mkFieldExpr(f, primary);
                        }
                        else {
                            primary = new Const(new FieldFunction(f));
                        }
                    }
                    break;
                case OP_INC:
                case OP_DEC:
                    // post increment/decrement expression
                    checkLValue(primary);
                    checkCompoundOp(primary, 0);
                    primary = (lex.id == OP_INC)
                            ? (Expression)new PostIncOp(primary)
                            : (Expression)new PostDecOp(primary);
                    break;
                default:
                    if ((lex.id & OP_PRIO_MASK) != OP_PRIO_POSTFIX) {
                        // no more postfix operators
                        break loop;
                    }
                    // vanilla postfix operator
                    primary = new PostfixOp((Function)operators.get(lex.oval),
                            primary);
                    break;
                }
                break;
            default:
                break loop;
            }
        }

        lex.pushBack();

        if (primary instanceof ScopeRef) {
            if (((ScopeRef)primary).getLength() == 0) {
                // allow 'this' as alias for the runtime environment map view
                if (env instanceof HojoRuntime) {
                    return new Const(((HojoRuntime)env).asMap());
                }
                else if (env instanceof BaseEnv) {
                    return new Const(((BaseEnv)env).getLink().asMap());
                }
            }
            throw new HojoException(null, HojoException.ERR_EXPECTED_TOKEN,
                    new String[] { stx.operators[OP_IDX_DOT] },
                    lex.currentLocation());
        }
        else {
            return primary;
        }
    }

    // compile a primary expression
    private Expression cPrimaryExpr(CompilerEnvironment env, Type typ) {
        Type t;

        switch (lex.nextToken(TT_ANY)) {
        case TT_NUMBER:
            int idx = (typ.kind() == Type.TYP_NUMBER)
                    ? ((NumberType)typ).numberType()
                    : NUM_PRI_BAD;
            return new Const(lex.parseNumber(
                    (idx == NUM_PRI_BAD) ? '\0' : stx.suffixes[idx], false));
        case TT_LITERAL:
            return (lex.oval instanceof Expression) ? (Expression)lex.oval
                    : new Const(lex.oval);
        case RES_VAR:
        case TT_TYPE:
            lex.pushBack();
            return new TypeExpr(cClassName(env, typ, false));
        case RES_LET:
            lex.nextToken(PCT_BLOCKSTART);
            CompilerEnv env2 = new CompilerEnv(env);
            Statement[] stms = cBlock(env2, null, CTXT_RETURN, PCT_BLOCKEND);
            Type rt = null;

            // infer the type of the statement, and check for uncreachable
            // statements
            // at the same time.
            try {
                if ((rt = Statement.checkBlock(stms, null)) == null) {
                    rt = VoidType.getInstance();
                }
            }
            catch (HojoException e) {
                e.setLocation(lex.currentLocation());
                throw e;
            }
            catch (UnreachableStatementException e) {
                throw new HojoException(e, HojoException.ERR_UNREACHABLE,
                        new String[] { e.getStatement().toString(stx,
                                StringUtils.defaultFormat, "")
                        }, lex.currentLocation());
            }
            return new LetExpr((short)env2.size(), rt, stms);
        case RES_THIS:
            return new ScopeRef((short)0);
        case RES_SUPER:
            return new ScopeRef((short)1);
        case TT_WORD:
            if (packagePrefixes.contains(lex.sval)) {
                lex.pushBack();
                return new TypeExpr(cClassName(env, typ, false));
            }
            // variable - look up the address and create code
            Type[] vType = new Type[1];
            short[] varAddr = env == null ? null : env.findVar(lex.sval, vType);
            if (varAddr == null) {
                throw new HojoException(null, HojoException.ERR_UNKNOWN_ID,
                        new String[] { lex.sval }, lex.currentLocation());
            }

            return new VarExpr(varAddr, vType[0]);
        case RES_OP:
            lex.nextToken(TT_ANY);
            switch (lex.ttype) {
            case TT_OPERATOR:
                Object op = operators.get(lex.oval);
                if (op == null) {
                    // impure operator - error
                    throw new HojoException(null, HojoException.ERR_OP_SYNTAX,
                            null,
                            lex.currentLocation());
                }
                return new Const(op);
            case PCT_LPAREN:
                if (lex.nextToken(TT_ANY) == PCT_RPAREN) {
                    return new Const(IdOp.getInstance());
                }
                lex.pushBack();
                Class c = cClassName(env, typ, false);
                lex.nextToken(PCT_RPAREN);
                return new Const(new TypeCastFunction(HojoLib.typeOf(c)));
            case PCT_IDXSTART:
                lex.nextToken(PCT_IDXEND);
                return new Const(IndexFunction.getInstance());
            /*
             * case PCT_BLOCKSTART: Class cls = cClassName(env, typ, false);
             *
             * // Array allocation function if (cls.isArray()) {
             * lex.nextToken(PCT_BLOCKEND); return new Const(new
             * AllocatorFunction(cls)); }
             *
             * // Constructor function - check for argument types and find the
             * constructor Class[] args = Function.NO_ARGS; if
             * (lex.nextToken(TT_ANY) == PCT_LPAREN) { args = cTypeList(typ,
             * PCT_RPAREN); } else { lex.pushBack(); }
             * lex.nextToken(PCT_BLOCKEND); Constructor constr =
             * ReflectUtils.findMatchingConstructor(cls, args); if (constr ==
             * null) { throw new HojoException(null,
             * HojoException.ERR_CONSTRUCTOR, new String[] { cls.getName(),
             * typeList2String(args) }, lex.currentLocation()); } return new
             * Const(new ConstructorFunction(constr));
             */
            default:
                throw new HojoException(null,
                        HojoException.ERR_EXPECTED_OPERATOR,
                        null, lex.currentLocation());
            }
        case RES_NEW:
            Class c = cClassName(env, typ, true);
            int dims = arrayDimensions.size();
            if (dims > 0) {
                // array creation
                return new ArrayCreateExpr(c, (Expression[])arrayDimensions
                        .toArray(new Expression[dims]));
            }
            else if (c.isArray()) {
                // array initializer
                lex.nextToken(TT_ARRAYSTART);
                t = HojoLib.typeOf(c.getComponentType());
                return new ArrayInitExpr(HojoLib.typeOf(c),
                        cExprList(env, t, FunctionType.VARIABLE_ARGS, null,
                                TT_ARRAYEND));
            }
            else {
                // constructor
                lex.nextToken(PCT_LPAREN);
                Expression[] xprs = cExprList(env, HojoLib.OBJ_TYPE,
                        FunctionType.VARIABLE_ARGS, ERR_EXPR, PCT_RPAREN);
                Constructor constr = getConstructor(c, typesOf(xprs),
                        optStrictArgTypes);
                Expression cre8 = (constr == null)
                        ? (Expression)new DelayedConstructorExpr(c, xprs)
                        : (Expression)new ConstructorExpr(constr, xprs);
                if (Collection.class.isAssignableFrom(c)) {
                    if (lex.nextToken(TT_ANY) == TT_LISTSTART) {
                        // collection initializer
                        return new CollectionInitExpr(HojoLib.typeOf(c), cre8,
                                cExprList(env, HojoLib.OBJ_TYPE,
                                        FunctionType.VARIABLE_ARGS,
                                        Const.NULL, TT_LISTEND));
                    }
                    else {
                        // no initializer
                        lex.pushBack();
                    }
                }
                else if (Map.class.isAssignableFrom(c)) {
                    if (lex.nextToken(TT_ANY) == TT_MAPSTART) {
                        // map initializer
                        Expression[][] tmp = cMapExpr(env, HojoLib.OBJ_TYPE);
                        return new MapInitExpr(HojoLib.typeOf(c),
                                cre8, tmp[0], tmp[1]);
                    }
                    else {
                        // no initializer
                        lex.pushBack();
                    }
                }
                // no initializer - return the creation expression
                return cre8;
            }
        case RES_LAMBDA:
            // lambda expression - get the argument list
            lex.nextToken(PCT_LPAREN);
            ArrayList typeL = new ArrayList(4);
            ArrayList nameL = new ArrayList(4);
            ArrayList modL = new ArrayList(4);
            ArrayList defaultL = new ArrayList(4);
            boolean variableArgs = cArgumentDecl(typeL, nameL, defaultL, modL);

            String extraName;
            if (variableArgs) {
                int rIdx = nameL.size() - 1;
                extraName = (String)nameL.remove(rIdx);
                typeL.remove(rIdx);
                defaultL.remove(rIdx);
                modL.remove(rIdx);
            }
            else {
                extraName = null;
            }

            // get the function creation operator
            lex.nextToken(TT_OPERATOR);
            if (lex.id != OP_FUNC) {
                throw new HojoException(null,
                        HojoException.ERR_EXPECTED_OPERATOR, null,
                        lex.currentLocation());
            }

            // create a new environment for the body expression
            String[] names = (String[])nameL.toArray(new String[nameL.size()]);
            Class[] types = (Class[])typeL.toArray(new Class[typeL.size()]);
            Object[] defaults = defaultL.toArray();
            short[] modifiers = (short[])HojoLib.toArray(modL, short[].class,
                    short.class, true);
            CompilerEnv lEnv = new CompilerEnv(env, names, types, modifiers,
                    true);
            if (extraName != null) {
                lEnv.alloc(extraName, HojoLib.TUPLE_TYPE, (short)MOD_FINAL);
            }

            // compile the function body in the new environment and return the
            // result
            Expression body = cExpr(lEnv, HojoLib.OBJ_TYPE);
            return new LambdaExpr(names, types, defaults, extraName, body,
                    (short)lEnv.size(), env == null ? 0 : env.getLevel());
        case PCT_ARRAYSTART:
            t = HojoLib.typeOf(typ.arrayElemClass());
            return new ArrayInitExpr(typ.arrayType(),
                    cExprList(env, t, null, TT_ARRAYEND));
        case PCT_LISTSTART:
            t = HojoLib.typeOf(typ.listElemClass());
            return new CollectionInitExpr(
                    HojoLib.typeOf(typ.listType().instanceClass()),
                    null, cExprList(env, t, t.defaultCode(), TT_LISTEND));
        case PCT_MAPSTART:
            Expression[][] tmp = cMapExpr(env, typ.mapElemClass());
            return new MapInitExpr(
                    HojoLib.typeOf(typ.mapType().instanceClass()),
                    null, tmp[0], tmp[1]);
        default:
            if (lex.ttype == stx.quotes[0] || lex.ttype == stx.quotes[2]) {
                return new Const(lex.sval);
            }
            else if (lex.ttype == stx.quotes[1]) {
                return new Const(new Character(lex.sval.charAt(0)));
            }
            else if (lex.ttype == TT_ARRAYSTART) {
                t = HojoLib.typeOf(typ.arrayElemClass());
                return new ArrayInitExpr(typ.arrayType(),
                        cExprList(env, t, null, TT_ARRAYEND));
            }
            else if (lex.ttype == TT_LISTSTART) {
                t = HojoLib.typeOf(typ.listElemClass());
                return new CollectionInitExpr(
                        HojoLib.typeOf(typ.listType().instanceClass()),
                        null, cExprList(env, t, t.defaultCode(), TT_LISTEND));
            }
            else if (lex.ttype == TT_MAPSTART) {
                Expression[][] tmp_ = cMapExpr(env, typ.mapElemClass());
                return new MapInitExpr(
                        HojoLib.typeOf(typ.mapType().instanceClass()),
                        typ.defaultCode(), tmp_[0], tmp_[1]);
            }
            else {
                throw new HojoException(null, HojoException.ERR_SYNTAX,
                        null, lex.currentLocation());
            }
        }
    }

    /* ******************** EXPRESSION HELP METHODS ******************** */

    // create the code for a field access of field f on the expression primary
    private Expression mkFieldExpr(Field f, Expression primary) {
        int mod = f.getModifiers();

        if ((mod & Modifier.PUBLIC) == 0) {
            throw new HojoException(null, HojoException.ERR_ACCESS, null,
                    lex.currentLocation());
        }
        if ((mod & Modifier.FINAL) != 0) {
            if ((mod & Modifier.STATIC) != 0 && f.getType().isPrimitive()) {
                try {
                    return new Const(f.get(null));
                }
                catch (IllegalAccessException e) {
                    throw HojoException.wrap(e);
                }
            }
            else {
                return new FinalFieldExpr(primary, f);
            }
        }
        else {
            return new FieldExpr(primary, f);
        }
    }

    // verify that an expression is of a numerical or compatible type
    private void checkCompoundOp(Expression e, int op) {
        Type t = e.getType();
        boolean ok = false;
        int k;

        if (op == OP_ID || op == OP_BEFORE) {
            return;
        }

        switch (k = t.kind()) {
        case Type.TYP_ARRAY:
            if (t.elementClass() != byte.class) {
                ok = (op == OP_ADD)
                        || (op == OP_MUL && !t.elementClass().isPrimitive());
                break;
            }
            // fall through:
        case Type.TYP_NUMBER:
        case Type.TYP_CHAR:
        case Type.TYP_DATE:
            ok = true;
            break;
        default:
            switch (op) {
            case 0:
                // inc/dec
                break;
            case OP_ADD:
                ok = true;
                break;
            case OP_SUB:
                ok = k == Type.TYP_COLLECTION || k == Type.TYP_LIST
                        || k == Type.TYP_SET;
                break;
            }
        }

        if (!ok) {
            if (optStrictConv) {
                throw new HojoException(null, HojoException.ERR_TYPE,
                        new String[] { t.toString(stx),
                                HojoLib.INT_TYPE.toString(stx) },
                        lex.currentLocation());
            }
            else if (obs != null) {
                obs.handleWarning(
                        new HojoException(null, HojoException.WARN_OP_TYPE,
                                new String[] { t.toString(stx) },
                                lex.currentLocation()));
            }
        }
    }

    // validate that a given expression has type void
    private Statement checkNonvoidExpr(Statement stm) {
        if (obs != null && stm instanceof ExprStm &&
                !((ExprStm)stm).getExpression().isJavaStatement()) {
            ((ExprStm)stm).getExpression();
            obs.handleWarning(new HojoException(null,
                    HojoException.WARN_NONVOID_EXPR, null,
                    lex.currentLocation()));
        }

        return stm;
    }

    // create a type cast, if necessary, and issue warnings etc.
    private Expression forceType(Expression expr, Type typ) {
        if (typ == null) {
            // no type forced
            return expr;
        }

        Expression e = TypecastExpr.mkConversion(typ, expr);
        if (e != expr) {
            // conversion necessary - issue warnings/errors
            if (optStrictConv) {
                throw new HojoException(null, HojoException.ERR_TYPE,
                        new String[] { typ.toString(stx),
                                expr.getType().toString(stx) },
                        lex.currentLocation());
            }
            else if (obs != null) {
                obs.handleWarning(new HojoException(null,
                        HojoException.WARN_ASSIGNMENT,
                        new String[] { typ.toString(stx),
                                expr.getType().toString(stx) },
                        lex.currentLocation()));
            }
        }
        return e;
    }

    // verify that an expression is assignable
    private void checkLValue(Expression l) {
        if (!(l instanceof LValue)) {
            throw new HojoException(null, HojoException.ERR_LVALUE, null,
                    lex.currentLocation());
        }

        int mod = 0;
        if (l instanceof FieldExpr) {
            mod = ((FieldExpr)l).getField().getModifiers();
        }
        else if (l instanceof VarExpr) {
            mod = ((VarExpr)l).getModifiers();
        }
        if ((mod & MOD_FINAL) == MOD_FINAL) {
            throw new HojoException(null, HojoException.ERR_FINAL, null,
                    lex.currentLocation());
        }
    }

    // check that the assignment type is compatible
    private boolean checkAssignment(Type lhs, Type rhs) {
        if (TypecastExpr.needConversion(lhs, rhs)) {
            if (optStrictConv) {
                throw new HojoException(null, HojoException.ERR_TYPE,
                        new String[] { lhs.toString(stx), rhs.toString(stx) },
                        lex.currentLocation());
            }
            else if (obs != null) {
                obs.handleWarning(new HojoException(null,
                        HojoException.WARN_ASSIGNMENT,
                        new String[] { lhs.toString(stx), rhs.toString(stx) },
                        lex.currentLocation()));
            }
            return false;
        }
        return true;
    }

    // create the appropriate code for the binary expression
    private Expression mkBinExpr(Expression e1, long opc, Function op,
            Expression e2) {
        switch ((int)opc) {
        case OP_BEFORE:
            return new BeforeExpr(e1, e2);
        case OP_THEN:
            return new ThenExpr(e1, e2);
        }

        // check for illegal void values
        Type t1 = e1.getType();
        Type t2 = e2.getType();
        if (t1.kind() == Type.TYP_VOID || t2.kind() == Type.TYP_VOID) {
            throw new HojoException(null, HojoException.ERR_VOID,
                    null, lex.currentLocation());
        }

        switch ((int)opc) {
        case OP_COND_AND:
            return new CondAndExpr(e1, e2);
        case OP_COND_OR:
            return new CondOrExpr(e1, e2);
        default:
            if ((opc & OP_PRIO_MASK) == OP_PRIO_ASSIGN) {
                // assignment operator - get the compound operation from the
                // high 32
                // bits of the operator ID
                int cOp = (int)(opc >> 32);
                Expression result;
                Type lhs = e1.getType(), rhs = null;

                switch (cOp) {
                case OP_ID:
                    // vanilla assignment
                    if (!checkAssignment(lhs, rhs = e2.getType())) {
                        e2 = new TypecastExpr(lhs, e2);
                    }
                    result = new AssignOp(e1, e2);
                    break;
                case OP_BEFORE:
                    // exchange assignment
                    if (!checkAssignment(lhs, rhs = e2.getType())) {
                        e2 = new TypecastExpr(lhs, e2);
                    }
                    result = new AssignXchgOp(e1, e2);
                    break;
                default:
                    // compound assignment. Check the resulting type and
                    // convert if necessary
                    Operator f = (Operator)operators.get(new Integer(cOp));
                    rhs = f.inferType(new Type[] { lhs, e2.getType() }, null);
                    if (checkAssignment(lhs, rhs)) {
                        // no explicit cast is necessary
                        lhs = null;
                    }
                    result = new AssignCompoundOp(f, e1, e2, lhs);
                }

                return result;
            }
            else {
                // ordinary binary operation
                return new BinaryOp(op, e1, e2);
            }
        }
    }

    // get the types of a sequence of exprssions
    private static Class[] typesOf(Expression[] exprs) {
        Class[] result = new Class[exprs.length];
        Type t = null;
        for (int i = exprs.length - 1; i >= 0; i--) {
            if (exprs[i] == null) {
                continue;
            }
            t = exprs[i].getType();
            if (t.kind() != Type.TYP_NULL) {
                result[i] = t.toClass();
            }
        }
        return result;
    }

    // find an appropriate constructor
    private Constructor getConstructor(Class base, Class[] ts, boolean strict) {
        Constructor constr;
        boolean isPossible;

        if (base.isPrimitive() ||
                (base.getModifiers()
                        & (Modifier.ABSTRACT | Modifier.INTERFACE)) != 0) {
            // don't look up constructors in primitive, abstract classes
            // or interfaces
            constr = null;
            isPossible = false;
        }
        else {
            constr = ReflectUtils.findMatchingConstructor(base, ts);
            isPossible = true;
        }

        if (constr == null) {
            String[] msg = { base.getName(), typeList2String(ts) };
            if (strict || !isPossible) {
                throw new HojoException(null, HojoException.ERR_CONSTRUCTOR,
                        msg, lex.currentLocation());
            }
            else if (obs != null) {
                obs.handleWarning(new HojoException(null,
                        HojoException.WARN_CONSTRUCTOR, msg,
                        lex.currentLocation()));
            }
        }
        return constr;
    }

    // find an appropriate method
    private Method getMethod(Class base, String name, Class[] argTypes,
            boolean isStatic, boolean strict) {
        // note: wrap the class to allow use of primitive typed values
        Method m = ReflectUtils.findMatchingMethod(ReflectUtils.wrap(base),
                name, argTypes);
        if (m == null) {
            String[] msg = { base.getName(), typeList2String(argTypes), name };
            if (strict) {
                throw new HojoException(null, HojoException.ERR_METHOD, msg,
                        lex.currentLocation());
            }
            else if (obs != null) {
                obs.handleWarning(new HojoException(null,
                        HojoException.WARN_METHOD, msg, lex.currentLocation()));
            }
        }
        else {
            if (isStatic && (m.getModifiers() & Modifier.STATIC) == 0) {
                throw new HojoException(null, HojoException.ERR_STATIC_METHOD,
                        new String[] { name + typeList2String(argTypes),
                                ReflectUtils.className2Java(base)
                        }, lex.currentLocation());
            }
        }
        return m;
    }

    // find an appropriate field
    private Field getField(Class base, String name, boolean isStatic,
            boolean strict) {
        Field f;
        Exception e = null;
        try {
            f = ReflectUtils.wrap(base).getField(name);
        }
        catch (NoSuchFieldException _e) {
            f = null;
            e = _e;
        }

        if (f == null) {
            String[] msg = { base.getName(), name };
            if (strict) {
                throw new HojoException(e, HojoException.ERR_FIELD,
                        msg, lex.currentLocation());
            }
            else if (obs != null) {
                obs.handleWarning(new HojoException(null,
                        HojoException.WARN_FIELD, msg, lex.currentLocation()));
            }
        }
        else {
            if (isStatic && (f.getModifiers() & Modifier.STATIC) == 0) {
                throw new HojoException(null, HojoException.ERR_STATIC_FIELD,
                        new String[] { name,
                                ReflectUtils.className2Java(base) },
                        lex.currentLocation());
            }
        }
        return f;
    }

    // format a list of types (classes)
    private String typeList2String(Class[] types) {
        StringBuffer sb = new StringBuffer(stx.punctuators[PCT_IDX_LPAREN]);
        for (int i = 0; i < types.length; i++) {
            sb.append(types[i] == null ? HojoLib.NULL_TYPE.toString(stx)
                    : types[i].getName());
            if (i < types.length - 1) {
                sb.append(stx.punctuators[PCT_IDX_DELIMITER]).append(' ');
            }
        }
        sb.append(stx.punctuators[PCT_IDX_RPAREN]);
        return sb.toString();
    }

    // compile a type expression
    private Class cClassName(CompilerEnvironment env, Type typ, boolean dim) {
        Class result;
        boolean allowArray;

        switch (lex.nextToken(TT_ANY)) {
        case RES_VAR:
            result = typ.toClass();
            // disallow array dimension indicators
            allowArray = false;
            if (dim) {
                // get the elemental type
                while (result.isArray()) {
                    result = result.getComponentType();
                }
            }
            break;
        case TT_TYPE:
            result = (Class)lex.oval;
            allowArray = true;
            break;
        case TT_WORD:
            allowArray = true;
            result = null;
            StringBuffer sb = new StringBuffer(lex.sval);
            while (true) {
                if (lex.peek() == dotChar1) {
                    if (lex.nextToken(TT_ANY) == TT_OPERATOR
                            && lex.id == OP_DOT) {
                        sb.append('.');
                        lex.nextToken(TT_WORD, true);
                        sb.append(lex.sval);
                        continue;
                    }
                    else {
                        lex.pushBack();
                    }
                }
                break;
            }
            try {
                result = Class.forName(sb.toString());
            }
            catch (ClassNotFoundException e) {
                throw new HojoException(e, HojoException.ERR_CLASSNAME,
                        new String[] { sb.toString() }, lex.currentLocation());
            }
            break;
        default:
            throw new HojoException(null, HojoException.ERR_SYNTAX, null,
                    lex.currentLocation());
        }

        // Parse the array dimension indicators, if allowed
        int dimensions = 0;
        arrayDimensions.clear();

        if (allowArray || dim) {
            classDim: while (lex.nextToken(TT_ANY) == PCT_IDXSTART) {
                lex.nextToken(TT_ANY);
                if (lex.ttype != PCT_IDXEND) {
                    lex.pushBack();
                    if (!dim) {
                        // create a syntax error
                        lex.nextToken(PCT_IDXEND);
                    }
                    else {
                        // Parse the array dimensions for the value
                        // (only if called from parseConstructor())
                        do {
                            arrayDimensions.add(cExpr(env, HojoLib.INT_TYPE));
                            lex.nextToken(PCT_IDXEND);
                        } while (lex.nextToken(TT_ANY) == PCT_IDXSTART);
                        break classDim;
                    }
                }
                else if (!allowArray) {
                    throw new HojoException(null,
                            HojoException.ERR_EXPECTED_IDENTIFIER,
                            null, lex.currentLocation());
                }
                dimensions++;
            }
            lex.pushBack();
        }
        return ReflectUtils.getArrayClass(result, dimensions);
    }

    // compile a list of expressions separated by PCT_DELIMITER terminated by
    // endToken
    // empty defines the default value to use if an empty place is encountered;
    // empty == ERR_EXPR disallows empty places.
    private Expression[] cExprList(CompilerEnvironment env, Type typ,
            Class[] types, Expression empty, int endToken) {
        ArrayList result = new ArrayList();
        Type t;
        boolean lastExpr = false; // true iff the last element was an expression
        boolean lastDelimiterUsed = false; // true iff the !lastExpr and the
                                           // delimiter was treated as a blank

        for (int i = 0;;) {
            if (lex.nextToken(TT_ANY) == PCT_DELIMITER) {
                if (!lastExpr) {
                    if (empty == ERR_EXPR) {
                        throw new HojoException(null,
                                HojoException.ERR_EXPECTED_IDENTIFIER,
                                null, lex.currentLocation());
                    }
                    result.add(empty);
                    i++;
                    lastDelimiterUsed = true;
                }
                else {
                    lastDelimiterUsed = false;
                }
                lastExpr = false;
                continue;
            }
            else if (lex.ttype == endToken) {
                if (!lastExpr && !lastDelimiterUsed && result.size() > 0) { // needDelimiter
                                                                            // &&
                                                                            // result.size()
                                                                            // >
                                                                            // 0)
                                                                            // {
                    if (empty == ERR_EXPR) {
                        throw new HojoException(null,
                                HojoException.ERR_EXPECTED_IDENTIFIER,
                                null, lex.currentLocation());
                    }
                    result.add(empty);
                    i++;
                }
                int missing = (types != FunctionType.VARIABLE_ARGS)
                        ? types.length - i
                        : 0;
                if (missing > 0) {
                    if (empty == ERR_EXPR) {
                        throw new HojoException(null,
                                HojoException.ERR_EXPECTED_IDENTIFIER,
                                null, lex.currentLocation());
                    }
                    for (; missing > 0; missing--) {
                        result.add(empty);
                    }
                }
                return (Expression[])result
                        .toArray(new Expression[result.size()]);
            }
            else if (lastExpr) {
                throw new HojoException(null, HojoException.ERR_EXPECTED_TOKEN,
                        new String[] { stx.punctuators[PCT_IDX_DELIMITER] },
                        lex.currentLocation());
            }

            lex.pushBack();
            t = (types == FunctionType.VARIABLE_ARGS || i >= types.length) ? typ
                    : HojoLib.typeOf(types[i]);
            Expression e = TypecastExpr.mkConversion(t, cExpr(env, t));

            if (e.getType().kind() == Type.TYP_VOID) {
                throw new HojoException(null, HojoException.ERR_VOID, null,
                        lex.currentLocation());
            }

            result.add(e);
            i++;
            lastExpr = true;
            lastDelimiterUsed = false;
        }
    }

    private final Expression[] cExprList(CompilerEnvironment env, Type typ,
            Expression empty, int endToken) {
        return cExprList(env, typ, FunctionType.VARIABLE_ARGS, empty, endToken);
    }

    // compile a type list
    private Class[] cTypeList(Type typ, int endToken) {
        ArrayList l = new ArrayList();

        do {
            if (lex.nextToken(TT_ANY) == endToken) {
                break;
            }
            lex.pushBack();
            l.add(cClassName(null, typ, false));
            if (lex.nextToken(TT_ANY) != PCT_DELIMITER) {
                lex.pushBack();
            }
        } while (true);

        return (Class[])l.toArray(new Class[l.size()]);
    }

    // compile an argument declaration list, return true iff a variable number
    // of arguments is permitted (this implies that the last argument is
    // declared 'final Object[] <name>').
    private boolean cArgumentDecl(List types, List names, List defaults,
            List modifiers) {
        int idx;
        Short _MOD_FINAL = new Short((short)MOD_FINAL);
        boolean isVariable = false, isVar = false;

        while (lex.nextToken(TT_ANY) != PCT_RPAREN) {
            if ("*".equals(lex.sval)) {
                // excess parameter
                isVariable = true;
                modifiers.add(_MOD_FINAL);
                types.add(Object[].class);
                lex.nextToken(TT_WORD);
            }
            else {
                if (lex.ttype == RES_FINAL) {
                    // final modifier
                    modifiers.add(_MOD_FINAL);
                    lex.nextToken(TT_ANY);
                }
                else {
                    modifiers.add(ConvertUtils.ZERO_SHORT);
                }
                if (lex.ttype == TT_WORD
                        && !packagePrefixes.contains(lex.sval)) {
                    // no type was given - declare as 'Object'
                    isVar = false;
                    types.add(Object.class);
                }
                else {
                    // get the declaration type and read the following name
                    isVar = lex.ttype == RES_VAR;
                    lex.pushBack();
                    Class c = cClassName(null, HojoLib.OBJ_TYPE, false);
                    if (c != Object.class || !isVar) {
                        // add the type only if 'var' was not used
                        isVar = false;
                        types.add(c);
                    }
                    lex.nextToken(TT_WORD);
                }
            }
            if ((idx = names.indexOf(lex.sval)) >= 0) {
                throw new HojoException(null, HojoException.ERR_DUPLICATE_ID,
                        new String[] { (String)names.get(idx),
                                HojoLib.typeOf((Class)types.get(idx))
                                        .toString(stx)
                        },
                        lex.currentLocation());
            }

            // add the parameter name
            names.add(lex.sval);

            if (lex.nextToken(TT_ANY) == TT_OPERATOR && lex.id == OP_ASSIGN
                    && !isVariable) {
                // default value
                if (isVar) {
                    // compile the expression in the default type context, and
                    // add the
                    // obtained type to the types
                    Expression e = cExpr(NoEnv.getInstance(), HojoLib.OBJ_TYPE);
                    types.add(e.getTypeC());
                    defaults.add(e.xeq(null));
                    isVar = false;
                }
                else {
                    // compile the expression in the given type context, and
                    // cast the
                    // result to the type
                    Type t = HojoLib
                            .typeOf((Class)(types.get(types.size() - 1)));
                    defaults.add(t
                            .typeCast(cExpr(NoEnv.getInstance(), t).xeq(null)));
                }
                lex.nextToken(TT_ANY);
            }
            else { // no default value
                if (isVar) {
                    // add the general Object type to the list
                    types.add(Object.class);
                    isVar = false;
                }
                defaults.add(Function.NO_ARG);
            }

            if (lex.ttype == PCT_RPAREN) {
                break;
            }
            else if (isVariable || lex.ttype != PCT_DELIMITER) {
                throw new HojoException(null, HojoException.ERR_EXPECTED_TOKEN,
                        new String[] {
                                stx.punctuators[isVariable ? PCT_IDX_RPAREN
                                        : PCT_IDX_DELIMITER]
                        }, lex.currentLocation());
            }
        } // while
        return isVariable;
    }

    // compile a map expression using the given type context
    private Expression[][] cMapExpr(CompilerEnvironment env, Type typ) {
        ArrayList keys = new ArrayList();
        ArrayList exprs = new ArrayList();

        lex.nextToken();
        while (lex.ttype != TT_MAPEND) {
            lex.pushBack();
            lex.nextToken(TT_ANY);
            if (lex.ttype == TT_WORD || lex.ttype == stx.quotes[0]) {
                keys.add(new Const(lex.sval));
            }
            else if (lex.ttype == PCT_LPAREN) {
                keys.add(cExpr(env, HojoLib.OBJ_TYPE));
                lex.nextToken(PCT_RPAREN);
            }
            else {
                // generate an error message
                lex.pushBack();
                lex.nextToken(TT_WORD);
            }

            lex.nextToken(TT_OPERATOR);
            if (lex.id != OP_ASSIGN) {
                throw new HojoException(null, HojoException.ERR_EXPECTED_TOKEN,
                        new String[] { stx.operators[OP_IDX_ASSIGN] },
                        lex.currentLocation());
            }

            exprs.add(cExpr(env, typ));
            if (lex.nextToken(TT_ANY) == PCT_DELIMITER) {
                lex.nextToken();
            }
        }

        int sz = keys.size();
        return new Expression[][] {
                (Expression[])keys.toArray(new Expression[sz]),
                (Expression[])exprs.toArray(new Expression[sz])
        };
    }

    private final Expression[][] cMapExpr(CompilerEnvironment env, Class typ) {
        return cMapExpr(env, HojoLib.typeOf(typ));
    }

}
