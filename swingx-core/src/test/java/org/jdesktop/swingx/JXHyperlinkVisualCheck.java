/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 */
package org.jdesktop.swingx;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.hyperlink.AbstractHyperlinkAction;
import org.jdesktop.swingx.hyperlink.EditorPaneLinkVisitor;
import org.jdesktop.swingx.hyperlink.HyperlinkAction;
import org.jdesktop.swingx.hyperlink.LinkModel;
import org.jdesktop.swingx.hyperlink.LinkModelAction;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.DefaultTreeRenderer;
import org.jdesktop.swingx.renderer.HyperlinkProvider;
import org.jdesktop.swingx.treetable.FileSystemModel;

/**
 * Test of JXHyperlink visuals. Raw usage and as hyperlinkRenderer.
 * <p>
 * 
 * @author Jeanette Winzenburg
 */
public class JXHyperlinkVisualCheck extends InteractiveTestCase {
	
    private static final Logger LOG = Logger.getLogger(JXHyperlinkVisualCheck.class.getName());

    public JXHyperlinkVisualCheck() {
        super("JXHyperlinkLabel Test");
    }

	public static void main(String[] args) throws Exception {
		JXHyperlinkVisualCheck test = new JXHyperlinkVisualCheck();
		try {
			test.runInteractiveTests();
//			test.runInteractiveTests("interactiveLink");
//			test.runInteractiveTests("interactiveTreeLinkRendererSimpleText");
//			test.runInteractiveTests("interactive.*Table.*");
//			test.runInteractiveTests("interactive.*List.*");
//			test.runInteractiveTests("interactive.*Tree.*");
//			test.runInteractiveTests("interactive.*Underline.*");
//			test.runInteractive("Block");
		} catch (Exception e) {
			System.err.println("exception when executing interactive tests:");
			e.printStackTrace();
		}
	}
    
    /**
     * Issue #1530-swingx:
     * 
     * Quick check if Desktop.browse is blocking (with a lame target site)
     * - doesn't appear so.
     * 
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public void interactiveHyperlinkActionBlocking() throws MalformedURLException, URISyntaxException {
        URI uri = new URL("http://www.java.net/forums/javadesktop/java-desktop-technologies/swinglabs").toURI();
        JXHyperlink link = new JXHyperlink(HyperlinkAction.createHyperlinkAction(uri));
        showInFrame(link, "blocking?");
    }
    /**
     * Issue #6-swingx: convenience api for setting uri's
     * @throws URISyntaxException
     */
    public void interactiveHyperlinkURI() throws URISyntaxException {
        JXHyperlink link = new JXHyperlink();
        URI uri = new URI("mailto:java-net@java.sun.com");
        link.setURI(uri);
        showInFrame(link, "uri");
    }
    
    /**
     * Issue #441-swingx: underline not showing for html text.
     * While text wrapping as such is working with html text the
     * underline is only under the last line. 
     */
    public void interactiveHtmlUnderlineWrapping() {
        @SuppressWarnings("serial")
		Action action = new AbstractAction("<html><b><i>Bold Italic Link and another loong way way out part of the text</i></b></html>") {

            public void actionPerformed(ActionEvent e) {
                LOG.info("Action in interactiveHtmlUnderlineWrapping performed - the event:\n"+e);
            }
            
        };
        JXHyperlink hyperlink = new JXHyperlink(action);
//      hyperlink.setMargin(new Insets(10, 10, 10, 10));
        hyperlink.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JFrame frame = wrapInFrame(hyperlink, "show html underline ");
        frame.setSize(200, 200);
        frame.setVisible(true);     
    }
    

    /**
     * Issue #441-swingx: underline not showing for html text.
     *
     */
    public void interactiveHtmlUnderlineButton() {
        @SuppressWarnings("serial")
		Action action = new AbstractAction("<html><b><i>Bold Italic Link</i></b></html>") {

            public void actionPerformed(ActionEvent e) {
                LOG.info("Action in interactiveHtmlUnderlineButton performed - the event:\n"+e);
            }
            
        };
        JXHyperlink hyperlink = new JXHyperlink(action );
//        hyperlink.setMargin(new Insets(10, 10, 10, 10));
        hyperlink.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JFrame frame = wrapInFrame(hyperlink, "show html underline ");
        frame.setSize(200, 200);
        frame.setVisible(true);
        
    }
    
