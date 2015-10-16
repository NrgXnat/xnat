/*
 * org.nrg.xft.search.ItemSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xft.search;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

public class ItemSearch implements SearchI {
	static org.apache.log4j.Logger logger = Logger.getLogger(ItemSearch.class);
	private UserI user = null;
	private GenericWrapperElement element = null;
	private CriteriaCollection criteriaCollection = new CriteriaCollection("AND");

	private boolean rootItem = true;
	private boolean extend = true;
	private String level = ViewManager.DEFAULT_LEVEL;
	private boolean allowMultiples = false;
    private boolean preventLoop = false;

	public String query = null;
	private boolean allowMultipleMatches=true;
	
    private static boolean ALLOW_OLD_SEARCH=true;

	public ItemSearch(){};

	/**
	 * Instanciate object with a user and the search element.
	 * @param u
	 * @param elementName
	 */
	public ItemSearch(UserI u, String elementName) throws ElementNotFoundException
	{
		user = u;
		this.setElement(elementName);
	}

	/**
	 * Instanciate object with a user and the search element.
	 * @param u
	 * @param e
	 */
	public ItemSearch(UserI u, GenericWrapperElement e)
	{
		user = u;
		element = e;
	}

	/**
	 * Instanciate object with a user, the search element, and a collection of search criteria.
	 * @param u
	 * @param elementName
	 * @param cc
	 */
	public ItemSearch(UserI u, String elementName, CriteriaCollection cc) throws ElementNotFoundException
	{
		user = u;
		this.setElement(elementName);
		criteriaCollection = cc;
	}

	/**
	 * Instanciate object with a user, the search element, and a collection of search criteria.
	 * @param u
	 * @param e
	 * @param cc
	 */
	public ItemSearch(UserI u, GenericWrapperElement e, CriteriaCollection cc)
	{
		user = u;
		element = e;
		criteriaCollection = cc;
	}

	/**
	 * Execute the search and return the matching XFTItems in an ItemCollection.
	 * Use the withChildren variable to specify whether or not the child items of the matching
	 * elements should be loaded.
	 * @param withChildren
	 * @return
	 * @throws Exception
	 */
	public ItemCollection exec(boolean withChildren) throws Exception
	{
	    this.setAllowMultiples(withChildren);
		return exec();
	}
