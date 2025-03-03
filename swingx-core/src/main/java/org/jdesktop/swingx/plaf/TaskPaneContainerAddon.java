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
package org.jdesktop.swingx.plaf;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.plaf.windows.WindowsClassicLookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;
import org.jdesktop.swingx.util.OS;

/**
 * Addon for <code>JXTaskPaneContainer</code>. This addon defines the following properties:
 * <ul>
 * <li>TaskPaneContainer.background - background color
 * <li>TaskPaneContainer.backgroundPainter - background painter
 * <li>TaskPaneContainer.border - container border
 * <li>TaskPaneContainer.font - font (currently unused)
 * <li>TaskPaneContainer.foreground - foreground color (currently unused)
 * </ul>
 *  
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 * @author Karl Schaefer
 */
public class TaskPaneContainerAddon extends AbstractComponentAddon {

	static private final String  KEY_BACKGROUND = "TaskPaneContainer.background";
	static private final Color COLOR_BACKGROUND = Color.decode("#005C5C"); // ~ dark olive green / deep cyan
	
	public TaskPaneContainerAddon() {
		super("JXTaskPaneContainer");
	}

	@Override
	protected void addBasicDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		super.addBasicDefaults(addon, defaults);

		defaults.add(JXTaskPaneContainer.uiClassID, "org.jdesktop.swingx.plaf.basic.BasicTaskPaneContainerUI");
		defaults.add(KEY_BACKGROUND,
				UIManagerExt.getSafeColor("Desktop.background", new ColorUIResource(COLOR_BACKGROUND)));
		defaults.add("TaskPaneContainer.border", new BorderUIResource(BorderFactory.createEmptyBorder(10, 10, 0, 10)));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addMetalDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		super.addMetalDefaults(addon, defaults);
		defaults.add(KEY_BACKGROUND, MetalLookAndFeel.getDesktopColor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addWindowsDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		super.addWindowsDefaults(addon, defaults);
		if (addon instanceof WindowsClassicLookAndFeelAddons) {
			defaults.add(KEY_BACKGROUND,
					UIManagerExt.getSafeColor("List.background", new ColorUIResource(COLOR_BACKGROUND)));
		} else if (addon instanceof WindowsLookAndFeelAddons) {
			String xpStyle = OS.getWindowsVisualStyle();
			ColorUIResource background;
			Color backgroundGradientStart;
			Color backgroundGradientEnd;

			if (WindowsLookAndFeelAddons.HOMESTEAD_VISUAL_STYLE.equalsIgnoreCase(xpStyle)) {
				background = new ColorUIResource(201, 215, 170);
				backgroundGradientStart = new Color(204, 217, 173);
				backgroundGradientEnd = new Color(165, 189, 132);
			} else if (WindowsLookAndFeelAddons.SILVER_VISUAL_STYLE.equalsIgnoreCase(xpStyle)) {
				background = new ColorUIResource(192, 195, 209);
				backgroundGradientStart = new Color(196, 200, 212);
				backgroundGradientEnd = new Color(177, 179, 200);
			} else {
				if (OS.isWindowsVista()) {
					final Toolkit toolkit = Toolkit.getDefaultToolkit();
					background = new ColorUIResource((Color) toolkit.getDesktopProperty("win.3d.backgroundColor"));
					backgroundGradientStart = (Color) toolkit.getDesktopProperty("win.frame.activeCaptionColor");
					backgroundGradientEnd = (Color) toolkit.getDesktopProperty("win.frame.inactiveCaptionColor");
				} else {
					background = new ColorUIResource(117, 150, 227);
					backgroundGradientStart = new ColorUIResource(123, 162, 231);
					backgroundGradientEnd = new ColorUIResource(99, 117, 214);
				}
			}

			defaults.add("TaskPaneContainer.backgroundPainter",
					new PainterUIResource<JXTaskPaneContainer>(new MattePainter(
							new GradientPaint(0f, 0f, backgroundGradientStart, 0f, 1f, backgroundGradientEnd), true)));
			defaults.add(KEY_BACKGROUND, background);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void addMacDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		super.addMacDefaults(addon, defaults);
		defaults.add(KEY_BACKGROUND, new ColorUIResource(238, 238, 238)); // white
	}

	@Override
	protected void addNimbusDefaults(LookAndFeelAddons addon, DefaultsList defaults) {
		super.addNimbusDefaults(addon, defaults);
		// dynamically changing the LaF to Nimbus does not refresh correctly the
		// control colors if they are not hard-coded due to Nimbus DerivedColors
		// lazy initialization
//        defaults.add(KEY_BACKGROUND, new ColorUIResource(UIManager.getColor("control")));
		defaults.add(KEY_BACKGROUND, new ColorUIResource(214, 217, 223));
	}

}
