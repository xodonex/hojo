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
package org.xodonex.hojo.util;

import org.xodonex.hojo.HojoException;
import org.xodonex.util.text.lexer.GenericLexer;
import org.xodonex.util.text.lexer.LexerTokens;

/**
 *
 * @author Henrik Lauritzen
 */
public class ClassLoaderAction implements GenericLexer.Action {

    protected String name;
    protected Class cls = null;

    public ClassLoaderAction(String name) {
        this.name = name;
    }

    @Override
    public void invoke(GenericLexer lex) throws HojoException {
        if (cls == null) {
            try {
                cls = Class.forName(name);
            }
            catch (Exception e) {
                throw new HojoException(e, HojoException.ERR_CLASSNAME,
                        new String[] { name }, lex.currentLocation());
            }
        }

        lex.ttype = LexerTokens.TT_TYPE;
        lex.oval = cls;
        lex.id = -1;
    }

    public Object getValue() {
        return (cls == null) ? (Object)name : (Object)cls;
    }
}
