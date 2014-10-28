package org.nrg.xdat.security.services;

import java.util.Collection;

public interface RoleRepositoryServiceI {
	
	public static interface RoleDefinitionI{
		public String getKey();
		public String getName();
		public String getWarning();
		public String getDescription();
	}
	
	public Collection<RoleDefinitionI> getRoles();
	
}
