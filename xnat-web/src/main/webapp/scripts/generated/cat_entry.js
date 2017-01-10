/*
 * web: cat_entry.js
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

function cat_entry(){
this.xsiType="cat:entry";

	this.getSchemaElementName=function(){
		return "entry";
	}

	this.getFullSchemaElementName=function(){
		return "cat:entry";
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

	this.Uri=null;


	function getUri() {
		return this.Uri;
	}
	this.getUri=getUri;


	function setUri(v){
		this.Uri=v;
	}
	this.setUri=setUri;

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

	this.Format=null;


	function getFormat() {
		return this.Format;
	}
	this.getFormat=getFormat;


	function setFormat(v){
		this.Format=v;
	}
	this.setFormat=setFormat;

	this.Content=null;


	function getContent() {
		return this.Content;
	}
	this.getContent=getContent;


	function setContent(v){
		this.Content=v;
	}
	this.setContent=setContent;

	this.Cachepath=null;


	function getCachepath() {
		return this.Cachepath;
	}
	this.getCachepath=getCachepath;


	function setCachepath(v){
		this.Cachepath=v;
	}
	this.setCachepath=setCachepath;

	this.Createdtime=null;


	function getCreatedtime() {
		return this.Createdtime;
	}
	this.getCreatedtime=getCreatedtime;


	function setCreatedtime(v){
		this.Createdtime=v;
	}
	this.setCreatedtime=setCreatedtime;

	this.Createdby=null;


	function getCreatedby() {
		return this.Createdby;
	}
	this.getCreatedby=getCreatedby;


	function setCreatedby(v){
		this.Createdby=v;
	}
	this.setCreatedby=setCreatedby;

	this.Createdeventid=null;


	function getCreatedeventid() {
		return this.Createdeventid;
	}
	this.getCreatedeventid=getCreatedeventid;


	function setCreatedeventid(v){
		this.Createdeventid=v;
	}
	this.setCreatedeventid=setCreatedeventid;

	this.Modifiedtime=null;


	function getModifiedtime() {
		return this.Modifiedtime;
	}
	this.getModifiedtime=getModifiedtime;


	function setModifiedtime(v){
		this.Modifiedtime=v;
	}
	this.setModifiedtime=setModifiedtime;

	this.Modifiedby=null;


	function getModifiedby() {
		return this.Modifiedby;
	}
	this.getModifiedby=getModifiedby;


	function setModifiedby(v){
		this.Modifiedby=v;
	}
	this.setModifiedby=setModifiedby;

	this.Modifiedeventid=null;


	function getModifiedeventid() {
		return this.Modifiedeventid;
	}
	this.getModifiedeventid=getModifiedeventid;


	function setModifiedeventid(v){
		this.Modifiedeventid=v;
	}
	this.setModifiedeventid=setModifiedeventid;

	this.Digest=null;


	function getDigest() {
		return this.Digest;
	}
	this.getDigest=getDigest;


	function setDigest(v){
		this.Digest=v;
	}
	this.setDigest=setDigest;

	this.CatEntryId=null;


	function getCatEntryId() {
		return this.CatEntryId;
	}
	this.getCatEntryId=getCatEntryId;


	function setCatEntryId(v){
		this.CatEntryId=v;
	}
	this.setCatEntryId=setCatEntryId;

	this.entries_entry_cat_catalog_cat_catalog_id_fk=null;


	this.getentries_entry_cat_catalog_cat_catalog_id=function() {
		return this.entries_entry_cat_catalog_cat_catalog_id_fk;
	}


	this.setentries_entry_cat_catalog_cat_catalog_id=function(v){
		this.entries_entry_cat_catalog_cat_catalog_id_fk=v;
	}


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
			if(xmlPath=="URI"){
				return this.Uri ;
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
			if(xmlPath=="format"){
				return this.Format ;
			} else 
			if(xmlPath=="content"){
				return this.Content ;
			} else 
			if(xmlPath=="cachePath"){
				return this.Cachepath ;
			} else 
			if(xmlPath=="createdTime"){
				return this.Createdtime ;
			} else 
			if(xmlPath=="createdBy"){
				return this.Createdby ;
			} else 
			if(xmlPath=="createdEventId"){
				return this.Createdeventid ;
			} else 
			if(xmlPath=="modifiedTime"){
				return this.Modifiedtime ;
			} else 
			if(xmlPath=="modifiedBy"){
				return this.Modifiedby ;
			} else 
			if(xmlPath=="modifiedEventId"){
				return this.Modifiedeventid ;
			} else 
			if(xmlPath=="digest"){
				return this.Digest ;
			} else 
			if(xmlPath=="extension_item"){
				return this.ExtensionItem ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="cat_entry_id"){
				return this.CatEntryId ;
			} else 
			if(xmlPath=="entries_entry_cat_catalog_cat_catalog_id"){
				return this.entries_entry_cat_catalog_cat_catalog_id_fk ;
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
						newChild= instanciateObject("cat:entry_metaField");//omUtils.js
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
						newChild= instanciateObject("cat:entry_tag");//omUtils.js
					}
					this.addTags_tag(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="URI"){
				this.Uri=value;
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
			if(xmlPath=="format"){
				this.Format=value;
			} else 
			if(xmlPath=="content"){
				this.Content=value;
			} else 
			if(xmlPath=="cachePath"){
				this.Cachepath=value;
			} else 
			if(xmlPath=="createdTime"){
				this.Createdtime=value;
			} else 
			if(xmlPath=="createdBy"){
				this.Createdby=value;
			} else 
			if(xmlPath=="createdEventId"){
				this.Createdeventid=value;
			} else 
			if(xmlPath=="modifiedTime"){
				this.Modifiedtime=value;
			} else 
			if(xmlPath=="modifiedBy"){
				this.Modifiedby=value;
			} else 
			if(xmlPath=="modifiedEventId"){
				this.Modifiedeventid=value;
			} else 
			if(xmlPath=="digest"){
				this.Digest=value;
			} else 
			if(xmlPath=="extension_item"){
				this.ExtensionItem=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="cat_entry_id"){
				this.CatEntryId=value;
			} else 
			if(xmlPath=="entries_entry_cat_catalog_cat_catalog_id"){
				this.entries_entry_cat_catalog_cat_catalog_id_fk=value;
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
			return "http://nrg.wustl.edu/catalog:entry_metaField";
		}else if (xmlPath=="tags/tag"){
			return "http://nrg.wustl.edu/catalog:entry_tag";
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
		}else if (xmlPath=="URI"){
			return "field_data";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="name"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_data";
		}else if (xmlPath=="format"){
			return "field_data";
		}else if (xmlPath=="content"){
			return "field_data";
		}else if (xmlPath=="cachePath"){
			return "field_data";
		}else if (xmlPath=="createdTime"){
			return "field_data";
		}else if (xmlPath=="createdBy"){
			return "field_data";
		}else if (xmlPath=="createdEventId"){
			return "field_data";
		}else if (xmlPath=="modifiedTime"){
			return "field_data";
		}else if (xmlPath=="modifiedBy"){
			return "field_data";
		}else if (xmlPath=="modifiedEventId"){
			return "field_data";
		}else if (xmlPath=="digest"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<cat:Entry";
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
		xmlTxt+="\n</cat:Entry>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.CatEntryId!=null){
				if(hiddenCount++>0)str+=",";
				str+="cat_entry_id=\"" + this.CatEntryId + "\"";
			}
			if(this.entries_entry_cat_catalog_cat_catalog_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="entries_entry_cat_catalog_cat_catalog_id=\"" + this.entries_entry_cat_catalog_cat_catalog_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Uri!=null)
			attTxt+=" URI=\"" +this.Uri +"\"";
		else attTxt+=" URI=\"\"";//REQUIRED FIELD

		if (this.Id!=null)
			attTxt+=" ID=\"" +this.Id +"\"";
		//NOT REQUIRED FIELD

		if (this.Name!=null)
			attTxt+=" name=\"" +this.Name +"\"";
		//NOT REQUIRED FIELD

		if (this.Description!=null)
			attTxt+=" description=\"" +this.Description +"\"";
		//NOT REQUIRED FIELD

		if (this.Format!=null)
			attTxt+=" format=\"" +this.Format +"\"";
		//NOT REQUIRED FIELD

		if (this.Content!=null)
			attTxt+=" content=\"" +this.Content +"\"";
		//NOT REQUIRED FIELD

		if (this.Cachepath!=null)
			attTxt+=" cachePath=\"" +this.Cachepath +"\"";
		//NOT REQUIRED FIELD

		if (this.Createdtime!=null)
			attTxt+=" createdTime=\"" +this.Createdtime +"\"";
		//NOT REQUIRED FIELD

		if (this.Createdby!=null)
			attTxt+=" createdBy=\"" +this.Createdby +"\"";
		//NOT REQUIRED FIELD

		if (this.Createdeventid!=null)
			attTxt+=" createdEventId=\"" +this.Createdeventid +"\"";
		//NOT REQUIRED FIELD

		if (this.Modifiedtime!=null)
			attTxt+=" modifiedTime=\"" +this.Modifiedtime +"\"";
		//NOT REQUIRED FIELD

		if (this.Modifiedby!=null)
			attTxt+=" modifiedBy=\"" +this.Modifiedby +"\"";
		//NOT REQUIRED FIELD

		if (this.Modifiedeventid!=null)
			attTxt+=" modifiedEventId=\"" +this.Modifiedeventid +"\"";
		//NOT REQUIRED FIELD

		if (this.Digest!=null)
			attTxt+=" digest=\"" +this.Digest +"\"";
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
			if(this.Metafields_metafield[Metafields_metafieldCOUNT].xsiType!="cat:entry_metaField"){
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

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.CatEntryId!=null) return true;
			if (this.entries_entry_cat_catalog_cat_catalog_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.Metafields_metafield.length>0)return true;
			if(this.Tags_tag.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