    /**
     * visually check how differently configured buttons behave on
     * clicked.
     *
     */
    public void interactiveCompareClicked() {
        JComponent box = Box.createVerticalBox();
        JXHyperlink noActionHyperlink = new JXHyperlink();
        noActionHyperlink.setText("have no action - auto-click");
        box.add(noActionHyperlink);
        AbstractHyperlinkAction<?> doNothingAction = createEmptyLinkAction("have do nothing action - follow action");
        JXHyperlink doNothingActionHyperlink = new JXHyperlink(doNothingAction);
        box.add(doNothingActionHyperlink);
        
        AbstractHyperlinkAction<?> doNothingAction2 = createEmptyLinkAction("have do nothing action - overrule");
        JXHyperlink overruleActionHyperlink = new JXHyperlink(doNothingAction2);
        overruleActionHyperlink.setOverrulesActionOnClick(true);
        box.add(overruleActionHyperlink);
        JXFrame frame = wrapInFrame(box, "compare clicked control");
        frame.setVisible(true);       
    }
    
    public void interactiveUnderlineButton() {
        @SuppressWarnings("serial")
		Action action = new AbstractAction("LinkModel@somewhere") {

            public void actionPerformed(ActionEvent e) {
                LOG.info("gotcha!");            
            }
            
        };
        JXHyperlink hyperlink = new JXHyperlink(action );
//        JButton hyperlink = new JButton("LinkModel somewhere");
//      hyperlink.setMargin(new Insets(10, 10, 10, 10));
        hyperlink.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JXPanel panel = new JXPanel(new BorderLayout());
        panel.add(hyperlink);
        panel.add(new JLabel("south"), BorderLayout.SOUTH);
        panel.add(new JLabel("north"), BorderLayout.NORTH);
        JFrame frame = wrapInFrame(panel, "show underline - no link action");
        frame.setSize(200, 200);
        frame.setVisible(true);
    }
    
 
    public void interactiveLink() throws Exception {
//        JXEditorPane pane = new JXEditorPane();
//        pane.setEditable(false);
//        LOG.info("JXEditorPane pane.ContentType:"+pane.getContentType());
//        EditorPaneLinkVisitor visitor = new EditorPaneLinkVisitor(pane);
        
        // with DefaultEditorPane has same behavior :
        EditorPaneLinkVisitor visitor = new EditorPaneLinkVisitor();
        
        LinkModel localLink = new LinkModel("Click me! - local text", null, JXEditorPaneTest.class.getResource("resources/test.html"));
        LinkModelAction<?> localAction = new LinkModelAction<LinkModel>(localLink, visitor);
        JXHyperlink localHyperlink = new JXHyperlink(localAction);
        
        String swingx = "http://swingx.java.net"; // Error 404: couldn't show
        // existing pages:
        String swinglabs = "https://en.wikipedia.org/wiki/SwingLabs";
        String htmlframes = "https://www.w3.org/TR/html401/present/frames.html";
        LinkModel externalLink = new LinkModel("Click me! - external text", null, new URL(htmlframes));
        LinkModelAction<?> externalAction = new LinkModelAction<LinkModel>(externalLink, visitor);
        JXHyperlink externalHyperlink = new JXHyperlink(externalAction);
        JPanel panel = new JPanel(new BorderLayout());
        
        panel.add(new JScrollPane(visitor.getOutputComponent()));
        panel.add(externalHyperlink, BorderLayout.NORTH);
        panel.add(localHyperlink, BorderLayout.SOUTH);
        JFrame frame = wrapInFrame(panel, "simple hyperlink");
        frame.setSize(200, 200);
        frame.setVisible(true);      
    }

//---------------------- interactive test: JXTree
    public void interactiveTreeLinkRendererSimpleText() {
        @SuppressWarnings("serial")
		AbstractHyperlinkAction<Object> simpleAction = new AbstractHyperlinkAction<Object>(null) {

            public void actionPerformed(ActionEvent e) {
                LOG.info("hit: " + getTarget());               
            }
            
        };
        JXTree tree = new JXTree(new FileSystemModel());
        tree.setRolloverEnabled(true);
        HyperlinkProvider provider =  new HyperlinkProvider(simpleAction);
        tree.setCellRenderer(new DefaultTreeRenderer(provider));
        tree.setHighlighters(HighlighterFactory.createSimpleStriping()); // no striping in Metal
//        tree.setHighlighters(HighlighterFactory.createAlternateStriping()); // ugly striping
        JFrame frame = wrapWithScrollingInFrame(tree, "tree and simple links");
        frame.setVisible(true);      
    }



//---------------------- interactive test: JXTable
 
