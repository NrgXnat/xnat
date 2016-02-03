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
public abstract class AutoXnatDicomseriesImage extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatDicomseriesImageI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatDicomseriesImage.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:dicomSeries_image";

	public AutoXnatDicomseriesImage(ItemI item)
	{
		super(item);
	}

	public AutoXnatDicomseriesImage(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatDicomseriesImage(UserI user)
	 **/
	public AutoXnatDicomseriesImage(){}

	public AutoXnatDicomseriesImage(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:dicomSeries_image";
	}

	//FIELD

	private String _Uri=null;

	/**
	 * @return Returns the URI.
	 */
	public String getUri(){
		try{
			if (_Uri==null){
				_Uri=getStringProperty("URI");
				return _Uri;
			}else {
				return _Uri;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for URI.
	 * @param v Value to Set.
	 */
	public void setUri(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/URI",v);
		_Uri=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _SopInstanceUid=null;

	/**
	 * @return Returns the sop_instance_UID.
	 */
	public String getSopInstanceUid(){
		try{
			if (_SopInstanceUid==null){
				_SopInstanceUid=getStringProperty("sop_instance_UID");
				return _SopInstanceUid;
			}else {
				return _SopInstanceUid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for sop_instance_UID.
	 * @param v Value to Set.
	 */
	public void setSopInstanceUid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/sop_instance_UID",v);
		_SopInstanceUid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _InstanceNumber=null;

	/**
	 * @return Returns the instance_number.
	 */
	public Integer getInstanceNumber() {
		try{
			if (_InstanceNumber==null){
				_InstanceNumber=getIntegerProperty("instance_number");
				return _InstanceNumber;
			}else {
				return _InstanceNumber;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for instance_number.
	 * @param v Value to Set.
	 */
	public void setInstanceNumber(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/instance_number",v);
		_InstanceNumber=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatDicomseriesImageId=null;

	/**
	 * @return Returns the xnat_dicomSeries_image_id.
	 */
	public Integer getXnatDicomseriesImageId() {
		try{
			if (_XnatDicomseriesImageId==null){
				_XnatDicomseriesImageId=getIntegerProperty("xnat_dicomSeries_image_id");
				return _XnatDicomseriesImageId;
			}else {
				return _XnatDicomseriesImageId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_dicomSeries_image_id.
	 * @param v Value to Set.
	 */
	public void setXnatDicomseriesImageId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_dicomSeries_image_id",v);
		_XnatDicomseriesImageId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatDicomseriesImage> getAllXnatDicomseriesImages(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDicomseriesImage> al = new ArrayList<org.nrg.xdat.om.XnatDicomseriesImage>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatDicomseriesImage> getXnatDicomseriesImagesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDicomseriesImage> al = new ArrayList<org.nrg.xdat.om.XnatDicomseriesImage>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatDicomseriesImage> getXnatDicomseriesImagesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatDicomseriesImage> al = new ArrayList<org.nrg.xdat.om.XnatDicomseriesImage>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatDicomseriesImage getXnatDicomseriesImagesByXnatDicomseriesImageId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:dicomSeries_image/xnat_dicomSeries_image_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatDicomseriesImage) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XnatDicomseriesImage getXnatDicomseriesImagesBySopInstanceUid(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:dicomSeries_image/sop_instance_UID",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatDicomseriesImage) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
