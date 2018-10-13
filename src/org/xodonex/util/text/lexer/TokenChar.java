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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.xodonex.util.ArrayUtils;
import org.xodonex.util.StringUtils;

/**
 * Data structure for special-token lookup. Each node in the tree corresponds to
 * the token consisting of the characters found in the parent chain followed by
 * the character of the node itself.
 */
class TokenChar implements LexerTokens {
    private final static char[] NO_CHARS = {};
    private final static TokenChar[] NO_INFO = {};

    // the parent node
    public TokenChar parent = null;

    // the char which this class represents
    public char c = (char)TT_UNDEFINED;

    // the next possible token chars
    protected char[] next = NO_CHARS;

    // the next possible token char representations (corresponding to the values
    // in next)
    protected TokenChar[] nextInfos = NO_INFO;

    // the token type represented by the token having the string representation
    // corresponding to the reverse sequence this.c, this.parent.c, ...
    public int ttype = GenericLexer.TT_NOTHING;

    // the token value for the represented token
    public Object value = null;

    // the action for the represented token
    public GenericLexer.Action action = null;

    public TokenChar() {
    }

    private TokenChar(TokenChar p, char c) {
        this();
        parent = p;
        this.c = c;
    }

    // root.findToken(s.toCharArray(), 0) returns the TokenChar corresponding to
    // the token s, if root is the root TokenChar
    public TokenChar findToken(char[] cseq, int index) {
        if (index >= cseq.length) {
            return null;
        }

        char nextChar = cseq[index];
        int nIdx;
        if (index < 0 || (nIdx = Arrays.binarySearch(next, nextChar)) < 0) {
            return null;
        }
        if (index == cseq.length - 1) {
            TokenChar tc = nextInfos[nIdx];
            return (tc.ttype == LexerTokens.TT_NOTHING) ? null : tc;
        }
        else {
            return nextInfos[nIdx].findToken(cseq, index + 1);
        }
    }

    // find the info corresponding to the char c
    public TokenChar findToken(int c) {
        int idx;
        return (c < 0 || (idx = Arrays.binarySearch(next, (char)c)) < 0) ? null
                : nextInfos[idx];
    }

    // root.installToken(s.toCharArray(), 0, ttype, value) installs a new token
    // consisting of the string s and having the given ttype and value
    public void installToken(char[] cseq, int index, int ttype, Object value,
            GenericLexer.Action action) {
        char nextChar = cseq[index];
        int nIdx = Arrays.binarySearch(next, nextChar);

        if (index == cseq.length - 1) {
            // install the new info below this node - or update the old one, if
            // one already exists
            if (nIdx >= 0) {
                nextInfos[nIdx].ttype = ttype;
                nextInfos[nIdx].value = value;
            }
            else {
                TokenChar ci = new TokenChar();
                ci.parent = this;
                ci.ttype = ttype;
                ci.value = value;
                ci.action = action;
                ci.c = nextChar;
                nIdx = -(nIdx + 1);
                next = ArrayUtils.insert(next, nIdx, nextChar);
                nextInfos = (TokenChar[])ArrayUtils.insert(nextInfos, nIdx,
                        ci);
            }
        }
        else {
            // install the info in the subtree rooted at nextInfos[nIdx], if
            // such
            // a branch exists. Otherwise, create the branch.
            if (nIdx < 0) {
                nIdx = -(nIdx + 1);
                nextInfos = (TokenChar[])ArrayUtils.insert(
                        nextInfos, nIdx, new TokenChar(this, nextChar));
                next = ArrayUtils.insert(next, nIdx, nextChar);
            }
            nextInfos[nIdx].installToken(cseq, index + 1, ttype, value, action);
        }
    }

    // removes the config represented by this info
    public void remove() {
        if (parent == null) {
            // the root cannot be removed
            return;
        }

        // remove this node from the parent, if no child nodes exist
        if (next.length == 0) {
            int idx = Arrays.binarySearch(parent.next, c);
            if (idx < 0) {
                return;
            }
            parent.next = ArrayUtils.removeRange(parent.next, idx, idx + 1);
            parent.nextInfos = (TokenChar[])ArrayUtils
                    .removeRange(parent.nextInfos, idx, idx + 1);

            // remove the parent, if this is the only child and the parent does
            // not
            // in itself contain a token
            if (parent.next.length == 1
                    && parent.ttype == LexerTokens.TT_NOTHING) {
                parent.next = NO_CHARS;
                parent.nextInfos = NO_INFO;
                parent.remove();
                parent = null;
            }
        }
        else {
            // mark this node as empty, but leave it in the tree
            ttype = LexerTokens.TT_NOTHING;
            value = null;
            action = null;
        }
    }

    protected void findTokens(int ttype, Collection addTo) {
        for (int i = 0; i < next.length; i++) {
            nextInfos[i].findTokens(ttype, addTo);
        }
        if (this.ttype == ttype) {
            addTo.add(this);
        }
    }

    // remove any tokens of the specified type
    public int removeTokens(int ttype) {
        ArrayList toRemove = new ArrayList();
        findTokens(ttype, toRemove);
        int result = toRemove.size();

        Iterator it = toRemove.iterator();
        while (it.hasNext()) {
            ((TokenChar)it.next()).remove();
        }

        return result;
    }

    // clear any references
    public void clear() {
        for (int i = nextInfos.length - 1; i >= 0;) {
            nextInfos[i--].clear();
        }
        nextInfos = NO_INFO;
        next = NO_CHARS;
        parent = null;
        value = null;
    }

    public String getToken() {
        StringBuffer result = new StringBuffer();
        TokenChar tc = this;
        while (tc != null) {
            result.append(tc.c);
            tc = tc.parent;
        }

        return result.reverse().toString();
    }

    @Override
    public String toString() {
        if (parent == null) {
            return "nil";
        }
        else {
            return "Token['" + StringUtils.toJavaChar(c) + "', t=" +
                    ttype + ", v=" + value + ", p=" + parent + "]";
        }
    }

    public String toDebugString() {
        StringBuffer result = new StringBuffer();
        createDebugString(result);
        return result.toString();
    }

    private void createDebugString(StringBuffer result) {
        if (nextInfos.length == 0) {
            result.append(this).append('\n');
        }
        else {
            if (ttype != LexerTokens.TT_NOTHING) {
                result.append(this).append('\n');
            }
            for (int i = 0; i < nextInfos.length; i++) {
                nextInfos[i].createDebugString(result);
            }
        }
    }
}
