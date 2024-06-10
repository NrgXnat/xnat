/*
 * web: xdat_access_log.js
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

function xdat_access_log(){
this.xsiType="xdat:access_log";

	this.getSchemaElementName=function(){
		return "access_log";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:access_log";
	}

	this.Login=null;


	function getLogin() {
		return this.Login;
	}
	this.getLogin=getLogin;


	function setLogin(v){
		this.Login=v;
	}
	this.setLogin=setLogin;

	this.AccessDate=null;


	function getAccessDate() {
		return this.AccessDate;
	}
	this.getAccessDate=getAccessDate;


	function setAccessDate(v){
		this.AccessDate=v;
	}
	this.setAccessDate=setAccessDate;

	this.Ip=null;


	function getIp() {
		return this.Ip;
	}
	this.getIp=getIp;


	function setIp(v){
		this.Ip=v;
	}
	this.setIp=setIp;

	this.Method=null;


	function getMethod() {
		return this.Method;
	}
	this.getMethod=getMethod;


	function setMethod(v){
		this.Method=v;
	}
	this.setMethod=setMethod;

	this.XdatAccessLogId=null;


	function getXdatAccessLogId() {
		return this.XdatAccessLogId;
	}
	this.getXdatAccessLogId=getXdatAccessLogId;


	function setXdatAccessLogId(v){
		this.XdatAccessLogId=v;
	}
	this.setXdatAccessLogId=setXdatAccessLogId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="login"){
				return this.Login ;
			} else 
			if(xmlPath=="access_date"){
				return this.AccessDate ;
			} else 
			if(xmlPath=="ip"){
				return this.Ip ;
			} else 
			if(xmlPath=="method"){
				return this.Method ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_access_log_id"){
				return this.XdatAccessLogId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="login"){
				this.Login=value;
			} else 
			if(xmlPath=="access_date"){
				this.AccessDate=value;
			} else 
			if(xmlPath=="ip"){
				this.Ip=value;
			} else 
			if(xmlPath=="method"){
				this.Method=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_access_log_id"){
				this.XdatAccessLogId=value;
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
		if (xmlPath=="login"){
			return "field_data";
		}else if (xmlPath=="access_date"){
			return "field_data";
		}else if (xmlPath=="ip"){
			return "field_data";
		}else if (xmlPath=="method"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:access_log";
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
		xmlTxt+="\n</xdat:access_log>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatAccessLogId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_access_log_id=\"" + this.XdatAccessLogId + "\"";
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
		if (this.Login!=null){
			xmlTxt+="\n<xdat:login";
			xmlTxt+=">";
			xmlTxt+=this.Login.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:login>";
		}
		if (this.AccessDate!=null){
			xmlTxt+="\n<xdat:access_date";
			xmlTxt+=">";
			xmlTxt+=this.AccessDate;
			xmlTxt+="</xdat:access_date>";
		}
		if (this.Ip!=null){
			xmlTxt+="\n<xdat:ip";
			xmlTxt+=">";
			xmlTxt+=this.Ip.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:ip>";
		}
		if (this.Method!=null){
			xmlTxt+="\n<xdat:method";
			xmlTxt+=">";
			xmlTxt+=this.Method.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:method>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatAccessLogId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Login!=null) return true;
		if (this.AccessDate!=null) return true;
		if (this.Ip!=null) return true;
		if (this.Method!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
