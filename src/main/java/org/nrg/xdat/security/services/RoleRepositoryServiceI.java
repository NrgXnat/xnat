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
