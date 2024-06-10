/*
 * web: xnat_computationData.js
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

function xnat_computationData(){
this.xsiType="xnat:computationData";

	this.getSchemaElementName=function(){
		return "computationData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:computationData";
	}

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.Value=null;


	function getValue() {
		return this.Value;
	}
	this.getValue=getValue;


	function setValue(v){
		this.Value=v;
	}
	this.setValue=setValue;

	this.Source=null;


	function getSource() {
		return this.Source;
	}
	this.getSource=getSource;


	function setSource(v){
		this.Source=v;
	}
	this.setSource=setSource;

	this.Units=null;


	function getUnits() {
		return this.Units;
	}
	this.getUnits=getUnits;


	function setUnits(v){
		this.Units=v;
	}
	this.setUnits=setUnits;

	this.XnatComputationdataId=null;


	function getXnatComputationdataId() {
		return this.XnatComputationdataId;
	}
	this.getXnatComputationdataId=getXnatComputationdataId;


	function setXnatComputationdataId(v){
		this.XnatComputationdataId=v;
	}
	this.setXnatComputationdataId=setXnatComputationdataId;

	this.computations_datum_xnat_reconst_xnat_reconstructedimagedata_id_fk=null;


	this.getcomputations_datum_xnat_reconst_xnat_reconstructedimagedata_id=function() {
		return this.computations_datum_xnat_reconst_xnat_reconstructedimagedata_id_fk;
	}


	this.setcomputations_datum_xnat_reconst_xnat_reconstructedimagedata_id=function(v){
		this.computations_datum_xnat_reconst_xnat_reconstructedimagedata_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="value"){
				return this.Value ;
			} else 
			if(xmlPath=="source"){
				return this.Source ;
			} else 
			if(xmlPath=="units"){
				return this.Units ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_computationData_id"){
				return this.XnatComputationdataId ;
			} else 
			if(xmlPath=="computations_datum_xnat_reconst_xnat_reconstructedimagedata_id"){
				return this.computations_datum_xnat_reconst_xnat_reconstructedimagedata_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="value"){
				this.Value=value;
			} else 
			if(xmlPath=="source"){
				this.Source=value;
			} else 
			if(xmlPath=="units"){
				this.Units=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_computationData_id"){
				this.XnatComputationdataId=value;
			} else 
			if(xmlPath=="computations_datum_xnat_reconst_xnat_reconstructedimagedata_id"){
				this.computations_datum_xnat_reconst_xnat_reconstructedimagedata_id_fk=value;
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
		if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="value"){
			return "field_data";
		}else if (xmlPath=="source"){
			return "field_data";
		}else if (xmlPath=="units"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:computationData";
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
		xmlTxt+="\n</xnat:computationData>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatComputationdataId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_computationData_id=\"" + this.XnatComputationdataId + "\"";
			}
			if(this.computations_datum_xnat_reconst_xnat_reconstructedimagedata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="computations_datum_xnat_reconst_xnat_reconstructedimagedata_id=\"" + this.computations_datum_xnat_reconst_xnat_reconstructedimagedata_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		else attTxt+=" name=\"\"";//REQUIRED FIELD

		if (this.Value!=null)
			attTxt+=" value=\"" +this.Value +"\"";
		else attTxt+=" value=\"\"";//REQUIRED FIELD

		if (this.Source!=null)
			attTxt+=" source=\"" +this.Source +"\"";
		else attTxt+=" source=\"\"";//REQUIRED FIELD

		if (this.Units!=null)
			attTxt+=" units=\"" +this.Units +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatComputationdataId!=null) return true;
			if (this.computations_datum_xnat_reconst_xnat_reconstructedimagedata_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		return false;
	}
}
