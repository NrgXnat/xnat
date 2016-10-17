/*
 * core: org.nrg.xft.collections.ItemCollection
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.collections;

import org.apache.log4j.Logger;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.security.UserI;

import java.util.*;

/**
 * @author Tim
 *
 */
public class ItemCollection {
	static org.apache.log4j.Logger logger = Logger.getLogger(ItemCollection.class);
	private ArrayList<ItemI> items = new ArrayList<ItemI>();
	
	private String lastSortBy = null;
	private ArrayList<ItemI> lastSort = null;
    private String lastSortOrder = null;
	
	public ItemCollection(){}
	
	public ItemCollection(ArrayList al)
	{
	    items = al;
	}
	
	public ArrayList<ItemI> getItems()
	{
		return items;
	}
	
	public ArrayList items()
	{
		return items;
	}
	
	public Iterator getItemIterator()
	{
		return getItems().iterator();
	}
	
	public void addItem(ItemI item)
	{
		items.add(item);
		lastSortBy = null;
	}
	
	public void add(ItemI item)
	{
		addItem(item);
	}
	
	public void addAll(List list)
	{
		items.addAll(list);
		lastSortBy=null;
	}
	
	public ItemI get(int i)
	{
		return (ItemI)items.get(i);
	}
	
	public void clear()
	{
	    items = new ArrayList<ItemI>();
	}
	
	public ItemI getFirst()
	{
		if (size()> 0)
		{
			return get(0);
		}else{
			return null;
		}
	}
	
	public ItemI first()
	{
	    return getFirst();
	}
	
	public Iterator iterator()
	{
		return items.iterator();
	}
	
	public void extendAll(boolean allowMultiples) throws XFTInitException,ElementNotFoundException, Exception
	{
		Iterator iter = getItemIterator();
		while(iter.hasNext())
		{
			ItemI item = (ItemI)iter.next();
			item.extend(allowMultiples);
		}
	}
	
	public void secureAllForRead(UserI user) throws XFTInitException,ElementNotFoundException,IllegalAccessException,org.nrg.xft.exception.MetaDataException, Exception
	{
		Iterator iter = getItemIterator();
		ArrayList<ItemI> remove = new ArrayList<ItemI>();
		while(iter.hasNext())
		{
			ItemI item = (ItemI)iter.next();
			try {
                ItemI temp = Permissions.secureItem(user,item);
                if (temp == null)
                {
                    remove.add(item);
                }
            } catch (IllegalAccessException e) {
                if (items.size()==1)
                {
                    throw e;
                }else{
                    remove.add(item);
                }
            }catch (org.nrg.xft.exception.MetaDataException e)
            {
                if (items.size()==1)
                {
                    throw e;
                }else{
                    remove.add(item);
                }
            }
		}
		
		if (remove.size() > 0)
		{
		    this.items.removeAll(remove);
		}
	}
	
   
    
    public ArrayList getItems(String sortBy, String sortOrder) throws FieldNotFoundException,ClassCastException
    {
        if (lastSortBy != null && lastSortBy.equalsIgnoreCase(sortBy) && lastSortOrder != null && lastSortOrder.equalsIgnoreCase(sortOrder))
        {
            return lastSort;
        }else{
            ArrayList<ItemI> sortedByOrder = getItems(sortBy);
            
            if (sortOrder != null && sortOrder.equalsIgnoreCase("DESC")) {
                Collections.reverse(sortedByOrder);
                lastSort = sortedByOrder;
            }
            lastSortOrder = sortOrder;
            return sortedByOrder;
            
        }
    }
    /**
	 * Sorts items by the field specified (via XML stype dot-syntax).  Field's value must be Comparable
	 * @param sortBy
	 * @return Returns a list of items sorted by the field specified
	 * @throws FieldNotFoundException
	 */
	public ArrayList<ItemI> getItems(String sortBy) throws FieldNotFoundException,ClassCastException
	{
		if (lastSortBy != null && lastSortBy.equalsIgnoreCase(sortBy))
		{
			return lastSort;
		}else{
			try {
				lastSortBy = sortBy;
				GenericWrapperField f = GenericWrapperElement.GetFieldForXMLPath(sortBy);
				Comparator byField = new ItemComparator(sortBy);
				lastSort = this.getItems();
				Collections.sort(lastSort,byField);
				return lastSort;
			} catch (XFTInitException e) {
				throw new FieldNotFoundException(sortBy);
			} catch (ElementNotFoundException e) {
				throw new FieldNotFoundException(sortBy);
			}
		}
	}
	
	public int size()
	{
		return this.items.size();
	}
	
	/**
	 * @param xmlPath
	 * @return Returns item iterator
	 * @throws FieldNotFoundException
	 */
	public Iterator getItemIterator(String xmlPath) throws FieldNotFoundException,ClassCastException
	{
		return getItems(xmlPath).iterator();
		
	}
	
	/**
	 * Return ItemCollection of items where this value is found at the specified xml location
	 * @param xmlPath
	 * @param value
	 * @return Returns ItemCollection of items where this value is found at the specified xml location
	 */
	public ItemCollection getItems(String xmlPath, Object value)
	{
	    ItemCollection temp = new ItemCollection();
	    Iterator iter = getItemIterator();
        
	    while (iter.hasNext())
	    {
	        XFTItem item = (XFTItem)iter.next();
	        try {
                if (item.hasProperty(xmlPath,value))
                {
                    temp.add(item);
                }
            } catch (Exception e) {
                logger.error("",e);
            }
	    }
	    return temp;
	}
	
