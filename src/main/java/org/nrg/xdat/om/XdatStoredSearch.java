/*
 * org.nrg.xdat.om.XdatStoredSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/21/14 9:52 AM
 */


package org.nrg.xdat.om;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.collections.DisplayFieldCollection;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.om.base.BaseXdatStoredSearch;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatCriteriaSet;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatStoredSearch extends BaseXdatStoredSearch {

	public XdatStoredSearch(ItemI item)
	{
		super(item);
	}

	public XdatStoredSearch(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatStoredSearch(UserI user)
	 **/
	public XdatStoredSearch()
	{}

	public XdatStoredSearch(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    
    public ArrayList getSearchFields() throws XFTInitException,ElementNotFoundException,FieldNotFoundException,Exception
    {
        ArrayList al = this.getSearchField();
        Collections.sort(al,XdatSearchField.SequenceComparator);
        return al;
    }


    /**
     * @return Returns the secure.
     */
    public Boolean getSecure() {
        Boolean _Secure = super.getSecure();
        if (_Secure==null)
        {
            _Secure=Boolean.TRUE;
        }
        return _Secure;
    }
    
    public boolean hasAllowedUser(final String username){
    	//implementation checks against database, in case local copy has been modified
    	
    	//PoolDBUtils.ReturnStatisticQuery(query, column, db, userName))
    	for(XdatStoredSearchAllowedUserI au: this.getAllowedUser()){
    		if(StringUtils.equals(au.getLogin(),username)){
    			return true;
    		}
    	}
    	
    	return false;
    }

    public ItemSearch getItemSearch(UserI user) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,Exception
    {
        ItemSearch ds = new ItemSearch();
        ds.setUser(user);
        ds.setElement(this.getRootElementName());
        
        //WHERE CLAUSE
        Iterator wheres = this.getChildItems("xdat:stored_search.search_where").iterator();
        while (wheres.hasNext())
        {
            XdatCriteriaSet set = new XdatCriteriaSet((XFTItem)wheres.next());
            ds.addCriteria(set.getItemSearchCriteria());
        }
        
        return ds;
    }

    public DisplaySearch getDisplaySearch(UserI user) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,Exception
    {
        DisplaySearch ds = new DisplaySearch();
        ds.setUser(user);
        ds.setTitle(this.getDescription());
        ds.setDescription(this.getDescription());
        ds.setRootElement(this.getRootElementName());
        ds.setStoredSearch(this);
        Boolean b = this.getAllowDiffColumns();
        if (b!=null)
        {
            ds.setAllowDiffs(b.booleanValue());
        }
        try {
            Iterator sfs = getSearchFields().iterator();
            while (sfs.hasNext())
            {
                XdatSearchField sf = (XdatSearchField)sfs.next();
                String e = sf.getElementName();
                String f = sf.getFieldId();
                String h = sf.getHeader();
                if(sf.getValue()!=null){
                	//remove value if it is appended to the field ID (deprecated value method)
                	if(f.indexOf("="+sf.getValue())>-1){
	                    int i =f.indexOf("="+sf.getValue());
	                    if (i>0){
	                         f = f.substring(0,i);
	                    }
                	}else if(f.indexOf("."+sf.getValue())>-1){
                		int i =f.indexOf("."+sf.getValue());
                        if (i>0){
                            f = f.substring(0,i);
                        }            		
                	}
                     
                }else{
                	//handle value passed by deprecated method
                	if(f.indexOf("=")>-1){
                		sf.setValue(f.substring(f.indexOf("=")+1));
                		f=f.substring(0,f.indexOf("="));
                	}
                	if(f.indexOf('.')>-1){
                		sf.setValue(f.substring(f.indexOf('.')+1));
                		f=f.substring(0,f.indexOf('.'));
                	}
                }
                
                if (h!=null && !h.trim().equals("")){
                    ds.addDisplayField(e,f,h,sf.getValue(),sf.getVisible());
                }else{
                    ds.addDisplayField(e,f,null,(Object)sf.getValue(),sf.getVisible());
                }
            }
        } catch (FieldNotFoundException e) {
            e.printStackTrace();
            logger.error("",e);
        }
        
        XdatCriteriaSet set=this.getInClause();
        if(set!=null){
        	for(XdatCriteria crit:set.getCriteria()){
        		ds.getInClauses().put(crit.getSchemaField(), crit.getValue());
        	}
        }else{
            //WHERE CLAUSE
            Iterator wheres = this.getChildItems("xdat:stored_search.search_where").iterator();
            while (wheres.hasNext())
            {
                set = new XdatCriteriaSet((XFTItem)wheres.next());
                ds.addCriteria(set.getDisplaySearchCriteria());
            }
        }
                
        //SORT BY
        if (this.getSortBy_elementName()!=null &&  this.getSortBy_fieldId()!=null)
            ds.setSortBy(this.getSortBy_elementName() + "." + this.getSortBy_fieldId());
        
//      boolean hasUser = false;
//        if (this.getSecure().booleanValue())
//        {
//            Iterator users = this.getChildItems("xdat:stored_search.allowed_user").iterator();
//            while (users.hasNext())
//            {
//                XFTItem u = (XFTItem)users.next();
//                if(user.getUsername().equalsIgnoreCase(u.getStringProperty("login")))
//                {
//                    hasUser = true;
//                    break;
//                }
//            }
//        }else{
//            hasUser=true;
//        }
//      
//      if (! hasUser)
//      {
//          ds = null;
//      }
        
        return ds;
    }
    
    public XdatCriteriaSet getInClause(){
    	try {
			Iterator wheres = this.getChildItems("xdat:stored_search.search_where").iterator();
			while (wheres.hasNext())
			{
			    XdatCriteriaSet set = new XdatCriteriaSet((XFTItem)wheres.next());
			    
			    if(set.hasInClause())return set;
			}
		} catch (XFTInitException e) {
			logger.error(e);
		} catch (ElementNotFoundException e) {
			logger.error(e);
		} catch (FieldNotFoundException e) {
			logger.error(e);
		}
		
		return null;
    }

    public DisplaySearch getCSVDisplaySearch(UserI user) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,Exception
    {
        DisplaySearch ds = new DisplaySearch();
        ds.setUser(user);
        ds.setTitle(this.getDescription());
        ds.setDescription(this.getDescription());
        ds.setRootElement(this.getRootElementName());
        ds.setStoredSearch(this);
        Boolean b = this.getAllowDiffColumns();
        if (b!=null)
        {
            ds.setAllowDiffs(b.booleanValue());
        }
        try {
            Iterator sfs = getSearchFields().iterator();
            while (sfs.hasNext())
            {
                XdatSearchField sf = (XdatSearchField)sfs.next();
                String e = sf.getElementName();
                String f = sf.getFieldId();
                String h = sf.getHeader();
                if(sf.getValue()!=null){
                    int i =f.indexOf("="+sf.getValue());
                    if (i>0){
                        f = f.substring(0,i);
                    }
               }else{
               	if(f.indexOf("=")>-1){
               		sf.setValue(f.substring(f.indexOf("=")+1));
               		f=f.substring(0,f.indexOf("="));
               	}
               }
                
                SchemaElement se = SchemaElement.GetElement(e);
                try {
                    DisplayField df = se.getDisplayField(f + "_CSV");
                    f = f + "_CSV";
                } catch (DisplayFieldCollection.DisplayFieldNotFoundException e1) {}
                
                
                if (h!=null && !h.trim().equals("")){
                    ds.addDisplayField(e,f,h,sf.getValue());
                }else{
                    ds.addDisplayField(e,f,(Object)sf.getValue());
                }
            }
        } catch (FieldNotFoundException e) {
            e.printStackTrace();
            logger.error("",e);
        }
        
        XdatCriteriaSet set=this.getInClause();
        if(set!=null){
        	for(XdatCriteria crit:set.getCriteria()){
        		ds.getInClauses().put(crit.getSchemaField(), crit.getValue());
        	}
        }else{
            //WHERE CLAUSE
            Iterator wheres = this.getChildItems("xdat:stored_search.search_where").iterator();
            while (wheres.hasNext())
            {
                set = new XdatCriteriaSet((XFTItem)wheres.next());
                ds.addCriteria(set.getDisplaySearchCriteria());
            }
        }
        
        //SORT BY
        if (this.getSortBy_elementName()!=null &&  this.getSortBy_fieldId()!=null)
            ds.setSortBy(this.getSortBy_elementName() + "." + this.getSortBy_fieldId());
        
//      boolean hasUser = false;
//        if (this.getSecure().booleanValue())
//        {
//            Iterator users = this.getChildItems("xdat:stored_search.allowed_user").iterator();
//            while (users.hasNext())
//            {
//                XFTItem u = (XFTItem)users.next();
//                if(user.getUsername().equalsIgnoreCase(u.getStringProperty("login")))
//                {
//                    hasUser = true;
//                    break;
//                }
//            }
//        }else{
//            hasUser=true;
//        }
//      
//      if (! hasUser)
//      {
//          ds = null;
//      }
        
        return ds;
    }
}
