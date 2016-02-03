/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:05 UTC 2016
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
public abstract class AutoXnatAbstractresource extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatAbstractresourceI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAbstractresource.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:abstractResource";

	public AutoXnatAbstractresource(ItemI item)
	{
		super(item);
	}

	public AutoXnatAbstractresource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAbstractresource(UserI user)
	 **/
	public AutoXnatAbstractresource(){}

	public AutoXnatAbstractresource(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:abstractResource";
	}

	//FIELD

	private String _Note=null;

	/**
	 * @return Returns the note.
	 */
	public String getNote(){
		try{
			if (_Note==null){
				_Note=getStringProperty("note");
				return _Note;
			}else {
				return _Note;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for note.
	 * @param v Value to Set.
	 */
	public void setNote(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/note",v);
		_Note=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag> _Tags_tag =null;

	/**
	 * tags/tag
	 * @return Returns an List of org.nrg.xdat.om.XnatAbstractresourceTag
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceTagI> List<A> getTags_tag() {
		try{
			if (_Tags_tag==null){
				_Tags_tag=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("tags/tag"));
			}
			return (List<A>) _Tags_tag;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag>();}
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
	 * Adds org.nrg.xdat.model.XnatAbstractresourceTagI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceTagI> void addTags_tag(A item) throws Exception{
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

	private String _Label=null;

	/**
	 * @return Returns the label.
	 */
	public String getLabel(){
		try{
			if (_Label==null){
				_Label=getStringProperty("label");
				return _Label;
			}else {
				return _Label;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for label.
	 * @param v Value to Set.
	 */
	public void setLabel(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/label",v);
		_Label=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _FileCount=null;

	/**
	 * @return Returns the file_count.
	 */
	public Integer getFileCount() {
		try{
			if (_FileCount==null){
				_FileCount=getIntegerProperty("file_count");
				return _FileCount;
			}else {
				return _FileCount;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for file_count.
	 * @param v Value to Set.
	 */
	public void setFileCount(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/file_count",v);
		_FileCount=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _FileSize=null;

	/**
	 * @return Returns the file_size.
	 */
	public Object getFileSize(){
		try{
			if (_FileSize==null){
				_FileSize=getProperty("file_size");
				return _FileSize;
			}else {
				return _FileSize;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for file_size.
	 * @param v Value to Set.
	 */
	public void setFileSize(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/file_size",v);
		_FileSize=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatAbstractresourceId=null;

	/**
	 * @return Returns the xnat_abstractResource_id.
	 */
	public Integer getXnatAbstractresourceId() {
		try{
			if (_XnatAbstractresourceId==null){
				_XnatAbstractresourceId=getIntegerProperty("xnat_abstractResource_id");
				return _XnatAbstractresourceId;
			}else {
				return _XnatAbstractresourceId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_abstractResource_id.
	 * @param v Value to Set.
	 */
	public void setXnatAbstractresourceId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_abstractResource_id",v);
		_XnatAbstractresourceId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractresource> getAllXnatAbstractresources(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractresource> al = new ArrayList<org.nrg.xdat.om.XnatAbstractresource>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractresource> getXnatAbstractresourcesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractresource> al = new ArrayList<org.nrg.xdat.om.XnatAbstractresource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractresource> getXnatAbstractresourcesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractresource> al = new ArrayList<org.nrg.xdat.om.XnatAbstractresource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAbstractresource getXnatAbstractresourcesByXnatAbstractresourceId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:abstractResource/xnat_abstractResource_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAbstractresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	//ABSTRACT
	return _return;
}
}
