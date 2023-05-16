/*
 * core: org.nrg.xdat.search.QueryOrganizer
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.search;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.display.*;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.QueryOrganizerI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;

import javax.annotation.Nullable;
import java.util.*;

@Slf4j
@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryOrganizer extends org.nrg.xft.search.QueryOrganizer implements QueryOrganizerI{
    private final List<String>                        viewFields     = new ArrayList<>();
    private final List<String>                        subqueryFields = new ArrayList<>();
    private final Map<String, QueryOrganizer>         addOns         = new HashMap<>();//0:elementName,1:QueryOrganizer
    private final Map<String, DisplayFieldReferenceI> subqueries     = new HashMap<>();

    public QueryOrganizer(String elementName, UserI u, String level, Map<String, CachedRootQuery> rootQueries) throws ElementNotFoundException {
        super(elementName, u, level, rootQueries);
    }

    public QueryOrganizer(String elementName, UserI u, String level) throws ElementNotFoundException {
        super(elementName, u, level,  null);
    }

    public QueryOrganizer(SchemaElementI se, UserI u, String level, Map<String, CachedRootQuery> rootQueries) {
        super(se, u, level, rootQueries);
    }

    public QueryOrganizer(SchemaElementI se, UserI u, String level) {
        this(se,u,level,null);
    }

    public static QueryOrganizer buildXDATQueryOrganizerWithClause(final SchemaElementI se, final UserI u, final String level){
        return new QueryOrganizer(se,u,level,new LinkedHashMap<>());
    }

    public void addSubquery(String key, DisplayFieldReferenceI df){
        subqueries.put(key, df);
    }

    /**
     * @param xmlPath    The XML path of the field to add.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    public void addField(String xmlPath) throws ElementNotFoundException {
        if (xmlPath.startsWith("VIEW_"))
        {
            xmlPath = xmlPath.substring(5);
            addView(xmlPath);
        }else if (xmlPath.startsWith("SUBQUERY_"))
        {
            xmlPath = xmlPath.substring(9);
            addSubquery(xmlPath);
        }else{
            xmlPath = XftStringUtils.StandardizeXMLPath(xmlPath);
            String root = XftStringUtils.GetRootElementName(xmlPath);
            if (rootElement.getFullXMLName().equalsIgnoreCase(root))
            {
                super.addField(xmlPath);
                try {
                    String temp = ViewManager.GetViewColumnName(rootElement.getGenericXFTElement(),xmlPath,ViewManager.DEFAULT_LEVEL,true,true);
                    if (temp !=null)
                    {
                        fieldAliases.put(xmlPath.toLowerCase(),temp);
                    }
                } catch (XFTInitException e) {
                    log.error("",e);
                }
            }else{
                QueryOrganizer qo = addOns.get(root);
                if (qo == null)
                {
                    qo = new QueryOrganizer(root, user, level, _rootQueries);
                    qo.setJoinCounter(joinCounter++);
                    addOns.put(root,qo);
                    addField(qo.getFilterField(root));
                }

                qo.addField(xmlPath);
                try {
                    SchemaElement se = SchemaElement.GetElement(root);
                    String temp = ViewManager.GetViewColumnName(se.getGenericXFTElement(),xmlPath,ViewManager.DEFAULT_LEVEL,true,true);
                    if (temp !=null)
                    {
                        fieldAliases.put(xmlPath.toLowerCase(), XftStringUtils.CreateAlias(se.getSQLName(), temp));
                    }
                } catch (XFTInitException e) {
                    log.error("",e);
                }
            }
        }
    }

    /**
     * @param xmlPath    The XML path of the subquery to add.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    public void addSubquery(String xmlPath) throws ElementNotFoundException
    {
        String root = XftStringUtils.GetRootElementName(xmlPath);
        if (rootElement.getFullXMLName().equalsIgnoreCase(root))
        {
            if (! subqueryFields.contains(xmlPath))
            {
                subqueryFields.add(xmlPath);
            }
        }else{
            QueryOrganizer qo = addOns.get(root);
            if (qo == null)
            {
                qo = new QueryOrganizer(root, user, level, _rootQueries);
                qo.setJoinCounter(joinCounter++);
                addOns.put(root,qo);
                addField(getFilterField(root));
            }

            qo.addSubquery(xmlPath);
        }
    }

    /**
     * @param xmlPath    The XML path of the view to add.
     * @throws ElementNotFoundException When a specified element isn't found on the object.
     */
    public void addView(String xmlPath) throws ElementNotFoundException
    {
        String root = XftStringUtils.GetRootElementName(xmlPath);
        if (rootElement.getFullXMLName().equalsIgnoreCase(root))
        {
            if (! viewFields.contains(xmlPath))
            {
                viewFields.add(xmlPath);

            }
        }else{
            QueryOrganizer qo = addOns.get(root);
            if (qo == null)
            {
                qo = new QueryOrganizer(root, user, level, _rootQueries);
                qo.setJoinCounter(joinCounter++);
                addOns.put(root,qo);
                addField(getFilterField(root));
            }

            qo.addView(xmlPath);
        }
    }

    /**
     * @param viewName    The view name to join.
     * @exception Exception When something goes wrong.
     */
    public void addSubqueryToJoin(String viewName) throws Exception
    {
        if (tables.get(viewName)==null)
        {
            try {
                ElementDisplay ed = ((new SchemaElement(rootElement.getGenericXFTElement()))).getDisplay();

                if (viewName.startsWith("SUBQUERYFIELD"))
                {
                    String s = viewName.substring(14);

                    int indexDot = s.indexOf(".");
                    String FIELDID = s.substring(0,indexDot);
                    String VALUE = s.substring(indexDot+1);
                    String alias = FIELDID + "_" + XftStringUtils.cleanColumnName(VALUE);
                    VALUE = StringUtils.replace(StringUtils.replace(VALUE, "_com_", ","), "_col_", ":");


                    final StringBuilder sb = new StringBuilder();

                    SQLQueryField df = (SQLQueryField) ed.getDisplayField(FIELDID);
                    String subquery = df.getSubQuery();
                    if (!VALUE.contains(",")){
                        subquery= StringUtils.replace(subquery, "@WHERE", VALUE);
                    }else{
                        ArrayList<String> values = XftStringUtils.CommaDelimitedStringToArrayList(VALUE);
                        int count =0;
                        for(String value1:values){
                            subquery= StringUtils.replace(subquery, "@WHERE" + count++, value1);
                        }
                    }

                    sb.append(" LEFT JOIN (").append(subquery).append(") AS ");
                    sb.append(alias);

                    sb.append(" ON ");
                    int counter = 0;
                    for (SQLQueryField.QueryMappingColumn mc : df.getMappingColumns()) {
                        if (counter++ != 0) {
                            sb.append(" AND ");
                        }
                        addFieldToJoin(mc.getSchemaField());
                        sb.append(this.getTableAndFieldSQL(mc.getSchemaField()));
                        sb.append("=");
                        sb.append(alias).append(".").append(mc.getQueryField());
                    }

                    joins.append(sb);
                } else {
                    throw new Exception("No Such Subquery Found. " + viewName);
                }

            } catch (XFTInitException | ElementNotFoundException e) {
                log.error("", e);
            }
        }
    }

    /**
     * @param viewName The view name to join.
     * @throws Exception When something goes wrong.
     */
    public void addViewToJoin(String viewName) throws Exception {
        if (tables.get(viewName) == null) {
            //This is where view references could be hijacted for alternate implementations (like Calculated Columns)
//            if (StringUtils.equals(viewName, "SUB_PROJECTS") && this.getRootElement().getFullXMLName().equals("xnat:subjectData")) {
//                //calculated field
//                return;
//            }
            try {
                ElementDisplay ed = ((new SchemaElement(rootElement.getGenericXFTElement()))).getDisplay();
                ViewLink vl = (ViewLink) ed.getViewLinks().get(viewName);
                if (vl != null) {
                    Mapping map = vl.getMapping();
                    String[] layers = new String[3];
                    layers[0] = rootElement.getFullXMLName();
                    layers[1] = rootElement.getSQLName();
                    addFieldToJoin(layers);

                    StringBuilder j = new StringBuilder(" LEFT JOIN " + map.getTableName() + " " + viewName);
                    j.append(" ON ");

                    Iterator mappingColumns = map.getColumns().iterator();
                    int counter = 0;
                    while (mappingColumns.hasNext()) {
                        MappingColumn mc = (MappingColumn) mappingColumns.next();
                        SchemaElementI mappedElement = SchemaElement.GetElement(mc.getRootElement());
                        if (mappedElement.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName())) {
                            if (counter++ != 0) {
                                j.append(" AND ");
                            }
                            addFieldToJoin(mc.getFieldElementXMLPath());
                            j.append(this.getTableAndFieldSQL(mc.getFieldElementXMLPath()));
                            j.append("=");
                            j.append(viewName).append(".").append(mc.getMapsTo());
                        }
                    }

                    joins.append(" ").append(j);
                    tables.put(viewName, viewName);
                } else {
                    if (viewName.startsWith("COUNT")) {
                        String s = viewName.substring(6);

                        SchemaLink sl = (SchemaLink) ed.getSchemaLinks().get(s);
                        SchemaElement foreign = SchemaElement.GetElement(s);

                        boolean linked = false;
                        if (sl != null) {
                            if (sl.getType().equalsIgnoreCase("mapping")) {
                                joins.append(" ").append(getSchemaLinkCount(sl, foreign));
                                tables.put(viewName, foreign.getSQLName() + "_COUNT");
                                linked = true;
                            }
                        }

                        if (!linked) {
//                          CHECK ARCS
                            ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement, foreign);
                            if (arcDefine != null) {
                                joins.append(getArcJoinCount(arcDefine, foreign));
                                tables.put(viewName, foreign.getSQLName() + "_COUNT");
                                linked = true;
                            }
                        }

                        if (!linked) {
                            //Look for direct connection
                            String[] connection = this.rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());
                            if (connection != null) {
                                QueryOrganizer foreignQO = new QueryOrganizer(foreign, this.getUser(), level, _rootQueries);
                                QueryOrganizer localQO = new QueryOrganizer(rootElement, this.getUser(), level, _rootQueries);
                                //BUILD CONNECTION FROM ROOT TO EXTENSION
                                String localSyntax = connection[0];
                                String xmlPath = connection[1];
                                SchemaFieldI gwf;
                                SchemaElementI extension;
                                if (localSyntax.indexOf(XFT.PATH_SEPARATOR) == -1) {
                                    extension = rootElement;
                                } else {
                                    gwf = SchemaElement.GetSchemaField(localSyntax);
                                    extension = gwf.getReferenceElement();
                                }

                                Iterator pks = extension.getAllPrimaryKeys().iterator();
                                while (pks.hasNext()) {
                                    SchemaFieldI sf = (SchemaFieldI) pks.next();
                                    foreignQO.addField(xmlPath + sf.getXMLPathString(""));
                                    localQO.addField(localSyntax + sf.getXMLPathString(""));

                                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(localSyntax + sf.getXMLPathString(""));
                                    addFieldToJoin(layers);
                                }

                                //BUILD CONNECTION FROM FOREIGN TO EXTENSION
                                String query = foreignQO.buildQuery();
                                StringBuffer sb = new StringBuffer();
                                sb.append(" JOIN (").append(query);
                                sb.append(") AS ").append(foreign.getSQLName()).append(" ON ");
                                pks = extension.getAllPrimaryKeys().iterator();
                                int pkCount = 0;
                                while (pks.hasNext()) {
                                    SchemaFieldI sf = (SchemaFieldI) pks.next();
                                    if (pkCount++ != 0) {
                                        sb.append(" AND ");
                                    }
                                    String localCol = translateXMLPath(localSyntax + sf.getXMLPathString(""), rootElement.getSQLName());
                                    String foreignCol = foreignQO.translateXMLPath(xmlPath + sf.getXMLPathString(""), foreign.getSQLName());

                                    sb.append(localCol).append("=");
                                    sb.append(foreignCol);
                                }

                                pks = rootElement.getAllPrimaryKeys().iterator();
                                pkCount = 0;
                                StringBuilder cols = new StringBuilder();
                                while (pks.hasNext()) {
                                    SchemaFieldI sf = (SchemaFieldI) pks.next();
                                    if (pkCount++ != 0) {
                                        sb.append(", ");
                                    }
                                    String localCol = translateXMLPath(sf.getXMLPathString(rootElement.getFullXMLName()), rootElement.getSQLName());
                                    cols.append(localCol);
                                }

                                String subQuery = "SELECT " + cols + ", COUNT(*) AS " + foreign.getSQLName() + "_COUNT ";
                                subQuery += " FROM (" + localQO.buildQuery() + ") " + rootElement.getSQLName() + sb.toString();
                                subQuery += " GROUP BY " + cols;

                                sb = new StringBuffer();
                                sb.append(" LEFT JOIN (").append(subQuery);
                                sb.append(") AS ").append(foreign.getSQLName()).append("_COUNT ON ");
                                pks = rootElement.getAllPrimaryKeys().iterator();
                                pkCount = 0;
                                while (pks.hasNext()) {
                                    SchemaFieldI sf = (SchemaFieldI) pks.next();
                                    if (pkCount++ != 0) {
                                        sb.append(" AND ");
                                    }
                                    String localCol = this.getTableAndFieldSQL(sf.getXMLPathString(rootElement.getFullXMLName()));
                                    String foreignCol = translateXMLPath(sf.getXMLPathString(rootElement.getFullXMLName()), foreign.getSQLName() + "_COUNT");

                                    sb.append(localCol).append("=");
                                    sb.append(foreignCol);
                                }
                                joins.append(sb.toString());
                            }
                            //}
                        }
                    } else if (!viewName.startsWith("SUBQUERYFIELD")) {
                        throw new Exception("No Such View Found. " + viewName);
                    }
                }
            } catch (XFTInitException | ElementNotFoundException e) {
                log.error("", e);
            }
        }
    }

    /***
     * Checks if elements extendees are of type baseType, or this instance itself is that type.
     * @param se
     * @param baseType
     * @return
     */
    public static boolean instanceOf(final SchemaElement se, final String baseType) {
        return (StringUtils.equals(se.getFullXMLName(), baseType) || se.instanceOf(baseType));
    }

    /********
     * Some joins can be greatly simplified based on pre-defined join logic.
     * This method figures out if an optimized join is possible between these elements.
     * @param rootSchemaElement
     * @param foreign
     * @return
     * @throws XFTInitException
     * @throws ElementNotFoundException
     */
    @Nullable
    public ManualJoin getOptimizedJoin(final SchemaElement rootSchemaElement, final SchemaElement foreign) throws XFTInitException, ElementNotFoundException {
        for(final ManualJoin mj: manualJoins){
            if(instanceOf(rootSchemaElement,mj._srcType) && instanceOf(foreign,mj._destType)){
                return mj;
            }
        }
        for(final ManualJoin used: usedOptimizations){
            for(final ManualJoin mj: manualJoins){
                if(instanceOf(SchemaElement.GetElement(used._destType),mj._srcType) && instanceOf(foreign,mj._destType)){
                    return mj;
                }
            }
        }
        return null;
    }

    private List<ManualJoin> usedOptimizations= Lists.newArrayList();

    /*************************
     * If these elements can be joined via an optimized configuration, returns true.  Else false.
     *
     * @param rootXMLType
     * @param foreignXMLType
     * @return
     * @throws XFTInitException
     * @throws ElementNotFoundException
     */
    public boolean isOptimizedField(final String rootXMLType, final String foreignXMLType) throws XFTInitException, ElementNotFoundException {
        return isOptimizedField(SchemaElement.GetElement(rootXMLType),SchemaElement.GetElement(foreignXMLType));
    }

    /************************
     * If these elements can be joined via an optimized configuration, returns true.  Else false.
     *
     * @param rootSchemaElement
     * @param foreign
     * @return
     * @throws XFTInitException
     * @throws ElementNotFoundException
     */
    public boolean isOptimizedField(final SchemaElement rootSchemaElement, final SchemaElement foreign) throws XFTInitException, ElementNotFoundException {
        return (getOptimizedJoin(rootSchemaElement,foreign)!=null);
    }

    /*********************
     * If an optimized join is possible, apply it to 'this'.
     * Join is manually applied by altering the contents of 'joins' and 'tables'
     *
     * @param foreignQO
     * @param foreign
     * @throws Exception
     */
    public void optimizedJoin(final QueryOrganizer foreignQO, final SchemaElement foreign) throws Exception{
        final ManualJoin mj = getOptimizedJoin(SchemaElement.GetElement(this.getRootElement().getFullXMLName()),foreign);
        if(mj!=null){
            String subJoin = foreignQO.buildJoin();
            final String toReplace = String.format(mj._join[0],foreign.getSQLName(),this.getTableNameForType(mj._srcType));
            final String toReplaceWith = String.format(mj._join[1],foreign.getSQLName(),this.getTableNameForType(mj._srcType));
            subJoin = StringUtils.replace(subJoin, toReplace,toReplaceWith);

            joins.append(subJoin);
            tables.putAll(foreignQO.getTableDefinitions());
            usedOptimizations.add(mj);
        }
    }

    public boolean containsOptimizedJoin(final SchemaElement root, final SchemaElement foreign) throws XFTInitException, ElementNotFoundException {
        final ManualJoin optimizedJoin = getOptimizedJoin(root,foreign);
        return (optimizedJoin!=null && usedOptimizations.contains(optimizedJoin));
    }

    @Nullable
    private String getTableNameForType(final String foreign) {
        for (Entry entry : tables.entrySet()) {
            if (StringUtils.endsWith((String) entry.getKey(), foreign)) {
                return (String) entry.getValue();
            }
        }

        return null;
    }

    /**
     * Builds query which gets all necessary fields from the db for the generation of the Display Fields
     * (Secured).
     * @return The join query.
     * @exception Exception When something goes wrong.
     */
    public String buildJoin() throws Exception
    {
        super.buildJoin();

        //ADD VIEWS
        for (final String viewField : viewFields) {
            final String temp = viewField.substring(viewField.indexOf(".") + 1);
            addViewToJoin(temp.substring(0, temp.indexOf(".")));
        }

        //ADD SubQueries
        for (final String subqueryField : subqueryFields) {
            //temp =  temp.substring(0,temp.indexOf("."));
            addSubqueryToJoin(subqueryField.substring(subqueryField.indexOf(".") + 1));
        }

        //ADD OTHER SUB-QUERIES
        for (final String root : addOns.keySet()) {
            boolean linked = false;
            SchemaElement rootSchemaElement = new SchemaElement(rootElement.getGenericXFTElement());
            ElementDisplay ed = rootSchemaElement.getDisplay();

            QueryOrganizer qo = addOns.get(root);
            SchemaElement foreign = SchemaElement.GetElement(root);

            if(isOptimizedField(rootSchemaElement,foreign))
            {
                qo.setSkipSecurity(true);
                //joining from subject to demographics... keep it simple.
                optimizedJoin(qo,foreign);
                continue;
            }

            SchemaLink sl = null;
            if (ed != null)
            {
                sl = (SchemaLink)ed.getSchemaLinks().get(root);
            }
            if (sl != null)
            {
                if (sl.getType().equalsIgnoreCase("mapping"))
                {
                    joins.append(getSchemaLinkJoin(sl,qo,foreign));
                    tables.put(root,foreign.getSQLName());
                    linked = true;
                }
            }

            if (! linked)
            {
//              CHECK ARCS
                ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement,foreign);
                if (arcDefine!=null)
                {
                    joins.append(getArcJoin(arcDefine,qo,foreign));
                    tables.put(root,foreign.getSQLName());
                    linked = true;
                }
            }

            if (! linked)
            {
                //Look for direct connection
                String[] connection = this.rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());

                //String[] connection = GenericWrapperElement.FindExtensionReferenceField(rootElement.getGenericXFTElement(),foreign.getGenericXFTElement());
                if (connection != null)
                {
                    //BUILD CONNECTION FROM ROOT TO EXTENSION
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
                    }else{
                        gwf = SchemaElement.GetSchemaField(localSyntax);
                        extension = gwf.getReferenceElement();

                        QueryOrganizer mappingQO = new QueryOrganizer(this.rootElement,this.getUser(),this.level,_rootQueries);
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
                    }
                }else{
                    //NO CONNECTION FOUND

                    //TRY ARC JOINABLE ELEMENTS
                    ArrayList checked = new ArrayList();

                    String mappingElement = null;

                    for (final ArcDefinition arc : DisplayManager.GetInstance().getArcDefinitions(rootElement)) {
                        if (!arc.getBridgeElement().equals(rootElement.getFullXMLName()))
                        {
                            if (!checked.contains(arc.getBridgeElement()))
                            {
                                checked.add(arc.getBridgeElement());
                                if (CanConnect(arc.getBridgeElement(),root))
                                {
                                    log.info("Connecting " + rootElement.getFullXMLName() + "->" + arc.getBridgeElement() + "->" + root);
                                    mappingElement = arc.getBridgeElement();
                                    break;
                                }
                            }
                        }

                        for (final String member : arc.getMemberList()) {
                            if (!checked.contains(member))
                            {
                                checked.add(member);
	                            if (CanConnect(member,root))
	                            {
	                                log.info("Connecting " + rootElement.getFullXMLName() + "->" + member + "->" + root);
	                                mappingElement = member;
	                                break;
	                            }
                            }
                        }
                    }

                    if (mappingElement == null){
                        assert ed != null;
                        for (final Object object : ed.getSchemaLinks().keySet()) {
                            String key = (String) object;
                            if (!checked.contains(key))
                            {
                                checked.add(key);
                                if (CanConnect(key,root))
                                {
                                    log.info("Connecting " + rootElement.getFullXMLName() + "->" + key + "->" + root);
                                    mappingElement = key;
                                    break;
                                }
                            }
                        }
                    }

                    if (mappingElement == null)
                    {
                        throw new Exception("Unable to connect " + rootElement.getFullXMLName() + " to " + root);
                    }else{
                        SchemaElement extension = SchemaElement.GetElement(mappingElement);

                        QueryOrganizer mappingQO = new QueryOrganizer(extension,this.getUser(),ViewManager.ALL,_rootQueries);
                        mappingQO.setIsMappingTable(true);

                        Iterator pks = foreign.getAllPrimaryKeys().iterator();
                        while (pks.hasNext())
                        {
                            SchemaFieldI sf = (SchemaFieldI)pks.next();
                            qo.addField(sf.getXMLPathString(foreign.getFullXMLName()));

                            mappingQO.addField(sf.getXMLPathString(foreign.getFullXMLName()));
                        }

                        Iterator pKeys = rootElement.getAllPrimaryKeys().iterator();
                        while (pKeys.hasNext())
                        {
                            SchemaFieldI pKey = (SchemaFieldI)pKeys.next();
                            mappingQO.addField(pKey.getXMLPathString(rootElement.getFullXMLName()));
                        }

                          final StringBuilder sb = new StringBuilder();

                          //BUILD MAPPING TABLE
                          String mappingTableName = "map_" + extension.getSQLName() + "_" + foreign.getSQLName();
                            String mappingQuery = mappingQO.buildQuery();
                            sb.append(" LEFT JOIN (").append(mappingQuery);
                          sb.append(") AS ").append(mappingTableName).append(" ON ");
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
                            String foreignCol = mappingQO.translateXMLPath(pKey.getXMLPathString(rootElement.getFullXMLName()),mappingTableName);

                          sb.append(localCol).append("=");
                          sb.append(foreignCol);
                        }


                        //BUILD CONNECTION FROM FOREIGN TO EXTENSION
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
                            String localCol = mappingQO.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),mappingTableName);
                            String foreignCol = qo.translateXMLPath(sf.getXMLPathString(foreign.getFullXMLName()),foreign.getSQLName());

                			sb.append(localCol).append("=");
                			sb.append(foreignCol);
                        }
            			joins.append(sb.toString());
                    }
                }
            }
        }

        return joins.toString();
    }

    /**
     * Is there a direct connection between the two elements?
     * @param rootElementName    The root element name.
     * @param foreignElementName The foreign element name.
     * @return The connection type between the two elements.
     */
    public static boolean CanConnect(String rootElementName,String foreignElementName)
    {
        try {
            SchemaElement rootElement = SchemaElement.GetElement(rootElementName);
            SchemaElement foreign = SchemaElement.GetElement(foreignElementName);

            ElementDisplay ed = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplay();
            SchemaLink sl = (SchemaLink)ed.getSchemaLinks().get(foreign.getFullXMLName());

            if (sl != null)
            {
                if (sl.getType().equalsIgnoreCase("mapping"))
            	{
                    return true;
            	}
            }


//          CHECK ARCS
            ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement,foreign);
            if (arcDefine!=null)
            {
                return true;
            }

            //Look for direct connection
            String[] connection = rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());

            //String[] connection = GenericWrapperElement.FindExtensionReferenceField(rootElement.getGenericXFTElement(),foreign.getGenericXFTElement());
            if (connection != null)
            {
                return true;
            }
        } catch (XFTInitException | ElementNotFoundException e) {
            log.error("",e);
        }

        return false;
    }



    /**
     * 'schemaelement','arc','connection','multi-leveled'
     * @param rootElementName    The root element name.
     * @param foreignElementName The foreign element name.
     * @return The connection type between the two elements.
     */
    public static String GetConnectionType(String rootElementName,String foreignElementName)
    {
        try {
            SchemaElement rootElement = SchemaElement.GetElement(rootElementName);
            SchemaElement foreign = SchemaElement.GetElement(foreignElementName);

            ElementDisplay ed = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplay();
            SchemaLink sl = (SchemaLink)ed.getSchemaLinks().get(foreign.getFullXMLName());

            if (sl != null)
            {
                if (sl.getType().equalsIgnoreCase("mapping"))
            	{
                    return "schemaelement";
            	}
            }


//          CHECK ARCS
            ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement,foreign);
            if (arcDefine!=null)
            {
                return "arc";
            }

            //Look for direct connection
            String[] connection = rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());

            //String[] connection = GenericWrapperElement.FindExtensionReferenceField(rootElement.getGenericXFTElement(),foreign.getGenericXFTElement());
            if (connection != null)
            {
                return "connection";
            }
        } catch (XFTInitException | ElementNotFoundException e) {
            log.error("",e);
        }

        return "multi-leveled";
    }
    
    private CriteriaCollection buildStatusCriteria(SchemaElement e,String level){
    	//add support for limited status reflected in search results
        CriteriaCollection inner = new CriteriaCollection("OR");
    	for(String l: XftStringUtils.CommaDelimitedStringToArrayList(level)){
        	inner.addClause(e.getFullXMLName()+"/meta/status", l);
    	}
    	return inner;
    }

    private String getArcJoinCount(ArcDefinition arcDefine,SchemaElement foreign) throws Exception
    {
		QueryOrganizer qo = new QueryOrganizer(foreign,this.getUser(),level,_rootQueries);
        final StringBuilder  sb = new StringBuilder();
        if (arcDefine.getBridgeElement().equalsIgnoreCase(rootElement.getFullXMLName()))
		{
			Arc foreignArc = (Arc)foreign.getArcs().get(arcDefine.getName());
			String rootField = arcDefine.getBridgeField();

			String foreignField = (String)foreignArc.getCommonFields().get(arcDefine.getEqualsField());

			DisplayField df = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayFieldElement dfe = df.getElements().get(0);
			this.addField(dfe.getSchemaElementName());
			String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
			this.addFieldToJoin(layers);

			String localCol = this.getTableAndFieldSQL(dfe.getSchemaElementName());

			DisplayField df2 = foreign.getDisplayField(foreignField);
			DisplayFieldElement dfe2 = df2.getElements().get(0);
			qo.addField(dfe2.getSchemaElementName());
			
             
         	qo.setWhere(buildStatusCriteria(foreign,level));

			String query = qo.buildQuery();

			String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName() +"_COUNT");

			String subQuery = "SELECT " + foreignCol + ", COUNT(*) AS " + foreign.getSQLName() + "_COUNT ";
			subQuery += " FROM (" + query + ") " + foreign.getSQLName() + "_COUNT ";
			subQuery += " GROUP BY " + foreignCol;

			sb.append(" LEFT JOIN (").append(subQuery);
			sb.append(") AS ").append(foreign.getSQLName()).append("_COUNT ON ");
			sb.append(localCol).append("=");
			sb.append(foreignCol);
		}else if (arcDefine.getBridgeElement().equalsIgnoreCase(foreign.getFullXMLName()))
		{
			Arc rootArc = (Arc)(new SchemaElement(rootElement.getGenericXFTElement())).getArcs().get(arcDefine.getName());

			String foreignField = arcDefine.getBridgeField();
			String rootField = (String)rootArc.getCommonFields().get(arcDefine.getEqualsField());

			DisplayField df = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayFieldElement dfe = df.getElements().get(0);
			this.addField(dfe.getSchemaElementName());
			String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
			this.addFieldToJoin(layers);
			String localCol = getTableAndFieldSQL(dfe.getSchemaElementName());

			DisplayField df2 = foreign.getDisplayField(foreignField);
			DisplayFieldElement dfe2 = df2.getElements().get(0);
			qo.addField(dfe2.getSchemaElementName());

         	qo.setWhere(buildStatusCriteria(foreign,level));
         	
			String query = qo.buildQuery();

			String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName() +"_COUNT");

			String subQuery = "SELECT " + foreignCol + ", COUNT(*) AS " + foreign.getSQLName() + "_COUNT ";
			subQuery += " FROM (" + query + ") " + foreign.getSQLName() + "_COUNT ";
			subQuery += " GROUP BY " + foreignCol;

			sb.append(" LEFT JOIN (").append(subQuery);
			sb.append(") AS ").append(foreign.getSQLName()).append("_COUNT ON ");
			sb.append(localCol).append("=");
			sb.append(foreignCol);
		}else
		{
			Arc rootArc = (Arc)(new SchemaElement(rootElement.getGenericXFTElement())).getArcs().get(arcDefine.getName());
			Arc foreignArc = (Arc)foreign.getArcs().get(arcDefine.getName());

			String distinctField = arcDefine.getDistinctField();

			String foreignField = (String)foreignArc.getCommonFields().get(distinctField);
			String rootField = (String)rootArc.getCommonFields().get(distinctField);

			String arcMapQuery = DisplayManager.GetArcDefinitionQuery(arcDefine,(new SchemaElement(rootElement.getGenericXFTElement())),foreign,user, _rootQueries);
			String arcTableName = DisplayManager.ARC_MAP + rootElement.getSQLName()+"_"+foreign.getSQLName();

			DisplayField rDF = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayField fDF = foreign.getDisplayField(foreignField);
			this.addField(rDF.getPrimarySchemaField());
			String[] layers = GenericWrapperElement.TranslateXMLPathToTables(rDF.getPrimarySchemaField());
			this.addFieldToJoin(layers);
			qo.addField(fDF.getPrimarySchemaField());

			sb.append(" LEFT JOIN (").append(arcMapQuery);
			sb.append(") ").append(arcTableName).append(" ON ").append(getTableAndFieldSQL(rDF.getPrimarySchemaField())).append("=").append(arcTableName);
			sb.append(".").append(rootElement.getSQLName()).append("_").append(distinctField);

         	qo.setWhere(buildStatusCriteria(foreign,level));
         	
			String query = qo.buildQuery();
			sb.append(" LEFT JOIN (").append(query);
			sb.append(") AS ").append(foreign.getSQLName()).append(" ON ").append(arcTableName);
			sb.append(".").append(foreign.getSQLName()).append("_");
			sb.append(distinctField).append("=").append(GetTableAndFieldSQL(foreign.getSQLName(),fDF.getPrimarySchemaField()));
		}
        return sb.toString();
    }

    private String getSchemaLinkCount(final SchemaLink sl, final SchemaElementI foreign) throws Exception
    {
        StringBuilder sb = new StringBuilder();

        final String foreignTable = foreign.getSQLName();
		Mapping map = sl.getMapping();

		sb.append("SELECT ");

		final String tableName = map.getTableName();
        final String columns = StringUtils.join(Iterables.filter(Lists.transform(map.getColumns(), new Function<Object, String>() {
            @Nullable
            @Override
            public String apply(final Object object) {
                final MappingColumn column = (MappingColumn) object;
                try {
                    final SchemaElementI element = SchemaElement.GetElement(column.getRootElement());
                    if (element.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName())) {
                        return tableName + "." + column.getMapsTo();
                    }
                } catch (XFTInitException | ElementNotFoundException e) {
                    log.error("An error occurred trying to get a column link", e);
                }
                return null;
            }
        }), Predicates.notNull()), ", ");
        sb.append(columns);

		sb.append(", COUNT(*) AS ").append("COUNT ");
		sb.append(" FROM ").append(tableName);
        sb.append(" LEFT JOIN ").append(foreign.getSQLName());
		sb.append(" ON ");

        sb.append(StringUtils.join(Iterables.filter(Lists.transform(map.getColumns(), new Function<Object, String>() {
            @Nullable
            @Override
            public String apply(final Object object) {
                final MappingColumn column = (MappingColumn) object;
                try {
                    final SchemaElementI element = SchemaElement.GetElement(column.getRootElement());
                    if (element.getFullXMLName().equalsIgnoreCase(foreign.getFullXMLName())) {
                        return GetTableAndFieldSQL(foreignTable, column.getFieldElementXMLPath()) + "=" + tableName + "." + column.getMapsTo();
                    }
                } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
                    log.error("An error occurred trying to get a column link", e);
                }
                return null;
            }
        }), Predicates.notNull()), " AND "));

		sb.append(" ORDER BY ").append(columns).append(" GROUP BY ").append(columns);
		return sb.toString();
    }

    private String getSchemaLinkJoin(final SchemaLink sl, final QueryOrganizer qo, final SchemaElementI foreign) throws Exception {
        final StringBuilder sb = new StringBuilder();
        final String foreignTable = foreign.getSQLName();
		final Mapping map = sl.getMapping();

		sb.append(" LEFT JOIN ").append(map.getTableName()).append(" ").append(map.getTableName());
		sb.append(" ON ");
        sb.append(StringUtils.join(Iterables.filter(Lists.transform(map.getColumns(), new Function() {
            @Nullable
            @Override
            public Object apply(final Object object) {
                final MappingColumn column = (MappingColumn) object;
                try {
                    final SchemaElementI element = SchemaElement.GetElement(column.getRootElement());
                    if (element.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName())) {
                        addField(column.getFieldElementXMLPath());
                        return getTableAndFieldSQL(column.getFieldElementXMLPath()) + "=" + map.getTableName() + "." + column.getMapsTo();
                    } else {
                        qo.addField(column.getFieldElementXMLPath());
                    }
                } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
                    log.error("An error occurred trying to get a column link", e);
                }
                return null;
            }
        }), Predicates.notNull()), " AND "));

		final String f = qo.buildQuery();

		sb.append(" LEFT JOIN (").append(f).append(") ").append(foreign.getSQLName());
		sb.append(" ON ");

        sb.append(StringUtils.join(Iterables.filter(Lists.transform(map.getColumns(), new Function() {
            @Nullable
            @Override
            public Object apply(final Object object) {
                final MappingColumn column = (MappingColumn) object;
                try {
                    final SchemaElementI element = SchemaElement.GetElement(column.getRootElement());
                    if (element.getFullXMLName().equalsIgnoreCase(foreign.getFullXMLName())) {
                        return qo.translateXMLPath(column.getFieldElementXMLPath(), foreignTable) + "=" + map.getTableName() + "." + column.getMapsTo();
                    }
                } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
                    log.error("An error occurred trying to get a column link", e);
                }
                return null;
            }
        }), Predicates.notNull()), " AND "));

		return sb.toString();
    }
    
    public String buildQuery() throws Exception{
    	return buildQuery(true);
    }

    /**
     * The big mamba jamba.
     * This method will build the SQL query that can be executed on the database to retrieve the requested data.
     * @param distinct : should a distinct clause be added to the root of the query
     * @return The resulting query.
     * @exception Exception When something goes wrong.

     */
    public String buildQuery(boolean distinct) throws Exception
    {
        final StringBuilder sb = new StringBuilder();

        final String rootElementFullXMLName = rootElement.getFullXMLName();
        String rootFilterField = getFilterField(this.getRootElement().getFullXMLName());
        if (rootFilterField != null)
        {
                log.debug("'{}' query: adding field for root filter field '{}'", rootElementFullXMLName, rootFilterField);
            addField(rootFilterField);
        }

        for (final Object object : rootElement.getAllPrimaryKeys()) {
		    final SchemaFieldI sf = (SchemaFieldI) object;
            final String xmlPathString = sf.getXMLPathString(rootElementFullXMLName);
            log.debug("'{}' query: adding field for primary key field '{}'", rootElementFullXMLName, xmlPathString);
            addField(xmlPathString);
        }

        if (getAddOns().isEmpty())
        {
          rootFilterField = null;
        }

        String join = buildJoin();
        sb.append("SELECT ");
        Iterator fieldIter = getAllFields().iterator();
        int counter=0;
        ArrayList selected = new ArrayList();
        while (fieldIter.hasNext())
        {
            String s = (String) fieldIter.next();
            if (!selected.contains(s.toLowerCase()))
            {
                selected.add(s.toLowerCase());
                String element = XftStringUtils.GetRootElementName(s);
                SchemaElementI se = SchemaElement.GetElement(element);
                if (rootElementFullXMLName.equalsIgnoreCase(se.getFullXMLName()))
                {
                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                    assert layers != null;
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".") + 1);
                    String colName   = layers[2];

					          String viewColName = ViewManager.GetViewColumnName(se.getGenericXFTElement(),s,ViewManager.ACTIVE,true,true);


                    if (tableAliases.get(tableName)!= null)
                    {
                        tableName = (String)tableAliases.get(tableName);
                    }else {
                        String tableNamePath = layers[0];
                        if (tables.get(tableNamePath) != null)
                        {
                            tableName = (String)tables.get(tableNamePath);
                        }
                    }
                    final String alias;
                    if (viewColName==null)
                    {
                        alias = XftStringUtils.CreateAlias(tableName, colName);
                    }else{
                        alias = XftStringUtils.Last62Chars(viewColName);
                    }
                    if (!selected.contains(alias.toLowerCase()))
                    {
                        selected.add(alias.toLowerCase());
//	                    if (!selected.contains(tableName.toLowerCase() + "_" + colName.toLowerCase()))
//	                    {
//	                        selected.add(tableName.toLowerCase() + "_" + colName.toLowerCase());
	                        if (counter++==0)
	                        {
	                            sb.append(tableName).append(".").append(colName).append(" AS ").append(alias);
	                        }else{
	                            sb.append(", ").append(tableName).append(".").append(colName).append(" AS ").append(alias);
	                        }

	                        fieldAliases.put(s.toLowerCase(),alias);
//	                    }else{
//	                        fieldAliases.put(s.toLowerCase(),alias);
//	                    }
		            }
                }else{
                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                    assert layers != null;
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".") + 1);
                    String colName   = layers[2];

                    String viewColName = ViewManager.GetViewColumnName(se.getGenericXFTElement(),s,ViewManager.ACTIVE,true,true);

                    if (tableAliases.get(tableName)!= null)
                    {
                        tableName = (String)tableAliases.get(tableName);
                    }
