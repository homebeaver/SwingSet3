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
package org.jdesktop.swingx.graphicstests;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.InteractiveTestCase;
import org.jdesktop.swingx.JXImageView;
import org.jdesktop.swingx.icon.ArrowIcon;
import org.jdesktop.swingx.icon.ChevronsIcon;
import org.jdesktop.swingx.icon.CircleIcon;
import org.jdesktop.swingx.icon.PlayIconSolid;
import org.jdesktop.swingx.icon.RadianceIcon;
import org.jdesktop.swingx.icon.SizingConstants;
import org.jdesktop.swingx.icon.TrafficLightGreenIcon;
import org.jdesktop.swingx.icon.TrafficLightRedIcon;
import org.jdesktop.swingx.icon.TrafficLightYellowIcon;
import org.jdesktop.swingx.util.GraphicsUtilities;

/**
 * GraphicsUtilities visual checks.
 * 
 * @author rah003
 */
public class GraphicsUtilitiesVisualCheck extends InteractiveTestCase {

    public static void main(String[] args) {
        GraphicsUtilitiesVisualCheck test = new GraphicsUtilitiesVisualCheck();
        try {
            test.runInteractiveTests();
        } catch (Exception e) {
            System.err.println("exception when executing interactive tests:");
            e.printStackTrace();
        }
    }

