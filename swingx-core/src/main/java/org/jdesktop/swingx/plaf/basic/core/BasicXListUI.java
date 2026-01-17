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
package org.jdesktop.swingx.plaf.basic.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.CellRendererPane;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.plaf.LookAndFeelUtils;
import org.jdesktop.swingx.plaf.basic.BasicYListUI;
import org.jdesktop.swingx.plaf.basic.core.DragRecognitionSupport.BeforeDrag;

/**
 * An extensible implementation of {@code ListUI} for JXList.
 * {@code BasicXListUI} instances cannot be shared between multiple lists.<p>
 * 
 * The heart of added functionality is to support sorting/filtering, that is keep 
 * model-selection and RowSorter state synchronized. The details are delegated to a ListSortUI, 
 * but this class is responsible to manage the sortUI on changes of list properties, model and 
 * view selection (same strategy as in JXTable).<p>
 * 
 * Note: this delegate is mostly a 1:1 copy of BasicListUI. The difference is that
 * it accesses the list elements and list elementCount exclusively through the 
 * JXList api. This allows a clean implementation of sorting/filtering.<p>
 * 
 * The differences (goal was to touch as little code as possible as this needs
 * to be updated on every change to core until that is changed to not access
 * the list's model directly, sigh) for core functionality:
 * <ul>
 * <li> extracted method for list.getModel().getSize (for the delegate class and 
 *      all contained static classes) and use that method exclusively
 * <li> similar for remaining list.getModel(): implemented wrapping listModel 
 *    which messages the list
 * <li> rename key for shared actionMap to keep core list actions separate 
 *    (just in case somebody wants both) - they point to the wrong delegate
 * <li> replaced references to SwingUtilities2 in sun packages by references to 
 *     pasted methods in SwingXUtilities
 * <li> replaced storage of shared Input/ActionMap in defaultLookup by direct
 *     storage in UIManager.         
 * </ul>
 * 
 * Differences to achieve extended functionality:
 * <ul>
 * <li> added methods to un/-installSortUI and call in un/installUI(component)
 * <li> changed PropertyChangeHandler to call a) hasHandledPropertyChange to 
 *  allow this class to replace super handler functinality and 
 *  b) updateSortUI after handling all not-sorter related properties.
 * <li> changed createPropertyChangeListener to return a PropertyChangeHandler
 * <li> changed ListDataHandler to check if event handled by SortUI and delegate
 *    to handler only if not
 * <li> changed createListDataListener to return a ListDataHandler
 * <li> changed ListSelectionHandler to check if event handled by SortUI and 
 *   delegate to handler only if not
 * </ul> changed createListSelectionListener to return a ListSelectionHandler
 * 
 * Differences for bug fixes (due to incorrectly extending super):
 * <ul>
 * <li> Issue #1495-swingx: getBaseline throughs NPE
 * </ul>
 * 
 * Note: extension of core (instead of implement from scratch) is to keep 
 * external (?) code working which expects a ui delegate of type BasicSomething.
 * LAF implementors with a custom ListUI extending BasicListUI should be able to
 * add support for JXList by adding a separate CustomXListUI extending this, same
 * as the default with parent changed. <b>Beware</b>: custom code must not 
 * call access the model directly or - if they insist - convert the row index to 
 * account for sorting/filtering! That's the whole point of this class.
 * 
 * @author Hans Muller
 * @author Philip Milne
 * @author Shannon Hickey (drag and drop)
 * @author EUG https://github.com/homebeaver (reorg)
 */
/*
JList hierarchie:
                 public abstract class javax.swing.plaf.ListUI extends ComponentUI
                                                          |
public class javax.swing.plaf.basic.BasicListUI extends ListUI

Instanziert wird die Klasse über die factory createUI, 
Die systematische/symetrische Ableitung wäre:

                                         ComponentUI
                                          |
YListUI ----------------- abstract class ListUI
 |                                        |
BasicYListUI               symetrisch zu BasicListUI
 |  |                                     |
 | BasicXListUI                           |
SynthYListUI               symetrisch zu SynthListUI

 */
public class BasicXListUI extends BasicYListUI {

    private static final Logger LOG = Logger.getLogger(BasicXListUI.class.getName());

    /**
     * Factory. Returns a new instance of BasicXListUI.  
     * BasicXListUI delegates are allocated one per JList.
     *
     * @param c JComponent
     * @return A new ListUI implementation for the Windows look and feel.
     */
    public static ComponentUI createUI(JComponent c) {
    	LOG.config("UI factory for JComponent:"+c);
        return new BasicXListUI(c);
    }

