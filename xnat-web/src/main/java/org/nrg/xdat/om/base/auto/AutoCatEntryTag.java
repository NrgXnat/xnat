/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:04 UTC 2016
 *
 */
package org.nrg.xdat.om.base.auto;
import org.nrg.xft.*;
import org.nrg.xft.security.UserI;
import org.nrg.xdat.om.*;
import org.nrg.xft.utils.ResourceFile;
import org.nrg.xft.exception.*;

import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoCatEntryTag extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.CatEntryTagI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoCatEntryTag.class);
	public static String SCHEMA_ELEMENT_NAME="cat:entry_tag";

	public AutoCatEntryTag(ItemI item)
	{
		super(item);
	}

	public AutoCatEntryTag(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoCatEntryTag(UserI user)
	 **/
	public AutoCatEntryTag(){}

	public AutoCatEntryTag(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "cat:entry_tag";
	}

	//FIELD

	private String _Tag=null;

	/**
	 * @return Returns the tag.
	 */
	public String getTag(){
		try{
			if (_Tag==null){
				_Tag=getStringProperty("tag");
				return _Tag;
			}else {
				return _Tag;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for tag.
	 * @param v Value to Set.
	 */
	public void setTag(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/tag",v);
		_Tag=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _CatEntryTagId=null;

	/**
	 * @return Returns the cat_entry_tag_id.
	 */
	public Integer getCatEntryTagId() {
		try{
			if (_CatEntryTagId==null){
				_CatEntryTagId=getIntegerProperty("cat_entry_tag_id");
				return _CatEntryTagId;
			}else {
				return _CatEntryTagId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cat_entry_tag_id.
	 * @param v Value to Set.
	 */
	public void setCatEntryTagId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cat_entry_tag_id",v);
		_CatEntryTagId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.CatEntryTag> getAllCatEntryTags(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatEntryTag> al = new ArrayList<org.nrg.xdat.om.CatEntryTag>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatEntryTag> getCatEntryTagsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatEntryTag> al = new ArrayList<org.nrg.xdat.om.CatEntryTag>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatEntryTag> getCatEntryTagsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatEntryTag> al = new ArrayList<org.nrg.xdat.om.CatEntryTag>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static CatEntryTag getCatEntryTagsByCatEntryTagId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("cat:entry_tag/cat_entry_tag_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (CatEntryTag) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static ArrayList wrapItems(ArrayList items)
	{
		ArrayList al = new ArrayList();
		al = org.nrg.xdat.base.BaseElement.WrapItems(items);
		al.trimToSize();
		return al;
	}

	public static ArrayList wrapItems(org.nrg.xft.collections.ItemCollection items)
	{
		return wrapItems(items.getItems());
	}
	public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop){
ArrayList<ResourceFile> _return = new ArrayList<ResourceFile>();
	 boolean localLoop = preventLoop;
	        localLoop = preventLoop;
	
	return _return;
}
}