    /**
     * Table url activation, disabled tooltips.
     */
    public void interactiveTableHyperlinkNoTooltip() {
        JXTable table = new JXTable(createModelWithLinks());
        EditorPaneLinkVisitor visitor = new EditorPaneLinkVisitor();
        @SuppressWarnings("serial")
		LinkModelAction<?> action = new LinkModelAction<LinkModel>(visitor) {

            @Override
            protected void updateFromTarget() {
                super.updateFromTarget();
                putValue(Action.SHORT_DESCRIPTION, null);
            }
            
        };
        // set the default renderer for LinkModel - which is basically a bean wrapped around an URL
        table.setDefaultRenderer(LinkModel.class, new DefaultTableRenderer(new HyperlinkProvider(action, LinkModel.class)));
        JXFrame frame = wrapWithScrollingInFrame(table, visitor.getOutputComponent(), "table and simple links");
        frame.setVisible(true);
    }
    

    /**
     * Table with both simple "hyperlinks" targets and url activation.
     */
    public void interactiveTableHyperlinkSimpleText() {
        JXTable table = new JXTable(createModelWithLinks());
        EditorPaneLinkVisitor visitor = new EditorPaneLinkVisitor();
        LinkModelAction<?> action = new LinkModelAction<LinkModel>(visitor);
        // set the default renderer for LinkModel - which is basically
        // a bean wrapped around an URL
        table.setDefaultRenderer(LinkModel.class, new DefaultTableRenderer(new HyperlinkProvider(action, LinkModel.class)));
        
        // JW: editor mis-use is not recommended but possible...
        // the LinkRenderer in its role as editor is not yet deprecated but should soon. 
        // Without registering a specialized editor the column should
        // not editable - otherwise the "normal" default editor would jump in
//        LinkModelAction action2 = new LinkModelAction<LinkModel>(visitor);
//        table.setDefaultEditor(LinkModel.class, new LinkRenderer(action2, LinkModel.class));

        // simple activatable action on the target in the first column
        @SuppressWarnings("serial")
		AbstractHyperlinkAction<Object> simpleAction = new AbstractHyperlinkAction<Object>(null) {
            
            public void actionPerformed(ActionEvent e) {
                LOG.info("hit: " + getTarget());
                
            }
            
        };
        // the action will be activated only if the column is not-editable
        // here that's done in the model
        // TODO JW: revisit
        table.getColumn(0).setCellRenderer(new DefaultTableRenderer(new HyperlinkProvider(simpleAction)));

        JXFrame frame = wrapWithScrollingInFrame(table, visitor.getOutputComponent(), "table and simple links");
        frame.setVisible(true);
    }
    
    
    /** 
     * Check visuals of hyperlink and highlighter.
     * Issue #513-swingx: no rollover effect for disabled table.
     * Hyperlink is disabled automatically by DefaultVisuals, no need
     * to disable the xxRolloverController.
     * 
     */
    public void interactiveTableHyperlinkLFStripingHighlighter() {
        EditorPaneLinkVisitor visitor = new EditorPaneLinkVisitor();
        final JXTable table = new JXTable(createModelWithLinks());
        LinkModelAction<?> action = new LinkModelAction<LinkModel>(visitor);
        table.setDefaultRenderer(LinkModel.class, new DefaultTableRenderer
                (new HyperlinkProvider(action, LinkModel.class)));
        table.setHighlighters(HighlighterFactory.createSimpleStriping());
        @SuppressWarnings("serial")
		Action enabled = new AbstractAction("toggle table enabled") {

            public void actionPerformed(ActionEvent e) {
                table.setEnabled(!table.isEnabled());
                
            }
            
        };
        JXFrame frame = wrapWithScrollingInFrame(table, visitor.getOutputComponent(), 
                "show link renderer in table with LF striping highlighter");
        addAction(frame, enabled);
        frame.setVisible(true);
    }

    
//----------------- interactive tests: JXList
 
    /**
     * Custom link target action in JXList.
     */
    public void interactiveListHyperlikPlayer() {
        @SuppressWarnings("serial")
		AbstractHyperlinkAction<Player> linkAction = new AbstractHyperlinkAction<Player>() {

            public void actionPerformed(ActionEvent e) {
                LOG.info("hit: " + getTarget());               
            }
            
            @Override
            protected void installTarget() {
                setName(getTarget() != null ? getTarget().name : "");
            }
            
        };
        
        JXList<Player> list = new JXList<Player>(createPlayerModel(), true);
        list.setRolloverEnabled(true);
        // descending order - check if the action is performed for the correct value
        list.setSortOrder(SortOrder.DESCENDING);
        list.setCellRenderer(new DefaultListRenderer<>(new HyperlinkProvider(linkAction, Player.class)));
        JFrame frame = wrapWithScrollingInFrame(list, "show simple bean link renderer in list");
        frame.setVisible(true);

    }
    
    private ListModel<Player> createPlayerModel() {
        DefaultListModel<Player> model = new DefaultListModel<Player>();
        model.addElement(new Player("Henry", 10));
        model.addElement(new Player("Berta", 112));
        model.addElement(new Player("Dave", 20));
        return model;
    }

