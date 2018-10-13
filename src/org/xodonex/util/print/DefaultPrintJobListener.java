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
package org.xodonex.util.print;

import javax.print.DocPrintJob;
import javax.print.attribute.PrintJobAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;

import org.xodonex.util.log.Log;
import org.xodonex.util.log.LogEntry;

/**
 * @author Henrik Lauritzen
 */
public class DefaultPrintJobListener implements PrintJobListener {

    public final static String SOURCE = "PRINT";

    public DefaultPrintJobListener(DocPrintJob job, Log log, Object notify) {
        if ((_job = job) == null) {
            throw new NullPointerException();
        }

        _log = log;
        _notify = notify;
        _finished = false;
    }

    @Override
    public void printJobRequiresAttention(PrintJobEvent evt) {
        logMessage("print.warning.jobRequiresAttention");
    }

    @Override
    public void printDataTransferCompleted(PrintJobEvent evt) {
        logMessage("print.info.dataTransferCompleted");
    }

    @Override
    public void printJobNoMoreEvents(PrintJobEvent evt) {
        if (!_finished) {
            // no events are generated - treat as complete
            printJobCompleted(evt);
        }
        else {
            terminate();
        }
    }

    @Override
    public void printJobFailed(PrintJobEvent evt) {
        logMessage("print.error.jobFailed");
        terminate();
    }

    @Override
    public void printJobCompleted(PrintJobEvent evt) {
        logMessage("print.info.jobCompleted");
        terminate();
    }

    @Override
    public void printJobCanceled(PrintJobEvent evt) {
        logMessage("print.info.jobCanceled");
        terminate();
    }

    private DocPrintJob _job;
    private Log _log;
    private Object _notify;

    private boolean _finished = false;

    private synchronized void terminate() {
        if (!_finished) {
            _finished = true;
            _job.removePrintJobListener(this);
            _job = null;
            _log = null;
            if (_notify == null) {
                return;
            }

            Object n = _notify;
            _notify = null;
            synchronized (n) {
                n.notifyAll();
            }
        }
    }

    private synchronized void logMessage(String key) {
        if (_finished || _log == null || _job == null) {
            return;
        }
        PrintJobAttributeSet aset = _job.getAttributes();
        JobName jn = (JobName)aset.get(JobName.class);

        String details = jn == null ? null : jn.getValue();
        _log.log(new LogEntry(SOURCE, _log.getLocalizedMessage(key), details));
    }

}
