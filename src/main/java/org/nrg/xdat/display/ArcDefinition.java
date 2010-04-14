//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 6, 2005
 *
 */
package org.nrg.xdat.display;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
/**
 * @author Tim
 *
 */
public class ArcDefinition {
	private String name = null;
	private String bridgeElement = "";
	private String bridgeField = "";
	private ArrayList filters = new ArrayList();//String[]filterField,filterType
	private Hashtable commonFields = new Hashtable();//fieldID,fieldType
	private ArrayList members = new ArrayList();
	/**
	 * @return
	 */
	public Hashtable getCommonFields() {
		return commonFields;
	}

	/**
	 * @return ArrayList of String[]fieldID,filterType
	 */
	public ArrayList getFilters() {
		return filters;
	}

	/**
	 * @return
	 */
	public String getBridgeElement() {
		return bridgeElement;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param hashtable
	 */
	public void setCommonFields(Hashtable hashtable) {
		commonFields = hashtable;
	}
	
	public void addCommonField(String id,String type)
	{
		commonFields.put(id,type);
	}

	/**
	 * @param list
	 */
	public void setFilters(ArrayList list) {
		filters = list;
	}
	
	public void addFilter(String fieldID,String filterType)
	{
		filters.add(new String[]{fieldID,filterType});
	}

	/**
	 * @param string
	 */
	public void setBridgeElement(String string) {
		bridgeElement = string;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * Iterator of ArrayList of ElementNames (String)
	 * @return
	 */
	public Iterator getMembers() {
		return members.iterator();
	}
	
	public void addMember(String elementName)
	{
		members.add(elementName);
	}
	
	public boolean isMember(String elementName)
	{
		return members.contains(elementName);
	}

	/**
	 * @return
	 */
	public String getBridgeField() {
		return bridgeField;
	}

	/**
	 * @param string
	 */
	public void setBridgeField(String string) {
		bridgeField = string;
	}
	
	public String getDistinctField()
	{
		String field = null;
		Iterator iter = this.filters.iterator();
		while (iter.hasNext())
		{
			String[] filter = (String[])iter.next();
			if (filter[1].equalsIgnoreCase("distinct"))
			{
				field= filter[0];
				break;
			}
		}
		return field;
	}
	
	public String getEqualsField()
	{
		String field = null;
		Iterator iter = this.filters.iterator();
		while (iter.hasNext())
		{
			String[] filter = (String[])iter.next();
			if (filter[1].equalsIgnoreCase("equals"))
			{
				field= filter[0];
				break;
			}
		}
		return field;
	}
	
	public String getClosestField()
	{
		String field = null;
		Iterator iter = this.filters.iterator();
		while (iter.hasNext())
		{
			String[] filter = (String[])iter.next();
			if (filter[1].equalsIgnoreCase("closest"))
			{
				field= filter[0];
				break;
			}
		}
		return field;
	}

}

