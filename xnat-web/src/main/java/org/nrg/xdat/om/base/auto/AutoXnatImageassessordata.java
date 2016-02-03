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
public abstract class AutoXnatImageassessordata extends XnatDeriveddata implements org.nrg.xdat.model.XnatImageassessordataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatImageassessordata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:imageAssessorData";

	public AutoXnatImageassessordata(ItemI item)
	{
		super(item);
	}

	public AutoXnatImageassessordata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatImageassessordata(UserI user)
	 **/
	public AutoXnatImageassessordata(){}

	public AutoXnatImageassessordata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:imageAssessorData";
	}
	 private org.nrg.xdat.om.XnatDeriveddata _Deriveddata =null;

	/**
	 * derivedData
	 * @return org.nrg.xdat.om.XnatDeriveddata
	 */
	public org.nrg.xdat.om.XnatDeriveddata getDeriveddata() {
		try{
			if (_Deriveddata==null){
				_Deriveddata=((XnatDeriveddata)org.nrg.xdat.base.BaseElement.GetGeneratedItem((XFTItem)getProperty("derivedData")));
				return _Deriveddata;
			}else {
				return _Deriveddata;
			}
		} catch (Exception e1) {return null;}
	}

	/**
	 * Sets the value for derivedData.
	 * @param v Value to Set.
	 */
	public void setDeriveddata(ItemI v) throws Exception{
		_Deriveddata =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/derivedData",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/derivedData",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * derivedData
	 * set org.nrg.xdat.model.XnatDeriveddataI
	 */
	public <A extends org.nrg.xdat.model.XnatDeriveddataI> void setDeriveddata(A item) throws Exception{
	setDeriveddata((ItemI)item);
	}

	/**
	 * Removes the derivedData.
	 * */
	public void removeDeriveddata() {
		_Deriveddata =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/derivedData",0);
		} catch (FieldNotFoundException e1) {logger.error(e1);}
		catch (java.lang.IndexOutOfBoundsException e1) {logger.error(e1);}
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

	//FIELD

	private String _ImagesessionId=null;

	/**
	 * @return Returns the imageSession_ID.
	 */
	public String getImagesessionId(){
		try{
			if (_ImagesessionId==null){
				_ImagesessionId=getStringProperty("imageSession_ID");
				return _ImagesessionId;
			}else {
				return _ImagesessionId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for imageSession_ID.
	 * @param v Value to Set.
	 */
	public void setImagesessionId(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/imageSession_ID",v);
		_ImagesessionId=null;
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

	public static ArrayList<org.nrg.xdat.om.XnatImageassessordata> getAllXnatImageassessordatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImageassessordata> al = new ArrayList<org.nrg.xdat.om.XnatImageassessordata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImageassessordata> getXnatImageassessordatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImageassessordata> al = new ArrayList<org.nrg.xdat.om.XnatImageassessordata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatImageassessordata> getXnatImageassessordatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatImageassessordata> al = new ArrayList<org.nrg.xdat.om.XnatImageassessordata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatImageassessordata getXnatImageassessordatasById(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:imageAssessorData/id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatImageassessordata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //derivedData
	        XnatDeriveddata childDeriveddata = (XnatDeriveddata)this.getDeriveddata();
	            if (childDeriveddata!=null){
	              for(ResourceFile rf: ((XnatDeriveddata)childDeriveddata).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("derivedData[" + ((XnatDeriveddata)childDeriveddata).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("derivedData/" + ((XnatDeriveddata)childDeriveddata).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
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
	
	return _return;
}
}
