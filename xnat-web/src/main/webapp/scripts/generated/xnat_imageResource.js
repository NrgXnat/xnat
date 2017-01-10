/*
 * web: xnat_imageResource.js
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

function xnat_imageResource(){
this.xsiType="xnat:imageResource";

	this.getSchemaElementName=function(){
		return "imageResource";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:imageResource";
	}
this.extension=dynamicJSLoad('xnat_resource','generated/xnat_resource.js');

	this.Dimensions_x=null;


	function getDimensions_x() {
		return this.Dimensions_x;
	}
	this.getDimensions_x=getDimensions_x;


	function setDimensions_x(v){
		this.Dimensions_x=v;
	}
	this.setDimensions_x=setDimensions_x;

	this.Dimensions_y=null;


	function getDimensions_y() {
		return this.Dimensions_y;
	}
	this.getDimensions_y=getDimensions_y;


	function setDimensions_y(v){
		this.Dimensions_y=v;
	}
	this.setDimensions_y=setDimensions_y;

	this.Dimensions_z=null;


	function getDimensions_z() {
		return this.Dimensions_z;
	}
	this.getDimensions_z=getDimensions_z;


	function setDimensions_z(v){
		this.Dimensions_z=v;
	}
	this.setDimensions_z=setDimensions_z;

	this.Dimensions_volumes=null;


	function getDimensions_volumes() {
		return this.Dimensions_volumes;
	}
	this.getDimensions_volumes=getDimensions_volumes;


	function setDimensions_volumes(v){
		this.Dimensions_volumes=v;
	}
	this.setDimensions_volumes=setDimensions_volumes;

	this.Voxelres_x=null;


	function getVoxelres_x() {
		return this.Voxelres_x;
	}
	this.getVoxelres_x=getVoxelres_x;


	function setVoxelres_x(v){
		this.Voxelres_x=v;
	}
	this.setVoxelres_x=setVoxelres_x;

	this.Voxelres_y=null;


	function getVoxelres_y() {
		return this.Voxelres_y;
	}
	this.getVoxelres_y=getVoxelres_y;


	function setVoxelres_y(v){
		this.Voxelres_y=v;
	}
	this.setVoxelres_y=setVoxelres_y;

	this.Voxelres_z=null;


	function getVoxelres_z() {
		return this.Voxelres_z;
	}
	this.getVoxelres_z=getVoxelres_z;


	function setVoxelres_z(v){
		this.Voxelres_z=v;
	}
	this.setVoxelres_z=setVoxelres_z;

	this.Voxelres_units=null;


	function getVoxelres_units() {
		return this.Voxelres_units;
	}
	this.getVoxelres_units=getVoxelres_units;


	function setVoxelres_units(v){
		this.Voxelres_units=v;
	}
	this.setVoxelres_units=setVoxelres_units;

	this.Orientation=null;


	function getOrientation() {
		return this.Orientation;
	}
	this.getOrientation=getOrientation;


	function setOrientation(v){
		this.Orientation=v;
	}
	this.setOrientation=setOrientation;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="resource"){
				return this.Resource ;
			} else 
			if(xmlPath.startsWith("resource")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Resource ;
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
				if(this.Resource!=undefined)return this.Resource.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="dimensions/x"){
				return this.Dimensions_x ;
			} else 
			if(xmlPath=="dimensions/y"){
				return this.Dimensions_y ;
			} else 
			if(xmlPath=="dimensions/z"){
				return this.Dimensions_z ;
			} else 
			if(xmlPath=="dimensions/volumes"){
				return this.Dimensions_volumes ;
			} else 
			if(xmlPath=="voxelRes/x"){
				return this.Voxelres_x ;
			} else 
			if(xmlPath=="voxelRes/y"){
				return this.Voxelres_y ;
			} else 
			if(xmlPath=="voxelRes/z"){
				return this.Voxelres_z ;
			} else 
			if(xmlPath=="voxelRes/units"){
				return this.Voxelres_units ;
			} else 
			if(xmlPath=="orientation"){
				return this.Orientation ;
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
			if(xmlPath=="resource"){
				this.Resource=value;
			} else 
			if(xmlPath.startsWith("resource")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Resource ;
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
				if(this.Resource!=undefined){
					this.Resource.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Resource= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Resource= instanciateObject("xnat:resource");//omUtils.js
						}
						if(options && options.where)this.Resource.setProperty(options.where.field,options.where.value);
						this.Resource.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="dimensions/x"){
				this.Dimensions_x=value;
			} else 
			if(xmlPath=="dimensions/y"){
				this.Dimensions_y=value;
			} else 
			if(xmlPath=="dimensions/z"){
				this.Dimensions_z=value;
			} else 
			if(xmlPath=="dimensions/volumes"){
				this.Dimensions_volumes=value;
			} else 
			if(xmlPath=="voxelRes/x"){
				this.Voxelres_x=value;
			} else 
			if(xmlPath=="voxelRes/y"){
				this.Voxelres_y=value;
			} else 
			if(xmlPath=="voxelRes/z"){
				this.Voxelres_z=value;
			} else 
			if(xmlPath=="voxelRes/units"){
				this.Voxelres_units=value;
			} else 
			if(xmlPath=="orientation"){
				this.Orientation=value;
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
		if (xmlPath=="dimensions/x"){
			return "field_data";
		}else if (xmlPath=="dimensions/y"){
			return "field_data";
		}else if (xmlPath=="dimensions/z"){
			return "field_data";
		}else if (xmlPath=="dimensions/volumes"){
			return "field_data";
		}else if (xmlPath=="voxelRes/x"){
			return "field_data";
		}else if (xmlPath=="voxelRes/y"){
			return "field_data";
		}else if (xmlPath=="voxelRes/z"){
			return "field_data";
		}else if (xmlPath=="voxelRes/units"){
			return "field_data";
		}else if (xmlPath=="orientation"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:imageResource";
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
		xmlTxt+="\n</xnat:imageResource>";
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
		var DimensionsATT = ""
		if (this.Dimensions_x!=null)
			DimensionsATT+=" x=\"" + this.Dimensions_x + "\"";
		if (this.Dimensions_y!=null)
			DimensionsATT+=" y=\"" + this.Dimensions_y + "\"";
		if (this.Dimensions_z!=null)
			DimensionsATT+=" z=\"" + this.Dimensions_z + "\"";
		if (this.Dimensions_volumes!=null)
			DimensionsATT+=" volumes=\"" + this.Dimensions_volumes + "\"";
		if(DimensionsATT!=""){
			xmlTxt+="\n<xnat:dimensions";
			xmlTxt+=DimensionsATT;
			xmlTxt+="/>";
		}

		var VoxelresATT = ""
		if (this.Voxelres_x!=null)
			VoxelresATT+=" x=\"" + this.Voxelres_x + "\"";
		if (this.Voxelres_y!=null)
			VoxelresATT+=" y=\"" + this.Voxelres_y + "\"";
		if (this.Voxelres_z!=null)
			VoxelresATT+=" z=\"" + this.Voxelres_z + "\"";
		if (this.Voxelres_units!=null)
			VoxelresATT+=" units=\"" + this.Voxelres_units.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if(VoxelresATT!=""){
			xmlTxt+="\n<xnat:voxelRes";
			xmlTxt+=VoxelresATT;
			xmlTxt+="/>";
		}

		if (this.Orientation!=null){
			xmlTxt+="\n<xnat:orientation";
			xmlTxt+=">";
			xmlTxt+=this.Orientation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:orientation>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Dimensions_x!=null)
			return true;
		if (this.Dimensions_y!=null)
			return true;
		if (this.Dimensions_z!=null)
			return true;
		if (this.Dimensions_volumes!=null)
			return true;
		if (this.Voxelres_x!=null)
			return true;
		if (this.Voxelres_y!=null)
			return true;
		if (this.Voxelres_z!=null)
			return true;
		if (this.Voxelres_units!=null)
			return true;
		if (this.Orientation!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