    /**
     * show CircleIcon different sizes and colors
     */
    public void interactiveCircleIcon() throws Exception {
    	RadianceIcon greenS = TrafficLightGreenIcon.of(SizingConstants.SMALL_ICON, SizingConstants.SMALL_ICON);
        JButton sButton = new JButton("green S", greenS);
        
    	RadianceIcon yellowN = TrafficLightYellowIcon.of(SizingConstants.N, SizingConstants.N);
        JButton nButton = new JButton("yellow N", yellowN);
        
    	RadianceIcon redXXL = TrafficLightRedIcon.of(SizingConstants.XXL, SizingConstants.XXL);
    	redXXL.setRotation(SwingConstants.NORTH_WEST); // das Licht fällt von NE
        JButton redXXLButton = new JButton("red XXL", redXXL);
        
    	RadianceIcon circle = CircleIcon.of(SizingConstants.ACTION_ICON, SizingConstants.ACTION_ICON);
        JButton mButton = new JButton("circle action (M) size", circle);
        
    	JPanel panel = new JPanel(new BorderLayout());
    	panel.add(sButton, BorderLayout.WEST);
    	panel.add(redXXLButton, BorderLayout.EAST);
    	panel.add(mButton, BorderLayout.CENTER);
    	panel.add(nButton, BorderLayout.SOUTH);
        showInFrame(panel, "interactiveCircleIcon");
    }
    /**
     * issue https://github.com/homebeaver/SwingSet/issues/22
     * show chevrons with all compass directions 
     * and different sizes and colors
     */
    public void interactiveChevronsIcon() throws Exception {
    	RadianceIcon chevrons = ChevronsIcon.of(SizingConstants.SMALL_ICON, SizingConstants.SMALL_ICON);
    	chevrons.setRotation(SwingConstants.SOUTH);
        JButton chevronsButton = new JButton("south S", chevrons);
        
    	RadianceIcon northChevrons = ChevronsIcon.of(SizingConstants.ACTION_ICON, SizingConstants.ACTION_ICON);
    	northChevrons.setRotation(SwingConstants.NORTH);
        JButton northButton = new JButton("north M", northChevrons); // SizingConstants.M == ACTION_ICON
        
    	RadianceIcon westChevrons = ChevronsIcon.of(SizingConstants.XXL, SizingConstants.XXL);
    	westChevrons.setRotation(SwingConstants.WEST);
    	westChevrons.setColorFilter(color -> Color.BLUE);
        JButton westButton = new JButton("west XXL", westChevrons);
        
    	RadianceIcon eastChevrons = ChevronsIcon.of(SizingConstants.XXL, SizingConstants.XXL);
    	eastChevrons.setRotation(SwingConstants.EAST);
    	eastChevrons.setColorFilter(color -> Color.RED);
        JButton eastButton = new JButton("east XXL", eastChevrons);
        
    	RadianceIcon northwestChevrons = ChevronsIcon.of(SizingConstants.XL, SizingConstants.XL);
    	northwestChevrons.setRotation(SwingConstants.NORTH_WEST);
    	northwestChevrons.setColorFilter(color -> Color.BLUE);
        JButton northwestButton = new JButton("north west XL", northwestChevrons);
        
    	RadianceIcon northeastChevrons = ChevronsIcon.of(SizingConstants.XL, SizingConstants.XL);
    	northeastChevrons.setRotation(SwingConstants.NORTH_EAST);
    	northeastChevrons.setColorFilter(color -> Color.RED);
        JButton northeastButton = new JButton("north east XL", northeastChevrons);
        
    	RadianceIcon southwestChevrons = ChevronsIcon.of(SizingConstants.L, SizingConstants.L);
    	southwestChevrons.setRotation(SwingConstants.SOUTH_WEST);
    	southwestChevrons.setColorFilter(color -> Color.BLUE);
        JButton southwestButton = new JButton("south west L", southwestChevrons);
        
    	RadianceIcon southeastChevrons = ChevronsIcon.of(SizingConstants.L, SizingConstants.L);
    	southeastChevrons.setRotation(SwingConstants.SOUTH_EAST);
    	southeastChevrons.setColorFilter(color -> Color.RED);
        JButton southeastButton = new JButton("south east L", southeastChevrons);
        
    	JPanel panel = new JPanel(new BorderLayout());
    	JPanel northpanel = new JPanel(new BorderLayout());
    	JPanel southpanel = new JPanel(new BorderLayout());
    	panel.add(new JLabel(" chevrons, blue in west, red in east "), BorderLayout.CENTER);
    	northpanel.add(northButton, BorderLayout.CENTER);
    	northpanel.add(northwestButton, BorderLayout.WEST);
    	northpanel.add(northeastButton, BorderLayout.EAST);
    	panel.add(northpanel, BorderLayout.NORTH);
    	
    	southpanel.add(chevronsButton, BorderLayout.CENTER);
    	southpanel.add(southwestButton, BorderLayout.WEST);
    	southpanel.add(southeastButton, BorderLayout.EAST);
    	panel.add(southpanel, BorderLayout.SOUTH);
    	
    	panel.add(westButton, BorderLayout.WEST);
    	panel.add(eastButton, BorderLayout.EAST);
        showInFrame(panel, "interactiveChevronsIcon");
    }
    /**
     * show ArrowIcon with all compass directions 
     * and different sizes and colors
     */
    public void interactiveArrowIcon() throws Exception {
    	RadianceIcon south = ArrowIcon.of(RadianceIcon.SMALL_ICON, RadianceIcon.SMALL_ICON);
    	south.setRotation(RadianceIcon.SOUTH);
    	JButton southButton = new JButton("south S", south);
        
    	RadianceIcon north = ArrowIcon.of(RadianceIcon.ACTION_ICON, RadianceIcon.ACTION_ICON);
    	north.setRotation(RadianceIcon.NORTH);
    	JButton northButton = new JButton("north M", north);
        
    	RadianceIcon west = ArrowIcon.of(RadianceIcon.XXL, RadianceIcon.XXL);
    	west.setRotation(SwingConstants.WEST);
    	west.setColorFilter(color -> Color.BLUE);
        JButton westButton = new JButton("west XXL", west);
        
    	RadianceIcon east = ArrowIcon.of(RadianceIcon.XXL, RadianceIcon.XXL);
    	east.setRotation(RadianceIcon.EAST);
    	east.setColorFilter(color -> Color.RED);
        JButton eastButton = new JButton("east XXL", east);
        
    	RadianceIcon northwest = ArrowIcon.of(RadianceIcon.XL, RadianceIcon.XL);
    	northwest.setRotation(RadianceIcon.NORTH_WEST);
    	northwest.setColorFilter(color -> Color.BLUE);
        JButton northwestButton = new JButton("north west XL", northwest);
        
    	RadianceIcon northeast = ArrowIcon.of(RadianceIcon.XL, RadianceIcon.XL);
    	northeast.setRotation(RadianceIcon.NORTH_EAST);
    	northeast.setColorFilter(color -> Color.RED);
        JButton northeastButton = new JButton("north east XL", northeast);
        
    	RadianceIcon southwest = ArrowIcon.of(RadianceIcon.L, RadianceIcon.L);
    	southwest.setRotation(RadianceIcon.SOUTH_WEST);
    	southwest.setColorFilter(color -> Color.BLUE);
        JButton southwestButton = new JButton("south west L", southwest);
        
    	RadianceIcon southeast = ArrowIcon.of(RadianceIcon.L, RadianceIcon.L);
    	southeast.setRotation(RadianceIcon.SOUTH_EAST);
    	southeast.setColorFilter(color -> Color.RED);
        JButton southeastButton = new JButton("south east L", southeast);
        
    	JPanel panel = new JPanel(new BorderLayout());
    	JPanel northpanel = new JPanel(new BorderLayout());
    	JPanel southpanel = new JPanel(new BorderLayout());
    	panel.add(new JLabel(" arrows, blue in west, red in east "), BorderLayout.CENTER);
    	northpanel.add(northButton, BorderLayout.CENTER);
    	northpanel.add(northwestButton, BorderLayout.WEST);
    	northpanel.add(northeastButton, BorderLayout.EAST);
    	panel.add(northpanel, BorderLayout.NORTH);
    	
    	southpanel.add(southButton, BorderLayout.CENTER);
    	southpanel.add(southwestButton, BorderLayout.WEST);
    	southpanel.add(southeastButton, BorderLayout.EAST);
    	panel.add(southpanel, BorderLayout.SOUTH);
    	
    	panel.add(westButton, BorderLayout.WEST);
    	panel.add(eastButton, BorderLayout.EAST);
        showInFrame(panel, "interactiveArrowIcon");
    }
    
