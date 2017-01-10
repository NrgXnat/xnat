/*
 * web: xdat_user.js
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

function xdat_user(){
this.xsiType="xdat:user";

	this.getSchemaElementName=function(){
		return "user";
	}

	this.getFullSchemaElementName=function(){
		return "xdat:user";
	}

	this.Login=null;


	function getLogin() {
		return this.Login;
	}
	this.getLogin=getLogin;


	function setLogin(v){
		this.Login=v;
	}
	this.setLogin=setLogin;

	this.Firstname=null;


	function getFirstname() {
		return this.Firstname;
	}
	this.getFirstname=getFirstname;


	function setFirstname(v){
		this.Firstname=v;
	}
	this.setFirstname=setFirstname;

	this.Lastname=null;


	function getLastname() {
		return this.Lastname;
	}
	this.getLastname=getLastname;


	function setLastname(v){
		this.Lastname=v;
	}
	this.setLastname=setLastname;

	this.Email=null;


	function getEmail() {
		return this.Email;
	}
	this.getEmail=getEmail;


	function setEmail(v){
		this.Email=v;
	}
	this.setEmail=setEmail;

	this.PrimaryPassword=null;


	function getPrimaryPassword() {
		return this.PrimaryPassword;
	}
	this.getPrimaryPassword=getPrimaryPassword;


	function setPrimaryPassword(v){
		this.PrimaryPassword=v;
	}
	this.setPrimaryPassword=setPrimaryPassword;

	this.PrimaryPassword_encrypt=null;


	function getPrimaryPassword_encrypt() {
		return this.PrimaryPassword_encrypt;
	}
	this.getPrimaryPassword_encrypt=getPrimaryPassword_encrypt;


	function setPrimaryPassword_encrypt(v){
		this.PrimaryPassword_encrypt=v;
	}
	this.setPrimaryPassword_encrypt=setPrimaryPassword_encrypt;


	this.isPrimaryPassword_encrypt=function(defaultValue) {
		if(this.PrimaryPassword_encrypt==null)return defaultValue;
		if(this.PrimaryPassword_encrypt=="1" || this.PrimaryPassword_encrypt==true)return true;
		return false;
	}
	this.ElementAccess =new Array();

	function getElementAccess() {
		return this.ElementAccess;
	}
	this.getElementAccess=getElementAccess;


	function addElementAccess(v){
		this.ElementAccess.push(v);
	}
	this.addElementAccess=addElementAccess;
	this.AssignedRoles_assignedRole =new Array();

	function getAssignedRoles_assignedRole() {
		return this.AssignedRoles_assignedRole;
	}
	this.getAssignedRoles_assignedRole=getAssignedRoles_assignedRole;


	function addAssignedRoles_assignedRole(v){
		this.AssignedRoles_assignedRole.push(v);
	}
	this.addAssignedRoles_assignedRole=addAssignedRoles_assignedRole;

	this.QuarantinePath=null;


	function getQuarantinePath() {
		return this.QuarantinePath;
	}
	this.getQuarantinePath=getQuarantinePath;


	function setQuarantinePath(v){
		this.QuarantinePath=v;
	}
	this.setQuarantinePath=setQuarantinePath;
	this.Groups_groupid =new Array();

	function getGroups_groupid() {
		return this.Groups_groupid;
	}
	this.getGroups_groupid=getGroups_groupid;


	function addGroups_groupid(v){
		this.Groups_groupid.push(v);
	}
	this.addGroups_groupid=addGroups_groupid;

	this.Enabled=null;


	function getEnabled() {
		return this.Enabled;
	}
	this.getEnabled=getEnabled;


	function setEnabled(v){
		this.Enabled=v;
	}
	this.setEnabled=setEnabled;


	this.isEnabled=function(defaultValue) {
		if(this.Enabled==null)return defaultValue;
		if(this.Enabled=="1" || this.Enabled==true)return true;
		return false;
	}

	this.Verified=null;


	function getVerified() {
		return this.Verified;
	}
	this.getVerified=getVerified;


	function setVerified(v){
		this.Verified=v;
	}
	this.setVerified=setVerified;


	this.isVerified=function(defaultValue) {
		if(this.Verified==null)return defaultValue;
		if(this.Verified=="1" || this.Verified==true)return true;
		return false;
	}

	this.Salt=null;


	function getSalt() {
		return this.Salt;
	}
	this.getSalt=getSalt;


	function setSalt(v){
		this.Salt=v;
	}
	this.setSalt=setSalt;

	this.XdatUserId=null;


	function getXdatUserId() {
		return this.XdatUserId;
	}
	this.getXdatUserId=getXdatUserId;


	function setXdatUserId(v){
		this.XdatUserId=v;
	}
	this.setXdatUserId=setXdatUserId;

	this.users_user_xdat_security_xdat_security_id_fk=null;


	this.getusers_user_xdat_security_xdat_security_id=function() {
		return this.users_user_xdat_security_xdat_security_id_fk;
	}


	this.setusers_user_xdat_security_xdat_security_id=function(v){
		this.users_user_xdat_security_xdat_security_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="login"){
				return this.Login ;
			} else 
			if(xmlPath=="firstname"){
				return this.Firstname ;
			} else 
			if(xmlPath=="lastname"){
				return this.Lastname ;
			} else 
			if(xmlPath=="email"){
				return this.Email ;
			} else 
			if(xmlPath=="primary_password"){
				return this.PrimaryPassword ;
			} else 
			if(xmlPath=="primary_password/encrypt"){
				return this.PrimaryPassword_encrypt ;
			} else 
			if(xmlPath=="element_access"){
				return this.ElementAccess ;
			} else 
			if(xmlPath.startsWith("element_access")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.ElementAccess ;
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

				for(var whereCount=0;whereCount<this.ElementAccess.length;whereCount++){

					var tempValue=this.ElementAccess[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ElementAccess[whereCount]);

					}

				}
				}else{

				whereArray=this.ElementAccess;
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
			if(xmlPath=="assigned_roles/assigned_role"){
				return this.AssignedRoles_assignedRole ;
			} else 
			if(xmlPath.startsWith("assigned_roles/assigned_role")){
				xmlPath=xmlPath.substring(28);
				if(xmlPath=="")return this.AssignedRoles_assignedRole ;
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

				for(var whereCount=0;whereCount<this.AssignedRoles_assignedRole.length;whereCount++){

					var tempValue=this.AssignedRoles_assignedRole[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.AssignedRoles_assignedRole[whereCount]);

					}

				}
				}else{

				whereArray=this.AssignedRoles_assignedRole;
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
			if(xmlPath=="quarantine_path"){
				return this.QuarantinePath ;
			} else 
			if(xmlPath=="groups/groupID"){
				return this.Groups_groupid ;
			} else 
			if(xmlPath.startsWith("groups/groupID")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Groups_groupid ;
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

				for(var whereCount=0;whereCount<this.Groups_groupid.length;whereCount++){

					var tempValue=this.Groups_groupid[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Groups_groupid[whereCount]);

					}

				}
				}else{

				whereArray=this.Groups_groupid;
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
			if(xmlPath=="enabled"){
				return this.Enabled ;
			} else 
			if(xmlPath=="verified"){
				return this.Verified ;
			} else 
			if(xmlPath=="salt"){
				return this.Salt ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xdat_user_id"){
				return this.XdatUserId ;
			} else 
			if(xmlPath=="users_user_xdat_security_xdat_security_id"){
				return this.users_user_xdat_security_xdat_security_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="login"){
				this.Login=value;
			} else 
			if(xmlPath=="firstname"){
				this.Firstname=value;
			} else 
			if(xmlPath=="lastname"){
				this.Lastname=value;
			} else 
			if(xmlPath=="email"){
				this.Email=value;
			} else 
			if(xmlPath=="primary_password"){
				this.PrimaryPassword=value;
			} else 
			if(xmlPath=="primary_password/encrypt"){
				this.PrimaryPassword_encrypt=value;
			} else 
			if(xmlPath=="element_access"){
				this.ElementAccess=value;
			} else 
			if(xmlPath.startsWith("element_access")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.ElementAccess ;
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

				for(var whereCount=0;whereCount<this.ElementAccess.length;whereCount++){

					var tempValue=this.ElementAccess[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.ElementAccess[whereCount]);

					}

				}
				}else{

				whereArray=this.ElementAccess;
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
						newChild= instanciateObject("xdat:element_access");//omUtils.js
					}
					this.addElementAccess(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="assigned_roles/assigned_role"){
				this.AssignedRoles_assignedRole=value;
			} else 
			if(xmlPath.startsWith("assigned_roles/assigned_role")){
				xmlPath=xmlPath.substring(28);
				if(xmlPath=="")return this.AssignedRoles_assignedRole ;
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

				for(var whereCount=0;whereCount<this.AssignedRoles_assignedRole.length;whereCount++){

					var tempValue=this.AssignedRoles_assignedRole[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.AssignedRoles_assignedRole[whereCount]);

					}

				}
				}else{

				whereArray=this.AssignedRoles_assignedRole;
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
					this.addAssignedRoles_assignedRole(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="quarantine_path"){
				this.QuarantinePath=value;
			} else 
			if(xmlPath=="groups/groupID"){
				this.Groups_groupid=value;
			} else 
			if(xmlPath.startsWith("groups/groupID")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Groups_groupid ;
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

				for(var whereCount=0;whereCount<this.Groups_groupid.length;whereCount++){

					var tempValue=this.Groups_groupid[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Groups_groupid[whereCount]);

					}

				}
				}else{

				whereArray=this.Groups_groupid;
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
						newChild= instanciateObject("xdat:user_groupID");//omUtils.js
					}
					this.addGroups_groupid(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="enabled"){
				this.Enabled=value;
			} else 
			if(xmlPath=="verified"){
				this.Verified=value;
			} else 
			if(xmlPath=="salt"){
				this.Salt=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xdat_user_id"){
				this.XdatUserId=value;
			} else 
			if(xmlPath=="users_user_xdat_security_xdat_security_id"){
				this.users_user_xdat_security_xdat_security_id_fk=value;
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
		if (xmlPath=="element_access"){
			this.addElementAccess(v);
		}else if (xmlPath=="assigned_roles/assigned_role"){
			this.addAssignedRoles_assignedRole(v);
		}else if (xmlPath=="groups/groupID"){
			this.addGroups_groupid(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="element_access"){
			return "http://nrg.wustl.edu/security:element_access";
		}else if (xmlPath=="assigned_roles/assigned_role"){
			return "http://nrg.wustl.edu/security:role_type";
		}else if (xmlPath=="groups/groupID"){
			return "http://nrg.wustl.edu/security:user_groupID";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="login"){
			return "field_data";
		}else if (xmlPath=="firstname"){
			return "field_data";
		}else if (xmlPath=="lastname"){
			return "field_data";
		}else if (xmlPath=="email"){
			return "field_data";
		}else if (xmlPath=="primary_password"){
			return "field_data";
		}else if (xmlPath=="primary_password/encrypt"){
			return "field_data";
		}else if (xmlPath=="element_access"){
			return "field_multi_reference";
		}else if (xmlPath=="assigned_roles/assigned_role"){
			return "field_multi_reference";
		}else if (xmlPath=="quarantine_path"){
			return "field_data";
		}else if (xmlPath=="groups/groupID"){
			return "field_inline_repeater";
		}else if (xmlPath=="enabled"){
			return "field_data";
		}else if (xmlPath=="verified"){
			return "field_data";
		}else if (xmlPath=="salt"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xdat:XDATUser";
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
		xmlTxt+="\n</xdat:XDATUser>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XdatUserId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xdat_user_id=\"" + this.XdatUserId + "\"";
			}
			if(this.users_user_xdat_security_xdat_security_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="users_user_xdat_security_xdat_security_id=\"" + this.users_user_xdat_security_xdat_security_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Enabled!=null)
			attTxt+=" enabled=\"" +this.Enabled +"\"";
		//NOT REQUIRED FIELD

		if (this.Verified!=null)
			attTxt+=" verified=\"" +this.Verified +"\"";
		//NOT REQUIRED FIELD

		if (this.Salt!=null)
			attTxt+=" salt=\"" +this.Salt +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.Login!=null){
			xmlTxt+="\n<xdat:login";
			xmlTxt+=">";
			xmlTxt+=this.Login.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:login>";
		}
		if (this.Firstname!=null){
			xmlTxt+="\n<xdat:firstname";
			xmlTxt+=">";
			xmlTxt+=this.Firstname.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:firstname>";
		}
		if (this.Lastname!=null){
			xmlTxt+="\n<xdat:lastname";
			xmlTxt+=">";
			xmlTxt+=this.Lastname.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:lastname>";
		}
		if (this.Email!=null){
			xmlTxt+="\n<xdat:email";
			xmlTxt+=">";
			xmlTxt+=this.Email.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:email>";
		}
		var PrimaryPasswordATT = ""
		if (this.PrimaryPassword_encrypt!=null)
			PrimaryPasswordATT+=" encrypt=\"" + this.PrimaryPassword_encrypt + "\"";
		if (this.PrimaryPassword!=null){
			xmlTxt+="\n<xdat:primary_password";
			xmlTxt+=PrimaryPasswordATT;
			xmlTxt+=">";
			xmlTxt+=this.PrimaryPassword.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:primary_password>";
		}
		else if(PrimaryPasswordATT!=""){
			xmlTxt+="\n<xdat:primary_password";
			xmlTxt+=PrimaryPasswordATT;
			xmlTxt+="/>";
		}

		for(var ElementAccessCOUNT=0;ElementAccessCOUNT<this.ElementAccess.length;ElementAccessCOUNT++){
			xmlTxt +="\n<xdat:element_access";
			xmlTxt +=this.ElementAccess[ElementAccessCOUNT].getXMLAtts();
			if(this.ElementAccess[ElementAccessCOUNT].xsiType!="xdat:element_access"){
				xmlTxt+=" xsi:type=\"" + this.ElementAccess[ElementAccessCOUNT].xsiType + "\"";
			}
			if (this.ElementAccess[ElementAccessCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.ElementAccess[ElementAccessCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:element_access>";
			}else {xmlTxt+="/>";}
		}
			var child0=0;
			var att0=0;
			child0+=this.AssignedRoles_assignedRole.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xdat:assigned_roles";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var AssignedRoles_assignedRoleCOUNT=0;AssignedRoles_assignedRoleCOUNT<this.AssignedRoles_assignedRole.length;AssignedRoles_assignedRoleCOUNT++){
			xmlTxt +="\n<xdat:assigned_role";
			xmlTxt +=this.AssignedRoles_assignedRole[AssignedRoles_assignedRoleCOUNT].getXMLAtts();
			if(this.AssignedRoles_assignedRole[AssignedRoles_assignedRoleCOUNT].xsiType!="xdat:role_type"){
				xmlTxt+=" xsi:type=\"" + this.AssignedRoles_assignedRole[AssignedRoles_assignedRoleCOUNT].xsiType + "\"";
			}
			if (this.AssignedRoles_assignedRole[AssignedRoles_assignedRoleCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.AssignedRoles_assignedRole[AssignedRoles_assignedRoleCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xdat:assigned_role>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xdat:assigned_roles>";
			}
			}

		if (this.QuarantinePath!=null){
			xmlTxt+="\n<xdat:quarantine_path";
			xmlTxt+=">";
			xmlTxt+=this.QuarantinePath.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xdat:quarantine_path>";
		}
			var child1=0;
			var att1=0;
			child1+=this.Groups_groupid.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xdat:groups";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Groups_groupidCOUNT=0;Groups_groupidCOUNT<this.Groups_groupid.length;Groups_groupidCOUNT++){
			xmlTxt+=this.Groups_groupid[Groups_groupidCOUNT].getXMLBody(preventComments);
		}
				xmlTxt+="\n</xdat:groups>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XdatUserId!=null) return true;
			if (this.users_user_xdat_security_xdat_security_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Login!=null) return true;
		if (this.Firstname!=null) return true;
		if (this.Lastname!=null) return true;
		if (this.Email!=null) return true;
		if (this.PrimaryPassword_encrypt!=null)
			return true;
		if (this.PrimaryPassword!=null) return true;
		if(this.ElementAccess.length>0) return true;
			if(this.AssignedRoles_assignedRole.length>0)return true;
		if (this.QuarantinePath!=null) return true;
			if(this.Groups_groupid.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
