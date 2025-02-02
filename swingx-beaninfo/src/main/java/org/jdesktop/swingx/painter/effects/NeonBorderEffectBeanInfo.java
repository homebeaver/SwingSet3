/*
 * NeonBorderEffectBeanInfo.java
 *
 * Created on October 30, 2006, 1:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jdesktop.swingx.painter.effects;

import org.jdesktop.beans.editors.EnumPropertyEditor;
import org.jdesktop.beans.editors.Paint2PropertyEditor;
import org.jdesktop.swingx.painter.AbstractPainterBeanInfo;

/**
 *
 * @author joshy
 */
public class NeonBorderEffectBeanInfo extends AbstractPainterBeanInfo {
    
    /** Creates a new instance of NeonBorderEffectBeanInfo */
    public NeonBorderEffectBeanInfo() {
        super(NeonBorderEffect.class);
    }
    
    @Override
    protected void initialize() {
        super.initialize();
        setPropertyEditor(Paint2PropertyEditor.class, "edgeColor", "centerColor", "brushColor");
        //setPropertyEditor(InsetsPropertyEditor.class,"insets");
        setPropertyEditor(BorderPositionPropertyEditor.class,"borderPosition");
    }
    
    
	/** TODO doc */
    public static final class BorderPositionPropertyEditor extends EnumPropertyEditor {
    	/** ctor */
        public BorderPositionPropertyEditor() {
            super(NeonBorderEffect.BorderPosition.class);
        }
    }
    

}
