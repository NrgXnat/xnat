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
public abstract class AutoCatEntryMetafield extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.CatEntryMetafieldI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoCatEntryMetafield.class);
	public static String SCHEMA_ELEMENT_NAME="cat:entry_metaField";

	public AutoCatEntryMetafield(ItemI item)
	{
		super(item);
	}

	public AutoCatEntryMetafield(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoCatEntryMetafield(UserI user)
	 **/
	public AutoCatEntryMetafield(){}

	public AutoCatEntryMetafield(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "cat:entry_metaField";
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

	private Integer _CatEntryMetafieldId=null;

	/**
	 * @return Returns the cat_entry_metaField_id.
	 */
	public Integer getCatEntryMetafieldId() {
		try{
			if (_CatEntryMetafieldId==null){
				_CatEntryMetafieldId=getIntegerProperty("cat_entry_metaField_id");
				return _CatEntryMetafieldId;
			}else {
				return _CatEntryMetafieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cat_entry_metaField_id.
	 * @param v Value to Set.
	 */
	public void setCatEntryMetafieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cat_entry_metaField_id",v);
		_CatEntryMetafieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.CatEntryMetafield> getAllCatEntryMetafields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatEntryMetafield> al = new ArrayList<org.nrg.xdat.om.CatEntryMetafield>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatEntryMetafield> getCatEntryMetafieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatEntryMetafield> al = new ArrayList<org.nrg.xdat.om.CatEntryMetafield>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatEntryMetafield> getCatEntryMetafieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatEntryMetafield> al = new ArrayList<org.nrg.xdat.om.CatEntryMetafield>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static CatEntryMetafield getCatEntryMetafieldsByCatEntryMetafieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("cat:entry_metaField/cat_entry_metaField_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (CatEntryMetafield) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
