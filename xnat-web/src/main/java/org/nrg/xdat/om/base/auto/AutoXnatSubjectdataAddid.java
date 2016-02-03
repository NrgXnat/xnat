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
public abstract class AutoXnatSubjectdataAddid extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatSubjectdataAddidI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatSubjectdataAddid.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:subjectData_addID";

	public AutoXnatSubjectdataAddid(ItemI item)
	{
		super(item);
	}

	public AutoXnatSubjectdataAddid(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatSubjectdataAddid(UserI user)
	 **/
	public AutoXnatSubjectdataAddid(){}

	public AutoXnatSubjectdataAddid(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:subjectData_addID";
	}

	//FIELD

	private String _Addid=null;

	/**
	 * @return Returns the addID.
	 */
	public String getAddid(){
		try{
			if (_Addid==null){
				_Addid=getStringProperty("addID");
				return _Addid;
			}else {
				return _Addid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for addID.
	 * @param v Value to Set.
	 */
	public void setAddid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/addID",v);
		_Addid=null;
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

	private Integer _XnatSubjectdataAddidId=null;

	/**
	 * @return Returns the xnat_subjectData_addID_id.
	 */
	public Integer getXnatSubjectdataAddidId() {
		try{
			if (_XnatSubjectdataAddidId==null){
				_XnatSubjectdataAddidId=getIntegerProperty("xnat_subjectData_addID_id");
				return _XnatSubjectdataAddidId;
			}else {
				return _XnatSubjectdataAddidId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_subjectData_addID_id.
	 * @param v Value to Set.
	 */
	public void setXnatSubjectdataAddidId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_subjectData_addID_id",v);
		_XnatSubjectdataAddidId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid> getAllXnatSubjectdataAddids(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid> al = new ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid> getXnatSubjectdataAddidsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid> al = new ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid> getXnatSubjectdataAddidsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid> al = new ArrayList<org.nrg.xdat.om.XnatSubjectdataAddid>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatSubjectdataAddid getXnatSubjectdataAddidsByXnatSubjectdataAddidId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:subjectData_addID/xnat_subjectData_addID_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatSubjectdataAddid) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
