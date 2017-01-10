/*
 * web: xnat_ctScanData_focalSpot.js
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

function xnat_ctScanData_focalSpot(){
this.xsiType="xnat:ctScanData_focalSpot";

	this.getSchemaElementName=function(){
		return "ctScanData_focalSpot";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:ctScanData_focalSpot";
	}

	this.Focalspot=null;


	function getFocalspot() {
		return this.Focalspot;
	}
	this.getFocalspot=getFocalspot;


	function setFocalspot(v){
		this.Focalspot=v;
	}
	this.setFocalspot=setFocalspot;

	this.XnatCtscandataFocalspotId=null;


	function getXnatCtscandataFocalspotId() {
		return this.XnatCtscandataFocalspotId;
	}
	this.getXnatCtscandataFocalspotId=getXnatCtscandataFocalspotId;


	function setXnatCtscandataFocalspotId(v){
		this.XnatCtscandataFocalspotId=v;
	}
	this.setXnatCtscandataFocalspotId=setXnatCtscandataFocalspotId;

	this.parameters_focalspots_focalspot_xnat_imagescandata_id_fk=null;


	this.getparameters_focalspots_focalspot_xnat_imagescandata_id=function() {
		return this.parameters_focalspots_focalspot_xnat_imagescandata_id_fk;
	}


	this.setparameters_focalspots_focalspot_xnat_imagescandata_id=function(v){
		this.parameters_focalspots_focalspot_xnat_imagescandata_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="focalSpot"){
				return this.Focalspot ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_ctScanData_focalSpot_id"){
				return this.XnatCtscandataFocalspotId ;
			} else 
			if(xmlPath=="parameters_focalspots_focalspot_xnat_imagescandata_id"){
				return this.parameters_focalspots_focalspot_xnat_imagescandata_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="focalSpot"){
				this.Focalspot=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_ctScanData_focalSpot_id"){
				this.XnatCtscandataFocalspotId=value;
			} else 
			if(xmlPath=="parameters_focalspots_focalspot_xnat_imagescandata_id"){
				this.parameters_focalspots_focalspot_xnat_imagescandata_id_fk=value;
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
		if (xmlPath=="focalSpot"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:ctScanData_focalSpot";
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
		xmlTxt+="\n</xnat:ctScanData_focalSpot>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatCtscandataFocalspotId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_ctScanData_focalSpot_id=\"" + this.XnatCtscandataFocalspotId + "\"";
			}
			if(this.parameters_focalspots_focalspot_xnat_imagescandata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="parameters_focalspots_focalspot_xnat_imagescandata_id=\"" + this.parameters_focalspots_focalspot_xnat_imagescandata_id_fk + "\"";
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
		if (this.Focalspot!=null){
			xmlTxt+="\n<xnat:focalSpot";
			xmlTxt+=">";
			xmlTxt+=this.Focalspot;
			xmlTxt+="</xnat:focalSpot>";
		}
		else{
			xmlTxt+="\n<xnat:focalSpot";
			xmlTxt+="/>";
		}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatCtscandataFocalspotId!=null) return true;
			if (this.parameters_focalspots_focalspot_xnat_imagescandata_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Focalspot!=null) return true;
		return true;//REQUIRED focalSpot
	}
}
