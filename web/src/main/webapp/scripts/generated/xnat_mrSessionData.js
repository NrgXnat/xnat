/*
 * web: xnat_mrSessionData.js
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

function xnat_mrSessionData(){
this.xsiType="xnat:mrSessionData";

	this.getSchemaElementName=function(){
		return "mrSessionData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:mrSessionData";
	}
this.extension=dynamicJSLoad('xnat_imageSessionData','generated/xnat_imageSessionData.js');

	this.Coil=null;


	function getCoil() {
		return this.Coil;
	}
	this.getCoil=getCoil;


	function setCoil(v){
		this.Coil=v;
	}
	this.setCoil=setCoil;

	this.Fieldstrength=null;


	function getFieldstrength() {
		return this.Fieldstrength;
	}
	this.getFieldstrength=getFieldstrength;


	function setFieldstrength(v){
		this.Fieldstrength=v;
	}
	this.setFieldstrength=setFieldstrength;

	this.Marker=null;


	function getMarker() {
		return this.Marker;
	}
	this.getMarker=getMarker;


	function setMarker(v){
		this.Marker=v;
	}
	this.setMarker=setMarker;

	this.Stabilization=null;


	function getStabilization() {
		return this.Stabilization;
	}
	this.getStabilization=getStabilization;


	function setStabilization(v){
		this.Stabilization=v;
	}
	this.setStabilization=setStabilization;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageSessionData"){
				return this.Imagesessiondata ;
			} else 
			if(xmlPath.startsWith("imageSessionData")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Imagesessiondata ;
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
				if(this.Imagesessiondata!=undefined)return this.Imagesessiondata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="coil"){
				return this.Coil ;
			} else 
			if(xmlPath=="fieldStrength"){
				return this.Fieldstrength ;
			} else 
			if(xmlPath=="marker"){
				return this.Marker ;
			} else 
			if(xmlPath=="stabilization"){
				return this.Stabilization ;
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
			if(xmlPath=="imageSessionData"){
				this.Imagesessiondata=value;
			} else 
			if(xmlPath.startsWith("imageSessionData")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Imagesessiondata ;
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
				if(this.Imagesessiondata!=undefined){
					this.Imagesessiondata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Imagesessiondata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Imagesessiondata= instanciateObject("xnat:imageSessionData");//omUtils.js
						}
						if(options && options.where)this.Imagesessiondata.setProperty(options.where.field,options.where.value);
						this.Imagesessiondata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="coil"){
				this.Coil=value;
			} else 
			if(xmlPath=="fieldStrength"){
				this.Fieldstrength=value;
			} else 
			if(xmlPath=="marker"){
				this.Marker=value;
			} else 
			if(xmlPath=="stabilization"){
				this.Stabilization=value;
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
		if (xmlPath=="coil"){
			return "field_data";
		}else if (xmlPath=="fieldStrength"){
			return "field_data";
		}else if (xmlPath=="marker"){
			return "field_data";
		}else if (xmlPath=="stabilization"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:MRSession";
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
		xmlTxt+="\n</xnat:MRSession>";
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
		if (this.Coil!=null){
			xmlTxt+="\n<xnat:coil";
			xmlTxt+=">";
			xmlTxt+=this.Coil.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:coil>";
		}
		if (this.Fieldstrength!=null){
			xmlTxt+="\n<xnat:fieldStrength";
			xmlTxt+=">";
			xmlTxt+=this.Fieldstrength.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:fieldStrength>";
		}
		if (this.Marker!=null){
			xmlTxt+="\n<xnat:marker";
			xmlTxt+=">";
			xmlTxt+=this.Marker.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:marker>";
		}
		if (this.Stabilization!=null){
			xmlTxt+="\n<xnat:stabilization";
			xmlTxt+=">";
			xmlTxt+=this.Stabilization.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:stabilization>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Coil!=null) return true;
		if (this.Fieldstrength!=null) return true;
		if (this.Marker!=null) return true;
		if (this.Stabilization!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
