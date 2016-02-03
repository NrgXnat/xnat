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
public abstract class AutoXnatProjectdataField extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatProjectdataFieldI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatProjectdataField.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:projectData_field";

	public AutoXnatProjectdataField(ItemI item)
	{
		super(item);
	}

	public AutoXnatProjectdataField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatProjectdataField(UserI user)
	 **/
	public AutoXnatProjectdataField(){}

	public AutoXnatProjectdataField(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:projectData_field";
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

	private Integer _XnatProjectdataFieldId=null;

	/**
	 * @return Returns the xnat_projectData_field_id.
	 */
	public Integer getXnatProjectdataFieldId() {
		try{
			if (_XnatProjectdataFieldId==null){
				_XnatProjectdataFieldId=getIntegerProperty("xnat_projectData_field_id");
				return _XnatProjectdataFieldId;
			}else {
				return _XnatProjectdataFieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_projectData_field_id.
	 * @param v Value to Set.
	 */
	public void setXnatProjectdataFieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_projectData_field_id",v);
		_XnatProjectdataFieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectdataField> getAllXnatProjectdataFields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectdataField> al = new ArrayList<org.nrg.xdat.om.XnatProjectdataField>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectdataField> getXnatProjectdataFieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectdataField> al = new ArrayList<org.nrg.xdat.om.XnatProjectdataField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectdataField> getXnatProjectdataFieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectdataField> al = new ArrayList<org.nrg.xdat.om.XnatProjectdataField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatProjectdataField getXnatProjectdataFieldsByXnatProjectdataFieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:projectData_field/xnat_projectData_field_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatProjectdataField) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
