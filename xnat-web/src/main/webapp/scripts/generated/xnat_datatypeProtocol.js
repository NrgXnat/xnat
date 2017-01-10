/*
 * web: xnat_datatypeProtocol.js
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

function xnat_datatypeProtocol(){
this.xsiType="xnat:datatypeProtocol";

	this.getSchemaElementName=function(){
		return "datatypeProtocol";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:datatypeProtocol";
	}
this.extension=dynamicJSLoad('xnat_abstractProtocol','generated/xnat_abstractProtocol.js');
	this.Definitions_definition =new Array();

	function getDefinitions_definition() {
		return this.Definitions_definition;
	}
	this.getDefinitions_definition=getDefinitions_definition;


	function addDefinitions_definition(v){
		this.Definitions_definition.push(v);
	}
	this.addDefinitions_definition=addDefinitions_definition;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractProtocol"){
				return this.Abstractprotocol ;
			} else 
			if(xmlPath.startsWith("abstractProtocol")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Abstractprotocol ;
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
				if(this.Abstractprotocol!=undefined)return this.Abstractprotocol.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="definitions/definition"){
				return this.Definitions_definition ;
			} else 
			if(xmlPath.startsWith("definitions/definition")){
				xmlPath=xmlPath.substring(22);
				if(xmlPath=="")return this.Definitions_definition ;
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
				var index=0;
				if(options){
					if(options.index)index=options.index;
				}

			var whereArray;
				if (options.where){

				whereArray=new Array();

				for(var whereCount=0;whereCount<this.Definitions_definition.length;whereCount++){

					var tempValue=this.Definitions_definition[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Definitions_definition[whereCount]);

					}

				}
				}else{

				whereArray=this.Definitions_definition;
				}

			var typeArray;
				if (options.xsiType){

				typeArray=new Array();

				for(var typeCount=0;typeCount<whereArray.length;typeCount++){

					if(whereArray[typeCount].getFullSchemaElementName()==options.xsiType){

						typeArray.push(whereArray[typeCount]);

					}

				}
				}else{

				typeArray=whereArray;
				}
				if (typeArray.length>index){
					return typeArray[index].getProperty(xmlPath);
				}else{
					return null;
				}
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
			if(xmlPath=="abstractProtocol"){
				this.Abstractprotocol=value;
			} else 
			if(xmlPath.startsWith("abstractProtocol")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Abstractprotocol ;
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
				if(this.Abstractprotocol!=undefined){
					this.Abstractprotocol.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Abstractprotocol= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Abstractprotocol= instanciateObject("xnat:abstractProtocol");//omUtils.js
						}
						if(options && options.where)this.Abstractprotocol.setProperty(options.where.field,options.where.value);
						this.Abstractprotocol.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="definitions/definition"){
				this.Definitions_definition=value;
			} else 
			if(xmlPath.startsWith("definitions/definition")){
				xmlPath=xmlPath.substring(22);
				if(xmlPath=="")return this.Definitions_definition ;
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
				var index=0;
				if(options){
					if(options.index)index=options.index;
				}

			var whereArray;
				if (options && options.where){

				whereArray=new Array();

				for(var whereCount=0;whereCount<this.Definitions_definition.length;whereCount++){

					var tempValue=this.Definitions_definition[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Definitions_definition[whereCount]);

					}

				}
				}else{

				whereArray=this.Definitions_definition;
				}

			var typeArray;
				if (options && options.xsiType){

				typeArray=new Array();

				for(var typeCount=0;typeCount<whereArray.length;typeCount++){

					if(whereArray[typeCount].getFullSchemaElementName()==options.xsiType){

						typeArray.push(whereArray[typeCount]);

					}

				}
				}else{

				typeArray=whereArray;
				}
				if (typeArray.length>index){
					typeArray[index].setProperty(xmlPath,value);
				}else{
					var newChild;
					if(options && options.xsiType){
						newChild= instanciateObject(options.xsiType);//omUtils.js
					}else{
						newChild= instanciateObject("xnat:fieldDefinitionGroup");//omUtils.js
					}
					this.addDefinitions_definition(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
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
		if (xmlPath=="definitions/definition"){
			this.addDefinitions_definition(v);
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
		if (xmlPath=="definitions/definition"){
			return "http://nrg.wustl.edu/xnat:fieldDefinitionGroup";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="definitions/definition"){
			return "field_multi_reference";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:datatypeProtocol";
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
		xmlTxt+="\n</xnat:datatypeProtocol>";
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
			var child0=0;
			var att0=0;
			child0+=this.Definitions_definition.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:definitions";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Definitions_definitionCOUNT=0;Definitions_definitionCOUNT<this.Definitions_definition.length;Definitions_definitionCOUNT++){
			xmlTxt +="\n<xnat:definition";
			xmlTxt +=this.Definitions_definition[Definitions_definitionCOUNT].getXMLAtts();
			if(this.Definitions_definition[Definitions_definitionCOUNT].xsiType!="xnat:fieldDefinitionGroup"){
				xmlTxt+=" xsi:type=\"" + this.Definitions_definition[Definitions_definitionCOUNT].xsiType + "\"";
			}
			if (this.Definitions_definition[Definitions_definitionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Definitions_definition[Definitions_definitionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:definition>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:definitions>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Definitions_definition.length>0)return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
