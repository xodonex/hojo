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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

/**
 * An EditorComponent which uses a JComboBox as user interface.
 *
 * @author Henrik Lauritzen
 */
public final class ListEditorComponent extends AbstractEditorComponent {

    public ListEditorComponent(DataEditor editor, ResourceBundle rsrc,
            int labelPlacement, boolean multiple, boolean indexed) {
        super(editor);

        _indexed = indexed;
        _multiple = multiple;

        _ui = new JList();
        Object cfg = init(_ui, _ui, rsrc, labelPlacement);

        if (cfg instanceof List) {
            _ui.setModel(
                    new ArrayListModel((_modelData = (List)cfg).toArray()));
        }
        else {
            String[] tags = editor.getTags();
            if (tags != null) {
                _modelData = Arrays.asList(tags);
                _ui.setModel(new ArrayListModel(tags));
            }
        }

        _ui.setSelectionMode(
                _multiple ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
                        : ListSelectionModel.SINGLE_SELECTION);

        _ui.setMaximumSize(_ui.getPreferredSize());
        updateFromEditor();
    }

    @Override
    public void updateFromEditor() {
        DataEditor e = getEditor();
        Object value = e.getValue();
        if (value == null) {
            _ui.clearSelection();
            return;
        }

        if (_multiple) {
            if (_indexed) {
                _ui.setSelectedIndices((int[])value);
            }
            else {
                // fixme
                ListSelectionModel lsm = _ui.getSelectionModel();
                lsm.clearSelection();
                Object[] data = value instanceof Collection
                        ? ((Collection)value).toArray()
                        : (Object[])value;

                for (int i = 0; i < data.length; i++) {
                    int idx = _modelData.indexOf(data[i]);
                    if (idx >= 0) {
                        lsm.addSelectionInterval(idx, idx);
                    }
                }
            }
        }
        else {
            if (_indexed) {
                _ui.setSelectedIndex(((Integer)value).intValue());
            }
            else {
                _ui.setSelectedValue(value, false);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean updateFromUI() {
        DataEditor ed = getEditor();
        Object value;

        if (_multiple) {
            if (_indexed) {
                value = _ui.getSelectedIndices();
            }
            else {
                // TODO: fix deprecation in a backwards-compatible way.
                value = _ui.getSelectedValues();
            }
        }
        else {
            if (_indexed) {
                value = new Integer(_ui.getSelectedIndex());
            }
            else {
                value = _ui.getSelectedValue();
            }
        }

        return ed.trySetValue(value);
    }

    private JList _ui;
    private List _modelData;
    private boolean _indexed, _multiple;

    private static class ArrayListModel implements ListModel {
        Object[] _list;

        ArrayListModel(Object[] list) {
            _list = list;
        }

        @Override
        public void removeListDataListener(
                javax.swing.event.ListDataListener listDataListener) {
            // the model is immutable
        }

        @Override
        public void addListDataListener(
                javax.swing.event.ListDataListener listDataListener) {
            // the model is immutable
        }

        @Override
        public int getSize() {
            return _list.length;
        }

        @Override
        public Object getElementAt(int idx) {
            return _list[idx];
        }
    }

}
