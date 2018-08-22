/*
 * web: org.nrg.xnat.event.listeners.methods.RoleServicesHandlerMethod
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.event.listeners.methods;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.services.RoleHolder;
import org.nrg.xdat.security.services.RoleRepositoryHolder;
import org.nrg.xdat.security.services.RoleRepositoryServiceI;
import org.nrg.xdat.security.services.RoleServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RoleServicesHandlerMethod extends AbstractXnatPreferenceHandlerMethod {
    @Autowired
    public RoleServicesHandlerMethod(final RoleHolder roleHolder, final RoleRepositoryHolder roleRepositoryHolder) {
        super(ROLE_SERVICE, ROLE_REPOSITORY_SERVICE);
        _roleHolder = roleHolder;
        _roleRepositoryHolder = roleRepositoryHolder;
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        switch (preference) {
            case ROLE_SERVICE:
                try {
                    _roleHolder.setRoleService(Class.forName(value).asSubclass(RoleServiceI.class).newInstance());
                } catch (InstantiationException e) {
                    log.error("An error occurred creating the role service with class: " + value, e);
                } catch (IllegalAccessException e) {
                    log.error("Access denied when creating the role service with class: " + value, e);
                } catch (ClassNotFoundException e) {
                    log.error("Could not find the specified role service class on the classpath: " + value, e);
                }
                break;

            case ROLE_REPOSITORY_SERVICE:
                try {
                    _roleRepositoryHolder.setRoleRepositoryService(Class.forName(value).asSubclass(RoleRepositoryServiceI.class).newInstance());
                } catch (InstantiationException e) {
                    log.error("An error occurred creating the role repository service with class: " + value, e);
                } catch (IllegalAccessException e) {
                    log.error("Access denied when creating the role repository service with class: " + value, e);
                } catch (ClassNotFoundException e) {
                    log.error("Could not find the specified role repository service class on the classpath: " + value, e);
                }
                break;
        }
    }

    private static final String ROLE_SERVICE            = "roleService";
    private static final String ROLE_REPOSITORY_SERVICE = "roleRepositoryService";

    private final RoleHolder           _roleHolder;
    private final RoleRepositoryHolder _roleRepositoryHolder;
}
