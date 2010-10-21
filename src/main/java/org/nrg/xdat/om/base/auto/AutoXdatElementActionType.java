// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatElementActionType;
import org.nrg.xdat.om.XdatElementActionTypeI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatElementActionType extends org.nrg.xdat.base.BaseElement implements XdatElementActionTypeI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatElementActionType.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:element_action_type";

	public AutoXdatElementActionType(ItemI item)
	{
		super(item);
	}

	public AutoXdatElementActionType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatElementActionType(UserI user)
	 **/
	public AutoXdatElementActionType(){}

	public AutoXdatElementActionType(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:element_action_type";
	}

	//FIELD

	private String _ElementActionName=null;

	/**
	 * @return Returns the element_action_name.
	 */
	public String getElementActionName(){
		try{
			if (_ElementActionName==null){
				_ElementActionName=getStringProperty("element_action_name");
				return _ElementActionName;
			}else {
				return _ElementActionName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for element_action_name.
	 * @param v Value to Set.
	 */
	public void setElementActionName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/element_action_name",v);
		_ElementActionName=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _DisplayName=null;

	/**
	 * @return Returns the display_name.
	 */
	public String getDisplayName(){
		try{
			if (_DisplayName==null){
				_DisplayName=getStringProperty("display_name");
				return _DisplayName;
			}else {
				return _DisplayName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for display_name.
	 * @param v Value to Set.
	 */
	public void setDisplayName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/display_name",v);
		_DisplayName=null;
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

	private String _Image=null;

	/**
	 * @return Returns the image.
	 */
	public String getImage(){
		try{
			if (_Image==null){
				_Image=getStringProperty("image");
				return _Image;
			}else {
				return _Image;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for image.
	 * @param v Value to Set.
	 */
	public void setImage(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/image",v);
		_Image=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Popup=null;

	/**
	 * @return Returns the popup.
	 */
	public String getPopup(){
		try{
			if (_Popup==null){
				_Popup=getStringProperty("popup");
				return _Popup;
			}else {
				return _Popup;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for popup.
	 * @param v Value to Set.
	 */
	public void setPopup(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/popup",v);
		_Popup=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Secureaccess=null;

	/**
	 * @return Returns the secureAccess.
	 */
	public String getSecureaccess(){
		try{
			if (_Secureaccess==null){
				_Secureaccess=getStringProperty("secureAccess");
				return _Secureaccess;
			}else {
				return _Secureaccess;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secureAccess.
	 * @param v Value to Set.
	 */
	public void setSecureaccess(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/secureAccess",v);
		_Secureaccess=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameterstring=null;

	/**
	 * @return Returns the parameterString.
	 */
	public String getParameterstring(){
		try{
			if (_Parameterstring==null){
				_Parameterstring=getStringProperty("parameterString");
				return _Parameterstring;
			}else {
				return _Parameterstring;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameterString.
	 * @param v Value to Set.
	 */
	public void setParameterstring(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameterString",v);
		_Parameterstring=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatElementActionTypeId=null;

	/**
	 * @return Returns the xdat_element_action_type_id.
	 */
	public Integer getXdatElementActionTypeId() {
		try{
			if (_XdatElementActionTypeId==null){
				_XdatElementActionTypeId=getIntegerProperty("xdat_element_action_type_id");
				return _XdatElementActionTypeId;
			}else {
				return _XdatElementActionTypeId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_element_action_type_id.
	 * @param v Value to Set.
	 */
	public void setXdatElementActionTypeId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_element_action_type_id",v);
		_XdatElementActionTypeId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementActionType> getAllXdatElementActionTypes(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementActionType> al = new ArrayList<org.nrg.xdat.om.XdatElementActionType>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementActionType> getXdatElementActionTypesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementActionType> al = new ArrayList<org.nrg.xdat.om.XdatElementActionType>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementActionType> getXdatElementActionTypesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementActionType> al = new ArrayList<org.nrg.xdat.om.XdatElementActionType>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatElementActionType getXdatElementActionTypesByXdatElementActionTypeId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:element_action_type/xdat_element_action_type_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatElementActionType) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
