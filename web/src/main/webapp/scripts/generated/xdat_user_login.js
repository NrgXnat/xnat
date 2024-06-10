/*
 * web: xdat_user_login.js
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

function xdat_user_login(){
this.xsiType="xdat:user_login";

	this.getSchemaElementName=function(){
		return "user_login";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:user_login";
	}

	this.LoginDate=null;


	function getLoginDate() {
		return this.LoginDate;
	}
	this.getLoginDate=getLoginDate;


	function setLoginDate(v){
		this.LoginDate=v;
	}
	this.setLoginDate=setLoginDate;

	this.LogoutDate=null;


	function getLogoutDate() {
		return this.LogoutDate;
	}
	this.getLogoutDate=getLogoutDate;


	function setLogoutDate(v){
		this.LogoutDate=v;
	}
	this.setLogoutDate=setLogoutDate;

	this.SessionId=null;


	function getSessionId() {
		return this.SessionId;
	}
	this.getSessionId=getSessionId;


	function setSessionId(v){
		this.SessionId=v;
	}
	this.setSessionId=setSessionId;

	this.IpAddress=null;


	function getIpAddress() {
		return this.IpAddress;
	}
	this.getIpAddress=getIpAddress;


	function setIpAddress(v){
		this.IpAddress=v;
	}
	this.setIpAddress=setIpAddress;
	this.User =null;
	function getUser() {
		return this.User;
	}
	this.getUser=getUser;


	function setUser(v){
		this.User =v;
	}
	this.setUser=setUser;

	this.User_UserXdatUserId=null;


	function getUser_UserXdatUserId(){
		return this.User_UserXdatUserId;
	}
	this.getUser_UserXdatUserId=getUser_UserXdatUserId;


	function setUser_UserXdatUserId(v){
		this.User_UserXdatUserId=v;
	}
	this.setUser_UserXdatUserId=setUser_UserXdatUserId;

	this.XdatUserLoginId=null;


	function getXdatUserLoginId() {
		return this.XdatUserLoginId;
	}
	this.getXdatUserLoginId=getXdatUserLoginId;


	function setXdatUserLoginId(v){
		this.XdatUserLoginId=v;
	}
	this.setXdatUserLoginId=setXdatUserLoginId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="login_date"){
				return this.LoginDate ;
			} else 
			if(xmlPath=="logout_date"){
				return this.LogoutDate ;
			} else 
			if(xmlPath=="session_id"){
				return this.SessionId ;
			} else 
			if(xmlPath=="ip_address"){
				return this.IpAddress ;
			} else 
			if(xmlPath=="user"){
				return this.User ;
			} else 
			if(xmlPath.startsWith("user")){
				xmlPath=xmlPath.substring(4);
				if(xmlPath=="")return this.User ;
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
				if(this.User!=undefined)return this.User.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_user_login_id"){
				return this.XdatUserLoginId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="login_date"){
				this.LoginDate=value;
			} else 
			if(xmlPath=="logout_date"){
				this.LogoutDate=value;
			} else 
			if(xmlPath=="session_id"){
				this.SessionId=value;
			} else 
			if(xmlPath=="ip_address"){
				this.IpAddress=value;
			} else 
			if(xmlPath=="user"){
				this.User=value;
			} else 
			if(xmlPath.startsWith("user")){
				xmlPath=xmlPath.substring(4);
				if(xmlPath=="")return this.User ;
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
				if(this.User!=undefined){
					this.User.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.User= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.User= instanciateObject("xdat:user");//omUtils.js
						}
						if(options && options.where)this.User.setProperty(options.where.field,options.where.value);
						this.User.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_user_login_id"){
				this.XdatUserLoginId=value;
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
		if (xmlPath=="user"){
			this.setUser(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="user"){
			return "http://nrg.wustl.edu/security:user";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="login_date"){
			return "field_data";
		}else if (xmlPath=="logout_date"){
			return "field_data";
		}else if (xmlPath=="session_id"){
			return "field_data";
		}else if (xmlPath=="ip_address"){
			return "field_data";
		}else if (xmlPath=="user"){
			return "field_single_reference";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:user_login";
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
		xmlTxt+="\n</xdat:user_login>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatUserLoginId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_user_login_id=\"" + this.XdatUserLoginId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.LoginDate!=null){
			xmlTxt+="\n<xdat:login_date";
			xmlTxt+=">";
			xmlTxt+=this.LoginDate;
			xmlTxt+="</xdat:login_date>";
		}
		if (this.LogoutDate!=null){
			xmlTxt+="\n<xdat:logout_date";
			xmlTxt+=">";
			xmlTxt+=this.LogoutDate;
			xmlTxt+="</xdat:logout_date>";
		}
		if (this.SessionId!=null){
			xmlTxt+="\n<xdat:session_id";
			xmlTxt+=">";
			xmlTxt+=this.SessionId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:session_id>";
		}
		if (this.IpAddress!=null){
			xmlTxt+="\n<xdat:ip_address";
			xmlTxt+=">";
			xmlTxt+=this.IpAddress.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:ip_address>";
		}
		if (this.User!=null){
			xmlTxt+="\n<xdat:user";
			xmlTxt+=this.User.getXMLAtts();
			if(this.User.xsiType!="xdat:user"){
				xmlTxt+=" xsi:type=\"" + this.User.xsiType + "\"";
			}
			if (this.User.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.User.getXMLBody(preventComments);
				xmlTxt+="</xdat:user>";
			}else {xmlTxt+="/>";}
		}
		else{
			xmlTxt+="\n<xdat:user/>";//REQUIRED
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatUserLoginId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.LoginDate!=null) return true;
		if (this.LogoutDate!=null) return true;
		if (this.SessionId!=null) return true;
		if (this.IpAddress!=null) return true;
		if (this.User!=null){
			if (this.User.hasXMLBodyContent()) return true;
		}
		return true;//REQUIRED user
	}
}
