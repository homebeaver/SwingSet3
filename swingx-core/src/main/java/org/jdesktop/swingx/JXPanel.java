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
package org.jdesktop.swingx;

import static org.jdesktop.swingx.util.GraphicsUtilities.createCompatibleTranslucentImage;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Painter;
import javax.swing.RepaintManager;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.synth.SynthLookAndFeel;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.painter.AbstractPainter;
import org.jdesktop.swingx.util.Contract;
import org.jdesktop.swingx.util.JVM;

/**
 * <p>
 * An extended {@code JPanel} that provides additional features.
 * </p>
 * <h2>Scrollable</h2>
 * <p>
 * {@code JXPanel} is {@link Scrollable} by default. It provides reasonable implementations of all
 * of the interface methods. In addition, it supports the setting of common scrolling approaches
 * defined in {@link ScrollableSizeHint}.
 * </p>
 * <h3>Alpha Support</h3>
 * <p>
 * {@code JXPanel} has full alpha-channel support. This means that the {@code JXPanel} can be made
 * fully or partially transparent. This means that the JXPanel and all of its children will behave
 * as a single paint at the specified alpha value. <strong>Cauton:</strong> best practice is to use
 * either alpha support or opacity support, but not both. See the documentation on the methods for
 * further information.
 * </p>
 * <p>
 * A transparency example, this following code will show the black background of the parent:
 * 
 * <pre>
 * JXPanel panel = new JXPanel();
 * panel.add(new JButton(&quot;Push Me&quot;));
 * panel.setAlpha(.5f);
 * 
 * container.setBackground(Color.BLACK);
 * container.add(panel);
 * </pre>
 * 
 * <h3>Painter Support</h3>
 * <p>
 * {@code JXPanel} has support for {@linkplain Painter}s.
 * </p>
 * <p>
 * A painter example, this following code will show how to add a simple painter:
 * 
 * <pre>
 * JXPanel panel = new JXPanel();
 * panel.setBackgroundPainter(new PinstripePainter());
 * </pre>
 * 
 * @author rbair
 * @see Scrollable
 * @see Painter
 */
@JavaBean
@SuppressWarnings("serial") // super Same-version serialization only
public class JXPanel extends JPanel implements AlphaPaintable, BackgroundPaintable<JComponent>, Scrollable {
	
    private static final Logger LOG = Logger.getLogger(JXPanel.class.getName());

    private ScrollableSizeHint scrollableWidthHint = ScrollableSizeHint.FIT;
    private ScrollableSizeHint scrollableHeightHint = ScrollableSizeHint.FIT;
    
    /**
     * The alpha level for this component.
     */
    private volatile float alpha = 1.0f;
    /**
     * If the old alpha value was 1.0, I keep track of the opaque setting because
     * a translucent component is not opaque, but I want to be able to restore
     * opacity to its default setting if the alpha is 1.0. Honestly, I don't know
     * if this is necessary or not, but it sounded good on paper :)
     * <p>TODO: Check whether this variable is necessary or not</p>
     */
    private boolean oldOpaque;
    
    private float oldAlpha = 1f;
    
    /**
     * Indicates whether this component should inherit its parent alpha value
     */
    private boolean inheritAlpha = true;
    
    /**
     * Specifies the Painter to use for painting the background of this panel.
     * If no painter is specified, the normal painting routine for JPanel is called. 
     * Old behavior is also honored for the time being if no backgroundPainter is specified
     */
    private Painter<? super JComponent> backgroundPainter;

    /**
     * The backgroundPainter of this component paints the background underneath the border.
     * 
     * @see #isPaintBorderInsets()
     */
    private boolean paintBorderInsets = true;

    /**
     * The listener installed on the current backgroundPainter, if any.
     */
    private PropertyChangeListener painterChangeListener;
    
    /**
     * Creates a new <code>JXPanel</code> with a double buffer
     * and a flow layout.
     */
    public JXPanel() {
    }
    
