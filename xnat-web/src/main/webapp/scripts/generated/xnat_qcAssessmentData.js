/*
 * web: xnat_qcAssessmentData.js
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

function xnat_qcAssessmentData(){
this.xsiType="xnat:qcAssessmentData";

	this.getSchemaElementName=function(){
		return "qcAssessmentData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:qcAssessmentData";
	}
this.extension=dynamicJSLoad('xnat_mrAssessorData','generated/xnat_mrAssessorData.js');
	this.Scans_scan =new Array();

	function getScans_scan() {
		return this.Scans_scan;
	}
	this.getScans_scan=getScans_scan;


	function addScans_scan(v){
		this.Scans_scan.push(v);
	}
	this.addScans_scan=addScans_scan;

	this.Type=null;


	function getType() {
		return this.Type;
	}
	this.getType=getType;


	function setType(v){
		this.Type=v;
	}
	this.setType=setType;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="mrAssessorData"){
				return this.Mrassessordata ;
			} else 
			if(xmlPath.startsWith("mrAssessorData")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Mrassessordata ;
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
				if(this.Mrassessordata!=undefined)return this.Mrassessordata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="scans/scan"){
				return this.Scans_scan ;
			} else 
			if(xmlPath.startsWith("scans/scan")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Scans_scan ;
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

				for(var whereCount=0;whereCount<this.Scans_scan.length;whereCount++){

					var tempValue=this.Scans_scan[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Scans_scan[whereCount]);

					}

				}
				}else{

				whereArray=this.Scans_scan;
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
			if(xmlPath=="type"){
				return this.Type ;
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
			if(xmlPath=="mrAssessorData"){
				this.Mrassessordata=value;
			} else 
			if(xmlPath.startsWith("mrAssessorData")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Mrassessordata ;
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
				if(this.Mrassessordata!=undefined){
					this.Mrassessordata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Mrassessordata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Mrassessordata= instanciateObject("xnat:mrAssessorData");//omUtils.js
						}
						if(options && options.where)this.Mrassessordata.setProperty(options.where.field,options.where.value);
						this.Mrassessordata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="scans/scan"){
				this.Scans_scan=value;
			} else 
			if(xmlPath.startsWith("scans/scan")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Scans_scan ;
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

				for(var whereCount=0;whereCount<this.Scans_scan.length;whereCount++){

					var tempValue=this.Scans_scan[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Scans_scan[whereCount]);

					}

				}
				}else{

				whereArray=this.Scans_scan;
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
						newChild= instanciateObject("xnat:qcAssessmentData_scan");//omUtils.js
					}
					this.addScans_scan(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="type"){
				this.Type=value;
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
		if (xmlPath=="scans/scan"){
			this.addScans_scan(v);
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
		if (xmlPath=="scans/scan"){
			return "http://nrg.wustl.edu/xnat:qcAssessmentData_scan";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="scans/scan"){
			return "field_multi_reference";
		}else if (xmlPath=="type"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:QCAssessment";
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
		xmlTxt+="\n</xnat:QCAssessment>";
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
		if (this.Type!=null)
			attTxt+=" type=\"" +this.Type +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		xmlTxt+=this.extension.getXMLBody(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Scans_scan.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:scans";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Scans_scanCOUNT=0;Scans_scanCOUNT<this.Scans_scan.length;Scans_scanCOUNT++){
			xmlTxt +="\n<xnat:scan";
			xmlTxt +=this.Scans_scan[Scans_scanCOUNT].getXMLAtts();
			if(this.Scans_scan[Scans_scanCOUNT].xsiType!="xnat:qcAssessmentData_scan"){
				xmlTxt+=" xsi:type=\"" + this.Scans_scan[Scans_scanCOUNT].xsiType + "\"";
			}
			if (this.Scans_scan[Scans_scanCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Scans_scan[Scans_scanCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:scan>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:scans>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Scans_scan.length>0)return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
