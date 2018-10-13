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

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.xodonex.util.ui.ResourceUtils;

/**
 * An abstract implementation of the EditorComponent interface. The
 * implementation is not suitable for use by multiple threads
 */
public abstract class AbstractEditorComponent
        implements EditorComponent, EditorConstants {

    protected AbstractEditorComponent(DataEditor editor) {
        if ((_editor = editor) == null) {
            throw new NullPointerException();
        }
    }

    protected Object init(JComponent container, ResourceBundle rsrc,
            int config) {
        return init(container, container, rsrc, config);
    }

    /**
     * Initializes the editor component.
     *
     * @param container
     *            the container for the editor
     * @param contents
     *            the editor contents
     * @param rsrc
     *            the resource bundle from which data is to be read
     * @param config
     *            determines where the label will be placed relative to the
     *            given component in the final {@link #getUI() UI}.
     * @return null, a String instance or a List instance, based on the presence
     *         of extra configuration data in the description obtained from the
     *         resource file.
     * @see #init(JComponent, Object[], int)
     */
    protected Object init(JComponent container, JComponent contents,
            ResourceBundle rsrc, int config) {

        Object[] data = (rsrc == null)
                ? new Object[] { _editor.getID(), null, "" }
                : ResourceUtils.decodeString(rsrc.getString(
                        getResourceId(_editor.getID())), 0);

        init(container, contents, data, config);
        return data.length > 3 ? data[3] : null;
    }

    /**
     * Generates a resource ID from an editor ID.
     *
     * @param id
     *            the resource id
     * @return the default implementation returns "edt." + id;
     */
    protected String getResourceId(String id) {
        return "edt." + id;
    }

    protected void init(JComponent c, Object[] data, int type) {
        init(c, c, data, type);
    }

    /**
     * Initializes the editor. The method should be invoked exactly once, and
     * before the editor is used for the first time.
     *
     * @param container
     *            the topmost component which is used to hold and edit data
     * @param contents
     *            the content component whose tooltip and enabled state should
     *            be configured
     * @param data
     *            the resource properties
     * @param type
     *            determines where the label will be placed relative to the
     *            given component in the final {@link #getUI() UI}.
     */
    protected void init(JComponent container, JComponent contents,
            Object[] data, int type) {
        try {
            _editorUI = contents;
            container.setMaximumSize(container.getPreferredSize());

            String lbl = data[0] == null ? "" : data[0].toString();
            if (lbl.length() == 0) {
                // no label - use the editor directly
                _ui = container;
                _label = null;
            }
            else {
                _label = new JLabel(lbl);

                _label.setLabelFor(container);
                if (data[1] != null) {
                    _label.setDisplayedMnemonic(((String)data[1]).charAt(0));
                }

                JPanel p = new JPanel();

                int labelPlacement = type & MASK_LABEL;
                if (labelPlacement == LABEL_WEST
                        || labelPlacement == LABEL_EAST) {
                    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
                    container.setAlignmentY(0.5f);
                    _label.setAlignmentY(0.5f);

                    if (labelPlacement == LABEL_WEST) {
                        p.add(_label);
                        p.add(container);
                    }
                    else {
                        p.add(container);
                        p.add(_label);
                    }
                }
                else { // LABEL_NORTH || LABEL_SOUTH
                    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
                    container.setAlignmentX(0.0f);
                    _label.setAlignmentX(0.0f);

                    if (labelPlacement == LABEL_SOUTH) {
                        p.add(container);
                        p.add(_label);
                    }
                    else {
                        p.add(_label);
                        p.add(container);
                    }
                }

                _ui = p;
            }

            // set an input verifier for the component, if it is not read-only
            if ((type & MASK_STATE) == STATE_ENABLED) {
                contents.setInputVerifier(new Verifier());
            }
            else {
                enableUI(false);
            }
            if (data[2] != null) {
                contents.setToolTipText(data[2].toString());
            }
        }
        catch (RuntimeException e) {
            throw new RuntimeException("Illegal resource description " +
                    Arrays.asList(data).toString());
        }
    }

    @Override
    public abstract void updateFromEditor();

    @Override
    public abstract boolean updateFromUI();

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        updateFromEditor();
    }

    @Override
    public final JComponent getUI() {
        return _ui;
    }

    @Override
    public final JComponent getEditorUI() {
        return _editorUI;
    }

    @Override
    public void enableUI(boolean enabled) {
        _editorUI.setEnabled(enabled);
    }

    @Override
    public final JLabel getLabel() {
        return _label;
    }

    @Override
    public final DataEditor getEditor() {
        return _editor;
    }

    // the associated editor
    private DataEditor _editor;

    // the visible component
    private JComponent _ui;

    // the editor part of the visible component
    private JComponent _editorUI;

    // the label used
    private JLabel _label;

    private class Verifier extends InputVerifier {
        @Override
        public boolean verify(JComponent input) {
            if (!updateFromUI()) {
                Toolkit.getDefaultToolkit().beep();
                return false;
            }
            else {
                return true;
            }
        }
    }

}
