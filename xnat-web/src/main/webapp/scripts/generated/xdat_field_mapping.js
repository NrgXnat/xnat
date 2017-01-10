/*
 * web: xdat_field_mapping.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * GENERATED FILE
 * Created on Fri Feb 12 15:43:51 CST 2016
 *
 */

/**
 * @author XDAT
 *
 */

function xdat_field_mapping(){
this.xsiType="xdat:field_mapping";

	this.getSchemaElementName=function(){
		return "field_mapping";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:field_mapping";
	}

	this.Field=null;


	function getField() {
		return this.Field;
	}
	this.getField=getField;


	function setField(v){
		this.Field=v;
	}
	this.setField=setField;

	this.FieldValue=null;


	function getFieldValue() {
		return this.FieldValue;
	}
	this.getFieldValue=getFieldValue;


	function setFieldValue(v){
		this.FieldValue=v;
	}
	this.setFieldValue=setFieldValue;

	this.CreateElement=null;


	function getCreateElement() {
		return this.CreateElement;
	}
	this.getCreateElement=getCreateElement;


	function setCreateElement(v){
		this.CreateElement=v;
	}
	this.setCreateElement=setCreateElement;


	this.isCreateElement=function(defaultValue) {
		if(this.CreateElement==null)return defaultValue;
		if(this.CreateElement=="1" || this.CreateElement==true)return true;
		return false;
	}

	this.ReadElement=null;


	function getReadElement() {
		return this.ReadElement;
	}
	this.getReadElement=getReadElement;


	function setReadElement(v){
		this.ReadElement=v;
	}
	this.setReadElement=setReadElement;


	this.isReadElement=function(defaultValue) {
		if(this.ReadElement==null)return defaultValue;
		if(this.ReadElement=="1" || this.ReadElement==true)return true;
		return false;
	}

	this.EditElement=null;


	function getEditElement() {
		return this.EditElement;
	}
	this.getEditElement=getEditElement;


	function setEditElement(v){
		this.EditElement=v;
	}
	this.setEditElement=setEditElement;


	this.isEditElement=function(defaultValue) {
		if(this.EditElement==null)return defaultValue;
		if(this.EditElement=="1" || this.EditElement==true)return true;
		return false;
	}

	this.DeleteElement=null;


	function getDeleteElement() {
		return this.DeleteElement;
	}
	this.getDeleteElement=getDeleteElement;


	function setDeleteElement(v){
		this.DeleteElement=v;
	}
	this.setDeleteElement=setDeleteElement;


	this.isDeleteElement=function(defaultValue) {
		if(this.DeleteElement==null)return defaultValue;
		if(this.DeleteElement=="1" || this.DeleteElement==true)return true;
		return false;
	}

	this.ActiveElement=null;


	function getActiveElement() {
		return this.ActiveElement;
	}
	this.getActiveElement=getActiveElement;


	function setActiveElement(v){
		this.ActiveElement=v;
	}
	this.setActiveElement=setActiveElement;


	this.isActiveElement=function(defaultValue) {
		if(this.ActiveElement==null)return defaultValue;
		if(this.ActiveElement=="1" || this.ActiveElement==true)return true;
		return false;
	}

	this.ComparisonType=null;


	function getComparisonType() {
		return this.ComparisonType;
	}
	this.getComparisonType=getComparisonType;


	function setComparisonType(v){
		this.ComparisonType=v;
	}
	this.setComparisonType=setComparisonType;

	this.XdatFieldMappingId=null;


	function getXdatFieldMappingId() {
		return this.XdatFieldMappingId;
	}
	this.getXdatFieldMappingId=getXdatFieldMappingId;


	function setXdatFieldMappingId(v){
		this.XdatFieldMappingId=v;
	}
	this.setXdatFieldMappingId=setXdatFieldMappingId;

	this.xdat_field_mapping_set_xdat_field_mapping_set_id_fk=null;


	this.getxdat_field_mapping_set_xdat_field_mapping_set_id=function() {
		return this.xdat_field_mapping_set_xdat_field_mapping_set_id_fk;
	}


	this.setxdat_field_mapping_set_xdat_field_mapping_set_id=function(v){
		this.xdat_field_mapping_set_xdat_field_mapping_set_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="field"){
				return this.Field ;
			} else 
			if(xmlPath=="field_value"){
				return this.FieldValue ;
			} else 
			if(xmlPath=="create_element"){
				return this.CreateElement ;
			} else 
			if(xmlPath=="read_element"){
				return this.ReadElement ;
			} else 
			if(xmlPath=="edit_element"){
				return this.EditElement ;
			} else 
			if(xmlPath=="delete_element"){
				return this.DeleteElement ;
			} else 
			if(xmlPath=="active_element"){
				return this.ActiveElement ;
			} else 
			if(xmlPath=="comparison_type"){
				return this.ComparisonType ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_field_mapping_id"){
				return this.XdatFieldMappingId ;
			} else 
			if(xmlPath=="xdat_field_mapping_set_xdat_field_mapping_set_id"){
				return this.xdat_field_mapping_set_xdat_field_mapping_set_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="field"){
				this.Field=value;
			} else 
			if(xmlPath=="field_value"){
				this.FieldValue=value;
			} else 
			if(xmlPath=="create_element"){
				this.CreateElement=value;
			} else 
			if(xmlPath=="read_element"){
				this.ReadElement=value;
			} else 
			if(xmlPath=="edit_element"){
				this.EditElement=value;
			} else 
			if(xmlPath=="delete_element"){
				this.DeleteElement=value;
			} else 
			if(xmlPath=="active_element"){
				this.ActiveElement=value;
			} else 
			if(xmlPath=="comparison_type"){
				this.ComparisonType=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_field_mapping_id"){
				this.XdatFieldMappingId=value;
			} else 
			if(xmlPath=="xdat_field_mapping_set_xdat_field_mapping_set_id"){
				this.xdat_field_mapping_set_xdat_field_mapping_set_id_fk=value;
			} else 
			{
				return null;
			}
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.setReferenceField=function(xmlPath,v) {
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="field"){
			return "field_data";
		}else if (xmlPath=="field_value"){
			return "field_data";
		}else if (xmlPath=="create_element"){
			return "field_data";
		}else if (xmlPath=="read_element"){
			return "field_data";
		}else if (xmlPath=="edit_element"){
			return "field_data";
		}else if (xmlPath=="delete_element"){
			return "field_data";
		}else if (xmlPath=="active_element"){
			return "field_data";
		}else if (xmlPath=="comparison_type"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:field_mapping";
		xmlTxt+=this.getXMLAtts();
		xmlTxt+=" xmlns:arc=\"http://nrg.wustl.edu/arc\"";
		xmlTxt+=" xmlns:cat=\"http://nrg.wustl.edu/catalog\"";
		xmlTxt+=" xmlns:pipe=\"http://nrg.wustl.edu/pipe\"";
		xmlTxt+=" xmlns:prov=\"http://www.nbirn.net/prov\"";
		xmlTxt+=" xmlns:scr=\"http://nrg.wustl.edu/scr\"";
		xmlTxt+=" xmlns:val=\"http://nrg.wustl.edu/val\"";
		xmlTxt+=" xmlns:wrk=\"http://nrg.wustl.edu/workflow\"";
		xmlTxt+=" xmlns:xdat=\"http://nrg.wustl.edu/security\"";
		xmlTxt+=" xmlns:xnat=\"http://nrg.wustl.edu/xnat\"";
		xmlTxt+=" xmlns:xnat_a=\"http://nrg.wustl.edu/xnat_assessments\"";
		xmlTxt+=" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"";
		xmlTxt+=">";
		xmlTxt+=this.getXMLBody(preventComments)
		xmlTxt+="\n</xdat:field_mapping>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatFieldMappingId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_field_mapping_id=\"" + this.XdatFieldMappingId + "\"";
			}
			if(this.xdat_field_mapping_set_xdat_field_mapping_set_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_field_mapping_set_xdat_field_mapping_set_id=\"" + this.xdat_field_mapping_set_xdat_field_mapping_set_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Field!=null)
			attTxt+=" field=\"" +this.Field +"\"";
		else attTxt+=" field=\"\"";//REQUIRED FIELD

		if (this.FieldValue!=null)
			attTxt+=" field_value=\"" +this.FieldValue +"\"";
		else attTxt+=" field_value=\"\"";//REQUIRED FIELD

		if (this.CreateElement!=null)
			attTxt+=" create_element=\"" +this.CreateElement +"\"";
		//NOT REQUIRED FIELD

		if (this.ReadElement!=null)
			attTxt+=" read_element=\"" +this.ReadElement +"\"";
		//NOT REQUIRED FIELD

		if (this.EditElement!=null)
			attTxt+=" edit_element=\"" +this.EditElement +"\"";
		//NOT REQUIRED FIELD

		if (this.DeleteElement!=null)
			attTxt+=" delete_element=\"" +this.DeleteElement +"\"";
		//NOT REQUIRED FIELD

		if (this.ActiveElement!=null)
			attTxt+=" active_element=\"" +this.ActiveElement +"\"";
		//NOT REQUIRED FIELD

		if (this.ComparisonType!=null)
			attTxt+=" comparison_type=\"" +this.ComparisonType +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatFieldMappingId!=null) return true;
			if (this.xdat_field_mapping_set_xdat_field_mapping_set_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		return false;
	}
}
