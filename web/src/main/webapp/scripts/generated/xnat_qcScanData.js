/*
 * web: xnat_qcScanData.js
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

function xnat_qcScanData(){
this.xsiType="xnat:qcScanData";

	this.getSchemaElementName=function(){
		return "qcScanData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:qcScanData";
	}

	this.ImagescanId=null;


	function getImagescanId() {
		return this.ImagescanId;
	}
	this.getImagescanId=getImagescanId;


	function setImagescanId(v){
		this.ImagescanId=v;
	}
	this.setImagescanId=setImagescanId;

	this.Rater=null;


	function getRater() {
		return this.Rater;
	}
	this.getRater=getRater;


	function setRater(v){
		this.Rater=v;
	}
	this.setRater=setRater;

	this.Coverage=null;


	function getCoverage() {
		return this.Coverage;
	}
	this.getCoverage=getCoverage;


	function setCoverage(v){
		this.Coverage=v;
	}
	this.setCoverage=setCoverage;

	this.Motion=null;


	function getMotion() {
		return this.Motion;
	}
	this.getMotion=getMotion;


	function setMotion(v){
		this.Motion=v;
	}
	this.setMotion=setMotion;

	this.Otherimageartifacts=null;


	function getOtherimageartifacts() {
		return this.Otherimageartifacts;
	}
	this.getOtherimageartifacts=getOtherimageartifacts;


	function setOtherimageartifacts(v){
		this.Otherimageartifacts=v;
	}
	this.setOtherimageartifacts=setOtherimageartifacts;

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

	this.Rating=null;


	function getRating() {
		return this.Rating;
	}
	this.getRating=getRating;


	function setRating(v){
		this.Rating=v;
	}
	this.setRating=setRating;

	this.Rating_scale=null;


	function getRating_scale() {
		return this.Rating_scale;
	}
	this.getRating_scale=getRating_scale;


	function setRating_scale(v){
		this.Rating_scale=v;
	}
	this.setRating_scale=setRating_scale;
	this.Fields_field =new Array();

	function getFields_field() {
		return this.Fields_field;
	}
	this.getFields_field=getFields_field;


	function addFields_field(v){
		this.Fields_field.push(v);
	}
	this.addFields_field=addFields_field;

	this.XnatQcscandataId=null;


	function getXnatQcscandataId() {
		return this.XnatQcscandataId;
	}
	this.getXnatQcscandataId=getXnatQcscandataId;


	function setXnatQcscandataId(v){
		this.XnatQcscandataId=v;
	}
	this.setXnatQcscandataId=setXnatQcscandataId;

	this.scans_scan_xnat_qcManualAssesso_id_fk=null;


	this.getscans_scan_xnat_qcManualAssesso_id=function() {
		return this.scans_scan_xnat_qcManualAssesso_id_fk;
	}


	this.setscans_scan_xnat_qcManualAssesso_id=function(v){
		this.scans_scan_xnat_qcManualAssesso_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageScan_ID"){
				return this.ImagescanId ;
			} else 
			if(xmlPath=="rater"){
				return this.Rater ;
			} else 
			if(xmlPath=="coverage"){
				return this.Coverage ;
			} else 
			if(xmlPath=="motion"){
				return this.Motion ;
			} else 
			if(xmlPath=="otherImageArtifacts"){
				return this.Otherimageartifacts ;
			} else 
			if(xmlPath=="comments"){
				return this.Comments ;
			} else 
			if(xmlPath=="pass"){
				return this.Pass ;
			} else 
			if(xmlPath=="rating"){
				return this.Rating ;
			} else 
			if(xmlPath=="rating/scale"){
				return this.Rating_scale ;
			} else 
			if(xmlPath=="fields/field"){
				return this.Fields_field ;
			} else 
			if(xmlPath.startsWith("fields/field")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Fields_field ;
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

				for(var whereCount=0;whereCount<this.Fields_field.length;whereCount++){

					var tempValue=this.Fields_field[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Fields_field[whereCount]);

					}

				}
				}else{

				whereArray=this.Fields_field;
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
			if(xmlPath=="extension_item"){
				return this.ExtensionItem ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_qcScanData_id"){
				return this.XnatQcscandataId ;
			} else 
			if(xmlPath=="scans_scan_xnat_qcManualAssesso_id"){
				return this.scans_scan_xnat_qcManualAssesso_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageScan_ID"){
				this.ImagescanId=value;
			} else 
			if(xmlPath=="rater"){
				this.Rater=value;
			} else 
			if(xmlPath=="coverage"){
				this.Coverage=value;
			} else 
			if(xmlPath=="motion"){
				this.Motion=value;
			} else 
			if(xmlPath=="otherImageArtifacts"){
				this.Otherimageartifacts=value;
			} else 
			if(xmlPath=="comments"){
				this.Comments=value;
			} else 
			if(xmlPath=="pass"){
				this.Pass=value;
			} else 
			if(xmlPath=="rating"){
				this.Rating=value;
			} else 
			if(xmlPath=="rating/scale"){
				this.Rating_scale=value;
			} else 
			if(xmlPath=="fields/field"){
				this.Fields_field=value;
			} else 
			if(xmlPath.startsWith("fields/field")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Fields_field ;
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

				for(var whereCount=0;whereCount<this.Fields_field.length;whereCount++){

					var tempValue=this.Fields_field[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Fields_field[whereCount]);

					}

				}
				}else{

				whereArray=this.Fields_field;
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
						newChild= instanciateObject("xnat:qcScanData_field");//omUtils.js
					}
					this.addFields_field(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="extension_item"){
				this.ExtensionItem=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_qcScanData_id"){
				this.XnatQcscandataId=value;
			} else 
			if(xmlPath=="scans_scan_xnat_qcManualAssesso_id"){
				this.scans_scan_xnat_qcManualAssesso_id_fk=value;
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
		if (xmlPath=="fields/field"){
			this.addFields_field(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="fields/field"){
			return "http://nrg.wustl.edu/xnat:qcScanData_field";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="imageScan_ID"){
			return "field_data";
		}else if (xmlPath=="rater"){
			return "field_data";
		}else if (xmlPath=="coverage"){
			return "field_data";
		}else if (xmlPath=="motion"){
			return "field_data";
		}else if (xmlPath=="otherImageArtifacts"){
			return "field_LONG_DATA";
		}else if (xmlPath=="comments"){
			return "field_LONG_DATA";
		}else if (xmlPath=="pass"){
			return "field_data";
		}else if (xmlPath=="rating"){
			return "field_data";
		}else if (xmlPath=="rating/scale"){
			return "field_data";
		}else if (xmlPath=="fields/field"){
			return "field_NO_CHILD";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:qcScanData";
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
		xmlTxt+="\n</xnat:qcScanData>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatQcscandataId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_qcScanData_id=\"" + this.XnatQcscandataId + "\"";
			}
			if(this.scans_scan_xnat_qcManualAssesso_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="scans_scan_xnat_qcManualAssesso_id=\"" + this.scans_scan_xnat_qcManualAssesso_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.ImagescanId!=null){
			xmlTxt+="\n<xnat:imageScan_ID";
			xmlTxt+=">";
			xmlTxt+=this.ImagescanId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:imageScan_ID>";
		}
		if (this.Rater!=null){
			xmlTxt+="\n<xnat:rater";
			xmlTxt+=">";
			xmlTxt+=this.Rater.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:rater>";
		}
		if (this.Coverage!=null){
			xmlTxt+="\n<xnat:coverage";
			xmlTxt+=">";
			xmlTxt+=this.Coverage.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:coverage>";
		}
		if (this.Motion!=null){
			xmlTxt+="\n<xnat:motion";
			xmlTxt+=">";
			xmlTxt+=this.Motion.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:motion>";
		}
		if (this.Otherimageartifacts!=null){
			xmlTxt+="\n<xnat:otherImageArtifacts";
			xmlTxt+=">";
			xmlTxt+=this.Otherimageartifacts.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:otherImageArtifacts>";
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
		var RatingATT = ""
		if (this.Rating_scale!=null)
			RatingATT+=" scale=\"" + this.Rating_scale.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Rating!=null){
			xmlTxt+="\n<xnat:rating";
			xmlTxt+=RatingATT;
			xmlTxt+=">";
			xmlTxt+=this.Rating.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:rating>";
		}
		else if(RatingATT!=""){
			xmlTxt+="\n<xnat:rating";
			xmlTxt+=RatingATT;
			xmlTxt+="/>";
		}

			var child0=0;
			var att0=0;
			child0+=this.Fields_field.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:fields";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Fields_fieldCOUNT=0;Fields_fieldCOUNT<this.Fields_field.length;Fields_fieldCOUNT++){
			xmlTxt +="\n<xnat:field";
			xmlTxt +=this.Fields_field[Fields_fieldCOUNT].getXMLAtts();
			if(this.Fields_field[Fields_fieldCOUNT].xsiType!="xnat:qcScanData_field"){
				xmlTxt+=" xsi:type=\"" + this.Fields_field[Fields_fieldCOUNT].xsiType + "\"";
			}
			if (this.Fields_field[Fields_fieldCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Fields_field[Fields_fieldCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:field>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:fields>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatQcscandataId!=null) return true;
			if (this.scans_scan_xnat_qcManualAssesso_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.ImagescanId!=null) return true;
		if (this.Rater!=null) return true;
		if (this.Coverage!=null) return true;
		if (this.Motion!=null) return true;
		if (this.Otherimageartifacts!=null) return true;
		if (this.Comments!=null) return true;
		if (this.Pass!=null) return true;
		if (this.Rating_scale!=null)
			return true;
		if (this.Rating!=null) return true;
			if(this.Fields_field.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