//
//	/**
//	 * Execute the search and return the matching XFTItems in an ItemCollection.
//	 * Use the withChildren variable to specify whether or not the child items of the matching
//	 * elements should be loaded.
//	 * Use the loadHistory variable to specify if the XFT history of each item should be loaded.
//	 * @param withChildren
//	 * @param loadHistory
//	 * @return
//	 * @throws Exception
//	 */
//	public ItemCollection execute(boolean withChildren,boolean loadHistory,boolean extend) throws Exception
//	{
//	    TableSearch search = new TableSearch(user,element,criteriaCollection,withChildren);
//		XFTTable table = search.execute(loadHistory);
//		ItemCollection items = populateItems(element.getFullXMLName(),table,withChildren,loadHistory,extend);
//		return items;
//	}
//
	/**
	 * Execute the search and return the matching XFTItems in an ItemCollection.
	 * Use the withChildren variable to specify whether or not the child items of the matching
	 * elements should be loaded.
	 * @param withChildren
	 * @param extend
	 * @return
	 * @throws Exception
	 */
	public ItemCollection exec(boolean withChildren,boolean extend) throws IllegalAccessException,Exception
	{
	    this.setAllowMultiples(withChildren);
	    this.setExtend(extend);
		return exec();
	}

	public ItemCollection execute() throws IllegalAccessException,org.nrg.xft.exception.MetaDataException,Exception
	{
		QueryOrganizer qo = new QueryOrganizer(element,user,this.level);
	    Iterator iter = ViewManager.GetFieldNames(element,level,allowMultiples,rootItem).iterator();
	    while(iter.hasNext())
	    {
	        String s = (String)iter.next();
	        qo.addDirectField(s);
	    }

	    if (criteriaCollection != null && criteriaCollection.size() > 0)
	    {
		    qo.setWhere(criteriaCollection);
	    }

	    query = qo.buildQuery();

		String login = null;
		if (user != null)
		{
		    login = user.getUsername();
		}
//
//	    if (criteriaCollection != null && criteriaCollection.size() > 0)
//			query = "SELECT * FROM (" + query + ") SEARCH WHERE " + criteriaCollection.getSQLClause(qo);
//		query += ";";

	    XFTTable table = TableSearch.Execute(query,element.getDbName(),login);
	    ItemCollection items = populateItems(element,table,qo,extend,this.allowMultiples);
	    //items.finalizeLoading(); moved to populateItems
	    query = null;
		return items;
	}

	public XFTTable executeToTable(boolean showMetaFields) throws IllegalAccessException,org.nrg.xft.exception.MetaDataException,Exception{
		QueryOrganizer qo = new QueryOrganizer(element,user,this.level);
	    Hashtable<String,String> field_names = ViewManager.GetFieldMap(element,level,allowMultiples,rootItem);
	    for(String s: field_names.keySet())
	    {
	        if(showMetaFields || (s.indexOf("/meta/")==-1 && !s.endsWith("_info")))
	        	qo.addDirectField(s);
	    }


	    if (criteriaCollection != null && criteriaCollection.size() > 0)
	    {
		    qo.setWhere(criteriaCollection);
	    }

	    query = qo.buildQuery();

		String login = null;
		if (user != null)
		{
		    login = user.getUsername();
		}
//
//	    if (criteriaCollection != null && criteriaCollection.size() > 0)
//			query = "SELECT * FROM (" + query + ") SEARCH WHERE " + criteriaCollection.getSQLClause(qo);
//		query += ";";

	    XFTTable t= TableSearch.Execute(query,element.getDbName(),login);

	    Iterator possibleFieldNames = qo.getAllFields().iterator();
		while (possibleFieldNames.hasNext())
		{

		    //org.nrg.xft.XFT.LogCurrentTime("BEGIN SET PROPERTY::1");
			String key = (String)possibleFieldNames.next();
			//org.nrg.xft.XFT.LogCurrentTime("BEGIN SET PROPERTY::2");
		    String colName = (String)qo.translateXMLPath(key).toLowerCase();

		    Integer i = t.getColumnIndex(colName);
	    	if(i!=null){
	    		if(key.indexOf("/")==-1){
		    		t.getColumns()[i]=key;
	    		}else{
		    		t.getColumns()[i]=key.substring(key.indexOf("/")+1);
	    		}
	    	}
		}

	    return t;
	}

