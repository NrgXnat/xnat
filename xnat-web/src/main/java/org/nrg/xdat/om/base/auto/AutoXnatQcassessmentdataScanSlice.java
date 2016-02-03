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
public abstract class AutoXnatQcassessmentdataScanSlice extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatQcassessmentdataScanSliceI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatQcassessmentdataScanSlice.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:qcAssessmentData_scan_slice";

	public AutoXnatQcassessmentdataScanSlice(ItemI item)
	{
		super(item);
	}

	public AutoXnatQcassessmentdataScanSlice(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatQcassessmentdataScanSlice(UserI user)
	 **/
	public AutoXnatQcassessmentdataScanSlice(){}

	public AutoXnatQcassessmentdataScanSlice(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:qcAssessmentData_scan_slice";
	}
	 private org.nrg.xdat.om.XnatAbstractstatistics _Slicestatistics =null;

	/**
	 * sliceStatistics
	 * @return org.nrg.xdat.om.XnatAbstractstatistics
	 */
	public org.nrg.xdat.om.XnatAbstractstatistics getSlicestatistics() {
		try{
			if (_Slicestatistics==null){
				_Slicestatistics=((XnatAbstractstatistics)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("sliceStatistics")));
				return _Slicestatistics;
			}else {
				return _Slicestatistics;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for sliceStatistics.
	 * @param v Value to Set.
	 */
	public void setSlicestatistics(ItemI v) throws Exception{
		_Slicestatistics =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sliceStatistics",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sliceStatistics",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * sliceStatistics
	 * set org.nrg.xdat.model.XnatAbstractstatisticsI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractstatisticsI> void setSlicestatistics(A item) throws Exception{
	setSlicestatistics((ItemI)item);
	}

	/**
	 * Removes the sliceStatistics.
	 * */
	public void removeSlicestatistics() {
		_Slicestatistics =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/sliceStatistics",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _SlicestatisticsFK=null;

	/**
	 * @return Returns the xnat:qcAssessmentData_scan_slice/slicestatistics_xnat_abstractstatistics_id.
	 */
	public Integer getSlicestatisticsFK(){
		try{
			if (_SlicestatisticsFK==null){
				_SlicestatisticsFK=getIntegerProperty("xnat:qcAssessmentData_scan_slice/slicestatistics_xnat_abstractstatistics_id");
				return _SlicestatisticsFK;
			}else {
				return _SlicestatisticsFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:qcAssessmentData_scan_slice/slicestatistics_xnat_abstractstatistics_id.
	 * @param v Value to Set.
	 */
	public void setSlicestatisticsFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/slicestatistics_xnat_abstractstatistics_id",v);
		_SlicestatisticsFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Number=null;

	/**
	 * @return Returns the number.
	 */
	public String getNumber(){
		try{
			if (_Number==null){
				_Number=getStringProperty("number");
				return _Number;
			}else {
				return _Number;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for number.
	 * @param v Value to Set.
	 */
	public void setNumber(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/number",v);
		_Number=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatQcassessmentdataScanSliceId=null;

	/**
	 * @return Returns the xnat_qcAssessmentData_scan_slice_id.
	 */
	public Integer getXnatQcassessmentdataScanSliceId() {
		try{
			if (_XnatQcassessmentdataScanSliceId==null){
				_XnatQcassessmentdataScanSliceId=getIntegerProperty("xnat_qcAssessmentData_scan_slice_id");
				return _XnatQcassessmentdataScanSliceId;
			}else {
				return _XnatQcassessmentdataScanSliceId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_qcAssessmentData_scan_slice_id.
	 * @param v Value to Set.
	 */
	public void setXnatQcassessmentdataScanSliceId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_qcAssessmentData_scan_slice_id",v);
		_XnatQcassessmentdataScanSliceId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice> getAllXnatQcassessmentdataScanSlices(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice> al = new ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice> getXnatQcassessmentdataScanSlicesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice> al = new ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice> getXnatQcassessmentdataScanSlicesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice> al = new ArrayList<org.nrg.xdat.om.XnatQcassessmentdataScanSlice>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatQcassessmentdataScanSlice getXnatQcassessmentdataScanSlicesByXnatQcassessmentdataScanSliceId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:qcAssessmentData_scan_slice/xnat_qcAssessmentData_scan_slice_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatQcassessmentdataScanSlice) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //sliceStatistics
	        XnatAbstractstatistics childSlicestatistics = (XnatAbstractstatistics)this.getSlicestatistics();
	            if (childSlicestatistics!=null){
	              for(ResourceFile rf: ((XnatAbstractstatistics)childSlicestatistics).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("sliceStatistics[" + ((XnatAbstractstatistics)childSlicestatistics).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("sliceStatistics/" + ((XnatAbstractstatistics)childSlicestatistics).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
