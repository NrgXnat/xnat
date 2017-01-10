/*
 * web: pipe_pipelineDetails_element.js
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

function pipe_pipelineDetails_element(){
this.xsiType="pipe:pipelineDetails_element";

	this.getSchemaElementName=function(){
		return "pipelineDetails_element";
	}

	this.getFullSchemaElementName=function(){
		return "pipe:pipelineDetails_element";
	}

	this.Element=null;


	function getElement() {
		return this.Element;
	}
	this.getElement=getElement;


	function setElement(v){
		this.Element=v;
	}
	this.setElement=setElement;

	this.PipePipelinedetailsElementId=null;


	function getPipePipelinedetailsElementId() {
		return this.PipePipelinedetailsElementId;
	}
	this.getPipePipelinedetailsElementId=getPipePipelinedetailsElementId;


	function setPipePipelinedetailsElementId(v){
		this.PipePipelinedetailsElementId=v;
	}
	this.setPipePipelinedetailsElementId=setPipePipelinedetailsElementId;

	this.generateselements_element_pipe__path_fk=null;


	this.getgenerateselements_element_pipe__path=function() {
		return this.generateselements_element_pipe__path_fk;
	}


	this.setgenerateselements_element_pipe__path=function(v){
		this.generateselements_element_pipe__path_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="element"){
				return this.Element ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="pipe_pipelineDetails_element_id"){
				return this.PipePipelinedetailsElementId ;
			} else 
			if(xmlPath=="generateselements_element_pipe__path"){
				return this.generateselements_element_pipe__path_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="element"){
				this.Element=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="pipe_pipelineDetails_element_id"){
				this.PipePipelinedetailsElementId=value;
			} else 
			if(xmlPath=="generateselements_element_pipe__path"){
				this.generateselements_element_pipe__path_fk=value;
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
		if (xmlPath=="element"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<pipe:pipelineDetails_element";
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
		xmlTxt+="\n</pipe:pipelineDetails_element>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.PipePipelinedetailsElementId!=null){
				if(hiddenCount++>0)str+=",";
				str+="pipe_pipelineDetails_element_id=\"" + this.PipePipelinedetailsElementId + "\"";
			}
			if(this.generateselements_element_pipe__path_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="generateselements_element_pipe__path=\"" + this.generateselements_element_pipe__path_fk + "\"";
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
		if (this.Element!=null){
			xmlTxt+="\n<pipe:element";
			xmlTxt+=">";
			xmlTxt+=this.Element.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</pipe:element>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.PipePipelinedetailsElementId!=null) return true;
			if (this.generateselements_element_pipe__path_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Element!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
