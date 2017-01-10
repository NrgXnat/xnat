/*
 * web: xnat_statisticsData_additionalStatistics.js
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

function xnat_statisticsData_additionalStatistics(){
this.xsiType="xnat:statisticsData_additionalStatistics";

	this.getSchemaElementName=function(){
		return "statisticsData_additionalStatistics";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:statisticsData_additionalStatistics";
	}

	this.Additionalstatistics=null;


	function getAdditionalstatistics() {
		return this.Additionalstatistics;
	}
	this.getAdditionalstatistics=getAdditionalstatistics;


	function setAdditionalstatistics(v){
		this.Additionalstatistics=v;
	}
	this.setAdditionalstatistics=setAdditionalstatistics;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.XnatStatisticsdataAdditionalstatisticsId=null;


	function getXnatStatisticsdataAdditionalstatisticsId() {
		return this.XnatStatisticsdataAdditionalstatisticsId;
	}
	this.getXnatStatisticsdataAdditionalstatisticsId=getXnatStatisticsdataAdditionalstatisticsId;


	function setXnatStatisticsdataAdditionalstatisticsId(v){
		this.XnatStatisticsdataAdditionalstatisticsId=v;
	}
	this.setXnatStatisticsdataAdditionalstatisticsId=setXnatStatisticsdataAdditionalstatisticsId;

	this.xnat_statisticsData_xnat_abstractstatistics_id_fk=null;


	this.getxnat_statisticsData_xnat_abstractstatistics_id=function() {
		return this.xnat_statisticsData_xnat_abstractstatistics_id_fk;
	}


	this.setxnat_statisticsData_xnat_abstractstatistics_id=function(v){
		this.xnat_statisticsData_xnat_abstractstatistics_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="additionalStatistics"){
				return this.Additionalstatistics ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_statisticsData_additionalStatistics_id"){
				return this.XnatStatisticsdataAdditionalstatisticsId ;
			} else 
			if(xmlPath=="xnat_statisticsData_xnat_abstractstatistics_id"){
				return this.xnat_statisticsData_xnat_abstractstatistics_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="additionalStatistics"){
				this.Additionalstatistics=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_statisticsData_additionalStatistics_id"){
				this.XnatStatisticsdataAdditionalstatisticsId=value;
			} else 
			if(xmlPath=="xnat_statisticsData_xnat_abstractstatistics_id"){
				this.xnat_statisticsData_xnat_abstractstatistics_id_fk=value;
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
		if (xmlPath=="additionalStatistics"){
			return "field_data";
		}else if (xmlPath=="name"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:statisticsData_additionalStatistics";
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
		xmlTxt+="\n</xnat:statisticsData_additionalStatistics>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatStatisticsdataAdditionalstatisticsId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_statisticsData_additionalStatistics_id=\"" + this.XnatStatisticsdataAdditionalstatisticsId + "\"";
			}
			if(this.xnat_statisticsData_xnat_abstractstatistics_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_statisticsData_xnat_abstractstatistics_id=\"" + this.xnat_statisticsData_xnat_abstractstatistics_id_fk + "\"";
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
		if (this.Additionalstatistics!=null){
			xmlTxt+=this.Additionalstatistics;
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatStatisticsdataAdditionalstatisticsId!=null) return true;
			if (this.xnat_statisticsData_xnat_abstractstatistics_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Additionalstatistics!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
