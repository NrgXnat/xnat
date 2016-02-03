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
public abstract class AutoArcPathinfo extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ArcPathinfoI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcPathinfo.class);
	public static String SCHEMA_ELEMENT_NAME="arc:pathInfo";

	public AutoArcPathinfo(ItemI item)
	{
		super(item);
	}

	public AutoArcPathinfo(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcPathinfo(UserI user)
	 **/
	public AutoArcPathinfo(){}

	public AutoArcPathinfo(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:pathInfo";
	}

	//FIELD

	private String _Archivepath=null;

	/**
	 * @return Returns the archivePath.
	 */
	public String getArchivepath(){
		try{
			if (_Archivepath==null){
				_Archivepath=getStringProperty("archivePath");
				return _Archivepath;
			}else {
				return _Archivepath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for archivePath.
	 * @param v Value to Set.
	 */
	public void setArchivepath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/archivePath",v);
		_Archivepath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Prearchivepath=null;

	/**
	 * @return Returns the prearchivePath.
	 */
	public String getPrearchivepath(){
		try{
			if (_Prearchivepath==null){
				_Prearchivepath=getStringProperty("prearchivePath");
				return _Prearchivepath;
			}else {
				return _Prearchivepath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for prearchivePath.
	 * @param v Value to Set.
	 */
	public void setPrearchivepath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/prearchivePath",v);
		_Prearchivepath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Cachepath=null;

	/**
	 * @return Returns the cachePath.
	 */
	public String getCachepath(){
		try{
			if (_Cachepath==null){
				_Cachepath=getStringProperty("cachePath");
				return _Cachepath;
			}else {
				return _Cachepath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for cachePath.
	 * @param v Value to Set.
	 */
	public void setCachepath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/cachePath",v);
		_Cachepath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Buildpath=null;

	/**
	 * @return Returns the buildPath.
	 */
	public String getBuildpath(){
		try{
			if (_Buildpath==null){
				_Buildpath=getStringProperty("buildPath");
				return _Buildpath;
			}else {
				return _Buildpath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for buildPath.
	 * @param v Value to Set.
	 */
	public void setBuildpath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/buildPath",v);
		_Buildpath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Ftppath=null;

	/**
	 * @return Returns the ftpPath.
	 */
	public String getFtppath(){
		try{
			if (_Ftppath==null){
				_Ftppath=getStringProperty("ftpPath");
				return _Ftppath;
			}else {
				return _Ftppath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ftpPath.
	 * @param v Value to Set.
	 */
	public void setFtppath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ftpPath",v);
		_Ftppath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Pipelinepath=null;

	/**
	 * @return Returns the pipelinePath.
	 */
	public String getPipelinepath(){
		try{
			if (_Pipelinepath==null){
				_Pipelinepath=getStringProperty("pipelinePath");
				return _Pipelinepath;
			}else {
				return _Pipelinepath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pipelinePath.
	 * @param v Value to Set.
	 */
	public void setPipelinepath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pipelinePath",v);
		_Pipelinepath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ArcPathinfoId=null;

	/**
	 * @return Returns the arc_pathInfo_id.
	 */
	public Integer getArcPathinfoId() {
		try{
			if (_ArcPathinfoId==null){
				_ArcPathinfoId=getIntegerProperty("arc_pathInfo_id");
				return _ArcPathinfoId;
			}else {
				return _ArcPathinfoId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc_pathInfo_id.
	 * @param v Value to Set.
	 */
	public void setArcPathinfoId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/arc_pathInfo_id",v);
		_ArcPathinfoId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcPathinfo> getAllArcPathinfos(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcPathinfo> al = new ArrayList<org.nrg.xdat.om.ArcPathinfo>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcPathinfo> getArcPathinfosByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcPathinfo> al = new ArrayList<org.nrg.xdat.om.ArcPathinfo>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcPathinfo> getArcPathinfosByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcPathinfo> al = new ArrayList<org.nrg.xdat.om.ArcPathinfo>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcPathinfo getArcPathinfosByArcPathinfoId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:pathInfo/arc_pathInfo_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcPathinfo) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
