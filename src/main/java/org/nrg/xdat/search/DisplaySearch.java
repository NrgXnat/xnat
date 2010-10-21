//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 3, 2005
 *
 */
package org.nrg.xdat.search;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.collections.DisplayFieldCollection.DisplayFieldNotFoundException;
import org.nrg.xdat.collections.DisplayFieldWrapperCollection;
import org.nrg.xdat.display.ArcDefinition;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayFieldElement;
import org.nrg.xdat.display.DisplayFieldRef;
import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.DisplayVersion;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.display.SQLQueryField;
import org.nrg.xdat.exceptions.IllegalAccessException;
import org.nrg.xdat.om.XdatCriteria;
import org.nrg.xdat.om.XdatCriteriaSet;
import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.presentation.PresentationA;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.XFTTableI;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.DBPoolException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.SQLClause;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.search.TableSearchI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;
/**
 * @author Tim
 *
 */
public class DisplaySearch implements TableSearchI{
	static org.apache.log4j.Logger logger = Logger.getLogger(DisplaySearch.class);
	private SchemaElement rootElement = null;
	private String display = "default";
	private ArrayList additionalViews = new ArrayList();//String[2]{elementName,display}
	private String sortBy = "";
	private String sortOrder = "";
	private String customSortBy = "";
	private String description = "";
	private boolean useVersions = false;

	public String resultsTableName =null;

	private String title = "";

	private DisplayFieldWrapperCollection fields = new DisplayFieldWrapperCollection();

	private Hashtable<String,String> inClauses = new Hashtable<String,String>();

	private org.nrg.xft.search.CriteriaCollection criteria = new org.nrg.xft.search.CriteriaCollection("OR");

	private boolean pagingOn = false;
	private int currentPageNum = 0;
	private int rowsPerPage = 30;
	private int numRows = 0;
	private int pages = 1;

	private boolean newQuery = true;

	public boolean allowDiffs= true;

	private XFTTableI table = null;
	private XFTTableI presentedTable = null;
	private PresentationA lastPresenter = null;

	private XDATUser user = null;

	private String level = ViewManager.DEFAULT_LEVEL;

	private Hashtable isMultipleRelationship = new Hashtable();

	private Hashtable webFormValues = new Hashtable();

	String query = "";

	private boolean isStoredSearch = false;
	private org.nrg.xdat.om.XdatStoredSearch storedSearch= null;

	public DisplaySearch(){}


	/**
	 * @param presenter
	 * @return
	 */
	public XFTTableI execute(PresentationA presenter, String login) throws XFTInitException,ElementNotFoundException,DBPoolException,SQLException,IllegalAccessException,Exception
	{
//		if (user == null)
//		{
//			throw new IllegalAccessException("Search requires a logged-in user account.");
//		}
		lastPresenter = presenter;

		query = this.getSQLQuery(presenter);
		resetResultsTableName();

		String db = rootElement.getGenericXFTElement().getDbName();
		if (pagingOn)
		{
			query = StringUtils.ReplaceStr(query,"'","*'*");
			query = StringUtils.ReplaceStr(query,"*'*","''");

			Long count = PoolDBUtils.CreateManagedTempTable(this.getResultsTableName(), query, user);
			try {

				if (count.intValue()==0)
				{
					table = new XFTTable();
					presentedTable = new XFTTable();
				}else{
					currentPageNum = 0;
					int offset = currentPageNum * rowsPerPage;
					if (offset < count.intValue())
					{
						table = PoolDBUtils.RetrieveManagedTempTable(this.getResultsTableName(), user, offset,rowsPerPage);
						this.numRows = Integer.valueOf(count.toString()).intValue();
						calculatePages();
						newQuery=false;
					}else{
						table = new XFTTable();
						presentedTable = new XFTTable();
						throw new Exception("Index out of bounds: Index:" + offset + " Rows:"+ count);
					}
				}
			} catch (RuntimeException e) {
				table = new XFTTable();
				presentedTable = new XFTTable();
				throw e;
			}
		}else{
			table = TableSearch.Execute(query,db,login);
		}

        //logger.debug("BEGIN FORMAT FOR PRESENTATION");
		if (presenter != null)
		{
			presenter.setRootElement(rootElement);
			presenter.setDisplay(display);
			presenter.setAdditionalViews(additionalViews);
			presentedTable = presenter.formatTable(table,this,this.allowDiffs);
		}else{
			presentedTable = table;
		}
		//logger.debug("END FORMAT FOR PRESENTATION");
		return presentedTable;
	}

	public Long createSearchCache(PresentationA presenter, String login) throws XFTInitException,ElementNotFoundException,DBPoolException,SQLException,IllegalAccessException,Exception{
		lastPresenter = presenter;

		query = this.getSQLQuery(presenter);
		resetResultsTableName();

		String db = rootElement.getGenericXFTElement().getDbName();
		query = StringUtils.ReplaceStr(query,"'","*'*");
		query = StringUtils.ReplaceStr(query,"*'*","''");

		return PoolDBUtils.CreateManagedTempTable(this.getResultsTableName(), query, user);

	}


    public void clearTables(){
        presentedTable=null;

        table=null;
    }

    private boolean hasSchemaOnlyCriteria(){
        if(this.criteria!=null && this.criteria.numClauses()>0 && this.criteria.numClauses()==this.criteria.numSchemaClauses()){
            return true;
        }else{
            return false;
        }
    }

	public String getSQLQuery(PresentationA presenter) throws Exception
	{
		ArrayList displayFields = new ArrayList();

		ElementDisplay ed = DisplayManager.GetElementDisplay(rootElement.getFullXMLName());
		DisplayVersion dv = null;
		if (this.getFields().size()==0)
		{
		    useVersions = true;
			if (presenter != null && !presenter.getVersionExtension().equalsIgnoreCase(""))
			{
				dv = ed.getVersion(display+ "_" + presenter.getVersionExtension(),display);
			}else
			{
				dv = ed.getVersion(display,"default");
			}

			displayFields.addAll(dv.getSortedDisplayFieldRefs());

			if (additionalViews != null && additionalViews.size() > 0)
			{
			    Iterator keys = additionalViews.iterator();
				while (keys.hasNext())
				{
				    String[] key = (String[])keys.next();
					String elementName = key[0];
					String version = key[1];
					SchemaElementI foreign = SchemaElement.GetElement(elementName);

					ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getFullXMLName());
					DisplayVersion foreignDV = null;
					if (presenter != null && !presenter.getVersionExtension().equalsIgnoreCase(""))
					{
						foreignDV = foreignEd.getVersion(version+ "_" + presenter.getVersionExtension(),version);
					}else
					{
						foreignDV = foreignEd.getVersion(version,"default");
					}
					displayFields.addAll(foreignDV.getSortedDisplayFieldRefs());
				}
			}

			this.setAllowDiffs(dv.isAllowDiffs());

			if (this.inClauses.size()>0)
			{
				for (Map.Entry<String, String> entry:inClauses.entrySet())
			    {
			        for (String key:StringUtils.CommaDelimitedStringToArrayList(entry.getKey()))
			        {
				        try {
				        	DisplayField df=DisplayField.getDisplayFieldForUnknownPath(key);
			        					        	
				        	if(df!=null){
		                        DisplayFieldReferenceI ref = new DisplayFieldWrapper(df);

		                        boolean found = false;

		                        Iterator dfs = displayFields.iterator();
		                        while (dfs.hasNext())
		                        {
		                            DisplayFieldReferenceI temp = (DisplayFieldReferenceI)dfs.next();
		                            if (temp.getId().equalsIgnoreCase(df.getId()) && temp.getElementName().equalsIgnoreCase(ref.getElementName()))
		                            {
		                                found = true;
		                                break;
		                            }
		                        }

		                        if (!found)
		                        {
		                            displayFields.add(ref);
		                        }
				        	}
				        }catch(Exception e)
				        {}
			        }
			    }
			}
		}else{
		    useVersions=false;
		    displayFields.addAll(this.getFields().getSortedFields());

		    if (this.inClauses.size()>0)
			{
		    	for (Map.Entry<String, String> entry:inClauses.entrySet())
			    {
			        for (String key:StringUtils.CommaDelimitedStringToArrayList(entry.getKey()))
			        {
				        try {
				        	DisplayField df=DisplayField.getDisplayFieldForUnknownPath(key);
			        	
				        	if(df!=null){
		                        DisplayFieldReferenceI ref = new DisplayFieldWrapper(df);

		                        boolean found = false;

		                        Iterator dfs = displayFields.iterator();
		                        while (dfs.hasNext())
		                        {
		                            DisplayFieldReferenceI temp = (DisplayFieldReferenceI)dfs.next();
		                            if (temp.getId().equalsIgnoreCase(df.getId()) && temp.getElementName().equalsIgnoreCase(ref.getElementName()))
		                            {
		                                found = true;
		                                break;
		                            }
		                        }

		                        if (!found)
		                        {
		                            displayFields.add(ref);
		                        }
				        	}
				        }catch(Exception e)
				        {}
			        }
			    }
			}
		}
		
