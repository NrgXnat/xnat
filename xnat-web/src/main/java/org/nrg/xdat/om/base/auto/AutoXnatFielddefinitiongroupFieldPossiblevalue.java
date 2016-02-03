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
public abstract class AutoXnatFielddefinitiongroupFieldPossiblevalue extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatFielddefinitiongroupFieldPossiblevalueI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatFielddefinitiongroupFieldPossiblevalue.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:fieldDefinitionGroup_field_possibleValue";

	public AutoXnatFielddefinitiongroupFieldPossiblevalue(ItemI item)
	{
		super(item);
	}

	public AutoXnatFielddefinitiongroupFieldPossiblevalue(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatFielddefinitiongroupFieldPossiblevalue(UserI user)
	 **/
	public AutoXnatFielddefinitiongroupFieldPossiblevalue(){}

	public AutoXnatFielddefinitiongroupFieldPossiblevalue(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:fieldDefinitionGroup_field_possibleValue";
	}

	//FIELD

	private String _Possiblevalue=null;

	/**
	 * @return Returns the possibleValue.
	 */
	public String getPossiblevalue(){
		try{
			if (_Possiblevalue==null){
				_Possiblevalue=getStringProperty("possibleValue");
				return _Possiblevalue;
			}else {
				return _Possiblevalue;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for possibleValue.
	 * @param v Value to Set.
	 */
	public void setPossiblevalue(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/possibleValue",v);
		_Possiblevalue=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Display=null;

	/**
	 * @return Returns the display.
	 */
	public String getDisplay(){
		try{
			if (_Display==null){
				_Display=getStringProperty("display");
				return _Display;
			}else {
				return _Display;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for display.
	 * @param v Value to Set.
	 */
	public void setDisplay(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/display",v);
		_Display=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatFielddefinitiongroupFieldPossiblevalueId=null;

	/**
	 * @return Returns the xnat_fieldDefinitionGroup_field_possibleValue_id.
	 */
	public Integer getXnatFielddefinitiongroupFieldPossiblevalueId() {
		try{
			if (_XnatFielddefinitiongroupFieldPossiblevalueId==null){
				_XnatFielddefinitiongroupFieldPossiblevalueId=getIntegerProperty("xnat_fieldDefinitionGroup_field_possibleValue_id");
				return _XnatFielddefinitiongroupFieldPossiblevalueId;
			}else {
				return _XnatFielddefinitiongroupFieldPossiblevalueId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_fieldDefinitionGroup_field_possibleValue_id.
	 * @param v Value to Set.
	 */
	public void setXnatFielddefinitiongroupFieldPossiblevalueId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_fieldDefinitionGroup_field_possibleValue_id",v);
		_XnatFielddefinitiongroupFieldPossiblevalueId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue> getAllXnatFielddefinitiongroupFieldPossiblevalues(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue> al = new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue> getXnatFielddefinitiongroupFieldPossiblevaluesByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue> al = new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue> getXnatFielddefinitiongroupFieldPossiblevaluesByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue> al = new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupFieldPossiblevalue>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatFielddefinitiongroupFieldPossiblevalue getXnatFielddefinitiongroupFieldPossiblevaluesByXnatFielddefinitiongroupFieldPossiblevalueId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:fieldDefinitionGroup_field_possibleValue/xnat_fieldDefinitionGroup_field_possibleValue_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatFielddefinitiongroupFieldPossiblevalue) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
