/*
 * web: wrk_xnatExecutionEnvironment.js
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

function wrk_xnatExecutionEnvironment(){
this.xsiType="wrk:xnatExecutionEnvironment";

	this.getSchemaElementName=function(){
		return "xnatExecutionEnvironment";
	}

	this.getFullSchemaElementName=function(){
		return "wrk:xnatExecutionEnvironment";
	}
this.extension=dynamicJSLoad('wrk_abstractExecutionEnvironment','generated/wrk_abstractExecutionEnvironment.js');

	this.Pipeline=null;


	function getPipeline() {
		return this.Pipeline;
	}
	this.getPipeline=getPipeline;


	function setPipeline(v){
		this.Pipeline=v;
	}
	this.setPipeline=setPipeline;

	this.Xnatuser=null;


	function getXnatuser() {
		return this.Xnatuser;
	}
	this.getXnatuser=getXnatuser;


	function setXnatuser(v){
		this.Xnatuser=v;
	}
	this.setXnatuser=setXnatuser;

	this.Host=null;


	function getHost() {
		return this.Host;
	}
	this.getHost=getHost;


	function setHost(v){
		this.Host=v;
	}
	this.setHost=setHost;

	this.Startat=null;


	function getStartat() {
		return this.Startat;
	}
	this.getStartat=getStartat;


	function setStartat(v){
		this.Startat=v;
	}
	this.setStartat=setStartat;
	this.Parameters_parameter =new Array();

	function getParameters_parameter() {
		return this.Parameters_parameter;
	}
	this.getParameters_parameter=getParameters_parameter;


	function addParameters_parameter(v){
		this.Parameters_parameter.push(v);
	}
	this.addParameters_parameter=addParameters_parameter;
	this.Notify =new Array();

	function getNotify() {
		return this.Notify;
	}
	this.getNotify=getNotify;


	function addNotify(v){
		this.Notify.push(v);
	}
	this.addNotify=addNotify;

	this.Datatype=null;


	function getDatatype() {
		return this.Datatype;
	}
	this.getDatatype=getDatatype;


	function setDatatype(v){
		this.Datatype=v;
	}
	this.setDatatype=setDatatype;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.Supressnotification=null;


	function getSupressnotification() {
		return this.Supressnotification;
	}
	this.getSupressnotification=getSupressnotification;


	function setSupressnotification(v){
		this.Supressnotification=v;
	}
	this.setSupressnotification=setSupressnotification;


	this.isSupressnotification=function(defaultValue) {
		if(this.Supressnotification==null)return defaultValue;
		if(this.Supressnotification=="1" || this.Supressnotification==true)return true;
		return false;
	}

	this.Log=null;


	function getLog() {
		return this.Log;
	}
	this.getLog=getLog;


	function setLog(v){
		this.Log=v;
	}
	this.setLog=setLog;

	this.Catalogpath=null;


	function getCatalogpath() {
		return this.Catalogpath;
	}
	this.getCatalogpath=getCatalogpath;


	function setCatalogpath(v){
		this.Catalogpath=v;
	}
	this.setCatalogpath=setCatalogpath;

	this.Parameterfile_xml=null;


	function getParameterfile_xml() {
		return this.Parameterfile_xml;
	}
	this.getParameterfile_xml=getParameterfile_xml;


	function setParameterfile_xml(v){
		this.Parameterfile_xml=v;
	}
	this.setParameterfile_xml=setParameterfile_xml;

	this.Parameterfile_path=null;


	function getParameterfile_path() {
		return this.Parameterfile_path;
	}
	this.getParameterfile_path=getParameterfile_path;


	function setParameterfile_path(v){
		this.Parameterfile_path=v;
	}
	this.setParameterfile_path=setParameterfile_path;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractExecutionEnvironment"){
				return this.Abstractexecutionenvironment ;
			} else 
			if(xmlPath.startsWith("abstractExecutionEnvironment")){
				xmlPath=xmlPath.substring(28);
				if(xmlPath=="")return this.Abstractexecutionenvironment ;
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
				if(this.Abstractexecutionenvironment!=undefined)return this.Abstractexecutionenvironment.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="pipeline"){
				return this.Pipeline ;
			} else 
			if(xmlPath=="xnatuser"){
				return this.Xnatuser ;
			} else 
			if(xmlPath=="host"){
				return this.Host ;
			} else 
			if(xmlPath=="startAt"){
				return this.Startat ;
			} else 
			if(xmlPath=="parameters/parameter"){
				return this.Parameters_parameter ;
			} else 
			if(xmlPath.startsWith("parameters/parameter")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Parameters_parameter ;
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

				for(var whereCount=0;whereCount<this.Parameters_parameter.length;whereCount++){

					var tempValue=this.Parameters_parameter[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_parameter[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_parameter;
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
			if(xmlPath=="notify"){
				return this.Notify ;
			} else 
			if(xmlPath.startsWith("notify")){
				xmlPath=xmlPath.substring(6);
				if(xmlPath=="")return this.Notify ;
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

				for(var whereCount=0;whereCount<this.Notify.length;whereCount++){

					var tempValue=this.Notify[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Notify[whereCount]);

					}

				}
				}else{

				whereArray=this.Notify;
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
			if(xmlPath=="dataType"){
				return this.Datatype ;
			} else 
			if(xmlPath=="id"){
				return this.Id ;
			} else 
			if(xmlPath=="supressNotification"){
				return this.Supressnotification ;
			} else 
			if(xmlPath=="log"){
				return this.Log ;
			} else 
			if(xmlPath=="catalogPath"){
				return this.Catalogpath ;
			} else 
			if(xmlPath=="parameterFile/xml"){
				return this.Parameterfile_xml ;
			} else 
			if(xmlPath=="parameterFile/path"){
				return this.Parameterfile_path ;
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
			if(xmlPath=="abstractExecutionEnvironment"){
				this.Abstractexecutionenvironment=value;
			} else 
			if(xmlPath.startsWith("abstractExecutionEnvironment")){
				xmlPath=xmlPath.substring(28);
				if(xmlPath=="")return this.Abstractexecutionenvironment ;
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
				if(this.Abstractexecutionenvironment!=undefined){
					this.Abstractexecutionenvironment.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Abstractexecutionenvironment= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Abstractexecutionenvironment= instanciateObject("wrk:abstractExecutionEnvironment");//omUtils.js
						}
						if(options && options.where)this.Abstractexecutionenvironment.setProperty(options.where.field,options.where.value);
						this.Abstractexecutionenvironment.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="pipeline"){
				this.Pipeline=value;
			} else 
			if(xmlPath=="xnatuser"){
				this.Xnatuser=value;
			} else 
			if(xmlPath=="host"){
				this.Host=value;
			} else 
			if(xmlPath=="startAt"){
				this.Startat=value;
			} else 
			if(xmlPath=="parameters/parameter"){
				this.Parameters_parameter=value;
			} else 
			if(xmlPath.startsWith("parameters/parameter")){
				xmlPath=xmlPath.substring(20);
				if(xmlPath=="")return this.Parameters_parameter ;
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

				for(var whereCount=0;whereCount<this.Parameters_parameter.length;whereCount++){

					var tempValue=this.Parameters_parameter[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_parameter[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_parameter;
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
						newChild= instanciateObject("wrk:xnatExecutionEnvironment_parameter");//omUtils.js
					}
					this.addParameters_parameter(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="notify"){
				this.Notify=value;
			} else 
			if(xmlPath.startsWith("notify")){
				xmlPath=xmlPath.substring(6);
				if(xmlPath=="")return this.Notify ;
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

				for(var whereCount=0;whereCount<this.Notify.length;whereCount++){

					var tempValue=this.Notify[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Notify[whereCount]);

					}

				}
				}else{

				whereArray=this.Notify;
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
						newChild= instanciateObject("wrk:xnatExecutionEnvironment_notify");//omUtils.js
					}
					this.addNotify(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="dataType"){
				this.Datatype=value;
			} else 
			if(xmlPath=="id"){
				this.Id=value;
			} else 
			if(xmlPath=="supressNotification"){
				this.Supressnotification=value;
			} else 
			if(xmlPath=="log"){
				this.Log=value;
			} else 
			if(xmlPath=="catalogPath"){
				this.Catalogpath=value;
			} else 
			if(xmlPath=="parameterFile/xml"){
				this.Parameterfile_xml=value;
			} else 
			if(xmlPath=="parameterFile/path"){
				this.Parameterfile_path=value;
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
		if (xmlPath=="parameters/parameter"){
			this.addParameters_parameter(v);
		}else if (xmlPath=="notify"){
			this.addNotify(v);
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
		if (xmlPath=="parameters/parameter"){
			return "http://nrg.wustl.edu/workflow:xnatExecutionEnvironment_parameter";
		}else if (xmlPath=="notify"){
			return "http://nrg.wustl.edu/workflow:xnatExecutionEnvironment_notify";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="pipeline"){
			return "field_data";
		}else if (xmlPath=="xnatuser"){
			return "field_data";
		}else if (xmlPath=="host"){
			return "field_data";
		}else if (xmlPath=="startAt"){
			return "field_data";
		}else if (xmlPath=="parameters/parameter"){
			return "field_NO_CHILD";
		}else if (xmlPath=="notify"){
			return "field_inline_repeater";
		}else if (xmlPath=="dataType"){
			return "field_data";
		}else if (xmlPath=="id"){
			return "field_data";
		}else if (xmlPath=="supressNotification"){
			return "field_data";
		}else if (xmlPath=="log"){
			return "field_data";
		}else if (xmlPath=="catalogPath"){
			return "field_data";
		}else if (xmlPath=="parameterFile/xml"){
			return "field_LONG_DATA";
		}else if (xmlPath=="parameterFile/path"){
			return "field_LONG_DATA";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<wrk:xnatExecutionEnvironment";
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
		xmlTxt+="\n</wrk:xnatExecutionEnvironment>";
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
		if (this.Pipeline!=null){
			xmlTxt+="\n<wrk:pipeline";
			xmlTxt+=">";
			xmlTxt+=this.Pipeline.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:pipeline>";
		}
		if (this.Xnatuser!=null){
			xmlTxt+="\n<wrk:xnatuser";
			xmlTxt+=">";
			xmlTxt+=this.Xnatuser.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:xnatuser>";
		}
		if (this.Host!=null){
			xmlTxt+="\n<wrk:host";
			xmlTxt+=">";
			xmlTxt+=this.Host.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:host>";
		}
		if (this.Startat!=null){
			xmlTxt+="\n<wrk:startAt";
			xmlTxt+=">";
			xmlTxt+=this.Startat.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:startAt>";
		}
			var child0=0;
			var att0=0;
			child0+=this.Parameters_parameter.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<wrk:parameters";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Parameters_parameterCOUNT=0;Parameters_parameterCOUNT<this.Parameters_parameter.length;Parameters_parameterCOUNT++){
			xmlTxt +="\n<wrk:parameter";
			xmlTxt +=this.Parameters_parameter[Parameters_parameterCOUNT].getXMLAtts();
			if(this.Parameters_parameter[Parameters_parameterCOUNT].xsiType!="wrk:xnatExecutionEnvironment_parameter"){
				xmlTxt+=" xsi:type=\"" + this.Parameters_parameter[Parameters_parameterCOUNT].xsiType + "\"";
			}
			if (this.Parameters_parameter[Parameters_parameterCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Parameters_parameter[Parameters_parameterCOUNT].getXMLBody(preventComments);
					xmlTxt+="</wrk:parameter>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</wrk:parameters>";
			}
			}

		for(var NotifyCOUNT=0;NotifyCOUNT<this.Notify.length;NotifyCOUNT++){
			xmlTxt+=this.Notify[NotifyCOUNT].getXMLBody(preventComments);
		}
		if (this.Datatype!=null){
			xmlTxt+="\n<wrk:dataType";
			xmlTxt+=">";
			xmlTxt+=this.Datatype.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:dataType>";
		}
		if (this.Id!=null){
			xmlTxt+="\n<wrk:id";
			xmlTxt+=">";
			xmlTxt+=this.Id.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:id>";
		}
		if (this.Supressnotification!=null){
			xmlTxt+="\n<wrk:supressNotification";
			xmlTxt+=">";
			xmlTxt+=this.Supressnotification;
			xmlTxt+="</wrk:supressNotification>";
		}
		else{
			xmlTxt+="\n<wrk:supressNotification";
			xmlTxt+="/>";
		}

		if (this.Log!=null){
			xmlTxt+="\n<wrk:log";
			xmlTxt+=">";
			xmlTxt+=this.Log.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:log>";
		}
		if (this.Catalogpath!=null){
			xmlTxt+="\n<wrk:catalogPath";
			xmlTxt+=">";
			xmlTxt+=this.Catalogpath.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:catalogPath>";
		}
		var ParameterfileATT = ""
		if (this.Parameterfile_path!=null)
			ParameterfileATT+=" path=\"" + this.Parameterfile_path.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
			var child1=0;
			var att1=0;
			if(this.Parameterfile_xml!=null)
			child1++;
			if(this.Parameterfile_path!=null)
			att1++;
			if(child1>0 || att1>0){
				xmlTxt+="\n<wrk:parameterFile";
				xmlTxt+=ParameterfileATT;
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Parameterfile_xml!=null){
			xmlTxt+="\n<wrk:xml";
			xmlTxt+=">";
			xmlTxt+=this.Parameterfile_xml.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</wrk:xml>";
		}
				xmlTxt+="\n</wrk:parameterFile>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Pipeline!=null) return true;
		if (this.Xnatuser!=null) return true;
		if (this.Host!=null) return true;
		if (this.Startat!=null) return true;
			if(this.Parameters_parameter.length>0)return true;
		if(this.Notify.length>0) return true;
		if (this.Datatype!=null) return true;
		if (this.Id!=null) return true;
		if (this.Supressnotification!=null) return true;
		return true;//REQUIRED supressNotification
	}
}
