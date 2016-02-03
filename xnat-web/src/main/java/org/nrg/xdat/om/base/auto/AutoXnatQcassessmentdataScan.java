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
public abstract class AutoXnatQcassessmentdataScan extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatQcassessmentdataScanI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatQcassessmentdataScan.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:qcAssessmentData_scan";

	public AutoXnatQcassessmentdataScan(ItemI item)
	{
		super(item);
	}

	public AutoXnatQcassessmentdataScan(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatQcassessmentdataScan(UserI user)
	 **/
	public AutoXnatQcassessmentdataScan(){}

	public AutoXnatQcassessmentdataScan(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:qcAssessmentData_scan";
	}
	 private ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice> _Sliceqc_slice =null;

	/**
	 * sliceQC/slice
	 * @return Returns an List of org.nrg.xdat.om.XnatQcassessmentdataScanSlice
	 */
	public <A extends org.nrg.xdat.model.XnatQcassessmentdataScanSliceI> List<A> getSliceqc_slice() {
		try{
			if (_Sliceqc_slice==null){
				_Sliceqc_slice=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("sliceQC/slice"));
			}
			return (List<A>) _Sliceqc_slice;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice>();}
	}

	/**
	 * Sets the value for sliceQC/slice.
	 * @param v Value to Set.
	 */
	public void setSliceqc_slice(ItemI v) throws Exception{
		_Sliceqc_slice =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sliceQC/slice",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sliceQC/slice",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * sliceQC/slice
	 * Adds org.nrg.xdat.model.XnatQcassessmentdataScanSliceI
	 */
	public <A extends org.nrg.xdat.model.XnatQcassessmentdataScanSliceI> void addSliceqc_slice(A item) throws Exception{
	setSliceqc_slice((ItemI)item);
	}

	/**
	 * Removes the sliceQC/slice of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSliceqc_slice(int index) throws java.lang.IndexOutOfBoundsException {
		_Sliceqc_slice =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/sliceQC/slice",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XnatAbstractstatistics _Scanstatistics =null;

	/**
	 * scanStatistics
	 * @return org.nrg.xdat.om.XnatAbstractstatistics
	 */
	public org.nrg.xdat.om.XnatAbstractstatistics getScanstatistics() {
		try{
			if (_Scanstatistics==null){
				_Scanstatistics=((XnatAbstractstatistics)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("scanStatistics")));
				return _Scanstatistics;
			}else {
				return _Scanstatistics;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for scanStatistics.
	 * @param v Value to Set.
	 */
	public void setScanstatistics(ItemI v) throws Exception{
		_Scanstatistics =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/scanStatistics",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/scanStatistics",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * scanStatistics
	 * set org.nrg.xdat.model.XnatAbstractstatisticsI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractstatisticsI> void setScanstatistics(A item) throws Exception{
	setScanstatistics((ItemI)item);
	}

	/**
	 * Removes the scanStatistics.
	 * */
	public void removeScanstatistics() {
		_Scanstatistics =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/scanStatistics",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ScanstatisticsFK=null;

	/**
	 * @return Returns the xnat:qcAssessmentData_scan/scanstatistics_xnat_abstractstatistics_id.
	 */
	public Integer getScanstatisticsFK(){
		try{
			if (_ScanstatisticsFK==null){
				_ScanstatisticsFK=getIntegerProperty("xnat:qcAssessmentData_scan/scanstatistics_xnat_abstractstatistics_id");
				return _ScanstatisticsFK;
			}else {
				return _ScanstatisticsFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:qcAssessmentData_scan/scanstatistics_xnat_abstractstatistics_id.
	 * @param v Value to Set.
	 */
	public void setScanstatisticsFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/scanstatistics_xnat_abstractstatistics_id",v);
		_ScanstatisticsFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Id=null;

	/**
	 * @return Returns the id.
	 */
	public String getId(){
		try{
			if (_Id==null){
				_Id=getStringProperty("id");
				return _Id;
			}else {
				return _Id;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for id.
	 * @param v Value to Set.
	 */
	public void setId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/id",v);
		_Id=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatQcassessmentdataScanId=null;

	/**
	 * @return Returns the xnat_qcAssessmentData_scan_id.
	 */
	public Integer getXnatQcassessmentdataScanId() {
		try{
			if (_XnatQcassessmentdataScanId==null){
				_XnatQcassessmentdataScanId=getIntegerProperty("xnat_qcAssessmentData_scan_id");
				return _XnatQcassessmentdataScanId;
			}else {
				return _XnatQcassessmentdataScanId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_qcAssessmentData_scan_id.
	 * @param v Value to Set.
	 */
	public void setXnatQcassessmentdataScanId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_qcAssessmentData_scan_id",v);
		_XnatQcassessmentdataScanId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan> getAllXnatQcassessmentdataScans(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan> al = new ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan> getXnatQcassessmentdataScansByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan> al = new ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan> getXnatQcassessmentdataScansByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan> al = new ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScan>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatQcassessmentdataScan getXnatQcassessmentdataScansByXnatQcassessmentdataScanId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:qcAssessmentData_scan/xnat_qcAssessmentData_scan_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatQcassessmentdataScan) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //sliceQC/slice
	        for(org.nrg.xdat.model.XnatQcassessmentdataScanSliceI childSliceqc_slice : this.getSliceqc_slice()){
	            if (childSliceqc_slice!=null){
	              for(ResourceFile rf: ((XnatQcassessmentdataScanSlice)childSliceqc_slice).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("sliceQC/slice[" + ((XnatQcassessmentdataScanSlice)childSliceqc_slice).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("sliceQC/slice/" + ((XnatQcassessmentdataScanSlice)childSliceqc_slice).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //scanStatistics
	        XnatAbstractstatistics childScanstatistics = (XnatAbstractstatistics)this.getScanstatistics();
	            if (childScanstatistics!=null){
	              for(ResourceFile rf: ((XnatAbstractstatistics)childScanstatistics).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("scanStatistics[" + ((XnatAbstractstatistics)childScanstatistics).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("scanStatistics/" + ((XnatAbstractstatistics)childScanstatistics).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
