/*
 * web: xdat_action_type.js
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

function xdat_action_type(){
this.xsiType="xdat:action_type";

	this.getSchemaElementName=function(){
		return "action_type";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:action_type";
	}

	this.ActionName=null;


	function getActionName() {
		return this.ActionName;
	}
	this.getActionName=getActionName;


	function setActionName(v){
		this.ActionName=v;
	}
	this.setActionName=setActionName;

	this.DisplayName=null;


	function getDisplayName() {
		return this.DisplayName;
	}
	this.getDisplayName=getDisplayName;


	function setDisplayName(v){
		this.DisplayName=v;
	}
	this.setDisplayName=setDisplayName;

	this.Sequence=null;


	function getSequence() {
		return this.Sequence;
	}
	this.getSequence=getSequence;


	function setSequence(v){
		this.Sequence=v;
	}
	this.setSequence=setSequence;

	this.actions_action_xdat_security_xdat_security_id_fk=null;


	this.getactions_action_xdat_security_xdat_security_id=function() {
		return this.actions_action_xdat_security_xdat_security_id_fk;
	}


	this.setactions_action_xdat_security_xdat_security_id=function(v){
		this.actions_action_xdat_security_xdat_security_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="action_name"){
				return this.ActionName ;
			} else 
			if(xmlPath=="display_name"){
				return this.DisplayName ;
			} else 
			if(xmlPath=="sequence"){
				return this.Sequence ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="actions_action_xdat_security_xdat_security_id"){
				return this.actions_action_xdat_security_xdat_security_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="action_name"){
				this.ActionName=value;
			} else 
			if(xmlPath=="display_name"){
				this.DisplayName=value;
			} else 
			if(xmlPath=="sequence"){
				this.Sequence=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="actions_action_xdat_security_xdat_security_id"){
				this.actions_action_xdat_security_xdat_security_id_fk=value;
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
		if (xmlPath=="action_name"){
			return "field_data";
		}else if (xmlPath=="display_name"){
			return "field_data";
		}else if (xmlPath=="sequence"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:action_type";
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
		xmlTxt+="\n</xdat:action_type>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.actions_action_xdat_security_xdat_security_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="actions_action_xdat_security_xdat_security_id=\"" + this.actions_action_xdat_security_xdat_security_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.ActionName!=null)
			attTxt+=" action_name=\"" +this.ActionName +"\"";
		else attTxt+=" action_name=\"\"";//REQUIRED FIELD

		if (this.DisplayName!=null)
			attTxt+=" display_name=\"" +this.DisplayName +"\"";
		//NOT REQUIRED FIELD

		if (this.Sequence!=null)
			attTxt+=" sequence=\"" +this.Sequence +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.actions_action_xdat_security_xdat_security_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		return false;
	}
}