		Iterator iter = displayFields.iterator();
		while(iter.hasNext())
		{
		   DisplayFieldReferenceI dfw = (DisplayFieldReferenceI) iter.next();
		   try {
		       DisplayField df=dfw.getDisplayField();
		       if(df instanceof SQLQueryField){
		    	   if(dfw.getValue().equals("{XDAT_USER_ID}")){
		    		   dfw.setValue(user.getXdatUserId());
		    	   }
		       }
		   }catch(Exception e){
			   
		   }
		}

		StringBuffer sb = new StringBuffer();
		StringBuffer where = new StringBuffer();
		StringBuffer join = new StringBuffer();
		StringBuffer select = new StringBuffer();
		StringBuffer orderBy= new StringBuffer();

		QueryOrganizer qo = new QueryOrganizer(this.getRootElement(),user,level);

        if (hasSchemaOnlyCriteria()){
            qo.setWhere(criteria);
        }

        try {
            qo.addField(getRootElement().getFullXMLName() + "/meta/status");
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }

//		build ORDER BY clause
		if ((sortBy == null || sortBy.equalsIgnoreCase("")) && (customSortBy.equalsIgnoreCase("")))
		{
		    if (dv==null)
		    {
		        DisplayFieldWrapper dfw = ((DisplayFieldWrapper)this.getFields().getSortedFields().get(0));
				sortBy = dfw.getId();
		    }else{
				sortBy = dv.getDefaultOrderBy();
				sortOrder = dv.getDefaultSortOrder();
		    }
		}

		if (sortBy !=null && !sortBy.equalsIgnoreCase(""))
		{
		    if (!this.customSortBy.equalsIgnoreCase(""))
		    {
		        customSortBy = "";
		    }

			if (sortOrder == null || sortOrder.equalsIgnoreCase(""))
			{
				if(sortBy.indexOf(".") != -1)
				{
					SchemaElement e = SchemaElement.GetElement(sortBy.substring(0,sortBy.indexOf(".")));
                    String fieldID = sortBy.substring(sortBy.indexOf(".")+1);
                    if (fieldID.indexOf(".")!=-1){
                        fieldID = fieldID.substring(0,fieldID.indexOf("."));
                    }
					DisplayField df = e.getDisplayField(fieldID);
					sortOrder = df.getSortOrder();
				}else{
					DisplayField df = rootElement.getDisplayField(sortBy);
					sortOrder = df.getSortOrder();
				}
			}
		}

		if (this.getCustomSortBy().equalsIgnoreCase(""))
		{
			if(sortBy.indexOf(".") != -1)
			{
				SchemaElement e = SchemaElement.GetElement(sortBy.substring(0,sortBy.indexOf(".")));
                String fieldID = sortBy.substring(sortBy.indexOf(".")+1);
                if (fieldID.indexOf(".")!=-1){
                    fieldID = fieldID.substring(0,fieldID.indexOf("."));
                }
                DisplayField df = e.getDisplayField(fieldID);
				
				Iterator sfs = df.getSchemaFields().iterator();
		        while (sfs.hasNext())
		        {
                    Object[] o=(Object[])sfs.next();
                    String s = (String)o[0];
		            qo.addField(s);
		        }
			}else{
				DisplayField df = rootElement.getDisplayField(sortBy);
				Iterator sfs = df.getSchemaFields().iterator();
		        while (sfs.hasNext())
		        {
                    Object[] o=(Object[])sfs.next();
                    String s = (String)o[0];
		            qo.addField(s);
		        }
			}
		}else{
		}

		//build JOIN clause
		iter = displayFields.iterator();
		while(iter.hasNext())
		{
		   DisplayFieldReferenceI dfw = (DisplayFieldReferenceI) iter.next();
		   try {
		       DisplayField df=dfw.getDisplayField();

               Iterator sfs = df.getSchemaFields().iterator();
                while (sfs.hasNext())
                {
                    Object[] o=(Object[])sfs.next();
                    String s = (String)o[0];
//System.out.println(s);
                    qo.addField(s);
                }

                sfs = dfw.getSecondaryFields().iterator();
                while (sfs.hasNext())
                {
                    String s = (String)sfs.next();
                    DisplayField df2=DisplayField.getDisplayFieldForUnknownPath(s);
    	        	
                    if(df2!=null){
                        Iterator sfs2 = df2.getSchemaFields().iterator();
                        while (sfs2.hasNext())
                        {
                            Object[] o=(Object[])sfs2.next();
                            String f = (String)o[0];
//    System.out.println(f);
                            qo.addField(f);
                        }
                    }
                }

                if (df instanceof SQLQueryField){
                    if (dfw.getValue()!=null){
//System.out.println("SUBQUERY_" + df.getParentDisplay().getElementName() + ".SUBQUERYFIELD_" + df.getId() +"." + StringUtils.ReplaceStr(StringUtils.ReplaceStr((dfw).getValue().toString(), ",", "_com_"),":", "_col_"));
                        qo.addField("SUBQUERY_" + df.getParentDisplay().getElementName() + ".SUBQUERYFIELD_" + df.getId() +"." + (dfw).getValue().toString());
                    }
                }
	        } catch (DisplayFieldNotFoundException e) {
	            if (dfw.getType()!=null)
	            {
	                if (dfw.getType().equalsIgnoreCase("COUNT"))
	                {
//System.out.println("VIEW_" + rootElement.getFullXMLName() + ".COUNT_" + dfw.getElementName() +".count");
		                qo.addField("VIEW_" + rootElement.getFullXMLName() + ".COUNT_" + dfw.getElementName() +".count");
	                }
	            }else{
		            logger.error("",e);
	            }
	        }
		}

		if (this.inClauses.size()>0)
		{
			for (Map.Entry<String, String> entry:inClauses.entrySet())
		    {
		        for (String key:StringUtils.CommaDelimitedStringToArrayList(entry.getKey()))
		        {
			        try {
			        	DisplayField df=DisplayField.getDisplayFieldForUnknownPath(key);
			        	
			        	if(df!=null){
	                        Iterator sfs = df.getSchemaFields().iterator();
	                        while (sfs.hasNext())
	                        {
	                            Object[] o=(Object[])sfs.next();
	                            String s = (String)o[0];
	                            if(XFT.VERBOSE)System.out.println(s);
	                            qo.addField(s);
	                        }
			        	}
                    } catch (DisplayFieldNotFoundException e) {
                        logger.error("",e);
                    }
		        }
		    }
		}

		String query = qo.buildQuery();
		if (!query.startsWith("SELECT DISTINCT"))
		{
		    query = "SELECT DISTINCT " + query.substring(6);
		}
		join.append(" FROM (").append(query).append(") SEARCH");

