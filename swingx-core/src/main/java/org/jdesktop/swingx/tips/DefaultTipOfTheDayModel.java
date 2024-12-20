/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
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
package org.jdesktop.swingx.tips;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Default {@link org.jdesktop.swingx.tips.TipOfTheDayModel} implementation.<br>
 * 
 * @author <a href="mailto:fred@L2FProd.com">Frederic Lavigne</a>
 */
public class DefaultTipOfTheDayModel implements TipOfTheDayModel {

    private List<Tip> tips = new ArrayList<Tip>();

    /**
     * TODO maven-javadoc-plugin 3.3.2 needs a doc here
     */
    public DefaultTipOfTheDayModel() {
    }

    /**
     * TODO maven-javadoc-plugin 3.3.2 needs a doc here
     * @param tips array of tips
     */
    public DefaultTipOfTheDayModel(Tip[] tips) {
        this(Arrays.asList(tips));
    }

    /**
     * TODO maven-javadoc-plugin 3.3.2 needs a doc here
     * @param tips Collection of tips
     */
    public DefaultTipOfTheDayModel(Collection<Tip> tips) {
        this.tips.addAll(tips);
    }

    @Override
    public Tip getTipAt(int index) {
        return tips.get(index);
    }

    @Override
    public int getTipCount() {
        return tips.size();
    }

    /**
     * TODO maven-javadoc-plugin 3.3.2 needs a doc here
     * @param tip a Tip
     */
    public void add(Tip tip) {
        tips.add(tip);
    }

    /**
     * TODO maven-javadoc-plugin 3.3.2 needs a doc here
     * @param tip a Tip
     */
    public void remove(Tip tip) {
        tips.remove(tip);
    }

    /**
     * TODO maven-javadoc-plugin 3.3.2 needs a doc here
     * @return tips
     */
    public Tip[] getTips() {
        return tips.toArray(new Tip[tips.size()]);
    }

    /**
     * TODO maven-javadoc-plugin 3.3.2 needs a doc here
     * @param tips array of tips
     */
    public void setTips(Tip[] tips) {
        this.tips.clear();
        this.tips.addAll(Arrays.asList(tips));
    }

}
