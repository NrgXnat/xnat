/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:04 UTC 2016
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
public abstract class AutoScrScreeningassessment extends XnatImageassessordata implements org.nrg.xdat.model.ScrScreeningassessmentI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoScrScreeningassessment.class);
	public static String SCHEMA_ELEMENT_NAME="scr:screeningAssessment";

	public AutoScrScreeningassessment(ItemI item)
	{
		super(item);
	}

	public AutoScrScreeningassessment(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoScrScreeningassessment(UserI user)
	 **/
	public AutoScrScreeningassessment(){}

	public AutoScrScreeningassessment(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "scr:screeningAssessment";
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

	//FIELD

	private String _Rater=null;

	/**
	 * @return Returns the rater.
	 */
	public String getRater(){
		try{
			if (_Rater==null){
				_Rater=getStringProperty("rater");
				return _Rater;
			}else {
				return _Rater;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rater.
	 * @param v Value to Set.
	 */
	public void setRater(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rater",v);
		_Rater=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Comments=null;

	/**
	 * @return Returns the comments.
	 */
	public String getComments(){
		try{
			if (_Comments==null){
				_Comments=getStringProperty("comments");
				return _Comments;
			}else {
				return _Comments;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for comments.
	 * @param v Value to Set.
	 */
	public void setComments(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/comments",v);
		_Comments=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Pass=null;

	/**
	 * @return Returns the pass.
	 */
	public String getPass(){
		try{
			if (_Pass==null){
				_Pass=getStringProperty("pass");
				return _Pass;
			}else {
				return _Pass;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pass.
	 * @param v Value to Set.
	 */
	public void setPass(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pass",v);
		_Pass=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ScrScreeningscandata> _Scans_scan =null;

	/**
	 * scans/scan
	 * @return Returns an List of org.nrg.xdat.om.ScrScreeningscandata
	 */
	public <A extends org.nrg.xdat.model.ScrScreeningscandataI> List<A> getScans_scan() {
		try{
			if (_Scans_scan==null){
				_Scans_scan=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("scans/scan"));
			}
			return (List<A>) _Scans_scan;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ScrScreeningscandata>();}
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
	 * Adds org.nrg.xdat.model.ScrScreeningscandataI
	 */
	public <A extends org.nrg.xdat.model.ScrScreeningscandataI> void addScans_scan(A item) throws Exception{
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

	public static ArrayList<org.nrg.xdat.om.ScrScreeningassessment> getAllScrScreeningassessments(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ScrScreeningassessment> al = new ArrayList<org.nrg.xdat.om.ScrScreeningassessment>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ScrScreeningassessment> getScrScreeningassessmentsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ScrScreeningassessment> al = new ArrayList<org.nrg.xdat.om.ScrScreeningassessment>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ScrScreeningassessment> getScrScreeningassessmentsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ScrScreeningassessment> al = new ArrayList<org.nrg.xdat.om.ScrScreeningassessment>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ScrScreeningassessment getScrScreeningassessmentsById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("scr:screeningAssessment/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ScrScreeningassessment) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //scans/scan
	        for(org.nrg.xdat.model.ScrScreeningscandataI childScans_scan : this.getScans_scan()){
	            if (childScans_scan!=null){
	              for(ResourceFile rf: ((ScrScreeningscandata)childScans_scan).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("scans/scan[" + ((ScrScreeningscandata)childScans_scan).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("scans/scan/" + ((ScrScreeningscandata)childScans_scan).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
