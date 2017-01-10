/*
 * web: wrk_abstractExecutionEnvironment.js
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

function wrk_abstractExecutionEnvironment(){
this.xsiType="wrk:abstractExecutionEnvironment";

	this.getSchemaElementName=function(){
		return "abstractExecutionEnvironment";
	}

	this.getFullSchemaElementName=function(){
		return "wrk:abstractExecutionEnvironment";
	}

	this.WrkAbstractexecutionenvironmentId=null;


	function getWrkAbstractexecutionenvironmentId() {
		return this.WrkAbstractexecutionenvironmentId;
	}
	this.getWrkAbstractexecutionenvironmentId=getWrkAbstractexecutionenvironmentId;


	function setWrkAbstractexecutionenvironmentId(v){
		this.WrkAbstractexecutionenvironmentId=v;
	}
	this.setWrkAbstractexecutionenvironmentId=setWrkAbstractexecutionenvironmentId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="extension_item"){
				return this.ExtensionItem ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="wrk_abstractExecutionEnvironment_id"){
				return this.WrkAbstractexecutionenvironmentId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="extension_item"){
				this.ExtensionItem=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="wrk_abstractExecutionEnvironment_id"){
				this.WrkAbstractexecutionenvironmentId=value;
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
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<wrk:abstractExecutionEnvironment";
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
		xmlTxt+="\n</wrk:abstractExecutionEnvironment>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.WrkAbstractexecutionenvironmentId!=null){
				if(hiddenCount++>0)str+=",";
				str+="wrk_abstractExecutionEnvironment_id=\"" + this.WrkAbstractexecutionenvironmentId + "\"";
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
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.WrkAbstractexecutionenvironmentId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		return false;
	}
}
