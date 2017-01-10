/*
 * web: val_protocolData.js
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

function val_protocolData(){
this.xsiType="val:protocolData";

	this.getSchemaElementName=function(){
		return "protocolData";
	}

	this.getFullSchemaElementName=function(){
		return "val:protocolData";
	}
this.extension=dynamicJSLoad('xnat_imageAssessorData','generated/xnat_imageAssessorData.js');
	this.Check_conditions_condition =new Array();

	function getCheck_conditions_condition() {
		return this.Check_conditions_condition;
	}
	this.getCheck_conditions_condition=getCheck_conditions_condition;


	function addCheck_conditions_condition(v){
		this.Check_conditions_condition.push(v);
	}
	this.addCheck_conditions_condition=addCheck_conditions_condition;
	this.Check_comments_comment =new Array();

	function getCheck_comments_comment() {
		return this.Check_comments_comment;
	}
	this.getCheck_comments_comment=getCheck_comments_comment;


	function addCheck_comments_comment(v){
		this.Check_comments_comment.push(v);
	}
	this.addCheck_comments_comment=addCheck_comments_comment;
	this.Check_additionalval =null;
	function getCheck_additionalval() {
		return this.Check_additionalval;
	}
	this.getCheck_additionalval=getCheck_additionalval;


	function setCheck_additionalval(v){
		this.Check_additionalval =v;
	}
	this.setCheck_additionalval=setCheck_additionalval;

	this.Check_additionalval_CheckAdditionalvalValAdditionalvalId=null;


	function getCheck_additionalval_CheckAdditionalvalValAdditionalvalId(){
		return this.Check_additionalval_CheckAdditionalvalValAdditionalvalId;
	}
	this.getCheck_additionalval_CheckAdditionalvalValAdditionalvalId=getCheck_additionalval_CheckAdditionalvalValAdditionalvalId;


	function setCheck_additionalval_CheckAdditionalvalValAdditionalvalId(v){
		this.Check_additionalval_CheckAdditionalvalValAdditionalvalId=v;
	}
	this.setCheck_additionalval_CheckAdditionalvalValAdditionalvalId=setCheck_additionalval_CheckAdditionalvalValAdditionalvalId;

	this.Check_status=null;


	function getCheck_status() {
		return this.Check_status;
	}
	this.getCheck_status=getCheck_status;


	function setCheck_status(v){
		this.Check_status=v;
	}
	this.setCheck_status=setCheck_status;
	this.Scans_scanCheck =new Array();

	function getScans_scanCheck() {
		return this.Scans_scanCheck;
	}
	this.getScans_scanCheck=getScans_scanCheck;


	function addScans_scanCheck(v){
		this.Scans_scanCheck.push(v);
	}
	this.addScans_scanCheck=addScans_scanCheck;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageAssessorData"){
				return this.Imageassessordata ;
			} else 
			if(xmlPath.startsWith("imageAssessorData")){
				xmlPath=xmlPath.substring(17);
				if(xmlPath=="")return this.Imageassessordata ;
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
				if(this.Imageassessordata!=undefined)return this.Imageassessordata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="check/conditions/condition"){
				return this.Check_conditions_condition ;
			} else 
			if(xmlPath.startsWith("check/conditions/condition")){
				xmlPath=xmlPath.substring(26);
				if(xmlPath=="")return this.Check_conditions_condition ;
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

				for(var whereCount=0;whereCount<this.Check_conditions_condition.length;whereCount++){

					var tempValue=this.Check_conditions_condition[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Check_conditions_condition[whereCount]);

					}

				}
				}else{

				whereArray=this.Check_conditions_condition;
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
			if(xmlPath=="check/comments/comment"){
				return this.Check_comments_comment ;
			} else 
			if(xmlPath.startsWith("check/comments/comment")){
				xmlPath=xmlPath.substring(22);
				if(xmlPath=="")return this.Check_comments_comment ;
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

				for(var whereCount=0;whereCount<this.Check_comments_comment.length;whereCount++){

					var tempValue=this.Check_comments_comment[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Check_comments_comment[whereCount]);

					}

				}
				}else{

				whereArray=this.Check_comments_comment;
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
			if(xmlPath=="check/additionalVal"){
				return this.Check_additionalval ;
			} else 
			if(xmlPath.startsWith("check/additionalVal")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Check_additionalval ;
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
				if(this.Check_additionalval!=undefined)return this.Check_additionalval.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="check/status"){
				return this.Check_status ;
			} else 
			if(xmlPath=="scans/scan_check"){
				return this.Scans_scanCheck ;
			} else 
			if(xmlPath.startsWith("scans/scan_check")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Scans_scanCheck ;
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

				for(var whereCount=0;whereCount<this.Scans_scanCheck.length;whereCount++){

					var tempValue=this.Scans_scanCheck[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Scans_scanCheck[whereCount]);

					}

				}
				}else{

				whereArray=this.Scans_scanCheck;
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
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			{
				return this.extension.getProperty(xmlPath);
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageAssessorData"){
				this.Imageassessordata=value;
			} else 
			if(xmlPath.startsWith("imageAssessorData")){
				xmlPath=xmlPath.substring(17);
				if(xmlPath=="")return this.Imageassessordata ;
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
				if(this.Imageassessordata!=undefined){
					this.Imageassessordata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Imageassessordata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Imageassessordata= instanciateObject("xnat:imageAssessorData");//omUtils.js
						}
						if(options && options.where)this.Imageassessordata.setProperty(options.where.field,options.where.value);
						this.Imageassessordata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="check/conditions/condition"){
				this.Check_conditions_condition=value;
			} else 
			if(xmlPath.startsWith("check/conditions/condition")){
				xmlPath=xmlPath.substring(26);
				if(xmlPath=="")return this.Check_conditions_condition ;
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

				for(var whereCount=0;whereCount<this.Check_conditions_condition.length;whereCount++){

					var tempValue=this.Check_conditions_condition[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Check_conditions_condition[whereCount]);

					}

				}
				}else{

				whereArray=this.Check_conditions_condition;
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
						newChild= instanciateObject("val:protocolData_condition");//omUtils.js
					}
					this.addCheck_conditions_condition(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="check/comments/comment"){
				this.Check_comments_comment=value;
			} else 
			if(xmlPath.startsWith("check/comments/comment")){
				xmlPath=xmlPath.substring(22);
				if(xmlPath=="")return this.Check_comments_comment ;
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

				for(var whereCount=0;whereCount<this.Check_comments_comment.length;whereCount++){

					var tempValue=this.Check_comments_comment[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Check_comments_comment[whereCount]);

					}

				}
				}else{

				whereArray=this.Check_comments_comment;
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
						newChild= instanciateObject("val:protocolData_comment");//omUtils.js
					}
					this.addCheck_comments_comment(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="check/additionalVal"){
				this.Check_additionalval=value;
			} else 
			if(xmlPath.startsWith("check/additionalVal")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Check_additionalval ;
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
				if(this.Check_additionalval!=undefined){
					this.Check_additionalval.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Check_additionalval= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Check_additionalval= instanciateObject("val:additionalVal");//omUtils.js
						}
						if(options && options.where)this.Check_additionalval.setProperty(options.where.field,options.where.value);
						this.Check_additionalval.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="check/status"){
				this.Check_status=value;
			} else 
			if(xmlPath=="scans/scan_check"){
				this.Scans_scanCheck=value;
			} else 
			if(xmlPath.startsWith("scans/scan_check")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Scans_scanCheck ;
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

				for(var whereCount=0;whereCount<this.Scans_scanCheck.length;whereCount++){

					var tempValue=this.Scans_scanCheck[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Scans_scanCheck[whereCount]);

					}

				}
				}else{

				whereArray=this.Scans_scanCheck;
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
						newChild= instanciateObject("val:protocolData_scan_check");//omUtils.js
					}
					this.addScans_scanCheck(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			{
				return this.extension.setProperty(xmlPath,value);
			}
	}

	/**
	 * Sets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.setReferenceField=function(xmlPath,v) {
		if (xmlPath=="check/conditions/condition"){
			this.addCheck_conditions_condition(v);
		}else if (xmlPath=="check/comments/comment"){
			this.addCheck_comments_comment(v);
		}else if (xmlPath=="check/additionalVal"){
			this.setCheck_additionalval(v);
		}else if (xmlPath=="scans/scan_check"){
			this.addScans_scanCheck(v);
		}
		else{
			this.extension.setReferenceField(xmlPath,v);
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="check/conditions/condition"){
			return "http://nrg.wustl.edu/val:protocolData_condition";
		}else if (xmlPath=="check/comments/comment"){
			return "http://nrg.wustl.edu/val:protocolData_comment";
		}else if (xmlPath=="check/additionalVal"){
			return "http://nrg.wustl.edu/val:additionalVal";
		}else if (xmlPath=="scans/scan_check"){
			return "http://nrg.wustl.edu/val:protocolData_scan_check";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="check/conditions/condition"){
			return "field_multi_reference";
		}else if (xmlPath=="check/comments/comment"){
			return "field_NO_CHILD";
		}else if (xmlPath=="check/additionalVal"){
			return "field_single_reference";
		}else if (xmlPath=="check/status"){
			return "field_data";
		}else if (xmlPath=="scans/scan_check"){
			return "field_multi_reference";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<val:ProtocolVal";
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
		xmlTxt+="\n</val:ProtocolVal>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = this.extension.getXMLAtts();
		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		xmlTxt+=this.extension.getXMLBody(preventComments);
		var CheckATT = ""
		if (this.Check_status!=null)
			CheckATT+=" status=\"" + this.Check_status.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		else CheckATT+=" status="+ this.Check_status +"\"";//REQUIRED FIELD

			var child0=0;
			var att0=0;
			if(this.Check_additionalval!=null)
			child0++;
			child0+=this.Check_conditions_condition.length;
			child0+=this.Check_comments_comment.length;
			if(this.Check_status!=null)
			att0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<val:check";
				xmlTxt+=CheckATT;
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
			var child1=0;
			var att1=0;
			child1+=this.Check_conditions_condition.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<val:conditions";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Check_conditions_conditionCOUNT=0;Check_conditions_conditionCOUNT<this.Check_conditions_condition.length;Check_conditions_conditionCOUNT++){
			xmlTxt +="\n<val:condition";
			xmlTxt +=this.Check_conditions_condition[Check_conditions_conditionCOUNT].getXMLAtts();
			if(this.Check_conditions_condition[Check_conditions_conditionCOUNT].xsiType!="val:protocolData_condition"){
				xmlTxt+=" xsi:type=\"" + this.Check_conditions_condition[Check_conditions_conditionCOUNT].xsiType + "\"";
			}
			if (this.Check_conditions_condition[Check_conditions_conditionCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Check_conditions_condition[Check_conditions_conditionCOUNT].getXMLBody(preventComments);
					xmlTxt+="</val:condition>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</val:conditions>";
			}
			}

			var child2=0;
			var att2=0;
			child2+=this.Check_comments_comment.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<val:comments";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Check_comments_commentCOUNT=0;Check_comments_commentCOUNT<this.Check_comments_comment.length;Check_comments_commentCOUNT++){
			xmlTxt +="\n<val:comment";
			xmlTxt +=this.Check_comments_comment[Check_comments_commentCOUNT].getXMLAtts();
			if(this.Check_comments_comment[Check_comments_commentCOUNT].xsiType!="val:protocolData_comment"){
				xmlTxt+=" xsi:type=\"" + this.Check_comments_comment[Check_comments_commentCOUNT].xsiType + "\"";
			}
			if (this.Check_comments_comment[Check_comments_commentCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Check_comments_comment[Check_comments_commentCOUNT].getXMLBody(preventComments);
					xmlTxt+="</val:comment>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</val:comments>";
			}
			}

		if (this.Check_additionalval!=null){
			xmlTxt+="\n<val:additionalVal";
			xmlTxt+=this.Check_additionalval.getXMLAtts();
			if(this.Check_additionalval.xsiType!="val:additionalVal"){
				xmlTxt+=" xsi:type=\"" + this.Check_additionalval.xsiType + "\"";
			}
			if (this.Check_additionalval.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Check_additionalval.getXMLBody(preventComments);
				xmlTxt+="</val:additionalVal>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

				xmlTxt+="\n</val:check>";
			}
			}

			var child3=0;
			var att3=0;
			child3+=this.Scans_scanCheck.length;
			if(child3>0 || att3>0){
				xmlTxt+="\n<val:scans";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Scans_scanCheckCOUNT=0;Scans_scanCheckCOUNT<this.Scans_scanCheck.length;Scans_scanCheckCOUNT++){
			xmlTxt +="\n<val:scan_check";
			xmlTxt +=this.Scans_scanCheck[Scans_scanCheckCOUNT].getXMLAtts();
			if(this.Scans_scanCheck[Scans_scanCheckCOUNT].xsiType!="val:protocolData_scan_check"){
				xmlTxt+=" xsi:type=\"" + this.Scans_scanCheck[Scans_scanCheckCOUNT].xsiType + "\"";
			}
			if (this.Scans_scanCheck[Scans_scanCheckCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Scans_scanCheck[Scans_scanCheckCOUNT].getXMLBody(preventComments);
					xmlTxt+="</val:scan_check>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</val:scans>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Check_status!=null)
			return true;
		return true;//REQUIRED check/status
	}
}
