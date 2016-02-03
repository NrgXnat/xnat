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
public abstract class AutoValProtocoldataCondition extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ValProtocoldataConditionI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoValProtocoldataCondition.class);
	public static String SCHEMA_ELEMENT_NAME="val:protocolData_condition";

	public AutoValProtocoldataCondition(ItemI item)
	{
		super(item);
	}

	public AutoValProtocoldataCondition(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoValProtocoldataCondition(UserI user)
	 **/
	public AutoValProtocoldataCondition(){}

	public AutoValProtocoldataCondition(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "val:protocolData_condition";
	}

	//FIELD

	private String _Verified=null;

	/**
	 * @return Returns the verified.
	 */
	public String getVerified(){
		try{
			if (_Verified==null){
				_Verified=getStringProperty("verified");
				return _Verified;
			}else {
				return _Verified;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for verified.
	 * @param v Value to Set.
	 */
	public void setVerified(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/verified",v);
		_Verified=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Diagnosis=null;

	/**
	 * @return Returns the diagnosis.
	 */
	public String getDiagnosis(){
		try{
			if (_Diagnosis==null){
				_Diagnosis=getStringProperty("diagnosis");
				return _Diagnosis;
			}else {
				return _Diagnosis;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for diagnosis.
	 * @param v Value to Set.
	 */
	public void setDiagnosis(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/diagnosis",v);
		_Diagnosis=null;
		} catch (Exception e1) {logger.error(e1);}
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

	private String _Xmlpath=null;

	/**
	 * @return Returns the xmlpath.
	 */
	public String getXmlpath(){
		try{
			if (_Xmlpath==null){
				_Xmlpath=getStringProperty("xmlpath");
				return _Xmlpath;
			}else {
				return _Xmlpath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xmlpath.
	 * @param v Value to Set.
	 */
	public void setXmlpath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xmlpath",v);
		_Xmlpath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ValProtocoldataConditionId=null;

	/**
	 * @return Returns the val_protocolData_condition_id.
	 */
	public Integer getValProtocoldataConditionId() {
		try{
			if (_ValProtocoldataConditionId==null){
				_ValProtocoldataConditionId=getIntegerProperty("val_protocolData_condition_id");
				return _ValProtocoldataConditionId;
			}else {
				return _ValProtocoldataConditionId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for val_protocolData_condition_id.
	 * @param v Value to Set.
	 */
	public void setValProtocoldataConditionId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/val_protocolData_condition_id",v);
		_ValProtocoldataConditionId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ValProtocoldataCondition> getAllValProtocoldataConditions(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValProtocoldataCondition> al = new ArrayList<org.nrg.xdat.om.ValProtocoldataCondition>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ValProtocoldataCondition> getValProtocoldataConditionsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValProtocoldataCondition> al = new ArrayList<org.nrg.xdat.om.ValProtocoldataCondition>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ValProtocoldataCondition> getValProtocoldataConditionsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValProtocoldataCondition> al = new ArrayList<org.nrg.xdat.om.ValProtocoldataCondition>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ValProtocoldataCondition getValProtocoldataConditionsByValProtocoldataConditionId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("val:protocolData_condition/val_protocolData_condition_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ValProtocoldataCondition) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
