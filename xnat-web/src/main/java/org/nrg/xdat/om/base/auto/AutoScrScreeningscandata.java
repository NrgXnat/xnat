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
public abstract class AutoScrScreeningscandata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ScrScreeningscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoScrScreeningscandata.class);
	public static String SCHEMA_ELEMENT_NAME="scr:screeningScanData";

	public AutoScrScreeningscandata(ItemI item)
	{
		super(item);
	}

	public AutoScrScreeningscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoScrScreeningscandata(UserI user)
	 **/
	public AutoScrScreeningscandata(){}

	public AutoScrScreeningscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "scr:screeningScanData";
	}

	//FIELD

	private String _ImagescanId=null;

	/**
	 * @return Returns the imageScan_ID.
	 */
	public String getImagescanId(){
		try{
			if (_ImagescanId==null){
				_ImagescanId=getStringProperty("imageScan_ID");
				return _ImagescanId;
			}else {
				return _ImagescanId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for imageScan_ID.
	 * @param v Value to Set.
	 */
	public void setImagescanId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/imageScan_ID",v);
		_ImagescanId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Comments=null;

	/**
	 * @return Returns the comments.
	 */
	public String getComments(){
		try{
			if (_Comments==null){
				_Comments=getStringProperty("comments");
				return _Comments;
			}else {
				return _Comments;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for comments.
	 * @param v Value to Set.
	 */
	public void setComments(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/comments",v);
		_Comments=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Pass=null;

	/**
	 * @return Returns the pass.
	 */
	public String getPass(){
		try{
			if (_Pass==null){
				_Pass=getStringProperty("pass");
				return _Pass;
			}else {
				return _Pass;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for pass.
	 * @param v Value to Set.
	 */
	public void setPass(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/pass",v);
		_Pass=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ScrScreeningscandataId=null;

	/**
	 * @return Returns the scr_screeningScanData_id.
	 */
	public Integer getScrScreeningscandataId() {
		try{
			if (_ScrScreeningscandataId==null){
				_ScrScreeningscandataId=getIntegerProperty("scr_screeningScanData_id");
				return _ScrScreeningscandataId;
			}else {
				return _ScrScreeningscandataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for scr_screeningScanData_id.
	 * @param v Value to Set.
	 */
	public void setScrScreeningscandataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/scr_screeningScanData_id",v);
		_ScrScreeningscandataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ScrScreeningscandata> getAllScrScreeningscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ScrScreeningscandata> al = new ArrayList<org.nrg.xdat.om.ScrScreeningscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ScrScreeningscandata> getScrScreeningscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ScrScreeningscandata> al = new ArrayList<org.nrg.xdat.om.ScrScreeningscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ScrScreeningscandata> getScrScreeningscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ScrScreeningscandata> al = new ArrayList<org.nrg.xdat.om.ScrScreeningscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ScrScreeningscandata getScrScreeningscandatasByScrScreeningscandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("scr:screeningScanData/scr_screeningScanData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ScrScreeningscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
