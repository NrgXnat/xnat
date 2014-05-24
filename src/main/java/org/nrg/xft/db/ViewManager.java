//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 28, 2005
 *
 */
package org.nrg.xft.db;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTool;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTManyToManyReference;
import org.nrg.xft.references.XFTMappingColumn;
import org.nrg.xft.references.XFTReferenceI;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.search.QueryOrganizer;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public class ViewManager {
	static org.apache.log4j.Logger logger = Logger.getLogger(ViewManager.class);
	public final static Hashtable FIELD_MAPS = new Hashtable();
	public final static Hashtable<String,ArrayList<String>> FIELD_NAMES = new Hashtable<String,ArrayList<String>>();
	public static final String ACTIVE = "active";
	public static final String ALL = "all";
	public static final String LOCKED = "locked";
	public static final String OBSOLETE = "obsolete"; 
	public static final String QUARANTINE = "quarantine";
	public static final String DELETED = "deleted";
	public final static String DEFAULT_LEVEL = ALL;
	public final static String ACCESSIBLE=ViewManager.ACTIVE+","+ ViewManager.LOCKED+","+ ViewManager.QUARANTINE;
	public final static boolean DEFAULT_MULTI = true;
	
	public static boolean PRE_LOAD_HISTORY = false;
	/**
	 * Object [4] : 0-tableName,1-field_name,2-header,3-tableAlias
	 * @param e
	 * @param header
	 * @param level
	 * @param allowMultiples
	 * @return
	 */
	private static ArrayList GetSubFields(GenericWrapperElement e, String level, boolean allowMultiples, String tableAlias, ArrayList hierarchy,String xmlPath,boolean isRoot) throws XFTInitException,ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		
		ArrayList localHierarchy = (ArrayList) hierarchy.clone();
		localHierarchy.add(e.getFullXMLName());
		
		al.addAll(GetDirectFields(e,tableAlias,xmlPath,isRoot,allowMultiples));
	
		if (!e.getAddin().equalsIgnoreCase("meta"))
		{
			Iterator refs = e.getReferenceFieldsWXMLDisplay(true, true).iterator();
			while (refs.hasNext()) {
				GenericWrapperField field = (GenericWrapperField) refs.next();
				if (field.isReference()
					&& ((!field.isMultiple())|| (field.isMultiple() && allowMultiples))) {
					try {
						GenericWrapperElement ref =((GenericWrapperElement) field.getReferenceElement());
						if ((e.getAddin().equalsIgnoreCase("")) || (!ref.getAddin().equalsIgnoreCase("")))
						{
							if (!localHierarchy.contains(ref.getFullXMLName()))
							{
								if (field.getXMLDisplay().equalsIgnoreCase("always") || isRoot)
								{
									Iterator refFields = null;
									if (tableAlias != null && !tableAlias.equalsIgnoreCase(""))
									{
									    if(e.getExtensionFieldName().equals(field.getName()))
									        refFields = (ViewManager.GetSubFields(ref,level,allowMultiples,tableAlias,localHierarchy,xmlPath + field.getXMLPathString(""),true)).iterator();
									    else{
									        refFields = (ViewManager.GetSubFields(ref,level,allowMultiples,tableAlias,localHierarchy,xmlPath + field.getXMLPathString(""),true)).iterator();
									    }
									}else{
									    if(e.getExtensionFieldName().equals(field.getName()))
											refFields = (ViewManager.GetSubFields(ref,level,allowMultiples,field.getSQLName() + "_" + ref.getSQLName(),localHierarchy,xmlPath + field.getXMLPathString(""),true)).iterator();
									    else{
											refFields = (ViewManager.GetSubFields(ref,level,allowMultiples,field.getSQLName() + "_" + ref.getSQLName(),localHierarchy,xmlPath + field.getXMLPathString(""),true)).iterator();
									    }	
									}
									
									while (refFields.hasNext())
									{
										String[] refField = (String[])refFields.next();
										if (refField[2] != null && ! refField[2].equalsIgnoreCase(""))
										{
											refField[2] = StringUtils.MinCharsAbbr(field.getSQLName())+ "_" + refField[2];
										}else{
											refField[2] = StringUtils.MinCharsAbbr(field.getSQLName());
										}
										al.add(refField);
									}
								}else {
									Iterator refFields = ViewManager.GetDirectFields(ref,tableAlias,xmlPath +field.getXMLPathString(""),false,allowMultiples).iterator();
									while (refFields.hasNext())
									{
										String[] refField = (String[])refFields.next();
										refField[2] = "";
										al.add(refField);
									}
								}
							}else{
								
							}
						}
					} catch (Exception e1) {
						logger.error(
							"ELEMENT:'" + e.getFullXMLName() + "'",
							e1);
					}
				}
			}
		}
		
		
		if (PRE_LOAD_HISTORY)
		{
			if (e.getAddin().equalsIgnoreCase(""))
			{
				if (level.equalsIgnoreCase(ViewManager.ALL))
				{
					GenericWrapperElement foreign = GenericWrapperElement.GetElement(e.getFullXMLName() + "_history");
					Iterator refFields = ViewManager.GetDirectFields(foreign,foreign.getSQLName(),xmlPath + XFT.PATH_SEPERATOR+"history",false,allowMultiples).iterator();
					while (refFields.hasNext())
					{
						String[] refField = (String[])refFields.next();
						if (refField[2] != null && ! refField[2].equalsIgnoreCase(""))
						{
							refField[2] = refField[2] + "_history";
						}else{
							refField[2] = "history";
						}
						refField[3] = tableAlias;
						
						al.add(refField);
					}

				}
			}
		}
		
		al.trimToSize();
		return al;
	}
	
	/**
	 * Object [4] : 0-tableName,1-field_name,2-header,3-tableAlias
	 * @param e
	 * @param header
	 * @param level
	 * @param allowMultiples
	 * @return
	 */
	private static ArrayList GetDirectFields(GenericWrapperElement e, String tableAlias,String xmlPath,boolean isRoot,boolean allowMultiples) throws XFTInitException,ElementNotFoundException
	{
		ArrayList minimizedFieldsForMetaData = new ArrayList();
		if (e.getAddin().equalsIgnoreCase("meta") && !isRoot)
		{
			minimizedFieldsForMetaData.add("status");
			minimizedFieldsForMetaData.add("meta_data_id");
			minimizedFieldsForMetaData.add("shareable");
			minimizedFieldsForMetaData.add("modified");
//			if (! allowMultiples)
//			{
				minimizedFieldsForMetaData.add("insert_date");
				minimizedFieldsForMetaData.add("activation_user_xdat_user_id");
				minimizedFieldsForMetaData.add("activation_date");
				minimizedFieldsForMetaData.add("insert_user_xdat_user_id");
//			}
		}
		
		ArrayList al = new ArrayList();
		Iterator iter = e.getAllFieldNames().iterator();
		while (iter.hasNext()) {
			Object[] field = (Object[]) iter.next();
			if (((String) field[2]).equalsIgnoreCase("false")) {
				
				GenericWrapperField f = (GenericWrapperField)field[3];
				if (f.isReference())
				{
					Iterator refs = f.getLocalRefNames().iterator();
					while (refs.hasNext()) {
						ArrayList ref = (ArrayList) refs.next();
						String array[] = new String[6];
						array[0]= e.getSQLName();
						array[1]= (String) ref.get(0);
						array[2]= StringUtils.RegCharsAbbr(e.getSQLName());
						if (tableAlias !=null && !tableAlias.equalsIgnoreCase(""))
						{
							array[3]= tableAlias;
						}else{
							array[3]= e.getSQLName();
						}
						array[5] = xmlPath + XFT.PATH_SEPERATOR + (String) ref.get(0);
						
						if (e.getAddin().equalsIgnoreCase("meta") && !isRoot)
						{
							if (minimizedFieldsForMetaData.contains(array[1]))
							{
								al.add(array);
							}
						}else{
							al.add(array);
						}
					}
				}else{
					String temp[] = new String[6];
					temp[0]= e.getSQLName();
					temp[1]= (String)field[0];
					temp[2]= StringUtils.RegCharsAbbr(e.getSQLName());
					if (tableAlias !=null && !tableAlias.equalsIgnoreCase(""))
					{
						temp[3]= tableAlias;
					}else{
						temp[3]= e.getSQLName();
					}
					temp[5]= xmlPath + f.getXMLPathString("");
					
					if (e.getAddin().equalsIgnoreCase("meta") && !isRoot)
					{
						if (minimizedFieldsForMetaData.contains(temp[1]))
						{
							al.add(temp);
						}
					}else{
						al.add(temp);
					}
				}
			}
		}
		return al;
	}
	
	private static String GetJoins(GenericWrapperElement e, String level, boolean allowMultiples,ArrayList hierarchy,boolean isRoot) throws XFTInitException,ElementNotFoundException
	{
		StringBuffer sb = new StringBuffer();
		sb.append(" FROM ").append(e.getSQLName());

		ArrayList localHierarchy = (ArrayList) hierarchy.clone();
		localHierarchy.add(e.getFullXMLName());
		
		Iterator iter =	e.getReferenceFieldsWXMLDisplay(true, true).iterator();
		while (iter.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) iter.next();
			if (field.isReference()) {
				if (!field.isMultiple()) 
				{
					if (isRoot || field.getXMLDisplay().equalsIgnoreCase("always"))
					{
						ArrayList refs = field.getLocalRefNames();
						Iterator refIter = refs.iterator();
						GenericWrapperElement foreign =	(GenericWrapperElement) ((ArrayList) refs.get(0)).get(2);
						
						if ((e.getAddin().equalsIgnoreCase("")) || (!foreign.getAddin().equalsIgnoreCase("")))
						{
							if (!localHierarchy.contains(foreign.getFullXMLName()))
							{
							    	boolean isExtension = e.getExtensionFieldName().equals(field.getName());
							    	if (isExtension && isRoot)
							    	{
							    	    isExtension = true;
							    	}else{
							    	    isExtension = false;
							    	}
									int counter = 0;
									while (refIter.hasNext()) {
										ArrayList ref = (ArrayList) refIter.next();
										GenericWrapperField foreignKey =(GenericWrapperField) ref.get(1);

										String temp = ViewManager.GetViewColumnName(foreign,foreignKey.getXMLPathString(foreign.getFullXMLName()),isExtension);
										
										if (counter++ == 0) {
											sb.append(" \nLEFT JOIN ").append(ViewManager.GetViewName(foreign,level,allowMultiples,isExtension));
											sb.append(" ").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(e.getSQLName()).append(".").append((String) ref.get(0));
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(".").append(temp);
										} else {
											sb.append(" AND ").append(e.getSQLName()).append(".").append((String) ref.get(0));
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(".").append(temp);
										}
									}
							}
						}
					}else if(! field.getXMLDisplay().equalsIgnoreCase("root")){
						ArrayList refs = field.getLocalRefNames();
						Iterator refIter = refs.iterator();
						GenericWrapperElement foreign =	(GenericWrapperElement) ((ArrayList) refs.get(0)).get(2);
						
						if ((e.getAddin().equalsIgnoreCase("")) || (!foreign.getAddin().equalsIgnoreCase("")))
						{
							if (!localHierarchy.contains(foreign.getFullXMLName()))
							{
								int counter = 0;
								while (refIter.hasNext()) {
									ArrayList ref = (ArrayList) refIter.next();
									GenericWrapperField foreignKey =(GenericWrapperField) ref.get(1);
						
									if (counter++ == 0) {
										sb.append(" \nLEFT JOIN ").append(foreign.getSQLName());
										sb.append(" ").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()));
										sb.append(" ON ").append(e.getSQLName()).append(".").append((String) ref.get(0));
										sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(".").append(foreignKey.getSQLName());
									} else {
										sb.append(" AND ").append(e.getSQLName()).append(".").append((String) ref.get(0));
										sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(".").append(foreignKey.getSQLName());
									}
								}
							}
						}
					}
				} else if (allowMultiples) {
					GenericWrapperElement foreign =	(GenericWrapperElement) field.getReferenceElement();
					if ((e.getAddin().equalsIgnoreCase("")) || (!foreign.getAddin().equalsIgnoreCase("")))
					{
						if (!localHierarchy.contains(foreign.getFullXMLName()))
						{
							if (isRoot || field.getXMLDisplay().equalsIgnoreCase("always"))
							{
								XFTReferenceI xftRef = field.getXFTReference();
								if (xftRef.isManyToMany()) {
									XFTManyToManyReference many = (XFTManyToManyReference) xftRef;
									int counter = 0;
									Iterator localMaps =many.getMappingColumnsForElement(e).iterator();
									while (localMaps.hasNext()) {
										XFTMappingColumn map =(XFTMappingColumn) localMaps.next();
										if (counter++ == 0) {
											sb.append(" \nLEFT JOIN ").append(many.getMappingTable()
													+ " "+ StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(e.getSQLName()).append(".").append(map.getForeignKey().getSQLName());
											sb
												.append("=")
												.append(StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
										} else {
											sb.append(" AND ").append(e.getSQLName()).append(
												".").append(
												map.getForeignKey().getSQLName());
											sb.append("=")
												.append(StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
										}
									}
		
									counter = 0;
									Iterator foreignMaps =
										many
											.getMappingColumnsForElement(
												foreign)
											.iterator();
									while (foreignMaps.hasNext()) {
										XFTMappingColumn map =
											(XFTMappingColumn) foreignMaps.next();
										String temp = ViewManager.GetViewColumnName(foreign,map.getForeignKey().getXMLPathString(foreign.getFullXMLName()));
											
										if (counter++ == 0) {
											sb.append(" \nLEFT JOIN ").append(ViewManager.GetViewName(foreign,level,allowMultiples,false));
											sb.append(" ").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()))
												.append(" ON ")
												.append(StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append(temp);
										} else {
											sb
												.append(" AND ")
												.append(StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append(temp);
										}
									}
								} else {
									ArrayList refs = field.getLocalRefNames();
									Iterator refIter = refs.iterator();
		
									int counter = 0;
									while (refIter.hasNext()) {
										ArrayList ref = (ArrayList) refIter.next();
										GenericWrapperField foreignKey =
											(GenericWrapperField) ref.get(1);
										String temp = ViewManager.GetViewColumnName(foreign,foreign.getFullXMLName() + XFT.PATH_SEPERATOR + (String)ref.get(0));
										
										if (counter++ == 0) {
											sb.append(" \nLEFT JOIN ").append(ViewManager.GetViewName(foreign,level,allowMultiples,false));
											sb.append(" ").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(
												e.getSQLName()).append(
												".").append(
												foreignKey.getSQLName());
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append(temp);
										} else {
											sb.append(" AND ").append(
												e.getSQLName()).append(
												".").append(
												foreignKey.getSQLName());
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append(temp);
										}
									}
								}
							
							}else if(! field.getXMLDisplay().equalsIgnoreCase("root")){
								XFTReferenceI xftRef = field.getXFTReference();
								if (xftRef.isManyToMany()) {
									XFTManyToManyReference many = (XFTManyToManyReference) xftRef;
									int counter = 0;
									Iterator localMaps =many.getMappingColumnsForElement(e).iterator();
									while (localMaps.hasNext()) {
										XFTMappingColumn map =(XFTMappingColumn) localMaps.next();
										if (counter++ == 0) {
											sb.append(" \nLEFT JOIN ").append(many.getMappingTable()
													+ " "+ StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(e.getSQLName()).append(".").append(map.getForeignKey().getSQLName());
											sb
												.append("=")
												.append(StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
										} else {
											sb.append(" AND ").append(e.getSQLName()).append(
												".").append(
												map.getForeignKey().getSQLName());
											sb.append("=")
												.append(StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
										}
									}
		
									counter = 0;
									Iterator foreignMaps =
										many
											.getMappingColumnsForElement(
												foreign)
											.iterator();
									while (foreignMaps.hasNext()) {
										XFTMappingColumn map =
											(XFTMappingColumn) foreignMaps.next();
										
										if (counter++ == 0) {
											sb.append(" \nLEFT JOIN ").append(foreign.getSQLName());
											sb.append(" ").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()))
												.append(" ON ")
												.append(StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append(map.getForeignKey().getSQLName());
										} else {
											sb
												.append(" AND ")
												.append(StringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append(map.getForeignKey().getSQLName());
										}
									}
								} else {
									ArrayList refs = field.getLocalRefNames();
									Iterator refIter = refs.iterator();
		
									int counter = 0;
									while (refIter.hasNext()) {
										ArrayList ref = (ArrayList) refIter.next();
										GenericWrapperField foreignKey =
											(GenericWrapperField) ref.get(1);
										
										if (counter++ == 0) {
											sb.append(" \nLEFT JOIN ").append(foreign.getSQLName());
											sb.append(" ").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(
												e.getSQLName()).append(
												".").append(
												foreignKey.getSQLName());
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append((String) ref.get(0));
										} else {
											sb.append(" AND ").append(
												e.getSQLName()).append(
												".").append(
												foreignKey.getSQLName());
											sb.append("=").append(StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append((String) ref.get(0));
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Object [4] : 0-tableName,1-field_name,2-header,3-tableAlias
	 * @param e
	 * @param level
	 * @param allowMultiples
	 * @return
	 */
	public static ArrayList GetFields(GenericWrapperElement e, String level, boolean allowMultiples,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
	    ArrayList hierarchy = new ArrayList();
	    //hierarchy.add(e.getFullXMLName());
		String xmlPath = e.getFullXMLName();
		ArrayList fieldsArray = new ArrayList();
		fieldsArray.addAll(ViewManager.GetDirectFields(e,"",xmlPath,isRoot,allowMultiples));
		Iterator refs = e.getReferenceFieldsWXMLDisplay(true, true).iterator();
		while (refs.hasNext()) {
			GenericWrapperField field = (GenericWrapperField) refs.next();
			if (field.isReference()
				&& ((!field.isMultiple())|| (field.isMultiple() && allowMultiples))) {
				try {
					Iterator refFields = null;
					GenericWrapperElement ref =((GenericWrapperElement) field.getReferenceElement());
					if ((e.getAddin().equalsIgnoreCase("")) || (!ref.getAddin().equalsIgnoreCase("")))
					{
						if (!ref.getSQLName().equalsIgnoreCase(e.getSQLName()))
						{
							if (isRoot || field.getXMLDisplay().equalsIgnoreCase("always"))
							{
								if (! e.getAddin().equalsIgnoreCase(""))
								{
								    if(e.getExtensionFieldName().equals(field.getName()))
										refFields = (ViewManager.GetSubFields(ref,ViewManager.QUARANTINE,allowMultiples,StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()),hierarchy,field.getXMLPathString(xmlPath),true)).iterator();
								    else{
										refFields = (ViewManager.GetSubFields(ref,ViewManager.QUARANTINE,allowMultiples,StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()),hierarchy,field.getXMLPathString(xmlPath),true)).iterator();
								    }
								}else{
								    if(e.getExtensionFieldName().equals(field.getName()))
										refFields = (ViewManager.GetSubFields(ref,level,allowMultiples,StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()),hierarchy,field.getXMLPathString(xmlPath),true)).iterator();
								    else{
										refFields = (ViewManager.GetSubFields(ref,level,allowMultiples,StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()),hierarchy,field.getXMLPathString(xmlPath),true)).iterator();
								    }
								}

								ArrayList circularRefs = new ArrayList();
								int counter = 0;
								while (refFields.hasNext())
								{
									String[] refField = (String[])refFields.next();
									String refXMLPath = refField[5];
									
									boolean duplicatedField = false;
									Iterator circularRefsIter = circularRefs.iterator();
									while (circularRefsIter.hasNext())
									{
									    String s = (String)circularRefsIter.next();
									    if (refXMLPath.startsWith(s))
									    {
									        duplicatedField = true;
									        break;
									    }
									}
									
									if (! duplicatedField)
									{
									    if (refField[0].equalsIgnoreCase(e.getSQLName()))
									    {
									        duplicatedField = true;
									        circularRefs.add(refXMLPath.substring(0,refXMLPath.lastIndexOf(XFT.PATH_SEPERATOR)));
									    }
									}
									
									if (! duplicatedField)
									{
										if (refField[2] != null && ! refField[2].equalsIgnoreCase(""))
										{
											refField[1] = refField[2] + "_" + refField[1];
										}
										refField[2]=StringUtils.MinCharsAbbr(field.getSQLName());
										
										refField[1]= refField[0] + counter++;
										fieldsArray.add(refField);
									}else{
									    counter++;
									}
								}
							}else if(! field.getXMLDisplay().equalsIgnoreCase("root")){
								refFields = (ViewManager.GetDirectFields(ref,StringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()),field.getXMLPathString(xmlPath),false,allowMultiples)).iterator();
										
								while (refFields.hasNext())
								{
									String[] refField = (String[])refFields.next();
									refField[2] = "";
									fieldsArray.add(refField);
								}

								int counter = 0;
								while (refFields.hasNext())
								{
									String[] refField = (String[])refFields.next();
									
									refField[2]=StringUtils.MinCharsAbbr(field.getSQLName());

									refField[1]= refField[0] + counter++;
									fieldsArray.add(refField);
								}
							}
						}
					}
					
				} catch (Exception e1) {
					logger.error(
						"ELEMENT:'" + e.getFullXMLName() + "'",
						e1);
				}
			}
		}		
		
		if (PRE_LOAD_HISTORY)
		{
			if (e.getAddin().equalsIgnoreCase(""))
			{
				if (level.equalsIgnoreCase(ViewManager.ALL))
				{
					GenericWrapperElement foreign = GenericWrapperElement.GetElement(e.getFullXMLName() + "_history");
					Iterator refFields = ViewManager.GetDirectFields(foreign,foreign.getSQLName(),xmlPath + ".history",false,allowMultiples).iterator();
					while (refFields.hasNext())
					{
						String[] refField = (String[])refFields.next();
						if (refField[2] != null && ! refField[2].equalsIgnoreCase(""))
						{
							refField[2] = refField[2] + "_history";
						}else{
							refField[2] = "history";
						}
						
						fieldsArray.add(refField);
					}

				}
			}
		}
		return fieldsArray;
	}
	
	public static String GetViewSQL(GenericWrapperElement e, String level, boolean allowMultiples,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{

		QueryOrganizer qo = new QueryOrganizer(e,null,level);
	    Iterator iter = ViewManager.GetFieldNames(e,level,allowMultiples,isRoot).iterator();
	    while(iter.hasNext())
	    {
	        String s = (String)iter.next();
	        s = StringUtils.StandardizeXMLPath(s);
	        qo.addField(s);
	    }		
	    
	    String query = "";
        try {
            query = qo.buildQuery();
        } catch (IllegalAccessException e1) {
            logger.error("",e1);
        } catch (Exception e1) {
            logger.error("",e1);
        }
        return query;
	    
//		StringBuffer sb = new StringBuffer(" SELECT DISTINCT ");
//		
//		ArrayList fieldsArray = GetFields(e,level,allowMultiples,isRoot);
//		
//		
//		Iterator fields = fieldsArray.iterator();
//		int counter = 0;
//		StringBuffer temp = new StringBuffer();
//		while (fields.hasNext())
//		{
//			Object[] field = (Object[])fields.next();
//			String table = (String)field[0];
//			String tableAlias = (String)field[3];
//			String fieldName = (String)field[1];
//			String header = (String)field[2];
//
//			if (counter == 0)
//			{
//				sb.append(tableAlias).append(".").append(fieldName).append(" AS ").append(table + counter);
//			}else{
//				sb.append(",\n").append(tableAlias).append(".").append(fieldName).append(" AS ").append(table + counter);
//			}
//			
//			String s= table + counter;
//			temp.append("\n").append(s);
//			int i = 65;
//			if (s.length()<30)
//			{
//				i= 30-s.length();
//			}else{
//				i = 65-s.length();
//			}
//			temp.append(StringUtils.WhiteSpace(i)).append(field[5]);
//			
//			counter++;
//		}
//		
//		sb.append(ViewManager.GetJoins(e,level,allowMultiples,new ArrayList(),isRoot));
//		
//		if (e.getAddin().equalsIgnoreCase(""))
//		{
//			GenericWrapperElement meta = GenericWrapperElement.GetElement(e.getFullXMLName() +"_meta_data");
//			String statusCol = ViewManager.GetViewColumnName(meta,e.getFullXMLName() +"_meta_data.status",false);
//		    String s = StringUtils.SQLMaxCharsAbbr(StringUtils.CleanForSQL(e.getType().getLocalType()) + "_info_" + meta.getSQLName());
//			if (level.equalsIgnoreCase(ViewManager.ACTIVE))
//			{
//				sb.append(" \nWHERE ").append(s);
//				sb.append(".").append(statusCol).append("='");
//				sb.append(ViewManager.ACTIVE).append("'");
//			}else if (level.equalsIgnoreCase(ViewManager.QUARANTINE))
//			{
//				sb.append(" \nWHERE ").append(s).append(".").append(statusCol).append("='");
//				sb.append(ViewManager.ACTIVE).append("' OR ").append(s);
//				sb.append(".").append(statusCol).append("='").append(ViewManager.QUARANTINE).append("'");
//			}else
//			{
//				if (PRE_LOAD_HISTORY)
//				{
//					GenericWrapperElement foreign = GenericWrapperElement.GetElement(e.getFullXMLName() + "_history");
//					int joinCounter = 0;
//					Iterator keys = e.getAllPrimaryKeys().iterator();
//					while (keys.hasNext())
//					{
//						GenericWrapperField field = (GenericWrapperField)keys.next();
//						if (joinCounter++ == 0) {
//							sb.append(" \nLEFT JOIN ").append(foreign.getSQLName());
//							sb.append(" ON ").append(e.getSQLName()).append(".").append(field.getSQLName());
//							sb.append("=").append(foreign.getSQLName()).append(".");
//							sb.append("new_row_" + field.getSQLName());
//						} else {
//							sb.append(" AND ").append(e.getSQLName()).append(".").append(field.getSQLName());
//							sb.append("=").append(foreign.getSQLName()).append(".");
//							sb.append("new_row_" + field.getSQLName());
//						}
//					}
//				}
//			}
//		}
//		
//		//FileUtils.OutputToSubFolder("views",e.getSQLName() + "_"+ allowMultiples + "_"+ level + "_"+ isRoot+".txt",temp.toString() +"\n\n"  + ViewManager.GetViewName(e,level,allowMultiples,isRoot) +"\n\n" + sb.toString());
//		
//		return sb.toString();
	}

	public static String GetViewName(GenericWrapperElement e, String level, boolean allowMultiples,boolean isRoot)
	{
		if (level.equalsIgnoreCase(ACTIVE))
		{
			if (allowMultiples)
			{
				if (isRoot)
				{
					return "ac_m_" +e.getSQLName()+"_r";
				}else
					return "ac_m_" +e.getSQLName();
			}else{
				if (isRoot)
				{
					return "ac_s_" +e.getSQLName()+"_r";
				}else
					return "ac_s_" +e.getSQLName();
			}
		}else if (level.equalsIgnoreCase(QUARANTINE))
		{
			if (allowMultiples)
			{
				if (isRoot)
				{
					return "q_m_" +e.getSQLName()+"_r";
				}else
					return "q_m_" +e.getSQLName();
			}else{
				if (isRoot)
				{
					return "q_s_" +e.getSQLName()+"_r";
				}else
					return "q_s_" +e.getSQLName();
			}
		}else
		{
			if (PRE_LOAD_HISTORY)
			{
				if (allowMultiples)
				{
					if (isRoot)
					{
						return "al_m_" +e.getSQLName()+"_r";
					}else
						return "al_m_" +e.getSQLName();
				}else{
					if (isRoot)
					{
						return "al_s_" +e.getSQLName()+"_r";
					}else
						return "al_s_" +e.getSQLName();
				}
			}else
			{
				if (allowMultiples)
				{
					if (isRoot)
					{
						return "q_m_" +e.getSQLName()+"_r";
					}else
						return "q_m_" +e.getSQLName();
				}else{
					if (isRoot)
					{
						return "q_s_" +e.getSQLName()+"_r";
					}else
						return "q_s_" +e.getSQLName();
				}
			}
		}
	}
	
	public static ArrayList GetAllFields(GenericWrapperElement e, boolean allowMultiples,boolean isRoot) throws XFTInitException,ElementNotFoundException
	{
		if (PRE_LOAD_HISTORY)
		{
			return ViewManager.GetFields(e,ALL,allowMultiples,isRoot);
		}else{
			return ViewManager.GetFields(e,QUARANTINE,allowMultiples,isRoot);
		}
	}
	
	public static ArrayList GetQuarantineFields(GenericWrapperElement e, String header, boolean allowMultiples,boolean isRoot) throws XFTInitException,ElementNotFoundException
	{
		return ViewManager.GetFields(e,QUARANTINE,allowMultiples,isRoot);
	}
	
	public static ArrayList GetActiveFields(GenericWrapperElement e, String header, boolean allowMultiples,boolean isRoot) throws XFTInitException,ElementNotFoundException
	{
		return ViewManager.GetFields(e,ACTIVE,allowMultiples,isRoot);
	}
	
	public static String GetAllView(GenericWrapperElement e, boolean allowMultiples,boolean isRoot) throws XFTInitException,ElementNotFoundException
	{
		if (PRE_LOAD_HISTORY)
		{
			return GetViewSQL(e,ALL,allowMultiples,isRoot);
		}else{
			return GetViewSQL(e,ViewManager.QUARANTINE,allowMultiples,isRoot);
		}
	}
	
	public static String GetQuarantineView(GenericWrapperElement e, boolean allowMultiples,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
		return GetViewSQL(e,QUARANTINE,allowMultiples,isRoot);
	}
	
	public static String GetActiveView(GenericWrapperElement e, boolean allowMultiples,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
		return GetViewSQL(e,ACTIVE,allowMultiples,isRoot);
	}
	

	public static Hashtable GetFieldMap(GenericWrapperElement e,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
		return GetFieldMap(e,DEFAULT_LEVEL,DEFAULT_MULTI,isRoot);		
	}
	
	/**
	 * String[] {0=field-sql-name,1=XML dot syntax
	 * @param e
	 * @param isRoot
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public synchronized static Hashtable GetFieldMap(GenericWrapperElement e,String level,boolean allowMultiples,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
		if (FIELD_MAPS.get(e.getSQLName() + isRoot + level + allowMultiples) == null)
		{
			Hashtable hash = new Hashtable();
			ArrayList al = new ArrayList();
			Iterator fields = null;
			if (PRE_LOAD_HISTORY && level.equalsIgnoreCase(ViewManager.ALL))
			{
				fields = GetFields(e,ViewManager.ALL,allowMultiples,isRoot).iterator();
			}else if (level.equalsIgnoreCase(ViewManager.ALL)){
				fields = GetFields(e,ViewManager.QUARANTINE,allowMultiples,isRoot).iterator();
			}else{
				fields = GetFields(e,level,allowMultiples,isRoot).iterator();
			}
				
			int counter = 0;
			while (fields.hasNext())
			{
				String [] field = (String[])fields.next();
				String fieldName = (String)field[1];
				String header = (String)field[2];
				if (header != null && !header.equalsIgnoreCase(""))
				{
					fieldName = header + "_" + fieldName;
				}
				
				String s = field[5];
				s = StringUtils.StandardizeXMLPath(s);
				hash.put(s.toLowerCase(),field[0] + Integer.toString(counter));
				al.add(s);
				counter++;
			}
			
			FIELD_MAPS.put(e.getSQLName() + isRoot + level + allowMultiples,hash);
			FIELD_NAMES.put(e.getSQLName() + isRoot + level + allowMultiples,al);
		}
		return (Hashtable)FIELD_MAPS.get(e.getSQLName() + isRoot + level + allowMultiples);
	}
	

	
	/**
	 * Returns XMLPath names of all child fields
	 * @param e
	 * @param isRoot
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static ArrayList GetFieldNames(GenericWrapperElement e,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
		return GetFieldNames(e,DEFAULT_LEVEL,DEFAULT_MULTI,isRoot);		
	}
	
	/**
	 * Returns XMLPath names of all child fields
	 * @param e
	 * @param isRoot
	 * @param loadHistory
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static ArrayList GetFieldNames(GenericWrapperElement e,boolean isRoot,boolean loadHistory)throws XFTInitException,ElementNotFoundException
	{
		if (loadHistory)
			return GetFieldNames(e,ALL,DEFAULT_MULTI,isRoot);
		else
			return GetFieldNames(e,DEFAULT_LEVEL,DEFAULT_MULTI,isRoot);
	}
	
	/**
	 * Returns XMLPath names of all child fields
	 * @param e
	 * @param level
	 * @param allowMultiples
	 * @param isRoot
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public synchronized static ArrayList<String> GetFieldNames(GenericWrapperElement e,String level,boolean allowMultiples,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
		if (FIELD_NAMES.get(e.getSQLName() + isRoot + level + allowMultiples) == null)
		{
			Hashtable hash = new Hashtable();
			ArrayList<String> al = new ArrayList<String>();
			Iterator fields = null;
			if (PRE_LOAD_HISTORY && level.equalsIgnoreCase(ViewManager.ALL))
			{
				fields = GetFields(e,ViewManager.ALL,allowMultiples,isRoot).iterator();
			}else if (level.equalsIgnoreCase(ViewManager.ALL)){
				fields = GetFields(e,ViewManager.QUARANTINE,allowMultiples,isRoot).iterator();
			}else{
				fields = GetFields(e,level,allowMultiples,isRoot).iterator();
			}
			int counter = 0;
			while (fields.hasNext())
			{
				String [] field = (String[])fields.next();
				String fieldName = (String)field[1];
				String header = (String)field[2];
				if (header != null && !header.equalsIgnoreCase(""))
				{
					fieldName = header + "_" + fieldName;
				}
				
				String s = field[5];
				s = StringUtils.StandardizeXMLPath(s);
				hash.put(s.toLowerCase(),field[0] + Integer.toString(counter));
				al.add(s);
				counter++;
			}
			
			FIELD_MAPS.put(e.getSQLName() + isRoot + level + allowMultiples,hash);
			FIELD_NAMES.put(e.getSQLName() + isRoot + level + allowMultiples,al);
		}
		return (ArrayList)FIELD_NAMES.get(e.getSQLName() + isRoot + level + allowMultiples);
	}
	
	public static String GetViewColumnName(GenericWrapperElement e, String xmlPath,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
		return GetViewColumnName(e,xmlPath,DEFAULT_LEVEL,DEFAULT_MULTI,isRoot);		
	}
	
	public static String GetViewColumnName(GenericWrapperElement e, String xmlPath)throws XFTInitException,ElementNotFoundException
	{
		return GetViewColumnName(e,xmlPath,DEFAULT_LEVEL,DEFAULT_MULTI,true);		
	}
	
	public static String GetViewColumnName(GenericWrapperElement e, String xmlPath,String level,boolean allowMultiples,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
		Hashtable hash = GetFieldMap(e,level,allowMultiples,isRoot);
		String s = (String)hash.get(xmlPath.toLowerCase());
		if (s == null)
		{
		    try {
                
	            String abbrxmlPath = GenericWrapperElement.GetVerifiedXMLPath(xmlPath);
	            s = (String)hash.get(abbrxmlPath.toLowerCase());
                if (s!=null)
                {
                    hash.put(xmlPath.toLowerCase(), s);
                }
	        } catch (Exception e1) {
	            //logger.error("",e1);
	        }
		}
		return s;		
	}
	
	public static void OutputFieldNames()
	{
		try {
			String local = XFTTool.GetSettingsLocation() + "fields" + File.separator;
			File f = new File(local);
			if (! f.exists())
			{
				f.mkdir();
			}
			
			StringBuffer hierarchy = new StringBuffer("Possible Parent Data-Types:");
			
			Iterator al = XFTMetaManager.GetElementNames().iterator();
			while (al.hasNext())
			{
				String s = (String)al.next();
				GenericWrapperElement e = GenericWrapperElement.GetElement(s);
				if (e.getAddin().equalsIgnoreCase(""))
				{
				    //create hierarchy
				    hierarchy.append("\n\n").append(e.getFullXMLName());
				    hierarchy.append("\n\t");
				    
				    ArrayList possibleParents = e.getPossibleParents(true);
				    if (possibleParents.size()>0)
				    {
					    Iterator pps = possibleParents.iterator();
					    while (pps.hasNext())
					    {
					        Object [] pp = (Object [])pps.next();
					        GenericWrapperElement pElement = (GenericWrapperElement)pp[0];
					        String xmlPath = (String)pp[1];
					        
					        hierarchy.append(xmlPath).append("  ");
					    }   
				    }else{
				        hierarchy.append("NONE");
				    }
				    
				    //create field mappings
					StringBuffer sb = new StringBuffer();
					
					//ACTIVE - MULTIPLE
					sb.append("\n\n*********************************************\n\n");
					sb.append("ac_s_").append(e.getSQLName()).append("_r");
					sb.append("\n(ACTIVE - MULTIPLE - ROOT)\n");

					Iterator iter = ViewManager.GetFieldNames(e,ViewManager.ACTIVE,true,true).iterator();
					Hashtable hash=ViewManager.GetFieldMap(e,ViewManager.ACTIVE,true,true);
					while (iter.hasNext())
					{
						String name = (String)iter.next();
						try {
                            GenericWrapperField temp = (GenericWrapperField)GenericWrapperElement.GetFieldForXMLPath(name);
                            String id = (String)hash.get(name.toLowerCase());
                            try {
                                id += "\t\t" +temp.getParentElement().getSQLName() + "."+temp.getSQLName();
                            } catch (RuntimeException e1) {
                                id += "\t\t" +temp.getSQLName();
                            }
                            //
                            if (name.length() < 100)
                            {
                            	int i = 100-name.length();
                            	sb.append("\n").append(name).append(StringUtils.WhiteSpace(i)).append(id);
                            }else{
                            	sb.append("\n").append(name).append("\t").append(id);
                            }
                        } catch (FieldNotFoundException e1) {
                        }
                        
					}
					
					
					//OUTPUT TO FILE
					File temp = new File(local + e.getSQLName() + ".txt");
					if (temp.exists())
					{
						temp.delete();
					}
					FileUtils.OutputToFile(sb.toString(),local + e.getSQLName() + ".txt");
				}
			}
			
			File temp = new File(local + "hierarchy.txt");
			if (temp.exists())
			{
				temp.delete();
			}
			FileUtils.OutputToFile(hierarchy.toString(),local + "hierarchy.txt");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}

