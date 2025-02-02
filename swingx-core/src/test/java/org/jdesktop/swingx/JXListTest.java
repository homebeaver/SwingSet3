/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 */
package org.jdesktop.swingx;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.util.Arrays;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.RowFilter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.text.Position.Bias;

import org.jdesktop.swingx.JXList.DelegatingRenderer;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.ComponentAdapterTest.JXListT;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.SearchPredicate;
import org.jdesktop.swingx.hyperlink.LinkModel;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.rollover.ListRolloverController;
import org.jdesktop.swingx.rollover.RolloverProducer;
import org.jdesktop.swingx.sort.DefaultSortController;
import org.jdesktop.swingx.sort.ListSortController;
import org.jdesktop.swingx.sort.RowFilters;
import org.jdesktop.swingx.sort.StringValueRegistry;
import org.jdesktop.swingx.sort.TableSortController;
import org.jdesktop.test.PropertyChangeReport;
import org.jdesktop.test.TestUtils;
import org.jdesktop.testtests.AncientSwingTeam;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Testing JXList. <p>
 * 
 * Note: all tests related to the disabled sorting/filtering support are moved to
 * JXListSortRevamp and forced to fail.
 * 
 * @author Jeanette Winzenburg
 */
@RunWith(JUnit4.class)
public class JXListTest extends InteractiveTestCase {

    private static final Logger LOG = Logger.getLogger(JXListTest.class.getName());

    @Before
    public void setUpJ4() throws Exception {
        setUp();
    }
    
    @After
    public void tearDownJ4() throws Exception {
        tearDown();
    }

    @Override // Override junit.framework.TestCase : Sets up the fixture
    protected void setUp() throws Exception {
        super.setUp();
        list = new JXList<Object>();
        listModel = createListModel();
        ascendingListModel = createAscendingListModel(0, 20);
        sv = createColorStringValue();
    }
    
    protected ListModel<Object> listModel;
    protected DefaultListModel<Object> ascendingListModel;
    /** empty default list */
    private JXList<Object> list;
    private StringValue sv;

    public JXListTest() {
        super("JXList Tests");
    }

    protected ListModel<Object> createListModel() {
        JXList<?> list = new JXList<Object>();
        return new DefaultComboBoxModel<Object>(list.getActionMap().allKeys());
    }

    protected DefaultListModel<Object> createAscendingListModel(int startRow, int count) {
        DefaultListModel<Object> lm = new DefaultListModel<Object>();
        for (int row = startRow; row < startRow  + count; row++) {
            lm.addElement(Integer.valueOf(row));
        }
        return lm;
    }

    /**
     * Issue #1563-swingx: find cell that was clicked for componentPopup
     * 
     * Test api and event firing.
     */
    @Test
    public void testPopupTriggerLocationAvailable() {
    	LOG.config("listModel.Size="+listModel.getSize()
    		+(listModel.getSize()>0 ? " first<E>:"+listModel.getElementAt(0) : "/ empty"));
        JXList<Object> table = new JXList<Object>(listModel);
        // x = 40 ; y = 5
        MouseEvent event = new MouseEvent(table, 0, 0, 0, 40, 5, 0, false);
        PropertyChangeReport report = new PropertyChangeReport(table);
        Point p = table.getPopupLocation(event);
        LOG.info("expected change of prop Point oldValue/newValue:"+p + " / "+event.getPoint());
        assertEquals(event.getPoint(), table.getPopupTriggerLocation());
        // oldValue = null ; newValue = event.getPoint()
        TestUtils.assertPropertyChangeEvent(report, "popupTriggerLocation", null, event.getPoint());
    }
    
    
    /**
     * Issue #1563-swingx: find cell that was clicked for componentPopup
     * 
     * Test safe return value.
     */
    @Test
    public void testPopupTriggerCopy() {
    	LOG.config("listModel.Size="+listModel.getSize()
    		+(listModel.getSize()>0 ? " last<E>:"+listModel.getElementAt(listModel.getSize()-1) : "/ empty"));
        JXList<Object> table = new JXList<Object>(listModel);
        MouseEvent event = new MouseEvent(table, 0, 0, 0, 40, 5, 0, false);
        table.getPopupLocation(event);
        if(table.getPopupTriggerLocation()==table.getPopupTriggerLocation()) {
        	LOG.warning("Same!!!!!!!!");
        } else {
            LOG.info("assertNotSame instances but equal hashCode: "+table.getPopupTriggerLocation().hashCode() + " / "+table.getPopupTriggerLocation().hashCode());
        }
        assertNotSame("trigger point must not be same", 
                table.getPopupTriggerLocation(), table.getPopupTriggerLocation());
    }
    
