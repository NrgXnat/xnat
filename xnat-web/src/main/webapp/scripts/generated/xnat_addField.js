/*
 * web: xnat_addField.js
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

function xnat_addField(){
this.xsiType="xnat:addField";

	this.getSchemaElementName=function(){
		return "addField";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:addField";
	}

	this.Addfield=null;


	function getAddfield() {
		return this.Addfield;
	}
	this.getAddfield=getAddfield;


	function setAddfield(v){
		this.Addfield=v;
	}
	this.setAddfield=setAddfield;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.XnatAddfieldId=null;


	function getXnatAddfieldId() {
		return this.XnatAddfieldId;
	}
	this.getXnatAddfieldId=getXnatAddfieldId;


	function setXnatAddfieldId(v){
		this.XnatAddfieldId=v;
	}
	this.setXnatAddfieldId=setXnatAddfieldId;

	this.parameters_addparam_xnat_mrScan_xnat_imagescandata_id_fk=null;


	this.getparameters_addparam_xnat_mrScan_xnat_imagescandata_id=function() {
		return this.parameters_addparam_xnat_mrScan_xnat_imagescandata_id_fk;
	}


	this.setparameters_addparam_xnat_mrScan_xnat_imagescandata_id=function(v){
		this.parameters_addparam_xnat_mrScan_xnat_imagescandata_id_fk=v;
	}

	this.parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id_fk=null;


	this.getparameters_addparam_xnat_recons_xnat_reconstructedimagedata_id=function() {
		return this.parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id_fk;
	}


	this.setparameters_addparam_xnat_recons_xnat_reconstructedimagedata_id=function(v){
		this.parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id_fk=v;
	}

	this.parameters_addparam_xnat_petSca_xnat_imagescandata_id_fk=null;


	this.getparameters_addparam_xnat_petSca_xnat_imagescandata_id=function() {
		return this.parameters_addparam_xnat_petSca_xnat_imagescandata_id_fk;
	}


	this.setparameters_addparam_xnat_petSca_xnat_imagescandata_id=function(v){
		this.parameters_addparam_xnat_petSca_xnat_imagescandata_id_fk=v;
	}

	this.parameters_addparam_xnat_imageA_id_fk=null;


	this.getparameters_addparam_xnat_imageA_id=function() {
		return this.parameters_addparam_xnat_imageA_id_fk;
	}


	this.setparameters_addparam_xnat_imageA_id=function(v){
		this.parameters_addparam_xnat_imageA_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="addField"){
				return this.Addfield ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_addField_id"){
				return this.XnatAddfieldId ;
			} else 
			if(xmlPath=="parameters_addparam_xnat_mrScan_xnat_imagescandata_id"){
				return this.parameters_addparam_xnat_mrScan_xnat_imagescandata_id_fk ;
			} else 
			if(xmlPath=="parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id"){
				return this.parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id_fk ;
			} else 
			if(xmlPath=="parameters_addparam_xnat_petSca_xnat_imagescandata_id"){
				return this.parameters_addparam_xnat_petSca_xnat_imagescandata_id_fk ;
			} else 
			if(xmlPath=="parameters_addparam_xnat_imageA_id"){
				return this.parameters_addparam_xnat_imageA_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="addField"){
				this.Addfield=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_addField_id"){
				this.XnatAddfieldId=value;
			} else 
			if(xmlPath=="parameters_addparam_xnat_mrScan_xnat_imagescandata_id"){
				this.parameters_addparam_xnat_mrScan_xnat_imagescandata_id_fk=value;
			} else 
			if(xmlPath=="parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id"){
				this.parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id_fk=value;
			} else 
			if(xmlPath=="parameters_addparam_xnat_petSca_xnat_imagescandata_id"){
				this.parameters_addparam_xnat_petSca_xnat_imagescandata_id_fk=value;
			} else 
			if(xmlPath=="parameters_addparam_xnat_imageA_id"){
				this.parameters_addparam_xnat_imageA_id_fk=value;
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
		if (xmlPath=="addField"){
			return "field_data";
		}else if (xmlPath=="name"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:addField";
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
		xmlTxt+="\n</xnat:addField>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatAddfieldId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_addField_id=\"" + this.XnatAddfieldId + "\"";
			}
			if(this.parameters_addparam_xnat_mrScan_xnat_imagescandata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="parameters_addparam_xnat_mrScan_xnat_imagescandata_id=\"" + this.parameters_addparam_xnat_mrScan_xnat_imagescandata_id_fk + "\"";
			}
			if(this.parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id=\"" + this.parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id_fk + "\"";
			}
			if(this.parameters_addparam_xnat_petSca_xnat_imagescandata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="parameters_addparam_xnat_petSca_xnat_imagescandata_id=\"" + this.parameters_addparam_xnat_petSca_xnat_imagescandata_id_fk + "\"";
			}
			if(this.parameters_addparam_xnat_imageA_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="parameters_addparam_xnat_imageA_id=\"" + this.parameters_addparam_xnat_imageA_id_fk + "\"";
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
		if (this.Addfield!=null){
			xmlTxt+=this.Addfield.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatAddfieldId!=null) return true;
			if (this.parameters_addparam_xnat_mrScan_xnat_imagescandata_id_fk!=null) return true;
			if (this.parameters_addparam_xnat_recons_xnat_reconstructedimagedata_id_fk!=null) return true;
			if (this.parameters_addparam_xnat_petSca_xnat_imagescandata_id_fk!=null) return true;
			if (this.parameters_addparam_xnat_imageA_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Addfield!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
