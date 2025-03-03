/*
 * Copyright 2007 Sun Microsystems, Inc., 4150 Network Circle,
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
package org.jdesktop.swingx.decorator;

import java.awt.Color;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import org.jdesktop.swingx.InteractiveTestCase;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ComponentAdapterTest.JXTableT;
import org.jdesktop.swingx.decorator.ComponentAdapterTest.JXTreeT;
import org.jdesktop.swingx.decorator.ComponentAdapterTest.JXTreeTableT;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.test.ComponentTreeTableModel;
import org.jdesktop.swingx.treetable.TreeTableModel;
import org.jdesktop.testtests.AncientSwingTeam;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Testing clients of ComponentAdapter, mainly clients which rely on uniform string 
 * representation across functionality. 
 * Not the optimal location, but where would that be? 
 * 
 * @author Jeanette Winzenburg
 * @author EUG https://github.com/homebeaver (reorg, use lambda for SAM interface StringValue)
 */
@RunWith(JUnit4.class)
public class ComponentAdapterClientTest extends InteractiveTestCase {

    private static final Logger LOG = Logger.getLogger(ComponentAdapterClientTest.class.getName());
    
    public static void main(String[] args) {
        ComponentAdapterClientTest test = new ComponentAdapterClientTest();
        try {
            test.runInteractiveTests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setUpJ4() throws Exception {
        setUp();
    }
    
    @After
    public void tearDownJ4() throws Exception {
        tearDown();
    }

    /**
     * A custom StringValue for Color. 
     * Maps to a string composed of the prefix "R/G/B: " and the Color's rgb value.
     */
    private StringValue sv;

    @Override
    protected void setUp() throws Exception {
        sv = createColorStringValue();
    }

    /**
     * Creates and returns a StringValue which maps a Color to it's R/G/B rep, 
     * prepending "R/G/B: "
     * 
     * @return the StringValue for color.
     */
    private StringValue createColorStringValue() {
    	StringValue sv = (Object value) -> {
            if (value instanceof Color) {
                Color color = (Color) value;
                return "R/G/B: " + color.getRGB();
            }
            return StringValues.TO_STRING.getString(value);
    	};
        return sv;
    }

    /**
     * Issue #1156-swingx: sort must use comparable
     * 
     * visually check that the custom string value for the color column is
     * used for sorting.
     */
    public void interactiveTableSortComparableAndStringValue() {
        JXTable table = new JXTable(new AncientSwingTeam());
        table.setDefaultRenderer(Color.class, new DefaultTableRenderer(sv));
        
        JTable core = new JTable(table.getModel());
        core.setAutoCreateRowSorter(true);
        JXFrame frame = wrapWithScrollingInFrame(table, core, "JXTable <--> JTable: Compare sorting of color column");
        
        show(frame);
    }

    /**
     * Issue #767-swingx: consistent string representation.
     * 
     * used in find/highlight
     */
    public void interactiveTableGetStringUsedInFind() {
        JXTable table = new JXTable(new AncientSwingTeam());
        table.setDefaultRenderer(Color.class, new DefaultTableRenderer(sv));
        HighlightPredicate predicate = new PatternPredicate("R/G/B: -2", 2, 2);
        table.addHighlighter(new ColorHighlighter(predicate, null, Color.RED));
        table.setColumnControlVisible(true);
        
        JXFrame frame = wrapWithScrollingInFrame(table, "Find/Highlight use adapter string value");
        addSearchModeToggle(frame);
        addMessage(frame, "Press ctrl-F to open search widget");
        show(frame);
    }

    /**
     * Issue #767-swingx: consistent string representation.
     * 
     * used in find/highlight
     */
    public void interactiveListGetStringUsedInFind() {
        JXList<Object> list = new JXList<Object>(AncientSwingTeam.createNamedColorListModel());
        list.setCellRenderer(new DefaultListRenderer<Object>(sv));
        HighlightPredicate predicate = new PatternPredicate("R/G/B: -2", 2, 2);
        list.addHighlighter(new ColorHighlighter(predicate, null, Color.RED));
        JXFrame frame = wrapWithScrollingInFrame(list, "Find/Highlight use adapter string value");
        addSearchModeToggle(frame);
        addMessage(frame, "Press ctrl-F to open search widget");
        show(frame);
    }

    /**
     * Issue #767-swingx: consistent string representation.
     * 
     * used in find/highlight
     */
    public void interactiveTreeGetStringUsedInFind() {
        JXTree table = new JXTree(AncientSwingTeam.createNamedColorTreeModel());
        table.setCellRenderer(new DefaultTreeRenderer(sv));
        HighlightPredicate predicate = new PatternPredicate("R/G/B: -2", 2, 2);
        table.addHighlighter(new ColorHighlighter(predicate, null, Color.RED));
        JXFrame frame = wrapWithScrollingInFrame(table, "Find/Highlight use adapter string value");
        addSearchModeToggle(frame);
        addMessage(frame, "Press ctrl-F to open search widget");
        show(frame);
    }
    
//--------------- unit tests

    /**
     * Issue #979-swingx: JXTreeTable broken string rep of hierarchical column
     * 
     * The breakage is visible in models with 
     * (node.toString) != (value for hierarchical column). 
     */
    @Test
    public void testTreeTableGetStringAtClippedTextRenderer() {
        JPanel panel = new JPanel();
        JButton button = new JButton();
        String buttonName = "buttonName";
        button.setName(buttonName);
        panel.add(button);
        TreeTableModel model = new ComponentTreeTableModel(panel);

        LOG.config("Root="+model.getRoot() + "\n with "+model.getChildCount(model.getRoot())+" childes"
        		+" ColumnCount="+model.getColumnCount() + " HierarchicalColumn="+model.getHierarchicalColumn());
        Object child = model.getChild(model.getRoot(), 0);
        LOG.config("the child:"+child);
//        for(int i=0;i<model.getColumnCount();i++) {
//        	System.out.println(""+i+":"+model.getColumnName(i)+"\t"+model.getColumnClass(i) + "\t"+model.getValueAt(child, i));
//        }
        assertEquals(4, model.getColumnCount());
        assertEquals(0, model.getHierarchicalColumn());
        assertEquals(1, model.getChildCount(model.getRoot())); // root:panel - child:button

        JXTree tree = new JXTree(model);
        tree.setRootVisible(true);
        tree.expandAll();
        String treeStringAt1 = tree.getStringAt(1);
        LOG.config("JXTree tree.getStringAt(1)="+treeStringAt1);
        
        
        JXTreeTableT treeTable = new JXTreeTableT(model);
        treeTable.setRootVisible(true);
        treeTable.expandAll();
        Object valueAt1 = treeTable.getModel().getValueAt(1, 0);
        LOG.config("expected ValueAt(1, 0)="+valueAt1 + " , type:"+valueAt1.getClass()
        	+ "\n IS treeTable.getStringAt(1, 0)="+treeTable.getStringAt(1, 0));
        assertEquals("string rep must be button name", treeTable.getValueAt(1, 0), treeTable.getStringAt(1, 0));
    }
    
    /**
     * Issue #979-swingx: JXTreeTable broken string rep of hierarchical column
     * 
     * here: test search (accidentally passing because node is instanceof NamedColor
     * with its toString the same as the value returned for the hierarchical column)
     */
    @Test
    public void testTreeTableGetStringUsedInSearchClippedTextRenderer() {
        JXTreeTableT table = new JXTreeTableT(AncientSwingTeam.createNamedColorTreeTableModel());
        table.expandAll();
        String text = table.getStringAt(2, 0);
        int matchRow = table.getSearchable().search(text);
        assertEquals(2, matchRow);
    }

    /**
     * Issue #979-swingx: JXTreeTable broken string rep of hierarchical column
     * 
     * here: test highlight (accidentally passing because node is instanceof NamedColor
     * with its toString the same as the value returned for the hierarchical column)
     */
    @Test
    public void testTreeTableGetStringUsedInPatternPredicateClippedTextRenderer() {
        JXTreeTableT table = new JXTreeTableT(AncientSwingTeam.createNamedColorTreeTableModel());
        int matchRow = 2;
        int matchColumn = 0;
        String text = table.getStringAt(matchRow, matchColumn);
        ComponentAdapter adapter = table.getComponentAdapter(matchRow, matchColumn);
        HighlightPredicate predicate = new PatternPredicate(text, matchColumn, PatternPredicate.ALL);
        assertTrue(predicate.isHighlighted(null, adapter));
    }

    /**
     * Issue #821-swingx: JXTreeTable broken string rep of hierarchical column
     * 
     * here: test highlight
     */
    @Test
    public void testTreeTableGetStringUsedInPatternPredicate() {
        JXTreeTableT table = new JXTreeTableT(AncientSwingTeam.createNamedColorTreeTableModel());
        table.setTreeCellRenderer(new DefaultTreeRenderer(sv));
        int matchRow = 2;
        int matchColumn = 0;
        String text = sv.getString(table.getValueAt(matchRow, matchColumn));
        ComponentAdapter adapter = table.getComponentAdapter(matchRow, matchColumn);
        HighlightPredicate predicate = new PatternPredicate(text, matchColumn, PatternPredicate.ALL);
        assertTrue(predicate.isHighlighted(null, adapter));
    }

    /**
     * Issue #821-swingx: JXTreeTable broken string rep of hierarchical column
     * see https://github.com/homebeaver/SwingSet/issues/53
     * here: test search
     */
    @Test
    public void testBuggyTreeTableGetStringUsedInSearch() {
        JXTreeTableT table = new JXTreeTableT(AncientSwingTeam.createNamedColorTreeTableModel());
        table.setTreeCellRenderer(new DefaultTreeRenderer(sv));
        Object objectAt2_0 = table.getValueAt(2, 0);
        String text = sv.getString(table.getValueAt(2, 0));
        LOG.config("objectAt2_0="+objectAt2_0+" sv.getString at table.getValueAt(2, 0)="+text);
        // table.getValueAt results to "R/G/B: -16711936" == text
        // cannot find text, because renderer shows color name
        text = "Green";
        int matchRow = table.getSearchable().search(text);
        assertEquals(2, matchRow);
    }
    /*
     * see https://github.com/homebeaver/SwingSet/issues/53
     * shows visible that hierarchical column is not rendered correctly
     */
    public void interactiveTreeTableGetStringUsedInSearch() {
        JXTreeTableT table = new JXTreeTableT(AncientSwingTeam.createNamedColorTreeTableModel());
        table.setTreeCellRenderer(new DefaultTreeRenderer(sv)); // the renter for table, not for hierarchical column
        JXFrame frame = wrapWithScrollingInFrame(table, "** buggy interactiveTreeTableGetStringUsedInSearch");
        addSearchModeToggle(frame);
        addMessage(frame, "Press ctrl-F to open search widget");
        show(frame);
    }
    /*
     * see https://github.com/homebeaver/SwingSet/issues/53
     * shows that renderer for hierarchical column ist set correctly
     */
    public void interactiveTreeTableGetStringUsedInSearch2() {
		@SuppressWarnings("serial")
		JXTreeTable.TreeTableCellRenderer renderer = new JXTreeTable.TreeTableCellRenderer(AncientSwingTeam.createNamedColorTreeTableModel()) {
			public TreeCellRenderer getCellRenderer() {
                return new JXTree.DelegatingRenderer(sv);
		    }
		};
		JXTreeTableT table = new JXTreeTableT(renderer);
        JXFrame frame = wrapWithScrollingInFrame(table, "** interactiveTreeTableGetStringUsedInSearch");
        addSearchModeToggle(frame);
        addMessage(frame, "Press ctrl-F to open search widget");
        show(frame);
    }

    /**
     * Issue #767-swingx: consistent string representation.
     * 
     * Here: test TableSearchable uses getStringXX
     */
    @Test
    public void testTreeGetStringAtUsedInSearch() {
        JXTreeT tree = new JXTreeT(AncientSwingTeam.createNamedColorTreeModel());
        tree.expandAll();
        tree.setCellRenderer(new DefaultTreeRenderer(sv));
        String text = sv.getString(((DefaultMutableTreeNode) tree.getPathForRow(2).getLastPathComponent()).getUserObject());
        int matchRow = tree.getSearchable().search(text);
        assertEquals(2, matchRow);
    }

    /**
     * Issue #767-swingx: consistent string representation.
     * 
     * Here: test TableSearchable uses getStringXX
     */
    @Test
    public void testListGetStringUsedInSearch() {
        JXList<Object> table = new JXList<Object>(AncientSwingTeam.createNamedColorListModel());
        table.setCellRenderer(new DefaultListRenderer<Object>(sv));
        String text = sv.getString(table.getElementAt(2));
        int matchRow = table.getSearchable().search(text);
        assertEquals(2, matchRow);
    }

    /**
     * Issue #767-swingx: consistent string representation.
     * 
     * Here: test TableSearchable uses getStringXX
     */
    @Test
    public void testTableGetStringUsedInSearch() {
        JXTable table = new JXTable(new AncientSwingTeam());
        table.setDefaultRenderer(Color.class, new DefaultTableRenderer(sv));
        String text = sv.getString(table.getValueAt(2, 2));
        int matchRow = table.getSearchable().search(text);
        assertEquals(2, matchRow);
    }

    /**
     * Issue #767-swingx: consistent string representation.
     * 
     * Here: test SearchPredicate uses getStringXX.
     */
    @Test
    public void testTableGetStringUsedInSearchPredicate() {
        JXTableT table = new JXTableT(new AncientSwingTeam());
        table.setDefaultRenderer(Color.class, new DefaultTableRenderer(sv));
        int matchRow = 3;
        int matchColumn = 2;
        String text = sv.getString(table.getValueAt(matchRow, matchColumn));
        ComponentAdapter adapter = table.getComponentAdapter(matchRow, matchColumn);
        SearchPredicate predicate = new SearchPredicate(text, matchRow, matchColumn);
        assertTrue(predicate.isHighlighted(null, adapter));
    }

    /**
     * Issue #767-swingx: consistent string representation.
     * 
     * Here: test PatternPredicate uses getStringxx().
     */
    @Test
    public void testTableGetStringUsedInPatternPredicate() {
        JXTableT table = new JXTableT(new AncientSwingTeam());
        table.setDefaultRenderer(Color.class, new DefaultTableRenderer(sv));
        int matchRow = 3;
        int matchColumn = 2;
        String text = sv.getString(table.getValueAt(matchRow, matchColumn));
        ComponentAdapter adapter = table.getComponentAdapter(matchRow, matchColumn);
        HighlightPredicate predicate = new PatternPredicate(text, matchColumn, PatternPredicate.ALL);
        assertTrue(predicate.isHighlighted(null, adapter));
    }

}
