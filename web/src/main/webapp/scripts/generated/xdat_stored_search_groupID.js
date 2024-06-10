/*
 * web: xdat_stored_search_groupID.js
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

function xdat_stored_search_groupID(){
this.xsiType="xdat:stored_search_groupID";

	this.getSchemaElementName=function(){
		return "stored_search_groupID";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:stored_search_groupID";
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

	this.XdatStoredSearchGroupidId=null;


	function getXdatStoredSearchGroupidId() {
		return this.XdatStoredSearchGroupidId;
	}
	this.getXdatStoredSearchGroupidId=getXdatStoredSearchGroupidId;


	function setXdatStoredSearchGroupidId(v){
		this.XdatStoredSearchGroupidId=v;
	}
	this.setXdatStoredSearchGroupidId=setXdatStoredSearchGroupidId;

	this.allowed_groups_groupid_xdat_sto_id_fk=null;


	this.getallowed_groups_groupid_xdat_sto_id=function() {
		return this.allowed_groups_groupid_xdat_sto_id_fk;
	}


	this.setallowed_groups_groupid_xdat_sto_id=function(v){
		this.allowed_groups_groupid_xdat_sto_id_fk=v;
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
			if(xmlPath=="xdat_stored_search_groupID_id"){
				return this.XdatStoredSearchGroupidId ;
			} else 
			if(xmlPath=="allowed_groups_groupid_xdat_sto_id"){
				return this.allowed_groups_groupid_xdat_sto_id_fk ;
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
			if(xmlPath=="xdat_stored_search_groupID_id"){
				this.XdatStoredSearchGroupidId=value;
			} else 
			if(xmlPath=="allowed_groups_groupid_xdat_sto_id"){
				this.allowed_groups_groupid_xdat_sto_id_fk=value;
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
		xmlTxt+="\n<xdat:stored_search_groupID";
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
		xmlTxt+="\n</xdat:stored_search_groupID>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatStoredSearchGroupidId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_stored_search_groupID_id=\"" + this.XdatStoredSearchGroupidId + "\"";
			}
			if(this.allowed_groups_groupid_xdat_sto_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="allowed_groups_groupid_xdat_sto_id=\"" + this.allowed_groups_groupid_xdat_sto_id_fk + "\"";
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
			if (this.XdatStoredSearchGroupidId!=null) return true;
			if (this.allowed_groups_groupid_xdat_sto_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Groupid!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