    // like in BasicListUI
    public static void loadActionMap(LazyActionMap map) {
    	BasicYListUI.loadActionMap(map);
    }

	public BasicXListUI(JComponent c) {
		super(c);
	}
	
	// vars in (super) BasicYListUI:
//  protected JList<Object> list = null; // defined in YListUI
//...

//-------------------- X-Wrapper
    
    private ListModel<Object> modelX;

    private ListSortUI sortUI;
    
    /**
     * Compatibility Wrapper: a synthetic model which delegates to list api and throws
     * @return ListModel
     */
	protected ListModel<Object> getViewModel() {
		if (modelX == null) {
			modelX = new ListModel<Object>() {

				@Override
				public int getSize() {
					if(list instanceof JXList<?>) {
						JXList<?> xlist = (JXList<?>)list;
						return xlist.getElementCount();
					}
					return list.getModel().getSize();
				}

				@Override
				public Object getElementAt(int index) {
					if(list instanceof JXList<?>) {
						JXList<?> xlist = (JXList<?>)list;
						return xlist.getElementAt(index);
					}
					return list.getModel().getElementAt(index);
				}

				@Override
				public void addListDataListener(ListDataListener l) {
					throw new UnsupportedOperationException("this is a synthetic model wrapper");
				}

				@Override
				public void removeListDataListener(ListDataListener l) {
					throw new UnsupportedOperationException("this is a synthetic model wrapper");

				}

			};
		}
		return modelX;
	}

    /**
     * @return no of elements
     */
    protected int getElementCount() {
    	if(list instanceof JXList<?>) {
			JXList<?> xlist = (JXList<?>)list;
    		return xlist.getElementCount();
    	}
    	return list.getModel().getSize();
    }

    /**
     * get Element At index <code>viewIndex</code>
     * @param viewIndex the index
     * @return Object
     */
    protected Object getElementAt(int viewIndex) {
		if(list instanceof JXList<?>) {
			JXList<?> xlist = (JXList<?>)list;
			return xlist.getElementAt(viewIndex);
		}
		return list.getModel().getElementAt(viewIndex);
    }


//--------------- api to support/control sorting/filtering
    
    /**
     * @return ListSortUI
     */
    protected ListSortUI getSortUI() {
        return sortUI;
    }
    
    /**
     * Installs SortUI if the list has a rowSorter. Does nothing if not.
     */
    protected void installSortUI() {
		if(list instanceof JXList<?>) {
			JXList<?> xlist = (JXList<?>)list;
	        if (xlist.getRowSorter() == null) return;
	        sortUI = new ListSortUI(xlist, xlist.getRowSorter());
		}
    }
    
    /**
     * Dispose and null's the sortUI if installed. Does nothing if not.
     */
    protected void uninstallSortUI() {
        if (sortUI == null) return;
        sortUI.dispose();
        sortUI = null;
    }
    
    /**
     * Called from the PropertyChangeHandler.
     * 
     * @param property the name of the changed property.
     */
    protected void updateSortUI(String property) {
        if ("rowSorter".equals(property)) {
            updateSortUIToRowSorterProperty();
        }
    }
    /**
     * 
     */
    private void updateSortUIToRowSorterProperty() {
        uninstallSortUI();
        installSortUI();
    }
    
    /**
     * Returns a boolean indicating whether or not the event has been processed
     * by the sortUI. 
     * @param e ListDataEvent
     * @return event has been processed
     */
    protected boolean processedBySortUI(ListDataEvent e) {
        if (sortUI == null)
            return false;
        sortUI.modelChanged(e);
        updateLayoutStateNeeded = modelChanged;
        redrawList();
        return true;
    }
    
    /**
     * Returns a boolean indicating whether or not the event has been processed
     * by the sortUI. 
     * @param e ListSelectionEvent
     * @return event has been processed
     */
    protected boolean processedBySortUI(ListSelectionEvent e) {
        if (sortUI == null) return false;
        sortUI.viewSelectionChanged(e);
        list.repaint();
        return true;
    }

//--------------------- enhanced support
    /**
     * Invalidates the cell size cache and revalidates/-paints the list.
     * 
     */
    public void invalidateCellSizeCache() {
        updateLayoutStateNeeded |= 1;
        redrawList();
    }

//---------------------  core copy

