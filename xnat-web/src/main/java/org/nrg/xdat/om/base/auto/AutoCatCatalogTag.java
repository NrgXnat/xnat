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
public abstract class AutoCatCatalogTag extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.CatCatalogTagI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoCatCatalogTag.class);
	public static String SCHEMA_ELEMENT_NAME="cat:catalog_tag";

	public AutoCatCatalogTag(ItemI item)
	{
		super(item);
	}

	public AutoCatCatalogTag(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoCatCatalogTag(UserI user)
	 **/
	public AutoCatCatalogTag(){}

	public AutoCatCatalogTag(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "cat:catalog_tag";
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

	private Integer _CatCatalogTagId=null;

	/**
	 * @return Returns the cat_catalog_tag_id.
	 */
	public Integer getCatCatalogTagId() {
		try{
			if (_CatCatalogTagId==null){
				_CatCatalogTagId=getIntegerProperty("cat_catalog_tag_id");
				return _CatCatalogTagId;
			}else {
				return _CatCatalogTagId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cat_catalog_tag_id.
	 * @param v Value to Set.
	 */
	public void setCatCatalogTagId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cat_catalog_tag_id",v);
		_CatCatalogTagId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.CatCatalogTag> getAllCatCatalogTags(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatCatalogTag> al = new ArrayList<org.nrg.xdat.om.CatCatalogTag>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatCatalogTag> getCatCatalogTagsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatCatalogTag> al = new ArrayList<org.nrg.xdat.om.CatCatalogTag>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatCatalogTag> getCatCatalogTagsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatCatalogTag> al = new ArrayList<org.nrg.xdat.om.CatCatalogTag>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static CatCatalogTag getCatCatalogTagsByCatCatalogTagId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("cat:catalog_tag/cat_catalog_tag_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (CatCatalogTag) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
