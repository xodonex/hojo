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

/**
 * Contains lexer error code definitions.
 */
public interface LexerErrors {

    int ERR_REDEFINED_SYMBOL = 0;
    int ERR_VALUE = 1;
    int ERR_STATE = 2;
    int ERR_IO = 10;
    int ERR_READ = 11;
    int ERR_EOF = 12;
    int ERR_RUNTIME = 20;
    int ERR_URL = 30;
    int ERR_CIRCULAR_MACRO = 31;
    int ERR_CIRCULAR_INCLUDE = 32;
    int ERR_UNKNOWN_META_SYMBOL = 33;
    int ERR_UNICODE = 40;
    int ERR_ESCAPE = 41;
    int ERR_QUOTE = 42;
    int ERR_CHARQUOTE = 43;
    int ERR_COMMENT = 45;
    int ERR_NUMFORMAT = 46;
    int ERR_EXPECTED_EOL = 50,
            ERR_EXPECTED_IDENTIFIER = 51,
            ERR_EXPECTED_OPERATOR = 52,
            ERR_EXPECTED_TOKEN = 53;

}
