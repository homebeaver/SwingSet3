package org.jdesktop.swingx.plaf.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.logging.Logger;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.CellRendererPane;
import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.LookAndFeel;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.Position;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JYList;
import org.jdesktop.swingx.plaf.XComboBoxUI;
import org.jdesktop.swingx.plaf.basic.core.LazyActionMap;
import org.jdesktop.swingx.renderer.DefaultComboBoxRenderer;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.YListCellRenderer;

/*
public abstract class ComboBoxUI extends ComponentUI // Pluggable look and feel interface for JComboBox
Die systematische/symetrische Ableitung:

                          abstract class ComponentUI
                                          |
 +----------------------> abstract class ComboBoxUI
abstract class XComboBoxUI                |
 |                                        |
BasicXComboBoxUI           symetrisch zu BasicComboBoxUI

 in abstract class javax.swing.plaf.ComponentUI :
    public ComponentUI() {}
    ... weitere Methoden, keine davon abstract
 in abstract class javax.swing.plaf.ComboBoxUI extends ComponentUI : implementation in BasicComboBoxUI
    protected ComboBoxUI() {}
    public abstract void setPopupVisible( JComboBox<?> c, boolean v );
    public abstract boolean isPopupVisible( JComboBox<?> c );
    public abstract boolean isFocusTraversable( JComboBox<?> c );
public abstract class XComboBoxUI extends ComboBoxUI {
	public abstract void installButton(Icon i);
	public abstract void setIsShowingPopupIcon(Icon i);
	public abstract void uninstallButton();
 */
public class BasicXComboBoxUI extends XComboBoxUI {

    private static final Logger LOG = Logger.getLogger(BasicXComboBoxUI.class.getName());

    /**
     * factory
     * @param c JComponent the factory is for
     * @return ComponentUI
     */
    public static ComponentUI createUI(JComponent c) {
//    	LOG.info("UI factory for JComponent:"+c); // c of type JXComboBox expected
        return new BasicXComboBoxUI();
    }

	protected JComboBox<?> comboBox;
    protected boolean hasFocus = false;
    protected ComboPopup popup; // interface ComboPopup
//    protected boolean popupVisible = false; // wg. BUG #57 moved to BasicXComboPopup
    protected JList<Object> listBox; // actually of subtype JXList
    protected Component editor;
    protected boolean squareButton = true; // used to calculate buttonWidth in getMinimumSize, Handler.layoutContainer
    protected Insets padding;
    protected CellRendererPane currentValuePane = new CellRendererPane();
    protected Icon icon;
    protected Icon isShowingPopupIcon;
    protected JButton arrowButton; // can be JXButton
    
    JComboBox.KeySelectionManager keySelectionManager;

    // Listeners that are attached to the ComboBox
    protected ItemListener itemListener;
    protected ItemListener createItemListener() {
        return null;
    }
    /**
     * handler implements Listeners needed by this class,
     * - extra XComboBoxLayoutManager ==> nicht mehr in Handler
     * - PropertyChangeListener ==> als lambda
     * - KeyListener,
     * - FocusListener,
     * - ListDataListener
     * and ActionListener ==> als lambda
     */
    private Handler handler;
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    /**
     * @return handler which implements LayoutManager 
     */
    protected LayoutManager createLayoutManager() {
        return new XComboBoxLayoutManager();
    }

    private PropertyChangeListener propertyChangeListener;
    private PropertyChangeListener getPropertyChangeListener() {
        if (propertyChangeListener == null) {
        	propertyChangeListener = new XPropertyChangeListener();
        }
        return propertyChangeListener;
    }
    protected KeyListener keyListener;
    protected KeyListener createKeyListener() {
        return getHandler();
    }
    protected FocusListener focusListener;
    protected FocusListener createFocusListener() {
        return getHandler();
    }
    // This is used for knowing when to cache the minimum preferred size.
    // If the data in the list changes, the cached value get marked for recalc.
    // Added to the current JComboBox model
    protected ListDataListener listDataListener;
    protected ListDataListener createListDataListener() {
        return getHandler();
    }
    // Listeners that the ComboPopup produces.
    protected MouseListener popupMouseListener;
    protected MouseMotionListener popupMouseMotionListener;
    protected KeyListener popupKeyListener;
    
    protected long timeFactor = 1000L;
    private long lastTime = 0L;
    private long time = 0L;
    private boolean isTableCellEditor = false;
    private static final String IS_TABLE_CELL_EDITOR = "JComboBox.isTableCellEditor";
    static final String PROP_DONT_CANCEL_POPUP = "doNotCancelPopup"; // unpublished Property Name
    static final String HIDE_POPUP_KEY = "HidePopupKey";

    // this ctor is implicit, used by factory
	protected BasicXComboBoxUI() {
		super();
	}

    /*
     * copied from javax.swing.plaf.basic.BasicComboBoxUI
     * Configures the specified component c JXComboBox
     * 1.Install default property values for color, fonts, borders, ... ==> installDefaults()
     * 2.Install a LayoutManager ==> createLayoutManager()
     * 3.Create/add any required sub-components ==> here + installComponents()
     * 4.Create/install event listeners ==> installListeners()
     * 5.Create/install a PropertyChangeListener ==> installListeners()
     * 6.Install keyboard UI (mnemonics, traversal, etc.) on the component. 
     * 7.Initialize any appropriate instance data. 
     */
    @Override // javax.swing.plaf.ComponentUI#installUI overridden
    public void installUI(JComponent c) {
        // JVM disables assertion validation by default: use -enableassertions to enable!
        assert c instanceof JComboBox;
        
    	isMinimumSizeDirty = true;
    	if(c instanceof JComboBox<?>) {
    		comboBox = (JComboBox<?>)c;
    	}
    	
    	// 1.Install default property values
    	installDefaults();
    	
    	// 2.Install a LayoutManager
        comboBox.setLayout(createLayoutManager());
        
/*         3.Create/add any required sub-components

comboBox JComboBox<?> :
 - popup            type ComboPopup interface implemented by BasicComboPopup
 -- listBox         type JXList<?>
 - arrowButton      type JButton
 - editor           type Component ... interface ComboBoxEditor implemented by BasicComboBoxEditor
                      in BasicComboBoxEditor EditorComponent is JTextField
 - currentValuePane type class javax.swing.CellRendererPane extends Container implements Accessible

 */
    	popup = createPopup(); // creates ComboPopup with listBox which is actually of type JXList
    	listBox = popup.getList();
    	LOG.config("listBox:"+listBox);
    	if(listBox instanceof JXList<?>) {
    		JXList<?> xListBox = (JXList<?>)listBox;
        	xListBox.setCellRenderer(new DefaultListRenderer());
        	xListBox.setRolloverEnabled(true);
    	} else if(listBox instanceof JYList<?>) {
    		JYList<?> yListBox = (JYList<?>)listBox;
//        	LOG.info("----+++---> UI delegate for "+c
//        			+ "\n interface ComboPopup:"+popup
//        			+ "\n popup.JList<Object>:"+yListBox
//        			);
        	yListBox.setCellRenderer(new DefaultListCellRenderer());
    	}

        // Is this combo box a cell editor?
        Boolean inTable = (Boolean)c.getClientProperty(IS_TABLE_CELL_EDITOR);
        if (inTable != null) {
            isTableCellEditor = inTable.equals(Boolean.TRUE) ? true : false;
        }

        if ( comboBox.getRenderer() == null || comboBox.getRenderer() instanceof UIResource ) {
            comboBox.setRenderer( createRenderer() );
        }
        installComponents();

        // 4.+5. install event listeners
        installListeners();
        
        comboBox.setRequestFocusEnabled( true );
        installKeyboardActions();
        comboBox.putClientProperty(PROP_DONT_CANCEL_POPUP, HIDE_POPUP_KEY);
       
        if (keySelectionManager == null || keySelectionManager instanceof UIResource) {
            keySelectionManager = new DefaultKeySelectionManager();
        }
        comboBox.setKeySelectionManager(keySelectionManager);
    }

