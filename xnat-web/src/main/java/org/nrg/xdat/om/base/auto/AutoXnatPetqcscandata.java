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
public abstract class AutoXnatPetqcscandata extends XnatQcscandata implements org.nrg.xdat.model.XnatPetqcscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatPetqcscandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:petQcScanData";

	public AutoXnatPetqcscandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatPetqcscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatPetqcscandata(UserI user)
	 **/
	public AutoXnatPetqcscandata(){}

	public AutoXnatPetqcscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:petQcScanData";
	}
	 private org.nrg.xdat.om.XnatQcscandata _Qcscandata =null;

	/**
	 * qcScanData
	 * @return org.nrg.xdat.om.XnatQcscandata
	 */
	public org.nrg.xdat.om.XnatQcscandata getQcscandata() {
		try{
			if (_Qcscandata==null){
				_Qcscandata=((XnatQcscandata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("qcScanData")));
				return _Qcscandata;
			}else {
				return _Qcscandata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for qcScanData.
	 * @param v Value to Set.
	 */
	public void setQcscandata(ItemI v) throws Exception{
		_Qcscandata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/qcScanData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/qcScanData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * qcScanData
	 * set org.nrg.xdat.model.XnatQcscandataI
	 */
	public <A extends org.nrg.xdat.model.XnatQcscandataI> void setQcscandata(A item) throws Exception{
	setQcscandata((ItemI)item);
	}

	/**
	 * Removes the qcScanData.
	 * */
	public void removeQcscandata() {
		_Qcscandata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/qcScanData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Acquisition=null;

	/**
	 * @return Returns the acquisition.
	 */
	public String getAcquisition(){
		try{
			if (_Acquisition==null){
				_Acquisition=getStringProperty("acquisition");
				return _Acquisition;
			}else {
				return _Acquisition;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for acquisition.
	 * @param v Value to Set.
	 */
	public void setAcquisition(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/acquisition",v);
		_Acquisition=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Correctreconstructionalgorithm=null;

	/**
	 * @return Returns the correctReconstructionAlgorithm.
	 */
	public String getCorrectreconstructionalgorithm(){
		try{
			if (_Correctreconstructionalgorithm==null){
				_Correctreconstructionalgorithm=getStringProperty("correctReconstructionAlgorithm");
				return _Correctreconstructionalgorithm;
			}else {
				return _Correctreconstructionalgorithm;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for correctReconstructionAlgorithm.
	 * @param v Value to Set.
	 */
	public void setCorrectreconstructionalgorithm(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/correctReconstructionAlgorithm",v);
		_Correctreconstructionalgorithm=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Reconstructionalgorithmused=null;

	/**
	 * @return Returns the reconstructionAlgorithmUsed.
	 */
	public String getReconstructionalgorithmused(){
		try{
			if (_Reconstructionalgorithmused==null){
				_Reconstructionalgorithmused=getStringProperty("reconstructionAlgorithmUsed");
				return _Reconstructionalgorithmused;
			}else {
				return _Reconstructionalgorithmused;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for reconstructionAlgorithmUsed.
	 * @param v Value to Set.
	 */
	public void setReconstructionalgorithmused(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/reconstructionAlgorithmUsed",v);
		_Reconstructionalgorithmused=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Correctiterationsandsubsets=null;

	/**
	 * @return Returns the correctIterationsAndSubsets.
	 */
	public String getCorrectiterationsandsubsets(){
		try{
			if (_Correctiterationsandsubsets==null){
				_Correctiterationsandsubsets=getStringProperty("correctIterationsAndSubsets");
				return _Correctiterationsandsubsets;
			}else {
				return _Correctiterationsandsubsets;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for correctIterationsAndSubsets.
	 * @param v Value to Set.
	 */
	public void setCorrectiterationsandsubsets(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/correctIterationsAndSubsets",v);
		_Correctiterationsandsubsets=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Correctfilters=null;

	/**
	 * @return Returns the correctFilters.
	 */
	public String getCorrectfilters(){
		try{
			if (_Correctfilters==null){
				_Correctfilters=getStringProperty("correctFilters");
				return _Correctfilters;
			}else {
				return _Correctfilters;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for correctFilters.
	 * @param v Value to Set.
	 */
	public void setCorrectfilters(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/correctFilters",v);
		_Correctfilters=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Correctslicethickness=null;

	/**
	 * @return Returns the correctSliceThickness.
	 */
	public String getCorrectslicethickness(){
		try{
			if (_Correctslicethickness==null){
				_Correctslicethickness=getStringProperty("correctSliceThickness");
				return _Correctslicethickness;
			}else {
				return _Correctslicethickness;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for correctSliceThickness.
	 * @param v Value to Set.
	 */
	public void setCorrectslicethickness(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/correctSliceThickness",v);
		_Correctslicethickness=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Acceptablevoxelsize=null;

	/**
	 * @return Returns the acceptableVoxelSize.
	 */
	public String getAcceptablevoxelsize(){
		try{
			if (_Acceptablevoxelsize==null){
				_Acceptablevoxelsize=getStringProperty("acceptableVoxelSize");
				return _Acceptablevoxelsize;
			}else {
				return _Acceptablevoxelsize;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for acceptableVoxelSize.
	 * @param v Value to Set.
	 */
	public void setAcceptablevoxelsize(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/acceptableVoxelSize",v);
		_Acceptablevoxelsize=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Unacceptableframes=null;

	/**
	 * @return Returns the unacceptableFrames.
	 */
	public String getUnacceptableframes(){
		try{
			if (_Unacceptableframes==null){
				_Unacceptableframes=getStringProperty("unacceptableFrames");
				return _Unacceptableframes;
			}else {
				return _Unacceptableframes;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for unacceptableFrames.
	 * @param v Value to Set.
	 */
	public void setUnacceptableframes(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/unacceptableFrames",v);
		_Unacceptableframes=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Reasonframesunacceptable=null;

	/**
	 * @return Returns the reasonFramesUnacceptable.
	 */
	public String getReasonframesunacceptable(){
		try{
			if (_Reasonframesunacceptable==null){
				_Reasonframesunacceptable=getStringProperty("reasonFramesUnacceptable");
				return _Reasonframesunacceptable;
			}else {
				return _Reasonframesunacceptable;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for reasonFramesUnacceptable.
	 * @param v Value to Set.
	 */
	public void setReasonframesunacceptable(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/reasonFramesUnacceptable",v);
		_Reasonframesunacceptable=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror> _Processingerrors_processingerror =null;

	/**
	 * processingErrors/processingError
	 * @return Returns an List of org.nrg.xdat.om.XnatPetqcscandataProcessingerror
	 */
	public <A extends org.nrg.xdat.model.XnatPetqcscandataProcessingerrorI> List<A> getProcessingerrors_processingerror() {
		try{
			if (_Processingerrors_processingerror==null){
				_Processingerrors_processingerror=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("processingErrors/processingError"));
			}
			return (List<A>) _Processingerrors_processingerror;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatPetqcscandataProcessingerror>();}
	}

	/**
	 * Sets the value for processingErrors/processingError.
	 * @param v Value to Set.
	 */
	public void setProcessingerrors_processingerror(ItemI v) throws Exception{
		_Processingerrors_processingerror =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/processingErrors/processingError",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/processingErrors/processingError",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * processingErrors/processingError
	 * Adds org.nrg.xdat.model.XnatPetqcscandataProcessingerrorI
	 */
	public <A extends org.nrg.xdat.model.XnatPetqcscandataProcessingerrorI> void addProcessingerrors_processingerror(A item) throws Exception{
	setProcessingerrors_processingerror((ItemI)item);
	}

	/**
	 * Removes the processingErrors/processingError of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeProcessingerrors_processingerror(int index) throws java.lang.IndexOutOfBoundsException {
		_Processingerrors_processingerror =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/processingErrors/processingError",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Qcoutcome=null;

	/**
	 * @return Returns the qcOutcome.
	 */
	public String getQcoutcome(){
		try{
			if (_Qcoutcome==null){
				_Qcoutcome=getStringProperty("qcOutcome");
				return _Qcoutcome;
			}else {
				return _Qcoutcome;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for qcOutcome.
	 * @param v Value to Set.
	 */
	public void setQcoutcome(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/qcOutcome",v);
		_Qcoutcome=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Qcoutcomereason=null;

	/**
	 * @return Returns the qcOutcomeReason.
	 */
	public String getQcoutcomereason(){
		try{
			if (_Qcoutcomereason==null){
				_Qcoutcomereason=getStringProperty("qcOutcomeReason");
				return _Qcoutcomereason;
			}else {
				return _Qcoutcomereason;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for qcOutcomeReason.
	 * @param v Value to Set.
	 */
	public void setQcoutcomereason(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/qcOutcomeReason",v);
		_Qcoutcomereason=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Topcutoff=null;

	/**
	 * @return Returns the topCutoff.
	 */
	public String getTopcutoff(){
		try{
			if (_Topcutoff==null){
				_Topcutoff=getStringProperty("topCutoff");
				return _Topcutoff;
			}else {
				return _Topcutoff;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for topCutoff.
	 * @param v Value to Set.
	 */
	public void setTopcutoff(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/topCutoff",v);
		_Topcutoff=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Bottomcutoff=null;

	/**
	 * @return Returns the bottomCutoff.
	 */
	public String getBottomcutoff(){
		try{
			if (_Bottomcutoff==null){
				_Bottomcutoff=getStringProperty("bottomCutoff");
				return _Bottomcutoff;
			}else {
				return _Bottomcutoff;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for bottomCutoff.
	 * @param v Value to Set.
	 */
	public void setBottomcutoff(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/bottomCutoff",v);
		_Bottomcutoff=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetqcscandata> getAllXnatPetqcscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetqcscandata> al = new ArrayList<org.nrg.xdat.om.XnatPetqcscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetqcscandata> getXnatPetqcscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetqcscandata> al = new ArrayList<org.nrg.xdat.om.XnatPetqcscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetqcscandata> getXnatPetqcscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetqcscandata> al = new ArrayList<org.nrg.xdat.om.XnatPetqcscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatPetqcscandata getXnatPetqcscandatasByXnatQcscandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:petQcScanData/xnat_qcscandata_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPetqcscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //qcScanData
	        XnatQcscandata childQcscandata = (XnatQcscandata)this.getQcscandata();
	            if (childQcscandata!=null){
	              for(ResourceFile rf: ((XnatQcscandata)childQcscandata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("qcScanData[" + ((XnatQcscandata)childQcscandata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("qcScanData/" + ((XnatQcscandata)childQcscandata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //processingErrors/processingError
	        for(org.nrg.xdat.model.XnatPetqcscandataProcessingerrorI childProcessingerrors_processingerror : this.getProcessingerrors_processingerror()){
	            if (childProcessingerrors_processingerror!=null){
	              for(ResourceFile rf: ((XnatPetqcscandataProcessingerror)childProcessingerrors_processingerror).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("processingErrors/processingError[" + ((XnatPetqcscandataProcessingerror)childProcessingerrors_processingerror).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("processingErrors/processingError/" + ((XnatPetqcscandataProcessingerror)childProcessingerrors_processingerror).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
