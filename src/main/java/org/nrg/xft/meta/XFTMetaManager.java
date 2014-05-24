//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Dec 10, 2004
 *
 */
package org.nrg.xft.meta;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.ItemI;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.XFTElement;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XFTSchema;
import org.nrg.xft.schema.XMLType;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.schema.design.XFTElementWrapper;
import org.nrg.xft.schema.design.XFTFactoryI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.SearchCriteria;
/**
 * This singleton class maintains references to all of the available XFTElements via XFTMetaElements
 * by fullXMLName, javaName, sqlName, element ID (from DB), or code.  It provides Find methods 
 * for accessing the proper element.  It also includes Mapping collections between schema URIs 
 * and their relative prefixs.
 * 
 * @author Tim
 */
public class XFTMetaManager {
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTMetaManager.class);
	private static XFTMetaManager manager = null;
	
	private static Hashtable URI_TO_PREFIX_MAPPING = new Hashtable();
	private static Hashtable PREFIX_TO_URI_MAPPING = new Hashtable();
	
	private Hashtable sqlHash = new Hashtable();
	private Hashtable javaHash = new Hashtable();
	private Hashtable idHash = new Hashtable();
	private Hashtable fullNameHash = new Hashtable();
	private Hashtable codeHash = new Hashtable();
	private ArrayList elementNames = new ArrayList();
	
	
	/**
	 * Constructs the collection of XFTMetaManagers by iterating through the XFTDataModels in
	 * the XFTManager and creating XFTMetaElements for each XFTElement.  If allowDBAccess is
	 * true then the element IDs will be loaded from the database.  Otherwise, the element IDs are
	 * not loaded.
	 * 
	 * @param allowDBAccess
	 * @throws XFTInitException
	 */
	private XFTMetaManager() throws XFTInitException
	{
		//logger.info("Initializing XFT Meta Data Manager");
		Iterator schemas = XFTManager.GetSchemas().iterator();
		while (schemas.hasNext())
		{
			XFTSchema s = (XFTSchema)schemas.next();
			Iterator elements = s.getSortedElements().iterator();
			while (elements.hasNext())
			{
				XFTElement e = (XFTElement)elements.next();
				XFTMetaElement meta = new XFTMetaElement(e);
				sqlHash.put(meta.getSqlName().toLowerCase(),meta);
				javaHash.put(meta.getJavaName().toLowerCase(),meta);
				fullNameHash.put(e.getType().getFullLocalType().toLowerCase(),meta);
				if (meta.getCode() != null)
				{
					codeHash.put(meta.getCode().toLowerCase(),meta);
				}
				elementNames.add(e.getType().getFullLocalType().toLowerCase());
			}
		}
	}
	
	
	private XFTMetaElement getElementBySQLName(String s)
	{
	    if (s!=null)
	    {
			return (XFTMetaElement)sqlHash.get(s.toLowerCase());
		}else{
		    return null;
		}
	}
	
	private XFTMetaElement getElementByJAVAName(String s)
	{
	    if (s!=null)
	    {
			return (XFTMetaElement)javaHash.get(s.toLowerCase());
		}else{
		    return null;
		}
	}
	
	private XFTMetaElement getElementByFullXMLName(String s)
	{
	    if (s!=null)
	    {
			return (XFTMetaElement)fullNameHash.get(s.toLowerCase());
		}else{
		    return null;
		}
	}
	
	private XFTMetaElement getElementByID(Integer i)
	{
		if (idHash.size() == 0)
		{
			Iterator iter = sqlHash.values().iterator();
			while (iter.hasNext())
			{
				XFTMetaElement me = (XFTMetaElement)iter.next();
				idHash.put(me.getElementId(true),me);
			}
		}
		
		return (XFTMetaElement)idHash.get(i);
	}
	
	private XFTMetaElement getElementByCode(String s)
	{
		return (XFTMetaElement)codeHash.get(s.toLowerCase());
	}
	
	/**
	 * Singleton reference to the XFTMetaManager instance.
	 * @return
	 * @throws XFTInitException
	 */
	public static XFTMetaManager GetInstance() throws XFTInitException
	{
		if (manager == null)
		{
			manager = new XFTMetaManager();
		}
		return manager;
	}
	
	/**
	 * Initialization method used to instantiate the manager.
	 * @param allowDBAccess
	 * @throws XFTInitException
	 */
	public static void init() throws XFTInitException
	{
		manager = new XFTMetaManager();
	}
	
	public static void clean()
	{
		manager = null;
		URI_TO_PREFIX_MAPPING = new Hashtable();
		PREFIX_TO_URI_MAPPING = new Hashtable();
		XFTElementIDManager.clean();
	}
	
	/**
	 * Gets an ArrayList of all the XFTElements in the manager.
	 * @return ArrayList of strings
	 * @throws XFTInitException
	 */
	public static ArrayList GetElementNames() throws XFTInitException
	{
		return XFTMetaManager.GetInstance().elementNames;
	}
	
	/**
	 * Searches for a XFTElement with a matching Full XML Name.  If this is not found, then
	 * it searches for an informal Element Type.  If none found, then null is returned.
	 * @param s name of element to find
	 * @return XFTElement (null if not found)
	 * @throws XFTInitException
	 */
	public static XFTElement FindElement(String s) throws XFTInitException,ElementNotFoundException
	{
		XFTMetaElement xe = XFTMetaManager.GetInstance().getElementByFullXMLName(s);
		if (xe == null)
		{
			final String type = XFTReferenceManager.GetElementType(XMLType.CleanType(s));
			if (type != null)
			{
				xe = XFTMetaManager.GetInstance().getElementByFullXMLName(type);
			}
			
			if (xe == null)
			{
				if (s.indexOf(":")==-1)
				{
					//CHECK ALL KNOWN PREFIXS
					Enumeration enumer = XFTMetaManager.PREFIX_TO_URI_MAPPING.keys();
					while (enumer.hasMoreElements())
					{
						String key = (String)enumer.nextElement();
						xe = XFTMetaManager.GetInstance().getElementByFullXMLName(key + ":" + s);
						if (xe!= null)
						{
							break;
						}
					}
				}else{
					if(s.indexOf("/")>-1){
						return FindElement(XMLType.CleanType(s), XMLType.GetPrefix(s));
					}
				}
			}
		}
		if (xe == null)
		{
			throw new ElementNotFoundException(s);
		}else{
			return xe.getElement();
		}
	}
	
	/**
	 * Searches for a XFTElement with the matching XMLType.
	 * @param t XMLType of Element
	 * @return XFTElement (null if not found)
	 * @throws XFTInitException
	 */
	public static XFTElement FindElement(XMLType t) throws XFTInitException,ElementNotFoundException
	{
		XFTMetaElement xe = XFTMetaManager.GetInstance().getElementByFullXMLName(t.getFullForeignType());
		if (xe == null)
		{
			String type = XFTReferenceManager.GetElementType(t.getLocalType());
			if (type != null)
			{
				xe = XFTMetaManager.GetInstance().getElementByFullXMLName(type);
			}
		}
		if (xe == null)
		{
			throw new ElementNotFoundException(t.getFullForeignType());
		}else{
			return xe.getElement();
		}
	}
	
	/**
	 * Searches for a XFTElement with the matching name.  If none is found, it substitutes the URI's
	 * matching prefix with the given name, and preforms the search on that.  If none is found, null
	 * is returned.
	 * @param name name of item to find.
	 * @param uri Schema URI which maps to a given XFTSchema
	 * @return XFTElement (null if not found)
	 * @throws XFTInitException
	 */
	public static XFTElement FindElement(String name,String uri) throws XFTInitException,ElementNotFoundException
	{
		XFTElement xe = XFTMetaManager.FindElement(name);
		if (xe ==null)
		{
			String abbr = TranslateURIToPrefix(uri);
			if (abbr != null)
			{
				xe= FindElement(abbr + ":" + XMLType.CleanType(name));
			}
		}
		if (xe == null)
		{
			throw new ElementNotFoundException(name);
		}else{
			return xe;
		}
	}

	/**
	 * Searches for a XFTElement with the matching XMLType, and wraps it using the XFTFactoryI.
	 * @param factory
	 * @param t
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static XFTElementWrapper GetWrappedElementByXMLType(XFTFactoryI factory,XMLType t) throws XFTInitException,ElementNotFoundException
	{
		XFTElement e = XFTMetaManager.FindElement(t);
		if (e != null)
		{
			return factory.wrapElement(e);
		}else{
			return null;
		}
	}

	/**
	 * Searches for a XFTElement with the matching name.  If none is found, it substitutes the URI's
	 * matching prefix with the given name, and preforms the search on that.  If none is found, null
	 * is returned.  Otherwise the found element is wrapped using the XFTFactoryI.
	 * @param factory
	 * @param name
	 * @param uri
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static XFTElementWrapper GetWrappedElementByNameAndURI(XFTFactoryI factory,String name, String uri) throws XFTInitException,ElementNotFoundException
	{
		String abbr = null;
		if (uri != null && !uri.equals(""))
		{
			abbr = TranslateURIToPrefix(uri);
		}else{
			XFTElement e = XFTMetaManager.FindElement(name);
			if (e != null)
			{
				return factory.wrapElement(e);
			}else{
				return null;
			}
		}
		
		if (abbr != null)
		{
			XFTElement e;
			try {
				e = XFTMetaManager.FindElement(abbr + ":" + name);
			} catch (ElementNotFoundException e1) {
				e = XFTMetaManager.FindElement(name);
				if (e != null)
				{
					return factory.wrapElement(e);
				}else{
					return null;
				}
			}
			
			if (e != null)
			{
				return factory.wrapElement(e);
			}else{
				return null;
			}
		}else
		{
			return null;
		}
	}

	/**
	 * Searches for a XFTElement with a matching Full XML Name.  If this is not found, then
	 * it searches for an informal Element Type.  If none found, then null is returned.  Otherwise,
	 * the found element is wrapped by the XFTFactoryI.
	 * @param factory
	 * @param name
	 * @return
	 * @throws XFTInitException
	 * @throws ElementNotFoundException
	 */
	public static XFTElementWrapper GetWrappedElementByName(XFTFactoryI factory,String name) throws XFTInitException,ElementNotFoundException
	{
		XFTElement e = XFTMetaManager.FindElement(name);
		if (e != null)
		{
			return factory.wrapElement(e);
		}else{
			return null;
		}
	}
	
	public static XFTElementWrapper GetWrappedElementByCode(XFTFactoryI factory,String code) throws XFTInitException,ElementNotFoundException
	{
		XFTMetaElement e = XFTMetaManager.GetInstance().getElementByCode(code);
		if (e != null)
		{
			return factory.wrapElement(e.getElement());
		}else{
			return null;
		}
	}
	
	/**
	 * Gets the ID from the database table XFT_Elements for this element.
	 * @param e
	 * @return
	 * @throws XFTInitException
	 */
	public static Integer GetElementID(GenericWrapperElement e) throws XFTInitException
	{
		XFTMetaElement me = XFTMetaManager.GetInstance().getElementByFullXMLName(e.getFullXMLName());
		return me.getElementId(true);
	}
	
	/**
	 * Gets the element that matches this id.
	 * @param e
	 * @return
	 * @throws XFTInitException
	 */
	public static ItemI GetElementExtensionItemByID(Integer i) throws XFTInitException,ElementNotFoundException,FieldNotFoundException,Exception
	{
		XFTMetaElement me = XFTMetaManager.GetInstance().getElementByID(i);
		if (me == null)
		{
			ItemSearch search = new ItemSearch(null,(GenericWrapperElement)GenericWrapperFactory.GetInstance().wrapElement(XFTManager.GetElementTable()));
			SearchCriteria c = new SearchCriteria();
			c.setFieldWXMLPath("xdat:meta_element/xdat_meta_element_id");
			c.setValue(i);
			search.addCriteria(c);
			ItemCollection items = search.exec(false);
			if (items.size() > 0)
			{
				return items.get(0);
			}else{
				return null;
			}
		}
		return me.getExtensionXFTItem(false);
	}
	
	public static ItemI GetExtensionXFTItem(GenericWrapperElement e) throws XFTInitException,ElementNotFoundException
	{
		XFTMetaElement me = XFTMetaManager.GetInstance().getElementByFullXMLName(e.getFullXMLName());
		return me.getExtensionXFTItem(true);
	}
	
	/**
	 * Translates the supplied URI to its corresponding prefix.
	 * @param uri
	 * @return
	 */
	public static String TranslateURIToPrefix(String uri)
	{
		return (String)URI_TO_PREFIX_MAPPING.get(uri);
	}
	
	/**
	 * Adds the provided URI-Prefix mapping.
	 * @param uri
	 * @param prefix
	 */
	public static void AddURIToPrefixMapping(String uri,String prefix)
	{
		URI_TO_PREFIX_MAPPING.put(uri,prefix);
		PREFIX_TO_URI_MAPPING.put(prefix,uri);
	}
	
	/**
	 * Translates the supplied prefix to its corresponding URI.
	 * @param prefix
	 * @return
	 */
	public static String TranslatePrefixToURI(String prefix)
	{
		return (String)PREFIX_TO_URI_MAPPING.get(prefix);
	}
	
	public static Enumeration getPrefixEnum()
	{
	    return PREFIX_TO_URI_MAPPING.keys();
	}
}

