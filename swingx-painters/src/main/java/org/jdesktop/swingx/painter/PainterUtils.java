package org.jdesktop.swingx.painter;

import java.awt.Component;
import java.awt.Font;
import java.awt.Paint;

// all methods are static
final class PainterUtils {
	
    private PainterUtils() {
        //prevent instantiation
    }
    
    static Paint getForegroundPaint(Paint current, Object o) {
        if (current == null) {
            if (o instanceof Component) {
                return ((Component) o).getForeground(); // class Color implements Paint
            }
        }
        
        return current;
    }
    
    static Paint getBackgroundPaint(Paint current, Object o) {
        if (current == null) {
            if (o instanceof Component) {
                return ((Component) o).getBackground(); // class Color implements Paints
            }
        }
        
        return current;
    }
    
    static Font getComponentFont(Font current, Object o) {
        if (current == null) {
            if (o instanceof Component) {
                return ((Component) o).getFont();
            }
        }
        
        return current;
    }
}
