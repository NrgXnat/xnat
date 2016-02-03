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
public abstract class AutoXnatProjectdataAlias extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatProjectdataAliasI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatProjectdataAlias.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:projectData_alias";

	public AutoXnatProjectdataAlias(ItemI item)
	{
		super(item);
	}

	public AutoXnatProjectdataAlias(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatProjectdataAlias(UserI user)
	 **/
	public AutoXnatProjectdataAlias(){}

	public AutoXnatProjectdataAlias(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:projectData_alias";
	}

	//FIELD

	private String _Alias=null;

	/**
	 * @return Returns the alias.
	 */
	public String getAlias(){
		try{
			if (_Alias==null){
				_Alias=getStringProperty("alias");
				return _Alias;
			}else {
				return _Alias;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for alias.
	 * @param v Value to Set.
	 */
	public void setAlias(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/alias",v);
		_Alias=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Source=null;

	/**
	 * @return Returns the source.
	 */
	public String getSource(){
		try{
			if (_Source==null){
				_Source=getStringProperty("source");
				return _Source;
			}else {
				return _Source;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for source.
	 * @param v Value to Set.
	 */
	public void setSource(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/source",v);
		_Source=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatProjectdataAliasId=null;

	/**
	 * @return Returns the xnat_projectData_alias_id.
	 */
	public Integer getXnatProjectdataAliasId() {
		try{
			if (_XnatProjectdataAliasId==null){
				_XnatProjectdataAliasId=getIntegerProperty("xnat_projectData_alias_id");
				return _XnatProjectdataAliasId;
			}else {
				return _XnatProjectdataAliasId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_projectData_alias_id.
	 * @param v Value to Set.
	 */
	public void setXnatProjectdataAliasId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_projectData_alias_id",v);
		_XnatProjectdataAliasId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectdataAlias> getAllXnatProjectdataAliass(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectdataAlias> al = new ArrayList<org.nrg.xdat.om.XnatProjectdataAlias>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectdataAlias> getXnatProjectdataAliassByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectdataAlias> al = new ArrayList<org.nrg.xdat.om.XnatProjectdataAlias>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatProjectdataAlias> getXnatProjectdataAliassByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatProjectdataAlias> al = new ArrayList<org.nrg.xdat.om.XnatProjectdataAlias>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatProjectdataAlias getXnatProjectdataAliassByXnatProjectdataAliasId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:projectData_alias/xnat_projectData_alias_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatProjectdataAlias) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
