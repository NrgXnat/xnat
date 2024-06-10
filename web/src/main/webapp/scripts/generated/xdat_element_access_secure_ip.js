/*
 * web: xdat_element_access_secure_ip.js
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

function xdat_element_access_secure_ip(){
this.xsiType="xdat:element_access_secure_ip";

	this.getSchemaElementName=function(){
		return "element_access_secure_ip";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:element_access_secure_ip";
	}

	this.SecureIp=null;


	function getSecureIp() {
		return this.SecureIp;
	}
	this.getSecureIp=getSecureIp;


	function setSecureIp(v){
		this.SecureIp=v;
	}
	this.setSecureIp=setSecureIp;

	this.XdatElementAccessSecureIpId=null;


	function getXdatElementAccessSecureIpId() {
		return this.XdatElementAccessSecureIpId;
	}
	this.getXdatElementAccessSecureIpId=getXdatElementAccessSecureIpId;


	function setXdatElementAccessSecureIpId(v){
		this.XdatElementAccessSecureIpId=v;
	}
	this.setXdatElementAccessSecureIpId=setXdatElementAccessSecureIpId;

	this.xdat_element_access_xdat_element_access_id_fk=null;


	this.getxdat_element_access_xdat_element_access_id=function() {
		return this.xdat_element_access_xdat_element_access_id_fk;
	}


	this.setxdat_element_access_xdat_element_access_id=function(v){
		this.xdat_element_access_xdat_element_access_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="secure_ip"){
				return this.SecureIp ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_element_access_secure_ip_id"){
				return this.XdatElementAccessSecureIpId ;
			} else 
			if(xmlPath=="xdat_element_access_xdat_element_access_id"){
				return this.xdat_element_access_xdat_element_access_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="secure_ip"){
				this.SecureIp=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_element_access_secure_ip_id"){
				this.XdatElementAccessSecureIpId=value;
			} else 
			if(xmlPath=="xdat_element_access_xdat_element_access_id"){
				this.xdat_element_access_xdat_element_access_id_fk=value;
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
		if (xmlPath=="secure_ip"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:element_access_secure_ip";
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
		xmlTxt+="\n</xdat:element_access_secure_ip>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatElementAccessSecureIpId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_element_access_secure_ip_id=\"" + this.XdatElementAccessSecureIpId + "\"";
			}
			if(this.xdat_element_access_xdat_element_access_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_element_access_xdat_element_access_id=\"" + this.xdat_element_access_xdat_element_access_id_fk + "\"";
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
		if (this.SecureIp!=null){
			xmlTxt+="\n<xdat:secure_ip";
			xmlTxt+=">";
			xmlTxt+=this.SecureIp.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:secure_ip>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatElementAccessSecureIpId!=null) return true;
			if (this.xdat_element_access_xdat_element_access_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.SecureIp!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