    @Override
    public void uninstallUI(JComponent c) {
        setPopupVisible(comboBox, false);
        popup.uninstallingUI();

        uninstallKeyboardActions();

        comboBox.setLayout( null );

        uninstallComponents();
        uninstallListeners();
        uninstallDefaults();

        if (comboBox.getRenderer() == null || comboBox.getRenderer() instanceof UIResource) {
            comboBox.setRenderer(null);
        }

        ComboBoxEditor comboBoxEditor = comboBox.getEditor();
        if (comboBoxEditor instanceof UIResource) {
            if (comboBoxEditor.getEditorComponent().hasFocus()) {
                // Leave focus in JComboBox.
                comboBox.requestFocusInWindow();
            }
            comboBox.setEditor( null );
        }

        if (keySelectionManager instanceof UIResource) {
            comboBox.setKeySelectionManager(null);
        }

        handler = null;
        keyListener = null;
        focusListener = null;
        listDataListener = null;
        propertyChangeListener = null;
        popup = null;
        listBox = null;
        comboBox = null;
    }

    /* aus javax.swing.plaf.basic.BasicLookAndFeel
            // *** ComboBox
            "ComboBox.font", sansSerifPlain12,
            "ComboBox.background", window,   //   "window", "#FFFFFF" ???
            "ComboBox.foreground", textText, // "textText", "#000000" ???
            "ComboBox.buttonBackground", control,
            "ComboBox.buttonShadow", controlShadow,
            "ComboBox.buttonDarkShadow", controlDkShadow,
            "ComboBox.buttonHighlight", controlLtHighlight,
            "ComboBox.selectionBackground", textHighlight,
            "ComboBox.selectionForeground", textHighlightText,
            "ComboBox.disabledBackground", control,
            "ComboBox.disabledForeground", textInactiveText,
            "ComboBox.timeFactor", oneThousand,
            "ComboBox.isEnterSelectablePopup", Boolean.FALSE,
            "ComboBox.ancestorInputMap",
               new UIDefaults.LazyInputMap(new Object[] {
                      "ESCAPE", "hidePopup",
                     "PAGE_UP", "pageUpPassThrough",
                   "PAGE_DOWN", "pageDownPassThrough",
                        "HOME", "homePassThrough",
                         "END", "endPassThrough",
                       "ENTER", "enterPressed"
                 }),
            "ComboBox.noActionOnKeyNavigation", Boolean.FALSE,
     */
    private static final String PROPERTY_PREFIX = "ComboBox" + ".";
    protected static final String FONT = PROPERTY_PREFIX + "font";
    protected static final String BACKGROUND = PROPERTY_PREFIX + "background";
    protected static final String FOREGROUND = PROPERTY_PREFIX + "foreground";
    protected static final String BORDER = PROPERTY_PREFIX + "border";
    protected static final String SELECTION_BG = PROPERTY_PREFIX + "selectionBackground";
    protected static final String SELECTION_FG = PROPERTY_PREFIX + "selectionForeground";
    protected static final String TIME_FACTOR = PROPERTY_PREFIX + "timeFactor";
    protected static final String SQUARE_BUTTON = PROPERTY_PREFIX + "squareButton";
    protected static final String PADDING = PROPERTY_PREFIX + "padding";
    protected static final String DISABLED_BG = PROPERTY_PREFIX + "disabledBackground";
    protected static final String DISABLED_FG = PROPERTY_PREFIX + "disabledForeground";
    // usw. props TODO
    /**
     * Install default property values for color, fonts, borders, icons, opacity, etc. on the component. 
     * Whenever possible, property values initialized by the client program should not be overridden. 
     */
    protected void installDefaults() {
		LOG.fine("LookAndFeelDefaults "+UIManager.get(comboBox.getUIClassID())
			+ "\n font "+UIManager.getLookAndFeelDefaults().get(FONT)
			+ "\n background "+UIManager.getLookAndFeelDefaults().get(BACKGROUND)
			+ "\n foreground "+UIManager.getLookAndFeelDefaults().get(FOREGROUND)
			+ "\n border "+UIManager.getLookAndFeelDefaults().get(BORDER)
			+ "\n property opaque "+UIManager.getLookAndFeelDefaults().get("opaque")
			+ "\n selectionBackground "+UIManager.getLookAndFeelDefaults().get(SELECTION_BG)
			+ "\n selectionForeground "+UIManager.getLookAndFeelDefaults().get(SELECTION_FG)
			+ "\n disabledBackground "+UIManager.getLookAndFeelDefaults().get(DISABLED_BG)
			+ "\n disabledForeground "+UIManager.getLookAndFeelDefaults().get(DISABLED_FG)
			+ "\n property timeFactor "+UIManager.getLookAndFeelDefaults().get(TIME_FACTOR)
			+ "\n property squareButton "+UIManager.getLookAndFeelDefaults().get(SQUARE_BUTTON)
			+ "\n padding "+UIManager.getLookAndFeelDefaults().get(PADDING));
/* results for Metal/Steel:
INFORMATION: LookAndFeelDefaults org.jdesktop.swingx.plaf.metal.MetalXComboBoxUI
 font javax.swing.plaf.FontUIResource[family=Dialog,name=Dialog,style=plain,size=12]
 background javax.swing.plaf.ColorUIResource[r=204,g=204,b=204] 0xCCCCCC secondary3
 foreground sun.swing.PrintColorUIResource[r=51,g=51,b=51] 0x333333
 border javax.swing.border.EtchedBorder@5a9f5d44
 property opaque null
 selectionBackground javax.swing.plaf.ColorUIResource[r=163,g=184,b=204] 0xA3B8CC
 selectionForeground sun.swing.PrintColorUIResource[r=51,g=51,b=51] 0x333333
 disabledBackground javax.swing.plaf.ColorUIResource[r=238,g=238,b=238]
 disabledForeground javax.swing.plaf.ColorUIResource[r=184,g=207,b=229]
 property timeFactor 1000
 property squareButton null
 padding null

  results for Metal/Ocean:
INFORMATION: LookAndFeelDefaults org.jdesktop.swingx.plaf.metal.MetalXComboBoxUI
 font javax.swing.plaf.FontUIResource[family=Dialog,name=Dialog,style=plain,size=12]
 background javax.swing.plaf.ColorUIResource[r=238,g=238,b=238] 0xEEEEEE SECONDARY3
 foreground sun.swing.PrintColorUIResource[r=51,g=51,b=51] 0x333333 CONTROL_TEXT_COLOR,OCEAN_BLACK
 border javax.swing.border.EtchedBorder@5a9f5d44
 property opaque null
 selectionBackground javax.swing.plaf.ColorUIResource[r=163,g=184,b=204] 0xA3B8CC PRIMARY2
 selectionForeground sun.swing.PrintColorUIResource[r=51,g=51,b=51] 0x333333 CONTROL_TEXT_COLOR,OCEAN_BLACK
 disabledBackground javax.swing.plaf.ColorUIResource[r=238,g=238,b=238] 0xEEEEEE SECONDARY3
 disabledForeground javax.swing.plaf.ColorUIResource[r=184,g=207,b=229] 0xB8CFE5 PRIMARY3 SECONDARY2
 property timeFactor 1000
 property squareButton null
 padding null
 */
        LookAndFeel.installColorsAndFont(comboBox, BACKGROUND, FOREGROUND, FONT);
        LookAndFeel.installBorder(comboBox, BORDER);
        LookAndFeel.installProperty( comboBox, "opaque", Boolean.TRUE); // nicht "ComboBox.opaque" !

        Long l = (Long)UIManager.get(TIME_FACTOR);
        timeFactor = l == null ? 1000L : l.longValue();

        //NOTE: this needs to default to true if not specified
        Boolean b = (Boolean)UIManager.get(SQUARE_BUTTON);
        squareButton = b == null ? true : b;

        padding = UIManager.getInsets(PADDING); // Insets for currently selected item
    }
    protected void uninstallDefaults() {
        LookAndFeel.installColorsAndFont(comboBox, BACKGROUND, FOREGROUND, FONT);
        LookAndFeel.uninstallBorder(comboBox);
    }
    
