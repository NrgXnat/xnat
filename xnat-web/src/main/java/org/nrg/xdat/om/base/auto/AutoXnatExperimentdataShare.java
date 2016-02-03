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
public abstract class AutoXnatExperimentdataShare extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatExperimentdataShareI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatExperimentdataShare.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:experimentData_share";

	public AutoXnatExperimentdataShare(ItemI item)
	{
		super(item);
	}

	public AutoXnatExperimentdataShare(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatExperimentdataShare(UserI user)
	 **/
	public AutoXnatExperimentdataShare(){}

	public AutoXnatExperimentdataShare(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:experimentData_share";
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

	private String _Visit=null;

	/**
	 * @return Returns the visit.
	 */
	public String getVisit(){
		try{
			if (_Visit==null){
				_Visit=getStringProperty("visit");
				return _Visit;
			}else {
				return _Visit;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for visit.
	 * @param v Value to Set.
	 */
	public void setVisit(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/visit",v);
		_Visit=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Protocol=null;

	/**
	 * @return Returns the protocol.
	 */
	public String getProtocol(){
		try{
			if (_Protocol==null){
				_Protocol=getStringProperty("protocol");
				return _Protocol;
			}else {
				return _Protocol;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for protocol.
	 * @param v Value to Set.
	 */
	public void setProtocol(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/protocol",v);
		_Protocol=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatExperimentdataShareId=null;

	/**
	 * @return Returns the xnat_experimentData_share_id.
	 */
	public Integer getXnatExperimentdataShareId() {
		try{
			if (_XnatExperimentdataShareId==null){
				_XnatExperimentdataShareId=getIntegerProperty("xnat_experimentData_share_id");
				return _XnatExperimentdataShareId;
			}else {
				return _XnatExperimentdataShareId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_experimentData_share_id.
	 * @param v Value to Set.
	 */
	public void setXnatExperimentdataShareId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_experimentData_share_id",v);
		_XnatExperimentdataShareId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatExperimentdataShare> getAllXnatExperimentdataShares(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatExperimentdataShare> al = new ArrayList<org.nrg.xdat.om.XnatExperimentdataShare>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatExperimentdataShare> getXnatExperimentdataSharesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatExperimentdataShare> al = new ArrayList<org.nrg.xdat.om.XnatExperimentdataShare>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatExperimentdataShare> getXnatExperimentdataSharesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatExperimentdataShare> al = new ArrayList<org.nrg.xdat.om.XnatExperimentdataShare>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatExperimentdataShare getXnatExperimentdataSharesByXnatExperimentdataShareId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:experimentData_share/xnat_experimentData_share_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatExperimentdataShare) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
