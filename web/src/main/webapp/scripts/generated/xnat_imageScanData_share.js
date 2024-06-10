/*
 * web: xnat_imageScanData_share.js
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

function xnat_imageScanData_share(){
this.xsiType="xnat:imageScanData_share";

	this.getSchemaElementName=function(){
		return "imageScanData_share";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:imageScanData_share";
	}

	this.Share=null;


	function getShare() {
		return this.Share;
	}
	this.getShare=getShare;


	function setShare(v){
		this.Share=v;
	}
	this.setShare=setShare;

	this.Label=null;


	function getLabel() {
		return this.Label;
	}
	this.getLabel=getLabel;


	function setLabel(v){
		this.Label=v;
	}
	this.setLabel=setLabel;

	this.Project=null;


	function getProject() {
		return this.Project;
	}
	this.getProject=getProject;


	function setProject(v){
		this.Project=v;
	}
	this.setProject=setProject;

	this.XnatImagescandataShareId=null;


	function getXnatImagescandataShareId() {
		return this.XnatImagescandataShareId;
	}
	this.getXnatImagescandataShareId=getXnatImagescandataShareId;


	function setXnatImagescandataShareId(v){
		this.XnatImagescandataShareId=v;
	}
	this.setXnatImagescandataShareId=setXnatImagescandataShareId;

	this.sharing_share_xnat_imageScanDat_xnat_imagescandata_id_fk=null;


	this.getsharing_share_xnat_imageScanDat_xnat_imagescandata_id=function() {
		return this.sharing_share_xnat_imageScanDat_xnat_imagescandata_id_fk;
	}


	this.setsharing_share_xnat_imageScanDat_xnat_imagescandata_id=function(v){
		this.sharing_share_xnat_imageScanDat_xnat_imagescandata_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="share"){
				return this.Share ;
			} else 
			if(xmlPath=="label"){
				return this.Label ;
			} else 
			if(xmlPath=="project"){
				return this.Project ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_imageScanData_share_id"){
				return this.XnatImagescandataShareId ;
			} else 
			if(xmlPath=="sharing_share_xnat_imageScanDat_xnat_imagescandata_id"){
				return this.sharing_share_xnat_imageScanDat_xnat_imagescandata_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="share"){
				this.Share=value;
			} else 
			if(xmlPath=="label"){
				this.Label=value;
			} else 
			if(xmlPath=="project"){
				this.Project=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_imageScanData_share_id"){
				this.XnatImagescandataShareId=value;
			} else 
			if(xmlPath=="sharing_share_xnat_imageScanDat_xnat_imagescandata_id"){
				this.sharing_share_xnat_imageScanDat_xnat_imagescandata_id_fk=value;
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
		if (xmlPath=="share"){
			return "field_data";
		}else if (xmlPath=="label"){
			return "field_data";
		}else if (xmlPath=="project"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:imageScanData_share";
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
		xmlTxt+="\n</xnat:imageScanData_share>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatImagescandataShareId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_imageScanData_share_id=\"" + this.XnatImagescandataShareId + "\"";
			}
			if(this.sharing_share_xnat_imageScanDat_xnat_imagescandata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="sharing_share_xnat_imageScanDat_xnat_imagescandata_id=\"" + this.sharing_share_xnat_imageScanDat_xnat_imagescandata_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Label!=null)
			attTxt+=" label=\"" +this.Label +"\"";
		//NOT REQUIRED FIELD

		if (this.Project!=null)
			attTxt+=" project=\"" +this.Project +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Share!=null){
			xmlTxt+=this.Share.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatImagescandataShareId!=null) return true;
			if (this.sharing_share_xnat_imageScanDat_xnat_imagescandata_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Share!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
