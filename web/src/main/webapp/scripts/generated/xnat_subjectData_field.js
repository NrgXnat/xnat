/*
 * web: xnat_subjectData_field.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

/*
 * GENERATED FILE
 * Created on Fri Feb 12 15:43:50 CST 2016
 *
 */

/**
 * @author XDAT
 *
 */

function xnat_subjectData_field(){
this.xsiType="xnat:subjectData_field";

	this.getSchemaElementName=function(){
		return "subjectData_field";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:subjectData_field";
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

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.XnatSubjectdataFieldId=null;


	function getXnatSubjectdataFieldId() {
		return this.XnatSubjectdataFieldId;
	}
	this.getXnatSubjectdataFieldId=getXnatSubjectdataFieldId;


	function setXnatSubjectdataFieldId(v){
		this.XnatSubjectdataFieldId=v;
	}
	this.setXnatSubjectdataFieldId=setXnatSubjectdataFieldId;

	this.fields_field_xnat_subjectData_id_fk=null;


	this.getfields_field_xnat_subjectData_id=function() {
		return this.fields_field_xnat_subjectData_id_fk;
	}


	this.setfields_field_xnat_subjectData_id=function(v){
		this.fields_field_xnat_subjectData_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="field"){
				return this.Field ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_subjectData_field_id"){
				return this.XnatSubjectdataFieldId ;
			} else 
			if(xmlPath=="fields_field_xnat_subjectData_id"){
				return this.fields_field_xnat_subjectData_id_fk ;
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
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_subjectData_field_id"){
				this.XnatSubjectdataFieldId=value;
			} else 
			if(xmlPath=="fields_field_xnat_subjectData_id"){
				this.fields_field_xnat_subjectData_id_fk=value;
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
		}else if (xmlPath=="name"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:subjectData_field";
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
		xmlTxt+="\n</xnat:subjectData_field>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatSubjectdataFieldId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_subjectData_field_id=\"" + this.XnatSubjectdataFieldId + "\"";
			}
			if(this.fields_field_xnat_subjectData_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="fields_field_xnat_subjectData_id=\"" + this.fields_field_xnat_subjectData_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Field!=null){
			xmlTxt+=this.Field.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatSubjectdataFieldId!=null) return true;
			if (this.fields_field_xnat_subjectData_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Field!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
