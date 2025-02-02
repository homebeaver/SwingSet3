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
package org.jdesktop.swingx.color;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.event.MouseInputAdapter;

import org.jdesktop.swingx.JXGradientChooser;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.multislider.MultiThumbModel;
import org.jdesktop.swingx.multislider.Thumb;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * <p><b>Dependency</b>: Because this class relies on LinearGradientPaint and
 * RadialGradientPaint, it requires the optional MultipleGradientPaint.jar</p>
 *  
 * @author jm158417 Joshua Marinacci joshy
 */
/*
 * MultipleGradientPaint.jar gibt es als com/kenai/swingjavabuilderext/swingjavabuilderext-swingx/1.0.3
 * von 2009-04-15 22:34
 * darin nur eine Klasse: org.javabuilders.swing.swingx.SwingXConfig
 * 
 * LinearGradientPaint und RadialGradientPaint sind seit 1.6
 * in package java.awt 
 */
@SuppressWarnings("serial") // super Same-version serialization only
public class GradientPreviewPanel extends JXPanel {

    private Logger log = Logger.getLogger(GradientPreviewPanel.class.getName());

    private Paint checker_texture = null;
    private Point2D start, end;
    
    /** picker */
    public JXGradientChooser picker;
    boolean moving_start = false;
    boolean moving_end = false;
    private boolean radial = false;
    private boolean reversed = false;
    private boolean reflected = false;
    private boolean repeated = false;
    private MultipleGradientPaint gradient;
    
    /**
     * ctor
     */
    public GradientPreviewPanel() {
        start = new Point2D.Float(10,10);
        end = new Point2D.Float(80,10);
        checker_texture = PaintUtils.getCheckerPaint();
        MouseInputAdapter ma = new GradientMouseHandler();
        this.addMouseListener(ma);
        this.addMouseMotionListener(ma);
    }
    
    /**
     * repaint
     */
    public void setGradient() {
        repaint();
    }
    
    /**
     * 
     * @param grad MultipleGradientPaint
     */
    public void setGradient(MultipleGradientPaint grad) {
        MultipleGradientPaint old = getGradient();
        if(grad instanceof LinearGradientPaint) {
            LinearGradientPaint paint = (LinearGradientPaint)grad;
            this.start = paint.getStartPoint();
            this.end = paint.getEndPoint();
        } else {
            RadialGradientPaint paint = (RadialGradientPaint)grad;
            this.start = paint.getCenterPoint();
            this.end = new Point2D.Double(start.getX(),start.getY()+paint.getRadius());
        }
        this.gradient = grad;
        firePropertyChange("gradient", old, getGradient());
        repaint();
    }
    
    /**
     * 
     * @return MultipleGradientPaint
     */
    public MultipleGradientPaint getGradient() {
        return this.gradient;
    }

    /**
     * calculate a color gradient 
     * @return MultipleGradientPaint
     */
    public MultipleGradientPaint calculateGradient() {
        List<Thumb<Color>> stops = getStops();
        int len = stops.size();
        
        // set up the data for the gradient
        float[] fractions = new float[len];
        Color[] colors = new Color[len];
        int i = 0;
        for (Thumb<Color> thumb : stops) {
            colors[i] = (Color)thumb.getObject();
            fractions[i] = thumb.getPosition();
            i++;
        }
        
        // get the final gradient
        this.setGradient(calculateGradient(fractions, colors));
        return getGradient();
    }

    private MultiThumbModel<Color> model;
    
    private List<Thumb<Color>> getStops() {
        // calculate the color stops
        return model == null ? null : model.getSortedThumbs();
    }
    
    /**
     * 
     * @param model MultiThumbModel
     */
    public void setMultiThumbModel(MultiThumbModel<Color> model) {
        MultiThumbModel<Color> old = getMultiThumbModel();
        this.model = model;
        firePropertyChange("multiThumbModel", old, getMultiThumbModel());
    }
    
    /**
     * 
     * @return MultiThumbModel
     */
    public MultiThumbModel<Color> getMultiThumbModel() {
        return this.model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        try {
            Graphics2D g2 = (Graphics2D)g;
            
            // fill the background with checker first
            g2.setPaint(checker_texture);
            g.fillRect(0,0,getWidth(),getHeight());

        
            Paint paint = getGradient();
            // fill the area
            if(paint != null) {
                g2.setPaint(paint);
            } else {
                g2.setPaint(Color.black);
            }

            g.fillRect(0,0,getWidth(),getHeight());

            drawHandles(g2);
        } catch (Exception ex) {
            log .severe("ex: " + ex);
        }
    }
    
