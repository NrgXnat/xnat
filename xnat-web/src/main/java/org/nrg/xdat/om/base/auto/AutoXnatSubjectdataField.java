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
public abstract class AutoXnatSubjectdataField extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatSubjectdataFieldI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatSubjectdataField.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:subjectData_field";

	public AutoXnatSubjectdataField(ItemI item)
	{
		super(item);
	}

	public AutoXnatSubjectdataField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatSubjectdataField(UserI user)
	 **/
	public AutoXnatSubjectdataField(){}

	public AutoXnatSubjectdataField(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:subjectData_field";
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

	private Integer _XnatSubjectdataFieldId=null;

	/**
	 * @return Returns the xnat_subjectData_field_id.
	 */
	public Integer getXnatSubjectdataFieldId() {
		try{
			if (_XnatSubjectdataFieldId==null){
				_XnatSubjectdataFieldId=getIntegerProperty("xnat_subjectData_field_id");
				return _XnatSubjectdataFieldId;
			}else {
				return _XnatSubjectdataFieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_subjectData_field_id.
	 * @param v Value to Set.
	 */
	public void setXnatSubjectdataFieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_subjectData_field_id",v);
		_XnatSubjectdataFieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectdataField> getAllXnatSubjectdataFields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectdataField> al = new ArrayList<org.nrg.xdat.om.XnatSubjectdataField>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectdataField> getXnatSubjectdataFieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectdataField> al = new ArrayList<org.nrg.xdat.om.XnatSubjectdataField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectdataField> getXnatSubjectdataFieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectdataField> al = new ArrayList<org.nrg.xdat.om.XnatSubjectdataField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatSubjectdataField getXnatSubjectdataFieldsByXnatSubjectdataFieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:subjectData_field/xnat_subjectData_field_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatSubjectdataField) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