    /**
     * Issue #1563-swingx: find cell that was clicked for componentPopup
     * 
     * Test safe handle null.
     */
    @Test
    public void testPopupTriggerKeyboard() {
        JXList<Object> table = new JXList<Object>(listModel);
        MouseEvent event = new MouseEvent(table, 0, 0, 0, 40, 5, 0, false);
        table.getPopupLocation(event);
        PropertyChangeReport report = new PropertyChangeReport(table);
        table.getPopupLocation(null); // null if the popup is not being shown as the result of a mouse event
        assertNull("trigger must null", 
                table.getPopupTriggerLocation());
        TestUtils.assertPropertyChangeEvent(report, "popupTriggerLocation", 
                event.getPoint(), null);
    }


    /**
     * Issue #1263-swingx: JXList selectedValues must convert index to model.
     */
    @Test
    public void testGetSelectedValue() {
        DefaultListModel<String> model = new DefaultListModel<String>();
        model.addElement("One");
        model.addElement("Two");
        model.addElement("Three");
        model.addElement("Four");
        model.addElement("Five");
        model.addElement("Six");
        model.addElement("Seven");
    	LOG.fine("listModel.Size="+model.getSize());
        JXList<String> list = new JXList<String>();
        list.setAutoCreateRowSorter(true);
        list.setModel(model);
        list.setSelectedIndex(2);
    	LOG.fine("list.ElementCount="+list.getElementCount());
        assertEquals("Three", list.getSelectedValue());
        list.setRowFilter(new RowFilter<ListModel<Object>, Integer>() {

			@Override
			public boolean include(Entry<? extends ListModel<Object>, ? extends Integer> entry) {
				return entry.getStringValue(entry.getIdentifier()).contains("e");
			}

        });
        assertEquals("Three", list.getSelectedValue());
    	LOG.info("listModel.Size="+model.getSize() + " list.ElementCount without Two,Four,Six ="+list.getElementCount());
    }

    /**
     * Issue #1263-swingx: JXList selectedValues must convert index to model.
     */
    @Test
    public void testGetSelectedValues() {
        DefaultListModel<String> model = new DefaultListModel<String>();
        model.addElement("One");
        model.addElement("Two");
        model.addElement("Three");
        model.addElement("Four");
        model.addElement("Five");
        model.addElement("Six");
        model.addElement("Seven");
        JXList<String> list = new JXList<String>();
        list.setAutoCreateRowSorter(true);
        list.setModel(model);
        list.setSelectedIndex(2);
        list.addSelectionInterval(0, 2);
        assertTrue(Arrays.deepEquals(new Object[] {"One", "Two", "Three"}, list.getSelectedValues()));
        
        list.setRowFilter(new RowFilter<ListModel<Object>, Integer>() {

			@Override
			public boolean include(Entry<? extends ListModel<Object>, ? extends Integer> entry) {
				return entry.getStringValue(entry.getIdentifier()).contains("e");
			}

        });
        assertTrue(Arrays.deepEquals(new Object[] {"One", "Three"}, list.getSelectedValues()));
        
        list.clearSelection();
        list.addSelectionInterval(0, 2);
        assertTrue(Arrays.deepEquals(new Object[] {"One", "Three", "Five"}, list.getSelectedValues()));
    }

    /**
     * Issue #1263-swingx: JXList selectedValues must convert index to model.
     */
    @Test
    public void testSetSelectedValue() {
        DefaultListModel<String> model = new DefaultListModel<String>();
        model.addElement("One");
        model.addElement("Two");
        model.addElement("Three");
        model.addElement("Four");
        model.addElement("Five");
        model.addElement("Six");
        model.addElement("Seven");
        JXList<String> list = new JXList<String>();
        list.setAutoCreateRowSorter(true);
        list.setModel(model);
        list.setSelectedValue("Three", false);
        assertEquals(2, list.getSelectedIndex());
        
        list.setRowFilter(new RowFilter<ListModel<Object>, Integer>() {

			@Override
			public boolean include(Entry<? extends ListModel<Object>, ? extends Integer> entry) {
				return entry.getStringValue(entry.getIdentifier()).contains("e");
			}

        });
        assertEquals(1, list.getSelectedIndex());
        list.setSelectedValue("Five", false);
        assertEquals(2, list.getSelectedIndex());
    }


    /**
     * Issue #1232-swingx: JXList must fire property change on setCellRenderer.
     * 
     */
    @Test
    public void testRendererNotification() {
        JXList<?> list = new JXList<Object>();
        assertNotNull("sanity: ", list.getCellRenderer());
        // very first setting: fires twice ... a bit annoying but ... waiting for complaints ;-)
        list.setCellRenderer(new DefaultListRenderer<>());
        PropertyChangeReport report = new PropertyChangeReport(list);
        list.setCellRenderer(new DefaultListRenderer<>());
        TestUtils.assertPropertyChangeEvent(report, "cellRenderer", null, list.getCellRenderer());
        LOG.info("report.EventCount:"+report.getEventCount());
    }
    /**
     * Issue #1162-swingx: getNextMatch incorrect if sorted.
     */
    @Test
    public void testNextMatch() {
        JXList<Object> list = new JXList<Object>(AncientSwingTeam.createNamedColorListModel(), true);
        int index = list.getNextMatch("b", 0, Bias.Forward);
        assertEquals(1, index);
        list.toggleSortOrder();
        assertEquals(0, list.getNextMatch("b", 0, Bias.Forward));
    }
    
