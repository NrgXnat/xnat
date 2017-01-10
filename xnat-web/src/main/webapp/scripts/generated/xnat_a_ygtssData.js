/*
 * web: xnat_a_ygtssData.js
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

function xnat_a_ygtssData(){
this.xsiType="xnat_a:ygtssData";

	this.getSchemaElementName=function(){
		return "ygtssData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat_a:ygtssData";
	}
this.extension=dynamicJSLoad('xnat_subjectAssessorData','generated/xnat_subjectAssessorData.js');

	this.Filledoutby=null;


	function getFilledoutby() {
		return this.Filledoutby;
	}
	this.getFilledoutby=getFilledoutby;


	function setFilledoutby(v){
		this.Filledoutby=v;
	}
	this.setFilledoutby=setFilledoutby;

	this.Period=null;


	function getPeriod() {
		return this.Period;
	}
	this.getPeriod=getPeriod;


	function setPeriod(v){
		this.Period=v;
	}
	this.setPeriod=setPeriod;

	this.Worsteverage=null;


	function getWorsteverage() {
		return this.Worsteverage;
	}
	this.getWorsteverage=getWorsteverage;


	function setWorsteverage(v){
		this.Worsteverage=v;
	}
	this.setWorsteverage=setWorsteverage;

	this.Motor_number=null;


	function getMotor_number() {
		return this.Motor_number;
	}
	this.getMotor_number=getMotor_number;


	function setMotor_number(v){
		this.Motor_number=v;
	}
	this.setMotor_number=setMotor_number;

	this.Motor_frequency=null;


	function getMotor_frequency() {
		return this.Motor_frequency;
	}
	this.getMotor_frequency=getMotor_frequency;


	function setMotor_frequency(v){
		this.Motor_frequency=v;
	}
	this.setMotor_frequency=setMotor_frequency;

	this.Motor_intensity=null;


	function getMotor_intensity() {
		return this.Motor_intensity;
	}
	this.getMotor_intensity=getMotor_intensity;


	function setMotor_intensity(v){
		this.Motor_intensity=v;
	}
	this.setMotor_intensity=setMotor_intensity;

	this.Motor_complexity=null;


	function getMotor_complexity() {
		return this.Motor_complexity;
	}
	this.getMotor_complexity=getMotor_complexity;


	function setMotor_complexity(v){
		this.Motor_complexity=v;
	}
	this.setMotor_complexity=setMotor_complexity;

	this.Motor_interference=null;


	function getMotor_interference() {
		return this.Motor_interference;
	}
	this.getMotor_interference=getMotor_interference;


	function setMotor_interference(v){
		this.Motor_interference=v;
	}
	this.setMotor_interference=setMotor_interference;

	this.Motor_inventory=null;


	function getMotor_inventory() {
		return this.Motor_inventory;
	}
	this.getMotor_inventory=getMotor_inventory;


	function setMotor_inventory(v){
		this.Motor_inventory=v;
	}
	this.setMotor_inventory=setMotor_inventory;

	this.Phonic_number=null;


	function getPhonic_number() {
		return this.Phonic_number;
	}
	this.getPhonic_number=getPhonic_number;


	function setPhonic_number(v){
		this.Phonic_number=v;
	}
	this.setPhonic_number=setPhonic_number;

	this.Phonic_frequency=null;


	function getPhonic_frequency() {
		return this.Phonic_frequency;
	}
	this.getPhonic_frequency=getPhonic_frequency;


	function setPhonic_frequency(v){
		this.Phonic_frequency=v;
	}
	this.setPhonic_frequency=setPhonic_frequency;

	this.Phonic_intensity=null;


	function getPhonic_intensity() {
		return this.Phonic_intensity;
	}
	this.getPhonic_intensity=getPhonic_intensity;


	function setPhonic_intensity(v){
		this.Phonic_intensity=v;
	}
	this.setPhonic_intensity=setPhonic_intensity;

	this.Phonic_complexity=null;


	function getPhonic_complexity() {
		return this.Phonic_complexity;
	}
	this.getPhonic_complexity=getPhonic_complexity;


	function setPhonic_complexity(v){
		this.Phonic_complexity=v;
	}
	this.setPhonic_complexity=setPhonic_complexity;

	this.Phonic_interference=null;


	function getPhonic_interference() {
		return this.Phonic_interference;
	}
	this.getPhonic_interference=getPhonic_interference;


	function setPhonic_interference(v){
		this.Phonic_interference=v;
	}
	this.setPhonic_interference=setPhonic_interference;

	this.Phonic_inventory=null;


	function getPhonic_inventory() {
		return this.Phonic_inventory;
	}
	this.getPhonic_inventory=getPhonic_inventory;


	function setPhonic_inventory(v){
		this.Phonic_inventory=v;
	}
	this.setPhonic_inventory=setPhonic_inventory;

	this.Impairment=null;


	function getImpairment() {
		return this.Impairment;
	}
	this.getImpairment=getImpairment;


	function setImpairment(v){
		this.Impairment=v;
	}
	this.setImpairment=setImpairment;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="subjectAssessorData"){
				return this.Subjectassessordata ;
			} else 
			if(xmlPath.startsWith("subjectAssessorData")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Subjectassessordata ;
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
				if(this.Subjectassessordata!=undefined)return this.Subjectassessordata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="filledOutBy"){
				return this.Filledoutby ;
			} else 
			if(xmlPath=="period"){
				return this.Period ;
			} else 
			if(xmlPath=="worstEverAge"){
				return this.Worsteverage ;
			} else 
			if(xmlPath=="motor/number"){
				return this.Motor_number ;
			} else 
			if(xmlPath=="motor/frequency"){
				return this.Motor_frequency ;
			} else 
			if(xmlPath=="motor/intensity"){
				return this.Motor_intensity ;
			} else 
			if(xmlPath=="motor/complexity"){
				return this.Motor_complexity ;
			} else 
			if(xmlPath=="motor/interference"){
				return this.Motor_interference ;
			} else 
			if(xmlPath=="motor/inventory"){
				return this.Motor_inventory ;
			} else 
			if(xmlPath=="phonic/number"){
				return this.Phonic_number ;
			} else 
			if(xmlPath=="phonic/frequency"){
				return this.Phonic_frequency ;
			} else 
			if(xmlPath=="phonic/intensity"){
				return this.Phonic_intensity ;
			} else 
			if(xmlPath=="phonic/complexity"){
				return this.Phonic_complexity ;
			} else 
			if(xmlPath=="phonic/interference"){
				return this.Phonic_interference ;
			} else 
			if(xmlPath=="phonic/inventory"){
				return this.Phonic_inventory ;
			} else 
			if(xmlPath=="impairment"){
				return this.Impairment ;
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
			if(xmlPath=="subjectAssessorData"){
				this.Subjectassessordata=value;
			} else 
			if(xmlPath.startsWith("subjectAssessorData")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Subjectassessordata ;
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
				if(this.Subjectassessordata!=undefined){
					this.Subjectassessordata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Subjectassessordata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Subjectassessordata= instanciateObject("xnat:subjectAssessorData");//omUtils.js
						}
						if(options && options.where)this.Subjectassessordata.setProperty(options.where.field,options.where.value);
						this.Subjectassessordata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="filledOutBy"){
				this.Filledoutby=value;
			} else 
			if(xmlPath=="period"){
				this.Period=value;
			} else 
			if(xmlPath=="worstEverAge"){
				this.Worsteverage=value;
			} else 
			if(xmlPath=="motor/number"){
				this.Motor_number=value;
			} else 
			if(xmlPath=="motor/frequency"){
				this.Motor_frequency=value;
			} else 
			if(xmlPath=="motor/intensity"){
				this.Motor_intensity=value;
			} else 
			if(xmlPath=="motor/complexity"){
				this.Motor_complexity=value;
			} else 
			if(xmlPath=="motor/interference"){
				this.Motor_interference=value;
			} else 
			if(xmlPath=="motor/inventory"){
				this.Motor_inventory=value;
			} else 
			if(xmlPath=="phonic/number"){
				this.Phonic_number=value;
			} else 
			if(xmlPath=="phonic/frequency"){
				this.Phonic_frequency=value;
			} else 
			if(xmlPath=="phonic/intensity"){
				this.Phonic_intensity=value;
			} else 
			if(xmlPath=="phonic/complexity"){
				this.Phonic_complexity=value;
			} else 
			if(xmlPath=="phonic/interference"){
				this.Phonic_interference=value;
			} else 
			if(xmlPath=="phonic/inventory"){
				this.Phonic_inventory=value;
			} else 
			if(xmlPath=="impairment"){
				this.Impairment=value;
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
			this.extension.setReferenceField(xmlPath,v);
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
			return this.extension.getReferenceFieldName(xmlPath);
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="filledOutBy"){
			return "field_data";
		}else if (xmlPath=="period"){
			return "field_data";
		}else if (xmlPath=="worstEverAge"){
			return "field_data";
		}else if (xmlPath=="motor/number"){
			return "field_data";
		}else if (xmlPath=="motor/frequency"){
			return "field_data";
		}else if (xmlPath=="motor/intensity"){
			return "field_data";
		}else if (xmlPath=="motor/complexity"){
			return "field_data";
		}else if (xmlPath=="motor/interference"){
			return "field_data";
		}else if (xmlPath=="motor/inventory"){
			return "field_data";
		}else if (xmlPath=="phonic/number"){
			return "field_data";
		}else if (xmlPath=="phonic/frequency"){
			return "field_data";
		}else if (xmlPath=="phonic/intensity"){
			return "field_data";
		}else if (xmlPath=="phonic/complexity"){
			return "field_data";
		}else if (xmlPath=="phonic/interference"){
			return "field_data";
		}else if (xmlPath=="phonic/inventory"){
			return "field_data";
		}else if (xmlPath=="impairment"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat_a:YGTSS";
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
		xmlTxt+="\n</xnat_a:YGTSS>";
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
		if (this.Filledoutby!=null){
			xmlTxt+="\n<xnat_a:filledOutBy";
			xmlTxt+=">";
			xmlTxt+=this.Filledoutby.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:filledOutBy>";
		}
		if (this.Period!=null){
			xmlTxt+="\n<xnat_a:period";
			xmlTxt+=">";
			xmlTxt+=this.Period.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:period>";
		}
		if (this.Worsteverage!=null){
			xmlTxt+="\n<xnat_a:worstEverAge";
			xmlTxt+=">";
			xmlTxt+=this.Worsteverage;
			xmlTxt+="</xnat_a:worstEverAge>";
		}
			var child0=0;
			var att0=0;
			if(this.Motor_intensity!=null)
			child0++;
			if(this.Motor_number!=null)
			child0++;
			if(this.Motor_inventory!=null)
			child0++;
			if(this.Motor_complexity!=null)
			child0++;
			if(this.Motor_interference!=null)
			child0++;
			if(this.Motor_frequency!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat_a:motor";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Motor_number!=null){
			xmlTxt+="\n<xnat_a:number";
			xmlTxt+=">";
			xmlTxt+=this.Motor_number;
			xmlTxt+="</xnat_a:number>";
		}
		if (this.Motor_frequency!=null){
			xmlTxt+="\n<xnat_a:frequency";
			xmlTxt+=">";
			xmlTxt+=this.Motor_frequency;
			xmlTxt+="</xnat_a:frequency>";
		}
		if (this.Motor_intensity!=null){
			xmlTxt+="\n<xnat_a:intensity";
			xmlTxt+=">";
			xmlTxt+=this.Motor_intensity;
			xmlTxt+="</xnat_a:intensity>";
		}
		if (this.Motor_complexity!=null){
			xmlTxt+="\n<xnat_a:complexity";
			xmlTxt+=">";
			xmlTxt+=this.Motor_complexity;
			xmlTxt+="</xnat_a:complexity>";
		}
		if (this.Motor_interference!=null){
			xmlTxt+="\n<xnat_a:interference";
			xmlTxt+=">";
			xmlTxt+=this.Motor_interference;
			xmlTxt+="</xnat_a:interference>";
		}
		if (this.Motor_inventory!=null){
			xmlTxt+="\n<xnat_a:inventory";
			xmlTxt+=">";
			xmlTxt+=this.Motor_inventory.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:inventory>";
		}
				xmlTxt+="\n</xnat_a:motor>";
			}
			}

			var child1=0;
			var att1=0;
			if(this.Phonic_intensity!=null)
			child1++;
			if(this.Phonic_complexity!=null)
			child1++;
			if(this.Phonic_interference!=null)
			child1++;
			if(this.Phonic_inventory!=null)
			child1++;
			if(this.Phonic_number!=null)
			child1++;
			if(this.Phonic_frequency!=null)
			child1++;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat_a:phonic";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Phonic_number!=null){
			xmlTxt+="\n<xnat_a:number";
			xmlTxt+=">";
			xmlTxt+=this.Phonic_number;
			xmlTxt+="</xnat_a:number>";
		}
		if (this.Phonic_frequency!=null){
			xmlTxt+="\n<xnat_a:frequency";
			xmlTxt+=">";
			xmlTxt+=this.Phonic_frequency;
			xmlTxt+="</xnat_a:frequency>";
		}
		if (this.Phonic_intensity!=null){
			xmlTxt+="\n<xnat_a:intensity";
			xmlTxt+=">";
			xmlTxt+=this.Phonic_intensity;
			xmlTxt+="</xnat_a:intensity>";
		}
		if (this.Phonic_complexity!=null){
			xmlTxt+="\n<xnat_a:complexity";
			xmlTxt+=">";
			xmlTxt+=this.Phonic_complexity;
			xmlTxt+="</xnat_a:complexity>";
		}
		if (this.Phonic_interference!=null){
			xmlTxt+="\n<xnat_a:interference";
			xmlTxt+=">";
			xmlTxt+=this.Phonic_interference;
			xmlTxt+="</xnat_a:interference>";
		}
		if (this.Phonic_inventory!=null){
			xmlTxt+="\n<xnat_a:inventory";
			xmlTxt+=">";
			xmlTxt+=this.Phonic_inventory.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:inventory>";
		}
				xmlTxt+="\n</xnat_a:phonic>";
			}
			}

		if (this.Impairment!=null){
			xmlTxt+="\n<xnat_a:impairment";
			xmlTxt+=">";
			xmlTxt+=this.Impairment;
			xmlTxt+="</xnat_a:impairment>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Filledoutby!=null) return true;
		if (this.Period!=null) return true;
		if (this.Worsteverage!=null) return true;
			if(this.Motor_intensity!=null) return true;
			if(this.Motor_number!=null) return true;
			if(this.Motor_inventory!=null) return true;
			if(this.Motor_complexity!=null) return true;
			if(this.Motor_interference!=null) return true;
			if(this.Motor_frequency!=null) return true;
			if(this.Phonic_intensity!=null) return true;
			if(this.Phonic_complexity!=null) return true;
			if(this.Phonic_interference!=null) return true;
			if(this.Phonic_inventory!=null) return true;
			if(this.Phonic_number!=null) return true;
			if(this.Phonic_frequency!=null) return true;
		if (this.Impairment!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
