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
package org.jdesktop.swingx.tree;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;

import org.jdesktop.swingx.plaf.UIDependent;

/**
 * Subclassed to hack around core bug with RtoL editing (#4980473).
 * 
 * The price to pay is currently is to guarantee a minimum size of the
 * editing field (is only one char wide if the node value is null).
 * 
 * PENDING: any possibility to position the editorContainer? 
 * BasicTreeUI adds it to the tree and positions at the node location. 
 * That's not a problem in LToR, only in RToL 
 *
 * 
 * @author Jeanette Winzenburg
 */
public class DefaultXTreeCellEditor extends DefaultTreeCellEditor implements UIDependent {

	private static final Logger LOG = Logger.getLogger(DefaultXTreeCellEditor.class.getName());
	/**
	 * ctor
	 * @param tree JTree
	 * @param renderer DefaultTreeCellRenderer
	 */
    public DefaultXTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }

    /**
     * ctor - maven-javadoc-plugin 3.3.2 needs a doc here
     * @param tree JTree
     * @param renderer DefaultTreeCellRenderer
     * @param editor TreeCellEditor
     */
    public DefaultXTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer, TreeCellEditor editor) {
        super(tree, renderer, editor);
    }

    /**
     * @param renderer DefaultTreeCellRenderer
     */
    public void setRenderer(DefaultTreeCellRenderer renderer) {
        this.renderer = renderer;
    }
    
    /**
     * @return renderer
     */
    public DefaultTreeCellRenderer getRenderer() {
        return renderer;
    }
    
    /**
     * TODO maven-javadoc-plugin 3.3.2 needs a doc here
     */
    @SuppressWarnings("serial")
	public class XEditorContainer extends DefaultTreeCellEditor.EditorContainer {

        @Override
        public Dimension getPreferredSize() {
        	Dimension pSize = new Dimension(0, 0);
            if (isRightToLeft()) {
            	// code copied from javax.swing.tree.DefaultTreeCellEditor$EditorContainer
                if(editingComponent != null) {
                    pSize = editingComponent.getPreferredSize();
                	LOG.info("isRightToLeft : getPreferredSize="+pSize+" in editingComponent:"+editingComponent);
                    pSize.width += offset + 5;
    
                    Dimension         rSize = (renderer != null) ?
                                              renderer.getPreferredSize() : null;
    
                    if(rSize != null)
                        pSize.height = Math.max(pSize.height, rSize.height);
                    if(editingIcon != null)
                        pSize.height = Math.max(pSize.height,
                                                editingIcon.getIconHeight());
    
                    // trying to enforce a minimum size leads to field being painted over the icon
                    // Make sure width is at least 100.
    //                pSize.width = Math.max(pSize.width, 100);
                    return pSize;
                }
                return pSize;
            }
            pSize = super.getPreferredSize();
        	LOG.config("LeftToRight : PreferredSize="+pSize);
//pSize.width=668-(offset+5+getInsets().left+getInsets().right); // TODO maximum berechnen, die 668 sind JScrollPane.getViewport().getWidth()
            return pSize;
        }

        @Override
        public void doLayout() {
            if (isRightToLeft()) {
                Dimension             cSize = getSize();

                editingComponent.getPreferredSize();
                editingComponent.setLocation(0, 0);
                editingComponent.setBounds(0, 0,
                                           cSize.width - offset,
                                           cSize.height);
            } else {
            	LOG.info("LeftToRight : offset="+offset+" realEditor:"+realEditor
            		+" editingComponent:"+editingComponent);
                super.doLayout();
            }
        }


        @Override
        public void paint(Graphics g) {
            if (isRightToLeft()) {
                Dimension size = getSize();

                // Then the icon.
                if (editingIcon != null) {
                    int yLoc = Math.max(0, (size.height - editingIcon
                            .getIconHeight()) / 2);
                    int xLoc = Math.max(0, size.width - offset);
                    editingIcon.paintIcon(this, g, xLoc, yLoc);
                }
                // need to prevent super from painting the icon
                Icon rememberIcon = editingIcon;
                editingIcon = null;
                super.paint(g);
                editingIcon = rememberIcon;
                
            } else {
            	LOG.info("super.paint: ... borderColor:"+getBorderSelectionColor()+" editingIcon:"+editingIcon+"\n");
                super.paint(g);
                // das paint geschieht in super.super.paint das ich hier nicht rufen kann
            }
        }
        
    }


    @Override
    protected Container createContainer() {
        return new XEditorContainer();
    }

    @Override
    protected void prepareForEditing() {
        super.prepareForEditing();
        applyComponentOrientation();
    }

    /**
     * delegates to editingContainer
     */
    protected void applyComponentOrientation() {
        if (tree != null) {
            editingContainer.applyComponentOrientation(tree.getComponentOrientation());
        }
        
    }

    /**
     * @return
     */
    private boolean isRightToLeft() {
        return (tree != null) && (!tree.getComponentOrientation().isLeftToRight());
    }

    /**
     * Implement UIDependent. Quick hack for #1060-swingx: icons lost on laf toggle.
     */
    @Override
    public void updateUI() {
        if (getRenderer() != null) {
            SwingUtilities.updateComponentTreeUI(getRenderer());
        }
        if (realEditor instanceof JComponent) {
            SwingUtilities.updateComponentTreeUI((JComponent) realEditor);
        } else if (realEditor instanceof UIDependent) {
            ((UIDependent) realEditor).updateUI();
        }
        
    }

}