    /**
     * Issue #1162-swingx: getNextMatch incorrect for non-standard stringValue
     */
    @Test
    public void testNextMatchUseString() {
        JXList<Object> list = new JXList<Object>(AncientSwingTeam.createNamedColorListModel(), true);
        list.setCellRenderer(new DefaultListRenderer<>(sv));
        assertEquals("must not find a match for 'b', all start with 'r'", 
                -1, list.getNextMatch("b", 0, Bias.Forward));
    }
    /**
     * Issue 1161-swingx: JXList not completely updated on setRowFilter
     * see JXListVisualCheck.interactiveRevalidateOnSetRowFilter
     */
    @Test
    public void testRevalidateOnSetRowFilter() throws InterruptedException, InvocationTargetException {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            return;
        }
        LOG.config("run ui test");
        final JXList<Object> list = new JXList<Object>(AncientSwingTeam.createNamedColorListModel(), true);
        showWithScrollingInFrame(list, "");
        final Dimension size = list.getSize();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                RowFilter<? super ListModel<Object>, ? super Integer> filter = RowFilters.regexFilter(Pattern.CASE_INSENSITIVE, "^b");
                list.setRowFilter(filter);
            }
        });
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                // subtraction arbitrary number, don't want to depend on single pixels
                assertTrue("height must be adjusted to reduced number of rows, " +
                		"but was (old/current): " + size.height + "/" + list.getSize().height, 
                		size.height - 50 > list.getSize().height);

            }
        });
    }
//--------------- string rep
    /**
     * Issue #1152-swingx: re-enable filtering with single-string-representation.
     * was: Issue #767-swingx: consistent string representation.
     * 
     * Here: test Pattern filtering uses string rep
     */
    @Test
    public void testListGetStringUsedInPatternFilter() {
        JXList<Object> list = new JXList<Object>(AncientSwingTeam.createNamedColorListModel(), true);
        list.setCellRenderer(new DefaultListRenderer<>(sv));
        RowFilter<Object, Integer> filter = RowFilter.regexFilter("R/G/B: -2.*", 0);
        list.setRowFilter(filter);
        assertTrue(list.getElementCount() > 0);
        assertEquals(sv.getString(list.getElementAt(0)), list.getStringAt(0));
    }

    /**
     * Issue #1152-swingx: re-enable filtering with single-string-representation.
     * was: Issue #767-swingx: consistent string representation.
     * 
     * Here: test list has stringValueProvider and configures the sortController with it
     */
    @Test
    public void testStringValueRegistry() {
        JXList<Object> list = new JXList<Object>(AncientSwingTeam.createNamedColorListModel(), true);
        assertSame(list.getStringValueRegistry(), getSortController(list).getStringValueProvider());
    }
    
    /**
     * Issue #1152-swingx: re-enable filtering with single-string-representation.
     * was: Issue #767-swingx: consistent string representation.
     * 
     * Here: test updates the sortController on renderer change.
     */
    @Test
    public void testStringValueRegistryFromRendererChange() {
        JXList<Object> list = new JXList<Object>(AncientSwingTeam.createNamedColorListModel(), true);
        StringValueRegistry provider = list.getStringValueRegistry();
        list.setCellRenderer(new DefaultListRenderer<>(sv));
        assertEquals(list.getWrappedCellRenderer(), provider.getStringValue(0, 0));
    }

    /**
     * Issue #1152-swingx: re-enable filtering with single-string-representation.
     * was: Issue #767-swingx: consistent string representation.
     * 
     * Here: test getStringAt use provider (sanity, trying to pull the rag failed
     * during re-enable)
     */
    @Test
    public void testStringAtUseProvider() {
        JXList<Object> list = new JXList<Object>(AncientSwingTeam.createNamedColorListModel(), true);
        list.setCellRenderer(new DefaultListRenderer<>(sv));
        list.getStringValueRegistry().setStringValue(StringValues.TO_STRING, 0);
        assertEquals(StringValues.TO_STRING.getString(list.getElementAt(0)), list.getStringAt(0));      
    }
    /**
     * Issue #1152-swingx: re-enable filtering with single-string-representation.
     * was: Issue #767-swingx: consistent string representation.
     * 
     * Here: test getStringAt of ComponentAdapter use provider 
     * (sanity, trying to pull the rag failed during re-enable)
     */
    @Test
    public void testStringAtComponentAdapterUseProvider() {
        JXListT list = new JXListT(AncientSwingTeam.createNamedColorListModel(), true);
        list.setCellRenderer(new DefaultListRenderer<>(sv));
        list.getStringValueRegistry().setStringValue(StringValues.TO_STRING, 0);
        ComponentAdapter adapter = list.getComponentAdapter();
        assertEquals(StringValues.TO_STRING.getString(list.getElementAt(0)), adapter.getStringAt(0, 0));      
    }
    
