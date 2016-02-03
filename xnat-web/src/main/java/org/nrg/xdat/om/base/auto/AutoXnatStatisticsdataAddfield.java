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
public abstract class AutoXnatStatisticsdataAddfield extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatStatisticsdataAddfieldI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatStatisticsdataAddfield.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:statisticsData_addField";

	public AutoXnatStatisticsdataAddfield(ItemI item)
	{
		super(item);
	}

	public AutoXnatStatisticsdataAddfield(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatStatisticsdataAddfield(UserI user)
	 **/
	public AutoXnatStatisticsdataAddfield(){}

	public AutoXnatStatisticsdataAddfield(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:statisticsData_addField";
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

	private Integer _XnatStatisticsdataAddfieldId=null;

	/**
	 * @return Returns the xnat_statisticsData_addField_id.
	 */
	public Integer getXnatStatisticsdataAddfieldId() {
		try{
			if (_XnatStatisticsdataAddfieldId==null){
				_XnatStatisticsdataAddfieldId=getIntegerProperty("xnat_statisticsData_addField_id");
				return _XnatStatisticsdataAddfieldId;
			}else {
				return _XnatStatisticsdataAddfieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_statisticsData_addField_id.
	 * @param v Value to Set.
	 */
	public void setXnatStatisticsdataAddfieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_statisticsData_addField_id",v);
		_XnatStatisticsdataAddfieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield> getAllXnatStatisticsdataAddfields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield> al = new ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield> getXnatStatisticsdataAddfieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield> al = new ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield> getXnatStatisticsdataAddfieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield> al = new ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatStatisticsdataAddfield getXnatStatisticsdataAddfieldsByXnatStatisticsdataAddfieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:statisticsData_addField/xnat_statisticsData_addField_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatStatisticsdataAddfield) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
