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
package org.xodonex.util.ui.comp;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceAdapter;

/**
 * @author Henrik Lauritzen
 */
public abstract class DragInitiator extends DragSourceAdapter implements
        DragGestureListener, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public DragInitiator() {
        this(null, DnDConstants.ACTION_COPY_OR_MOVE);
    }

    public DragInitiator(Component comp) {
        this(comp, DnDConstants.ACTION_COPY_OR_MOVE);
    }

    public DragInitiator(Component comp, int type) {
        _dragSource = new DragSource();

        if (comp != null) {
            setup(comp, type);
        }
    }

    public void setup(Component c, int type) {
        _comp = c;
        _dragSource.createDefaultDragGestureRecognizer(c, type, this);
        _type = type;
    }

    protected abstract Transferable createCommand(DragGestureEvent e);

    protected Component getComponent() {
        return _comp;
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent e) {
        // start a new drag operation
        _dragSource.startDrag(e, getCursorFor(_type), createCommand(e), this);
    }

    public static java.awt.Cursor getCursorFor(int type) {
        switch (type) {
        case DnDConstants.ACTION_COPY_OR_MOVE:
            // fall through
        case DnDConstants.ACTION_COPY:
            return DragSource.DefaultCopyDrop;
        case DnDConstants.ACTION_LINK:
            return DragSource.DefaultLinkDrop;
        case DnDConstants.ACTION_MOVE:
            return DragSource.DefaultMoveDrop;
        default:
            return DragSource.DefaultMoveNoDrop;
        }
    }

    private DragSource _dragSource;
    private Component _comp;
    private int _type;

}
