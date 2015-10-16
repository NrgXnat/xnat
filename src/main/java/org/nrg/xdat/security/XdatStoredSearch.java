/*
 * org.nrg.xdat.security.XdatStoredSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.security;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.turbine.Turbine;
import org.apache.velocity.app.Velocity;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.layeredSequence.LayeredSequenceCollection;
import org.nrg.xft.layeredSequence.LayeredSequenceObjectI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")
public class XdatStoredSearch extends org.nrg.xdat.om.XdatStoredSearch implements LayeredSequenceObjectI{
    private LayeredSequenceCollection layeredSequence= new LayeredSequenceCollection();
    private static ArrayList<XdatStoredSearch> ALL_STORED_SEARCHES = null;
	public XdatStoredSearch(ItemI item)
	{
		super(item);
	}

	public XdatStoredSearch(UserI user)
	{
		super(user);
	}

	public XdatStoredSearch(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

    public static ArrayList<XdatStoredSearch> GetSearches(CriteriaCollection cc,boolean withChildren) {
        ArrayList _ALL_STORED_SEARCHES = new ArrayList<XdatStoredSearch>();
        try {
			Iterator iter = ItemSearch.GetItems(cc,null,withChildren).items().iterator();
			while (iter.hasNext()){
			   XFTItem i = (XFTItem)iter.next();
			   XdatStoredSearch xss = new XdatStoredSearch(i);
			   _ALL_STORED_SEARCHES.add(xss);
			}
		} catch (Exception e) {
			logger.error(e);
		}
        
        return _ALL_STORED_SEARCHES;
    }
    
    public static ArrayList<XdatStoredSearch> GetSearches(String field,String value,boolean withChildren) {
        CriteriaCollection cc = new CriteriaCollection("AND");
        cc.addClause(field, value);
        return GetSearches(cc,withChildren);
    }

    public static XdatStoredSearch GetPreLoadedSearch(String id,boolean withChildren)
    {
    	try {
			ArrayList<XdatStoredSearch> matches= GetSearches("xdat:stored_search/ID",id, withChildren);
			if (matches.size()>0)return matches.get(0);
		} catch (Exception e) {
			logger.error(e);
		}
		
		return null;
    }

    public static ArrayList<XdatStoredSearch> GetPreLoadedSearchesByAllowedUser(String login)
	{
        return GetSearches("xdat:stored_search.allowed_user.login", login,true);
	}
    

    public static ArrayList<XdatStoredSearch> GetPreLoadedSearchesByAllowedGroup(String groupID)
    {
        return GetSearches("xdat:stored_search.allowed_groups.groupID", groupID,true);
    }
    
    public boolean canRead(String login){
        Boolean b = getSecure();
        if (b==null)
        {
            b=Boolean.TRUE;
        }
        if (b.booleanValue())
        {
            try {
                if (hasProperty("xdat:stored_search.allowed_user.login",login))
                {
                    return true;
                }
            } catch (XFTInitException e) {
                logger.error("",e);
                return false;
            } catch (ElementNotFoundException e) {
                logger.error("",e);
                return false;
            } catch (FieldNotFoundException e) {
                logger.error("",e);
                return false;
            }
        }else{
           return true;
        }
        
        return false;
    }
	
    public void addLayeredChild(LayeredSequenceObjectI o){
        layeredSequence.addSequencedItem(o);
    }
    
    public ArrayList getLayeredChildren(){
        return this.layeredSequence.getItems();
    }
    
    public String outputHTMLMenu(String server,int anchorCounter)
    {
        StringBuffer sb = new StringBuffer();
        
        sb.append("<a href=\"").append(server);
        sb.append("app/action/BundleAction/bundle/").append(this.getId()).append("\">");
        sb.append(this.getDescription()).append("</a>");
        
        if (this.getLayeredChildren().size()>0)
        {
            ArrayList al = getLayeredChildren();
	        if (al != null && al.size()>0)
	        {
	            sb.append("<span ID=\"span").append(this.getId()).append("\" style=\"position:relative; display:none;\">");
	            sb.append("<TABLE align=\"left\" valign=\"top\">");
	            for (int i=0;i<al.size();i++)
	            {
	                XdatStoredSearch xss = (XdatStoredSearch)al.get(i);
	                sb.append("\n\t<tr><td valign=\"top\">");
	                if (xss.getLayeredChildren().size()>0)
	                {
	                    String temp= xss.getId();
		                sb.append("<A NAME=\"LINK").append(temp).append("\"");
		                sb.append(" HREF=\"#LINK").append(temp).append("\" ");
		                sb.append("onClick=\" return blocking('").append(temp).append("');\">");
		                sb.append("<img ID=\"IMG").append(temp).append("\" src=\"");
		                sb.append(Turbine.getContextPath()).append("/images/plus.jpg\" border=0>");
	                }else{
		                sb.append("&#8226;");
	                }
	                sb.append(" </td><td align=\"left\">");
	                sb.append(xss.outputHTMLMenu(server,anchorCounter));
	                sb.append("\n\t</TD></TR>");
	            }
	            sb.append("\n</TABLE>");
	            sb.append("</span>");
	        }
        }
        
        return sb.toString();
    }
    
    public String toString()
    {
        String s=  this.getId();
        for(int i=0;i<this.getLayeredChildren().size();i++)
        {
            s+= "\n\t" + this.getLayeredChildren().get(i).toString();
        }
        
        return s;
    }

    
    public String getCustomSearchVM()
    {
        try {
            String templateName = "/screens/" + this.getId().toLowerCase() + "_search.vm";

            if (Velocity.resourceExists(templateName))
            {
                return templateName;
            }else
            {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    public XdatSearchField getField(String elementName, String field){
    	for(XdatSearchField sf:this.getSearchField()){
    		if(sf.getElementName().equals(elementName) && sf.getFieldId().equals(field)){
    			return sf;
    		}
    	}
    	
    	return null;
    }
    
    public XdatSearchField getField(String elementName, String field, String value){
    	for(XdatSearchField sf:this.getSearchField()){
    		if(sf.getValue()!=null){
        		if(sf.getElementName().equals(elementName) && sf.getFieldId().equals(field) && sf.getValue().equals(value)){
        			return sf;
        		}
    		}
    	}
    	
    	return null;
    }
}
