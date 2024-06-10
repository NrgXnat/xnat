/*
 * web: cat_dcmEntry.js
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

function cat_dcmEntry(){
this.xsiType="cat:dcmEntry";

	this.getSchemaElementName=function(){
		return "dcmEntry";
	}

	this.getFullSchemaElementName=function(){
		return "cat:dcmEntry";
	}
this.extension=dynamicJSLoad('cat_entry','generated/cat_entry.js');

	this.Uid=null;


	function getUid() {
		return this.Uid;
	}
	this.getUid=getUid;


	function setUid(v){
		this.Uid=v;
	}
	this.setUid=setUid;

	this.Instancenumber=null;


	function getInstancenumber() {
		return this.Instancenumber;
	}
	this.getInstancenumber=getInstancenumber;


	function setInstancenumber(v){
		this.Instancenumber=v;
	}
	this.setInstancenumber=setInstancenumber;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="entry"){
				return this.Entry ;
			} else 
			if(xmlPath.startsWith("entry")){
				xmlPath=xmlPath.substring(5);
				if(xmlPath=="")return this.Entry ;
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
				if(this.Entry!=undefined)return this.Entry.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="UID"){
				return this.Uid ;
			} else 
			if(xmlPath=="instanceNumber"){
				return this.Instancenumber ;
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
			if(xmlPath=="entry"){
				this.Entry=value;
			} else 
			if(xmlPath.startsWith("entry")){
				xmlPath=xmlPath.substring(5);
				if(xmlPath=="")return this.Entry ;
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
				if(this.Entry!=undefined){
					this.Entry.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Entry= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Entry= instanciateObject("cat:entry");//omUtils.js
						}
						if(options && options.where)this.Entry.setProperty(options.where.field,options.where.value);
						this.Entry.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="UID"){
				this.Uid=value;
			} else 
			if(xmlPath=="instanceNumber"){
				this.Instancenumber=value;
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
		if (xmlPath=="UID"){
			return "field_data";
		}else if (xmlPath=="instanceNumber"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<cat:dcmEntry";
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
		xmlTxt+="\n</cat:dcmEntry>";
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
		if (this.Uid!=null)
			attTxt+=" UID=\"" +this.Uid +"\"";
		//NOT REQUIRED FIELD

		if (this.Instancenumber!=null)
			attTxt+=" instanceNumber=\"" +this.Instancenumber +"\"";
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
