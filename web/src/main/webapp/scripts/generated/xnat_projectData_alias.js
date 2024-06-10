/*
 * web: xnat_projectData_alias.js
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

function xnat_projectData_alias(){
this.xsiType="xnat:projectData_alias";

	this.getSchemaElementName=function(){
		return "projectData_alias";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:projectData_alias";
	}

	this.Alias=null;


	function getAlias() {
		return this.Alias;
	}
	this.getAlias=getAlias;


	function setAlias(v){
		this.Alias=v;
	}
	this.setAlias=setAlias;

	this.Source=null;


	function getSource() {
		return this.Source;
	}
	this.getSource=getSource;


	function setSource(v){
		this.Source=v;
	}
	this.setSource=setSource;

	this.XnatProjectdataAliasId=null;


	function getXnatProjectdataAliasId() {
		return this.XnatProjectdataAliasId;
	}
	this.getXnatProjectdataAliasId=getXnatProjectdataAliasId;


	function setXnatProjectdataAliasId(v){
		this.XnatProjectdataAliasId=v;
	}
	this.setXnatProjectdataAliasId=setXnatProjectdataAliasId;

	this.aliases_alias_xnat_projectData_id_fk=null;


	this.getaliases_alias_xnat_projectData_id=function() {
		return this.aliases_alias_xnat_projectData_id_fk;
	}


	this.setaliases_alias_xnat_projectData_id=function(v){
		this.aliases_alias_xnat_projectData_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="alias"){
				return this.Alias ;
			} else 
			if(xmlPath=="source"){
				return this.Source ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_projectData_alias_id"){
				return this.XnatProjectdataAliasId ;
			} else 
			if(xmlPath=="aliases_alias_xnat_projectData_id"){
				return this.aliases_alias_xnat_projectData_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="alias"){
				this.Alias=value;
			} else 
			if(xmlPath=="source"){
				this.Source=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_projectData_alias_id"){
				this.XnatProjectdataAliasId=value;
			} else 
			if(xmlPath=="aliases_alias_xnat_projectData_id"){
				this.aliases_alias_xnat_projectData_id_fk=value;
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
		if (xmlPath=="alias"){
			return "field_data";
		}else if (xmlPath=="source"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:projectData_alias";
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
		xmlTxt+="\n</xnat:projectData_alias>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatProjectdataAliasId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_projectData_alias_id=\"" + this.XnatProjectdataAliasId + "\"";
			}
			if(this.aliases_alias_xnat_projectData_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="aliases_alias_xnat_projectData_id=\"" + this.aliases_alias_xnat_projectData_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Source!=null)
			attTxt+=" source=\"" +this.Source +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Alias!=null){
			xmlTxt+=this.Alias.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatProjectdataAliasId!=null) return true;
			if (this.aliases_alias_xnat_projectData_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Alias!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
