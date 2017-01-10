/*
 * web: arc_pathInfo.js
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

function arc_pathInfo(){
this.xsiType="arc:pathInfo";

	this.getSchemaElementName=function(){
		return "pathInfo";
	}

	this.getFullSchemaElementName=function(){
		return "arc:pathInfo";
	}

	this.Archivepath=null;


	function getArchivepath() {
		return this.Archivepath;
	}
	this.getArchivepath=getArchivepath;


	function setArchivepath(v){
		this.Archivepath=v;
	}
	this.setArchivepath=setArchivepath;

	this.Prearchivepath=null;


	function getPrearchivepath() {
		return this.Prearchivepath;
	}
	this.getPrearchivepath=getPrearchivepath;


	function setPrearchivepath(v){
		this.Prearchivepath=v;
	}
	this.setPrearchivepath=setPrearchivepath;

	this.Cachepath=null;


	function getCachepath() {
		return this.Cachepath;
	}
	this.getCachepath=getCachepath;


	function setCachepath(v){
		this.Cachepath=v;
	}
	this.setCachepath=setCachepath;

	this.Buildpath=null;


	function getBuildpath() {
		return this.Buildpath;
	}
	this.getBuildpath=getBuildpath;


	function setBuildpath(v){
		this.Buildpath=v;
	}
	this.setBuildpath=setBuildpath;

	this.Ftppath=null;


	function getFtppath() {
		return this.Ftppath;
	}
	this.getFtppath=getFtppath;


	function setFtppath(v){
		this.Ftppath=v;
	}
	this.setFtppath=setFtppath;

	this.Pipelinepath=null;


	function getPipelinepath() {
		return this.Pipelinepath;
	}
	this.getPipelinepath=getPipelinepath;


	function setPipelinepath(v){
		this.Pipelinepath=v;
	}
	this.setPipelinepath=setPipelinepath;

	this.ArcPathinfoId=null;


	function getArcPathinfoId() {
		return this.ArcPathinfoId;
	}
	this.getArcPathinfoId=getArcPathinfoId;


	function setArcPathinfoId(v){
		this.ArcPathinfoId=v;
	}
	this.setArcPathinfoId=setArcPathinfoId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="archivePath"){
				return this.Archivepath ;
			} else 
			if(xmlPath=="prearchivePath"){
				return this.Prearchivepath ;
			} else 
			if(xmlPath=="cachePath"){
				return this.Cachepath ;
			} else 
			if(xmlPath=="buildPath"){
				return this.Buildpath ;
			} else 
			if(xmlPath=="ftpPath"){
				return this.Ftppath ;
			} else 
			if(xmlPath=="pipelinePath"){
				return this.Pipelinepath ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="arc_pathInfo_id"){
				return this.ArcPathinfoId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="archivePath"){
				this.Archivepath=value;
			} else 
			if(xmlPath=="prearchivePath"){
				this.Prearchivepath=value;
			} else 
			if(xmlPath=="cachePath"){
				this.Cachepath=value;
			} else 
			if(xmlPath=="buildPath"){
				this.Buildpath=value;
			} else 
			if(xmlPath=="ftpPath"){
				this.Ftppath=value;
			} else 
			if(xmlPath=="pipelinePath"){
				this.Pipelinepath=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="arc_pathInfo_id"){
				this.ArcPathinfoId=value;
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
		if (xmlPath=="archivePath"){
			return "field_data";
		}else if (xmlPath=="prearchivePath"){
			return "field_data";
		}else if (xmlPath=="cachePath"){
			return "field_data";
		}else if (xmlPath=="buildPath"){
			return "field_data";
		}else if (xmlPath=="ftpPath"){
			return "field_data";
		}else if (xmlPath=="pipelinePath"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<arc:pathInfo";
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
		xmlTxt+="\n</arc:pathInfo>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ArcPathinfoId!=null){
				if(hiddenCount++>0)str+=",";
				str+="arc_pathInfo_id=\"" + this.ArcPathinfoId + "\"";
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
		if (this.Archivepath!=null){
			xmlTxt+="\n<arc:archivePath";
			xmlTxt+=">";
			xmlTxt+=this.Archivepath.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:archivePath>";
		}
		if (this.Prearchivepath!=null){
			xmlTxt+="\n<arc:prearchivePath";
			xmlTxt+=">";
			xmlTxt+=this.Prearchivepath.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:prearchivePath>";
		}
		if (this.Cachepath!=null){
			xmlTxt+="\n<arc:cachePath";
			xmlTxt+=">";
			xmlTxt+=this.Cachepath.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:cachePath>";
		}
		if (this.Buildpath!=null){
			xmlTxt+="\n<arc:buildPath";
			xmlTxt+=">";
			xmlTxt+=this.Buildpath.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:buildPath>";
		}
		if (this.Ftppath!=null){
			xmlTxt+="\n<arc:ftpPath";
			xmlTxt+=">";
			xmlTxt+=this.Ftppath.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:ftpPath>";
		}
		if (this.Pipelinepath!=null){
			xmlTxt+="\n<arc:pipelinePath";
			xmlTxt+=">";
			xmlTxt+=this.Pipelinepath.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:pipelinePath>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ArcPathinfoId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Archivepath!=null) return true;
		if (this.Prearchivepath!=null) return true;
		if (this.Cachepath!=null) return true;
		if (this.Buildpath!=null) return true;
		if (this.Ftppath!=null) return true;
		if (this.Pipelinepath!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
