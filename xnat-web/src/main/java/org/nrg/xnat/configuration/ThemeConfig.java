/*
 * org.nrg.xnat.turbine.modules.screens.ManageProtocol
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Author: Justin Cleveland <clevelandj@wustl.edu>
 * Last modified 2/8/2016 11:29 AM
 */

package org.nrg.xnat.configuration;

import java.util.ArrayList;

/**
 * Created by jcleve01 on 2/8/2016.
 */
public class ThemeConfig {
    private String name = null;
    private String path = null;
    private boolean enabled = true;
    private ArrayList roles = new ArrayList();

    /**
     * Dummy constructor to make Jackson mapper happy. Don't explictly use this.
     */
    public ThemeConfig() {
    }
    public ThemeConfig(String themeName, String themePath, boolean enabled) {
        this.name = themeName;
        this.path = themePath;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ArrayList getRoles() {
        return roles;
    }

    public void setRoles(ArrayList roles) {
        this.roles = roles;
    }
}
