/*
 * org.nrg.xnat.helpers.prearchive.PrearcConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.prearchive;

// MIGRATION: Get rid of this class in favor of settings in the preferences service.
public final class PrearcConfig {

    private boolean reloadPrearcDatabaseOnApplicationStartup;

    public boolean isReloadPrearcDatabaseOnApplicationStartup() {
        return reloadPrearcDatabaseOnApplicationStartup;
    }

    public void setReloadPrearcDatabaseOnApplicationStartup(boolean reloadPrearcDatabaseOnApplicationStartup) {
        this.reloadPrearcDatabaseOnApplicationStartup = reloadPrearcDatabaseOnApplicationStartup;
    }
}
