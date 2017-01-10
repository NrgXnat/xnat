/*
 * web: xnat_optScanData.js
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

function xnat_optScanData(){
this.xsiType="xnat:optScanData";

	this.getSchemaElementName=function(){
		return "optScanData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:optScanData";
	}
this.extension=dynamicJSLoad('xnat_imageScanData','generated/xnat_imageScanData.js');

	this.Parameters_voxelres_units=null;


	function getParameters_voxelres_units() {
		return this.Parameters_voxelres_units;
	}
	this.getParameters_voxelres_units=getParameters_voxelres_units;


	function setParameters_voxelres_units(v){
		this.Parameters_voxelres_units=v;
	}
	this.setParameters_voxelres_units=setParameters_voxelres_units;

	this.Parameters_voxelres_x=null;


	function getParameters_voxelres_x() {
		return this.Parameters_voxelres_x;
	}
	this.getParameters_voxelres_x=getParameters_voxelres_x;


	function setParameters_voxelres_x(v){
		this.Parameters_voxelres_x=v;
	}
	this.setParameters_voxelres_x=setParameters_voxelres_x;

	this.Parameters_voxelres_y=null;


	function getParameters_voxelres_y() {
		return this.Parameters_voxelres_y;
	}
	this.getParameters_voxelres_y=getParameters_voxelres_y;


	function setParameters_voxelres_y(v){
		this.Parameters_voxelres_y=v;
	}
	this.setParameters_voxelres_y=setParameters_voxelres_y;

	this.Parameters_voxelres_z=null;


	function getParameters_voxelres_z() {
		return this.Parameters_voxelres_z;
	}
	this.getParameters_voxelres_z=getParameters_voxelres_z;


	function setParameters_voxelres_z(v){
		this.Parameters_voxelres_z=v;
	}
	this.setParameters_voxelres_z=setParameters_voxelres_z;

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

	this.Parameters_laterality=null;


	function getParameters_laterality() {
		return this.Parameters_laterality;
	}
	this.getParameters_laterality=getParameters_laterality;


	function setParameters_laterality(v){
		this.Parameters_laterality=v;
	}
	this.setParameters_laterality=setParameters_laterality;

	this.Parameters_illuminationWavelength=null;


	function getParameters_illuminationWavelength() {
		return this.Parameters_illuminationWavelength;
	}
	this.getParameters_illuminationWavelength=getParameters_illuminationWavelength;


	function setParameters_illuminationWavelength(v){
		this.Parameters_illuminationWavelength=v;
	}
	this.setParameters_illuminationWavelength=setParameters_illuminationWavelength;

	this.Parameters_illuminationPower=null;


	function getParameters_illuminationPower() {
		return this.Parameters_illuminationPower;
	}
	this.getParameters_illuminationPower=getParameters_illuminationPower;


	function setParameters_illuminationPower(v){
		this.Parameters_illuminationPower=v;
	}
	this.setParameters_illuminationPower=setParameters_illuminationPower;

	this.Parameters_imagetype=null;


	function getParameters_imagetype() {
		return this.Parameters_imagetype;
	}
	this.getParameters_imagetype=getParameters_imagetype;


	function setParameters_imagetype(v){
		this.Parameters_imagetype=v;
	}
	this.setParameters_imagetype=setParameters_imagetype;

	this.Dcmvalidation=null;


	function getDcmvalidation() {
		return this.Dcmvalidation;
	}
	this.getDcmvalidation=getDcmvalidation;


	function setDcmvalidation(v){
		this.Dcmvalidation=v;
	}
	this.setDcmvalidation=setDcmvalidation;

	this.Dcmvalidation_status=null;


	function getDcmvalidation_status() {
		return this.Dcmvalidation_status;
	}
	this.getDcmvalidation_status=getDcmvalidation_status;


	function setDcmvalidation_status(v){
		this.Dcmvalidation_status=v;
	}
	this.setDcmvalidation_status=setDcmvalidation_status;


	this.isDcmvalidation_status=function(defaultValue) {
		if(this.Dcmvalidation_status==null)return defaultValue;
		if(this.Dcmvalidation_status=="1" || this.Dcmvalidation_status==true)return true;
		return false;
	}


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
			if(xmlPath=="parameters/voxelRes/units"){
				return this.Parameters_voxelres_units ;
			} else 
			if(xmlPath=="parameters/voxelRes/x"){
				return this.Parameters_voxelres_x ;
			} else 
			if(xmlPath=="parameters/voxelRes/y"){
				return this.Parameters_voxelres_y ;
			} else 
			if(xmlPath=="parameters/voxelRes/z"){
				return this.Parameters_voxelres_z ;
			} else 
			if(xmlPath=="parameters/fov/x"){
				return this.Parameters_fov_x ;
			} else 
			if(xmlPath=="parameters/fov/y"){
				return this.Parameters_fov_y ;
			} else 
			if(xmlPath=="parameters/laterality"){
				return this.Parameters_laterality ;
			} else 
			if(xmlPath=="parameters/illumination_wavelength"){
				return this.Parameters_illuminationWavelength ;
			} else 
			if(xmlPath=="parameters/illumination_power"){
				return this.Parameters_illuminationPower ;
			} else 
			if(xmlPath=="parameters/imageType"){
				return this.Parameters_imagetype ;
			} else 
			if(xmlPath=="dcmValidation"){
				return this.Dcmvalidation ;
			} else 
			if(xmlPath=="dcmValidation/status"){
				return this.Dcmvalidation_status ;
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
			if(xmlPath=="parameters/voxelRes/units"){
				this.Parameters_voxelres_units=value;
			} else 
			if(xmlPath=="parameters/voxelRes/x"){
				this.Parameters_voxelres_x=value;
			} else 
			if(xmlPath=="parameters/voxelRes/y"){
				this.Parameters_voxelres_y=value;
			} else 
			if(xmlPath=="parameters/voxelRes/z"){
				this.Parameters_voxelres_z=value;
			} else 
			if(xmlPath=="parameters/fov/x"){
				this.Parameters_fov_x=value;
			} else 
			if(xmlPath=="parameters/fov/y"){
				this.Parameters_fov_y=value;
			} else 
			if(xmlPath=="parameters/laterality"){
				this.Parameters_laterality=value;
			} else 
			if(xmlPath=="parameters/illumination_wavelength"){
				this.Parameters_illuminationWavelength=value;
			} else 
			if(xmlPath=="parameters/illumination_power"){
				this.Parameters_illuminationPower=value;
			} else 
			if(xmlPath=="parameters/imageType"){
				this.Parameters_imagetype=value;
			} else 
			if(xmlPath=="dcmValidation"){
				this.Dcmvalidation=value;
			} else 
			if(xmlPath=="dcmValidation/status"){
				this.Dcmvalidation_status=value;
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
		if (xmlPath=="parameters/voxelRes/units"){
			return "field_data";
		}else if (xmlPath=="parameters/voxelRes/x"){
			return "field_data";
		}else if (xmlPath=="parameters/voxelRes/y"){
			return "field_data";
		}else if (xmlPath=="parameters/voxelRes/z"){
			return "field_data";
		}else if (xmlPath=="parameters/fov/x"){
			return "field_data";
		}else if (xmlPath=="parameters/fov/y"){
			return "field_data";
		}else if (xmlPath=="parameters/laterality"){
			return "field_data";
		}else if (xmlPath=="parameters/illumination_wavelength"){
			return "field_data";
		}else if (xmlPath=="parameters/illumination_power"){
			return "field_data";
		}else if (xmlPath=="parameters/imageType"){
			return "field_data";
		}else if (xmlPath=="dcmValidation"){
			return "field_data";
		}else if (xmlPath=="dcmValidation/status"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:OPTScan";
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
		xmlTxt+="\n</xnat:OPTScan>";
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
			if(this.Parameters_voxelres_z!=null)
			child0++;
			if(this.Parameters_voxelres_y!=null)
			child0++;
			if(this.Parameters_voxelres_x!=null)
			child0++;
			if(this.Parameters_fov_y!=null)
			child0++;
			if(this.Parameters_fov_x!=null)
			child0++;
			if(this.Parameters_illuminationPower!=null)
			child0++;
			if(this.Parameters_laterality!=null)
			child0++;
			if(this.Parameters_voxelres_units!=null)
			child0++;
			if(this.Parameters_imagetype!=null)
			child0++;
			if(this.Parameters_illuminationWavelength!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:parameters";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		var Parameters_voxelresATT = ""
		if (this.Parameters_voxelres_units!=null)
			Parameters_voxelresATT+=" units=\"" + this.Parameters_voxelres_units.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Parameters_voxelres_x!=null)
			Parameters_voxelresATT+=" x=\"" + this.Parameters_voxelres_x + "\"";
		if (this.Parameters_voxelres_y!=null)
			Parameters_voxelresATT+=" y=\"" + this.Parameters_voxelres_y + "\"";
		if (this.Parameters_voxelres_z!=null)
			Parameters_voxelresATT+=" z=\"" + this.Parameters_voxelres_z + "\"";
		if(Parameters_voxelresATT!=""){
			xmlTxt+="\n<xnat:voxelRes";
			xmlTxt+=Parameters_voxelresATT;
			xmlTxt+="/>";
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

		if (this.Parameters_laterality!=null){
			xmlTxt+="\n<xnat:laterality";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_laterality.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:laterality>";
		}
		if (this.Parameters_illuminationWavelength!=null){
			xmlTxt+="\n<xnat:illumination_wavelength";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_illuminationWavelength.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:illumination_wavelength>";
		}
		if (this.Parameters_illuminationPower!=null){
			xmlTxt+="\n<xnat:illumination_power";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_illuminationPower.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:illumination_power>";
		}
		if (this.Parameters_imagetype!=null){
			xmlTxt+="\n<xnat:imageType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_imagetype.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:imageType>";
		}
				xmlTxt+="\n</xnat:parameters>";
			}
			}

		var DcmvalidationATT = ""
		if (this.Dcmvalidation_status!=null)
			DcmvalidationATT+=" status=\"" + this.Dcmvalidation_status + "\"";
		if (this.Dcmvalidation!=null){
			xmlTxt+="\n<xnat:dcmValidation";
			xmlTxt+=DcmvalidationATT;
			xmlTxt+=">";
			xmlTxt+=this.Dcmvalidation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:dcmValidation>";
		}
		else if(DcmvalidationATT!=""){
			xmlTxt+="\n<xnat:dcmValidation";
			xmlTxt+=DcmvalidationATT;
			xmlTxt+="/>";
		}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Parameters_voxelres_z!=null) return true;
			if(this.Parameters_voxelres_y!=null) return true;
			if(this.Parameters_voxelres_x!=null) return true;
			if(this.Parameters_fov_y!=null) return true;
			if(this.Parameters_fov_x!=null) return true;
			if(this.Parameters_illuminationPower!=null) return true;
			if(this.Parameters_laterality!=null) return true;
			if(this.Parameters_voxelres_units!=null) return true;
			if(this.Parameters_imagetype!=null) return true;
			if(this.Parameters_illuminationWavelength!=null) return true;
		if (this.Dcmvalidation_status!=null)
			return true;
		if (this.Dcmvalidation!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
