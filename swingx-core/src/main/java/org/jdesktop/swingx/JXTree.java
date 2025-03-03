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
package org.jdesktop.swingx;

import java.applet.Applet; // Applet has been deprecated since version 9
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.CellEditor;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.text.Position.Bias;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.UIAction;
import org.jdesktop.swingx.plaf.UIDependent;
import org.jdesktop.swingx.plaf.XTreeAddon;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.StringValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.jdesktop.swingx.renderer.WrappingProvider;
import org.jdesktop.swingx.rollover.RolloverProducer;
import org.jdesktop.swingx.rollover.RolloverRenderer;
import org.jdesktop.swingx.rollover.TreeRolloverController;
import org.jdesktop.swingx.rollover.TreeRolloverProducer;
import org.jdesktop.swingx.search.SearchFactory;
import org.jdesktop.swingx.search.Searchable;
import org.jdesktop.swingx.search.TreeSearchable;
import org.jdesktop.swingx.tree.DefaultXTreeCellEditor;
import org.jdesktop.swingx.tree.DefaultXTreeCellRenderer;

/**
 * Enhanced Tree component with support for SwingX rendering, highlighting,
 * rollover and search functionality.
 * 
 * <h2>Rendering and Highlighting</h2>
 * 
 * As all SwingX collection views, a JXTree is a HighlighterClient (PENDING JW:
 * formally define and implement, like in AbstractTestHighlighter), that is it
 * provides consistent api to add and remove Highlighters which can visually
 * decorate the rendering component.
 * 
 * <pre>
 * <code>
 * 
 * JXTree tree = new JXTree(new FileSystemModel());
 * // use system file icons and name to render
 * tree.setCellRenderer(new DefaultTreeRenderer(IconValues.FILE_ICON, 
 *      StringValues.FILE_NAME));
 * // highlight condition: file modified after a date     
 * HighlightPredicate predicate = new HighlightPredicate() {
 *    public boolean isHighlighted(Component renderer,
 *                     ComponentAdapter adapter) {
 *       File file = getUserObject(adapter.getValue());
 *       return file != null ? lastWeek &lt; file.lastModified : false;
 *    }
 * };
 * // highlight with foreground color 
 * tree.addHighlighter(new ColorHighlighter(predicate, null, Color.RED);      
 * 
 * </code>
 * </pre>
 * 
 * <i>Note:</i> for full functionality, a DefaultTreeRenderer must be installed
 * as TreeCellRenderer. This is not done by default, because there are
 * unresolved issues when editing. PENDING JW: still? Check!
 * 
 * <i>Note:</i> to support the highlighting this implementation wraps the
 * TreeCellRenderer set by client code with a DelegatingRenderer which applies
 * the Highlighter after delegating the default configuration to the wrappee. As
 * a side-effect, getCellRenderer does return the wrapper instead of the custom
 * renderer. To access the latter, client code must call getWrappedCellRenderer.
 *
 * <h2>Rollover</h2>
 * 
 * As all SwingX collection views, a JXTree supports per-cell rollover. If
 * enabled, the component fires rollover events on enter/exit of a cell which by
 * default is promoted to the renderer if it implements RolloverRenderer, that
 * is simulates live behaviour. The rollover events can be used by client code
 * as well, f.i. to decorate the rollover row using a Highlighter.
 * 
 * <pre>
 * <code>
 * 
 * JXTree tree = new JXTree();
 * tree.setRolloverEnabled(true);
 * tree.setCellRenderer(new DefaultTreeRenderer());
 * tree.addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, 
 *      null, Color.RED);      
 * 
 * </code>
 * </pre>
 * 
 * <h2>Location of Trigger for ComponentPopupMenu</h2>
 * 
 * JXList allows access to the mouse location that triggered the showing of the
 * componentPopupMenu. This feature allows to implement dynamic cell-context
 * sensitive popupMenus, either in the menu actions or in a PopupMenuListener.
 * <p>
 * 
 * The example below selects the cell that was clicked, event being the
 * <code>PopupMenuEvent</code> received in a <code>PopupMenuListener</code>.
 * 
 * <pre>
 * <code>
 * JXTree tree = (JXTree) ((JPopupMenu) e.getSource()).getInvoker();
 * Point trigger = tree.getPopupTriggerLocation();
 * if (trigger != null) {
 *     int row = tree.getRowForLocation(trigger.x, trigger.y);
 *     tree.setSelectionRow(row);
 * }
 * </code>
 * </pre>
 * 
 * 
 * <h2>Search</h2>
 * 
 * As all SwingX collection views, a JXTree is searchable. A search action is
 * registered in its ActionMap under the key "find". The default behaviour is to
 * ask the SearchFactory to open a search component on this component. The
 * default keybinding is retrieved from the SearchFactory, typically ctrl-f (or
 * cmd-f for Mac). Client code can register custom actions and/or bindings as
 * appropriate.
 * <p>
 * 
 * JXTree provides api to vend a renderer-controlled String representation of
 * cell content. This allows the Searchable and Highlighters to use WYSIWYM
 * (What-You-See-Is-What-You-Match), that is pattern matching against the actual
 * string as seen by the user.
 * 
 * <h2>Miscellaneous</h2>
 * 
 * <ul>
 * <li>Improved usability for editing: guarantees that the tree is the
 * focusOwner if editing terminated by user gesture and guards against data
 * corruption if focusLost while editing
 * <li>Access methods for selection colors, for consistency with JXTable, JXList
 * <li>Convenience methods and actions to expand, collapse all nodes
 * </ul>
 * 
 * @author Ramesh Gupta
 * @author Jeanette Winzenburg
 * 
 * @see org.jdesktop.swingx.renderer.DefaultTreeRenderer
 * @see org.jdesktop.swingx.renderer.ComponentProvider
 * @see org.jdesktop.swingx.decorator.Highlighter
 * @see org.jdesktop.swingx.decorator.HighlightPredicate
 * @see org.jdesktop.swingx.search.SearchFactory
 * @see org.jdesktop.swingx.search.Searchable
 * 
 */
@SuppressWarnings("serial")
@JavaBean
public class JXTree extends JTree {

    private static final Logger LOG = Logger.getLogger(JXTree.class.getName());

    static {
        LookAndFeelAddons.contribute(new XTreeAddon());
    }

