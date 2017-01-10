/*
 * web: xnat_investigatorData.js
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

function xnat_investigatorData(){
this.xsiType="xnat:investigatorData";

	this.getSchemaElementName=function(){
		return "investigatorData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:investigatorData";
	}

	this.Title=null;


	function getTitle() {
		return this.Title;
	}
	this.getTitle=getTitle;


	function setTitle(v){
		this.Title=v;
	}
	this.setTitle=setTitle;

	this.Firstname=null;


	function getFirstname() {
		return this.Firstname;
	}
	this.getFirstname=getFirstname;


	function setFirstname(v){
		this.Firstname=v;
	}
	this.setFirstname=setFirstname;

	this.Lastname=null;


	function getLastname() {
		return this.Lastname;
	}
	this.getLastname=getLastname;


	function setLastname(v){
		this.Lastname=v;
	}
	this.setLastname=setLastname;

	this.Institution=null;


	function getInstitution() {
		return this.Institution;
	}
	this.getInstitution=getInstitution;


	function setInstitution(v){
		this.Institution=v;
	}
	this.setInstitution=setInstitution;

	this.Department=null;


	function getDepartment() {
		return this.Department;
	}
	this.getDepartment=getDepartment;


	function setDepartment(v){
		this.Department=v;
	}
	this.setDepartment=setDepartment;

	this.Email=null;


	function getEmail() {
		return this.Email;
	}
	this.getEmail=getEmail;


	function setEmail(v){
		this.Email=v;
	}
	this.setEmail=setEmail;

	this.Phone=null;


	function getPhone() {
		return this.Phone;
	}
	this.getPhone=getPhone;


	function setPhone(v){
		this.Phone=v;
	}
	this.setPhone=setPhone;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.XnatInvestigatordataId=null;


	function getXnatInvestigatordataId() {
		return this.XnatInvestigatordataId;
	}
	this.getXnatInvestigatordataId=getXnatInvestigatordataId;


	function setXnatInvestigatordataId(v){
		this.XnatInvestigatordataId=v;
	}
	this.setXnatInvestigatordataId=setXnatInvestigatordataId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="title"){
				return this.Title ;
			} else 
			if(xmlPath=="firstname"){
				return this.Firstname ;
			} else 
			if(xmlPath=="lastname"){
				return this.Lastname ;
			} else 
			if(xmlPath=="institution"){
				return this.Institution ;
			} else 
			if(xmlPath=="department"){
				return this.Department ;
			} else 
			if(xmlPath=="email"){
				return this.Email ;
			} else 
			if(xmlPath=="phone"){
				return this.Phone ;
			} else 
			if(xmlPath=="ID"){
				return this.Id ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_investigatorData_id"){
				return this.XnatInvestigatordataId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="title"){
				this.Title=value;
			} else 
			if(xmlPath=="firstname"){
				this.Firstname=value;
			} else 
			if(xmlPath=="lastname"){
				this.Lastname=value;
			} else 
			if(xmlPath=="institution"){
				this.Institution=value;
			} else 
			if(xmlPath=="department"){
				this.Department=value;
			} else 
			if(xmlPath=="email"){
				this.Email=value;
			} else 
			if(xmlPath=="phone"){
				this.Phone=value;
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_investigatorData_id"){
				this.XnatInvestigatordataId=value;
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
		if (xmlPath=="title"){
			return "field_data";
		}else if (xmlPath=="firstname"){
			return "field_data";
		}else if (xmlPath=="lastname"){
			return "field_data";
		}else if (xmlPath=="institution"){
			return "field_data";
		}else if (xmlPath=="department"){
			return "field_data";
		}else if (xmlPath=="email"){
			return "field_data";
		}else if (xmlPath=="phone"){
			return "field_data";
		}else if (xmlPath=="ID"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:Investigator";
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
		xmlTxt+="\n</xnat:Investigator>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatInvestigatordataId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_investigatorData_id=\"" + this.XnatInvestigatordataId + "\"";
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

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Title!=null){
			xmlTxt+="\n<xnat:title";
			xmlTxt+=">";
			xmlTxt+=this.Title.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:title>";
		}
		if (this.Firstname!=null){
			xmlTxt+="\n<xnat:firstname";
			xmlTxt+=">";
			xmlTxt+=this.Firstname.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:firstname>";
		}
		if (this.Lastname!=null){
			xmlTxt+="\n<xnat:lastname";
			xmlTxt+=">";
			xmlTxt+=this.Lastname.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:lastname>";
		}
		if (this.Institution!=null){
			xmlTxt+="\n<xnat:institution";
			xmlTxt+=">";
			xmlTxt+=this.Institution.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:institution>";
		}
		if (this.Department!=null){
			xmlTxt+="\n<xnat:department";
			xmlTxt+=">";
			xmlTxt+=this.Department.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:department>";
		}
		if (this.Email!=null){
			xmlTxt+="\n<xnat:email";
			xmlTxt+=">";
			xmlTxt+=this.Email.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:email>";
		}
		if (this.Phone!=null){
			xmlTxt+="\n<xnat:phone";
			xmlTxt+=">";
			xmlTxt+=this.Phone.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:phone>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatInvestigatordataId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Title!=null) return true;
		if (this.Firstname!=null) return true;
		if (this.Lastname!=null) return true;
		if (this.Institution!=null) return true;
		if (this.Department!=null) return true;
		if (this.Email!=null) return true;
		if (this.Phone!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