//                    if (! tableName.startsWith(se.getSQLName()))
//                    {
//                        tableName = se.getSQLName() + "_" + tableName;
//                    }
                    String alias;
                    if (viewColName!=null)
                    {
                        alias = XftStringUtils.CreateAlias(tableName, viewColName);
                    }else{
                        alias = XftStringUtils.CreateAlias(tableName, colName);
                    }
                  if (! alias.startsWith(se.getSQLName()))
                  {
                      alias = XftStringUtils.CreateAlias(se.getSQLName(), alias);
                  }
                    if (!selected.contains(alias.toLowerCase()))
		                {
		                selected.add(alias.toLowerCase());
	                    QueryOrganizer subQO = getAddOns().get(se.getFullXMLName());
                      String subName = subQO.translateXMLPath(s);

                      if(containsOptimizedJoin(SchemaElement.GetElement(rootElement.getFullXMLName()),SchemaElement.GetElement(subQO.rootElement.getFullXMLName()))){
                          //this field has been manually joined via an optimized strategy.
                          tableName=(String)subQO.getTableAliases().get(tableName);
                          subName=colName;
                      }else{
                          tableName=se.getSQLName();
                      }

	                    if (!selected.contains(tableName.toLowerCase() + "_" + colName.toLowerCase() + alias.toLowerCase()))
	                    {
	                        selected.add(tableName.toLowerCase() + "_" + colName.toLowerCase() + alias.toLowerCase());
		                    if (counter++==0)
		                    {
		                        sb.append(tableName).append(".").append(subName).append(" AS ").append(alias);
		                    }else{
		                        sb.append(", ").append(tableName).append(".").append(subName).append(" AS ").append(alias);
		                    }

		                    fieldAliases.put(s.toLowerCase(),alias);
	                    }else{
	                        fieldAliases.put(s.toLowerCase(),alias);
	                    }
		            }else{
		                fieldAliases.put(s.toLowerCase(),alias);
		            }
                }
            }
        }

        for (final Object object : getAllViews()) {
            String xmlPath = (String) object;
            String elementName = XftStringUtils.GetRootElementName(xmlPath);
            String s = xmlPath.substring(xmlPath.indexOf(".")+1);

            if (!selected.contains(s.toLowerCase()))
            {
                selected.add(s.toLowerCase());
                if (s.startsWith("COUNT_"))
                {
                    s = s.substring(6);
                    String foreignElement = s.substring(0,s.indexOf("."));
                    SchemaElementI foreign = SchemaElement.GetElement(foreignElement);
                    s = foreign.getSQLName() + "_COUNT";
                }

	            String temp = StringUtils.replace(s, ".", "_");
	            if (! selected.contains(temp.toLowerCase()))
	            {
	                selected.add(temp.toLowerCase());
		            String alias = temp;
		            SchemaElement se = SchemaElement.GetElement(elementName);
                if(counter++>0){
                    sb.append(", ");
                }
                  //This is where view references could be hijacted for alternate implementations (like Calculated Columns)
                  //commented out code was a POC of this injection, left for future information if pursued later
//                if(StringUtils.equals(s,"SUB_PROJECTS.PROJECTS")){
//                    sb.append("xnat_subjectdata.xs_projects").append(" AS ").append(temp);
//                    fieldAliases.put(xmlPath.toLowerCase(),temp);
//                    continue;
//                }

		            if (se.getFullXMLName().equalsIgnoreCase(rootElementFullXMLName) || containsOptimizedJoin(SchemaElement.GetElement(rootElement.getFullXMLName()),se))
		            {
		                sb.append(s).append(" AS ").append(temp);
		            }else{
		                alias = XftStringUtils.RegCharsAbbr(se.getSQLName()) + "_" + temp;
		                sb.append(se.getSQLName()).append(".").append(temp).append(" AS ").append(alias);
		            }

		            fieldAliases.put(xmlPath.toLowerCase(),alias);
	            }else{
                    fieldAliases.put(s.toLowerCase(),temp);
                }
            }
        }

        for (final Object object : getAllSubqueries()) {
            String xmlPath = (String) object;
            String elementName = XftStringUtils.GetRootElementName(xmlPath);
            String s = xmlPath.substring(xmlPath.indexOf(".")+1);

            if (!selected.contains(s.toLowerCase()))
            {
                selected.add(s.toLowerCase());
                if (s.startsWith("SUBQUERYFIELD_"))
                {
                    s = s.substring(14);
                }

                String temp = XftStringUtils.cleanColumnName(StringUtils.replace(s, ".", "_"));
                if (! selected.contains(temp.toLowerCase()))
                {
                    selected.add(temp.toLowerCase());
                    String alias = temp;
                    SchemaElement se = SchemaElement.GetElement(elementName);
                    SQLQueryField df = (SQLQueryField) se.getDisplayField(s.substring(0, s.indexOf(".")));
                    s=temp + "." + df.getSQLContent(this);
                    if (se.getFullXMLName().equalsIgnoreCase(rootElementFullXMLName) || containsOptimizedJoin(SchemaElement.GetElement(rootElement.getFullXMLName()),se))
                    {
                        if (counter++==0)
                        {
                            sb.append(s).append(" AS ").append(temp);
                        }else{
                            sb.append(", ").append(s).append(" AS ").append(temp);
                        }
                    }else{
                        alias = XftStringUtils.RegCharsAbbr(se.getSQLName()) + "_" + temp;
                        if (counter++==0)
                        {
                            sb.append(se.getSQLName()).append(".").append(temp).append(" AS ").append(alias);
                        }else{
                            sb.append(", ").append(se.getSQLName()).append(".").append(temp).append(" AS ").append(alias);
                        }
                    }

                    fieldAliases.put(xmlPath.toLowerCase(),alias);
                }else{
                    fieldAliases.put(s.toLowerCase(),temp);
                }
            }
        }

        sb.append(join);
        
        if (getAddOns().size()>0)
        {
            final StringBuilder select = new StringBuilder("SELECT ");

            //the distinct clause is optional depending on usage
            if (distinct && !isMappingTable)
            {
                select.append("DISTINCT ON (");
                select.append(StringUtils.join(Lists.transform(rootElement.getAllPrimaryKeys(), new Function<Object, String>() {
                    @Override
                    public String apply(final Object object) {
                        final SchemaFieldI sf = (SchemaFieldI) object;
                        return fieldAliases.get(sf.getXMLPathString(rootElementFullXMLName).toLowerCase());
                    }
                }), ", "));
                select.append(")");
            }

            select.append(" *");

            final List<String> searchOrder = new ArrayList<>();
            for (final Object object : getOrderBys(rootFilterField)) {
                final String[] s = (String[]) object;
                if (!selected.contains(s[0].toLowerCase())) {
                    select.append(", ").append(s[0]).append(" AS ").append(s[2]);
                }
                searchOrder.add(s[1] == null ? s[0] : s[0] + " " + s[1]);
            }

            select.append(" FROM (");
            select.append(sb.toString());
            select.append(") SEARCH ORDER BY ");
            select.append(StringUtils.join(searchOrder, ", "));

            return select.toString();
        }
        return sb.toString();
    }

    /**
     * Returns a list of ORDER BY classes in the form of a list of string arrays. Each array contains:
     *
     * <ul>
     *     <li>SQL for the ORDER BY</li>
     *     <li>ASC or DESC</li>
     *     <li>Alias</li>
     * </ul>
     *
     * @param rootFilterField The root filter field on which the order bys should be based.
     *
     * @return Returns a list of ORDER BY clauses, where each clause is actually an array of strings.
     *
     * @throws ElementNotFoundException When an element in the query can't be found.
     * @throws FieldNotFoundException When an field in the query can't be found.
     */
    private ArrayList getOrderBys(String rootFilterField) throws ElementNotFoundException,FieldNotFoundException
    {
        ArrayList orderBys = new ArrayList();//
		if (rootFilterField==null)
		{
		    for (final Object object : rootElement.getAllPrimaryKeys()) {
                SchemaFieldI sf = (SchemaFieldI) object;
			    String field = sf.getXMLPathString(rootElement.getFullXMLName());
			    String alias = fieldAliases.get(field.toLowerCase());
			    String[] s = new String[3];
		        s[0] = alias;
		        s[2] = alias;
		        orderBys.add(s);
			}

            for (final String key : addOns.keySet()) {
		        String foreignFilter = getFilterField(key);
		        try {
                    SchemaElement foreign = SchemaElement.GetElement(key);

                    if(isOptimizedField(SchemaElement.GetElement(rootElement.getFullXMLName()),foreign)){
                        continue;
                    }
                    SchemaFieldI foreignF = SchemaElement.GetSchemaField(foreignFilter);
                    String foreignT = foreignF.getGenericXFTField().getXMLType().getFullForeignType();

                    String foreignAlias = fieldAliases.get(foreignFilter.toLowerCase());

                    if (XMLType.IsDate(foreignT))
                    {
                        String[] s = new String[3];
                        s[0] = foreignAlias;
                        s[1] = "DESC";
                        s[2] = XftStringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
                        orderBys.add(s);
                    }else{
                        String[] s = new String[3];
                        s[0] = foreignAlias;
                        s[1] = "ASC";
                        s[2] = XftStringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
                        orderBys.add(s);
                    }
                } catch (XFTInitException e) {
                    log.error("",e);
                }
		    }
		}else{
		    SchemaFieldI rootF = SchemaElement.GetSchemaField(rootFilterField);
		    String rootAlias = fieldAliases.get(rootFilterField.toLowerCase());
            for (final Object object : rootElement.getAllPrimaryKeys()) {
                SchemaFieldI sf = (SchemaFieldI) object;
			    String field = sf.getXMLPathString(rootElement.getFullXMLName());
			    String alias = fieldAliases.get(field.toLowerCase());
			    String[] s = new String[3];
		        s[0] = alias;
		        s[2] = alias;
		        orderBys.add(s);
			}

            for (final String key : addOns.keySet()) {
		        String foreignFilter = getFilterField(key);
                try {
                    SchemaElementI foreign = SchemaElement.GetElement(key);
                    if(isOptimizedField(SchemaElement.GetElement(rootElement.getFullXMLName()),SchemaElement.GetElement(foreign.getFullXMLName()))){
                        continue;
                    }

                    SchemaFieldI foreignF = SchemaElement.GetSchemaField(foreignFilter);
                    String localT = rootF.getGenericXFTField().getXMLType().getLocalType();
                    String foreignT = foreignF.getGenericXFTField().getXMLType().getLocalType();

                    String foreignAlias = fieldAliases.get(foreignFilter.toLowerCase());

                    if (localT.equals(foreignT))
                    {
                        if (localT.equalsIgnoreCase("dateTime") || localT.equalsIgnoreCase("timestamp") || localT.equalsIgnoreCase("time"))
                        {
                            String[] s = new String[3];
                            s[0] = "EXTRACT( MILLISECONDS FROM (" + rootAlias + "-" + foreignAlias + "))";
                            s[1] = "ASC";
                            s[2] = XftStringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
                            orderBys.add(s);
                        }else{
                            String[] s = new String[3];
                            s[0] = "(" + rootAlias + "-" + foreignAlias + ")";
                            s[1] = "ASC";
                            s[2] = XftStringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
                            orderBys.add(s);
                        }
                    }else{
                        if (XMLType.IsDate(foreignT))
                        {
                            String[] s = new String[3];
                            s[0] = foreignAlias;
                            s[1] = "DESC";
                            s[2] = XftStringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
                            orderBys.add(s);
                        }else{
                            String[] s = new String[3];
                            s[0] = foreignAlias;
                            s[1] = "ASC";
                            s[2] = XftStringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName() + "_DIFF");
                            orderBys.add(s);
                        }
                    }
                } catch (XFTInitException e) {
                    log.error("",e);
                }
		    }
		}
		return orderBys;
    }

	public ArrayList getAllFields()
    {
        final ArrayList<String> al = new ArrayList<>(fields);
        for (final String root : addOns.keySet()) {
            al.addAll(addOns.get(root).getAllFields());
        }
        return al;
    }

    public ArrayList getAllViews()
    {
        final ArrayList<String> al = new ArrayList(viewFields);
        for (final String root : addOns.keySet()) {
            al.addAll(addOns.get(root).getAllViews());
        }
        return al;
    }
    public ArrayList getAllSubqueries()
    {
        final ArrayList<String> al = new ArrayList<>(subqueryFields);
        for (final String root : addOns.keySet()) {
            al.addAll(addOns.get(root).getAllSubqueries());
        }
        return al;
    }

    /**
     * @return Returns the addOns.
     */
    public Map<String, QueryOrganizer> getAddOns() {
        return addOns;
    }
    /**
     * @param addOns The addOns to set.
     */
    public void setAddOns(final Map<String, QueryOrganizer> addOns) {
        this.addOns.clear();
        this.addOns.putAll(addOns);
    }

    public String translateXMLPath(String xmlPath) throws FieldNotFoundException
    {
        if (xmlPath.startsWith("VIEW_"))
        {
            xmlPath = xmlPath.substring(5);
        }else if (xmlPath.startsWith("SUBQUERY_"))
        {
            xmlPath = xmlPath.substring(5);
        }else{
            try {
                SchemaFieldI f = SchemaElement.GetSchemaField(xmlPath);
                if (f.isReference())
                {
                    SchemaElementI foreign = f.getReferenceElement();
                    SchemaFieldI sf = (SchemaFieldI)foreign.getAllPrimaryKeys().get(0);
                    xmlPath = xmlPath + sf.getXMLPathString("");
                }
            } catch (Exception e) {
                log.error("",e);
            }
        }

        try {
            String temp = ViewManager.GetViewColumnName(rootElement.getGenericXFTElement(),xmlPath,ViewManager.DEFAULT_LEVEL,true,true);
            if (temp !=null && fieldAliases.containsValue(temp))
            {
                return temp;
            }
        } catch (XFTInitException | ElementNotFoundException e1) {
            log.error("",e1);
        }

        String copy=xmlPath;
        if(copy.startsWith("/")){
        	copy=copy.substring(1);
        }

        if (fieldAliases.get(copy.toLowerCase())==null)
        {
            return getTableAndFieldSQL(xmlPath);
        }else{
            return fieldAliases.get(copy.toLowerCase());
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
        }else{
            try {
                SchemaFieldI f = SchemaElement.GetSchemaField(xmlPath);
                if (f.isReference())
                {
                    SchemaElementI foreign = f.getReferenceElement();
                    SchemaFieldI sf = (SchemaFieldI)foreign.getAllPrimaryKeys().get(0);
                    xmlPath = xmlPath + sf.getXMLPathString("");
                }
            } catch (Exception e) {
                log.error("",e);
            }

            if (tableAlias==null || tableAlias.equalsIgnoreCase(""))
	        {
	            return translateXMLPath(xmlPath);
	        }

			try {
                String temp = ViewManager.GetViewColumnName(rootElement.getGenericXFTElement(),xmlPath,ViewManager.DEFAULT_LEVEL,true,true);
                if (temp !=null)
                {
                    return tableAlias + "." + temp;
                }
            } catch (XFTInitException | ElementNotFoundException e1) {
                log.error("",e1);
            }

            if (fieldAliases.get(xmlPath.toLowerCase())==null)
	        {
	            return QueryOrganizer.GetTableAndFieldSQL(tableAlias,xmlPath);
	        }else{
	            return tableAlias + "." + fieldAliases.get(xmlPath.toLowerCase());
	        }

        }

    }

    /****************************************
     * Hardcoded ManualJoin objects to produce optimmized joins
     */
    public static class ManualJoin
    {
        private final String _srcType;
        private final String _destType;
        private final String[] _join;

        public ManualJoin(String srcType, String destType, String joinPre, String joinPost) {
            this._srcType = srcType;
            this._destType = destType;
            this._join = new String[]{joinPre,joinPost};
        }
    }

    public final static List<ManualJoin> manualJoins=Lists.newArrayList(
        new ManualJoin("xnat:subjectData","xnat:demographicData","FROM xnat_demographicData xnat_demographicData", " LEFT JOIN xnat_demographicData ON xnat_subjectData.demographics_xnat_abstractdemographicdata_id=xnat_demographicData.xnat_abstractdemographicdata_id"),
        new ManualJoin("xnat:subjectAssessorData","xnat:subjectData", "FROM xnat_subjectData xnat_subjectData", " LEFT JOIN xnat_subjectData ON %2$s.subject_id=xnat_subjectData.id"),
        new ManualJoin("xnat:imageScanData","xnat:imageSessionData", "FROM %1$s %1$s", " LEFT JOIN %1$s ON %2$s.image_session_id=%1$s.id"),
        new ManualJoin("xnat:imageAssessorData","xnat:imageSessionData", "FROM %1$s %1$s", " LEFT JOIN %1$s ON %2$s.imagesession_id=%1$s.id"));
}