    /**
     * UI Class ID
     * @see #getUIClassID
     * @see javax.swing.JComponent#readObject
     */
    public final static String uiClassID = "XTreeUI";

    /** Empty int array used in getSelectedRows(). */
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    /** Empty TreePath used in getSelectedPath() if selection empty. */
    private static final TreePath[] EMPTY_TREEPATH_ARRAY = new TreePath[0];

    /** Collection of active Highlighters. */
    protected CompoundHighlighter compoundHighlighter;
    /** Listener to changes of Highlighters in collection. */
    private ChangeListener highlighterChangeListener;

    /** Wrapper around the installed renderer, needed to support Highlighters. */
    private DelegatingRenderer delegatingRenderer;

    /**
     * The RolloverProducer used if rollover is enabled.
     */
    private RolloverProducer rolloverProducer;

    /**
     * The RolloverController used if rollover is enabled.
     */
    private TreeRolloverController<JXTree> linkController;
    
    private boolean overwriteIcons;
    private Searchable searchable;
    
    // hacks around core focus issues around editing.
    /**
     * The propertyChangeListener responsible for terminating
     * edits if focus lost.
     */
    private CellEditorRemover editorRemover;
    /**
     * The CellEditorListener responsible to force the 
     * focus back to the tree after terminating edits.
     */
    private CellEditorListener editorListener;
    
    /** Color of selected foreground. Added for consistent api across collection components. */
    private Color selectionForeground;
    /** Color of selected background. Added for consistent api across collection components. */
    private Color selectionBackground;


    private Point popupTriggerLocation;
    
    
    
    /**
     * Constructs a <code>JXTree</code> with a sample model. The default model
     * used by this tree defines a leaf node as any node without children.
     */
    public JXTree() {
        init();
    }

    /**
     * Constructs a <code>JXTree</code> with each element of the specified array
     * as the child of a new root node which is not displayed. By default, this
     * tree defines a leaf node as any node without children.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param value an array of objects that are children of the root.
     */
    public JXTree(Object[] value) {
        super(value);
        init();
    }

    /**
     * Constructs a <code>JXTree</code> with each element of the specified
     * Vector as the child of a new root node which is not displayed.
     * By default, this tree defines a leaf node as any node without children.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param value an Vector of objects that are children of the root.
     */
    public JXTree(Vector<?> value) {
        super(value);
        init();
    }

    /**
     * Constructs a <code>JXTree</code> created from a Hashtable which does not
     * display with root. Each value-half of the key/value pairs in the HashTable
     * becomes a child of the new root node. By default, the tree defines a leaf
     * node as any node without children.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param value a Hashtable containing objects that are children of the root.
     */
    public JXTree(Hashtable<?, ?> value) {
        super(value);
        init();
    }

    /**
     * Constructs a <code>JXTree</code> with the specified TreeNode as its root,
     * which displays the root node. By default, the tree defines a leaf node as
     * any node without children.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param root root node of this tree
     */
    public JXTree(TreeNode root) {
        super(root, false);
        init();
    }

    /**
     * Constructs a <code>JXTree</code> with the specified TreeNode as its root,
     * which displays the root node and which decides whether a node is a leaf
     * node in the specified manner.
     *
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     *
     * @param root root node of this tree
     * @param asksAllowsChildren if true, only nodes that do not allow children
     * are leaf nodes; otherwise, any node without children is a leaf node;
     * @see javax.swing.tree.DefaultTreeModel#asksAllowsChildren
     */
    public JXTree(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
        init();
    }

    /**
     * Constructs an instance of <code>JXTree</code> which displays the root
     * node -- the tree is created using the specified data model.
     * 
     * This version of the constructor simply invokes the super class version
     * with the same arguments.
     * 
     * @param newModel
     *            the <code>TreeModel</code> to use as the data model
     */
    public JXTree(TreeModel newModel) {
        super(newModel);
        init();
    }

    /**
     * Instantiates JXTree state which is new compared to super. Installs the
     * Delegating renderer and editor, registers actions and keybindings.
     * 
     * This must be called from each constructor.
     */
    private void init() {
        // Issue #1061-swingx: renderer inconsistencies
        // force setting of renderer
        setCellRenderer(createDefaultCellRenderer());
        // Issue #233-swingx: default editor not bidi-compliant 
        // manually install an enhanced TreeCellEditor which 
        // behaves slightly better in RtoL orientation.
        // Issue #231-swingx: icons lost
        // Anyway, need to install the editor manually because
        // the default install in BasicTreeUI doesn't know about
        // the DelegatingRenderer and therefore can't see
        // the DefaultTreeCellRenderer type to delegate to. 
        // As a consequence, the icons are lost in the default setup.
        //
        // JW PENDING need to mimic ui-delegate default re-set?
        // JW PENDING alternatively, cleanup and use DefaultXXTreeCellEditor in incubator
        if (getWrappedCellRenderer() instanceof DefaultTreeCellRenderer) {
            setCellEditor(new DefaultXTreeCellEditor(this, (DefaultTreeCellRenderer) getWrappedCellRenderer()));
        }
        // Register the actions that this class can handle.
        ActionMap map = getActionMap();
        map.put("expand-all", new Actions("expand-all"));
        map.put("collapse-all", new Actions("collapse-all"));
        map.put("find", createFindAction());

        KeyStroke findStroke = SearchFactory.getInstance().getSearchAccelerator();
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(findStroke, "find");
    }

    /**
     * Listens to the model and updates the {@code expandedState} accordingly
     * when nodes are removed, or changed.
     * <p>
     * This class will expand an invisible root when a child has been added to
     * it.
     * 
     * @author Karl George Schaefer
     */
    protected class XTreeModelHandler extends TreeModelHandler {
        /**
         * {@inheritDoc}
         */
        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            TreePath path = e.getTreePath();
            
            //fixes SwingX bug #612
            if (path.getParentPath() == null && !isRootVisible() && isCollapsed(path)) {
                //should this be wrapped in SwingUtilities.invokeLater? XXX
                expandPath(path);
            }
            
