/*
 * GENERATED FILE
 * Created on Thu Jan 28 18:10:06 UTC 2016
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
public abstract class AutoXnatImagescandata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatImagescandataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatImagescandata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:imageScanData";

	public AutoXnatImagescandata(ItemI item)
	{
		super(item);
	}

	public AutoXnatImagescandata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatImagescandata(UserI user)
	 **/
	public AutoXnatImagescandata(){}

	public AutoXnatImagescandata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:imageScanData";
	}
	 private ArrayList<org.nrg.xdat.om.XnatImagescandataShare> _Sharing_share =null;

	/**
	 * sharing/share
	 * @return Returns an List of org.nrg.xdat.om.XnatImagescandataShare
	 */
	public <A extends org.nrg.xdat.model.XnatImagescandataShareI> List<A> getSharing_share() {
		try{
			if (_Sharing_share==null){
				_Sharing_share=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("sharing/share"));
			}
			return (List<A>) _Sharing_share;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatImagescandataShare>();}
	}

	/**
	 * Sets the value for sharing/share.
	 * @param v Value to Set.
	 */
	public void setSharing_share(ItemI v) throws Exception{
		_Sharing_share =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sharing/share",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/sharing/share",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * sharing/share
	 * Adds org.nrg.xdat.model.XnatImagescandataShareI
	 */
	public <A extends org.nrg.xdat.model.XnatImagescandataShareI> void addSharing_share(A item) throws Exception{
	setSharing_share((ItemI)item);
	}

	/**
	 * Removes the sharing/share of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSharing_share(int index) throws java.lang.IndexOutOfBoundsException {
		_Sharing_share =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/sharing/share",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}

	//FIELD

	private String _ImageSessionId=null;

	/**
	 * @return Returns the image_session_ID.
	 */
	public String getImageSessionId(){
		try{
			if (_ImageSessionId==null){
				_ImageSessionId=getStringProperty("image_session_ID");
				return _ImageSessionId;
			}else {
				return _ImageSessionId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for image_session_ID.
	 * @param v Value to Set.
	 */
	public void setImageSessionId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/image_session_ID",v);
		_ImageSessionId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Note=null;

	/**
	 * @return Returns the note.
	 */
	public String getNote(){
		try{
			if (_Note==null){
				_Note=getStringProperty("note");
				return _Note;
			}else {
				return _Note;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for note.
	 * @param v Value to Set.
	 */
	public void setNote(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/note",v);
		_Note=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Quality=null;

	/**
	 * @return Returns the quality.
	 */
	public String getQuality(){
		try{
			if (_Quality==null){
				_Quality=getStringProperty("quality");
				return _Quality;
			}else {
				return _Quality;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for quality.
	 * @param v Value to Set.
	 */
	public void setQuality(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/quality",v);
		_Quality=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Condition=null;

	/**
	 * @return Returns the condition.
	 */
	public String getCondition(){
		try{
			if (_Condition==null){
				_Condition=getStringProperty("condition");
				return _Condition;
			}else {
				return _Condition;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for condition.
	 * @param v Value to Set.
	 */
	public void setCondition(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/condition",v);
		_Condition=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _SeriesDescription=null;

	/**
	 * @return Returns the series_description.
	 */
	public String getSeriesDescription(){
		try{
			if (_SeriesDescription==null){
				_SeriesDescription=getStringProperty("series_description");
				return _SeriesDescription;
			}else {
				return _SeriesDescription;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for series_description.
	 * @param v Value to Set.
	 */
	public void setSeriesDescription(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/series_description",v);
		_SeriesDescription=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Documentation=null;

	/**
	 * @return Returns the documentation.
	 */
	public String getDocumentation(){
		try{
			if (_Documentation==null){
				_Documentation=getStringProperty("documentation");
				return _Documentation;
			}else {
				return _Documentation;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for documentation.
	 * @param v Value to Set.
	 */
	public void setDocumentation(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/documentation",v);
		_Documentation=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Scanner=null;

	/**
	 * @return Returns the scanner.
	 */
	public String getScanner(){
		try{
			if (_Scanner==null){
				_Scanner=getStringProperty("scanner");
				return _Scanner;
			}else {
				return _Scanner;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for scanner.
	 * @param v Value to Set.
	 */
	public void setScanner(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/scanner",v);
		_Scanner=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Scanner_manufacturer=null;

	/**
	 * @return Returns the scanner/manufacturer.
	 */
	public String getScanner_manufacturer(){
		try{
			if (_Scanner_manufacturer==null){
				_Scanner_manufacturer=getStringProperty("scanner/manufacturer");
				return _Scanner_manufacturer;
			}else {
				return _Scanner_manufacturer;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for scanner/manufacturer.
	 * @param v Value to Set.
	 */
	public void setScanner_manufacturer(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/scanner/manufacturer",v);
		_Scanner_manufacturer=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Scanner_model=null;

	/**
	 * @return Returns the scanner/model.
	 */
	public String getScanner_model(){
		try{
			if (_Scanner_model==null){
				_Scanner_model=getStringProperty("scanner/model");
				return _Scanner_model;
			}else {
				return _Scanner_model;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for scanner/model.
	 * @param v Value to Set.
	 */
	public void setScanner_model(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/scanner/model",v);
		_Scanner_model=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Modality=null;

	/**
	 * @return Returns the modality.
	 */
	public String getModality(){
		try{
			if (_Modality==null){
				_Modality=getStringProperty("modality");
				return _Modality;
			}else {
				return _Modality;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for modality.
	 * @param v Value to Set.
	 */
	public void setModality(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/modality",v);
		_Modality=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Frames=null;

	/**
	 * @return Returns the frames.
	 */
	public Integer getFrames() {
		try{
			if (_Frames==null){
				_Frames=getIntegerProperty("frames");
				return _Frames;
			}else {
				return _Frames;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for frames.
	 * @param v Value to Set.
	 */
	public void setFrames(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/frames",v);
		_Frames=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Operator=null;

	/**
	 * @return Returns the operator.
	 */
	public String getOperator(){
		try{
			if (_Operator==null){
				_Operator=getStringProperty("operator");
				return _Operator;
			}else {
				return _Operator;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for operator.
	 * @param v Value to Set.
	 */
	public void setOperator(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/operator",v);
		_Operator=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatAbstractresource> _File =null;

	/**
	 * file
	 * @return Returns an List of org.nrg.xdat.om.XnatAbstractresource
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> List<A> getFile() {
		try{
			if (_File==null){
				_File=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("file"));
			}
			return (List<A>) _File;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatAbstractresource>();}
	}

	/**
	 * Sets the value for file.
	 * @param v Value to Set.
	 */
	public void setFile(ItemI v) throws Exception{
		_File =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/file",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/file",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * file
	 * Adds org.nrg.xdat.model.XnatAbstractresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> void addFile(A item) throws Exception{
	setFile((ItemI)item);
	}

	/**
	 * Removes the file of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeFile(int index) throws java.lang.IndexOutOfBoundsException {
		_File =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/file",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XnatValidationdata _Validation =null;

	/**
	 * validation
	 * @return org.nrg.xdat.om.XnatValidationdata
	 */
	public org.nrg.xdat.om.XnatValidationdata getValidation() {
		try{
			if (_Validation==null){
				_Validation=((XnatValidationdata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("validation")));
				return _Validation;
			}else {
				return _Validation;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for validation.
	 * @param v Value to Set.
	 */
	public void setValidation(ItemI v) throws Exception{
		_Validation =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/validation",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/validation",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * validation
	 * set org.nrg.xdat.model.XnatValidationdataI
	 */
	public <A extends org.nrg.xdat.model.XnatValidationdataI> void setValidation(A item) throws Exception{
	setValidation((ItemI)item);
	}

	/**
	 * Removes the validation.
	 * */
	public void removeValidation() {
		_Validation =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/validation",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ValidationFK=null;

	/**
	 * @return Returns the xnat:imageScanData/validation_xnat_validationdata_id.
	 */
	public Integer getValidationFK(){
		try{
			if (_ValidationFK==null){
				_ValidationFK=getIntegerProperty("xnat:imageScanData/validation_xnat_validationdata_id");
				return _ValidationFK;
			}else {
				return _ValidationFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:imageScanData/validation_xnat_validationdata_id.
	 * @param v Value to Set.
	 */
	public void setValidationFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/validation_xnat_validationdata_id",v);
		_ValidationFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Starttime=null;

	/**
	 * @return Returns the startTime.
	 */
	public Object getStarttime(){
		try{
			if (_Starttime==null){
				_Starttime=getProperty("startTime");
				return _Starttime;
			}else {
				return _Starttime;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for startTime.
	 * @param v Value to Set.
	 */
	public void setStarttime(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/startTime",v);
		_Starttime=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Id=null;

	/**
	 * @return Returns the ID.
	 */
	public String getId(){
		try{
			if (_Id==null){
				_Id=getStringProperty("ID");
				return _Id;
			}else {
				return _Id;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for ID.
	 * @param v Value to Set.
	 */
	public void setId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/ID",v);
		_Id=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Type=null;

	/**
	 * @return Returns the type.
	 */
	public String getType(){
		try{
			if (_Type==null){
				_Type=getStringProperty("type");
				return _Type;
			}else {
				return _Type;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for type.
	 * @param v Value to Set.
	 */
	public void setType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/type",v);
		_Type=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Uid=null;

	/**
	 * @return Returns the UID.
	 */
	public String getUid(){
		try{
			if (_Uid==null){
				_Uid=getStringProperty("UID");
				return _Uid;
			}else {
				return _Uid;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for UID.
	 * @param v Value to Set.
	 */
	public void setUid(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/UID",v);
		_Uid=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Project=null;

	/**
	 * @return Returns the project.
	 */
	public String getProject(){
		try{
			if (_Project==null){
				_Project=getStringProperty("project");
				return _Project;
			}else {
				return _Project;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for project.
	 * @param v Value to Set.
	 */
	public void setProject(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/project",v);
		_Project=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatImagescandataId=null;

	/**
	 * @return Returns the xnat_imageScanData_id.
	 */
	public Integer getXnatImagescandataId() {
		try{
			if (_XnatImagescandataId==null){
				_XnatImagescandataId=getIntegerProperty("xnat_imageScanData_id");
				return _XnatImagescandataId;
			}else {
				return _XnatImagescandataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_imageScanData_id.
	 * @param v Value to Set.
	 */
	public void setXnatImagescandataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_imageScanData_id",v);
		_XnatImagescandataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatImagescandata> getAllXnatImagescandatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImagescandata> al = new ArrayList<org.nrg.xdat.om.XnatImagescandata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImagescandata> getXnatImagescandatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImagescandata> al = new ArrayList<org.nrg.xdat.om.XnatImagescandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImagescandata> getXnatImagescandatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImagescandata> al = new ArrayList<org.nrg.xdat.om.XnatImagescandata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatImagescandata getXnatImagescandatasByXnatImagescandataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:imageScanData/xnat_imageScanData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatImagescandata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //sharing/share
	        for(org.nrg.xdat.model.XnatImagescandataShareI childSharing_share : this.getSharing_share()){
	            if (childSharing_share!=null){
	              for(ResourceFile rf: ((XnatImagescandataShare)childSharing_share).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("sharing/share[" + ((XnatImagescandataShare)childSharing_share).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("sharing/share/" + ((XnatImagescandataShare)childSharing_share).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //file
	        for(org.nrg.xdat.model.XnatAbstractresourceI childFile : this.getFile()){
	            if (childFile!=null){
	              int counterFile=0;
	              for(java.io.File f: ((XnatAbstractresource)childFile).getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("file[xnat_abstractresource_id=" + ((XnatAbstractresource)childFile).getXnatAbstractresourceId() + "]/file/" + counterFile +"");
	                 rf.setXdatPath("file/" + ((XnatAbstractresource)childFile).getXnatAbstractresourceId() + "/" + counterFile++);
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //validation
	        XnatValidationdata childValidation = (XnatValidationdata)this.getValidation();
	            if (childValidation!=null){
	              for(ResourceFile rf: ((XnatValidationdata)childValidation).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("validation[" + ((XnatValidationdata)childValidation).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("validation/" + ((XnatValidationdata)childValidation).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        localLoop = preventLoop;
	
	return _return;
}
}
