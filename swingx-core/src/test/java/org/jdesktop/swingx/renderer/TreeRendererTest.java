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
 *
 */
package org.jdesktop.swingx.renderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

import org.jdesktop.swingx.InteractiveTestCase;
import org.jdesktop.swingx.JXEditorPaneTest;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.hyperlink.LinkModel;
import org.jdesktop.swingx.plaf.UIAction;
import org.jdesktop.swingx.test.ActionMapTreeTableModel;
import org.jdesktop.swingx.test.ComponentTreeTableModel;
import org.jdesktop.swingx.treetable.FileSystemModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests behaviour of SwingX <code>DefaultTreeRenderer</code>. 
 * Contains characterization to
 * guarantee that it behaves similar to the standard. 
 * 
 * @author Jeanette Winzenburg
 */
@RunWith(JUnit4.class)
public class TreeRendererTest extends InteractiveTestCase {

	private static final Logger LOG = Logger.getLogger(TreeRendererTest.class .getName());
    
    private DefaultTreeCellRenderer coreTreeRenderer;
    private DefaultTreeRenderer xTreeRenderer;

    
    @Override
    protected void setUp() throws Exception {
//        setSystemLF(true);
        LOG.config("LF: " + UIManager.getLookAndFeel());
//        LOG.info("Theme: " + ((MetalLookAndFeel) UIManager.getLookAndFeel()).getCurrentTheme());
//        UIManager.put("Tree.drawsFocusBorderAroundIcon", Boolean.TRUE);
        coreTreeRenderer = new DefaultTreeCellRenderer();
        xTreeRenderer = new DefaultTreeRenderer();
    }

