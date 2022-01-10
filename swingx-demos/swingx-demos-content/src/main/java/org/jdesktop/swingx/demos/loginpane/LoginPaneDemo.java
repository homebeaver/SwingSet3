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
 */
package org.jdesktop.swingx.demos.loginpane;

import static org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.jdesktop.application.Application;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXLoginPane;
import org.jdesktop.swingx.JXLoginPane.Status;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.auth.DefaultUserNameStore;
import org.jdesktop.swingx.auth.KeyChain;
import org.jdesktop.swingx.auth.PasswordStore;
import org.jdesktop.swingx.auth.UserNameStore;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.util.PaintUtils;
import org.jdesktop.swingxset.DefaultDemoPanel;
import org.jdesktop.swingxset.SwingXSet;

import com.sun.swingset3.DemoProperties;

/**
 * A demo for the {@code JXLoginPane}.
 *
 * @author Karl George Schaefer
 * @author rah003 (original JXLoginPaneDemo)
 * @author hb https://github.com/homebeaver (locale lang selector + custom moon banner)
 */
@DemoProperties(
    value = "JXLoginPane Demo",
    category = "Controls",
    description = "Demonstrates JXLoginPane, a security login control.",
    sourceFiles = {
        "org/jdesktop/swingx/demos/loginpane/LoginPaneDemo.java",
        "org/jdesktop/swingx/demos/loginpane/resources/LoginPaneDemo.properties",
        "org/jdesktop/swingx/demos/loginpane/resources/LoginPaneDemo.html",
        "org/jdesktop/swingx/demos/loginpane/resources/images/LoginPaneDemo.png",
        "org/jdesktop/swingx/demos/loginpane/DemoLoginService.java"
    }
)
@SuppressWarnings("serial")
//abstract class DefaultDemoPanel extends JXPanel
public class LoginPaneDemo extends DefaultDemoPanel {
	
    private static final Logger LOG = Logger.getLogger(LoginPaneDemo.class.getName());

    private PasswordStore ps;
    private UserNameStore us = null; // not used ==> DefaultUserNameStore
    private DemoLoginService service;
    private JXLoginPane loginPane;
    // controler:
    private JXLabel statusLabel;
    private JToggleButton allowLogin;
    private JXComboBox<DisplayLocale> localeBox; // DisplayLocale is a wrapper for Locale
    private Locale selectedLocale;
    private JXButton loginLauncher;
    
    /**
     * main method allows us to run as a standalone demo.
     */
    /*
     * damit diese Klasse als einzige im SwingXSet gestartet werden kann (Application.launch),
     * muss sie in einem file stehen (==>onlyXButtonDemo).
     * Dieses file wird dann vom DemoCreator eingelesen, "-a"/"-augment" erweitert den demo-Vorrat.
     */
    public static void main(String[] args) {
    	Application.launch(SwingXSet.class, new String[] {"META-INF/onlyLoginDemo"});
    }

    public LoginPaneDemo() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    // implements abstract void DefaultDemoPanel.createDemo() called in super ctor
	@Override
	protected void createDemo() {
		LOG.config("ctor");
        setLayout(new BorderLayout());
	}

	@Override
    protected void injectResources() {
        createLoginPaneControler();
        super.injectResources();
    }

