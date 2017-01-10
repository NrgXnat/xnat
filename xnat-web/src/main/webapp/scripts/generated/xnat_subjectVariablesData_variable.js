/*
 * web: xnat_subjectVariablesData_variable.js
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

function xnat_subjectVariablesData_variable(){
this.xsiType="xnat:subjectVariablesData_variable";

	this.getSchemaElementName=function(){
		return "subjectVariablesData_variable";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:subjectVariablesData_variable";
	}

	this.Variable=null;


	function getVariable() {
		return this.Variable;
	}
	this.getVariable=getVariable;


	function setVariable(v){
		this.Variable=v;
	}
	this.setVariable=setVariable;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.XnatSubjectvariablesdataVariableId=null;


	function getXnatSubjectvariablesdataVariableId() {
		return this.XnatSubjectvariablesdataVariableId;
	}
	this.getXnatSubjectvariablesdataVariableId=getXnatSubjectvariablesdataVariableId;


	function setXnatSubjectvariablesdataVariableId(v){
		this.XnatSubjectvariablesdataVariableId=v;
	}
	this.setXnatSubjectvariablesdataVariableId=setXnatSubjectvariablesdataVariableId;

	this.variables_variable_xnat_subject_id_fk=null;


	this.getvariables_variable_xnat_subject_id=function() {
		return this.variables_variable_xnat_subject_id_fk;
	}


	this.setvariables_variable_xnat_subject_id=function(v){
		this.variables_variable_xnat_subject_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="variable"){
				return this.Variable ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_subjectVariablesData_variable_id"){
				return this.XnatSubjectvariablesdataVariableId ;
			} else 
			if(xmlPath=="variables_variable_xnat_subject_id"){
				return this.variables_variable_xnat_subject_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="variable"){
				this.Variable=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_subjectVariablesData_variable_id"){
				this.XnatSubjectvariablesdataVariableId=value;
			} else 
			if(xmlPath=="variables_variable_xnat_subject_id"){
				this.variables_variable_xnat_subject_id_fk=value;
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
		if (xmlPath=="variable"){
			return "field_data";
		}else if (xmlPath=="name"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:subjectVariablesData_variable";
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
		xmlTxt+="\n</xnat:subjectVariablesData_variable>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatSubjectvariablesdataVariableId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_subjectVariablesData_variable_id=\"" + this.XnatSubjectvariablesdataVariableId + "\"";
			}
			if(this.variables_variable_xnat_subject_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="variables_variable_xnat_subject_id=\"" + this.variables_variable_xnat_subject_id_fk + "\"";
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
		if (this.Variable!=null){
			xmlTxt+=this.Variable.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatSubjectvariablesdataVariableId!=null) return true;
			if (this.variables_variable_xnat_subject_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Variable!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
