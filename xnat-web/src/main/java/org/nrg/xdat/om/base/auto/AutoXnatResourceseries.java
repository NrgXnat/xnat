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
public abstract class AutoXnatResourceseries extends XnatAbstractresource implements org.nrg.xdat.model.XnatResourceseriesI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatResourceseries.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:resourceSeries";

	public AutoXnatResourceseries(ItemI item)
	{
		super(item);
	}

	public AutoXnatResourceseries(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatResourceseries(UserI user)
	 **/
	public AutoXnatResourceseries(){}

	public AutoXnatResourceseries(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:resourceSeries";
	}
	 private org.nrg.xdat.om.XnatAbstractresource _Abstractresource =null;

	/**
	 * abstractResource
	 * @return org.nrg.xdat.om.XnatAbstractresource
	 */
	public org.nrg.xdat.om.XnatAbstractresource getAbstractresource() {
		try{
			if (_Abstractresource==null){
				_Abstractresource=((XnatAbstractresource)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("abstractResource")));
				return _Abstractresource;
			}else {
				return _Abstractresource;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for abstractResource.
	 * @param v Value to Set.
	 */
	public void setAbstractresource(ItemI v) throws Exception{
		_Abstractresource =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractResource",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractResource",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * abstractResource
	 * set org.nrg.xdat.model.XnatAbstractresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> void setAbstractresource(A item) throws Exception{
	setAbstractresource((ItemI)item);
	}

	/**
	 * Removes the abstractResource.
	 * */
	public void removeAbstractresource() {
		_Abstractresource =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/abstractResource",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Path=null;

	/**
	 * @return Returns the path.
	 */
	public String getPath(){
		try{
			if (_Path==null){
				_Path=getStringProperty("path");
				return _Path;
			}else {
				return _Path;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for path.
	 * @param v Value to Set.
	 */
	public void setPath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/path",v);
		_Path=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Pattern=null;

	/**
	 * @return Returns the pattern.
	 */
	public String getPattern(){
		try{
			if (_Pattern==null){
				_Pattern=getStringProperty("pattern");
				return _Pattern;
			}else {
				return _Pattern;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pattern.
	 * @param v Value to Set.
	 */
	public void setPattern(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pattern",v);
		_Pattern=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Count=null;

	/**
	 * @return Returns the count.
	 */
	public Integer getCount() {
		try{
			if (_Count==null){
				_Count=getIntegerProperty("count");
				return _Count;
			}else {
				return _Count;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for count.
	 * @param v Value to Set.
	 */
	public void setCount(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/count",v);
		_Count=null;
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

	public static ArrayList<org.nrg.xdat.om.XnatResourceseries> getAllXnatResourceseriess(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatResourceseries> al = new ArrayList<org.nrg.xdat.om.XnatResourceseries>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatResourceseries> getXnatResourceseriessByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatResourceseries> al = new ArrayList<org.nrg.xdat.om.XnatResourceseries>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatResourceseries> getXnatResourceseriessByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatResourceseries> al = new ArrayList<org.nrg.xdat.om.XnatResourceseries>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatResourceseries getXnatResourceseriessByXnatAbstractresourceId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:resourceSeries/xnat_abstractresource_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatResourceseries) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	              int counter=0;
	              for(java.io.File f: this.getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("file/" + counter +"");
	                 rf.setXdatPath((counter++) +"");
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	return _return;
}
}
