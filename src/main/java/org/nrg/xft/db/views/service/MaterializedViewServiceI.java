package org.nrg.xft.db.views.service;

import java.util.Hashtable;

import org.nrg.xft.db.MaterializedViewI;
import org.nrg.xft.security.UserI;

public interface MaterializedViewServiceI {

	public void deleteViewsByUser(UserI user) throws Exception;

	public MaterializedViewI createView(UserI user);

	public MaterializedViewI getViewByTablename(String tablename, UserI user) throws Exception;

	public MaterializedViewI getViewBySearchID(String search_id, UserI user) throws Exception;

	public MaterializedViewI populateView(Hashtable t, UserI u);

}
