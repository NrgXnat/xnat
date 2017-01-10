/*
 * web: arc_ArchiveSpecification_notification_type.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

/*
 * GENERATED FILE
 * Created on Fri Feb 12 15:43:50 CST 2016
 *
 */

/**
 * @author XDAT
 *
 */

function arc_ArchiveSpecification_notification_type(){
this.xsiType="arc:ArchiveSpecification_notification_type";

	this.getSchemaElementName=function(){
		return "ArchiveSpecification_notification_type";
	}

	this.getFullSchemaElementName=function(){
		return "arc:ArchiveSpecification_notification_type";
	}

	this.NotificationType=null;


	function getNotificationType() {
		return this.NotificationType;
	}
	this.getNotificationType=getNotificationType;


	function setNotificationType(v){
		this.NotificationType=v;
	}
	this.setNotificationType=setNotificationType;

	this.EmailAddresses=null;


	function getEmailAddresses() {
		return this.EmailAddresses;
	}
	this.getEmailAddresses=getEmailAddresses;


	function setEmailAddresses(v){
		this.EmailAddresses=v;
	}
	this.setEmailAddresses=setEmailAddresses;

	this.ArcArchivespecificationNotificationTypeId=null;


	function getArcArchivespecificationNotificationTypeId() {
		return this.ArcArchivespecificationNotificationTypeId;
	}
	this.getArcArchivespecificationNotificationTypeId=getArcArchivespecificationNotificationTypeId;


	function setArcArchivespecificationNotificationTypeId(v){
		this.ArcArchivespecificationNotificationTypeId=v;
	}
	this.setArcArchivespecificationNotificationTypeId=setArcArchivespecificationNotificationTypeId;

	this.notification_types_notification_arc_archivespecification_id_fk=null;


	this.getnotification_types_notification_arc_archivespecification_id=function() {
		return this.notification_types_notification_arc_archivespecification_id_fk;
	}


	this.setnotification_types_notification_arc_archivespecification_id=function(v){
		this.notification_types_notification_arc_archivespecification_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="notification_type"){
				return this.NotificationType ;
			} else 
			if(xmlPath=="email_addresses"){
				return this.EmailAddresses ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="arc_ArchiveSpecification_notification_type_id"){
				return this.ArcArchivespecificationNotificationTypeId ;
			} else 
			if(xmlPath=="notification_types_notification_arc_archivespecification_id"){
				return this.notification_types_notification_arc_archivespecification_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="notification_type"){
				this.NotificationType=value;
			} else 
			if(xmlPath=="email_addresses"){
				this.EmailAddresses=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="arc_ArchiveSpecification_notification_type_id"){
				this.ArcArchivespecificationNotificationTypeId=value;
			} else 
			if(xmlPath=="notification_types_notification_arc_archivespecification_id"){
				this.notification_types_notification_arc_archivespecification_id_fk=value;
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
		if (xmlPath=="notification_type"){
			return "field_data";
		}else if (xmlPath=="email_addresses"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<arc:ArchiveSpecification_notification_type";
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
		xmlTxt+="\n</arc:ArchiveSpecification_notification_type>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ArcArchivespecificationNotificationTypeId!=null){
				if(hiddenCount++>0)str+=",";
				str+="arc_ArchiveSpecification_notification_type_id=\"" + this.ArcArchivespecificationNotificationTypeId + "\"";
			}
			if(this.notification_types_notification_arc_archivespecification_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="notification_types_notification_arc_archivespecification_id=\"" + this.notification_types_notification_arc_archivespecification_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.EmailAddresses!=null)
			attTxt+=" email_addresses=\"" +this.EmailAddresses +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.NotificationType!=null){
			xmlTxt+=this.NotificationType.replace(/>/g,"&gt;").replace(/</g,"&lt;");
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ArcArchivespecificationNotificationTypeId!=null) return true;
			if (this.notification_types_notification_arc_archivespecification_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.NotificationType!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
