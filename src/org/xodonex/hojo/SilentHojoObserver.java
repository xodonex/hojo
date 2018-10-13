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

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;

import org.xodonex.hojo.lang.Statement;
import org.xodonex.util.os.OsInterface;
import org.xodonex.util.text.lexer.GenericLexer;

/**
 * A {@link HojoObserver} implementation which does a minimum of actions.
 */
public class SilentHojoObserver implements HojoObserver {

    private int errorCount = 0;
    private int warningCount = 0;

    private PrintWriter err, warn;

    public SilentHojoObserver() {
        this(null, null);
    }

    public SilentHojoObserver(PrintWriter err, PrintWriter warn) {
        this.err = err == null ? OsInterface.NULL : err;
        this.warn = warn == null ? OsInterface.NULL : warn;
    }

    @Override
    public boolean started(Reader in) {
        return true;
    }

    @Override
    public boolean includeStart(URL url) {
        return true;
    }

    @Override
    public void includeEnd(URL url) {
    }

    @Override
    public void handleWarning(HojoException w) {
        warn.println(w + "\n");
        warn.flush();
        warningCount++;
    }

    @Override
    public boolean handleError(HojoException e) {
        err.println(e + "\n");
        err.flush();
        errorCount++;
        return true;
    }

    @Override
    public boolean doRecovery(GenericLexer.Recovery rec) {
        return false;
    }

    @Override
    public void recovered() {
    }

    @Override
    public void commandResult() {
    }

    @Override
    public void commandResult(Object result) {
    }

    @Override
    public boolean pragmaDirective(String id, Object value) {
        return false;
    }

    @Override
    public int listDirectives(Collection names, Collection types,
            Collection comments) {
        return 0;
    }

    @Override
    public void commandRead() {
    }

    @Override
    public boolean commandExecute(Statement stm) {
        return true;
    }

    @Override
    public boolean commandExecute(String[] cmds) {
        return true;
    }

    @Override
    public void finished() {
    }

    @Override
    public Writer getOutputWriter() {
        return OsInterface.NULL;
    }

    @Override
    public PrintWriter getErrorWriter() {
        return err;
    }

    @Override
    public PrintWriter getWarningWriter() {
        return warn;
    }

    @Override
    public void reset() {
        errorCount = warningCount = 0;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningCount() {
        return warningCount;
    }
}
