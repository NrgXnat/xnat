/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:06 UTC 2016
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
public abstract class AutoXnatImagescandataShare extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatImagescandataShareI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatImagescandataShare.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:imageScanData_share";

	public AutoXnatImagescandataShare(ItemI item)
	{
		super(item);
	}

	public AutoXnatImagescandataShare(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatImagescandataShare(UserI user)
	 **/
	public AutoXnatImagescandataShare(){}

	public AutoXnatImagescandataShare(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:imageScanData_share";
	}

	//FIELD

	private String _Share=null;

	/**
	 * @return Returns the share.
	 */
	public String getShare(){
		try{
			if (_Share==null){
				_Share=getStringProperty("share");
				return _Share;
			}else {
				return _Share;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for share.
	 * @param v Value to Set.
	 */
	public void setShare(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/share",v);
		_Share=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Label=null;

	/**
	 * @return Returns the label.
	 */
	public String getLabel(){
		try{
			if (_Label==null){
				_Label=getStringProperty("label");
				return _Label;
			}else {
				return _Label;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for label.
	 * @param v Value to Set.
	 */
	public void setLabel(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/label",v);
		_Label=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Project=null;

	/**
	 * @return Returns the project.
	 */
	public String getProject(){
		try{
			if (_Project==null){
				_Project=getStringProperty("project");
				return _Project;
			}else {
				return _Project;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for project.
	 * @param v Value to Set.
	 */
	public void setProject(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/project",v);
		_Project=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatImagescandataShareId=null;

	/**
	 * @return Returns the xnat_imageScanData_share_id.
	 */
	public Integer getXnatImagescandataShareId() {
		try{
			if (_XnatImagescandataShareId==null){
				_XnatImagescandataShareId=getIntegerProperty("xnat_imageScanData_share_id");
				return _XnatImagescandataShareId;
			}else {
				return _XnatImagescandataShareId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_imageScanData_share_id.
	 * @param v Value to Set.
	 */
	public void setXnatImagescandataShareId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_imageScanData_share_id",v);
		_XnatImagescandataShareId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatImagescandataShare> getAllXnatImagescandataShares(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImagescandataShare> al = new ArrayList<org.nrg.xdat.om.XnatImagescandataShare>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImagescandataShare> getXnatImagescandataSharesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImagescandataShare> al = new ArrayList<org.nrg.xdat.om.XnatImagescandataShare>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImagescandataShare> getXnatImagescandataSharesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImagescandataShare> al = new ArrayList<org.nrg.xdat.om.XnatImagescandataShare>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatImagescandataShare getXnatImagescandataSharesByXnatImagescandataShareId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:imageScanData_share/xnat_imageScanData_share_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatImagescandataShare) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
