/*
 * notify: org.nrg.notify.api.CategoryScope
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.api;

public enum CategoryScope {
    Site,
    Project,
    Subject,
    Session;
    
    public static CategoryScope Default = Site;
}
