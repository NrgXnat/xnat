/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:10 UTC 2016
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
public abstract class AutoProvProcessstep extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ProvProcessstepI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoProvProcessstep.class);
	public static String SCHEMA_ELEMENT_NAME="prov:processStep";

	public AutoProvProcessstep(ItemI item)
	{
		super(item);
	}

	public AutoProvProcessstep(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoProvProcessstep(UserI user)
	 **/
	public AutoProvProcessstep(){}

	public AutoProvProcessstep(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "prov:processStep";
	}

	//FIELD

	private String _Program=null;

	/**
	 * @return Returns the program.
	 */
	public String getProgram(){
		try{
			if (_Program==null){
				_Program=getStringProperty("program");
				return _Program;
			}else {
				return _Program;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for program.
	 * @param v Value to Set.
	 */
	public void setProgram(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/program",v);
		_Program=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Program_version=null;

	/**
	 * @return Returns the program/version.
	 */
	public String getProgram_version(){
		try{
			if (_Program_version==null){
				_Program_version=getStringProperty("program/version");
				return _Program_version;
			}else {
				return _Program_version;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for program/version.
	 * @param v Value to Set.
	 */
	public void setProgram_version(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/program/version",v);
		_Program_version=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Program_arguments=null;

	/**
	 * @return Returns the program/arguments.
	 */
	public String getProgram_arguments(){
		try{
			if (_Program_arguments==null){
				_Program_arguments=getStringProperty("program/arguments");
				return _Program_arguments;
			}else {
				return _Program_arguments;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for program/arguments.
	 * @param v Value to Set.
	 */
	public void setProgram_arguments(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/program/arguments",v);
		_Program_arguments=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Timestamp=null;

	/**
	 * @return Returns the timestamp.
	 */
	public Object getTimestamp(){
		try{
			if (_Timestamp==null){
				_Timestamp=getProperty("timestamp");
				return _Timestamp;
			}else {
				return _Timestamp;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for timestamp.
	 * @param v Value to Set.
	 */
	public void setTimestamp(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/timestamp",v);
		_Timestamp=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Cvs=null;

	/**
	 * @return Returns the cvs.
	 */
	public String getCvs(){
		try{
			if (_Cvs==null){
				_Cvs=getStringProperty("cvs");
				return _Cvs;
			}else {
				return _Cvs;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cvs.
	 * @param v Value to Set.
	 */
	public void setCvs(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cvs",v);
		_Cvs=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _userProperty=null;

	/**
	 * @return Returns the user.
	 */
	public String getuserProperty(){
		try{
			if (_userProperty==null){
				_userProperty=getStringProperty("user");
				return _userProperty;
			}else {
				return _userProperty;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for user.
	 * @param v Value to Set.
	 */
	public void setuserProperty(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/user",v);
		_userProperty=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Machine=null;

	/**
	 * @return Returns the machine.
	 */
	public String getMachine(){
		try{
			if (_Machine==null){
				_Machine=getStringProperty("machine");
				return _Machine;
			}else {
				return _Machine;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for machine.
	 * @param v Value to Set.
	 */
	public void setMachine(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/machine",v);
		_Machine=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Platform=null;

	/**
	 * @return Returns the platform.
	 */
	public String getPlatform(){
		try{
			if (_Platform==null){
				_Platform=getStringProperty("platform");
				return _Platform;
			}else {
				return _Platform;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for platform.
	 * @param v Value to Set.
	 */
	public void setPlatform(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/platform",v);
		_Platform=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Platform_version=null;

	/**
	 * @return Returns the platform/version.
	 */
	public String getPlatform_version(){
		try{
			if (_Platform_version==null){
				_Platform_version=getStringProperty("platform/version");
				return _Platform_version;
			}else {
				return _Platform_version;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for platform/version.
	 * @param v Value to Set.
	 */
	public void setPlatform_version(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/platform/version",v);
		_Platform_version=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Compiler=null;

	/**
	 * @return Returns the compiler.
	 */
	public String getCompiler(){
		try{
			if (_Compiler==null){
				_Compiler=getStringProperty("compiler");
				return _Compiler;
			}else {
				return _Compiler;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for compiler.
	 * @param v Value to Set.
	 */
	public void setCompiler(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/compiler",v);
		_Compiler=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Compiler_version=null;

	/**
	 * @return Returns the compiler/version.
	 */
	public String getCompiler_version(){
		try{
			if (_Compiler_version==null){
				_Compiler_version=getStringProperty("compiler/version");
				return _Compiler_version;
			}else {
				return _Compiler_version;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for compiler/version.
	 * @param v Value to Set.
	 */
	public void setCompiler_version(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/compiler/version",v);
		_Compiler_version=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary> _Library =null;

	/**
	 * library
	 * @return Returns an List of org.nrg.xdat.om.ProvProcessstepLibrary
	 */
	public <A extends org.nrg.xdat.model.ProvProcessstepLibraryI> List<A> getLibrary() {
		try{
			if (_Library==null){
				_Library=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("library"));
			}
			return (List<A>) _Library;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.ProvProcessstepLibrary>();}
	}

	/**
	 * Sets the value for library.
	 * @param v Value to Set.
	 */
	public void setLibrary(ItemI v) throws Exception{
		_Library =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/library",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/library",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * library
	 * Adds org.nrg.xdat.model.ProvProcessstepLibraryI
	 */
	public <A extends org.nrg.xdat.model.ProvProcessstepLibraryI> void addLibrary(A item) throws Exception{
	setLibrary((ItemI)item);
	}

	/**
	 * Removes the library of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeLibrary(int index) throws java.lang.IndexOutOfBoundsException {
		_Library =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/library",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ProvProcessstepId=null;

	/**
	 * @return Returns the prov_processStep_id.
	 */
	public Integer getProvProcessstepId() {
		try{
			if (_ProvProcessstepId==null){
				_ProvProcessstepId=getIntegerProperty("prov_processStep_id");
				return _ProvProcessstepId;
			}else {
				return _ProvProcessstepId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for prov_processStep_id.
	 * @param v Value to Set.
	 */
	public void setProvProcessstepId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/prov_processStep_id",v);
		_ProvProcessstepId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ProvProcessstep> getAllProvProcesssteps(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ProvProcessstep> al = new ArrayList<org.nrg.xdat.om.ProvProcessstep>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ProvProcessstep> getProvProcessstepsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ProvProcessstep> al = new ArrayList<org.nrg.xdat.om.ProvProcessstep>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ProvProcessstep> getProvProcessstepsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ProvProcessstep> al = new ArrayList<org.nrg.xdat.om.ProvProcessstep>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ProvProcessstep getProvProcessstepsByProvProcessstepId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("prov:processStep/prov_processStep_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ProvProcessstep) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //library
	        for(org.nrg.xdat.model.ProvProcessstepLibraryI childLibrary : this.getLibrary()){
	            if (childLibrary!=null){
	              for(ResourceFile rf: ((ProvProcessstepLibrary)childLibrary).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("library[" + ((ProvProcessstepLibrary)childLibrary).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("library/" + ((ProvProcessstepLibrary)childLibrary).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
