// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatCriteria;
import org.nrg.xdat.om.XdatCriteriaI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatCriteria extends org.nrg.xdat.base.BaseElement implements XdatCriteriaI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatCriteria.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:criteria";

	public AutoXdatCriteria(ItemI item)
	{
		super(item);
	}

	public AutoXdatCriteria(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatCriteria(UserI user)
	 **/
	public AutoXdatCriteria(){}

	public AutoXdatCriteria(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:criteria";
	}

	//FIELD

	private String _SchemaField=null;

	/**
	 * @return Returns the schema_field.
	 */
	public String getSchemaField(){
		try{
			if (_SchemaField==null){
				_SchemaField=getStringProperty("schema_field");
				return _SchemaField;
			}else {
				return _SchemaField;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for schema_field.
	 * @param v Value to Set.
	 */
	public void setSchemaField(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/schema_field",v);
		_SchemaField=null;
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

	private String _CustomSearch=null;

	/**
	 * @return Returns the custom_search.
	 */
	public String getCustomSearch(){
		try{
			if (_CustomSearch==null){
				_CustomSearch=getStringProperty("custom_search");
				return _CustomSearch;
			}else {
				return _CustomSearch;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for custom_search.
	 * @param v Value to Set.
	 */
	public void setCustomSearch(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/custom_search",v);
		_CustomSearch=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Value=null;

	/**
	 * @return Returns the value.
	 */
	public String getValue(){
		try{
			if (_Value==null){
				_Value=getStringProperty("value");
				return _Value;
			}else {
				return _Value;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for value.
	 * @param v Value to Set.
	 */
	public void setValue(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/value",v);
		_Value=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _OverrideValueFormatting=null;

	/**
	 * @return Returns the override_value_formatting.
	 */
	public Boolean getOverrideValueFormatting() {
		try{
			if (_OverrideValueFormatting==null){
				_OverrideValueFormatting=getBooleanProperty("override_value_formatting");
				return _OverrideValueFormatting;
			}else {
				return _OverrideValueFormatting;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for override_value_formatting.
	 * @param v Value to Set.
	 */
	public void setOverrideValueFormatting(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/override_value_formatting",v);
		_OverrideValueFormatting=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatCriteriaId=null;

	/**
	 * @return Returns the xdat_criteria_id.
	 */
	public Integer getXdatCriteriaId() {
		try{
			if (_XdatCriteriaId==null){
				_XdatCriteriaId=getIntegerProperty("xdat_criteria_id");
				return _XdatCriteriaId;
			}else {
				return _XdatCriteriaId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_criteria_id.
	 * @param v Value to Set.
	 */
	public void setXdatCriteriaId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_criteria_id",v);
		_XdatCriteriaId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatCriteria> getAllXdatCriterias(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatCriteria> al = new ArrayList<org.nrg.xdat.om.XdatCriteria>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatCriteria> getXdatCriteriasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatCriteria> al = new ArrayList<org.nrg.xdat.om.XdatCriteria>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatCriteria> getXdatCriteriasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatCriteria> al = new ArrayList<org.nrg.xdat.om.XdatCriteria>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatCriteria getXdatCriteriasByXdatCriteriaId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:criteria/xdat_criteria_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatCriteria) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
