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
package org.jdesktop.swingx.renderer;

import java.io.Serializable;


/**
 * A simple converter to return a String representation of an object.
 * 
 * This class is intended to be the "small coin" to configure/format textual
 * cell content of concrete subclasses of <code>ComponentProvider</code>.
 * <p>
 * 
 * F.i. to show a Contributor cell object as "Busywoman, Herta" implement a
 * custom StringValue and use it in a text rendering provider.
 * 
 * <pre><code>
 * StringValue stringValue = (Object value) -> {
 * 	if(value instanceof Contributor c) {
 * 		return c.lastName + &quot;, &quot; + contributor.firstName;
 * 	}
 * 	return TO_STRING.getString(value);
 * };
 * 
 * ComponentProvider provider = new LabelProvider(stringValue);
 * table.setDefaultRenderer(Contributor.class, new DefaultTableRenderer(provider));
 * </code></pre>
 * 
 * <p>
 * 
 * PENDING: use a full-fledged Format instead?
 * Would impose a higher burden onto implementors but could be re-used in editors.
 * 
 * @author Jeanette Winzenburg
 * 
 * @see ComponentProvider
 * @see LabelProvider
 * @see DefaultTableRenderer
 * @see DefaultListRenderer
 * @see DefaultTreeRenderer
 */
/*
Interfaces mit einer einzigen abstrakten Methode heißen neuerdings "Functional Interface Types" 
oder auch "SAM Types". "SAM" steht für "Single Abstract Method". 
Solche Interfaces können mit der Annotation @FunctionalInterface versehen werden 
und haben für Lambdas eine besondere Bedeutung, 
da sie die einzigen Typen sind, die als "Target-Typing-Zieltypen" in Frage kommen.

siehe: https://www.torsten-horn.de/techdocs/java-lambdas.htm#SAM

interface StringValue ist "SAM" !!!

 */
@FunctionalInterface
public interface StringValue extends Serializable {

    /**
     * Returns a string representation of the given value. <p>
     * 
     * PENDING JW: forgot - why not null return guaranteed?
     * 
     * @param value the object to present as a string
     * @return a string representation of the given value, 
     *  guaranteed to be not null
     */
    String getString(Object value);
}
