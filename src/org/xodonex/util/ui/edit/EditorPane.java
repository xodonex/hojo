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
package org.xodonex.util.ui.edit;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JPanel;

import org.xodonex.util.ui.layout.ColumnLayout;

/**
 * A pane for showing an editor component.
 */
public class EditorPane extends JPanel implements EditorConstants {

    private static final long serialVersionUID = 1L;

    public EditorPane(ResourceBundle rsrc) {
        super(new ColumnLayout(4, ColumnLayout.LEFT, -1));
        _rsrc = rsrc;
    }

    public EditorPane(ResourceBundle rsrc, Map data) {
        this(rsrc);
        addEntries(data);
    }

    public Collection addEntries(Map data) {
        Collection result = new ArrayList(data.size());

        for (Iterator i = data.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            String key = (String)e.getKey();

            Object value = e.getValue();
            DataEditor ed;
            int typedesc = EDITOR_TEXT_FIELD;

            if (value instanceof Boolean) {
                ed = new BooleanDataEditor(data, key);
                typedesc = EDITOR_CHECK_BOX;
            }
            else if (value instanceof Number) {
                if (value instanceof Integer) {
                    ed = new IntDataEditor(data, key);
                }
                else {
                    ed = new DoubleDataEditor(data, key);
                }
            }
            else if (value instanceof Date) {
                ed = new DateDataEditor(data, key);
            }
            else if (value instanceof String) {
                String v = (String)value;
                ed = new StringDataEditor(data, key);
                if (v.length() > 40) {
                    typedesc = EDITOR_TEXT_AREA;
                }
            }
            else {
                ed = new ObjectDataEditor(data, key);
            }

            EditorComponent comp = createEntry(ed, typedesc | LABEL_NORTH);
            addComponent(comp.getUI(), false);
            result.add(comp);
        }

        return result;
    }

    public EditorComponent addEntry(DataEditor ed, int typedesc) {
        EditorComponent ec = createEntry(ed, typedesc);
        addComponent(ec.getUI(), false);
        return ec;
    }

    /**
     * Adds a new entry to this pane
     *
     * @param ec
     *            the editor component to be added
     * @param sameRow
     *            if true, the component will be added at the current row;
     *            otherwise it will create a new row.
     */
    public void addEntry(EditorComponent ec, boolean sameRow) {
        addComponent(ec.getUI(), sameRow);
        registerEntry(ec);
    }

    public EditorComponent createEntry(DataEditor ed, int typedesc) {
        return createEntry(ed, typedesc, true);
    }

    /**
     * Create a new data entry
     *
     * @param ed
     *            the data editor which maintains data
     * @param typedesc
     *            determines which kind of user interface should be created. The
     *            value should be a conjunction of the constants defined in
     *            {@link EditorConstants}.
     * @param addListener
     *            whether to add a listener
     * @return the editor component
     */
    public EditorComponent createEntry(DataEditor ed, int typedesc,
            boolean addListener) {
        EditorComponent ec;
        boolean indexed;

        switch (typedesc & MASK_EDITOR) {
        case EDITOR_CHECK_BOX:
            ec = new CheckBoxEditorComponent(ed, _rsrc, typedesc);
            break;
        case EDITOR_COMBO_BOX_EDITABLE:
        case EDITOR_COMBO_BOX:
            boolean editable = (typedesc
                    & MASK_EDITOR) == EDITOR_COMBO_BOX_EDITABLE;
            indexed = Integer.class == ed.getValueClass();
            ec = new ComboBoxEditorComponent(ed, _rsrc, typedesc,
                    editable, indexed);
            break;
        case EDITOR_LIST:
        case EDITOR_LIST_MULTIPLE:
            boolean multiple = (typedesc & MASK_EDITOR) == EDITOR_LIST_MULTIPLE;
            indexed = (multiple ? int[].class : Integer.class) == ed
                    .getValueClass();
            ec = new ListEditorComponent(ed, _rsrc, typedesc,
                    multiple, indexed);
            break;
        case EDITOR_TEXT_AREA:
            ec = new TextAreaEditorComponent(ed, _rsrc, typedesc);
            break;
        case EDITOR_TEXT_FIELD:
            ec = new TextFieldEditorComponent(ed, _rsrc, typedesc);
            break;
        case EDITOR_PASSWORD_FIELD:
            ec = new PasswordEditorComponent(ed, _rsrc, typedesc);
            break;
        default:
            return null;
        }

        if ((typedesc & MASK_STATE) == STATE_DISABLED) {
            ec.enableUI(false);
        }
        if (addListener) {
            ed.addPropertyChangeListener(ec);
        }
        registerEntry(ec);
        return ec;
    }

    /**
     * Add a component to this pane
     *
     * @param comp
     *            the component to be added
     * @param sameRow
     *            if true, the component will be added at the current row;
     *            otherwise, it will create a new row.
     */
    public void addComponent(Component comp, boolean sameRow) {
        RowPanel rp;
        int c = getComponentCount();

        if (c == 0 || !sameRow) {
            rp = new RowPanel();
            rp.add(comp);
            add(rp);
            return;
        }

        rp = (RowPanel)getComponent(c - 1);
        rp.add(comp);
    }

    /**
     * Registers a given entry with this pane, such that it can be retrieved
     * again later
     *
     * @param ec
     *            the editor component to be registered
     * @see #getEntry(String)
     */
    public void registerEntry(EditorComponent ec) {
        _entries.put(ec.getEditor().getID(), ec);
    }

    /**
     * Retrieve a previously {@link #registerEntry(EditorComponent registered)}
     * editor component.
     *
     * @param key
     *            an identification string for the component to be retrieved
     * @return the editor component registered under the key
     */
    public EditorComponent getEntry(String key) {
        return (EditorComponent)_entries.get(key);
    }

    // the resource bundle to be used
    private ResourceBundle _rsrc;

    // a map of the data entries used in this pane
    private Map _entries = new HashMap();

    private static class RowPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        RowPanel() {
            super();
        }
    }
}
