/*
 * web: prov_processStep_library.js
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

function prov_processStep_library(){
this.xsiType="prov:processStep_library";

	this.getSchemaElementName=function(){
		return "processStep_library";
	}

	this.getFullSchemaElementName=function(){
		return "prov:processStep_library";
	}

	this.Library=null;


	function getLibrary() {
		return this.Library;
	}
	this.getLibrary=getLibrary;


	function setLibrary(v){
		this.Library=v;
	}
	this.setLibrary=setLibrary;

	this.Version=null;


	function getVersion() {
		return this.Version;
	}
	this.getVersion=getVersion;


	function setVersion(v){
		this.Version=v;
	}
	this.setVersion=setVersion;

	this.ProvProcessstepLibraryId=null;


	function getProvProcessstepLibraryId() {
		return this.ProvProcessstepLibraryId;
	}
	this.getProvProcessstepLibraryId=getProvProcessstepLibraryId;


	function setProvProcessstepLibraryId(v){
		this.ProvProcessstepLibraryId=v;
	}
	this.setProvProcessstepLibraryId=setProvProcessstepLibraryId;

	this.prov_processStep_prov_processstep_id_fk=null;


	this.getprov_processStep_prov_processstep_id=function() {
		return this.prov_processStep_prov_processstep_id_fk;
	}


	this.setprov_processStep_prov_processstep_id=function(v){
		this.prov_processStep_prov_processstep_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="library"){
				return this.Library ;
			} else 
			if(xmlPath=="version"){
				return this.Version ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="prov_processStep_library_id"){
				return this.ProvProcessstepLibraryId ;
			} else 
			if(xmlPath=="prov_processStep_prov_processstep_id"){
				return this.prov_processStep_prov_processstep_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="library"){
				this.Library=value;
			} else 
			if(xmlPath=="version"){
				this.Version=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="prov_processStep_library_id"){
				this.ProvProcessstepLibraryId=value;
			} else 
			if(xmlPath=="prov_processStep_prov_processstep_id"){
				this.prov_processStep_prov_processstep_id_fk=value;
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
		if (xmlPath=="library"){
			return "field_data";
		}else if (xmlPath=="version"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<prov:processStep_library";
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
		xmlTxt+="\n</prov:processStep_library>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ProvProcessstepLibraryId!=null){
				if(hiddenCount++>0)str+=",";
				str+="prov_processStep_library_id=\"" + this.ProvProcessstepLibraryId + "\"";
			}
			if(this.prov_processStep_prov_processstep_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="prov_processStep_prov_processstep_id=\"" + this.prov_processStep_prov_processstep_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Version!=null)
			attTxt+=" version=\"" +this.Version +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Library!=null){
			xmlTxt+=this.Library.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ProvProcessstepLibraryId!=null) return true;
			if (this.prov_processStep_prov_processstep_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Library!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
