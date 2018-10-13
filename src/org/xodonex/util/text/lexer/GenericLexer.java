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
package org.xodonex.util.text.lexer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import org.xodonex.util.ArrayUtils;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;
import org.xodonex.util.io.StackedReader;

/**
 * A generic, flexible lexer/tokenizer implementation.
 */
public class GenericLexer implements LexerTokens, LexerErrors {

    // default error handler
    private final static Handler DEFAULT_HANDLER = new Handler() {
        @Override
        public void handle(Throwable t, int code, Object param,
                GenericLexer lex) {
            if (t != null) {
                System.err.print(t.getMessage());
            }
            else {
                System.err.print("Error " + code);
            }
            if (param != null) {
                System.err.println(" - " + param);
            }
            else {
                System.err.println();
            }

            System.err.println(lex.currentLocation());
        }
    };

    // special token types for the commentTokens
    private final static int TT_LINECOMMENT = -128;
    private final static int TT_MULTICOMMENTSTART = -129;
    private final static int TT_MULTICOMMENTEND = -130;

    private final static int CT_MIN = 1; // least configurable char
    private final static int CT_MAX = 255; // largest configurable char

    private final static int CT_ORDINARY = 0, // no special significance
            CT_WHITESPACE = 1, // white space

            CT_NUM = 2 * CT_WHITESPACE, // numeral
            CT_HEXNUM = 2 * CT_NUM, // hexadecimal numeral
            CT_WORD = 2 * CT_HEXNUM, // ordinary letter

            CT_QUOTE = 2 * CT_WORD, // quote char
            CT_CHARQUOTE = CT_QUOTE | 2 * CT_QUOTE, // single-char quote char
            CT_RAWQUOTE = CT_QUOTE | 4 * CT_QUOTE, // raw quote char (preserve
                                                   // escapes and linebreaks).

            CT_POS = 8 * CT_QUOTE, // positive sign char
            CT_NEG = 2 * CT_POS, // negetive sign char
            CT_EXP = 2 * CT_NEG, // exponent char
            CT_POINT = 2 * CT_EXP, // decimal point
            CT_HEX = 2 * CT_POINT, // hex indicator char
            CT_SUFFIX = 2 * CT_HEX, // number type suffix char

            CT_META = 2 * CT_SUFFIX, // meta-command character

            CT_WORDCHAR = CT_WORD | CT_NUM, // identifier part char
            CT_NORMAL_MASK = CT_WHITESPACE | CT_NUM | CT_WORD | CT_QUOTE;

    // the unique reader instance used to represent macros that substitute
    // to the empty string
    private final static StringReader DEFINED = new StringReader("") {
        @Override
        public String toString() {
            return "";
        }
    };

    /**
     * The type of the last token
     */
    public int ttype;

    /**
     * The numeric value of the last parsed token, if it is {@link #TT_NUMBER}
     */
    public double nval;

    /**
     * The string value of the last token, if it is a quoted string or an
     * identifier.
     */
    public String sval;

    /**
     * The object value associated with the last token, or <code>null</code> if
     * no value has been associated.
     */
    public Object oval;

    /**
     * The numeric value of the {@link #oval token value}, if this is a
     * {@link java.lang.Number Number}, and <code>-1</code> otherwise.
     */
    public int id;

    /**
     * The <code>Reader</code> from which all input is read.
     */
    protected StackedReader in = new StackedReader();

    // pushback storage
    private final CharStack stack = new CharStack(8);

    // string buffer for input text
    private int inputIdx = 0;
    private char[] inputBuffer = new char[64];

    // token location
    private int tokenStart = 0;
    private int tokenEnd = 0;

    // the next characte to be processed by nextToken()
    private int next = -1;

    // the next character to be unicode-processed
    private int nextRaw = -1;

    // the number of contiguous backslashes seen so far
    private int escapeCount = 0;

    // true iff ttype == TT_EOF was returned
    private boolean clearInput = false;

    // true iff pushBack() was called last
    private boolean pushedBack = false;

    // true iff resync() was called last
    private boolean resynced = true;

    // true iff parseToken0() has parsed a raw token (escape prefix)
    private boolean rawEscape = false;

    // debug output writer
    private Writer debug = null;

    // config data
    private boolean nestedComments; // whether multiline comments are nested
    private boolean useEOL; // return TT_EOL
    private boolean useEOF; // return TT_EOF
    private boolean caseSensitive; // don't convert to lowercase
    private boolean uuDecode; // decode unicode-escapes
    private boolean parseNumbers; // don't return TT_NUMBER when numbers are
                                  // parsed
    private boolean enforceJavaID; // return TT_WORD iff first
                                   // char.isJavaIdentifierStart()

    // temporary buffer for quoted strings and identifiers
    private char[] tokenBuffer = new char[64];
    private int tokenIdx = 0;

    // lookup tables

    // identifier -m-> StringReader
    private HashMap macros = new HashMap(11);

    // identifier -m-> TokenConfig
    private final HashMap metaSymbols = new HashMap(21);

    // identifier -m-> TokenConfig
    private final HashMap reserved = new HashMap(53);

    // Character -m-> Integer
    private final HashMap numberSuffixes = new HashMap(17);

    // all special tokens, including startComments
    private final TokenChar specialTokens = new TokenChar();

    private final TokenChar startComments = new TokenChar();
    private final TokenChar endComments = new TokenChar();

    // Integer -m-> String (for error messages)
    private final HashMap tokenTypeNames = new HashMap(31);

    // char config table, contains CT_xxx flags
    private final int[] ctypes = new int[CT_MAX + 1];

    // a Recovery view of this lexer
    private final Recovery rec;

    // the observer for this lexer
    private Observer obs = null;

    // the error handler
    private Handler handler = DEFAULT_HANDLER;

    // the current URL
    private URL loc = null;

    // The current line number
    private int lineno = 1;

    // The locations (URLs) for every open, inactive reader
    private final Stack locationStack = new Stack();

    // The read-ahead characters for every open, inactive reader
    private final Stack recycleStack = new Stack();

    // Token type / value / action configuration
    private static class TokenConfig {
        private int ttype;
        private Object value;
        private Action action;

        public TokenConfig(int ttype, Object value, Action action) {
            this.ttype = ttype;
            this.value = value;
            this.action = action;
        }

        public int setResult(GenericLexer lex, boolean useAction) {
            lex.oval = value;
            lex.id = (value instanceof Number) ? ((Number)value).intValue()
                    : -1;
            lex.ttype = ttype;
            if (useAction && action != null) {
                if (lex.debug != null) {
                    lex.addDebugText(lex.createDebugEntry("ACTION",
                            ") : " + action + '\n'));
                }
                action.invoke(lex);
            }
            return lex.ttype;
        }

        public int ttype() {
            return ttype;
        }

        public Object value() {
            return value;
        }

        public Action action() {
            return action;
        }

        @Override
        public int hashCode() {
            return ttype ^ ((value == null) ? 0 : value.hashCode());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TokenConfig)) {
                return false;
            }

