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
public abstract class AutoArcProjectDescendant extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ArcProjectDescendantI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcProjectDescendant.class);
	public static String SCHEMA_ELEMENT_NAME="arc:project_descendant";

	public AutoArcProjectDescendant(ItemI item)
	{
		super(item);
	}

	public AutoArcProjectDescendant(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcProjectDescendant(UserI user)
	 **/
	public AutoArcProjectDescendant(){}

	public AutoArcProjectDescendant(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:project_descendant";
	}
	 private ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline> _Pipeline =null;

	/**
	 * pipeline
	 * @return Returns an List of org.nrg.xdat.om.ArcProjectDescendantPipeline
	 */
	public <A extends org.nrg.xdat.model.ArcProjectDescendantPipelineI> List<A> getPipeline() {
		try{
			if (_Pipeline==null){
				_Pipeline=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("pipeline"));
			}
			return (List<A>) _Pipeline;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ArcProjectDescendantPipeline>();}
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
	 * Adds org.nrg.xdat.model.ArcProjectDescendantPipelineI
	 */
	public <A extends org.nrg.xdat.model.ArcProjectDescendantPipelineI> void addPipeline(A item) throws Exception{
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

	private String _Xsitype=null;

	/**
	 * @return Returns the xsiType.
	 */
	public String getXsitype(){
		try{
			if (_Xsitype==null){
				_Xsitype=getStringProperty("xsiType");
				return _Xsitype;
			}else {
				return _Xsitype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xsiType.
	 * @param v Value to Set.
	 */
	public void setXsitype(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xsiType",v);
		_Xsitype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ArcProjectDescendantId=null;

	/**
	 * @return Returns the arc_project_descendant_id.
	 */
	public Integer getArcProjectDescendantId() {
		try{
			if (_ArcProjectDescendantId==null){
				_ArcProjectDescendantId=getIntegerProperty("arc_project_descendant_id");
				return _ArcProjectDescendantId;
			}else {
				return _ArcProjectDescendantId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc_project_descendant_id.
	 * @param v Value to Set.
	 */
	public void setArcProjectDescendantId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/arc_project_descendant_id",v);
		_ArcProjectDescendantId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcProjectDescendant> getAllArcProjectDescendants(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProjectDescendant> al = new ArrayList<org.nrg.xdat.om.ArcProjectDescendant>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcProjectDescendant> getArcProjectDescendantsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProjectDescendant> al = new ArrayList<org.nrg.xdat.om.ArcProjectDescendant>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcProjectDescendant> getArcProjectDescendantsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProjectDescendant> al = new ArrayList<org.nrg.xdat.om.ArcProjectDescendant>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcProjectDescendant getArcProjectDescendantsByArcProjectDescendantId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:project_descendant/arc_project_descendant_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcProjectDescendant) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	        for(org.nrg.xdat.model.ArcProjectDescendantPipelineI childPipeline : this.getPipeline()){
	            if (childPipeline!=null){
	              for(ResourceFile rf: ((ArcProjectDescendantPipeline)childPipeline).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("pipeline[" + ((ArcProjectDescendantPipeline)childPipeline).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("pipeline/" + ((ArcProjectDescendantPipeline)childPipeline).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
