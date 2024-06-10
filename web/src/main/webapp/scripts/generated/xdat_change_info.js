/*
 * web: xdat_change_info.js
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

function xdat_change_info(){
this.xsiType="xdat:change_info";

	this.getSchemaElementName=function(){
		return "change_info";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:change_info";
	}
	this.ChangeUser =null;
	function getChangeUser() {
		return this.ChangeUser;
	}
	this.getChangeUser=getChangeUser;


	function setChangeUser(v){
		this.ChangeUser =v;
	}
	this.setChangeUser=setChangeUser;

	this.ChangeUser_ChangeUser=null;


	function getChangeUser_ChangeUser(){
		return this.ChangeUser_ChangeUser;
	}
	this.getChangeUser_ChangeUser=getChangeUser_ChangeUser;


	function setChangeUser_ChangeUser(v){
		this.ChangeUser_ChangeUser=v;
	}
	this.setChangeUser_ChangeUser=setChangeUser_ChangeUser;

	this.Comment=null;


	function getComment() {
		return this.Comment;
	}
	this.getComment=getComment;


	function setComment(v){
		this.Comment=v;
	}
	this.setComment=setComment;

	this.ChangeDate=null;


	function getChangeDate() {
		return this.ChangeDate;
	}
	this.getChangeDate=getChangeDate;


	function setChangeDate(v){
		this.ChangeDate=v;
	}
	this.setChangeDate=setChangeDate;

	this.EventId=null;


	function getEventId() {
		return this.EventId;
	}
	this.getEventId=getEventId;


	function setEventId(v){
		this.EventId=v;
	}
	this.setEventId=setEventId;

	this.XdatChangeInfoId=null;


	function getXdatChangeInfoId() {
		return this.XdatChangeInfoId;
	}
	this.getXdatChangeInfoId=getXdatChangeInfoId;


	function setXdatChangeInfoId(v){
		this.XdatChangeInfoId=v;
	}
	this.setXdatChangeInfoId=setXdatChangeInfoId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="change_user"){
				return this.ChangeUser ;
			} else 
			if(xmlPath.startsWith("change_user")){
				xmlPath=xmlPath.substring(11);
				if(xmlPath=="")return this.ChangeUser ;
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
				if(this.ChangeUser!=undefined)return this.ChangeUser.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="comment"){
				return this.Comment ;
			} else 
			if(xmlPath=="change_date"){
				return this.ChangeDate ;
			} else 
			if(xmlPath=="event_id"){
				return this.EventId ;
			} else 
			if(xmlPath=="xdat_change_info_id"){
				return this.XdatChangeInfoId ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="change_user"){
				this.ChangeUser=value;
			} else 
			if(xmlPath.startsWith("change_user")){
				xmlPath=xmlPath.substring(11);
				if(xmlPath=="")return this.ChangeUser ;
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
				if(this.ChangeUser!=undefined){
					this.ChangeUser.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.ChangeUser= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.ChangeUser= instanciateObject("xdat:user");//omUtils.js
						}
						if(options && options.where)this.ChangeUser.setProperty(options.where.field,options.where.value);
						this.ChangeUser.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="comment"){
				this.Comment=value;
			} else 
			if(xmlPath=="change_date"){
				this.ChangeDate=value;
			} else 
			if(xmlPath=="event_id"){
				this.EventId=value;
			} else 
			if(xmlPath=="xdat_change_info_id"){
				this.XdatChangeInfoId=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
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
		if (xmlPath=="change_user"){
			this.setChangeUser(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="change_user"){
			return "http://nrg.wustl.edu/security:user";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="change_user"){
			return "field_single_reference";
		}else if (xmlPath=="comment"){
			return "field_data";
		}else if (xmlPath=="change_date"){
			return "field_data";
		}else if (xmlPath=="event_id"){
			return "field_data";
		}else if (xmlPath=="xdat_change_info_id"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:change_info";
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
		xmlTxt+="\n</xdat:change_info>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.ChangeDate!=null)
			attTxt+=" change_date=\"" +this.ChangeDate +"\"";
		//NOT REQUIRED FIELD

		if (this.EventId!=null)
			attTxt+=" event_id=\"" +this.EventId +"\"";
		//NOT REQUIRED FIELD

		if (this.XdatChangeInfoId!=null)
			attTxt+=" xdat_change_info_id=\"" +this.XdatChangeInfoId +"\"";
		else attTxt+=" xdat_change_info_id=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.ChangeUser!=null){
			xmlTxt+="\n<xdat:change_user";
			xmlTxt+=this.ChangeUser.getXMLAtts();
			if(this.ChangeUser.xsiType!="xdat:user"){
				xmlTxt+=" xsi:type=\"" + this.ChangeUser.xsiType + "\"";
			}
			if (this.ChangeUser.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.ChangeUser.getXMLBody(preventComments);
				xmlTxt+="</xdat:change_user>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

		if (this.Comment!=null){
			xmlTxt+="\n<xdat:comment";
			xmlTxt+=">";
			xmlTxt+=this.Comment.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:comment>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.ChangeUser!=null){
			if (this.ChangeUser.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if (this.Comment!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
