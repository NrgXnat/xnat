/*
 * web: xdat_stored_search.js
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

function xdat_stored_search(){
this.xsiType="xdat:stored_search";

	this.getSchemaElementName=function(){
		return "stored_search";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:stored_search";
	}

	this.RootElementName=null;


	function getRootElementName() {
		return this.RootElementName;
	}
	this.getRootElementName=getRootElementName;


	function setRootElementName(v){
		this.RootElementName=v;
	}
	this.setRootElementName=setRootElementName;
	this.SearchField =new Array();

	function getSearchField() {
		return this.SearchField;
	}
	this.getSearchField=getSearchField;


	function addSearchField(v){
		this.SearchField.push(v);
	}
	this.addSearchField=addSearchField;
	this.SearchWhere =new Array();

	function getSearchWhere() {
		return this.SearchWhere;
	}
	this.getSearchWhere=getSearchWhere;


	function addSearchWhere(v){
		this.SearchWhere.push(v);
	}
	this.addSearchWhere=addSearchWhere;

	this.SortBy_elementName=null;


	function getSortBy_elementName() {
		return this.SortBy_elementName;
	}
	this.getSortBy_elementName=getSortBy_elementName;


	function setSortBy_elementName(v){
		this.SortBy_elementName=v;
	}
	this.setSortBy_elementName=setSortBy_elementName;

	this.SortBy_fieldId=null;


	function getSortBy_fieldId() {
		return this.SortBy_fieldId;
	}
	this.getSortBy_fieldId=getSortBy_fieldId;


	function setSortBy_fieldId(v){
		this.SortBy_fieldId=v;
	}
	this.setSortBy_fieldId=setSortBy_fieldId;
	this.AllowedUser =new Array();

	function getAllowedUser() {
		return this.AllowedUser;
	}
	this.getAllowedUser=getAllowedUser;


	function addAllowedUser(v){
		this.AllowedUser.push(v);
	}
	this.addAllowedUser=addAllowedUser;
	this.AllowedGroups_groupid =new Array();

	function getAllowedGroups_groupid() {
		return this.AllowedGroups_groupid;
	}
	this.getAllowedGroups_groupid=getAllowedGroups_groupid;


	function addAllowedGroups_groupid(v){
		this.AllowedGroups_groupid.push(v);
	}
	this.addAllowedGroups_groupid=addAllowedGroups_groupid;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.Description=null;


	function getDescription() {
		return this.Description;
	}
	this.getDescription=getDescription;


	function setDescription(v){
		this.Description=v;
	}
	this.setDescription=setDescription;

	this.Layeredsequence=null;


	function getLayeredsequence() {
		return this.Layeredsequence;
	}
	this.getLayeredsequence=getLayeredsequence;


	function setLayeredsequence(v){
		this.Layeredsequence=v;
	}
	this.setLayeredsequence=setLayeredsequence;

	this.AllowDiffColumns=null;


	function getAllowDiffColumns() {
		return this.AllowDiffColumns;
	}
	this.getAllowDiffColumns=getAllowDiffColumns;


	function setAllowDiffColumns(v){
		this.AllowDiffColumns=v;
	}
	this.setAllowDiffColumns=setAllowDiffColumns;


	this.isAllowDiffColumns=function(defaultValue) {
		if(this.AllowDiffColumns==null)return defaultValue;
		if(this.AllowDiffColumns=="1" || this.AllowDiffColumns==true)return true;
		return false;
	}

	this.Secure=null;


	function getSecure() {
		return this.Secure;
	}
	this.getSecure=getSecure;


	function setSecure(v){
		this.Secure=v;
	}
	this.setSecure=setSecure;


	this.isSecure=function(defaultValue) {
		if(this.Secure==null)return defaultValue;
		if(this.Secure=="1" || this.Secure==true)return true;
		return false;
	}

	this.BriefDescription=null;


	function getBriefDescription() {
		return this.BriefDescription;
	}
	this.getBriefDescription=getBriefDescription;


	function setBriefDescription(v){
		this.BriefDescription=v;
	}
	this.setBriefDescription=setBriefDescription;

	this.Tag=null;


	function getTag() {
		return this.Tag;
	}
	this.getTag=getTag;


	function setTag(v){
		this.Tag=v;
	}
	this.setTag=setTag;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="root_element_name"){
				return this.RootElementName ;
			} else 
			if(xmlPath=="search_field"){
				return this.SearchField ;
			} else 
			if(xmlPath.startsWith("search_field")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.SearchField ;
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

				for(var whereCount=0;whereCount<this.SearchField.length;whereCount++){

					var tempValue=this.SearchField[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.SearchField[whereCount]);

					}

				}
				}else{

				whereArray=this.SearchField;
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
			if(xmlPath=="search_where"){
				return this.SearchWhere ;
			} else 
			if(xmlPath.startsWith("search_where")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.SearchWhere ;
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

				for(var whereCount=0;whereCount<this.SearchWhere.length;whereCount++){

					var tempValue=this.SearchWhere[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.SearchWhere[whereCount]);

					}

				}
				}else{

				whereArray=this.SearchWhere;
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
			if(xmlPath=="sort_by/element_name"){
				return this.SortBy_elementName ;
			} else 
			if(xmlPath=="sort_by/field_ID"){
				return this.SortBy_fieldId ;
			} else 
			if(xmlPath=="allowed_user"){
				return this.AllowedUser ;
			} else 
			if(xmlPath.startsWith("allowed_user")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.AllowedUser ;
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

				for(var whereCount=0;whereCount<this.AllowedUser.length;whereCount++){

					var tempValue=this.AllowedUser[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.AllowedUser[whereCount]);

					}

				}
				}else{

				whereArray=this.AllowedUser;
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
			if(xmlPath=="allowed_groups/groupID"){
				return this.AllowedGroups_groupid ;
			} else 
			if(xmlPath.startsWith("allowed_groups/groupID")){
				xmlPath=xmlPath.substring(22);
				if(xmlPath=="")return this.AllowedGroups_groupid ;
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

				for(var whereCount=0;whereCount<this.AllowedGroups_groupid.length;whereCount++){

					var tempValue=this.AllowedGroups_groupid[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.AllowedGroups_groupid[whereCount]);

					}

				}
				}else{

				whereArray=this.AllowedGroups_groupid;
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
			if(xmlPath=="description"){
				return this.Description ;
			} else 
			if(xmlPath=="layeredSequence"){
				return this.Layeredsequence ;
			} else 
			if(xmlPath=="allow-diff-columns"){
				return this.AllowDiffColumns ;
			} else 
			if(xmlPath=="secure"){
				return this.Secure ;
			} else 
			if(xmlPath=="brief-description"){
				return this.BriefDescription ;
			} else 
			if(xmlPath=="tag"){
				return this.Tag ;
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
			if(xmlPath=="root_element_name"){
				this.RootElementName=value;
			} else 
			if(xmlPath=="search_field"){
				this.SearchField=value;
			} else 
			if(xmlPath.startsWith("search_field")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.SearchField ;
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

				for(var whereCount=0;whereCount<this.SearchField.length;whereCount++){

					var tempValue=this.SearchField[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.SearchField[whereCount]);

					}

				}
				}else{

				whereArray=this.SearchField;
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
						newChild= instanciateObject("xdat:search_field");//omUtils.js
					}
					this.addSearchField(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="search_where"){
				this.SearchWhere=value;
			} else 
			if(xmlPath.startsWith("search_where")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.SearchWhere ;
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

				for(var whereCount=0;whereCount<this.SearchWhere.length;whereCount++){

					var tempValue=this.SearchWhere[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.SearchWhere[whereCount]);

					}

				}
				}else{

				whereArray=this.SearchWhere;
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
						newChild= instanciateObject("xdat:criteria_set");//omUtils.js
					}
					this.addSearchWhere(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="sort_by/element_name"){
				this.SortBy_elementName=value;
			} else 
			if(xmlPath=="sort_by/field_ID"){
				this.SortBy_fieldId=value;
			} else 
			if(xmlPath=="allowed_user"){
				this.AllowedUser=value;
			} else 
			if(xmlPath.startsWith("allowed_user")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.AllowedUser ;
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

				for(var whereCount=0;whereCount<this.AllowedUser.length;whereCount++){

					var tempValue=this.AllowedUser[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.AllowedUser[whereCount]);

					}

				}
				}else{

				whereArray=this.AllowedUser;
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
						newChild= instanciateObject("xdat:stored_search_allowed_user");//omUtils.js
					}
					this.addAllowedUser(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="allowed_groups/groupID"){
				this.AllowedGroups_groupid=value;
			} else 
			if(xmlPath.startsWith("allowed_groups/groupID")){
				xmlPath=xmlPath.substring(22);
				if(xmlPath=="")return this.AllowedGroups_groupid ;
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

				for(var whereCount=0;whereCount<this.AllowedGroups_groupid.length;whereCount++){

					var tempValue=this.AllowedGroups_groupid[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.AllowedGroups_groupid[whereCount]);

					}

				}
				}else{

				whereArray=this.AllowedGroups_groupid;
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
						newChild= instanciateObject("xdat:stored_search_groupID");//omUtils.js
					}
					this.addAllowedGroups_groupid(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="description"){
				this.Description=value;
			} else 
			if(xmlPath=="layeredSequence"){
				this.Layeredsequence=value;
			} else 
			if(xmlPath=="allow-diff-columns"){
				this.AllowDiffColumns=value;
			} else 
			if(xmlPath=="secure"){
				this.Secure=value;
			} else 
			if(xmlPath=="brief-description"){
				this.BriefDescription=value;
			} else 
			if(xmlPath=="tag"){
				this.Tag=value;
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
		if (xmlPath=="search_field"){
			this.addSearchField(v);
		}else if (xmlPath=="search_where"){
			this.addSearchWhere(v);
		}else if (xmlPath=="allowed_user"){
			this.addAllowedUser(v);
		}else if (xmlPath=="allowed_groups/groupID"){
			this.addAllowedGroups_groupid(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="search_field"){
			return "http://nrg.wustl.edu/security:search_field";
		}else if (xmlPath=="search_where"){
			return "http://nrg.wustl.edu/security:criteria_set";
		}else if (xmlPath=="allowed_user"){
			return "http://nrg.wustl.edu/security:stored_search_allowed_user";
		}else if (xmlPath=="allowed_groups/groupID"){
			return "http://nrg.wustl.edu/security:stored_search_groupID";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="root_element_name"){
			return "field_data";
		}else if (xmlPath=="search_field"){
			return "field_multi_reference";
		}else if (xmlPath=="search_where"){
			return "field_multi_reference";
		}else if (xmlPath=="sort_by/element_name"){
			return "field_data";
		}else if (xmlPath=="sort_by/field_ID"){
			return "field_data";
		}else if (xmlPath=="allowed_user"){
			return "field_multi_reference";
		}else if (xmlPath=="allowed_groups/groupID"){
			return "field_inline_repeater";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="description"){
			return "field_data";
		}else if (xmlPath=="layeredSequence"){
			return "field_data";
		}else if (xmlPath=="allow-diff-columns"){
			return "field_data";
		}else if (xmlPath=="secure"){
			return "field_data";
		}else if (xmlPath=="brief-description"){
			return "field_data";
		}else if (xmlPath=="tag"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:bundle";
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
		xmlTxt+="\n</xdat:bundle>";
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

		if (this.Description!=null)
			attTxt+=" description=\"" +this.Description +"\"";
		//NOT REQUIRED FIELD

		if (this.Layeredsequence!=null)
			attTxt+=" layeredSequence=\"" +this.Layeredsequence +"\"";
		//NOT REQUIRED FIELD

		if (this.AllowDiffColumns!=null)
			attTxt+=" allow-diff-columns=\"" +this.AllowDiffColumns +"\"";
		//NOT REQUIRED FIELD

		if (this.Secure!=null)
			attTxt+=" secure=\"" +this.Secure +"\"";
		//NOT REQUIRED FIELD

		if (this.BriefDescription!=null)
			attTxt+=" brief-description=\"" +this.BriefDescription +"\"";
		//NOT REQUIRED FIELD

		if (this.Tag!=null)
			attTxt+=" tag=\"" +this.Tag +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.RootElementName!=null){
			xmlTxt+="\n<xdat:root_element_name";
			xmlTxt+=">";
			xmlTxt+=this.RootElementName.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:root_element_name>";
		}
		for(var SearchFieldCOUNT=0;SearchFieldCOUNT<this.SearchField.length;SearchFieldCOUNT++){
			xmlTxt +="\n<xdat:search_field";
			xmlTxt +=this.SearchField[SearchFieldCOUNT].getXMLAtts();
			if(this.SearchField[SearchFieldCOUNT].xsiType!="xdat:search_field"){
				xmlTxt+=" xsi:type=\"" + this.SearchField[SearchFieldCOUNT].xsiType + "\"";
			}
			if (this.SearchField[SearchFieldCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.SearchField[SearchFieldCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:search_field>";
			}else {xmlTxt+="/>";}
		}
		for(var SearchWhereCOUNT=0;SearchWhereCOUNT<this.SearchWhere.length;SearchWhereCOUNT++){
			xmlTxt +="\n<xdat:search_where";
			xmlTxt +=this.SearchWhere[SearchWhereCOUNT].getXMLAtts();
			if(this.SearchWhere[SearchWhereCOUNT].xsiType!="xdat:criteria_set"){
				xmlTxt+=" xsi:type=\"" + this.SearchWhere[SearchWhereCOUNT].xsiType + "\"";
			}
			if (this.SearchWhere[SearchWhereCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.SearchWhere[SearchWhereCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:search_where>";
			}else {xmlTxt+="/>";}
		}
			var child0=0;
			var att0=0;
			if(this.SortBy_elementName!=null)
			child0++;
			if(this.SortBy_fieldId!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xdat:sort_by";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.SortBy_elementName!=null){
			xmlTxt+="\n<xdat:element_name";
			xmlTxt+=">";
			xmlTxt+=this.SortBy_elementName.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:element_name>";
		}
		if (this.SortBy_fieldId!=null){
			xmlTxt+="\n<xdat:field_ID";
			xmlTxt+=">";
			xmlTxt+=this.SortBy_fieldId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:field_ID>";
		}
				xmlTxt+="\n</xdat:sort_by>";
			}
			}

		for(var AllowedUserCOUNT=0;AllowedUserCOUNT<this.AllowedUser.length;AllowedUserCOUNT++){
			xmlTxt +="\n<xdat:allowed_user";
			xmlTxt +=this.AllowedUser[AllowedUserCOUNT].getXMLAtts();
			if(this.AllowedUser[AllowedUserCOUNT].xsiType!="xdat:stored_search_allowed_user"){
				xmlTxt+=" xsi:type=\"" + this.AllowedUser[AllowedUserCOUNT].xsiType + "\"";
			}
			if (this.AllowedUser[AllowedUserCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.AllowedUser[AllowedUserCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:allowed_user>";
			}else {xmlTxt+="/>";}
		}
			var child1=0;
			var att1=0;
			child1+=this.AllowedGroups_groupid.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xdat:allowed_groups";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var AllowedGroups_groupidCOUNT=0;AllowedGroups_groupidCOUNT<this.AllowedGroups_groupid.length;AllowedGroups_groupidCOUNT++){
			xmlTxt+=this.AllowedGroups_groupid[AllowedGroups_groupidCOUNT].getXMLBody(preventComments);
		}
				xmlTxt+="\n</xdat:allowed_groups>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.RootElementName!=null) return true;
		if(this.SearchField.length>0) return true;
		if(this.SearchWhere.length>0) return true;
			if(this.SortBy_elementName!=null) return true;
			if(this.SortBy_fieldId!=null) return true;
		if(this.AllowedUser.length>0) return true;
			if(this.AllowedGroups_groupid.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