    protected Insets getInsets() {
        return comboBox.getInsets();
    }

    /**
     * Create and install event listeners. 
     * Including a PropertyChangeListener in order to detect and respond to property changes appropriately
     */
    // die listener (aka observer) werden comboBox und nicht der UI zugeordnet,
    // siehe protected EventListenerList listenerList in JComponent.
    // Ausnahme : ListDataListener
    protected void installListeners() {
        if ( (itemListener = createItemListener()) != null) {
            comboBox.addItemListener( itemListener );
        }
        if ( (propertyChangeListener = getPropertyChangeListener()) != null) {
            comboBox.addPropertyChangeListener( propertyChangeListener );
        }
        
        // keyListener, focusListener sind in Handler implementiert:
        if ( (keyListener = createKeyListener()) != null ) {
            comboBox.addKeyListener( keyListener );
        }
        if ( (focusListener = createFocusListener()) != null ) {
            comboBox.addFocusListener( focusListener );
        }
        
        if ((popupMouseListener = popup.getMouseListener()) != null) {
            comboBox.addMouseListener( popupMouseListener );
        }
        if ((popupMouseMotionListener = popup.getMouseMotionListener()) != null) {
            comboBox.addMouseMotionListener( popupMouseMotionListener );
        }
        if ((popupKeyListener = popup.getKeyListener()) != null) {
            comboBox.addKeyListener(popupKeyListener);
        }

        ComboBoxModel<?> model = comboBox.getModel();
        if (model != null) {
            LOG.config("Do not add any ListDataListener for "+model);
        }
    }
    protected void uninstallListeners() {
        if ( keyListener != null ) {
            comboBox.removeKeyListener( keyListener );
        }
        if ( itemListener != null) {
            comboBox.removeItemListener( itemListener );
        }
        if ( propertyChangeListener != null ) {
            comboBox.removePropertyChangeListener( propertyChangeListener );
        }
        if ( focusListener != null) {
            comboBox.removeFocusListener( focusListener );
        }
        if ( popupMouseListener != null) {
            comboBox.removeMouseListener( popupMouseListener );
        }
        if ( popupMouseMotionListener != null) {
            comboBox.removeMouseMotionListener( popupMouseMotionListener );
        }
        if (popupKeyListener != null) {
            comboBox.removeKeyListener(popupKeyListener);
        }
        if ( comboBox.getModel() != null ) {
            if ( listDataListener != null ) {
                comboBox.getModel().removeListDataListener( listDataListener );
            }
        }
    }

    // used in subclass MetalXComboBoxUI and Synth*
    protected ComboPopup createPopup() {
    	return new BasicXComboPopup(comboBox);
    }

    protected ListCellRenderer<Object> createRenderer() {
    	return new DefaultComboBoxRenderer<>();
    }

    protected ComboBoxEditor createEditor() {
//    	LOG.info("call new BasicXComboBoxEditor.UIResource()");
        return new BasicXComboBoxEditor.UIResource();
    }

    //===============================
    // begin Sub-Component Management
    //
    
    /**
     * Creates and initializes the components which make up the aggregate combo box. 
     * This method is called as part of the UI installation process {@code installUI}.
     */
    protected void installComponents() {
    	installButton((Icon)null);
        if ( comboBox.isEditable() ) {
            addEditor();
        }
        comboBox.add(currentValuePane);
    }
    /**
     * {@inheritDoc} <p>
     * This method is called as part of the UI installation process {@code installUI}.
     */
    @Override
    public void installButton(Icon i) {
        arrowButton = createComboButton(i);
        if (arrowButton != null)  {
            comboBox.add(arrowButton);
            configureArrowButton();
        }
    }
    @Override
    public void setIsShowingPopupIcon(Icon i) {
    	isShowingPopupIcon = i;
    }
    public void installButton(BasicXComboBoxUI oldUI) {
    	installButton(oldUI.icon);
    	setIsShowingPopupIcon(oldUI.isShowingPopupIcon);
    }
    /**
     * The aggregate components which comprise the combo box are unregistered and uninitialized. 
     * This method is called as part of the UI uninstallation process {@code uninstallUI}.
     */
    protected void uninstallComponents() {
    	uninstallButton();
        if (editor != null) {
            unconfigureEditor();
        }
        comboBox.removeAll(); // Just to be safe.
    }
    /**
     * {@inheritDoc} <p>
     * Removes the {@code arrowButton} and registered Listener.
     * This method is called as part of the UI installation process {@code uninstallUI}.
     */
    @Override
    public void uninstallButton() {
        if (arrowButton != null) {
        	comboBox.remove(arrowButton);
            arrowButton.removeMouseListener(popup.getMouseListener());
            arrowButton.removeMouseMotionListener(popup.getMouseMotionListener());
            arrowButton = null;
        }
    }

    /**
     * Creates a button which will be used as the control to show or hide
     * the popup portion of the combo box.
     * <br>
     * In Metal and Nimbus (synth) this method is overridden.
     *
     * @return a button which represents the popup control
     */
    // code identical to BasicComboBox#createArrowButton
    protected JButton createArrowButton() {
    	JButton button = new BasicArrowButton(SwingConstants.SOUTH,
                UIManager.getColor("ComboBox.buttonBackground"),
                UIManager.getColor("ComboBox.buttonShadow"),
                UIManager.getColor("ComboBox.buttonDarkShadow"),
                UIManager.getColor("ComboBox.buttonHighlight"));
        button.setName("ComboBox.arrowButton");
        return button;
    }
    /* warum createComboButton und createArrowButton?
     * Der Begriff "ArrowButton" stammt aus javax.swing.plaf.basic.BasicComboBoxUI,
     * dort ist JButton arrowButton definiert.
     * In swingx habe ich den Begriff übernommen, auch wenn es nicht immer ein "Arrow" ist.
     * Denn das icon ist frei wählbar.
     *  createArrowButton() entspricht createComboButton(null) 
     */
    protected JButton createComboButton(Icon i) {
    	icon = i==null ? UIManager.getIcon("ComboBox.icon") : i;
    	// user defined button should be square
    	if(i!=null) squareButton = true;

    	JButton button;
    	if(icon==null) {
    		button = createArrowButton();
    	} else {
    		button = new JXButton(icon);
    		button.setBackground(UIManager.getColor("ComboBox.buttonBackground"));
    	}
        button.setName("ComboBox.arrowButton");
//        button.addActionListener( ae -> {
//        	LOG.info("hasFocus="+hasFocus);
//        	if (!isPopupVisible(comboBox)) {
//            	LOG.info("is NOT PopupVisible UND hasFocus="+hasFocus);
/* dies ist ein misslungener Versuch 
 * den buttonicon beim Zuklappen in die andere Richtung (SOUTH) zu setzen
 * Ausgangslage: 
 * - die popup Box ist ausgeklappt (hasFocus==true , button direction NORTH)
 * - es wird kein listenelement ausgewählt, sondern auf die combobox geklickt
 * - dadurch klappt die popup Box zu
 * - ALLERDINGS: buttonicon bleibt in direction NORTH
 * - erst beim Klick ausserhalb der ComboBox (focusLost) springt buttonicon nach SOUTH
 *
        		this.getHandler().focusLost(null);
 */
//        		new FocusEvent(comboBox, FocusEvent.FOCUS_LOST, false, comboBox.getParent(), FocusEvent.Cause.ROLLBACK);
//        		new FocusEvent(comboBox.getParent(), FocusEvent.FOCUS_GAINED, false, comboBox, FocusEvent.Cause.ROLLBACK);
//        	}
//        });
        return button;
    }
    
