/*
 * web: cat_entry_tag.js
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

function cat_entry_tag(){
this.xsiType="cat:entry_tag";

	this.getSchemaElementName=function(){
		return "entry_tag";
	}

	this.getFullSchemaElementName=function(){
		return "cat:entry_tag";
	}

	this.Tag=null;


	function getTag() {
		return this.Tag;
	}
	this.getTag=getTag;


	function setTag(v){
		this.Tag=v;
	}
	this.setTag=setTag;

	this.CatEntryTagId=null;


	function getCatEntryTagId() {
		return this.CatEntryTagId;
	}
	this.getCatEntryTagId=getCatEntryTagId;


	function setCatEntryTagId(v){
		this.CatEntryTagId=v;
	}
	this.setCatEntryTagId=setCatEntryTagId;

	this.tags_tag_cat_entry_cat_entry_id_fk=null;


	this.gettags_tag_cat_entry_cat_entry_id=function() {
		return this.tags_tag_cat_entry_cat_entry_id_fk;
	}


	this.settags_tag_cat_entry_cat_entry_id=function(v){
		this.tags_tag_cat_entry_cat_entry_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="tag"){
				return this.Tag ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="cat_entry_tag_id"){
				return this.CatEntryTagId ;
			} else 
			if(xmlPath=="tags_tag_cat_entry_cat_entry_id"){
				return this.tags_tag_cat_entry_cat_entry_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="tag"){
				this.Tag=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="cat_entry_tag_id"){
				this.CatEntryTagId=value;
			} else 
			if(xmlPath=="tags_tag_cat_entry_cat_entry_id"){
				this.tags_tag_cat_entry_cat_entry_id_fk=value;
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
		if (xmlPath=="tag"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<cat:entry_tag";
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
		xmlTxt+="\n</cat:entry_tag>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.CatEntryTagId!=null){
				if(hiddenCount++>0)str+=",";
				str+="cat_entry_tag_id=\"" + this.CatEntryTagId + "\"";
			}
			if(this.tags_tag_cat_entry_cat_entry_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="tags_tag_cat_entry_cat_entry_id=\"" + this.tags_tag_cat_entry_cat_entry_id_fk + "\"";
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
		if (this.Tag!=null){
			xmlTxt+="\n<cat:tag";
			xmlTxt+=">";
			xmlTxt+=this.Tag.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</cat:tag>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.CatEntryTagId!=null) return true;
			if (this.tags_tag_cat_entry_cat_entry_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Tag!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
