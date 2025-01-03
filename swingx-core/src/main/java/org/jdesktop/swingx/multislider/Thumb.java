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
package org.jdesktop.swingx.multislider;

/**
 * @author jm158417 Joshua Marinacci joshy
 */
public class Thumb<E> {
	
    private float position;
    private E object;
    private MultiThumbModel<E> model;

    /** Creates a new instance of Thumb */
    public Thumb(MultiThumbModel<E> model) {
        this.model = model;
    }

    public float getPosition() {
        return position;
    }

    public void setPosition(float position) {
        this.position = position;
        model.thumbPositionChanged(this);
    }

    public E getObject() {
        return object;
    }

    public void setObject(E object) {
        this.object = object;
        model.thumbValueChanged(this);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer("[");
        buffer.append(this.getClass().toString());
        buffer.append(":");
        buffer.append("position");
        buffer.append('=');
        buffer.append(position);
        buffer.append(',');
        
        buffer.append("object");
        buffer.append('=');
        buffer.append(object.toString());
        buffer.append(',');
        
        buffer.append("model");
        buffer.append('=');
        buffer.append(model.toString());
        buffer.append(']');

        return buffer.toString();
    }
}