    // code identical to public BasicComboBox#configureArrowButton
    private void configureArrowButton() {
        if (arrowButton != null) {
            arrowButton.setEnabled(comboBox.isEnabled());
            arrowButton.setFocusable(comboBox.isFocusable());
            arrowButton.setRequestFocusEnabled(false);
            arrowButton.addMouseListener(popup.getMouseListener());
            arrowButton.addMouseMotionListener( popup.getMouseMotionListener() );
            arrowButton.resetKeyboardActions();
            arrowButton.putClientProperty(PROP_DONT_CANCEL_POPUP, HIDE_POPUP_KEY);
            arrowButton.setInheritsPopupMenu(true);
        }
    }

    // code identical to public BasicComboBox#addEditor
    private void addEditor() {
        removeEditor();
        // in BasicComboBoxEditor EditorComponent is JTextField, a Component
        editor = comboBox.getEditor().getEditorComponent();
        if (editor != null) {
            configureEditor();
            comboBox.add(editor);
            if(comboBox.isFocusOwner()) {
                // Switch focus to the editor component
                editor.requestFocusInWindow();
            }
        }
    }
    // code identical to public BasicComboBox#removeEditor
    private void removeEditor() {
        if (editor != null) {
            unconfigureEditor();
            comboBox.remove(editor);
            editor = null;
        }
    }
    // code identical to public BasicComboBoxUI#configureEditor
    private void configureEditor() {
        // Should be in the same state as the combobox
        editor.setEnabled(comboBox.isEnabled());
        
        editor.setFocusable(comboBox.isFocusable());
        editor.setFont(comboBox.getFont());

        if (focusListener != null) {
            editor.addFocusListener(focusListener);
        }
        editor.addFocusListener(getHandler());
        
        // ---
        ComboBoxEditor cbcbe = comboBox.getEditor();
        LOG.config(
         "\n comboBox.editor:"+cbcbe.getClass() + (cbcbe instanceof ComboBoxEditor ? " (instanceof ComboBoxEditor)" : "")
        +"\n     this.editor:"+editor.getClass() + (editor instanceof ComboBoxEditor ? " (instanceof ComboBoxEditor)" : "")
        );
        // ---
        comboBox.getEditor().addActionListener( ae -> {
        /* wieso nicht editor.addActionListener(getHandler()); ?
         * this.editor ist vom Typ Component,
         * comboBox.getEditor() liefert dagegen interface ComboBoxEditor.
         * Im interface ist addActionListener definiert,
         * im Component nicht!
         */
        	LOG.fine("lambda ActionEvent:"+ae);
            Object item = comboBox.getEditor().getItem();
            if (item != null) {
                if (!comboBox.isPopupVisible() && !item.equals(comboBox.getSelectedItem())) {
                    comboBox.setSelectedItem(comboBox.getEditor().getItem());
                }
                ActionMap am = comboBox.getActionMap();
                if (am != null) {
                    Action action = am.get("enterPressed");
                    if (action != null) {
                    	ActionEvent actionEvent =
                    	new ActionEvent(comboBox, ae.getID(), ae.getActionCommand(), ae.getModifiers());
                    	LOG.config("actionEvent:"+actionEvent);
                        action.actionPerformed(actionEvent);
                    }
                }
            }
        });
//        if(editor instanceof ComboBoxEditor) {
//        	ComboBoxEditor cbe = (ComboBoxEditor)editor;
//        	LOG.info("xxxxxxxxxx>>"+cbe);
////        	cbe.addActionListener(getHandler());
////        	// macht: anEditor.setItem(anItem):
////        	comboBox.configureEditor(cbe, comboBox.getSelectedItem());
//        } else {
//        	LOG.info("\n!!!editor:"+editor // BasicComboBoxEditor$BorderlessTextField
//        			+"\n!!!comboBox.getEditor():"+comboBox.getEditor() // BasicComboBoxEditor
//        			);
//        }

        if(editor instanceof JComponent) {
        	JComponent jc = (JComponent)editor;
            jc.putClientProperty(PROP_DONT_CANCEL_POPUP, HIDE_POPUP_KEY);
            jc.setInheritsPopupMenu(true);
        }

        // macht: anEditor.setItem(anItem)
        comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());

