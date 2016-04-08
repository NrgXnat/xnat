/*
 * org.nrg.xft.db.DBItemCache
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;

/**
 * @author Tim
 *
 */
public class DBItemCache {
	static org.apache.log4j.Logger logger = Logger.getLogger(DBItemCache.class);
    private ItemCollection saved = new ItemCollection();
    private ItemCollection removed = new ItemCollection();
    private ItemCollection preexisting = new ItemCollection();
    private ItemCollection dbTrigger = new ItemCollection();
    private ItemCollection modified = new ItemCollection();

    private ArrayList<String> sql = new ArrayList<String>();

    private Date mod_time;
    private Object change_id;
    private String comment;
    private Object event_id;
    
    private static String dbName=null,table=null,pk=null,sequence=null;
    
    private final UserI user;
    
    /**
     *
     */
    public DBItemCache(UserI user,EventMetaI event) throws Exception {
        this.mod_time=EventUtils.getEventDate(event, false);
               
        this.comment=(event==null)?null:event.getMessage();

        this.event_id=(event==null)?null:event.getEventId();
        
        this.user=user;
    }
    
//    /**
//     *
//     */
//    public DBItemCache(UserI user,String comment) throws Exception {
//        this(user,comment,null);
//    }
    
    public void setComment(String s){
    	this.comment=s;
    }

    public Date getModTime(){
    	return mod_time;
    }
    
    public Object getChangeId() throws Exception{
    	if(change_id==null){
            change_id=getNextChangeID();
    	}
    	return change_id;
    }
    
    private synchronized static Object getNextChangeID() throws Exception{
        if(dbName==null){
        	try {
				GenericWrapperElement element=GenericWrapperElement.GetElement("xdat:change_info");
				dbName=element.getDbName();
				table=element.getSQLName();
				pk="xdat_change_info_id";
				sequence=element.getSequenceName();
			} catch (XFTInitException e) {
				logger.error("",e);
			} catch (ElementNotFoundException e) {
				logger.error("",e);
			}
        }
    	return PoolDBUtils.GetNextID(dbName, table, pk, sequence);
    }

    public void addStatement(String query)
    {
        if (!query.endsWith(";"))
        {
            query += ";";
        }

        sql.add("\n" + query);
    }

    public ArrayList<String> getStatements()
    {
        return sql;
    }
    
    public void finalize() throws Exception{    	
		XFTItem cache_info=XFTItem.NewItem("xdat:change_info", user);
		cache_info.setDirectProperty("change_date",getModTime());
		if (user != null)
		{
			cache_info.setDirectProperty("change_user",user.getID());
		}
		cache_info.setDirectProperty("xdat_change_info_id",this.getChangeId());
		
		if(this.comment!=null){
			cache_info.setDirectProperty("comment",this.comment);
		}
		
		if(this.event_id!=null){
			cache_info.setDirectProperty("event_id",this.event_id);
		}
		
		DBAction.InsertItem(cache_info, null, this, false);
    }

    public String getSQL()
    {
        StringBuffer sb = new StringBuffer();
        Iterator<String> iter = sql.iterator();
        while (iter.hasNext())
        {
            sb.append(iter.next());
        }
        return sb.toString();
    }

    public void reset() throws Exception
    {
        this.sql = new ArrayList<String>();

        saved.clear();
        removed.clear();
        preexisting.clear();
    }

    public String toString()
    {
        return this.sql.toString();
    }
    /**
     * @return Returns the preexisting.
     */
    public ItemCollection getPreexisting() {
        return preexisting;
    }
    /**
     * @param preexisting The preexisting to set.
     */
    public void setPreexisting(ItemCollection preexisting) {
        this.preexisting = preexisting;
    }
    /**
     * @return Returns the removed.
     */
    public ItemCollection getRemoved() {
        return removed;
    }
    /**
     * @param removed The removed to set.
     */
    public void setRemoved(ItemCollection removed) {
        this.removed = removed;
    }
    /**
     * @return Returns the saved.
     */
    public ItemCollection getSaved() {
        return saved;
    }
    /**
     * @param saved The saved to set.
     */
    public void setSaved(ItemCollection saved) {
        this.saved = saved;
    }

    public void store(String fileName, String dbName,UserI user) throws Exception
    {
       XFT.LogInsert(getSQL(),fileName);
 	   if (!getSQL().equals("") && !getSQL().equals("[]"))
 	   {
 		   PoolDBUtils con = null;
 			try {
 				con = new PoolDBUtils();
 				String username = null;
 				if (user != null)
 				{
 				    username = user.getUsername();
 				}
 				con.sendBatch(this,dbName,username);
 			} catch (SQLException e) {
 				throw e;
 			} catch (Exception e) {
 				throw e;
 			}
 	   }
    }

    /**
     * @return the modified
     */
    public ItemCollection getDBTriggers() {
        return dbTrigger;
    }

    /**
     * @param modified the modified to set
     */
    public void setDBTriggers(ItemCollection modified) {
        this.dbTrigger = modified;
    }

    public void prependStatments(ArrayList<String> new_statements){
    	this.sql.addAll(0, new_statements);
    }

    public void appendStatments(ArrayList<String> new_statements){
    	this.sql.addAll(new_statements);
    }

	public ItemCollection getModified() {
		return modified;
	}

	public void setModified(ItemCollection modified) {
		this.modified = modified;
	}
    
	static int next_id =0;
	public final synchronized static Integer getNextExternalId(){
		return Integer.valueOf(next_id++);
	}
	
	public void handlePostModificationAction(XFTItem item, String action) {
		try {
			final String element=item.getGenericSchemaElement().getJAVAName();
			final String packageName="org.nrg.xnat.extensions.db." + action + "." + element;
			if(Reflection.getClassesForPackage(packageName).size()>0){
				Map<String,Object> params=new HashMap<String,Object>();
				params.put("transaction",this);
				params.put("item",item);
					
				Reflection.injectDynamicImplementations(packageName, params);
				
			}
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public static abstract class PostModificationActionAbst implements Reflection.InjectableI{
		protected XFTItem item;
		protected DBItemCache transaction;
		public PostModificationActionAbst(){
		}
		
		public void execute(Map<String,Object> params){
			item=(XFTItem) params.get("item");
			transaction=(DBItemCache) params.get("transaction");
			
			execute();
		}
		
		public abstract void execute();
	}
}
