/*
 * core: org.nrg.xft.search.QueryOrganizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.search;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.display.*;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

import java.util.*;

/**
 * @author Tim
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Slf4j
public class QueryOrganizer implements QueryOrganizerI{
    protected SchemaElementI       rootElement = null;
    protected UserI                user;

    protected String level;
    protected ArrayList<String> fields = new ArrayList<>();
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
            log.error("", e);
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

    private List<String> keys= new ArrayList<>();
    
    public List<String> getKeys(){
    	return keys;
    }
    
    private void setPKField()
    {
        for (final GenericWrapperField sf : rootElement.getGenericXFTElement().getAllPrimaryKeys()) {
            try {
            	String key=sf.getXMLPathString(rootElement.getFullXMLName());
                addField(key);
            	this.keys.add(key);
            } catch (ElementNotFoundException e) {
                log.error("", e);
            }
        }
    }

    public void setIsMappingTable(boolean isMap)
    {
        isMappingTable=isMap;
    }

    protected void addDirectField(String xmlPath)
    {
        fields.add(xmlPath);
    }

    /**
     * @param xmlPath    The XML path to add.
     * @throws ElementNotFoundException If the indicated element isn't found.
     */
    public void addField(String xmlPath) throws ElementNotFoundException
    {
        xmlPath = XftStringUtils.StandardizeXMLPath(xmlPath);

        final String rootElementName = getRootElement().getFullXMLName();
        if (XftStringUtils.GetRootElementName(xmlPath).equalsIgnoreCase(rootElementName))
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
            SchemaElementI foreign = XftStringUtils.GetRootElement(xmlPath);

            assert foreign != null;
            final String foreignFullXMLName = foreign.getFullXMLName();
            ArrayList    al          = (ArrayList)externalFields.get(foreignFullXMLName);

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
                if (rootElementName.equals("xnat:projectData")) {
                    log.debug("Adding foreign element '{}' to project from XMLPath '{}'", foreignFullXMLName, xmlPath, new Exception());
                }
                externalFields.put(foreignFullXMLName, al);
            }
        }

    }

    /**
     * @param elementName    The element name to retrieve.
     * @return The indicated filter field.
     * @throws ElementNotFoundException If the indicated element isn't found.
     */
    public String getFilterField(String elementName) throws ElementNotFoundException
    {
        try {
            return GenericWrapperElement.GetElement(elementName).getFilterField();
        } catch (XFTInitException e) {
            log.error("", e);
            return null;
        }
    }

    /**
     * @param xmlPath    The XML path to retrieve SQL for.
     * @return The SQL for the requested XML path.
     * @throws FieldNotFoundException If the field indicated by xmlPath isn't found.
     */
    public String getTableAndFieldSQL(String xmlPath) throws FieldNotFoundException {
        String[] layers = GenericWrapperElement.TranslateXMLPathToTables(xmlPath);
        assert layers != null;
        String s         = layers[1];
        String tableName = s.substring(s.lastIndexOf(".") + 1);
        if (tableAliases.get(tableName) != null) {
            tableName = (String) tableAliases.get(tableName);
        } else {
            String tableNamePath = layers[0];
            if (tables.get(tableNamePath) != null) {
                tableName = (String) tables.get(tableNamePath);
            }
        }

        return tableName + "." + layers[2];
    }

    /**
     * @param tableAlias    The alias for the table.
     * @param xmlPath       The XML path to retrieve SQL for.
     * @return The SQL for the requested XML path.
     * @throws FieldNotFoundException If the field indicated by xmlPath isn't found.
     */
    public static String GetTableAndFieldSQL(String tableAlias, String xmlPath) throws FieldNotFoundException
    {
        String[] layers = GenericWrapperElement.TranslateXMLPathToTables(xmlPath);
	    
        String tableName;
        assert layers != null;
        if (layers[1].contains(".")){
            tableName= layers[1].substring(layers[1].lastIndexOf(".") + 1);
        }else{
            tableName = layers[1];
        }
        String rootElement = XftStringUtils.GetRootElementName(xmlPath);
        String viewColumnName="";
        try {
            SchemaElement se = SchemaElement.GetElement(rootElement);
            viewColumnName = ViewManager.GetViewColumnName(se.getGenericXFTElement(),xmlPath,ViewManager.ACTIVE,true,true);
        } catch (XFTInitException | ElementNotFoundException e) {
            log.error("", e);
        }
        return tableAlias + "." + XftStringUtils.CreateAlias(tableName, viewColumnName);
    }

    /**
     * Builds query which gets all necessary fields from the db for the generation of the Display Fields
     * (Secured).
     * @return The join query.
     * @throws Exception When an error occurs.
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
            	throw e;
            } catch (Exception e) {
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

            for (final Object anAl : al) {
                String eXmlPath = (String) anAl;
                qo.addField(eXmlPath);
            }
            
            
//              CHECK ARCS
                ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement,foreign);
				if (arcDefine!=null)
				{
				    joins.append(getArcJoin(arcDefine,qo,foreign));
                    tables.put(k,foreign.getSQLName());

                    for (final Object anAl : al) {
                        String eXmlPath = (String) anAl;
                        this.externalFieldAliases.put(eXmlPath.toLowerCase(), qo.translateXMLPath(eXmlPath, foreign.getSQLName()));
                    }
                    continue;
				}

            
                String[] connection = this.rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());

                if (connection != null)
                {
                    String localSyntax = connection[0];
                    String xmlPath = connection[1];

                    log.info("JOINING: " + localSyntax + " to " + xmlPath);
                    SchemaFieldI gwf;
                    SchemaElementI extension;
                    if (localSyntax.indexOf(XFT.PATH_SEPARATOR) == -1)
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
                        String        query = qo.buildQuery();
            			StringBuilder sb    = new StringBuilder();
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

                        for (final Object anAl : al) {
                            String eXmlPath = (String) anAl;
                            this.externalFieldAliases.put(eXmlPath.toLowerCase(), qo.translateXMLPath(eXmlPath, foreign.getSQLName()));
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

            			StringBuilder sb = new StringBuilder();

            			//BUILD MAPPING TABLE
                        String mappingQuery = mappingQO.buildQuery();
                        sb.append(" LEFT JOIN (").append(mappingQuery);
            			sb.append(") AS ").append("map_").append(foreign.getSQLName()).append(" ON ");
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
                        for (final Object anAl : al) {
                            String eXmlPath = (String) anAl;
                            this.externalFieldAliases.put(eXmlPath.toLowerCase(), qo.translateXMLPath(eXmlPath, foreign.getSQLName()));
                        }
                    }
                }else{
                    throw new Exception("Unable to join " + rootElement.getSQLName() + " AND " + foreign.getFullXMLName());
                }

        }

        return joins.toString();
    }

    public String buildQuery() throws Exception
    {
        StringBuilder sb = new StringBuilder();

       // SQLClause securityClause = null;
		if (user != null)
		{
		    log.debug("Get user {}'s XFT read criteria for element {}", user.getUsername(), rootElement.getFormattedName());
		    SQLClause coll = Permissions.getCriteriaForXFTRead(user,rootElement);
			if (coll != null)
			{
			    if (coll.numClauses() == 0) {
			        throw new IllegalAccessException("No defined read privileges for " + rootElement.getFullXMLName());
			    }
			}
		}


        String join = buildJoin();
        fieldAliases = new Hashtable<>();
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
                String element = XftStringUtils.GetRootElementName(s);
                GenericWrapperElement se = GenericWrapperElement.GetElement(element);
                if (rootElement.getFullXMLName().equalsIgnoreCase(se.getFullXMLName()))
                {
                    log.info("Found matching root and element names: {}", rootElement.getFullXMLName());
                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                    assert layers != null;
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".") + 1);
                    String colName   = layers[2];

                    String viewColName = ViewManager.GetViewColumnName(se,s,ViewManager.ACTIVE,true,true);


                    if (tableAliases.get(tableName)!= null)
                    {
                        tableName = (String)tableAliases.get(tableName);
                    }
                    String alias;
                    if (viewColName==null)
                    {
                        alias = XftStringUtils.CreateAlias(tableName, colName);
                    }else{
                        alias = XftStringUtils.Last62Chars(viewColName);
                    }

                    String tableNameLC= tableName.toLowerCase();
                    String colNameLC= colName.toLowerCase();

                    final String tableColumnReference = tableNameLC + "_" + colNameLC;
                    if (!selected.contains(tableColumnReference))
                    {
                        selected.add(tableColumnReference);
                        if (counter++==0)
                        {
                            sb.append(tableName).append(".").append(colName).append(" AS ").append(alias);
                        }else{
                            sb.append(", ").append(tableName).append(".").append(colName).append(" AS ").append(alias);
                        }

                        fieldAliases.put(lowerCase, alias);
                        tableColumnAlias.put(tableColumnReference, alias);
                        log.info("Matching root and element: Stored table '{}' column '{}' and table-column reference '{}' as field alias '{}'", tableName, colName, tableColumnReference, alias);
                    }else{
                        if (tableColumnAlias.get(tableColumnReference) != null)
                        {
                            log.info("Matching root and element: Found existing table-column reference '{}' for table '{}' column '{}', calculated alias is '{}', cached alias is '{}'", tableColumnReference, tableName, colName, alias, tableColumnAlias.get(tableColumnReference));
                            alias = (String)tableColumnAlias.get(tableColumnReference);
                        }
                        fieldAliases.put(lowerCase,alias);
                    }
                }else{
                    log.info("Found non-matching root and element names: '{}' vs '{}'", rootElement.getFullXMLName(), se.getFullXMLName());
                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                    assert layers != null;
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".") + 1);
                    String colName   = layers[2];

                    String viewColName = ViewManager.GetViewColumnName(se,s,ViewManager.ACTIVE,true,true);


                    if (tableAliases.get(tableName)!= null)
                    {
                        tableName = (String)tableAliases.get(tableName);
                    }
                    String alias;
                    if (viewColName!=null)
                    {
                        alias = XftStringUtils.CreateAlias(tableName, viewColName);
                    }else{
                        alias = XftStringUtils.CreateAlias(tableName, colName);
                    }

                    String tableNameLC= tableName.toLowerCase();
                    String colNameLC= colName.toLowerCase();

                    final String tableColumnReference = tableNameLC + "_" + colNameLC;
                    if (!selected.contains(tableColumnReference))
                    {
                        selected.add(tableColumnReference);
	                    if (counter++==0)
	                    {
	                        sb.append(se.getSQLName()).append(".").append(tableName).append("_").append(colName).append(" AS ").append(alias);
	                    }else{
	                        sb.append(", ").append(se.getSQLName()).append(".").append(tableName).append("_").append(colName).append(" AS ").append(alias);
	                    }

	                    fieldAliases.put(lowerCase,alias);
                        tableColumnAlias.put(tableColumnReference, alias);
                        log.info("Non-matching root and element: Stored table '{}' column '{}' and table-column reference '{}' as field alias '{}'", tableName, colName, tableColumnReference, alias);
                    }else{
                        if (tableColumnAlias.get(tableColumnReference) != null)
                        {
                            log.info("Matching root and element: Found existing table-column reference '{}' for table '{}' column '{}', calculated alias is '{}', cached alias is '{}'", tableColumnReference, tableName, colName, alias, tableColumnAlias.get(tableColumnReference));
                            alias = (String)tableColumnAlias.get(tableColumnReference);
                        }
                        fieldAliases.put(lowerCase,alias);
                    }
                }
            }
        }

        for (final Object o : this.getExternalFieldXMLPaths()) {
            String s   = (String) o;
            String sLC = s.toLowerCase();

            String alias      = (String) this.externalFieldAliases.get(sLC);
            String localAlias = alias.substring(alias.indexOf(".") + 1);

            sb.append(", ").append(alias).append(" AS ").append(localAlias);

            fieldAliases.put(sLC, localAlias);
        }

        sb.append(join);

        final String query = sb.toString();
        log.trace("Composed query: {}", query);
        return query;
    }

    public ArrayList getAllFields()
    {
        return new ArrayList<>(fields);
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
            log.error("An error occurred accessing XFT", e);
            return null;
        } catch (ElementNotFoundException e) {
            log.error("Couldn't find the element " + e.ELEMENT, e);
            return null;
        }
    }

    protected String getRootQuery(GenericWrapperElement e,String sql_name,String level)throws IllegalAccessException{
        QueryOrganizer securityQO;
        SQLClause coll = null;

        try {
            try {
                if (user==null){
                    if (where!=null && where.numClauses()>0){
                      coll =where;
                    }
                }else{
                    coll = Permissions.getCriteriaForXDATRead(user,new SchemaElement(e));
                }


                if (coll ==null || coll.numClauses()==0){
                    if (where!=null && where.numClauses()>0){
                        coll =where;
                      }
                }
            } catch (IllegalAccessException e1) {
                log.error("", e1);
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
                        if (StringUtils.isNotBlank(level) && !level.equals(ViewManager.ALL)){
                            CriteriaCollection inner = new CriteriaCollection("OR");
                        	for(String l: XftStringUtils.CommaDelimitedStringToArrayList(level)){
                            	inner.addClause(e.getFullXMLName()+"/meta/status", l);
                        	}
                            newColl.add(inner);
                        }
                        
                        coll = newColl;
                    }else if (StringUtils.isNotBlank(level) && !level.equals(ViewManager.ALL)){
                        CriteriaCollection newColl = new CriteriaCollection("AND");
                        newColl.add(coll);
                        
                        //add support for limited status reflected in search results
                        CriteriaCollection inner = new CriteriaCollection("OR");
                    	for(String l: XftStringUtils.CommaDelimitedStringToArrayList(level)){
                        	inner.addClause(e.getFullXMLName()+"/meta/status", l);
                    	}
                        newColl.add(inner);
                                           
                        coll = newColl;
                    }

                    securityQO = new QueryOrganizer(rootElement,null,ViewManager.ALL);
                    for (final Object o1 : coll.getSchemaFields()) {
                        Object[]     o = (Object[]) o1;
                        String       path = (String) o[0];
                        SchemaFieldI field = (SchemaFieldI) o[1];
                        final boolean hasSchemaField;
                        if (field == null) {
                            field = GenericWrapperElement.GetFieldForXMLPath(path);
                            hasSchemaField = false;
                        } else {
                            hasSchemaField = true;
                        }
                        String fieldName = path.substring(path.lastIndexOf("/") + 1);
                        assert field != null;
                        String id = field.getId();
                        if (log.isTraceEnabled()) {
                            if (hasSchemaField) {
                                log.trace("There was a schema field for type '{}' with ID '{}' and parent type '{}'", path, id, figureItOut(field));
                            } else {
                                log.trace("There was no schema field for type '{}', retrieved the field by XMLPath with ID '{}' and name '{}'", path, id, figureItOut(field));
                            }
                        }
                        if (!id.equalsIgnoreCase(fieldName)) {
                            log.debug("The ID '{}' does not match the name '{}', checking for reference", id, fieldName);
                            if (field.isReference()) {
                                final GenericWrapperElement foreign = (GenericWrapperElement) field.getReferenceElement();
                                final GenericWrapperField   foreignPK      = foreign.getAllPrimaryKeys().get(0);
                                log.debug("Field '{}' is a reference! The element is '{}', with primary key field '{}' and a parent '{}'", id, foreign.getXMLName(), foreignPK.getXMLPathString(), foreignPK.getParentElement().getFullXMLName());
                                path = foreignPK.getXMLPathString(path);
                            }
                        }
                        log.info("Adding field '{}' to the query", path);
                        securityQO.addField(path);
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
                    String        subQuery      = securityQO.buildQuery();
                    StringBuilder securityQuery = new StringBuilder("SELECT DISTINCT ON (");

                    for (int i=0;i<keyXMLFields.size();i++){
                        if (i>0) securityQuery.append(", ");
                        securityQuery.append(securityQO.getFieldAlias((String) keyXMLFields.get(i)));
                    }

                    securityQuery.append(") * FROM (").append(subQuery).append(") SECURITY WHERE ");
                    securityQuery.append(coll.getSQLClause(securityQO));

                    StringBuilder query = new StringBuilder("SELECT SEARCH.* FROM (" + securityQuery + ") SECURITY LEFT JOIN " + e.getSQLName() + " SEARCH ON ");

                    keys = rootElement.getAllPrimaryKeys().iterator();
                    int keyCounter=0;
                    while (keys.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)keys.next();
                        String key =sf.getXMLPathString(rootElement.getFullXMLName());
                        if (keyCounter++>0){
                            query.append(" AND ");
                        }
                        query.append("SECURITY.").append(securityQO.getFieldAlias(key)).append("=SEARCH.").append(sf.getSQLName());

                    }
                    return "(" +query + ")";
                }else{
                    StringBuilder query    = new StringBuilder("SELECT * FROM " + sql_name);
                    Iterator      keys     = rootElement.getAllPrimaryKeys().iterator();
                    int           keyCount =0;
                    while (keys.hasNext())
                    {
                        SchemaFieldI sf = (SchemaFieldI)keys.next();

                        if (keyCount++>0){
                            query.append(" AND ");
                        }else{
                            query.append(" WHERE ");
                        }

                        query.append(sf.getSQLName()).append(" IS NULL ");
                    }
                    return "(" +query + ")";
                    //throw new IllegalAccessException("No defined read privileges for " + rootElement.getFullXMLName());
                }
            }
        } catch (IllegalAccessException e1) {
            log.error("", e1);
            throw e1;
        } catch (Exception e1) {
            log.error("", e1);
        }

        return sql_name;
    }

    private String figureItOut(final SchemaFieldI field) {
        if (field instanceof GenericWrapperField) {
            return ((GenericWrapperField) field).getWrapped().getParentElement().getName();
        }
        if (field instanceof SchemaField) {
            return ((SchemaField) field).getWrapped().getParentElement().getFullXMLName();
        }
        return "";
    }

    protected void addFieldToJoin(String[] layers) throws Exception
    {
	    String tableString = layers[0];
	    if (tables.get(tableString)==null)
	    {
		    if (!tableString.contains("."))
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
                    if (layers[1].contains("."))
                    {
                        layers1[1]= layers[1].substring(0,layers[1].lastIndexOf("."));
                    }
                    if (!layers[3].contains("."))
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

                    String rootElementName;

                    if (!subString.contains("]"))
                    {
                        rootElementName = subString;
                    }else{
                        rootElementName = subString.substring(subString.lastIndexOf("]")+1);
                    }

                    GenericWrapperElement rootElement = getGenericWrapperElement(rootElementName);

                    String foreignFieldName=null;
                    String foreignElementName;

                    if (!joinTable.contains("]"))
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
                        tables.put(tableString, tableAliases.get(foreignAlias));
                    }else{
                        tables.put(tableString,foreignAlias);
                    }
		    }
	    }else{
	        String tableAlias = layers[1];
	        if (tableAlias.contains("."))
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
     * @param root         The root element.
     * @param foreign      The foreign element.
     * @param rootAlias    The alias to use for the root element.
     * @return The join clause generated from the parameters.
     * @throws Exception When an error occurs.
     */
    private String getJoinClause(GenericWrapperElement root, GenericWrapperElement foreign, String rootAlias,String foreignAlias, String[] layer, String relativeFieldRef) throws Exception
    {
    	if(!tableAliases.containsKey(foreignAlias))
    		tableAliases.put(foreignAlias,"table"+ tableAliases.size());
        foreignAlias = (String)tableAliases.get(foreignAlias);

        String last;
        if (!layer[3].contains("."))
        {
            last = layer[3];
        }else{
            last = layer[3].substring(layer[3].lastIndexOf(".") + 1);
        }
        GenericWrapperField f;
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

        StringBuilder j = new StringBuilder(" ");

        XFTReferenceI ref = f.getXFTReference();
        if (ref.isManyToMany())
        {
            XFTManyToManyReference many = (XFTManyToManyReference)ref;
            String mapTableName = many.getMappingTable() + tableAliases.size();
            j.append(" LEFT JOIN ").append(many.getMappingTable()).append(" AS ").append(mapTableName);
            j.append(" ON ");

            Iterator iter =many.getMappingColumnsForElement(root).iterator();
		    int counter = 0;
		    while(iter.hasNext())
		    {
		        XFTMappingColumn map = (XFTMappingColumn)iter.next();
		        if (counter++==0)
	            {
	                j.append(" ").append(rootAlias).append(".").append(map.getForeignKey().getSQLName()).append("=").append(mapTableName).append(".").append(map.getLocalSqlName());
	            }else{
	                j.append(" AND ").append(rootAlias).append(".").append(map.getForeignKey().getSQLName()).append("=").append(mapTableName).append(".").append(map.getLocalSqlName());
	            }
		    }
            j.append(" LEFT JOIN ").append(foreign.getSQLName()).append(" ").append(foreignAlias);
            j.append(" ON ");
		    iter =many.getMappingColumnsForElement(foreign).iterator();
		    counter = 0;
		    while(iter.hasNext())
		    {
		        XFTMappingColumn map = (XFTMappingColumn)iter.next();
		        if (counter++==0)
	            {
	                j.append(" ").append(mapTableName).append(".").append(map.getLocalSqlName()).append("=").append(foreignAlias).append(".").append(map.getForeignKey().getSQLName());
	            }else{
	                j.append(" AND ").append(mapTableName).append(".").append(map.getLocalSqlName()).append("=").append(foreignAlias).append(".").append(map.getForeignKey().getSQLName());
	            }
		    }
        }else{
            j.append(" LEFT JOIN ").append(foreign.getSQLName()).append(" ").append(foreignAlias);
            j.append(" ON ");
            XFTSuperiorReference sup = (XFTSuperiorReference)ref;
            Iterator iter = sup.getKeyRelations().iterator();
            int counter=0;
            while (iter.hasNext())
            {
                XFTRelationSpecification spec = (XFTRelationSpecification)iter.next();
                if (spec.getLocalTable().equalsIgnoreCase(root.getSQLName()))
                {
                    if (counter!=0)
                        j.append(" AND ");
                    j.append(rootAlias).append(".").append(spec.getLocalCol());
                    j.append("=").append(foreignAlias).append(".").append(spec.getForeignCol());
                }else{
                    if (counter!=0)
                        j.append(" AND ");
                    j.append(rootAlias).append(".").append(spec.getForeignCol());
                    j.append("=").append(foreignAlias).append(".").append(spec.getLocalCol());
                }
                counter++;
            }
        }
        return j.toString();
    }

    private GenericWrapperElement getGenericWrapperElement(String s) throws Exception
    {
        if (s.contains("."))
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

    @SuppressWarnings("unused")
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
                assert f != null;
                String id = f.getId();
                if (!id.equalsIgnoreCase(fieldName))
                {
                    if (f.isReference())
                    {
                        GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
                        GenericWrapperField sf = foreign.getAllPrimaryKeys().get(0);
                        xmlPath = sf.getXMLPathString(xmlPath);
                    }
                }
            } catch (Exception e) {
                log.error("", e);
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
        } catch (XFTInitException | ElementNotFoundException e1) {
            log.error("", e1);
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
                assert f != null;
                if (!f.getId().equalsIgnoreCase(fieldName))
                {
                    if (f.isReference())
                    {
                        GenericWrapperElement foreign = (GenericWrapperElement)f.getReferenceElement();
                        GenericWrapperField sf = foreign.getAllPrimaryKeys().get(0);
                        xmlPath = sf.getXMLPathString(xmlPath);
                    }
                }
            } catch (Exception e) {
                log.error("", e);
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
        StringBuilder sb = new StringBuilder();
        sb.append(this.getRootElement().getFullXMLName()).append("\n");

        for (final Object o : this.getAllFields()) {
            String s = (String) o;
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
        StringBuilder sb = new StringBuilder();
        if (arcDefine.getBridgeElement().equalsIgnoreCase(rootElement.getFullXMLName()))
		{
			Arc foreignArc = (Arc)foreign.getArcs().get(arcDefine.getName());
			String rootField = arcDefine.getBridgeField();

			String foreignField = (String)foreignArc.getCommonFields().get(arcDefine.getEqualsField());

			foreign.getDisplay().getDisplayField(foreignField);

			DisplayField df = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayFieldElement dfe = df.getElements().get(0);
			String localFieldElement = dfe.getSchemaElementName();

			if (XftStringUtils.GetRootElementName(localFieldElement).equalsIgnoreCase(getRootElement().getFullXMLName()))
			{

				this.addField(dfe.getSchemaElementName());

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = getTableAndFieldSQL(dfe.getSchemaElementName());

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 = df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (XftStringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
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
	    			int pkCount;
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
			    SchemaElementI middle = XftStringUtils.GetRootElement(localFieldElement);
			    QueryOrganizer localMap = new QueryOrganizer(rootElement,this.getUser(),ViewManager.ALL);
			    localMap.setIsMappingTable(true);
				localMap.addField(localFieldElement);

				Iterator pks = rootElement.getAllPrimaryKeys().iterator();
    			int pkCount;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    localMap.addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                    addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                }

				String localMapQuery = localMap.buildQuery();
                assert middle != null;
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
				DisplayFieldElement dfe2 = df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (XftStringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
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
			DisplayFieldElement dfe = df.getElements().get(0);
			String localFieldElement = dfe.getSchemaElementName();

			if (XftStringUtils.GetRootElementName(localFieldElement).equalsIgnoreCase(getRootElement().getFullXMLName()))
			{

				this.addField(dfe.getSchemaElementName());

				//String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
				//this.addFieldToJoin(layers);
				String localCol = getTableAndFieldSQL(dfe.getSchemaElementName());

				DisplayField df2 = foreign.getDisplayField(foreignField);
				DisplayFieldElement dfe2 = df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (XftStringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
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
	    			int pkCount;
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
			    SchemaElementI middle = XftStringUtils.GetRootElement(localFieldElement);
			    QueryOrganizer localMap = new QueryOrganizer(rootElement,this.getUser(),ViewManager.ALL);
			    localMap.setIsMappingTable(true);
			    localMap.addField(localFieldElement);

				Iterator pks = rootElement.getAllPrimaryKeys().iterator();
    			int pkCount;
                while (pks.hasNext())
                {
                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                    localMap.addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                    addField(sf.getXMLPathString(rootElement.getFullXMLName()));
                }

				String localMapQuery = localMap.buildQuery();
                assert middle != null;
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
				DisplayFieldElement dfe2 = df2.getElements().get(0);

				String foreignFieldElement = dfe2.getSchemaElementName();

				if (XftStringUtils.GetRootElementName(foreignFieldElement).equalsIgnoreCase(qo.getRootElement().getFullXMLName()))
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
