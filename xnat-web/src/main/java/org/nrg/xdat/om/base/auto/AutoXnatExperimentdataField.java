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
public abstract class AutoXnatExperimentdataField extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatExperimentdataFieldI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatExperimentdataField.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:experimentData_field";

	public AutoXnatExperimentdataField(ItemI item)
	{
		super(item);
	}

	public AutoXnatExperimentdataField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatExperimentdataField(UserI user)
	 **/
	public AutoXnatExperimentdataField(){}

	public AutoXnatExperimentdataField(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:experimentData_field";
	}

	//FIELD

	private String _Field=null;

	/**
	 * @return Returns the field.
	 */
	public String getField(){
		try{
			if (_Field==null){
				_Field=getStringProperty("field");
				return _Field;
			}else {
				return _Field;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for field.
	 * @param v Value to Set.
	 */
	public void setField(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/field",v);
		_Field=null;
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

	private Integer _XnatExperimentdataFieldId=null;

	/**
	 * @return Returns the xnat_experimentData_field_id.
	 */
	public Integer getXnatExperimentdataFieldId() {
		try{
			if (_XnatExperimentdataFieldId==null){
				_XnatExperimentdataFieldId=getIntegerProperty("xnat_experimentData_field_id");
				return _XnatExperimentdataFieldId;
			}else {
				return _XnatExperimentdataFieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_experimentData_field_id.
	 * @param v Value to Set.
	 */
	public void setXnatExperimentdataFieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_experimentData_field_id",v);
		_XnatExperimentdataFieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatExperimentdataField> getAllXnatExperimentdataFields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatExperimentdataField> al = new ArrayList<org.nrg.xdat.om.XnatExperimentdataField>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatExperimentdataField> getXnatExperimentdataFieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatExperimentdataField> al = new ArrayList<org.nrg.xdat.om.XnatExperimentdataField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatExperimentdataField> getXnatExperimentdataFieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatExperimentdataField> al = new ArrayList<org.nrg.xdat.om.XnatExperimentdataField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatExperimentdataField getXnatExperimentdataFieldsByXnatExperimentdataFieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:experimentData_field/xnat_experimentData_field_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatExperimentdataField) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
