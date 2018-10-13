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
import java.io.Writer;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xodonex.hojo.lang.Const;
import org.xodonex.hojo.lang.Function;
import org.xodonex.hojo.lib.StdLib;
import org.xodonex.hojo.util.ClassLoaderAction;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;
import org.xodonex.util.os.OsInterface;
import org.xodonex.util.struct.PrimitiveArrayList;
import org.xodonex.util.text.lexer.GenericLexer;

/**
 * This class holds the lexical syntax configuration for a Hojo
 * compiler/interpreter.
 */
public class HojoSyntax implements Cloneable, java.io.Serializable, HojoConst {

    private static final long serialVersionUID = 1L;

    public final static HojoSyntax DEFAULT = new HojoSyntax();

    public HojoSyntax() {
    }

    /* ***************** Tokenizer setup ***************** */

    /**
     * The characters that are treated as whitespace (blank) characters by the
     * tokenizer. The characters at even indices are lower bounds, odd indices
     * are upper bounds, ie. <code>'a'-'z', 'A'-'Z'</code> would be
     * <code>{'a', 'z', 'A', 'Z'}</code>.<BR>
     * <strong>N.B:</strong>: The character '\0' should not be a whitespace
     * character.
     *
     * @see java.io.StreamTokenizer#whitespaceChars(int, int)
     */
    public char[] chrWhitespace = { '\1', ' ' };

    /**
     * The characters that are treated as word characters by the tokenizer (a
     * sequence of word characters will be treated as a single token, whereas
     * characters that are neither word nor whitespace characters will yield
     * separate tokens (separators)) The characters at even indices are lower
     * bounds, odd indices are upper bounds, ie. <code>'a'-'z', 'A'-'Z'</code>
     * would be <code>{'a', 'z', 'A', 'Z'}</code>.
     *
     * @see java.io.StreamTokenizer#wordChars(int, int)
     */
    public char[] chrWord = { 'a', 'z', 'A', 'Z', '0', '9', '_', '_', '$',
            '$' };

    /**
     * The strings that mark a single-line comment.
     */
    public String[] singleLineComments = { "//" };

    /**
     * The strings that mark a multiple-line comment.
     */
    public String[][] multipleLineComments = { { "/*", "*/" } };

    /**
     * Whether multiple-line comments are nested.
     */
    public boolean nestedComments = false;

    /**
     * Whether the language is case sensitive (all word characters will be
     * converted to lowercase if not)
     */
    public boolean optCaseSensitive = true;

    /**
     * Whether unicode-escape sequences should be decoded
     */
    public boolean optUnicodeEscapes = true;

    /**
     * Whether the first character of an identifier should be a
     * {@link java.lang.Character#isJavaIdentifierStart(char) Java-identifier}
     * character.
     */
    public boolean optJavaIdentifiers = true;

    /**
     * This array holds the special punctuators. The contents are the following:
     * <ol>
     * <li value="0">The left parenthesis (S)
     * <li value="1">The right parenthesis (S)
     * <li value="2">The table start parenthesis (S)
     * <li value="3">The table end parenthesis (S)
     * <li value="4">The list start parenthesis (S)
     * <li value="5">The list end parenthesis (S)
     * <li value="6">The array start parenthesis (S)
     * <li value="7">The array end parenthesis (S)
     * <li value="8">The statement separator (S). As it is used to resynchronize
     * the input upon errors, it should not be used otherwise.
     * <li value="9">The expression separator (S).
     * <li value="10">The attribute/method access operator, and package name
     * separator (S).
     * </ol>
     * The table and array start/end parentheses are allowed to be equal.
     */
    public String[] punctuators = {
            ";", ",", "(", ")", "{", "}", "[", "]", "[.", ".]", "{", "}", "[",
            "]", ":"
    };

    /**
     * This array holds the different quote characters (<code>'\0'</code>
     * disables):
     * <ol>
     * <li value="0">String literal quote
     * <li value="1">Character literal quote
     * <li value="3">Raw string quote
     * </ol>
     */
    public char[] quotes = { '"', '\'', '`' };

    /**
     * This array holds the number-literal specific characters (except from the
     * {@link #suffixes}). The array contains the following characters:
     * <ol>
     * <li value="0">The positive-exponent sign
     * <li value="1">The negative-exponent sign
     * <li value="2">The decimal point
     * <li value="3">The exponent character
     * <li value="4">The hexadecimal prefix character
     * </ol>
     */
    public char[] numberChars = { '+', '-', '.', 'e', 'x' };

    /**
     * This array contains the number type suffixes. The contents are the
     * following:
     * <ol>
     * <li value="0">java.lang.Byte suffix (W)
     * <li value="1">java.lang.Short suffix (W)
     * <li value="2">java.lang.Integer suffix (W)
     * <li value="3">java.lang.Long suffix (W)
     * <li value="4">java.lang.Float suffix (W)
     * <li value="5">java.lang.Double suffix (W)
     * <li value="6">java.math.BigInteger suffix (W)
     * <li value="7">java.math.BigDecimal suffix (W)
     * </ol>
     * <code>'\0'</code> can be used to disable a suffix.
     */
    public char[] suffixes = { 'b', 's', '\0', 'l', 'f', 'd', 'i', 'r' };

    /**
     * This table holds the special reserved words (keywords) of the primary
     * language itself. Apart from these, the {@link #metaSyntax},
     * {@link #standardLiterals}, {@link #customLiteralNames} and {@link #types}
     * are also reserved, as in, generally unavailable for identifiers).
     */
    public String[] reserved = {
            "this", "super",
            "public", "final", "synchronized",
            "var", "let", "new", "op", "fn", "class",
            "return", "break", "continue", "throw", "import",
            "if", "else", "while", "do", "for",
            "switch", "case", "default", "try", "catch", "finally"
    };

