/*
 * web: cat_catalog.js
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

function cat_catalog(){
this.xsiType="cat:catalog";

	this.getSchemaElementName=function(){
		return "catalog";
	}

	this.getFullSchemaElementName=function(){
		return "cat:catalog";
	}
	this.Metafields_metafield =new Array();

	function getMetafields_metafield() {
		return this.Metafields_metafield;
	}
	this.getMetafields_metafield=getMetafields_metafield;


	function addMetafields_metafield(v){
		this.Metafields_metafield.push(v);
	}
	this.addMetafields_metafield=addMetafields_metafield;
	this.Tags_tag =new Array();

	function getTags_tag() {
		return this.Tags_tag;
	}
	this.getTags_tag=getTags_tag;


	function addTags_tag(v){
		this.Tags_tag.push(v);
	}
	this.addTags_tag=addTags_tag;
	this.Sets_entryset =new Array();

	function getSets_entryset() {
		return this.Sets_entryset;
	}
	this.getSets_entryset=getSets_entryset;


	function addSets_entryset(v){
		this.Sets_entryset.push(v);
	}
	this.addSets_entryset=addSets_entryset;
	this.Entries_entry =new Array();

	function getEntries_entry() {
		return this.Entries_entry;
	}
	this.getEntries_entry=getEntries_entry;


	function addEntries_entry(v){
		this.Entries_entry.push(v);
	}
	this.addEntries_entry=addEntries_entry;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

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

	this.CatCatalogId=null;


	function getCatCatalogId() {
		return this.CatCatalogId;
	}
	this.getCatCatalogId=getCatCatalogId;


	function setCatCatalogId(v){
		this.CatCatalogId=v;
	}
	this.setCatCatalogId=setCatCatalogId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="metaFields/metaField"){
				return this.Metafields_metafield ;
			} else 
			if(xmlPath.startsWith("metaFields/metaField")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Metafields_metafield ;
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

				for(var whereCount=0;whereCount<this.Metafields_metafield.length;whereCount++){

					var tempValue=this.Metafields_metafield[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Metafields_metafield[whereCount]);

					}

				}
				}else{

				whereArray=this.Metafields_metafield;
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
			if(xmlPath=="tags/tag"){
				return this.Tags_tag ;
			} else 
			if(xmlPath.startsWith("tags/tag")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Tags_tag ;
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

				for(var whereCount=0;whereCount<this.Tags_tag.length;whereCount++){

					var tempValue=this.Tags_tag[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Tags_tag[whereCount]);

					}

				}
				}else{

				whereArray=this.Tags_tag;
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
			if(xmlPath=="sets/entrySet"){
				return this.Sets_entryset ;
			} else 
			if(xmlPath.startsWith("sets/entrySet")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Sets_entryset ;
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

				for(var whereCount=0;whereCount<this.Sets_entryset.length;whereCount++){

					var tempValue=this.Sets_entryset[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Sets_entryset[whereCount]);

					}

				}
				}else{

				whereArray=this.Sets_entryset;
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
			if(xmlPath=="entries/entry"){
				return this.Entries_entry ;
			} else 
			if(xmlPath.startsWith("entries/entry")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Entries_entry ;
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

				for(var whereCount=0;whereCount<this.Entries_entry.length;whereCount++){

					var tempValue=this.Entries_entry[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Entries_entry[whereCount]);

					}

				}
				}else{

				whereArray=this.Entries_entry;
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
			if(xmlPath=="name"){
				return this.Name ;
			} else 
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="extension_item"){
				return this.ExtensionItem ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="cat_catalog_id"){
				return this.CatCatalogId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="metaFields/metaField"){
				this.Metafields_metafield=value;
			} else 
			if(xmlPath.startsWith("metaFields/metaField")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Metafields_metafield ;
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

				for(var whereCount=0;whereCount<this.Metafields_metafield.length;whereCount++){

					var tempValue=this.Metafields_metafield[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Metafields_metafield[whereCount]);

					}

				}
				}else{

				whereArray=this.Metafields_metafield;
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
						newChild= instanciateObject("cat:catalog_metaField");//omUtils.js
					}
					this.addMetafields_metafield(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="tags/tag"){
				this.Tags_tag=value;
			} else 
			if(xmlPath.startsWith("tags/tag")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Tags_tag ;
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

				for(var whereCount=0;whereCount<this.Tags_tag.length;whereCount++){

					var tempValue=this.Tags_tag[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Tags_tag[whereCount]);

					}

				}
				}else{

				whereArray=this.Tags_tag;
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
						newChild= instanciateObject("cat:catalog_tag");//omUtils.js
					}
					this.addTags_tag(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="sets/entrySet"){
				this.Sets_entryset=value;
			} else 
			if(xmlPath.startsWith("sets/entrySet")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Sets_entryset ;
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

				for(var whereCount=0;whereCount<this.Sets_entryset.length;whereCount++){

					var tempValue=this.Sets_entryset[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Sets_entryset[whereCount]);

					}

				}
				}else{

				whereArray=this.Sets_entryset;
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
						newChild= instanciateObject("cat:catalog");//omUtils.js
					}
					this.addSets_entryset(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="entries/entry"){
				this.Entries_entry=value;
			} else 
			if(xmlPath.startsWith("entries/entry")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Entries_entry ;
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

				for(var whereCount=0;whereCount<this.Entries_entry.length;whereCount++){

					var tempValue=this.Entries_entry[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Entries_entry[whereCount]);

					}

				}
				}else{

				whereArray=this.Entries_entry;
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
						newChild= instanciateObject("cat:entry");//omUtils.js
					}
					this.addEntries_entry(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="name"){
				this.Name=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="extension_item"){
				this.ExtensionItem=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="cat_catalog_id"){
				this.CatCatalogId=value;
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
		if (xmlPath=="metaFields/metaField"){
			this.addMetafields_metafield(v);
		}else if (xmlPath=="tags/tag"){
			this.addTags_tag(v);
		}else if (xmlPath=="sets/entrySet"){
			this.addSets_entryset(v);
		}else if (xmlPath=="entries/entry"){
			this.addEntries_entry(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="metaFields/metaField"){
			return "http://nrg.wustl.edu/catalog:catalog_metaField";
		}else if (xmlPath=="tags/tag"){
			return "http://nrg.wustl.edu/catalog:catalog_tag";
		}else if (xmlPath=="sets/entrySet"){
			return "http://nrg.wustl.edu/catalog:catalog";
		}else if (xmlPath=="entries/entry"){
			return "http://nrg.wustl.edu/catalog:entry";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="metaFields/metaField"){
			return "field_NO_CHILD";
		}else if (xmlPath=="tags/tag"){
			return "field_inline_repeater";
		}else if (xmlPath=="sets/entrySet"){
			return "field_multi_reference";
		}else if (xmlPath=="entries/entry"){
			return "field_multi_reference";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<cat:Catalog";
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
		xmlTxt+="\n</cat:Catalog>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.CatCatalogId!=null){
				if(hiddenCount++>0)str+=",";
				str+="cat_catalog_id=\"" + this.CatCatalogId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Id!=null)
			attTxt+=" ID=\"" +this.Id +"\"";
		//NOT REQUIRED FIELD

		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		//NOT REQUIRED FIELD

		if (this.Description!=null)
			attTxt+=" description=\"" +this.Description +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Metafields_metafield.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<cat:metaFields";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Metafields_metafieldCOUNT=0;Metafields_metafieldCOUNT<this.Metafields_metafield.length;Metafields_metafieldCOUNT++){
			xmlTxt +="\n<cat:metaField";
			xmlTxt +=this.Metafields_metafield[Metafields_metafieldCOUNT].getXMLAtts();
			if(this.Metafields_metafield[Metafields_metafieldCOUNT].xsiType!="cat:catalog_metaField"){
				xmlTxt+=" xsi:type=\"" + this.Metafields_metafield[Metafields_metafieldCOUNT].xsiType + "\"";
			}
			if (this.Metafields_metafield[Metafields_metafieldCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Metafields_metafield[Metafields_metafieldCOUNT].getXMLBody(preventComments);
					xmlTxt+="</cat:metaField>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</cat:metaFields>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.Tags_tag.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<cat:tags";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Tags_tagCOUNT=0;Tags_tagCOUNT<this.Tags_tag.length;Tags_tagCOUNT++){
			xmlTxt+=this.Tags_tag[Tags_tagCOUNT].getXMLBody(preventComments);
		}
				xmlTxt+="\n</cat:tags>";
			}
			}

			var child2=0;
			var att2=0;
			child2+=this.Sets_entryset.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<cat:sets";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Sets_entrysetCOUNT=0;Sets_entrysetCOUNT<this.Sets_entryset.length;Sets_entrysetCOUNT++){
			xmlTxt +="\n<cat:entrySet";
			xmlTxt +=this.Sets_entryset[Sets_entrysetCOUNT].getXMLAtts();
			if(this.Sets_entryset[Sets_entrysetCOUNT].xsiType!="cat:catalog"){
				xmlTxt+=" xsi:type=\"" + this.Sets_entryset[Sets_entrysetCOUNT].xsiType + "\"";
			}
			if (this.Sets_entryset[Sets_entrysetCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Sets_entryset[Sets_entrysetCOUNT].getXMLBody(preventComments);
					xmlTxt+="</cat:entrySet>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</cat:sets>";
			}
			}

			var child3=0;
			var att3=0;
			child3+=this.Entries_entry.length;
			if(child3>0 || att3>0){
				xmlTxt+="\n<cat:entries";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Entries_entryCOUNT=0;Entries_entryCOUNT<this.Entries_entry.length;Entries_entryCOUNT++){
			xmlTxt +="\n<cat:entry";
			xmlTxt +=this.Entries_entry[Entries_entryCOUNT].getXMLAtts();
			if(this.Entries_entry[Entries_entryCOUNT].xsiType!="cat:entry"){
				xmlTxt+=" xsi:type=\"" + this.Entries_entry[Entries_entryCOUNT].xsiType + "\"";
			}
			if (this.Entries_entry[Entries_entryCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Entries_entry[Entries_entryCOUNT].getXMLBody(preventComments);
					xmlTxt+="</cat:entry>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</cat:entries>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.CatCatalogId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.Metafields_metafield.length>0)return true;
			if(this.Tags_tag.length>0)return true;
			if(this.Sets_entryset.length>0)return true;
			if(this.Entries_entry.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
