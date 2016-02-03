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
public abstract class AutoCatCatalogMetafield extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.CatCatalogMetafieldI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoCatCatalogMetafield.class);
	public static String SCHEMA_ELEMENT_NAME="cat:catalog_metaField";

	public AutoCatCatalogMetafield(ItemI item)
	{
		super(item);
	}

	public AutoCatCatalogMetafield(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoCatCatalogMetafield(UserI user)
	 **/
	public AutoCatCatalogMetafield(){}

	public AutoCatCatalogMetafield(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "cat:catalog_metaField";
	}

	//FIELD

	private String _Metafield=null;

	/**
	 * @return Returns the metaField.
	 */
	public String getMetafield(){
		try{
			if (_Metafield==null){
				_Metafield=getStringProperty("metaField");
				return _Metafield;
			}else {
				return _Metafield;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for metaField.
	 * @param v Value to Set.
	 */
	public void setMetafield(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/metaField",v);
		_Metafield=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Name=null;

	/**
	 * @return Returns the name.
	 */
	public String getName(){
		try{
			if (_Name==null){
				_Name=getStringProperty("name");
				return _Name;
			}else {
				return _Name;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for name.
	 * @param v Value to Set.
	 */
	public void setName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/name",v);
		_Name=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _CatCatalogMetafieldId=null;

	/**
	 * @return Returns the cat_catalog_metaField_id.
	 */
	public Integer getCatCatalogMetafieldId() {
		try{
			if (_CatCatalogMetafieldId==null){
				_CatCatalogMetafieldId=getIntegerProperty("cat_catalog_metaField_id");
				return _CatCatalogMetafieldId;
			}else {
				return _CatCatalogMetafieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cat_catalog_metaField_id.
	 * @param v Value to Set.
	 */
	public void setCatCatalogMetafieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cat_catalog_metaField_id",v);
		_CatCatalogMetafieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.CatCatalogMetafield> getAllCatCatalogMetafields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatCatalogMetafield> al = new ArrayList<org.nrg.xdat.om.CatCatalogMetafield>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatCatalogMetafield> getCatCatalogMetafieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatCatalogMetafield> al = new ArrayList<org.nrg.xdat.om.CatCatalogMetafield>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatCatalogMetafield> getCatCatalogMetafieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatCatalogMetafield> al = new ArrayList<org.nrg.xdat.om.CatCatalogMetafield>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static CatCatalogMetafield getCatCatalogMetafieldsByCatCatalogMetafieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("cat:catalog_metaField/cat_catalog_metaField_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (CatCatalogMetafield) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
