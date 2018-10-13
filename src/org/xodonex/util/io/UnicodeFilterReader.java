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
package org.xodonex.util.io;

import java.io.CharConversionException;
import java.io.EOFException;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class UnicodeFilterReader extends FilterReader {

    public final StringBuffer monBuffer = new StringBuffer();

    private int ahead = -1;
    private int charsRead = 0;

    private boolean doMonitor = false;

    public UnicodeFilterReader(Reader in) {
        super(in);
    }

    public void toggleMonitoring(boolean doMonitor) {
        synchronized (lock) {
            this.doMonitor = doMonitor;
        }
    }

    public void resetCounter() {
        synchronized (lock) {
            charsRead = 0;
        }
    }

    public int getSize() {
        synchronized (lock) {
            return charsRead;
        }
    }

    public int getLastRead() {
        synchronized (lock) {
            int l = monBuffer.length();
            return (l == 0) ? -1 : monBuffer.charAt(l - 1);
        }
    }

    public void resync() {
        synchronized (lock) {
            ahead = -1;
        }
    }

    @Override
    public int read() throws IOException {
        synchronized (lock) {
            int result = ahead;

            ahead = -1;

            if (result >= 0) {
                if (doMonitor) {
                    monBuffer.append((char)result);
                }
                charsRead++;
                return result;
            }

            if ((result = in.read()) == '\\') {
                result = in.read();
                if (result == 'u') {
                    StringBuffer errMsg = new StringBuffer("\\u");
                    while ((result = in.read()) == 'u') {
                        errMsg.append('u');
                    }

                    int charValue = 0;
                    int counter = 0;
                    do {
                        if (result < 0) {
                            throw new EOFException();
                        }

                        errMsg.append((char)result);
                        if ((result = Character.digit((char)result, 16)) < 0) {
                            throw new CharConversionException(
                                    errMsg.toString());
                        }
                        charValue = (charValue << 4) + result;

                        if (++counter == 4) {
                            break;
                        }

                        result = in.read();
                    } while (true);

                    if (doMonitor) {
                        monBuffer.append((char)charValue);
                    }
                    charsRead++;
                    return ((char)charValue);
                }
                else {
                    ahead = result;
                    if (doMonitor) {
                        monBuffer.append('\\');
                    }
                    charsRead++;
                    return '\\';
                }
            }
            else {
                if (doMonitor) {
                    monBuffer.append((char)result);
                }
                charsRead++;
                return result;
            }
        }
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
        int read = 0;
        int c;

        for (int i = off; i < off + len; i++) {
            if ((c = read()) < 0) {
                break;
            }
            read++;
            cbuf[i] = (char)c;
        }

        return ((read == 0) && (len > 0)) ? -1 : read;
    }

}
