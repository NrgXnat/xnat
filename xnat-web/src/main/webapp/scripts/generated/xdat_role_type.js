/*
 * web: xdat_role_type.js
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

function xdat_role_type(){
this.xsiType="xdat:role_type";

	this.getSchemaElementName=function(){
		return "role_type";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:role_type";
	}
	this.AllowedActions_allowedAction =new Array();

	function getAllowedActions_allowedAction() {
		return this.AllowedActions_allowedAction;
	}
	this.getAllowedActions_allowedAction=getAllowedActions_allowedAction;


	function addAllowedActions_allowedAction(v){
		this.AllowedActions_allowedAction.push(v);
	}
	this.addAllowedActions_allowedAction=addAllowedActions_allowedAction;

	this.RoleName=null;


	function getRoleName() {
		return this.RoleName;
	}
	this.getRoleName=getRoleName;


	function setRoleName(v){
		this.RoleName=v;
	}
	this.setRoleName=setRoleName;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;

	this.Sequence=null;


	function getSequence() {
		return this.Sequence;
	}
	this.getSequence=getSequence;


	function setSequence(v){
		this.Sequence=v;
	}
	this.setSequence=setSequence;

	this.roles_role_xdat_security_xdat_security_id_fk=null;


	this.getroles_role_xdat_security_xdat_security_id=function() {
		return this.roles_role_xdat_security_xdat_security_id_fk;
	}


	this.setroles_role_xdat_security_xdat_security_id=function(v){
		this.roles_role_xdat_security_xdat_security_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="allowed_actions/allowed_action"){
				return this.AllowedActions_allowedAction ;
			} else 
			if(xmlPath.startsWith("allowed_actions/allowed_action")){
				xmlPath=xmlPath.substring(30);
				if(xmlPath=="")return this.AllowedActions_allowedAction ;
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

				for(var whereCount=0;whereCount<this.AllowedActions_allowedAction.length;whereCount++){

					var tempValue=this.AllowedActions_allowedAction[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.AllowedActions_allowedAction[whereCount]);

					}

				}
				}else{

				whereArray=this.AllowedActions_allowedAction;
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
			if(xmlPath=="role_name"){
				return this.RoleName ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="sequence"){
				return this.Sequence ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="roles_role_xdat_security_xdat_security_id"){
				return this.roles_role_xdat_security_xdat_security_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="allowed_actions/allowed_action"){
				this.AllowedActions_allowedAction=value;
			} else 
			if(xmlPath.startsWith("allowed_actions/allowed_action")){
				xmlPath=xmlPath.substring(30);
				if(xmlPath=="")return this.AllowedActions_allowedAction ;
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

				for(var whereCount=0;whereCount<this.AllowedActions_allowedAction.length;whereCount++){

					var tempValue=this.AllowedActions_allowedAction[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.AllowedActions_allowedAction[whereCount]);

					}

				}
				}else{

				whereArray=this.AllowedActions_allowedAction;
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
						newChild= instanciateObject("xdat:action_type");//omUtils.js
					}
					this.addAllowedActions_allowedAction(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="role_name"){
				this.RoleName=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="sequence"){
				this.Sequence=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="roles_role_xdat_security_xdat_security_id"){
				this.roles_role_xdat_security_xdat_security_id_fk=value;
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
		if (xmlPath=="allowed_actions/allowed_action"){
			this.addAllowedActions_allowedAction(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="allowed_actions/allowed_action"){
			return "http://nrg.wustl.edu/security:action_type";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="allowed_actions/allowed_action"){
			return "field_multi_reference";
		}else if (xmlPath=="role_name"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_data";
		}else if (xmlPath=="sequence"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:role_type";
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
		xmlTxt+="\n</xdat:role_type>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.roles_role_xdat_security_xdat_security_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="roles_role_xdat_security_xdat_security_id=\"" + this.roles_role_xdat_security_xdat_security_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.RoleName!=null)
			attTxt+=" role_name=\"" +this.RoleName +"\"";
		else attTxt+=" role_name=\"\"";//REQUIRED FIELD

		if (this.Description!=null)
			attTxt+=" description=\"" +this.Description +"\"";
		//NOT REQUIRED FIELD

		if (this.Sequence!=null)
			attTxt+=" sequence=\"" +this.Sequence +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.AllowedActions_allowedAction.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xdat:allowed_actions";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var AllowedActions_allowedActionCOUNT=0;AllowedActions_allowedActionCOUNT<this.AllowedActions_allowedAction.length;AllowedActions_allowedActionCOUNT++){
			xmlTxt +="\n<xdat:allowed_action";
			xmlTxt +=this.AllowedActions_allowedAction[AllowedActions_allowedActionCOUNT].getXMLAtts();
			if(this.AllowedActions_allowedAction[AllowedActions_allowedActionCOUNT].xsiType!="xdat:action_type"){
				xmlTxt+=" xsi:type=\"" + this.AllowedActions_allowedAction[AllowedActions_allowedActionCOUNT].xsiType + "\"";
			}
			if (this.AllowedActions_allowedAction[AllowedActions_allowedActionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.AllowedActions_allowedAction[AllowedActions_allowedActionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:allowed_action>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:allowed_actions>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.roles_role_xdat_security_xdat_security_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.AllowedActions_allowedAction.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
