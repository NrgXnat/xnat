/*
 * web: xnat_volumetricRegion.js
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

function xnat_volumetricRegion(){
this.xsiType="xnat:volumetricRegion";

	this.getSchemaElementName=function(){
		return "volumetricRegion";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:volumetricRegion";
	}
	this.Subregions_subregion =new Array();

	function getSubregions_subregion() {
		return this.Subregions_subregion;
	}
	this.getSubregions_subregion=getSubregions_subregion;


	function addSubregions_subregion(v){
		this.Subregions_subregion.push(v);
	}
	this.addSubregions_subregion=addSubregions_subregion;

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.Units=null;


	function getUnits() {
		return this.Units;
	}
	this.getUnits=getUnits;


	function setUnits(v){
		this.Units=v;
	}
	this.setUnits=setUnits;

	this.Voxels=null;


	function getVoxels() {
		return this.Voxels;
	}
	this.getVoxels=getVoxels;


	function setVoxels(v){
		this.Voxels=v;
	}
	this.setVoxels=setVoxels;

	this.Hemisphere=null;


	function getHemisphere() {
		return this.Hemisphere;
	}
	this.getHemisphere=getHemisphere;


	function setHemisphere(v){
		this.Hemisphere=v;
	}
	this.setHemisphere=setHemisphere;

	this.XnatVolumetricregionId=null;


	function getXnatVolumetricregionId() {
		return this.XnatVolumetricregionId;
	}
	this.getXnatVolumetricregionId=getXnatVolumetricregionId;


	function setXnatVolumetricregionId(v){
		this.XnatVolumetricregionId=v;
	}
	this.setXnatVolumetricregionId=setXnatVolumetricregionId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="subregions/subregion"){
				return this.Subregions_subregion ;
			} else 
			if(xmlPath.startsWith("subregions/subregion")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Subregions_subregion ;
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

				for(var whereCount=0;whereCount<this.Subregions_subregion.length;whereCount++){

					var tempValue=this.Subregions_subregion[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Subregions_subregion[whereCount]);

					}

				}
				}else{

				whereArray=this.Subregions_subregion;
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
			if(xmlPath=="units"){
				return this.Units ;
			} else 
			if(xmlPath=="voxels"){
				return this.Voxels ;
			} else 
			if(xmlPath=="hemisphere"){
				return this.Hemisphere ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_volumetricRegion_id"){
				return this.XnatVolumetricregionId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="subregions/subregion"){
				this.Subregions_subregion=value;
			} else 
			if(xmlPath.startsWith("subregions/subregion")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Subregions_subregion ;
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

				for(var whereCount=0;whereCount<this.Subregions_subregion.length;whereCount++){

					var tempValue=this.Subregions_subregion[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Subregions_subregion[whereCount]);

					}

				}
				}else{

				whereArray=this.Subregions_subregion;
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
						newChild= instanciateObject("xnat:volumetricRegion_subregion");//omUtils.js
					}
					this.addSubregions_subregion(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="units"){
				this.Units=value;
			} else 
			if(xmlPath=="voxels"){
				this.Voxels=value;
			} else 
			if(xmlPath=="hemisphere"){
				this.Hemisphere=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_volumetricRegion_id"){
				this.XnatVolumetricregionId=value;
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
		if (xmlPath=="subregions/subregion"){
			this.addSubregions_subregion(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="subregions/subregion"){
			return "http://nrg.wustl.edu/xnat:volumetricRegion_subregion";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="subregions/subregion"){
			return "field_multi_reference";
		}else if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="units"){
			return "field_data";
		}else if (xmlPath=="voxels"){
			return "field_data";
		}else if (xmlPath=="hemisphere"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:volumetricRegion";
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
		xmlTxt+="\n</xnat:volumetricRegion>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatVolumetricregionId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_volumetricRegion_id=\"" + this.XnatVolumetricregionId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		//NOT REQUIRED FIELD

		if (this.Units!=null)
			attTxt+=" units=\"" +this.Units +"\"";
		//NOT REQUIRED FIELD

		if (this.Voxels!=null)
			attTxt+=" voxels=\"" +this.Voxels +"\"";
		//NOT REQUIRED FIELD

		if (this.Hemisphere!=null)
			attTxt+=" hemisphere=\"" +this.Hemisphere +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Subregions_subregion.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:subregions";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Subregions_subregionCOUNT=0;Subregions_subregionCOUNT<this.Subregions_subregion.length;Subregions_subregionCOUNT++){
			xmlTxt +="\n<xnat:subregion";
			xmlTxt +=this.Subregions_subregion[Subregions_subregionCOUNT].getXMLAtts();
			if(this.Subregions_subregion[Subregions_subregionCOUNT].xsiType!="xnat:volumetricRegion_subregion"){
				xmlTxt+=" xsi:type=\"" + this.Subregions_subregion[Subregions_subregionCOUNT].xsiType + "\"";
			}
			if (this.Subregions_subregion[Subregions_subregionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Subregions_subregion[Subregions_subregionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:subregion>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:subregions>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatVolumetricregionId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.Subregions_subregion.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
