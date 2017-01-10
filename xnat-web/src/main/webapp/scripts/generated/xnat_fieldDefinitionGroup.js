/*
 * web: xnat_fieldDefinitionGroup.js
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

function xnat_fieldDefinitionGroup(){
this.xsiType="xnat:fieldDefinitionGroup";

	this.getSchemaElementName=function(){
		return "fieldDefinitionGroup";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:fieldDefinitionGroup";
	}
	this.Fields_field =new Array();

	function getFields_field() {
		return this.Fields_field;
	}
	this.getFields_field=getFields_field;


	function addFields_field(v){
		this.Fields_field.push(v);
	}
	this.addFields_field=addFields_field;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.DataType=null;


	function getDataType() {
		return this.DataType;
	}
	this.getDataType=getDataType;


	function setDataType(v){
		this.DataType=v;
	}
	this.setDataType=setDataType;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;

	this.Shareable=null;


	function getShareable() {
		return this.Shareable;
	}
	this.getShareable=getShareable;


	function setShareable(v){
		this.Shareable=v;
	}
	this.setShareable=setShareable;


	this.isShareable=function(defaultValue) {
		if(this.Shareable==null)return defaultValue;
		if(this.Shareable=="1" || this.Shareable==true)return true;
		return false;
	}

	this.ProjectSpecific=null;


	function getProjectSpecific() {
		return this.ProjectSpecific;
	}
	this.getProjectSpecific=getProjectSpecific;


	function setProjectSpecific(v){
		this.ProjectSpecific=v;
	}
	this.setProjectSpecific=setProjectSpecific;


	this.isProjectSpecific=function(defaultValue) {
		if(this.ProjectSpecific==null)return defaultValue;
		if(this.ProjectSpecific=="1" || this.ProjectSpecific==true)return true;
		return false;
	}

	this.XnatFielddefinitiongroupId=null;


	function getXnatFielddefinitiongroupId() {
		return this.XnatFielddefinitiongroupId;
	}
	this.getXnatFielddefinitiongroupId=getXnatFielddefinitiongroupId;


	function setXnatFielddefinitiongroupId(v){
		this.XnatFielddefinitiongroupId=v;
	}
	this.setXnatFielddefinitiongroupId=setXnatFielddefinitiongroupId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="fields/field"){
				return this.Fields_field ;
			} else 
			if(xmlPath.startsWith("fields/field")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Fields_field ;
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

				for(var whereCount=0;whereCount<this.Fields_field.length;whereCount++){

					var tempValue=this.Fields_field[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Fields_field[whereCount]);

					}

				}
				}else{

				whereArray=this.Fields_field;
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
			if(xmlPath=="ID"){
				return this.Id ;
			} else 
			if(xmlPath=="data-type"){
				return this.DataType ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="shareable"){
				return this.Shareable ;
			} else 
			if(xmlPath=="project-specific"){
				return this.ProjectSpecific ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_fieldDefinitionGroup_id"){
				return this.XnatFielddefinitiongroupId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="fields/field"){
				this.Fields_field=value;
			} else 
			if(xmlPath.startsWith("fields/field")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Fields_field ;
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

				for(var whereCount=0;whereCount<this.Fields_field.length;whereCount++){

					var tempValue=this.Fields_field[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Fields_field[whereCount]);

					}

				}
				}else{

				whereArray=this.Fields_field;
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
						newChild= instanciateObject("xnat:fieldDefinitionGroup_field");//omUtils.js
					}
					this.addFields_field(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="data-type"){
				this.DataType=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="shareable"){
				this.Shareable=value;
			} else 
			if(xmlPath=="project-specific"){
				this.ProjectSpecific=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_fieldDefinitionGroup_id"){
				this.XnatFielddefinitiongroupId=value;
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
		if (xmlPath=="fields/field"){
			this.addFields_field(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="fields/field"){
			return "http://nrg.wustl.edu/xnat:fieldDefinitionGroup_field";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="fields/field"){
			return "field_multi_reference";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="data-type"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_LONG_DATA";
		}else if (xmlPath=="shareable"){
			return "field_data";
		}else if (xmlPath=="project-specific"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:FieldDefinitionSet";
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
		xmlTxt+="\n</xnat:FieldDefinitionSet>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatFielddefinitiongroupId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_fieldDefinitionGroup_id=\"" + this.XnatFielddefinitiongroupId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Id!=null)
			attTxt+=" ID=\"" +this.Id +"\"";
		//NOT REQUIRED FIELD

		if (this.DataType!=null)
			attTxt+=" data-type=\"" +this.DataType +"\"";
		//NOT REQUIRED FIELD

		if (this.Description!=null)
			attTxt+=" description=\"" +this.Description +"\"";
		//NOT REQUIRED FIELD

		if (this.Shareable!=null)
			attTxt+=" shareable=\"" +this.Shareable +"\"";
		//NOT REQUIRED FIELD

		if (this.ProjectSpecific!=null)
			attTxt+=" project-specific=\"" +this.ProjectSpecific +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Fields_field.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:fields";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Fields_fieldCOUNT=0;Fields_fieldCOUNT<this.Fields_field.length;Fields_fieldCOUNT++){
			xmlTxt +="\n<xnat:field";
			xmlTxt +=this.Fields_field[Fields_fieldCOUNT].getXMLAtts();
			if(this.Fields_field[Fields_fieldCOUNT].xsiType!="xnat:fieldDefinitionGroup_field"){
				xmlTxt+=" xsi:type=\"" + this.Fields_field[Fields_fieldCOUNT].xsiType + "\"";
			}
			if (this.Fields_field[Fields_fieldCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Fields_field[Fields_fieldCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:field>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:fields>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatFielddefinitiongroupId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.Fields_field.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
