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
public abstract class AutoArcProperty extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ArcPropertyI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcProperty.class);
	public static String SCHEMA_ELEMENT_NAME="arc:property";

	public AutoArcProperty(ItemI item)
	{
		super(item);
	}

	public AutoArcProperty(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcProperty(UserI user)
	 **/
	public AutoArcProperty(){}

	public AutoArcProperty(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:property";
	}

	//FIELD

	private String _Property=null;

	/**
	 * @return Returns the property.
	 */
	public String getProperty(){
		try{
			if (_Property==null){
				_Property=getStringProperty("property");
				return _Property;
			}else {
				return _Property;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for property.
	 * @param v Value to Set.
	 */
	public void setProperty(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/property",v);
		_Property=null;
		} catch (Exception e1) {logger.error(e1);}
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

	private Integer _ArcPropertyId=null;

	/**
	 * @return Returns the arc_property_id.
	 */
	public Integer getArcPropertyId() {
		try{
			if (_ArcPropertyId==null){
				_ArcPropertyId=getIntegerProperty("arc_property_id");
				return _ArcPropertyId;
			}else {
				return _ArcPropertyId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc_property_id.
	 * @param v Value to Set.
	 */
	public void setArcPropertyId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/arc_property_id",v);
		_ArcPropertyId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcProperty> getAllArcPropertys(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProperty> al = new ArrayList<org.nrg.xdat.om.ArcProperty>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcProperty> getArcPropertysByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProperty> al = new ArrayList<org.nrg.xdat.om.ArcProperty>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcProperty> getArcPropertysByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcProperty> al = new ArrayList<org.nrg.xdat.om.ArcProperty>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcProperty getArcPropertysByArcPropertyId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:property/arc_property_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcProperty) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
