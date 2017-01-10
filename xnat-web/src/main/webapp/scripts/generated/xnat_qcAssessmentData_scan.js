/*
 * web: xnat_qcAssessmentData_scan.js
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

function xnat_qcAssessmentData_scan(){
this.xsiType="xnat:qcAssessmentData_scan";

	this.getSchemaElementName=function(){
		return "qcAssessmentData_scan";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:qcAssessmentData_scan";
	}
	this.Sliceqc_slice =new Array();

	function getSliceqc_slice() {
		return this.Sliceqc_slice;
	}
	this.getSliceqc_slice=getSliceqc_slice;


	function addSliceqc_slice(v){
		this.Sliceqc_slice.push(v);
	}
	this.addSliceqc_slice=addSliceqc_slice;
	this.Scanstatistics =null;
	function getScanstatistics() {
		return this.Scanstatistics;
	}
	this.getScanstatistics=getScanstatistics;


	function setScanstatistics(v){
		this.Scanstatistics =v;
	}
	this.setScanstatistics=setScanstatistics;

	this.Scanstatistics_ScanstatisticsXnatAbstractstatisticsId=null;


	function getScanstatistics_ScanstatisticsXnatAbstractstatisticsId(){
		return this.Scanstatistics_ScanstatisticsXnatAbstractstatisticsId;
	}
	this.getScanstatistics_ScanstatisticsXnatAbstractstatisticsId=getScanstatistics_ScanstatisticsXnatAbstractstatisticsId;


	function setScanstatistics_ScanstatisticsXnatAbstractstatisticsId(v){
		this.Scanstatistics_ScanstatisticsXnatAbstractstatisticsId=v;
	}
	this.setScanstatistics_ScanstatisticsXnatAbstractstatisticsId=setScanstatistics_ScanstatisticsXnatAbstractstatisticsId;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.XnatQcassessmentdataScanId=null;


	function getXnatQcassessmentdataScanId() {
		return this.XnatQcassessmentdataScanId;
	}
	this.getXnatQcassessmentdataScanId=getXnatQcassessmentdataScanId;


	function setXnatQcassessmentdataScanId(v){
		this.XnatQcassessmentdataScanId=v;
	}
	this.setXnatQcassessmentdataScanId=setXnatQcassessmentdataScanId;

	this.scans_scan_xnat_qcAssessmentDat_id_fk=null;


	this.getscans_scan_xnat_qcAssessmentDat_id=function() {
		return this.scans_scan_xnat_qcAssessmentDat_id_fk;
	}


	this.setscans_scan_xnat_qcAssessmentDat_id=function(v){
		this.scans_scan_xnat_qcAssessmentDat_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="sliceQC/slice"){
				return this.Sliceqc_slice ;
			} else 
			if(xmlPath.startsWith("sliceQC/slice")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Sliceqc_slice ;
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

				for(var whereCount=0;whereCount<this.Sliceqc_slice.length;whereCount++){

					var tempValue=this.Sliceqc_slice[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Sliceqc_slice[whereCount]);

					}

				}
				}else{

				whereArray=this.Sliceqc_slice;
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
			if(xmlPath=="scanStatistics"){
				return this.Scanstatistics ;
			} else 
			if(xmlPath.startsWith("scanStatistics")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Scanstatistics ;
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
				if(this.Scanstatistics!=undefined)return this.Scanstatistics.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="id"){
				return this.Id ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_qcAssessmentData_scan_id"){
				return this.XnatQcassessmentdataScanId ;
			} else 
			if(xmlPath=="scans_scan_xnat_qcAssessmentDat_id"){
				return this.scans_scan_xnat_qcAssessmentDat_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="sliceQC/slice"){
				this.Sliceqc_slice=value;
			} else 
			if(xmlPath.startsWith("sliceQC/slice")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Sliceqc_slice ;
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

				for(var whereCount=0;whereCount<this.Sliceqc_slice.length;whereCount++){

					var tempValue=this.Sliceqc_slice[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Sliceqc_slice[whereCount]);

					}

				}
				}else{

				whereArray=this.Sliceqc_slice;
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
						newChild= instanciateObject("xnat:qcAssessmentData_scan_slice");//omUtils.js
					}
					this.addSliceqc_slice(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="scanStatistics"){
				this.Scanstatistics=value;
			} else 
			if(xmlPath.startsWith("scanStatistics")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Scanstatistics ;
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
				if(this.Scanstatistics!=undefined){
					this.Scanstatistics.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Scanstatistics= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Scanstatistics= instanciateObject("xnat:abstractStatistics");//omUtils.js
						}
						if(options && options.where)this.Scanstatistics.setProperty(options.where.field,options.where.value);
						this.Scanstatistics.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="id"){
				this.Id=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_qcAssessmentData_scan_id"){
				this.XnatQcassessmentdataScanId=value;
			} else 
			if(xmlPath=="scans_scan_xnat_qcAssessmentDat_id"){
				this.scans_scan_xnat_qcAssessmentDat_id_fk=value;
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
		if (xmlPath=="sliceQC/slice"){
			this.addSliceqc_slice(v);
		}else if (xmlPath=="scanStatistics"){
			this.setScanstatistics(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="sliceQC/slice"){
			return "http://nrg.wustl.edu/xnat:qcAssessmentData_scan_slice";
		}else if (xmlPath=="scanStatistics"){
			return "http://nrg.wustl.edu/xnat:abstractStatistics";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="sliceQC/slice"){
			return "field_multi_reference";
		}else if (xmlPath=="scanStatistics"){
			return "field_single_reference";
		}else if (xmlPath=="id"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:qcAssessmentData_scan";
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
		xmlTxt+="\n</xnat:qcAssessmentData_scan>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatQcassessmentdataScanId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_qcAssessmentData_scan_id=\"" + this.XnatQcassessmentdataScanId + "\"";
			}
			if(this.scans_scan_xnat_qcAssessmentDat_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="scans_scan_xnat_qcAssessmentDat_id=\"" + this.scans_scan_xnat_qcAssessmentDat_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Id!=null)
			attTxt+=" id=\"" +this.Id +"\"";
		else attTxt+=" id=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Sliceqc_slice.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:sliceQC";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Sliceqc_sliceCOUNT=0;Sliceqc_sliceCOUNT<this.Sliceqc_slice.length;Sliceqc_sliceCOUNT++){
			xmlTxt +="\n<xnat:slice";
			xmlTxt +=this.Sliceqc_slice[Sliceqc_sliceCOUNT].getXMLAtts();
			if(this.Sliceqc_slice[Sliceqc_sliceCOUNT].xsiType!="xnat:qcAssessmentData_scan_slice"){
				xmlTxt+=" xsi:type=\"" + this.Sliceqc_slice[Sliceqc_sliceCOUNT].xsiType + "\"";
			}
			if (this.Sliceqc_slice[Sliceqc_sliceCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Sliceqc_slice[Sliceqc_sliceCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:slice>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:sliceQC>";
			}
			}

		if (this.Scanstatistics!=null){
			xmlTxt+="\n<xnat:scanStatistics";
			xmlTxt+=this.Scanstatistics.getXMLAtts();
			if(this.Scanstatistics.xsiType!="xnat:abstractStatistics"){
				xmlTxt+=" xsi:type=\"" + this.Scanstatistics.xsiType + "\"";
			}
			if (this.Scanstatistics.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Scanstatistics.getXMLBody(preventComments);
				xmlTxt+="</xnat:scanStatistics>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatQcassessmentdataScanId!=null) return true;
			if (this.scans_scan_xnat_qcAssessmentDat_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.Sliceqc_slice.length>0)return true;
		if (this.Scanstatistics!=null){
			if (this.Scanstatistics.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if(this.hasXMLComments())return true;
		return false;
	}
}
