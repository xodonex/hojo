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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.xodonex.util.ui.GuiResource;

/**
 *
 * @author Henrik Lauritzen
 */
public class TabbedPropertySheet extends JTabbedPane {

    private static final long serialVersionUID = 1L;

    public final static PropertyClassifier DEFAULT_CLASSIFIER = new PropertyClassifier() {

        @Override
        public int countClasses() {
            return 2;
        }

        @Override
        public String[] getClasses() {
            return new String[] { "Properties", "Expert" };
        }

        @Override
        public void classify(Iterator pds, Collection[] classes) {
            while (pds.hasNext()) {
                PropertyDescriptor pd = (PropertyDescriptor)pds.next();
                if (pd.isHidden()) {
                    continue;
                }
                classes[pd.isExpert() ? 1 : 0].add(pd);
            }
        }

    };

    private PropertyClassifier _cls;

    public TabbedPropertySheet(JFrame ownerFrame, GuiResource rsrc) {
        this(BOTTOM, null, ownerFrame, rsrc);
    }

    public TabbedPropertySheet(PropertyClassifier cls, JFrame ownerFrame,
            GuiResource rsrc) {
        this(BOTTOM, cls, ownerFrame, rsrc);
    }

    public TabbedPropertySheet(int tabPlacement, PropertyClassifier cls,
            JFrame ownerFrame, GuiResource rsrc) {
        super(tabPlacement);
        _cls = cls == null ? DEFAULT_CLASSIFIER : cls;

        String[] classNames = _cls.getClasses();
        for (int i = 0; i < classNames.length; i++) {
            addTab(classNames[i], new PropertySheet(ownerFrame, rsrc));
        }
    }

    public void setBean(Object bean) throws IntrospectionException {
        int classes = _cls.countClasses();

        if (bean == null) {
            for (int i = classes - 1; i >= 0; i--) {
                ((PropertySheet)getComponentAt(i)).setBean(null, null);
                setEnabledAt(i, false);
            }
            setSelectedIndex(0);
            return;
        }

        Collection allProps = BeanUtils.createPropertyDescriptors(bean);

        int propCount = allProps.size();
        Collection[] propSets = new Collection[classes];
        for (int i = 0; i < classes; i++) {
            propSets[i] = new ArrayList(propCount / classes);
        }

        _cls.classify(allProps.iterator(), propSets);

        for (int i = 0; i < classes; i++) {
            ((PropertySheet)getComponentAt(i)).setBean(bean, propSets[i]);
            setEnabledAt(i, propSets[i].size() > 0);
        }
    }

    public void refreshView() {
        PropertySheet ps = (PropertySheet)getSelectedComponent();
        ps.refreshView();
    }

}
