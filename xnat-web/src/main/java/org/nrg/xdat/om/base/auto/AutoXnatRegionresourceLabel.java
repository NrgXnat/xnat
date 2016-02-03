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
public abstract class AutoXnatRegionresourceLabel extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.XnatRegionresourceLabelI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoXnatRegionresourceLabel.class);
	public static String SCHEMA_ELEMENT_NAME="xnat:regionResource_label";

	public AutoXnatRegionresourceLabel(ItemI item)
	{
		super(item);
	}

	public AutoXnatRegionresourceLabel(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoXnatRegionresourceLabel(UserI user)
	 **/
	public AutoXnatRegionresourceLabel(){}

	public AutoXnatRegionresourceLabel(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "xnat:regionResource_label";
	}

	//FIELD

	private String _Label=null;

	/**
	 * @return Returns the label.
	 */
	public String getLabel(){
		try{
			if (_Label==null){
				_Label=getStringProperty("label");
				return _Label;
			}else {
				return _Label;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for label.
	 * @param v Value to Set.
	 */
	public void setLabel(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/label",v);
		_Label=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Object _Id=null;

	/**
	 * @return Returns the id.
	 */
	public Object getId(){
		try{
			if (_Id==null){
				_Id=getProperty("id");
				return _Id;
			}else {
				return _Id;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for id.
	 * @param v Value to Set.
	 */
	public void setId(Object v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/id",v);
		_Id=null;
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

	private Integer _XnatRegionresourceLabelId=null;

	/**
	 * @return Returns the xnat_regionResource_label_id.
	 */
	public Integer getXnatRegionresourceLabelId() {
		try{
			if (_XnatRegionresourceLabelId==null){
				_XnatRegionresourceLabelId=getIntegerProperty("xnat_regionResource_label_id");
				return _XnatRegionresourceLabelId;
			}else {
				return _XnatRegionresourceLabelId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for xnat_regionResource_label_id.
	 * @param v Value to Set.
	 */
	public void setXnatRegionresourceLabelId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/xnat_regionResource_label_id",v);
		_XnatRegionresourceLabelId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel> getAllXnatRegionresourceLabels(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel> al = new ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel> getXnatRegionresourceLabelsByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel> al = new ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel> getXnatRegionresourceLabelsByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel> al = new ArrayList<org.nrg.xdat.om.XnatRegionresourceLabel>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static XnatRegionresourceLabel getXnatRegionresourceLabelsByXnatRegionresourceLabelId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("xnat:regionResource_label/xnat_regionResource_label_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (XnatRegionresourceLabel) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
