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
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author Henrik Lauritzen
 */
public class MultipleUndoManager {

    protected HashMap documents = new HashMap();
    protected UndoState currentState = null;

    /**
     * This key for {@link Action#putValue(String, Object)} is used to let the
     * undo and redo actions update the enabled state of the given component.
     */
    public final static String ENABLE_COMPONENT = "ENABLE_COMPONENT";

    public final Action UNDO;
    public final Action REDO;

    public MultipleUndoManager() {
        UNDO = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    undo();
                }
                catch (CannotUndoException ex) {
                    ex.printStackTrace();
                    return;
                }
            }

            @Override
            public void setEnabled(boolean b) {
                super.setEnabled(b);
                Component c = (Component)getValue(ENABLE_COMPONENT);
                if (c != null) {
                    c.setEnabled(b);
                }
            }
        };
        REDO = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    redo();
                }
                catch (CannotUndoException ex) {
                    ex.printStackTrace();
                    return;
                }
            }

            @Override
            public void setEnabled(boolean b) {
                super.setEnabled(b);
                Component c = (Component)getValue(ENABLE_COMPONENT);
                if (c != null) {
                    c.setEnabled(b);
                }
            }
        };

        updateActionState();
    }

    public void addDocuments(Collection c, int historySize) {
        Iterator it = c.iterator();
        while (it.hasNext()) {
            addDocument((Document)it.next(), historySize);
        }
    }

    public void addDocument(Document doc, int historySize) {
        if (documents.containsKey(doc)) {
            return;
        }
        documents.put(doc, new UndoState(doc, historySize));
    }

    public Document removeDocument(Document doc) {
        UndoState us = getState(doc);
        if (us != null) {
            if (us == currentState) {
                currentState = null;
                updateActionState();
            }
            return us.getDoc();
        }
        else {
            return null;
        }
    }

    public void activateDocument(Document doc) {
        UndoState us = (doc == null) ? null : getStateThrow(doc);
        currentState = us;
        updateActionState();
    }

    public void undo() throws CannotUndoException {
        if (currentState == null) {
            throw new CannotUndoException();
        }
        currentState.getUndo().undo();
        updateActionState();
    }

    public void redo() throws CannotRedoException {
        if (currentState == null) {
            throw new CannotRedoException();
        }
        currentState.getUndo().redo();
        updateActionState();
    }

    public void setUndoLimit(Document doc, int limit) {
        UndoState us = getStateThrow(doc);
        us.setUndoLimit(limit);
    }

    protected final UndoState getState(Document doc) {
        return (UndoState)documents.get(doc);
    }

    protected final UndoState getStateThrow(Document doc) {
        UndoState us = (UndoState)documents.get(doc);
        if (us == null) {
            throw new IllegalArgumentException(doc.toString());
        }
        return us;
    }

    protected void updateActionState() {
        boolean canUndo, canRedo;

        if (currentState == null) {
            canUndo = canRedo = false;
        }
        else {
            canUndo = currentState.getUndo().canUndo();
            canRedo = currentState.getUndo().canRedo();
        }

        UNDO.setEnabled(canUndo);
        REDO.setEnabled(canRedo);
    }

    private class UndoState {
        Document doc;
        UndoManager undo;

        UndoState(Document doc, int size) {
            this.doc = doc;
            undo = new UndoManager();
            setUndoLimit(size);
            doc.addUndoableEditListener(new UndoableEditListener() {
                @Override
                public void undoableEditHappened(UndoableEditEvent e) {
                    undo.addEdit(e.getEdit());
                    updateActionState();
                }
            });
        }

        void setUndoLimit(int size) {
            undo.setLimit(size);
        }

        Document getDoc() {
            return doc;
        }

        UndoManager getUndo() {
            return undo;
        }
    }
}
