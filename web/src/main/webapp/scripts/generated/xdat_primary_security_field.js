/*
 * web: xdat_primary_security_field.js
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

function xdat_primary_security_field(){
this.xsiType="xdat:primary_security_field";

	this.getSchemaElementName=function(){
		return "primary_security_field";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:primary_security_field";
	}

	this.PrimarySecurityField=null;


	function getPrimarySecurityField() {
		return this.PrimarySecurityField;
	}
	this.getPrimarySecurityField=getPrimarySecurityField;


	function setPrimarySecurityField(v){
		this.PrimarySecurityField=v;
	}
	this.setPrimarySecurityField=setPrimarySecurityField;

	this.XdatPrimarySecurityFieldId=null;


	function getXdatPrimarySecurityFieldId() {
		return this.XdatPrimarySecurityFieldId;
	}
	this.getXdatPrimarySecurityFieldId=getXdatPrimarySecurityFieldId;


	function setXdatPrimarySecurityFieldId(v){
		this.XdatPrimarySecurityFieldId=v;
	}
	this.setXdatPrimarySecurityFieldId=setXdatPrimarySecurityFieldId;

	this.primary_security_fields_primary_element_name_fk=null;


	this.getprimary_security_fields_primary_element_name=function() {
		return this.primary_security_fields_primary_element_name_fk;
	}


	this.setprimary_security_fields_primary_element_name=function(v){
		this.primary_security_fields_primary_element_name_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="primary_security_field"){
				return this.PrimarySecurityField ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_primary_security_field_id"){
				return this.XdatPrimarySecurityFieldId ;
			} else 
			if(xmlPath=="primary_security_fields_primary_element_name"){
				return this.primary_security_fields_primary_element_name_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="primary_security_field"){
				this.PrimarySecurityField=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_primary_security_field_id"){
				this.XdatPrimarySecurityFieldId=value;
			} else 
			if(xmlPath=="primary_security_fields_primary_element_name"){
				this.primary_security_fields_primary_element_name_fk=value;
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
		if (xmlPath=="primary_security_field"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:primary_security_field";
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
		xmlTxt+="\n</xdat:primary_security_field>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatPrimarySecurityFieldId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_primary_security_field_id=\"" + this.XdatPrimarySecurityFieldId + "\"";
			}
			if(this.primary_security_fields_primary_element_name_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="primary_security_fields_primary_element_name=\"" + this.primary_security_fields_primary_element_name_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.PrimarySecurityField!=null){
			xmlTxt+=this.PrimarySecurityField.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatPrimarySecurityFieldId!=null) return true;
			if (this.primary_security_fields_primary_element_name_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.PrimarySecurityField!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
