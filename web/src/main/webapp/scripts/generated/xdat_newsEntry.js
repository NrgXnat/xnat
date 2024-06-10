/*
 * web: xdat_newsEntry.js
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

function xdat_newsEntry(){
this.xsiType="xdat:newsEntry";

	this.getSchemaElementName=function(){
		return "newsEntry";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:newsEntry";
	}

	this.Date=null;


	function getDate() {
		return this.Date;
	}
	this.getDate=getDate;


	function setDate(v){
		this.Date=v;
	}
	this.setDate=setDate;

	this.Title=null;


	function getTitle() {
		return this.Title;
	}
	this.getTitle=getTitle;


	function setTitle(v){
		this.Title=v;
	}
	this.setTitle=setTitle;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;

	this.Link=null;


	function getLink() {
		return this.Link;
	}
	this.getLink=getLink;


	function setLink(v){
		this.Link=v;
	}
	this.setLink=setLink;

	this.XdatNewsentryId=null;


	function getXdatNewsentryId() {
		return this.XdatNewsentryId;
	}
	this.getXdatNewsentryId=getXdatNewsentryId;


	function setXdatNewsentryId(v){
		this.XdatNewsentryId=v;
	}
	this.setXdatNewsentryId=setXdatNewsentryId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="date"){
				return this.Date ;
			} else 
			if(xmlPath=="title"){
				return this.Title ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="link"){
				return this.Link ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_newsEntry_id"){
				return this.XdatNewsentryId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="date"){
				this.Date=value;
			} else 
			if(xmlPath=="title"){
				this.Title=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="link"){
				this.Link=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_newsEntry_id"){
				this.XdatNewsentryId=value;
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
		if (xmlPath=="date"){
			return "field_data";
		}else if (xmlPath=="title"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_LONG_DATA";
		}else if (xmlPath=="link"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:News";
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
		xmlTxt+="\n</xdat:News>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatNewsentryId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_newsEntry_id=\"" + this.XdatNewsentryId + "\"";
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
		if (this.Date!=null){
			xmlTxt+="\n<xdat:date";
			xmlTxt+=">";
			xmlTxt+=this.Date;
			xmlTxt+="</xdat:date>";
		}
		if (this.Title!=null){
			xmlTxt+="\n<xdat:title";
			xmlTxt+=">";
			xmlTxt+=this.Title.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:title>";
		}
		if (this.Description!=null){
			xmlTxt+="\n<xdat:description";
			xmlTxt+=">";
			xmlTxt+=this.Description.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:description>";
		}
		if (this.Link!=null){
			xmlTxt+="\n<xdat:link";
			xmlTxt+=">";
			xmlTxt+=this.Link.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:link>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatNewsentryId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Date!=null) return true;
		if (this.Title!=null) return true;
		if (this.Description!=null) return true;
		if (this.Link!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
