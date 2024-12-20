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
package org.jdesktop.swingx.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.painter.effects.AreaEffect;
import org.jdesktop.swingx.util.ShapeUtils;

/**
 * A painter which paints square and rounded rectangles
 * 
 * @author joshua.marinacci@sun.com
 */
// @see PainterDemo#createCompoundPainterDemos()
@JavaBean
public class RectanglePainter extends AbstractAreaPainter<Component> {

    /**
     * {@inheritDoc}
     */
    @Override // implements the abstract method AbstractPainter.doPaint
    protected void doPaint(Graphics2D g, Component component, int width, int height) {
        Shape shape = provideShape(g, component, width, height);
        switch (getStyle()) {
        case BOTH:
            drawBackground(g,shape,width,height);
            drawBorder(g,shape,width,height);
            break;
        case FILLED:
            drawBackground(g,shape,width,height);
            break;
        case OUTLINE:
            drawBorder(g,shape,width,height);
            break;
		case NONE:
//			break;
		default:
			break;
        }

        // background
        // border
        // leave the clip to support masking other painters
        ShapeUtils.mergeClip(g,shape);
        /*
        Area area = new Area(g.getClip());
        area.intersect(new Area(shape));//new Rectangle(0,0,width,height)));
        g.setClip(area);*/
        //g.setClip(shape);
    }

    private boolean rounded = false;
    //private Insets insets = new Insets(0,0,0,0);
    private int roundWidth = 20;
    private int roundHeight = 20;
    private int width = -1;
    private int height = -1;
    //private double strokeWidth = 1;

    /** Creates a new instance of RectanglePainter */
    public RectanglePainter() {
        this(0, 0, 0, 0);
    }
 
    /**
     * constructs a RectanglePainter
     * @param top param for the border Instets
     * @param left param for the border Instets
     * @param bottom param for the border Instets
     * @param right param for the border Instets
     */
    public RectanglePainter(int top, int left, int bottom, int right) {
        this(top, left, bottom, right, 0, 0);
    }

    /**
     * constructs a RectanglePainter
     * @param top param for the border Instets
     * @param left param for the border Instets
     * @param bottom param for the border Instets
     * @param right param for the border Instets
     * @param roundWidth width of the rounded rectangle
     * @param roundHeight height of the rounded rectangle
     */
    public RectanglePainter(int top, int left, int bottom, int right,
            int roundWidth, int roundHeight) {
        this(top, left, bottom, right, roundWidth, roundHeight, roundWidth != 0 || roundHeight != 0, Color.RED, 1f, Color.BLACK);
    }

    /**
     * constructs a RectanglePainter
     * @param top param for the border Instets
     * @param left param for the border Instets
     * @param bottom param for the border Instets
     * @param right param for the border Instets
     * @param roundWidth width of the rounded rectangle
     * @param roundHeight height of the rounded rectangle
     * @param rounded Indicates if the rectangle is rounded
     * @param fillPaint a Paint instance for fill paint
     * @param strokeWidth
     * @param borderPaint a Paint instance for border paint
     */
    public RectanglePainter(int top, int left, int bottom, int right, 
    		int roundWidth, int roundHeight, boolean rounded, 
    		Paint fillPaint, float strokeWidth, Paint borderPaint) {
        this(new Insets(top, left, bottom, right), -1, -1, roundWidth, roundHeight, rounded, fillPaint, strokeWidth, borderPaint);
    }

    /**
     * constructs a RectanglePainter
     * @param fillPaint a Paint instance for fill paint
     * @param borderPaint a Paint instance for border paint
     */
    public RectanglePainter(Color fillPaint, Color borderPaint) {
        this(fillPaint, borderPaint, 1f, null);
    }

    /**
     * constructs a RectanglePainter
     * @param fillPaint a Paint instance for fill paint
     * @param borderPaint a Paint instance for border paint
     * @param borderWidth
     * @param style fill style
     */
    public RectanglePainter(Paint fillPaint, Paint borderPaint, float borderWidth, Style style) {
        this(new Insets(0, 0, 0, 0), -1, -1, 0, 0, false, fillPaint, borderWidth, borderPaint);
        setStyle(style);
        setDirty(false);
    }

    /**
     * constructs a RectanglePainter
     * @param width width of the area to paint.
     * @param height height of the area to paint.
     * @param cornerRadius
     * @param fillPaint a Paint instance for fill paint
     */
    public RectanglePainter(int width, int height, int cornerRadius, Paint fillPaint) {
        this(new Insets(0, 0, 0, 0), width, height, cornerRadius, cornerRadius, true, fillPaint, 1f, Color.BLACK);
    }

