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
 */
package org.jdesktop.swingx.hyperlink;

import java.awt.event.ItemEvent;

import org.jdesktop.swingx.action.AbstractActionExt;

/**
 * Convenience implementation to simplify {@link org.jdesktop.swingx.JXHyperlink} configuration and
 * provide minimal api.
 * 
 * @author Jeanette Winzenburg
 */
public abstract class AbstractHyperlinkAction<T> extends AbstractActionExt {

    /**
     * Key for the visited property value.
     */
    public static final String VISITED_KEY = "visited";
    /**
     * the object the actionPerformed can act on.
     */
    protected T target;

    
    /**
     * Instantiates a LinkAction with null target. 
     * 
     */
    public AbstractHyperlinkAction () {
        this(null);    }
    
    /**
     * Instantiates a LinkAction with a target of type targetClass. 
     * The visited property is initialized as defined by 
     * {@link AbstractHyperlinkAction#installTarget()}
     * 
     * @param target the target this action should act on.
     */
    public AbstractHyperlinkAction(T target) {
       setTarget(target);
    }

    /**
     * Set the visited property.
     * 
     * @param visited boolean value of the prop
     */
    public void setVisited(boolean visited) {
        putValue(VISITED_KEY, visited);
    }

    /**
     * 
     * @return visited state
     */
    public boolean isVisited() {
        Boolean visited = (Boolean) getValue(VISITED_KEY);
        return Boolean.TRUE.equals(visited);
    }

    /**
     * get the object the actionPerformed acts on.
     * @return target
     */
    public T getTarget() {
        return target;
    }

    /**
     * set the object the actionPerformed cam act on.
     * PRE: isTargetable(target)
     * @param target generic object
     */
    public void setTarget(T target) {
        T oldTarget = getTarget();
        uninstallTarget();
        this.target = target;
        installTarget();
        firePropertyChange("target", oldTarget, getTarget());
        
    }

    /**
     * hook for subclasses to update internal state after
     * a new target has been set. <p>
     * 
     * Subclasses are free to decide the details. 
     * Here: 
     * <ul>
     * <li> the text property is set to target.toString or empty String if
     * the target is null
     * <li> visited is set to false.
     * </ul>
     */
    protected void installTarget() {
        setName(target != null ? target.toString() : "" );
        setVisited(false);
    }

    /**
     * hook for subclasses to cleanup before the old target
     * is overwritten. <p>
     * 
     * Subclasses are free to decide the details. 
     * Here: does nothing.
     */
    protected void uninstallTarget() {
        
    }
    
    @Override
    public void itemStateChanged(ItemEvent e) {
        // do nothing
    }

    /**
     * Set the state property.
     * Overridden to to nothing.
     * PENDING: really?
     * @param state if true then this action will fire ItemEvents
     */
    @Override
    public void setStateAction(boolean state) {
    }

    

}
