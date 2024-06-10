/*
 * web: xnat_derivedData.js
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

function xnat_derivedData(){
this.xsiType="xnat:derivedData";

	this.getSchemaElementName=function(){
		return "derivedData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:derivedData";
	}
this.extension=dynamicJSLoad('xnat_experimentData','generated/xnat_experimentData.js');
	this.Provenance =null;
	function getProvenance() {
		return this.Provenance;
	}
	this.getProvenance=getProvenance;


	function setProvenance(v){
		this.Provenance =v;
	}
	this.setProvenance=setProvenance;

	this.Provenance_ProvenanceProvProcessId=null;


	function getProvenance_ProvenanceProvProcessId(){
		return this.Provenance_ProvenanceProvProcessId;
	}
	this.getProvenance_ProvenanceProvProcessId=getProvenance_ProvenanceProvProcessId;


	function setProvenance_ProvenanceProvProcessId(v){
		this.Provenance_ProvenanceProvProcessId=v;
	}
	this.setProvenance_ProvenanceProvProcessId=setProvenance_ProvenanceProvProcessId;


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
			if(xmlPath=="provenance"){
				return this.Provenance ;
			} else 
			if(xmlPath.startsWith("provenance")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Provenance ;
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
				if(this.Provenance!=undefined)return this.Provenance.getProperty(xmlPath);
				else return null;
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
			if(xmlPath=="provenance"){
				this.Provenance=value;
			} else 
			if(xmlPath.startsWith("provenance")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Provenance ;
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
				if(this.Provenance!=undefined){
					this.Provenance.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Provenance= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Provenance= instanciateObject("prov:process");//omUtils.js
						}
						if(options && options.where)this.Provenance.setProperty(options.where.field,options.where.value);
						this.Provenance.setProperty(xmlPath,value);
				}
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
		if (xmlPath=="provenance"){
			this.setProvenance(v);
		}
		else{
			this.extension.setReferenceField(xmlPath,v);
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="provenance"){
			return "http://www.nbirn.net/prov:process";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="provenance"){
			return "field_single_reference";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:derivedData";
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
		xmlTxt+="\n</xnat:derivedData>";
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
		if (this.Provenance!=null){
			xmlTxt+="\n<xnat:provenance";
			xmlTxt+=this.Provenance.getXMLAtts();
			if(this.Provenance.xsiType!="prov:process"){
				xmlTxt+=" xsi:type=\"" + this.Provenance.xsiType + "\"";
			}
			if (this.Provenance.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Provenance.getXMLBody(preventComments);
				xmlTxt+="</xnat:provenance>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Provenance!=null){
			if (this.Provenance.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
