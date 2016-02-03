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
public abstract class AutoXnatStudyprotocol extends XnatAbstractprotocol implements org.nrg.xdat.model.XnatStudyprotocolI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatStudyprotocol.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:studyProtocol";

	public AutoXnatStudyprotocol(ItemI item)
	{
		super(item);
	}

	public AutoXnatStudyprotocol(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatStudyprotocol(UserI user)
	 **/
	public AutoXnatStudyprotocol(){}

	public AutoXnatStudyprotocol(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:studyProtocol";
	}
	 private org.nrg.xdat.om.XnatAbstractprotocol _Abstractprotocol =null;

	/**
	 * abstractProtocol
	 * @return org.nrg.xdat.om.XnatAbstractprotocol
	 */
	public org.nrg.xdat.om.XnatAbstractprotocol getAbstractprotocol() {
		try{
			if (_Abstractprotocol==null){
				_Abstractprotocol=((XnatAbstractprotocol)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("abstractProtocol")));
				return _Abstractprotocol;
			}else {
				return _Abstractprotocol;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for abstractProtocol.
	 * @param v Value to Set.
	 */
	public void setAbstractprotocol(ItemI v) throws Exception{
		_Abstractprotocol =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractProtocol",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractProtocol",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * abstractProtocol
	 * set org.nrg.xdat.model.XnatAbstractprotocolI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractprotocolI> void setAbstractprotocol(A item) throws Exception{
	setAbstractprotocol((ItemI)item);
	}

	/**
	 * Removes the abstractProtocol.
	 * */
	public void removeAbstractprotocol() {
		_Abstractprotocol =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/abstractProtocol",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatStudyprotocolCondition> _Acqconditions_condition =null;

	/**
	 * acqConditions/condition
	 * @return Returns an List of org.nrg.xdat.om.XnatStudyprotocolCondition
	 */
	public <A extends org.nrg.xdat.model.XnatStudyprotocolConditionI> List<A> getAcqconditions_condition() {
		try{
			if (_Acqconditions_condition==null){
				_Acqconditions_condition=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("acqConditions/condition"));
			}
			return (List<A>) _Acqconditions_condition;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatStudyprotocolCondition>();}
	}

	/**
	 * Sets the value for acqConditions/condition.
	 * @param v Value to Set.
	 */
	public void setAcqconditions_condition(ItemI v) throws Exception{
		_Acqconditions_condition =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/acqConditions/condition",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/acqConditions/condition",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * acqConditions/condition
	 * Adds org.nrg.xdat.model.XnatStudyprotocolConditionI
	 */
	public <A extends org.nrg.xdat.model.XnatStudyprotocolConditionI> void addAcqconditions_condition(A item) throws Exception{
	setAcqconditions_condition((ItemI)item);
	}

	/**
	 * Removes the acqConditions/condition of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeAcqconditions_condition(int index) throws java.lang.IndexOutOfBoundsException {
		_Acqconditions_condition =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/acqConditions/condition",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatStudyprotocolGroup> _Subjectgroups_group =null;

	/**
	 * subjectGroups/group
	 * @return Returns an List of org.nrg.xdat.om.XnatStudyprotocolGroup
	 */
	public <A extends org.nrg.xdat.model.XnatStudyprotocolGroupI> List<A> getSubjectgroups_group() {
		try{
			if (_Subjectgroups_group==null){
				_Subjectgroups_group=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("subjectGroups/group"));
			}
			return (List<A>) _Subjectgroups_group;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatStudyprotocolGroup>();}
	}

	/**
	 * Sets the value for subjectGroups/group.
	 * @param v Value to Set.
	 */
	public void setSubjectgroups_group(ItemI v) throws Exception{
		_Subjectgroups_group =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectGroups/group",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectGroups/group",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * subjectGroups/group
	 * Adds org.nrg.xdat.model.XnatStudyprotocolGroupI
	 */
	public <A extends org.nrg.xdat.model.XnatStudyprotocolGroupI> void addSubjectgroups_group(A item) throws Exception{
	setSubjectgroups_group((ItemI)item);
	}

	/**
	 * Removes the subjectGroups/group of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSubjectgroups_group(int index) throws java.lang.IndexOutOfBoundsException {
		_Subjectgroups_group =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/subjectGroups/group",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatStudyprotocolVariable> _Subjectvariables_variable =null;

	/**
	 * subjectVariables/variable
	 * @return Returns an List of org.nrg.xdat.om.XnatStudyprotocolVariable
	 */
	public <A extends org.nrg.xdat.model.XnatStudyprotocolVariableI> List<A> getSubjectvariables_variable() {
		try{
			if (_Subjectvariables_variable==null){
				_Subjectvariables_variable=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("subjectVariables/variable"));
			}
			return (List<A>) _Subjectvariables_variable;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatStudyprotocolVariable>();}
	}

	/**
	 * Sets the value for subjectVariables/variable.
	 * @param v Value to Set.
	 */
	public void setSubjectvariables_variable(ItemI v) throws Exception{
		_Subjectvariables_variable =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectVariables/variable",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectVariables/variable",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * subjectVariables/variable
	 * Adds org.nrg.xdat.model.XnatStudyprotocolVariableI
	 */
	public <A extends org.nrg.xdat.model.XnatStudyprotocolVariableI> void addSubjectvariables_variable(A item) throws Exception{
	setSubjectvariables_variable((ItemI)item);
	}

	/**
	 * Removes the subjectVariables/variable of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSubjectvariables_variable(int index) throws java.lang.IndexOutOfBoundsException {
		_Subjectvariables_variable =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/subjectVariables/variable",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatStudyprotocolSession> _Imagesessiontypes_session =null;

	/**
	 * imageSessionTypes/session
	 * @return Returns an List of org.nrg.xdat.om.XnatStudyprotocolSession
	 */
	public <A extends org.nrg.xdat.model.XnatStudyprotocolSessionI> List<A> getImagesessiontypes_session() {
		try{
			if (_Imagesessiontypes_session==null){
				_Imagesessiontypes_session=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("imageSessionTypes/session"));
			}
			return (List<A>) _Imagesessiontypes_session;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatStudyprotocolSession>();}
	}

	/**
	 * Sets the value for imageSessionTypes/session.
	 * @param v Value to Set.
	 */
	public void setImagesessiontypes_session(ItemI v) throws Exception{
		_Imagesessiontypes_session =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/imageSessionTypes/session",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/imageSessionTypes/session",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * imageSessionTypes/session
	 * Adds org.nrg.xdat.model.XnatStudyprotocolSessionI
	 */
	public <A extends org.nrg.xdat.model.XnatStudyprotocolSessionI> void addImagesessiontypes_session(A item) throws Exception{
	setImagesessiontypes_session((ItemI)item);
	}

	/**
	 * Removes the imageSessionTypes/session of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeImagesessiontypes_session(int index) throws java.lang.IndexOutOfBoundsException {
		_Imagesessiontypes_session =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/imageSessionTypes/session",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatStudyprotocol> getAllXnatStudyprotocols(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStudyprotocol> al = new ArrayList<org.nrg.xdat.om.XnatStudyprotocol>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatStudyprotocol> getXnatStudyprotocolsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStudyprotocol> al = new ArrayList<org.nrg.xdat.om.XnatStudyprotocol>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatStudyprotocol> getXnatStudyprotocolsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStudyprotocol> al = new ArrayList<org.nrg.xdat.om.XnatStudyprotocol>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatStudyprotocol getXnatStudyprotocolsByXnatAbstractprotocolId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:studyProtocol/xnat_abstractprotocol_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatStudyprotocol) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //abstractProtocol
	        XnatAbstractprotocol childAbstractprotocol = (XnatAbstractprotocol)this.getAbstractprotocol();
	            if (childAbstractprotocol!=null){
	              for(ResourceFile rf: ((XnatAbstractprotocol)childAbstractprotocol).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("abstractProtocol[" + ((XnatAbstractprotocol)childAbstractprotocol).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("abstractProtocol/" + ((XnatAbstractprotocol)childAbstractprotocol).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //acqConditions/condition
	        for(org.nrg.xdat.model.XnatStudyprotocolConditionI childAcqconditions_condition : this.getAcqconditions_condition()){
	            if (childAcqconditions_condition!=null){
	              for(ResourceFile rf: ((XnatStudyprotocolCondition)childAcqconditions_condition).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("acqConditions/condition[" + ((XnatStudyprotocolCondition)childAcqconditions_condition).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("acqConditions/condition/" + ((XnatStudyprotocolCondition)childAcqconditions_condition).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //subjectGroups/group
	        for(org.nrg.xdat.model.XnatStudyprotocolGroupI childSubjectgroups_group : this.getSubjectgroups_group()){
	            if (childSubjectgroups_group!=null){
	              for(ResourceFile rf: ((XnatStudyprotocolGroup)childSubjectgroups_group).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("subjectGroups/group[" + ((XnatStudyprotocolGroup)childSubjectgroups_group).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("subjectGroups/group/" + ((XnatStudyprotocolGroup)childSubjectgroups_group).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //subjectVariables/variable
	        for(org.nrg.xdat.model.XnatStudyprotocolVariableI childSubjectvariables_variable : this.getSubjectvariables_variable()){
	            if (childSubjectvariables_variable!=null){
	              for(ResourceFile rf: ((XnatStudyprotocolVariable)childSubjectvariables_variable).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("subjectVariables/variable[" + ((XnatStudyprotocolVariable)childSubjectvariables_variable).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("subjectVariables/variable/" + ((XnatStudyprotocolVariable)childSubjectvariables_variable).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //imageSessionTypes/session
	        for(org.nrg.xdat.model.XnatStudyprotocolSessionI childImagesessiontypes_session : this.getImagesessiontypes_session()){
	            if (childImagesessiontypes_session!=null){
	              for(ResourceFile rf: ((XnatStudyprotocolSession)childImagesessiontypes_session).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("imageSessionTypes/session[" + ((XnatStudyprotocolSession)childImagesessiontypes_session).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("imageSessionTypes/session/" + ((XnatStudyprotocolSession)childImagesessiontypes_session).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
