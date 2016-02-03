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
public abstract class AutoWrkXnatexecutionenvironmentParameter extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.WrkXnatexecutionenvironmentParameterI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoWrkXnatexecutionenvironmentParameter.class);
	public static String SCHEMA_ELEMENT_NAME="wrk:xnatExecutionEnvironment_parameter";

	public AutoWrkXnatexecutionenvironmentParameter(ItemI item)
	{
		super(item);
	}

	public AutoWrkXnatexecutionenvironmentParameter(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoWrkXnatexecutionenvironmentParameter(UserI user)
	 **/
	public AutoWrkXnatexecutionenvironmentParameter(){}

	public AutoWrkXnatexecutionenvironmentParameter(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "wrk:xnatExecutionEnvironment_parameter";
	}

	//FIELD

	private String _Parameter=null;

	/**
	 * @return Returns the parameter.
	 */
	public String getParameter(){
		try{
			if (_Parameter==null){
				_Parameter=getStringProperty("parameter");
				return _Parameter;
			}else {
				return _Parameter;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameter.
	 * @param v Value to Set.
	 */
	public void setParameter(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameter",v);
		_Parameter=null;
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

	private Integer _WrkXnatexecutionenvironmentParameterId=null;

	/**
	 * @return Returns the wrk_xnatExecutionEnvironment_parameter_id.
	 */
	public Integer getWrkXnatexecutionenvironmentParameterId() {
		try{
			if (_WrkXnatexecutionenvironmentParameterId==null){
				_WrkXnatexecutionenvironmentParameterId=getIntegerProperty("wrk_xnatExecutionEnvironment_parameter_id");
				return _WrkXnatexecutionenvironmentParameterId;
			}else {
				return _WrkXnatexecutionenvironmentParameterId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for wrk_xnatExecutionEnvironment_parameter_id.
	 * @param v Value to Set.
	 */
	public void setWrkXnatexecutionenvironmentParameterId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/wrk_xnatExecutionEnvironment_parameter_id",v);
		_WrkXnatexecutionenvironmentParameterId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter> getAllWrkXnatexecutionenvironmentParameters(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter> al = new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter> getWrkXnatexecutionenvironmentParametersByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter> al = new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter> getWrkXnatexecutionenvironmentParametersByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter> al = new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static WrkXnatexecutionenvironmentParameter getWrkXnatexecutionenvironmentParametersByWrkXnatexecutionenvironmentParameterId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("wrk:xnatExecutionEnvironment_parameter/wrk_xnatExecutionEnvironment_parameter_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (WrkXnatexecutionenvironmentParameter) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
