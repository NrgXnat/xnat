/*
 * web: org.nrg.xnat.restlet.actions.importer.ImporterHandlerPackages
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions.importer;

import java.util.HashSet;
import java.util.Set;

public class ImporterHandlerPackages extends HashSet<String> {
    public ImporterHandlerPackages(Set<String> packages) {
    	super();
        this.setPackages(packages);
    }
    public void setPackages(Set<String> packages) {
        clear();
        addAll(packages);
    }
}
