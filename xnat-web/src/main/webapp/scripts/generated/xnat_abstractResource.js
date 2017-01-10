/*
 * web: xnat_abstractResource.js
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

function xnat_abstractResource(){
this.xsiType="xnat:abstractResource";

	this.getSchemaElementName=function(){
		return "abstractResource";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:abstractResource";
	}

	this.Note=null;


	function getNote() {
		return this.Note;
	}
	this.getNote=getNote;


	function setNote(v){
		this.Note=v;
	}
	this.setNote=setNote;
	this.Tags_tag =new Array();

	function getTags_tag() {
		return this.Tags_tag;
	}
	this.getTags_tag=getTags_tag;


	function addTags_tag(v){
		this.Tags_tag.push(v);
	}
	this.addTags_tag=addTags_tag;

	this.Label=null;


	function getLabel() {
		return this.Label;
	}
	this.getLabel=getLabel;


	function setLabel(v){
		this.Label=v;
	}
	this.setLabel=setLabel;

	this.FileCount=null;


	function getFileCount() {
		return this.FileCount;
	}
	this.getFileCount=getFileCount;


	function setFileCount(v){
		this.FileCount=v;
	}
	this.setFileCount=setFileCount;

	this.FileSize=null;


	function getFileSize() {
		return this.FileSize;
	}
	this.getFileSize=getFileSize;


	function setFileSize(v){
		this.FileSize=v;
	}
	this.setFileSize=setFileSize;

	this.XnatAbstractresourceId=null;


	function getXnatAbstractresourceId() {
		return this.XnatAbstractresourceId;
	}
	this.getXnatAbstractresourceId=getXnatAbstractresourceId;


	function setXnatAbstractresourceId(v){
		this.XnatAbstractresourceId=v;
	}
	this.setXnatAbstractresourceId=setXnatAbstractresourceId;

	this.xnat_imageScanData_xnat_imagescandata_id_fk=null;


	this.getxnat_imageScanData_xnat_imagescandata_id=function() {
		return this.xnat_imageScanData_xnat_imagescandata_id_fk;
	}


	this.setxnat_imageScanData_xnat_imagescandata_id=function(v){
		this.xnat_imageScanData_xnat_imagescandata_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="note"){
				return this.Note ;
			} else 
			if(xmlPath=="tags/tag"){
				return this.Tags_tag ;
			} else 
			if(xmlPath.startsWith("tags/tag")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Tags_tag ;
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
				var index=0;
				if(options){
					if(options.index)index=options.index;
				}

			var whereArray;
				if (options.where){

				whereArray=new Array();

				for(var whereCount=0;whereCount<this.Tags_tag.length;whereCount++){

					var tempValue=this.Tags_tag[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Tags_tag[whereCount]);

					}

				}
				}else{

				whereArray=this.Tags_tag;
				}

			var typeArray;
				if (options.xsiType){

				typeArray=new Array();

				for(var typeCount=0;typeCount<whereArray.length;typeCount++){

					if(whereArray[typeCount].getFullSchemaElementName()==options.xsiType){

						typeArray.push(whereArray[typeCount]);

					}

				}
				}else{

				typeArray=whereArray;
				}
				if (typeArray.length>index){
					return typeArray[index].getProperty(xmlPath);
				}else{
					return null;
				}
			} else 
			if(xmlPath=="label"){
				return this.Label ;
			} else 
			if(xmlPath=="file_count"){
				return this.FileCount ;
			} else 
			if(xmlPath=="file_size"){
				return this.FileSize ;
			} else 
			if(xmlPath=="extension_item"){
				return this.ExtensionItem ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_abstractResource_id"){
				return this.XnatAbstractresourceId ;
			} else 
			if(xmlPath=="xnat_imageScanData_xnat_imagescandata_id"){
				return this.xnat_imageScanData_xnat_imagescandata_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="note"){
				this.Note=value;
			} else 
			if(xmlPath=="tags/tag"){
				this.Tags_tag=value;
			} else 
			if(xmlPath.startsWith("tags/tag")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Tags_tag ;
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
				var index=0;
				if(options){
					if(options.index)index=options.index;
				}

			var whereArray;
				if (options && options.where){

				whereArray=new Array();

				for(var whereCount=0;whereCount<this.Tags_tag.length;whereCount++){

					var tempValue=this.Tags_tag[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Tags_tag[whereCount]);

					}

				}
				}else{

				whereArray=this.Tags_tag;
				}

			var typeArray;
				if (options && options.xsiType){

				typeArray=new Array();

				for(var typeCount=0;typeCount<whereArray.length;typeCount++){

					if(whereArray[typeCount].getFullSchemaElementName()==options.xsiType){

						typeArray.push(whereArray[typeCount]);

					}

				}
				}else{

				typeArray=whereArray;
				}
				if (typeArray.length>index){
					typeArray[index].setProperty(xmlPath,value);
				}else{
					var newChild;
					if(options && options.xsiType){
						newChild= instanciateObject(options.xsiType);//omUtils.js
					}else{
						newChild= instanciateObject("xnat:abstractResource_tag");//omUtils.js
					}
					this.addTags_tag(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="label"){
				this.Label=value;
			} else 
			if(xmlPath=="file_count"){
				this.FileCount=value;
			} else 
			if(xmlPath=="file_size"){
				this.FileSize=value;
			} else 
			if(xmlPath=="extension_item"){
				this.ExtensionItem=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_abstractResource_id"){
				this.XnatAbstractresourceId=value;
			} else 
			if(xmlPath=="xnat_imageScanData_xnat_imagescandata_id"){
				this.xnat_imageScanData_xnat_imagescandata_id_fk=value;
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
		if (xmlPath=="tags/tag"){
			this.addTags_tag(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="tags/tag"){
			return "http://nrg.wustl.edu/xnat:abstractResource_tag";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="note"){
			return "field_LONG_DATA";
		}else if (xmlPath=="tags/tag"){
			return "field_NO_CHILD";
		}else if (xmlPath=="label"){
			return "field_data";
		}else if (xmlPath=="file_count"){
			return "field_data";
		}else if (xmlPath=="file_size"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:abstractResource";
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
		xmlTxt+="\n</xnat:abstractResource>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatAbstractresourceId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_abstractResource_id=\"" + this.XnatAbstractresourceId + "\"";
			}
			if(this.xnat_imageScanData_xnat_imagescandata_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_imageScanData_xnat_imagescandata_id=\"" + this.xnat_imageScanData_xnat_imagescandata_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Label!=null)
			attTxt+=" label=\"" +this.Label +"\"";
		//NOT REQUIRED FIELD

		if (this.FileCount!=null)
			attTxt+=" file_count=\"" +this.FileCount +"\"";
		//NOT REQUIRED FIELD

		if (this.FileSize!=null)
			attTxt+=" file_size=\"" +this.FileSize +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Note!=null){
			xmlTxt+="\n<xnat:note";
			xmlTxt+=">";
			xmlTxt+=this.Note.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:note>";
		}
			var child0=0;
			var att0=0;
			child0+=this.Tags_tag.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:tags";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Tags_tagCOUNT=0;Tags_tagCOUNT<this.Tags_tag.length;Tags_tagCOUNT++){
			xmlTxt +="\n<xnat:tag";
			xmlTxt +=this.Tags_tag[Tags_tagCOUNT].getXMLAtts();
			if(this.Tags_tag[Tags_tagCOUNT].xsiType!="xnat:abstractResource_tag"){
				xmlTxt+=" xsi:type=\"" + this.Tags_tag[Tags_tagCOUNT].xsiType + "\"";
			}
			if (this.Tags_tag[Tags_tagCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Tags_tag[Tags_tagCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:tag>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:tags>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatAbstractresourceId!=null) return true;
			if (this.xnat_imageScanData_xnat_imagescandata_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Note!=null) return true;
			if(this.Tags_tag.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
