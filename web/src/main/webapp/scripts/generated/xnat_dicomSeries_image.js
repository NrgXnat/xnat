/*
 * web: xnat_dicomSeries_image.js
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

function xnat_dicomSeries_image(){
this.xsiType="xnat:dicomSeries_image";

	this.getSchemaElementName=function(){
		return "dicomSeries_image";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:dicomSeries_image";
	}

	this.Uri=null;


	function getUri() {
		return this.Uri;
	}
	this.getUri=getUri;


	function setUri(v){
		this.Uri=v;
	}
	this.setUri=setUri;

	this.SopInstanceUid=null;


	function getSopInstanceUid() {
		return this.SopInstanceUid;
	}
	this.getSopInstanceUid=getSopInstanceUid;


	function setSopInstanceUid(v){
		this.SopInstanceUid=v;
	}
	this.setSopInstanceUid=setSopInstanceUid;

	this.InstanceNumber=null;


	function getInstanceNumber() {
		return this.InstanceNumber;
	}
	this.getInstanceNumber=getInstanceNumber;


	function setInstanceNumber(v){
		this.InstanceNumber=v;
	}
	this.setInstanceNumber=setInstanceNumber;

	this.XnatDicomseriesImageId=null;


	function getXnatDicomseriesImageId() {
		return this.XnatDicomseriesImageId;
	}
	this.getXnatDicomseriesImageId=getXnatDicomseriesImageId;


	function setXnatDicomseriesImageId(v){
		this.XnatDicomseriesImageId=v;
	}
	this.setXnatDicomseriesImageId=setXnatDicomseriesImageId;

	this.imageset_image_xnat_dicomSeries_xnat_abstractresource_id_fk=null;


	this.getimageset_image_xnat_dicomSeries_xnat_abstractresource_id=function() {
		return this.imageset_image_xnat_dicomSeries_xnat_abstractresource_id_fk;
	}


	this.setimageset_image_xnat_dicomSeries_xnat_abstractresource_id=function(v){
		this.imageset_image_xnat_dicomSeries_xnat_abstractresource_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="URI"){
				return this.Uri ;
			} else 
			if(xmlPath=="sop_instance_UID"){
				return this.SopInstanceUid ;
			} else 
			if(xmlPath=="instance_number"){
				return this.InstanceNumber ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_dicomSeries_image_id"){
				return this.XnatDicomseriesImageId ;
			} else 
			if(xmlPath=="imageset_image_xnat_dicomSeries_xnat_abstractresource_id"){
				return this.imageset_image_xnat_dicomSeries_xnat_abstractresource_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="URI"){
				this.Uri=value;
			} else 
			if(xmlPath=="sop_instance_UID"){
				this.SopInstanceUid=value;
			} else 
			if(xmlPath=="instance_number"){
				this.InstanceNumber=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_dicomSeries_image_id"){
				this.XnatDicomseriesImageId=value;
			} else 
			if(xmlPath=="imageset_image_xnat_dicomSeries_xnat_abstractresource_id"){
				this.imageset_image_xnat_dicomSeries_xnat_abstractresource_id_fk=value;
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
		if (xmlPath=="URI"){
			return "field_data";
		}else if (xmlPath=="sop_instance_UID"){
			return "field_data";
		}else if (xmlPath=="instance_number"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:dicomSeries_image";
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
		xmlTxt+="\n</xnat:dicomSeries_image>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatDicomseriesImageId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_dicomSeries_image_id=\"" + this.XnatDicomseriesImageId + "\"";
			}
			if(this.imageset_image_xnat_dicomSeries_xnat_abstractresource_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="imageset_image_xnat_dicomSeries_xnat_abstractresource_id=\"" + this.imageset_image_xnat_dicomSeries_xnat_abstractresource_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Uri!=null)
			attTxt+=" URI=\"" +this.Uri +"\"";
		else attTxt+=" URI=\"\"";//REQUIRED FIELD

		if (this.SopInstanceUid!=null)
			attTxt+=" sop_instance_UID=\"" +this.SopInstanceUid +"\"";
		else attTxt+=" sop_instance_UID=\"\"";//REQUIRED FIELD

		if (this.InstanceNumber!=null)
			attTxt+=" instance_number=\"" +this.InstanceNumber +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatDicomseriesImageId!=null) return true;
			if (this.imageset_image_xnat_dicomSeries_xnat_abstractresource_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		return false;
	}
}
