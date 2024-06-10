/*
 * web: xdat_user_groupID.js
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

function xdat_user_groupID(){
this.xsiType="xdat:user_groupID";

	this.getSchemaElementName=function(){
		return "user_groupID";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:user_groupID";
	}

	this.Groupid=null;


	function getGroupid() {
		return this.Groupid;
	}
	this.getGroupid=getGroupid;


	function setGroupid(v){
		this.Groupid=v;
	}
	this.setGroupid=setGroupid;

	this.XdatUserGroupidId=null;


	function getXdatUserGroupidId() {
		return this.XdatUserGroupidId;
	}
	this.getXdatUserGroupidId=getXdatUserGroupidId;


	function setXdatUserGroupidId(v){
		this.XdatUserGroupidId=v;
	}
	this.setXdatUserGroupidId=setXdatUserGroupidId;

	this.groups_groupid_xdat_user_xdat_user_id_fk=null;


	this.getgroups_groupid_xdat_user_xdat_user_id=function() {
		return this.groups_groupid_xdat_user_xdat_user_id_fk;
	}


	this.setgroups_groupid_xdat_user_xdat_user_id=function(v){
		this.groups_groupid_xdat_user_xdat_user_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="groupID"){
				return this.Groupid ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_user_groupID_id"){
				return this.XdatUserGroupidId ;
			} else 
			if(xmlPath=="groups_groupid_xdat_user_xdat_user_id"){
				return this.groups_groupid_xdat_user_xdat_user_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="groupID"){
				this.Groupid=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_user_groupID_id"){
				this.XdatUserGroupidId=value;
			} else 
			if(xmlPath=="groups_groupid_xdat_user_xdat_user_id"){
				this.groups_groupid_xdat_user_xdat_user_id_fk=value;
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
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="groupID"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:user_groupID";
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
		xmlTxt+="\n</xdat:user_groupID>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatUserGroupidId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_user_groupID_id=\"" + this.XdatUserGroupidId + "\"";
			}
			if(this.groups_groupid_xdat_user_xdat_user_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="groups_groupid_xdat_user_xdat_user_id=\"" + this.groups_groupid_xdat_user_xdat_user_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Groupid!=null){
			xmlTxt+="\n<xdat:groupID";
			xmlTxt+=">";
			xmlTxt+=this.Groupid.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:groupID>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatUserGroupidId!=null) return true;
			if (this.groups_groupid_xdat_user_xdat_user_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Groupid!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
