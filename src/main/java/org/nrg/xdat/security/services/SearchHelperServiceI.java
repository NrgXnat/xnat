package org.nrg.xdat.security.services;

import java.util.List;

import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.UserGroupI;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.security.UserI;

public interface SearchHelperServiceI {
	public abstract List<XdatStoredSearch> getSearchesForUser(UserI user);
	public abstract XdatStoredSearch getSearchForUser(UserI user, String id);
	public abstract void replacePreLoadedSearchForUser(UserI user, XdatStoredSearch i);
	public abstract DisplaySearch getSearchForUser(UserI user, String elementName,String display) throws Exception;
	
	public abstract List<XdatStoredSearch> getSearchesForGroup(UserGroupI group);
	public abstract XdatStoredSearch getSearchForGroup(UserGroupI group, String id);
	public abstract void replacePreLoadedSearchForGroup(UserGroupI group, XdatStoredSearch i);
}
