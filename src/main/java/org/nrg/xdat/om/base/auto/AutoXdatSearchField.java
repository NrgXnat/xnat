// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatSearchField;
import org.nrg.xdat.om.XdatSearchFieldI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
public abstract class AutoXdatSearchField extends org.nrg.xdat.base.BaseElement implements XdatSearchFieldI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatSearchField.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:search_field";

	public AutoXdatSearchField(ItemI item)
	{
		super(item);
	}

	public AutoXdatSearchField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatSearchField(UserI user)
	 **/
	public AutoXdatSearchField(){}

	public AutoXdatSearchField(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:search_field";
	}

	//FIELD

	private String _ElementName=null;

	/**
	 * @return Returns the element_name.
	 */
	public String getElementName(){
		try{
			if (_ElementName==null){
				_ElementName=getStringProperty("element_name");
				return _ElementName;
			}else {
				return _ElementName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for element_name.
	 * @param v Value to Set.
	 */
	public void setElementName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/element_name",v);
		_ElementName=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _FieldId=null;

	/**
	 * @return Returns the field_ID.
	 */
	public String getFieldId(){
		try{
			if (_FieldId==null){
				_FieldId=getStringProperty("field_ID");
				return _FieldId;
			}else {
				return _FieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for field_ID.
	 * @param v Value to Set.
	 */
	public void setFieldId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/field_ID",v);
		_FieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Sequence=null;

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence() {
		try{
			if (_Sequence==null){
				_Sequence=getIntegerProperty("sequence");
				return _Sequence;
			}else {
				return _Sequence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/sequence",v);
		_Sequence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Type=null;

	/**
	 * @return Returns the type.
	 */
	public String getType(){
		try{
			if (_Type==null){
				_Type=getStringProperty("type");
				return _Type;
			}else {
				return _Type;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	public void setType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/type",v);
		_Type=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Header=null;

	/**
	 * @return Returns the header.
	 */
	public String getHeader(){
		try{
			if (_Header==null){
				_Header=getStringProperty("header");
				return _Header;
			}else {
				return _Header;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for header.
	 * @param v Value to Set.
	 */
	public void setHeader(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/header",v);
		_Header=null;
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

	private Boolean _Visible=null;

	/**
	 * @return Returns the value.
	 */
	public Boolean getVisible(){
		try{
			if (_Visible==null){
				_Visible=getBooleanProperty("visible");
				return _Visible;
			}else {
				return _Visible;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for value.
	 * @param v Value to Set.
	 */
	public void setVisible(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/visible",v);
		_Visible=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatSearchFieldId=null;

	/**
	 * @return Returns the xdat_search_field_id.
	 */
	public Integer getXdatSearchFieldId() {
		try{
			if (_XdatSearchFieldId==null){
				_XdatSearchFieldId=getIntegerProperty("xdat_search_field_id");
				return _XdatSearchFieldId;
			}else {
				return _XdatSearchFieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_search_field_id.
	 * @param v Value to Set.
	 */
	public void setXdatSearchFieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_search_field_id",v);
		_XdatSearchFieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatSearchField> getAllXdatSearchFields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatSearchField> al = new ArrayList<org.nrg.xdat.om.XdatSearchField>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatSearchField> getXdatSearchFieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatSearchField> al = new ArrayList<org.nrg.xdat.om.XdatSearchField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatSearchField> getXdatSearchFieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatSearchField> al = new ArrayList<org.nrg.xdat.om.XdatSearchField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatSearchField getXdatSearchFieldsByXdatSearchFieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:search_field/xdat_search_field_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatSearchField) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