    /**
     * This array contains the keywords used for the built-in types in Hojo; the
     * corresponding Java classes are as follows:
     * <ol>
     * <li value="0">java.lang.Object
     * <li value="1">java.lang.Boolean.TYPE
     * <li value="2">java.lang.Boolean
     * <li value="3">java.lang.Byte.TYPE
     * <li value="4">java.lang.Byte
     * <li value="5">java.lang.Short.TYPE
     * <li value="6">java.lang.Short
     * <li value="7">java.lang.Character.TYPE
     * <li value="8">java.lang.Character
     * <li value="9">java.lang.Integer.TYPE
     * <li value="10">java.lang.Integer
     * <li value="11">java.lang.Long.TYPE
     * <li value="12">java.lang.Long
     * <li value="13">java.lang.Float.TYPE
     * <li value="14">java.lang.Float
     * <li value="15">java.lang.Double.TYPE
     * <li value="16">java.lang.Double
     * <li value="17">java.math.BigInteger
     * <li value="18">java.math.BigDecimal
     * <li value="19">java.util.Date
     * <li value="20">java.lang.String
     * <li value="21">java.net.URL
     * <li value="22">java.lang.Class
     * <li value="23">java.util.Map (any Map)
     * <li value="24">java.util.List (any List)
     * <li value="25">java.lang.Object[] (any array)
     * </ol>
     * All values must be identifiers, or <code>null</code>, if the type should
     * not be used.
     */
    public String[] types = {
            "Object", "Function", "void",
            "Map", "Array", "List", "Set", "Collection", "Iterator",
            "boolean", "Boolean", "char", "Character",
            "CharSequence", "String", "StringBuffer",
            "Pattern", "Class", "Date", "File", "URL",
            "Number", "byte", "Byte", "short", "Short",
            "int", "Integer", "long", "Long",
            "float", "Float", "double", "Double",
            "BigInteger", "BigDecimal"
    };

    /**
     * Names of classes which should be default-imported.
     */
    public String[] imports = {
            "java.lang.Cloneable", "java.lang.Comparable", "java.lang.Runnable",
            "java.lang.System", "java.lang.Runtime", "java.lang.Thread",
            "java.lang.ThreadGroup", "java.lang.Math", "java.lang.Throwable",
            "java.lang.Exception",
            "java.io.Serializable",
    };

    /**
     * The package prefixes, ie. the identifiers which signify the start of a
     * fully classified class name
     */
    public String[] packagePrefixes = { "java", "javax", "sun",
            "com", "net", "org", "biz" };

    /**
     * This array contains the operator definitions. The following restrictions
     * apply:
     * <ul>
     * <li>An operator consists of one or two separator characters.
     * <li>All operators must be distinct, except for binary and unary minus
     * </ul>
     * The contains the following operators (operator priorities are shown in
     * parentheses):
     * <ol>
     * <li value="0">Assignment (0)
     * <li value="1">Sum assignment (0)
     * <li value="2">Difference assignment (0)
     * <li value="3">Product assignment (0)
     * <li value="4">Quotient assignment (0)
     * <li value="5">Remainder assignment (0)
     * <li value="6">Bitwise AND assignment (0)
     * <li value="7">Bitwise OR assignment (0)
     * <li value="8">Bitwise XOR assignment (0)
     * <li value="9">Bitwise shift left assignment (0)
     * <li value="10">Bitwise shift right assignment (0)
     * <li value="11">Ternary "if-then" (1)
     * <li value="12">Ternary "else" (1)
     * <li value="13">Boolean OR (2)
     * <li value="14">Boolean AND (3)
     * <li value="15">Bitwise OR (4)
     * <li value="16">Bitwise AND (4)
     * <li value="17">Bitwise XOR (4)
     * <li value="18">Equality comparison (5)
     * <li value="19">Inequality comparison (5)
     * <li value="20">Less-than comparison (6)
     * <li value="21">Less-than-or-equal comparison (6)
     * <li value="22">Greater-than-or-equal comparison (6)
     * <li value="23">Greater-than comparison (6)
     * <li value="24">Min-operator (6)
     * <li value="25">Max-operator (6)
     * <li value="26">String concatenation / addition (7)
     * <li value="27">Subtraction (7)
     * <li value="28">Multiplication (8)
     * <li value="29">Division (8)
     * <li value="30">Remainder (modulo) (8)
     * <li value="31">Bitwise shift left (9)
     * <li value="32">Bitwise shift right (9)
     * <li value="33">Arithmetic negation (10)
     * <li value="34">Boolean negation (10)
     * <li value="35">Bitwise complement (10)
     * <li value="36">Increment (10)
     * <li value="37">Decrement (10)
     * <li value="38">Instance identity code (10)
     * <li value="39">Hexadecimal number conversion (10)
     * <li value="40">Ditto operator(10)
     * </ol>
     */
    public String[] operators = {
            ".", "=>", // [15]
            "-", "~", "!", "+-", "++", "--", "@", "@@", null, // [14]
            "**", // [13]
            "*", "/", "%", // [12]
            "+", "-", "/\\", // [11]
            "<<", ">>", ">>>", // [10]
            "<", "<=", ">=", ">", "<?", ">?", "in", "partof", // [9]
            "==", "!=", "===", "!==", "instanceof", // [8]
            "::", "<>", // [7]
            "&", "|", "^", // [6]
            "&&", // [5]
            "||", // [4]
            "?", ":", "..", ",,", // [3]
            "=", "?=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", "&=", "|=",
            "^=", // [2]
            "before", "then" // [1]
    };

