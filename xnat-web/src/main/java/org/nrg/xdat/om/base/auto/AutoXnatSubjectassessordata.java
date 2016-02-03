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
public abstract class AutoXnatSubjectassessordata extends XnatExperimentdata implements org.nrg.xdat.model.XnatSubjectassessordataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatSubjectassessordata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:subjectAssessorData";

	public AutoXnatSubjectassessordata(ItemI item)
	{
		super(item);
	}

	public AutoXnatSubjectassessordata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatSubjectassessordata(UserI user)
	 **/
	public AutoXnatSubjectassessordata(){}

	public AutoXnatSubjectassessordata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:subjectAssessorData";
	}
	 private org.nrg.xdat.om.XnatExperimentdata _Experimentdata =null;

	/**
	 * experimentData
	 * @return org.nrg.xdat.om.XnatExperimentdata
	 */
	public org.nrg.xdat.om.XnatExperimentdata getExperimentdata() {
		try{
			if (_Experimentdata==null){
				_Experimentdata=((XnatExperimentdata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("experimentData")));
				return _Experimentdata;
			}else {
				return _Experimentdata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for experimentData.
	 * @param v Value to Set.
	 */
	public void setExperimentdata(ItemI v) throws Exception{
		_Experimentdata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/experimentData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/experimentData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * experimentData
	 * set org.nrg.xdat.model.XnatExperimentdataI
	 */
	public <A extends org.nrg.xdat.model.XnatExperimentdataI> void setExperimentdata(A item) throws Exception{
	setExperimentdata((ItemI)item);
	}

	/**
	 * Removes the experimentData.
	 * */
	public void removeExperimentdata() {
		_Experimentdata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/experimentData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _SubjectId=null;

	/**
	 * @return Returns the subject_ID.
	 */
	public String getSubjectId(){
		try{
			if (_SubjectId==null){
				_SubjectId=getStringProperty("subject_ID");
				return _SubjectId;
			}else {
				return _SubjectId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for subject_ID.
	 * @param v Value to Set.
	 */
	public void setSubjectId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/subject_ID",v);
		_SubjectId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Age=null;

	/**
	 * @return Returns the age.
	 */
	public Double getAge() {
		try{
			if (_Age==null){
				_Age=getDoubleProperty("age");
				return _Age;
			}else {
				return _Age;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for age.
	 * @param v Value to Set.
	 */
	public void setAge(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/age",v);
		_Age=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectassessordata> getAllXnatSubjectassessordatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectassessordata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectassessordata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectassessordata> getXnatSubjectassessordatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectassessordata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectassessordata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatSubjectassessordata> getXnatSubjectassessordatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatSubjectassessordata> al = new ArrayList<org.nrg.xdat.om.XnatSubjectassessordata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatSubjectassessordata getXnatSubjectassessordatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:subjectAssessorData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatSubjectassessordata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //experimentData
	        XnatExperimentdata childExperimentdata = (XnatExperimentdata)this.getExperimentdata();
	            if (childExperimentdata!=null){
	              for(ResourceFile rf: ((XnatExperimentdata)childExperimentdata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("experimentData[" + ((XnatExperimentdata)childExperimentdata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("experimentData/" + ((XnatExperimentdata)childExperimentdata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
