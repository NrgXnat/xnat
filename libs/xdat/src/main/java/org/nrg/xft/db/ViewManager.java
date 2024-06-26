/*
 * core: org.nrg.xft.db.ViewManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.db;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.nrg.xft.utils.XftStringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tim
 *
 */
@Slf4j
public class ViewManager {
	public final static Map<String, Map<String, String>> FIELD_MAPS  = new ConcurrentHashMap<>();
	public final static Map<String, List<String>>        FIELD_NAMES = new ConcurrentHashMap<>();
	public static final String                           ACTIVE      = "active";
	public static final String                           ALL         = "all";
	public static final String                           LOCKED      = "locked";
	public static final String                           OBSOLETE    = "obsolete";
	public static final String                           QUARANTINE  = "quarantine";
	public static final String                           DELETED     = "deleted";
	public final static String DEFAULT_LEVEL = ALL;
	public final static String ACCESSIBLE=ViewManager.ACTIVE+","+ ViewManager.LOCKED+","+ ViewManager.QUARANTINE;
	public final static boolean DEFAULT_MULTI = true;
	
	public static boolean PRE_LOAD_HISTORY = false;

	/**
	 * Gets subfields for the element. This returns a list of string arrays, where each array contains:
	 *
	 * <ul>
	 *     <li>Table name</li>
	 *     <li>Field name</li>
	 *     <li>Header</li>
	 *     <li>The table alias</li>
	 * </ul>
	 *
	 * @param element        The element to analyze.
	 * @param level          The level of the current field.
	 * @param allowMultiples Indicates whether multiple fields should be considered.
	 * @param tableAlias     The alias for the element's table.
	 * @param hierarchy      The current hierarchy.
	 * @param xmlPath        The XML path for the element.
	 * @param isRoot         Indicates whether the element is a root element
	 *
	 * @return Returns a list of string arrays indicating all direct fields for the element.
	 *
	 * @throws XFTInitException When an error occurs accessing XFT
	 * @throws ElementNotFoundException When the element or a referenced element can't be found.
	 */
	private static ArrayList GetSubFields(final GenericWrapperElement element, final String level, final boolean allowMultiples, final String tableAlias, final ArrayList hierarchy, final String xmlPath, final boolean isRoot) throws XFTInitException, ElementNotFoundException
	{
		ArrayList al = new ArrayList();
		
		ArrayList localHierarchy = (ArrayList) hierarchy.clone();
		localHierarchy.add(element.getFullXMLName());
		
		al.addAll(GetDirectFields(element,tableAlias,xmlPath,isRoot,allowMultiples));
	
		if (!element.getAddin().equalsIgnoreCase("meta"))
		{
			Iterator refs = element.getReferenceFieldsWXMLDisplay(true, true).iterator();
			while (refs.hasNext()) {
				GenericWrapperField field = (GenericWrapperField) refs.next();
				if (field.isReference()
					&& ((!field.isMultiple())|| (field.isMultiple() && allowMultiples))) {
					try {
						GenericWrapperElement ref =((GenericWrapperElement) field.getReferenceElement());
						if ((element.getAddin().equalsIgnoreCase("")) || (!ref.getAddin().equalsIgnoreCase("")))
						{
							if (!localHierarchy.contains(ref.getFullXMLName()))
							{
								if (field.getXMLDisplay().equalsIgnoreCase("always") || isRoot)
								{
									Iterator refFields = null;
									if (tableAlias != null && !tableAlias.equalsIgnoreCase(""))
									{
									    if(element.getExtensionFieldName().equals(field.getName()))
									        refFields = (ViewManager.GetSubFields(ref,level,allowMultiples,tableAlias,localHierarchy,xmlPath + field.getXMLPathString(""),true)).iterator();
									    else{
									        refFields = (ViewManager.GetSubFields(ref,level,allowMultiples,tableAlias,localHierarchy,xmlPath + field.getXMLPathString(""),true)).iterator();
									    }
									}else{
									    if(element.getExtensionFieldName().equals(field.getName()))
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
											refField[2] = XftStringUtils.MinCharsAbbr(field.getSQLName()) + "_" + refField[2];
										}else{
											refField[2] = XftStringUtils.MinCharsAbbr(field.getSQLName());
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
						log.error(
							"ELEMENT:'" + element.getFullXMLName() + "'",
							e1);
					}
				}
			}
		}
		
		
		if (PRE_LOAD_HISTORY)
		{
			if (element.getAddin().equalsIgnoreCase(""))
			{
				if (level.equalsIgnoreCase(ViewManager.ALL))
				{
					GenericWrapperElement foreign = GenericWrapperElement.GetElement(element.getFullXMLName() + "_history");
					Iterator refFields = ViewManager.GetDirectFields(foreign, foreign.getSQLName(), xmlPath + XFT.PATH_SEPARATOR + "history", false, allowMultiples).iterator();
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
	 * Gets the direct fields for the element. This returns a list of string arrays, where each array contains:
	 *
	 * <ul>
	 *     <li>Table name</li>
	 *     <li>Field name</li>
	 *     <li>Header</li>
	 *     <li>The table alias</li>
	 * </ul>
	 *
	 * @param element        The element to analyze.
	 * @param tableAlias     The alias for the element's table.
	 * @param xmlPath        The XML path for the element.
	 * @param isRoot         Indicates whether the element is a root element
	 * @param allowMultiples Deprecated and ignored.
	 *
	 * @return Returns a list of string arrays indicating all direct fields for the element.
	 *
	 * @throws XFTInitException When an error occurs accessing XFT
	 * @throws ElementNotFoundException When the element or a referenced element can't be found.
	 */
	private static ArrayList GetDirectFields(final GenericWrapperElement element, final String tableAlias, final String xmlPath, final boolean isRoot, final boolean allowMultiples) throws XFTInitException,ElementNotFoundException
	{
		ArrayList minimizedFieldsForMetaData = new ArrayList();
		if (element.getAddin().equalsIgnoreCase("meta") && !isRoot)
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
		Iterator iter = element.getAllFieldNames().iterator();
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
						array[0]= element.getSQLName();
						array[1]= (String) ref.get(0);
						array[2]= XftStringUtils.RegCharsAbbr(element.getSQLName());
						if (tableAlias !=null && !tableAlias.equalsIgnoreCase(""))
						{
							array[3]= tableAlias;
						}else{
							array[3]= element.getSQLName();
						}
						array[5] = xmlPath + XFT.PATH_SEPARATOR + (String) ref.get(0);
						
						if (element.getAddin().equalsIgnoreCase("meta") && !isRoot)
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
					temp[0]= element.getSQLName();
					temp[1]= (String)field[0];
					temp[2]= XftStringUtils.RegCharsAbbr(element.getSQLName());
					if (tableAlias !=null && !tableAlias.equalsIgnoreCase(""))
					{
						temp[3]= tableAlias;
					}else{
						temp[3]= element.getSQLName();
					}
					temp[5]= xmlPath + f.getXMLPathString("");
					
					if (element.getAddin().equalsIgnoreCase("meta") && !isRoot)
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
											sb.append(" ").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(e.getSQLName()).append(".").append((String) ref.get(0));
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(".").append(temp);
										} else {
											sb.append(" AND ").append(e.getSQLName()).append(".").append((String) ref.get(0));
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(".").append(temp);
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
										sb.append(" ").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()));
										sb.append(" ON ").append(e.getSQLName()).append(".").append((String) ref.get(0));
										sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(".").append(foreignKey.getSQLName());
									} else {
										sb.append(" AND ").append(e.getSQLName()).append(".").append((String) ref.get(0));
										sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(".").append(foreignKey.getSQLName());
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
																			  + " " + XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(e.getSQLName()).append(".").append(map.getForeignKey().getSQLName());
											sb
												.append("=")
												.append(XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
										} else {
											sb.append(" AND ").append(e.getSQLName()).append(
												".").append(
												map.getForeignKey().getSQLName());
											sb.append("=")
												.append(XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
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
											sb.append(" ").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()))
												.append(" ON ")
												.append(XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append(temp);
										} else {
											sb
												.append(" AND ")
												.append(XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
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
										String temp = ViewManager.GetViewColumnName(foreign, foreign.getFullXMLName() + XFT.PATH_SEPARATOR + (String)ref.get(0));
										
										if (counter++ == 0) {
											sb.append(" \nLEFT JOIN ").append(ViewManager.GetViewName(foreign,level,allowMultiples,false));
											sb.append(" ").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(
												e.getSQLName()).append(
												".").append(
												foreignKey.getSQLName());
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append(temp);
										} else {
											sb.append(" AND ").append(
												e.getSQLName()).append(
												".").append(
												foreignKey.getSQLName());
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
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
																			  + " " + XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(e.getSQLName()).append(".").append(map.getForeignKey().getSQLName());
											sb
												.append("=")
												.append(XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
										} else {
											sb.append(" AND ").append(e.getSQLName()).append(
												".").append(
												map.getForeignKey().getSQLName());
											sb.append("=")
												.append(XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
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
											sb.append(" ").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()))
												.append(" ON ")
												.append(XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append(map.getForeignKey().getSQLName());
										} else {
											sb
												.append(" AND ")
												.append(XftStringUtils.SQLMaxCharsAbbr("mapping_" + field.getSQLName() + "_" + foreign.getSQLName()))
												.append(".")
												.append(map.getLocalSqlName());
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
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
											sb.append(" ").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName()));
											sb.append(" ON ").append(
												e.getSQLName()).append(
												".").append(
												foreignKey.getSQLName());
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
												".").append((String) ref.get(0));
										} else {
											sb.append(" AND ").append(
												e.getSQLName()).append(
												".").append(
												foreignKey.getSQLName());
											sb.append("=").append(XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + foreign.getSQLName())).append(
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
	 * @return Returns a list of fields
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
										refFields = (ViewManager.GetSubFields(ref, ViewManager.QUARANTINE, allowMultiples, XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()), hierarchy, field.getXMLPathString(xmlPath), true)).iterator();
								    else{
										refFields = (ViewManager.GetSubFields(ref, ViewManager.QUARANTINE, allowMultiples, XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()), hierarchy, field.getXMLPathString(xmlPath), true)).iterator();
								    }
								}else{
								    if(e.getExtensionFieldName().equals(field.getName()))
										refFields = (ViewManager.GetSubFields(ref, level, allowMultiples, XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()), hierarchy, field.getXMLPathString(xmlPath), true)).iterator();
								    else{
										refFields = (ViewManager.GetSubFields(ref, level, allowMultiples, XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()), hierarchy, field.getXMLPathString(xmlPath), true)).iterator();
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
									        circularRefs.add(refXMLPath.substring(0,refXMLPath.lastIndexOf(XFT.PATH_SEPARATOR)));
									    }
									}
									
									if (! duplicatedField)
									{
										if (refField[2] != null && ! refField[2].equalsIgnoreCase(""))
										{
											refField[1] = refField[2] + "_" + refField[1];
										}
										refField[2]= XftStringUtils.MinCharsAbbr(field.getSQLName());
										
										refField[1]= refField[0] + counter++;
										fieldsArray.add(refField);
									}else{
									    counter++;
									}
								}
							}else if(! field.getXMLDisplay().equalsIgnoreCase("root")){
								refFields = (ViewManager.GetDirectFields(ref, XftStringUtils.SQLMaxCharsAbbr(field.getSQLName() + "_" + ref.getSQLName()), field.getXMLPathString(xmlPath), false, allowMultiples)).iterator();
										
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
									
									refField[2]= XftStringUtils.MinCharsAbbr(field.getSQLName());

									refField[1]= refField[0] + counter++;
									fieldsArray.add(refField);
								}
							}
						}
					}
					
				} catch (Exception e1) {
					log.error(
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
	        s = XftStringUtils.StandardizeXMLPath(s);
	        qo.addField(s);
	    }		
	    
	    String query = "";
        try {
            query = qo.buildQuery();
        } catch (IllegalAccessException e1) {
            log.error("", e1);
        } catch (Exception e1) {
            log.error("", e1);
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
//			temp.append(XftStringUtils.WhiteSpace(i)).append(field[5]);
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
//		    String s = XftStringUtils.SQLMaxCharsAbbr(XftStringUtils.CleanForSQL(e.getType().getLocalType()) + "_info_" + meta.getSQLName());
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
	

	public static Map<String, String> GetFieldMap(GenericWrapperElement e,boolean isRoot)throws XFTInitException,ElementNotFoundException
	{
		return GetFieldMap(e,DEFAULT_LEVEL,DEFAULT_MULTI,isRoot);		
	}
	
	/**
	 * String[] {0=field-sql-name,1=XML dot syntax
	 * @param e
	 * @param isRoot
	 * @return Returns a field map hashtable
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public synchronized static Map<String, String> GetFieldMap(final GenericWrapperElement e, final String level, final boolean allowMultiples, final boolean isRoot)throws XFTInitException,ElementNotFoundException {
		return getFieldElements(FIELD_MAPS, e, level, allowMultiples, isRoot);
	}

	private static <T> T getFieldElements(final Map<String, T> elementMap, final GenericWrapperElement element, final String level, final boolean allowMultiples, final boolean isRoot) throws XFTInitException, ElementNotFoundException {
		final String fieldElementKey = element.getSQLName() + isRoot + level + allowMultiples;
		if (!elementMap.containsKey(fieldElementKey)) {
			final String fullXMLName = element.getFullXMLName();
			log.info("No entry found for element key {}: element {}, level {}, multiples {}, isRoot {}", fieldElementKey, fullXMLName, level, allowMultiples, isRoot);
			final Map<String, String> fieldMap   = new HashMap<>();
			final List<String>        fieldNames = new ArrayList<>();

			final List<String[]> fields = new ArrayList<>();
			if (PRE_LOAD_HISTORY && level.equalsIgnoreCase(ViewManager.ALL)) {
				//noinspection unchecked
				fields.addAll(GetFields(element, ViewManager.ALL, allowMultiples, isRoot));
			} else if (level.equalsIgnoreCase(ViewManager.ALL)) {
				//noinspection unchecked
				fields.addAll(GetFields(element, ViewManager.QUARANTINE, allowMultiples, isRoot));
			} else {
				//noinspection unchecked
				fields.addAll(GetFields(element, level, allowMultiples, isRoot));
			}

			log.debug("Starting to process {} fields for element key {}: element {}, level {}, multiples {}, isRoot {}", fields.size(), fullXMLName, PRE_LOAD_HISTORY, level);

			int counter = 0;
			for (final String[] field : fields) {
				final String xmlPath = XftStringUtils.StandardizeXMLPath(field[5]);
				final String value   = field[0] + Integer.toString(counter++);

				log.debug("Setting element {} XML path '{}' to alias '{}'", fullXMLName, xmlPath, value);
				fieldMap.put(xmlPath.toLowerCase(), value);
				fieldNames.add(xmlPath);
			}

			FIELD_MAPS.put(fieldElementKey, fieldMap);
			FIELD_NAMES.put(fieldElementKey, fieldNames);
		}
		return elementMap.get(fieldElementKey);
	}
	
	/**
	 * Returns XMLPath names of all child fields
	 * @param e
	 * @param isRoot
	 * @return Returns XMLPath names of all child fields
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static List<String> GetFieldNames(GenericWrapperElement e,boolean isRoot)throws XFTInitException,ElementNotFoundException {
		return GetFieldNames(e,DEFAULT_LEVEL,DEFAULT_MULTI,isRoot);		
	}
	
	/**
	 * Returns XMLPath names of all child fields
	 * @param e
	 * @param isRoot
	 * @param loadHistory
	 * @return Returns XMLPath names of all child fields
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static List<String> GetFieldNames(GenericWrapperElement e,boolean isRoot,boolean loadHistory)throws XFTInitException,ElementNotFoundException
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
	 * @return Returns XMLPath names of all child fields
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public synchronized static List<String> GetFieldNames(GenericWrapperElement e,String level,boolean allowMultiples,boolean isRoot)throws XFTInitException,ElementNotFoundException {
		return getFieldElements(FIELD_NAMES, e, level, allowMultiples, isRoot);
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
		final Map<String, String> fieldMap = GetFieldMap(e, level, allowMultiples, isRoot);
		final String        viewColumnName    = fieldMap.get(xmlPath.toLowerCase());
		if (StringUtils.isNotBlank(viewColumnName)) {
			return viewColumnName;
		}
		try {
			final String abbrxmlPath = GenericWrapperElement.GetVerifiedXMLPath(xmlPath).toLowerCase();
			if (fieldMap.containsKey(abbrxmlPath)) {
				final String aliased = fieldMap.get(abbrxmlPath);
				fieldMap.put(xmlPath.toLowerCase(), aliased);
				return aliased;
			}
		} catch (Exception e1) {
			//log.error("",e1);
		}
		return null;
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

					final Map<String, String> fieldMap =ViewManager.GetFieldMap(e, ViewManager.ACTIVE, true, true);
						for (final String name : ViewManager.GetFieldNames(e,ViewManager.ACTIVE,true,true)) {
						try {
                            GenericWrapperField temp = (GenericWrapperField)GenericWrapperElement.GetFieldForXMLPath(name);
                            String id = (String)fieldMap.get(name.toLowerCase());
                            try {
                                id += "\t\t" +temp.getParentElement().getSQLName() + "."+temp.getSQLName();
                            } catch (RuntimeException e1) {
                                id += "\t\t" +temp.getSQLName();
                            }
                            //
                            if (name.length() < 100)
                            {
                            	int i = 100-name.length();
                            	sb.append("\n").append(name).append(XftStringUtils.WhiteSpace(i)).append(id);
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

