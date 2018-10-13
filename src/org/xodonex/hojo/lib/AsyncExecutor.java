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
package org.xodonex.hojo.lib;

import org.xodonex.hojo.lang.Function;

public class AsyncExecutor extends Thread {

    private Function fv;
    private Object[] args;
    private boolean finished = false;
    private Object returnValue;
    private Exception errorCode;

    private static String printName(Function fv, Object[] args) {
        return fv.toString() + "(" +
                org.xodonex.util.StringUtils.argumentList2String(args, ", ")
                + ")";
    }

    public AsyncExecutor(Function fv, Object[] args)
            throws IllegalArgumentException {
        super(printName(fv, args));
        this.fv = fv;
        this.args = fv.validateArgs(args);
    }

    @Override
    public void run() {
        try {
            errorCode = null;
            returnValue = fv.invoke(args);
        }
        catch (Exception e) {
            returnValue = null;
            errorCode = e;
        }

        // clean up
        synchronized (this) {
            fv = null;
            args = null;
            finished = true;
            notifyAll();
        }
    }

    public boolean waitFor() throws InterruptedException {
        return waitFor(0);
    }

    public synchronized boolean waitFor(long maxTime)
            throws InterruptedException {
        if (!finished) {
            wait(maxTime);
        }
        return finished;
    }

    public synchronized Object waitAndGetErrorCode()
            throws InterruptedException {
        if (!finished) {
            wait();
        }
        return errorCode;
    }

    public synchronized boolean isFinished() {
        return finished;
    }

    public Object getReturnValue() throws IllegalStateException {
        return getReturnValue(false);
    }

    public synchronized Object getReturnValue(boolean clear)
            throws IllegalStateException {
        if (!finished) {
            throw new IllegalStateException();
        }

        Object result = returnValue;
        if (clear) {
            returnValue = null;
        }
        return result;
    }

    public synchronized Exception getErrorCode(boolean clear)
            throws IllegalStateException {
        if (!finished) {
            throw new IllegalStateException();
        }

        Exception result = errorCode;
        if (clear) {
            errorCode = null;
        }
        return result;
    }

}
