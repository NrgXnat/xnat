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
public abstract class AutoXnatQcassessmentdata extends XnatMrassessordata implements org.nrg.xdat.model.XnatQcassessmentdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatQcassessmentdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:qcAssessmentData";

	public AutoXnatQcassessmentdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatQcassessmentdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatQcassessmentdata(UserI user)
	 **/
	public AutoXnatQcassessmentdata(){}

	public AutoXnatQcassessmentdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:qcAssessmentData";
	}
	 private org.nrg.xdat.om.XnatMrassessordata _Mrassessordata =null;

	/**
	 * mrAssessorData
	 * @return org.nrg.xdat.om.XnatMrassessordata
	 */
	public org.nrg.xdat.om.XnatMrassessordata getMrassessordata() {
		try{
			if (_Mrassessordata==null){
				_Mrassessordata=((XnatMrassessordata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("mrAssessorData")));
				return _Mrassessordata;
			}else {
				return _Mrassessordata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for mrAssessorData.
	 * @param v Value to Set.
	 */
	public void setMrassessordata(ItemI v) throws Exception{
		_Mrassessordata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/mrAssessorData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/mrAssessorData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * mrAssessorData
	 * set org.nrg.xdat.model.XnatMrassessordataI
	 */
	public <A extends org.nrg.xdat.model.XnatMrassessordataI> void setMrassessordata(A item) throws Exception{
	setMrassessordata((ItemI)item);
	}

	/**
	 * Removes the mrAssessorData.
	 * */
	public void removeMrassessordata() {
		_Mrassessordata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/mrAssessorData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan> _Scans_scan =null;

	/**
	 * scans/scan
	 * @return Returns an List of org.nrg.xdat.om.XnatQcassessmentdataScan
	 */
	public <A extends org.nrg.xdat.model.XnatQcassessmentdataScanI> List<A> getScans_scan() {
		try{
			if (_Scans_scan==null){
				_Scans_scan=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("scans/scan"));
			}
			return (List<A>) _Scans_scan;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan>();}
	}

	/**
	 * Sets the value for scans/scan.
	 * @param v Value to Set.
	 */
	public void setScans_scan(ItemI v) throws Exception{
		_Scans_scan =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/scans/scan",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/scans/scan",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * scans/scan
	 * Adds org.nrg.xdat.model.XnatQcassessmentdataScanI
	 */
	public <A extends org.nrg.xdat.model.XnatQcassessmentdataScanI> void addScans_scan(A item) throws Exception{
	setScans_scan((ItemI)item);
	}

	/**
	 * Removes the scans/scan of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeScans_scan(int index) throws java.lang.IndexOutOfBoundsException {
		_Scans_scan =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/scans/scan",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Type=null;

	/**
	 * @return Returns the type.
	 */
	public String getType(){
		try{
			if (_Type==null){
				_Type=getStringProperty("type");
				return _Type;
			}else {
				return _Type;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	public void setType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/type",v);
		_Type=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcassessmentdata> getAllXnatQcassessmentdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcassessmentdata> al = new ArrayList<org.nrg.xdat.om.XnatQcassessmentdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcassessmentdata> getXnatQcassessmentdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcassessmentdata> al = new ArrayList<org.nrg.xdat.om.XnatQcassessmentdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcassessmentdata> getXnatQcassessmentdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcassessmentdata> al = new ArrayList<org.nrg.xdat.om.XnatQcassessmentdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatQcassessmentdata getXnatQcassessmentdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:qcAssessmentData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatQcassessmentdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
		XFTItem mr = org.nrg.xft.search.ItemSearch.GetItem("xnat:mrSessionData.ID",getItem().getProperty("xnat:qcAssessmentData.imageSession_ID"),getItem().getUser(),false);
		al.add(mr);
		al.add(org.nrg.xft.search.ItemSearch.GetItem("xnat:subjectData.ID",mr.getProperty("xnat:mrSessionData.subject_ID"),getItem().getUser(),false));
		al.trimToSize();
		return org.nrg.xft.schema.Wrappers.XMLWrapper.XMLWriter.ItemListToDOM(al);
	}
	public ArrayList<ResourceFile> getFileResources(String rootPath, boolean preventLoop){
ArrayList<ResourceFile> _return = new ArrayList<ResourceFile>();
	 boolean localLoop = preventLoop;
	        localLoop = preventLoop;
	
	        //mrAssessorData
	        XnatMrassessordata childMrassessordata = (XnatMrassessordata)this.getMrassessordata();
	            if (childMrassessordata!=null){
	              for(ResourceFile rf: ((XnatMrassessordata)childMrassessordata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("mrAssessorData[" + ((XnatMrassessordata)childMrassessordata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("mrAssessorData/" + ((XnatMrassessordata)childMrassessordata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //scans/scan
	        for(org.nrg.xdat.model.XnatQcassessmentdataScanI childScans_scan : this.getScans_scan()){
	            if (childScans_scan!=null){
	              for(ResourceFile rf: ((XnatQcassessmentdataScan)childScans_scan).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("scans/scan[" + ((XnatQcassessmentdataScan)childScans_scan).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("scans/scan/" + ((XnatQcassessmentdataScan)childScans_scan).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
