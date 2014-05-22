//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Apr 1, 2004
 */
package org.nrg.xft.generators;

import org.apache.log4j.Logger;
import org.nrg.xft.TypeConverter.TorqueMapping;
import org.nrg.xft.TypeConverter.TypeConverter;
import org.nrg.xft.XFT;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.references.*;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XFTSchema;
import org.nrg.xft.utils.XMLUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
/**
 * Generater class that is used to output a torque schema for the collection of XFTSchemas.
 * 
 * @author Tim
 */
public class TorqueSchemaGenerator {
	static org.apache.log4j.Logger logger = Logger.getLogger(TorqueSchemaGenerator.class);
	/**
	 * outputs a torque schema for the collection of XFTSchemas.
	 * @param location
	 */
	public static void generateDoc(String location)
	{
		try {
			XFTManager manager = XFTManager.GetInstance();
			XMLWriter writer = new XMLWriter();
			Document doc = writer.getDocument();
			
			Element root = doc.createElement("database");
			root.setAttribute("defaultJavaType","object");
			doc.appendChild(root);
			
			TypeConverter converter = null;
			String prefix = "xs";
				
			XFTSchema.OutputSchema();
			XFTSchema schema = null;
			
			Iterator schemas = XFTManager.GetSchemas().iterator();
			while (schemas.hasNext())
			{
				schema = (XFTSchema)schemas.next();
				
				converter = new TypeConverter(new TorqueMapping(schema.getXMLNS()));
				prefix = schema.getXMLNS();
							
				Iterator elements = schema.getWrappedElementsSorted(GenericWrapperFactory.GetInstance()).iterator();
				while (elements.hasNext())
				{
					
					GenericWrapperElement element = (GenericWrapperElement)elements.next();
		logger.debug("Generating Torque schema element for '" + element.getDirectXMLName() + "'");
					Element main = doc.createElement("table");
					if (element.getXMLJavaNameValue() != "")
						main.setAttribute("javaName",element.getXMLJavaNameValue());
					if (element.getAlias() != "")
						main.setAttribute("alias",element.getAlias());
					if (element.getBasePeer() != "")
						main.setAttribute("basePeer",element.getBasePeer());
					if (element.getBaseClass() != "")
						main.setAttribute("baseClass",element.getBaseClass());
					if (element.getIdMethod() != "")
						main.setAttribute("idMethod",element.getIdMethod());
					main.setAttribute("name",element.getFormattedName());
		
					Iterator iter = element.getAllFieldsWAddIns(false,false).iterator();
					while (iter.hasNext())
					{
						GenericWrapperField field = (GenericWrapperField)iter.next();
						if (field.isReference())
						{
							if (! field.isMultiple())
							{
								GenericWrapperElement foreign = GenericWrapperElement.GetElement(field.getXMLType());
								ArrayList primaryKeys = foreign.getAllPrimaryKeys();
								if (field.getXMLSqlNameValue() == "")
								{
									Iterator keys = primaryKeys.iterator();
									while(keys.hasNext())
									{
										GenericWrapperField key = (GenericWrapperField)keys.next();
										Element column = doc.createElement("column");
										if(field.getRequired()!="")
											column.setAttribute("required",field.getRequired());
										if(key.getAdjustedSize()!="")
											column.setAttribute("size",key.getAdjustedSize());
										if(field.getPrimaryKey()!="")
											column.setAttribute("primaryKey",field.getPrimaryKey());
										if(field.getAutoIncrement()!="")
											column.setAttribute("autoIncrement",field.getAutoIncrement());
										if(key.getType(converter)!="")
											column.setAttribute("type",key.getType(converter));
										column.setAttribute("name",key.getSQLName());
										main.appendChild(column);
									}	
								}else
								{
									if (primaryKeys.size() > 1)
									{
										Iterator keys = primaryKeys.iterator();
										while(keys.hasNext())
										{
											GenericWrapperField key = (GenericWrapperField)keys.next();
											Element column = doc.createElement("column");
											if(field.getRequired()!="")
												column.setAttribute("required",field.getRequired());
											if(key.getAdjustedSize()!="")
												column.setAttribute("size",key.getAdjustedSize());
											if(field.getPrimaryKey()!="")
												column.setAttribute("primaryKey",field.getPrimaryKey());
											if(field.getAutoIncrement()!="")
												column.setAttribute("autoIncrement",field.getAutoIncrement());
											if(key.getType(converter)!="")
												column.setAttribute("type",key.getType(converter));
											column.setAttribute("name",field.getXMLSqlNameValue() + key.getSQLName());
											main.appendChild(column);
										}
									}else
									{
										Iterator keys = primaryKeys.iterator();
										while(keys.hasNext())
										{
											GenericWrapperField key = (GenericWrapperField)keys.next();
											Element column = doc.createElement("column");
											if(field.getRequired()!="")
												column.setAttribute("required",field.getRequired());
											if(key.getAdjustedSize()!="")
												column.setAttribute("size",key.getAdjustedSize());
											if(field.getPrimaryKey()!="")
												column.setAttribute("primaryKey",field.getPrimaryKey());
											if(field.getAutoIncrement()!="")
												column.setAttribute("autoIncrement",field.getAutoIncrement());
											if(key.getType(converter)!="")
												column.setAttribute("type",key.getType(converter));
											column.setAttribute("name",field.getXMLSqlNameValue());
											main.appendChild(column);
										}
									}
								}
							}
						}else
						{
							if(field.getType(converter)!="")
							{
								Element column = doc.createElement("column");
								if(field.getRequired()!="")
									column.setAttribute("required",field.getRequired());
								if(field.getAdjustedSize()!="")
									column.setAttribute("size",field.getAdjustedSize());
								if(field.getPrimaryKey()!="")
									column.setAttribute("primaryKey",field.getPrimaryKey());
								if(field.getAutoIncrement()!="")
									column.setAttribute("autoIncrement",field.getAutoIncrement());
							
								column.setAttribute("type",field.getType(converter));
								column.setAttribute("name",field.getSQLName());
								main.appendChild(column);
							}
						}
					}
				
					Iterator unique = element.getUniqueFields().iterator();
					while (unique.hasNext())
					{
						GenericWrapperField field = (GenericWrapperField)unique.next();
						Node node = doc.createElement("unique");
						Element uniqueCol = doc.createElement("unique-column");
						uniqueCol.setAttribute("name",field.getSQLName());
						node.appendChild(uniqueCol);
						main.appendChild(node);
					}
				
					Hashtable uniqueHash = element.getUniqueCompositeFields();
					if (uniqueHash.size() > 0)
					{
					    Enumeration uHashEnum = uniqueHash.keys();
					    while(uHashEnum.hasMoreElements())
					    {
					        String s = (String)uHashEnum.nextElement();
					        
					        ArrayList uniqueCs = (ArrayList)uniqueHash.get(s);
							Iterator uniqueComposite = uniqueCs.iterator();
							Node node = doc.createElement("unique");
							while (uniqueComposite.hasNext())
							{
								GenericWrapperField field = (GenericWrapperField)uniqueComposite.next();
								Element uniqueCol = doc.createElement("unique-column");
								uniqueCol.setAttribute("name",field.getSQLName());
								node.appendChild(uniqueCol);
							}
							main.appendChild(node);
					    }
					}
				
					Iterator refs = element.getReferenceFieldsWAddIns().iterator();
					while (refs.hasNext())
					{
						try {
							GenericWrapperField ref = (GenericWrapperField)refs.next();
							XFTReferenceI xftRef = ref.getXFTReference();
							if (! xftRef.isManyToMany())
							{
								if (! ref.isMultiple())
								{
									XFTSuperiorReference oneRef = (XFTSuperiorReference)xftRef;
									Iterator keyRelations = oneRef.getKeyRelations().iterator();
									while (keyRelations.hasNext())
									{
										XFTRelationSpecification spec = (XFTRelationSpecification)keyRelations.next();
										Element node = doc.createElement("foreign-key");
										Element subCol = doc.createElement("reference");
										node.setAttribute("foreignTable",spec.getForeignTable());
										node.setAttribute("onDelete",ref.getOnDelete());
										subCol.setAttribute("local",spec.getLocalCol());
										subCol.setAttribute("foreign",spec.getForeignCol());
										node.appendChild(subCol);
										main.appendChild(node);
									}
								}
							}

						} catch (DOMException e1) {
							e1.printStackTrace();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
							
					Iterator relations = element.getRelationFields().iterator();
					while (relations.hasNext())
					{
						GenericWrapperField field = (GenericWrapperField)relations.next();
									
						Element node = doc.createElement("foreign-key");
						Element subCol = doc.createElement("reference");
					
						node.setAttribute("foreignTable",field.getForeignKeyTable());
						node.setAttribute("onDelete",field.getOnDelete());
						subCol.setAttribute("local",field.getSQLName());
						subCol.setAttribute("foreign",field.getForeignCol());
						node.appendChild(subCol);
						main.appendChild(node);
					}
				
					if (element.getSchema().getDbType().equalsIgnoreCase("postgresql"))
					{
						if (element.getIdMethod().equalsIgnoreCase("native")||element.getIdMethod().equalsIgnoreCase(""))
						{
							ArrayList al = element.getAllPrimaryKeys();
							if (al.size() == 1)
							{
								Iterator id = al.iterator();
								while(id.hasNext())
								{
									GenericWrapperField key = (GenericWrapperField)id.next();
									if (key.getType(converter).equalsIgnoreCase("INTEGER"))
									{
										Element node = doc.createElement("id-method-parameter");
										node.setAttribute("value",(element.getFormattedName()+ "_" + key.getSQLName() + "_seq").toLowerCase());
										node.setAttribute("name","seqName");
										main.appendChild(node);
									}
								}
							}
						}
					}
				
					root.appendChild(main);
				}
			}
			
			Iterator mappingTables = XFTReferenceManager.GetInstance().getUniqueMappings().iterator();
			while (mappingTables.hasNext())
			{
				XFTManyToManyReference map = (XFTManyToManyReference)mappingTables.next();
				Element main = doc.createElement("table");
				main.setAttribute("idMethod","native");
				main.setAttribute("name",map.getMappingTable());

				Iterator iter = map.getMappingColumns().iterator();
				while (iter.hasNext())
				{
					XFTMappingColumn mapCol = (XFTMappingColumn)iter.next();
					GenericWrapperField fKey = mapCol.getForeignKey();
					if(fKey.getType(converter)!="")
					{
						Element column = doc.createElement("column");
						if(fKey.getAdjustedSize()!="")
							column.setAttribute("size",fKey.getAdjustedSize());
	
						column.setAttribute("type",fKey.getType(converter));
						column.setAttribute("name",mapCol.getLocalSqlName());
						main.appendChild(column);
					}
				}
				
				//ADD PK
				Element column = doc.createElement("column");
				column.setAttribute("primaryKey","true");
				column.setAttribute("autoIncrement","true");
			
				column.setAttribute("type",converter.convert(prefix + ":integer"));
				column.setAttribute("name",map.getMappingTable() + "_id");
				main.appendChild(column);

				Iterator relations = map.getMappingColumns().iterator();
				while (relations.hasNext())
				{
					ArrayList field = (ArrayList)relations.next();
				
					Element node = doc.createElement("foreign-key");
					Element subCol = doc.createElement("reference");

					node.setAttribute("foreignTable",(String)field.get(2));
					node.setAttribute("onDelete","cascade");
					subCol.setAttribute("local",(String)field.get(0));
					subCol.setAttribute("foreign",(String)field.get(3));
					node.appendChild(subCol);
					main.appendChild(node);
				}

				if (schema.getDbType().equalsIgnoreCase("postgresql"))
				{
					Element node = doc.createElement("id-method-parameter");
					node.setAttribute("value",(map.getMappingTable()+ "_" + map.getMappingTable() + "_id_seq").toLowerCase());
					node.setAttribute("name","seqName");
					main.appendChild(node);
				}

				root.appendChild(main);
			}
			
			try {
				XMLUtils.DOMToFile(doc,location);
				System.out.println("File Created: " + location);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (DOMException e) {
			logger.error("",e);
		} catch (org.nrg.xft.exception.XFTInitException e) {
			logger.error("",e);
		} catch (ElementNotFoundException e) {
			logger.error("",e);
		}
	}
	
	public static void main(String args[]) {
		if (args.length == 2){
			try {
				XFT.init(new File(args[0]).toURI(), true);
				TorqueSchemaGenerator.generateDoc(args[1]);
			} catch (ElementNotFoundException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else
		{
			System.out.println("Arguments: <Output File location>");
			return;
		}
	}
}

