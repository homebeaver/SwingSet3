/*
 * Copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
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
package org.jdesktop.swingx.icon;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import org.jdesktop.swingx.painter.Painter;

public class PainterIcon implements Icon {
	
    private int width;
    private int height;
    
    private Painter<Component> painter;
    
    public PainterIcon(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    public PainterIcon(Dimension size) {
    	this(Double.valueOf(size.getWidth()).intValue(), Double.valueOf(size.getHeight()).intValue());
    }

    public Painter<Component> getPainter() {
        return painter;
    }

    public void setPainter(Painter<Component> painter) {
        this.painter = painter;
    }

    
    // implements interface Icon:
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (getPainter() != null && g instanceof Graphics2D) {
            g = g.create();
            
            try {
                g.translate(x, y);
                getPainter().paint((Graphics2D) g, c, width, height);
                g.translate(-x, -y);
            } finally {
                g.dispose();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIconHeight() {
        return height;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getIconWidth() {
        return width;
    }    

}