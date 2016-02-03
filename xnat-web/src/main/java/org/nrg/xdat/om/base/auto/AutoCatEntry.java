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
public abstract class AutoCatEntry extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.CatEntryI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoCatEntry.class);
	public static String SCHEMA_ELEMENT_NAME="cat:entry";

	public AutoCatEntry(ItemI item)
	{
		super(item);
	}

	public AutoCatEntry(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoCatEntry(UserI user)
	 **/
	public AutoCatEntry(){}

	public AutoCatEntry(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "cat:entry";
	}
	 private ArrayList<org.nrg.xdat.om.CatEntryMetafield> _Metafields_metafield =null;

	/**
	 * metaFields/metaField
	 * @return Returns an List of org.nrg.xdat.om.CatEntryMetafield
	 */
	public <A extends org.nrg.xdat.model.CatEntryMetafieldI> List<A> getMetafields_metafield() {
		try{
			if (_Metafields_metafield==null){
				_Metafields_metafield=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("metaFields/metaField"));
			}
			return (List<A>) _Metafields_metafield;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.CatEntryMetafield>();}
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
	 * Adds org.nrg.xdat.model.CatEntryMetafieldI
	 */
	public <A extends org.nrg.xdat.model.CatEntryMetafieldI> void addMetafields_metafield(A item) throws Exception{
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
	 private ArrayList<org.nrg.xdat.om.CatEntryTag> _Tags_tag =null;

	/**
	 * tags/tag
	 * @return Returns an List of org.nrg.xdat.om.CatEntryTag
	 */
	public <A extends org.nrg.xdat.model.CatEntryTagI> List<A> getTags_tag() {
		try{
			if (_Tags_tag==null){
				_Tags_tag=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("tags/tag"));
			}
			return (List<A>) _Tags_tag;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.CatEntryTag>();}
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
	 * Adds org.nrg.xdat.model.CatEntryTagI
	 */
	public <A extends org.nrg.xdat.model.CatEntryTagI> void addTags_tag(A item) throws Exception{
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

	//FIELD

	private String _Uri=null;

	/**
	 * @return Returns the URI.
	 */
	public String getUri(){
		try{
			if (_Uri==null){
				_Uri=getStringProperty("URI");
				return _Uri;
			}else {
				return _Uri;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for URI.
	 * @param v Value to Set.
	 */
	public void setUri(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/URI",v);
		_Uri=null;
		} catch (Exception e1) {logger.error(e1);}
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

	private String _Format=null;

	/**
	 * @return Returns the format.
	 */
	public String getFormat(){
		try{
			if (_Format==null){
				_Format=getStringProperty("format");
				return _Format;
			}else {
				return _Format;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for format.
	 * @param v Value to Set.
	 */
	public void setFormat(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/format",v);
		_Format=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Content=null;

	/**
	 * @return Returns the content.
	 */
	public String getContent(){
		try{
			if (_Content==null){
				_Content=getStringProperty("content");
				return _Content;
			}else {
				return _Content;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for content.
	 * @param v Value to Set.
	 */
	public void setContent(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/content",v);
		_Content=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Cachepath=null;

	/**
	 * @return Returns the cachePath.
	 */
	public String getCachepath(){
		try{
			if (_Cachepath==null){
				_Cachepath=getStringProperty("cachePath");
				return _Cachepath;
			}else {
				return _Cachepath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cachePath.
	 * @param v Value to Set.
	 */
	public void setCachepath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cachePath",v);
		_Cachepath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Createdtime=null;

	/**
	 * @return Returns the createdTime.
	 */
	public Object getCreatedtime(){
		try{
			if (_Createdtime==null){
				_Createdtime=getProperty("createdTime");
				return _Createdtime;
			}else {
				return _Createdtime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for createdTime.
	 * @param v Value to Set.
	 */
	public void setCreatedtime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/createdTime",v);
		_Createdtime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Createdby=null;

	/**
	 * @return Returns the createdBy.
	 */
	public String getCreatedby(){
		try{
			if (_Createdby==null){
				_Createdby=getStringProperty("createdBy");
				return _Createdby;
			}else {
				return _Createdby;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for createdBy.
	 * @param v Value to Set.
	 */
	public void setCreatedby(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/createdBy",v);
		_Createdby=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Createdeventid=null;

	/**
	 * @return Returns the createdEventId.
	 */
	public Integer getCreatedeventid() {
		try{
			if (_Createdeventid==null){
				_Createdeventid=getIntegerProperty("createdEventId");
				return _Createdeventid;
			}else {
				return _Createdeventid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for createdEventId.
	 * @param v Value to Set.
	 */
	public void setCreatedeventid(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/createdEventId",v);
		_Createdeventid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Modifiedtime=null;

	/**
	 * @return Returns the modifiedTime.
	 */
	public Object getModifiedtime(){
		try{
			if (_Modifiedtime==null){
				_Modifiedtime=getProperty("modifiedTime");
				return _Modifiedtime;
			}else {
				return _Modifiedtime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for modifiedTime.
	 * @param v Value to Set.
	 */
	public void setModifiedtime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/modifiedTime",v);
		_Modifiedtime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Modifiedby=null;

	/**
	 * @return Returns the modifiedBy.
	 */
	public String getModifiedby(){
		try{
			if (_Modifiedby==null){
				_Modifiedby=getStringProperty("modifiedBy");
				return _Modifiedby;
			}else {
				return _Modifiedby;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for modifiedBy.
	 * @param v Value to Set.
	 */
	public void setModifiedby(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/modifiedBy",v);
		_Modifiedby=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Modifiedeventid=null;

	/**
	 * @return Returns the modifiedEventId.
	 */
	public Integer getModifiedeventid() {
		try{
			if (_Modifiedeventid==null){
				_Modifiedeventid=getIntegerProperty("modifiedEventId");
				return _Modifiedeventid;
			}else {
				return _Modifiedeventid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for modifiedEventId.
	 * @param v Value to Set.
	 */
	public void setModifiedeventid(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/modifiedEventId",v);
		_Modifiedeventid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Digest=null;

	/**
	 * @return Returns the digest.
	 */
	public String getDigest(){
		try{
			if (_Digest==null){
				_Digest=getStringProperty("digest");
				return _Digest;
			}else {
				return _Digest;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for digest.
	 * @param v Value to Set.
	 */
	public void setDigest(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/digest",v);
		_Digest=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _CatEntryId=null;

	/**
	 * @return Returns the cat_entry_id.
	 */
	public Integer getCatEntryId() {
		try{
			if (_CatEntryId==null){
				_CatEntryId=getIntegerProperty("cat_entry_id");
				return _CatEntryId;
			}else {
				return _CatEntryId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cat_entry_id.
	 * @param v Value to Set.
	 */
	public void setCatEntryId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cat_entry_id",v);
		_CatEntryId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.CatEntry> getAllCatEntrys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatEntry> al = new ArrayList<org.nrg.xdat.om.CatEntry>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatEntry> getCatEntrysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatEntry> al = new ArrayList<org.nrg.xdat.om.CatEntry>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.CatEntry> getCatEntrysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.CatEntry> al = new ArrayList<org.nrg.xdat.om.CatEntry>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static CatEntry getCatEntrysByCatEntryId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("cat:entry/cat_entry_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (CatEntry) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	        for(org.nrg.xdat.model.CatEntryMetafieldI childMetafields_metafield : this.getMetafields_metafield()){
	            if (childMetafields_metafield!=null){
	              for(ResourceFile rf: ((CatEntryMetafield)childMetafields_metafield).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("metaFields/metaField[" + ((CatEntryMetafield)childMetafields_metafield).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("metaFields/metaField/" + ((CatEntryMetafield)childMetafields_metafield).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //tags/tag
	        for(org.nrg.xdat.model.CatEntryTagI childTags_tag : this.getTags_tag()){
	            if (childTags_tag!=null){
	              for(ResourceFile rf: ((CatEntryTag)childTags_tag).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("tags/tag[" + ((CatEntryTag)childTags_tag).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("tags/tag/" + ((CatEntryTag)childTags_tag).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        localLoop = preventLoop;
	
	return _return;
}
}
