/*
 * web: xnat_petQcScanData.js
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

function xnat_petQcScanData(){
this.xsiType="xnat:petQcScanData";

	this.getSchemaElementName=function(){
		return "petQcScanData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:petQcScanData";
	}
this.extension=dynamicJSLoad('xnat_qcScanData','generated/xnat_qcScanData.js');

	this.Acquisition=null;


	function getAcquisition() {
		return this.Acquisition;
	}
	this.getAcquisition=getAcquisition;


	function setAcquisition(v){
		this.Acquisition=v;
	}
	this.setAcquisition=setAcquisition;

	this.Correctreconstructionalgorithm=null;


	function getCorrectreconstructionalgorithm() {
		return this.Correctreconstructionalgorithm;
	}
	this.getCorrectreconstructionalgorithm=getCorrectreconstructionalgorithm;


	function setCorrectreconstructionalgorithm(v){
		this.Correctreconstructionalgorithm=v;
	}
	this.setCorrectreconstructionalgorithm=setCorrectreconstructionalgorithm;

	this.Reconstructionalgorithmused=null;


	function getReconstructionalgorithmused() {
		return this.Reconstructionalgorithmused;
	}
	this.getReconstructionalgorithmused=getReconstructionalgorithmused;


	function setReconstructionalgorithmused(v){
		this.Reconstructionalgorithmused=v;
	}
	this.setReconstructionalgorithmused=setReconstructionalgorithmused;

	this.Correctiterationsandsubsets=null;


	function getCorrectiterationsandsubsets() {
		return this.Correctiterationsandsubsets;
	}
	this.getCorrectiterationsandsubsets=getCorrectiterationsandsubsets;


	function setCorrectiterationsandsubsets(v){
		this.Correctiterationsandsubsets=v;
	}
	this.setCorrectiterationsandsubsets=setCorrectiterationsandsubsets;

	this.Correctfilters=null;


	function getCorrectfilters() {
		return this.Correctfilters;
	}
	this.getCorrectfilters=getCorrectfilters;


	function setCorrectfilters(v){
		this.Correctfilters=v;
	}
	this.setCorrectfilters=setCorrectfilters;

	this.Correctslicethickness=null;


	function getCorrectslicethickness() {
		return this.Correctslicethickness;
	}
	this.getCorrectslicethickness=getCorrectslicethickness;


	function setCorrectslicethickness(v){
		this.Correctslicethickness=v;
	}
	this.setCorrectslicethickness=setCorrectslicethickness;

	this.Acceptablevoxelsize=null;


	function getAcceptablevoxelsize() {
		return this.Acceptablevoxelsize;
	}
	this.getAcceptablevoxelsize=getAcceptablevoxelsize;


	function setAcceptablevoxelsize(v){
		this.Acceptablevoxelsize=v;
	}
	this.setAcceptablevoxelsize=setAcceptablevoxelsize;

	this.Unacceptableframes=null;


	function getUnacceptableframes() {
		return this.Unacceptableframes;
	}
	this.getUnacceptableframes=getUnacceptableframes;


	function setUnacceptableframes(v){
		this.Unacceptableframes=v;
	}
	this.setUnacceptableframes=setUnacceptableframes;

	this.Reasonframesunacceptable=null;


	function getReasonframesunacceptable() {
		return this.Reasonframesunacceptable;
	}
	this.getReasonframesunacceptable=getReasonframesunacceptable;


	function setReasonframesunacceptable(v){
		this.Reasonframesunacceptable=v;
	}
	this.setReasonframesunacceptable=setReasonframesunacceptable;
	this.Processingerrors_processingerror =new Array();

	function getProcessingerrors_processingerror() {
		return this.Processingerrors_processingerror;
	}
	this.getProcessingerrors_processingerror=getProcessingerrors_processingerror;


	function addProcessingerrors_processingerror(v){
		this.Processingerrors_processingerror.push(v);
	}
	this.addProcessingerrors_processingerror=addProcessingerrors_processingerror;

	this.Qcoutcome=null;


	function getQcoutcome() {
		return this.Qcoutcome;
	}
	this.getQcoutcome=getQcoutcome;


	function setQcoutcome(v){
		this.Qcoutcome=v;
	}
	this.setQcoutcome=setQcoutcome;

	this.Qcoutcomereason=null;


	function getQcoutcomereason() {
		return this.Qcoutcomereason;
	}
	this.getQcoutcomereason=getQcoutcomereason;


	function setQcoutcomereason(v){
		this.Qcoutcomereason=v;
	}
	this.setQcoutcomereason=setQcoutcomereason;

	this.Topcutoff=null;


	function getTopcutoff() {
		return this.Topcutoff;
	}
	this.getTopcutoff=getTopcutoff;


	function setTopcutoff(v){
		this.Topcutoff=v;
	}
	this.setTopcutoff=setTopcutoff;

	this.Bottomcutoff=null;


	function getBottomcutoff() {
		return this.Bottomcutoff;
	}
	this.getBottomcutoff=getBottomcutoff;


	function setBottomcutoff(v){
		this.Bottomcutoff=v;
	}
	this.setBottomcutoff=setBottomcutoff;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="qcScanData"){
				return this.Qcscandata ;
			} else 
			if(xmlPath.startsWith("qcScanData")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Qcscandata ;
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
				if(this.Qcscandata!=undefined)return this.Qcscandata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="acquisition"){
				return this.Acquisition ;
			} else 
			if(xmlPath=="correctReconstructionAlgorithm"){
				return this.Correctreconstructionalgorithm ;
			} else 
			if(xmlPath=="reconstructionAlgorithmUsed"){
				return this.Reconstructionalgorithmused ;
			} else 
			if(xmlPath=="correctIterationsAndSubsets"){
				return this.Correctiterationsandsubsets ;
			} else 
			if(xmlPath=="correctFilters"){
				return this.Correctfilters ;
			} else 
			if(xmlPath=="correctSliceThickness"){
				return this.Correctslicethickness ;
			} else 
			if(xmlPath=="acceptableVoxelSize"){
				return this.Acceptablevoxelsize ;
			} else 
			if(xmlPath=="unacceptableFrames"){
				return this.Unacceptableframes ;
			} else 
			if(xmlPath=="reasonFramesUnacceptable"){
				return this.Reasonframesunacceptable ;
			} else 
			if(xmlPath=="processingErrors/processingError"){
				return this.Processingerrors_processingerror ;
			} else 
			if(xmlPath.startsWith("processingErrors/processingError")){
				xmlPath=xmlPath.substring(32);
				if(xmlPath=="")return this.Processingerrors_processingerror ;
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

				for(var whereCount=0;whereCount<this.Processingerrors_processingerror.length;whereCount++){

					var tempValue=this.Processingerrors_processingerror[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Processingerrors_processingerror[whereCount]);

					}

				}
				}else{

				whereArray=this.Processingerrors_processingerror;
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
			if(xmlPath=="qcOutcome"){
				return this.Qcoutcome ;
			} else 
			if(xmlPath=="qcOutcomeReason"){
				return this.Qcoutcomereason ;
			} else 
			if(xmlPath=="topCutoff"){
				return this.Topcutoff ;
			} else 
			if(xmlPath=="bottomCutoff"){
				return this.Bottomcutoff ;
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
			if(xmlPath=="qcScanData"){
				this.Qcscandata=value;
			} else 
			if(xmlPath.startsWith("qcScanData")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Qcscandata ;
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
				if(this.Qcscandata!=undefined){
					this.Qcscandata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Qcscandata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Qcscandata= instanciateObject("xnat:qcScanData");//omUtils.js
						}
						if(options && options.where)this.Qcscandata.setProperty(options.where.field,options.where.value);
						this.Qcscandata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="acquisition"){
				this.Acquisition=value;
			} else 
			if(xmlPath=="correctReconstructionAlgorithm"){
				this.Correctreconstructionalgorithm=value;
			} else 
			if(xmlPath=="reconstructionAlgorithmUsed"){
				this.Reconstructionalgorithmused=value;
			} else 
			if(xmlPath=="correctIterationsAndSubsets"){
				this.Correctiterationsandsubsets=value;
			} else 
			if(xmlPath=="correctFilters"){
				this.Correctfilters=value;
			} else 
			if(xmlPath=="correctSliceThickness"){
				this.Correctslicethickness=value;
			} else 
			if(xmlPath=="acceptableVoxelSize"){
				this.Acceptablevoxelsize=value;
			} else 
			if(xmlPath=="unacceptableFrames"){
				this.Unacceptableframes=value;
			} else 
			if(xmlPath=="reasonFramesUnacceptable"){
				this.Reasonframesunacceptable=value;
			} else 
			if(xmlPath=="processingErrors/processingError"){
				this.Processingerrors_processingerror=value;
			} else 
			if(xmlPath.startsWith("processingErrors/processingError")){
				xmlPath=xmlPath.substring(32);
				if(xmlPath=="")return this.Processingerrors_processingerror ;
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

				for(var whereCount=0;whereCount<this.Processingerrors_processingerror.length;whereCount++){

					var tempValue=this.Processingerrors_processingerror[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Processingerrors_processingerror[whereCount]);

					}

				}
				}else{

				whereArray=this.Processingerrors_processingerror;
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
						newChild= instanciateObject("xnat:petQcScanData_processingError");//omUtils.js
					}
					this.addProcessingerrors_processingerror(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="qcOutcome"){
				this.Qcoutcome=value;
			} else 
			if(xmlPath=="qcOutcomeReason"){
				this.Qcoutcomereason=value;
			} else 
			if(xmlPath=="topCutoff"){
				this.Topcutoff=value;
			} else 
			if(xmlPath=="bottomCutoff"){
				this.Bottomcutoff=value;
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
		if (xmlPath=="processingErrors/processingError"){
			this.addProcessingerrors_processingerror(v);
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
		if (xmlPath=="processingErrors/processingError"){
			return "http://nrg.wustl.edu/xnat:petQcScanData_processingError";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="acquisition"){
			return "field_data";
		}else if (xmlPath=="correctReconstructionAlgorithm"){
			return "field_data";
		}else if (xmlPath=="reconstructionAlgorithmUsed"){
			return "field_data";
		}else if (xmlPath=="correctIterationsAndSubsets"){
			return "field_data";
		}else if (xmlPath=="correctFilters"){
			return "field_data";
		}else if (xmlPath=="correctSliceThickness"){
			return "field_data";
		}else if (xmlPath=="acceptableVoxelSize"){
			return "field_data";
		}else if (xmlPath=="unacceptableFrames"){
			return "field_data";
		}else if (xmlPath=="reasonFramesUnacceptable"){
			return "field_data";
		}else if (xmlPath=="processingErrors/processingError"){
			return "field_inline_repeater";
		}else if (xmlPath=="qcOutcome"){
			return "field_data";
		}else if (xmlPath=="qcOutcomeReason"){
			return "field_data";
		}else if (xmlPath=="topCutoff"){
			return "field_data";
		}else if (xmlPath=="bottomCutoff"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:petQcScanData";
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
		xmlTxt+="\n</xnat:petQcScanData>";
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
		if (this.Acquisition!=null){
			xmlTxt+="\n<xnat:acquisition";
			xmlTxt+=">";
			xmlTxt+=this.Acquisition.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:acquisition>";
		}
		if (this.Correctreconstructionalgorithm!=null){
			xmlTxt+="\n<xnat:correctReconstructionAlgorithm";
			xmlTxt+=">";
			xmlTxt+=this.Correctreconstructionalgorithm.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:correctReconstructionAlgorithm>";
		}
		if (this.Reconstructionalgorithmused!=null){
			xmlTxt+="\n<xnat:reconstructionAlgorithmUsed";
			xmlTxt+=">";
			xmlTxt+=this.Reconstructionalgorithmused.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:reconstructionAlgorithmUsed>";
		}
		if (this.Correctiterationsandsubsets!=null){
			xmlTxt+="\n<xnat:correctIterationsAndSubsets";
			xmlTxt+=">";
			xmlTxt+=this.Correctiterationsandsubsets.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:correctIterationsAndSubsets>";
		}
		if (this.Correctfilters!=null){
			xmlTxt+="\n<xnat:correctFilters";
			xmlTxt+=">";
			xmlTxt+=this.Correctfilters.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:correctFilters>";
		}
		if (this.Correctslicethickness!=null){
			xmlTxt+="\n<xnat:correctSliceThickness";
			xmlTxt+=">";
			xmlTxt+=this.Correctslicethickness.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:correctSliceThickness>";
		}
		if (this.Acceptablevoxelsize!=null){
			xmlTxt+="\n<xnat:acceptableVoxelSize";
			xmlTxt+=">";
			xmlTxt+=this.Acceptablevoxelsize.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:acceptableVoxelSize>";
		}
		if (this.Unacceptableframes!=null){
			xmlTxt+="\n<xnat:unacceptableFrames";
			xmlTxt+=">";
			xmlTxt+=this.Unacceptableframes.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:unacceptableFrames>";
		}
		if (this.Reasonframesunacceptable!=null){
			xmlTxt+="\n<xnat:reasonFramesUnacceptable";
			xmlTxt+=">";
			xmlTxt+=this.Reasonframesunacceptable.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:reasonFramesUnacceptable>";
		}
			var child0=0;
			var att0=0;
			child0+=this.Processingerrors_processingerror.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:processingErrors";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Processingerrors_processingerrorCOUNT=0;Processingerrors_processingerrorCOUNT<this.Processingerrors_processingerror.length;Processingerrors_processingerrorCOUNT++){
			xmlTxt+=this.Processingerrors_processingerror[Processingerrors_processingerrorCOUNT].getXMLBody(preventComments);
		}
				xmlTxt+="\n</xnat:processingErrors>";
			}
			}

		if (this.Qcoutcome!=null){
			xmlTxt+="\n<xnat:qcOutcome";
			xmlTxt+=">";
			xmlTxt+=this.Qcoutcome.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:qcOutcome>";
		}
		if (this.Qcoutcomereason!=null){
			xmlTxt+="\n<xnat:qcOutcomeReason";
			xmlTxt+=">";
			xmlTxt+=this.Qcoutcomereason.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:qcOutcomeReason>";
		}
		if (this.Topcutoff!=null){
			xmlTxt+="\n<xnat:topCutoff";
			xmlTxt+=">";
			xmlTxt+=this.Topcutoff.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:topCutoff>";
		}
		if (this.Bottomcutoff!=null){
			xmlTxt+="\n<xnat:bottomCutoff";
			xmlTxt+=">";
			xmlTxt+=this.Bottomcutoff.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:bottomCutoff>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Acquisition!=null) return true;
		if (this.Correctreconstructionalgorithm!=null) return true;
		if (this.Reconstructionalgorithmused!=null) return true;
		if (this.Correctiterationsandsubsets!=null) return true;
		if (this.Correctfilters!=null) return true;
		if (this.Correctslicethickness!=null) return true;
		if (this.Acceptablevoxelsize!=null) return true;
		if (this.Unacceptableframes!=null) return true;
		if (this.Reasonframesunacceptable!=null) return true;
			if(this.Processingerrors_processingerror.length>0)return true;
		if (this.Qcoutcome!=null) return true;
		if (this.Qcoutcomereason!=null) return true;
		if (this.Topcutoff!=null) return true;
		if (this.Bottomcutoff!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
