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

import java.net.URL;

import org.xodonex.util.text.lexer.GenericLexer;

/**
 * The component used for lexical analysis of the Hojo language.
 *
 * The lexical analysis is implemented by the {@link GenericLexer}, and this
 * class only adds the handler/observer parts needed by the
 * {@link HojoInterpreter}.
 * 
 * @see GenericLexer
 * @see HojoSyntax
 */
public class HojoLexer extends GenericLexer implements GenericLexer.Handler {

    /**
     * Dummy exception which is rethrown from the handler.
     */
    public final static HojoException ALREADY_HANDLED = new HojoException(null,
            0xffff, null, null);

    private HojoObserver _obs;

    public HojoLexer() {
        super();
        setHandler(this);
    }

    public HojoObserver getHojoObserver() {
        return _obs;
    }

    public void setObserver(final HojoObserver obs) {
        _obs = obs;
        super.setObserver(obs instanceof GenericLexer.Observer
                ? (GenericLexer.Observer)obs
                : new GenericLexer.Observer() {
                    @Override
                    public boolean includeStart(URL url) {
                        return obs.includeStart(url);
                    }

                    @Override
                    public void includeEnd(URL url) {
                        obs.includeEnd(url);
                    }
                });
    }

    @Override
    public void handle(Throwable t, int lexCode, Object value,
            GenericLexer lex) {
        int hojoCode;
        switch (lexCode) {
        case ERR_REDEFINED_SYMBOL:
            hojoCode = HojoException.ERR_REDEFINED_SYMBOL;
            break;
        case ERR_VALUE:
            hojoCode = HojoException.ERR_VALUE;
            break;
        case ERR_STATE:
            hojoCode = HojoException.ERR_STATE;
            break;
        case ERR_IO:
            hojoCode = HojoException.ERR_RUNTIME;
            break;
        case ERR_READ:
            hojoCode = HojoException.ERR_READ;
            break;
        case ERR_EOF:
            hojoCode = HojoException.ERR_EOF;
            break;
        case ERR_RUNTIME:
            hojoCode = HojoException.ERR_RUNTIME;
            break;
        case ERR_URL:
            hojoCode = HojoException.ERR_URL;
            break;
        case ERR_CIRCULAR_MACRO:
            hojoCode = HojoException.ERR_CIRCULAR_MACRO;
            break;
        case ERR_CIRCULAR_INCLUDE:
            hojoCode = HojoException.ERR_CIRCULAR_INCLUDE;
            break;
        case ERR_UNKNOWN_META_SYMBOL:
            hojoCode = HojoException.ERR_UNKNOWN_META_SYMBOL;
            break;
        case ERR_UNICODE:
            hojoCode = HojoException.ERR_UNICODE;
            break;
        case ERR_ESCAPE:
            hojoCode = HojoException.ERR_ESCAPE;
            break;
        case ERR_QUOTE:
            hojoCode = HojoException.ERR_QUOTE;
            break;
        case ERR_CHARQUOTE:
            hojoCode = HojoException.ERR_CHARQUOTE;
            break;
        case ERR_COMMENT:
            hojoCode = HojoException.ERR_COMMENT;
            break;
        case ERR_NUMFORMAT:
            hojoCode = HojoException.ERR_NUMFORMAT;
            break;
        case ERR_EXPECTED_EOL:
            hojoCode = HojoException.ERR_EXPECTED_EOL;
            break;
        case ERR_EXPECTED_IDENTIFIER:
            hojoCode = HojoException.ERR_EXPECTED_IDENTIFIER;
            break;
        case ERR_EXPECTED_OPERATOR:
            hojoCode = HojoException.ERR_EXPECTED_OPERATOR;
            break;
        case ERR_EXPECTED_TOKEN:
            hojoCode = HojoException.ERR_EXPECTED_TOKEN;
            break;
        default:
            hojoCode = HojoException.ERR_RUNTIME;
        }

        HojoException e = new HojoException(t, hojoCode,
                value == null ? null : new String[] { "" + value },
                lex.currentLocation());
        if (_obs != null) {
            _obs.handleError(e);
            throw (RuntimeException)ALREADY_HANDLED.fillInStackTrace();
        }
        else {
            throw e;
        }
    }

    @Override
    public Observer setObserver(Observer obs) {
        _obs = (HojoObserver)obs;
        return super.setObserver(obs);
    }

}
