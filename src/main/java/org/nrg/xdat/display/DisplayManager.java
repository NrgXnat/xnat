/*
 * org.nrg.xdat.display.DisplayManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.display;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.collections.DisplayFieldCollection;
import org.nrg.xdat.collections.DisplayFieldRefCollection;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.search.QueryOrganizer;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.DataModelDefinition;
import org.nrg.xft.schema.XFTDataModel;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.NodeUtils;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xft.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * @author Tim
 *
 */
public class DisplayManager {
	static Logger logger = Logger.getLogger(DisplayManager.class);
	public static final String DISPLAY_FIELDS_VIEW = "displayfields_";
//	private static final String LINKED_TABLE = "linked_";
//	private static final String SCHEMA_LINK = "schemaLink_";
//	private static final String SCHEMA_LINK_MAPPING = "schemaLink_mapping_";
	public static final String ARC_MAP = "arc_map_";
	private Hashtable elements = new Hashtable();
	private static DisplayManager instance = null;
	private ArrayList schemaLinks = new ArrayList();//Object[] elementName,SchemaLink
	private Hashtable arcDefinitions = new Hashtable();
	private static Hashtable SQL_FUNCTIONS = new Hashtable();
	/**
	 * @return
	 */
	public Hashtable getElements() {
		return elements;
	}

	/**
	 * @param hashtable
	 */
	public void setElements(Hashtable hashtable) {
		elements = hashtable;
	}

	public void addElement(ElementDisplay ed)
	{
		elements.put(ed.getElementName(),ed);
	}
	
	
	
	public static DisplayManager GetInstance()
	{
		if (instance == null)
		{
			instance = new DisplayManager();
			instance.init();
		}
		return instance;
	}
	
	public static ElementDisplay GetElementDisplay(String s)
	{
		return (ElementDisplay)GetInstance().getElements().get(s);
	}
	
