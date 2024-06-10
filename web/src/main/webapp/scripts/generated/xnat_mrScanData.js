/*
 * web: xnat_mrScanData.js
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

function xnat_mrScanData(){
this.xsiType="xnat:mrScanData";

	this.getSchemaElementName=function(){
		return "mrScanData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:mrScanData";
	}
this.extension=dynamicJSLoad('xnat_imageScanData','generated/xnat_imageScanData.js');

	this.Coil=null;


	function getCoil() {
		return this.Coil;
	}
	this.getCoil=getCoil;


	function setCoil(v){
		this.Coil=v;
	}
	this.setCoil=setCoil;

	this.Fieldstrength=null;


	function getFieldstrength() {
		return this.Fieldstrength;
	}
	this.getFieldstrength=getFieldstrength;


	function setFieldstrength(v){
		this.Fieldstrength=v;
	}
	this.setFieldstrength=setFieldstrength;

	this.Marker=null;


	function getMarker() {
		return this.Marker;
	}
	this.getMarker=getMarker;


	function setMarker(v){
		this.Marker=v;
	}
	this.setMarker=setMarker;

	this.Stabilization=null;


	function getStabilization() {
		return this.Stabilization;
	}
	this.getStabilization=getStabilization;


	function setStabilization(v){
		this.Stabilization=v;
	}
	this.setStabilization=setStabilization;

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

	this.Parameters_matrix_x=null;


	function getParameters_matrix_x() {
		return this.Parameters_matrix_x;
	}
	this.getParameters_matrix_x=getParameters_matrix_x;


	function setParameters_matrix_x(v){
		this.Parameters_matrix_x=v;
	}
	this.setParameters_matrix_x=setParameters_matrix_x;

	this.Parameters_matrix_y=null;


	function getParameters_matrix_y() {
		return this.Parameters_matrix_y;
	}
	this.getParameters_matrix_y=getParameters_matrix_y;


	function setParameters_matrix_y(v){
		this.Parameters_matrix_y=v;
	}
	this.setParameters_matrix_y=setParameters_matrix_y;

	this.Parameters_partitions=null;


	function getParameters_partitions() {
		return this.Parameters_partitions;
	}
	this.getParameters_partitions=getParameters_partitions;


	function setParameters_partitions(v){
		this.Parameters_partitions=v;
	}
	this.setParameters_partitions=setParameters_partitions;

	this.Parameters_tr=null;


	function getParameters_tr() {
		return this.Parameters_tr;
	}
	this.getParameters_tr=getParameters_tr;


	function setParameters_tr(v){
		this.Parameters_tr=v;
	}
	this.setParameters_tr=setParameters_tr;

	this.Parameters_te=null;


	function getParameters_te() {
		return this.Parameters_te;
	}
	this.getParameters_te=getParameters_te;


	function setParameters_te(v){
		this.Parameters_te=v;
	}
	this.setParameters_te=setParameters_te;

	this.Parameters_ti=null;


	function getParameters_ti() {
		return this.Parameters_ti;
	}
	this.getParameters_ti=getParameters_ti;


	function setParameters_ti(v){
		this.Parameters_ti=v;
	}
	this.setParameters_ti=setParameters_ti;

	this.Parameters_flip=null;


	function getParameters_flip() {
		return this.Parameters_flip;
	}
	this.getParameters_flip=getParameters_flip;


	function setParameters_flip(v){
		this.Parameters_flip=v;
	}
	this.setParameters_flip=setParameters_flip;

	this.Parameters_sequence=null;


	function getParameters_sequence() {
		return this.Parameters_sequence;
	}
	this.getParameters_sequence=getParameters_sequence;


	function setParameters_sequence(v){
		this.Parameters_sequence=v;
	}
	this.setParameters_sequence=setParameters_sequence;

	this.Parameters_origin=null;


	function getParameters_origin() {
		return this.Parameters_origin;
	}
	this.getParameters_origin=getParameters_origin;


	function setParameters_origin(v){
		this.Parameters_origin=v;
	}
	this.setParameters_origin=setParameters_origin;

	this.Parameters_imagetype=null;


	function getParameters_imagetype() {
		return this.Parameters_imagetype;
	}
	this.getParameters_imagetype=getParameters_imagetype;


	function setParameters_imagetype(v){
		this.Parameters_imagetype=v;
	}
	this.setParameters_imagetype=setParameters_imagetype;

	this.Parameters_scansequence=null;


	function getParameters_scansequence() {
		return this.Parameters_scansequence;
	}
	this.getParameters_scansequence=getParameters_scansequence;


	function setParameters_scansequence(v){
		this.Parameters_scansequence=v;
	}
	this.setParameters_scansequence=setParameters_scansequence;

	this.Parameters_seqvariant=null;


	function getParameters_seqvariant() {
		return this.Parameters_seqvariant;
	}
	this.getParameters_seqvariant=getParameters_seqvariant;


	function setParameters_seqvariant(v){
		this.Parameters_seqvariant=v;
	}
	this.setParameters_seqvariant=setParameters_seqvariant;

	this.Parameters_scanoptions=null;


	function getParameters_scanoptions() {
		return this.Parameters_scanoptions;
	}
	this.getParameters_scanoptions=getParameters_scanoptions;


	function setParameters_scanoptions(v){
		this.Parameters_scanoptions=v;
	}
	this.setParameters_scanoptions=setParameters_scanoptions;

	this.Parameters_acqtype=null;


	function getParameters_acqtype() {
		return this.Parameters_acqtype;
	}
	this.getParameters_acqtype=getParameters_acqtype;


	function setParameters_acqtype(v){
		this.Parameters_acqtype=v;
	}
	this.setParameters_acqtype=setParameters_acqtype;

	this.Parameters_coil=null;


	function getParameters_coil() {
		return this.Parameters_coil;
	}
	this.getParameters_coil=getParameters_coil;


	function setParameters_coil(v){
		this.Parameters_coil=v;
	}
	this.setParameters_coil=setParameters_coil;

	this.Parameters_dtiacqcount=null;


	function getParameters_dtiacqcount() {
		return this.Parameters_dtiacqcount;
	}
	this.getParameters_dtiacqcount=getParameters_dtiacqcount;


	function setParameters_dtiacqcount(v){
		this.Parameters_dtiacqcount=v;
	}
	this.setParameters_dtiacqcount=setParameters_dtiacqcount;

	this.Parameters_pixelbandwidth=null;


	function getParameters_pixelbandwidth() {
		return this.Parameters_pixelbandwidth;
	}
	this.getParameters_pixelbandwidth=getParameters_pixelbandwidth;


	function setParameters_pixelbandwidth(v){
		this.Parameters_pixelbandwidth=v;
	}
	this.setParameters_pixelbandwidth=setParameters_pixelbandwidth;

	this.Parameters_diffusion_bvalues=null;


	function getParameters_diffusion_bvalues() {
		return this.Parameters_diffusion_bvalues;
	}
	this.getParameters_diffusion_bvalues=getParameters_diffusion_bvalues;


	function setParameters_diffusion_bvalues(v){
		this.Parameters_diffusion_bvalues=v;
	}
	this.setParameters_diffusion_bvalues=setParameters_diffusion_bvalues;

	this.Parameters_diffusion_directionality=null;


	function getParameters_diffusion_directionality() {
		return this.Parameters_diffusion_directionality;
	}
	this.getParameters_diffusion_directionality=getParameters_diffusion_directionality;


	function setParameters_diffusion_directionality(v){
		this.Parameters_diffusion_directionality=v;
	}
	this.setParameters_diffusion_directionality=setParameters_diffusion_directionality;

	this.Parameters_diffusion_orientations=null;


	function getParameters_diffusion_orientations() {
		return this.Parameters_diffusion_orientations;
	}
	this.getParameters_diffusion_orientations=getParameters_diffusion_orientations;


	function setParameters_diffusion_orientations(v){
		this.Parameters_diffusion_orientations=v;
	}
	this.setParameters_diffusion_orientations=setParameters_diffusion_orientations;

	this.Parameters_diffusion_anisotropytype=null;


	function getParameters_diffusion_anisotropytype() {
		return this.Parameters_diffusion_anisotropytype;
	}
	this.getParameters_diffusion_anisotropytype=getParameters_diffusion_anisotropytype;


	function setParameters_diffusion_anisotropytype(v){
		this.Parameters_diffusion_anisotropytype=v;
	}
	this.setParameters_diffusion_anisotropytype=setParameters_diffusion_anisotropytype;
	this.Parameters_addparam =new Array();

	function getParameters_addparam() {
		return this.Parameters_addparam;
	}
	this.getParameters_addparam=getParameters_addparam;


	function addParameters_addparam(v){
		this.Parameters_addparam.push(v);
	}
	this.addParameters_addparam=addParameters_addparam;

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
			if(xmlPath=="coil"){
				return this.Coil ;
			} else 
			if(xmlPath=="fieldStrength"){
				return this.Fieldstrength ;
			} else 
			if(xmlPath=="marker"){
				return this.Marker ;
			} else 
			if(xmlPath=="stabilization"){
				return this.Stabilization ;
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
			if(xmlPath=="parameters/matrix/x"){
				return this.Parameters_matrix_x ;
			} else 
			if(xmlPath=="parameters/matrix/y"){
				return this.Parameters_matrix_y ;
			} else 
			if(xmlPath=="parameters/partitions"){
				return this.Parameters_partitions ;
			} else 
			if(xmlPath=="parameters/tr"){
				return this.Parameters_tr ;
			} else 
			if(xmlPath=="parameters/te"){
				return this.Parameters_te ;
			} else 
			if(xmlPath=="parameters/ti"){
				return this.Parameters_ti ;
			} else 
			if(xmlPath=="parameters/flip"){
				return this.Parameters_flip ;
			} else 
			if(xmlPath=="parameters/sequence"){
				return this.Parameters_sequence ;
			} else 
			if(xmlPath=="parameters/origin"){
				return this.Parameters_origin ;
			} else 
			if(xmlPath=="parameters/imageType"){
				return this.Parameters_imagetype ;
			} else 
			if(xmlPath=="parameters/scanSequence"){
				return this.Parameters_scansequence ;
			} else 
			if(xmlPath=="parameters/seqVariant"){
				return this.Parameters_seqvariant ;
			} else 
			if(xmlPath=="parameters/scanOptions"){
				return this.Parameters_scanoptions ;
			} else 
			if(xmlPath=="parameters/acqType"){
				return this.Parameters_acqtype ;
			} else 
			if(xmlPath=="parameters/coil"){
				return this.Parameters_coil ;
			} else 
			if(xmlPath=="parameters/dtiAcqCount"){
				return this.Parameters_dtiacqcount ;
			} else 
			if(xmlPath=="parameters/pixelBandwidth"){
				return this.Parameters_pixelbandwidth ;
			} else 
			if(xmlPath=="parameters/diffusion/bValues"){
				return this.Parameters_diffusion_bvalues ;
			} else 
			if(xmlPath=="parameters/diffusion/directionality"){
				return this.Parameters_diffusion_directionality ;
			} else 
			if(xmlPath=="parameters/diffusion/orientations"){
				return this.Parameters_diffusion_orientations ;
			} else 
			if(xmlPath=="parameters/diffusion/anisotropyType"){
				return this.Parameters_diffusion_anisotropytype ;
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
			if(xmlPath=="coil"){
				this.Coil=value;
			} else 
			if(xmlPath=="fieldStrength"){
				this.Fieldstrength=value;
			} else 
			if(xmlPath=="marker"){
				this.Marker=value;
			} else 
			if(xmlPath=="stabilization"){
				this.Stabilization=value;
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
			if(xmlPath=="parameters/matrix/x"){
				this.Parameters_matrix_x=value;
			} else 
			if(xmlPath=="parameters/matrix/y"){
				this.Parameters_matrix_y=value;
			} else 
			if(xmlPath=="parameters/partitions"){
				this.Parameters_partitions=value;
			} else 
			if(xmlPath=="parameters/tr"){
				this.Parameters_tr=value;
			} else 
			if(xmlPath=="parameters/te"){
				this.Parameters_te=value;
			} else 
			if(xmlPath=="parameters/ti"){
				this.Parameters_ti=value;
			} else 
			if(xmlPath=="parameters/flip"){
				this.Parameters_flip=value;
			} else 
			if(xmlPath=="parameters/sequence"){
				this.Parameters_sequence=value;
			} else 
			if(xmlPath=="parameters/origin"){
				this.Parameters_origin=value;
			} else 
			if(xmlPath=="parameters/imageType"){
				this.Parameters_imagetype=value;
			} else 
			if(xmlPath=="parameters/scanSequence"){
				this.Parameters_scansequence=value;
			} else 
			if(xmlPath=="parameters/seqVariant"){
				this.Parameters_seqvariant=value;
			} else 
			if(xmlPath=="parameters/scanOptions"){
				this.Parameters_scanoptions=value;
			} else 
			if(xmlPath=="parameters/acqType"){
				this.Parameters_acqtype=value;
			} else 
			if(xmlPath=="parameters/coil"){
				this.Parameters_coil=value;
			} else 
			if(xmlPath=="parameters/dtiAcqCount"){
				this.Parameters_dtiacqcount=value;
			} else 
			if(xmlPath=="parameters/pixelBandwidth"){
				this.Parameters_pixelbandwidth=value;
			} else 
			if(xmlPath=="parameters/diffusion/bValues"){
				this.Parameters_diffusion_bvalues=value;
			} else 
			if(xmlPath=="parameters/diffusion/directionality"){
				this.Parameters_diffusion_directionality=value;
			} else 
			if(xmlPath=="parameters/diffusion/orientations"){
				this.Parameters_diffusion_orientations=value;
			} else 
			if(xmlPath=="parameters/diffusion/anisotropyType"){
				this.Parameters_diffusion_anisotropytype=value;
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
		if (xmlPath=="parameters/addParam"){
			this.addParameters_addparam(v);
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
		}
		else{
			return this.extension.getReferenceFieldName(xmlPath);
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="coil"){
			return "field_data";
		}else if (xmlPath=="fieldStrength"){
			return "field_data";
		}else if (xmlPath=="marker"){
			return "field_data";
		}else if (xmlPath=="stabilization"){
			return "field_data";
		}else if (xmlPath=="parameters/voxelRes/units"){
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
		}else if (xmlPath=="parameters/matrix/x"){
			return "field_data";
		}else if (xmlPath=="parameters/matrix/y"){
			return "field_data";
		}else if (xmlPath=="parameters/partitions"){
			return "field_data";
		}else if (xmlPath=="parameters/tr"){
			return "field_data";
		}else if (xmlPath=="parameters/te"){
			return "field_data";
		}else if (xmlPath=="parameters/ti"){
			return "field_data";
		}else if (xmlPath=="parameters/flip"){
			return "field_data";
		}else if (xmlPath=="parameters/sequence"){
			return "field_data";
		}else if (xmlPath=="parameters/origin"){
			return "field_data";
		}else if (xmlPath=="parameters/imageType"){
			return "field_data";
		}else if (xmlPath=="parameters/scanSequence"){
			return "field_data";
		}else if (xmlPath=="parameters/seqVariant"){
			return "field_data";
		}else if (xmlPath=="parameters/scanOptions"){
			return "field_data";
		}else if (xmlPath=="parameters/acqType"){
			return "field_data";
		}else if (xmlPath=="parameters/coil"){
			return "field_data";
		}else if (xmlPath=="parameters/dtiAcqCount"){
			return "field_data";
		}else if (xmlPath=="parameters/pixelBandwidth"){
			return "field_data";
		}else if (xmlPath=="parameters/diffusion/bValues"){
			return "field_data";
		}else if (xmlPath=="parameters/diffusion/directionality"){
			return "field_data";
		}else if (xmlPath=="parameters/diffusion/orientations"){
			return "field_data";
		}else if (xmlPath=="parameters/diffusion/anisotropyType"){
			return "field_data";
		}else if (xmlPath=="parameters/addParam"){
			return "field_NO_CHILD";
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
		xmlTxt+="\n<xnat:MRScan";
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
		xmlTxt+="\n</xnat:MRScan>";
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
		if (this.Coil!=null){
			xmlTxt+="\n<xnat:coil";
			xmlTxt+=">";
			xmlTxt+=this.Coil.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:coil>";
		}
		if (this.Fieldstrength!=null){
			xmlTxt+="\n<xnat:fieldStrength";
			xmlTxt+=">";
			xmlTxt+=this.Fieldstrength.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:fieldStrength>";
		}
		if (this.Marker!=null){
			xmlTxt+="\n<xnat:marker";
			xmlTxt+=">";
			xmlTxt+=this.Marker.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:marker>";
		}
		if (this.Stabilization!=null){
			xmlTxt+="\n<xnat:stabilization";
			xmlTxt+=">";
			xmlTxt+=this.Stabilization.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:stabilization>";
		}
			var child0=0;
			var att0=0;
			if(this.Parameters_scansequence!=null)
			child0++;
			if(this.Parameters_scanoptions!=null)
			child0++;
			if(this.Parameters_tr!=null)
			child0++;
			if(this.Parameters_matrix_y!=null)
			child0++;
			if(this.Parameters_matrix_x!=null)
			child0++;
			if(this.Parameters_imagetype!=null)
			child0++;
			if(this.Parameters_fov_y!=null)
			child0++;
			if(this.Parameters_fov_x!=null)
			child0++;
			if(this.Parameters_diffusion_anisotropytype!=null)
			child0++;
			if(this.Parameters_voxelres_units!=null)
			child0++;
			if(this.Parameters_ti!=null)
			child0++;
			if(this.Parameters_dtiacqcount!=null)
			child0++;
			if(this.Parameters_flip!=null)
			child0++;
			if(this.Parameters_pixelbandwidth!=null)
			child0++;
			if(this.Parameters_te!=null)
			child0++;
			if(this.Parameters_partitions!=null)
			child0++;
			if(this.Parameters_sequence!=null)
			child0++;
			if(this.Parameters_orientation!=null)
			child0++;
			if(this.Parameters_diffusion_orientations!=null)
			child0++;
			if(this.Parameters_voxelres_z!=null)
			child0++;
			if(this.Parameters_voxelres_y!=null)
			child0++;
			if(this.Parameters_voxelres_x!=null)
			child0++;
			child0+=this.Parameters_addparam.length;
			if(this.Parameters_coil!=null)
			child0++;
			if(this.Parameters_diffusion_directionality!=null)
			child0++;
			if(this.Parameters_diffusion_bvalues!=null)
			child0++;
			if(this.Parameters_origin!=null)
			child0++;
			if(this.Parameters_seqvariant!=null)
			child0++;
			if(this.Parameters_acqtype!=null)
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

		var Parameters_matrixATT = ""
		if (this.Parameters_matrix_x!=null)
			Parameters_matrixATT+=" x=\"" + this.Parameters_matrix_x + "\"";
		if (this.Parameters_matrix_y!=null)
			Parameters_matrixATT+=" y=\"" + this.Parameters_matrix_y + "\"";
		if(Parameters_matrixATT!=""){
			xmlTxt+="\n<xnat:matrix";
			xmlTxt+=Parameters_matrixATT;
			xmlTxt+="/>";
		}

		if (this.Parameters_partitions!=null){
			xmlTxt+="\n<xnat:partitions";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_partitions;
			xmlTxt+="</xnat:partitions>";
		}
		if (this.Parameters_tr!=null){
			xmlTxt+="\n<xnat:tr";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_tr;
			xmlTxt+="</xnat:tr>";
		}
		if (this.Parameters_te!=null){
			xmlTxt+="\n<xnat:te";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_te;
			xmlTxt+="</xnat:te>";
		}
		if (this.Parameters_ti!=null){
			xmlTxt+="\n<xnat:ti";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_ti;
			xmlTxt+="</xnat:ti>";
		}
		if (this.Parameters_flip!=null){
			xmlTxt+="\n<xnat:flip";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_flip;
			xmlTxt+="</xnat:flip>";
		}
		if (this.Parameters_sequence!=null){
			xmlTxt+="\n<xnat:sequence";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_sequence.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:sequence>";
		}
		if (this.Parameters_origin!=null){
			xmlTxt+="\n<xnat:origin";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_origin.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:origin>";
		}
		if (this.Parameters_imagetype!=null){
			xmlTxt+="\n<xnat:imageType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_imagetype.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:imageType>";
		}
		if (this.Parameters_scansequence!=null){
			xmlTxt+="\n<xnat:scanSequence";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_scansequence.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:scanSequence>";
		}
		if (this.Parameters_seqvariant!=null){
			xmlTxt+="\n<xnat:seqVariant";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_seqvariant.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:seqVariant>";
		}
		if (this.Parameters_scanoptions!=null){
			xmlTxt+="\n<xnat:scanOptions";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_scanoptions.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:scanOptions>";
		}
		if (this.Parameters_acqtype!=null){
			xmlTxt+="\n<xnat:acqType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_acqtype.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:acqType>";
		}
		if (this.Parameters_coil!=null){
			xmlTxt+="\n<xnat:coil";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_coil.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:coil>";
		}
		if (this.Parameters_dtiacqcount!=null){
			xmlTxt+="\n<xnat:dtiAcqCount";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_dtiacqcount;
			xmlTxt+="</xnat:dtiAcqCount>";
		}
		if (this.Parameters_pixelbandwidth!=null){
			xmlTxt+="\n<xnat:pixelBandwidth";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_pixelbandwidth;
			xmlTxt+="</xnat:pixelBandwidth>";
		}
			var child1=0;
			var att1=0;
			if(this.Parameters_diffusion_bvalues!=null)
			child1++;
			if(this.Parameters_diffusion_anisotropytype!=null)
			child1++;
			if(this.Parameters_diffusion_orientations!=null)
			child1++;
			if(this.Parameters_diffusion_directionality!=null)
			child1++;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:diffusion";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Parameters_diffusion_bvalues!=null){
			xmlTxt+="\n<xnat:bValues";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_diffusion_bvalues.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:bValues>";
		}
		if (this.Parameters_diffusion_directionality!=null){
			xmlTxt+="\n<xnat:directionality";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_diffusion_directionality.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:directionality>";
		}
		if (this.Parameters_diffusion_orientations!=null){
			xmlTxt+="\n<xnat:orientations";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_diffusion_orientations.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:orientations>";
		}
		if (this.Parameters_diffusion_anisotropytype!=null){
			xmlTxt+="\n<xnat:anisotropyType";
			xmlTxt+=">";
			xmlTxt+=this.Parameters_diffusion_anisotropytype.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:anisotropyType>";
		}
				xmlTxt+="\n</xnat:diffusion>";
			}
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
		if (this.Coil!=null) return true;
		if (this.Fieldstrength!=null) return true;
		if (this.Marker!=null) return true;
		if (this.Stabilization!=null) return true;
			if(this.Parameters_scansequence!=null) return true;
			if(this.Parameters_scanoptions!=null) return true;
			if(this.Parameters_tr!=null) return true;
			if(this.Parameters_matrix_y!=null) return true;
			if(this.Parameters_matrix_x!=null) return true;
			if(this.Parameters_imagetype!=null) return true;
			if(this.Parameters_fov_y!=null) return true;
			if(this.Parameters_fov_x!=null) return true;
			if(this.Parameters_diffusion_anisotropytype!=null) return true;
			if(this.Parameters_voxelres_units!=null) return true;
			if(this.Parameters_ti!=null) return true;
			if(this.Parameters_dtiacqcount!=null) return true;
			if(this.Parameters_flip!=null) return true;
			if(this.Parameters_pixelbandwidth!=null) return true;
			if(this.Parameters_te!=null) return true;
			if(this.Parameters_partitions!=null) return true;
			if(this.Parameters_sequence!=null) return true;
			if(this.Parameters_orientation!=null) return true;
			if(this.Parameters_diffusion_orientations!=null) return true;
			if(this.Parameters_voxelres_z!=null) return true;
			if(this.Parameters_voxelres_y!=null) return true;
			if(this.Parameters_voxelres_x!=null) return true;
			if(this.Parameters_addparam.length>0)return true;
			if(this.Parameters_coil!=null) return true;
			if(this.Parameters_diffusion_directionality!=null) return true;
			if(this.Parameters_diffusion_bvalues!=null) return true;
			if(this.Parameters_origin!=null) return true;
			if(this.Parameters_seqvariant!=null) return true;
			if(this.Parameters_acqtype!=null) return true;
		if (this.Dcmvalidation_status!=null)
			return true;
		if (this.Dcmvalidation!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
