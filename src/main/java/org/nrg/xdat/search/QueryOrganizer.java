//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 9, 2005
 *
 */
package org.nrg.xdat.search;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xdat.display.Arc;
import org.nrg.xdat.display.ArcDefinition;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayFieldElement;
import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.display.Mapping;
import org.nrg.xdat.display.MappingColumn;
import org.nrg.xdat.display.SQLQueryField;
import org.nrg.xdat.display.SchemaLink;
import org.nrg.xdat.display.ViewLink;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.XFT;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.search.QueryOrganizerI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 * FindBugs says this class should be renamed to something other than QueryOrganizer.  I agree.  However,
 * this code is used in alot of places and is due for a big refactoring.  I vote for putting off this fix
 * until the search is refactored.  Unable to suppress warnings to support 1.5. 
 * 
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class QueryOrganizer extends org.nrg.xft.search.QueryOrganizer implements QueryOrganizerI{
	static org.apache.log4j.Logger logger = Logger.getLogger(QueryOrganizer.class);

    private ArrayList viewFields = new ArrayList();
    private ArrayList subqueryFields = new ArrayList();
    private Hashtable addOns = new Hashtable();//0:elementName,1:QueryOrganizer
    private Hashtable subqueries = new Hashtable();


    public QueryOrganizer(String elementName, UserI u, String level) throws ElementNotFoundException
    {
        super(elementName,u,level);
    }

    public QueryOrganizer(SchemaElementI se, UserI u, String level)
    {
        super(se,u,level);
    }

    public void addSubquery(String key, DisplayFieldReferenceI df){
        subqueries.put(key, df);
    }

    /**
     * @param xmlPath
     * @throws ElementNotFoundException
     */
    public void addField(String xmlPath) throws ElementNotFoundException
    {
        if (xmlPath.startsWith("VIEW_"))
        {
            xmlPath = xmlPath.substring(5);
            addView(xmlPath);
        }else if (xmlPath.startsWith("SUBQUERY_"))
        {
            xmlPath = xmlPath.substring(9);
            addSubquery(xmlPath);
        }else{
        	xmlPath = org.nrg.xft.utils.StringUtils.StandardizeXMLPath(xmlPath);
            String root = StringUtils.GetRootElementName(xmlPath);
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
                        logger.error("",e);
                    } catch (ElementNotFoundException e) {
                        logger.error("",e);
                    }
            }else{
                QueryOrganizer qo = (QueryOrganizer)addOns.get(root);
                if (qo == null)
                {
                    qo = new QueryOrganizer(root,user,level);
                    addOns.put(root,qo);
                    addField(qo.getFilterField(root));
                }

                qo.addField(xmlPath);
                try {
                    SchemaElement se = SchemaElement.GetElement(root);
                    String temp = ViewManager.GetViewColumnName(se.getGenericXFTElement(),xmlPath,ViewManager.DEFAULT_LEVEL,true,true);
                    if (temp !=null)
                    {
                        fieldAliases.put(xmlPath.toLowerCase(),StringUtils.CreateAlias(se.getSQLName(),temp));
                    }
                } catch (XFTInitException e) {
                    logger.error("",e);
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                }
            }
        }
    }

    /**
     * @param xmlPath
     * @throws ElementNotFoundException
     */
    public void addSubquery(String xmlPath) throws ElementNotFoundException
    {
        String root = StringUtils.GetRootElementName(xmlPath);
        if (rootElement.getFullXMLName().equalsIgnoreCase(root))
        {
            if (! subqueryFields.contains(xmlPath))
            {
                subqueryFields.add(xmlPath);
            }
        }else{
            QueryOrganizer qo = (QueryOrganizer)addOns.get(root);
            if (qo == null)
            {
                qo = new QueryOrganizer(root,user,level);
                addOns.put(root,qo);
                addField(getFilterField(root));
            }

            qo.addSubquery(xmlPath);
        }
    }

    /**
     * @param xmlPath
     * @throws ElementNotFoundException
     */
    public void addView(String xmlPath) throws ElementNotFoundException
    {
        String root = StringUtils.GetRootElementName(xmlPath);
        if (rootElement.getFullXMLName().equalsIgnoreCase(root))
        {
            if (! viewFields.contains(xmlPath))
            {
                viewFields.add(xmlPath);

            }
        }else{
            QueryOrganizer qo = (QueryOrganizer)addOns.get(root);
            if (qo == null)
            {
                qo = new QueryOrganizer(root,user,level);
                addOns.put(root,qo);
                addField(getFilterField(root));
            }

            qo.addView(xmlPath);
        }
    }

    /**
     * @param viewName
     * @throws Exception
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
                    String alias = FIELDID +"_" + DisplaySearch.cleanColumnName(VALUE);
                    VALUE = StringUtils.ReplaceStr(StringUtils.ReplaceStr(VALUE, "_com_", ","),"_col_",":");


                    StringBuffer sb = new StringBuffer();

                    SQLQueryField df = (SQLQueryField) ed.getDisplayField(FIELDID);
                    String subquery = df.getSubQuery();
                    if (VALUE.indexOf(",")==-1){
                        subquery= StringUtils.ReplaceStr(subquery, "@WHERE", VALUE);
                    }else{
                        ArrayList<String> values = StringUtils.CommaDelimitedStringToArrayList(VALUE);
                        int count =0;
                        for(String value1:values){
                            subquery= StringUtils.ReplaceStr(subquery, "@WHERE" + count++, value1);
                        }
                    }

                    sb.append(" LEFT JOIN (").append(subquery).append(") AS ");
                    sb.append(alias);

                    sb.append(" ON ");
                    int counter = 0;
                    for(SQLQueryField.QueryMappingColumn mc : df.getMappingColumns())
                    {
                        if (counter++ != 0)
                        {
                            sb.append(" AND ");
                        }
                        addFieldToJoin(mc.getSchemaField());
                        sb.append(this.getTableAndFieldSQL(mc.getSchemaField()));
                        sb.append("=");
                        sb.append(alias + "." + mc.getQueryField());
                    }

                    joins.append(sb);
                }else{
                    throw new Exception("No Such Subquery Found. " + viewName);
                }

            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            }
        }
    }

    /**
     * @param viewName
     * @throws Exception
     */
    public void addViewToJoin(String viewName) throws Exception
    {
        if (tables.get(viewName)==null)
        {
            try {
                ElementDisplay ed = ((new SchemaElement(rootElement.getGenericXFTElement()))).getDisplay();
                ViewLink vl = (ViewLink)ed.getViewLinks().get(viewName);
                if (vl != null)
                {
                    Mapping map = vl.getMapping();
                    String[] layers = new String[3];
                    layers[0] = rootElement.getFullXMLName();
                    layers[1] = rootElement.getSQLName();
                    addFieldToJoin(layers);

                    String j = " LEFT JOIN " + map.getTableName() + " " + viewName;
                    j+= " ON ";

                    Iterator mappingColumns = map.getColumns().iterator();
					int counter = 0;
					while (mappingColumns.hasNext())
					{
						MappingColumn mc = (MappingColumn)mappingColumns.next();
						SchemaElementI mappedElement = SchemaElement.GetElement(mc.getRootElement());
						if (mappedElement.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName()))
						{
							if (counter++ != 0)
							{
							    j+=" AND ";
							}
							addFieldToJoin(mc.getFieldElementXMLPath());
							j+=this.getTableAndFieldSQL(mc.getFieldElementXMLPath());
							j+="=";
							j+=viewName + "." + mc.getMapsTo();
						}
					}

					joins.append(" ").append(j);
                    tables.put(viewName,viewName);
                }else{
                    if (viewName.startsWith("COUNT"))
                    {
                        String s = viewName.substring(6);

                        SchemaLink sl = (SchemaLink)ed.getSchemaLinks().get(s);
                        SchemaElement foreign = SchemaElement.GetElement(s);

                        boolean linked = false;
                        if (sl != null)
                        {
                            if (sl.getType().equalsIgnoreCase("mapping"))
            				{
            					joins.append(" ").append(getSchemaLinkCount(sl,foreign));
            					tables.put(viewName,foreign.getSQLName() + "_COUNT");
                                linked = true;
            				}
                        }

                        if (! linked)
                        {
//                          CHECK ARCS
                            ArcDefinition arcDefine = DisplayManager.GetInstance().getArcDefinition(rootElement,foreign);
            				if (arcDefine!=null)
            				{
            				    joins.append(getArcJoinCount(arcDefine,foreign));
            					tables.put(viewName,foreign.getSQLName() + "_COUNT");
                                linked = true;
            				}
                        }

                        if (! linked)
                        {
                            //Look for direct connection
                            String[] connection = this.rootElement.getGenericXFTElement().findSchemaConnection(foreign.getGenericXFTElement());
                            if (connection != null)
                            {
                                QueryOrganizer foreignQO = new QueryOrganizer(foreign,this.getUser(),level);
                                QueryOrganizer localQO = new QueryOrganizer(rootElement,this.getUser(),level);
                                //BUILD CONNECTION FROM ROOT TO EXTENSION
                                String localSyntax = connection[0];
                                String xmlPath = connection[1];
                                SchemaFieldI gwf = null;
                                SchemaElementI extension=null;
                                if (localSyntax.indexOf(XFT.PATH_SEPERATOR)==-1)
                                {
                                    extension = rootElement;
                                }else{
                                    gwf = SchemaElement.GetSchemaField(localSyntax);
                                    extension = gwf.getReferenceElement();
                                }

                                Iterator pks = extension.getAllPrimaryKeys().iterator();
                                while (pks.hasNext())
                                {
                                    SchemaFieldI sf = (SchemaFieldI)pks.next();
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
                    			int pkCount=0;
                                while (pks.hasNext())
                                {
                                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                                    if (pkCount++ != 0)
                                    {
                                        sb.append(" AND ");
                                    }
                                    String localCol = translateXMLPath(localSyntax + sf.getXMLPathString(""),rootElement.getSQLName());
                                    String foreignCol = foreignQO.translateXMLPath(xmlPath + sf.getXMLPathString(""),foreign.getSQLName());

                        			sb.append(localCol).append("=");
                        			sb.append(foreignCol);
                                }

                                pks = rootElement.getAllPrimaryKeys().iterator();
                    			pkCount=0;
                    			String cols="";
                                while (pks.hasNext())
                                {
                                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                                    if (pkCount++ != 0)
                                    {
                                        sb.append(", ");
                                    }
                                    String localCol = translateXMLPath(sf.getXMLPathString(rootElement.getFullXMLName()),rootElement.getSQLName());
                                    cols += localCol;
                                }

                                String subQuery = "SELECT " + cols + ", COUNT(*) AS " + foreign.getSQLName() +"_COUNT ";
                                subQuery += " FROM (" + localQO.buildQuery() + ") " + rootElement.getSQLName() + sb.toString();
                    			subQuery += " GROUP BY " + cols;

                                sb = new StringBuffer();
                    			sb.append(" LEFT JOIN (").append(subQuery);
                    			sb.append(") AS ").append(foreign.getSQLName()).append("_COUNT ON ");
                    			pks = rootElement.getAllPrimaryKeys().iterator();
                    			pkCount=0;
                                while (pks.hasNext())
                                {
                                    SchemaFieldI sf = (SchemaFieldI)pks.next();
                                    if (pkCount++ != 0)
                                    {
                                        sb.append(" AND ");
                                    }
                                    String localCol = this.getTableAndFieldSQL(sf.getXMLPathString(rootElement.getFullXMLName()));
                                    String foreignCol =translateXMLPath(sf.getXMLPathString(rootElement.getFullXMLName()),foreign.getSQLName() + "_COUNT");


                        			sb.append(localCol).append("=");
                        			sb.append(foreignCol);
                                }
                    			joins.append(sb.toString());
                            }
                            //}
                        }
                    }else if (viewName.startsWith("SUBQUERYFIELD"))
                    {

                    }else{
                        throw new Exception("No Such View Found. " + viewName);
                    }
                }
            } catch (XFTInitException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
            }
        }
    }

    /**
     * Builds query which gets all necessary fields from the db for the generation of the Display Fields
     * (Secured).
     * @return
     */
    public String buildJoin() throws Exception
    {
        super.buildJoin();

        //ADD VIEWS
        Iterator viewIter = viewFields.iterator();
        while (viewIter.hasNext())
        {
            String s = (String) viewIter.next();
            String temp =  s.substring(s.indexOf(".") + 1);
            temp =  temp.substring(0,temp.indexOf("."));
            addViewToJoin(temp);
        }

        //ADD SubQueries
        Iterator subQuerieIter = subqueryFields.iterator();
        while (subQuerieIter.hasNext())
        {
            String s = (String) subQuerieIter.next();
            String temp =  s.substring(s.indexOf(".") + 1);
            //temp =  temp.substring(0,temp.indexOf("."));
            addSubqueryToJoin(temp);
        }

        //ADD OTHER SUB-QUERIES
        Enumeration enumer = addOns.keys();
        while (enumer.hasMoreElements())
        {
            String s = (String)enumer.nextElement();
            ElementDisplay ed = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplay();

            SchemaLink sl = null;
            if (ed != null)
            {
                sl = (SchemaLink)ed.getSchemaLinks().get(s);
            }
            QueryOrganizer qo = (QueryOrganizer)addOns.get(s);
            SchemaElement foreign = SchemaElement.GetElement(s);

            boolean linked = false;
            if (sl != null)
            {
                if (sl.getType().equalsIgnoreCase("mapping"))
				{
					joins.append(getSchemaLinkJoin(sl,qo,foreign));
                    tables.put(s,foreign.getSQLName());
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
                    tables.put(s,foreign.getSQLName());
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
                    }else{
                        gwf = SchemaElement.GetSchemaField(localSyntax);
                        extension = gwf.getReferenceElement();

                        QueryOrganizer mappingQO = new QueryOrganizer(this.rootElement,this.getUser(),this.level);
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
                    }
                }else{
                    //NO CONNECTION FOUND

                    //TRY ARC JOINABLE ELEMENTS
                    ArrayList checked = new ArrayList();

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
                                if (CanConnect(arc.getBridgeElement(),s))
                                {
                                    logger.info("Connecting " + rootElement.getFullXMLName() + "->" + arc.getBridgeElement() + "->" + s);
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
	                            if (CanConnect(member,s))
	                            {
	                                logger.info("Connecting " + rootElement.getFullXMLName() + "->" + member + "->" + s);
	                                mappingElement = member;
	                                break;
	                            }
                            }
                        }
                    }

                    if (mappingElement == null){
                        Iterator sls = ed.getSchemaLinks().keySet().iterator();
                        while (sls.hasNext())
                        {
                            String key = (String) sls.next();
                            if (!checked.contains(key))
                            {
                                checked.add(key);
                                if (CanConnect(key,s))
                                {
                                    logger.info("Connecting " + rootElement.getFullXMLName() + "->" + key + "->" + s);
                                    mappingElement = key;
                                    break;
                                }
                            }
                        }
                    }

                    if (mappingElement == null)
                    {
                        throw new Exception("Unable to connect " + rootElement.getFullXMLName() + " to " + s);
                    }else{
                        SchemaElement extension = SchemaElement.GetElement(mappingElement);

                        QueryOrganizer mappingQO = new QueryOrganizer(extension,this.getUser(),ViewManager.ALL);
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

            			StringBuffer sb = new StringBuffer();

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
     * @param foreignElement
     * @return
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
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }

        return false;
    }



    /**
     * 'schemaelement','arc','connection','multi-leveled'
     * @param foreignElement
     * @return
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
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }

        return "multi-leveled";
    }

    private String getArcJoinCount(ArcDefinition arcDefine,SchemaElement foreign) throws Exception
    {
		QueryOrganizer qo = new QueryOrganizer(foreign,this.getUser(),level);
        StringBuffer sb = new StringBuffer();
        if (arcDefine.getBridgeElement().equalsIgnoreCase(rootElement.getFullXMLName()))
		{
			Arc foreignArc = (Arc)foreign.getArcs().get(arcDefine.getName());
			String rootField = arcDefine.getBridgeField();

			String foreignField = (String)foreignArc.getCommonFields().get(arcDefine.getEqualsField());

			DisplayField df = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayFieldElement dfe =(DisplayFieldElement)df.getElements().get(0);
			this.addField(dfe.getSchemaElementName());
			String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
			this.addFieldToJoin(layers);

			String localCol = this.getTableAndFieldSQL(dfe.getSchemaElementName());

			DisplayField df2 = foreign.getDisplayField(foreignField);
			DisplayFieldElement dfe2 =(DisplayFieldElement)df2.getElements().get(0);
			qo.addField(dfe2.getSchemaElementName());

			String query = qo.buildQuery();

			String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName() +"_COUNT");

			String subQuery = "SELECT " + foreignCol + ", COUNT(*) AS " + foreign.getSQLName() + "_COUNT ";
			subQuery += " FROM (" + query + ") " + foreign.getSQLName() + "_COUNT ";
			subQuery += " GROUP BY " + foreignCol;

			sb.append(" LEFT JOIN (").append(subQuery);
			sb.append(") AS ").append(foreign.getSQLName() + "_COUNT ON ");
			sb.append(localCol).append("=");
			sb.append(foreignCol);
		}else if (arcDefine.getBridgeElement().equalsIgnoreCase(foreign.getFullXMLName()))
		{
			Arc rootArc = (Arc)(new SchemaElement(rootElement.getGenericXFTElement())).getArcs().get(arcDefine.getName());

			String foreignField = arcDefine.getBridgeField();
			String rootField = (String)rootArc.getCommonFields().get(arcDefine.getEqualsField());

			DisplayField df = (new SchemaElement(rootElement.getGenericXFTElement())).getDisplayField(rootField);
			DisplayFieldElement dfe =(DisplayFieldElement)df.getElements().get(0);
			this.addField(dfe.getSchemaElementName());
			String[] layers = GenericWrapperElement.TranslateXMLPathToTables(dfe.getSchemaElementName());
			this.addFieldToJoin(layers);
			String localCol = getTableAndFieldSQL(dfe.getSchemaElementName());

			DisplayField df2 = foreign.getDisplayField(foreignField);
			DisplayFieldElement dfe2 =(DisplayFieldElement)df2.getElements().get(0);
			qo.addField(dfe2.getSchemaElementName());

			String query = qo.buildQuery();

			String foreignCol = qo.translateXMLPath(dfe2.getSchemaElementName(),foreign.getSQLName() +"_COUNT");

			String subQuery = "SELECT " + foreignCol + ", COUNT(*) AS " + foreign.getSQLName() + "_COUNT ";
			subQuery += " FROM (" + query + ") " + foreign.getSQLName() + "_COUNT ";
			subQuery += " GROUP BY " + foreignCol;

			sb.append(" LEFT JOIN (").append(subQuery);
			sb.append(") AS ").append(foreign.getSQLName() + "_COUNT ON ");
			sb.append(localCol).append("=");
			sb.append(foreignCol);
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
			String[] layers = GenericWrapperElement.TranslateXMLPathToTables(rDF.getPrimarySchemaField());
			this.addFieldToJoin(layers);
			qo.addField(fDF.getPrimarySchemaField());

			sb.append(" LEFT JOIN (").append(arcMapQuery);
			sb.append(") ").append(arcTableName).append(" ON ").append(getTableAndFieldSQL(rDF.getPrimarySchemaField())).append("=").append(arcTableName);
			sb.append(".").append(rootElement.getSQLName()).append("_").append(distinctField);

			String query = qo.buildQuery();
			sb.append(" LEFT JOIN (").append(query);
			sb.append(") AS ").append(foreign.getSQLName()).append(" ON ").append(arcTableName);
			sb.append(".").append(foreign.getSQLName()).append("_");
			sb.append(distinctField).append("=").append(GetTableAndFieldSQL(foreign.getSQLName(),fDF.getPrimarySchemaField()));
		}
        return sb.toString();
    }

    private String getSchemaLinkCount(SchemaLink sl, SchemaElementI foreign) throws Exception
    {
        StringBuffer sb = new StringBuffer();

        String foreignTable = foreign.getSQLName();
		Mapping map = sl.getMapping();

		sb.append("SELECT ");

		Iterator mappingColumns = map.getColumns().iterator();
		int counter = 0;
		while (mappingColumns.hasNext())
		{
			MappingColumn mc = (MappingColumn)mappingColumns.next();
			SchemaElementI mappedElement = SchemaElement.GetElement(mc.getRootElement());
			if (mappedElement.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName()))
			{
				if (counter++ != 0)
				{
					sb.append(", ");
				}
				sb.append(map.getTableName()).append(".").append(mc.getMapsTo());
			}
		}

		sb.append(", COUNT(*) AS ").append("COUNT ");

		sb.append(" FROM " + map.getTableName());


		mappingColumns = map.getColumns().iterator();

		sb.append(" LEFT JOIN ").append(foreign.getSQLName());
		sb.append(" ON ");

		counter=0;
		mappingColumns = map.getColumns().iterator();
		while (mappingColumns.hasNext())
		{
			MappingColumn mc = (MappingColumn)mappingColumns.next();
			SchemaElementI mappedElement = SchemaElement.GetElement(mc.getRootElement());
			if (mappedElement.getFullXMLName().equalsIgnoreCase(foreign.getFullXMLName()))
			{

				if (counter++ != 0)
				{
					sb.append(" AND ");
				}
				sb.append(GetTableAndFieldSQL(foreignTable,mc.getFieldElementXMLPath()));
				sb.append("=");
				sb.append(map.getTableName()).append(".").append(mc.getMapsTo());
			}
		}

		mappingColumns = map.getColumns().iterator();
		counter = 0;
		String cols = "";
		while (mappingColumns.hasNext())
		{
			MappingColumn mc = (MappingColumn)mappingColumns.next();
			SchemaElementI mappedElement = SchemaElement.GetElement(mc.getRootElement());
			if (mappedElement.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName()))
			{
				if (counter++ != 0)
				{
				    cols += ", ";
				}
				cols += map.getTableName()+ "." + mc.getMapsTo();
			}
		}

		sb.append(" ORDER BY ").append(cols).append(" GROUP BY ").append(cols);

		String query = " LEFT JOIN (" + sb.toString() + ") " + foreign.getSQLName() + "_COUNT ON ";
		mappingColumns = map.getColumns().iterator();
		counter = 0;
		while (mappingColumns.hasNext())
		{
			MappingColumn mc = (MappingColumn)mappingColumns.next();
			SchemaElementI mappedElement = SchemaElement.GetElement(mc.getRootElement());
			if (mappedElement.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName()))
			{
				if (counter++ != 0)
				{
					query +=" AND ";
				}
				addField(mc.getFieldElementXMLPath());
				query +=this.getTableAndFieldSQL(mc.getFieldElementXMLPath());
				query +="=";
				query += foreign.getSQLName() + "_COUNT." + mc.getMapsTo();
			}
		}

		return sb.toString();
    }

    private String getSchemaLinkJoin(SchemaLink sl, QueryOrganizer qo, SchemaElementI foreign) throws Exception
    {
        StringBuffer sb = new StringBuffer();

        String foreignTable = foreign.getSQLName();
		Mapping map = sl.getMapping();


		sb.append(" LEFT JOIN ").append(map.getTableName()).append(" ").append(map.getTableName());
		sb.append(" ON ");
		Iterator mappingColumns = map.getColumns().iterator();
		int counter = 0;
		while (mappingColumns.hasNext())
		{
			MappingColumn mc = (MappingColumn)mappingColumns.next();
			SchemaElementI mappedElement = SchemaElement.GetElement(mc.getRootElement());
			if (mappedElement.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName()))
			{
				if (counter++ != 0)
				{
					sb.append(" AND ");
				}
				addField(mc.getFieldElementXMLPath());
				sb.append(this.getTableAndFieldSQL(mc.getFieldElementXMLPath()));
				sb.append("=");
				sb.append(map.getTableName()).append(".").append(mc.getMapsTo());
			}else{
			    qo.addField(mc.getFieldElementXMLPath());
			}
		}


		mappingColumns = map.getColumns().iterator();

		String f = qo.buildQuery();

		sb.append(" LEFT JOIN (").append(f).append(") ").append(foreign.getSQLName());
		sb.append(" ON ");

		counter=0;
		mappingColumns = map.getColumns().iterator();
		while (mappingColumns.hasNext())
		{
			MappingColumn mc = (MappingColumn)mappingColumns.next();
			SchemaElementI mappedElement = SchemaElement.GetElement(mc.getRootElement());
			if (mappedElement.getFullXMLName().equalsIgnoreCase(foreign.getFullXMLName()))
			{

				if (counter++ != 0)
				{
					sb.append(" AND ");
				}
				sb.append(qo.translateXMLPath(mc.getFieldElementXMLPath(),foreignTable));
				sb.append("=");
				sb.append(map.getTableName()).append(".").append(mc.getMapsTo());
			}
		}
		return sb.toString();
    }

    public String buildQuery() throws Exception
    {
        StringBuffer sb = new StringBuffer();

		String rootFilterField = null;

		rootFilterField = getFilterField(this.getRootElement().getFullXMLName());
		if (rootFilterField != null)
		{
		    addField(rootFilterField);
		}

		Iterator keys = rootElement.getAllPrimaryKeys().iterator();
		while (keys.hasNext())
		{
		    SchemaFieldI sf = (SchemaFieldI)keys.next();
		    addField(sf.getXMLPathString(rootElement.getFullXMLName()));
		}

		if (getAddOns().size() == 0)
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
                String element = StringUtils.GetRootElementName(s);
                SchemaElementI se = SchemaElement.GetElement(element);
                if (rootElement.getFullXMLName().equalsIgnoreCase(se.getFullXMLName()))
                {
                    String[] layers = GenericWrapperElement.TranslateXMLPathToTables(s);
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".")+1);
                    String colName = layers[2];

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
                    String alias = "";
                    if (viewColName==null)
                    {
                        alias = StringUtils.CreateAlias(tableName,colName);
                    }else{
                        alias = StringUtils.Last62Chars(viewColName);
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
                    String tableName = layers[1].substring(layers[1].lastIndexOf(".")+1);
                    String colName = layers[2];

                    String viewColName = ViewManager.GetViewColumnName(se.getGenericXFTElement(),s,ViewManager.ACTIVE,true,true);

                    if (tableAliases.get(tableName)!= null)
                    {
                        tableName = (String)tableAliases.get(tableName);
                    }
//                    if (! tableName.startsWith(se.getSQLName()))
//                    {
//                        tableName = se.getSQLName() + "_" + tableName;
//                    }
                    String alias = "";
                    if (viewColName!=null)
                    {
                        alias = StringUtils.CreateAlias(tableName,viewColName);
                    }else{
                        alias = StringUtils.CreateAlias(tableName,colName);
                    }
                  if (! alias.startsWith(se.getSQLName()))
                  {
                      alias = StringUtils.CreateAlias(se.getSQLName(),alias);
                  }
                    if (!selected.contains(alias.toLowerCase()))
		            {
		                selected.add(alias.toLowerCase());
	                    QueryOrganizer subQO = (QueryOrganizer)getAddOns().get(se.getFullXMLName());
	                    String subName = subQO.translateXMLPath(s);

	                    if (!selected.contains(tableName.toLowerCase() + "_" + colName.toLowerCase() + alias.toLowerCase()))
	                    {
	                        selected.add(tableName.toLowerCase() + "_" + colName.toLowerCase() + alias.toLowerCase());
		                    if (counter++==0)
		                    {
		                        sb.append(se.getSQLName()).append(".").append(subName).append(" AS ").append(alias);
		                    }else{
		                        sb.append(", ").append(se.getSQLName()).append(".").append(subName).append(" AS ").append(alias);
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

        Iterator viewIter = getAllViews().iterator();
        while (viewIter.hasNext())
        {
            String xmlPath = (String) viewIter.next();
            String elementName = StringUtils.GetRootElementName(xmlPath);
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

	            String temp = StringUtils.ReplaceStr(s,".","_");
	            if (! selected.contains(temp.toLowerCase()))
	            {
	                selected.add(temp.toLowerCase());
		            String alias = temp;
		            SchemaElementI se = SchemaElement.GetElement(elementName);
		            if (se.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName()))
		            {
		                if (counter++==0)
		                {
		                    sb.append(s).append(" AS ").append(temp);
		                }else{
		                    sb.append(", ").append(s).append(" AS ").append(temp);
		                }
		            }else{
		                alias = StringUtils.RegCharsAbbr(se.getSQLName()) + "_" + temp;
		                if (counter++==0)
		                {
		                    sb.append(se.getSQLName() + "." + temp).append(" AS ").append(alias);
		                }else{
		                    sb.append(", ").append(se.getSQLName() + "." + temp).append(" AS ").append(alias);
		                }
		            }

		            fieldAliases.put(xmlPath.toLowerCase(),alias);
	            }else{
                    fieldAliases.put(s.toLowerCase(),temp);
                }
            }
        }

        Iterator subQueryIter = getAllSubqueries().iterator();
        while (subQueryIter.hasNext())
        {
            String xmlPath = (String) subQueryIter.next();
            String elementName = StringUtils.GetRootElementName(xmlPath);
            String s = xmlPath.substring(xmlPath.indexOf(".")+1);

            if (!selected.contains(s.toLowerCase()))
            {
                selected.add(s.toLowerCase());
                if (s.startsWith("SUBQUERYFIELD_"))
                {
                    s = s.substring(14);
                }

                String temp = DisplaySearch.cleanColumnName(StringUtils.ReplaceStr(s,".","_"));
                if (! selected.contains(temp.toLowerCase()))
                {
                    selected.add(temp.toLowerCase());
                    String alias = temp;
                    SchemaElement se = SchemaElement.GetElement(elementName);
                    SQLQueryField df = null;
                    df =(SQLQueryField)se.getDisplayField(s.substring(0,s.indexOf(".")));
                    s=temp + "." + df.getSQLContent(this);
                    if (se.getFullXMLName().equalsIgnoreCase(rootElement.getFullXMLName()))
                    {
                        if (counter++==0)
                        {
                            sb.append(s).append(" AS ").append(temp);
                        }else{
                            sb.append(", ").append(s).append(" AS ").append(temp);
                        }
                    }else{
                        alias = StringUtils.RegCharsAbbr(se.getSQLName()) + "_" + temp;
                        if (counter++==0)
                        {
                            sb.append(se.getSQLName() + "." + temp).append(" AS ").append(alias);
                        }else{
                            sb.append(", ").append(se.getSQLName() + "." + temp).append(" AS ").append(alias);
                        }
                    }

                    fieldAliases.put(xmlPath.toLowerCase(),alias);
                }else{
                    fieldAliases.put(s.toLowerCase(),temp);
                }
            }
        }

        sb.append(join);
//        if (securityClause!=null)
//        {
//
//            if (getAddOns().size()>0)
//            {
//                String select = "SELECT ";
//
//                if (!isMappingTable)
//                {
//                    select +="DISTINCT ON (";
//                    keys = rootElement.getAllPrimaryKeys().iterator();
//                    int keyCounter = 0;
//        			while (keys.hasNext())
//        			{
//        			    SchemaFieldI sf = (SchemaFieldI)keys.next();
//        			    String field = sf.getXMLPathString(rootElement.getFullXMLName());
//        			    String alias = (String)fieldAliases.get(field.toLowerCase());
//        			    if (keyCounter++==0)
//        			    {
//        			        select += alias;
//        			    }else{
//        			        select += "," + alias;
//        			    }
//        			}
//        			select += ")";
//                }
//
//    			select +=" *";
//    			ArrayList orderBys = getOrderBys(rootFilterField);
//    			Iterator oBIter = orderBys.iterator();
//    			while (oBIter.hasNext())
//    			{
//    			    String[] s = (String[])oBIter.next();
//    			    if (! selected.contains(s[0].toLowerCase()))
//    			    {
//    			        if (! fieldAliases.values().contains(s[0]))
//    			        {
//        			        select += ", " + s[0] + " AS " + s[2];
//    			        }
//    			    }
//    			}
//
//                String query = "";
//                if (securityQO !=null)
//                {
//                    keys = rootElement.getAllPrimaryKeys().iterator();
//                    ArrayList keyXMLFields = new ArrayList();
//                    while (keys.hasNext())
//                    {
//                        SchemaFieldI sf = (SchemaFieldI)keys.next();
//                        String key =sf.getXMLPathString(rootElement.getFullXMLName());
//                        keyXMLFields.add(key);
//                        securityQO.addField(key);
//                    }
//                    String subQuery = securityQO.buildQuery();
//                    String securityQuery = "SELECT DISTINCT ON (";
//
//                    for (int i=0;i<keyXMLFields.size();i++){
//                        if (i>0)securityQuery +=", ";
//                        securityQuery+=securityQO.getFieldAlias((String)keyXMLFields.get(i));
//                    }
//
//                    securityQuery +=") * FROM (" + subQuery + ") SECURITY WHERE ";
//                    securityQuery +=securityClause.getSQLClause(securityQO);
//
//                    query = "SELECT SEARCH.* FROM ("+ securityQuery +") SECURITY LEFT JOIN (" + sb.toString() + ") SEARCH ON ";
//
//                        keys = rootElement.getAllPrimaryKeys().iterator();
//                        int keyCounter=0;
//                    while (keys.hasNext())
//                    {
//                        SchemaFieldI sf = (SchemaFieldI)keys.next();
//                        String key =sf.getXMLPathString(rootElement.getFullXMLName());
//                        if (keyCounter++>0){
//                            query += " AND ";
//                        }
//                        query +="SECURITY." + securityQO.getFieldAlias(key) + "=SEARCH." + this.getFieldAlias(key);
//
//                    }
//
//                }else{
//                    query = sb.toString();
//                }
//
//    			select +=" FROM (";
//    			select += query;
//    			select += ") SEARCH  ORDER BY ";
//
//    			oBIter = orderBys.iterator();
//    			int oBcounter = 0;
//    			while (oBIter.hasNext())
//    			{
//    			    String[] s = (String[])oBIter.next();
//    			    if (oBcounter++==0)
//    			    {
//    			        select += s[0];
//    			        if (s[1]!=null)
//    			            select+=" " + s[1];
//    			    }else{
//        			    select += ", " + s[0];
//    			        if (s[1]!=null)
//    			            select+=" " + s[1];
//    			    }
//    			}
//
//    			return select;
//            }else{
//                if (securityQO !=null)
//                {
//                    keys = rootElement.getAllPrimaryKeys().iterator();
//                    ArrayList keyXMLFields = new ArrayList();
//                    while (keys.hasNext())
//                    {
//                        SchemaFieldI sf = (SchemaFieldI)keys.next();
//                        String key =sf.getXMLPathString(rootElement.getFullXMLName());
//                        keyXMLFields.add(key);
//                        securityQO.addField(key);
//                    }
//                    String subQuery = securityQO.buildQuery();
//                    String securityQuery = "SELECT DISTINCT ON (";
//
//                    for (int i=0;i<keyXMLFields.size();i++){
//                        if (i>0)securityQuery +=", ";
//                        securityQuery+=securityQO.getFieldAlias((String)keyXMLFields.get(i));
//                    }
//
//                    securityQuery +=") * FROM (" + subQuery + ") SECURITY WHERE ";
//                    securityQuery +=securityClause.getSQLClause(securityQO);
//
//                    String query = "SELECT SEARCH.* FROM ("+ securityQuery +") SECURITY LEFT JOIN (" + sb.toString() + ") SEARCH ON ";
//
//                    keys = rootElement.getAllPrimaryKeys().iterator();
//                    int keyCounter=0;
//                    while (keys.hasNext())
//                    {
//                        SchemaFieldI sf = (SchemaFieldI)keys.next();
//                        String key =sf.getXMLPathString(rootElement.getFullXMLName());
//                        if (keyCounter++>0){
//                            query += " AND ";
//                        }
//                        query +="SECURITY." + securityQO.getFieldAlias(key) + "=SEARCH." + this.getFieldAlias(key);
//
//                    }
//
//                    return query;
//                }else{
//                    return "SELECT * FROM (" + sb.toString() + ") SEARCH WHERE " +securityClause.getSQLClause(this);
//                }
//            }
//        }else{
            if (getAddOns().size()>0)
            {
                String select = "SELECT ";

                if (!isMappingTable)
                {
                    select +="DISTINCT ON (";
                    keys = rootElement.getAllPrimaryKeys().iterator();
                    int keyCounter = 0;
        			while (keys.hasNext())
        			{
        			    SchemaFieldI sf = (SchemaFieldI)keys.next();
        			    String field = sf.getXMLPathString(rootElement.getFullXMLName());
        			    String alias = (String)fieldAliases.get(field.toLowerCase());
        			    if (keyCounter++==0)
        			    {
        			        select += alias;
        			    }else{
        			        select += "," + alias;
        			    }
        			}
        			select += ")";
                }

    			select +=" *";

    			ArrayList orderBys = getOrderBys(rootFilterField);
    			Iterator oBIter = orderBys.iterator();
    			while (oBIter.hasNext())
    			{
    			    String[] s = (String[])oBIter.next();
    			    if (! selected.contains(s[0].toLowerCase()))
    			    {
    			        select += ", " + s[0] + " AS " + s[2];
    			    }
    			}

    			select +=" FROM (";
    			select += sb.toString();
    			select += ") SEARCH ORDER BY ";

    			oBIter = orderBys.iterator();
    			int oBcounter = 0;
    			while (oBIter.hasNext())
    			{
    			    String[] s = (String[])oBIter.next();
    			    if (oBcounter++==0)
    			    {
    			        select += s[0];
    			        if (s[1]!=null)
    			            select+=" " + s[1];
    			    }else{
        			    select += ", " + s[0];
    			        if (s[1]!=null)
    			            select+=" " + s[1];
    			    }
    			}

    			return select;
            }else{
                String select = sb.toString();
    			return select;
            }
 //       }
    }

    /**
     * arraylist of string[2] 0:SQL 1: ASC/DESC 2:ALIAS
     * @param rootFilterField
     * @return
     * @throws ElementNotFoundException
     * @throws FieldNotFoundException
     */
    private ArrayList getOrderBys(String rootFilterField) throws ElementNotFoundException,FieldNotFoundException
    {
        ArrayList orderBys = new ArrayList();//
		if (rootFilterField==null)
		{
		    Iterator keys = rootElement.getAllPrimaryKeys().iterator();
			while (keys.hasNext())
			{
			    SchemaFieldI sf = (SchemaFieldI)keys.next();
			    String field = sf.getXMLPathString(rootElement.getFullXMLName());
			    String alias = (String)fieldAliases.get(field.toLowerCase());
			    String[] s = new String[3];
		        s[0] = alias;
		        s[2] = alias;
		        orderBys.add(s);
			}

		    Enumeration addOnKeys = addOns.keys();
		    while (addOnKeys.hasMoreElements())
		    {
		        String key = (String)addOnKeys.nextElement();
		        String foreignFilter = getFilterField(key);
		        try {
                    SchemaElementI foreign = SchemaElement.GetElement(key);

                    SchemaFieldI foreignF = SchemaElement.GetSchemaField(foreignFilter);
                    String foreignT = foreignF.getGenericXFTField().getXMLType().getFullForeignType();

                    String foreignAlias = (String)fieldAliases.get(foreignFilter.toLowerCase());

                    if (XMLType.IsDate(foreignT))
                    {
                        String[] s = new String[3];
                        s[0] = foreignAlias;
                        s[1] = "DESC";
                        s[2] = StringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName()+ "_DIFF");
                        orderBys.add(s);
                    }else{
                        String[] s = new String[3];
                        s[0] = foreignAlias;
                        s[1] = "ASC";
                        s[2] = StringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName()+ "_DIFF");
                        orderBys.add(s);
                    }
                } catch (XFTInitException e) {
                    logger.error("",e);
                }
		    }
		}else{
		    SchemaFieldI rootF = SchemaElement.GetSchemaField(rootFilterField);
		    String rootAlias = (String)fieldAliases.get(rootFilterField.toLowerCase());

		    Iterator keys = rootElement.getAllPrimaryKeys().iterator();
			while (keys.hasNext())
			{
			    SchemaFieldI sf = (SchemaFieldI)keys.next();
			    String field = sf.getXMLPathString(rootElement.getFullXMLName());
			    String alias = (String)fieldAliases.get(field.toLowerCase());
			    String[] s = new String[3];
		        s[0] = alias;
		        s[2] = alias;
		        orderBys.add(s);
			}

		    Enumeration addOnKeys = addOns.keys();
		    while (addOnKeys.hasMoreElements())
		    {
		        String key = (String)addOnKeys.nextElement();
		        String foreignFilter = getFilterField(key);
                try {
                    SchemaElementI foreign = SchemaElement.GetElement(key);

                    SchemaFieldI foreignF = SchemaElement.GetSchemaField(foreignFilter);
                    String localT = rootF.getGenericXFTField().getXMLType().getLocalType();
                    String foreignT = foreignF.getGenericXFTField().getXMLType().getLocalType();

                    String foreignAlias = (String)fieldAliases.get(foreignFilter.toLowerCase());

                    if (localT.equals(foreignT))
                    {
                        if (localT.equalsIgnoreCase("dateTime") || localT.equalsIgnoreCase("timestamp") || localT.equalsIgnoreCase("time"))
                        {
                            String[] s = new String[3];
                            s[0] = "EXTRACT( MILLISECONDS FROM (" + rootAlias + "-" + foreignAlias + "))";
                            s[1] = "ASC";
                            s[2] = StringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName()+ "_DIFF");
                            orderBys.add(s);
                        }else{
                            String[] s = new String[3];
                            s[0] = "(" + rootAlias + "-" + foreignAlias + ")";
                            s[1] = "ASC";
                            s[2] = StringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName()+ "_DIFF");
                            orderBys.add(s);
                        }
                    }else{
                        if (XMLType.IsDate(foreignT))
                        {
                            String[] s = new String[3];
                            s[0] = foreignAlias;
                            s[1] = "DESC";
                            s[2] = StringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName()+ "_DIFF");
                            orderBys.add(s);
                        }else{
                            String[] s = new String[3];
                            s[0] = foreignAlias;
                            s[1] = "ASC";
                            s[2] = StringUtils.SQLMaxCharsAbbr(rootElement.getSQLName() + "_" + foreign.getSQLName()+ "_DIFF");
                            orderBys.add(s);
                        }
                    }
                } catch (XFTInitException e) {
                    logger.error("",e);
                }
		    }
		}
		return orderBys;
    }

	public ArrayList getAllFields()
    {
        ArrayList al = new ArrayList();
        al.addAll(fields);
        Enumeration enumer = this.addOns.keys();
        while (enumer.hasMoreElements())
        {
            String s = (String)enumer.nextElement();
            QueryOrganizer qo = (QueryOrganizer)addOns.get(s);
            al.addAll(qo.getAllFields());
        }
        al.trimToSize();
        return al;
    }
    public ArrayList getAllViews()
    {
        ArrayList al = new ArrayList();
        al.addAll(viewFields);
        Enumeration enumer = this.addOns.keys();
        while (enumer.hasMoreElements())
        {
            String s = (String)enumer.nextElement();
            QueryOrganizer qo = (QueryOrganizer)addOns.get(s);
            al.addAll(qo.getAllViews());
        }
        al.trimToSize();
        return al;
    }
    public ArrayList getAllSubqueries()
    {
        ArrayList al = new ArrayList();
        al.addAll(subqueryFields);
        Enumeration enumer = this.addOns.keys();
        while (enumer.hasMoreElements())
        {
            String s = (String)enumer.nextElement();
            QueryOrganizer qo = (QueryOrganizer)addOns.get(s);
            al.addAll(qo.getAllSubqueries());
        }
        al.trimToSize();
        return al;
    }

    /**
     * @return Returns the addOns.
     */
    public Hashtable getAddOns() {
        return addOns;
    }
    /**
     * @param addOns The addOns to set.
     */
    public void setAddOns(Hashtable addOns) {
        this.addOns = addOns;
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
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
			} catch (Exception e) {
                logger.error("",e);
            }
        }

        try {
            String temp = ViewManager.GetViewColumnName(rootElement.getGenericXFTElement(),xmlPath,ViewManager.DEFAULT_LEVEL,true,true);
            if (temp !=null && fieldAliases.containsValue(temp))
            {
                return temp;
            }
        } catch (XFTInitException e1) {
            logger.error("",e1);
        } catch (ElementNotFoundException e1) {
            logger.error("",e1);
        }
        
        String copy=xmlPath;
        if(copy.startsWith("/")){
        	copy=copy.substring(1);
        }

        if (fieldAliases.get(copy.toLowerCase())==null)
        {
            return getTableAndFieldSQL(xmlPath);
        }else{
            return (String)fieldAliases.get(copy.toLowerCase());
        }
    }

    public String translateStandardizedPath(String xmlPath) throws FieldNotFoundException{

        if (fieldAliases.get(xmlPath.toLowerCase())==null)
        {
            return getTableAndFieldSQL(xmlPath);
        }else{
            return (String)fieldAliases.get(xmlPath.toLowerCase());
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
                return tableAlias + "." + (String)fieldAliases.get(xmlPath.toLowerCase());
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
            } catch (FieldNotFoundException e) {
                logger.error("",e);
            } catch (ElementNotFoundException e) {
                logger.error("",e);
			} catch (Exception e) {
                logger.error("",e);
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
            } catch (XFTInitException e1) {
                logger.error("",e1);
            } catch (ElementNotFoundException e1) {
                logger.error("",e1);
            }

			if (fieldAliases.get(xmlPath.toLowerCase())==null)
	        {
	            return QueryOrganizer.GetTableAndFieldSQL(tableAlias,xmlPath);
	        }else{
	            return tableAlias + "." + (String)fieldAliases.get(xmlPath.toLowerCase());
	        }

        }

    }
}
