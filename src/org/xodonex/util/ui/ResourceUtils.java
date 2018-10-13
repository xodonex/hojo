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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.xodonex.util.ConvertUtils;
import org.xodonex.util.beans.BeanUtils;
import org.xodonex.util.ui.comp.GenericAction;

/**
 * Utilities for dealing with resource bundles, including a simple syntax for
 * defining common UI properties.
 */
public class ResourceUtils {

    // resource string escape character
    public final static char CHAR_ESCAPE = '%';

    // mnemonic escape character
    public final static char CHAR_MNEMONIC = '&';

    /**
     * Decode a string description of a font. Syntax: family , size [ ,
     * {modifier } ] where modifier is one of of <em>italic</em> and
     * <em>bold</em> or <em>plain</em>
     *
     * @param s
     *            the resource string
     * @return the corresponding system font
     * @throws IllegalArgumentException
     *             on input error
     */
    public static Font toFont(String s) throws IllegalArgumentException,
            NumberFormatException {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.length() == 0) {
            return null;
        }

        String[] def = new String[3];
        decodeString(s, def, 0, -1);

        int points = def[1] == null ? 12 : Integer.parseInt(def[1]);

        int style = 0, idx;
        s = def[2];

        while ((idx = s.indexOf(' ')) > 0 || s.length() > 0) {
            String s_;

            if (idx < 0) {
                s_ = s.trim().toUpperCase();
                s = "";
            }
            else {
                s_ = s.substring(0, idx).trim().toUpperCase();
                s = s.substring(idx + 1);
            }

            if (s_.equals("BOLD")) {
                style |= Font.BOLD;
            }
            else if (s_.equals("ITALIC")) {
                style |= Font.ITALIC;
            }
            else if (s_.equals("PLAIN")) {
                style = Font.PLAIN;
            }
            else {
                throw new IllegalArgumentException(s_);
            }
        }
        return new Font(def[0], style, points);
    }

    /**
     * Inverse of {@link #toFont(String)}.
     *
     * @param font
     *            the font to be described
     * @return the resource description of the font
     */
    public static String toString(Font font) {
        if (font == null) {
            return "";
        }

        String style;
        switch (font.getStyle()) {
        case Font.PLAIN:
            style = ", plain";
            break;
        case Font.BOLD:
            style = ", bold";
            break;
        case Font.ITALIC:
            style = ", italic";
            break;
        case Font.BOLD | Font.ITALIC:
            style = ", bold italic";
            break;
        default:
            style = "";
        }

        return font.getFontName() + ", " + font.getSize() + style;
    }

    /**
     * Decode a string description of a color. Syntax: rrggbb (hex) or
     * rrr,ggg,bbb[,aaa] (decimal)
     *
     * @param s
     *            the color to be decoded
     * @return the resulting color
     */
    public static Color toColor(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.length() == 0) {
            return null;
        }

        int idx = s.indexOf(',');
        int a = 255, r, g = -1, b = -1;

        if (idx < 0) {
            int l = s.length();
            if (l != 6 && l != 8) {
                throw new IllegalArgumentException(s);
            }

            if (l == 8) {
                a = Integer.parseInt(s.substring(0, 2), 16);
                s = s.substring(2);
            }
            r = Integer.parseInt(s.substring(0, 2), 16);
            g = Integer.parseInt(s.substring(2, 4), 16);
            b = Integer.parseInt(s.substring(4), 16);
        }
        else {
            r = Integer.parseInt(s.substring(0, idx).trim());
            s = s.substring(idx + 1);
            if ((idx = s.indexOf(',')) > 0) {
                g = Integer.parseInt(s.substring(0, idx).trim());
                s = s.substring(idx + 1);

                if ((idx = s.indexOf(',')) > 0) {
                    b = Integer.parseInt(s.substring(0, idx).trim());
                    a = Integer.parseInt(s.substring(idx + 1).trim());
                }
                else {
                    b = Integer.parseInt(s.trim());
                }
            }
        }

        return new Color(r, g, b, a);
    }

    /**
     * Inverse of {@link #toColor(String)}.
     *
     * @param col
     *            the color to be described
     * @return the resource description of the color
     */
    public static String toString(Color col) {
        if (col == null) {
            return "";
        }

        StringBuffer buf = new StringBuffer(8);

        int i = col.getAlpha();
        if (i != 255) {
            toHex(buf, i);
        }
        toHex(buf, col.getRed());
        toHex(buf, col.getGreen());
        toHex(buf, col.getBlue());
        return buf.toString();
    }

    /**
     * Decode a textual representation of a key stroke. Syntax: see
     * {@link KeyStroke#getKeyStroke(String)}
     *
     * @param s
     *            the keystroke description
     * @return the correponding keystroke
     */
    public static KeyStroke toKeyStroke(String s) {
        if (s == null) {
            return null;
        }
        s = s.trim();
        if (s.length() == 0) {
            return null;
        }

        KeyStroke result = KeyStroke.getKeyStroke(s);
        if (result == null) {
            throw new IllegalArgumentException(s);
        }

        return result;
    }

    /**
     * Inverse of {@link #toKeyStroke(String)}.
     *
     * @param ks
     *            a keystroke
     * @return the description of the keystroke
     */
    public static String toString(KeyStroke ks) {
        if (ks == null) {
            return "";
        }

        String mod = KeyEvent.getKeyModifiersText(ks.getModifiers());
        String key = KeyEvent.getKeyText(ks.getKeyCode());
        return mod.length() == 0 ? key : mod + " " + key;
    }

    public static Dimension toDimension(String s) {
        char[] cs = s.trim().toCharArray();

        int w = 0, h = 0, i = 0;

        while (i < cs.length && cs[i] <= '9' && cs[i] >= '0') {
            w = w * 10 + (cs[i++] - '0');
        }
        while (i < cs.length && (cs[i] > '9' || cs[i] < '0')) {
            i++;
        }
        while (i < cs.length && cs[i] <= '9' && cs[i] >= '0') {
            h = h * 10 + (cs[i++] - '0');
        }

        return new Dimension(w, h);
    }

    public static String toString(Dimension d) {
        return "" + d.getWidth() + " x " + d.getHeight();
    }

    /**
     * Decode a menu bar description.
     *
     * @param def
     *            the description
     * @param font
     *            the font to be used for the menu bar
     * @param ap
     *            the action provider to be used resolving action names into
     *            actions
     * @return the generated menubar
     */
    public static JMenuBar createMenuBar(String def, Font font,
            ActionProvider ap) {
        JMenuBar result = new JMenuBar();
        result.setFont(font);

        ResourceStringTokenizer tok = new ResourceStringTokenizer(def,
                CHAR_ESCAPE);

        // parse the menu definitions
        while (true) {
            JMenu m = (JMenu)decodeMenu(tok, ap);
            if (m == null) {
                break;
            }
            result.add(m);
        }

        return result;
    }

    /**
     * Decode a tool bar description.
     *
     * @param def
     *            the description
     * @param ap
     *            the action provider to be used resolving action names into
     *            actions
     * @return the generated tool bar
     */
    public static JToolBar[] createToolBars(String def, ActionProvider ap) {
        java.util.ArrayList l = new java.util.ArrayList(4);

        ResourceStringTokenizer tok = new ResourceStringTokenizer(def,
                CHAR_ESCAPE);

        if (tok.nextToken(-1) != ResourceStringTokenizer.TT_LPAR) {
            throw new IllegalArgumentException(def);
        }
        while (tok.nextToken(-1) != ResourceStringTokenizer.TT_RPAR) {
            if (tok.ttype != ResourceStringTokenizer.TT_WORD) {
                throw new IllegalArgumentException(def);
            }

            JToolBar tb = new JToolBar(tok.sval);
            tok.nextToken(-1);

            if (tok.nextToken(-1) == ResourceStringTokenizer.TT_WORD) {
                tb.setFloatable(Boolean.valueOf(tok.sval).booleanValue());
            }
            else {
                tok.pushBack();
            }

            if (tok.nextToken(-1) != ResourceStringTokenizer.TT_LPAR) {
                throw new IllegalArgumentException(def);
            }

            while (tok.nextToken(-1) != ResourceStringTokenizer.TT_RPAR) {
                if (tok.ttype == ResourceStringTokenizer.TT_SEP) {
                    tb.addSeparator();
                }
                else if (tok.ttype != ResourceStringTokenizer.TT_WORD) {
                    throw new IllegalArgumentException(def);
                }
                else {
                    tb.add(ap.getAction(tok.sval));
                    if (tok.nextToken(-1) == ResourceStringTokenizer.TT_RPAR) {
                        tok.pushBack();
                    }
                }
            } // actions

            l.add(tb);

            if (tok.nextToken(-1) != ResourceStringTokenizer.TT_SEP) {
                tok.pushBack();
            }
        } // tool bar

        return (JToolBar[])l.toArray(new JToolBar[l.size()]);
    }

    /**
     * Decode a property description into an array of properties
     *
     * @param descr
     *            the property description
     * @return the decoded property description
     * @see #configureProperty(PropertyDescriptor, Object[])
     */
    public static Object[] decodePropertyResource(String descr) {
        Object[] def = new Object[4];

        ResourceStringTokenizer tok = new ResourceStringTokenizer(descr,
                CHAR_ESCAPE);

        if (tok.nextToken(-1) == ResourceStringTokenizer.TT_WORD) {
            def[0] = tok.sval;
        }
        else if (tok.ttype == ResourceStringTokenizer.TT_EOS) {
            return def;
        }
        else {
            tok.pushBack();
        }
        ;
        tok.nextToken(ResourceStringTokenizer.TT_SEP);

        if (tok.nextToken(-1) == ResourceStringTokenizer.TT_WORD) {
            def[1] = tok.sval;
        }
        else if (tok.ttype == ResourceStringTokenizer.TT_EOS) {
            return def;
        }
        else {
            tok.pushBack();
        }
        ;
        tok.nextToken(ResourceStringTokenizer.TT_SEP);

        if (tok.nextToken(-1) == ResourceStringTokenizer.TT_WORD) {
            def[2] = Integer.valueOf(tok.sval);
        }
        else if (tok.ttype == ResourceStringTokenizer.TT_EOS) {
            return def;
        }
        else {
            tok.pushBack();
        }
        ;
        if (tok.nextToken(-1) == ResourceStringTokenizer.TT_EOS) {
            return def;
        }
        else if (tok.ttype != ResourceStringTokenizer.TT_SEP) {
            throw new IllegalArgumentException(descr);
        }

        if (tok.nextToken(-1) != ResourceStringTokenizer.TT_LPAR) {
            throw new IllegalArgumentException(descr);
        }

        ArrayList tags = new ArrayList(4);
        while (tok.nextToken(-1) != ResourceStringTokenizer.TT_RPAR) {
            if (tok.ttype == ResourceStringTokenizer.TT_EOS) {
                throw new IllegalArgumentException(descr);
            }
            else if (tok.ttype == ResourceStringTokenizer.TT_WORD) {
                tags.add(tok.sval);
            }
        } // while
        def[3] = tags.toArray(new String[tags.size()]);

        return def;
    }

    /**
     * Configure a property descriptor from a set of properties
     *
     * @param pd
     *            the property descriptor to be configured
     * @param cfg
     *            the configuration data, interpreted as follows:
     *            <ol>
     *            <li value="0">Display name
     *            <li value="1">Short description
     *            <li value="2">Property flags
     *            {@link BeanUtils#PROPERTY_CLASS_HIDDEN etc.}
     *            <li value="3">Property editor tags
     *            </ol>
     *            Any of the parameters may be omitted by giving a shorter
     *            array, or by having a contained <code>null</code> in the
     *            array.
     */
    public static void configureProperty(PropertyDescriptor pd, Object[] cfg) {
        String s;

        if (cfg.length < 1) {
            return;
        }

        switch (cfg.length > 4 ? 4 : cfg.length) {
        case 4:
            Object tags = cfg[3];
            if (tags != null) {
                if (pd.getPropertyType() == Integer.TYPE &&
                        tags instanceof String[]) {
                    String[] tgs = (String[])tags;
                    Map m;
                    try {
                        m = (Map)ConvertUtils.DEFAULT_MAP_CLASS.newInstance();
                    }
                    catch (Throwable t) {
                        m = new HashMap(2 * tgs.length);
                    }

                    for (int i = 0; i < tgs.length; i++) {
                        m.put(tgs[i], new Integer(i));
                    }

                    pd.setValue(BeanUtils.PROPERTY_KEY_TAGS, m);
                }
                else {
                    pd.setValue(BeanUtils.PROPERTY_KEY_TAGS, tags);
                }
            }
            // fall through
        case 3:
            Integer i = (Integer)cfg[2];
            if (i != null) {
                int flags = i.intValue();
                pd.setExpert((flags & BeanUtils.PROPERTY_CLASS_EXPERT) != 0);
                pd.setExpert((flags & BeanUtils.PROPERTY_CLASS_HIDDEN) != 0);
                pd.setValue(BeanUtils.PROPERTY_KEY_CLASS, i);
            }
            // fall through
        case 2:
            if ((s = (String)cfg[1]) != null) {
                pd.setShortDescription(s);
            }
            // fall through
        case 1:
            if ((s = (String)cfg[0]) != null) {
                pd.setDisplayName(s);
            }
        }
    }

    /**
     * Decode an action description into an array of properties
     *
     * @param descr
     *            the action description
     * @return the decoded actions
     * @see #configureAction(Action, Object[], IconProvider)
     */
    public static String[] decodeActionResource(String descr) {
        String[] def = new String[8];
        decodeString(descr, def, 1, 1);
        return def;
    }

    /**
     * Configure an action from a set of properties
     *
     * @param a
     *            the action to be configured
     * @param cfg
     *            the configuration data, interpreted as follows:
     *            <ol>
     *            <li value="0">ID
     *            <li value="1">name
     *            <li value="2">mnemonic key
     *            <li value="3">accelerator key
     *            <li value="4">icon
     *            <li value="5">short description
     *            <li value="6">long description
     *            <li value="7">command key
     *            </ol>
     *            Any of the parameters may be omitted by giving a shorter
     *            array, or by having a contained <code>null</code> in the
     *            array.
     * @param ip
     *            the component used to resolve an icon description into an
     *            icon. This may be omitted, if the icon description is not
     *            present in the configuration data.
     */
    public static void configureAction(Action a, Object[] cfg,
            IconProvider ip) {
        String s;

        if (cfg.length < 1) {
            return;
        }

        switch (cfg.length > 8 ? 8 : cfg.length) {
        case 8:
            if ((s = (String)cfg[7]) != null) {
                a.putValue(Action.ACTION_COMMAND_KEY, s);
            }
            // fall through
        case 7:
            if ((s = (String)cfg[6]) != null) {
                a.putValue(Action.LONG_DESCRIPTION, s);
            }
            // fall through
        case 6:
            if ((s = (String)cfg[5]) != null) {
                a.putValue(Action.SHORT_DESCRIPTION, s);
            }
            // fall through
        case 5:
            if ((s = (String)cfg[4]) != null) {
                a.putValue(Action.SMALL_ICON, ip.getIcon(s));
            }
            // fall through
        case 4:
            if ((s = (String)cfg[3]) != null) {
                a.putValue(Action.ACCELERATOR_KEY, toKeyStroke(s));
            }
            // fall through
        case 3:
            if ((s = (String)cfg[2]) != null) {
                KeyStroke ks = toKeyStroke(s);
                if (ks != null) {
                    a.putValue(Action.MNEMONIC_KEY,
                            new Integer(ks.getKeyCode()));
                }
            }
            // fall through
        case 2:
            if ((s = (String)cfg[1]) != null) {
                a.putValue(Action.NAME, s);
            }
            // fall through
        case 1:
            // set the ID
            if ((s = (String)cfg[0]) != null) {
                a.putValue(GenericAction.ID, s);
            }
        }
    }

    /**
     * Decode a label description into an array of properties
     *
     * @param descr
     *            the label description
     * @return the decoded properties
     * @see #configureLabel(JLabel, Object[], IconProvider)
     */
    public static String[] decodeLabelResource(String descr) {
        String[] def = new String[5];
        decodeString(descr, def, 0, 0);
        return def;
    }

    /**
     * Configure a label from a set of properties
     *
     * @param lbl
     *            the action to be configured
     * @param cfg
     *            the configuration data, interpreted as follows:
     *            <ol>
     *            <li value="0">text
     *            <li value="1">mnemonic
     *            <li value="2">tooltip
     *            <li value="3">icon
     *            <li value="4">disabled
     *            </ol>
     *            Any of the parameters may be omitted by giving a shorter
     *            array, or by having a contained <code>null</code> in the
     *            array.
     * @param ip
     *            the component used to resolve an icon description into an
     *            icon. This may be omitted, if none of the icon descriptions
     *            are present in the configuration data.
     */
    public static void configureLabel(JLabel lbl, Object[] cfg,
            IconProvider ip) {
        String s;

        if (cfg.length < 1) {
            return;
        }

        switch (cfg.length > 5 ? 5 : cfg.length) {
        case 5:
            if ((s = (String)cfg[4]) != null) {
                lbl.setDisabledIcon(ip.getIcon(s));
            }
            // fall through
        case 4:
            if ((s = (String)cfg[3]) != null) {
                lbl.setIcon(ip.getIcon(s));
            }
            // fall through
        case 3:
            if ((s = (String)cfg[2]) != null) {
                lbl.setToolTipText(s);
            }
            // fall through
        case 2:
            if ((s = (String)cfg[1]) != null) {
                lbl.setDisplayedMnemonic(
                        KeyStroke.getKeyStroke(s).getKeyCode());
            }
            // fall through
        case 1:
            if ((s = (String)cfg[0]) != null) {
                lbl.setText(s);
            }
        }
    }

    /**
     * Decode a button description into an array of properties
     *
     * @param descr
     *            the button description
     * @return the decoded button properties
     * @see #configureButton(AbstractButton, Object[], IconProvider)
     */
    public static String[] decodeButtonResource(String descr) {
        String[] def = new String[9];
        decodeString(descr, def, 0, 0);
        return def;
    }

    /**
     * Configure a button from a set of properties
     *
     * @param btn
     *            the action to be configured
     * @param cfg
     *            the configuration data, interpreted as follows:
     *            <ol>
     *            <li value="0">text
     *            <li value="1">mnemonic
     *            <li value="2">tooltip
     *            <li value="3">enabled
     *            <li value="4">normal icon
     *            <li value="5">disabled icon
     *            <li value="6">pressed icon
     *            <li value="7">rollover icon
     *            <li value="8">rollover selected icon
     *            </ol>
     *            Any of the parameters may be omitted by giving a shorter
     *            array, or by having a contained <code>null</code> in the
     *            array.
     * @param ip
     *            the component used to resolve an icon description into an
     *            icon. This may be omitted, if none of the icon descriptions
     *            are present in the configuration data.
     */
    public static void configureButton(AbstractButton btn, Object[] cfg,
            IconProvider ip) {
        String s;

        if (cfg.length < 1) {
            return;
        }

        switch (cfg.length > 9 ? 9 : cfg.length) {
        case 9:
            if ((s = (String)cfg[8]) != null) {
                btn.setRolloverSelectedIcon(ip.getIcon(s));
                btn.setRolloverEnabled(true);
            }
            // fall through
        case 8:
            if ((s = (String)cfg[7]) != null) {
                btn.setRolloverIcon(ip.getIcon(s));
                btn.setRolloverEnabled(true);
            }
            // fall through
        case 7:
            if ((s = (String)cfg[6]) != null) {
                btn.setPressedIcon(ip.getIcon(s));
            }
            // fall through
        case 6:
            if ((s = (String)cfg[5]) != null) {
                btn.setDisabledIcon(ip.getIcon(s));
            }
            // fall through
        case 5:
            if ((s = (String)cfg[4]) != null) {
                btn.setIcon(ip.getIcon(s));
            }
            // fall through
        case 4:
            if ((s = (String)cfg[3]) != null) {
                btn.setEnabled(Boolean.valueOf(s).booleanValue());
            }
            // fall through
        case 3:
            if ((s = (String)cfg[2]) != null) {
                btn.setToolTipText(s);
            }
            // fall through
        case 2:
            if ((s = (String)cfg[1]) != null) {
                btn.setMnemonic(KeyStroke.getKeyStroke(s).getKeyCode());
            }
            // fall through
        case 1:
            if ((s = (String)cfg[0]) != null) {
                btn.setText(s);
            }
        }
    }

    /**
     * Equivalent to <code>decodeString(descr, null, 0, -1)</code>
     *
     * @param descr
     *            the property description
     * @return the decoded properties
     */
    public static Object[] decodeString(String descr) {
        return decodeString(descr, null, 0, -1);
    }

    /**
     * Equivalent to <code>decodeString(descr, null, 0, mnemonicIdx)</code>
     *
     * @param descr
     *            the property description
     * @param mnemonicIdx
     *            the index at which a mnemonic string is decoded, if any
     * @return the decoded properties
     */
    public static Object[] decodeString(String descr, int mnemonicIdx) {
        return decodeString(descr, null, 0, mnemonicIdx);
    }

    /**
     * Decode a resource string into an array of strings and/or lists
     *
     * @param descr
     *            the property description
     * @param def
     *            the array to store the description. If the given value is
     *            null, then a new array will be created.
     * @param idx
     *            the index at which to store the decoded strings into the
     *            array.
     * @param mnemonicIdx
     *            the index at which a mnemonic string is decoded, if any
     * @return the decoded properties
     */
    public static Object[] decodeString(String descr, Object[] def,
            int idx, int mnemonicIdx) {
        ResourceStringTokenizer tok = new ResourceStringTokenizer(descr,
                CHAR_ESCAPE);
        ArrayList l = def == null ? new ArrayList() : null;
        String s;

        while (true) {
            if (tok.nextToken(idx == mnemonicIdx ? CHAR_MNEMONIC
                    : -1) == ResourceStringTokenizer.TT_WORD) {
                s = tok.sval;
                if (def == null) {
                    l.add(s);
                }
                else {
                    def[idx] = s;
                }

                if (idx++ == mnemonicIdx) {
                    if (tok.mnemonic >= 0) {
                        String c = String.valueOf((char)tok.mnemonic);
                        if (def == null) {
                            l.add(c);
                        }
                        else {
                            def[idx] = c;
                        }
                    }
                    else {
                        if (def == null) {
                            l.add(null);
                        }
                        else {
                            def[idx] = null;
                        }
                    }
                    idx++;
                }
                tok.nextToken(-1);
            }
            else if (tok.ttype == ResourceStringTokenizer.TT_EOS) {
                break;
            }
            else if (tok.ttype == ResourceStringTokenizer.TT_LPAR) {
                tok.pushBack();
                if (def == null) {
                    l.add(decodeString(tok));
                }
                else {
                    def[idx] = decodeString(tok);
                }
            }
            else {
                if (def == null) {
                    l.add(null);
                }

                if (idx++ == mnemonicIdx) {
                    if (def == null) {
                        l.add(null);
                    }
                    idx++;
                }
            }
        }

        if (def == null) {
            def = l.toArray(new Object[l.size()]);
        }
        return def;
    }

    private static Object decodeString(ResourceStringTokenizer tok) {
        tok.nextToken(-1);

        if (tok.ttype == ResourceStringTokenizer.TT_EOS) {
            return null;
        }
        else if (tok.ttype == ResourceStringTokenizer.TT_WORD) {
            String s = tok.sval;

            // read a the delimiter
            if (tok.nextToken(-1) == ResourceStringTokenizer.TT_RPAR) {
                tok.pushBack();
            }
            return s;
        }
        else if (tok.ttype == ResourceStringTokenizer.TT_LPAR) {
            ArrayList l = new ArrayList(2);
            while (tok.nextToken(-1) != ResourceStringTokenizer.TT_EOS
                    && tok.ttype != ResourceStringTokenizer.TT_RPAR) {
                tok.pushBack();
                l.add(decodeString(tok));
            }
            if (tok.ttype == ResourceStringTokenizer.TT_EOS) {
                throw new IllegalArgumentException(tok.toString());
            }

            return l;
        }
        else {
            throw new IllegalArgumentException(tok.toString());
        }
    }

    private ResourceUtils() {
    }

    private static void toHex(StringBuffer buf, int i) {
        buf.append(i >>> 4).append(i % 16);
    }

    // create a menu item or menu from a description
    private static JComponent decodeMenu(ResourceStringTokenizer tok,
            ActionProvider ap) {
        return decodeMenu(tok, ap, false);
    }

    private final static JComponent SEPARATOR = new JPanel();

    private static JComponent decodeMenu(ResourceStringTokenizer tok,
            ActionProvider ap, boolean allowRPAR) {
        if (tok.nextToken(CHAR_MNEMONIC) == ResourceStringTokenizer.TT_EOS) {
            return null;
        }
        else if (tok.ttype == ResourceStringTokenizer.TT_SEP) {
            return SEPARATOR;
        }
        else if (tok.ttype == ResourceStringTokenizer.TT_RPAR && allowRPAR) {
            // end of block
            return null;
        }
        else if (tok.ttype != ResourceStringTokenizer.TT_WORD) {
            throw new IllegalArgumentException();
        }

        // save the name and mnemonic
        String s = tok.sval;
        int mn = tok.mnemonic;
        JComponent result;

        // look ahead for a left parenthesis
        if (tok.nextToken(CHAR_MNEMONIC) == ResourceStringTokenizer.TT_LPAR) {
            // a submenu was specified
            JMenu m = new JMenu(s);
            if (mn >= 0) {
                m.setMnemonic((char)mn);
            }

            // parse the individual menu items
            while (true) {
                JComponent jc = decodeMenu(tok, ap, true);
                if (jc == null) {
                    if (tok.ttype != ResourceStringTokenizer.TT_RPAR) {
                        throw new IllegalArgumentException();
                    }
                    break;
                }

                if (jc == SEPARATOR) {
                    m.addSeparator();
                }
                else {
                    m.add(jc);
                }
            } // while

            result = m;
        }
        else {
            tok.pushBack();
            result = new JMenuItem(ap.getAction(s));
        }

        if (tok.nextToken(CHAR_MNEMONIC) == ResourceStringTokenizer.TT_RPAR) {
            tok.pushBack();
        }
        else if (tok.ttype != ResourceStringTokenizer.TT_EOS
                && tok.ttype != ResourceStringTokenizer.TT_SEP) {
            throw new IllegalArgumentException();
        }

        return result;
    }

}
