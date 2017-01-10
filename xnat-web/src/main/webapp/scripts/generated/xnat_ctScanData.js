/*
 * web: xnat_ctScanData.js
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

function xnat_ctScanData(){
this.xsiType="xnat:ctScanData";

	this.getSchemaElementName=function(){
		return "ctScanData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:ctScanData";
	}
this.extension=dynamicJSLoad('xnat_imageScanData','generated/xnat_imageScanData.js');

	this.Parameters_voxelres_units=null;


	function getParameters_voxelres_units() {
		return this.Parameters_voxelres_units;
	}
	this.getParameters_voxelres_units=getParameters_voxelres_units;


	function setParameters_voxelres_units(v){
		this.Parameters_voxelres_units=v;
	}
	this.setParameters_voxelres_units=setParameters_voxelres_units;

	this.Parameters_voxelres_x=null;


	function getParameters_voxelres_x() {
		return this.Parameters_voxelres_x;
	}
	this.getParameters_voxelres_x=getParameters_voxelres_x;


	function setParameters_voxelres_x(v){
		this.Parameters_voxelres_x=v;
	}
	this.setParameters_voxelres_x=setParameters_voxelres_x;

	this.Parameters_voxelres_y=null;


	function getParameters_voxelres_y() {
		return this.Parameters_voxelres_y;
	}
	this.getParameters_voxelres_y=getParameters_voxelres_y;


	function setParameters_voxelres_y(v){
		this.Parameters_voxelres_y=v;
	}
	this.setParameters_voxelres_y=setParameters_voxelres_y;

	this.Parameters_voxelres_z=null;


	function getParameters_voxelres_z() {
		return this.Parameters_voxelres_z;
	}
	this.getParameters_voxelres_z=getParameters_voxelres_z;


	function setParameters_voxelres_z(v){
		this.Parameters_voxelres_z=v;
	}
	this.setParameters_voxelres_z=setParameters_voxelres_z;

	this.Parameters_orientation=null;


	function getParameters_orientation() {
		return this.Parameters_orientation;
	}
	this.getParameters_orientation=getParameters_orientation;


	function setParameters_orientation(v){
		this.Parameters_orientation=v;
	}
	this.setParameters_orientation=setParameters_orientation;

	this.Parameters_fov_x=null;


	function getParameters_fov_x() {
		return this.Parameters_fov_x;
	}
	this.getParameters_fov_x=getParameters_fov_x;


	function setParameters_fov_x(v){
		this.Parameters_fov_x=v;
	}
	this.setParameters_fov_x=setParameters_fov_x;

	this.Parameters_fov_y=null;


	function getParameters_fov_y() {
		return this.Parameters_fov_y;
	}
	this.getParameters_fov_y=getParameters_fov_y;


	function setParameters_fov_y(v){
		this.Parameters_fov_y=v;
	}
	this.setParameters_fov_y=setParameters_fov_y;

	this.Parameters_rescale_intercept=null;


	function getParameters_rescale_intercept() {
		return this.Parameters_rescale_intercept;
	}
	this.getParameters_rescale_intercept=getParameters_rescale_intercept;


	function setParameters_rescale_intercept(v){
		this.Parameters_rescale_intercept=v;
	}
	this.setParameters_rescale_intercept=setParameters_rescale_intercept;

	this.Parameters_rescale_slope=null;


	function getParameters_rescale_slope() {
		return this.Parameters_rescale_slope;
	}
	this.getParameters_rescale_slope=getParameters_rescale_slope;


	function setParameters_rescale_slope(v){
		this.Parameters_rescale_slope=v;
	}
	this.setParameters_rescale_slope=setParameters_rescale_slope;

	this.Parameters_kvp=null;


	function getParameters_kvp() {
		return this.Parameters_kvp;
	}
	this.getParameters_kvp=getParameters_kvp;


	function setParameters_kvp(v){
		this.Parameters_kvp=v;
	}
	this.setParameters_kvp=setParameters_kvp;

	this.Parameters_acquisitionnumber=null;


	function getParameters_acquisitionnumber() {
		return this.Parameters_acquisitionnumber;
	}
	this.getParameters_acquisitionnumber=getParameters_acquisitionnumber;


	function setParameters_acquisitionnumber(v){
		this.Parameters_acquisitionnumber=v;
	}
	this.setParameters_acquisitionnumber=setParameters_acquisitionnumber;

	this.Parameters_imagetype=null;


	function getParameters_imagetype() {
		return this.Parameters_imagetype;
	}
	this.getParameters_imagetype=getParameters_imagetype;


	function setParameters_imagetype(v){
		this.Parameters_imagetype=v;
	}
	this.setParameters_imagetype=setParameters_imagetype;

	this.Parameters_options=null;


	function getParameters_options() {
		return this.Parameters_options;
	}
	this.getParameters_options=getParameters_options;


	function setParameters_options(v){
		this.Parameters_options=v;
	}
	this.setParameters_options=setParameters_options;

	this.Parameters_collectiondiameter=null;


	function getParameters_collectiondiameter() {
		return this.Parameters_collectiondiameter;
	}
	this.getParameters_collectiondiameter=getParameters_collectiondiameter;


	function setParameters_collectiondiameter(v){
		this.Parameters_collectiondiameter=v;
	}
	this.setParameters_collectiondiameter=setParameters_collectiondiameter;

	this.Parameters_distancesourcetodetector=null;


	function getParameters_distancesourcetodetector() {
		return this.Parameters_distancesourcetodetector;
	}
	this.getParameters_distancesourcetodetector=getParameters_distancesourcetodetector;


	function setParameters_distancesourcetodetector(v){
		this.Parameters_distancesourcetodetector=v;
	}
	this.setParameters_distancesourcetodetector=setParameters_distancesourcetodetector;

	this.Parameters_distancesourcetopatient=null;


	function getParameters_distancesourcetopatient() {
		return this.Parameters_distancesourcetopatient;
	}
	this.getParameters_distancesourcetopatient=getParameters_distancesourcetopatient;


	function setParameters_distancesourcetopatient(v){
		this.Parameters_distancesourcetopatient=v;
	}
	this.setParameters_distancesourcetopatient=setParameters_distancesourcetopatient;

	this.Parameters_gantrytilt=null;


	function getParameters_gantrytilt() {
		return this.Parameters_gantrytilt;
	}
	this.getParameters_gantrytilt=getParameters_gantrytilt;


	function setParameters_gantrytilt(v){
		this.Parameters_gantrytilt=v;
	}
	this.setParameters_gantrytilt=setParameters_gantrytilt;

	this.Parameters_tableheight=null;


	function getParameters_tableheight() {
		return this.Parameters_tableheight;
	}
	this.getParameters_tableheight=getParameters_tableheight;


	function setParameters_tableheight(v){
		this.Parameters_tableheight=v;
	}
	this.setParameters_tableheight=setParameters_tableheight;

	this.Parameters_rotationdirection=null;


	function getParameters_rotationdirection() {
		return this.Parameters_rotationdirection;
	}
	this.getParameters_rotationdirection=getParameters_rotationdirection;


	function setParameters_rotationdirection(v){
		this.Parameters_rotationdirection=v;
	}
	this.setParameters_rotationdirection=setParameters_rotationdirection;

	this.Parameters_exposuretime=null;


	function getParameters_exposuretime() {
		return this.Parameters_exposuretime;
	}
	this.getParameters_exposuretime=getParameters_exposuretime;


	function setParameters_exposuretime(v){
		this.Parameters_exposuretime=v;
	}
	this.setParameters_exposuretime=setParameters_exposuretime;

	this.Parameters_xraytubecurrent=null;


	function getParameters_xraytubecurrent() {
		return this.Parameters_xraytubecurrent;
	}
	this.getParameters_xraytubecurrent=getParameters_xraytubecurrent;


	function setParameters_xraytubecurrent(v){
		this.Parameters_xraytubecurrent=v;
	}
	this.setParameters_xraytubecurrent=setParameters_xraytubecurrent;

	this.Parameters_exposure=null;


	function getParameters_exposure() {
		return this.Parameters_exposure;
	}
	this.getParameters_exposure=getParameters_exposure;


	function setParameters_exposure(v){
		this.Parameters_exposure=v;
	}
	this.setParameters_exposure=setParameters_exposure;

	this.Parameters_filter=null;


	function getParameters_filter() {
		return this.Parameters_filter;
	}
	this.getParameters_filter=getParameters_filter;


	function setParameters_filter(v){
		this.Parameters_filter=v;
	}
	this.setParameters_filter=setParameters_filter;

	this.Parameters_generatorpower=null;


	function getParameters_generatorpower() {
		return this.Parameters_generatorpower;
	}
	this.getParameters_generatorpower=getParameters_generatorpower;


	function setParameters_generatorpower(v){
		this.Parameters_generatorpower=v;
	}
	this.setParameters_generatorpower=setParameters_generatorpower;
	this.Parameters_focalspots_focalspot =new Array();

	function getParameters_focalspots_focalspot() {
		return this.Parameters_focalspots_focalspot;
	}
	this.getParameters_focalspots_focalspot=getParameters_focalspots_focalspot;


	function addParameters_focalspots_focalspot(v){
		this.Parameters_focalspots_focalspot.push(v);
	}
	this.addParameters_focalspots_focalspot=addParameters_focalspots_focalspot;

	this.Parameters_convolutionkernel=null;


	function getParameters_convolutionkernel() {
		return this.Parameters_convolutionkernel;
	}
	this.getParameters_convolutionkernel=getParameters_convolutionkernel;


	function setParameters_convolutionkernel(v){
		this.Parameters_convolutionkernel=v;
	}
	this.setParameters_convolutionkernel=setParameters_convolutionkernel;

	this.Parameters_collimationwidth_single=null;


	function getParameters_collimationwidth_single() {
		return this.Parameters_collimationwidth_single;
	}
	this.getParameters_collimationwidth_single=getParameters_collimationwidth_single;


	function setParameters_collimationwidth_single(v){
		this.Parameters_collimationwidth_single=v;
	}
	this.setParameters_collimationwidth_single=setParameters_collimationwidth_single;

	this.Parameters_collimationwidth_total=null;


	function getParameters_collimationwidth_total() {
		return this.Parameters_collimationwidth_total;
	}
	this.getParameters_collimationwidth_total=getParameters_collimationwidth_total;


	function setParameters_collimationwidth_total(v){
		this.Parameters_collimationwidth_total=v;
	}
	this.setParameters_collimationwidth_total=setParameters_collimationwidth_total;

	this.Parameters_tablespeed=null;


	function getParameters_tablespeed() {
		return this.Parameters_tablespeed;
	}
	this.getParameters_tablespeed=getParameters_tablespeed;


	function setParameters_tablespeed(v){
		this.Parameters_tablespeed=v;
	}
	this.setParameters_tablespeed=setParameters_tablespeed;

	this.Parameters_tablefeedperrotation=null;


	function getParameters_tablefeedperrotation() {
		return this.Parameters_tablefeedperrotation;
	}
	this.getParameters_tablefeedperrotation=getParameters_tablefeedperrotation;


	function setParameters_tablefeedperrotation(v){
		this.Parameters_tablefeedperrotation=v;
	}
	this.setParameters_tablefeedperrotation=setParameters_tablefeedperrotation;

	this.Parameters_pitchfactor=null;


	function getParameters_pitchfactor() {
		return this.Parameters_pitchfactor;
	}
	this.getParameters_pitchfactor=getParameters_pitchfactor;


	function setParameters_pitchfactor(v){
		this.Parameters_pitchfactor=v;
	}
	this.setParameters_pitchfactor=setParameters_pitchfactor;

	this.Parameters_estimateddosesaving_modulation=null;


	function getParameters_estimateddosesaving_modulation() {
		return this.Parameters_estimateddosesaving_modulation;
	}
	this.getParameters_estimateddosesaving_modulation=getParameters_estimateddosesaving_modulation;


	function setParameters_estimateddosesaving_modulation(v){
		this.Parameters_estimateddosesaving_modulation=v;
	}
	this.setParameters_estimateddosesaving_modulation=setParameters_estimateddosesaving_modulation;

	this.Parameters_estimateddosesaving=null;


	function getParameters_estimateddosesaving() {
		return this.Parameters_estimateddosesaving;
	}
	this.getParameters_estimateddosesaving=getParameters_estimateddosesaving;


	function setParameters_estimateddosesaving(v){
		this.Parameters_estimateddosesaving=v;
	}
	this.setParameters_estimateddosesaving=setParameters_estimateddosesaving;

	this.Parameters_ctdivol=null;


	function getParameters_ctdivol() {
		return this.Parameters_ctdivol;
	}
	this.getParameters_ctdivol=getParameters_ctdivol;


	function setParameters_ctdivol(v){
		this.Parameters_ctdivol=v;
	}
	this.setParameters_ctdivol=setParameters_ctdivol;

	this.Parameters_derivation=null;


	function getParameters_derivation() {
		return this.Parameters_derivation;
	}
	this.getParameters_derivation=getParameters_derivation;


	function setParameters_derivation(v){
		this.Parameters_derivation=v;
	}
	this.setParameters_derivation=setParameters_derivation;
	this.Parameters_contrastbolus =null;
	function getParameters_contrastbolus() {
		return this.Parameters_contrastbolus;
	}
	this.getParameters_contrastbolus=getParameters_contrastbolus;


	function setParameters_contrastbolus(v){
		this.Parameters_contrastbolus =v;
	}
	this.setParameters_contrastbolus=setParameters_contrastbolus;

	this.Parameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId=null;


	function getParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId(){
		return this.Parameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId;
	}
	this.getParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId=getParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId;


	function setParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId(v){
		this.Parameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId=v;
	}
	this.setParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId=setParameters_contrastbolus_ParametersContrastbolusXnatContrastbolusId;

	this.Dcmvalidation=null;


	function getDcmvalidation() {
		return this.Dcmvalidation;
	}
	this.getDcmvalidation=getDcmvalidation;


	function setDcmvalidation(v){
		this.Dcmvalidation=v;
	}
	this.setDcmvalidation=setDcmvalidation;

	this.Dcmvalidation_status=null;


	function getDcmvalidation_status() {
		return this.Dcmvalidation_status;
	}
	this.getDcmvalidation_status=getDcmvalidation_status;


	function setDcmvalidation_status(v){
		this.Dcmvalidation_status=v;
	}
	this.setDcmvalidation_status=setDcmvalidation_status;


	this.isDcmvalidation_status=function(defaultValue) {
		if(this.Dcmvalidation_status==null)return defaultValue;
		if(this.Dcmvalidation_status=="1" || this.Dcmvalidation_status==true)return true;
		return false;
	}


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="imageScanData"){
				return this.Imagescandata ;
			} else 
			if(xmlPath.startsWith("imageScanData")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Imagescandata ;
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
				if(this.Imagescandata!=undefined)return this.Imagescandata.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="parameters/voxelRes/units"){
				return this.Parameters_voxelres_units ;
			} else 
			if(xmlPath=="parameters/voxelRes/x"){
				return this.Parameters_voxelres_x ;
			} else 
			if(xmlPath=="parameters/voxelRes/y"){
				return this.Parameters_voxelres_y ;
			} else 
			if(xmlPath=="parameters/voxelRes/z"){
				return this.Parameters_voxelres_z ;
			} else 
			if(xmlPath=="parameters/orientation"){
				return this.Parameters_orientation ;
			} else 
			if(xmlPath=="parameters/fov/x"){
				return this.Parameters_fov_x ;
			} else 
			if(xmlPath=="parameters/fov/y"){
				return this.Parameters_fov_y ;
			} else 
			if(xmlPath=="parameters/rescale/intercept"){
				return this.Parameters_rescale_intercept ;
			} else 
			if(xmlPath=="parameters/rescale/slope"){
				return this.Parameters_rescale_slope ;
			} else 
			if(xmlPath=="parameters/kvp"){
				return this.Parameters_kvp ;
			} else 
			if(xmlPath=="parameters/acquisitionNumber"){
				return this.Parameters_acquisitionnumber ;
			} else 
			if(xmlPath=="parameters/imageType"){
				return this.Parameters_imagetype ;
			} else 
			if(xmlPath=="parameters/options"){
				return this.Parameters_options ;
			} else 
			if(xmlPath=="parameters/collectionDiameter"){
				return this.Parameters_collectiondiameter ;
			} else 
			if(xmlPath=="parameters/distanceSourceToDetector"){
				return this.Parameters_distancesourcetodetector ;
			} else 
			if(xmlPath=="parameters/distanceSourceToPatient"){
				return this.Parameters_distancesourcetopatient ;
			} else 
			if(xmlPath=="parameters/gantryTilt"){
				return this.Parameters_gantrytilt ;
			} else 
			if(xmlPath=="parameters/tableHeight"){
				return this.Parameters_tableheight ;
			} else 
			if(xmlPath=="parameters/rotationDirection"){
				return this.Parameters_rotationdirection ;
			} else 
			if(xmlPath=="parameters/exposureTime"){
				return this.Parameters_exposuretime ;
			} else 
			if(xmlPath=="parameters/xrayTubeCurrent"){
				return this.Parameters_xraytubecurrent ;
			} else 
			if(xmlPath=="parameters/exposure"){
				return this.Parameters_exposure ;
			} else 
			if(xmlPath=="parameters/filter"){
				return this.Parameters_filter ;
			} else 
			if(xmlPath=="parameters/generatorPower"){
				return this.Parameters_generatorpower ;
			} else 
			if(xmlPath=="parameters/focalSpots/focalSpot"){
				return this.Parameters_focalspots_focalspot ;
			} else 
			if(xmlPath.startsWith("parameters/focalSpots/focalSpot")){
				xmlPath=xmlPath.substring(31);
				if(xmlPath=="")return this.Parameters_focalspots_focalspot ;
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

				for(var whereCount=0;whereCount<this.Parameters_focalspots_focalspot.length;whereCount++){

					var tempValue=this.Parameters_focalspots_focalspot[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_focalspots_focalspot[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_focalspots_focalspot;
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
			if(xmlPath=="parameters/convolutionKernel"){
				return this.Parameters_convolutionkernel ;
			} else 
			if(xmlPath=="parameters/collimationWidth/single"){
				return this.Parameters_collimationwidth_single ;
			} else 
			if(xmlPath=="parameters/collimationWidth/total"){
				return this.Parameters_collimationwidth_total ;
			} else 
			if(xmlPath=="parameters/tableSpeed"){
				return this.Parameters_tablespeed ;
			} else 
			if(xmlPath=="parameters/tableFeedPerRotation"){
				return this.Parameters_tablefeedperrotation ;
			} else 
			if(xmlPath=="parameters/pitchFactor"){
				return this.Parameters_pitchfactor ;
			} else 
			if(xmlPath=="parameters/estimatedDoseSaving/modulation"){
				return this.Parameters_estimateddosesaving_modulation ;
			} else 
			if(xmlPath=="parameters/estimatedDoseSaving"){
				return this.Parameters_estimateddosesaving ;
			} else 
			if(xmlPath=="parameters/ctDIvol"){
				return this.Parameters_ctdivol ;
			} else 
			if(xmlPath=="parameters/derivation"){
				return this.Parameters_derivation ;
			} else 
			if(xmlPath=="parameters/contrastBolus"){
				return this.Parameters_contrastbolus ;
			} else 
			if(xmlPath.startsWith("parameters/contrastBolus")){
				xmlPath=xmlPath.substring(24);
				if(xmlPath=="")return this.Parameters_contrastbolus ;
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
				if(this.Parameters_contrastbolus!=undefined)return this.Parameters_contrastbolus.getProperty(xmlPath);
				else return null;
			} else 
			if(xmlPath=="dcmValidation"){
				return this.Dcmvalidation ;
			} else 
			if(xmlPath=="dcmValidation/status"){
				return this.Dcmvalidation_status ;
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
			if(xmlPath=="imageScanData"){
				this.Imagescandata=value;
			} else 
			if(xmlPath.startsWith("imageScanData")){
				xmlPath=xmlPath.substring(13);
				if(xmlPath=="")return this.Imagescandata ;
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
				if(this.Imagescandata!=undefined){
					this.Imagescandata.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Imagescandata= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Imagescandata= instanciateObject("xnat:imageScanData");//omUtils.js
						}
						if(options && options.where)this.Imagescandata.setProperty(options.where.field,options.where.value);
						this.Imagescandata.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="parameters/voxelRes/units"){
				this.Parameters_voxelres_units=value;
			} else 
			if(xmlPath=="parameters/voxelRes/x"){
				this.Parameters_voxelres_x=value;
			} else 
			if(xmlPath=="parameters/voxelRes/y"){
				this.Parameters_voxelres_y=value;
			} else 
			if(xmlPath=="parameters/voxelRes/z"){
				this.Parameters_voxelres_z=value;
			} else 
			if(xmlPath=="parameters/orientation"){
				this.Parameters_orientation=value;
			} else 
			if(xmlPath=="parameters/fov/x"){
				this.Parameters_fov_x=value;
			} else 
			if(xmlPath=="parameters/fov/y"){
				this.Parameters_fov_y=value;
			} else 
			if(xmlPath=="parameters/rescale/intercept"){
				this.Parameters_rescale_intercept=value;
			} else 
			if(xmlPath=="parameters/rescale/slope"){
				this.Parameters_rescale_slope=value;
			} else 
			if(xmlPath=="parameters/kvp"){
				this.Parameters_kvp=value;
			} else 
			if(xmlPath=="parameters/acquisitionNumber"){
				this.Parameters_acquisitionnumber=value;
			} else 
			if(xmlPath=="parameters/imageType"){
				this.Parameters_imagetype=value;
			} else 
			if(xmlPath=="parameters/options"){
				this.Parameters_options=value;
			} else 
			if(xmlPath=="parameters/collectionDiameter"){
				this.Parameters_collectiondiameter=value;
			} else 
			if(xmlPath=="parameters/distanceSourceToDetector"){
				this.Parameters_distancesourcetodetector=value;
			} else 
			if(xmlPath=="parameters/distanceSourceToPatient"){
				this.Parameters_distancesourcetopatient=value;
			} else 
			if(xmlPath=="parameters/gantryTilt"){
				this.Parameters_gantrytilt=value;
			} else 
			if(xmlPath=="parameters/tableHeight"){
				this.Parameters_tableheight=value;
			} else 
			if(xmlPath=="parameters/rotationDirection"){
				this.Parameters_rotationdirection=value;
			} else 
			if(xmlPath=="parameters/exposureTime"){
				this.Parameters_exposuretime=value;
			} else 
			if(xmlPath=="parameters/xrayTubeCurrent"){
				this.Parameters_xraytubecurrent=value;
			} else 
			if(xmlPath=="parameters/exposure"){
				this.Parameters_exposure=value;
			} else 
			if(xmlPath=="parameters/filter"){
				this.Parameters_filter=value;
			} else 
			if(xmlPath=="parameters/generatorPower"){
				this.Parameters_generatorpower=value;
			} else 
			if(xmlPath=="parameters/focalSpots/focalSpot"){
				this.Parameters_focalspots_focalspot=value;
			} else 
			if(xmlPath.startsWith("parameters/focalSpots/focalSpot")){
				xmlPath=xmlPath.substring(31);
				if(xmlPath=="")return this.Parameters_focalspots_focalspot ;
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

				for(var whereCount=0;whereCount<this.Parameters_focalspots_focalspot.length;whereCount++){

					var tempValue=this.Parameters_focalspots_focalspot[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_focalspots_focalspot[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_focalspots_focalspot;
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
						newChild= instanciateObject("xnat:ctScanData_focalSpot");//omUtils.js
					}
					this.addParameters_focalspots_focalspot(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="parameters/convolutionKernel"){
				this.Parameters_convolutionkernel=value;
			} else 
			if(xmlPath=="parameters/collimationWidth/single"){
				this.Parameters_collimationwidth_single=value;
			} else 
			if(xmlPath=="parameters/collimationWidth/total"){
				this.Parameters_collimationwidth_total=value;
			} else 
			if(xmlPath=="parameters/tableSpeed"){
				this.Parameters_tablespeed=value;
			} else 
			if(xmlPath=="parameters/tableFeedPerRotation"){
				this.Parameters_tablefeedperrotation=value;
			} else 
			if(xmlPath=="parameters/pitchFactor"){
				this.Parameters_pitchfactor=value;
			} else 
			if(xmlPath=="parameters/estimatedDoseSaving/modulation"){
				this.Parameters_estimateddosesaving_modulation=value;
			} else 
			if(xmlPath=="parameters/estimatedDoseSaving"){
				this.Parameters_estimateddosesaving=value;
			} else 
			if(xmlPath=="parameters/ctDIvol"){
				this.Parameters_ctdivol=value;
			} else 
			if(xmlPath=="parameters/derivation"){
				this.Parameters_derivation=value;
			} else 
			if(xmlPath=="parameters/contrastBolus"){
				this.Parameters_contrastbolus=value;
			} else 
			if(xmlPath.startsWith("parameters/contrastBolus")){
				xmlPath=xmlPath.substring(24);
				if(xmlPath=="")return this.Parameters_contrastbolus ;
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
				if(this.Parameters_contrastbolus!=undefined){
					this.Parameters_contrastbolus.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Parameters_contrastbolus= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Parameters_contrastbolus= instanciateObject("xnat:contrastBolus");//omUtils.js
						}
						if(options && options.where)this.Parameters_contrastbolus.setProperty(options.where.field,options.where.value);
						this.Parameters_contrastbolus.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="dcmValidation"){
				this.Dcmvalidation=value;
			} else 
			if(xmlPath=="dcmValidation/status"){
				this.Dcmvalidation_status=value;
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
		if (xmlPath=="parameters/focalSpots/focalSpot"){
			this.addParameters_focalspots_focalspot(v);
		}else if (xmlPath=="parameters/contrastBolus"){
			this.setParameters_contrastbolus(v);
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
		if (xmlPath=="parameters/focalSpots/focalSpot"){
			return "http://nrg.wustl.edu/xnat:ctScanData_focalSpot";
		}else if (xmlPath=="parameters/contrastBolus"){
			return "http://nrg.wustl.edu/xnat:contrastBolus";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="parameters/voxelRes/units"){
			return "field_data";
		}else if (xmlPath=="parameters/voxelRes/x"){
			return "field_data";
		}else if (xmlPath=="parameters/voxelRes/y"){
			return "field_data";
		}else if (xmlPath=="parameters/voxelRes/z"){
			return "field_data";
		}else if (xmlPath=="parameters/orientation"){
			return "field_data";
		}else if (xmlPath=="parameters/fov/x"){
			return "field_data";
		}else if (xmlPath=="parameters/fov/y"){
			return "field_data";
		}else if (xmlPath=="parameters/rescale/intercept"){
			return "field_data";
		}else if (xmlPath=="parameters/rescale/slope"){
			return "field_data";
		}else if (xmlPath=="parameters/kvp"){
			return "field_data";
		}else if (xmlPath=="parameters/acquisitionNumber"){
			return "field_data";
		}else if (xmlPath=="parameters/imageType"){
			return "field_data";
		}else if (xmlPath=="parameters/options"){
			return "field_data";
		}else if (xmlPath=="parameters/collectionDiameter"){
			return "field_data";
		}else if (xmlPath=="parameters/distanceSourceToDetector"){
			return "field_data";
		}else if (xmlPath=="parameters/distanceSourceToPatient"){
			return "field_data";
		}else if (xmlPath=="parameters/gantryTilt"){
			return "field_data";
		}else if (xmlPath=="parameters/tableHeight"){
			return "field_data";
		}else if (xmlPath=="parameters/rotationDirection"){
			return "field_data";
		}else if (xmlPath=="parameters/exposureTime"){
			return "field_data";
		}else if (xmlPath=="parameters/xrayTubeCurrent"){
			return "field_data";
		}else if (xmlPath=="parameters/exposure"){
			return "field_data";
		}else if (xmlPath=="parameters/filter"){
			return "field_data";
		}else if (xmlPath=="parameters/generatorPower"){
			return "field_data";
		}else if (xmlPath=="parameters/focalSpots/focalSpot"){
			return "field_inline_repeater";
		}else if (xmlPath=="parameters/convolutionKernel"){
			return "field_data";
		}else if (xmlPath=="parameters/collimationWidth/single"){
			return "field_data";
		}else if (xmlPath=="parameters/collimationWidth/total"){
			return "field_data";
		}else if (xmlPath=="parameters/tableSpeed"){
			return "field_data";
		}else if (xmlPath=="parameters/tableFeedPerRotation"){
			return "field_data";
		}else if (xmlPath=="parameters/pitchFactor"){
			return "field_data";
		}else if (xmlPath=="parameters/estimatedDoseSaving/modulation"){
			return "field_data";
		}else if (xmlPath=="parameters/estimatedDoseSaving"){
			return "field_data";
		}else if (xmlPath=="parameters/ctDIvol"){
			return "field_data";
		}else if (xmlPath=="parameters/derivation"){
			return "field_data";
		}else if (xmlPath=="parameters/contrastBolus"){
			return "field_single_reference";
		}else if (xmlPath=="dcmValidation"){
			return "field_data";
		}else if (xmlPath=="dcmValidation/status"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:CTScan";
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
		xmlTxt+="\n</xnat:CTScan>";
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
			var child0=0;
			var att0=0;
			if(this.Parameters_distancesourcetopatient!=null)
			child0++;
			if(this.Parameters_contrastbolus!=null)
			child0++;
			if(this.Parameters_acquisitionnumber!=null)
			child0++;
			if(this.Parameters_rescale_intercept!=null)
			child0++;
			if(this.Parameters_rescale_slope!=null)
			child0++;
			if(this.Parameters_ctdivol!=null)
			child0++;
			if(this.Parameters_fov_y!=null)
			child0++;
			if(this.Parameters_fov_x!=null)
			child0++;
			if(this.Parameters_xraytubecurrent!=null)
			child0++;
			if(this.Parameters_voxelres_z!=null)
			child0++;
			if(this.Parameters_voxelres_y!=null)
			child0++;
			if(this.Parameters_voxelres_x!=null)
			child0++;
			if(this.Parameters_convolutionkernel!=null)
			child0++;
			if(this.Parameters_collectiondiameter!=null)
			child0++;
			if(this.Parameters_exposuretime!=null)
			child0++;
			if(this.Parameters_generatorpower!=null)
			child0++;
			if(this.Parameters_kvp!=null)
			child0++;
			if(this.Parameters_pitchfactor!=null)
			child0++;
			if(this.Parameters_estimateddosesaving_modulation!=null)
			child0++;
			if(this.Parameters_tablefeedperrotation!=null)
			child0++;
			if(this.Parameters_rotationdirection!=null)
			child0++;
			if(this.Parameters_distancesourcetodetector!=null)
			child0++;
			if(this.Parameters_estimateddosesaving!=null)
			child0++;
			if(this.Parameters_filter!=null)
			child0++;
			if(this.Parameters_tableheight!=null)
			child0++;
			if(this.Parameters_collimationwidth_total!=null)
			child0++;
			if(this.Parameters_voxelres_units!=null)
			child0++;
			if(this.Parameters_tablespeed!=null)
			child0++;
			if(this.Parameters_imagetype!=null)
			child0++;
			if(this.Parameters_gantrytilt!=null)
			child0++;
			child0+=this.Parameters_focalspots_focalspot.length;
			if(this.Parameters_orientation!=null)
			child0++;
			if(this.Parameters_options!=null)
			child0++;
			if(this.Parameters_derivation!=null)
			child0++;
			if(this.Parameters_collimationwidth_single!=null)
			child0++;
			if(this.Parameters_exposure!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:parameters";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		var Parameters_voxelresATT = ""
		if (this.Parameters_voxelres_units!=null)
			Parameters_voxelresATT+=" units=\"" + this.Parameters_voxelres_units.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Parameters_voxelres_x!=null)
			Parameters_voxelresATT+=" x=\"" + this.Parameters_voxelres_x + "\"";
		if (this.Parameters_voxelres_y!=null)
			Parameters_voxelresATT+=" y=\"" + this.Parameters_voxelres_y + "\"";
		if (this.Parameters_voxelres_z!=null)
			Parameters_voxelresATT+=" z=\"" + this.Parameters_voxelres_z + "\"";
		if(Parameters_voxelresATT!=""){
			xmlTxt+="\n<xnat:voxelRes";
			xmlTxt+=Parameters_voxelresATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_orientation!=null){
			xmlTxt+="\n<xnat:orientation";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_orientation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:orientation>";
		}
		var Parameters_fovATT = ""
		if (this.Parameters_fov_x!=null)
			Parameters_fovATT+=" x=\"" + this.Parameters_fov_x + "\"";
		if (this.Parameters_fov_y!=null)
			Parameters_fovATT+=" y=\"" + this.Parameters_fov_y + "\"";
		if(Parameters_fovATT!=""){
			xmlTxt+="\n<xnat:fov";
			xmlTxt+=Parameters_fovATT;
			xmlTxt+="/>";
		}

			var child1=0;
			var att1=0;
			if(this.Parameters_rescale_slope!=null)
			child1++;
			if(this.Parameters_rescale_intercept!=null)
			child1++;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:rescale";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Parameters_rescale_intercept!=null){
			xmlTxt+="\n<xnat:intercept";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_rescale_intercept.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:intercept>";
		}
		if (this.Parameters_rescale_slope!=null){
			xmlTxt+="\n<xnat:slope";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_rescale_slope.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:slope>";
		}
				xmlTxt+="\n</xnat:rescale>";
			}
			}

		if (this.Parameters_kvp!=null){
			xmlTxt+="\n<xnat:kvp";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_kvp;
			xmlTxt+="</xnat:kvp>";
		}
		if (this.Parameters_acquisitionnumber!=null){
			xmlTxt+="\n<xnat:acquisitionNumber";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_acquisitionnumber;
			xmlTxt+="</xnat:acquisitionNumber>";
		}
		if (this.Parameters_imagetype!=null){
			xmlTxt+="\n<xnat:imageType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_imagetype.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:imageType>";
		}
		if (this.Parameters_options!=null){
			xmlTxt+="\n<xnat:options";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_options.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:options>";
		}
		if (this.Parameters_collectiondiameter!=null){
			xmlTxt+="\n<xnat:collectionDiameter";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_collectiondiameter;
			xmlTxt+="</xnat:collectionDiameter>";
		}
		if (this.Parameters_distancesourcetodetector!=null){
			xmlTxt+="\n<xnat:distanceSourceToDetector";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_distancesourcetodetector;
			xmlTxt+="</xnat:distanceSourceToDetector>";
		}
		if (this.Parameters_distancesourcetopatient!=null){
			xmlTxt+="\n<xnat:distanceSourceToPatient";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_distancesourcetopatient;
			xmlTxt+="</xnat:distanceSourceToPatient>";
		}
		if (this.Parameters_gantrytilt!=null){
			xmlTxt+="\n<xnat:gantryTilt";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_gantrytilt;
			xmlTxt+="</xnat:gantryTilt>";
		}
		if (this.Parameters_tableheight!=null){
			xmlTxt+="\n<xnat:tableHeight";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_tableheight;
			xmlTxt+="</xnat:tableHeight>";
		}
		if (this.Parameters_rotationdirection!=null){
			xmlTxt+="\n<xnat:rotationDirection";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_rotationdirection.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:rotationDirection>";
		}
		if (this.Parameters_exposuretime!=null){
			xmlTxt+="\n<xnat:exposureTime";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_exposuretime;
			xmlTxt+="</xnat:exposureTime>";
		}
		if (this.Parameters_xraytubecurrent!=null){
			xmlTxt+="\n<xnat:xrayTubeCurrent";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_xraytubecurrent;
			xmlTxt+="</xnat:xrayTubeCurrent>";
		}
		if (this.Parameters_exposure!=null){
			xmlTxt+="\n<xnat:exposure";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_exposure;
			xmlTxt+="</xnat:exposure>";
		}
		if (this.Parameters_filter!=null){
			xmlTxt+="\n<xnat:filter";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_filter.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:filter>";
		}
		if (this.Parameters_generatorpower!=null){
			xmlTxt+="\n<xnat:generatorPower";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_generatorpower;
			xmlTxt+="</xnat:generatorPower>";
		}
			var child2=0;
			var att2=0;
			child2+=this.Parameters_focalspots_focalspot.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xnat:focalSpots";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Parameters_focalspots_focalspotCOUNT=0;Parameters_focalspots_focalspotCOUNT<this.Parameters_focalspots_focalspot.length;Parameters_focalspots_focalspotCOUNT++){
			xmlTxt+=this.Parameters_focalspots_focalspot[Parameters_focalspots_focalspotCOUNT].getXMLBody(preventComments);
		}
				xmlTxt+="\n</xnat:focalSpots>";
			}
			}

		if (this.Parameters_convolutionkernel!=null){
			xmlTxt+="\n<xnat:convolutionKernel";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_convolutionkernel.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:convolutionKernel>";
		}
			var child3=0;
			var att3=0;
			if(this.Parameters_collimationwidth_total!=null)
			child3++;
			if(this.Parameters_collimationwidth_single!=null)
			child3++;
			if(child3>0 || att3>0){
				xmlTxt+="\n<xnat:collimationWidth";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Parameters_collimationwidth_single!=null){
			xmlTxt+="\n<xnat:single";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_collimationwidth_single;
			xmlTxt+="</xnat:single>";
		}
		if (this.Parameters_collimationwidth_total!=null){
			xmlTxt+="\n<xnat:total";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_collimationwidth_total;
			xmlTxt+="</xnat:total>";
		}
				xmlTxt+="\n</xnat:collimationWidth>";
			}
			}

		if (this.Parameters_tablespeed!=null){
			xmlTxt+="\n<xnat:tableSpeed";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_tablespeed;
			xmlTxt+="</xnat:tableSpeed>";
		}
		if (this.Parameters_tablefeedperrotation!=null){
			xmlTxt+="\n<xnat:tableFeedPerRotation";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_tablefeedperrotation;
			xmlTxt+="</xnat:tableFeedPerRotation>";
		}
		if (this.Parameters_pitchfactor!=null){
			xmlTxt+="\n<xnat:pitchFactor";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_pitchfactor;
			xmlTxt+="</xnat:pitchFactor>";
		}
		var Parameters_estimateddosesavingATT = ""
		if (this.Parameters_estimateddosesaving_modulation!=null)
			Parameters_estimateddosesavingATT+=" modulation=\"" + this.Parameters_estimateddosesaving_modulation.replace(/>/g,"&gt;").replace(/</g,"&lt;") + "\"";
		if (this.Parameters_estimateddosesaving!=null){
			xmlTxt+="\n<xnat:estimatedDoseSaving";
			xmlTxt+=Parameters_estimateddosesavingATT;
			xmlTxt+=">";
			xmlTxt+=this.Parameters_estimateddosesaving;
			xmlTxt+="</xnat:estimatedDoseSaving>";
		}
		else if(Parameters_estimateddosesavingATT!=""){
			xmlTxt+="\n<xnat:estimatedDoseSaving";
			xmlTxt+=Parameters_estimateddosesavingATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_ctdivol!=null){
			xmlTxt+="\n<xnat:ctDIvol";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_ctdivol;
			xmlTxt+="</xnat:ctDIvol>";
		}
		if (this.Parameters_derivation!=null){
			xmlTxt+="\n<xnat:derivation";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_derivation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:derivation>";
		}
		if (this.Parameters_contrastbolus!=null){
			xmlTxt+="\n<xnat:contrastBolus";
			xmlTxt+=this.Parameters_contrastbolus.getXMLAtts();
			if(this.Parameters_contrastbolus.xsiType!="xnat:contrastBolus"){
				xmlTxt+=" xsi:type=\"" + this.Parameters_contrastbolus.xsiType + "\"";
			}
			if (this.Parameters_contrastbolus.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Parameters_contrastbolus.getXMLBody(preventComments);
				xmlTxt+="</xnat:contrastBolus>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

				xmlTxt+="\n</xnat:parameters>";
			}
			}

		var DcmvalidationATT = ""
		if (this.Dcmvalidation_status!=null)
			DcmvalidationATT+=" status=\"" + this.Dcmvalidation_status + "\"";
		if (this.Dcmvalidation!=null){
			xmlTxt+="\n<xnat:dcmValidation";
			xmlTxt+=DcmvalidationATT;
			xmlTxt+=">";
			xmlTxt+=this.Dcmvalidation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:dcmValidation>";
		}
		else if(DcmvalidationATT!=""){
			xmlTxt+="\n<xnat:dcmValidation";
			xmlTxt+=DcmvalidationATT;
			xmlTxt+="/>";
		}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Parameters_distancesourcetopatient!=null) return true;
			if(this.Parameters_contrastbolus!=null) return true;
			if(this.Parameters_acquisitionnumber!=null) return true;
			if(this.Parameters_rescale_intercept!=null) return true;
			if(this.Parameters_rescale_slope!=null) return true;
			if(this.Parameters_ctdivol!=null) return true;
			if(this.Parameters_fov_y!=null) return true;
			if(this.Parameters_fov_x!=null) return true;
			if(this.Parameters_xraytubecurrent!=null) return true;
			if(this.Parameters_voxelres_z!=null) return true;
			if(this.Parameters_voxelres_y!=null) return true;
			if(this.Parameters_voxelres_x!=null) return true;
			if(this.Parameters_convolutionkernel!=null) return true;
			if(this.Parameters_collectiondiameter!=null) return true;
			if(this.Parameters_exposuretime!=null) return true;
			if(this.Parameters_generatorpower!=null) return true;
			if(this.Parameters_kvp!=null) return true;
			if(this.Parameters_pitchfactor!=null) return true;
			if(this.Parameters_estimateddosesaving_modulation!=null) return true;
			if(this.Parameters_tablefeedperrotation!=null) return true;
			if(this.Parameters_rotationdirection!=null) return true;
			if(this.Parameters_distancesourcetodetector!=null) return true;
			if(this.Parameters_estimateddosesaving!=null) return true;
			if(this.Parameters_filter!=null) return true;
			if(this.Parameters_tableheight!=null) return true;
			if(this.Parameters_collimationwidth_total!=null) return true;
			if(this.Parameters_voxelres_units!=null) return true;
			if(this.Parameters_tablespeed!=null) return true;
			if(this.Parameters_imagetype!=null) return true;
			if(this.Parameters_gantrytilt!=null) return true;
			if(this.Parameters_focalspots_focalspot.length>0)return true;
			if(this.Parameters_orientation!=null) return true;
			if(this.Parameters_options!=null) return true;
			if(this.Parameters_derivation!=null) return true;
			if(this.Parameters_collimationwidth_single!=null) return true;
			if(this.Parameters_exposure!=null) return true;
		if (this.Dcmvalidation_status!=null)
			return true;
		if (this.Dcmvalidation!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
