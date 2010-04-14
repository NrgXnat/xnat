// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatElementAccessSecureIp;
import org.nrg.xdat.om.XdatElementAccessSecureIpI;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
public abstract class AutoXdatElementAccessSecureIp extends org.nrg.xdat.base.BaseElement implements XdatElementAccessSecureIpI{
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatElementAccessSecureIp.class);
	public static String SCHEMA_ELEMENT_NAME="xdat:element_access_secure_ip";

	public AutoXdatElementAccessSecureIp(ItemI item)
	{
		super(item);
	}

	public AutoXdatElementAccessSecureIp(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatElementAccessSecureIp(UserI user)
	 **/
	public AutoXdatElementAccessSecureIp(){}

	public AutoXdatElementAccessSecureIp(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:element_access_secure_ip";
	}

	//FIELD

	private String _SecureIp=null;

	/**
	 * @return Returns the secure_ip.
	 */
	public String getSecureIp(){
		try{
			if (_SecureIp==null){
				_SecureIp=getStringProperty("secure_ip");
				return _SecureIp;
			}else {
				return _SecureIp;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for secure_ip.
	 * @param v Value to Set.
	 */
	public void setSecureIp(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/secure_ip",v);
		_SecureIp=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XdatElementAccessSecureIpId=null;

	/**
	 * @return Returns the xdat_element_access_secure_ip_id.
	 */
	public Integer getXdatElementAccessSecureIpId() {
		try{
			if (_XdatElementAccessSecureIpId==null){
				_XdatElementAccessSecureIpId=getIntegerProperty("xdat_element_access_secure_ip_id");
				return _XdatElementAccessSecureIpId;
			}else {
				return _XdatElementAccessSecureIpId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xdat_element_access_secure_ip_id.
	 * @param v Value to Set.
	 */
	public void setXdatElementAccessSecureIpId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xdat_element_access_secure_ip_id",v);
		_XdatElementAccessSecureIpId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp> getAllXdatElementAccessSecureIps(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp> al = new ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp> getXdatElementAccessSecureIpsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp> al = new ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp> getXdatElementAccessSecureIpsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp> al = new ArrayList<org.nrg.xdat.om.XdatElementAccessSecureIp>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatElementAccessSecureIp getXdatElementAccessSecureIpsByXdatElementAccessSecureIpId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:element_access_secure_ip/xdat_element_access_secure_ip_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatElementAccessSecureIp) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
