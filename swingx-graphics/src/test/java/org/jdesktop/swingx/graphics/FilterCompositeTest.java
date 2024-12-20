package org.jdesktop.swingx.graphics;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

import java.awt.Composite;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class FilterCompositeTest {
	
    @Test(expected=NullPointerException.class)
    public void checkThrowOnNullComposite() {
        new FilterComposite(null);
    }
    
    @Test
    public void checkUnaryConstructorHasNoFilter() {
        Composite composite = mock(Composite.class);
        FilterComposite fc = new FilterComposite(composite);
        assertThat(fc.getFilter(), CoreMatchers.is(nullValue()));
    }
}
