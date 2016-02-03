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
public abstract class AutoXnatImagesessiondata extends XnatSubjectassessordata implements org.nrg.xdat.model.XnatImagesessiondataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatImagesessiondata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:imageSessionData";

	public AutoXnatImagesessiondata(ItemI item)
	{
		super(item);
	}

	public AutoXnatImagesessiondata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatImagesessiondata(UserI user)
	 **/
	public AutoXnatImagesessiondata(){}

	public AutoXnatImagesessiondata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:imageSessionData";
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
	 private ArrayList<org.nrg.xdat.om.XnatRegionresource> _Regions_region =null;

	/**
	 * regions/region
	 * @return Returns an List of org.nrg.xdat.om.XnatRegionresource
	 */
	public <A extends org.nrg.xdat.model.XnatRegionresourceI> List<A> getRegions_region() {
		try{
			if (_Regions_region==null){
				_Regions_region=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("regions/region"));
			}
			return (List<A>) _Regions_region;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatRegionresource>();}
	}

	/**
	 * Sets the value for regions/region.
	 * @param v Value to Set.
	 */
	public void setRegions_region(ItemI v) throws Exception{
		_Regions_region =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/regions/region",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/regions/region",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * regions/region
	 * Adds org.nrg.xdat.model.XnatRegionresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatRegionresourceI> void addRegions_region(A item) throws Exception{
	setRegions_region((ItemI)item);
	}

	/**
	 * Removes the regions/region of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeRegions_region(int index) throws java.lang.IndexOutOfBoundsException {
		_Regions_region =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/regions/region",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Scanner=null;

	/**
	 * @return Returns the scanner.
	 */
	public String getScanner(){
		try{
			if (_Scanner==null){
				_Scanner=getStringProperty("scanner");
				return _Scanner;
			}else {
				return _Scanner;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for scanner.
	 * @param v Value to Set.
	 */
	public void setScanner(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/scanner",v);
		_Scanner=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Scanner_manufacturer=null;

	/**
	 * @return Returns the scanner/manufacturer.
	 */
	public String getScanner_manufacturer(){
		try{
			if (_Scanner_manufacturer==null){
				_Scanner_manufacturer=getStringProperty("scanner/manufacturer");
				return _Scanner_manufacturer;
			}else {
				return _Scanner_manufacturer;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for scanner/manufacturer.
	 * @param v Value to Set.
	 */
	public void setScanner_manufacturer(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/scanner/manufacturer",v);
		_Scanner_manufacturer=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Scanner_model=null;

	/**
	 * @return Returns the scanner/model.
	 */
	public String getScanner_model(){
		try{
			if (_Scanner_model==null){
				_Scanner_model=getStringProperty("scanner/model");
				return _Scanner_model;
			}else {
				return _Scanner_model;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for scanner/model.
	 * @param v Value to Set.
	 */
	public void setScanner_model(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/scanner/model",v);
		_Scanner_model=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Operator=null;

	/**
	 * @return Returns the operator.
	 */
	public String getOperator(){
		try{
			if (_Operator==null){
				_Operator=getStringProperty("operator");
				return _Operator;
			}else {
				return _Operator;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for operator.
	 * @param v Value to Set.
	 */
	public void setOperator(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/operator",v);
		_Operator=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Prearchivepath=null;

	/**
	 * @return Returns the prearchivePath.
	 */
	public String getPrearchivepath(){
		try{
			if (_Prearchivepath==null){
				_Prearchivepath=getStringProperty("prearchivePath");
				return _Prearchivepath;
			}else {
				return _Prearchivepath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for prearchivePath.
	 * @param v Value to Set.
	 */
	public void setPrearchivepath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/prearchivePath",v);
		_Prearchivepath=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatImagescandata> _Scans_scan =null;

	/**
	 * scans/scan
	 * @return Returns an List of org.nrg.xdat.om.XnatImagescandata
	 */
	public <A extends org.nrg.xdat.model.XnatImagescandataI> List<A> getScans_scan() {
		try{
			if (_Scans_scan==null){
				_Scans_scan=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("scans/scan"));
			}
			return (List<A>) _Scans_scan;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatImagescandata>();}
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
	 * Adds org.nrg.xdat.model.XnatImagescandataI
	 */
	public <A extends org.nrg.xdat.model.XnatImagescandataI> void addScans_scan(A item) throws Exception{
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
	 private ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata> _Reconstructions_reconstructedimage =null;

	/**
	 * reconstructions/reconstructedImage
	 * @return Returns an List of org.nrg.xdat.om.XnatReconstructedimagedata
	 */
	public <A extends org.nrg.xdat.model.XnatReconstructedimagedataI> List<A> getReconstructions_reconstructedimage() {
		try{
			if (_Reconstructions_reconstructedimage==null){
				_Reconstructions_reconstructedimage=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("reconstructions/reconstructedImage"));
			}
			return (List<A>) _Reconstructions_reconstructedimage;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata>();}
	}

	/**
	 * Sets the value for reconstructions/reconstructedImage.
	 * @param v Value to Set.
	 */
	public void setReconstructions_reconstructedimage(ItemI v) throws Exception{
		_Reconstructions_reconstructedimage =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/reconstructions/reconstructedImage",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/reconstructions/reconstructedImage",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * reconstructions/reconstructedImage
	 * Adds org.nrg.xdat.model.XnatReconstructedimagedataI
	 */
	public <A extends org.nrg.xdat.model.XnatReconstructedimagedataI> void addReconstructions_reconstructedimage(A item) throws Exception{
	setReconstructions_reconstructedimage((ItemI)item);
	}

	/**
	 * Removes the reconstructions/reconstructedImage of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeReconstructions_reconstructedimage(int index) throws java.lang.IndexOutOfBoundsException {
		_Reconstructions_reconstructedimage =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/reconstructions/reconstructedImage",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatImageassessordata> _Assessors_assessor =null;

	/**
	 * assessors/assessor
	 * @return Returns an List of org.nrg.xdat.om.XnatImageassessordata
	 */
	public <A extends org.nrg.xdat.model.XnatImageassessordataI> List<A> getAssessors_assessor() {
		try{
			if (_Assessors_assessor==null){
				_Assessors_assessor=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("assessors/assessor"));
			}
			return (List<A>) _Assessors_assessor;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatImageassessordata>();}
	}

	/**
	 * Sets the value for assessors/assessor.
	 * @param v Value to Set.
	 */
	public void setAssessors_assessor(ItemI v) throws Exception{
		_Assessors_assessor =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/assessors/assessor",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/assessors/assessor",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * assessors/assessor
	 * Adds org.nrg.xdat.model.XnatImageassessordataI
	 */
	public <A extends org.nrg.xdat.model.XnatImageassessordataI> void addAssessors_assessor(A item) throws Exception{
	setAssessors_assessor((ItemI)item);
	}

	/**
	 * Removes the assessors/assessor of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeAssessors_assessor(int index) throws java.lang.IndexOutOfBoundsException {
		_Assessors_assessor =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/assessors/assessor",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Dcmaccessionnumber=null;

	/**
	 * @return Returns the dcmAccessionNumber.
	 */
	public String getDcmaccessionnumber(){
		try{
			if (_Dcmaccessionnumber==null){
				_Dcmaccessionnumber=getStringProperty("dcmAccessionNumber");
				return _Dcmaccessionnumber;
			}else {
				return _Dcmaccessionnumber;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcmAccessionNumber.
	 * @param v Value to Set.
	 */
	public void setDcmaccessionnumber(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dcmAccessionNumber",v);
		_Dcmaccessionnumber=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Dcmpatientid=null;

	/**
	 * @return Returns the dcmPatientId.
	 */
	public String getDcmpatientid(){
		try{
			if (_Dcmpatientid==null){
				_Dcmpatientid=getStringProperty("dcmPatientId");
				return _Dcmpatientid;
			}else {
				return _Dcmpatientid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcmPatientId.
	 * @param v Value to Set.
	 */
	public void setDcmpatientid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dcmPatientId",v);
		_Dcmpatientid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Dcmpatientname=null;

	/**
	 * @return Returns the dcmPatientName.
	 */
	public String getDcmpatientname(){
		try{
			if (_Dcmpatientname==null){
				_Dcmpatientname=getStringProperty("dcmPatientName");
				return _Dcmpatientname;
			}else {
				return _Dcmpatientname;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcmPatientName.
	 * @param v Value to Set.
	 */
	public void setDcmpatientname(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dcmPatientName",v);
		_Dcmpatientname=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Dcmpatientbirthdate=null;

	/**
	 * @return Returns the dcmPatientBirthDate.
	 */
	public Object getDcmpatientbirthdate(){
		try{
			if (_Dcmpatientbirthdate==null){
				_Dcmpatientbirthdate=getProperty("dcmPatientBirthDate");
				return _Dcmpatientbirthdate;
			}else {
				return _Dcmpatientbirthdate;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dcmPatientBirthDate.
	 * @param v Value to Set.
	 */
	public void setDcmpatientbirthdate(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dcmPatientBirthDate",v);
		_Dcmpatientbirthdate=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _SessionType=null;

	/**
	 * @return Returns the session_type.
	 */
	public String getSessionType(){
		try{
			if (_SessionType==null){
				_SessionType=getStringProperty("session_type");
				return _SessionType;
			}else {
				return _SessionType;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for session_type.
	 * @param v Value to Set.
	 */
	public void setSessionType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/session_type",v);
		_SessionType=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Modality=null;

	/**
	 * @return Returns the modality.
	 */
	public String getModality(){
		try{
			if (_Modality==null){
				_Modality=getStringProperty("modality");
				return _Modality;
			}else {
				return _Modality;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for modality.
	 * @param v Value to Set.
	 */
	public void setModality(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/modality",v);
		_Modality=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Uid=null;

	/**
	 * @return Returns the UID.
	 */
	public String getUid(){
		try{
			if (_Uid==null){
				_Uid=getStringProperty("UID");
				return _Uid;
			}else {
				return _Uid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for UID.
	 * @param v Value to Set.
	 */
	public void setUid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/UID",v);
		_Uid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatImagesessiondata> getAllXnatImagesessiondatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImagesessiondata> al = new ArrayList<org.nrg.xdat.om.XnatImagesessiondata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImagesessiondata> getXnatImagesessiondatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImagesessiondata> al = new ArrayList<org.nrg.xdat.om.XnatImagesessiondata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImagesessiondata> getXnatImagesessiondatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImagesessiondata> al = new ArrayList<org.nrg.xdat.om.XnatImagesessiondata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatImagesessiondata getXnatImagesessiondatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:imageSessionData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatImagesessiondata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //regions/region
	        for(org.nrg.xdat.model.XnatRegionresourceI childRegions_region : this.getRegions_region()){
	            if (childRegions_region!=null){
	              for(ResourceFile rf: ((XnatRegionresource)childRegions_region).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("regions/region[" + ((XnatRegionresource)childRegions_region).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("regions/region/" + ((XnatRegionresource)childRegions_region).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //scans/scan
	        for(org.nrg.xdat.model.XnatImagescandataI childScans_scan : this.getScans_scan()){
	            if (childScans_scan!=null){
	              for(ResourceFile rf: ((XnatImagescandata)childScans_scan).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("scans/scan[" + ((XnatImagescandata)childScans_scan).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("scans/scan/" + ((XnatImagescandata)childScans_scan).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //reconstructions/reconstructedImage
	        for(org.nrg.xdat.model.XnatReconstructedimagedataI childReconstructions_reconstructedimage : this.getReconstructions_reconstructedimage()){
	            if (childReconstructions_reconstructedimage!=null){
	              for(ResourceFile rf: ((XnatReconstructedimagedata)childReconstructions_reconstructedimage).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("reconstructions/reconstructedImage[" + ((XnatReconstructedimagedata)childReconstructions_reconstructedimage).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("reconstructions/reconstructedImage/" + ((XnatReconstructedimagedata)childReconstructions_reconstructedimage).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //assessors/assessor
	        for(org.nrg.xdat.model.XnatImageassessordataI childAssessors_assessor : this.getAssessors_assessor()){
	            if (childAssessors_assessor!=null){
	              for(ResourceFile rf: ((XnatImageassessordata)childAssessors_assessor).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("assessors/assessor[" + ((XnatImageassessordata)childAssessors_assessor).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("assessors/assessor/" + ((XnatImageassessordata)childAssessors_assessor).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
