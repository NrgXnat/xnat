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
public abstract class AutoXnatPvisitdata extends XnatGenericdata implements org.nrg.xdat.model.XnatPvisitdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatPvisitdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:pVisitData";

	public AutoXnatPvisitdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatPvisitdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatPvisitdata(UserI user)
	 **/
	public AutoXnatPvisitdata(){}

	public AutoXnatPvisitdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:pVisitData";
	}
	 private org.nrg.xdat.om.XnatGenericdata _Genericdata =null;

	/**
	 * genericData
	 * @return org.nrg.xdat.om.XnatGenericdata
	 */
	public org.nrg.xdat.om.XnatGenericdata getGenericdata() {
		try{
			if (_Genericdata==null){
				_Genericdata=((XnatGenericdata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("genericData")));
				return _Genericdata;
			}else {
				return _Genericdata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for genericData.
	 * @param v Value to Set.
	 */
	public void setGenericdata(ItemI v) throws Exception{
		_Genericdata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/genericData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/genericData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * genericData
	 * set org.nrg.xdat.model.XnatGenericdataI
	 */
	public <A extends org.nrg.xdat.model.XnatGenericdataI> void setGenericdata(A item) throws Exception{
	setGenericdata((ItemI)item);
	}

	/**
	 * Removes the genericData.
	 * */
	public void removeGenericdata() {
		_Genericdata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/genericData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _SubjectId=null;

	/**
	 * @return Returns the subject_ID.
	 */
	public String getSubjectId(){
		try{
			if (_SubjectId==null){
				_SubjectId=getStringProperty("subject_ID");
				return _SubjectId;
			}else {
				return _SubjectId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for subject_ID.
	 * @param v Value to Set.
	 */
	public void setSubjectId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/subject_ID",v);
		_SubjectId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _VisitType=null;

	/**
	 * @return Returns the visit_type.
	 */
	public String getVisitType(){
		try{
			if (_VisitType==null){
				_VisitType=getStringProperty("visit_type");
				return _VisitType;
			}else {
				return _VisitType;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for visit_type.
	 * @param v Value to Set.
	 */
	public void setVisitType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/visit_type",v);
		_VisitType=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _VisitName=null;

	/**
	 * @return Returns the visit_name.
	 */
	public String getVisitName(){
		try{
			if (_VisitName==null){
				_VisitName=getStringProperty("visit_name");
				return _VisitName;
			}else {
				return _VisitName;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for visit_name.
	 * @param v Value to Set.
	 */
	public void setVisitName(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/visit_name",v);
		_VisitName=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Notes=null;

	/**
	 * @return Returns the notes.
	 */
	public String getNotes(){
		try{
			if (_Notes==null){
				_Notes=getStringProperty("notes");
				return _Notes;
			}else {
				return _Notes;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for notes.
	 * @param v Value to Set.
	 */
	public void setNotes(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/notes",v);
		_Notes=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _StartDate=null;

	/**
	 * @return Returns the start_date.
	 */
	public Object getStartDate(){
		try{
			if (_StartDate==null){
				_StartDate=getProperty("start_date");
				return _StartDate;
			}else {
				return _StartDate;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for start_date.
	 * @param v Value to Set.
	 */
	public void setStartDate(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/start_date",v);
		_StartDate=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _EndDate=null;

	/**
	 * @return Returns the end_date.
	 */
	public Object getEndDate(){
		try{
			if (_EndDate==null){
				_EndDate=getProperty("end_date");
				return _EndDate;
			}else {
				return _EndDate;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for end_date.
	 * @param v Value to Set.
	 */
	public void setEndDate(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/end_date",v);
		_EndDate=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Closed=null;

	/**
	 * @return Returns the closed.
	 */
	public Boolean getClosed() {
		try{
			if (_Closed==null){
				_Closed=getBooleanProperty("closed");
				return _Closed;
			}else {
				return _Closed;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for closed.
	 * @param v Value to Set.
	 */
	public void setClosed(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/closed",v);
		_Closed=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Terminal=null;

	/**
	 * @return Returns the terminal.
	 */
	public Boolean getTerminal() {
		try{
			if (_Terminal==null){
				_Terminal=getBooleanProperty("terminal");
				return _Terminal;
			}else {
				return _Terminal;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for terminal.
	 * @param v Value to Set.
	 */
	public void setTerminal(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/terminal",v);
		_Terminal=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Status=null;

	/**
	 * @return Returns the status.
	 */
	public String getStatus(){
		try{
			if (_Status==null){
				_Status=getStringProperty("status");
				return _Status;
			}else {
				return _Status;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for status.
	 * @param v Value to Set.
	 */
	public void setStatus(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/status",v);
		_Status=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Protocolversion=null;

	/**
	 * @return Returns the protocolVersion.
	 */
	public Integer getProtocolversion() {
		try{
			if (_Protocolversion==null){
				_Protocolversion=getIntegerProperty("protocolVersion");
				return _Protocolversion;
			}else {
				return _Protocolversion;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for protocolVersion.
	 * @param v Value to Set.
	 */
	public void setProtocolversion(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/protocolVersion",v);
		_Protocolversion=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Protocolid=null;

	/**
	 * @return Returns the protocolId.
	 */
	public String getProtocolid(){
		try{
			if (_Protocolid==null){
				_Protocolid=getStringProperty("protocolId");
				return _Protocolid;
			}else {
				return _Protocolid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for protocolId.
	 * @param v Value to Set.
	 */
	public void setProtocolid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/protocolId",v);
		_Protocolid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatPvisitdata> getAllXnatPvisitdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPvisitdata> al = new ArrayList<org.nrg.xdat.om.XnatPvisitdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPvisitdata> getXnatPvisitdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPvisitdata> al = new ArrayList<org.nrg.xdat.om.XnatPvisitdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatPvisitdata> getXnatPvisitdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatPvisitdata> al = new ArrayList<org.nrg.xdat.om.XnatPvisitdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatPvisitdata getXnatPvisitdatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:pVisitData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatPvisitdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //genericData
	        XnatGenericdata childGenericdata = (XnatGenericdata)this.getGenericdata();
	            if (childGenericdata!=null){
	              for(ResourceFile rf: ((XnatGenericdata)childGenericdata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("genericData[" + ((XnatGenericdata)childGenericdata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("genericData/" + ((XnatGenericdata)childGenericdata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
