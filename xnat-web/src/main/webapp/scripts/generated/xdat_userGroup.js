/*
 * web: xdat_userGroup.js
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

function xdat_userGroup(){
this.xsiType="xdat:userGroup";

	this.getSchemaElementName=function(){
		return "userGroup";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:userGroup";
	}
	this.ElementAccess =new Array();

	function getElementAccess() {
		return this.ElementAccess;
	}
	this.getElementAccess=getElementAccess;


	function addElementAccess(v){
		this.ElementAccess.push(v);
	}
	this.addElementAccess=addElementAccess;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.Displayname=null;


	function getDisplayname() {
		return this.Displayname;
	}
	this.getDisplayname=getDisplayname;


	function setDisplayname(v){
		this.Displayname=v;
	}
	this.setDisplayname=setDisplayname;

	this.Tag=null;


	function getTag() {
		return this.Tag;
	}
	this.getTag=getTag;


	function setTag(v){
		this.Tag=v;
	}
	this.setTag=setTag;

	this.XdatUsergroupId=null;


	function getXdatUsergroupId() {
		return this.XdatUsergroupId;
	}
	this.getXdatUsergroupId=getXdatUsergroupId;


	function setXdatUsergroupId(v){
		this.XdatUsergroupId=v;
	}
	this.setXdatUsergroupId=setXdatUsergroupId;

	this.groups_group_xdat_security_xdat_security_id_fk=null;


	this.getgroups_group_xdat_security_xdat_security_id=function() {
		return this.groups_group_xdat_security_xdat_security_id_fk;
	}


	this.setgroups_group_xdat_security_xdat_security_id=function(v){
		this.groups_group_xdat_security_xdat_security_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="element_access"){
				return this.ElementAccess ;
			} else 
			if(xmlPath.startsWith("element_access")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.ElementAccess ;
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

				for(var whereCount=0;whereCount<this.ElementAccess.length;whereCount++){

					var tempValue=this.ElementAccess[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ElementAccess[whereCount]);

					}

				}
				}else{

				whereArray=this.ElementAccess;
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
			if(xmlPath=="displayName"){
				return this.Displayname ;
			} else 
			if(xmlPath=="tag"){
				return this.Tag ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_userGroup_id"){
				return this.XdatUsergroupId ;
			} else 
			if(xmlPath=="groups_group_xdat_security_xdat_security_id"){
				return this.groups_group_xdat_security_xdat_security_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="element_access"){
				this.ElementAccess=value;
			} else 
			if(xmlPath.startsWith("element_access")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.ElementAccess ;
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

				for(var whereCount=0;whereCount<this.ElementAccess.length;whereCount++){

					var tempValue=this.ElementAccess[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ElementAccess[whereCount]);

					}

				}
				}else{

				whereArray=this.ElementAccess;
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
						newChild= instanciateObject("xdat:element_access");//omUtils.js
					}
					this.addElementAccess(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="displayName"){
				this.Displayname=value;
			} else 
			if(xmlPath=="tag"){
				this.Tag=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_userGroup_id"){
				this.XdatUsergroupId=value;
			} else 
			if(xmlPath=="groups_group_xdat_security_xdat_security_id"){
				this.groups_group_xdat_security_xdat_security_id_fk=value;
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
		if (xmlPath=="element_access"){
			this.addElementAccess(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="element_access"){
			return "http://nrg.wustl.edu/security:element_access";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="element_access"){
			return "field_multi_reference";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="displayName"){
			return "field_data";
		}else if (xmlPath=="tag"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:UserGroup";
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
		xmlTxt+="\n</xdat:UserGroup>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatUsergroupId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_userGroup_id=\"" + this.XdatUsergroupId + "\"";
			}
			if(this.groups_group_xdat_security_xdat_security_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="groups_group_xdat_security_xdat_security_id=\"" + this.groups_group_xdat_security_xdat_security_id_fk + "\"";
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

		if (this.Displayname!=null)
			attTxt+=" displayName=\"" +this.Displayname +"\"";
		//NOT REQUIRED FIELD

		if (this.Tag!=null)
			attTxt+=" tag=\"" +this.Tag +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		for(var ElementAccessCOUNT=0;ElementAccessCOUNT<this.ElementAccess.length;ElementAccessCOUNT++){
			xmlTxt +="\n<xdat:element_access";
			xmlTxt +=this.ElementAccess[ElementAccessCOUNT].getXMLAtts();
			if(this.ElementAccess[ElementAccessCOUNT].xsiType!="xdat:element_access"){
				xmlTxt+=" xsi:type=\"" + this.ElementAccess[ElementAccessCOUNT].xsiType + "\"";
			}
			if (this.ElementAccess[ElementAccessCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.ElementAccess[ElementAccessCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:element_access>";
			}else {xmlTxt+="/>";}
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatUsergroupId!=null) return true;
			if (this.groups_group_xdat_security_xdat_security_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.ElementAccess.length>0) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
