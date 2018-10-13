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

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.MediaTray;

/**
 * @author Henrik Lauritzen
 */
public class PrintInterface {

    public final static MediaTray[] MEDIA_TRAYS = {
            MediaTray.MAIN,
            MediaTray.LARGE_CAPACITY,
            MediaTray.TOP,
            MediaTray.MIDDLE,
            MediaTray.BOTTOM,
            MediaTray.MANUAL,
            MediaTray.SIDE,
            MediaTray.ENVELOPE
    };

    public PrintInterface() {
        this(null, null);
    }

    public PrintInterface(String printerName) {
        this(null, null);
    }

    public PrintInterface(String printerName, DocFlavor flavor) {
        setPreferredPrinterName(printerName);
        _preferredFlavor = flavor;
        _attributes = new HashPrintRequestAttributeSet();
        _service = getPrintService(printerName, _preferredFlavor, _attributes);
    }

    public synchronized String getPreferredPrinterName() {
        return _preferredPrinter;
    }

    public synchronized void setPreferredPrinterName(String printerName) {
        if (printerName == null || printerName.length() == 0) {
            PrintService ps = PrintServiceLookup.lookupDefaultPrintService();
            _preferredPrinter = ps == null ? null : ps.getName();
        }
        else {
            _preferredPrinter = printerName;
        }
    }

    public synchronized DocFlavor getPreferredDocFlavor() {
        return _preferredFlavor;
    }

    public synchronized void setPreferredDocFlavor(DocFlavor df) {
        _preferredFlavor = df;
    }

    public synchronized PrintService getPrintService() {
        return _service;
    }

    public synchronized PrintService showPrintDialog(
            java.awt.GraphicsConfiguration gc, int x, int y) {
        PrintService[] services = getServices(_preferredFlavor);
        PrintService result = null;

        if (services != null && services.length > 0) {
            result = ServiceUI.printDialog(gc, x, y, services,
                    _service, _preferredFlavor, _attributes);
        }

        if (result != null) {
            _service = result;
        }
        return result;
    }

    public synchronized PrintRequestAttributeSet getAttributeSet() {
        return _attributes;
    }

    public static PrintService getPrintService(String printerName,
            DocFlavor flavor, PrintRequestAttributeSet attributes) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor,
                attributes);
        PrintService preferred = null;

        for (int i = 0; i < services.length; i++) {
            if (printerName == null
                    || printerName.equals(services[i].getName())) {
                // matching request (or no specific request) - return the
                // service
                preferred = services[i];
                break;
            }
        }

        return preferred;
        /*
         * PrintService result = ServiceUI.printDialog(null, 0, 0, services,
         * preferred, flavor, attributes == null ? new
         * javax.print.attribute.HashPrintRequestAttributeSet() : attributes);
         *
         * return result;
         */
    }

    public static PrintService[] getServices() {
        return PrintServiceLookup.lookupPrintServices(null, null);
    }

    public static PrintService[] getServices(DocFlavor flavor) {
        return PrintServiceLookup.lookupPrintServices(flavor, null);
    }

    public static String[] getServiceNames() {
        return getServiceNames(null);
    }

    public static String[] getServiceNames(DocFlavor flavor) {
        PrintService[] services = getServices(flavor);
        String[] result = new String[services.length];

        for (int i = 0; i < services.length; i++) {
            result[i] = services[i].getName();
        }

        return result;
    }

    private String _preferredPrinter;
    private DocFlavor _preferredFlavor;
    private PrintRequestAttributeSet _attributes;
    private PrintService _service;

}
