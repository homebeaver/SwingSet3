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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.beans.BeanProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import javax.accessibility.Accessible;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.ComboPopup;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.UIDependent;
import org.jdesktop.swingx.plaf.XComboBoxAddon;
import org.jdesktop.swingx.plaf.XComboBoxUI;
import org.jdesktop.swingx.plaf.basic.BasicXComboBoxEditor;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.JRendererPanel;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.rollover.RolloverRenderer;
import org.jdesktop.swingx.sort.ListSortController;
import org.jdesktop.swingx.sort.SortController;
import org.jdesktop.swingx.sort.StringValueRegistry;
import org.jdesktop.swingx.util.Contract;

/**
 * An enhanced {@code JComboBox} that provides the following additional functionality:
 * <p>
 * Auto-starts edits correctly for AutoCompletion when inside a {@code JTable}. 
 * A normal {@code JComboBox} fails to recognize the first key stroke when it has been
 * {@link org.jdesktop.swingx.autocomplete.AutoCompleteDecorator#decorate(JComboBox) decorated}.
 * <p>
 * Adds highlighting support. <br>
 * User defined ComboBoxIcon. <br>
 * Add sort api: autoCreateRowSorter
 * 
 * @see org.jdesktop.swingx.JXList
 * 
 * @author Karl Schaefer
 * @author Jeanette Winzenburg
 * @author EUG https://github.com/homebeaver
 */
@SuppressWarnings("serial")
public class JXComboBox<E> extends JComboBox<E> {

    private static final Logger LOG = Logger.getLogger(JXComboBox.class.getName());

    static {
        LookAndFeelAddons.contribute(new XComboBoxAddon());
    }

    /**
     * UI Class ID
     * @see #getUIClassID
     * @see javax.swing.JComponent#readObject
     */
    // public, weil in XComboBoxAddon genutzt
    public static final String uiClassID = "XComboBoxUI";

    /**
     * Returns a string that specifies the name of the LaF class that renders this component.
     *
     * @return the string {@code uiClassID}
     * @see JComponent#getUIClassID
     * @see UIDefaults#getUI
     */
    @BeanProperty(bound = false)
    public String getUIClassID() {
        return uiClassID;
    }

    /**
     * A decorator for the original ListCellRenderer. 
     * Needed to hook highlighters after messaging the delegate.
     */
    public class DelegatingRenderer implements ListCellRenderer<E>, RolloverRenderer, UIDependent {
        /** the delegate. */
        private ListCellRenderer<? super E> delegateRenderer;
        private JRendererPanel wrapperPanel;

        /**
         * Instantiates a DelegatingRenderer with combo box's default renderer as delegate.
         */
        public DelegatingRenderer() {
            this(null);
        }
        
        /**
         * Instantiates a DelegatingRenderer with the given delegate. If the
         * delegate is {@code null}, the default is created via the combo box's factory method.
         * 
         * @param delegate the delegate to use, if {@code null} the combo box's default is
         *   created and used.
         */
        public DelegatingRenderer(ListCellRenderer<E> delegate) {
            wrapperPanel = new JRendererPanel(new BorderLayout());
            setDelegateRenderer(delegate);
        }

        /**
         * Sets the delegate. If the delegate is {@code null}, the default is created via the combo
         * box's factory method.
         * 
         * @param delegate
         *            the delegate to use, if null the list's default is created and used.
         */
        public void setDelegateRenderer(ListCellRenderer<? super E> delegate) {
            if (delegate == null) {
                delegate = createDefaultCellRenderer();
            }
            delegateRenderer = delegate;
        }

