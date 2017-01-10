/*
 * web: xnat_qcManualAssessorData.js
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

function xnat_qcManualAssessorData(){
this.xsiType="xnat:qcManualAssessorData";

	this.getSchemaElementName=function(){
		return "qcManualAssessorData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:qcManualAssessorData";
	}
this.extension=dynamicJSLoad('xnat_imageAssessorData','generated/xnat_imageAssessorData.js');

	this.Rater=null;


	function getRater() {
		return this.Rater;
	}
	this.getRater=getRater;


	function setRater(v){
		this.Rater=v;
	}
	this.setRater=setRater;

	this.Stereotacticmarker=null;


	function getStereotacticmarker() {
		return this.Stereotacticmarker;
	}
	this.getStereotacticmarker=getStereotacticmarker;


	function setStereotacticmarker(v){
		this.Stereotacticmarker=v;
	}
	this.setStereotacticmarker=setStereotacticmarker;

	this.Incidentalfindings=null;


	function getIncidentalfindings() {
		return this.Incidentalfindings;
	}
	this.getIncidentalfindings=getIncidentalfindings;


	function setIncidentalfindings(v){
		this.Incidentalfindings=v;
	}
	this.setIncidentalfindings=setIncidentalfindings;
	this.Scans_scan =new Array();

	function getScans_scan() {
		return this.Scans_scan;
	}
	this.getScans_scan=getScans_scan;


	function addScans_scan(v){
		this.Scans_scan.push(v);
	}
	this.addScans_scan=addScans_scan;

	this.Comments=null;


	function getComments() {
		return this.Comments;
	}
	this.getComments=getComments;


	function setComments(v){
		this.Comments=v;
	}
	this.setComments=setComments;

	this.Pass=null;


	function getPass() {
		return this.Pass;
	}
	this.getPass=getPass;


	function setPass(v){
		this.Pass=v;
	}
	this.setPass=setPass;

	this.Payable=null;


	function getPayable() {
		return this.Payable;
	}
	this.getPayable=getPayable;


	function setPayable(v){
		this.Payable=v;
	}
	this.setPayable=setPayable;

	this.Rescan=null;


	function getRescan() {
		return this.Rescan;
	}
	this.getRescan=getRescan;


	function setRescan(v){
		this.Rescan=v;
	}
	this.setRescan=setRescan;

	this.Resolvable=null;


	function getResolvable() {
		return this.Resolvable;
	}
	this.getResolvable=getResolvable;


	function setResolvable(v){
		this.Resolvable=v;
	}
	this.setResolvable=setResolvable;

	this.Retrain=null;


	function getRetrain() {
		return this.Retrain;
	}
	this.getRetrain=getRetrain;


	function setRetrain(v){
		this.Retrain=v;
	}
	this.setRetrain=setRetrain;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageAssessorData"){
				return this.Imageassessordata ;
			} else 
			if(xmlPath.startsWith("imageAssessorData")){
				xmlPath=xmlPath.substring(17);
				if(xmlPath=="")return this.Imageassessordata ;
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
				if(this.Imageassessordata!=undefined)return this.Imageassessordata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="rater"){
				return this.Rater ;
			} else 
			if(xmlPath=="stereotacticMarker"){
				return this.Stereotacticmarker ;
			} else 
			if(xmlPath=="incidentalFindings"){
				return this.Incidentalfindings ;
			} else 
			if(xmlPath=="scans/scan"){
				return this.Scans_scan ;
			} else 
			if(xmlPath.startsWith("scans/scan")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Scans_scan ;
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

				for(var whereCount=0;whereCount<this.Scans_scan.length;whereCount++){

					var tempValue=this.Scans_scan[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Scans_scan[whereCount]);

					}

				}
				}else{

				whereArray=this.Scans_scan;
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
			if(xmlPath=="comments"){
				return this.Comments ;
			} else 
			if(xmlPath=="pass"){
				return this.Pass ;
			} else 
			if(xmlPath=="payable"){
				return this.Payable ;
			} else 
			if(xmlPath=="rescan"){
				return this.Rescan ;
			} else 
			if(xmlPath=="resolvable"){
				return this.Resolvable ;
			} else 
			if(xmlPath=="retrain"){
				return this.Retrain ;
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
			if(xmlPath=="imageAssessorData"){
				this.Imageassessordata=value;
			} else 
			if(xmlPath.startsWith("imageAssessorData")){
				xmlPath=xmlPath.substring(17);
				if(xmlPath=="")return this.Imageassessordata ;
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
				if(this.Imageassessordata!=undefined){
					this.Imageassessordata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Imageassessordata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Imageassessordata= instanciateObject("xnat:imageAssessorData");//omUtils.js
						}
						if(options && options.where)this.Imageassessordata.setProperty(options.where.field,options.where.value);
						this.Imageassessordata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="rater"){
				this.Rater=value;
			} else 
			if(xmlPath=="stereotacticMarker"){
				this.Stereotacticmarker=value;
			} else 
			if(xmlPath=="incidentalFindings"){
				this.Incidentalfindings=value;
			} else 
			if(xmlPath=="scans/scan"){
				this.Scans_scan=value;
			} else 
			if(xmlPath.startsWith("scans/scan")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Scans_scan ;
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

				for(var whereCount=0;whereCount<this.Scans_scan.length;whereCount++){

					var tempValue=this.Scans_scan[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Scans_scan[whereCount]);

					}

				}
				}else{

				whereArray=this.Scans_scan;
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
						newChild= instanciateObject("xnat:qcScanData");//omUtils.js
					}
					this.addScans_scan(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="comments"){
				this.Comments=value;
			} else 
			if(xmlPath=="pass"){
				this.Pass=value;
			} else 
			if(xmlPath=="payable"){
				this.Payable=value;
			} else 
			if(xmlPath=="rescan"){
				this.Rescan=value;
			} else 
			if(xmlPath=="resolvable"){
				this.Resolvable=value;
			} else 
			if(xmlPath=="retrain"){
				this.Retrain=value;
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
		if (xmlPath=="scans/scan"){
			this.addScans_scan(v);
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
		if (xmlPath=="scans/scan"){
			return "http://nrg.wustl.edu/xnat:qcScanData";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="rater"){
			return "field_data";
		}else if (xmlPath=="stereotacticMarker"){
			return "field_data";
		}else if (xmlPath=="incidentalFindings"){
			return "field_LONG_DATA";
		}else if (xmlPath=="scans/scan"){
			return "field_multi_reference";
		}else if (xmlPath=="comments"){
			return "field_LONG_DATA";
		}else if (xmlPath=="pass"){
			return "field_data";
		}else if (xmlPath=="payable"){
			return "field_data";
		}else if (xmlPath=="rescan"){
			return "field_data";
		}else if (xmlPath=="resolvable"){
			return "field_data";
		}else if (xmlPath=="retrain"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:QCManualAssessment";
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
		xmlTxt+="\n</xnat:QCManualAssessment>";
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
		if (this.Rater!=null){
			xmlTxt+="\n<xnat:rater";
			xmlTxt+=">";
			xmlTxt+=this.Rater.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:rater>";
		}
		if (this.Stereotacticmarker!=null){
			xmlTxt+="\n<xnat:stereotacticMarker";
			xmlTxt+=">";
			xmlTxt+=this.Stereotacticmarker.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:stereotacticMarker>";
		}
		if (this.Incidentalfindings!=null){
			xmlTxt+="\n<xnat:incidentalFindings";
			xmlTxt+=">";
			xmlTxt+=this.Incidentalfindings.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:incidentalFindings>";
		}
			var child0=0;
			var att0=0;
			child0+=this.Scans_scan.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:scans";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Scans_scanCOUNT=0;Scans_scanCOUNT<this.Scans_scan.length;Scans_scanCOUNT++){
			xmlTxt +="\n<xnat:scan";
			xmlTxt +=this.Scans_scan[Scans_scanCOUNT].getXMLAtts();
			if(this.Scans_scan[Scans_scanCOUNT].xsiType!="xnat:qcScanData"){
				xmlTxt+=" xsi:type=\"" + this.Scans_scan[Scans_scanCOUNT].xsiType + "\"";
			}
			if (this.Scans_scan[Scans_scanCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Scans_scan[Scans_scanCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:scan>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:scans>";
			}
			}

		if (this.Comments!=null){
			xmlTxt+="\n<xnat:comments";
			xmlTxt+=">";
			xmlTxt+=this.Comments.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:comments>";
		}
		if (this.Pass!=null){
			xmlTxt+="\n<xnat:pass";
			xmlTxt+=">";
			xmlTxt+=this.Pass.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:pass>";
		}
		if (this.Payable!=null){
			xmlTxt+="\n<xnat:payable";
			xmlTxt+=">";
			xmlTxt+=this.Payable.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:payable>";
		}
		if (this.Rescan!=null){
			xmlTxt+="\n<xnat:rescan";
			xmlTxt+=">";
			xmlTxt+=this.Rescan.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:rescan>";
		}
		if (this.Resolvable!=null){
			xmlTxt+="\n<xnat:resolvable";
			xmlTxt+=">";
			xmlTxt+=this.Resolvable.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:resolvable>";
		}
		if (this.Retrain!=null){
			xmlTxt+="\n<xnat:retrain";
			xmlTxt+=">";
			xmlTxt+=this.Retrain.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:retrain>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Rater!=null) return true;
		if (this.Stereotacticmarker!=null) return true;
		if (this.Incidentalfindings!=null) return true;
			if(this.Scans_scan.length>0)return true;
		if (this.Comments!=null) return true;
		if (this.Pass!=null) return true;
		if (this.Payable!=null) return true;
		if (this.Rescan!=null) return true;
		if (this.Resolvable!=null) return true;
		if (this.Retrain!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
