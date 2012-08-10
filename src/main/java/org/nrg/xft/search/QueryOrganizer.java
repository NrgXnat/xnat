//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jun 24, 2005
 *
 */
package org.nrg.xft.search;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.xdat.display.Arc;
import org.nrg.xdat.display.ArcDefinition;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayFieldElement;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTMappingColumn;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.references.XFTRelationSpecification;
import org.nrg.xft.references.XFTSuperiorReference;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryOrganizer implements QueryOrganizerI{
	static org.apache.log4j.Logger logger = Logger.getLogger(QueryOrganizer.class);
    protected SchemaElementI rootElement = null;
    protected UserI user = null;

    protected String level = ViewManager.DEFAULT_LEVEL;
    protected ArrayList<String> fields = new ArrayList<String>();
	protected Hashtable tables = new Hashtable();

    protected StringBuffer joins = new StringBuffer();

    protected Hashtable<String,String> fieldAliases= new Hashtable();
    protected Hashtable tableAliases= new Hashtable();

    private Hashtable tableColumnAlias = new Hashtable();

    private Hashtable externalFields = new Hashtable();
    private Hashtable externalFieldAliases = new Hashtable();
    protected boolean isMappingTable = false;

    CriteriaCollection where = null;

    public QueryOrganizer(String elementName, UserI u, String level) throws ElementNotFoundException
    {
        try {
            rootElement = GenericWrapperElement.GetElement(elementName);
        } catch (XFTInitException e) {
            logger.error("",e);
        }
        this.level=level;
        user = u;
        setPKField();
    }

    public QueryOrganizer(SchemaElementI se, UserI u, String level)
    {
        rootElement=se;
        user = u;
        this.level=level;
        setPKField();
    }

    private void setPKField()
    {
        Iterator keys = rootElement.getGenericXFTElement().getAllPrimaryKeys().iterator();
        while (keys.hasNext())
        {
            GenericWrapperField sf = (GenericWrapperField)keys.next();
            try {
                addField(sf.getXMLPathString(rootElement.getFullXMLName()));
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            }
        }
    }

    public void setIsMappingTable(boolean isMap)
    {
        isMappingTable=isMap;
    }

    protected void addDirectField(String xmlPath) throws ElementNotFoundException
    {
        fields.add(xmlPath);
    }

    /**
     * @param xmlPath
     * @throws ElementNotFoundException
     */
    public void addField(String xmlPath) throws ElementNotFoundException
    {
        xmlPath = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(xmlPath);

        if (StringUtils.GetRootElementName(xmlPath).equalsIgnoreCase(this.getRootElement().getFullXMLName()))
        {
            boolean found = false;
            for(String tField: fields)
            {
                if (tField.toLowerCase().equals(xmlPath.toLowerCase()))
                {
                    found= true;
                    break;
                }
            }
            if (! found)
            {
                fields.add(xmlPath);
            }
        }else{
            SchemaElementI foreign = StringUtils.GetRootElement(xmlPath);

            ArrayList al = (ArrayList)externalFields.get(foreign.getFullXMLName());

            if (al == null)
            {
                al = new ArrayList();
            }

            Iterator fieldIter  = al.iterator();
            boolean found = false;
            while(fieldIter.hasNext())
            {
                String tField = (String)fieldIter.next();
                if (tField.toLowerCase().equals(xmlPath.toLowerCase()))
                {
                    found= true;
                    break;
                }
            }
            if (! found)
            {
                al.add(xmlPath);
                externalFields.put(foreign.getFullXMLName(),al);
            }
        }

    }

    /**
     * @param elementName
     * @return
     * @throws ElementNotFoundException
     */
    public String getFilterField(String elementName) throws ElementNotFoundException
    {
        try {
            return GenericWrapperElement.GetElement(elementName).getFilterField();
        } catch (XFTInitException e) {
            logger.error("",e);
            return null;
        }
    }

    /**
     * @param xmlPath
     * @return
     */
    public String getTableAndFieldSQL(String xmlPath) throws FieldNotFoundException
    {
            String[] layers = GenericWrapperElement.TranslateXMLPathToTables(xmlPath);
            String s = layers[1];
            String tableName = s.substring(s.lastIndexOf(".")+1);
            if (tableAliases.get(tableName)!=null)
            {
                tableName = (String)tableAliases.get(tableName);
            }else
            {
                String tableNamePath = layers[0];
                if (tables.get(tableNamePath) != null)
                {
                    tableName = (String)tables.get(tableNamePath);
                }
            }

            return tableName+ "." + layers[2];
    }

    /**
     * @param tableAlias
     * @param xmlPath
     * @return
     */
    public static String GetTableAndFieldSQL(String tableAlias, String xmlPath) throws FieldNotFoundException
    {
        String[] layers = GenericWrapperElement.TranslateXMLPathToTables(xmlPath);
	    
        String tableName = null;
        if (layers[1].indexOf(".")!=-1){
            tableName= layers[1].substring(layers[1].lastIndexOf(".") + 1);
        }else{
            tableName = layers[1];
        }
        String rootElement = StringUtils.GetRootElementName(xmlPath);
        String viewColumnName="";
        try {
            SchemaElement se = SchemaElement.GetElement(rootElement);
            viewColumnName = ViewManager.GetViewColumnName(se.getGenericXFTElement(),xmlPath,ViewManager.ACTIVE,true,true);
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
        return tableAlias + "." + StringUtils.CreateAlias(tableName,viewColumnName);
    }

    /**
     * Builds query which gets all necessary fields from the db for the generation of the Display Fields
     * (Secured).
     * @return
     */
    public String buildJoin() throws Exception
    {
        joins = new StringBuffer();
        tables = new Hashtable();
        //ADD FIELDS
        for (String s:fields)
        {
            try {
                String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                addFieldToJoin(layers);
            } catch (FieldNotFoundException e) {
            	e.printStackTrace();
            	throw new FieldNotFoundException(s);
            } catch (Exception e) {
                logger.error(e);
                throw new FieldNotFoundException(s);
            }
        }

        Enumeration keys = externalFields.keys();
        while (keys.hasMoreElements())
        {
            String k = (String)keys.nextElement();
            SchemaElement foreign = SchemaElement.GetElement(k);

            ArrayList al = (ArrayList)externalFields.get(k);
            
            QueryOrganizer qo = new QueryOrganizer(foreign,null,ViewManager.ALL);

            Iterator eFieldIter = al.iterator();
            while(eFieldIter.hasNext())
            {
                String eXmlPath = (String)eFieldIter.next();
                qo.addField(eXmlPath);
            }
            
            
//              CHECK ARCS
                ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement,foreign);
				if (arcDefine!=null)
				{
				    joins.append(getArcJoin(arcDefine,qo,foreign));
                    tables.put(k,foreign.getSQLName());
                    
                    eFieldIter = al.iterator();
                    while(eFieldIter.hasNext())
                    {
                        String eXmlPath = (String)eFieldIter.next();
                        this.externalFieldAliases.put(eXmlPath.toLowerCase(),qo.translateXMLPath(eXmlPath,foreign.getSQLName()));
                    }
                    continue;
				}

            
                String[] connection = this.rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());

                if (connection != null)
                {
                    String localSyntax = connection[0];
                    String xmlPath = connection[1];

                    logger.info("JOINING: " + localSyntax + " to " + xmlPath);
                    SchemaFieldI gwf = null;
                    SchemaElementI extension=null;
                    if (localSyntax.indexOf(XFT.PATH_SEPERATOR)==-1)
                    {
                        extension = rootElement;

                        //MAPS DIRECTLY TO THE ROOT TABLE
                        Iterator pks = extension.getAllPrimaryKeys().iterator();
                        while (pks.hasNext())
                        {
                            SchemaFieldI sf = (SchemaFieldI)pks.next();
                            qo.addField(xmlPath + sf.getXMLPathString(""));

                            String[] layers = GenericWrapperElement.TranslateXMLPathToTables(localSyntax + sf.getXMLPathString(""));
                            addFieldToJoin(layers);
                        }


                        //BUILD CONNECTION FROM FOREIGN TO EXTENSION
                        String query = qo.buildQuery();
            			StringBuffer sb = new StringBuffer();
            			sb.append(" LEFT JOIN (").append(query);
            			sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");

            			pks = extension.getAllPrimaryKeys().iterator();
            			int pkCount=0;
                        while (pks.hasNext())
                        {
                            SchemaFieldI sf = (SchemaFieldI)pks.next();
                            if (pkCount++ != 0)
                            {
                                sb.append(" AND ");
                            }
                            String localCol = this.getTableAndFieldSQL(localSyntax + sf.getXMLPathString(""));
                            String foreignCol = qo.translateXMLPath(xmlPath + sf.getXMLPathString(""),foreign.getSQLName());

                			sb.append(localCol).append("=");
                			sb.append(foreignCol);
                        }
            			joins.append(sb.toString());

            			eFieldIter = al.iterator();
                        while(eFieldIter.hasNext())
                        {
                            String eXmlPath = (String)eFieldIter.next();
                            this.externalFieldAliases.put(eXmlPath.toLowerCase(),qo.translateXMLPath(eXmlPath,foreign.getSQLName()));
                        }
                    }else{
                        gwf = SchemaElement.GetSchemaField(localSyntax);
                        extension = gwf.getReferenceElement();

                        

                        QueryOrganizer mappingQO = new QueryOrganizer(this.rootElement,null,this.level);
                        if (this.where!=null){
                            mappingQO.setWhere(where);
                        }
                        mappingQO.setIsMappingTable(true);

                        Iterator pks = extension.getAllPrimaryKeys().iterator();
                        while (pks.hasNext())
                        {
                            SchemaFieldI sf = (SchemaFieldI)pks.next();
                            qo.addField(xmlPath + sf.getXMLPathString(""));

                            mappingQO.addField(localSyntax + sf.getXMLPathString(""));
                        }

                        Iterator pKeys = rootElement.getAllPrimaryKeys().iterator();
                        while (pKeys.hasNext())
                        {
                            SchemaFieldI pKey = (SchemaFieldI)pKeys.next();
                            mappingQO.addField(pKey.getXMLPathString(rootElement.getFullXMLName()));
                        }

            			StringBuffer sb = new StringBuffer();

            			//BUILD MAPPING TABLE
                        String mappingQuery = mappingQO.buildQuery();
                        sb.append(" LEFT JOIN (").append(mappingQuery);
            			sb.append(") AS ").append("map_" + foreign.getSQLName()).append(" ON ");
            			pKeys = rootElement.getAllPrimaryKeys().iterator();
            			int pkCount=0;
            			while (pKeys.hasNext())
                        {
                            SchemaFieldI pKey = (SchemaFieldI)pKeys.next();
                            if (pkCount++ != 0)
                            {
                                sb.append(" AND ");
                            }

                            String localCol = this.getTableAndFieldSQL(pKey.getXMLPathString(rootElement.getFullXMLName()));
                            String foreignCol = mappingQO.translateXMLPath(pKey.getXMLPathString(rootElement.getFullXMLName()),"map_" + foreign.getSQLName());

                			sb.append(localCol).append("=");
                			sb.append(foreignCol);
                        }


                        //BUILD CONNECTION FROM FOREIGN TO EXTENSION
                        String query = qo.buildQuery();
            			sb.append(" LEFT JOIN (").append(query);
            			sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");

            			pks = extension.getAllPrimaryKeys().iterator();
            			pkCount=0;
                        while (pks.hasNext())
                        {
                            SchemaFieldI sf = (SchemaFieldI)pks.next();
                            if (pkCount++ != 0)
                            {
                                sb.append(" AND ");
                            }
                            String localCol = mappingQO.translateXMLPath(localSyntax + sf.getXMLPathString(""),"map_" + foreign.getSQLName());
                            String foreignCol = qo.translateXMLPath(xmlPath + sf.getXMLPathString(""),foreign.getSQLName());

                			sb.append(localCol).append("=");
                			sb.append(foreignCol);
                        }
            			joins.append(sb.toString());

            			//SET EXTERNAL COLUMN ALIASES
            			eFieldIter = al.iterator();
                        while(eFieldIter.hasNext())
                        {
                            String eXmlPath = (String)eFieldIter.next();
                            this.externalFieldAliases.put(eXmlPath.toLowerCase(),qo.translateXMLPath(eXmlPath,foreign.getSQLName()));
                        }
                    }
                }else{
                    throw new Exception("Unable to join " + rootElement.getSQLName() + " AND " + foreign.getFullXMLName());
                }

        }

        return joins.toString();
    }

    public String buildQuery() throws IllegalAccessException,Exception
    {
        StringBuffer sb = new StringBuffer();

       // SQLClause securityClause = null;
		if (user != null)
		{
		    SQLClause coll = user.getCriteriaForBackendRead(rootElement);
			if (coll != null)
			{
			    if (coll.numClauses() > 0)
			    {
//			        securityClause = coll;
//			        Iterator criteriaIter = coll.getSchemaFields().iterator();
//					while (criteriaIter.hasNext())
//					{
//                        Object[] o = (Object[])criteriaIter.next();
//                        String s = (String)o[0];
//                        SchemaFieldI f = (SchemaFieldI) o[1];
//                        if (f==null)
//                            f=GenericWrapperElement.GetFieldForXMLPath(s);
//		                String fieldName = s.substring(s.lastIndexOf("/") + 1);
//		                if (!f.getId().equalsIgnoreCase(fieldName))
//		                {
//		                    if (f.isReference())
//		                    {
//		                        GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
//		                        GenericWrapperField sf = (GenericWrapperField)foreign.getAllPrimaryKeys().get(0);
//		                        s = sf.getXMLPathString(s);
//		                    }
//		                }
//					    addField(s);
//					}
			    }else{
			        throw new IllegalAccessException("No defined read privileges for " + rootElement.getFullXMLName());
			    }
			}
		}


        String join = buildJoin();
        fieldAliases = new Hashtable<String,String>();
        sb.append("SELECT ");
        Iterator fieldIter = getAllFields().iterator();
        int counter=0;
        ArrayList selected = new ArrayList();
        while (fieldIter.hasNext())
        {
            String s = (String) fieldIter.next();
            String lowerCase = s.toLowerCase();
            if (!selected.contains(lowerCase))
            {
                selected.add(lowerCase);
                String element = StringUtils.GetRootElementName(s);
                GenericWrapperElement se = GenericWrapperElement.GetElement(element);
                if (rootElement.getFullXMLName().equalsIgnoreCase(se.getFullXMLName()))
                {
                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".")+1);
                    String colName = layers[2];

                    String viewColName = ViewManager.GetViewColumnName(se,s,ViewManager.ACTIVE,true,true);


                    if (tableAliases.get(tableName)!= null)
                    {
                        tableName = (String)tableAliases.get(tableName);
                    }
                    String alias = "";
                    if (viewColName==null)
                    {
                        alias = StringUtils.CreateAlias(tableName,colName);
                    }else{
                        alias = StringUtils.Last62Chars(viewColName);
                    }

                    String tableNameLC= tableName.toLowerCase();
                    String colNameLC= colName.toLowerCase();

                    if (!selected.contains(tableNameLC + "_" + colNameLC))
                    {
                        selected.add(tableNameLC + "_" + colNameLC);
                        if (counter++==0)
                        {
                            sb.append(tableName).append(".").append(colName).append(" AS ").append(alias);
                        }else{
                            sb.append(", ").append(tableName).append(".").append(colName).append(" AS ").append(alias);
                        }

                        fieldAliases.put(lowerCase,alias);
                        tableColumnAlias.put(tableNameLC + "_" + colNameLC,alias);
                    }else{
                        if (tableColumnAlias.get(tableNameLC + "_" + colNameLC) != null)
                        {
                            alias = (String)tableColumnAlias.get(tableNameLC + "_" + colNameLC);
                        }
                        fieldAliases.put(lowerCase,alias);
                    }
                }else{
                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".")+1);
                    String colName = layers[2];

                    String viewColName = ViewManager.GetViewColumnName(se,s,ViewManager.ACTIVE,true,true);


                    if (tableAliases.get(tableName)!= null)
                    {
                        tableName = (String)tableAliases.get(tableName);
                    }
                    String alias = "";
                    if (viewColName!=null)
                    {
                        alias = StringUtils.CreateAlias(tableName,viewColName);
                    }else{
                        alias = StringUtils.CreateAlias(tableName,colName);
                    }

                    String tableNameLC= tableName.toLowerCase();
                    String colNameLC= colName.toLowerCase();

                    if (!selected.contains(tableNameLC + "_" + colNameLC))
                    {
                        selected.add(tableNameLC + "_" + colNameLC);
	                    if (counter++==0)
	                    {
	                        sb.append(se.getSQLName()).append(".").append(tableName + "_" + colName).append(" AS ").append(alias);
	                    }else{
	                        sb.append(", ").append(se.getSQLName()).append(".").append(tableName + "_" + colName).append(" AS ").append(alias);
	                    }

	                    fieldAliases.put(lowerCase,alias);
                        tableColumnAlias.put(tableNameLC + "_" + colNameLC,alias);
                    }else{
                        if (tableColumnAlias.get(tableNameLC + "_" + colNameLC) != null)
                        {
                            alias = (String)tableColumnAlias.get(tableNameLC + "_" + colNameLC);
                        }
                        fieldAliases.put(lowerCase,alias);
                    }
                }
            }
        }

        Iterator eFields = this.getExternalFieldXMLPaths().iterator();
        while (eFields.hasNext())
        {
            String s = (String)eFields.next();
            String sLC=s.toLowerCase();

            String alias = (String)this.externalFieldAliases.get(sLC);
            String localAlias=alias.substring(alias.indexOf(".")+1);

            sb.append(", ").append(alias).append(" AS ").append(localAlias);

            fieldAliases.put(sLC,localAlias);
        }

        sb.append(join);