    /**
     * This table must hold definitions of the 5 standard literals, of which the
     * first 3 must be different from <code>null</code>. The following order is
     * implied:
     * <ol>
     * <li value="0">The <code>null</code> literal
     * <li value="1">The <code>false</code> literal
     * <li value="2">The <code>true</code> literal
     * <li value="3">The literal for the OS interface instance
     * <li value="4">The literal for the standard library instance
     * </ol>
     */
    public String[] standardLiterals = {
            "null", "false", "true", "os", "lib"
    };

    /**
     * This table holds the names of the custom literals (if any).
     */
    public String[] customLiteralNames = {};

    /**
     * This table holds the values for the {@link #customLiteralNames custom
     * literal names}.
     */
    public Object[] customLiteralValues = {};

    /* ***************** Meta language ***************** */

    /**
     * The character that marks a preprocessor command (it must be a separator).
     */
    public char META = '#';

    /**
     * The metalanguage keywords. The list must contain the following items
     * (which must all be word tokens)
     * <ol>
     * <li value="0">The <i>include</i> directive.
     * <li value="1">The <i>define</i> directive.
     * <li value="2">The <i>undef</i> directive.
     * <li value="3">The <i>if</i> directive.
     * <li value="4">The <i>ifdef</i> directive.
     * <li value="5">The <i>ifndef</i> directive.
     * <li value="6">The <i>elif</i> directive.
     * <li value="7">The <i>else</i> directive.
     * <li value="8">The <i>endif</i> directive.
     *
     * </ol>
     */
    public String[] metaSyntax = {
            "_",
            "define", "undef",
            "import", "export", "package", "nopackage",
            "declare", "undeclare", "load", "unload",
            "op", "left", "right", "nop",
            "remove", "include", "exit", "if", "endif", "pragma",
            "args", "version", "revision",
            "typeof", "valueof", "source", "base", "line",
            "out", "err", "warn", "print"
    };

    // the standard library instance
    private StdLib lib = null;

    public StdLib getStdLib() {
        return lib;
    }

    public StringUtils.Format createFormat() {
        StringUtils.Format result = new StringUtils.Format();

        // Set the default output format to match the configured syntax
        result.setQuotes(quotes[1], quotes[0]);
        result.setSeparator(punctuators[PCT_IDX_SEPARATOR]);
        result.setDelimiter(punctuators[PCT_IDX_DELIMITER] + " ");
        result.setListParentheses(punctuators[PCT_IDX_LISTSTART],
                punctuators[PCT_IDX_LISTEND]);
        result.setMapParentheses(punctuators[PCT_IDX_MAPSTART],
                punctuators[PCT_IDX_MAPEND]);
        result.setArrayParentheses(punctuators[PCT_IDX_ARRAYSTART],
                punctuators[PCT_IDX_ARRAYEND]);
        result.setNull(standardLiterals[0]);
        result.setVariableFormat("", " " + operators[OP_IDX_ASSIGN] + " ");
        return result;
    }

    // check whether two symbols are defined equally, ignoring undefined symbols
    private final static boolean equalDef(String s1, String s2) {
        return (s1 == null) ? false : s1.equals(s2);
    }

    private static String getAlias(String s) {
        int idx = s.lastIndexOf('.');
        return s.substring(idx + 1);
    }

    void configureTypes(HojoLexer lex) {
        String s;
        for (int i = 0; i < types.length; i++) {
            if ((s = types[i]) != null) {
                lex.addSymbol(s, TT_TYPE, HojoLib.types[i].toClass());
            }
        }
        for (int i = 0; i < imports.length; i++) {
            lex.addSymbol(getAlias(imports[i]), TT_TYPE, null,
                    new ClassLoaderAction(imports[i]));
        }
    }

    // configure the operators, while allowing OP_NEG and OP_SUB to be equal.
    // return the configured ID for OP_NEG
    int configureOperators(HojoLexer lex) {
        boolean restoreOpNeg;
        int result;
        Number n;
        String s;

        if (equalDef(operators[OP_IDX_NEG], operators[OP_IDX_SUB])) {
            result = OP_SUB;
            operators[OP_IDX_NEG] = null;
            restoreOpNeg = true;
        }
        else {
            result = OP_NEG;
            restoreOpNeg = false;
        }
        try {
            for (int i = 0; i < operators.length; i++) {
                if ((s = operators[i]) != null) {
                    if (i >= OP_IDX_ASSIGN && i <= OP_IDX_ASSGN_XOR) {
                        // compound assignment - store the additional operator
                        // code as well.
                        // lex.id will still yield OP_CODES[i] because
                        // .intValue() is used
                        // to set lex.id.
                        n = new Long(((long)OP_COMPOUND_OPS[i
                                - OP_IDX_ASSIGN] << 32) | OP_CODES[i]);
                    }
                    else {
                        n = new Integer(OP_CODES[i]);
                    }
                    lex.addSymbol(s, TT_OPERATOR, n);
                }
            }
        }
        finally {
            if (restoreOpNeg) {
                operators[OP_IDX_NEG] = operators[OP_IDX_SUB];
            }
        }
        return result;
    }