	/**
	 * @param doc
	 */
	public void assignDisplays(Document doc)
	{
		Element root = doc.getDocumentElement();
		
		String name = NodeUtils.GetAttributeValue(root,"schema-element","");
		
		ElementDisplay ed = GetElementDisplay(name);
		if (ed == null)
		{
			ed = new ElementDisplay();
			ed.setElementName(name);
		}
		
		String temp = "";
		temp = NodeUtils.GetAttributeValue(root,"value_field","");
		if (! temp.equalsIgnoreCase(""))
		{
			ed.setValueField(temp);
		}
		
		temp = NodeUtils.GetAttributeValue(root,"display_field","");
		if (! temp.equalsIgnoreCase(""))
		{
			ed.setDisplayField(temp);
		}
				
		temp = NodeUtils.GetAttributeValue(root,"display_label","");
		if (! temp.equalsIgnoreCase(""))
		{
			ed.setDisplayLabel(temp);
		}
		
		temp = NodeUtils.GetAttributeValue(root,"brief-description","");
		if (! temp.equalsIgnoreCase(""))
		{
			ed.setBriefDescription(temp);
		}
		
		temp = NodeUtils.GetAttributeValue(root,"full-description","");
		if (! temp.equalsIgnoreCase(""))
		{
			ed.setFullDescription(temp);
		}
		
		int views = 0;
		int functions = 0;
		
		NodeList nodes = root.getChildNodes();
		for(int i=0;i<nodes.getLength();i++)
		{
			if ( nodes.item(i).getNodeName().equalsIgnoreCase("DisplayField"))
			{		
				Node child1 = nodes.item(i);				
				DisplayField df = null;
                if (NodeUtils.GetAttributeValue(child1, "xsi:type", "").equals("SubQueryField")){
                    df = new SQLQueryField(ed);
                }else{
                    df=new DisplayField(ed);
                }
	
				df.setId(NodeUtils.GetAttributeValue(child1,"id",""));
				df.setHeader(NodeUtils.GetAttributeValue(child1,"header",""));
				df.setImage(NodeUtils.GetAttributeValue(child1,"image","false"));
				df.setVisible(NodeUtils.GetAttributeValue(child1,"visible","true"));
				df.setSearchable(NodeUtils.GetAttributeValue(child1,"searchable","false"));
				df.setDataType(NodeUtils.GetAttributeValue(child1,"data-type",null));
				df.setSortBy(NodeUtils.GetAttributeValue(child1,"sort-by",""));
				df.setSortOrder(NodeUtils.GetAttributeValue(child1,"sort-order",""));
				df.setHtmlContent(NodeUtils.GetBooleanAttributeValue(child1,"html-content",false));
	
                
                
				for (int k=0;k<child1.getChildNodes().getLength();k++)
				{
					Node child2 = child1.getChildNodes().item(k);
					if ( child2.getNodeName().equalsIgnoreCase("DisplayFieldElement"))
					{	
						DisplayFieldElement dfe = new DisplayFieldElement();
			
						dfe.setName(NodeUtils.GetAttributeValue(child2,"name",""));
						dfe.setSchemaElementName(NodeUtils.GetAttributeValue(child2,"schema-element",""));
						dfe.setViewColumn(NodeUtils.GetAttributeValue(child2,"viewColumn",""));
						dfe.setViewName(NodeUtils.GetAttributeValue(child2,"viewName",""));
						dfe.setXdatType(NodeUtils.GetAttributeValue(child2,"xdat-type",""));
			
						df.addDisplayFieldElement(dfe);
					}else if ( child2.getNodeName().equalsIgnoreCase("Content"))
					{
						String type = NodeUtils.GetAttributeValue(child2,"type","sql");
						String value = child2.getFirstChild().getNodeValue();
						df.getContent().put(type,value);
					}else if ( child2.getNodeName().equalsIgnoreCase("description"))
					{
						String value = child2.getFirstChild().getNodeValue();
						df.setDescription(value);
					}else if ( child2.getNodeName().equalsIgnoreCase("HTML-Link"))
					{
						HTMLLink htmlLink = new HTMLLink();
						for (int l=0;l<child2.getChildNodes().getLength();l++)
						{
							Node child3 = child2.getChildNodes().item(l);
							if ( child3.getNodeName().equalsIgnoreCase("Property"))
							{	
								HTMLLinkProperty prop = new HTMLLinkProperty();
								prop.setName(NodeUtils.GetAttributeValue(child3,"name",""));
								prop.setValue(NodeUtils.GetAttributeValue(child3,"value",""));
					
								for (int m=0;m<child3.getChildNodes().getLength();m++)
								{
									Node child4 = child3.getChildNodes().item(m);
									if (child4.getNodeName().equalsIgnoreCase("InsertValue"))
									{
										String id = NodeUtils.GetAttributeValue(child4,"id","");
										String field = NodeUtils.GetAttributeValue(child4,"field","");
										if (!id.equalsIgnoreCase("") && !field.equalsIgnoreCase(""))
										{
											prop.addInsertedValue(id,field);
										}
									}
								}
					
								htmlLink.addProperty(prop);
							}else if ( child3.getNodeName().equalsIgnoreCase("SecureLink"))
							{	
								htmlLink.setSecureLinkTo(NodeUtils.GetAttributeValue(child3,"elementName",""));
								for (int m=0;m<child3.getChildNodes().getLength();m++)
								{
									Node child4 = child3.getChildNodes().item(m);
									if (child4.getNodeName().equalsIgnoreCase("securityMappingValue"))
									{
										String id = NodeUtils.GetAttributeValue(child4,"displayFieldId","");
										String field = StringUtils.StandardizeXMLPath(NodeUtils.GetAttributeValue(child4,"schemaElementMap",""));
										if (!id.equalsIgnoreCase("") && !field.equalsIgnoreCase(""))
										{
											htmlLink.getSecureProps().put(id,field);
										}
									}
								}
							}  
						}
			
						df.setHtmlLink(htmlLink);
					}else if ( child2.getNodeName().equalsIgnoreCase("HTML-Cell"))
					{
						df.getHtmlCell().setWidth(NodeUtils.GetAttributeValue(child2,"width",""));
						df.getHtmlCell().setHeight(NodeUtils.GetAttributeValue(child2,"height",""));
						df.getHtmlCell().setValign(NodeUtils.GetAttributeValue(child2,"valign",null));
						df.getHtmlCell().setAlign(NodeUtils.GetAttributeValue(child2,"align",null));
						df.getHtmlCell().setServerLink(NodeUtils.GetAttributeValue(child2,"serverLink",null));
					}else if ( child2.getNodeName().equalsIgnoreCase("HTML-Image"))
					{
						df.getHtmlImage().setWidth(NodeUtils.GetAttributeValue(child2,"width",""));
						df.getHtmlImage().setHeight(NodeUtils.GetAttributeValue(child2,"height",""));
					}else if ( child2.getNodeName().equalsIgnoreCase("SubQuery"))
                    {   
                        if (df instanceof SQLQueryField){
                            String value = child2.getFirstChild().getNodeValue();
                            ((SQLQueryField)df).setSubQuery(value);
                        }
                    }else if ( child2.getNodeName().equalsIgnoreCase("MappingColumns"))
                    {   
                        if (df instanceof SQLQueryField){
                            SQLQueryField sqf = (SQLQueryField)df;
                            for (int l=0;l<child2.getChildNodes().getLength();l++)
                            {
                                Node child3 = child2.getChildNodes().item(l);
                                if ( child3.getNodeName().equalsIgnoreCase("MappingColumn"))
                                {   
                                    String schemaField = NodeUtils.GetAttributeValue(child3,"schemaField","");
                                    String queryField = NodeUtils.GetAttributeValue(child3,"queryField","");
                                    
                                    sqf.addMappingColumn(schemaField, queryField);
                                }
                            }
                        }
                    }  
				}
	
				try {
					ed.addDisplayFieldWException(df);
				} catch (DisplayFieldCollection.DuplicateDisplayFieldException e) {
                    logger.error(df.getParentDisplay().getElementName() + "." + df.getId());
					logger.error("",e);
				}
			}else if ( nodes.item(i).getNodeName().equalsIgnoreCase("DisplayVersion"))
			{
				Node displayVersion = nodes.item(i);
				DisplayVersion dv = new DisplayVersion();
				
				dv.setVersionName(NodeUtils.GetAttributeValue(displayVersion,"versionName","default"));
				dv.setDefaultOrderBy(NodeUtils.GetAttributeValue(displayVersion,"default-order-by",""));
				dv.setDefaultSortOrder(NodeUtils.GetAttributeValue(displayVersion,"default-sort-order","ASC"));
				dv.setBriefDescription(NodeUtils.GetAttributeValue(displayVersion,"brief-description",""));
				dv.setDarkColor(NodeUtils.GetAttributeValue(displayVersion,"dark-color",""));
				dv.setLightColor(NodeUtils.GetAttributeValue(displayVersion,"light-color",""));
				dv.setAllowDiffs(NodeUtils.GetBooleanAttributeValue(displayVersion,"allow-diff-columns",true));

				ed.addVersion(dv);
				
				for (int j=0;j<displayVersion.getChildNodes().getLength();j++)
				{
					Node child1 = displayVersion.getChildNodes().item(j);
					if ( child1.getNodeName().equalsIgnoreCase("DisplayFieldRef"))
					{						
						DisplayFieldRef df = new DisplayFieldRef(dv);
						df.setElementName(NodeUtils.GetAttributeValue(child1,"element_name",""));
						df.setId(NodeUtils.GetAttributeValue(child1,"id",""));
						df.setType(NodeUtils.GetAttributeValue(child1,"type",null));
                        df.setValue(NodeUtils.GetAttributeValue(child1,"value",null));
						df.setHeader(NodeUtils.GetAttributeValue(child1,"header",null));
						df.setVisible(NodeUtils.GetAttributeValue(child1,"visible",null));
						try {
						    if (df.getElementName().equals(""))
						    {
								ed.getDisplayFieldWException(df.getId());
						    }
							dv.addDisplayField(df);
						} catch (DisplayFieldRefCollection.DuplicateDisplayFieldRefException e) {
							logger.error("",e);
						} catch (DisplayFieldCollection.DisplayFieldNotFoundException e) {
							logger.error("",e);
						}
					}else if ( child1.getNodeName().equalsIgnoreCase("HTML-Header"))
					{
						dv.getHeaderCell().setWidth(NodeUtils.GetAttributeValue(child1,"width",""));
						dv.getHeaderCell().setHeight(NodeUtils.GetAttributeValue(child1,"height",""));
						dv.getHeaderCell().setValign(NodeUtils.GetAttributeValue(child1,"valign",null));
						dv.getHeaderCell().setAlign(NodeUtils.GetAttributeValue(child1,"align",null));
						dv.getHeaderCell().setServerLink(NodeUtils.GetAttributeValue(child1,"serverLink",null));
					}
				}
			}else if ( nodes.item(i).getNodeName().equalsIgnoreCase("SQLView"))
			{
				SQLView sql = new SQLView();
				sql.setName(NodeUtils.GetAttributeValue(nodes.item(i),"name",""));
				sql.setSql(NodeUtils.GetAttributeValue(nodes.item(i),"sql",""));
				sql.setSortOrder(views++);
				ed.addView(sql);
			}else if ( nodes.item(i).getNodeName().equalsIgnoreCase("SQLFunction"))
			{
				SQLFunction sql = new SQLFunction();
				sql.setName(NodeUtils.GetAttributeValue(nodes.item(i),"name",""));
				sql.setContent(NodeUtils.GetAttributeValue(nodes.item(i),"content",""));
				sql.setSortOrder(functions++);
				AddSqlFunction(sql);
			}else if ( nodes.item(i).getNodeName().equalsIgnoreCase("Arc-Definition"))
			{
				Node child = nodes.item(i);
				ArcDefinition arcDefine = new ArcDefinition();
				arcDefine.setName(NodeUtils.GetAttributeValue(child,"Id",""));
				for (int j=0;j<child.getChildNodes().getLength();j++)
				{
					Node child1 = child.getChildNodes().item(j);
					if ( child1.getNodeName().equalsIgnoreCase("CommonField"))
					{
						String id = NodeUtils.GetAttributeValue(child1,"id","");
						String type = NodeUtils.GetAttributeValue(child1,"type","");
						arcDefine.addCommonField(id,type);
					}else if ( child1.getNodeName().equalsIgnoreCase("Bridge-Element"))
					{
						arcDefine.setBridgeElement(NodeUtils.GetAttributeValue(child1,"name",null));
						arcDefine.setBridgeField(NodeUtils.GetAttributeValue(child1,"field",null));
					}else if ( child1.getNodeName().equalsIgnoreCase("Filter"))
					{
						String field =NodeUtils.GetAttributeValue(child1,"field",null);
						String filter =NodeUtils.GetAttributeValue(child1,"filterType",null);
						arcDefine.addFilter(field,filter);
					}
				}
								
				this.addArcDefinition(arcDefine);
			}else if ( nodes.item(i).getNodeName().equalsIgnoreCase("Arc"))
			{
				Node child = nodes.item(i);
				Arc arc = new Arc();
				arc.setName(NodeUtils.GetAttributeValue(child,"name",""));
				for (int j=0;j<child.getChildNodes().getLength();j++)
				{
					Node child1 = child.getChildNodes().item(j);
					if ( child1.getNodeName().equalsIgnoreCase("CommonField"))
					{
						String id = NodeUtils.GetAttributeValue(child1,"id","");
						String type = NodeUtils.GetAttributeValue(child1,"local-field","");
						arc.addCommonField(id,type);
					}
				}
				ed.addArc(arc);
			}else if (nodes.item(i).getNodeName().equalsIgnoreCase("SchemaLink"))
			{
				Node child1 = nodes.item(i);
				SchemaLink link = new SchemaLink(ed.getElementName());
				link.setElement(NodeUtils.GetAttributeValue(child1,"element",""));
				link.setType(NodeUtils.GetAttributeValue(child1,"type",""));
				link.setAlias(NodeUtils.GetAttributeValue(child1,"alias",""));
						
				for (int k=0;k<child1.getChildNodes().getLength();k++)
				{
					Node child2 = child1.getChildNodes().item(k);
							
					if (child2.getNodeName().equalsIgnoreCase("Mapping"))
					{
						Mapping m = new Mapping();
						m.setTableName(NodeUtils.GetAttributeValue(child2,"TableName",""));
								
						for (int l=0;l<child2.getChildNodes().getLength();l++)
						{
							Node child3 = child2.getChildNodes().item(l);
							if ( child3.getNodeName().equalsIgnoreCase("MappingColumn"))
							{
								MappingColumn mc = new MappingColumn();
								mc.setFieldElementXMLPath(NodeUtils.GetAttributeValue(child3,"fieldElement",""));
								mc.setMapsTo(NodeUtils.GetAttributeValue(child3,"mapsTo",""));
								mc.setRootElement(NodeUtils.GetAttributeValue(child3,"rootElement",""));
								m.addColumn(mc);
							}
						}
						link.setMapping(m);
					}
				}
						
				ed.addSchemaLink(link);
				addSchemaLink(link);
			}else if ( nodes.item(i).getNodeName().equalsIgnoreCase("ViewLink"))
			{
				Node child1 = nodes.item(i);
				ViewLink link = new ViewLink();
				link.setAlias(NodeUtils.GetAttributeValue(child1,"alias",""));
				for (int k=0;k<child1.getChildNodes().getLength();k++)
				{
					Node child2 = child1.getChildNodes().item(k);
	
					if (child2.getNodeName().equalsIgnoreCase("Mapping"))
					{
						Mapping m = new Mapping();
						m.setTableName(NodeUtils.GetAttributeValue(child2,"TableName",""));
		
						for (int l=0;l<child2.getChildNodes().getLength();l++)
						{
							Node child3 = child2.getChildNodes().item(l);
							if ( child3.getNodeName().equalsIgnoreCase("MappingColumn"))
							{
								MappingColumn mc = new MappingColumn();
								mc.setFieldElementXMLPath(NodeUtils.GetAttributeValue(child3,"fieldElement",""));
								mc.setMapsTo(NodeUtils.GetAttributeValue(child3,"mapsTo",""));
								mc.setRootElement(NodeUtils.GetAttributeValue(child3,"rootElement",""));
								m.addColumn(mc);
							}
						}
						link.setMapping(m);
					}
				}

				ed.addViewLink(link);
			}
		}
		this.addElement(ed);
	}
	
