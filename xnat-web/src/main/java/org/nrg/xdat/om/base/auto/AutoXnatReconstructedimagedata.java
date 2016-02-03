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
public abstract class AutoXnatReconstructedimagedata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatReconstructedimagedataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatReconstructedimagedata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:reconstructedImageData";

	public AutoXnatReconstructedimagedata(ItemI item)
	{
		super(item);
	}

	public AutoXnatReconstructedimagedata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatReconstructedimagedata(UserI user)
	 **/
	public AutoXnatReconstructedimagedata(){}

	public AutoXnatReconstructedimagedata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:reconstructedImageData";
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
	 private ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid> _Inscans_scanid =null;

	/**
	 * inScans/scanID
	 * @return Returns an List of org.nrg.xdat.om.XnatReconstructedimagedataScanid
	 */
	public <A extends org.nrg.xdat.model.XnatReconstructedimagedataScanidI> List<A> getInscans_scanid() {
		try{
			if (_Inscans_scanid==null){
				_Inscans_scanid=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("inScans/scanID"));
			}
			return (List<A>) _Inscans_scanid;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatReconstructedimagedataScanid>();}
	}

	/**
	 * Sets the value for inScans/scanID.
	 * @param v Value to Set.
	 */
	public void setInscans_scanid(ItemI v) throws Exception{
		_Inscans_scanid =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/inScans/scanID",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/inScans/scanID",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * inScans/scanID
	 * Adds org.nrg.xdat.model.XnatReconstructedimagedataScanidI
	 */
	public <A extends org.nrg.xdat.model.XnatReconstructedimagedataScanidI> void addInscans_scanid(A item) throws Exception{
	setInscans_scanid((ItemI)item);
	}

	/**
	 * Removes the inScans/scanID of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeInscans_scanid(int index) throws java.lang.IndexOutOfBoundsException {
		_Inscans_scanid =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/inScans/scanID",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatAbstractresource> _In_file =null;

	/**
	 * in/file
	 * @return Returns an List of org.nrg.xdat.om.XnatAbstractresource
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> List<A> getIn_file() {
		try{
			if (_In_file==null){
				_In_file=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("in/file"));
			}
			return (List<A>) _In_file;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatAbstractresource>();}
	}

	/**
	 * Sets the value for in/file.
	 * @param v Value to Set.
	 */
	public void setIn_file(ItemI v) throws Exception{
		_In_file =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/in/file",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/in/file",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * in/file
	 * Adds org.nrg.xdat.model.XnatAbstractresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> void addIn_file(A item) throws Exception{
	setIn_file((ItemI)item);
	}

	/**
	 * Removes the in/file of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeIn_file(int index) throws java.lang.IndexOutOfBoundsException {
		_In_file =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/in/file",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatAbstractresource> _Out_file =null;

	/**
	 * out/file
	 * @return Returns an List of org.nrg.xdat.om.XnatAbstractresource
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> List<A> getOut_file() {
		try{
			if (_Out_file==null){
				_Out_file=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("out/file"));
			}
			return (List<A>) _Out_file;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatAbstractresource>();}
	}

	/**
	 * Sets the value for out/file.
	 * @param v Value to Set.
	 */
	public void setOut_file(ItemI v) throws Exception{
		_Out_file =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/out/file",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/out/file",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * out/file
	 * Adds org.nrg.xdat.model.XnatAbstractresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> void addOut_file(A item) throws Exception{
	setOut_file((ItemI)item);
	}

	/**
	 * Removes the out/file of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeOut_file(int index) throws java.lang.IndexOutOfBoundsException {
		_Out_file =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/out/file",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.ProvProcess _Provenance =null;

	/**
	 * provenance
	 * @return org.nrg.xdat.om.ProvProcess
	 */
	public org.nrg.xdat.om.ProvProcess getProvenance() {
		try{
			if (_Provenance==null){
				_Provenance=((ProvProcess)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("provenance")));
				return _Provenance;
			}else {
				return _Provenance;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for provenance.
	 * @param v Value to Set.
	 */
	public void setProvenance(ItemI v) throws Exception{
		_Provenance =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/provenance",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/provenance",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * provenance
	 * set org.nrg.xdat.model.ProvProcessI
	 */
	public <A extends org.nrg.xdat.model.ProvProcessI> void setProvenance(A item) throws Exception{
	setProvenance((ItemI)item);
	}

	/**
	 * Removes the provenance.
	 * */
	public void removeProvenance() {
		_Provenance =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/provenance",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ProvenanceFK=null;

	/**
	 * @return Returns the xnat:reconstructedImageData/provenance_prov_process_id.
	 */
	public Integer getProvenanceFK(){
		try{
			if (_ProvenanceFK==null){
				_ProvenanceFK=getIntegerProperty("xnat:reconstructedImageData/provenance_prov_process_id");
				return _ProvenanceFK;
			}else {
				return _ProvenanceFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:reconstructedImageData/provenance_prov_process_id.
	 * @param v Value to Set.
	 */
	public void setProvenanceFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/provenance_prov_process_id",v);
		_ProvenanceFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatAddfield> _Parameters_addparam =null;

	/**
	 * parameters/addParam
	 * @return Returns an List of org.nrg.xdat.om.XnatAddfield
	 */
	public <A extends org.nrg.xdat.model.XnatAddfieldI> List<A> getParameters_addparam() {
		try{
			if (_Parameters_addparam==null){
				_Parameters_addparam=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("parameters/addParam"));
			}
			return (List<A>) _Parameters_addparam;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatAddfield>();}
	}

	/**
	 * Sets the value for parameters/addParam.
	 * @param v Value to Set.
	 */
	public void setParameters_addparam(ItemI v) throws Exception{
		_Parameters_addparam =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/addParam",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/parameters/addParam",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * parameters/addParam
	 * Adds org.nrg.xdat.model.XnatAddfieldI
	 */
	public <A extends org.nrg.xdat.model.XnatAddfieldI> void addParameters_addparam(A item) throws Exception{
	setParameters_addparam((ItemI)item);
	}

	/**
	 * Removes the parameters/addParam of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeParameters_addparam(int index) throws java.lang.IndexOutOfBoundsException {
		_Parameters_addparam =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/parameters/addParam",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatComputationdata> _Computations_datum =null;

	/**
	 * computations/datum
	 * @return Returns an List of org.nrg.xdat.om.XnatComputationdata
	 */
	public <A extends org.nrg.xdat.model.XnatComputationdataI> List<A> getComputations_datum() {
		try{
			if (_Computations_datum==null){
				_Computations_datum=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("computations/datum"));
			}
			return (List<A>) _Computations_datum;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatComputationdata>();}
	}

	/**
	 * Sets the value for computations/datum.
	 * @param v Value to Set.
	 */
	public void setComputations_datum(ItemI v) throws Exception{
		_Computations_datum =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/computations/datum",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/computations/datum",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * computations/datum
	 * Adds org.nrg.xdat.model.XnatComputationdataI
	 */
	public <A extends org.nrg.xdat.model.XnatComputationdataI> void addComputations_datum(A item) throws Exception{
	setComputations_datum((ItemI)item);
	}

	/**
	 * Removes the computations/datum of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeComputations_datum(int index) throws java.lang.IndexOutOfBoundsException {
		_Computations_datum =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/computations/datum",index);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
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

	private String _Basescantype=null;

	/**
	 * @return Returns the baseScanType.
	 */
	public String getBasescantype(){
		try{
			if (_Basescantype==null){
				_Basescantype=getStringProperty("baseScanType");
				return _Basescantype;
			}else {
				return _Basescantype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for baseScanType.
	 * @param v Value to Set.
	 */
	public void setBasescantype(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/baseScanType",v);
		_Basescantype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatReconstructedimagedataId=null;

	/**
	 * @return Returns the xnat_reconstructedImageData_id.
	 */
	public Integer getXnatReconstructedimagedataId() {
		try{
			if (_XnatReconstructedimagedataId==null){
				_XnatReconstructedimagedataId=getIntegerProperty("xnat_reconstructedImageData_id");
				return _XnatReconstructedimagedataId;
			}else {
				return _XnatReconstructedimagedataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_reconstructedImageData_id.
	 * @param v Value to Set.
	 */
	public void setXnatReconstructedimagedataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_reconstructedImageData_id",v);
		_XnatReconstructedimagedataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata> getAllXnatReconstructedimagedatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata> al = new ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata> getXnatReconstructedimagedatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata> al = new ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata> getXnatReconstructedimagedatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata> al = new ArrayList<org.nrg.xdat.om.XnatReconstructedimagedata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatReconstructedimagedata getXnatReconstructedimagedatasByXnatReconstructedimagedataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:reconstructedImageData/xnat_reconstructedImageData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatReconstructedimagedata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
			else
				 return null;
		} catch (Exception e) {
			logger.error("",e);
		}

		return null;
	}

	public static XnatReconstructedimagedata getXnatReconstructedimagedatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:reconstructedImageData/ID",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatReconstructedimagedata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //inScans/scanID
	        for(org.nrg.xdat.model.XnatReconstructedimagedataScanidI childInscans_scanid : this.getInscans_scanid()){
	            if (childInscans_scanid!=null){
	              for(ResourceFile rf: ((XnatReconstructedimagedataScanid)childInscans_scanid).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("inScans/scanID[" + ((XnatReconstructedimagedataScanid)childInscans_scanid).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("inScans/scanID/" + ((XnatReconstructedimagedataScanid)childInscans_scanid).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //in/file
	        for(org.nrg.xdat.model.XnatAbstractresourceI childIn_file : this.getIn_file()){
	            if (childIn_file!=null){
	              int counterIn_file=0;
	              for(java.io.File f: ((XnatAbstractresource)childIn_file).getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("in/file[xnat_abstractresource_id=" + ((XnatAbstractresource)childIn_file).getXnatAbstractresourceId() + "]/file/" + counterIn_file +"");
	                 rf.setXdatPath("in/file/" + ((XnatAbstractresource)childIn_file).getXnatAbstractresourceId() + "/" + counterIn_file++);
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //out/file
	        for(org.nrg.xdat.model.XnatAbstractresourceI childOut_file : this.getOut_file()){
	            if (childOut_file!=null){
	              int counterOut_file=0;
	              for(java.io.File f: ((XnatAbstractresource)childOut_file).getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("out/file[xnat_abstractresource_id=" + ((XnatAbstractresource)childOut_file).getXnatAbstractresourceId() + "]/file/" + counterOut_file +"");
	                 rf.setXdatPath("out/file/" + ((XnatAbstractresource)childOut_file).getXnatAbstractresourceId() + "/" + counterOut_file++);
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //provenance
	        ProvProcess childProvenance = (ProvProcess)this.getProvenance();
	            if (childProvenance!=null){
	              for(ResourceFile rf: ((ProvProcess)childProvenance).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("provenance[" + ((ProvProcess)childProvenance).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("provenance/" + ((ProvProcess)childProvenance).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //parameters/addParam
	        for(org.nrg.xdat.model.XnatAddfieldI childParameters_addparam : this.getParameters_addparam()){
	            if (childParameters_addparam!=null){
	              for(ResourceFile rf: ((XnatAddfield)childParameters_addparam).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("parameters/addParam[" + ((XnatAddfield)childParameters_addparam).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("parameters/addParam/" + ((XnatAddfield)childParameters_addparam).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	        //computations/datum
	        for(org.nrg.xdat.model.XnatComputationdataI childComputations_datum : this.getComputations_datum()){
	            if (childComputations_datum!=null){
	              for(ResourceFile rf: ((XnatComputationdata)childComputations_datum).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("computations/datum[" + ((XnatComputationdata)childComputations_datum).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("computations/datum/" + ((XnatComputationdata)childComputations_datum).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
