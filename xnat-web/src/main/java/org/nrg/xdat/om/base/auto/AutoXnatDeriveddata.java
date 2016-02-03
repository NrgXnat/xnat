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
public abstract class AutoXnatDeriveddata extends XnatExperimentdata implements org.nrg.xdat.model.XnatDeriveddataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatDeriveddata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:derivedData";

	public AutoXnatDeriveddata(ItemI item)
	{
		super(item);
	}

	public AutoXnatDeriveddata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatDeriveddata(UserI user)
	 **/
	public AutoXnatDeriveddata(){}

	public AutoXnatDeriveddata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:derivedData";
	}
	 private org.nrg.xdat.om.XnatExperimentdata _Experimentdata =null;

	/**
	 * experimentData
	 * @return org.nrg.xdat.om.XnatExperimentdata
	 */
	public org.nrg.xdat.om.XnatExperimentdata getExperimentdata() {
		try{
			if (_Experimentdata==null){
				_Experimentdata=((XnatExperimentdata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("experimentData")));
				return _Experimentdata;
			}else {
				return _Experimentdata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for experimentData.
	 * @param v Value to Set.
	 */
	public void setExperimentdata(ItemI v) throws Exception{
		_Experimentdata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/experimentData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/experimentData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * experimentData
	 * set org.nrg.xdat.model.XnatExperimentdataI
	 */
	public <A extends org.nrg.xdat.model.XnatExperimentdataI> void setExperimentdata(A item) throws Exception{
	setExperimentdata((ItemI)item);
	}

	/**
	 * Removes the experimentData.
	 * */
	public void removeExperimentdata() {
		_Experimentdata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/experimentData",0);
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
	 * @return Returns the xnat:derivedData/provenance_prov_process_id.
	 */
	public Integer getProvenanceFK(){
		try{
			if (_ProvenanceFK==null){
				_ProvenanceFK=getIntegerProperty("xnat:derivedData/provenance_prov_process_id");
				return _ProvenanceFK;
			}else {
				return _ProvenanceFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:derivedData/provenance_prov_process_id.
	 * @param v Value to Set.
	 */
	public void setProvenanceFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/provenance_prov_process_id",v);
		_ProvenanceFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatDeriveddata> getAllXnatDeriveddatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDeriveddata> al = new ArrayList<org.nrg.xdat.om.XnatDeriveddata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatDeriveddata> getXnatDeriveddatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDeriveddata> al = new ArrayList<org.nrg.xdat.om.XnatDeriveddata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatDeriveddata> getXnatDeriveddatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDeriveddata> al = new ArrayList<org.nrg.xdat.om.XnatDeriveddata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatDeriveddata getXnatDeriveddatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:derivedData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatDeriveddata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //experimentData
	        XnatExperimentdata childExperimentdata = (XnatExperimentdata)this.getExperimentdata();
	            if (childExperimentdata!=null){
	              for(ResourceFile rf: ((XnatExperimentdata)childExperimentdata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("experimentData[" + ((XnatExperimentdata)childExperimentdata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("experimentData/" + ((XnatExperimentdata)childExperimentdata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //provenance
	        ProvProcess childProvenance = (ProvProcess)this.getProvenance();
	            if (childProvenance!=null){
	              for(ResourceFile rf: ((ProvProcess)childProvenance).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("provenance[" + ((ProvProcess)childProvenance).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("provenance/" + ((ProvProcess)childProvenance).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
