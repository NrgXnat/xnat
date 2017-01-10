/*
 * web: val_protocolData_scan_check.js
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

function val_protocolData_scan_check(){
this.xsiType="val:protocolData_scan_check";

	this.getSchemaElementName=function(){
		return "protocolData_scan_check";
	}

	this.getFullSchemaElementName=function(){
		return "val:protocolData_scan_check";
	}
	this.Conditions_condition =new Array();

	function getConditions_condition() {
		return this.Conditions_condition;
	}
	this.getConditions_condition=getConditions_condition;


	function addConditions_condition(v){
		this.Conditions_condition.push(v);
	}
	this.addConditions_condition=addConditions_condition;
	this.Comments_comment =new Array();

	function getComments_comment() {
		return this.Comments_comment;
	}
	this.getComments_comment=getComments_comment;


	function addComments_comment(v){
		this.Comments_comment.push(v);
	}
	this.addComments_comment=addComments_comment;
	this.Additionalval =null;
	function getAdditionalval() {
		return this.Additionalval;
	}
	this.getAdditionalval=getAdditionalval;


	function setAdditionalval(v){
		this.Additionalval =v;
	}
	this.setAdditionalval=setAdditionalval;

	this.Additionalval_AdditionalvalValAdditionalvalId=null;


	function getAdditionalval_AdditionalvalValAdditionalvalId(){
		return this.Additionalval_AdditionalvalValAdditionalvalId;
	}
	this.getAdditionalval_AdditionalvalValAdditionalvalId=getAdditionalval_AdditionalvalValAdditionalvalId;


	function setAdditionalval_AdditionalvalValAdditionalvalId(v){
		this.Additionalval_AdditionalvalValAdditionalvalId=v;
	}
	this.setAdditionalval_AdditionalvalValAdditionalvalId=setAdditionalval_AdditionalvalValAdditionalvalId;

	this.ScanId=null;


	function getScanId() {
		return this.ScanId;
	}
	this.getScanId=getScanId;


	function setScanId(v){
		this.ScanId=v;
	}
	this.setScanId=setScanId;

	this.Status=null;


	function getStatus() {
		return this.Status;
	}
	this.getStatus=getStatus;


	function setStatus(v){
		this.Status=v;
	}
	this.setStatus=setStatus;

	this.ValProtocoldataScanCheckId=null;


	function getValProtocoldataScanCheckId() {
		return this.ValProtocoldataScanCheckId;
	}
	this.getValProtocoldataScanCheckId=getValProtocoldataScanCheckId;


	function setValProtocoldataScanCheckId(v){
		this.ValProtocoldataScanCheckId=v;
	}
	this.setValProtocoldataScanCheckId=setValProtocoldataScanCheckId;

	this.scans_scan_check_val_protocolDa_id_fk=null;


	this.getscans_scan_check_val_protocolDa_id=function() {
		return this.scans_scan_check_val_protocolDa_id_fk;
	}


	this.setscans_scan_check_val_protocolDa_id=function(v){
		this.scans_scan_check_val_protocolDa_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="conditions/condition"){
				return this.Conditions_condition ;
			} else 
			if(xmlPath.startsWith("conditions/condition")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Conditions_condition ;
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

				for(var whereCount=0;whereCount<this.Conditions_condition.length;whereCount++){

					var tempValue=this.Conditions_condition[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Conditions_condition[whereCount]);

					}

				}
				}else{

				whereArray=this.Conditions_condition;
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
			if(xmlPath=="comments/comment"){
				return this.Comments_comment ;
			} else 
			if(xmlPath.startsWith("comments/comment")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Comments_comment ;
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

				for(var whereCount=0;whereCount<this.Comments_comment.length;whereCount++){

					var tempValue=this.Comments_comment[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Comments_comment[whereCount]);

					}

				}
				}else{

				whereArray=this.Comments_comment;
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
			if(xmlPath=="additionalVal"){
				return this.Additionalval ;
			} else 
			if(xmlPath.startsWith("additionalVal")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Additionalval ;
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
				if(this.Additionalval!=undefined)return this.Additionalval.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="SCAN_ID"){
				return this.ScanId ;
			} else 
			if(xmlPath=="status"){
				return this.Status ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="val_protocolData_scan_check_id"){
				return this.ValProtocoldataScanCheckId ;
			} else 
			if(xmlPath=="scans_scan_check_val_protocolDa_id"){
				return this.scans_scan_check_val_protocolDa_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="conditions/condition"){
				this.Conditions_condition=value;
			} else 
			if(xmlPath.startsWith("conditions/condition")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Conditions_condition ;
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

				for(var whereCount=0;whereCount<this.Conditions_condition.length;whereCount++){

					var tempValue=this.Conditions_condition[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Conditions_condition[whereCount]);

					}

				}
				}else{

				whereArray=this.Conditions_condition;
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
						newChild= instanciateObject("val:protocolData_scan_check_condition");//omUtils.js
					}
					this.addConditions_condition(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="comments/comment"){
				this.Comments_comment=value;
			} else 
			if(xmlPath.startsWith("comments/comment")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Comments_comment ;
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

				for(var whereCount=0;whereCount<this.Comments_comment.length;whereCount++){

					var tempValue=this.Comments_comment[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Comments_comment[whereCount]);

					}

				}
				}else{

				whereArray=this.Comments_comment;
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
						newChild= instanciateObject("val:protocolData_scan_check_comment");//omUtils.js
					}
					this.addComments_comment(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="additionalVal"){
				this.Additionalval=value;
			} else 
			if(xmlPath.startsWith("additionalVal")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Additionalval ;
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
				if(this.Additionalval!=undefined){
					this.Additionalval.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Additionalval= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Additionalval= instanciateObject("val:additionalVal");//omUtils.js
						}
						if(options && options.where)this.Additionalval.setProperty(options.where.field,options.where.value);
						this.Additionalval.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="SCAN_ID"){
				this.ScanId=value;
			} else 
			if(xmlPath=="status"){
				this.Status=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="val_protocolData_scan_check_id"){
				this.ValProtocoldataScanCheckId=value;
			} else 
			if(xmlPath=="scans_scan_check_val_protocolDa_id"){
				this.scans_scan_check_val_protocolDa_id_fk=value;
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
		if (xmlPath=="conditions/condition"){
			this.addConditions_condition(v);
		}else if (xmlPath=="comments/comment"){
			this.addComments_comment(v);
		}else if (xmlPath=="additionalVal"){
			this.setAdditionalval(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="conditions/condition"){
			return "http://nrg.wustl.edu/val:protocolData_scan_check_condition";
		}else if (xmlPath=="comments/comment"){
			return "http://nrg.wustl.edu/val:protocolData_scan_check_comment";
		}else if (xmlPath=="additionalVal"){
			return "http://nrg.wustl.edu/val:additionalVal";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="conditions/condition"){
			return "field_multi_reference";
		}else if (xmlPath=="comments/comment"){
			return "field_NO_CHILD";
		}else if (xmlPath=="additionalVal"){
			return "field_single_reference";
		}else if (xmlPath=="SCAN_ID"){
			return "field_data";
		}else if (xmlPath=="status"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<val:protocolData_scan_check";
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
		xmlTxt+="\n</val:protocolData_scan_check>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ValProtocoldataScanCheckId!=null){
				if(hiddenCount++>0)str+=",";
				str+="val_protocolData_scan_check_id=\"" + this.ValProtocoldataScanCheckId + "\"";
			}
			if(this.scans_scan_check_val_protocolDa_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="scans_scan_check_val_protocolDa_id=\"" + this.scans_scan_check_val_protocolDa_id_fk + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.ScanId!=null)
			attTxt+=" SCAN_ID=\"" +this.ScanId +"\"";
		else attTxt+=" SCAN_ID=\"\"";//REQUIRED FIELD

		if (this.Status!=null)
			attTxt+=" status=\"" +this.Status +"\"";
		else attTxt+=" status=\"\"";//REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
			var child0=0;
			var att0=0;
			child0+=this.Conditions_condition.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<val:conditions";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Conditions_conditionCOUNT=0;Conditions_conditionCOUNT<this.Conditions_condition.length;Conditions_conditionCOUNT++){
			xmlTxt +="\n<val:condition";
			xmlTxt +=this.Conditions_condition[Conditions_conditionCOUNT].getXMLAtts();
			if(this.Conditions_condition[Conditions_conditionCOUNT].xsiType!="val:protocolData_scan_check_condition"){
				xmlTxt+=" xsi:type=\"" + this.Conditions_condition[Conditions_conditionCOUNT].xsiType + "\"";
			}
			if (this.Conditions_condition[Conditions_conditionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Conditions_condition[Conditions_conditionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</val:condition>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</val:conditions>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.Comments_comment.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<val:comments";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Comments_commentCOUNT=0;Comments_commentCOUNT<this.Comments_comment.length;Comments_commentCOUNT++){
			xmlTxt +="\n<val:comment";
			xmlTxt +=this.Comments_comment[Comments_commentCOUNT].getXMLAtts();
			if(this.Comments_comment[Comments_commentCOUNT].xsiType!="val:protocolData_scan_check_comment"){
				xmlTxt+=" xsi:type=\"" + this.Comments_comment[Comments_commentCOUNT].xsiType + "\"";
			}
			if (this.Comments_comment[Comments_commentCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Comments_comment[Comments_commentCOUNT].getXMLBody(preventComments);
					xmlTxt+="</val:comment>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</val:comments>";
			}
			}

		if (this.Additionalval!=null){
			xmlTxt+="\n<val:additionalVal";
			xmlTxt+=this.Additionalval.getXMLAtts();
			if(this.Additionalval.xsiType!="val:additionalVal"){
				xmlTxt+=" xsi:type=\"" + this.Additionalval.xsiType + "\"";
			}
			if (this.Additionalval.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Additionalval.getXMLBody(preventComments);
				xmlTxt+="</val:additionalVal>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ValProtocoldataScanCheckId!=null) return true;
			if (this.scans_scan_check_val_protocolDa_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
			if(this.Conditions_condition.length>0)return true;
			if(this.Comments_comment.length>0)return true;
		if (this.Additionalval!=null){
			if (this.Additionalval.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

		if(this.hasXMLComments())return true;
		return false;
	}
}
