/*
 * web: xnat_petSessionData.js
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

function xnat_petSessionData(){
this.xsiType="xnat:petSessionData";

	this.getSchemaElementName=function(){
		return "petSessionData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:petSessionData";
	}
this.extension=dynamicJSLoad('xnat_imageSessionData','generated/xnat_imageSessionData.js');

	this.Stabilization=null;


	function getStabilization() {
		return this.Stabilization;
	}
	this.getStabilization=getStabilization;


	function setStabilization(v){
		this.Stabilization=v;
	}
	this.setStabilization=setStabilization;

	this.Studytype=null;


	function getStudytype() {
		return this.Studytype;
	}
	this.getStudytype=getStudytype;


	function setStudytype(v){
		this.Studytype=v;
	}
	this.setStudytype=setStudytype;

	this.Patientid=null;


	function getPatientid() {
		return this.Patientid;
	}
	this.getPatientid=getPatientid;


	function setPatientid(v){
		this.Patientid=v;
	}
	this.setPatientid=setPatientid;

	this.Patientname=null;


	function getPatientname() {
		return this.Patientname;
	}
	this.getPatientname=getPatientname;


	function setPatientname(v){
		this.Patientname=v;
	}
	this.setPatientname=setPatientname;

	this.Tracer_dose_units=null;


	function getTracer_dose_units() {
		return this.Tracer_dose_units;
	}
	this.getTracer_dose_units=getTracer_dose_units;


	function setTracer_dose_units(v){
		this.Tracer_dose_units=v;
	}
	this.setTracer_dose_units=setTracer_dose_units;

	this.Tracer_dose=null;


	function getTracer_dose() {
		return this.Tracer_dose;
	}
	this.getTracer_dose=getTracer_dose;


	function setTracer_dose(v){
		this.Tracer_dose=v;
	}
	this.setTracer_dose=setTracer_dose;

	this.Tracer_specificactivity=null;


	function getTracer_specificactivity() {
		return this.Tracer_specificactivity;
	}
	this.getTracer_specificactivity=getTracer_specificactivity;


	function setTracer_specificactivity(v){
		this.Tracer_specificactivity=v;
	}
	this.setTracer_specificactivity=setTracer_specificactivity;

	this.Tracer_totalmass_units=null;


	function getTracer_totalmass_units() {
		return this.Tracer_totalmass_units;
	}
	this.getTracer_totalmass_units=getTracer_totalmass_units;


	function setTracer_totalmass_units(v){
		this.Tracer_totalmass_units=v;
	}
	this.setTracer_totalmass_units=setTracer_totalmass_units;

	this.Tracer_totalmass=null;


	function getTracer_totalmass() {
		return this.Tracer_totalmass;
	}
	this.getTracer_totalmass=getTracer_totalmass;


	function setTracer_totalmass(v){
		this.Tracer_totalmass=v;
	}
	this.setTracer_totalmass=setTracer_totalmass;

	this.Tracer_intermediate_units=null;


	function getTracer_intermediate_units() {
		return this.Tracer_intermediate_units;
	}
	this.getTracer_intermediate_units=getTracer_intermediate_units;


	function setTracer_intermediate_units(v){
		this.Tracer_intermediate_units=v;
	}
	this.setTracer_intermediate_units=setTracer_intermediate_units;

	this.Tracer_intermediate=null;


	function getTracer_intermediate() {
		return this.Tracer_intermediate;
	}
	this.getTracer_intermediate=getTracer_intermediate;


	function setTracer_intermediate(v){
		this.Tracer_intermediate=v;
	}
	this.setTracer_intermediate=setTracer_intermediate;

	this.Tracer_isotope_halfLife=null;


	function getTracer_isotope_halfLife() {
		return this.Tracer_isotope_halfLife;
	}
	this.getTracer_isotope_halfLife=getTracer_isotope_halfLife;


	function setTracer_isotope_halfLife(v){
		this.Tracer_isotope_halfLife=v;
	}
	this.setTracer_isotope_halfLife=setTracer_isotope_halfLife;

	this.Tracer_isotope=null;


	function getTracer_isotope() {
		return this.Tracer_isotope;
	}
	this.getTracer_isotope=getTracer_isotope;


	function setTracer_isotope(v){
		this.Tracer_isotope=v;
	}
	this.setTracer_isotope=setTracer_isotope;

	this.Tracer_transmissions=null;


	function getTracer_transmissions() {
		return this.Tracer_transmissions;
	}
	this.getTracer_transmissions=getTracer_transmissions;


	function setTracer_transmissions(v){
		this.Tracer_transmissions=v;
	}
	this.setTracer_transmissions=setTracer_transmissions;

	this.Tracer_transmissionsStarttime=null;


	function getTracer_transmissionsStarttime() {
		return this.Tracer_transmissionsStarttime;
	}
	this.getTracer_transmissionsStarttime=getTracer_transmissionsStarttime;


	function setTracer_transmissionsStarttime(v){
		this.Tracer_transmissionsStarttime=v;
	}
	this.setTracer_transmissionsStarttime=setTracer_transmissionsStarttime;

	this.Tracer_name=null;


	function getTracer_name() {
		return this.Tracer_name;
	}
	this.getTracer_name=getTracer_name;


	function setTracer_name(v){
		this.Tracer_name=v;
	}
	this.setTracer_name=setTracer_name;

	this.Tracer_starttime=null;


	function getTracer_starttime() {
		return this.Tracer_starttime;
	}
	this.getTracer_starttime=getTracer_starttime;


	function setTracer_starttime(v){
		this.Tracer_starttime=v;
	}
	this.setTracer_starttime=setTracer_starttime;

	this.StartTime=null;


	function getStartTime() {
		return this.StartTime;
	}
	this.getStartTime=getStartTime;


	function setStartTime(v){
		this.StartTime=v;
	}
	this.setStartTime=setStartTime;

	this.StartTimeScan=null;


	function getStartTimeScan() {
		return this.StartTimeScan;
	}
	this.getStartTimeScan=getStartTimeScan;


	function setStartTimeScan(v){
		this.StartTimeScan=v;
	}
	this.setStartTimeScan=setStartTimeScan;

	this.StartTimeInjection=null;


	function getStartTimeInjection() {
		return this.StartTimeInjection;
	}
	this.getStartTimeInjection=getStartTimeInjection;


	function setStartTimeInjection(v){
		this.StartTimeInjection=v;
	}
	this.setStartTimeInjection=setStartTimeInjection;

	this.BloodGlucose=null;


	function getBloodGlucose() {
		return this.BloodGlucose;
	}
	this.getBloodGlucose=getBloodGlucose;


	function setBloodGlucose(v){
		this.BloodGlucose=v;
	}
	this.setBloodGlucose=setBloodGlucose;

	this.BloodGlucoseUnits=null;


	function getBloodGlucoseUnits() {
		return this.BloodGlucoseUnits;
	}
	this.getBloodGlucoseUnits=getBloodGlucoseUnits;


	function setBloodGlucoseUnits(v){
		this.BloodGlucoseUnits=v;
	}
	this.setBloodGlucoseUnits=setBloodGlucoseUnits;

	this.BloodGlucoseTime=null;


	function getBloodGlucoseTime() {
		return this.BloodGlucoseTime;
	}
	this.getBloodGlucoseTime=getBloodGlucoseTime;


	function setBloodGlucoseTime(v){
		this.BloodGlucoseTime=v;
	}
	this.setBloodGlucoseTime=setBloodGlucoseTime;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageSessionData"){
				return this.Imagesessiondata ;
			} else 
			if(xmlPath.startsWith("imageSessionData")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Imagesessiondata ;
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
				if(this.Imagesessiondata!=undefined)return this.Imagesessiondata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="stabilization"){
				return this.Stabilization ;
			} else 
			if(xmlPath=="studyType"){
				return this.Studytype ;
			} else 
			if(xmlPath=="patientID"){
				return this.Patientid ;
			} else 
			if(xmlPath=="patientName"){
				return this.Patientname ;
			} else 
			if(xmlPath=="tracer/dose/units"){
				return this.Tracer_dose_units ;
			} else 
			if(xmlPath=="tracer/dose"){
				return this.Tracer_dose ;
			} else 
			if(xmlPath=="tracer/specificActivity"){
				return this.Tracer_specificactivity ;
			} else 
			if(xmlPath=="tracer/totalMass/units"){
				return this.Tracer_totalmass_units ;
			} else 
			if(xmlPath=="tracer/totalMass"){
				return this.Tracer_totalmass ;
			} else 
			if(xmlPath=="tracer/intermediate/units"){
				return this.Tracer_intermediate_units ;
			} else 
			if(xmlPath=="tracer/intermediate"){
				return this.Tracer_intermediate ;
			} else 
			if(xmlPath=="tracer/isotope/half-life"){
				return this.Tracer_isotope_halfLife ;
			} else 
			if(xmlPath=="tracer/isotope"){
				return this.Tracer_isotope ;
			} else 
			if(xmlPath=="tracer/transmissions"){
				return this.Tracer_transmissions ;
			} else 
			if(xmlPath=="tracer/transmissions_starttime"){
				return this.Tracer_transmissionsStarttime ;
			} else 
			if(xmlPath=="tracer/name"){
				return this.Tracer_name ;
			} else 
			if(xmlPath=="tracer/startTime"){
				return this.Tracer_starttime ;
			} else 
			if(xmlPath=="start_time"){
				return this.StartTime ;
			} else 
			if(xmlPath=="start_time_scan"){
				return this.StartTimeScan ;
			} else 
			if(xmlPath=="start_time_injection"){
				return this.StartTimeInjection ;
			} else 
			if(xmlPath=="blood_glucose"){
				return this.BloodGlucose ;
			} else 
			if(xmlPath=="blood_glucose_units"){
				return this.BloodGlucoseUnits ;
			} else 
			if(xmlPath=="blood_glucose_time"){
				return this.BloodGlucoseTime ;
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
			if(xmlPath=="imageSessionData"){
				this.Imagesessiondata=value;
			} else 
			if(xmlPath.startsWith("imageSessionData")){
				xmlPath=xmlPath.substring(16);
				if(xmlPath=="")return this.Imagesessiondata ;
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
				if(this.Imagesessiondata!=undefined){
					this.Imagesessiondata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Imagesessiondata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Imagesessiondata= instanciateObject("xnat:imageSessionData");//omUtils.js
						}
						if(options && options.where)this.Imagesessiondata.setProperty(options.where.field,options.where.value);
						this.Imagesessiondata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="stabilization"){
				this.Stabilization=value;
			} else 
			if(xmlPath=="studyType"){
				this.Studytype=value;
			} else 
			if(xmlPath=="patientID"){
				this.Patientid=value;
			} else 
			if(xmlPath=="patientName"){
				this.Patientname=value;
			} else 
			if(xmlPath=="tracer/dose/units"){
				this.Tracer_dose_units=value;
			} else 
			if(xmlPath=="tracer/dose"){
				this.Tracer_dose=value;
			} else 
			if(xmlPath=="tracer/specificActivity"){
				this.Tracer_specificactivity=value;
			} else 
			if(xmlPath=="tracer/totalMass/units"){
				this.Tracer_totalmass_units=value;
			} else 
			if(xmlPath=="tracer/totalMass"){
				this.Tracer_totalmass=value;
			} else 
			if(xmlPath=="tracer/intermediate/units"){
				this.Tracer_intermediate_units=value;
			} else 
			if(xmlPath=="tracer/intermediate"){
				this.Tracer_intermediate=value;
			} else 
			if(xmlPath=="tracer/isotope/half-life"){
				this.Tracer_isotope_halfLife=value;
			} else 
			if(xmlPath=="tracer/isotope"){
				this.Tracer_isotope=value;
			} else 
			if(xmlPath=="tracer/transmissions"){
				this.Tracer_transmissions=value;
			} else 
			if(xmlPath=="tracer/transmissions_starttime"){
				this.Tracer_transmissionsStarttime=value;
			} else 
			if(xmlPath=="tracer/name"){
				this.Tracer_name=value;
			} else 
			if(xmlPath=="tracer/startTime"){
				this.Tracer_starttime=value;
			} else 
			if(xmlPath=="start_time"){
				this.StartTime=value;
			} else 
			if(xmlPath=="start_time_scan"){
				this.StartTimeScan=value;
			} else 
			if(xmlPath=="start_time_injection"){
				this.StartTimeInjection=value;
			} else 
			if(xmlPath=="blood_glucose"){
				this.BloodGlucose=value;
			} else 
			if(xmlPath=="blood_glucose_units"){
				this.BloodGlucoseUnits=value;
			} else 
			if(xmlPath=="blood_glucose_time"){
				this.BloodGlucoseTime=value;
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
		if (xmlPath=="stabilization"){
			return "field_data";
		}else if (xmlPath=="studyType"){
			return "field_data";
		}else if (xmlPath=="patientID"){
			return "field_data";
		}else if (xmlPath=="patientName"){
			return "field_data";
		}else if (xmlPath=="tracer/dose/units"){
			return "field_data";
		}else if (xmlPath=="tracer/dose"){
			return "field_data";
		}else if (xmlPath=="tracer/specificActivity"){
			return "field_data";
		}else if (xmlPath=="tracer/totalMass/units"){
			return "field_data";
		}else if (xmlPath=="tracer/totalMass"){
			return "field_data";
		}else if (xmlPath=="tracer/intermediate/units"){
			return "field_data";
		}else if (xmlPath=="tracer/intermediate"){
			return "field_data";
		}else if (xmlPath=="tracer/isotope/half-life"){
			return "field_data";
		}else if (xmlPath=="tracer/isotope"){
			return "field_data";
		}else if (xmlPath=="tracer/transmissions"){
			return "field_data";
		}else if (xmlPath=="tracer/transmissions_starttime"){
			return "field_data";
		}else if (xmlPath=="tracer/name"){
			return "field_data";
		}else if (xmlPath=="tracer/startTime"){
			return "field_data";
		}else if (xmlPath=="start_time"){
			return "field_data";
		}else if (xmlPath=="start_time_scan"){
			return "field_data";
		}else if (xmlPath=="start_time_injection"){
			return "field_data";
		}else if (xmlPath=="blood_glucose"){
			return "field_data";
		}else if (xmlPath=="blood_glucose_units"){
			return "field_data";
		}else if (xmlPath=="blood_glucose_time"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:PETSession";
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
		xmlTxt+="\n</xnat:PETSession>";
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
		if (this.Stabilization!=null){
			xmlTxt+="\n<xnat:stabilization";
			xmlTxt+=">";
			xmlTxt+=this.Stabilization.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:stabilization>";
		}
		if (this.Studytype!=null){
			xmlTxt+="\n<xnat:studyType";
			xmlTxt+=">";
			xmlTxt+=this.Studytype.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:studyType>";
		}
		if (this.Patientid!=null){
			xmlTxt+="\n<xnat:patientID";
			xmlTxt+=">";
			xmlTxt+=this.Patientid.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:patientID>";
		}
		if (this.Patientname!=null){
			xmlTxt+="\n<xnat:patientName";
			xmlTxt+=">";
			xmlTxt+=this.Patientname.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:patientName>";
		}
		var TracerATT = ""
		if (this.Tracer_name!=null)
			TracerATT+=" name=\"" + this.Tracer_name.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Tracer_starttime!=null)
			TracerATT+=" startTime=\"" + this.Tracer_starttime + "\"";
			var child0=0;
			var att0=0;
			if(this.Tracer_transmissions!=null)
			child0++;
			if(this.Tracer_totalmass!=null)
			child0++;
			if(this.Tracer_intermediate_units!=null)
			child0++;
			if(this.Tracer_specificactivity!=null)
			child0++;
			if(this.Tracer_dose!=null)
			child0++;
			if(this.Tracer_totalmass_units!=null)
			child0++;
			if(this.Tracer_starttime!=null)
			att0++;
			if(this.Tracer_transmissionsStarttime!=null)
			child0++;
			if(this.Tracer_intermediate!=null)
			child0++;
			if(this.Tracer_dose_units!=null)
			child0++;
			if(this.Tracer_name!=null)
			att0++;
			if(this.Tracer_isotope_halfLife!=null)
			child0++;
			if(this.Tracer_isotope!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:tracer";
				xmlTxt+=TracerATT;
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		var Tracer_doseATT = ""
		if (this.Tracer_dose_units!=null)
			Tracer_doseATT+=" units=\"" + this.Tracer_dose_units.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Tracer_dose!=null){
			xmlTxt+="\n<xnat:dose";
			xmlTxt+=Tracer_doseATT;
			xmlTxt+=">";
			xmlTxt+=this.Tracer_dose;
			xmlTxt+="</xnat:dose>";
		}
		else if(Tracer_doseATT!=""){
			xmlTxt+="\n<xnat:dose";
			xmlTxt+=Tracer_doseATT;
			xmlTxt+="/>";
		}

		if (this.Tracer_specificactivity!=null){
			xmlTxt+="\n<xnat:specificActivity";
			xmlTxt+=">";
			xmlTxt+=this.Tracer_specificactivity;
			xmlTxt+="</xnat:specificActivity>";
		}
		var Tracer_totalmassATT = ""
		if (this.Tracer_totalmass_units!=null)
			Tracer_totalmassATT+=" units=\"" + this.Tracer_totalmass_units.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Tracer_totalmass!=null){
			xmlTxt+="\n<xnat:totalMass";
			xmlTxt+=Tracer_totalmassATT;
			xmlTxt+=">";
			xmlTxt+=this.Tracer_totalmass;
			xmlTxt+="</xnat:totalMass>";
		}
		else if(Tracer_totalmassATT!=""){
			xmlTxt+="\n<xnat:totalMass";
			xmlTxt+=Tracer_totalmassATT;
			xmlTxt+="/>";
		}

		var Tracer_intermediateATT = ""
		if (this.Tracer_intermediate_units!=null)
			Tracer_intermediateATT+=" units=\"" + this.Tracer_intermediate_units.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Tracer_intermediate!=null){
			xmlTxt+="\n<xnat:intermediate";
			xmlTxt+=Tracer_intermediateATT;
			xmlTxt+=">";
			xmlTxt+=this.Tracer_intermediate;
			xmlTxt+="</xnat:intermediate>";
		}
		else if(Tracer_intermediateATT!=""){
			xmlTxt+="\n<xnat:intermediate";
			xmlTxt+=Tracer_intermediateATT;
			xmlTxt+="/>";
		}

		var Tracer_isotopeATT = ""
		if (this.Tracer_isotope_halfLife!=null)
			Tracer_isotopeATT+=" half-life=\"" + this.Tracer_isotope_halfLife + "\"";
		if (this.Tracer_isotope!=null){
			xmlTxt+="\n<xnat:isotope";
			xmlTxt+=Tracer_isotopeATT;
			xmlTxt+=">";
			xmlTxt+=this.Tracer_isotope.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:isotope>";
		}
		else if(Tracer_isotopeATT!=""){
			xmlTxt+="\n<xnat:isotope";
			xmlTxt+=Tracer_isotopeATT;
			xmlTxt+="/>";
		}

		if (this.Tracer_transmissions!=null){
			xmlTxt+="\n<xnat:transmissions";
			xmlTxt+=">";
			xmlTxt+=this.Tracer_transmissions;
			xmlTxt+="</xnat:transmissions>";
		}
		if (this.Tracer_transmissionsStarttime!=null){
			xmlTxt+="\n<xnat:transmissions_starttime";
			xmlTxt+=">";
			xmlTxt+=this.Tracer_transmissionsStarttime;
			xmlTxt+="</xnat:transmissions_starttime>";
		}
				xmlTxt+="\n</xnat:tracer>";
			}
			}

		if (this.StartTime!=null){
			xmlTxt+="\n<xnat:start_time";
			xmlTxt+=">";
			xmlTxt+=this.StartTime;
			xmlTxt+="</xnat:start_time>";
		}
		if (this.StartTimeScan!=null){
			xmlTxt+="\n<xnat:start_time_scan";
			xmlTxt+=">";
			xmlTxt+=this.StartTimeScan;
			xmlTxt+="</xnat:start_time_scan>";
		}
		if (this.StartTimeInjection!=null){
			xmlTxt+="\n<xnat:start_time_injection";
			xmlTxt+=">";
			xmlTxt+=this.StartTimeInjection;
			xmlTxt+="</xnat:start_time_injection>";
		}
		if (this.BloodGlucose!=null){
			xmlTxt+="\n<xnat:blood_glucose";
			xmlTxt+=">";
			xmlTxt+=this.BloodGlucose;
			xmlTxt+="</xnat:blood_glucose>";
		}
		if (this.BloodGlucoseUnits!=null){
			xmlTxt+="\n<xnat:blood_glucose_units";
			xmlTxt+=">";
			xmlTxt+=this.BloodGlucoseUnits.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:blood_glucose_units>";
		}
		if (this.BloodGlucoseTime!=null){
			xmlTxt+="\n<xnat:blood_glucose_time";
			xmlTxt+=">";
			xmlTxt+=this.BloodGlucoseTime;
			xmlTxt+="</xnat:blood_glucose_time>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Stabilization!=null) return true;
		if (this.Studytype!=null) return true;
		if (this.Patientid!=null) return true;
		if (this.Patientname!=null) return true;
		if (this.Tracer_name!=null)
			return true;
		if (this.Tracer_starttime!=null)
			return true;
			if(this.Tracer_transmissions!=null) return true;
			if(this.Tracer_totalmass!=null) return true;
			if(this.Tracer_intermediate_units!=null) return true;
			if(this.Tracer_specificactivity!=null) return true;
			if(this.Tracer_dose!=null) return true;
			if(this.Tracer_totalmass_units!=null) return true;
			if(this.Tracer_starttime!=null) return true;
			if(this.Tracer_transmissionsStarttime!=null) return true;
			if(this.Tracer_intermediate!=null) return true;
			if(this.Tracer_dose_units!=null) return true;
			if(this.Tracer_name!=null) return true;
			if(this.Tracer_isotope_halfLife!=null) return true;
			if(this.Tracer_isotope!=null) return true;
		if (this.StartTime!=null) return true;
		if (this.StartTimeScan!=null) return true;
		if (this.StartTimeInjection!=null) return true;
		if (this.BloodGlucose!=null) return true;
		if (this.BloodGlucoseUnits!=null) return true;
		if (this.BloodGlucoseTime!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
