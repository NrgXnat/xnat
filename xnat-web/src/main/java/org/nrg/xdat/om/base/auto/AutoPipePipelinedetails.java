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
public abstract class AutoPipePipelinedetails extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.PipePipelinedetailsI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoPipePipelinedetails.class);
	public static String SCHEMA_ELEMENT_NAME="pipe:pipelineDetails";

	public AutoPipePipelinedetails(ItemI item)
	{
		super(item);
	}

	public AutoPipePipelinedetails(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoPipePipelinedetails(UserI user)
	 **/
	public AutoPipePipelinedetails(){}

	public AutoPipePipelinedetails(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "pipe:pipelineDetails";
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
	 private ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement> _Generateselements_element =null;

	/**
	 * generatesElements/element
	 * @return Returns an List of org.nrg.xdat.om.PipePipelinedetailsElement
	 */
	public <A extends org.nrg.xdat.model.PipePipelinedetailsElementI> List<A> getGenerateselements_element() {
		try{
			if (_Generateselements_element==null){
				_Generateselements_element=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("generatesElements/element"));
			}
			return (List<A>) _Generateselements_element;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.PipePipelinedetailsElement>();}
	}

	/**
	 * Sets the value for generatesElements/element.
	 * @param v Value to Set.
	 */
	public void setGenerateselements_element(ItemI v) throws Exception{
		_Generateselements_element =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/generatesElements/element",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/generatesElements/element",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * generatesElements/element
	 * Adds org.nrg.xdat.model.PipePipelinedetailsElementI
	 */
	public <A extends org.nrg.xdat.model.PipePipelinedetailsElementI> void addGenerateselements_element(A item) throws Exception{
	setGenerateselements_element((ItemI)item);
	}

	/**
	 * Removes the generatesElements/element of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeGenerateselements_element(int index) throws java.lang.IndexOutOfBoundsException {
		_Generateselements_element =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/generatesElements/element",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Customwebpage=null;

	/**
	 * @return Returns the customwebpage.
	 */
	public String getCustomwebpage(){
		try{
			if (_Customwebpage==null){
				_Customwebpage=getStringProperty("customwebpage");
				return _Customwebpage;
			}else {
				return _Customwebpage;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for customwebpage.
	 * @param v Value to Set.
	 */
	public void setCustomwebpage(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/customwebpage",v);
		_Customwebpage=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter> _Parameters_parameter =null;

	/**
	 * parameters/parameter
	 * @return Returns an List of org.nrg.xdat.om.PipePipelinedetailsParameter
	 */
	public <A extends org.nrg.xdat.model.PipePipelinedetailsParameterI> List<A> getParameters_parameter() {
		try{
			if (_Parameters_parameter==null){
				_Parameters_parameter=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("parameters/parameter"));
			}
			return (List<A>) _Parameters_parameter;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter>();}
	}

	/**
	 * Sets the value for parameters/parameter.
	 * @param v Value to Set.
	 */
	public void setParameters_parameter(ItemI v) throws Exception{
		_Parameters_parameter =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/parameter",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/parameter",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * parameters/parameter
	 * Adds org.nrg.xdat.model.PipePipelinedetailsParameterI
	 */
	public <A extends org.nrg.xdat.model.PipePipelinedetailsParameterI> void addParameters_parameter(A item) throws Exception{
	setParameters_parameter((ItemI)item);
	}

	/**
	 * Removes the parameters/parameter of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeParameters_parameter(int index) throws java.lang.IndexOutOfBoundsException {
		_Parameters_parameter =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/parameters/parameter",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Appliesto=null;

	/**
	 * @return Returns the appliesTo.
	 */
	public String getAppliesto(){
		try{
			if (_Appliesto==null){
				_Appliesto=getStringProperty("appliesTo");
				return _Appliesto;
			}else {
				return _Appliesto;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for appliesTo.
	 * @param v Value to Set.
	 */
	public void setAppliesto(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/appliesTo",v);
		_Appliesto=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinedetails> getAllPipePipelinedetailss(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinedetails> al = new ArrayList<org.nrg.xdat.om.PipePipelinedetails>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinedetails> getPipePipelinedetailssByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinedetails> al = new ArrayList<org.nrg.xdat.om.PipePipelinedetails>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinedetails> getPipePipelinedetailssByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinedetails> al = new ArrayList<org.nrg.xdat.om.PipePipelinedetails>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static PipePipelinedetails getPipePipelinedetailssByPath(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("pipe:pipelineDetails/path",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (PipePipelinedetails) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //generatesElements/element
	        for(org.nrg.xdat.model.PipePipelinedetailsElementI childGenerateselements_element : this.getGenerateselements_element()){
	            if (childGenerateselements_element!=null){
	              for(ResourceFile rf: ((PipePipelinedetailsElement)childGenerateselements_element).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("generatesElements/element[" + ((PipePipelinedetailsElement)childGenerateselements_element).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("generatesElements/element/" + ((PipePipelinedetailsElement)childGenerateselements_element).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //parameters/parameter
	        for(org.nrg.xdat.model.PipePipelinedetailsParameterI childParameters_parameter : this.getParameters_parameter()){
	            if (childParameters_parameter!=null){
	              for(ResourceFile rf: ((PipePipelinedetailsParameter)childParameters_parameter).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("parameters/parameter[" + ((PipePipelinedetailsParameter)childParameters_parameter).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("parameters/parameter/" + ((PipePipelinedetailsParameter)childParameters_parameter).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
