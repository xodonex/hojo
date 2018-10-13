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

import java.awt.PrintJob;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * The PrintInterface maintains a printer job and page format.
 *
 * @author Henrik Lauritzen
 */
public class AwtPrintInterface {

    private PrinterJob _job;
    private PageFormat _pf;

    public AwtPrintInterface() {
        this(null, null);
    }

    public AwtPrintInterface(PrintJob job) {
        this(null, null);
    }

    public AwtPrintInterface(PrinterJob job, PageFormat pf) {
        _job = job == null ? PrinterJob.getPrinterJob() : job;

        if (pf == null) {
            pf = _job.defaultPage();
            Paper p = pf.getPaper();
            p.setSize(PageRenderer.mm2point(210),
                    PageRenderer.mm2point(297));
            p.setImageableArea(PageRenderer.mm2point(15),
                    PageRenderer.mm2point(15),
                    PageRenderer.mm2point(180),
                    PageRenderer.mm2point(267));
            pf.setPaper(p);
        }
        _pf = pf;
    }

    /**
     * Allow the user to configure the page format.
     *
     * @return boolean if the user cancelled the dialog.
     */
    public synchronized boolean selectPageFormat() {
        PageFormat old = _pf;

        _pf = _job.pageDialog(_pf);
        return old != _pf;
    }

    /**
     * @return the page format used by this interface.
     */
    public synchronized PageFormat getPageFormat() {
        return _pf;
    }

    /**
     * @return the print job used by this interface.
     */
    public PrinterJob getPrinterJob() {
        return _job;
    }

    /**
     * Print the given data, using the contained print job and page format.
     *
     * @param p
     *            the data which should be printed.
     * @param showPrintDialog
     *            if true, then a print dialog will be shown first.
     * @return false iff a print dialog was shown and cancelled by the user
     * @exception PrinterException
     *                if the printing fails
     */
    public boolean print(Printable p, boolean showPrintDialog)
            throws PrinterException {
        if (p == null) {
            throw new NullPointerException();
        }
        if (showPrintDialog && !_job.printDialog()) {
            return false;
        }

        _job.setPrintable(p, _pf);
        _job.print();
        return true;
    }

}
