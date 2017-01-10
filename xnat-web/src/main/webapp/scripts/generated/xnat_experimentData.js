/*
 * web: xnat_experimentData.js
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

function xnat_experimentData(){
this.xsiType="xnat:experimentData";

	this.getSchemaElementName=function(){
		return "experimentData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:experimentData";
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

	this.Date=null;


	function getDate() {
		return this.Date;
	}
	this.getDate=getDate;


	function setDate(v){
		this.Date=v;
	}
	this.setDate=setDate;

	this.Time=null;


	function getTime() {
		return this.Time;
	}
	this.getTime=getTime;


	function setTime(v){
		this.Time=v;
	}
	this.setTime=setTime;

	this.Duration=null;


	function getDuration() {
		return this.Duration;
	}
	this.getDuration=getDuration;


	function setDuration(v){
		this.Duration=v;
	}
	this.setDuration=setDuration;

	this.Delay=null;


	function getDelay() {
		return this.Delay;
	}
	this.getDelay=getDelay;


	function setDelay(v){
		this.Delay=v;
	}
	this.setDelay=setDelay;

	this.Delay_refExptId=null;


	function getDelay_refExptId() {
		return this.Delay_refExptId;
	}
	this.getDelay_refExptId=getDelay_refExptId;


	function setDelay_refExptId(v){
		this.Delay_refExptId=v;
	}
	this.setDelay_refExptId=setDelay_refExptId;

	this.Note=null;


	function getNote() {
		return this.Note;
	}
	this.getNote=getNote;


	function setNote(v){
		this.Note=v;
	}
	this.setNote=setNote;
	this.Investigator =null;
	function getInvestigator() {
		return this.Investigator;
	}
	this.getInvestigator=getInvestigator;


	function setInvestigator(v){
		this.Investigator =v;
	}
	this.setInvestigator=setInvestigator;

	this.Investigator_InvestigatorXnatInvestigatordataId=null;


	function getInvestigator_InvestigatorXnatInvestigatordataId(){
		return this.Investigator_InvestigatorXnatInvestigatordataId;
	}
	this.getInvestigator_InvestigatorXnatInvestigatordataId=getInvestigator_InvestigatorXnatInvestigatordataId;


	function setInvestigator_InvestigatorXnatInvestigatordataId(v){
		this.Investigator_InvestigatorXnatInvestigatordataId=v;
	}
	this.setInvestigator_InvestigatorXnatInvestigatordataId=setInvestigator_InvestigatorXnatInvestigatordataId;
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
	this.Resources_resource =new Array();

	function getResources_resource() {
		return this.Resources_resource;
	}
	this.getResources_resource=getResources_resource;


	function addResources_resource(v){
		this.Resources_resource.push(v);
	}
	this.addResources_resource=addResources_resource;
	this.Fields_field =new Array();

	function getFields_field() {
		return this.Fields_field;
	}
	this.getFields_field=getFields_field;


	function addFields_field(v){
		this.Fields_field.push(v);
	}
	this.addFields_field=addFields_field;

	this.AcquisitionSite=null;


	function getAcquisitionSite() {
		return this.AcquisitionSite;
	}
	this.getAcquisitionSite=getAcquisitionSite;


	function setAcquisitionSite(v){
		this.AcquisitionSite=v;
	}
	this.setAcquisitionSite=setAcquisitionSite;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.Project=null;


	function getProject() {
		return this.Project;
	}
	this.getProject=getProject;


	function setProject(v){
		this.Project=v;
	}
	this.setProject=setProject;

	this.VisitId=null;


	function getVisitId() {
		return this.VisitId;
	}
	this.getVisitId=getVisitId;


	function setVisitId(v){
		this.VisitId=v;
	}
	this.setVisitId=setVisitId;

	this.Visit=null;


	function getVisit() {
		return this.Visit;
	}
	this.getVisit=getVisit;


	function setVisit(v){
		this.Visit=v;
	}
	this.setVisit=setVisit;

	this.Version=null;


	function getVersion() {
		return this.Version;
	}
	this.getVersion=getVersion;


	function setVersion(v){
		this.Version=v;
	}
	this.setVersion=setVersion;

	this.Original=null;


	function getOriginal() {
		return this.Original;
	}
	this.getOriginal=getOriginal;


	function setOriginal(v){
		this.Original=v;
	}
	this.setOriginal=setOriginal;

	this.Protocol=null;


	function getProtocol() {
		return this.Protocol;
	}
	this.getProtocol=getProtocol;


	function setProtocol(v){
		this.Protocol=v;
	}
	this.setProtocol=setProtocol;

	this.Label=null;


	function getLabel() {
		return this.Label;
	}
	this.getLabel=getLabel;


	function setLabel(v){
		this.Label=v;
	}
	this.setLabel=setLabel;


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
			if(xmlPath=="date"){
				return this.Date ;
			} else 
			if(xmlPath=="time"){
				return this.Time ;
			} else 
			if(xmlPath=="duration"){
				return this.Duration ;
			} else 
			if(xmlPath=="delay"){
				return this.Delay ;
			} else 
			if(xmlPath=="delay/ref_expt_id"){
				return this.Delay_refExptId ;
			} else 
			if(xmlPath=="note"){
				return this.Note ;
			} else 
			if(xmlPath=="investigator"){
				return this.Investigator ;
			} else 
			if(xmlPath.startsWith("investigator")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Investigator ;
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
				if(this.Investigator!=undefined)return this.Investigator.getProperty(xmlPath);
				else return null;
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
			if(xmlPath=="resources/resource"){
				return this.Resources_resource ;
			} else 
			if(xmlPath.startsWith("resources/resource")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Resources_resource ;
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

				for(var whereCount=0;whereCount<this.Resources_resource.length;whereCount++){

					var tempValue=this.Resources_resource[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Resources_resource[whereCount]);

					}

				}
				}else{

				whereArray=this.Resources_resource;
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
			if(xmlPath=="acquisition_site"){
				return this.AcquisitionSite ;
			} else 
			if(xmlPath=="ID"){
				return this.Id ;
			} else 
			if(xmlPath=="project"){
				return this.Project ;
			} else 
			if(xmlPath=="visit_id"){
				return this.VisitId ;
			} else 
			if(xmlPath=="visit"){
				return this.Visit ;
			} else 
			if(xmlPath=="version"){
				return this.Version ;
			} else 
			if(xmlPath=="original"){
				return this.Original ;
			} else 
			if(xmlPath=="protocol"){
				return this.Protocol ;
			} else 
			if(xmlPath=="label"){
				return this.Label ;
			} else 
			if(xmlPath=="extension_item"){
				return this.ExtensionItem ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
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
						newChild= instanciateObject("xnat:experimentData_share");//omUtils.js
					}
					this.addSharing_share(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="date"){
				this.Date=value;
			} else 
			if(xmlPath=="time"){
				this.Time=value;
			} else 
			if(xmlPath=="duration"){
				this.Duration=value;
			} else 
			if(xmlPath=="delay"){
				this.Delay=value;
			} else 
			if(xmlPath=="delay/ref_expt_id"){
				this.Delay_refExptId=value;
			} else 
			if(xmlPath=="note"){
				this.Note=value;
			} else 
			if(xmlPath=="investigator"){
				this.Investigator=value;
			} else 
			if(xmlPath.startsWith("investigator")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Investigator ;
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
				if(this.Investigator!=undefined){
					this.Investigator.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Investigator= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Investigator= instanciateObject("xnat:investigatorData");//omUtils.js
						}
						if(options && options.where)this.Investigator.setProperty(options.where.field,options.where.value);
						this.Investigator.setProperty(xmlPath,value);
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
			if(xmlPath=="resources/resource"){
				this.Resources_resource=value;
			} else 
			if(xmlPath.startsWith("resources/resource")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Resources_resource ;
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

				for(var whereCount=0;whereCount<this.Resources_resource.length;whereCount++){

					var tempValue=this.Resources_resource[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Resources_resource[whereCount]);

					}

				}
				}else{

				whereArray=this.Resources_resource;
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
					this.addResources_resource(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
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
						newChild= instanciateObject("xnat:experimentData_field");//omUtils.js
					}
					this.addFields_field(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="acquisition_site"){
				this.AcquisitionSite=value;
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="project"){
				this.Project=value;
			} else 
			if(xmlPath=="visit_id"){
				this.VisitId=value;
			} else 
			if(xmlPath=="visit"){
				this.Visit=value;
			} else 
			if(xmlPath=="version"){
				this.Version=value;
			} else 
			if(xmlPath=="original"){
				this.Original=value;
			} else 
			if(xmlPath=="protocol"){
				this.Protocol=value;
			} else 
			if(xmlPath=="label"){
				this.Label=value;
			} else 
			if(xmlPath=="extension_item"){
				this.ExtensionItem=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
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
		}else if (xmlPath=="investigator"){
			this.setInvestigator(v);
		}else if (xmlPath=="validation"){
			this.setValidation(v);
		}else if (xmlPath=="resources/resource"){
			this.addResources_resource(v);
		}else if (xmlPath=="fields/field"){
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
		if (xmlPath=="sharing/share"){
			return "http://nrg.wustl.edu/xnat:experimentData_share";
		}else if (xmlPath=="investigator"){
			return "http://nrg.wustl.edu/xnat:investigatorData";
		}else if (xmlPath=="validation"){
			return "http://nrg.wustl.edu/xnat:validationData";
		}else if (xmlPath=="resources/resource"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="fields/field"){
			return "http://nrg.wustl.edu/xnat:experimentData_field";
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
		}else if (xmlPath=="date"){
			return "field_data";
		}else if (xmlPath=="time"){
			return "field_data";
		}else if (xmlPath=="duration"){
			return "field_data";
		}else if (xmlPath=="delay"){
			return "field_data";
		}else if (xmlPath=="delay/ref_expt_id"){
			return "field_data";
		}else if (xmlPath=="note"){
			return "field_LONG_DATA";
		}else if (xmlPath=="investigator"){
			return "field_single_reference";
		}else if (xmlPath=="validation"){
			return "field_single_reference";
		}else if (xmlPath=="resources/resource"){
			return "field_multi_reference";
		}else if (xmlPath=="fields/field"){
			return "field_NO_CHILD";
		}else if (xmlPath=="acquisition_site"){
			return "field_data";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="project"){
			return "field_data";
		}else if (xmlPath=="visit_id"){
			return "field_data";
		}else if (xmlPath=="visit"){
			return "field_data";
		}else if (xmlPath=="version"){
			return "field_data";
		}else if (xmlPath=="original"){
			return "field_data";
		}else if (xmlPath=="protocol"){
			return "field_data";
		}else if (xmlPath=="label"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:experimentData";
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
		xmlTxt+="\n</xnat:experimentData>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Id!=null)
			attTxt+=" ID=\"" +this.Id +"\"";
		else attTxt+=" ID=\"\"";//REQUIRED FIELD

		if (this.Project!=null)
			attTxt+=" project=\"" +this.Project +"\"";
		//NOT REQUIRED FIELD

		if (this.VisitId!=null)
			attTxt+=" visit_id=\"" +this.VisitId +"\"";
		//NOT REQUIRED FIELD

		if (this.Visit!=null)
			attTxt+=" visit=\"" +this.Visit +"\"";
		//NOT REQUIRED FIELD

		if (this.Version!=null)
			attTxt+=" version=\"" +this.Version +"\"";
		//NOT REQUIRED FIELD

		if (this.Original!=null)
			attTxt+=" original=\"" +this.Original +"\"";
		//NOT REQUIRED FIELD

		if (this.Protocol!=null)
			attTxt+=" protocol=\"" +this.Protocol +"\"";
		//NOT REQUIRED FIELD

		if (this.Label!=null)
			attTxt+=" label=\"" +this.Label +"\"";
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
			if(this.Sharing_share[Sharing_shareCOUNT].xsiType!="xnat:experimentData_share"){
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

		if (this.Date!=null){
			xmlTxt+="\n<xnat:date";
			xmlTxt+=">";
			xmlTxt+=this.Date;
			xmlTxt+="</xnat:date>";
		}
		if (this.Time!=null){
			xmlTxt+="\n<xnat:time";
			xmlTxt+=">";
			xmlTxt+=this.Time;
			xmlTxt+="</xnat:time>";
		}
		if (this.Duration!=null){
			xmlTxt+="\n<xnat:duration";
			xmlTxt+=">";
			xmlTxt+=this.Duration;
			xmlTxt+="</xnat:duration>";
		}
		var DelayATT = ""
		if (this.Delay_refExptId!=null)
			DelayATT+=" ref_expt_id=\"" + this.Delay_refExptId.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Delay!=null){
			xmlTxt+="\n<xnat:delay";
			xmlTxt+=DelayATT;
			xmlTxt+=">";
			xmlTxt+=this.Delay;
			xmlTxt+="</xnat:delay>";
		}
		else if(DelayATT!=""){
			xmlTxt+="\n<xnat:delay";
			xmlTxt+=DelayATT;
			xmlTxt+="/>";
		}

		if (this.Note!=null){
			xmlTxt+="\n<xnat:note";
			xmlTxt+=">";
			xmlTxt+=this.Note.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:note>";
		}
		if (this.Investigator!=null){
			xmlTxt+="\n<xnat:investigator";
			xmlTxt+=this.Investigator.getXMLAtts();
			if(this.Investigator.xsiType!="xnat:investigatorData"){
				xmlTxt+=" xsi:type=\"" + this.Investigator.xsiType + "\"";
			}
			if (this.Investigator.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Investigator.getXMLBody(preventComments);
				xmlTxt+="</xnat:investigator>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

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

			var child1=0;
			var att1=0;
			child1+=this.Resources_resource.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:resources";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Resources_resourceCOUNT=0;Resources_resourceCOUNT<this.Resources_resource.length;Resources_resourceCOUNT++){
			xmlTxt +="\n<xnat:resource";
			xmlTxt +=this.Resources_resource[Resources_resourceCOUNT].getXMLAtts();
			if(this.Resources_resource[Resources_resourceCOUNT].xsiType!="xnat:abstractResource"){
				xmlTxt+=" xsi:type=\"" + this.Resources_resource[Resources_resourceCOUNT].xsiType + "\"";
			}
			if (this.Resources_resource[Resources_resourceCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Resources_resource[Resources_resourceCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:resource>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:resources>";
			}
			}

			var child2=0;
			var att2=0;
			child2+=this.Fields_field.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xnat:fields";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Fields_fieldCOUNT=0;Fields_fieldCOUNT<this.Fields_field.length;Fields_fieldCOUNT++){
			xmlTxt +="\n<xnat:field";
			xmlTxt +=this.Fields_field[Fields_fieldCOUNT].getXMLAtts();
			if(this.Fields_field[Fields_fieldCOUNT].xsiType!="xnat:experimentData_field"){
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

		if (this.AcquisitionSite!=null){
			xmlTxt+="\n<xnat:acquisition_site";
			xmlTxt+=">";
			xmlTxt+=this.AcquisitionSite.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:acquisition_site>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Sharing_share.length>0)return true;
		if (this.Date!=null) return true;
		if (this.Time!=null) return true;
		if (this.Duration!=null) return true;
		if (this.Delay_refExptId!=null)
			return true;
		if (this.Delay!=null) return true;
		if (this.Note!=null) return true;
		if (this.Investigator!=null){
			if (this.Investigator.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if (this.Validation!=null){
			if (this.Validation.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

			if(this.Resources_resource.length>0)return true;
			if(this.Fields_field.length>0)return true;
		if (this.AcquisitionSite!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