//    public List<Object> getKeys() throws IllegalAccessException,org.nrg.xft.exception.MetaDataException,Exception{
//        long startTime = Calendar.getInstance().getTimeInMillis();
//	    String login = null;
//		if (user != null)
//		{
//		    login = user.getUsername();
//		}
//
//		List<List<IdentifierResults>> matches = this.getIdentifierResults(login);
//        
//	    if (!allowMultipleMatches && matches.size()>1)
//	    {
//	        throw new MultipleMatchException();
//	    }
//
//		List<Object> al= new ArrayList<Object>();
//		for (List<IdentifierResults> item:matches)
//		{
//		    al.add(item.get(0).value);
//		}
//        return al;
//    }
    
    public List<List<IdentifierResults>> getIdentifiers() throws Exception{
        String login = null;
        if (user != null)
        {
            login = user.getUsername();
        }
        return getIdentifierResults(login);
    }
    
    public List<List<IdentifierResults>> getIdentifierResults(String login) throws Exception{
    	QueryOrganizer qo = new QueryOrganizer(element,null,this.level);
        ArrayList keys = element.getAllPrimaryKeys();
        Iterator keyIter = keys.iterator();
        String pk = null;
        while (keyIter.hasNext())
        {
            GenericWrapperField sf = (GenericWrapperField)keyIter.next();
            pk = sf.getXMLPathString(element.getXSIType());
            qo.addField(pk);
        }


        if (criteriaCollection != null && criteriaCollection.size() > 0)
        {
            Iterator iter = criteriaCollection.getSchemaFields().iterator();
            while(iter.hasNext())
            {
                Object[] o=(Object[])iter.next();
                String s = (String)o[0];
                qo.addField(s);
            }
        }

        query = qo.buildQuery();


        String distinct = "DISTINCT ON (";
        ArrayList<Object[]> keyColumns = new ArrayList<Object[]>();
        keyIter = keys.iterator();
        int count=0;
        while (keyIter.hasNext())
        {
            GenericWrapperField sf = (GenericWrapperField)keyIter.next();
            pk = sf.getXMLPathString(element.getXSIType());
            String colname =qo.translateXMLPath(pk);
            Object[] o = new Object[]{colname,sf};
            keyColumns.add(o);
            if (count++>0)distinct+=",";
            distinct+=colname;
        }
        distinct +=") ";

        if (criteriaCollection != null && criteriaCollection.size() > 0)
            query = "SELECT " + distinct +" * FROM (SELECT * FROM (" + query + ") SEARCH  WHERE " + criteriaCollection.getSQLClause(qo) + ") SEARCH";
        query += ";";


        XFTTable table= TableSearch.Execute(query,element.getDbName(),login);
        
        List<List<IdentifierResults>> items=new ArrayList<List<IdentifierResults>>();
                
	    table.resetRowCursor();   
		while (table.hasMoreRows())
		{
		    Hashtable row = table.nextRowHash();
			List<IdentifierResults> item=new ArrayList<IdentifierResults>();
		    Iterator iter = keyColumns.iterator();
		    count =0;
            String ids = "";
		    while (iter.hasNext())
		    {
		        Object[] key = (Object[])iter.next();
		        item.add(new IdentifierResults(row.get(((String)key[0]).toLowerCase()),(GenericWrapperField)key[1]));
		    }
		    items.add(item);
		}
        
        return items;
    }
    
    public String getFunctionName(){
    	String functionName= element.getTextFunctionName();
	    if ((!this.isExtend()) && (element.isExtended() && (!(element.getName().endsWith("meta_data")))))
	    {
	        functionName= GenericWrapperUtils.TXT_EXT_FUNCTION + element.getFormattedName();
	    }
	    return functionName;
    }

    public ItemCollection getItemsFromKeys(List<List<IdentifierResults>> matches, String login)throws IllegalAccessException,org.nrg.xft.exception.MetaDataException,Exception{
	    if (!allowMultipleMatches && matches.size()>1)
	    {
	        throw new MultipleMatchException();
	    }

	    long midTime = Calendar.getInstance().getTimeInMillis();

	    ItemCollection items = new ItemCollection();
	    final String functionName=getFunctionName();
	    
	    

        int loaded = 0;
        Exception ex = null;
		for (List<IdentifierResults> rows:matches)
		{
			String ids="";
		    query = "SELECT " + functionName + "(";
		    int count =0;
		    for (IdentifierResults ir:rows)
		    {
		        if (count++>0){
                    query+=", ";
                    ids +=",";
                }
                ids +=ir.value.toString();
		        query+=ir.getParsedValue();
		    }
		    if (allowMultiples)
		    {
			    query+=",0,TRUE,TRUE," + this.isPreventLoop() +")";
		    }else{
			    query+=",0,FALSE,TRUE," + this.isPreventLoop() +")";
		    }
            String s = null;
            if (allowMultiples && element.canBeRoot())
            {
                s=PoolDBUtils.RetrieveItemString(element.getFullXMLName(), ids, query, functionName, login);
            }else{
                long localTime = Calendar.getInstance().getTimeInMillis();
                s =(String)PoolDBUtils.ReturnStatisticQuery(query,functionName,element.getDbName(),login);
                //System.out.println((Calendar.getInstance().getTimeInMillis()-localTime) + "ms \tREALTIME:" + element.getFullXMLName() + ":" + ids);
            }
		    XFTItem item;
            try {
            	if(s!=null){
                    item = XFTItem.PopulateItemFromFlatString(s,user,allowMultiples);
                    item.removeEmptyItems();
                    if(item.hasProperties()){
                        items.add(item);
                        loaded++;
                    }
            	}
            } catch (IllegalAccessException e) {
                logger.error("",e);
                ex = e;
            }
		}

        if (loaded==0){
           if (ex!=null){
               throw ex;
           }
        }
	    query = null;
		return items;
    }

	public ItemCollection exec() throws IllegalAccessException,org.nrg.xft.exception.MetaDataException,Exception
	{
        if (!allowMultiples && ALLOW_OLD_SEARCH && !element.isExtended() && !element.getName().endsWith("_history")){
            return execute();
        }else{
            String login = null;
            if (user != null)
            {
                login = user.getUsername();
            }
            
            List<List<IdentifierResults>> matches=this.getIdentifierResults(login);
                	    
            return this.getItemsFromKeys(matches,login);
        }
	}


	/**
	 * Add a search criteria (org.nrg.xft.services.search.SearchCriteria) or a collection of criteria (org.nrg.xft.services.search.CriteriaCollection).
	 * @param c
	 */
	public void addCriteria(SQLClause c)
	{
		criteriaCollection.addClause(c);
	}

	/**
	 * Add a search criteria (org.nrg.xft.services.search.SearchCriteria) or a collection of criteria (org.nrg.xft.services.search.CriteriaCollection).
	 * @param c
	 */
	public void add(SQLClause c)
	{
		addCriteria(c);
	}

	/**
	 * Add a search criteria
	 * @param xmlPath ex. xnat:mrSessionData.scanner
	 * @param value SCANNER_NAME
	 * @param comparison (=, >, <, >=, <=, IS, IS NOT)
	 * @throws Exception
	 */
	public void addCriteria(String xmlPath, Object value, String comparison)throws Exception
	{
		SearchCriteria c = new SearchCriteria();
		c.setFieldWXMLPath(xmlPath);
		if (comparison.trim().equalsIgnoreCase("LIKE"))
		{
		    comparison = " LIKE ";
	        String temp = value.toString();
		    if (temp.startsWith("'"))
		    {
		        temp =temp.substring(1);
		    }
		    if (temp.endsWith("'"))
		    {
		        temp=temp.substring(0,temp.length()-1);
		    }

		    if (temp.indexOf("%") == -1)
		    {
		        temp = "%" + temp + "%";
		    }
		    
		    value = temp;
		}
		c.setValue(value);
		
		c.setComparison_type(comparison.trim());
		

		add(c);
	}

	/**
	 * Add a search criteria (comparison type is equals (default).
	 * @param xmlPath
	 * @param value
	 * @throws Exception
	 */
	public void addCriteria(String xmlPath, Object value)throws Exception
	{
		addCriteria(xmlPath,value,"=");
	}

	/**
	 * Add a search criteria using the specific Schema Field
	 * @param f
	 * @param value
	 * @param comparison
	 * @throws Exception
	 */
	public void addCriteria(GenericWrapperField f, Object value, String comparison)throws Exception
	{
		SearchCriteria c = new SearchCriteria();
		c.setField(f);
		c.setValue(value);
		c.setComparison_type(comparison);
		add(c);
	}

	/**
	 * Add a search criteria using the specific Schema Field
	 * @param f
	 * @param value
	 * @throws Exception
	 */
	public void addCriteria(GenericWrapperField f, Object value)throws Exception
	{
		addCriteria(f,value,"=");
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("ITEM SEARCH");
		if (getElement() != null)
			sb.append("\nELEMENT: ").append(this.getElement().getFullXMLName());
		else
			sb.append("\nELEMENT: NULL");
		if (getUser() != null)
			sb.append("\nUSER: ").append(this.getUser().getUsername());
		else
			sb.append("\nUSER: NULL");

		try {
			if (this.getCriteriaCollection() != null)
				sb.append("\nCRITERIA: ").append(this.getCriteriaCollection().getSQLClause(null));
			else
				sb.append("\nCRITERIA: NULL");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sb.toString();
	}


	/**
	 * translates a XFTTable into a list of items with all available sub-items populated.
	 * @param element
	 * @param table
	 * @param qo
	 * @param extend
	 * @param allowMultiples
	 * @return ItemCollection of XFTItems
	 */
	public ItemCollection populateItems(GenericWrapperElement element, XFTTable table,QueryOrganizer qo, boolean extend, boolean allowMultiples) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,IllegalAccessException,org.nrg.xft.exception.MetaDataException,Exception
	{
		ItemCollection items = new ItemCollection();

		ArrayList pksValues = new ArrayList();
		ArrayList objects = new ArrayList();

		if (!element.getName().endsWith("_meta_data"))
		    logger.debug("ItemSearch (" + element.getName() +") TABLE ROWS : " + table.size());

		//logger.debug("BEGIN POPULATE ITEMS");
		table.resetRowCursor();

		while (table.hasMoreRows())
		{
		    Hashtable row = table.nextRowHash();
			//org.nrg.xft.XFT.LogCurrentTime("BEGIN POPULATE ITEMS::1");
			XFTItem item = XFTItem.PopulateItemsFromQueryOrganizer(qo,element,new ArrayList(),row);
            if (this.allowMultiples)
            {
                item.setPreLoaded(true);
            }

            //org.nrg.xft.XFT.LogCurrentTime("BEGIN POPULATE ITEMS::2");

            Object pkValue = item.getPK();
            int index = Collections.binarySearch(pksValues,pkValue);
            if (index < 0)
            {
                //ITEM IS NEW
                index = (-index)-1;
                pksValues.add(index,pkValue);
                objects.add(index,item);
            }else{
                XFTItem previous = (XFTItem)objects.get(index);
                        //org.nrg.xft.XFT.LogCurrentTime("BEGIN POPULATE ITEMS::3");
                item = XFTItem.ReconcileItems(previous,item,this.allowMultiples);
                objects.remove(index);
                objects.add(index,item);
            }
		}

		items.addAll(objects);

		//logger.debug("ItemSearch ITEMS : " + items.size());
		//logger.debug("END POPULATE ITEMS");

		//logger.debug("BEGIN EXTENSIONS");
		if (extend)
		    items.extendAll(allowMultiples);
		//logger.debug("END EXTENSIONS");


        items.finalizeLoading();

		//logger.debug("BEGIN SECURITY CHECK");
		if (this.user !=null)
		    items.secureAllForRead(user);
		//logger.debug("END SECURITY CHECK");

		if (!element.getName().endsWith("_meta_data"))
		    logger.debug("ItemSearch ITEMS : " + items.size());

		return items;
	}
//
//	/**
//	 * translates a XFTTable into a list of items with all available sub-items populated.
//	 * @param name of schema element
//	 * @return ItemCollection of XFTItems
//	 */
//	public ItemCollection populateItems(String name, XFTTable table,boolean withChildren,boolean loadHistory,boolean extend) throws ElementNotFoundException,XFTInitException,FieldNotFoundException,Exception
//	{
//		org.nrg.xft.XFT.LogCurrentTime("BEGIN POPULATE ITEMS " + name);
//		ItemCollection items = new ItemCollection();
//
//		ArrayList temp = new ArrayList();
//
//		table.resetRowCursor();
//
//		while (table.hasMoreRows())
//		{
//			Object[] row = table.nextRow();
//			//org.nrg.xft.XFT.LogCurrentTime("BEGIN POPULATE ITEMS::1");
//			XFTItem item = XFTItem.PopulateItemsFromObjectArray(row,table.getColumnNumberHash(),name,"",new ArrayList(),withChildren,loadHistory);
//
//			//org.nrg.xft.XFT.LogCurrentTime("BEGIN POPULATE ITEMS::2");
//			Iterator iter = temp.iterator();
//			int itemCounter = 0;
//			while (iter.hasNext())
//			{
//			    XFTItem previous = (XFTItem)iter.next();
//			    if (XFTItem.CompareItemsByPKs(previous,item))
//			    {
//			       // org.nrg.xft.XFT.LogCurrentTime("BEGIN POPULATE ITEMS :: RECONCILE::2");
//			        item = XFTItem.ReconcileItems(previous,item,withChildren);
//			        temp.remove(itemCounter);
//			        break;
//			    }
//			    itemCounter++;
//			}
//			temp.add(item);
//			//org.nrg.xft.XFT.LogCurrentTime("BEGIN POPULATE ITEMS::3");
//		}
//
//		items.addAll(temp);
//
//		org.nrg.xft.XFT.LogCurrentTime("END POPULATE ITEMS");
//
//		org.nrg.xft.XFT.LogCurrentTime("BEGIN EXTENSIONS");
//		if (withChildren && extend)
//		    items.extendAll();
//		org.nrg.xft.XFT.LogCurrentTime("END EXTENSIONS");
//
//		org.nrg.xft.XFT.LogCurrentTime("BEGIN SECURITY CHECK");
//		if (this.user !=null)
//		    items.secureAllForRead(user);
//		org.nrg.xft.XFT.LogCurrentTime("END SECURITY CHECK");
//
//		return items;
//	}

	/**
	 * @return Returns the criteriaCollection.
	 */
	public CriteriaCollection getCriteriaCollection() {
		return criteriaCollection;
	}
	/**
	 * @param criteriaCollection The criteriaCollection to set.
	 */
	public void setCriteriaCollection(CriteriaCollection criteriaCollection) {
		this.criteriaCollection = criteriaCollection;
	}
	/**
	 * @return Returns the element.
	 */
	public GenericWrapperElement getElement() {
		return element;
	}
	/**
	 * @param element The element to set.
	 */
	public void setElement(GenericWrapperElement element) {
		this.element = element;
	}
	/**
	 * @param elementName The element to set.
	 */
	public void setElement(String elementName) throws ElementNotFoundException {
		try {
            this.element = GenericWrapperElement.GetElement(elementName);
        } catch (XFTInitException e) {
            logger.error("",e);
        }
	}
	/**
	 * @return Returns the user.
	 */
	public UserI getUser() {
		return user;
	}
	/**
	 * @param user The user to set.
	 */
	public void setUser(UserI user) {
		this.user = user;
	}


	/**
	 * Pass in a String identifying the XML field which you are searching by: ex. xdat:mrSessionData.scanner
	 * with a value and a UserI object.  The matching XFTItems will be returned in a org.nrg.xft.ItemCollection.
	 * @param xmlPath
	 * @param v
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public static ItemCollection GetItems(String xmlPath,String comparisonType, Object v, UserI user, boolean preLoad) throws Exception
	{
	    xmlPath = StringUtils.StandardizeXMLPath(xmlPath);
	    String rootElement = StringUtils.GetRootElementName(xmlPath);
	    ItemSearch search = GetItemSearch(rootElement,user);
	    search.setAllowMultiples(preLoad);
	    search.addCriteria(xmlPath,v,comparisonType);
	    return search.exec(preLoad);
	}

	/**
	 * Pass in a String identifying the XML field which you are searching by: ex. xdat:mrSessionData.scanner
	 * with a value and a UserI object.  The matching XFTItems will be returned in a org.nrg.xft.ItemCollection.
	 * @param xmlPath
	 * @param v
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public static ItemCollection GetItems(String xmlPath,Object v, UserI user, boolean preLoad) throws Exception
	{
	    xmlPath = StringUtils.StandardizeXMLPath(xmlPath);
	    String rootElement = StringUtils.GetRootElementName(xmlPath);
	    ItemSearch search = GetItemSearch(rootElement,user);
	    search.setAllowMultiples(preLoad);
	    search.addCriteria(xmlPath,v);
	    return search.exec(preLoad);
	}

	/**
	 * Pass in a String identifying the XML field which you are searching by: ex. xdat:mrSessionData.scanner
	 * with a value and a UserI object.  The first matching XFTItem will be returned.
	 * @param xmlPath
	 * @param v
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public static XFTItem GetItem(String xmlPath,Object v, UserI user,boolean preLoad) throws Exception
	{
	    xmlPath = StringUtils.StandardizeXMLPath(xmlPath);
	    ItemCollection items = ItemSearch.GetItems(xmlPath,v,user,preLoad);
	    if (items.size()>0)
	    {
	        return (XFTItem)items.first();
	    }else{
	        return null;
	    }
	}

    /**
     * Gets the matching items based on the specified criteria.
     * @param cc
     * @param user
     * @return
     * @throws Exception
     */
    public static ItemCollection GetItems(String elementName, CriteriaCollection cc, UserI user,boolean preLoad) throws Exception
    {
        if (cc.size() > 0)
        {
            ItemSearch search = GetItemSearch(elementName,user);
            search.setCriteriaCollection(cc);
            return search.exec(preLoad);
        }else{
            return new ItemCollection();
        }
    }

	/**
	 * Gets the matching items based on the specified criteria.
	 * @param cc
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public static ItemCollection GetItems(CriteriaCollection cc, UserI user,boolean preLoad) throws Exception
	{
		if(cc.toArrayList().size()==0){
			return new ItemCollection();
		}
        SQLClause clause = (SQLClause)cc.toArrayList().get(0);
		try {
	    return GetItems(clause.getElementName(),cc,user,preLoad);
		} catch (IllegalAccessException e) {
			logger.warn("Got an illegal access exception: \"" + e.getMessage() + "\". Returning empty item collection.");
			return new ItemCollection();
		}
	}

	/**
	 * Get an empty ItemSearch object for the specified data type.
	 * @param elementName
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public static ItemSearch GetItemSearch(String elementName,UserI user) throws Exception
	{
	    GenericWrapperElement gwe = GenericWrapperElement.GetElement(elementName);
	    return new ItemSearch(user,gwe);
	}


	/**
	 * Get all XFTItems of the specified data type.
	 * @param elementName
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public static ItemCollection GetAllItems(String elementName,UserI user,boolean preLoad) throws Exception
	{
	    GenericWrapperElement gwe = GenericWrapperElement.GetElement(elementName);
	    ItemSearch search = new ItemSearch(user,gwe);
	    ItemCollection items = search.exec(preLoad);
	    return items;
	}

    /**
     * @return Returns the allowMultiples.
     */
    public boolean isAllowMultiples() {
        return allowMultiples;
    }
    /**
     * @param allowMultiples The allowMultiples to set.
     */
    public void setAllowMultiples(boolean allowMultiples) {
        this.allowMultiples = allowMultiples;
    }
    /**
     * @return Returns the level.
     */
    public String getLevel() {
        return level;
    }
    /**
     * @param level The level to set. ('active', 'quarantine', or 'all')
     */
    public void setLevel(String level) {
        this.level = level;
    }

    public void onlyActiveData()
    {
        this.level = ViewManager.ACTIVE;
    }

    public void allData()
    {
        this.level = ViewManager.QUARANTINE;
    }
    /**
     * @return Returns the extend.
     */
    public boolean isExtend() {
        return extend;
    }
    /**
     * @param extend The extend to set.
     */
    public void setExtend(boolean extend) {
        this.extend = extend;
    }
    /**
     * @return Returns the rootItem.
     */
    public boolean isRootItem() {
        return rootItem;
    }
    /**
     * @param rootItem The rootItem to set.
     */
    public void setRootItem(boolean rootItem) {
        this.rootItem = rootItem;
    }
    /**
     * @return Returns the allowMultipleMatches.
     */
    public boolean isAllowMultipleMatches() {
        return allowMultipleMatches;
    }
    /**
     * @param allowMultipleMatches The allowMultipleMatches to set.
     */
    public void setAllowMultipleMatches(boolean allowMultipleMatches) {
        this.allowMultipleMatches = allowMultipleMatches;
    }

    @SuppressWarnings("serial")
    public class MultipleMatchException extends Exception{

        public MultipleMatchException() {
            super("Query returned multiple matches.");
        }

    }

    /**
     * @return the preventLoop
     */
    public boolean isPreventLoop() {
        return preventLoop;
    }

    /**
     * @param preventLoop the preventLoop to set
     */
    public void setPreventLoop(boolean preventLoop) {
        this.preventLoop = preventLoop;
    }
    
    public static class IdentifierResults{
    	public GenericWrapperField field=null;
    	public Object value;
    	public String type=null;
    	
    	public IdentifierResults(Object v,GenericWrapperField t){
    		field=t;
    		value=v;
    	}
    	
    	public IdentifierResults(Object v,String t){
    		value=v;
    		type=t;
    	}
    	
    	public String getParsedValue()throws XFTInitException,InvalidValueException{
    		if(field!=null){
				return DBAction.ValueParser(value,field,false);
    		}else{
    			return DBAction.ValueParser(value, type,false);
    		}
    	}
    }
}

