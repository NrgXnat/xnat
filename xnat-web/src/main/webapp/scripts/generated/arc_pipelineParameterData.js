/*
 * web: arc_pipelineParameterData.js
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

function arc_pipelineParameterData(){
this.xsiType="arc:pipelineParameterData";

	this.getSchemaElementName=function(){
		return "pipelineParameterData";
	}

	this.getFullSchemaElementName=function(){
		return "arc:pipelineParameterData";
	}

	this.Schemalink=null;


	function getSchemalink() {
		return this.Schemalink;
	}
	this.getSchemalink=getSchemalink;


	function setSchemalink(v){
		this.Schemalink=v;
	}
	this.setSchemalink=setSchemalink;

	this.Csvvalues=null;


	function getCsvvalues() {
		return this.Csvvalues;
	}
	this.getCsvvalues=getCsvvalues;


	function setCsvvalues(v){
		this.Csvvalues=v;
	}
	this.setCsvvalues=setCsvvalues;

	this.Csvvalues_selected=null;


	function getCsvvalues_selected() {
		return this.Csvvalues_selected;
	}
	this.getCsvvalues_selected=getCsvvalues_selected;


	function setCsvvalues_selected(v){
		this.Csvvalues_selected=v;
	}
	this.setCsvvalues_selected=setCsvvalues_selected;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.Multiplevalues=null;


	function getMultiplevalues() {
		return this.Multiplevalues;
	}
	this.getMultiplevalues=getMultiplevalues;


	function setMultiplevalues(v){
		this.Multiplevalues=v;
	}
	this.setMultiplevalues=setMultiplevalues;


	this.isMultiplevalues=function(defaultValue) {
		if(this.Multiplevalues==null)return defaultValue;
		if(this.Multiplevalues=="1" || this.Multiplevalues==true)return true;
		return false;
	}

	this.Editable=null;


	function getEditable() {
		return this.Editable;
	}
	this.getEditable=getEditable;


	function setEditable(v){
		this.Editable=v;
	}
	this.setEditable=setEditable;


	this.isEditable=function(defaultValue) {
		if(this.Editable==null)return defaultValue;
		if(this.Editable=="1" || this.Editable==true)return true;
		return false;
	}

	this.Batchparam=null;


	function getBatchparam() {
		return this.Batchparam;
	}
	this.getBatchparam=getBatchparam;


	function setBatchparam(v){
		this.Batchparam=v;
	}
	this.setBatchparam=setBatchparam;


	this.isBatchparam=function(defaultValue) {
		if(this.Batchparam==null)return defaultValue;
		if(this.Batchparam=="1" || this.Batchparam==true)return true;
		return false;
	}

	this.ArcPipelineparameterdataId=null;


	function getArcPipelineparameterdataId() {
		return this.ArcPipelineparameterdataId;
	}
	this.getArcPipelineparameterdataId=getArcPipelineparameterdataId;


	function setArcPipelineparameterdataId(v){
		this.ArcPipelineparameterdataId=v;
	}
	this.setArcPipelineparameterdataId=setArcPipelineparameterdataId;

	this.parameters_parameter_arc_pipeli_arc_pipelinedata_id_fk=null;


	this.getparameters_parameter_arc_pipeli_arc_pipelinedata_id=function() {
		return this.parameters_parameter_arc_pipeli_arc_pipelinedata_id_fk;
	}


	this.setparameters_parameter_arc_pipeli_arc_pipelinedata_id=function(v){
		this.parameters_parameter_arc_pipeli_arc_pipelinedata_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="schemaLink"){
				return this.Schemalink ;
			} else 
			if(xmlPath=="csvValues"){
				return this.Csvvalues ;
			} else 
			if(xmlPath=="csvValues/selected"){
				return this.Csvvalues_selected ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="multipleValues"){
				return this.Multiplevalues ;
			} else 
			if(xmlPath=="editable"){
				return this.Editable ;
			} else 
			if(xmlPath=="batchParam"){
				return this.Batchparam ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="arc_pipelineParameterData_id"){
				return this.ArcPipelineparameterdataId ;
			} else 
			if(xmlPath=="parameters_parameter_arc_pipeli_arc_pipelinedata_id"){
				return this.parameters_parameter_arc_pipeli_arc_pipelinedata_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="schemaLink"){
				this.Schemalink=value;
			} else 
			if(xmlPath=="csvValues"){
				this.Csvvalues=value;
			} else 
			if(xmlPath=="csvValues/selected"){
				this.Csvvalues_selected=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="multipleValues"){
				this.Multiplevalues=value;
			} else 
			if(xmlPath=="editable"){
				this.Editable=value;
			} else 
			if(xmlPath=="batchParam"){
				this.Batchparam=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="arc_pipelineParameterData_id"){
				this.ArcPipelineparameterdataId=value;
			} else 
			if(xmlPath=="parameters_parameter_arc_pipeli_arc_pipelinedata_id"){
				this.parameters_parameter_arc_pipeli_arc_pipelinedata_id_fk=value;
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
		if (xmlPath=="schemaLink"){
			return "field_data";
		}else if (xmlPath=="csvValues"){
			return "field_data";
		}else if (xmlPath=="csvValues/selected"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_LONG_DATA";
		}else if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="multipleValues"){
			return "field_data";
		}else if (xmlPath=="editable"){
			return "field_data";
		}else if (xmlPath=="batchParam"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<arc:pipelineParameterData";
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
		xmlTxt+="\n</arc:pipelineParameterData>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ArcPipelineparameterdataId!=null){
				if(hiddenCount++>0)str+=",";
				str+="arc_pipelineParameterData_id=\"" + this.ArcPipelineparameterdataId + "\"";
			}
			if(this.parameters_parameter_arc_pipeli_arc_pipelinedata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="parameters_parameter_arc_pipeli_arc_pipelinedata_id=\"" + this.parameters_parameter_arc_pipeli_arc_pipelinedata_id_fk + "\"";
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

		if (this.Multiplevalues!=null)
			attTxt+=" multipleValues=\"" +this.Multiplevalues +"\"";
		//NOT REQUIRED FIELD

		if (this.Editable!=null)
			attTxt+=" editable=\"" +this.Editable +"\"";
		//NOT REQUIRED FIELD

		if (this.Batchparam!=null)
			attTxt+=" batchParam=\"" +this.Batchparam +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Schemalink!=null){
			xmlTxt+="\n<arc:schemaLink";
			xmlTxt+=">";
			xmlTxt+=this.Schemalink.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:schemaLink>";
		}
		var CsvvaluesATT = ""
		if (this.Csvvalues_selected!=null)
			CsvvaluesATT+=" selected=\"" + this.Csvvalues_selected.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Csvvalues!=null){
			xmlTxt+="\n<arc:csvValues";
			xmlTxt+=CsvvaluesATT;
			xmlTxt+=">";
			xmlTxt+=this.Csvvalues.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:csvValues>";
		}
		else if(CsvvaluesATT!=""){
			xmlTxt+="\n<arc:csvValues";
			xmlTxt+=CsvvaluesATT;
			xmlTxt+="/>";
		}

		if (this.Description!=null){
			xmlTxt+="\n<arc:description";
			xmlTxt+=">";
			xmlTxt+=this.Description.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:description>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ArcPipelineparameterdataId!=null) return true;
			if (this.parameters_parameter_arc_pipeli_arc_pipelinedata_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Schemalink!=null) return true;
		if (this.Csvvalues_selected!=null)
			return true;
		if (this.Csvvalues!=null) return true;
		if (this.Description!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
