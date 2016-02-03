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
public abstract class AutoXnatStatisticsdataAdditionalstatistics extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatStatisticsdataAdditionalstatisticsI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatStatisticsdataAdditionalstatistics.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:statisticsData_additionalStatistics";

	public AutoXnatStatisticsdataAdditionalstatistics(ItemI item)
	{
		super(item);
	}

	public AutoXnatStatisticsdataAdditionalstatistics(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatStatisticsdataAdditionalstatistics(UserI user)
	 **/
	public AutoXnatStatisticsdataAdditionalstatistics(){}

	public AutoXnatStatisticsdataAdditionalstatistics(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:statisticsData_additionalStatistics";
	}

	//FIELD

	private Double _Additionalstatistics=null;

	/**
	 * @return Returns the additionalStatistics.
	 */
	public Double getAdditionalstatistics() {
		try{
			if (_Additionalstatistics==null){
				_Additionalstatistics=getDoubleProperty("additionalStatistics");
				return _Additionalstatistics;
			}else {
				return _Additionalstatistics;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for additionalStatistics.
	 * @param v Value to Set.
	 */
	public void setAdditionalstatistics(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/additionalStatistics",v);
		_Additionalstatistics=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Name=null;

	/**
	 * @return Returns the name.
	 */
	public String getName(){
		try{
			if (_Name==null){
				_Name=getStringProperty("name");
				return _Name;
			}else {
				return _Name;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for name.
	 * @param v Value to Set.
	 */
	public void setName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/name",v);
		_Name=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatStatisticsdataAdditionalstatisticsId=null;

	/**
	 * @return Returns the xnat_statisticsData_additionalStatistics_id.
	 */
	public Integer getXnatStatisticsdataAdditionalstatisticsId() {
		try{
			if (_XnatStatisticsdataAdditionalstatisticsId==null){
				_XnatStatisticsdataAdditionalstatisticsId=getIntegerProperty("xnat_statisticsData_additionalStatistics_id");
				return _XnatStatisticsdataAdditionalstatisticsId;
			}else {
				return _XnatStatisticsdataAdditionalstatisticsId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_statisticsData_additionalStatistics_id.
	 * @param v Value to Set.
	 */
	public void setXnatStatisticsdataAdditionalstatisticsId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_statisticsData_additionalStatistics_id",v);
		_XnatStatisticsdataAdditionalstatisticsId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics> getAllXnatStatisticsdataAdditionalstatisticss(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics> al = new ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics> getXnatStatisticsdataAdditionalstatisticssByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics> al = new ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics> getXnatStatisticsdataAdditionalstatisticssByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics> al = new ArrayList<org.nrg.xdat.om.XnatStatisticsdataAdditionalstatistics>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatStatisticsdataAdditionalstatistics getXnatStatisticsdataAdditionalstatisticssByXnatStatisticsdataAdditionalstatisticsId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:statisticsData_additionalStatistics/xnat_statisticsData_additionalStatistics_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatStatisticsdataAdditionalstatistics) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	return _return;
}
}
