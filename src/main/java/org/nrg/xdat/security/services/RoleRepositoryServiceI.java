/*
 * core: org.nrg.xdat.security.services.RoleRepositoryServiceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.services;

import java.util.Collection;

public interface RoleRepositoryServiceI {
	String DEFAULT_ROLE_REPO_SERVICE = "org.nrg.xdat.security.services.impl.RoleRepositoryServiceImpl";

	interface RoleDefinitionI{
		String getKey();
		String getName();
		String getWarning();
		String getDescription();
	}
	
	Collection<RoleDefinitionI> getRoles();
}
