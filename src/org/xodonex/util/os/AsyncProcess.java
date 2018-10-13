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
package org.xodonex.util.os;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Writer;

public class AsyncProcess extends Thread {

    public final static int PROCESS_KILLED = 0x80000000;
    public final static int PROCESS_NOTSTARTED = 0x80000001;

    private final static String NEWLINE;
    static {
        String s = "\n";
        try {
            s = System.getProperty("line.separator");
        }
        catch (Exception e) {
        }
        NEWLINE = s;
    }

    private Process p;
    private StringBuffer out;
    private AsyncStreamBuffer outBuf;
    private StringBuffer err;
    private AsyncStreamBuffer errBuf;
    private PrintWriter in;

    private boolean isKilled = false;
    private int returnValue = 0;
    private Runnable endObserver;

    private static class AsyncStreamBuffer extends Thread {
        private final int BUFSIZE = 80;

        private StringBuffer buffer;
        private InputStreamReader stream;
        private Writer cc;
        private boolean active = true;

        AsyncStreamBuffer(ThreadGroup tg, String name, StringBuffer buffer,
                InputStreamReader stream, Writer cc) {
            super(tg, name);
            this.buffer = buffer;
            this.stream = stream;
            this.cc = cc;
            start();
        }

        // Clear all internal variables; this will also cause the thread to
        // terminate, if called asynchronously
        private synchronized void kill() {
            if (!active) {
                return;
            }

            buffer = null;
            try {
                stream.close();
            }
            catch (Exception e) {
            }
            stream = null;
            cc = null; // don't close
            active = false;

            notifyAll();
        }

        @Override
        public void run() {
            int data = 0;
            char[] readBuffer = new char[BUFSIZE];

            try {
                while ((data = stream.read(readBuffer)) > 0) {
                    buffer.append(readBuffer, 0, data);
                    if (cc != null) {
                        try {
                            cc.write(readBuffer, 0, data);
                            cc.flush();
                        }
                        catch (Exception e) {
                        }
                    }
                }
            }
            catch (Exception e) {
            }

            kill();
        }

        synchronized void waitFor() {
            if (active) {
                try {
                    wait();
                }
                catch (InterruptedException e) {
                }
            }
        }

    }

    private static String printCmd(String[] cmd) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < cmd.length; i++) {
            result.append(" ").append(cmd[i]);
        }
        return result.toString();
    }

    public AsyncProcess(String cmd) {
        this(cmd, null, null, null);
    }

    public AsyncProcess(String cmd, Writer out, Writer err) {
        this(new String[] { cmd }, null, out, err);
    }

    public AsyncProcess(String[] cmd, String[] env) {
        this(cmd, env, null, null);
    }

    public AsyncProcess(String cmd, String[] env) {
        this(new String[] { cmd }, env, null, null);
    }

    public AsyncProcess(String cmd, String[] env, Writer out, Writer err) {
        this(new String[] { cmd }, env, out, err);
    }

    public AsyncProcess(String[] cmd, String[] env, Writer out, Writer err) {
        super(new ThreadGroup("exec" + printCmd(cmd)), "main");
        ThreadGroup g = getThreadGroup();
        this.out = new StringBuffer();
        this.err = new StringBuffer();

        try {
            p = Runtime.getRuntime().exec(cmd, env);
            outBuf = new AsyncStreamBuffer(g, "out", this.out,
                    new InputStreamReader(p.getInputStream()), out);
            errBuf = new AsyncStreamBuffer(g, "err", this.err,
                    new InputStreamReader(p.getErrorStream()), err);
            in = new PrintWriter(p.getOutputStream());
            this.start();
        }
        catch (Exception e) {
            returnValue = PROCESS_NOTSTARTED;
            this.err.append("" + e);
            isKilled = true;
        }
    }

    public synchronized void closeInput() {
        if (!isKilled) {
            in.close();
        }
    }

    public synchronized boolean input(String cmd) {
        if (isKilled) {
            return false;
        }
        in.print(cmd);
        in.flush();
        return true;
    }

    public synchronized boolean inputln(String cmd) {
        if (isKilled) {
            return false;
        }
        in.print(cmd + NEWLINE);
        in.flush();
        return true;
    }

    public String getOutput() {
        return getOutput(false);
    }

    public String getOutput(boolean resetBuffer) {
        synchronized (out) {
            String result = out.toString();
            if (resetBuffer) {
                out.setLength(0);
            }
            return result;
        }
    }

    public String getErrors() {
        return getErrors(false);
    }

    public String getErrors(boolean resetBuffer) {
        synchronized (err) {
            String result = err.toString();
            if (resetBuffer) {
                err.setLength(0);
            }
            return result;
        }
    }

    public void kill() {
        kill(true);
    }

    private synchronized void kill(boolean killSubs) {
        if (isKilled) {
            this.interrupt();
            return;
        }
        isKilled = true;

        try {
            returnValue = p.exitValue();
        }
        catch (IllegalThreadStateException e) {
            p.destroy();
            returnValue = PROCESS_KILLED;
        }

        if (killSubs) {
            this.interrupt();
            outBuf.interrupt();
            errBuf.interrupt();
        }
        else {
            outBuf.waitFor();
            errBuf.waitFor();
        }

        if (endObserver != null) {
            try {
                endObserver.run();
            }
            catch (Throwable t) {
            }
            endObserver = null;
        }
        notifyAll();
    }

    public synchronized void setEndObserver(Runnable obs) {
        if (isKilled && obs != null) {
            try {
                obs.run();
            }
            catch (Throwable t) {
            }
        }
        else {
            endObserver = obs;
        }
    }

    public synchronized boolean isFinished() {
        return isKilled;
    }

    public synchronized StringBuffer[] waitAndGetOutput()
            throws InterruptedException {
        if (!isKilled) {
            wait();
        }
        return new StringBuffer[] { out, err };
    }

    public synchronized int waitAndGetResult() throws InterruptedException {
        if (!isKilled) {
            wait();
        }
        return returnValue;
    }

    @Override
    public void run() {
        try {
            returnValue = p.waitFor();
        }
        catch (InterruptedException e) {
        }

        kill(false);
    }

}
