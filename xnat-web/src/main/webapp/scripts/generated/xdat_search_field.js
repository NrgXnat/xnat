/*
 * web: xdat_search_field.js
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

function xdat_search_field(){
this.xsiType="xdat:search_field";

	this.getSchemaElementName=function(){
		return "search_field";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:search_field";
	}

	this.ElementName=null;


	function getElementName() {
		return this.ElementName;
	}
	this.getElementName=getElementName;


	function setElementName(v){
		this.ElementName=v;
	}
	this.setElementName=setElementName;

	this.FieldId=null;


	function getFieldId() {
		return this.FieldId;
	}
	this.getFieldId=getFieldId;


	function setFieldId(v){
		this.FieldId=v;
	}
	this.setFieldId=setFieldId;

	this.Sequence=null;


	function getSequence() {
		return this.Sequence;
	}
	this.getSequence=getSequence;


	function setSequence(v){
		this.Sequence=v;
	}
	this.setSequence=setSequence;

	this.Type=null;


	function getType() {
		return this.Type;
	}
	this.getType=getType;


	function setType(v){
		this.Type=v;
	}
	this.setType=setType;

	this.Header=null;


	function getHeader() {
		return this.Header;
	}
	this.getHeader=getHeader;


	function setHeader(v){
		this.Header=v;
	}
	this.setHeader=setHeader;

	this.Value=null;


	function getValue() {
		return this.Value;
	}
	this.getValue=getValue;


	function setValue(v){
		this.Value=v;
	}
	this.setValue=setValue;

	this.Visible=null;


	function getVisible() {
		return this.Visible;
	}
	this.getVisible=getVisible;


	function setVisible(v){
		this.Visible=v;
	}
	this.setVisible=setVisible;


	this.isVisible=function(defaultValue) {
		if(this.Visible==null)return defaultValue;
		if(this.Visible=="1" || this.Visible==true)return true;
		return false;
	}

	this.XdatSearchFieldId=null;


	function getXdatSearchFieldId() {
		return this.XdatSearchFieldId;
	}
	this.getXdatSearchFieldId=getXdatSearchFieldId;


	function setXdatSearchFieldId(v){
		this.XdatSearchFieldId=v;
	}
	this.setXdatSearchFieldId=setXdatSearchFieldId;

	this.xdat_stored_search_id_fk=null;


	this.getxdat_stored_search_id=function() {
		return this.xdat_stored_search_id_fk;
	}


	this.setxdat_stored_search_id=function(v){
		this.xdat_stored_search_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="element_name"){
				return this.ElementName ;
			} else 
			if(xmlPath=="field_ID"){
				return this.FieldId ;
			} else 
			if(xmlPath=="sequence"){
				return this.Sequence ;
			} else 
			if(xmlPath=="type"){
				return this.Type ;
			} else 
			if(xmlPath=="header"){
				return this.Header ;
			} else 
			if(xmlPath=="value"){
				return this.Value ;
			} else 
			if(xmlPath=="visible"){
				return this.Visible ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_search_field_id"){
				return this.XdatSearchFieldId ;
			} else 
			if(xmlPath=="xdat_stored_search_id"){
				return this.xdat_stored_search_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="element_name"){
				this.ElementName=value;
			} else 
			if(xmlPath=="field_ID"){
				this.FieldId=value;
			} else 
			if(xmlPath=="sequence"){
				this.Sequence=value;
			} else 
			if(xmlPath=="type"){
				this.Type=value;
			} else 
			if(xmlPath=="header"){
				this.Header=value;
			} else 
			if(xmlPath=="value"){
				this.Value=value;
			} else 
			if(xmlPath=="visible"){
				this.Visible=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_search_field_id"){
				this.XdatSearchFieldId=value;
			} else 
			if(xmlPath=="xdat_stored_search_id"){
				this.xdat_stored_search_id_fk=value;
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
		if (xmlPath=="element_name"){
			return "field_data";
		}else if (xmlPath=="field_ID"){
			return "field_data";
		}else if (xmlPath=="sequence"){
			return "field_data";
		}else if (xmlPath=="type"){
			return "field_data";
		}else if (xmlPath=="header"){
			return "field_data";
		}else if (xmlPath=="value"){
			return "field_data";
		}else if (xmlPath=="visible"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:search_field";
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
		xmlTxt+="\n</xdat:search_field>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatSearchFieldId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_search_field_id=\"" + this.XdatSearchFieldId + "\"";
			}
			if(this.xdat_stored_search_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_stored_search_id=\"" + this.xdat_stored_search_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Visible!=null)
			attTxt+=" visible=\"" +this.Visible +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.ElementName!=null){
			xmlTxt+="\n<xdat:element_name";
			xmlTxt+=">";
			xmlTxt+=this.ElementName.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:element_name>";
		}
		if (this.FieldId!=null){
			xmlTxt+="\n<xdat:field_ID";
			xmlTxt+=">";
			xmlTxt+=this.FieldId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:field_ID>";
		}
		if (this.Sequence!=null){
			xmlTxt+="\n<xdat:sequence";
			xmlTxt+=">";
			xmlTxt+=this.Sequence;
			xmlTxt+="</xdat:sequence>";
		}
		else{
			xmlTxt+="\n<xdat:sequence";
			xmlTxt+="/>";
		}

		if (this.Type!=null){
			xmlTxt+="\n<xdat:type";
			xmlTxt+=">";
			xmlTxt+=this.Type.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:type>";
		}
		if (this.Header!=null){
			xmlTxt+="\n<xdat:header";
			xmlTxt+=">";
			xmlTxt+=this.Header.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:header>";
		}
		if (this.Value!=null){
			xmlTxt+="\n<xdat:value";
			xmlTxt+=">";
			xmlTxt+=this.Value.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:value>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatSearchFieldId!=null) return true;
			if (this.xdat_stored_search_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.ElementName!=null) return true;
		if (this.FieldId!=null) return true;
		if (this.Sequence!=null) return true;
		return true;//REQUIRED sequence
	}
}
