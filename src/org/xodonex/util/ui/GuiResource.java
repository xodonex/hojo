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
package org.xodonex.util.ui;

import java.awt.Font;
import java.beans.PropertyDescriptor;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

/**
 * The GuiResources provides methods to create GUI components from a resource
 * bundle.
 */
public class GuiResource {

    private final static WeakHashMap _eventRelation = new WeakHashMap();

    private static WeakHashMap _defaults = new WeakHashMap();
    private static IconProvider sysIconProvider = new IconProvider() {
        @Override
        public Icon getIcon(String id) {
            return getSystemIcon(id);
        }
    };

    public static GuiResource getDefaultInstance() {
        return getDefaultInstance(null);
    }

    public synchronized static GuiResource getDefaultInstance(Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }

        GuiResource rsrc = (GuiResource)_defaults.get(locale);
        if (rsrc != null) {
            return rsrc;
        }

        rsrc = new GuiResource(locale, null, null, sysIconProvider, null);
        _defaults.put(locale, rsrc);
        return rsrc;
    }

    public static IconProvider getSystemIconProvider() {
        return sysIconProvider;
    }

    public static Icon getSystemIcon(String id) {
        return getSystemIcon(id, null);
    }

    public static Icon getSystemIcon(String id, Locale locale) {
        URL url = getSystemResource(id, locale);
        if (url == null) {
            throw new NullPointerException("Missing resource " + id);
        }
        return new ImageIcon(url);
    }

    public static URL getSystemResource(String id) {
        return getSystemResource(id, null);
    }

    public static URL getSystemResource(String id, Locale locale) {
        GuiResource gr = getDefaultInstance(locale);
        return gr.getClass().getClassLoader()
                .getResource(getSystemPath(gr, id));
    }

    /**
     * Set an <em>event relation</em>, i.e. a relation which determines which
     * action to take for a given event source. If a shared Action instance is
     * to be used in multiple contexts (such as popup menus, tool bars, menus
     * etc.), the event relation can be used to give a detailed information on
     * the context in which the action was used.
     *
     * @param source
     *            the event source
     * @param data
     *            some data which is more useful to the event listener than the
     *            source of the event itself.
     * @return the previously related data for the given source.
     */
    public synchronized static Object setEventRelation(Object source,
            Object data) {
        return _eventRelation.put(source, data);
    }

    /**
     * Remove an {@link #setEventRelation(Object, Object) event relation}.
     *
     * @param source
     *            the event source whose relation should be removed
     */
    public synchronized static void removeEventRelation(Object source) {
        _eventRelation.remove(source);
    }

    /**
     * @return the {@link #setEventRelation(Object, Object) related data} for
     *         the given event source.
     * 
     * @param source
     *            the event source whose relation should be removed
     */
    public synchronized static Object getEventRelation(Object source) {
        return _eventRelation.get(source);
    }

    private Locale _locale;
    private ResourceBundle _rsrc;
    private ActionProvider _ap;
    private IconProvider _ip;
    private JFrame _main;

    // cache of decoded resource strings (excluding menu bar and tool bars)
    private Map _cache = new HashMap(16, 0.9f);

    public GuiResource(Locale locale, ResourceBundle rsrc,
            ActionProvider ap, IconProvider ip, JFrame main)
            throws MissingResourceException {
        // locate the base resources
        ResourceBundle base = ResourceBundle.getBundle(
                getSystemPath(this, "GUI"), _locale = locale);

        _rsrc = rsrc == null ? base : new CompositeResourceBundle(rsrc, base);
        _ap = ap;
        _ip = ip;
        _main = main;
    }

    public Locale getLocale() {
        return _locale;
    }

    public ActionProvider getActionProvider() {
        return _ap;
    }

    public IconProvider getIconProvider() {
        return _ip;
    }

    public synchronized JFrame getMainFrame() {
        return _main;
    }

    public synchronized void setMainFrame(JFrame main) {
        _main = main;
    }

    public ResourceBundle getBundle() {
        return _rsrc;
    }

    public String getString(String key) {
        return _rsrc.getString(key);
    }

    public String getSimpleString(String key) {
        return toSimpleString(getString(key));
    }

    public final JMenuBar createMenuBar(String key, Font font) {
        return createMenuBar(key, font, _ap);
    }

    public JMenuBar createMenuBar(String key, Font font, ActionProvider ap) {
        return ResourceUtils.createMenuBar(getString(key), font, ap);
    }

    public final JToolBar[] createToolBars(String key) {
        return createToolBars(key, _ap);
    }

    public JToolBar[] createToolBars(String key, ActionProvider ap) {
        return ResourceUtils.createToolBars(getString(key), ap);
    }

    public synchronized PropertyDescriptor configureProperty(
            PropertyDescriptor pd, String key) {
        Object[] def = (Object[])_cache.get(key);
        if (def == null) {
            def = ResourceUtils.decodePropertyResource(getString(key));
            _cache.put(key, def);
        }

        ResourceUtils.configureProperty(pd, def);
        return pd;
    }

    public final AbstractButton configureButton(AbstractButton btn,
            String key) {
        return configureButton(btn, key, _ip);
    }

    public synchronized AbstractButton configureButton(AbstractButton btn,
            String key, IconProvider ip) {
        Object[] def = (Object[])_cache.get(key);
        if (def == null) {
            def = ResourceUtils.decodeButtonResource(getString(key));
            _cache.put(key, def);
        }

        ResourceUtils.configureButton(btn, def, ip);
        return btn;
    }

    public JButton createButton(String key) {
        JButton btn = new JButton();
        configureButton(btn, key);
        return btn;
    }

    public synchronized JLabel configureLabel(JLabel lbl, String key) {
        Object[] def = (Object[])_cache.get(key);
        if (def == null) {
            def = ResourceUtils.decodeLabelResource(getString(key));
            _cache.put(key, def);
        }

        ResourceUtils.configureLabel(lbl, def, _ip);
        return lbl;
    }

    public JLabel createLabel(String key) {
        JLabel lbl = new JLabel();
        configureLabel(lbl, key);
        return lbl;
    }

    public final Action configureAction(Action action, String key) {
        return configureAction(action, key, _ip);
    }

    public synchronized Action configureAction(Action action, String key,
            IconProvider ip) {
        Object[] def = (Object[])_cache.get(key);
        if (def == null) {
            def = ResourceUtils.decodeActionResource(getString(key));
            _cache.put(key, def);
        }

        ResourceUtils.configureAction(action, def, ip);
        return action;
    }

    private static String getSystemPath(GuiResource gr, String id) {
        return gr.getClass().getPackage().getName().replace('.', '/') +
                "/resource/" + id;
    }

    private static String toSimpleString(String s) {
        int idx = s.indexOf(ResourceUtils.CHAR_MNEMONIC);
        if (idx >= 0) {
            if (idx > 0 && s.charAt(idx - 1) == ResourceUtils.CHAR_ESCAPE) {
                idx--;
            }
            return s.substring(0, idx) + s.substring(idx + 1);
        }
        else {
            return s;
        }
    }

}