//----------------- sorter api on JXList

    /**
     * JXList has responsibility to guarantee usage of its comparator.
     */
    @Test
    public void testSetComparatorToSortController() {
        JXList<Object> list = new JXList<Object>(listModel, true);
        list.setComparator(Collator.getInstance());
        assertSame(list.getComparator(), getSortController(list).getComparator(0));
    }
    
    /**
     * added setSortOrder(SortOrder)
     */
    @Test
    public void testSetSortOrder() {
        JXList<Object> list = new JXList<Object>(ascendingListModel, true);
        list.setSortOrder(SortOrder.ASCENDING);
        assertSame("column must be sorted after setting sortOrder on ", SortOrder.ASCENDING, list.getSortOrder());
        assertSame(SortOrder.ASCENDING, getSortController(list).getSortOrder(0));
    }

    /**
     * testing new sorter api: 
     * getSortOrder(), toggleSortOrder(), resetSortOrder().
     *
     */
    @Test
    public void testToggleSortOrder() {
        JXList<Object> list = new JXList<Object>(ascendingListModel, true);
        assertSame(SortOrder.UNSORTED, list.getSortOrder());
        list.toggleSortOrder();
        assertSame(SortOrder.ASCENDING, list.getSortOrder());
        list.toggleSortOrder();
        assertSame(SortOrder.DESCENDING, list.getSortOrder());
        list.resetSortOrder();
        assertSame(SortOrder.UNSORTED, list.getSortOrder());
    }

    /**
     * prepare sort testing: internal probs with SortController?
     */
    @Test
    public void testSortController() {
        JXList<Object> list = new JXList<Object>(ascendingListModel, true);
        assertNotNull("sortController must be initialized", list.getRowSorter());
    }
    

