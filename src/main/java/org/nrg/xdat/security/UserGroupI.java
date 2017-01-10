/*
 * core: org.nrg.xdat.security.UserGroupI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import java.util.List;

public interface UserGroupI {

	public abstract String getId();
	public abstract String getTag();
	public abstract String getDisplayname();
	public abstract Integer getPK();
	public abstract void setId(String id);
	public abstract void setTag(String tag);
	public abstract void setDisplayname(String displayName);
	public abstract void setPK(Integer pk);
	
	public List<List<Object>> getPermissionItems(String login) throws Exception;
}
