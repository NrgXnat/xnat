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

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class XDATUserHelperService extends UserHelperServiceI {
    public XDATUserHelperService(final UserI user) throws UserNotFoundException, UserInitException {
        if (user == null) {
            throw new UserNotFoundException("null");
        }
        _user = (user instanceof XDATUser) ? (XDATUser) user : new XDATUser(user.getUsername());
    }

    @Override
    public UserI getUser() {
        return _user;
    }

    @Override
    public XFTTable getQueryResults(final String query) throws SQLException, DBPoolException {
        return _user.getQueryResults(query);
    }

    @Override
    public List<List> getQueryResultsAsArrayList(final String query) throws SQLException, DBPoolException {
        return _user.getQueryResultsAsArrayList(query);
    }

    @Override
    public List<List> getQueryResults(final String xmlPaths, final String rootElement) {
        return _user.getQueryResults(xmlPaths, rootElement);
    }

    @Override
    public boolean isOwner(final String tag) {
        return _user.isOwner(tag);
    }

    @Override
    public Map getReadableCounts() {
        return _user.getReadableCounts();
    }

    @Override
    public Map getTotalCounts() {
        return _user.getTotalCounts();
    }

    @Override
    public List<ItemI> getCachedItems(final String elementName, final String securityPermission, final boolean preLoad) {
        return _user.getCachedItems(elementName, securityPermission, preLoad);
    }

    @Override
    public List<ItemI> getCachedItemsByFieldValue(final String elementName, final String securityPermission, final boolean preLoad, final String field, final Object value) {
        return _user.getCachedItemsByFieldValue(elementName, securityPermission, preLoad, field, value);
    }

    @Override
    public Map<Object, Object> getCachedItemValuesHash(final String elementName, final String securityPermission, final boolean preLoad, final String idField, final String valueField) {
        return _user.getCachedItemValuesHash(elementName, securityPermission, preLoad, idField, valueField);
    }

    @Override
    public Date getPreviousLogin() throws Exception {
        return _user.getPreviousLogin();
    }

    @Override
    public List<ElementDisplay> getSearchableElementDisplays() {
        return _user.getSearchableElementDisplays();
    }

    @Override
    public List<ElementDisplay> getSearchableElementDisplaysByDesc() {
        return _user.getSearchableElementDisplaysByDesc();
    }

    @Override
    public List<ElementDisplay> getSearchableElementDisplaysByPluralDesc() {
        return _user.getSearchableElementDisplaysByPluralDesc();
    }

    @Override
    public List<ElementDisplay> getBrowseableElementDisplays() {
        return _user.getBrowseableElementDisplays();
    }

    @Override
    public List<ElementDisplay> getBrowseableCreateableElementDisplays() {
        return _user.getBrowseableCreateableElementDisplays();
    }

    @Override
    public ElementDisplay getBrowseableElementDisplay(final String elementName) {
        return _user.getBrowseableElementDisplay(elementName);
    }

    @Override
    public List<ElementDisplay> getCreateableElementDisplays() {
        try {
            return _user.getCreateableElementDisplays();
        } catch (Exception e) {
            log.error("", e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean hasEditAccessToSessionDataByTag(final String tag) throws Exception {
        return _user.hasAccessTo(tag);
    }

    private final XDATUser _user;
}