        editor.addPropertyChangeListener(propertyChangeListener);
    }
    
    // code identical to public BasicComboBox#configureEditor
    private void unconfigureEditor() {
        if (focusListener != null) {
            editor.removeFocusListener(focusListener);
        }
        editor.removePropertyChangeListener(propertyChangeListener);
// ActionListener als lambda ==> kein remove
//        comboBox.getEditor().removeActionListener(getHandler());
        editor.removeFocusListener(getHandler());
    }
    
    //
    // end Sub-Component Management
    //===============================

    // Synchronizes the ToolTip text for the components within the combo box 
    // to be the same value as the combo box ToolTip text.
    // copied from javax.swing.plaf.basic.BasicComboBoxUI.updateToolTipTextForChildren
    private void synchronizeToolTipTextForChildren() {
        Component[] children = comboBox.getComponents();
        for ( int i = 0; i < children.length; ++i ) {
            if ( children[i] instanceof JComponent ) {
                ((JComponent)children[i]).setToolTipText( comboBox.getToolTipText() );
            }
        }
    }

    //================================
    // begin XComboBoxUI Implementation
    //

	@Override
	public boolean isPopupVisible(JComboBox<?> c) {
        return popup != null && popup.isVisible();
	}

    /**
     * Shows or hides the popup depending on visibilty
     * @param c a {@code JComboBox} (not used)
     * @param v a {@code boolean} determining the visibilty of the popup
     */
	@Override
	public void setPopupVisible(JComboBox<?> c, boolean v) {
        if (popup != null) {
        	LOG.fine("popup "+(v?"show":"hide")+" for "+c);
            ((BasicXComboPopup)popup).setPopupVisible(v);
        }
	}
	protected void setButtonDirection() {
        if (hasFocus) {
            if(arrowButton instanceof BasicArrowButton) {
            	BasicArrowButton basicArrowButton = (BasicArrowButton)arrowButton;
	            basicArrowButton.setDirection(SwingConstants.NORTH);
            } else if(arrowButton instanceof org.jdesktop.swingx.plaf.synth.SynthArrowButton) {
            	org.jdesktop.swingx.plaf.synth.SynthArrowButton synthArrowButton = (org.jdesktop.swingx.plaf.synth.SynthArrowButton)arrowButton;
            	synthArrowButton.setDirection(SwingConstants.NORTH);
            } else if(arrowButton!=null) {
            	arrowButton.setIcon(isShowingPopupIcon==null?icon:isShowingPopupIcon);
            }
        } else {
            if(arrowButton instanceof BasicArrowButton) {
            	BasicArrowButton basicArrowButton = (BasicArrowButton)arrowButton;
	            basicArrowButton.setDirection(SwingConstants.SOUTH);
            } else if(arrowButton instanceof org.jdesktop.swingx.plaf.synth.SynthArrowButton) {
            	org.jdesktop.swingx.plaf.synth.SynthArrowButton synthArrowButton = (org.jdesktop.swingx.plaf.synth.SynthArrowButton)arrowButton;
            	synthArrowButton.setDirection(SwingConstants.SOUTH);
            } else if(arrowButton!=null) {
            	arrowButton.setIcon(icon);
            }
        }
	}

	@Override
	public boolean isFocusTraversable(JComboBox<?> c) {
        return !comboBox.isEditable();
	}

    //
    // end ComboBoxUI Implementation
    //==============================

    //=================================
    // begin ComponentUI Implementation
	
    @Override
    public void paint(Graphics g, JComponent c) {
        hasFocus = comboBox.hasFocus();
        if (!comboBox.isEditable()) {
            Rectangle r = rectangleForCurrentValue();
            paintCurrentValueBackground(g,r,hasFocus);
            paintCurrentValue(g,r,hasFocus);
        }
        // Empty out the renderer pane, allowing renderers to be gc'ed.
        currentValuePane.removeAll();
    }

    /**
     * @return the area that is reserved for drawing the currently selected item
     */
    protected Rectangle rectangleForCurrentValue() {
        int width = comboBox.getWidth();
        int height = comboBox.getHeight();
        Insets insets = getInsets();
        int buttonSize = height - (insets.top + insets.bottom);
        if (arrowButton != null) {
            buttonSize = arrowButton.getWidth();
        }
        if(comboBox.getComponentOrientation().isLeftToRight()) {
            return new Rectangle(insets.left, insets.top,
                         width - (insets.left + insets.right + buttonSize),
                         height - (insets.top + insets.bottom));
        }
        else {
            return new Rectangle(insets.left + buttonSize, insets.top,
                         width - (insets.left + insets.right + buttonSize),
                         height - (insets.top + insets.bottom));
        }
    }

    private boolean sameBaseline;
    /**
     * Returns the baseline.
     *
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @see javax.swing.JComponent#getBaseline(int, int)
     * @since 1.6
     */
    @Override
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        int baseline = -1;
        // force sameBaseline to be updated.
        getDisplaySize();
        if (sameBaseline) {
            Insets insets = c.getInsets();
            height = Math.max(height - insets.top - insets.bottom, 0);
            if (!comboBox.isEditable()) {
                ListCellRenderer<Object> renderer = (ListCellRenderer<Object>)comboBox.getRenderer();
                if (renderer == null)  {
                    renderer = new DefaultListCellRenderer(); // implements ListCellRenderer<Object>
                }
                Object value = null;
                Object prototypeValue = comboBox.getPrototypeDisplayValue();
                if (prototypeValue != null)  {
                    value = prototypeValue;
                }
                else if (comboBox.getModel().getSize() > 0) {
                    // Note, we're assuming the baseline is the same for all
                    // cells, if not, this needs to loop through all.
                    value = comboBox.getModel().getElementAt(0);
                }
                Component component = renderer.getListCellRendererComponent(listBox, value, -1, false, false);
                if (component instanceof JLabel) {
                    JLabel label = (JLabel) component;
                    String text = label.getText();
                    if ((text == null) || text.isEmpty()) {
                        label.setText(" ");
                    }
                }
                if (component instanceof JComponent) {
                    component.setFont(comboBox.getFont());
                }
                baseline = component.getBaseline(width, height);
            }
            else {
                baseline = editor.getBaseline(width, height);
            }
            if (baseline > 0) {
                baseline += insets.top;
            }
        }
        return baseline;
    }

    /**
     * Returns an enum indicating how the baseline of the component
     * changes as the size changes.
     *
     * @throws NullPointerException {@inheritDoc}
     * @see javax.swing.JComponent#getBaseline(int, int)
     * @since 1.6
     */
    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(JComponent c) {
        super.getBaselineResizeBehavior(c);
        // Force sameBaseline to be updated.
        getDisplaySize();
        if (comboBox.isEditable()) {
            return editor.getBaselineResizeBehavior();
        }
        else if (sameBaseline) {
            ListCellRenderer<Object> renderer = (ListCellRenderer<Object>)comboBox.getRenderer();
            if (renderer == null)  {
                renderer = new DefaultListCellRenderer();
            }
            Object value = null;
            Object prototypeValue = comboBox.getPrototypeDisplayValue();
            if (prototypeValue != null)  {
                value = prototypeValue;
            }
            else if (comboBox.getModel().getSize() > 0) {
                // Note, we're assuming the baseline is the same for all
                // cells, if not, this needs to loop through all.
                value = comboBox.getModel().getElementAt(0);
            }
            if (value != null) {
                Component component = renderer.getListCellRendererComponent(listBox, value, -1, false, false);
                return component.getBaselineResizeBehavior();
            }
        }
        return Component.BaselineResizeBehavior.OTHER;
    }

    // This is currently hacky...
    @Override
    public int getAccessibleChildrenCount(JComponent c) {
        if (comboBox.isEditable()) {
            return 2;
        }
        else {
            return 1;
        }
    }

    // This is currently hacky...
    @Override
    public Accessible getAccessibleChild(JComponent c, int i) {
        // 0 = the popup
        // 1 = the editor
        switch ( i ) {
        case 0:
            if (popup instanceof Accessible) {
                AccessibleContext ac = ((Accessible) popup).getAccessibleContext();
                ac.setAccessibleParent(comboBox);
                return(Accessible) popup;
            }
            break;
        case 1:
            if (comboBox.isEditable() && (editor instanceof Accessible)) {
                AccessibleContext ac = ((Accessible) editor).getAccessibleContext();
                ac.setAccessibleParent(comboBox);
                return(Accessible) editor;
            }
            break;
        }
        return null;
    }

    //
    // end ComponentUI Implementation
    //===============================

    //===============================
    // begin Painting Utility Methods
    //

    /**
     * Paints the currently selected item.
     *
     * @param g an instance of {@code Graphics}
     * @param bounds a bounding rectangle to render to
     * @param hasFocus is focused
     */
    public void paintCurrentValue(Graphics g, Rectangle bounds, boolean hasFocus) {
        ListCellRenderer<Object> renderer = (ListCellRenderer<Object>)comboBox.getRenderer();
        Component c;

        if (hasFocus && !isPopupVisible(comboBox)) {
            LOG.fine("this.hasFocus && Popup NOT Visible, renderer:"+renderer);
            c = renderer.getListCellRendererComponent( listBox,
                                                       comboBox.getSelectedItem(),
                                                       -1,
                                                       true, // isSelected 
                                                       hasFocus ); // cellHasFocus
        } else {
            LOG.fine("this.hasFocus="+hasFocus+" || Popup Visible renderer:"+renderer);
            c = renderer.getListCellRendererComponent( listBox,
                                                       comboBox.getSelectedItem(),
                                                       -1,
                                                       false,
                                                       hasFocus );
            c.setBackground(UIManager.getColor(BACKGROUND));
        }
        c.setFont(comboBox.getFont());
        if (hasFocus && !isPopupVisible(comboBox)) {
            c.setForeground(listBox.getSelectionForeground());
            c.setBackground(listBox.getSelectionBackground());
        } else {
            if (comboBox.isEnabled()) {
                c.setForeground(comboBox.getForeground());
                c.setBackground(comboBox.getBackground());
            } else {
            	c.setForeground(UIManager.getColor(DISABLED_FG));
            	c.setBackground(UIManager.getColor(DISABLED_BG));
            }
        }

        // Fix for 4238829: should lay out the JPanel.
//        LOG.info("!!!!!!!! c:"+c);
        boolean shouldValidate = false;
        if (c instanceof JPanel)  {
            shouldValidate = true;
        }

        int x = bounds.x, y = bounds.y, w = bounds.width, h = bounds.height;
        if (padding != null) {
            x = bounds.x + padding.left;
            y = bounds.y + padding.top;
            w = bounds.width - (padding.left + padding.right);
            h = bounds.height - (padding.top + padding.bottom);
        }

        currentValuePane.paintComponent(g,c,comboBox,x,y,w,h,shouldValidate);
        LOG.exiting("BasicXComboBoxUI", "paintCurrentValue");
    }

    /**
     * Paints the background of the currently selected item.
     *
     * @param g an instance of {@code Graphics}
     * @param bounds a bounding rectangle to render to
     * @param hasFocus is focused
     */
    public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
        Color t = g.getColor();
        g.setColor(UIManager.getColor(comboBox.isEnabled() ? BACKGROUND : DISABLED_BG));
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height); // Fills rectangle with bg color
        g.setColor(t);
    }

    /**
     * Repaint the currently selected item.
     */
    void repaintCurrentValue() {
        Rectangle r = rectangleForCurrentValue();
        comboBox.repaint(r.x, r.y, r.width, r.height);
    }

    //
    // end Painting Utility Methods
    //=============================

    //===============================
    // begin Size Utility Methods
    //

    @Override
    public Dimension getPreferredSize( JComponent c ) {
        return getMinimumSize(c);
    }

    protected boolean isMinimumSizeDirty = true;
    protected Dimension cachedMinimumSize = new Dimension( 0, 0 );
    @Override
    public Dimension getMinimumSize( JComponent c ) {
        if (!isMinimumSizeDirty) {
            return new Dimension(cachedMinimumSize);
        }
        // The minimum size is the size of the display area plus insets plus the button.
        Dimension size = getDisplaySize();
        Insets insets = getInsets();
        //calculate the width and height of the button
        int buttonHeight = size.height;
        int buttonWidth = buttonHeight;
        if(!squareButton) {
            buttonWidth = arrowButton==null ? 16 : arrowButton.getPreferredSize().width;
        }
        //adjust the size based on the button width
        size.height += insets.top + insets.bottom;
        size.width +=  insets.left + insets.right + buttonWidth;

        cachedMinimumSize.setSize( size.width, size.height );
        isMinimumSizeDirty = false;

        return new Dimension(size);
    }

    @Override
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }
    
    // Used for calculating the default size.
    private static ListCellRenderer<Object> getDefaultListCellRenderer() {
    	return new YListCellRenderer();
    }
    /**
     * Return the default size of an empty display area of the combo box using
     * the current renderer and font.
     *
     * @return the size of an empty display area
     * @see #getDisplaySize
     */
    protected Dimension getDefaultSize() {
        // Calculates the height and width using the default text renderer
        Component comp = getDefaultListCellRenderer().getListCellRendererComponent(listBox, " ", -1, false, false);
        Dimension d = getSizeForComponent(comp);
        LOG.config("DefaultSize is "+d+", Component to calculate size:"+comp);

        return new Dimension(d.width, d.height);
    }
    /**
     * Returns the calculated size of the display area. The display area is the
     * portion of the combo box in which the selected item is displayed. This
     * method will use the prototype display value if it has been set.
     * <p>
     * For combo boxes with a non trivial number of items, it is recommended to
     * use a prototype display value to significantly speed up the display
     * size calculation.
     *
     * @return the size of the display area calculated from the combo box items
     * @see javax.swing.JComboBox#setPrototypeDisplayValue
     */
    private boolean isDisplaySizeDirty = true;
    private Dimension cachedDisplaySize = new Dimension( 0, 0 );
    protected Dimension getDisplaySize() {
//    	LOG.info("isDisplaySizeDirty="+isDisplaySizeDirty + " cachedDisplaySize="+cachedDisplaySize + "modelSize="+ comboBox.getModel().getSize());
        if (!isDisplaySizeDirty)  {
            return new Dimension(cachedDisplaySize);
        }
        Dimension result = new Dimension();

        ListCellRenderer<Object> renderer = (ListCellRenderer<Object>)comboBox.getRenderer();
        if (renderer == null)  {
            renderer = new DefaultListCellRenderer();
        }

        sameBaseline = true;

        Object prototypeValue = comboBox.getPrototypeDisplayValue();
        if (prototypeValue != null)  {
            // Calculates the dimension based on the prototype value
            result = getSizeForComponent(
            	renderer.getListCellRendererComponent(listBox, prototypeValue, -1, false, false)
            	);
        } else {
            // Calculate the dimension by iterating over all 
        	// the elements in the combo box list.
            ComboBoxModel<Object> model = (ComboBoxModel<Object>)comboBox.getModel();
            int modelSize = model.getSize();
            int baseline = -1;
            Dimension d;

            if (modelSize > 0 ) {
            	LOG.fine("Calculates the maximum height and width based on the largest element modelSize="+modelSize);
                for (int i = 0; i < modelSize ; i++ ) {
                    // Calculates the maximum height and width based on the largest element
                    Object value = model.getElementAt(i);
                    Component c = renderer.getListCellRendererComponent(listBox, value, -1, false, false);
                    d = getSizeForComponent(c);
                    if (sameBaseline && value != null &&
                            (!(value instanceof String) || !"".equals(value))) {
                        int newBaseline = c.getBaseline(d.width, d.height);
                        if (newBaseline == -1) {
                            sameBaseline = false;
                        }
                        else if (baseline == -1) {
                            baseline = newBaseline;
                        }
                        else if (baseline != newBaseline) {
                            sameBaseline = false;
                        }
                    }
                    result.width = Math.max(result.width,d.width);
                    result.height = Math.max(result.height,d.height);
                }
            	LOG.fine("Calculates the maximum height and width based on the largest element result="+result);
            } else {
                result = getDefaultSize();
                if (comboBox.isEditable()) {
                    result.width = 100;
                }
            }
        }

        if (comboBox.isEditable()) {
            Dimension d = editor.getPreferredSize();
            result.width = Math.max(result.width,d.width);
            result.height = Math.max(result.height,d.height);
        }

        // calculate in the padding
        if (padding != null) {
            result.width += padding.left + padding.right;
            result.height += padding.top + padding.bottom;
        }

        // Set the cached value
        cachedDisplaySize.setSize(result.width, result.height);
        isDisplaySizeDirty = false;

        return result;
    }

    /**
     * Returns the size a component would have if used as a cell renderer.
     *
     * @param comp a {@code Component} to check
     * @return size of the component
     * @since 1.7
     */
    protected Dimension getSizeForComponent(Component comp) {
        // This has been refactored out in hopes that it may be investigated and
        // simplified for the next major release. adding/removing
        // the component to the currentValuePane and changing the font may be
        // redundant operations.
        currentValuePane.add(comp);
        comp.setFont(comboBox.getFont());
        Dimension d = comp.getPreferredSize();
        currentValuePane.remove(comp);
        return d;
    }

    //
    // end Size Utility Methods
    //=============================

    //=================================
    // begin Keyboard Action Management
    //
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
        	return (InputMap)UIManager.get("ComboBox.ancestorInputMap");
