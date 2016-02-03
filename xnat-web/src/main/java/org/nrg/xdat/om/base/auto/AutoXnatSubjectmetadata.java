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
public abstract class AutoXnatSubjectmetadata extends XnatAbstractsubjectmetadata implements org.nrg.xdat.model.XnatSubjectmetadataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatSubjectmetadata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:subjectMetadata";

	public AutoXnatSubjectmetadata(ItemI item)
	{
		super(item);
	}

	public AutoXnatSubjectmetadata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatSubjectmetadata(UserI user)
	 **/
	public AutoXnatSubjectmetadata(){}

	public AutoXnatSubjectmetadata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:subjectMetadata";
	}
	 private org.nrg.xdat.om.XnatAbstractsubjectmetadata _Abstractsubjectmetadata =null;

	/**
	 * abstractSubjectMetadata
	 * @return org.nrg.xdat.om.XnatAbstractsubjectmetadata
	 */
	public org.nrg.xdat.om.XnatAbstractsubjectmetadata getAbstractsubjectmetadata() {
		try{
			if (_Abstractsubjectmetadata==null){
				_Abstractsubjectmetadata=((XnatAbstractsubjectmetadata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("abstractSubjectMetadata")));
				return _Abstractsubjectmetadata;
			}else {
				return _Abstractsubjectmetadata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for abstractSubjectMetadata.
	 * @param v Value to Set.
	 */
	public void setAbstractsubjectmetadata(ItemI v) throws Exception{
		_Abstractsubjectmetadata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractSubjectMetadata",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractSubjectMetadata",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * abstractSubjectMetadata
	 * set org.nrg.xdat.model.XnatAbstractsubjectmetadataI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractsubjectmetadataI> void setAbstractsubjectmetadata(A item) throws Exception{
	setAbstractsubjectmetadata((ItemI)item);
	}

	/**
	 * Removes the abstractSubjectMetadata.
	 * */
	public void removeAbstractsubjectmetadata() {
		_Abstractsubjectmetadata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/abstractSubjectMetadata",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Cohort=null;

	/**
	 * @return Returns the cohort.
	 */
	public String getCohort(){
		try{
			if (_Cohort==null){
				_Cohort=getStringProperty("cohort");
				return _Cohort;
			}else {
				return _Cohort;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cohort.
	 * @param v Value to Set.
	 */
	public void setCohort(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cohort",v);
		_Cohort=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectmetadata> getAllXnatSubjectmetadatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectmetadata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectmetadata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectmetadata> getXnatSubjectmetadatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectmetadata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectmetadata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectmetadata> getXnatSubjectmetadatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectmetadata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectmetadata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatSubjectmetadata getXnatSubjectmetadatasByXnatAbstractsubjectmetadataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:subjectMetadata/xnat_abstractsubjectmetadata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatSubjectmetadata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //abstractSubjectMetadata
	        XnatAbstractsubjectmetadata childAbstractsubjectmetadata = (XnatAbstractsubjectmetadata)this.getAbstractsubjectmetadata();
	            if (childAbstractsubjectmetadata!=null){
	              for(ResourceFile rf: ((XnatAbstractsubjectmetadata)childAbstractsubjectmetadata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("abstractSubjectMetadata[" + ((XnatAbstractsubjectmetadata)childAbstractsubjectmetadata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("abstractSubjectMetadata/" + ((XnatAbstractsubjectmetadata)childAbstractsubjectmetadata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
