package org.nrg.xdat.security.services;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.XDATUserHelperService;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.FavEntries;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;

public abstract class UserHelperServiceI {
    static Logger logger = Logger.getLogger(UserHelperServiceI.class);
	public abstract void setUser(UserI user);
	public abstract UserI getUser();
	
	public abstract XFTTable getQueryResults(String query) throws SQLException, DBPoolException;
	public abstract List<List> getQueryResultsAsArrayList(String query) throws SQLException, DBPoolException;
	public abstract List<List> getQueryResults(String xmlPaths, String rootElement);
	
	public abstract boolean isOwner(String tag);
	public abstract Map getReadableCounts();
	public abstract Map getTotalCounts();
	
	public abstract List<ItemI> getCachedItems(String elementName, String security_permission, boolean preLoad);
	public abstract List<ItemI> getCachedItemsByFieldValue(String elementName, String security_permission, boolean preLoad, String field, Object value);
	public abstract Map<Object, Object> getCachedItemValuesHash(String elementName, String security_permission, boolean preLoad, String idField, String valueField);
	
	public abstract Date getPreviousLogin() throws SQLException, Exception;
	
	public abstract List<ElementDisplay> getSearchableElementDisplays();
	public abstract List<ElementDisplay> getSearchableElementDisplaysByDesc();
	public abstract List<ElementDisplay> getSearchableElementDisplaysByPluralDesc();
	public abstract List<ElementDisplay> getBrowseableElementDisplays();
    public abstract List<ElementDisplay> getBrowseableCreateableElementDisplays();
	public abstract ElementDisplay getBrowseableElementDisplay(String elementName);
	public abstract List<ElementDisplay> getCreateableElementDisplays();


    public ElementSecurity getElementSecurity(String name) throws Exception {
        return ElementSecurity.GetElementSecurity(name);
    }
    
    public boolean canCreate(String rootElement){
    	return Permissions.canAny(getUser(), rootElement, SecurityManager.CREATE);
    }


	public boolean canCreate(ItemI item) throws Exception{
		return Permissions.canCreate(getUser(), item);
	}

	public boolean canRead(ItemI item) throws Exception{
		return Permissions.canRead(getUser(), item);
	}

	public boolean canEdit(ItemI item) throws Exception{
		return Permissions.canEdit(getUser(), item);
	}

	public boolean canDelete(ItemI item) throws Exception{
		return Permissions.canDelete(getUser(), item);
	}

	public boolean canCreate(String xmlPath, Object value) throws Exception{
		return Permissions.canCreate(getUser(), xmlPath, value);
	}

	public boolean canEdit(String xmlPath, Object value) throws Exception{
		return Permissions.canEdit(getUser(), xmlPath, value);
	}

	public boolean canDelete(String xmlPath, Object value) throws Exception{
		return Permissions.canDelete(getUser(), xmlPath, value);
	}

	public boolean canRead(String xmlPath, Object value) throws Exception{
		return Permissions.canRead(getUser(), xmlPath, value);
	}
	
	public List<Object> getAllowedValues(String elementName, String xmlPath, String action){
		return Permissions.getAllowedValues(getUser(), elementName, xmlPath, action);
	}
	
	public Map<String,Object> getAllowedValues(String elementName, String action){
		return Permissions.getAllowedValues(getUser(), elementName, action);
	}
	
	public List<XdatStoredSearch> getStoredSearches(){
		return UserHelper.getSearchHelperService().getSearchesForUser(getUser());
	}
	
	public boolean isMember(String id){
		return Groups.isMember(getUser(), id);
	}
	
	public List<String> getGroupIds(){
		return Groups.getGroupIdsForUser(getUser());
	}
	

	public DisplayManager getDisplayManager() {
        return DisplayManager.GetInstance();
    }

    public ArrayList getAllItems(String elementName, boolean preLoad) {
        try {
            ArrayList al = ItemSearch.GetAllItems(elementName, getUser(), preLoad).getItems();
            if (al.size() > 0) {
                al = BaseElement.WrapItems(al);
            }
            return al;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList();
        }
    }

    public ArrayList getAllItems(String elementName, boolean preLoad, String sortBy) {
        try {
            ArrayList al = ItemSearch.GetAllItems(elementName, getUser(), preLoad).getItems(sortBy);
            if (al.size() > 0) {
                al = BaseElement.WrapItems(al);
            }
            return al;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList();
        }
    }

    public ArrayList getAllItems(String elementName, boolean preLoad, String sortBy, String sortOrder) {
        try {
            ArrayList al = ItemSearch.GetAllItems(elementName, getUser(), preLoad).getItems(sortBy, sortOrder);
            if (al.size() > 0) {
                al = BaseElement.WrapItems(al);
            }
            return al;
        } catch (Exception e) {
            logger.error("", e);
            return new ArrayList();
        }
    }
    
    public boolean isFavorite(String elementName, String id) {
        FavEntries fe = null;
        try {
            fe = FavEntries.GetFavoriteEntries(elementName, id, getUser());
        } catch (DBPoolException e) {
            logger.error("", e);
        } catch (SQLException e) {
            logger.error("", e);
        }
        if (fe == null) {
            return false;
        } else {
            return true;
        }
    }
}
