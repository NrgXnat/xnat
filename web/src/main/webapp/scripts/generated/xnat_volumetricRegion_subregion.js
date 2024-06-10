/*
 * web: xnat_volumetricRegion_subregion.js
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

function xnat_volumetricRegion_subregion(){
this.xsiType="xnat:volumetricRegion_subregion";

	this.getSchemaElementName=function(){
		return "volumetricRegion_subregion";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:volumetricRegion_subregion";
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

	this.Voxels=null;


	function getVoxels() {
		return this.Voxels;
	}
	this.getVoxels=getVoxels;


	function setVoxels(v){
		this.Voxels=v;
	}
	this.setVoxels=setVoxels;

	this.XnatVolumetricregionSubregionId=null;


	function getXnatVolumetricregionSubregionId() {
		return this.XnatVolumetricregionSubregionId;
	}
	this.getXnatVolumetricregionSubregionId=getXnatVolumetricregionSubregionId;


	function setXnatVolumetricregionSubregionId(v){
		this.XnatVolumetricregionSubregionId=v;
	}
	this.setXnatVolumetricregionSubregionId=setXnatVolumetricregionSubregionId;

	this.subregions_subregion_xnat_volum_xnat_volumetricregion_id_fk=null;


	this.getsubregions_subregion_xnat_volum_xnat_volumetricregion_id=function() {
		return this.subregions_subregion_xnat_volum_xnat_volumetricregion_id_fk;
	}


	this.setsubregions_subregion_xnat_volum_xnat_volumetricregion_id=function(v){
		this.subregions_subregion_xnat_volum_xnat_volumetricregion_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="voxels"){
				return this.Voxels ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_volumetricRegion_subregion_id"){
				return this.XnatVolumetricregionSubregionId ;
			} else 
			if(xmlPath=="subregions_subregion_xnat_volum_xnat_volumetricregion_id"){
				return this.subregions_subregion_xnat_volum_xnat_volumetricregion_id_fk ;
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
			if(xmlPath=="voxels"){
				this.Voxels=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_volumetricRegion_subregion_id"){
				this.XnatVolumetricregionSubregionId=value;
			} else 
			if(xmlPath=="subregions_subregion_xnat_volum_xnat_volumetricregion_id"){
				this.subregions_subregion_xnat_volum_xnat_volumetricregion_id_fk=value;
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
		}else if (xmlPath=="voxels"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:volumetricRegion_subregion";
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
		xmlTxt+="\n</xnat:volumetricRegion_subregion>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatVolumetricregionSubregionId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_volumetricRegion_subregion_id=\"" + this.XnatVolumetricregionSubregionId + "\"";
			}
			if(this.subregions_subregion_xnat_volum_xnat_volumetricregion_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="subregions_subregion_xnat_volum_xnat_volumetricregion_id=\"" + this.subregions_subregion_xnat_volum_xnat_volumetricregion_id_fk + "\"";
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

		if (this.Voxels!=null)
			attTxt+=" voxels=\"" +this.Voxels +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatVolumetricregionSubregionId!=null) return true;
			if (this.subregions_subregion_xnat_volum_xnat_volumetricregion_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		return false;
	}
}
