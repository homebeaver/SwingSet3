module swingx.core {
	exports org.jdesktop.swingx;
	exports org.jdesktop.swingx.auth;
	exports org.jdesktop.swingx.table;
	exports org.jdesktop.swingx.icon;
	
	// Definiert Schnittstellen und exportiert sie
	uses org.jdesktop.swingx.plaf.LookAndFeelAddons;
//	uses org.jdesktop.swingx.plaf.TableAddon;
	exports org.jdesktop.swingx.plaf;
	uses org.jdesktop.swingx.decorator.Highlighter;
	exports org.jdesktop.swingx.decorator;
	
	exports org.jdesktop.swingx.plaf.basic;
	exports org.jdesktop.swingx.plaf.basic.core;
	exports org.jdesktop.swingx.plaf.metal;
	exports org.jdesktop.swingx.plaf.misc;
	exports org.jdesktop.swingx.plaf.nimbus;
	exports org.jdesktop.swingx.plaf.synth;
	
	requires transitive java.desktop;
	requires java.logging;
	requires java.prefs;
	requires java.sql;
	requires java.naming;

	requires transitive swingx.common;
	requires swingx.graphics;
	requires swingx.autocomplete;
	requires swingx.action;
	requires swingx.painters;
	
	requires org.kohsuke.metainf_services;

}