		//build SELECT clause
		ArrayList added = new ArrayList();
		int counter = 0;
		iter = displayFields.iterator();
		while(iter.hasNext())
		{
		   DisplayFieldReferenceI dfr = (DisplayFieldReferenceI) iter.next();
		   try {
		       DisplayField df=dfr.getDisplayField();
               String alias = df.getId();
               if (df instanceof SQLQueryField){
            	   if(dfr.getValue().equals("{XDAT_USER_ID}")){
            		   dfr.setValue(user.getXdatUserId());
            	   }
                   alias = df.getId() +"_" + cleanColumnName((dfr).getValue().toString());
               }
               
               if (!added.contains(dfr.getElementName() + alias))
               {
            	   String content = this.getSQLContent(df,qo);
                   if (df instanceof SQLQueryField){
                       String xmlPath = "";
                       content= qo.getFieldAlias(df.getParentDisplay().getElementName() + ".SUBQUERYFIELD_" + df.getId() +"." + (dfr).getValue().toString());

                   }

            	   if (counter==0)
                   {
                       select.append("SELECT ");
                       select.append(content);
                       SchemaElementI se = df.getParentDisplay().getSchemaElement();
                       if (se.getFullXMLName().equalsIgnoreCase(this.getRootElement().getFullXMLName()))
                       {
            	           select.append(" AS ").append(alias);
                       }else{
            	           select.append(" AS ").append(se.getSQLName()).append("_").append(alias);
                       }
                       counter++;
                   }else{
                       select.append(", ");
                       select.append(content);
                       SchemaElementI se = df.getParentDisplay().getSchemaElement();
                       if (se.getFullXMLName().equalsIgnoreCase(this.getRootElement().getFullXMLName()))
                       {
            	           select.append(" AS ").append(alias);
                       }else{
            	           select.append(" AS ").append(se.getSQLName()).append("_").append(alias);
                       }
                       counter++;
                   }
            	   added.add(dfr.getElementName() + df.getId());
               }

               Iterator sfs = dfr.getSecondaryFields().iterator();
               while (sfs.hasNext())
               {
                   String s = (String)sfs.next();
                   DisplayField df2=DisplayField.getDisplayFieldForUnknownPath(s);
   	        	   if (df2!=null && !added.contains(dfr.getElementName() + df2.getId()))
            	   {
            		   String content = this.getSQLContent(df2,qo);
            		   SchemaElementI se = SchemaElement.GetElement(StringUtils.GetRootElementName(s));
        	           
            		   if (counter==0)
            	       {
            	           select.append("SELECT ");
            	           select.append(content);
            	           if (se.getFullXMLName().equalsIgnoreCase(this.getRootElement().getFullXMLName()))
            	           {
            		           select.append(" AS ").append(df2.getId());
            	           }else{
            		           select.append(" AS ").append(se.getSQLName()).append("_").append(df2.getId());
            	           }
            	           counter++;
            	       }else{
            	           select.append(", ");
            	           select.append(content);
            	           if (se.getFullXMLName().equalsIgnoreCase(this.getRootElement().getFullXMLName()))
            	           {
            		           select.append(" AS ").append(df2.getId());
            	           }else{
            		           select.append(" AS ").append(se.getSQLName()).append("_").append(df2.getId());
            	           }
            	           counter++;
            	       }
            		   added.add(dfr.getElementName() + df2.getId());
            	   }
               	}
	        } catch (DisplayFieldNotFoundException e) {
	            if (dfr.getType()!=null)
	            {
	                if (dfr.getType().equalsIgnoreCase("COUNT"))
	                {
		                select.append(", ");
		                SchemaElementI se = SchemaElement.GetElement(dfr.getElementName());
	                    select.append(se.getSQLName() + "_COUNT").append(" AS ").append(se.getSQLName() + "_"+ dfr.getId());
	                }
	            }else{
		            logger.error("",e);
	            }
	        }
		}

		String statusCol = qo.getFieldAlias(getRootElement().getFullXMLName() + "/meta/status","SEARCH");
		select.append(", ");
		select.append(statusCol).append(" AS QUARANTINE_STATUS ");

