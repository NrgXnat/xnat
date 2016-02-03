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
public abstract class AutoXnatComputationdata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatComputationdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatComputationdata.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:computationData";

	public AutoXnatComputationdata(ItemI item)
	{
		super(item);
	}

	public AutoXnatComputationdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatComputationdata(UserI user)
	 **/
	public AutoXnatComputationdata(){}

	public AutoXnatComputationdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:computationData";
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

	private String _Value=null;

	/**
	 * @return Returns the value.
	 */
	public String getValue(){
		try{
			if (_Value==null){
				_Value=getStringProperty("value");
				return _Value;
			}else {
				return _Value;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for value.
	 * @param v Value to Set.
	 */
	public void setValue(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/value",v);
		_Value=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Source=null;

	/**
	 * @return Returns the source.
	 */
	public String getSource(){
		try{
			if (_Source==null){
				_Source=getStringProperty("source");
				return _Source;
			}else {
				return _Source;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for source.
	 * @param v Value to Set.
	 */
	public void setSource(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/source",v);
		_Source=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Units=null;

	/**
	 * @return Returns the units.
	 */
	public String getUnits(){
		try{
			if (_Units==null){
				_Units=getStringProperty("units");
				return _Units;
			}else {
				return _Units;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for units.
	 * @param v Value to Set.
	 */
	public void setUnits(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/units",v);
		_Units=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _XnatComputationdataId=null;

	/**
	 * @return Returns the xnat_computationData_id.
	 */
	public Integer getXnatComputationdataId() {
		try{
			if (_XnatComputationdataId==null){
				_XnatComputationdataId=getIntegerProperty("xnat_computationData_id");
				return _XnatComputationdataId;
			}else {
				return _XnatComputationdataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_computationData_id.
	 * @param v Value to Set.
	 */
	public void setXnatComputationdataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_computationData_id",v);
		_XnatComputationdataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatComputationdata> getAllXnatComputationdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatComputationdata> al = new ArrayList<org.nrg.xdat.om.XnatComputationdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatComputationdata> getXnatComputationdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatComputationdata> al = new ArrayList<org.nrg.xdat.om.XnatComputationdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatComputationdata> getXnatComputationdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatComputationdata> al = new ArrayList<org.nrg.xdat.om.XnatComputationdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatComputationdata getXnatComputationdatasByXnatComputationdataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:computationData/xnat_computationData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatComputationdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
