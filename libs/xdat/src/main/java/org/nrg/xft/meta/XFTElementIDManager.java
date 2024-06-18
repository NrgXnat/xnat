/*
 * core: org.nrg.xft.meta.XFTElementIDManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.meta;
import java.util.Hashtable;

import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.utils.SaveItemHelper;
/**
 * @author Tim
 *
 */
public class XFTElementIDManager {
	private Hashtable idMap = new Hashtable();
	
	private static XFTElementIDManager instance = null;
	
	public static XFTElementIDManager GetInstance()throws Exception
	{
		if (instance == null)
		{
			instance = new XFTElementIDManager();
		}
		return instance;
	}
	
	public static void clean()
	{
		instance = null;
	}
	
	private XFTElementIDManager() throws Exception{
		GenericWrapperElement e =(GenericWrapperElement)GenericWrapperFactory.GetInstance().wrapElement(XFTManager.GetElementTable());
		XFTTable table = TableSearch.Execute("SELECT * FROM " + e.getSQLName(), e.getDbName(),null);
		while (table.hasMoreRows())
		{
			Hashtable row = table.nextRowHash();
			String elementName = (String)row.get("element_name");
			Integer id = (Integer)row.get("xdat_meta_element_id");
			idMap.put(elementName,id);
		}
	}
	
	private Integer createID(String s) throws Exception
	{
		if (idMap.get(s)!= null)
		{
			return (Integer)idMap.get(s);
		}else{
			GenericWrapperElement e =(GenericWrapperElement)GenericWrapperFactory.GetInstance().wrapElement(XFTManager.GetElementTable());
			XFTItem item = XFTItem.NewItem(e,null);
			item.setDirectProperty("element_name",s);
			SaveItemHelper.authorizedSave(item,null,true,false,(EventMetaI)null);
			
			Integer i = null;
			Object o = item.getProperty("xdat_meta_element_id");
			if (o== null)
			{
				throw new Exception("NO ID RETURNED.");
			}else{
				if (o.getClass().equals(Integer.class))
				{
					i = (Integer)o;
				}else{
					i = Integer.valueOf(o.toString());
				}
			
				idMap.put(s,i);
				return i;
			}
		}
	}
	
	public Integer getID(String s,boolean allowCreate) throws Exception
	{
		if (idMap.get(s) == null)
		{
			if (allowCreate)
			{
				return createID(s);
			}else{
				return null;
			}
		}else{
			return (Integer)idMap.get(s);
		}
	}
}

