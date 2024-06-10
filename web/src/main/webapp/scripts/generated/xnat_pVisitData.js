/*
 * web: xnat_pVisitData.js
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

function xnat_pVisitData(){
this.xsiType="xnat:pVisitData";

	this.getSchemaElementName=function(){
		return "pVisitData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:pVisitData";
	}
this.extension=dynamicJSLoad('xnat_genericData','generated/xnat_genericData.js');

	this.SubjectId=null;


	function getSubjectId() {
		return this.SubjectId;
	}
	this.getSubjectId=getSubjectId;


	function setSubjectId(v){
		this.SubjectId=v;
	}
	this.setSubjectId=setSubjectId;

	this.VisitType=null;


	function getVisitType() {
		return this.VisitType;
	}
	this.getVisitType=getVisitType;


	function setVisitType(v){
		this.VisitType=v;
	}
	this.setVisitType=setVisitType;

	this.VisitName=null;


	function getVisitName() {
		return this.VisitName;
	}
	this.getVisitName=getVisitName;


	function setVisitName(v){
		this.VisitName=v;
	}
	this.setVisitName=setVisitName;

	this.Notes=null;


	function getNotes() {
		return this.Notes;
	}
	this.getNotes=getNotes;


	function setNotes(v){
		this.Notes=v;
	}
	this.setNotes=setNotes;

	this.StartDate=null;


	function getStartDate() {
		return this.StartDate;
	}
	this.getStartDate=getStartDate;


	function setStartDate(v){
		this.StartDate=v;
	}
	this.setStartDate=setStartDate;

	this.EndDate=null;


	function getEndDate() {
		return this.EndDate;
	}
	this.getEndDate=getEndDate;


	function setEndDate(v){
		this.EndDate=v;
	}
	this.setEndDate=setEndDate;

	this.Closed=null;


	function getClosed() {
		return this.Closed;
	}
	this.getClosed=getClosed;


	function setClosed(v){
		this.Closed=v;
	}
	this.setClosed=setClosed;


	this.isClosed=function(defaultValue) {
		if(this.Closed==null)return defaultValue;
		if(this.Closed=="1" || this.Closed==true)return true;
		return false;
	}

	this.Terminal=null;


	function getTerminal() {
		return this.Terminal;
	}
	this.getTerminal=getTerminal;


	function setTerminal(v){
		this.Terminal=v;
	}
	this.setTerminal=setTerminal;


	this.isTerminal=function(defaultValue) {
		if(this.Terminal==null)return defaultValue;
		if(this.Terminal=="1" || this.Terminal==true)return true;
		return false;
	}

	this.Status=null;


	function getStatus() {
		return this.Status;
	}
	this.getStatus=getStatus;


	function setStatus(v){
		this.Status=v;
	}
	this.setStatus=setStatus;

	this.Protocolversion=null;


	function getProtocolversion() {
		return this.Protocolversion;
	}
	this.getProtocolversion=getProtocolversion;


	function setProtocolversion(v){
		this.Protocolversion=v;
	}
	this.setProtocolversion=setProtocolversion;

	this.Protocolid=null;


	function getProtocolid() {
		return this.Protocolid;
	}
	this.getProtocolid=getProtocolid;


	function setProtocolid(v){
		this.Protocolid=v;
	}
	this.setProtocolid=setProtocolid;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="genericData"){
				return this.Genericdata ;
			} else 
			if(xmlPath.startsWith("genericData")){
				xmlPath=xmlPath.substring(11);
				if(xmlPath=="")return this.Genericdata ;
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
				if(this.Genericdata!=undefined)return this.Genericdata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="subject_ID"){
				return this.SubjectId ;
			} else 
			if(xmlPath=="visit_type"){
				return this.VisitType ;
			} else 
			if(xmlPath=="visit_name"){
				return this.VisitName ;
			} else 
			if(xmlPath=="notes"){
				return this.Notes ;
			} else 
			if(xmlPath=="start_date"){
				return this.StartDate ;
			} else 
			if(xmlPath=="end_date"){
				return this.EndDate ;
			} else 
			if(xmlPath=="closed"){
				return this.Closed ;
			} else 
			if(xmlPath=="terminal"){
				return this.Terminal ;
			} else 
			if(xmlPath=="status"){
				return this.Status ;
			} else 
			if(xmlPath=="protocolVersion"){
				return this.Protocolversion ;
			} else 
			if(xmlPath=="protocolId"){
				return this.Protocolid ;
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
			if(xmlPath=="genericData"){
				this.Genericdata=value;
			} else 
			if(xmlPath.startsWith("genericData")){
				xmlPath=xmlPath.substring(11);
				if(xmlPath=="")return this.Genericdata ;
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
				if(this.Genericdata!=undefined){
					this.Genericdata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Genericdata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Genericdata= instanciateObject("xnat:genericData");//omUtils.js
						}
						if(options && options.where)this.Genericdata.setProperty(options.where.field,options.where.value);
						this.Genericdata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="subject_ID"){
				this.SubjectId=value;
			} else 
			if(xmlPath=="visit_type"){
				this.VisitType=value;
			} else 
			if(xmlPath=="visit_name"){
				this.VisitName=value;
			} else 
			if(xmlPath=="notes"){
				this.Notes=value;
			} else 
			if(xmlPath=="start_date"){
				this.StartDate=value;
			} else 
			if(xmlPath=="end_date"){
				this.EndDate=value;
			} else 
			if(xmlPath=="closed"){
				this.Closed=value;
			} else 
			if(xmlPath=="terminal"){
				this.Terminal=value;
			} else 
			if(xmlPath=="status"){
				this.Status=value;
			} else 
			if(xmlPath=="protocolVersion"){
				this.Protocolversion=value;
			} else 
			if(xmlPath=="protocolId"){
				this.Protocolid=value;
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
		if (xmlPath=="subject_ID"){
			return "field_data";
		}else if (xmlPath=="visit_type"){
			return "field_data";
		}else if (xmlPath=="visit_name"){
			return "field_data";
		}else if (xmlPath=="notes"){
			return "field_LONG_DATA";
		}else if (xmlPath=="start_date"){
			return "field_data";
		}else if (xmlPath=="end_date"){
			return "field_data";
		}else if (xmlPath=="closed"){
			return "field_data";
		}else if (xmlPath=="terminal"){
			return "field_data";
		}else if (xmlPath=="status"){
			return "field_data";
		}else if (xmlPath=="protocolVersion"){
			return "field_data";
		}else if (xmlPath=="protocolId"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:PVisit";
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
		xmlTxt+="\n</xnat:PVisit>";
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
		if (this.Closed!=null)
			attTxt+=" closed=\"" +this.Closed +"\"";
		//NOT REQUIRED FIELD

		if (this.Terminal!=null)
			attTxt+=" terminal=\"" +this.Terminal +"\"";
		//NOT REQUIRED FIELD

		if (this.Status!=null)
			attTxt+=" status=\"" +this.Status +"\"";
		//NOT REQUIRED FIELD

		if (this.Protocolversion!=null)
			attTxt+=" protocolVersion=\"" +this.Protocolversion +"\"";
		//NOT REQUIRED FIELD

		if (this.Protocolid!=null)
			attTxt+=" protocolId=\"" +this.Protocolid +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		xmlTxt+=this.extension.getXMLBody(preventComments);
		if (this.SubjectId!=null){
			xmlTxt+="\n<xnat:subject_ID";
			xmlTxt+=">";
			xmlTxt+=this.SubjectId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:subject_ID>";
		}
		if (this.VisitType!=null){
			xmlTxt+="\n<xnat:visit_type";
			xmlTxt+=">";
			xmlTxt+=this.VisitType.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:visit_type>";
		}
		if (this.VisitName!=null){
			xmlTxt+="\n<xnat:visit_name";
			xmlTxt+=">";
			xmlTxt+=this.VisitName.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:visit_name>";
		}
		if (this.Notes!=null){
			xmlTxt+="\n<xnat:notes";
			xmlTxt+=">";
			xmlTxt+=this.Notes.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:notes>";
		}
		if (this.StartDate!=null){
			xmlTxt+="\n<xnat:start_date";
			xmlTxt+=">";
			xmlTxt+=this.StartDate;
			xmlTxt+="</xnat:start_date>";
		}
		if (this.EndDate!=null){
			xmlTxt+="\n<xnat:end_date";
			xmlTxt+=">";
			xmlTxt+=this.EndDate;
			xmlTxt+="</xnat:end_date>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.SubjectId!=null) return true;
		if (this.VisitType!=null) return true;
		if (this.VisitName!=null) return true;
		if (this.Notes!=null) return true;
		if (this.StartDate!=null) return true;
		if (this.EndDate!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
