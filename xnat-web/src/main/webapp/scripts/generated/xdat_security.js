/*
 * web: xdat_security.js
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

function xdat_security(){
this.xsiType="xdat:security";

	this.getSchemaElementName=function(){
		return "security";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:security";
	}
	this.Groups_group =new Array();

	function getGroups_group() {
		return this.Groups_group;
	}
	this.getGroups_group=getGroups_group;


	function addGroups_group(v){
		this.Groups_group.push(v);
	}
	this.addGroups_group=addGroups_group;
	this.Users_user =new Array();

	function getUsers_user() {
		return this.Users_user;
	}
	this.getUsers_user=getUsers_user;


	function addUsers_user(v){
		this.Users_user.push(v);
	}
	this.addUsers_user=addUsers_user;
	this.Roles_role =new Array();

	function getRoles_role() {
		return this.Roles_role;
	}
	this.getRoles_role=getRoles_role;


	function addRoles_role(v){
		this.Roles_role.push(v);
	}
	this.addRoles_role=addRoles_role;
	this.Actions_action =new Array();

	function getActions_action() {
		return this.Actions_action;
	}
	this.getActions_action=getActions_action;


	function addActions_action(v){
		this.Actions_action.push(v);
	}
	this.addActions_action=addActions_action;
	this.ElementSecuritySet_elementSecurity =new Array();

	function getElementSecuritySet_elementSecurity() {
		return this.ElementSecuritySet_elementSecurity;
	}
	this.getElementSecuritySet_elementSecurity=getElementSecuritySet_elementSecurity;


	function addElementSecuritySet_elementSecurity(v){
		this.ElementSecuritySet_elementSecurity.push(v);
	}
	this.addElementSecuritySet_elementSecurity=addElementSecuritySet_elementSecurity;
	this.Newslist_news =null;
	function getNewslist_news() {
		return this.Newslist_news;
	}
	this.getNewslist_news=getNewslist_news;


	function setNewslist_news(v){
		this.Newslist_news =v;
	}
	this.setNewslist_news=setNewslist_news;

	this.Newslist_news_NewslistNewsXdatNewsentryId=null;


	function getNewslist_news_NewslistNewsXdatNewsentryId(){
		return this.Newslist_news_NewslistNewsXdatNewsentryId;
	}
	this.getNewslist_news_NewslistNewsXdatNewsentryId=getNewslist_news_NewslistNewsXdatNewsentryId;


	function setNewslist_news_NewslistNewsXdatNewsentryId(v){
		this.Newslist_news_NewslistNewsXdatNewsentryId=v;
	}
	this.setNewslist_news_NewslistNewsXdatNewsentryId=setNewslist_news_NewslistNewsXdatNewsentryId;
	this.Infolist_info =null;
	function getInfolist_info() {
		return this.Infolist_info;
	}
	this.getInfolist_info=getInfolist_info;


	function setInfolist_info(v){
		this.Infolist_info =v;
	}
	this.setInfolist_info=setInfolist_info;

	this.Infolist_info_InfolistInfoXdatInfoentryId=null;


	function getInfolist_info_InfolistInfoXdatInfoentryId(){
		return this.Infolist_info_InfolistInfoXdatInfoentryId;
	}
	this.getInfolist_info_InfolistInfoXdatInfoentryId=getInfolist_info_InfolistInfoXdatInfoentryId;


	function setInfolist_info_InfolistInfoXdatInfoentryId(v){
		this.Infolist_info_InfolistInfoXdatInfoentryId=v;
	}
	this.setInfolist_info_InfolistInfoXdatInfoentryId=setInfolist_info_InfolistInfoXdatInfoentryId;

	this.System=null;


	function getSystem() {
		return this.System;
	}
	this.getSystem=getSystem;


	function setSystem(v){
		this.System=v;
	}
	this.setSystem=setSystem;

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

	this.XdatSecurityId=null;


	function getXdatSecurityId() {
		return this.XdatSecurityId;
	}
	this.getXdatSecurityId=getXdatSecurityId;


	function setXdatSecurityId(v){
		this.XdatSecurityId=v;
	}
	this.setXdatSecurityId=setXdatSecurityId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="groups/group"){
				return this.Groups_group ;
			} else 
			if(xmlPath.startsWith("groups/group")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Groups_group ;
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

				for(var whereCount=0;whereCount<this.Groups_group.length;whereCount++){

					var tempValue=this.Groups_group[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Groups_group[whereCount]);

					}

				}
				}else{

				whereArray=this.Groups_group;
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
			if(xmlPath=="users/user"){
				return this.Users_user ;
			} else 
			if(xmlPath.startsWith("users/user")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Users_user ;
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

				for(var whereCount=0;whereCount<this.Users_user.length;whereCount++){

					var tempValue=this.Users_user[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Users_user[whereCount]);

					}

				}
				}else{

				whereArray=this.Users_user;
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
			if(xmlPath=="roles/role"){
				return this.Roles_role ;
			} else 
			if(xmlPath.startsWith("roles/role")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Roles_role ;
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

				for(var whereCount=0;whereCount<this.Roles_role.length;whereCount++){

					var tempValue=this.Roles_role[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Roles_role[whereCount]);

					}

				}
				}else{

				whereArray=this.Roles_role;
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
			if(xmlPath=="actions/action"){
				return this.Actions_action ;
			} else 
			if(xmlPath.startsWith("actions/action")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Actions_action ;
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

				for(var whereCount=0;whereCount<this.Actions_action.length;whereCount++){

					var tempValue=this.Actions_action[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Actions_action[whereCount]);

					}

				}
				}else{

				whereArray=this.Actions_action;
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
			if(xmlPath=="element_security_set/element_security"){
				return this.ElementSecuritySet_elementSecurity ;
			} else 
			if(xmlPath.startsWith("element_security_set/element_security")){
				xmlPath=xmlPath.substring(37);
				if(xmlPath=="")return this.ElementSecuritySet_elementSecurity ;
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

				for(var whereCount=0;whereCount<this.ElementSecuritySet_elementSecurity.length;whereCount++){

					var tempValue=this.ElementSecuritySet_elementSecurity[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ElementSecuritySet_elementSecurity[whereCount]);

					}

				}
				}else{

				whereArray=this.ElementSecuritySet_elementSecurity;
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
			if(xmlPath=="newsList/news"){
				return this.Newslist_news ;
			} else 
			if(xmlPath.startsWith("newsList/news")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Newslist_news ;
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
				if(this.Newslist_news!=undefined)return this.Newslist_news.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="infoList/info"){
				return this.Infolist_info ;
			} else 
			if(xmlPath.startsWith("infoList/info")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Infolist_info ;
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
				if(this.Infolist_info!=undefined)return this.Infolist_info.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="system"){
				return this.System ;
			} else 
			if(xmlPath=="require_login"){
				return this.RequireLogin ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_security_id"){
				return this.XdatSecurityId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="groups/group"){
				this.Groups_group=value;
			} else 
			if(xmlPath.startsWith("groups/group")){
				xmlPath=xmlPath.substring(12);
				if(xmlPath=="")return this.Groups_group ;
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

				for(var whereCount=0;whereCount<this.Groups_group.length;whereCount++){

					var tempValue=this.Groups_group[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Groups_group[whereCount]);

					}

				}
				}else{

				whereArray=this.Groups_group;
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
						newChild= instanciateObject("xdat:userGroup");//omUtils.js
					}
					this.addGroups_group(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="users/user"){
				this.Users_user=value;
			} else 
			if(xmlPath.startsWith("users/user")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Users_user ;
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

				for(var whereCount=0;whereCount<this.Users_user.length;whereCount++){

					var tempValue=this.Users_user[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Users_user[whereCount]);

					}

				}
				}else{

				whereArray=this.Users_user;
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
						newChild= instanciateObject("xdat:user");//omUtils.js
					}
					this.addUsers_user(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="roles/role"){
				this.Roles_role=value;
			} else 
			if(xmlPath.startsWith("roles/role")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Roles_role ;
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

				for(var whereCount=0;whereCount<this.Roles_role.length;whereCount++){

					var tempValue=this.Roles_role[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Roles_role[whereCount]);

					}

				}
				}else{

				whereArray=this.Roles_role;
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
						newChild= instanciateObject("xdat:role_type");//omUtils.js
					}
					this.addRoles_role(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="actions/action"){
				this.Actions_action=value;
			} else 
			if(xmlPath.startsWith("actions/action")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Actions_action ;
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

				for(var whereCount=0;whereCount<this.Actions_action.length;whereCount++){

					var tempValue=this.Actions_action[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Actions_action[whereCount]);

					}

				}
				}else{

				whereArray=this.Actions_action;
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
						newChild= instanciateObject("xdat:action_type");//omUtils.js
					}
					this.addActions_action(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="element_security_set/element_security"){
				this.ElementSecuritySet_elementSecurity=value;
			} else 
			if(xmlPath.startsWith("element_security_set/element_security")){
				xmlPath=xmlPath.substring(37);
				if(xmlPath=="")return this.ElementSecuritySet_elementSecurity ;
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

				for(var whereCount=0;whereCount<this.ElementSecuritySet_elementSecurity.length;whereCount++){

					var tempValue=this.ElementSecuritySet_elementSecurity[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ElementSecuritySet_elementSecurity[whereCount]);

					}

				}
				}else{

				whereArray=this.ElementSecuritySet_elementSecurity;
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
						newChild= instanciateObject("xdat:element_security");//omUtils.js
					}
					this.addElementSecuritySet_elementSecurity(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="newsList/news"){
				this.Newslist_news=value;
			} else 
			if(xmlPath.startsWith("newsList/news")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Newslist_news ;
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
				if(this.Newslist_news!=undefined){
					this.Newslist_news.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Newslist_news= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Newslist_news= instanciateObject("xdat:newsEntry");//omUtils.js
						}
						if(options && options.where)this.Newslist_news.setProperty(options.where.field,options.where.value);
						this.Newslist_news.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="infoList/info"){
				this.Infolist_info=value;
			} else 
			if(xmlPath.startsWith("infoList/info")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Infolist_info ;
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
				if(this.Infolist_info!=undefined){
					this.Infolist_info.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Infolist_info= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Infolist_info= instanciateObject("xdat:infoEntry");//omUtils.js
						}
						if(options && options.where)this.Infolist_info.setProperty(options.where.field,options.where.value);
						this.Infolist_info.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="system"){
				this.System=value;
			} else 
			if(xmlPath=="require_login"){
				this.RequireLogin=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_security_id"){
				this.XdatSecurityId=value;
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
		if (xmlPath=="groups/group"){
			this.addGroups_group(v);
		}else if (xmlPath=="users/user"){
			this.addUsers_user(v);
		}else if (xmlPath=="roles/role"){
			this.addRoles_role(v);
		}else if (xmlPath=="actions/action"){
			this.addActions_action(v);
		}else if (xmlPath=="element_security_set/element_security"){
			this.addElementSecuritySet_elementSecurity(v);
		}else if (xmlPath=="newsList/news"){
			this.setNewslist_news(v);
		}else if (xmlPath=="infoList/info"){
			this.setInfolist_info(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="groups/group"){
			return "http://nrg.wustl.edu/security:userGroup";
		}else if (xmlPath=="users/user"){
			return "http://nrg.wustl.edu/security:user";
		}else if (xmlPath=="roles/role"){
			return "http://nrg.wustl.edu/security:role_type";
		}else if (xmlPath=="actions/action"){
			return "http://nrg.wustl.edu/security:action_type";
		}else if (xmlPath=="element_security_set/element_security"){
			return "http://nrg.wustl.edu/security:element_security";
		}else if (xmlPath=="newsList/news"){
			return "http://nrg.wustl.edu/security:newsEntry";
		}else if (xmlPath=="infoList/info"){
			return "http://nrg.wustl.edu/security:infoEntry";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="groups/group"){
			return "field_multi_reference";
		}else if (xmlPath=="users/user"){
			return "field_multi_reference";
		}else if (xmlPath=="roles/role"){
			return "field_multi_reference";
		}else if (xmlPath=="actions/action"){
			return "field_multi_reference";
		}else if (xmlPath=="element_security_set/element_security"){
			return "field_multi_reference";
		}else if (xmlPath=="newsList/news"){
			return "field_single_reference";
		}else if (xmlPath=="infoList/info"){
			return "field_single_reference";
		}else if (xmlPath=="system"){
			return "field_data";
		}else if (xmlPath=="require_login"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:security";
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
		xmlTxt+="\n</xdat:security>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatSecurityId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_security_id=\"" + this.XdatSecurityId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.System!=null)
			attTxt+=" system=\"" +this.System +"\"";
		else attTxt+=" system=\"\"";//REQUIRED FIELD

		if (this.RequireLogin!=null)
			attTxt+=" require_login=\"" +this.RequireLogin +"\"";
		else attTxt+=" require_login=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Groups_group.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xdat:groups";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Groups_groupCOUNT=0;Groups_groupCOUNT<this.Groups_group.length;Groups_groupCOUNT++){
			xmlTxt +="\n<xdat:group";
			xmlTxt +=this.Groups_group[Groups_groupCOUNT].getXMLAtts();
			if(this.Groups_group[Groups_groupCOUNT].xsiType!="xdat:userGroup"){
				xmlTxt+=" xsi:type=\"" + this.Groups_group[Groups_groupCOUNT].xsiType + "\"";
			}
			if (this.Groups_group[Groups_groupCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Groups_group[Groups_groupCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:group>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:groups>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.Users_user.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xdat:users";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Users_userCOUNT=0;Users_userCOUNT<this.Users_user.length;Users_userCOUNT++){
			xmlTxt +="\n<xdat:user";
			xmlTxt +=this.Users_user[Users_userCOUNT].getXMLAtts();
			if(this.Users_user[Users_userCOUNT].xsiType!="xdat:user"){
				xmlTxt+=" xsi:type=\"" + this.Users_user[Users_userCOUNT].xsiType + "\"";
			}
			if (this.Users_user[Users_userCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Users_user[Users_userCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:user>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:users>";
			}
			}

			var child2=0;
			var att2=0;
			child2+=this.Roles_role.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xdat:roles";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Roles_roleCOUNT=0;Roles_roleCOUNT<this.Roles_role.length;Roles_roleCOUNT++){
			xmlTxt +="\n<xdat:role";
			xmlTxt +=this.Roles_role[Roles_roleCOUNT].getXMLAtts();
			if(this.Roles_role[Roles_roleCOUNT].xsiType!="xdat:role_type"){
				xmlTxt+=" xsi:type=\"" + this.Roles_role[Roles_roleCOUNT].xsiType + "\"";
			}
			if (this.Roles_role[Roles_roleCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Roles_role[Roles_roleCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:role>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:roles>";
			}
			}

			var child3=0;
			var att3=0;
			child3+=this.Actions_action.length;
			if(child3>0 || att3>0){
				xmlTxt+="\n<xdat:actions";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Actions_actionCOUNT=0;Actions_actionCOUNT<this.Actions_action.length;Actions_actionCOUNT++){
			xmlTxt +="\n<xdat:action";
			xmlTxt +=this.Actions_action[Actions_actionCOUNT].getXMLAtts();
			if(this.Actions_action[Actions_actionCOUNT].xsiType!="xdat:action_type"){
				xmlTxt+=" xsi:type=\"" + this.Actions_action[Actions_actionCOUNT].xsiType + "\"";
			}
			if (this.Actions_action[Actions_actionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Actions_action[Actions_actionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:action>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:actions>";
			}
			}

			var child4=0;
			var att4=0;
			child4+=this.ElementSecuritySet_elementSecurity.length;
			if(child4>0 || att4>0){
				xmlTxt+="\n<xdat:element_security_set";
			if(child4==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var ElementSecuritySet_elementSecurityCOUNT=0;ElementSecuritySet_elementSecurityCOUNT<this.ElementSecuritySet_elementSecurity.length;ElementSecuritySet_elementSecurityCOUNT++){
			xmlTxt +="\n<xdat:element_security";
			xmlTxt +=this.ElementSecuritySet_elementSecurity[ElementSecuritySet_elementSecurityCOUNT].getXMLAtts();
			if(this.ElementSecuritySet_elementSecurity[ElementSecuritySet_elementSecurityCOUNT].xsiType!="xdat:element_security"){
				xmlTxt+=" xsi:type=\"" + this.ElementSecuritySet_elementSecurity[ElementSecuritySet_elementSecurityCOUNT].xsiType + "\"";
			}
			if (this.ElementSecuritySet_elementSecurity[ElementSecuritySet_elementSecurityCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.ElementSecuritySet_elementSecurity[ElementSecuritySet_elementSecurityCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:element_security>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:element_security_set>";
			}
			}

			var child5=0;
			var att5=0;
			if(this.Newslist_news!=null)
			child5++;
			if(child5>0 || att5>0){
				xmlTxt+="\n<xdat:newsList";
			if(child5==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Newslist_news!=null){
			xmlTxt+="\n<xdat:news";
			xmlTxt+=this.Newslist_news.getXMLAtts();
			if(this.Newslist_news.xsiType!="xdat:newsEntry"){
				xmlTxt+=" xsi:type=\"" + this.Newslist_news.xsiType + "\"";
			}
			if (this.Newslist_news.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Newslist_news.getXMLBody(preventComments);
				xmlTxt+="</xdat:news>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

				xmlTxt+="\n</xdat:newsList>";
			}
			}

			var child6=0;
			var att6=0;
			if(this.Infolist_info!=null)
			child6++;
			if(child6>0 || att6>0){
				xmlTxt+="\n<xdat:infoList";
			if(child6==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Infolist_info!=null){
			xmlTxt+="\n<xdat:info";
			xmlTxt+=this.Infolist_info.getXMLAtts();
			if(this.Infolist_info.xsiType!="xdat:infoEntry"){
				xmlTxt+=" xsi:type=\"" + this.Infolist_info.xsiType + "\"";
			}
			if (this.Infolist_info.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Infolist_info.getXMLBody(preventComments);
				xmlTxt+="</xdat:info>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

				xmlTxt+="\n</xdat:infoList>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatSecurityId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.Groups_group.length>0)return true;
			if(this.Users_user.length>0)return true;
			if(this.Roles_role.length>0)return true;
			if(this.Actions_action.length>0)return true;
			if(this.ElementSecuritySet_elementSecurity.length>0)return true;
			if(this.Newslist_news!=null) return true;
			if(this.Infolist_info!=null) return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
