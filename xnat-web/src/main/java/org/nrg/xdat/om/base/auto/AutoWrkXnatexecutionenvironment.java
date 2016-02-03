/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:04 UTC 2016
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
public abstract class AutoWrkXnatexecutionenvironment extends WrkAbstractexecutionenvironment implements org.nrg.xdat.model.WrkXnatexecutionenvironmentI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoWrkXnatexecutionenvironment.class);
	public static String SCHEMA_ELEMENT_NAME="wrk:xnatExecutionEnvironment";

	public AutoWrkXnatexecutionenvironment(ItemI item)
	{
		super(item);
	}

	public AutoWrkXnatexecutionenvironment(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoWrkXnatexecutionenvironment(UserI user)
	 **/
	public AutoWrkXnatexecutionenvironment(){}

	public AutoWrkXnatexecutionenvironment(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "wrk:xnatExecutionEnvironment";
	}
	 private org.nrg.xdat.om.WrkAbstractexecutionenvironment _Abstractexecutionenvironment =null;

	/**
	 * abstractExecutionEnvironment
	 * @return org.nrg.xdat.om.WrkAbstractexecutionenvironment
	 */
	public org.nrg.xdat.om.WrkAbstractexecutionenvironment getAbstractexecutionenvironment() {
		try{
			if (_Abstractexecutionenvironment==null){
				_Abstractexecutionenvironment=((WrkAbstractexecutionenvironment)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("abstractExecutionEnvironment")));
				return _Abstractexecutionenvironment;
			}else {
				return _Abstractexecutionenvironment;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for abstractExecutionEnvironment.
	 * @param v Value to Set.
	 */
	public void setAbstractexecutionenvironment(ItemI v) throws Exception{
		_Abstractexecutionenvironment =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractExecutionEnvironment",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/abstractExecutionEnvironment",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * abstractExecutionEnvironment
	 * set org.nrg.xdat.model.WrkAbstractexecutionenvironmentI
	 */
	public <A extends org.nrg.xdat.model.WrkAbstractexecutionenvironmentI> void setAbstractexecutionenvironment(A item) throws Exception{
	setAbstractexecutionenvironment((ItemI)item);
	}

	/**
	 * Removes the abstractExecutionEnvironment.
	 * */
	public void removeAbstractexecutionenvironment() {
		_Abstractexecutionenvironment =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/abstractExecutionEnvironment",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Pipeline=null;

	/**
	 * @return Returns the pipeline.
	 */
	public String getPipeline(){
		try{
			if (_Pipeline==null){
				_Pipeline=getStringProperty("pipeline");
				return _Pipeline;
			}else {
				return _Pipeline;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pipeline.
	 * @param v Value to Set.
	 */
	public void setPipeline(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pipeline",v);
		_Pipeline=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Xnatuser=null;

	/**
	 * @return Returns the xnatuser.
	 */
	public String getXnatuser(){
		try{
			if (_Xnatuser==null){
				_Xnatuser=getStringProperty("xnatuser");
				return _Xnatuser;
			}else {
				return _Xnatuser;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnatuser.
	 * @param v Value to Set.
	 */
	public void setXnatuser(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnatuser",v);
		_Xnatuser=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Host=null;

	/**
	 * @return Returns the host.
	 */
	public String getHost(){
		try{
			if (_Host==null){
				_Host=getStringProperty("host");
				return _Host;
			}else {
				return _Host;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for host.
	 * @param v Value to Set.
	 */
	public void setHost(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/host",v);
		_Host=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Startat=null;

	/**
	 * @return Returns the startAt.
	 */
	public String getStartat(){
		try{
			if (_Startat==null){
				_Startat=getStringProperty("startAt");
				return _Startat;
			}else {
				return _Startat;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for startAt.
	 * @param v Value to Set.
	 */
	public void setStartat(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/startAt",v);
		_Startat=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter> _Parameters_parameter =null;

	/**
	 * parameters/parameter
	 * @return Returns an List of org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter
	 */
	public <A extends org.nrg.xdat.model.WrkXnatexecutionenvironmentParameterI> List<A> getParameters_parameter() {
		try{
			if (_Parameters_parameter==null){
				_Parameters_parameter=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("parameters/parameter"));
			}
			return (List<A>) _Parameters_parameter;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentParameter>();}
	}

	/**
	 * Sets the value for parameters/parameter.
	 * @param v Value to Set.
	 */
	public void setParameters_parameter(ItemI v) throws Exception{
		_Parameters_parameter =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/parameter",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/parameter",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * parameters/parameter
	 * Adds org.nrg.xdat.model.WrkXnatexecutionenvironmentParameterI
	 */
	public <A extends org.nrg.xdat.model.WrkXnatexecutionenvironmentParameterI> void addParameters_parameter(A item) throws Exception{
	setParameters_parameter((ItemI)item);
	}

	/**
	 * Removes the parameters/parameter of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeParameters_parameter(int index) throws java.lang.IndexOutOfBoundsException {
		_Parameters_parameter =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/parameters/parameter",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify> _Notify =null;

	/**
	 * notify
	 * @return Returns an List of org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify
	 */
	public <A extends org.nrg.xdat.model.WrkXnatexecutionenvironmentNotifyI> List<A> getNotify() {
		try{
			if (_Notify==null){
				_Notify=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("notify"));
			}
			return (List<A>) _Notify;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironmentNotify>();}
	}

	/**
	 * Sets the value for notify.
	 * @param v Value to Set.
	 */
	public void setNotify(ItemI v) throws Exception{
		_Notify =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/notify",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/notify",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * notify
	 * Adds org.nrg.xdat.model.WrkXnatexecutionenvironmentNotifyI
	 */
	public <A extends org.nrg.xdat.model.WrkXnatexecutionenvironmentNotifyI> void addNotify(A item) throws Exception{
	setNotify((ItemI)item);
	}

	/**
	 * Removes the notify of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeNotify(int index) throws java.lang.IndexOutOfBoundsException {
		_Notify =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/notify",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _Datatype=null;

	/**
	 * @return Returns the dataType.
	 */
	public String getDatatype(){
		try{
			if (_Datatype==null){
				_Datatype=getStringProperty("dataType");
				return _Datatype;
			}else {
				return _Datatype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dataType.
	 * @param v Value to Set.
	 */
	public void setDatatype(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dataType",v);
		_Datatype=null;
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

	private Boolean _Supressnotification=null;

	/**
	 * @return Returns the supressNotification.
	 */
	public Boolean getSupressnotification() {
		try{
			if (_Supressnotification==null){
				_Supressnotification=getBooleanProperty("supressNotification");
				return _Supressnotification;
			}else {
				return _Supressnotification;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for supressNotification.
	 * @param v Value to Set.
	 */
	public void setSupressnotification(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/supressNotification",v);
		_Supressnotification=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Log=null;

	/**
	 * @return Returns the log.
	 */
	public String getLog(){
		try{
			if (_Log==null){
				_Log=getStringProperty("log");
				return _Log;
			}else {
				return _Log;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for log.
	 * @param v Value to Set.
	 */
	public void setLog(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/log",v);
		_Log=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Catalogpath=null;

	/**
	 * @return Returns the catalogPath.
	 */
	public String getCatalogpath(){
		try{
			if (_Catalogpath==null){
				_Catalogpath=getStringProperty("catalogPath");
				return _Catalogpath;
			}else {
				return _Catalogpath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for catalogPath.
	 * @param v Value to Set.
	 */
	public void setCatalogpath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/catalogPath",v);
		_Catalogpath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameterfile_xml=null;

	/**
	 * @return Returns the parameterFile/xml.
	 */
	public String getParameterfile_xml(){
		try{
			if (_Parameterfile_xml==null){
				_Parameterfile_xml=getStringProperty("parameterFile/xml");
				return _Parameterfile_xml;
			}else {
				return _Parameterfile_xml;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameterFile/xml.
	 * @param v Value to Set.
	 */
	public void setParameterfile_xml(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameterFile/xml",v);
		_Parameterfile_xml=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Parameterfile_path=null;

	/**
	 * @return Returns the parameterFile/path.
	 */
	public String getParameterfile_path(){
		try{
			if (_Parameterfile_path==null){
				_Parameterfile_path=getStringProperty("parameterFile/path");
				return _Parameterfile_path;
			}else {
				return _Parameterfile_path;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for parameterFile/path.
	 * @param v Value to Set.
	 */
	public void setParameterfile_path(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/parameterFile/path",v);
		_Parameterfile_path=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironment> getAllWrkXnatexecutionenvironments(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironment> al = new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironment>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironment> getWrkXnatexecutionenvironmentsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironment> al = new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironment>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironment> getWrkXnatexecutionenvironmentsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironment> al = new ArrayList<org.nrg.xdat.om.WrkXnatexecutionenvironment>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static WrkXnatexecutionenvironment getWrkXnatexecutionenvironmentsByWrkAbstractexecutionenvironmentId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("wrk:xnatExecutionEnvironment/wrk_abstractexecutionenvironment_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (WrkXnatexecutionenvironment) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //abstractExecutionEnvironment
	        WrkAbstractexecutionenvironment childAbstractexecutionenvironment = (WrkAbstractexecutionenvironment)this.getAbstractexecutionenvironment();
	            if (childAbstractexecutionenvironment!=null){
	              for(ResourceFile rf: ((WrkAbstractexecutionenvironment)childAbstractexecutionenvironment).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("abstractExecutionEnvironment[" + ((WrkAbstractexecutionenvironment)childAbstractexecutionenvironment).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("abstractExecutionEnvironment/" + ((WrkAbstractexecutionenvironment)childAbstractexecutionenvironment).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //parameters/parameter
	        for(org.nrg.xdat.model.WrkXnatexecutionenvironmentParameterI childParameters_parameter : this.getParameters_parameter()){
	            if (childParameters_parameter!=null){
	              for(ResourceFile rf: ((WrkXnatexecutionenvironmentParameter)childParameters_parameter).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("parameters/parameter[" + ((WrkXnatexecutionenvironmentParameter)childParameters_parameter).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("parameters/parameter/" + ((WrkXnatexecutionenvironmentParameter)childParameters_parameter).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //notify
	        for(org.nrg.xdat.model.WrkXnatexecutionenvironmentNotifyI childNotify : this.getNotify()){
	            if (childNotify!=null){
	              for(ResourceFile rf: ((WrkXnatexecutionenvironmentNotify)childNotify).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("notify[" + ((WrkXnatexecutionenvironmentNotify)childNotify).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("notify/" + ((WrkXnatexecutionenvironmentNotify)childNotify).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
