/*
 * web: pipe_pipelineDetails_parameter.js
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

function pipe_pipelineDetails_parameter(){
this.xsiType="pipe:pipelineDetails_parameter";

	this.getSchemaElementName=function(){
		return "pipelineDetails_parameter";
	}

	this.getFullSchemaElementName=function(){
		return "pipe:pipelineDetails_parameter";
	}

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.Values_schemalink=null;


	function getValues_schemalink() {
		return this.Values_schemalink;
	}
	this.getValues_schemalink=getValues_schemalink;


	function setValues_schemalink(v){
		this.Values_schemalink=v;
	}
	this.setValues_schemalink=setValues_schemalink;

	this.Values_csvvalues=null;


	function getValues_csvvalues() {
		return this.Values_csvvalues;
	}
	this.getValues_csvvalues=getValues_csvvalues;


	function setValues_csvvalues(v){
		this.Values_csvvalues=v;
	}
	this.setValues_csvvalues=setValues_csvvalues;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;

	this.PipePipelinedetailsParameterId=null;


	function getPipePipelinedetailsParameterId() {
		return this.PipePipelinedetailsParameterId;
	}
	this.getPipePipelinedetailsParameterId=getPipePipelinedetailsParameterId;


	function setPipePipelinedetailsParameterId(v){
		this.PipePipelinedetailsParameterId=v;
	}
	this.setPipePipelinedetailsParameterId=setPipePipelinedetailsParameterId;

	this.parameters_parameter_pipe_pipel_path_fk=null;


	this.getparameters_parameter_pipe_pipel_path=function() {
		return this.parameters_parameter_pipe_pipel_path_fk;
	}


	this.setparameters_parameter_pipe_pipel_path=function(v){
		this.parameters_parameter_pipe_pipel_path_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="values/schemaLink"){
				return this.Values_schemalink ;
			} else 
			if(xmlPath=="values/csvValues"){
				return this.Values_csvvalues ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="pipe_pipelineDetails_parameter_id"){
				return this.PipePipelinedetailsParameterId ;
			} else 
			if(xmlPath=="parameters_parameter_pipe_pipel_path"){
				return this.parameters_parameter_pipe_pipel_path_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="values/schemaLink"){
				this.Values_schemalink=value;
			} else 
			if(xmlPath=="values/csvValues"){
				this.Values_csvvalues=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="pipe_pipelineDetails_parameter_id"){
				this.PipePipelinedetailsParameterId=value;
			} else 
			if(xmlPath=="parameters_parameter_pipe_pipel_path"){
				this.parameters_parameter_pipe_pipel_path_fk=value;
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
		if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="values/schemaLink"){
			return "field_data";
		}else if (xmlPath=="values/csvValues"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_LONG_DATA";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<pipe:pipelineDetails_parameter";
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
		xmlTxt+="\n</pipe:pipelineDetails_parameter>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.PipePipelinedetailsParameterId!=null){
				if(hiddenCount++>0)str+=",";
				str+="pipe_pipelineDetails_parameter_id=\"" + this.PipePipelinedetailsParameterId + "\"";
			}
			if(this.parameters_parameter_pipe_pipel_path_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="parameters_parameter_pipe_pipel_path=\"" + this.parameters_parameter_pipe_pipel_path_fk + "\"";
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
		if (this.Name!=null){
			xmlTxt+="\n<pipe:name";
			xmlTxt+=">";
			xmlTxt+=this.Name.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</pipe:name>";
		}
			var child0=0;
			var att0=0;
			if(this.Values_schemalink!=null)
			child0++;
			if(this.Values_csvvalues!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<pipe:values";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Values_schemalink!=null){
			xmlTxt+="\n<pipe:schemaLink";
			xmlTxt+=">";
			xmlTxt+=this.Values_schemalink.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</pipe:schemaLink>";
		}
		if (this.Values_csvvalues!=null){
			xmlTxt+="\n<pipe:csvValues";
			xmlTxt+=">";
			xmlTxt+=this.Values_csvvalues.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</pipe:csvValues>";
		}
				xmlTxt+="\n</pipe:values>";
			}
			}

		if (this.Description!=null){
			xmlTxt+="\n<pipe:description";
			xmlTxt+=">";
			xmlTxt+=this.Description.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</pipe:description>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.PipePipelinedetailsParameterId!=null) return true;
			if (this.parameters_parameter_pipe_pipel_path_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Name!=null) return true;
			if(this.Values_schemalink!=null) return true;
			if(this.Values_csvvalues!=null) return true;
		if (this.Description!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
