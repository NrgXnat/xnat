//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Oct 20, 2004
 */
package org.nrg.xft.db;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTMappingColumn;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.SecurityManagerI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.DateUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.StringUtils;
/**
 * Class used to store XFTItems to the database.
 * 
 * <BR><BR>To insert or update an item into the db, the StoredItem method is used.  If the item has
 * a specified pk (not usually defined in xml) then it is assumed to be an update.  If it doesn't have
 * a pk, then a select is performed to see if there are any rows where all of the item's properties match.
 * If so, then this is assumed to be the same row.  Otherwise, a new row is generated.
 * 
 * @author Tim
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DBAction {
	static org.apache.log4j.Logger logger = Logger.getLogger(DBAction.class);
	
	private static boolean ADJUSTED_SEQUENCES = false;
	
	private static Hashtable sequences = new Hashtable();
	/**
	 * This method is used to insert/update an item into the database.
	 * 
	 * <BR><BR>First, if the item has an extended field, then the extended field is populated with 
	 * the extension name.  Next, it stores the single-reference items.  The pk values of those items 
	 * are then copied into this item as foreign-keys.  If this item is a single column item then its pk
	 * is set manually using the nextVal().  If the item has its primary key, then a select is performed
	 * to verify if the record already exists based on its pks.  If so, then it is an UPDATE statement.
	 * If the record does not exist, then an INSERT is performed.  If the item did not have a pk value
	 * set, then a select is performed to see if there are any rows in the table that have all of the
	 * item's field values.  If one is found, then it is assumed that this item is a duplicate of that
	 * row.  Otherwise, a new row is INSERTed.
	 * 
	 * @param item
	 * @return updated XFTItem
	 */
	public static boolean StoreItem(XFTItem item, UserI user,boolean checkForDuplicates,boolean quarantine, boolean overrideQuarantine, boolean allowItemOverwrite,SecurityManagerI securityManager,EventMetaI c) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,SQLException,Exception
	{	    
       long totalStartTime= Calendar.getInstance().getTimeInMillis();
       long localStartTime= Calendar.getInstance().getTimeInMillis();
	   DBItemCache cache =  new DBItemCache(user,c);
	   item = StoreItem(item,user,checkForDuplicates,new ArrayList(),quarantine,overrideQuarantine,allowItemOverwrite,cache,securityManager,false);

       logger.debug("prepare-sql: "+(Calendar.getInstance().getTimeInMillis()-localStartTime) + " ms");
       localStartTime= Calendar.getInstance().getTimeInMillis();
       
	   if (!cache.getSQL().equals("") && !cache.getSQL().equals("[]"))
	   {
	       Quarantine(item,user,quarantine,overrideQuarantine,cache);
	       
	       logger.debug("quarantine-sql: "+(Calendar.getInstance().getTimeInMillis()-localStartTime) + " ms");
	       localStartTime= Calendar.getInstance().getTimeInMillis();
	       
		   XFT.LogInsert(cache.getSQL(),item);
		   
		   PoolDBUtils con = null;
			try {
				String username = null;
				Integer xdat_user_id = null;
				if (user != null)
				{
				    username = user.getUsername();
				    xdat_user_id=user.getID();
				}
                if (!cache.getDBTriggers().contains(item, false)){
                    cache.getDBTriggers().add(item);
                }

                if(cache.getRemoved().size()>0){
                	if(XFT.VERBOSE)System.out.println("***** " + cache.getRemoved().size() + " REMOVED ITEMS *******");
                	PerformUpdateTriggers(cache,username,xdat_user_id,false);
                }
                //PerformUpdateTriggers(cache,username,xdat_user_id,(cache.getRemoved().size()>0)?false:true);
                
                logger.debug("pre-triggers: "+(Calendar.getInstance().getTimeInMillis()-localStartTime) + " ms");
                localStartTime= Calendar.getInstance().getTimeInMillis();

				con = new PoolDBUtils();				
				con.sendBatch(cache,item.getDBName(),username);
				if(XFT.VERBOSE)System.out.println("Item modifications stored. " + cache.getDBTriggers().size() + " modified elements. " + cache.getStatements().size() + " SQL statements.");

                
                logger.debug("store: "+(Calendar.getInstance().getTimeInMillis()-localStartTime) + " ms");
                localStartTime= Calendar.getInstance().getTimeInMillis();

                PerformUpdateTriggers(cache,username,xdat_user_id,false);
                
                logger.debug("post-triggers: "+(Calendar.getInstance().getTimeInMillis()-localStartTime) + " ms");
                localStartTime= Calendar.getInstance().getTimeInMillis();
                
                logger.debug("Total: "+(Calendar.getInstance().getTimeInMillis()-totalStartTime) + " ms");
                return true;
			} catch (SQLException e) {
				throw e;
			} catch (Exception e) {
				throw e;
			}
	   }else{
	       logger.info("Pre-existing item found without modifications");
	       if(XFT.VERBOSE)System.out.println("Pre-existing item found without modifications");
           return false;
       }
	   
	}
	
	public static void executeCache(final DBItemCache cache, final UserI user, final String db, final String logFileName) throws Exception{
		 	XFT.LogInsert(cache.getSQL(),logFileName);
		   
		    PoolDBUtils con = null;
			try {
				String username = null;
				Integer xdat_user_id = null;
				if (user != null)
				{
				    username = user.getUsername();
				    xdat_user_id=user.getID();
				}

                if(cache.getRemoved().size()>0){
                	PerformUpdateTriggers(cache,username,xdat_user_id,false);
                }
                
                con = new PoolDBUtils();				
				con.sendBatch(cache,db,username);
				
				PerformUpdateTriggers(cache,username,xdat_user_id,false);
                
			} catch (SQLException e) {
				throw e;
			} catch (Exception e) {
				throw e;
			}
	}
	
	/**
	 * This method is used to insert/update an item into the database.
	 * 
	 * <BR><BR>First, if the item has an extended field, then the extended field is populated with 
	 * the extension name.  Next, it stores the single-reference items.  The pk values of those items 
	 * are then copied into this item as foreign-keys.  If this item is a single column item then its pk
	 * is set manually using the nextVal().  If the item has its primary key, then a select is performed
	 * to verify if the record already exists based on its pks.  If so, then it is an UPDATE statement.
	 * If the record does not exist, then an INSERT is performed.  If the item did not have a pk value
	 * set, then a select is performed to see if there are any rows in the table that have all of the
	 * item's field values.  If one is found, then it is assumed that this item is a duplicate of that
	 * row.  Otherwise, a new row is INSERTed.
	 * 
	 * @param item
	 * @return updated XFTItem
	 */
	public static DBItemCache StoreItem(XFTItem item, UserI user,boolean checkForDuplicates,boolean quarantine, boolean overrideQuarantine, boolean allowItemOverwrite,SecurityManagerI securityManager, DBItemCache cache) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,SQLException,Exception
	{	    
	   StoreItem(item,user,checkForDuplicates,new ArrayList(),quarantine,overrideQuarantine,allowItemOverwrite,cache,securityManager,false);

	   return cache;
	}
    
    /**
     * This method is used to insert/update an item into the database.
     * 
     * <BR><BR>First, if the item has an extended field, then the extended field is populated with 
     * the extension name.  Next, it stores the single-reference items.  The pk values of those items 
     * are then copied into this item as foreign-keys.  If this item is a single column item then its pk
     * is set manually using the nextVal().  If the item has its primary key, then a select is performed
     * to verify if the record already exists based on its pks.  If so, then it is an UPDATE statement.
     * If the record does not exist, then an INSERT is performed.  If the item did not have a pk value
     * set, then a select is performed to see if there are any rows in the table that have all of the
     * item's field values.  If one is found, then it is assumed that this item is a duplicate of that
     * row.  Otherwise, a new row is INSERTed.
     * 
     * @param item
     * @return updated XFTItem
     */
    private static XFTItem StoreItem(XFTItem item, UserI user,boolean checkForDuplicates,boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,DBItemCache cache,SecurityManagerI securityManager, boolean allowFieldMatching) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,SQLException,Exception
    {
	   return StoreItem(item,user,checkForDuplicates,new ArrayList(),quarantine,overrideQuarantine,allowItemRemoval, cache,securityManager,allowFieldMatching);
    }
	/**
	 * This method is used to insert/update an item into the database.
	 * 
	 * <BR><BR>First, if the item has an extended field, then the extended field is populated with 
	 * the extension name.  Next, it stores the single-reference items.  The pk values of those items 
	 * are then copied into this item as foreign-keys.  If this item is a single column item then its pk
	 * is set manually using the nextVal().  If the item has its primary key, then a select is performed
	 * to verify if the record already exists based on its pks.  If so, then it is an UPDATE statement.
	 * If the record does not exist, then an INSERT is performed.  If the item did not have a pk value
	 * set, then a select is performed to see if there are any rows in the table that have all of the
	 * item's field values.  If one is found, then it is assumed that this item is a duplicate of that
	 * row.  Otherwise, a new row is INSERTed.
	 * 
	 * @param item
	 * @return updated XFTItem
	 */
    	private static XFTItem StoreItem(XFTItem item, UserI user,boolean checkForDuplicates, ArrayList storedRelationships,boolean quarantine, boolean overrideQuarantine, boolean allowItemOverwrite,DBItemCache cache,SecurityManagerI securityManager, boolean allowFieldMatching) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,SQLException,Exception
    	{
            boolean isNew = true;
    		try {
    
    			String login = null;
    			if (user != null)
    			{
    			    login = user.getUsername();
    			}
    			
                if (item.hasExtendedField())
                {
                	item.setExtenderName();
                }
                
                if (item.getGenericSchemaElement().isExtension())
                {
                	item.setExtenderName();
                }
                boolean localQuarantine;
                if (overrideQuarantine)
                {
                    localQuarantine = quarantine;
                }else{
                    localQuarantine = item.getGenericSchemaElement().isQuarantine(quarantine);
                }
                boolean hasOneColumnTable = StoreSingleRefs(item,false,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,true);

                if (item.getPossibleFieldNames().size() == 1)
                {
                	String keyName = (String)((Object[])item.getPossibleFieldNames().get(0))[0];
                	if (item.getProperty(keyName) == null)
                	{
                		PoolDBUtils con = null;
                		try {
                			con = new PoolDBUtils();
                			XFTTable t = con.executeSelectQuery("SELECT nextval('" + item.getGenericSchemaElement().getSQLName() + "_" + keyName + "_seq" + "')",item.getGenericSchemaElement().getDbName(),login);
                			while (t.hasMoreRows())
                			{
                				t.nextRow();
                				item.setFieldValue(keyName,t.getCellValue("nextval"));
                			}
                		} catch (SQLException e) {
                			e.printStackTrace();
                		} catch (Exception e) {
                			e.printStackTrace();
                		}
                	}
                }
                
                //Check if the primary key for this item is set.
                boolean hasPK = item.hasPK();
                
                boolean itemAlreadyStoredInCache = false;
                
                if (hasPK)
                {
                	//HAS ASSIGNED PK
                		ItemCollection al = item.getPkMatches(false);
                		if (al.size() > 0)
                		{
                		    isNew = false;
                			//ITEM EXISTS
                			XFTItem sub = (XFTItem)al.get(0);
                			String output = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + sub.getPK() + ")";
                			if (sub.getXSIType().startsWith("xdat:"))
                			{
                    			logger.debug(output);
                			}else{
                    			logger.info(output);
                			}
                			
                			if (hasOneColumnTable)
        					{
                			    DBAction.ImportNoIdentifierFKs(item,sub);
        					}
                			
                			if (HasNewFields(sub,item,allowItemOverwrite))
                			{
                				logger.debug("OLD\n" + sub.toString());
                				logger.debug("NEW\n" + item.toString());
                				item = UpdateItem(sub,item,user,localQuarantine,overrideQuarantine,cache,allowItemOverwrite);
                			}else{
                			    item.importNonItemFields(sub,allowItemOverwrite);
                			}
                			
            				cache.getPreexisting().add(item);
                			
                			if (hasOneColumnTable)
        					{
                			    sub.importNonItemFields(item,false);
        						StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                				
        						if (HasNewFields(sub,item,allowItemOverwrite))
                    			{
                    				item = UpdateItem(sub,item,user,localQuarantine,overrideQuarantine,cache,false);
                    				
                    			}
        					}
                		}else
                		{
                			if (item.hasUniques())
                			{
                				ItemCollection temp = item.getUniqueMatches(false);
                				if (temp.size() > 0)
                				{
                        		    isNew = false;
                					XFTItem duplicate = (XFTItem)temp.get(0);
                					String output = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + duplicate.getPK() + ")";
                					if (duplicate.getXSIType().startsWith("xdat:"))
                        			{
                            			logger.debug(output);
                        			}else{
                            			logger.info(output);
                        			}
                					
                        			if (hasOneColumnTable)
                					{
                        			    DBAction.ImportNoIdentifierFKs(item,duplicate);
                					}
                        			
                					if (HasNewFields(duplicate,item,allowItemOverwrite))
                					{
                						logger.debug("OLD\n" + duplicate.toString());
                						logger.debug("NEW\n" + item.toString());
                						item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,allowItemOverwrite);
                					}else{
                					    item.importNonItemFields(duplicate,allowItemOverwrite);
                        			    
                					}
                					
                    				cache.getPreexisting().add(item);
                					
                					if (hasOneColumnTable)
                					{
                        			    duplicate.importNonItemFields(item,false);
                						StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                        				
                						if (HasNewFields(duplicate,item,allowItemOverwrite))
                            			{
                            				item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,false);
                            				
                            			}
                					}
                				}else
                				{
                				    if (cache.getSaved().containsByPK(item,false))
                				    {
                				        itemAlreadyStoredInCache = true;
                	                    XFTItem duplicate = (XFTItem)cache.getSaved().findByPK(item,false);
                        				item.importNonItemFields(duplicate,false);
                        				
                        				if (HasNewFields(duplicate,item,false))
                    					{
                        				    item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,false);
                    					}
                        				
                						if (hasOneColumnTable)
                						{
                            			    DBAction.ImportNoIdentifierFKs(item,duplicate);
                							StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                						}
                				    }else if (cache.getSaved().containsByUnique(item,false))
                				    {
                				        itemAlreadyStoredInCache = true;
                	                    XFTItem duplicate = (XFTItem)cache.getSaved().findByUnique(item,false);
                        				item.importNonItemFields(duplicate,false);

                        				if (HasNewFields(duplicate,item,false))
                    					{
                        				    item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,false);
                    					}
                        				
                						if (hasOneColumnTable)
                						{
                            			    DBAction.ImportNoIdentifierFKs(item,duplicate);
                							StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                						}
                				    }else{
    //            				      ITEM IS NEW
                						if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
                						{
                							GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                							XFTItem meta=(XFTItem)item.getProperty(f);
                							if(meta == null)
                							{
                								meta = XFTItem.NewMetaDataElement(user,item.getXSIType(),localQuarantine,cache.getModTime(),cache.getChangeId());
                								StoreItem(meta,user,true,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                								
                								GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                								
                								item.setChild(ref,meta,true);
                								
                								Iterator refIter = ref.getLocalRefNames().iterator();
                								while (refIter.hasNext())
                								{
                									ArrayList refName = (ArrayList)refIter.next();
                									String localKey = (String)refName.get(0);
                									GenericWrapperField foreignKey = (GenericWrapperField)refName.get(1);
                									Object value = meta.getProperty(foreignKey.getId());
                									if (value != null)
                									{
                										
                										if (!item.setFieldValue(localKey,value)){
                											 throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
                										 }
                									}
                								}
                							}else{
                								meta.setDirectProperty("row_last_modified",cache.getModTime());
                								meta.setDirectProperty("last_modified",cache.getModTime());
                							}
                						}
                						
                						if (hasOneColumnTable)
                						{
                							StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                						}
                						
                						item = InsertItem(item,login,cache,false);
                				    }
                				    
                				}
                				
                			}else{
                			    if (!cache.getSaved().containsByPK(item,false))
            	                {
                					//ITEM IS NEW
                					if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
                					{
                						GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                						XFTItem meta=(XFTItem)item.getProperty(f);
            							if(meta == null)
            							{
                							meta = XFTItem.NewMetaDataElement(user,item.getXSIType(),localQuarantine,cache.getModTime(),cache.getChangeId());
                							StoreItem(meta,user,true,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                							
                							GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                							
                							item.setChild(ref,meta,true);
                							
                							Iterator refIter = ref.getLocalRefNames().iterator();
                							while (refIter.hasNext())
                							{
                								ArrayList refName = (ArrayList)refIter.next();
                								String localKey = (String)refName.get(0);
                								GenericWrapperField foreignKey = (GenericWrapperField)refName.get(1);
                								Object value = meta.getProperty(foreignKey.getId());
                								if (value != null)
                								{
            										if (!item.setFieldValue(localKey,value)){
            											 throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
            										 }
                								}
                							}
                						}else{
            								meta.setDirectProperty("row_last_modified",cache.getModTime());
            								meta.setDirectProperty("last_modified",cache.getModTime());
            							}
                					}
    
            						if (hasOneColumnTable)
            						{
            							StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
            						}
                					
                					item = InsertItem(item,login,cache,false);
            	                }else{
            				        itemAlreadyStoredInCache = true;
            	                    XFTItem duplicate = (XFTItem)cache.getSaved().findByPK(item,false);
                    				item.importNonItemFields(duplicate,false);

                    				if (HasNewFields(duplicate,item,false))
                					{
                    				    item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,false);
                					}
                    				
            						if (hasOneColumnTable)
            						{
                        			    DBAction.ImportNoIdentifierFKs(item,duplicate);
            							StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
            						}
            	                }
                			}
                		}
                    
                }else
                {
                	//HAS NO PK
                	if (item.hasUniques())
                	{
                		ItemCollection temp = item.getUniqueMatches(false);
                		if (temp.size() > 0)
                		{
                		    isNew = false;
                			XFTItem duplicate = (XFTItem)temp.get(0);
                			String output = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + duplicate.getPK() + ")";
                			if (duplicate.getXSIType().startsWith("xdat:"))
                			{
                    			logger.debug(output);
                			}else{
                    			logger.info(output);
                			}
        					
                			if (hasOneColumnTable)
        					{
                			    DBAction.ImportNoIdentifierFKs(item,duplicate);
        					}
                			
                			if (HasNewFields(duplicate,item,allowItemOverwrite))
                			{
                				logger.debug("OLD\n" + duplicate.toString());
                				logger.debug("NEW\n" + item.toString());
                				item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,allowItemOverwrite);
                				
                			}else{
                			    item.importNonItemFields(duplicate,allowItemOverwrite);
                			    
                			}
                			
            				cache.getPreexisting().add(item);
                			
                			if (hasOneColumnTable)
        					{
                			    duplicate.importNonItemFields(item,false);
        						StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                				
        						if (HasNewFields(duplicate,item,allowItemOverwrite))
                    			{
                    				item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,false);
                    				
                    			}
        					}
                		}else
                		{
                		    if (!cache.getSaved().containsByUnique(item,false))
        	                {
                    		    //ITEM IS NEW
                				if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
                				{
                					GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                					XFTItem meta=(XFTItem)item.getProperty(f);
        							if(meta == null)
        							{
                						meta = XFTItem.NewMetaDataElement(user,item.getXSIType(),localQuarantine,cache.getModTime(),cache.getChangeId());
                						StoreItem(meta,user,true,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                						
                						GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                						
                						item.setChild(ref,meta,true);
                						
                						Iterator refIter = ref.getLocalRefNames().iterator();
                						while (refIter.hasNext())
                						{
                							ArrayList refName = (ArrayList)refIter.next();
                							String localKey = (String)refName.get(0);
                							GenericWrapperField foreignKey = (GenericWrapperField)refName.get(1);
                							Object value = meta.getProperty(foreignKey.getId());
                							if (value != null)
                							{
        										if (!item.setFieldValue(localKey,value)){
       											 throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
       										 }
                							}
                						}
                					}else{
        								meta.setDirectProperty("row_last_modified",cache.getModTime());
        								meta.setDirectProperty("last_modified",cache.getModTime());
        							}
                				}
    
                				if (hasOneColumnTable)
                				{
                					StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                				}
                				
                				item = InsertItem(item,login,cache,false);
        	                }else{
        				        itemAlreadyStoredInCache = true;
        	                    XFTItem duplicate = (XFTItem)cache.getSaved().findByUnique(item,false);
                				item.importNonItemFields(duplicate,false);

                				if (HasNewFields(duplicate,item,false))
            					{
                				    item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,false);
            					}
                				
        	                    
                				if (hasOneColumnTable)
                				{
                    			    DBAction.ImportNoIdentifierFKs(item,duplicate);
                					StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                				}
        	                }
                				
                		}
                	}else if (item.getGenericSchemaElement().matchByValues() && allowFieldMatching){
                	    ItemCollection temp = item.getExtFieldsMatches(true);
                		if (temp.size() > 0)
                		{
                		    isNew = false;
                			XFTItem duplicate = (XFTItem)temp.get(0);
                			String output = "Duplicate " + item.getGenericSchemaElement().getFullXMLName() + " Found. (" + duplicate.getPK() + ")";
                			if (duplicate.getXSIType().startsWith("xdat:"))
                			{
                    			logger.debug(output);
                			}else{
                    			logger.info(output);
                			}
        					
                			if (hasOneColumnTable)
        					{
                			    DBAction.ImportNoIdentifierFKs(item,duplicate);
        					}
                			
                			if (HasNewFields(duplicate,item,allowItemOverwrite))
                			{
                				logger.debug("OLD\n" + duplicate.toString());
                				logger.debug("NEW\n" + item.toString());
                				item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,allowItemOverwrite);
                				
                			}else{
                			    item.importNonItemFields(duplicate,allowItemOverwrite);
                			    
                			}
                			
            				cache.getPreexisting().add(item);
                			
                			if (hasOneColumnTable)
        					{
                			    duplicate.importNonItemFields(item,false);
        						StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                				
        						if (HasNewFields(duplicate,item,allowItemOverwrite))
                    			{
                    				item = UpdateItem(duplicate,item,user,localQuarantine,overrideQuarantine,cache,false);
                    			}
        					}
                		}else
                		{
//                		  ITEM IS NEW
                			if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
                			{
                				GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                				XFTItem meta=(XFTItem)item.getProperty(f);
    							if(meta == null)
    							{
                					meta = XFTItem.NewMetaDataElement(user,item.getXSIType(),localQuarantine,cache.getModTime(),cache.getChangeId());
                					StoreItem(meta,user,true,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                					
                					GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                					
                					item.setChild(ref,meta,true);
                					
                					Iterator refIter = ref.getLocalRefNames().iterator();
                					while (refIter.hasNext())
                					{
                						ArrayList refName = (ArrayList)refIter.next();
                						String localKey = (String)refName.get(0);
                						GenericWrapperField foreignKey = (GenericWrapperField)refName.get(1);
                						Object value = meta.getProperty(foreignKey.getId());
                						if (value != null)
                						{
    										if (!item.setFieldValue(localKey,value)){
   											 throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
   										 }
                						}
                					}
                				}else{
    								meta.setDirectProperty("row_last_modified",cache.getModTime());
    								meta.setDirectProperty("last_modified",cache.getModTime());
    							}
                			}
    
                			if (hasOneColumnTable)
                			{
                				StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                			}
                			
                			item = InsertItem(item,login,cache,false);
                		}
                	}else{
            		    //ITEM IS NEW
                			if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
                			{
                				GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
                				XFTItem meta=(XFTItem)item.getProperty(f);
    							if(meta == null)
    							{
    								meta = XFTItem.NewMetaDataElement(user,item.getXSIType(),localQuarantine,cache.getModTime(),cache.getChangeId());
                					StoreItem(meta,user,true,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                					
                					GenericWrapperField ref = item.getGenericSchemaElement().getField("meta");
                					
                					item.setChild(ref,meta,true);
                					
                					Iterator refIter = ref.getLocalRefNames().iterator();
                					while (refIter.hasNext())
                					{
                						ArrayList refName = (ArrayList)refIter.next();
                						String localKey = (String)refName.get(0);
                						GenericWrapperField foreignKey = (GenericWrapperField)refName.get(1);
                						Object value = meta.getProperty(foreignKey.getId());
                						if (value != null)
                						{
    										if (!item.setFieldValue(localKey,value)){
   											 throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
   										 }
                						}
                					}
                				}else{
    								meta.setDirectProperty("row_last_modified",cache.getModTime());
    								meta.setDirectProperty("last_modified",cache.getModTime());
    							}
                			}
    
                			if (hasOneColumnTable)
                			{
                				StoreSingleRefs(item,true,user,localQuarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,(!isNew));
                			}
                			
                			item = InsertItem(item,login,cache,false);
                	}
                }
                
                if(!itemAlreadyStoredInCache) {
                    StoreMultipleRefs(item,user,localQuarantine,overrideQuarantine, allowItemOverwrite,cache,securityManager);
                }
                
               //StoreDuplicateRelationships(item,user,storedRelationships,localQuarantine,overrideQuarantine);
            } catch (ElementNotFoundException e) {
                logger.error("Error saving item: \n"+ item.toString());
                throw e;
            } catch (XFTInitException e) {
                logger.error("Error saving item: \n"+ item.toString());
                throw e;
            } catch (FieldNotFoundException e) {
                logger.error("Error saving item: \n"+ item.toString());
                throw e;
            } catch (DBPoolException e) {
                logger.error("Error saving item: \n"+ item.toString());
                throw e;
            } catch (SQLException e) {
                logger.error("Error saving item: \n"+ item.toString());
                throw e;
            } catch (Exception e) {
                logger.error("Error saving item: \n"+ item.toString());
                throw e;
            }

            
            if(item.modified && !isNew){
	            if(item.getGenericSchemaElement().isExtension()){
	            	//add extensions to the history (if they weren't already)
	            	confirmExtensionHistory(item,cache,user);
	            	
	            	//confirm that item has history has been modified
	            	if(!cache.getModified().contains(item,false)){
    	        		StoreHistoryAndMeta(item,user,null,cache);
            			cache.getModified().add(item);
            		}
	            }
            }
            
            if ((item.modified || item.child_modified) && item.getGenericSchemaElement().canBeRoot()){
            	if (!cache.getDBTriggers().contains(item, false)){
                    cache.getDBTriggers().add(item);
                }
            }
            
    		return item;
    	}
    	
    	/**
    	 * if this item is an extension of another item, and this item was modified, then add history rows for the extended item.
    	 * @param i
    	 * @param cache
    	 * @param user
    	 * @throws ElementNotFoundException
    	 * @throws XFTInitException
    	 * @throws FieldNotFoundException
    	 * @throws Exception
    	 */
    	private static void confirmExtensionHistory(XFTItem i,DBItemCache cache, UserI user) throws ElementNotFoundException, XFTInitException, FieldNotFoundException, Exception{
            if(i.getGenericSchemaElement().isExtension()){
	    		final XFTItem extension=i.getExtensionItem();
	
	        	//add history rows for this if extended row modified
	        	if(!cache.getModified().contains(extension,false)){
	        		//extended item was not modified... but this one was.
	            	//add history rows for extended items.
	        		StoreHistoryAndMeta(extension,user,null,cache);
	    			cache.getModified().add(extension);
	        	}
	        	
	        	confirmExtensionHistory(extension, cache, user);
            }
    	}
	
	/**
	 * @param oldI
	 * @param newI
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	private static boolean HasNewFields(XFTItem oldI, XFTItem newI,boolean allowItemOverwrite) throws XFTInitException,ElementNotFoundException,InvalidValueException
	{

	    boolean b = false;
		
		Hashtable newHash = newI.getProps();
		Hashtable oldHashClone = (Hashtable)oldI.getProps().clone();
		Enumeration enumer = newHash.keys();
		while (enumer.hasMoreElements())
		{
			String field = (String)enumer.nextElement();
			GenericWrapperField gwf= oldI.getGenericSchemaElement().getField(field);
			Object newObject = newHash.get(field);
			if (!(newObject instanceof XFTItem))
			{
				try {
                    if (oldI.getProperty(field)!= null)
                    {
                        oldHashClone.remove(field);
						String oldValue = DBAction.ValueParser(oldI.getProperty(field),gwf,true);
						String newValue = DBAction.ValueParser(newHash.get(field),gwf,false);
						String type = null;
						if (gwf !=null)
						{
						    type = gwf.getXMLType().getLocalType();
						}

						if (IsNewValue(type, oldValue, newValue)){
						    return true;
						}
                    }else{
                        if (newObject.toString().equals("NULL") || newObject.toString()=="")
                        {
                            
                        }else{
                            logger.info("OLD:NULL NEW:" + newObject);
                        	return true;
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.error("",e);
                    logger.info("OLD:NULL NEW:" + newObject);
                	return true;
                } catch (XFTInitException e) {
                    logger.error("",e);
                    logger.info("OLD:NULL NEW:" + newObject);
                	return true;
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                    logger.info("OLD:NULL NEW:" + newObject);
                	return true;
                } catch (FieldNotFoundException e) {
                    logger.error("",e);
                    logger.info("OLD:NULL NEW:" + newObject);
                	return true;
                }
			}
			
		}
		
		if (allowItemOverwrite)
		{
			enumer = oldHashClone.keys();
			while (enumer.hasMoreElements())
			{
				String field = (String)enumer.nextElement();
				GenericWrapperField gwf= oldI.getGenericSchemaElement().getField(field);
				if (gwf==null)
				{
				    if (!oldI.getGenericSchemaElement().isHiddenFK(field))
				    {
					    Object newObject = oldHashClone.get(field);
						if (!(newObject instanceof XFTItem))
						{
						    if (!newObject.toString().trim().equals("") || newObject.toString()=="")
							{
							    if (! oldI.getPkNames().contains(field))
								{
				                    logger.info("NEW:NULL OLD:" + newObject);
				                	return true;
								}else{
								    newI.getProps().put(field,newObject);
								}
							}
						}
				    }
				}else if (gwf.isReference())
				{
				    if (!oldI.getGenericSchemaElement().isHiddenFK(field))
				    {
					    
					    GenericWrapperElement e = (GenericWrapperElement)gwf.getReferenceElement();
	
						Object newObject = oldHashClone.get(field);
						if (e.getAddin().equals(""))
						{
							if (!(newObject instanceof XFTItem))
							{
							    if (!newObject.toString().trim().equals("") || newObject.toString()=="")
								{
								    if (! oldI.getPkNames().contains(field))
									{
					                    logger.info("NEW:NULL OLD:" + newObject);
					                	return true;
									}else{
									    newI.getProps().put(field,newObject);
									}
								}
							}
						}else{
						    newI.getProps().put(field,newObject);
						}
				    }
				}else{
				    if (!oldI.getGenericSchemaElement().isHiddenFK(field))
				    {
					    Object newObject = oldHashClone.get(field);
					    String type =gwf.getXMLType().getLocalType();
					    if (type != null)
					    {
						    newObject = XMLWriter.ValueParser(newObject,type);
					    }
					    if (!(newObject instanceof XFTItem))
					    {
							if (!newObject.toString().trim().equals("") || newObject.toString()=="")
							{
							    if (! oldI.getPkNames().contains(field))
								{
				                    logger.info("NEW:NULL OLD:" + newObject);
				                	return true;
								}else{
								    newI.getProps().put(field,newObject);
								}
							}
					    }
				    }
					
				}
			}
		}
		return b;
	}
	
    public static boolean IsNewValue(String type,String oldValue,String newValue){
        if (type == null)
        {
            //REMOVE STRING FORMATTING
            if (oldValue.startsWith("'") && oldValue.endsWith("'"))
            {
                oldValue = oldValue.substring(1,oldValue.lastIndexOf("'"));
            }
            if (newValue.startsWith("'") && newValue.endsWith("'"))
            {
                newValue = newValue.substring(1,newValue.lastIndexOf("'"));
            }
            
            if (! oldValue.equals(newValue))
            {
                logger.info("OLD:" + oldValue + " NEW:" + newValue);
                return true;
            }
        }else{
            if (type.equalsIgnoreCase(""))
            {
                if (! oldValue.equals(newValue))
                {
                    logger.info("OLD:" + oldValue + " NEW:" + newValue);
                    return true;
                }
            }else
            {
                if (type.equalsIgnoreCase("integer"))
                {
                    Integer o1 = Integer.valueOf(oldValue);
                    Integer o2 = Integer.valueOf(newValue);
                    
                    if (! o1.equals(o2))
                    {
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
                        return true;
                    }
                }else if (type.equalsIgnoreCase("boolean"))
                {
                    Boolean o1 = null;
                    Boolean o2 = null;
                    
                    if (oldValue.equalsIgnoreCase("true") || oldValue.equalsIgnoreCase("1"))
                    {
                        o1=Boolean.TRUE;
                    }else
                    {
                        o1=Boolean.FALSE;
                    }
                    
                    if (newValue.equalsIgnoreCase("true") || newValue.equalsIgnoreCase("1"))
                    {
                        o2=Boolean.TRUE;
                    }else
                    {
                        o2=Boolean.FALSE;
                    }
                    
                    if (! o1.equals(o2))
                    {
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
                        return true;
                    }
                }else if (type.equalsIgnoreCase("float"))
                {
                    if (oldValue.equalsIgnoreCase("NaN")){
                        oldValue="'NaN'";
                    }
                    if (newValue.equalsIgnoreCase("NaN")){
                        newValue="'NaN'";
                    } 
                    if (oldValue.equalsIgnoreCase("INF")){
                        oldValue="'Infinity'";
                    }
                    if (oldValue.equalsIgnoreCase("-INF")){
                        oldValue="'-Infinity'";
                    }
                    if (newValue.equalsIgnoreCase("-INF")){
                        newValue="'-Infinity'";
                    }
                    if (oldValue.equals("'NaN'") || newValue.equals("'NaN'")){
                        if (!oldValue.equals(newValue)){
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }else if (oldValue.equals("'INF'") || newValue.equals("'INF'")){
                        if (!oldValue.equals(newValue)){
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }else{
                        Float o1 = Float.valueOf(oldValue);
                        Float o2 = Float.valueOf(newValue);
                        
                        if (! o1.equals(o2))
                        {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }
                }else if (type.equalsIgnoreCase("double"))
                {
                    if (oldValue.equalsIgnoreCase("NaN")){
                        oldValue="'NaN'";
                    }
                    if (newValue.equalsIgnoreCase("NaN")){
                        newValue="'NaN'";
                    }
                    if (oldValue.equalsIgnoreCase("INF")){
                        oldValue="'Infinity'";
                    }
                    if (newValue.equalsIgnoreCase("INF")){
                    	newValue="'Infinity'";
                    }
                    if (oldValue.equalsIgnoreCase("-INF")){
                        oldValue="'-Infinity'";
                    }
                    if (newValue.equalsIgnoreCase("-INF")){
                        newValue="'-Infinity'";
                    }
                    if (oldValue.equals("'NaN'") || newValue.equals("'NaN'")){
                        if (!oldValue.equals(newValue)){
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }else if (oldValue.contains("Infinity") || newValue.contains("Infinity")){
                        if (!oldValue.equals(newValue)){
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }else{
                        Double o1 = Double.valueOf(oldValue);
                        Double o2 = Double.valueOf(newValue);
                        
                        if (! o1.equals(o2))
                        {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }
                }else if (type.equalsIgnoreCase("decimal"))
                {
                    if (oldValue.equalsIgnoreCase("NaN")){
                        oldValue="'NaN'";
                    }
                    if (newValue.equalsIgnoreCase("NaN")){
                        newValue="'NaN'";
                    }
                    if (oldValue.equalsIgnoreCase("INF")){
                        oldValue="'Infinity'";
                    }
                    if (oldValue.equalsIgnoreCase("-INF")){
                        oldValue="'-Infinity'";
                    }
                    if (newValue.equalsIgnoreCase("-INF")){
                        newValue="'-Infinity'";
                    }
                    if (oldValue.equals("'NaN'") || newValue.equals("'NaN'")){
                        if (!oldValue.equals(newValue)){
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }else if (oldValue.equals("'INF'") || newValue.equals("'INF'")){
                        if (!oldValue.equals(newValue)){
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }else{
                        Float o1 = Float.valueOf(oldValue);
                        Float o2 = Float.valueOf(newValue);
                        
                        if (! o1.equals(o2))
                        {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    }
                }else if (type.equalsIgnoreCase("date"))
                {
                    try {
                        Date o1= DateUtils.parseDate(oldValue);
                        Date o2= DateUtils.parseDate(newValue);
                        
                        if (! o1.equals(o2))
                        {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } catch (ParseException e) {
                        logger.error("",e);
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
                        return true;
                    }
                }else if (type.equalsIgnoreCase("dateTime"))
                {
                    try {
                        Date o1= DateUtils.parseDateTime(oldValue);
                        Date o2= DateUtils.parseDateTime(newValue);
                        
                        if (! o1.equals(o2))
                        {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } catch (ParseException e) {
                        logger.error("",e);
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
                        return true;
                    }
                }else if (type.equalsIgnoreCase("time"))
                {
                    try {
                        Date o1= DateUtils.parseTime(oldValue);
                        Date o2= DateUtils.parseTime(newValue);
                        
                        if (! o1.equals(o2))
                        {
                            logger.info("OLD:" + oldValue + " NEW:" + newValue);
                            return true;
                        }
                    } catch (ParseException e) {
                        logger.error("",e);
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
                        return true;
                    }
                }else {
                    if (! oldValue.equals(newValue))
                    {
                        logger.info("OLD:" + oldValue + " NEW:" + newValue);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
	private static XFTItem ImportNoIdentifierFKs(XFTItem item, XFTItem dbVersion)
	{
		try {
            Iterator refs = item.getGenericSchemaElement().getReferenceFields(false).iterator();
            while (refs.hasNext())
            {
            	GenericWrapperField ref = (GenericWrapperField)refs.next();
            	try {
            	    boolean isNoIdentifierTable = false;
            	    try {
                        Object o = item.getProperty(ref.getId());
                        
                        if (o != null && o instanceof XFTItem)
                        {
                        	XFTItem temp = (XFTItem)o;
                        	
                        	//check for one column table (if found, see if root item is already stored.  If, the item already was stored and has
                        	// a fk value for this table that should be used. Else, this ref item will be stored and the root item will be updated with the fk value
                        	String keyName = "";
                        	if (temp != null)
                        	{				
                        		if (temp.getPossibleFieldNames().size() == 1)
                        		{
                        			keyName =(String)((Object[])temp.getPossibleFieldNames().get(0))[0];
                        			try {
                                        if (temp.getProperty(keyName) == null)
                                        {
                                            isNoIdentifierTable =true;
                                        }
                                    } catch (FieldNotFoundException e1) {
                                        logger.error("",e1);
                                    }
                        		}
                        		
                        		if (! temp.getGenericSchemaElement().hasUniqueIdentifiers())
                        		{
                        		    isNoIdentifierTable =true;
                        		}
                        	}
                        }
                        
                        if (isNoIdentifierTable)
                        {
                            try {
                                XFTSuperiorReference supRef = (XFTSuperiorReference)ref.getXFTReference();
                                //Set foreign keys based on saved values.
                                Iterator iterator = supRef.getKeyRelations().iterator();
                                  while (iterator.hasNext())
                                  {
                                      XFTRelationSpecification spec = (XFTRelationSpecification)iterator.next();
                                      String fieldName = spec.getLocalCol();
                                	  try {
                                        Object value = dbVersion.getProperty(fieldName);
                                          if (value != null)
                                          {
                                        	  try {
        										if (!item.setFieldValue(fieldName.toLowerCase(),value)){
        											 throw new FieldNotFoundException(item.getXSIType() + "/" + fieldName.toLowerCase());
        										 }
                                            } catch (ElementNotFoundException e) {
                                                logger.error("",e);
                                            } catch (XFTInitException e) {
                                                logger.error("",e);
                                            }
                                          }
                                    } catch (XFTInitException e) {
                                        logger.error("",e);
                                    } catch (ElementNotFoundException e) {
                                        logger.error("",e);
                                    } catch (FieldNotFoundException e) {
                                        logger.error("",e);
                                    }
                                  }
                            } catch (XFTInitException e) {
                                logger.error("",e);
                            } catch (ElementNotFoundException e) {
                                logger.error("",e);
                            }
                        }
                    } catch (FieldNotFoundException e1) {
                        logger.error("",e1);
                    }
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                }
            }
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
        return item;
	}
	
		private static boolean StoreSingleRefs(XFTItem item,boolean storeSubItems, UserI user, boolean quarantine, boolean overrideQuarantine, boolean allowItemOverwrite,DBItemCache cache,SecurityManagerI securityManager,boolean allowFieldMatching) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
    	{
    		boolean hasNoIdentifier = false;
    		//save single refs
    		GenericWrapperField ext = null;
    		Iterator refs = item.getGenericSchemaElement().getReferenceFields(false).iterator();
    		while (refs.hasNext())
    		{
    			GenericWrapperField ref = (GenericWrapperField)refs.next();
    			if (!item.getGenericSchemaElement().getExtensionFieldName().equalsIgnoreCase(ref.getName()))
    			{
        			Object o = item.getProperty(ref.getId());
        			
        			if (o != null && o instanceof XFTItem)
        			{
        				XFTItem temp = (XFTItem)o;
        				
        				//check for one column table (if found, see if root item is already stored.  If, the item already was stored and has
        				// a fk value for this table that should be used. Else, this ref item will be stored and the root item will be updated with the fk value
        				boolean isNoIdentifierTable = false;
        				String keyName = "";
        				if (temp != null)
        				{				
        					if (temp.getPossibleFieldNames().size() == 1)
        					{
        						keyName =(String)((Object[])temp.getPossibleFieldNames().get(0))[0];
        						if (temp.getProperty(keyName) == null)
        						{
        						    isNoIdentifierTable =true;
        							hasNoIdentifier = true;
        						}
        					}
        					
        					if (! temp.getGenericSchemaElement().hasUniqueIdentifiers())
        					{
        					    isNoIdentifierTable =true;
        					    hasNoIdentifier = true;
        					}
        		
        					if ((! isNoIdentifierTable) && (! storeSubItems))
        					{
        						//Store this item.
        					    if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName()))
        					    {
        					        StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,true);
        					    }else{
        					        if (securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName()))
        					        {
                						StoreItem(temp,user,false,quarantine,overrideQuarantine,false, cache,securityManager,true);
        					        }
        					    }
        						
        						XFTSuperiorReference supRef = (XFTSuperiorReference)ref.getXFTReference();
        						//Set foreign keys based on saved values.
        						Iterator iterator = supRef.getKeyRelations().iterator();
        						  while (iterator.hasNext())
        						  {
        						      XFTRelationSpecification spec = (XFTRelationSpecification)iterator.next();
        						      String fieldName = spec.getLocalCol();
        							  Object value = temp.getProperty(spec.getForeignCol());
        							  if (value != null)
        							  {
  										if (!item.setFieldValue(fieldName.toLowerCase(),value)){
											 throw new FieldNotFoundException(item.getXSIType() + "/" + fieldName.toLowerCase());
										 }
        							  }
        						  }
                                  
                                  if (temp.modified || temp.child_modified){
                                      item.child_modified=true;
                                  }
        					}else if ((isNoIdentifierTable) && (storeSubItems))
        					{
        						ArrayList refName = (ArrayList)ref.getLocalRefNames().get(0);
        						String localKey = (String)refName.get(0);
        						GenericWrapperField foreignKey = (GenericWrapperField)refName.get(1);
        						Object rootFKValue = item.getProperty(localKey);
        
        						if (rootFKValue != null)
        						{
        							if (keyName.equals(""))
        							{
        							    keyName = foreignKey.getId();
        								temp.setFieldValue(keyName,rootFKValue);
        								if (temp.getGenericSchemaElement().isExtension())
        								{
        								    temp.extendPK();
        								}
        								if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName()))
        	    					    {
        	        						StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,true);
        	    					    }else{
        	        						StoreItem(temp,user,false,quarantine,overrideQuarantine,false, cache,securityManager,true);
        	    					    }
        							}else{
        								temp.setFieldValue(keyName,rootFKValue);
        								if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName()))
        	    					    {
        	        						StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,true);
        	    					    }else{
        	        						StoreItem(temp,user,false,quarantine,overrideQuarantine,false, cache,securityManager,true);
        	    					    }
        							}
        						}else{
        //							 Store this item.
        						    if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName()))
            					    {
                						StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,allowFieldMatching);
            					    }else{
                						StoreItem(temp,user,false,quarantine,overrideQuarantine,false, cache,securityManager,true);
            					    }
        	
        							 //Set foreign keys based on saved values.
        							 ref.getLocalRefNames().iterator();
        							 Object value = temp.getProperty(foreignKey.getId());
        							 if (value != null)
        							 {
										if (!item.setFieldValue(localKey,value)){
											 throw new FieldNotFoundException(item.getXSIType() + "/" + localKey.toLowerCase());
										 }
        							 }
        						 
        						}
        						if (temp.modified || temp.child_modified){
                                    item.child_modified=true;
                                    item.modified=true;
                                }
        					}
        				}
        			}
    			}else{
    			    ext = ref;
    			}
    		}
    		
    		//STORE EXTENSION AFTER OTHER REFERENCES
    		if (ext != null)
    		{
    		    GenericWrapperField ref = ext;
    		    Object o = item.getProperty(ref.getId());
    			
    			if (o != null && o instanceof XFTItem)
    			{
    				XFTItem temp = (XFTItem)o;
    				
    				//check for one column table (if found, see if root item is already stored.  If, the item already was stored and has
    				// a fk value for this table that should be used. Else, this ref item will be stored and the root item will be updated with the fk value
    				boolean isNoIdentifierTable = false;
    				String keyName = "";
    				if (temp != null)
    				{				
    					if (temp.getPossibleFieldNames().size() == 1)
    					{
    						keyName =(String)((Object[])temp.getPossibleFieldNames().get(0))[0];
    						if (temp.getProperty(keyName) == null)
    						{
    						    isNoIdentifierTable =true;
    							hasNoIdentifier = true;
    						}
    					}
    					
    					if (! temp.getGenericSchemaElement().hasUniqueIdentifiers() && !temp.getGenericSchemaElement().matchByValues())
    					{
    					    isNoIdentifierTable =true;
    					    hasNoIdentifier = true;
    					}
    		
    					if ((! isNoIdentifierTable) && (! storeSubItems))
    					{
    						//Store this item.
    					    if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName()))
    					    {
        						StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,allowFieldMatching);
    					    }else{
    					        //
        						//StoreItem(temp,user,false,quarantine,overrideQuarantine,false, cache,securityManager,allowFieldMatching);
    					    }
    						
    						XFTSuperiorReference supRef = (XFTSuperiorReference)ref.getXFTReference();
    						//Set foreign keys based on saved values.
    						Iterator iterator = supRef.getKeyRelations().iterator();
    						  while (iterator.hasNext())
    						  {
    						      XFTRelationSpecification spec = (XFTRelationSpecification)iterator.next();
    						      String fieldName = spec.getLocalCol();
    							  Object value = temp.getProperty(spec.getForeignCol());
    							  if (value != null)
    							  {
										if (!item.setFieldValue(fieldName.toLowerCase(),value)){
											 throw new FieldNotFoundException(item.getXSIType() + "/" + fieldName.toLowerCase());
										 }
    							  }
    						  }
                              
                              if (temp.modified){
                                  item.modified=true;
                              }
    					}else if ((isNoIdentifierTable) && (storeSubItems))
    					{
    						ArrayList refName = (ArrayList)ref.getLocalRefNames().get(0);
    						String localKey = (String)refName.get(0);
    						GenericWrapperField foreignKey = (GenericWrapperField)refName.get(1);
    						Object rootFKValue = item.getProperty(localKey);
    
    						if (rootFKValue != null)
    						{
    							if (keyName.equals(""))
    							{
    							    keyName = foreignKey.getId();
    								temp.setFieldValue(keyName,rootFKValue);
    								if (temp.getGenericSchemaElement().isExtension())
    								{
    								    temp.extendPK();
    								}
    								if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName()))
    	    					    {
    	        						StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,allowFieldMatching);
    	    					    }else{
    	        						StoreItem(temp,user,false,quarantine,overrideQuarantine,false, cache,securityManager,allowFieldMatching);
    	    					    }
    							}else{
    								temp.setFieldValue(keyName,rootFKValue);
    								if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName()))
    	    					    {
    	        						StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,allowFieldMatching);
    	    					    }else{
    	        						StoreItem(temp,user,false,quarantine,overrideQuarantine,false, cache,securityManager,allowFieldMatching);
    	    					    }
    							}
    						}else{
    //							 Store this item.
    						    if (securityManager == null || !securityManager.isSecurityElement(temp.getGenericSchemaElement().getFullXMLName()))
        					    {
            						StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemOverwrite, cache,securityManager,allowFieldMatching);
        					    }else{
            						StoreItem(temp,user,false,quarantine,overrideQuarantine,false, cache,securityManager,allowFieldMatching);
        					    }
    	
    							 //Set foreign keys based on saved values.
    							 ref.getLocalRefNames().iterator();
    							 Object value = temp.getProperty(foreignKey.getId());
    							 if (value != null)
    							 {
									if (!item.setFieldValue(localKey,value)){
										 throw new FieldNotFoundException(item.getXSIType() + "/" + localKey);
									 }
    							 }
    						 
    						}
                            
                            if (temp.modified){
                                item.modified=true;
                            }
    					}
    				}
    			}
    		}
    		return hasNoIdentifier;
    	}
	
	private static ItemI StoreMultipleRefs(XFTItem item, UserI user, boolean quarantine, boolean overrideQuarantine, boolean allowItemRemoval,DBItemCache cache,SecurityManagerI securityManager) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
	{
//		save multiple refs
		  Iterator mRefs = item.getGenericSchemaElement().getMultiReferenceFields().iterator();

			String login = null;
			if (user != null)
			{
			    login = user.getUsername();
			}
	      XFTItem dbVersion = null;
		  while (mRefs.hasNext())
		  {
			  try {
				  GenericWrapperField ref = (GenericWrapperField)mRefs.next();
			
				  XFTReferenceI xftRef = ref.getXFTReference();
				  if (xftRef.isManyToMany())
				  {
				      GenericWrapperElement foreignElement = (GenericWrapperElement)ref.getReferenceElement();

					  XFTManyToManyReference many = (XFTManyToManyReference)xftRef;
					  
				      if (!foreignElement.hasUniqueIdentifiers() && (!foreignElement.matchByValues()))
					  {
				          //if allowItemRemoval then removes pre-existing non-identified rows
					      if (allowItemRemoval)
					      {
						      if (dbVersion==null){
						          dbVersion=item.getCurrentDBVersion(false,false);
						      }

						      if (dbVersion != null)
						      {
							      ItemCollection items = dbVersion.getChildItemCollection(ref);
							      
							      Iterator itemsToRemove = items.iterator();
							      while (itemsToRemove.hasNext())
							      {
							          XFTItem itemToRemove = (XFTItem)itemsToRemove.next();
							          
							          boolean found = false;
							          Iterator children = item.getChildItems(ref).iterator();
									  while (children.hasNext())
									  {
										  XFTItem temp = (XFTItem)children.next();
										  if (temp.hasPK())
										  {
                                              if(XFTItem.CompareItemsByPKs(temp,itemToRemove)){
                                                  found = true;
                                                  break;
                                              }
										  }else{
                                              if (temp.hasUniques(true)){
                                                  if(XFTItem.CompareItemsByUniques(temp,itemToRemove,true)){
                                                      found = true;
                                                      break;
                                                  }
                                              }
                                          }
                                          
									  }
							          
									  if(!found){
                                          
									      DBAction.RemoveItemReference(dbVersion,ref.getXMLPathString(item.getXSIType()),itemToRemove,user,cache,false,false);
                                          
                                          item.child_modified=true;
									  }
							      }
						      }
					      }
					  }
				      
					  int counter = 0;
					  Iterator children = item.getChildItems(ref).iterator();
					  while (children.hasNext())
					  {
						  XFTItem temp = (XFTItem)children.next();
						  temp =StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemRemoval, cache,securityManager,true);
						  item.setFieldValue(ref.getSQLName().toLowerCase()+ (counter),temp);
						  counter = counter+1;

                          
						  if (temp.modified || temp.child_modified){
                              item.child_modified=true;
                          }
					  }
				
					  counter = 0;
					 children = item.getChildItems(ref).iterator();
					 while (children.hasNext())
					 {
						 XFTItem temp = (XFTItem)children.next();
					
						  CriteriaCollection search = new CriteriaCollection("AND");
						  //SET MAPPING VALUES
						  Iterator iter = many.getMappingColumns().iterator();
						  while (iter.hasNext())
						  {
							  XFTMappingColumn col = (XFTMappingColumn)iter.next();
							  if (col.getForeignElement().getFormattedName().equalsIgnoreCase(item.getGenericSchemaElement().getFormattedName()))
							  {
								  //PRIMARY ITEM
								  if (item.getProperty(col.getForeignKey().getId()) !=null)
								  {
									  SearchCriteria c = new SearchCriteria();
									  c.setField_name(col.getLocalSqlName());
									  c.setValue(item.getProperty(col.getForeignKey().getId()));
									  c.setCleanedType(col.getXmlType().getLocalType());
								
									  search.add(c);
								  }
							  }else{
								  //TEMP ITEM
								  if (temp.getProperty(col.getForeignKey().getId()) !=null)
								  {
									  SearchCriteria c = new SearchCriteria();
									  c.setField_name(col.getLocalSqlName());
									  c.setValue(temp.getProperty(col.getForeignKey().getId()));
									  c.setCleanedType(col.getXmlType().getLocalType());
								
									  search.add(c);
								  }
							  }
						  }
					
						  if (StoreMapping(many,search,login,cache)){
						      item.child_modified=true;
						  }
					  }
					 
					 if (foreignElement.hasUniqueIdentifiers() && (!foreignElement.matchByValues()))
					  {
				          //if allowItemRemoval then removes pre-existing non-identified rows
					      if (allowItemRemoval)
					      {
						      if (dbVersion==null){
						          dbVersion=item.getCurrentDBVersion(false,false);
						      }

						      if (dbVersion != null)
						      {
							      ItemCollection items = dbVersion.getChildItemCollection(ref);
							      
							      Iterator itemsToRemove = items.iterator();
							      while (itemsToRemove.hasNext())
							      {
							          XFTItem itemToRemove = (XFTItem)itemsToRemove.next();
							          
							          boolean found = false;
							          children = item.getChildItems(ref).iterator();
									  while (children.hasNext())
									  {
										  XFTItem temp = (XFTItem)children.next();
                                          if (temp.hasPK())
                                          {
                                              if(XFTItem.CompareItemsByPKs(temp,itemToRemove)){
                                                  found = true;
                                                  break;
                                              }
                                          }else{
                                              if (temp.hasUniques(true)){
                                                  if(XFTItem.CompareItemsByUniques(temp,itemToRemove,true)){
                                                      found = true;
                                                      break;
                                                  }
                                              }
                                          }
									  }
							          
									  if(!found){
									      DBAction.RemoveItemReference(dbVersion,ref.getXMLPathString(item.getXSIType()),itemToRemove,user,cache,false,false);
                                          item.child_modified=true;
									  }
							      }
						      }
					      }
					  }
				
				  }else{
					  GenericWrapperElement foreignElement = (GenericWrapperElement)ref.getReferenceElement();

					  foreignElement.getField(item.getGenericSchemaElement().getFullXMLName());
					  if (!foreignElement.hasUniqueIdentifiers() && (!foreignElement.matchByValues()))
					  {
					      if (allowItemRemoval)
					      {
						      if (dbVersion==null){
						          dbVersion=item.getCurrentDBVersion(false,false);
						      }
						      if (dbVersion != null)
						      {
							      ItemCollection items = dbVersion.getChildItemCollection(ref);
							      
							      Iterator itemsToRemove = items.iterator();
							      while (itemsToRemove.hasNext())
							      {
							          XFTItem itemToRemove = (XFTItem)itemsToRemove.next();
							          
							          boolean found = false;
							          Iterator children = item.getChildItems(ref).iterator();
									  while (children.hasNext())
									  {
										  XFTItem temp = (XFTItem)children.next();
                                          if (temp.hasPK())
                                          {
                                              if(XFTItem.CompareItemsByPKs(temp,itemToRemove)){
                                                  found = true;
                                                  break;
                                              }
                                          }else{
                                              if (temp.hasUniques(true)){
                                                  if(XFTItem.CompareItemsByUniques(temp,itemToRemove,true)){
                                                      found = true;
                                                      break;
                                                  }
                                              }
                                          }
									  }
							          
									  if(!found){
                                          
									      DBAction.RemoveItemReference(dbVersion,ref.getXMLPathString(item.getXSIType()),itemToRemove,user,cache,false,false);
                                          
                                          item.child_modified=true;
                                      }
							      }
						      }
					      }
					  }
					  
					  XFTSuperiorReference supRef = (XFTSuperiorReference)xftRef;
					  
					  Iterator children = item.getChildItems(ref).iterator();
					  while (children.hasNext())
					  {
						  XFTItem temp = (XFTItem)children.next();
	
						  Iterator iterator = supRef.getKeyRelations().iterator();
						  while (iterator.hasNext())
						  {
						      XFTRelationSpecification spec = (XFTRelationSpecification)iterator.next();
						      String fieldName = spec.getLocalCol();
							  Object value = item.getProperty(spec.getForeignCol());
							  if (value != null)
							  {
								 boolean set= temp.setFieldValue(fieldName.toLowerCase(),value);
								 if (!set){
									 throw new FieldNotFoundException(temp.getXSIType() + "/" + fieldName.toLowerCase());
								 }
							  }
						  }
				
						  if (temp != null)
						  {
							  temp = StoreItem(temp,user,false,quarantine,overrideQuarantine,allowItemRemoval, cache,securityManager,true);

                              
							  if (temp.modified || temp.child_modified){
	                              item.child_modified=true;
	                          }
							  
						  }			
					  }
					  

					  if (allowItemRemoval)
				      {
					      if (dbVersion==null){
					          dbVersion=item.getCurrentDBVersion(false,false);
					      }

					      if (dbVersion !=null)
					      {
						      ItemCollection items = dbVersion.getChildItemCollection(ref);
						      
						      Iterator dbItems = items.iterator();
						      while (dbItems.hasNext())
						      {
						          XFTItem itemToRemove = (XFTItem)dbItems.next();
						          
						          boolean found = false;
						          children = item.getChildItems(ref).iterator();
								  while (children.hasNext())
								  {
									  XFTItem temp = (XFTItem)children.next();
                                      if (temp.hasPK())
                                      {
                                          if(XFTItem.CompareItemsByPKs(temp,itemToRemove)){
                                              found = true;
                                              break;
                                          }
                                      }else{
                                          if (temp.hasUniques(true)){
                                              if(XFTItem.CompareItemsByUniques(temp,itemToRemove,true)){
                                                  found = true;
                                                  break;
                                              }
                                          }
                                      }
								  }
						          
								  if(!found)
								  {
								      DBAction.RemoveItemReference(dbVersion,ref.getXMLPathString(item.getXSIType()),itemToRemove,user,cache,false,false);
                                      item.child_modified=true;
								  }
						      }
					      }
				      }
				  }
			
			  } catch (Exception e) {
				  throw e;
			  }
		  }
		  
		  return item;
	}
	
	private static boolean StoreMapping(XFTManyToManyReference mapping,CriteriaCollection criteria,String login,DBItemCache cache) throws DBPoolException,java.sql.SQLException,ElementNotFoundException,XFTInitException,Exception
	{
		
		XFTTable table = TableSearch.GetMappingTable(mapping,criteria,login);
		
		if (table.getNumRows() > 0)
		{
			logger.info("Duplicate mapping table row found in '" + mapping.getMappingTable() + "'");
            return false;
		}else
		{
			String query = "INSERT INTO ";

			query += mapping.getMappingTable() + " (";

			String fields ="";
			String values ="";

			Iterator props= criteria.iterator();
			int counter = 0;
			while (props.hasNext())
			{
				SearchCriteria c = (SearchCriteria)props.next();
				if (c.getValue() != null)
				{
					if (counter++ == 0)
					{
						fields = c.getField_name();
						values = c.valueToDB();
					}else
					{
						fields += "," + c.getField_name();
						values += "," + c.valueToDB();
					}
				}
			}
			query += fields + ") VALUES ("+ values + ");";

			PoolDBUtils con = null;
			try {
				con = new PoolDBUtils();
				con.insertItem(query,mapping.getElement1().getDbName(),login,cache);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
            return true;
		}
	}
	
	public static XFTItem CheckMetaData(XFTItem item,UserI user,boolean quarantine)
	{
		try {
			GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(item.getXSIType() + "/meta");
			if(item.getProperty(f) == null)
			{
				XFTItem meta = XFTItem.NewMetaDataElement(user,item.getXSIType(),quarantine,Calendar.getInstance().getTime(),null);
				
				item.setChild(f,meta,true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return item;
	}
	
	/**
	 * @param item
	 * @return
	 */
	public static XFTItem InsertItem(XFTItem item,String login,DBItemCache cache,boolean allowInvalidValues) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
	{
        item.modified=true;
		item.assignDefaultValues();
		String query = "INSERT INTO ";
		GenericWrapperElement element = item.getGenericSchemaElement();

		PoolDBUtils con = null;
		con = new PoolDBUtils();
		if (element.isAutoIncrement())
		{
			if (! item.hasPK())
			{
				Object key= con.getNextID(element.getDbName(),element.getSQLName(),(String)item.getPkNames().get(0),element.getSequenceName());
				if (key != null)
				{
					item.setFieldValue((String)item.getPkNames().get(0),key);
				}
			}
		}
		
		query += element.getSQLName() + " (";
		
		String fields ="";
		String values ="";
		
		Hashtable props = item.getProps();
		Enumeration enumer = props.keys();
		int counter = 0;
		while (enumer.hasMoreElements())
		{
			String key = (String)enumer.nextElement();
			Object value = props.get(key);
			if (!( value instanceof XFTItem))
			{
				GenericWrapperField field = element.getNonMultipleDataField(key);
				if (field==null)
				{
					if (element.validateID(key))
					{
						if (counter++ == 0)
						{
							fields = key;
							values = ValueParser(value,field,allowInvalidValues);
						}else
						{
							fields += "," + key;
							values += "," + ValueParser(value,field,allowInvalidValues);
						}
					}
				}else{
					if (counter++ == 0)
					{
						fields = key;
						values = ValueParser(value,field,allowInvalidValues);
					}else
					{
						fields += "," + key;
						values += "," + ValueParser(value,field,allowInvalidValues);
					}
				}
			}
		}
		query += fields + ") VALUES ("+ values + ");";

		//XFT.LogSQLInfo(query);
		cache.getSaved().add(item);
		if (element.isAutoIncrement())
		{
			if (! item.hasPK())
			{
				Object key= con.insertNativeItem(query,element.getDbName(),element.getSQLName(),(String)item.getPkNames().get(0),element.getSequenceName());
				if (key != null)
				{
					item.setFieldValue((String)item.getPkNames().get(0),key);
				}
			}else{
				con.insertItem(query,element.getDbName(),login,cache);
			}
		}else
		{
			con.insertItem(query,element.getDbName(),login,cache);
		}
		
		if (!element.getFullXMLName().toLowerCase().startsWith("xdat"))
		{
			logger.info(element.getFullXMLName() +" stored.");	
		}
		
		return item;
	}

	
	public static String getSequenceName(GenericWrapperElement e) 
	{
	    if (sequences.get(e.getSQLName().toLowerCase()) == null)
	    {
	        String col_name = null;

		    GenericWrapperField key = (GenericWrapperField)e.getAllPrimaryKeys().get(0);
			
			String newQuery = "SELECT pg_get_serial_sequence('"+ e.getSQLName() + "','"+ key.getSQLName() + "') AS col_name";
			try {
			    Object o =(String)PoolDBUtils.ReturnStatisticQuery(newQuery,"col_name",e.getDbName(),null);
			    col_name = o.toString();
	            
	        } catch (Exception e1) {
	            col_name = e.getSQLName() + "_" + key.getSQLName() + "_seq";
	            newQuery = "SELECT * FROM " + col_name;
	            try {
                    PoolDBUtils.ExecuteNonSelectQuery(newQuery,e.getDbName(),null);
                } catch (Exception e2) {
                    col_name = StringUtils.SQLSequenceFormat1(e.getSQLName(),key.getSQLName());
                    newQuery = "SELECT * FROM " + col_name;
    	            try {
                        PoolDBUtils.ExecuteNonSelectQuery(newQuery,e.getDbName(),null);
                    } catch (Exception e3) {
                        col_name = StringUtils.SQLSequenceFormat2(e.getSQLName(),key.getSQLName());
                        newQuery = "SELECT * FROM " + col_name;
        	            try {
                            PoolDBUtils.ExecuteNonSelectQuery(newQuery,e.getDbName(),null);
                        } catch (Exception e4) {
                        }
                    }
                }

	        }
	        
	        if (col_name != null)
	        {
	            sequences.put(e.getSQLName().toLowerCase(),col_name);
	        }
	    }
		
		return (String)sequences.get(e.getSQLName().toLowerCase());
	}
	
	public static String getSequenceName(String table, String key, String dbName)
	{
	    String col_name = "";
	    String newQuery = "SELECT pg_get_serial_sequence('"+ table + "','"+ key + "') AS col_name";
		try {
		    Object o =(String)PoolDBUtils.ReturnStatisticQuery(newQuery,"col_name",dbName,null);
		    col_name = o.toString();
            
        } catch (Exception e1) {
            col_name = table + "_" + key + "_seq";
            newQuery = "SELECT * FROM " + col_name;
            try {
                PoolDBUtils.ExecuteNonSelectQuery(newQuery,dbName,null);
            } catch (Exception e2) {
                col_name = StringUtils.SQLSequenceFormat1(table,key);
                newQuery = "SELECT * FROM " + col_name;
	            try {
                    PoolDBUtils.ExecuteNonSelectQuery(newQuery,dbName,null);
                } catch (Exception e3) {
                    col_name = StringUtils.SQLSequenceFormat2(table,key);
                    newQuery = "SELECT * FROM " + col_name;
    	            try {
                        PoolDBUtils.ExecuteNonSelectQuery(newQuery,dbName,null);
                    } catch (Exception e4) {
                    }
                }
            }

        }
        return col_name;
	}
	
	private static void Quarantine(XFTItem oldI,UserI user, boolean quarantine, boolean overrideQuarantine,DBItemCache cache) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
	{
		// MARK MODIFIED AS TRUE
	    if (oldI.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
	    {
			Object metaDataId = (Object)oldI.getProperty(oldI.getGenericSchemaElement().getMetaDataFieldName().toLowerCase());
			if (metaDataId != null)
			{
				XFTItem oldMeta = XFTItem.NewItem(oldI.getGenericSchemaElement().getFullXMLName() +"_meta_data",null);
				oldMeta.setFieldValue("meta_data_id",metaDataId);
                oldMeta.setFieldValue("modified","1");
                oldMeta.setFieldValue("last_modified",Calendar.getInstance().getTime());
				
				boolean q;
				if (overrideQuarantine)
				    q = quarantine;
				else
				    q = oldI.getGenericSchemaElement().isQuarantine(quarantine);
				
				if (q){
				    oldMeta.setFieldValue("status",ViewManager.QUARANTINE);
				}
				UpdateItem(oldMeta,user,cache,false);
			}
            oldI.modified=true;
	    }
        
	}
	private static void StoreHistoryAndMeta(XFTItem oldI, XFTItem newI,UserI user,Boolean quarantine,DBItemCache cache) throws Exception
	{
		XFTItem meta=StoreHistoryAndMeta(oldI, user, quarantine, cache);
		
		
		if(newI!=null && meta!=null){
			newI.setProperty("meta.meta_data_id",meta.getProperty("meta_data_id"));
			newI.setProperty("meta.status",meta.getProperty("status"));
		}
	}
	
	private static final List<String> modifiable_status=Arrays.asList(ViewManager.ACTIVE,ViewManager.QUARANTINE);
	
	private static XFTItem StoreHistoryAndMeta(XFTItem oldI, UserI user,Boolean quarantine,DBItemCache cache) throws Exception
	{
		if (oldI.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
	    {
	    	XFTItem meta=(XFTItem)oldI.getMeta();

			//STORE HISTORY ITEM
	    	try {
				StoreHistoryItem(oldI,user,cache,oldI.getRowLastModified());
			} catch (ElementNotFoundException e) {
			}
	    	
			if (meta != null)
			{
				meta.setFieldValue("modified","1");
				meta.setFieldValue("last_modified",cache.getModTime());//other processes may change this as well, like modifications to a child element
				meta.setFieldValue("row_last_modified",cache.getModTime());//added to track specific changes to this row
				meta.setFieldValue("xft_version",cache.getChangeId());//added to track specific changes to this row
								
				if(!modifiable_status.contains(meta.getField("status"))){
					throw new UnmodifiableStatusException(oldI.getXSIType() + ":"+ oldI.getPKValueString() + ":"+ meta.getField("status"));
				}
				
				if (quarantine !=null){
					if(quarantine)
						meta.setFieldValue("status",ViewManager.QUARANTINE);
					else
						meta.setFieldValue("status",ViewManager.ACTIVE);
				}
				
				UpdateItem(meta,user,cache,false);
			}
			return meta;
	    }
		return null;
	}
	
	public static class UnmodifiableStatusException extends Exception{
		private static final long serialVersionUID = 68282673755608498L;
		
		public UnmodifiableStatusException(String s){
			super(s);
		}
	}
	
	/**
	 * @param item
	 * @return
	 */
	private static XFTItem UpdateItem(XFTItem oldI, XFTItem newI, UserI user,boolean quarantine, boolean overrideQuarantine,DBItemCache cache,boolean storeNULLS) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
	{

		Boolean q;
		if (overrideQuarantine)
		    q = quarantine;
		else
		    q = oldI.getGenericSchemaElement().isQuarantine(quarantine);
		
		// MARK MODIFIED AS TRUE
	    StoreHistoryAndMeta(oldI, newI, user, q, cache);
					
		//COPY PK VALUES INTO NEW ITEM
		newI.getProps().putAll(oldI.getPkValues());
		
		//UPDATE ITEM
		newI = UpdateItem(newI,user,cache,storeNULLS);

		cache.getModified().add(newI);
				
        oldI.modified=true;
        newI.modified=true;
        
		return newI;
	}
	
	public static void StoreHistoryItem(XFTItem oldI,UserI user,final DBItemCache cache,final Date previousChangeDate) throws ElementNotFoundException,Exception
	{
		String login = null;
		if (user != null)
		{
		    login = user.getUsername();
		}
		if (oldI.getGenericSchemaElement().storeHistory())
		{
		    XFTItem history = XFTItem.NewItem(oldI.getXSIType() + "_history",null);
			history.importNonItemFields(oldI,false);
			if (user != null)
			{
				history.setDirectProperty("change_user",user.getID());
			}
			if(previousChangeDate!=null){
				history.setDirectProperty("previous_change_date",previousChangeDate);
			}
			history.setDirectProperty("change_date",cache.getModTime());
			history.setDirectProperty("xft_version",oldI.getXFTVersion());
			
			Hashtable pkHash = (Hashtable)oldI.getPkValues();
			Enumeration pks = pkHash.keys();
			while (pks.hasMoreElements())
			{
				String name = (String)pks.nextElement();
				history.setFieldValue("new_row_" + name,pkHash.get(name));
			}
			
			//INSERT HISTORY ITEM
			InsertItem(history,login,cache,true);
		}
	}
	
	/**
	 * @param item
	 * @return
	 */
	private static XFTItem UpdateItem(XFTItem item,UserI user,DBItemCache cache, boolean storeNULLS) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,InvalidValueException
	{
	    String login = null;
		if (user != null)
		{
		    login = user.getUsername();
		}
		String query = "UPDATE ";
		GenericWrapperElement element = item.getGenericSchemaElement();
		
		query += element.getSQLName() + " SET ";
		
		Hashtable props = item.getProps();
		int counter = 0;
		Iterator iter = item.getPossibleFieldNames().iterator();
		while (iter.hasNext())
		{
		    Object[] possibleField = (Object[])iter.next();
		    String key = (String)possibleField[0];
			Object value = props.get(key);
		    if (! item.getPkNames().contains(key))
			{
		        if (value == null)
		        {
		            if (storeNULLS)
		            {
		                if (!item.getGenericSchemaElement().isHiddenFK(key))
					    {
						    GenericWrapperField field = element.getNonMultipleDataField(key);
						
			                if (! element.getExtensionFieldName().equalsIgnoreCase(field.getName()))
			                {
			                    if (field.isReference())
			                    {
			                        GenericWrapperElement e = (GenericWrapperElement)field.getReferenceElement();
	
			    					if (e.getAddin().equals(""))
			    					{
						                if (counter++ == 0)
										{
											query += key + "= NULL";
										}else
										{
											query += ", " + key + "= NULL";
										}
			    					}
			                    }else{
					                if (counter++ == 0)
									{
										query += key + "= NULL";
									}else
									{
										query += ", " + key + "= NULL";
									}
			                    }
			                }
					    }
		            }
		        }else{
			        if (! (value instanceof XFTItem))
					{
						GenericWrapperField field = element.getNonMultipleField(key);
						if (counter++ == 0)
						{
							query += key + "=" + ValueParser(value,field,false);
						}else
						{
							query += ", " + key + "=" + ValueParser(value,field,false);
						}
					}
		        }
			}
		}
		query += " WHERE ";
		
		counter = 0;
		iter = item.getPkNames().iterator();
		while (iter.hasNext())
		{
			String key = (String)iter.next();
			GenericWrapperField field = element.getNonMultipleField(key);
			if (counter++ == 0)
			{
				query += key + "=" + ValueParser(item.getProperty(key),field,false) + " ";
			}else
			{
				query += " AND " + key + "=" + ValueParser(item.getProperty(key),field,false) + " ";
			}
		}
		query += ";";
		
		//logger.debug(query);
		PoolDBUtils con = null;
		try {
			con = new PoolDBUtils();
			con.updateItem(query,element.getDbName(),login,cache);
			
			if (!element.getFullXMLName().toLowerCase().startsWith("xdat"))
			{
				logger.info(element.getFullXMLName() +" updated.");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
        item.modified=true;
        
		return item;
	}
	
	private static String buildType(GenericWrapperField field){
		return field.getXMLType().getLocalType();
	}
	
	
	/**
	 * Formats the object to a string for SQL interaction based on the XMLType of the field.
	 * @param o
	 * @param field
	 * @return
	 * @throws org.nrg.xft.exception.XFTInitException
	 */
	public static String ValueParser(Object o,GenericWrapperField field,boolean allowInvalidValues) throws org.nrg.xft.exception.XFTInitException,InvalidValueException
	{
		if (field != null)
		{
			if (field.isReference())
			{
				   try {
					   GenericWrapperElement foreign = (GenericWrapperElement)field.getReferenceElement();
					   GenericWrapperField foreignKey = (GenericWrapperField)foreign.getAllPrimaryKeys().get(0);
					   return ValueParser(o,foreignKey,allowInvalidValues);
				   } catch (Exception e) {
					   return o.toString();
				   }
			}


			String type = buildType(field);
			if (type.equalsIgnoreCase(""))
			{
				if (o instanceof String)
				{
				    if (o.toString().equalsIgnoreCase("NULL"))
				    {
				        return "NULL";
				    }else{
						return "'" + o.toString() + "'";
				    }
				}else
				{
					return o.toString();
				}
			}else
			{
				if (type.equalsIgnoreCase("string"))
				{
					if(field.getWrapped().getRule().getBaseType().equals("xs:anyURI")){
	                    return ValueParser(o,"anyURI",allowInvalidValues);
	                }
				}
				return ValueParser(o,type,allowInvalidValues);
			}
		}else
		{
			if (o instanceof String)
			{
			    if (o.toString().equalsIgnoreCase("NULL"))
			    {
			        return "NULL";
			    }else{
					return "'" + StringUtils.CleanForSQLValue(o.toString()) + "'";
			    }
			}else
			{
				return o.toString();
			}
		}
	}
	
	/**
	 * Formats the object to a string for SQL interaction based on the submitted type.
	 * @param o
	 * @param type
	 * @return
	 */
	public static String ValueParser(Object o,String type,boolean allowInvalidValues) throws InvalidValueException
	{
	    if (o != null)
	    {
	        if (o.toString().equalsIgnoreCase("NULL"))
	        {
	            return "NULL";
	        }
	    }else{
	        return "";
	    }
	    
		if (type.equalsIgnoreCase("string"))
		{
			if (o !=null && o.getClass().getName().equalsIgnoreCase("[B"))
			{
				byte[] b = (byte[]) o;
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
				try {
					baos.write(b);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (baos.toString().equalsIgnoreCase("NULL"))
				{
				    return "NULL";
				}else{
					String s=baos.toString();
					String upper=s.toUpperCase();
					if(s.contains("<") && s.contains(">") && (upper.contains("SCRIPT") || ((upper.contains("IMG") || upper.contains("IMAGE")) && (upper.contains("JAVASCRIPT"))))){
						if(!allowInvalidValues){
							AdminUtils.sendAdminEmail("Possible Cross-site scripting attempt blocked", s);
							throw new InvalidValueException("Use of '<' and '>' are not allowed in content.");
						}
					}
					return "'" + StringUtils.CleanForSQLValue(s) + "'";
				}
			}else{
			    if (o == null || o.toString().equalsIgnoreCase("NULL"))
				{
				    return "NULL";
				}else{
					String s=o.toString();
					String upper=s.toUpperCase();
					if(s.contains("<") && s.contains(">") && (upper.contains("SCRIPT") || ((upper.contains("IMG") || upper.contains("IMAGE")) && (upper.contains("JAVASCRIPT"))))){
						if(!allowInvalidValues){
							AdminUtils.sendAdminEmail("Possible Cross-site scripting attempt blocked", s);
							throw new InvalidValueException("Use of '<' and '>' are not allowed in content.");						
						}
					}
					return "'" + StringUtils.CleanForSQLValue(s) + "'";
				}
			}
		}else if (type.equalsIgnoreCase("anyURI"))
		{
			if (o == null || o.toString().equalsIgnoreCase("NULL"))
			{
			    return "NULL";
			}else{
				final String uri=o.toString().replace('\\', '/');
					if(uri.indexOf("..")>-1){
						throw new InvalidValueException("URIs cannot contain '..'");
					}
					if(FileUtils.IsAbsolutePath(uri)){
						//must be within a specified directory, to prevent directory traversal to secured files.
						if(!allowInvalidValues)FileUtils.ValidateUriAgainstRoot(uri, XFT.GetArchiveRootPath(),"URI references data outside of the archive:" + uri);

					}
					return "'" + StringUtils.CleanForSQLValue(uri) + "'";
			}
		}else if (type.equalsIgnoreCase("ID"))
		{
			return "'" + StringUtils.CleanForSQLValue(o.toString()) + "'";
		}else if (type.equalsIgnoreCase("boolean"))
		{
			if (o.toString().equalsIgnoreCase("true") || o.toString().equalsIgnoreCase("1"))
			{
				return "1";
			}else
			{
				return "0";
			}
		}else if (type.equalsIgnoreCase("float"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else {
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("double"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("decimal"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("integer"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("nonPositiveInteger"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("negativeInteger"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("long"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("int"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("short"))
        {
            return o.toString();
        }else if (type.equalsIgnoreCase("byte"))
        {
            return o.toString();
        }else if (type.equalsIgnoreCase("nonNegativeInteger"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("unsignedLong"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("unsignedInt"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("unsignedShort"))
        {
            return o.toString();
        }else if (type.equalsIgnoreCase("unsignedByte"))
        {
            return o.toString();
        }else if (type.equalsIgnoreCase("positiveInteger"))
        {
            if (o.toString().equalsIgnoreCase("nan"))
            {
                return "'" + o.toString() +  "'";
            }else if (o.toString().equalsIgnoreCase("inf")||o.toString().equalsIgnoreCase("Infinity")){
                return "'Infinity'";
            }else if (o.toString().equalsIgnoreCase("-inf")||o.toString().equalsIgnoreCase("-Infinity")){
                return "'-Infinity'";
            }else if(o.toString().equals("")){
                return "NULL";
            }else{
                return o.toString();
            }
        }else if (type.equalsIgnoreCase("time"))
		{
			return "'" + StringUtils.CleanForSQLValue(o.toString()) + "'";
		}else if (type.equalsIgnoreCase("date"))
		{
			return "'" + StringUtils.CleanForSQLValue(o.toString()) + "'";
		}else if (type.equalsIgnoreCase("dateTime"))
		{
            Date d = null;
            if (o instanceof Date)
            {
                d= (Date)o;
            }else{
                try {
                    d = DateUtils.parseDateTime(o.toString());
                } catch (ParseException e) {
                    if (o.toString().trim().equals("NOW()")){
                        return "NOW()";
                    }else{
                        return "'" + StringUtils.CleanForSQLValue(o.toString()) + "'";
                    }
                }
            }
            
            if (d!=null){
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                
                return "'" + df.format(d) + "'";
            }else{
                return "'" + StringUtils.CleanForSQLValue(o.toString()) + "'";
            }
		}else
		{
			return o.toString();
		}
	}
		
	/**
	 * @param item
	 * @param toRemove
	 * @param user
	 */
	public static void RemoveItemReference(XFTItem item, String xmlPath, XFTItem toRemove, UserI user, EventMetaI c) throws SQLException,Exception
	{
	    DBItemCache cache =  new DBItemCache(user,c);
	    RemoveItemReference((XFTItem)item,xmlPath,(XFTItem)toRemove,user,cache,false,false);
	    
	    PoolDBUtils con = null;
		try {
			if (!cache.getSQL().equals("") && !cache.getSQL().equals("[]"))
			{
                XFT.LogInsert(cache.getSQL(),item);

				String username = null;
				Integer xdat_user_id = null;
				if (user != null)
				{
				    username = user.getUsername();
				    xdat_user_id=user.getID();
				}
    			
                if (!cache.getDBTriggers().contains(item, false)){
                    cache.getDBTriggers().add(item);
                }
                            
                PerformUpdateTriggers(cache, username,xdat_user_id,false);
                
    			con = new PoolDBUtils();
    			con.sendBatch(cache,item.getDBName(),username);

                //PerformUpdateTriggers(cache, username,xdat_user_id,false); This shouldn't be necessary TO
			}else{
                   logger.info("Pre-existing item found without modifications");
               }
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
        
	}
	
	/**
	 * @param item
	 * @param toRemove
	 * @param user
	 */
	private static void RemoveItemReference(XFTItem item, String xmlPath, XFTItem toRemove, UserI user,DBItemCache cache, boolean parentDeleted,boolean noHistory) throws Exception
	{
		String login = null;
		if (user != null)
		{
		    login = user.getUsername();
		}
		
		if(cache!=null){
			cache.getRemoved().add(toRemove);
		}
		
	    try {
            GenericWrapperElement root = item.getGenericSchemaElement();

            boolean foundField = false;
            GenericWrapperField field = null;
            if (xmlPath != null && !xmlPath.equals(""))
            {
                field = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
            }else{
                Iterator iter = root.getReferenceFields(true).iterator();
                while (iter.hasNext())
                {
                    GenericWrapperField f = (GenericWrapperField)iter.next();
                    if (f.getReferenceElement().getFullXMLName().equalsIgnoreCase(toRemove.getXSIType()))
                    {
                        ArrayList subs = item.getChildItems(f);
                        Iterator children = subs.iterator();
                        while(children.hasNext())
                        {
                            XFTItem child = (XFTItem)children.next();
                            if (XFTItem.CompareItemsByPKs(child,toRemove))
                            {
                                foundField = true;
                                field = f;
                                break;
                            }
                        }
                        
                        if (foundField)
                        {
                            break;
                        } 
                    }
                }
            }
            
            
            if (field!=null)
            {
                //GenericWrapperElement foreign = (GenericWrapperElement)field.getReferenceElement();
                //MODIFIED ON 04/25/07 BY TIM To capture extensions
                GenericWrapperElement foreign = toRemove.getGenericSchemaElement();
                if (foreign.hasUniqueIdentifiers() || foreign.matchByValues())
                {
                    Integer referenceCount = null;
                    if (cache.getPreexisting().contains(toRemove,false))
                    {
                        referenceCount = new Integer(100);
                    }else{
                        referenceCount = XFTReferenceManager.NumberOfReferences(toRemove);
                    }
                    
                    if (field.isMultiple())
                    {
                        //CHECK TO SEE IF OTHER ELEMENTS REFERENCE THIS ONE
                        //IF SO, BREAK THE REFERENCE BUT DO NOT DELETE
                        //ELSE, DELETE THE ITEM
                        if (referenceCount.intValue() > 1)
                        {
                            if (field.getXFTReference().isManyToMany())
                            {
                                XFTManyToManyReference ref = (XFTManyToManyReference)field.getXFTReference();
                                ArrayList values = new ArrayList();
                                Iterator refCols = ref.getMappingColumnsForElement(foreign).iterator();
                                while (refCols.hasNext())
                                {
                                    XFTMappingColumn spec = (XFTMappingColumn)refCols.next();
                                    Object o = toRemove.getProperty(spec.getForeignKey().getXMLPathString(toRemove.getXSIType()));
                                    ArrayList al = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o,spec.getXmlType().getLocalType(),true));
                                    values.add(al);
                                }
                                
                                refCols = ref.getMappingColumnsForElement(root).iterator();
                                while (refCols.hasNext())
                                {
                                    XFTMappingColumn spec = (XFTMappingColumn)refCols.next();
                                    Object o = item.getProperty(spec.getForeignKey().getXMLPathString(item.getXSIType()));
                                    ArrayList al = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o,spec.getXmlType().getLocalType(),true));
                                    values.add(al);
                                }
                                
                                if(values.size()>1){
                                    DBAction.DeleteMappings(ref,root.getDbName(),values,login,cache,noHistory);                            
                                }else{
                                    throw new Exception("Failed to identify both ids for the mapping table.");
                                }
                                
                            }else{  

                                GenericWrapperElement gwe = null;
                        		XFTSuperiorReference ref = (XFTSuperiorReference)field.getXFTReference();
                                Iterator refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext())
                                {
                                    XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
                                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." +spec.getLocalCol());
                                    gwe = f.getParentElement().getGenericXFTElement();
                                } 
                        		
                                XFTItem updateItem = toRemove;
                                if (!gwe.getFullXMLName().equalsIgnoreCase(updateItem.getXSIType()))
                                {
                                    updateItem = updateItem.getExtensionItem(gwe.getFullXMLName());
                                    if (updateItem == null)
                                    {
                                        updateItem = toRemove;
                                    }
                                }
                                
                                if (!noHistory)
                                {
                                    try {
                    	        		StoreHistoryAndMeta(updateItem,user,null,cache);
                            		} catch (ElementNotFoundException e) {
                            			if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
                            			{
                            				throw e;
                            			}
                            		}
                                }
                                
                                refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext())
                                {
                                    XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
                                    updateItem.setProperty(updateItem.getXSIType() + "." +spec.getLocalCol(),"NULL");
                                }
                                
                        		//UPDATE ITEM
                                toRemove = UpdateItem(updateItem,user,cache,true);
                        		item.removeItem(toRemove);
                            }
                        }else{
                            if (field.getXFTReference().isManyToMany())
                            {
                                XFTManyToManyReference ref = (XFTManyToManyReference)field.getXFTReference();
                                ArrayList values = new ArrayList();
                                ArrayList<XFTMappingColumn> forCols = ref.getMappingColumnsForElement(foreign);
                                for (XFTMappingColumn spec:forCols)
                                {
                                    Object o = toRemove.getProperty(spec.getForeignKey().getXMLPathString(toRemove.getXSIType()));
                                    ArrayList al = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o,spec.getXmlType().getLocalType(),true));
                                    values.add(al);
                                }
                                
                                ArrayList<XFTMappingColumn> localCols = ref.getMappingColumnsForElement(root);
                                for (XFTMappingColumn spec:localCols)
                                {
                                    Object o = item.getProperty(spec.getForeignKey().getXMLPathString(item.getXSIType()));
                                    ArrayList al = new ArrayList();
                                    al.add(spec.getLocalSqlName());
                                    al.add(DBAction.ValueParser(o,spec.getXmlType().getLocalType(),true));
                                    values.add(al);
                                }
                                
                                if(values.size()>1){
                                    
                                    DBAction.DeleteMappings(ref,root.getDbName(),values,login,cache,noHistory);  
                                   
                            		DeleteItem(toRemove,user,cache,noHistory,field.isPossibleLoop());
                            		item.removeItem(toRemove);
                                }else{
                                    throw new Exception("Failed to identify both ids for the mapping table.");
                                }
                            }else{
                        		
                        		DeleteItem(toRemove,user,cache,noHistory,field.isPossibleLoop());
                        		item.removeItem(toRemove);
                            }
                        }
                    }else{
                        if (referenceCount.intValue() > 1)
                        {
                            if (!parentDeleted)
                            {
                                GenericWrapperElement gwe = null;
        	            		XFTSuperiorReference ref = (XFTSuperiorReference)field.getXFTReference();
        	            		
        	            		//FIND EXTENSION LEVEL FOR UPDATE
                                Iterator refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext())
                                {
                                    XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
                                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." +spec.getLocalCol());
                                    gwe = f.getParentElement().getGenericXFTElement();
                                }
                                
                                //FIND CORRECT EXTENSION LEVEL ITEM
                                XFTItem updateItem = item;
                                if (!gwe.getFullXMLName().equalsIgnoreCase(updateItem.getXSIType()))
                                {
                                    updateItem = updateItem.getExtensionItem(gwe.getFullXMLName());
                                    if (updateItem == null)
                                    {
                                        updateItem = item;
                                    }
                                }

                                if (!noHistory)
                                {
            	                    try {
                    	        		StoreHistoryAndMeta(updateItem,user,null,cache);
            	            		} catch (ElementNotFoundException e) {
            	            			if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
            	            			{
            	            				throw e;
            	            			}
            	            		}
                                }
                                
                                //UPDATE REFERENCE
                                refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext())
                                {
                                    XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
                                    updateItem.setProperty(updateItem.getXSIType() + "." +spec.getLocalCol(),"NULL");
                                }
                                
        	            		
        	            		//UPDATE ITEM
                                updateItem = UpdateItem(updateItem,user,cache,false);
                            }
                    		item.removeItem(toRemove);
                        }else{
                            if (!parentDeleted)
                            {
                                GenericWrapperElement gwe = null;
        	            		XFTSuperiorReference ref = (XFTSuperiorReference)field.getXFTReference();
        	            		
        	            		//FIND EXTENSION LEVEL FOR UPDATE
                                Iterator refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext())
                                {
                                    XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
                                    GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." +spec.getLocalCol());
                                    gwe = f.getParentElement().getGenericXFTElement();
                                }
                                
                                //FIND CORRECT EXTENSION LEVEL ITEM
                                XFTItem updateItem = item;
                                if (!gwe.getFullXMLName().equalsIgnoreCase(updateItem.getXSIType()))
                                {
                                    updateItem = updateItem.getExtensionItem(gwe.getFullXMLName());
                                    if (updateItem == null)
                                    {
                                        updateItem = item;
                                    }
                                }

                                if (!noHistory)
                                {
            	                    try {
                    	        		StoreHistoryAndMeta(updateItem,user,null,cache);
            	            		} catch (ElementNotFoundException e) {
            	            			if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
            	            			{
            	            				throw e;
            	            			}
            	            		}
                                }
                                
                                //UPDATE REFERENCE
                                refsCols = ref.getKeyRelations().iterator();
                                while (refsCols.hasNext())
                                {
                                    XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
                                    updateItem.setProperty(updateItem.getXSIType() + "." +spec.getLocalCol(),"NULL");
                                }
                                
        	            		
        	            		//UPDATE ITEM
                                updateItem = UpdateItem(updateItem,user,cache,false);
                            }
    	            		item.removeItem(toRemove);
    	            		
                    		DeleteItem(toRemove,user,cache,noHistory,field.isPossibleLoop());
                        }
                    }
                }else{
                    if (field.isMultiple())
                    {
                        if (field.getXFTReference().isManyToMany())
                        {
                            XFTManyToManyReference ref = (XFTManyToManyReference)field.getXFTReference();
                            ArrayList values = new ArrayList();
                            Iterator refCols = ref.getMappingColumnsForElement(foreign).iterator();
                            while (refCols.hasNext())
                            {
                                XFTMappingColumn spec = (XFTMappingColumn)refCols.next();
                                Object o = toRemove.getProperty(spec.getForeignKey().getXMLPathString(toRemove.getXSIType()));
                                ArrayList al = new ArrayList();
                                al.add(spec.getLocalSqlName());
                                al.add(DBAction.ValueParser(o,spec.getXmlType().getLocalType(),true));
                                values.add(al);
                            }
                            
                            refCols = ref.getMappingColumnsForElement(root).iterator();
                            while (refCols.hasNext())
                            {
                                XFTMappingColumn spec = (XFTMappingColumn)refCols.next();
                                Object o = item.getProperty(spec.getForeignKey().getXMLPathString(item.getXSIType()));
                                ArrayList al = new ArrayList();
                                al.add(spec.getLocalSqlName());
                                al.add(DBAction.ValueParser(o,spec.getXmlType().getLocalType(),true));
                                values.add(al);
                            }
                            
                            if(values.size()>1){
                        	DBAction.DeleteMappings(ref,root.getDbName(),values,login,cache,noHistory);  
                           
                    		DeleteItem(toRemove,user,cache,noHistory,field.isPossibleLoop());
                    		item.removeItem(toRemove);
                            }else{
                                throw new Exception("Failed to identify both ids for the mapping table.");
                            }
                        }else{
                    		
                    		DeleteItem(toRemove,user,cache,noHistory,field.isPossibleLoop());
                    		item.removeItem(toRemove);
                        }
                    }else{

                        if (!parentDeleted)
                        {
                            if (field.isRequired())
                            {
                                throw new Exception("Unable to delete REQUIRED " + toRemove.getXSIType() + ". The entire parent " + item.getXSIType() + " must be deleted.");
                            }
                            
                            GenericWrapperElement gwe = null;
    	            		XFTSuperiorReference ref = (XFTSuperiorReference)field.getXFTReference();
    	            		
    	            		//FIND EXTENSION LEVEL FOR UPDATE
                            Iterator refsCols = ref.getKeyRelations().iterator();
                            while (refsCols.hasNext())
                            {
                                XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
                                GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(toRemove.getXSIType() + "." +spec.getLocalCol());
                                gwe = f.getParentElement().getGenericXFTElement();
                            }
                            
                            //FIND CORRECT EXTENSION LEVEL ITEM
                            XFTItem updateItem = item;
                            if (!gwe.getFullXMLName().equalsIgnoreCase(updateItem.getXSIType()))
                            {
                                updateItem = updateItem.getExtensionItem(gwe.getFullXMLName());
                                if (updateItem == null)
                                {
                                    updateItem = item;
                                }
                            }

                            if (!noHistory)
                            {
        	                    try {
                	        		StoreHistoryAndMeta(updateItem,user,null,cache);
        	            		} catch (ElementNotFoundException e) {
        	            			if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
        	            			{
        	            				throw e;
        	            			}
        	            		}
                            }
                            
                            //UPDATE REFERENCE
                            refsCols = ref.getKeyRelations().iterator();
                            while (refsCols.hasNext())
                            {
                                XFTRelationSpecification spec = (XFTRelationSpecification)refsCols.next();
                                updateItem.setProperty(updateItem.getXSIType() + "." +spec.getLocalCol(),"NULL");
                            }
                            
    	            		
    	            		//UPDATE ITEM
                            updateItem = UpdateItem(updateItem,user,cache,false);
                        }
                        
	            		item.removeItem(toRemove);
	            		
                		DeleteItem(toRemove,user,cache,noHistory,field.isPossibleLoop());
                    }
                }
            }
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
            throw e;
        }
	}
	

	
	/**
	 * @param mappingName
	 * @param dbName
	 * @param cc
	 * @throws Exception
	 */
	private static void DeleteMappings(XFTManyToManyReference mapping, String dbName, ArrayList values,String login,DBItemCache cache, boolean noHistory) throws Exception
	{
	    if (values.size() > 0)
	    {
			PoolDBUtils con = null;
			try {
				con = new PoolDBUtils();

				String query;
				int counter;
				Iterator keys;
				if (!noHistory)
				{
				    query = "INSERT INTO " + mapping.getHistoryTableName();
					query += " (";
					  
					counter =0;
					keys = values.iterator();
					while (keys.hasNext())
					{
					    ArrayList key = (ArrayList)keys.next();
					    
					    if (counter++!=0)
					    {
					        query +=", ";
					    }
					    query += key.get(0);
					}
					query += ") VALUES (";
					
					counter =0;
					keys = values.iterator();
					while (keys.hasNext())
					{
					    ArrayList key = (ArrayList)keys.next();
					    
					    if (counter++!=0)
					    {
					        query +=", ";
					    }
					    query += key.get(1);
					}
					query +=");";

					logger.debug(query);		        
					con.updateItem(query,dbName,login,cache);
				}
				    
				query = "DELETE FROM " + mapping.getMappingTable() + " WHERE ";
				counter =0;
				keys = values.iterator();
				while (keys.hasNext())
				{
				    ArrayList key = (ArrayList)keys.next();
				    
				    if (counter++!=0)
				    {
				        query +=" AND ";
				    }
				    query += key.get(0) + "=" + key.get(1);
				}
				
				logger.debug(query);		        
				con.updateItem(query,dbName,login,cache);
				logger.info(mapping.getMappingTable() +" removed.");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}	
	    }
	}
	
	/**
	 * @param item
	 * @param toRemove
	 * @param user
	 */
	public static void DeleteItem(XFTItem item,UserI user,EventMetaI c) throws SQLException,Exception
	{
	    DBItemCache cache =  new DBItemCache(user,c);
	    DeleteItem(item,user,cache,false,false);
	    
        XFT.LogInsert(cache.getSQL(),item);
        
	    PoolDBUtils con = null;
		try {

			String username = null;
			Integer xdat_user_id = null;
			if (user != null)
			{
			    username = user.getUsername();
			    xdat_user_id=user.getID();
			}
			
            if (!cache.getDBTriggers().contains(item, false)){
                cache.getDBTriggers().add(item);
            }
            
            PerformUpdateTriggers(cache, username,xdat_user_id,false);
            
			con = new PoolDBUtils();
			con.sendBatch(cache,item.getDBName(),username);

            //PerformUpdateTriggers(cache, username,xdat_user_id,false); This shouldn't be necessary TO
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * @param item
	 * @param toRemove
	 * @param user
	 */
	public static void CleanDeleteItem(XFTItem item,UserI user,EventMetaI c) throws SQLException,Exception
	{
	    DBItemCache cache =  new DBItemCache(user,c);
	    DeleteItem(item,user,cache,true,false);

        XFT.LogInsert(cache.getSQL(),item);

	    PoolDBUtils con = null;
		try {

			String username = null;
			Integer xdat_user_id = null;
			if (user != null)
			{
			    username = user.getUsername();
			    xdat_user_id=user.getID();
			}
			
            if (!cache.getDBTriggers().contains(item, false)){
                cache.getDBTriggers().add(item);
            }

            PerformUpdateTriggers(cache, username,xdat_user_id,false);
            
			con = new PoolDBUtils();
			con.sendBatch(cache,item.getDBName(),username);

            //PerformUpdateTriggers(cache, username,xdat_user_id,false); This shouldn't be necessary TO
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private static void DeleteItem(XFTItem item,UserI user,DBItemCache cache,boolean cleanHistory, boolean possibleLoop) throws XFTInitException,ElementNotFoundException,Exception
	{
		String login = null;
		if (user != null)
		{
		    login = user.getUsername();
		}
		
		if(user!=null)
		{
			if(!user.canDelete(item)){
				throw new org.nrg.xdat.exceptions.IllegalAccessException("Unable to delete "+ item.getXSIType());
			}
		}
		
		if (!cleanHistory)
		{
		    try {
        		StoreHistoryAndMeta(item,user,null,cache);
			} catch (ElementNotFoundException e) {
				if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
				{
					throw e;
				}
			} catch (Exception e) {
				if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
				{
					throw e;
				}
			}
		}
		
		XFTItem extensionItem = null;
		if (item.getGenericSchemaElement().isExtension())
		{
		    extensionItem = item.getExtensionItem();
		}
		
		//DELETE RELATIONS
		//allowExtension set to true to delete xnat:demographicData when deleting xnat:subjectData (otherwise only deletes xnat:abstactDemographicdata)
		item = item.getCurrentDBVersion(false,false);
		
		if(item!=null){
			Iterator refs = item.getGenericSchemaElement().getReferenceFields(true).iterator();
			while (refs.hasNext())
			{
			    GenericWrapperField ref = (GenericWrapperField)refs.next();
			    String xmlPath = ref.getXMLPathString(item.getXSIType());
			    
	            if (!ref.getPreventLoop() || !possibleLoop){
	    		    Iterator children = item.getChildItems(xmlPath).iterator();
	    		    while (children.hasNext())
	    		    {
	    		        XFTItem child = (XFTItem)children.next();
	    		        
	    		        if (child.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
	    			    {
	    			        if (extensionItem==null || (! XFTItem.CompareItemsByPKs(child,extensionItem)))
	    			        {
	    			            DBAction.RemoveItemReference(item,xmlPath,child.getCurrentDBVersion(true, true),user,cache,true,cleanHistory);
	    			        }
	    			    }
	    		    }
	            }
			}
			
			if (cleanHistory)
			{
				//CLEAN HISTORY
			    if (item.hasHistory())
			    {
				    ItemCollection items = item.getHistory();
				    Iterator iter = items.getItemIterator();
				    while (iter.hasNext())
				    {
				        ItemI history = (ItemI)iter.next();
				        DeleteHistoryItem(history.getItem(),item.getGenericSchemaElement(),login,cache);
				    }
			    }
			}
			

			try {
	            // MARK META_DATA to DELETED
	            if (item.getGenericSchemaElement().getAddin().equalsIgnoreCase(""))
	            {
	            	Object metaDataId = (Object)item.getProperty(item.getGenericSchemaElement().getMetaDataFieldName().toLowerCase());
	            	if (metaDataId != null)
	            	{
	            		XFTItem oldMeta = XFTItem.NewItem(item.getGenericSchemaElement().getFullXMLName() +"_meta_data",null);
	            		oldMeta.setFieldValue("meta_data_id",metaDataId);
	            		oldMeta.setFieldValue("status",ViewManager.DELETED);
	            		UpdateItem(oldMeta,user,cache,false);
	            	}
	            }
	        } catch (Exception e) {
	            logger.error("",e);
	        }
			
			//DELETE
			DeleteItem(item,login,cache);
			
			if (extensionItem !=null)
			{
			    DeleteItem(extensionItem,user,cache,cleanHistory,false);
			}
		}
	}
	
	private static void DeleteHistoryItem(XFTItem history, GenericWrapperElement parentElement,String login, DBItemCache cache) throws Exception
	{
	    //DELETE OTHER HISTORY ITEMS
	    
	    DeleteItem(history,login,cache);
	}
	
	private static void DeleteItem(XFTItem item,String login,DBItemCache cache) throws Exception
	{
		String query = "DELETE FROM ";
		GenericWrapperElement element = item.getGenericSchemaElement();
		
		query += element.getSQLName() + " WHERE ";
		
		Hashtable props = (Hashtable)item.getPkValues();
		Enumeration enumer = props.keys();
		int counter = 0;
		while (enumer.hasMoreElements())
		{
			String key = (String)enumer.nextElement();
			Object value = props.get(key);
			if (!(value instanceof XFTItem))
			{
				GenericWrapperField field = element.getNonMultipleDataField(key);
				if (counter++ == 0)
				{
					query += key + "=" + ValueParser(value,field,true);
				}else
				{
					query += ", " + key + "=" + ValueParser(value,field,true);
				}
			}
		}
		query += ";";
		
		logger.debug(query);
		PoolDBUtils con = null;
		try {
			con = new PoolDBUtils();
			con.updateItem(query,element.getDbName(),login,cache);
			logger.info(element.getFullXMLName() +" Removed.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static void InsertMetaDatas()
	{
		try {
			Iterator iter = XFTManager.GetInstance().getAllElements().iterator();
			while (iter.hasNext())
			{
				GenericWrapperElement e =(GenericWrapperElement)iter.next();
                if (e.getAddin().equals(""))
				  InsertMetaDatas(e.getXSIType());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void InsertMetaDatas(String elementName)
	{
	    try {
			GenericWrapperElement e =(GenericWrapperElement)GenericWrapperElement.GetElement(elementName);
			//System.out.println("Update Meta-Data: " + e.getFullXMLName());

            DBItemCache cache = new DBItemCache(null,null);
            logger.info("Update Meta-Data: " + e.getFullXMLName());
            ArrayList<GenericWrapperField> keys=e.getAllPrimaryKeys();
            String keyString="";
            for (GenericWrapperField key: keys){
               if (!keyString.equals("")){
                   keyString +=",";
               }
               keyString += key.getSQLName();
            }
            String query = "SELECT "+ keyString +" FROM " + e.getSQLName() + " WHERE " + e.getMetaDataFieldName() + " IS NULL;";
            XFTTable t = XFTTable.Execute(query, e.getDbName(), null);
            if (t.size()> 0){
                System.out.println(e.getFullXMLName() + " missing " + t.size() + " meta rows.");
                t.resetRowCursor();
                while(t.hasMoreRows()){     
                    Object[] row = t.nextRow();
                    XFTItem meta = XFTItem.NewMetaDataElement(null,e.getXSIType(),false,Calendar.getInstance().getTime(),null);
                    StoreItem(meta,null,true,false,false,false, cache,null,true);
                    keyString = "";
                    int count =0;
                    for (GenericWrapperField key: keys){
                        if (!keyString.equals("")){
                            keyString +=" AND ";
                        }
                        keyString += key.getSQLName() + "=" + DBAction.ValueParser(row[count++], key,true);
                     }
                    String st = "UPDATE " + e.getSQLName() + " SET " + e.getMetaDataFieldName() + "=" + meta.getProperty("meta_data_id") + " WHERE " + keyString;
                    
                    cache.addStatement(st);
                    
                }
                
            }
            else{
                System.out.println(e.getFullXMLName() + " has all meta rows.");
            }
            if (!cache.getSQL().equals("") && !cache.getSQL().equals("[]"))
           {
               PoolDBUtils con = null;
                try {
                    con = new PoolDBUtils();
                    con.sendBatch(cache,e.getDbName(),null);
                     
                } catch (SQLException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw ex;
                }
           }else{
            }
		} catch (Exception e) {
			logger.error(e);
            System.out.println("FAILED: " + elementName);
            
		}
	}
	

	
	public static void AdjustSequences()
	{
        long startTime = Calendar.getInstance().getTimeInMillis();
	    if (!ADJUSTED_SEQUENCES)
	    {
			try {
                ArrayList dbs= new ArrayList();
				Iterator iter = XFTManager.GetInstance().getAllElements().iterator();
				while (iter.hasNext())
				{
					GenericWrapperElement input =(GenericWrapperElement)iter.next();
					if (input.isAutoIncrement() && !input.getSQLName().equalsIgnoreCase("xdat_history") && !input.getSQLName().equalsIgnoreCase("xdat_meta_data"))
					{
                        String dbName = input.getDbName();
                        if (!dbs.contains(dbName)){
                            dbs.add(dbName);
                        }
						GenericWrapperField pk = (GenericWrapperField)input.getAllPrimaryKeys().get(0);
						String sequenceName = input.getSequenceName();
						Object o = PoolDBUtils.ReturnStatisticQuery("SELECT MAX(" + pk.getSQLName() + ") AS MAX_COUNT from "+input.getSQLName(),"MAX_COUNT",input.getDbName(),null);
						Object current = PoolDBUtils.ReturnStatisticQuery("SELECT last_value AS LAST_COUNT from "+sequenceName,"LAST_COUNT",input.getDbName(),null);
						if (o == null)
						{
						    o = 1;
						}
						
						if (current == null)
						{
                            System.out.println("Adjusting missing sequence (" + input.getFullXMLName() +");");
							PoolDBUtils.ExecuteNonSelectQuery("SELECT setval('"+ sequenceName +"',"+o+")",input.getDbName(),null);
						}else{
                            int i1 = o instanceof Integer ? (Integer) o : (o instanceof Long ? ((Long) o).intValue() : Integer.parseInt(o.toString()));
						    Long i2 = (Long)current;
						    if (i1 > i2.intValue()){
                                System.out.println("Adjusting invalid sequence (" + input.getFullXMLName() +");");
						        PoolDBUtils.ExecuteNonSelectQuery("SELECT setval('"+ sequenceName +"',"+o+")",input.getDbName(),null);
						    }
						}
					}
				}

				for (Object object : XFTReferenceManager.GetInstance().getUniqueMappings())
				{
                    XFTManyToManyReference map = (XFTManyToManyReference) object;
					String sequenceName = DBAction.getSequenceName(map.getMappingTable(),map.getMappingTable() + "_id",map.getElement1().getDbName());
					Object o = PoolDBUtils.ReturnStatisticQuery("SELECT MAX(" + map.getMappingTable() + "_id) AS MAX_COUNT from "+map.getMappingTable(),"MAX_COUNT",map.getElement1().getDbName(),null);
					//PoolDBUtils.ExecuteNonSelectQuery("SELECT setval('"+ sequenceName +"',"+o+")",map.getElement1().getDbName(),null);
                    Object current = PoolDBUtils.ReturnStatisticQuery("SELECT last_value AS LAST_COUNT from "+sequenceName,"LAST_COUNT",map.getElement1().getDbName(),null);
                    if (o == null)
                    {
                        o = 1;
                    }

                    if (current == null)
                    {
                        System.out.println("Adjusting missing mapping sequence (" + map.getMappingTable() +");");
                        PoolDBUtils.ExecuteNonSelectQuery("SELECT setval('"+  map.getMappingTable() +"',"+o+")",map.getElement1().getDbName(),null);
                    }else{
                        Integer i1 = (Integer)o;
                        Long i2 = (Long)current;
                        if (i1.intValue()>i2.intValue()){
                            System.out.println("Adjusting invalid mapping sequence (" + map.getMappingTable() +");");
                            PoolDBUtils.ExecuteNonSelectQuery("SELECT setval('"+  map.getMappingTable() +"',"+o+")",map.getElement1().getDbName(),null);
                        }
                    }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	        ADJUSTED_SEQUENCES = true;
            if(XFT.VERBOSE)System.out.println("Finished db sequence check " + (Calendar.getInstance().getTimeInMillis()-startTime) + "ms");
	    }
	}
	
	public static Long CountInstancesOfFieldValues(String tableName,String db,CriteriaCollection al) throws Exception
	{
	    String query = " SELECT COUNT(*) AS INSTANCE_COUNT FROM " + tableName;
	    query += " WHERE " + al.getSQLClause(null) + ";";
	    
	    return (Long)PoolDBUtils.ReturnStatisticQuery(query,"INSTANCE_COUNT",db,null);
	}
	
	public static XFTItem SelectItemByID(String query, String functionName, GenericWrapperElement element, UserI user,boolean allowMultiples) throws Exception
	{    
	    String login = null;
	    if (user != null)
	    {
	        login = user.getUsername();
	    }
	    String s =(String)PoolDBUtils.ReturnStatisticQuery(query,functionName,element.getDbName(),login);
	    XFTItem item = XFTItem.PopulateItemFromFlatString(s,user);
	    if (allowMultiples)
	    {
		    item.setPreLoaded(true);
	    }
	    s = null;
	    return item;
	}
	
	public static XFTItem SelectItemByIDs(GenericWrapperElement element, Object[] ids, UserI user, boolean allowMultiples,boolean preventLoop) throws Exception
	{
	    String functionName = element.getTextFunctionName();
	    String functionCall = "SELECT " + functionName + "(";
	    for (int i=0; i<ids.length;i++)
	    {
	        if (i>0)functionCall+=",";
	        functionCall+=ids[i];
	    }
		functionCall+=",0," + allowMultiples + ",FALSE," + preventLoop +");";
	    
	    return SelectItemByID(functionCall,functionName,element,user,allowMultiples);
	}
	
	public static void PerformUpdateTriggers(DBItemCache cache,String userName,Integer xdat_user_id,boolean asynchronous){
        long localStartTime= Calendar.getInstance().getTimeInMillis();
        ArrayList<String> cmds= new ArrayList<String>();
        try{
            //process modification triggers
            if (cache.getDBTriggers().size()>0){
            	
                ArrayList<XFTItem> items=cache.getDBTriggers().items();
                
                if(asynchronous)cmds.add("SET LOCAL synchronous_commit TO OFF;");
                
                String dbname=null;
                
                for (XFTItem mod : items){
                	int count =0;
                    String ids = "";
                    ArrayList keys = mod.getGenericSchemaElement().getAllPrimaryKeys();
                    Iterator keyIter = keys.iterator();
                    while (keyIter.hasNext())
                    {
                        GenericWrapperField sf = (GenericWrapperField)keyIter.next();
                        Object id = mod.getProperty(sf);
                        if (count++>0)ids+=",";
                        ids+=DBAction.ValueParser(id, sf,true);
                    }

                    dbname=mod.getDBName();

                	PoolDBUtils.CreateCache(dbname,userName);
                    cmds.add(String.format("SELECT update_ls_%s(%s,%s)", new Object[]{mod.getGenericSchemaElement().getFormattedName(),ids,(xdat_user_id==null)?"NULL":xdat_user_id}));
                }
                
                if(asynchronous)cmds.add("SET LOCAL synchronous_commit TO ON;");
                
                //PoolDBUtils.ExecuteBatch(cmds, dbname, userName);
                
                for(String s:cmds){
                	try {
						PoolDBUtils.ExecuteNonSelectQuery(s, dbname, userName);
					} catch (RuntimeException e) {
					}
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        }
        if(XFT.VERBOSE)System.out.println("triggers (" + cmds.size() + "): "+(Calendar.getInstance().getTimeInMillis()-localStartTime) + " ms");
	}
}

