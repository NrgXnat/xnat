/*
 * web: xnat_imageAssessorData.js
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

function xnat_imageAssessorData(){
this.xsiType="xnat:imageAssessorData";

	this.getSchemaElementName=function(){
		return "imageAssessorData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:imageAssessorData";
	}
this.extension=dynamicJSLoad('xnat_derivedData','generated/xnat_derivedData.js');
	this.In_file =new Array();

	function getIn_file() {
		return this.In_file;
	}
	this.getIn_file=getIn_file;


	function addIn_file(v){
		this.In_file.push(v);
	}
	this.addIn_file=addIn_file;
	this.Out_file =new Array();

	function getOut_file() {
		return this.Out_file;
	}
	this.getOut_file=getOut_file;


	function addOut_file(v){
		this.Out_file.push(v);
	}
	this.addOut_file=addOut_file;

	this.ImagesessionId=null;


	function getImagesessionId() {
		return this.ImagesessionId;
	}
	this.getImagesessionId=getImagesessionId;


	function setImagesessionId(v){
		this.ImagesessionId=v;
	}
	this.setImagesessionId=setImagesessionId;
	this.Parameters_addparam =new Array();

	function getParameters_addparam() {
		return this.Parameters_addparam;
	}
	this.getParameters_addparam=getParameters_addparam;


	function addParameters_addparam(v){
		this.Parameters_addparam.push(v);
	}
	this.addParameters_addparam=addParameters_addparam;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="derivedData"){
				return this.Deriveddata ;
			} else 
			if(xmlPath.startsWith("derivedData")){
				xmlPath=xmlPath.substring(11);
				if(xmlPath=="")return this.Deriveddata ;
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
				if(this.Deriveddata!=undefined)return this.Deriveddata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="in/file"){
				return this.In_file ;
			} else 
			if(xmlPath.startsWith("in/file")){
				xmlPath=xmlPath.substring(7);
				if(xmlPath=="")return this.In_file ;
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

				for(var whereCount=0;whereCount<this.In_file.length;whereCount++){

					var tempValue=this.In_file[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.In_file[whereCount]);

					}

				}
				}else{

				whereArray=this.In_file;
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
			if(xmlPath=="out/file"){
				return this.Out_file ;
			} else 
			if(xmlPath.startsWith("out/file")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Out_file ;
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

				for(var whereCount=0;whereCount<this.Out_file.length;whereCount++){

					var tempValue=this.Out_file[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Out_file[whereCount]);

					}

				}
				}else{

				whereArray=this.Out_file;
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
			if(xmlPath=="imageSession_ID"){
				return this.ImagesessionId ;
			} else 
			if(xmlPath=="parameters/addParam"){
				return this.Parameters_addparam ;
			} else 
			if(xmlPath.startsWith("parameters/addParam")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Parameters_addparam ;
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

				for(var whereCount=0;whereCount<this.Parameters_addparam.length;whereCount++){

					var tempValue=this.Parameters_addparam[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_addparam[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_addparam;
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
			if(xmlPath=="derivedData"){
				this.Deriveddata=value;
			} else 
			if(xmlPath.startsWith("derivedData")){
				xmlPath=xmlPath.substring(11);
				if(xmlPath=="")return this.Deriveddata ;
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
				if(this.Deriveddata!=undefined){
					this.Deriveddata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Deriveddata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Deriveddata= instanciateObject("xnat:derivedData");//omUtils.js
						}
						if(options && options.where)this.Deriveddata.setProperty(options.where.field,options.where.value);
						this.Deriveddata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="in/file"){
				this.In_file=value;
			} else 
			if(xmlPath.startsWith("in/file")){
				xmlPath=xmlPath.substring(7);
				if(xmlPath=="")return this.In_file ;
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

				for(var whereCount=0;whereCount<this.In_file.length;whereCount++){

					var tempValue=this.In_file[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.In_file[whereCount]);

					}

				}
				}else{

				whereArray=this.In_file;
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
						newChild= instanciateObject("xnat:abstractResource");//omUtils.js
					}
					this.addIn_file(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="out/file"){
				this.Out_file=value;
			} else 
			if(xmlPath.startsWith("out/file")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Out_file ;
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

				for(var whereCount=0;whereCount<this.Out_file.length;whereCount++){

					var tempValue=this.Out_file[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Out_file[whereCount]);

					}

				}
				}else{

				whereArray=this.Out_file;
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
						newChild= instanciateObject("xnat:abstractResource");//omUtils.js
					}
					this.addOut_file(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="imageSession_ID"){
				this.ImagesessionId=value;
			} else 
			if(xmlPath=="parameters/addParam"){
				this.Parameters_addparam=value;
			} else 
			if(xmlPath.startsWith("parameters/addParam")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Parameters_addparam ;
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

				for(var whereCount=0;whereCount<this.Parameters_addparam.length;whereCount++){

					var tempValue=this.Parameters_addparam[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_addparam[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_addparam;
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
						newChild= instanciateObject("xnat:addField");//omUtils.js
					}
					this.addParameters_addparam(newChild);
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
		if (xmlPath=="in/file"){
			this.addIn_file(v);
		}else if (xmlPath=="out/file"){
			this.addOut_file(v);
		}else if (xmlPath=="parameters/addParam"){
			this.addParameters_addparam(v);
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
		if (xmlPath=="in/file"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="out/file"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="parameters/addParam"){
			return "http://nrg.wustl.edu/xnat:addField";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="in/file"){
			return "field_multi_reference";
		}else if (xmlPath=="out/file"){
			return "field_multi_reference";
		}else if (xmlPath=="imageSession_ID"){
			return "field_data";
		}else if (xmlPath=="parameters/addParam"){
			return "field_NO_CHILD";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:imageAssessorData";
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
		xmlTxt+="\n</xnat:imageAssessorData>";
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
			child0+=this.In_file.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:in";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var In_fileCOUNT=0;In_fileCOUNT<this.In_file.length;In_fileCOUNT++){
			xmlTxt +="\n<xnat:file";
			xmlTxt +=this.In_file[In_fileCOUNT].getXMLAtts();
			if(this.In_file[In_fileCOUNT].xsiType!="xnat:abstractResource"){
				xmlTxt+=" xsi:type=\"" + this.In_file[In_fileCOUNT].xsiType + "\"";
			}
			if (this.In_file[In_fileCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.In_file[In_fileCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:file>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:in>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.Out_file.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:out";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Out_fileCOUNT=0;Out_fileCOUNT<this.Out_file.length;Out_fileCOUNT++){
			xmlTxt +="\n<xnat:file";
			xmlTxt +=this.Out_file[Out_fileCOUNT].getXMLAtts();
			if(this.Out_file[Out_fileCOUNT].xsiType!="xnat:abstractResource"){
				xmlTxt+=" xsi:type=\"" + this.Out_file[Out_fileCOUNT].xsiType + "\"";
			}
			if (this.Out_file[Out_fileCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Out_file[Out_fileCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:file>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:out>";
			}
			}

		if (this.ImagesessionId!=null){
			xmlTxt+="\n<xnat:imageSession_ID";
			xmlTxt+=">";
			xmlTxt+=this.ImagesessionId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:imageSession_ID>";
		}
			var child2=0;
			var att2=0;
			child2+=this.Parameters_addparam.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xnat:parameters";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Parameters_addparamCOUNT=0;Parameters_addparamCOUNT<this.Parameters_addparam.length;Parameters_addparamCOUNT++){
			xmlTxt +="\n<xnat:addParam";
			xmlTxt +=this.Parameters_addparam[Parameters_addparamCOUNT].getXMLAtts();
			if(this.Parameters_addparam[Parameters_addparamCOUNT].xsiType!="xnat:addField"){
				xmlTxt+=" xsi:type=\"" + this.Parameters_addparam[Parameters_addparamCOUNT].xsiType + "\"";
			}
			if (this.Parameters_addparam[Parameters_addparamCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Parameters_addparam[Parameters_addparamCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:addParam>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:parameters>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.In_file.length>0)return true;
			if(this.Out_file.length>0)return true;
		if (this.ImagesessionId!=null) return true;
			if(this.Parameters_addparam.length>0)return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
