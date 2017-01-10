/*
 * web: xnat_regionResource.js
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

function xnat_regionResource(){
this.xsiType="xnat:regionResource";

	this.getSchemaElementName=function(){
		return "regionResource";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:regionResource";
	}
	this.File =null;
	function getFile() {
		return this.File;
	}
	this.getFile=getFile;


	function setFile(v){
		this.File =v;
	}
	this.setFile=setFile;

	this.File_FileXnatAbstractresourceId=null;


	function getFile_FileXnatAbstractresourceId(){
		return this.File_FileXnatAbstractresourceId;
	}
	this.getFile_FileXnatAbstractresourceId=getFile_FileXnatAbstractresourceId;


	function setFile_FileXnatAbstractresourceId(v){
		this.File_FileXnatAbstractresourceId=v;
	}
	this.setFile_FileXnatAbstractresourceId=setFile_FileXnatAbstractresourceId;
	this.Baseimage =null;
	function getBaseimage() {
		return this.Baseimage;
	}
	this.getBaseimage=getBaseimage;


	function setBaseimage(v){
		this.Baseimage =v;
	}
	this.setBaseimage=setBaseimage;

	this.Baseimage_BaseimageXnatAbstractresourceId=null;


	function getBaseimage_BaseimageXnatAbstractresourceId(){
		return this.Baseimage_BaseimageXnatAbstractresourceId;
	}
	this.getBaseimage_BaseimageXnatAbstractresourceId=getBaseimage_BaseimageXnatAbstractresourceId;


	function setBaseimage_BaseimageXnatAbstractresourceId(v){
		this.Baseimage_BaseimageXnatAbstractresourceId=v;
	}
	this.setBaseimage_BaseimageXnatAbstractresourceId=setBaseimage_BaseimageXnatAbstractresourceId;

	this.Creator_firstname=null;


	function getCreator_firstname() {
		return this.Creator_firstname;
	}
	this.getCreator_firstname=getCreator_firstname;


	function setCreator_firstname(v){
		this.Creator_firstname=v;
	}
	this.setCreator_firstname=setCreator_firstname;

	this.Creator_lastname=null;


	function getCreator_lastname() {
		return this.Creator_lastname;
	}
	this.getCreator_lastname=getCreator_lastname;


	function setCreator_lastname(v){
		this.Creator_lastname=v;
	}
	this.setCreator_lastname=setCreator_lastname;
	this.Subregionlabels_label =new Array();

	function getSubregionlabels_label() {
		return this.Subregionlabels_label;
	}
	this.getSubregionlabels_label=getSubregionlabels_label;


	function addSubregionlabels_label(v){
		this.Subregionlabels_label.push(v);
	}
	this.addSubregionlabels_label=addSubregionlabels_label;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.Hemisphere=null;


	function getHemisphere() {
		return this.Hemisphere;
	}
	this.getHemisphere=getHemisphere;


	function setHemisphere(v){
		this.Hemisphere=v;
	}
	this.setHemisphere=setHemisphere;

	this.SessionId=null;


	function getSessionId() {
		return this.SessionId;
	}
	this.getSessionId=getSessionId;


	function setSessionId(v){
		this.SessionId=v;
	}
	this.setSessionId=setSessionId;

	this.XnatRegionresourceId=null;


	function getXnatRegionresourceId() {
		return this.XnatRegionresourceId;
	}
	this.getXnatRegionresourceId=getXnatRegionresourceId;


	function setXnatRegionresourceId(v){
		this.XnatRegionresourceId=v;
	}
	this.setXnatRegionresourceId=setXnatRegionresourceId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="file"){
				return this.File ;
			} else 
			if(xmlPath.startsWith("file")){
				xmlPath=xmlPath.substring(4);
				if(xmlPath=="")return this.File ;
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
				if(this.File!=undefined)return this.File.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="baseimage"){
				return this.Baseimage ;
			} else 
			if(xmlPath.startsWith("baseimage")){
				xmlPath=xmlPath.substring(9);
				if(xmlPath=="")return this.Baseimage ;
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
				if(this.Baseimage!=undefined)return this.Baseimage.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="creator/firstname"){
				return this.Creator_firstname ;
			} else 
			if(xmlPath=="creator/lastname"){
				return this.Creator_lastname ;
			} else 
			if(xmlPath=="subregionlabels/label"){
				return this.Subregionlabels_label ;
			} else 
			if(xmlPath.startsWith("subregionlabels/label")){
				xmlPath=xmlPath.substring(21);
				if(xmlPath=="")return this.Subregionlabels_label ;
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

				for(var whereCount=0;whereCount<this.Subregionlabels_label.length;whereCount++){

					var tempValue=this.Subregionlabels_label[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Subregionlabels_label[whereCount]);

					}

				}
				}else{

				whereArray=this.Subregionlabels_label;
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
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="hemisphere"){
				return this.Hemisphere ;
			} else 
			if(xmlPath=="session_id"){
				return this.SessionId ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_regionResource_id"){
				return this.XnatRegionresourceId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="file"){
				this.File=value;
			} else 
			if(xmlPath.startsWith("file")){
				xmlPath=xmlPath.substring(4);
				if(xmlPath=="")return this.File ;
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
				if(this.File!=undefined){
					this.File.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.File= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.File= instanciateObject("xnat:abstractResource");//omUtils.js
						}
						if(options && options.where)this.File.setProperty(options.where.field,options.where.value);
						this.File.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="baseimage"){
				this.Baseimage=value;
			} else 
			if(xmlPath.startsWith("baseimage")){
				xmlPath=xmlPath.substring(9);
				if(xmlPath=="")return this.Baseimage ;
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
				if(this.Baseimage!=undefined){
					this.Baseimage.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Baseimage= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Baseimage= instanciateObject("xnat:abstractResource");//omUtils.js
						}
						if(options && options.where)this.Baseimage.setProperty(options.where.field,options.where.value);
						this.Baseimage.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="creator/firstname"){
				this.Creator_firstname=value;
			} else 
			if(xmlPath=="creator/lastname"){
				this.Creator_lastname=value;
			} else 
			if(xmlPath=="subregionlabels/label"){
				this.Subregionlabels_label=value;
			} else 
			if(xmlPath.startsWith("subregionlabels/label")){
				xmlPath=xmlPath.substring(21);
				if(xmlPath=="")return this.Subregionlabels_label ;
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

				for(var whereCount=0;whereCount<this.Subregionlabels_label.length;whereCount++){

					var tempValue=this.Subregionlabels_label[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Subregionlabels_label[whereCount]);

					}

				}
				}else{

				whereArray=this.Subregionlabels_label;
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
						newChild= instanciateObject("xnat:regionResource_label");//omUtils.js
					}
					this.addSubregionlabels_label(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="hemisphere"){
				this.Hemisphere=value;
			} else 
			if(xmlPath=="session_id"){
				this.SessionId=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_regionResource_id"){
				this.XnatRegionresourceId=value;
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
		if (xmlPath=="file"){
			this.setFile(v);
		}else if (xmlPath=="baseimage"){
			this.setBaseimage(v);
		}else if (xmlPath=="subregionlabels/label"){
			this.addSubregionlabels_label(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="file"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="baseimage"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="subregionlabels/label"){
			return "http://nrg.wustl.edu/xnat:regionResource_label";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="file"){
			return "field_single_reference";
		}else if (xmlPath=="baseimage"){
			return "field_single_reference";
		}else if (xmlPath=="creator/firstname"){
			return "field_data";
		}else if (xmlPath=="creator/lastname"){
			return "field_data";
		}else if (xmlPath=="subregionlabels/label"){
			return "field_NO_CHILD";
		}else if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="hemisphere"){
			return "field_data";
		}else if (xmlPath=="session_id"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:ImageRegionResource";
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
		xmlTxt+="\n</xnat:ImageRegionResource>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatRegionresourceId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_regionResource_id=\"" + this.XnatRegionresourceId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		else attTxt+=" name=\"\"";//REQUIRED FIELD

		if (this.Hemisphere!=null)
			attTxt+=" hemisphere=\"" +this.Hemisphere +"\"";
		else attTxt+=" hemisphere=\"\"";//REQUIRED FIELD

		if (this.SessionId!=null)
			attTxt+=" session_id=\"" +this.SessionId +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.File!=null){
			xmlTxt+="\n<xnat:file";
			xmlTxt+=this.File.getXMLAtts();
			if(this.File.xsiType!="xnat:abstractResource"){
				xmlTxt+=" xsi:type=\"" + this.File.xsiType + "\"";
			}
			if (this.File.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.File.getXMLBody(preventComments);
				xmlTxt+="</xnat:file>";
			}else {xmlTxt+="/>";}
		}
		else{
			xmlTxt+="\n<xnat:file/>";//REQUIRED
		}
		if (this.Baseimage!=null){
			xmlTxt+="\n<xnat:baseimage";
			xmlTxt+=this.Baseimage.getXMLAtts();
			if(this.Baseimage.xsiType!="xnat:abstractResource"){
				xmlTxt+=" xsi:type=\"" + this.Baseimage.xsiType + "\"";
			}
			if (this.Baseimage.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Baseimage.getXMLBody(preventComments);
				xmlTxt+="</xnat:baseimage>";
			}else {xmlTxt+="/>";}
		}
		else{
			xmlTxt+="\n<xnat:baseimage/>";//REQUIRED
		}
			var child0=0;
			var att0=0;
			if(this.Creator_firstname!=null)
			child0++;
			if(this.Creator_lastname!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:creator";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Creator_firstname!=null){
			xmlTxt+="\n<xnat:firstname";
			xmlTxt+=">";
			xmlTxt+=this.Creator_firstname.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:firstname>";
		}
		if (this.Creator_lastname!=null){
			xmlTxt+="\n<xnat:lastname";
			xmlTxt+=">";
			xmlTxt+=this.Creator_lastname.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:lastname>";
		}
				xmlTxt+="\n</xnat:creator>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.Subregionlabels_label.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:subregionlabels";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Subregionlabels_labelCOUNT=0;Subregionlabels_labelCOUNT<this.Subregionlabels_label.length;Subregionlabels_labelCOUNT++){
			xmlTxt +="\n<xnat:label";
			xmlTxt +=this.Subregionlabels_label[Subregionlabels_labelCOUNT].getXMLAtts();
			if(this.Subregionlabels_label[Subregionlabels_labelCOUNT].xsiType!="xnat:regionResource_label"){
				xmlTxt+=" xsi:type=\"" + this.Subregionlabels_label[Subregionlabels_labelCOUNT].xsiType + "\"";
			}
			if (this.Subregionlabels_label[Subregionlabels_labelCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Subregionlabels_label[Subregionlabels_labelCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:label>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:subregionlabels>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatRegionresourceId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.File!=null){
			if (this.File.hasXMLBodyContent()) return true;
		}
		return true;//REQUIRED file
	}
}
