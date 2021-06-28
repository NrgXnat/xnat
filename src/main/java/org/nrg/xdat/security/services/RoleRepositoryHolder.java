/*
 * core: org.nrg.xdat.security.services.RoleRepositoryHolder
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
@Slf4j
public class RoleRepositoryHolder implements RoleRepositoryServiceI {
    public RoleRepositoryHolder() {
        try {
            roleRepositoryService = Class.forName(RoleRepositoryServiceI.DEFAULT_ROLE_REPO_SERVICE).asSubclass(RoleRepositoryServiceI.class).newInstance();
        } catch (Exception e) {
            log.error("An error occurred trying to create the role repository service {}", RoleRepositoryServiceI.DEFAULT_ROLE_REPO_SERVICE, e);
        }
    }

    public RoleRepositoryHolder(final RoleRepositoryServiceI roleRepositoryService) {
        this.roleRepositoryService = roleRepositoryService;
    }

    public void setRoleRepositoryService(final RoleRepositoryServiceI roleRepositoryService) {
        this.roleRepositoryService = roleRepositoryService;
    }

    @Override
    public Collection<RoleDefinitionI> getRoles() {
        return roleRepositoryService.getRoles();
    }

    private RoleRepositoryServiceI roleRepositoryService;
}
