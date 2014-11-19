package org.nrg.xdat.security;

import java.util.List;

import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.services.SearchHelperServiceI;
import org.nrg.xft.security.UserI;

public class XDATSearchHelperService implements SearchHelperServiceI {

	@Override
	public XdatStoredSearch getSearchForUser(UserI user, String id) {
		return ((XDATUser)user).getStoredSearch(id);
	}

	@Override
	public DisplaySearch getSearchForUser(UserI user, String elementName, String display) throws Exception {
		return ((XDATUser)user).getSearch(elementName, display);
	}

	@Override
	public void replacePreLoadedSearchForUser(UserI user, XdatStoredSearch i) {
		((XDATUser)user).replacePreLoadedSearch(i);
	}

	@Override
	public List<XdatStoredSearch> getSearchesForUser(UserI user) {
		return ((XDATUser)user).getStoredSearches();
	}

	@Override
	public List<XdatStoredSearch> getSearchesForGroup(UserGroupI group) {
		return ((UserGroup)group).getStoredSearches();
		
	}

	@Override
	public XdatStoredSearch getSearchForGroup(UserGroupI group, String id) {
		return ((UserGroup)group).getStoredSearch(id);
	}
 
	@Override
	public void replacePreLoadedSearchForGroup(UserGroupI group, XdatStoredSearch i) {
		((UserGroup)group).replacePreLoadedSearch(i);
	}

}
