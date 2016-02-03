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
public abstract class AutoXnatAddfield extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatAddfieldI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAddfield.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:addField";

	public AutoXnatAddfield(ItemI item)
	{
		super(item);
	}

	public AutoXnatAddfield(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAddfield(UserI user)
	 **/
	public AutoXnatAddfield(){}

	public AutoXnatAddfield(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:addField";
	}

	//FIELD

	private String _Addfield=null;

	/**
	 * @return Returns the addField.
	 */
	public String getAddfield(){
		try{
			if (_Addfield==null){
				_Addfield=getStringProperty("addField");
				return _Addfield;
			}else {
				return _Addfield;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for addField.
	 * @param v Value to Set.
	 */
	public void setAddfield(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/addField",v);
		_Addfield=null;
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

	private Integer _XnatAddfieldId=null;

	/**
	 * @return Returns the xnat_addField_id.
	 */
	public Integer getXnatAddfieldId() {
		try{
			if (_XnatAddfieldId==null){
				_XnatAddfieldId=getIntegerProperty("xnat_addField_id");
				return _XnatAddfieldId;
			}else {
				return _XnatAddfieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_addField_id.
	 * @param v Value to Set.
	 */
	public void setXnatAddfieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_addField_id",v);
		_XnatAddfieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAddfield> getAllXnatAddfields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAddfield> al = new ArrayList<org.nrg.xdat.om.XnatAddfield>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAddfield> getXnatAddfieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAddfield> al = new ArrayList<org.nrg.xdat.om.XnatAddfield>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAddfield> getXnatAddfieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAddfield> al = new ArrayList<org.nrg.xdat.om.XnatAddfield>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAddfield getXnatAddfieldsByXnatAddfieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:addField/xnat_addField_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAddfield) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
