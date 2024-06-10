/*
 * web: xnat_projectParticipant.js
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

function xnat_projectParticipant(){
this.xsiType="xnat:projectParticipant";

	this.getSchemaElementName=function(){
		return "projectParticipant";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:projectParticipant";
	}

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

	this.SubjectId=null;


	function getSubjectId() {
		return this.SubjectId;
	}
	this.getSubjectId=getSubjectId;


	function setSubjectId(v){
		this.SubjectId=v;
	}
	this.setSubjectId=setSubjectId;

	this.Group=null;


	function getGroup() {
		return this.Group;
	}
	this.getGroup=getGroup;


	function setGroup(v){
		this.Group=v;
	}
	this.setGroup=setGroup;

	this.XnatProjectparticipantId=null;


	function getXnatProjectparticipantId() {
		return this.XnatProjectparticipantId;
	}
	this.getXnatProjectparticipantId=getXnatProjectparticipantId;


	function setXnatProjectparticipantId(v){
		this.XnatProjectparticipantId=v;
	}
	this.setXnatProjectparticipantId=setXnatProjectparticipantId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="label"){
				return this.Label ;
			} else 
			if(xmlPath=="project"){
				return this.Project ;
			} else 
			if(xmlPath=="subject_ID"){
				return this.SubjectId ;
			} else 
			if(xmlPath=="group"){
				return this.Group ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_projectParticipant_id"){
				return this.XnatProjectparticipantId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="label"){
				this.Label=value;
			} else 
			if(xmlPath=="project"){
				this.Project=value;
			} else 
			if(xmlPath=="subject_ID"){
				this.SubjectId=value;
			} else 
			if(xmlPath=="group"){
				this.Group=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_projectParticipant_id"){
				this.XnatProjectparticipantId=value;
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
		if (xmlPath=="label"){
			return "field_data";
		}else if (xmlPath=="project"){
			return "field_data";
		}else if (xmlPath=="subject_ID"){
			return "field_data";
		}else if (xmlPath=="group"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:projectParticipant";
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
		xmlTxt+="\n</xnat:projectParticipant>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatProjectparticipantId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_projectParticipant_id=\"" + this.XnatProjectparticipantId + "\"";
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

		if (this.SubjectId!=null)
			attTxt+=" subject_ID=\"" +this.SubjectId +"\"";
		//NOT REQUIRED FIELD

		if (this.Group!=null)
			attTxt+=" group=\"" +this.Group +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatProjectparticipantId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		return false;
	}
}
