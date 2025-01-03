package org.jdesktop.swingx;

public class JXTextFieldBeanInfo extends JXPromptBeanInfo {
	/** ctor */
	public JXTextFieldBeanInfo() {
		this(JXTextField.class);
	}
	
	protected JXTextFieldBeanInfo(Class<? extends JXTextField> beanClass) {
		super(beanClass);
		
		setPreferred(true, "outerMargin");
	}
}