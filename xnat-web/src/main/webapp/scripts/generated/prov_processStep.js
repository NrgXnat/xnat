/*
 * web: prov_processStep.js
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

function prov_processStep(){
this.xsiType="prov:processStep";

	this.getSchemaElementName=function(){
		return "processStep";
	}

	this.getFullSchemaElementName=function(){
		return "prov:processStep";
	}

	this.Program=null;


	function getProgram() {
		return this.Program;
	}
	this.getProgram=getProgram;


	function setProgram(v){
		this.Program=v;
	}
	this.setProgram=setProgram;

	this.Program_version=null;


	function getProgram_version() {
		return this.Program_version;
	}
	this.getProgram_version=getProgram_version;


	function setProgram_version(v){
		this.Program_version=v;
	}
	this.setProgram_version=setProgram_version;

	this.Program_arguments=null;


	function getProgram_arguments() {
		return this.Program_arguments;
	}
	this.getProgram_arguments=getProgram_arguments;


	function setProgram_arguments(v){
		this.Program_arguments=v;
	}
	this.setProgram_arguments=setProgram_arguments;

	this.Timestamp=null;


	function getTimestamp() {
		return this.Timestamp;
	}
	this.getTimestamp=getTimestamp;


	function setTimestamp(v){
		this.Timestamp=v;
	}
	this.setTimestamp=setTimestamp;

	this.Cvs=null;


	function getCvs() {
		return this.Cvs;
	}
	this.getCvs=getCvs;


	function setCvs(v){
		this.Cvs=v;
	}
	this.setCvs=setCvs;

	this.User=null;


	function getUser() {
		return this.User;
	}
	this.getUser=getUser;


	function setUser(v){
		this.User=v;
	}
	this.setUser=setUser;

	this.Machine=null;


	function getMachine() {
		return this.Machine;
	}
	this.getMachine=getMachine;


	function setMachine(v){
		this.Machine=v;
	}
	this.setMachine=setMachine;

	this.Platform=null;


	function getPlatform() {
		return this.Platform;
	}
	this.getPlatform=getPlatform;


	function setPlatform(v){
		this.Platform=v;
	}
	this.setPlatform=setPlatform;

	this.Platform_version=null;


	function getPlatform_version() {
		return this.Platform_version;
	}
	this.getPlatform_version=getPlatform_version;


	function setPlatform_version(v){
		this.Platform_version=v;
	}
	this.setPlatform_version=setPlatform_version;

	this.Compiler=null;


	function getCompiler() {
		return this.Compiler;
	}
	this.getCompiler=getCompiler;


	function setCompiler(v){
		this.Compiler=v;
	}
	this.setCompiler=setCompiler;

	this.Compiler_version=null;


	function getCompiler_version() {
		return this.Compiler_version;
	}
	this.getCompiler_version=getCompiler_version;


	function setCompiler_version(v){
		this.Compiler_version=v;
	}
	this.setCompiler_version=setCompiler_version;
	this.Library =new Array();

	function getLibrary() {
		return this.Library;
	}
	this.getLibrary=getLibrary;


	function addLibrary(v){
		this.Library.push(v);
	}
	this.addLibrary=addLibrary;

	this.ProvProcessstepId=null;


	function getProvProcessstepId() {
		return this.ProvProcessstepId;
	}
	this.getProvProcessstepId=getProvProcessstepId;


	function setProvProcessstepId(v){
		this.ProvProcessstepId=v;
	}
	this.setProvProcessstepId=setProvProcessstepId;

	this.prov_process_prov_process_id_fk=null;


	this.getprov_process_prov_process_id=function() {
		return this.prov_process_prov_process_id_fk;
	}


	this.setprov_process_prov_process_id=function(v){
		this.prov_process_prov_process_id_fk=v;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="program"){
				return this.Program ;
			} else 
			if(xmlPath=="program/version"){
				return this.Program_version ;
			} else 
			if(xmlPath=="program/arguments"){
				return this.Program_arguments ;
			} else 
			if(xmlPath=="timestamp"){
				return this.Timestamp ;
			} else 
			if(xmlPath=="cvs"){
				return this.Cvs ;
			} else 
			if(xmlPath=="user"){
				return this.User ;
			} else 
			if(xmlPath=="machine"){
				return this.Machine ;
			} else 
			if(xmlPath=="platform"){
				return this.Platform ;
			} else 
			if(xmlPath=="platform/version"){
				return this.Platform_version ;
			} else 
			if(xmlPath=="compiler"){
				return this.Compiler ;
			} else 
			if(xmlPath=="compiler/version"){
				return this.Compiler_version ;
			} else 
			if(xmlPath=="library"){
				return this.Library ;
			} else 
			if(xmlPath.startsWith("library")){
				xmlPath=xmlPath.substring(7);
				if(xmlPath=="")return this.Library ;
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

				for(var whereCount=0;whereCount<this.Library.length;whereCount++){

					var tempValue=this.Library[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Library[whereCount]);

					}

				}
				}else{

				whereArray=this.Library;
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
			if(xmlPath=="prov_processStep_id"){
				return this.ProvProcessstepId ;
			} else 
			if(xmlPath=="prov_process_prov_process_id"){
				return this.prov_process_prov_process_id_fk ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="program"){
				this.Program=value;
			} else 
			if(xmlPath=="program/version"){
				this.Program_version=value;
			} else 
			if(xmlPath=="program/arguments"){
				this.Program_arguments=value;
			} else 
			if(xmlPath=="timestamp"){
				this.Timestamp=value;
			} else 
			if(xmlPath=="cvs"){
				this.Cvs=value;
			} else 
			if(xmlPath=="user"){
				this.User=value;
			} else 
			if(xmlPath=="machine"){
				this.Machine=value;
			} else 
			if(xmlPath=="platform"){
				this.Platform=value;
			} else 
			if(xmlPath=="platform/version"){
				this.Platform_version=value;
			} else 
			if(xmlPath=="compiler"){
				this.Compiler=value;
			} else 
			if(xmlPath=="compiler/version"){
				this.Compiler_version=value;
			} else 
			if(xmlPath=="library"){
				this.Library=value;
			} else 
			if(xmlPath.startsWith("library")){
				xmlPath=xmlPath.substring(7);
				if(xmlPath=="")return this.Library ;
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

				for(var whereCount=0;whereCount<this.Library.length;whereCount++){

					var tempValue=this.Library[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Library[whereCount]);

					}

				}
				}else{

				whereArray=this.Library;
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
						newChild= instanciateObject("prov:processStep_library");//omUtils.js
					}
					this.addLibrary(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="prov_processStep_id"){
				this.ProvProcessstepId=value;
			} else 
			if(xmlPath=="prov_process_prov_process_id"){
				this.prov_process_prov_process_id_fk=value;
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
		if (xmlPath=="library"){
			this.addLibrary(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="library"){
			return "http://www.nbirn.net/prov:processStep_library";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="program"){
			return "field_LONG_DATA";
		}else if (xmlPath=="program/version"){
			return "field_data";
		}else if (xmlPath=="program/arguments"){
			return "field_LONG_DATA";
		}else if (xmlPath=="timestamp"){
			return "field_data";
		}else if (xmlPath=="cvs"){
			return "field_LONG_DATA";
		}else if (xmlPath=="user"){
			return "field_data";
		}else if (xmlPath=="machine"){
			return "field_data";
		}else if (xmlPath=="platform"){
			return "field_data";
		}else if (xmlPath=="platform/version"){
			return "field_data";
		}else if (xmlPath=="compiler"){
			return "field_data";
		}else if (xmlPath=="compiler/version"){
			return "field_data";
		}else if (xmlPath=="library"){
			return "field_NO_CHILD";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<prov:processStep";
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
		xmlTxt+="\n</prov:processStep>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.ProvProcessstepId!=null){
				if(hiddenCount++>0)str+=",";
				str+="prov_processStep_id=\"" + this.ProvProcessstepId + "\"";
			}
			if(this.prov_process_prov_process_id_fk!=null){
				if(hiddenCount++>0)str+=",";
				str+="prov_process_prov_process_id=\"" + this.prov_process_prov_process_id_fk + "\"";
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
		var ProgramATT = ""
		if (this.Program_version!=null)
			ProgramATT+=" version=\"" + this.Program_version.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Program_arguments!=null)
			ProgramATT+=" arguments=\"" + this.Program_arguments.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Program!=null){
			xmlTxt+="\n<prov:program";
			xmlTxt+=ProgramATT;
			xmlTxt+=">";
			xmlTxt+=this.Program.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</prov:program>";
		}
		else if(ProgramATT!=""){
			xmlTxt+="\n<prov:program";
			xmlTxt+=ProgramATT;
			xmlTxt+="/>";
		}

		if (this.Timestamp!=null){
			xmlTxt+="\n<prov:timestamp";
			xmlTxt+=">";
			xmlTxt+=this.Timestamp;
			xmlTxt+="</prov:timestamp>";
		}
		else{
			xmlTxt+="\n<prov:timestamp";
			xmlTxt+="/>";
		}

		if (this.Cvs!=null){
			xmlTxt+="\n<prov:cvs";
			xmlTxt+=">";
			xmlTxt+=this.Cvs.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</prov:cvs>";
		}
		else{
			xmlTxt+="\n<prov:cvs";
			xmlTxt+="/>";
		}

		if (this.User!=null){
			xmlTxt+="\n<prov:user";
			xmlTxt+=">";
			xmlTxt+=this.User.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</prov:user>";
		}
		if (this.Machine!=null){
			xmlTxt+="\n<prov:machine";
			xmlTxt+=">";
			xmlTxt+=this.Machine.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</prov:machine>";
		}
		var PlatformATT = ""
		if (this.Platform_version!=null)
			PlatformATT+=" version=\"" + this.Platform_version.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Platform!=null){
			xmlTxt+="\n<prov:platform";
			xmlTxt+=PlatformATT;
			xmlTxt+=">";
			xmlTxt+=this.Platform.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</prov:platform>";
		}
		else if(PlatformATT!=""){
			xmlTxt+="\n<prov:platform";
			xmlTxt+=PlatformATT;
			xmlTxt+="/>";
		}

		var CompilerATT = ""
		if (this.Compiler_version!=null)
			CompilerATT+=" version=\"" + this.Compiler_version.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Compiler!=null){
			xmlTxt+="\n<prov:compiler";
			xmlTxt+=CompilerATT;
			xmlTxt+=">";
			xmlTxt+=this.Compiler.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</prov:compiler>";
		}
		else if(CompilerATT!=""){
			xmlTxt+="\n<prov:compiler";
			xmlTxt+=CompilerATT;
			xmlTxt+="/>";
		}

		for(var LibraryCOUNT=0;LibraryCOUNT<this.Library.length;LibraryCOUNT++){
			xmlTxt +="\n<prov:library";
			xmlTxt +=this.Library[LibraryCOUNT].getXMLAtts();
			if(this.Library[LibraryCOUNT].xsiType!="prov:processStep_library"){
				xmlTxt+=" xsi:type=\"" + this.Library[LibraryCOUNT].xsiType + "\"";
			}
			if (this.Library[LibraryCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Library[LibraryCOUNT].getXMLBody(preventComments);
					xmlTxt+="</prov:library>";
			}else {xmlTxt+="/>";}
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.ProvProcessstepId!=null) return true;
			if (this.prov_process_prov_process_id_fk!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.Program_version!=null)
			return true;
		if (this.Program_arguments!=null)
			return true;
		if (this.Program!=null) return true;
		if (this.Timestamp!=null) return true;
		return true;//REQUIRED timestamp
	}
}
