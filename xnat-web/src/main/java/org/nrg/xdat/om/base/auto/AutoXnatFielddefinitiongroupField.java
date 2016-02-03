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
public abstract class AutoXnatFielddefinitiongroupField extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatFielddefinitiongroupFieldI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatFielddefinitiongroupField.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:fieldDefinitionGroup_field";

	public AutoXnatFielddefinitiongroupField(ItemI item)
	{
		super(item);
	}

	public AutoXnatFielddefinitiongroupField(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatFielddefinitiongroupField(UserI user)
	 **/
	public AutoXnatFielddefinitiongroupField(){}

	public AutoXnatFielddefinitiongroupField(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:fieldDefinitionGroup_field";
	}
	 private ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue> _Possiblevalues_possiblevalue =null;

	/**
	 * possibleValues/possibleValue
	 * @return Returns an List of org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue
	 */
	public <A extends org.nrg.xdat.model.XnatFielddefinitiongroupFieldPossiblevalueI> List<A> getPossiblevalues_possiblevalue() {
		try{
			if (_Possiblevalues_possiblevalue==null){
				_Possiblevalues_possiblevalue=org.nrg.xdat.base.BaseElement.WrapItems(getChildItems("possibleValues/possibleValue"));
			}
			return (List<A>) _Possiblevalues_possiblevalue;
		} catch (Exception e1) {return (List<A>) new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue>();}
	}

	/**
	 * Sets the value for possibleValues/possibleValue.
	 * @param v Value to Set.
	 */
	public void setPossiblevalues_possiblevalue(ItemI v) throws Exception{
		_Possiblevalues_possiblevalue =null;
		try{
			if (v instanceof XFTItem)
			{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/possibleValues/possibleValue",v,true);
			}else{
				getItem().setChild(SCHEMA_ELEMENT_NAME + "/possibleValues/possibleValue",v.getItem(),true);
			}
		} catch (Exception e1) {logger.error(e1);throw e1;}
	}

	/**
	 * possibleValues/possibleValue
	 * Adds org.nrg.xdat.model.XnatFielddefinitiongroupFieldPossiblevalueI
	 */
	public <A extends org.nrg.xdat.model.XnatFielddefinitiongroupFieldPossiblevalueI> void addPossiblevalues_possiblevalue(A item) throws Exception{
	setPossiblevalues_possiblevalue((ItemI)item);
	}

	/**
	 * Removes the possibleValues/possibleValue of the given index.
	 * @param index Index of child to remove.
	 */
	public void removePossiblevalues_possiblevalue(int index) throws java.lang.IndexOutOfBoundsException {
		_Possiblevalues_possiblevalue =null;
		try{
			getItem().removeChild(SCHEMA_ELEMENT_NAME + "/possibleValues/possibleValue",index);
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

	private String _Datatype=null;

	/**
	 * @return Returns the datatype.
	 */
	public String getDatatype(){
		try{
			if (_Datatype==null){
				_Datatype=getStringProperty("datatype");
				return _Datatype;
			}else {
				return _Datatype;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for datatype.
	 * @param v Value to Set.
	 */
	public void setDatatype(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/datatype",v);
		_Datatype=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Required=null;

	/**
	 * @return Returns the required.
	 */
	public Boolean getRequired() {
		try{
			if (_Required==null){
				_Required=getBooleanProperty("required");
				return _Required;
			}else {
				return _Required;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for required.
	 * @param v Value to Set.
	 */
	public void setRequired(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/required",v);
		_Required=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _Sequence=null;

	/**
	 * @return Returns the sequence.
	 */
	public Integer getSequence() {
		try{
			if (_Sequence==null){
				_Sequence=getIntegerProperty("sequence");
				return _Sequence;
			}else {
				return _Sequence;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for sequence.
	 * @param v Value to Set.
	 */
	public void setSequence(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/sequence",v);
		_Sequence=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Xmlpath=null;

	/**
	 * @return Returns the xmlPath.
	 */
	public String getXmlpath(){
		try{
			if (_Xmlpath==null){
				_Xmlpath=getStringProperty("xmlPath");
				return _Xmlpath;
			}else {
				return _Xmlpath;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xmlPath.
	 * @param v Value to Set.
	 */
	public void setXmlpath(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xmlPath",v);
		_Xmlpath=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Group=null;

	/**
	 * @return Returns the group.
	 */
	public String getGroup(){
		try{
			if (_Group==null){
				_Group=getStringProperty("group");
				return _Group;
			}else {
				return _Group;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for group.
	 * @param v Value to Set.
	 */
	public void setGroup(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/group",v);
		_Group=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatFielddefinitiongroupFieldId=null;

	/**
	 * @return Returns the xnat_fieldDefinitionGroup_field_id.
	 */
	public Integer getXnatFielddefinitiongroupFieldId() {
		try{
			if (_XnatFielddefinitiongroupFieldId==null){
				_XnatFielddefinitiongroupFieldId=getIntegerProperty("xnat_fieldDefinitionGroup_field_id");
				return _XnatFielddefinitiongroupFieldId;
			}else {
				return _XnatFielddefinitiongroupFieldId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_fieldDefinitionGroup_field_id.
	 * @param v Value to Set.
	 */
	public void setXnatFielddefinitiongroupFieldId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_fieldDefinitionGroup_field_id",v);
		_XnatFielddefinitiongroupFieldId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField> getAllXnatFielddefinitiongroupFields(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField> al = new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField> getXnatFielddefinitiongroupFieldsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField> al = new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField> getXnatFielddefinitiongroupFieldsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField> al = new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatFielddefinitiongroupField getXnatFielddefinitiongroupFieldsByXnatFielddefinitiongroupFieldId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:fieldDefinitionGroup_field/xnat_fieldDefinitionGroup_field_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatFielddefinitiongroupField) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
	
	        //possibleValues/possibleValue
	        for(org.nrg.xdat.model.XnatFielddefinitiongroupFieldPossiblevalueI childPossiblevalues_possiblevalue : this.getPossiblevalues_possiblevalue()){
	            if (childPossiblevalues_possiblevalue!=null){
	              for(ResourceFile rf: ((XnatFielddefinitiongroupFieldPossiblevalue)childPossiblevalues_possiblevalue).getFileResources(rootPath, localLoop)) {
	                 rf.setXpath("possibleValues/possibleValue[" + ((XnatFielddefinitiongroupFieldPossiblevalue)childPossiblevalues_possiblevalue).getItem().getPKString() + "]/" + rf.getXpath());
	                 rf.setXdatPath("possibleValues/possibleValue/" + ((XnatFielddefinitiongroupFieldPossiblevalue)childPossiblevalues_possiblevalue).getItem().getPKString() + "/" + rf.getXpath());
	                 _return.add(rf);
	              }
	            }
	        }
	
	        localLoop = preventLoop;
	
	return _return;
}
}
