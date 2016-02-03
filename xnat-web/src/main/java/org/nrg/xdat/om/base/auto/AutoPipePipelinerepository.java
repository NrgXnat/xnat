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
public abstract class AutoPipePipelinerepository extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.PipePipelinerepositoryI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoPipePipelinerepository.class);
	public static String SCHEMA_ELEMENT_NAME="pipe:PipelineRepository";

	public AutoPipePipelinerepository(ItemI item)
	{
		super(item);
	}

	public AutoPipePipelinerepository(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoPipePipelinerepository(UserI user)
	 **/
	public AutoPipePipelinerepository(){}

	public AutoPipePipelinerepository(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "pipe:PipelineRepository";
	}
	 private ArrayList<org.nrg.xdat.om.PipePipelinedetails> _Pipeline =null;

	/**
	 * pipeline
	 * @return Returns an List of org.nrg.xdat.om.PipePipelinedetails
	 */
	public <A extends org.nrg.xdat.model.PipePipelinedetailsI> List<A> getPipeline() {
		try{
			if (_Pipeline==null){
				_Pipeline=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("pipeline"));
			}
			return (List<A>) _Pipeline;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.PipePipelinedetails>();}
	}

	/**
	 * Sets the value for pipeline.
	 * @param v Value to Set.
	 */
	public void setPipeline(ItemI v) throws Exception{
		_Pipeline =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/pipeline",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/pipeline",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * pipeline
	 * Adds org.nrg.xdat.model.PipePipelinedetailsI
	 */
	public <A extends org.nrg.xdat.model.PipePipelinedetailsI> void addPipeline(A item) throws Exception{
	setPipeline((ItemI)item);
	}

	/**
	 * Removes the pipeline of the given index.
	 * @param index Index of child to remove.
	 */
	public void removePipeline(int index) throws java.lang.IndexOutOfBoundsException {
		_Pipeline =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/pipeline",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _PipePipelinerepositoryId=null;

	/**
	 * @return Returns the pipe_PipelineRepository_id.
	 */
	public Integer getPipePipelinerepositoryId() {
		try{
			if (_PipePipelinerepositoryId==null){
				_PipePipelinerepositoryId=getIntegerProperty("pipe_PipelineRepository_id");
				return _PipePipelinerepositoryId;
			}else {
				return _PipePipelinerepositoryId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pipe_PipelineRepository_id.
	 * @param v Value to Set.
	 */
	public void setPipePipelinerepositoryId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pipe_PipelineRepository_id",v);
		_PipePipelinerepositoryId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinerepository> getAllPipePipelinerepositorys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinerepository> al = new ArrayList<org.nrg.xdat.om.PipePipelinerepository>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinerepository> getPipePipelinerepositorysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinerepository> al = new ArrayList<org.nrg.xdat.om.PipePipelinerepository>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinerepository> getPipePipelinerepositorysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinerepository> al = new ArrayList<org.nrg.xdat.om.PipePipelinerepository>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static PipePipelinerepository getPipePipelinerepositorysByPipePipelinerepositoryId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("pipe:PipelineRepository/pipe_PipelineRepository_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (PipePipelinerepository) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //pipeline
	        for(org.nrg.xdat.model.PipePipelinedetailsI childPipeline : this.getPipeline()){
	            if (childPipeline!=null){
	              for(ResourceFile rf: ((PipePipelinedetails)childPipeline).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("pipeline[" + ((PipePipelinedetails)childPipeline).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("pipeline/" + ((PipePipelinedetails)childPipeline).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
