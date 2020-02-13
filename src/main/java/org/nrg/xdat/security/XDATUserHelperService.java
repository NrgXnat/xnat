/*
 * core: org.nrg.xdat.security.XDATUserHelperService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.services.UserHelperServiceI;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.security.UserI;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class XDATUserHelperService extends UserHelperServiceI implements Serializable {
	public XDATUserHelperService(final UserI user) throws UserNotFoundException, UserInitException {
        if (user == null) {
            throw new UserNotFoundException("null");
        }
        _user = (user instanceof XDATUser) ? (XDATUser) user : new XDATUser(user.getUsername());
        _username = user.getUsername();
    }
    
    public XDATUserHelperService() {}

    @Override
    public UserI getUser() {
    	if(_user==null){
    		try {
				_user=new XDATUser(_username);
			} catch (UserNotFoundException | UserInitException e) {
	            log.error("", e);
			}
    	}
        return _user;
    }

    @Override
    public XFTTable getQueryResults(final String query) throws SQLException, DBPoolException {
        return ((XDATUser)getUser()).getQueryResults(query);
    }

    @Override
    public List<List> getQueryResultsAsArrayList(final String query) throws SQLException, DBPoolException {
        return ((XDATUser)getUser()).getQueryResultsAsArrayList(query);
    }

    @Override
    public List<List> getQueryResults(final String xmlPaths, final String rootElement) {
        return ((XDATUser)getUser()).getQueryResults(xmlPaths, rootElement);
    }

    @Override
    public boolean isOwner(final String tag) {
        return ((XDATUser)getUser()).isOwner(tag);
    }

    @Override
    public Map<String, Long> getReadableCounts() {
        return ((XDATUser)getUser()).getReadableCounts();
    }

    @Override
    public List<ItemI> getCachedItems(final String elementName, final String securityPermission, final boolean preLoad) {
        return ((XDATUser)getUser()).getCachedItems(elementName, securityPermission, preLoad);
    }

    @Override
    public List<ItemI> getCachedItemsByFieldValue(final String elementName, final String securityPermission, final boolean preLoad, final String field, final Object value) {
        return ((XDATUser)getUser()).getCachedItemsByFieldValue(elementName, securityPermission, preLoad, field, value);
    }

    @Override
    public Map<Object, Object> getCachedItemValuesHash(final String elementName, final String securityPermission, final boolean preLoad, final String idField, final String valueField) {
        return ((XDATUser)getUser()).getCachedItemValuesHash(elementName, securityPermission, preLoad, idField, valueField);
    }

    @Override
    public Date getPreviousLogin() throws Exception {
        return ((XDATUser)getUser()).getPreviousLogin();
    }

    @Override
    public List<ElementDisplay> getSearchableElementDisplays() {
        return ((XDATUser)getUser()).getSearchableElementDisplays();
    }

    @Override
    public List<ElementDisplay> getSearchableElementDisplaysByDesc() {
        return ((XDATUser)getUser()).getSearchableElementDisplaysByDesc();
    }

    @Override
    public List<ElementDisplay> getSearchableElementDisplaysByPluralDesc() {
        return ((XDATUser)getUser()).getSearchableElementDisplaysByPluralDesc();
    }

    @Override
    public List<ElementDisplay> getBrowseableElementDisplays() {
        return ((XDATUser)getUser()).getBrowseableElementDisplays();
    }

    @Override
    public List<ElementDisplay> getBrowseableCreateableElementDisplays() {
        return ((XDATUser)getUser()).getBrowseableCreateableElementDisplays();
    }

    @Override
    public ElementDisplay getBrowseableElementDisplay(final String elementName) {
        return ((XDATUser)getUser()).getBrowseableElementDisplay(elementName);
    }

    @Override
    public List<ElementDisplay> getCreateableElementDisplays() {
        try {
            return ((XDATUser)getUser()).getCreateableElementDisplays();
        } catch (Exception e) {
            log.error("", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean hasEditAccessToSessionDataByTag(final String tag) throws Exception {
        return ((XDATUser)getUser()).hasAccessTo(tag);
    }
    
    public String toString(){
    	return "UserHelper ["+_username +"]";
    }

    private XDATUser _user=null;
    private String _username=null;
	private static final long serialVersionUID = -8490037823552870106L;
}
