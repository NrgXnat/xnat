/*
 * web: pipe_PipelineRepository.js
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

function pipe_PipelineRepository(){
this.xsiType="pipe:PipelineRepository";

	this.getSchemaElementName=function(){
		return "PipelineRepository";
	}

	this.getFullSchemaElementName=function(){
		return "pipe:PipelineRepository";
	}
	this.Pipeline =new Array();

	function getPipeline() {
		return this.Pipeline;
	}
	this.getPipeline=getPipeline;


	function addPipeline(v){
		this.Pipeline.push(v);
	}
	this.addPipeline=addPipeline;

	this.PipePipelinerepositoryId=null;


	function getPipePipelinerepositoryId() {
		return this.PipePipelinerepositoryId;
	}
	this.getPipePipelinerepositoryId=getPipePipelinerepositoryId;


	function setPipePipelinerepositoryId(v){
		this.PipePipelinerepositoryId=v;
	}
	this.setPipePipelinerepositoryId=setPipePipelinerepositoryId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="pipeline"){
				return this.Pipeline ;
			} else 
			if(xmlPath.startsWith("pipeline")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Pipeline ;
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

				for(var whereCount=0;whereCount<this.Pipeline.length;whereCount++){

					var tempValue=this.Pipeline[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Pipeline[whereCount]);

					}

				}
				}else{

				whereArray=this.Pipeline;
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
			if(xmlPath=="pipe_PipelineRepository_id"){
				return this.PipePipelinerepositoryId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="pipeline"){
				this.Pipeline=value;
			} else 
			if(xmlPath.startsWith("pipeline")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Pipeline ;
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

				for(var whereCount=0;whereCount<this.Pipeline.length;whereCount++){

					var tempValue=this.Pipeline[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Pipeline[whereCount]);

					}

				}
				}else{

				whereArray=this.Pipeline;
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
						newChild= instanciateObject("pipe:pipelineDetails");//omUtils.js
					}
					this.addPipeline(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="pipe_PipelineRepository_id"){
				this.PipePipelinerepositoryId=value;
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
		if (xmlPath=="pipeline"){
			this.addPipeline(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="pipeline"){
			return "http://nrg.wustl.edu/pipe:pipelineDetails";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="pipeline"){
			return "field_multi_reference";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<pipe:PipelineRepository";
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
		xmlTxt+="\n</pipe:PipelineRepository>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.PipePipelinerepositoryId!=null){
				if(hiddenCount++>0)str+=",";
				str+="pipe_PipelineRepository_id=\"" + this.PipePipelinerepositoryId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		for(var PipelineCOUNT=0;PipelineCOUNT<this.Pipeline.length;PipelineCOUNT++){
			xmlTxt +="\n<pipe:pipeline";
			xmlTxt +=this.Pipeline[PipelineCOUNT].getXMLAtts();
			if(this.Pipeline[PipelineCOUNT].xsiType!="pipe:pipelineDetails"){
				xmlTxt+=" xsi:type=\"" + this.Pipeline[PipelineCOUNT].xsiType + "\"";
			}
			if (this.Pipeline[PipelineCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Pipeline[PipelineCOUNT].getXMLBody(preventComments);
					xmlTxt+="</pipe:pipeline>";
			}else {xmlTxt+="/>";}
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.PipePipelinerepositoryId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.Pipeline.length>0) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
