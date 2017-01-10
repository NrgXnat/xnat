/*
 * web: xnat_mrQcScanData.js
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

function xnat_mrQcScanData(){
this.xsiType="xnat:mrQcScanData";

	this.getSchemaElementName=function(){
		return "mrQcScanData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:mrQcScanData";
	}
this.extension=dynamicJSLoad('xnat_qcScanData','generated/xnat_qcScanData.js');

	this.Blurring=null;


	function getBlurring() {
		return this.Blurring;
	}
	this.getBlurring=getBlurring;


	function setBlurring(v){
		this.Blurring=v;
	}
	this.setBlurring=setBlurring;

	this.Flow=null;


	function getFlow() {
		return this.Flow;
	}
	this.getFlow=getFlow;


	function setFlow(v){
		this.Flow=v;
	}
	this.setFlow=setFlow;

	this.Imagecontrast=null;


	function getImagecontrast() {
		return this.Imagecontrast;
	}
	this.getImagecontrast=getImagecontrast;


	function setImagecontrast(v){
		this.Imagecontrast=v;
	}
	this.setImagecontrast=setImagecontrast;

	this.Inhomogeneity=null;


	function getInhomogeneity() {
		return this.Inhomogeneity;
	}
	this.getInhomogeneity=getInhomogeneity;


	function setInhomogeneity(v){
		this.Inhomogeneity=v;
	}
	this.setInhomogeneity=setInhomogeneity;

	this.Wrap=null;


	function getWrap() {
		return this.Wrap;
	}
	this.getWrap=getWrap;


	function setWrap(v){
		this.Wrap=v;
	}
	this.setWrap=setWrap;

	this.Susceptibility=null;


	function getSusceptibility() {
		return this.Susceptibility;
	}
	this.getSusceptibility=getSusceptibility;


	function setSusceptibility(v){
		this.Susceptibility=v;
	}
	this.setSusceptibility=setSusceptibility;

	this.Interpacmotion=null;


	function getInterpacmotion() {
		return this.Interpacmotion;
	}
	this.getInterpacmotion=getInterpacmotion;


	function setInterpacmotion(v){
		this.Interpacmotion=v;
	}
	this.setInterpacmotion=setInterpacmotion;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="qcScanData"){
				return this.Qcscandata ;
			} else 
			if(xmlPath.startsWith("qcScanData")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Qcscandata ;
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
				if(this.Qcscandata!=undefined)return this.Qcscandata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="blurring"){
				return this.Blurring ;
			} else 
			if(xmlPath=="flow"){
				return this.Flow ;
			} else 
			if(xmlPath=="imageContrast"){
				return this.Imagecontrast ;
			} else 
			if(xmlPath=="inhomogeneity"){
				return this.Inhomogeneity ;
			} else 
			if(xmlPath=="wrap"){
				return this.Wrap ;
			} else 
			if(xmlPath=="susceptibility"){
				return this.Susceptibility ;
			} else 
			if(xmlPath=="interpacMotion"){
				return this.Interpacmotion ;
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
			if(xmlPath=="qcScanData"){
				this.Qcscandata=value;
			} else 
			if(xmlPath.startsWith("qcScanData")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Qcscandata ;
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
				if(this.Qcscandata!=undefined){
					this.Qcscandata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Qcscandata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Qcscandata= instanciateObject("xnat:qcScanData");//omUtils.js
						}
						if(options && options.where)this.Qcscandata.setProperty(options.where.field,options.where.value);
						this.Qcscandata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="blurring"){
				this.Blurring=value;
			} else 
			if(xmlPath=="flow"){
				this.Flow=value;
			} else 
			if(xmlPath=="imageContrast"){
				this.Imagecontrast=value;
			} else 
			if(xmlPath=="inhomogeneity"){
				this.Inhomogeneity=value;
			} else 
			if(xmlPath=="wrap"){
				this.Wrap=value;
			} else 
			if(xmlPath=="susceptibility"){
				this.Susceptibility=value;
			} else 
			if(xmlPath=="interpacMotion"){
				this.Interpacmotion=value;
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
		if (xmlPath=="blurring"){
			return "field_data";
		}else if (xmlPath=="flow"){
			return "field_data";
		}else if (xmlPath=="imageContrast"){
			return "field_data";
		}else if (xmlPath=="inhomogeneity"){
			return "field_data";
		}else if (xmlPath=="wrap"){
			return "field_data";
		}else if (xmlPath=="susceptibility"){
			return "field_data";
		}else if (xmlPath=="interpacMotion"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:mrQcScanData";
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
		xmlTxt+="\n</xnat:mrQcScanData>";
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
		if (this.Blurring!=null){
			xmlTxt+="\n<xnat:blurring";
			xmlTxt+=">";
			xmlTxt+=this.Blurring.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:blurring>";
		}
		if (this.Flow!=null){
			xmlTxt+="\n<xnat:flow";
			xmlTxt+=">";
			xmlTxt+=this.Flow.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:flow>";
		}
		if (this.Imagecontrast!=null){
			xmlTxt+="\n<xnat:imageContrast";
			xmlTxt+=">";
			xmlTxt+=this.Imagecontrast.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:imageContrast>";
		}
		if (this.Inhomogeneity!=null){
			xmlTxt+="\n<xnat:inhomogeneity";
			xmlTxt+=">";
			xmlTxt+=this.Inhomogeneity.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:inhomogeneity>";
		}
		if (this.Wrap!=null){
			xmlTxt+="\n<xnat:wrap";
			xmlTxt+=">";
			xmlTxt+=this.Wrap.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:wrap>";
		}
		if (this.Susceptibility!=null){
			xmlTxt+="\n<xnat:susceptibility";
			xmlTxt+=">";
			xmlTxt+=this.Susceptibility.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:susceptibility>";
		}
		if (this.Interpacmotion!=null){
			xmlTxt+="\n<xnat:interpacMotion";
			xmlTxt+=">";
			xmlTxt+=this.Interpacmotion.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:interpacMotion>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Blurring!=null) return true;
		if (this.Flow!=null) return true;
		if (this.Imagecontrast!=null) return true;
		if (this.Inhomogeneity!=null) return true;
		if (this.Wrap!=null) return true;
		if (this.Susceptibility!=null) return true;
		if (this.Interpacmotion!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