//            return (InputMap)DefaultLookup.get(comboBox, this,
//                                               "ComboBox.ancestorInputMap");
        }
        return null;
    }
    // copied from javax.swing.plaf.basic.BasicComboBoxUI with modifications
    protected void installKeyboardActions() {
        InputMap km = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(comboBox, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, km);
        LazyActionMap.installLazyActionMap(comboBox, BasicXComboBoxUI.class, // BasicComboBoxUI modified XXX
                                           "ComboBox.actionMap");
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIInputMap(comboBox, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, null);
        SwingUtilities.replaceUIActionMap(comboBox, null);
    }
    //
    // end Keyboard Utility Methods
    //=============================

    boolean isTableCellEditor() {
        return isTableCellEditor;
    }

    private boolean isNavigationKey(int keyCode, int modifiers) {
        InputMap inputMap = comboBox.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        KeyStroke key = KeyStroke.getKeyStroke(keyCode, modifiers);

        if (inputMap != null && inputMap.get(key) != null) {
            return true;
        }
        return false;
    }

    /**
     * This layout manager handles the 'standard' layout of combo boxes.
     * It puts the arrow button to the right and the editor to the left.
     * If there is no editor it still keeps the arrow button to the right.
     */
    protected class XComboBoxLayoutManager implements LayoutManager {
        public void addLayoutComponent(String name, Component comp) {}

        public void removeLayoutComponent(Component comp) {}

        public Dimension preferredLayoutSize(Container parent) {
//        	LOG.info("Container parent:"+parent);
            return parent.getPreferredSize();
        }

        public Dimension minimumLayoutSize(Container parent) {
            return parent.getMinimumSize();
        }

        public void layoutContainer(Container parent) {
//        	LOG.info(">>>>>>>>>>>"
//        		+ "\n Container parent:"+parent
//        		+ "\n set Bounds for arrowButton:"+arrowButton
//        		+ "\n set Bounds for editor:"+editor
//        		);
            JComboBox<?> cb = (JComboBox<?>)parent;
            int width = cb.getWidth();
            int height = cb.getHeight();

            Insets insets = getInsets();
            int buttonHeight = height - (insets.top + insets.bottom);
            int buttonWidth = buttonHeight;
            if (arrowButton != null) {
                Insets arrowInsets = arrowButton.getInsets();
                buttonWidth = squareButton ? buttonHeight 
                	: arrowButton.getPreferredSize().width + arrowInsets.left + arrowInsets.right;
                // set arrowButton Bounds
                if (cb.getComponentOrientation().isLeftToRight()) {
                    arrowButton.setBounds(width-(insets.right+buttonWidth), insets.top, buttonWidth, buttonHeight);
                } else {
                    arrowButton.setBounds(insets.left, insets.top, buttonWidth, buttonHeight);
                }
            }

            Rectangle cvb;
            if (editor != null) {
                cvb = rectangleForCurrentValue();
                editor.setBounds(cvb);
            }
        }
    }

    // inner class copied from private javax.swing.plaf.basic.BasicComboBoxUI with modifications
	protected class Handler 
    implements KeyListener
