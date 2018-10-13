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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * A Transferable which transfers a serialized object
 *
 * @author Henrik Lauritzen
 */
public class SerializedTransferable implements Transferable {

    public final static DataFlavor SERIALIZED_FLAVOR;
    static {
        DataFlavor f;
        try {
            f = new DataFlavor(DataFlavor.javaSerializedObjectMimeType);
        }
        catch (Exception e) {
            f = null;
        }
        SERIALIZED_FLAVOR = f;
    }

    public SerializedTransferable(Object data) throws IOException {
        _data = IoUtils.marshal(data);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
        return SERIALIZED_FLAVOR.equals(dataFlavor);
    }

    @Override
    public Object getTransferData(DataFlavor dataFlavor)
            throws UnsupportedFlavorException, IOException {
        if (!isDataFlavorSupported(dataFlavor)) {
            throw new UnsupportedFlavorException(dataFlavor);
        }
        try {
            return IoUtils.unmarshal(_data);
        }
        catch (ClassNotFoundException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] { SERIALIZED_FLAVOR };
    }

    private byte[] _data;

}
