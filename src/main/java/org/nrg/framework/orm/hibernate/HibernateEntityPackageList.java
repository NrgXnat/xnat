/**
 * HibernateEntityPackageList
 * (C) 2012 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on 2/29/12 by rherri01
 */
package org.nrg.framework.orm.hibernate;

import java.util.ArrayList;
import java.util.List;

public class HibernateEntityPackageList extends ArrayList<String> {
    public void setItems(List<String> items) {
        clear();
        addAll(items);
    }
}
