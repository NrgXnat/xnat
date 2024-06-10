/*
 * web: pipe_pipelineDetails.js
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

function pipe_pipelineDetails(){
this.xsiType="pipe:pipelineDetails";

	this.getSchemaElementName=function(){
		return "pipelineDetails";
	}

	this.getFullSchemaElementName=function(){
		return "pipe:pipelineDetails";
	}

	this.Path=null;


	function getPath() {
		return this.Path;
	}
	this.getPath=getPath;


	function setPath(v){
		this.Path=v;
	}
	this.setPath=setPath;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;
	this.Generateselements_element =new Array();

	function getGenerateselements_element() {
		return this.Generateselements_element;
	}
	this.getGenerateselements_element=getGenerateselements_element;


	function addGenerateselements_element(v){
		this.Generateselements_element.push(v);
	}
	this.addGenerateselements_element=addGenerateselements_element;

	this.Customwebpage=null;


	function getCustomwebpage() {
		return this.Customwebpage;
	}
	this.getCustomwebpage=getCustomwebpage;


	function setCustomwebpage(v){
		this.Customwebpage=v;
	}
	this.setCustomwebpage=setCustomwebpage;
	this.Parameters_parameter =new Array();

	function getParameters_parameter() {
		return this.Parameters_parameter;
	}
	this.getParameters_parameter=getParameters_parameter;


	function addParameters_parameter(v){
		this.Parameters_parameter.push(v);
	}
	this.addParameters_parameter=addParameters_parameter;

	this.Appliesto=null;


	function getAppliesto() {
		return this.Appliesto;
	}
	this.getAppliesto=getAppliesto;


	function setAppliesto(v){
		this.Appliesto=v;
	}
	this.setAppliesto=setAppliesto;

	this.pipe_PipelineRepository_pipe_pipelinerepository_id_fk=null;


	this.getpipe_PipelineRepository_pipe_pipelinerepository_id=function() {
		return this.pipe_PipelineRepository_pipe_pipelinerepository_id_fk;
	}


	this.setpipe_PipelineRepository_pipe_pipelinerepository_id=function(v){
		this.pipe_PipelineRepository_pipe_pipelinerepository_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="path"){
				return this.Path ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="generatesElements/element"){
				return this.Generateselements_element ;
			} else 
			if(xmlPath.startsWith("generatesElements/element")){
				xmlPath=xmlPath.substring(25);
				if(xmlPath=="")return this.Generateselements_element ;
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

				for(var whereCount=0;whereCount<this.Generateselements_element.length;whereCount++){

					var tempValue=this.Generateselements_element[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Generateselements_element[whereCount]);

					}

				}
				}else{

				whereArray=this.Generateselements_element;
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
			if(xmlPath=="customwebpage"){
				return this.Customwebpage ;
			} else 
			if(xmlPath=="parameters/parameter"){
				return this.Parameters_parameter ;
			} else 
			if(xmlPath.startsWith("parameters/parameter")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Parameters_parameter ;
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

				for(var whereCount=0;whereCount<this.Parameters_parameter.length;whereCount++){

					var tempValue=this.Parameters_parameter[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_parameter[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_parameter;
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
			if(xmlPath=="appliesTo"){
				return this.Appliesto ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="pipe_PipelineRepository_pipe_pipelinerepository_id"){
				return this.pipe_PipelineRepository_pipe_pipelinerepository_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="path"){
				this.Path=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="generatesElements/element"){
				this.Generateselements_element=value;
			} else 
			if(xmlPath.startsWith("generatesElements/element")){
				xmlPath=xmlPath.substring(25);
				if(xmlPath=="")return this.Generateselements_element ;
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

				for(var whereCount=0;whereCount<this.Generateselements_element.length;whereCount++){

					var tempValue=this.Generateselements_element[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Generateselements_element[whereCount]);

					}

				}
				}else{

				whereArray=this.Generateselements_element;
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
						newChild= instanciateObject("pipe:pipelineDetails_element");//omUtils.js
					}
					this.addGenerateselements_element(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="customwebpage"){
				this.Customwebpage=value;
			} else 
			if(xmlPath=="parameters/parameter"){
				this.Parameters_parameter=value;
			} else 
			if(xmlPath.startsWith("parameters/parameter")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Parameters_parameter ;
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

				for(var whereCount=0;whereCount<this.Parameters_parameter.length;whereCount++){

					var tempValue=this.Parameters_parameter[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_parameter[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_parameter;
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
						newChild= instanciateObject("pipe:pipelineDetails_parameter");//omUtils.js
					}
					this.addParameters_parameter(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="appliesTo"){
				this.Appliesto=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="pipe_PipelineRepository_pipe_pipelinerepository_id"){
				this.pipe_PipelineRepository_pipe_pipelinerepository_id_fk=value;
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
		if (xmlPath=="generatesElements/element"){
			this.addGenerateselements_element(v);
		}else if (xmlPath=="parameters/parameter"){
			this.addParameters_parameter(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="generatesElements/element"){
			return "http://nrg.wustl.edu/pipe:pipelineDetails_element";
		}else if (xmlPath=="parameters/parameter"){
			return "http://nrg.wustl.edu/pipe:pipelineDetails_parameter";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="path"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_LONG_DATA";
		}else if (xmlPath=="generatesElements/element"){
			return "field_inline_repeater";
		}else if (xmlPath=="customwebpage"){
			return "field_data";
		}else if (xmlPath=="parameters/parameter"){
			return "field_multi_reference";
		}else if (xmlPath=="appliesTo"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<pipe:pipelineDetails";
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
		xmlTxt+="\n</pipe:pipelineDetails>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.pipe_PipelineRepository_pipe_pipelinerepository_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="pipe_PipelineRepository_pipe_pipelinerepository_id=\"" + this.pipe_PipelineRepository_pipe_pipelinerepository_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Appliesto!=null)
			attTxt+=" appliesTo=\"" +this.Appliesto +"\"";
		else attTxt+=" appliesTo=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Path!=null){
			xmlTxt+="\n<pipe:path";
			xmlTxt+=">";
			xmlTxt+=this.Path.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</pipe:path>";
		}
		else{
			xmlTxt+="\n<pipe:path";
			xmlTxt+="/>";
		}

		if (this.Description!=null){
			xmlTxt+="\n<pipe:description";
			xmlTxt+=">";
			xmlTxt+=this.Description.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</pipe:description>";
		}
			var child0=0;
			var att0=0;
			child0+=this.Generateselements_element.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<pipe:generatesElements";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Generateselements_elementCOUNT=0;Generateselements_elementCOUNT<this.Generateselements_element.length;Generateselements_elementCOUNT++){
			xmlTxt+=this.Generateselements_element[Generateselements_elementCOUNT].getXMLBody(preventComments);
		}
				xmlTxt+="\n</pipe:generatesElements>";
			}
			}

		if (this.Customwebpage!=null){
			xmlTxt+="\n<pipe:customwebpage";
			xmlTxt+=">";
			xmlTxt+=this.Customwebpage.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</pipe:customwebpage>";
		}
			var child1=0;
			var att1=0;
			child1+=this.Parameters_parameter.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<pipe:parameters";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Parameters_parameterCOUNT=0;Parameters_parameterCOUNT<this.Parameters_parameter.length;Parameters_parameterCOUNT++){
			xmlTxt +="\n<pipe:parameter";
			xmlTxt +=this.Parameters_parameter[Parameters_parameterCOUNT].getXMLAtts();
			if(this.Parameters_parameter[Parameters_parameterCOUNT].xsiType!="pipe:pipelineDetails_parameter"){
				xmlTxt+=" xsi:type=\"" + this.Parameters_parameter[Parameters_parameterCOUNT].xsiType + "\"";
			}
			if (this.Parameters_parameter[Parameters_parameterCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Parameters_parameter[Parameters_parameterCOUNT].getXMLBody(preventComments);
					xmlTxt+="</pipe:parameter>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</pipe:parameters>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.pipe_PipelineRepository_pipe_pipelinerepository_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Path!=null) return true;
		return true;//REQUIRED path
	}
}
