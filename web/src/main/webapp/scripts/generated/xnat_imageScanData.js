/*
 * web: xnat_imageScanData.js
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

function xnat_imageScanData(){
this.xsiType="xnat:imageScanData";

	this.getSchemaElementName=function(){
		return "imageScanData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:imageScanData";
	}
	this.Sharing_share =new Array();

	function getSharing_share() {
		return this.Sharing_share;
	}
	this.getSharing_share=getSharing_share;


	function addSharing_share(v){
		this.Sharing_share.push(v);
	}
	this.addSharing_share=addSharing_share;

	this.ImageSessionId=null;


	function getImageSessionId() {
		return this.ImageSessionId;
	}
	this.getImageSessionId=getImageSessionId;


	function setImageSessionId(v){
		this.ImageSessionId=v;
	}
	this.setImageSessionId=setImageSessionId;

	this.Note=null;


	function getNote() {
		return this.Note;
	}
	this.getNote=getNote;


	function setNote(v){
		this.Note=v;
	}
	this.setNote=setNote;

	this.Quality=null;


	function getQuality() {
		return this.Quality;
	}
	this.getQuality=getQuality;


	function setQuality(v){
		this.Quality=v;
	}
	this.setQuality=setQuality;

	this.Condition=null;


	function getCondition() {
		return this.Condition;
	}
	this.getCondition=getCondition;


	function setCondition(v){
		this.Condition=v;
	}
	this.setCondition=setCondition;

	this.SeriesDescription=null;


	function getSeriesDescription() {
		return this.SeriesDescription;
	}
	this.getSeriesDescription=getSeriesDescription;


	function setSeriesDescription(v){
		this.SeriesDescription=v;
	}
	this.setSeriesDescription=setSeriesDescription;

	this.Documentation=null;


	function getDocumentation() {
		return this.Documentation;
	}
	this.getDocumentation=getDocumentation;


	function setDocumentation(v){
		this.Documentation=v;
	}
	this.setDocumentation=setDocumentation;

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

	this.Modality=null;


	function getModality() {
		return this.Modality;
	}
	this.getModality=getModality;


	function setModality(v){
		this.Modality=v;
	}
	this.setModality=setModality;

	this.Frames=null;


	function getFrames() {
		return this.Frames;
	}
	this.getFrames=getFrames;


	function setFrames(v){
		this.Frames=v;
	}
	this.setFrames=setFrames;

	this.Operator=null;


	function getOperator() {
		return this.Operator;
	}
	this.getOperator=getOperator;


	function setOperator(v){
		this.Operator=v;
	}
	this.setOperator=setOperator;
	this.File =new Array();

	function getFile() {
		return this.File;
	}
	this.getFile=getFile;


	function addFile(v){
		this.File.push(v);
	}
	this.addFile=addFile;
	this.Validation =null;
	function getValidation() {
		return this.Validation;
	}
	this.getValidation=getValidation;


	function setValidation(v){
		this.Validation =v;
	}
	this.setValidation=setValidation;

	this.Validation_ValidationXnatValidationdataId=null;


	function getValidation_ValidationXnatValidationdataId(){
		return this.Validation_ValidationXnatValidationdataId;
	}
	this.getValidation_ValidationXnatValidationdataId=getValidation_ValidationXnatValidationdataId;


	function setValidation_ValidationXnatValidationdataId(v){
		this.Validation_ValidationXnatValidationdataId=v;
	}
	this.setValidation_ValidationXnatValidationdataId=setValidation_ValidationXnatValidationdataId;

	this.Starttime=null;


	function getStarttime() {
		return this.Starttime;
	}
	this.getStarttime=getStarttime;


	function setStarttime(v){
		this.Starttime=v;
	}
	this.setStarttime=setStarttime;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.Type=null;


	function getType() {
		return this.Type;
	}
	this.getType=getType;


	function setType(v){
		this.Type=v;
	}
	this.setType=setType;

	this.Uid=null;


	function getUid() {
		return this.Uid;
	}
	this.getUid=getUid;


	function setUid(v){
		this.Uid=v;
	}
	this.setUid=setUid;

	this.Project=null;


	function getProject() {
		return this.Project;
	}
	this.getProject=getProject;


	function setProject(v){
		this.Project=v;
	}
	this.setProject=setProject;

	this.XnatImagescandataId=null;


	function getXnatImagescandataId() {
		return this.XnatImagescandataId;
	}
	this.getXnatImagescandataId=getXnatImagescandataId;


	function setXnatImagescandataId(v){
		this.XnatImagescandataId=v;
	}
	this.setXnatImagescandataId=setXnatImagescandataId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="sharing/share"){
				return this.Sharing_share ;
			} else 
			if(xmlPath.startsWith("sharing/share")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Sharing_share ;
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

				for(var whereCount=0;whereCount<this.Sharing_share.length;whereCount++){

					var tempValue=this.Sharing_share[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Sharing_share[whereCount]);

					}

				}
				}else{

				whereArray=this.Sharing_share;
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
			if(xmlPath=="image_session_ID"){
				return this.ImageSessionId ;
			} else 
			if(xmlPath=="note"){
				return this.Note ;
			} else 
			if(xmlPath=="quality"){
				return this.Quality ;
			} else 
			if(xmlPath=="condition"){
				return this.Condition ;
			} else 
			if(xmlPath=="series_description"){
				return this.SeriesDescription ;
			} else 
			if(xmlPath=="documentation"){
				return this.Documentation ;
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
			if(xmlPath=="modality"){
				return this.Modality ;
			} else 
			if(xmlPath=="frames"){
				return this.Frames ;
			} else 
			if(xmlPath=="operator"){
				return this.Operator ;
			} else 
			if(xmlPath=="file"){
				return this.File ;
			} else 
			if(xmlPath.startsWith("file")){
				xmlPath=xmlPath.substring(4);
				if(xmlPath=="")return this.File ;
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

				for(var whereCount=0;whereCount<this.File.length;whereCount++){

					var tempValue=this.File[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.File[whereCount]);

					}

				}
				}else{

				whereArray=this.File;
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
			if(xmlPath=="validation"){
				return this.Validation ;
			} else 
			if(xmlPath.startsWith("validation")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Validation ;
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
				if(this.Validation!=undefined)return this.Validation.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="startTime"){
				return this.Starttime ;
			} else 
			if(xmlPath=="ID"){
				return this.Id ;
			} else 
			if(xmlPath=="type"){
				return this.Type ;
			} else 
			if(xmlPath=="UID"){
				return this.Uid ;
			} else 
			if(xmlPath=="project"){
				return this.Project ;
			} else 
			if(xmlPath=="extension_item"){
				return this.ExtensionItem ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_imageScanData_id"){
				return this.XnatImagescandataId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="sharing/share"){
				this.Sharing_share=value;
			} else 
			if(xmlPath.startsWith("sharing/share")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Sharing_share ;
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

				for(var whereCount=0;whereCount<this.Sharing_share.length;whereCount++){

					var tempValue=this.Sharing_share[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Sharing_share[whereCount]);

					}

				}
				}else{

				whereArray=this.Sharing_share;
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
						newChild= instanciateObject("xnat:imageScanData_share");//omUtils.js
					}
					this.addSharing_share(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="image_session_ID"){
				this.ImageSessionId=value;
			} else 
			if(xmlPath=="note"){
				this.Note=value;
			} else 
			if(xmlPath=="quality"){
				this.Quality=value;
			} else 
			if(xmlPath=="condition"){
				this.Condition=value;
			} else 
			if(xmlPath=="series_description"){
				this.SeriesDescription=value;
			} else 
			if(xmlPath=="documentation"){
				this.Documentation=value;
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
			if(xmlPath=="modality"){
				this.Modality=value;
			} else 
			if(xmlPath=="frames"){
				this.Frames=value;
			} else 
			if(xmlPath=="operator"){
				this.Operator=value;
			} else 
			if(xmlPath=="file"){
				this.File=value;
			} else 
			if(xmlPath.startsWith("file")){
				xmlPath=xmlPath.substring(4);
				if(xmlPath=="")return this.File ;
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

				for(var whereCount=0;whereCount<this.File.length;whereCount++){

					var tempValue=this.File[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.File[whereCount]);

					}

				}
				}else{

				whereArray=this.File;
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
						newChild= instanciateObject("xnat:abstractResource");//omUtils.js
					}
					this.addFile(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="validation"){
				this.Validation=value;
			} else 
			if(xmlPath.startsWith("validation")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Validation ;
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
				if(this.Validation!=undefined){
					this.Validation.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Validation= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Validation= instanciateObject("xnat:validationData");//omUtils.js
						}
						if(options && options.where)this.Validation.setProperty(options.where.field,options.where.value);
						this.Validation.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="startTime"){
				this.Starttime=value;
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="type"){
				this.Type=value;
			} else 
			if(xmlPath=="UID"){
				this.Uid=value;
			} else 
			if(xmlPath=="project"){
				this.Project=value;
			} else 
			if(xmlPath=="extension_item"){
				this.ExtensionItem=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_imageScanData_id"){
				this.XnatImagescandataId=value;
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
		if (xmlPath=="sharing/share"){
			this.addSharing_share(v);
		}else if (xmlPath=="file"){
			this.addFile(v);
		}else if (xmlPath=="validation"){
			this.setValidation(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="sharing/share"){
			return "http://nrg.wustl.edu/xnat:imageScanData_share";
		}else if (xmlPath=="file"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="validation"){
			return "http://nrg.wustl.edu/xnat:validationData";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="sharing/share"){
			return "field_NO_CHILD";
		}else if (xmlPath=="image_session_ID"){
			return "field_data";
		}else if (xmlPath=="note"){
			return "field_data";
		}else if (xmlPath=="quality"){
			return "field_data";
		}else if (xmlPath=="condition"){
			return "field_data";
		}else if (xmlPath=="series_description"){
			return "field_data";
		}else if (xmlPath=="documentation"){
			return "field_LONG_DATA";
		}else if (xmlPath=="scanner"){
			return "field_data";
		}else if (xmlPath=="scanner/manufacturer"){
			return "field_data";
		}else if (xmlPath=="scanner/model"){
			return "field_data";
		}else if (xmlPath=="modality"){
			return "field_data";
		}else if (xmlPath=="frames"){
			return "field_data";
		}else if (xmlPath=="operator"){
			return "field_data";
		}else if (xmlPath=="file"){
			return "field_multi_reference";
		}else if (xmlPath=="validation"){
			return "field_single_reference";
		}else if (xmlPath=="startTime"){
			return "field_data";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="type"){
			return "field_data";
		}else if (xmlPath=="UID"){
			return "field_data";
		}else if (xmlPath=="project"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:imageScanData";
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
		xmlTxt+="\n</xnat:imageScanData>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatImagescandataId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_imageScanData_id=\"" + this.XnatImagescandataId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Id!=null)
			attTxt+=" ID=\"" +this.Id +"\"";
		else attTxt+=" ID=\"\"";//REQUIRED FIELD

		if (this.Type!=null)
			attTxt+=" type=\"" +this.Type +"\"";
		//NOT REQUIRED FIELD

		if (this.Uid!=null)
			attTxt+=" UID=\"" +this.Uid +"\"";
		//NOT REQUIRED FIELD

		if (this.Project!=null)
			attTxt+=" project=\"" +this.Project +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Sharing_share.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:sharing";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Sharing_shareCOUNT=0;Sharing_shareCOUNT<this.Sharing_share.length;Sharing_shareCOUNT++){
			xmlTxt +="\n<xnat:share";
			xmlTxt +=this.Sharing_share[Sharing_shareCOUNT].getXMLAtts();
			if(this.Sharing_share[Sharing_shareCOUNT].xsiType!="xnat:imageScanData_share"){
				xmlTxt+=" xsi:type=\"" + this.Sharing_share[Sharing_shareCOUNT].xsiType + "\"";
			}
			if (this.Sharing_share[Sharing_shareCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Sharing_share[Sharing_shareCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:share>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:sharing>";
			}
			}

		if (this.ImageSessionId!=null){
			xmlTxt+="\n<xnat:image_session_ID";
			xmlTxt+=">";
			xmlTxt+=this.ImageSessionId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:image_session_ID>";
		}
		if (this.Note!=null){
			xmlTxt+="\n<xnat:note";
			xmlTxt+=">";
			xmlTxt+=this.Note.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:note>";
		}
		if (this.Quality!=null){
			xmlTxt+="\n<xnat:quality";
			xmlTxt+=">";
			xmlTxt+=this.Quality.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:quality>";
		}
		if (this.Condition!=null){
			xmlTxt+="\n<xnat:condition";
			xmlTxt+=">";
			xmlTxt+=this.Condition.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:condition>";
		}
		if (this.SeriesDescription!=null){
			xmlTxt+="\n<xnat:series_description";
			xmlTxt+=">";
			xmlTxt+=this.SeriesDescription.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:series_description>";
		}
		if (this.Documentation!=null){
			xmlTxt+="\n<xnat:documentation";
			xmlTxt+=">";
			xmlTxt+=this.Documentation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:documentation>";
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

		if (this.Modality!=null){
			xmlTxt+="\n<xnat:modality";
			xmlTxt+=">";
			xmlTxt+=this.Modality.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:modality>";
		}
		if (this.Frames!=null){
			xmlTxt+="\n<xnat:frames";
			xmlTxt+=">";
			xmlTxt+=this.Frames;
			xmlTxt+="</xnat:frames>";
		}
		if (this.Operator!=null){
			xmlTxt+="\n<xnat:operator";
			xmlTxt+=">";
			xmlTxt+=this.Operator.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:operator>";
		}
		for(var FileCOUNT=0;FileCOUNT<this.File.length;FileCOUNT++){
			xmlTxt +="\n<xnat:file";
			xmlTxt +=this.File[FileCOUNT].getXMLAtts();
			if(this.File[FileCOUNT].xsiType!="xnat:abstractResource"){
				xmlTxt+=" xsi:type=\"" + this.File[FileCOUNT].xsiType + "\"";
			}
			if (this.File[FileCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.File[FileCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:file>";
			}else {xmlTxt+="/>";}
		}
		if (this.Validation!=null){
			xmlTxt+="\n<xnat:validation";
			xmlTxt+=this.Validation.getXMLAtts();
			if(this.Validation.xsiType!="xnat:validationData"){
				xmlTxt+=" xsi:type=\"" + this.Validation.xsiType + "\"";
			}
			if (this.Validation.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Validation.getXMLBody(preventComments);
				xmlTxt+="</xnat:validation>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

		if (this.Starttime!=null){
			xmlTxt+="\n<xnat:startTime";
			xmlTxt+=">";
			xmlTxt+=this.Starttime;
			xmlTxt+="</xnat:startTime>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatImagescandataId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.Sharing_share.length>0)return true;
		if (this.ImageSessionId!=null) return true;
		if (this.Note!=null) return true;
		if (this.Quality!=null) return true;
		if (this.Condition!=null) return true;
		if (this.SeriesDescription!=null) return true;
		if (this.Documentation!=null) return true;
		if (this.Scanner_manufacturer!=null)
			return true;
		if (this.Scanner_model!=null)
			return true;
		if (this.Scanner!=null) return true;
		if (this.Modality!=null) return true;
		if (this.Frames!=null) return true;
		if (this.Operator!=null) return true;
		if(this.File.length>0) return true;
		if (this.Validation!=null){
			if (this.Validation.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if (this.Starttime!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
