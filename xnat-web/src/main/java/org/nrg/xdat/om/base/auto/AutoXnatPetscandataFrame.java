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
public abstract class AutoXnatPetscandataFrame extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatPetscandataFrameI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatPetscandataFrame.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:petScanData_frame";

	public AutoXnatPetscandataFrame(ItemI item)
	{
		super(item);
	}

	public AutoXnatPetscandataFrame(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatPetscandataFrame(UserI user)
	 **/
	public AutoXnatPetscandataFrame(){}

	public AutoXnatPetscandataFrame(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:petScanData_frame";
	}

	//FIELD

	private Object _Number=null;

	/**
	 * @return Returns the number.
	 */
	public Object getNumber(){
		try{
			if (_Number==null){
				_Number=getProperty("number");
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
	public void setNumber(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/number",v);
		_Number=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Starttime=null;

	/**
	 * @return Returns the starttime.
	 */
	public Double getStarttime() {
		try{
			if (_Starttime==null){
				_Starttime=getDoubleProperty("starttime");
				return _Starttime;
			}else {
				return _Starttime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for starttime.
	 * @param v Value to Set.
	 */
	public void setStarttime(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/starttime",v);
		_Starttime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Length=null;

	/**
	 * @return Returns the length.
	 */
	public Double getLength() {
		try{
			if (_Length==null){
				_Length=getDoubleProperty("length");
				return _Length;
			}else {
				return _Length;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for length.
	 * @param v Value to Set.
	 */
	public void setLength(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/length",v);
		_Length=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Units=null;

	/**
	 * @return Returns the units.
	 */
	public String getUnits(){
		try{
			if (_Units==null){
				_Units=getStringProperty("units");
				return _Units;
			}else {
				return _Units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for units.
	 * @param v Value to Set.
	 */
	public void setUnits(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/units",v);
		_Units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatPetscandataFrameId=null;

	/**
	 * @return Returns the xnat_petScanData_frame_id.
	 */
	public Integer getXnatPetscandataFrameId() {
		try{
			if (_XnatPetscandataFrameId==null){
				_XnatPetscandataFrameId=getIntegerProperty("xnat_petScanData_frame_id");
				return _XnatPetscandataFrameId;
			}else {
				return _XnatPetscandataFrameId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_petScanData_frame_id.
	 * @param v Value to Set.
	 */
	public void setXnatPetscandataFrameId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_petScanData_frame_id",v);
		_XnatPetscandataFrameId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetscandataFrame> getAllXnatPetscandataFrames(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetscandataFrame> al = new ArrayList<org.nrg.xdat.om.XnatPetscandataFrame>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetscandataFrame> getXnatPetscandataFramesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetscandataFrame> al = new ArrayList<org.nrg.xdat.om.XnatPetscandataFrame>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPetscandataFrame> getXnatPetscandataFramesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPetscandataFrame> al = new ArrayList<org.nrg.xdat.om.XnatPetscandataFrame>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatPetscandataFrame getXnatPetscandataFramesByXnatPetscandataFrameId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:petScanData_frame/xnat_petScanData_frame_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPetscandataFrame) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
