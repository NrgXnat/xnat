//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/* 
 * XDAT � Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 19, 2004
 */
package org.nrg.xft.references;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.utils.FileUtils;
/**
 * Object used to maintain a list of pseudonyms which can be used to describe a specific 
 * full xml name.  These usually include proper names, reference field names, and extensions.
 * 
 * <BR><BR>A static Hashtable is used to maintain all of the element types and their pseudonyms.
 * 
 * @author Tim
 */
public class XFTPseudonymManager {
	static org.apache.log4j.Logger logger = Logger.getLogger(XFTPseudonymManager.class);
	private String name = null;
	private ArrayList pseudos = new ArrayList();
	private static Hashtable Pseudonyms = new Hashtable();
	
	
	public static void clean()
	{
		Pseudonyms = new Hashtable();
	}
	/**
	 * An item which can be a pseudonym for another type or have pseudonyms for it.
	 * @param primary
	 */
	private XFTPseudonymManager(String primary)
	{
		name = primary;
	}
	
	/**
	 * An item which can be a pseudonym for another type or have pseudonyms for it (including
	 * the specified pseudoynm).
	 * @param primary
	 * @param pseudonym
	 */
	private XFTPseudonymManager(String primary,String pseudonym)
	{
		name = primary;
		if (Pseudonyms.get(pseudonym) != null)
		{
			XFTPseudonymManager temp = (XFTPseudonymManager)Pseudonyms.get(pseudonym);
			pseudos.add(temp);
		}else
		{
			XFTPseudonymManager temp = new XFTPseudonymManager(pseudonym);
			Pseudonyms.put(temp.getName(),temp);
			pseudos.add(temp);
		}
	}
	
	/**
	 * Adds a new type to the collection of pseudonyms.
	 * @param primary
	 */
	public static void AddNewPseudonymManager(String primary)
	{
		if (Pseudonyms.get(primary) == null)
		{
			XFTPseudonymManager temp = new XFTPseudonymManager(primary);
			Pseudonyms.put(temp.getName(),temp);
		}
	}
	
	/**
	 * Adds a new type with the given pseudonym.
	 * @param primary
	 * @param pseudonym
	 */
	public static void AddNewPseudonymManager(String primary,String pseudonym)
	{
		if (Pseudonyms.get(primary) == null)
		{
			XFTPseudonymManager temp = new XFTPseudonymManager(primary,pseudonym);
			Pseudonyms.put(temp.getName(),temp);
		}
		else{
			((XFTPseudonymManager)Pseudonyms.get(primary)).addPseudonym(pseudonym);
		}
	}
	
	/**
	 * If this type doesn't already exist as a pseudonym of the parent type, 
	 * then it is added to the collection of pseudonyms for the parent type.
	 * @param pseudonym
	 */
	public void addPseudonym(String pseudonym)
	{
		if (Pseudonyms.get(pseudonym) != null)
		{
			Iterator iter = pseudos.iterator();
			boolean found = false;
			while (iter.hasNext())
			{
				XFTPseudonymManager temp = (XFTPseudonymManager)iter.next();
				if (temp.getName().equalsIgnoreCase(pseudonym))
				{
					found = true;
				}
			}
			
			if (! found) pseudos.add(Pseudonyms.get(pseudonym));
		}else
		{
			XFTPseudonymManager temp = new XFTPseudonymManager(pseudonym);
			Pseudonyms.put(temp.getName(),temp);
			pseudos.add(temp);
		}
	}
	
	/**
	 * Gets the collection of pseudoynm names for this parent type.
	 * @return ArrayList of Strings
	 */
	public ArrayList getPseudonymNames()
	{
		ArrayList al = new ArrayList();
		
		Iterator pseudosIter = this.pseudos.iterator();
		while (pseudosIter.hasNext())
		{
			XFTPseudonymManager pseudo = (XFTPseudonymManager)pseudosIter.next();
			
			if (! al.contains(pseudo.getName()))
			{
				al.add(pseudo.getName());
				al.addAll(pseudo.getPseudonymNames());
			}
		}
		
		al.trimToSize();
		return al;
	}
	
