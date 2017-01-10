/*
 * web: xdat_criteria.js
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

function xdat_criteria(){
this.xsiType="xdat:criteria";

	this.getSchemaElementName=function(){
		return "criteria";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:criteria";
	}

	this.SchemaField=null;


	function getSchemaField() {
		return this.SchemaField;
	}
	this.getSchemaField=getSchemaField;


	function setSchemaField(v){
		this.SchemaField=v;
	}
	this.setSchemaField=setSchemaField;

	this.ComparisonType=null;


	function getComparisonType() {
		return this.ComparisonType;
	}
	this.getComparisonType=getComparisonType;


	function setComparisonType(v){
		this.ComparisonType=v;
	}
	this.setComparisonType=setComparisonType;

	this.CustomSearch=null;


	function getCustomSearch() {
		return this.CustomSearch;
	}
	this.getCustomSearch=getCustomSearch;


	function setCustomSearch(v){
		this.CustomSearch=v;
	}
	this.setCustomSearch=setCustomSearch;

	this.Value=null;


	function getValue() {
		return this.Value;
	}
	this.getValue=getValue;


	function setValue(v){
		this.Value=v;
	}
	this.setValue=setValue;

	this.OverrideValueFormatting=null;


	function getOverrideValueFormatting() {
		return this.OverrideValueFormatting;
	}
	this.getOverrideValueFormatting=getOverrideValueFormatting;


	function setOverrideValueFormatting(v){
		this.OverrideValueFormatting=v;
	}
	this.setOverrideValueFormatting=setOverrideValueFormatting;


	this.isOverrideValueFormatting=function(defaultValue) {
		if(this.OverrideValueFormatting==null)return defaultValue;
		if(this.OverrideValueFormatting=="1" || this.OverrideValueFormatting==true)return true;
		return false;
	}

	this.XdatCriteriaId=null;


	function getXdatCriteriaId() {
		return this.XdatCriteriaId;
	}
	this.getXdatCriteriaId=getXdatCriteriaId;


	function setXdatCriteriaId(v){
		this.XdatCriteriaId=v;
	}
	this.setXdatCriteriaId=setXdatCriteriaId;

	this.xdat_criteria_set_xdat_criteria_set_id_fk=null;


	this.getxdat_criteria_set_xdat_criteria_set_id=function() {
		return this.xdat_criteria_set_xdat_criteria_set_id_fk;
	}


	this.setxdat_criteria_set_xdat_criteria_set_id=function(v){
		this.xdat_criteria_set_xdat_criteria_set_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="schema_field"){
				return this.SchemaField ;
			} else 
			if(xmlPath=="comparison_type"){
				return this.ComparisonType ;
			} else 
			if(xmlPath=="custom_search"){
				return this.CustomSearch ;
			} else 
			if(xmlPath=="value"){
				return this.Value ;
			} else 
			if(xmlPath=="override_value_formatting"){
				return this.OverrideValueFormatting ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_criteria_id"){
				return this.XdatCriteriaId ;
			} else 
			if(xmlPath=="xdat_criteria_set_xdat_criteria_set_id"){
				return this.xdat_criteria_set_xdat_criteria_set_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="schema_field"){
				this.SchemaField=value;
			} else 
			if(xmlPath=="comparison_type"){
				this.ComparisonType=value;
			} else 
			if(xmlPath=="custom_search"){
				this.CustomSearch=value;
			} else 
			if(xmlPath=="value"){
				this.Value=value;
			} else 
			if(xmlPath=="override_value_formatting"){
				this.OverrideValueFormatting=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_criteria_id"){
				this.XdatCriteriaId=value;
			} else 
			if(xmlPath=="xdat_criteria_set_xdat_criteria_set_id"){
				this.xdat_criteria_set_xdat_criteria_set_id_fk=value;
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
		if (xmlPath=="schema_field"){
			return "field_data";
		}else if (xmlPath=="comparison_type"){
			return "field_data";
		}else if (xmlPath=="custom_search"){
			return "field_data";
		}else if (xmlPath=="value"){
			return "field_data";
		}else if (xmlPath=="override_value_formatting"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:criteria";
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
		xmlTxt+="\n</xdat:criteria>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatCriteriaId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_criteria_id=\"" + this.XdatCriteriaId + "\"";
			}
			if(this.xdat_criteria_set_xdat_criteria_set_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_criteria_set_xdat_criteria_set_id=\"" + this.xdat_criteria_set_xdat_criteria_set_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.OverrideValueFormatting!=null)
			attTxt+=" override_value_formatting=\"" +this.OverrideValueFormatting +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.SchemaField!=null){
			xmlTxt+="\n<xdat:schema_field";
			xmlTxt+=">";
			xmlTxt+=this.SchemaField.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:schema_field>";
		}
		if (this.ComparisonType!=null){
			xmlTxt+="\n<xdat:comparison_type";
			xmlTxt+=">";
			xmlTxt+=this.ComparisonType.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:comparison_type>";
		}
		if (this.CustomSearch!=null){
			xmlTxt+="\n<xdat:custom_search";
			xmlTxt+=">";
			xmlTxt+=this.CustomSearch.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:custom_search>";
		}
		if (this.Value!=null){
			xmlTxt+="\n<xdat:value";
			xmlTxt+=">";
			xmlTxt+=this.Value.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:value>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatCriteriaId!=null) return true;
			if (this.xdat_criteria_set_xdat_criteria_set_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.SchemaField!=null) return true;
		if (this.ComparisonType!=null) return true;
		if (this.CustomSearch!=null) return true;
		if (this.Value!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
