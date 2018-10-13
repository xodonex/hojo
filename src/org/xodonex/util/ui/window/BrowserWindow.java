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
package org.xodonex.util.ui.window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.xodonex.util.StringUtils;
import org.xodonex.util.ui.GuiResource;
import org.xodonex.util.ui.WindowManager;

public class BrowserWindow extends BasicWindow {

    private static final long serialVersionUID = 1L;

    protected HTMLPane _text;
    protected JButton _close;
    protected JButton _back;
    protected Stack _history = new Stack();
    protected URL _currentURL;

    private Dimension _preferredSize = null;

    /*
     * -----------------------------------------------------------------------
     * Constructors
     * -----------------------------------------------------------------------
     */

    public BrowserWindow() {
        super();
    }

    public BrowserWindow(GuiResource rsrc) {
        super(rsrc);
    }

    public BrowserWindow(JFrame owner, GuiResource rsrc) {
        super(owner, rsrc);
    }

    @Override
    protected void setup() {
        super.setup();

        _close = _guiResource.createButton("btn.Close");
        _back = _guiResource.createButton("btn.Back");
        _back.setEnabled(false);

        _close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        _back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jump(null, false);
            }
        });

        _text = new HTMLPane();
        _text.setEditable(false);

        _text.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType()
                        .equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    jump(e.getURL(), true);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(_text);
        getContentPane().add(scroll, BorderLayout.CENTER);
        addButtons(new JButton[] { _close, _back },
                BorderFactory.createEtchedBorder(), 0);
    }

    /*
     * -----------------------------------------------------------------------
     * New methods
     * -----------------------------------------------------------------------
     */

    public void show(URL url, String title, double width, double height)
            throws IllegalStateException, IOException {
        synchronized (_lock) {
            if (_active) {
                throw new IllegalArgumentException();
            }

            if (width > 1.0 && height > 1.0) {
                _preferredSize = new Dimension((int)width, (int)height);
            }
            else {
                _preferredSize = new Dimension(
                        (int)(width * WindowManager.getScreenWidth()),
                        (int)(height * WindowManager.getScreenHeight()));
            }

            setTitle(title);
            jump(url, true);

            activate();
        }
    }

    public void back() {
        jump(null, false);
    }

    public void jump(URL url, boolean forward) {
        boolean setPage;

        if (forward) {
            if (_currentURL != null) {
                _history.push(_currentURL);
                _back.setEnabled(true);
                setPage = !url.sameFile(_currentURL);
            }
            else {
                setPage = true;
            }
            _currentURL = url;
        }
        else {
            int sz = _history.size();
            url = _currentURL;
            if (sz > 0) {
                _currentURL = (URL)_history.pop();
            }
            setPage = (url == null) ? true : !url.sameFile(_currentURL);
            url = _currentURL;
            if (sz <= 1) {
                _back.setEnabled(false);
            }
        }

        if (setPage) {
            try {
                _text.setPage(url);
            }
            catch (IOException e) {
                _text.setText(StringUtils.createTrace(e));
            }
        }
        if (url != null) {
            _text.scrollToReference(url.getRef());
        }
    }

    public URL currentURL() {
        return _currentURL;
    }

    /*
     * -----------------------------------------------------------------------
     * Overridden methods
     * -----------------------------------------------------------------------
     */

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            _text.setText("");
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (_preferredSize == null) {
            _preferredSize = new Dimension(super.getPreferredSize());
        }
        return _preferredSize;
    }

    /*
     * -----------------------------------------------------------------------
     * Inner classes
     * -----------------------------------------------------------------------
     */

    // needed in order to gain public access to scrollToReference()
    private static class HTMLPane extends JEditorPane {
        private static final long serialVersionUID = 1L;

        HTMLPane() {
            super();
        }

        @Override
        public void scrollToReference(String reference) {
            super.scrollToReference(reference);
        }
    }

}
