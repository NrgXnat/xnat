/*
 * core: org.nrg.xft.search.ItemSearch
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.search;

import com.google.common.collect.Maps;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.python.apache.commons.compress.utils.Lists;

import java.sql.SQLException;
import java.sql.Types;
import java.util.*;

@Slf4j
public class ItemSearch implements SearchI {
	public static final String $_USER_ID = "${USER_ID}";
	public static final String VAR_OPEN = "${";
	public static final String SEARCH_SEQ = "}";
	public static final String QUESTION = "?";
	private UserI                  user               = null;
	private GenericWrapperElement  element            = null;
	private CriteriaCollection     criteriaCollection = new CriteriaCollection("AND");

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
	 * @return Returns a collection with the results of the search
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
	 * @return Returns a collection with the results of the search
	 * @throws Exception
	 */
	public ItemCollection exec(boolean withChildren,boolean extend) throws IllegalAccessException,Exception
	{
	    this.setAllowMultiples(withChildren);
	    this.setExtend(extend);
		return exec();
	}

	private ExecutionResult internalExeToTable(Boolean showMetaFields) throws Exception 
    {
		QueryOrganizer qo = new QueryOrganizer(element,user,this.level);
        for (String fieldName : ViewManager.GetFieldNames(element, level, allowMultiples, rootItem)) {
			if(showMetaFields==null){
				qo.addDirectField(fieldName);
			}else{
				if(showMetaFields || (!fieldName.contains("/meta/") && !fieldName.endsWith("_info")))
					qo.addDirectField(fieldName);
			}
		}

		SQLClause.ValueTracker mapper = new SQLClause.ValueTracker();

		if (criteriaCollection != null && criteriaCollection.size() > 0)
	    {
			qo.setWhere(criteriaCollection);
		}

		if (criteriaCollection != null && criteriaCollection.size() > 0)
	    {
			criteriaCollection = (CriteriaCollection) criteriaCollection.templatizeQuery(mapper);
		}

        query = qo.buildQuery();

		String login = null;
		if (user != null)
		{
		    login = user.getUsername();
		}


		if(user!=null){
			mapper.getValues().put($_USER_ID, new SQLClause.ParamValue(user.getID(), Types.INTEGER));
		}

		//build ordered list of parameters for the Prepared Statement
		List<SQLClause.ParamValue> params= Lists.newArrayList();
		int lastVarIndex = 0;
		while((StringUtils.indexOf(query, VAR_OPEN,lastVarIndex))> -1){
			lastVarIndex=StringUtils.indexOf(query, VAR_OPEN,lastVarIndex);
			final String var = StringUtils.substring(query,lastVarIndex,StringUtils.indexOf(query,SEARCH_SEQ,lastVarIndex)+1);
			params.add(mapper.getValues().get(var));
			query=StringUtils.replaceOnce(query,var,QUESTION);
		}

		XFTTable table=XFTTable.ExecutePS(query, params.toArray(new SQLClause.ParamValue[0]));
		return new ExecutionResult(login, qo,table);
	}

	public XFTTable executeToTable(boolean showMetaFields) throws IllegalAccessException,org.nrg.xft.exception.MetaDataException,Exception{
		final ExecutionResult result=internalExeToTable(showMetaFields);

		for (final Object o : result.qo.getAllFields()) {
			final String key = (String) o;
			final String colName = result.qo.translateXMLPath(key).toLowerCase();

			final Integer i = result.table.getColumnIndex(colName);
			if (i != null) {
				result.table.getColumns()[i] = !key.contains("/") ? key : key.substring(key.indexOf("/") + 1);
			}
		}

		return result.table;
	}
    
    public List<List<IdentifierResults>> getIdentifiers() throws Exception{
        String login = null;
        if (user != null)
        {
            login = user.getUsername();
        }
        return getIdentifierResults(login);
    }
    
    private List<List<IdentifierResults>> getIdentifierResults(String login) throws Exception{
		SQLClause.ValueTracker mapper = new SQLClause.ValueTracker();

		if (criteriaCollection != null && criteriaCollection.size() > 0) {
			criteriaCollection = (CriteriaCollection) criteriaCollection.templatizeQuery(mapper);
		}


			QueryOrganizer qo = new QueryOrganizer(element,null,this.level);
			ArrayList keys = element.getAllPrimaryKeys();
			Iterator keyIter = keys.iterator();
			String pk = null;
            while (keyIter.hasNext())
            {
				GenericWrapperField sf = (GenericWrapperField) keyIter.next();
				pk = sf.getXMLPathString(element.getXSIType());
				qo.addField(pk);
			}


            if (criteriaCollection != null && criteriaCollection.size() > 0)
            {
                Iterator iter = criteriaCollection.getSchemaFields().iterator();
                while(iter.hasNext())
                {
					Object[] o = (Object[]) iter.next();
					String s = (String) o[0];
					qo.addField(s);
				}
			}

			query = qo.buildQuery();


			String distinct = "DISTINCT ON (";
			ArrayList<Object[]> keyColumns = new ArrayList<Object[]>();
			keyIter = keys.iterator();
			int count = 0;
            while (keyIter.hasNext())
            {
				GenericWrapperField sf = (GenericWrapperField) keyIter.next();
				pk = sf.getXMLPathString(element.getXSIType());
				String colname = qo.translateXMLPath(pk);
				Object[] o = new Object[]{colname, sf};
				keyColumns.add(o);
				if (count++ > 0) distinct += ",";
				distinct += colname;
			}
			distinct += ") ";

			if (criteriaCollection != null && criteriaCollection.size() > 0)
				query = "SELECT " + distinct + " * FROM (SELECT * FROM (" + query + ") SEARCH  WHERE " + criteriaCollection.getSQLClause(qo) + ") SEARCH";
			query += ";";



		if(user!=null){
			mapper.getValues().put($_USER_ID, new SQLClause.ParamValue(user.getID(), Types.INTEGER));
		}

		//build ordered list of parameters for the Prepared Statement
		List<SQLClause.ParamValue> params= Lists.newArrayList();
		int lastVarIndex = 0;
		while((StringUtils.indexOf(query, VAR_OPEN,lastVarIndex))> -1){
			lastVarIndex=StringUtils.indexOf(query, VAR_OPEN,lastVarIndex);
			final String var = StringUtils.substring(query,lastVarIndex,StringUtils.indexOf(query,SEARCH_SEQ,lastVarIndex)+1);
			params.add(mapper.getValues().get(var));
			query=StringUtils.replaceOnce(query,var,QUESTION);
		}

		XFTTable table=XFTTable.ExecutePS(query, params.toArray(new SQLClause.ParamValue[0]));
        
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

	public ItemCollection execute() throws Exception
	{
		final ExecutionResult result=internalExeToTable(null);

		final ItemCollection items = populateItems(element, result.table, result.qo, extend, this.allowMultiples);
		log.debug("Got {} results for user '{}' query '{}'", items.size(), StringUtils.defaultIfBlank(result.login, "<no user>"), query);
		query = null;
		return items;
	}

	private class ExecutionResult{
		public QueryOrganizer qo;
		public XFTTable table;
		public String login;

		public ExecutionResult(String login, QueryOrganizer qo, XFTTable table){
			this.login=login;
			this.qo=qo;
			this.table=table;
		}
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
			StringBuilder ids = new StringBuilder();
		    query = "SELECT " + functionName + "(";
		    int count =0;
		    for (IdentifierResults ir:rows)
		    {
		        if (count++>0){
                    query+=", ";
                    ids.append(",");
                }
                ids.append(ir.value.toString());
		        query+=ir.getParsedValue();
		    }
			query += ",0,";
			query += allowMultiples ? "TRUE" : "FALSE";
			query += ",TRUE," + this.isPreventLoop() + ")";

			final String flatString = allowMultiples && element.canBeRoot()
									  ? PoolDBUtils.RetrieveItemString(element.getFullXMLName(), ids.toString(), query, functionName, login)
									  : (String) PoolDBUtils.ReturnStatisticQuery(query, functionName, element.getDbName(), login);
			XFTItem item;
			try {
				if (StringUtils.isNotBlank(flatString)) {
					log.debug("Preparing to populate item from flat string: {}", flatString);
					item = XFTItem.PopulateItemFromFlatString(flatString, user, allowMultiples);
					item.removeEmptyItems();
					if (item.hasProperties()) {
						items.add(item);
						loaded++;
					}
				}
			} catch (IllegalAccessException e) {
				log.error("", e);
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

		private boolean instanceOf(final GenericWrapperElement gwe, final String dataType){
			return (StringUtils.equalsIgnoreCase(gwe.getFullXMLName(),dataType) || gwe.instanceOf(dataType));
		}

		@Nullable
		private List<List<IdentifierResults>> getIdMatch(final String query, SQLClause.ParamValue... params) throws DBPoolException, SQLException {
			final XFTTable table = XFTTable.ExecutePS(query, params);
			if (table.getNumRows() > 0 && table.getNumCols()>0) {
				final List<IdentifierResults> inner = new ArrayList<IdentifierResults>();
				inner.add(new IdentifierResults(table.rows().get(0)[0], "string"));
				final List<List<IdentifierResults>> matches = Lists.newArrayList();
				matches.add(inner);
				return matches;
			}
			return null;
		}

		public enum PROJECT_MATCH{
			NONE,
			PRIMARY,
			SHARED
		}

		private PROJECT_MATCH isLabelSearch(CriteriaCollection cc){
			if(cc.size()!=2){
				return PROJECT_MATCH.NONE;
			}

			if((cc.containsXMLPathEndingWith("/project") && cc.containsXMLPathEndingWith("/label"))){
				if((cc.containsXMLPathEndingWith("/sharing/share/project") && cc.containsXMLPathEndingWith("/sharing/share/label"))){
					return PROJECT_MATCH.SHARED;
				}else{
					return PROJECT_MATCH.PRIMARY;
				}
			}

			return PROJECT_MATCH.NONE;
		}


	public ItemCollection exec() throws Exception {
		String login = null;
		if (user != null) {
			login = user.getUsername();
		}

		List<List<IdentifierResults>> matches = null;
		final PROJECT_MATCH isLabelSearch= isLabelSearch(this.getCriteriaCollection());

		if (instanceOf(getElement(),"xnat:experimentData")){
			//streamline experiment query if possible
			if (this.getCriteriaCollection().size() == 1 &&
					this.getCriteriaCollection().containsXMLPathEndingWith("/ID")) {
				//searching by ID
				final String query = "SELECT ID FROM xnat_experimentData WHERE ID=?";
				final Object id = this.getCriteriaCollection().getValueEndingWith("/ID");
				matches= getIdMatch(query,new SQLClause.ParamValue(id, Types.VARCHAR));
			} else if(isLabelSearch==PROJECT_MATCH.PRIMARY){
				final String query = "SELECT ID FROM xnat_experimentData WHERE project=? AND label=?";
				final Object project = getCriteriaCollection().getValueEndingWith("/project");
				final Object label = getCriteriaCollection().getValueEndingWith("/label");

				matches= getIdMatch(query,new SQLClause.ParamValue(project, Types.VARCHAR),new SQLClause.ParamValue(label, Types.VARCHAR));
			}else if(isLabelSearch==PROJECT_MATCH.SHARED){
				final String query = "SELECT sharing_share_xnat_experimentda_id AS ID FROM xnat_experimentData_share WHERE project=? AND label=?";
				final Object project = getCriteriaCollection().getValueEndingWith("/project");
				final Object label = getCriteriaCollection().getValueEndingWith("/label");

				matches= getIdMatch(query,new SQLClause.ParamValue(project, Types.VARCHAR),new SQLClause.ParamValue(label, Types.VARCHAR));
			}else {
				matches = this.getIdentifierResults(login);
			}
		}else	if (instanceOf(getElement(),"xnat:subjectData")) {
			if (this.getCriteriaCollection().size() == 1 &&
					this.getCriteriaCollection().toArrayList().get(0) instanceof SearchCriteria &&
					"xnat:subjectData/ID".equals(((SearchCriteria) this.getCriteriaCollection().toArrayList().get(0)).getXMLPath())) {
				String query = "SELECT ID FROM xnat_subjectData WHERE ID=?";
				String id = (String) ((SearchCriteria) this.getCriteriaCollection().toArrayList().get(0)).getValue();
				matches= getIdMatch(query,new SQLClause.ParamValue(id, Types.VARCHAR));
			}else if(isLabelSearch==PROJECT_MATCH.PRIMARY){
				final String query = "SELECT ID FROM xnat_subjectData WHERE project=? AND label=?";
				final Object project = getCriteriaCollection().getValueEndingWith("/project");
				final Object label = getCriteriaCollection().getValueEndingWith("/label");

				matches= getIdMatch(query,new SQLClause.ParamValue(project, Types.VARCHAR),new SQLClause.ParamValue(label, Types.VARCHAR));
			}else if(isLabelSearch==PROJECT_MATCH.SHARED){
				final String query = "SELECT subject_id AS ID FROM xnat_projectParticipant WHERE project=? AND label=?";
				final Object project = getCriteriaCollection().getValueEndingWith("/project");
				final Object label = getCriteriaCollection().getValueEndingWith("/label");

				matches= getIdMatch(query,new SQLClause.ParamValue(project, Types.VARCHAR),new SQLClause.ParamValue(label, Types.VARCHAR));
			}else {
				matches = this.getIdentifierResults(login);
			}
		} else {
			matches = this.getIdentifierResults(login);
		}


		if (matches == null || matches.size() == 0) {
			return new ItemCollection();
		} else {
			return this.getItemsFromKeys(matches, login);
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
	 * @param comparison (=, &gt;, &lt;, &gt;=, &lt;=, IS, IS NOT)
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
		    log.debug("ItemSearch (" + element.getName() + ") TABLE ROWS : " + table.size());

		//log.debug("BEGIN POPULATE ITEMS");
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
            if (pkValue == null || pksValues == null) {
                continue;
            }
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

		//log.debug("ItemSearch ITEMS : " + items.size());
		//log.debug("END POPULATE ITEMS");

		//log.debug("BEGIN EXTENSIONS");
		if (extend)
		    items.extendAll(allowMultiples);
		//log.debug("END EXTENSIONS");


        items.finalizeLoading();

		//log.debug("BEGIN SECURITY CHECK");
		if (this.user !=null)
		    items.secureAllForRead(user);
		//log.debug("END SECURITY CHECK");

		if (!element.getName().endsWith("_meta_data"))
		    log.debug("ItemSearch ITEMS : " + items.size());

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
            log.error("", e);
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
	 * @return Returns a collections of the XFTItems matching specified criteria
	 * @throws Exception
	 */
	public static ItemCollection GetItems(String xmlPath,String comparisonType, Object v, UserI user, boolean preLoad) throws Exception
	{
	    xmlPath = XftStringUtils.StandardizeXMLPath(xmlPath);
	    String rootElement = XftStringUtils.GetRootElementName(xmlPath);
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
	 * @return Returns collection of XFTItems matching specified criteria
	 * @throws Exception
	 */
	public static ItemCollection GetItems(String xmlPath,Object v, UserI user, boolean preLoad) throws Exception
	{
	    xmlPath = XftStringUtils.StandardizeXMLPath(xmlPath);
	    String rootElement = XftStringUtils.GetRootElementName(xmlPath);
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
	 * @return Returns first XFTItem matching specified criteria
	 * @throws Exception
	 */
	public static XFTItem GetItem(String xmlPath,Object v, UserI user,boolean preLoad) throws Exception
	{
	    xmlPath = XftStringUtils.StandardizeXMLPath(xmlPath);
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
     * @return Returns collection of items matching specified criteria
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
	 * @return Returns collection of items matching specified criteria
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
			log.warn("Got an illegal access exception: \"" + e.getMessage() + "\". Returning empty item collection.");
			return new ItemCollection();
		}
	}

	/**
	 * Get an empty ItemSearch object for the specified data type.
	 * @param elementName
	 * @param user
	 * @return Returns an empty ItemSearch object for the specified data type
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
	 * @return Returns collection of all items
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

