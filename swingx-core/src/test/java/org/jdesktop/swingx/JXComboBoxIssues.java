/*
 * Copyright 2010 Sun Microsystems, Inc., 4150 Network Circle,
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

import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PainterHighlighter;
import org.jdesktop.swingx.painter.ImagePainter;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.test.XTestUtils;
import org.junit.Test;

/**
 *
 * @author kschaefer
 */
public class JXComboBoxIssues extends InteractiveTestCase {
    
    private static final Logger LOG = Logger.getLogger(JXComboBoxIssues.class.getName());
    
    private ComboBoxModel<Object> model;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() {
        model = createComboBoxModel();
    }
    
    private DefaultComboBoxModel<Object> createComboBoxModel() {
        return new DefaultComboBoxModel<Object>(new JComboBox<Object>().getActionMap().allKeys());
    }
    

    public static void main(String[] args) {
        JXComboBoxIssues test = new JXComboBoxIssues();
        
        try {
          test.runInteractiveTests();
        } catch (Exception e) {
            System.err.println("exception when executing interactive tests:");
            e.printStackTrace();
        }
    }


    public void interactiveHighlightSelectedItemNotInList() {
        final JXComboBox<Object> combo = new JXComboBox<Object>(createComboBoxModel());
        combo.getModel().setSelectedItem("not-in-list");
        PainterHighlighter hl = new PainterHighlighter(new ImagePainter(XTestUtils.loadDefaultImage()));
        combo.addHighlighter(hl);
        JComponent panel = new JXPanel();
        panel.add(new JButton("something to focus"));
        panel.add(combo);
        panel.add(new JComboBox<Object>(combo.getModel()));
        showInFrame(panel, "Painter");
    }
    
    public void interactiveComboBoxHighlighterNotEditable() {
        final JXComboBox<Object> combo = new JXComboBox<Object>(createComboBoxModel());
        combo.addHighlighter(HighlighterFactory.createSimpleStriping(HighlighterFactory.LINE_PRINTER));
        
        JComponent panel = new JXPanel();
        panel.add(new JButton("something to focus"));
        panel.add(combo);
        panel.add(new JComboBox<Object>(combo.getModel()));
        JXFrame frame = wrapInFrame(panel, "Highlighter - not editable");
        Action action = new AbstractAction("toggle useHighlighterOnCurrent") {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                combo.setUseHighlightersForCurrentValue(!combo.isUsingHighlightersForCurrentValue());
            }
        };
        addAction(frame, action);
        addStatusMessage(frame, "incorrect not-editable xcombo appearance");
        show(frame);
    }
    
    public void interactiveComboBoxHighlighterEditable() {
        JXComboBox<Object> combo = new JXComboBox<Object>(createComboBoxModel());
        combo.setEditable(true);
        combo.addHighlighter(HighlighterFactory.createSimpleStriping(HighlighterFactory.LINE_PRINTER));

        JComponent panel = new JXPanel();
        panel.add(new JButton("something to focus"));
        panel.add(combo);
        JComboBox<Object> plain = new JComboBox<Object>(createComboBoxModel());
        plain.setEditable(true);
        panel.add(plain);
        JXFrame frame = showInFrame(panel, "Highlighter - editable");
        addStatusMessage(frame, "editable xcombo appearance looks okay in Win/Nimbus");
    }

