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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.WeakHashMap;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.xodonex.util.StringUtils;
import org.xodonex.util.log.Log;
import org.xodonex.util.log.LogEntry;
import org.xodonex.util.log.LogListener;
import org.xodonex.util.ui.comp.Splash;
import org.xodonex.util.ui.comp.StatusBar;
import org.xodonex.util.ui.dialog.InputDialog;
import org.xodonex.util.ui.dialog.MessageDialog;
import org.xodonex.util.ui.window.ValidatedWindow;

/**
 * A basic GUI application framework, based on common elements created from
 * localized resource-files.
 */
public abstract class AbstractGui implements ActionProvider, IconProvider {

    public final static String SOURCE = "GUI";

    /**
     * @param cls
     *            a class
     * @return the path in which the given class should be found. The last
     *         character is a separator
     */
    public String createPathFrom(Class cls) {
        return getClass().getPackage().getName().replace('.', '/') + '/';
    }

    // global action map
    private Map _actions;

    // the locale
    private Locale _locale;

    // the GUI resource
    private GuiResource _rsrc;

    // whether the gui has been started or terminated
    private boolean _started = false, _terminated = false;

    // the system log
    private Log _log;

    // entries placed in this set are not shown in the status bar when logged.
    private Map _suppressedEntries = Collections
            .synchronizedMap(new WeakHashMap());

    // the main window
    private JFrame _main;

    // the tool bar container
    private Container _toolbars;

    // the status bar
    private StatusBar _statusBar;

    // the contents of the main window
    @SuppressWarnings("unused")
    private JPanel _mainContents;

    // the windows currently being owned by the application, excluding the
    // main window.
    private Map _windows = Collections.synchronizedMap(new WeakHashMap());

    // the currently active window
    private Window _activeWindow = null;

    // a cache for icons
    private Map _iconCache = new HashMap(10, 2.0f);

    public AbstractGui() {
        this(null, null);
    }

    public AbstractGui(Log log) {
        this(log, null);
    }

    public AbstractGui(Log log, Locale locale) {
        _log = log;

        if ((_locale = locale) == null) {
            _locale = Locale.getDefault();
        }
    }

    public synchronized void start() throws IllegalStateException {
        if (_started) {
            throw new IllegalStateException();
        }
        _started = true;

        // mark as terminated, in case an exception occurs
        _terminated = true;

        Splash splash = null;
        try {
            // create the resource bundle fist
            ResourceBundle bundle = createResource(null, _locale);
            _rsrc = new GuiResource(_locale, bundle, this, this, null);

            // launch the splash screen
            splash = getSplash();

            // create the system actions
            _actions = createActions();

            // ... and localize them
            for (Iterator i = _actions.entrySet().iterator(); i.hasNext();) {
                Map.Entry e = (Map.Entry)i.next();

                localizeAction((Action)e.getValue(), (String)e.getKey());
            }

            // build the main window
            _main = createMainFrame();
            _rsrc.setMainFrame(_main);
            _main.setTitle(getTitle());
            _main.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            Image image = getIconImage();
            if (image != null) {
                _main.setIconImage(image);
            }

            // build the menu bar and add it to the main window
            String mbar = defineMenuBar();
            if (mbar != null) {
                _main.setJMenuBar(createMenuBar(mbar));
                mbar = null;
            }

            // build the tool bars and add them to the container
            _toolbars = new Box(BoxLayout.X_AXIS);
            String tbars = defineToolBars();
            if (tbars != null) {
                JToolBar[] toolbars = createToolBars(tbars);
                for (int i = 0; i < toolbars.length; i++) {
                    _toolbars.add(toolbars[i]);
                }
            }

            // build the status bar
            _statusBar = createStatusBar();

            // initialize the main window contents
            Container c = _main.getContentPane();
            c.setLayout(new BorderLayout());
            c.add(_toolbars, BorderLayout.NORTH);
            if (_statusBar != null) {
                c.add(_statusBar, BorderLayout.SOUTH);
            }
            c.add(_mainContents = new JPanel(), BorderLayout.CENTER);

            // configure the log
            if (_log != null) {
                int statusIdx = getStatusIndex();

                if (statusIdx >= 0) {
                    // add a listener such that the status bar is cleared
                    // at each message, and such that messages between
                    // debug and warning level are shown
                    _log.addLogListener(new LogListener() {
                        @Override
                        public void logged(final LogEntry e) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    _statusBar.setText(getStatusIndex(), "");
                                    if (_suppressedEntries.containsKey(e)) {
                                        return;
                                    }
                                    int severity = e.getSeverity();
                                    if (severity > LogEntry.SEVERITY_DEBUG &&
                                            severity < LogEntry.SEVERITY_WARNING) {
                                        _statusBar.setText(getStatusIndex(),
                                                e.getMessage());
                                    }
                                }
                            });
                        }
                    }, LogEntry.SEVERITY_MIN, LogEntry.SEVERITY_MAX);

