/*
 * web: xnat_fieldDefinitionGroup_field_possibleValue.js
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

function xnat_fieldDefinitionGroup_field_possibleValue(){
this.xsiType="xnat:fieldDefinitionGroup_field_possibleValue";

	this.getSchemaElementName=function(){
		return "fieldDefinitionGroup_field_possibleValue";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:fieldDefinitionGroup_field_possibleValue";
	}

	this.Possiblevalue=null;


	function getPossiblevalue() {
		return this.Possiblevalue;
	}
	this.getPossiblevalue=getPossiblevalue;


	function setPossiblevalue(v){
		this.Possiblevalue=v;
	}
	this.setPossiblevalue=setPossiblevalue;

	this.Display=null;


	function getDisplay() {
		return this.Display;
	}
	this.getDisplay=getDisplay;


	function setDisplay(v){
		this.Display=v;
	}
	this.setDisplay=setDisplay;

	this.XnatFielddefinitiongroupFieldPossiblevalueId=null;


	function getXnatFielddefinitiongroupFieldPossiblevalueId() {
		return this.XnatFielddefinitiongroupFieldPossiblevalueId;
	}
	this.getXnatFielddefinitiongroupFieldPossiblevalueId=getXnatFielddefinitiongroupFieldPossiblevalueId;


	function setXnatFielddefinitiongroupFieldPossiblevalueId(v){
		this.XnatFielddefinitiongroupFieldPossiblevalueId=v;
	}
	this.setXnatFielddefinitiongroupFieldPossiblevalueId=setXnatFielddefinitiongroupFieldPossiblevalueId;

	this.possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field_fk=null;


	this.getpossiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field=function() {
		return this.possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field_fk;
	}


	this.setpossiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field=function(v){
		this.possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="possibleValue"){
				return this.Possiblevalue ;
			} else 
			if(xmlPath=="display"){
				return this.Display ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_fieldDefinitionGroup_field_possibleValue_id"){
				return this.XnatFielddefinitiongroupFieldPossiblevalueId ;
			} else 
			if(xmlPath=="possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field"){
				return this.possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="possibleValue"){
				this.Possiblevalue=value;
			} else 
			if(xmlPath=="display"){
				this.Display=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_fieldDefinitionGroup_field_possibleValue_id"){
				this.XnatFielddefinitiongroupFieldPossiblevalueId=value;
			} else 
			if(xmlPath=="possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field"){
				this.possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field_fk=value;
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
		if (xmlPath=="possibleValue"){
			return "field_data";
		}else if (xmlPath=="display"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:fieldDefinitionGroup_field_possibleValue";
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
		xmlTxt+="\n</xnat:fieldDefinitionGroup_field_possibleValue>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatFielddefinitiongroupFieldPossiblevalueId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_fieldDefinitionGroup_field_possibleValue_id=\"" + this.XnatFielddefinitiongroupFieldPossiblevalueId + "\"";
			}
			if(this.possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field=\"" + this.possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Display!=null)
			attTxt+=" display=\"" +this.Display +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Possiblevalue!=null){
			xmlTxt+=this.Possiblevalue.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatFielddefinitiongroupFieldPossiblevalueId!=null) return true;
			if (this.possiblevalues_possiblevalue_xn_xnat_fielddefinitiongroup_field_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Possiblevalue!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
