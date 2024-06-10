/*
 * web: wrk_xnatExecutionEnvironment_parameter.js
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

function wrk_xnatExecutionEnvironment_parameter(){
this.xsiType="wrk:xnatExecutionEnvironment_parameter";

	this.getSchemaElementName=function(){
		return "xnatExecutionEnvironment_parameter";
	}

	this.getFullSchemaElementName=function(){
		return "wrk:xnatExecutionEnvironment_parameter";
	}

	this.Parameter=null;


	function getParameter() {
		return this.Parameter;
	}
	this.getParameter=getParameter;


	function setParameter(v){
		this.Parameter=v;
	}
	this.setParameter=setParameter;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.WrkXnatexecutionenvironmentParameterId=null;


	function getWrkXnatexecutionenvironmentParameterId() {
		return this.WrkXnatexecutionenvironmentParameterId;
	}
	this.getWrkXnatexecutionenvironmentParameterId=getWrkXnatexecutionenvironmentParameterId;


	function setWrkXnatexecutionenvironmentParameterId(v){
		this.WrkXnatexecutionenvironmentParameterId=v;
	}
	this.setWrkXnatexecutionenvironmentParameterId=setWrkXnatexecutionenvironmentParameterId;

	this.parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen_fk=null;


	this.getparameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen=function() {
		return this.parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen_fk;
	}


	this.setparameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen=function(v){
		this.parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="parameter"){
				return this.Parameter ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="wrk_xnatExecutionEnvironment_parameter_id"){
				return this.WrkXnatexecutionenvironmentParameterId ;
			} else 
			if(xmlPath=="parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen"){
				return this.parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="parameter"){
				this.Parameter=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="wrk_xnatExecutionEnvironment_parameter_id"){
				this.WrkXnatexecutionenvironmentParameterId=value;
			} else 
			if(xmlPath=="parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen"){
				this.parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen_fk=value;
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
		if (xmlPath=="parameter"){
			return "field_LONG_DATA";
		}else if (xmlPath=="name"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<wrk:xnatExecutionEnvironment_parameter";
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
		xmlTxt+="\n</wrk:xnatExecutionEnvironment_parameter>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.WrkXnatexecutionenvironmentParameterId!=null){
				if(hiddenCount++>0)str+=",";
				str+="wrk_xnatExecutionEnvironment_parameter_id=\"" + this.WrkXnatexecutionenvironmentParameterId + "\"";
			}
			if(this.parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen=\"" + this.parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		else attTxt+=" name=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Parameter!=null){
			xmlTxt+=this.Parameter.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.WrkXnatexecutionenvironmentParameterId!=null) return true;
			if (this.parameters_parameter_wrk_xnatEx_wrk_abstractexecutionenvironmen_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Parameter!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
