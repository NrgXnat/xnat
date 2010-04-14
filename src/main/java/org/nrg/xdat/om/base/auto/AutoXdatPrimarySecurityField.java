// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatPrimarySecurityField;
import org.nrg.xdat.om.XdatPrimarySecurityFieldI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
public abstract class AutoXdatPrimarySecurityField extends org.nrg.xdat.base.BaseElement implements XdatPrimarySecurityFieldI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatPrimarySecurityField.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:primary_security_field";

	public AutoXdatPrimarySecurityField(ItemI item)
	{
		super(item);
	}

	public AutoXdatPrimarySecurityField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatPrimarySecurityField(UserI user)
	 **/
	public AutoXdatPrimarySecurityField(){}

	public AutoXdatPrimarySecurityField(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:primary_security_field";
	}

	//FIELD

	private String _PrimarySecurityField=null;

	/**
	 * @return Returns the primary_security_field.
	 */
	public String getPrimarySecurityField(){
		try{
			if (_PrimarySecurityField==null){
				_PrimarySecurityField=getStringProperty("primary_security_field");
				return _PrimarySecurityField;
			}else {
				return _PrimarySecurityField;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for primary_security_field.
	 * @param v Value to Set.
	 */
	public void setPrimarySecurityField(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/primary_security_field",v);
		_PrimarySecurityField=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatPrimarySecurityFieldId=null;

	/**
	 * @return Returns the xdat_primary_security_field_id.
	 */
	public Integer getXdatPrimarySecurityFieldId() {
		try{
			if (_XdatPrimarySecurityFieldId==null){
				_XdatPrimarySecurityFieldId=getIntegerProperty("xdat_primary_security_field_id");
				return _XdatPrimarySecurityFieldId;
			}else {
				return _XdatPrimarySecurityFieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_primary_security_field_id.
	 * @param v Value to Set.
	 */
	public void setXdatPrimarySecurityFieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_primary_security_field_id",v);
		_XdatPrimarySecurityFieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField> getAllXdatPrimarySecurityFields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField> al = new ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField> getXdatPrimarySecurityFieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField> al = new ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField> getXdatPrimarySecurityFieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField> al = new ArrayList<org.nrg.xdat.om.XdatPrimarySecurityField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatPrimarySecurityField getXdatPrimarySecurityFieldsByXdatPrimarySecurityFieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:primary_security_field/xdat_primary_security_field_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatPrimarySecurityField) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
