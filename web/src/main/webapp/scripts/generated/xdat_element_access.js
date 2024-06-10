/*
 * web: xdat_element_access.js
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

function xdat_element_access(){
this.xsiType="xdat:element_access";

	this.getSchemaElementName=function(){
		return "element_access";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:element_access";
	}

	this.SecondaryPassword=null;


	function getSecondaryPassword() {
		return this.SecondaryPassword;
	}
	this.getSecondaryPassword=getSecondaryPassword;


	function setSecondaryPassword(v){
		this.SecondaryPassword=v;
	}
	this.setSecondaryPassword=setSecondaryPassword;

	this.SecondaryPassword_encrypt=null;


	function getSecondaryPassword_encrypt() {
		return this.SecondaryPassword_encrypt;
	}
	this.getSecondaryPassword_encrypt=getSecondaryPassword_encrypt;


	function setSecondaryPassword_encrypt(v){
		this.SecondaryPassword_encrypt=v;
	}
	this.setSecondaryPassword_encrypt=setSecondaryPassword_encrypt;


	this.isSecondaryPassword_encrypt=function(defaultValue) {
		if(this.SecondaryPassword_encrypt==null)return defaultValue;
		if(this.SecondaryPassword_encrypt=="1" || this.SecondaryPassword_encrypt==true)return true;
		return false;
	}
	this.SecureIp =new Array();

	function getSecureIp() {
		return this.SecureIp;
	}
	this.getSecureIp=getSecureIp;


	function addSecureIp(v){
		this.SecureIp.push(v);
	}
	this.addSecureIp=addSecureIp;
	this.Permissions_allowSet =new Array();

	function getPermissions_allowSet() {
		return this.Permissions_allowSet;
	}
	this.getPermissions_allowSet=getPermissions_allowSet;


	function addPermissions_allowSet(v){
		this.Permissions_allowSet.push(v);
	}
	this.addPermissions_allowSet=addPermissions_allowSet;

	this.ElementName=null;


	function getElementName() {
		return this.ElementName;
	}
	this.getElementName=getElementName;


	function setElementName(v){
		this.ElementName=v;
	}
	this.setElementName=setElementName;

	this.XdatElementAccessId=null;


	function getXdatElementAccessId() {
		return this.XdatElementAccessId;
	}
	this.getXdatElementAccessId=getXdatElementAccessId;


	function setXdatElementAccessId(v){
		this.XdatElementAccessId=v;
	}
	this.setXdatElementAccessId=setXdatElementAccessId;

	this.xdat_user_xdat_user_id_fk=null;


	this.getxdat_user_xdat_user_id=function() {
		return this.xdat_user_xdat_user_id_fk;
	}


	this.setxdat_user_xdat_user_id=function(v){
		this.xdat_user_xdat_user_id_fk=v;
	}

	this.xdat_userGroup_xdat_usergroup_id_fk=null;


	this.getxdat_userGroup_xdat_usergroup_id=function() {
		return this.xdat_userGroup_xdat_usergroup_id_fk;
	}


	this.setxdat_userGroup_xdat_usergroup_id=function(v){
		this.xdat_userGroup_xdat_usergroup_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="secondary_password"){
				return this.SecondaryPassword ;
			} else 
			if(xmlPath=="secondary_password/encrypt"){
				return this.SecondaryPassword_encrypt ;
			} else 
			if(xmlPath=="secure_ip"){
				return this.SecureIp ;
			} else 
			if(xmlPath.startsWith("secure_ip")){
				xmlPath=xmlPath.substring(9);
				if(xmlPath=="")return this.SecureIp ;
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

				for(var whereCount=0;whereCount<this.SecureIp.length;whereCount++){

					var tempValue=this.SecureIp[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.SecureIp[whereCount]);

					}

				}
				}else{

				whereArray=this.SecureIp;
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
			if(xmlPath=="permissions/allow_set"){
				return this.Permissions_allowSet ;
			} else 
			if(xmlPath.startsWith("permissions/allow_set")){
				xmlPath=xmlPath.substring(21);
				if(xmlPath=="")return this.Permissions_allowSet ;
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

				for(var whereCount=0;whereCount<this.Permissions_allowSet.length;whereCount++){

					var tempValue=this.Permissions_allowSet[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Permissions_allowSet[whereCount]);

					}

				}
				}else{

				whereArray=this.Permissions_allowSet;
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
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_element_access_id"){
				return this.XdatElementAccessId ;
			} else 
			if(xmlPath=="xdat_user_xdat_user_id"){
				return this.xdat_user_xdat_user_id_fk ;
			} else 
			if(xmlPath=="xdat_userGroup_xdat_usergroup_id"){
				return this.xdat_userGroup_xdat_usergroup_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="secondary_password"){
				this.SecondaryPassword=value;
			} else 
			if(xmlPath=="secondary_password/encrypt"){
				this.SecondaryPassword_encrypt=value;
			} else 
			if(xmlPath=="secure_ip"){
				this.SecureIp=value;
			} else 
			if(xmlPath.startsWith("secure_ip")){
				xmlPath=xmlPath.substring(9);
				if(xmlPath=="")return this.SecureIp ;
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

				for(var whereCount=0;whereCount<this.SecureIp.length;whereCount++){

					var tempValue=this.SecureIp[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.SecureIp[whereCount]);

					}

				}
				}else{

				whereArray=this.SecureIp;
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
						newChild= instanciateObject("xdat:element_access_secure_ip");//omUtils.js
					}
					this.addSecureIp(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="permissions/allow_set"){
				this.Permissions_allowSet=value;
			} else 
			if(xmlPath.startsWith("permissions/allow_set")){
				xmlPath=xmlPath.substring(21);
				if(xmlPath=="")return this.Permissions_allowSet ;
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

				for(var whereCount=0;whereCount<this.Permissions_allowSet.length;whereCount++){

					var tempValue=this.Permissions_allowSet[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Permissions_allowSet[whereCount]);

					}

				}
				}else{

				whereArray=this.Permissions_allowSet;
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
						newChild= instanciateObject("xdat:field_mapping_set");//omUtils.js
					}
					this.addPermissions_allowSet(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="element_name"){
				this.ElementName=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_element_access_id"){
				this.XdatElementAccessId=value;
			} else 
			if(xmlPath=="xdat_user_xdat_user_id"){
				this.xdat_user_xdat_user_id_fk=value;
			} else 
			if(xmlPath=="xdat_userGroup_xdat_usergroup_id"){
				this.xdat_userGroup_xdat_usergroup_id_fk=value;
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
		if (xmlPath=="secure_ip"){
			this.addSecureIp(v);
		}else if (xmlPath=="permissions/allow_set"){
			this.addPermissions_allowSet(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="secure_ip"){
			return "http://nrg.wustl.edu/security:element_access_secure_ip";
		}else if (xmlPath=="permissions/allow_set"){
			return "http://nrg.wustl.edu/security:field_mapping_set";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="secondary_password"){
			return "field_data";
		}else if (xmlPath=="secondary_password/encrypt"){
			return "field_data";
		}else if (xmlPath=="secure_ip"){
			return "field_inline_repeater";
		}else if (xmlPath=="permissions/allow_set"){
			return "field_multi_reference";
		}else if (xmlPath=="element_name"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:element_access";
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
		xmlTxt+="\n</xdat:element_access>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatElementAccessId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_element_access_id=\"" + this.XdatElementAccessId + "\"";
			}
			if(this.xdat_user_xdat_user_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_user_xdat_user_id=\"" + this.xdat_user_xdat_user_id_fk + "\"";
			}
			if(this.xdat_userGroup_xdat_usergroup_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_userGroup_xdat_usergroup_id=\"" + this.xdat_userGroup_xdat_usergroup_id_fk + "\"";
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

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		var SecondaryPasswordATT = ""
		if (this.SecondaryPassword_encrypt!=null)
			SecondaryPasswordATT+=" encrypt=\"" + this.SecondaryPassword_encrypt + "\"";
		if (this.SecondaryPassword!=null){
			xmlTxt+="\n<xdat:secondary_password";
			xmlTxt+=SecondaryPasswordATT;
			xmlTxt+=">";
			xmlTxt+=this.SecondaryPassword.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:secondary_password>";
		}
		else if(SecondaryPasswordATT!=""){
			xmlTxt+="\n<xdat:secondary_password";
			xmlTxt+=SecondaryPasswordATT;
			xmlTxt+="/>";
		}

		for(var SecureIpCOUNT=0;SecureIpCOUNT<this.SecureIp.length;SecureIpCOUNT++){
			xmlTxt+=this.SecureIp[SecureIpCOUNT].getXMLBody(preventComments);
		}
			var child0=0;
			var att0=0;
			child0+=this.Permissions_allowSet.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xdat:permissions";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Permissions_allowSetCOUNT=0;Permissions_allowSetCOUNT<this.Permissions_allowSet.length;Permissions_allowSetCOUNT++){
			xmlTxt +="\n<xdat:allow_set";
			xmlTxt +=this.Permissions_allowSet[Permissions_allowSetCOUNT].getXMLAtts();
			if(this.Permissions_allowSet[Permissions_allowSetCOUNT].xsiType!="xdat:field_mapping_set"){
				xmlTxt+=" xsi:type=\"" + this.Permissions_allowSet[Permissions_allowSetCOUNT].xsiType + "\"";
			}
			if (this.Permissions_allowSet[Permissions_allowSetCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Permissions_allowSet[Permissions_allowSetCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:allow_set>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:permissions>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatElementAccessId!=null) return true;
			if (this.xdat_user_xdat_user_id_fk!=null) return true;
			if (this.xdat_userGroup_xdat_usergroup_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.SecondaryPassword_encrypt!=null)
			return true;
		if (this.SecondaryPassword!=null) return true;
		if(this.SecureIp.length>0) return true;
			if(this.Permissions_allowSet.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
