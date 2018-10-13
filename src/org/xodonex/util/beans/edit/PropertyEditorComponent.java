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
package org.xodonex.util.beans.edit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.util.Iterator;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.xodonex.util.ReflectUtils;
import org.xodonex.util.beans.BeanUtils;
import org.xodonex.util.beans.PropertySheet;
import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.window.Display;

/**
 *
 * @author Henrik Lauritzen
 */
public class PropertyEditorComponent extends JSplitPane {

    private static final long serialVersionUID = 1L;

    // quick reference to String.class
    private final static Class STRING_CLASS = String.class;

    private PropertySheet _owner;

    private PropertyViewer _viewer;
    private PropertyModifier _modifier;
    private IndexedPropertySpinner _selector;

    private boolean _isActive;

    private PropertyEditor _edit;
    private Object _bean;
    private PropertyDescriptor _pd;

    private MouseListener _popupMouseListener;

    public PropertyEditorComponent(PropertySheet owner,
            final Object bean, final PropertyDescriptor pd) {
        // super(new ColumnLayout(2, 0, owner.getComponentHeight()));
        super(HORIZONTAL_SPLIT);
        setDividerSize(0);
        setEnabled(false);
        setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        if ((_bean = bean) == null || (_owner = owner) == null ||
                (_pd = pd) == null) {
            throw new NullPointerException();
        }

        boolean indexed = pd instanceof IndexedPropertyDescriptor;
        IndexedPropertyDescriptor ipd = indexed
                ? (IndexedPropertyDescriptor)pd
                : null;

        try {
            // get a suitable property editor
            Class pec = pd.getPropertyEditorClass();
            Class t = pd.getPropertyType();

            if (pec != null) {
                if (t == STRING_CLASS) {
                    _edit = new StringPropertyEditor(pd);
                }
                else {
                    _edit = (PropertyEditor)pec.newInstance();
                }
            }
            else {
                if (indexed) {
                    t = ipd.getIndexedPropertyType();
                    // create the index selector, if any, and set the initial
                    // property value
                    _selector = new IndexedPropertySpinner();
                    setLeftComponent(_selector);
                }
                else {
                    _selector = null;
                    setDividerLocation(0.0);
                }

                if (t != STRING_CLASS) {
                    _edit = PropertyEditorManager.findEditor(t);
                }
                if (_edit == null) {
                    _edit = new StringPropertyEditor(pd);
                }
            }

            // set the initial value of the editor
            try {
                _edit.setValue(BeanUtils.getPropertyValue(bean, pd, 0));
            }
            catch (Exception e) {
                // should be because an indexed property has 0 elements
                if (_selector == null) {
                    e.printStackTrace();
                }
                else {
                    _selector.setMax(-1);
                }
            }

            // get a list of tags (or tag-to-value map) from the descriptor,
            // if present
            Object tags = pd.getValue(BeanUtils.PROPERTY_KEY_TAGS);

            // create the viewer
            boolean isBooleanProperty = t == Boolean.TYPE || t == Boolean.class;

            if (isBooleanProperty) {
                _viewer = new BooleanPropertyViewer(_edit);
            }
            else if (_edit.isPaintable()) {
                _viewer = new PaintablePropertyViewer(_edit);
            }
            else {
                _viewer = new StringPropertyViewer(_edit, tags);
            }

            _edit.addPropertyChangeListener(_viewer);
            JComponent c = _viewer.getView();
            c.setToolTipText(ReflectUtils.className2Java(t));

            // create the modifier, if the property can be read
            final Component customizer;

            if ((indexed ? ipd.getIndexedWriteMethod()
                    : pd.getWriteMethod()) == null) {
                // read-only property
                _modifier = null;
                customizer = null;
            }
            else {
                // allow a click on the viewer to start editing
                c.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if ((e.getModifiers()
                                & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                            _owner.startEditing(PropertyEditorComponent.this);
                        }
                    }
                });

                // save the custom editor
                customizer = _edit.getCustomEditor();

                // select an appropriate modifier component
                if (isBooleanProperty) {
                    _modifier = new BooleanPropertyModifier();
                }
                else if (_edit.getTags() != null || tags != null) {
                    _modifier = new TagPropertyModifier(tags);
                }
                else if (_edit.getAsText() != null) {
                    _modifier = new StringPropertyModifier();
                }
                else {// (customizer != null) {
                    _modifier = new CustomizerPropertyModifier();
                }

                _viewer.getView().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if ((e.getModifiers()
                                & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                            _owner.startEditing(PropertyEditorComponent.this);
                        }
                    }
                });

                // set the appropriate listeners
                final JComponent jc = _modifier.getEditor();
                if (jc != null) {
                    jc.setInputVerifier(new InputVerifier() {
                        @Override
                        public boolean verify(JComponent input) {
                            if (!_modifier.applyChanges()) {
                                Toolkit.getDefaultToolkit().beep();
                                return false;
                            }
                            else {
                                return true;
                            }
                        }
                    });

                    final JPopupMenu popup = new JPopupMenu();
                    Action a;

                    popup.add(a = new AbstractAction() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _owner.cancelEditing();
                        }
                    });
                    owner.getResource().configureAction(
                            a, "act._Property.CancelEdit");

                    popup.add(a = new AbstractAction() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _owner.stopEditing();
                        }
                    });
                    owner.getResource().configureAction(
                            a, "act._Property.FinishEdit");

                    if (customizer != null) {
                        popup.addSeparator();
                        popup.add(a = new AbstractAction() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (!_owner.stopEditing()) {
                                    Toolkit.getDefaultToolkit().beep();
                                    return;
                                }
                                new Display(_owner.getOwnerFrame(),
                                        _owner.getResource(),
                                        customizer).setVisible(true);
                            }
                        });

                        owner.getResource().configureAction(
                                a, "act._Property.Customize");
                    }
                    ;

                    _popupMouseListener = new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (!_owner.isEditing()) {
                                return;
                            }
                            if (((e.getModifiers()
                                    & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
                                    ||
                                    e.isPopupTrigger()) {
                                Object src = e.getSource();
                                Component s = src instanceof Component
                                        ? (Component)src
                                        : jc;
                                popup.show(s, e.getX(), e.getY());
                            }
                        }
                    };

                    jc.addMouseListener(_popupMouseListener);
                }
            }

            // initialize as passive
            setRightComponent(_viewer.getView());
            _isActive = false;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void switchState() {
        if (_isActive) {
            deactivate(false);
        }
        else {
            activate();
        }
    }

    public void activate() {
        if (_isActive || _modifier == null) {
            return;
        }

        JComponent jc = _modifier.getEditor();
        if (jc == null) {
            // start editing without switching state, if no editor component is
            // given (this is the case for customizer editors)
            _modifier.startEditing(_owner.getOwnerFrame(),
                    _selector != null ? _selector.getIndex() : 0);
            return;
        }

        int idx = 0;
        if (_selector != null) {
            idx = _selector.getIndex();
            _selector.setEnabled(false);
        }

        setRightComponent(jc);
        repaint();
        jc.requestFocus();

        _modifier.startEditing(_owner.getOwnerFrame(), idx);
        _isActive = true;
    }

    public boolean deactivate(boolean cancel) {
        if (!_isActive) {
            return true;
        }

        // update the property value, if necessary
        if (cancel) {
            _modifier.cancelEditing();
        }
        else if (!_modifier.applyChanges()) {
            Toolkit.getDefaultToolkit().beep();
            return false;
        }

        if (_selector != null) {
            _selector.setEnabled(true);
        }

        // set the state to passive
        JComponent jc = _viewer.getView();
        setRightComponent(jc);
        repaint();
        _isActive = false;
        return true;
    }

    public MouseListener getPopupMouseListener() {
        return _popupMouseListener;
    }

    public void refreshView() {
        int idx = _selector == null ? 0 : _selector.getIndex();
        try {
            _edit.setValue(BeanUtils.getPropertyValue(_bean, _pd, idx));
        }
        catch (Exception e) {
        }
    }

    public void refreshEditor() {
        _modifier.reload();
    }

    private class IndexedPropertySpinner extends JPanel {

        private static final long serialVersionUID = 1L;

        private int _idx = 0, _max = Integer.MAX_VALUE;
        private JTextField _lbl;
        private JButton _up, _down;

        public IndexedPropertySpinner() {
            super(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 16));

            _up = new JButton(GuiResource.getSystemIcon("up.png"));
            _up.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    up();
                }
            });

            _down = new JButton(GuiResource.getSystemIcon("down.png"));
            _down.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    down();
                }
            });

            Border bdr = BorderFactory.createEtchedBorder();
            _up.setBorder(bdr);
            _down.setBorder(bdr);

            Box b = new Box(BoxLayout.Y_AXIS);
            b.add(_up);
            b.add(Box.createVerticalStrut(2));
            b.add(_down);

            _lbl = new JTextField("0", 2);
            _lbl.setEditable(false);
            _lbl.setHorizontalAlignment(SwingConstants.RIGHT);

            add(_lbl, BorderLayout.CENTER);
            add(b, BorderLayout.EAST);

            setEnabled(true);
        }

        private void fireIndexChange() {
            Object value = null;
            boolean setValue = false;

            try {
                value = BeanUtils.getPropertyValue(_bean, _pd, _idx);
                setValue = true;
            }
            catch (RuntimeException e) {
                // mark the max property index
                _max = --_idx;
            }
            if (setValue) {
                _edit.setValue(value);
            }

            if (super.isEnabled()) {
                // update the status of the buttons
                setEnabled(true);
            }
            _lbl.setText(String.valueOf(_idx));
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            _down.setEnabled(enabled && _idx > 0);
            _up.setEnabled(enabled && _idx < _max);
        }

        public void setMax(int max) {
            if ((_max = max) < _idx) {
                _idx = max;
                if (max < 0) {
                    _lbl.setText("-1");
                    setEnabled(isEnabled());
                }
                else {
                    fireIndexChange();
                }
            }
            else {
                setEnabled(isEnabled());
            }
        }

        public void down() {
            --_idx;
            fireIndexChange();
        }

        public void up() {
            ++_idx;
            fireIndexChange();
        }

        public int getIndex() {
            return _idx;
        }
    }

    private abstract class AbstractPropertyModifier
            implements PropertyModifier {
        private int _idx;

        @Override
        public abstract JComponent getEditor();

        public abstract void setInitialSelection();

        @Override
        public void startEditing(JFrame ownerFrame, int index) {
            _idx = index;
            setEditorFromProperty();
            setValueFromEditor();
            setInitialSelection();
        }

        @Override
        public void cancelEditing() {
        }

        @Override
        public boolean applyChanges() {
            try {
                setEditorFromValue();
                setPropertyFromEditor();
                return true;
            }
            catch (IllegalArgumentException e) {
                // e.printStackTrace();
                return false;
            }
        }

        public void setPropertyFromEditor() {
            BeanUtils.setPropertyValue(_bean, _pd, _idx, _edit.getValue());
        }

        public void setEditorFromProperty() {
            _edit.setValue(BeanUtils.getPropertyValue(_bean, _pd, _idx));
        }

        @Override
        public final void reload() {
            setEditorFromProperty();
            setValueFromEditor();
        }

        public abstract void setValueFromEditor();

        protected abstract void setEditorFromValue();

        protected int getIndex() {
            return _idx;
        }
    }

    public class BooleanPropertyModifier extends AbstractPropertyModifier {

        private JCheckBox _cb;

        public BooleanPropertyModifier() {
            _cb = new JCheckBox();
        }

        @Override
        public JComponent getEditor() {
            return _cb;
        }

        @Override
        public void setInitialSelection() {
        }

        @Override
        protected void setEditorFromValue() {
            _edit.setValue(_cb.isSelected() ? Boolean.TRUE : Boolean.FALSE);
        }

        @Override
        public void setValueFromEditor() {
            _cb.setSelected(((Boolean)_edit.getValue()).booleanValue());
        }

    }

    public class StringPropertyModifier extends AbstractPropertyModifier {

        private JTextField _tf;

        public StringPropertyModifier() {
            _tf = new JTextField();
        }

        @Override
        public JComponent getEditor() {
            return _tf;
        }

        @Override
        public void setInitialSelection() {
            String s = _tf.getText();
            _tf.setSelectionStart(0);
            _tf.setSelectionEnd(s.length());
        }

        @Override
        protected void setEditorFromValue() {
            _edit.setAsText(_tf.getText());
        }

        @Override
        public void setValueFromEditor() {
            _tf.setText(_edit.getAsText());
        }
    }

    public class TagPropertyModifier extends AbstractPropertyModifier {

        private JComboBox _cb;
        private Map _xlat;

        public TagPropertyModifier(Object ts) {
            String[] tags;

            if (ts == null) {
                tags = _edit.getTags();
            }
            else if (ts instanceof Map) {
                _xlat = (Map)ts;
                tags = (String[])_xlat.keySet()
                        .toArray(new String[_xlat.size()]);
            }
            else {
                tags = (String[])ts;
            }
            _cb = new JComboBox(tags);
        }

        @Override
        public JComponent getEditor() {
            return _cb;
        }

        @Override
        public void setInitialSelection() {
        }

        @Override
        public void setValueFromEditor() {
            if (_xlat != null) {
                // find the key for the default value
                Object value = _edit.getValue();
                for (Iterator i = _xlat.entrySet().iterator(); i.hasNext();) {
                    Map.Entry e = (Map.Entry)i.next();
                    if (value == null ? e.getValue() == null
                            : value.equals(e.getValue())) {
                        _cb.setSelectedItem(e.getKey());
                        return;
                    }
                }
            }
            else {
                // use the text value
                _cb.setSelectedItem(_edit.getAsText());
            }
        }

        @Override
        public boolean applyChanges() {
            _cb.hidePopup();
            return super.applyChanges();
        }

        @Override
        protected void setEditorFromValue() {
            if (_xlat != null) {
                Object obj = _xlat.get(_cb.getSelectedItem());
                _edit.setValue(obj);
            }
            else {
                _edit.setAsText((String)_cb.getSelectedItem());
            }
        }
    }

    public class CustomizerPropertyModifier extends AbstractPropertyModifier {

        private Component _c;

        public CustomizerPropertyModifier() {
            _c = _edit.getCustomEditor();
            _edit.addPropertyChangeListener(new PropertyChangeListener() {
                // fixme - indexed ?
                @Override
                public void propertyChange(PropertyChangeEvent p1) {
                    BeanUtils.setPropertyValue(_bean, _pd, getIndex(),
                            p1.getNewValue());
                }
            });
        }

        @Override
        public JComponent getEditor() {
            return null;
        }

        @Override
        public void setInitialSelection() {
        }

        @Override
        public void startEditing(JFrame ownerFrame, int index) {
            new Display(ownerFrame, _owner.getResource(), _c).setVisible(true);
        }

        @Override
        protected void setEditorFromValue() {
            // dummy
        }

        @Override
        public void setValueFromEditor() {
            // dummy
        }
    }

}
