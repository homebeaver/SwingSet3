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
package org.jdesktop.swingx.painter;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Painter;

import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author rbair
 */
@SuppressWarnings({"nls", "unchecked", "rawtypes"})
public class CompoundPainterTest extends AbstractPainterTest {
	
    /**
     * {@inheritDoc}
     */
    @Override
    protected CompoundPainter createTestingPainter() {
        return new CompoundPainter();
    }
    
    /**
     * TODO remove when the compound painter does not start dirty 
     */
    private void copyOfSuper_testDefaultsWithCorrectedValues() {
        assertThat(p.getFilters().length, CoreMatchers.is(0));
        assertThat(p.getInterpolation(), CoreMatchers.is(AbstractPainter.Interpolation.NearestNeighbor));
        assertThat(p.isAntialiasing(), CoreMatchers.is(true));
        assertThat(p.isCacheable(), CoreMatchers.is(false));
        assertThat(p.isCacheCleared(), CoreMatchers.is(true));
        //TODO why does CompoundPainter start dirty?
        assertThat(p.isDirty(), CoreMatchers.is(true));
        assertThat(p.isInPaintContext(), CoreMatchers.is(false));
        assertThat(p.isVisible(), CoreMatchers.is(true));
        assertThat(p.shouldUseCache(), CoreMatchers.is(false));
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Overridden for CompoundPainter defaults.
     */
    @Test
    @Override
    public void testDefaults() {
        //TODO replace with super.testDefaults() when corrected
        copyOfSuper_testDefaultsWithCorrectedValues();
//        super.testDefaults();
        
        CompoundPainter cp = (CompoundPainter) p;
        assertThat(cp.getPainters(), CoreMatchers.is(new Painter[0]));
        assertThat(cp.getTransform(), CoreMatchers.is(nullValue()));
        assertThat(cp.isCheckingDirtyChildPainters(), CoreMatchers.is(true));
        assertThat(cp.isClipPreserved(), CoreMatchers.is(false));
    }

    /**
     * Issue #497-swingx: setPainters can't cope with null.
     * 
     */
    @Test
    public void testSetNullPainters() {
        CompoundPainter<Object> painter = new CompoundPainter<Object>();
        // changed to cast to Painter, since uncasted it is equivalent to
        // Painter[], which is checked in the next test
        painter.setPainters((Painter<?>) null);
    }
    
    /**
     * Issue #497-swingx: setPainters can't cope with null.
     *
     */
    @Test
    public void testSetEmptyPainters() {
        CompoundPainter<Object> painter = new CompoundPainter<Object>();
        // okay
        painter.setPainters();
        // fails
        painter.setPainters((Painter[]) null);
    }
    
    @Test
    public void testSetttingOnePainterDoesNotEnableCache() {
        ((CompoundPainter) p).setPainters(mock(Painter.class));
        
        assertThat(p.shouldUseCache(), CoreMatchers.is(false));
    }
    
    @Test
    @Ignore("not sure this is the right thing to do")
    public void testSettingMoreThanOnePainterEnablesCache() {
        ((CompoundPainter) p).setPainters(mock(Painter.class), mock(Painter.class));
        
        assertThat(p.shouldUseCache(), CoreMatchers.is(true));
    }
    
    /**
     * Issue #1218-swingx: must fire property change if contained painter
     *    changed.
     */
    public void testDirtyNotification() {
        AbstractPainter<Component> child = spy(new DummyPainter());
        ((CompoundPainter<?>) p).setPainters(child);
        
        assertThat(p.isDirty(), CoreMatchers.is(true));
        verify(child, never()).setDirty(true);
        
        p.paint(g, null, 10, 10);
        
        assertThat(p.isDirty(), CoreMatchers.is(false));
        
        PropertyChangeListener pcl = mock(PropertyChangeListener.class);
        p.addPropertyChangeListener(pcl);
        
        child.setDirty(true);
        
        assertThat(p.isDirty(), CoreMatchers.is(true));
        
        ArgumentCaptor<PropertyChangeEvent> captor = ArgumentCaptor.forClass(PropertyChangeEvent.class);
        verify(pcl).propertyChange(captor.capture());
        
        assertThat(captor.getValue().getSource(), CoreMatchers.<Object>is(sameInstance(p)));
        assertThat(captor.getValue().getPropertyName(), CoreMatchers.is("dirty"));
        assertThat(captor.getAllValues().size(), CoreMatchers.is(1));
    }
}
