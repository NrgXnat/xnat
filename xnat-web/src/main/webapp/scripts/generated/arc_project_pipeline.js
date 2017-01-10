/*
 * web: arc_project_pipeline.js
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

function arc_project_pipeline(){
this.xsiType="arc:project_pipeline";

	this.getSchemaElementName=function(){
		return "project_pipeline";
	}

	this.getFullSchemaElementName=function(){
		return "arc:project_pipeline";
	}
this.extension=dynamicJSLoad('arc_pipelineData','generated/arc_pipelineData.js');

	this.Stepid=null;


	function getStepid() {
		return this.Stepid;
	}
	this.getStepid=getStepid;


	function setStepid(v){
		this.Stepid=v;
	}
	this.setStepid=setStepid;

	this.Dependent=null;


	function getDependent() {
		return this.Dependent;
	}
	this.getDependent=getDependent;


	function setDependent(v){
		this.Dependent=v;
	}
	this.setDependent=setDependent;


	this.isDependent=function(defaultValue) {
		if(this.Dependent==null)return defaultValue;
		if(this.Dependent=="1" || this.Dependent==true)return true;
		return false;
	}

	this.pipelines_pipeline_arc_project_arc_project_id_fk=null;


	this.getpipelines_pipeline_arc_project_arc_project_id=function() {
		return this.pipelines_pipeline_arc_project_arc_project_id_fk;
	}


	this.setpipelines_pipeline_arc_project_arc_project_id=function(v){
		this.pipelines_pipeline_arc_project_arc_project_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="pipelineData"){
				return this.Pipelinedata ;
			} else 
			if(xmlPath.startsWith("pipelineData")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Pipelinedata ;
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
				if(this.Pipelinedata!=undefined)return this.Pipelinedata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="stepId"){
				return this.Stepid ;
			} else 
			if(xmlPath=="dependent"){
				return this.Dependent ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="pipelines_pipeline_arc_project_arc_project_id"){
				return this.pipelines_pipeline_arc_project_arc_project_id_fk ;
			} else 
			{
				return this.extension.getProperty(xmlPath);
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="pipelineData"){
				this.Pipelinedata=value;
			} else 
			if(xmlPath.startsWith("pipelineData")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Pipelinedata ;
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
				if(this.Pipelinedata!=undefined){
					this.Pipelinedata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Pipelinedata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Pipelinedata= instanciateObject("arc:pipelineData");//omUtils.js
						}
						if(options && options.where)this.Pipelinedata.setProperty(options.where.field,options.where.value);
						this.Pipelinedata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="stepId"){
				this.Stepid=value;
			} else 
			if(xmlPath=="dependent"){
				this.Dependent=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="pipelines_pipeline_arc_project_arc_project_id"){
				this.pipelines_pipeline_arc_project_arc_project_id_fk=value;
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
		if (xmlPath=="stepId"){
			return "field_data";
		}else if (xmlPath=="dependent"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<arc:project_pipeline";
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
		xmlTxt+="\n</arc:project_pipeline>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.pipelines_pipeline_arc_project_arc_project_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="pipelines_pipeline_arc_project_arc_project_id=\"" + this.pipelines_pipeline_arc_project_arc_project_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = this.extension.getXMLAtts();
		if (this.Stepid!=null)
			attTxt+=" stepId=\"" +this.Stepid +"\"";
		else attTxt+=" stepId=\"\"";//REQUIRED FIELD

		if (this.Dependent!=null)
			attTxt+=" dependent=\"" +this.Dependent +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		xmlTxt+=this.extension.getXMLBody(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.pipelines_pipeline_arc_project_arc_project_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
