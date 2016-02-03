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
public abstract class AutoXnatResource extends XnatAbstractresource implements org.nrg.xdat.model.XnatResourceI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatResource.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:resource";

	public AutoXnatResource(ItemI item)
	{
		super(item);
	}

	public AutoXnatResource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatResource(UserI user)
	 **/
	public AutoXnatResource(){}

	public AutoXnatResource(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:resource";
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
	 private org.nrg.xdat.om.ProvProcess _Provenance =null;

	/**
	 * provenance
	 * @return org.nrg.xdat.om.ProvProcess
	 */
	public org.nrg.xdat.om.ProvProcess getProvenance() {
		try{
			if (_Provenance==null){
				_Provenance=((ProvProcess)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("provenance")));
				return _Provenance;
			}else {
				return _Provenance;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for provenance.
	 * @param v Value to Set.
	 */
	public void setProvenance(ItemI v) throws Exception{
		_Provenance =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/provenance",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/provenance",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * provenance
	 * set org.nrg.xdat.model.ProvProcessI
	 */
	public <A extends org.nrg.xdat.model.ProvProcessI> void setProvenance(A item) throws Exception{
	setProvenance((ItemI)item);
	}

	/**
	 * Removes the provenance.
	 * */
	public void removeProvenance() {
		_Provenance =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/provenance",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ProvenanceFK=null;

	/**
	 * @return Returns the xnat:resource/provenance_prov_process_id.
	 */
	public Integer getProvenanceFK(){
		try{
			if (_ProvenanceFK==null){
				_ProvenanceFK=getIntegerProperty("xnat:resource/provenance_prov_process_id");
				return _ProvenanceFK;
			}else {
				return _ProvenanceFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:resource/provenance_prov_process_id.
	 * @param v Value to Set.
	 */
	public void setProvenanceFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/provenance_prov_process_id",v);
		_ProvenanceFK=null;
		} catch (Exception e1) {logger.error(e1);}
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

	public static ArrayList<org.nrg.xdat.om.XnatResource> getAllXnatResources(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatResource> al = new ArrayList<org.nrg.xdat.om.XnatResource>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatResource> getXnatResourcesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatResource> al = new ArrayList<org.nrg.xdat.om.XnatResource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatResource> getXnatResourcesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatResource> al = new ArrayList<org.nrg.xdat.om.XnatResource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatResource getXnatResourcesByXnatAbstractresourceId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:resource/xnat_abstractresource_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatResource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XnatResource getXnatResourcesByUri(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:resource/URI",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatResource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
