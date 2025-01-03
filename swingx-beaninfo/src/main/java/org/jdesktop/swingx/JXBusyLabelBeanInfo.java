package org.jdesktop.swingx;

import org.jdesktop.beans.BeanInfoSupport;

/**
 * BeanInfo class for JXBusyLabel.
 * 
 * @author Jan Stola
 */
public class JXBusyLabelBeanInfo extends BeanInfoSupport {

	/** ctor */
    public JXBusyLabelBeanInfo() {
        super(JXBusyLabel.class);        
    }
    
    @Override
    protected void initialize() {
        setPreferred(true, "busy");
        String iconName = "resources/" + JXBusyLabel.class.getSimpleName();
        setSmallMonoIconName(iconName + "16.png");
        setMonoIconName(iconName + "32.png");
    }
}