    /**
     * Creates a new <code>JXPanel</code> with <code>FlowLayout</code>
     * and the specified buffering strategy.
     * If <code>isDoubleBuffered</code> is true, the <code>JXPanel</code>
     * will use a double buffer.
     *
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free 
     *        updates
     */
    public JXPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }
    
    /**
     * Create a new buffered JXPanel with the specified layout manager
     *
     * @param layout  the LayoutManager to use
     */
    public JXPanel(LayoutManager layout) {
        super(layout);
    }
    
    /**
     * Creates a new JXPanel with the specified layout manager and buffering
     * strategy.
     *
     * @param layout  the LayoutManager to use
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free 
     *        updates
     */
    public JXPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Setting the component to be opaque will reset the alpha setting to {@code 1f} (full
     * opaqueness). Setting the component to be non-opaque will restore the previous alpha
     * transparency. If the component is non-opaque with a fully-opaque alpha value ({@code 1f}),
     * the behavior should be the same as as a {@code JPanel} that is non-opaque.
     */
    @Override
    public void setOpaque(boolean opaque) {
        if (isPatch()) {
            setOpaquePatch(opaque);
            return;
        }
        if (opaque) {
            oldAlpha = getAlpha();
            
            if (oldAlpha < 1f) {
                setAlpha(1f);
            } else {
                super.setOpaque(true);
                repaint();
            }
        } else if (getAlpha() == 1f) {
            if (oldAlpha == 1f) {
                super.setOpaque(false);
                repaint();
            } else {
                setAlpha(oldAlpha);
            }
        }
    }
    
    @Override
    public boolean isOpaque() {
        if (isPatch()) {
            return isOpaquePatch();
        }
        return super.isOpaque();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getAlpha() {
        return alpha;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlpha(float alpha) {
        if (isPatch()) {
            setAlphaPatch(alpha);
            return;
        }
        if (alpha < 0f || alpha > 1f) {
            throw new IllegalArgumentException("invalid alpha value " + alpha);
        }
        
        float oldValue = getAlpha();
        this.alpha = alpha;
        
        if (getAlpha() < 1f) {
            if (oldValue == 1) {
                //it used to be 1, but now is not. Save the oldOpaque
                oldOpaque = isOpaque();
                super.setOpaque(false);
            }
            
            installRepaintManager();
        } else {
            uninstallRepaintManager();
            
            //restore the oldOpaque if it was true (since opaque is false now)
            if (oldOpaque) {
                super.setOpaque(true);
            }
        }
        
        firePropertyChange("alpha", oldValue, getAlpha());
        repaint();
    }
    
    /**
     * experimental version: doesn't tweak opaque
     * 
     * called if isPatch
     * @param alpha
     */
    private void setAlphaPatch(float alpha) {
        if (alpha < 0f || alpha > 1f) {
            throw new IllegalArgumentException("invalid alpha value " + alpha);
        }
        
        float oldValue = getAlpha();
        this.alpha = alpha;
        
        if (getAlpha() < 1f) {
            if (oldValue == 1) {
                //it used to be 1, but now is not. Save the oldOpaque
                oldOpaque = isOpaque();
//                super.setOpaque(false);
            }
            
            installRepaintManager();
        } else {
            uninstallRepaintManager();
            
            //restore the oldOpaque if it was true (since opaque is false now)
            if (oldOpaque) {
//                super.setOpaque(true);
            }
        }
        
        firePropertyChange("alpha", oldValue, getAlpha());
        repaint();
    }
    
    void installRepaintManager() {
        if (!JVM.current().isOrLater(JVM.JDK1_7)) {
            RepaintManager manager = RepaintManager.currentManager(this);
            RepaintManager trm = SwingXUtilities.getTranslucentRepaintManager(manager);
            RepaintManager.setCurrentManager(trm);
        }
    }
    
    void uninstallRepaintManager() {
        //TODO uninstall TranslucentRepaintManager when no more non-opaque JXPanel's exist
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public float getEffectiveAlpha() {
        float a = getAlpha();
        
        if (isInheritAlpha()) {
            for (Component c = getParent(); c != null; c = c.getParent()) {
                if (c instanceof AlphaPaintable) {
                    a = Math.min(((AlphaPaintable) c).getEffectiveAlpha(), a);
                    break;
                }
            }
        }
        
        return a;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInheritAlpha() {
        return inheritAlpha;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInheritAlpha(boolean val) {
        boolean oldValue = isInheritAlpha();
        inheritAlpha = val;
        firePropertyChange("inheritAlpha", oldValue, isInheritAlpha());
    }
    
    /**
     * Sets the horizontal sizing hint. The hint is used by the Scrollable implementation
     * to service the getScrollableTracksWidth.
     * 
     * @param hint the horizontal sizing hint, must not be null
     *   and must be vertical.
     * 
     * @throws NullPointerException if null
     * 
     * @see #setScrollableHeightHint(ScrollableSizeHint)
     * @see ScrollableSizeHint
     */
    public final void setScrollableWidthHint(ScrollableSizeHint hint) {
        Contract.asNotNull(hint, "hint cannot be null");
        ScrollableSizeHint oldValue = getScrollableWidthHint();
        if (oldValue == hint) return;
        this.scrollableWidthHint = hint;
        revalidate();
        firePropertyChange("scrollableWidthHint", oldValue, getScrollableWidthHint());
    }
    
    
    /**
     * Sets the vertical sizing hint. The hint is used by the Scrollable implementation
     * to service the getScrollableTracksHeight.
     * 
     * @param hint the vertical sizing hint, must not be null
     *   and must be vertical.
     * 
     * @throws NullPointerException if null
     * 
     * @see #setScrollableWidthHint(ScrollableSizeHint)
     * @see ScrollableSizeHint
     */
    public final void setScrollableHeightHint(ScrollableSizeHint hint) {
        Contract.asNotNull(hint, "hint cannot be null");
        ScrollableSizeHint oldValue = getScrollableHeightHint();
        if (oldValue == hint) return;
        this.scrollableHeightHint = hint;
        revalidate();
        firePropertyChange("scrollableHeightHint", oldValue, getScrollableHeightHint());
    }
    
    /**
     * get scrollableWidthHint
     * @return ScrollableSizeHint
     */
    protected ScrollableSizeHint getScrollableWidthHint() {
        return scrollableWidthHint;
    }
    
    /**
     * get scrollableHeightHint
     * @return ScrollableSizeHint
     */
    protected ScrollableSizeHint getScrollableHeightHint() {
        return scrollableHeightHint;
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return scrollableHeightHint.getTracksParentSize(this, SwingConstants.VERTICAL);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return scrollableWidthHint.getTracksParentSize(this, SwingConstants.HORIZONTAL);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            return visibleRect.height;
        } else if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width;
        } else {
            throw new IllegalArgumentException("invalid orientation"); //$NON-NLS-1$
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getScrollableBlockIncrement(visibleRect, orientation, direction) / 10;
    }
    
    /**
     * 
     * Sets the vertical size tracking to either ScrollableSizeTrack.FIT or NONE, if the
     * boolean parameter is true or false, respectively.<p>
     * 
     * <b>NOTE</b>: this method is kept for backward compatibility only, for full 
     * control use setScrollableHeightHint.
     * 
     * @param scrollableTracksViewportHeight The scrollableTracksViewportHeight to set.
     * 
     * @see #setScrollableHeightHint(ScrollableSizeHint)
     */
    public void setScrollableTracksViewportHeight(boolean scrollableTracksViewportHeight) {
        setScrollableHeightHint(scrollableTracksViewportHeight ? 
                ScrollableSizeHint.FIT : ScrollableSizeHint.NONE);
    }
    /**
     * Sets the horizontal size tracking to either ScrollableSizeTrack.FIT or NONE, if the
     * boolean parameter is true or false, respectively.<p>
     * 
     * <b>NOTE</b>: this method is kept for backward compatibility only, for full 
     * control use setScrollableWidthHint.
     * 
     * 
     * @param scrollableTracksViewportWidth The scrollableTracksViewportWidth to set.
     * 
     * @see #setScrollableWidthHint(ScrollableSizeHint)
     */
    public void setScrollableTracksViewportWidth(boolean scrollableTracksViewportWidth) {
        setScrollableWidthHint(scrollableTracksViewportWidth ? 
                ScrollableSizeHint.FIT : ScrollableSizeHint.NONE);
    }
    
    /**
     * @return a listener for painter change events
     */
    protected PropertyChangeListener getPainterChangeListener() {
        if (painterChangeListener == null) {
            painterChangeListener = new PropertyChangeListener() {
                
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    repaint();
                }
            };
        }
        return painterChangeListener;
    }

    /**
     * Sets a Painter to use to paint the background of this JXPanel.
     * 
     * @param p the new painter
     * @see #getBackgroundPainter()
     */
    @Override
	// implements interface BackgroundPaintable<T> method void setBackgroundPainter(Painter<T> painter)
    public void setBackgroundPainter(Painter<? super JComponent> p) {
    	Painter<? super JComponent> old = getBackgroundPainter();
        if (old instanceof AbstractPainter) {
            ((AbstractPainter<?>) old).removePropertyChangeListener(painterChangeListener);
        }
        backgroundPainter = p;
        if (backgroundPainter instanceof AbstractPainter) {
            ((AbstractPainter<?>) backgroundPainter).addPropertyChangeListener(getPainterChangeListener());
        }
        firePropertyChange("backgroundPainter", old, getBackgroundPainter());
        repaint();
    }
    
    /**
     * Returns the current background painter. The default value of this property 
     * is a painter which draws the normal JPanel background according to the current look and feel.
     * 
     * @return the current painter
     * @see #setBackgroundPainter(Painter)
     * @see #isPaintBorderInsets()
     */
	@Override
	// implements interface BackgroundPaintable<T> method Painter<T> getBackgroundPainter()
    public Painter<? super JComponent> getBackgroundPainter() {
		return backgroundPainter;
    }
    
    /**
     * Returns true if the background painter should paint where the border is
     * or false if it should only paint inside the border. This property is 
     * true by default. This property affects the width, height,
     * and initial transform passed to the background painter.
     */
    @Override
	// implements interface BackgroundPaintable<T> method boolean isPaintBorderInsets()
    public boolean isPaintBorderInsets() {
        return paintBorderInsets;
    }
    
    /**
     * Sets the paintBorderInsets property.
     * Set to true if the background painter should paint where the border is
     * or false if it should only paint inside the border. This property is true by default.
     * This property affects the width, height,
     * and initial transform passed to the background painter.
     * 
     * This is a bound property.
     */
    @Override
	// implements interface BackgroundPaintable<T> method void setPaintBorderInsets(boolean paintBorderInsets)
    public void setPaintBorderInsets(boolean paintBorderInsets) {
        boolean old = this.isPaintBorderInsets();
        this.paintBorderInsets = paintBorderInsets;
        firePropertyChange("paintBorderInsets", old, isPaintBorderInsets());
    }
    
    //support for Java 7 painting improvements
    protected boolean isPaintingOrigin() {
        return getAlpha() < 1f;
    }

    /**
     * Overridden paint method to take into account the alpha setting.
     * 
     * @param g
     *            the <code>Graphics</code> context in which to paint
     */
    @Override
    public void paint(Graphics g) {
        //short circuit painting if no transparency
        if (getAlpha() == 1f) {
            super.paint(g);
/*
  reproduce: 
- open SwingSet Demos
- switch to Nimbus
- start XTree Demo
//- move cursor to "Rock"
- switch back to Metal (XTree Panel ist still Nimbus)
- focus XTree Panel ===> exception
  
Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException: Cannot invoke "java.awt.Font.hashCode()" because "font" is null
	at java.desktop/sun.font.FontDesignMetrics$MetricsKey.init(FontDesignMetrics.java:219)
	at java.desktop/sun.font.FontDesignMetrics.getMetrics(FontDesignMetrics.java:288)
	at java.desktop/sun.swing.SwingUtilities2.getFontMetrics(SwingUtilities2.java:1242)
	at java.desktop/javax.swing.JComponent.getFontMetrics(JComponent.java:1691)
	at java.desktop/sun.swing.SwingUtilities2.getFontMetrics(SwingUtilities2.java:355)
	at java.desktop/javax.swing.plaf.synth.SynthTabbedPaneUI.paintTab(SynthTabbedPaneUI.java:648)
	at java.desktop/javax.swing.plaf.synth.SynthTabbedPaneUI.paintTabArea(SynthTabbedPaneUI.java:548)
	at java.desktop/javax.swing.plaf.synth.SynthTabbedPaneUI.paint(SynthTabbedPaneUI.java:486)
	at java.desktop/javax.swing.plaf.synth.SynthTabbedPaneUI.update(SynthTabbedPaneUI.java:384)
	at java.desktop/javax.swing.JComponent.paintComponent(JComponent.java:842)
	at java.desktop/javax.swing.JComponent.paint(JComponent.java:1119)
	at java.desktop/javax.swing.JComponent.paintChildren(JComponent.java:952)
	at java.desktop/javax.swing.JComponent.paint(JComponent.java:1128)
	at org.jdesktop.swingx.JXPanel.paint(JXPanel.java:592) <===================
	at java.desktop/javax.swing.JComponent.paintToOffscreen(JComponent.java:5311)
	at java.desktop/javax.swing.RepaintManager$PaintManager.paintDoubleBufferedFPScales(RepaintManager.java:1721)
	at java.desktop/javax.swing.RepaintManager$PaintManager.paintDoubleBuffered(RepaintManager.java:1630)
	at java.desktop/javax.swing.RepaintManager$PaintManager.paint(RepaintManager.java:1570)
	at java.desktop/javax.swing.RepaintManager.paint(RepaintManager.java:1337)
	at java.desktop/javax.swing.JComponent._paintImmediately(JComponent.java:5259)
	at java.desktop/javax.swing.JComponent.paintImmediately(JComponent.java:5069)
	at java.desktop/javax.swing.RepaintManager$4.run(RepaintManager.java:879)
	at java.desktop/javax.swing.RepaintManager$4.run(RepaintManager.java:862)
	at java.base/java.security.AccessController.doPrivileged(AccessController.java:399)
	at java.base/java.security.ProtectionDomain$JavaSecurityAccessImpl.doIntersectionPrivilege(ProtectionDomain.java:86)
	at java.desktop/javax.swing.RepaintManager.paintDirtyRegions(RepaintManager.java:862)
	at java.desktop/javax.swing.RepaintManager.paintDirtyRegions(RepaintManager.java:835)
	at java.desktop/javax.swing.RepaintManager.prePaintDirtyRegions(RepaintManager.java:784)
	at java.desktop/javax.swing.RepaintManager$ProcessingRunnable.run(RepaintManager.java:1898)
	at java.desktop/java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:318)
	at java.desktop/java.awt.EventQueue.dispatchEventImpl(EventQueue.java:771)
	at java.desktop/java.awt.EventQueue$4.run(EventQueue.java:722)
	at java.desktop/java.awt.EventQueue$4.run(EventQueue.java:716)
	at java.base/java.security.AccessController.doPrivileged(AccessController.java:399)
	at java.base/java.security.ProtectionDomain$JavaSecurityAccessImpl.doIntersectionPrivilege(ProtectionDomain.java:86)
	at java.desktop/java.awt.EventQueue.dispatchEvent(EventQueue.java:741)
	at java.desktop/java.awt.EventDispatchThread.pumpOneEventForFilters(EventDispatchThread.java:203)
	at java.desktop/java.awt.EventDispatchThread.pumpEventsForFilter(EventDispatchThread.java:124)
	at java.desktop/java.awt.EventDispatchThread.pumpEventsForHierarchy(EventDispatchThread.java:113)
	at java.desktop/java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:109)
	at java.desktop/java.awt.EventDispatchThread.pumpEvents(EventDispatchThread.java:101)
	at java.desktop/java.awt.EventDispatchThread.run(EventDispatchThread.java:90)

  
 */
        } else {
            //the component is translucent, so we need to render to
            //an intermediate image before painting
            // TODO should we cache this image? repaint to same image unless size changes?
            BufferedImage img = createCompatibleTranslucentImage(getWidth(), getHeight());
            Graphics2D gfx = img.createGraphics();
            
            try {
                super.paint(gfx);
            } finally {
                gfx.dispose();
            }
            
            Graphics2D g2d = (Graphics2D) g;
            Composite oldComp = g2d.getComposite();
            
            try {
                Composite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getEffectiveAlpha());
                g2d.setComposite(alphaComp);
                //TODO should we cache the image?
                g2d.drawImage(img, null, 0, 0);
            } finally {
                g2d.setComposite(oldComp);
            }
        }
    }
    
    /**
     * Overridden to provide Painter support. It will call backgroundPainter.paint()
     * if it is not null, else it will call super.paintComponent().
     * 
     * @param g
     *            the <code>Graphics</code> context in which to paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (isPatch()) {
            paintComponentPatch(g);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        
        try {
            // we should be painting the background behind the painter if we have one
            // this prevents issues with buffer reuse where visual artifacts sneak in
            if (isOpaque() || UIManager.getLookAndFeel() instanceof SynthLookAndFeel) {
                //this will paint the foreground if a JXPanel subclass is 
                //unfortunate enough to have one
                super.paintComponent(g2);
            } else if (getAlpha() < 1f) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            
            if (getBackgroundPainter() != null) {
            	Painter<? super JComponent> bp = getBackgroundPainter();
        		LOG.fine("backgroundPainter bp type:"+bp.getClass());

                if (isPaintBorderInsets()) {
                    bp.paint(g2, this, getWidth(), getHeight());
                } else {
                    Insets insets = getInsets();
                    g.translate(insets.left, insets.top);
                    bp.paint(g2, this, getWidth() - insets.left - insets.right,
                            getHeight() - insets.top - insets.bottom);
                    g.translate(-insets.left, -insets.top);
                }
            }
            
            //force the foreground to paint again...workaround for folks that 
            //incorrectly extend JXPanel instead of JComponent
            getUI().paint(g2, this);
        } finally {
            g2.dispose();
        }
    }
 
//--------------------- experimental patch
        
    protected boolean isPatch() {
        return Boolean.TRUE.equals(UIManager.get("JXPanel.patch"));
    }

    boolean fakeTransparent;
    
    protected void paintComponentPatch(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        
        try {
            if (isPaintingBackground()) {
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            if (getBackgroundPainter() != null) {
            	Painter<? super JComponent> bp = getBackgroundPainter();
        		LOG.fine("backgroundPainter bp type:"+bp.getClass());
                bp.paint(g2, this, getWidth(), getHeight());
            }
            fakeTransparent = true;
            getUI().update(g2, this);
        } finally {
            g2.dispose();
            fakeTransparent = false;
        }
        
    }
    
    /**
     * when background should be painted returns if patch is opaque
     * @return true if this component is completely opaque.
     */
    protected boolean isOpaquePatch() {
        if (fakeTransparent) return false;
        if (isPaintingBackground()) {
            return !isTransparentBackground() && !isAlpha();
        }
        return false;
    }

    protected void setOpaquePatch(boolean opaque) {
        super.setOpaque(opaque);
    }
    /**
     * Returns whether or not the container hierarchy below is transparent.
     * 
     * @return true if getAlpha() in AlphaPaintable &lt; 1.0
     */
    protected boolean isAlpha() {
        // PENDING JW: use effective alpha?
        return getAlpha() < 1.0f;
    }

    /**
     * Returns whether or not the background is transparent.
     * 
     * @return true if background'a Alpha &lt; 255
     */
    protected boolean isTransparentBackground() {
        return getBackground().getAlpha() < 255;
    }

    /**
     * Returns whether or not the background should be painted.
     * 
     * @return true if panel is opaque
     */
    protected boolean isPaintingBackground() {
        return super.isOpaque();
    }
    
}
