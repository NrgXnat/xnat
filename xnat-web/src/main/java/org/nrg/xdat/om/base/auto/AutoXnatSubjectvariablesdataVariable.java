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
public abstract class AutoXnatSubjectvariablesdataVariable extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatSubjectvariablesdataVariableI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatSubjectvariablesdataVariable.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:subjectVariablesData_variable";

	public AutoXnatSubjectvariablesdataVariable(ItemI item)
	{
		super(item);
	}

	public AutoXnatSubjectvariablesdataVariable(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatSubjectvariablesdataVariable(UserI user)
	 **/
	public AutoXnatSubjectvariablesdataVariable(){}

	public AutoXnatSubjectvariablesdataVariable(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:subjectVariablesData_variable";
	}

	//FIELD

	private String _Variable=null;

	/**
	 * @return Returns the variable.
	 */
	public String getVariable(){
		try{
			if (_Variable==null){
				_Variable=getStringProperty("variable");
				return _Variable;
			}else {
				return _Variable;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for variable.
	 * @param v Value to Set.
	 */
	public void setVariable(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/variable",v);
		_Variable=null;
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

	private Integer _XnatSubjectvariablesdataVariableId=null;

	/**
	 * @return Returns the xnat_subjectVariablesData_variable_id.
	 */
	public Integer getXnatSubjectvariablesdataVariableId() {
		try{
			if (_XnatSubjectvariablesdataVariableId==null){
				_XnatSubjectvariablesdataVariableId=getIntegerProperty("xnat_subjectVariablesData_variable_id");
				return _XnatSubjectvariablesdataVariableId;
			}else {
				return _XnatSubjectvariablesdataVariableId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_subjectVariablesData_variable_id.
	 * @param v Value to Set.
	 */
	public void setXnatSubjectvariablesdataVariableId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_subjectVariablesData_variable_id",v);
		_XnatSubjectvariablesdataVariableId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable> getAllXnatSubjectvariablesdataVariables(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable> al = new ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable> getXnatSubjectvariablesdataVariablesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable> al = new ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable> getXnatSubjectvariablesdataVariablesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable> al = new ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatSubjectvariablesdataVariable getXnatSubjectvariablesdataVariablesByXnatSubjectvariablesdataVariableId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:subjectVariablesData_variable/xnat_subjectVariablesData_variable_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatSubjectvariablesdataVariable) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
