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
public abstract class AutoXnatStatisticsdata extends XnatAbstractstatistics implements org.nrg.xdat.model.XnatStatisticsdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatStatisticsdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:statisticsData";

	public AutoXnatStatisticsdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatStatisticsdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatStatisticsdata(UserI user)
	 **/
	public AutoXnatStatisticsdata(){}

	public AutoXnatStatisticsdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:statisticsData";
	}
	 private org.nrg.xdat.om.XnatAbstractstatistics _Abstractstatistics =null;

	/**
	 * abstractStatistics
	 * @return org.nrg.xdat.om.XnatAbstractstatistics
	 */
	public org.nrg.xdat.om.XnatAbstractstatistics getAbstractstatistics() {
		try{
			if (_Abstractstatistics==null){
				_Abstractstatistics=((XnatAbstractstatistics)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("abstractStatistics")));
				return _Abstractstatistics;
			}else {
				return _Abstractstatistics;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for abstractStatistics.
	 * @param v Value to Set.
	 */
	public void setAbstractstatistics(ItemI v) throws Exception{
		_Abstractstatistics =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractStatistics",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractStatistics",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * abstractStatistics
	 * set org.nrg.xdat.model.XnatAbstractstatisticsI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractstatisticsI> void setAbstractstatistics(A item) throws Exception{
	setAbstractstatistics((ItemI)item);
	}

	/**
	 * Removes the abstractStatistics.
	 * */
	public void removeAbstractstatistics() {
		_Abstractstatistics =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/abstractStatistics",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Mean=null;

	/**
	 * @return Returns the mean.
	 */
	public Double getMean() {
		try{
			if (_Mean==null){
				_Mean=getDoubleProperty("mean");
				return _Mean;
			}else {
				return _Mean;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for mean.
	 * @param v Value to Set.
	 */
	public void setMean(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/mean",v);
		_Mean=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Snr=null;

	/**
	 * @return Returns the snr.
	 */
	public Double getSnr() {
		try{
			if (_Snr==null){
				_Snr=getDoubleProperty("snr");
				return _Snr;
			}else {
				return _Snr;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for snr.
	 * @param v Value to Set.
	 */
	public void setSnr(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/snr",v);
		_Snr=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Min=null;

	/**
	 * @return Returns the min.
	 */
	public Double getMin() {
		try{
			if (_Min==null){
				_Min=getDoubleProperty("min");
				return _Min;
			}else {
				return _Min;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for min.
	 * @param v Value to Set.
	 */
	public void setMin(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/min",v);
		_Min=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Max=null;

	/**
	 * @return Returns the max.
	 */
	public Double getMax() {
		try{
			if (_Max==null){
				_Max=getDoubleProperty("max");
				return _Max;
			}else {
				return _Max;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for max.
	 * @param v Value to Set.
	 */
	public void setMax(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/max",v);
		_Max=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Stddev=null;

	/**
	 * @return Returns the stddev.
	 */
	public Double getStddev() {
		try{
			if (_Stddev==null){
				_Stddev=getDoubleProperty("stddev");
				return _Stddev;
			}else {
				return _Stddev;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for stddev.
	 * @param v Value to Set.
	 */
	public void setStddev(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/stddev",v);
		_Stddev=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _NoOfVoxels=null;

	/**
	 * @return Returns the no_of_voxels.
	 */
	public Integer getNoOfVoxels() {
		try{
			if (_NoOfVoxels==null){
				_NoOfVoxels=getIntegerProperty("no_of_voxels");
				return _NoOfVoxels;
			}else {
				return _NoOfVoxels;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for no_of_voxels.
	 * @param v Value to Set.
	 */
	public void setNoOfVoxels(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/no_of_voxels",v);
		_NoOfVoxels=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics> _Additionalstatistics =null;

	/**
	 * additionalStatistics
	 * @return Returns an List of org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics
	 */
	public <A extends org.nrg.xdat.model.XnatStatisticsdataAdditionalstatisticsI> List<A> getAdditionalstatistics() {
		try{
			if (_Additionalstatistics==null){
				_Additionalstatistics=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("additionalStatistics"));
			}
			return (List<A>) _Additionalstatistics;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics>();}
	}

	/**
	 * Sets the value for additionalStatistics.
	 * @param v Value to Set.
	 */
	public void setAdditionalstatistics(ItemI v) throws Exception{
		_Additionalstatistics =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/additionalStatistics",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/additionalStatistics",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * additionalStatistics
	 * Adds org.nrg.xdat.model.XnatStatisticsdataAdditionalstatisticsI
	 */
	public <A extends org.nrg.xdat.model.XnatStatisticsdataAdditionalstatisticsI> void addAdditionalstatistics(A item) throws Exception{
	setAdditionalstatistics((ItemI)item);
	}

	/**
	 * Removes the additionalStatistics of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeAdditionalstatistics(int index) throws java.lang.IndexOutOfBoundsException {
		_Additionalstatistics =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/additionalStatistics",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield> _Addfield =null;

	/**
	 * addField
	 * @return Returns an List of org.nrg.xdat.om.XnatStatisticsdataAddfield
	 */
	public <A extends org.nrg.xdat.model.XnatStatisticsdataAddfieldI> List<A> getAddfield() {
		try{
			if (_Addfield==null){
				_Addfield=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("addField"));
			}
			return (List<A>) _Addfield;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatStatisticsdataAddfield>();}
	}

	/**
	 * Sets the value for addField.
	 * @param v Value to Set.
	 */
	public void setAddfield(ItemI v) throws Exception{
		_Addfield =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/addField",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/addField",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * addField
	 * Adds org.nrg.xdat.model.XnatStatisticsdataAddfieldI
	 */
	public <A extends org.nrg.xdat.model.XnatStatisticsdataAddfieldI> void addAddfield(A item) throws Exception{
	setAddfield((ItemI)item);
	}

	/**
	 * Removes the addField of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeAddfield(int index) throws java.lang.IndexOutOfBoundsException {
		_Addfield =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/addField",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatStatisticsdata> getAllXnatStatisticsdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStatisticsdata> al = new ArrayList<org.nrg.xdat.om.XnatStatisticsdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatStatisticsdata> getXnatStatisticsdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStatisticsdata> al = new ArrayList<org.nrg.xdat.om.XnatStatisticsdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatStatisticsdata> getXnatStatisticsdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStatisticsdata> al = new ArrayList<org.nrg.xdat.om.XnatStatisticsdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatStatisticsdata getXnatStatisticsdatasByXnatAbstractstatisticsId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:statisticsData/xnat_abstractstatistics_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatStatisticsdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //abstractStatistics
	        XnatAbstractstatistics childAbstractstatistics = (XnatAbstractstatistics)this.getAbstractstatistics();
	            if (childAbstractstatistics!=null){
	              for(ResourceFile rf: ((XnatAbstractstatistics)childAbstractstatistics).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("abstractStatistics[" + ((XnatAbstractstatistics)childAbstractstatistics).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("abstractStatistics/" + ((XnatAbstractstatistics)childAbstractstatistics).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //additionalStatistics
	        for(org.nrg.xdat.model.XnatStatisticsdataAdditionalstatisticsI childAdditionalstatistics : this.getAdditionalstatistics()){
	            if (childAdditionalstatistics!=null){
	              for(ResourceFile rf: ((XnatStatisticsdataAdditionalstatistics)childAdditionalstatistics).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("additionalStatistics[" + ((XnatStatisticsdataAdditionalstatistics)childAdditionalstatistics).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("additionalStatistics/" + ((XnatStatisticsdataAdditionalstatistics)childAdditionalstatistics).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //addField
	        for(org.nrg.xdat.model.XnatStatisticsdataAddfieldI childAddfield : this.getAddfield()){
	            if (childAddfield!=null){
	              for(ResourceFile rf: ((XnatStatisticsdataAddfield)childAddfield).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("addField[" + ((XnatStatisticsdataAddfield)childAddfield).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("addField/" + ((XnatStatisticsdataAddfield)childAddfield).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
