/*
 * web: xnat_studyProtocol.js
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

function xnat_studyProtocol(){
this.xsiType="xnat:studyProtocol";

	this.getSchemaElementName=function(){
		return "studyProtocol";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:studyProtocol";
	}
this.extension=dynamicJSLoad('xnat_abstractProtocol','generated/xnat_abstractProtocol.js');
	this.Acqconditions_condition =new Array();

	function getAcqconditions_condition() {
		return this.Acqconditions_condition;
	}
	this.getAcqconditions_condition=getAcqconditions_condition;


	function addAcqconditions_condition(v){
		this.Acqconditions_condition.push(v);
	}
	this.addAcqconditions_condition=addAcqconditions_condition;
	this.Subjectgroups_group =new Array();

	function getSubjectgroups_group() {
		return this.Subjectgroups_group;
	}
	this.getSubjectgroups_group=getSubjectgroups_group;


	function addSubjectgroups_group(v){
		this.Subjectgroups_group.push(v);
	}
	this.addSubjectgroups_group=addSubjectgroups_group;
	this.Subjectvariables_variable =new Array();

	function getSubjectvariables_variable() {
		return this.Subjectvariables_variable;
	}
	this.getSubjectvariables_variable=getSubjectvariables_variable;


	function addSubjectvariables_variable(v){
		this.Subjectvariables_variable.push(v);
	}
	this.addSubjectvariables_variable=addSubjectvariables_variable;
	this.Imagesessiontypes_session =new Array();

	function getImagesessiontypes_session() {
		return this.Imagesessiontypes_session;
	}
	this.getImagesessiontypes_session=getImagesessiontypes_session;


	function addImagesessiontypes_session(v){
		this.Imagesessiontypes_session.push(v);
	}
	this.addImagesessiontypes_session=addImagesessiontypes_session;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractProtocol"){
				return this.Abstractprotocol ;
			} else 
			if(xmlPath.startsWith("abstractProtocol")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Abstractprotocol ;
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
				if(this.Abstractprotocol!=undefined)return this.Abstractprotocol.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="acqConditions/condition"){
				return this.Acqconditions_condition ;
			} else 
			if(xmlPath.startsWith("acqConditions/condition")){
				xmlPath=xmlPath.substring(23);
				if(xmlPath=="")return this.Acqconditions_condition ;
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

				for(var whereCount=0;whereCount<this.Acqconditions_condition.length;whereCount++){

					var tempValue=this.Acqconditions_condition[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Acqconditions_condition[whereCount]);

					}

				}
				}else{

				whereArray=this.Acqconditions_condition;
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
			if(xmlPath=="subjectGroups/group"){
				return this.Subjectgroups_group ;
			} else 
			if(xmlPath.startsWith("subjectGroups/group")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Subjectgroups_group ;
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

				for(var whereCount=0;whereCount<this.Subjectgroups_group.length;whereCount++){

					var tempValue=this.Subjectgroups_group[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Subjectgroups_group[whereCount]);

					}

				}
				}else{

				whereArray=this.Subjectgroups_group;
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
			if(xmlPath=="subjectVariables/variable"){
				return this.Subjectvariables_variable ;
			} else 
			if(xmlPath.startsWith("subjectVariables/variable")){
				xmlPath=xmlPath.substring(25);
				if(xmlPath=="")return this.Subjectvariables_variable ;
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

				for(var whereCount=0;whereCount<this.Subjectvariables_variable.length;whereCount++){

					var tempValue=this.Subjectvariables_variable[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Subjectvariables_variable[whereCount]);

					}

				}
				}else{

				whereArray=this.Subjectvariables_variable;
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
			if(xmlPath=="imageSessionTypes/session"){
				return this.Imagesessiontypes_session ;
			} else 
			if(xmlPath.startsWith("imageSessionTypes/session")){
				xmlPath=xmlPath.substring(25);
				if(xmlPath=="")return this.Imagesessiontypes_session ;
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

				for(var whereCount=0;whereCount<this.Imagesessiontypes_session.length;whereCount++){

					var tempValue=this.Imagesessiontypes_session[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Imagesessiontypes_session[whereCount]);

					}

				}
				}else{

				whereArray=this.Imagesessiontypes_session;
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
			if(xmlPath=="abstractProtocol"){
				this.Abstractprotocol=value;
			} else 
			if(xmlPath.startsWith("abstractProtocol")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Abstractprotocol ;
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
				if(this.Abstractprotocol!=undefined){
					this.Abstractprotocol.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Abstractprotocol= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Abstractprotocol= instanciateObject("xnat:abstractProtocol");//omUtils.js
						}
						if(options && options.where)this.Abstractprotocol.setProperty(options.where.field,options.where.value);
						this.Abstractprotocol.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="acqConditions/condition"){
				this.Acqconditions_condition=value;
			} else 
			if(xmlPath.startsWith("acqConditions/condition")){
				xmlPath=xmlPath.substring(23);
				if(xmlPath=="")return this.Acqconditions_condition ;
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

				for(var whereCount=0;whereCount<this.Acqconditions_condition.length;whereCount++){

					var tempValue=this.Acqconditions_condition[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Acqconditions_condition[whereCount]);

					}

				}
				}else{

				whereArray=this.Acqconditions_condition;
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
						newChild= instanciateObject("xnat:studyProtocol_condition");//omUtils.js
					}
					this.addAcqconditions_condition(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="subjectGroups/group"){
				this.Subjectgroups_group=value;
			} else 
			if(xmlPath.startsWith("subjectGroups/group")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Subjectgroups_group ;
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

				for(var whereCount=0;whereCount<this.Subjectgroups_group.length;whereCount++){

					var tempValue=this.Subjectgroups_group[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Subjectgroups_group[whereCount]);

					}

				}
				}else{

				whereArray=this.Subjectgroups_group;
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
						newChild= instanciateObject("xnat:studyProtocol_group");//omUtils.js
					}
					this.addSubjectgroups_group(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="subjectVariables/variable"){
				this.Subjectvariables_variable=value;
			} else 
			if(xmlPath.startsWith("subjectVariables/variable")){
				xmlPath=xmlPath.substring(25);
				if(xmlPath=="")return this.Subjectvariables_variable ;
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

				for(var whereCount=0;whereCount<this.Subjectvariables_variable.length;whereCount++){

					var tempValue=this.Subjectvariables_variable[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Subjectvariables_variable[whereCount]);

					}

				}
				}else{

				whereArray=this.Subjectvariables_variable;
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
						newChild= instanciateObject("xnat:studyProtocol_variable");//omUtils.js
					}
					this.addSubjectvariables_variable(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="imageSessionTypes/session"){
				this.Imagesessiontypes_session=value;
			} else 
			if(xmlPath.startsWith("imageSessionTypes/session")){
				xmlPath=xmlPath.substring(25);
				if(xmlPath=="")return this.Imagesessiontypes_session ;
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

				for(var whereCount=0;whereCount<this.Imagesessiontypes_session.length;whereCount++){

					var tempValue=this.Imagesessiontypes_session[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Imagesessiontypes_session[whereCount]);

					}

				}
				}else{

				whereArray=this.Imagesessiontypes_session;
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
						newChild= instanciateObject("xnat:studyProtocol_session");//omUtils.js
					}
					this.addImagesessiontypes_session(newChild);
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
		if (xmlPath=="acqConditions/condition"){
			this.addAcqconditions_condition(v);
		}else if (xmlPath=="subjectGroups/group"){
			this.addSubjectgroups_group(v);
		}else if (xmlPath=="subjectVariables/variable"){
			this.addSubjectvariables_variable(v);
		}else if (xmlPath=="imageSessionTypes/session"){
			this.addImagesessiontypes_session(v);
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
		if (xmlPath=="acqConditions/condition"){
			return "http://nrg.wustl.edu/xnat:studyProtocol_condition";
		}else if (xmlPath=="subjectGroups/group"){
			return "http://nrg.wustl.edu/xnat:studyProtocol_group";
		}else if (xmlPath=="subjectVariables/variable"){
			return "http://nrg.wustl.edu/xnat:studyProtocol_variable";
		}else if (xmlPath=="imageSessionTypes/session"){
			return "http://nrg.wustl.edu/xnat:studyProtocol_session";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="acqConditions/condition"){
			return "field_multi_reference";
		}else if (xmlPath=="subjectGroups/group"){
			return "field_multi_reference";
		}else if (xmlPath=="subjectVariables/variable"){
			return "field_multi_reference";
		}else if (xmlPath=="imageSessionTypes/session"){
			return "field_multi_reference";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:studyProtocol";
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
		xmlTxt+="\n</xnat:studyProtocol>";
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
			child0+=this.Acqconditions_condition.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:acqConditions";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Acqconditions_conditionCOUNT=0;Acqconditions_conditionCOUNT<this.Acqconditions_condition.length;Acqconditions_conditionCOUNT++){
			xmlTxt +="\n<xnat:condition";
			xmlTxt +=this.Acqconditions_condition[Acqconditions_conditionCOUNT].getXMLAtts();
			if(this.Acqconditions_condition[Acqconditions_conditionCOUNT].xsiType!="xnat:studyProtocol_condition"){
				xmlTxt+=" xsi:type=\"" + this.Acqconditions_condition[Acqconditions_conditionCOUNT].xsiType + "\"";
			}
			if (this.Acqconditions_condition[Acqconditions_conditionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Acqconditions_condition[Acqconditions_conditionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:condition>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:acqConditions>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.Subjectgroups_group.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:subjectGroups";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Subjectgroups_groupCOUNT=0;Subjectgroups_groupCOUNT<this.Subjectgroups_group.length;Subjectgroups_groupCOUNT++){
			xmlTxt +="\n<xnat:group";
			xmlTxt +=this.Subjectgroups_group[Subjectgroups_groupCOUNT].getXMLAtts();
			if(this.Subjectgroups_group[Subjectgroups_groupCOUNT].xsiType!="xnat:studyProtocol_group"){
				xmlTxt+=" xsi:type=\"" + this.Subjectgroups_group[Subjectgroups_groupCOUNT].xsiType + "\"";
			}
			if (this.Subjectgroups_group[Subjectgroups_groupCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Subjectgroups_group[Subjectgroups_groupCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:group>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:subjectGroups>";
			}
			}

			var child2=0;
			var att2=0;
			child2+=this.Subjectvariables_variable.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xnat:subjectVariables";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Subjectvariables_variableCOUNT=0;Subjectvariables_variableCOUNT<this.Subjectvariables_variable.length;Subjectvariables_variableCOUNT++){
			xmlTxt +="\n<xnat:variable";
			xmlTxt +=this.Subjectvariables_variable[Subjectvariables_variableCOUNT].getXMLAtts();
			if(this.Subjectvariables_variable[Subjectvariables_variableCOUNT].xsiType!="xnat:studyProtocol_variable"){
				xmlTxt+=" xsi:type=\"" + this.Subjectvariables_variable[Subjectvariables_variableCOUNT].xsiType + "\"";
			}
			if (this.Subjectvariables_variable[Subjectvariables_variableCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Subjectvariables_variable[Subjectvariables_variableCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:variable>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:subjectVariables>";
			}
			}

			var child3=0;
			var att3=0;
			child3+=this.Imagesessiontypes_session.length;
			if(child3>0 || att3>0){
				xmlTxt+="\n<xnat:imageSessionTypes";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Imagesessiontypes_sessionCOUNT=0;Imagesessiontypes_sessionCOUNT<this.Imagesessiontypes_session.length;Imagesessiontypes_sessionCOUNT++){
			xmlTxt +="\n<xnat:session";
			xmlTxt +=this.Imagesessiontypes_session[Imagesessiontypes_sessionCOUNT].getXMLAtts();
			if(this.Imagesessiontypes_session[Imagesessiontypes_sessionCOUNT].xsiType!="xnat:studyProtocol_session"){
				xmlTxt+=" xsi:type=\"" + this.Imagesessiontypes_session[Imagesessiontypes_sessionCOUNT].xsiType + "\"";
			}
			if (this.Imagesessiontypes_session[Imagesessiontypes_sessionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Imagesessiontypes_session[Imagesessiontypes_sessionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:session>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:imageSessionTypes>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Acqconditions_condition.length>0)return true;
			if(this.Subjectgroups_group.length>0)return true;
			if(this.Subjectvariables_variable.length>0)return true;
			if(this.Imagesessiontypes_session.length>0)return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
