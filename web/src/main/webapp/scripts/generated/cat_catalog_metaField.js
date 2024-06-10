/*
 * web: cat_catalog_metaField.js
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

function cat_catalog_metaField(){
this.xsiType="cat:catalog_metaField";

	this.getSchemaElementName=function(){
		return "catalog_metaField";
	}

	this.getFullSchemaElementName=function(){
		return "cat:catalog_metaField";
	}

	this.Metafield=null;


	function getMetafield() {
		return this.Metafield;
	}
	this.getMetafield=getMetafield;


	function setMetafield(v){
		this.Metafield=v;
	}
	this.setMetafield=setMetafield;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.CatCatalogMetafieldId=null;


	function getCatCatalogMetafieldId() {
		return this.CatCatalogMetafieldId;
	}
	this.getCatCatalogMetafieldId=getCatCatalogMetafieldId;


	function setCatCatalogMetafieldId(v){
		this.CatCatalogMetafieldId=v;
	}
	this.setCatCatalogMetafieldId=setCatCatalogMetafieldId;

	this.metafields_metafield_cat_catalo_cat_catalog_id_fk=null;


	this.getmetafields_metafield_cat_catalo_cat_catalog_id=function() {
		return this.metafields_metafield_cat_catalo_cat_catalog_id_fk;
	}


	this.setmetafields_metafield_cat_catalo_cat_catalog_id=function(v){
		this.metafields_metafield_cat_catalo_cat_catalog_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="metaField"){
				return this.Metafield ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="cat_catalog_metaField_id"){
				return this.CatCatalogMetafieldId ;
			} else 
			if(xmlPath=="metafields_metafield_cat_catalo_cat_catalog_id"){
				return this.metafields_metafield_cat_catalo_cat_catalog_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="metaField"){
				this.Metafield=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="cat_catalog_metaField_id"){
				this.CatCatalogMetafieldId=value;
			} else 
			if(xmlPath=="metafields_metafield_cat_catalo_cat_catalog_id"){
				this.metafields_metafield_cat_catalo_cat_catalog_id_fk=value;
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
		if (xmlPath=="metaField"){
			return "field_data";
		}else if (xmlPath=="name"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<cat:catalog_metaField";
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
		xmlTxt+="\n</cat:catalog_metaField>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.CatCatalogMetafieldId!=null){
				if(hiddenCount++>0)str+=",";
				str+="cat_catalog_metaField_id=\"" + this.CatCatalogMetafieldId + "\"";
			}
			if(this.metafields_metafield_cat_catalo_cat_catalog_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="metafields_metafield_cat_catalo_cat_catalog_id=\"" + this.metafields_metafield_cat_catalo_cat_catalog_id_fk + "\"";
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
		if (this.Metafield!=null){
			xmlTxt+=this.Metafield.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.CatCatalogMetafieldId!=null) return true;
			if (this.metafields_metafield_cat_catalo_cat_catalog_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Metafield!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
