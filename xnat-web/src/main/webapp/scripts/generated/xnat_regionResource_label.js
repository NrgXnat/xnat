/*
 * web: xnat_regionResource_label.js
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

function xnat_regionResource_label(){
this.xsiType="xnat:regionResource_label";

	this.getSchemaElementName=function(){
		return "regionResource_label";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:regionResource_label";
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

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.Hemisphere=null;


	function getHemisphere() {
		return this.Hemisphere;
	}
	this.getHemisphere=getHemisphere;


	function setHemisphere(v){
		this.Hemisphere=v;
	}
	this.setHemisphere=setHemisphere;

	this.XnatRegionresourceLabelId=null;


	function getXnatRegionresourceLabelId() {
		return this.XnatRegionresourceLabelId;
	}
	this.getXnatRegionresourceLabelId=getXnatRegionresourceLabelId;


	function setXnatRegionresourceLabelId(v){
		this.XnatRegionresourceLabelId=v;
	}
	this.setXnatRegionresourceLabelId=setXnatRegionresourceLabelId;

	this.subregionlabels_label_xnat_regi_xnat_regionresource_id_fk=null;


	this.getsubregionlabels_label_xnat_regi_xnat_regionresource_id=function() {
		return this.subregionlabels_label_xnat_regi_xnat_regionresource_id_fk;
	}


	this.setsubregionlabels_label_xnat_regi_xnat_regionresource_id=function(v){
		this.subregionlabels_label_xnat_regi_xnat_regionresource_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="label"){
				return this.Label ;
			} else 
			if(xmlPath=="id"){
				return this.Id ;
			} else 
			if(xmlPath=="hemisphere"){
				return this.Hemisphere ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_regionResource_label_id"){
				return this.XnatRegionresourceLabelId ;
			} else 
			if(xmlPath=="subregionlabels_label_xnat_regi_xnat_regionresource_id"){
				return this.subregionlabels_label_xnat_regi_xnat_regionresource_id_fk ;
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
			if(xmlPath=="id"){
				this.Id=value;
			} else 
			if(xmlPath=="hemisphere"){
				this.Hemisphere=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_regionResource_label_id"){
				this.XnatRegionresourceLabelId=value;
			} else 
			if(xmlPath=="subregionlabels_label_xnat_regi_xnat_regionresource_id"){
				this.subregionlabels_label_xnat_regi_xnat_regionresource_id_fk=value;
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
		}else if (xmlPath=="id"){
			return "field_data";
		}else if (xmlPath=="hemisphere"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:regionResource_label";
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
		xmlTxt+="\n</xnat:regionResource_label>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatRegionresourceLabelId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_regionResource_label_id=\"" + this.XnatRegionresourceLabelId + "\"";
			}
			if(this.subregionlabels_label_xnat_regi_xnat_regionresource_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="subregionlabels_label_xnat_regi_xnat_regionresource_id=\"" + this.subregionlabels_label_xnat_regi_xnat_regionresource_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Id!=null)
			attTxt+=" id=\"" +this.Id +"\"";
		else attTxt+=" id=\"\"";//REQUIRED FIELD

		if (this.Hemisphere!=null)
			attTxt+=" hemisphere=\"" +this.Hemisphere +"\"";
		else attTxt+=" hemisphere=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Label!=null){
			xmlTxt+=this.Label.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatRegionresourceLabelId!=null) return true;
			if (this.subregionlabels_label_xnat_regi_xnat_regionresource_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Label!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
