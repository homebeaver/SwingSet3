/*
 * Copyright 2008 Sun Microsystems, Inc., 4150 Network Circle,
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
package org.jdesktop.swingx.autocomplete;

import java.awt.Color;
import java.awt.Font;

import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Karl George Schaefer
 */
public class AutoCompleteStyledDocument extends AutoCompleteDocument implements StyledDocument {
	
	/**
     * Creates a new AutoCompleteStyledDocument for the given AbstractAutoCompleteAdaptor.
	 * 
	 * @param adaptor AbstractAutoCompleteAdaptor
	 * @param strictMatching boolean
	 * @param stringConverter ObjectToStringConverter
	 * @param delegate StyledDocument
	 */
    public AutoCompleteStyledDocument(AbstractAutoCompleteAdaptor adaptor,
            boolean strictMatching, ObjectToStringConverter stringConverter,
            StyledDocument delegate) {
        super(adaptor, strictMatching, stringConverter, delegate);
    }

    /**
     * Creates a new AutoCompleteStyledDocument for the given AbstractAutoCompleteAdaptor.
     * 
     * @param adaptor The adaptor that will be used to find and select matching items.
     * @param strictMatching true, if only items from the adaptor's list should be allowed to be entered
     * @param stringConverter the converter used to transform items to strings
     */
    public AutoCompleteStyledDocument(AbstractAutoCompleteAdaptor adaptor,
            boolean strictMatching, ObjectToStringConverter stringConverter) {
        super(adaptor, strictMatching, stringConverter);
    }

    /**
     * Creates a new AutoCompleteStyledDocument for the given AbstractAutoCompleteAdaptor.
     * 
     * @param adaptor The adaptor that will be used to find and select matching items.
     * @param strictMatching true, if only items from the adaptor's list should be allowed to be entered
     */
    public AutoCompleteStyledDocument(AbstractAutoCompleteAdaptor adaptor, boolean strictMatching) {
        super(adaptor, strictMatching);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Document createDefaultDocument() {
        return new DefaultStyledDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Style addStyle(String nm, Style parent) {
        return ((StyledDocument) delegate).addStyle(nm, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getBackground(AttributeSet attr) {
        return ((StyledDocument) delegate).getBackground(attr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element getCharacterElement(int pos) {
        return ((StyledDocument) delegate).getCharacterElement(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Font getFont(AttributeSet attr) {
        return ((StyledDocument) delegate).getFont(attr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getForeground(AttributeSet attr) {
        return ((StyledDocument) delegate).getForeground(attr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Style getLogicalStyle(int p) {
        return ((StyledDocument) delegate).getLogicalStyle(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element getParagraphElement(int pos) {
        return ((StyledDocument) delegate).getParagraphElement(pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Style getStyle(String nm) {
        return ((StyledDocument) delegate).getStyle(nm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeStyle(String nm) {
        ((StyledDocument) delegate).removeStyle(nm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCharacterAttributes(int offset, int length,
            AttributeSet s, boolean replace) {
                ((StyledDocument) delegate).setCharacterAttributes(offset, length, s, replace);
            }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogicalStyle(int pos, Style s) {
        ((StyledDocument) delegate).setLogicalStyle(pos, s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParagraphAttributes(int offset, int length,
            AttributeSet s, boolean replace) {
                ((StyledDocument) delegate).setParagraphAttributes(offset, length, s, replace);
            }
}