	@Override
    protected void bind() {
        // bind source allowLogin button to target service.validLogin erst nach createLoginPaneDemo()!
		// d.h. bind() registriert nur den event Observer aka Listener
    	loginLauncher.addActionListener(event -> {
    		if(loginPane==null) {
    			createLoginPaneDemo();
    	        Bindings.createAutoBinding(READ, 
    	        		allowLogin, BeanProperty.create("selected"),
    	                service, BeanProperty.create("validLogin")
    	                ).bind();
    		}
    		if(selectedLocale!=null) loginPane.setLocale(this.selectedLocale);
    		
    		if(statusLabel.getText().equals(Status.SUCCEEDED.toString())) {
    			LOG.info("status:SUCCEEDED!!!! - do reset ...");
    			loginPane = null;
    			statusLabel.setText(Status.NOT_STARTED.toString());
    			localeBox.setSelectedItem(localeBox.getModel().getElementAt(0)); // Locale.0 is en
    			loginLauncher.setText("reset done, launch again.");
    			return;
    		}
    		
    		Status status = JXLoginPane.showLoginDialog(LoginPaneDemo.this, loginPane); // 1623
    		statusLabel.setText(status.toString());
    		
    		if(status==Status.SUCCEEDED) {
    			String[] userNames = loginPane.getUserNameStore().getUserNames();
    			String allNames = Arrays.toString(userNames);
    			LOG.info("User:"+loginPane.getUserName() + " of "+userNames.length + allNames
    				+ ", Server:"+service.getServer() + ", LoginService:"+loginPane.getLoginService().getServer()
					+ ", isRememberPassword? : "+loginPane.isRememberPassword());  			
    			ps.set("USER#allNames", null, allNames.toCharArray()); // userNames persistent ablegen
    			if(loginPane.isRememberPassword()) {
    				// wurde schon in loginPane gemacht:
    				//ps.set(loginPane.getUserName(), null, loginPane.getPassword());
    			}
				((InnerFilePasswordStore)ps).store(); // make ps persistent
				
    			loginPane.setVisible(false);
    			loginLauncher.setText("login "+Status.SUCCEEDED.toString());
    			// kein disable -> launch kann beliebig of wiederholt werden -> reset
    			//loginLauncher.setEnabled(false);
    		}
    	});   	
    }
    
    private void createLoginPaneDemo() {
        final String[] serverArray = { null
//        		, "sun-ds.sfbay" 
//            	, "jdbc:postgresql://localhost/demo", "jdbc:postgresql://localhost/ad393"
        		, "jdbc:h2:~/data/H2/demodata" };
        
        service = new DemoLoginService(serverArray);
    	ps = new InnerFilePasswordStore();
    	char[] allNames = ps.get("USER#allNames", null);
    	/*
    	   allNames==null => ps ist leer
    	   allNames=[a]   => ein user
    	   allNames=[a, b, beta] => mehrere user
    	 */
//    	us = new LoggingUserNameStore(); // uncomment to be verbose 
    	us = new DefaultUserNameStore();
    	LOG.info("-------------------> us:"+us + ", allNames="+(allNames==null?"null":String.valueOf(allNames)));
    	if(allNames!=null) {
    		String allNamesasString = String.valueOf(allNames);
    		String[] names = allNamesasString.substring(1, allNamesasString.length()-1).split(", ");
        	int l = names.length;
        	for(int i=0;i<names.length;i++) {
            	LOG.info("-------------------> #allNames="+l + " "+i+":"+names[i]);
        		us.addUserName(names[i]);
        	}
        	us.saveUserNames();
    	}
        
        loginPane = new JXLoginPane(service, ps, us);
        LOG.info("banner:"+loginPane.getBanner());
        List<String> servers = new ArrayList<String>(Arrays.asList(serverArray));
        loginPane.setServers(servers);

        loginPane.addPropertyChangeListener("status", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				JXLoginPane.Status status = (JXLoginPane.Status)evt.getNewValue();
				JXLoginPane.Status old = (JXLoginPane.Status)evt.getOldValue();
				LOG.info("new status is "+status + " old:"+old);
				statusLabel.setText(status.toString());
				LoginPaneDemo.this.validate();
			}
        	
        });
        
        // customization:
//        loginPane.setBanner(null); // No banner (customization)
        loginPane.setBanner(new MoonLoginPaneUI(loginPane).getBanner());
//        loginPane.setBannerText("BannerText");
        
        // nicht notwendig: wird anhand ps+us gesetzt:
