/*
 * web: arc_fieldSpecification.js
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

function arc_fieldSpecification(){
this.xsiType="arc:fieldSpecification";

	this.getSchemaElementName=function(){
		return "fieldSpecification";
	}

	this.getFullSchemaElementName=function(){
		return "arc:fieldSpecification";
	}

	this.Fieldspecification=null;


	function getFieldspecification() {
		return this.Fieldspecification;
	}
	this.getFieldspecification=getFieldspecification;


	function setFieldspecification(v){
		this.Fieldspecification=v;
	}
	this.setFieldspecification=setFieldspecification;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.ArcFieldspecificationId=null;


	function getArcFieldspecificationId() {
		return this.ArcFieldspecificationId;
	}
	this.getArcFieldspecificationId=getArcFieldspecificationId;


	function setArcFieldspecificationId(v){
		this.ArcFieldspecificationId=v;
	}
	this.setArcFieldspecificationId=setArcFieldspecificationId;

	this.fieldspecifications_fieldspecif_arc_archivespecification_id_fk=null;


	this.getfieldspecifications_fieldspecif_arc_archivespecification_id=function() {
		return this.fieldspecifications_fieldspecif_arc_archivespecification_id_fk;
	}


	this.setfieldspecifications_fieldspecif_arc_archivespecification_id=function(v){
		this.fieldspecifications_fieldspecif_arc_archivespecification_id_fk=v;
	}

	this.fieldspecifications_fieldspecif_arc_project_id_fk=null;


	this.getfieldspecifications_fieldspecif_arc_project_id=function() {
		return this.fieldspecifications_fieldspecif_arc_project_id_fk;
	}


	this.setfieldspecifications_fieldspecif_arc_project_id=function(v){
		this.fieldspecifications_fieldspecif_arc_project_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="fieldSpecification"){
				return this.Fieldspecification ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="arc_fieldSpecification_id"){
				return this.ArcFieldspecificationId ;
			} else 
			if(xmlPath=="fieldspecifications_fieldspecif_arc_archivespecification_id"){
				return this.fieldspecifications_fieldspecif_arc_archivespecification_id_fk ;
			} else 
			if(xmlPath=="fieldspecifications_fieldspecif_arc_project_id"){
				return this.fieldspecifications_fieldspecif_arc_project_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="fieldSpecification"){
				this.Fieldspecification=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="arc_fieldSpecification_id"){
				this.ArcFieldspecificationId=value;
			} else 
			if(xmlPath=="fieldspecifications_fieldspecif_arc_archivespecification_id"){
				this.fieldspecifications_fieldspecif_arc_archivespecification_id_fk=value;
			} else 
			if(xmlPath=="fieldspecifications_fieldspecif_arc_project_id"){
				this.fieldspecifications_fieldspecif_arc_project_id_fk=value;
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
		if (xmlPath=="fieldSpecification"){
			return "field_LONG_DATA";
		}else if (xmlPath=="name"){
			return "field_LONG_DATA";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<arc:fieldSpecification";
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
		xmlTxt+="\n</arc:fieldSpecification>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ArcFieldspecificationId!=null){
				if(hiddenCount++>0)str+=",";
				str+="arc_fieldSpecification_id=\"" + this.ArcFieldspecificationId + "\"";
			}
			if(this.fieldspecifications_fieldspecif_arc_archivespecification_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="fieldspecifications_fieldspecif_arc_archivespecification_id=\"" + this.fieldspecifications_fieldspecif_arc_archivespecification_id_fk + "\"";
			}
			if(this.fieldspecifications_fieldspecif_arc_project_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="fieldspecifications_fieldspecif_arc_project_id=\"" + this.fieldspecifications_fieldspecif_arc_project_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Fieldspecification!=null){
			xmlTxt+=this.Fieldspecification.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ArcFieldspecificationId!=null) return true;
			if (this.fieldspecifications_fieldspecif_arc_archivespecification_id_fk!=null) return true;
			if (this.fieldspecifications_fieldspecif_arc_project_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Fieldspecification!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
