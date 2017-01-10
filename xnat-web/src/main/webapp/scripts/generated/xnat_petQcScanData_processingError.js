/*
 * web: xnat_petQcScanData_processingError.js
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

function xnat_petQcScanData_processingError(){
this.xsiType="xnat:petQcScanData_processingError";

	this.getSchemaElementName=function(){
		return "petQcScanData_processingError";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:petQcScanData_processingError";
	}

	this.Processingerror=null;


	function getProcessingerror() {
		return this.Processingerror;
	}
	this.getProcessingerror=getProcessingerror;


	function setProcessingerror(v){
		this.Processingerror=v;
	}
	this.setProcessingerror=setProcessingerror;

	this.XnatPetqcscandataProcessingerrorId=null;


	function getXnatPetqcscandataProcessingerrorId() {
		return this.XnatPetqcscandataProcessingerrorId;
	}
	this.getXnatPetqcscandataProcessingerrorId=getXnatPetqcscandataProcessingerrorId;


	function setXnatPetqcscandataProcessingerrorId(v){
		this.XnatPetqcscandataProcessingerrorId=v;
	}
	this.setXnatPetqcscandataProcessingerrorId=setXnatPetqcscandataProcessingerrorId;

	this.processingerrors_processingerro_xnat_qcscandata_id_fk=null;


	this.getprocessingerrors_processingerro_xnat_qcscandata_id=function() {
		return this.processingerrors_processingerro_xnat_qcscandata_id_fk;
	}


	this.setprocessingerrors_processingerro_xnat_qcscandata_id=function(v){
		this.processingerrors_processingerro_xnat_qcscandata_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="processingError"){
				return this.Processingerror ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_petQcScanData_processingError_id"){
				return this.XnatPetqcscandataProcessingerrorId ;
			} else 
			if(xmlPath=="processingerrors_processingerro_xnat_qcscandata_id"){
				return this.processingerrors_processingerro_xnat_qcscandata_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="processingError"){
				this.Processingerror=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_petQcScanData_processingError_id"){
				this.XnatPetqcscandataProcessingerrorId=value;
			} else 
			if(xmlPath=="processingerrors_processingerro_xnat_qcscandata_id"){
				this.processingerrors_processingerro_xnat_qcscandata_id_fk=value;
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
		if (xmlPath=="processingError"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:petQcScanData_processingError";
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
		xmlTxt+="\n</xnat:petQcScanData_processingError>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatPetqcscandataProcessingerrorId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_petQcScanData_processingError_id=\"" + this.XnatPetqcscandataProcessingerrorId + "\"";
			}
			if(this.processingerrors_processingerro_xnat_qcscandata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="processingerrors_processingerro_xnat_qcscandata_id=\"" + this.processingerrors_processingerro_xnat_qcscandata_id_fk + "\"";
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
		if (this.Processingerror!=null){
			xmlTxt+="\n<xnat:processingError";
			xmlTxt+=">";
			xmlTxt+=this.Processingerror.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:processingError>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatPetqcscandataProcessingerrorId!=null) return true;
			if (this.processingerrors_processingerro_xnat_qcscandata_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Processingerror!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
