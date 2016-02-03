/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:10 UTC 2016
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
public abstract class AutoProvProcess extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ProvProcessI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoProvProcess.class);
	public static String SCHEMA_ELEMENT_NAME="prov:process";

	public AutoProvProcess(ItemI item)
	{
		super(item);
	}

	public AutoProvProcess(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoProvProcess(UserI user)
	 **/
	public AutoProvProcess(){}

	public AutoProvProcess(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "prov:process";
	}
	 private ArrayList<org.nrg.xdat.om.ProvProcessstep> _Processstep =null;

	/**
	 * processStep
	 * @return Returns an List of org.nrg.xdat.om.ProvProcessstep
	 */
	public <A extends org.nrg.xdat.model.ProvProcessstepI> List<A> getProcessstep() {
		try{
			if (_Processstep==null){
				_Processstep=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("processStep"));
			}
			return (List<A>) _Processstep;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ProvProcessstep>();}
	}

	/**
	 * Sets the value for processStep.
	 * @param v Value to Set.
	 */
	public void setProcessstep(ItemI v) throws Exception{
		_Processstep =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/processStep",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/processStep",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * processStep
	 * Adds org.nrg.xdat.model.ProvProcessstepI
	 */
	public <A extends org.nrg.xdat.model.ProvProcessstepI> void addProcessstep(A item) throws Exception{
	setProcessstep((ItemI)item);
	}

	/**
	 * Removes the processStep of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeProcessstep(int index) throws java.lang.IndexOutOfBoundsException {
		_Processstep =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/processStep",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ProvProcessId=null;

	/**
	 * @return Returns the prov_process_id.
	 */
	public Integer getProvProcessId() {
		try{
			if (_ProvProcessId==null){
				_ProvProcessId=getIntegerProperty("prov_process_id");
				return _ProvProcessId;
			}else {
				return _ProvProcessId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for prov_process_id.
	 * @param v Value to Set.
	 */
	public void setProvProcessId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/prov_process_id",v);
		_ProvProcessId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ProvProcess> getAllProvProcesss(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ProvProcess> al = new ArrayList<org.nrg.xdat.om.ProvProcess>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ProvProcess> getProvProcesssByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ProvProcess> al = new ArrayList<org.nrg.xdat.om.ProvProcess>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ProvProcess> getProvProcesssByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ProvProcess> al = new ArrayList<org.nrg.xdat.om.ProvProcess>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ProvProcess getProvProcesssByProvProcessId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("prov:process/prov_process_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ProvProcess) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //processStep
	        for(org.nrg.xdat.model.ProvProcessstepI childProcessstep : this.getProcessstep()){
	            if (childProcessstep!=null){
	              for(ResourceFile rf: ((ProvProcessstep)childProcessstep).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("processStep[" + ((ProvProcessstep)childProcessstep).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("processStep/" + ((ProvProcessstep)childProcessstep).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
