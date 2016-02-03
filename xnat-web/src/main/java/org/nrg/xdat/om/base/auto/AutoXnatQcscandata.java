/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:09 UTC 2016
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
public abstract class AutoXnatQcscandata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatQcscandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatQcscandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:qcScanData";

	public AutoXnatQcscandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatQcscandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatQcscandata(UserI user)
	 **/
	public AutoXnatQcscandata(){}

	public AutoXnatQcscandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:qcScanData";
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

	private String _Rater=null;

	/**
	 * @return Returns the rater.
	 */
	public String getRater(){
		try{
			if (_Rater==null){
				_Rater=getStringProperty("rater");
				return _Rater;
			}else {
				return _Rater;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rater.
	 * @param v Value to Set.
	 */
	public void setRater(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rater",v);
		_Rater=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Coverage=null;

	/**
	 * @return Returns the coverage.
	 */
	public String getCoverage(){
		try{
			if (_Coverage==null){
				_Coverage=getStringProperty("coverage");
				return _Coverage;
			}else {
				return _Coverage;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for coverage.
	 * @param v Value to Set.
	 */
	public void setCoverage(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/coverage",v);
		_Coverage=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Motion=null;

	/**
	 * @return Returns the motion.
	 */
	public String getMotion(){
		try{
			if (_Motion==null){
				_Motion=getStringProperty("motion");
				return _Motion;
			}else {
				return _Motion;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for motion.
	 * @param v Value to Set.
	 */
	public void setMotion(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/motion",v);
		_Motion=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Otherimageartifacts=null;

	/**
	 * @return Returns the otherImageArtifacts.
	 */
	public String getOtherimageartifacts(){
		try{
			if (_Otherimageartifacts==null){
				_Otherimageartifacts=getStringProperty("otherImageArtifacts");
				return _Otherimageartifacts;
			}else {
				return _Otherimageartifacts;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for otherImageArtifacts.
	 * @param v Value to Set.
	 */
	public void setOtherimageartifacts(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/otherImageArtifacts",v);
		_Otherimageartifacts=null;
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

	private String _Rating=null;

	/**
	 * @return Returns the rating.
	 */
	public String getRating(){
		try{
			if (_Rating==null){
				_Rating=getStringProperty("rating");
				return _Rating;
			}else {
				return _Rating;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rating.
	 * @param v Value to Set.
	 */
	public void setRating(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rating",v);
		_Rating=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Rating_scale=null;

	/**
	 * @return Returns the rating/scale.
	 */
	public String getRating_scale(){
		try{
			if (_Rating_scale==null){
				_Rating_scale=getStringProperty("rating/scale");
				return _Rating_scale;
			}else {
				return _Rating_scale;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for rating/scale.
	 * @param v Value to Set.
	 */
	public void setRating_scale(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/rating/scale",v);
		_Rating_scale=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatQcscandataField> _Fields_field =null;

	/**
	 * fields/field
	 * @return Returns an List of org.nrg.xdat.om.XnatQcscandataField
	 */
	public <A extends org.nrg.xdat.model.XnatQcscandataFieldI> List<A> getFields_field() {
		try{
			if (_Fields_field==null){
				_Fields_field=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("fields/field"));
			}
			return (List<A>) _Fields_field;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatQcscandataField>();}
	}

	/**
	 * Sets the value for fields/field.
	 * @param v Value to Set.
	 */
	public void setFields_field(ItemI v) throws Exception{
		_Fields_field =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/fields/field",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/fields/field",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * fields/field
	 * Adds org.nrg.xdat.model.XnatQcscandataFieldI
	 */
	public <A extends org.nrg.xdat.model.XnatQcscandataFieldI> void addFields_field(A item) throws Exception{
	setFields_field((ItemI)item);
	}

	/**
	 * Removes the fields/field of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeFields_field(int index) throws java.lang.IndexOutOfBoundsException {
		_Fields_field =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/fields/field",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatQcscandataId=null;

	/**
	 * @return Returns the xnat_qcScanData_id.
	 */
	public Integer getXnatQcscandataId() {
		try{
			if (_XnatQcscandataId==null){
				_XnatQcscandataId=getIntegerProperty("xnat_qcScanData_id");
				return _XnatQcscandataId;
			}else {
				return _XnatQcscandataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_qcScanData_id.
	 * @param v Value to Set.
	 */
	public void setXnatQcscandataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_qcScanData_id",v);
		_XnatQcscandataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcscandata> getAllXnatQcscandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcscandata> al = new ArrayList<org.nrg.xdat.om.XnatQcscandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcscandata> getXnatQcscandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcscandata> al = new ArrayList<org.nrg.xdat.om.XnatQcscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatQcscandata> getXnatQcscandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatQcscandata> al = new ArrayList<org.nrg.xdat.om.XnatQcscandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatQcscandata getXnatQcscandatasByXnatQcscandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:qcScanData/xnat_qcScanData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatQcscandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //fields/field
	        for(org.nrg.xdat.model.XnatQcscandataFieldI childFields_field : this.getFields_field()){
	            if (childFields_field!=null){
	              for(ResourceFile rf: ((XnatQcscandataField)childFields_field).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("fields/field[" + ((XnatQcscandataField)childFields_field).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("fields/field/" + ((XnatQcscandataField)childFields_field).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        localLoop = preventLoop;
	
	return _return;
}
}
