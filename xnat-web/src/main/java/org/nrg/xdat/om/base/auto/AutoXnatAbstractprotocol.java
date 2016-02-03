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
public abstract class AutoXnatAbstractprotocol extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatAbstractprotocolI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatAbstractprotocol.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:abstractProtocol";

	public AutoXnatAbstractprotocol(ItemI item)
	{
		super(item);
	}

	public AutoXnatAbstractprotocol(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatAbstractprotocol(UserI user)
	 **/
	public AutoXnatAbstractprotocol(){}

	public AutoXnatAbstractprotocol(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:abstractProtocol";
	}

	//FIELD

	private String _Id=null;

	/**
	 * @return Returns the ID.
	 */
	public String getId(){
		try{
			if (_Id==null){
				_Id=getStringProperty("ID");
				return _Id;
			}else {
				return _Id;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	public void setId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ID",v);
		_Id=null;
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

	private String _Description=null;

	/**
	 * @return Returns the description.
	 */
	public String getDescription(){
		try{
			if (_Description==null){
				_Description=getStringProperty("description");
				return _Description;
			}else {
				return _Description;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for description.
	 * @param v Value to Set.
	 */
	public void setDescription(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/description",v);
		_Description=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _DataType=null;

	/**
	 * @return Returns the data-type.
	 */
	public String getDataType(){
		try{
			if (_DataType==null){
				_DataType=getStringProperty("data-type");
				return _DataType;
			}else {
				return _DataType;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for data-type.
	 * @param v Value to Set.
	 */
	public void setDataType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/data-type",v);
		_DataType=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatAbstractprotocolId=null;

	/**
	 * @return Returns the xnat_abstractProtocol_id.
	 */
	public Integer getXnatAbstractprotocolId() {
		try{
			if (_XnatAbstractprotocolId==null){
				_XnatAbstractprotocolId=getIntegerProperty("xnat_abstractProtocol_id");
				return _XnatAbstractprotocolId;
			}else {
				return _XnatAbstractprotocolId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_abstractProtocol_id.
	 * @param v Value to Set.
	 */
	public void setXnatAbstractprotocolId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_abstractProtocol_id",v);
		_XnatAbstractprotocolId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractprotocol> getAllXnatAbstractprotocols(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractprotocol> al = new ArrayList<org.nrg.xdat.om.XnatAbstractprotocol>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractprotocol> getXnatAbstractprotocolsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractprotocol> al = new ArrayList<org.nrg.xdat.om.XnatAbstractprotocol>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatAbstractprotocol> getXnatAbstractprotocolsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatAbstractprotocol> al = new ArrayList<org.nrg.xdat.om.XnatAbstractprotocol>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatAbstractprotocol getXnatAbstractprotocolsByXnatAbstractprotocolId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:abstractProtocol/xnat_abstractProtocol_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAbstractprotocol) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XnatAbstractprotocol getXnatAbstractprotocolsById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:abstractProtocol/ID",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatAbstractprotocol) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        localLoop = preventLoop;
	
	return _return;
}
}