//        if (securityClause!=null)
//        {
//            return "SELECT * FROM (" + sb.toString() + ") SEARCH WHERE " +securityClause.getSQLClause(this);
//        }else{
            String select = sb.toString();
    		return select;
//        }
    }

    public ArrayList getAllFields()
    {
        ArrayList al = new ArrayList();
        al.addAll(fields);
        al.trimToSize();
        return al;
    }

    protected void addFieldToJoin(String s) throws Exception
    {
		String[] layer = GenericWrapperElement.TranslateXMLPathToTables(s);
		addFieldToJoin(layer);
    }

    protected String getRootQuery(String elementName,String sql_name,String level)throws IllegalAccessException{
        try {
            GenericWrapperElement e = GenericWrapperElement.GetElement(elementName);
            return getRootQuery(e,sql_name,level);
        } catch (XFTInitException e) {
            logger.error("",e);
            return null;
        } catch (ElementNotFoundException e) {
            logger.error("",e);
            return null;
        }
    }

    protected String getRootQuery(GenericWrapperElement e,String sql_name,String level)throws IllegalAccessException{
        QueryOrganizer securityQO = null;
        SQLClause coll = null;

        try {
            try {
                if (user==null){
                    if (where!=null && where.numClauses()>0){
                      coll =where;
                    }
                }else{
                    coll = ((XDATUser)user).getCriteriaForDisplayRead(e);
                }


                if (coll ==null || coll.numClauses()==0){
                    if (where!=null && where.numClauses()>0){
                        coll =where;
                      }
                }
            } catch (IllegalAccessException e1) {
                logger.error("",e1);
                coll = new org.nrg.xdat.search.CriteriaCollection("AND");
            }

            if (coll != null)
            {
                if (coll.numClauses() > 0)
                {
                    if (where!=null && where.numClauses()>0){
                        CriteriaCollection newColl = new CriteriaCollection("AND");
                        newColl.add(where);
                        newColl.add(coll);

                        //add support for limited status reflected in search results
                        if (!StringUtils.IsEmpty(level) && !level.equals(ViewManager.ALL)){
                            CriteriaCollection inner = new CriteriaCollection("OR");
                        	for(String l: StringUtils.CommaDelimitedStringToArrayList(level)){
                            	inner.addClause(e.getFullXMLName()+"/meta/status", l);
                        	}
                            newColl.add(inner);
                        }
                        
                        coll = newColl;
                    }else if (!StringUtils.IsEmpty(level) && !level.equals(ViewManager.ALL)){
                        CriteriaCollection newColl = new CriteriaCollection("AND");
                        newColl.add(coll);
                        
                        //add support for limited status reflected in search results
                        CriteriaCollection inner = new CriteriaCollection("OR");
                    	for(String l: StringUtils.CommaDelimitedStringToArrayList(level)){
                        	inner.addClause(e.getFullXMLName()+"/meta/status", l);
                    	}
                        newColl.add(inner);
                                           
                        coll = newColl;
                    }

                    securityQO = new QueryOrganizer(rootElement,null,ViewManager.ALL);
                    Iterator criteriaIter = coll.getSchemaFields().iterator();
                    while (criteriaIter.hasNext())
                    {
                        Object[] o = (Object[])criteriaIter.next();
                        String s = (String)o[0];
                        SchemaFieldI f = (SchemaFieldI) o[1];
                        if (f==null)
                            f=GenericWrapperElement.GetFieldForXMLPath(s);
                        String fieldName = s.substring(s.lastIndexOf("/") + 1);
                        String id = f.getId();
                        if (!id.equalsIgnoreCase(fieldName))
                        {
                            if (f.isReference())
                            {
                                GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
                                GenericWrapperField sf = (GenericWrapperField)foreign.getAllPrimaryKeys().get(0);
                                s = sf.getXMLPathString(s);
                            }
                        }
                        securityQO.addField(s);
                    }

                    Iterator keys = rootElement.getAllPrimaryKeys().iterator();
                    ArrayList keyXMLFields = new ArrayList();
                    while (keys.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)keys.next();
                        String key =sf.getXMLPathString(rootElement.getFullXMLName());
                        keyXMLFields.add(key);
                        securityQO.addField(key);
                    }
                    String subQuery = securityQO.buildQuery();
                    String securityQuery = "SELECT DISTINCT ON (";

                    for (int i=0;i<keyXMLFields.size();i++){
                        if (i>0)securityQuery +=", ";
                        securityQuery+=securityQO.getFieldAlias((String)keyXMLFields.get(i));
                    }

                    securityQuery +=") * FROM (" + subQuery + ") SECURITY WHERE ";
                    securityQuery +=coll.getSQLClause(securityQO);

                    String query = "SELECT SEARCH.* FROM ("+ securityQuery +") SECURITY LEFT JOIN " + e.getSQLName() + " SEARCH ON ";

                    keys = rootElement.getAllPrimaryKeys().iterator();
                    int keyCounter=0;
                    while (keys.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)keys.next();
                        String key =sf.getXMLPathString(rootElement.getFullXMLName());
                        if (keyCounter++>0){
                            query += " AND ";
                        }
                        query +="SECURITY." + securityQO.getFieldAlias(key) + "=SEARCH." + sf.getSQLName();

                    }
                    return "(" +query + ")";
                }else{
                    String query = "SELECT * FROM " + sql_name;
                    Iterator keys = rootElement.getAllPrimaryKeys().iterator();
                    int keyCount =0;
                    while (keys.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)keys.next();

                        if (keyCount++>0){
                            query +=" AND ";
                        }else{
                            query +=" WHERE ";
                        }

                        query+= sf.getSQLName() + " IS NULL ";
                    }
                    return "(" +query + ")";
                    //throw new IllegalAccessException("No defined read privileges for " + rootElement.getFullXMLName());
                }
            }
        } catch (IllegalAccessException e1) {
            logger.error("",e1);
            throw e1;
        } catch (XFTInitException e1) {
            logger.error("",e1);
        } catch (ElementNotFoundException e1) {
            logger.error("",e1);
        } catch (FieldNotFoundException e1) {
            logger.error("",e1);
        } catch (Exception e1) {
            logger.error("",e1);
        }

        return sql_name;
    }

    protected void addFieldToJoin(String[] layers) throws Exception
    {
	    String tableString = layers[0];
	    if (tables.get(tableString)==null)
	    {
		    if (tableString.indexOf(".")==-1)
		    {
		        String elementName = tableString.substring(tableString.lastIndexOf("]")+1);
		        if (rootElement.getFullXMLName().equalsIgnoreCase(elementName))
		        {
		            tables.put(tableString,layers[1]);
		            joins.append(" FROM ").append(getRootQuery(elementName,layers[1],this.getLevel())).append(" ").append(layers[1]);
		        }else{
		            throw new Exception("Improper initialization of QueryOrganizer");
		        }
		    }else{

                    String foreignAlias = layers[1].substring(layers[1].lastIndexOf(".") + 1);
                    String subString = tableString.substring(0,tableString.lastIndexOf("."));
                    String joinTable = tableString.substring(tableString.lastIndexOf(".") + 1);

                    String [] layers1 = new String[4];
                    layers1[0]= subString;
                    if (layers[1].indexOf(".")>-1)
                    {
                        layers1[1]= layers[1].substring(0,layers[1].lastIndexOf("."));
                    }
                    if (layers[3].indexOf(".")==-1)
                    {
                        layers1[3] = "";
                    }else{
                        layers1[3]= layers[3].substring(0,layers[3].lastIndexOf("."));
                    }

                    addFieldToJoin(layers1);

                    //JOIN TO joinTable
                    String rootTable = (String)tables.get(subString);
                    if (tableAliases.get(rootTable)!=null)
                    {
                        rootTable = (String)tableAliases.get(rootTable);
                    }

                    String rootElementName = null;

                    if (subString.indexOf("]")==-1)
                    {
                        rootElementName = subString;
                    }else{
                        rootElementName = subString.substring(subString.lastIndexOf("]")+1);
                    }

                    GenericWrapperElement rootElement = getGenericWrapperElement(rootElementName);

                    String foreignFieldName=null;
                    String foreignElementName = null;

                    if (joinTable.indexOf("]")==-1)
                    {
                        foreignElementName = joinTable;
                    }else{
                        foreignElementName = joinTable.substring(joinTable.lastIndexOf("]")+1);
                        foreignFieldName=joinTable.substring(joinTable.lastIndexOf("[")+1,joinTable.lastIndexOf("]"));
                    }

                    GenericWrapperElement joinElement = getGenericWrapperElement(foreignElementName);

                    String join = getJoinClause(rootElement,joinElement,rootTable,foreignAlias,layers,foreignFieldName);
                    joins.append(" ").append(join);
                    if (tableAliases.get(foreignAlias)!=null)
                    {
                        tables.put(tableString,(String)tableAliases.get(foreignAlias));
                    }else{
                        tables.put(tableString,foreignAlias);
                    }
		    }
	    }else{
	        String tableAlias = layers[1];
	        if (tableAlias.indexOf(".")!=-1)
	        {
	            tableAlias = tableAlias.substring(tableAlias.lastIndexOf(".") + 1);
	        }
	        if (tableAliases.get(tableAlias)==null)
	        {
	            tableAliases.put(tableAlias,tables.get(tableString));
	        }
	    }
    }

    /**
     * Returns String []{0:SQL JOIN CLAUSE,1:FOREIGN ALIAS}
     * @param root
     * @param foreign
     * @param rootAlias
     * @return
     */
    private String getJoinClause(GenericWrapperElement root, GenericWrapperElement foreign, String rootAlias,String foreignAlias, String[] layer, String relativeFieldRef) throws Exception
    {
    	if(!tableAliases.containsKey(foreignAlias))
    		tableAliases.put(foreignAlias,"table"+ tableAliases.size());
        foreignAlias = (String)tableAliases.get(foreignAlias);

        String last = null;
        if (layer[3].indexOf(".")==-1)
        {
            last = layer[3];
        }else{
            last = layer[3].substring(layer[3].lastIndexOf(".") + 1);
        }
        GenericWrapperField f=null;
        if (relativeFieldRef==null)
        {
            if(last.endsWith("EXT"))
            {
                f = root.getExtensionField();
            }else{
                f = root.getField(last);
            }
        }else{
            f = root.getField(relativeFieldRef);
            if (f==null)
            {
                if(last.endsWith("EXT"))
                {
                    f = root.getExtensionField();
                }else{
                    f = root.getField(last);
                }
            }
        }

        if(f==null && foreign.isExtensionOf(root)){
        	f= foreign.getExtensionField();
        }
        
        if (f==null)
        {
            f=root.getField(foreign.getXSIType());
        }
        
        if (f == null)
        {
            throw new FieldNotFoundException("");
        }

        String j = " ";

        XFTReferenceI ref = f.getXFTReference();
        if (ref.isManyToMany())
        {
            XFTManyToManyReference many = (XFTManyToManyReference)ref;
            String mapTableName = many.getMappingTable() + tableAliases.size();
            j+= " LEFT JOIN " + many.getMappingTable() + " AS "+ mapTableName;
            j+= " ON ";

            Iterator iter =many.getMappingColumnsForElement(root).iterator();
		    int counter = 0;
		    while(iter.hasNext())
		    {
		        XFTMappingColumn map = (XFTMappingColumn)iter.next();
		        if (counter++==0)
	            {
	                j += " " + rootAlias + "." + map.getForeignKey().getSQLName() + "=" + mapTableName +"." + map.getLocalSqlName();
	            }else{
	                j += " AND " + rootAlias + "." + map.getForeignKey().getSQLName() + "=" + mapTableName +"." + map.getLocalSqlName();
	            }
		    }
            j+= " LEFT JOIN " + foreign.getSQLName() + " " + foreignAlias;
            j+= " ON ";
		    iter =many.getMappingColumnsForElement(foreign).iterator();
		    counter = 0;
		    while(iter.hasNext())
		    {
		        XFTMappingColumn map = (XFTMappingColumn)iter.next();
		        if (counter++==0)
	            {
	                j += " " + mapTableName +"." + map.getLocalSqlName() + "=" + foreignAlias + "." + map.getForeignKey().getSQLName();
	            }else{
	                j += " AND " + mapTableName +"." + map.getLocalSqlName() + "=" + foreignAlias + "." + map.getForeignKey().getSQLName();
	            }
		    }
        }else{
            j+= " LEFT JOIN " + foreign.getSQLName() + " " + foreignAlias;
            j+= " ON ";
            XFTSuperiorReference sup = (XFTSuperiorReference)ref;
            Iterator iter = sup.getKeyRelations().iterator();
            int counter=0;
            while (iter.hasNext())
            {
                XFTRelationSpecification spec = (XFTRelationSpecification)iter.next();
                if (spec.getLocalTable().equalsIgnoreCase(root.getSQLName()))
                {
                    if (counter!=0)
                        j += " AND ";
                    j += rootAlias + "." + spec.getLocalCol();
                    j+= "=" + foreignAlias + "." + spec.getForeignCol();
                }else{
                    if (counter!=0)
                        j += " AND ";
                    j += rootAlias + "." + spec.getForeignCol();
                    j+= "=" + foreignAlias + "." + spec.getLocalCol();
                }
            }
        }
        return j;
    }

    private GenericWrapperElement getGenericWrapperElement(String s) throws Exception
    {
        if (s.indexOf(".")!=-1)
        {
            s= s.substring(s.lastIndexOf(".")+1);
        }
        return GenericWrapperElement.GetElement(s);
    }

    /**
     * @return Returns the fields.
     */
    public ArrayList getFields() {
        return fields;
    }
    /**
     * @param fields The fields to set.
     */
    public void setFields(ArrayList fields) {
        this.fields = fields;
    }
    /**
     * @return Returns the rootElement.
     */
    public GenericWrapperElement getRootElement() {
        return rootElement.getGenericXFTElement();
    }
    /**
     * @param rootElement The rootElement to set.
     */
    public void setRootElement(SchemaElementI rootElement) {
        this.rootElement = rootElement;
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

    public Hashtable<String,String> getFieldAliass()
    {
        return fieldAliases;
    }

    public String getFieldAlias(String xmlPath)
    {
        return fieldAliases.get(xmlPath.toLowerCase());
    }

    public String getFieldAlias(String xmlPath,String tableAlias) throws FieldNotFoundException
    {
        if (xmlPath.startsWith("VIEW_"))
        {
            xmlPath = xmlPath.substring(5);
        }
        if (fieldAliases.get(xmlPath.toLowerCase())==null)
        {
            return QueryOrganizer.GetTableAndFieldSQL(tableAlias,xmlPath);
        }else{
            return tableAlias + "." + fieldAliases.get(xmlPath.toLowerCase());
        }
    }
    
    public String getXPATHforAlias(String alias){
    		for(Map.Entry<String, String> entry: this.fieldAliases.entrySet()){
    			if(entry.getValue().equalsIgnoreCase(alias)){
    				return entry.getKey();
    			}
    		}
    	
    	return null;
    }

    public String translateXMLPath(String xmlPath) throws FieldNotFoundException
    {
        //  org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::1");
        if (xmlPath.startsWith("VIEW_"))
        {
            xmlPath = xmlPath.substring(5);
        }else{
            try {
                //          org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::2");
                GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
                String fieldName = xmlPath.substring(xmlPath.lastIndexOf("/") + 1);
                String id = f.getId();
                if (!id.equalsIgnoreCase(fieldName))
                {
                    if (f.isReference())
                    {
                        GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
                        GenericWrapperField sf = (GenericWrapperField)foreign.getAllPrimaryKeys().get(0);
                        xmlPath = sf.getXMLPathString(xmlPath);
                    }
                }
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        //    org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::3");
		try {
            String temp = ViewManager.GetViewColumnName(rootElement.getGenericXFTElement(),xmlPath,ViewManager.DEFAULT_LEVEL,true,true);
            if (temp !=null)
            {
                //           org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::4");
                return temp;
            }
        } catch (XFTInitException e1) {
            logger.error("",e1);
        } catch (ElementNotFoundException e1) {
            logger.error("",e1);
        }

        //     org.nrg.xft.XFT.LogCurrentTime("translateXMLPath::5");
        if (fieldAliases.get(xmlPath.toLowerCase())==null)
        {
            return getTableAndFieldSQL(xmlPath);
        }else{
            return fieldAliases.get(xmlPath.toLowerCase());
        }
    }

    public String translateStandardizedPath(String xmlPath) throws FieldNotFoundException{

        if (fieldAliases.get(xmlPath.toLowerCase())==null)
        {
            return getTableAndFieldSQL(xmlPath);
        }else{
            return fieldAliases.get(xmlPath.toLowerCase());
        }
    }

    public String translateXMLPath(String xmlPath,String tableAlias) throws FieldNotFoundException
    {
        if (xmlPath.startsWith("VIEW_"))
        {
            xmlPath = xmlPath.substring(5);
        }else{
            try {
                GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(xmlPath);
                String fieldName = xmlPath.substring(xmlPath.lastIndexOf("/") + 1);
                if (!f.getId().equalsIgnoreCase(fieldName))
                {
                    if (f.isReference())
                    {
                        GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
                        GenericWrapperField sf = (GenericWrapperField)foreign.getAllPrimaryKeys().get(0);
                        xmlPath = sf.getXMLPathString(xmlPath);
                    }
                }
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        if (tableAlias==null || tableAlias.equalsIgnoreCase(""))
        {
            return translateXMLPath(xmlPath);
        }
        if (fieldAliases.get(xmlPath.toLowerCase())==null)
        {
            return QueryOrganizer.GetTableAndFieldSQL(tableAlias,xmlPath);
        }else{
            return tableAlias + "." + fieldAliases.get(xmlPath.toLowerCase());
        }
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

    public ArrayList getExternalFieldXMLPaths()
    {
        ArrayList _return = new ArrayList();
        Enumeration keys = externalFields.keys();
        while (keys.hasMoreElements())
        {
            String k = (String)keys.nextElement();

            ArrayList al = (ArrayList)externalFields.get(k);
            _return.addAll(al);
        }

        return _return;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getRootElement().getFullXMLName()).append("\n");

        Iterator iter =this.getAllFields().iterator();
        while (iter.hasNext())
        {
            String s = (String)iter.next();
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    /**
     * @return the where
     */
    public CriteriaCollection getWhere() {
        return where;
    }

    /**
     * @param where the where to set
     */
    public void setWhere(CriteriaCollection where) {
        this.where = where;
    }


    public String getArcJoin(ArcDefinition arcDefine,QueryOrganizerI qo,SchemaElement foreign) throws Exception
    {
        StringBuffer sb = new StringBuffer();
        if (arcDefine.getBridgeElement().equalsIgnoreCase(rootElement.getFullXMLName()))
		{
			Arc foreignArc = (Arc)foreign.getArcs().get(arcDefine.getName());
			String rootField = arcDefine.getBridgeField();

			String foreignField = (String)foreignArc.getCommonFields().get(arcDefine.getEqualsField());

			foreign.getDisplay().getDisplayField(foreignField);

			DisplayField df = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayFieldElement dfe =(DisplayFieldElement)df.getElements().get(0);
			String localFieldElement = dfe.getSchemaElementName();

			if (StringUtils.GetRootElementName(localFieldElement).equalsIgnoreCase(getRootElement().getFullXMLName()))
			{

				this.addField(dfe.getSchemaElementName());

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = getTableAndFieldSQL(dfe.getSchemaElementName());

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 =(DisplayFieldElement)df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (StringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
				{
					qo.addField(dfe2.getSchemaElementName());

					String query = qo.buildQuery();

					String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName());

					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					sb.append(localCol).append("=");
					sb.append(foreignCol);
				}else{
				    QueryOrganizer foreignMap = new QueryOrganizer(foreign,this.getUser(),ViewManager.ALL);

					foreignMap.addField(foreignFieldElement);

					Iterator pks = foreign.getAllPrimaryKeys().iterator();
	    			int pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    foreignMap.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                    qo.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                }

					String foreignMapQuery = foreignMap.buildQuery();
					String foreignMapName = "map_" + foreign.getSQLName();
					sb.append(" LEFT JOIN (").append(foreignMapQuery);
					sb.append(") AS ").append(foreignMapName).append(" ON ");

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
						pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String foreignC = foreignMap.translateXMLPath(foreignFieldElement,foreignMapName);

	        			sb.append(localCol).append("=");
	        			sb.append(foreignC);
	                }

					String query = qo.buildQuery();


					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String localC = foreignMap.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreignMapName);
	                    String foreignC = qo.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreign.getSQLName());

	        			sb.append(localC).append("=");
	        			sb.append(foreignC);
	                }
				}
			}else{
			    SchemaElementI middle = StringUtils.GetRootElement(localFieldElement);
			    QueryOrganizer localMap = new QueryOrganizer(rootElement,this.getUser(),ViewManager.ALL);
			    localMap.setIsMappingTable(true);
				localMap.addField(localFieldElement);

				Iterator pks = rootElement.getAllPrimaryKeys().iterator();
    			int pkCount=0;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    localMap.addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                    addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                }

				String localMapQuery = localMap.buildQuery();
				String localMapName = "map_" + rootElement.getSQLName() + "_" + middle.getSQLName();
				sb.append(" LEFT JOIN (").append(localMapQuery);
				sb.append(") AS ").append(localMapName).append(" ON ");

				pks = rootElement.getAllPrimaryKeys().iterator();
    			pkCount=0;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    if (pkCount++ != 0)
                    {
                        sb.append(" AND ");
                    }
                    String localCol = getTableAndFieldSQL(sf.getXMLPathString(rootElement.getFullXMLName()));
                    String foreignC = localMap.translateXMLPath(sf.getXMLPathString(rootElement.getFullXMLName()),localMapName);

        			sb.append(localCol).append("=");
        			sb.append(foreignC);
                }

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = localMap.translateXMLPath(localFieldElement,localMapName);

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 =(DisplayFieldElement)df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (StringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
				{
					qo.addField(dfe2.getSchemaElementName());

					String query = qo.buildQuery();

					String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName());

					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					sb.append(localCol).append("=");
					sb.append(foreignCol);
				}else{
				    QueryOrganizer foreignMap = new QueryOrganizer(foreign,this.getUser(),ViewManager.ALL);
					foreignMap.addField(foreignFieldElement);

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    foreignMap.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                    qo.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                }

					String foreignMapQuery = foreignMap.buildQuery();
					String foreignMapName = "map_" + foreign.getSQLName();
					sb.append(" LEFT JOIN (").append(foreignMapQuery);
					sb.append(") AS ").append(foreignMapName).append(" ON ");

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
						pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String foreignC = foreignMap.translateXMLPath(foreignFieldElement,foreignMapName);

	        			sb.append(localCol).append("=");
	        			sb.append(foreignC);
	                }

					String query = qo.buildQuery();


					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String localC = foreignMap.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreignMapName);
	                    String foreignC = qo.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreign.getSQLName());

	        			sb.append(localC).append("=");
	        			sb.append(foreignC);
	                }
				}
			}
		}else if (arcDefine.getBridgeElement().equalsIgnoreCase(foreign.getFullXMLName()))
		{
			Arc rootArc = (Arc)(new SchemaElement(rootElement.getGenericXFTElement())).getArcs().get(arcDefine.getName());

			String foreignField = arcDefine.getBridgeField();
			String rootField = (String)rootArc.getCommonFields().get(arcDefine.getEqualsField());

			DisplayField df = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayFieldElement dfe =(DisplayFieldElement)df.getElements().get(0);
			String localFieldElement = dfe.getSchemaElementName();

			if (StringUtils.GetRootElementName(localFieldElement).equalsIgnoreCase(getRootElement().getFullXMLName()))
			{

				this.addField(dfe.getSchemaElementName());

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = getTableAndFieldSQL(dfe.getSchemaElementName());

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 =(DisplayFieldElement)df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (StringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
				{
					qo.addField(dfe2.getSchemaElementName());

					String query = qo.buildQuery();

					String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName());

					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					sb.append(localCol).append("=");
					sb.append(foreignCol);
				}else{
				    QueryOrganizer foreignMap = new QueryOrganizer(foreign,this.getUser(),ViewManager.ALL);
					foreignMap.addField(foreignFieldElement);

					Iterator pks = foreign.getAllPrimaryKeys().iterator();
	    			int pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    foreignMap.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                    qo.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                }

					String foreignMapQuery = foreignMap.buildQuery();
					String foreignMapName = "map_" + foreign.getSQLName();
					sb.append(" LEFT JOIN (").append(foreignMapQuery);
					sb.append(") AS ").append(foreignMapName).append(" ON ");

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
						pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String foreignC = foreignMap.translateXMLPath(foreignFieldElement,foreignMapName);

	        			sb.append(localCol).append("=");
	        			sb.append(foreignC);
	                }

					String query = qo.buildQuery();


					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String localC = foreignMap.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreignMapName);
	                    String foreignC = qo.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreign.getSQLName());

	        			sb.append(localC).append("=");
	        			sb.append(foreignC);
	                }
				}
			}else{
			    SchemaElementI middle = StringUtils.GetRootElement(localFieldElement);
			    QueryOrganizer localMap = new QueryOrganizer(rootElement,this.getUser(),ViewManager.ALL);
			    localMap.setIsMappingTable(true);
			    localMap.addField(localFieldElement);

				Iterator pks = rootElement.getAllPrimaryKeys().iterator();
    			int pkCount=0;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    localMap.addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                    addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                }

				String localMapQuery = localMap.buildQuery();
				String localMapName = "map_" + rootElement.getSQLName() + "_" + middle.getSQLName();
				sb.append(" LEFT JOIN (").append(localMapQuery);
				sb.append(") AS ").append(localMapName).append(" ON ");

				pks = rootElement.getAllPrimaryKeys().iterator();
    			pkCount=0;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    if (pkCount++ != 0)
                    {
                        sb.append(" AND ");
                    }
                    String localCol = getTableAndFieldSQL(sf.getXMLPathString(rootElement.getFullXMLName()));
                    String foreignC = localMap.translateXMLPath(sf.getXMLPathString(rootElement.getFullXMLName()),localMapName);

        			sb.append(localCol).append("=");
        			sb.append(foreignC);
                }

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = localMap.translateXMLPath(localFieldElement,localMapName);

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 =(DisplayFieldElement)df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (StringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
				{
					qo.addField(dfe2.getSchemaElementName());

					String query = qo.buildQuery();

					String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName());

					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					sb.append(localCol).append("=");
					sb.append(foreignCol);
				}else{
				    QueryOrganizer foreignMap = new QueryOrganizer(foreign,this.getUser(),ViewManager.ALL);
					foreignMap.addField(foreignFieldElement);

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    foreignMap.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                    qo.addField(sf.getXMLPathString(foreign.getFullXMLName()));
	                }

					String foreignMapQuery = foreignMap.buildQuery();
					String foreignMapName = "map_" + foreign.getSQLName();
					sb.append(" LEFT JOIN (").append(foreignMapQuery);
					sb.append(") AS ").append(foreignMapName).append(" ON ");

					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String foreignC = foreignMap.translateXMLPath(foreignFieldElement,foreignMapName);

	        			sb.append(localCol).append("=");
	        			sb.append(foreignC);
	                }

					String query = qo.buildQuery();


					sb.append(" LEFT JOIN (").append(query);
					sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
					pks = foreign.getAllPrimaryKeys().iterator();
	    			pkCount=0;
	                while (pks.hasNext())
	                {
	                    SchemaFieldI sf = (SchemaFieldI)pks.next();
	                    if (pkCount++ != 0)
	                    {
	                        sb.append(" AND ");
	                    }
	                    String localC = foreignMap.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreignMapName);
	                    String foreignC = qo.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreign.getSQLName());

	        			sb.append(localC).append("=");
	        			sb.append(foreignC);
	                }
				}
			}
		}else
		{
			Arc rootArc = (Arc)(new SchemaElement(rootElement.getGenericXFTElement())).getArcs().get(arcDefine.getName());
			Arc foreignArc = (Arc)foreign.getArcs().get(arcDefine.getName());

			String distinctField = arcDefine.getDistinctField();

			String foreignField = (String)foreignArc.getCommonFields().get(distinctField);
			String rootField = (String)rootArc.getCommonFields().get(distinctField);

			String arcMapQuery = DisplayManager.GetArcDefinitionQuery(arcDefine,(new SchemaElement(rootElement.getGenericXFTElement())),foreign,user);
			String arcTableName = DisplayManager.ARC_MAP + rootElement.getSQLName()+"_"+foreign.getSQLName();

			DisplayField rDF = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayField fDF = foreign.getDisplayField(foreignField);
			this.addField(rDF.getPrimarySchemaField());
			//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(rDF.getPrimarySchemaField());
			//this.addFieldToJoin(layers);
			qo.addField(fDF.getPrimarySchemaField());

			sb.append(" LEFT JOIN (").append(arcMapQuery);
			sb.append(") ").append(arcTableName).append(" ON ").append(getTableAndFieldSQL(rDF.getPrimarySchemaField())).append("=").append(arcTableName);
			sb.append(".").append(rootElement.getSQLName()).append("_").append(distinctField);

			String query = qo.buildQuery();
			sb.append(" LEFT JOIN (").append(query);
			sb.append(") AS ").append(foreign.getSQLName()).append(" ON ").append(arcTableName);
			sb.append(".").append(foreign.getSQLName()).append("_");
			sb.append(distinctField).append("=").append(qo.translateXMLPath(fDF.getPrimarySchemaField(),foreign.getSQLName()));
		}
        return sb.toString();
    }
}
