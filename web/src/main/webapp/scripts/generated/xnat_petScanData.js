/*
 * web: xnat_petScanData.js
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

function xnat_petScanData(){
this.xsiType="xnat:petScanData";

	this.getSchemaElementName=function(){
		return "petScanData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:petScanData";
	}
this.extension=dynamicJSLoad('xnat_imageScanData','generated/xnat_imageScanData.js');

	this.Parameters_orientation=null;


	function getParameters_orientation() {
		return this.Parameters_orientation;
	}
	this.getParameters_orientation=getParameters_orientation;


	function setParameters_orientation(v){
		this.Parameters_orientation=v;
	}
	this.setParameters_orientation=setParameters_orientation;
	this.Parameters_addparam =new Array();

	function getParameters_addparam() {
		return this.Parameters_addparam;
	}
	this.getParameters_addparam=getParameters_addparam;


	function addParameters_addparam(v){
		this.Parameters_addparam.push(v);
	}
	this.addParameters_addparam=addParameters_addparam;

	this.Parameters_originalfilename=null;


	function getParameters_originalfilename() {
		return this.Parameters_originalfilename;
	}
	this.getParameters_originalfilename=getParameters_originalfilename;


	function setParameters_originalfilename(v){
		this.Parameters_originalfilename=v;
	}
	this.setParameters_originalfilename=setParameters_originalfilename;

	this.Parameters_systemtype=null;


	function getParameters_systemtype() {
		return this.Parameters_systemtype;
	}
	this.getParameters_systemtype=getParameters_systemtype;


	function setParameters_systemtype(v){
		this.Parameters_systemtype=v;
	}
	this.setParameters_systemtype=setParameters_systemtype;

	this.Parameters_filetype=null;


	function getParameters_filetype() {
		return this.Parameters_filetype;
	}
	this.getParameters_filetype=getParameters_filetype;


	function setParameters_filetype(v){
		this.Parameters_filetype=v;
	}
	this.setParameters_filetype=setParameters_filetype;

	this.Parameters_transaxialfov=null;


	function getParameters_transaxialfov() {
		return this.Parameters_transaxialfov;
	}
	this.getParameters_transaxialfov=getParameters_transaxialfov;


	function setParameters_transaxialfov(v){
		this.Parameters_transaxialfov=v;
	}
	this.setParameters_transaxialfov=setParameters_transaxialfov;

	this.Parameters_acqtype=null;


	function getParameters_acqtype() {
		return this.Parameters_acqtype;
	}
	this.getParameters_acqtype=getParameters_acqtype;


	function setParameters_acqtype(v){
		this.Parameters_acqtype=v;
	}
	this.setParameters_acqtype=setParameters_acqtype;

	this.Parameters_facility=null;


	function getParameters_facility() {
		return this.Parameters_facility;
	}
	this.getParameters_facility=getParameters_facility;


	function setParameters_facility(v){
		this.Parameters_facility=v;
	}
	this.setParameters_facility=setParameters_facility;

	this.Parameters_numplanes=null;


	function getParameters_numplanes() {
		return this.Parameters_numplanes;
	}
	this.getParameters_numplanes=getParameters_numplanes;


	function setParameters_numplanes(v){
		this.Parameters_numplanes=v;
	}
	this.setParameters_numplanes=setParameters_numplanes;
	this.Parameters_frames_frame =new Array();

	function getParameters_frames_frame() {
		return this.Parameters_frames_frame;
	}
	this.getParameters_frames_frame=getParameters_frames_frame;


	function addParameters_frames_frame(v){
		this.Parameters_frames_frame.push(v);
	}
	this.addParameters_frames_frame=addParameters_frames_frame;

	this.Parameters_frames_numframes=null;


	function getParameters_frames_numframes() {
		return this.Parameters_frames_numframes;
	}
	this.getParameters_frames_numframes=getParameters_frames_numframes;


	function setParameters_frames_numframes(v){
		this.Parameters_frames_numframes=v;
	}
	this.setParameters_frames_numframes=setParameters_frames_numframes;

	this.Parameters_numgates=null;


	function getParameters_numgates() {
		return this.Parameters_numgates;
	}
	this.getParameters_numgates=getParameters_numgates;


	function setParameters_numgates(v){
		this.Parameters_numgates=v;
	}
	this.setParameters_numgates=setParameters_numgates;

	this.Parameters_planeseparation=null;


	function getParameters_planeseparation() {
		return this.Parameters_planeseparation;
	}
	this.getParameters_planeseparation=getParameters_planeseparation;


	function setParameters_planeseparation(v){
		this.Parameters_planeseparation=v;
	}
	this.setParameters_planeseparation=setParameters_planeseparation;

	this.Parameters_binsize=null;


	function getParameters_binsize() {
		return this.Parameters_binsize;
	}
	this.getParameters_binsize=getParameters_binsize;


	function setParameters_binsize(v){
		this.Parameters_binsize=v;
	}
	this.setParameters_binsize=setParameters_binsize;

	this.Parameters_datatype=null;


	function getParameters_datatype() {
		return this.Parameters_datatype;
	}
	this.getParameters_datatype=getParameters_datatype;


	function setParameters_datatype(v){
		this.Parameters_datatype=v;
	}
	this.setParameters_datatype=setParameters_datatype;

	this.Parameters_dimensions_x=null;


	function getParameters_dimensions_x() {
		return this.Parameters_dimensions_x;
	}
	this.getParameters_dimensions_x=getParameters_dimensions_x;


	function setParameters_dimensions_x(v){
		this.Parameters_dimensions_x=v;
	}
	this.setParameters_dimensions_x=setParameters_dimensions_x;

	this.Parameters_dimensions_y=null;


	function getParameters_dimensions_y() {
		return this.Parameters_dimensions_y;
	}
	this.getParameters_dimensions_y=getParameters_dimensions_y;


	function setParameters_dimensions_y(v){
		this.Parameters_dimensions_y=v;
	}
	this.setParameters_dimensions_y=setParameters_dimensions_y;

	this.Parameters_dimensions_z=null;


	function getParameters_dimensions_z() {
		return this.Parameters_dimensions_z;
	}
	this.getParameters_dimensions_z=getParameters_dimensions_z;


	function setParameters_dimensions_z(v){
		this.Parameters_dimensions_z=v;
	}
	this.setParameters_dimensions_z=setParameters_dimensions_z;

	this.Parameters_dimensions_num=null;


	function getParameters_dimensions_num() {
		return this.Parameters_dimensions_num;
	}
	this.getParameters_dimensions_num=getParameters_dimensions_num;


	function setParameters_dimensions_num(v){
		this.Parameters_dimensions_num=v;
	}
	this.setParameters_dimensions_num=setParameters_dimensions_num;

	this.Parameters_offset_x=null;


	function getParameters_offset_x() {
		return this.Parameters_offset_x;
	}
	this.getParameters_offset_x=getParameters_offset_x;


	function setParameters_offset_x(v){
		this.Parameters_offset_x=v;
	}
	this.setParameters_offset_x=setParameters_offset_x;

	this.Parameters_offset_y=null;


	function getParameters_offset_y() {
		return this.Parameters_offset_y;
	}
	this.getParameters_offset_y=getParameters_offset_y;


	function setParameters_offset_y(v){
		this.Parameters_offset_y=v;
	}
	this.setParameters_offset_y=setParameters_offset_y;

	this.Parameters_offset_z=null;


	function getParameters_offset_z() {
		return this.Parameters_offset_z;
	}
	this.getParameters_offset_z=getParameters_offset_z;


	function setParameters_offset_z(v){
		this.Parameters_offset_z=v;
	}
	this.setParameters_offset_z=setParameters_offset_z;

	this.Parameters_reconzoom=null;


	function getParameters_reconzoom() {
		return this.Parameters_reconzoom;
	}
	this.getParameters_reconzoom=getParameters_reconzoom;


	function setParameters_reconzoom(v){
		this.Parameters_reconzoom=v;
	}
	this.setParameters_reconzoom=setParameters_reconzoom;

	this.Parameters_pixelsize_x=null;


	function getParameters_pixelsize_x() {
		return this.Parameters_pixelsize_x;
	}
	this.getParameters_pixelsize_x=getParameters_pixelsize_x;


	function setParameters_pixelsize_x(v){
		this.Parameters_pixelsize_x=v;
	}
	this.setParameters_pixelsize_x=setParameters_pixelsize_x;

	this.Parameters_pixelsize_y=null;


	function getParameters_pixelsize_y() {
		return this.Parameters_pixelsize_y;
	}
	this.getParameters_pixelsize_y=getParameters_pixelsize_y;


	function setParameters_pixelsize_y(v){
		this.Parameters_pixelsize_y=v;
	}
	this.setParameters_pixelsize_y=setParameters_pixelsize_y;

	this.Parameters_pixelsize_z=null;


	function getParameters_pixelsize_z() {
		return this.Parameters_pixelsize_z;
	}
	this.getParameters_pixelsize_z=getParameters_pixelsize_z;


	function setParameters_pixelsize_z(v){
		this.Parameters_pixelsize_z=v;
	}
	this.setParameters_pixelsize_z=setParameters_pixelsize_z;

	this.Parameters_filtercode=null;


	function getParameters_filtercode() {
		return this.Parameters_filtercode;
	}
	this.getParameters_filtercode=getParameters_filtercode;


	function setParameters_filtercode(v){
		this.Parameters_filtercode=v;
	}
	this.setParameters_filtercode=setParameters_filtercode;

	this.Parameters_resolution_x=null;


	function getParameters_resolution_x() {
		return this.Parameters_resolution_x;
	}
	this.getParameters_resolution_x=getParameters_resolution_x;


	function setParameters_resolution_x(v){
		this.Parameters_resolution_x=v;
	}
	this.setParameters_resolution_x=setParameters_resolution_x;

	this.Parameters_resolution_y=null;


	function getParameters_resolution_y() {
		return this.Parameters_resolution_y;
	}
	this.getParameters_resolution_y=getParameters_resolution_y;


	function setParameters_resolution_y(v){
		this.Parameters_resolution_y=v;
	}
	this.setParameters_resolution_y=setParameters_resolution_y;

	this.Parameters_resolution_z=null;


	function getParameters_resolution_z() {
		return this.Parameters_resolution_z;
	}
	this.getParameters_resolution_z=getParameters_resolution_z;


	function setParameters_resolution_z(v){
		this.Parameters_resolution_z=v;
	}
	this.setParameters_resolution_z=setParameters_resolution_z;

	this.Parameters_numrelements=null;


	function getParameters_numrelements() {
		return this.Parameters_numrelements;
	}
	this.getParameters_numrelements=getParameters_numrelements;


	function setParameters_numrelements(v){
		this.Parameters_numrelements=v;
	}
	this.setParameters_numrelements=setParameters_numrelements;

	this.Parameters_numangles=null;


	function getParameters_numangles() {
		return this.Parameters_numangles;
	}
	this.getParameters_numangles=getParameters_numangles;


	function setParameters_numangles(v){
		this.Parameters_numangles=v;
	}
	this.setParameters_numangles=setParameters_numangles;

	this.Parameters_zrotationangle=null;


	function getParameters_zrotationangle() {
		return this.Parameters_zrotationangle;
	}
	this.getParameters_zrotationangle=getParameters_zrotationangle;


	function setParameters_zrotationangle(v){
		this.Parameters_zrotationangle=v;
	}
	this.setParameters_zrotationangle=setParameters_zrotationangle;

	this.Parameters_processingcode=null;


	function getParameters_processingcode() {
		return this.Parameters_processingcode;
	}
	this.getParameters_processingcode=getParameters_processingcode;


	function setParameters_processingcode(v){
		this.Parameters_processingcode=v;
	}
	this.setParameters_processingcode=setParameters_processingcode;

	this.Parameters_gateduration=null;


	function getParameters_gateduration() {
		return this.Parameters_gateduration;
	}
	this.getParameters_gateduration=getParameters_gateduration;


	function setParameters_gateduration(v){
		this.Parameters_gateduration=v;
	}
	this.setParameters_gateduration=setParameters_gateduration;

	this.Parameters_rwaveoffset=null;


	function getParameters_rwaveoffset() {
		return this.Parameters_rwaveoffset;
	}
	this.getParameters_rwaveoffset=getParameters_rwaveoffset;


	function setParameters_rwaveoffset(v){
		this.Parameters_rwaveoffset=v;
	}
	this.setParameters_rwaveoffset=setParameters_rwaveoffset;

	this.Parameters_numacceptedbeats=null;


	function getParameters_numacceptedbeats() {
		return this.Parameters_numacceptedbeats;
	}
	this.getParameters_numacceptedbeats=getParameters_numacceptedbeats;


	function setParameters_numacceptedbeats(v){
		this.Parameters_numacceptedbeats=v;
	}
	this.setParameters_numacceptedbeats=setParameters_numacceptedbeats;

	this.Parameters_filter_cutoff=null;


	function getParameters_filter_cutoff() {
		return this.Parameters_filter_cutoff;
	}
	this.getParameters_filter_cutoff=getParameters_filter_cutoff;


	function setParameters_filter_cutoff(v){
		this.Parameters_filter_cutoff=v;
	}
	this.setParameters_filter_cutoff=setParameters_filter_cutoff;

	this.Parameters_annotation=null;


	function getParameters_annotation() {
		return this.Parameters_annotation;
	}
	this.getParameters_annotation=getParameters_annotation;


	function setParameters_annotation(v){
		this.Parameters_annotation=v;
	}
	this.setParameters_annotation=setParameters_annotation;

	this.Parameters_mt11=null;


	function getParameters_mt11() {
		return this.Parameters_mt11;
	}
	this.getParameters_mt11=getParameters_mt11;


	function setParameters_mt11(v){
		this.Parameters_mt11=v;
	}
	this.setParameters_mt11=setParameters_mt11;

	this.Parameters_mt12=null;


	function getParameters_mt12() {
		return this.Parameters_mt12;
	}
	this.getParameters_mt12=getParameters_mt12;


	function setParameters_mt12(v){
		this.Parameters_mt12=v;
	}
	this.setParameters_mt12=setParameters_mt12;

	this.Parameters_mt13=null;


	function getParameters_mt13() {
		return this.Parameters_mt13;
	}
	this.getParameters_mt13=getParameters_mt13;


	function setParameters_mt13(v){
		this.Parameters_mt13=v;
	}
	this.setParameters_mt13=setParameters_mt13;

	this.Parameters_mt14=null;


	function getParameters_mt14() {
		return this.Parameters_mt14;
	}
	this.getParameters_mt14=getParameters_mt14;


	function setParameters_mt14(v){
		this.Parameters_mt14=v;
	}
	this.setParameters_mt14=setParameters_mt14;

	this.Parameters_mt21=null;


	function getParameters_mt21() {
		return this.Parameters_mt21;
	}
	this.getParameters_mt21=getParameters_mt21;


	function setParameters_mt21(v){
		this.Parameters_mt21=v;
	}
	this.setParameters_mt21=setParameters_mt21;

	this.Parameters_mt22=null;


	function getParameters_mt22() {
		return this.Parameters_mt22;
	}
	this.getParameters_mt22=getParameters_mt22;


	function setParameters_mt22(v){
		this.Parameters_mt22=v;
	}
	this.setParameters_mt22=setParameters_mt22;

	this.Parameters_mt23=null;


	function getParameters_mt23() {
		return this.Parameters_mt23;
	}
	this.getParameters_mt23=getParameters_mt23;


	function setParameters_mt23(v){
		this.Parameters_mt23=v;
	}
	this.setParameters_mt23=setParameters_mt23;

	this.Parameters_mt24=null;


	function getParameters_mt24() {
		return this.Parameters_mt24;
	}
	this.getParameters_mt24=getParameters_mt24;


	function setParameters_mt24(v){
		this.Parameters_mt24=v;
	}
	this.setParameters_mt24=setParameters_mt24;

	this.Parameters_mt31=null;


	function getParameters_mt31() {
		return this.Parameters_mt31;
	}
	this.getParameters_mt31=getParameters_mt31;


	function setParameters_mt31(v){
		this.Parameters_mt31=v;
	}
	this.setParameters_mt31=setParameters_mt31;

	this.Parameters_mt32=null;


	function getParameters_mt32() {
		return this.Parameters_mt32;
	}
	this.getParameters_mt32=getParameters_mt32;


	function setParameters_mt32(v){
		this.Parameters_mt32=v;
	}
	this.setParameters_mt32=setParameters_mt32;

	this.Parameters_mt33=null;


	function getParameters_mt33() {
		return this.Parameters_mt33;
	}
	this.getParameters_mt33=getParameters_mt33;


	function setParameters_mt33(v){
		this.Parameters_mt33=v;
	}
	this.setParameters_mt33=setParameters_mt33;

	this.Parameters_mt34=null;


	function getParameters_mt34() {
		return this.Parameters_mt34;
	}
	this.getParameters_mt34=getParameters_mt34;


	function setParameters_mt34(v){
		this.Parameters_mt34=v;
	}
	this.setParameters_mt34=setParameters_mt34;

	this.Parameters_rfilter_cutoff=null;


	function getParameters_rfilter_cutoff() {
		return this.Parameters_rfilter_cutoff;
	}
	this.getParameters_rfilter_cutoff=getParameters_rfilter_cutoff;


	function setParameters_rfilter_cutoff(v){
		this.Parameters_rfilter_cutoff=v;
	}
	this.setParameters_rfilter_cutoff=setParameters_rfilter_cutoff;

	this.Parameters_rfilter_resolution=null;


	function getParameters_rfilter_resolution() {
		return this.Parameters_rfilter_resolution;
	}
	this.getParameters_rfilter_resolution=getParameters_rfilter_resolution;


	function setParameters_rfilter_resolution(v){
		this.Parameters_rfilter_resolution=v;
	}
	this.setParameters_rfilter_resolution=setParameters_rfilter_resolution;

	this.Parameters_rfilter_code=null;


	function getParameters_rfilter_code() {
		return this.Parameters_rfilter_code;
	}
	this.getParameters_rfilter_code=getParameters_rfilter_code;


	function setParameters_rfilter_code(v){
		this.Parameters_rfilter_code=v;
	}
	this.setParameters_rfilter_code=setParameters_rfilter_code;

	this.Parameters_rfilter_order=null;


	function getParameters_rfilter_order() {
		return this.Parameters_rfilter_order;
	}
	this.getParameters_rfilter_order=getParameters_rfilter_order;


	function setParameters_rfilter_order(v){
		this.Parameters_rfilter_order=v;
	}
	this.setParameters_rfilter_order=setParameters_rfilter_order;

	this.Parameters_zfilter_cutoff=null;


	function getParameters_zfilter_cutoff() {
		return this.Parameters_zfilter_cutoff;
	}
	this.getParameters_zfilter_cutoff=getParameters_zfilter_cutoff;


	function setParameters_zfilter_cutoff(v){
		this.Parameters_zfilter_cutoff=v;
	}
	this.setParameters_zfilter_cutoff=setParameters_zfilter_cutoff;

	this.Parameters_zfilter_resolution=null;


	function getParameters_zfilter_resolution() {
		return this.Parameters_zfilter_resolution;
	}
	this.getParameters_zfilter_resolution=getParameters_zfilter_resolution;


	function setParameters_zfilter_resolution(v){
		this.Parameters_zfilter_resolution=v;
	}
	this.setParameters_zfilter_resolution=setParameters_zfilter_resolution;

	this.Parameters_zfilter_code=null;


	function getParameters_zfilter_code() {
		return this.Parameters_zfilter_code;
	}
	this.getParameters_zfilter_code=getParameters_zfilter_code;


	function setParameters_zfilter_code(v){
		this.Parameters_zfilter_code=v;
	}
	this.setParameters_zfilter_code=setParameters_zfilter_code;

	this.Parameters_zfilter_order=null;


	function getParameters_zfilter_order() {
		return this.Parameters_zfilter_order;
	}
	this.getParameters_zfilter_order=getParameters_zfilter_order;


	function setParameters_zfilter_order(v){
		this.Parameters_zfilter_order=v;
	}
	this.setParameters_zfilter_order=setParameters_zfilter_order;

	this.Parameters_scattertype=null;


	function getParameters_scattertype() {
		return this.Parameters_scattertype;
	}
	this.getParameters_scattertype=getParameters_scattertype;


	function setParameters_scattertype(v){
		this.Parameters_scattertype=v;
	}
	this.setParameters_scattertype=setParameters_scattertype;

	this.Parameters_recontype=null;


	function getParameters_recontype() {
		return this.Parameters_recontype;
	}
	this.getParameters_recontype=getParameters_recontype;


	function setParameters_recontype(v){
		this.Parameters_recontype=v;
	}
	this.setParameters_recontype=setParameters_recontype;

	this.Parameters_reconviews=null;


	function getParameters_reconviews() {
		return this.Parameters_reconviews;
	}
	this.getParameters_reconviews=getParameters_reconviews;


	function setParameters_reconviews(v){
		this.Parameters_reconviews=v;
	}
	this.setParameters_reconviews=setParameters_reconviews;

	this.Parameters_bedposition=null;


	function getParameters_bedposition() {
		return this.Parameters_bedposition;
	}
	this.getParameters_bedposition=getParameters_bedposition;


	function setParameters_bedposition(v){
		this.Parameters_bedposition=v;
	}
	this.setParameters_bedposition=setParameters_bedposition;

	this.Parameters_ecatcalibrationfactor=null;


	function getParameters_ecatcalibrationfactor() {
		return this.Parameters_ecatcalibrationfactor;
	}
	this.getParameters_ecatcalibrationfactor=getParameters_ecatcalibrationfactor;


	function setParameters_ecatcalibrationfactor(v){
		this.Parameters_ecatcalibrationfactor=v;
	}
	this.setParameters_ecatcalibrationfactor=setParameters_ecatcalibrationfactor;

	this.Ecatvalidation=null;


	function getEcatvalidation() {
		return this.Ecatvalidation;
	}
	this.getEcatvalidation=getEcatvalidation;


	function setEcatvalidation(v){
		this.Ecatvalidation=v;
	}
	this.setEcatvalidation=setEcatvalidation;

	this.Ecatvalidation_status=null;


	function getEcatvalidation_status() {
		return this.Ecatvalidation_status;
	}
	this.getEcatvalidation_status=getEcatvalidation_status;


	function setEcatvalidation_status(v){
		this.Ecatvalidation_status=v;
	}
	this.setEcatvalidation_status=setEcatvalidation_status;


	this.isEcatvalidation_status=function(defaultValue) {
		if(this.Ecatvalidation_status==null)return defaultValue;
		if(this.Ecatvalidation_status=="1" || this.Ecatvalidation_status==true)return true;
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
			if(xmlPath=="parameters/orientation"){
				return this.Parameters_orientation ;
			} else 
			if(xmlPath=="parameters/addParam"){
				return this.Parameters_addparam ;
			} else 
			if(xmlPath.startsWith("parameters/addParam")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Parameters_addparam ;
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

				for(var whereCount=0;whereCount<this.Parameters_addparam.length;whereCount++){

					var tempValue=this.Parameters_addparam[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_addparam[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_addparam;
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
			if(xmlPath=="parameters/originalFileName"){
				return this.Parameters_originalfilename ;
			} else 
			if(xmlPath=="parameters/systemType"){
				return this.Parameters_systemtype ;
			} else 
			if(xmlPath=="parameters/fileType"){
				return this.Parameters_filetype ;
			} else 
			if(xmlPath=="parameters/transaxialFOV"){
				return this.Parameters_transaxialfov ;
			} else 
			if(xmlPath=="parameters/acqType"){
				return this.Parameters_acqtype ;
			} else 
			if(xmlPath=="parameters/facility"){
				return this.Parameters_facility ;
			} else 
			if(xmlPath=="parameters/numPlanes"){
				return this.Parameters_numplanes ;
			} else 
			if(xmlPath=="parameters/frames/frame"){
				return this.Parameters_frames_frame ;
			} else 
			if(xmlPath.startsWith("parameters/frames/frame")){
				xmlPath=xmlPath.substring(23);
				if(xmlPath=="")return this.Parameters_frames_frame ;
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

				for(var whereCount=0;whereCount<this.Parameters_frames_frame.length;whereCount++){

					var tempValue=this.Parameters_frames_frame[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_frames_frame[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_frames_frame;
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
			if(xmlPath=="parameters/frames/numFrames"){
				return this.Parameters_frames_numframes ;
			} else 
			if(xmlPath=="parameters/numGates"){
				return this.Parameters_numgates ;
			} else 
			if(xmlPath=="parameters/planeSeparation"){
				return this.Parameters_planeseparation ;
			} else 
			if(xmlPath=="parameters/binSize"){
				return this.Parameters_binsize ;
			} else 
			if(xmlPath=="parameters/dataType"){
				return this.Parameters_datatype ;
			} else 
			if(xmlPath=="parameters/dimensions/x"){
				return this.Parameters_dimensions_x ;
			} else 
			if(xmlPath=="parameters/dimensions/y"){
				return this.Parameters_dimensions_y ;
			} else 
			if(xmlPath=="parameters/dimensions/z"){
				return this.Parameters_dimensions_z ;
			} else 
			if(xmlPath=="parameters/dimensions/num"){
				return this.Parameters_dimensions_num ;
			} else 
			if(xmlPath=="parameters/offset/x"){
				return this.Parameters_offset_x ;
			} else 
			if(xmlPath=="parameters/offset/y"){
				return this.Parameters_offset_y ;
			} else 
			if(xmlPath=="parameters/offset/z"){
				return this.Parameters_offset_z ;
			} else 
			if(xmlPath=="parameters/reconZoom"){
				return this.Parameters_reconzoom ;
			} else 
			if(xmlPath=="parameters/pixelSize/x"){
				return this.Parameters_pixelsize_x ;
			} else 
			if(xmlPath=="parameters/pixelSize/y"){
				return this.Parameters_pixelsize_y ;
			} else 
			if(xmlPath=="parameters/pixelSize/z"){
				return this.Parameters_pixelsize_z ;
			} else 
			if(xmlPath=="parameters/filterCode"){
				return this.Parameters_filtercode ;
			} else 
			if(xmlPath=="parameters/resolution/x"){
				return this.Parameters_resolution_x ;
			} else 
			if(xmlPath=="parameters/resolution/y"){
				return this.Parameters_resolution_y ;
			} else 
			if(xmlPath=="parameters/resolution/z"){
				return this.Parameters_resolution_z ;
			} else 
			if(xmlPath=="parameters/numRElements"){
				return this.Parameters_numrelements ;
			} else 
			if(xmlPath=="parameters/numAngles"){
				return this.Parameters_numangles ;
			} else 
			if(xmlPath=="parameters/ZRotationAngle"){
				return this.Parameters_zrotationangle ;
			} else 
			if(xmlPath=="parameters/processingCode"){
				return this.Parameters_processingcode ;
			} else 
			if(xmlPath=="parameters/gateDuration"){
				return this.Parameters_gateduration ;
			} else 
			if(xmlPath=="parameters/rWaveOffset"){
				return this.Parameters_rwaveoffset ;
			} else 
			if(xmlPath=="parameters/numAcceptedBeats"){
				return this.Parameters_numacceptedbeats ;
			} else 
			if(xmlPath=="parameters/filter/cutoff"){
				return this.Parameters_filter_cutoff ;
			} else 
			if(xmlPath=="parameters/annotation"){
				return this.Parameters_annotation ;
			} else 
			if(xmlPath=="parameters/MT_1_1"){
				return this.Parameters_mt11 ;
			} else 
			if(xmlPath=="parameters/MT_1_2"){
				return this.Parameters_mt12 ;
			} else 
			if(xmlPath=="parameters/MT_1_3"){
				return this.Parameters_mt13 ;
			} else 
			if(xmlPath=="parameters/MT_1_4"){
				return this.Parameters_mt14 ;
			} else 
			if(xmlPath=="parameters/MT_2_1"){
				return this.Parameters_mt21 ;
			} else 
			if(xmlPath=="parameters/MT_2_2"){
				return this.Parameters_mt22 ;
			} else 
			if(xmlPath=="parameters/MT_2_3"){
				return this.Parameters_mt23 ;
			} else 
			if(xmlPath=="parameters/MT_2_4"){
				return this.Parameters_mt24 ;
			} else 
			if(xmlPath=="parameters/MT_3_1"){
				return this.Parameters_mt31 ;
			} else 
			if(xmlPath=="parameters/MT_3_2"){
				return this.Parameters_mt32 ;
			} else 
			if(xmlPath=="parameters/MT_3_3"){
				return this.Parameters_mt33 ;
			} else 
			if(xmlPath=="parameters/MT_3_4"){
				return this.Parameters_mt34 ;
			} else 
			if(xmlPath=="parameters/RFilter/cutoff"){
				return this.Parameters_rfilter_cutoff ;
			} else 
			if(xmlPath=="parameters/RFilter/resolution"){
				return this.Parameters_rfilter_resolution ;
			} else 
			if(xmlPath=="parameters/RFilter/code"){
				return this.Parameters_rfilter_code ;
			} else 
			if(xmlPath=="parameters/RFilter/order"){
				return this.Parameters_rfilter_order ;
			} else 
			if(xmlPath=="parameters/ZFilter/cutoff"){
				return this.Parameters_zfilter_cutoff ;
			} else 
			if(xmlPath=="parameters/ZFilter/resolution"){
				return this.Parameters_zfilter_resolution ;
			} else 
			if(xmlPath=="parameters/ZFilter/code"){
				return this.Parameters_zfilter_code ;
			} else 
			if(xmlPath=="parameters/ZFilter/order"){
				return this.Parameters_zfilter_order ;
			} else 
			if(xmlPath=="parameters/scatterType"){
				return this.Parameters_scattertype ;
			} else 
			if(xmlPath=="parameters/reconType"){
				return this.Parameters_recontype ;
			} else 
			if(xmlPath=="parameters/reconViews"){
				return this.Parameters_reconviews ;
			} else 
			if(xmlPath=="parameters/bedPosition"){
				return this.Parameters_bedposition ;
			} else 
			if(xmlPath=="parameters/ecatCalibrationFactor"){
				return this.Parameters_ecatcalibrationfactor ;
			} else 
			if(xmlPath=="ecatValidation"){
				return this.Ecatvalidation ;
			} else 
			if(xmlPath=="ecatValidation/status"){
				return this.Ecatvalidation_status ;
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
			if(xmlPath=="parameters/orientation"){
				this.Parameters_orientation=value;
			} else 
			if(xmlPath=="parameters/addParam"){
				this.Parameters_addparam=value;
			} else 
			if(xmlPath.startsWith("parameters/addParam")){
				xmlPath=xmlPath.substring(19);
				if(xmlPath=="")return this.Parameters_addparam ;
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

				for(var whereCount=0;whereCount<this.Parameters_addparam.length;whereCount++){

					var tempValue=this.Parameters_addparam[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_addparam[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_addparam;
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
						newChild= instanciateObject("xnat:addField");//omUtils.js
					}
					this.addParameters_addparam(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="parameters/originalFileName"){
				this.Parameters_originalfilename=value;
			} else 
			if(xmlPath=="parameters/systemType"){
				this.Parameters_systemtype=value;
			} else 
			if(xmlPath=="parameters/fileType"){
				this.Parameters_filetype=value;
			} else 
			if(xmlPath=="parameters/transaxialFOV"){
				this.Parameters_transaxialfov=value;
			} else 
			if(xmlPath=="parameters/acqType"){
				this.Parameters_acqtype=value;
			} else 
			if(xmlPath=="parameters/facility"){
				this.Parameters_facility=value;
			} else 
			if(xmlPath=="parameters/numPlanes"){
				this.Parameters_numplanes=value;
			} else 
			if(xmlPath=="parameters/frames/frame"){
				this.Parameters_frames_frame=value;
			} else 
			if(xmlPath.startsWith("parameters/frames/frame")){
				xmlPath=xmlPath.substring(23);
				if(xmlPath=="")return this.Parameters_frames_frame ;
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

				for(var whereCount=0;whereCount<this.Parameters_frames_frame.length;whereCount++){

					var tempValue=this.Parameters_frames_frame[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Parameters_frames_frame[whereCount]);

					}

				}
				}else{

				whereArray=this.Parameters_frames_frame;
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
						newChild= instanciateObject("xnat:petScanData_frame");//omUtils.js
					}
					this.addParameters_frames_frame(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="parameters/frames/numFrames"){
				this.Parameters_frames_numframes=value;
			} else 
			if(xmlPath=="parameters/numGates"){
				this.Parameters_numgates=value;
			} else 
			if(xmlPath=="parameters/planeSeparation"){
				this.Parameters_planeseparation=value;
			} else 
			if(xmlPath=="parameters/binSize"){
				this.Parameters_binsize=value;
			} else 
			if(xmlPath=="parameters/dataType"){
				this.Parameters_datatype=value;
			} else 
			if(xmlPath=="parameters/dimensions/x"){
				this.Parameters_dimensions_x=value;
			} else 
			if(xmlPath=="parameters/dimensions/y"){
				this.Parameters_dimensions_y=value;
			} else 
			if(xmlPath=="parameters/dimensions/z"){
				this.Parameters_dimensions_z=value;
			} else 
			if(xmlPath=="parameters/dimensions/num"){
				this.Parameters_dimensions_num=value;
			} else 
			if(xmlPath=="parameters/offset/x"){
				this.Parameters_offset_x=value;
			} else 
			if(xmlPath=="parameters/offset/y"){
				this.Parameters_offset_y=value;
			} else 
			if(xmlPath=="parameters/offset/z"){
				this.Parameters_offset_z=value;
			} else 
			if(xmlPath=="parameters/reconZoom"){
				this.Parameters_reconzoom=value;
			} else 
			if(xmlPath=="parameters/pixelSize/x"){
				this.Parameters_pixelsize_x=value;
			} else 
			if(xmlPath=="parameters/pixelSize/y"){
				this.Parameters_pixelsize_y=value;
			} else 
			if(xmlPath=="parameters/pixelSize/z"){
				this.Parameters_pixelsize_z=value;
			} else 
			if(xmlPath=="parameters/filterCode"){
				this.Parameters_filtercode=value;
			} else 
			if(xmlPath=="parameters/resolution/x"){
				this.Parameters_resolution_x=value;
			} else 
			if(xmlPath=="parameters/resolution/y"){
				this.Parameters_resolution_y=value;
			} else 
			if(xmlPath=="parameters/resolution/z"){
				this.Parameters_resolution_z=value;
			} else 
			if(xmlPath=="parameters/numRElements"){
				this.Parameters_numrelements=value;
			} else 
			if(xmlPath=="parameters/numAngles"){
				this.Parameters_numangles=value;
			} else 
			if(xmlPath=="parameters/ZRotationAngle"){
				this.Parameters_zrotationangle=value;
			} else 
			if(xmlPath=="parameters/processingCode"){
				this.Parameters_processingcode=value;
			} else 
			if(xmlPath=="parameters/gateDuration"){
				this.Parameters_gateduration=value;
			} else 
			if(xmlPath=="parameters/rWaveOffset"){
				this.Parameters_rwaveoffset=value;
			} else 
			if(xmlPath=="parameters/numAcceptedBeats"){
				this.Parameters_numacceptedbeats=value;
			} else 
			if(xmlPath=="parameters/filter/cutoff"){
				this.Parameters_filter_cutoff=value;
			} else 
			if(xmlPath=="parameters/annotation"){
				this.Parameters_annotation=value;
			} else 
			if(xmlPath=="parameters/MT_1_1"){
				this.Parameters_mt11=value;
			} else 
			if(xmlPath=="parameters/MT_1_2"){
				this.Parameters_mt12=value;
			} else 
			if(xmlPath=="parameters/MT_1_3"){
				this.Parameters_mt13=value;
			} else 
			if(xmlPath=="parameters/MT_1_4"){
				this.Parameters_mt14=value;
			} else 
			if(xmlPath=="parameters/MT_2_1"){
				this.Parameters_mt21=value;
			} else 
			if(xmlPath=="parameters/MT_2_2"){
				this.Parameters_mt22=value;
			} else 
			if(xmlPath=="parameters/MT_2_3"){
				this.Parameters_mt23=value;
			} else 
			if(xmlPath=="parameters/MT_2_4"){
				this.Parameters_mt24=value;
			} else 
			if(xmlPath=="parameters/MT_3_1"){
				this.Parameters_mt31=value;
			} else 
			if(xmlPath=="parameters/MT_3_2"){
				this.Parameters_mt32=value;
			} else 
			if(xmlPath=="parameters/MT_3_3"){
				this.Parameters_mt33=value;
			} else 
			if(xmlPath=="parameters/MT_3_4"){
				this.Parameters_mt34=value;
			} else 
			if(xmlPath=="parameters/RFilter/cutoff"){
				this.Parameters_rfilter_cutoff=value;
			} else 
			if(xmlPath=="parameters/RFilter/resolution"){
				this.Parameters_rfilter_resolution=value;
			} else 
			if(xmlPath=="parameters/RFilter/code"){
				this.Parameters_rfilter_code=value;
			} else 
			if(xmlPath=="parameters/RFilter/order"){
				this.Parameters_rfilter_order=value;
			} else 
			if(xmlPath=="parameters/ZFilter/cutoff"){
				this.Parameters_zfilter_cutoff=value;
			} else 
			if(xmlPath=="parameters/ZFilter/resolution"){
				this.Parameters_zfilter_resolution=value;
			} else 
			if(xmlPath=="parameters/ZFilter/code"){
				this.Parameters_zfilter_code=value;
			} else 
			if(xmlPath=="parameters/ZFilter/order"){
				this.Parameters_zfilter_order=value;
			} else 
			if(xmlPath=="parameters/scatterType"){
				this.Parameters_scattertype=value;
			} else 
			if(xmlPath=="parameters/reconType"){
				this.Parameters_recontype=value;
			} else 
			if(xmlPath=="parameters/reconViews"){
				this.Parameters_reconviews=value;
			} else 
			if(xmlPath=="parameters/bedPosition"){
				this.Parameters_bedposition=value;
			} else 
			if(xmlPath=="parameters/ecatCalibrationFactor"){
				this.Parameters_ecatcalibrationfactor=value;
			} else 
			if(xmlPath=="ecatValidation"){
				this.Ecatvalidation=value;
			} else 
			if(xmlPath=="ecatValidation/status"){
				this.Ecatvalidation_status=value;
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
		if (xmlPath=="parameters/addParam"){
			this.addParameters_addparam(v);
		}else if (xmlPath=="parameters/frames/frame"){
			this.addParameters_frames_frame(v);
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
		if (xmlPath=="parameters/addParam"){
			return "http://nrg.wustl.edu/xnat:addField";
		}else if (xmlPath=="parameters/frames/frame"){
			return "http://nrg.wustl.edu/xnat:petScanData_frame";
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="parameters/orientation"){
			return "field_data";
		}else if (xmlPath=="parameters/addParam"){
			return "field_NO_CHILD";
		}else if (xmlPath=="parameters/originalFileName"){
			return "field_data";
		}else if (xmlPath=="parameters/systemType"){
			return "field_data";
		}else if (xmlPath=="parameters/fileType"){
			return "field_data";
		}else if (xmlPath=="parameters/transaxialFOV"){
			return "field_data";
		}else if (xmlPath=="parameters/acqType"){
			return "field_data";
		}else if (xmlPath=="parameters/facility"){
			return "field_data";
		}else if (xmlPath=="parameters/numPlanes"){
			return "field_data";
		}else if (xmlPath=="parameters/frames/frame"){
			return "field_multi_reference";
		}else if (xmlPath=="parameters/frames/numFrames"){
			return "field_data";
		}else if (xmlPath=="parameters/numGates"){
			return "field_data";
		}else if (xmlPath=="parameters/planeSeparation"){
			return "field_data";
		}else if (xmlPath=="parameters/binSize"){
			return "field_data";
		}else if (xmlPath=="parameters/dataType"){
			return "field_data";
		}else if (xmlPath=="parameters/dimensions/x"){
			return "field_data";
		}else if (xmlPath=="parameters/dimensions/y"){
			return "field_data";
		}else if (xmlPath=="parameters/dimensions/z"){
			return "field_data";
		}else if (xmlPath=="parameters/dimensions/num"){
			return "field_data";
		}else if (xmlPath=="parameters/offset/x"){
			return "field_data";
		}else if (xmlPath=="parameters/offset/y"){
			return "field_data";
		}else if (xmlPath=="parameters/offset/z"){
			return "field_data";
		}else if (xmlPath=="parameters/reconZoom"){
			return "field_data";
		}else if (xmlPath=="parameters/pixelSize/x"){
			return "field_data";
		}else if (xmlPath=="parameters/pixelSize/y"){
			return "field_data";
		}else if (xmlPath=="parameters/pixelSize/z"){
			return "field_data";
		}else if (xmlPath=="parameters/filterCode"){
			return "field_data";
		}else if (xmlPath=="parameters/resolution/x"){
			return "field_data";
		}else if (xmlPath=="parameters/resolution/y"){
			return "field_data";
		}else if (xmlPath=="parameters/resolution/z"){
			return "field_data";
		}else if (xmlPath=="parameters/numRElements"){
			return "field_data";
		}else if (xmlPath=="parameters/numAngles"){
			return "field_data";
		}else if (xmlPath=="parameters/ZRotationAngle"){
			return "field_data";
		}else if (xmlPath=="parameters/processingCode"){
			return "field_data";
		}else if (xmlPath=="parameters/gateDuration"){
			return "field_data";
		}else if (xmlPath=="parameters/rWaveOffset"){
			return "field_data";
		}else if (xmlPath=="parameters/numAcceptedBeats"){
			return "field_data";
		}else if (xmlPath=="parameters/filter/cutoff"){
			return "field_data";
		}else if (xmlPath=="parameters/annotation"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_1_1"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_1_2"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_1_3"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_1_4"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_2_1"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_2_2"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_2_3"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_2_4"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_3_1"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_3_2"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_3_3"){
			return "field_data";
		}else if (xmlPath=="parameters/MT_3_4"){
			return "field_data";
		}else if (xmlPath=="parameters/RFilter/cutoff"){
			return "field_data";
		}else if (xmlPath=="parameters/RFilter/resolution"){
			return "field_data";
		}else if (xmlPath=="parameters/RFilter/code"){
			return "field_data";
		}else if (xmlPath=="parameters/RFilter/order"){
			return "field_data";
		}else if (xmlPath=="parameters/ZFilter/cutoff"){
			return "field_data";
		}else if (xmlPath=="parameters/ZFilter/resolution"){
			return "field_data";
		}else if (xmlPath=="parameters/ZFilter/code"){
			return "field_data";
		}else if (xmlPath=="parameters/ZFilter/order"){
			return "field_data";
		}else if (xmlPath=="parameters/scatterType"){
			return "field_data";
		}else if (xmlPath=="parameters/reconType"){
			return "field_data";
		}else if (xmlPath=="parameters/reconViews"){
			return "field_data";
		}else if (xmlPath=="parameters/bedPosition"){
			return "field_data";
		}else if (xmlPath=="parameters/ecatCalibrationFactor"){
			return "field_data";
		}else if (xmlPath=="ecatValidation"){
			return "field_data";
		}else if (xmlPath=="ecatValidation/status"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:PETScan";
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
		xmlTxt+="\n</xnat:PETScan>";
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
			if(this.Parameters_zfilter_resolution!=null)
			child0++;
			if(this.Parameters_reconviews!=null)
			child0++;
			if(this.Parameters_zfilter_code!=null)
			child0++;
			if(this.Parameters_rfilter_order!=null)
			child0++;
			if(this.Parameters_rfilter_cutoff!=null)
			child0++;
			if(this.Parameters_transaxialfov!=null)
			child0++;
			if(this.Parameters_numgates!=null)
			child0++;
			if(this.Parameters_filtercode!=null)
			child0++;
			if(this.Parameters_gateduration!=null)
			child0++;
			if(this.Parameters_pixelsize_z!=null)
			child0++;
			if(this.Parameters_pixelsize_y!=null)
			child0++;
			if(this.Parameters_mt14!=null)
			child0++;
			if(this.Parameters_pixelsize_x!=null)
			child0++;
			if(this.Parameters_mt13!=null)
			child0++;
			if(this.Parameters_mt12!=null)
			child0++;
			if(this.Parameters_mt11!=null)
			child0++;
			if(this.Parameters_numplanes!=null)
			child0++;
			child0+=this.Parameters_frames_frame.length;
			if(this.Parameters_filetype!=null)
			child0++;
			if(this.Parameters_datatype!=null)
			child0++;
			if(this.Parameters_dimensions_z!=null)
			child0++;
			if(this.Parameters_frames_numframes!=null)
			child0++;
			if(this.Parameters_dimensions_y!=null)
			child0++;
			if(this.Parameters_dimensions_x!=null)
			child0++;
			if(this.Parameters_numrelements!=null)
			child0++;
			if(this.Parameters_planeseparation!=null)
			child0++;
			if(this.Parameters_rwaveoffset!=null)
			child0++;
			if(this.Parameters_processingcode!=null)
			child0++;
			if(this.Parameters_systemtype!=null)
			child0++;
			if(this.Parameters_mt34!=null)
			child0++;
			if(this.Parameters_zrotationangle!=null)
			child0++;
			if(this.Parameters_bedposition!=null)
			child0++;
			if(this.Parameters_rfilter_resolution!=null)
			child0++;
			if(this.Parameters_mt33!=null)
			child0++;
			if(this.Parameters_reconzoom!=null)
			child0++;
			child0+=this.Parameters_addparam.length;
			if(this.Parameters_mt32!=null)
			child0++;
			if(this.Parameters_mt31!=null)
			child0++;
			if(this.Parameters_facility!=null)
			child0++;
			if(this.Parameters_filter_cutoff!=null)
			child0++;
			if(this.Parameters_numangles!=null)
			child0++;
			if(this.Parameters_numacceptedbeats!=null)
			child0++;
			if(this.Parameters_zfilter_order!=null)
			child0++;
			if(this.Parameters_recontype!=null)
			child0++;
			if(this.Parameters_rfilter_code!=null)
			child0++;
			if(this.Parameters_annotation!=null)
			child0++;
			if(this.Parameters_ecatcalibrationfactor!=null)
			child0++;
			if(this.Parameters_scattertype!=null)
			child0++;
			if(this.Parameters_acqtype!=null)
			child0++;
			if(this.Parameters_offset_z!=null)
			child0++;
			if(this.Parameters_offset_y!=null)
			child0++;
			if(this.Parameters_offset_x!=null)
			child0++;
			if(this.Parameters_resolution_z!=null)
			child0++;
			if(this.Parameters_resolution_y!=null)
			child0++;
			if(this.Parameters_resolution_x!=null)
			child0++;
			if(this.Parameters_mt24!=null)
			child0++;
			if(this.Parameters_mt23!=null)
			child0++;
			if(this.Parameters_mt22!=null)
			child0++;
			if(this.Parameters_orientation!=null)
			child0++;
			if(this.Parameters_mt21!=null)
			child0++;
			if(this.Parameters_dimensions_num!=null)
			child0++;
			if(this.Parameters_binsize!=null)
			child0++;
			if(this.Parameters_originalfilename!=null)
			child0++;
			if(this.Parameters_zfilter_cutoff!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:parameters";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Parameters_orientation!=null){
			xmlTxt+="\n<xnat:orientation";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_orientation;
			xmlTxt+="</xnat:orientation>";
		}
		for(var Parameters_addparamCOUNT=0;Parameters_addparamCOUNT<this.Parameters_addparam.length;Parameters_addparamCOUNT++){
			xmlTxt +="\n<xnat:addParam";
			xmlTxt +=this.Parameters_addparam[Parameters_addparamCOUNT].getXMLAtts();
			if(this.Parameters_addparam[Parameters_addparamCOUNT].xsiType!="xnat:addField"){
				xmlTxt+=" xsi:type=\"" + this.Parameters_addparam[Parameters_addparamCOUNT].xsiType + "\"";
			}
			if (this.Parameters_addparam[Parameters_addparamCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Parameters_addparam[Parameters_addparamCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:addParam>";
			}else {xmlTxt+="/>";}
		}
		if (this.Parameters_originalfilename!=null){
			xmlTxt+="\n<xnat:originalFileName";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_originalfilename.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:originalFileName>";
		}
		if (this.Parameters_systemtype!=null){
			xmlTxt+="\n<xnat:systemType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_systemtype;
			xmlTxt+="</xnat:systemType>";
		}
		if (this.Parameters_filetype!=null){
			xmlTxt+="\n<xnat:fileType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_filetype;
			xmlTxt+="</xnat:fileType>";
		}
		if (this.Parameters_transaxialfov!=null){
			xmlTxt+="\n<xnat:transaxialFOV";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_transaxialfov;
			xmlTxt+="</xnat:transaxialFOV>";
		}
		if (this.Parameters_acqtype!=null){
			xmlTxt+="\n<xnat:acqType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_acqtype;
			xmlTxt+="</xnat:acqType>";
		}
		if (this.Parameters_facility!=null){
			xmlTxt+="\n<xnat:facility";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_facility.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:facility>";
		}
		if (this.Parameters_numplanes!=null){
			xmlTxt+="\n<xnat:numPlanes";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_numplanes;
			xmlTxt+="</xnat:numPlanes>";
		}
		var Parameters_framesATT = ""
		if (this.Parameters_frames_numframes!=null)
			Parameters_framesATT+=" numFrames=\"" + this.Parameters_frames_numframes + "\"";
			var child1=0;
			var att1=0;
			child1+=this.Parameters_frames_frame.length;
			if(this.Parameters_frames_numframes!=null)
			att1++;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:frames";
				xmlTxt+=Parameters_framesATT;
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Parameters_frames_frameCOUNT=0;Parameters_frames_frameCOUNT<this.Parameters_frames_frame.length;Parameters_frames_frameCOUNT++){
			xmlTxt +="\n<xnat:frame";
			xmlTxt +=this.Parameters_frames_frame[Parameters_frames_frameCOUNT].getXMLAtts();
			if(this.Parameters_frames_frame[Parameters_frames_frameCOUNT].xsiType!="xnat:petScanData_frame"){
				xmlTxt+=" xsi:type=\"" + this.Parameters_frames_frame[Parameters_frames_frameCOUNT].xsiType + "\"";
			}
			if (this.Parameters_frames_frame[Parameters_frames_frameCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Parameters_frames_frame[Parameters_frames_frameCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:frame>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:frames>";
			}
			}

		if (this.Parameters_numgates!=null){
			xmlTxt+="\n<xnat:numGates";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_numgates;
			xmlTxt+="</xnat:numGates>";
		}
		if (this.Parameters_planeseparation!=null){
			xmlTxt+="\n<xnat:planeSeparation";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_planeseparation;
			xmlTxt+="</xnat:planeSeparation>";
		}
		if (this.Parameters_binsize!=null){
			xmlTxt+="\n<xnat:binSize";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_binsize;
			xmlTxt+="</xnat:binSize>";
		}
		if (this.Parameters_datatype!=null){
			xmlTxt+="\n<xnat:dataType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_datatype;
			xmlTxt+="</xnat:dataType>";
		}
		var Parameters_dimensionsATT = ""
		if (this.Parameters_dimensions_x!=null)
			Parameters_dimensionsATT+=" x=\"" + this.Parameters_dimensions_x + "\"";
		if (this.Parameters_dimensions_y!=null)
			Parameters_dimensionsATT+=" y=\"" + this.Parameters_dimensions_y + "\"";
		if (this.Parameters_dimensions_z!=null)
			Parameters_dimensionsATT+=" z=\"" + this.Parameters_dimensions_z + "\"";
		if (this.Parameters_dimensions_num!=null)
			Parameters_dimensionsATT+=" num=\"" + this.Parameters_dimensions_num + "\"";
		if(Parameters_dimensionsATT!=""){
			xmlTxt+="\n<xnat:dimensions";
			xmlTxt+=Parameters_dimensionsATT;
			xmlTxt+="/>";
		}

		var Parameters_offsetATT = ""
		if (this.Parameters_offset_x!=null)
			Parameters_offsetATT+=" x=\"" + this.Parameters_offset_x + "\"";
		if (this.Parameters_offset_y!=null)
			Parameters_offsetATT+=" y=\"" + this.Parameters_offset_y + "\"";
		if (this.Parameters_offset_z!=null)
			Parameters_offsetATT+=" z=\"" + this.Parameters_offset_z + "\"";
		if(Parameters_offsetATT!=""){
			xmlTxt+="\n<xnat:offset";
			xmlTxt+=Parameters_offsetATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_reconzoom!=null){
			xmlTxt+="\n<xnat:reconZoom";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_reconzoom;
			xmlTxt+="</xnat:reconZoom>";
		}
		var Parameters_pixelsizeATT = ""
		if (this.Parameters_pixelsize_x!=null)
			Parameters_pixelsizeATT+=" x=\"" + this.Parameters_pixelsize_x + "\"";
		if (this.Parameters_pixelsize_y!=null)
			Parameters_pixelsizeATT+=" y=\"" + this.Parameters_pixelsize_y + "\"";
		if (this.Parameters_pixelsize_z!=null)
			Parameters_pixelsizeATT+=" z=\"" + this.Parameters_pixelsize_z + "\"";
		if(Parameters_pixelsizeATT!=""){
			xmlTxt+="\n<xnat:pixelSize";
			xmlTxt+=Parameters_pixelsizeATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_filtercode!=null){
			xmlTxt+="\n<xnat:filterCode";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_filtercode;
			xmlTxt+="</xnat:filterCode>";
		}
		var Parameters_resolutionATT = ""
		if (this.Parameters_resolution_x!=null)
			Parameters_resolutionATT+=" x=\"" + this.Parameters_resolution_x + "\"";
		if (this.Parameters_resolution_y!=null)
			Parameters_resolutionATT+=" y=\"" + this.Parameters_resolution_y + "\"";
		if (this.Parameters_resolution_z!=null)
			Parameters_resolutionATT+=" z=\"" + this.Parameters_resolution_z + "\"";
		if(Parameters_resolutionATT!=""){
			xmlTxt+="\n<xnat:resolution";
			xmlTxt+=Parameters_resolutionATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_numrelements!=null){
			xmlTxt+="\n<xnat:numRElements";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_numrelements;
			xmlTxt+="</xnat:numRElements>";
		}
		if (this.Parameters_numangles!=null){
			xmlTxt+="\n<xnat:numAngles";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_numangles;
			xmlTxt+="</xnat:numAngles>";
		}
		if (this.Parameters_zrotationangle!=null){
			xmlTxt+="\n<xnat:ZRotationAngle";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_zrotationangle;
			xmlTxt+="</xnat:ZRotationAngle>";
		}
		if (this.Parameters_processingcode!=null){
			xmlTxt+="\n<xnat:processingCode";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_processingcode;
			xmlTxt+="</xnat:processingCode>";
		}
		if (this.Parameters_gateduration!=null){
			xmlTxt+="\n<xnat:gateDuration";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_gateduration;
			xmlTxt+="</xnat:gateDuration>";
		}
		if (this.Parameters_rwaveoffset!=null){
			xmlTxt+="\n<xnat:rWaveOffset";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_rwaveoffset;
			xmlTxt+="</xnat:rWaveOffset>";
		}
		if (this.Parameters_numacceptedbeats!=null){
			xmlTxt+="\n<xnat:numAcceptedBeats";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_numacceptedbeats;
			xmlTxt+="</xnat:numAcceptedBeats>";
		}
		var Parameters_filterATT = ""
		if (this.Parameters_filter_cutoff!=null)
			Parameters_filterATT+=" cutoff=\"" + this.Parameters_filter_cutoff + "\"";
		if(Parameters_filterATT!=""){
			xmlTxt+="\n<xnat:filter";
			xmlTxt+=Parameters_filterATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_annotation!=null){
			xmlTxt+="\n<xnat:annotation";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_annotation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:annotation>";
		}
		if (this.Parameters_mt11!=null){
			xmlTxt+="\n<xnat:MT_1_1";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt11;
			xmlTxt+="</xnat:MT_1_1>";
		}
		if (this.Parameters_mt12!=null){
			xmlTxt+="\n<xnat:MT_1_2";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt12;
			xmlTxt+="</xnat:MT_1_2>";
		}
		if (this.Parameters_mt13!=null){
			xmlTxt+="\n<xnat:MT_1_3";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt13;
			xmlTxt+="</xnat:MT_1_3>";
		}
		if (this.Parameters_mt14!=null){
			xmlTxt+="\n<xnat:MT_1_4";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt14;
			xmlTxt+="</xnat:MT_1_4>";
		}
		if (this.Parameters_mt21!=null){
			xmlTxt+="\n<xnat:MT_2_1";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt21;
			xmlTxt+="</xnat:MT_2_1>";
		}
		if (this.Parameters_mt22!=null){
			xmlTxt+="\n<xnat:MT_2_2";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt22;
			xmlTxt+="</xnat:MT_2_2>";
		}
		if (this.Parameters_mt23!=null){
			xmlTxt+="\n<xnat:MT_2_3";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt23;
			xmlTxt+="</xnat:MT_2_3>";
		}
		if (this.Parameters_mt24!=null){
			xmlTxt+="\n<xnat:MT_2_4";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt24;
			xmlTxt+="</xnat:MT_2_4>";
		}
		if (this.Parameters_mt31!=null){
			xmlTxt+="\n<xnat:MT_3_1";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt31;
			xmlTxt+="</xnat:MT_3_1>";
		}
		if (this.Parameters_mt32!=null){
			xmlTxt+="\n<xnat:MT_3_2";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt32;
			xmlTxt+="</xnat:MT_3_2>";
		}
		if (this.Parameters_mt33!=null){
			xmlTxt+="\n<xnat:MT_3_3";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt33;
			xmlTxt+="</xnat:MT_3_3>";
		}
		if (this.Parameters_mt34!=null){
			xmlTxt+="\n<xnat:MT_3_4";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_mt34;
			xmlTxt+="</xnat:MT_3_4>";
		}
		var Parameters_rfilterATT = ""
		if (this.Parameters_rfilter_cutoff!=null)
			Parameters_rfilterATT+=" cutoff=\"" + this.Parameters_rfilter_cutoff + "\"";
		if (this.Parameters_rfilter_resolution!=null)
			Parameters_rfilterATT+=" resolution=\"" + this.Parameters_rfilter_resolution + "\"";
		if (this.Parameters_rfilter_code!=null)
			Parameters_rfilterATT+=" code=\"" + this.Parameters_rfilter_code + "\"";
		if (this.Parameters_rfilter_order!=null)
			Parameters_rfilterATT+=" order=\"" + this.Parameters_rfilter_order + "\"";
		if(Parameters_rfilterATT!=""){
			xmlTxt+="\n<xnat:RFilter";
			xmlTxt+=Parameters_rfilterATT;
			xmlTxt+="/>";
		}

		var Parameters_zfilterATT = ""
		if (this.Parameters_zfilter_cutoff!=null)
			Parameters_zfilterATT+=" cutoff=\"" + this.Parameters_zfilter_cutoff + "\"";
		if (this.Parameters_zfilter_resolution!=null)
			Parameters_zfilterATT+=" resolution=\"" + this.Parameters_zfilter_resolution + "\"";
		if (this.Parameters_zfilter_code!=null)
			Parameters_zfilterATT+=" code=\"" + this.Parameters_zfilter_code + "\"";
		if (this.Parameters_zfilter_order!=null)
			Parameters_zfilterATT+=" order=\"" + this.Parameters_zfilter_order + "\"";
		if(Parameters_zfilterATT!=""){
			xmlTxt+="\n<xnat:ZFilter";
			xmlTxt+=Parameters_zfilterATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_scattertype!=null){
			xmlTxt+="\n<xnat:scatterType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_scattertype;
			xmlTxt+="</xnat:scatterType>";
		}
		if (this.Parameters_recontype!=null){
			xmlTxt+="\n<xnat:reconType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_recontype;
			xmlTxt+="</xnat:reconType>";
		}
		if (this.Parameters_reconviews!=null){
			xmlTxt+="\n<xnat:reconViews";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_reconviews;
			xmlTxt+="</xnat:reconViews>";
		}
		if (this.Parameters_bedposition!=null){
			xmlTxt+="\n<xnat:bedPosition";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_bedposition;
			xmlTxt+="</xnat:bedPosition>";
		}
		if (this.Parameters_ecatcalibrationfactor!=null){
			xmlTxt+="\n<xnat:ecatCalibrationFactor";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_ecatcalibrationfactor;
			xmlTxt+="</xnat:ecatCalibrationFactor>";
		}
				xmlTxt+="\n</xnat:parameters>";
			}
			}

		var EcatvalidationATT = ""
		if (this.Ecatvalidation_status!=null)
			EcatvalidationATT+=" status=\"" + this.Ecatvalidation_status + "\"";
		if (this.Ecatvalidation!=null){
			xmlTxt+="\n<xnat:ecatValidation";
			xmlTxt+=EcatvalidationATT;
			xmlTxt+=">";
			xmlTxt+=this.Ecatvalidation.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:ecatValidation>";
		}
		else if(EcatvalidationATT!=""){
			xmlTxt+="\n<xnat:ecatValidation";
			xmlTxt+=EcatvalidationATT;
			xmlTxt+="/>";
		}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
			if(this.Parameters_zfilter_resolution!=null) return true;
			if(this.Parameters_reconviews!=null) return true;
			if(this.Parameters_zfilter_code!=null) return true;
			if(this.Parameters_rfilter_order!=null) return true;
			if(this.Parameters_rfilter_cutoff!=null) return true;
			if(this.Parameters_transaxialfov!=null) return true;
			if(this.Parameters_numgates!=null) return true;
			if(this.Parameters_filtercode!=null) return true;
			if(this.Parameters_gateduration!=null) return true;
			if(this.Parameters_pixelsize_z!=null) return true;
			if(this.Parameters_pixelsize_y!=null) return true;
			if(this.Parameters_mt14!=null) return true;
			if(this.Parameters_pixelsize_x!=null) return true;
			if(this.Parameters_mt13!=null) return true;
			if(this.Parameters_mt12!=null) return true;
			if(this.Parameters_mt11!=null) return true;
			if(this.Parameters_numplanes!=null) return true;
			if(this.Parameters_frames_frame.length>0)return true;
			if(this.Parameters_filetype!=null) return true;
			if(this.Parameters_datatype!=null) return true;
			if(this.Parameters_dimensions_z!=null) return true;
			if(this.Parameters_frames_numframes!=null) return true;
			if(this.Parameters_dimensions_y!=null) return true;
			if(this.Parameters_dimensions_x!=null) return true;
			if(this.Parameters_numrelements!=null) return true;
			if(this.Parameters_planeseparation!=null) return true;
			if(this.Parameters_rwaveoffset!=null) return true;
			if(this.Parameters_processingcode!=null) return true;
			if(this.Parameters_systemtype!=null) return true;
			if(this.Parameters_mt34!=null) return true;
			if(this.Parameters_zrotationangle!=null) return true;
			if(this.Parameters_bedposition!=null) return true;
			if(this.Parameters_rfilter_resolution!=null) return true;
			if(this.Parameters_mt33!=null) return true;
			if(this.Parameters_reconzoom!=null) return true;
			if(this.Parameters_addparam.length>0)return true;
			if(this.Parameters_mt32!=null) return true;
			if(this.Parameters_mt31!=null) return true;
			if(this.Parameters_facility!=null) return true;
			if(this.Parameters_filter_cutoff!=null) return true;
			if(this.Parameters_numangles!=null) return true;
			if(this.Parameters_numacceptedbeats!=null) return true;
			if(this.Parameters_zfilter_order!=null) return true;
			if(this.Parameters_recontype!=null) return true;
			if(this.Parameters_rfilter_code!=null) return true;
			if(this.Parameters_annotation!=null) return true;
			if(this.Parameters_ecatcalibrationfactor!=null) return true;
			if(this.Parameters_scattertype!=null) return true;
			if(this.Parameters_acqtype!=null) return true;
			if(this.Parameters_offset_z!=null) return true;
			if(this.Parameters_offset_y!=null) return true;
			if(this.Parameters_offset_x!=null) return true;
			if(this.Parameters_resolution_z!=null) return true;
			if(this.Parameters_resolution_y!=null) return true;
			if(this.Parameters_resolution_x!=null) return true;
			if(this.Parameters_mt24!=null) return true;
			if(this.Parameters_mt23!=null) return true;
			if(this.Parameters_mt22!=null) return true;
			if(this.Parameters_orientation!=null) return true;
			if(this.Parameters_mt21!=null) return true;
			if(this.Parameters_dimensions_num!=null) return true;
			if(this.Parameters_binsize!=null) return true;
			if(this.Parameters_originalfilename!=null) return true;
			if(this.Parameters_zfilter_cutoff!=null) return true;
		if (this.Ecatvalidation_status!=null)
			return true;
		if (this.Ecatvalidation!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