	/**
	 * Gets the collection of extension names for this parent type.
	 * @return
	 */
	public ArrayList getExtentions()
	{
		ArrayList al = new ArrayList();
		
		Iterator p = getPseudonyms().iterator();
		while (p.hasNext())
		{
			XFTPseudonymManager x = (XFTPseudonymManager)p.next();
			if (x.getPseudonyms().size() > 0)
			{
				al.addAll(x.getExtentions());
			}else{
				if (! al.contains(x.getName()))
					al.add(x.getName());
			}
		}		
		
		al.trimToSize();
		return al;
	}
	
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * ArrayList of XFTPseudonymManagers
	 * @return
	 */
	public ArrayList getPseudonyms() {
		return pseudos;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * ArrayList of XFTPseudonymManagers
	 * @param list
	 */
	public void setPseudonyms(ArrayList list) {
		pseudos = list;
	}
	
	/**
	 * if this type has any pseudonyms.
	 * @param s
	 * @return
	 */
	public static boolean HasPseudonyms(String s)
	{
		if (Pseudonyms.get(s) != null)
		{
			if (((XFTPseudonymManager)Pseudonyms.get(s)).getPseudonyms().size() > 0)
			{
				return true;
			}else{
				return false;
			}
		}else
		{
			return false;
		}
	}
	
	/**
	 * Gets pseudonyms for the given type.
	 * @param s
	 * @return
	 */
	public static ArrayList GetPseudonyms(String s)
	{
		ArrayList al = new ArrayList();
		
		if (Pseudonyms.get(s) != null)
		{
			al.addAll(((XFTPseudonymManager)Pseudonyms.get(s)).getPseudonymNames());
		}
		
		al.trimToSize();
		return al;
	}
	
	/**
	 * if alias is a pseudonym for the primary type.
	 * @param primary
	 * @param alias
	 * @return
	 */
	public static boolean IsAnAlias(String primary,String alias)
	{
		if (HasPseudonyms(primary))
		{
			if (GetPseudonyms(primary).contains(alias))
			{
				return true;
			}else
			{
				return false;
			}
			
		}else
		{
			return false;
		}
	}

	
	/**
	 * Gets collection of pseudonym names for the given type.
	 * @param s
	 * @return ArrayList of strings
	 */
	public static ArrayList GetExtensionItems(String s)
	{
		ArrayList al = new ArrayList();
		
		if (Pseudonyms.get(s) != null)
		{
			XFTPseudonymManager x = (XFTPseudonymManager)Pseudonyms.get(s);
			al.addAll(x.getExtentions());
		}
		
		al.trimToSize();
		return al;
	}
	
	/**
	 * Gets the GenericWrapperElements for the pseudonym.
	 * @param s
	 * @return ArrayList of GenericWrapperElements
	 */
	public static ArrayList GetExtensionElements(String s) throws XFTInitException
	{
		ArrayList al = new ArrayList();
		Iterator iter = GetExtensionItems(s).iterator();
		while (iter.hasNext())
		{
			String n = (String)iter.next();
			try {
				GenericWrapperElement e = GenericWrapperElement.GetElement(n);
				al.add(e);
			} catch (ElementNotFoundException ex) {}
		}
		al.trimToSize();
		return al;
	}

	/**
	 * Outputs the list of pseudonyms to a file called 'pseudonyms.txt'.
	 */
	public static void OutputPseudonyms()
	{
		try {
			StringBuffer p = new StringBuffer();
			StringBuffer e = new StringBuffer();
			Enumeration enumer = Pseudonyms.keys();
			while(enumer.hasMoreElements())
			{
				XFTPseudonymManager manager =(XFTPseudonymManager)Pseudonyms.get(enumer.nextElement());
				if (manager.getPseudonyms().size() > 0)
				{
					Iterator iter = manager.getPseudonymNames().iterator();
					p.append("\n").append(manager.getName()).append(" -> ");
					int counter = 0;
					while (iter.hasNext())
					{
						String n = (String)iter.next();
						if (counter++ == 0)
						{
							p.append(n);
						}else{
							p.append(",").append(n);
						}
					}
				}
			}
			FileUtils.OutputToFile(p.toString(),XFTManager.GetInstance().getSourceDir() + "pseudonyms.txt");
		} catch (org.nrg.xft.exception.XFTInitException e) {
			logger.error("",e);
		} catch (Exception e) {
			logger.error("",e);
		}
	}

	/**
	 * Adds new pseudonym
	 * @param name
	 * @param alias
	 */
	public static void AddPseudonym(String name, String alias)
	{
		XFTPseudonymManager.AddNewPseudonymManager(alias,name);
	}
}

