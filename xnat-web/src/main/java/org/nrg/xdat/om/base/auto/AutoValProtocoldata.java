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
public abstract class AutoValProtocoldata extends XnatImageassessordata implements org.nrg.xdat.model.ValProtocoldataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoValProtocoldata.class);
	public static String SCHEMA_ELEMENT_NAME="val:protocolData";

	public AutoValProtocoldata(ItemI item)
	{
		super(item);
	}

	public AutoValProtocoldata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoValProtocoldata(UserI user)
	 **/
	public AutoValProtocoldata(){}

	public AutoValProtocoldata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "val:protocolData";
	}
	 private org.nrg.xdat.om.XnatImageassessordata _Imageassessordata =null;

	/**
	 * imageAssessorData
	 * @return org.nrg.xdat.om.XnatImageassessordata
	 */
	public org.nrg.xdat.om.XnatImageassessordata getImageassessordata() {
		try{
			if (_Imageassessordata==null){
				_Imageassessordata=((XnatImageassessordata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("imageAssessorData")));
				return _Imageassessordata;
			}else {
				return _Imageassessordata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for imageAssessorData.
	 * @param v Value to Set.
	 */
	public void setImageassessordata(ItemI v) throws Exception{
		_Imageassessordata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/imageAssessorData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/imageAssessorData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * imageAssessorData
	 * set org.nrg.xdat.model.XnatImageassessordataI
	 */
	public <A extends org.nrg.xdat.model.XnatImageassessordataI> void setImageassessordata(A item) throws Exception{
	setImageassessordata((ItemI)item);
	}

	/**
	 * Removes the imageAssessorData.
	 * */
	public void removeImageassessordata() {
		_Imageassessordata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/imageAssessorData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ValProtocoldataCondition> _Check_conditions_condition =null;

	/**
	 * check/conditions/condition
	 * @return Returns an List of org.nrg.xdat.om.ValProtocoldataCondition
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataConditionI> List<A> getCheck_conditions_condition() {
		try{
			if (_Check_conditions_condition==null){
				_Check_conditions_condition=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("check/conditions/condition"));
			}
			return (List<A>) _Check_conditions_condition;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ValProtocoldataCondition>();}
	}

	/**
	 * Sets the value for check/conditions/condition.
	 * @param v Value to Set.
	 */
	public void setCheck_conditions_condition(ItemI v) throws Exception{
		_Check_conditions_condition =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/check/conditions/condition",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/check/conditions/condition",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * check/conditions/condition
	 * Adds org.nrg.xdat.model.ValProtocoldataConditionI
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataConditionI> void addCheck_conditions_condition(A item) throws Exception{
	setCheck_conditions_condition((ItemI)item);
	}

	/**
	 * Removes the check/conditions/condition of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeCheck_conditions_condition(int index) throws java.lang.IndexOutOfBoundsException {
		_Check_conditions_condition =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/check/conditions/condition",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ValProtocoldataComment> _Check_comments_comment =null;

	/**
	 * check/comments/comment
	 * @return Returns an List of org.nrg.xdat.om.ValProtocoldataComment
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataCommentI> List<A> getCheck_comments_comment() {
		try{
			if (_Check_comments_comment==null){
				_Check_comments_comment=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("check/comments/comment"));
			}
			return (List<A>) _Check_comments_comment;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ValProtocoldataComment>();}
	}

	/**
	 * Sets the value for check/comments/comment.
	 * @param v Value to Set.
	 */
	public void setCheck_comments_comment(ItemI v) throws Exception{
		_Check_comments_comment =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/check/comments/comment",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/check/comments/comment",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * check/comments/comment
	 * Adds org.nrg.xdat.model.ValProtocoldataCommentI
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataCommentI> void addCheck_comments_comment(A item) throws Exception{
	setCheck_comments_comment((ItemI)item);
	}

	/**
	 * Removes the check/comments/comment of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeCheck_comments_comment(int index) throws java.lang.IndexOutOfBoundsException {
		_Check_comments_comment =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/check/comments/comment",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.ValAdditionalval _Check_additionalval =null;

	/**
	 * check/additionalVal
	 * @return org.nrg.xdat.om.ValAdditionalval
	 */
	public org.nrg.xdat.om.ValAdditionalval getCheck_additionalval() {
		try{
			if (_Check_additionalval==null){
				_Check_additionalval=((ValAdditionalval)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("check/additionalVal")));
				return _Check_additionalval;
			}else {
				return _Check_additionalval;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for check/additionalVal.
	 * @param v Value to Set.
	 */
	public void setCheck_additionalval(ItemI v) throws Exception{
		_Check_additionalval =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/check/additionalVal",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/check/additionalVal",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * check/additionalVal
	 * set org.nrg.xdat.model.ValAdditionalvalI
	 */
	public <A extends org.nrg.xdat.model.ValAdditionalvalI> void setCheck_additionalval(A item) throws Exception{
	setCheck_additionalval((ItemI)item);
	}

	/**
	 * Removes the check/additionalVal.
	 * */
	public void removeCheck_additionalval() {
		_Check_additionalval =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/check/additionalVal",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Check_additionalvalFK=null;

	/**
	 * @return Returns the val:protocolData/check_additionalval_val_additionalval_id.
	 */
	public Integer getCheck_additionalvalFK(){
		try{
			if (_Check_additionalvalFK==null){
				_Check_additionalvalFK=getIntegerProperty("val:protocolData/check_additionalval_val_additionalval_id");
				return _Check_additionalvalFK;
			}else {
				return _Check_additionalvalFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for val:protocolData/check_additionalval_val_additionalval_id.
	 * @param v Value to Set.
	 */
	public void setCheck_additionalvalFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/check_additionalval_val_additionalval_id",v);
		_Check_additionalvalFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Check_status=null;

	/**
	 * @return Returns the check/status.
	 */
	public String getCheck_status(){
		try{
			if (_Check_status==null){
				_Check_status=getStringProperty("check/status");
				return _Check_status;
			}else {
				return _Check_status;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for check/status.
	 * @param v Value to Set.
	 */
	public void setCheck_status(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/check/status",v);
		_Check_status=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck> _Scans_scanCheck =null;

	/**
	 * scans/scan_check
	 * @return Returns an List of org.nrg.xdat.om.ValProtocoldataScanCheck
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataScanCheckI> List<A> getScans_scanCheck() {
		try{
			if (_Scans_scanCheck==null){
				_Scans_scanCheck=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("scans/scan_check"));
			}
			return (List<A>) _Scans_scanCheck;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ValProtocoldataScanCheck>();}
	}

	/**
	 * Sets the value for scans/scan_check.
	 * @param v Value to Set.
	 */
	public void setScans_scanCheck(ItemI v) throws Exception{
		_Scans_scanCheck =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/scans/scan_check",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/scans/scan_check",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * scans/scan_check
	 * Adds org.nrg.xdat.model.ValProtocoldataScanCheckI
	 */
	public <A extends org.nrg.xdat.model.ValProtocoldataScanCheckI> void addScans_scanCheck(A item) throws Exception{
	setScans_scanCheck((ItemI)item);
	}

	/**
	 * Removes the scans/scan_check of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeScans_scanCheck(int index) throws java.lang.IndexOutOfBoundsException {
		_Scans_scanCheck =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/scans/scan_check",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ValProtocoldata> getAllValProtocoldatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValProtocoldata> al = new ArrayList<org.nrg.xdat.om.ValProtocoldata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ValProtocoldata> getValProtocoldatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValProtocoldata> al = new ArrayList<org.nrg.xdat.om.ValProtocoldata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ValProtocoldata> getValProtocoldatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ValProtocoldata> al = new ArrayList<org.nrg.xdat.om.ValProtocoldata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ValProtocoldata getValProtocoldatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("val:protocolData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ValProtocoldata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //imageAssessorData
	        XnatImageassessordata childImageassessordata = (XnatImageassessordata)this.getImageassessordata();
	            if (childImageassessordata!=null){
	              for(ResourceFile rf: ((XnatImageassessordata)childImageassessordata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("imageAssessorData[" + ((XnatImageassessordata)childImageassessordata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("imageAssessorData/" + ((XnatImageassessordata)childImageassessordata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //check/conditions/condition
	        for(org.nrg.xdat.model.ValProtocoldataConditionI childCheck_conditions_condition : this.getCheck_conditions_condition()){
	            if (childCheck_conditions_condition!=null){
	              for(ResourceFile rf: ((ValProtocoldataCondition)childCheck_conditions_condition).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("check/conditions/condition[" + ((ValProtocoldataCondition)childCheck_conditions_condition).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("check/conditions/condition/" + ((ValProtocoldataCondition)childCheck_conditions_condition).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //check/comments/comment
	        for(org.nrg.xdat.model.ValProtocoldataCommentI childCheck_comments_comment : this.getCheck_comments_comment()){
	            if (childCheck_comments_comment!=null){
	              for(ResourceFile rf: ((ValProtocoldataComment)childCheck_comments_comment).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("check/comments/comment[" + ((ValProtocoldataComment)childCheck_comments_comment).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("check/comments/comment/" + ((ValProtocoldataComment)childCheck_comments_comment).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //check/additionalVal
	        ValAdditionalval childCheck_additionalval = (ValAdditionalval)this.getCheck_additionalval();
	            if (childCheck_additionalval!=null){
	              for(ResourceFile rf: ((ValAdditionalval)childCheck_additionalval).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("check/additionalVal[" + ((ValAdditionalval)childCheck_additionalval).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("check/additionalVal/" + ((ValAdditionalval)childCheck_additionalval).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //scans/scan_check
	        for(org.nrg.xdat.model.ValProtocoldataScanCheckI childScans_scanCheck : this.getScans_scanCheck()){
	            if (childScans_scanCheck!=null){
	              for(ResourceFile rf: ((ValProtocoldataScanCheck)childScans_scanCheck).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("scans/scan_check[" + ((ValProtocoldataScanCheck)childScans_scanCheck).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("scans/scan_check/" + ((ValProtocoldataScanCheck)childScans_scanCheck).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
