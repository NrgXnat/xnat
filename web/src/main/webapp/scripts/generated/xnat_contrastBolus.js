/*
 * web: xnat_contrastBolus.js
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

function xnat_contrastBolus(){
this.xsiType="xnat:contrastBolus";

	this.getSchemaElementName=function(){
		return "contrastBolus";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:contrastBolus";
	}

	this.Agent=null;


	function getAgent() {
		return this.Agent;
	}
	this.getAgent=getAgent;


	function setAgent(v){
		this.Agent=v;
	}
	this.setAgent=setAgent;

	this.Route=null;


	function getRoute() {
		return this.Route;
	}
	this.getRoute=getRoute;


	function setRoute(v){
		this.Route=v;
	}
	this.setRoute=setRoute;

	this.Volume=null;


	function getVolume() {
		return this.Volume;
	}
	this.getVolume=getVolume;


	function setVolume(v){
		this.Volume=v;
	}
	this.setVolume=setVolume;

	this.Totaldose=null;


	function getTotaldose() {
		return this.Totaldose;
	}
	this.getTotaldose=getTotaldose;


	function setTotaldose(v){
		this.Totaldose=v;
	}
	this.setTotaldose=setTotaldose;

	this.Flowrate=null;


	function getFlowrate() {
		return this.Flowrate;
	}
	this.getFlowrate=getFlowrate;


	function setFlowrate(v){
		this.Flowrate=v;
	}
	this.setFlowrate=setFlowrate;

	this.Flowduration=null;


	function getFlowduration() {
		return this.Flowduration;
	}
	this.getFlowduration=getFlowduration;


	function setFlowduration(v){
		this.Flowduration=v;
	}
	this.setFlowduration=setFlowduration;

	this.Activeingredient=null;


	function getActiveingredient() {
		return this.Activeingredient;
	}
	this.getActiveingredient=getActiveingredient;


	function setActiveingredient(v){
		this.Activeingredient=v;
	}
	this.setActiveingredient=setActiveingredient;

	this.Concentration=null;


	function getConcentration() {
		return this.Concentration;
	}
	this.getConcentration=getConcentration;


	function setConcentration(v){
		this.Concentration=v;
	}
	this.setConcentration=setConcentration;

	this.XnatContrastbolusId=null;


	function getXnatContrastbolusId() {
		return this.XnatContrastbolusId;
	}
	this.getXnatContrastbolusId=getXnatContrastbolusId;


	function setXnatContrastbolusId(v){
		this.XnatContrastbolusId=v;
	}
	this.setXnatContrastbolusId=setXnatContrastbolusId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="agent"){
				return this.Agent ;
			} else 
			if(xmlPath=="route"){
				return this.Route ;
			} else 
			if(xmlPath=="volume"){
				return this.Volume ;
			} else 
			if(xmlPath=="totalDose"){
				return this.Totaldose ;
			} else 
			if(xmlPath=="flowRate"){
				return this.Flowrate ;
			} else 
			if(xmlPath=="flowDuration"){
				return this.Flowduration ;
			} else 
			if(xmlPath=="activeIngredient"){
				return this.Activeingredient ;
			} else 
			if(xmlPath=="concentration"){
				return this.Concentration ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_contrastBolus_id"){
				return this.XnatContrastbolusId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="agent"){
				this.Agent=value;
			} else 
			if(xmlPath=="route"){
				this.Route=value;
			} else 
			if(xmlPath=="volume"){
				this.Volume=value;
			} else 
			if(xmlPath=="totalDose"){
				this.Totaldose=value;
			} else 
			if(xmlPath=="flowRate"){
				this.Flowrate=value;
			} else 
			if(xmlPath=="flowDuration"){
				this.Flowduration=value;
			} else 
			if(xmlPath=="activeIngredient"){
				this.Activeingredient=value;
			} else 
			if(xmlPath=="concentration"){
				this.Concentration=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_contrastBolus_id"){
				this.XnatContrastbolusId=value;
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
		if (xmlPath=="agent"){
			return "field_data";
		}else if (xmlPath=="route"){
			return "field_data";
		}else if (xmlPath=="volume"){
			return "field_data";
		}else if (xmlPath=="totalDose"){
			return "field_data";
		}else if (xmlPath=="flowRate"){
			return "field_data";
		}else if (xmlPath=="flowDuration"){
			return "field_data";
		}else if (xmlPath=="activeIngredient"){
			return "field_data";
		}else if (xmlPath=="concentration"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:contrastBolus";
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
		xmlTxt+="\n</xnat:contrastBolus>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatContrastbolusId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_contrastBolus_id=\"" + this.XnatContrastbolusId + "\"";
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
		if (this.Agent!=null){
			xmlTxt+="\n<xnat:agent";
			xmlTxt+=">";
			xmlTxt+=this.Agent.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:agent>";
		}
		if (this.Route!=null){
			xmlTxt+="\n<xnat:route";
			xmlTxt+=">";
			xmlTxt+=this.Route.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:route>";
		}
		if (this.Volume!=null){
			xmlTxt+="\n<xnat:volume";
			xmlTxt+=">";
			xmlTxt+=this.Volume;
			xmlTxt+="</xnat:volume>";
		}
		if (this.Totaldose!=null){
			xmlTxt+="\n<xnat:totalDose";
			xmlTxt+=">";
			xmlTxt+=this.Totaldose;
			xmlTxt+="</xnat:totalDose>";
		}
		if (this.Flowrate!=null){
			xmlTxt+="\n<xnat:flowRate";
			xmlTxt+=">";
			xmlTxt+=this.Flowrate;
			xmlTxt+="</xnat:flowRate>";
		}
		if (this.Flowduration!=null){
			xmlTxt+="\n<xnat:flowDuration";
			xmlTxt+=">";
			xmlTxt+=this.Flowduration;
			xmlTxt+="</xnat:flowDuration>";
		}
		if (this.Activeingredient!=null){
			xmlTxt+="\n<xnat:activeIngredient";
			xmlTxt+=">";
			xmlTxt+=this.Activeingredient.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:activeIngredient>";
		}
		if (this.Concentration!=null){
			xmlTxt+="\n<xnat:concentration";
			xmlTxt+=">";
			xmlTxt+=this.Concentration;
			xmlTxt+="</xnat:concentration>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatContrastbolusId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Agent!=null) return true;
		if (this.Route!=null) return true;
		if (this.Volume!=null) return true;
		if (this.Totaldose!=null) return true;
		if (this.Flowrate!=null) return true;
		if (this.Flowduration!=null) return true;
		if (this.Activeingredient!=null) return true;
		if (this.Concentration!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