	public static void clean()
	{
		instance = null;
	}
	public void init()
	{
		Enumeration enumer = XFTManager.GetDataModels().elements();
		Hashtable hash = new Hashtable();
		while (enumer.hasMoreElements())
		{
			XFTDataModel dm = (XFTDataModel)enumer.nextElement();
			String location = dm.getFileLocation();
			location = FileUtils.AppendSlash(location);
			
			if (! hash.containsKey(location))
			{
				hash.put(location,location);
				File folder = new File(location + "display");
				if (folder.exists())
				{
					File[] files = folder.listFiles();
					for (int i=0;i<files.length;i++)
					{
						if (files[i].getName().endsWith("_display.xml"))
						{
							Document doc = XMLUtils.GetDOM(files[i]);
							assignDisplays(doc);
						}
					}
				}
			}
		}
		
		for(DataModelDefinition annotation: XFTManager.discoverDataModelDefs()){
			for(String s:annotation.getDisplayDocs()){
				if(!StringUtils.IsEmpty(s)){
					InputStream in=annotation.getClass().getClassLoader().getResourceAsStream(s);

					if(in!=null){
						Document doc = XMLUtils.GetDOM(in);
						assignDisplays(doc);
					}
				}
			}
		}
		
		try {
			initArcs();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void initArcs() throws Exception
	{
		Enumeration enumer = elements.keys();
		while (enumer.hasMoreElements())
		{
			String elementName = (String)enumer.nextElement();
			ElementDisplay ed = (ElementDisplay)elements.get(elementName);
			Enumeration arcs = ed.getArcs().keys();
			while (arcs.hasMoreElements())
			{
				String arcName = (String)arcs.nextElement();
				ArcDefinition arcDefine = getArcDefinition(arcName);
				if (arcDefine==null)
				{
					throw new Exception("INVALID ARC:" + arcName);
				}else
				arcDefine.addMember(elementName);
			}
		}
	}
	
	public static List<String> GetCreateViewsSQL(boolean isUpdate)
	{
		ArrayList drops = new ArrayList();
		ArrayList views = new ArrayList();
		

		views.add("GRANT ALL ON TABLE xdat_search.xs_item_access TO public;");
		
		Object[] col = GetInstance().getElements().values().toArray();

		//CREATE FUNCTIONS
		Iterator functions = GetSortedFunctions().iterator();
		while(functions.hasNext())
		{
		    SQLFunction function = (SQLFunction)functions.next();
		    String content = function.getContent();
		    if (content.indexOf("CREATE TYPE ")!=-1)
		    {
		    	try {
					if(PoolDBUtils.checkIfTypeExists(function.getName().trim())){
						continue;
					}
				} catch (Exception e) {
					logger.error("",e);
				}
			    if (content.endsWith(";"))
			    {
			        content =content.substring(0,content.length()-1);
			    }
		    }else{

			    if (!content.endsWith(";"))
			    {
			        content +=";";
			    }
		    }
		    views.add("--DEFINED FUNCTION\n" + content + "\n\n");
		}
		
		ArrayList createdAlias = new ArrayList();
		for (int i=0;i<col.length;i++)
		{
		    ElementDisplay ed = (ElementDisplay)col[i];
                logger.debug("CREATE VIEWS FOR " + ed.getElementName());
                Iterator iter = ed.getSortedViews().iterator();
                while (iter.hasNext())
                {
                	SQLView view = (SQLView)iter.next();
                	drops.add("\n--DEFINED VIEW\nDROP VIEW " + view.getName() + ";");
                	views.add("--DEFINED VIEW\nCREATE OR REPLACE VIEW " + view.getName() + " AS "+view.getSql()+";\n\n");
                }
                	
                SchemaElementI root =null;
                try {
                    root = SchemaElement.GetElement(ed.getElementName());
                    
	                try {
		                DisplaySearch ds = new DisplaySearch();
		                ds.setRootElement(ed.getElementName());
		                Iterator dfs = ed.getSortedFields().iterator();
		                while (dfs.hasNext())
		                {
		                    DisplayField df = (DisplayField)dfs.next();
                            if (! (df instanceof SQLQueryField))
                                ds.addDisplayField(df);
		                }
	                
	                    String query = ds.getSQLQuery(null);
	                    
	                    String viewName = DISPLAY_FIELDS_VIEW + root.getGenericXFTElement().getSQLName();
	                    if (! createdAlias.contains(viewName))
	                    {
	                    	createdAlias.add(viewName);
	                    	views.add("--DISPLAY LINK\nCREATE OR REPLACE VIEW " + viewName + " AS " + query + ";\n\n");
	                    }
	                } catch (Exception e1) {
	                    logger.error("Error in Display Document for '" + root.getFullXMLName() + "'.\n" + e1.getMessage());
	                }
                } catch (XFTInitException e) {
                } catch (ElementNotFoundException e) {
                    logger.error("Error in Display Document.  \nNo such schema-element '" + ed.getElementName() + "'.");
                }
		
		}

		
		views.add("CREATE OR REPLACE FUNCTION xdat_search_create(\"varchar\",\"varchar\")"+
				"\n  RETURNS \"varchar\" AS"+
				"\n'"+
				"\n    declare"+
				"\n        search_query_name alias for $1;"+
				"\n        search_query alias for $2;"+
				"\n	entry xdat_searches%ROWTYPE;"+
				"\n    begin"+
				"\n	SELECT * INTO entry FROM xdat_searches WHERE search_name = search_query_name;"+
				"\n"+
				"\n	    IF FOUND THEN"+
				"\n		RAISE NOTICE ''Search Table % exists.'',"+
				"\n		  search_query_name;"+
				"\n		UPDATE xdat_searches SET last_access=NOW() WHERE search_name = search_query_name;"+
				"\n	    ELSE"+
				"\n		RAISE NOTICE ''Creating Search Table %.'',"+
				"\n		  search_query_name;"+
				"\n		EXECUTE ''CREATE TABLE '' || search_query_name || '' AS '' || search_query;"+
				"\n		INSERT INTO xdat_searches (search_name) VALUES (search_query_name);"+
				"\n     EXECUTE ''GRANT ALL ON TABLE '' || search_query_name || '' TO public'';"+
				"\n	    END IF;"+
				"\n"+
				"\n	PERFORM xdat_search_drop_unused();"+
				"\n"+
				"\n	RETURN ''DONE'';"+
				"\n    end;"+
				"\n'"+
				"\n  LANGUAGE 'plpgsql' VOLATILE;");

		views.add("CREATE OR REPLACE FUNCTION xdat_search_create(\"varchar\", \"varchar\", \"varchar\")"+
				"\n  RETURNS \"varchar\" AS"+
				"\n'"+
				"\n    declare"+
				"\n        search_query_name alias for $1;"+
				"\n        search_query alias for $2;"+
				"\n        search_owner alias for $3;"+
				"\n	entry xdat_searches%ROWTYPE;"+
				"\n    begin"+
				"\n	SELECT * INTO entry FROM xdat_searches WHERE search_name = search_query_name;"+
				"\n"+
				"\n	    IF FOUND THEN"+
				"\n		RAISE NOTICE ''Search Table % exists.'',"+
				"\n		  search_query_name;"+
				"\n		UPDATE xdat_searches SET last_access=NOW() WHERE search_name = search_query_name;"+
				"\n	    ELSE"+
				"\n		RAISE NOTICE ''Creating Search Table %.'',"+
				"\n		  search_query_name;"+
				"\n		EXECUTE ''CREATE TABLE '' || search_query_name || '' AS '' || search_query;"+
				"\n		INSERT INTO xdat_searches (search_name,owner) VALUES (search_query_name,search_owner);"+
				"\n	    END IF;"+
				"\n"+
				"\n	PERFORM xdat_search_drop_unused(search_owner);"+
				"\n"+
				"\n	RETURN ''DONE'';"+
				"\n    end;"+
				"\n'"+
				"\n  LANGUAGE 'plpgsql' VOLATILE;");

		views.add("CREATE OR REPLACE FUNCTION xdat_search_drop(\"varchar\")"+
				"\n  RETURNS \"varchar\" AS"+
				"\n'"+
				"\n    declare"+
				"\n        search_query_name alias for $1;"+
				"\n    begin"+
				"\n	EXECUTE ''DROP TABLE '' || search_query_name;"+
				"\n	DELETE FROM xdat_searches WHERE search_name = search_query_name;"+
				"\n	"+
				"\n	RETURN ''DONE'';"+
				"\n    end;"+
				"\n'"+
				"\n  LANGUAGE 'plpgsql' VOLATILE;");

		views.add("CREATE OR REPLACE FUNCTION xdat_search_drop_unused()"+
				"\n  RETURNS \"varchar\" AS"+
				"\n'"+
				"\n    declare"+
				"\n	entry xdat_searches%ROWTYPE;"+
				"\n    begin"+
				"\n	FOR entry IN SELECT * FROM xdat_searches WHERE last_access + interval ''1 hour'' / int ''2'' < NOW()"+
				"\n	LOOP"+
				"\n		PERFORM xdat_search_drop(entry.search_name);"+
				"\n"+
				"\n		RAISE NOTICE ''Dropped Expired Search Table %. (Last Access: %)'',"+
				"\n		  entry.search_name,entry.last_access;"+
				"\n	END LOOP;"+
				"\n"+
				"\n	RETURN ''DONE'';"+
				"\n    end;"+
				"\n'"+
				"\n  LANGUAGE 'plpgsql' VOLATILE;");

		views.add("CREATE OR REPLACE FUNCTION xdat_search_drop_unused(\"varchar\")"+
				"\n  RETURNS \"varchar\" AS"+
				"\n'"+
				"\n    declare"+
				"\n	entry xdat_searches%ROWTYPE;"+
				"\n        search_owner alias for $1;"+
				"\n    begin"+
				"\n	FOR entry IN SELECT * FROM xdat_searches WHERE owner=search_owner AND last_access + interval ''1 hour'' / int ''2'' < NOW()"+
				"\n	LOOP"+
				"\n		PERFORM xdat_search_drop(entry.search_name);"+
				"\n"+
				"\n		RAISE NOTICE ''Dropped Expired Search Table %. (Last Access: %)'',"+
				"\n		  entry.search_name,entry.last_access;"+
				"\n	END LOOP;"+
				"\n"+
				"\n	RETURN ''DONE'';"+
				"\n    end;"+
				"\n'"+
				"\n  LANGUAGE 'plpgsql' VOLATILE;");

//		try {
//			if(!PoolDBUtils.checkIfTypeExists("sortedstrings")){
//				views.add("CREATE TYPE sortedstrings AS (strings \"varchar\",sort_order int4);");
//			}
//		} catch (Exception e) {
//			logger.error("",e);
//		}

		views.add("CREATE OR REPLACE FUNCTION getnextview()   RETURNS name AS "+
				"\n' DECLARE   my_record RECORD;  viewName name; "+
				"\nBEGIN  FOR my_record IN SELECT c.relname FROM pg_catalog.pg_class AS c LEFT JOIN pg_catalog.pg_namespace AS n ON n.oid = c.relnamespace"+
				"\nWHERE     c.relkind IN (''v'') AND n.nspname NOT IN (''pg_catalog'', ''pg_toast'') AND pg_catalog.pg_table_is_visible(c.oid) LIMIT 1"+
				"\nLOOP   viewName := my_record.relname;  END LOOP;  RETURN (viewName); END; '  LANGUAGE 'plpgsql' VOLATILE;");

		views.add("CREATE OR REPLACE FUNCTION viewcount()   RETURNS int8 AS ' DECLARE   my_record RECORD;  counter int8;"+
				"\nBEGIN  FOR my_record IN SELECT * FROM (SELECT COUNT (c.relname) AS view_count FROM pg_catalog.pg_class AS c "+
				"\nLEFT JOIN pg_catalog.pg_namespace AS n ON n.oid = c.relnamespace WHERE     c.relkind IN (''v'') AND n.nspname "+
				"\nNOT IN (''pg_catalog'', ''pg_toast'') AND pg_catalog.pg_table_is_visible(c.oid) LIMIT 1) AS COUNT_TABLE  LOOP   counter := my_record.view_count;  "+
				"\nEND LOOP;  RETURN (counter); END; '  LANGUAGE 'plpgsql' VOLATILE;");

		views.add("CREATE OR REPLACE FUNCTION getsortedstring(\"varchar\", int4)   RETURNS sortedstrings AS 'DECLARE  sorted_strings sortedStrings%ROWTYPE; "+
				"\nBEGIN  sorted_strings.strings:=$1;  sorted_strings.sort_order:=$2;  return sorted_strings; END;'   LANGUAGE 'plpgsql' VOLATILE;");

		views.add("CREATE OR REPLACE FUNCTION removeviews()   RETURNS varchar AS ' DECLARE  viewName name;  viewCounter int8; "+
				"\nBEGIN  SELECT INTO viewName getnextview();  SELECT INTO viewCounter viewCount();  WHILE (viewCounter > 0)   LOOP"+
				"\nEXECUTE ''DROP VIEW ''|| viewName || '' CASCADE'';   RAISE NOTICE ''DROPPED %. % more.'',viewName,viewCounter;   SELECT INTO viewName getnextview();"+
				"\nSELECT INTO viewCounter viewCount();  END LOOP;   RETURN (''DONE''); END; '   LANGUAGE 'plpgsql' VOLATILE;");

		views.add("CREATE OR REPLACE FUNCTION stringstosortedtable(varchar[])"+
				"\nRETURNS SETOF sortedstrings AS"+
				"\n'DECLARE  "+
				"\nss sortedstrings%ROWTYPE; "+
				"\ni int4;  "+
				"\nBEGIN  "+
				"\ni :=1 ;"+
				"\nWHILE ($1[i] IS NOT NULL) "+
				"\nLOOP   "+
				"\n		FOR ss IN "+
				"\n			SELECT * FROM getSortedString($1[i],i) "+
				"\n		LOOP"+
				"\n			RAISE NOTICE ''SORTED STRING: %,%'',ss.strings,ss.sort_order;"+
				"\n			RETURN NEXT ss;"+
				"\n		END LOOP;"+
				"\n		i:=i+1; "+
				"\n	END LOOP; "+
				"\n	RETURN; "+
				"\nEND;'"+
				"\n   LANGUAGE 'plpgsql' VOLATILE;");

		return views;
	}
	
	public static String GetArcDefinitionQuery(ArcDefinition arcD, SchemaElement root, SchemaElement foreign, UserI user) throws Exception
	{
	    StringBuffer select = new StringBuffer("");
		StringBuffer join = new StringBuffer(" FROM ");
		int joinCounter =0;
		StringBuffer where = new StringBuffer("");
		int whereCounter =0;
		StringBuffer orderBy = new StringBuffer("");
		int orderByCounter =0;
		Arc rootArc =(Arc)root.getDisplay().getArcs().get(arcD.getName());
		Arc foreignArc =(Arc)foreign.getDisplay().getArcs().get(arcD.getName());
		
		QueryOrganizer rootQuery = new QueryOrganizer(root,user,ViewManager.DEFAULT_LEVEL);
		QueryOrganizer foreignQuery = new QueryOrganizer(foreign,user,ViewManager.DEFAULT_LEVEL);
		
		for (Map.Entry<String,String> cf: arcD.getCommonFields().entrySet())
		{
			String id = cf.getKey();
			
			String rootField = (String)rootArc.getCommonFields().get(id);
			String foreignField = (String)foreignArc.getCommonFields().get(id);
			
			DisplayField rDF = root.getDisplayField(rootField);
			DisplayField fDF = foreign.getDisplayField(foreignField);
			
			rootQuery.addField(rDF.getPrimarySchemaField());
			foreignQuery.addField(fDF.getPrimarySchemaField());
		}
		
		String rootString = rootQuery.buildQuery();
		String foreignString = foreignQuery.buildQuery();
		
		join.append("(" + rootString + ") ").append(root.getGenericXFTElement().getSQLName());
		join.append(" LEFT JOIN ").append("(" + foreignString + ") ").append(foreign.getGenericXFTElement().getSQLName());
		
		int counter = 0;

		for (Map.Entry<String,String> cf: arcD.getCommonFields().entrySet())
		{
			String id = cf.getKey();
			
			if (counter++ != 0)
			{
				select.append(", ");
			}
			
			String rootField = (String)rootArc.getCommonFields().get(id);
			String foreignField = (String)foreignArc.getCommonFields().get(id);
			DisplayField rDF = root.getDisplayField(rootField);
			DisplayField fDF = foreign.getDisplayField(foreignField);
			
			select.append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(),root.getSQLName())).append(" AS ");
			select.append(root.getGenericXFTElement().getSQLName()).append("_").append(id);
			
			select.append(",").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(),foreign.getSQLName())).append(" AS ");
			select.append(foreign.getGenericXFTElement().getSQLName()).append("_").append(id);
			
		}

		Iterator filters = arcD.getFilters().iterator();
		while (filters.hasNext())
		{
			String[] filter = (String[])filters.next();
			String filterID = filter[0];
			String filterType = filter[1];
			
			if (filterType.equalsIgnoreCase("equals"))
			{
				if (joinCounter++ == 0)
				{
					join.append(" ON ");
				}else{
					join.append(" AND ");
				}
				
				String rootField = (String)rootArc.getCommonFields().get(filterID);
				String foreignField = (String)foreignArc.getCommonFields().get(filterID);
				DisplayField rDF = root.getDisplayField(rootField);
				DisplayField fDF = foreign.getDisplayField(foreignField);
				
				join.append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(),root.getSQLName()));
				join.append("=").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(),foreign.getSQLName()));
			}else if (filterType.equalsIgnoreCase("distinct"))
			{
				String rootField = (String)rootArc.getCommonFields().get(filterID);
				DisplayField rDF = root.getDisplayField(rootField);
				
				select.insert(0,"SELECT DISTINCT ON ("+ rootQuery.translateXMLPath(rDF.getPrimarySchemaField(),root.getSQLName()) + ") ");
				
				if (orderByCounter++ == 0)
				{
					orderBy.append(" ORDER BY ");
				}else
				{
					orderBy.append(", ");
				}
				orderBy.append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(),root.getSQLName()));
			}else if (filterType.equalsIgnoreCase("closest"))
			{
				String fieldType = (String)arcD.getCommonFields().get(filterID);
				if (fieldType.equalsIgnoreCase("DATE"))
				{
					String rootField = (String)rootArc.getCommonFields().get(filterID);
					String foreignField = (String)foreignArc.getCommonFields().get(filterID);
					DisplayField rDF = root.getDisplayField(rootField);
					DisplayField fDF = foreign.getDisplayField(foreignField);
					
					select.append(", ").append("(").append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(),root.getSQLName()));
					select.append("-").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(),foreign.getSQLName())).append(")");
					select.append(" AS ").append(filterID).append("_DIFF");
					
					if (orderByCounter++ == 0)
					{
						orderBy.append(" ORDER BY ");
					}else
					{
						orderBy.append(", ");
					}
					orderBy.append("abs(").append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(),root.getSQLName()));
					orderBy.append("-").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(),foreign.getSQLName())).append(")");
				}
			}else if (filterType.equalsIgnoreCase("before"))
			{
				String fieldType = (String)arcD.getCommonFields().get(filterID);
				if (fieldType.equalsIgnoreCase("DATE"))
				{
					String rootField = (String)rootArc.getCommonFields().get(filterID);
					String foreignField = (String)foreignArc.getCommonFields().get(filterID);
					DisplayField rDF = root.getDisplayField(rootField);
					DisplayField fDF = foreign.getDisplayField(foreignField);
					
					if (whereCounter++ == 0)
					{
						where.append(" WHERE ");
					}else
					{
						where.append(" AND ");
					}
					where.append(" ").append(rootQuery.translateXMLPath(rDF.getPrimarySchemaField(),root.getSQLName())).append("<=").append(foreignQuery.translateXMLPath(fDF.getPrimarySchemaField(),foreign.getSQLName()));
				}
			}
		}
			
		
		return select.toString() + join.toString() + where.toString() + orderBy.toString();
	}
	/**
	 * @return Object[String elementName, SchemaLink link]
	 */
	public ArrayList getSchemaLinks() {
		return schemaLinks;
	}

	/**
	 * @param list
	 */
	public void setSchemaLinks(ArrayList list) {
		schemaLinks = list;
	}
	
	public void addSchemaLink(SchemaLink link)
	{
		schemaLinks.add(new Object[]{link.getRootElement(),link});
		schemaLinks.add(new Object[]{link.getElement(),link});
	}
	
	public ArrayList getSchemaLinksFor(String elementName)
	{
		ArrayList al = new ArrayList();
		Iterator iter = schemaLinks.iterator();
		while (iter.hasNext())
		{
			Object[] o = (Object[])iter.next();
			if (((String)o[0]).equalsIgnoreCase(elementName))
			{
				al.add((SchemaLink)o[1]);
			}
		}
		return al;
	}

	/**
	 * @return
	 */
	public Hashtable getArcDefinitions() {
		return arcDefinitions;
	}


	/**
	 * @param hashtable
	 */
	public void setArcDefinitions(Hashtable hashtable) {
		arcDefinitions = hashtable;
	}
	
	public void addArcDefinition(ArcDefinition arc)
	{
		arcDefinitions.put(arc.getName(),arc);
	}

	public ArcDefinition getArcDefinition(String name)
	{
		return (ArcDefinition)arcDefinitions.get(name);
	}
	
	/**
	 * Get the ArcDefinition which relates these two elements.
	 * @param root
	 * @param foreign
	 * @return
	 */
	public ArcDefinition getArcDefinition(SchemaElementI root, SchemaElementI foreign)
	{
		ArcDefinition temp = null;
		Iterator arcs = getArcDefinitions().values().iterator();
		while (arcs.hasNext())
		{
			ArcDefinition arcDefine = (ArcDefinition)arcs.next();
			if (arcDefine.getBridgeElement().equalsIgnoreCase(root.getFullXMLName()) &&
			arcDefine.isMember(foreign.getFullXMLName()))
			{
				temp= arcDefine;
				break;
			}else if (arcDefine.getBridgeElement().equalsIgnoreCase(foreign.getFullXMLName()) &&
			arcDefine.isMember(root.getFullXMLName()))
			{
				temp= arcDefine;
				break;
			}
		}
		
		if (temp == null)
		{
			arcs = getArcDefinitions().values().iterator();
			while (arcs.hasNext())
			{
				ArcDefinition arcDefine = (ArcDefinition)arcs.next();
				if (arcDefine.isMember(root.getFullXMLName()) && arcDefine.isMember(foreign.getFullXMLName()))
				{
					temp= arcDefine;
					break;
				}
			}
		}
		
		return temp;
	}
	
	/**
	 * Get all ArcDefinitions for this Element
	 * @param root
	 * @return
	 */
	public ArrayList getArcDefinitions(SchemaElementI root)
	{
	    ArrayList al = new ArrayList();
		Iterator arcs = getArcDefinitions().values().iterator();
		while (arcs.hasNext())
		{
			ArcDefinition arcDefine = (ArcDefinition)arcs.next();
			if (arcDefine.getBridgeElement().equalsIgnoreCase(root.getFullXMLName()))
			{
				al.add(arcDefine);
			}else if (arcDefine.isMember(root.getFullXMLName()))
			{
			    al.add(arcDefine);
			}
		}
		
		al.trimToSize();
		return al;
	}
	
	public static void AddSqlFunction(SQLFunction function)
	{
	    SQL_FUNCTIONS.put(function.getName(),function);
	}
	
    /**
     * @return Returns the sqlFunctions.
     */
    public static Hashtable GetSqlFunctions() {
        return SQL_FUNCTIONS;
    }
    


	public static ArrayList GetSortedFunctions()
	{
		ArrayList temp = new ArrayList();
		temp.addAll(GetSqlFunctions().values());
		Collections.sort(temp,SQLFunction.SequenceComparator);
		return temp;
	}
	
	public String getDisplayNameForElement(String elementName)
	{
	    try {
            SchemaElement se = SchemaElement.GetElement(elementName);
            return se.getSingularDescription();
//            ElementDisplay ed = se.getDisplay();
//            if (ed == null)
//            {
//                return elementName;
//            }else{
//                if (ed.getDescription()==null || ed.getDescription().equals(""))
//                {
//                    return elementName;
//                }else{
//                    return ed.getDescription();
//                }
//            }
        } catch (Exception e) {
            return elementName;
        }
	}

    public String getSingularDisplayNameForElement(String elementName) {
	try {
	    SchemaElement se = SchemaElement.GetElement(elementName);
	    return se.getSingularDescription();
	} catch (Exception e) {
	    return elementName;
	}
    }

    public String getPluralDisplayNameForElement(String elementName) {
	try {
	    SchemaElement se = SchemaElement.GetElement(elementName);
	    return se.getPluralDescription();
	} catch (Exception e) {
	    return elementName;
	}
    }

    public String getSingularDisplayNameForProject() {
	return getSingularDisplayNameForElement("xnat:projectData");
    }

    public String getPluralDisplayNameForProject() {
	return getPluralDisplayNameForElement("xnat:projectData");
    }

    public String getSingularDisplayNameForSubject() {
	return getSingularDisplayNameForElement("xnat:subjectData");
    }

    public String getPluralDisplayNameForSubject() {
	return getPluralDisplayNameForElement("xnat:subjectData");
    }

    /**
     * xnat:imageSessionData is not an instantiable data type, and it seems silly to make it so just to reference the
     * singular/plural display names, so we'll just use a site config property for this one.
     */
    public String getSingularDisplayNameForImageSession() throws ConfigServiceException {
	return org.apache.commons.lang.StringUtils.defaultIfEmpty(
		XDAT.getSiteConfigurationProperty("displayNameForGenericImageSession.singular"), "Session");
    }

    public String getPluralDisplayNameForImageSession() throws ConfigServiceException {
	return org.apache.commons.lang.StringUtils.defaultIfEmpty(
		XDAT.getSiteConfigurationProperty("displayNameForGenericImageSession.plural"), "Sessions");
    }

    public String getSingularDisplayNameForMRSession() {
	return getSingularDisplayNameForElement("xnat:mrSessionData");
    }

    public String getPluralDisplayNameForMRSession() {
	return getPluralDisplayNameForElement("xnat:mrSessionData");
    }
}

