/*
 * web: xnat_petScanData_frame.js
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

function xnat_petScanData_frame(){
this.xsiType="xnat:petScanData_frame";

	this.getSchemaElementName=function(){
		return "petScanData_frame";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:petScanData_frame";
	}

	this.Number=null;


	function getNumber() {
		return this.Number;
	}
	this.getNumber=getNumber;


	function setNumber(v){
		this.Number=v;
	}
	this.setNumber=setNumber;

	this.Starttime=null;


	function getStarttime() {
		return this.Starttime;
	}
	this.getStarttime=getStarttime;


	function setStarttime(v){
		this.Starttime=v;
	}
	this.setStarttime=setStarttime;

	this.Length=null;


	function getLength() {
		return this.Length;
	}
	this.getLength=getLength;


	function setLength(v){
		this.Length=v;
	}
	this.setLength=setLength;

	this.Units=null;


	function getUnits() {
		return this.Units;
	}
	this.getUnits=getUnits;


	function setUnits(v){
		this.Units=v;
	}
	this.setUnits=setUnits;

	this.XnatPetscandataFrameId=null;


	function getXnatPetscandataFrameId() {
		return this.XnatPetscandataFrameId;
	}
	this.getXnatPetscandataFrameId=getXnatPetscandataFrameId;


	function setXnatPetscandataFrameId(v){
		this.XnatPetscandataFrameId=v;
	}
	this.setXnatPetscandataFrameId=setXnatPetscandataFrameId;

	this.parameters_frames_frame_xnat_pe_xnat_imagescandata_id_fk=null;


	this.getparameters_frames_frame_xnat_pe_xnat_imagescandata_id=function() {
		return this.parameters_frames_frame_xnat_pe_xnat_imagescandata_id_fk;
	}


	this.setparameters_frames_frame_xnat_pe_xnat_imagescandata_id=function(v){
		this.parameters_frames_frame_xnat_pe_xnat_imagescandata_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="number"){
				return this.Number ;
			} else 
			if(xmlPath=="starttime"){
				return this.Starttime ;
			} else 
			if(xmlPath=="length"){
				return this.Length ;
			} else 
			if(xmlPath=="units"){
				return this.Units ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_petScanData_frame_id"){
				return this.XnatPetscandataFrameId ;
			} else 
			if(xmlPath=="parameters_frames_frame_xnat_pe_xnat_imagescandata_id"){
				return this.parameters_frames_frame_xnat_pe_xnat_imagescandata_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="number"){
				this.Number=value;
			} else 
			if(xmlPath=="starttime"){
				this.Starttime=value;
			} else 
			if(xmlPath=="length"){
				this.Length=value;
			} else 
			if(xmlPath=="units"){
				this.Units=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_petScanData_frame_id"){
				this.XnatPetscandataFrameId=value;
			} else 
			if(xmlPath=="parameters_frames_frame_xnat_pe_xnat_imagescandata_id"){
				this.parameters_frames_frame_xnat_pe_xnat_imagescandata_id_fk=value;
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
		if (xmlPath=="number"){
			return "field_data";
		}else if (xmlPath=="starttime"){
			return "field_data";
		}else if (xmlPath=="length"){
			return "field_data";
		}else if (xmlPath=="units"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:petScanData_frame";
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
		xmlTxt+="\n</xnat:petScanData_frame>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatPetscandataFrameId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_petScanData_frame_id=\"" + this.XnatPetscandataFrameId + "\"";
			}
			if(this.parameters_frames_frame_xnat_pe_xnat_imagescandata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="parameters_frames_frame_xnat_pe_xnat_imagescandata_id=\"" + this.parameters_frames_frame_xnat_pe_xnat_imagescandata_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Number!=null)
			attTxt+=" number=\"" +this.Number +"\"";
		else attTxt+=" number=\"\"";//REQUIRED FIELD

		if (this.Starttime!=null)
			attTxt+=" starttime=\"" +this.Starttime +"\"";
		else attTxt+=" starttime=\"\"";//REQUIRED FIELD

		if (this.Length!=null)
			attTxt+=" length=\"" +this.Length +"\"";
		else attTxt+=" length=\"\"";//REQUIRED FIELD

		if (this.Units!=null)
			attTxt+=" units=\"" +this.Units +"\"";
		else attTxt+=" units=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatPetscandataFrameId!=null) return true;
			if (this.parameters_frames_frame_xnat_pe_xnat_imagescandata_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		return false;
	}
}
