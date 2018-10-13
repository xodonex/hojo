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
package org.xodonex.util.beans;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.xodonex.util.beans.edit.PropertyEditorComponent;
import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.layout.ColumnLayout;

/**
 * A PropertySheet is a GUI component which allows customization of a Java Bean.
 *
 * @author Henrik Lauritzen
 */
public class PropertySheet extends JSplitPane {// implements Scrollable {

    private static final long serialVersionUID = 1L;

    // the bean object
    @SuppressWarnings("unused")
    private Object _bean = null;

    // the property descriptors
    private PropertyDescriptor[] _propertyDescriptors = new PropertyDescriptor[0];

    // the property name components
    private NameComponent[] _nameComponents = new NameComponent[0];

    // the property editor components
    private PropertyEditorComponent[] _editorComponents = new PropertyEditorComponent[0];

    // the currently active editor component (if any)
    private PropertyEditorComponent _active = null;

    // the owner frame
    private JFrame _ownerFrame;

    // the GUI resource
    private GuiResource _rsrc;

    // the left and right views
    private JPanel _left, _right;

    // the preferred component height
    private int _preferredHeight;

    public PropertySheet() {
        this(null, null);
    }

    public PropertySheet(GuiResource rsrc) {
        this(rsrc == null ? null : rsrc.getMainFrame(), rsrc);
    }

    public PropertySheet(JFrame ownerFrame, GuiResource rsrc) {
        super(HORIZONTAL_SPLIT);

        _ownerFrame = ownerFrame;
        _rsrc = rsrc == null ? GuiResource.getDefaultInstance() : rsrc;

        JButton dummy = new JButton("M");
        Dimension d = dummy.getPreferredSize();
        _preferredHeight = d.height;

        // setViewportView(_split = new Split(
        setLeftComponent(_left = new JPanel(new ColumnLayout(0, 0, d.height)));
        setRightComponent(
                _right = new JPanel(new ColumnLayout(0, 0, d.height)));
        _right.setBorder(new javax.swing.border.EmptyBorder(0, 5, 0, 5));

        setInputVerifier(new InputVerifier() {
            @Override
            public boolean verify(JComponent input) {
                return stopEditing();
            }
        });

    }

    public void setBean(Object bean) throws IntrospectionException {
        if (bean == null) {
            setBean(null, null);
        }
        else {
            setBean(bean, BeanUtils.createPropertyDescriptors(bean));
        }
    }

    public void setBean(Object bean, Collection descriptors)
            throws IntrospectionException {

        if (bean == null) {
            // empty the pane
            _propertyDescriptors = new PropertyDescriptor[0];
        }
        else {
            // build a list of all properties and their names.
            _propertyDescriptors = (PropertyDescriptor[])descriptors.toArray(
                    new PropertyDescriptor[descriptors.size()]);
        }

        int size = _propertyDescriptors.length;

        // build the editor and name components
        _editorComponents = new PropertyEditorComponent[size];
        _nameComponents = new NameComponent[size];

        for (int i = size - 1; i >= 0; i--) {
            PropertyEditorComponent ec = _editorComponents[i] = new PropertyEditorComponent(
                    this, bean,
                    _propertyDescriptors[i]);
            (_nameComponents[i] = new NameComponent(_propertyDescriptors[i],
                    ec)).addMouseListener(ec.getPopupMouseListener());
        }

        // add the components to the view
        _left.removeAll();
        _right.removeAll();
        for (int i = 0; i < size; i++) {
            _left.add(_nameComponents[i]);
            _right.add(_editorComponents[i]);
        }

        // Set up hotkeys for the properties
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();
        for (int i = 0, j = 0; i < _propertyDescriptors.length; i++) {
            if (_nameComponents[i].isReadOnly()) {
                continue;
            }

            char shortcut = getShortcutChar(j++);
            if (shortcut <= 'Z') {
                _nameComponents[i].setText("" + shortcut + ".  " +
                        _nameComponents[i].getText());

                String name = "hotkey" + shortcut;
                im.put(KeyStroke.getKeyStroke(shortcut, InputEvent.ALT_MASK),
                        name);
                am.put(name, new BeginEdit(_editorComponents[i]));
            }
        }

        // set the split location
        Insets insets = _left.getInsets();
        setDividerLocation(_left.getPreferredSize().width +
                insets.left + insets.right + 2);
        setDividerSize(2);
    }