    /**
     * constructs a RectanglePainter
     * @param insets the border Instets
     * @param width width of the area to paint.
     * @param height height of the area to paint.
     * @param roundWidth width of the rounded rectangle
     * @param roundHeight height of the rounded rectangle
     * @param rounded Indicates if the rectangle is rounded
     * @param fillPaint a Paint instance for fill paint
     * @param strokeWidth for border width
     * @param borderPaint a Paint instance for border paint
     */
    public RectanglePainter(Insets insets, int width, int height, int roundWidth, int roundHeight,
            boolean rounded, Paint fillPaint, float strokeWidth, Paint borderPaint) {
        this.width = width;
        this.height = height;
        setFillHorizontal(width < 0);
        setFillVertical(height < 0);
        setInsets(insets);
        this.roundWidth = roundWidth;
        this.roundHeight = roundHeight;
        this.rounded = rounded;
        this.setFillPaint(fillPaint);
        this.setBorderWidth(strokeWidth);
        this.setBorderPaint(borderPaint);
        this.setDirty(false);
    }

    /**
     * Indicates if the rectangle is rounded
     * @return if the rectangle is rounded
     */
    public boolean isRounded() {
        return rounded;
    }

    /**
     * sets if the rectangle should be rounded
     * @param rounded if the rectangle should be rounded
     */
    public void setRounded(boolean rounded) {
        boolean oldRounded = isRounded();
        this.rounded = rounded;
        setDirty(true);
        firePropertyChange("rounded",oldRounded,rounded);
    }

    /**
     * gets the round width of the rectangle
     * @return the current round width
     */
    public int getRoundWidth() {
        return roundWidth;
    }

    /**
     * sets the round width of the rectangle
     * @param roundWidth a new round width
     */
    public void setRoundWidth(int roundWidth) {
        int oldRoundWidth = getRoundWidth();
        this.roundWidth = roundWidth;
        setDirty(true);
        firePropertyChange("roundWidth",oldRoundWidth,roundWidth);
    }

    /**
     * gets the round height of the rectangle
     * @return the current round height
     */
    public int getRoundHeight() {
        return roundHeight;
    }

    /**
     * sets the round height of the rectangle
     * @param roundHeight a new round height
     */
    public void setRoundHeight(int roundHeight) {
        int oldRoundHeight = getRoundHeight();
        this.roundHeight = roundHeight;
        setDirty(true);
        firePropertyChange("roundHeight",oldRoundHeight,roundHeight);
    }


    /* ======== drawing code ============ */
    /**
     * calculating a RectangularShape
     * @param width width of the area to paint.
     * @param height height of the area to paint.
     * @return RectangularShape
     */
    protected RectangularShape calculateShape(int width, int height) {
        Insets insets = getInsets();
        int x = insets.left;
        int y = insets.top;

        // use the position calcs from the super class
        Rectangle bounds = calculateLayout(this.width, this.height, width, height);
        if(this.width != -1 && !isFillHorizontal()) {
            width = this.width;
            x = bounds.x;
        }
        if(this.height != -1 && !isFillVertical()) {
            height = this.height;
            y = bounds.y;
        }

        if(isFillHorizontal()) {
            width = width - insets.left - insets.right;
        }
        if(isFillVertical()) {
            height = height - insets.top - insets.bottom;
        }


        RectangularShape shape = new Rectangle2D.Double(x, y, width, height);
        if(rounded) {
            shape = new RoundRectangle2D.Double(x, y, width, height, roundWidth, roundHeight);
        }
        return shape;
    }

    private void drawBorder(Graphics2D g, Shape shape, int width, int height) {
        Paint p = getBorderPaint();
        if(isPaintStretched()) {
            p = calculateSnappedPaint(p, width, height);
        }

        g.setPaint(p);

        g.setStroke(new BasicStroke(getBorderWidth()));
        
        // shrink the border by 1 px
        if(shape instanceof Rectangle2D) {
            Rectangle2D rect = (Rectangle2D) shape;
            
            g.draw(new Rectangle2D.Double(rect.getX(), rect.getY(),
                    rect.getWidth()-1, rect.getHeight()-1));
        } else if(shape instanceof RoundRectangle2D) {
            RoundRectangle2D rect = (RoundRectangle2D) shape;
            
            g.draw(new RoundRectangle2D.Double(rect.getX(), rect.getY(),
                    rect.getWidth()-1, rect.getHeight()-1,
                    rect.getArcWidth(), rect.getArcHeight()));
        } else {
            g.draw(shape);
        }
    }

    private void drawBackground(Graphics2D g, Shape shape, int width, int height) {
        Paint p = getFillPaint();
        if(isPaintStretched()) {
            p = calculateSnappedPaint(p, width, height);
        }

        g.setPaint(p);

        g.fill(shape);
        if(getAreaEffects() != null) {
            for(AreaEffect ef : getAreaEffects()) {
                ef.apply(g, shape, width, height);
            }
        }
    }

    @Override
    protected Shape provideShape(Graphics2D g, Component comp, int width, int height) {
        return calculateShape(width,height);
    }
}