		ArrayList addons = getAddOns(displayFields);
		if (addons.size()>0)
		{
		    String rootField = rootElement.getGenericXFTElement().getFilter();
		    if (rootField != null)
		    {
				Iterator addOnIter = addons.iterator();
				while (addOnIter.hasNext())
				{
				    String fName = (String)addOnIter.next();
				    SchemaElementI foreign = SchemaElement.GetElement(fName);
				    if (isMultipleRelationship(foreign))
				    {
					    String foreignFilter = foreign.getGenericXFTElement().getFilterField();

					    String localType = GenericWrapperElement.GetFieldForXMLPath(rootField).getXMLType().getLocalType();
					    String foreignType = GenericWrapperElement.GetFieldForXMLPath(foreignFilter).getXMLType().getLocalType();
					    if (localType.equalsIgnoreCase(foreignType))
					    {
						    select.append(", ").append(StringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName() + "_DIFF"));
					    }
				    }
				}
		    }
		}

		orderBy.append(" ORDER BY ");
		if (this.getCustomSortBy().equalsIgnoreCase(""))
		{
			if(sortBy.indexOf(".") != -1)
			{
				SchemaElement e = SchemaElement.GetElement(sortBy.substring(0,sortBy.indexOf(".")));
                String fieldID = sortBy.substring(sortBy.indexOf(".")+1);
                if (fieldID.indexOf(".")!=-1){
                    fieldID = fieldID.substring(0,fieldID.indexOf("."));
                }
                DisplayField df = e.getDisplayField(fieldID);
                String content = this.getSQLContent(df,qo);
                if (df instanceof SQLQueryField){
                        String xmlPath = "";
                        iter = displayFields.iterator();
                        boolean matched =false;
                        DisplayFieldReferenceI dfr=null;
                        while(iter.hasNext())
                        {
                           dfr = (DisplayFieldReferenceI) iter.next();
                           if (dfr.getDisplayField().getId().equals(df.getId())){
                               matched=true;
                               break;
                           }
                        }
                        if (matched)
                            content= qo.getFieldAlias(df.getParentDisplay().getElementName() + ".SUBQUERYFIELD_" + df.getId() +"." + (dfr).getValue().toString());
                        else{
                            content="1";
                        }

                }

                orderBy.append("(" + content + ") ");
                sortOrder = sortOrder.trim();
                if (sortOrder.equalsIgnoreCase("desc") || sortOrder.equalsIgnoreCase("asc")){
                    orderBy.append(sortOrder);
                }

			}else{
				DisplayField df = rootElement.getDisplayField(sortBy);
				String content = this.getSQLContent(df,qo);
				orderBy.append("(" + content + ") ");
				sortOrder = sortOrder.trim();
				if (sortOrder.equalsIgnoreCase("desc") || sortOrder.equalsIgnoreCase("asc")){
				    orderBy.append(sortOrder);
				}
			}
		}else{
		    orderBy.append(this.getCustomSortBy());
		}

        if (!hasSchemaOnlyCriteria()){

            QueryOrganizer whereqo = new QueryOrganizer(this.getRootElement(),user,level);

            //build WHERE clause
            Iterator criteriaIter = criteria.getSchemaFields().iterator();
            while (criteriaIter.hasNext())
            {
                Object[] o=(Object[])criteriaIter.next();
                String s = (String)o[0];
                whereqo.addField(s);
            }
            
            for(DisplayCriteria dc:criteria.getSubQueries()){
            	
            	whereqo.addField("SUBQUERY_" + dc.getElementName() + ".SUBQUERYFIELD_" + dc.getField() +"." + dc.getWhere_value());
            }

            Iterator keys = rootElement.getAllPrimaryKeys().iterator();
            ArrayList keyXMLFields = new ArrayList();
            while (keys.hasNext())
            {
                SchemaFieldI sf = (SchemaFieldI)keys.next();
                String key =sf.getXMLPathString(rootElement.getFullXMLName());
                keyXMLFields.add(key);
                whereqo.addField(key);
            }

            String subQuery = whereqo.buildQuery();

            int whereCounter = 0;
            criteriaIter = criteria.iterator();
            while(criteriaIter.hasNext())
            {
                SQLClause c = (SQLClause) criteriaIter.next();
                if (whereCounter++==0)
                {
                    where.append(" \nWHERE ");
                }else
                {
                    where.append(" AND ");
                }

                where.append(c.getSQLClause(whereqo));

            }

            String whereQuery = "SELECT DISTINCT ";

            for (int i=0;i<keyXMLFields.size();i++){
                if (i>0)whereQuery +=", ";
                whereQuery+=whereqo.getFieldAlias((String)keyXMLFields.get(i));
            }

            whereQuery +=" FROM (" + subQuery + ") WHERE_CLAUSE " + where;

            query = select.toString() + join.toString() + " RIGHT JOIN ("+ whereQuery +") WHERE_CLAUSE ON ";
            //query = "SELECT SEARCH.* FROM ("+ whereQuery +") WHERE_CLAUSE LEFT JOIN (" + select + join + orderBy + ") SEARCH ON ";

            keys = rootElement.getAllPrimaryKeys().iterator();
            int keyCounter=0;
            while (keys.hasNext())
            {
                SchemaFieldI sf = (SchemaFieldI)keys.next();
                String key =sf.getXMLPathString(rootElement.getFullXMLName());
                if (keyCounter++>0){
                    query += " AND ";
                }
                query +="WHERE_CLAUSE." + whereqo.getFieldAlias(key) + "=SEARCH." + qo.getFieldAlias(key);

            }
            
            query+=orderBy.toString();
        }else{
            query =select.toString() + join.toString() + orderBy.toString();
        }

		if (this.inClauses.size()>0)
		{
	        sb.append(query);

		    StringBuffer sb2 = new StringBuffer();
		    sb2.append("SELECT DISTINCT DISPLAY_SEARCH.*");

		    int inCounter =0;
		    while (inCounter<inClauses.size())
		    {
		        sb2.append(", search").append(inCounter).append(".strings AS SEARCH_FIELD").append(inCounter);

		        sb2.append(", search").append(inCounter).append(".sort_order AS SORT_ORDER").append(inCounter);
		        inCounter++;
		    }

		    sb2.append(" FROM (").append(sb.toString()).append(") AS DISPLAY_SEARCH");

		    inCounter =0;

		    String orderByClause = "";
		    for (Map.Entry<String,String> entry:inClauses.entrySet())
		    {
		        String values = entry.getValue();
		        sb2.append(" RIGHT JOIN (SELECT * FROM stringstosortedtable(").append(values);
		        sb2.append(")) AS search").append(inCounter);

		        int subCounter=0;
		        for (String key:StringUtils.CommaDelimitedStringToArrayList(entry.getKey()))
		        {
			        DisplayField inDF=DisplayField.getDisplayFieldForUnknownPath(key);
			        if(inDF!=null){
				        String keyField=inDF.getId();
				        
				        if (! inDF.getParentDisplay().getElementName().equalsIgnoreCase(this.getRootElement().getFullXMLName()))
				        {
				            keyField = inDF.getParentDisplay().getSchemaElement().getSQLName() + "_" + keyField;
				        }

				        if (subCounter++==0)
				        {
					        sb2.append(" ON DISPLAY_SEARCH");
					        sb2.append(".").append(keyField).append("=search").append(inCounter).append(".strings");
				        }else{
					        sb2.append(" OR DISPLAY_SEARCH");
					        sb2.append(".").append(keyField).append("=search").append(inCounter).append(".strings");
				        }
			        }
		        }

		        if (inCounter==0)
		        {
		            orderByClause +=" ORDER BY search0.sort_order";
		        }else{
		            orderByClause +=", search" + inCounter +".sort_order";
		        }

		        inCounter++;
		    }
		    sb2.append(orderByClause);
		    sb = sb2;
		}else{
	        sb.append(query);
		}
		return sb.toString();
	}
	
	public static String cleanColumnName(String s){
		s=StringUtils.ReplaceStr(s, ",", "_com_");
		s=StringUtils.ReplaceStr(s, ":", "_col_");
		s=StringUtils.ReplaceStr(s, "-", "_");
		s=StringUtils.ReplaceStr(s, "/", "");
		s=StringUtils.ReplaceStr(s, "\\", "_");
		s=StringUtils.ReplaceStr(s, ";", "_");
		s=StringUtils.ReplaceStr(s, "'", "_");
		s=StringUtils.ReplaceStr(s, "\"", "_");
		s=StringUtils.ReplaceStr(s, "?", "_");
		s=StringUtils.ReplaceStr(s, "!", "_");
		s=StringUtils.ReplaceStr(s, "~", "_");
		s=StringUtils.ReplaceStr(s, "`", "_");
		s=StringUtils.ReplaceStr(s, "#", "_");
		s=StringUtils.ReplaceStr(s, "$", "_");
		s=StringUtils.ReplaceStr(s, "%", "_");
		s=StringUtils.ReplaceStr(s, "^", "_");
		s=StringUtils.ReplaceStr(s, "&", "_");
		s=StringUtils.ReplaceStr(s, "*", "_");
		s=StringUtils.ReplaceStr(s, "(", "_");
		s=StringUtils.ReplaceStr(s, ")", "_");
		s=StringUtils.ReplaceStr(s, "+", "_");
		s=StringUtils.ReplaceStr(s, "=", "_");
		s=StringUtils.ReplaceStr(s, "|", "_");
		s=StringUtils.ReplaceStr(s, "{", "_");
		s=StringUtils.ReplaceStr(s, "}", "_");
		s=StringUtils.ReplaceStr(s, "[", "");
		s=StringUtils.ReplaceStr(s, "]", "");
		s=StringUtils.ReplaceStr(s, "<", "_");
		s=StringUtils.ReplaceStr(s, ">", "_");
		s=StringUtils.ReplaceStr(s, "@", "");
		return s;
	}

    /**
     * @param foreign
     * @return
     */
    public boolean isMultipleRelationship(SchemaElementI foreign)
    {
        if (foreign.getFullXMLName().equals(rootElement.getFullXMLName())){
            return false;
        }else{
            Boolean b = (Boolean)this.isMultipleRelationship.get(foreign.getSQLName());
            if (b==null)
            {
                boolean isMultiple = IsMultipleReference(rootElement,foreign);

               b = new Boolean(isMultiple);
               this.isMultipleRelationship.put(foreign.getSQLName(),b);
            }
            return b.booleanValue();
        }
    }

    private static boolean IsMultipleReference(SchemaElementI rootElement,SchemaElementI foreign)
    {
        boolean isMultiple = true;
        //XFT.LogCurrentTime("isMultipleRelationship :1");
        String connectionType = QueryOrganizer.GetConnectionType(rootElement.getFullXMLName(),foreign.getFullXMLName());
        if (connectionType.equals("schemaelement"))
        {
            isMultiple=  false;
        }else if (connectionType.equals("arc"))
        {
            ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement,foreign);
            if (arcDefine!=null)
            {
                if (arcDefine.getBridgeElement().equals(rootElement.getFullXMLName())){
                    return false;
                }else if (arcDefine.getBridgeElement().equals(foreign.getFullXMLName())){
                    return false;
                }else{
                    String s = arcDefine.getClosestField();
                    if (s==null)
                    {
                        return false;
                    }else{
                        return true;
                    }
                }
            }
        }else if (connectionType.equals("connection"))
        {
            String[] connection = rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());

            //XFT.LogCurrentTime("isMultipleRelationship :2");
    	     if (connection !=null)
              {
    	         if (connection[2].equalsIgnoreCase("reference"))
                 {
    	             isMultiple= true;
                 }else{
                     try {
                        if (GenericWrapperElement.IsMultipleReference(connection[0]))
                         {
                             isMultiple= true;
                         }
//                        else if(GenericWrapperElement.IsMultipleReference(connection[1])){
//                             isMultiple= true;
//                         }
                        else{
                             isMultiple= false;
                         }
                    } catch (RuntimeException e) {
                        isMultiple= true;
                    }
                 }
              }
        }else if (connectionType.equals("multi-leveled"))
        {
            ArrayList checked = new ArrayList();
            String s = foreign.getFullXMLName();
            String mappingElement = null;

            Iterator arcs = DisplayManager.GetInstance().getArcDefinitions(rootElement).iterator();
            while (arcs.hasNext())
            {
                ArcDefinition arc = (ArcDefinition)arcs.next();
                if (!arc.getBridgeElement().equals(rootElement.getFullXMLName()))
                {
                    if (!checked.contains(arc.getBridgeElement()))
                    {
                        checked.add(arc.getBridgeElement());
                        if (QueryOrganizer.CanConnect(arc.getBridgeElement(),s))
                        {
                            mappingElement = arc.getBridgeElement();
                            break;
                        }
                    }
                }

                Iterator arcMembers = arc.getMembers();
                while (arcMembers.hasNext())
                {
                    String member = (String)arcMembers.next();
                    if (!checked.contains(member))
                    {
                        checked.add(member);
                        if (QueryOrganizer.CanConnect(member,s))
                        {
                            mappingElement = member;
                            break;
                        }
                    }
                }
            }

            if (mappingElement == null)
            {
                //UNKNOWN CONNECTION
                return true;
            }else{
                try {
                    SchemaElementI mappingE = SchemaElement.GetElement(mappingElement);

                    isMultiple = IsMultipleReference(rootElement,mappingE);
                    if (isMultiple)
                    {
                        return isMultiple;
                    }else{
                        isMultiple = IsMultipleReference(mappingE,foreign);
                        return isMultiple;
                    }
                } catch (Exception e) {
                    logger.error("",e);
                    return true;
                }
            }

        }

        return isMultiple;
    }

	public ArrayList getAddOns(ArrayList displayFields)
	{
	    ArrayList al = new ArrayList();
	    ArrayList done = new ArrayList();
	    Iterator iter = displayFields.iterator();
		while(iter.hasNext())
		{
		   DisplayFieldReferenceI dfr = (DisplayFieldReferenceI) iter.next();
		   if (!done.contains(dfr.getElementName()))
		   {
		       if (dfr.getType() ==null || dfr.getType().equals(""))
		       {
				   done.add(dfr.getElementName());
				   if (!rootElement.getFullXMLName().equalsIgnoreCase(dfr.getElementName()))
				   {

				       	al.add(dfr.getElementName());
				   }
		       }
		   }
		}
		return al;
	}

	public String getSQLContent(DisplayField df2, QueryOrganizer qo) throws FieldNotFoundException
	{
	    String content = df2.getSqlContent();

	   Iterator dfes = df2.getElements().iterator();
	   while (dfes.hasNext())
	   {
	       DisplayFieldElement dfe = (DisplayFieldElement)dfes.next();
	       String dfeAlias = null;
	       if (dfe.getSchemaElementName().equalsIgnoreCase(""))
	       {
	           String viewName = df2.getParentDisplay().getElementName() + ".";
	           viewName += dfe.getViewName() + "." + dfe.getViewColumn();
	           if (qo.getFieldAlias(viewName) !=null)
	           {
	               dfeAlias = (String)qo.getFieldAlias(viewName);
	           }else{
		           dfeAlias = dfe.getViewName() + "_" + dfe.getViewColumn();
	           }
	       }else{
	           if (dfe.getXdatType() == null || dfe.getXdatType().equalsIgnoreCase("")){
		           dfeAlias = qo.getFieldAlias(dfe.getSchemaElementName(),"SEARCH");
	           }else{
	        	   if(df2.getParentDisplay().getElementName().equals(this.getRootElement().getFullXMLName())){
		               try {
			               dfeAlias = SchemaElement.GetElement(dfe.getSchemaElementName()).getSQLName() + "_COUNT";
		                } catch (XFTInitException e) {
		                    logger.error("",e);
		                    dfeAlias = "'ERROR'";
		                } catch (ElementNotFoundException e) {
		                    logger.error("",e);
		                    dfeAlias = "'ERROR'";
		                }
	        	   }else{
	        		   String viewName = df2.getParentDisplay().getElementName() + ".";
	    	           viewName += dfe.getXdatType() + "_" + dfe.getSchemaElementName() + "." + dfe.getXdatType();
	    	           dfeAlias = (String)qo.getFieldAlias(viewName);
	        	   }
	           }
	       }

	       if (content==null)
	       {
		       content= dfeAlias;
	       }else{
		       content= StringUtils.ReplaceStr(content,"@" + dfe.getName(),dfeAlias);
	       }
	   }

//	   if (XFT.getWEBAPP_NAME()!=null)
//	   {
//	       if (content.indexOf("@WEBAPP")!=-1)
//	       {
//			   content = StringUtils.ReplaceStr(content,"@WEBAPP",XFT.getWEBAPP_NAME());
//	       }
//	   }

	   return content;
	}

	public XFTTableI execute(String login) throws Exception
	{
	    return this.execute(null,login);
	}

	public org.nrg.xft.search.CriteriaCollection getEmptyCollection(String andOr)
	{
	    return new org.nrg.xft.search.CriteriaCollection(andOr);
	}

	public XFTTableI getPage(int pageNumber,PresentationA p,String login) throws XFTInitException,ElementNotFoundException,DBPoolException,SQLException,Exception
	{
		if (newQuery || !pagingOn)
		{
			currentPageNum=pageNumber;
			lastPresenter= p;
			return execute(lastPresenter,login);
		}else{

			currentPageNum=pageNumber;
			int offset = currentPageNum * rowsPerPage;

            Long count = PoolDBUtils.CreateManagedTempTable(this.getResultsTableName(), query, user);
            table = PoolDBUtils.RetrieveManagedTempTable(this.getResultsTableName(), user, offset, rowsPerPage);
			lastPresenter=p;
			if (lastPresenter != null)
			{
				lastPresenter.setRootElement(rootElement);
				lastPresenter.setDisplay(display);
				lastPresenter.setAdditionalViews(additionalViews);
				presentedTable = lastPresenter.formatTable(table,this,this.allowDiffs);
			}else{
				presentedTable = table;
			}
			return presentedTable;
		}
	}

	private void calculatePages()
	{
		if (this.numRows > this.rowsPerPage)
		{
			pages = (numRows/rowsPerPage) + 1;
		}else if (numRows ==0)
		{
			pages = 0;
		}else{
			pages = 1;
		}
	}

	private static String GetSelectsForVersion(SchemaElement rootElement,DisplayVersion dv, int counter,boolean isRoot) throws XFTInitException,ElementNotFoundException,Exception
	{
		StringBuffer sb = new StringBuffer();
		Iterator fields = dv.getDisplayFieldRefIterator();
		while (fields.hasNext())
		{
			if (counter++ != 0)
			{
				sb.append(" ,");
			}

			DisplayFieldRef dfr = (DisplayFieldRef)fields.next();
			if (isRoot)
				sb.append(rootElement.getDisplayTable()).append(".").append(dfr.getId());
			else
				sb.append(rootElement.getDisplayTable()).append(".").append(dfr.getId()).append(" AS ").append(rootElement.getSQLName()).append("_").append(dfr.getId());
		}
		return sb.toString();
	}

	/**
	 * @return
	 */
	public ArrayList getAdditionalViews() {
		return additionalViews;
	}

	/**
	 * @return
	 */
	public ArrayList getCriteria() {
		return criteria.toArrayList();
	}

	/**
	 * @return
	 */
	public String getDisplay() {
		return display;
	}

	/**
	 * @return
	 */
	public XFTTableI getPresentedTable() {
		return presentedTable;
	}


	/**
	 * @return
	 */
	public SchemaElement getRootElement() {
		return rootElement;
	}

	/**
	 * @return
	 */
	public String getSortBy() {
		return sortBy;
	}

	/**
	 * @return
	 */
	public String getSortOrder() {
		return sortOrder;
	}

	/**
	 * @return
	 */
	protected XFTTableI getRootTable() {
		return table;
	}

	public int getNumColumns()
	{
	    if (table ==null)
	    {
	        return 0;
	    }else{
	        return table.getNumCols();
	    }
	}

	/**
	 * @param hashtable
	 */
	public void setAdditionalViews(ArrayList hashtable) {
		additionalViews = hashtable;
		newQuery = true;
	}

	public void addAdditionalView(String element, String display)
	{
		additionalViews.add(new String[]{element,display});
		newQuery = true;
	}

	/**
	 * @param ArrayList of Criteria and/or CriteriaCollections
	 */
	public void setCriteria(ArrayList list) {
		criteria.addCriteria(list);
		newQuery = true;
	}

	public void addCriteria(SQLClause c)
	{
		criteria.add(c);
		newQuery = true;
	}

	public void addCriteria(String element, String displayField, String comparisonType, Object value)throws Exception
	{
	    addCriteria(element,displayField,comparisonType,value,false);
	}

	public void addCriteria(String element, String displayField, String comparisonType, Object value,boolean overrideDataTypeFormatting)throws Exception
	{
	    DisplayCriteria dc = new DisplayCriteria();
	    dc.setSearchFieldByDisplayField(element,displayField);
	    dc.setComparisonType(comparisonType);
	    dc.setValue(value,true);
	    dc.setOverrideDataFormatting(overrideDataTypeFormatting);
	    addCriteria(dc);
	}

	public void addCriteria(String xmlPath, String comparisonType, Object value) throws Exception
	{
	    ElementCriteria ec = new ElementCriteria();
	    ec.setFieldWXMLPath(xmlPath);
	    SchemaElement se = SchemaElement.GetElement(ec.getElementName());
	    DisplayField df = se.getDisplayFieldForXMLPath(xmlPath);
	    if (df == null)
	    {
	        ec.setComparison_type(comparisonType);
	        ec.setValue(value);
	        addCriteria(ec);
	    }else{
	        addCriteria(se.getFullXMLName(),df.getId(),comparisonType,value);
	    }
	}

	/**
	 * @param string
	 */
	public void setDisplay(String string) {
		display = string;
		newQuery = true;
	}

	/**
	 * @param element
	 */
	public void setRootElement(SchemaElement element) {
		rootElement = element;
		newQuery = true;
	}

	public GenericWrapperElement getElement()
	{
	    return this.rootElement.getGenericXFTElement();
	}

	public void setElement(GenericWrapperElement element)
	{
	    SchemaElement se = new SchemaElement(element);
	    setRootElement(se);
	}

	public void setRootElement(String elementName)throws XFTInitException,ElementNotFoundException
	{
		rootElement = SchemaElement.GetElement(elementName);
		newQuery = true;
	}

	/**
	 * @param string
	 */
	public void setSortBy(String string) {
		sortBy = string;
		newQuery = true;
	}

	/**
	 * @param string
	 */
	public void setSortOrder(String string) {
		sortOrder = string;
		newQuery = true;
	}

	public PresentationA assignPresenterProperties(PresentationA presenter)
	{
		presenter.setRootElement(rootElement);
		presenter.setDisplay(display);
		presenter.setAdditionalViews(additionalViews);
		return presenter;
	}

	public boolean isSuperSearch()
	{
		if (this.additionalViews.size() > 0)
		{
			return true;
		}else
		{
			return false;
		}
	}
	/**
	 * @return
	 */
	public int getCurrentPageNum() {
		return currentPageNum;
	}

	/**
	 * @return
	 */
	public boolean isPagingOn() {
		return pagingOn;
	}

	/**
	 * @return
	 */
	public int getRowsPerPage() {
		return rowsPerPage;
	}

	/**
	 * @param i
	 */
    @SuppressWarnings("unused")
	private void setCurrentPageNum(int i) {
		currentPageNum = i;
	}

	/**
	 * @param b
	 */
	public void setPagingOn(boolean b) {
		if (pagingOn != b)
		{
			newQuery=true;
		}
		pagingOn = b;
	}

	/**
	 * @param i
	 */
	public void setRowsPerPage(int i) {
		rowsPerPage = i;
		currentPageNum = 0;
		calculatePages();
	}

	/**
	 * @return
	 */
	public int getPages() {
		return pages;
	}

	/**
	 * @param i
	 */
	@SuppressWarnings("unused")
    private void setPages(int i) {
		pages = i;
	}

	public static ArrayList SearchForItems(SchemaElementI e,org.nrg.xft.search.CriteriaCollection criteria) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,Exception
	{
		ItemSearch search = new ItemSearch(null,e.getGenericXFTElement(),criteria);
		return search.exec(true).getItems();
	}
	/**
	 * @return
	 */
	public UserI getUser() {
		return user;
	}

	/**
	 * @param user
	 */
	public void setUser(UserI user) {
		this.user = (XDATUser)user;
	}

	/**
	 * @return
	 */
	public int getNumRows() {
		return numRows;
	}

	/**
	 * @return Returns the criteriaCollection.
	 */
	public org.nrg.xft.search.CriteriaCollection getCriteriaCollection() {
		return criteria;
	}
	/**
	 * @param criteriaCollection The criteriaCollection to set.
	 */
	public void setCriteriaCollection(org.nrg.xft.search.CriteriaCollection criteriaCollection) {
		this.criteria = criteriaCollection;
	}

	public String toString()
	{
	    StringBuffer sb = new StringBuffer();
	    sb.append("DisplaySearch (").append(this.getElement().getFullXMLName());
	    try {
            sb.append(") Criteria:").append(this.criteria.getSQLClause(null));
        } catch (Exception e) {
            logger.error("",e);
        }
        return sb.toString();
	}

    /**
     * @return Returns the customSortBy.
     */
    public String getCustomSortBy() {
        return customSortBy;
    }
    /**
     * @param customSortBy The customSortBy to set.
     */
    public void setCustomSortBy(String customSortBy) {
        this.customSortBy = customSortBy;
    }

    /**
     * @return Returns the inClauses.
     */
    public Hashtable<String,String> getInClauses() {
        return inClauses;
    }

    public void addInClause(String fields, String commaDelimitedValues)
    {
        commaDelimitedValues = StringUtils.ReplaceStr(commaDelimitedValues,"\r\n,",",");
        commaDelimitedValues = StringUtils.ReplaceStr(commaDelimitedValues,",\r\n",",");
        commaDelimitedValues = StringUtils.ReplaceStr(commaDelimitedValues,"\"","");
        commaDelimitedValues = StringUtils.ReplaceStr(commaDelimitedValues,"'","");
        commaDelimitedValues = StringUtils.ReplaceStr(commaDelimitedValues,"\r\n",",");

        ArrayList al = StringUtils.CommaDelimitedStringToArrayList(commaDelimitedValues);
        Iterator iter = al.iterator();
        StringBuffer sb = new StringBuffer();
        int counter = 0;
        while (iter.hasNext())
        {

            String s = (String)iter.next();
            if (s.trim().indexOf(" ") == -1)
            {
                if (counter++!=0)
                {
                    sb.append(",\"").append(s.trim()).append("\"");
                }else{
                    sb.append("\"").append(s.trim()).append("\"");
                }
            }else{
                Iterator iter2 = StringUtils.DelimitedStringToArrayList(s.trim()," ").iterator();
                while (iter2.hasNext())
                {
                    String s2 = (String)iter2.next();
                    if (counter++!=0)
                    {
                        sb.append(",\"").append(s2.trim()).append("\"");
                    }else{
                        sb.append("\"").append(s2.trim()).append("\"");
                    }
                }
            }
        }

        commaDelimitedValues = "'{" + sb.toString() + "}'";
        inClauses.put(fields,commaDelimitedValues);
    }


    /**
     * @return Returns the fields.
     */
    public DisplayFieldWrapperCollection getFields() {
        return fields;
    }
    /**
     * @param fields The fields to set.
     */
    public void setFields(DisplayFieldWrapperCollection fields) {
        this.fields = fields;
    }

    public void addDisplayField(DisplayField df)
    {
        this.getFields().addDisplayField(df);
    }

    public void addDisplayFields(Collection coll)
    {
        this.getFields().addDisplayFields(coll);
    }

    public void addDisplayField(String elementName, String fieldID)
    {
        try {
			SchemaElement se = SchemaElement.GetElement(elementName);
			try {
			    DisplayField df = se.getDisplayField(fieldID);
			    this.getFields().addDisplayField(df);
			} catch (DisplayFieldNotFoundException e) {
				try {
					DisplayField df=se.getDisplayFieldForXMLPath(fieldID);
					if(df==null){
						logger.error("",e);
					}else{
						this.getFields().addDisplayField(df);
					}
				} catch (Exception e1) {
			        logger.error("",e);
				}
			}
		} catch (XFTInitException e) {
	        logger.error("",e);
		} catch (ElementNotFoundException e) {
	        logger.error("",e);
		}
    }

    public void addDisplayField(String elementName, String fieldID, String header)
    {
        try {
			SchemaElement se = SchemaElement.GetElement(elementName);
			try {
			    DisplayField df = se.getDisplayField(fieldID);
			    this.getFields().addDisplayField(df,header,null);
			} catch (DisplayFieldNotFoundException e) {
				try {
					DisplayField df=se.getDisplayFieldForXMLPath(fieldID);
					if(df==null){
						logger.error("",e);
					}else{
						this.getFields().addDisplayField(df,header,null);
					}
				} catch (Exception e1) {
			        logger.error("",e);
				}
			}
		} catch (XFTInitException e) {
	        logger.error("",e);
		} catch (ElementNotFoundException e) {
	        logger.error("",e);
		}
    }

    public void addDisplayField(String elementName, String fieldID, Object value)
    {
        try {
			SchemaElement se = SchemaElement.GetElement(elementName);
			try {
			    DisplayField df = se.getDisplayField(fieldID);
			    this.getFields().addDisplayField(df,value);
			} catch (DisplayFieldNotFoundException e) {
				try {
					DisplayField df=se.getDisplayFieldForXMLPath(fieldID);
					if(df==null){
						logger.error("",e);
					}else{
						this.getFields().addDisplayField(df,value);
					}
				} catch (Exception e1) {
			        logger.error("",e);
				}
			}
		} catch (XFTInitException e) {
	        logger.error("",e);
		} catch (ElementNotFoundException e) {
	        logger.error("",e);
		}
    }

    public void addDisplayField(String elementName, String fieldID, String header, Object value)
    {
        try {
			SchemaElement se = SchemaElement.GetElement(elementName);
			try {
			    DisplayField df = se.getDisplayField(fieldID);
			    this.getFields().addDisplayField(df,header,value);
			} catch (DisplayFieldNotFoundException e) {
				try {
					DisplayField df=se.getDisplayFieldForXMLPath(fieldID);
					if(df==null){
						logger.error("",e);
					}else{
						this.getFields().addDisplayField(df,header,value);
					}
				} catch (Exception e1) {
			        logger.error("",e);
				}
			}
		} catch (XFTInitException e) {
	        logger.error("",e);
		} catch (ElementNotFoundException e) {
	        logger.error("",e);
		}
    }

    public void addDisplayField(String elementName, String fieldID, String header, Object value,Boolean visible)
    {
        try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            try{
	            DisplayField df = se.getDisplayField(fieldID);
	            this.getFields().addDisplayField(df,header,value,visible);
            } catch (DisplayFieldNotFoundException e) {
            	try {
            		DisplayField df;
            		if(fieldID.indexOf(".")==-1 && fieldID.indexOf("/")==-1){
    					df=se.getDisplayFieldForXMLPath(this.getRootElement().getFullXMLName() + "/" + fieldID);
            		}else{
    					df=se.getDisplayFieldForXMLPath(fieldID);
            		}
					if(df==null){
						logger.error("",e);
					}else{
						this.getFields().addDisplayField(df,header,value,visible);
					}
				} catch (Exception e1) {
		            logger.error(e.getMessage());
				}
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
    }

    /**
     * @return Returns the useVersions.
     */
    public boolean useVersions() {
        return useVersions;
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return Returns the level.
     */
    public String getLevel() {
        return level;
    }
    /**
     * @param level The level to set.
     */
    public void setLevel(String level) {
        this.level = level;
    }


    /**
     * @return Returns the title.
     */
    public String getTitle() {
        if (title != null && !title.equals(""))
        {
            return title;
        }else{
            if (this.isSuperSearch())
            {
                return "Super Search";
            }else{
                return getRootElement().getDisplay().getDescription();
            }
        }
    }
    /**
     * @param title The title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public Object getWebFormValue(String formFieldName)
    {
        if (this.webFormValues.get(formFieldName.toLowerCase())==null)
        {
            return "";
        }else{
            return webFormValues.get(formFieldName.toLowerCase());
        }
    }

    public void setWebFormValue(String formFieldName,Object value)
    {
        webFormValues.put(formFieldName.toLowerCase(),value);
    }

    public void resetWebFormValues()
    {
        webFormValues = new Hashtable();
    }

    public Hashtable getWebFormValues()
    {
        return webFormValues;
    }

    public ArrayList getAdditialViewArrayLists()
    {
        ArrayList al = new ArrayList();
        for (int i=0;i<this.getAdditionalViews().size();i++)
        {
            String[] view = (String[])this.getAdditionalViews().get(i);
            ArrayList sub = new ArrayList();
            sub.add(view[0]);
            sub.add(view[1]);
            al.add(sub);
        }
        al.trimToSize();
        return al;
    }


    /**
     * @return Returns the resultsTableName.
     */
    public void resetResultsTableName() {
        resultsTableName = null;
    }

    /**
     * @return Returns the resultsTableName.
     */
    private String getResultsTableName() {
        if (resultsTableName ==null)
        {
            if (this.getUser() == null)
            {
                resultsTableName = "xs_" + Calendar.getInstance().getTimeInMillis();
            }else{
                resultsTableName = "xs_" + getUser().getUsername() + "_" +Calendar.getInstance().getTimeInMillis();
            }
        }
        return resultsTableName.toLowerCase();
    }
    /**
     * @return Returns the customSearch.
     */
    public boolean isStoredSearch() {
        return isStoredSearch;
    }
    /**
     * @param customSearch The customSearch to set.
     */
    public void setStoredSearch(boolean isStoredSearch) {
        this.isStoredSearch = isStoredSearch;
    }
    /**
     * @return Returns the storedSearch.
     */
    public org.nrg.xdat.om.XdatStoredSearch getStoredSearch() {
        return storedSearch;
    }
    /**
     * @param storedSearch The storedSearch to set.
     */
    public void setStoredSearch(
            org.nrg.xdat.om.XdatStoredSearch storedSearch) {
        this.storedSearch = storedSearch;
        this.setStoredSearch(true);
    }
    /**
     * @return Returns the allowDiffs.
     */
    public boolean isAllowDiffs() {
        return allowDiffs;
    }
    /**
     * @param allowDiffs The allowDiffs to set.
     */
    public void setAllowDiffs(boolean allowDiffs) {
        this.allowDiffs = allowDiffs;
    }

    public XdatStoredSearch convertToStoredSearch(String identifier){
        XdatStoredSearch xss = null;
        try {
            xss = new XdatStoredSearch((UserI)this.getUser());

            xss.setRootElementName(this.getRootElement().getFullXMLName());
            String sortBy=this.getSortBy();

            if (sortBy.indexOf(".")==-1)
            {
                if (!sortBy.equals("")){
                    xss.setSortBy_elementName(xss.getRootElementName());
                    xss.setSortBy_fieldId(sortBy);
                }
            }else{
                String elementName = sortBy.substring(0,sortBy.indexOf("."));
                String fieldId = sortBy.substring(sortBy.indexOf(".")+1);
                xss.setSortBy_elementName(elementName);
                xss.setSortBy_fieldId(fieldId);
            }

            ElementDisplay ed = DisplayManager.GetElementDisplay(xss.getRootElementName());
            if(this.getFields().size()>0){
                int sequence = 0;
                
            	for(DisplayFieldReferenceI ref:this.getFields().getSortedVisibleFields()){
            		XdatSearchField xsf = new XdatSearchField();
                    if (ref.getElementName()!=null){
                        xsf.setElementName(ref.getElementName());
                    }else{
                        xsf.setElementName(this.getRootElement().getFullXMLName());
                    }
                    if(ref.getId().indexOf(".")==-1){
                    	String f=ref.getId();
                    	if(f.indexOf("=")==-1){
                    		xsf.setFieldId(f);
                    	}else{
                    		xsf.setFieldId(f.substring(0,f.indexOf("=")));
                    		xsf.setValue(f.substring(f.indexOf("=")+1));
                    	}
                    }else{
                    	String f=ref.getId().substring(ref.getId().indexOf(".")+1);
                    	if(f.indexOf("=")==-1){
                    		xsf.setFieldId(f);
                    	}else{
                    		xsf.setFieldId(f.substring(0,f.indexOf("=")));
                    		xsf.setValue(f.substring(f.indexOf("=")+1));
                    	}
                    }
                    if (ref.getHeader()==null || ref.getHeader().equals(""))
                        xsf.setHeader("  ");
                    else
                        xsf.setHeader(ref.getHeader());
                    xsf.setType(ref.getDisplayField().getDataType());
                    xsf.setSequence(new Integer(sequence++));
                    if (ref.getValue()!=null && !ref.getValue().equals(""))
                        xsf.setValue(ref.getValue().toString());
                    if(!ref.isVisible()){
                 	   xsf.setVisible(false);
                    }
                    xss.setSearchField(xsf);
            	}
            }else{
                DisplayVersion rootdv = ed.getVersion(this.getDisplay(),"listing");

                ArrayList<DisplayVersion> displayVersions = new ArrayList<DisplayVersion>();
                displayVersions.add(rootdv);


                if (this.getAdditionalViews() != null && this.getAdditionalViews().size() > 0)
                {
                    Iterator keys = this.getAdditionalViews().iterator();
                    while (keys.hasNext())
                    {
                        String[] key = (String[])keys.next();
                        String elementName = key[0];
                        String version = key[1];
                        SchemaElementI foreign = SchemaElement.GetElement(elementName);

                        ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getFullXMLName());
                        DisplayVersion foreignDV = null;
                        foreignDV = foreignEd.getVersion(version,"default");
                        displayVersions.add(foreignDV);
                    }
                }

                int sequence = 0;
                
                for(DisplayVersion dv:displayVersions)
                {
                   Iterator iter = dv.getDisplayFieldRefIterator();
                   while(iter.hasNext())
                   {
                       DisplayFieldRef ref = (DisplayFieldRef)iter.next();
                       try {
                        if (ref.getDisplayField()!=null){
                               XdatSearchField xsf = new XdatSearchField();
                               if (ref.getElementName()!=null){
                                   xsf.setElementName(ref.getElementName());
                               }else{
                                   xsf.setElementName(dv.getParentElementDisplay().getElementName());
                               }
                               xsf.setFieldId(ref.getId());
                               if (ref.getHeader()==null || ref.getHeader().equals(""))
                                   xsf.setHeader("  ");
                               else
                                   xsf.setHeader(ref.getHeader());
                               xsf.setType(ref.getDisplayField().getDataType());
                               xsf.setSequence(new Integer(sequence++));
                               if (ref.getValue()!=null && !ref.getValue().equals(""))
                                   xsf.setValue(ref.getValue().toString());
                               if(!ref.isVisible()){
                            	   xsf.setVisible(false);
                               }
                               xss.setSearchField(xsf);
                           }
                        } catch (DisplayFieldNotFoundException e) {
                            logger.error("",e);
                        }
                   }
                }
            }

            if(this.getCriteria().size()>0){
                XdatCriteriaSet set = new XdatCriteriaSet();
        		for(int i=0;i<this.getCriteria().size();i++){
        			set.setMethod("AND");
        	        Iterator iter = this.getCriteria().iterator();
        	        while (iter.hasNext())
        	        {
        	            SQLClause c = (SQLClause)iter.next();
        	            if (c instanceof org.nrg.xft.search.CriteriaCollection)
        	            {
        	                XdatCriteriaSet subset = new XdatCriteriaSet();
        	                subset.populateCriteria((org.nrg.xft.search.CriteriaCollection)c);

        	                if (subset.size()> 0)
        	                {
        	                    set.setChildSet(subset);
        	                }
        	            }else{
        	                XdatCriteria criteria = new XdatCriteria();
        	                criteria.populateCriteria(c);
        	                
        	                set.setCriteria(criteria);
        	            }
        	        }
        		}
                if (set.size()> 0)
                {
                    xss.setSearchWhere(set);
                }
            }else if(this.getInClauses().size()>0){
                XdatCriteriaSet set = new XdatCriteriaSet();
                set.setMethod("OR");
                for(Map.Entry<String, String> entry : this.getInClauses().entrySet()){
                    XdatCriteria crit = new XdatCriteria();
                    crit.setSchemaField(entry.getKey());
                    crit.setValue(entry.getValue());
                    crit.setComparisonType("IN");
                    set.setCriteria(crit);
                }
                xss.setSearchWhere(set);
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (DisplayFieldNotFoundException e) {
            logger.error("",e);
        } catch (Exception e) {
            logger.error("",e);
        }

        return xss;
    }



	public List<DisplayFieldReferenceI> getAllFields(String versionExtension) throws ElementNotFoundException, XFTInitException
	{
		final ElementDisplay ed = DisplayManager.GetElementDisplay(getRootElement().getFullXMLName());
	    final DisplayVersion dv;
		List<DisplayFieldReferenceI> allfields=new ArrayList<DisplayFieldReferenceI>();
		if (this.useVersions())
		{
			if (! versionExtension.equalsIgnoreCase(""))
			{
				dv = ed.getVersion(getDisplay() + "_" + versionExtension,getDisplay());
			}else{
				dv = ed.getVersion(getDisplay(),"default");
			}
			allfields = dv.getAllFields();

			if (getAdditionalViews() != null && getAdditionalViews().size() > 0)
			{
				Iterator keys = getAdditionalViews().iterator();
				while (keys.hasNext())
				{
				    String[] key = (String[])keys.next();
					String elementName = key[0];
					String version = key[1];
					GenericWrapperElement foreign = GenericWrapperElement.GetElement(elementName);

					ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getType().getFullForeignType());
					DisplayVersion foreignDV = null;
					if (! versionExtension.equalsIgnoreCase(""))
					{
						foreignDV = foreignEd.getVersion(version + "_" + versionExtension,version);
					}else{
						foreignDV = foreignEd.getVersion(version,"default");
					}

					allfields.addAll(foreignDV.getAllFields());
				}
			}
		}else{
			allfields = this.getFields().getSortedFields();
		}

		return allfields;
	}

	public ArrayList<DisplayFieldReferenceI> getVisibleFields(String versionExtension) throws ElementNotFoundException, XFTInitException
	{
		ElementDisplay ed = DisplayManager.GetElementDisplay(getRootElement().getFullXMLName());
	    DisplayVersion dv = null;
		ArrayList<DisplayFieldReferenceI> visibleFields=new ArrayList<DisplayFieldReferenceI>();
		if (this.useVersions())
		{
			if (! versionExtension.equalsIgnoreCase(""))
			{
				dv = ed.getVersion(getDisplay() + "_" + versionExtension,getDisplay());
			}else{
				dv = ed.getVersion(getDisplay(),"default");
			}
			visibleFields = dv.getVisibleFields();

			if (getAdditionalViews() != null && getAdditionalViews().size() > 0)
			{
				Iterator keys = getAdditionalViews().iterator();
				while (keys.hasNext())
				{
				    String[] key = (String[])keys.next();
					String elementName = key[0];
					String version = key[1];
					GenericWrapperElement foreign = GenericWrapperElement.GetElement(elementName);

					ElementDisplay foreignEd = DisplayManager.GetElementDisplay(foreign.getType().getFullForeignType());
					DisplayVersion foreignDV = null;
					if (! versionExtension.equalsIgnoreCase(""))
					{
						foreignDV = foreignEd.getVersion(version + "_" + versionExtension,version);
					}else{
						foreignDV = foreignEd.getVersion(version,"default");
					}

					visibleFields.addAll(foreignDV.getVisibleFields());
				}
			}
		}else{
		    visibleFields = this.getFields().getSortedVisibleFields();
		}

		return visibleFields;
	}
}

