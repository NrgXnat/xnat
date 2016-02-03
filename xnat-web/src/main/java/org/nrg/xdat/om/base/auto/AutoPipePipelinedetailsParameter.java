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
public abstract class AutoPipePipelinedetailsParameter extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.PipePipelinedetailsParameterI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoPipePipelinedetailsParameter.class);
	public static String SCHEMA_ELEMENT_NAME="pipe:pipelineDetails_parameter";

	public AutoPipePipelinedetailsParameter(ItemI item)
	{
		super(item);
	}

	public AutoPipePipelinedetailsParameter(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoPipePipelinedetailsParameter(UserI user)
	 **/
	public AutoPipePipelinedetailsParameter(){}

	public AutoPipePipelinedetailsParameter(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "pipe:pipelineDetails_parameter";
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

	private String _Values_schemalink=null;

	/**
	 * @return Returns the values/schemaLink.
	 */
	public String getValues_schemalink(){
		try{
			if (_Values_schemalink==null){
				_Values_schemalink=getStringProperty("values/schemaLink");
				return _Values_schemalink;
			}else {
				return _Values_schemalink;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for values/schemaLink.
	 * @param v Value to Set.
	 */
	public void setValues_schemalink(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/values/schemaLink",v);
		_Values_schemalink=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Values_csvvalues=null;

	/**
	 * @return Returns the values/csvValues.
	 */
	public String getValues_csvvalues(){
		try{
			if (_Values_csvvalues==null){
				_Values_csvvalues=getStringProperty("values/csvValues");
				return _Values_csvvalues;
			}else {
				return _Values_csvvalues;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for values/csvValues.
	 * @param v Value to Set.
	 */
	public void setValues_csvvalues(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/values/csvValues",v);
		_Values_csvvalues=null;
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

	private Integer _PipePipelinedetailsParameterId=null;

	/**
	 * @return Returns the pipe_pipelineDetails_parameter_id.
	 */
	public Integer getPipePipelinedetailsParameterId() {
		try{
			if (_PipePipelinedetailsParameterId==null){
				_PipePipelinedetailsParameterId=getIntegerProperty("pipe_pipelineDetails_parameter_id");
				return _PipePipelinedetailsParameterId;
			}else {
				return _PipePipelinedetailsParameterId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pipe_pipelineDetails_parameter_id.
	 * @param v Value to Set.
	 */
	public void setPipePipelinedetailsParameterId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pipe_pipelineDetails_parameter_id",v);
		_PipePipelinedetailsParameterId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter> getAllPipePipelinedetailsParameters(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter> al = new ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter> getPipePipelinedetailsParametersByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter> al = new ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter> getPipePipelinedetailsParametersByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter> al = new ArrayList<org.nrg.xdat.om.PipePipelinedetailsParameter>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static PipePipelinedetailsParameter getPipePipelinedetailsParametersByPipePipelinedetailsParameterId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("pipe:pipelineDetails_parameter/pipe_pipelineDetails_parameter_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (PipePipelinedetailsParameter) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