// https://github.com/homebeaver/SwingSet/issues/69 
    // intentionally unsorted
	private static final String[] petStrings = { "Tyrannosaurus Rex", "Dog", "Bird", "Cat", "Rabbit", "Pig" };
    // a controbutor stub
	static class Contributor {
		public String getLastName() {
			return "LastName";
		}
		public String getFirstName() {
			return "FirstName";
		}
		public String getMerits() {
			return "Merits"+this.hashCode();
		}
		public String toString() {
			return getLastName() + ", " + getFirstName() + " (" + getMerits() + ")";
		}
	}
	private static String preferredStringRepresentation(Object o) {
		if (o == null)
			return "";
		// to be used in JXComboBox<Object> xcb
		if (o instanceof Contributor c) {
			return c.getLastName() + ", " + c.getFirstName() + " (" + c.getMerits() + ")";
		}
		// to be used in controller JXComboBox<DisplayInfo<Icon>> iconChooserCombo
		return o.toString();
	}

	/**
	 * test a sorted combo box with items of different type, define a comparator
	 * https://github.com/homebeaver/SwingSet/issues/69 :
	 * version 1.7.9 : BUG when adding items in sorted ComboBoxModel throws ArrayIndexOutOfBoundsException
	 */
	@Test
	public void interactiveSortedComboBoxWithAddedElements() {
		JXComboBox<Object> xcb;
		// Create a sorted combo box with Object items; define a comparator
		xcb = new JXComboBox<>(petStrings, true);
		xcb.setComparator(new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				String s1 = preferredStringRepresentation(o1);
				String s2 = preferredStringRepresentation(o2);
				//System.out.println("Comparator compare " + s1 + " with " + s2);
				return s1.compareToIgnoreCase(s2);
			}
		});
        // add some contributors
        ComboBoxModel<Contributor> m = //Contributors.getContributorModel();
        		new DefaultComboBoxModel<Contributor>();
        ((DefaultComboBoxModel<Contributor>)m).addElement(new Contributor());
        ((DefaultComboBoxModel<Contributor>)m).addElement(new Contributor());
        ((DefaultComboBoxModel<Contributor>)m).addElement(new Contributor());
        System.out.println("Contributor item count = "+m.getSize());
        assertEquals(3, m.getSize());
        assertEquals(petStrings.length, xcb.getModel().getSize());
        for(int c=0; c<3; c++) {
        	Contributor contributor = m.getElementAt(c);
        	System.out.println("TEST : add Contributor " + contributor + " model.Class="+xcb.getModel().getClass());
        	xcb.addItem(contributor);
        }
        assertEquals(petStrings.length+3, xcb.getModel().getSize());
        xcb.removeItem(m.getElementAt(0));

        // determine selected item (-1 indicates no selection, 0 is the default)
        if(xcb.getModel().getSize()>0) {
        	xcb.setSelectedItem(petStrings[1]);
        }

//        xcb.setToolTipText(getBundleString("cb.toolTipText", toolTipText));
//        xcb.addActionListener(ae -> {
//            Object o = xcb.getSelectedItem();
//            if(o instanceof String petName) {
//                updateLabel(rightpic, petName);
//            } else {
//                updateLabel(rightpic, o.toString());
//            }
//        });
        // setRenderer(ListCellRenderer<? super E> renderer)
        StringValue sv = (Object value) -> {
        	return preferredStringRepresentation(value);
        };
//		IconValue iv = (Object value) -> {
//			if (value instanceof Contributor c) {
//				return flagIcons[(c.getMerits()) % flagIcons.length];
//			}
//			return IconValue.NULL_ICON;
//		};
//        xcb.setRenderer(new DefaultListRenderer<Object>(sv, iv));
        xcb.setRenderer(new DefaultListRenderer<Object>(sv));
//        right.add(xcb, BorderLayout.CENTER);
//        
//        //Set up the item picture.
//        rightpic = new JLabel();
//        rightpic.setFont(rightpic.getFont().deriveFont(Font.ITALIC));
//        rightpic.setHorizontalAlignment(JLabel.CENTER);
//        int si = xcb.getSelectedIndex();
//        if (si<0 || si>=petStrings.length) {
//        	updateLabel(rightpic, "unbekannt");
//        } else {
//        	updateLabel(rightpic, petStrings[si]);
//        }
//        rightpic.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
//        right.add(rightpic, BorderLayout.PAGE_END);
//        
//        right.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
//        return right;

        JComponent panel = new JXPanel();
        panel.add(xcb);
        showInFrame(panel, "SortedComboBoxWithAddedElements");
	}
    
    public void testDummy() {
    	interactiveSortedComboBoxWithAddedElements();
    }
}