                    // add a listener such that all messages above info level
                    // are shown in a message box
                    _log.addLogListener(new LogListener() {
                        @Override
                        public void logged(final LogEntry e) {
                            if (_suppressedEntries.containsKey(e)) {
                                return;
                            }
                            showMessage(e, true);
                        }
                    }, LogEntry.SEVERITY_INFO + 1, LogEntry.SEVERITY_MAX);
                } // statusIdx >= 0
            } // _log != null

            // add the appropriate listeners
            _main.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    AbstractGui.this.terminate();
                }

                @Override
                public void windowActivated(WindowEvent e) {
                    AbstractGui.this.windowActivated();
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                    AbstractGui.this.windowDeactivated();
                }

                @Override
                public void windowIconified(WindowEvent e) {
                    AbstractGui.this.windowIconified();
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                    AbstractGui.this.windowDeiconified();
                }
            });

            // execute the custom configuration
            init();

            // set terminated back to false, since no error occurred.
            _terminated = false;
        }
        finally {
            // remove the splash, if necessary
            if (splash != null) {
                splash.remove();
            }
        }
    }

    /**
     * @return the title of the GUI The default implementation retrieves the
     *         title from the {@link #getLocalizedString(String) localized
     *         string} "gui.title"
     */
    public String getTitle() {
        return getLocalizedString("gui.title");
    }

    /**
     * @return the Font which should be used for the menu items The default
     *         implementation retrieves the title from the
     *         {@link #getLocalizedString(String) localized string} "font.menu"
     */
    public Font getMenuFont() {
        return ResourceUtils.toFont(getLocalizedString("font.menu"));
    }

    /**
     * @return the Font which should be used for the status messages The default
     *         implementation retrieves the title from the
     *         {@link #getLocalizedString(String) localized string}
     *         "font.status"
     */
    public Font getStatusFont() {
        return ResourceUtils.toFont(getLocalizedString("font.status"));
    }

    /**
     * @return the icon image to be used for the GUI (if any) The default
     *         implementation retrieves the icon from the
     *         {@link #getLocalizedString(String) localized string}
     *         "gui.iconImage"
     */
    public Image getIconImage() {
        String s = getLocalizedString("gui.iconImage");
        return s == null ? null : getImage(s);
    }

    /**
     * @return the Splash screen to be used (if any) The default implementation
     *         creates a splash screen from the image defined by the
     *         {@link #getLocalizedString(String) localized string} "gui.splash"
     */
    public Splash getSplash() {
        String s = getLocalizedString("gui.splash").trim();
        return s == null ? null : new Splash(getImage(s), 0L);
    }

    /**
     * Log the given entry to the log used by the GUI.
     *
     * @param e
     *            the log entry
     * @param silent
     *            if true, then the GUI will suppress the usual status message
     *            or dialog box.
     */
    public void log(LogEntry e, boolean silent) {
        if (silent) {
            _suppressedEntries.put(e, null);
        }
        _log.log(e);
    }

    /**
     * Displays a message in a message box
     *
     * @param e
     *            the message
     * @param silent
     *            whether an entry should be placed in the system log
     */
    public void showMessage(LogEntry e, boolean silent) {
        if (!silent) {
            _suppressedEntries.put(e, null);
            _log.log(e);
        }

        if (getStatusIndex() > 0) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    _statusBar.setText(getStatusIndex(), "");
                }
            });
        }

        MessageDialog.showMessage(getActiveWindow(), _rsrc, e);
    }

    /**
     * Prompts the user for confirmation on something, using a localized
     * message.
     *
     * @return true iff the user confirmed
     * @param key
     *            the key for the message string
     * @param details
     *            the message details
     * @param silent
     *            whether an indication should be placed in the system log
     * @see #promptConfirmation(String, String, boolean)
     */
    public boolean promptStandardConfirmation(String key, String details,
            boolean silent) {
        String s = getLocalizedString("msg.Confirm");
        Object[] args = new String[] { getLocalizedString(key) };

        return promptConfirmation(MessageFormat.format(s, args), details,
                silent);
    }

    /**
     * Prompts the user for confirmation on something, using a localized
     * message.
     *
     * @param key
     *            the key for the message string
     * @param args
     *            arguments for the message string
     * @param details
     *            the message details
     * @param silent
     *            whether an indication should be placed in the system log
     * @return true iff the user confirmed
     * @see #promptConfirmation(String, String, boolean)
     */
    public boolean promptStandardConfirmation(String key, String[] args,
            String details,
            boolean silent) {
        String s = getLocalizedString(key);
        s = MessageFormat.format(s, (Object[])args);

        s = MessageFormat.format(getLocalizedString("msg.Confirm"),
                new Object[] { s });

        return promptConfirmation(s, details, silent);
    }

    /**
     * Prompts the user for confirmation on something
     *
     * @return true iff the user confirmed
     * @param message
     *            the message
     * @param details
     *            the message details
     * @param silent
     *            whether an indication should be placed in the system log
     */
    public boolean promptConfirmation(String message, String details,
            boolean silent) {
        boolean result = MessageDialog.createDialog(getActiveWindow(), _rsrc)
                .showQuestionMessage(message, details) == 0;

        if (!silent) {
            LogEntry e = new LogEntry(SOURCE, LogEntry.SEVERITY_MIN,
                    "\"" + message + "\" : " + result, null);
            _suppressedEntries.put(e, null);
            _log.log(e);
        }
        return result;
    }

    /**
     * Displays a new, modal dialog in the application. The dialog will be
     * contain a single component.
     *
     * @param comp
     *            the component to be shown in a dialog
     * @param buttons
     *            the buttons for the dialog
     * @param title
     *            the title for the dialog
     * @return the index of the button use to dismiss the dialog.
     */
    public int showDialog(Component comp, String[] buttons, String title) {
        InputDialog dlg = InputDialog.createDialog(getActiveWindow(),
                getResource());
        dlg.setTitle(title == null ? "" : title);
        return dlg.show(comp, buttons);
    }

    /**
     * Retrieves the window having the given ID
     *
     * @param key
     *            the window ID
     * @return null if no matching window was found
     */
    public Window getWindow(Object key) {
        if (key == null) {
            return null;
        }

        for (Iterator i = _windows.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            if (key.equals(e.getValue())) {
                return (Window)e.getKey();
            }
        }
        return null;
    }

    /**
     * Corresponds to {@link #showWindow(Window, Object) showWindow(wnd, null)}
     *
     * @param wnd
     *            the window to be shown.
     */
    public void showWindow(Window wnd) {
        showWindow(wnd, null);
    }

    /**
     * Displays a new window in the application. The default implementation
     * simply centers the window on the screen.
     *
     * @param wnd
     *            the window to be displayed
     * @param key
     *            the key value to be assigned to the window
     */
    public void showWindow(final Window wnd, Object key) {
        if (wnd == null) {
            return;
        }
        // getMain().toFront();

        if (!_windows.containsKey(wnd)) {
            WindowListener l = new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    _activeWindow = wnd;
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    _windows.remove(wnd);
                    wnd.removeWindowListener(this);
                    if (_activeWindow == wnd) {
                        _activeWindow = null;
                    }
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    if (closeWindow(wnd)) {
                        _windows.remove(wnd);
                        wnd.removeWindowListener(this);
                        if (_activeWindow == wnd) {
                            _activeWindow = null;
                        }
                    }
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                    _activeWindow = null;
                }
            };

            wnd.addWindowListener(l);
            if (wnd instanceof JFrame) {
                ((JFrame)wnd).setDefaultCloseOperation(
                        WindowConstants.DO_NOTHING_ON_CLOSE);
            }

            // show the window centered on the screen
            WindowManager.showCentered(wnd);
        }
        else {
            // reactivate the window
            wnd.toFront();
        }

        // (re)assign the ID to the window
        _windows.put(wnd, key);
    }

    /**
     * Shows the given component in a new window. The window style will depend
     * on the implementation of the GUI - it might be shown in a new internal
     * frame, in a dedicated part of the main window etc. The default
     * implementation creates a new JFrame from the component an uses
     * {@link #showWindow(Window)} to show it.
     *
     * @param c
     *            the component to be shown
     * @param title
     *            the title (or tool tip, label etc., depending on the
     *            implementation) to be used when displaying the component.
     */
    public void showComponent(Component c, String title) {
        if (title == null) {
            title = c.getName();
        }
        title = MessageFormat.format(getLocalizedString("gui.titleFormat"),
                new Object[] { title == null ? c.getName() : title,
                        getMain().getTitle() });

        Window wnd;
        if (c instanceof Window) {
            wnd = (Window)c;
            c.setName(title);
        }
        else {
            JFrame frm = new JFrame(title);
            frm.setIconImage(getMain().getIconImage());
            frm.getContentPane().add(c);
            wnd = frm;
        }
        showWindow(wnd);
    }

    /**
     * Close the given window, validating its contents if necessary. The default
     * implementation allows a {@link ValidatedWindow} to dismiss itself;
     * otherwise, {@link UiUtils#verifyInput(Component)} is used to verify the
     * window, before it is allowed to be closed.
     *
     * @param wnd
     *            the window
     * @return true iff the window was validated and closed.
     */
    public boolean closeWindow(Window wnd) {
        if (wnd instanceof ValidatedWindow) {
            return ((ValidatedWindow)wnd).dismiss(ValidatedWindow.CANCEL);
        }

        Container c = wnd;
        if (wnd instanceof RootPaneContainer) {
            c = ((RootPaneContainer)wnd).getContentPane();
        }

        if (!UiUtils.verifyInput(c)) {
            return false;
        }
        wnd.dispose();
        return !wnd.isVisible();
    }

    /**
     * @return the currently active window, or null if no window is currently
     *         active.
     */
    public Window getActiveWindow() {
        return _activeWindow;
    }

    /**
     * @return the currently active windows owned by the application. Note that
     *         a window is considered as owned by the application only if shown
     *         using {@link #showWindow(Window)}.
     */
    public Collection getOwnedWindows() {
        return new ArrayList(_windows.keySet());
    }

    /**
     * Terminate the application.
     *
     * @return true iff the application terminated
     * @see #checkTerminate()
     * @see #askTerminate()
     * @see #doTerminate()
     */
    public final boolean terminate() {
        synchronized (this) {
            if (_terminated) {
                return true;
            }

            if (!(askTerminate() && checkTerminate())) {
                return false;
            }

            doTerminate();
            _terminated = true;
            notifyAll();
        }
        return true;
    }

    /**
     * Block the current thread until the application has terminated
     *
     * @throws IllegalStateException
     *             if the application is not started yet
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    public final synchronized void join() throws IllegalStateException,
            InterruptedException {
        if (!_started) {
            throw new IllegalStateException();
        }
        if (_terminated) {
            return;
        }
        wait();
    }

    /**
     * @return the main frame of the GUI.
     */
    public JFrame getMain() {
        return _main;
    }

    /**
     * @return the GUI resource
     */
    public GuiResource getResource() {
        return _rsrc;
    }

    /**
     * @return the GUI action which has the given ID
     * @exception IllegalArgumentException
     *                if the action does not exist
     */
    @Override
    public javax.swing.Action getAction(String name) {
        Action result = (Action)_actions.get(name);
        if (result == null) {
            throw new IllegalArgumentException(name);
        }
        return result;
    }

    /**
     * @param id
     *            a key to look up
     * @return the localized string named by the given key.
     * @exception MissingResourceException
     *                if the localized string does not exist.
     */
    public String getLocalizedString(String id)
            throws MissingResourceException {
        String s = _rsrc.getString(id);
        return s.length() == 0 ? null : s;
    }

    /**
     * @param path
     *            a path
     * @return an Image loaded from the given path
     * @see #locateImage(String)
     */
    public Image getImage(String path) {
        URL url = locateImage(path);
        if (url == null) {
            throw new MissingResourceException("Missing image \"" + path + "\"",
                    getClass().getName(), path);
        }

        return Toolkit.getDefaultToolkit().createImage(url);
    }

    /**
     * @param path
     *            a path
     * @return an Icon loaded from the given path
     * @see #locateIcon(String)
     */
    @Override
    public Icon getIcon(String path) {
        if (_iconCache.containsKey(path)) {
            return (Icon)_iconCache.get(path);
        }
        URL url = locateIcon(path);
        if (url == null) {
            return null;
        }

        Icon i = new ImageIcon(url);
        _iconCache.put(path, i);
        return i;
    }

    /**
     * @param resource
     *            a resource location
     * @return a new Properties instance which has been loaded from the given
     *         resource location.
     * @throws IOException
     *             on I/O error
     */
    public Properties loadProperties(String resource) throws IOException {
        Properties ps = new Properties();
        URL u = locateResource(resource);
        InputStream i = u.openStream();
        try {
            ps.load(i);
        }
        finally {
            i.close();
        }

        return ps;
    }

    /**
     * @param name
     *            the name of the resource bundle. If the given name is null,
     *            the name of the class of the receiving object will be used.
     * @param locale
     *            the locale which should be used creating the resource bundle.
     *            If this is null, the locale given to the constructor will be
     *            used.
     * @return the localized resource bundle containing the GUI definitions.
     */
    public ResourceBundle createResource(String name, Locale locale) {
        if (name == null) {
            name = getClass().getName();
            name = name.substring(name.lastIndexOf('.') + 1);
        }
        if (locale == null) {
            locale = _locale;
        }
        return ResourceBundle.getBundle(getResourceBase() + name, locale);
    }

    /**
     * @param address
     *            an address
     * @return an absolute URL which defines the location of the image whose
     *         relative location is the given address. Subclasses which locate
     *         images in a non-standard manner should override this method. The
     *         default implementation uses {@link #locateResource(String)}
     * @exception IllegalArgumentException
     *                if the resource does not exist
     */
    public URL locateImage(String address) throws IllegalArgumentException {
        return locateResource(address);
    }

    /**
     * @param address
     *            and address
     * @return an absolute URL which defines the location of the icon whose
     *         relative location is the given address. Subclasses which locate
     *         icons in a non-standard manner should override this method. The
     *         default implementation uses {@link #locateResource(String)}
     * @exception IllegalArgumentException
     *                if the resource does not exist
     */
    public URL locateIcon(String address) throws IllegalArgumentException {
        return locateResource(address);
    }

    /**
     * @param address
     *            and address
     * @return an absolute URL which defines the location of the resource whose
     *         relative location is the given address. This locates the resource
     *         relative to the class and the {@link #getResourceBase() resource
     *         base}.
     * @exception IllegalArgumentException
     *                if the resource does not exist
     */
    public URL locateResource(String address) throws IllegalArgumentException {
        return locateResource(getResourceBase(), address);
    }

    /**
     * @param path
     *            a path
     * @param address
     *            an address
     * @return an absolute URL which defines the location of the resource whose
     *         location is the given address.
     * @exception IllegalArgumentException
     *                if the resource does not exist
     */
    public URL locateResource(String path, String address)
            throws IllegalArgumentException {
        String addr = path + address;

        // find the resource, circumventing Java bug 4214785.
        java.net.URLClassLoader cl = (java.net.URLClassLoader)getClass()
                .getClassLoader();
        URL result = cl.findResource(addr);

        if (result == null) {
            throw new IllegalArgumentException(addr);
        }
        return result;
    }

    /**
     * @return an absolute path to the root directory in which resources for
     *         this GUI are stored. The default implementation returns the
     *         subdirectory "resource" relative to the class itself.
     */
    public String getResourceBase() {
        return createPathFrom(getClass()) + "resource/";
    }

    /**
     * @return a map containing the global actions supported by this GUI, stored
     *         under their respective String ID. This ID is used to retreive the
     *         action in {@link #getAction(String)}.
     */
    protected abstract Map createActions();

    /**
     * Set the localized properties of the given action, and return it (or a
     * replacement). The default implementation retreives and parses the
     * {@link #getLocalizedString(String) localized string} having the specified
     * key prefixed by "act.".
     *
     * @param a
     *            the action to be localized
     * @param key
     *            the resource key
     */
    protected void localizeAction(Action a, String key) {
        String rsrc = getLocalizedString("act." + key);
        ResourceUtils.configureAction(a,
                ResourceUtils.decodeActionResource(rsrc), this);
    }

    /**
     * @return a definition of the menu bar for the GUI. If the return value is
     *         null, no menu bar will be created. The default implementation
     *         retrieves the title from the {@link #getLocalizedString(String)
     *         localized string} "gui.menuBar"
     * @see #createMenuBar(String)
     */
    protected String defineMenuBar() {
        return getLocalizedString("gui.menuBar");
    }

    /**
     * @return a definition of the tool bars of the GUI. If the return value is
     *         null, no tool bars will be created. The default implementation
     *         retrieves the title from the {@link #getLocalizedString(String)
     *         localized string} "gui.toolBars"
     * @see #createToolBars(String)
     */
    protected String defineToolBars() {
        return getLocalizedString("gui.toolBars");
    }

    /**
     * @return a JFrame instance which will be used as the main window of the
     *         application. It should only be necessary to override this method
     *         if a subclass of javax.swing.JFrame is necessary. The default
     *         implementation returns a new JFrame instance
     */
    protected JFrame createMainFrame() {
        return new JFrame();
    }

    /**
     * @return the StatusBar instance which will be used for status messages.
     *         The default implementation returns a new StatusBar which uses one
     *         label of minimum 10 wide characters.
     */
    protected StatusBar createStatusBar() {
        return new StatusBar(new int[] { -10 }, 30, getStatusFont(), null,
                null);
    }

    /**
     * @return the component index in the status bar which is used for status
     *         messages. If this is negative, the no status messages are used.
     *         The default implementation returns 0
     */
    protected int getStatusIndex() {
        return 0;
    }

    /**
     * This method is invoked from the constructor once the default
     * initialization has been executed. Any initialization of the GUI should be
     * done by overriding this method, not in the constructor.<BR>
     * The default implementation simply shows the main window.
     */
    protected void init() {
        _main.pack();
        _main.setVisible(true);
        _main.toFront();
    }

    /**
     * This method is invoked every time the main window is brought into focus.
     * The default implementation does nothing
     *
     * @see java.awt.event.WindowListener
     */
    protected void windowActivated() {
    }

    /**
     * This method is invoked every time the main window loses focus. The
     * default implementation does nothing
     *
     * @see java.awt.event.WindowListener
     */
    protected void windowDeactivated() {
    }

    /**
     * This method is invoked every time the main window is iconified. The
     * default implementation does nothing
     *
     * @see java.awt.event.WindowListener
     */
    protected void windowIconified() {
    }

    /**
     * This method is invoked every time the main window is deiconified. The
     * default implementation does nothing
     *
     * @see java.awt.event.WindowListener
     */
    protected void windowDeiconified() {
    }

    /**
     * Determines whether it is allowed to terminate the application. The
     * default implementation ensures that all child windows are validated, then
     * returns true.
     *
     * @return true if the application can terminate
     */
    protected boolean checkTerminate() {
        Collection c = getOwnedWindows();

        for (Iterator i = c.iterator(); i.hasNext();) {
            if (!closeWindow((Window)i.next())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prompts the user whether the application should be terminated. This will
     * be invoked only if {@link #checkTerminate()} returns true, and only if
     * this method itself returns true will the application be terminated. <BR>
     * The default implementation simply shows a confirmation dialog.
     *
     * @return whether the user permitted the application to terminate
     */
    protected boolean askTerminate() {
        return promptStandardConfirmation("msg.confirm.terminate", null, false);
    }

    /**
     * This method is invoked when the application terminates. The default
     * implementation simply disposes the main window.
     */
    protected void doTerminate() {
        _main.dispose();
    }

    /**
     * Creates a menu bar from a definition. The syntax is as follows:
     * <ul>
     * <li>The escape character % may be used to turn off any special
     * significance of the following charater
     * <li>The character | is used to separate items
     * <li>A menu item is defined by a sequence of characters comprising an
     * action ID
     * <li>A menu is defined by a sequence of characters comprising the menu
     * name, followed by a menu definition enclosed in brackets. The character
     * &amp; may be used as a prefix to any character in the menu name to define
     * the mnemonic character.
     * </ul>
     *
     * @param def
     *            the definition of the menu bar to be created
     * @return the created menu bar
     * @see #getAction(String)
     */
    protected JMenuBar createMenuBar(String def) {
        try {
            return ResourceUtils.createMenuBar(def, getMenuFont(), this);
        }
        catch (IllegalArgumentException e) {
            _log.log(new LogEntry(SOURCE, "Invalid menu definition \"" +
                    StringUtils.toJavaString(def) + "\"", e));
            throw e;
        }
    }

    /**
     * Creates a collection of tool bars from a definition.
     *
     * @param def
     *            the definition of the tool bars to be crated
     * @return the generated tool bars
     */
    protected JToolBar[] createToolBars(String def) {
        return ResourceUtils.createToolBars(def, this);
    }

}