    /**
     * Registers the keyboard bindings on the <code>JList</code> that the
     * <code>BasicXListUI</code> is associated with. 
     * This method is called at installUI() time.
     *
     * @see #installUI
     */
    // modified code from javax.swing.plaf.basic.BasicListUI#installKeyboardActions
    protected void installKeyboardActions() {
        InputMap inputMap = getInputMap(JComponent.WHEN_FOCUSED);
        SwingUtilities.replaceUIInputMap(list, JComponent.WHEN_FOCUSED, inputMap);
        LazyActionMap.installLazyActionMap(list, BasicXListUI.class, "XList.actionMap");
    }

    // modified code from javax.swing.plaf.basic.BasicListUI#getInputMap
    // intentionally private
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_FOCUSED) {
            // PENDING JW: side-effect when reverting to ui manager? revisit!
            InputMap keyMap = (InputMap) UIManager.get("List.focusInputMap");
            InputMap rtlKeyMap;
            if (isLeftToRight || ((rtlKeyMap = (InputMap) UIManager.get("List.focusInputMap.RightToLeft")) == null)) {
                    return keyMap;
            } else {
                rtlKeyMap.setParent(keyMap);
                return rtlKeyMap;
            }
        }
        return null;
    }

    /**
     * Unregisters keyboard actions installed from
     * <code>installKeyboardActions</code>.
     * This method is called at uninstallUI() time - subclassess should
     * ensure that all of the keyboard actions registered at installUI
     * time are removed here.
     *
     * @see #installUI
     */
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(list, null);
        SwingUtilities.replaceUIInputMap(list, JComponent.WHEN_FOCUSED, null);
    }


    /**
     * Create and install the listeners for the JList, its model, and its
     * selectionModel.  This method is called at installUI() time.
     *
     * @see #installUI
     * @see #uninstallListeners
     */
    protected void installListeners() {
        TransferHandler th = list.getTransferHandler();
        if (th == null || th instanceof UIResource) {
            list.setTransferHandler(defaultTransferHandler);
            // default TransferHandler doesn't support drop
            // so we don't want drop handling
            if (list.getDropTarget() instanceof UIResource) {
                list.setDropTarget(null);
            }
        }

        focusListener = createFocusListener();
        mouseInputListener = createMouseInputListener();
        propertyChangeListener = createPropertyChangeListener();
        listSelectionListener = createListSelectionListener();
        listDataListener = createListDataListener();

        list.addFocusListener(focusListener);
        list.addMouseListener(mouseInputListener);
        list.addMouseMotionListener(mouseInputListener);
        list.addPropertyChangeListener(propertyChangeListener);
        list.addKeyListener(getHandler());
        // JW: here we really want the model
        ListModel<Object> model = list.getModel();
        if (model != null) {
            LOG.config("addListDataListener "+listDataListener + "\n for model "+model);
            model.addListDataListener(listDataListener);
        }

        ListSelectionModel selectionModel = list.getSelectionModel();
        if (selectionModel != null) {
            selectionModel.addListSelectionListener(listSelectionListener);
        }
    }


    /**
     * Remove the listeners for the JList, its model, and its
     * selectionModel.  All of the listener fields, are reset to
     * null here.  This method is called at uninstallUI() time,
     * it should be kept in sync with installListeners.
     *
     * @see #uninstallUI
     * @see #installListeners
     */
    protected void uninstallListeners() {
        list.removeFocusListener(focusListener);
        list.removeMouseListener(mouseInputListener);
        list.removeMouseMotionListener(mouseInputListener);
        list.removePropertyChangeListener(propertyChangeListener);
        list.removeKeyListener(getHandler());

        ListModel<Object> model = list.getModel();
        if (model != null) {
            model.removeListDataListener(listDataListener);
        }

        ListSelectionModel selectionModel = list.getSelectionModel();
        if (selectionModel != null) {
            selectionModel.removeListSelectionListener(listSelectionListener);
        }

        focusListener = null;
        mouseInputListener  = null;
        listSelectionListener = null;
        listDataListener = null;
        propertyChangeListener = null;
        handler = null;
    }


    /**
     * Initialize JList properties, e.g. font, foreground, and background,
     * and add the CellRendererPane.  The font, foreground, and background
     * properties are only set if their current value is either null
     * or a UIResource, other properties are set if the current
     * value is null.
     *
     * @see #uninstallDefaults
     * @see #installUI
     * @see CellRendererPane
     */
    protected void installDefaults() {
        list.setLayout(null);

        LookAndFeel.installBorder(list, "List.border");

        LookAndFeel.installColorsAndFont(list, "List.background", "List.foreground", "List.font");

        LookAndFeel.installProperty(list, "opaque", Boolean.TRUE);

        Color sbg = list.getSelectionBackground();
        if (sbg == null || sbg instanceof UIResource) {
            list.setSelectionBackground(UIManager.getColor("List.selectionBackground"));
        }

        Color sfg = list.getSelectionForeground();
        if (sfg == null || sfg instanceof UIResource) {
            list.setSelectionForeground(UIManager.getColor("List.selectionForeground"));
        }

        Long l = (Long)UIManager.get("List.timeFactor");
        timeFactor = (l!=null) ? l.longValue() : 1000L;

        updateIsFileList();
    }

    protected void updateIsFileList() {
        boolean b = Boolean.TRUE.equals(list.getClientProperty("List.isFileList"));
        if (b != isFileList) {
            isFileList = b;
            Font oldFont = list.getFont();
            if (oldFont == null || oldFont instanceof UIResource) {
                Font newFont = UIManager.getFont(b ? "FileChooser.listFont" : "List.font");
                if (newFont != null && newFont != oldFont) {
                    list.setFont(newFont);
                }
            }
        }
    }


    /**
     * Set the JList properties that haven't been explicitly overridden to
     * null.  A property is considered overridden if its current value
     * is not a UIResource.
     *
     * @see #installDefaults
     * @see #uninstallUI
     * @see CellRendererPane
     */
    protected void uninstallDefaults() {
        LookAndFeel.uninstallBorder(list);
        if (list.getFont() instanceof UIResource) {
            list.setFont(null);
        }
        if (list.getForeground() instanceof UIResource) {
            list.setForeground(null);
        }
        if (list.getBackground() instanceof UIResource) {
            list.setBackground(null);
        }
        if (list.getSelectionBackground() instanceof UIResource) {
            list.setSelectionBackground(null);
        }
        if (list.getSelectionForeground() instanceof UIResource) {
            list.setSelectionForeground(null);
        }
        if (list.getCellRenderer() instanceof UIResource) {
            list.setCellRenderer(null);
        }
        if (list.getTransferHandler() instanceof UIResource) {
            list.setTransferHandler(null);
        }
    }

    /**
     * {@inheritDoc} <p>
     * Also installs <code>ListSortUI()</code>
     */
    public void installUI(JComponent c) {
    	super.installUI(c);
        installSortUI();
    }

    /**
     * {@inheritDoc} <p>
     * Also uninstalls <code>ListSortUI()</code>
     */
    public void uninstallUI(JComponent c) {
        uninstallSortUI();
        super.uninstallUI(c);
    }

    /**
     * If updateLayoutStateNeeded is non zero, call updateLayoutState() and reset
     * updateLayoutStateNeeded.  This method should be called by methods
     * before doing any computation based on the geometry of the list.
     * For example it's the first call in paint() and getPreferredSize().
     *
     * @see #updateLayoutState
     */
    protected void maybeUpdateLayoutState()
    {
        if (updateLayoutStateNeeded != 0) {
            updateLayoutState();
            updateLayoutStateNeeded = 0;
        }
    }

    protected BasicYListUI.Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }

    @Override
    protected ListSelectionListener createListSelectionListener() {
        return new ListSelectionHandler() {
        	@Override
        	public void valueChanged(ListSelectionEvent e) {
        		if (processedBySortUI(e)) return;
        		getHandler().valueChanged(e);
        	}
        };
    }

    @Override
    protected ListDataListener createListDataListener() {
        return new ListDataHandler() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                if (processedBySortUI(e)) return;
                getHandler().intervalAdded(e);
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                if (processedBySortUI(e)) return;
                getHandler().intervalRemoved(e);
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                if (processedBySortUI(e)) return;
                getHandler().contentsChanged(e);
            }
        };
    }

    @Override
    protected PropertyChangeListener createPropertyChangeListener() {
        return new PropertyChangeHandler() {
            @Override
            public void propertyChange(PropertyChangeEvent e) {
                getHandler().propertyChange(e);
                updateSortUI(e.getPropertyName());
            }
        };
    }

    // PENDING JW: this is not a complete replacement of sun.UIAction ...
    protected static class Actions extends BasicYListUI.Actions {
        protected Actions(String name) {
            super(name);
        }
        protected int getElementCount(JList<?> list) {
			if(list instanceof JXList<?>) {
				JXList<?> xlist = (JXList<?>)list;
				return xlist.getElementCount();
			}
			return list.getModel().getSize();
		}

		public void actionPerformed(ActionEvent e) {
            String name = getName();
            @SuppressWarnings("unchecked")
            JList<Object> list = (JList<Object>)e.getSource();
            BasicXListUI ui = (BasicXListUI)LookAndFeelUtils.getUIOfType(list.getUI(), BasicXListUI.class);

            if (name == SELECT_PREVIOUS_COLUMN) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextColumnIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_PREVIOUS_COLUMN_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextColumnIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_PREVIOUS_COLUMN_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextColumnIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_NEXT_COLUMN) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextColumnIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_NEXT_COLUMN_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextColumnIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_NEXT_COLUMN_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextColumnIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_PREVIOUS_ROW) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_PREVIOUS_ROW_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_PREVIOUS_ROW_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextIndex(list, ui, -1), -1);
            }
            else if (name == SELECT_NEXT_ROW) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_NEXT_ROW_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_NEXT_ROW_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextIndex(list, ui, 1), 1);
            }
            else if (name == SELECT_FIRST_ROW) {
                changeSelection(list, CHANGE_SELECTION, 0, -1);
            }
            else if (name == SELECT_FIRST_ROW_EXTEND) {
                changeSelection(list, EXTEND_SELECTION, 0, -1);
            }
            else if (name == SELECT_FIRST_ROW_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD, 0, -1);
            }
            else if (name == SELECT_LAST_ROW) {
                changeSelection(list, CHANGE_SELECTION, getElementCount(list) - 1, 1);
            }
            else if (name == SELECT_LAST_ROW_EXTEND) {
                changeSelection(list, EXTEND_SELECTION, getElementCount(list) - 1, 1);
            }
            else if (name == SELECT_LAST_ROW_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD, getElementCount(list) - 1, 1);
            }
            else if (name == SCROLL_UP) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextPageIndex(list, -1), -1);
            }
            else if (name == SCROLL_UP_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextPageIndex(list, -1), -1);
            }
            else if (name == SCROLL_UP_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextPageIndex(list, -1), -1);
            }
            else if (name == SCROLL_DOWN) {
                changeSelection(list, CHANGE_SELECTION,
                                getNextPageIndex(list, 1), 1);
            }
            else if (name == SCROLL_DOWN_EXTEND) {
                changeSelection(list, EXTEND_SELECTION,
                                getNextPageIndex(list, 1), 1);
            }
            else if (name == SCROLL_DOWN_CHANGE_LEAD) {
                changeSelection(list, CHANGE_LEAD,
                                getNextPageIndex(list, 1), 1);
            }
            else if (name == SELECT_ALL) {
                selectAll(list);
            }
            else if (name == CLEAR_SELECTION) {
                clearSelection(list);
            }
            else if (name == ADD_TO_SELECTION) {
                int index = adjustIndex(
                    list.getSelectionModel().getLeadSelectionIndex(), list);

                if (!list.isSelectedIndex(index)) {
                    int oldAnchor = list.getSelectionModel().getAnchorSelectionIndex();
                    list.setValueIsAdjusting(true);
                    list.addSelectionInterval(index, index);
                    list.getSelectionModel().setAnchorSelectionIndex(oldAnchor);
                    list.setValueIsAdjusting(false);
                }
            }
            else if (name == TOGGLE_AND_ANCHOR) {
                int index = adjustIndex(
                    list.getSelectionModel().getLeadSelectionIndex(), list);

                if (list.isSelectedIndex(index)) {
                    list.removeSelectionInterval(index, index);
                } else {
                    list.addSelectionInterval(index, index);
                }
            }
            else if (name == EXTEND_TO) {
                changeSelection(
                    list, EXTEND_SELECTION,
                    adjustIndex(list.getSelectionModel().getLeadSelectionIndex(), list),
                    0);
            }
            else if (name == MOVE_SELECTION_TO) {
                changeSelection(
                    list, CHANGE_SELECTION,
                    adjustIndex(list.getSelectionModel().getLeadSelectionIndex(), list),
                    0);
            }
        }

        @Override
        public boolean isEnabled(Object c) {
        	return accept(c);
        }

    }

    protected class Handler extends BasicYListUI.Handler 
    implements FocusListener
    		 , KeyListener
    		 , ListDataListener
    		 , ListSelectionListener
    		 , MouseInputListener
    		 , PropertyChangeListener
    		 , BeforeDrag 
    {
        protected int getElementCount(JList<?> list) {
        	if(list instanceof JXList<?>) {
    			JXList<?> xlist = (JXList<?>)list;
        		return xlist.getElementCount();
        	}
        	return list.getModel().getSize();
        }
    }

    private static int adjustIndex(int index, JList<?> list) {
        return index < ((JXList<?>) list).getElementCount() ? index : -1;
    }

}
