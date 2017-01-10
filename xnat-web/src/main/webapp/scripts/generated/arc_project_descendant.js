/*
 * web: arc_project_descendant.js
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

function arc_project_descendant(){
this.xsiType="arc:project_descendant";

	this.getSchemaElementName=function(){
		return "project_descendant";
	}

	this.getFullSchemaElementName=function(){
		return "arc:project_descendant";
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

	this.Xsitype=null;


	function getXsitype() {
		return this.Xsitype;
	}
	this.getXsitype=getXsitype;


	function setXsitype(v){
		this.Xsitype=v;
	}
	this.setXsitype=setXsitype;

	this.ArcProjectDescendantId=null;


	function getArcProjectDescendantId() {
		return this.ArcProjectDescendantId;
	}
	this.getArcProjectDescendantId=getArcProjectDescendantId;


	function setArcProjectDescendantId(v){
		this.ArcProjectDescendantId=v;
	}
	this.setArcProjectDescendantId=setArcProjectDescendantId;

	this.pipelines_descendants_descendan_arc_project_id_fk=null;


	this.getpipelines_descendants_descendan_arc_project_id=function() {
		return this.pipelines_descendants_descendan_arc_project_id_fk;
	}


	this.setpipelines_descendants_descendan_arc_project_id=function(v){
		this.pipelines_descendants_descendan_arc_project_id_fk=v;
	}


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
			if(xmlPath=="xsiType"){
				return this.Xsitype ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="arc_project_descendant_id"){
				return this.ArcProjectDescendantId ;
			} else 
			if(xmlPath=="pipelines_descendants_descendan_arc_project_id"){
				return this.pipelines_descendants_descendan_arc_project_id_fk ;
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
						newChild= instanciateObject("arc:project_descendant_pipeline");//omUtils.js
					}
					this.addPipeline(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="xsiType"){
				this.Xsitype=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="arc_project_descendant_id"){
				this.ArcProjectDescendantId=value;
			} else 
			if(xmlPath=="pipelines_descendants_descendan_arc_project_id"){
				this.pipelines_descendants_descendan_arc_project_id_fk=value;
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
			return "http://nrg.wustl.edu/arc:project_descendant_pipeline";
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
		}else if (xmlPath=="xsiType"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<arc:project_descendant";
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
		xmlTxt+="\n</arc:project_descendant>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ArcProjectDescendantId!=null){
				if(hiddenCount++>0)str+=",";
				str+="arc_project_descendant_id=\"" + this.ArcProjectDescendantId + "\"";
			}
			if(this.pipelines_descendants_descendan_arc_project_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="pipelines_descendants_descendan_arc_project_id=\"" + this.pipelines_descendants_descendan_arc_project_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Xsitype!=null)
			attTxt+=" xsiType=\"" +this.Xsitype +"\"";
		else attTxt+=" xsiType=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		for(var PipelineCOUNT=0;PipelineCOUNT<this.Pipeline.length;PipelineCOUNT++){
			xmlTxt +="\n<arc:pipeline";
			xmlTxt +=this.Pipeline[PipelineCOUNT].getXMLAtts();
			if(this.Pipeline[PipelineCOUNT].xsiType!="arc:project_descendant_pipeline"){
				xmlTxt+=" xsi:type=\"" + this.Pipeline[PipelineCOUNT].xsiType + "\"";
			}
			if (this.Pipeline[PipelineCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Pipeline[PipelineCOUNT].getXMLBody(preventComments);
					xmlTxt+="</arc:pipeline>";
			}else {xmlTxt+="/>";}
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ArcProjectDescendantId!=null) return true;
			if (this.pipelines_descendants_descendan_arc_project_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.Pipeline.length>0) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
