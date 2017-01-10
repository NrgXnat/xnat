/*
 * web: org.nrg.xnat.event.listeners.methods.RoleServicesHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import com.google.common.collect.ImmutableList;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.RoleRepositoryHolder;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleServiceI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RoleServicesHandlerMethod extends AbstractSiteConfigPreferenceHandlerMethod {
    @Autowired
    public RoleServicesHandlerMethod(final SiteConfigPreferences preferences, final RoleHolder roleHolder, final RoleRepositoryHolder roleRepositoryHolder) {
        _preferences = preferences;
        _roleHolder = roleHolder;
        _roleRepositoryHolder = roleRepositoryHolder;
    }

    @Override
    public List<String> getHandledPreferences() {
        return PREFERENCES;
    }

    @Override
    public void handlePreferences(final Map<String, String> values) {
        if (!Collections.disjoint(PREFERENCES, values.keySet())) {
            updateFeatureServices();
        }
    }

    @Override
    public void handlePreference(final String preference, final String value) {
        if (PREFERENCES.contains(preference)) {
            updateFeatureServices();
        }
    }

    private void updateFeatureServices() {
        final String roleService = _preferences.getRoleService();
        final String roleRepositoryService = _preferences.getRoleRepositoryService();
        try {
            _roleHolder.setRoleService(Class.forName(roleService).asSubclass(RoleServiceI.class).newInstance());
        } catch (InstantiationException e) {
            _log.error("An error occurred creating the role service with class: " + roleService, e);
        } catch (IllegalAccessException e) {
            _log.error("Access denied when creating the role service with class: " + roleService, e);
        } catch (ClassNotFoundException e) {
            _log.error("Could not find the specified role service class on the classpath: " + roleService, e);
        }
        try {
            _roleRepositoryHolder.setRoleRepositoryService(Class.forName(roleRepositoryService).asSubclass(RoleRepositoryServiceI.class).newInstance());
        } catch (InstantiationException e) {
            _log.error("An error occurred creating the role repository service with class: " + roleRepositoryService, e);
        } catch (IllegalAccessException e) {
            _log.error("Access denied when creating the role repository service with class: " + roleRepositoryService, e);
        } catch (ClassNotFoundException e) {
            _log.error("Could not find the specified role repository service class on the classpath: " + roleRepositoryService, e);
        }
    }

    private static final Logger       _log        = LoggerFactory.getLogger(RoleServicesHandlerMethod.class);
    private static final List<String> PREFERENCES = ImmutableList.copyOf(Arrays.asList("roleService", "roleRepositoryService"));

    private final SiteConfigPreferences _preferences;
    private final RoleHolder            _roleHolder;
    private final RoleRepositoryHolder  _roleRepositoryHolder;

}