    /**
     * show solid PlayIcon with different sizes and colors
     * diff between method of and uiResourceOf: 
     * see https://github.com/kirill-grouchnikov/radiance/issues/417
     */
    public void interactivePlayIcon() throws Exception {
    	RadianceIcon south = PlayIconSolid.uiResourceOf(RadianceIcon.SMALL_ICON, RadianceIcon.SMALL_ICON);
    	south.setRotation(Math.toRadians(90d));
        JButton southButton = new JButton("south S", south);
        
    	RadianceIcon north = PlayIconSolid.uiResourceOf(RadianceIcon.ACTION_ICON, RadianceIcon.ACTION_ICON);
    	north.setRotation(Math.toRadians(270d));
        JButton northButton = new JButton("north M", north); // SizingConstants.M
        
    	RadianceIcon west = PlayIconSolid.uiResourceOf(RadianceIcon.XXL, RadianceIcon.XXL);
    	west.setRotation(Math.toRadians(180d)); // rotate 180°
    	west.setColorFilter(color -> Color.BLUE);
        JButton westButton = new JButton("west XXL", west);
        
    	RadianceIcon east = PlayIconSolid.uiResourceOf(RadianceIcon.XXL, RadianceIcon.XXL);
//    	east.setRotation(RadianceIcon.NORTH); // no rotation
    	east.setColorFilter(color -> Color.RED);
        JButton eastButton = new JButton("east XXL", east);
        
    	RadianceIcon northwest = PlayIconSolid.uiResourceOf(RadianceIcon.XL, RadianceIcon.XL);
    	northwest.setRotation(Math.toRadians(225d));
    	northwest.setColorFilter(color -> Color.BLUE);
        JButton northwestButton = new JButton("north west XL", northwest);
        
    	RadianceIcon northeast = PlayIconSolid.uiResourceOf(RadianceIcon.XL, RadianceIcon.XL);
    	northeast.setRotation(Math.toRadians(315d));
    	northeast.setColorFilter(color -> Color.RED);
        JButton northeastButton = new JButton("north east XL", northeast);
        
    	RadianceIcon southwest = PlayIconSolid.uiResourceOf(RadianceIcon.L, RadianceIcon.L);
    	southwest.setRotation(Math.toRadians(135d));
    	southwest.setColorFilter(color -> Color.BLUE);
        JButton southwestButton = new JButton("south west L", southwest);
        
    	RadianceIcon southeast = PlayIconSolid.uiResourceOf(RadianceIcon.L, RadianceIcon.L);
    	southeast.setRotation(Math.toRadians(45d));
    	southeast.setColorFilter(color -> Color.RED);
        JButton southeastButton = new JButton("south east L", southeast);
        
    	JPanel panel = new JPanel(new BorderLayout());
    	JPanel northpanel = new JPanel(new BorderLayout());
    	JPanel southpanel = new JPanel(new BorderLayout());
    	panel.add(new JLabel(" PlayIcon, blue in west, red in east "), BorderLayout.CENTER);
    	northpanel.add(northButton, BorderLayout.CENTER);
    	northpanel.add(northwestButton, BorderLayout.WEST);
    	northpanel.add(northeastButton, BorderLayout.EAST);
    	panel.add(northpanel, BorderLayout.NORTH);
    	
    	southpanel.add(southButton, BorderLayout.CENTER);
    	southpanel.add(southwestButton, BorderLayout.WEST);
    	southpanel.add(southeastButton, BorderLayout.EAST);
    	panel.add(southpanel, BorderLayout.SOUTH);
    	
    	panel.add(westButton, BorderLayout.WEST);
    	panel.add(eastButton, BorderLayout.EAST);
        showInFrame(panel, "interactivePlayIcon");

    }
    
    /**
     * Issue #524-swingx: Thumbnails not generated correctly.
     * 
     */
    public void interactiveMistargetedKeyStrokes() throws Exception {
        BufferedImage im = ImageIO.read(getClass().getResourceAsStream(
                "/org/jdesktop/swingx/resources/images/500by500.png"));
        System.out.println("size:" + im.getWidth() + ", " + im.getHeight());
        BufferedImage im2 = GraphicsUtilities.createThumbnail(im, 100);
        JXImageView ipa = new JXImageView();
        ipa.setImage(im2);
        showInFrame(ipa, "interactiveMistargetedKeyStrokes");
    }

    /**
     * do nothing test - keep the test runner happy.
     */
    public void testDummy() {
    }

}
