/*
 * web: xnat_subjectAssessorData.js
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

function xnat_subjectAssessorData(){
this.xsiType="xnat:subjectAssessorData";

	this.getSchemaElementName=function(){
		return "subjectAssessorData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:subjectAssessorData";
	}
this.extension=dynamicJSLoad('xnat_experimentData','generated/xnat_experimentData.js');

	this.SubjectId=null;


	function getSubjectId() {
		return this.SubjectId;
	}
	this.getSubjectId=getSubjectId;


	function setSubjectId(v){
		this.SubjectId=v;
	}
	this.setSubjectId=setSubjectId;

	this.Age=null;


	function getAge() {
		return this.Age;
	}
	this.getAge=getAge;


	function setAge(v){
		this.Age=v;
	}
	this.setAge=setAge;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="experimentData"){
				return this.Experimentdata ;
			} else 
			if(xmlPath.startsWith("experimentData")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Experimentdata ;
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
				if(this.Experimentdata!=undefined)return this.Experimentdata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="subject_ID"){
				return this.SubjectId ;
			} else 
			if(xmlPath=="age"){
				return this.Age ;
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
			if(xmlPath=="experimentData"){
				this.Experimentdata=value;
			} else 
			if(xmlPath.startsWith("experimentData")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Experimentdata ;
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
				if(this.Experimentdata!=undefined){
					this.Experimentdata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Experimentdata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Experimentdata= instanciateObject("xnat:experimentData");//omUtils.js
						}
						if(options && options.where)this.Experimentdata.setProperty(options.where.field,options.where.value);
						this.Experimentdata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="subject_ID"){
				this.SubjectId=value;
			} else 
			if(xmlPath=="age"){
				this.Age=value;
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
		if (xmlPath=="subject_ID"){
			return "field_data";
		}else if (xmlPath=="age"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:subjectAssessorData";
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
		xmlTxt+="\n</xnat:subjectAssessorData>";
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
		if (this.SubjectId!=null){
			xmlTxt+="\n<xnat:subject_ID";
			xmlTxt+=">";
			xmlTxt+=this.SubjectId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:subject_ID>";
		}
		if (this.Age!=null){
			xmlTxt+="\n<xnat:age";
			xmlTxt+=">";
			xmlTxt+=this.Age;
			xmlTxt+="</xnat:age>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.SubjectId!=null) return true;
		if (this.Age!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
