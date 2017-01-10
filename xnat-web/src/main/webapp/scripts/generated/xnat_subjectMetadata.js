/*
 * web: xnat_subjectMetadata.js
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

function xnat_subjectMetadata(){
this.xsiType="xnat:subjectMetadata";

	this.getSchemaElementName=function(){
		return "subjectMetadata";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:subjectMetadata";
	}
this.extension=dynamicJSLoad('xnat_abstractSubjectMetadata','generated/xnat_abstractSubjectMetadata.js');

	this.Cohort=null;


	function getCohort() {
		return this.Cohort;
	}
	this.getCohort=getCohort;


	function setCohort(v){
		this.Cohort=v;
	}
	this.setCohort=setCohort;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractSubjectMetadata"){
				return this.Abstractsubjectmetadata ;
			} else 
			if(xmlPath.startsWith("abstractSubjectMetadata")){
				xmlPath=xmlPath.substring(23);
				if(xmlPath=="")return this.Abstractsubjectmetadata ;
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
				if(this.Abstractsubjectmetadata!=undefined)return this.Abstractsubjectmetadata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="cohort"){
				return this.Cohort ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			{
				return this.extension.getProperty(xmlPath);
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractSubjectMetadata"){
				this.Abstractsubjectmetadata=value;
			} else 
			if(xmlPath.startsWith("abstractSubjectMetadata")){
				xmlPath=xmlPath.substring(23);
				if(xmlPath=="")return this.Abstractsubjectmetadata ;
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
				if(this.Abstractsubjectmetadata!=undefined){
					this.Abstractsubjectmetadata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Abstractsubjectmetadata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Abstractsubjectmetadata= instanciateObject("xnat:abstractSubjectMetadata");//omUtils.js
						}
						if(options && options.where)this.Abstractsubjectmetadata.setProperty(options.where.field,options.where.value);
						this.Abstractsubjectmetadata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="cohort"){
				this.Cohort=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			{
				return this.extension.setProperty(xmlPath,value);
			}
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.setReferenceField=function(xmlPath,v) {
			this.extension.setReferenceField(xmlPath,v);
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
			return this.extension.getReferenceFieldName(xmlPath);
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="cohort"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:subjectMetadata";
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
		xmlTxt+="\n</xnat:subjectMetadata>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = this.extension.getXMLAtts();
		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		xmlTxt+=this.extension.getXMLBody(preventComments);
		if (this.Cohort!=null){
			xmlTxt+="\n<xnat:cohort";
			xmlTxt+=">";
			xmlTxt+=this.Cohort.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:cohort>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Cohort!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
