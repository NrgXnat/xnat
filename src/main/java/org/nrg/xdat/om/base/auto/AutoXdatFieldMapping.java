// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatFieldMapping;
import org.nrg.xdat.om.XdatFieldMappingI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
public abstract class AutoXdatFieldMapping extends org.nrg.xdat.base.BaseElement implements XdatFieldMappingI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatFieldMapping.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:field_mapping";

	public AutoXdatFieldMapping(ItemI item)
	{
		super(item);
	}

	public AutoXdatFieldMapping(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatFieldMapping(UserI user)
	 **/
	public AutoXdatFieldMapping(){}

	public AutoXdatFieldMapping(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:field_mapping";
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

	private String _FieldValue=null;

	/**
	 * @return Returns the field_value.
	 */
	public String getFieldValue(){
		try{
			if (_FieldValue==null){
				_FieldValue=getStringProperty("field_value");
				return _FieldValue;
			}else {
				return _FieldValue;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for field_value.
	 * @param v Value to Set.
	 */
	public void setFieldValue(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/field_value",v);
		_FieldValue=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _CreateElement=null;

	/**
	 * @return Returns the create_element.
	 */
	public Boolean getCreateElement() {
		try{
			if (_CreateElement==null){
				_CreateElement=getBooleanProperty("create_element");
				return _CreateElement;
			}else {
				return _CreateElement;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for create_element.
	 * @param v Value to Set.
	 */
	public void setCreateElement(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/create_element",v);
		_CreateElement=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _ReadElement=null;

	/**
	 * @return Returns the read_element.
	 */
	public Boolean getReadElement() {
		try{
			if (_ReadElement==null){
				_ReadElement=getBooleanProperty("read_element");
				return _ReadElement;
			}else {
				return _ReadElement;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for read_element.
	 * @param v Value to Set.
	 */
	public void setReadElement(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/read_element",v);
		_ReadElement=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _EditElement=null;

	/**
	 * @return Returns the edit_element.
	 */
	public Boolean getEditElement() {
		try{
			if (_EditElement==null){
				_EditElement=getBooleanProperty("edit_element");
				return _EditElement;
			}else {
				return _EditElement;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for edit_element.
	 * @param v Value to Set.
	 */
	public void setEditElement(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/edit_element",v);
		_EditElement=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _DeleteElement=null;

	/**
	 * @return Returns the delete_element.
	 */
	public Boolean getDeleteElement() {
		try{
			if (_DeleteElement==null){
				_DeleteElement=getBooleanProperty("delete_element");
				return _DeleteElement;
			}else {
				return _DeleteElement;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for delete_element.
	 * @param v Value to Set.
	 */
	public void setDeleteElement(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/delete_element",v);
		_DeleteElement=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _ActiveElement=null;

	/**
	 * @return Returns the active_element.
	 */
	public Boolean getActiveElement() {
		try{
			if (_ActiveElement==null){
				_ActiveElement=getBooleanProperty("active_element");
				return _ActiveElement;
			}else {
				return _ActiveElement;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for active_element.
	 * @param v Value to Set.
	 */
	public void setActiveElement(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/active_element",v);
		_ActiveElement=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _ComparisonType=null;

	/**
	 * @return Returns the comparison_type.
	 */
	public String getComparisonType(){
		try{
			if (_ComparisonType==null){
				_ComparisonType=getStringProperty("comparison_type");
				return _ComparisonType;
			}else {
				return _ComparisonType;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for comparison_type.
	 * @param v Value to Set.
	 */
	public void setComparisonType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/comparison_type",v);
		_ComparisonType=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatFieldMappingId=null;

	/**
	 * @return Returns the xdat_field_mapping_id.
	 */
	public Integer getXdatFieldMappingId() {
		try{
			if (_XdatFieldMappingId==null){
				_XdatFieldMappingId=getIntegerProperty("xdat_field_mapping_id");
				return _XdatFieldMappingId;
			}else {
				return _XdatFieldMappingId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_field_mapping_id.
	 * @param v Value to Set.
	 */
	public void setXdatFieldMappingId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_field_mapping_id",v);
		_XdatFieldMappingId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatFieldMapping> getAllXdatFieldMappings(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatFieldMapping> al = new ArrayList<org.nrg.xdat.om.XdatFieldMapping>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatFieldMapping> getXdatFieldMappingsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatFieldMapping> al = new ArrayList<org.nrg.xdat.om.XdatFieldMapping>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatFieldMapping> getXdatFieldMappingsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatFieldMapping> al = new ArrayList<org.nrg.xdat.om.XdatFieldMapping>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatFieldMapping getXdatFieldMappingsByXdatFieldMappingId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:field_mapping/xdat_field_mapping_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatFieldMapping) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
