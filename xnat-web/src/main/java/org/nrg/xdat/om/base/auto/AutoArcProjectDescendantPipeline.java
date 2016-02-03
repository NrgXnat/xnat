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
public abstract class AutoArcProjectDescendantPipeline extends ArcPipelinedata implements org.nrg.xdat.model.ArcProjectDescendantPipelineI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcProjectDescendantPipeline.class);
	public static String SCHEMA_ELEMENT_NAME="arc:project_descendant_pipeline";

	public AutoArcProjectDescendantPipeline(ItemI item)
	{
		super(item);
	}

	public AutoArcProjectDescendantPipeline(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcProjectDescendantPipeline(UserI user)
	 **/
	public AutoArcProjectDescendantPipeline(){}

	public AutoArcProjectDescendantPipeline(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:project_descendant_pipeline";
	}
	 private org.nrg.xdat.om.ArcPipelinedata _Pipelinedata =null;

	/**
	 * pipelineData
	 * @return org.nrg.xdat.om.ArcPipelinedata
	 */
	public org.nrg.xdat.om.ArcPipelinedata getPipelinedata() {
		try{
			if (_Pipelinedata==null){
				_Pipelinedata=((ArcPipelinedata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("pipelineData")));
				return _Pipelinedata;
			}else {
				return _Pipelinedata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for pipelineData.
	 * @param v Value to Set.
	 */
	public void setPipelinedata(ItemI v) throws Exception{
		_Pipelinedata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/pipelineData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/pipelineData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * pipelineData
	 * set org.nrg.xdat.model.ArcPipelinedataI
	 */
	public <A extends org.nrg.xdat.model.ArcPipelinedataI> void setPipelinedata(A item) throws Exception{
	setPipelinedata((ItemI)item);
	}

	/**
	 * Removes the pipelineData.
	 * */
	public void removePipelinedata() {
		_Pipelinedata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/pipelineData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Stepid=null;

	/**
	 * @return Returns the stepId.
	 */
	public String getStepid(){
		try{
			if (_Stepid==null){
				_Stepid=getStringProperty("stepId");
				return _Stepid;
			}else {
				return _Stepid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for stepId.
	 * @param v Value to Set.
	 */
	public void setStepid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/stepId",v);
		_Stepid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Dependent=null;

	/**
	 * @return Returns the dependent.
	 */
	public Boolean getDependent() {
		try{
			if (_Dependent==null){
				_Dependent=getBooleanProperty("dependent");
				return _Dependent;
			}else {
				return _Dependent;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dependent.
	 * @param v Value to Set.
	 */
	public void setDependent(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/dependent",v);
		_Dependent=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline> getAllArcProjectDescendantPipelines(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline> al = new ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline> getArcProjectDescendantPipelinesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline> al = new ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline> getArcProjectDescendantPipelinesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline> al = new ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcProjectDescendantPipeline getArcProjectDescendantPipelinesByArcPipelinedataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:project_descendant_pipeline/arc_pipelinedata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcProjectDescendantPipeline) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //pipelineData
	        ArcPipelinedata childPipelinedata = (ArcPipelinedata)this.getPipelinedata();
	            if (childPipelinedata!=null){
	              for(ResourceFile rf: ((ArcPipelinedata)childPipelinedata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("pipelineData[" + ((ArcPipelinedata)childPipelinedata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("pipelineData/" + ((ArcPipelinedata)childPipelinedata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