//      , LayoutManager ==> extra subclass XComboBoxLayoutManager
//    	, PropertyChangeListener ==> extra subclass XPropertyChangeListener
    	, FocusListener
    	, ListDataListener
//    	, ActionListener ==> als lambda
    {
    	
        //
        // KeyListener with keyPressed , keyTyped , keyReleased
        //
        /**
         * This listener checks to see if the key event isn't a navigation key.  
         * If it finds a key event that wasn't a navigation key it dispatches it to JComboBox.selectWithKeyChar() 
         * so that it can do type-ahead.
         */
        public void keyPressed(KeyEvent e) {
        	LOG.config("KeyEvent "+e);
            if ( isNavigationKey(e.getKeyCode(), e.getModifiersEx()) ) {
                lastTime = 0L;
            } else if (comboBox.isEnabled() && comboBox.getModel().getSize()!=0 &&
                        isTypeAheadKey( e ) && e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                time = e.getWhen();
                if (comboBox.selectWithKeyChar(e.getKeyChar())) {
                    e.consume();
                }
            }
        }
        public void keyTyped(KeyEvent e) {}
        public void keyReleased(KeyEvent e) {}
        private boolean isTypeAheadKey( KeyEvent e ) {
/* The method isMenuShortcutKeyDown(InputEvent) from the type BasicGraphicsUtils is not visible
            return !e.isAltDown() && !BasicGraphicsUtils.isMenuShortcutKeyDown(e);

    static boolean isMenuShortcutKeyDown(InputEvent event) {
        return (event.getModifiersEx() &
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0;
    }

 */
        	boolean isMenuShortcutKeyDown 
        		= (e.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0;
            return !e.isAltDown() && !isMenuShortcutKeyDown;
        }

        //
        // FocusListener with focusGained and focusLost
        //
        // NOTE: The class is added to both the Editor and ComboBox.
        // The combo box listener hides the popup when the focus is lost.
        // It also repaints when focus is gained or lost.
        public void focusGained( FocusEvent e ) {
        	LOG.fine("FocusEvent "+e);
            ComboBoxEditor comboBoxEditor = comboBox.getEditor();

            if ( (comboBoxEditor != null) &&
                 (e!=null && e.getSource() == comboBoxEditor.getEditorComponent()) ) {
                return;
            }
            hasFocus = true;
            setButtonDirection();
            comboBox.repaint();

            if (comboBox.isEditable() && editor != null) {
                editor.requestFocus();
            }
        }
        public void focusLost( FocusEvent e ) {
        	LOG.fine("FocusEvent "+e);
            ComboBoxEditor editor = comboBox.getEditor();
            if ( (editor != null) &&
                 (e!=null && e.getSource() == editor.getEditorComponent()) ) {
                Object item = editor.getItem();

                Object selectedItem = comboBox.getSelectedItem();
                if (e!=null && !e.isTemporary() && item != null &&
                    !item.equals((selectedItem == null) ? "" : selectedItem )) {
                    comboBox.actionPerformed
                        (new ActionEvent(editor, 0, "",
                                      EventQueue.getMostRecentEventTime(), 0));
                }
            }

            hasFocus = false;
            if (e!=null && !e.isTemporary()) {
                setButtonDirection();
                setPopupVisible(comboBox, false);
            } else if (e==null) {
                setButtonDirection();
                setPopupVisible(comboBox, false);
            }
            comboBox.repaint();
        }

        //
        // ListDataListener
        //

        // This listener watches for changes in the ComboBoxModel
        public void contentsChanged( ListDataEvent e ) {
            if (!(e.getIndex0() == -1 && e.getIndex1() == -1)) {
                isMinimumSizeDirty = true;
                comboBox.revalidate();
            }

            // set the editor with the selected item since this
            // is the event handler for a selected item change.
            if (comboBox.isEditable() && editor != null) {
                comboBox.configureEditor(comboBox.getEditor(),
                                         comboBox.getSelectedItem());
            }

            isDisplaySizeDirty = true;
            comboBox.repaint();
        }
        public void intervalAdded( ListDataEvent e ) {
            contentsChanged( e );
        }
        public void intervalRemoved( ListDataEvent e ) {
            contentsChanged( e );
        }
        
        //
        // LayoutManager verschoben in XComboBoxLayoutManager
        //
        
        //
        // ActionListener ==> als lambda
        //
        // Fix for 4515752: Forward the Enter pressed on the
        // editable combo box to the default button

        // Note: This could depend on event ordering. The first ActionEvent
        // from the editor may be handled by the JComboBox in which case, the
        // enterPressed action will always be invoked.
//        public void actionPerformed(ActionEvent evt) {
//        	LOG.info(">>>ActionEvent:"+evt);
//            Object item = comboBox.getEditor().getItem();
//            if (item != null) {
//                if (!comboBox.isPopupVisible() && !item.equals(comboBox.getSelectedItem())) {
//                    comboBox.setSelectedItem(comboBox.getEditor().getItem());
//                }
//                ActionMap am = comboBox.getActionMap();
//                if (am != null) {
//                    Action action = am.get("enterPressed");
//                    if (action != null) {
//                        action.actionPerformed(new ActionEvent(comboBox, evt.getID(),
//                                evt.getActionCommand(),
//                                evt.getModifiers()));
//                    }
//                }
//            }
//        }

    }

	class XPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            LOG.fine(propertyName+" PropertyChangeEvent:"+e);
            if (e.getSource() == editor) {
                // If the border of the editor changes then this can effect
                // the size of the editor which can cause the combo's size to
                // become invalid so we need to clear size caches
                if ("border".equals(propertyName)){
                    isMinimumSizeDirty = true;
                    isDisplaySizeDirty = true;
                    comboBox.revalidate();
                }
                return;
            }
            if (e.getSource() != comboBox) {
                LOG.warning("PropertyChangeEvent source is not JComboBox "+e.getSource());
                return;
            }
            JXComboBox<?> xComboBox = (JXComboBox<?>)e.getSource();
            if (propertyName == "model") {
                ComboBoxModel<?> newModel = (ComboBoxModel<?>)e.getNewValue();
                ComboBoxModel<?> oldModel = (ComboBoxModel<?>)e.getOldValue();

                if (oldModel != null && listDataListener != null) {
                    oldModel.removeListDataListener(listDataListener);
                }

                if (newModel != null && listDataListener != null) {
                    newModel.addListDataListener(listDataListener);
                }

                if (editor != null) {
                    xComboBox.configureEditor(xComboBox.getEditor(), xComboBox.getSelectedItem() );
                }
                isMinimumSizeDirty = true;
                isDisplaySizeDirty = true;
                xComboBox.revalidate();
                xComboBox.repaint();
            } else if( propertyName == "rowSorter") {
                if(listBox instanceof JXList<?>) {
                    JXList<?> xlist = (JXList<?>)listBox;
                    xlist.setAutoCreateRowSorter(xComboBox.hasRowSorter());
                    RowSorter rs = xComboBox.getRowSorter();
                    xlist.setRowSorter(rs);
                }
            } else if (propertyName == "editor" && xComboBox.isEditable()) {
                addEditor();
                xComboBox.revalidate();
            } else if (propertyName == "editable") {
                if (xComboBox.isEditable()) {
                    xComboBox.setRequestFocusEnabled(false);
                    addEditor();
                } else {
                    xComboBox.setRequestFocusEnabled(true);
                    removeEditor();
                }
                synchronizeToolTipTextForChildren();
                xComboBox.revalidate();
            } else if (propertyName == "enabled") {
                boolean enabled = xComboBox.isEnabled();
                if (editor != null) editor.setEnabled(enabled);
                if (arrowButton != null) arrowButton.setEnabled(enabled);
                xComboBox.repaint();
            } else if (propertyName == "focusable") {
                boolean focusable = xComboBox.isFocusable();
                if (editor != null) editor.setFocusable(focusable);
                if (arrowButton != null) arrowButton.setFocusable(focusable);
                xComboBox.repaint();
            } else if (propertyName == "maximumRowCount") {
                if (isPopupVisible(xComboBox)) {
                    setPopupVisible(xComboBox, true);
                }
            } else if (propertyName == "font") {
                listBox.setFont(xComboBox.getFont());
                if (editor != null) {
                    editor.setFont(xComboBox.getFont());
                }
                isMinimumSizeDirty = true;
                isDisplaySizeDirty = true;
                xComboBox.validate();
            } else if ("graphicsConfiguration".equals(propertyName)) {
                if(e.getOldValue()!=e.getNewValue()) {
                    LOG.fine(propertyName+" is set to "+e.getNewValue());
                    var newGC = (GraphicsConfiguration) e.getOldValue();
                    var oldGC = (GraphicsConfiguration) e.getNewValue();
                    var newTx = newGC != null ? newGC.getDefaultTransform() : null;
                    var oldTx = oldGC != null ? oldGC.getDefaultTransform() : null;
                    if(!Objects.equals(newTx, oldTx)) {
                        // the scale used by "graphicsConfiguration" was changed
                        isMinimumSizeDirty = true;
                        isDisplaySizeDirty = true;
                        xComboBox.validate();
                    }
                }
            } else if (propertyName == JComponent.TOOL_TIP_TEXT_KEY) {
                synchronizeToolTipTextForChildren();
            } else if (propertyName == IS_TABLE_CELL_EDITOR) {
                Boolean newValue = (Boolean)e.getNewValue();
                isTableCellEditor = newValue.equals(Boolean.TRUE) ? true : false;
            } else if (propertyName == "prototypeDisplayValue") {
                isMinimumSizeDirty = true;
                isDisplaySizeDirty = true;
                xComboBox.revalidate();
            } else if (propertyName == "renderer") {
                isMinimumSizeDirty = true;
                isDisplaySizeDirty = true;
                xComboBox.revalidate();
            } else if (propertyName == "UI") {
                if(e.getOldValue()==e.getNewValue()) {
                    LOG.config(propertyName+" is unchanged.");
                } else if(e.getOldValue() instanceof BasicXComboBoxUI) {
                    BasicXComboBoxUI oui = (BasicXComboBoxUI)e.getOldValue();
                    BasicXComboBoxUI nui = (BasicXComboBoxUI)e.getNewValue();
                    if(oui.arrowButton!=null) {
                        LOG.config("uninstall Button "+oui.arrowButton+" in "+oui.comboBox);
                        oui.uninstallButton();
                    }
                    nui.installButton(oui.icon);
                    nui.setIsShowingPopupIcon(oui.isShowingPopupIcon);
                }
//NOT handled properties:
            } else if (propertyName == "ancestor") {
                /*
                 * there is an AncestorListener in JComboBox
                 * which hides popup on AncestorEvents
                 */
            } else {
                /* expected for PROP_DONT_CANCEL_POPUP == doNotCancelPopup
INFORMATION: NOT handled property highlighters
INFORMATION: NOT handled property background
INFORMATION: NOT handled property foreground
INFO: NOT handled property name , border , labeledBy
                 */
                LOG.config("NOT handled property "+propertyName 
                    + "\n OldValue:"+e.getOldValue() + "\n NewValue:"+e.getNewValue());
            }
        }
    }

    // inner class copied from private javax.swing.plaf.basic.BasicComboBoxUI with modifications
    class DefaultKeySelectionManager implements JComboBox.KeySelectionManager, UIResource {
        private String prefix = "";
        private String typedString = "";

		@Override
		public int selectionForKey(char aKey, ComboBoxModel<?> aModel) {
            if (lastTime == 0L) {
                prefix = "";
                typedString = "";
            }
            boolean startingFromSelection = true;

            int startIndex = comboBox.getSelectedIndex();
            if (time - lastTime < timeFactor) {
                typedString += aKey;
                if((prefix.length() == 1) && (aKey == prefix.charAt(0))) {
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
            lastTime = time;

            if (startIndex < 0 || startIndex >= aModel.getSize()) {
                startingFromSelection = false;
                startIndex = 0;
            }
            int index = listBox.getNextMatch(prefix, startIndex,
                                             Position.Bias.Forward);
            if (index < 0 && startingFromSelection) { // wrap
                index = listBox.getNextMatch(prefix, 0,
                                             Position.Bias.Forward);
            }
            return index;
		}
    	
    }

}
