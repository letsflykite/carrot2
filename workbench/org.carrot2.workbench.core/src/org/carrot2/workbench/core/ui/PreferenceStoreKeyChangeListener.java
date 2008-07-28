package org.carrot2.workbench.core.ui;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * An abstract class that reacts on changes to a given property in the
 * {@link IPreferenceStore}.
 */
public abstract class PreferenceStoreKeyChangeListener implements IPropertyChangeListener
{
    protected final String property;
    
    public PreferenceStoreKeyChangeListener(String property)
    {
        this.property = property;
    }
    
    public void propertyChange(PropertyChangeEvent event)
    {
        if (ObjectUtils.equals(property, event.getProperty()))
        {
            propertyChangeFiltered(event);
        }
    }

    protected abstract void propertyChangeFiltered(PropertyChangeEvent event);
}
