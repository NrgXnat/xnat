/*
 * web: xnat_publicationResource.js
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

function xnat_publicationResource(){
this.xsiType="xnat:publicationResource";

	this.getSchemaElementName=function(){
		return "publicationResource";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:publicationResource";
	}
this.extension=dynamicJSLoad('xnat_abstractResource','generated/xnat_abstractResource.js');

	this.Title=null;


	function getTitle() {
		return this.Title;
	}
	this.getTitle=getTitle;


	function setTitle(v){
		this.Title=v;
	}
	this.setTitle=setTitle;

	this.Citation=null;


	function getCitation() {
		return this.Citation;
	}
	this.getCitation=getCitation;


	function setCitation(v){
		this.Citation=v;
	}
	this.setCitation=setCitation;

	this.Abstract=null;


	function getAbstract() {
		return this.Abstract;
	}
	this.getAbstract=getAbstract;


	function setAbstract(v){
		this.Abstract=v;
	}
	this.setAbstract=setAbstract;

	this.Commentary=null;


	function getCommentary() {
		return this.Commentary;
	}
	this.getCommentary=getCommentary;


	function setCommentary(v){
		this.Commentary=v;
	}
	this.setCommentary=setCommentary;

	this.Isprimary=null;


	function getIsprimary() {
		return this.Isprimary;
	}
	this.getIsprimary=getIsprimary;


	function setIsprimary(v){
		this.Isprimary=v;
	}
	this.setIsprimary=setIsprimary;


	this.isIsprimary=function(defaultValue) {
		if(this.Isprimary==null)return defaultValue;
		if(this.Isprimary=="1" || this.Isprimary==true)return true;
		return false;
	}

	this.Doi=null;


	function getDoi() {
		return this.Doi;
	}
	this.getDoi=getDoi;


	function setDoi(v){
		this.Doi=v;
	}
	this.setDoi=setDoi;

	this.Pubmed=null;


	function getPubmed() {
		return this.Pubmed;
	}
	this.getPubmed=getPubmed;


	function setPubmed(v){
		this.Pubmed=v;
	}
	this.setPubmed=setPubmed;

	this.Medline=null;


	function getMedline() {
		return this.Medline;
	}
	this.getMedline=getMedline;


	function setMedline(v){
		this.Medline=v;
	}
	this.setMedline=setMedline;

	this.Uri=null;


	function getUri() {
		return this.Uri;
	}
	this.getUri=getUri;


	function setUri(v){
		this.Uri=v;
	}
	this.setUri=setUri;

	this.Other=null;


	function getOther() {
		return this.Other;
	}
	this.getOther=getOther;


	function setOther(v){
		this.Other=v;
	}
	this.setOther=setOther;

	this.Type=null;


	function getType() {
		return this.Type;
	}
	this.getType=getType;


	function setType(v){
		this.Type=v;
	}
	this.setType=setType;

	this.publications_publication_xnat_p_id_fk=null;


	this.getpublications_publication_xnat_p_id=function() {
		return this.publications_publication_xnat_p_id_fk;
	}


	this.setpublications_publication_xnat_p_id=function(v){
		this.publications_publication_xnat_p_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractResource"){
				return this.Abstractresource ;
			} else 
			if(xmlPath.startsWith("abstractResource")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Abstractresource ;
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
				if(this.Abstractresource!=undefined)return this.Abstractresource.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="title"){
				return this.Title ;
			} else 
			if(xmlPath=="citation"){
				return this.Citation ;
			} else 
			if(xmlPath=="abstract"){
				return this.Abstract ;
			} else 
			if(xmlPath=="commentary"){
				return this.Commentary ;
			} else 
			if(xmlPath=="isPrimary"){
				return this.Isprimary ;
			} else 
			if(xmlPath=="doi"){
				return this.Doi ;
			} else 
			if(xmlPath=="pubmed"){
				return this.Pubmed ;
			} else 
			if(xmlPath=="medline"){
				return this.Medline ;
			} else 
			if(xmlPath=="uri"){
				return this.Uri ;
			} else 
			if(xmlPath=="other"){
				return this.Other ;
			} else 
			if(xmlPath=="type"){
				return this.Type ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="publications_publication_xnat_p_id"){
				return this.publications_publication_xnat_p_id_fk ;
			} else 
			{
				return this.extension.getProperty(xmlPath);
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractResource"){
				this.Abstractresource=value;
			} else 
			if(xmlPath.startsWith("abstractResource")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Abstractresource ;
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
				if(this.Abstractresource!=undefined){
					this.Abstractresource.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Abstractresource= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Abstractresource= instanciateObject("xnat:abstractResource");//omUtils.js
						}
						if(options && options.where)this.Abstractresource.setProperty(options.where.field,options.where.value);
						this.Abstractresource.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="title"){
				this.Title=value;
			} else 
			if(xmlPath=="citation"){
				this.Citation=value;
			} else 
			if(xmlPath=="abstract"){
				this.Abstract=value;
			} else 
			if(xmlPath=="commentary"){
				this.Commentary=value;
			} else 
			if(xmlPath=="isPrimary"){
				this.Isprimary=value;
			} else 
			if(xmlPath=="doi"){
				this.Doi=value;
			} else 
			if(xmlPath=="pubmed"){
				this.Pubmed=value;
			} else 
			if(xmlPath=="medline"){
				this.Medline=value;
			} else 
			if(xmlPath=="uri"){
				this.Uri=value;
			} else 
			if(xmlPath=="other"){
				this.Other=value;
			} else 
			if(xmlPath=="type"){
				this.Type=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="publications_publication_xnat_p_id"){
				this.publications_publication_xnat_p_id_fk=value;
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
		if (xmlPath=="title"){
			return "field_LONG_DATA";
		}else if (xmlPath=="citation"){
			return "field_LONG_DATA";
		}else if (xmlPath=="abstract"){
			return "field_LONG_DATA";
		}else if (xmlPath=="commentary"){
			return "field_LONG_DATA";
		}else if (xmlPath=="isPrimary"){
			return "field_data";
		}else if (xmlPath=="doi"){
			return "field_data";
		}else if (xmlPath=="pubmed"){
			return "field_data";
		}else if (xmlPath=="medline"){
			return "field_data";
		}else if (xmlPath=="uri"){
			return "field_data";
		}else if (xmlPath=="other"){
			return "field_data";
		}else if (xmlPath=="type"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:publicationResource";
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
		xmlTxt+="\n</xnat:publicationResource>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.publications_publication_xnat_p_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="publications_publication_xnat_p_id=\"" + this.publications_publication_xnat_p_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = this.extension.getXMLAtts();
		if (this.Isprimary!=null)
			attTxt+=" isPrimary=\"" +this.Isprimary +"\"";
		//NOT REQUIRED FIELD

		if (this.Doi!=null)
			attTxt+=" doi=\"" +this.Doi +"\"";
		//NOT REQUIRED FIELD

		if (this.Pubmed!=null)
			attTxt+=" pubmed=\"" +this.Pubmed +"\"";
		//NOT REQUIRED FIELD

		if (this.Medline!=null)
			attTxt+=" medline=\"" +this.Medline +"\"";
		//NOT REQUIRED FIELD

		if (this.Uri!=null)
			attTxt+=" uri=\"" +this.Uri +"\"";
		//NOT REQUIRED FIELD

		if (this.Other!=null)
			attTxt+=" other=\"" +this.Other +"\"";
		//NOT REQUIRED FIELD

		if (this.Type!=null)
			attTxt+=" type=\"" +this.Type +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		xmlTxt+=this.extension.getXMLBody(preventComments);
		if (this.Title!=null){
			xmlTxt+="\n<xnat:title";
			xmlTxt+=">";
			xmlTxt+=this.Title.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:title>";
		}
		if (this.Citation!=null){
			xmlTxt+="\n<xnat:citation";
			xmlTxt+=">";
			xmlTxt+=this.Citation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:citation>";
		}
		if (this.Abstract!=null){
			xmlTxt+="\n<xnat:abstract";
			xmlTxt+=">";
			xmlTxt+=this.Abstract.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:abstract>";
		}
		if (this.Commentary!=null){
			xmlTxt+="\n<xnat:commentary";
			xmlTxt+=">";
			xmlTxt+=this.Commentary.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:commentary>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.publications_publication_xnat_p_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Title!=null) return true;
		if (this.Citation!=null) return true;
		if (this.Abstract!=null) return true;
		if (this.Commentary!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
