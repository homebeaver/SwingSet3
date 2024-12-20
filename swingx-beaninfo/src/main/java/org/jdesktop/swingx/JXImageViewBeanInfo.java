/*
 * JXImageViewBeanInfo.java
 * 
 * Created on May 3, 2007, 5:20:50 PM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jdesktop.swingx;

import org.jdesktop.beans.BeanInfoSupport;

/**
 *
 * @author joshy
 */
public class JXImageViewBeanInfo extends BeanInfoSupport{

	/** ctor */
    public JXImageViewBeanInfo() {
        super(JXImageView.class);
    }
    
    @Override
    protected void initialize() {
        setPreferred(true, "icon");
        setPreferred(true, "dragEnabled");
        setPreferred(true, "imageString");
        setPreferred(true, "imageURL");
        setPreferred(true, "image");
    }

}
