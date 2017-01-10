/*
 * web: xnat_projectData.js
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

function xnat_projectData(){
this.xsiType="xnat:projectData";

	this.getSchemaElementName=function(){
		return "projectData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:projectData";
	}

	this.Name=null;


	function getName() {
		return this.Name;
	}
	this.getName=getName;


	function setName(v){
		this.Name=v;
	}
	this.setName=setName;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;

	this.Keywords=null;


	function getKeywords() {
		return this.Keywords;
	}
	this.getKeywords=getKeywords;


	function setKeywords(v){
		this.Keywords=v;
	}
	this.setKeywords=setKeywords;
	this.Aliases_alias =new Array();

	function getAliases_alias() {
		return this.Aliases_alias;
	}
	this.getAliases_alias=getAliases_alias;


	function addAliases_alias(v){
		this.Aliases_alias.push(v);
	}
	this.addAliases_alias=addAliases_alias;
	this.Publications_publication =new Array();

	function getPublications_publication() {
		return this.Publications_publication;
	}
	this.getPublications_publication=getPublications_publication;


	function addPublications_publication(v){
		this.Publications_publication.push(v);
	}
	this.addPublications_publication=addPublications_publication;
	this.Resources_resource =new Array();

	function getResources_resource() {
		return this.Resources_resource;
	}
	this.getResources_resource=getResources_resource;


	function addResources_resource(v){
		this.Resources_resource.push(v);
	}
	this.addResources_resource=addResources_resource;
	this.Studyprotocol =new Array();

	function getStudyprotocol() {
		return this.Studyprotocol;
	}
	this.getStudyprotocol=getStudyprotocol;


	function addStudyprotocol(v){
		this.Studyprotocol.push(v);
	}
	this.addStudyprotocol=addStudyprotocol;
	this.Pi =null;
	function getPi() {
		return this.Pi;
	}
	this.getPi=getPi;


	function setPi(v){
		this.Pi =v;
	}
	this.setPi=setPi;

	this.Pi_PiXnatInvestigatordataId=null;


	function getPi_PiXnatInvestigatordataId(){
		return this.Pi_PiXnatInvestigatordataId;
	}
	this.getPi_PiXnatInvestigatordataId=getPi_PiXnatInvestigatordataId;


	function setPi_PiXnatInvestigatordataId(v){
		this.Pi_PiXnatInvestigatordataId=v;
	}
	this.setPi_PiXnatInvestigatordataId=setPi_PiXnatInvestigatordataId;
	this.Investigators_investigator =new Array();

	function getInvestigators_investigator() {
		return this.Investigators_investigator;
	}
	this.getInvestigators_investigator=getInvestigators_investigator;


	function addInvestigators_investigator(v){
		this.Investigators_investigator.push(v);
	}
	this.addInvestigators_investigator=addInvestigators_investigator;
	this.Fields_field =new Array();

	function getFields_field() {
		return this.Fields_field;
	}
	this.getFields_field=getFields_field;


	function addFields_field(v){
		this.Fields_field.push(v);
	}
	this.addFields_field=addFields_field;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.SecondaryId=null;


	function getSecondaryId() {
		return this.SecondaryId;
	}
	this.getSecondaryId=getSecondaryId;


	function setSecondaryId(v){
		this.SecondaryId=v;
	}
	this.setSecondaryId=setSecondaryId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="keywords"){
				return this.Keywords ;
			} else 
			if(xmlPath=="aliases/alias"){
				return this.Aliases_alias ;
			} else 
			if(xmlPath.startsWith("aliases/alias")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Aliases_alias ;
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

				for(var whereCount=0;whereCount<this.Aliases_alias.length;whereCount++){

					var tempValue=this.Aliases_alias[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Aliases_alias[whereCount]);

					}

				}
				}else{

				whereArray=this.Aliases_alias;
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
			if(xmlPath=="publications/publication"){
				return this.Publications_publication ;
			} else 
			if(xmlPath.startsWith("publications/publication")){
				xmlPath=xmlPath.substring(24);
				if(xmlPath=="")return this.Publications_publication ;
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

				for(var whereCount=0;whereCount<this.Publications_publication.length;whereCount++){

					var tempValue=this.Publications_publication[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Publications_publication[whereCount]);

					}

				}
				}else{

				whereArray=this.Publications_publication;
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
			if(xmlPath=="studyProtocol"){
				return this.Studyprotocol ;
			} else 
			if(xmlPath.startsWith("studyProtocol")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Studyprotocol ;
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

				for(var whereCount=0;whereCount<this.Studyprotocol.length;whereCount++){

					var tempValue=this.Studyprotocol[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Studyprotocol[whereCount]);

					}

				}
				}else{

				whereArray=this.Studyprotocol;
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
			if(xmlPath=="PI"){
				return this.Pi ;
			} else 
			if(xmlPath.startsWith("PI")){
				xmlPath=xmlPath.substring(2);
				if(xmlPath=="")return this.Pi ;
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
				if(this.Pi!=undefined)return this.Pi.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="investigators/investigator"){
				return this.Investigators_investigator ;
			} else 
			if(xmlPath.startsWith("investigators/investigator")){
				xmlPath=xmlPath.substring(26);
				if(xmlPath=="")return this.Investigators_investigator ;
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

				for(var whereCount=0;whereCount<this.Investigators_investigator.length;whereCount++){

					var tempValue=this.Investigators_investigator[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Investigators_investigator[whereCount]);

					}

				}
				}else{

				whereArray=this.Investigators_investigator;
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
			if(xmlPath=="ID"){
				return this.Id ;
			} else 
			if(xmlPath=="secondary_ID"){
				return this.SecondaryId ;
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
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="keywords"){
				this.Keywords=value;
			} else 
			if(xmlPath=="aliases/alias"){
				this.Aliases_alias=value;
			} else 
			if(xmlPath.startsWith("aliases/alias")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Aliases_alias ;
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

				for(var whereCount=0;whereCount<this.Aliases_alias.length;whereCount++){

					var tempValue=this.Aliases_alias[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Aliases_alias[whereCount]);

					}

				}
				}else{

				whereArray=this.Aliases_alias;
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
						newChild= instanciateObject("xnat:projectData_alias");//omUtils.js
					}
					this.addAliases_alias(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="publications/publication"){
				this.Publications_publication=value;
			} else 
			if(xmlPath.startsWith("publications/publication")){
				xmlPath=xmlPath.substring(24);
				if(xmlPath=="")return this.Publications_publication ;
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

				for(var whereCount=0;whereCount<this.Publications_publication.length;whereCount++){

					var tempValue=this.Publications_publication[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Publications_publication[whereCount]);

					}

				}
				}else{

				whereArray=this.Publications_publication;
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
						newChild= instanciateObject("xnat:publicationResource");//omUtils.js
					}
					this.addPublications_publication(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
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
			if(xmlPath=="studyProtocol"){
				this.Studyprotocol=value;
			} else 
			if(xmlPath.startsWith("studyProtocol")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Studyprotocol ;
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

				for(var whereCount=0;whereCount<this.Studyprotocol.length;whereCount++){

					var tempValue=this.Studyprotocol[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Studyprotocol[whereCount]);

					}

				}
				}else{

				whereArray=this.Studyprotocol;
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
						newChild= instanciateObject("xnat:abstractProtocol");//omUtils.js
					}
					this.addStudyprotocol(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="PI"){
				this.Pi=value;
			} else 
			if(xmlPath.startsWith("PI")){
				xmlPath=xmlPath.substring(2);
				if(xmlPath=="")return this.Pi ;
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
				if(this.Pi!=undefined){
					this.Pi.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Pi= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Pi= instanciateObject("xnat:investigatorData");//omUtils.js
						}
						if(options && options.where)this.Pi.setProperty(options.where.field,options.where.value);
						this.Pi.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="investigators/investigator"){
				this.Investigators_investigator=value;
			} else 
			if(xmlPath.startsWith("investigators/investigator")){
				xmlPath=xmlPath.substring(26);
				if(xmlPath=="")return this.Investigators_investigator ;
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

				for(var whereCount=0;whereCount<this.Investigators_investigator.length;whereCount++){

					var tempValue=this.Investigators_investigator[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Investigators_investigator[whereCount]);

					}

				}
				}else{

				whereArray=this.Investigators_investigator;
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
						newChild= instanciateObject("xnat:investigatorData");//omUtils.js
					}
					this.addInvestigators_investigator(newChild);
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
						newChild= instanciateObject("xnat:projectData_field");//omUtils.js
					}
					this.addFields_field(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="secondary_ID"){
				this.SecondaryId=value;
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
		if (xmlPath=="aliases/alias"){
			this.addAliases_alias(v);
		}else if (xmlPath=="publications/publication"){
			this.addPublications_publication(v);
		}else if (xmlPath=="resources/resource"){
			this.addResources_resource(v);
		}else if (xmlPath=="studyProtocol"){
			this.addStudyprotocol(v);
		}else if (xmlPath=="PI"){
			this.setPi(v);
		}else if (xmlPath=="investigators/investigator"){
			this.addInvestigators_investigator(v);
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
		if (xmlPath=="aliases/alias"){
			return "http://nrg.wustl.edu/xnat:projectData_alias";
		}else if (xmlPath=="publications/publication"){
			return "http://nrg.wustl.edu/xnat:publicationResource";
		}else if (xmlPath=="resources/resource"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="studyProtocol"){
			return "http://nrg.wustl.edu/xnat:abstractProtocol";
		}else if (xmlPath=="PI"){
			return "http://nrg.wustl.edu/xnat:investigatorData";
		}else if (xmlPath=="investigators/investigator"){
			return "http://nrg.wustl.edu/xnat:investigatorData";
		}else if (xmlPath=="fields/field"){
			return "http://nrg.wustl.edu/xnat:projectData_field";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_LONG_DATA";
		}else if (xmlPath=="keywords"){
			return "field_data";
		}else if (xmlPath=="aliases/alias"){
			return "field_NO_CHILD";
		}else if (xmlPath=="publications/publication"){
			return "field_multi_reference";
		}else if (xmlPath=="resources/resource"){
			return "field_multi_reference";
		}else if (xmlPath=="studyProtocol"){
			return "field_multi_reference";
		}else if (xmlPath=="PI"){
			return "field_single_reference";
		}else if (xmlPath=="investigators/investigator"){
			return "field_multi_reference";
		}else if (xmlPath=="fields/field"){
			return "field_NO_CHILD";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="secondary_ID"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:Project";
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
		xmlTxt+="\n</xnat:Project>";
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

		if (this.SecondaryId!=null)
			attTxt+=" secondary_ID=\"" +this.SecondaryId +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Name!=null){
			xmlTxt+="\n<xnat:name";
			xmlTxt+=">";
			xmlTxt+=this.Name.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:name>";
		}
		if (this.Description!=null){
			xmlTxt+="\n<xnat:description";
			xmlTxt+=">";
			xmlTxt+=this.Description.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:description>";
		}
		if (this.Keywords!=null){
			xmlTxt+="\n<xnat:keywords";
			xmlTxt+=">";
			xmlTxt+=this.Keywords.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:keywords>";
		}
			var child0=0;
			var att0=0;
			child0+=this.Aliases_alias.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:aliases";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Aliases_aliasCOUNT=0;Aliases_aliasCOUNT<this.Aliases_alias.length;Aliases_aliasCOUNT++){
			xmlTxt +="\n<xnat:alias";
			xmlTxt +=this.Aliases_alias[Aliases_aliasCOUNT].getXMLAtts();
			if(this.Aliases_alias[Aliases_aliasCOUNT].xsiType!="xnat:projectData_alias"){
				xmlTxt+=" xsi:type=\"" + this.Aliases_alias[Aliases_aliasCOUNT].xsiType + "\"";
			}
			if (this.Aliases_alias[Aliases_aliasCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Aliases_alias[Aliases_aliasCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:alias>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:aliases>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.Publications_publication.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:publications";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Publications_publicationCOUNT=0;Publications_publicationCOUNT<this.Publications_publication.length;Publications_publicationCOUNT++){
			xmlTxt +="\n<xnat:publication";
			xmlTxt +=this.Publications_publication[Publications_publicationCOUNT].getXMLAtts();
			if(this.Publications_publication[Publications_publicationCOUNT].xsiType!="xnat:publicationResource"){
				xmlTxt+=" xsi:type=\"" + this.Publications_publication[Publications_publicationCOUNT].xsiType + "\"";
			}
			if (this.Publications_publication[Publications_publicationCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Publications_publication[Publications_publicationCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:publication>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:publications>";
			}
			}

			var child2=0;
			var att2=0;
			child2+=this.Resources_resource.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xnat:resources";
			if(child2==0){
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

		for(var StudyprotocolCOUNT=0;StudyprotocolCOUNT<this.Studyprotocol.length;StudyprotocolCOUNT++){
			xmlTxt +="\n<xnat:studyProtocol";
			xmlTxt +=this.Studyprotocol[StudyprotocolCOUNT].getXMLAtts();
			if(this.Studyprotocol[StudyprotocolCOUNT].xsiType!="xnat:abstractProtocol"){
				xmlTxt+=" xsi:type=\"" + this.Studyprotocol[StudyprotocolCOUNT].xsiType + "\"";
			}
			if (this.Studyprotocol[StudyprotocolCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Studyprotocol[StudyprotocolCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:studyProtocol>";
			}else {xmlTxt+="/>";}
		}
		if (this.Pi!=null){
			xmlTxt+="\n<xnat:PI";
			xmlTxt+=this.Pi.getXMLAtts();
			if(this.Pi.xsiType!="xnat:investigatorData"){
				xmlTxt+=" xsi:type=\"" + this.Pi.xsiType + "\"";
			}
			if (this.Pi.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Pi.getXMLBody(preventComments);
				xmlTxt+="</xnat:PI>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

			var child3=0;
			var att3=0;
			child3+=this.Investigators_investigator.length;
			if(child3>0 || att3>0){
				xmlTxt+="\n<xnat:investigators";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Investigators_investigatorCOUNT=0;Investigators_investigatorCOUNT<this.Investigators_investigator.length;Investigators_investigatorCOUNT++){
			xmlTxt +="\n<xnat:investigator";
			xmlTxt +=this.Investigators_investigator[Investigators_investigatorCOUNT].getXMLAtts();
			if(this.Investigators_investigator[Investigators_investigatorCOUNT].xsiType!="xnat:investigatorData"){
				xmlTxt+=" xsi:type=\"" + this.Investigators_investigator[Investigators_investigatorCOUNT].xsiType + "\"";
			}
			if (this.Investigators_investigator[Investigators_investigatorCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Investigators_investigator[Investigators_investigatorCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:investigator>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:investigators>";
			}
			}

			var child4=0;
			var att4=0;
			child4+=this.Fields_field.length;
			if(child4>0 || att4>0){
				xmlTxt+="\n<xnat:fields";
			if(child4==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Fields_fieldCOUNT=0;Fields_fieldCOUNT<this.Fields_field.length;Fields_fieldCOUNT++){
			xmlTxt +="\n<xnat:field";
			xmlTxt +=this.Fields_field[Fields_fieldCOUNT].getXMLAtts();
			if(this.Fields_field[Fields_fieldCOUNT].xsiType!="xnat:projectData_field"){
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
	}


	this.hasXMLBodyContent=function(){
		if (this.Name!=null) return true;
		if (this.Description!=null) return true;
		if (this.Keywords!=null) return true;
			if(this.Aliases_alias.length>0)return true;
			if(this.Publications_publication.length>0)return true;
			if(this.Resources_resource.length>0)return true;
		if(this.Studyprotocol.length>0) return true;
		if (this.Pi!=null){
			if (this.Pi.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

			if(this.Investigators_investigator.length>0)return true;
			if(this.Fields_field.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
