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
public abstract class AutoXnatRegionresource extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatRegionresourceI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatRegionresource.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:regionResource";

	public AutoXnatRegionresource(ItemI item)
	{
		super(item);
	}

	public AutoXnatRegionresource(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatRegionresource(UserI user)
	 **/
	public AutoXnatRegionresource(){}

	public AutoXnatRegionresource(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:regionResource";
	}
	 private org.nrg.xdat.om.XnatAbstractresource _File =null;

	/**
	 * file
	 * @return org.nrg.xdat.om.XnatAbstractresource
	 */
	public org.nrg.xdat.om.XnatAbstractresource getFile() {
		try{
			if (_File==null){
				_File=((XnatAbstractresource)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("file")));
				return _File;
			}else {
				return _File;
			}
		} catch (Exception e1) {return null;}
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
	 * set org.nrg.xdat.model.XnatAbstractresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> void setFile(A item) throws Exception{
	setFile((ItemI)item);
	}

	/**
	 * Removes the file.
	 * */
	public void removeFile() {
		_File =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/file",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _FileFK=null;

	/**
	 * @return Returns the xnat:regionResource/file_xnat_abstractresource_id.
	 */
	public Integer getFileFK(){
		try{
			if (_FileFK==null){
				_FileFK=getIntegerProperty("xnat:regionResource/file_xnat_abstractresource_id");
				return _FileFK;
			}else {
				return _FileFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:regionResource/file_xnat_abstractresource_id.
	 * @param v Value to Set.
	 */
	public void setFileFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/file_xnat_abstractresource_id",v);
		_FileFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private org.nrg.xdat.om.XnatAbstractresource _Baseimage =null;

	/**
	 * baseimage
	 * @return org.nrg.xdat.om.XnatAbstractresource
	 */
	public org.nrg.xdat.om.XnatAbstractresource getBaseimage() {
		try{
			if (_Baseimage==null){
				_Baseimage=((XnatAbstractresource)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("baseimage")));
				return _Baseimage;
			}else {
				return _Baseimage;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for baseimage.
	 * @param v Value to Set.
	 */
	public void setBaseimage(ItemI v) throws Exception{
		_Baseimage =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/baseimage",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/baseimage",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * baseimage
	 * set org.nrg.xdat.model.XnatAbstractresourceI
	 */
	public <A extends org.nrg.xdat.model.XnatAbstractresourceI> void setBaseimage(A item) throws Exception{
	setBaseimage((ItemI)item);
	}

	/**
	 * Removes the baseimage.
	 * */
	public void removeBaseimage() {
		_Baseimage =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/baseimage",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _BaseimageFK=null;

	/**
	 * @return Returns the xnat:regionResource/baseimage_xnat_abstractresource_id.
	 */
	public Integer getBaseimageFK(){
		try{
			if (_BaseimageFK==null){
				_BaseimageFK=getIntegerProperty("xnat:regionResource/baseimage_xnat_abstractresource_id");
				return _BaseimageFK;
			}else {
				return _BaseimageFK;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat:regionResource/baseimage_xnat_abstractresource_id.
	 * @param v Value to Set.
	 */
	public void setBaseimageFK(Integer v) {
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/baseimage_xnat_abstractresource_id",v);
		_BaseimageFK=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Creator_firstname=null;

	/**
	 * @return Returns the creator/firstname.
	 */
	public String getCreator_firstname(){
		try{
			if (_Creator_firstname==null){
				_Creator_firstname=getStringProperty("creator/firstname");
				return _Creator_firstname;
			}else {
				return _Creator_firstname;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for creator/firstname.
	 * @param v Value to Set.
	 */
	public void setCreator_firstname(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/creator/firstname",v);
		_Creator_firstname=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Creator_lastname=null;

	/**
	 * @return Returns the creator/lastname.
	 */
	public String getCreator_lastname(){
		try{
			if (_Creator_lastname==null){
				_Creator_lastname=getStringProperty("creator/lastname");
				return _Creator_lastname;
			}else {
				return _Creator_lastname;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for creator/lastname.
	 * @param v Value to Set.
	 */
	public void setCreator_lastname(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/creator/lastname",v);
		_Creator_lastname=null;
		} catch (Exception e1) {logger.error(e1);}
	}
	 private ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel> _Subregionlabels_label =null;

	/**
	 * subregionlabels/label
	 * @return Returns an List of org.nrg.xdat.om.XnatRegionresourceLabel
	 */
	public <A extends org.nrg.xdat.model.XnatRegionresourceLabelI> List<A> getSubregionlabels_label() {
		try{
			if (_Subregionlabels_label==null){
				_Subregionlabels_label=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("subregionlabels/label"));
			}
			return (List<A>) _Subregionlabels_label;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel>();}
	}

	/**
	 * Sets the value for subregionlabels/label.
	 * @param v Value to Set.
	 */
	public void setSubregionlabels_label(ItemI v) throws Exception{
		_Subregionlabels_label =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subregionlabels/label",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/subregionlabels/label",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * subregionlabels/label
	 * Adds org.nrg.xdat.model.XnatRegionresourceLabelI
	 */
	public <A extends org.nrg.xdat.model.XnatRegionresourceLabelI> void addSubregionlabels_label(A item) throws Exception{
	setSubregionlabels_label((ItemI)item);
	}

	/**
	 * Removes the subregionlabels/label of the given index.
	 * @param index Index of child to remove.
	 */
	public void removeSubregionlabels_label(int index) throws java.lang.IndexOutOfBoundsException {
		_Subregionlabels_label =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/subregionlabels/label",index);
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

	private String _SessionId=null;

	/**
	 * @return Returns the session_id.
	 */
	public String getSessionId(){
		try{
			if (_SessionId==null){
				_SessionId=getStringProperty("session_id");
				return _SessionId;
			}else {
				return _SessionId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for session_id.
	 * @param v Value to Set.
	 */
	public void setSessionId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/session_id",v);
		_SessionId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatRegionresourceId=null;

	/**
	 * @return Returns the xnat_regionResource_id.
	 */
	public Integer getXnatRegionresourceId() {
		try{
			if (_XnatRegionresourceId==null){
				_XnatRegionresourceId=getIntegerProperty("xnat_regionResource_id");
				return _XnatRegionresourceId;
			}else {
				return _XnatRegionresourceId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_regionResource_id.
	 * @param v Value to Set.
	 */
	public void setXnatRegionresourceId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_regionResource_id",v);
		_XnatRegionresourceId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatRegionresource> getAllXnatRegionresources(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatRegionresource> al = new ArrayList<org.nrg.xdat.om.XnatRegionresource>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatRegionresource> getXnatRegionresourcesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatRegionresource> al = new ArrayList<org.nrg.xdat.om.XnatRegionresource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatRegionresource> getXnatRegionresourcesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatRegionresource> al = new ArrayList<org.nrg.xdat.om.XnatRegionresource>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatRegionresource getXnatRegionresourcesByXnatRegionresourceId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:regionResource/xnat_regionResource_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatRegionresource) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //file
	        org.nrg.xdat.model.XnatAbstractresourceI childFile = (XnatAbstractresource)this.getFile();
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
	
	        localLoop = preventLoop;
	
	        //baseimage
	        org.nrg.xdat.model.XnatAbstractresourceI childBaseimage = (XnatAbstractresource)this.getBaseimage();
	            if (childBaseimage!=null){
	              int counterBaseimage=0;
	              for(java.io.File f: ((XnatAbstractresource)childBaseimage).getCorrespondingFiles(rootPath)){
	                 ResourceFile rf = new ResourceFile(f);
	                 rf.setXpath("baseimage[xnat_abstractresource_id=" + ((XnatAbstractresource)childBaseimage).getXnatAbstractresourceId() + "]/file/" + counterBaseimage +"");
	                 rf.setXdatPath("baseimage/" + ((XnatAbstractresource)childBaseimage).getXnatAbstractresourceId() + "/" + counterBaseimage++);
	                 rf.setSize(f.length());
	                 rf.setAbsolutePath(f.getAbsolutePath());
	                 _return.add(rf);
	              }
	            }
	
	        localLoop = preventLoop;
	
	        //subregionlabels/label
	        for(org.nrg.xdat.model.XnatRegionresourceLabelI childSubregionlabels_label : this.getSubregionlabels_label()){
	            if (childSubregionlabels_label!=null){
	              for(ResourceFile rf: ((XnatRegionresourceLabel)childSubregionlabels_label).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("subregionlabels/label[" + ((XnatRegionresourceLabel)childSubregionlabels_label).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("subregionlabels/label/" + ((XnatRegionresourceLabel)childSubregionlabels_label).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
