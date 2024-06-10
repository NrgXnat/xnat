/*
 * web: val_protocolData_condition.js
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

function val_protocolData_condition(){
this.xsiType="val:protocolData_condition";

	this.getSchemaElementName=function(){
		return "protocolData_condition";
	}

	this.getFullSchemaElementName=function(){
		return "val:protocolData_condition";
	}

	this.Verified=null;


	function getVerified() {
		return this.Verified;
	}
	this.getVerified=getVerified;


	function setVerified(v){
		this.Verified=v;
	}
	this.setVerified=setVerified;

	this.Diagnosis=null;


	function getDiagnosis() {
		return this.Diagnosis;
	}
	this.getDiagnosis=getDiagnosis;


	function setDiagnosis(v){
		this.Diagnosis=v;
	}
	this.setDiagnosis=setDiagnosis;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.Status=null;


	function getStatus() {
		return this.Status;
	}
	this.getStatus=getStatus;


	function setStatus(v){
		this.Status=v;
	}
	this.setStatus=setStatus;

	this.Xmlpath=null;


	function getXmlpath() {
		return this.Xmlpath;
	}
	this.getXmlpath=getXmlpath;


	function setXmlpath(v){
		this.Xmlpath=v;
	}
	this.setXmlpath=setXmlpath;

	this.ValProtocoldataConditionId=null;


	function getValProtocoldataConditionId() {
		return this.ValProtocoldataConditionId;
	}
	this.getValProtocoldataConditionId=getValProtocoldataConditionId;


	function setValProtocoldataConditionId(v){
		this.ValProtocoldataConditionId=v;
	}
	this.setValProtocoldataConditionId=setValProtocoldataConditionId;

	this.check_conditions_condition_val__id_fk=null;


	this.getcheck_conditions_condition_val__id=function() {
		return this.check_conditions_condition_val__id_fk;
	}


	this.setcheck_conditions_condition_val__id=function(v){
		this.check_conditions_condition_val__id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="verified"){
				return this.Verified ;
			} else 
			if(xmlPath=="diagnosis"){
				return this.Diagnosis ;
			} else 
			if(xmlPath=="ID"){
				return this.Id ;
			} else 
			if(xmlPath=="status"){
				return this.Status ;
			} else 
			if(xmlPath=="xmlpath"){
				return this.Xmlpath ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="val_protocolData_condition_id"){
				return this.ValProtocoldataConditionId ;
			} else 
			if(xmlPath=="check_conditions_condition_val__id"){
				return this.check_conditions_condition_val__id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="verified"){
				this.Verified=value;
			} else 
			if(xmlPath=="diagnosis"){
				this.Diagnosis=value;
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="status"){
				this.Status=value;
			} else 
			if(xmlPath=="xmlpath"){
				this.Xmlpath=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="val_protocolData_condition_id"){
				this.ValProtocoldataConditionId=value;
			} else 
			if(xmlPath=="check_conditions_condition_val__id"){
				this.check_conditions_condition_val__id_fk=value;
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
		if (xmlPath=="verified"){
			return "field_data";
		}else if (xmlPath=="diagnosis"){
			return "field_LONG_DATA";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="status"){
			return "field_data";
		}else if (xmlPath=="xmlpath"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<val:protocolData_condition";
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
		xmlTxt+="\n</val:protocolData_condition>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ValProtocoldataConditionId!=null){
				if(hiddenCount++>0)str+=",";
				str+="val_protocolData_condition_id=\"" + this.ValProtocoldataConditionId + "\"";
			}
			if(this.check_conditions_condition_val__id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="check_conditions_condition_val__id=\"" + this.check_conditions_condition_val__id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Id!=null)
			attTxt+=" ID=\"" +this.Id +"\"";
		//NOT REQUIRED FIELD

		if (this.Status!=null)
			attTxt+=" status=\"" +this.Status +"\"";
		//NOT REQUIRED FIELD

		if (this.Xmlpath!=null)
			attTxt+=" xmlpath=\"" +this.Xmlpath +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Verified!=null){
			xmlTxt+="\n<val:verified";
			xmlTxt+=">";
			xmlTxt+=this.Verified.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</val:verified>";
		}
		if (this.Diagnosis!=null){
			xmlTxt+="\n<val:diagnosis";
			xmlTxt+=">";
			xmlTxt+=this.Diagnosis.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</val:diagnosis>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ValProtocoldataConditionId!=null) return true;
			if (this.check_conditions_condition_val__id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Verified!=null) return true;
		if (this.Diagnosis!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
