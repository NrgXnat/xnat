/*
 * web: xnat_xaScanData.js
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

function xnat_xaScanData(){
this.xsiType="xnat:xaScanData";

	this.getSchemaElementName=function(){
		return "xaScanData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:xaScanData";
	}
this.extension=dynamicJSLoad('xnat_imageScanData','generated/xnat_imageScanData.js');

	this.Parameters_pixelres_units=null;


	function getParameters_pixelres_units() {
		return this.Parameters_pixelres_units;
	}
	this.getParameters_pixelres_units=getParameters_pixelres_units;


	function setParameters_pixelres_units(v){
		this.Parameters_pixelres_units=v;
	}
	this.setParameters_pixelres_units=setParameters_pixelres_units;

	this.Parameters_pixelres_x=null;


	function getParameters_pixelres_x() {
		return this.Parameters_pixelres_x;
	}
	this.getParameters_pixelres_x=getParameters_pixelres_x;


	function setParameters_pixelres_x(v){
		this.Parameters_pixelres_x=v;
	}
	this.setParameters_pixelres_x=setParameters_pixelres_x;

	this.Parameters_pixelres_y=null;


	function getParameters_pixelres_y() {
		return this.Parameters_pixelres_y;
	}
	this.getParameters_pixelres_y=getParameters_pixelres_y;


	function setParameters_pixelres_y(v){
		this.Parameters_pixelres_y=v;
	}
	this.setParameters_pixelres_y=setParameters_pixelres_y;

	this.Parameters_orientation=null;


	function getParameters_orientation() {
		return this.Parameters_orientation;
	}
	this.getParameters_orientation=getParameters_orientation;


	function setParameters_orientation(v){
		this.Parameters_orientation=v;
	}
	this.setParameters_orientation=setParameters_orientation;

	this.Parameters_fov_x=null;


	function getParameters_fov_x() {
		return this.Parameters_fov_x;
	}
	this.getParameters_fov_x=getParameters_fov_x;


	function setParameters_fov_x(v){
		this.Parameters_fov_x=v;
	}
	this.setParameters_fov_x=setParameters_fov_x;

	this.Parameters_fov_y=null;


	function getParameters_fov_y() {
		return this.Parameters_fov_y;
	}
	this.getParameters_fov_y=getParameters_fov_y;


	function setParameters_fov_y(v){
		this.Parameters_fov_y=v;
	}
	this.setParameters_fov_y=setParameters_fov_y;

	this.Parameters_imagetype=null;


	function getParameters_imagetype() {
		return this.Parameters_imagetype;
	}
	this.getParameters_imagetype=getParameters_imagetype;


	function setParameters_imagetype(v){
		this.Parameters_imagetype=v;
	}
	this.setParameters_imagetype=setParameters_imagetype;

	this.Parameters_options=null;


	function getParameters_options() {
		return this.Parameters_options;
	}
	this.getParameters_options=getParameters_options;


	function setParameters_options(v){
		this.Parameters_options=v;
	}
	this.setParameters_options=setParameters_options;

	this.Parameters_derivation=null;


	function getParameters_derivation() {
		return this.Parameters_derivation;
	}
	this.getParameters_derivation=getParameters_derivation;


	function setParameters_derivation(v){
		this.Parameters_derivation=v;
	}
	this.setParameters_derivation=setParameters_derivation;
	this.Parameters_contrastbolus =null;
	function getParameters_contrastbolus() {
		return this.Parameters_contrastbolus;
	}
	this.getParameters_contrastbolus=getParameters_contrastbolus;


	function setParameters_contrastbolus(v){
		this.Parameters_contrastbolus =v;
	}
	this.setParameters_contrastbolus=setParameters_contrastbolus;

	this.Parameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId=null;


	function getParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId(){
		return this.Parameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId;
	}
	this.getParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId=getParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId;


	function setParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId(v){
		this.Parameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId=v;
	}
	this.setParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId=setParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageScanData"){
				return this.Imagescandata ;
			} else 
			if(xmlPath.startsWith("imageScanData")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Imagescandata ;
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
				if(this.Imagescandata!=undefined)return this.Imagescandata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="parameters/pixelRes/units"){
				return this.Parameters_pixelres_units ;
			} else 
			if(xmlPath=="parameters/pixelRes/x"){
				return this.Parameters_pixelres_x ;
			} else 
			if(xmlPath=="parameters/pixelRes/y"){
				return this.Parameters_pixelres_y ;
			} else 
			if(xmlPath=="parameters/orientation"){
				return this.Parameters_orientation ;
			} else 
			if(xmlPath=="parameters/fov/x"){
				return this.Parameters_fov_x ;
			} else 
			if(xmlPath=="parameters/fov/y"){
				return this.Parameters_fov_y ;
			} else 
			if(xmlPath=="parameters/imageType"){
				return this.Parameters_imagetype ;
			} else 
			if(xmlPath=="parameters/options"){
				return this.Parameters_options ;
			} else 
			if(xmlPath=="parameters/derivation"){
				return this.Parameters_derivation ;
			} else 
			if(xmlPath=="parameters/contrastBolus"){
				return this.Parameters_contrastbolus ;
			} else 
			if(xmlPath.startsWith("parameters/contrastBolus")){
				xmlPath=xmlPath.substring(24);
				if(xmlPath=="")return this.Parameters_contrastbolus ;
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
				if(this.Parameters_contrastbolus!=undefined)return this.Parameters_contrastbolus.getProperty(xmlPath);
				else return null;
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
			if(xmlPath=="imageScanData"){
				this.Imagescandata=value;
			} else 
			if(xmlPath.startsWith("imageScanData")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Imagescandata ;
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
				if(this.Imagescandata!=undefined){
					this.Imagescandata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Imagescandata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Imagescandata= instanciateObject("xnat:imageScanData");//omUtils.js
						}
						if(options && options.where)this.Imagescandata.setProperty(options.where.field,options.where.value);
						this.Imagescandata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="parameters/pixelRes/units"){
				this.Parameters_pixelres_units=value;
			} else 
			if(xmlPath=="parameters/pixelRes/x"){
				this.Parameters_pixelres_x=value;
			} else 
			if(xmlPath=="parameters/pixelRes/y"){
				this.Parameters_pixelres_y=value;
			} else 
			if(xmlPath=="parameters/orientation"){
				this.Parameters_orientation=value;
			} else 
			if(xmlPath=="parameters/fov/x"){
				this.Parameters_fov_x=value;
			} else 
			if(xmlPath=="parameters/fov/y"){
				this.Parameters_fov_y=value;
			} else 
			if(xmlPath=="parameters/imageType"){
				this.Parameters_imagetype=value;
			} else 
			if(xmlPath=="parameters/options"){
				this.Parameters_options=value;
			} else 
			if(xmlPath=="parameters/derivation"){
				this.Parameters_derivation=value;
			} else 
			if(xmlPath=="parameters/contrastBolus"){
				this.Parameters_contrastbolus=value;
			} else 
			if(xmlPath.startsWith("parameters/contrastBolus")){
				xmlPath=xmlPath.substring(24);
				if(xmlPath=="")return this.Parameters_contrastbolus ;
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
				if(this.Parameters_contrastbolus!=undefined){
					this.Parameters_contrastbolus.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Parameters_contrastbolus= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Parameters_contrastbolus= instanciateObject("xnat:contrastBolus");//omUtils.js
						}
						if(options && options.where)this.Parameters_contrastbolus.setProperty(options.where.field,options.where.value);
						this.Parameters_contrastbolus.setProperty(xmlPath,value);
				}
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
		if (xmlPath=="parameters/contrastBolus"){
			this.setParameters_contrastbolus(v);
		}
		else{
			this.extension.setReferenceField(xmlPath,v);
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="parameters/contrastBolus"){
			return "http://nrg.wustl.edu/xnat:contrastBolus";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="parameters/pixelRes/units"){
			return "field_data";
		}else if (xmlPath=="parameters/pixelRes/x"){
			return "field_data";
		}else if (xmlPath=="parameters/pixelRes/y"){
			return "field_data";
		}else if (xmlPath=="parameters/orientation"){
			return "field_data";
		}else if (xmlPath=="parameters/fov/x"){
			return "field_data";
		}else if (xmlPath=="parameters/fov/y"){
			return "field_data";
		}else if (xmlPath=="parameters/imageType"){
			return "field_data";
		}else if (xmlPath=="parameters/options"){
			return "field_data";
		}else if (xmlPath=="parameters/derivation"){
			return "field_data";
		}else if (xmlPath=="parameters/contrastBolus"){
			return "field_single_reference";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:XAScan";
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
		xmlTxt+="\n</xnat:XAScan>";
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
			var child0=0;
			var att0=0;
			if(this.Parameters_options!=null)
			child0++;
			if(this.Parameters_fov_y!=null)
			child0++;
			if(this.Parameters_fov_x!=null)
			child0++;
			if(this.Parameters_contrastbolus!=null)
			child0++;
			if(this.Parameters_orientation!=null)
			child0++;
			if(this.Parameters_pixelres_units!=null)
			child0++;
			if(this.Parameters_imagetype!=null)
			child0++;
			if(this.Parameters_pixelres_y!=null)
			child0++;
			if(this.Parameters_pixelres_x!=null)
			child0++;
			if(this.Parameters_derivation!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:parameters";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		var Parameters_pixelresATT = ""
		if (this.Parameters_pixelres_units!=null)
			Parameters_pixelresATT+=" units=\"" + this.Parameters_pixelres_units.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Parameters_pixelres_x!=null)
			Parameters_pixelresATT+=" x=\"" + this.Parameters_pixelres_x + "\"";
		if (this.Parameters_pixelres_y!=null)
			Parameters_pixelresATT+=" y=\"" + this.Parameters_pixelres_y + "\"";
		if(Parameters_pixelresATT!=""){
			xmlTxt+="\n<xnat:pixelRes";
			xmlTxt+=Parameters_pixelresATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_orientation!=null){
			xmlTxt+="\n<xnat:orientation";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_orientation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:orientation>";
		}
		var Parameters_fovATT = ""
		if (this.Parameters_fov_x!=null)
			Parameters_fovATT+=" x=\"" + this.Parameters_fov_x + "\"";
		if (this.Parameters_fov_y!=null)
			Parameters_fovATT+=" y=\"" + this.Parameters_fov_y + "\"";
		if(Parameters_fovATT!=""){
			xmlTxt+="\n<xnat:fov";
			xmlTxt+=Parameters_fovATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_imagetype!=null){
			xmlTxt+="\n<xnat:imageType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_imagetype.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:imageType>";
		}
		if (this.Parameters_options!=null){
			xmlTxt+="\n<xnat:options";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_options.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:options>";
		}
		if (this.Parameters_derivation!=null){
			xmlTxt+="\n<xnat:derivation";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_derivation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:derivation>";
		}
		if (this.Parameters_contrastbolus!=null){
			xmlTxt+="\n<xnat:contrastBolus";
			xmlTxt+=this.Parameters_contrastbolus.getXMLAtts();
			if(this.Parameters_contrastbolus.xsiType!="xnat:contrastBolus"){
				xmlTxt+=" xsi:type=\"" + this.Parameters_contrastbolus.xsiType + "\"";
			}
			if (this.Parameters_contrastbolus.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Parameters_contrastbolus.getXMLBody(preventComments);
				xmlTxt+="</xnat:contrastBolus>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

				xmlTxt+="\n</xnat:parameters>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Parameters_options!=null) return true;
			if(this.Parameters_fov_y!=null) return true;
			if(this.Parameters_fov_x!=null) return true;
			if(this.Parameters_contrastbolus!=null) return true;
			if(this.Parameters_orientation!=null) return true;
			if(this.Parameters_pixelres_units!=null) return true;
			if(this.Parameters_imagetype!=null) return true;
			if(this.Parameters_pixelres_y!=null) return true;
			if(this.Parameters_pixelres_x!=null) return true;
			if(this.Parameters_derivation!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