    public static class Player {
        String name;
        int score;
        public Player(String name, int score) {
            this.name = name;
            this.score = score;
        }
        @Override
        public String toString() {
            return name + " has score: " + score;
        }
    }

    /**
     * Simple target action.
     *
     */
    public void interactiveListHyperlinkSimpleText() {
        @SuppressWarnings("serial")
		AbstractHyperlinkAction<Object> linkAction = new AbstractHyperlinkAction<Object>(null) {

            public void actionPerformed(ActionEvent e) {
                LOG.info("hit: " + getTarget());
            }
            
        };
        JXList<String> list = new JXList<String>(createTextOnlyListModel(20));
        list.setRolloverEnabled(true);
        list.setCellRenderer(new DefaultListRenderer<>(new HyperlinkProvider(linkAction)));
        JFrame frame = wrapWithScrollingInFrame(list, "show simple link renderer in list");
        frame.setVisible(true);
    }

    /**
     * ListModel with LinkModel.
     *
     */
    public void interactiveListHyperlink() {
        EditorPaneLinkVisitor visitor = new EditorPaneLinkVisitor();
        JXList<LinkModel> list = new JXList<LinkModel>(createListModelWithLinks(20));
        list.setRolloverEnabled(true);
        LinkModelAction<?> action = new LinkModelAction<LinkModel>(visitor);
        list.setCellRenderer(new DefaultListRenderer<>(new HyperlinkProvider(action, LinkModel.class)));
        JFrame frame = wrapWithScrollingInFrame(list, visitor.getOutputComponent(), "show link renderer in list");
        frame.setVisible(true);
    }
    
    /**
     * Visuals of Hyperlink/Highlighter interaction.
     *
     */
    public void interactiveListHyperlinkLFStripingHighlighter() {
        EditorPaneLinkVisitor visitor = new EditorPaneLinkVisitor();
        JXList<LinkModel> list = new JXList<LinkModel>(createListModelWithLinks(20));
        LinkModelAction<?> action = new LinkModelAction<LinkModel>(visitor);
        list.setCellRenderer(new DefaultListRenderer<>(new HyperlinkProvider(action, LinkModel.class)));
        list.setRolloverEnabled(true);
        list.setHighlighters(HighlighterFactory.createSimpleStriping());
        JFrame frame = wrapWithScrollingInFrame(list, visitor.getOutputComponent(), 
                "show link renderer in list with LFStriping highlighter");
        frame.setVisible(true);
    }
    


    private ListModel<String> createTextOnlyListModel(int count) {
        DefaultListModel<String> model = new DefaultListModel<String>();
        for (int i = 0; i < count; i++) {
                model.addElement("text #" + i);
        }
        return model;
    }
    
    private ListModel<LinkModel> createListModelWithLinks(int count) {
        DefaultListModel<LinkModel> model = new DefaultListModel<LinkModel>();
        for (int i = 0; i < count; i++) {
            try {
                LinkModel link = new LinkModel("a link text " + i, null, new URL("http://some.dummy.url" + i));
                if (i == 1) {
                    URL url = JXEditorPaneTest.class.getResource("resources/test.html");

                    link = new LinkModel("a link text " + i, null, url);
                }
                model.addElement(link);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } 
        return model;
    }
    
    private TableModel createModelWithLinks() {
        String[] columnNames = { "text - not-editable",  "Link not-editable", "Bool editable", "Bool not-editable" };
        
        @SuppressWarnings("serial")
		DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                    return !getColumnName(column).contains("not");
            }
            
        };
        for (int i = 0; i < 4; i++) {
            try {
                LinkModel link = new LinkModel("a link text " + i, null, new URL("http://some.dummy.url" + i));
                if (i == 1) {
                    URL url = JXEditorPaneTest.class.getResource("resources/test.html");

                    link = new LinkModel("a link text " + i, null, url);
                }
                model.addRow(new Object[] {"text only " + i, link, Boolean.TRUE, Boolean.TRUE });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return model;
    }

    
	protected AbstractHyperlinkAction<Object> createEmptyLinkAction() {
		@SuppressWarnings("serial")
		AbstractHyperlinkAction<Object> linkAction = new AbstractHyperlinkAction<Object>(null) {

			public void actionPerformed(ActionEvent e) {
			}

		};
		return linkAction;
	}

    protected AbstractHyperlinkAction<?> createEmptyLinkAction(String name) {
        AbstractHyperlinkAction<?> linkAction = createEmptyLinkAction();
        linkAction.setName(name);
        return linkAction;
    }

    /**
     * make auto-run happy in the absence of real test methods.
     *
     */
    public void testDummy() {
        
    }
}
