/*
 * web: arc_ArchiveSpecification.js
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

function arc_ArchiveSpecification(){
this.xsiType="arc:ArchiveSpecification";

	this.getSchemaElementName=function(){
		return "ArchiveSpecification";
	}

	this.getFullSchemaElementName=function(){
		return "arc:ArchiveSpecification";
	}
	this.Globalpaths =null;
	function getGlobalpaths() {
		return this.Globalpaths;
	}
	this.getGlobalpaths=getGlobalpaths;


	function setGlobalpaths(v){
		this.Globalpaths =v;
	}
	this.setGlobalpaths=setGlobalpaths;

	this.Globalpaths_GlobalpathsArcPathinfoId=null;


	function getGlobalpaths_GlobalpathsArcPathinfoId(){
		return this.Globalpaths_GlobalpathsArcPathinfoId;
	}
	this.getGlobalpaths_GlobalpathsArcPathinfoId=getGlobalpaths_GlobalpathsArcPathinfoId;


	function setGlobalpaths_GlobalpathsArcPathinfoId(v){
		this.Globalpaths_GlobalpathsArcPathinfoId=v;
	}
	this.setGlobalpaths_GlobalpathsArcPathinfoId=setGlobalpaths_GlobalpathsArcPathinfoId;
	this.Fieldspecifications_fieldspecification =new Array();

	function getFieldspecifications_fieldspecification() {
		return this.Fieldspecifications_fieldspecification;
	}
	this.getFieldspecifications_fieldspecification=getFieldspecifications_fieldspecification;


	function addFieldspecifications_fieldspecification(v){
		this.Fieldspecifications_fieldspecification.push(v);
	}
	this.addFieldspecifications_fieldspecification=addFieldspecifications_fieldspecification;
	this.Projects_project =new Array();

	function getProjects_project() {
		return this.Projects_project;
	}
	this.getProjects_project=getProjects_project;


	function addProjects_project(v){
		this.Projects_project.push(v);
	}
	this.addProjects_project=addProjects_project;

	this.Emailspecifications_newUserRegistration=null;


	function getEmailspecifications_newUserRegistration() {
		return this.Emailspecifications_newUserRegistration;
	}
	this.getEmailspecifications_newUserRegistration=getEmailspecifications_newUserRegistration;


	function setEmailspecifications_newUserRegistration(v){
		this.Emailspecifications_newUserRegistration=v;
	}
	this.setEmailspecifications_newUserRegistration=setEmailspecifications_newUserRegistration;


	this.isEmailspecifications_newUserRegistration=function(defaultValue) {
		if(this.Emailspecifications_newUserRegistration==null)return defaultValue;
		if(this.Emailspecifications_newUserRegistration=="1" || this.Emailspecifications_newUserRegistration==true)return true;
		return false;
	}

	this.Emailspecifications_pipeline=null;


	function getEmailspecifications_pipeline() {
		return this.Emailspecifications_pipeline;
	}
	this.getEmailspecifications_pipeline=getEmailspecifications_pipeline;


	function setEmailspecifications_pipeline(v){
		this.Emailspecifications_pipeline=v;
	}
	this.setEmailspecifications_pipeline=setEmailspecifications_pipeline;


	this.isEmailspecifications_pipeline=function(defaultValue) {
		if(this.Emailspecifications_pipeline==null)return defaultValue;
		if(this.Emailspecifications_pipeline=="1" || this.Emailspecifications_pipeline==true)return true;
		return false;
	}

	this.Emailspecifications_projectAccess=null;


	function getEmailspecifications_projectAccess() {
		return this.Emailspecifications_projectAccess;
	}
	this.getEmailspecifications_projectAccess=getEmailspecifications_projectAccess;


	function setEmailspecifications_projectAccess(v){
		this.Emailspecifications_projectAccess=v;
	}
	this.setEmailspecifications_projectAccess=setEmailspecifications_projectAccess;


	this.isEmailspecifications_projectAccess=function(defaultValue) {
		if(this.Emailspecifications_projectAccess==null)return defaultValue;
		if(this.Emailspecifications_projectAccess=="1" || this.Emailspecifications_projectAccess==true)return true;
		return false;
	}

	this.Emailspecifications_transfer=null;


	function getEmailspecifications_transfer() {
		return this.Emailspecifications_transfer;
	}
	this.getEmailspecifications_transfer=getEmailspecifications_transfer;


	function setEmailspecifications_transfer(v){
		this.Emailspecifications_transfer=v;
	}
	this.setEmailspecifications_transfer=setEmailspecifications_transfer;


	this.isEmailspecifications_transfer=function(defaultValue) {
		if(this.Emailspecifications_transfer==null)return defaultValue;
		if(this.Emailspecifications_transfer=="1" || this.Emailspecifications_transfer==true)return true;
		return false;
	}

	this.Emailspecifications_pageEmail=null;


	function getEmailspecifications_pageEmail() {
		return this.Emailspecifications_pageEmail;
	}
	this.getEmailspecifications_pageEmail=getEmailspecifications_pageEmail;


	function setEmailspecifications_pageEmail(v){
		this.Emailspecifications_pageEmail=v;
	}
	this.setEmailspecifications_pageEmail=setEmailspecifications_pageEmail;


	this.isEmailspecifications_pageEmail=function(defaultValue) {
		if(this.Emailspecifications_pageEmail==null)return defaultValue;
		if(this.Emailspecifications_pageEmail=="1" || this.Emailspecifications_pageEmail==true)return true;
		return false;
	}
	this.NotificationTypes_notificationType =new Array();

	function getNotificationTypes_notificationType() {
		return this.NotificationTypes_notificationType;
	}
	this.getNotificationTypes_notificationType=getNotificationTypes_notificationType;


	function addNotificationTypes_notificationType(v){
		this.NotificationTypes_notificationType.push(v);
	}
	this.addNotificationTypes_notificationType=addNotificationTypes_notificationType;

	this.Dcm_dcmHost=null;


	function getDcm_dcmHost() {
		return this.Dcm_dcmHost;
	}
	this.getDcm_dcmHost=getDcm_dcmHost;


	function setDcm_dcmHost(v){
		this.Dcm_dcmHost=v;
	}
	this.setDcm_dcmHost=setDcm_dcmHost;

	this.Dcm_dcmPort=null;


	function getDcm_dcmPort() {
		return this.Dcm_dcmPort;
	}
	this.getDcm_dcmPort=getDcm_dcmPort;


	function setDcm_dcmPort(v){
		this.Dcm_dcmPort=v;
	}
	this.setDcm_dcmPort=setDcm_dcmPort;

	this.Dcm_dcmAe=null;


	function getDcm_dcmAe() {
		return this.Dcm_dcmAe;
	}
	this.getDcm_dcmAe=getDcm_dcmAe;


	function setDcm_dcmAe(v){
		this.Dcm_dcmAe=v;
	}
	this.setDcm_dcmAe=setDcm_dcmAe;

	this.Dcm_httpUrl=null;


	function getDcm_httpUrl() {
		return this.Dcm_httpUrl;
	}
	this.getDcm_httpUrl=getDcm_httpUrl;


	function setDcm_httpUrl(v){
		this.Dcm_httpUrl=v;
	}
	this.setDcm_httpUrl=setDcm_httpUrl;

	this.Dcm_appletLink=null;


	function getDcm_appletLink() {
		return this.Dcm_appletLink;
	}
	this.getDcm_appletLink=getDcm_appletLink;


	function setDcm_appletLink(v){
		this.Dcm_appletLink=v;
	}
	this.setDcm_appletLink=setDcm_appletLink;


	this.isDcm_appletLink=function(defaultValue) {
		if(this.Dcm_appletLink==null)return defaultValue;
		if(this.Dcm_appletLink=="1" || this.Dcm_appletLink==true)return true;
		return false;
	}

	this.SiteId=null;


	function getSiteId() {
		return this.SiteId;
	}
	this.getSiteId=getSiteId;


	function setSiteId(v){
		this.SiteId=v;
	}
	this.setSiteId=setSiteId;

	this.SiteAdminEmail=null;


	function getSiteAdminEmail() {
		return this.SiteAdminEmail;
	}
	this.getSiteAdminEmail=getSiteAdminEmail;


	function setSiteAdminEmail(v){
		this.SiteAdminEmail=v;
	}
	this.setSiteAdminEmail=setSiteAdminEmail;

	this.SiteUrl=null;


	function getSiteUrl() {
		return this.SiteUrl;
	}
	this.getSiteUrl=getSiteUrl;


	function setSiteUrl(v){
		this.SiteUrl=v;
	}
	this.setSiteUrl=setSiteUrl;

	this.SmtpHost=null;


	function getSmtpHost() {
		return this.SmtpHost;
	}
	this.getSmtpHost=getSmtpHost;


	function setSmtpHost(v){
		this.SmtpHost=v;
	}
	this.setSmtpHost=setSmtpHost;

	this.RequireLogin=null;


	function getRequireLogin() {
		return this.RequireLogin;
	}
	this.getRequireLogin=getRequireLogin;


	function setRequireLogin(v){
		this.RequireLogin=v;
	}
	this.setRequireLogin=setRequireLogin;


	this.isRequireLogin=function(defaultValue) {
		if(this.RequireLogin==null)return defaultValue;
		if(this.RequireLogin=="1" || this.RequireLogin==true)return true;
		return false;
	}

	this.EnableNewRegistrations=null;


	function getEnableNewRegistrations() {
		return this.EnableNewRegistrations;
	}
	this.getEnableNewRegistrations=getEnableNewRegistrations;


	function setEnableNewRegistrations(v){
		this.EnableNewRegistrations=v;
	}
	this.setEnableNewRegistrations=setEnableNewRegistrations;


	this.isEnableNewRegistrations=function(defaultValue) {
		if(this.EnableNewRegistrations==null)return defaultValue;
		if(this.EnableNewRegistrations=="1" || this.EnableNewRegistrations==true)return true;
		return false;
	}

	this.EnableCsrfToken=null;


	function getEnableCsrfToken() {
		return this.EnableCsrfToken;
	}
	this.getEnableCsrfToken=getEnableCsrfToken;


	function setEnableCsrfToken(v){
		this.EnableCsrfToken=v;
	}
	this.setEnableCsrfToken=setEnableCsrfToken;


	this.isEnableCsrfToken=function(defaultValue) {
		if(this.EnableCsrfToken==null)return defaultValue;
		if(this.EnableCsrfToken=="1" || this.EnableCsrfToken==true)return true;
		return false;
	}

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

	this.ArcArchivespecificationId=null;


	function getArcArchivespecificationId() {
		return this.ArcArchivespecificationId;
	}
	this.getArcArchivespecificationId=getArcArchivespecificationId;


	function setArcArchivespecificationId(v){
		this.ArcArchivespecificationId=v;
	}
	this.setArcArchivespecificationId=setArcArchivespecificationId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="globalPaths"){
				return this.Globalpaths ;
			} else 
			if(xmlPath.startsWith("globalPaths")){
				xmlPath=xmlPath.substring(11);
				if(xmlPath=="")return this.Globalpaths ;
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
				if(this.Globalpaths!=undefined)return this.Globalpaths.getProperty(xmlPath);
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
			if(xmlPath=="projects/project"){
				return this.Projects_project ;
			} else 
			if(xmlPath.startsWith("projects/project")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Projects_project ;
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

				for(var whereCount=0;whereCount<this.Projects_project.length;whereCount++){

					var tempValue=this.Projects_project[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Projects_project[whereCount]);

					}

				}
				}else{

				whereArray=this.Projects_project;
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
			if(xmlPath=="emailSpecifications/new_user_registration"){
				return this.Emailspecifications_newUserRegistration ;
			} else 
			if(xmlPath=="emailSpecifications/pipeline"){
				return this.Emailspecifications_pipeline ;
			} else 
			if(xmlPath=="emailSpecifications/project_access"){
				return this.Emailspecifications_projectAccess ;
			} else 
			if(xmlPath=="emailSpecifications/transfer"){
				return this.Emailspecifications_transfer ;
			} else 
			if(xmlPath=="emailSpecifications/page_email"){
				return this.Emailspecifications_pageEmail ;
			} else 
			if(xmlPath=="notification_types/notification_type"){
				return this.NotificationTypes_notificationType ;
			} else 
			if(xmlPath.startsWith("notification_types/notification_type")){
				xmlPath=xmlPath.substring(36);
				if(xmlPath=="")return this.NotificationTypes_notificationType ;
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

				for(var whereCount=0;whereCount<this.NotificationTypes_notificationType.length;whereCount++){

					var tempValue=this.NotificationTypes_notificationType[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.NotificationTypes_notificationType[whereCount]);

					}

				}
				}else{

				whereArray=this.NotificationTypes_notificationType;
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
			if(xmlPath=="dcm/dcm_host"){
				return this.Dcm_dcmHost ;
			} else 
			if(xmlPath=="dcm/dcm_port"){
				return this.Dcm_dcmPort ;
			} else 
			if(xmlPath=="dcm/dcm_ae"){
				return this.Dcm_dcmAe ;
			} else 
			if(xmlPath=="dcm/http_url"){
				return this.Dcm_httpUrl ;
			} else 
			if(xmlPath=="dcm/applet_link"){
				return this.Dcm_appletLink ;
			} else 
			if(xmlPath=="site_id"){
				return this.SiteId ;
			} else 
			if(xmlPath=="site_admin_email"){
				return this.SiteAdminEmail ;
			} else 
			if(xmlPath=="site_url"){
				return this.SiteUrl ;
			} else 
			if(xmlPath=="smtp_host"){
				return this.SmtpHost ;
			} else 
			if(xmlPath=="require_login"){
				return this.RequireLogin ;
			} else 
			if(xmlPath=="enable_new_registrations"){
				return this.EnableNewRegistrations ;
			} else 
			if(xmlPath=="enable_csrf_token"){
				return this.EnableCsrfToken ;
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
			if(xmlPath=="arc_ArchiveSpecification_id"){
				return this.ArcArchivespecificationId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="globalPaths"){
				this.Globalpaths=value;
			} else 
			if(xmlPath.startsWith("globalPaths")){
				xmlPath=xmlPath.substring(11);
				if(xmlPath=="")return this.Globalpaths ;
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
				if(this.Globalpaths!=undefined){
					this.Globalpaths.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Globalpaths= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Globalpaths= instanciateObject("arc:pathInfo");//omUtils.js
						}
						if(options && options.where)this.Globalpaths.setProperty(options.where.field,options.where.value);
						this.Globalpaths.setProperty(xmlPath,value);
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
			if(xmlPath=="projects/project"){
				this.Projects_project=value;
			} else 
			if(xmlPath.startsWith("projects/project")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Projects_project ;
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

				for(var whereCount=0;whereCount<this.Projects_project.length;whereCount++){

					var tempValue=this.Projects_project[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Projects_project[whereCount]);

					}

				}
				}else{

				whereArray=this.Projects_project;
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
						newChild= instanciateObject("arc:project");//omUtils.js
					}
					this.addProjects_project(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="emailSpecifications/new_user_registration"){
				this.Emailspecifications_newUserRegistration=value;
			} else 
			if(xmlPath=="emailSpecifications/pipeline"){
				this.Emailspecifications_pipeline=value;
			} else 
			if(xmlPath=="emailSpecifications/project_access"){
				this.Emailspecifications_projectAccess=value;
			} else 
			if(xmlPath=="emailSpecifications/transfer"){
				this.Emailspecifications_transfer=value;
			} else 
			if(xmlPath=="emailSpecifications/page_email"){
				this.Emailspecifications_pageEmail=value;
			} else 
			if(xmlPath=="notification_types/notification_type"){
				this.NotificationTypes_notificationType=value;
			} else 
			if(xmlPath.startsWith("notification_types/notification_type")){
				xmlPath=xmlPath.substring(36);
				if(xmlPath=="")return this.NotificationTypes_notificationType ;
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

				for(var whereCount=0;whereCount<this.NotificationTypes_notificationType.length;whereCount++){

					var tempValue=this.NotificationTypes_notificationType[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.NotificationTypes_notificationType[whereCount]);

					}

				}
				}else{

				whereArray=this.NotificationTypes_notificationType;
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
						newChild= instanciateObject("arc:ArchiveSpecification_notification_type");//omUtils.js
					}
					this.addNotificationTypes_notificationType(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="dcm/dcm_host"){
				this.Dcm_dcmHost=value;
			} else 
			if(xmlPath=="dcm/dcm_port"){
				this.Dcm_dcmPort=value;
			} else 
			if(xmlPath=="dcm/dcm_ae"){
				this.Dcm_dcmAe=value;
			} else 
			if(xmlPath=="dcm/http_url"){
				this.Dcm_httpUrl=value;
			} else 
			if(xmlPath=="dcm/applet_link"){
				this.Dcm_appletLink=value;
			} else 
			if(xmlPath=="site_id"){
				this.SiteId=value;
			} else 
			if(xmlPath=="site_admin_email"){
				this.SiteAdminEmail=value;
			} else 
			if(xmlPath=="site_url"){
				this.SiteUrl=value;
			} else 
			if(xmlPath=="smtp_host"){
				this.SmtpHost=value;
			} else 
			if(xmlPath=="require_login"){
				this.RequireLogin=value;
			} else 
			if(xmlPath=="enable_new_registrations"){
				this.EnableNewRegistrations=value;
			} else 
			if(xmlPath=="enable_csrf_token"){
				this.EnableCsrfToken=value;
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
			if(xmlPath=="arc_ArchiveSpecification_id"){
				this.ArcArchivespecificationId=value;
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
		if (xmlPath=="globalPaths"){
			this.setGlobalpaths(v);
		}else if (xmlPath=="fieldSpecifications/fieldSpecification"){
			this.addFieldspecifications_fieldspecification(v);
		}else if (xmlPath=="projects/project"){
			this.addProjects_project(v);
		}else if (xmlPath=="notification_types/notification_type"){
			this.addNotificationTypes_notificationType(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="globalPaths"){
			return "http://nrg.wustl.edu/arc:pathInfo";
		}else if (xmlPath=="fieldSpecifications/fieldSpecification"){
			return "http://nrg.wustl.edu/arc:fieldSpecification";
		}else if (xmlPath=="projects/project"){
			return "http://nrg.wustl.edu/arc:project";
		}else if (xmlPath=="notification_types/notification_type"){
			return "http://nrg.wustl.edu/arc:ArchiveSpecification_notification_type";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="globalPaths"){
			return "field_single_reference";
		}else if (xmlPath=="fieldSpecifications/fieldSpecification"){
			return "field_NO_CHILD";
		}else if (xmlPath=="projects/project"){
			return "field_multi_reference";
		}else if (xmlPath=="emailSpecifications/new_user_registration"){
			return "field_data";
		}else if (xmlPath=="emailSpecifications/pipeline"){
			return "field_data";
		}else if (xmlPath=="emailSpecifications/project_access"){
			return "field_data";
		}else if (xmlPath=="emailSpecifications/transfer"){
			return "field_data";
		}else if (xmlPath=="emailSpecifications/page_email"){
			return "field_data";
		}else if (xmlPath=="notification_types/notification_type"){
			return "field_NO_CHILD";
		}else if (xmlPath=="dcm/dcm_host"){
			return "field_data";
		}else if (xmlPath=="dcm/dcm_port"){
			return "field_data";
		}else if (xmlPath=="dcm/dcm_ae"){
			return "field_data";
		}else if (xmlPath=="dcm/http_url"){
			return "field_data";
		}else if (xmlPath=="dcm/applet_link"){
			return "field_data";
		}else if (xmlPath=="site_id"){
			return "field_data";
		}else if (xmlPath=="site_admin_email"){
			return "field_data";
		}else if (xmlPath=="site_url"){
			return "field_data";
		}else if (xmlPath=="smtp_host"){
			return "field_data";
		}else if (xmlPath=="require_login"){
			return "field_data";
		}else if (xmlPath=="enable_new_registrations"){
			return "field_data";
		}else if (xmlPath=="enable_csrf_token"){
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
		xmlTxt+="\n<arc:ArchiveSpecification";
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
		xmlTxt+="\n</arc:ArchiveSpecification>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ArcArchivespecificationId!=null){
				if(hiddenCount++>0)str+=",";
				str+="arc_ArchiveSpecification_id=\"" + this.ArcArchivespecificationId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.SiteId!=null)
			attTxt+=" site_id=\"" +this.SiteId +"\"";
		else attTxt+=" site_id=\"\"";//REQUIRED FIELD

		if (this.SiteAdminEmail!=null)
			attTxt+=" site_admin_email=\"" +this.SiteAdminEmail +"\"";
		//NOT REQUIRED FIELD

		if (this.SiteUrl!=null)
			attTxt+=" site_url=\"" +this.SiteUrl +"\"";
		//NOT REQUIRED FIELD

		if (this.SmtpHost!=null)
			attTxt+=" smtp_host=\"" +this.SmtpHost +"\"";
		//NOT REQUIRED FIELD

		if (this.RequireLogin!=null)
			attTxt+=" require_login=\"" +this.RequireLogin +"\"";
		//NOT REQUIRED FIELD

		if (this.EnableNewRegistrations!=null)
			attTxt+=" enable_new_registrations=\"" +this.EnableNewRegistrations +"\"";
		//NOT REQUIRED FIELD

		if (this.EnableCsrfToken!=null)
			attTxt+=" enable_csrf_token=\"" +this.EnableCsrfToken +"\"";
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
		if (this.Globalpaths!=null){
			xmlTxt+="\n<arc:globalPaths";
			xmlTxt+=this.Globalpaths.getXMLAtts();
			if(this.Globalpaths.xsiType!="arc:pathInfo"){
				xmlTxt+=" xsi:type=\"" + this.Globalpaths.xsiType + "\"";
			}
			if (this.Globalpaths.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Globalpaths.getXMLBody(preventComments);
				xmlTxt+="</arc:globalPaths>";
			}else {xmlTxt+="/>";}
		}
		else{
			xmlTxt+="\n<arc:globalPaths/>";//REQUIRED
		}
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
			child1+=this.Projects_project.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<arc:projects";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Projects_projectCOUNT=0;Projects_projectCOUNT<this.Projects_project.length;Projects_projectCOUNT++){
			xmlTxt +="\n<arc:project";
			xmlTxt +=this.Projects_project[Projects_projectCOUNT].getXMLAtts();
			if(this.Projects_project[Projects_projectCOUNT].xsiType!="arc:project"){
				xmlTxt+=" xsi:type=\"" + this.Projects_project[Projects_projectCOUNT].xsiType + "\"";
			}
			if (this.Projects_project[Projects_projectCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Projects_project[Projects_projectCOUNT].getXMLBody(preventComments);
					xmlTxt+="</arc:project>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</arc:projects>";
			}
			}

		var EmailspecificationsATT = ""
		if (this.Emailspecifications_newUserRegistration!=null)
			EmailspecificationsATT+=" new_user_registration=\"" + this.Emailspecifications_newUserRegistration + "\"";
		if (this.Emailspecifications_pipeline!=null)
			EmailspecificationsATT+=" pipeline=\"" + this.Emailspecifications_pipeline + "\"";
		if (this.Emailspecifications_projectAccess!=null)
			EmailspecificationsATT+=" project_access=\"" + this.Emailspecifications_projectAccess + "\"";
		if (this.Emailspecifications_transfer!=null)
			EmailspecificationsATT+=" transfer=\"" + this.Emailspecifications_transfer + "\"";
		if (this.Emailspecifications_pageEmail!=null)
			EmailspecificationsATT+=" page_email=\"" + this.Emailspecifications_pageEmail + "\"";
		if(EmailspecificationsATT!=""){
			xmlTxt+="\n<arc:emailSpecifications";
			xmlTxt+=EmailspecificationsATT;
			xmlTxt+="/>";
		}

			var child2=0;
			var att2=0;
			child2+=this.NotificationTypes_notificationType.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<arc:notification_types";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var NotificationTypes_notificationTypeCOUNT=0;NotificationTypes_notificationTypeCOUNT<this.NotificationTypes_notificationType.length;NotificationTypes_notificationTypeCOUNT++){
			xmlTxt +="\n<arc:notification_type";
			xmlTxt +=this.NotificationTypes_notificationType[NotificationTypes_notificationTypeCOUNT].getXMLAtts();
			if(this.NotificationTypes_notificationType[NotificationTypes_notificationTypeCOUNT].xsiType!="arc:ArchiveSpecification_notification_type"){
				xmlTxt+=" xsi:type=\"" + this.NotificationTypes_notificationType[NotificationTypes_notificationTypeCOUNT].xsiType + "\"";
			}
			if (this.NotificationTypes_notificationType[NotificationTypes_notificationTypeCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.NotificationTypes_notificationType[NotificationTypes_notificationTypeCOUNT].getXMLBody(preventComments);
					xmlTxt+="</arc:notification_type>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</arc:notification_types>";
			}
			}

		var DcmATT = ""
		if (this.Dcm_dcmHost!=null)
			DcmATT+=" dcm_host=\"" + this.Dcm_dcmHost.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Dcm_dcmPort!=null)
			DcmATT+=" dcm_port=\"" + this.Dcm_dcmPort.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Dcm_dcmAe!=null)
			DcmATT+=" dcm_ae=\"" + this.Dcm_dcmAe.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Dcm_httpUrl!=null)
			DcmATT+=" http_url=\"" + this.Dcm_httpUrl.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Dcm_appletLink!=null)
			DcmATT+=" applet_link=\"" + this.Dcm_appletLink + "\"";
		if(DcmATT!=""){
			xmlTxt+="\n<arc:dcm";
			xmlTxt+=DcmATT;
			xmlTxt+="/>";
		}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ArcArchivespecificationId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Globalpaths!=null){
			if (this.Globalpaths.hasXMLBodyContent()) return true;
		}
		return true;//REQUIRED globalPaths
	}
}
