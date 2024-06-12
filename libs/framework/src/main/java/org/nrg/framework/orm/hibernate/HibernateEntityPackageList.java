/*
 * framework: org.nrg.framework.orm.hibernate.HibernateEntityPackageList
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HibernateEntityPackageList extends ArrayList<String> {
    public HibernateEntityPackageList() {
        super();
    }

    public HibernateEntityPackageList(final List<String> packages) {
        super(packages);
    }

    public HibernateEntityPackageList(final String... packages) {
        super(Arrays.asList(packages));
    }

    public void setItems(List<String> items) {
        clear();
        addAll(items);
    }
}