//        loginPane.setSaveMode(SaveMode.PASSWORD);
    }
    
    private void createLoginPaneControler() {
        Font font = new Font("SansSerif", Font.PLAIN, 16);

        loginLauncher = new JXButton();
        loginLauncher.setName("launcher"); // den text aus prop "launcher.text" holen
        loginLauncher.setFont(font);
        final Painter<Object> orangeBgPainter = new MattePainter(PaintUtils.ORANGE_DELIGHT, true);
        loginLauncher.setBackgroundPainter(orangeBgPainter);
        loginLauncher.addMouseListener(new MouseAdapter() { // disable BG painter
            @Override
            public void mouseEntered(MouseEvent e) {
            	loginLauncher.setBackgroundPainter(null);
            }

            @Override
            public void mouseExited(MouseEvent e) {
            	loginLauncher.setBackgroundPainter(orangeBgPainter);
            }          
        });
        
        JXPanel n = new JXPanel(new HorizontalLayout());
        JXLabel status = new JXLabel("Status:");
        status.setFont(font);
        status.setHorizontalAlignment(SwingConstants.RIGHT);
        n.add(status);
        statusLabel = new JXLabel(loginPane==null ? Status.NOT_STARTED.toString() : loginPane.getStatus().name());
        statusLabel.setFont(font);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        n.add(statusLabel);
        add(n, BorderLayout.NORTH);
        add(loginLauncher, BorderLayout.SOUTH);
        
        JXPanel p = new JXPanel(new VerticalLayout());
        add(p);
        
        allowLogin = new JRadioButton(); // JRadioButton extends JToggleButton
        allowLogin.setFont(font);
        allowLogin.setName("allowLogin");
        JRadioButton disallowLogin = new JRadioButton("disallow"); // JRadioButton extends JToggleButton
        disallowLogin.setFont(font);
      //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(allowLogin);
        group.add(disallowLogin);
        JPanel radioPanel = new JPanel(new java.awt.GridLayout( 1, 2 ));
        radioPanel.add(allowLogin);
        radioPanel.add(disallowLogin);
        p.add(radioPanel);
        
        JLabel langLabel = new JXLabel("select language for Login Screen:", SwingConstants.LEFT);
        
        localeBox = new JXComboBox<DisplayLocale>();
        localeBox.setModel(createDisplayLocaleList());
        
        langLabel.setFont(font);
        p.add(langLabel);
        
        localeBox.setFont(font);
        localeBox.addHighlighter(HighlighterFactory.createSimpleStriping(HighlighterFactory.LINE_PRINTER));
        localeBox.addActionListener(event -> {
        	Locale selected = ((DisplayLocale)localeBox.getSelectedItem()).getLocale();
        	LOG.info("Locale selected:"+selected + ", loginPane:"+loginPane);
            // an dieser Stelle ist loginPane immer null, daher macht der Observer so wenig Sinn
        	//if(loginPane!=null) loginPane.setLocale(selected);
        	selectedLocale = selected;
        });
        p.add(localeBox);
    }
    
    /**
     * wrapper for class Locale
     * <p>
     * class Locale is final, so cannot subclass it
     *
     */
    public class DisplayLocale {
        private final Locale locale;
        
        public DisplayLocale(String lang) {
            this.locale = new Locale(lang);
        }
        public DisplayLocale(Locale item) {
            this.locale = item;
        }
        
        public Locale getLocale() {
            return locale;
        }

        // used in JXComboBox
        public String toString() {
			return locale.toString() + " " + locale.getDisplayLanguage(locale) + "/" +locale.getDisplayLanguage();  	
        }
    }
    
    private ComboBoxModel<DisplayLocale> createDisplayLocaleList() {
        DefaultComboBoxModel<DisplayLocale> model = new DefaultComboBoxModel<DisplayLocale>();
        model.addElement(new DisplayLocale(Locale.ENGLISH));
        model.addElement(new DisplayLocale("cs"));
        model.addElement(new DisplayLocale("es"));
        model.addElement(new DisplayLocale(Locale.FRENCH));
        model.addElement(new DisplayLocale(Locale.GERMAN));
//        model.addElement(new DisplayLocale(new Locale("de", "CH")));
//        model.addElement(new DisplayLocale(new Locale("fr", "CH")));
//        model.addElement(new DisplayLocale(new Locale("it", "CH")));
//        model.addElement(new DisplayLocale(new Locale("rm", "CH")));
        model.addElement(new DisplayLocale(Locale.ITALIAN));
        model.addElement(new DisplayLocale("nl"));
        model.addElement(new DisplayLocale("pl"));
        model.addElement(new DisplayLocale(new Locale("pt", "BR")));
        model.addElement(new DisplayLocale("sv"));
		return model;
    }
    
    public class LoggingUserNameStore extends DefaultUserNameStore {
    	
    	LoggingUserNameStore() {
    		super();
        	LOG.info(">>>>>>>>>>> ctor");
    	}
    	
        @Override
        public void addUserName(String name) {
        	LOG.info(">>>>>>>>>>> name="+name);
        	super.addUserName(name);
        }

        @Override
        public void removeUserName(String name) {
        	LOG.info(">>>>>>>>>>> name="+name);
        	super.removeUserName(name);
        }
        
        @Override
        public String[] getUserNames() {
        	String[] res = super.getUserNames();
        	LOG.info(">>>>>>>>>>> " + " results to array length="+(res==null ? "nix" : res.length));
        	return res;
        }
        @Override
        public boolean containsUserName(String name) {
        	boolean res = super.containsUserName(name);
        	LOG.info(">>>>>>>>>>> name="+name + " results to "+res);
        	return res;
        }
        
        public void loadUserNames() {
        	super.loadUserNames();
        	LOG.info(">>>>>>>>>>> done.");
        }
        public void saveUserNames() {
        	super.saveUserNames();
        	LOG.info(">>>>>>>>>>> done.");
        }
    }

    /*
     * Alternative
     * https://github.com/xafero/java-keyring
     * 
     * wg. static class see
     * https://stackoverflow.com/questions/70324/java-inner-class-and-static-nested-class
     * https://stackoverflow.com/questions/253492/static-nested-class-in-java-why
     * https://stackoverflow.com/questions/16524373/why-and-when-to-use-static-inner-class-or-instance-inner-class
     */
    private static final class InnerFilePasswordStore extends PasswordStore {
    	
    	private static final String FILENAME = "KeyChainInnerStore.txt";
    	
    	KeyChain kc;
    	
    	public InnerFilePasswordStore() {
            FileInputStream fis = null;
    		try {
    	        File file = new File(FILENAME); // in eclipse ws : swingset\SwingSet3\swingx-demos
    	        if (!file.exists()) {
    	            file.createNewFile(); // throws IOException
    	            LOG.info("created "+FILENAME);
    	            fis = null;
    	        } else {
    	            fis = new FileInputStream(file); // throws FileNotFoundException
    	            LOG.info("use existing "+FILENAME);
    	        }
    		} catch (FileNotFoundException e) {
                LOG.warning("new FileInputStream throws"+e);
    		} catch (IOException e) {
                LOG.warning("file.createNewFile throws"+e);
    		}
    		
            try {
    			kc = new KeyChain("test".toCharArray(), fis);
    			store(); // store the empty DS
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    	}

    	// equals und hashCode sind in JXLoginPane.NullPasswordStore definiert. Wozu?
//    	@Override // overwrites Object.equals
//        public boolean equals(Object obj) {
//            return obj instanceof InnerFilePasswordStore;
//        }
//    	@Override // overwrites Object.hashCode
//        public int hashCode() {
//            return 17;
//        }

    	/**
    	 *  Persist the KeyChain and reflect any changes, <b>store</b> with an OutputStream.
    	 */
        public void store() {
            FileOutputStream fos = null;
            File file = new File(FILENAME);
            try {
                fos = new FileOutputStream(file); // throws FileNotFoundException
    			kc.store(fos); // throws IOException
                LOG.info("PasswordStore stored to "+FILENAME);
    		} catch (FileNotFoundException e) {
                LOG.warning("new FileOutputStream throws"+e);
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }

        /**
         * Adds a password to the store for a given account/user and server.
         * 
         * {@inheritDoc}
         *
         *  @param username account/user
         *  @param server server used for authentication, can be null
         *  @param password password to save. 
         *  	Password can't be null. Use empty array for empty password.
         *  @return <code>true</code> if stored, <code>false</code> if password is null
         */
    	@Override
    	public boolean set(String username, String server, char[] password) {
    		if(password==null) return false;
    		LOG.info("username="+username + ", server="+server);
    		kc.addPassword(username, server, password);
    		return true;
    	}

        /**
         * {@inheritDoc}
         */
    	@Override
    	public char[] get(String username, String server) {
    		String pw = kc.getPassword(username, server);
    		return pw==null ? null : pw.toCharArray();
    	}

        /**
         * {@inheritDoc}
         */
    	@Override
    	public void removeUserPassword(String username) {
            LOG.info("TODO username="+username);
    		// TODO ? server
    	}

    }
}
