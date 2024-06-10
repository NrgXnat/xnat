/*
 * web: xnat_demographicData.js
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

function xnat_demographicData(){
this.xsiType="xnat:demographicData";

	this.getSchemaElementName=function(){
		return "demographicData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:demographicData";
	}
this.extension=dynamicJSLoad('xnat_abstractDemographicData','generated/xnat_abstractDemographicData.js');

	this.Dob=null;


	function getDob() {
		return this.Dob;
	}
	this.getDob=getDob;


	function setDob(v){
		this.Dob=v;
	}
	this.setDob=setDob;

	this.Yob=null;


	function getYob() {
		return this.Yob;
	}
	this.getYob=getYob;


	function setYob(v){
		this.Yob=v;
	}
	this.setYob=setYob;

	this.Age=null;


	function getAge() {
		return this.Age;
	}
	this.getAge=getAge;


	function setAge(v){
		this.Age=v;
	}
	this.setAge=setAge;

	this.Gender=null;


	function getGender() {
		return this.Gender;
	}
	this.getGender=getGender;


	function setGender(v){
		this.Gender=v;
	}
	this.setGender=setGender;

	this.Handedness=null;


	function getHandedness() {
		return this.Handedness;
	}
	this.getHandedness=getHandedness;


	function setHandedness(v){
		this.Handedness=v;
	}
	this.setHandedness=setHandedness;

	this.Ses=null;


	function getSes() {
		return this.Ses;
	}
	this.getSes=getSes;


	function setSes(v){
		this.Ses=v;
	}
	this.setSes=setSes;

	this.Employment=null;


	function getEmployment() {
		return this.Employment;
	}
	this.getEmployment=getEmployment;


	function setEmployment(v){
		this.Employment=v;
	}
	this.setEmployment=setEmployment;

	this.Education=null;


	function getEducation() {
		return this.Education;
	}
	this.getEducation=getEducation;


	function setEducation(v){
		this.Education=v;
	}
	this.setEducation=setEducation;

	this.Educationdesc=null;


	function getEducationdesc() {
		return this.Educationdesc;
	}
	this.getEducationdesc=getEducationdesc;


	function setEducationdesc(v){
		this.Educationdesc=v;
	}
	this.setEducationdesc=setEducationdesc;

	this.Race=null;


	function getRace() {
		return this.Race;
	}
	this.getRace=getRace;


	function setRace(v){
		this.Race=v;
	}
	this.setRace=setRace;

	this.Race2=null;


	function getRace2() {
		return this.Race2;
	}
	this.getRace2=getRace2;


	function setRace2(v){
		this.Race2=v;
	}
	this.setRace2=setRace2;

	this.Race3=null;


	function getRace3() {
		return this.Race3;
	}
	this.getRace3=getRace3;


	function setRace3(v){
		this.Race3=v;
	}
	this.setRace3=setRace3;

	this.Race4=null;


	function getRace4() {
		return this.Race4;
	}
	this.getRace4=getRace4;


	function setRace4(v){
		this.Race4=v;
	}
	this.setRace4=setRace4;

	this.Race5=null;


	function getRace5() {
		return this.Race5;
	}
	this.getRace5=getRace5;


	function setRace5(v){
		this.Race5=v;
	}
	this.setRace5=setRace5;

	this.Race6=null;


	function getRace6() {
		return this.Race6;
	}
	this.getRace6=getRace6;


	function setRace6(v){
		this.Race6=v;
	}
	this.setRace6=setRace6;

	this.Ethnicity=null;


	function getEthnicity() {
		return this.Ethnicity;
	}
	this.getEthnicity=getEthnicity;


	function setEthnicity(v){
		this.Ethnicity=v;
	}
	this.setEthnicity=setEthnicity;

	this.Weight=null;


	function getWeight() {
		return this.Weight;
	}
	this.getWeight=getWeight;


	function setWeight(v){
		this.Weight=v;
	}
	this.setWeight=setWeight;

	this.Weight_units=null;


	function getWeight_units() {
		return this.Weight_units;
	}
	this.getWeight_units=getWeight_units;


	function setWeight_units(v){
		this.Weight_units=v;
	}
	this.setWeight_units=setWeight_units;

	this.Height=null;


	function getHeight() {
		return this.Height;
	}
	this.getHeight=getHeight;


	function setHeight(v){
		this.Height=v;
	}
	this.setHeight=setHeight;

	this.Height_units=null;


	function getHeight_units() {
		return this.Height_units;
	}
	this.getHeight_units=getHeight_units;


	function setHeight_units(v){
		this.Height_units=v;
	}
	this.setHeight_units=setHeight_units;

	this.GestationalAge=null;


	function getGestationalAge() {
		return this.GestationalAge;
	}
	this.getGestationalAge=getGestationalAge;


	function setGestationalAge(v){
		this.GestationalAge=v;
	}
	this.setGestationalAge=setGestationalAge;

	this.PostMenstrualAge=null;


	function getPostMenstrualAge() {
		return this.PostMenstrualAge;
	}
	this.getPostMenstrualAge=getPostMenstrualAge;


	function setPostMenstrualAge(v){
		this.PostMenstrualAge=v;
	}
	this.setPostMenstrualAge=setPostMenstrualAge;

	this.BirthWeight=null;


	function getBirthWeight() {
		return this.BirthWeight;
	}
	this.getBirthWeight=getBirthWeight;


	function setBirthWeight(v){
		this.BirthWeight=v;
	}
	this.setBirthWeight=setBirthWeight;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="abstractDemographicData"){
				return this.Abstractdemographicdata ;
			} else 
			if(xmlPath.startsWith("abstractDemographicData")){
				xmlPath=xmlPath.substring(23);
				if(xmlPath=="")return this.Abstractdemographicdata ;
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
				if(this.Abstractdemographicdata!=undefined)return this.Abstractdemographicdata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="dob"){
				return this.Dob ;
			} else 
			if(xmlPath=="yob"){
				return this.Yob ;
			} else 
			if(xmlPath=="age"){
				return this.Age ;
			} else 
			if(xmlPath=="gender"){
				return this.Gender ;
			} else 
			if(xmlPath=="handedness"){
				return this.Handedness ;
			} else 
			if(xmlPath=="ses"){
				return this.Ses ;
			} else 
			if(xmlPath=="employment"){
				return this.Employment ;
			} else 
			if(xmlPath=="education"){
				return this.Education ;
			} else 
			if(xmlPath=="educationDesc"){
				return this.Educationdesc ;
			} else 
			if(xmlPath=="race"){
				return this.Race ;
			} else 
			if(xmlPath=="race2"){
				return this.Race2 ;
			} else 
			if(xmlPath=="race3"){
				return this.Race3 ;
			} else 
			if(xmlPath=="race4"){
				return this.Race4 ;
			} else 
			if(xmlPath=="race5"){
				return this.Race5 ;
			} else 
			if(xmlPath=="race6"){
				return this.Race6 ;
			} else 
			if(xmlPath=="ethnicity"){
				return this.Ethnicity ;
			} else 
			if(xmlPath=="weight"){
				return this.Weight ;
			} else 
			if(xmlPath=="weight/units"){
				return this.Weight_units ;
			} else 
			if(xmlPath=="height"){
				return this.Height ;
			} else 
			if(xmlPath=="height/units"){
				return this.Height_units ;
			} else 
			if(xmlPath=="gestational_age"){
				return this.GestationalAge ;
			} else 
			if(xmlPath=="post_menstrual_age"){
				return this.PostMenstrualAge ;
			} else 
			if(xmlPath=="birth_weight"){
				return this.BirthWeight ;
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
			if(xmlPath=="abstractDemographicData"){
				this.Abstractdemographicdata=value;
			} else 
			if(xmlPath.startsWith("abstractDemographicData")){
				xmlPath=xmlPath.substring(23);
				if(xmlPath=="")return this.Abstractdemographicdata ;
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
				if(this.Abstractdemographicdata!=undefined){
					this.Abstractdemographicdata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Abstractdemographicdata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Abstractdemographicdata= instanciateObject("xnat:abstractDemographicData");//omUtils.js
						}
						if(options && options.where)this.Abstractdemographicdata.setProperty(options.where.field,options.where.value);
						this.Abstractdemographicdata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="dob"){
				this.Dob=value;
			} else 
			if(xmlPath=="yob"){
				this.Yob=value;
			} else 
			if(xmlPath=="age"){
				this.Age=value;
			} else 
			if(xmlPath=="gender"){
				this.Gender=value;
			} else 
			if(xmlPath=="handedness"){
				this.Handedness=value;
			} else 
			if(xmlPath=="ses"){
				this.Ses=value;
			} else 
			if(xmlPath=="employment"){
				this.Employment=value;
			} else 
			if(xmlPath=="education"){
				this.Education=value;
			} else 
			if(xmlPath=="educationDesc"){
				this.Educationdesc=value;
			} else 
			if(xmlPath=="race"){
				this.Race=value;
			} else 
			if(xmlPath=="race2"){
				this.Race2=value;
			} else 
			if(xmlPath=="race3"){
				this.Race3=value;
			} else 
			if(xmlPath=="race4"){
				this.Race4=value;
			} else 
			if(xmlPath=="race5"){
				this.Race5=value;
			} else 
			if(xmlPath=="race6"){
				this.Race6=value;
			} else 
			if(xmlPath=="ethnicity"){
				this.Ethnicity=value;
			} else 
			if(xmlPath=="weight"){
				this.Weight=value;
			} else 
			if(xmlPath=="weight/units"){
				this.Weight_units=value;
			} else 
			if(xmlPath=="height"){
				this.Height=value;
			} else 
			if(xmlPath=="height/units"){
				this.Height_units=value;
			} else 
			if(xmlPath=="gestational_age"){
				this.GestationalAge=value;
			} else 
			if(xmlPath=="post_menstrual_age"){
				this.PostMenstrualAge=value;
			} else 
			if(xmlPath=="birth_weight"){
				this.BirthWeight=value;
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
		if (xmlPath=="dob"){
			return "field_data";
		}else if (xmlPath=="yob"){
			return "field_data";
		}else if (xmlPath=="age"){
			return "field_data";
		}else if (xmlPath=="gender"){
			return "field_data";
		}else if (xmlPath=="handedness"){
			return "field_data";
		}else if (xmlPath=="ses"){
			return "field_data";
		}else if (xmlPath=="employment"){
			return "field_data";
		}else if (xmlPath=="education"){
			return "field_data";
		}else if (xmlPath=="educationDesc"){
			return "field_data";
		}else if (xmlPath=="race"){
			return "field_data";
		}else if (xmlPath=="race2"){
			return "field_data";
		}else if (xmlPath=="race3"){
			return "field_data";
		}else if (xmlPath=="race4"){
			return "field_data";
		}else if (xmlPath=="race5"){
			return "field_data";
		}else if (xmlPath=="race6"){
			return "field_data";
		}else if (xmlPath=="ethnicity"){
			return "field_data";
		}else if (xmlPath=="weight"){
			return "field_data";
		}else if (xmlPath=="weight/units"){
			return "field_data";
		}else if (xmlPath=="height"){
			return "field_data";
		}else if (xmlPath=="height/units"){
			return "field_data";
		}else if (xmlPath=="gestational_age"){
			return "field_data";
		}else if (xmlPath=="post_menstrual_age"){
			return "field_data";
		}else if (xmlPath=="birth_weight"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:demographicData";
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
		xmlTxt+="\n</xnat:demographicData>";
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
		if (this.Dob!=null){
			xmlTxt+="\n<xnat:dob";
			xmlTxt+=">";
			xmlTxt+=this.Dob;
			xmlTxt+="</xnat:dob>";
		}
		if (this.Yob!=null){
			xmlTxt+="\n<xnat:yob";
			xmlTxt+=">";
			xmlTxt+=this.Yob;
			xmlTxt+="</xnat:yob>";
		}
		if (this.Age!=null){
			xmlTxt+="\n<xnat:age";
			xmlTxt+=">";
			xmlTxt+=this.Age;
			xmlTxt+="</xnat:age>";
		}
		if (this.Gender!=null){
			xmlTxt+="\n<xnat:gender";
			xmlTxt+=">";
			xmlTxt+=this.Gender.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:gender>";
		}
		if (this.Handedness!=null){
			xmlTxt+="\n<xnat:handedness";
			xmlTxt+=">";
			xmlTxt+=this.Handedness.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:handedness>";
		}
		if (this.Ses!=null){
			xmlTxt+="\n<xnat:ses";
			xmlTxt+=">";
			xmlTxt+=this.Ses;
			xmlTxt+="</xnat:ses>";
		}
		if (this.Employment!=null){
			xmlTxt+="\n<xnat:employment";
			xmlTxt+=">";
			xmlTxt+=this.Employment;
			xmlTxt+="</xnat:employment>";
		}
		if (this.Education!=null){
			xmlTxt+="\n<xnat:education";
			xmlTxt+=">";
			xmlTxt+=this.Education;
			xmlTxt+="</xnat:education>";
		}
		if (this.Educationdesc!=null){
			xmlTxt+="\n<xnat:educationDesc";
			xmlTxt+=">";
			xmlTxt+=this.Educationdesc.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:educationDesc>";
		}
		if (this.Race!=null){
			xmlTxt+="\n<xnat:race";
			xmlTxt+=">";
			xmlTxt+=this.Race.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:race>";
		}
		if (this.Race2!=null){
			xmlTxt+="\n<xnat:race2";
			xmlTxt+=">";
			xmlTxt+=this.Race2.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:race2>";
		}
		if (this.Race3!=null){
			xmlTxt+="\n<xnat:race3";
			xmlTxt+=">";
			xmlTxt+=this.Race3.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:race3>";
		}
		if (this.Race4!=null){
			xmlTxt+="\n<xnat:race4";
			xmlTxt+=">";
			xmlTxt+=this.Race4.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:race4>";
		}
		if (this.Race5!=null){
			xmlTxt+="\n<xnat:race5";
			xmlTxt+=">";
			xmlTxt+=this.Race5.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:race5>";
		}
		if (this.Race6!=null){
			xmlTxt+="\n<xnat:race6";
			xmlTxt+=">";
			xmlTxt+=this.Race6.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:race6>";
		}
		if (this.Ethnicity!=null){
			xmlTxt+="\n<xnat:ethnicity";
			xmlTxt+=">";
			xmlTxt+=this.Ethnicity.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:ethnicity>";
		}
		var WeightATT = ""
		if (this.Weight_units!=null)
			WeightATT+=" units=\"" + this.Weight_units.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Weight!=null){
			xmlTxt+="\n<xnat:weight";
			xmlTxt+=WeightATT;
			xmlTxt+=">";
			xmlTxt+=this.Weight;
			xmlTxt+="</xnat:weight>";
		}
		else if(WeightATT!=""){
			xmlTxt+="\n<xnat:weight";
			xmlTxt+=WeightATT;
			xmlTxt+="/>";
		}

		var HeightATT = ""
		if (this.Height_units!=null)
			HeightATT+=" units=\"" + this.Height_units.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Height!=null){
			xmlTxt+="\n<xnat:height";
			xmlTxt+=HeightATT;
			xmlTxt+=">";
			xmlTxt+=this.Height;
			xmlTxt+="</xnat:height>";
		}
		else if(HeightATT!=""){
			xmlTxt+="\n<xnat:height";
			xmlTxt+=HeightATT;
			xmlTxt+="/>";
		}

		if (this.GestationalAge!=null){
			xmlTxt+="\n<xnat:gestational_age";
			xmlTxt+=">";
			xmlTxt+=this.GestationalAge;
			xmlTxt+="</xnat:gestational_age>";
		}
		if (this.PostMenstrualAge!=null){
			xmlTxt+="\n<xnat:post_menstrual_age";
			xmlTxt+=">";
			xmlTxt+=this.PostMenstrualAge;
			xmlTxt+="</xnat:post_menstrual_age>";
		}
		if (this.BirthWeight!=null){
			xmlTxt+="\n<xnat:birth_weight";
			xmlTxt+=">";
			xmlTxt+=this.BirthWeight;
			xmlTxt+="</xnat:birth_weight>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Dob!=null) return true;
		if (this.Yob!=null) return true;
		if (this.Age!=null) return true;
		if (this.Gender!=null) return true;
		if (this.Handedness!=null) return true;
		if (this.Ses!=null) return true;
		if (this.Employment!=null) return true;
		if (this.Education!=null) return true;
		if (this.Educationdesc!=null) return true;
		if (this.Race!=null) return true;
		if (this.Race2!=null) return true;
		if (this.Race3!=null) return true;
		if (this.Race4!=null) return true;
		if (this.Race5!=null) return true;
		if (this.Race6!=null) return true;
		if (this.Ethnicity!=null) return true;
		if (this.Weight_units!=null)
			return true;
		if (this.Weight!=null) return true;
		if (this.Height_units!=null)
			return true;
		if (this.Height!=null) return true;
		if (this.GestationalAge!=null) return true;
		if (this.PostMenstrualAge!=null) return true;
		if (this.BirthWeight!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
