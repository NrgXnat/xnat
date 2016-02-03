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
public abstract class AutoCatCatalog extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.CatCatalogI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoCatCatalog.class);
	public static String SCHEMA_ELEMENT_NAME="cat:catalog";

	public AutoCatCatalog(ItemI item)
	{
		super(item);
	}

	public AutoCatCatalog(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoCatCatalog(UserI user)
	 **/
	public AutoCatCatalog(){}

	public AutoCatCatalog(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "cat:catalog";
	}
	 private ArrayList<org.nrg.xdat.om.CatCatalogMetafield> _Metafields_metafield =null;

	/**
	 * metaFields/metaField
	 * @return Returns an List of org.nrg.xdat.om.CatCatalogMetafield
	 */
	public <A extends org.nrg.xdat.model.CatCatalogMetafieldI> List<A> getMetafields_metafield() {
		try{
			if (_Metafields_metafield==null){
				_Metafields_metafield=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("metaFields/metaField"));
			}
			return (List<A>) _Metafields_metafield;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.CatCatalogMetafield>();}
	}

	/**
	 * Sets the value for metaFields/metaField.
	 * @param v Value to Set.
	 */
	public void setMetafields_metafield(ItemI v) throws Exception{
		_Metafields_metafield =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/metaFields/metaField",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/metaFields/metaField",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * metaFields/metaField
	 * Adds org.nrg.xdat.model.CatCatalogMetafieldI
	 */
	public <A extends org.nrg.xdat.model.CatCatalogMetafieldI> void addMetafields_metafield(A item) throws Exception{
	setMetafields_metafield((ItemI)item);
	}

	/**
	 * Removes the metaFields/metaField of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeMetafields_metafield(int index) throws java.lang.IndexOutOfBoundsException {
		_Metafields_metafield =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/metaFields/metaField",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.CatCatalogTag> _Tags_tag =null;

	/**
	 * tags/tag
	 * @return Returns an List of org.nrg.xdat.om.CatCatalogTag
	 */
	public <A extends org.nrg.xdat.model.CatCatalogTagI> List<A> getTags_tag() {
		try{
			if (_Tags_tag==null){
				_Tags_tag=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("tags/tag"));
			}
			return (List<A>) _Tags_tag;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.CatCatalogTag>();}
	}

	/**
	 * Sets the value for tags/tag.
	 * @param v Value to Set.
	 */
	public void setTags_tag(ItemI v) throws Exception{
		_Tags_tag =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/tags/tag",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/tags/tag",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * tags/tag
	 * Adds org.nrg.xdat.model.CatCatalogTagI
	 */
	public <A extends org.nrg.xdat.model.CatCatalogTagI> void addTags_tag(A item) throws Exception{
	setTags_tag((ItemI)item);
	}

	/**
	 * Removes the tags/tag of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeTags_tag(int index) throws java.lang.IndexOutOfBoundsException {
		_Tags_tag =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/tags/tag",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.CatCatalog> _Sets_entryset =null;

	/**
	 * sets/entrySet
	 * @return Returns an List of org.nrg.xdat.om.CatCatalog
	 */
	public <A extends org.nrg.xdat.model.CatCatalogI> List<A> getSets_entryset() {
		try{
			if (_Sets_entryset==null){
				_Sets_entryset=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("sets/entrySet"));
			}
			return (List<A>) _Sets_entryset;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.CatCatalog>();}
	}

	/**
	 * Sets the value for sets/entrySet.
	 * @param v Value to Set.
	 */
	public void setSets_entryset(ItemI v) throws Exception{
		_Sets_entryset =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sets/entrySet",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sets/entrySet",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * sets/entrySet
	 * Adds org.nrg.xdat.model.CatCatalogI
	 */
	public <A extends org.nrg.xdat.model.CatCatalogI> void addSets_entryset(A item) throws Exception{
	setSets_entryset((ItemI)item);
	}

	/**
	 * Removes the sets/entrySet of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSets_entryset(int index) throws java.lang.IndexOutOfBoundsException {
		_Sets_entryset =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/sets/entrySet",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.CatEntry> _Entries_entry =null;

	/**
	 * entries/entry
	 * @return Returns an List of org.nrg.xdat.om.CatEntry
	 */
	public <A extends org.nrg.xdat.model.CatEntryI> List<A> getEntries_entry() {
		try{
			if (_Entries_entry==null){
				_Entries_entry=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("entries/entry"));
			}
			return (List<A>) _Entries_entry;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.CatEntry>();}
	}

	/**
	 * Sets the value for entries/entry.
	 * @param v Value to Set.
	 */
	public void setEntries_entry(ItemI v) throws Exception{
		_Entries_entry =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/entries/entry",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/entries/entry",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * entries/entry
	 * Adds org.nrg.xdat.model.CatEntryI
	 */
	public <A extends org.nrg.xdat.model.CatEntryI> void addEntries_entry(A item) throws Exception{
	setEntries_entry((ItemI)item);
	}

	/**
	 * Removes the entries/entry of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeEntries_entry(int index) throws java.lang.IndexOutOfBoundsException {
		_Entries_entry =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/entries/entry",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Id=null;

	/**
	 * @return Returns the ID.
	 */
	public String getId(){
		try{
			if (_Id==null){
				_Id=getStringProperty("ID");
				return _Id;
			}else {
				return _Id;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	public void setId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ID",v);
		_Id=null;
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

	private String _Description=null;

	/**
	 * @return Returns the description.
	 */
	public String getDescription(){
		try{
			if (_Description==null){
				_Description=getStringProperty("description");
				return _Description;
			}else {
				return _Description;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for description.
	 * @param v Value to Set.
	 */
	public void setDescription(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/description",v);
		_Description=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _CatCatalogId=null;

	/**
	 * @return Returns the cat_catalog_id.
	 */
	public Integer getCatCatalogId() {
		try{
			if (_CatCatalogId==null){
				_CatCatalogId=getIntegerProperty("cat_catalog_id");
				return _CatCatalogId;
			}else {
				return _CatCatalogId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cat_catalog_id.
	 * @param v Value to Set.
	 */
	public void setCatCatalogId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cat_catalog_id",v);
		_CatCatalogId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.CatCatalog> getAllCatCatalogs(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatCatalog> al = new ArrayList<org.nrg.xdat.om.CatCatalog>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatCatalog> getCatCatalogsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatCatalog> al = new ArrayList<org.nrg.xdat.om.CatCatalog>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatCatalog> getCatCatalogsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatCatalog> al = new ArrayList<org.nrg.xdat.om.CatCatalog>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static CatCatalog getCatCatalogsByCatCatalogId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("cat:catalog/cat_catalog_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (CatCatalog) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //metaFields/metaField
	        for(org.nrg.xdat.model.CatCatalogMetafieldI childMetafields_metafield : this.getMetafields_metafield()){
	            if (childMetafields_metafield!=null){
	              for(ResourceFile rf: ((CatCatalogMetafield)childMetafields_metafield).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("metaFields/metaField[" + ((CatCatalogMetafield)childMetafields_metafield).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("metaFields/metaField/" + ((CatCatalogMetafield)childMetafields_metafield).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //tags/tag
	        for(org.nrg.xdat.model.CatCatalogTagI childTags_tag : this.getTags_tag()){
	            if (childTags_tag!=null){
	              for(ResourceFile rf: ((CatCatalogTag)childTags_tag).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("tags/tag[" + ((CatCatalogTag)childTags_tag).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("tags/tag/" + ((CatCatalogTag)childTags_tag).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //sets/entrySet
	        for(org.nrg.xdat.model.CatCatalogI childSets_entryset : this.getSets_entryset()){
	            if (childSets_entryset!=null){
	              for(ResourceFile rf: ((CatCatalog)childSets_entryset).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("sets/entrySet[" + ((CatCatalog)childSets_entryset).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("sets/entrySet/" + ((CatCatalog)childSets_entryset).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //entries/entry
	        for(org.nrg.xdat.model.CatEntryI childEntries_entry : this.getEntries_entry()){
	            if (childEntries_entry!=null){
	              for(ResourceFile rf: ((CatEntry)childEntries_entry).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("entries/entry[" + ((CatEntry)childEntries_entry).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("entries/entry/" + ((CatEntry)childEntries_entry).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        localLoop = preventLoop;
	
	return _return;
}
}
