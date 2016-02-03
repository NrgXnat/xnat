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
public abstract class AutoXnatImageresource extends XnatResource implements org.nrg.xdat.model.XnatImageresourceI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatImageresource.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:imageResource";

	public AutoXnatImageresource(ItemI item)
	{
		super(item);
	}

	public AutoXnatImageresource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatImageresource(UserI user)
	 **/
	public AutoXnatImageresource(){}

	public AutoXnatImageresource(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:imageResource";
	}
	 private org.nrg.xdat.om.XnatResource _Resource =null;

	/**
	 * resource
	 * @return org.nrg.xdat.om.XnatResource
	 */
	public org.nrg.xdat.om.XnatResource getResource() {
		try{
			if (_Resource==null){
				_Resource=((XnatResource)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("resource")));
				return _Resource;
			}else {
				return _Resource;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for resource.
	 * @param v Value to Set.
	 */
	public void setResource(ItemI v) throws Exception{
		_Resource =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/resource",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/resource",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * resource
	 * set org.nrg.xdat.model.XnatResourceI
	 */
	public <A extends org.nrg.xdat.model.XnatResourceI> void setResource(A item) throws Exception{
	setResource((ItemI)item);
	}

	/**
	 * Removes the resource.
	 * */
	public void removeResource() {
		_Resource =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/resource",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Dimensions_x=null;

	/**
	 * @return Returns the dimensions/x.
	 */
	public Integer getDimensions_x() {
		try{
			if (_Dimensions_x==null){
				_Dimensions_x=getIntegerProperty("dimensions/x");
				return _Dimensions_x;
			}else {
				return _Dimensions_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dimensions/x.
	 * @param v Value to Set.
	 */
	public void setDimensions_x(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dimensions/x",v);
		_Dimensions_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Dimensions_y=null;

	/**
	 * @return Returns the dimensions/y.
	 */
	public Integer getDimensions_y() {
		try{
			if (_Dimensions_y==null){
				_Dimensions_y=getIntegerProperty("dimensions/y");
				return _Dimensions_y;
			}else {
				return _Dimensions_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dimensions/y.
	 * @param v Value to Set.
	 */
	public void setDimensions_y(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dimensions/y",v);
		_Dimensions_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Dimensions_z=null;

	/**
	 * @return Returns the dimensions/z.
	 */
	public Integer getDimensions_z() {
		try{
			if (_Dimensions_z==null){
				_Dimensions_z=getIntegerProperty("dimensions/z");
				return _Dimensions_z;
			}else {
				return _Dimensions_z;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dimensions/z.
	 * @param v Value to Set.
	 */
	public void setDimensions_z(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dimensions/z",v);
		_Dimensions_z=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Dimensions_volumes=null;

	/**
	 * @return Returns the dimensions/volumes.
	 */
	public Integer getDimensions_volumes() {
		try{
			if (_Dimensions_volumes==null){
				_Dimensions_volumes=getIntegerProperty("dimensions/volumes");
				return _Dimensions_volumes;
			}else {
				return _Dimensions_volumes;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for dimensions/volumes.
	 * @param v Value to Set.
	 */
	public void setDimensions_volumes(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/dimensions/volumes",v);
		_Dimensions_volumes=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Voxelres_x=null;

	/**
	 * @return Returns the voxelRes/x.
	 */
	public Double getVoxelres_x() {
		try{
			if (_Voxelres_x==null){
				_Voxelres_x=getDoubleProperty("voxelRes/x");
				return _Voxelres_x;
			}else {
				return _Voxelres_x;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for voxelRes/x.
	 * @param v Value to Set.
	 */
	public void setVoxelres_x(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/voxelRes/x",v);
		_Voxelres_x=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Voxelres_y=null;

	/**
	 * @return Returns the voxelRes/y.
	 */
	public Double getVoxelres_y() {
		try{
			if (_Voxelres_y==null){
				_Voxelres_y=getDoubleProperty("voxelRes/y");
				return _Voxelres_y;
			}else {
				return _Voxelres_y;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for voxelRes/y.
	 * @param v Value to Set.
	 */
	public void setVoxelres_y(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/voxelRes/y",v);
		_Voxelres_y=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Double _Voxelres_z=null;

	/**
	 * @return Returns the voxelRes/z.
	 */
	public Double getVoxelres_z() {
		try{
			if (_Voxelres_z==null){
				_Voxelres_z=getDoubleProperty("voxelRes/z");
				return _Voxelres_z;
			}else {
				return _Voxelres_z;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for voxelRes/z.
	 * @param v Value to Set.
	 */
	public void setVoxelres_z(Double v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/voxelRes/z",v);
		_Voxelres_z=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Voxelres_units=null;

	/**
	 * @return Returns the voxelRes/units.
	 */
	public String getVoxelres_units(){
		try{
			if (_Voxelres_units==null){
				_Voxelres_units=getStringProperty("voxelRes/units");
				return _Voxelres_units;
			}else {
				return _Voxelres_units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for voxelRes/units.
	 * @param v Value to Set.
	 */
	public void setVoxelres_units(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/voxelRes/units",v);
		_Voxelres_units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Orientation=null;

	/**
	 * @return Returns the orientation.
	 */
	public String getOrientation(){
		try{
			if (_Orientation==null){
				_Orientation=getStringProperty("orientation");
				return _Orientation;
			}else {
				return _Orientation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for orientation.
	 * @param v Value to Set.
	 */
	public void setOrientation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/orientation",v);
		_Orientation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatImageresource> getAllXnatImageresources(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImageresource> al = new ArrayList<org.nrg.xdat.om.XnatImageresource>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImageresource> getXnatImageresourcesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImageresource> al = new ArrayList<org.nrg.xdat.om.XnatImageresource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImageresource> getXnatImageresourcesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImageresource> al = new ArrayList<org.nrg.xdat.om.XnatImageresource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatImageresource getXnatImageresourcesByXnatAbstractresourceId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:imageResource/xnat_abstractresource_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatImageresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	              int counter=0;
	              for(java.io.File f: this.getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("file/" + counter +"");
	                 rf.setXdatPath((counter++) +"");
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	return _return;
}
}
