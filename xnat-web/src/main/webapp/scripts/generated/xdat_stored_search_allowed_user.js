/*
 * web: xdat_stored_search_allowed_user.js
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

function xdat_stored_search_allowed_user(){
this.xsiType="xdat:stored_search_allowed_user";

	this.getSchemaElementName=function(){
		return "stored_search_allowed_user";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:stored_search_allowed_user";
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

	this.XdatStoredSearchAllowedUserId=null;


	function getXdatStoredSearchAllowedUserId() {
		return this.XdatStoredSearchAllowedUserId;
	}
	this.getXdatStoredSearchAllowedUserId=getXdatStoredSearchAllowedUserId;


	function setXdatStoredSearchAllowedUserId(v){
		this.XdatStoredSearchAllowedUserId=v;
	}
	this.setXdatStoredSearchAllowedUserId=setXdatStoredSearchAllowedUserId;

	this.xdat_stored_search_id_fk=null;


	this.getxdat_stored_search_id=function() {
		return this.xdat_stored_search_id_fk;
	}


	this.setxdat_stored_search_id=function(v){
		this.xdat_stored_search_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="login"){
				return this.Login ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_stored_search_allowed_user_id"){
				return this.XdatStoredSearchAllowedUserId ;
			} else 
			if(xmlPath=="xdat_stored_search_id"){
				return this.xdat_stored_search_id_fk ;
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
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_stored_search_allowed_user_id"){
				this.XdatStoredSearchAllowedUserId=value;
			} else 
			if(xmlPath=="xdat_stored_search_id"){
				this.xdat_stored_search_id_fk=value;
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
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:stored_search_allowed_user";
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
		xmlTxt+="\n</xdat:stored_search_allowed_user>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatStoredSearchAllowedUserId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_stored_search_allowed_user_id=\"" + this.XdatStoredSearchAllowedUserId + "\"";
			}
			if(this.xdat_stored_search_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_stored_search_id=\"" + this.xdat_stored_search_id_fk + "\"";
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
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatStoredSearchAllowedUserId!=null) return true;
			if (this.xdat_stored_search_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Login!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
