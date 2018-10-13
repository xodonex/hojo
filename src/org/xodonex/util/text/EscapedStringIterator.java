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
package org.xodonex.util.text;

/**
 * A simple character iterator for an escaped string.
 */
public class EscapedStringIterator {

    /**
     * Indicates an escaped character.
     */
    public final static int ESCAPED = 0x100000;

    // the escape character
    private int _escape;

    // the last character seen
    private int _c = 0;

    // push-back indicator
    private boolean _pushedBack = false;

    // the string over which to iterate
    private String _s;

    // the length of, and current index to, the iterated string
    private int _len, _idx = 0;

    /**
     * Creates a new EscapedStringIterator.
     *
     * @param s
     *            the string over which to iterate.
     * @param escapeChar
     *            the escape character. A negative value means that no character
     *            will be treated as an escape character.
     */
    public EscapedStringIterator(String s, int escapeChar) {
        _s = s;
        _len = s.length();
        _escape = escapeChar;
    }

    /**
     * @return the last character to be seen
     */
    public int last() {
        return _c;
    }

    /**
     * @return the next character of the sequence. The return value is
     *         <ul>
     *         <li>negative, iff no more characters exist in the sequence
     *         <li>larger than or equal to {@link #ESCAPED} iff the next
     *         character has been escaped. If so, {@link #ESCAPED} should be
     *         subtracted from the result to get the character which was
     *         escaped.
     *         <li>between 0 and {@link #ESCAPED} iff the next character exists
     *         and isn't escaped.
     *         </ul>
     */
    public int next() {
        if (_c < 0) {
            // at end-of-string
            return -1;
        }
        else if (_pushedBack) {
            _pushedBack = false;
            return _c;
        }

        if (_idx >= _len) {
            return _c = -1;
        }

        _c = _s.charAt(_idx++);
        if (_c == _escape) {
            if (_idx < _len) {
                // read the escaped char and indicate the escape
                _c = _s.charAt(_idx++) + ESCAPED;
            }
            else {
                // missing escaped char. Treat as an escaped escape, i.e. do
                // nothing
            }
        }

        return _c;
    }

    /**
     * Push back the sequence, such that the next call to {@link #next()} will
     * not change the current position.
     */
    public void pushBack() {
        _pushedBack = true;
    }

}