//----------------- data api on JXList
    /**
     * testing that rowSorter's model is updated
     */
    @Test
    public void testSetModel() {
        JXList<Object> list = new JXList<Object>(true);
        list.setModel(listModel);
        assertEquals(listModel.getSize(), list.getElementCount());
        assertSame(listModel, list.getRowSorter().getModel());
    }



    @Test(expected = IndexOutOfBoundsException.class)
    public void testConvertToModelPreconditions() {
        final JXList<Object> list = new JXList<Object>(ascendingListModel, true);
        assertEquals(20, list.getElementCount());
        RowFilter<ListModel<?>, Integer> filter = RowFilters.regexFilter("0", 0);
        list.setRowFilter(filter);
        assertEquals(2, list.getElementCount());
        list.convertIndexToModel(list.getElementCount());
    }
 

    @Test(expected = IndexOutOfBoundsException.class)
    public void testElementAtPreconditions() {
        final JXList<Object> list = new JXList<Object>(ascendingListModel, true);
        assertEquals(20, list.getElementCount());
        RowFilter<ListModel<?>, Integer> filter = RowFilters.regexFilter("0", 0);
        list.setRowFilter(filter);
        assertEquals(2, list.getElementCount());
        list.getElementAt(list.getElementCount());
    }

    /**
     * 
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testConvertToViewPreconditions() {
        final JXList<Object> list = new JXList<Object>(ascendingListModel);
        list.setAutoCreateRowSorter(true);
        assertEquals(20, list.getElementCount());
        RowFilter<ListModel<?>, Integer> filter = RowFilters.regexFilter("0", 0);
        list.setRowFilter(filter);
        assertEquals(2, list.getElementCount());
        list.convertIndexToView(ascendingListModel.getSize());
    }

    @Test
    public void testNoSorter() {
        JXList<Object> list = new JXList<Object>(ascendingListModel);
        assertEquals(ascendingListModel.getSize(), list.getElementCount());
        assertEquals(ascendingListModel.getElementAt(0), list.getElementAt(0));
    }
    
    @Test
    public void testSorterNotSorted() {
        JXList<Object> list = new JXList<Object>(ascendingListModel, true);
        assertEquals(ascendingListModel.getSize(), list.getElementCount());
        assertEquals(ascendingListModel.getElementAt(0), list.getElementAt(0));
    }
    
    @Test
    public void testSorterSorted() {
        JXList<Object> list = new JXList<Object>(ascendingListModel, true);
        list.setSortOrder(SortOrder.DESCENDING);
        assertEquals(ascendingListModel.getSize(), list.getElementCount());
        assertEquals(ascendingListModel.getElementAt(0), list.getElementAt(list.getElementCount() - 1));
    }


    //------------- sort properties

    @Test
    public void testPropertiesToSorterOnSetRowsorter() {
        list.setAutoCreateRowSorter(true);
        list.setSortsOnUpdates(false);
        list.setSortable(false);
        Collator comparator = Collator.getInstance();
        list.setComparator(comparator);
        ListSortController<ListModel<Object>> controller = new ListSortController<ListModel<Object>>(list.getModel());
        list.setRowSorter(controller);

        assertEquals("sortable propagated", false, controller.isSortable(0));
        assertSame("comparator propagated", comparator, controller.getComparator(0));
        assertEquals("sortsOnUpdates propagated", false, controller.getSortsOnUpdates());
    }
    
    @Test
    public void testSortsOnUpdate() {
        PropertyChangeReport report = new PropertyChangeReport(list);
        list.setSortsOnUpdates(false);
        TestUtils.assertPropertyChangeEvent(report, "sortsOnUpdates", true, false);
        assertFalse(list.getSortsOnUpdates());
    }

    
    @Test
    public void testSortable() {
        PropertyChangeReport report = new PropertyChangeReport(list);
        list.setSortable(false);
        TestUtils.assertPropertyChangeEvent(report, "sortable", true, false);
        assertFalse(list.isSortable());
    }
    
    /**
     * Setting table's sortable property updates controller.
     */
    @Test
    public void testTableRowFilterSynchedToController() {
        JXList<Object> list = new JXList<Object>(true);
        RowFilter<Object, Object> filter = RowFilters.regexFilter(".*");
        list.setRowFilter(filter);
        assertEquals(filter, getSortController(list).getRowFilter());
        assertEquals(filter, list.getRowFilter());
    }
    
    /**
     * Setting table's sortable property updates controller.
     */
    @Test
    public void testSortOrderCycle() {
        JXList<Object> list = new JXList<Object>(true);
        SortOrder[] cycle = new SortOrder[] {SortOrder.DESCENDING, SortOrder.UNSORTED};
        PropertyChangeReport report = new PropertyChangeReport(list);
        list.setSortOrderCycle(cycle);
        TestUtils.assertPropertyChangeEvent(report, "sortOrderCycle", 
                    DefaultSortController.getDefaultSortOrderCycle(), list.getSortOrderCycle());
    }


    /**
     * Convenience: type cast of default rowSorter.
     * @param list
     * @return
     */
    private ListSortController<? extends ListModel<?>> getSortController(JXList<?> list) {
        return (ListSortController<? extends ListModel<?>>) list.getRowSorter();
    }

    //------------ rowSorter api
    
    /**
     * test filterEnabled property on initialization.
     *
     */
    @Test
    public void testConstructorAutoCreateSorter() {
        assertAutoCreateRowSorter(new JXList<>(), false);
        assertAutoCreateRowSorter(new JXList<>(new DefaultListModel<>()), false);
        assertAutoCreateRowSorter(new JXList<>(new Vector<Object>()), false);
        assertAutoCreateRowSorter(new JXList<>(new Object[] { }), false);
        
        assertAutoCreateRowSorter(new JXList<>(false), false);
        assertAutoCreateRowSorter(new JXList<>(new DefaultListModel<>(), false), false);
        assertAutoCreateRowSorter(new JXList<>(new Vector<Object>(), false), false);
        assertAutoCreateRowSorter(new JXList<>(new Object[] { }, false), false);

        assertAutoCreateRowSorter(new JXList<>(true), true);
        assertAutoCreateRowSorter(new JXList<>(new DefaultListModel<>(), true), true);
        assertAutoCreateRowSorter(new JXList<>(new Vector<Object>(), true), true);
        assertAutoCreateRowSorter(new JXList<>(new Object[] { }, true), true);
    }
    
    private void assertAutoCreateRowSorter(JXList<?> list, boolean b) {
        assertEquals(b, list.getAutoCreateRowSorter());
    }
    
   
    @Test
    public void testRowSorterSet() {
        assertNull(list.getRowSorter());
        ListSortController<ListModel<Object>> controller = new ListSortController<ListModel<Object>>(list.getModel());
        PropertyChangeReport report = new PropertyChangeReport(list);
        list.setRowSorter(controller);
        TestUtils.assertPropertyChangeEvent(report, list, "rowSorter", null, controller);
        assertSame(controller, list.getRowSorter());
    }
    
    @Test
    public void testAutoCreateRowSorterSet() {
        PropertyChangeReport report = new PropertyChangeReport(list);
        list.setAutoCreateRowSorter(true);
        assertTrue(list.getAutoCreateRowSorter());
        TestUtils.assertPropertyChangeEvent(report, "autoCreateRowSorter", false, true, false);
        assertNotNull(list.getRowSorter());
    }
 
    
    /**
     * Test assumptions of accessing list model/view values through
     * the list's componentAdapter.
     */
    @Test
    public void testComponentAdapterCoordinates() {
        JXList<Object> list = new JXList<Object>(ascendingListModel, true);
        list.setComparator(TableSortController.COMPARABLE_COMPARATOR);
        Object originalFirstRowValue = list.getElementAt(0);
        Object originalLastRowValue = list.getElementAt(list.getElementCount() - 1);
        assertEquals("view row coordinate equals model row coordinate", 
                list.getModel().getElementAt(0), originalFirstRowValue);
        // sort first column - actually does not change anything order 
        list.toggleSortOrder();
        // sanity asssert
        assertEquals("view order must be unchanged ", 
                list.getElementAt(0), originalFirstRowValue);
        // invert sort
        list.toggleSortOrder();
        // sanity assert
        assertEquals("view order must be reversed changed ", 
                list.getElementAt(0), originalLastRowValue);
        ComponentAdapter adapter = list.getComponentAdapter();
        assertEquals("adapter filteredValue expects row view coordinates", 
                list.getElementAt(0), adapter.getFilteredValueAt(0, 0));
        // adapter coordinates are view coordinates
        adapter.row = 0;
        adapter.column = 0;
        assertEquals("adapter.getValue must return value at adapter coordinates", 
                list.getElementAt(0), adapter.getValue());
        assertEquals("adapter.getValue must return value at adapter coordinates", 
                list.getElementAt(0), adapter.getValue(0));
    }
    

