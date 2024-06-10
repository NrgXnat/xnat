/*
 * web: xnat_reconstructedImageData_scanID.js
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

function xnat_reconstructedImageData_scanID(){
this.xsiType="xnat:reconstructedImageData_scanID";

	this.getSchemaElementName=function(){
		return "reconstructedImageData_scanID";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:reconstructedImageData_scanID";
	}

	this.Scanid=null;


	function getScanid() {
		return this.Scanid;
	}
	this.getScanid=getScanid;


	function setScanid(v){
		this.Scanid=v;
	}
	this.setScanid=setScanid;

	this.XnatReconstructedimagedataScanidId=null;


	function getXnatReconstructedimagedataScanidId() {
		return this.XnatReconstructedimagedataScanidId;
	}
	this.getXnatReconstructedimagedataScanidId=getXnatReconstructedimagedataScanidId;


	function setXnatReconstructedimagedataScanidId(v){
		this.XnatReconstructedimagedataScanidId=v;
	}
	this.setXnatReconstructedimagedataScanidId=setXnatReconstructedimagedataScanidId;

	this.inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id_fk=null;


	this.getinscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id=function() {
		return this.inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id_fk;
	}


	this.setinscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id=function(v){
		this.inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="scanID"){
				return this.Scanid ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_reconstructedImageData_scanID_id"){
				return this.XnatReconstructedimagedataScanidId ;
			} else 
			if(xmlPath=="inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id"){
				return this.inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="scanID"){
				this.Scanid=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_reconstructedImageData_scanID_id"){
				this.XnatReconstructedimagedataScanidId=value;
			} else 
			if(xmlPath=="inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id"){
				this.inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id_fk=value;
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
		if (xmlPath=="scanID"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:reconstructedImageData_scanID";
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
		xmlTxt+="\n</xnat:reconstructedImageData_scanID>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatReconstructedimagedataScanidId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_reconstructedImageData_scanID_id=\"" + this.XnatReconstructedimagedataScanidId + "\"";
			}
			if(this.inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id=\"" + this.inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id_fk + "\"";
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
		if (this.Scanid!=null){
			xmlTxt+="\n<xnat:scanID";
			xmlTxt+=">";
			xmlTxt+=this.Scanid.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:scanID>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatReconstructedimagedataScanidId!=null) return true;
			if (this.inscans_scanid_xnat_reconstruct_xnat_reconstructedimagedata_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Scanid!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