	public XFTItem getItem(String xmlPath, Object value)
	{
	    XFTItem temp = null;
	    Iterator iter = getItemIterator();
        
	    while (iter.hasNext())
	    {
	        XFTItem item = (XFTItem)iter.next();
	        try {
                if (item.hasProperty(xmlPath,value))
                {
                    temp = item;
                    break;
                }
            } catch (Exception e) {
                logger.error("",e);
                return null;
            }
	    }
	    return temp;
	}
	
	public String toString()
	{
	    return items.toString();
	}
	
	public void finalizeLoading()
	{
	    Iterator iter = getItemIterator();
		while(iter.hasNext())
		{
			ItemI item = (ItemI)iter.next();
			((XFTItem)item).setLoading(false);
		}
	}
	

    public boolean containsByPK(ItemI item,boolean checkExtensions)
    {
        Iterator iter = this.getItemIterator();
        while(iter.hasNext())
        {
            ItemI temp = (ItemI)iter.next();
            try {
                if (XFTItem.CompareItemsByPKs(item.getItem(),temp.getItem(),false,checkExtensions))
                {
                    return true;
                }
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        
        return false;
    }	

    public ItemI findByPK(ItemI item,boolean checkExtensions)
    {
        Iterator iter = this.getItemIterator();
        while(iter.hasNext())
        {
            ItemI temp = (ItemI)iter.next();
            try {
                if (XFTItem.CompareItemsByPKs(item.getItem(),temp.getItem(),false,checkExtensions))
                {
                    return temp;
                }
            } catch (Exception e) {
                logger.error("",e);
            }
        }
        
        
        return null;
    }

    public boolean containsByUnique(ItemI item,boolean checkExtensions)
    {
        try {
            if (item.getItem().hasUniques())
            {
                Iterator iter = this.getItemIterator();
                while(iter.hasNext())
                {
                    ItemI temp = (ItemI)iter.next();
                    try {
                        if (XFTItem.CompareItemsByUniques(item.getItem(),temp.getItem(),checkExtensions))
                        {
                            return true;
                        }
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
        
        return false;
    }
    
    public boolean contains(ItemI item,boolean checkExtensions)
    {
        if (containsByPK(item,checkExtensions))
        {
            return true;
        }else{
            return containsByUnique(item,checkExtensions);
        }
    }


    public ItemI findByUnique(ItemI item,boolean checkExtensions)
    {
        try {
            if (item.getItem().hasUniques())
            {
                Iterator iter = this.getItemIterator();
                while(iter.hasNext())
                {
                    ItemI temp = (ItemI)iter.next();
                    try {
                        if (XFTItem.CompareItemsByUniques(item.getItem(),temp.getItem(),checkExtensions))
                        {
                            return temp;
                        }
                    } catch (Exception e) {
                        logger.error("",e);
                    }
                }
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        }
        
        return null;
    }
    
    public ItemI find(ItemI item,boolean checkExtensions)
    {
        ItemI match = null;
        
        try {
            if(item.getItem().hasPK())
            {
                match = this.findByPK(item,checkExtensions);
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        } catch (ElementNotFoundException e) {
            logger.error("",e);
        } catch (FieldNotFoundException e) {
            logger.error("",e);
        }
        
        if (match==null)
        {
            return this.findByUnique(item,checkExtensions);
        }
        
        return match;
    }
    
    public String output(){
        StringBuffer sb= new StringBuffer();
        Iterator iter = this.getItemIterator();
        while (iter.hasNext())
        {
            XFTItem item =(XFTItem)iter.next();
            ItemI bo = BaseElement.GetGeneratedItem(item);
            sb.append(bo.output());

            sb.append("\n");
        }
        String s = sb.toString();
        return s;
    }
    
    public String output(String templateName){
        StringBuffer sb= new StringBuffer();
        Iterator iter = this.getItemIterator();
        while (iter.hasNext())
        {
            XFTItem item =(XFTItem)iter.next();
            ItemI bo = BaseElement.GetGeneratedItem(item);
            sb.append(bo.output(templateName));

            sb.append("\n");
        }
        return sb.toString();
    }
    
	public class ItemComparator implements Comparator{
		private String field = "";
		public ItemComparator(String f)
		{
			field = f;
		}
        
        public int compare(Object o1, Object o2) {
			try {
				Comparable value1 = (Comparable)((ItemI)o1).getProperty(field);
				Comparable value2 = (Comparable)((ItemI)o2).getProperty(field);
				if (value1 == null){
					if (value2 == null)
					{
						return 0;
					}else{
                        return -1;
					
                    }
				}
				if (value2== null)
				{
					return 1;
				}
				int i =  value1.compareTo(value2);
				return i;
			} catch (XFTInitException e) {
				e.printStackTrace();
			} catch (ElementNotFoundException e) {
				e.printStackTrace();
			} catch (FieldNotFoundException e) {
				e.printStackTrace();
			}
			return 0;
		}
		public void setField(String s){field = s;}
	}

    
    public boolean removeItem(ItemI i){
        return this.items.remove(i);
    }
    
    public void merge(ItemCollection _new){
    	if(_new==null)return;
    	for(ItemI i: _new.items){
    		if(!this.contains(i, false)){
    			this.add(i);
    		}
    	}
    }
}

