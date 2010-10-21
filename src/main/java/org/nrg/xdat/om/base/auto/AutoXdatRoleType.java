// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Thu Oct 25 16:43:04 CDT 2007
 *
 */
package org.nrg.xdat.om.base.auto;
import java.util.ArrayList;
import java.util.Hashtable;

import org.nrg.xdat.om.XdatActionType;
import org.nrg.xdat.om.XdatRoleType;
import org.nrg.xdat.om.XdatRoleTypeI;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ResourceFile;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class AutoXdatRoleType extends org.nrg.xdat.base.BaseElement implements XdatRoleTypeI{
	public final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXdatRoleType.class);
	public final static String SCHEMA_ELEMENT_NAME="xdat:role_type";

	public AutoXdatRoleType(ItemI item)
	{
		super(item);
	}

	public AutoXdatRoleType(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXdatRoleType(UserI user)
	 **/
	public AutoXdatRoleType(){}

	public AutoXdatRoleType(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xdat:role_type";
	}
	 private ArrayList<org.nrg.xdat.om.XdatActionType> _AllowedActions_allowedAction =null;

	/**
	 * allowed_actions/allowed_action
	 * @return Returns an ArrayList of org.nrg.xdat.om.XdatActionType
	 */
	public ArrayList<org.nrg.xdat.om.XdatActionType> getAllowedActions_allowedAction() {
		try{
			if (_AllowedActions_allowedAction==null){
				_AllowedActions_allowedAction=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("allowed_actions/allowed_action"));
				return _AllowedActions_allowedAction;
			}else {
				return _AllowedActions_allowedAction;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XdatActionType>();}
	}

	/**
	 * Sets the value for allowed_actions/allowed_action.
	 * @param v Value to Set.
	 */
	public void setAllowedActions_allowedAction(ItemI v) throws Exception{
		_AllowedActions_allowedAction =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/allowed_actions/allowed_action",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/allowed_actions/allowed_action",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * Removes the allowed_actions/allowed_action of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeAllowedActions_allowedAction(int index) throws java.lang.IndexOutOfBoundsException {
		_AllowedActions_allowedAction =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/allowed_actions/allowed_action",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _RoleName=null;

	/**
	 * @return Returns the role_name.
	 */
	public String getRoleName(){
		try{
			if (_RoleName==null){
				_RoleName=getStringProperty("role_name");
				return _RoleName;
			}else {
				return _RoleName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for role_name.
	 * @param v Value to Set.
	 */
	public void setRoleName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/role_name",v);
		_RoleName=null;
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

	private Integer _Sequence=null;

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence() {
		try{
			if (_Sequence==null){
				_Sequence=getIntegerProperty("sequence");
				return _Sequence;
			}else {
				return _Sequence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/sequence",v);
		_Sequence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XdatRoleType> getAllXdatRoleTypes(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatRoleType> al = new ArrayList<org.nrg.xdat.om.XdatRoleType>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatRoleType> getXdatRoleTypesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatRoleType> al = new ArrayList<org.nrg.xdat.om.XdatRoleType>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XdatRoleType> getXdatRoleTypesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XdatRoleType> al = new ArrayList<org.nrg.xdat.om.XdatRoleType>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XdatRoleType getXdatRoleTypesByRoleName(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xdat:role_type/role_name",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XdatRoleType) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //allowed_actions/allowed_action
	        for(XdatActionType childAllowedActions_allowedAction : this.getAllowedActions_allowedAction()){
	            for(ResourceFile rf: childAllowedActions_allowedAction.getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("allowed_actions/allowed_action[" + childAllowedActions_allowedAction.getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("allowed_actions/allowed_action/" + childAllowedActions_allowedAction.getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