//------------------------end of re-enable sort/filter
    /**
     * Issue #816-swingx: Delegating renderer must create list's default.
     * Consistent api: expose wrappedRenderer the same way as wrappedModel
     */
    @Test
    public void testWrappedRendererDefault() {
        JXList<?> list = new JXList<>();
        ListCellRenderer<?> lcr = list.getCellRenderer();
        // XXX JXList.DelegatingRenderer is not row! but implements interface ListCellRenderer<E>
        LOG.info("ListCellRenderer<?> lcr type "+lcr.getClass() +" is instanceof ListCellRenderer="+(lcr instanceof ListCellRenderer));
        DelegatingRenderer renderer = (DelegatingRenderer)list.getCellRenderer();
        assertSame("wrapping renderer must use list's default on null", 
                 renderer.getDelegateRenderer(), list.getWrappedCellRenderer());
    }

    /**
     * Issue #816-swingx: Delegating renderer must create list's default.
     * Consistent api: expose wrappedRenderer the same way as wrappedModel
     */
    @Test
    public void testWrappedRendererCustom() {
        JXList<Object> list = new JXList<Object>();
        DelegatingRenderer renderer = (DelegatingRenderer) list.getCellRenderer();
        ListCellRenderer<Object> custom = new DefaultListRenderer<Object>();
        list.setCellRenderer(custom);
        assertSame("wrapping renderer must use list's default on null", 
                 renderer.getDelegateRenderer(), list.getWrappedCellRenderer());
    }
    
    /**
     * Issue #816-swingx: Delegating renderer must create list's default.
     * Delegating uses default on null, here: default default.
     */
    @Test
    public void testDelegatingRendererUseDefaultSetNull() {
        JXList<Object> list = new JXList<Object>();
        ListCellRenderer<Object> defaultRenderer = list.createDefaultCellRenderer();
        DelegatingRenderer renderer = (DelegatingRenderer) list.getCellRenderer();
        list.setCellRenderer(null);
        assertEquals("wrapping renderer must use list's default on null", 
                defaultRenderer.getClass(), renderer.getDelegateRenderer().getClass());
    }

    /**
     * Issue #816-swingx: Delegating renderer must create list's default.
     * Delegating has default from list initially, here: default default.
     */
    @Test
    public void testDelegatingRendererUseDefault() {
        JXList<Object> list = new JXList<Object>();
        ListCellRenderer<Object> defaultRenderer = list.createDefaultCellRenderer();
        assertEquals("sanity: creates default", DefaultListRenderer.class, 
                defaultRenderer.getClass());
        DelegatingRenderer renderer = (DelegatingRenderer) list.getCellRenderer();
        assertEquals(defaultRenderer.getClass(), renderer.getDelegateRenderer().getClass());
    }
    
    /**
     * Issue #816-swingx: Delegating renderer must create list's default.
     * Delegating has default from list initially, here: custom default.
     */
    @Test
    public void testDelegatingRendererUseCustomDefaultSetNull() {
        JXList list = new JXList() {

            @Override
            protected ListCellRenderer createDefaultCellRenderer() {
                return new CustomDefaultRenderer();
            }
            
        };
        ListCellRenderer defaultRenderer = list.createDefaultCellRenderer();
        DelegatingRenderer renderer = (DelegatingRenderer) list.getCellRenderer();
        list.setCellRenderer(null);
        assertEquals("wrapping renderer must use list's default on null",
                defaultRenderer.getClass(), renderer.getDelegateRenderer().getClass());
    }
    
    /**
     * Issue #816-swingx: Delegating renderer must create list's default.
     * Delegating has default from list initially, here: custom default.
     */
    @Test
    public void testDelegatingRendererUseCustomDefault() {
        @SuppressWarnings("serial")
		JXList<Object> list = new JXList<>() {

            @Override
            protected ListCellRenderer<Object> createDefaultCellRenderer() {
                return new CustomDefaultRenderer();
            }
            
        };
        ListCellRenderer<Object> defaultRenderer = list.createDefaultCellRenderer();
        assertEquals("sanity: creates custom", CustomDefaultRenderer.class, 
                defaultRenderer.getClass());
        DelegatingRenderer renderer = (DelegatingRenderer) list.getCellRenderer();
        assertEquals(defaultRenderer.getClass(), renderer.getDelegateRenderer().getClass());
    }
    /**
     * Dummy extension for testing - does nothing more as super.
     */
    @SuppressWarnings("serial")
	public static class CustomDefaultRenderer extends DefaultListCellRenderer {
    }
    
    /**
     * Issue #767-swingx: consistent string representation.
     * 
     * Here: test api on JXTable.
     */
    @Test
    public void testGetString() {
        JXList<Object> list = new JXList<Object>(AncientSwingTeam.createNamedColorListModel());
        @SuppressWarnings("serial")
		StringValue sv = new StringValue() {

            public String getString(Object value) {
                if (value instanceof Color) {
                    Color color = (Color) value;
                    return "R/G/B: " + color.getRGB();
                }
                return StringValues.TO_STRING.getString(value);
            }
            
        };
        DefaultListRenderer<Object> renderer = new DefaultListRenderer<>(sv);
        list.setCellRenderer(renderer);
        String text = list.getStringAt(0);
        assertEquals(sv.getString(list.getElementAt(0)), text);
    }
    

    /**
     * test that swingx renderer is used by default.
     *
     */
    @Test
    public void testDefaultListRenderer() {
        JXList<?> list = new JXList<>();
        ListCellRenderer<Object> renderer = ((DelegatingRenderer)list.getCellRenderer()).getDelegateRenderer();
        assertTrue("default renderer expected to be DefaultListRenderer " +
                        "\n but is " + renderer.getClass(),
                renderer instanceof DefaultListRenderer);
    }
    
    /**
     * Issue #473-swingx: NPE in list with highlighter. <p> 
     * 
     * Renderers are doc'ed to cope with invalid input values.
     * Highlighters can rely on valid ComponentAdapter state. 
     * JXList delegatingRenderer is the culprit which does set
     * invalid ComponentAdapter state. Negative invalid index.
     *
     */
    @Test
    public void testIllegalNegativeListRowIndex() {
        JXList<Object> list = new JXList<Object>(new Object[] {1, 2, 3});
        ListCellRenderer<Object> renderer = list.getCellRenderer();
        renderer.getListCellRendererComponent(list, "dummy", -1, false, false);
        SearchPredicate predicate = new SearchPredicate("\\QNode\\E");
        Highlighter searchHighlighter = new ColorHighlighter(predicate, null, Color.RED);
        list.addHighlighter(searchHighlighter);
        renderer.getListCellRendererComponent(list, "dummy", -1, false, false);
    }
    
    /**
     * Issue #473-swingx: NPE in list with highlighter. <p> 
     * 
     * Renderers are doc'ed to cope with invalid input values.
     * Highlighters can rely on valid ComponentAdapter state. 
     * JXList delegatingRenderer is the culprit which does set
     * invalid ComponentAdapter state. Invalid index > valid range.
     *
     */
    @Test
    public void testIllegalExceedingListRowIndex() {
        JXList<Object> list = new JXList<Object>(new Object[] {1, 2, 3});
        ListCellRenderer<Object> renderer = list.getCellRenderer();
        renderer.getListCellRendererComponent(list, "dummy", list.getElementCount(), false, false);
        SearchPredicate predicate = new SearchPredicate("\\QNode\\E");
        Highlighter searchHighlighter = new ColorHighlighter(predicate, null, Color.RED);
        list.addHighlighter(searchHighlighter);
        renderer.getListCellRendererComponent(list, "dummy", list.getElementCount(), false, false);
    }
    
    /**
     * test convenience method accessing the configured adapter.
     *
     */
    @Test
    public void testConfiguredComponentAdapter() {
        JXList<Object> list = new JXList<Object>(new Object[] {1, 2, 3});
        ComponentAdapter adapter = list.getComponentAdapter();
        assertEquals(0, adapter.column);
        assertEquals(0, adapter.row);
        adapter.row = 1;
        // corrupt adapter
        adapter.column = 1;
        adapter = list.getComponentAdapter(0);
        assertEquals(0, adapter.column);
        assertEquals(0, adapter.row);
    }
    

    /**
     * test exceptions on null data(model, vector, array).
     *
     */
    @Test
    public void testNullData() {
        try {
            new JXList<>((ListModel<?>) null);
            fail("JXList contructor must throw on null data");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("unexpected exception type " + e);
        }
        
        try {
           new JXList<>((Vector<?>) null);
            fail("JXList contructor must throw on null data");
        } catch (IllegalArgumentException e) {
            // expected
        } catch (Exception e) {
            fail("unexpected exception type " + e);
        }
        
        try {
            new JXList<Object>((Object[]) null);
             fail("JXList contructor must throw on null data");
         } catch (IllegalArgumentException e) {
             // expected
         } catch (Exception e) {
             fail("unexpected exception type " + e);
         }
    }
    
    
    /**
     * add and test comparator property.
     * 
     */
    @Test
    public void testComparator() {
        JXList<?> list = new JXList<>();
        assertNull(list.getComparator());
        Collator comparator = Collator.getInstance();
        PropertyChangeReport report = new PropertyChangeReport();
        list.addPropertyChangeListener(report);
        list.setComparator(comparator);
        assertEquals(comparator, list.getComparator());
        assertEquals(1, report.getEventCount());
        assertEquals(1, report.getEventCount("comparator"));
        
    }

    /**
     * test if LinkController/executeButtonAction is properly registered/unregistered on
     * setRolloverEnabled.
     *
     */
    @Test
    public void testLinkControllerListening() {
        JXList<?> table = new JXList<>();
        table.setRolloverEnabled(true);
        assertNotNull("LinkController must be listening", getLinkControllerAsPropertyChangeListener(table, RolloverProducer.CLICKED_KEY));
        assertNotNull("LinkController must be listening", getLinkControllerAsPropertyChangeListener(table, RolloverProducer.ROLLOVER_KEY));
        assertNotNull("execute button action must be registered", table.getActionMap().get(JXList.EXECUTE_BUTTON_ACTIONCOMMAND));
        table.setRolloverEnabled(false);
        assertNull("LinkController must not be listening", getLinkControllerAsPropertyChangeListener(table, RolloverProducer.CLICKED_KEY ));
        assertNull("LinkController must be listening", getLinkControllerAsPropertyChangeListener(table, RolloverProducer.ROLLOVER_KEY));
        assertNull("execute button action must be de-registered", table.getActionMap().get(JXList.EXECUTE_BUTTON_ACTIONCOMMAND));
    }

    private PropertyChangeListener getLinkControllerAsPropertyChangeListener(JXList<?> table, String propertyName) {
        PropertyChangeListener[] listeners = table.getPropertyChangeListeners(propertyName);
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof ListRolloverController<?>) {
                return (ListRolloverController<?>) listeners[i];
            }
        }
        return null;
    }


    protected DefaultListModel<?> createListModelWithLinks() {
        DefaultListModel<Object> model = new DefaultListModel<>();
        for (int i = 0; i < 20; i++) {
            try {
                LinkModel link = new LinkModel("a link text " + i, null, new URL("http://some.dummy.url" + i));
                if (i == 1) {
                    URL url = JXEditorPaneTest.class.getResource("resources/test.html");

                    link = new LinkModel("a resource", null, url);
                }
                model.addElement(link);
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
 
        return model;
    }

    /**
     * Creates and returns a StringValue which maps a Color to it's R/G/B rep, 
     * prepending "R/G/B: "
     * 
     * @return the StringValue for color.
     */
    private StringValue createColorStringValue() {
        @SuppressWarnings("serial")
		StringValue sv = new StringValue() {

            public String getString(Object value) {
                if (value instanceof Color) {
                    Color color = (Color) value;
                    return "R/G/B: " + color.getRGB();
                }
                return StringValues.TO_STRING.getString(value);
            }
            
        };
        return sv;
    }
    

    /**
     * Creates and returns a number filter, passing values which are numbers and
     * have int values inside or outside of the bounds (included), depending on the given 
     * flag.
     * 
     * @param lowerBound
     * @param upperBound
     * @param inside 
     * @return
     */
//    protected Filter createNumberFilter(final int lowerBound, final int upperBound, final boolean inside) {
//        PatternFilter f = new PatternFilter() {
//
//            @Override
//            public boolean test(int row) {
//                Object value = getInputValue(row, getColumnIndex());
//                if (!(value instanceof Number)) return false;
//                boolean isInside = ((Number) value).intValue() >= lowerBound 
//                    && ((Number) value).intValue() <= upperBound;
//                return inside ? isInside : !isInside;
//            }
//            
//        };
//        return f;
//    }
    

    
}
