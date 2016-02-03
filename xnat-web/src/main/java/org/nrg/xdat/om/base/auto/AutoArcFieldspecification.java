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
public abstract class AutoArcFieldspecification extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ArcFieldspecificationI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcFieldspecification.class);
	public static String SCHEMA_ELEMENT_NAME="arc:fieldSpecification";

	public AutoArcFieldspecification(ItemI item)
	{
		super(item);
	}

	public AutoArcFieldspecification(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcFieldspecification(UserI user)
	 **/
	public AutoArcFieldspecification(){}

	public AutoArcFieldspecification(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:fieldSpecification";
	}

	//FIELD

	private String _Fieldspecification=null;

	/**
	 * @return Returns the fieldSpecification.
	 */
	public String getFieldspecification(){
		try{
			if (_Fieldspecification==null){
				_Fieldspecification=getStringProperty("fieldSpecification");
				return _Fieldspecification;
			}else {
				return _Fieldspecification;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for fieldSpecification.
	 * @param v Value to Set.
	 */
	public void setFieldspecification(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/fieldSpecification",v);
		_Fieldspecification=null;
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

	private Integer _ArcFieldspecificationId=null;

	/**
	 * @return Returns the arc_fieldSpecification_id.
	 */
	public Integer getArcFieldspecificationId() {
		try{
			if (_ArcFieldspecificationId==null){
				_ArcFieldspecificationId=getIntegerProperty("arc_fieldSpecification_id");
				return _ArcFieldspecificationId;
			}else {
				return _ArcFieldspecificationId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc_fieldSpecification_id.
	 * @param v Value to Set.
	 */
	public void setArcFieldspecificationId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/arc_fieldSpecification_id",v);
		_ArcFieldspecificationId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcFieldspecification> getAllArcFieldspecifications(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcFieldspecification> al = new ArrayList<org.nrg.xdat.om.ArcFieldspecification>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcFieldspecification> getArcFieldspecificationsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcFieldspecification> al = new ArrayList<org.nrg.xdat.om.ArcFieldspecification>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcFieldspecification> getArcFieldspecificationsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcFieldspecification> al = new ArrayList<org.nrg.xdat.om.ArcFieldspecification>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcFieldspecification getArcFieldspecificationsByArcFieldspecificationId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:fieldSpecification/arc_fieldSpecification_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcFieldspecification) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
