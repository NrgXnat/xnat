/*
 * web: xnat_dicomSeries.js
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

function xnat_dicomSeries(){
this.xsiType="xnat:dicomSeries";

	this.getSchemaElementName=function(){
		return "dicomSeries";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:dicomSeries";
	}
this.extension=dynamicJSLoad('xnat_abstractResource','generated/xnat_abstractResource.js');

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
	this.Imageset_image =new Array();

	function getImageset_image() {
		return this.Imageset_image;
	}
	this.getImageset_image=getImageset_image;


	function addImageset_image(v){
		this.Imageset_image.push(v);
	}
	this.addImageset_image=addImageset_image;

	this.Format=null;


	function getFormat() {
		return this.Format;
	}
	this.getFormat=getFormat;


	function setFormat(v){
		this.Format=v;
	}
	this.setFormat=setFormat;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;

	this.Content=null;


	function getContent() {
		return this.Content;
	}
	this.getContent=getContent;


	function setContent(v){
		this.Content=v;
	}
	this.setContent=setContent;

	this.Cachepath=null;


	function getCachepath() {
		return this.Cachepath;
	}
	this.getCachepath=getCachepath;


	function setCachepath(v){
		this.Cachepath=v;
	}
	this.setCachepath=setCachepath;

	this.Uid=null;


	function getUid() {
		return this.Uid;
	}
	this.getUid=getUid;


	function setUid(v){
		this.Uid=v;
	}
	this.setUid=setUid;


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
			if(xmlPath=="imageSet/image"){
				return this.Imageset_image ;
			} else 
			if(xmlPath.startsWith("imageSet/image")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Imageset_image ;
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

				for(var whereCount=0;whereCount<this.Imageset_image.length;whereCount++){

					var tempValue=this.Imageset_image[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Imageset_image[whereCount]);

					}

				}
				}else{

				whereArray=this.Imageset_image;
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
			if(xmlPath=="format"){
				return this.Format ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="content"){
				return this.Content ;
			} else 
			if(xmlPath=="cachePath"){
				return this.Cachepath ;
			} else 
			if(xmlPath=="UID"){
				return this.Uid ;
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
			if(xmlPath=="imageSet/image"){
				this.Imageset_image=value;
			} else 
			if(xmlPath.startsWith("imageSet/image")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Imageset_image ;
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

				for(var whereCount=0;whereCount<this.Imageset_image.length;whereCount++){

					var tempValue=this.Imageset_image[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Imageset_image[whereCount]);

					}

				}
				}else{

				whereArray=this.Imageset_image;
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
						newChild= instanciateObject("xnat:dicomSeries_image");//omUtils.js
					}
					this.addImageset_image(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="format"){
				this.Format=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="content"){
				this.Content=value;
			} else 
			if(xmlPath=="cachePath"){
				this.Cachepath=value;
			} else 
			if(xmlPath=="UID"){
				this.Uid=value;
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
		if (xmlPath=="imageSet/image"){
			this.addImageset_image(v);
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
		if (xmlPath=="imageSet/image"){
			return "http://nrg.wustl.edu/xnat:dicomSeries_image";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
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
		}else if (xmlPath=="imageSet/image"){
			return "field_multi_reference";
		}else if (xmlPath=="format"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_data";
		}else if (xmlPath=="content"){
			return "field_data";
		}else if (xmlPath=="cachePath"){
			return "field_data";
		}else if (xmlPath=="UID"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:dicomSeries";
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
		xmlTxt+="\n</xnat:dicomSeries>";
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
		if (this.Format!=null)
			attTxt+=" format=\"" +this.Format +"\"";
		//NOT REQUIRED FIELD

		if (this.Description!=null)
			attTxt+=" description=\"" +this.Description +"\"";
		//NOT REQUIRED FIELD

		if (this.Content!=null)
			attTxt+=" content=\"" +this.Content +"\"";
		//NOT REQUIRED FIELD

		if (this.Cachepath!=null)
			attTxt+=" cachePath=\"" +this.Cachepath +"\"";
		//NOT REQUIRED FIELD

		if (this.Uid!=null)
			attTxt+=" UID=\"" +this.Uid +"\"";
		else attTxt+=" UID=\"\"";//REQUIRED FIELD

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
			var child0=0;
			var att0=0;
			child0+=this.Imageset_image.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:imageSet";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Imageset_imageCOUNT=0;Imageset_imageCOUNT<this.Imageset_image.length;Imageset_imageCOUNT++){
			xmlTxt +="\n<xnat:image";
			xmlTxt +=this.Imageset_image[Imageset_imageCOUNT].getXMLAtts();
			if(this.Imageset_image[Imageset_imageCOUNT].xsiType!="xnat:dicomSeries_image"){
				xmlTxt+=" xsi:type=\"" + this.Imageset_image[Imageset_imageCOUNT].xsiType + "\"";
			}
			if (this.Imageset_image[Imageset_imageCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Imageset_image[Imageset_imageCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:image>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:imageSet>";
			}
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
			if(this.Imageset_image.length>0)return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
