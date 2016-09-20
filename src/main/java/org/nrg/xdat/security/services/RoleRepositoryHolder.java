/*
 * core: org.nrg.xdat.security.services.RoleRepositoryHolder
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class RoleRepositoryHolder implements RoleRepositoryServiceI {
	public RoleRepositoryHolder(){
		try {
			roleRepositoryService = Class.forName(RoleRepositoryServiceI.DEFAULT_ROLE_REPO_SERVICE).asSubclass(RoleRepositoryServiceI.class).newInstance();
		}
		catch(Exception e){
			logger.error("",e);
		}
	}

	public RoleRepositoryHolder(RoleRepositoryServiceI roleRepositoryService){
		this.roleRepositoryService=roleRepositoryService;
	}

	public void setRoleRepositoryService(RoleRepositoryServiceI roleRepositoryService){
		this.roleRepositoryService=roleRepositoryService;
	}

	@Override
	public Collection<RoleDefinitionI> getRoles() {
		return roleRepositoryService.getRoles();
	}

	private RoleRepositoryServiceI roleRepositoryService;
	private final static Logger logger = Logger.getLogger(RoleRepositoryHolder.class);

}
