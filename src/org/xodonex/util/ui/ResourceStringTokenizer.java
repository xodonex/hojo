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
package org.xodonex.util.ui;

import org.xodonex.util.text.EscapedStringIterator;

/**
 * A tokenizer used to decode composite resource strings
 *
 * @author Henrik Lauritzen
 */
class ResourceStringTokenizer {

    /**
     * End-of-string token type
     */
    public static final int TT_EOS = -1;

    /**
     * Word token type.
     */
    public static final int TT_WORD = -3;

    /**
     * Separator token type
     */
    public static final int TT_SEP = -10;

    /**
     * Left-parenthesis token type
     */
    public static final int TT_LPAR = -11;

    /**
     * Right-parenthesis token type
     */
    public static final int TT_RPAR = -12;

    /**
     * The last token type returned by {@link #nextToken(int)}
     */
    public int ttype = TT_SEP;

    /**
     * The contents of the word token, if {@link #ttype} == {@link #TT_WORD}.
     */
    public String sval = null;

    /**
     * The mnemonic character, if {@link #ttype} == {@link #TT_WORD} and the
     * specified mnemonic escape character was used in {@link #nextToken(int)}
     * (the value is negative if not)
     */
    public int mnemonic = -1;

    /**
     * Constructs a new ResourceStringTokenizer
     *
     * @param s
     *            the string to be tokenized
     * @param escape
     *            the escape character which may be used to turn off the special
     *            significance of the separator ('|'), left parenthesis ('['),
     *            right parenthesis (']'), and mnemonic indicator ('&amp;'), as
     *            well as the escape character itself
     */
    public ResourceStringTokenizer(String s, char escape) {
        _it = new EscapedStringIterator(s, escape);
        _buf = new StringBuffer(s.length());
    }

    /**
     * @return the token type of the next token.
     * @param mnemonicEscape
     *            the character used as a mnemonic escape character.
     */
    public int nextToken(int mnemonicEscape) {
        // pushed back?
        if (_pushedBack) {
            _pushedBack = false;
            return ttype;
        }

        // clear the current state
        sval = null;
        mnemonic = -1;

        // ignore all leading spaces
        int c;
        while ((c = _it.next()) <= ' ') {
            if (c < 0) {
                return ttype = TT_EOS;
            }
        }

        // special token ?
        switch (c) {
        case '[':
            return ttype = TT_LPAR;
        case ']':
            return ttype = TT_RPAR;
        case '|':
            return ttype = TT_SEP;
        }

        // identifier : parse the contents
        _buf.setLength(0);
        loop: while (true) {
            switch (c) {
            case -1:
            case '[':
            case ']':
            case '|':
                // token was terminated - stop parsing
                _it.pushBack();
                break loop;
            }

            if (c >= EscapedStringIterator.ESCAPED) {
                // escaped character - add directly
                _buf.append((char)(c - EscapedStringIterator.ESCAPED));
            }
            else if (c == mnemonicEscape) {
                // escape char for a mnemonic character - read the next
                // character and save it
                c = _it.next();
                if (c >= 0) {
                    // ignore the mnemonic at EOS; otherwise, de-escape
                    // and save the uppercase mnemonic
                    if (c >= EscapedStringIterator.ESCAPED) {
                        c -= EscapedStringIterator.ESCAPED;
                    }
                    _buf.append((char)c);
                    mnemonic = Character.toUpperCase((char)c);
                }
            }
            else {
                // ordinary char
                _buf.append((char)c);
            }

            // read the next char
            c = _it.next();
        } // loop

        // return the token
        sval = _buf.toString().trim();
        return ttype = TT_WORD;
    }

    /**
     * Push back the input such that the next call to {@link #nextToken()} will
     * not parse a new token
     */
    public void pushBack() {
        _pushedBack = true;
    }

    @Override
    public String toString() {
        switch (ttype) {
        case TT_EOS:
            return "EOS";
        case TT_SEP:
            return "SEP";
        case TT_LPAR:
            return "LPAR";
        case TT_RPAR:
            return "RPAR";
        default:
            return "TT_WORD(" + org.xodonex.util.StringUtils.toJavaString(sval)
                    +
                    (mnemonic >= 0 ? " &" + (char)mnemonic : "") + ")";
        }
    }

    // for character iteration
    private EscapedStringIterator _it;

    // termporary storage for tokens
    private StringBuffer _buf;

    // pushed back indicator
    private boolean _pushedBack = false;

}
