/*
 * web: xnat_validationData.js
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

function xnat_validationData(){
this.xsiType="xnat:validationData";

	this.getSchemaElementName=function(){
		return "validationData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:validationData";
	}

	this.Method=null;


	function getMethod() {
		return this.Method;
	}
	this.getMethod=getMethod;


	function setMethod(v){
		this.Method=v;
	}
	this.setMethod=setMethod;

	this.Date=null;


	function getDate() {
		return this.Date;
	}
	this.getDate=getDate;


	function setDate(v){
		this.Date=v;
	}
	this.setDate=setDate;

	this.Notes=null;


	function getNotes() {
		return this.Notes;
	}
	this.getNotes=getNotes;


	function setNotes(v){
		this.Notes=v;
	}
	this.setNotes=setNotes;

	this.ValidatedBy=null;


	function getValidatedBy() {
		return this.ValidatedBy;
	}
	this.getValidatedBy=getValidatedBy;


	function setValidatedBy(v){
		this.ValidatedBy=v;
	}
	this.setValidatedBy=setValidatedBy;

	this.Status=null;


	function getStatus() {
		return this.Status;
	}
	this.getStatus=getStatus;


	function setStatus(v){
		this.Status=v;
	}
	this.setStatus=setStatus;

	this.XnatValidationdataId=null;


	function getXnatValidationdataId() {
		return this.XnatValidationdataId;
	}
	this.getXnatValidationdataId=getXnatValidationdataId;


	function setXnatValidationdataId(v){
		this.XnatValidationdataId=v;
	}
	this.setXnatValidationdataId=setXnatValidationdataId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="method"){
				return this.Method ;
			} else 
			if(xmlPath=="date"){
				return this.Date ;
			} else 
			if(xmlPath=="notes"){
				return this.Notes ;
			} else 
			if(xmlPath=="validated_by"){
				return this.ValidatedBy ;
			} else 
			if(xmlPath=="status"){
				return this.Status ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_validationData_id"){
				return this.XnatValidationdataId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="method"){
				this.Method=value;
			} else 
			if(xmlPath=="date"){
				this.Date=value;
			} else 
			if(xmlPath=="notes"){
				this.Notes=value;
			} else 
			if(xmlPath=="validated_by"){
				this.ValidatedBy=value;
			} else 
			if(xmlPath=="status"){
				this.Status=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_validationData_id"){
				this.XnatValidationdataId=value;
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
		if (xmlPath=="method"){
			return "field_data";
		}else if (xmlPath=="date"){
			return "field_data";
		}else if (xmlPath=="notes"){
			return "field_LONG_DATA";
		}else if (xmlPath=="validated_by"){
			return "field_data";
		}else if (xmlPath=="status"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:validationData";
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
		xmlTxt+="\n</xnat:validationData>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatValidationdataId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_validationData_id=\"" + this.XnatValidationdataId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Status!=null)
			attTxt+=" status=\"" +this.Status +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Method!=null){
			xmlTxt+="\n<xnat:method";
			xmlTxt+=">";
			xmlTxt+=this.Method.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:method>";
		}
		if (this.Date!=null){
			xmlTxt+="\n<xnat:date";
			xmlTxt+=">";
			xmlTxt+=this.Date;
			xmlTxt+="</xnat:date>";
		}
		if (this.Notes!=null){
			xmlTxt+="\n<xnat:notes";
			xmlTxt+=">";
			xmlTxt+=this.Notes.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:notes>";
		}
		if (this.ValidatedBy!=null){
			xmlTxt+="\n<xnat:validated_by";
			xmlTxt+=">";
			xmlTxt+=this.ValidatedBy.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:validated_by>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatValidationdataId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Method!=null) return true;
		if (this.Date!=null) return true;
		if (this.Notes!=null) return true;
		if (this.ValidatedBy!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
