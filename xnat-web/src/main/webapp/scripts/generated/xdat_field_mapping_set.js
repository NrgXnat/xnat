/*
 * web: xdat_field_mapping_set.js
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

function xdat_field_mapping_set(){
this.xsiType="xdat:field_mapping_set";

	this.getSchemaElementName=function(){
		return "field_mapping_set";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:field_mapping_set";
	}
	this.Allow =new Array();

	function getAllow() {
		return this.Allow;
	}
	this.getAllow=getAllow;


	function addAllow(v){
		this.Allow.push(v);
	}
	this.addAllow=addAllow;
	this.SubSet =new Array();

	function getSubSet() {
		return this.SubSet;
	}
	this.getSubSet=getSubSet;


	function addSubSet(v){
		this.SubSet.push(v);
	}
	this.addSubSet=addSubSet;

	this.Method=null;


	function getMethod() {
		return this.Method;
	}
	this.getMethod=getMethod;


	function setMethod(v){
		this.Method=v;
	}
	this.setMethod=setMethod;

	this.XdatFieldMappingSetId=null;


	function getXdatFieldMappingSetId() {
		return this.XdatFieldMappingSetId;
	}
	this.getXdatFieldMappingSetId=getXdatFieldMappingSetId;


	function setXdatFieldMappingSetId(v){
		this.XdatFieldMappingSetId=v;
	}
	this.setXdatFieldMappingSetId=setXdatFieldMappingSetId;

	this.permissions_allow_set_xdat_elem_xdat_element_access_id_fk=null;


	this.getpermissions_allow_set_xdat_elem_xdat_element_access_id=function() {
		return this.permissions_allow_set_xdat_elem_xdat_element_access_id_fk;
	}


	this.setpermissions_allow_set_xdat_elem_xdat_element_access_id=function(v){
		this.permissions_allow_set_xdat_elem_xdat_element_access_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="allow"){
				return this.Allow ;
			} else 
			if(xmlPath.startsWith("allow")){
				xmlPath=xmlPath.substring(5);
				if(xmlPath=="")return this.Allow ;
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

				for(var whereCount=0;whereCount<this.Allow.length;whereCount++){

					var tempValue=this.Allow[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Allow[whereCount]);

					}

				}
				}else{

				whereArray=this.Allow;
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
			if(xmlPath=="sub_set"){
				return this.SubSet ;
			} else 
			if(xmlPath.startsWith("sub_set")){
				xmlPath=xmlPath.substring(7);
				if(xmlPath=="")return this.SubSet ;
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

				for(var whereCount=0;whereCount<this.SubSet.length;whereCount++){

					var tempValue=this.SubSet[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.SubSet[whereCount]);

					}

				}
				}else{

				whereArray=this.SubSet;
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
			if(xmlPath=="xdat_field_mapping_set_id"){
				return this.XdatFieldMappingSetId ;
			} else 
			if(xmlPath=="permissions_allow_set_xdat_elem_xdat_element_access_id"){
				return this.permissions_allow_set_xdat_elem_xdat_element_access_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="allow"){
				this.Allow=value;
			} else 
			if(xmlPath.startsWith("allow")){
				xmlPath=xmlPath.substring(5);
				if(xmlPath=="")return this.Allow ;
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

				for(var whereCount=0;whereCount<this.Allow.length;whereCount++){

					var tempValue=this.Allow[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Allow[whereCount]);

					}

				}
				}else{

				whereArray=this.Allow;
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
						newChild= instanciateObject("xdat:field_mapping");//omUtils.js
					}
					this.addAllow(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="sub_set"){
				this.SubSet=value;
			} else 
			if(xmlPath.startsWith("sub_set")){
				xmlPath=xmlPath.substring(7);
				if(xmlPath=="")return this.SubSet ;
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

				for(var whereCount=0;whereCount<this.SubSet.length;whereCount++){

					var tempValue=this.SubSet[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.SubSet[whereCount]);

					}

				}
				}else{

				whereArray=this.SubSet;
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
						newChild= instanciateObject("xdat:field_mapping_set");//omUtils.js
					}
					this.addSubSet(newChild);
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
			if(xmlPath=="xdat_field_mapping_set_id"){
				this.XdatFieldMappingSetId=value;
			} else 
			if(xmlPath=="permissions_allow_set_xdat_elem_xdat_element_access_id"){
				this.permissions_allow_set_xdat_elem_xdat_element_access_id_fk=value;
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
		if (xmlPath=="allow"){
			this.addAllow(v);
		}else if (xmlPath=="sub_set"){
			this.addSubSet(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="allow"){
			return "http://nrg.wustl.edu/security:field_mapping";
		}else if (xmlPath=="sub_set"){
			return "http://nrg.wustl.edu/security:field_mapping_set";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="allow"){
			return "field_multi_reference";
		}else if (xmlPath=="sub_set"){
			return "field_multi_reference";
		}else if (xmlPath=="method"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:field_mapping_set";
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
		xmlTxt+="\n</xdat:field_mapping_set>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatFieldMappingSetId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_field_mapping_set_id=\"" + this.XdatFieldMappingSetId + "\"";
			}
			if(this.permissions_allow_set_xdat_elem_xdat_element_access_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="permissions_allow_set_xdat_elem_xdat_element_access_id=\"" + this.permissions_allow_set_xdat_elem_xdat_element_access_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Method!=null)
			attTxt+=" method=\"" +this.Method +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		for(var AllowCOUNT=0;AllowCOUNT<this.Allow.length;AllowCOUNT++){
			xmlTxt +="\n<xdat:allow";
			xmlTxt +=this.Allow[AllowCOUNT].getXMLAtts();
			if(this.Allow[AllowCOUNT].xsiType!="xdat:field_mapping"){
				xmlTxt+=" xsi:type=\"" + this.Allow[AllowCOUNT].xsiType + "\"";
			}
			if (this.Allow[AllowCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Allow[AllowCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:allow>";
			}else {xmlTxt+="/>";}
		}
		for(var SubSetCOUNT=0;SubSetCOUNT<this.SubSet.length;SubSetCOUNT++){
			xmlTxt +="\n<xdat:sub_set";
			xmlTxt +=this.SubSet[SubSetCOUNT].getXMLAtts();
			if(this.SubSet[SubSetCOUNT].xsiType!="xdat:field_mapping_set"){
				xmlTxt+=" xsi:type=\"" + this.SubSet[SubSetCOUNT].xsiType + "\"";
			}
			if (this.SubSet[SubSetCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.SubSet[SubSetCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:sub_set>";
			}else {xmlTxt+="/>";}
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatFieldMappingSetId!=null) return true;
			if (this.permissions_allow_set_xdat_elem_xdat_element_access_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.Allow.length>0) return true;
		if(this.SubSet.length>0) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
