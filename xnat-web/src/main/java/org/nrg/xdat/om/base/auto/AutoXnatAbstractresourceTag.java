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
public abstract class AutoXnatAbstractresourceTag extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatAbstractresourceTagI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAbstractresourceTag.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:abstractResource_tag";

	public AutoXnatAbstractresourceTag(ItemI item)
	{
		super(item);
	}

	public AutoXnatAbstractresourceTag(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAbstractresourceTag(UserI user)
	 **/
	public AutoXnatAbstractresourceTag(){}

	public AutoXnatAbstractresourceTag(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:abstractResource_tag";
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

	private Integer _XnatAbstractresourceTagId=null;

	/**
	 * @return Returns the xnat_abstractResource_tag_id.
	 */
	public Integer getXnatAbstractresourceTagId() {
		try{
			if (_XnatAbstractresourceTagId==null){
				_XnatAbstractresourceTagId=getIntegerProperty("xnat_abstractResource_tag_id");
				return _XnatAbstractresourceTagId;
			}else {
				return _XnatAbstractresourceTagId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_abstractResource_tag_id.
	 * @param v Value to Set.
	 */
	public void setXnatAbstractresourceTagId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_abstractResource_tag_id",v);
		_XnatAbstractresourceTagId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag> getAllXnatAbstractresourceTags(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag> al = new ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag> getXnatAbstractresourceTagsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag> al = new ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag> getXnatAbstractresourceTagsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag> al = new ArrayList<org.nrg.xdat.om.XnatAbstractresourceTag>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAbstractresourceTag getXnatAbstractresourceTagsByXnatAbstractresourceTagId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:abstractResource_tag/xnat_abstractResource_tag_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAbstractresourceTag) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
