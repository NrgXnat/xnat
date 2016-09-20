/*
 * core: org.nrg.xft.db.views.service.MaterializedViewServiceI
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.db.views.service;

import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.security.UserI;

import java.util.Hashtable;

public interface MaterializedViewServiceI {

	public void deleteViewsByUser(UserI user) throws Exception;

	public MaterializedViewI createView(UserI user);

	public MaterializedViewI getViewByTablename(String tablename, UserI user) throws Exception;

	public MaterializedViewI getViewBySearchID(String search_id, UserI user) throws Exception;

	public MaterializedViewI populateView(Hashtable t, UserI u);

    public void save(MaterializedViewI i) throws Exception;

    public void delete(MaterializedViewI i) throws Exception;


}
