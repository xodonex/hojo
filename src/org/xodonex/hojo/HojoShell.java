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
package org.xodonex.hojo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.xodonex.hojo.lang.Code;
import org.xodonex.hojo.lang.Statement;
import org.xodonex.hojo.lang.env.BaseEnv;
import org.xodonex.util.ArrayUtils;
import org.xodonex.util.ConvertUtils;
import org.xodonex.util.ReflectUtils;
import org.xodonex.util.StringUtils;
import org.xodonex.util.io.BufferReader;
import org.xodonex.util.os.OsInterface;
import org.xodonex.util.os.PatternFilechooserFileFilter;
import org.xodonex.util.thread.CommandQueue;
import org.xodonex.util.ui.ButtonConstants;
import org.xodonex.util.ui.DialogUtils;
import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.WindowManager;
import org.xodonex.util.ui.comp.AutoScrollPane;
import org.xodonex.util.ui.comp.MultipleUndoManager;
import org.xodonex.util.ui.comp.Splash;
import org.xodonex.util.ui.comp.StatusBar;
import org.xodonex.util.ui.dialog.SelectDialog;
import org.xodonex.util.ui.dialog.TextDialog;
import org.xodonex.util.ui.layout.SingleCenterLayout;
import org.xodonex.util.ui.window.BrowserWindow;

/**
 * A GUI front-end (user shell) for the {@link HojoInterpreter}.
 */
public class HojoShell implements HojoConst {

    // constants for action codes
    private final static int ACT_BASE_MENU = 0,
            ACT_BASE_MENU_FILE = ACT_BASE_MENU + 100,
            ACT_BASE_MENU_EDIT = ACT_BASE_MENU + 200,
            ACT_BASE_MENU_COMMAND = ACT_BASE_MENU + 300,
            ACT_BASE_MENU_OPTIONS = ACT_BASE_MENU + 400,
            ACT_BASE_MENU_HELP = ACT_BASE_MENU + 500,

            ACT_MENU_FILE_INCLUDE = ACT_BASE_MENU_FILE + 1,
            ACT_MENU_FILE_LOAD_SCRIPT = ACT_BASE_MENU_FILE + 2,
            ACT_MENU_FILE_SAVE_SCRIPT = ACT_BASE_MENU_FILE + 3,
            ACT_MENU_FILE_LOAD_VALUE = ACT_BASE_MENU_FILE + 4,
            ACT_MENU_FILE_SAVE_VALUE = ACT_BASE_MENU_FILE + 5,
            ACT_MENU_FILE_SAVE_VARS = ACT_BASE_MENU_FILE + 6,
            ACT_MENU_FILE_CHDIR = ACT_BASE_MENU_FILE + 7,
            ACT_MENU_FILE_QUIT = ACT_BASE_MENU_FILE + 8,

            ACT_MENU_EDIT_CUT = ACT_BASE_MENU_EDIT + 1,
            ACT_MENU_EDIT_COPY = ACT_BASE_MENU_EDIT + 2,
            ACT_MENU_EDIT_PASTE = ACT_BASE_MENU_EDIT + 3,
            ACT_MENU_EDIT_SELECT_ALL = ACT_BASE_MENU_EDIT + 4,
            ACT_MENU_EDIT_CURRENT = ACT_BASE_MENU_EDIT + 5,
            ACT_MENU_EDIT_NEXT = ACT_BASE_MENU_EDIT + 6,
            ACT_MENU_EDIT_PREV = ACT_BASE_MENU_EDIT + 7,
            ACT_MENU_EDIT_SIZE = ACT_BASE_MENU_EDIT + 8,

            ACT_MENU_COMMAND_COMPILE = ACT_BASE_MENU_COMMAND + 1,
            ACT_MENU_COMMAND_RUN = ACT_BASE_MENU_COMMAND + 2,
            ACT_MENU_COMMAND_INTERRUPT = ACT_BASE_MENU_COMMAND + 3,
            ACT_MENU_COMMAND_SHOW_TRANSCRIPT = ACT_BASE_MENU_COMMAND + 4,
            ACT_MENU_COMMAND_SHOW_SPLIT = ACT_BASE_MENU_COMMAND + 5,
            ACT_MENU_COMMAND_SHOW_ERROR_LOG = ACT_BASE_MENU_COMMAND + 6,
            ACT_MENU_COMMAND_CLEARTRANSCRIPT = ACT_BASE_MENU_COMMAND + 7,
            ACT_MENU_COMMAND_RESETSYNTAX = ACT_BASE_MENU_COMMAND + 8,
            ACT_MENU_COMMAND_REMOVE_VAR = ACT_BASE_MENU_COMMAND + 9,
            ACT_MENU_COMMAND_CLEAR_VARS = ACT_BASE_MENU_COMMAND + 10,

            ACT_OPTIONS_STRICT_TYPES = ACT_BASE_MENU_OPTIONS + 1,
            ACT_OPTIONS_TRACE_LEVEL = ACT_BASE_MENU_OPTIONS + 2,
            ACT_OPTIONS_WARN_LEVEL = ACT_BASE_MENU_OPTIONS + 3,
            ACT_OPTIONS_WARN_AS_ERROR = ACT_BASE_MENU_OPTIONS + 4,
            ACT_OPTIONS_SHOW_INPUT = ACT_BASE_MENU_OPTIONS + 5,
            ACT_OPTIONS_SHOW_CODE = ACT_BASE_MENU_OPTIONS + 6,
            ACT_OPTIONS_SHOW_OUTPUT = ACT_BASE_MENU_OPTIONS + 7,
            ACT_OPTIONS_SHOW_TYPES = ACT_BASE_MENU_OPTIONS + 8,
            ACT_OPTIONS_MAX_STRING = ACT_BASE_MENU_OPTIONS + 9,
            ACT_OPTIONS_MAX_ELEMS = ACT_BASE_MENU_OPTIONS + 10,

            ACT_MENU_HELP_LANG_GUIDE = ACT_BASE_MENU_HELP + 1,
            ACT_MENU_HELP_PRI_SYNTAX = ACT_BASE_MENU_HELP + 2,
            ACT_MENU_HELP_META_SYNTAX = ACT_BASE_MENU_HELP + 3,
            ACT_MENU_HELP_CUSTOM_SYNTAX = ACT_BASE_MENU_HELP + 4,
            ACT_MENU_HELP_PRAGMA = ACT_BASE_MENU_HELP + 5,
            ACT_MENU_HELP_LICENSE = ACT_BASE_MENU_HELP + 6,
            ACT_MENU_HELP_ABOUT = ACT_BASE_MENU_HELP + 7,

            STATUS_READY = 0, STATUS_COMPILE = 1, STATUS_EXECUTE = 2,
            STATUS_WARNING = 3, STATUS_ERROR = 4, STATUS_NONE = 5,

            STYLE_NORMAL = 0, STYLE_INPUT = 1, STYLE_CODE = 2, STYLE_RESULT = 3,
            STYLE_WARNING = 4, STYLE_ERROR = 5;

    // Quiet switch
    boolean QUIET = false;

    // Resources for the shell
    private ResourceBundle rsrc;
    private GuiResource _guiResource;

    private String[] statusMessages;
    private Icon[] statusIcons;
    private Image frmIcon;
    private SimpleAttributeSet[] styles;
    // output names for menu commands
    private String osName, thisName, pragmaName, removeName, includeName,
            ansName;

    // Event control tables
    private boolean active = false; // true iff the shell is running
    private HashMap actionTable = new HashMap(61);// source object -> action
                                                  // code
    private HashMap hintTable = new HashMap(61); // source object -> menu item
                                                 // hint
    private HashMap menuTable = new HashMap(61); // action code -> menu item
    private KeyStroke key_IPRET, key_PREV, key_NEXT, key_HISTORY; // command
                                                                  // line config

    // The number of command editors
    private final int editorCount;

    // The number of lines in each editor
    private int editorSize;

    // GUI state controls
    private ActionListener menuActionListener;
    private ChangeListener menuChangeListener;
    private MenuListener menuListener;
    private MultipleUndoManager undo = new MultipleUndoManager();
    private MenuStateController menuCtrl;
    private CommandKeyListener cmdLineCtrl;

    // the listeners for the GUI
    private Collection _listeners = new HashSet(8);

    private boolean changeFilter = false; // for menuChangeListener
    private String originalStatus = null;
    private String lastHint = null;

    // Interpreter and environment objects
    private HojoRuntime runtime;
    private HojoInterpreter ipret;
    private HojoCompiler comp;
    private HojoLexer lex;
    private HojoSyntax stx;
    private OsInterface os;
    private ShellObserver obs;
    private ControlThread control;

    // The components of the shell
    private Container parent;
    private JFrame frm;
    private JInternalFrame iFrm;
    private Container frame;
    private JMenuBar menu;
    private StatusBar statusBar;
    private JTabbedPane editorPane;
    private JTextComponent[] editors;
    private JTextComponent commandLine;
    private JTextPane outputLog;
    private AutoScrollPane outputScroll;
    private JTextPane errorLog;
    private AutoScrollPane errorScroll;
    private JSplitPane outputSplit;
    private Splash splash = null;

    private JMenuItem interruptMenuItem;
    private JFileChooser fchooser;

    public HojoShell() {
        this(null, null, null, null, true);
    }

    public HojoShell(HojoSyntax stx) {
        this(new HojoInterpreter(stx), null, null, null, true);
    }

