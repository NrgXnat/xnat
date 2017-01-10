/*
 * web: arc_project.js
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

function arc_project(){
this.xsiType="arc:project";

	this.getSchemaElementName=function(){
		return "project";
	}

	this.getFullSchemaElementName=function(){
		return "arc:project";
	}
	this.Paths =null;
	function getPaths() {
		return this.Paths;
	}
	this.getPaths=getPaths;


	function setPaths(v){
		this.Paths =v;
	}
	this.setPaths=setPaths;

	this.Paths_PathsArcPathinfoId=null;


	function getPaths_PathsArcPathinfoId(){
		return this.Paths_PathsArcPathinfoId;
	}
	this.getPaths_PathsArcPathinfoId=getPaths_PathsArcPathinfoId;


	function setPaths_PathsArcPathinfoId(v){
		this.Paths_PathsArcPathinfoId=v;
	}
	this.setPaths_PathsArcPathinfoId=setPaths_PathsArcPathinfoId;
	this.Fieldspecifications_fieldspecification =new Array();

	function getFieldspecifications_fieldspecification() {
		return this.Fieldspecifications_fieldspecification;
	}
	this.getFieldspecifications_fieldspecification=getFieldspecifications_fieldspecification;


	function addFieldspecifications_fieldspecification(v){
		this.Fieldspecifications_fieldspecification.push(v);
	}
	this.addFieldspecifications_fieldspecification=addFieldspecifications_fieldspecification;
	this.Properties_property =new Array();

	function getProperties_property() {
		return this.Properties_property;
	}
	this.getProperties_property=getProperties_property;


	function addProperties_property(v){
		this.Properties_property.push(v);
	}
	this.addProperties_property=addProperties_property;
	this.Pipelines_descendants_descendant =new Array();

	function getPipelines_descendants_descendant() {
		return this.Pipelines_descendants_descendant;
	}
	this.getPipelines_descendants_descendant=getPipelines_descendants_descendant;


	function addPipelines_descendants_descendant(v){
		this.Pipelines_descendants_descendant.push(v);
	}
	this.addPipelines_descendants_descendant=addPipelines_descendants_descendant;
	this.Pipelines_pipeline =new Array();

	function getPipelines_pipeline() {
		return this.Pipelines_pipeline;
	}
	this.getPipelines_pipeline=getPipelines_pipeline;


	function addPipelines_pipeline(v){
		this.Pipelines_pipeline.push(v);
	}
	this.addPipelines_pipeline=addPipelines_pipeline;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.CurrentArc=null;


	function getCurrentArc() {
		return this.CurrentArc;
	}
	this.getCurrentArc=getCurrentArc;


	function setCurrentArc(v){
		this.CurrentArc=v;
	}
	this.setCurrentArc=setCurrentArc;

	this.QuarantineCode=null;


	function getQuarantineCode() {
		return this.QuarantineCode;
	}
	this.getQuarantineCode=getQuarantineCode;


	function setQuarantineCode(v){
		this.QuarantineCode=v;
	}
	this.setQuarantineCode=setQuarantineCode;

	this.PrearchiveCode=null;


	function getPrearchiveCode() {
		return this.PrearchiveCode;
	}
	this.getPrearchiveCode=getPrearchiveCode;


	function setPrearchiveCode(v){
		this.PrearchiveCode=v;
	}
	this.setPrearchiveCode=setPrearchiveCode;

	this.ArcProjectId=null;


	function getArcProjectId() {
		return this.ArcProjectId;
	}
	this.getArcProjectId=getArcProjectId;


	function setArcProjectId(v){
		this.ArcProjectId=v;
	}
	this.setArcProjectId=setArcProjectId;

	this.projects_project_arc_ArchiveSpe_arc_archivespecification_id_fk=null;


	this.getprojects_project_arc_ArchiveSpe_arc_archivespecification_id=function() {
		return this.projects_project_arc_ArchiveSpe_arc_archivespecification_id_fk;
	}


	this.setprojects_project_arc_ArchiveSpe_arc_archivespecification_id=function(v){
		this.projects_project_arc_ArchiveSpe_arc_archivespecification_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="paths"){
				return this.Paths ;
			} else 
			if(xmlPath.startsWith("paths")){
				xmlPath=xmlPath.substring(5);
				if(xmlPath=="")return this.Paths ;
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
				if(this.Paths!=undefined)return this.Paths.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="fieldSpecifications/fieldSpecification"){
				return this.Fieldspecifications_fieldspecification ;
			} else 
			if(xmlPath.startsWith("fieldSpecifications/fieldSpecification")){
				xmlPath=xmlPath.substring(38);
				if(xmlPath=="")return this.Fieldspecifications_fieldspecification ;
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

				for(var whereCount=0;whereCount<this.Fieldspecifications_fieldspecification.length;whereCount++){

					var tempValue=this.Fieldspecifications_fieldspecification[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Fieldspecifications_fieldspecification[whereCount]);

					}

				}
				}else{

				whereArray=this.Fieldspecifications_fieldspecification;
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
			if(xmlPath=="properties/property"){
				return this.Properties_property ;
			} else 
			if(xmlPath.startsWith("properties/property")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Properties_property ;
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

				for(var whereCount=0;whereCount<this.Properties_property.length;whereCount++){

					var tempValue=this.Properties_property[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Properties_property[whereCount]);

					}

				}
				}else{

				whereArray=this.Properties_property;
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
			if(xmlPath=="pipelines/descendants/descendant"){
				return this.Pipelines_descendants_descendant ;
			} else 
			if(xmlPath.startsWith("pipelines/descendants/descendant")){
				xmlPath=xmlPath.substring(32);
				if(xmlPath=="")return this.Pipelines_descendants_descendant ;
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

				for(var whereCount=0;whereCount<this.Pipelines_descendants_descendant.length;whereCount++){

					var tempValue=this.Pipelines_descendants_descendant[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Pipelines_descendants_descendant[whereCount]);

					}

				}
				}else{

				whereArray=this.Pipelines_descendants_descendant;
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
			if(xmlPath=="pipelines/pipeline"){
				return this.Pipelines_pipeline ;
			} else 
			if(xmlPath.startsWith("pipelines/pipeline")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Pipelines_pipeline ;
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

				for(var whereCount=0;whereCount<this.Pipelines_pipeline.length;whereCount++){

					var tempValue=this.Pipelines_pipeline[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Pipelines_pipeline[whereCount]);

					}

				}
				}else{

				whereArray=this.Pipelines_pipeline;
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
			if(xmlPath=="id"){
				return this.Id ;
			} else 
			if(xmlPath=="current_arc"){
				return this.CurrentArc ;
			} else 
			if(xmlPath=="quarantine_code"){
				return this.QuarantineCode ;
			} else 
			if(xmlPath=="prearchive_code"){
				return this.PrearchiveCode ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="arc_project_id"){
				return this.ArcProjectId ;
			} else 
			if(xmlPath=="projects_project_arc_ArchiveSpe_arc_archivespecification_id"){
				return this.projects_project_arc_ArchiveSpe_arc_archivespecification_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="paths"){
				this.Paths=value;
			} else 
			if(xmlPath.startsWith("paths")){
				xmlPath=xmlPath.substring(5);
				if(xmlPath=="")return this.Paths ;
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
				if(this.Paths!=undefined){
					this.Paths.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Paths= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Paths= instanciateObject("arc:pathInfo");//omUtils.js
						}
						if(options && options.where)this.Paths.setProperty(options.where.field,options.where.value);
						this.Paths.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="fieldSpecifications/fieldSpecification"){
				this.Fieldspecifications_fieldspecification=value;
			} else 
			if(xmlPath.startsWith("fieldSpecifications/fieldSpecification")){
				xmlPath=xmlPath.substring(38);
				if(xmlPath=="")return this.Fieldspecifications_fieldspecification ;
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

				for(var whereCount=0;whereCount<this.Fieldspecifications_fieldspecification.length;whereCount++){

					var tempValue=this.Fieldspecifications_fieldspecification[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Fieldspecifications_fieldspecification[whereCount]);

					}

				}
				}else{

				whereArray=this.Fieldspecifications_fieldspecification;
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
						newChild= instanciateObject("arc:fieldSpecification");//omUtils.js
					}
					this.addFieldspecifications_fieldspecification(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="properties/property"){
				this.Properties_property=value;
			} else 
			if(xmlPath.startsWith("properties/property")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Properties_property ;
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

				for(var whereCount=0;whereCount<this.Properties_property.length;whereCount++){

					var tempValue=this.Properties_property[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Properties_property[whereCount]);

					}

				}
				}else{

				whereArray=this.Properties_property;
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
						newChild= instanciateObject("arc:property");//omUtils.js
					}
					this.addProperties_property(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="pipelines/descendants/descendant"){
				this.Pipelines_descendants_descendant=value;
			} else 
			if(xmlPath.startsWith("pipelines/descendants/descendant")){
				xmlPath=xmlPath.substring(32);
				if(xmlPath=="")return this.Pipelines_descendants_descendant ;
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

				for(var whereCount=0;whereCount<this.Pipelines_descendants_descendant.length;whereCount++){

					var tempValue=this.Pipelines_descendants_descendant[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Pipelines_descendants_descendant[whereCount]);

					}

				}
				}else{

				whereArray=this.Pipelines_descendants_descendant;
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
						newChild= instanciateObject("arc:project_descendant");//omUtils.js
					}
					this.addPipelines_descendants_descendant(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="pipelines/pipeline"){
				this.Pipelines_pipeline=value;
			} else 
			if(xmlPath.startsWith("pipelines/pipeline")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Pipelines_pipeline ;
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

				for(var whereCount=0;whereCount<this.Pipelines_pipeline.length;whereCount++){

					var tempValue=this.Pipelines_pipeline[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Pipelines_pipeline[whereCount]);

					}

				}
				}else{

				whereArray=this.Pipelines_pipeline;
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
						newChild= instanciateObject("arc:project_pipeline");//omUtils.js
					}
					this.addPipelines_pipeline(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="id"){
				this.Id=value;
			} else 
			if(xmlPath=="current_arc"){
				this.CurrentArc=value;
			} else 
			if(xmlPath=="quarantine_code"){
				this.QuarantineCode=value;
			} else 
			if(xmlPath=="prearchive_code"){
				this.PrearchiveCode=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="arc_project_id"){
				this.ArcProjectId=value;
			} else 
			if(xmlPath=="projects_project_arc_ArchiveSpe_arc_archivespecification_id"){
				this.projects_project_arc_ArchiveSpe_arc_archivespecification_id_fk=value;
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
		if (xmlPath=="paths"){
			this.setPaths(v);
		}else if (xmlPath=="fieldSpecifications/fieldSpecification"){
			this.addFieldspecifications_fieldspecification(v);
		}else if (xmlPath=="properties/property"){
			this.addProperties_property(v);
		}else if (xmlPath=="pipelines/descendants/descendant"){
			this.addPipelines_descendants_descendant(v);
		}else if (xmlPath=="pipelines/pipeline"){
			this.addPipelines_pipeline(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="paths"){
			return "http://nrg.wustl.edu/arc:pathInfo";
		}else if (xmlPath=="fieldSpecifications/fieldSpecification"){
			return "http://nrg.wustl.edu/arc:fieldSpecification";
		}else if (xmlPath=="properties/property"){
			return "http://nrg.wustl.edu/arc:property";
		}else if (xmlPath=="pipelines/descendants/descendant"){
			return "http://nrg.wustl.edu/arc:project_descendant";
		}else if (xmlPath=="pipelines/pipeline"){
			return "http://nrg.wustl.edu/arc:project_pipeline";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="paths"){
			return "field_single_reference";
		}else if (xmlPath=="fieldSpecifications/fieldSpecification"){
			return "field_NO_CHILD";
		}else if (xmlPath=="properties/property"){
			return "field_NO_CHILD";
		}else if (xmlPath=="pipelines/descendants/descendant"){
			return "field_multi_reference";
		}else if (xmlPath=="pipelines/pipeline"){
			return "field_multi_reference";
		}else if (xmlPath=="id"){
			return "field_data";
		}else if (xmlPath=="current_arc"){
			return "field_data";
		}else if (xmlPath=="quarantine_code"){
			return "field_data";
		}else if (xmlPath=="prearchive_code"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<arc:project";
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
		xmlTxt+="\n</arc:project>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ArcProjectId!=null){
				if(hiddenCount++>0)str+=",";
				str+="arc_project_id=\"" + this.ArcProjectId + "\"";
			}
			if(this.projects_project_arc_ArchiveSpe_arc_archivespecification_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="projects_project_arc_ArchiveSpe_arc_archivespecification_id=\"" + this.projects_project_arc_ArchiveSpe_arc_archivespecification_id_fk + "\"";
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

		if (this.CurrentArc!=null)
			attTxt+=" current_arc=\"" +this.CurrentArc +"\"";
		//NOT REQUIRED FIELD

		if (this.QuarantineCode!=null)
			attTxt+=" quarantine_code=\"" +this.QuarantineCode +"\"";
		//NOT REQUIRED FIELD

		if (this.PrearchiveCode!=null)
			attTxt+=" prearchive_code=\"" +this.PrearchiveCode +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Paths!=null){
			xmlTxt+="\n<arc:paths";
			xmlTxt+=this.Paths.getXMLAtts();
			if(this.Paths.xsiType!="arc:pathInfo"){
				xmlTxt+=" xsi:type=\"" + this.Paths.xsiType + "\"";
			}
			if (this.Paths.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Paths.getXMLBody(preventComments);
				xmlTxt+="</arc:paths>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

			var child0=0;
			var att0=0;
			child0+=this.Fieldspecifications_fieldspecification.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<arc:fieldSpecifications";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Fieldspecifications_fieldspecificationCOUNT=0;Fieldspecifications_fieldspecificationCOUNT<this.Fieldspecifications_fieldspecification.length;Fieldspecifications_fieldspecificationCOUNT++){
			xmlTxt +="\n<arc:fieldSpecification";
			xmlTxt +=this.Fieldspecifications_fieldspecification[Fieldspecifications_fieldspecificationCOUNT].getXMLAtts();
			if(this.Fieldspecifications_fieldspecification[Fieldspecifications_fieldspecificationCOUNT].xsiType!="arc:fieldSpecification"){
				xmlTxt+=" xsi:type=\"" + this.Fieldspecifications_fieldspecification[Fieldspecifications_fieldspecificationCOUNT].xsiType + "\"";
			}
			if (this.Fieldspecifications_fieldspecification[Fieldspecifications_fieldspecificationCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Fieldspecifications_fieldspecification[Fieldspecifications_fieldspecificationCOUNT].getXMLBody(preventComments);
					xmlTxt+="</arc:fieldSpecification>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</arc:fieldSpecifications>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.Properties_property.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<arc:properties";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Properties_propertyCOUNT=0;Properties_propertyCOUNT<this.Properties_property.length;Properties_propertyCOUNT++){
			xmlTxt +="\n<arc:property";
			xmlTxt +=this.Properties_property[Properties_propertyCOUNT].getXMLAtts();
			if(this.Properties_property[Properties_propertyCOUNT].xsiType!="arc:property"){
				xmlTxt+=" xsi:type=\"" + this.Properties_property[Properties_propertyCOUNT].xsiType + "\"";
			}
			if (this.Properties_property[Properties_propertyCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Properties_property[Properties_propertyCOUNT].getXMLBody(preventComments);
					xmlTxt+="</arc:property>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</arc:properties>";
			}
			}

			var child2=0;
			var att2=0;
			child2+=this.Pipelines_pipeline.length;
			child2+=this.Pipelines_descendants_descendant.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<arc:pipelines";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
			var child3=0;
			var att3=0;
			child3+=this.Pipelines_descendants_descendant.length;
			if(child3>0 || att3>0){
				xmlTxt+="\n<arc:descendants";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Pipelines_descendants_descendantCOUNT=0;Pipelines_descendants_descendantCOUNT<this.Pipelines_descendants_descendant.length;Pipelines_descendants_descendantCOUNT++){
			xmlTxt +="\n<arc:descendant";
			xmlTxt +=this.Pipelines_descendants_descendant[Pipelines_descendants_descendantCOUNT].getXMLAtts();
			if(this.Pipelines_descendants_descendant[Pipelines_descendants_descendantCOUNT].xsiType!="arc:project_descendant"){
				xmlTxt+=" xsi:type=\"" + this.Pipelines_descendants_descendant[Pipelines_descendants_descendantCOUNT].xsiType + "\"";
			}
			if (this.Pipelines_descendants_descendant[Pipelines_descendants_descendantCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Pipelines_descendants_descendant[Pipelines_descendants_descendantCOUNT].getXMLBody(preventComments);
					xmlTxt+="</arc:descendant>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</arc:descendants>";
			}
			}

		for(var Pipelines_pipelineCOUNT=0;Pipelines_pipelineCOUNT<this.Pipelines_pipeline.length;Pipelines_pipelineCOUNT++){
			xmlTxt +="\n<arc:pipeline";
			xmlTxt +=this.Pipelines_pipeline[Pipelines_pipelineCOUNT].getXMLAtts();
			if(this.Pipelines_pipeline[Pipelines_pipelineCOUNT].xsiType!="arc:project_pipeline"){
				xmlTxt+=" xsi:type=\"" + this.Pipelines_pipeline[Pipelines_pipelineCOUNT].xsiType + "\"";
			}
			if (this.Pipelines_pipeline[Pipelines_pipelineCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Pipelines_pipeline[Pipelines_pipelineCOUNT].getXMLBody(preventComments);
					xmlTxt+="</arc:pipeline>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</arc:pipelines>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ArcProjectId!=null) return true;
			if (this.projects_project_arc_ArchiveSpe_arc_archivespecification_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Paths!=null){
			if (this.Paths.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

			if(this.Fieldspecifications_fieldspecification.length>0)return true;
			if(this.Properties_property.length>0)return true;
			if(this.Pipelines_pipeline.length>0)return true;
			if(this.Pipelines_descendants_descendant.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
