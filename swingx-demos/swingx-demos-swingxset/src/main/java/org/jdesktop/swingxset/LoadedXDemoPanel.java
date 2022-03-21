/*
 * Copyright 2009 Sun Microsystems, Inc., 4150 Network Circle,
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
 *
 */
package org.jdesktop.swingxset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.Painter;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.util.PaintUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.swingset3.Demo;
import com.sun.swingset3.utilities.HTMLPanel;
import com.sun.swingset3.utilities.RoundedBorder;
import com.sun.swingset3.utilities.Utilities;

@SuppressWarnings("serial")
public class LoadedXDemoPanel extends JXTitledPanel {
	
    private static final Logger LOG = Logger.getLogger(LoadedXDemoPanel.class.getName());

    private JComponent descriptionArea;

    private JComponent demoPanel;

    public LoadedXDemoPanel(Demo demo) throws Exception {
        super(demo.getName());
        initComponents(demo);
        layoutComponents();
        setBorder(new RoundedBorder(5));//DemoXPanel.roundedBorder);
        if (descriptionArea != null) {
            demoPanel.setBorder(DemoXPanel.roundedBorder);
        }
        applyDefaults();
        LOG.config("descriptionArea:"+descriptionArea + " demoPanel:"+demoPanel);
    }

    private void layoutComponents() {
        if (descriptionArea == null) {
            setContentContainer(demoPanel);
        } else {
            layoutWithDescription();
        }
    }

    /**
     * 
     */
    private void layoutWithDescription() {
        JComponent top = (JComponent) getUI().getTitleBar();
        removeAll();
        FormLayout formLayout = new FormLayout("f:[100dlu, m]:g, f:d:g", // columns
                "f:d:n, f:d:g "); // rows
        PanelBuilder builder = new PanelBuilder(formLayout, this);
        CellConstraints cc = new CellConstraints();
        JScrollPane scrollPane = new JScrollPane(descriptionArea,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        builder.add(scrollPane, cc.xywh(1, 2, 1, 1));
        builder.add(demoPanel, cc.xywh(2, 2, 1, 1));
        builder.add(top, cc.xywh(1, 1, 2, 1));
    }

    /**
     * @throws Exception
     * 
     */
    private void initComponents(Demo demo) throws Exception {
        URL description = demo.getHTMLDescription();
        LOG.info("URL description:"+description);
        if(description == null) {
        	LOG.warning("no HTML Description for "+demo);
        } else {
            descriptionArea = createDescriptionArea(description);
        }
        demoPanel = new JXPanel(new BorderLayout());
        demoPanel.add(demo.createDemoComponent());
    }

    private JComponent createDescriptionArea(URL descriptionURL) {
    	
        final JEditorPane editor =  new HTMLPanel(); // HTMLPanel extends JXEditorPane
        editor.setEditable(false);
        editor.setMargin(DemoXPanel.margin);
        editor.setOpaque(true);
        
        try {
            editor.setPage(descriptionURL);
        } catch (IOException e) {
            System.err.println("couldn't load description from URL:" + descriptionURL);
        }
        return editor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doLayout() {
        super.doLayout();
        if (descriptionArea != null) {
            // manual tweaking of demo panel: displace slightly to overlap the title
            // PENDING JW: displace so that it is below the title baseline
            Rectangle bounds = demoPanel.getBounds();
            int delta = getUI().getTitleBar().getHeight() / 2;
            bounds.y -= delta;
            bounds.height += delta / 2;
            demoPanel.setBounds(bounds);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUI() {
        super.updateUI();
        if (descriptionArea != null) {
            layoutWithDescription();
        }
        applyDefaults();
    }

    private void applyDefaults() {
        Color color = UIManager.getColor(SwingXSet.TITLE_GRADIENT_COLOR2_KEY);
        GradientPaint gradient = new GradientPaint(0f, 0f, UIManager.getColor(SwingXSet.TITLE_GRADIENT_COLOR1_KEY), 
        		500f, 0f,
                PaintUtils.setAlpha(color, 0));
        Painter<Component> matte = new MattePainter(gradient, true);
        setTitlePainter(matte); 

        setTitleFont(UIManager.getFont(SwingXSet.TITLE_FONT_KEY));
        Color bg = Utilities.deriveColorHSB(UIManager.getColor("Panel.background"), 0, 0, -.06f);
        setBackground(bg);
        setForeground(UIManager.getColor(SwingXSet.TITLE_FOREGROUND_KEY));
        if (demoPanel != null) {
            demoPanel.setBackground(Utilities.deriveColorHSB(bg, 0, 0, .04f));
        }
        if (descriptionArea != null) {
            descriptionArea.setBackground(bg);
        }
    }
}