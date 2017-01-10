/*
 * web: xnat_fieldDefinitionGroup_field.js
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

function xnat_fieldDefinitionGroup_field(){
this.xsiType="xnat:fieldDefinitionGroup_field";

	this.getSchemaElementName=function(){
		return "fieldDefinitionGroup_field";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:fieldDefinitionGroup_field";
	}
	this.Possiblevalues_possiblevalue =new Array();

	function getPossiblevalues_possiblevalue() {
		return this.Possiblevalues_possiblevalue;
	}
	this.getPossiblevalues_possiblevalue=getPossiblevalues_possiblevalue;


	function addPossiblevalues_possiblevalue(v){
		this.Possiblevalues_possiblevalue.push(v);
	}
	this.addPossiblevalues_possiblevalue=addPossiblevalues_possiblevalue;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.Type=null;


	function getType() {
		return this.Type;
	}
	this.getType=getType;


	function setType(v){
		this.Type=v;
	}
	this.setType=setType;

	this.Datatype=null;


	function getDatatype() {
		return this.Datatype;
	}
	this.getDatatype=getDatatype;


	function setDatatype(v){
		this.Datatype=v;
	}
	this.setDatatype=setDatatype;

	this.Required=null;


	function getRequired() {
		return this.Required;
	}
	this.getRequired=getRequired;


	function setRequired(v){
		this.Required=v;
	}
	this.setRequired=setRequired;


	this.isRequired=function(defaultValue) {
		if(this.Required==null)return defaultValue;
		if(this.Required=="1" || this.Required==true)return true;
		return false;
	}

	this.Sequence=null;


	function getSequence() {
		return this.Sequence;
	}
	this.getSequence=getSequence;


	function setSequence(v){
		this.Sequence=v;
	}
	this.setSequence=setSequence;

	this.Xmlpath=null;


	function getXmlpath() {
		return this.Xmlpath;
	}
	this.getXmlpath=getXmlpath;


	function setXmlpath(v){
		this.Xmlpath=v;
	}
	this.setXmlpath=setXmlpath;

	this.Group=null;


	function getGroup() {
		return this.Group;
	}
	this.getGroup=getGroup;


	function setGroup(v){
		this.Group=v;
	}
	this.setGroup=setGroup;

	this.XnatFielddefinitiongroupFieldId=null;


	function getXnatFielddefinitiongroupFieldId() {
		return this.XnatFielddefinitiongroupFieldId;
	}
	this.getXnatFielddefinitiongroupFieldId=getXnatFielddefinitiongroupFieldId;


	function setXnatFielddefinitiongroupFieldId(v){
		this.XnatFielddefinitiongroupFieldId=v;
	}
	this.setXnatFielddefinitiongroupFieldId=setXnatFielddefinitiongroupFieldId;

	this.fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id_fk=null;


	this.getfields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id=function() {
		return this.fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id_fk;
	}


	this.setfields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id=function(v){
		this.fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="possibleValues/possibleValue"){
				return this.Possiblevalues_possiblevalue ;
			} else 
			if(xmlPath.startsWith("possibleValues/possibleValue")){
				xmlPath=xmlPath.substring(28);
				if(xmlPath=="")return this.Possiblevalues_possiblevalue ;
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

				for(var whereCount=0;whereCount<this.Possiblevalues_possiblevalue.length;whereCount++){

					var tempValue=this.Possiblevalues_possiblevalue[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Possiblevalues_possiblevalue[whereCount]);

					}

				}
				}else{

				whereArray=this.Possiblevalues_possiblevalue;
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
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="type"){
				return this.Type ;
			} else 
			if(xmlPath=="datatype"){
				return this.Datatype ;
			} else 
			if(xmlPath=="required"){
				return this.Required ;
			} else 
			if(xmlPath=="sequence"){
				return this.Sequence ;
			} else 
			if(xmlPath=="xmlPath"){
				return this.Xmlpath ;
			} else 
			if(xmlPath=="group"){
				return this.Group ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_fieldDefinitionGroup_field_id"){
				return this.XnatFielddefinitiongroupFieldId ;
			} else 
			if(xmlPath=="fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id"){
				return this.fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="possibleValues/possibleValue"){
				this.Possiblevalues_possiblevalue=value;
			} else 
			if(xmlPath.startsWith("possibleValues/possibleValue")){
				xmlPath=xmlPath.substring(28);
				if(xmlPath=="")return this.Possiblevalues_possiblevalue ;
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

				for(var whereCount=0;whereCount<this.Possiblevalues_possiblevalue.length;whereCount++){

					var tempValue=this.Possiblevalues_possiblevalue[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Possiblevalues_possiblevalue[whereCount]);

					}

				}
				}else{

				whereArray=this.Possiblevalues_possiblevalue;
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
						newChild= instanciateObject("xnat:fieldDefinitionGroup_field_possibleValue");//omUtils.js
					}
					this.addPossiblevalues_possiblevalue(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="type"){
				this.Type=value;
			} else 
			if(xmlPath=="datatype"){
				this.Datatype=value;
			} else 
			if(xmlPath=="required"){
				this.Required=value;
			} else 
			if(xmlPath=="sequence"){
				this.Sequence=value;
			} else 
			if(xmlPath=="xmlPath"){
				this.Xmlpath=value;
			} else 
			if(xmlPath=="group"){
				this.Group=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_fieldDefinitionGroup_field_id"){
				this.XnatFielddefinitiongroupFieldId=value;
			} else 
			if(xmlPath=="fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id"){
				this.fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id_fk=value;
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
		if (xmlPath=="possibleValues/possibleValue"){
			this.addPossiblevalues_possiblevalue(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="possibleValues/possibleValue"){
			return "http://nrg.wustl.edu/xnat:fieldDefinitionGroup_field_possibleValue";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="possibleValues/possibleValue"){
			return "field_NO_CHILD";
		}else if (xmlPath=="name"){
			return "field_LONG_DATA";
		}else if (xmlPath=="type"){
			return "field_data";
		}else if (xmlPath=="datatype"){
			return "field_data";
		}else if (xmlPath=="required"){
			return "field_data";
		}else if (xmlPath=="sequence"){
			return "field_data";
		}else if (xmlPath=="xmlPath"){
			return "field_LONG_DATA";
		}else if (xmlPath=="group"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:fieldDefinitionGroup_field";
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
		xmlTxt+="\n</xnat:fieldDefinitionGroup_field>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatFielddefinitiongroupFieldId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_fieldDefinitionGroup_field_id=\"" + this.XnatFielddefinitiongroupFieldId + "\"";
			}
			if(this.fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id=\"" + this.fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		else attTxt+=" name=\"\"";//REQUIRED FIELD

		if (this.Type!=null)
			attTxt+=" type=\"" +this.Type +"\"";
		//NOT REQUIRED FIELD

		if (this.Datatype!=null)
			attTxt+=" datatype=\"" +this.Datatype +"\"";
		//NOT REQUIRED FIELD

		if (this.Required!=null)
			attTxt+=" required=\"" +this.Required +"\"";
		//NOT REQUIRED FIELD

		if (this.Sequence!=null)
			attTxt+=" sequence=\"" +this.Sequence +"\"";
		//NOT REQUIRED FIELD

		if (this.Xmlpath!=null)
			attTxt+=" xmlPath=\"" +this.Xmlpath +"\"";
		//NOT REQUIRED FIELD

		if (this.Group!=null)
			attTxt+=" group=\"" +this.Group +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Possiblevalues_possiblevalue.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:possibleValues";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Possiblevalues_possiblevalueCOUNT=0;Possiblevalues_possiblevalueCOUNT<this.Possiblevalues_possiblevalue.length;Possiblevalues_possiblevalueCOUNT++){
			xmlTxt +="\n<xnat:possibleValue";
			xmlTxt +=this.Possiblevalues_possiblevalue[Possiblevalues_possiblevalueCOUNT].getXMLAtts();
			if(this.Possiblevalues_possiblevalue[Possiblevalues_possiblevalueCOUNT].xsiType!="xnat:fieldDefinitionGroup_field_possibleValue"){
				xmlTxt+=" xsi:type=\"" + this.Possiblevalues_possiblevalue[Possiblevalues_possiblevalueCOUNT].xsiType + "\"";
			}
			if (this.Possiblevalues_possiblevalue[Possiblevalues_possiblevalueCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Possiblevalues_possiblevalue[Possiblevalues_possiblevalueCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:possibleValue>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:possibleValues>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatFielddefinitiongroupFieldId!=null) return true;
			if (this.fields_field_xnat_fieldDefiniti_xnat_fielddefinitiongroup_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.Possiblevalues_possiblevalue.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
