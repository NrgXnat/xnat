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
public abstract class AutoPipePipelinedetailsElement extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.PipePipelinedetailsElementI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoPipePipelinedetailsElement.class);
	public static String SCHEMA_ELEMENT_NAME="pipe:pipelineDetails_element";

	public AutoPipePipelinedetailsElement(ItemI item)
	{
		super(item);
	}

	public AutoPipePipelinedetailsElement(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoPipePipelinedetailsElement(UserI user)
	 **/
	public AutoPipePipelinedetailsElement(){}

	public AutoPipePipelinedetailsElement(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "pipe:pipelineDetails_element";
	}

	//FIELD

	private String _Element=null;

	/**
	 * @return Returns the element.
	 */
	public String getElement(){
		try{
			if (_Element==null){
				_Element=getStringProperty("element");
				return _Element;
			}else {
				return _Element;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for element.
	 * @param v Value to Set.
	 */
	public void setElement(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/element",v);
		_Element=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _PipePipelinedetailsElementId=null;

	/**
	 * @return Returns the pipe_pipelineDetails_element_id.
	 */
	public Integer getPipePipelinedetailsElementId() {
		try{
			if (_PipePipelinedetailsElementId==null){
				_PipePipelinedetailsElementId=getIntegerProperty("pipe_pipelineDetails_element_id");
				return _PipePipelinedetailsElementId;
			}else {
				return _PipePipelinedetailsElementId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pipe_pipelineDetails_element_id.
	 * @param v Value to Set.
	 */
	public void setPipePipelinedetailsElementId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pipe_pipelineDetails_element_id",v);
		_PipePipelinedetailsElementId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement> getAllPipePipelinedetailsElements(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement> al = new ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement> getPipePipelinedetailsElementsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement> al = new ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement> getPipePipelinedetailsElementsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement> al = new ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static PipePipelinedetailsElement getPipePipelinedetailsElementsByPipePipelinedetailsElementId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("pipe:pipelineDetails_element/pipe_pipelineDetails_element_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (PipePipelinedetailsElement) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