    void configureMetaSyntax(final HojoLexer lex, final HojoInterpreter ipret) {
        lex.metaChar(META);
        String s;
        for (int i = META_DEFINE; i <= META_PRAGMA; i++) {
            if ((s = metaSyntax[i - META_BASE_ID]) != null) {
                lex.addMetaSymbol(s, new Integer(i), null);
            }
        }

        if ((s = metaSyntax[META_ANS - META_BASE_ID]) != null) {
            lex.addSymbol(s, TT_LITERAL, null, new GenericLexer.Action() {
                @Override
                public void invoke(GenericLexer lex) {
                    lex.oval = ipret.getLastResult();
                }
            });
        }
        if ((s = metaSyntax[META_VERSION - META_BASE_ID]) != null) {
            lex.addMetaSymbol(s, new Integer(META_VERSION),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            lex.ttype = TT_LITERAL;
                            double version = Version.VERSION;
                            lex.oval = new Double(version);
                            lex.id = (int)version;
                        }
                    });
        }
        if ((s = metaSyntax[META_REVISION - META_BASE_ID]) != null) {
            lex.addMetaSymbol(s, new Integer(META_REVISION),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            lex.ttype = TT_LITERAL;
                            lex.oval = Version.REVISION;
                        }
                    });
        }
        if ((s = metaSyntax[META_TYPEOF - META_BASE_ID]) != null) {
            lex.addMetaSymbol(s, new Integer(META_TYPEOF),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            Object[] cfg = lex
                                    .getConfig(lex.nextRawToken(true, false));
                            lex.ttype = TT_LITERAL;
                            if (cfg == null) {
                                lex.oval = new Integer(lex.id = TT_UNDEFINED);
                            }
                            else {
                                lex.id = ((Integer)(lex.oval = cfg[0]))
                                        .intValue();
                            }
                        }
                    });
        }
        if ((s = metaSyntax[META_VALUEOF - META_BASE_ID]) != null) {
            lex.addMetaSymbol(s, new Integer(META_VALUEOF),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            Object[] cfg = lex
                                    .getConfig(lex.nextRawToken(true, false));
                            lex.ttype = TT_LITERAL;
                            if (cfg == null) {
                                lex.oval = null;
                                lex.id = -1;
                            }
                            else {
                                if ((lex.oval = cfg[1]) instanceof Number) {
                                    lex.id = ((Number)lex.oval).intValue();
                                }
                                else {
                                    lex.id = -1;
                                }
                            }
                        }
                    });
        }
        if ((s = metaSyntax[META_LINE - META_BASE_ID]) != null) {
            lex.addMetaSymbol(s, new Integer(META_LINE),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            lex.ttype = TT_LITERAL;
                            lex.id = lex.lineno();
                            lex.oval = new Integer(lex.id);
                        }
                    });
        }
        if ((s = metaSyntax[META_SOURCE - META_BASE_ID]) != null) {
            lex.addMetaSymbol(s, new Integer(META_SOURCE),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            lex.ttype = TT_LITERAL;
                            lex.oval = lex.url();
                        }
                    });
        }
        if ((s = metaSyntax[META_BASE - META_BASE_ID]) != null) {
            lex.addMetaSymbol(s, new Integer(META_BASE),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            lex.ttype = TT_LITERAL;
                            lex.oval = null;
                            try {
                                URL url = lex.url();
                                if (url != null) {
                                    lex.oval = new File(url.getFile())
                                            .getParentFile();
                                }
                            }
                            catch (Exception e) {
                            }
                        }
                    });
        }
        if ((s = metaSyntax[META_OUT - META_BASE_ID]) != null) {
            lex.addMetaSymbol(s, new Integer(META_OUT),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            lex.ttype = TT_LITERAL;
                            lex.id = -1;
                            lex.oval = ((HojoLexer)lex).getHojoObserver()
                                    .getOutputWriter();
                        }
                    });
        }
        if ((s = metaSyntax[META_ERR - META_BASE_ID]) != null) {
            lex.addMetaSymbol(s, new Integer(META_ERR),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            lex.ttype = TT_LITERAL;
                            lex.id = -1;
                            lex.oval = ((HojoLexer)lex).getHojoObserver()
                                    .getErrorWriter();
                        }
                    });
        }
        if ((s = metaSyntax[META_WARN - META_BASE_ID]) != null) {
            final String name = "" + META + s;
            final Function warn = new StandardFunction() {
                private static final long serialVersionUID = 1L;

                @Override
                public Class[] getParameterTypes() {
                    return STRING_ARG;
                }

                @Override
                public Class getReturnType() {
                    return Void.TYPE;
                }

                @Override
                public Object invoke(Object[] arguments) throws HojoException {
                    ipret.getObserver().handleWarning(new HojoException(
                            null, HojoException.WARN_USER,
                            new String[] { "" + arguments[0] },
                            lex.currentLocation()));
                    return null;
                }

                @Override
                public String toString() {
                    return name;
                }
            };
            lex.addMetaSymbol(s, new Integer(META_WARN),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            lex.ttype = TT_LITERAL;
                            lex.oval = warn;
                        }
                    });
        }

        final Function format = lib.format;

        if ((s = metaSyntax[META_PRINT - META_BASE_ID]) != null) {
            final String name = "" + META + s;
            final Function print = new StandardFunction() {
                private static final long serialVersionUID = 1L;

                @Override
                public Class[] getParameterTypes() {
                    return ONE_ARG;
                }

                @Override
                public Class getReturnType() {
                    return Void.TYPE;
                }

                @Override
                public Object invoke(Object[] arguments) throws HojoException {
                    Writer wr = ipret.getObserver().getOutputWriter();
                    StringBuffer buf = (StringBuffer)format.invoke(
                            format.validateArgs(arguments));

                    try {
                        char[] cs = new char[buf.length()];
                        buf.getChars(0, buf.length(), cs, 0);
                        wr.write(cs);
                        wr.flush();
                    }
                    catch (IOException e) {
                    }
                    return null;
                }

                @Override
                public String toString() {
                    return name;
                }
            };
            lex.addMetaSymbol(s, new Integer(META_PRINT),
                    new GenericLexer.Action() {
                        @Override
                        public void invoke(GenericLexer lex) {
                            lex.ttype = TT_LITERAL;
                            lex.oval = print;
                        }
                    });
        }
        /*
         * if ((s = metaSyntax[META_FORMAT - META_BASE_ID]) != null) { final
         * String name = "" + META + s; final Function fmt = new
         * StandardFunction() { public Class[] getParameterTypes() { return
         * ONE_ARG; }
         *
         * public Class getReturnType() { return StringBuffer.class; }
         *
         * public Object invoke(Object[] arguments) throws HojoException {
         * return toHojoString(arguments[0], null, ""); }
         *
         * public String toString() { return name; } }; lex.addMetaSymbol(s, new
         * Integer(META_FORMAT), new GenericLexer.Action() { public void
         * invoke(GenericLexer lex) { lex.ttype = TT_LITERAL; lex.oval = fmt; }
         * }); }
         */
    }

    public int[] configure(HojoLexer lex, StringUtils.Format standardFormat)
            throws IllegalArgumentException {
        String s;
        int[] result = new int[8];

        lex.eolIsSignificant(false);
        lex.parseNumbers(false);
        lex.useJavaIdentifierStart(optJavaIdentifiers);
        lex.lowerCaseMode(!optCaseSensitive);
        lex.unicodeMode(optUnicodeEscapes);

        lex.setNumberFormat(numberChars[0], numberChars[1], numberChars[2],
                numberChars[3], numberChars[4], suffixes);
        lex.quoteChar(quotes[0]);
        lex.charQuoteChar(quotes[1]);
        lex.rawQuoteChar(quotes[2]);
        lex.nestedComments(nestedComments);

        for (int i = 0; i < chrWhitespace.length; i++) {
            lex.whitespaceChars(chrWhitespace[i++], chrWhitespace[i]);
        }
        for (int i = 0; i < chrWord.length; i++) {
            lex.wordChars(chrWord[i++], chrWord[i]);
        }
        for (int i = 0; i < singleLineComments.length; i++) {
            lex.addSingleLineComment(singleLineComments[i]);
        }
        for (int i = 0; i < multipleLineComments.length; i++) {
            lex.addMultipleLineComment(multipleLineComments[i][0],
                    multipleLineComments[i][1]);
        }
        for (int i = 0; i < reserved.length; i++) {
            if ((s = reserved[i]) != null) {
                lex.addSymbol(s, RES_BASE_ID + i);
            }
        }
        configureTypes(lex);

        final StringUtils.Format fmt = (StringUtils.Format)standardFormat
                .clone();
        fmt.setFmt(StringUtils.FORMAT_NONE);
        fmt.useSizeLimit(false);
        lib = new StdLib(fmt);

        if ((s = standardLiterals[0]) != null) {
            lex.addSymbol(s, TT_LITERAL, Const.NULL);
        }
        if ((s = standardLiterals[1]) != null) {
            lex.addSymbol(s, TT_LITERAL, Const.FALSE);
        }
        if ((s = standardLiterals[2]) != null) {
            lex.addSymbol(s, TT_LITERAL, Const.TRUE);
        }
        if ((s = standardLiterals[3]) != null) {
            lex.addSymbol(s, TT_LITERAL, new Const(new OsInterface()));
        }
        if ((s = standardLiterals[4]) != null) {
            lex.addSymbol(s, TT_LITERAL, new Const(lib));
        }
        for (int i = 0; i < customLiteralNames.length; i++) {
            if ((s = customLiteralNames[i]) != null) {
                lex.addSymbol(s, TT_LITERAL, customLiteralValues[i]);
            }
        }

        // configure the punctuators, while allowing some operators to be
        // equally
        // defined.

        // start by adding token names for error messages
        for (int i = 0; i < punctuators.length; i++) {
            lex.setTokenName(i + PCT_BASE_ID, punctuators[i]);
        }

        // Add theordinary, block and index parentheses. These must
        // be unambiguously defined
        for (int i = PCT_IDXEND - PCT_BASE_ID; i >= 0; i--) {
            lex.addSpecialToken(punctuators[i], PCT_BASE_ID + i);
        }

        // add PCT_MAPSTART and PCT_MAPEND
        s = punctuators[PCT_MAPSTART - PCT_BASE_ID];
        if (equalDef(s, punctuators[PCT_BLOCKSTART - PCT_BASE_ID])) {
            result[0] = PCT_BLOCKSTART;
        }
        else if (equalDef(s, punctuators[PCT_IDXSTART - PCT_BASE_ID])) {
            result[0] = PCT_IDXSTART;
        }
        else {
            result[0] = PCT_MAPSTART;
            lex.addSpecialToken(s, PCT_MAPSTART);
        }
        s = punctuators[PCT_MAPEND - PCT_BASE_ID];
        if (equalDef(s, punctuators[PCT_BLOCKEND - PCT_BASE_ID])) {
            result[1] = PCT_BLOCKEND;
        }
        else if (equalDef(s, punctuators[PCT_IDXEND - PCT_BASE_ID])) {
            result[1] = PCT_IDXEND;
        }
        else {
            result[1] = PCT_MAPEND;
            lex.addSpecialToken(s, PCT_MAPEND);
        }

        // add PCT_ARRAYSTART and PCT_ARRAYEND
        s = punctuators[PCT_ARRAYSTART - PCT_BASE_ID];
        if (equalDef(s, punctuators[PCT_BLOCKSTART - PCT_BASE_ID])) {
            result[2] = PCT_BLOCKSTART;
        }
        else if (equalDef(s, punctuators[PCT_IDXSTART - PCT_BASE_ID])) {
            result[2] = PCT_IDXSTART;
        }
        else if (equalDef(s, punctuators[PCT_MAPSTART - PCT_BASE_ID])) {
            result[2] = PCT_MAPSTART;
        }
        else {
            result[2] = PCT_ARRAYSTART;
            lex.addSpecialToken(s, PCT_ARRAYSTART);
        }
        s = punctuators[PCT_ARRAYEND - PCT_BASE_ID];
        if (equalDef(s, punctuators[PCT_BLOCKEND - PCT_BASE_ID])) {
            result[3] = PCT_BLOCKEND;
        }
        else if (equalDef(s, punctuators[PCT_IDXEND - PCT_BASE_ID])) {
            result[3] = PCT_IDXEND;
        }
        else if (equalDef(s, punctuators[PCT_MAPEND - PCT_BASE_ID])) {
            result[3] = PCT_MAPEND;
        }
        else {
            result[3] = PCT_ARRAYEND;
            lex.addSpecialToken(s, PCT_ARRAYEND);
        }

        // add PCT_LISTSTART and PCT_LISTEND
        s = punctuators[PCT_LISTSTART - PCT_BASE_ID];
        if (equalDef(s, punctuators[PCT_BLOCKSTART - PCT_BASE_ID])) {
            result[4] = PCT_BLOCKSTART;
        }
        else if (equalDef(s, punctuators[PCT_IDXSTART - PCT_BASE_ID])) {
            result[4] = PCT_IDXSTART;
        }
        else if (equalDef(s, punctuators[PCT_MAPSTART - PCT_BASE_ID])) {
            result[4] = PCT_MAPSTART;
        }
        else if (equalDef(s, punctuators[PCT_ARRAYSTART - PCT_BASE_ID])) {
            result[4] = PCT_ARRAYSTART;
        }
        else {
            result[4] = PCT_LISTSTART;
            lex.addSpecialToken(s, PCT_LISTSTART);
        }
        s = punctuators[PCT_LISTEND - PCT_BASE_ID];
        if (equalDef(s, punctuators[PCT_BLOCKEND - PCT_BASE_ID])) {
            result[5] = PCT_BLOCKEND;
        }
        else if (equalDef(s, punctuators[PCT_IDXEND - PCT_BASE_ID])) {
            result[5] = PCT_IDXEND;
        }
        else if (equalDef(s, punctuators[PCT_MAPEND - PCT_BASE_ID])) {
            result[5] = PCT_MAPEND;
        }
        else if (equalDef(s, punctuators[PCT_ARRAYEND - PCT_BASE_ID])) {
            result[5] = PCT_ARRAYEND;
        }
        else {
            result[5] = PCT_LISTEND;
            lex.addSpecialToken(s, PCT_LISTEND);
        }

        if (equalDef(s = punctuators[PCT_IDX_CASELABEL],
                operators[OP_IDX_ELSE])) {
            result[6] = TT_OPERATOR;
        }
        else {
            lex.addSpecialToken(s, result[6] = PCT_CASELABEL);
        }

        result[7] = configureOperators(lex);

        return result;
    }

    public void setArgs(HojoLexer lex, final String[] args) {
        lex.addMetaSymbol(metaSyntax[META_ARGS - META_BASE_ID], null,
                new GenericLexer.Action() {
                    @Override
                    public void invoke(GenericLexer lex) {
                        lex.ttype = TT_LITERAL;
                        lex.oval = args;
                    }
                });
    }

    @Override
    public Object clone() {
        try {
            HojoSyntax stx = (HojoSyntax)super.clone();
            stx.chrWhitespace = chrWhitespace.clone();
            stx.chrWord = chrWord.clone();
            stx.singleLineComments = singleLineComments.clone();
            stx.multipleLineComments = multipleLineComments.clone();
            stx.customLiteralNames = customLiteralNames.clone();
            stx.customLiteralValues = customLiteralValues.clone();
            stx.metaSyntax = metaSyntax.clone();
            stx.numberChars = numberChars.clone();
            stx.operators = operators.clone();
            stx.packagePrefixes = packagePrefixes.clone();
            stx.punctuators = punctuators.clone();
            stx.quotes = quotes.clone();
            stx.reserved = reserved.clone();
            stx.standardLiterals = standardLiterals.clone();
            stx.suffixes = suffixes.clone();
            stx.types = types.clone();
            stx.imports = imports.clone();
            return stx;
        }
        catch (CloneNotSupportedException e) {
            throw new HojoException();
        }
    }

    public String describeCompilerSyntax(String baseMsg, String falseMsg,
            String trueMsg, String rangeMsg, int colWidth, int width) {

        String[] args = {
                describeCharRange(chrWhitespace, rangeMsg, colWidth, width),
                describeCharRange(chrWord, rangeMsg, colWidth, width),
                StringUtils.listColumns(null, singleLineComments, "", true,
                        colWidth, width).toString(),
                StringUtils
                        .listColumns(null, combineConfig(multipleLineComments),
                                "", true, colWidth, width)
                        .toString(),
                nestedComments ? trueMsg : falseMsg,
                optCaseSensitive ? trueMsg : falseMsg,
                optUnicodeEscapes ? trueMsg : falseMsg,
                optJavaIdentifiers ? trueMsg : falseMsg,
                StringUtils.listColumns(null, punctuators, "", true, colWidth,
                        width).toString(),
                StringUtils.listColumns(null, getCharConfig(quotes), "", false,
                        colWidth, width).toString(),
                StringUtils.listColumns(null, getCharConfig(numberChars), "",
                        false, colWidth, width).toString(),
                StringUtils.listColumns(null, getCharConfig(suffixes), "",
                        false, colWidth, width).toString(),
                StringUtils
                        .listColumns(null, reserved, "", true, colWidth, width)
                        .toString(),
                StringUtils.listColumns(null, types, "", true, colWidth, width)
                        .toString(),
                getNames(imports),
                StringUtils.listColumns(null, packagePrefixes, "", true,
                        colWidth, width).toString(),
                StringUtils
                        .listColumns(null, operators, "", true, colWidth, width)
                        .toString(),
                StringUtils.listColumns(null, standardLiterals, "", true,
                        colWidth, width).toString(),
                StringUtils.listColumns(null, customLiteralNames, "", true,
                        colWidth, width).toString(),
        };

        // Do an ad hoc format, since MessageFormat limits the argument number
        // to 9 !!!!!!!!!!!!!!
        return StringUtils.simpleMessageFormat(null, baseMsg, args).toString();
    }

    public String describeInterpreterSyntax(String baseMsg, int colWidth,
            int width) {
        String[] args = {
                StringUtils.toJavaChar(META),
                StringUtils.toJavaString(metaSyntax[META_ANS - META_BASE_ID]),
                StringUtils.listColumns(null,
                        getConfig(metaSyntax, META_DEFINE - META_BASE_ID,
                                META_PRAGMA - META_BASE_ID),
                        "" + META, true, colWidth, width).toString(),
                StringUtils.listColumns(null,
                        getConfig(metaSyntax, META_ARGS - META_BASE_ID,
                                META_PRINT - META_BASE_ID),
                        "" + META, true, colWidth, width).toString()
        };

        // Do an ad hoc format, since MessageFormat limits the argument number
        // to 9 !!!!!!!!!!!!!!
        return StringUtils.simpleMessageFormat(null, baseMsg, args).toString();
    }

    private static String describeChars(char lo, char hi, String rangeMsg) {
        if (lo == hi) {
            return StringUtils.toJavaChar(lo);
        }
        else {
            return MessageFormat.format(rangeMsg, new Object[] {
                    StringUtils.toJavaChar(lo), StringUtils.toJavaChar(hi) });
        }
    }

    private static String describeCharRange(char[] cs, String rangeMsg,
            int colWidth, int width) {
        String[] config = new String[cs.length / 2];
        for (int i = 0; i < cs.length; i += 2) {
            config[i / 2] = describeChars(cs[i], cs[i + 1], rangeMsg);
        }
        return StringUtils.listColumns(null, config, "", false, colWidth,
                width);
    }

    private static String[] combineConfig(String[][] config) {
        String[] result = new String[config.length];
        for (int i = 0; i < config.length; i++) {
            if (config[i] != null) {
                result[i] = config[i][0] + ' ' + config[i][1];
            }
        }
        return result;
    }

    private static String getNames(String[] cs) {
        StringBuffer result = new StringBuffer(20 * cs.length);
        for (int i = 0; i < cs.length; i++) {
            result.append(cs[i] == null ? "\n" : cs[i] + '\n');
        }
        return result.toString();
    }

    private static String[] getCharConfig(char[] cfg) {
        String[] result = new String[cfg.length];
        for (int i = 0; i < cfg.length; i++) {
            result[i] = cfg[i] == '\0' ? null : StringUtils.toJavaChar(cfg[i]);
        }
        return result;
    }

    private static String[] getConfig(String[] cfg, int lo, int hi) {
        String[] result = new String[hi - lo + 1];
        System.arraycopy(cfg, lo, result, 0, result.length);
        return result;
    }

    /**
     * Convert the given object to a string which, when translated by a Hojo
     * compiler having the syntax represented by this instance, will yield an
     * object equal to the given object. This will not work for arbitrary
     * classes of objects, though: given an instance i of class c, the default
     * conversion will be
     * <code>new <i>c.getName()</i>(<i>i.toString()</i>)</code>.
     *
     * @param obj
     *            the object to be converted
     * @return a string in Hojo syntax for re-generating the value
     */
    public String toHojoString(Object obj) {
        return toHojoString(obj, new StringBuffer(), null, "").toString();
    }

    public StringBuffer toHojoString(Object obj, StringBuffer buf,
            String indent) {
        return toHojoString(obj, buf, null, indent);
    }

    public StringBuffer toHojoString(Object obj, StringBuffer buf,
            Class enclosing, String indent) {
        Class c;

        if (buf == null) {
            buf = new StringBuffer();
        }

        if (obj == null) {
            return buf.append(standardLiterals[0]);
        }
        else if (obj instanceof Number) {
            return toHojoNumber((Number)obj, buf);
        }
        else if (obj instanceof String) {
            return toHojoStringLiteral((String)obj, buf);
        }
        else if (obj instanceof Character) {
            return buf.append(quotes[1]).append(StringUtils.toJavaChar(
                    ((Character)obj).charValue(), true)).append(quotes[1]);
        }
        else if (obj instanceof Boolean) {
            return buf.append(
                    standardLiterals[((Boolean)obj).booleanValue() ? 2 : 1]);
        }
        else if (obj instanceof Date) {
            StringBuffer b = new StringBuffer();
            toHojoNumber(new Long(((Date)obj).getTime()), b);
            return buf.append(reserved[RES_NEW - RES_BASE_ID]).append(' ')
                    .append("java.util.Date")
                    .append(punctuators[PCT_IDX_LPAREN]).append(b)
                    .append(punctuators[PCT_IDX_RPAREN]);
        }
        else if (obj instanceof Class) {
            c = (Class)obj;
            if (c.isPrimitive()) {
                if (c == Void.TYPE) {
                    return buf.append("java.lang.Void ")
                            .append(operators[OP_IDX_DOT]).append("TYPE");
                }
                else {
                    Class cw = ReflectUtils.getWrapper(c);
                    return buf.append(cw.getName()).append(' ')
                            .append(operators[OP_IDX_DOT]).append("TYPE");
                }
            }
            else {
                buf.append("java.lang.Class .forName")
                        .append(punctuators[PCT_IDX_LPAREN]);
                toHojoStringLiteral(c.getName(), buf);
                return buf.append(punctuators[PCT_IDX_RPAREN]);
            }
        }

        c = obj.getClass();

        if (obj instanceof Object[] || c.isArray()) {
            Class objc = Object[].class;
            if (c != enclosing && c != objc) {
                // add an explicit array class name
                buf.append(reserved[RES_NEW - RES_BASE_ID]).append(' ')
                        .append(ReflectUtils.className2Java(c)).append(' ');
            }

            buf.append(punctuators[PCT_IDX_ARRAYSTART]);
            Class newEnclosing = enclosing == null ? c.getComponentType()
                    : enclosing.getComponentType();
            if (!newEnclosing.isArray()) {
                newEnclosing = null;
            }
            mkHojoSequence(
                    obj instanceof Object[] ? Arrays.asList((Object[])obj)
                            : (List)new PrimitiveArrayList(obj),
                    buf, newEnclosing, indent);
            return buf.append(punctuators[PCT_IDX_ARRAYEND]);
        }
        else if (obj instanceof Collection) {
            if (c != enclosing && c != ConvertUtils.DEFAULT_LIST_CLASS) {
                // add an explicit class name and default constructor
                buf.append(reserved[RES_NEW - RES_BASE_ID]).append(' ')
                        .append(ReflectUtils.className2Java(c)).append(' ')
                        .append(punctuators[PCT_IDX_LPAREN])
                        .append(punctuators[PCT_IDX_RPAREN]).append(' ');
            }
            buf.append(punctuators[PCT_IDX_LISTSTART]);
            mkHojoSequence((Collection)obj, buf, null, indent);
            return buf.append(punctuators[PCT_IDX_LISTEND]);
        }
        else if (obj instanceof Map) {
            if (c != enclosing && c != ConvertUtils.DEFAULT_MAP_CLASS) {
                // add an explicit class name and default constructor
                buf.append(reserved[RES_NEW - RES_BASE_ID]).append(' ')
                        .append(ReflectUtils.className2Java(c)).append(' ')
                        .append(punctuators[PCT_IDX_LPAREN])
                        .append(punctuators[PCT_IDX_RPAREN]).append(' ');
            }

            buf.append(punctuators[PCT_IDX_MAPSTART]);
            String indent2 = indent + "  ";
            boolean delim = false;
            for (Iterator i = ((Map)obj).entrySet().iterator(); i.hasNext();) {
                if (delim) {
                    buf.append(punctuators[PCT_IDX_DELIMITER]);
                }
                else {
                    delim = true;
                }
                buf.append('\n').append(indent2);

                Map.Entry e = (Map.Entry)i.next();
                Object key = e.getKey();

                if (key instanceof String) {
                    toHojoStringLiteral((String)key, buf);
                }
                else {
                    buf.append(punctuators[PCT_IDX_LPAREN]);
                    toHojoString(key, buf, null, indent2);
                    buf.append(punctuators[PCT_IDX_RPAREN]);
                }

                buf.append(' ').append(operators[OP_IDX_ASSIGN]).append(' ');
                toHojoString(e.getValue(), buf, null, indent2);
            }

            return buf.append('\n').append(indent)
                    .append(punctuators[PCT_IDX_MAPEND]);
        }

        return toDefaultHojoString(obj, buf);
    }

    public StringBuffer toHojoNumber(Number n, StringBuffer buf) {
        int pri = ReflectUtils.getPriority(n);
        if (pri == NUM_PRI_BAD) {
            // unknown number type
            return toDefaultHojoString(n, buf);
        }

        // convert the number to a Hojo number
        String s = n.toString();
        if (s.charAt(0) == '-') {
            // add the unary minus
            buf.append(numberChars[1]);
            s = s.substring(1);
        }

        int idx = s.indexOf('.');
        if (idx >= 0) {
            // add the mantissa and convert the decimal point
            buf.append(s.substring(0, idx)).append(numberChars[2]);
            s = s.substring(idx + 1);
        }

        idx = s.indexOf('E');
        if (idx >= 0) {
            // add the fractional part and convert the exponent char
            buf.append(s.substring(0, idx)).append(numberChars[3]);
            s = s.substring(idx + 1);
            if (s.charAt(0) == '+') {
                buf.append(numberChars[0]).append(s.substring(1));
            }
            else if (s.charAt(0) == '-') {
                buf.append(numberChars[1]).append(s.substring(1));
            }
            else {
                buf.append(s);
            }
        }
        else {
            // add the rest of the number
            buf.append(s);
        }

        // add the type suffix character
        if (suffixes[pri] != '\0') {
            buf.append(suffixes[pri]);
        }
        return buf;
    }

    public StringBuffer toHojoStringLiteral(String s, StringBuffer buf) {
        return buf.append(quotes[0]).append(StringUtils.toJavaString(s, true))
                .append(quotes[0]);
    }

    public StringBuffer toDefaultHojoString(Object obj, StringBuffer buf) {
        if (obj == null) {
            return buf.append(standardLiterals[0]);
        }

        Class c = obj.getClass();
        buf.append(reserved[RES_NEW - RES_BASE_ID]).append(' ')
                .append(ReflectUtils.className2Java(c))
                .append(punctuators[PCT_IDX_LPAREN]);

        if (c != Object.class) {
            toHojoStringLiteral(obj.toString(), buf);
        }

        return buf.append(punctuators[PCT_IDX_RPAREN]);
    }

    private StringBuffer mkHojoSequence(Collection c, StringBuffer buf,
            Class enclosing, String indent) {
        boolean delim = false;

        for (Iterator i = c.iterator(); i.hasNext();) {
            if (delim) {
                buf.append(punctuators[PCT_IDX_DELIMITER]).append(' ');
            }
            else {
                delim = true;
            }

            toHojoString(i.next(), buf, enclosing, indent);
        }

        return buf;
    }

}
