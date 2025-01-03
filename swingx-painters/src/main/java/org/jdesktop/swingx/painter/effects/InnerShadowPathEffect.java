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
package org.jdesktop.swingx.painter.effects;

import java.awt.Color;
import java.awt.Point;

/**
 * An effect which draws a shadow inside the path painter.
 * @author joshy


    public ShadowPathEffect() {
        super();
        setBrushColor(Color.BLACK);
        setRenderInsideShape(false);
        setShouldFillShape(true);
        setOffset(new Point(3,3));
    }


 */
public class InnerShadowPathEffect extends AbstractAreaEffect {
    
    /** Creates a new instance of InnerShadowPathEffect */
    public InnerShadowPathEffect() {
        super();
        setRenderInsideShape(true);
        setBrushColor(Color.BLACK);
        setOffset(new Point(2,2));
    }
    
}
