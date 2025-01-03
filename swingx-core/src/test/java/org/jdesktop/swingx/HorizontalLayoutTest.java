/**
 * 
 */
package org.jdesktop.swingx;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.jdesktop.test.EDTRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Karl George Schaefer
 */
@RunWith(EDTRunner.class)
public class HorizontalLayoutTest {
    /**
     * SwingX Issue #666: incorrect calculation of preferred size.
     */
    @Test
    public void testLayout() {
        JPanel panel = new JPanel(new HorizontalLayout());
        
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Color.RED);
        p.setPreferredSize(new Dimension(50, 25));
        panel.add(p);
        
        p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(125, 15));
        panel.add(p);
        
        p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Color.BLUE);
        p.setPreferredSize(new Dimension(30, 30));
        panel.add(p);
        
        assertThat(new Dimension(205, 30), is(panel.getPreferredSize()));
    }
    /**
     * SwingX Issue #1443: incorrect calculation of preferred size with gaps.
     */
    @Test
    public void testLayoutWithGaps() {
        JPanel panel = new JPanel(new HorizontalLayout(5));
        
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Color.RED);
        p.setPreferredSize(new Dimension(50, 25));
        panel.add(p);
        
        p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(125, 15));
        panel.add(p);
        
        p = new JPanel();
        p.setOpaque(true);
        p.setBackground(Color.BLUE);
        p.setPreferredSize(new Dimension(30, 30));
        panel.add(p);
        
        assertThat(new Dimension(215, 30), is(panel.getPreferredSize()));
    }
}
