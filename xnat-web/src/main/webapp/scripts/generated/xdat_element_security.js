/*
 * web: xdat_element_security.js
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

function xdat_element_security(){
this.xsiType="xdat:element_security";

	this.getSchemaElementName=function(){
		return "element_security";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:element_security";
	}
	this.PrimarySecurityFields_primarySecurityField =new Array();

	function getPrimarySecurityFields_primarySecurityField() {
		return this.PrimarySecurityFields_primarySecurityField;
	}
	this.getPrimarySecurityFields_primarySecurityField=getPrimarySecurityFields_primarySecurityField;


	function addPrimarySecurityFields_primarySecurityField(v){
		this.PrimarySecurityFields_primarySecurityField.push(v);
	}
	this.addPrimarySecurityFields_primarySecurityField=addPrimarySecurityFields_primarySecurityField;
	this.ElementActions_elementAction =new Array();

	function getElementActions_elementAction() {
		return this.ElementActions_elementAction;
	}
	this.getElementActions_elementAction=getElementActions_elementAction;


	function addElementActions_elementAction(v){
		this.ElementActions_elementAction.push(v);
	}
	this.addElementActions_elementAction=addElementActions_elementAction;
	this.ListingActions_listingAction =new Array();

	function getListingActions_listingAction() {
		return this.ListingActions_listingAction;
	}
	this.getListingActions_listingAction=getListingActions_listingAction;


	function addListingActions_listingAction(v){
		this.ListingActions_listingAction.push(v);
	}
	this.addListingActions_listingAction=addListingActions_listingAction;

	this.ElementName=null;


	function getElementName() {
		return this.ElementName;
	}
	this.getElementName=getElementName;


	function setElementName(v){
		this.ElementName=v;
	}
	this.setElementName=setElementName;

	this.SecondaryPassword=null;


	function getSecondaryPassword() {
		return this.SecondaryPassword;
	}
	this.getSecondaryPassword=getSecondaryPassword;


	function setSecondaryPassword(v){
		this.SecondaryPassword=v;
	}
	this.setSecondaryPassword=setSecondaryPassword;


	this.isSecondaryPassword=function(defaultValue) {
		if(this.SecondaryPassword==null)return defaultValue;
		if(this.SecondaryPassword=="1" || this.SecondaryPassword==true)return true;
		return false;
	}

	this.SecureIp=null;


	function getSecureIp() {
		return this.SecureIp;
	}
	this.getSecureIp=getSecureIp;


	function setSecureIp(v){
		this.SecureIp=v;
	}
	this.setSecureIp=setSecureIp;


	this.isSecureIp=function(defaultValue) {
		if(this.SecureIp==null)return defaultValue;
		if(this.SecureIp=="1" || this.SecureIp==true)return true;
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

	this.Browse=null;


	function getBrowse() {
		return this.Browse;
	}
	this.getBrowse=getBrowse;


	function setBrowse(v){
		this.Browse=v;
	}
	this.setBrowse=setBrowse;


	this.isBrowse=function(defaultValue) {
		if(this.Browse==null)return defaultValue;
		if(this.Browse=="1" || this.Browse==true)return true;
		return false;
	}

	this.Sequence=null;


	function getSequence() {
		return this.Sequence;
	}
	this.getSequence=getSequence;


	function setSequence(v){
		this.Sequence=v;
	}
	this.setSequence=setSequence;

	this.Quarantine=null;


	function getQuarantine() {
		return this.Quarantine;
	}
	this.getQuarantine=getQuarantine;


	function setQuarantine(v){
		this.Quarantine=v;
	}
	this.setQuarantine=setQuarantine;


	this.isQuarantine=function(defaultValue) {
		if(this.Quarantine==null)return defaultValue;
		if(this.Quarantine=="1" || this.Quarantine==true)return true;
		return false;
	}

	this.PreLoad=null;


	function getPreLoad() {
		return this.PreLoad;
	}
	this.getPreLoad=getPreLoad;


	function setPreLoad(v){
		this.PreLoad=v;
	}
	this.setPreLoad=setPreLoad;


	this.isPreLoad=function(defaultValue) {
		if(this.PreLoad==null)return defaultValue;
		if(this.PreLoad=="1" || this.PreLoad==true)return true;
		return false;
	}

	this.Searchable=null;


	function getSearchable() {
		return this.Searchable;
	}
	this.getSearchable=getSearchable;


	function setSearchable(v){
		this.Searchable=v;
	}
	this.setSearchable=setSearchable;


	this.isSearchable=function(defaultValue) {
		if(this.Searchable==null)return defaultValue;
		if(this.Searchable=="1" || this.Searchable==true)return true;
		return false;
	}

	this.SecureRead=null;


	function getSecureRead() {
		return this.SecureRead;
	}
	this.getSecureRead=getSecureRead;


	function setSecureRead(v){
		this.SecureRead=v;
	}
	this.setSecureRead=setSecureRead;


	this.isSecureRead=function(defaultValue) {
		if(this.SecureRead==null)return defaultValue;
		if(this.SecureRead=="1" || this.SecureRead==true)return true;
		return false;
	}

	this.SecureEdit=null;


	function getSecureEdit() {
		return this.SecureEdit;
	}
	this.getSecureEdit=getSecureEdit;


	function setSecureEdit(v){
		this.SecureEdit=v;
	}
	this.setSecureEdit=setSecureEdit;


	this.isSecureEdit=function(defaultValue) {
		if(this.SecureEdit==null)return defaultValue;
		if(this.SecureEdit=="1" || this.SecureEdit==true)return true;
		return false;
	}

	this.SecureCreate=null;


	function getSecureCreate() {
		return this.SecureCreate;
	}
	this.getSecureCreate=getSecureCreate;


	function setSecureCreate(v){
		this.SecureCreate=v;
	}
	this.setSecureCreate=setSecureCreate;


	this.isSecureCreate=function(defaultValue) {
		if(this.SecureCreate==null)return defaultValue;
		if(this.SecureCreate=="1" || this.SecureCreate==true)return true;
		return false;
	}

	this.SecureDelete=null;


	function getSecureDelete() {
		return this.SecureDelete;
	}
	this.getSecureDelete=getSecureDelete;


	function setSecureDelete(v){
		this.SecureDelete=v;
	}
	this.setSecureDelete=setSecureDelete;


	this.isSecureDelete=function(defaultValue) {
		if(this.SecureDelete==null)return defaultValue;
		if(this.SecureDelete=="1" || this.SecureDelete==true)return true;
		return false;
	}

	this.Accessible=null;


	function getAccessible() {
		return this.Accessible;
	}
	this.getAccessible=getAccessible;


	function setAccessible(v){
		this.Accessible=v;
	}
	this.setAccessible=setAccessible;


	this.isAccessible=function(defaultValue) {
		if(this.Accessible==null)return defaultValue;
		if(this.Accessible=="1" || this.Accessible==true)return true;
		return false;
	}

	this.Usage=null;


	function getUsage() {
		return this.Usage;
	}
	this.getUsage=getUsage;


	function setUsage(v){
		this.Usage=v;
	}
	this.setUsage=setUsage;

	this.Singular=null;


	function getSingular() {
		return this.Singular;
	}
	this.getSingular=getSingular;


	function setSingular(v){
		this.Singular=v;
	}
	this.setSingular=setSingular;

	this.Plural=null;


	function getPlural() {
		return this.Plural;
	}
	this.getPlural=getPlural;


	function setPlural(v){
		this.Plural=v;
	}
	this.setPlural=setPlural;

	this.Category=null;


	function getCategory() {
		return this.Category;
	}
	this.getCategory=getCategory;


	function setCategory(v){
		this.Category=v;
	}
	this.setCategory=setCategory;

	this.Code=null;


	function getCode() {
		return this.Code;
	}
	this.getCode=getCode;


	function setCode(v){
		this.Code=v;
	}
	this.setCode=setCode;

	this.element_security_set_element_se_xdat_security_id_fk=null;


	this.getelement_security_set_element_se_xdat_security_id=function() {
		return this.element_security_set_element_se_xdat_security_id_fk;
	}


	this.setelement_security_set_element_se_xdat_security_id=function(v){
		this.element_security_set_element_se_xdat_security_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="primary_security_fields/primary_security_field"){
				return this.PrimarySecurityFields_primarySecurityField ;
			} else 
			if(xmlPath.startsWith("primary_security_fields/primary_security_field")){
				xmlPath=xmlPath.substring(46);
				if(xmlPath=="")return this.PrimarySecurityFields_primarySecurityField ;
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

				for(var whereCount=0;whereCount<this.PrimarySecurityFields_primarySecurityField.length;whereCount++){

					var tempValue=this.PrimarySecurityFields_primarySecurityField[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.PrimarySecurityFields_primarySecurityField[whereCount]);

					}

				}
				}else{

				whereArray=this.PrimarySecurityFields_primarySecurityField;
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
			if(xmlPath=="element_actions/element_action"){
				return this.ElementActions_elementAction ;
			} else 
			if(xmlPath.startsWith("element_actions/element_action")){
				xmlPath=xmlPath.substring(30);
				if(xmlPath=="")return this.ElementActions_elementAction ;
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

				for(var whereCount=0;whereCount<this.ElementActions_elementAction.length;whereCount++){

					var tempValue=this.ElementActions_elementAction[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ElementActions_elementAction[whereCount]);

					}

				}
				}else{

				whereArray=this.ElementActions_elementAction;
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
			if(xmlPath=="listing_actions/listing_action"){
				return this.ListingActions_listingAction ;
			} else 
			if(xmlPath.startsWith("listing_actions/listing_action")){
				xmlPath=xmlPath.substring(30);
				if(xmlPath=="")return this.ListingActions_listingAction ;
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

				for(var whereCount=0;whereCount<this.ListingActions_listingAction.length;whereCount++){

					var tempValue=this.ListingActions_listingAction[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ListingActions_listingAction[whereCount]);

					}

				}
				}else{

				whereArray=this.ListingActions_listingAction;
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
			if(xmlPath=="element_name"){
				return this.ElementName ;
			} else 
			if(xmlPath=="secondary_password"){
				return this.SecondaryPassword ;
			} else 
			if(xmlPath=="secure_ip"){
				return this.SecureIp ;
			} else 
			if(xmlPath=="secure"){
				return this.Secure ;
			} else 
			if(xmlPath=="browse"){
				return this.Browse ;
			} else 
			if(xmlPath=="sequence"){
				return this.Sequence ;
			} else 
			if(xmlPath=="quarantine"){
				return this.Quarantine ;
			} else 
			if(xmlPath=="pre_load"){
				return this.PreLoad ;
			} else 
			if(xmlPath=="searchable"){
				return this.Searchable ;
			} else 
			if(xmlPath=="secure_read"){
				return this.SecureRead ;
			} else 
			if(xmlPath=="secure_edit"){
				return this.SecureEdit ;
			} else 
			if(xmlPath=="secure_create"){
				return this.SecureCreate ;
			} else 
			if(xmlPath=="secure_delete"){
				return this.SecureDelete ;
			} else 
			if(xmlPath=="accessible"){
				return this.Accessible ;
			} else 
			if(xmlPath=="usage"){
				return this.Usage ;
			} else 
			if(xmlPath=="singular"){
				return this.Singular ;
			} else 
			if(xmlPath=="plural"){
				return this.Plural ;
			} else 
			if(xmlPath=="category"){
				return this.Category ;
			} else 
			if(xmlPath=="code"){
				return this.Code ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="element_security_set_element_se_xdat_security_id"){
				return this.element_security_set_element_se_xdat_security_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="primary_security_fields/primary_security_field"){
				this.PrimarySecurityFields_primarySecurityField=value;
			} else 
			if(xmlPath.startsWith("primary_security_fields/primary_security_field")){
				xmlPath=xmlPath.substring(46);
				if(xmlPath=="")return this.PrimarySecurityFields_primarySecurityField ;
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

				for(var whereCount=0;whereCount<this.PrimarySecurityFields_primarySecurityField.length;whereCount++){

					var tempValue=this.PrimarySecurityFields_primarySecurityField[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.PrimarySecurityFields_primarySecurityField[whereCount]);

					}

				}
				}else{

				whereArray=this.PrimarySecurityFields_primarySecurityField;
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
						newChild= instanciateObject("xdat:primary_security_field");//omUtils.js
					}
					this.addPrimarySecurityFields_primarySecurityField(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="element_actions/element_action"){
				this.ElementActions_elementAction=value;
			} else 
			if(xmlPath.startsWith("element_actions/element_action")){
				xmlPath=xmlPath.substring(30);
				if(xmlPath=="")return this.ElementActions_elementAction ;
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

				for(var whereCount=0;whereCount<this.ElementActions_elementAction.length;whereCount++){

					var tempValue=this.ElementActions_elementAction[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ElementActions_elementAction[whereCount]);

					}

				}
				}else{

				whereArray=this.ElementActions_elementAction;
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
						newChild= instanciateObject("xdat:element_action_type");//omUtils.js
					}
					this.addElementActions_elementAction(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="listing_actions/listing_action"){
				this.ListingActions_listingAction=value;
			} else 
			if(xmlPath.startsWith("listing_actions/listing_action")){
				xmlPath=xmlPath.substring(30);
				if(xmlPath=="")return this.ListingActions_listingAction ;
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

				for(var whereCount=0;whereCount<this.ListingActions_listingAction.length;whereCount++){

					var tempValue=this.ListingActions_listingAction[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ListingActions_listingAction[whereCount]);

					}

				}
				}else{

				whereArray=this.ListingActions_listingAction;
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
						newChild= instanciateObject("xdat:element_security_listing_action");//omUtils.js
					}
					this.addListingActions_listingAction(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="element_name"){
				this.ElementName=value;
			} else 
			if(xmlPath=="secondary_password"){
				this.SecondaryPassword=value;
			} else 
			if(xmlPath=="secure_ip"){
				this.SecureIp=value;
			} else 
			if(xmlPath=="secure"){
				this.Secure=value;
			} else 
			if(xmlPath=="browse"){
				this.Browse=value;
			} else 
			if(xmlPath=="sequence"){
				this.Sequence=value;
			} else 
			if(xmlPath=="quarantine"){
				this.Quarantine=value;
			} else 
			if(xmlPath=="pre_load"){
				this.PreLoad=value;
			} else 
			if(xmlPath=="searchable"){
				this.Searchable=value;
			} else 
			if(xmlPath=="secure_read"){
				this.SecureRead=value;
			} else 
			if(xmlPath=="secure_edit"){
				this.SecureEdit=value;
			} else 
			if(xmlPath=="secure_create"){
				this.SecureCreate=value;
			} else 
			if(xmlPath=="secure_delete"){
				this.SecureDelete=value;
			} else 
			if(xmlPath=="accessible"){
				this.Accessible=value;
			} else 
			if(xmlPath=="usage"){
				this.Usage=value;
			} else 
			if(xmlPath=="singular"){
				this.Singular=value;
			} else 
			if(xmlPath=="plural"){
				this.Plural=value;
			} else 
			if(xmlPath=="category"){
				this.Category=value;
			} else 
			if(xmlPath=="code"){
				this.Code=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="element_security_set_element_se_xdat_security_id"){
				this.element_security_set_element_se_xdat_security_id_fk=value;
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
		if (xmlPath=="primary_security_fields/primary_security_field"){
			this.addPrimarySecurityFields_primarySecurityField(v);
		}else if (xmlPath=="element_actions/element_action"){
			this.addElementActions_elementAction(v);
		}else if (xmlPath=="listing_actions/listing_action"){
			this.addListingActions_listingAction(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="primary_security_fields/primary_security_field"){
			return "http://nrg.wustl.edu/security:primary_security_field";
		}else if (xmlPath=="element_actions/element_action"){
			return "http://nrg.wustl.edu/security:element_action_type";
		}else if (xmlPath=="listing_actions/listing_action"){
			return "http://nrg.wustl.edu/security:element_security_listing_action";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="primary_security_fields/primary_security_field"){
			return "field_NO_CHILD";
		}else if (xmlPath=="element_actions/element_action"){
			return "field_multi_reference";
		}else if (xmlPath=="listing_actions/listing_action"){
			return "field_multi_reference";
		}else if (xmlPath=="element_name"){
			return "field_data";
		}else if (xmlPath=="secondary_password"){
			return "field_data";
		}else if (xmlPath=="secure_ip"){
			return "field_data";
		}else if (xmlPath=="secure"){
			return "field_data";
		}else if (xmlPath=="browse"){
			return "field_data";
		}else if (xmlPath=="sequence"){
			return "field_data";
		}else if (xmlPath=="quarantine"){
			return "field_data";
		}else if (xmlPath=="pre_load"){
			return "field_data";
		}else if (xmlPath=="searchable"){
			return "field_data";
		}else if (xmlPath=="secure_read"){
			return "field_data";
		}else if (xmlPath=="secure_edit"){
			return "field_data";
		}else if (xmlPath=="secure_create"){
			return "field_data";
		}else if (xmlPath=="secure_delete"){
			return "field_data";
		}else if (xmlPath=="accessible"){
			return "field_data";
		}else if (xmlPath=="usage"){
			return "field_data";
		}else if (xmlPath=="singular"){
			return "field_data";
		}else if (xmlPath=="plural"){
			return "field_data";
		}else if (xmlPath=="category"){
			return "field_data";
		}else if (xmlPath=="code"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:element_security";
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
		xmlTxt+="\n</xdat:element_security>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.element_security_set_element_se_xdat_security_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="element_security_set_element_se_xdat_security_id=\"" + this.element_security_set_element_se_xdat_security_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.ElementName!=null)
			attTxt+=" element_name=\"" +this.ElementName +"\"";
		else attTxt+=" element_name=\"\"";//REQUIRED FIELD

		if (this.SecondaryPassword!=null)
			attTxt+=" secondary_password=\"" +this.SecondaryPassword +"\"";
		//NOT REQUIRED FIELD

		if (this.SecureIp!=null)
			attTxt+=" secure_ip=\"" +this.SecureIp +"\"";
		//NOT REQUIRED FIELD

		if (this.Secure!=null)
			attTxt+=" secure=\"" +this.Secure +"\"";
		//NOT REQUIRED FIELD

		if (this.Browse!=null)
			attTxt+=" browse=\"" +this.Browse +"\"";
		//NOT REQUIRED FIELD

		if (this.Sequence!=null)
			attTxt+=" sequence=\"" +this.Sequence +"\"";
		//NOT REQUIRED FIELD

		if (this.Quarantine!=null)
			attTxt+=" quarantine=\"" +this.Quarantine +"\"";
		//NOT REQUIRED FIELD

		if (this.PreLoad!=null)
			attTxt+=" pre_load=\"" +this.PreLoad +"\"";
		//NOT REQUIRED FIELD

		if (this.Searchable!=null)
			attTxt+=" searchable=\"" +this.Searchable +"\"";
		//NOT REQUIRED FIELD

		if (this.SecureRead!=null)
			attTxt+=" secure_read=\"" +this.SecureRead +"\"";
		//NOT REQUIRED FIELD

		if (this.SecureEdit!=null)
			attTxt+=" secure_edit=\"" +this.SecureEdit +"\"";
		//NOT REQUIRED FIELD

		if (this.SecureCreate!=null)
			attTxt+=" secure_create=\"" +this.SecureCreate +"\"";
		//NOT REQUIRED FIELD

		if (this.SecureDelete!=null)
			attTxt+=" secure_delete=\"" +this.SecureDelete +"\"";
		//NOT REQUIRED FIELD

		if (this.Accessible!=null)
			attTxt+=" accessible=\"" +this.Accessible +"\"";
		//NOT REQUIRED FIELD

		if (this.Usage!=null)
			attTxt+=" usage=\"" +this.Usage +"\"";
		//NOT REQUIRED FIELD

		if (this.Singular!=null)
			attTxt+=" singular=\"" +this.Singular +"\"";
		//NOT REQUIRED FIELD

		if (this.Plural!=null)
			attTxt+=" plural=\"" +this.Plural +"\"";
		//NOT REQUIRED FIELD

		if (this.Category!=null)
			attTxt+=" category=\"" +this.Category +"\"";
		//NOT REQUIRED FIELD

		if (this.Code!=null)
			attTxt+=" code=\"" +this.Code +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.PrimarySecurityFields_primarySecurityField.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xdat:primary_security_fields";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var PrimarySecurityFields_primarySecurityFieldCOUNT=0;PrimarySecurityFields_primarySecurityFieldCOUNT<this.PrimarySecurityFields_primarySecurityField.length;PrimarySecurityFields_primarySecurityFieldCOUNT++){
			xmlTxt +="\n<xdat:primary_security_field";
			xmlTxt +=this.PrimarySecurityFields_primarySecurityField[PrimarySecurityFields_primarySecurityFieldCOUNT].getXMLAtts();
			if(this.PrimarySecurityFields_primarySecurityField[PrimarySecurityFields_primarySecurityFieldCOUNT].xsiType!="xdat:primary_security_field"){
				xmlTxt+=" xsi:type=\"" + this.PrimarySecurityFields_primarySecurityField[PrimarySecurityFields_primarySecurityFieldCOUNT].xsiType + "\"";
			}
			if (this.PrimarySecurityFields_primarySecurityField[PrimarySecurityFields_primarySecurityFieldCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.PrimarySecurityFields_primarySecurityField[PrimarySecurityFields_primarySecurityFieldCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:primary_security_field>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:primary_security_fields>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.ElementActions_elementAction.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xdat:element_actions";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var ElementActions_elementActionCOUNT=0;ElementActions_elementActionCOUNT<this.ElementActions_elementAction.length;ElementActions_elementActionCOUNT++){
			xmlTxt +="\n<xdat:element_action";
			xmlTxt +=this.ElementActions_elementAction[ElementActions_elementActionCOUNT].getXMLAtts();
			if(this.ElementActions_elementAction[ElementActions_elementActionCOUNT].xsiType!="xdat:element_action_type"){
				xmlTxt+=" xsi:type=\"" + this.ElementActions_elementAction[ElementActions_elementActionCOUNT].xsiType + "\"";
			}
			if (this.ElementActions_elementAction[ElementActions_elementActionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.ElementActions_elementAction[ElementActions_elementActionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:element_action>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:element_actions>";
			}
			}

			var child2=0;
			var att2=0;
			child2+=this.ListingActions_listingAction.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xdat:listing_actions";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var ListingActions_listingActionCOUNT=0;ListingActions_listingActionCOUNT<this.ListingActions_listingAction.length;ListingActions_listingActionCOUNT++){
			xmlTxt +="\n<xdat:listing_action";
			xmlTxt +=this.ListingActions_listingAction[ListingActions_listingActionCOUNT].getXMLAtts();
			if(this.ListingActions_listingAction[ListingActions_listingActionCOUNT].xsiType!="xdat:element_security_listing_action"){
				xmlTxt+=" xsi:type=\"" + this.ListingActions_listingAction[ListingActions_listingActionCOUNT].xsiType + "\"";
			}
			if (this.ListingActions_listingAction[ListingActions_listingActionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.ListingActions_listingAction[ListingActions_listingActionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:listing_action>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:listing_actions>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.element_security_set_element_se_xdat_security_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.PrimarySecurityFields_primarySecurityField.length>0)return true;
			if(this.ElementActions_elementAction.length>0)return true;
			if(this.ListingActions_listingAction.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
