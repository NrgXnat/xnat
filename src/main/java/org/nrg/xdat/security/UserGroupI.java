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

	String getId();

	void setId(String id);

	String getTag();

	void setTag(String tag);

	String getDisplayname();

	void setDisplayname(String displayName);

	Integer getPK();

	void setPK(Integer pk);

	List<String> getUsernames();

	List<List<Object>> getPermissionItems(String login) throws Exception;

	List<PermissionCriteriaI> getPermissionsByDataType(final String type);
}