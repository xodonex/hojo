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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.xodonex.hojo.lang.Code;
import org.xodonex.util.StringUtils;
import org.xodonex.util.text.lexer.Location;

/**
 * Base class for unchecked exceptions thrown from the {@link HojoCompiler}.
 */
public class HojoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final int ECODE_INTERNAL = -1,
            ECODE_IO = 0,
            ECODE_LEXICAL = 1,
            ECODE_META = 2,
            ECODE_SYNTAX = 3,
            ECODE_SEMANTICAL = 4,
            ECODE_RUNTIME = 5,
            ECODE_WARNING = 6,

            ECODE_CODEMASK = 0xffff,
            ECODE_ARGMASK = 0xffff0000,
            ECODE_ARGSHIFT = 16,
            ECODE_TYPEFCT = 1000,

            ECODE_NULLARY = 0,
            ECODE_UNARY = 1 << ECODE_ARGSHIFT,
            ECODE_BINARY = 2 << ECODE_ARGSHIFT,
            ECODE_TERNARY = 3 << ECODE_ARGSHIFT,

            _ERR_IO = ECODE_IO * ECODE_TYPEFCT,
            _ERR_LEX = ECODE_LEXICAL * ECODE_TYPEFCT,
            _ERR_META = ECODE_META * ECODE_TYPEFCT,
            _ERR_SYNTAX = ECODE_SYNTAX * ECODE_TYPEFCT,
            _ERR_SEM = ECODE_SEMANTICAL * ECODE_TYPEFCT,
            _ERR_RUNTIME = ECODE_RUNTIME * ECODE_TYPEFCT,
            _ERR_WARNING = ECODE_WARNING * ECODE_TYPEFCT;

    // no-argument indicator for MessageFormat.format()
    private static final String[] NO_ARGS = {};

    /**
     * Error code indicating an internal error in the Hojo implementation.
     */
    public static final int ERR_INTERNAL = -1; // ECODE_BINARY

    /* ****************************** _ERR_IO ****************************** */

    /**
     * Error code indicating an unexpected end-of-file.
     */
    public static final int ERR_EOF = (_ERR_IO + 1) | ECODE_NULLARY;

    /**
     * Indicates that an URL could not be accessed.
     */
    public static final int ERR_URL = (_ERR_IO + 2) | ECODE_UNARY;

    /**
     * Error code indicating a general I/O exception was encountered while
     * reading parser input.
     */
    public static final int ERR_READ = (_ERR_IO + 3) | ECODE_NULLARY;

    /* ****************************** _ERR_LEX ****************************** */

    /**
     * Error code indicating an invalid unicode-escape sequence.
     */
    public static final int ERR_UNICODE = (_ERR_LEX + 1) | ECODE_NULLARY;

    /**
     * Error code indicating an invalid escape character.
     */
    public static final int ERR_ESCAPE = (_ERR_LEX + 2) | ECODE_NULLARY;

    /**
     * Error code indicating an invalid multiple-line qomment.
     */
    public static final int ERR_COMMENT = (_ERR_LEX + 3) | ECODE_NULLARY;

    /**
     * Error code indicating an unfinished quoted string.
     */
    public static final int ERR_QUOTE = (_ERR_LEX + 4) | ECODE_NULLARY;

    /**
     * Error code indicating an invalid character literal.
     */
    public static final int ERR_CHARQUOTE = (_ERR_LEX + 5) | ECODE_NULLARY;

    /**
     * Error code indicating an illegal number was encountered.
     */
    public static final int ERR_NUMFORMAT = (_ERR_LEX + 6) | ECODE_NULLARY;

    /*
     * ****************************** _ERR_META ******************************
     */

    /**
     * Error code indicating a circular chain of included files.
     */
    public static final int ERR_CIRCULAR_INCLUDE = (_ERR_META + 1)
            | ECODE_UNARY;

    /**
     * Indicates that an unknown meta symbol is used
     */
    public static final int ERR_UNKNOWN_META_SYMBOL = (_ERR_META + 2)
            | ECODE_UNARY;

    /**
     * Indicates that a macro is circularly defined.
     */
    public static final int ERR_CIRCULAR_MACRO = (_ERR_META + 3) | ECODE_UNARY;

    /**
     * Indicates that an #elif / #else / #endif directive was used in a bad
     * context.
     */
    public static final int ERR_METASYNTAX = (_ERR_META + 4) | ECODE_UNARY;

    /**
     * Indicates that the redefinition of a symbol was attempted.
     */
    public static final int ERR_REDEFINED_SYMBOL = (_ERR_META + 5)
            | ECODE_UNARY;

    /**
     * Indicates that a fixed symbol was attempted removed.
     */
    public static final int ERR_REMOVED_SYMBOL = (_ERR_META + 6) | ECODE_UNARY;

    /* ****************************** _ERR_STX ****************************** */

    /**
     * Indicates that an end-of-line was expected but not received.
     */
    public static final int ERR_EXPECTED_EOL = (_ERR_SYNTAX + 1)
            | ECODE_NULLARY;

    /**
     * Indicates that an identifier was expected but not received.
     */
    public static final int ERR_EXPECTED_IDENTIFIER = (_ERR_SYNTAX + 2)
            | ECODE_NULLARY;

    /**
     * Indicates that an operator was expected but not received.
     */
    public static final int ERR_EXPECTED_OPERATOR = (_ERR_SYNTAX + 3)
            | ECODE_NULLARY;

    /**
     * Indicates that a specific token was expected but not received.
     */
    public static final int ERR_EXPECTED_TOKEN = (_ERR_SYNTAX + 4)
            | ECODE_UNARY;

    /**
     * Indicates that an unknown class name was used.
     */
    public static final int ERR_CLASSNAME = (_ERR_SYNTAX + 5) | ECODE_UNARY;

    /**
     * Indicates that an unknown field name was used.
     */
    public static final int ERR_FIELD = (_ERR_SYNTAX + 6) | ECODE_BINARY;

    /**
     * Indicates that no matching constructor could be found.
     */
    public static final int ERR_CONSTRUCTOR = (_ERR_SYNTAX + 7) | ECODE_BINARY;

    /**
     * Indicates that no matching method could be found.
     */
    public static final int ERR_METHOD = (_ERR_SYNTAX + 8) | ECODE_TERNARY;

    /**
     * Indicates that an instance field was accesssed as a static field.
     */
    public static final int ERR_STATIC_FIELD = (_ERR_SYNTAX + 9) | ECODE_BINARY;

    /**
     * Indicates that an instance method was accesssed as a static method.
     */
    public static final int ERR_STATIC_METHOD = (_ERR_SYNTAX + 10)
            | ECODE_BINARY;

    /**
     * Indicates that a variable expression was expected.
     */
    public static final int ERR_LVALUE = (_ERR_SYNTAX + 11) | ECODE_NULLARY;

    /**
     * Indicates that an illegal index expression was encountered.
     */
    public static final int ERR_INDEX = (_ERR_SYNTAX + 12) | ECODE_UNARY;

    /**
     * Indicates that an invalid extended type cast expression was encountered.
     */
    public static final int ERR_TYPECAST = (_ERR_SYNTAX + 13) | ECODE_NULLARY;

    /**
     * Indicates that an invalid function application was encountered.
     */
    public static final int ERR_FUNCTION = (_ERR_SYNTAX + 14) | ECODE_UNARY;

    /**
     * Indicates that an invalid use of the function creation operator was
     * encountered.
     */
    public static final int ERR_FUNC_OP = (_ERR_SYNTAX + 15);

    /**
     * Indicates that an invalid operator was attempted used as a function.
     */
    public static final int ERR_OP_SYNTAX = (_ERR_SYNTAX + 16) | ECODE_NULLARY;

    /**
     * Indicates that an unknown identifier was encountered.
     */
    public static final int ERR_UNKNOWN_ID = (_ERR_SYNTAX + 17) | ECODE_UNARY;

    /**
     * Indicates that a duplicate modifier was used.
     */
    public static final int ERR_DUPLICATE_MODIFIER = (_ERR_SYNTAX + 18)
            | ECODE_UNARY;

    /**
     * Indicates that an invalid type was used in a catch clause.
     */
    public static final int ERR_CATCH_TYPE = (_ERR_SYNTAX + 19) | ECODE_UNARY;

    /**
     * Indicates that an invalid number of arguments were supplied to a
     * function.
     */
    public static final int ERR_ARG_COUNT = (_ERR_SYNTAX + 20) | ECODE_BINARY;

    /**
     * Indicates that a required parameter was unspecified in a function call
     */
    public static final int ERR_ARG_MISSING = (_ERR_SYNTAX + 21) | ECODE_BINARY;

    /**
     * Indicates that a general syntax error was encountered.
     */
    public static final int ERR_SYNTAX = (_ERR_SYNTAX + 22) | ECODE_NULLARY;

    /* ****************************** _ERR_SEM ****************************** */

    /**
     * Indicates that a statement is not reachable
     */
    public static final int ERR_UNREACHABLE = (_ERR_SEM + 1) | ECODE_UNARY;

    /**
     * Indicates that a return type is missing
     */
    public static final int ERR_MISSING_RETURN_TYPE = (_ERR_SEM + 2)
            | ECODE_UNARY;

    /**
     * Indicates that an illegal return type was used.
     */
    public static final int ERR_RETURN_TYPE = (_ERR_SEM + 3) | ECODE_UNARY;

    /**
     * Indicates that a modifier was used in an illegal way
     */
    public static final int ERR_ILLEGAL_MODIFIER = (_ERR_SEM + 4) | ECODE_UNARY;

    /**
     * Indicates that a statement was used in an invalid context.
     */
    public static final int ERR_ILLEGAL_STATEMENT = (_ERR_SEM + 5)
            | ECODE_NULLARY;

    /**
     * Indicates that a control transfer statement was used in an invalid
     * context.
     */
    public static final int ERR_ILLEGAL_DECLARATION = (_ERR_SEM + 6)
            | ECODE_NULLARY;

    /**
     * Indicates that a duplicate identifier was used.
     */
    public static final int ERR_DUPLICATE_ID = (_ERR_SEM + 7) | ECODE_BINARY;

    /**
     * Indicates that a function was redeclared using a different modifier
     * signature.
     */
    public static final int ERR_REDEF_MODIFIER = (_ERR_SEM + 8) | ECODE_BINARY;

    /**
     * Indicates that a function declared final was attempted redeclared.
     */
    public static final int ERR_REDEF_FINAL = (_ERR_SEM + 9) | ECODE_BINARY;

    /**
     * Indicates that a class member could not be accessed
     */
    public static final int ERR_ACCESS = (_ERR_SEM + 10) | ECODE_NULLARY;

    /**
     * Indicates that a final value was attempted overwritten
     */
    public static final int ERR_FINAL = (_ERR_SEM + 11) | ECODE_NULLARY;

    /**
     * Indicates that an incompatible type was used.
     */
    public static final int ERR_TYPE = (_ERR_SEM + 12) | ECODE_BINARY;

    /**
     * Indicates that a value of type void was attempted referenced.
     */
    public static final int ERR_VOID = (_ERR_SEM + 13) | ECODE_NULLARY;

    /**
     * Indicates that an illegal value was given.
     */
    public static final int ERR_VALUE = (_ERR_SEM + 14) | ECODE_UNARY;

    /**
     * Indicates that a precondition of some operation was violated.
     */
    public static final int ERR_STATE = (_ERR_SEM + 15) | ECODE_UNARY;

    /*
     * ****************************** _ERR_RUNTIME
     * ******************************
     */

    /**
     * Indicates that a general runtime error occurred.
     */
    public static final int ERR_RUNTIME = (_ERR_RUNTIME + 0) | ECODE_UNARY;

    /**
     * Indicates that a general runtime error occurred during execution of Hojo
     * code.
     */
    public static final int ERR_RUNTIME_HOJO = (_ERR_RUNTIME + 1)
            | ECODE_BINARY;

    /**
     * Indicates that an invalid number of arguments were supplied to a
     * function.
     */
    public static final int ERR_RUNTIME_ARG_COUNT = (_ERR_RUNTIME + 2)
            | ECODE_BINARY;

    /**
     * Indicates that a required parameter was unspecified in a function call
     */
    public static final int ERR_RUNTIME_ARG_MISSING = (_ERR_RUNTIME + 3)
            | ECODE_BINARY;

    /*
     * ****************************** _ERR_WARNING
     * ******************************
     */

    /**
     * Indicates that a user-generated warning was issued.
     */
    public static final int WARN_USER = (_ERR_WARNING + 0) | ECODE_UNARY;

    /**
     * Warning indicating that a specific field could not be found.
     */
    public static final int WARN_FIELD = (_ERR_WARNING + 1) | ECODE_BINARY;

    /**
     * Warning indicating that a specific constructor could not be found.
     */
    public static final int WARN_CONSTRUCTOR = (_ERR_WARNING + 2)
            | ECODE_BINARY;

    /**
     * Warning indicating that a specific method could not be found.
     */
    public static final int WARN_METHOD = (_ERR_WARNING + 3) | ECODE_TERNARY;

    /**
     * Warning indicating that an index operation may not succeed.
     */
    public static final int WARN_INDEX = (_ERR_WARNING + 4) | ECODE_UNARY;

    /**
     * Warning indicating that the value of an expression is being discarded.
     */
    public static final int WARN_NONVOID_EXPR = (_ERR_WARNING + 5)
            | ECODE_NULLARY;

    /**
     * Warning indicating that a symbol was not removed.
     */
    public static final int WARN_NOTREMOVED = (_ERR_WARNING + 6) | ECODE_UNARY;

    /**
     * Warning indicating that pragma directive was ignored
     */
    public static final int WARN_IGNORED = (_ERR_WARNING + 7) | ECODE_UNARY;

    /**
     * Warning indicating that a function is returning a value that cannot be
     * contained within its return type.
     */
    public static final int WARN_RETURN_TYPE = (_ERR_WARNING + 8)
            | ECODE_BINARY;

    /**
     * Warning indicating that an operation may not succeed.
     */
    public static final int WARN_OP_TYPE = (_ERR_WARNING + 9) | ECODE_UNARY;

    /**
     * Warning indicating that an assignment may be incompatible.
     */
    public static final int WARN_ASSIGNMENT = (_ERR_WARNING + 10)
            | ECODE_BINARY;

    private static ResourceBundle messages = ResourceBundle.getBundle(
            "org/xodonex/hojo/resource/HojoException",
            Locale.getDefault());

    /**
     * The error code for this exception
     */
    protected int code;

    /**
     * The error location
     */
    protected Location loc;

    /**
     * The <code>Throwable</code> that caused this error
     */
    protected Throwable error;

    /**
     * Constructs a new internal <code>HojoException</code>.
     */
    public HojoException() {
        this(ERR_INTERNAL, null, null, null);
    }

    /**
     * Constructs a new runtime <code>HojoException</code> from the given
     * exception.
     *
     * @param t
     *            the base cause/exception for this exception.
     */
    public HojoException(Throwable t) {
        this(null, getException(t));
    }

    private HojoException(Object dummy, Throwable t) {
        this(ERR_RUNTIME, t,
                new String[] { (t == null) ? ""
                        : ((t.getMessage() == null)
                                ? " : " + t.getClass().getName()
                                : " : " + t.getMessage()) },
                null);
    }

    public HojoException(Throwable t, int code, String[] args, Location loc) {
        this(code, getException(t), args, loc);
    }

    private HojoException(int code, Throwable t, String[] args, Location loc) {
        super(constructMessage(t, code, args));
        error = t;
        this.code = code;
        this.loc = loc;
    }

    private static Throwable getException(Throwable t) {
        if (t instanceof InvocationTargetException) {
            return ((InvocationTargetException)t).getTargetException();
        }
        else {
            return t;
        }
    }

    public static HojoException wrap(Throwable t) {
        Throwable t_ = getException(t);
        if (t_ instanceof HojoException) {
            return (HojoException)t_;
        }
        else {
            return new HojoException(t_);
        }
    }

    public static HojoException wrap(Throwable t, Code c) {
        Throwable t_ = getException(t);
        if (t_ instanceof HojoException) {
            return (HojoException)t_;
        }
        else {
            String message = t_.getMessage();
            if (message == null) {
                message = t_.getClass().getName();
            }
            return new HojoException(t_, ERR_RUNTIME_HOJO,
                    new String[] { message, c.toString(HojoSyntax.DEFAULT,
                            StringUtils.defaultFormat, "") },
                    null);
        }
    }

    private static int getType(int code) {
        return (code < 0) ? -1 : ((code & ECODE_CODEMASK) / ECODE_TYPEFCT);
    }

    private static int getArity(int code) {
        return (code & ECODE_ARGMASK) >> ECODE_ARGSHIFT;
    }

    public static int getCode(int code) {
        return code & ECODE_CODEMASK;
    }

    private static String constructInternalMessage(Throwable t,
            String contents) {
        try {
            return MessageFormat.format(messages.getString("internal"),
                    new Object[] { contents, StringUtils.createTrace(t) });
        }
        catch (RuntimeException e) {
            return "Internal error (bad resource file).\n"
                    + StringUtils.createTrace(e);
        }
    }

    private static String getInternalMessage(String[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        return args[0];
    }

    private static String constructMessage(Throwable t, int code,
            String[] args) {
        if (t instanceof HojoException) {
            return t.getMessage();
        }

        int type = getType(code);
        int arity = getArity(code);
        code = getCode(code);

        if (args == null) {
            args = NO_ARGS;
        }
        if (type == ECODE_INTERNAL) {
            return constructInternalMessage(t, getInternalMessage(args));
        }

        String msg = "";
        String sCode = StringUtils.expandLeft("" + code, '0', 4);

        try {
            msg = messages.getString(sCode);
        }
        catch (MissingResourceException e) {
            msg = "";
        }
        StringBuffer result = new StringBuffer();

        result.append("H");
        result.append(sCode);
        result.append(": ");

        if (arity > args.length || msg == null) {
            return constructInternalMessage(new Exception(),
                    "Bad error message format");
        }
        if (arity > 0) {
            try {
                msg = MessageFormat.format(msg, (Object[])args);
            }
            catch (RuntimeException e) {
                return constructInternalMessage(e,
                        "Bad resource file entry \"" + sCode + "\"");
            }
        }

        result.append(msg);
        return result.toString();
    }

    public boolean isInternal() {
        return getType(code) == ECODE_INTERNAL;
    }

    public boolean isWarning() {
        return getType(code) == ECODE_WARNING;
    }

    public boolean isRuntimeError() {
        return getType(code) == ECODE_RUNTIME;
    }

    public int getFullCode() {
        return code;
    }

    public int getCode() {
        return getCode(code);
    }

    public Location getLocation() {
        return loc;
    }

    public void setLocation(Location loc) {
        this.loc = loc;
    }

    public Throwable getError() {
        return error;
    }

    @Override
    public String toString() {
        if (loc != null) {
            return getMessage() + "\n" + getLocation();
        }
        else {
            return "" + getMessage();
        }
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream err) {
        if (error != null) {
            err.println(toString());
            error.printStackTrace(err);
            if (error instanceof InvocationTargetException) {
                ((InvocationTargetException)error).getTargetException()
                        .printStackTrace(err);
            }
        }
        else {
            super.printStackTrace(err);
        }
    }

    @Override
    public void printStackTrace(PrintWriter err) {
        if (error != null) {
            err.println(toString());
            error.printStackTrace(err);
        }
        else {
            super.printStackTrace(err);
        }
    }
}
