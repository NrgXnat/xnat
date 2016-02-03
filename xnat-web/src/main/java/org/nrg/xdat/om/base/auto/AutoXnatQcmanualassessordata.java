/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:09 UTC 2016
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
public abstract class AutoXnatQcmanualassessordata extends XnatImageassessordata implements org.nrg.xdat.model.XnatQcmanualassessordataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatQcmanualassessordata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:qcManualAssessorData";

	public AutoXnatQcmanualassessordata(ItemI item)
	{
		super(item);
	}

	public AutoXnatQcmanualassessordata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatQcmanualassessordata(UserI user)
	 **/
	public AutoXnatQcmanualassessordata(){}

	public AutoXnatQcmanualassessordata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:qcManualAssessorData";
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

	private String _Stereotacticmarker=null;

	/**
	 * @return Returns the stereotacticMarker.
	 */
	public String getStereotacticmarker(){
		try{
			if (_Stereotacticmarker==null){
				_Stereotacticmarker=getStringProperty("stereotacticMarker");
				return _Stereotacticmarker;
			}else {
				return _Stereotacticmarker;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for stereotacticMarker.
	 * @param v Value to Set.
	 */
	public void setStereotacticmarker(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/stereotacticMarker",v);
		_Stereotacticmarker=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Incidentalfindings=null;

	/**
	 * @return Returns the incidentalFindings.
	 */
	public String getIncidentalfindings(){
		try{
			if (_Incidentalfindings==null){
				_Incidentalfindings=getStringProperty("incidentalFindings");
				return _Incidentalfindings;
			}else {
				return _Incidentalfindings;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for incidentalFindings.
	 * @param v Value to Set.
	 */
	public void setIncidentalfindings(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/incidentalFindings",v);
		_Incidentalfindings=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatQcscandata> _Scans_scan =null;

	/**
	 * scans/scan
	 * @return Returns an List of org.nrg.xdat.om.XnatQcscandata
	 */
	public <A extends org.nrg.xdat.model.XnatQcscandataI> List<A> getScans_scan() {
		try{
			if (_Scans_scan==null){
				_Scans_scan=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("scans/scan"));
			}
			return (List<A>) _Scans_scan;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatQcscandata>();}
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
	 * Adds org.nrg.xdat.model.XnatQcscandataI
	 */
	public <A extends org.nrg.xdat.model.XnatQcscandataI> void addScans_scan(A item) throws Exception{
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

	//FIELD

	private String _Payable=null;

	/**
	 * @return Returns the payable.
	 */
	public String getPayable(){
		try{
			if (_Payable==null){
				_Payable=getStringProperty("payable");
				return _Payable;
			}else {
				return _Payable;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for payable.
	 * @param v Value to Set.
	 */
	public void setPayable(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/payable",v);
		_Payable=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Rescan=null;

	/**
	 * @return Returns the rescan.
	 */
	public String getRescan(){
		try{
			if (_Rescan==null){
				_Rescan=getStringProperty("rescan");
				return _Rescan;
			}else {
				return _Rescan;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rescan.
	 * @param v Value to Set.
	 */
	public void setRescan(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rescan",v);
		_Rescan=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Resolvable=null;

	/**
	 * @return Returns the resolvable.
	 */
	public String getResolvable(){
		try{
			if (_Resolvable==null){
				_Resolvable=getStringProperty("resolvable");
				return _Resolvable;
			}else {
				return _Resolvable;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for resolvable.
	 * @param v Value to Set.
	 */
	public void setResolvable(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/resolvable",v);
		_Resolvable=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Retrain=null;

	/**
	 * @return Returns the retrain.
	 */
	public String getRetrain(){
		try{
			if (_Retrain==null){
				_Retrain=getStringProperty("retrain");
				return _Retrain;
			}else {
				return _Retrain;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for retrain.
	 * @param v Value to Set.
	 */
	public void setRetrain(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/retrain",v);
		_Retrain=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcmanualassessordata> getAllXnatQcmanualassessordatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcmanualassessordata> al = new ArrayList<org.nrg.xdat.om.XnatQcmanualassessordata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcmanualassessordata> getXnatQcmanualassessordatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcmanualassessordata> al = new ArrayList<org.nrg.xdat.om.XnatQcmanualassessordata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcmanualassessordata> getXnatQcmanualassessordatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcmanualassessordata> al = new ArrayList<org.nrg.xdat.om.XnatQcmanualassessordata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatQcmanualassessordata getXnatQcmanualassessordatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:qcManualAssessorData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatQcmanualassessordata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	        for(org.nrg.xdat.model.XnatQcscandataI childScans_scan : this.getScans_scan()){
	            if (childScans_scan!=null){
	              for(ResourceFile rf: ((XnatQcscandata)childScans_scan).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("scans/scan[" + ((XnatQcscandata)childScans_scan).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("scans/scan/" + ((XnatQcscandata)childScans_scan).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
