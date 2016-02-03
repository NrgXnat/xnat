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
public abstract class AutoXnatVolumetricregion extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatVolumetricregionI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatVolumetricregion.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:volumetricRegion";

	public AutoXnatVolumetricregion(ItemI item)
	{
		super(item);
	}

	public AutoXnatVolumetricregion(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatVolumetricregion(UserI user)
	 **/
	public AutoXnatVolumetricregion(){}

	public AutoXnatVolumetricregion(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:volumetricRegion";
	}
	 private ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion> _Subregions_subregion =null;

	/**
	 * subregions/subregion
	 * @return Returns an List of org.nrg.xdat.om.XnatVolumetricregionSubregion
	 */
	public <A extends org.nrg.xdat.model.XnatVolumetricregionSubregionI> List<A> getSubregions_subregion() {
		try{
			if (_Subregions_subregion==null){
				_Subregions_subregion=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("subregions/subregion"));
			}
			return (List<A>) _Subregions_subregion;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatVolumetricregionSubregion>();}
	}

	/**
	 * Sets the value for subregions/subregion.
	 * @param v Value to Set.
	 */
	public void setSubregions_subregion(ItemI v) throws Exception{
		_Subregions_subregion =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subregions/subregion",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subregions/subregion",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * subregions/subregion
	 * Adds org.nrg.xdat.model.XnatVolumetricregionSubregionI
	 */
	public <A extends org.nrg.xdat.model.XnatVolumetricregionSubregionI> void addSubregions_subregion(A item) throws Exception{
	setSubregions_subregion((ItemI)item);
	}

	/**
	 * Removes the subregions/subregion of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSubregions_subregion(int index) throws java.lang.IndexOutOfBoundsException {
		_Subregions_subregion =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/subregions/subregion",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
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

	private Integer _Voxels=null;

	/**
	 * @return Returns the voxels.
	 */
	public Integer getVoxels() {
		try{
			if (_Voxels==null){
				_Voxels=getIntegerProperty("voxels");
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
	public void setVoxels(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/voxels",v);
		_Voxels=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Hemisphere=null;

	/**
	 * @return Returns the hemisphere.
	 */
	public String getHemisphere(){
		try{
			if (_Hemisphere==null){
				_Hemisphere=getStringProperty("hemisphere");
				return _Hemisphere;
			}else {
				return _Hemisphere;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for hemisphere.
	 * @param v Value to Set.
	 */
	public void setHemisphere(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/hemisphere",v);
		_Hemisphere=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatVolumetricregionId=null;

	/**
	 * @return Returns the xnat_volumetricRegion_id.
	 */
	public Integer getXnatVolumetricregionId() {
		try{
			if (_XnatVolumetricregionId==null){
				_XnatVolumetricregionId=getIntegerProperty("xnat_volumetricRegion_id");
				return _XnatVolumetricregionId;
			}else {
				return _XnatVolumetricregionId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_volumetricRegion_id.
	 * @param v Value to Set.
	 */
	public void setXnatVolumetricregionId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_volumetricRegion_id",v);
		_XnatVolumetricregionId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatVolumetricregion> getAllXnatVolumetricregions(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatVolumetricregion> al = new ArrayList<org.nrg.xdat.om.XnatVolumetricregion>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatVolumetricregion> getXnatVolumetricregionsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatVolumetricregion> al = new ArrayList<org.nrg.xdat.om.XnatVolumetricregion>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatVolumetricregion> getXnatVolumetricregionsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatVolumetricregion> al = new ArrayList<org.nrg.xdat.om.XnatVolumetricregion>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatVolumetricregion getXnatVolumetricregionsByXnatVolumetricregionId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:volumetricRegion/xnat_volumetricRegion_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatVolumetricregion) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //subregions/subregion
	        for(org.nrg.xdat.model.XnatVolumetricregionSubregionI childSubregions_subregion : this.getSubregions_subregion()){
	            if (childSubregions_subregion!=null){
	              for(ResourceFile rf: ((XnatVolumetricregionSubregion)childSubregions_subregion).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("subregions/subregion[" + ((XnatVolumetricregionSubregion)childSubregions_subregion).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("subregions/subregion/" + ((XnatVolumetricregionSubregion)childSubregions_subregion).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