            super.treeNodesInserted(e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected TreeModelListener createTreeModelListener() {
        return new XTreeModelHandler();
    }

    /**
     * A small class which dispatches actions.
     * TODO: Is there a way that we can make this static?
     */
    private class Actions extends UIAction {
        Actions(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            if ("expand-all".equals(getName())) {
                expandAll();
            }
            else if ("collapse-all".equals(getName())) {
                collapseAll();
            }
        }
    }


//-------------------- search support
    
    /**
     * Creates and returns the action to invoke on a find request.
     * 
     * @return the action to invoke on a find request.
     */
    private Action createFindAction() {
        return new UIAction("find") {
            @Override
            public void actionPerformed(ActionEvent e) {
                doFind();
            }
        };
    }

    /**
     * Starts a search on this Tree's visible nodes. This implementation asks the
     * SearchFactory to open a find widget on itself.
     */
    protected void doFind() {
        SearchFactory.getInstance().showFindInput(this, getSearchable());
    }

    /**
     * Returns a Searchable for this component, guaranteed to be not null. This 
     * implementation lazily creates a TreeSearchable if necessary.
     *  
     * 
     * @return a not-null Searchable for this component.
     * 
     * @see #setSearchable(Searchable)
     * @see org.jdesktop.swingx.search.TreeSearchable
     */
    public Searchable getSearchable() {
        if (searchable == null) {
            searchable = new TreeSearchable(this);
        }
        return searchable;
    }

    /**
     * Sets the Searchable for this component. If null, a default 
     * Searchable will be created and used.
     * 
     * @param searchable the Searchable to use for this component, may be null to 
     *   indicate using the default.
     * 
     * @see #getSearchable()
     */
    public void setSearchable(Searchable searchable) {
        this.searchable = searchable;
    }
    
    /**
     * Returns the string representation of the cell value at the given position. 
     * 
     * @param row the row index of the cell in view coordinates
     * @return the string representation of the cell value as it will appear in the 
     *   table. 
     */
    public String getStringAt(int row) {
        return getStringAt(getPathForRow(row));
    }

    /**
     * Returns the string representation of the cell value at the given position. 
     * 
     * @param path the TreePath representing the node.
     * @return the string representation of the cell value as it will appear 
     * in the tree or table, or null if the path is not visible. 
     */
    public String getStringAt(TreePath path) {
        if (path == null) return null;
        TreeCellRenderer renderer = getDelegatingRenderer().getDelegateRenderer();
        if (renderer instanceof StringValue) {
        	StringValue sv = (StringValue)renderer;
            return sv.getString(path.getLastPathComponent());
        }
        // else: (to satisfy Test ComponentAdapterTest.testTreeTableGetStringAtHiddenHierarchicalColumn)
        Object lpc = path.getLastPathComponent();
        if(lpc instanceof JComponent) {
        	JComponent comp = (JComponent)lpc;
            return comp.getName();
        }
        // else:
        return StringValues.TO_STRING.getString(lpc);
    }

    
    /**
     * Overridden to respect the string representation, if any. This takes over 
     * completely (as compared to super), internally messaging the Searchable.
     * <p>
     * 
     * PENDING JW: re-visit once we support deep node search.
     * 
     */
    @Override
    public TreePath getNextMatch(String prefix, int startingRow, Bias bias) {
        Pattern pattern = Pattern.compile("^" + prefix, Pattern.CASE_INSENSITIVE);
        int row = getSearchable().search(pattern, startingRow, bias ==Bias.Backward);
        return getPathForRow(row);
    }

//--------------------- misc. new api and super overrides
    /**
     * Collapses all nodes in this tree.
     */
    public void collapseAll() {
        for (int i = getRowCount() - 1; i >= 0 ; i--) {
            collapseRow(i);
        }
    }

    /**
     * Expands all nodes in this tree.<p>
     * 
     * Note: it's not recommended to use this method on the EDT for large/deep trees
     * because expansion can take a considerable amount of time. 
     */
    public void expandAll() {
        if (getRowCount() == 0) {
            expandRoot();
        }
        for (int i = 0; i < getRowCount(); i++) {
            expandRow(i);
        }
    }

    /**
     * Expands the root path if a TreeModel has been set, does nothing if not.
     * 
     */
    private void expandRoot() {
        TreeModel model = getModel();
        if (model != null && model.getRoot() != null) {
            expandPath(new TreePath(model.getRoot()));
        }
    }

    /**
     * Sets the selection model.
     * 
     * @param selectionMode mode which must be one of 
     * TreeSelectionModel.SINGLE_TREE_SELECTION,
     * TreeSelectionModel.CONTIGUOUS_TREE_SELECTION or TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION.
     * 
     * @see javax.swing.tree.TreeSelectionModel#setSelectionMode
     */
    public void setSelectionMode(int selectionMode) {
    	getSelectionModel().setSelectionMode(selectionMode);
    }
    /**
     * Returns the current selection model.
     * 
     * @return the selection model.
     * 
     * @see javax.swing.tree.TreeSelectionModel#getSelectionMode
     */
    public int getSelectionMode() {
        return getSelectionModel().getSelectionMode();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * 
     * Overridden to always return a not-null array (following SwingX convention).
     */
    @Override
    public int[] getSelectionRows() {
        int[] rows = super.getSelectionRows();
        return rows != null ? rows : EMPTY_INT_ARRAY; 
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * 
     * Overridden to always return a not-null array (following SwingX
     * convention).
     */
    @Override
    public TreePath[] getSelectionPaths() {
        TreePath[] paths = super.getSelectionPaths();
        return paths != null ? paths : EMPTY_TREEPATH_ARRAY; 
    }

    /**
     * Returns the background color for selected cells.
     *
     * @return the <code>Color</code> used for the background of
     * selected list items
     * @see #setSelectionBackground
     * @see #setSelectionForeground
     */
    public Color getSelectionBackground() {
        return selectionBackground;
    }
    
    /**
     * Returns the selection foreground color.
     *
     * @return the <code>Color</code> object for the foreground property
     * @see #setSelectionForeground
     * @see #setSelectionBackground
     */
    public Color getSelectionForeground() {
        return selectionForeground;
    }
   
    /**
     * Sets the foreground color for selected cells.  Cell renderers
     * can use this color to render text and graphics for selected
     * cells.
     * <p>
     * The default value of this property is defined by the look
     * and feel implementation.
     * <p>
     * This is a JavaBeans bound property.
     *
     * @param selectionForeground  the <code>Color</code> to use in the foreground
     *                             for selected list items
     * @see #getSelectionForeground
     * @see #setSelectionBackground
     * @see #setForeground
     * @see #setBackground
     * @see #setFont
     */
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

    /**
     * Sets the background color for selected cells.  Cell renderers
     * can use this color to the fill selected cells.
     * <p>
     * The default value of this property is defined by the look
     * and feel implementation.
     * <p>
     * This is a JavaBeans bound property.
     *
     * @param selectionBackground  the <code>Color</code> to use for the 
     *                             background of selected cells
     * @see #getSelectionBackground
     * @see #setSelectionForeground
     * @see #setForeground
     * @see #setBackground
     * @see #setFont
     */
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
   
//------------------------- update ui 
    
    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to update selection background/foreground. Mimicking behaviour of 
     * ui-delegates for JTable, JList.
     */
    @Override
    public void updateUI() {
        uninstallSelectionColors();
        super.updateUI();
        installSelectionColors();
        updateHighlighterUI();
        updateRendererEditorUI();
        invalidateCellSizeCache();
    }

    
    /**
     * Quick fix for #1060-swingx: icons lost on toggling LAF
     */
    protected void updateRendererEditorUI() {
        if (getCellEditor() instanceof UIDependent) {
            ((UIDependent) getCellEditor()).updateUI();
        }
        // PENDING JW: here we get the DelegationRenderer which is not (yet) UIDependent
        // need to think about how to handle the per-tree icons
        // anyway, the "real" renderer usually is updated accidentally 
        // don't know exactly why, added to the comp hierarchy?
//        if (getCellRenderer() instanceof UIDependent) {
//            ((UIDependent) getCellRenderer()).updateUI();
//        }
    }

    /**
     * Installs selection colors from UIManager. <p>
     * 
     * <b>Note:</b> this should be done in the UI delegate.
     */
    private void installSelectionColors() {
        if (SwingXUtilities.isUIInstallable(getSelectionBackground())) {
            setSelectionBackground(UIManager.getColor("Tree.selectionBackground"));
        }
        if (SwingXUtilities.isUIInstallable(getSelectionForeground())) {
            setSelectionForeground(UIManager.getColor("Tree.selectionForeground"));
        }
        
    }

    /**
     * Uninstalls selection colors. <p>
     * 
     * <b>Note:</b> this should be done in the UI delegate.
     */
    private void uninstallSelectionColors() {
        if (SwingXUtilities.isUIInstallable(getSelectionBackground())) {
            setSelectionBackground(null);
        }
        if (SwingXUtilities.isUIInstallable(getSelectionForeground())) {
            setSelectionForeground(null);
        }
    }

    /**
     * Updates highlighter after <code>updateUI</code> changes.
     * 
     * @see org.jdesktop.swingx.plaf.UIDependent
     */
    protected void updateHighlighterUI() {
        if (compoundHighlighter == null) return;
        compoundHighlighter.updateUI();
    }



//------------------------ Rollover support
    
    /**
     * Sets the property to enable/disable rollover support. If enabled, the tree
     * fires property changes on per-cell mouse rollover state, i.e. 
     * when the mouse enters/leaves a tree cell. <p>
     * 
     * This can be enabled to show "live" rollover behaviour, f.i. the cursor over a cell 
     * rendered by a JXHyperlink.<p>
     * 
     * The default value is false.
     * 
     * @param rolloverEnabled a boolean indicating whether or not the rollover
     *   functionality should be enabled.
     * 
     * @see #isRolloverEnabled()
     * @see #getLinkController()
     * @see #createRolloverProducer()
     * @see org.jdesktop.swingx.rollover.RolloverRenderer  
     */
    public void setRolloverEnabled(boolean rolloverEnabled) {
        boolean old = isRolloverEnabled();
        if (rolloverEnabled == old) return;
        if (rolloverEnabled) {
            rolloverProducer = createRolloverProducer();
            rolloverProducer.install(this);
            getLinkController().install(this);
        } else {
            rolloverProducer.release(this);
            rolloverProducer = null;
            getLinkController().release();
        }
        firePropertyChange("rolloverEnabled", old, isRolloverEnabled());
    }

    /**
     * Returns a boolean indicating whether or not rollover support is enabled. 
     *
     * @return a boolean indicating whether or not rollover support is enabled. 
     * 
     * @see #setRolloverEnabled(boolean)
     */
    public boolean isRolloverEnabled() {
        return rolloverProducer != null;
    }
    
    /**
     * Returns the RolloverController for this component. Lazyly creates the 
     * controller if necessary, that is the return value is guaranteed to be 
     * not null. <p>
     * 
     * PENDING JW: rename to getRolloverController
     * 
     * @return the RolloverController for this tree, guaranteed to be not null.
     * 
     * @see #setRolloverEnabled(boolean)
     * @see #createLinkController()
     * @see org.jdesktop.swingx.rollover.RolloverController
     */
    protected TreeRolloverController<JXTree> getLinkController() {
        if (linkController == null) {
            linkController = createLinkController();
        }
        return linkController;
    }

    /**
     * Creates and returns a RolloverController appropriate for this tree.
     * 
     * @return a RolloverController appropriate for this tree.
     * 
     * @see #getLinkController()
     * @see org.jdesktop.swingx.rollover.RolloverController
     */
    protected TreeRolloverController<JXTree> createLinkController() {
        return new TreeRolloverController<JXTree>();
    }

    /**
     * Creates and returns the RolloverProducer to use with this tree.
     * <p>
     * 
     * @return <code>RolloverProducer</code> to use with this tree
     * 
     * @see #setRolloverEnabled(boolean)
     */
    protected RolloverProducer createRolloverProducer() {
        return new TreeRolloverProducer();
    }

//---------------------- enhanced component popup support
    
    /**
     * {@inheritDoc} <p>
     * 
     * Overridden for bookkeeping: the given event location is 
     * stored for later access.
     * 
     * @see #getPopupTriggerLocation()
     */
    @Override
    public Point getPopupLocation(MouseEvent event) {
        updatePopupTrigger(event);
        return super.getPopupLocation(event);
    }
    
    /**
     * Handles internal bookkeeping related to popupLocation, called from 
     * getPopupLocation.<p>
     * 
     * This implementation stores the mouse location as popupTriggerLocation.
     * 
     * @param event the event that triggered the showing of the 
     * componentPopup, might be null if triggered by keyboard
     */
    protected void updatePopupTrigger(MouseEvent event) {
        Point old = getPopupTriggerLocation();
        // note: getPoint creates a new Point on each call, safe to use as-is
        popupTriggerLocation = event != null ? event.getPoint() : null;
        firePropertyChange("popupTriggerLocation", old, getPopupTriggerLocation());
    }

    /**
     * Returns the location of the mouseEvent that triggered the
     * showing of the ComponentPopupMenu. 
     * 
     * @return the location of the mouseEvent that triggered the
     * last showing of the ComponentPopup, or null if it was
     * triggered by keyboard.
     */
    public Point getPopupTriggerLocation() {
        return popupTriggerLocation != null ? new Point(popupTriggerLocation) : null;
    }
    
    
  
//----------------------- Highlighter api
    
    /**
     * Sets the <code>Highlighter</code>s to the tree, replacing any old settings.
     * None of the given Highlighters must be null.<p>
     * 
     * This is a bound property. <p> 
     * 
     * Note: as of version #1.257 the null constraint is enforced strictly. 
     * To remove all highlighters use this method without param.
     * 
     * @param highlighters zero or more not null highlighters to use for renderer decoration.
     * @throws NullPointerException if array is null or array contains null values.
     * 
     * @see #getHighlighters()
     * @see #addHighlighter(Highlighter)
     * @see #removeHighlighter(Highlighter)
     * 
     */
    public void setHighlighters(Highlighter... highlighters) {
        Highlighter[] old = getHighlighters();
        getCompoundHighlighter().setHighlighters(highlighters);
        firePropertyChange("highlighters", old, getHighlighters());
    }

    /**
     * Returns the <code>Highlighter</code>s used by this tree.
     * Maybe empty, but guarantees to be never null.
     * 
     * @return the Highlighters used by this tree, guaranteed to never null.
     * @see #setHighlighters(Highlighter[])
     */
    public Highlighter[] getHighlighters() {
        return getCompoundHighlighter().getHighlighters();
    }
    
    /**
     * Appends a <code>Highlighter</code> to the end of the list of used <code>Highlighter</code>s. 
     * The argument must not be null. <p>
     * 
     * @param highlighter the <code>Highlighter</code> to add, must not be null.
     * @throws NullPointerException if <code>Highlighter</code> is null.
     * 
     * @see #removeHighlighter(Highlighter)
     * @see #setHighlighters(Highlighter[])
     */
    public void addHighlighter(Highlighter highlighter) {
        Highlighter[] old = getHighlighters();
        LOG.config("# of Highlighters="+old.length + " add "+highlighter);
        getCompoundHighlighter().addHighlighter(highlighter);
        firePropertyChange("highlighters", old, getHighlighters());
    }

    /**
     * Removes the given Highlighter. <p>
     * 
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
     * Returns the CompoundHighlighter assigned to the tree, null if none.
     * PENDING: open up for subclasses again?.
     * 
     * @return the CompoundHighlighter assigned to the tree.
     */
    protected CompoundHighlighter getCompoundHighlighter() {
        if (compoundHighlighter == null) {
            compoundHighlighter = new CompoundHighlighter();
            compoundHighlighter.addChangeListener(getHighlighterChangeListener());
        }
        return compoundHighlighter;
    }

    /**
     * Returns the <code>ChangeListener</code> to use with highlighters. Lazily creates the listener.
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
     * Here: repaints the tree on receiving a stateChanged.
     * 
     * @return the ChangeListener defining the reaction to changes of highlighters.
     */
    protected ChangeListener createHighlighterChangeListener() {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                repaint();
            }
        };
    }
    
    /**
     * Sets the Icon to use for the handle of an expanded node.<p>
     * 
     * Note: this will only succeed if the current ui delegate is
     * a BasicTreeUI otherwise it will do nothing.<p>
     * 
     * PENDING JW: incomplete api (no getter) and not a bound property.
     * 
     * @param expandedIcon the Icon to use for the handle of an expanded node.
     */
    public void setExpandedIcon(Icon expandedIcon) {
        if (getUI() instanceof BasicTreeUI) {
            ((BasicTreeUI) getUI()).setExpandedIcon(expandedIcon);
        }
    }
    
    /**
     * Sets the Icon to use for the handle of a collapsed node.
     * 
     * Note: this will only succeed if the current ui delegate is
     * a BasicTreeUI otherwise it will do nothing.
     *  
     * PENDING JW: incomplete api (no getter) and not a bound property.
     * 
     * @param collapsedIcon the Icon to use for the handle of a collapsed node.
     */
    public void setCollapsedIcon(Icon collapsedIcon) {
        if (getUI() instanceof BasicTreeUI) {
            ((BasicTreeUI) getUI()).setCollapsedIcon(collapsedIcon);
        }
    }
    
    /**
     * Sets the Icon to use for a leaf node.<p>
     * 
     * Note: this will only succeed if current renderer is a 
     * DefaultTreeCellRenderer.<p>
     * 
     * PENDING JW: this (all setXXIcon) is old api pulled up from the JXTreeTable. 
     * Need to review if we really want it - problematic if sharing the same
     * renderer instance across different trees.
     * 
     * PENDING JW: incomplete api (no getter) and not a bound property.<p>
     * 
     * @param leafIcon the Icon to use for a leaf node.
     */
    public void setLeafIcon(Icon leafIcon) {
        getDelegatingRenderer().setLeafIcon(leafIcon);
    }
    
    /**
     * Sets the Icon to use for an open folder node.
     * 
     * Note: this will only succeed if current renderer is a 
     * DefaultTreeCellRenderer.
     * 
     * PENDING JW: incomplete api (no getter) and not a bound property.
     * 
     * @param openIcon the Icon to use for an open folder node.
     */
    public void setOpenIcon(Icon openIcon) {
        getDelegatingRenderer().setOpenIcon(openIcon);
    }
    
    /**
     * Sets the Icon to use for a closed folder node.
     * 
     * Note: this will only succeed if current renderer is a 
     * DefaultTreeCellRenderer.
     * 
     * PENDING JW: incomplete api (no getter) and not a bound property.
     * 
     * @param closedIcon the Icon to use for a closed folder node.
     */
    public void setClosedIcon(Icon closedIcon) {
        getDelegatingRenderer().setClosedIcon(closedIcon);
    }
    
    /* from JComponent
     * Sets the background color of this component. 
     *  
     * The background color is used only if the component is opaque, 
     * and only by subclasses of <code>JComponent</code> or
     * <code>ComponentUI</code> implementations.
     * 
     * Direct subclasses of
     * <code>JComponent</code> must override
     * <code>paintComponent</code> to honor this property.
     * <p>
     * It is up to the look and feel to honor this property, some may choose to ignore it.
     *
     * @param bg the desired background <code>Color</code>
     * @see java.awt.Component#getBackground
     * @see #setOpaque
     */
    public void setBackground(Color bg) {
//    	if(!this.isOpaque()) {
//        	LOG.warning("????XTREEE isOpaque="+this.isOpaque()+" The background color is used only if the component is opaque, ... "+bg);
//    	}
    	super.setBackground(bg);
    }

    /**
     * Property to control whether per-tree icons should be 
     * copied to the renderer on setCellRenderer. <p>
     * 
     * The default value is false.
     * 
     * PENDING: should update the current renderer's icons when 
     * setting to true?
     * 
     * @param overwrite a boolean to indicate if the per-tree Icons should
     *   be copied to the new renderer on setCellRenderer.
     * 
     * @see #isOverwriteRendererIcons()  
     * @see #setLeafIcon(Icon)
     * @see #setOpenIcon(Icon)
     * @see #setClosedIcon(Icon)  
     */
    public void setOverwriteRendererIcons(boolean overwrite) {
        if (overwriteIcons == overwrite) return;
        boolean old = overwriteIcons;
        this.overwriteIcons = overwrite;
        firePropertyChange("overwriteRendererIcons", old, overwrite);
    }

    /**
     * Returns a boolean indicating whether the per-tree icons should be 
     * copied to the renderer on setCellRenderer.
     * 
     * @return true if a TreeCellRenderer's icons will be overwritten with the
     *   tree's Icons, false if the renderer's icons will be unchanged.
     *   
     * @see #setOverwriteRendererIcons(boolean)
     * @see #setLeafIcon(Icon)
     * @see #setOpenIcon(Icon)
     * @see #setClosedIcon(Icon)  
     *     
     */
    public boolean isOverwriteRendererIcons() {
        return overwriteIcons;
    }
    
    private DelegatingRenderer getDelegatingRenderer() {
        if (delegatingRenderer == null) {
        	/* in BasicTreeUI.updateRenderer 2092 steht:
            TreeCellRenderer      newCellRenderer;
            newCellRenderer = tree.getCellRenderer();
               was hierhin führt
            Dh. das kommt im JXTree ctor dran
        	 */
            LOG.config("only called once (in ctor)... to get hold of the default?");
            delegatingRenderer = new DelegatingRenderer();
        }
        return delegatingRenderer;
    }

    /**
     * Creates and returns the default cell renderer to use. Subclasses may
     * override to use a different type.
     * <p>
     * 
     * This implementation returns a renderer of type
     * <code>DefaultTreeCellRenderer</code>. <b>Note:</b> Will be changed to
     * return a renderer of type <code>DefaultTreeRenderer</code>,
     * once WrappingProvider is reasonably stable.
     * 
     * @return the default cell renderer to use with this tree.
     */
    protected TreeCellRenderer createDefaultCellRenderer() {
//        return new DefaultTreeCellRenderer();
        return new DefaultXTreeCellRenderer();
    }

    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to return the delegating renderer which is wrapped around the
     * original to support highlighting. The returned renderer is of type 
     * DelegatingRenderer and guaranteed to not-null<p>
     * 
     * @see #setCellRenderer(TreeCellRenderer)
     * @see DelegatingRenderer
     */
    @Override
    public TreeCellRenderer getCellRenderer() {
        // PENDING JW: something wrong here - why exactly can't we return super? 
        // not even if we force the initial setting in init?
    	//
    	// EUG: interface TreeCellRenderer has method getTreeCellRendererComponent
    	// DelegatingRenderer is a class which implements TreeCellRenderer 
    	// And RolloverRenderer with methods isEnabled and doClick
    	// ==> we need a Class which implements both interfaces !!!
        return getDelegatingRenderer();
    }

    /**
     * Returns the renderer installed by client code or the default if none has
     * been set.
     * 
     * @return the wrapped renderer.
     * @see #setCellRenderer(TreeCellRenderer)
     */
    public TreeCellRenderer getWrappedCellRenderer() {
        return getDelegatingRenderer().getDelegateRenderer();
    }

    /**
     * {@inheritDoc} <p>
     * 
     * Overridden to wrap the given renderer in a DelegatingRenderer to support
     * highlighting. <p>
     * 
     * Note: the wrapping implies that the renderer returned from the getCellRenderer
     * is <b>not</b> the renderer as given here, but the wrapper. To access the original,
     * use <code>getWrappedCellRenderer</code>.
     * 
     * @see #getWrappedCellRenderer()
     * @see #getCellRenderer()
     */
    @Override
    public void setCellRenderer(TreeCellRenderer renderer) {
        // PENDING: do something against recursive setting
        // == multiple delegation...
        getDelegatingRenderer().setDelegateRenderer(renderer);
        super.setCellRenderer(delegatingRenderer);
        // quick hack for #1061: renderer/editor inconsistent
        if ((renderer instanceof DefaultTreeCellRenderer) && 
                (getCellEditor() instanceof DefaultXTreeCellEditor)) {
           ((DefaultXTreeCellEditor) getCellEditor()).setRenderer((DefaultTreeCellRenderer) renderer); 
        }
        firePropertyChange("cellRenderer", null, delegatingRenderer);
    }

    
    /**
     * A decorator for the original TreeCellRenderer. Needed to hook highlighters
     * after messaging the delegate.<p>
     * 
     * PENDING JW: formally implement UIDependent? 
     * PENDING JW: missing updateUI anyway (got lost when pasted from JXList ;-)
     * PENDING JW: missing override of updateUI in xtree ...
     */
    public class DelegatingRenderer extends DefaultTreeRenderer 
    		implements UIResource, TreeCellRenderer, RolloverRenderer {
    	// NB: 
    	// public class DefaultTreeRenderer extends AbstractRenderer implements TreeCellRenderer
    	// abstract class AbstractRenderer implements RolloverRenderer, StringValue, Serializable, UIDependent
        private Icon    closedIcon = null;
        private Icon    openIcon = null;
        private Icon    leafIcon = null;
       
        private TreeCellRenderer delegate;
        // NB: in AbstractRenderer: protected ComponentProvider<?> componentController;
        
        /**
         * Instantiates a DelegatingRenderer with tree's default renderer as delegate.
         */
        public DelegatingRenderer() {
            this(null, null, null);
//            initIcons(new DefaultTreeCellRenderer());
        }

        public DelegatingRenderer(StringValue sv) {
        	this(null, null, sv);
        }
        public DelegatingRenderer(IconValue iv, StringValue sv) {
        	this(null, iv, sv);
        }

        /**
         * Instantiates a DelegatingRenderer with the given delegate. 
         * If the delegate is null, the default is created via the trees's factory method.
         * 
         * @param delegate the delegate to use, 
         * 	if not instanceof DefaultTreeCellRenderer the tree's default is created and used.
	     * @param iv the IconValue to use for mapping a custom icon for a given value
	     * @param sv the converter to use for mapping the content value to a String representation.
         */
        public DelegatingRenderer(TreeCellRenderer delegate, IconValue iv, StringValue sv) {
        	super(new WrappingProvider(iv, sv));        	
        	if(delegate instanceof DefaultTreeCellRenderer) {
        		initIcons((DefaultTreeCellRenderer)delegate);
        	} else {
//        		initIcons(new DefaultTreeCellRenderer());
        		// EUG better DefaultXTreeCellRenderer extends DefaultTreeCellRenderer ?
        		initIcons(new DefaultXTreeCellRenderer());
        	}
        }

        /**
         * Sets the delegate. If the
         * delegate is null, the default is created via the tree's factory method.
         * Updates the folder/leaf icons. 
         * 
         * THINK: how to update? always override with this.icons, only
         * if renderer's icons are null, update this icons if they are not,
         * update all if only one is != null.... ??
         * 
         * @param delegate the delegate to use, if null the list's default is
         *   created and used.
         */
        public void setDelegateRenderer(TreeCellRenderer delegate) {
            if (delegate == null) {
                delegate = createDefaultCellRenderer();
            }
            this.delegate = delegate;
            updateIcons();
        }
        
        /**
         * initially sets the icons to the defaults as given
         * by a DefaultTreeCellRenderer.
         * 
         * @param renderer
         */
        private void initIcons(DefaultTreeCellRenderer renderer) {
            closedIcon = renderer.getDefaultClosedIcon();
            openIcon = renderer.getDefaultOpenIcon();
            leafIcon = renderer.getDefaultLeafIcon();
        }

        /**
         * tries to set the renderers icons. Can succeed only if the
         * delegate is a DefaultTreeCellRenderer.
         * THINK: how to update? always override with this.icons, only
         * if renderer's icons are null, update this icons if they are not,
         * update all if only one is != null.... ??
         * 
         */
        private void updateIcons() {
            if (!isOverwriteRendererIcons()) return;
            setClosedIcon(closedIcon);
            setOpenIcon(openIcon);
            setLeafIcon(leafIcon);
        }

        public void setClosedIcon(Icon closedIcon) {
            if (delegate instanceof DefaultTreeCellRenderer) {
                ((DefaultTreeCellRenderer) delegate).setClosedIcon(closedIcon);
            }
            this.closedIcon = closedIcon;
        }
        
        public void setOpenIcon(Icon openIcon) {
            if (delegate instanceof DefaultTreeCellRenderer) {
                ((DefaultTreeCellRenderer) delegate).setOpenIcon(openIcon);
            }
            this.openIcon = openIcon;
        }
        
        public void setLeafIcon(Icon leafIcon) {
            if (delegate instanceof DefaultTreeCellRenderer) {
                ((DefaultTreeCellRenderer) delegate).setLeafIcon(leafIcon);
            }
            this.leafIcon = leafIcon;
        }
        
        //--------------- TreeCellRenderer
        
        /**
         * Returns the delegate.
         * 
         * @return the delegate renderer used by this renderer, guaranteed to
         *   not-null.
         */
        public TreeCellRenderer getDelegateRenderer() {
            return delegate;
        }
        
        /**
         * {@inheritDoc} <p>
         * 
         * Overridden to apply the highlighters, if any, after calling the delegate.
         * The decorators are not applied if the row is invalid.
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component result = super.getTreeCellRendererComponent(tree, value, 
            	selected, expanded, leaf, row, hasFocus);

//            boolean applyHighliter = false;
            if ((compoundHighlighter != null) && (row < getRowCount()) && (row >= 0)) {
                result = compoundHighlighter.highlight(result, getComponentAdapter(row));
//                applyHighliter = true;
            } 
//            LOG.info("Component for "+row+" row value:"+value+" is "+(applyHighliter?"(highlighted) ":"")+result);
            
            return result;
        }
            
            // ------------------ RolloverRenderer

        @Override
        public boolean isEnabled() {
        	if(delegate instanceof RolloverRenderer) {
        		RolloverRenderer ror = (RolloverRenderer)delegate;
        		ror.isEnabled();
        	}
            return false;
        }
            
        @Override
        public void doClick() {
            if (isEnabled()) {
                ((RolloverRenderer) delegate).doClick();
            }
        }

    }

    /**
     * Invalidates cell size caching in the ui delegate. May do nothing if there's no
     * safe (i.e. without reflection) way to message the delegate. <p>
     * 
     * This implementation calls BasicTreeUI setLeftChildIndent with the old indent if available. 
     * Beware: clearing the cache is an undocumented implementation side-effect of the 
     * method. Revisit if we ever should have a custom ui delegate.
     * 
     * 
     */
    public void invalidateCellSizeCache() {
        if (getUI() instanceof BasicTreeUI) {
            BasicTreeUI ui = (BasicTreeUI) getUI();
            ui.setLeftChildIndent(ui.getLeftChildIndent());
        }
    }
    
//----------------------- edit
    
    /**
     * {@inheritDoc} <p>
     * Overridden to fix focus issues with editors. 
     * This method installs and updates the internal CellEditorRemover which
     * terminates ongoing edits if appropriate. Additionally, it
     * registers a CellEditorListener with the cell editor to grab the 
     * focus back to tree, if appropriate.
     * 
     * @see #updateEditorRemover()
     */
    @Override
    public void startEditingAtPath(TreePath path) {
        super.startEditingAtPath(path);
        if (isEditing()) {
            updateEditorListener();
            updateEditorRemover();
        }
    }

    
    /**
     * Hack to grab focus after editing.
     */
    private void updateEditorListener() {
        if (editorListener == null) {
            editorListener = new CellEditorListener() {

                @Override
                public void editingCanceled(ChangeEvent e) {
                    terminated(e);
                }

                /**
                 * @param e
                 */
                private void terminated(ChangeEvent e) {
                    analyseFocus();
                    ((CellEditor) e.getSource()).removeCellEditorListener(editorListener);
                }

                @Override
                public void editingStopped(ChangeEvent e) {
                    terminated(e);
                }
                
            };
        }
        getCellEditor().addCellEditorListener(editorListener);

    }

    /**
     * This is called from cell editor listener if edit terminated.
     * Trying to analyse if we should grab the focus back to the
     * tree after. Brittle ... we assume we are the first to 
     * get the event, so we can analyse the hierarchy before the
     * editing component is removed.
     */
    protected void analyseFocus() {
        if (isFocusOwnerDescending()) {
            requestFocusInWindow();
        }
    }


    /**
     * Returns a boolean to indicate if the current focus owner 
     * is descending from this table. 
     * Returns false if not editing, otherwise walks the focusOwner
     * hierarchy, taking popups into account. <p>
     * 
     * PENDING: copied from JXTable ... should be somewhere in a utility
     * class?
     * 
     * @return a boolean to indicate if the current focus
     *   owner is contained.
     */
    private boolean isFocusOwnerDescending() {
        if (!isEditing()) return false;
        Component focusOwner = 
            KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        // PENDING JW: special casing to not fall through ... really wanted?
        if (focusOwner == null) return false;
        if (SwingXUtilities.isDescendingFrom(focusOwner, this)) return true;
        // same with permanent focus owner
        Component permanent = 
            KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
        return SwingXUtilities.isDescendingFrom(permanent, this);
    }



    /**
     * Overridden to release the CellEditorRemover, if any.
     */
    @Override
    public void removeNotify() {
        if (editorRemover != null) {
            editorRemover.release();
            editorRemover = null;
        }
        super.removeNotify();
    }

    /**
     * Lazily creates and updates the internal CellEditorRemover.
     * 
     *
     */
    private void updateEditorRemover() {
        if (editorRemover == null) {
            editorRemover = new CellEditorRemover();
        }
        editorRemover.updateKeyboardFocusManager();
    }

    /** This class tracks changes in the keyboard focus state. It is used
     * when the JXTree is editing to determine when to terminate the edit.
     * If focus switches to a component outside of the JXTree, but in the
     * same window, this will terminate editing. The exact terminate 
     * behaviour is controlled by the invokeStopEditing property.
     * 
     * @see javax.swing.JTree#setInvokesStopCellEditing(boolean)
     * 
     */
    public class CellEditorRemover implements PropertyChangeListener {
        /** the focusManager this is listening to. */
        KeyboardFocusManager focusManager;

        public CellEditorRemover() {
            updateKeyboardFocusManager();
        }

        /**
         * Updates itself to listen to the current KeyboardFocusManager. 
         *
         */
        public void updateKeyboardFocusManager() {
            KeyboardFocusManager current = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            setKeyboardFocusManager(current);
        }

        /**
         * stops listening.
         *
         */
        public void release() {
            setKeyboardFocusManager(null);
        }
        
        /**
         * Sets the focusManager this is listening to. 
         * Unregisters/registers itself from/to the old/new manager, 
         * respectively. 
         * 
         * @param current the KeyboardFocusManager to listen too.
         */
        private void setKeyboardFocusManager(KeyboardFocusManager current) {
            if (focusManager == current)
                return;
            KeyboardFocusManager old = focusManager;
            if (old != null) {
                old.removePropertyChangeListener("permanentFocusOwner", this);
            }
            focusManager = current;
            if (focusManager != null) {
                focusManager.addPropertyChangeListener("permanentFocusOwner", this);
            }

        }
        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (!isEditing()) {
                return;
            }

            Component c = focusManager.getPermanentFocusOwner();
            JXTree tree = JXTree.this;
            while (c != null) {
                if (c instanceof JPopupMenu) {
                    c = ((JPopupMenu) c).getInvoker();
                } else {

                    if (c == tree) {
                        // focus remains inside the table
                        return;
                    } else if ((c instanceof Window) ||
                            (c instanceof Applet && c.getParent() == null)) {
                        if (c == SwingUtilities.getRoot(tree)) {
                            if (tree.getInvokesStopCellEditing()) {
                                tree.stopEditing();
                            }
                            if (tree.isEditing()) {
                                tree.cancelEditing();
                            }
                        }
                        break;
                    }
                    c = c.getParent();
                }
            }
        }
    }

//------------------------------- ComponentAdapter    
    /**
     * @return the unconfigured ComponentAdapter.
     */
    protected ComponentAdapter getComponentAdapter() {
        if (dataAdapter == null) {
            dataAdapter = new TreeAdapter(this);
        }
        return dataAdapter;
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

    protected ComponentAdapter dataAdapter;

    protected static class TreeAdapter extends ComponentAdapter {
        private final JXTree tree;

        /**
         * Constructs a <code>TableCellRenderContext</code> for the specified
         * target component.
         *
         * @param component the target component
         */
        public TreeAdapter(JXTree component) {
            super(component);
            tree = component;
        }
        
        public JXTree getTree() {
            return tree;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasFocus() {
            return tree.isFocusOwner() && (tree.getLeadSelectionRow() == row);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValueAt(int row, int column) {
            TreePath path = tree.getPathForRow(row);
            return path.getLastPathComponent();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String getStringAt(int row, int column) {
            return tree.getStringAt(row);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Rectangle getCellBounds() {
            return tree.getRowBounds(row);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEditable() {
            //this is not as robust as JXTable; should it be? -- kgs
            return tree.isPathEditable(tree.getPathForRow(row));
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isSelected() {
            return tree.isRowSelected(row);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isExpanded() {
            return tree.isExpanded(row);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getDepth() {
            return tree.getPathForRow(row).getPathCount() - 1;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isHierarchical() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isLeaf() {
            return tree.getModel().isLeaf(getValue());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;        /** TODO: ? */
        }
    }


}
