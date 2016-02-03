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
public abstract class AutoXnatVolumetricregionSubregion extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatVolumetricregionSubregionI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatVolumetricregionSubregion.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:volumetricRegion_subregion";

	public AutoXnatVolumetricregionSubregion(ItemI item)
	{
		super(item);
	}

	public AutoXnatVolumetricregionSubregion(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatVolumetricregionSubregion(UserI user)
	 **/
	public AutoXnatVolumetricregionSubregion(){}

	public AutoXnatVolumetricregionSubregion(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:volumetricRegion_subregion";
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

	private Double _Voxels=null;

	/**
	 * @return Returns the voxels.
	 */
	public Double getVoxels() {
		try{
			if (_Voxels==null){
				_Voxels=getDoubleProperty("voxels");
				return _Voxels;
			}else {
				return _Voxels;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for voxels.
	 * @param v Value to Set.
	 */
	public void setVoxels(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/voxels",v);
		_Voxels=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatVolumetricregionSubregionId=null;

	/**
	 * @return Returns the xnat_volumetricRegion_subregion_id.
	 */
	public Integer getXnatVolumetricregionSubregionId() {
		try{
			if (_XnatVolumetricregionSubregionId==null){
				_XnatVolumetricregionSubregionId=getIntegerProperty("xnat_volumetricRegion_subregion_id");
				return _XnatVolumetricregionSubregionId;
			}else {
				return _XnatVolumetricregionSubregionId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_volumetricRegion_subregion_id.
	 * @param v Value to Set.
	 */
	public void setXnatVolumetricregionSubregionId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_volumetricRegion_subregion_id",v);
		_XnatVolumetricregionSubregionId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion> getAllXnatVolumetricregionSubregions(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion> al = new ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion> getXnatVolumetricregionSubregionsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion> al = new ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion> getXnatVolumetricregionSubregionsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion> al = new ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatVolumetricregionSubregion getXnatVolumetricregionSubregionsByXnatVolumetricregionSubregionId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:volumetricRegion_subregion/xnat_volumetricRegion_subregion_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatVolumetricregionSubregion) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
