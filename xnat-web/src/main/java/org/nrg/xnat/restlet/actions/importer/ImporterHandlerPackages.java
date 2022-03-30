/*
 * web: org.nrg.xnat.restlet.actions.importer.ImporterHandlerPackages
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.actions.importer;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@Slf4j
public class ImporterHandlerPackages extends HashSet<String> {
    public ImporterHandlerPackages(final Collection<String> packages) {
        super();
        setPackages(packages);
    }

    public ImporterHandlerPackages(final String... packages) {
        this(Arrays.asList(packages));
    }

    public void setPackages(final Collection<String> packages) {
        clear();
        addAll(packages);
    }
}
