/*
 * web: xdat_criteria_set.js
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

function xdat_criteria_set(){
this.xsiType="xdat:criteria_set";

	this.getSchemaElementName=function(){
		return "criteria_set";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:criteria_set";
	}
	this.Criteria =new Array();

	function getCriteria() {
		return this.Criteria;
	}
	this.getCriteria=getCriteria;


	function addCriteria(v){
		this.Criteria.push(v);
	}
	this.addCriteria=addCriteria;
	this.ChildSet =new Array();

	function getChildSet() {
		return this.ChildSet;
	}
	this.getChildSet=getChildSet;


	function addChildSet(v){
		this.ChildSet.push(v);
	}
	this.addChildSet=addChildSet;

	this.Method=null;


	function getMethod() {
		return this.Method;
	}
	this.getMethod=getMethod;


	function setMethod(v){
		this.Method=v;
	}
	this.setMethod=setMethod;

	this.XdatCriteriaSetId=null;


	function getXdatCriteriaSetId() {
		return this.XdatCriteriaSetId;
	}
	this.getXdatCriteriaSetId=getXdatCriteriaSetId;


	function setXdatCriteriaSetId(v){
		this.XdatCriteriaSetId=v;
	}
	this.setXdatCriteriaSetId=setXdatCriteriaSetId;

	this.xdat_stored_search_id_fk=null;


	this.getxdat_stored_search_id=function() {
		return this.xdat_stored_search_id_fk;
	}


	this.setxdat_stored_search_id=function(v){
		this.xdat_stored_search_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="criteria"){
				return this.Criteria ;
			} else 
			if(xmlPath.startsWith("criteria")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Criteria ;
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

				for(var whereCount=0;whereCount<this.Criteria.length;whereCount++){

					var tempValue=this.Criteria[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Criteria[whereCount]);

					}

				}
				}else{

				whereArray=this.Criteria;
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
			if(xmlPath=="child_set"){
				return this.ChildSet ;
			} else 
			if(xmlPath.startsWith("child_set")){
				xmlPath=xmlPath.substring(9);
				if(xmlPath=="")return this.ChildSet ;
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

				for(var whereCount=0;whereCount<this.ChildSet.length;whereCount++){

					var tempValue=this.ChildSet[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ChildSet[whereCount]);

					}

				}
				}else{

				whereArray=this.ChildSet;
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
			if(xmlPath=="method"){
				return this.Method ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_criteria_set_id"){
				return this.XdatCriteriaSetId ;
			} else 
			if(xmlPath=="xdat_stored_search_id"){
				return this.xdat_stored_search_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="criteria"){
				this.Criteria=value;
			} else 
			if(xmlPath.startsWith("criteria")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Criteria ;
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

				for(var whereCount=0;whereCount<this.Criteria.length;whereCount++){

					var tempValue=this.Criteria[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Criteria[whereCount]);

					}

				}
				}else{

				whereArray=this.Criteria;
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
						newChild= instanciateObject("xdat:criteria");//omUtils.js
					}
					this.addCriteria(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="child_set"){
				this.ChildSet=value;
			} else 
			if(xmlPath.startsWith("child_set")){
				xmlPath=xmlPath.substring(9);
				if(xmlPath=="")return this.ChildSet ;
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

				for(var whereCount=0;whereCount<this.ChildSet.length;whereCount++){

					var tempValue=this.ChildSet[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ChildSet[whereCount]);

					}

				}
				}else{

				whereArray=this.ChildSet;
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
						newChild= instanciateObject("xdat:criteria_set");//omUtils.js
					}
					this.addChildSet(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="method"){
				this.Method=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_criteria_set_id"){
				this.XdatCriteriaSetId=value;
			} else 
			if(xmlPath=="xdat_stored_search_id"){
				this.xdat_stored_search_id_fk=value;
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
		if (xmlPath=="criteria"){
			this.addCriteria(v);
		}else if (xmlPath=="child_set"){
			this.addChildSet(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="criteria"){
			return "http://nrg.wustl.edu/security:criteria";
		}else if (xmlPath=="child_set"){
			return "http://nrg.wustl.edu/security:criteria_set";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="criteria"){
			return "field_multi_reference";
		}else if (xmlPath=="child_set"){
			return "field_multi_reference";
		}else if (xmlPath=="method"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:criteria_set";
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
		xmlTxt+="\n</xdat:criteria_set>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatCriteriaSetId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_criteria_set_id=\"" + this.XdatCriteriaSetId + "\"";
			}
			if(this.xdat_stored_search_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_stored_search_id=\"" + this.xdat_stored_search_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Method!=null)
			attTxt+=" method=\"" +this.Method +"\"";
		else attTxt+=" method=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		for(var CriteriaCOUNT=0;CriteriaCOUNT<this.Criteria.length;CriteriaCOUNT++){
			xmlTxt +="\n<xdat:criteria";
			xmlTxt +=this.Criteria[CriteriaCOUNT].getXMLAtts();
			if(this.Criteria[CriteriaCOUNT].xsiType!="xdat:criteria"){
				xmlTxt+=" xsi:type=\"" + this.Criteria[CriteriaCOUNT].xsiType + "\"";
			}
			if (this.Criteria[CriteriaCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Criteria[CriteriaCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:criteria>";
			}else {xmlTxt+="/>";}
		}
		for(var ChildSetCOUNT=0;ChildSetCOUNT<this.ChildSet.length;ChildSetCOUNT++){
			xmlTxt +="\n<xdat:child_set";
			xmlTxt +=this.ChildSet[ChildSetCOUNT].getXMLAtts();
			if(this.ChildSet[ChildSetCOUNT].xsiType!="xdat:criteria_set"){
				xmlTxt+=" xsi:type=\"" + this.ChildSet[ChildSetCOUNT].xsiType + "\"";
			}
			if (this.ChildSet[ChildSetCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.ChildSet[ChildSetCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:child_set>";
			}else {xmlTxt+="/>";}
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatCriteriaSetId!=null) return true;
			if (this.xdat_stored_search_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.Criteria.length>0) return true;
		if(this.ChildSet.length>0) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
