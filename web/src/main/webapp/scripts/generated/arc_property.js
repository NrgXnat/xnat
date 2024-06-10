/*
 * web: arc_property.js
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

function arc_property(){
this.xsiType="arc:property";

	this.getSchemaElementName=function(){
		return "property";
	}

	this.getFullSchemaElementName=function(){
		return "arc:property";
	}

	this.Property=null;


	function getProperty() {
		return this.Property;
	}
	this.getProperty=getProperty;


	function setProperty(v){
		this.Property=v;
	}
	this.setProperty=setProperty;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.ArcPropertyId=null;


	function getArcPropertyId() {
		return this.ArcPropertyId;
	}
	this.getArcPropertyId=getArcPropertyId;


	function setArcPropertyId(v){
		this.ArcPropertyId=v;
	}
	this.setArcPropertyId=setArcPropertyId;

	this.properties_property_arc_project_arc_project_id_fk=null;


	this.getproperties_property_arc_project_arc_project_id=function() {
		return this.properties_property_arc_project_arc_project_id_fk;
	}


	this.setproperties_property_arc_project_arc_project_id=function(v){
		this.properties_property_arc_project_arc_project_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="property"){
				return this.Property ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="arc_property_id"){
				return this.ArcPropertyId ;
			} else 
			if(xmlPath=="properties_property_arc_project_arc_project_id"){
				return this.properties_property_arc_project_arc_project_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="property"){
				this.Property=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="arc_property_id"){
				this.ArcPropertyId=value;
			} else 
			if(xmlPath=="properties_property_arc_project_arc_project_id"){
				this.properties_property_arc_project_arc_project_id_fk=value;
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
		if (xmlPath=="property"){
			return "field_LONG_DATA";
		}else if (xmlPath=="name"){
			return "field_LONG_DATA";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<arc:property";
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
		xmlTxt+="\n</arc:property>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ArcPropertyId!=null){
				if(hiddenCount++>0)str+=",";
				str+="arc_property_id=\"" + this.ArcPropertyId + "\"";
			}
			if(this.properties_property_arc_project_arc_project_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="properties_property_arc_project_arc_project_id=\"" + this.properties_property_arc_project_arc_project_id_fk + "\"";
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
		if (this.Property!=null){
			xmlTxt+=this.Property.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ArcPropertyId!=null) return true;
			if (this.properties_property_arc_project_arc_project_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Property!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