            TokenConfig tc = (TokenConfig)o;
            return tc.ttype == ttype &&
                    ((value == null) ? tc.value == null
                            : value.equals(tc.value))
                    &&
                    ((action == null) ? tc.action == null
                            : action.equals(tc.action));
        }
    }

    /**
     * An <code>Observer</code> gets notified of changes in lexer input.
     */
    public static interface Observer {
        /**
         * Notifies the observer that the given URL is about to be included.
         *
         * @param url
         *            the URL which is about to be included
         * @return true iff the URL is allowed to be included.
         */
        public boolean includeStart(URL url);

        /**
         * Notifies the observer that the given URL is no longer included.
         *
         * @param url
         *            the URL which has just ceased to be included
         */
        public void includeEnd(URL url);
    }

    /**
     * A <code>Handler</code> is responsible for taking the appropriate action
     * in case of an error condition.
     */
    public static interface Handler extends LexerErrors {

        /**
         * Handle the given error condition.
         *
         * @param t
         *            the <code>Throwable</code> which caused the error. This
         *            may be <code>null</code>.
         * @param code
         *            an error code.
         * @param param
         *            an addtional, optional parameter which may be present for
         *            some errors.
         * @param lex
         *            the lexer which caused the error.
         */
        public void handle(Throwable t, int code, Object param,
                GenericLexer lex);
    }

    /**
     * An <code>Action</code> can be used to execute some code every time the
     * symbol to which it is assigned is encountered by the lexer.
     */
    public static interface Action {
        /**
         * Invokes the action represented by this object
         *
         * @param lex
         *            The lexer which triggered the action. The lexer's current
         *            token will be the action trigger.
         * @exception RuntimeException
         *                if an error occurs during the execution.
         */
        public void invoke(GenericLexer lex) throws RuntimeException;
    }

    /**
     * A <code>Recovery</code> object is used to enable external objects to
     * remove erroneous input from the lexer without giving access to other
     * methods in the lexer.
     */
    public class Recovery {

        /**
         * Parses the next token from the input and stores the result as the
         * current token. However, no actions will be performed, and numbers
         * will always be be parsed.
         *
         * @return The type of the token which was read
         * @exception RuntimeException
         *                If an I/O or lexical error is encountered.
         */
        public int skipToken() throws RuntimeException {
            return GenericLexer.this.skipToken();
        }

        /**
         * Discards the current input token, but preserves any characters
         * already read.
         */
        public void dropToken() {
            GenericLexer.this.dropToken();
        }

        /**
         * Removes the lexer's top-level input stream.
         *
         * @return <code>true</code> if the top-level input was removed, and
         *         <code>false</code> if no input is was present.
         * @exception RuntimeException
         *                If an I/O error occurs.
         */
        public boolean exit() throws RuntimeException {
            return GenericLexer.this.exit();
        }

        /**
         * Resynchronizes the input, such that any input already read will be
         * discarded. This will force the lexer to begin reading upon the next
         * call to {@link #nextToken()}. At the same time, the current token is
         * cleared.
         *
         * @return The input character that was discarded.
         */
        public int resync() {
            return GenericLexer.this.resync();
        }
    }

    /**
     * Constructs a new, empty <code>GenericLexer</code>.
     */
    public GenericLexer() {
        resetSyntax();
        resync();
        in.setAutoClose(false);
        in.setMonitoring(false);
        rec = new Recovery();
    }

    /**
     * Constructs a new <code>GenericLexer</code> having the specified
     * <code>Reader</code> as input.
     *
     * @param r
     *            the initial input.
     */
    public GenericLexer(Reader r) {
        this();
        in.push(r);
    }

    // return whether c can be used as configuration char
    private boolean use(int c) {
        return (c >= CT_MIN) && (c <= CT_MAX);
    }

    // return the configured type for the char c
    private int typeOf(int c) {
        return (c < CT_MIN) ? ((c == '\0') ? CT_WHITESPACE : 0)
                : ((c > CT_MAX) ? (((c >> 8) == 0xe0) ? 0 : CT_WORD)
                        : ctypes[c]);
    }

    // return whether c is configured to the given type
    private boolean isType(int c, int type) {
        if (c < CT_MIN) {
            return type == CT_WORD && c == '\0';
        }
        else if (c > CT_MAX) {
            return type == (((c >> 8) == 0xe0) ? 0 : CT_WORD);
        }
        else {
            return (ctypes[c] & type) == type;
        }
    }

    // add the flag to the config for char c
    private void addFlag(int c, int flag) {
        if ((c >= CT_MIN) && (c <= CT_MAX)) {
            ctypes[c] |= flag;
        }
    }

    // add the flag to every configurable char from lo to hi
    private void addFlags(int lo, int hi, int flag) {
        if (lo < CT_MIN) {
            lo = CT_MIN;
        }
        if (hi > CT_MAX) {
            hi = CT_MAX;
        }

        while (lo <= hi) {
            ctypes[lo++] |= flag;
        }
    }

    // return the number type ID corresponding to the given number type suffix
    private int numberType(char suffix) {
        if (suffix == '\0') {
            return ReflectUtils.NUM_PRI_BAD;
        }

        Integer i = (Integer)numberSuffixes.get(new Character(suffix));
        return (i == null) ? ReflectUtils.NUM_PRI_BAD : i.intValue();
    }

    /**
     * Clears the entire syntax configuration. This implies that no characters
     * have a special significance and will thus be treated as separate tokens.
     */
    public void resetSyntax() {
        useEOL = false;
        uuDecode = false;
        caseSensitive = true;
        parseNumbers = false;
        enforceJavaID = false;

        macros.clear();
        metaSymbols.clear();
        reserved.clear();
        specialTokens.clear();
        startComments.clear();
        endComments.clear();
        numberSuffixes.clear();
        tokenTypeNames.clear();

        for (int i = CT_MAX; i >= 0;) {
            ctypes[i--] = CT_ORDINARY;
        }
    }

    /**
     * Toggles whether end-of-line tokens ({@link #TT_EOL}) should be returned
     * or not.
     *
     * @param significant
     *            If <code>true</code>, end-of-line tokens will be returned; if
     *            <code>false</code> then no end-of-line tokens are returned.
     * @see java.io.StreamTokenizer#eolIsSignificant(boolean)
     * @see #TT_EOL
     */
    public void eolIsSignificant(boolean significant) {
        useEOL = significant;
    }

    /**
     * Toggles whether end-of-file tokens ({@link #TT_EOF}) should be returned
     * at the end of included input.
     *
     * @param significant
     *            If <code>true</code>, end-of-file tokens will be returned; if
     *            <code>false</code> then no end-of-file tokens are returned.
     */
    public void eofIsSignificant(boolean significant) {
        useEOF = significant;
    }

    /**
     * Toggles whether identifiers should be automatically lowercased.
     *
     * @param toLower
     *            If <code>true</code>, all identifiers are converted to
     *            lowercase. This implies that macro names, reserved words,
     *            literal names etc. must be defined in lowercase, but will be
     *            recognized regardless of the input's case.
     * @see java.io.StreamTokenizer#lowerCaseMode(boolean)
     */
    public void lowerCaseMode(boolean toLower) {
        caseSensitive = !toLower;
    }

    /**
     * Toggles whether Unicode-escapes should be recognized.
     *
     * @param decode
     *            If <code>true</code>, then unicode-escape sequences are
     *            decoded. The decoding is done prior to all other lexical
     *            processing.
     */
    public void unicodeMode(boolean decode) {
        uuDecode = decode;
    }

    /**
     * Indicates that the given character has no special significance.
     *
     * @param c
     *            The character whose special importance should be removed.
     * @see java.io.StreamTokenizer#ordinaryChar(int)
     */
    public void ordinaryChar(int c) {
        if (use(c)) {
            ctypes[c] = CT_ORDINARY;
        }
    }

    /**
     * Indicates that all characters in the given range should be deprived of
     * any special significance.
     *
     * @param lo
     *            The lower end of the range (inclusive).
     * @param hi
     *            The higher end of the range (inclusive).
     * @see java.io.StreamTokenizer#ordinaryChars(int, int)
     */
    public void ordinaryChars(int lo, int hi) {
        if (lo < CT_MIN) {
            lo = CT_MIN;
        }
        if (hi > CT_MAX) {
            hi = CT_MAX;
        }

        while (lo <= hi) {
            ctypes[lo++] = CT_ORDINARY;
        }
    }

    /**
     * This is equivalent to {@link #parseNumbers(boolean) parseNumbers(true)}.
     */
    public void parseNumbers() {
        parseNumbers(true);
    }

    /**
     * Determines how numbers should be parsed. This can be done in three ways:
     * <ol>
     * <li>No number parsing. This will be the case if
     * {@link #ordinaryChars(int, int) ordinaryChars('0', '9')} is called.
     * <li>Simple number parsing. This implies that numbers are parsed
     * automatically using a positive sign and a variable representation type.
     * <li>Full number parsing. This implies that {@link #TT_NUMBER} is returned
     * as soon as a digit is encountered; {@link #parseNumber(char, boolean)
     * parseNumber()} is then used to parse the number given a representation
     * type and sign.
     * </ol>
     *
     * @param fullParse
     *            If <code>true</code>, then full number parsing is used.
     *            Otherwise simple number parsing is used.
     * @see #TT_NUMBER
     */
    public void parseNumbers(boolean fullParse) {
        parseNumbers = fullParse;
        for (int i = '0'; i <= '9'; i++) {
            ctypes[i] |= (CT_NUM | CT_HEXNUM);
        }
        for (int i = 'a'; i <= 'f'; i++) {
            ctypes[i] |= CT_HEXNUM;
        }
        for (int i = 'A'; i <= 'F'; i++) {
            ctypes[i] |= CT_HEXNUM;
        }
    }

    /**
     * Configures which format of numbers should be recognized.
     *
     * @param pos
     *            The positive-exponent sign.
     * @param neg
     *            The negative-exponent sign.
     * @param exp
     *            The exponent character (case is insignificant).
     * @param point
     *            The decimal point.
     * @param hex
     *            The hexadecimal indicator character (case is insignificant).
     * @param suffixes
     *            An array containing the suffix characters for the different
     *            number types. The case is insignificant, and <code>'\0'</code>
     *            disables a suffix. The different number types are the
     *            following:
     *            <ol>
     *            <li>{@link java.lang.Byte}
     *            <li>{@link java.lang.Short}
     *            <li>{@link java.lang.Integer}
     *            <li>{@link java.lang.Long}
     *            <li>{@link java.lang.Float}
     *            <li>{@link java.lang.Double}
     *            <li>{@link java.math.BigInteger}
     *            <li>{@link java.math.BigDecimal}
     *            </ol>
     * @see #parseNumbers(boolean)
     * @see #parseNumber(char, boolean)
     */
    public void setNumberFormat(char pos, char neg, char point,
            char exp, char hex, char[] suffixes)
            throws ArrayIndexOutOfBoundsException {
        addFlag(pos, CT_POS);
        addFlag(neg, CT_NEG);
        addFlag(point, CT_POINT);

        addFlag(Character.toLowerCase(exp), CT_EXP);
        addFlag(Character.toUpperCase(exp), CT_EXP);
        addFlag(Character.toLowerCase(hex), CT_HEX);
        addFlag(Character.toUpperCase(hex), CT_HEX);

        Integer I;
        char c;
        for (int i = ReflectUtils.NUM_PRI_BYTE; i <= ReflectUtils.NUM_PRI_BDEC; i++) {
            if (!use(suffixes[i])) {
                continue;
            }

            I = new Integer(i);
            addFlag(c = Character.toLowerCase(suffixes[i]), CT_SUFFIX);
            numberSuffixes.put(new Character(c), I);
            addFlag(c = Character.toUpperCase(suffixes[i]), CT_SUFFIX);
            numberSuffixes.put(new Character(c), I);
        }
    }

    /**
     * Toggles whether identifiers should be restricted to word tokens having a
     * first character for which the method
     * {@link java.lang.Character#isJavaIdentifierStart(char)} returns
     * <code>true</code>.
     *
     * @param use
     *            If <code>true</code>, then identifiers are restricted to those
     *            starting with a Java-type start character.
     */
    public void useJavaIdentifierStart(boolean use) {
        enforceJavaID = use;
    }

    /**
     * Configures the given character to be used as a string quote character.
     *
     * @param c
     *            The string quote character.
     * @see java.io.StreamTokenizer#quoteChar(int)
     */
    public void quoteChar(int c) {
        addFlag(c, CT_QUOTE);
    }

    /**
     * Configures the given character to be used as a char quote character.
     *
     * @param c
     *            The char quote character.
     */
    public void charQuoteChar(int c) {
        addFlag(c, CT_CHARQUOTE);
    }

    /**
     * Configures the given character to be used as a raw quote character.
     *
     * @param c
     *            The raw quote character.
     */
    public void rawQuoteChar(int c) {
        addFlag(c, CT_RAWQUOTE);
    }

    /**
     * Configures the given character to be used as the meta character.
     *
     * @param c
     *            The meta character.
     */
    public void metaChar(int c) {
        addFlag(c, CT_META);
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addSingleLineComment(String) addSingleLineComment("" + (char)c)}.
     *
     * @param c
     *            the single-line comment character
     * @see java.io.StreamTokenizer#commentChar(int)
     */
    public void commentChar(int c) throws RuntimeException {
        if (c >= 0 && c <= '\uffff') {
            addSingleLineComment("" + (char)c);
        }
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addSingleLineComment(String) addSingleLineComment("//")} or
     * {@link #removeSingleLineComment(String) removeSingleLineComment("//")},
     * depending on the parameter value.
     *
     * @param use
     *            If <code>true</code>, then the double-slash comments are used.
     * @see java.io.StreamTokenizer#slashSlashComments(boolean)
     */
    public void slashSlashComments(boolean use) throws RuntimeException {
        if (use) {
            addSingleLineComment("//");
        }
        else {
            removeSingleLineComment("//");
        }
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addMultipleLineComment(String, String)
     * addMultipleLineComment("/&#42;", "&#42;/")} or
     * {@link #removeMultipleLineComment(String, String)
     * removeMultipleLineComment("/&#42;", "&#42;/")} depending on the parameter
     * value.
     *
     * @param use
     *            If <code>true</code>, then the /&#42;&nbsp;&#42;/
     *            multiple-line comments are used.
     * @see java.io.StreamTokenizer#slashStarComments(boolean)
     */
    public void slashStarComments(boolean use) throws RuntimeException {
        if (use) {
            addMultipleLineComment("/*", "*/");
        }
        else {
            removeMultipleLineComment("/*", "*/");
        }
    }

    /**
     * Configures the given string to be used as a single-line comment
     * indicator.
     *
     * @param s
     *            The comment indicator string.
     * @exception RuntimeException
     *                if the given comment has already been configured as a
     *                single-line comment indicator.
     */
    public void addSingleLineComment(String s) throws RuntimeException {
        char[] sChars = s.toCharArray();
        if (sChars.length < 1 || specialTokens.findToken(sChars, 0) != null ||
                s.indexOf('\r') >= 0 || s.indexOf('\n') >= 0) {
            handler.handle(null, ERR_REDEFINED_SYMBOL, s, this);
            return;
        }
        specialTokens.installToken(sChars, 0, TT_LINECOMMENT, null, null);
    }

    /**
     * Removes a single-line comment indicator.
     *
     * @param s
     *            The comment indicator string.
     * @return <code>true</code> if the removal was accomplished, and
     *         <code>false</code> otherwise (if the string did not constitute a
     *         single-line comment indicator).
     */
    public boolean removeSingleLineComment(String s) {
        TokenChar tchar = specialTokens.findToken(s.toCharArray(), 0);
        if (tchar != null && tchar.ttype == TT_LINECOMMENT) {
            tchar.remove();
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Configures the given strings to be the delimiters of a multiple-line
     * comment.
     *
     * @param start
     *            The string marking the start of a multiple-line comment.
     * @param end
     *            The string marking the end of a multiple-line comment.
     * @exception RuntimeException
     *                if one of the given strings has already been configured as
     *                a comment indicator.
     */
    public void addMultipleLineComment(String start, String end)
            throws RuntimeException {
        char[] sChars = start.toCharArray();
        if (sChars.length < 1 || specialTokens.findToken(sChars, 0) != null ||
                start.indexOf('\r') >= 0 || start.indexOf('\n') >= 0) {
            handler.handle(null, ERR_REDEFINED_SYMBOL, start, this);
            return;
        }
        char[] eChars = end.toCharArray();
        if (eChars.length < 1 || endComments.findToken(eChars, 0) != null ||
                end.indexOf('\r') >= 0 || end.indexOf('\n') >= 0) {
            handler.handle(null, ERR_REDEFINED_SYMBOL, end, this);
            return;
        }

        specialTokens.installToken(sChars, 0, TT_MULTICOMMENTSTART, null, null);
        startComments.installToken(sChars, 0, TT_MULTICOMMENTSTART, null, null);
        endComments.installToken(eChars, 0, TT_MULTICOMMENTEND, null, null);
    }

    public boolean removeMultipleLineComment(String start, String end) {
        char[] chars = start.toCharArray();
        TokenChar tchar = specialTokens.findToken(chars, 0);
        if (tchar != null) {
            tchar.remove();
            startComments.findToken(chars, 0).remove();
            tchar = endComments.findToken(end.toCharArray(), 0);
            if (tchar != null) {
                tchar.remove();
            }
            return true;
        }
        else {
            return false;
        }
    }

    public void nestedComments(boolean use) {
        nestedComments = use;
    }

    public int clearLineComments() {
        return specialTokens.removeTokens(TT_LINECOMMENT);
    }

    public int clarMultipleLineComments() {
        startComments.clear();
        endComments.clear();
        return specialTokens.removeTokens(TT_MULTICOMMENTSTART);
    }

    public int clearComments() {
        startComments.clear();
        endComments.clear();
        return specialTokens.removeTokens(TT_MULTICOMMENTSTART) +
                specialTokens.removeTokens(TT_LINECOMMENT);
    }

    /**
     * Configures all characters in the given range to be white space
     * (separator) characters.
     *
     * @param lo
     *            The lower end of the range (inclusive).
     * @param hi
     *            The higher end of the range (inclusive).
     * @see java.io.StreamTokenizer#whitespaceChars(int, int)
     */
    public void whitespaceChars(int lo, int hi) {
        addFlags(lo, hi, CT_WHITESPACE);
    }

    /**
     * Configures all characters in the given range to be word characters.
     *
     * @param lo
     *            The lower end of the range (inclusive).
     * @param hi
     *            The higher end of the range (inclusive).
     * @see java.io.StreamTokenizer#wordChars(int, int)
     */
    public void wordChars(int lo, int hi) {
        addFlags(lo, hi, CT_WORD);
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addReserved(String, Object, int) addReserved(reserved, null,
     * TT_RESERVED)}.
     *
     * @param reserved
     *            the reserved word. This must be a valid identifier which has
     *            not already been configured as a reserved word.
     * @exception RuntimeException
     *                if the string is invalid.
     */
    public final void addReserved(String reserved) throws RuntimeException {
        addReserved(reserved, null, TT_RESERVED);
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addReserved(String, Object, int, Action) addReserved(reserved,
     * value, TT_RESERVED, null)}.
     *
     * @param reserved
     *            the reserved word. This must be a valid identifier which has
     *            not already been configured as a reserved word.
     * @param value
     *            the ID associated with the reserved word.
     * @exception RuntimeException
     *                if the string is invalid.
     */
    public final void addReserved(String reserved, Object value)
            throws RuntimeException {
        addReserved(reserved, value, TT_RESERVED, null);
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addReserved(String, Object, int, Action) addReserved(reserved,
     * value, ttype, null)}.
     *
     * @param reserved
     *            the reserved word. This must be a valid identifier which has
     *            not already been configured as a reserved word.
     * @param value
     *            the ID associated with the reserved word.
     * @param ttype
     *            the token type of the reserved word.
     * @exception RuntimeException
     *                if the string is invalid.
     */
    public final void addReserved(String reserved, Object value, int ttype)
            throws RuntimeException {
        addReserved(reserved, value, ttype, null);
    }

    /**
     * Configures the given string to be used as a reserved word.
     *
     * @param reserved
     *            the reserved word. This must be a valid identifier which has
     *            not already been configured as a reserved word.
     * @param value
     *            the ID associated with the reserved word.
     * @param ttype
     *            the token type of the reserved word.
     * @param action
     *            the action triggered by this token.
     * @exception RuntimeException
     *                if the string is invalid.
     */
    public void addReserved(String reserved, Object value, int ttype,
            Action action)
            throws RuntimeException {
        if (!isIdentifier(reserved)) {
            handler.handle(null, ERR_VALUE, reserved, this);
            return;
        }
        else if (this.reserved.containsKey(reserved)) {
            handler.handle(null, ERR_REDEFINED_SYMBOL, reserved, this);
            return;
        }
        this.reserved.put(reserved, new TokenConfig(ttype, value, action));
    }

    /**
     * Removes a reserved word.
     *
     * @param reserved
     *            The reserved word that should be removed.
     * @return <code>true</code> if the removal was accomplished, and
     *         <code>false</code> otherwise (if the string did not constitute a
     *         reserved word).
     */
    public boolean removeReserved(String reserved) {
        return this.reserved.remove(reserved) != null;
    }

    /**
     * Convenience method. This is equivalent to {@link #clearReserved(int)
     * clearReserved(TT_ANY)}.
     *
     * @return The number of reserved words that were removed.
     */
    public final int clearReserved() {
        return clearReserved(TT_ANY);
    }

    /**
     * Clears all reserved words having the given token type.
     *
     * @param ttype
     *            The type of reserved words which should be removed. The token
     *            type {@link #TT_ANY} removes all reserved words.
     * @return The number of reserved words that were removed.
     */
    public int clearReserved(int ttype) {
        int result;

        if (ttype == TT_ANY) {
            result = reserved.size();
            reserved.clear();
        }
        else {
            Iterator it = reserved.values().iterator();
            result = 0;
            while (it.hasNext()) {
                if (((TokenConfig)it.next()).ttype() == ttype) {
                    it.remove();
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addSpecialToken(String, int, Object, Action)
     * addSpecialToken(special, ttype, null, null)}.
     *
     * @param special
     *            the special token.
     * @param ttype
     *            the token type of the special token.
     * @exception RuntimeException
     *                if the string is invalid.
     */
    public final void addSpecialToken(String special, int ttype) {
        addSpecialToken(special, ttype, null);
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addSpecialToken(String, int, Object, Action)
     * addSpecialToken(special, ttype, value, null)}.
     *
     * @param special
     *            the special token.
     * @param ttype
     *            the token type of the special token.
     * @param value
     *            the ID associated with the special token.
     * @exception RuntimeException
     *                if the string is invalid.
     */
    public final void addSpecialToken(String special, int ttype, Object value) {
        addSpecialToken(special, ttype, value, null);
    }

    /**
     * Configures the given string to be used as a special token.
     *
     * @param special
     *            the special token.
     * @param ttype
     *            the token type of the special token.
     * @param value
     *            the ID associated with the special token.
     * @param action
     *            the action triggered by this token.
     * @exception RuntimeException
     *                if the string is invalid.
     */
    public void addSpecialToken(String special, int ttype, Object value,
            Action action)
            throws RuntimeException {
        if (!checkSpecialToken(special)) {
            handler.handle(null, ERR_VALUE, special, this);
            return;
        }
        char[] sChars = special.toCharArray();
        if (specialTokens.findToken(sChars, 0) != null) {
            handler.handle(null, ERR_REDEFINED_SYMBOL, special, this);
            return;
        }

        specialTokens.installToken(sChars, 0, ttype, value, action);
    }

    /**
     * Removes a special token.
     *
     * @param special
     *            The special token that should be removed.
     * @return <code>true</code> if the removal was accomplished, and
     *         <code>false</code> otherwise (if the string did not constitute a
     *         special token).
     */
    public boolean removeSpecialToken(String special) {
        TokenChar tchar = specialTokens.findToken(special.toCharArray(), 0);
        if (tchar != null) {
            tchar.remove();
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Convenience method. This is equivalent to {@link #clearSpecialTokens(int)
     * clearSpecialTokens(TT_ANY)}.
     * 
     * @return The number of special tokens that were removed.
     */
    public final int clearSpecialTokens() {
        return clearSpecialTokens(TT_ANY);
    }

    /**
     * Clears all special tokens having the given token type.
     *
     * @param ttype
     *            The type of special tokens which should be removed. The token
     *            type {@link #TT_ANY} removes all special tokens.
     * @return The number of special tokens that were removed.
     */
    public int clearSpecialTokens(int ttype) {
        return specialTokens.removeTokens(ttype);
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addSymbol(String, int, Object, Action) addSymbol(reserved, ttype,
     * null, null)}.
     *
     * @param symbol
     *            the symbol string.
     * @param ttype
     *            the token type of the symbol.
     * @exception RuntimeException
     *                if the symbol is invalid.
     */
    public final void addSymbol(String symbol, int ttype) {
        addSymbol(symbol, ttype, null, null);
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #addSymbol(String, int, Object, Action) addSymbol(reserved, ttype,
     * value, null)}.
     *
     * @param symbol
     *            the symbol string.
     * @param ttype
     *            the token type of the symbol.
     * @param value
     *            the ID associated with the symbol.
     * @exception RuntimeException
     *                if the symbol is invalid.
     */
    public final void addSymbol(String symbol, int ttype, Object value) {
        addSymbol(symbol, ttype, value, null);
    }

    /**
     * Configures the given string to be recognized as a symbol.
     *
     * @param symbol
     *            the symbol string.
     * @param ttype
     *            the token type of the symbol.
     * @param value
     *            the ID associated with the symbol.
     * @param action
     *            the action triggered by this symbol.
     * @exception RuntimeException
     *                if the symbol is invalid.
     */
    public void addSymbol(String symbol, int ttype, Object value, Action action)
            throws RuntimeException {
        if (isIdentifier(symbol)) {
            if (reserved.containsKey(symbol)) {
                handler.handle(null, ERR_REDEFINED_SYMBOL, symbol, this);
                return;
            }
            reserved.put(symbol, new TokenConfig(ttype, value, action));
        }
        else {
            addSpecialToken(symbol, ttype, value, action);
        }
    }

    /**
     * Removes the given symbol from the syntax configuration.
     *
     * @param symbol
     *            The symbol which should be removed.
     * @return <code>true</code> if the removal was accomplished, and
     *         <code>false</code> otherwise (if the symbol was not included in
     *         the syntax configuration).
     */
    public boolean removeSymbol(String symbol) {
        return symbol.length() > 0 &&
                (removeReserved(symbol) || removeSpecialToken(symbol));
    }

    /**
     * Removes the given symbol iff it has the given token type.
     *
     * @param symbol
     *            The symbol which should be removed
     * @param ttype
     *            The token type which the symbol must be configured to have.
     * @return <code>true</code> iff the symbol did have the specified token
     *         type and thus was removed.
     */
    public boolean removeSymbol(String symbol, int ttype) {
        if (isIdentifier(symbol)) {
            TokenConfig cfg = (TokenConfig)reserved.get(symbol);
            if (cfg == null || cfg.ttype() != ttype) {
                return false;
            }
            reserved.remove(symbol);
            return true;
        }
        else {
            TokenChar tc = specialTokens.findToken(symbol.toCharArray(), 0);
            if (tc == null || tc.ttype != ttype) {
                return false;
            }
            tc.remove();
            return true;
        }
    }

    /**
     * Returns the current configuration of the given symbol.
     *
     * @param symbol
     *            the symbol
     * @return <code>null</code>, if the given symbol has no configuration.
     *         Otherwise, the elements of the return value contain the
     *         configured token type, value and action, respectively.
     */
    public Object[] getConfig(String symbol) {
        TokenConfig cfg = (TokenConfig)reserved.get(symbol);
        if (cfg == null) {
            cfg = (TokenConfig)metaSymbols.get(symbol);
        }
        if (cfg != null) {
            return new Object[] { new Integer(cfg.ttype()), cfg.value(),
                    cfg.action() };
        }

        Object obj;
        if ((obj = macros.get(symbol)) != null) {
            return new Object[] { new Integer(TT_MACRO), obj.toString(), null };
        }

        TokenChar tc = specialTokens.findToken(symbol.toCharArray(), 0);
        if (tc != null) {
            return new Object[] { new Integer(tc.ttype), tc.value, tc.action };
        }
        else {
            return null;
        }
    }

    /**
     * Convenience method. This is equivalent to {@link #clearSymbols(int)
     * clearSymbols(TT_ANY)}.
     * 
     * @return The number of symbols that were removed.
     */
    public final int clearSymbols() {
        return clearSymbols(TT_ANY);
    }

    /**
     * Clears all symbols having the given token type.
     *
     * @param ttype
     *            The type of symbols which should be removed. The token type
     *            {@link #TT_ANY} removes all symbols.
     * @return The number of symbols that were removed.
     */
    public int clearSymbols(int ttype) {
        return clearReserved(ttype) + clearSpecialTokens(ttype);
    }

    /**
     * Configures the given string to be used as a meta symbol.
     *
     * @param name
     *            the name of the meta symbol. This must be a valid identifier
     *            which does not constitute a meta symbol.
     * @param value
     *            the value associated with the meta symbol.
     * @param action
     *            the action triggered by this symbol.
     * @exception RuntimeException
     *                if the given name is invalid.
     */
    public void addMetaSymbol(String name, Object value, Action action)
            throws RuntimeException {
        if (!isIdentifier(name) || metaSymbols.containsKey(name)) {
            handler.handle(null,
                    metaSymbols.containsKey(name) ? ERR_REDEFINED_SYMBOL
                            : ERR_VALUE,
                    name, this);
            return;
        }
        metaSymbols.put(name, new TokenConfig(TT_META, value, action));
    }

    /**
     * Removes the given meta symbols.
     *
     * @param name
     *            The name of the meta symbol which should be removed.
     * @return <code>true</code> if the removal was accomplished, and
     *         <code>false</code> otherwise (if the meta symbol was not
     *         defined).
     */
    public boolean removeMetaSymbol(String name) {
        return metaSymbols.remove(name) != null;
    }

    /**
     * Clears all meta symbols from the syntax configuration.
     *
     * @return The number of meta symbols that were removed.
     */
    public int clearMetaSymbols() {
        int result = metaSymbols.size();
        metaSymbols.clear();
        return result;
    }

    /**
     * Adds a macro to the syntax configuration.
     *
     * @param name
     *            The macro name. This must be a valid identifier.
     * @param contents
     *            The macro contents.
     * @exception RuntimeException
     *                If the macro name is not an identifier or if the macro has
     *                already been defined.
     */
    public void addMacro(String name, final String contents)
            throws RuntimeException {
        if (!isIdentifier(name) || reserved.containsKey(name)) {
            handler.handle(null, ERR_VALUE, name, this);
            return;
        }
        else if (macros.containsKey(name)) {
            handler.handle(null, ERR_REDEFINED_SYMBOL, name, this);
            return;
        }
        macros.put(name, (contents.length() == 0) ? DEFINED
                : new StringReader(contents) {
                    @Override
                    public String toString() {
                        return contents;
                    }
                });
    }

    /**
     * Removes the given macro.
     *
     * @param name
     *            The reserved word that should be removed.
     * @return <code>true</code> if the removal was accomplished, and
     *         <code>false</code> otherwise (if the string did not constitute a
     *         macro name).
     */
    public boolean removeMacro(String name) {
        return macros.remove(name) != null;
    }

    public int clearMacros() {
        int result = macros.size();
        macros.clear();
        return result;
    }

    public void clearTokenNames() {
        tokenTypeNames.clear();
    }

    public void setTokenName(int ttype, String name) {
        if (name == null) {
            tokenTypeNames.remove(new Integer(ttype));
        }
        else {
            tokenTypeNames.put(new Integer(ttype), name);
        }
    }

    /**
     * Includes the given input.
     *
     * @param s
     *            The string which should be read next by the lexer.
     * @param checkDuplicate
     *            Whether it should be verified that the string is not already
     *            being included.
     * @exception RuntimeException
     *                If an I/O error occurs, or if <code>checkDuplicate</code>
     *                was <code>true</code> and the lexer already has included
     *                the exact same input using
     *                <code>checkDuplicate == true</code>.
     */
    public void include(String s, boolean checkDuplicate)
            throws RuntimeException {
        insertInput(new StringReader(s), checkDuplicate ? s : null);
    }

    /**
     * Includes the given input.
     *
     * @param r
     *            The <code>Reader</code> which should be read next by the
     *            lexer.
     * @exception RuntimeException
     *                If an I/O error occurs, or if the reader is already being
     *                read.
     */
    public void include(Reader r) throws RuntimeException {
        insertInput(r, r);
    }

    /**
     * Includes the contents of the given URL.
     *
     * @param url
     *            The <code>URL</code> whose contents should be read next by the
     *            lexer.
     * @exception RuntimeException
     *                If an I/O error occurs, or if the URL is already being
     *                read.
     */
    public void include(URL url) throws RuntimeException {
        if (obs != null) {
            if (!obs.includeStart(url)) {
                return;
            }
        }

        InputStreamReader r;
        try {
            r = new InputStreamReader(url.openStream());
        }
        catch (IOException e) {
            handler.handle(e, ERR_URL, url, this);
            return;
        }
        insertInput(r, url);
    }

    /**
     * Removes the top-level input.
     *
     * @return <code>true</code> if the top-level input was removed, and
     *         <code>false</code> otherwise (if no input is present).
     * @exception RuntimeException
     *                If an I/O error occurs.
     */
    public boolean exit() throws RuntimeException {
        if (in.size() <= 0) {
            return false;
        }

        // pop the next input, and restore the read-ahead characters from that
        // input
        try {
            Object id = in.getActiveID();
            boolean isMacro = id instanceof String;
            in.pop(!isMacro);
            char[] recycled = (char[])recycleStack.pop();

            resetInputBuffer();
            stack.insert(recycled);
            tokenStart = tokenEnd = 0;
            next = -1;

            // notify the observer and create debug texts.
            if (id instanceof URL) {
                if (obs != null) {
                    obs.includeEnd((URL)id);
                }
                restoreLocation();
                if (debug != null) {
                    addDebugText("Finished URL " + id + ". Popped \"" +
                            StringUtils.toJavaString(recycled) + "\"\n");
                }
            }
            else if (debug != null) {
                if (isMacro) {
                    addDebugText("Finished macro " + id + ". Popped \"" +
                            StringUtils.toJavaString(recycled) + "\"\n");
                }
                else {
                    addDebugText("Finished reader " + id + ". Popped \"" +
                            StringUtils.toJavaString(recycled) + "\"\n");
                }
            }
            return true;
        }
        catch (IOException e) {
            handler.handle(e, ERR_RUNTIME, null, this);
            return false;
        }
    }

    /**
     * @return whether the argument is a whitespace character.
     * @param c
     *            the character
     */
    public boolean isWhitespace(int c) {
        return isType(c, CT_WHITESPACE);
    }

    /**
     * @return whether the argument is digit.
     * @param c
     *            the character
     */
    public boolean isDigit(int c) {
        return isType(c, CT_NUM);
    }

    /**
     * @return whether the argument is a letter (word character).
     * @param c
     *            the character
     */
    public boolean isLetter(int c) {
        return isType(c, CT_WORD);
    }

    /**
     * @return whether the argument has any special attributes or not.
     * @param c
     *            the character
     */
    public boolean isOrdinary(int c) {
        return (typeOf(c) & CT_NORMAL_MASK) == 0;
    }

    /**
     * @return whether the argument is a quote character.
     * @param c
     *            the character
     */
    public boolean isQuote(int c) {
        return isType(c, CT_QUOTE);
    }

    /**
     * @return whether the argument is a character quote character.
     * @param c
     *            the character
     */
    public boolean isCharQuote(int c) {
        return isType(c, CT_CHARQUOTE);
    }

    /**
     * @return whether the argument is a raw quote character.
     * @param c
     *            the character
     */
    public boolean isRawQuote(int c) {
        return isType(c, CT_RAWQUOTE);
    }

    /**
     * @return whether the argument is a comment marker.
     * @param s
     *            the token
     */
    public boolean isComment(String s) {
        TokenChar tc = specialTokens.findToken(s.toCharArray(), 0);
        if (tc != null) {
            return (tc.ttype == TT_LINECOMMENT
                    || tc.ttype == TT_MULTICOMMENTSTART);
        }
        else {
            return false;
        }
    }

    /**
     * @return whether the argument is a valid identifier.
     * @param id
     *            the identifer
     */
    public boolean isIdentifier(String id) {
        if (id == null || id.length() < 1) {
            return false;
        }

        char[] chars = id.toCharArray();

        if (enforceJavaID && !(Character.isJavaIdentifierStart(chars[0]))) {
            return false;
        }
        else if (!enforceJavaID && !isType(chars[0], CT_WORD)) {
            return false;
        }

        for (int i = chars.length - 1; i >= 0;) {
            if (!isType(chars[i--], CT_WORD)) {
                return false;
            }
        }

        return true;
    }

    /**
     * @return whether the argument string is valid as a special token.
     * @param s
     *            the token
     */
    public boolean checkSpecialToken(String s) {
        if (s == null || s.length() < 1 ||
                (typeOf(s.charAt(0)) & CT_NORMAL_MASK) != 0) {
            return false;
        }

        char[] chars = s.toCharArray();
        for (int i = chars.length - 1; i >= 1;) {
            if (isType(chars[i--], CT_WHITESPACE)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Convenience method. This is equivalent to {@link #isReserved(String, int)
     * isReserved(s, TT_ANY)}.
     *
     * @param s
     *            The reserved word
     * @return <code>true</code> if the argument <code>s</code> is a reserved
     *         word of the given token type.
     */
    public final boolean isReserved(String s) {
        return isReserved(s, TT_ANY);
    }

    /**
     * Determines whether the given string is a reserved word of the given token
     * type.
     *
     * @param s
     *            The reserved word
     * @param ttype
     *            The token type. {@link #TT_ANY} may be used if the token type
     *            is insignificant.
     * @return <code>true</code> if the argument <code>s</code> is a reserved
     *         word of the given token type.
     */
    public boolean isReserved(String s, int ttype) {
        if (ttype == TT_ANY) {
            return reserved.containsKey(s);
        }
        TokenConfig tc;
        return (tc = (TokenConfig)reserved.get(s)) != null
                && tc.ttype() == ttype;
    }

    /**
     * Convenience method. This is equivalent to
     * {@link #isSpecialToken(String, int) isSpecialToken(s, TT_ANY)}.
     *
     * @param s
     *            The special token
     * @return <code>true</code> if the argument <code>s</code> is a reserved
     *         word of the given token type.
     */
    public final boolean isSpecialToken(String s) {
        return isSpecialToken(s, TT_ANY);
    }

    /**
     * Determines whether the given string is a special token of the given token
     * type.
     *
     * @param s
     *            The special token
     * @param ttype
     *            The token type. {@link #TT_ANY} may be used if the token type
     *            is insignificant.
     * @return <code>true</code> if the argument <code>s</code> is a reserved
     *         word of the given token type.
     */
    public boolean isSpecialToken(String s, int ttype) {
        if (ttype == TT_ANY) {
            return reserved.containsKey(s);
        }
        TokenConfig tc;
        return (tc = (TokenConfig)reserved.get(s)) != null
                && tc.ttype() == ttype;
    }

    /**
     * Retrieves the token type of the argument symbol.
     *
     * @param symbol
     *            The symbol whose token type should be retreived.
     * @return The token type of the symbol, if any. The {@link #TT_NOTHING}
     *         value indicates that the symbol has not been defined.
     */
    public int getTTypeOf(String symbol) {
        TokenConfig cfg = (TokenConfig)reserved.get(symbol);
        if (cfg != null) {
            return cfg.ttype();
        }
        TokenChar ch = specialTokens.findToken(symbol.toCharArray(), 0);
        if (ch != null) {
            return ch.ttype;
        }
        else {
            return TT_NOTHING;
        }
    }

    /**
     * Retrieves the ID associated with the argument symbol.
     *
     * @param symbol
     *            The symbol whose ID should be retreived.
     * @return The ID associated with the symbol, if any. The <code>null</code>
     *         value indicates that the symbol is not defined or that no ID has
     *         been associated with the symbol.
     */
    public Object getIDFor(String symbol) {
        TokenConfig cfg = (TokenConfig)reserved.get(symbol);
        if (cfg != null) {
            return cfg.value();
        }
        TokenChar ch = specialTokens.findToken(symbol.toCharArray(), 0);
        if (ch != null) {
            return ch.value;
        }
        else {
            return null;
        }
    }

    /**
     * Convenience method. This is equivalent to {@link #getMacroText(String)
     * getMacroText(s) != null}.
     * 
     * @param s
     *            the macro
     * @return The contents of the specified macro, or <code>null</code> if no
     *         such macro exists.
     */
    public final boolean isMacro(String s) {
        return getMacroText(s) != null;
    }

    /**
     * Retreives the contents of the specified macro.
     *
     * @param s
     *            The name of the macro whose contents should be retreived.
     * @return The contents of the specified macro, or <code>null</code> if no
     *         such macro exists.
     */
    public String getMacroText(String s) {
        Object obj = macros.get(s);
        return (obj == null) ? null : obj.toString();
    }

    /**
     * Configures the observer component which receives notifications about the
     * current <code>URL</code> being parsed.
     *
     * @param obs
     *            The new observer for this lexer. The <code>null</code> value
     *            may be used, if no observation is necessary.
     * @return The observer previously associated with this lexer.
     */
    public Observer setObserver(Observer obs) {
        Observer result = this.obs;
        this.obs = obs;
        return result;
    }

    /**
     * @return the observer for this lexer.
     */
    public Observer getObserver() {
        return obs;
    }

    /**
     * Configures the error handler for this lexer.
     *
     * @param h
     *            the handler
     */
    public void setHandler(Handler h) {
        handler = h == null ? DEFAULT_HANDLER : h;
    }

    /**
     * @return the error handler for this lexer.
     */
    public Handler getHandler() {
        return handler;
    }

    /**
     * Returns the line number of the {@link #currentLocation() current
     * location}.
     *
     * @return The current line number (base 1).
     */
    public int lineno() {
        return lineno;
    }

    /**
     * Returns the <code>URL</code> of the {@link #currentLocation() current
     * location}.
     *
     * @return The current <code>URL</code>, or <code>null</code> if no
     *         <code>URL</code> has been {@link #include(URL) included}.
     */
    public URL url() {
        return loc;
    }

    /**
     * Returns the <i>current location</i> of the lexer.
     *
     * @return The location in input at which the lexer is currently positioned.
     */
    public Location currentLocation() {
        return new Location(loc, lineno, getInputChars(),
                new int[] { tokenStart, tokenEnd });
    }

    /**
     * Returns the character following the current token.
     *
     * @return The character immediately following the last character of the
     *         current token. The return value is negative iff no more input is
     *         available.
     */
    public int peek() {
        return (next < 0) ? read() : next;
    }

    /**
     * Removes the character following the current token by reading one
     * character from the input.
     *
     * @return The character that was removed.
     * @exception RuntimeException
     *                If an I/O error occurs.
     */
    public int removeNext() throws RuntimeException {
        int result = (next < 0) ? read() : next;
        read();
        return result;
    }

    /**
     * Convenience method. This is equivalent to {@link #resync(boolean)
     * resync(true)}.
     *
     * @return The input character that was discarded.
     */
    public int resync() {
        return resync(true);
    }

    /**
     * Resynchronizes the input, such that any input already read will be
     * discarded as well as any token having been {@link #pushBack() pushed
     * back}. This will force the lexer to begin reading upon the next call to
     * {@link #nextToken()}.
     *
     * @param clearLocation
     *            whether the current location buffer should be cleared.
     * @return The input character that was discarded.
     */
    public int resync(boolean clearLocation) {
        int result = next;
        next = nextRaw = -1;
        escapeCount = 0;
        clearInput = clearLocation;
        resynced = true;
        pushedBack = false;
        ttype = TT_NOTHING;
        nval = 0.0;
        oval = sval = null;
        return result;
    }

    /**
     * Discards the current input token, but preserves any characters already
     * read.
     */
    public void dropToken() {
        if (next >= 0) {
            stack.push((char)next);
        }
        next = nextRaw = -1;
        escapeCount = 0;
        resynced = true;
        pushedBack = false;
        ttype = TT_NOTHING;
        nval = 0.0;
        oval = sval = null;
    }

    /**
     * Changes the current line number.
     *
     * @param lineno
     *            the new number of the current line
     */
    public void setLineno(int lineno) {
        this.lineno = lineno;
    }

    /**
     * Clears the entire lexer input. Every contained <code>Reader</code> will
     * be closed, and the input discarded.
     *
     * @exception RuntimeException
     *                If an I/O error occurs. Every <code>Reader</code> is
     *                guaranteed to be closed, though.
     */
    public void clearInput() throws RuntimeException {
        locationStack.clear();
        recycleStack.clear();
        stack.clear();
        lineno = 1;
        loc = null;
        resync();
        try {
            in.closeAll();
        }
        catch (IOException e) {
            handler.handle(e, ERR_IO, null, this);
            return;
        }
    }

    /**
     * @return a <code>Recovery</code> view of this lexer.
     */
    public Recovery asRecovery() {
        return rec;
    }

    /**
     * Set the debug writer, and return the old one
     *
     * @param w
     *            the debug writer
     * @return the previous debug writer
     */
    public Writer setDebugWriter(Writer w) {
        Writer result = debug;
        debug = w;
        return result;
    }

    /**
     * @return the current debug writer
     */
    public Writer getDebugWriter() {
        return debug;
    }

    // output the given text to the debug writer
    private void addDebugText(String s) {
        if (debug != null) {
            try {
                debug.write(s);
                debug.flush();
            }
            catch (IOException e) {
            }
        }
    }

    // create a debug entry containing the current token and location
    private String createDebugEntry() {
        return createDebugEntry("", "");
    }

    private String createDebugEntry(String start, String end) {
        StringBuffer result = new StringBuffer(start);

        result.append(toString());
        StringUtils.expandRight(result, ' ', 60);
        result.append("'" + StringUtils.toJavaChar((char)next) + "'");
        StringUtils.expandRight(result, ' ', 70);
        result.append('[');
        result.append(StringUtils.expandLeft("" + lineno, ' ', 5));
        result.append("]   ");
        result.append(getInputChars());
        result.append('\n');
        result.append(StringUtils.fill(' ', 80 + tokenStart));
        result.append(StringUtils.fill('^', tokenEnd - tokenStart + 1));
        result.append(end);
        result.append('\n');
        return result.toString();
    }

    /**
     * This hook is called every time a new line is encountered by the lexer.
     */
    protected void newLine() {
        lineno++;
    }

    /**
     * This method is used to retrieve the next input character from the input
     * stream. All input is processed using this method.
     *
     * @return the next character of input, or a negative number on EOF.
     * @exception RuntimeException
     *                If an I/O error occurs. An invalid unicode escape will
     *                also cause an exception at this point.
     */
    protected int read() throws RuntimeException {
        nextC(uuDecode);
        return next;
    }

    // save the current input location
    private void switchToLocation(URL loc) {
        locationStack.push(new Object[] { new Integer(lineno), this.loc });
        this.loc = loc;
        lineno = 1;
    }

    // restore a previously saved input location
    private void restoreLocation() {
        Object[] l = (Object[])locationStack.pop();
        lineno = ((Integer)l[0]).intValue();
        loc = (URL)l[1];
    }

    // insert the given reader using the specified key. key == input indicates
    // a general reader include, whereas a string or URL indicates a macro or
    // URL include, respectively.
    private void insertInput(Reader input, Object key) {
        if (input == null) {
            throw new NullPointerException();
        }

        // try to insert the input reader. key == input indicates that input is
        // neither
        // a macro nor an include, and will thus not be subject to circularity
        // checks
        try {
            in.push(input, key, 1);
        }
        catch (IllegalStateException e) {
            if (key instanceof String) {
                handler.handle(e, ERR_CIRCULAR_MACRO, key, this);
                return;
            }
            else {
                handler.handle(e, ERR_CIRCULAR_INCLUDE, key, this);
                return;
            }
        }

        // switch location, if the key is an URL
        if (key instanceof URL) {
            switchToLocation((URL)key);
        }

        // save the read-ahead data (which has already been removed from the
        // input) then force input to be taken from the new reader
        if (next >= 0) {
            stack.push((char)next);
        }
        stack.push('\0'); // insert an extra whitespace to enforce separation
        recycleStack.push(stack.getChars());
        stack.clear();
        resync();

        // reset the reader, if it is a macro
        if (key instanceof String) {
            try {
                input.reset();
            }
            catch (IOException e) {
            }
        }

        // debug
        if (debug != null) {
            String entry;
            if (input == key) {
                entry = "Inserted the input stream " + input;
            }
            else if (key instanceof String) {
                entry = "Macro " + key + ": \"" +
                        StringUtils.toJavaString(input.toString()) + '\"';
            }
            else {
                entry = "#include " + key; // key instanceof URL holds
            }
            addDebugText(entry + '\n');
        }
    }

    // add an input character to the buffer
    private void addInputChar(int c) {
        if (c <= 0 || c == '\r' || c == '\n') {
            return;
        }
        if (inputIdx >= inputBuffer.length) {
            inputBuffer = ArrayUtils.enlarge(inputBuffer, 0);
        }
        inputBuffer[inputIdx++] = (c <= ' ') ? ' ' : (char)c;
    }

    // get the input characters
    private String getInputChars() {
        return String.copyValueOf(inputBuffer, 0, inputIdx);
    }

    // reset the input string buffer
    private void resetInputBuffer() {
        inputIdx = 0;
    }

    // convert a unicode digit to integer
    private int uuDigit(int digit) throws RuntimeException {
        int result = (digit < 0) ? -1 : Character.digit((char)digit, 16);
        if (result < 0) {
            handler.handle(null, ERR_UNICODE, null, this);
            throw new NumberFormatException(); // terminate the uudecoding
        }
        return result;
    }

    // read a char and handle EOF (pop input)
    private int readNext() {
        int result;

        try {
            if ((result = in.read()) >= 0) {
                // normal char - return it
                return result;
            }
            else if (in.size() == 0) {
                // empty stack - return EOF
                return -1;
            }

            // pop the input stack
            exit();
        }
        catch (IOException e) {
            // An exception at this point means that the current input
            // stream is malfunctioning. Remove it.
            exit();
            handler.handle(e, ERR_READ, null, this);
        }

        // return a TT_EOF token if required
        if (useEOF) {
            return TT_EOF;
        }

        // return the next char, using the stack first. Disable uudecoding on
        // the
        // next pass
        return nextC(false);
    }

    // get the next input char and uudecode, if required
    private int nextC(boolean decode) throws RuntimeException {
        if (stack.size() > 0) {
            // retreive decoded look-ahead chars first
            tokenEnd++;
            return next = stack.pop();
        }

        // save the current input location, and get the next raw input char
        int tEnd = tokenEnd;
        if (nextRaw >= 0) {
            // use the lookahead from previous run
            next = nextRaw;
            nextRaw = -1;
        }
        else {
            // read a fresh input char
            next = readNext();
        }
        if (next == '\\') {
            escapeCount++;
            if (decode && (escapeCount % 2) == 1) {
                // check for unicode escapes only if decoding and an odd number
                // of
                // contiguous escape chars has been encountered
                if ((next = readNext()) == 'u') {
                    while ((next = readNext()) == 'u') {
                        ;
                    }
                    try {
                        int result = (((((uuDigit(next) << 4)
                                | uuDigit(readNext())) << 4)
                                | uuDigit(readNext())) << 4)
                                | uuDigit(readNext());
                        addInputChar(next = result);
                    }
                    catch (NumberFormatException e) {
                        next = 0;
                    }
                    tokenStart = tokenEnd = tEnd + 1;
                    escapeCount = 0;
                }
                else {
                    nextRaw = next;
                    next = '\\';
                } // not a unicode escape
            } // uudecode
        } // escape
        else {
            // reset the number of contiguous escape chars
            escapeCount = 0;
        }

        if (next >= 0) {
            // add the character to the location buffer
            addInputChar(next);
            tokenEnd++;
        }

        return next;
    }

    // add a token character to the buffer
    private void addTokenChar(char c) {
        if (tokenIdx >= tokenBuffer.length) {
            tokenBuffer = ArrayUtils.enlarge(tokenBuffer, 0);
        }
        tokenBuffer[tokenIdx++] = c;
    }

    // get the token characters stored in the buffer
    private String getTokenChars() {
        String result = String.copyValueOf(tokenBuffer, 0, tokenIdx);
        return result;
    }

    // reset the token string buffer
    private void resetTokenBuffer() {
        tokenIdx = 0;
    }

    // parse a character in a quoted string
    private char parseQuoteChar() throws RuntimeException {
        int c = next;

        if (c == '\\') {
            c = read();
            if ((c >= '0') && (c <= '7')) {
                // octal escape - save the first digit (must be between 0 and 3)
                int firstDigit = c;

                // use c to save the char value
                c = c - '0';
                read();
                if ((next >= '0') && (next <= '7')) {
                    c = (c << 3) | (next - '0');
                    read();
                    if ((next >= '0') && (next <= '7') && (firstDigit <= '3')) {
                        c = (c << 3) | (next - '0');
                        read();
                    }
                }
                return (char)c;
            } // octal

            switch (c) {
            case 'a':
                c = 0x7;
                break;
            case 'b':
                c = '\b';
                break;
            case 'f':
                c = '\f';
                break;
            case 'n':
                c = '\n';
                break;
            case 'r':
                c = '\r';
                break;
            case 't':
                c = '\t';
                break;
            case 'v':
                c = 0xB;
                break;
            case '\\':
            case '\'':
            case '"':
                break;
            case '\r':
            case '\n':
                // skip any blanks until the next backslash
                if (skipBlanks(false) == '\\') {
                    resetInputBuffer();
                    read();
                    return parseQuoteChar();
                }
                // fall through and create an exception:
            default:
                try {
                    read();
                    handler.handle(null, ERR_ESCAPE, null, this);
                }
                catch (Throwable t) {
                }
                return '\0';
            }
            read(); // maintain the look-ahead
            return (char)c;
        }
        else {
            if ((c == '\r') || (c == '\n') || (c < 0)) {
                handler.handle(null, ERR_QUOTE, null, this);
                throw new IllegalArgumentException(); // stop parsing the quoted
                                                      // string
            }
            read();
            return (char)c;
        }
    }

    private int skipBlanks() {
        return skipBlanks(useEOL);
    }

    // skip any consecutive whitespace in the input. eolIsSignificant determines
    // whether EOL is a blank.
    private int skipBlanks(boolean eolIsSignificant) {
        int c, ct;

        if (resynced) {
            resynced = false;
            read();
        }

        c = next;
        ct = typeOf(c);

        while ((ct & CT_WHITESPACE) != 0) {
            if (c == '\r') {
                newLine();
                if ((c = read()) == '\n') {
                    resynced = true; // don't read further ahead
                }
                if (eolIsSignificant) {
                    // return an EOL token
                    clearInput = true;
                    return TT_EOL;
                }
                else {
                    // read the next char, if this hasn't been done already, and
                    // clear
                    // the location buffer
                    resetInputBuffer();
                    if (resynced) {
                        resynced = false;
                        c = read();
                    }
                    else {
                        addInputChar(next);
                    }
                }
            }
            else if (c == '\n') {
                newLine();
                if (eolIsSignificant) {
                    // return an EOL token
                    resynced = true;
                    clearInput = true;
                    return TT_EOL;
                }
                else {
                    // read the next char and clear the location buffer
                    resetInputBuffer();
                    c = read();
                }
            }
            else {
                // read the next char
                c = read();
            }

            if (c < 0) {
                // no more input!
                return TT_EOF;
            }

            // update the type of the next char
            ct = typeOf(c);
        } // while

        return c;
    }

    // return the TokenChar representing the longest token possible from the
    // given lookahead char.
    private TokenChar scanForward(TokenChar root, int ahead) {
        TokenChar inf = null;
        TokenChar nextInf = root.findToken(ahead);

        if (nextInf == null) {
            // not a token char
            return null;
        }

        // scan forward until a character that cannot be part of the token is
        // encountered
        while (nextInf != null) {
            inf = nextInf;
            nextInf = inf.findToken(read());
        }

        // push back the token characters that do not represent a token
        if (next >= 0) {
            stack.push((char)next);
            tokenEnd--;
        }
        next = -1;
        while (inf != root && inf.ttype == TT_NOTHING) {
            stack.push(inf.c);
            tokenEnd--;
            inf = inf.parent;
        }
        if (inf == root) {
            inf = null;
        }

        // read back one char such that one char has been read ahead of the
        // token
        read();
        tokenEnd--;
        return inf;
    }

    // Verify that the current token has the expected token type.
    private void checkToken(int expected) throws RuntimeException {
        if (expected == TT_ANY || ttype == expected) {
            return;
        }

        int ecode;
        Object args = null;
        switch (expected) {
        case TT_EOL:
            ecode = ERR_EXPECTED_EOL;
            break;
        case TT_WORD:
            ecode = ERR_EXPECTED_IDENTIFIER;
            break;
        case TT_OPERATOR:
            ecode = ERR_EXPECTED_OPERATOR;
            break;
        default:
            ecode = ERR_EXPECTED_TOKEN;
            String tokenName = (String)tokenTypeNames
                    .get(new Integer(expected));
            if (tokenName == null) {
                if (expected >= '\0' && expected <= '\uffff') {
                    tokenName = StringUtils.toJavaChar((char)expected);
                }
                else {
                    tokenName = "" + expected;
                }
            }
            args = tokenName;
        }

        handler.handle(null, ecode, args, this);
    }

    public String nextLine() throws RuntimeException {
        // Cannot read the raw token if pushed back
        if (pushedBack) {
            handler.handle(null, ERR_STATE, "", this);
            return null;
        }

        StringBuffer buf = new StringBuffer();
        int c;

        // re-use the read-ahead character, if not resynced
        if (resynced) {
            resynced = false;
            read();
        }

        c = next;

        while (true) {
            if (c == '\r') {
                newLine();
                if ((c = read()) == '\n') {
                    resynced = true; // don't read further ahead
                }
                clearInput = true; // cause the input buffer to clear next
                break;
            }
            else if (c == '\n') {
                newLine();
                resynced = true; // don't read further ahead
                clearInput = true; // cause the input buffer to clear next
                break;
            }
            else if (c < 0) {
                // EOF
                break;
            }
            else {
                // save the next char of the line
                buf.append((char)c);
                c = read();
            }
        }

        return buf.toString();
    }

    public String nextRawToken() throws RuntimeException {
        return nextRawToken(false, false);
    }

    public String nextRawToken(boolean useEOL, boolean allowQuotes)
            throws RuntimeException {
        // Cannot read the raw token if pushed back
        if (pushedBack) {
            handler.handle(null, ERR_STATE, "", this);
            return null;
        }

        // skip leading blanks
        int c = skipBlanks(useEOL);
        if (c == TT_EOF || c == TT_EOL) {
            return null;
        }

        // reset the token position
        tokenStart = tokenEnd = inputIdx - 1 - stack.size();

        // add chars to the token buffer as long as the next char is not
        // a whitespace character
        resetTokenBuffer();
        if (allowQuotes && isType(c, CT_QUOTE)) {
            int endToken = c;
            read();
            while ((next >= 0) && (next != endToken)) {
                try {
                    addTokenChar(parseQuoteChar());
                }
                catch (Exception e) {
                    // terminate the string token on errors
                    break;
                }
            }
            read();
        }
        else {
            while (c >= 0) {
                if (c == '\\') {
                    c = read();
                    if (!isType(c, CT_WHITESPACE)) {
                        addTokenChar('\'');
                    }
                    if (c < 0) {
                        break;
                    }
                }
                else if (isType(c, CT_WHITESPACE)) {
                    break;
                }

                addTokenChar((char)c);
                c = read();
            }
        }
        tokenEnd--;

        nval = 0.0;
        oval = null;
        id = -1;
        ttype = TT_WORD;
        return sval = getTokenChars();
    }

    /**
     * Convenience method. This is equivalent to {@link #nextToken(int, boolean)
     * nextToken(TT_ANY, false)}.
     *
     * @return The token type of the next token, after macros have been
     *         substituted.
     * @exception RuntimeException
     *                If an I/O or lexical error is encountered. Also, if the
     *                expected token type is not matched, this will cause an
     *                exception (a syntax error).
     * @see #parseToken(int, boolean)
     */
    public final int nextToken() throws RuntimeException {
        return nextToken(TT_ANY, false);
    }

    /**
     * Convenience method. This is equivalent to {@link #nextToken(int, boolean)
     * nextToken(expected, false)}.
     *
     * @param expected
     *            The expected (required) token type, or {@link #TT_ANY}, if no
     *            special token type is required.
     * @return The token type of the next token, after macros have been
     *         substituted.
     * @exception RuntimeException
     *                If an I/O or lexical error is encountered. Also, if the
     *                expected token type is not matched, this will cause an
     *                exception (a syntax error).
     * @see #parseToken(int, boolean)
     */
    public final int nextToken(int expected) throws RuntimeException {
        return nextToken(expected, false);
    }

    /**
     * Parses the next token from the input, and automatically performs macro
     * substitution / word quotes.
     *
     * @param expected
     *            The expected (required) token type, or {@link #TT_ANY}, if no
     *            special token type is required.
     * @param allowAllIdentifiers
     *            if <code>true</code>, then all identifiers (excecpt macro
     *            names) will result in {@link #TT_WORD} regardless of the
     *            current configuration of that identifier.
     * @return The token type of the next token, after macros have been
     *         substituted.
     * @exception RuntimeException
     *                If an I/O or lexical error is encountered. Also, if the
     *                expected token type is not matched, this will cause an
     *                exception (a syntax error).
     * @see #parseToken(int, boolean)
     */
    public int nextToken(int expected, boolean allowAllIdentifiers)
            throws RuntimeException {
        boolean decode = !pushedBack;
        parseToken0(false, allowAllIdentifiers);

        if (decode && !rawEscape) {
            StringReader macro;
            if (ttype == TT_WORD &&
                    (macro = (StringReader)macros.get(sval)) != null
                    && macro != DEFINED) {
                // substitute the macro, if it is defined
                insertInput(macro, sval);
                return nextToken(expected, allowAllIdentifiers);
            }

            if (debug != null) {
                addDebugText(createDebugEntry());
            }
        }

        checkToken(expected);
        return ttype;
    }

    /**
     * Parses the next token from the input and stores the result as the current
     * token.
     *
     * @param expected
     *            The expected (required) token type, or {@link #TT_ANY}, if no
     *            special token type is required.
     * @param allowAllIdentifiers
     *            whether to skip ID processing for identifiers
     * @return the next token of input
     * @exception RuntimeException
     *                If an I/O or lexical error is encountered. Also, if the
     *                expected token type is not matched, this will cause an
     *                exception (a syntax error).
     * @see #nextToken(int)
     */
    public int parseToken(int expected, boolean allowAllIdentifiers)
            throws RuntimeException {
        parseToken0(false, allowAllIdentifiers);
        if (debug != null) {
            addDebugText(createDebugEntry());
        }
        checkToken(expected);
        return ttype;
    }

    /**
     * Parses the next token from the input and stores the result as the current
     * token. However, no actions will be performed, and numbers will always be
     * be parsed.
     *
     * @return The type of the token which was read
     * @exception RuntimeException
     *                If an I/O or lexical error is encountered.
     */
    public int skipToken() throws RuntimeException {
        parseToken0(true, true);
        if (debug != null) {
            addDebugText(createDebugEntry("SKIP(", ")"));
        }
        return ttype;
    }

    // parse a token without macro processing
    private int parseToken0(boolean skip, boolean skipIDs)
            throws RuntimeException {
        if (pushedBack) {
            // reuse the current token, if it has been pushed back.
            pushedBack = false;
            return ttype;
        }

        int c; // the character to be considered next
        int ct; // the configuration of c

        // reset the token state
        nval = 0.0;
        oval = sval = null;
        id = -1;
        rawEscape = false;
        if (clearInput) {
            clearInput = false;
            resetInputBuffer();
            if (!resynced) {
                addInputChar(next);
            }
        }

        // get the next input character
        if (resynced) {
            resynced = false;
            c = read();
        }
        else {
            if ((c = next) < 0) {
                c = read();
            }
        }

        // check for EOF
        if (c < 0) {
            return ttype = TT_EOF;
        }

        // skip any leading blank chars
        if ((c = skipBlanks()) == TT_EOF || c == TT_EOL) {
            return ttype = c;
        }

        // look up the char config for the next char.
        // Now c contains the next char, and ct the type, which is neither
        // CT_BLANK nor TT_EOF.
        ct = typeOf(c);

        // reset the token position
        tokenStart = tokenEnd = inputIdx - 1 - stack.size();

        /* -- handle meta symbols -- */
        if ((ct & CT_META) == CT_META) {
            read();
            if (next == c) {
                // double-meta character (raw escape)
                read();
                nextRawToken(useEOL, true);
                rawEscape = true;
                return ttype; // TT_WORD
            }
            if (isType(next, CT_WORD) &&
                    (!enforceJavaID
                            || Character.isJavaIdentifierStart((char)next))) {
                // meta command symbol - parse the following identifier
                resetTokenBuffer();

                while (isType(next, CT_WORD)) {
                    addTokenChar((char)next);
                    read();
                }
                tokenEnd--;
                sval = getTokenChars();

                // look up the meta symbol
                if ((oval = metaSymbols.get(sval)) == null) {
                    handler.handle(null, ERR_UNKNOWN_META_SYMBOL, sval, this);
                    return parseToken0(skip, skipIDs);
                }
                else {
                    try {
                        ((TokenConfig)oval).setResult(this, !skip);
                    }
                    catch (RuntimeException e) {
                        handler.handle(e, ERR_RUNTIME, null, this);
                        return parseToken0(skip, skipIDs);
                    }
                    catch (Error e) {
                        handler.handle(e, ERR_RUNTIME, null, this);
                        return parseToken0(skip, skipIDs);
                    }
                }
                return ttype; // = TT_META;
            }
            else {
                // single meta char (meta operator)
                return ttype = c;
            }
        } // CT_META

        /* -- handle numbers -- */
        if ((ct & CT_NUM) != 0) {
            if (skip || parseNumbers) {
                // parse the number using no type preference (integer) and no
                // sign,
                // if numbers should be parsed
                Number n = parseNumber('\0', false);
                oval = n;
                nval = n.doubleValue();
            }
            return ttype = TT_NUMBER;
        } // CT_NUM

        /* -- handle identifiers -- */
        if (((ct & CT_WORD) != 0) &&
                (!enforceJavaID || Character.isJavaIdentifierStart((char)c))) {
            resetTokenBuffer();
            do {
                // add the char to the string buffer
                addTokenChar((char)c);
                c = read();
                // treat EOF as a separator
                ct = (c < 0) ? CT_WHITESPACE : typeOf(c);
            } while ((ct & CT_WORDCHAR) != 0);
            tokenEnd--;

            // convert the buffer to a string
            sval = String.copyValueOf(tokenBuffer, 0, tokenIdx);
            if (!caseSensitive) {
                // convert to lower case, if required
                sval = sval.toLowerCase();
            }

            // check for special words
            if (!skipIDs && (oval = reserved.get(sval)) != null) {
                return ((TokenConfig)oval).setResult(this, !skip);
            }
            else {
                return ttype = TT_WORD;
            }
        } // CT_WORD

        /* -- handle quoted strings -- */
        if ((ct & CT_QUOTE) != 0) {
            ttype = c;
            read();

            int endToken = ttype;
            resetTokenBuffer();

            if ((ct & CT_RAWQUOTE) == CT_RAWQUOTE) {
                // raw quote - copy any char except escaped end chars.
                loop: while (next >= 0) {
                    switch (next) {
                    case '\r':
                        read();
                        newLine();
                        if (next == '\n') {
                            addTokenChar('\n');
                            read();
                        }
                        else {
                            addTokenChar('\r');
                        }
                        continue;
                    case '\n':
                        newLine();
                        addTokenChar('\n');
                        read();
                        continue;
                    case '\\':
                        read();
                        if (next == endToken) {
                            addTokenChar((char)endToken);
                            read();
                        }
                        else {
                            addTokenChar('\\');
                        }
                        continue;
                    default:
                        if (next == endToken) {
                            break loop;
                        }
                    }

                    addTokenChar((char)next);
                    read();
                }
            }
            else {
                // parse a variable-length quoted string
                while ((next >= 0) && (next != endToken)) {
                    try {
                        addTokenChar(parseQuoteChar());
                    }
                    catch (Exception e) {
                        break;
                    }
                }
            }

            // maintain the look-ahead
            read();
            tokenEnd--;
            sval = getTokenChars();

            // check for valid character literals
            if (((ct & CT_CHARQUOTE) == CT_CHARQUOTE) && (sval.length() != 1)) {
                handler.handle(null, ERR_CHARQUOTE, null, this);
                if (sval.length() < 1) {
                    sval = " ";
                }
            }
            return ttype;
        } // CT_QUOTE

        /* -- handle comments and special tokens -- */
        TokenChar inf = scanForward(specialTokens, c);
        if (inf != null) {
            // a comment start character was encountered.
            if (inf.ttype == TT_LINECOMMENT) {
                // skip the rest of the line, and parse a new token
                c = next < 0 ? read() : next;
                do {
                    if ((c == '\n') || (c == '\r') || (c < 0)) {
                        break;
                    }
                    c = read();
                } while (true);
                return parseToken0(skip, skipIDs);
            } // line comment
            else if (inf.ttype != TT_MULTICOMMENTSTART) {
                // retreive the token info and return the token type
                sval = inf.getToken().substring(1);
                oval = inf.value;
                if (oval instanceof Number) {
                    id = ((Number)oval).intValue();
                }
                else {
                    id = -1;
                }
                return ttype = inf.ttype;
            } // special token

            // skip the multiple-line comment
            int nestingDepth = 1;
            read();
            while (nestingDepth > 0) {
                // handle EOL / EOF
                if (next == '\r') {
                    newLine();
                    if (read() == '\n') {
                        read();
                    }
                    // clear the location buffer
                    resetInputBuffer();
                    addInputChar(next);
                    continue;
                }
                else if (next == '\n') {
                    newLine();
                    resetInputBuffer();
                    addInputChar(read());
                    continue;
                }
                else if (next < 0) {
                    handler.handle(null, ERR_COMMENT, null, this);
                    return -1;
                }

                // look for new comment delimiters
                if (nestedComments) {
                    if ((inf = scanForward(startComments, next)) != null) {
                        read();
                        nestingDepth++;
                        continue;
                    }
                }
                if ((inf = scanForward(endComments, next)) != null) {
                    nestingDepth--;
                }
                else {
                    // skip the char, if it is not part of a commment token
                    read();
                }
            } // multiple-line comment
            return parseToken0(skip, skipIDs);
        } // comment | special token
        else {
            // ordinary single-char token - maintain the lookahead and return
            // the
            // character type
            read();
            tokenEnd--;
            return ttype = c;
        }
    }

    /**
     * Pushes the current token back, such that it will be reused upon the next
     * call to {@link #nextToken(int) nextToken()} or
     * {@link #parseToken(int, boolean) parseToken()}.
     */
    public void pushBack() {
        if (!resynced) {
            pushedBack = true;
        }
    }

    /**
     * Parses a number from the input, and represents it using the specified
     * number type. This requires that the next input character is the beginning
     * of a number, which will be the case after {@link #TT_NUMBER} has been
     * returned by {@link #nextToken(int) nextToken()}
     * ({@link #parseToken(int, boolean) parseToken()}) when
     * {@link #parseNumbers(boolean) full number parsing} is used.
     *
     * @param prefFmt
     *            The type suffix character representing the preferred type of
     *            the parsed number. The character <code>'\0'</code> indicates
     *            that no preference is given, such that the resulting type of
     *            the number will be the smallest type that is able to represent
     *            the number. Otherwise, the parsed number will be converted to
     *            the specified type, even if the input contains a type suffix
     *            character.
     * @param negative
     *            Determines whether the number should be treated as having a
     *            preceding unary minus.
     * @return The parsed number.
     * @exception IllegalStateException
     *                If the next input character is not a digit.
     * @exception RuntimeException
     *                If the number format is invalid.
     * @see #setNumberFormat(char, char, char, char, char, char[])
     */
    public Number parseNumber(char prefFmt, boolean negative)
            throws RuntimeException, IllegalStateException {
        if (!isType(next, CT_NUM)) {
            handler.handle(null, ERR_STATE, toString(), this);
            return null;
        }

        int ct;
        boolean hasPoint = false; // whether a decimal point is present
        boolean hasExp = false; // whether an exponent char is present

        // use the token buffer to represent a Java-parsable number
        // corresponding
        // to the parsed number.
        resetTokenBuffer();
        if (negative) {
            addTokenChar('-');
        }
        addTokenChar((char)next);

        ct = typeOf(read());

        if ((ct & CT_HEX) != 0) {
            // hexadecimal number
            resetTokenBuffer();
            while (isType(read(), CT_HEXNUM)) {
                addTokenChar((char)next);
            }
            tokenEnd--;

            try {
                Number result = ConvertUtils.fromHexString(getTokenChars(),
                        numberType(prefFmt));
                nval = result.doubleValue();
                oval = result;
                ttype = TT_NUMBER;
                addDebugText(createDebugEntry());
                return result;
            }
            catch (NumberFormatException e) {
                handler.handle(e, ERR_NUMFORMAT, null, this);
                return null;
            }
        }
        else {
            // add the first digit sequence
            if ((ct & CT_NUM) != 0) {
                do {
                    addTokenChar((char)next);
                } while (isType(read(), CT_NUM));
            }

            if (isType(next, CT_POINT)) {
                // check whether the decimal point is the beginning of a special
                // token
                int point = next;
                boolean isNum = isType(read(), CT_NUM);
                if (isNum || isType(next, CT_EXP)) {
                    hasPoint = true;
                    addTokenChar('.');
                    if (isNum) {
                        do {
                            addTokenChar((char)next);
                        } while (isType(read(), CT_NUM));
                    }
                }
                else {
                    stack.push((char)next);
                    next = point;
                    tokenEnd--;
                }
            } // hasPoint

            if (isType(next, CT_EXP)) {
                hasExp = true;
                addTokenChar('e');
                boolean needDigit = true;

                ct = typeOf(read());
                if ((ct & CT_POS) != 0) {
                    addTokenChar('+');
                }
                else if ((ct & CT_NEG) != 0) {
                    addTokenChar('-');
                }
                else if ((ct & CT_NUM) == 0) {
                    needDigit = false;
                }
                else {
                    addTokenChar((char)next);
                    needDigit = false;
                }

                if (needDigit) {
                    if (!isType(read(), CT_NUM)) {
                        handler.handle(null, ERR_NUMFORMAT, null, this);
                        return null;
                    }
                    addTokenChar((char)next);
                }
                while (isType(read(), CT_NUM)) {
                    addTokenChar((char)next);
                }
            } // hasExp
        }

        // get the suffix char, if any.
        if (isType(next, CT_SUFFIX)) {
            prefFmt = (char)next;
            read();
        }
        tokenEnd--;

        // Convert and return the result
        try {
            Number result = ConvertUtils.valueOf(getTokenChars(),
                    numberType(prefFmt), hasPoint, hasExp);
            nval = result.doubleValue();
            oval = result;
            addDebugText(createDebugEntry());
            return result;
        }
        catch (NumberFormatException e) {
            handler.handle(e, ERR_NUMFORMAT, null, this);
            return null;
        }
    }

    // format the value of oval for toString()
    private final String fmtOval() {
        return "" + oval
                + ((oval instanceof Number)
                        ? " - " + StringUtils.toHexString(id, 8)
                        : "");
    }

    /**
     * Returns a string representation of this lexer.
     *
     * @return The current token and preceding input formatted as a string.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(pushedBack ? "P " : resynced ? "R " : "  ");

        if (ttype >= 0 && ttype != TT_EOL) {
            result.append('\'').append(StringUtils.toJavaChar((char)ttype))
                    .append("' (").append(ttype).append(')');
            if (isType(ttype, CT_QUOTE)) {
                StringUtils.expandRight(result, ' ', 18);
                result.append((char)ttype)
                        .append(StringUtils.toJavaString(sval))
                        .append((char)ttype);
            }
        }
        else {
            String s;
            String val = null;
            switch (ttype) {
            case TT_EOL:
                s = "TT_EOL";
                break;
            case TT_EOF:
                s = "TT_EOF";
                break;
            case TT_NUMBER:
                s = "TT_NUMBER";
                if (parseNumbers) {
                    val = "" + nval;
                }
                else {
                    if (oval == null) {
                        val = "" + null;
                    }
                    else {
                        val = "" + oval + " : " + oval.getClass().getName();
                    }
                }
                break;
            case TT_WORD:
                s = "TT_WORD";
                val = sval;
                break;
            case TT_NOTHING:
                s = "TT_NOTHING";
                break;
            case TT_META:
                s = "TT_META";
                val = sval + ", " + fmtOval();
                break;
            case TT_RESERVED:
                s = "TT_RESERVED";
                val = sval + ", " + fmtOval();
                break;
            case TT_LITERAL:
                s = "TT_LITERAL";
                val = sval + ", " + fmtOval();
                break;
            case TT_OPERATOR:
                s = "TT_OPERATOR";
                val = sval + ", " + fmtOval();
                break;
            case TT_TYPE:
                s = "TT_TYPE";
                val = sval + ", " + fmtOval();
                break;
            default:
                s = "TT_[" + ttype + "]";
                val = sval + ", " + fmtOval();
            }
            result.append(s);
            if (val != null) {
                StringUtils.expandRight(result, ' ', 18);
                result.append(val);
            }
        }

        return result.toString();
    }

    /**
     * Resets all I/O related internal state variables.
     */
    public void reset() {
        try {
            in.closeAll();
        }
        catch (IOException e) {
        }
        in.resetTotal();

        Writer w = debug;
        debug = null;
        if (w != null) {
            try {
                w.close();
            }
            catch (IOException e) {
            }
        }
        obs = null;
        resync();
    }

    /**
     * @return the current input being used
     */
    public StackedReader getInput() {
        return in;
    }
}