    public static void main(String[] args) {
        TreeRendererTest test = new TreeRendererTest();
        try {
            test.runInteractiveTests();
//            test.runInteractiveTests("interactiveDefaultWrapper");
//            test.runInteractiveTests("interactiveTreeButtonFormatting");
//            test.runInteractiveTests("interactiveXTreeLabelFormattingHighlighter");
//            test.runInteractiveTests(".*Wrapper.*");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    /**
     * Issue #873-swingx: WrappingIconPanel - delegate font unchanged
     */
    @Test
    public void testDelegateFont() {
        JXTree tree = new JXTree();
        tree.setCellRenderer(new DefaultTreeRenderer());
        final Font bold = tree.getFont().deriveFont(Font.BOLD, 20f);
        Highlighter hl = new AbstractHighlighter() {
            @Override
            protected Component doHighlight(Component component, ComponentAdapter adapter) {
                component.setFont(bold);
                return component;
            }
        };
        tree.addHighlighter(hl);
        WrappingIconPanel component = 
        	(WrappingIconPanel) tree.getCellRenderer().getTreeCellRendererComponent(tree, "something", false, false, false, 0, false);
        assertEquals(bold, component.getFont());
        assertEquals(bold, component.delegate.getFont());
    }

    /**
     * Wrapping provider: hyperlink foreground must be preserved.
     *
     */
    @Test
    public void testTreeHyperlinkForeground() {
        JXTree tree = new JXTree(createTreeModelWithLinks(20));
        ComponentProvider<JXHyperlink> context = new HyperlinkProvider();
        Color foreground = context.rendererComponent.getForeground();
        tree.setCellRenderer(new DefaultTreeRenderer(new WrappingProvider(context)));
        tree.getCellRenderer().getTreeCellRendererComponent(tree, "something", false, false, false, -1, false);
        assertEquals("hyperlink color must be preserved", foreground, context.rendererComponent.getForeground());
    }
    
    /**
     * related to Issue #22-swingx: tree background highlighting broken.
     * test if background color is moved down to delegate component.
     *
     */
    @Test
    public void testDelegateBackground() {
        WrappingProvider provider = new WrappingProvider();
        DefaultTreeRenderer renderer = new DefaultTreeRenderer(provider);
        Component comp = renderer.getTreeCellRendererComponent(null, "dummy", false, false, false, -1, false);
        assertTrue(comp instanceof WrappingIconPanel);
        comp.setBackground(Color.RED);
        // sanity
        assertTrue(provider.getRendererComponent(null).isBackgroundSet());
        assertEquals(Color.RED, provider.getRendererComponent(null).getBackground());
        // sanity
        assertTrue(provider.delegate.getRendererComponent(null).isBackgroundSet());
        assertEquals(Color.RED, provider.delegate.getRendererComponent(null).getBackground());
    }
    
    /**
     * related to Issue #22-swingx: tree background highlighting broken.
     * test if foreground color is moved down to delegate component.
     *
     */
    @Test
    public void testDelegateForeground() {
        WrappingProvider provider = new WrappingProvider();
        DefaultTreeRenderer renderer = new DefaultTreeRenderer(provider);
        Component comp = renderer.getTreeCellRendererComponent(null, "dummy", false, false, false, -1, false);
        assertTrue(comp instanceof WrappingIconPanel);
        comp.setForeground(Color.RED);
        // sanity
        assertTrue(provider.getRendererComponent(null).isForegroundSet());
        assertEquals(Color.RED, provider.getRendererComponent(null).getForeground());
        // sanity
        assertTrue(provider.delegate.getRendererComponent(null).isForegroundSet());
        assertEquals(Color.RED, provider.delegate.getRendererComponent(null).getForeground());
    }


    /**
     * characterize opaqueness of rendering components.
     * Hmm... tree-magic is different
     */
    @Test
    public void testTreeOpaqueRenderer() {
        // sanity
        assertFalse(new JLabel().isOpaque());
        
//        assertTrue(coreTreeRenderer.isOpaque());
//        assertTrue(xListRenderer.getRendererComponent().isOpaque());
    }

    /**
     * base existence/type tests while adding DefaultTableCellRendererExt.
     *
     */
    @Test
    public void testTreeRendererExt() {
        DefaultTreeRenderer renderer = new DefaultTreeRenderer();
        assertTrue(renderer instanceof TreeCellRenderer);
        assertTrue(renderer instanceof Serializable);
    }

//---------------------- interactive methods
 
    /**
     * Example for using no node icons in the tree.
     * 
     */
    public void interactiveCustomIconPerNodeType() {
        JTree tree = new JTree();
        tree.setCellRenderer(new DefaultTreeRenderer(IconValues.NONE));
        final JXFrame frame = wrapWithScrollingInFrame(tree, "tree - no icons");
        frame.setVisible(true);
    }
    

    /**
     * Sanity check: icons updated on LF change
     * 
     */
    public void interactiveDefaultIconsToggleLF() {
        JTree tree = new JTree();
        tree.setCellRenderer(new DefaultTreeRenderer());
        final JXFrame frame = wrapInFrame(tree, "tree - toggle lf", true);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }
    

    /**
     * Example for using arbitrary wrappee controllers. 
     * Here: a checkbox representing enabled entries in ActionMap.
     */
    public void interactiveTreeButtonFormatting() {
        TreeModel model = createActionTreeModel(); // model holds the ActionMap of a TreeTable
        JTree tree = new JTree(model);

        ComponentProvider<AbstractButton> wrappee = createButtonProvider();
        tree.setCellRenderer(new DefaultTreeRenderer(new WrappingProvider(wrappee)));
//        tree.expandAll(); // not implemented in JTree
        
        JList<Action> list = new JList<Action>(createActionListModel());
        list.setCellRenderer(new DefaultListRenderer<Action>(wrappee)); 
        final JXFrame frame = wrapWithScrollingInFrame(tree, list
        		, "custom renderer - same in tree (expand first child) and list");
        frame.setVisible(true);
    }
    

    /**
     * Custom format on JTree/JXTree (latter with highlighter).
     *
     */
    public void interactiveXTreeLabelFormattingHighlighter() {
        TreeModel model = createComponentHierarchyModel();
        JTree tree = new JTree(model);
    	// StringValue is "Functional Interface" aka SAM Type
    	StringValue sv = (Object value) -> {
    		if (value instanceof Component) {
    			Component c = (Component)value;
    			return "Name:" + c.getName() + "/"+c.getClass();
    		}
    		return StringValues.TO_STRING.getString(value);
    	};
    	DefaultTreeRenderer renderer = new DefaultTreeRenderer(sv);
        tree.setCellRenderer(renderer);
        
        @SuppressWarnings("serial")
		JXTree xtree = new JXTree(model) {
        	public Insets getInsets() {
        		return new Insets(5,5,5,5);
        	}
        	public TreeCellRenderer getCellRenderer() {
        		return new JXTree.DelegatingRenderer(sv) {
        	        public Component getTreeCellRendererComponent(JTree tree, Object value,
        	                boolean selected, boolean expanded, boolean leaf, int row,boolean hasFocus) {
        	        	Component c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        	        	LOG.fine("getTreeCellRendererComponent for "+(value==null?"":value.getClass())+" value "+value
        	        		+ " componentController/Provider:"+getComponentProvider()
        	        		+ "\n returns "+c);
        	        	return c;
        	        }
        		};
        	}
        };
        xtree.setHighlighters(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, Color.RED, Color.YELLOW));
        xtree.setRolloverEnabled(true);
        // share renderer
        xtree.setCellRenderer(tree.getCellRenderer());
//        xtree.setCellRenderer(renderer);
        final JXFrame frame = wrapWithScrollingInFrame(tree, xtree, "custom format - tree vs. xtree (+Rollover renderer)");
        frame.setVisible(true);
    }

    /**
     * Custom tree colors in JTree. Compare core default renderer with Swingx
     * default renderer.
     * 
     */
    public void interactiveCompareTreeExtTreeColors() {
        JTree xtree = new JTree();
        Color background = Color.MAGENTA;
        Color foreground = Color.YELLOW;
        xtree.setBackground(background);
        xtree.setForeground(foreground);
        DefaultTreeCellRenderer coreTreeCellRenderer = new DefaultTreeCellRenderer();
        // to get a uniform color on both tree and node
        // the core default renderer needs to be configured
        coreTreeCellRenderer.setBackgroundNonSelectionColor(background);
        coreTreeCellRenderer.setTextNonSelectionColor(foreground);
        xtree.setCellRenderer(coreTreeCellRenderer);
        JTree tree = new JTree();
        tree.setBackground(background);
        tree.setForeground(foreground);
        // swingx renderer uses tree colors
        tree.setCellRenderer(xTreeRenderer);
        final JXFrame frame = wrapWithScrollingInFrame(xtree, tree,
                "custom tree colors - core vs. ext renderer");
        frame.setVisible(true);
    }
    
    /**
     * Component orientation in JTree. Compare core default renderer with Swingx
     * default renderer.
     * 
     */
    public void interactiveCompareTreeRToL() {
        JTree xtree = new JTree();
        xtree.setCellRenderer(coreTreeRenderer);
        JTree tree = new JTree();
        tree.setCellRenderer(xTreeRenderer);
        final JXFrame frame = wrapWithScrollingInFrame(xtree, tree,
                "orientation - core vs. ext renderer");
        Action toggleComponentOrientation = new AbstractAction("toggle orientation") {

            public void actionPerformed(ActionEvent e) {
                ComponentOrientation current = frame.getComponentOrientation();
                if (current == ComponentOrientation.LEFT_TO_RIGHT) {
                    frame.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                } else {
                    frame.applyComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
                }
            }

        };
        addAction(frame, toggleComponentOrientation);
        frame.setVisible(true);
    }

    /**
     * Format custom model.
     *
     * PENDING: editor uses default toString and looses icons -
     *   because the renderer is not a label.
     */
    public void interactiveDefaultWrapper() {
        JTree xtree = new JTree(createComponentHierarchyModel());
    	// StringValue is "Functional Interface" aka SAM Type
    	StringValue componentFormat = (Object value) -> {
    		if (value instanceof Component) {
    			Component c = (Component)value;
    			return c.getName();
    		}
    		return StringValues.TO_STRING.getString(value);
    	};
        xtree.setCellRenderer(new DefaultTreeRenderer(componentFormat));
        xtree.setEditable(true);
        
        JTree tree = new JTree(new FileSystemModel());
        StringValue format = (Object value) -> {
        	if (value instanceof File) {
        		File file = (File)value;
        		return file.getName();
        	}
        	return StringValues.TO_STRING.getString(value);
        };
        tree.setCellRenderer(new DefaultTreeRenderer(format));
        final JXFrame frame = wrapWithScrollingInFrame(xtree, tree, "wrapper and different models");
        frame.setVisible(true);
    }
//-------------------------- factory methods
    
    private TreeModel createTreeModelWithLinks(int count) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Links");
        for (int i = 0; i < count; i++) {
            try {
                LinkModel link = new LinkModel("a link text " + i, null, new URL("http://some.dummy.url" + i));
                if (i == 1) {
                    URL url = JXEditorPaneTest.class.getResource("resources/test.html");

                    link = new LinkModel("a link text " + i, null, url);
                }
                root.add(new DefaultMutableTreeNode(link));
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return new DefaultTreeModel(root);
    }
    /**
     * 
     * @return a button controller specialized on ActionEntryNode.
     */
    private CheckBoxProvider createButtonProvider() {
    	// StringValue and BooleanValue are "Functional Interface" or SAM Types
    	StringValue sv = (Object value) -> {
    		if (value instanceof Action) {
    			Action action = (Action)value;
    			return (String)action.getValue(Action.NAME);
    		}
    		return "";
    	};
        BooleanValue bv = (Object value) -> {
        	if (value instanceof AbstractActionExt) {
        		AbstractActionExt aae = (AbstractActionExt)value;
        		return aae.isSelected();
        	}
        	return false;
        };

        // CheckBoxProvider extends ComponentProvider<AbstractButton>
        CheckBoxProvider wrapper = new CheckBoxProvider(new MappedValue(sv, null, bv), JLabel.LEADING);
        return wrapper;
    }


    /**
     * @return ListModel<Action>
     */
    private ListModel<Action> createActionListModel() {
        JXTable table = new JXTable(10, 10);
        table.setHorizontalScrollEnabled(true);
        ActionMap map = table.getActionMap();
        Object[] keys = map.keys();
        DefaultListModel<Action> model = new DefaultListModel<Action>();
        for (Object object : keys) {
        	Action action = map.get(object);
        	if(action instanceof BoundAction) {
        		// BoundAction extends AbstractActionExt extends AbstractAction, AbstractAction implements Action
        		model.addElement(action); 
        	} else if(action instanceof UIAction) {
        		// private JXTable$Actions extends UIAction, UIAction implements Action
        		model.addElement(action);
        	} else {
        		LOG.warning("key:"+object + " maps to action:"+action);
        		model.addElement(action);
        	}
        }
        return model;
    }

    /**
     * @return
     */
    private TreeModel createActionTreeModel() {
        JXTable table = new JXTable(10, 10);
        table.setHorizontalScrollEnabled(true);
        return new ActionMapTreeTableModel(table);
    }


	/**
	 * @return
	 */
	private TreeModel createComponentHierarchyModel() {
		JXFrame frame = new JXFrame("dummy");
		frame.add(new JScrollPane(new JXTree()));
		return new ComponentTreeTableModel(frame);
	}

}
