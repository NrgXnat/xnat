/*
 * web: xnat_qcAssessmentData_scan_slice.js
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

function xnat_qcAssessmentData_scan_slice(){
this.xsiType="xnat:qcAssessmentData_scan_slice";

	this.getSchemaElementName=function(){
		return "qcAssessmentData_scan_slice";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:qcAssessmentData_scan_slice";
	}
	this.Slicestatistics =null;
	function getSlicestatistics() {
		return this.Slicestatistics;
	}
	this.getSlicestatistics=getSlicestatistics;


	function setSlicestatistics(v){
		this.Slicestatistics =v;
	}
	this.setSlicestatistics=setSlicestatistics;

	this.Slicestatistics_SlicestatisticsXnatAbstractstatisticsId=null;


	function getSlicestatistics_SlicestatisticsXnatAbstractstatisticsId(){
		return this.Slicestatistics_SlicestatisticsXnatAbstractstatisticsId;
	}
	this.getSlicestatistics_SlicestatisticsXnatAbstractstatisticsId=getSlicestatistics_SlicestatisticsXnatAbstractstatisticsId;


	function setSlicestatistics_SlicestatisticsXnatAbstractstatisticsId(v){
		this.Slicestatistics_SlicestatisticsXnatAbstractstatisticsId=v;
	}
	this.setSlicestatistics_SlicestatisticsXnatAbstractstatisticsId=setSlicestatistics_SlicestatisticsXnatAbstractstatisticsId;

	this.Number=null;


	function getNumber() {
		return this.Number;
	}
	this.getNumber=getNumber;


	function setNumber(v){
		this.Number=v;
	}
	this.setNumber=setNumber;

	this.XnatQcassessmentdataScanSliceId=null;


	function getXnatQcassessmentdataScanSliceId() {
		return this.XnatQcassessmentdataScanSliceId;
	}
	this.getXnatQcassessmentdataScanSliceId=getXnatQcassessmentdataScanSliceId;


	function setXnatQcassessmentdataScanSliceId(v){
		this.XnatQcassessmentdataScanSliceId=v;
	}
	this.setXnatQcassessmentdataScanSliceId=setXnatQcassessmentdataScanSliceId;

	this.sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id_fk=null;


	this.getsliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id=function() {
		return this.sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id_fk;
	}


	this.setsliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id=function(v){
		this.sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="sliceStatistics"){
				return this.Slicestatistics ;
			} else 
			if(xmlPath.startsWith("sliceStatistics")){
				xmlPath=xmlPath.substring(15);
				if(xmlPath=="")return this.Slicestatistics ;
				if(xmlPath.startsWith("[")){
					if (xmlPath.indexOf("/")>-1){
						var optionString=xmlPath.substring(0,xmlPath.indexOf("/"));
						xmlPath=xmlPath.substring(xmlPath.indexOf("/")+1);
					}else{
						var optionString=xmlPath;
						xmlPath="";
					}
					
					var options = loadOptions(optionString);//omUtils.js
				}else{xmlPath=xmlPath.substring(1);}
				if(this.Slicestatistics!=undefined)return this.Slicestatistics.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="number"){
				return this.Number ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_qcAssessmentData_scan_slice_id"){
				return this.XnatQcassessmentdataScanSliceId ;
			} else 
			if(xmlPath=="sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id"){
				return this.sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="sliceStatistics"){
				this.Slicestatistics=value;
			} else 
			if(xmlPath.startsWith("sliceStatistics")){
				xmlPath=xmlPath.substring(15);
				if(xmlPath=="")return this.Slicestatistics ;
				if(xmlPath.startsWith("[")){
					if (xmlPath.indexOf("/")>-1){
						var optionString=xmlPath.substring(0,xmlPath.indexOf("/"));
						xmlPath=xmlPath.substring(xmlPath.indexOf("/")+1);
					}else{
						var optionString=xmlPath;
						xmlPath="";
					}
					
					var options = loadOptions(optionString);//omUtils.js
				}else{xmlPath=xmlPath.substring(1);}
				if(this.Slicestatistics!=undefined){
					this.Slicestatistics.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Slicestatistics= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Slicestatistics= instanciateObject("xnat:abstractStatistics");//omUtils.js
						}
						if(options && options.where)this.Slicestatistics.setProperty(options.where.field,options.where.value);
						this.Slicestatistics.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="number"){
				this.Number=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_qcAssessmentData_scan_slice_id"){
				this.XnatQcassessmentdataScanSliceId=value;
			} else 
			if(xmlPath=="sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id"){
				this.sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id_fk=value;
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
		if (xmlPath=="sliceStatistics"){
			this.setSlicestatistics(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="sliceStatistics"){
			return "http://nrg.wustl.edu/xnat:abstractStatistics";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="sliceStatistics"){
			return "field_single_reference";
		}else if (xmlPath=="number"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:qcAssessmentData_scan_slice";
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
		xmlTxt+="\n</xnat:qcAssessmentData_scan_slice>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatQcassessmentdataScanSliceId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_qcAssessmentData_scan_slice_id=\"" + this.XnatQcassessmentdataScanSliceId + "\"";
			}
			if(this.sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id=\"" + this.sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Number!=null)
			attTxt+=" number=\"" +this.Number +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Slicestatistics!=null){
			xmlTxt+="\n<xnat:sliceStatistics";
			xmlTxt+=this.Slicestatistics.getXMLAtts();
			if(this.Slicestatistics.xsiType!="xnat:abstractStatistics"){
				xmlTxt+=" xsi:type=\"" + this.Slicestatistics.xsiType + "\"";
			}
			if (this.Slicestatistics.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Slicestatistics.getXMLBody(preventComments);
				xmlTxt+="</xnat:sliceStatistics>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatQcassessmentdataScanSliceId!=null) return true;
			if (this.sliceqc_slice_xnat_qcAssessment_xnat_qcassessmentdata_scan_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Slicestatistics!=null){
			if (this.Slicestatistics.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if(this.hasXMLComments())return true;
		return false;
	}
}
