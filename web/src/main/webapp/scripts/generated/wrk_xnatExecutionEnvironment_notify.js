/*
 * web: wrk_xnatExecutionEnvironment_notify.js
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

function wrk_xnatExecutionEnvironment_notify(){
this.xsiType="wrk:xnatExecutionEnvironment_notify";

	this.getSchemaElementName=function(){
		return "xnatExecutionEnvironment_notify";
	}

	this.getFullSchemaElementName=function(){
		return "wrk:xnatExecutionEnvironment_notify";
	}

	this.Notify=null;


	function getNotify() {
		return this.Notify;
	}
	this.getNotify=getNotify;


	function setNotify(v){
		this.Notify=v;
	}
	this.setNotify=setNotify;

	this.WrkXnatexecutionenvironmentNotifyId=null;


	function getWrkXnatexecutionenvironmentNotifyId() {
		return this.WrkXnatexecutionenvironmentNotifyId;
	}
	this.getWrkXnatexecutionenvironmentNotifyId=getWrkXnatexecutionenvironmentNotifyId;


	function setWrkXnatexecutionenvironmentNotifyId(v){
		this.WrkXnatexecutionenvironmentNotifyId=v;
	}
	this.setWrkXnatexecutionenvironmentNotifyId=setWrkXnatexecutionenvironmentNotifyId;

	this.wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen_fk=null;


	this.getwrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen=function() {
		return this.wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen_fk;
	}


	this.setwrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen=function(v){
		this.wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="notify"){
				return this.Notify ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="wrk_xnatExecutionEnvironment_notify_id"){
				return this.WrkXnatexecutionenvironmentNotifyId ;
			} else 
			if(xmlPath=="wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen"){
				return this.wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="notify"){
				this.Notify=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="wrk_xnatExecutionEnvironment_notify_id"){
				this.WrkXnatexecutionenvironmentNotifyId=value;
			} else 
			if(xmlPath=="wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen"){
				this.wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen_fk=value;
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
		if (xmlPath=="notify"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<wrk:xnatExecutionEnvironment_notify";
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
		xmlTxt+="\n</wrk:xnatExecutionEnvironment_notify>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.WrkXnatexecutionenvironmentNotifyId!=null){
				if(hiddenCount++>0)str+=",";
				str+="wrk_xnatExecutionEnvironment_notify_id=\"" + this.WrkXnatexecutionenvironmentNotifyId + "\"";
			}
			if(this.wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen=\"" + this.wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen_fk + "\"";
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
		if (this.Notify!=null){
			xmlTxt+="\n<wrk:notify";
			xmlTxt+=">";
			xmlTxt+=this.Notify.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:notify>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.WrkXnatexecutionenvironmentNotifyId!=null) return true;
			if (this.wrk_xnatExecutionEnvironment_wrk_abstractexecutionenvironmen_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Notify!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