    public HojoShell(HojoInterpreter ipret) {
        this(ipret, null, null, null, true);
    }

    public HojoShell(HojoInterpreter ipret, HojoRuntime runtime) {
        this(ipret, runtime, null, null, true);
    }

    public HojoShell(HojoInterpreter ipret, HojoRuntime runtime,
            ResourceBundle rsrc) {
        this(ipret, runtime, rsrc, null, true);
    }

    public HojoShell(HojoInterpreter ipret, HojoRuntime runtime,
            ResourceBundle rsrc, JComponent parent) {
        this(ipret, runtime, rsrc, parent, true);
    }

    /* ------------------------- Constructor / GUI ------------------------- */

    public HojoShell(HojoInterpreter ipret, HojoRuntime runtime,
            ResourceBundle rsrc, Container parent, boolean showSplash) {
        // Get the resource bundle to be used
        if (rsrc == null) {
            rsrc = ResourceBundle.getBundle(
                    "org/xodonex/hojo/resource/HojoShell",
                    Locale.getDefault());
        }
        this.rsrc = rsrc;
        _guiResource = new GuiResource(this.rsrc.getLocale(), this.rsrc,
                null, null, null);

        // Show the splash, if not disabled
        if (showSplash) {
            showSplash(0, true);
        }

        // Load the status messages
        statusMessages = new String[] {
                rsrc.getString("status.ready"),
                rsrc.getString("status.compile"),
                rsrc.getString("status.execute"),
                rsrc.getString("status.singleWarning"),
                rsrc.getString("status.singleError"),
                rsrc.getString("status.noProblem"),
                rsrc.getString("status.summary"),
                rsrc.getString("status.multiError"),
                rsrc.getString("status.multiWarning")
        };

        // Load the status icons
        statusIcons = new Icon[6];
        String base = "org/xodonex/hojo/resource/Status";
        URL url = findResource(base + "Ready.gif");
        statusIcons[STATUS_READY] = new ImageIcon(url);
        url = findResource(base + "Compile.gif");
        statusIcons[STATUS_COMPILE] = new ImageIcon(url);
        url = findResource(base + "Execute.gif");
        statusIcons[STATUS_EXECUTE] = new ImageIcon(url);
        url = findResource(base + "Warning.gif");
        statusIcons[STATUS_WARNING] = new ImageIcon(url);
        url = findResource(base + "Error.gif");
        statusIcons[STATUS_ERROR] = new ImageIcon(url);
        url = findResource(base + "None.gif");
        statusIcons[STATUS_NONE] = new ImageIcon(url);

        // Create the menu control
        menuActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
                Object src = ev.getSource();
                Integer ID = (Integer)actionTable.get(src);
                if (ID == null) {
                    return;
                }
                invokeAction(src, ID.intValue());
            }
        };

        // create the hint control
        menuChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!changeFilter) {
                    return;
                }

                lastHint = (String)hintTable.get(e.getSource());
                if (lastHint != null) {
                    statusBar.setText(0, lastHint);
                }
            }
        };

        menuListener = new MenuListener() {
            @Override
            public void menuCanceled(MenuEvent e) {
                String currentMsg = statusBar.getText(0);
                if (currentMsg.equals(lastHint)) {
                    // restore the status message only the text is the last hint
                    // displayed
                    statusBar.setText(0,
                            originalStatus == null ? "" : originalStatus);
                    originalStatus = null;
                    lastHint = null;
                }
                changeFilter = false;
            }

            @Override
            public void menuDeselected(MenuEvent e) {
                menuCanceled(e);
            }

            @Override
            public void menuSelected(MenuEvent e) {
                originalStatus = statusBar.getText(0);
                changeFilter = true;
            }
        };

        // Create the menu bar; the actionTable and menuTable are also created
        Font menuFont = createFont("menu.font");

        menu = new JMenuBar();
        menu.add(createMenu("menu.file",
                new String[] { "include", "loadScript", "saveScript", null,
                        "loadValue", "saveValue", "saveVars", null,
                        "chdir", null, "quit" },
                ACT_BASE_MENU_FILE, true, menuFont));
        menu.add(createEditMenu(menuFont));
        menu.add(createMenu("menu.command",
                new String[] { "compile", "run", "interrupt", null,
                        "showTranscript", "showSplit", "showErrorLog", null,
                        "clearTranscript", "resetSyntax", "removeVar",
                        "clearVars" },
                ACT_BASE_MENU_COMMAND, true, menuFont));
        menu.add(createMenu("menu.options",
                new String[] { "strictTypes", null, "traceLevel", "warnLevel",
                        "warnAsError", null,
                        "showInput", "showCode", "showOutput", "showTypes",
                        null,
                        "maxString", "maxElems" },
                ACT_BASE_MENU_OPTIONS, true, menuFont));
        menu.add(createMenu("menu.help",
                new String[] { "langGuide", null,
                        "priSyntax", "metaSyntax", "customSyntax", "pragma",
                        null, "license", null, "about" },
                ACT_BASE_MENU_HELP, true, menuFont));

        // Save a reference to the 'interrupt' menu item, and disable it
        interruptMenuItem = ((JMenuItem)menuTable.remove(
                new Integer(ACT_MENU_COMMAND_INTERRUPT)));
        interruptMenuItem.setEnabled(false);

        // Remove from the menuTable every menu item which are not affected by
        // active commands (ACT_MENU_EDIT_xxx are already excluded)
        menuTable.remove(new Integer(ACT_MENU_FILE_QUIT));
        menuTable.remove(new Integer(ACT_MENU_COMMAND_SHOW_TRANSCRIPT));
        menuTable.remove(new Integer(ACT_MENU_COMMAND_SHOW_SPLIT));
        menuTable.remove(new Integer(ACT_MENU_COMMAND_SHOW_ERROR_LOG));
        menuTable.remove(new Integer(ACT_MENU_HELP_LANG_GUIDE));
        menuTable.remove(new Integer(ACT_MENU_HELP_PRI_SYNTAX));
        menuTable.remove(new Integer(ACT_MENU_HELP_META_SYNTAX));
        menuTable.remove(new Integer(ACT_MENU_HELP_PRAGMA));
        menuTable.remove(new Integer(ACT_MENU_HELP_LICENSE));
        menuTable.remove(new Integer(ACT_MENU_HELP_ABOUT));

        // Create key bindings for the command line editor.
        String s;
        key_IPRET = getKeyConfig(rsrc.getString(s = "cmdline.xeq"), s);
        key_NEXT = getKeyConfig(rsrc.getString(s = "cmdline.next"), s);
        key_PREV = getKeyConfig(rsrc.getString(s = "cmdline.prev"), s);
        key_HISTORY = getKeyConfig(rsrc.getString(s = "cmdline.history"), s);

        // Create the main frame
        String frmTitle = MessageFormat.format(rsrc.getString("title"),
                new Object[] { "" + Version.VERSION, Version.REVISION });
        frmIcon = Toolkit.getDefaultToolkit().createImage(
                findResource("org/xodonex/hojo/resource/ShellIcon.gif"));

        Container cpane;
        if ((this.parent = parent) != null) {
            // Create a JInternalFrame
            frame = iFrm = new JInternalFrame(frmTitle, true, false, false,
                    false);
            iFrm.setFrameIcon(new ImageIcon(frmIcon));
            iFrm.setJMenuBar(menu);
            frm = null;
            cpane = iFrm.getContentPane();
        }
        else {
            // Create a JFrame
            frame = frm = new JFrame();
            frm.setTitle(frmTitle);
            frm.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            frm.setIconImage(frmIcon);
            frm.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    invokeAction(null, ACT_MENU_FILE_QUIT);
                }
            });
            frm.setJMenuBar(menu);

            _guiResource.setMainFrame(frm);

            iFrm = null;
            cpane = frm.getContentPane();
        }

        // Load general preferences
        int editorCols = ConvertUtils.toInt(rsrc.getString("columns"));
        Font font = createFont("font");

        // Create the status bar
        Font statusFont = createFont("status.font");
        s = rsrc.getString("status.foreground").trim();
        Color sfg = null;
        if (s.length() > 0) {
            sfg = new Color(ConvertUtils.toInt(s));
        }
        statusBar = new StatusBar(new int[] { -15, 15, 0, 0 }, 32,
                statusFont, sfg, null);
        statusBar.setIcon(2, statusIcons[STATUS_READY],
                statusMessages[STATUS_READY]);
        statusBar.setIcon(3, statusIcons[STATUS_NONE],
                statusMessages[STATUS_NONE]);
        statusBar.setText(0, statusMessages[STATUS_READY]);
        statusBar.setText(1, "");

        // Create the editor windows
        editorPane = new JTabbedPane(SwingConstants.BOTTOM);
        editorCount = ConvertUtils.toInt(rsrc.getString("editor.count"));
        editorSize = ConvertUtils.toInt(rsrc.getString("editor.rows"));
        editors = new JTextComponent[editorCount + 1];
        String buftext = rsrc.getString("editor.text");
        String buftip = rsrc.getString("editor.tooltip");
        int undoSize = ConvertUtils.toInt(rsrc.getString("editor.undoSize"));
        String[] dummyS = new String[1];
        JTextComponent jtc;
        for (int i = 0; i <= editorCount; i++) {
            if (i == 0) {
                jtc = editors[i] = new JTextField(editorCols);
            }
            else {
                jtc = editors[i] = new JTextArea(editorSize, editorCols);
                undo.addDocument(jtc.getDocument(), undoSize);
            }
            jtc.setFont(font);
            if (i == 0) {
                jtc.setToolTipText(MessageFormat.format(
                        rsrc.getString("cmdline.tooltip"), new Object[] {
                                keyStroke2String(key_IPRET),
                                keyStroke2String(key_NEXT),
                                keyStroke2String(key_PREV),
                                keyStroke2String(key_HISTORY)
                        }));
                jtc.addKeyListener(
                        cmdLineCtrl = new CommandKeyListener(ConvertUtils.toInt(
                                rsrc.getString("cmdline.history.size"))));
                commandLine = jtc;

                jtc.setMaximumSize(jtc.getPreferredSize());
                Container c = new JPanel(new SingleCenterLayout(false));
                c.add(jtc);
                editorPane.addTab(rsrc.getString("cmdline.text"), null, c);
                // jtc.requestDefaultFocus();
            }
            else {
                JTextArea area = (JTextArea)jtc;
                area.setLineWrap(false);
                area.setToolTipText(
                        MessageFormat.format(buftip, (Object[])dummyS));
                dummyS[0] = "" + i;
                editorPane.addTab(
                        MessageFormat.format(buftext, (Object[])dummyS), null,
                        new JScrollPane(area));
            }
        }
        editorPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int idx = editorPane.getSelectedIndex();
                undo.activateDocument(
                        idx == 0 ? null : editors[idx].getDocument());
                menuCtrl.toggleEditorState(idx == 0);
            }
        });

        // Create the output/error log styles
        styles = new SimpleAttributeSet[6];
        SimpleAttributeSet attrib = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrib, font.getFamily());
        StyleConstants.setFontSize(attrib, font.getSize());
        StyleConstants.setItalic(attrib, false);
        StyleConstants.setBold(attrib, false);
        s = rsrc.getString("transcript.background");
        StyleConstants.setBackground(attrib,
                (s.length() == 0) ? Color.white
                        : new Color(ConvertUtils.toInt(s)));
        StyleConstants.setForeground(attrib, Color.black);
        styles[STYLE_NORMAL] = createStyle(new SimpleAttributeSet(attrib),
                "transcript.output");
        styles[STYLE_CODE] = createStyle(new SimpleAttributeSet(attrib),
                "transcript.code");
        styles[STYLE_INPUT] = createStyle(new SimpleAttributeSet(attrib),
                "transcript.input");
        styles[STYLE_RESULT] = createStyle(new SimpleAttributeSet(attrib),
                "transcript.result");
        styles[STYLE_WARNING] = createStyle(new SimpleAttributeSet(attrib),
                "errors.warning");
        styles[STYLE_ERROR] = createStyle(new SimpleAttributeSet(attrib),
                "errors.error");

        // Create the error log and output log windows
        errorLog = new JTextPane(new DummyDocument());
        errorLog.setEditable(false);
        errorLog.setToolTipText(rsrc.getString("errors.tooltip"));
        errorScroll = new AutoScrollPane(errorLog);

        outputLog = new JTextPane(new DummyDocument());
        outputLog.setEditable(false);
        outputLog.setToolTipText(rsrc.getString("transcript.tooltip"));
        outputScroll = new AutoScrollPane(outputLog);

        // Create a file chooser; this will fail if read access to the file
        // system is not granted
        try {
            fchooser = new JFileChooser();
            // Create the file filters to be used
            String[] filters = StringUtils.split(rsrc.getString("filters"),
                    '\n');
            for (int i = 0; i < filters.length; i++) {
                String[] descr = StringUtils
                        .split(rsrc.getString(filters[i].trim()), '\n');
                fchooser.addChoosableFileFilter(
                        new PatternFilechooserFileFilter(
                                descr[0].trim(),
                                ConvertUtils.toPattern(descr[1].trim())));
            }
        }
        catch (SecurityException e) {
            // permanently disable the file-related menus which use the file
            // chooser.
            fchooser = null;
            ((JMenuItem)menuTable
                    .remove(new Integer(ACT_MENU_FILE_LOAD_SCRIPT)))
                            .setEnabled(false);
            ((JMenuItem)menuTable
                    .remove(new Integer(ACT_MENU_FILE_SAVE_SCRIPT)))
                            .setEnabled(false);
            ((JMenuItem)menuTable.remove(new Integer(ACT_MENU_FILE_INCLUDE)))
                    .setEnabled(false);
            ((JMenuItem)menuTable
                    .remove(new Integer(ACT_MENU_FILE_LOAD_VALUE)))
                            .setEnabled(false);
            ((JMenuItem)menuTable
                    .remove(new Integer(ACT_MENU_FILE_SAVE_VALUE)))
                            .setEnabled(false);
            ((JMenuItem)menuTable.remove(new Integer(ACT_MENU_FILE_CHDIR)))
                    .setEnabled(false);
            ((JMenuItem)menuTable.remove(new Integer(ACT_MENU_FILE_SAVE_VARS)))
                    .setEnabled(false);
        }

        // create the menu control and initialize it
        menuCtrl = new MenuStateController();
        menuCtrl.setState(true, true);

        // Add the components to the frame
        cpane.setLayout(new BorderLayout());

        outputSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                outputScroll, errorScroll);
        outputSplit.setEnabled(true);
        outputSplit.setDividerSize(4);
        // outputSplit.setOneTouchExpandable(true);

        JPanel p = new JPanel(new BorderLayout());
        p.add(outputSplit, BorderLayout.CENTER);
        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBorder(BorderFactory.createEmptyBorder(4, 2, 2, 2));
        p2.add(editorPane);
        p.add(p2, BorderLayout.SOUTH);
        p.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        cpane.add(statusBar, BorderLayout.SOUTH);
        cpane.add(p, BorderLayout.CENTER);

        // create the control objects
        this.ipret = (ipret == null) ? new HojoInterpreter(HojoSyntax.DEFAULT)
                : ipret;
        comp = this.ipret.getCompiler();
        comp.strictTypeCheck(0);
        lex = comp.getLexer();
        stx = comp.getSyntax();
        os = this.ipret.getOsInterface();
        os.setGuiResource(_guiResource);

        StringUtils.Format f = (StringUtils.Format)this.comp
                .getStandardFormat().clone();
        f.setTypeIndicator(" : ");
        this.obs = new ShellObserver(f, editorCols);
        if (runtime == null) {
            this.runtime = new HojoRuntime();
        }
        else {
            this.runtime = runtime;
        }

        this.ipret.setObserver(this.obs);
        this.ipret.setup(this.runtime);
        control = new ControlThread();

        // Save these names in order to produce pseudocommands from menu actions
        if ((thisName = stx.reserved[RES_THIS - RES_BASE_ID]) == null) {
            thisName = "this";
        }
        if ((osName = stx.standardLiterals[3]) == null) {
            osName = "os";
        }
        if ((pragmaName = stx.metaSyntax[META_PRAGMA - META_BASE_ID]) == null) {
            pragmaName = "" + stx.META + "pragma";
        }
        else {
            pragmaName = "" + stx.META + pragmaName;
        }
        if ((includeName = stx.metaSyntax[META_INCLUDE
                - META_BASE_ID]) == null) {
            includeName = "" + stx.META + "include";
        }
        else {
            includeName = "" + stx.META + includeName;
        }
        if ((removeName = stx.metaSyntax[META_REMOVE - META_BASE_ID]) == null) {
            removeName = "" + stx.META + "remove";
        }
        else {
            removeName = "" + stx.META + removeName;
        }
        if ((ansName = stx.metaSyntax[META_ANS - META_BASE_ID]) == null) {
            ansName = "ans";
        }
    }

    private String keyStroke2String(KeyStroke ks) {
        if (ks == null) {
            return "";
        }
        String mod = KeyEvent.getKeyModifiersText(ks.getModifiers());
        String key = KeyEvent.getKeyText(ks.getKeyCode());
        return mod.length() == 0 ? key : mod + " " + key;
    }

    // find a resource
    private URL findResource(String name) {
        URL result = getClass().getClassLoader().getResource(name);
        if (result == null) {
            throw new RuntimeException("Can't locate resource: " + name);
        }
        return result;
    }

    // create a font from a resource name
    private Font createFont(String rsrcName) {
        String s = rsrc.getString(rsrcName + ".size");
        int size = s.length() > 0 ? ConvertUtils.toInt(s) : 12;
        int attribs = Font.PLAIN;

        if (((s = rsrc.getString(rsrcName + ".bold")).length() > 0) &&
                ConvertUtils.toBool(s)) {
            attribs |= Font.BOLD;
        }
        if (((s = rsrc.getString(rsrcName + ".italic")).length() > 0) &&
                ConvertUtils.toBool(s)) {
            attribs |= Font.ITALIC;
        }

        if ((s = rsrc.getString(rsrcName + ".type")).length() == 0) {
            s = "monospaced";
        }
        return new Font(s, attribs, size);
    }

    // create an output style from thre resource file
    private SimpleAttributeSet createStyle(SimpleAttributeSet set,
            String name) {
        String s = rsrc.getString(name + ".size");
        if (s.length() > 0) {
            StyleConstants.setFontSize(set, ConvertUtils.toInt(s));
        }
        s = rsrc.getString(name + ".color");
        if (s.length() > 0) {
            StyleConstants.setForeground(set, new Color(ConvertUtils.toInt(s)));
        }
        s = rsrc.getString(name + ".italic");
        if (s.length() > 0) {
            StyleConstants.setItalic(set, ConvertUtils.toBool(s));
        }
        s = rsrc.getString(name + ".bold");
        if (s.length() > 0) {
            StyleConstants.setBold(set, ConvertUtils.toBool(s));
        }
        return set;
    }

    private String getMenuDef(String rsrcName, KeyStroke[] keys) {
        String s = rsrc.getString(rsrcName);
        int idx = s.indexOf('\n');

        if (idx < 0) {
            keys[0] = keys[1] = null;
            return s;
        }

        String result = s.substring(0, idx).trim();
        s = s.substring(idx + 1);
        idx = s.indexOf('\n');
        if (idx < 0) {
            keys[1] = null;
        }
        else {
            String accel = s.substring(idx + 1);
            s = s.substring(0, idx);
            keys[1] = getKeyConfig(accel, rsrcName);
        }

        keys[0] = getKeyConfig(s, rsrcName);
        return result;
    }

    // create a menu item given a property name
    private JMenuItem createMenuItem(String pname, int ID,
            boolean addToTbl, Font font) {
        KeyStroke[] keyCfg = new KeyStroke[2];
        String name = getMenuDef(pname, keyCfg);

        JMenuItem item = new JMenuItem(name);
        item.setFont(font);
        if (keyCfg[0] != null) {
            item.setMnemonic(keyCfg[0].getKeyCode());
        }
        if (keyCfg[1] != null) {
            item.setAccelerator(keyCfg[1]);
        }

        if (ID > 0) {
            // ID <= 0 signfies that the default action handler should be
            // bypassed
            Integer _id = new Integer(ID);
            actionTable.put(item, _id);
            if (addToTbl) {
                menuTable.put(_id, item);
            }
            item.addActionListener(menuActionListener);
        }

        String hint = rsrc.getString(pname + ".hint").trim();
        if (hint.length() > 0) {
            hintTable.put(item, hint);
        }

        item.addChangeListener(menuChangeListener);
        return item;
    }

    private KeyStroke getKeyConfig(String s, String pname) {
        String s_ = s.trim();
        if (s_.length() == 0) {
            return null;
        }
        try {
            KeyStroke stroke = KeyStroke.getKeyStroke(s_);
            if (stroke == null) {
                throw new NullPointerException();
            }
            return stroke;
        }
        catch (Throwable t) {
            throw new IllegalArgumentException(pname + " = " + s);
        }
    }

    // create a menu item given a property name
    private JMenu createMenu(String pname, Font font) {
        KeyStroke[] keyCfg = new KeyStroke[2];
        String name = getMenuDef(pname, keyCfg);

        JMenu result = new JMenu(name);
        result.setFont(font);
        result.addMenuListener(menuListener);
        if (keyCfg[0] != null) {
            result.setMnemonic(keyCfg[0].getKeyCode());
        }
        return result;
    }

    private JMenu addMenuDefs(JMenu base, String pname, String[] items, int ID,
            boolean addToTbl, Font font) {
        JMenuItem item;

        int nextID = ID + 1;
        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                base.addSeparator();
            }
            else {
                item = createMenuItem(pname + '.' + items[i], nextID++,
                        addToTbl, font);
                base.add(item);
            }
        }
        return base;
    }

    private JMenu addMenuDefs(JMenu base, String pname, String[] items,
            Action[] actions, Font font) {
        JMenuItem item;

        for (int i = 0; i < items.length; i++) {
            if (items[i] == null) {
                base.addSeparator();
                continue;
            }

            item = createMenuItem(pname + '.' + items[i], -1, false, font);
            item.setEnabled(actions[i].isEnabled());
            actions[i].putValue(MultipleUndoManager.ENABLE_COMPONENT, item);
            item.addActionListener(actions[i]);
            base.add(item);
        }
        return base;
    }

    private JMenu createMenu(String pname, String[] items, int ID,
            boolean addToTbl, Font font) {
        JMenu base = createMenu(pname, font);
        return addMenuDefs(base, pname, items, ID, addToTbl, font);
    }

    private JMenu createEditMenu(Font font) {
        final String pname = "menu.edit";
        JMenu result = createMenu(pname, font);

        addMenuDefs(result, pname, new String[] { "undo", "redo" },
                new Action[] { undo.UNDO, undo.REDO }, font);
        result.addSeparator();
        return addMenuDefs(result, pname, new String[] {
                "cut", "copy", "paste", "selectAll", null,
                "current", "next", "prev", null, "size" },
                ACT_BASE_MENU_EDIT, false, font);
    }

    public synchronized void showSplash(long delay) {
        showSplash(delay, !active);

    }

    private synchronized void showSplash(long delay, boolean saveRef) {
        if (splash != null) {
            splash.remove();
            splash = null;
        }

        URL imageUrl = findResource("org/xodonex/hojo/resource/Splash.jpg");
        if (imageUrl == null) {
            System.err.println("FIXME: resource not found!");
        }
        else {
            Splash s = new Splash(
                    Toolkit.getDefaultToolkit().createImage(imageUrl),
                    delay < 0
                            ? ConvertUtils.toInt(rsrc.getString("splash.delay"))
                            : delay,
                    true);
            if (saveRef) {
                splash = s;
            }
        }
    }

    private String getInput(String legalName, String[] params) {
        return JOptionPane.showInputDialog(frame,
                MessageFormat.format(rsrc.getString("legalvalue.text"),
                        new Object[] {
                                MessageFormat.format(rsrc.getString(legalName),
                                        (Object[])params)
                        }));
    }

    private void doPragma(boolean internal, String name,
            String legal, String[] args, String value) {
        if (value == null) {
            value = getInput(legal, args);
        }
        if (value == null) {
            return;
        }
        String cmd = pragmaName + ' ' +
                (internal ? "HOJO " : "") + name + " " + stx.quotes[0] +
                StringUtils.toJavaString(value) + stx.quotes[0];
        cmdLineCtrl.addCommand(cmd);
        obs.started(new CommandReader(cmd));
        ipret.setPragma(internal, name, value);
        obs.finished();
    }

    private static String[] getSorted(Collection c) {
        String[] result = (String[])c.toArray(new String[c.size()]);
        Arrays.sort(result, Collator.getInstance());
        return result;
    }

    // Main event handler
    private void invokeAction(Object source, int code) {
        if (code == ACT_MENU_FILE_QUIT) {
            if (DialogUtils.showConfirmationDialog(
                    rsrc.getString("confirm.quit"),
                    ButtonConstants.BTNS_YES_NO,
                    _guiResource) == ButtonConstants.BTN_YES) {
                stop();
            }
            return;
        }
        else if (code == ACT_MENU_COMMAND_INTERRUPT) {
            control.doInterrupt();
            return;
        }

        int idx;
        File f = null;
        String s = null;
        Object obj;
        StringBuffer buf;
        final String title = (source instanceof JMenuItem)
                ? ((JMenuItem)source).getText()
                : null;

        switch (code) {
        case ACT_MENU_FILE_CHDIR:
            f = os.pwd();
            fchooser.setFileFilter(fchooser.getAcceptAllFileFilter());
            fchooser.setCurrentDirectory(f);
            fchooser.setDialogTitle(title);
            fchooser.setMultiSelectionEnabled(false);
            fchooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fchooser.showOpenDialog(frm) == JFileChooser.APPROVE_OPTION) {
                try {
                    f = fchooser.getSelectedFile();
                    obs.started(new CommandReader(osName +
                            stx.operators[OP_IDX_DOT] + "cd" +
                            stx.punctuators[PCT_IDX_LPAREN] +
                            StringUtils.any2String(f.toString(),
                                    comp.getStandardFormat(), "")
                            +
                            stx.punctuators[PCT_IDX_RPAREN]));
                    f = os.cd(f);
                    ipret.setLastResult(f);
                    obs.commandResult(f);
                }
                catch (Throwable _t) {
                    obs.handleError(HojoException.wrap(_t));
                }
                obs.finished();
            }
            break;
        case ACT_MENU_FILE_INCLUDE:
        case ACT_MENU_FILE_LOAD_SCRIPT:
        case ACT_MENU_FILE_SAVE_SCRIPT:
        case ACT_MENU_FILE_LOAD_VALUE:
        case ACT_MENU_FILE_SAVE_VALUE:
        case ACT_MENU_FILE_SAVE_VARS:
            f = os.pwd();
            fchooser.setCurrentDirectory(f);
            fchooser.setDialogTitle(title);
            fchooser.setMultiSelectionEnabled(false);
            fchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (((code == ACT_MENU_FILE_LOAD_VALUE
                    || code == ACT_MENU_FILE_INCLUDE ||
                    code == ACT_MENU_FILE_LOAD_SCRIPT)
                            ? fchooser.showOpenDialog(frm)
                            : fchooser.showSaveDialog(
                                    frm)) == JFileChooser.APPROVE_OPTION) {

                f = fchooser.getSelectedFile();
                String cmd;
                switch (code) {
                case ACT_MENU_FILE_LOAD_VALUE:
                case ACT_MENU_FILE_SAVE_VALUE:
                case ACT_MENU_FILE_SAVE_VARS:
                    cmd = osName + stx.operators[OP_IDX_DOT] +
                            (code == ACT_MENU_FILE_LOAD_VALUE ? "load" : "save")
                            +
                            stx.punctuators[PCT_IDX_LPAREN] +
                            (code == ACT_MENU_FILE_LOAD_VALUE ? ""
                                    : (code == ACT_MENU_FILE_SAVE_VARS
                                            ? stx.reserved[RES_NEW
                                                    - RES_BASE_ID] +
                                                    " java"
                                                    + stx.operators[OP_IDX_DOT]
                                                    + "util" +
                                                    stx.operators[OP_IDX_DOT]
                                                    + "HashMap" +
                                                    stx.punctuators[PCT_IDX_LPAREN]
                                                    + thisName +
                                                    stx.punctuators[PCT_IDX_RPAREN]
                                            : stx.punctuators[PCT_IDX_LPAREN] +
                                                    "java"
                                                    + stx.operators[OP_IDX_DOT]
                                                    + "io" +
                                                    stx.operators[OP_IDX_DOT]
                                                    + "Serializable" +
                                                    stx.punctuators[PCT_IDX_RPAREN]
                                                    +
                                                    ansName)
                                            +
                                            stx.punctuators[PCT_IDX_DELIMITER]
                                            + ' ')
                            +
                            StringUtils.any2String(f.toString(),
                                    comp.getStandardFormat(), "")
                            +
                            stx.punctuators[PCT_IDX_RPAREN] +
                            stx.punctuators[PCT_IDX_SEPARATOR];
                    break;
                case ACT_MENU_FILE_INCLUDE:
                    cmd = includeName + " " +
                            stx.punctuators[PCT_IDX_LPAREN] +
                            "java" + stx.operators[OP_IDX_DOT] + "net" +
                            stx.operators[OP_IDX_DOT] + "URL" +
                            stx.punctuators[PCT_IDX_RPAREN] +
                            stx.punctuators[PCT_IDX_LPAREN] +
                            "java" + stx.operators[OP_IDX_DOT] + "io" +
                            stx.operators[OP_IDX_DOT] + "File" +
                            stx.punctuators[PCT_IDX_RPAREN] +
                            StringUtils.any2String(f.toString(),
                                    comp.getStandardFormat(), "")
                            +
                            stx.punctuators[PCT_IDX_SEPARATOR];
                    break;
                default:
                    cmd = pragmaName + " " +
                            (code == ACT_MENU_FILE_LOAD_SCRIPT
                                    ? ShellObserver.PRAGMA_S_LOAD_SCRIPT
                                    : ShellObserver.PRAGMA_S_SAVE_SCRIPT)
                            + " " +
                            stx.punctuators[PCT_IDX_ARRAYSTART] +
                            editorPane.getSelectedIndex() +
                            stx.punctuators[PCT_IDX_DELIMITER] +
                            (code == ACT_MENU_FILE_LOAD_SCRIPT ? osName +
                                    stx.operators[OP_IDX_DOT] + "read" +
                                    stx.punctuators[PCT_IDX_LPAREN]
                                    : "")
                            +
                            StringUtils.any2String(f.toString(),
                                    comp.getStandardFormat(), "")
                            +
                            (code == ACT_MENU_FILE_LOAD_SCRIPT
                                    ? stx.punctuators[PCT_IDX_RPAREN]
                                    : "")
                            +
                            stx.punctuators[PCT_IDX_ARRAYEND] +
                            stx.punctuators[PCT_IDX_SEPARATOR];
                }

                errorLog.setText("");
                cmdLineCtrl.addCommand(cmd);
                control.q.put(
                        new Command(CMD_IPRET, false, new CommandReader(cmd)));
            }
            break;
        case ACT_MENU_EDIT_CUT:
            editors[editorPane.getSelectedIndex()].cut();
            break;
        case ACT_MENU_EDIT_COPY:
            editors[editorPane.getSelectedIndex()].copy();
            break;
        case ACT_MENU_EDIT_PASTE:
            editors[editorPane.getSelectedIndex()].paste();
            break;
        case ACT_MENU_EDIT_SELECT_ALL:
            editors[editorPane.getSelectedIndex()].selectAll();
            break;
        case ACT_MENU_EDIT_CURRENT:
            editors[editorPane.getSelectedIndex()].requestFocus();
            break;
        case ACT_MENU_EDIT_PREV:
            idx = editorPane.getSelectedIndex() - 1;
            idx = (idx < 0) ? editorCount : idx;
            editorPane.setSelectedIndex(idx);
            editors[idx].requestFocus();
            break;
        case ACT_MENU_EDIT_NEXT:
            idx = (editorPane.getSelectedIndex() + 1) % (editorCount + 1);
            editorPane.setSelectedIndex(idx);
            editors[idx].requestFocus();
            break;
        case ACT_MENU_EDIT_SIZE:
            try {
                int newLines = ConvertUtils.toInt(getInput("legalvalue.range",
                        new String[] { "1", "32" }));
                if (newLines >= 1 && newLines <= 32) {
                    editorSize = newLines;
                    for (int i = 1; i < editors.length; i++) {
                        ((JTextArea)editors[i]).setRows(editorSize);
                    }
                    if (editorCount > 0) {
                        idx = editorPane.getSelectedIndex();
                        editorPane.setSelectedIndex(2);
                        editorPane.validate();
                        editorPane.setSelectedIndex(idx);
                    }
                    frame.validate();
                    outputSplit.setDividerLocation(1.0);
                    frame.repaint();
                }
            }
            catch (Exception e) {
            }
            break;
        case ACT_MENU_COMMAND_COMPILE:
        case ACT_MENU_COMMAND_RUN:
            idx = editorPane.getSelectedIndex();
            errorLog.setText("");

            final String input = editors[idx].getText();
            StringReader inputReader = new CommandReader(input);
            Command cmd = new Command(
                    idx == 0 ? CMD_IPRET
                            : code == ACT_MENU_COMMAND_COMPILE ? CMD_COMPILE
                                    : CMD_RUN,
                    false, inputReader);
            control.q.put(cmd);
            break;
        case ACT_MENU_COMMAND_SHOW_TRANSCRIPT:
            outputSplit.setDividerLocation(1.0);
            break;
        case ACT_MENU_COMMAND_SHOW_SPLIT:
            outputSplit.setDividerLocation(0.5);
            break;
        case ACT_MENU_COMMAND_SHOW_ERROR_LOG:
            outputSplit.setDividerLocation(0.0);
            break;
        case ACT_MENU_COMMAND_CLEARTRANSCRIPT:
            if (DialogUtils.showConfirmationDialog(
                    rsrc.getString("confirm.clearTranscript"),
                    ButtonConstants.BTNS_YES_NO,
                    _guiResource) == ButtonConstants.BTN_YES) {
                outputLog.setText("");
                if (!QUIET) {
                    obs.getOutLogWriter().writeStyled(
                            Version.COPYRIGHT +
                                    obs.inputIndicator,
                            STYLE_NORMAL);
                }
            }
            break;
        case ACT_MENU_COMMAND_RESETSYNTAX:
            if (DialogUtils.showConfirmationDialog(
                    rsrc.getString("confirm.resetSyntax"),
                    ButtonConstants.BTNS_YES_NO,
                    _guiResource) == ButtonConstants.BTN_YES) {
                comp.resetSyntax();
            }
            break;
        case ACT_MENU_COMMAND_CLEAR_VARS:
            if (DialogUtils.showConfirmationDialog(
                    rsrc.getString("confirm.clearVars"),
                    ButtonConstants.BTNS_YES_NO,
                    _guiResource) == ButtonConstants.BTN_YES) {
                obs.started(new CommandReader(removeName + ' ' + thisName));
                ipret.setLastResult(obj = ipret.removeVar(thisName));
                obs.commandResult(obj);
                obs.finished();
                // doPragma(true, "clear", null, null, thisName);
            }
            break;
        case ACT_MENU_COMMAND_REMOVE_VAR:
            s = JOptionPane.showInputDialog(frame,
                    rsrc.getString("msg.removeVar"));
            if (s != null) {
                if (!s.equals(thisName) ||
                        (DialogUtils.showConfirmationDialog(
                                rsrc.getString("confirm.clearVars"),
                                ButtonConstants.BTNS_YES_NO,
                                _guiResource) == ButtonConstants.BTN_YES)) {
                    obs.started(new CommandReader(removeName + ' ' + s));
                    ipret.setLastResult(obj = ipret.removeVar(s));
                    obs.commandResult(obj);
                    obs.finished();
                    // doPragma(true, "clear", null, null, s);
                }
            }
            break;
        case ACT_OPTIONS_STRICT_TYPES:
            doPragma(true, "strictTypes", "legalvalue.range",
                    new String[] { "0", "2" }, null);
            break;
        case ACT_OPTIONS_TRACE_LEVEL:
            doPragma(false, DefaultHojoObserver.PRAGMA_S_TRACE_LEVEL,
                    "legalvalue.range", new String[] { "0", "4" }, null);
            break;
        case ACT_OPTIONS_WARN_LEVEL:
            doPragma(false, DefaultHojoObserver.PRAGMA_S_WARN_LEVEL,
                    "legalvalue.range", new String[] { "0", "4" }, null);
            break;
        case ACT_OPTIONS_WARN_AS_ERROR:
            doPragma(false, DefaultHojoObserver.PRAGMA_S_WARN_AS_ERROR,
                    "legalvalue.boolean", null, null);
            break;
        case ACT_OPTIONS_SHOW_INPUT:
            doPragma(false, DefaultHojoObserver.PRAGMA_S_SHOW_INPUT,
                    "legalvalue.boolean", null, null);
            break;
        case ACT_OPTIONS_SHOW_CODE:
            doPragma(false, DefaultHojoObserver.PRAGMA_S_SHOW_CODE,
                    "legalvalue.boolean", null, null);
            break;
        case ACT_OPTIONS_SHOW_OUTPUT:
            doPragma(false, DefaultHojoObserver.PRAGMA_S_SHOW_OUTPUT,
                    "legalvalue.boolean", null, null);
            break;
        case ACT_OPTIONS_SHOW_TYPES:
            doPragma(false, DefaultHojoObserver.PRAGMA_S_SHOW_TYPES,
                    "legalvalue.boolean", null, null);
            break;
        case ACT_OPTIONS_MAX_STRING:
            doPragma(false, DefaultHojoObserver.PRAGMA_S_MAX_STRING,
                    "legalvalue.type", new String[] { "java.lang.Integer" },
                    null);
            break;
        case ACT_OPTIONS_MAX_ELEMS:
            doPragma(false, DefaultHojoObserver.PRAGMA_S_MAX_ELEMS,
                    "legalvalue.type", new String[] { "java.lang.Integer" },
                    null);
            break;
        case ACT_MENU_HELP_PRI_SYNTAX:
        case ACT_MENU_HELP_META_SYNTAX:
        case ACT_MENU_HELP_CUSTOM_SYNTAX:
        case ACT_MENU_HELP_PRAGMA:
            final int width = ConvertUtils
                    .toInt(rsrc.getString("help.syntax.width"));
            ConvertUtils.toInt(rsrc.getString("help.syntax.height"));
            int cols = ConvertUtils
                    .toInt(rsrc.getString("help.syntax.columns"));
            final String text;

            switch (code) {
            case ACT_MENU_HELP_PRI_SYNTAX:
                text = stx.describeCompilerSyntax(
                        rsrc.getString("help.syntax.primary"),
                        rsrc.getString("help.syntax.false"),
                        rsrc.getString("help.syntax.true"),
                        rsrc.getString("help.syntax.range"), width / cols,
                        width);
                break;
            case ACT_MENU_HELP_META_SYNTAX:
                text = stx.describeInterpreterSyntax(
                        rsrc.getString("help.syntax.meta"),
                        width / cols, width);
                break;
            case ACT_MENU_HELP_CUSTOM_SYNTAX:
                ArrayList l = new ArrayList(64);
                String[] args = new String[5];

                comp.listMacros(l);
                args[0] = StringUtils.listColumns(null, getSorted(l), "", true,
                        width / cols, width);

                l.clear();
                comp.listCustomTypeIDs(l);
                String[] tmp = getSorted(l);
                buf = new StringBuffer(tmp.length * 20);
                for (int i = 0; i < tmp.length; i++) {
                    buf.append(tmp[i]);
                    if (i < tmp.length - 1) {
                        buf.append('\n');
                    }
                }
                args[1] = buf.toString();

                l.clear();
                comp.listPackagePrefixes(l);
                args[2] = StringUtils.listColumns(null, getSorted(l), "", true,
                        width / cols, width);

                l.clear();
                comp.listCustomOperators(l);
                args[3] = StringUtils.listColumns(null, getSorted(l), "", true,
                        width / cols, width);

                l.clear();
                comp.listCustomLiterals(l);
                args[4] = StringUtils.listColumns(null, getSorted(l), "", true,
                        width / cols, width);

                text = MessageFormat
                        .format(rsrc.getString("help.syntax.custom"),
                                (Object[])args);
                break;
            case ACT_MENU_HELP_PRAGMA:
                String msg = rsrc.getString("help.syntax.pragma");
                buf = new StringBuffer(256);
                ArrayList names = new ArrayList(16);
                ArrayList types = new ArrayList(16);
                ArrayList comments = new ArrayList(16);

                int split = ipret.listDirectives(names, types, comments);
                obs.listDirectives(names, types, comments);

                int size = names.size();
                for (int i = 0; i < size; i++) {
                    buf.append(MessageFormat.format(msg, new Object[] {
                            (String)names.get(i),
                            ReflectUtils.className2Java((Class)types.get(i)),
                            (s = (String)comments.get(i)) == null ? "" : s }));
                    if (i < size - 1) {
                        buf.append("\n\n");
                    }
                    if (i == split - 1) {
                        buf.append('\n');
                    }
                }

                text = buf.toString();
                break;
            default:
                text = null;
            }

            new TextDialog(_guiResource).show(text, title);
            break;
        case ACT_MENU_HELP_LICENSE:
            String msg;
            try {
                StringBuffer bf = new StringBuffer();
                URL url = findResource("COPYING");
                InputStream istream = url.openStream();
                OsInterface.read(bf, istream);
                istream.close();
                msg = bf.toString();
            }
            catch (Exception e) {
                msg = StringUtils.createTrace(e);
            }

            new TextDialog(_guiResource).show(msg, title);
            break;
        case ACT_MENU_HELP_LANG_GUIDE:
            try {
                BrowserWindow dlg = new BrowserWindow(_guiResource);
                URL u = findResource("org/xodonex/hojo/resource/LangGuide.htm");
                if (u != null) {
                    u = new URL(u, "#top");
                }
                dlg.show(u, title, frame.getWidth(), frame.getHeight());
            }
            catch (Exception e) {
            }
            break;
        case ACT_MENU_HELP_ABOUT:
            showSplash(0);
            break;
        default:
            throw new HojoException(null, HojoException.ERR_INTERNAL,
                    new String[] { "" + code }, null);
        }
    }

    public void start() {
        start(false);
    }

    public synchronized void start(boolean showSplash) {
        if (showSplash) {
            // a splash screen may be shown already by the constructor
            showSplash(-1, false);
        }

        // resize the frame according to the defined size
        final double w = ConvertUtils.toDouble(rsrc.getString("width"));
        final double h = ConvertUtils.toDouble(rsrc.getString("height"));

        // show the GUI, and set the initial appearance
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!QUIET) {
                        outputLog.getDocument().insertString(0,
                                Version.COPYRIGHT +
                                        obs.inputIndicator,
                                styles[STYLE_NORMAL]);
                    }
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
                if (frm != null) {
                    WindowManager.showWindow(frm, w, h, true);
                }
                else if (iFrm != null && parent != null) {
                    parent.add(iFrm);
                    Insets isets = parent.getInsets();
                    iFrm.setSize(parent.getWidth() - isets.left - isets.right,
                            parent.getHeight() - isets.top - isets.bottom);
                    iFrm.validate();
                    iFrm.setVisible(true);
                    try {
                        iFrm.setSelected(true);
                    }
                    catch (java.beans.PropertyVetoException e) {
                    }
                    parent.repaint();
                }
                outputSplit.setDividerLocation(1.0);
                editors[0].requestFocus();
                synchronized (HojoShell.this) {
                    if (splash != null) {
                        // hide the initial splash screen, if one was created
                        // by the constructor.
                        splash.remove();
                        splash = null;
                    }
                }
            }
        });

        active = true;
    }

    public synchronized boolean stop() {
        if (active) {
            control.q.clear();
            control.doInterrupt();
            control.q.put(new Command(CMD_STOP, false, null));
            try {
                synchronized (control) {
                    if (!control.finished) {
                        control.wait();
                    }
                }
            }
            catch (InterruptedException e) {
            }
            active = false;
            if (frm != null) {
                frm.dispose();
            }
            if (iFrm != null) {
                iFrm.dispose();
            }
            parent = null;
            for (Iterator i = _listeners.iterator(); i.hasNext();) {
                try {
                    ((Runnable)i.next()).run();
                }
                catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            _listeners = null;
            notifyAll();
            return true;
        }
        else {
            return false;
        }
    }

    public synchronized void waitFor() throws InterruptedException {
        if (active) {
            wait();
        }
    }

    public synchronized void addTerminationListener(Runnable r) {
        if (r == null) {
            throw new NullPointerException();
        }

        _listeners.add(r);
    }

    public synchronized boolean removeTerminationListener(Runnable r) {
        return _listeners.remove(r);
    }

    // Dummy document to circumvent Java bug 4244547
    private static class DummyDocument extends DefaultStyledDocument {
        private static final long serialVersionUID = 1L;

        DummyDocument() {
            super();
        }

        @Override
        public Color getBackground(AttributeSet attr) {
            return StyleConstants.getBackground(attr);
        }
    }

    /* ------------------------------ Control ------------------------------ */

    private class LogWriter extends Writer {
        private StringBuffer buf = new StringBuffer(64);
        private AutoScrollPane scroll;
        private JTextPane pane = null;
        private int currentStyle = STYLE_NORMAL;

        LogWriter(JTextPane pane, AutoScrollPane scroll) {
            this.scroll = scroll;
            this.pane = pane;
        }

        @Override
        public void close() {
            flush();
            buf = null;
            scroll = null;
            pane = null;
        }

        @Override
        public void flush() {
            flush(currentStyle, false);
        }

        public void flush(boolean doScroll) {
            flush(currentStyle, doScroll);
        }

        public void flush(final int style, final boolean doScroll) {
            if (!doScroll && buf.length() == 0) {
                return;
            }
            final String out = buf.toString();
            buf.setLength(0);

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        Document doc = pane.getDocument();
                        doc.insertString(doc.getLength(), out, styles[style]);
                        if (doScroll) {
                            scroll.scrollToMax(pane);
                        }
                    }
                    catch (BadLocationException ble) {
                        ble.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void write(char[] cbuf, int off, int len) {
            buf.append(cbuf, off, len);
        }

        public void writeStyled(String s, int style) {
            flush();
            buf.append(s);
            flush(style, false);
        }

        public void setStyle(int style) {
            currentStyle = style;
        }
    }

    // create compiler input from a string; toString() is overwritten in order
    // to let the observer print the input
    private class CommandReader extends StringReader {
        String input;

        CommandReader(String s) {
            super(s + '\n' + stx.punctuators[HojoConst.PCT_IDX_SEPARATOR]
                    + '\n');
            input = s;
        }

        @Override
        public String toString() {
            return input;
        }
    }

    private class ShellObserver extends DefaultHojoObserver {
        final String INDENT_INPUT = "\t";
        final String INDENT_CODE = INDENT_INPUT;
        final String INDENT_ERROR = INDENT_INPUT;
        final String INDENT_RESULT = "";

        final static String PRAGMA_S_LOAD_SCRIPT = "loadScript";
        final static String PRAGMA_S_SAVE_SCRIPT = "saveScript";

        int actionState;
        int errorState;
        @SuppressWarnings("unused")
        int includeCount;
        boolean clearOnError; // true iff doRecovery() should clear all input

        LogWriter outWriter, errWriter;
        BufferReader commandReader = null;
        @SuppressWarnings("unused")
        String statusMsg = null;

        ShellObserver(StringUtils.Format fmt, int cols) {
            super(fmt);
            setOutputWriter(new PrintWriter(
                    outWriter = new LogWriter(outputLog, outputScroll)));
            setErrorWriter(new PrintWriter(
                    errWriter = new LogWriter(errorLog, errorScroll)));
            setWarningWriter(null); // use the error writer for warnings

            // force the writer to show the correct style!
            errWriter.setStyle(STYLE_ERROR);
            try {
                errWriter.write(" ");
            }
            catch (IOException e) {
            }
            errorLog.setText("");

            String dividerString = rsrc.getString("transcript.dividerString");
            String dividerChar = rsrc.getString("transcript.dividerChar");
            if (dividerChar.length() > 0) {
                int l = dividerString.length();
                if (l >= cols) {
                    inputIndicator = dividerString + "\n";
                }
                else {
                    char[] cs = new char[cols + 1];
                    ArrayUtils.fill(cs, 0, cols, dividerChar.charAt(0));
                    cs[cols] = '\n';
                    if (l > 0) {
                        char[] middle = dividerString.toCharArray();
                        int ofs = cols / 2 - 1;
                        for (int i = 0; i < middle.length; i++) {
                            cs[ofs++] = middle[i];
                        }
                    }
                    inputIndicator = new String(cs);
                }
            }
            else {
                inputIndicator = dividerString + "\n";
            }
        }

        LogWriter getOutLogWriter() {
            return outWriter;
        }

        @SuppressWarnings("unused")
        LogWriter getErrLogWriter() {
            return errWriter;
        }

        @Override
        public boolean pragmaDirective(String id, Object value) {
            if (super.pragmaDirective(id, value)) {
                return true;
            }
            if (id.equals(PRAGMA_S_LOAD_SCRIPT)) {
                Object[] args = (Object[])ConvertUtils.toArray(value,
                        Object[].class);
                final int editor = ConvertUtils.toInt(args[0]);
                if (editor < 1 || editor > editorCount) {
                    throw new IllegalArgumentException("" + editor);
                }
                final String contents = ConvertUtils.toString(args[1]);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        editors[editor].setText(contents);
                    }
                });
                return true;
            }
            else if (id.equals(PRAGMA_S_SAVE_SCRIPT)) {
                Object[] args = (Object[])ConvertUtils.toArray(value,
                        Object[].class);
                final int editor = ConvertUtils.toInt(args[0]);
                if (editor < 1 || editor > editorCount) {
                    throw new IllegalArgumentException("" + editor);
                }
                final File dest;
                try {
                    dest = os.resolve(ConvertUtils.toString(args[1]));
                }
                catch (Exception e) {
                    throw HojoException.wrap(e);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            os.cat(editors[editor].getText(), dest, false);
                        }
                        catch (IOException e) {
                            editors[editor].setText(StringUtils.createTrace(e));
                        }
                    }
                });
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        public int listDirectives(Collection names, Collection types,
                Collection comments) {
            return super.listDirectives(names, types, comments) +
                    listDirectives(names, types, comments, rsrc,
                            new String[] {
                                    PRAGMA_S_LOAD_SCRIPT,
                                    PRAGMA_S_SAVE_SCRIPT
                            },
                            new Class[] {
                                    Object[].class,
                                    Object[].class
                            });
        }

        @Override
        public boolean started(Reader in) {
            super.reset();
            lex.setLineno(1);
            clearOnError = true;
            if (optShowInput) {
                outWriter.writeStyled(in.toString() + "\n", STYLE_INPUT);
            }
            menuCtrl.toggleMenuEnabledState(false);
            setState(STATUS_COMPILE, STATUS_NONE, true, -1);
            return true;
        }

        @Override
        public boolean includeStart(URL url) {
            // change error recovery mode, such that the full URL will be
            // checked.
            clearOnError = false;
            includeCount++;
            return true;
        }

        @Override
        public void includeEnd(URL url) {
            includeCount--;
        }

        @Override
        protected void handleWarning0(HojoException w) {
            errWriter.setStyle(STYLE_WARNING);
            super.handleWarning0(w);
            outWriter.writeStyled(INDENT_ERROR + w.getMessage() + "\n",
                    STYLE_WARNING);
            setState(actionState, (errorState != STATUS_ERROR) ? STATUS_WARNING
                    : STATUS_ERROR, true, -1);
        }

        @Override
        public boolean handleError(HojoException e) {
            if (commandReader != null) {
                commandReader.close();
                commandReader = null;
            }
            errWriter.setStyle(STYLE_ERROR);
            super.handleError(e);
            outWriter.writeStyled(INDENT_ERROR + e.getMessage() + "\n",
                    STYLE_ERROR);
            setState(actionState, STATUS_ERROR, true, -1);
            return true;
        }

        @Override
        public boolean doRecovery(HojoLexer.Recovery rec) {
            if (clearOnError) {
                while (rec.exit()) {
                    ;
                }
                rec.resync();
                return true;
            }
            else {
                return false;
            }
        }

        @Override
        public void commandResult() {
            setState(STATUS_COMPILE, errorState, false, -1.0);
        }

        @Override
        public void commandResult(Object result) {
            if (commandReader != null) {
                commandReader.close();
                commandReader = null;
            }
            if (optShowOutput) {
                outWriter.writeStyled(formatOutput(result), STYLE_RESULT);
            }
            setState(STATUS_COMPILE, errorState, false, -1.0);
        }

        @Override
        public void commandRead() {
            setState(STATUS_COMPILE, errorState, true, -1.0);
        }

        @Override
        protected void commandExecute0(Statement stm) {
            if (optShowCode) {
                outWriter.writeStyled(stm.toString(stx, fmt, INDENT_CODE) +
                        "\n", STYLE_CODE);
            }
            setState(STATUS_EXECUTE, errorState, false, -1.0);
        }

        BufferReader getCommandReader() {
            return commandReader;
        }

        @Override
        protected void commandExecute0(String[] cmds) {
            lex.include(commandReader = new BufferReader());
            setState(STATUS_EXECUTE, errorState, false, -1);
        }

        @Override
        public void finished() {
            if (commandReader != null) {
                commandReader.close();
                commandReader = null;
            }
            outWriter.writeStyled(inputIndicator, STYLE_NORMAL);
            setState(STATUS_READY, errorState, false, -1.0);
            super.finished();
            outWriter.flush(true);
            errWriter.flush(true);
            menuCtrl.toggleMenuEnabledState(true);
        }

        @Override
        public void indicateInput() {
            // do nothing
        }

        @Override
        public String formatOutput(Object output) {
            if (output instanceof Code) {
                return ((Code)output).toString(stx, fmt,
                        INDENT_RESULT) + "\n";
            }
            else {
                return StringUtils.any2String(output, fmt, INDENT_RESULT)
                        + "\n";
            }
        }

        public synchronized boolean acceptsInput() {
            return commandReader != null || super.isFinished();
        }

        // upate the status messages and icons
        void setState(int aState, int eState, boolean updateMsg, double split) {
            final String[] nextText = new String[3]; // command status, error
                                                     // status, error status
                                                     // tooltip
            final Icon[] nextIcon = new Icon[2]; // command status icon, error
                                                 // status icon
            final double[] dividerLoc = { split }; // new position of the
                                                   // divider
            final boolean interruptState; // new enabled status of the interrupt
                                          // menu item

            if (aState != actionState) {
                interruptState = (actionState = aState) == STATUS_EXECUTE;
                nextText[0] = statusMessages[actionState];
                nextIcon[0] = statusIcons[actionState];
            }
            else {
                interruptState = false;
            }

            if (eState != errorState) {
                nextIcon[1] = statusIcons[errorState];
            }
            errorState = eState;

            /*
             * switch (errorState = eState) { case STATUS_WARNING: dividerLoc[0]
             * = Math.min(0.5, split); break; case STATUS_ERROR: dividerLoc[0] =
             * Math.min(0.0, split); break; default: dividerLoc[0] =
             * Math.min(1.0, split); break; }
             */
            if (updateMsg) {
                nextIcon[1] = statusIcons[errorState];
                if (errors + warnings == 0) {
                    nextText[1] = "";
                    nextText[2] = statusMessages[STATUS_NONE];
                    statusMsg = null; // indicate no errors or warnings
                }
                else {
                    String warn = (warnings == 1)
                            ? statusMessages[STATUS_WARNING]
                            : MessageFormat.format(
                                    statusMessages[STATUS_NONE + 3],
                                    new Object[] { "" + warnings });
                    String err = (errors == 1) ? statusMessages[STATUS_ERROR]
                            : MessageFormat.format(
                                    statusMessages[STATUS_NONE + 2],
                                    new Object[] { "" + errors });
                    statusMsg = nextText[2] = nextText[1] = (errors == 0) ? warn
                            : (warnings == 0) ? err
                                    : MessageFormat.format(
                                            statusMessages[STATUS_NONE + 1],
                                            new Object[] { err, warn });
                }
            }

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    interruptMenuItem.setEnabled(interruptState);
                    if (nextText[0] != null) {
                        statusBar.setText(0, nextText[0]);
                        statusBar.setIcon(2, nextIcon[0], nextText[0]);
                    }
                    if (nextText[1] != null) {
                        statusBar.setText(1, nextText[1]);
                        if (nextIcon[1] != null) {
                            statusBar.setIcon(3, nextIcon[1], nextText[2]);
                        }
                    }
                    else if (nextIcon[1] != null) {
                        statusBar.setIcon(3, nextIcon[1], null);
                    }
                    if (dividerLoc[0] >= 0.0) {
                        outputSplit.setDividerLocation(dividerLoc[0]);
                    }
                }
            });
        }
    }

    private final static int CMD_IPRET = 0,
            CMD_RUN = 1,
            CMD_COMPILE = 2,
            CMD_STOP = 3,
            CMD_DATA = 4;

    private static class Command {
        int command;
        boolean ignoreWarnings;
        StringReader input;

        public Command(int command, boolean ignoreWarnings,
                StringReader input) {
            this.command = command;
            this.ignoreWarnings = ignoreWarnings;
            this.input = input;
        }
    }

    private class CommandKeyListener implements KeyListener {
        int cmdIndex = 0;
        java.util.List history;

        private SelectDialog sDlg = new SelectDialog(frm, _guiResource);

        CommandKeyListener(int historySize) {
            history = new ArrayList(historySize);
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);

            if (key_IPRET.equals(ks)) {
                e.consume();
                if (!obs.acceptsInput()) {
                    Toolkit.getDefaultToolkit().beep();
                }
                else {
                    go(obs.getCommandReader() != null ? CMD_DATA : CMD_IPRET);
                }
            }
            else if (key_NEXT.equals(ks)) {
                // next command key ?
                e.consume();
                if (history.size() == 0) {
                    Toolkit.getDefaultToolkit().beep();
                }
                else {
                    setCommand(nextCommand());
                }
            }
            else if (key_PREV.equals(ks)) {
                // previous command key
                e.consume();
                if (history.size() == 0) {
                    Toolkit.getDefaultToolkit().beep();
                }
                else {
                    setCommand(prevCommand());
                }
            }
            else if (key_HISTORY.equals(ks)) {
                // history command key?
                e.consume();
                if (history.size() == 0) {
                    Toolkit.getDefaultToolkit().beep();
                }
                else {
                    showHistory();
                }
            }
        }

        String prevCommand() {
            if ("".equals(commandLine.getText())) {
                return (String)history.get(cmdIndex);
            }
            if (++cmdIndex >= history.size()) {
                cmdIndex = 0;
            }
            return (String)history.get(cmdIndex);
        }

        String nextCommand() {
            if ("".equals(commandLine.getText())) {
                return (String)history.get(cmdIndex);
            }
            if (--cmdIndex < 0) {
                cmdIndex = history.size() - 1;
            }
            return (String)history.get(cmdIndex);
        }

        void setCommand(String s) {
            commandLine.setText(s);
            // commandLine.selectAll();
        }

        void addCommand(String s) {
            cmdIndex = 0;

            int idx = history.indexOf(s);
            if (idx > 0) {
                s = (String)history.remove(idx);
            }
            if (idx != 0) {
                history.add(0, s);
            }
        }

        void showHistory() {
            Integer Idx = (Integer)sDlg.select(
                    rsrc.getString("cmdline.history.title"), history, true,
                    true);
            if (Idx == null) {
                return;
            }
            setCommand((String)history.get(cmdIndex = Idx.intValue()));
        }

        void go(int cmd) {
            String s = commandLine.getText();
            if (s.length() == 0) {
                if (cmd == CMD_DATA) {
                    // end of command data
                    BufferReader r = obs.getCommandReader();
                    r.clear();
                    r.close();
                }
                else {
                    // do nothing
                    return;
                }
            }
            else {
                // commandLine.selectAll();
                commandLine.setText("");
                addCommand(s);

                if (cmd == CMD_DATA) {
                    try {
                        obs.getCommandReader().put(s + '\n');
                    }
                    catch (IOException e) {
                        // won't happen
                        obs.getCommandReader().close();
                    }
                }
                else {
                    control.q
                            .put(new Command(cmd, false, new CommandReader(s)));
                }
            }
        }
    }

    private class MenuStateController {
        JMenuItem[] nonEditorItems;
        JMenuItem[] editorItems;
        boolean enabled = true;
        boolean editor0 = true;

        MenuStateController() {
            ArrayList edItems = new ArrayList();
            HashSet edCodes = new HashSet(8);
            ArrayList nedItems = new ArrayList();

            JMenuItem itm;
            Integer i;
            if ((itm = (JMenuItem)menuTable.get(
                    i = new Integer(ACT_MENU_FILE_LOAD_SCRIPT))) != null) {
                edCodes.add(i);
                edItems.add(itm);
            }
            if ((itm = (JMenuItem)menuTable.get(
                    i = new Integer(ACT_MENU_FILE_SAVE_SCRIPT))) != null) {
                edCodes.add(i);
                edItems.add(itm);
            }
            if ((itm = (JMenuItem)menuTable.get(
                    i = new Integer(ACT_MENU_COMMAND_COMPILE))) != null) {
                edCodes.add(i);
                edItems.add(itm);
            }
            if ((itm = (JMenuItem)menuTable.get(
                    i = new Integer(ACT_MENU_COMMAND_RUN))) != null) {
                edCodes.add(i);
                edItems.add(itm);
            }

            Iterator it = menuTable.entrySet().iterator();
            java.util.Map.Entry e;
            while (it.hasNext()) {
                e = (java.util.Map.Entry)it.next();
                if (!edCodes.contains(e.getKey())) {
                    nedItems.add(e.getValue());
                }
            }

            editorItems = (JMenuItem[])edItems
                    .toArray(new JMenuItem[edItems.size()]);
            nonEditorItems = (JMenuItem[])nedItems
                    .toArray(new JMenuItem[nedItems.size()]);
        }

        // toggle the active state
        synchronized void toggleMenuEnabledState(final boolean enabled) {
            this.enabled = enabled;
            final boolean ed0 = editor0;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < nonEditorItems.length; i++) {
                        nonEditorItems[i].setEnabled(enabled);
                    }
                    for (int i = 0; i < editorItems.length; i++) {
                        editorItems[i].setEnabled(enabled && !ed0);
                    }
                }
            });
        }

        // toggle whether editor 0 (the command line) is active
        synchronized void toggleEditorState(final boolean isEditor0) {
            editor0 = isEditor0;
            if (!enabled) {
                return; // the item should still be disabled
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < editorItems.length; i++) {
                        editorItems[i].setEnabled(!isEditor0);
                    }
                }
            });
        }

        synchronized void setState(boolean enabled, boolean editor0) {
            this.editor0 = editor0;
            toggleMenuEnabledState(enabled);
        }
    }

    private class ControlThread extends Thread {
        CommandQueue q = new CommandQueue();
        boolean finished = false;

        ControlThread() {
            super(HojoShell.this.toString() + "-control");
            ControlThread.this.start();
        }

        public void doInterrupt() {
            this.interrupt();
        }

        @Override
        public void run() {
            Command cmd;
            Statement stm;

            while (true) {
                try {
                    cmd = (Command)q.get();
                }
                catch (InterruptedException e) {
                    continue;
                }
                if (cmd.command == CMD_STOP) {
                    synchronized (this) {
                        finished = true;
                        notify();
                        return;
                    }
                }

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        errorLog.setText("");
                    }
                });

                synchronized (ipret) {
                    if (cmd.command == CMD_IPRET) {
                        try {
                            ipret.run(cmd.input);
                        }
                        catch (Throwable t) {
                            obs.handleError(HojoException.wrap(t));
                        }
                        continue;
                    }

                    lex.include(cmd.input);
                    obs.started(cmd.input);
                    try {
                        BaseEnv env = ipret.getBaseEnv();
                        env.clear();
                        stm = comp.compileBlock(env, obs);
                    }
                    catch (HojoException e) {
                        obs.handleError(e);
                        try {
                            while (lex.exit()) {
                                ;
                            }
                        }
                        catch (RuntimeException e_) {
                        }
                        lex.resync();
                        obs.recovered();
                        stm = null;
                    }

                    if (stm == null || obs.getErrorCount() > 0 ||
                            (!cmd.ignoreWarnings
                                    && obs.getWarningCount() > 0)) {
                        obs.finished();
                        continue;
                    }

                    if (cmd.command == CMD_COMPILE) {
                        ipret.setLastResult(stm);
                        obs.commandResult(stm);
                        obs.finished();
                    }
                    else {
                        ipret.execute(stm);
                        obs.finished();
                    }
                } // sync
            } // while
        }
    }

}
