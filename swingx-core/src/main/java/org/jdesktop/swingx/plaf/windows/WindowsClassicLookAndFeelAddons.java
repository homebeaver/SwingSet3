/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jdesktop.swingx.plaf.windows;

import static javax.swing.UIManager.getLookAndFeel;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;

import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.util.OS;
import org.kohsuke.MetaInfServices;

/**
 * Adds new pluggable UI following the Windows Classic look and feel. Currently
 * it extends the XP look and feel and overrides the <code>JTaskPane</code>
 * and <code>JTaskPaneGroup</code> UIs.
 */
@MetaInfServices(LookAndFeelAddons.class)
public class WindowsClassicLookAndFeelAddons extends WindowsLookAndFeelAddons {
    
    @Override
	public String toString() {
		return "["+this.getClass().getSimpleName()+", "+super.toString()+"]";		
	}

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean matches() {
        String laf = getLookAndFeel().getClass().getName();
        
        if (isSystemAddon()) {
            //special-case the jgoodies to ensure that we can match it
            return getSystemLookAndFeelClassName().equals(laf)
                    || "com.jgoodies.looks.windows.WindowsLookAndFeel".equals(laf);
        }
        
        return "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel".equals(laf);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isSystemAddon() {
        return OS.isWindows() && !OS.isUsingWindowsVisualStyles();
    }
}