    private MultipleGradientPaint calculateGradient(final float[] fractions, final Color[] colors) {
        // set up the end points
        Point2D start = this.start;
        Point2D end = this.end;
        if(isReversed()) {
        //if(picker.reversedCheck.isSelected()) {
            start = this.end;
            end = this.start;
        }

        // set up the cycle type
        MultipleGradientPaint.CycleMethod cycle = MultipleGradientPaint.CycleMethod.NO_CYCLE;
        if(isRepeated()) {
        //if(picker.repeatedRadio.isSelected()) {
            cycle = MultipleGradientPaint.CycleMethod.REPEAT;
        }
        if(isReflected()) {
        //if(picker.reflectedRadio.isSelected()) {
            cycle = MultipleGradientPaint.CycleMethod.REFLECT;
        }
        
        // create the underlying gradient paint
        MultipleGradientPaint paint = null;
        if(isRadial()) { //picker.styleCombo.getSelectedItem().toString().equals("Radial")) {
            //@ConstructorProperties({ "centerPoint", "radius", "focusPoint", "fractions", "colors", "cycleMethod", "colorSpace", "transform" })
            paint = new RadialGradientPaint(start // centerPoint
            	, (float)start.distance(end) 	// Radius must be greater than zero TODO
            	, start
            	, fractions, colors, cycle, MultipleGradientPaint.ColorSpaceType.SRGB
            	, new AffineTransform() // Gradient transform cannot be null : use Identity transformation
            );
        } else {
        	//@ConstructorProperties({ "startPoint", "endPoint", "fractions", "colors", "cycleMethod", "colorSpace", "transform" })
            paint = new LinearGradientPaint(start // startPoint
            	, end // endPoint : Start point cannot equal endpoint TODO
            	, fractions, colors, cycle
            );
        }
        return paint;
    }
    
    private void drawHandles(final Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // draw the points and gradient line
        g2.setColor(Color.black);
        g2.drawOval((int)start.getX()-5,(int)start.getY()-5,10,10);
        g2.setColor(Color.white);
        g2.drawOval((int)start.getX()-4,(int)start.getY()-4,8,8);
        
        g2.setColor(Color.black);
        g2.drawOval((int)end.getX()-5,(int)end.getY()-5,10,10);
        g2.setColor(Color.white);
        g2.drawOval((int)end.getX()-4,(int)end.getY()-4,8,8);
        
        g2.setColor(Color.darkGray);
        g2.drawLine((int)start.getX(),(int)start.getY(), (int)end.getX(),(int)end.getY());
        g2.setColor(Color.gray);
        g2.drawLine((int)start.getX()-1,(int)start.getY()-1, (int)end.getX()-1,(int)end.getY()-1);
    }
    
    private class GradientMouseHandler extends MouseInputAdapter {

        @Override
        public void mousePressed(MouseEvent evt) {
            moving_start = false;
            moving_end = false;
            if (evt.getPoint().distance(start) < 5) {
                moving_start = true;
                start = evt.getPoint();
                return;
            }
            
            if (evt.getPoint().distance(end) < 5) {
                moving_end = true;
                end = evt.getPoint();
                return;
            }

            start = evt.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent evt) {
            if (moving_start) {
                start = evt.getPoint();
            } else {
                end = evt.getPoint();
            }
            calculateGradient();
            repaint();
        }
    }

    /**
     * 
     * @return radial
     */
    public boolean isRadial() {
        return radial;
    }

    /**
     * 
     * @param radial boolean
     */
    public void setRadial(boolean radial) {
        boolean old = isRadial();
        this.radial = radial;
        firePropertyChange("radial", old, isRadial());
    }

    /**
     * 
     * @return reversed
     */
    public boolean isReversed() {
        return reversed;
    }

    /**
     * 
     * @param reversed boolean
     */
    public void setReversed(boolean reversed) {
        boolean old = isReversed(); 
        this.reversed = reversed;
        firePropertyChange("reversed", old, isReversed());
    }

    /**
     * 
     * @return reflected
     */
    public boolean isReflected() {
        return reflected;
    }

    /**
     * 
     * @param reflected boolean
     */
    public void setReflected(boolean reflected) {
        boolean old = isReflected();
        this.reflected = reflected;
        firePropertyChange("reflected", old, isReflected());
    }

    /**
     * 
     * @return repeated
     */
    public boolean isRepeated() {
        return repeated;
    }

    /**
     * 
     * @param repeated boolean
     */
    public void setRepeated(boolean repeated) {
        boolean old = isRepeated();
        this.repeated = repeated;
        firePropertyChange("repeated", old, isRepeated());
    }
}

