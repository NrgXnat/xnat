/*
 * web: xnat_reconstructedImageData.js
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

function xnat_reconstructedImageData(){
this.xsiType="xnat:reconstructedImageData";

	this.getSchemaElementName=function(){
		return "reconstructedImageData";
	}

	this.getFullSchemaElementName=function(){
		return "xnat:reconstructedImageData";
	}

	this.ImageSessionId=null;


	function getImageSessionId() {
		return this.ImageSessionId;
	}
	this.getImageSessionId=getImageSessionId;


	function setImageSessionId(v){
		this.ImageSessionId=v;
	}
	this.setImageSessionId=setImageSessionId;
	this.Inscans_scanid =new Array();

	function getInscans_scanid() {
		return this.Inscans_scanid;
	}
	this.getInscans_scanid=getInscans_scanid;


	function addInscans_scanid(v){
		this.Inscans_scanid.push(v);
	}
	this.addInscans_scanid=addInscans_scanid;
	this.In_file =new Array();

	function getIn_file() {
		return this.In_file;
	}
	this.getIn_file=getIn_file;


	function addIn_file(v){
		this.In_file.push(v);
	}
	this.addIn_file=addIn_file;
	this.Out_file =new Array();

	function getOut_file() {
		return this.Out_file;
	}
	this.getOut_file=getOut_file;


	function addOut_file(v){
		this.Out_file.push(v);
	}
	this.addOut_file=addOut_file;
	this.Provenance =null;
	function getProvenance() {
		return this.Provenance;
	}
	this.getProvenance=getProvenance;


	function setProvenance(v){
		this.Provenance =v;
	}
	this.setProvenance=setProvenance;

	this.Provenance_ProvenanceProvProcessId=null;


	function getProvenance_ProvenanceProvProcessId(){
		return this.Provenance_ProvenanceProvProcessId;
	}
	this.getProvenance_ProvenanceProvProcessId=getProvenance_ProvenanceProvProcessId;


	function setProvenance_ProvenanceProvProcessId(v){
		this.Provenance_ProvenanceProvProcessId=v;
	}
	this.setProvenance_ProvenanceProvProcessId=setProvenance_ProvenanceProvProcessId;
	this.Parameters_addparam =new Array();

	function getParameters_addparam() {
		return this.Parameters_addparam;
	}
	this.getParameters_addparam=getParameters_addparam;


	function addParameters_addparam(v){
		this.Parameters_addparam.push(v);
	}
	this.addParameters_addparam=addParameters_addparam;
	this.Computations_datum =new Array();

	function getComputations_datum() {
		return this.Computations_datum;
	}
	this.getComputations_datum=getComputations_datum;


	function addComputations_datum(v){
		this.Computations_datum.push(v);
	}
	this.addComputations_datum=addComputations_datum;

	this.Id=null;


	function getId() {
		return this.Id;
	}
	this.getId=getId;


	function setId(v){
		this.Id=v;
	}
	this.setId=setId;

	this.Type=null;


	function getType() {
		return this.Type;
	}
	this.getType=getType;


	function setType(v){
		this.Type=v;
	}
	this.setType=setType;

	this.Basescantype=null;


	function getBasescantype() {
		return this.Basescantype;
	}
	this.getBasescantype=getBasescantype;


	function setBasescantype(v){
		this.Basescantype=v;
	}
	this.setBasescantype=setBasescantype;

	this.XnatReconstructedimagedataId=null;


	function getXnatReconstructedimagedataId() {
		return this.XnatReconstructedimagedataId;
	}
	this.getXnatReconstructedimagedataId=getXnatReconstructedimagedataId;


	function setXnatReconstructedimagedataId(v){
		this.XnatReconstructedimagedataId=v;
	}
	this.setXnatReconstructedimagedataId=setXnatReconstructedimagedataId;


	this.getProperty=function(xmlPath){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="image_session_ID"){
				return this.ImageSessionId ;
			} else 
			if(xmlPath=="inScans/scanID"){
				return this.Inscans_scanid ;
			} else 
			if(xmlPath.startsWith("inScans/scanID")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Inscans_scanid ;
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

				for(var whereCount=0;whereCount<this.Inscans_scanid.length;whereCount++){

					var tempValue=this.Inscans_scanid[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Inscans_scanid[whereCount]);

					}

				}
				}else{

				whereArray=this.Inscans_scanid;
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
			if(xmlPath=="in/file"){
				return this.In_file ;
			} else 
			if(xmlPath.startsWith("in/file")){
				xmlPath=xmlPath.substring(7);
				if(xmlPath=="")return this.In_file ;
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

				for(var whereCount=0;whereCount<this.In_file.length;whereCount++){

					var tempValue=this.In_file[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.In_file[whereCount]);

					}

				}
				}else{

				whereArray=this.In_file;
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
			if(xmlPath=="out/file"){
				return this.Out_file ;
			} else 
			if(xmlPath.startsWith("out/file")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Out_file ;
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

				for(var whereCount=0;whereCount<this.Out_file.length;whereCount++){

					var tempValue=this.Out_file[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Out_file[whereCount]);

					}

				}
				}else{

				whereArray=this.Out_file;
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
			if(xmlPath=="provenance"){
				return this.Provenance ;
			} else 
			if(xmlPath.startsWith("provenance")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Provenance ;
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
				if(this.Provenance!=undefined)return this.Provenance.getProperty(xmlPath);
				else return null;
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
			if(xmlPath=="computations/datum"){
				return this.Computations_datum ;
			} else 
			if(xmlPath.startsWith("computations/datum")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Computations_datum ;
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

				for(var whereCount=0;whereCount<this.Computations_datum.length;whereCount++){

					var tempValue=this.Computations_datum[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Computations_datum[whereCount]);

					}

				}
				}else{

				whereArray=this.Computations_datum;
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
			if(xmlPath=="ID"){
				return this.Id ;
			} else 
			if(xmlPath=="type"){
				return this.Type ;
			} else 
			if(xmlPath=="baseScanType"){
				return this.Basescantype ;
			} else 
			if(xmlPath=="meta"){
				return this.Meta ;
			} else 
			if(xmlPath=="xnat_reconstructedImageData_id"){
				return this.XnatReconstructedimagedataId ;
			} else 
			{
				return null;
			}
	}


	this.setProperty=function(xmlPath,value){
			if(xmlPath.startsWith(this.getFullSchemaElementName())){
				xmlPath=xmlPath.substring(this.getFullSchemaElementName().length + 1);
			}
			if(xmlPath=="image_session_ID"){
				this.ImageSessionId=value;
			} else 
			if(xmlPath=="inScans/scanID"){
				this.Inscans_scanid=value;
			} else 
			if(xmlPath.startsWith("inScans/scanID")){
				xmlPath=xmlPath.substring(14);
				if(xmlPath=="")return this.Inscans_scanid ;
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

				for(var whereCount=0;whereCount<this.Inscans_scanid.length;whereCount++){

					var tempValue=this.Inscans_scanid[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Inscans_scanid[whereCount]);

					}

				}
				}else{

				whereArray=this.Inscans_scanid;
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
						newChild= instanciateObject("xnat:reconstructedImageData_scanID");//omUtils.js
					}
					this.addInscans_scanid(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="in/file"){
				this.In_file=value;
			} else 
			if(xmlPath.startsWith("in/file")){
				xmlPath=xmlPath.substring(7);
				if(xmlPath=="")return this.In_file ;
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

				for(var whereCount=0;whereCount<this.In_file.length;whereCount++){

					var tempValue=this.In_file[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.In_file[whereCount]);

					}

				}
				}else{

				whereArray=this.In_file;
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
						newChild= instanciateObject("xnat:abstractResource");//omUtils.js
					}
					this.addIn_file(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="out/file"){
				this.Out_file=value;
			} else 
			if(xmlPath.startsWith("out/file")){
				xmlPath=xmlPath.substring(8);
				if(xmlPath=="")return this.Out_file ;
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

				for(var whereCount=0;whereCount<this.Out_file.length;whereCount++){

					var tempValue=this.Out_file[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Out_file[whereCount]);

					}

				}
				}else{

				whereArray=this.Out_file;
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
						newChild= instanciateObject("xnat:abstractResource");//omUtils.js
					}
					this.addOut_file(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="provenance"){
				this.Provenance=value;
			} else 
			if(xmlPath.startsWith("provenance")){
				xmlPath=xmlPath.substring(10);
				if(xmlPath=="")return this.Provenance ;
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
				if(this.Provenance!=undefined){
					this.Provenance.setProperty(xmlPath,value);
				}else{
						if(options && options.xsiType){
							this.Provenance= instanciateObject(options.xsiType);//omUtils.js
						}else{
							this.Provenance= instanciateObject("prov:process");//omUtils.js
						}
						if(options && options.where)this.Provenance.setProperty(options.where.field,options.where.value);
						this.Provenance.setProperty(xmlPath,value);
				}
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
			if(xmlPath=="computations/datum"){
				this.Computations_datum=value;
			} else 
			if(xmlPath.startsWith("computations/datum")){
				xmlPath=xmlPath.substring(18);
				if(xmlPath=="")return this.Computations_datum ;
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

				for(var whereCount=0;whereCount<this.Computations_datum.length;whereCount++){

					var tempValue=this.Computations_datum[whereCount].getProperty(options.where.field);

					if(tempValue!=null)if(tempValue.toString()==options.where.value.toString()){

						whereArray.push(this.Computations_datum[whereCount]);

					}

				}
				}else{

				whereArray=this.Computations_datum;
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
						newChild= instanciateObject("xnat:computationData");//omUtils.js
					}
					this.addComputations_datum(newChild);
					if(options && options.where)newChild.setProperty(options.where.field,options.where.value);
					newChild.setProperty(xmlPath,value);
				}
			} else 
			if(xmlPath=="ID"){
				this.Id=value;
			} else 
			if(xmlPath=="type"){
				this.Type=value;
			} else 
			if(xmlPath=="baseScanType"){
				this.Basescantype=value;
			} else 
			if(xmlPath=="meta"){
				this.Meta=value;
			} else 
			if(xmlPath=="xnat_reconstructedImageData_id"){
				this.XnatReconstructedimagedataId=value;
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
		if (xmlPath=="inScans/scanID"){
			this.addInscans_scanid(v);
		}else if (xmlPath=="in/file"){
			this.addIn_file(v);
		}else if (xmlPath=="out/file"){
			this.addOut_file(v);
		}else if (xmlPath=="provenance"){
			this.setProvenance(v);
		}else if (xmlPath=="parameters/addParam"){
			this.addParameters_addparam(v);
		}else if (xmlPath=="computations/datum"){
			this.addComputations_datum(v);
		}
		else{
		}
	}

	/**
	 * Gets the value for a field via the XMLPATH.
	 * @param v Value to Set.
	 */
	this.getReferenceFieldName=function(xmlPath) {
		if (xmlPath=="inScans/scanID"){
			return "http://nrg.wustl.edu/xnat:reconstructedImageData_scanID";
		}else if (xmlPath=="in/file"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="out/file"){
			return "http://nrg.wustl.edu/xnat:abstractResource";
		}else if (xmlPath=="provenance"){
			return "http://www.nbirn.net/prov:process";
		}else if (xmlPath=="parameters/addParam"){
			return "http://nrg.wustl.edu/xnat:addField";
		}else if (xmlPath=="computations/datum"){
			return "http://nrg.wustl.edu/xnat:computationData";
		}
		else{
		}
	}

	/**
	 * Returns whether or not this is a reference field
	 */
	this.getFieldType=function(xmlPath){
		if (xmlPath=="image_session_ID"){
			return "field_data";
		}else if (xmlPath=="inScans/scanID"){
			return "field_inline_repeater";
		}else if (xmlPath=="in/file"){
			return "field_multi_reference";
		}else if (xmlPath=="out/file"){
			return "field_multi_reference";
		}else if (xmlPath=="provenance"){
			return "field_single_reference";
		}else if (xmlPath=="parameters/addParam"){
			return "field_NO_CHILD";
		}else if (xmlPath=="computations/datum"){
			return "field_multi_reference";
		}else if (xmlPath=="ID"){
			return "field_data";
		}else if (xmlPath=="type"){
			return "field_data";
		}else if (xmlPath=="baseScanType"){
			return "field_data";
		}
		else{
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat:ReconstructedImage";
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
		xmlTxt+="\n</xnat:ReconstructedImage>";
		return xmlTxt;
	}


	this.getXMLComments=function(preventComments){
		var str ="";
		if((preventComments==undefined || !preventComments) && this.hasXMLComments()){
		str += "<!--hidden_fields[";
		var hiddenCount = 0;
			if(this.XnatReconstructedimagedataId!=null){
				if(hiddenCount++>0)str+=",";
				str+="xnat_reconstructedImageData_id=\"" + this.XnatReconstructedimagedataId + "\"";
			}
		str +="]-->";
		}
		return str;
	}


	this.getXMLAtts=function(){
		var attTxt = "";
		if (this.Id!=null)
			attTxt+=" ID=\"" +this.Id +"\"";
		//NOT REQUIRED FIELD

		if (this.Type!=null)
			attTxt+=" type=\"" +this.Type +"\"";
		//NOT REQUIRED FIELD

		if (this.Basescantype!=null)
			attTxt+=" baseScanType=\"" +this.Basescantype +"\"";
		//NOT REQUIRED FIELD

		return attTxt;
	}


	this.getXMLBody=function(preventComments){
		var xmlTxt=this.getXMLComments(preventComments);
		if (this.ImageSessionId!=null){
			xmlTxt+="\n<xnat:image_session_ID";
			xmlTxt+=">";
			xmlTxt+=this.ImageSessionId.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat:image_session_ID>";
		}
			var child0=0;
			var att0=0;
			child0+=this.Inscans_scanid.length;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat:inScans";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Inscans_scanidCOUNT=0;Inscans_scanidCOUNT<this.Inscans_scanid.length;Inscans_scanidCOUNT++){
			xmlTxt+=this.Inscans_scanid[Inscans_scanidCOUNT].getXMLBody(preventComments);
		}
				xmlTxt+="\n</xnat:inScans>";
			}
			}

			var child1=0;
			var att1=0;
			child1+=this.In_file.length;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat:in";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var In_fileCOUNT=0;In_fileCOUNT<this.In_file.length;In_fileCOUNT++){
			xmlTxt +="\n<xnat:file";
			xmlTxt +=this.In_file[In_fileCOUNT].getXMLAtts();
			if(this.In_file[In_fileCOUNT].xsiType!="xnat:abstractResource"){
				xmlTxt+=" xsi:type=\"" + this.In_file[In_fileCOUNT].xsiType + "\"";
			}
			if (this.In_file[In_fileCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.In_file[In_fileCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:file>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:in>";
			}
			}

			var child2=0;
			var att2=0;
			child2+=this.Out_file.length;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xnat:out";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Out_fileCOUNT=0;Out_fileCOUNT<this.Out_file.length;Out_fileCOUNT++){
			xmlTxt +="\n<xnat:file";
			xmlTxt +=this.Out_file[Out_fileCOUNT].getXMLAtts();
			if(this.Out_file[Out_fileCOUNT].xsiType!="xnat:abstractResource"){
				xmlTxt+=" xsi:type=\"" + this.Out_file[Out_fileCOUNT].xsiType + "\"";
			}
			if (this.Out_file[Out_fileCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Out_file[Out_fileCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:file>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:out>";
			}
			}

		if (this.Provenance!=null){
			xmlTxt+="\n<xnat:provenance";
			xmlTxt+=this.Provenance.getXMLAtts();
			if(this.Provenance.xsiType!="prov:process"){
				xmlTxt+=" xsi:type=\"" + this.Provenance.xsiType + "\"";
			}
			if (this.Provenance.hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Provenance.getXMLBody(preventComments);
				xmlTxt+="</xnat:provenance>";
			}else {xmlTxt+="/>";}
		}
		//NOT REQUIRED

			var child3=0;
			var att3=0;
			child3+=this.Parameters_addparam.length;
			if(child3>0 || att3>0){
				xmlTxt+="\n<xnat:parameters";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
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

			var child4=0;
			var att4=0;
			child4+=this.Computations_datum.length;
			if(child4>0 || att4>0){
				xmlTxt+="\n<xnat:computations";
			if(child4==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		for(var Computations_datumCOUNT=0;Computations_datumCOUNT<this.Computations_datum.length;Computations_datumCOUNT++){
			xmlTxt +="\n<xnat:datum";
			xmlTxt +=this.Computations_datum[Computations_datumCOUNT].getXMLAtts();
			if(this.Computations_datum[Computations_datumCOUNT].xsiType!="xnat:computationData"){
				xmlTxt+=" xsi:type=\"" + this.Computations_datum[Computations_datumCOUNT].xsiType + "\"";
			}
			if (this.Computations_datum[Computations_datumCOUNT].hasXMLBodyContent()){
				xmlTxt+=">";
				xmlTxt+=this.Computations_datum[Computations_datumCOUNT].getXMLBody(preventComments);
					xmlTxt+="</xnat:datum>";
			}else {xmlTxt+="/>";}
		}
				xmlTxt+="\n</xnat:computations>";
			}
			}

		return xmlTxt;
	}


	this.hasXMLComments=function(){
			if (this.XnatReconstructedimagedataId!=null) return true;
			return false;
	}


	this.hasXMLBodyContent=function(){
		if (this.ImageSessionId!=null) return true;
			if(this.Inscans_scanid.length>0)return true;
			if(this.In_file.length>0)return true;
			if(this.Out_file.length>0)return true;
		if (this.Provenance!=null){
			if (this.Provenance.hasXMLBodyContent()) return true;
		}
		//NOT REQUIRED

			if(this.Parameters_addparam.length>0)return true;
			if(this.Computations_datum.length>0)return true;
		if(this.hasXMLComments())return true;
		return false;
	}
}
