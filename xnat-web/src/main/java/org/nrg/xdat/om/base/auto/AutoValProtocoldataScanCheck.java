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
public abstract class AutoValProtocoldataScanCheck extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ValProtocoldataScanCheckI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoValProtocoldataScanCheck.class);
	public static String SCHEMA_ELEMENT_NAME="val:protocolData_scan_check";

	public AutoValProtocoldataScanCheck(ItemI item)
	{
		super(item);
	}

	public AutoValProtocoldataScanCheck(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoValProtocoldataScanCheck(UserI user)
	 **/
	public AutoValProtocoldataScanCheck(){}

	public AutoValProtocoldataScanCheck(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "val:protocolData_scan_check";
	}
	 private ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheckCondition> _Conditions_condition =null;

	/**
	 * conditions/condition
	 * @return Returns an List of org.nrg.xdat.om.ValProtocoldataScanCheckCondition
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataScanCheckConditionI> List<A> getConditions_condition() {
		try{
			if (_Conditions_condition==null){
				_Conditions_condition=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("conditions/condition"));
			}
			return (List<A>) _Conditions_condition;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheckCondition>();}
	}

	/**
	 * Sets the value for conditions/condition.
	 * @param v Value to Set.
	 */
	public void setConditions_condition(ItemI v) throws Exception{
		_Conditions_condition =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/conditions/condition",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/conditions/condition",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * conditions/condition
	 * Adds org.nrg.xdat.model.ValProtocoldataScanCheckConditionI
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataScanCheckConditionI> void addConditions_condition(A item) throws Exception{
	setConditions_condition((ItemI)item);
	}

	/**
	 * Removes the conditions/condition of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeConditions_condition(int index) throws java.lang.IndexOutOfBoundsException {
		_Conditions_condition =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/conditions/condition",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheckComment> _Comments_comment =null;

	/**
	 * comments/comment
	 * @return Returns an List of org.nrg.xdat.om.ValProtocoldataScanCheckComment
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataScanCheckCommentI> List<A> getComments_comment() {
		try{
			if (_Comments_comment==null){
				_Comments_comment=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("comments/comment"));
			}
			return (List<A>) _Comments_comment;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheckComment>();}
	}

	/**
	 * Sets the value for comments/comment.
	 * @param v Value to Set.
	 */
	public void setComments_comment(ItemI v) throws Exception{
		_Comments_comment =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/comments/comment",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/comments/comment",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * comments/comment
	 * Adds org.nrg.xdat.model.ValProtocoldataScanCheckCommentI
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataScanCheckCommentI> void addComments_comment(A item) throws Exception{
	setComments_comment((ItemI)item);
	}

	/**
	 * Removes the comments/comment of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeComments_comment(int index) throws java.lang.IndexOutOfBoundsException {
		_Comments_comment =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/comments/comment",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.ValAdditionalval _Additionalval =null;

	/**
	 * additionalVal
	 * @return org.nrg.xdat.om.ValAdditionalval
	 */
	public org.nrg.xdat.om.ValAdditionalval getAdditionalval() {
		try{
			if (_Additionalval==null){
				_Additionalval=((ValAdditionalval)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("additionalVal")));
				return _Additionalval;
			}else {
				return _Additionalval;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for additionalVal.
	 * @param v Value to Set.
	 */
	public void setAdditionalval(ItemI v) throws Exception{
		_Additionalval =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/additionalVal",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/additionalVal",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * additionalVal
	 * set org.nrg.xdat.model.ValAdditionalvalI
	 */
	public <A extends org.nrg.xdat.model.ValAdditionalvalI> void setAdditionalval(A item) throws Exception{
	setAdditionalval((ItemI)item);
	}

	/**
	 * Removes the additionalVal.
	 * */
	public void removeAdditionalval() {
		_Additionalval =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/additionalVal",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _AdditionalvalFK=null;

	/**
	 * @return Returns the val:protocolData_scan_check/additionalval_val_additionalval_id.
	 */
	public Integer getAdditionalvalFK(){
		try{
			if (_AdditionalvalFK==null){
				_AdditionalvalFK=getIntegerProperty("val:protocolData_scan_check/additionalval_val_additionalval_id");
				return _AdditionalvalFK;
			}else {
				return _AdditionalvalFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for val:protocolData_scan_check/additionalval_val_additionalval_id.
	 * @param v Value to Set.
	 */
	public void setAdditionalvalFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/additionalval_val_additionalval_id",v);
		_AdditionalvalFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _ScanId=null;

	/**
	 * @return Returns the SCAN_ID.
	 */
	public String getScanId(){
		try{
			if (_ScanId==null){
				_ScanId=getStringProperty("SCAN_ID");
				return _ScanId;
			}else {
				return _ScanId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for SCAN_ID.
	 * @param v Value to Set.
	 */
	public void setScanId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/SCAN_ID",v);
		_ScanId=null;
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

	private Integer _ValProtocoldataScanCheckId=null;

	/**
	 * @return Returns the val_protocolData_scan_check_id.
	 */
	public Integer getValProtocoldataScanCheckId() {
		try{
			if (_ValProtocoldataScanCheckId==null){
				_ValProtocoldataScanCheckId=getIntegerProperty("val_protocolData_scan_check_id");
				return _ValProtocoldataScanCheckId;
			}else {
				return _ValProtocoldataScanCheckId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for val_protocolData_scan_check_id.
	 * @param v Value to Set.
	 */
	public void setValProtocoldataScanCheckId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/val_protocolData_scan_check_id",v);
		_ValProtocoldataScanCheckId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck> getAllValProtocoldataScanChecks(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck> al = new ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck> getValProtocoldataScanChecksByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck> al = new ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck> getValProtocoldataScanChecksByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck> al = new ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ValProtocoldataScanCheck getValProtocoldataScanChecksByValProtocoldataScanCheckId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("val:protocolData_scan_check/val_protocolData_scan_check_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ValProtocoldataScanCheck) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //conditions/condition
	        for(org.nrg.xdat.model.ValProtocoldataScanCheckConditionI childConditions_condition : this.getConditions_condition()){
	            if (childConditions_condition!=null){
	              for(ResourceFile rf: ((ValProtocoldataScanCheckCondition)childConditions_condition).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("conditions/condition[" + ((ValProtocoldataScanCheckCondition)childConditions_condition).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("conditions/condition/" + ((ValProtocoldataScanCheckCondition)childConditions_condition).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //comments/comment
	        for(org.nrg.xdat.model.ValProtocoldataScanCheckCommentI childComments_comment : this.getComments_comment()){
	            if (childComments_comment!=null){
	              for(ResourceFile rf: ((ValProtocoldataScanCheckComment)childComments_comment).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("comments/comment[" + ((ValProtocoldataScanCheckComment)childComments_comment).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("comments/comment/" + ((ValProtocoldataScanCheckComment)childComments_comment).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //additionalVal
	        ValAdditionalval childAdditionalval = (ValAdditionalval)this.getAdditionalval();
	            if (childAdditionalval!=null){
	              for(ResourceFile rf: ((ValAdditionalval)childAdditionalval).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("additionalVal[" + ((ValAdditionalval)childAdditionalval).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("additionalVal/" + ((ValAdditionalval)childAdditionalval).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
