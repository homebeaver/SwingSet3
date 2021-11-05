/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sun.swingset3.demos;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * @author Pavel Porvatov
 * @author Eugen Hanussek https://github.com/homebeaver (avoid import of jnlp)
 */
public class DemoUtilities {
	
    private static final Logger LOG = Logger.getLogger(DemoUtilities.class.getName());

    private DemoUtilities() {
    	LOG.info("// Hide constructor");  // all mathods are static      
    }

    public static void setToplevelLocation(Window toplevel, Component component, int relativePosition) {

        Rectangle compBounds = component.getBounds();

        // Convert component location to screen coordinates
        Point p = new Point();
        SwingUtilities.convertPointToScreen(p, component);

        int x;
        int y;

        // Set frame location to be centered on panel
        switch (relativePosition) {
            case SwingConstants.NORTH: {
                x = (p.x + (compBounds.width / 2)) - (toplevel.getWidth() / 2);
                y = p.y - toplevel.getHeight();
                break;
            }
            case SwingConstants.EAST: {
                x = p.x + compBounds.width;
                y = (p.y + (compBounds.height / 2)) - (toplevel.getHeight() / 2);
                break;
            }
            case SwingConstants.SOUTH: {
                x = (p.x + (compBounds.width / 2)) - (toplevel.getWidth() / 2);
                y = p.y + compBounds.height;
                break;
            }
            case SwingConstants.WEST: {
                x = p.x - toplevel.getWidth();
                y = (p.y + (compBounds.height / 2)) - (toplevel.getHeight() / 2);
                break;
            }
            case SwingConstants.NORTH_EAST: {
                x = p.x + compBounds.width;
                y = p.y - toplevel.getHeight();
                break;
            }
            case SwingConstants.NORTH_WEST: {
                x = p.x - toplevel.getWidth();
                y = p.y - toplevel.getHeight();
                break;
            }
            case SwingConstants.SOUTH_EAST: {
                x = p.x + compBounds.width;
                y = p.y + compBounds.height;
                break;
            }
            case SwingConstants.SOUTH_WEST: {
                x = p.x - toplevel.getWidth();
                y = p.y + compBounds.height;
                break;
            }
            default:
            case SwingConstants.CENTER: {
                x = (p.x + (compBounds.width / 2)) - (toplevel.getWidth() / 2);
                y = (p.y + (compBounds.height / 2)) - (toplevel.getHeight() / 2);
            }
        }
        toplevel.setLocation(x, y);
    }

    // do not import jnlp : javax.jnlp.UnavailableServiceException extends java.lang.Exception
    public static boolean browse(URI uri) throws IOException, Exception {
    	LOG.info("Try using the Desktop api first");              	
        try {
        	// On some platforms the Desktop API may not besupported; 
        	// use the isDesktopSupported method todetermine if the current desktop is supported.
        	if(Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.browse(uri);
                return true;
        	} else {
            	LOG.warning("Desktop not besupported on this platform.");              	
                return false;
        	}

        } catch (SecurityException e) {
        	LOG.info("Running in sandbox, try using WebStart service");              	
//        	javax.jnlp.BasicService basicService =
//                    (javax.jnlp.BasicService) javax.jnlp.ServiceManager.lookup("javax.jnlp.BasicService");
        	ClassLoader cl = ClassLoader.getSystemClassLoader();
        	Class<?> typeServiceManager = cl.loadClass("javax.jnlp.ServiceManager");
        	Method lookup = typeServiceManager.getMethod("lookup", String.class);
        	Object basicService = lookup.invoke("javax.jnlp.BasicService");

        	Class<?> typeBasicService = cl.loadClass("javax.jnlp.BasicService");
        	Method isWebBrowserSupported = typeBasicService.getMethod("isWebBrowserSupported");
        	Method showDocument = typeBasicService.getMethod("showDocument", URL.class);
        	Object bool = isWebBrowserSupported.invoke(typeBasicService.cast(basicService));
//            if (basicService.isWebBrowserSupported()) {
//                return basicService.showDocument(uri.toURL());
//            }
        	if(Boolean.TRUE.equals(bool)) {
        		Object ret = showDocument.invoke(typeBasicService.cast(basicService), uri.toURL());
        		return (Boolean)ret;
        	}
        }

        return false;
    }
}
