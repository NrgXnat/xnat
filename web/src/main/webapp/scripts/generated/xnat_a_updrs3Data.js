/*
 * web: xnat_a_updrs3Data.js
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

function xnat_a_updrs3Data(){
this.xsiType="xnat_a:updrs3Data";

	this.getSchemaElementName=function(){
		return "updrs3Data";
	}

	this.getFullSchemaElementName=function(){
		return "xnat_a:updrs3Data";
	}
this.extension=dynamicJSLoad('xnat_subjectAssessorData','generated/xnat_subjectAssessorData.js');

	this.Inscanner=null;


	function getInscanner() {
		return this.Inscanner;
	}
	this.getInscanner=getInscanner;


	function setInscanner(v){
		this.Inscanner=v;
	}
	this.setInscanner=setInscanner;


	this.isInscanner=function(defaultValue) {
		if(this.Inscanner==null)return defaultValue;
		if(this.Inscanner=="1" || this.Inscanner==true)return true;
		return false;
	}

	this.Clicker_right=null;


	function getClicker_right() {
		return this.Clicker_right;
	}
	this.getClicker_right=getClicker_right;


	function setClicker_right(v){
		this.Clicker_right=v;
	}
	this.setClicker_right=setClicker_right;

	this.Clicker_left=null;


	function getClicker_left() {
		return this.Clicker_left;
	}
	this.getClicker_left=getClicker_left;


	function setClicker_left(v){
		this.Clicker_left=v;
	}
	this.setClicker_left=setClicker_left;

	this.Clicker_interval=null;


	function getClicker_interval() {
		return this.Clicker_interval;
	}
	this.getClicker_interval=getClicker_interval;


	function setClicker_interval(v){
		this.Clicker_interval=v;
	}
	this.setClicker_interval=setClicker_interval;

	this.Tremorrest_face=null;


	function getTremorrest_face() {
		return this.Tremorrest_face;
	}
	this.getTremorrest_face=getTremorrest_face;


	function setTremorrest_face(v){
		this.Tremorrest_face=v;
	}
	this.setTremorrest_face=setTremorrest_face;

	this.Tremorrest_rue=null;


	function getTremorrest_rue() {
		return this.Tremorrest_rue;
	}
	this.getTremorrest_rue=getTremorrest_rue;


	function setTremorrest_rue(v){
		this.Tremorrest_rue=v;
	}
	this.setTremorrest_rue=setTremorrest_rue;

	this.Tremorrest_lue=null;


	function getTremorrest_lue() {
		return this.Tremorrest_lue;
	}
	this.getTremorrest_lue=getTremorrest_lue;


	function setTremorrest_lue(v){
		this.Tremorrest_lue=v;
	}
	this.setTremorrest_lue=setTremorrest_lue;

	this.Tremorrest_rle=null;


	function getTremorrest_rle() {
		return this.Tremorrest_rle;
	}
	this.getTremorrest_rle=getTremorrest_rle;


	function setTremorrest_rle(v){
		this.Tremorrest_rle=v;
	}
	this.setTremorrest_rle=setTremorrest_rle;

	this.Tremorrest_lle=null;


	function getTremorrest_lle() {
		return this.Tremorrest_lle;
	}
	this.getTremorrest_lle=getTremorrest_lle;


	function setTremorrest_lle(v){
		this.Tremorrest_lle=v;
	}
	this.setTremorrest_lle=setTremorrest_lle;

	this.Rigidity_neck=null;


	function getRigidity_neck() {
		return this.Rigidity_neck;
	}
	this.getRigidity_neck=getRigidity_neck;


	function setRigidity_neck(v){
		this.Rigidity_neck=v;
	}
	this.setRigidity_neck=setRigidity_neck;

	this.Rigidity_rue=null;


	function getRigidity_rue() {
		return this.Rigidity_rue;
	}
	this.getRigidity_rue=getRigidity_rue;


	function setRigidity_rue(v){
		this.Rigidity_rue=v;
	}
	this.setRigidity_rue=setRigidity_rue;

	this.Rigidity_lue=null;


	function getRigidity_lue() {
		return this.Rigidity_lue;
	}
	this.getRigidity_lue=getRigidity_lue;


	function setRigidity_lue(v){
		this.Rigidity_lue=v;
	}
	this.setRigidity_lue=setRigidity_lue;

	this.Rigidity_rle=null;


	function getRigidity_rle() {
		return this.Rigidity_rle;
	}
	this.getRigidity_rle=getRigidity_rle;


	function setRigidity_rle(v){
		this.Rigidity_rle=v;
	}
	this.setRigidity_rle=setRigidity_rle;

	this.Rigidity_lle=null;


	function getRigidity_lle() {
		return this.Rigidity_lle;
	}
	this.getRigidity_lle=getRigidity_lle;


	function setRigidity_lle(v){
		this.Rigidity_lle=v;
	}
	this.setRigidity_lle=setRigidity_lle;

	this.Handmovementsgrip_right=null;


	function getHandmovementsgrip_right() {
		return this.Handmovementsgrip_right;
	}
	this.getHandmovementsgrip_right=getHandmovementsgrip_right;


	function setHandmovementsgrip_right(v){
		this.Handmovementsgrip_right=v;
	}
	this.setHandmovementsgrip_right=setHandmovementsgrip_right;

	this.Handmovementsgrip_left=null;


	function getHandmovementsgrip_left() {
		return this.Handmovementsgrip_left;
	}
	this.getHandmovementsgrip_left=getHandmovementsgrip_left;


	function setHandmovementsgrip_left(v){
		this.Handmovementsgrip_left=v;
	}
	this.setHandmovementsgrip_left=setHandmovementsgrip_left;

	this.Facialexpression=null;


	function getFacialexpression() {
		return this.Facialexpression;
	}
	this.getFacialexpression=getFacialexpression;


	function setFacialexpression(v){
		this.Facialexpression=v;
	}
	this.setFacialexpression=setFacialexpression;

	this.Gait=null;


	function getGait() {
		return this.Gait;
	}
	this.getGait=getGait;


	function setGait(v){
		this.Gait=v;
	}
	this.setGait=setGait;

	this.Speech=null;


	function getSpeech() {
		return this.Speech;
	}
	this.getSpeech=getSpeech;


	function setSpeech(v){
		this.Speech=v;
	}
	this.setSpeech=setSpeech;

	this.Writing=null;


	function getWriting() {
		return this.Writing;
	}
	this.getWriting=getWriting;


	function setWriting(v){
		this.Writing=v;
	}
	this.setWriting=setWriting;

	this.Arisefromchair=null;


	function getArisefromchair() {
		return this.Arisefromchair;
	}
	this.getArisefromchair=getArisefromchair;


	function setArisefromchair(v){
		this.Arisefromchair=v;
	}
	this.setArisefromchair=setArisefromchair;

	this.Actionposturaltremor_right=null;


	function getActionposturaltremor_right() {
		return this.Actionposturaltremor_right;
	}
	this.getActionposturaltremor_right=getActionposturaltremor_right;


	function setActionposturaltremor_right(v){
		this.Actionposturaltremor_right=v;
	}
	this.setActionposturaltremor_right=setActionposturaltremor_right;

	this.Actionposturaltremor_left=null;


	function getActionposturaltremor_left() {
		return this.Actionposturaltremor_left;
	}
	this.getActionposturaltremor_left=getActionposturaltremor_left;


	function setActionposturaltremor_left(v){
		this.Actionposturaltremor_left=v;
	}
	this.setActionposturaltremor_left=setActionposturaltremor_left;

	this.Handsram_right=null;


	function getHandsram_right() {
		return this.Handsram_right;
	}
	this.getHandsram_right=getHandsram_right;


	function setHandsram_right(v){
		this.Handsram_right=v;
	}
	this.setHandsram_right=setHandsram_right;

	this.Handsram_left=null;


	function getHandsram_left() {
		return this.Handsram_left;
	}
	this.getHandsram_left=getHandsram_left;


	function setHandsram_left(v){
		this.Handsram_left=v;
	}
	this.setHandsram_left=setHandsram_left;

	this.Fingertaps_right=null;


	function getFingertaps_right() {
		return this.Fingertaps_right;
	}
	this.getFingertaps_right=getFingertaps_right;


	function setFingertaps_right(v){
		this.Fingertaps_right=v;
	}
	this.setFingertaps_right=setFingertaps_right;

	this.Fingertaps_left=null;


	function getFingertaps_left() {
		return this.Fingertaps_left;
	}
	this.getFingertaps_left=getFingertaps_left;


	function setFingertaps_left(v){
		this.Fingertaps_left=v;
	}
	this.setFingertaps_left=setFingertaps_left;

	this.Posture=null;


	function getPosture() {
		return this.Posture;
	}
	this.getPosture=getPosture;


	function setPosture(v){
		this.Posture=v;
	}
	this.setPosture=setPosture;

	this.Posturalstability=null;


	function getPosturalstability() {
		return this.Posturalstability;
	}
	this.getPosturalstability=getPosturalstability;


	function setPosturalstability(v){
		this.Posturalstability=v;
	}
	this.setPosturalstability=setPosturalstability;

	this.Foottaps_right=null;


	function getFoottaps_right() {
		return this.Foottaps_right;
	}
	this.getFoottaps_right=getFoottaps_right;


	function setFoottaps_right(v){
		this.Foottaps_right=v;
	}
	this.setFoottaps_right=setFoottaps_right;

	this.Foottaps_left=null;


	function getFoottaps_left() {
		return this.Foottaps_left;
	}
	this.getFoottaps_left=getFoottaps_left;


	function setFoottaps_left(v){
		this.Foottaps_left=v;
	}
	this.setFoottaps_left=setFoottaps_left;

	this.Bodybradykinesiahypokinesia=null;


	function getBodybradykinesiahypokinesia() {
		return this.Bodybradykinesiahypokinesia;
	}
	this.getBodybradykinesiahypokinesia=getBodybradykinesiahypokinesia;


	function setBodybradykinesiahypokinesia(v){
		this.Bodybradykinesiahypokinesia=v;
	}
	this.setBodybradykinesiahypokinesia=setBodybradykinesiahypokinesia;

	this.Problem=null;


	function getProblem() {
		return this.Problem;
	}
	this.getProblem=getProblem;


	function setProblem(v){
		this.Problem=v;
	}
	this.setProblem=setProblem;


	this.isProblem=function(defaultValue) {
		if(this.Problem==null)return defaultValue;
		if(this.Problem=="1" || this.Problem==true)return true;
		return false;
	}


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
			if(xmlPath=="inScanner"){
				return this.Inscanner ;
			} else 
			if(xmlPath=="clicker/right"){
				return this.Clicker_right ;
			} else 
			if(xmlPath=="clicker/left"){
				return this.Clicker_left ;
			} else 
			if(xmlPath=="clicker/interval"){
				return this.Clicker_interval ;
			} else 
			if(xmlPath=="tremorRest/face"){
				return this.Tremorrest_face ;
			} else 
			if(xmlPath=="tremorRest/rue"){
				return this.Tremorrest_rue ;
			} else 
			if(xmlPath=="tremorRest/lue"){
				return this.Tremorrest_lue ;
			} else 
			if(xmlPath=="tremorRest/rle"){
				return this.Tremorrest_rle ;
			} else 
			if(xmlPath=="tremorRest/lle"){
				return this.Tremorrest_lle ;
			} else 
			if(xmlPath=="rigidity/neck"){
				return this.Rigidity_neck ;
			} else 
			if(xmlPath=="rigidity/rue"){
				return this.Rigidity_rue ;
			} else 
			if(xmlPath=="rigidity/lue"){
				return this.Rigidity_lue ;
			} else 
			if(xmlPath=="rigidity/rle"){
				return this.Rigidity_rle ;
			} else 
			if(xmlPath=="rigidity/lle"){
				return this.Rigidity_lle ;
			} else 
			if(xmlPath=="handMovementsGrip/right"){
				return this.Handmovementsgrip_right ;
			} else 
			if(xmlPath=="handMovementsGrip/left"){
				return this.Handmovementsgrip_left ;
			} else 
			if(xmlPath=="facialExpression"){
				return this.Facialexpression ;
			} else 
			if(xmlPath=="gait"){
				return this.Gait ;
			} else 
			if(xmlPath=="speech"){
				return this.Speech ;
			} else 
			if(xmlPath=="writing"){
				return this.Writing ;
			} else 
			if(xmlPath=="ariseFromChair"){
				return this.Arisefromchair ;
			} else 
			if(xmlPath=="actionPosturalTremor/right"){
				return this.Actionposturaltremor_right ;
			} else 
			if(xmlPath=="actionPosturalTremor/left"){
				return this.Actionposturaltremor_left ;
			} else 
			if(xmlPath=="handsRAM/right"){
				return this.Handsram_right ;
			} else 
			if(xmlPath=="handsRAM/left"){
				return this.Handsram_left ;
			} else 
			if(xmlPath=="fingerTaps/right"){
				return this.Fingertaps_right ;
			} else 
			if(xmlPath=="fingerTaps/left"){
				return this.Fingertaps_left ;
			} else 
			if(xmlPath=="posture"){
				return this.Posture ;
			} else 
			if(xmlPath=="posturalStability"){
				return this.Posturalstability ;
			} else 
			if(xmlPath=="footTaps/right"){
				return this.Foottaps_right ;
			} else 
			if(xmlPath=="footTaps/left"){
				return this.Foottaps_left ;
			} else 
			if(xmlPath=="bodyBradykinesiaHypokinesia"){
				return this.Bodybradykinesiahypokinesia ;
			} else 
			if(xmlPath=="problem"){
				return this.Problem ;
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
			if(xmlPath=="inScanner"){
				this.Inscanner=value;
			} else 
			if(xmlPath=="clicker/right"){
				this.Clicker_right=value;
			} else 
			if(xmlPath=="clicker/left"){
				this.Clicker_left=value;
			} else 
			if(xmlPath=="clicker/interval"){
				this.Clicker_interval=value;
			} else 
			if(xmlPath=="tremorRest/face"){
				this.Tremorrest_face=value;
			} else 
			if(xmlPath=="tremorRest/rue"){
				this.Tremorrest_rue=value;
			} else 
			if(xmlPath=="tremorRest/lue"){
				this.Tremorrest_lue=value;
			} else 
			if(xmlPath=="tremorRest/rle"){
				this.Tremorrest_rle=value;
			} else 
			if(xmlPath=="tremorRest/lle"){
				this.Tremorrest_lle=value;
			} else 
			if(xmlPath=="rigidity/neck"){
				this.Rigidity_neck=value;
			} else 
			if(xmlPath=="rigidity/rue"){
				this.Rigidity_rue=value;
			} else 
			if(xmlPath=="rigidity/lue"){
				this.Rigidity_lue=value;
			} else 
			if(xmlPath=="rigidity/rle"){
				this.Rigidity_rle=value;
			} else 
			if(xmlPath=="rigidity/lle"){
				this.Rigidity_lle=value;
			} else 
			if(xmlPath=="handMovementsGrip/right"){
				this.Handmovementsgrip_right=value;
			} else 
			if(xmlPath=="handMovementsGrip/left"){
				this.Handmovementsgrip_left=value;
			} else 
			if(xmlPath=="facialExpression"){
				this.Facialexpression=value;
			} else 
			if(xmlPath=="gait"){
				this.Gait=value;
			} else 
			if(xmlPath=="speech"){
				this.Speech=value;
			} else 
			if(xmlPath=="writing"){
				this.Writing=value;
			} else 
			if(xmlPath=="ariseFromChair"){
				this.Arisefromchair=value;
			} else 
			if(xmlPath=="actionPosturalTremor/right"){
				this.Actionposturaltremor_right=value;
			} else 
			if(xmlPath=="actionPosturalTremor/left"){
				this.Actionposturaltremor_left=value;
			} else 
			if(xmlPath=="handsRAM/right"){
				this.Handsram_right=value;
			} else 
			if(xmlPath=="handsRAM/left"){
				this.Handsram_left=value;
			} else 
			if(xmlPath=="fingerTaps/right"){
				this.Fingertaps_right=value;
			} else 
			if(xmlPath=="fingerTaps/left"){
				this.Fingertaps_left=value;
			} else 
			if(xmlPath=="posture"){
				this.Posture=value;
			} else 
			if(xmlPath=="posturalStability"){
				this.Posturalstability=value;
			} else 
			if(xmlPath=="footTaps/right"){
				this.Foottaps_right=value;
			} else 
			if(xmlPath=="footTaps/left"){
				this.Foottaps_left=value;
			} else 
			if(xmlPath=="bodyBradykinesiaHypokinesia"){
				this.Bodybradykinesiahypokinesia=value;
			} else 
			if(xmlPath=="problem"){
				this.Problem=value;
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
		if (xmlPath=="inScanner"){
			return "field_data";
		}else if (xmlPath=="clicker/right"){
			return "field_data";
		}else if (xmlPath=="clicker/left"){
			return "field_data";
		}else if (xmlPath=="clicker/interval"){
			return "field_data";
		}else if (xmlPath=="tremorRest/face"){
			return "field_data";
		}else if (xmlPath=="tremorRest/rue"){
			return "field_data";
		}else if (xmlPath=="tremorRest/lue"){
			return "field_data";
		}else if (xmlPath=="tremorRest/rle"){
			return "field_data";
		}else if (xmlPath=="tremorRest/lle"){
			return "field_data";
		}else if (xmlPath=="rigidity/neck"){
			return "field_data";
		}else if (xmlPath=="rigidity/rue"){
			return "field_data";
		}else if (xmlPath=="rigidity/lue"){
			return "field_data";
		}else if (xmlPath=="rigidity/rle"){
			return "field_data";
		}else if (xmlPath=="rigidity/lle"){
			return "field_data";
		}else if (xmlPath=="handMovementsGrip/right"){
			return "field_data";
		}else if (xmlPath=="handMovementsGrip/left"){
			return "field_data";
		}else if (xmlPath=="facialExpression"){
			return "field_data";
		}else if (xmlPath=="gait"){
			return "field_data";
		}else if (xmlPath=="speech"){
			return "field_data";
		}else if (xmlPath=="writing"){
			return "field_data";
		}else if (xmlPath=="ariseFromChair"){
			return "field_data";
		}else if (xmlPath=="actionPosturalTremor/right"){
			return "field_data";
		}else if (xmlPath=="actionPosturalTremor/left"){
			return "field_data";
		}else if (xmlPath=="handsRAM/right"){
			return "field_data";
		}else if (xmlPath=="handsRAM/left"){
			return "field_data";
		}else if (xmlPath=="fingerTaps/right"){
			return "field_data";
		}else if (xmlPath=="fingerTaps/left"){
			return "field_data";
		}else if (xmlPath=="posture"){
			return "field_data";
		}else if (xmlPath=="posturalStability"){
			return "field_data";
		}else if (xmlPath=="footTaps/right"){
			return "field_data";
		}else if (xmlPath=="footTaps/left"){
			return "field_data";
		}else if (xmlPath=="bodyBradykinesiaHypokinesia"){
			return "field_data";
		}else if (xmlPath=="problem"){
			return "field_data";
		}
		else{
			return this.extension.getFieldType(xmlPath);
		}
	}


	this.toXML=function(xmlTxt,preventComments){
		xmlTxt+="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		xmlTxt+="\n<xnat_a:UPDRS3";
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
		xmlTxt+="\n</xnat_a:UPDRS3>";
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
		if (this.Inscanner!=null){
			xmlTxt+="\n<xnat_a:inScanner";
			xmlTxt+=">";
			xmlTxt+=this.Inscanner;
			xmlTxt+="</xnat_a:inScanner>";
		}
			var child0=0;
			var att0=0;
			if(this.Clicker_left!=null)
			child0++;
			if(this.Clicker_interval!=null)
			child0++;
			if(this.Clicker_right!=null)
			child0++;
			if(child0>0 || att0>0){
				xmlTxt+="\n<xnat_a:clicker";
			if(child0==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Clicker_right!=null){
			xmlTxt+="\n<xnat_a:right";
			xmlTxt+=">";
			xmlTxt+=this.Clicker_right;
			xmlTxt+="</xnat_a:right>";
		}
		if (this.Clicker_left!=null){
			xmlTxt+="\n<xnat_a:left";
			xmlTxt+=">";
			xmlTxt+=this.Clicker_left;
			xmlTxt+="</xnat_a:left>";
		}
		if (this.Clicker_interval!=null){
			xmlTxt+="\n<xnat_a:interval";
			xmlTxt+=">";
			xmlTxt+=this.Clicker_interval;
			xmlTxt+="</xnat_a:interval>";
		}
		else{
			xmlTxt+="\n<xnat_a:interval";
			xmlTxt+="/>";
		}

				xmlTxt+="\n</xnat_a:clicker>";
			}
			}

			var child1=0;
			var att1=0;
			if(this.Tremorrest_lue!=null)
			child1++;
			if(this.Tremorrest_rle!=null)
			child1++;
			if(this.Tremorrest_lle!=null)
			child1++;
			if(this.Tremorrest_face!=null)
			child1++;
			if(this.Tremorrest_rue!=null)
			child1++;
			if(child1>0 || att1>0){
				xmlTxt+="\n<xnat_a:tremorRest";
			if(child1==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Tremorrest_face!=null){
			xmlTxt+="\n<xnat_a:face";
			xmlTxt+=">";
			xmlTxt+=this.Tremorrest_face.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:face>";
		}
		if (this.Tremorrest_rue!=null){
			xmlTxt+="\n<xnat_a:rue";
			xmlTxt+=">";
			xmlTxt+=this.Tremorrest_rue.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:rue>";
		}
		if (this.Tremorrest_lue!=null){
			xmlTxt+="\n<xnat_a:lue";
			xmlTxt+=">";
			xmlTxt+=this.Tremorrest_lue.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:lue>";
		}
		if (this.Tremorrest_rle!=null){
			xmlTxt+="\n<xnat_a:rle";
			xmlTxt+=">";
			xmlTxt+=this.Tremorrest_rle.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:rle>";
		}
		if (this.Tremorrest_lle!=null){
			xmlTxt+="\n<xnat_a:lle";
			xmlTxt+=">";
			xmlTxt+=this.Tremorrest_lle.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:lle>";
		}
				xmlTxt+="\n</xnat_a:tremorRest>";
			}
			}

			var child2=0;
			var att2=0;
			if(this.Rigidity_lle!=null)
			child2++;
			if(this.Rigidity_rue!=null)
			child2++;
			if(this.Rigidity_lue!=null)
			child2++;
			if(this.Rigidity_rle!=null)
			child2++;
			if(this.Rigidity_neck!=null)
			child2++;
			if(child2>0 || att2>0){
				xmlTxt+="\n<xnat_a:rigidity";
			if(child2==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Rigidity_neck!=null){
			xmlTxt+="\n<xnat_a:neck";
			xmlTxt+=">";
			xmlTxt+=this.Rigidity_neck.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:neck>";
		}
		if (this.Rigidity_rue!=null){
			xmlTxt+="\n<xnat_a:rue";
			xmlTxt+=">";
			xmlTxt+=this.Rigidity_rue.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:rue>";
		}
		if (this.Rigidity_lue!=null){
			xmlTxt+="\n<xnat_a:lue";
			xmlTxt+=">";
			xmlTxt+=this.Rigidity_lue.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:lue>";
		}
		if (this.Rigidity_rle!=null){
			xmlTxt+="\n<xnat_a:rle";
			xmlTxt+=">";
			xmlTxt+=this.Rigidity_rle.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:rle>";
		}
		if (this.Rigidity_lle!=null){
			xmlTxt+="\n<xnat_a:lle";
			xmlTxt+=">";
			xmlTxt+=this.Rigidity_lle.replace(/>/g,"&gt;").replace(/</g,"&lt;");
			xmlTxt+="</xnat_a:lle>";
		}
				xmlTxt+="\n</xnat_a:rigidity>";
			}
			}

			var child3=0;
			var att3=0;
			if(this.Handmovementsgrip_left!=null)
			child3++;
			if(this.Handmovementsgrip_right!=null)
			child3++;
			if(child3>0 || att3>0){
				xmlTxt+="\n<xnat_a:handMovementsGrip";
			if(child3==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Handmovementsgrip_right!=null){
			xmlTxt+="\n<xnat_a:right";
			xmlTxt+=">";
			xmlTxt+=this.Handmovementsgrip_right;
			xmlTxt+="</xnat_a:right>";
		}
		if (this.Handmovementsgrip_left!=null){
			xmlTxt+="\n<xnat_a:left";
			xmlTxt+=">";
			xmlTxt+=this.Handmovementsgrip_left;
			xmlTxt+="</xnat_a:left>";
		}
				xmlTxt+="\n</xnat_a:handMovementsGrip>";
			}
			}

		if (this.Facialexpression!=null){
			xmlTxt+="\n<xnat_a:facialExpression";
			xmlTxt+=">";
			xmlTxt+=this.Facialexpression;
			xmlTxt+="</xnat_a:facialExpression>";
		}
		if (this.Gait!=null){
			xmlTxt+="\n<xnat_a:gait";
			xmlTxt+=">";
			xmlTxt+=this.Gait;
			xmlTxt+="</xnat_a:gait>";
		}
		if (this.Speech!=null){
			xmlTxt+="\n<xnat_a:speech";
			xmlTxt+=">";
			xmlTxt+=this.Speech;
			xmlTxt+="</xnat_a:speech>";
		}
		if (this.Writing!=null){
			xmlTxt+="\n<xnat_a:writing";
			xmlTxt+=">";
			xmlTxt+=this.Writing;
			xmlTxt+="</xnat_a:writing>";
		}
		if (this.Arisefromchair!=null){
			xmlTxt+="\n<xnat_a:ariseFromChair";
			xmlTxt+=">";
			xmlTxt+=this.Arisefromchair;
			xmlTxt+="</xnat_a:ariseFromChair>";
		}
			var child4=0;
			var att4=0;
			if(this.Actionposturaltremor_right!=null)
			child4++;
			if(this.Actionposturaltremor_left!=null)
			child4++;
			if(child4>0 || att4>0){
				xmlTxt+="\n<xnat_a:actionPosturalTremor";
			if(child4==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Actionposturaltremor_right!=null){
			xmlTxt+="\n<xnat_a:right";
			xmlTxt+=">";
			xmlTxt+=this.Actionposturaltremor_right;
			xmlTxt+="</xnat_a:right>";
		}
		if (this.Actionposturaltremor_left!=null){
			xmlTxt+="\n<xnat_a:left";
			xmlTxt+=">";
			xmlTxt+=this.Actionposturaltremor_left;
			xmlTxt+="</xnat_a:left>";
		}
				xmlTxt+="\n</xnat_a:actionPosturalTremor>";
			}
			}

			var child5=0;
			var att5=0;
			if(this.Handsram_right!=null)
			child5++;
			if(this.Handsram_left!=null)
			child5++;
			if(child5>0 || att5>0){
				xmlTxt+="\n<xnat_a:handsRAM";
			if(child5==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Handsram_right!=null){
			xmlTxt+="\n<xnat_a:right";
			xmlTxt+=">";
			xmlTxt+=this.Handsram_right;
			xmlTxt+="</xnat_a:right>";
		}
		if (this.Handsram_left!=null){
			xmlTxt+="\n<xnat_a:left";
			xmlTxt+=">";
			xmlTxt+=this.Handsram_left;
			xmlTxt+="</xnat_a:left>";
		}
				xmlTxt+="\n</xnat_a:handsRAM>";
			}
			}

			var child6=0;
			var att6=0;
			if(this.Fingertaps_left!=null)
			child6++;
			if(this.Fingertaps_right!=null)
			child6++;
			if(child6>0 || att6>0){
				xmlTxt+="\n<xnat_a:fingerTaps";
			if(child6==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Fingertaps_right!=null){
			xmlTxt+="\n<xnat_a:right";
			xmlTxt+=">";
			xmlTxt+=this.Fingertaps_right;
			xmlTxt+="</xnat_a:right>";
		}
		if (this.Fingertaps_left!=null){
			xmlTxt+="\n<xnat_a:left";
			xmlTxt+=">";
			xmlTxt+=this.Fingertaps_left;
			xmlTxt+="</xnat_a:left>";
		}
				xmlTxt+="\n</xnat_a:fingerTaps>";
			}
			}

		if (this.Posture!=null){
			xmlTxt+="\n<xnat_a:posture";
			xmlTxt+=">";
			xmlTxt+=this.Posture;
			xmlTxt+="</xnat_a:posture>";
		}
		if (this.Posturalstability!=null){
			xmlTxt+="\n<xnat_a:posturalStability";
			xmlTxt+=">";
			xmlTxt+=this.Posturalstability;
			xmlTxt+="</xnat_a:posturalStability>";
		}
			var child7=0;
			var att7=0;
			if(this.Foottaps_left!=null)
			child7++;
			if(this.Foottaps_right!=null)
			child7++;
			if(child7>0 || att7>0){
				xmlTxt+="\n<xnat_a:footTaps";
			if(child7==0){
				xmlTxt+="/>";
			}else{
				xmlTxt+=">";
		if (this.Foottaps_right!=null){
			xmlTxt+="\n<xnat_a:right";
			xmlTxt+=">";
			xmlTxt+=this.Foottaps_right;
			xmlTxt+="</xnat_a:right>";
		}
		if (this.Foottaps_left!=null){
			xmlTxt+="\n<xnat_a:left";
			xmlTxt+=">";
			xmlTxt+=this.Foottaps_left;
			xmlTxt+="</xnat_a:left>";
		}
				xmlTxt+="\n</xnat_a:footTaps>";
			}
			}

		if (this.Bodybradykinesiahypokinesia!=null){
			xmlTxt+="\n<xnat_a:bodyBradykinesiaHypokinesia";
			xmlTxt+=">";
			xmlTxt+=this.Bodybradykinesiahypokinesia;
			xmlTxt+="</xnat_a:bodyBradykinesiaHypokinesia>";
		}
		if (this.Problem!=null){
			xmlTxt+="\n<xnat_a:problem";
			xmlTxt+=">";
			xmlTxt+=this.Problem;
			xmlTxt+="</xnat_a:problem>";
		}
		return xmlTxt;
	}


	this.hasXMLComments=function(){
	}


	this.hasXMLBodyContent=function(){
		if (this.Inscanner!=null) return true;
			if(this.Clicker_left!=null) return true;
			if(this.Clicker_interval!=null) return true;
			if(this.Clicker_right!=null) return true;
			if(this.Tremorrest_lue!=null) return true;
			if(this.Tremorrest_rle!=null) return true;
			if(this.Tremorrest_lle!=null) return true;
			if(this.Tremorrest_face!=null) return true;
			if(this.Tremorrest_rue!=null) return true;
			if(this.Rigidity_lle!=null) return true;
			if(this.Rigidity_rue!=null) return true;
			if(this.Rigidity_lue!=null) return true;
			if(this.Rigidity_rle!=null) return true;
			if(this.Rigidity_neck!=null) return true;
			if(this.Handmovementsgrip_left!=null) return true;
			if(this.Handmovementsgrip_right!=null) return true;
		if (this.Facialexpression!=null) return true;
		if (this.Gait!=null) return true;
		if (this.Speech!=null) return true;
		if (this.Writing!=null) return true;
		if (this.Arisefromchair!=null) return true;
			if(this.Actionposturaltremor_right!=null) return true;
			if(this.Actionposturaltremor_left!=null) return true;
			if(this.Handsram_right!=null) return true;
			if(this.Handsram_left!=null) return true;
			if(this.Fingertaps_left!=null) return true;
			if(this.Fingertaps_right!=null) return true;
		if (this.Posture!=null) return true;
		if (this.Posturalstability!=null) return true;
			if(this.Foottaps_left!=null) return true;
			if(this.Foottaps_right!=null) return true;
		if (this.Bodybradykinesiahypokinesia!=null) return true;
		if (this.Problem!=null) return true;
		if(this.hasXMLComments())return true;
		if(this.extension.hasXMLBodyContent())return true;
		return false;
	}
}
