/*
 * web: arc_pipelineData.js
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

function arc_pipelineData(){
this.xsiType="arc:pipelineData";

	this.getSchemaElementName=function(){
		return "pipelineData";
	}

	this.getFullSchemaElementName=function(){
		return "arc:pipelineData";
	}

	this.Displaytext=null;


	function getDisplaytext() {
		return this.Displaytext;
	}
	this.getDisplaytext=getDisplaytext;


	function setDisplaytext(v){
		this.Displaytext=v;
	}
	this.setDisplaytext=setDisplaytext;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.Location=null;


	function getLocation() {
		return this.Location;
	}
	this.getLocation=getLocation;


	function setLocation(v){
		this.Location=v;
	}
	this.setLocation=setLocation;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;
	this.Parameters_parameter =new Array();

	function getParameters_parameter() {
		return this.Parameters_parameter;
	}
	this.getParameters_parameter=getParameters_parameter;


	function addParameters_parameter(v){
		this.Parameters_parameter.push(v);
	}
	this.addParameters_parameter=addParameters_parameter;

	this.Customwebpage=null;


	function getCustomwebpage() {
		return this.Customwebpage;
	}
	this.getCustomwebpage=getCustomwebpage;


	function setCustomwebpage(v){
		this.Customwebpage=v;
	}
	this.setCustomwebpage=setCustomwebpage;

	this.ArcPipelinedataId=null;


	function getArcPipelinedataId() {
		return this.ArcPipelinedataId;
	}
	this.getArcPipelinedataId=getArcPipelinedataId;


	function setArcPipelinedataId(v){
		this.ArcPipelinedataId=v;
	}
	this.setArcPipelinedataId=setArcPipelinedataId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="displayText"){
				return this.Displaytext ;
			} else 
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="location"){
				return this.Location ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
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
			if(xmlPath=="customwebpage"){
				return this.Customwebpage ;
			} else 
			if(xmlPath=="extension_item"){
				return this.ExtensionItem ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="arc_pipelineData_id"){
				return this.ArcPipelinedataId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="displayText"){
				this.Displaytext=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="location"){
				this.Location=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
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
						newChild= instanciateObject("arc:pipelineParameterData");//omUtils.js
					}
					this.addParameters_parameter(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="customwebpage"){
				this.Customwebpage=value;
			} else 
			if(xmlPath=="extension_item"){
				this.ExtensionItem=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="arc_pipelineData_id"){
				this.ArcPipelinedataId=value;
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
		if (xmlPath=="parameters/parameter"){
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
		if (xmlPath=="parameters/parameter"){
			return "http://nrg.wustl.edu/arc:pipelineParameterData";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="displayText"){
			return "field_data";
		}else if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="location"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_data";
		}else if (xmlPath=="parameters/parameter"){
			return "field_multi_reference";
		}else if (xmlPath=="customwebpage"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<arc:pipelineData";
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
		xmlTxt+="\n</arc:pipelineData>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ArcPipelinedataId!=null){
				if(hiddenCount++>0)str+=",";
				str+="arc_pipelineData_id=\"" + this.ArcPipelinedataId + "\"";
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
		if (this.Displaytext!=null){
			xmlTxt+="\n<arc:displayText";
			xmlTxt+=">";
			xmlTxt+=this.Displaytext.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:displayText>";
		}
		if (this.Name!=null){
			xmlTxt+="\n<arc:name";
			xmlTxt+=">";
			xmlTxt+=this.Name.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:name>";
		}
		if (this.Location!=null){
			xmlTxt+="\n<arc:location";
			xmlTxt+=">";
			xmlTxt+=this.Location.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:location>";
		}
		if (this.Description!=null){
			xmlTxt+="\n<arc:description";
			xmlTxt+=">";
			xmlTxt+=this.Description.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:description>";
		}
			var child0=0;
			var att0=0;
			child0+=this.Parameters_parameter.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<arc:parameters";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Parameters_parameterCOUNT=0;Parameters_parameterCOUNT<this.Parameters_parameter.length;Parameters_parameterCOUNT++){
			xmlTxt +="\n<arc:parameter";
			xmlTxt +=this.Parameters_parameter[Parameters_parameterCOUNT].getXMLAtts();
			if(this.Parameters_parameter[Parameters_parameterCOUNT].xsiType!="arc:pipelineParameterData"){
				xmlTxt+=" xsi:type=\"" + this.Parameters_parameter[Parameters_parameterCOUNT].xsiType + "\"";
			}
			if (this.Parameters_parameter[Parameters_parameterCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Parameters_parameter[Parameters_parameterCOUNT].getXMLBody(preventComments);
					xmlTxt+="</arc:parameter>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</arc:parameters>";
			}
			}

		if (this.Customwebpage!=null){
			xmlTxt+="\n<arc:customwebpage";
			xmlTxt+=">";
			xmlTxt+=this.Customwebpage.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</arc:customwebpage>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ArcPipelinedataId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Displaytext!=null) return true;
		if (this.Name!=null) return true;
		if (this.Location!=null) return true;
		if (this.Description!=null) return true;
			if(this.Parameters_parameter.length>0)return true;
		if (this.Customwebpage!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
