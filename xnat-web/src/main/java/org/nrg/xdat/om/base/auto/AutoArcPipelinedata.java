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
public abstract class AutoArcPipelinedata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ArcPipelinedataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcPipelinedata.class);
	public static String SCHEMA_ELEMENT_NAME="arc:pipelineData";

	public AutoArcPipelinedata(ItemI item)
	{
		super(item);
	}

	public AutoArcPipelinedata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcPipelinedata(UserI user)
	 **/
	public AutoArcPipelinedata(){}

	public AutoArcPipelinedata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:pipelineData";
	}

	//FIELD

	private String _Displaytext=null;

	/**
	 * @return Returns the displayText.
	 */
	public String getDisplaytext(){
		try{
			if (_Displaytext==null){
				_Displaytext=getStringProperty("displayText");
				return _Displaytext;
			}else {
				return _Displaytext;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for displayText.
	 * @param v Value to Set.
	 */
	public void setDisplaytext(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/displayText",v);
		_Displaytext=null;
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

	private String _Location=null;

	/**
	 * @return Returns the location.
	 */
	public String getLocation(){
		try{
			if (_Location==null){
				_Location=getStringProperty("location");
				return _Location;
			}else {
				return _Location;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for location.
	 * @param v Value to Set.
	 */
	public void setLocation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/location",v);
		_Location=null;
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
	 private ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata> _Parameters_parameter =null;

	/**
	 * parameters/parameter
	 * @return Returns an List of org.nrg.xdat.om.ArcPipelineparameterdata
	 */
	public <A extends org.nrg.xdat.model.ArcPipelineparameterdataI> List<A> getParameters_parameter() {
		try{
			if (_Parameters_parameter==null){
				_Parameters_parameter=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("parameters/parameter"));
			}
			return (List<A>) _Parameters_parameter;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata>();}
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
	 * Adds org.nrg.xdat.model.ArcPipelineparameterdataI
	 */
	public <A extends org.nrg.xdat.model.ArcPipelineparameterdataI> void addParameters_parameter(A item) throws Exception{
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

	//FIELD

	private Integer _ArcPipelinedataId=null;

	/**
	 * @return Returns the arc_pipelineData_id.
	 */
	public Integer getArcPipelinedataId() {
		try{
			if (_ArcPipelinedataId==null){
				_ArcPipelinedataId=getIntegerProperty("arc_pipelineData_id");
				return _ArcPipelinedataId;
			}else {
				return _ArcPipelinedataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc_pipelineData_id.
	 * @param v Value to Set.
	 */
	public void setArcPipelinedataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/arc_pipelineData_id",v);
		_ArcPipelinedataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcPipelinedata> getAllArcPipelinedatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcPipelinedata> al = new ArrayList<org.nrg.xdat.om.ArcPipelinedata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcPipelinedata> getArcPipelinedatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcPipelinedata> al = new ArrayList<org.nrg.xdat.om.ArcPipelinedata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcPipelinedata> getArcPipelinedatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcPipelinedata> al = new ArrayList<org.nrg.xdat.om.ArcPipelinedata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcPipelinedata getArcPipelinedatasByArcPipelinedataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:pipelineData/arc_pipelineData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcPipelinedata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //parameters/parameter
	        for(org.nrg.xdat.model.ArcPipelineparameterdataI childParameters_parameter : this.getParameters_parameter()){
	            if (childParameters_parameter!=null){
	              for(ResourceFile rf: ((ArcPipelineparameterdata)childParameters_parameter).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("parameters/parameter[" + ((ArcPipelineparameterdata)childParameters_parameter).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("parameters/parameter/" + ((ArcPipelineparameterdata)childParameters_parameter).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        localLoop = preventLoop;
	
	return _return;
}
}
