/*
 * Created on 05.11.2010
 *
 */
package org.jdesktop.swingx.renderer;

import javax.swing.Icon;

/**
 * Interface for tagging rendering components to allow Highlighters to treat 
 * the Icon (Issue #1311-swingx) as a visual decoration. ComponentProviders 
 * which hand out IconAware rendering components must guarantee to reset its
 * Icon property in each configuration round.  
 * 
 * @author Jeanette Winzenburg, Berlin
 */
public interface IconAware {

    /**
     * Sets the icon property.
     * 
     * @param icon Icon
     */
    public void setIcon(Icon icon);
    
    /**
     * Returns the icon property.
     * 
     * @return Icon
     */
    public Icon getIcon();
    
}
