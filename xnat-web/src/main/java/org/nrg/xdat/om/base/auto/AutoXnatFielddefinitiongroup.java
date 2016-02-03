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
public abstract class AutoXnatFielddefinitiongroup extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatFielddefinitiongroupI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatFielddefinitiongroup.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:fieldDefinitionGroup";

	public AutoXnatFielddefinitiongroup(ItemI item)
	{
		super(item);
	}

	public AutoXnatFielddefinitiongroup(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatFielddefinitiongroup(UserI user)
	 **/
	public AutoXnatFielddefinitiongroup(){}

	public AutoXnatFielddefinitiongroup(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:fieldDefinitionGroup";
	}
	 private ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField> _Fields_field =null;

	/**
	 * fields/field
	 * @return Returns an List of org.nrg.xdat.om.XnatFielddefinitiongroupField
	 */
	public <A extends org.nrg.xdat.model.XnatFielddefinitiongroupFieldI> List<A> getFields_field() {
		try{
			if (_Fields_field==null){
				_Fields_field=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("fields/field"));
			}
			return (List<A>) _Fields_field;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField>();}
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
	 * Adds org.nrg.xdat.model.XnatFielddefinitiongroupFieldI
	 */
	public <A extends org.nrg.xdat.model.XnatFielddefinitiongroupFieldI> void addFields_field(A item) throws Exception{
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

	private String _DataType=null;

	/**
	 * @return Returns the data-type.
	 */
	public String getDataType(){
		try{
			if (_DataType==null){
				_DataType=getStringProperty("data-type");
				return _DataType;
			}else {
				return _DataType;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for data-type.
	 * @param v Value to Set.
	 */
	public void setDataType(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/data-type",v);
		_DataType=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Description=null;

	/**
	 * @return Returns the description.
	 */
	public String getDescription(){
		try{
			if (_Description==null){
				_Description=getStringProperty("description");
				return _Description;
			}else {
				return _Description;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for description.
	 * @param v Value to Set.
	 */
	public void setDescription(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/description",v);
		_Description=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Shareable=null;

	/**
	 * @return Returns the shareable.
	 */
	public Boolean getShareable() {
		try{
			if (_Shareable==null){
				_Shareable=getBooleanProperty("shareable");
				return _Shareable;
			}else {
				return _Shareable;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for shareable.
	 * @param v Value to Set.
	 */
	public void setShareable(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/shareable",v);
		_Shareable=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _ProjectSpecific=null;

	/**
	 * @return Returns the project-specific.
	 */
	public Boolean getProjectSpecific() {
		try{
			if (_ProjectSpecific==null){
				_ProjectSpecific=getBooleanProperty("project-specific");
				return _ProjectSpecific;
			}else {
				return _ProjectSpecific;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for project-specific.
	 * @param v Value to Set.
	 */
	public void setProjectSpecific(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/project-specific",v);
		_ProjectSpecific=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatFielddefinitiongroupId=null;

	/**
	 * @return Returns the xnat_fieldDefinitionGroup_id.
	 */
	public Integer getXnatFielddefinitiongroupId() {
		try{
			if (_XnatFielddefinitiongroupId==null){
				_XnatFielddefinitiongroupId=getIntegerProperty("xnat_fieldDefinitionGroup_id");
				return _XnatFielddefinitiongroupId;
			}else {
				return _XnatFielddefinitiongroupId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_fieldDefinitionGroup_id.
	 * @param v Value to Set.
	 */
	public void setXnatFielddefinitiongroupId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_fieldDefinitionGroup_id",v);
		_XnatFielddefinitiongroupId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup> getAllXnatFielddefinitiongroups(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup> al = new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup> getXnatFielddefinitiongroupsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup> al = new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup> getXnatFielddefinitiongroupsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup> al = new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroup>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatFielddefinitiongroup getXnatFielddefinitiongroupsByXnatFielddefinitiongroupId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:fieldDefinitionGroup/xnat_fieldDefinitionGroup_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatFielddefinitiongroup) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	        for(org.nrg.xdat.model.XnatFielddefinitiongroupFieldI childFields_field : this.getFields_field()){
	            if (childFields_field!=null){
	              for(ResourceFile rf: ((XnatFielddefinitiongroupField)childFields_field).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("fields/field[" + ((XnatFielddefinitiongroupField)childFields_field).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("fields/field/" + ((XnatFielddefinitiongroupField)childFields_field).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
