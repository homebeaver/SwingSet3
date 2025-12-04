package org.jdesktop.swingx.renderer;

import java.awt.Component;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXComboBox;

/*
 
 ersetzt javax.swing.plaf.basic.BasicComboBoxRenderer

public class BasicComboBoxRenderer extends JLabel
implements ListCellRenderer<Object>, Serializable {

wie DefaultListRenderer<E>

E == Object
 */
public class DefaultComboBoxRenderer<E> extends AbstractRenderer implements ListCellRenderer<E> {

	private static final long serialVersionUID = -441370707689161796L;
	private static final Logger LOG = Logger.getLogger(DefaultComboBoxRenderer.class.getName());

    public DefaultComboBoxRenderer() {
    	// null ==> createDefaultComponentProvider()
        this((ComponentProvider<?>) null);
    }
    /*
public abstract class ComponentProvider<T extends JComponent> implements Serializable, UIDependent {
public class LabelProvider extends ComponentProvider<JLabel> {
     */
    public DefaultComboBoxRenderer(ComponentProvider<?> componentProvider) {
        super(componentProvider);
        LOG.config("ctor ComponentProvider<?>="+componentProvider+" componentController="+componentController);
        this.context = new ComboBoxContext();
    }

    static Border etchedNoFocusBorder = BorderFactory.createEtchedBorder();
    Border getEmptyNoFocusBorder() {
    	return new EmptyBorder(1, 1, 1, 1);
    }

    protected JXComboBox<?> cb;
    protected ComboBoxContext context;

	@Override
	public Component getListCellRendererComponent(JList<? extends E> list, E value, 
			int index, boolean isSelected, boolean cellHasFocus) {

//      public void installContext(JComboBox<?> component, Object value, int row, int column,
//              boolean selected, boolean focused, boolean expanded, boolean leaf) {
		context.installContext(cb, value, index, -1, isSelected, cellHasFocus, false, false);
        JComponent comp = componentController.getRendererComponent(context);
        comp.setBorder(index == -1 ? getEmptyNoFocusBorder() : etchedNoFocusBorder);
//			if (isSelected) {
//				setBackground(list.getSelectionBackground());
//				setForeground(list.getSelectionForeground());
//			} else {
//				setBackground(list.getBackground());
//				setForeground(list.getForeground());
//			}
//
//			setFont(list.getFont());
//
//			if (value instanceof Icon) {
//				setIcon((Icon) value);
//			} else {
//				setText((value == null) ? "" : value.toString());
//			}
        
// component whose paint() method will render the specified value.
//        LOG.info("component whose paint() method will render the specified value:"+value+"\n"+comp+"\n list:"+list);
		return comp;
	}

	@Override
	protected ComponentProvider<?> createDefaultComponentProvider() {
		ComponentProvider<JLabel> labelProvider = new LabelProvider(createDefaultStringValue());
		LOG.config("ComponentProvider<JLabel> labelProvider:"+labelProvider);
		return labelProvider;
	}

	protected StringValue createDefaultStringValue() {
        return MappedValues.STRING_OR_ICON_ONLY;
    }

}
