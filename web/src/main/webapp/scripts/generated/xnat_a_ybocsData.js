/*
 * web: xnat_a_ybocsData.js
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

function xnat_a_ybocsData(){
this.xsiType="xnat_a:ybocsData";

	this.getSchemaElementName=function(){
		return "ybocsData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat_a:ybocsData";
	}
this.extension=dynamicJSLoad('xnat_subjectAssessorData','generated/xnat_subjectAssessorData.js');

	this.Currentorworstever=null;


	function getCurrentorworstever() {
		return this.Currentorworstever;
	}
	this.getCurrentorworstever=getCurrentorworstever;


	function setCurrentorworstever(v){
		this.Currentorworstever=v;
	}
	this.setCurrentorworstever=setCurrentorworstever;

	this.Timeoccupiedwiththoughts=null;


	function getTimeoccupiedwiththoughts() {
		return this.Timeoccupiedwiththoughts;
	}
	this.getTimeoccupiedwiththoughts=getTimeoccupiedwiththoughts;


	function setTimeoccupiedwiththoughts(v){
		this.Timeoccupiedwiththoughts=v;
	}
	this.setTimeoccupiedwiththoughts=setTimeoccupiedwiththoughts;

	this.Thoughtsinterferefunctioning=null;


	function getThoughtsinterferefunctioning() {
		return this.Thoughtsinterferefunctioning;
	}
	this.getThoughtsinterferefunctioning=getThoughtsinterferefunctioning;


	function setThoughtsinterferefunctioning(v){
		this.Thoughtsinterferefunctioning=v;
	}
	this.setThoughtsinterferefunctioning=setThoughtsinterferefunctioning;

	this.Distresscaused=null;


	function getDistresscaused() {
		return this.Distresscaused;
	}
	this.getDistresscaused=getDistresscaused;


	function setDistresscaused(v){
		this.Distresscaused=v;
	}
	this.setDistresscaused=setDistresscaused;

	this.Efforttoresistthoughts=null;


	function getEfforttoresistthoughts() {
		return this.Efforttoresistthoughts;
	}
	this.getEfforttoresistthoughts=getEfforttoresistthoughts;


	function setEfforttoresistthoughts(v){
		this.Efforttoresistthoughts=v;
	}
	this.setEfforttoresistthoughts=setEfforttoresistthoughts;

	this.Controloverthoughts=null;


	function getControloverthoughts() {
		return this.Controloverthoughts;
	}
	this.getControloverthoughts=getControloverthoughts;


	function setControloverthoughts(v){
		this.Controloverthoughts=v;
	}
	this.setControloverthoughts=setControloverthoughts;

	this.Timeperforming=null;


	function getTimeperforming() {
		return this.Timeperforming;
	}
	this.getTimeperforming=getTimeperforming;


	function setTimeperforming(v){
		this.Timeperforming=v;
	}
	this.setTimeperforming=setTimeperforming;

	this.Behaviorsinterferefunctioning=null;


	function getBehaviorsinterferefunctioning() {
		return this.Behaviorsinterferefunctioning;
	}
	this.getBehaviorsinterferefunctioning=getBehaviorsinterferefunctioning;


	function setBehaviorsinterferefunctioning(v){
		this.Behaviorsinterferefunctioning=v;
	}
	this.setBehaviorsinterferefunctioning=setBehaviorsinterferefunctioning;

	this.Feelingifprevented=null;


	function getFeelingifprevented() {
		return this.Feelingifprevented;
	}
	this.getFeelingifprevented=getFeelingifprevented;


	function setFeelingifprevented(v){
		this.Feelingifprevented=v;
	}
	this.setFeelingifprevented=setFeelingifprevented;

	this.Efforttoresistbehaviors=null;


	function getEfforttoresistbehaviors() {
		return this.Efforttoresistbehaviors;
	}
	this.getEfforttoresistbehaviors=getEfforttoresistbehaviors;


	function setEfforttoresistbehaviors(v){
		this.Efforttoresistbehaviors=v;
	}
	this.setEfforttoresistbehaviors=setEfforttoresistbehaviors;

	this.Behaviordrivestrength=null;


	function getBehaviordrivestrength() {
		return this.Behaviordrivestrength;
	}
	this.getBehaviordrivestrength=getBehaviordrivestrength;


	function setBehaviordrivestrength(v){
		this.Behaviordrivestrength=v;
	}
	this.setBehaviordrivestrength=setBehaviordrivestrength;

	this.Untiljustright=null;


	function getUntiljustright() {
		return this.Untiljustright;
	}
	this.getUntiljustright=getUntiljustright;


	function setUntiljustright(v){
		this.Untiljustright=v;
	}
	this.setUntiljustright=setUntiljustright;


	this.isUntiljustright=function(defaultValue) {
		if(this.Untiljustright==null)return defaultValue;
		if(this.Untiljustright=="1" || this.Untiljustright==true)return true;
		return false;
	}

	this.Untiljustrightawareness=null;


	function getUntiljustrightawareness() {
		return this.Untiljustrightawareness;
	}
	this.getUntiljustrightawareness=getUntiljustrightawareness;


	function setUntiljustrightawareness(v){
		this.Untiljustrightawareness=v;
	}
	this.setUntiljustrightawareness=setUntiljustrightawareness;

	this.Untiljustrightperceptions=null;


	function getUntiljustrightperceptions() {
		return this.Untiljustrightperceptions;
	}
	this.getUntiljustrightperceptions=getUntiljustrightperceptions;


	function setUntiljustrightperceptions(v){
		this.Untiljustrightperceptions=v;
	}
	this.setUntiljustrightperceptions=setUntiljustrightperceptions;

	this.Whenstartuntiljustright=null;


	function getWhenstartuntiljustright() {
		return this.Whenstartuntiljustright;
	}
	this.getWhenstartuntiljustright=getWhenstartuntiljustright;


	function setWhenstartuntiljustright(v){
		this.Whenstartuntiljustright=v;
	}
	this.setWhenstartuntiljustright=setWhenstartuntiljustright;

	this.Frequencyuntiljustright=null;


	function getFrequencyuntiljustright() {
		return this.Frequencyuntiljustright;
	}
	this.getFrequencyuntiljustright=getFrequencyuntiljustright;


	function setFrequencyuntiljustright(v){
		this.Frequencyuntiljustright=v;
	}
	this.setFrequencyuntiljustright=setFrequencyuntiljustright;

	this.Firstuntiljustrightage=null;


	function getFirstuntiljustrightage() {
		return this.Firstuntiljustrightage;
	}
	this.getFirstuntiljustrightage=getFirstuntiljustrightage;


	function setFirstuntiljustrightage(v){
		this.Firstuntiljustrightage=v;
	}
	this.setFirstuntiljustrightage=setFirstuntiljustrightage;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="subjectAssessorData"){
				return this.Subjectassessordata ;
			} else 
			if(xmlPath.startsWith("subjectAssessorData")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Subjectassessordata ;
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
				if(this.Subjectassessordata!=undefined)return this.Subjectassessordata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="currentOrWorstEver"){
				return this.Currentorworstever ;
			} else 
			if(xmlPath=="timeOccupiedWithThoughts"){
				return this.Timeoccupiedwiththoughts ;
			} else 
			if(xmlPath=="thoughtsInterfereFunctioning"){
				return this.Thoughtsinterferefunctioning ;
			} else 
			if(xmlPath=="distressCaused"){
				return this.Distresscaused ;
			} else 
			if(xmlPath=="effortToResistThoughts"){
				return this.Efforttoresistthoughts ;
			} else 
			if(xmlPath=="controlOverThoughts"){
				return this.Controloverthoughts ;
			} else 
			if(xmlPath=="timePerforming"){
				return this.Timeperforming ;
			} else 
			if(xmlPath=="behaviorsInterfereFunctioning"){
				return this.Behaviorsinterferefunctioning ;
			} else 
			if(xmlPath=="feelingIfPrevented"){
				return this.Feelingifprevented ;
			} else 
			if(xmlPath=="effortToResistBehaviors"){
				return this.Efforttoresistbehaviors ;
			} else 
			if(xmlPath=="behaviorDriveStrength"){
				return this.Behaviordrivestrength ;
			} else 
			if(xmlPath=="untilJustRight"){
				return this.Untiljustright ;
			} else 
			if(xmlPath=="untilJustRightAwareness"){
				return this.Untiljustrightawareness ;
			} else 
			if(xmlPath=="untilJustRightPerceptions"){
				return this.Untiljustrightperceptions ;
			} else 
			if(xmlPath=="whenStartUntilJustRight"){
				return this.Whenstartuntiljustright ;
			} else 
			if(xmlPath=="frequencyUntilJustRight"){
				return this.Frequencyuntiljustright ;
			} else 
			if(xmlPath=="firstUntilJustRightAge"){
				return this.Firstuntiljustrightage ;
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
			if(xmlPath=="subjectAssessorData"){
				this.Subjectassessordata=value;
			} else 
			if(xmlPath.startsWith("subjectAssessorData")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Subjectassessordata ;
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
				if(this.Subjectassessordata!=undefined){
					this.Subjectassessordata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Subjectassessordata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Subjectassessordata= instanciateObject("xnat:subjectAssessorData");//omUtils.js
						}
						if(options && options.where)this.Subjectassessordata.setProperty(options.where.field,options.where.value);
						this.Subjectassessordata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="currentOrWorstEver"){
				this.Currentorworstever=value;
			} else 
			if(xmlPath=="timeOccupiedWithThoughts"){
				this.Timeoccupiedwiththoughts=value;
			} else 
			if(xmlPath=="thoughtsInterfereFunctioning"){
				this.Thoughtsinterferefunctioning=value;
			} else 
			if(xmlPath=="distressCaused"){
				this.Distresscaused=value;
			} else 
			if(xmlPath=="effortToResistThoughts"){
				this.Efforttoresistthoughts=value;
			} else 
			if(xmlPath=="controlOverThoughts"){
				this.Controloverthoughts=value;
			} else 
			if(xmlPath=="timePerforming"){
				this.Timeperforming=value;
			} else 
			if(xmlPath=="behaviorsInterfereFunctioning"){
				this.Behaviorsinterferefunctioning=value;
			} else 
			if(xmlPath=="feelingIfPrevented"){
				this.Feelingifprevented=value;
			} else 
			if(xmlPath=="effortToResistBehaviors"){
				this.Efforttoresistbehaviors=value;
			} else 
			if(xmlPath=="behaviorDriveStrength"){
				this.Behaviordrivestrength=value;
			} else 
			if(xmlPath=="untilJustRight"){
				this.Untiljustright=value;
			} else 
			if(xmlPath=="untilJustRightAwareness"){
				this.Untiljustrightawareness=value;
			} else 
			if(xmlPath=="untilJustRightPerceptions"){
				this.Untiljustrightperceptions=value;
			} else 
			if(xmlPath=="whenStartUntilJustRight"){
				this.Whenstartuntiljustright=value;
			} else 
			if(xmlPath=="frequencyUntilJustRight"){
				this.Frequencyuntiljustright=value;
			} else 
			if(xmlPath=="firstUntilJustRightAge"){
				this.Firstuntiljustrightage=value;
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
			this.extension.setReferenceField(xmlPath,v);
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
			return this.extension.getReferenceFieldName(xmlPath);
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="currentOrWorstEver"){
			return "field_data";
		}else if (xmlPath=="timeOccupiedWithThoughts"){
			return "field_data";
		}else if (xmlPath=="thoughtsInterfereFunctioning"){
			return "field_data";
		}else if (xmlPath=="distressCaused"){
			return "field_data";
		}else if (xmlPath=="effortToResistThoughts"){
			return "field_data";
		}else if (xmlPath=="controlOverThoughts"){
			return "field_data";
		}else if (xmlPath=="timePerforming"){
			return "field_data";
		}else if (xmlPath=="behaviorsInterfereFunctioning"){
			return "field_data";
		}else if (xmlPath=="feelingIfPrevented"){
			return "field_data";
		}else if (xmlPath=="effortToResistBehaviors"){
			return "field_data";
		}else if (xmlPath=="behaviorDriveStrength"){
			return "field_data";
		}else if (xmlPath=="untilJustRight"){
			return "field_data";
		}else if (xmlPath=="untilJustRightAwareness"){
			return "field_data";
		}else if (xmlPath=="untilJustRightPerceptions"){
			return "field_data";
		}else if (xmlPath=="whenStartUntilJustRight"){
			return "field_data";
		}else if (xmlPath=="frequencyUntilJustRight"){
			return "field_data";
		}else if (xmlPath=="firstUntilJustRightAge"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat_a:YBOCS";
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
		xmlTxt+="\n</xnat_a:YBOCS>";
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
		if (this.Currentorworstever!=null){
			xmlTxt+="\n<xnat_a:currentOrWorstEver";
			xmlTxt+=">";
			xmlTxt+=this.Currentorworstever.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:currentOrWorstEver>";
		}
		if (this.Timeoccupiedwiththoughts!=null){
			xmlTxt+="\n<xnat_a:timeOccupiedWithThoughts";
			xmlTxt+=">";
			xmlTxt+=this.Timeoccupiedwiththoughts;
			xmlTxt+="</xnat_a:timeOccupiedWithThoughts>";
		}
		if (this.Thoughtsinterferefunctioning!=null){
			xmlTxt+="\n<xnat_a:thoughtsInterfereFunctioning";
			xmlTxt+=">";
			xmlTxt+=this.Thoughtsinterferefunctioning;
			xmlTxt+="</xnat_a:thoughtsInterfereFunctioning>";
		}
		if (this.Distresscaused!=null){
			xmlTxt+="\n<xnat_a:distressCaused";
			xmlTxt+=">";
			xmlTxt+=this.Distresscaused;
			xmlTxt+="</xnat_a:distressCaused>";
		}
		if (this.Efforttoresistthoughts!=null){
			xmlTxt+="\n<xnat_a:effortToResistThoughts";
			xmlTxt+=">";
			xmlTxt+=this.Efforttoresistthoughts;
			xmlTxt+="</xnat_a:effortToResistThoughts>";
		}
		if (this.Controloverthoughts!=null){
			xmlTxt+="\n<xnat_a:controlOverThoughts";
			xmlTxt+=">";
			xmlTxt+=this.Controloverthoughts;
			xmlTxt+="</xnat_a:controlOverThoughts>";
		}
		if (this.Timeperforming!=null){
			xmlTxt+="\n<xnat_a:timePerforming";
			xmlTxt+=">";
			xmlTxt+=this.Timeperforming;
			xmlTxt+="</xnat_a:timePerforming>";
		}
		if (this.Behaviorsinterferefunctioning!=null){
			xmlTxt+="\n<xnat_a:behaviorsInterfereFunctioning";
			xmlTxt+=">";
			xmlTxt+=this.Behaviorsinterferefunctioning;
			xmlTxt+="</xnat_a:behaviorsInterfereFunctioning>";
		}
		if (this.Feelingifprevented!=null){
			xmlTxt+="\n<xnat_a:feelingIfPrevented";
			xmlTxt+=">";
			xmlTxt+=this.Feelingifprevented;
			xmlTxt+="</xnat_a:feelingIfPrevented>";
		}
		if (this.Efforttoresistbehaviors!=null){
			xmlTxt+="\n<xnat_a:effortToResistBehaviors";
			xmlTxt+=">";
			xmlTxt+=this.Efforttoresistbehaviors;
			xmlTxt+="</xnat_a:effortToResistBehaviors>";
		}
		if (this.Behaviordrivestrength!=null){
			xmlTxt+="\n<xnat_a:behaviorDriveStrength";
			xmlTxt+=">";
			xmlTxt+=this.Behaviordrivestrength;
			xmlTxt+="</xnat_a:behaviorDriveStrength>";
		}
		if (this.Untiljustright!=null){
			xmlTxt+="\n<xnat_a:untilJustRight";
			xmlTxt+=">";
			xmlTxt+=this.Untiljustright;
			xmlTxt+="</xnat_a:untilJustRight>";
		}
		if (this.Untiljustrightawareness!=null){
			xmlTxt+="\n<xnat_a:untilJustRightAwareness";
			xmlTxt+=">";
			xmlTxt+=this.Untiljustrightawareness.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:untilJustRightAwareness>";
		}
		if (this.Untiljustrightperceptions!=null){
			xmlTxt+="\n<xnat_a:untilJustRightPerceptions";
			xmlTxt+=">";
			xmlTxt+=this.Untiljustrightperceptions.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:untilJustRightPerceptions>";
		}
		if (this.Whenstartuntiljustright!=null){
			xmlTxt+="\n<xnat_a:whenStartUntilJustRight";
			xmlTxt+=">";
			xmlTxt+=this.Whenstartuntiljustright.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:whenStartUntilJustRight>";
		}
		if (this.Frequencyuntiljustright!=null){
			xmlTxt+="\n<xnat_a:frequencyUntilJustRight";
			xmlTxt+=">";
			xmlTxt+=this.Frequencyuntiljustright.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:frequencyUntilJustRight>";
		}
		if (this.Firstuntiljustrightage!=null){
			xmlTxt+="\n<xnat_a:firstUntilJustRightAge";
			xmlTxt+=">";
			xmlTxt+=this.Firstuntiljustrightage;
			xmlTxt+="</xnat_a:firstUntilJustRightAge>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Currentorworstever!=null) return true;
		if (this.Timeoccupiedwiththoughts!=null) return true;
		if (this.Thoughtsinterferefunctioning!=null) return true;
		if (this.Distresscaused!=null) return true;
		if (this.Efforttoresistthoughts!=null) return true;
		if (this.Controloverthoughts!=null) return true;
		if (this.Timeperforming!=null) return true;
		if (this.Behaviorsinterferefunctioning!=null) return true;
		if (this.Feelingifprevented!=null) return true;
		if (this.Efforttoresistbehaviors!=null) return true;
		if (this.Behaviordrivestrength!=null) return true;
		if (this.Untiljustright!=null) return true;
		if (this.Untiljustrightawareness!=null) return true;
		if (this.Untiljustrightperceptions!=null) return true;
		if (this.Whenstartuntiljustright!=null) return true;
		if (this.Frequencyuntiljustright!=null) return true;
		if (this.Firstuntiljustrightage!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
