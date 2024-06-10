/*
 * web: xdat_element_security_listing_action.js
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

function xdat_element_security_listing_action(){
this.xsiType="xdat:element_security_listing_action";

	this.getSchemaElementName=function(){
		return "element_security_listing_action";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:element_security_listing_action";
	}

	this.ElementActionName=null;


	function getElementActionName() {
		return this.ElementActionName;
	}
	this.getElementActionName=getElementActionName;


	function setElementActionName(v){
		this.ElementActionName=v;
	}
	this.setElementActionName=setElementActionName;

	this.DisplayName=null;


	function getDisplayName() {
		return this.DisplayName;
	}
	this.getDisplayName=getDisplayName;


	function setDisplayName(v){
		this.DisplayName=v;
	}
	this.setDisplayName=setDisplayName;

	this.Sequence=null;


	function getSequence() {
		return this.Sequence;
	}
	this.getSequence=getSequence;


	function setSequence(v){
		this.Sequence=v;
	}
	this.setSequence=setSequence;

	this.Image=null;


	function getImage() {
		return this.Image;
	}
	this.getImage=getImage;


	function setImage(v){
		this.Image=v;
	}
	this.setImage=setImage;

	this.Popup=null;


	function getPopup() {
		return this.Popup;
	}
	this.getPopup=getPopup;


	function setPopup(v){
		this.Popup=v;
	}
	this.setPopup=setPopup;

	this.Secureaccess=null;


	function getSecureaccess() {
		return this.Secureaccess;
	}
	this.getSecureaccess=getSecureaccess;


	function setSecureaccess(v){
		this.Secureaccess=v;
	}
	this.setSecureaccess=setSecureaccess;

	this.Securefeature=null;


	function getSecurefeature() {
		return this.Securefeature;
	}
	this.getSecurefeature=getSecurefeature;


	function setSecurefeature(v){
		this.Securefeature=v;
	}
	this.setSecurefeature=setSecurefeature;

	this.Parameterstring=null;


	function getParameterstring() {
		return this.Parameterstring;
	}
	this.getParameterstring=getParameterstring;


	function setParameterstring(v){
		this.Parameterstring=v;
	}
	this.setParameterstring=setParameterstring;

	this.XdatElementSecurityListingActionId=null;


	function getXdatElementSecurityListingActionId() {
		return this.XdatElementSecurityListingActionId;
	}
	this.getXdatElementSecurityListingActionId=getXdatElementSecurityListingActionId;


	function setXdatElementSecurityListingActionId(v){
		this.XdatElementSecurityListingActionId=v;
	}
	this.setXdatElementSecurityListingActionId=setXdatElementSecurityListingActionId;

	this.listing_actions_listing_action__element_name_fk=null;


	this.getlisting_actions_listing_action__element_name=function() {
		return this.listing_actions_listing_action__element_name_fk;
	}


	this.setlisting_actions_listing_action__element_name=function(v){
		this.listing_actions_listing_action__element_name_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="element_action_name"){
				return this.ElementActionName ;
			} else 
			if(xmlPath=="display_name"){
				return this.DisplayName ;
			} else 
			if(xmlPath=="sequence"){
				return this.Sequence ;
			} else 
			if(xmlPath=="image"){
				return this.Image ;
			} else 
			if(xmlPath=="popup"){
				return this.Popup ;
			} else 
			if(xmlPath=="secureAccess"){
				return this.Secureaccess ;
			} else 
			if(xmlPath=="secureFeature"){
				return this.Securefeature ;
			} else 
			if(xmlPath=="parameterString"){
				return this.Parameterstring ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_element_security_listing_action_id"){
				return this.XdatElementSecurityListingActionId ;
			} else 
			if(xmlPath=="listing_actions_listing_action__element_name"){
				return this.listing_actions_listing_action__element_name_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="element_action_name"){
				this.ElementActionName=value;
			} else 
			if(xmlPath=="display_name"){
				this.DisplayName=value;
			} else 
			if(xmlPath=="sequence"){
				this.Sequence=value;
			} else 
			if(xmlPath=="image"){
				this.Image=value;
			} else 
			if(xmlPath=="popup"){
				this.Popup=value;
			} else 
			if(xmlPath=="secureAccess"){
				this.Secureaccess=value;
			} else 
			if(xmlPath=="secureFeature"){
				this.Securefeature=value;
			} else 
			if(xmlPath=="parameterString"){
				this.Parameterstring=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_element_security_listing_action_id"){
				this.XdatElementSecurityListingActionId=value;
			} else 
			if(xmlPath=="listing_actions_listing_action__element_name"){
				this.listing_actions_listing_action__element_name_fk=value;
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
		if (xmlPath=="element_action_name"){
			return "field_data";
		}else if (xmlPath=="display_name"){
			return "field_data";
		}else if (xmlPath=="sequence"){
			return "field_data";
		}else if (xmlPath=="image"){
			return "field_data";
		}else if (xmlPath=="popup"){
			return "field_data";
		}else if (xmlPath=="secureAccess"){
			return "field_data";
		}else if (xmlPath=="secureFeature"){
			return "field_data";
		}else if (xmlPath=="parameterString"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:element_security_listing_action";
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
		xmlTxt+="\n</xdat:element_security_listing_action>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatElementSecurityListingActionId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_element_security_listing_action_id=\"" + this.XdatElementSecurityListingActionId + "\"";
			}
			if(this.listing_actions_listing_action__element_name_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="listing_actions_listing_action__element_name=\"" + this.listing_actions_listing_action__element_name_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.ElementActionName!=null)
			attTxt+=" element_action_name=\"" +this.ElementActionName +"\"";
		else attTxt+=" element_action_name=\"\"";//REQUIRED FIELD

		if (this.DisplayName!=null)
			attTxt+=" display_name=\"" +this.DisplayName +"\"";
		else attTxt+=" display_name=\"\"";//REQUIRED FIELD

		if (this.Sequence!=null)
			attTxt+=" sequence=\"" +this.Sequence +"\"";
		//NOT REQUIRED FIELD

		if (this.Image!=null)
			attTxt+=" image=\"" +this.Image +"\"";
		//NOT REQUIRED FIELD

		if (this.Popup!=null)
			attTxt+=" popup=\"" +this.Popup +"\"";
		//NOT REQUIRED FIELD

		if (this.Secureaccess!=null)
			attTxt+=" secureAccess=\"" +this.Secureaccess +"\"";
		//NOT REQUIRED FIELD

		if (this.Securefeature!=null)
			attTxt+=" secureFeature=\"" +this.Securefeature +"\"";
		//NOT REQUIRED FIELD

		if (this.Parameterstring!=null)
			attTxt+=" parameterString=\"" +this.Parameterstring +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatElementSecurityListingActionId!=null) return true;
			if (this.listing_actions_listing_action__element_name_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if(this.hasXMLComments())return true;
		return false;
	}
}
