package org.jdesktop.swingx.plaf.basic;

import javax.swing.plaf.basic.BasicComboBoxEditor;

public class BasicXComboBoxEditor extends BasicComboBoxEditor {

    public BasicXComboBoxEditor() {
    	super();
//    	System.out.println("BasicXComboBoxEditor extends BasicComboBoxEditor ctor done, editor:"+this.getEditorComponent());
    }

    public static class UIResource extends BasicXComboBoxEditor
    		implements javax.swing.plaf.UIResource {
    	
        /**
         * Constructs a {@code UIResource}.
         */
        public UIResource() {}
    }

}
