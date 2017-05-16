/*
 * core: org.nrg.xdat.security.XDATUserHelperService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class XDATUserHelperService extends UserHelperServiceI {
    static Logger logger = Logger.getLogger(XDATUserHelperService.class);
	private XDATUser user;
	
	@Override
	public void setUser(UserI user) {
		this.user=(XDATUser)user;
	}

	@Override
	public UserI getUser() {
		return user;
	}

	@Override
	public XFTTable getQueryResults(String query) throws SQLException,
			DBPoolException {
		return user.getQueryResults(query);
	}

	@Override
	public List<List> getQueryResultsAsArrayList(String query)
			throws SQLException, DBPoolException {
		return user.getQueryResultsAsArrayList(query);
	}

	@Override
	public List<List> getQueryResults(String xmlPaths, String rootElement) {
		return user.getQueryResults(xmlPaths, rootElement);
	}

	@Override
	public boolean isOwner(String tag) {
		return user.isOwner(tag);
	}

	@Override
	public Map getReadableCounts() {
		return user.getReadableCounts();
	}

	@Override
	public Map getTotalCounts() {
		return user.getTotalCounts();
	}

	@Override
	public List<ItemI> getCachedItems(String elementName, String security_permission, boolean preLoad) {
		return user.getCachedItems(elementName, security_permission, preLoad);
	}

	@Override
	public List<ItemI> getCachedItemsByFieldValue(String elementName, String security_permission, boolean preLoad, String field, Object value) {
		return user.getCachedItemsByFieldValue(elementName, security_permission, preLoad, field, value);
	}

	@Override
	public Map<Object, Object> getCachedItemValuesHash(String elementName, String security_permission, boolean preLoad, String idField,	String valueField) {
		return user.getCachedItemValuesHash(elementName, security_permission, preLoad, idField, valueField);
	}

	@Override
	public Date getPreviousLogin() throws Exception {
		return user.getPreviousLogin();
	}

	@Override
	public List<ElementDisplay> getSearchableElementDisplays() {
		return user.getSearchableElementDisplays();
	}

	@Override
	public List<ElementDisplay> getSearchableElementDisplaysByDesc() {
		return user.getSearchableElementDisplaysByDesc();
	}

	@Override
	public List<ElementDisplay> getSearchableElementDisplaysByPluralDesc() {
		return user.getSearchableElementDisplaysByPluralDesc();
	}

	@Override
	public List<ElementDisplay> getBrowseableElementDisplays() {
		return user.getBrowseableElementDisplays();
	}

	@Override
	public List<ElementDisplay> getBrowseableCreateableElementDisplays() {
		return user.getBrowseableCreateableElementDisplays();
	}

	@Override
	public ElementDisplay getBrowseableElementDisplay(String elementName) {
		return user.getBrowseableElementDisplay(elementName);
	}

	@Override
	public List<ElementDisplay> getCreateableElementDisplays() {
		try {
			return user.getCreateableElementDisplays();
		} catch (Exception e) {
			logger.error("",e);
			return Lists.newArrayList();
		}
	}

	@Override
	public boolean hasEditAccessToSessionDataByTag(String tag) throws Exception {
		return user.hasAccessTo(tag);
	}
}
