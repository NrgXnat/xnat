/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:06 UTC 2016
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
public abstract class AutoXnatContrastbolus extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatContrastbolusI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatContrastbolus.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:contrastBolus";

	public AutoXnatContrastbolus(ItemI item)
	{
		super(item);
	}

	public AutoXnatContrastbolus(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatContrastbolus(UserI user)
	 **/
	public AutoXnatContrastbolus(){}

	public AutoXnatContrastbolus(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:contrastBolus";
	}

	//FIELD

	private String _Agent=null;

	/**
	 * @return Returns the agent.
	 */
	public String getAgent(){
		try{
			if (_Agent==null){
				_Agent=getStringProperty("agent");
				return _Agent;
			}else {
				return _Agent;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for agent.
	 * @param v Value to Set.
	 */
	public void setAgent(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/agent",v);
		_Agent=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Route=null;

	/**
	 * @return Returns the route.
	 */
	public String getRoute(){
		try{
			if (_Route==null){
				_Route=getStringProperty("route");
				return _Route;
			}else {
				return _Route;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for route.
	 * @param v Value to Set.
	 */
	public void setRoute(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/route",v);
		_Route=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Volume=null;

	/**
	 * @return Returns the volume.
	 */
	public Double getVolume() {
		try{
			if (_Volume==null){
				_Volume=getDoubleProperty("volume");
				return _Volume;
			}else {
				return _Volume;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for volume.
	 * @param v Value to Set.
	 */
	public void setVolume(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/volume",v);
		_Volume=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Totaldose=null;

	/**
	 * @return Returns the totalDose.
	 */
	public Double getTotaldose() {
		try{
			if (_Totaldose==null){
				_Totaldose=getDoubleProperty("totalDose");
				return _Totaldose;
			}else {
				return _Totaldose;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for totalDose.
	 * @param v Value to Set.
	 */
	public void setTotaldose(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/totalDose",v);
		_Totaldose=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Flowrate=null;

	/**
	 * @return Returns the flowRate.
	 */
	public Double getFlowrate() {
		try{
			if (_Flowrate==null){
				_Flowrate=getDoubleProperty("flowRate");
				return _Flowrate;
			}else {
				return _Flowrate;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for flowRate.
	 * @param v Value to Set.
	 */
	public void setFlowrate(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/flowRate",v);
		_Flowrate=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Flowduration=null;

	/**
	 * @return Returns the flowDuration.
	 */
	public Double getFlowduration() {
		try{
			if (_Flowduration==null){
				_Flowduration=getDoubleProperty("flowDuration");
				return _Flowduration;
			}else {
				return _Flowduration;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for flowDuration.
	 * @param v Value to Set.
	 */
	public void setFlowduration(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/flowDuration",v);
		_Flowduration=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Activeingredient=null;

	/**
	 * @return Returns the activeIngredient.
	 */
	public String getActiveingredient(){
		try{
			if (_Activeingredient==null){
				_Activeingredient=getStringProperty("activeIngredient");
				return _Activeingredient;
			}else {
				return _Activeingredient;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for activeIngredient.
	 * @param v Value to Set.
	 */
	public void setActiveingredient(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/activeIngredient",v);
		_Activeingredient=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Concentration=null;

	/**
	 * @return Returns the concentration.
	 */
	public Double getConcentration() {
		try{
			if (_Concentration==null){
				_Concentration=getDoubleProperty("concentration");
				return _Concentration;
			}else {
				return _Concentration;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for concentration.
	 * @param v Value to Set.
	 */
	public void setConcentration(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/concentration",v);
		_Concentration=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatContrastbolusId=null;

	/**
	 * @return Returns the xnat_contrastBolus_id.
	 */
	public Integer getXnatContrastbolusId() {
		try{
			if (_XnatContrastbolusId==null){
				_XnatContrastbolusId=getIntegerProperty("xnat_contrastBolus_id");
				return _XnatContrastbolusId;
			}else {
				return _XnatContrastbolusId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_contrastBolus_id.
	 * @param v Value to Set.
	 */
	public void setXnatContrastbolusId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_contrastBolus_id",v);
		_XnatContrastbolusId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatContrastbolus> getAllXnatContrastboluss(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatContrastbolus> al = new ArrayList<org.nrg.xdat.om.XnatContrastbolus>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatContrastbolus> getXnatContrastbolussByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatContrastbolus> al = new ArrayList<org.nrg.xdat.om.XnatContrastbolus>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatContrastbolus> getXnatContrastbolussByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatContrastbolus> al = new ArrayList<org.nrg.xdat.om.XnatContrastbolus>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatContrastbolus getXnatContrastbolussByXnatContrastbolusId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:contrastBolus/xnat_contrastBolus_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatContrastbolus) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
