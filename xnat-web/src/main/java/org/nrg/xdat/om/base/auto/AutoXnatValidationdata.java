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
public abstract class AutoXnatValidationdata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatValidationdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatValidationdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:validationData";

	public AutoXnatValidationdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatValidationdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatValidationdata(UserI user)
	 **/
	public AutoXnatValidationdata(){}

	public AutoXnatValidationdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:validationData";
	}

	//FIELD

	private String _Method=null;

	/**
	 * @return Returns the method.
	 */
	public String getMethod(){
		try{
			if (_Method==null){
				_Method=getStringProperty("method");
				return _Method;
			}else {
				return _Method;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for method.
	 * @param v Value to Set.
	 */
	public void setMethod(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/method",v);
		_Method=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Date=null;

	/**
	 * @return Returns the date.
	 */
	public Object getDate(){
		try{
			if (_Date==null){
				_Date=getProperty("date");
				return _Date;
			}else {
				return _Date;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for date.
	 * @param v Value to Set.
	 */
	public void setDate(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/date",v);
		_Date=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Notes=null;

	/**
	 * @return Returns the notes.
	 */
	public String getNotes(){
		try{
			if (_Notes==null){
				_Notes=getStringProperty("notes");
				return _Notes;
			}else {
				return _Notes;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for notes.
	 * @param v Value to Set.
	 */
	public void setNotes(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/notes",v);
		_Notes=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _ValidatedBy=null;

	/**
	 * @return Returns the validated_by.
	 */
	public String getValidatedBy(){
		try{
			if (_ValidatedBy==null){
				_ValidatedBy=getStringProperty("validated_by");
				return _ValidatedBy;
			}else {
				return _ValidatedBy;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for validated_by.
	 * @param v Value to Set.
	 */
	public void setValidatedBy(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/validated_by",v);
		_ValidatedBy=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Status=null;

	/**
	 * @return Returns the status.
	 */
	public String getStatus(){
		try{
			if (_Status==null){
				_Status=getStringProperty("status");
				return _Status;
			}else {
				return _Status;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for status.
	 * @param v Value to Set.
	 */
	public void setStatus(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/status",v);
		_Status=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatValidationdataId=null;

	/**
	 * @return Returns the xnat_validationData_id.
	 */
	public Integer getXnatValidationdataId() {
		try{
			if (_XnatValidationdataId==null){
				_XnatValidationdataId=getIntegerProperty("xnat_validationData_id");
				return _XnatValidationdataId;
			}else {
				return _XnatValidationdataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_validationData_id.
	 * @param v Value to Set.
	 */
	public void setXnatValidationdataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_validationData_id",v);
		_XnatValidationdataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatValidationdata> getAllXnatValidationdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatValidationdata> al = new ArrayList<org.nrg.xdat.om.XnatValidationdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatValidationdata> getXnatValidationdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatValidationdata> al = new ArrayList<org.nrg.xdat.om.XnatValidationdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatValidationdata> getXnatValidationdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatValidationdata> al = new ArrayList<org.nrg.xdat.om.XnatValidationdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatValidationdata getXnatValidationdatasByXnatValidationdataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:validationData/xnat_validationData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatValidationdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
