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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.Painter;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * A <code>JLabel</code> optimized for usage in renderers and
 * with a minimal background painter support. <p>
 * 
 * <i>Note</i>: the painter support will be switched to painter_work as 
 * soon it enters main. 
 * 
 * The reasoning for the performance-overrides is copied from core: <p>
 * 
 * The standard <code>JLabel</code> component was not
 * designed to be used this way and we want to avoid 
 * triggering a <code>revalidate</code> each time the
 * cell is drawn. This would greatly decrease performance because the
 * <code>revalidate</code> message would be
 * passed up the hierarchy of the container to determine whether any other
 * components would be affected.  
 * As the renderer is only parented for the lifetime of a painting operation
 * we similarly want to avoid the overhead associated with walking the
 * hierarchy for painting operations.
 * So this class
 * overrides the <code>validate</code>, <code>invalidate</code>,
 * <code>revalidate</code>, <code>repaint</code>, and
 * <code>firePropertyChange</code> methods to be 
 * no-ops and override the <code>isOpaque</code> method solely to improve
 * performance.  If you write your own renderer component,
 * please keep this performance consideration in mind.
 * 
 * @author Jeanette Winzenburg
 */
@SuppressWarnings("serial")
public class JRendererLabel extends JLabel implements PainterAware<Object>, IconAware {

	@Override
    protected void paintBorder(Graphics g) {
/* in JComponent:
        Border border = getBorder();
        if (border != null) {
            border.paintBorder(this, g, 0, 0, getWidth(), getHeight());
        }
    	
 */
//		Border border = getBorder();
//		System.out.println("JRendererLabel#paintBorder:"+border);
//		if(border instanceof EtchedBorder) {
//			EtchedBorder eb = (EtchedBorder)border;
//			System.out.println("EtchedBorder:"+eb
//			//eb.getEtchType() == LOWERED aka 1
//			+ " this.getBackground()====="+this.getBackground());
//			// getBackground() ist @Transient
//		}
		super.paintBorder(g);
    }
	
	/* interface javax.swing.Painter<T> with
	 *     public void paint(Graphics2D g, T object, int width, int height);
	 */
    protected Painter<JComponent> painter;

    public JRendererLabel() {
        super();
        setOpaque(true);
    }

    /**
     * {@inheritDoc}
     */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setPainter(Painter painter) {
        Painter<?> old = getPainter();
        this.painter = painter;
        firePropertyChange("painter", old, getPainter());
	}

    /**
     * {@inheritDoc}
     */
	@Override
    public Painter<?> getPainter() {
        return painter;
    }

	@Override
	public void paint(Graphics2D g, Object object, int width, int height) {
		super.paint(g);
	}

    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to inject Painter's painting. <p>
     * TODO: cleanup logic - see JRendererCheckBox.
     * 
     */
    @Override
    protected void paintComponent(Graphics g) {
        // JW: hack around for #1178-swingx (core issue) 
        // grab painting if Nimbus detected
        if ((painter != null) || isNimbus()) {
//        if (isNimbus()) {
            // we have a custom (background) painter
            // try to inject if possible
            // there's no guarantee - some LFs have their own background 
            // handling  elsewhere
            if (isOpaque()) {
                // replace the paintComponent completely 
                paintComponentWithPainter((Graphics2D) g);
            } else {
                // transparent apply the background painter before calling super
                paintPainter(g);
                super.paintComponent(g);
            }
        } else {
            // nothing to worry about - delegate to super
            super.paintComponent(g);
        }
    }

    /**
     * Hack around Nimbus not respecting background colors if UIResource.
     * So by-pass ... 
     * 
     * @return
     */
    private boolean isNimbus() {
        return UIManager.getLookAndFeel().getName().contains("Nimbus");
    }


    /**
     * 
     * Hack around AbstractPainter.paint bug which disposes the Graphics.
     * So here we give it a scratch to paint on. <p>
     * TODO - remove again, the issue is fixed?
     * 
     * @param g the graphics to paint on
     */
    private void paintPainter(Graphics g) {
        if (painter == null) return;
        // fail fast: we assume that g must not be null
        // which throws an NPE here instead deeper down the bowels
        // this differs from corresponding core implementation!
        Graphics2D scratch = (Graphics2D) g.create();
        try {
        	painter.paint(scratch, this, getWidth(), getHeight());
        }
        finally {
            scratch.dispose();
        }
    }
    
    /**
     * PRE: painter != null, isOpaque()
     * @param g Graphics2D
     */
    protected void paintComponentWithPainter(Graphics2D g) {
        // 1. be sure to fill the background
        // 2. paint the painter
        // by-pass ui.update and hook into ui.paint directly
        if (ui != null) {
            // fail fast: we assume that g must not be null
            // which throws an NPE here instead deeper down the bowels
            // this differs from corresponding core implementation!
            Graphics2D scratchGraphics = (Graphics2D) g.create();
                try {
                    scratchGraphics.setColor(getBackground());
                    scratchGraphics.fillRect(0, 0, getWidth(), getHeight());
                    paintPainter(g);
                    ui.paint(scratchGraphics, this);
                }
                finally {
                    scratchGraphics.dispose();
                }
        }        
    }

    
    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to not automatically de/register itself from/to the ToolTipManager.
     * As rendering component it is not considered to be active in any way, so the
     * manager must not listen. 
     */
    @Override
    public void setToolTipText(String text) {
        putClientProperty(TOOL_TIP_TEXT_KEY, text);
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void invalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void validate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void revalidate() {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void repaint(Rectangle r) { }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     *
     * @since 1.5
     */
    @Override
    public void repaint() {
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {  
        // Strings get interned...
        if ("text".equals(propertyName)) {
            super.firePropertyChange(propertyName, oldValue, newValue);
        }
    }

    /**
     * Overridden for performance reasons.
     * See the <a href="#override">Implementation Note</a> 
     * for more information.
     */
    @Override
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { }

}