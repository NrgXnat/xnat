/*
 * web: xnat_imageSessionData.js
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

function xnat_imageSessionData(){
this.xsiType="xnat:imageSessionData";

	this.getSchemaElementName=function(){
		return "imageSessionData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:imageSessionData";
	}
this.extension=dynamicJSLoad('xnat_subjectAssessorData','generated/xnat_subjectAssessorData.js');
	this.Regions_region =new Array();

	function getRegions_region() {
		return this.Regions_region;
	}
	this.getRegions_region=getRegions_region;


	function addRegions_region(v){
		this.Regions_region.push(v);
	}
	this.addRegions_region=addRegions_region;

	this.Scanner=null;


	function getScanner() {
		return this.Scanner;
	}
	this.getScanner=getScanner;


	function setScanner(v){
		this.Scanner=v;
	}
	this.setScanner=setScanner;

	this.Scanner_manufacturer=null;


	function getScanner_manufacturer() {
		return this.Scanner_manufacturer;
	}
	this.getScanner_manufacturer=getScanner_manufacturer;


	function setScanner_manufacturer(v){
		this.Scanner_manufacturer=v;
	}
	this.setScanner_manufacturer=setScanner_manufacturer;

	this.Scanner_model=null;


	function getScanner_model() {
		return this.Scanner_model;
	}
	this.getScanner_model=getScanner_model;


	function setScanner_model(v){
		this.Scanner_model=v;
	}
	this.setScanner_model=setScanner_model;

	this.Operator=null;


	function getOperator() {
		return this.Operator;
	}
	this.getOperator=getOperator;


	function setOperator(v){
		this.Operator=v;
	}
	this.setOperator=setOperator;

	this.Prearchivepath=null;


	function getPrearchivepath() {
		return this.Prearchivepath;
	}
	this.getPrearchivepath=getPrearchivepath;


	function setPrearchivepath(v){
		this.Prearchivepath=v;
	}
	this.setPrearchivepath=setPrearchivepath;
	this.Scans_scan =new Array();

	function getScans_scan() {
		return this.Scans_scan;
	}
	this.getScans_scan=getScans_scan;


	function addScans_scan(v){
		this.Scans_scan.push(v);
	}
	this.addScans_scan=addScans_scan;
	this.Reconstructions_reconstructedimage =new Array();

	function getReconstructions_reconstructedimage() {
		return this.Reconstructions_reconstructedimage;
	}
	this.getReconstructions_reconstructedimage=getReconstructions_reconstructedimage;


	function addReconstructions_reconstructedimage(v){
		this.Reconstructions_reconstructedimage.push(v);
	}
	this.addReconstructions_reconstructedimage=addReconstructions_reconstructedimage;
	this.Assessors_assessor =new Array();

	function getAssessors_assessor() {
		return this.Assessors_assessor;
	}
	this.getAssessors_assessor=getAssessors_assessor;


	function addAssessors_assessor(v){
		this.Assessors_assessor.push(v);
	}
	this.addAssessors_assessor=addAssessors_assessor;

	this.Dcmaccessionnumber=null;


	function getDcmaccessionnumber() {
		return this.Dcmaccessionnumber;
	}
	this.getDcmaccessionnumber=getDcmaccessionnumber;


	function setDcmaccessionnumber(v){
		this.Dcmaccessionnumber=v;
	}
	this.setDcmaccessionnumber=setDcmaccessionnumber;

	this.Dcmpatientid=null;


	function getDcmpatientid() {
		return this.Dcmpatientid;
	}
	this.getDcmpatientid=getDcmpatientid;


	function setDcmpatientid(v){
		this.Dcmpatientid=v;
	}
	this.setDcmpatientid=setDcmpatientid;

	this.Dcmpatientname=null;


	function getDcmpatientname() {
		return this.Dcmpatientname;
	}
	this.getDcmpatientname=getDcmpatientname;


	function setDcmpatientname(v){
		this.Dcmpatientname=v;
	}
	this.setDcmpatientname=setDcmpatientname;

	this.Dcmpatientbirthdate=null;


	function getDcmpatientbirthdate() {
		return this.Dcmpatientbirthdate;
	}
	this.getDcmpatientbirthdate=getDcmpatientbirthdate;


	function setDcmpatientbirthdate(v){
		this.Dcmpatientbirthdate=v;
	}
	this.setDcmpatientbirthdate=setDcmpatientbirthdate;

	this.SessionType=null;


	function getSessionType() {
		return this.SessionType;
	}
	this.getSessionType=getSessionType;


	function setSessionType(v){
		this.SessionType=v;
	}
	this.setSessionType=setSessionType;

	this.Modality=null;


	function getModality() {
		return this.Modality;
	}
	this.getModality=getModality;


	function setModality(v){
		this.Modality=v;
	}
	this.setModality=setModality;

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
			if(xmlPath=="subjectAssessorData"){
				return this.Subjectassessordata ;
			} else 
			if(xmlPath.startsWith("subjectAssessorData")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Subjectassessordata ;
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
				if(this.Subjectassessordata!=undefined)return this.Subjectassessordata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="regions/region"){
				return this.Regions_region ;
			} else 
			if(xmlPath.startsWith("regions/region")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Regions_region ;
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

				for(var whereCount=0;whereCount<this.Regions_region.length;whereCount++){

					var tempValue=this.Regions_region[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Regions_region[whereCount]);

					}

				}
				}else{

				whereArray=this.Regions_region;
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
			if(xmlPath=="scanner"){
				return this.Scanner ;
			} else 
			if(xmlPath=="scanner/manufacturer"){
				return this.Scanner_manufacturer ;
			} else 
			if(xmlPath=="scanner/model"){
				return this.Scanner_model ;
			} else 
			if(xmlPath=="operator"){
				return this.Operator ;
			} else 
			if(xmlPath=="prearchivePath"){
				return this.Prearchivepath ;
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
			if(xmlPath=="reconstructions/reconstructedImage"){
				return this.Reconstructions_reconstructedimage ;
			} else 
			if(xmlPath.startsWith("reconstructions/reconstructedImage")){
				xmlPath=xmlPath.substring(34);
				if(xmlPath=="")return this.Reconstructions_reconstructedimage ;
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

				for(var whereCount=0;whereCount<this.Reconstructions_reconstructedimage.length;whereCount++){

					var tempValue=this.Reconstructions_reconstructedimage[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Reconstructions_reconstructedimage[whereCount]);

					}

				}
				}else{

				whereArray=this.Reconstructions_reconstructedimage;
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
			if(xmlPath=="assessors/assessor"){
				return this.Assessors_assessor ;
			} else 
			if(xmlPath.startsWith("assessors/assessor")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Assessors_assessor ;
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

				for(var whereCount=0;whereCount<this.Assessors_assessor.length;whereCount++){

					var tempValue=this.Assessors_assessor[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Assessors_assessor[whereCount]);

					}

				}
				}else{

				whereArray=this.Assessors_assessor;
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
			if(xmlPath=="dcmAccessionNumber"){
				return this.Dcmaccessionnumber ;
			} else 
			if(xmlPath=="dcmPatientId"){
				return this.Dcmpatientid ;
			} else 
			if(xmlPath=="dcmPatientName"){
				return this.Dcmpatientname ;
			} else 
			if(xmlPath=="dcmPatientBirthDate"){
				return this.Dcmpatientbirthdate ;
			} else 
			if(xmlPath=="session_type"){
				return this.SessionType ;
			} else 
			if(xmlPath=="modality"){
				return this.Modality ;
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
			if(xmlPath=="subjectAssessorData"){
				this.Subjectassessordata=value;
			} else 
			if(xmlPath.startsWith("subjectAssessorData")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Subjectassessordata ;
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
				if(this.Subjectassessordata!=undefined){
					this.Subjectassessordata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Subjectassessordata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Subjectassessordata= instanciateObject("xnat:subjectAssessorData");//omUtils.js
						}
						if(options && options.where)this.Subjectassessordata.setProperty(options.where.field,options.where.value);
						this.Subjectassessordata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="regions/region"){
				this.Regions_region=value;
			} else 
			if(xmlPath.startsWith("regions/region")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Regions_region ;
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

				for(var whereCount=0;whereCount<this.Regions_region.length;whereCount++){

					var tempValue=this.Regions_region[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Regions_region[whereCount]);

					}

				}
				}else{

				whereArray=this.Regions_region;
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
						newChild= instanciateObject("xnat:regionResource");//omUtils.js
					}
					this.addRegions_region(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="scanner"){
				this.Scanner=value;
			} else 
			if(xmlPath=="scanner/manufacturer"){
				this.Scanner_manufacturer=value;
			} else 
			if(xmlPath=="scanner/model"){
				this.Scanner_model=value;
			} else 
			if(xmlPath=="operator"){
				this.Operator=value;
			} else 
			if(xmlPath=="prearchivePath"){
				this.Prearchivepath=value;
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
						newChild= instanciateObject("xnat:imageScanData");//omUtils.js
					}
					this.addScans_scan(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="reconstructions/reconstructedImage"){
				this.Reconstructions_reconstructedimage=value;
			} else 
			if(xmlPath.startsWith("reconstructions/reconstructedImage")){
				xmlPath=xmlPath.substring(34);
				if(xmlPath=="")return this.Reconstructions_reconstructedimage ;
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

				for(var whereCount=0;whereCount<this.Reconstructions_reconstructedimage.length;whereCount++){

					var tempValue=this.Reconstructions_reconstructedimage[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Reconstructions_reconstructedimage[whereCount]);

					}

				}
				}else{

				whereArray=this.Reconstructions_reconstructedimage;
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
						newChild= instanciateObject("xnat:reconstructedImageData");//omUtils.js
					}
					this.addReconstructions_reconstructedimage(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="assessors/assessor"){
				this.Assessors_assessor=value;
			} else 
			if(xmlPath.startsWith("assessors/assessor")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Assessors_assessor ;
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

				for(var whereCount=0;whereCount<this.Assessors_assessor.length;whereCount++){

					var tempValue=this.Assessors_assessor[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Assessors_assessor[whereCount]);

					}

				}
				}else{

				whereArray=this.Assessors_assessor;
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
						newChild= instanciateObject("xnat:imageAssessorData");//omUtils.js
					}
					this.addAssessors_assessor(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="dcmAccessionNumber"){
				this.Dcmaccessionnumber=value;
			} else 
			if(xmlPath=="dcmPatientId"){
				this.Dcmpatientid=value;
			} else 
			if(xmlPath=="dcmPatientName"){
				this.Dcmpatientname=value;
			} else 
			if(xmlPath=="dcmPatientBirthDate"){
				this.Dcmpatientbirthdate=value;
			} else 
			if(xmlPath=="session_type"){
				this.SessionType=value;
			} else 
			if(xmlPath=="modality"){
				this.Modality=value;
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
		if (xmlPath=="regions/region"){
			this.addRegions_region(v);
		}else if (xmlPath=="scans/scan"){
			this.addScans_scan(v);
		}else if (xmlPath=="reconstructions/reconstructedImage"){
			this.addReconstructions_reconstructedimage(v);
		}else if (xmlPath=="assessors/assessor"){
			this.addAssessors_assessor(v);
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
		if (xmlPath=="regions/region"){
			return "http://nrg.wustl.edu/xnat:regionResource";
		}else if (xmlPath=="scans/scan"){
			return "http://nrg.wustl.edu/xnat:imageScanData";
		}else if (xmlPath=="reconstructions/reconstructedImage"){
			return "http://nrg.wustl.edu/xnat:reconstructedImageData";
		}else if (xmlPath=="assessors/assessor"){
			return "http://nrg.wustl.edu/xnat:imageAssessorData";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="regions/region"){
			return "field_multi_reference";
		}else if (xmlPath=="scanner"){
			return "field_data";
		}else if (xmlPath=="scanner/manufacturer"){
			return "field_data";
		}else if (xmlPath=="scanner/model"){
			return "field_data";
		}else if (xmlPath=="operator"){
			return "field_data";
		}else if (xmlPath=="prearchivePath"){
			return "field_data";
		}else if (xmlPath=="scans/scan"){
			return "field_multi_reference";
		}else if (xmlPath=="reconstructions/reconstructedImage"){
			return "field_multi_reference";
		}else if (xmlPath=="assessors/assessor"){
			return "field_multi_reference";
		}else if (xmlPath=="dcmAccessionNumber"){
			return "field_data";
		}else if (xmlPath=="dcmPatientId"){
			return "field_data";
		}else if (xmlPath=="dcmPatientName"){
			return "field_data";
		}else if (xmlPath=="dcmPatientBirthDate"){
			return "field_data";
		}else if (xmlPath=="session_type"){
			return "field_data";
		}else if (xmlPath=="modality"){
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
		xmlTxt+="\n<xnat:imageSessionData";
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
		xmlTxt+="\n</xnat:imageSessionData>";
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
		if (this.SessionType!=null)
			attTxt+=" session_type=\"" +this.SessionType +"\"";
		//NOT REQUIRED FIELD

		if (this.Modality!=null)
			attTxt+=" modality=\"" +this.Modality +"\"";
		//NOT REQUIRED FIELD

		if (this.Uid!=null)
			attTxt+=" UID=\"" +this.Uid +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		xmlTxt+=this.extension.getXMLBody(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Regions_region.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:regions";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Regions_regionCOUNT=0;Regions_regionCOUNT<this.Regions_region.length;Regions_regionCOUNT++){
			xmlTxt +="\n<xnat:region";
			xmlTxt +=this.Regions_region[Regions_regionCOUNT].getXMLAtts();
			if(this.Regions_region[Regions_regionCOUNT].xsiType!="xnat:regionResource"){
				xmlTxt+=" xsi:type=\"" + this.Regions_region[Regions_regionCOUNT].xsiType + "\"";
			}
			if (this.Regions_region[Regions_regionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Regions_region[Regions_regionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:region>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:regions>";
			}
			}

		var ScannerATT = ""
		if (this.Scanner_manufacturer!=null)
			ScannerATT+=" manufacturer=\"" + this.Scanner_manufacturer.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Scanner_model!=null)
			ScannerATT+=" model=\"" + this.Scanner_model.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Scanner!=null){
			xmlTxt+="\n<xnat:scanner";
			xmlTxt+=ScannerATT;
			xmlTxt+=">";
			xmlTxt+=this.Scanner.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:scanner>";
		}
		else if(ScannerATT!=""){
			xmlTxt+="\n<xnat:scanner";
			xmlTxt+=ScannerATT;
			xmlTxt+="/>";
		}

		if (this.Operator!=null){
			xmlTxt+="\n<xnat:operator";
			xmlTxt+=">";
			xmlTxt+=this.Operator.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:operator>";
		}
		if (this.Prearchivepath!=null){
			xmlTxt+="\n<xnat:prearchivePath";
			xmlTxt+=">";
			xmlTxt+=this.Prearchivepath.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:prearchivePath>";
		}
			var child1=0;
			var att1=0;
			child1+=this.Scans_scan.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:scans";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Scans_scanCOUNT=0;Scans_scanCOUNT<this.Scans_scan.length;Scans_scanCOUNT++){
			xmlTxt +="\n<xnat:scan";
			xmlTxt +=this.Scans_scan[Scans_scanCOUNT].getXMLAtts();
			if(this.Scans_scan[Scans_scanCOUNT].xsiType!="xnat:imageScanData"){
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

			var child2=0;
			var att2=0;
			child2+=this.Reconstructions_reconstructedimage.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xnat:reconstructions";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Reconstructions_reconstructedimageCOUNT=0;Reconstructions_reconstructedimageCOUNT<this.Reconstructions_reconstructedimage.length;Reconstructions_reconstructedimageCOUNT++){
			xmlTxt +="\n<xnat:reconstructedImage";
			xmlTxt +=this.Reconstructions_reconstructedimage[Reconstructions_reconstructedimageCOUNT].getXMLAtts();
			if(this.Reconstructions_reconstructedimage[Reconstructions_reconstructedimageCOUNT].xsiType!="xnat:reconstructedImageData"){
				xmlTxt+=" xsi:type=\"" + this.Reconstructions_reconstructedimage[Reconstructions_reconstructedimageCOUNT].xsiType + "\"";
			}
			if (this.Reconstructions_reconstructedimage[Reconstructions_reconstructedimageCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Reconstructions_reconstructedimage[Reconstructions_reconstructedimageCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:reconstructedImage>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:reconstructions>";
			}
			}

			var child3=0;
			var att3=0;
			child3+=this.Assessors_assessor.length;
			if(child3>0 || att3>0){
				xmlTxt+="\n<xnat:assessors";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Assessors_assessorCOUNT=0;Assessors_assessorCOUNT<this.Assessors_assessor.length;Assessors_assessorCOUNT++){
			xmlTxt +="\n<xnat:assessor";
			xmlTxt +=this.Assessors_assessor[Assessors_assessorCOUNT].getXMLAtts();
			if(this.Assessors_assessor[Assessors_assessorCOUNT].xsiType!="xnat:imageAssessorData"){
				xmlTxt+=" xsi:type=\"" + this.Assessors_assessor[Assessors_assessorCOUNT].xsiType + "\"";
			}
			if (this.Assessors_assessor[Assessors_assessorCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Assessors_assessor[Assessors_assessorCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:assessor>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:assessors>";
			}
			}

		if (this.Dcmaccessionnumber!=null){
			xmlTxt+="\n<xnat:dcmAccessionNumber";
			xmlTxt+=">";
			xmlTxt+=this.Dcmaccessionnumber.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:dcmAccessionNumber>";
		}
		if (this.Dcmpatientid!=null){
			xmlTxt+="\n<xnat:dcmPatientId";
			xmlTxt+=">";
			xmlTxt+=this.Dcmpatientid.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:dcmPatientId>";
		}
		if (this.Dcmpatientname!=null){
			xmlTxt+="\n<xnat:dcmPatientName";
			xmlTxt+=">";
			xmlTxt+=this.Dcmpatientname.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:dcmPatientName>";
		}
		if (this.Dcmpatientbirthdate!=null){
			xmlTxt+="\n<xnat:dcmPatientBirthDate";
			xmlTxt+=">";
			xmlTxt+=this.Dcmpatientbirthdate;
			xmlTxt+="</xnat:dcmPatientBirthDate>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Regions_region.length>0)return true;
		if (this.Scanner_manufacturer!=null)
			return true;
		if (this.Scanner_model!=null)
			return true;
		if (this.Scanner!=null) return true;
		if (this.Operator!=null) return true;
		if (this.Prearchivepath!=null) return true;
			if(this.Scans_scan.length>0)return true;
			if(this.Reconstructions_reconstructedimage.length>0)return true;
			if(this.Assessors_assessor.length>0)return true;
		if (this.Dcmaccessionnumber!=null) return true;
		if (this.Dcmpatientid!=null) return true;
		if (this.Dcmpatientname!=null) return true;
		if (this.Dcmpatientbirthdate!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
