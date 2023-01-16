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
package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import org.jdesktop.swingx.JXMonthView;
import org.jdesktop.swingx.icon.ChevronIcon;
import org.jdesktop.swingx.icon.RadianceIcon;
import org.jdesktop.swingx.icon.RadianceIconUIResource;

public class MonthViewAddon extends AbstractComponentAddon {
	
    static RadianceIconUIResource getIcon(int size, int rotation) {
    	RadianceIcon icon = ChevronIcon.of(size, size);
    	icon.setRotation(rotation);
    	return new RadianceIconUIResource(icon);
    }
    
	Icon monthDown;
	Icon monthUp;
	
    public MonthViewAddon() {
        super("JXMonthView");
        monthDown = getIcon(RadianceIcon.SMALL_ICON, RadianceIcon.WEST);
        monthUp = getIcon(RadianceIcon.SMALL_ICON, RadianceIcon.EAST); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
        super.addBasicDefaults(addon, defaults);
        
        defaults.add(JXMonthView.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicMonthViewUI");
        
        defaults.add("JXMonthView.background", new ColorUIResource(Color.WHITE));
        defaults.add("JXMonthView.monthStringBackground", new ColorUIResource(138, 173, 209)); // hellgrau blau
        defaults.add("JXMonthView.monthStringForeground", new ColorUIResource(68, 68, 68)); // ~ dark gray
        defaults.add("JXMonthView.daysOfTheWeekForeground", new ColorUIResource(68, 68, 68));
        defaults.add("JXMonthView.weekOfTheYearForeground", new ColorUIResource(68, 68, 68));
        defaults.add("JXMonthView.unselectableDayForeground", new ColorUIResource(Color.RED));
        defaults.add("JXMonthView.selectedBackground", new ColorUIResource(197, 220, 240)); // blass blau ~ light blue
        defaults.add("JXMonthView.flaggedDayForeground", new ColorUIResource(Color.RED));
        defaults.add("JXMonthView.leadingDayForeground", new ColorUIResource(Color.LIGHT_GRAY));
        defaults.add("JXMonthView.trailingDayForeground", new ColorUIResource(Color.LIGHT_GRAY));
        defaults.add("JXMonthView.font", UIManagerExt.getSafeFont("Button.font", new FontUIResource("Dialog", Font.PLAIN, 12)));
        // EUG: issue 37 - remove the png:
//        defaults.add("JXMonthView.monthDownFileName", LookAndFeel.makeIcon(MonthViewAddon.class, "basic/resources/month-down.png"));
//        defaults.add("JXMonthView.monthUpFileName", LookAndFeel.makeIcon(MonthViewAddon.class, "basic/resources/month-up.png"));
        defaults.add("JXMonthView.monthDown", monthDown);
        defaults.add("JXMonthView.monthUp", monthUp);

        defaults.add("JXMonthView.boxPaddingX", 3);
        defaults.add("JXMonthView.boxPaddingY", 3);
    }
}
