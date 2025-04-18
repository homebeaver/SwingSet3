/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 */
package org.jdesktop.testtests;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

import org.jdesktop.swingx.JXTreeTable;

/**
 * A CellEditorListener that stores the received ChangeEvents.
 * 
 */
public class TreeExpansionReport implements TreeExpansionListener {
    
	private static final Logger LOG = Logger.getLogger(TreeExpansionReport.class.getName());
	
    /**
     * Holds a list of all canceled events.
     */
    protected List<TreeExpansionEvent> expandedEvents = Collections.synchronizedList(new LinkedList<TreeExpansionEvent>());
    protected List<TreeExpansionEvent> collapsedEvents = Collections.synchronizedList(new LinkedList<TreeExpansionEvent>());
    protected List<TreeExpansionEvent> allEvents = Collections.synchronizedList(new LinkedList<TreeExpansionEvent>());
    
    public TreeExpansionReport() {
        this((JTree) null);
    }
    
    public TreeExpansionReport(JTree tree) {
        if (tree != null) {
            tree.addTreeExpansionListener(this);
        }
    }
    
    public TreeExpansionReport(JXTreeTable tree) {
        if (tree != null) {
            tree.addTreeExpansionListener(this);
    		LOG.config("registered as TreeExpansionListener to " + tree);
        }
    }

//------------------------ implement CellEditorListener

	public void treeExpanded(TreeExpansionEvent e) {
		LOG.config("TreeExpansionEvent no " + (expandedEvents.size()+1) + " " + e);
		expandedEvents.add(0, e);
		allEvents.add(0, e);
	}

	public void treeCollapsed(TreeExpansionEvent e) {
		LOG.config("TreeExpansionEvent no " + (collapsedEvents.size()+1) + " " + e);
		collapsedEvents.add(0, e);
		allEvents.add(0, e);
	}
    
//----------------------- utility methods to access all events    
    public void clear() {
        expandedEvents.clear();
        collapsedEvents.clear();
        allEvents.clear();
    }

    public int getEventCount() {
        return allEvents.size();
    }
     
    public boolean hasEvents() {
        return !allEvents.isEmpty();
    }
 
    public TreeExpansionEvent getLastEvent() {
        return allEvents.isEmpty() ? null : allEvents.get(0);
    }

//------------------ access canceled events
    public int getExpandedEventCount() {
        return expandedEvents.size();
    }
    
    public boolean hasExpandedEvents() {
        return !expandedEvents.isEmpty();
    }
    
    public TreeExpansionEvent getLastExpandedEvent() {
        return expandedEvents.isEmpty() ? null : expandedEvents.get(0);
    }

//----------------- access stopped events
    
    public int getCollapsedEventCount() {
        return collapsedEvents.size();
    }
    
    public boolean hasCollapsedEvents() {
        return !collapsedEvents.isEmpty();
    }
    
    public TreeExpansionEvent getLastCollapsedEvent() {
        return collapsedEvents.isEmpty() ? null : collapsedEvents.get(0);
    }
    
    
}
