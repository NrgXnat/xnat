/*
 * web: xnat_subjectData.js
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

function xnat_subjectData(){
this.xsiType="xnat:subjectData";

	this.getSchemaElementName=function(){
		return "subjectData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:subjectData";
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
	this.Resources_resource =new Array();

	function getResources_resource() {
		return this.Resources_resource;
	}
	this.getResources_resource=getResources_resource;


	function addResources_resource(v){
		this.Resources_resource.push(v);
	}
	this.addResources_resource=addResources_resource;
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
	this.Demographics =null;
	function getDemographics() {
		return this.Demographics;
	}
	this.getDemographics=getDemographics;


	function setDemographics(v){
		this.Demographics =v;
	}
	this.setDemographics=setDemographics;

	this.Demographics_DemographicsXnatAbstractdemographicdataId=null;


	function getDemographics_DemographicsXnatAbstractdemographicdataId(){
		return this.Demographics_DemographicsXnatAbstractdemographicdataId;
	}
	this.getDemographics_DemographicsXnatAbstractdemographicdataId=getDemographics_DemographicsXnatAbstractdemographicdataId;


	function setDemographics_DemographicsXnatAbstractdemographicdataId(v){
		this.Demographics_DemographicsXnatAbstractdemographicdataId=v;
	}
	this.setDemographics_DemographicsXnatAbstractdemographicdataId=setDemographics_DemographicsXnatAbstractdemographicdataId;
	this.Metadata =null;
	function getMetadata() {
		return this.Metadata;
	}
	this.getMetadata=getMetadata;


	function setMetadata(v){
		this.Metadata =v;
	}
	this.setMetadata=setMetadata;

	this.Metadata_MetadataXnatAbstractsubjectmetadataId=null;


	function getMetadata_MetadataXnatAbstractsubjectmetadataId(){
		return this.Metadata_MetadataXnatAbstractsubjectmetadataId;
	}
	this.getMetadata_MetadataXnatAbstractsubjectmetadataId=getMetadata_MetadataXnatAbstractsubjectmetadataId;


	function setMetadata_MetadataXnatAbstractsubjectmetadataId(v){
		this.Metadata_MetadataXnatAbstractsubjectmetadataId=v;
	}
	this.setMetadata_MetadataXnatAbstractsubjectmetadataId=setMetadata_MetadataXnatAbstractsubjectmetadataId;
	this.Addid =new Array();

	function getAddid() {
		return this.Addid;
	}
	this.getAddid=getAddid;


	function addAddid(v){
		this.Addid.push(v);
	}
	this.addAddid=addAddid;
	this.Fields_field =new Array();

	function getFields_field() {
		return this.Fields_field;
	}
	this.getFields_field=getFields_field;


	function addFields_field(v){
		this.Fields_field.push(v);
	}
	this.addFields_field=addFields_field;
	this.Experiments_experiment =new Array();

	function getExperiments_experiment() {
		return this.Experiments_experiment;
	}
	this.getExperiments_experiment=getExperiments_experiment;


	function addExperiments_experiment(v){
		this.Experiments_experiment.push(v);
	}
	this.addExperiments_experiment=addExperiments_experiment;

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

	this.Group=null;


	function getGroup() {
		return this.Group;
	}
	this.getGroup=getGroup;


	function setGroup(v){
		this.Group=v;
	}
	this.setGroup=setGroup;

	this.Label=null;


	function getLabel() {
		return this.Label;
	}
	this.getLabel=getLabel;


	function setLabel(v){
		this.Label=v;
	}
	this.setLabel=setLabel;

	this.Src=null;


	function getSrc() {
		return this.Src;
	}
	this.getSrc=getSrc;


	function setSrc(v){
		this.Src=v;
	}
	this.setSrc=setSrc;

	this.Initials=null;


	function getInitials() {
		return this.Initials;
	}
	this.getInitials=getInitials;


	function setInitials(v){
		this.Initials=v;
	}
	this.setInitials=setInitials;


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
			if(xmlPath=="demographics"){
				return this.Demographics ;
			} else 
			if(xmlPath.startsWith("demographics")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Demographics ;
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
				if(this.Demographics!=undefined)return this.Demographics.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="metadata"){
				return this.Metadata ;
			} else 
			if(xmlPath.startsWith("metadata")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Metadata ;
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
				if(this.Metadata!=undefined)return this.Metadata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="addID"){
				return this.Addid ;
			} else 
			if(xmlPath.startsWith("addID")){
				xmlPath=xmlPath.substring(5);
				if(xmlPath=="")return this.Addid ;
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

				for(var whereCount=0;whereCount<this.Addid.length;whereCount++){

					var tempValue=this.Addid[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Addid[whereCount]);

					}

				}
				}else{

				whereArray=this.Addid;
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
			if(xmlPath=="experiments/experiment"){
				return this.Experiments_experiment ;
			} else 
			if(xmlPath.startsWith("experiments/experiment")){
				xmlPath=xmlPath.substring(22);
				if(xmlPath=="")return this.Experiments_experiment ;
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

				for(var whereCount=0;whereCount<this.Experiments_experiment.length;whereCount++){

					var tempValue=this.Experiments_experiment[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Experiments_experiment[whereCount]);

					}

				}
				}else{

				whereArray=this.Experiments_experiment;
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
			if(xmlPath=="project"){
				return this.Project ;
			} else 
			if(xmlPath=="group"){
				return this.Group ;
			} else 
			if(xmlPath=="label"){
				return this.Label ;
			} else 
			if(xmlPath=="src"){
				return this.Src ;
			} else 
			if(xmlPath=="initials"){
				return this.Initials ;
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
						newChild= instanciateObject("xnat:projectParticipant");//omUtils.js
					}
					this.addSharing_share(newChild);
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
			if(xmlPath=="demographics"){
				this.Demographics=value;
			} else 
			if(xmlPath.startsWith("demographics")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Demographics ;
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
				if(this.Demographics!=undefined){
					this.Demographics.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Demographics= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Demographics= instanciateObject("xnat:abstractDemographicData");//omUtils.js
						}
						if(options && options.where)this.Demographics.setProperty(options.where.field,options.where.value);
						this.Demographics.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="metadata"){
				this.Metadata=value;
			} else 
			if(xmlPath.startsWith("metadata")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Metadata ;
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
				if(this.Metadata!=undefined){
					this.Metadata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Metadata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Metadata= instanciateObject("xnat:abstractSubjectMetadata");//omUtils.js
						}
						if(options && options.where)this.Metadata.setProperty(options.where.field,options.where.value);
						this.Metadata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="addID"){
				this.Addid=value;
			} else 
			if(xmlPath.startsWith("addID")){
				xmlPath=xmlPath.substring(5);
				if(xmlPath=="")return this.Addid ;
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

				for(var whereCount=0;whereCount<this.Addid.length;whereCount++){

					var tempValue=this.Addid[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Addid[whereCount]);

					}

				}
				}else{

				whereArray=this.Addid;
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
						newChild= instanciateObject("xnat:subjectData_addID");//omUtils.js
					}
					this.addAddid(newChild);
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
						newChild= instanciateObject("xnat:subjectData_field");//omUtils.js
					}
					this.addFields_field(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="experiments/experiment"){
				this.Experiments_experiment=value;
			} else 
			if(xmlPath.startsWith("experiments/experiment")){
				xmlPath=xmlPath.substring(22);
				if(xmlPath=="")return this.Experiments_experiment ;
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

				for(var whereCount=0;whereCount<this.Experiments_experiment.length;whereCount++){

					var tempValue=this.Experiments_experiment[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Experiments_experiment[whereCount]);

					}

				}
				}else{

				whereArray=this.Experiments_experiment;
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
						newChild= instanciateObject("xnat:subjectAssessorData");//omUtils.js
					}
					this.addExperiments_experiment(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="project"){
				this.Project=value;
			} else 
			if(xmlPath=="group"){
				this.Group=value;
			} else 
			if(xmlPath=="label"){
				this.Label=value;
			} else 
			if(xmlPath=="src"){
				this.Src=value;
			} else 
			if(xmlPath=="initials"){
				this.Initials=value;
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
		}else if (xmlPath=="resources/resource"){
			this.addResources_resource(v);
		}else if (xmlPath=="investigator"){
			this.setInvestigator(v);
		}else if (xmlPath=="demographics"){
			this.setDemographics(v);
		}else if (xmlPath=="metadata"){
			this.setMetadata(v);
		}else if (xmlPath=="addID"){
			this.addAddid(v);
		}else if (xmlPath=="fields/field"){
			this.addFields_field(v);
		}else if (xmlPath=="experiments/experiment"){
			this.addExperiments_experiment(v);
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
			return "http://nrg.wustl.edu/xnat:projectParticipant";
		}else if (xmlPath=="resources/resource"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="investigator"){
			return "http://nrg.wustl.edu/xnat:investigatorData";
		}else if (xmlPath=="demographics"){
			return "http://nrg.wustl.edu/xnat:abstractDemographicData";
		}else if (xmlPath=="metadata"){
			return "http://nrg.wustl.edu/xnat:abstractSubjectMetadata";
		}else if (xmlPath=="addID"){
			return "http://nrg.wustl.edu/xnat:subjectData_addID";
		}else if (xmlPath=="fields/field"){
			return "http://nrg.wustl.edu/xnat:subjectData_field";
		}else if (xmlPath=="experiments/experiment"){
			return "http://nrg.wustl.edu/xnat:subjectAssessorData";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="sharing/share"){
			return "field_multi_reference";
		}else if (xmlPath=="resources/resource"){
			return "field_multi_reference";
		}else if (xmlPath=="investigator"){
			return "field_single_reference";
		}else if (xmlPath=="demographics"){
			return "field_single_reference";
		}else if (xmlPath=="metadata"){
			return "field_single_reference";
		}else if (xmlPath=="addID"){
			return "field_NO_CHILD";
		}else if (xmlPath=="fields/field"){
			return "field_NO_CHILD";
		}else if (xmlPath=="experiments/experiment"){
			return "field_multi_reference";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="project"){
			return "field_data";
		}else if (xmlPath=="group"){
			return "field_data";
		}else if (xmlPath=="label"){
			return "field_data";
		}else if (xmlPath=="src"){
			return "field_data";
		}else if (xmlPath=="initials"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:Subject";
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
		xmlTxt+="\n</xnat:Subject>";
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

		if (this.Group!=null)
			attTxt+=" group=\"" +this.Group +"\"";
		//NOT REQUIRED FIELD

		if (this.Label!=null)
			attTxt+=" label=\"" +this.Label +"\"";
		//NOT REQUIRED FIELD

		if (this.Src!=null)
			attTxt+=" src=\"" +this.Src +"\"";
		//NOT REQUIRED FIELD

		if (this.Initials!=null)
			attTxt+=" initials=\"" +this.Initials +"\"";
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
			if(this.Sharing_share[Sharing_shareCOUNT].xsiType!="xnat:projectParticipant"){
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

		if (this.Demographics!=null){
			xmlTxt+="\n<xnat:demographics";
			xmlTxt+=this.Demographics.getXMLAtts();
			if(this.Demographics.xsiType!="xnat:abstractDemographicData"){
				xmlTxt+=" xsi:type=\"" + this.Demographics.xsiType + "\"";
			}
			if (this.Demographics.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Demographics.getXMLBody(preventComments);
				xmlTxt+="</xnat:demographics>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

		if (this.Metadata!=null){
			xmlTxt+="\n<xnat:metadata";
			xmlTxt+=this.Metadata.getXMLAtts();
			if(this.Metadata.xsiType!="xnat:abstractSubjectMetadata"){
				xmlTxt+=" xsi:type=\"" + this.Metadata.xsiType + "\"";
			}
			if (this.Metadata.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Metadata.getXMLBody(preventComments);
				xmlTxt+="</xnat:metadata>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

		for(var AddidCOUNT=0;AddidCOUNT<this.Addid.length;AddidCOUNT++){
			xmlTxt +="\n<xnat:addID";
			xmlTxt +=this.Addid[AddidCOUNT].getXMLAtts();
			if(this.Addid[AddidCOUNT].xsiType!="xnat:subjectData_addID"){
				xmlTxt+=" xsi:type=\"" + this.Addid[AddidCOUNT].xsiType + "\"";
			}
			if (this.Addid[AddidCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Addid[AddidCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:addID>";
			}else {xmlTxt+="/>";}
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
			if(this.Fields_field[Fields_fieldCOUNT].xsiType!="xnat:subjectData_field"){
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

			var child3=0;
			var att3=0;
			child3+=this.Experiments_experiment.length;
			if(child3>0 || att3>0){
				xmlTxt+="\n<xnat:experiments";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Experiments_experimentCOUNT=0;Experiments_experimentCOUNT<this.Experiments_experiment.length;Experiments_experimentCOUNT++){
			xmlTxt +="\n<xnat:experiment";
			xmlTxt +=this.Experiments_experiment[Experiments_experimentCOUNT].getXMLAtts();
			if(this.Experiments_experiment[Experiments_experimentCOUNT].xsiType!="xnat:subjectAssessorData"){
				xmlTxt+=" xsi:type=\"" + this.Experiments_experiment[Experiments_experimentCOUNT].xsiType + "\"";
			}
			if (this.Experiments_experiment[Experiments_experimentCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Experiments_experiment[Experiments_experimentCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:experiment>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:experiments>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Sharing_share.length>0)return true;
			if(this.Resources_resource.length>0)return true;
		if (this.Investigator!=null){
			if (this.Investigator.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if (this.Demographics!=null){
			if (this.Demographics.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if (this.Metadata!=null){
			if (this.Metadata.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if(this.Addid.length>0) return true;
			if(this.Fields_field.length>0)return true;
			if(this.Experiments_experiment.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
