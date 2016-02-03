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
public abstract class AutoArcPipelineparameterdata extends org.nrg.xdat.base.BaseElement implements org.nrg.xdat.model.ArcPipelineparameterdataI {
	public static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AutoArcPipelineparameterdata.class);
	public static String SCHEMA_ELEMENT_NAME="arc:pipelineParameterData";

	public AutoArcPipelineparameterdata(ItemI item)
	{
		super(item);
	}

	public AutoArcPipelineparameterdata(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use AutoArcPipelineparameterdata(UserI user)
	 **/
	public AutoArcPipelineparameterdata(){}

	public AutoArcPipelineparameterdata(Hashtable properties,UserI user)
	{
		super(properties,user);
	}

	public String getSchemaElementName(){
		return "arc:pipelineParameterData";
	}

	//FIELD

	private String _Schemalink=null;

	/**
	 * @return Returns the schemaLink.
	 */
	public String getSchemalink(){
		try{
			if (_Schemalink==null){
				_Schemalink=getStringProperty("schemaLink");
				return _Schemalink;
			}else {
				return _Schemalink;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for schemaLink.
	 * @param v Value to Set.
	 */
	public void setSchemalink(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/schemaLink",v);
		_Schemalink=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Csvvalues=null;

	/**
	 * @return Returns the csvValues.
	 */
	public String getCsvvalues(){
		try{
			if (_Csvvalues==null){
				_Csvvalues=getStringProperty("csvValues");
				return _Csvvalues;
			}else {
				return _Csvvalues;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for csvValues.
	 * @param v Value to Set.
	 */
	public void setCsvvalues(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/csvValues",v);
		_Csvvalues=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private String _Csvvalues_selected=null;

	/**
	 * @return Returns the csvValues/selected.
	 */
	public String getCsvvalues_selected(){
		try{
			if (_Csvvalues_selected==null){
				_Csvvalues_selected=getStringProperty("csvValues/selected");
				return _Csvvalues_selected;
			}else {
				return _Csvvalues_selected;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for csvValues/selected.
	 * @param v Value to Set.
	 */
	public void setCsvvalues_selected(String v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/csvValues/selected",v);
		_Csvvalues_selected=null;
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

	private Boolean _Multiplevalues=null;

	/**
	 * @return Returns the multipleValues.
	 */
	public Boolean getMultiplevalues() {
		try{
			if (_Multiplevalues==null){
				_Multiplevalues=getBooleanProperty("multipleValues");
				return _Multiplevalues;
			}else {
				return _Multiplevalues;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for multipleValues.
	 * @param v Value to Set.
	 */
	public void setMultiplevalues(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/multipleValues",v);
		_Multiplevalues=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Editable=null;

	/**
	 * @return Returns the editable.
	 */
	public Boolean getEditable() {
		try{
			if (_Editable==null){
				_Editable=getBooleanProperty("editable");
				return _Editable;
			}else {
				return _Editable;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for editable.
	 * @param v Value to Set.
	 */
	public void setEditable(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/editable",v);
		_Editable=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Boolean _Batchparam=null;

	/**
	 * @return Returns the batchParam.
	 */
	public Boolean getBatchparam() {
		try{
			if (_Batchparam==null){
				_Batchparam=getBooleanProperty("batchParam");
				return _Batchparam;
			}else {
				return _Batchparam;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for batchParam.
	 * @param v Value to Set.
	 */
	public void setBatchparam(Object v){
		try{
		setBooleanProperty(SCHEMA_ELEMENT_NAME + "/batchParam",v);
		_Batchparam=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	//FIELD

	private Integer _ArcPipelineparameterdataId=null;

	/**
	 * @return Returns the arc_pipelineParameterData_id.
	 */
	public Integer getArcPipelineparameterdataId() {
		try{
			if (_ArcPipelineparameterdataId==null){
				_ArcPipelineparameterdataId=getIntegerProperty("arc_pipelineParameterData_id");
				return _ArcPipelineparameterdataId;
			}else {
				return _ArcPipelineparameterdataId;
			}
		} catch (Exception e1) {logger.error(e1);return null;}
	}

	/**
	 * Sets the value for arc_pipelineParameterData_id.
	 * @param v Value to Set.
	 */
	public void setArcPipelineparameterdataId(Integer v){
		try{
		setProperty(SCHEMA_ELEMENT_NAME + "/arc_pipelineParameterData_id",v);
		_ArcPipelineparameterdataId=null;
		} catch (Exception e1) {logger.error(e1);}
	}

	public static ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata> getAllArcPipelineparameterdatas(org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata> al = new ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata>();

		try{
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetAllItems(SCHEMA_ELEMENT_NAME,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata> getArcPipelineparameterdatasByField(String xmlPath, Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata> al = new ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(xmlPath,value,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata> getArcPipelineparameterdatasByField(org.nrg.xft.search.CriteriaCollection criteria, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata> al = new ArrayList<org.nrg.xdat.om.ArcPipelineparameterdata>();
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems(criteria,user,preLoad);
			al = org.nrg.xdat.base.BaseElement.WrapItems(items.getItems());
		} catch (Exception e) {
			logger.error("",e);
		}

		al.trimToSize();
		return al;
	}

	public static ArcPipelineparameterdata getArcPipelineparameterdatasByArcPipelineparameterdataId(Object value, org.nrg.xft.security.UserI user,boolean preLoad)
	{
		try {
			org.nrg.xft.collections.ItemCollection items = org.nrg.xft.search.ItemSearch.GetItems("arc:pipelineParameterData/arc_pipelineParameterData_id",value,user,preLoad);
			ItemI match = items.getFirst();
			if (match!=null)
				return (ArcPipelineparameterdata) org.nrg.xdat.base.BaseElement.GetGeneratedItem(match);
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
