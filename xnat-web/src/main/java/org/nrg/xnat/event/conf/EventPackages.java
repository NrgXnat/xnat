/*
 * web: org.nrg.xnat.event.conf.EventPackages
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.conf;

import java.util.HashSet;
import java.util.Set;

/**
 * The Class EventPackages.
 */
public class EventPackages extends HashSet<String> {
    
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9166262514950558701L;

	/**
     * Instantiates a new event packages.
     *
     * @param packages the packages
     */
    public EventPackages(Set<String> packages) {
    	super();
        this.setPackages(packages);
    }
    
    /**
     * Sets the packages.
     *
     * @param packages the new packages
     */
    public void setPackages(Set<String> packages) {
        clear();
        addAll(packages);
    }
}
