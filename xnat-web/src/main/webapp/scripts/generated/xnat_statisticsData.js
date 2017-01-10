/*
 * web: xnat_statisticsData.js
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

function xnat_statisticsData(){
this.xsiType="xnat:statisticsData";

	this.getSchemaElementName=function(){
		return "statisticsData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:statisticsData";
	}
this.extension=dynamicJSLoad('xnat_abstractStatistics','generated/xnat_abstractStatistics.js');

	this.Mean=null;


	function getMean() {
		return this.Mean;
	}
	this.getMean=getMean;


	function setMean(v){
		this.Mean=v;
	}
	this.setMean=setMean;

	this.Snr=null;


	function getSnr() {
		return this.Snr;
	}
	this.getSnr=getSnr;


	function setSnr(v){
		this.Snr=v;
	}
	this.setSnr=setSnr;

	this.Min=null;


	function getMin() {
		return this.Min;
	}
	this.getMin=getMin;


	function setMin(v){
		this.Min=v;
	}
	this.setMin=setMin;

	this.Max=null;


	function getMax() {
		return this.Max;
	}
	this.getMax=getMax;


	function setMax(v){
		this.Max=v;
	}
	this.setMax=setMax;

	this.Stddev=null;


	function getStddev() {
		return this.Stddev;
	}
	this.getStddev=getStddev;


	function setStddev(v){
		this.Stddev=v;
	}
	this.setStddev=setStddev;

	this.NoOfVoxels=null;


	function getNoOfVoxels() {
		return this.NoOfVoxels;
	}
	this.getNoOfVoxels=getNoOfVoxels;


	function setNoOfVoxels(v){
		this.NoOfVoxels=v;
	}
	this.setNoOfVoxels=setNoOfVoxels;
	this.Additionalstatistics =new Array();

	function getAdditionalstatistics() {
		return this.Additionalstatistics;
	}
	this.getAdditionalstatistics=getAdditionalstatistics;


	function addAdditionalstatistics(v){
		this.Additionalstatistics.push(v);
	}
	this.addAdditionalstatistics=addAdditionalstatistics;
	this.Addfield =new Array();

	function getAddfield() {
		return this.Addfield;
	}
	this.getAddfield=getAddfield;


	function addAddfield(v){
		this.Addfield.push(v);
	}
	this.addAddfield=addAddfield;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractStatistics"){
				return this.Abstractstatistics ;
			} else 
			if(xmlPath.startsWith("abstractStatistics")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Abstractstatistics ;
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
				if(this.Abstractstatistics!=undefined)return this.Abstractstatistics.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="mean"){
				return this.Mean ;
			} else 
			if(xmlPath=="snr"){
				return this.Snr ;
			} else 
			if(xmlPath=="min"){
				return this.Min ;
			} else 
			if(xmlPath=="max"){
				return this.Max ;
			} else 
			if(xmlPath=="stddev"){
				return this.Stddev ;
			} else 
			if(xmlPath=="no_of_voxels"){
				return this.NoOfVoxels ;
			} else 
			if(xmlPath=="additionalStatistics"){
				return this.Additionalstatistics ;
			} else 
			if(xmlPath.startsWith("additionalStatistics")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Additionalstatistics ;
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

				for(var whereCount=0;whereCount<this.Additionalstatistics.length;whereCount++){

					var tempValue=this.Additionalstatistics[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Additionalstatistics[whereCount]);

					}

				}
				}else{

				whereArray=this.Additionalstatistics;
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
			if(xmlPath=="addField"){
				return this.Addfield ;
			} else 
			if(xmlPath.startsWith("addField")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Addfield ;
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

				for(var whereCount=0;whereCount<this.Addfield.length;whereCount++){

					var tempValue=this.Addfield[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Addfield[whereCount]);

					}

				}
				}else{

				whereArray=this.Addfield;
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
			if(xmlPath=="abstractStatistics"){
				this.Abstractstatistics=value;
			} else 
			if(xmlPath.startsWith("abstractStatistics")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Abstractstatistics ;
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
				if(this.Abstractstatistics!=undefined){
					this.Abstractstatistics.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Abstractstatistics= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Abstractstatistics= instanciateObject("xnat:abstractStatistics");//omUtils.js
						}
						if(options && options.where)this.Abstractstatistics.setProperty(options.where.field,options.where.value);
						this.Abstractstatistics.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="mean"){
				this.Mean=value;
			} else 
			if(xmlPath=="snr"){
				this.Snr=value;
			} else 
			if(xmlPath=="min"){
				this.Min=value;
			} else 
			if(xmlPath=="max"){
				this.Max=value;
			} else 
			if(xmlPath=="stddev"){
				this.Stddev=value;
			} else 
			if(xmlPath=="no_of_voxels"){
				this.NoOfVoxels=value;
			} else 
			if(xmlPath=="additionalStatistics"){
				this.Additionalstatistics=value;
			} else 
			if(xmlPath.startsWith("additionalStatistics")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Additionalstatistics ;
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

				for(var whereCount=0;whereCount<this.Additionalstatistics.length;whereCount++){

					var tempValue=this.Additionalstatistics[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Additionalstatistics[whereCount]);

					}

				}
				}else{

				whereArray=this.Additionalstatistics;
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
						newChild= instanciateObject("xnat:statisticsData_additionalStatistics");//omUtils.js
					}
					this.addAdditionalstatistics(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="addField"){
				this.Addfield=value;
			} else 
			if(xmlPath.startsWith("addField")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Addfield ;
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

				for(var whereCount=0;whereCount<this.Addfield.length;whereCount++){

					var tempValue=this.Addfield[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Addfield[whereCount]);

					}

				}
				}else{

				whereArray=this.Addfield;
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
						newChild= instanciateObject("xnat:statisticsData_addField");//omUtils.js
					}
					this.addAddfield(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
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
		if (xmlPath=="additionalStatistics"){
			this.addAdditionalstatistics(v);
		}else if (xmlPath=="addField"){
			this.addAddfield(v);
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
		if (xmlPath=="additionalStatistics"){
			return "http://nrg.wustl.edu/xnat:statisticsData_additionalStatistics";
		}else if (xmlPath=="addField"){
			return "http://nrg.wustl.edu/xnat:statisticsData_addField";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="mean"){
			return "field_data";
		}else if (xmlPath=="snr"){
			return "field_data";
		}else if (xmlPath=="min"){
			return "field_data";
		}else if (xmlPath=="max"){
			return "field_data";
		}else if (xmlPath=="stddev"){
			return "field_data";
		}else if (xmlPath=="no_of_voxels"){
			return "field_data";
		}else if (xmlPath=="additionalStatistics"){
			return "field_NO_CHILD";
		}else if (xmlPath=="addField"){
			return "field_NO_CHILD";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:statisticsData";
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
		xmlTxt+="\n</xnat:statisticsData>";
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
		if (this.Mean!=null){
			xmlTxt+="\n<xnat:mean";
			xmlTxt+=">";
			xmlTxt+=this.Mean;
			xmlTxt+="</xnat:mean>";
		}
		if (this.Snr!=null){
			xmlTxt+="\n<xnat:snr";
			xmlTxt+=">";
			xmlTxt+=this.Snr;
			xmlTxt+="</xnat:snr>";
		}
		if (this.Min!=null){
			xmlTxt+="\n<xnat:min";
			xmlTxt+=">";
			xmlTxt+=this.Min;
			xmlTxt+="</xnat:min>";
		}
		if (this.Max!=null){
			xmlTxt+="\n<xnat:max";
			xmlTxt+=">";
			xmlTxt+=this.Max;
			xmlTxt+="</xnat:max>";
		}
		if (this.Stddev!=null){
			xmlTxt+="\n<xnat:stddev";
			xmlTxt+=">";
			xmlTxt+=this.Stddev;
			xmlTxt+="</xnat:stddev>";
		}
		if (this.NoOfVoxels!=null){
			xmlTxt+="\n<xnat:no_of_voxels";
			xmlTxt+=">";
			xmlTxt+=this.NoOfVoxels;
			xmlTxt+="</xnat:no_of_voxels>";
		}
		for(var AdditionalstatisticsCOUNT=0;AdditionalstatisticsCOUNT<this.Additionalstatistics.length;AdditionalstatisticsCOUNT++){
			xmlTxt +="\n<xnat:additionalStatistics";
			xmlTxt +=this.Additionalstatistics[AdditionalstatisticsCOUNT].getXMLAtts();
			if(this.Additionalstatistics[AdditionalstatisticsCOUNT].xsiType!="xnat:statisticsData_additionalStatistics"){
				xmlTxt+=" xsi:type=\"" + this.Additionalstatistics[AdditionalstatisticsCOUNT].xsiType + "\"";
			}
			if (this.Additionalstatistics[AdditionalstatisticsCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Additionalstatistics[AdditionalstatisticsCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:additionalStatistics>";
			}else {xmlTxt+="/>";}
		}
		for(var AddfieldCOUNT=0;AddfieldCOUNT<this.Addfield.length;AddfieldCOUNT++){
			xmlTxt +="\n<xnat:addField";
			xmlTxt +=this.Addfield[AddfieldCOUNT].getXMLAtts();
			if(this.Addfield[AddfieldCOUNT].xsiType!="xnat:statisticsData_addField"){
				xmlTxt+=" xsi:type=\"" + this.Addfield[AddfieldCOUNT].xsiType + "\"";
			}
			if (this.Addfield[AddfieldCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Addfield[AddfieldCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:addField>";
			}else {xmlTxt+="/>";}
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Mean!=null) return true;
		if (this.Snr!=null) return true;
		if (this.Min!=null) return true;
		if (this.Max!=null) return true;
		if (this.Stddev!=null) return true;
		if (this.NoOfVoxels!=null) return true;
		if(this.Additionalstatistics.length>0) return true;
		if(this.Addfield.length>0) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