    public JFrame getOwnerFrame() {
        return _ownerFrame;
    }

    public GuiResource getResource() {
        return _rsrc;
    }

    public void refreshView() {
        for (int i = 0; i < _editorComponents.length; i++) {
            PropertyEditorComponent pec = _editorComponents[i];
            if (pec == _active) {
                pec.refreshEditor();
            }
            else {
                pec.refreshView();
            }
        }
    }

    public int getComponentHeight() {
        return _preferredHeight;
    }

    public boolean isEditing() {
        return _active != null;
    }

    public void cancelEditing() {
        if (_active == null) {
            return;
        }

        _active.deactivate(true);
        _active = null;
    }

    /**
     * Apply any pending changes and stop editing.
     *
     * @return true iff the changes could be applied.
     */
    public boolean stopEditing() {
        if (_active == null) {
            return true;
        }

        try {
            if (!_active.deactivate(false)) {
                return false;
            }
        }
        catch (Throwable t) {
            return false;
        }

        _active = null;
        return true;
    }

    // n.b: check that pec is contained in the sheet and is editable
    public boolean startEditing(PropertyEditorComponent pec) {
        if (pec == null || !stopEditing()) {
            return false;
        }

        (_active = pec).activate();
        return true;
    }

    /*
     * public java.awt.Dimension getPreferredScrollableViewportSize() { return
     * getPreferredSize(); }
     *
     *
     * public int getScrollableBlockIncrement(java.awt.Rectangle r, int
     * orientation, int direction) { return 16; }
     *
     *
     * public boolean getScrollableTracksViewportHeight() { return false; }
     *
     *
     * public boolean getScrollableTracksViewportWidth() { return false; }
     *
     *
     * public int getScrollableUnitIncrement(java.awt.Rectangle r, int
     * orientation, int direction) { return 16; }
     */

    // calculate a shortcut character
    private static char getShortcutChar(int i) {
        if (i < 0) {
            throw new IllegalArgumentException();
        }
        else if (i <= 8) {
            return (char)('1' + i);
        }
        else if (i == 9) {
            return '0';
        }
        else {
            return (char)('A' + i - 10);
        }
    }

    /*
     * // calculate the shortcut from a character private static int
     * getShortcutNumber(char c) { return c <= '9' && c >= '0' ? c - '0' : c >=
     * 'A' ? c - 'A' : -1; }
     */

    private class NameComponent extends JButton {

        private static final long serialVersionUID = 1L;

        private boolean _readOnly;

        NameComponent(PropertyDescriptor pd,
                final PropertyEditorComponent editorComponent) {
            super(pd.getDisplayName());
            setToolTipText(pd.getShortDescription());
            setHorizontalAlignment(SwingConstants.LEFT);

            _readOnly = ((pd instanceof IndexedPropertyDescriptor)
                    ? ((IndexedPropertyDescriptor)pd).getIndexedWriteMethod()
                    : pd.getWriteMethod()) == null;
            if (_readOnly) {
                setEnabled(false);
            }
            else {
                addActionListener(new BeginEdit(editorComponent));
                addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (_active != editorComponent) {
                            stopEditing();
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                    }
                });
            }
        }

        public boolean isReadOnly() {
            return _readOnly;
        }

    }

    private class BeginEdit extends AbstractAction {
        private static final long serialVersionUID = 1L;

        private PropertyEditorComponent _c;

        BeginEdit(PropertyEditorComponent c) {
            _c = c;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (_active == _c) {
                // deactivate only
                stopEditing();
                return;
            }

            if (!stopEditing()) {
                // can't stop editing
                return;
            }

            // start editing the new component
            startEditing(_c);
        }
    }

}