        /**
         * Returns the delegate.
         * 
         * @return the delegate renderer used by this renderer, guaranteed to not-null.
         */
        public ListCellRenderer<? super E> getDelegateRenderer() {
            return delegateRenderer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void updateUI() {
             wrapperPanel.updateUI();
             
             if (delegateRenderer instanceof UIDependent) {
            	 UIDependent uiDelegateRenderer = (UIDependent)delegateRenderer;
            	 uiDelegateRenderer.updateUI();
             } else if (delegateRenderer instanceof Component) {
            	 Component comp = (Component)delegateRenderer;
                 SwingUtilities.updateComponentTreeUI(comp);
             } else if (delegateRenderer != null) {
            	 // ListCellRenderer<? super E> delegateRenderer, dh superclass von E
                 try {
                	 // cast info
                	 // JList<? extends Object> getPopupListFor
                	 // interface : getListCellRendererComponent( JList<? extends E> list, E value, ...
                	 JList<? extends Object> lo = getPopupListFor(JXComboBox.this);
                	 @SuppressWarnings("unchecked")
					 JList<? extends E> list = (JList<? extends E>)lo;
                     Component comp = delegateRenderer.getListCellRendererComponent(list, null, -1, false, false);
                     SwingUtilities.updateComponentTreeUI(comp);
                 } catch (Exception e) {
                     // nothing to do - renderer barked on off-range row
                 }
             }
         }
         
         // --------- implement ListCellRenderer
        /**
         * {@inheritDoc} <p>
         * 
         * Overridden to apply the highlighters, if any, after calling the delegate.
         * The decorators are not applied if the row is invalid.
         */
        @Override
        public Component getListCellRendererComponent(JList<? extends E> list, E value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Component comp = null;

            if (index == -1) {
                comp = delegateRenderer.getListCellRendererComponent(list, value, getSelectedIndex(), isSelected, cellHasFocus);
                
                if (isUsingHighlightersForCurrentValue() && compoundHighlighter != null && getSelectedIndex() != -1) {
                    comp = compoundHighlighter.highlight(comp, getComponentAdapter(getSelectedIndex()));
                    
                    // this is done to "trick" BasicComboBoxUI.paintCurrentValue which resets all of
                    // the painted information after asking the list to render the value. the panel
                    // wrappers receives all of the post-rendering configuration, which is dutifully
                    // ignored by the real rendering component
                    wrapperPanel.add(comp);
                    comp = wrapperPanel;
                }
            } else {
                comp = delegateRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if ((compoundHighlighter != null) && (index >= 0) && (index < getItemCount())) {
                    comp = compoundHighlighter.highlight(comp, getComponentAdapter(index));
                }
            }

            return comp;
        }

        // implement RolloverRenderer
        
        /**
         * {@inheritDoc}
         * 
         */
        @Override
        public boolean isEnabled() {
            return (delegateRenderer instanceof RolloverRenderer) && 
               ((RolloverRenderer) delegateRenderer).isEnabled();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void doClick() {
            if (isEnabled()) {
                ((RolloverRenderer) delegateRenderer).doClick();
            }
        }
    }
    
    protected static class ComboBoxAdapter extends ComponentAdapter {
        private final JXComboBox<?> comboBox;

        /**
         * Constructs a <code>ListAdapter</code> for the specified target JXList.
         * 
         * @param component  the target list.
         */
        public ComboBoxAdapter(JXComboBox<?> component) {
            super(component);
            comboBox = component;
        }

        /**
         * Typesafe accessor for the target component.
         * 
         * @return the target component as a {@link org.jdesktop.swingx.JXComboBox}
         */
        public JXComboBox<?> getComboBox() {
            return comboBox;
        }

        /**
         * A safe way to access the combo box's popup visibility.
         * 
         * @return {@code true} if the popup is visible; {@code false} otherwise
         */
        protected boolean isPopupVisible() {
            if (comboBox.updatingUI) {
                return false;
            }
            
            return comboBox.isPopupVisible();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasFocus() {
            if (isPopupVisible()) {
                JList<? extends Object> list = getPopupListFor(comboBox);
                
                return list != null && list.isFocusOwner() && (row == list.getLeadSelectionIndex());
            }
            
            return comboBox.isFocusOwner();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getRowCount() {
            return comboBox.getModel().getSize();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValueAt(int row, int column) {
            return comboBox.getModel().getElementAt(row);
        }

        /**
         * {@inheritDoc}
         * This is implemented to query the table's StringValueRegistry for an appropriate
         * StringValue and use that for getting the string representation.
         */
        @Override
        public String getStringAt(int row, int column) {
            StringValue sv = comboBox.getStringValueRegistry().getStringValue(row, column);
            
            return sv.getString(getValueAt(row, column));
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Rectangle getCellBounds() {
            JList<? extends Object> list = getPopupListFor(comboBox);
            
            if (list == null) {
                assert false;
                return new Rectangle(comboBox.getSize());
            }

            return list.getCellBounds(row, row);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return row == -1 && comboBox.isEditable();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEditable() {
            return isCellEditable(row, column);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSelected() {
            if (isPopupVisible()) {
                JList<? extends Object> list = getPopupListFor(comboBox);
                
                return list != null && row == list.getLeadSelectionIndex();
            }
            
            return comboBox.isFocusOwner();
        }
    }
    
    class StringValueKeySelectionManager implements KeySelectionManager, Serializable, UIDependent {
        private long timeFactor;
        private long lastTime = 0L;
        private String prefix = "";
        private String typedString = "";
        
        public StringValueKeySelectionManager() {
            updateUI();
        }

        @Override
        public int selectionForKey(char aKey, ComboBoxModel<?> aModel) {
            if (lastTime == 0L) {
                prefix = "";
                typedString = "";
            }
            
            int startIndex = getSelectedIndex();
            
            if (EventQueue.getMostRecentEventTime() - lastTime < timeFactor) {
                typedString += aKey;
                if ((prefix.length() == 1) && (aKey == prefix.charAt(0))) {
                    // Subsequent same key presses move the keyboard focus to the next
                    // object that starts with the same letter.
                    startIndex++;
                } else {
                    prefix = typedString;
                }
            } else {
                startIndex++;
                typedString = "" + aKey;
                prefix = typedString;
            }
            
            lastTime = EventQueue.getMostRecentEventTime();

            if (startIndex < 0 || startIndex >= aModel.getSize()) {
                startIndex = 0;
            }
            
            for (int i = startIndex, c = aModel.getSize(); i < c; i++) {
                String v = getStringAt(i).toLowerCase();
                
                if (v.length() > 0 && v.charAt(0) == aKey) {
                    return i;
                }
            }
            
            for (int i = startIndex, c = aModel.getSize(); i < c; i++) {
                String v = getStringAt(i).toLowerCase();
                
                if (v.length() > 0 && v.charAt(0) == aKey) {
                    return i;
                }
            }

            for (int i = 0; i < startIndex; i++) {
                String v = getStringAt(i).toLowerCase();
                
                if (v.length() > 0 && v.charAt(0) == aKey) {
                    return i;
                }
            }
            
            return -1;
        }

        @Override
        public void updateUI() {
            Long l = (Long) UIManager.get("ComboBox.timeFactor");
            timeFactor = l == null ? 1000L : l.longValue();
        }
    }

    /**
     * Returns the PopupList of a comboBox.
     * @param comboBox JComboBox
     * @return JList of Objects
     */
    protected static JList<? extends Object> getPopupListFor(JComboBox<? extends Object> comboBox) {
        int count = comboBox.getUI().getAccessibleChildrenCount(comboBox);

        for (int i = 0; i < count; i++) {
            Accessible a = comboBox.getUI().getAccessibleChild(comboBox, i);
            
            // interface ComboPopup with method public JList<Object> getList()
            if (a instanceof ComboPopup) {
//            	LOG.info("ComboPopup:"+a);
            	ComboPopup popup = (ComboPopup)a;
                return popup.getList();
            }
        }

        return null;
    }

    private ComboBoxAdapter dataAdapter;
    
    private DelegatingRenderer delegatingRenderer;
    
    private StringValueRegistry stringValueRegistry;

    private boolean usingHighlightersForCurrentValue = true;
    
    /**
     * The pipeline holding the highlighters.
     */
    private CompoundHighlighter compoundHighlighter;
    /** listening to changeEvents from compoundHighlighter. */
    private ChangeListener highlighterChangeListener;

    private List<KeyEvent> pendingEvents;

    private boolean isDispatching;

    private boolean updatingUI;

/*
Vergleich ctor JXList JYList und JXComboBox :

()           ()       ()       JXComboBox wie JYList
this(false); super(); super();

(boolean autoCreateRowSorter)                       // existiert in JYList nicht => auch nicht in JXComboBox
(ListModel<E> model, boolean autoCreateRowSorter)   // dto
(JXList(E[] items, boolean autoCreateRowSorter)     // dto
(Vector<E> items, boolean autoCreateRowSorter)      // dto

(ListModel<E> model)    | (ListModel<E> model)              | (ComboBoxModel<E> model) // JXComboBox wie JYList
this(model, false);       super(model);                       super(model);

(E[] items)             | (final E[] items)                 | (E[] items)              // JXComboBox wie JYList
this(items, false);

JXList(Vector<E> items) | (final Vector<? extends E> items) | (Vector<E> items)        // JXComboBox wie JYList
this(items, false);

Daraus folgt: JXComboBox müsste eigentlich JYComboBox heissen, da es keine sortiere items unterstützt
Es geht aber um die popup liste, und die ist in BasicXComboBoxUI.popup bzw in BasicXComboBoxUI.listBox definiert:
	protected JList<Object> listBox; // actually of subtype JXList
    	popup = createPopup();
    	listBox = popup.getList();

 */
    
    /**
     * Creates a {@code JXComboBox} with a default data model. 
     * The default data model is an empty list of objects. Use {@code addItem} to add items.
     * <p> 
     * By default the first item in the data model becomes selected.
     * 
     * @see DefaultComboBoxModel
     */
    public JXComboBox() {
        this(false);
    }
    /**
     * Creates a {@code JXComboBox} with a default data model. 
     * @param autoCreateRowSorter {@code boolean} to determine if a {@code RowSorter} should be created automatically.
     * @see #JXComboBox()
     */
    public JXComboBox(boolean autoCreateRowSorter) {
        super();
        init();
        if(autoCreateRowSorter) {
        	setRowSorter(createDefaultRowSorter(), SortOrder.ASCENDING);
        } else {
            setRowSorter(null, null);
        }
    }

    /**
     * Creates a {@code JXComboBox} that takes its items from an existing {@code ComboBoxModel}.
     * Since the {@code ComboBoxModel} is provided, a combo box created using this constructor 
     * does not create a default combo box model and may impact how the insert, remove and add methods behave.
     * 
     * @param model
     *            the {@code ComboBoxModel} that provides the displayed list of items
     * @see ComboBoxModel
     */
    public JXComboBox(ComboBoxModel<E> model) {
        this(model, false);
    }
    /**
     * Creates a {@code JXComboBox} that takes its items from an existing {@code ComboBoxModel}.
     * 
     * @param model
     *            the {@code ComboBoxModel} that provides the displayed list of items
     * @param autoCreateRowSorter {@code boolean} to determine if a {@code RowSorter} should be created automatically.
     * @see #JXComboBox(ComboBoxModel)
     */
    public JXComboBox(ComboBoxModel<E> model, boolean autoCreateRowSorter) {
        super(model);
        init();
        if(autoCreateRowSorter) {
        	setRowSorter(createDefaultRowSorter(), SortOrder.ASCENDING);
        } else {
            setRowSorter(null, null);
        }
    }

    /**
     * Creates a {@code JXComboBox} that contains the elements in the specified array. 
     * By default the first item in the array (and therefore the data model) becomes selected.
     * 
     * @param items
     *            an array of objects to insert into the combo box
     * @see DefaultComboBoxModel
     */
    public JXComboBox(E[] items) {
        this(items, false);
    }
    /**
     * Creates a {@code JXComboBox} that contains the elements in the specified array. 
     * If autoCreateRowSorter is set the first item in the array becomes selected,
     * this element may not be the first shown in the dropdown box.
     * 
     * @param items an array of objects to insert into the combo box
     * @param autoCreateRowSorter {@code boolean} to determine if a {@code RowSorter} should be created automatically.
     */
    public JXComboBox(E[] items, boolean autoCreateRowSorter) {
        super(items);
        init();
        if(autoCreateRowSorter) {
        	setRowSorter(createDefaultRowSorter(), SortOrder.ASCENDING);
        } else {
            setRowSorter(null, null);
        }
    }

    /**
     * Creates a {@code JXComboBox} that contains the elements in the specified Vector. 
     * By default the first item in the vector (and therefore the data model) becomes selected.
     * 
     * @param items {@code Vector} with elements to insert into the combo box
     */
    public JXComboBox(Vector<E> items) {
        this(items, false);
    }
    public JXComboBox(Vector<E> items, boolean autoCreateRowSorter) {
        super(items);
        init();
        if(autoCreateRowSorter) {
        	setRowSorter(createDefaultRowSorter(), SortOrder.ASCENDING);
        } else {
            setRowSorter(null, null);
        }
    }

    private void init() {
        pendingEvents = new ArrayList<KeyEvent>();
        
        if (keySelectionManager == null || keySelectionManager instanceof UIResource) {
            setKeySelectionManager(createDefaultKeySelectionManager());
        }
        ComboBoxModel<E> m = getModel();
        /* z.B.
         * StringValue sv = (Object value) -> {
         *   return value==null ? "" : value.toString();
         * };
         * jxComboBox.setRenderer(new DefaultListRenderer<Object>(sv));
         */
        Object si = m.getSelectedItem();
        if(si!=null && (si.getClass()==String.class || si.getClass()==Integer.class)) {
        	LOG.config("set DefaultListRenderer for "+si.getClass()+" StringValue");
        	StringValue sv = (Object value) -> {
        		return value==null ? "" : value.toString();
        	};
        	setRenderer(new DefaultListRenderer<Object>(sv));
        }
        updateUI(); // wg. BUG 66
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses the {@code StringValue} representation of the elements to determine
     * the selected item.
     */
    @Override
    protected KeySelectionManager createDefaultKeySelectionManager() {
        return new StringValueKeySelectionManager();
    }
    
    /*
     * {@inheritDoc} from JComponent
     * Overrides method in JCompobox
     * 
     * liefert true wenn es eine aktive zuordnung zu einer Aktion für die Taste gibt
     */
    /**
     * Invoked to process the key bindings for {@code keyStroke} as the result of the {@code KeyEvent} {@code e}. 
     * This obtains the appropriate {@code InputMap},
     * gets the binding, 
     * gets the action from the {@code ActionMap},
     * and then (if the action is found and the component is enabled) invokes {@code notifyAction}.
     *
     * @param keyStroke the {@code keyStroke} queried
     * @param e the {@code KeyEvent}
     * @param condition one of the following values:
     * <ul>
     * <li>JComponent.WHEN_FOCUSED = 0
     * <li>JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT= 1
     * <li>JComponent.WHEN_IN_FOCUSED_WINDOW = 2
     * </ul>
     * @param pressed true if the key is pressed
     * @return true if there was a binding to an action, and the action was enabled
     */
    @Override
    protected boolean processKeyBinding(KeyStroke keyStroke, final KeyEvent e, int condition, boolean pressed) {
        boolean retValue = super.processKeyBinding(keyStroke, e, condition, pressed);
//    	System.out.println("JXComboBox.processKeyBinding superMethod("+keyStroke
//    			+", KeyEvent="+KeyEvent.getKeyText(e.getKeyCode())
//    			+", condition="+condition
//    			+", the key is "+(pressed?"pressed":"released")
//    		+")\n returns "+retValue);

        if (!retValue && editor != null) {
            if (isStartingCellEdit(e)) {
                pendingEvents.add(e);
            } else if (pendingEvents.size() == 2) {
                pendingEvents.add(e);
                isDispatching = true;

                SwingUtilities.invokeLater( () -> {
                    try {
                        for (KeyEvent event : pendingEvents) {
                            editor.getEditorComponent().dispatchEvent(event);
                        }

                        pendingEvents.clear();
                    } finally {
                        isDispatching = false;
                    }
                });
            }
        }
        return retValue;
    }

    private boolean isStartingCellEdit(KeyEvent e) {
        if (isDispatching) {
            return false;
        }

        JTable table = (JTable) SwingUtilities.getAncestorOfClass(JTable.class, this);
        boolean isOwned = table != null
                && !Boolean.FALSE.equals(table.getClientProperty("JTable.autoStartsEdit"));

        return isOwned && e.getComponent() == table;
    }

    /**
     * @return the unconfigured ComponentAdapter.
     */
    protected ComponentAdapter getComponentAdapter() {
        if (dataAdapter == null) {
            dataAdapter = new ComboBoxAdapter(this);
        }
        return dataAdapter;
    }

    /**
     * Returns the StringValueRegistry which defines the string representation for
     * each cells. This is strictly for internal use by the table, which has the 
     * responsibility to keep in synch with registered renderers.
     * <p>
     * Currently exposed for testing reasons, client code is recommended to not use nor override.
     * 
     * @return the current string value registry
     */
    protected StringValueRegistry getStringValueRegistry() {
        if (stringValueRegistry == null) {
            stringValueRegistry = createDefaultStringValueRegistry();
        }
        return stringValueRegistry;
    }

    /**
     * Creates and returns the default registry for StringValues.
     * 
     * @return the default registry for StringValues.
     */
    protected StringValueRegistry createDefaultStringValueRegistry() {
        return new StringValueRegistry();
    }
    
    /**
	 * Convenience to access a configured ComponentAdapter.
	 * Note: the column index of the configured adapter is always 0.
	 * 
	 * @param index the row index in view coordinates, must be valid.
	 * @return the configured ComponentAdapter.
	 */
	protected ComponentAdapter getComponentAdapter(int index) {
	    ComponentAdapter adapter = getComponentAdapter();
	    adapter.column = 0;
	    adapter.row = index;
	    return adapter;
	}
	/**
     * Returns the string representation of the cell value at the given model position. 
     * 
     * @param row the row index of the item in model coordinates
     * @return the string representation of the item value as it will appear in the combo box. 
     */
    public String getStringAt(int row) {
        // changed implementation to use StringValueRegistry
        StringValue stringValue = getStringValueRegistry().getStringValue(row, 0);
        
        return stringValue.getString(getItemAt(row));
    }

    private DelegatingRenderer getDelegatingRenderer() {
        if (delegatingRenderer == null) {
            // only called once... to get hold of the default?
            delegatingRenderer = new DelegatingRenderer();
        }
        return delegatingRenderer;
    }

    /**
     * Creates and returns the default cell renderer to use. 
     * Subclasses may override to use a different type. Here: returns a {@code DefaultListRenderer}.
     * 
     * @return the default cell renderer to use with this list.
     */
    protected ListCellRenderer<E> createDefaultCellRenderer() {
        return new DefaultListRenderer<E>();
    }

    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to return the delegating renderer which is wrapped around the
     * original to support highlighting. The returned renderer is of type 
     * DelegatingRenderer and guaranteed to not-null
     * 
     * @see #setRenderer(ListCellRenderer)
     * @see DelegatingRenderer
     */
    @Override
    public ListCellRenderer<? super E> getRenderer() {
        return super.getRenderer();
    }

    /**
     * Returns the renderer installed by client code or the default if none has been set.
     * <p>
     * This is a shortcut for 
     * <code>((JXComboBox.DelegatingRenderer)getRenderer()).getDelegateRenderer())</code>
     * 
     * @return the wrapped renderer.
     * @see #setRenderer(ListCellRenderer)
     */
    public ListCellRenderer<?> getWrappedRenderer() {
        return getDelegatingRenderer().getDelegateRenderer();
    }

    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to wrap the given renderer in a DelegatingRenderer to support highlighting.
     *  <p>
     * Note: the wrapping implies that the renderer returned from the getCellRenderer
     * is <b>not</b> the renderer as given here, but the wrapper. 
     * To access the original, use {@code getWrappedCellRenderer}.
     * 
     * @see #getWrappedRenderer()
     * @see #getRenderer()
     */
    /* z.B.
     * StringValue sv = (Object value) -> {
     *   return value==null ? "" : value.toString();
     * };
     * jxComboBox.setRenderer(new DefaultListRenderer<Object>(sv));
     */
    @Override
    public void setRenderer(ListCellRenderer<? super E> renderer) {
        // PENDING: do something against recursive setting
        // == multiple delegation...
        getDelegatingRenderer().setDelegateRenderer(renderer);
        getStringValueRegistry().setStringValue(renderer instanceof StringValue ? (StringValue) renderer : null, 0);
        super.setRenderer(delegatingRenderer);
    }

    /**
     * @return {@code true} if the combo box decorates the current value with highlighters; 
     * 	{@code false} otherwise
     */
    public boolean isUsingHighlightersForCurrentValue() {
        return usingHighlightersForCurrentValue;
    }
    
    /**
     * @param newValue for usingHighlightersForCurrentValue boolean property
     */
    public void setUseHighlightersForCurrentValue(boolean newValue) {
        boolean oldValue = isUsingHighlightersForCurrentValue();
        this.usingHighlightersForCurrentValue = newValue;
        repaint();
        firePropertyChange("useHighlightersForCurrentValue", oldValue,
                isUsingHighlightersForCurrentValue());
    }
    
    /**
     * Sets the {@code Highlighter}s to the dropdown list, replacing any old settings. 
     * None of the given Highlighters must be null.
     * 
     * @param highlighters
     *            zero or more not null highlighters to use for renderer decoration.
     * 
     * @see #getHighlighters()
     * @see #addHighlighter(Highlighter)
     * @see #removeHighlighter(Highlighter)
     * 
     */
    public void setHighlighters(Highlighter... highlighters) {
        Contract.asNotNull(highlighters, "highlighters cannot be null or contain null");
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().setHighlighters(highlighters);
        firePropertyChange("highlighters", old, getHighlighters());
    }

    /**
     * Returns the {@code Highlighter}s used by the dropdown list. 
     * Maybe empty, but guarantees to be never null.
     * 
     * @return the Highlighters used by this combobox.
     * @see #setHighlighters(Highlighter[])
     */
    public Highlighter[] getHighlighters() {
        return getCompoundHighlighter().getHighlighters();
    }

    /**
     * Appends a {@code Highlighter} to the end of the list of used {@code Highlighter}s. 
     * The argument must not be null. 
     * 
     * @param highlighter the {@code Highlighter} to add.
     * @throws NullPointerException if {@code Highlighter} is null.
     * 
     * @see #removeHighlighter(Highlighter)
     * @see #setHighlighters(Highlighter[])
     */
    public void addHighlighter(Highlighter highlighter) {
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().addHighlighter(highlighter);
        firePropertyChange("highlighters", old, getHighlighters());
    }

    /**
     * Removes the given Highlighter. <p>
     * Does nothing if the Highlighter is not contained.
     * 
     * @param highlighter the Highlighter to remove.
     * @see #addHighlighter(Highlighter)
     * @see #setHighlighters(Highlighter...)
     */
    public void removeHighlighter(Highlighter highlighter) {
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().removeHighlighter(highlighter);
        firePropertyChange("highlighters", old, getHighlighters());
    }
    
    /**
     * Returns the CompoundHighlighter assigned to the combo box, null if none. 
     * 
     * @return the CompoundHighlighter assigned to the table.
     */
//  * @see #setCompoundHighlighter(CompoundHighlighter)
    protected CompoundHighlighter getCompoundHighlighter() {
        if (compoundHighlighter == null) {
            compoundHighlighter = new CompoundHighlighter();
            compoundHighlighter.addChangeListener(getHighlighterChangeListener());
        }
        return compoundHighlighter;
    }

    /**
     * Returns the <code>ChangeListener</code> to use with {@code Highlighter}. 
     * Lazily creates the listener.
     * 
     * @return the ChangeListener for observing changes of highlighters, 
     *   guaranteed to be <code>not-null</code>
     */
    protected ChangeListener getHighlighterChangeListener() {
        if (highlighterChangeListener == null) {
            highlighterChangeListener = createHighlighterChangeListener();
        }
        return highlighterChangeListener;
    }

    /**
     * Creates and returns the ChangeListener observing Highlighters.
     * <p>
     * A property change event is create for a state change.
     * 
     * @return the ChangeListener defining the reaction to changes of highlighters.
     */
    protected ChangeListener createHighlighterChangeListener() {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // need to fire change so JXComboBox can update
                firePropertyChange("highlighters", null, getHighlighters());
                repaint();
            }
        };
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Overridden to update renderer
     */
    @Override
    public void updateUI() {
        updatingUI = true;
        try {
        	// expected UIClass: ComboBoxUI
        	ComponentUI ui = LookAndFeelAddons.getUI(this, ComboBoxUI.class);
        	setUI((ComboBoxUI)ui);
            
            if (keySelectionManager instanceof UIDependent) {
            	UIDependent uiKeySelectionManager = (UIDependent)keySelectionManager;
            	uiKeySelectionManager.updateUI();
            }
            
            ListCellRenderer<? super E> renderer = getRenderer();        
            if (renderer instanceof UIDependent) {
            	UIDependent uiRenderer = (UIDependent)renderer;
            	uiRenderer.updateUI();
            } else if (renderer instanceof Component) {
            	Component comp = (Component)renderer;
                SwingUtilities.updateComponentTreeUI(comp);
            }
            
            if (compoundHighlighter != null) {
                compoundHighlighter.updateUI();
            }
        } finally {
            updatingUI = false;
        }
    }
    // ----------- ab hier meine Erweiterungen :
/* uncomment for logging
    @Override
    protected void installAncestorListener() {
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent event) { 
            	LOG.info("hidePopup when Added Ancestor "+event.getAncestor());
            	hidePopup();
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent event) { 
            	LOG.info("hidePopup when Removed Ancestor "+event.getAncestor());
            	hidePopup();
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent event) {
                if (event.getSource() != JXComboBox.this) {
                	LOG.info("hidePopup when Moved Ancestor "+event.getAncestor());
                	hidePopup();
                }
            }
        });
    }
 */

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUI(ComboBoxUI newUI) {
    	if(ui==newUI) return;
    	
        ComponentUI oldUI = ui;
        ui = newUI;
        if (ui != null) {
            ui.installUI(this); // calls BasicXComboBoxUI#installUI, evtl via MetalXComboBoxUI resp. SynthXComboBoxUI#installUI
        }
        firePropertyChange("UI", oldUI, newUI);
        revalidate();
        repaint();
    }
    // in JComboBox gibt es setEditor(ComboBoxEditor anEditor) das hier nicht überschrieben wird
/*
    @BeanProperty(expert = true, description
            = "The editor that combo box uses to edit the current value")
    public void setEditor(ComboBoxEditor anEditor) {
        ComboBoxEditor oldEditor = editor;

        if ( editor != null ) {
            editor.removeActionListener(this);
        }
        editor = anEditor;
        if ( editor != null ) {
            editor.addActionListener(this);
        }
        firePropertyChange( "editor", oldEditor, editor );
    }    
 */
    // in JComboBox : return editor;
    @Override
    public ComboBoxEditor getEditor() {
    	if(editor==null) {
    			/*
    protected JTextField createEditorComponent() {
    // BorderlessTextField is not visible
        JTextField editor = new BorderlessTextField("",9);
        editor.setBorder(null);
        return editor;
    }
    			 */
//    		editor = new BasicComboBoxEditor() {
//    			@Override
//    			protected JTextField createEditorComponent() {
//    				JTextField txtEditor = new JTextField(null, "", 9);// Document doc, String text, int columns
//    				return txtEditor;
//    			}
//    		};
    		editor = new BasicXComboBoxEditor();
    	}
        return editor;
    }    
    public void setSelectedItem(Object anObject) {
//    	System.out.println("JXComboBox.setSelectedItem to anObject="+anObject);
// BUG in JComboBox Z.603 getEditor liefert null
//    	getEditor().setItem(anObject);

    	super.setSelectedItem(anObject);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Overridden to respect sorted Dropdown List
     * 
     * @param anIndex an integer specifying the list item to select (for sorted lists not coercively the model index),
     *                  where 0 specifies the first item in the list and -1 indicates no selection
     */
    public void setSelectedIndex(int anIndex) {
		int modelIndex = anIndex;
    	if(anIndex>-1 && hasRowSorter()) {
    		modelIndex = getRowSorter().convertRowIndexToModel(anIndex);
        	LOG.config("index="+anIndex+" new modelIndex="+modelIndex);
    	}
    	super.setSelectedIndex(modelIndex);
    }
    public void setComboBoxIcon(Icon icon) {
    	setComboBoxIcon(icon, icon);
    }
    public void setComboBoxIcon(Icon icon, Icon isShowingPopupIcon) {
		XComboBoxUI ui = (XComboBoxUI)getUI();
    	ui.uninstallButton();
    	ui.installButton(icon);
    	ui.setIsShowingPopupIcon(isShowingPopupIcon);
    	this.repaint();
    }
    @Override
    public ComboBoxUI getUI() {
        return (XComboBoxUI)super.getUI();
    }
    
    private Color selectionBackground;
    public Color getSelectionBackground() {
        return selectionBackground;
    }
    /* @beaninfo
     *       bound: true
     *   attribute: visualUpdate true
     * description: The background color of selected cells.
     */
    public void setSelectionBackground(Color selectionBackground) {
        Object oldValue = getSelectionBackground();
        this.selectionBackground = selectionBackground;
        firePropertyChange("selectionBackground", oldValue, getSelectionBackground());
        repaint();
    }

    private Color selectionForeground;
    public Color getSelectionForeground() {
        return selectionForeground;
    }
    /* @beaninfo
     *       bound: true
     *   attribute: visualUpdate true
     * description: The foreground color of selected cells.
     */
    public void setSelectionForeground(Color selectionForeground) {
        Object oldValue = getSelectionForeground();
        this.selectionForeground = selectionForeground;
        firePropertyChange("selectionForeground", oldValue, getSelectionForeground());
        repaint();
    }

    /* sort api: siehe JXList
     * abstract class javax.swing.RowSorter<M> beinhaltet eine Liste SortKeys.
     * Im allgemeinen Fall werden Tabellen sortiert, daher werden mehrere sort keys benötigt.
     * Pro SortSpalte ein SortKey. Siehe public static class RowSorter.SortKey
     * SortKey Attribute : int column und SortOrder sortOrder {ASCENDING, DESCENDING, UNSORTED}
     * Die ComboBox Klappliste ist einspaltig, der column index also immer 0.
     * Mit getRowSorter().getSortKeys().get(0).getSortOrder() bekommen wir das enum SortOrder.
     * Die Elemente im Klapplistenmodell (eine Liste) werden im RowSorter nicht umsortiert.
     * Sie behalten ihre Position in der Liste. Zwei Methoden liefern die ZeilenSortierung
     *   int convertRowIndexToView(int index)
     *   int convertRowIndexToModel(int index)
     * bzw. das Inverse.
     * 
     * Es gib keinen public setter für rowSorter! 
     * Im ctor kann die Sortierung der Klappliste gewählt werden, param autoCreateRowSorter.
     * Ist rowSorter erstmal gesetzt, so kann er nicht mehr auf null zurückgesetzt werden!
     * Mit xcb.setSortOrder(SortOrder.UNSORTED) wird die Klappliste im urprünglichen Zustand angezeigt. 
     */
    private RowSorter<? extends ListModel<E>> rowSorter;
    /*
     * liefert true, wenn autoCreateRowSorter im ctor angefordert wurde
     * oder wenn rowSorter mit setSortOrder(SortOrder so) , so!=SortOrder.UNSORTED erstellt wurde.
     * in JXList heisst diese Methode hasSortController ==> auch nicht optimal, da es ein UI-Element suggeriert
     */
    public boolean hasRowSorter() {
    	return rowSorter != null;
    }
    public RowSorter<? extends ListModel<E>> getRowSorter() {
        return rowSorter;
    }
    // DefaultRowSorter ist ListSortController <== es braucht keinen Controller, nur Model
    // ==> die Namen Controller in ListSortController und DefaultSortController sind irritierend: 
    // Es sind keine UI elemente 
    protected RowSorter<? extends ListModel<E>> createDefaultRowSorter() {
        return new ListSortController<ListModel<E>>(getModel());
    }
    // in JXList ist diese Methode public und ohne SortOrder param
    protected void setRowSorter(RowSorter<? extends ListModel<E>> sorter, SortOrder so) {
        RowSorter<? extends ListModel<E>> oldRowSorter = getRowSorter();
        this.rowSorter = sorter;
        
        //configureSorterProperties:
        if(getRowSorter()!=null) {
    		RowSorter.SortKey sk = new RowSorter.SortKey(0, so);
    		getRowSorter().setSortKeys(Arrays.asList(sk));
        }

        firePropertyChange("rowSorter", oldRowSorter, getRowSorter());
    }
    public SortOrder getSortOrder() {
    	if(!hasRowSorter()) return null; // NO rowSorter ==> NO SortOrder
    	// getRowSorter() != null ==>
    	return getRowSorter().getSortKeys().get(0).getSortOrder();
    }
    public void setSortOrder(SortOrder so) {
    	if(hasRowSorter()) {
    		SortOrder old = getRowSorter().getSortKeys().get(0).getSortOrder();
    		// unchanged:
    		if(old==so || (so==null && old==SortOrder.UNSORTED)) return; 
    		// changed:	
    		if(so==null || so==SortOrder.UNSORTED) {
    			getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(0, SortOrder.UNSORTED)));
    			return;
    		}
    		if(old==SortOrder.UNSORTED && so!=SortOrder.UNSORTED) {
    			getRowSorter().setSortKeys(Arrays.asList(new RowSorter.SortKey(0, so)));
    			return;
    		}
    		toggleSortOrder();
    	} else {
    		setRowSorter(createDefaultRowSorter(), so==null ? SortOrder.UNSORTED : so);
    	}
    }
    // Comparator to be passed to a sort function
    private Comparator<?> comparator;
    public Comparator<?> getComparator() {
        return comparator;
    }
    public void setComparator(Comparator<?> comparator) {
        Comparator<?> old = getComparator();
        this.comparator = comparator;
        updateSortAfterComparatorChange();
        firePropertyChange("comparator", old, getComparator());
    }
    protected void updateSortAfterComparatorChange() {
        if(getRowSorter() instanceof SortController<?>) {
            getSortController().setComparator(0, getComparator());
        }
    }
    @SuppressWarnings("unchecked")
	// JW: the RowSorter is always of type <? extends ListModel> so the unchecked cast is safe
	protected SortController<? extends ListModel<Object>> getSortController() {
    	if(getRowSorter() instanceof SortController<?>) {
    		return (SortController<? extends ListModel<Object>>) getRowSorter();
    	}
        return null;
    }
    public void toggleSortOrder() {
    	if(getRowSorter() instanceof SortController<?>) {
            getSortController().toggleSortOrder(0);
    	}
    }
}
