/*
 * web: scr_screeningScanData.js
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

function scr_screeningScanData(){
this.xsiType="scr:screeningScanData";

	this.getSchemaElementName=function(){
		return "screeningScanData";
	}

	this.getFullSchemaElementName=function(){
		return "scr:screeningScanData";
	}

	this.ImagescanId=null;


	function getImagescanId() {
		return this.ImagescanId;
	}
	this.getImagescanId=getImagescanId;


	function setImagescanId(v){
		this.ImagescanId=v;
	}
	this.setImagescanId=setImagescanId;

	this.Comments=null;


	function getComments() {
		return this.Comments;
	}
	this.getComments=getComments;


	function setComments(v){
		this.Comments=v;
	}
	this.setComments=setComments;

	this.Pass=null;


	function getPass() {
		return this.Pass;
	}
	this.getPass=getPass;


	function setPass(v){
		this.Pass=v;
	}
	this.setPass=setPass;

	this.ScrScreeningscandataId=null;


	function getScrScreeningscandataId() {
		return this.ScrScreeningscandataId;
	}
	this.getScrScreeningscandataId=getScrScreeningscandataId;


	function setScrScreeningscandataId(v){
		this.ScrScreeningscandataId=v;
	}
	this.setScrScreeningscandataId=setScrScreeningscandataId;

	this.scans_scan_scr_screeningAssessm_id_fk=null;


	this.getscans_scan_scr_screeningAssessm_id=function() {
		return this.scans_scan_scr_screeningAssessm_id_fk;
	}


	this.setscans_scan_scr_screeningAssessm_id=function(v){
		this.scans_scan_scr_screeningAssessm_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageScan_ID"){
				return this.ImagescanId ;
			} else 
			if(xmlPath=="comments"){
				return this.Comments ;
			} else 
			if(xmlPath=="pass"){
				return this.Pass ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="scr_screeningScanData_id"){
				return this.ScrScreeningscandataId ;
			} else 
			if(xmlPath=="scans_scan_scr_screeningAssessm_id"){
				return this.scans_scan_scr_screeningAssessm_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageScan_ID"){
				this.ImagescanId=value;
			} else 
			if(xmlPath=="comments"){
				this.Comments=value;
			} else 
			if(xmlPath=="pass"){
				this.Pass=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="scr_screeningScanData_id"){
				this.ScrScreeningscandataId=value;
			} else 
			if(xmlPath=="scans_scan_scr_screeningAssessm_id"){
				this.scans_scan_scr_screeningAssessm_id_fk=value;
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
		if (xmlPath=="imageScan_ID"){
			return "field_data";
		}else if (xmlPath=="comments"){
			return "field_LONG_DATA";
		}else if (xmlPath=="pass"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<scr:screeningScanData";
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
		xmlTxt+="\n</scr:screeningScanData>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ScrScreeningscandataId!=null){
				if(hiddenCount++>0)str+=",";
				str+="scr_screeningScanData_id=\"" + this.ScrScreeningscandataId + "\"";
			}
			if(this.scans_scan_scr_screeningAssessm_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="scans_scan_scr_screeningAssessm_id=\"" + this.scans_scan_scr_screeningAssessm_id_fk + "\"";
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
		if (this.ImagescanId!=null){
			xmlTxt+="\n<scr:imageScan_ID";
			xmlTxt+=">";
			xmlTxt+=this.ImagescanId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</scr:imageScan_ID>";
		}
		if (this.Comments!=null){
			xmlTxt+="\n<scr:comments";
			xmlTxt+=">";
			xmlTxt+=this.Comments.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</scr:comments>";
		}
		if (this.Pass!=null){
			xmlTxt+="\n<scr:pass";
			xmlTxt+=">";
			xmlTxt+=this.Pass.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</scr:pass>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ScrScreeningscandataId!=null) return true;
			if (this.scans_scan_scr_screeningAssessm_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.ImagescanId!=null) return true;
		if (this.Comments!=null) return true;
		if (this.Pass!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
