/*
 * web: xnat_resourceSeries.js
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

function xnat_resourceSeries(){
this.xsiType="xnat:resourceSeries";

	this.getSchemaElementName=function(){
		return "resourceSeries";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:resourceSeries";
	}
this.extension=dynamicJSLoad('xnat_abstractResource','generated/xnat_abstractResource.js');

	this.Path=null;


	function getPath() {
		return this.Path;
	}
	this.getPath=getPath;


	function setPath(v){
		this.Path=v;
	}
	this.setPath=setPath;

	this.Pattern=null;


	function getPattern() {
		return this.Pattern;
	}
	this.getPattern=getPattern;


	function setPattern(v){
		this.Pattern=v;
	}
	this.setPattern=setPattern;

	this.Count=null;


	function getCount() {
		return this.Count;
	}
	this.getCount=getCount;


	function setCount(v){
		this.Count=v;
	}
	this.setCount=setCount;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.Format=null;


	function getFormat() {
		return this.Format;
	}
	this.getFormat=getFormat;


	function setFormat(v){
		this.Format=v;
	}
	this.setFormat=setFormat;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;

	this.Content=null;


	function getContent() {
		return this.Content;
	}
	this.getContent=getContent;


	function setContent(v){
		this.Content=v;
	}
	this.setContent=setContent;

	this.Cachepath=null;


	function getCachepath() {
		return this.Cachepath;
	}
	this.getCachepath=getCachepath;


	function setCachepath(v){
		this.Cachepath=v;
	}
	this.setCachepath=setCachepath;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractResource"){
				return this.Abstractresource ;
			} else 
			if(xmlPath.startsWith("abstractResource")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Abstractresource ;
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
				if(this.Abstractresource!=undefined)return this.Abstractresource.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="path"){
				return this.Path ;
			} else 
			if(xmlPath=="pattern"){
				return this.Pattern ;
			} else 
			if(xmlPath=="count"){
				return this.Count ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="format"){
				return this.Format ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="content"){
				return this.Content ;
			} else 
			if(xmlPath=="cachePath"){
				return this.Cachepath ;
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
			if(xmlPath=="abstractResource"){
				this.Abstractresource=value;
			} else 
			if(xmlPath.startsWith("abstractResource")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Abstractresource ;
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
				if(this.Abstractresource!=undefined){
					this.Abstractresource.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Abstractresource= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Abstractresource= instanciateObject("xnat:abstractResource");//omUtils.js
						}
						if(options && options.where)this.Abstractresource.setProperty(options.where.field,options.where.value);
						this.Abstractresource.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="path"){
				this.Path=value;
			} else 
			if(xmlPath=="pattern"){
				this.Pattern=value;
			} else 
			if(xmlPath=="count"){
				this.Count=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="format"){
				this.Format=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="content"){
				this.Content=value;
			} else 
			if(xmlPath=="cachePath"){
				this.Cachepath=value;
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
		if (xmlPath=="path"){
			return "field_data";
		}else if (xmlPath=="pattern"){
			return "field_data";
		}else if (xmlPath=="count"){
			return "field_data";
		}else if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="format"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_data";
		}else if (xmlPath=="content"){
			return "field_data";
		}else if (xmlPath=="cachePath"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:resourceSeries";
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
		xmlTxt+="\n</xnat:resourceSeries>";
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
		if (this.Path!=null)
			attTxt+=" path=\"" +this.Path +"\"";
		else attTxt+=" path=\"\"";//REQUIRED FIELD

		if (this.Pattern!=null)
			attTxt+=" pattern=\"" +this.Pattern +"\"";
		else attTxt+=" pattern=\"\"";//REQUIRED FIELD

		if (this.Count!=null)
			attTxt+=" count=\"" +this.Count +"\"";
		//NOT REQUIRED FIELD

		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		//NOT REQUIRED FIELD

		if (this.Format!=null)
			attTxt+=" format=\"" +this.Format +"\"";
		//NOT REQUIRED FIELD

		if (this.Description!=null)
			attTxt+=" description=\"" +this.Description +"\"";
		//NOT REQUIRED FIELD

		if (this.Content!=null)
			attTxt+=" content=\"" +this.Content +"\"";
		//NOT REQUIRED FIELD

		if (this.Cachepath!=null)
			attTxt+=" cachePath=\"" +this.Cachepath +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		xmlTxt+=this.extension.getXMLBody(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
