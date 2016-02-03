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
public abstract class AutoXnatSubjectvariablesdata extends XnatSubjectassessordata implements org.nrg.xdat.model.XnatSubjectvariablesdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatSubjectvariablesdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:subjectVariablesData";

	public AutoXnatSubjectvariablesdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatSubjectvariablesdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatSubjectvariablesdata(UserI user)
	 **/
	public AutoXnatSubjectvariablesdata(){}

	public AutoXnatSubjectvariablesdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:subjectVariablesData";
	}
	 private org.nrg.xdat.om.XnatSubjectassessordata _Subjectassessordata =null;

	/**
	 * subjectAssessorData
	 * @return org.nrg.xdat.om.XnatSubjectassessordata
	 */
	public org.nrg.xdat.om.XnatSubjectassessordata getSubjectassessordata() {
		try{
			if (_Subjectassessordata==null){
				_Subjectassessordata=((XnatSubjectassessordata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("subjectAssessorData")));
				return _Subjectassessordata;
			}else {
				return _Subjectassessordata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for subjectAssessorData.
	 * @param v Value to Set.
	 */
	public void setSubjectassessordata(ItemI v) throws Exception{
		_Subjectassessordata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectAssessorData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subjectAssessorData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * subjectAssessorData
	 * set org.nrg.xdat.model.XnatSubjectassessordataI
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectassessordataI> void setSubjectassessordata(A item) throws Exception{
	setSubjectassessordata((ItemI)item);
	}

	/**
	 * Removes the subjectAssessorData.
	 * */
	public void removeSubjectassessordata() {
		_Subjectassessordata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/subjectAssessorData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable> _Variables_variable =null;

	/**
	 * variables/variable
	 * @return Returns an List of org.nrg.xdat.om.XnatSubjectvariablesdataVariable
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectvariablesdataVariableI> List<A> getVariables_variable() {
		try{
			if (_Variables_variable==null){
				_Variables_variable=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("variables/variable"));
			}
			return (List<A>) _Variables_variable;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdataVariable>();}
	}

	/**
	 * Sets the value for variables/variable.
	 * @param v Value to Set.
	 */
	public void setVariables_variable(ItemI v) throws Exception{
		_Variables_variable =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/variables/variable",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/variables/variable",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * variables/variable
	 * Adds org.nrg.xdat.model.XnatSubjectvariablesdataVariableI
	 */
	public <A extends org.nrg.xdat.model.XnatSubjectvariablesdataVariableI> void addVariables_variable(A item) throws Exception{
	setVariables_variable((ItemI)item);
	}

	/**
	 * Removes the variables/variable of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeVariables_variable(int index) throws java.lang.IndexOutOfBoundsException {
		_Variables_variable =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/variables/variable",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdata> getAllXnatSubjectvariablesdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdata> getXnatSubjectvariablesdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdata> getXnatSubjectvariablesdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectvariablesdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatSubjectvariablesdata getXnatSubjectvariablesdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:subjectVariablesData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatSubjectvariablesdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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

	public org.w3c.dom.Document toJoinedXML() throws Exception
	{
		ArrayList al = new ArrayList();
		al.add(this.getItem());
		al.add(org.nrg.xft.search.ItemSearch.GetItem("xnat:subjectData.ID",this.getItem().getProperty("xnat:mrSessionData.subject_ID"),getItem().getUser(),false));
		al.trimToSize();
		return org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter.ItemListToDOM(al);
	}
	public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop){
ArrayList<ResourceFile> _return = new ArrayList<ResourceFile>();
	 boolean localLoop = preventLoop;
	        localLoop = preventLoop;
	
	        //subjectAssessorData
	        XnatSubjectassessordata childSubjectassessordata = (XnatSubjectassessordata)this.getSubjectassessordata();
	            if (childSubjectassessordata!=null){
	              for(ResourceFile rf: ((XnatSubjectassessordata)childSubjectassessordata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("subjectAssessorData[" + ((XnatSubjectassessordata)childSubjectassessordata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("subjectAssessorData/" + ((XnatSubjectassessordata)childSubjectassessordata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //variables/variable
	        for(org.nrg.xdat.model.XnatSubjectvariablesdataVariableI childVariables_variable : this.getVariables_variable()){
	            if (childVariables_variable!=null){
	              for(ResourceFile rf: ((XnatSubjectvariablesdataVariable)childVariables_variable).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("variables/variable[" + ((XnatSubjectvariablesdataVariable)childVariables_variable).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("variables/variable/" + ((XnatSubjectvariablesdataVariable)childVariables_variable).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
