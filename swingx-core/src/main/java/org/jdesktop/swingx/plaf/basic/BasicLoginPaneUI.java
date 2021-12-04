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
package org.jdesktop.swingx.plaf.basic;

import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.plaf.LoginPaneUI;
import org.jdesktop.swingx.plaf.UIManagerExt;
import org.jdesktop.swingx.util.GraphicsUtilities;

/**
 * Base implementation of the <code>JXLoginPane</code> UI.
 *
 * @author rbair
 */
public class BasicLoginPaneUI extends LoginPaneUI {
    
    private JXLoginPane dlg;
    
    /** Creates a new instance of BasicLoginDialogUI */
    public BasicLoginPaneUI(JXLoginPane dlg) {
        this.dlg = dlg;
    }
    
	public static ComponentUI createUI(JComponent c) {
		return new BasicLoginPaneUI((JXLoginPane) c);
	}

    /**
     * @inheritDoc
     */
    @Override
    public void installUI(JComponent c) {
        installDefaults();
    }
    
    protected void installDefaults() {
        String s = dlg.getBannerText();
        if (s == null || s.equals("")) {
            dlg.setBannerText(UIManagerExt.getString("JXLoginPane.bannerString", dlg.getLocale()));
        }
        
        s = dlg.getErrorMessage();
        if (s == null || s.equals("")) {
            dlg.setErrorMessage(UIManagerExt.getString("JXLoginPane.errorMessage", dlg.getLocale()));
        }
    }
    
    /**
     * Creates default 400x60 Image banner for the login panel.
     * @see org.jdesktop.swingx.plaf.LoginPaneUI#getBanner()
     */
    @Override
    public Image getBanner() {
    	return getXBanner(dlg.getBannerText());
    }
    private Image eBanner = null;
    @Override
    public Image getEBanner() {
    	if(eBanner==null) eBanner = getXBanner(null);
    	return eBanner;
    }
    public Image getXBanner(String bannerText) {
        int w = 400;
        int h = 60;
        float loginStringX = w * .05f;
        float loginStringY = h * .75f;

        BufferedImage img = GraphicsUtilities.createCompatibleImage(w, h);
        Graphics2D g2 = img.createGraphics();
//        String bannerText = dlg.getBannerText();
        try {
            Font font = UIManager.getFont("JXLoginPane.bannerFont");
            g2.setFont(font);
            Graphics2D originalGraphics = g2;

            try {
                if (!dlg.getComponentOrientation().isLeftToRight()) {
                    originalGraphics = (Graphics2D) g2.create();
                    g2.scale(-1, 1);
                    g2.translate(-w, 0);
                    loginStringX = w
                    	- (((float) font.getStringBounds(bannerText, originalGraphics.getFontRenderContext())
                                    .getWidth()) + w * .05f);
                }

                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

                // draw a big square
                g2.setColor(UIManager.getColor("JXLoginPane.bannerDarkBackground"));
                g2.fillRect(0, 0, w, h);

                // create the curve shape
                GeneralPath curveShape = new GeneralPath(GeneralPath.WIND_NON_ZERO);
                curveShape.moveTo(0, h * .6f);
                // drawing a Bézier curve:
                curveShape.curveTo(w * .167f, h * 1.2f, w * .667f, h * -.5f, w, h * .75f);
                curveShape.lineTo(w, h);
                curveShape.lineTo(0, h);
                curveShape.lineTo(0, h * .8f);
                curveShape.closePath();

                // draw into the buffer a gradient (bottom to top), and the text "Login"
                GradientPaint gp = new GradientPaint(0, h, UIManager.getColor("JXLoginPane.bannerDarkBackground")
                		, 0, 0, UIManager.getColor("JXLoginPane.bannerLightBackground"));
                g2.setPaint(gp);
                g2.fill(curveShape);

                originalGraphics.setColor(UIManager.getColor("JXLoginPane.bannerForeground"));
                if(bannerText!=null) {
                    originalGraphics.drawString(bannerText, loginStringX, loginStringY);
                }
            } finally {
                originalGraphics.dispose();
            }
        } finally {
            g2.dispose();
        }
        return img;
    }
}