

/*
 * web: ClassMapping.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

function ClassMapping(){
	this.newInstance=function(name){
		if(name=="xnat:DXScan"){
			if(window.xnat_dxScanData==undefined)dynamicJSLoad('xnat_dxScanData','generated/xnat_dxScanData.js');
			return new xnat_dxScanData();
		}
		if(name=="xnat:ECGSession"){
			if(window.xnat_ecgSessionData==undefined)dynamicJSLoad('xnat_ecgSessionData','generated/xnat_ecgSessionData.js');
			return new xnat_ecgSessionData();
		}
		if(name=="xdat:search_field"){
			if(window.xdat_search_field==undefined)dynamicJSLoad('xdat_search_field','generated/xdat_search_field.js');
			return new xdat_search_field();
		}
		if(name=="http://nrg.wustl.edu/xnat:regionResource"){
			if(window.xnat_regionResource==undefined)dynamicJSLoad('xnat_regionResource','generated/xnat_regionResource.js');
			return new xnat_regionResource();
		}
		if(name=="http://nrg.wustl.edu/xnat:MEGScan"){
			if(window.xnat_megScanData==undefined)dynamicJSLoad('xnat_megScanData','generated/xnat_megScanData.js');
			return new xnat_megScanData();
		}
		if(name=="xnat:addField"){
			if(window.xnat_addField==undefined)dynamicJSLoad('xnat_addField','generated/xnat_addField.js');
			return new xnat_addField();
		}
		if(name=="xnat_a:YBOCS"){
			if(window.xnat_a_ybocsData==undefined)dynamicJSLoad('xnat_a_ybocsData','generated/xnat_a_ybocsData.js');
			return new xnat_a_ybocsData();
		}
		if(name=="xnat:dx3DCraniofacialScanData"){
			if(window.xnat_dx3DCraniofacialScanData==undefined)dynamicJSLoad('xnat_dx3DCraniofacialScanData','generated/xnat_dx3DCraniofacialScanData.js');
			return new xnat_dx3DCraniofacialScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:esvSessionData"){
			if(window.xnat_esvSessionData==undefined)dynamicJSLoad('xnat_esvSessionData','generated/xnat_esvSessionData.js');
			return new xnat_esvSessionData();
		}
		if(name=="cat:entry_tag"){
			if(window.cat_entry_tag==undefined)dynamicJSLoad('cat_entry_tag','generated/cat_entry_tag.js');
			return new cat_entry_tag();
		}
		if(name=="http://nrg.wustl.edu/xnat:XA3DScan"){
			if(window.xnat_xa3DScanData==undefined)dynamicJSLoad('xnat_xa3DScanData','generated/xnat_xa3DScanData.js');
			return new xnat_xa3DScanData();
		}
		if(name=="http://nrg.wustl.edu/security:change_info"){
			if(window.xdat_change_info==undefined)dynamicJSLoad('xdat_change_info','generated/xdat_change_info.js');
			return new xdat_change_info();
		}
		if(name=="xnat:mgSessionData"){
			if(window.xnat_mgSessionData==undefined)dynamicJSLoad('xnat_mgSessionData','generated/xnat_mgSessionData.js');
			return new xnat_mgSessionData();
		}
		if(name=="xnat_a:sideEffectsPittsburghData"){
			if(window.xnat_a_sideEffectsPittsburghData==undefined)dynamicJSLoad('xnat_a_sideEffectsPittsburghData','generated/xnat_a_sideEffectsPittsburghData.js');
			return new xnat_a_sideEffectsPittsburghData();
		}
		if(name=="xnat_a:SideEffectsPittsburgh"){
			if(window.xnat_a_sideEffectsPittsburghData==undefined)dynamicJSLoad('xnat_a_sideEffectsPittsburghData','generated/xnat_a_sideEffectsPittsburghData.js');
			return new xnat_a_sideEffectsPittsburghData();
		}
		if(name=="xnat:RTImageScan"){
			if(window.xnat_rtImageScanData==undefined)dynamicJSLoad('xnat_rtImageScanData','generated/xnat_rtImageScanData.js');
			return new xnat_rtImageScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:EEGSession"){
			if(window.xnat_eegSessionData==undefined)dynamicJSLoad('xnat_eegSessionData','generated/xnat_eegSessionData.js');
			return new xnat_eegSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:validationData"){
			if(window.xnat_validationData==undefined)dynamicJSLoad('xnat_validationData','generated/xnat_validationData.js');
			return new xnat_validationData();
		}
		if(name=="http://nrg.wustl.edu/workflow:abstractExecutionEnvironment"){
			if(window.wrk_abstractExecutionEnvironment==undefined)dynamicJSLoad('wrk_abstractExecutionEnvironment','generated/wrk_abstractExecutionEnvironment.js');
			return new wrk_abstractExecutionEnvironment();
		}
		if(name=="http://nrg.wustl.edu/security:primary_security_field"){
			if(window.xdat_primary_security_field==undefined)dynamicJSLoad('xdat_primary_security_field','generated/xdat_primary_security_field.js');
			return new xdat_primary_security_field();
		}
		if(name=="xnat:segScanData"){
			if(window.xnat_segScanData==undefined)dynamicJSLoad('xnat_segScanData','generated/xnat_segScanData.js');
			return new xnat_segScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ResourceCatalog"){
			if(window.xnat_resourceCatalog==undefined)dynamicJSLoad('xnat_resourceCatalog','generated/xnat_resourceCatalog.js');
			return new xnat_resourceCatalog();
		}
		if(name=="xnat:optSessionData"){
			if(window.xnat_optSessionData==undefined)dynamicJSLoad('xnat_optSessionData','generated/xnat_optSessionData.js');
			return new xnat_optSessionData();
		}
		if(name=="xnat:fieldDefinitionGroup"){
			if(window.xnat_fieldDefinitionGroup==undefined)dynamicJSLoad('xnat_fieldDefinitionGroup','generated/xnat_fieldDefinitionGroup.js');
			return new xnat_fieldDefinitionGroup();
		}
		if(name=="http://nrg.wustl.edu/security:stored_search"){
			if(window.xdat_stored_search==undefined)dynamicJSLoad('xdat_stored_search','generated/xdat_stored_search.js');
			return new xdat_stored_search();
		}
		if(name=="xnat:ESVSession"){
			if(window.xnat_esvSessionData==undefined)dynamicJSLoad('xnat_esvSessionData','generated/xnat_esvSessionData.js');
			return new xnat_esvSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:HDScan"){
			if(window.xnat_hdScanData==undefined)dynamicJSLoad('xnat_hdScanData','generated/xnat_hdScanData.js');
			return new xnat_hdScanData();
		}
		if(name=="xnat:projectData_alias"){
			if(window.xnat_projectData_alias==undefined)dynamicJSLoad('xnat_projectData_alias','generated/xnat_projectData_alias.js');
			return new xnat_projectData_alias();
		}
		if(name=="http://nrg.wustl.edu/xnat:mgScanData"){
			if(window.xnat_mgScanData==undefined)dynamicJSLoad('xnat_mgScanData','generated/xnat_mgScanData.js');
			return new xnat_mgScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:mrQcScanData"){
			if(window.xnat_mrQcScanData==undefined)dynamicJSLoad('xnat_mrQcScanData','generated/xnat_mrQcScanData.js');
			return new xnat_mrQcScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:imageScanData"){
			if(window.xnat_imageScanData==undefined)dynamicJSLoad('xnat_imageScanData','generated/xnat_imageScanData.js');
			return new xnat_imageScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:resourceSeries"){
			if(window.xnat_resourceSeries==undefined)dynamicJSLoad('xnat_resourceSeries','generated/xnat_resourceSeries.js');
			return new xnat_resourceSeries();
		}
		if(name=="http://nrg.wustl.edu/xnat:mrAssessorData"){
			if(window.xnat_mrAssessorData==undefined)dynamicJSLoad('xnat_mrAssessorData','generated/xnat_mrAssessorData.js');
			return new xnat_mrAssessorData();
		}
		if(name=="xnat_a:ygtssData"){
			if(window.xnat_a_ygtssData==undefined)dynamicJSLoad('xnat_a_ygtssData','generated/xnat_a_ygtssData.js');
			return new xnat_a_ygtssData();
		}
		if(name=="xnat:Generic"){
			if(window.xnat_genericData==undefined)dynamicJSLoad('xnat_genericData','generated/xnat_genericData.js');
			return new xnat_genericData();
		}
		if(name=="prov:process"){
			if(window.prov_process==undefined)dynamicJSLoad('prov_process','generated/prov_process.js');
			return new prov_process();
		}
		if(name=="http://nrg.wustl.edu/xnat:ImageRegionResource"){
			if(window.xnat_regionResource==undefined)dynamicJSLoad('xnat_regionResource','generated/xnat_regionResource.js');
			return new xnat_regionResource();
		}
		if(name=="xdat:security"){
			if(window.xdat_security==undefined)dynamicJSLoad('xdat_security','generated/xdat_security.js');
			return new xdat_security();
		}
		if(name=="http://nrg.wustl.edu/xnat:IOScan"){
			if(window.xnat_ioScanData==undefined)dynamicJSLoad('xnat_ioScanData','generated/xnat_ioScanData.js');
			return new xnat_ioScanData();
		}
		if(name=="xdat:user_login"){
			if(window.xdat_user_login==undefined)dynamicJSLoad('xdat_user_login','generated/xdat_user_login.js');
			return new xdat_user_login();
		}
		if(name=="http://nrg.wustl.edu/xnat:PETScan"){
			if(window.xnat_petScanData==undefined)dynamicJSLoad('xnat_petScanData','generated/xnat_petScanData.js');
			return new xnat_petScanData();
		}
		if(name=="xnat:petSessionData"){
			if(window.xnat_petSessionData==undefined)dynamicJSLoad('xnat_petSessionData','generated/xnat_petSessionData.js');
			return new xnat_petSessionData();
		}
		if(name=="http://nrg.wustl.edu/security:access_log"){
			if(window.xdat_access_log==undefined)dynamicJSLoad('xdat_access_log','generated/xdat_access_log.js');
			return new xdat_access_log();
		}
		if(name=="http://nrg.wustl.edu/security:element_access_secure_ip"){
			if(window.xdat_element_access_secure_ip==undefined)dynamicJSLoad('xdat_element_access_secure_ip','generated/xdat_element_access_secure_ip.js');
			return new xdat_element_access_secure_ip();
		}
		if(name=="http://nrg.wustl.edu/xnat:hdScanData"){
			if(window.xnat_hdScanData==undefined)dynamicJSLoad('xnat_hdScanData','generated/xnat_hdScanData.js');
			return new xnat_hdScanData();
		}
		if(name=="xnat:qcManualAssessorData"){
			if(window.xnat_qcManualAssessorData==undefined)dynamicJSLoad('xnat_qcManualAssessorData','generated/xnat_qcManualAssessorData.js');
			return new xnat_qcManualAssessorData();
		}
		if(name=="cat:catalog_tag"){
			if(window.cat_catalog_tag==undefined)dynamicJSLoad('cat_catalog_tag','generated/cat_catalog_tag.js');
			return new cat_catalog_tag();
		}
		if(name=="http://nrg.wustl.edu/xnat:projectData"){
			if(window.xnat_projectData==undefined)dynamicJSLoad('xnat_projectData','generated/xnat_projectData.js');
			return new xnat_projectData();
		}
		if(name=="scr:screeningAssessment"){
			if(window.scr_screeningAssessment==undefined)dynamicJSLoad('scr_screeningAssessment','generated/scr_screeningAssessment.js');
			return new scr_screeningAssessment();
		}
		if(name=="xnat:gmvSessionData"){
			if(window.xnat_gmvSessionData==undefined)dynamicJSLoad('xnat_gmvSessionData','generated/xnat_gmvSessionData.js');
			return new xnat_gmvSessionData();
		}
		if(name=="xnat:investigatorData"){
			if(window.xnat_investigatorData==undefined)dynamicJSLoad('xnat_investigatorData','generated/xnat_investigatorData.js');
			return new xnat_investigatorData();
		}
		if(name=="xdat:action_type"){
			if(window.xdat_action_type==undefined)dynamicJSLoad('xdat_action_type','generated/xdat_action_type.js');
			return new xdat_action_type();
		}
		if(name=="xnat:NMScan"){
			if(window.xnat_nmScanData==undefined)dynamicJSLoad('xnat_nmScanData','generated/xnat_nmScanData.js');
			return new xnat_nmScanData();
		}
		if(name=="http://nrg.wustl.edu/security:element_security_listing_action"){
			if(window.xdat_element_security_listing_action==undefined)dynamicJSLoad('xdat_element_security_listing_action','generated/xdat_element_security_listing_action.js');
			return new xdat_element_security_listing_action();
		}
		if(name=="http://nrg.wustl.edu/xnat:dx3DCraniofacialSessionData"){
			if(window.xnat_dx3DCraniofacialSessionData==undefined)dynamicJSLoad('xnat_dx3DCraniofacialSessionData','generated/xnat_dx3DCraniofacialSessionData.js');
			return new xnat_dx3DCraniofacialSessionData();
		}
		if(name=="http://nrg.wustl.edu/catalog:dcmEntry"){
			if(window.cat_dcmEntry==undefined)dynamicJSLoad('cat_dcmEntry','generated/cat_dcmEntry.js');
			return new cat_dcmEntry();
		}
		if(name=="xdat:criteria_set"){
			if(window.xdat_criteria_set==undefined)dynamicJSLoad('xdat_criteria_set','generated/xdat_criteria_set.js');
			return new xdat_criteria_set();
		}
		if(name=="http://nrg.wustl.edu/xnat:xcvSessionData"){
			if(window.xnat_xcvSessionData==undefined)dynamicJSLoad('xnat_xcvSessionData','generated/xnat_xcvSessionData.js');
			return new xnat_xcvSessionData();
		}
		if(name=="xdat:News"){
			if(window.xdat_newsEntry==undefined)dynamicJSLoad('xdat_newsEntry','generated/xdat_newsEntry.js');
			return new xdat_newsEntry();
		}
		if(name=="xdat:element_security"){
			if(window.xdat_element_security==undefined)dynamicJSLoad('xdat_element_security','generated/xdat_element_security.js');
			return new xdat_element_security();
		}
		if(name=="xdat:element_access"){
			if(window.xdat_element_access==undefined)dynamicJSLoad('xdat_element_access','generated/xdat_element_access.js');
			return new xdat_element_access();
		}
		if(name=="wrk:Workflow"){
			if(window.wrk_workflowData==undefined)dynamicJSLoad('wrk_workflowData','generated/wrk_workflowData.js');
			return new wrk_workflowData();
		}
		if(name=="http://nrg.wustl.edu/xnat:petAssessorData"){
			if(window.xnat_petAssessorData==undefined)dynamicJSLoad('xnat_petAssessorData','generated/xnat_petAssessorData.js');
			return new xnat_petAssessorData();
		}
		if(name=="http://nrg.wustl.edu/xnat:OPScan"){
			if(window.xnat_opScanData==undefined)dynamicJSLoad('xnat_opScanData','generated/xnat_opScanData.js');
			return new xnat_opScanData();
		}
		if(name=="http://nrg.wustl.edu/workflow:workflowData"){
			if(window.wrk_workflowData==undefined)dynamicJSLoad('wrk_workflowData','generated/wrk_workflowData.js');
			return new wrk_workflowData();
		}
		if(name=="xnat:subjectData"){
			if(window.xnat_subjectData==undefined)dynamicJSLoad('xnat_subjectData','generated/xnat_subjectData.js');
			return new xnat_subjectData();
		}
		if(name=="pipe:pipelineDetails"){
			if(window.pipe_pipelineDetails==undefined)dynamicJSLoad('pipe_pipelineDetails','generated/pipe_pipelineDetails.js');
			return new pipe_pipelineDetails();
		}
		if(name=="xnat:NMSession"){
			if(window.xnat_nmSessionData==undefined)dynamicJSLoad('xnat_nmSessionData','generated/xnat_nmSessionData.js');
			return new xnat_nmSessionData();
		}
		if(name=="xnat:RFSession"){
			if(window.xnat_rfSessionData==undefined)dynamicJSLoad('xnat_rfSessionData','generated/xnat_rfSessionData.js');
			return new xnat_rfSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:XCVSession"){
			if(window.xnat_xcvSessionData==undefined)dynamicJSLoad('xnat_xcvSessionData','generated/xnat_xcvSessionData.js');
			return new xnat_xcvSessionData();
		}
		if(name=="xdat:newsEntry"){
			if(window.xdat_newsEntry==undefined)dynamicJSLoad('xdat_newsEntry','generated/xdat_newsEntry.js');
			return new xdat_newsEntry();
		}
		if(name=="http://nrg.wustl.edu/xnat:esScanData"){
			if(window.xnat_esScanData==undefined)dynamicJSLoad('xnat_esScanData','generated/xnat_esScanData.js');
			return new xnat_esScanData();
		}
		if(name=="xnat:imageResource"){
			if(window.xnat_imageResource==undefined)dynamicJSLoad('xnat_imageResource','generated/xnat_imageResource.js');
			return new xnat_imageResource();
		}
		if(name=="xnat:volumetricRegion_subregion"){
			if(window.xnat_volumetricRegion_subregion==undefined)dynamicJSLoad('xnat_volumetricRegion_subregion','generated/xnat_volumetricRegion_subregion.js');
			return new xnat_volumetricRegion_subregion();
		}
		if(name=="http://nrg.wustl.edu/security:user"){
			if(window.xdat_user==undefined)dynamicJSLoad('xdat_user','generated/xdat_user.js');
			return new xdat_user();
		}
		if(name=="val:protocolData_comment"){
			if(window.val_protocolData_comment==undefined)dynamicJSLoad('val_protocolData_comment','generated/val_protocolData_comment.js');
			return new val_protocolData_comment();
		}
		if(name=="http://nrg.wustl.edu/xnat:optSessionData"){
			if(window.xnat_optSessionData==undefined)dynamicJSLoad('xnat_optSessionData','generated/xnat_optSessionData.js');
			return new xnat_optSessionData();
		}
		if(name=="xnat_a:UPDRS3"){
			if(window.xnat_a_updrs3Data==undefined)dynamicJSLoad('xnat_a_updrs3Data','generated/xnat_a_updrs3Data.js');
			return new xnat_a_updrs3Data();
		}
		if(name=="arc:property"){
			if(window.arc_property==undefined)dynamicJSLoad('arc_property','generated/arc_property.js');
			return new arc_property();
		}
		if(name=="xnat:demographicData"){
			if(window.xnat_demographicData==undefined)dynamicJSLoad('xnat_demographicData','generated/xnat_demographicData.js');
			return new xnat_demographicData();
		}
		if(name=="http://nrg.wustl.edu/catalog:Catalog"){
			if(window.cat_catalog==undefined)dynamicJSLoad('cat_catalog','generated/cat_catalog.js');
			return new cat_catalog();
		}
		if(name=="http://nrg.wustl.edu/xnat:fieldDefinitionGroup_field_possibleValue"){
			if(window.xnat_fieldDefinitionGroup_field_possibleValue==undefined)dynamicJSLoad('xnat_fieldDefinitionGroup_field_possibleValue','generated/xnat_fieldDefinitionGroup_field_possibleValue.js');
			return new xnat_fieldDefinitionGroup_field_possibleValue();
		}
		if(name=="http://nrg.wustl.edu/arc:pathInfo"){
			if(window.arc_pathInfo==undefined)dynamicJSLoad('arc_pathInfo','generated/arc_pathInfo.js');
			return new arc_pathInfo();
		}
		if(name=="http://nrg.wustl.edu/xnat:studyProtocol_session"){
			if(window.xnat_studyProtocol_session==undefined)dynamicJSLoad('xnat_studyProtocol_session','generated/xnat_studyProtocol_session.js');
			return new xnat_studyProtocol_session();
		}
		if(name=="xnat:otherQcScanData"){
			if(window.xnat_otherQcScanData==undefined)dynamicJSLoad('xnat_otherQcScanData','generated/xnat_otherQcScanData.js');
			return new xnat_otherQcScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:resourceCatalog"){
			if(window.xnat_resourceCatalog==undefined)dynamicJSLoad('xnat_resourceCatalog','generated/xnat_resourceCatalog.js');
			return new xnat_resourceCatalog();
		}
		if(name=="http://nrg.wustl.edu/xnat:hdSessionData"){
			if(window.xnat_hdSessionData==undefined)dynamicJSLoad('xnat_hdSessionData','generated/xnat_hdSessionData.js');
			return new xnat_hdSessionData();
		}
		if(name=="xnat:xcvSessionData"){
			if(window.xnat_xcvSessionData==undefined)dynamicJSLoad('xnat_xcvSessionData','generated/xnat_xcvSessionData.js');
			return new xnat_xcvSessionData();
		}
		if(name=="xnat:experimentData"){
			if(window.xnat_experimentData==undefined)dynamicJSLoad('xnat_experimentData','generated/xnat_experimentData.js');
			return new xnat_experimentData();
		}
		if(name=="xnat:gmScanData"){
			if(window.xnat_gmScanData==undefined)dynamicJSLoad('xnat_gmScanData','generated/xnat_gmScanData.js');
			return new xnat_gmScanData();
		}
		if(name=="http://nrg.wustl.edu/catalog:entry"){
			if(window.cat_entry==undefined)dynamicJSLoad('cat_entry','generated/cat_entry.js');
			return new cat_entry();
		}
		if(name=="cat:catalog"){
			if(window.cat_catalog==undefined)dynamicJSLoad('cat_catalog','generated/cat_catalog.js');
			return new cat_catalog();
		}
		if(name=="http://nrg.wustl.edu/xnat:ESScan"){
			if(window.xnat_esScanData==undefined)dynamicJSLoad('xnat_esScanData','generated/xnat_esScanData.js');
			return new xnat_esScanData();
		}
		if(name=="xnat:subjectAssessorData"){
			if(window.xnat_subjectAssessorData==undefined)dynamicJSLoad('xnat_subjectAssessorData','generated/xnat_subjectAssessorData.js');
			return new xnat_subjectAssessorData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ecgScanData"){
			if(window.xnat_ecgScanData==undefined)dynamicJSLoad('xnat_ecgScanData','generated/xnat_ecgScanData.js');
			return new xnat_ecgScanData();
		}
		if(name=="xnat:CRSession"){
			if(window.xnat_crSessionData==undefined)dynamicJSLoad('xnat_crSessionData','generated/xnat_crSessionData.js');
			return new xnat_crSessionData();
		}
		if(name=="prov:processStep"){
			if(window.prov_processStep==undefined)dynamicJSLoad('prov_processStep','generated/prov_processStep.js');
			return new prov_processStep();
		}
		if(name=="http://nrg.wustl.edu/xnat:XA3DSession"){
			if(window.xnat_xa3DSessionData==undefined)dynamicJSLoad('xnat_xa3DSessionData','generated/xnat_xa3DSessionData.js');
			return new xnat_xa3DSessionData();
		}
		if(name=="xdat:element_security_listing_action"){
			if(window.xdat_element_security_listing_action==undefined)dynamicJSLoad('xdat_element_security_listing_action','generated/xdat_element_security_listing_action.js');
			return new xdat_element_security_listing_action();
		}
		if(name=="xnat:srScanData"){
			if(window.xnat_srScanData==undefined)dynamicJSLoad('xnat_srScanData','generated/xnat_srScanData.js');
			return new xnat_srScanData();
		}
		if(name=="xnat:statisticsData"){
			if(window.xnat_statisticsData==undefined)dynamicJSLoad('xnat_statisticsData','generated/xnat_statisticsData.js');
			return new xnat_statisticsData();
		}
		if(name=="xnat:smSessionData"){
			if(window.xnat_smSessionData==undefined)dynamicJSLoad('xnat_smSessionData','generated/xnat_smSessionData.js');
			return new xnat_smSessionData();
		}
		if(name=="http://nrg.wustl.edu/val:protocolData_condition"){
			if(window.val_protocolData_condition==undefined)dynamicJSLoad('val_protocolData_condition','generated/val_protocolData_condition.js');
			return new val_protocolData_condition();
		}
		if(name=="xnat:HDSession"){
			if(window.xnat_hdSessionData==undefined)dynamicJSLoad('xnat_hdSessionData','generated/xnat_hdSessionData.js');
			return new xnat_hdSessionData();
		}
		if(name=="http://nrg.wustl.edu/val:protocolData"){
			if(window.val_protocolData==undefined)dynamicJSLoad('val_protocolData','generated/val_protocolData.js');
			return new val_protocolData();
		}
		if(name=="http://nrg.wustl.edu/arc:fieldSpecification"){
			if(window.arc_fieldSpecification==undefined)dynamicJSLoad('arc_fieldSpecification','generated/arc_fieldSpecification.js');
			return new arc_fieldSpecification();
		}
		if(name=="http://nrg.wustl.edu/xnat:PETMRSession"){
			if(window.xnat_petmrSessionData==undefined)dynamicJSLoad('xnat_petmrSessionData','generated/xnat_petmrSessionData.js');
			return new xnat_petmrSessionData();
		}
		if(name=="xdat:bundle"){
			if(window.xdat_stored_search==undefined)dynamicJSLoad('xdat_stored_search','generated/xdat_stored_search.js');
			return new xdat_stored_search();
		}
		if(name=="xnat:DX3DCraniofacialSession"){
			if(window.xnat_dx3DCraniofacialSessionData==undefined)dynamicJSLoad('xnat_dx3DCraniofacialSessionData','generated/xnat_dx3DCraniofacialSessionData.js');
			return new xnat_dx3DCraniofacialSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:EPSScan"){
			if(window.xnat_epsScanData==undefined)dynamicJSLoad('xnat_epsScanData','generated/xnat_epsScanData.js');
			return new xnat_epsScanData();
		}
		if(name=="xnat:gmSessionData"){
			if(window.xnat_gmSessionData==undefined)dynamicJSLoad('xnat_gmSessionData','generated/xnat_gmSessionData.js');
			return new xnat_gmSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:SRScan"){
			if(window.xnat_srScanData==undefined)dynamicJSLoad('xnat_srScanData','generated/xnat_srScanData.js');
			return new xnat_srScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:xaSessionData"){
			if(window.xnat_xaSessionData==undefined)dynamicJSLoad('xnat_xaSessionData','generated/xnat_xaSessionData.js');
			return new xnat_xaSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:petSessionData"){
			if(window.xnat_petSessionData==undefined)dynamicJSLoad('xnat_petSessionData','generated/xnat_petSessionData.js');
			return new xnat_petSessionData();
		}
		if(name=="http://nrg.wustl.edu/arc:project"){
			if(window.arc_project==undefined)dynamicJSLoad('arc_project','generated/arc_project.js');
			return new arc_project();
		}
		if(name=="xnat:MRSScan"){
			if(window.xnat_mrsScanData==undefined)dynamicJSLoad('xnat_mrsScanData','generated/xnat_mrsScanData.js');
			return new xnat_mrsScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:xaScanData"){
			if(window.xnat_xaScanData==undefined)dynamicJSLoad('xnat_xaScanData','generated/xnat_xaScanData.js');
			return new xnat_xaScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:pVisitData"){
			if(window.xnat_pVisitData==undefined)dynamicJSLoad('xnat_pVisitData','generated/xnat_pVisitData.js');
			return new xnat_pVisitData();
		}
		if(name=="http://nrg.wustl.edu/xnat:gmvSessionData"){
			if(window.xnat_gmvSessionData==undefined)dynamicJSLoad('xnat_gmvSessionData','generated/xnat_gmvSessionData.js');
			return new xnat_gmvSessionData();
		}
		if(name=="val:protocolData_scan_check_comment"){
			if(window.val_protocolData_scan_check_comment==undefined)dynamicJSLoad('val_protocolData_scan_check_comment','generated/val_protocolData_scan_check_comment.js');
			return new val_protocolData_scan_check_comment();
		}
		if(name=="http://nrg.wustl.edu/xnat:OPTScan"){
			if(window.xnat_optScanData==undefined)dynamicJSLoad('xnat_optScanData','generated/xnat_optScanData.js');
			return new xnat_optScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:PVisit"){
			if(window.xnat_pVisitData==undefined)dynamicJSLoad('xnat_pVisitData','generated/xnat_pVisitData.js');
			return new xnat_pVisitData();
		}
		if(name=="http://nrg.wustl.edu/xnat:GMScan"){
			if(window.xnat_gmScanData==undefined)dynamicJSLoad('xnat_gmScanData','generated/xnat_gmScanData.js');
			return new xnat_gmScanData();
		}
		if(name=="http://nrg.wustl.edu/security:Info"){
			if(window.xdat_infoEntry==undefined)dynamicJSLoad('xdat_infoEntry','generated/xdat_infoEntry.js');
			return new xdat_infoEntry();
		}
		if(name=="xnat:ioScanData"){
			if(window.xnat_ioScanData==undefined)dynamicJSLoad('xnat_ioScanData','generated/xnat_ioScanData.js');
			return new xnat_ioScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:volumetricRegion"){
			if(window.xnat_volumetricRegion==undefined)dynamicJSLoad('xnat_volumetricRegion','generated/xnat_volumetricRegion.js');
			return new xnat_volumetricRegion();
		}
		if(name=="xnat:nmSessionData"){
			if(window.xnat_nmSessionData==undefined)dynamicJSLoad('xnat_nmSessionData','generated/xnat_nmSessionData.js');
			return new xnat_nmSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:abstractResource_tag"){
			if(window.xnat_abstractResource_tag==undefined)dynamicJSLoad('xnat_abstractResource_tag','generated/xnat_abstractResource_tag.js');
			return new xnat_abstractResource_tag();
		}
		if(name=="http://nrg.wustl.edu/xnat:dicomSeries"){
			if(window.xnat_dicomSeries==undefined)dynamicJSLoad('xnat_dicomSeries','generated/xnat_dicomSeries.js');
			return new xnat_dicomSeries();
		}
		if(name=="xnat:studyProtocol_variable"){
			if(window.xnat_studyProtocol_variable==undefined)dynamicJSLoad('xnat_studyProtocol_variable','generated/xnat_studyProtocol_variable.js');
			return new xnat_studyProtocol_variable();
		}
		if(name=="http://nrg.wustl.edu/xnat:XASession"){
			if(window.xnat_xaSessionData==undefined)dynamicJSLoad('xnat_xaSessionData','generated/xnat_xaSessionData.js');
			return new xnat_xaSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:DX3DCraniofacialSession"){
			if(window.xnat_dx3DCraniofacialSessionData==undefined)dynamicJSLoad('xnat_dx3DCraniofacialSessionData','generated/xnat_dx3DCraniofacialSessionData.js');
			return new xnat_dx3DCraniofacialSessionData();
		}
		if(name=="http://nrg.wustl.edu/security:field_mapping"){
			if(window.xdat_field_mapping==undefined)dynamicJSLoad('xdat_field_mapping','generated/xdat_field_mapping.js');
			return new xdat_field_mapping();
		}
		if(name=="http://nrg.wustl.edu/xnat:mgSessionData"){
			if(window.xnat_mgSessionData==undefined)dynamicJSLoad('xnat_mgSessionData','generated/xnat_mgSessionData.js');
			return new xnat_mgSessionData();
		}
		if(name=="http://nrg.wustl.edu/security:userGroup"){
			if(window.xdat_userGroup==undefined)dynamicJSLoad('xdat_userGroup','generated/xdat_userGroup.js');
			return new xdat_userGroup();
		}
		if(name=="xnat:OPSession"){
			if(window.xnat_opSessionData==undefined)dynamicJSLoad('xnat_opSessionData','generated/xnat_opSessionData.js');
			return new xnat_opSessionData();
		}
		if(name=="xnat:HDScan"){
			if(window.xnat_hdScanData==undefined)dynamicJSLoad('xnat_hdScanData','generated/xnat_hdScanData.js');
			return new xnat_hdScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:projectData_alias"){
			if(window.xnat_projectData_alias==undefined)dynamicJSLoad('xnat_projectData_alias','generated/xnat_projectData_alias.js');
			return new xnat_projectData_alias();
		}
		if(name=="http://nrg.wustl.edu/xnat:subjectVariablesData_variable"){
			if(window.xnat_subjectVariablesData_variable==undefined)dynamicJSLoad('xnat_subjectVariablesData_variable','generated/xnat_subjectVariablesData_variable.js');
			return new xnat_subjectVariablesData_variable();
		}
		if(name=="http://nrg.wustl.edu/xnat:qcAssessmentData_scan"){
			if(window.xnat_qcAssessmentData_scan==undefined)dynamicJSLoad('xnat_qcAssessmentData_scan','generated/xnat_qcAssessmentData_scan.js');
			return new xnat_qcAssessmentData_scan();
		}
		if(name=="xnat:ESSession"){
			if(window.xnat_esSessionData==undefined)dynamicJSLoad('xnat_esSessionData','generated/xnat_esSessionData.js');
			return new xnat_esSessionData();
		}
		if(name=="http://nrg.wustl.edu/val:protocolData_comment"){
			if(window.val_protocolData_comment==undefined)dynamicJSLoad('val_protocolData_comment','generated/val_protocolData_comment.js');
			return new val_protocolData_comment();
		}
		if(name=="http://nrg.wustl.edu/xnat:imageAssessorData"){
			if(window.xnat_imageAssessorData==undefined)dynamicJSLoad('xnat_imageAssessorData','generated/xnat_imageAssessorData.js');
			return new xnat_imageAssessorData();
		}
		if(name=="http://nrg.wustl.edu/xnat:xcSessionData"){
			if(window.xnat_xcSessionData==undefined)dynamicJSLoad('xnat_xcSessionData','generated/xnat_xcSessionData.js');
			return new xnat_xcSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:usScanData"){
			if(window.xnat_usScanData==undefined)dynamicJSLoad('xnat_usScanData','generated/xnat_usScanData.js');
			return new xnat_usScanData();
		}
		if(name=="xnat:crScanData"){
			if(window.xnat_crScanData==undefined)dynamicJSLoad('xnat_crScanData','generated/xnat_crScanData.js');
			return new xnat_crScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:UPDRS3"){
			if(window.xnat_a_updrs3Data==undefined)dynamicJSLoad('xnat_a_updrs3Data','generated/xnat_a_updrs3Data.js');
			return new xnat_a_updrs3Data();
		}
		if(name=="xnat:xcvScanData"){
			if(window.xnat_xcvScanData==undefined)dynamicJSLoad('xnat_xcvScanData','generated/xnat_xcvScanData.js');
			return new xnat_xcvScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:YBOCS"){
			if(window.xnat_a_ybocsData==undefined)dynamicJSLoad('xnat_a_ybocsData','generated/xnat_a_ybocsData.js');
			return new xnat_a_ybocsData();
		}
		if(name=="xnat:IOScan"){
			if(window.xnat_ioScanData==undefined)dynamicJSLoad('xnat_ioScanData','generated/xnat_ioScanData.js');
			return new xnat_ioScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:qcAssessmentData_scan_slice"){
			if(window.xnat_qcAssessmentData_scan_slice==undefined)dynamicJSLoad('xnat_qcAssessmentData_scan_slice','generated/xnat_qcAssessmentData_scan_slice.js');
			return new xnat_qcAssessmentData_scan_slice();
		}
		if(name=="http://nrg.wustl.edu/security:XDATUser"){
			if(window.xdat_user==undefined)dynamicJSLoad('xdat_user','generated/xdat_user.js');
			return new xdat_user();
		}
		if(name=="http://nrg.wustl.edu/xnat:xa3DScanData"){
			if(window.xnat_xa3DScanData==undefined)dynamicJSLoad('xnat_xa3DScanData','generated/xnat_xa3DScanData.js');
			return new xnat_xa3DScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:PETSession"){
			if(window.xnat_petSessionData==undefined)dynamicJSLoad('xnat_petSessionData','generated/xnat_petSessionData.js');
			return new xnat_petSessionData();
		}
		if(name=="http://nrg.wustl.edu/arc:pipelineData"){
			if(window.arc_pipelineData==undefined)dynamicJSLoad('arc_pipelineData','generated/arc_pipelineData.js');
			return new arc_pipelineData();
		}
		if(name=="xnat:voiceAudioScanData"){
			if(window.xnat_voiceAudioScanData==undefined)dynamicJSLoad('xnat_voiceAudioScanData','generated/xnat_voiceAudioScanData.js');
			return new xnat_voiceAudioScanData();
		}
		if(name=="cat:dcmCatalog"){
			if(window.cat_dcmCatalog==undefined)dynamicJSLoad('cat_dcmCatalog','generated/cat_dcmCatalog.js');
			return new cat_dcmCatalog();
		}
		if(name=="xnat:ESVScan"){
			if(window.xnat_esvScanData==undefined)dynamicJSLoad('xnat_esvScanData','generated/xnat_esvScanData.js');
			return new xnat_esvScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ctScanData"){
			if(window.xnat_ctScanData==undefined)dynamicJSLoad('xnat_ctScanData','generated/xnat_ctScanData.js');
			return new xnat_ctScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:optScanData"){
			if(window.xnat_optScanData==undefined)dynamicJSLoad('xnat_optScanData','generated/xnat_optScanData.js');
			return new xnat_optScanData();
		}
		if(name=="xnat:EEGSession"){
			if(window.xnat_eegSessionData==undefined)dynamicJSLoad('xnat_eegSessionData','generated/xnat_eegSessionData.js');
			return new xnat_eegSessionData();
		}
		if(name=="cat:entry_metaField"){
			if(window.cat_entry_metaField==undefined)dynamicJSLoad('cat_entry_metaField','generated/cat_entry_metaField.js');
			return new cat_entry_metaField();
		}
		if(name=="http://nrg.wustl.edu/xnat:subjectAssessorData"){
			if(window.xnat_subjectAssessorData==undefined)dynamicJSLoad('xnat_subjectAssessorData','generated/xnat_subjectAssessorData.js');
			return new xnat_subjectAssessorData();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:ybocsData"){
			if(window.xnat_a_ybocsData==undefined)dynamicJSLoad('xnat_a_ybocsData','generated/xnat_a_ybocsData.js');
			return new xnat_a_ybocsData();
		}
		if(name=="http://nrg.wustl.edu/xnat:XCScan"){
			if(window.xnat_xcScanData==undefined)dynamicJSLoad('xnat_xcScanData','generated/xnat_xcScanData.js');
			return new xnat_xcScanData();
		}
		if(name=="xnat:ecgScanData"){
			if(window.xnat_ecgScanData==undefined)dynamicJSLoad('xnat_ecgScanData','generated/xnat_ecgScanData.js');
			return new xnat_ecgScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:experimentData"){
			if(window.xnat_experimentData==undefined)dynamicJSLoad('xnat_experimentData','generated/xnat_experimentData.js');
			return new xnat_experimentData();
		}
		if(name=="xnat:XA3DSession"){
			if(window.xnat_xa3DSessionData==undefined)dynamicJSLoad('xnat_xa3DSessionData','generated/xnat_xa3DSessionData.js');
			return new xnat_xa3DSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:rtImageScanData"){
			if(window.xnat_rtImageScanData==undefined)dynamicJSLoad('xnat_rtImageScanData','generated/xnat_rtImageScanData.js');
			return new xnat_rtImageScanData();
		}
		if(name=="http://www.nbirn.net/prov:process"){
			if(window.prov_process==undefined)dynamicJSLoad('prov_process','generated/prov_process.js');
			return new prov_process();
		}
		if(name=="http://nrg.wustl.edu/xnat:SubjectVariables"){
			if(window.xnat_subjectVariablesData==undefined)dynamicJSLoad('xnat_subjectVariablesData','generated/xnat_subjectVariablesData.js');
			return new xnat_subjectVariablesData();
		}
		if(name=="xnat:subjectData_addID"){
			if(window.xnat_subjectData_addID==undefined)dynamicJSLoad('xnat_subjectData_addID','generated/xnat_subjectData_addID.js');
			return new xnat_subjectData_addID();
		}
		if(name=="http://nrg.wustl.edu/scr:ScreeningAssessment"){
			if(window.scr_screeningAssessment==undefined)dynamicJSLoad('scr_screeningAssessment','generated/scr_screeningAssessment.js');
			return new scr_screeningAssessment();
		}
		if(name=="val:protocolData"){
			if(window.val_protocolData==undefined)dynamicJSLoad('val_protocolData','generated/val_protocolData.js');
			return new val_protocolData();
		}
		if(name=="xnat:OPScan"){
			if(window.xnat_opScanData==undefined)dynamicJSLoad('xnat_opScanData','generated/xnat_opScanData.js');
			return new xnat_opScanData();
		}
		if(name=="xnat:publicationResource"){
			if(window.xnat_publicationResource==undefined)dynamicJSLoad('xnat_publicationResource','generated/xnat_publicationResource.js');
			return new xnat_publicationResource();
		}
		if(name=="http://nrg.wustl.edu/xnat:statisticsData"){
			if(window.xnat_statisticsData==undefined)dynamicJSLoad('xnat_statisticsData','generated/xnat_statisticsData.js');
			return new xnat_statisticsData();
		}
		if(name=="http://nrg.wustl.edu/security:criteria"){
			if(window.xdat_criteria==undefined)dynamicJSLoad('xdat_criteria','generated/xdat_criteria.js');
			return new xdat_criteria();
		}
		if(name=="xnat:XA3DScan"){
			if(window.xnat_xa3DScanData==undefined)dynamicJSLoad('xnat_xa3DScanData','generated/xnat_xa3DScanData.js');
			return new xnat_xa3DScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:abstractProtocol"){
			if(window.xnat_abstractProtocol==undefined)dynamicJSLoad('xnat_abstractProtocol','generated/xnat_abstractProtocol.js');
			return new xnat_abstractProtocol();
		}
		if(name=="xnat:subjectMetadata"){
			if(window.xnat_subjectMetadata==undefined)dynamicJSLoad('xnat_subjectMetadata','generated/xnat_subjectMetadata.js');
			return new xnat_subjectMetadata();
		}
		if(name=="http://nrg.wustl.edu/xnat:ctScanData_focalSpot"){
			if(window.xnat_ctScanData_focalSpot==undefined)dynamicJSLoad('xnat_ctScanData_focalSpot','generated/xnat_ctScanData_focalSpot.js');
			return new xnat_ctScanData_focalSpot();
		}
		if(name=="xnat:ioSessionData"){
			if(window.xnat_ioSessionData==undefined)dynamicJSLoad('xnat_ioSessionData','generated/xnat_ioSessionData.js');
			return new xnat_ioSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:scScanData"){
			if(window.xnat_scScanData==undefined)dynamicJSLoad('xnat_scScanData','generated/xnat_scScanData.js');
			return new xnat_scScanData();
		}
		if(name=="xnat:GMVScan"){
			if(window.xnat_gmvScanData==undefined)dynamicJSLoad('xnat_gmvScanData','generated/xnat_gmvScanData.js');
			return new xnat_gmvScanData();
		}
		if(name=="http://nrg.wustl.edu/catalog:catalog_tag"){
			if(window.cat_catalog_tag==undefined)dynamicJSLoad('cat_catalog_tag','generated/cat_catalog_tag.js');
			return new cat_catalog_tag();
		}
		if(name=="xnat:mgScanData"){
			if(window.xnat_mgScanData==undefined)dynamicJSLoad('xnat_mgScanData','generated/xnat_mgScanData.js');
			return new xnat_mgScanData();
		}
		if(name=="xnat:opSessionData"){
			if(window.xnat_opSessionData==undefined)dynamicJSLoad('xnat_opSessionData','generated/xnat_opSessionData.js');
			return new xnat_opSessionData();
		}
		if(name=="http://nrg.wustl.edu/security:element_access"){
			if(window.xdat_element_access==undefined)dynamicJSLoad('xdat_element_access','generated/xdat_element_access.js');
			return new xdat_element_access();
		}
		if(name=="xnat:datatypeProtocol"){
			if(window.xnat_datatypeProtocol==undefined)dynamicJSLoad('xnat_datatypeProtocol','generated/xnat_datatypeProtocol.js');
			return new xnat_datatypeProtocol();
		}
		if(name=="http://nrg.wustl.edu/xnat:genericData"){
			if(window.xnat_genericData==undefined)dynamicJSLoad('xnat_genericData','generated/xnat_genericData.js');
			return new xnat_genericData();
		}
		if(name=="xnat:QCAssessment"){
			if(window.xnat_qcAssessmentData==undefined)dynamicJSLoad('xnat_qcAssessmentData','generated/xnat_qcAssessmentData.js');
			return new xnat_qcAssessmentData();
		}
		if(name=="xnat:reconstructedImageData_scanID"){
			if(window.xnat_reconstructedImageData_scanID==undefined)dynamicJSLoad('xnat_reconstructedImageData_scanID','generated/xnat_reconstructedImageData_scanID.js');
			return new xnat_reconstructedImageData_scanID();
		}
		if(name=="xnat:abstractResource_tag"){
			if(window.xnat_abstractResource_tag==undefined)dynamicJSLoad('xnat_abstractResource_tag','generated/xnat_abstractResource_tag.js');
			return new xnat_abstractResource_tag();
		}
		if(name=="http://nrg.wustl.edu/xnat:rfSessionData"){
			if(window.xnat_rfSessionData==undefined)dynamicJSLoad('xnat_rfSessionData','generated/xnat_rfSessionData.js');
			return new xnat_rfSessionData();
		}
		if(name=="xnat:hdScanData"){
			if(window.xnat_hdScanData==undefined)dynamicJSLoad('xnat_hdScanData','generated/xnat_hdScanData.js');
			return new xnat_hdScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:petQcScanData_processingError"){
			if(window.xnat_petQcScanData_processingError==undefined)dynamicJSLoad('xnat_petQcScanData_processingError','generated/xnat_petQcScanData_processingError.js');
			return new xnat_petQcScanData_processingError();
		}
		if(name=="http://nrg.wustl.edu/xnat:epsScanData"){
			if(window.xnat_epsScanData==undefined)dynamicJSLoad('xnat_epsScanData','generated/xnat_epsScanData.js');
			return new xnat_epsScanData();
		}
		if(name=="xnat:xa3DSessionData"){
			if(window.xnat_xa3DSessionData==undefined)dynamicJSLoad('xnat_xa3DSessionData','generated/xnat_xa3DSessionData.js');
			return new xnat_xa3DSessionData();
		}
		if(name=="http://nrg.wustl.edu/arc:project_descendant_pipeline"){
			if(window.arc_project_descendant_pipeline==undefined)dynamicJSLoad('arc_project_descendant_pipeline','generated/arc_project_descendant_pipeline.js');
			return new arc_project_descendant_pipeline();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:updrs3Data"){
			if(window.xnat_a_updrs3Data==undefined)dynamicJSLoad('xnat_a_updrs3Data','generated/xnat_a_updrs3Data.js');
			return new xnat_a_updrs3Data();
		}
		if(name=="xnat:ESScan"){
			if(window.xnat_esScanData==undefined)dynamicJSLoad('xnat_esScanData','generated/xnat_esScanData.js');
			return new xnat_esScanData();
		}
		if(name=="xnat:SRSession"){
			if(window.xnat_srSessionData==undefined)dynamicJSLoad('xnat_srSessionData','generated/xnat_srSessionData.js');
			return new xnat_srSessionData();
		}
		if(name=="http://nrg.wustl.edu/arc:pipelineParameterData"){
			if(window.arc_pipelineParameterData==undefined)dynamicJSLoad('arc_pipelineParameterData','generated/arc_pipelineParameterData.js');
			return new arc_pipelineParameterData();
		}
		if(name=="xnat:megSessionData"){
			if(window.xnat_megSessionData==undefined)dynamicJSLoad('xnat_megSessionData','generated/xnat_megSessionData.js');
			return new xnat_megSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:qcAssessmentData"){
			if(window.xnat_qcAssessmentData==undefined)dynamicJSLoad('xnat_qcAssessmentData','generated/xnat_qcAssessmentData.js');
			return new xnat_qcAssessmentData();
		}
		if(name=="http://nrg.wustl.edu/xnat:NMSession"){
			if(window.xnat_nmSessionData==undefined)dynamicJSLoad('xnat_nmSessionData','generated/xnat_nmSessionData.js');
			return new xnat_nmSessionData();
		}
		if(name=="xnat:SRScan"){
			if(window.xnat_srScanData==undefined)dynamicJSLoad('xnat_srScanData','generated/xnat_srScanData.js');
			return new xnat_srScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:RFSession"){
			if(window.xnat_rfSessionData==undefined)dynamicJSLoad('xnat_rfSessionData','generated/xnat_rfSessionData.js');
			return new xnat_rfSessionData();
		}
		if(name=="xdat:XDATUser"){
			if(window.xdat_user==undefined)dynamicJSLoad('xdat_user','generated/xdat_user.js');
			return new xdat_user();
		}
		if(name=="xnat:projectData"){
			if(window.xnat_projectData==undefined)dynamicJSLoad('xnat_projectData','generated/xnat_projectData.js');
			return new xnat_projectData();
		}
		if(name=="arc:pipelineData"){
			if(window.arc_pipelineData==undefined)dynamicJSLoad('arc_pipelineData','generated/arc_pipelineData.js');
			return new arc_pipelineData();
		}
		if(name=="cat:DCMCatalog"){
			if(window.cat_dcmCatalog==undefined)dynamicJSLoad('cat_dcmCatalog','generated/cat_dcmCatalog.js');
			return new cat_dcmCatalog();
		}
		if(name=="xnat:XCVSession"){
			if(window.xnat_xcvSessionData==undefined)dynamicJSLoad('xnat_xcvSessionData','generated/xnat_xcvSessionData.js');
			return new xnat_xcvSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:FieldDefinitionSet"){
			if(window.xnat_fieldDefinitionGroup==undefined)dynamicJSLoad('xnat_fieldDefinitionGroup','generated/xnat_fieldDefinitionGroup.js');
			return new xnat_fieldDefinitionGroup();
		}
		if(name=="xnat:PVisit"){
			if(window.xnat_pVisitData==undefined)dynamicJSLoad('xnat_pVisitData','generated/xnat_pVisitData.js');
			return new xnat_pVisitData();
		}
		if(name=="xnat:GMScan"){
			if(window.xnat_gmScanData==undefined)dynamicJSLoad('xnat_gmScanData','generated/xnat_gmScanData.js');
			return new xnat_gmScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:Generic"){
			if(window.xnat_genericData==undefined)dynamicJSLoad('xnat_genericData','generated/xnat_genericData.js');
			return new xnat_genericData();
		}
		if(name=="xnat:RTSession"){
			if(window.xnat_rtSessionData==undefined)dynamicJSLoad('xnat_rtSessionData','generated/xnat_rtSessionData.js');
			return new xnat_rtSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:publicationResource"){
			if(window.xnat_publicationResource==undefined)dynamicJSLoad('xnat_publicationResource','generated/xnat_publicationResource.js');
			return new xnat_publicationResource();
		}
		if(name=="xnat:ResourceCatalog"){
			if(window.xnat_resourceCatalog==undefined)dynamicJSLoad('xnat_resourceCatalog','generated/xnat_resourceCatalog.js');
			return new xnat_resourceCatalog();
		}
		if(name=="xnat:ecgSessionData"){
			if(window.xnat_ecgSessionData==undefined)dynamicJSLoad('xnat_ecgSessionData','generated/xnat_ecgSessionData.js');
			return new xnat_ecgSessionData();
		}
		if(name=="xnat:crSessionData"){
			if(window.xnat_crSessionData==undefined)dynamicJSLoad('xnat_crSessionData','generated/xnat_crSessionData.js');
			return new xnat_crSessionData();
		}
		if(name=="xnat:SMSession"){
			if(window.xnat_smSessionData==undefined)dynamicJSLoad('xnat_smSessionData','generated/xnat_smSessionData.js');
			return new xnat_smSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:CTScan"){
			if(window.xnat_ctScanData==undefined)dynamicJSLoad('xnat_ctScanData','generated/xnat_ctScanData.js');
			return new xnat_ctScanData();
		}
		if(name=="wrk:xnatExecutionEnvironment_notify"){
			if(window.wrk_xnatExecutionEnvironment_notify==undefined)dynamicJSLoad('wrk_xnatExecutionEnvironment_notify','generated/wrk_xnatExecutionEnvironment_notify.js');
			return new wrk_xnatExecutionEnvironment_notify();
		}
		if(name=="http://nrg.wustl.edu/val:ProtocolVal"){
			if(window.val_protocolData==undefined)dynamicJSLoad('val_protocolData','generated/val_protocolData.js');
			return new val_protocolData();
		}
		if(name=="http://nrg.wustl.edu/security:newsEntry"){
			if(window.xdat_newsEntry==undefined)dynamicJSLoad('xdat_newsEntry','generated/xdat_newsEntry.js');
			return new xdat_newsEntry();
		}
		if(name=="xnat:mrScanData"){
			if(window.xnat_mrScanData==undefined)dynamicJSLoad('xnat_mrScanData','generated/xnat_mrScanData.js');
			return new xnat_mrScanData();
		}
		if(name=="xdat:criteria"){
			if(window.xdat_criteria==undefined)dynamicJSLoad('xdat_criteria','generated/xdat_criteria.js');
			return new xdat_criteria();
		}
		if(name=="http://nrg.wustl.edu/val:protocolData_scan_check_condition"){
			if(window.val_protocolData_scan_check_condition==undefined)dynamicJSLoad('val_protocolData_scan_check_condition','generated/val_protocolData_scan_check_condition.js');
			return new val_protocolData_scan_check_condition();
		}
		if(name=="xnat:mrQcScanData"){
			if(window.xnat_mrQcScanData==undefined)dynamicJSLoad('xnat_mrQcScanData','generated/xnat_mrQcScanData.js');
			return new xnat_mrQcScanData();
		}
		if(name=="xnat:studyProtocol_group"){
			if(window.xnat_studyProtocol_group==undefined)dynamicJSLoad('xnat_studyProtocol_group','generated/xnat_studyProtocol_group.js');
			return new xnat_studyProtocol_group();
		}
		if(name=="xnat:qcScanData_field"){
			if(window.xnat_qcScanData_field==undefined)dynamicJSLoad('xnat_qcScanData_field','generated/xnat_qcScanData_field.js');
			return new xnat_qcScanData_field();
		}
		if(name=="xnat:OtherDicomSession"){
			if(window.xnat_otherDicomSessionData==undefined)dynamicJSLoad('xnat_otherDicomSessionData','generated/xnat_otherDicomSessionData.js');
			return new xnat_otherDicomSessionData();
		}
		if(name=="xnat:USSession"){
			if(window.xnat_usSessionData==undefined)dynamicJSLoad('xnat_usSessionData','generated/xnat_usSessionData.js');
			return new xnat_usSessionData();
		}
		if(name=="cat:catalog_metaField"){
			if(window.cat_catalog_metaField==undefined)dynamicJSLoad('cat_catalog_metaField','generated/cat_catalog_metaField.js');
			return new cat_catalog_metaField();
		}
		if(name=="http://nrg.wustl.edu/xnat:CRSession"){
			if(window.xnat_crSessionData==undefined)dynamicJSLoad('xnat_crSessionData','generated/xnat_crSessionData.js');
			return new xnat_crSessionData();
		}
		if(name=="xnat:imageScanData_share"){
			if(window.xnat_imageScanData_share==undefined)dynamicJSLoad('xnat_imageScanData_share','generated/xnat_imageScanData_share.js');
			return new xnat_imageScanData_share();
		}
		if(name=="http://nrg.wustl.edu/pipe:pipelineDetails_parameter"){
			if(window.pipe_pipelineDetails_parameter==undefined)dynamicJSLoad('pipe_pipelineDetails_parameter','generated/pipe_pipelineDetails_parameter.js');
			return new pipe_pipelineDetails_parameter();
		}
		if(name=="xnat:projectData_field"){
			if(window.xnat_projectData_field==undefined)dynamicJSLoad('xnat_projectData_field','generated/xnat_projectData_field.js');
			return new xnat_projectData_field();
		}
		if(name=="http://nrg.wustl.edu/xnat:gmvScanData"){
			if(window.xnat_gmvScanData==undefined)dynamicJSLoad('xnat_gmvScanData','generated/xnat_gmvScanData.js');
			return new xnat_gmvScanData();
		}
		if(name=="scr:screeningScanData"){
			if(window.scr_screeningScanData==undefined)dynamicJSLoad('scr_screeningScanData','generated/scr_screeningScanData.js');
			return new scr_screeningScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:HDSession"){
			if(window.xnat_hdSessionData==undefined)dynamicJSLoad('xnat_hdSessionData','generated/xnat_hdSessionData.js');
			return new xnat_hdSessionData();
		}
		if(name=="http://nrg.wustl.edu/arc:project_pipeline"){
			if(window.arc_project_pipeline==undefined)dynamicJSLoad('arc_project_pipeline','generated/arc_project_pipeline.js');
			return new arc_project_pipeline();
		}
		if(name=="http://nrg.wustl.edu/xnat:SMScan"){
			if(window.xnat_smScanData==undefined)dynamicJSLoad('xnat_smScanData','generated/xnat_smScanData.js');
			return new xnat_smScanData();
		}
		if(name=="xnat:resource"){
			if(window.xnat_resource==undefined)dynamicJSLoad('xnat_resource','generated/xnat_resource.js');
			return new xnat_resource();
		}
		if(name=="http://nrg.wustl.edu/xnat:experimentData_field"){
			if(window.xnat_experimentData_field==undefined)dynamicJSLoad('xnat_experimentData_field','generated/xnat_experimentData_field.js');
			return new xnat_experimentData_field();
		}
		if(name=="http://www.nbirn.net/prov:processStep"){
			if(window.prov_processStep==undefined)dynamicJSLoad('prov_processStep','generated/prov_processStep.js');
			return new prov_processStep();
		}
		if(name=="xnat:ctSessionData"){
			if(window.xnat_ctSessionData==undefined)dynamicJSLoad('xnat_ctSessionData','generated/xnat_ctSessionData.js');
			return new xnat_ctSessionData();
		}
		if(name=="xnat:xaScanData"){
			if(window.xnat_xaScanData==undefined)dynamicJSLoad('xnat_xaScanData','generated/xnat_xaScanData.js');
			return new xnat_xaScanData();
		}
		if(name=="xnat:eegSessionData"){
			if(window.xnat_eegSessionData==undefined)dynamicJSLoad('xnat_eegSessionData','generated/xnat_eegSessionData.js');
			return new xnat_eegSessionData();
		}
		if(name=="xnat:studyProtocol_condition"){
			if(window.xnat_studyProtocol_condition==undefined)dynamicJSLoad('xnat_studyProtocol_condition','generated/xnat_studyProtocol_condition.js');
			return new xnat_studyProtocol_condition();
		}
		if(name=="cat:Entry"){
			if(window.cat_entry==undefined)dynamicJSLoad('cat_entry','generated/cat_entry.js');
			return new cat_entry();
		}
		if(name=="xnat:XCScan"){
			if(window.xnat_xcScanData==undefined)dynamicJSLoad('xnat_xcScanData','generated/xnat_xcScanData.js');
			return new xnat_xcScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:subjectData_addID"){
			if(window.xnat_subjectData_addID==undefined)dynamicJSLoad('xnat_subjectData_addID','generated/xnat_subjectData_addID.js');
			return new xnat_subjectData_addID();
		}
		if(name=="http://nrg.wustl.edu/xnat:OPSession"){
			if(window.xnat_opSessionData==undefined)dynamicJSLoad('xnat_opSessionData','generated/xnat_opSessionData.js');
			return new xnat_opSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:xcScanData"){
			if(window.xnat_xcScanData==undefined)dynamicJSLoad('xnat_xcScanData','generated/xnat_xcScanData.js');
			return new xnat_xcScanData();
		}
		if(name=="xnat:petAssessorData"){
			if(window.xnat_petAssessorData==undefined)dynamicJSLoad('xnat_petAssessorData','generated/xnat_petAssessorData.js');
			return new xnat_petAssessorData();
		}
		if(name=="http://nrg.wustl.edu/arc:property"){
			if(window.arc_property==undefined)dynamicJSLoad('arc_property','generated/arc_property.js');
			return new arc_property();
		}
		if(name=="http://nrg.wustl.edu/xnat:opScanData"){
			if(window.xnat_opScanData==undefined)dynamicJSLoad('xnat_opScanData','generated/xnat_opScanData.js');
			return new xnat_opScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:Investigator"){
			if(window.xnat_investigatorData==undefined)dynamicJSLoad('xnat_investigatorData','generated/xnat_investigatorData.js');
			return new xnat_investigatorData();
		}
		if(name=="http://nrg.wustl.edu/xnat:megSessionData"){
			if(window.xnat_megSessionData==undefined)dynamicJSLoad('xnat_megSessionData','generated/xnat_megSessionData.js');
			return new xnat_megSessionData();
		}
		if(name=="arc:pipelineParameterData"){
			if(window.arc_pipelineParameterData==undefined)dynamicJSLoad('arc_pipelineParameterData','generated/arc_pipelineParameterData.js');
			return new arc_pipelineParameterData();
		}
		if(name=="xnat:esSessionData"){
			if(window.xnat_esSessionData==undefined)dynamicJSLoad('xnat_esSessionData','generated/xnat_esSessionData.js');
			return new xnat_esSessionData();
		}
		if(name=="http://nrg.wustl.edu/val:protocolData_scan_check"){
			if(window.val_protocolData_scan_check==undefined)dynamicJSLoad('val_protocolData_scan_check','generated/val_protocolData_scan_check.js');
			return new val_protocolData_scan_check();
		}
		if(name=="http://nrg.wustl.edu/xnat:studyProtocol_condition"){
			if(window.xnat_studyProtocol_condition==undefined)dynamicJSLoad('xnat_studyProtocol_condition','generated/xnat_studyProtocol_condition.js');
			return new xnat_studyProtocol_condition();
		}
		if(name=="http://nrg.wustl.edu/xnat:derivedData"){
			if(window.xnat_derivedData==undefined)dynamicJSLoad('xnat_derivedData','generated/xnat_derivedData.js');
			return new xnat_derivedData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ESSession"){
			if(window.xnat_esSessionData==undefined)dynamicJSLoad('xnat_esSessionData','generated/xnat_esSessionData.js');
			return new xnat_esSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:regionResource_label"){
			if(window.xnat_regionResource_label==undefined)dynamicJSLoad('xnat_regionResource_label','generated/xnat_regionResource_label.js');
			return new xnat_regionResource_label();
		}
		if(name=="http://nrg.wustl.edu/xnat:otherDicomSessionData"){
			if(window.xnat_otherDicomSessionData==undefined)dynamicJSLoad('xnat_otherDicomSessionData','generated/xnat_otherDicomSessionData.js');
			return new xnat_otherDicomSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:reconstructedImageData"){
			if(window.xnat_reconstructedImageData==undefined)dynamicJSLoad('xnat_reconstructedImageData','generated/xnat_reconstructedImageData.js');
			return new xnat_reconstructedImageData();
		}
		if(name=="xnat:srSessionData"){
			if(window.xnat_srSessionData==undefined)dynamicJSLoad('xnat_srSessionData','generated/xnat_srSessionData.js');
			return new xnat_srSessionData();
		}
		if(name=="xnat:usScanData"){
			if(window.xnat_usScanData==undefined)dynamicJSLoad('xnat_usScanData','generated/xnat_usScanData.js');
			return new xnat_usScanData();
		}
		if(name=="xnat:dicomSeries"){
			if(window.xnat_dicomSeries==undefined)dynamicJSLoad('xnat_dicomSeries','generated/xnat_dicomSeries.js');
			return new xnat_dicomSeries();
		}
		if(name=="xnat:mrSessionData"){
			if(window.xnat_mrSessionData==undefined)dynamicJSLoad('xnat_mrSessionData','generated/xnat_mrSessionData.js');
			return new xnat_mrSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ecgSessionData"){
			if(window.xnat_ecgSessionData==undefined)dynamicJSLoad('xnat_ecgSessionData','generated/xnat_ecgSessionData.js');
			return new xnat_ecgSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:ygtssData"){
			if(window.xnat_a_ygtssData==undefined)dynamicJSLoad('xnat_a_ygtssData','generated/xnat_a_ygtssData.js');
			return new xnat_a_ygtssData();
		}
		if(name=="xnat:statisticsData_additionalStatistics"){
			if(window.xnat_statisticsData_additionalStatistics==undefined)dynamicJSLoad('xnat_statisticsData_additionalStatistics','generated/xnat_statisticsData_additionalStatistics.js');
			return new xnat_statisticsData_additionalStatistics();
		}
		if(name=="xnat:imageResourceSeries"){
			if(window.xnat_imageResourceSeries==undefined)dynamicJSLoad('xnat_imageResourceSeries','generated/xnat_imageResourceSeries.js');
			return new xnat_imageResourceSeries();
		}
		if(name=="xnat:statisticsData_addField"){
			if(window.xnat_statisticsData_addField==undefined)dynamicJSLoad('xnat_statisticsData_addField','generated/xnat_statisticsData_addField.js');
			return new xnat_statisticsData_addField();
		}
		if(name=="http://nrg.wustl.edu/xnat:rfScanData"){
			if(window.xnat_rfScanData==undefined)dynamicJSLoad('xnat_rfScanData','generated/xnat_rfScanData.js');
			return new xnat_rfScanData();
		}
		if(name=="http://nrg.wustl.edu/security:News"){
			if(window.xdat_newsEntry==undefined)dynamicJSLoad('xdat_newsEntry','generated/xdat_newsEntry.js');
			return new xdat_newsEntry();
		}
		if(name=="http://nrg.wustl.edu/xnat:MRSScan"){
			if(window.xnat_mrsScanData==undefined)dynamicJSLoad('xnat_mrsScanData','generated/xnat_mrsScanData.js');
			return new xnat_mrsScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:volumetricRegion_subregion"){
			if(window.xnat_volumetricRegion_subregion==undefined)dynamicJSLoad('xnat_volumetricRegion_subregion','generated/xnat_volumetricRegion_subregion.js');
			return new xnat_volumetricRegion_subregion();
		}
		if(name=="http://nrg.wustl.edu/xnat:statisticsData_additionalStatistics"){
			if(window.xnat_statisticsData_additionalStatistics==undefined)dynamicJSLoad('xnat_statisticsData_additionalStatistics','generated/xnat_statisticsData_additionalStatistics.js');
			return new xnat_statisticsData_additionalStatistics();
		}
		if(name=="http://nrg.wustl.edu/xnat:imageScanData_share"){
			if(window.xnat_imageScanData_share==undefined)dynamicJSLoad('xnat_imageScanData_share','generated/xnat_imageScanData_share.js');
			return new xnat_imageScanData_share();
		}
		if(name=="xnat:XCVScan"){
			if(window.xnat_xcvScanData==undefined)dynamicJSLoad('xnat_xcvScanData','generated/xnat_xcvScanData.js');
			return new xnat_xcvScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:imageResource"){
			if(window.xnat_imageResource==undefined)dynamicJSLoad('xnat_imageResource','generated/xnat_imageResource.js');
			return new xnat_imageResource();
		}
		if(name=="xnat:abstractResource"){
			if(window.xnat_abstractResource==undefined)dynamicJSLoad('xnat_abstractResource','generated/xnat_abstractResource.js');
			return new xnat_abstractResource();
		}
		if(name=="http://nrg.wustl.edu/xnat:otherDicomScanData"){
			if(window.xnat_otherDicomScanData==undefined)dynamicJSLoad('xnat_otherDicomScanData','generated/xnat_otherDicomScanData.js');
			return new xnat_otherDicomScanData();
		}
		if(name=="xnat:resourceCatalog"){
			if(window.xnat_resourceCatalog==undefined)dynamicJSLoad('xnat_resourceCatalog','generated/xnat_resourceCatalog.js');
			return new xnat_resourceCatalog();
		}
		if(name=="xnat:rtSessionData"){
			if(window.xnat_rtSessionData==undefined)dynamicJSLoad('xnat_rtSessionData','generated/xnat_rtSessionData.js');
			return new xnat_rtSessionData();
		}
		if(name=="xnat:ctScanData"){
			if(window.xnat_ctScanData==undefined)dynamicJSLoad('xnat_ctScanData','generated/xnat_ctScanData.js');
			return new xnat_ctScanData();
		}
		if(name=="xnat:gmvScanData"){
			if(window.xnat_gmvScanData==undefined)dynamicJSLoad('xnat_gmvScanData','generated/xnat_gmvScanData.js');
			return new xnat_gmvScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:statisticsData_addField"){
			if(window.xnat_statisticsData_addField==undefined)dynamicJSLoad('xnat_statisticsData_addField','generated/xnat_statisticsData_addField.js');
			return new xnat_statisticsData_addField();
		}
		if(name=="xnat:MRSession"){
			if(window.xnat_mrSessionData==undefined)dynamicJSLoad('xnat_mrSessionData','generated/xnat_mrSessionData.js');
			return new xnat_mrSessionData();
		}
		if(name=="http://nrg.wustl.edu/catalog:entry_metaField"){
			if(window.cat_entry_metaField==undefined)dynamicJSLoad('cat_entry_metaField','generated/cat_entry_metaField.js');
			return new cat_entry_metaField();
		}
		if(name=="arc:project"){
			if(window.arc_project==undefined)dynamicJSLoad('arc_project','generated/arc_project.js');
			return new arc_project();
		}
		if(name=="xnat:dicomSeries_image"){
			if(window.xnat_dicomSeries_image==undefined)dynamicJSLoad('xnat_dicomSeries_image','generated/xnat_dicomSeries_image.js');
			return new xnat_dicomSeries_image();
		}
		if(name=="http://nrg.wustl.edu/pipe:PipelineRepository"){
			if(window.pipe_PipelineRepository==undefined)dynamicJSLoad('pipe_PipelineRepository','generated/pipe_PipelineRepository.js');
			return new pipe_PipelineRepository();
		}
		if(name=="http://nrg.wustl.edu/xnat:OPTSession"){
			if(window.xnat_optSessionData==undefined)dynamicJSLoad('xnat_optSessionData','generated/xnat_optSessionData.js');
			return new xnat_optSessionData();
		}
		if(name=="xnat:fieldDefinitionGroup_field"){
			if(window.xnat_fieldDefinitionGroup_field==undefined)dynamicJSLoad('xnat_fieldDefinitionGroup_field','generated/xnat_fieldDefinitionGroup_field.js');
			return new xnat_fieldDefinitionGroup_field();
		}
		if(name=="xnat:ECGScan"){
			if(window.xnat_ecgScanData==undefined)dynamicJSLoad('xnat_ecgScanData','generated/xnat_ecgScanData.js');
			return new xnat_ecgScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:abstractStatistics"){
			if(window.xnat_abstractStatistics==undefined)dynamicJSLoad('xnat_abstractStatistics','generated/xnat_abstractStatistics.js');
			return new xnat_abstractStatistics();
		}
		if(name=="xdat:user_groupID"){
			if(window.xdat_user_groupID==undefined)dynamicJSLoad('xdat_user_groupID','generated/xdat_user_groupID.js');
			return new xdat_user_groupID();
		}
		if(name=="xnat:optScanData"){
			if(window.xnat_optScanData==undefined)dynamicJSLoad('xnat_optScanData','generated/xnat_optScanData.js');
			return new xnat_optScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:eegSessionData"){
			if(window.xnat_eegSessionData==undefined)dynamicJSLoad('xnat_eegSessionData','generated/xnat_eegSessionData.js');
			return new xnat_eegSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:smSessionData"){
			if(window.xnat_smSessionData==undefined)dynamicJSLoad('xnat_smSessionData','generated/xnat_smSessionData.js');
			return new xnat_smSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:projectParticipant"){
			if(window.xnat_projectParticipant==undefined)dynamicJSLoad('xnat_projectParticipant','generated/xnat_projectParticipant.js');
			return new xnat_projectParticipant();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:sideEffectsPittsburghData"){
			if(window.xnat_a_sideEffectsPittsburghData==undefined)dynamicJSLoad('xnat_a_sideEffectsPittsburghData','generated/xnat_a_sideEffectsPittsburghData.js');
			return new xnat_a_sideEffectsPittsburghData();
		}
		if(name=="xnat:experimentData_field"){
			if(window.xnat_experimentData_field==undefined)dynamicJSLoad('xnat_experimentData_field','generated/xnat_experimentData_field.js');
			return new xnat_experimentData_field();
		}
		if(name=="xnat:CTScan"){
			if(window.xnat_ctScanData==undefined)dynamicJSLoad('xnat_ctScanData','generated/xnat_ctScanData.js');
			return new xnat_ctScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:gmSessionData"){
			if(window.xnat_gmSessionData==undefined)dynamicJSLoad('xnat_gmSessionData','generated/xnat_gmSessionData.js');
			return new xnat_gmSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:petScanData"){
			if(window.xnat_petScanData==undefined)dynamicJSLoad('xnat_petScanData','generated/xnat_petScanData.js');
			return new xnat_petScanData();
		}
		if(name=="xnat:esScanData"){
			if(window.xnat_esScanData==undefined)dynamicJSLoad('xnat_esScanData','generated/xnat_esScanData.js');
			return new xnat_esScanData();
		}
		if(name=="xnat:subjectVariablesData_variable"){
			if(window.xnat_subjectVariablesData_variable==undefined)dynamicJSLoad('xnat_subjectVariablesData_variable','generated/xnat_subjectVariablesData_variable.js');
			return new xnat_subjectVariablesData_variable();
		}
		if(name=="http://nrg.wustl.edu/xnat:MEGSession"){
			if(window.xnat_megSessionData==undefined)dynamicJSLoad('xnat_megSessionData','generated/xnat_megSessionData.js');
			return new xnat_megSessionData();
		}
		if(name=="xnat:usSessionData"){
			if(window.xnat_usSessionData==undefined)dynamicJSLoad('xnat_usSessionData','generated/xnat_usSessionData.js');
			return new xnat_usSessionData();
		}
		if(name=="http://nrg.wustl.edu/security:criteria_set"){
			if(window.xdat_criteria_set==undefined)dynamicJSLoad('xdat_criteria_set','generated/xdat_criteria_set.js');
			return new xdat_criteria_set();
		}
		if(name=="xnat:qcAssessmentData_scan_slice"){
			if(window.xnat_qcAssessmentData_scan_slice==undefined)dynamicJSLoad('xnat_qcAssessmentData_scan_slice','generated/xnat_qcAssessmentData_scan_slice.js');
			return new xnat_qcAssessmentData_scan_slice();
		}
		if(name=="xdat:element_action_type"){
			if(window.xdat_element_action_type==undefined)dynamicJSLoad('xdat_element_action_type','generated/xdat_element_action_type.js');
			return new xdat_element_action_type();
		}
		if(name=="http://nrg.wustl.edu/xnat:petmrSessionData"){
			if(window.xnat_petmrSessionData==undefined)dynamicJSLoad('xnat_petmrSessionData','generated/xnat_petmrSessionData.js');
			return new xnat_petmrSessionData();
		}
		if(name=="xnat:dxSessionData"){
			if(window.xnat_dxSessionData==undefined)dynamicJSLoad('xnat_dxSessionData','generated/xnat_dxSessionData.js');
			return new xnat_dxSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:imageSessionData"){
			if(window.xnat_imageSessionData==undefined)dynamicJSLoad('xnat_imageSessionData','generated/xnat_imageSessionData.js');
			return new xnat_imageSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ESVScan"){
			if(window.xnat_esvScanData==undefined)dynamicJSLoad('xnat_esvScanData','generated/xnat_esvScanData.js');
			return new xnat_esvScanData();
		}
		if(name=="xnat:derivedData"){
			if(window.xnat_derivedData==undefined)dynamicJSLoad('xnat_derivedData','generated/xnat_derivedData.js');
			return new xnat_derivedData();
		}
		if(name=="xdat:field_mapping_set"){
			if(window.xdat_field_mapping_set==undefined)dynamicJSLoad('xdat_field_mapping_set','generated/xdat_field_mapping_set.js');
			return new xdat_field_mapping_set();
		}
		if(name=="xdat:element_access_secure_ip"){
			if(window.xdat_element_access_secure_ip==undefined)dynamicJSLoad('xdat_element_access_secure_ip','generated/xdat_element_access_secure_ip.js');
			return new xdat_element_access_secure_ip();
		}
		if(name=="xnat:ctScanData_focalSpot"){
			if(window.xnat_ctScanData_focalSpot==undefined)dynamicJSLoad('xnat_ctScanData_focalSpot','generated/xnat_ctScanData_focalSpot.js');
			return new xnat_ctScanData_focalSpot();
		}
		if(name=="val:protocolData_scan_check_condition"){
			if(window.val_protocolData_scan_check_condition==undefined)dynamicJSLoad('val_protocolData_scan_check_condition','generated/val_protocolData_scan_check_condition.js');
			return new val_protocolData_scan_check_condition();
		}
		if(name=="http://nrg.wustl.edu/xnat:nmSessionData"){
			if(window.xnat_nmSessionData==undefined)dynamicJSLoad('xnat_nmSessionData','generated/xnat_nmSessionData.js');
			return new xnat_nmSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:SRSession"){
			if(window.xnat_srSessionData==undefined)dynamicJSLoad('xnat_srSessionData','generated/xnat_srSessionData.js');
			return new xnat_srSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:projectData_field"){
			if(window.xnat_projectData_field==undefined)dynamicJSLoad('xnat_projectData_field','generated/xnat_projectData_field.js');
			return new xnat_projectData_field();
		}
		if(name=="wrk:xnatExecutionEnvironment_parameter"){
			if(window.wrk_xnatExecutionEnvironment_parameter==undefined)dynamicJSLoad('wrk_xnatExecutionEnvironment_parameter','generated/wrk_xnatExecutionEnvironment_parameter.js');
			return new wrk_xnatExecutionEnvironment_parameter();
		}
		if(name=="http://nrg.wustl.edu/xnat:imageResourceSeries"){
			if(window.xnat_imageResourceSeries==undefined)dynamicJSLoad('xnat_imageResourceSeries','generated/xnat_imageResourceSeries.js');
			return new xnat_imageResourceSeries();
		}
		if(name=="xnat:contrastBolus"){
			if(window.xnat_contrastBolus==undefined)dynamicJSLoad('xnat_contrastBolus','generated/xnat_contrastBolus.js');
			return new xnat_contrastBolus();
		}
		if(name=="xnat:regionResource_label"){
			if(window.xnat_regionResource_label==undefined)dynamicJSLoad('xnat_regionResource_label','generated/xnat_regionResource_label.js');
			return new xnat_regionResource_label();
		}
		if(name=="http://nrg.wustl.edu/security:Search"){
			if(window.xdat_Search==undefined)dynamicJSLoad('xdat_Search','generated/xdat_Search.js');
			return new xdat_Search();
		}
		if(name=="xnat:dx3DCraniofacialSessionData"){
			if(window.xnat_dx3DCraniofacialSessionData==undefined)dynamicJSLoad('xnat_dx3DCraniofacialSessionData','generated/xnat_dx3DCraniofacialSessionData.js');
			return new xnat_dx3DCraniofacialSessionData();
		}
		if(name=="xdat:primary_security_field"){
			if(window.xdat_primary_security_field==undefined)dynamicJSLoad('xdat_primary_security_field','generated/xdat_primary_security_field.js');
			return new xdat_primary_security_field();
		}
		if(name=="http://nrg.wustl.edu/xnat:EPSSession"){
			if(window.xnat_epsSessionData==undefined)dynamicJSLoad('xnat_epsSessionData','generated/xnat_epsSessionData.js');
			return new xnat_epsSessionData();
		}
		if(name=="xnat_a:YGTSS"){
			if(window.xnat_a_ygtssData==undefined)dynamicJSLoad('xnat_a_ygtssData','generated/xnat_a_ygtssData.js');
			return new xnat_a_ygtssData();
		}
		if(name=="xnat:genericData"){
			if(window.xnat_genericData==undefined)dynamicJSLoad('xnat_genericData','generated/xnat_genericData.js');
			return new xnat_genericData();
		}
		if(name=="wrk:xnatExecutionEnvironment"){
			if(window.wrk_xnatExecutionEnvironment==undefined)dynamicJSLoad('wrk_xnatExecutionEnvironment','generated/wrk_xnatExecutionEnvironment.js');
			return new wrk_xnatExecutionEnvironment();
		}
		if(name=="http://nrg.wustl.edu/xnat:XAScan"){
			if(window.xnat_xaScanData==undefined)dynamicJSLoad('xnat_xaScanData','generated/xnat_xaScanData.js');
			return new xnat_xaScanData();
		}
		if(name=="prov:processStep_library"){
			if(window.prov_processStep_library==undefined)dynamicJSLoad('prov_processStep_library','generated/prov_processStep_library.js');
			return new prov_processStep_library();
		}
		if(name=="xnat_a:updrs3Data"){
			if(window.xnat_a_updrs3Data==undefined)dynamicJSLoad('xnat_a_updrs3Data','generated/xnat_a_updrs3Data.js');
			return new xnat_a_updrs3Data();
		}
		if(name=="http://nrg.wustl.edu/xnat:dxScanData"){
			if(window.xnat_dxScanData==undefined)dynamicJSLoad('xnat_dxScanData','generated/xnat_dxScanData.js');
			return new xnat_dxScanData();
		}
		if(name=="xnat:SMScan"){
			if(window.xnat_smScanData==undefined)dynamicJSLoad('xnat_smScanData','generated/xnat_smScanData.js');
			return new xnat_smScanData();
		}
		if(name=="xdat:UserGroup"){
			if(window.xdat_userGroup==undefined)dynamicJSLoad('xdat_userGroup','generated/xdat_userGroup.js');
			return new xdat_userGroup();
		}
		if(name=="xnat:IOSession"){
			if(window.xnat_ioSessionData==undefined)dynamicJSLoad('xnat_ioSessionData','generated/xnat_ioSessionData.js');
			return new xnat_ioSessionData();
		}
		if(name=="xnat:studyProtocol_session"){
			if(window.xnat_studyProtocol_session==undefined)dynamicJSLoad('xnat_studyProtocol_session','generated/xnat_studyProtocol_session.js');
			return new xnat_studyProtocol_session();
		}
		if(name=="xnat:pVisitData"){
			if(window.xnat_pVisitData==undefined)dynamicJSLoad('xnat_pVisitData','generated/xnat_pVisitData.js');
			return new xnat_pVisitData();
		}
		if(name=="xnat:epsScanData"){
			if(window.xnat_epsScanData==undefined)dynamicJSLoad('xnat_epsScanData','generated/xnat_epsScanData.js');
			return new xnat_epsScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:RTSession"){
			if(window.xnat_rtSessionData==undefined)dynamicJSLoad('xnat_rtSessionData','generated/xnat_rtSessionData.js');
			return new xnat_rtSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:GMVScan"){
			if(window.xnat_gmvScanData==undefined)dynamicJSLoad('xnat_gmvScanData','generated/xnat_gmvScanData.js');
			return new xnat_gmvScanData();
		}
		if(name=="http://nrg.wustl.edu/workflow:xnatExecutionEnvironment_parameter"){
			if(window.wrk_xnatExecutionEnvironment_parameter==undefined)dynamicJSLoad('wrk_xnatExecutionEnvironment_parameter','generated/wrk_xnatExecutionEnvironment_parameter.js');
			return new wrk_xnatExecutionEnvironment_parameter();
		}
		if(name=="http://nrg.wustl.edu/xnat:SMSession"){
			if(window.xnat_smSessionData==undefined)dynamicJSLoad('xnat_smSessionData','generated/xnat_smSessionData.js');
			return new xnat_smSessionData();
		}
		if(name=="xnat:rtImageScanData"){
			if(window.xnat_rtImageScanData==undefined)dynamicJSLoad('xnat_rtImageScanData','generated/xnat_rtImageScanData.js');
			return new xnat_rtImageScanData();
		}
		if(name=="val:protocolData_condition"){
			if(window.val_protocolData_condition==undefined)dynamicJSLoad('val_protocolData_condition','generated/val_protocolData_condition.js');
			return new val_protocolData_condition();
		}
		if(name=="xnat:petScanData_frame"){
			if(window.xnat_petScanData_frame==undefined)dynamicJSLoad('xnat_petScanData_frame','generated/xnat_petScanData_frame.js');
			return new xnat_petScanData_frame();
		}
		if(name=="xnat:abstractDemographicData"){
			if(window.xnat_abstractDemographicData==undefined)dynamicJSLoad('xnat_abstractDemographicData','generated/xnat_abstractDemographicData.js');
			return new xnat_abstractDemographicData();
		}
		if(name=="http://nrg.wustl.edu/xnat:QCManualAssessment"){
			if(window.xnat_qcManualAssessorData==undefined)dynamicJSLoad('xnat_qcManualAssessorData','generated/xnat_qcManualAssessorData.js');
			return new xnat_qcManualAssessorData();
		}
		if(name=="http://nrg.wustl.edu/arc:ArchiveSpecification_notification_type"){
			if(window.arc_ArchiveSpecification_notification_type==undefined)dynamicJSLoad('arc_ArchiveSpecification_notification_type','generated/arc_ArchiveSpecification_notification_type.js');
			return new arc_ArchiveSpecification_notification_type();
		}
		if(name=="http://nrg.wustl.edu/security:search_field"){
			if(window.xdat_search_field==undefined)dynamicJSLoad('xdat_search_field','generated/xdat_search_field.js');
			return new xdat_search_field();
		}
		if(name=="xnat:DXSession"){
			if(window.xnat_dxSessionData==undefined)dynamicJSLoad('xnat_dxSessionData','generated/xnat_dxSessionData.js');
			return new xnat_dxSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:USSession"){
			if(window.xnat_usSessionData==undefined)dynamicJSLoad('xnat_usSessionData','generated/xnat_usSessionData.js');
			return new xnat_usSessionData();
		}
		if(name=="cat:dcmEntry"){
			if(window.cat_dcmEntry==undefined)dynamicJSLoad('cat_dcmEntry','generated/cat_dcmEntry.js');
			return new cat_dcmEntry();
		}
		if(name=="http://nrg.wustl.edu/xnat:esSessionData"){
			if(window.xnat_esSessionData==undefined)dynamicJSLoad('xnat_esSessionData','generated/xnat_esSessionData.js');
			return new xnat_esSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:megScanData"){
			if(window.xnat_megScanData==undefined)dynamicJSLoad('xnat_megScanData','generated/xnat_megScanData.js');
			return new xnat_megScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:abstractDemographicData"){
			if(window.xnat_abstractDemographicData==undefined)dynamicJSLoad('xnat_abstractDemographicData','generated/xnat_abstractDemographicData.js');
			return new xnat_abstractDemographicData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ioSessionData"){
			if(window.xnat_ioSessionData==undefined)dynamicJSLoad('xnat_ioSessionData','generated/xnat_ioSessionData.js');
			return new xnat_ioSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:nmScanData"){
			if(window.xnat_nmScanData==undefined)dynamicJSLoad('xnat_nmScanData','generated/xnat_nmScanData.js');
			return new xnat_nmScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:studyProtocol_group"){
			if(window.xnat_studyProtocol_group==undefined)dynamicJSLoad('xnat_studyProtocol_group','generated/xnat_studyProtocol_group.js');
			return new xnat_studyProtocol_group();
		}
		if(name=="xnat:petQcScanData"){
			if(window.xnat_petQcScanData==undefined)dynamicJSLoad('xnat_petQcScanData','generated/xnat_petQcScanData.js');
			return new xnat_petQcScanData();
		}
		if(name=="xnat:PETSession"){
			if(window.xnat_petSessionData==undefined)dynamicJSLoad('xnat_petSessionData','generated/xnat_petSessionData.js');
			return new xnat_petSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:investigatorData"){
			if(window.xnat_investigatorData==undefined)dynamicJSLoad('xnat_investigatorData','generated/xnat_investigatorData.js');
			return new xnat_investigatorData();
		}
		if(name=="http://nrg.wustl.edu/xnat:subjectVariablesData"){
			if(window.xnat_subjectVariablesData==undefined)dynamicJSLoad('xnat_subjectVariablesData','generated/xnat_subjectVariablesData.js');
			return new xnat_subjectVariablesData();
		}
		if(name=="xnat:qcAssessmentData_scan"){
			if(window.xnat_qcAssessmentData_scan==undefined)dynamicJSLoad('xnat_qcAssessmentData_scan','generated/xnat_qcAssessmentData_scan.js');
			return new xnat_qcAssessmentData_scan();
		}
		if(name=="http://nrg.wustl.edu/xnat:srSessionData"){
			if(window.xnat_srSessionData==undefined)dynamicJSLoad('xnat_srSessionData','generated/xnat_srSessionData.js');
			return new xnat_srSessionData();
		}
		if(name=="xnat:Subject"){
			if(window.xnat_subjectData==undefined)dynamicJSLoad('xnat_subjectData','generated/xnat_subjectData.js');
			return new xnat_subjectData();
		}
		if(name=="pipe:pipelineDetails_element"){
			if(window.pipe_pipelineDetails_element==undefined)dynamicJSLoad('pipe_pipelineDetails_element','generated/pipe_pipelineDetails_element.js');
			return new pipe_pipelineDetails_element();
		}
		if(name=="http://nrg.wustl.edu/xnat:mrSessionData"){
			if(window.xnat_mrSessionData==undefined)dynamicJSLoad('xnat_mrSessionData','generated/xnat_mrSessionData.js');
			return new xnat_mrSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:opSessionData"){
			if(window.xnat_opSessionData==undefined)dynamicJSLoad('xnat_opSessionData','generated/xnat_opSessionData.js');
			return new xnat_opSessionData();
		}
		if(name=="xnat:PETMRSession"){
			if(window.xnat_petmrSessionData==undefined)dynamicJSLoad('xnat_petmrSessionData','generated/xnat_petmrSessionData.js');
			return new xnat_petmrSessionData();
		}
		if(name=="xnat:OtherDicomScan"){
			if(window.xnat_otherDicomScanData==undefined)dynamicJSLoad('xnat_otherDicomScanData','generated/xnat_otherDicomScanData.js');
			return new xnat_otherDicomScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:SCID"){
			if(window.xnat_a_scidResearchData==undefined)dynamicJSLoad('xnat_a_scidResearchData','generated/xnat_a_scidResearchData.js');
			return new xnat_a_scidResearchData();
		}
		if(name=="http://nrg.wustl.edu/security:stored_search_groupID"){
			if(window.xdat_stored_search_groupID==undefined)dynamicJSLoad('xdat_stored_search_groupID','generated/xdat_stored_search_groupID.js');
			return new xdat_stored_search_groupID();
		}
		if(name=="http://nrg.wustl.edu/xnat:DX3DCraniofacialScan"){
			if(window.xnat_dx3DCraniofacialScanData==undefined)dynamicJSLoad('xnat_dx3DCraniofacialScanData','generated/xnat_dx3DCraniofacialScanData.js');
			return new xnat_dx3DCraniofacialScanData();
		}
		if(name=="http://nrg.wustl.edu/val:protocolData_scan_check_comment"){
			if(window.val_protocolData_scan_check_comment==undefined)dynamicJSLoad('val_protocolData_scan_check_comment','generated/val_protocolData_scan_check_comment.js');
			return new val_protocolData_scan_check_comment();
		}
		if(name=="xnat:xcScanData"){
			if(window.xnat_xcScanData==undefined)dynamicJSLoad('xnat_xcScanData','generated/xnat_xcScanData.js');
			return new xnat_xcScanData();
		}
		if(name=="xnat:opScanData"){
			if(window.xnat_opScanData==undefined)dynamicJSLoad('xnat_opScanData','generated/xnat_opScanData.js');
			return new xnat_opScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:CRScan"){
			if(window.xnat_crScanData==undefined)dynamicJSLoad('xnat_crScanData','generated/xnat_crScanData.js');
			return new xnat_crScanData();
		}
		if(name=="cat:Catalog"){
			if(window.cat_catalog==undefined)dynamicJSLoad('cat_catalog','generated/cat_catalog.js');
			return new cat_catalog();
		}
		if(name=="http://nrg.wustl.edu/pipe:pipelineDetails_element"){
			if(window.pipe_pipelineDetails_element==undefined)dynamicJSLoad('pipe_pipelineDetails_element','generated/pipe_pipelineDetails_element.js');
			return new pipe_pipelineDetails_element();
		}
		if(name=="xnat:scScanData"){
			if(window.xnat_scScanData==undefined)dynamicJSLoad('xnat_scScanData','generated/xnat_scScanData.js');
			return new xnat_scScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:addField"){
			if(window.xnat_addField==undefined)dynamicJSLoad('xnat_addField','generated/xnat_addField.js');
			return new xnat_addField();
		}
		if(name=="http://nrg.wustl.edu/xnat:ReconstructedImage"){
			if(window.xnat_reconstructedImageData==undefined)dynamicJSLoad('xnat_reconstructedImageData','generated/xnat_reconstructedImageData.js');
			return new xnat_reconstructedImageData();
		}
		if(name=="xnat:XCSession"){
			if(window.xnat_xcSessionData==undefined)dynamicJSLoad('xnat_xcSessionData','generated/xnat_xcSessionData.js');
			return new xnat_xcSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:computationData"){
			if(window.xnat_computationData==undefined)dynamicJSLoad('xnat_computationData','generated/xnat_computationData.js');
			return new xnat_computationData();
		}
		if(name=="xdat:access_log"){
			if(window.xdat_access_log==undefined)dynamicJSLoad('xdat_access_log','generated/xdat_access_log.js');
			return new xdat_access_log();
		}
		if(name=="val:protocolData_scan_check"){
			if(window.val_protocolData_scan_check==undefined)dynamicJSLoad('val_protocolData_scan_check','generated/val_protocolData_scan_check.js');
			return new val_protocolData_scan_check();
		}
		if(name=="http://nrg.wustl.edu/scr:screeningAssessment"){
			if(window.scr_screeningAssessment==undefined)dynamicJSLoad('scr_screeningAssessment','generated/scr_screeningAssessment.js');
			return new scr_screeningAssessment();
		}
		if(name=="http://nrg.wustl.edu/xnat:MRScan"){
			if(window.xnat_mrScanData==undefined)dynamicJSLoad('xnat_mrScanData','generated/xnat_mrScanData.js');
			return new xnat_mrScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ECGSession"){
			if(window.xnat_ecgSessionData==undefined)dynamicJSLoad('xnat_ecgSessionData','generated/xnat_ecgSessionData.js');
			return new xnat_ecgSessionData();
		}
		if(name=="xnat:subjectData_field"){
			if(window.xnat_subjectData_field==undefined)dynamicJSLoad('xnat_subjectData_field','generated/xnat_subjectData_field.js');
			return new xnat_subjectData_field();
		}
		if(name=="xnat:rfScanData"){
			if(window.xnat_rfScanData==undefined)dynamicJSLoad('xnat_rfScanData','generated/xnat_rfScanData.js');
			return new xnat_rfScanData();
		}
		if(name=="xnat:EEGScan"){
			if(window.xnat_eegScanData==undefined)dynamicJSLoad('xnat_eegScanData','generated/xnat_eegScanData.js');
			return new xnat_eegScanData();
		}
		if(name=="wrk:abstractExecutionEnvironment"){
			if(window.wrk_abstractExecutionEnvironment==undefined)dynamicJSLoad('wrk_abstractExecutionEnvironment','generated/wrk_abstractExecutionEnvironment.js');
			return new wrk_abstractExecutionEnvironment();
		}
		if(name=="http://nrg.wustl.edu/xnat:GMVSession"){
			if(window.xnat_gmvSessionData==undefined)dynamicJSLoad('xnat_gmvSessionData','generated/xnat_gmvSessionData.js');
			return new xnat_gmvSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:OtherDicomSession"){
			if(window.xnat_otherDicomSessionData==undefined)dynamicJSLoad('xnat_otherDicomSessionData','generated/xnat_otherDicomSessionData.js');
			return new xnat_otherDicomSessionData();
		}
		if(name=="xnat:CTSession"){
			if(window.xnat_ctSessionData==undefined)dynamicJSLoad('xnat_ctSessionData','generated/xnat_ctSessionData.js');
			return new xnat_ctSessionData();
		}
		if(name=="xnat:GMSession"){
			if(window.xnat_gmSessionData==undefined)dynamicJSLoad('xnat_gmSessionData','generated/xnat_gmSessionData.js');
			return new xnat_gmSessionData();
		}
		if(name=="xnat:reconstructedImageData"){
			if(window.xnat_reconstructedImageData==undefined)dynamicJSLoad('xnat_reconstructedImageData','generated/xnat_reconstructedImageData.js');
			return new xnat_reconstructedImageData();
		}
		if(name=="xdat:stored_search"){
			if(window.xdat_stored_search==undefined)dynamicJSLoad('xdat_stored_search','generated/xdat_stored_search.js');
			return new xdat_stored_search();
		}
		if(name=="http://nrg.wustl.edu/xnat:crSessionData"){
			if(window.xnat_crSessionData==undefined)dynamicJSLoad('xnat_crSessionData','generated/xnat_crSessionData.js');
			return new xnat_crSessionData();
		}
		if(name=="xnat:PETScan"){
			if(window.xnat_petScanData==undefined)dynamicJSLoad('xnat_petScanData','generated/xnat_petScanData.js');
			return new xnat_petScanData();
		}
		if(name=="http://nrg.wustl.edu/security:user_login"){
			if(window.xdat_user_login==undefined)dynamicJSLoad('xdat_user_login','generated/xdat_user_login.js');
			return new xdat_user_login();
		}
		if(name=="xnat:SegScan"){
			if(window.xnat_segScanData==undefined)dynamicJSLoad('xnat_segScanData','generated/xnat_segScanData.js');
			return new xnat_segScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:MRSession"){
			if(window.xnat_mrSessionData==undefined)dynamicJSLoad('xnat_mrSessionData','generated/xnat_mrSessionData.js');
			return new xnat_mrSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:usSessionData"){
			if(window.xnat_usSessionData==undefined)dynamicJSLoad('xnat_usSessionData','generated/xnat_usSessionData.js');
			return new xnat_usSessionData();
		}
		if(name=="xnat:xa3DScanData"){
			if(window.xnat_xa3DScanData==undefined)dynamicJSLoad('xnat_xa3DScanData','generated/xnat_xa3DScanData.js');
			return new xnat_xa3DScanData();
		}
		if(name=="http://nrg.wustl.edu/val:additionalVal"){
			if(window.val_additionalVal==undefined)dynamicJSLoad('val_additionalVal','generated/val_additionalVal.js');
			return new val_additionalVal();
		}
		if(name=="xnat:megScanData"){
			if(window.xnat_megScanData==undefined)dynamicJSLoad('xnat_megScanData','generated/xnat_megScanData.js');
			return new xnat_megScanData();
		}
		if(name=="http://www.nbirn.net/prov:processStep_library"){
			if(window.prov_processStep_library==undefined)dynamicJSLoad('prov_processStep_library','generated/prov_processStep_library.js');
			return new prov_processStep_library();
		}
		if(name=="http://nrg.wustl.edu/catalog:catalog"){
			if(window.cat_catalog==undefined)dynamicJSLoad('cat_catalog','generated/cat_catalog.js');
			return new cat_catalog();
		}
		if(name=="xnat:XAScan"){
			if(window.xnat_xaScanData==undefined)dynamicJSLoad('xnat_xaScanData','generated/xnat_xaScanData.js');
			return new xnat_xaScanData();
		}
		if(name=="arc:project_pipeline"){
			if(window.arc_project_pipeline==undefined)dynamicJSLoad('arc_project_pipeline','generated/arc_project_pipeline.js');
			return new arc_project_pipeline();
		}
		if(name=="xnat:OPTSession"){
			if(window.xnat_optSessionData==undefined)dynamicJSLoad('xnat_optSessionData','generated/xnat_optSessionData.js');
			return new xnat_optSessionData();
		}
		if(name=="xnat:volumetricRegion"){
			if(window.xnat_volumetricRegion==undefined)dynamicJSLoad('xnat_volumetricRegion','generated/xnat_volumetricRegion.js');
			return new xnat_volumetricRegion();
		}
		if(name=="scr:ScreeningAssessment"){
			if(window.scr_screeningAssessment==undefined)dynamicJSLoad('scr_screeningAssessment','generated/scr_screeningAssessment.js');
			return new scr_screeningAssessment();
		}
		if(name=="xnat:subjectVariablesData"){
			if(window.xnat_subjectVariablesData==undefined)dynamicJSLoad('xnat_subjectVariablesData','generated/xnat_subjectVariablesData.js');
			return new xnat_subjectVariablesData();
		}
		if(name=="http://nrg.wustl.edu/xnat:petScanData_frame"){
			if(window.xnat_petScanData_frame==undefined)dynamicJSLoad('xnat_petScanData_frame','generated/xnat_petScanData_frame.js');
			return new xnat_petScanData_frame();
		}
		if(name=="xnat:Investigator"){
			if(window.xnat_investigatorData==undefined)dynamicJSLoad('xnat_investigatorData','generated/xnat_investigatorData.js');
			return new xnat_investigatorData();
		}
		if(name=="http://nrg.wustl.edu/xnat:otherQcScanData"){
			if(window.xnat_otherQcScanData==undefined)dynamicJSLoad('xnat_otherQcScanData','generated/xnat_otherQcScanData.js');
			return new xnat_otherQcScanData();
		}
		if(name=="xnat_a:scidResearchData"){
			if(window.xnat_a_scidResearchData==undefined)dynamicJSLoad('xnat_a_scidResearchData','generated/xnat_a_scidResearchData.js');
			return new xnat_a_scidResearchData();
		}
		if(name=="http://nrg.wustl.edu/xnat:OtherDicomScan"){
			if(window.xnat_otherDicomScanData==undefined)dynamicJSLoad('xnat_otherDicomScanData','generated/xnat_otherDicomScanData.js');
			return new xnat_otherDicomScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:USScan"){
			if(window.xnat_usScanData==undefined)dynamicJSLoad('xnat_usScanData','generated/xnat_usScanData.js');
			return new xnat_usScanData();
		}
		if(name=="val:ProtocolVal"){
			if(window.val_protocolData==undefined)dynamicJSLoad('val_protocolData','generated/val_protocolData.js');
			return new val_protocolData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ctSessionData"){
			if(window.xnat_ctSessionData==undefined)dynamicJSLoad('xnat_ctSessionData','generated/xnat_ctSessionData.js');
			return new xnat_ctSessionData();
		}
		if(name=="xdat:role_type"){
			if(window.xdat_role_type==undefined)dynamicJSLoad('xdat_role_type','generated/xdat_role_type.js');
			return new xdat_role_type();
		}
		if(name=="http://nrg.wustl.edu/catalog:Entry"){
			if(window.cat_entry==undefined)dynamicJSLoad('cat_entry','generated/cat_entry.js');
			return new cat_entry();
		}
		if(name=="xnat:MGSession"){
			if(window.xnat_mgSessionData==undefined)dynamicJSLoad('xnat_mgSessionData','generated/xnat_mgSessionData.js');
			return new xnat_mgSessionData();
		}
		if(name=="xnat:MEGSession"){
			if(window.xnat_megSessionData==undefined)dynamicJSLoad('xnat_megSessionData','generated/xnat_megSessionData.js');
			return new xnat_megSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:MGScan"){
			if(window.xnat_mgScanData==undefined)dynamicJSLoad('xnat_mgScanData','generated/xnat_mgScanData.js');
			return new xnat_mgScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:XCVScan"){
			if(window.xnat_xcvScanData==undefined)dynamicJSLoad('xnat_xcvScanData','generated/xnat_xcvScanData.js');
			return new xnat_xcvScanData();
		}
		if(name=="xnat:DX3DCraniofacialScan"){
			if(window.xnat_dx3DCraniofacialScanData==undefined)dynamicJSLoad('xnat_dx3DCraniofacialScanData','generated/xnat_dx3DCraniofacialScanData.js');
			return new xnat_dx3DCraniofacialScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:dx3DCraniofacialScanData"){
			if(window.xnat_dx3DCraniofacialScanData==undefined)dynamicJSLoad('xnat_dx3DCraniofacialScanData','generated/xnat_dx3DCraniofacialScanData.js');
			return new xnat_dx3DCraniofacialScanData();
		}
		if(name=="xnat:Project"){
			if(window.xnat_projectData==undefined)dynamicJSLoad('xnat_projectData','generated/xnat_projectData.js');
			return new xnat_projectData();
		}
		if(name=="http://nrg.wustl.edu/xnat:smScanData"){
			if(window.xnat_smScanData==undefined)dynamicJSLoad('xnat_smScanData','generated/xnat_smScanData.js');
			return new xnat_smScanData();
		}
		if(name=="xnat:fieldDefinitionGroup_field_possibleValue"){
			if(window.xnat_fieldDefinitionGroup_field_possibleValue==undefined)dynamicJSLoad('xnat_fieldDefinitionGroup_field_possibleValue','generated/xnat_fieldDefinitionGroup_field_possibleValue.js');
			return new xnat_fieldDefinitionGroup_field_possibleValue();
		}
		if(name=="xnat:petScanData"){
			if(window.xnat_petScanData==undefined)dynamicJSLoad('xnat_petScanData','generated/xnat_petScanData.js');
			return new xnat_petScanData();
		}
		if(name=="xnat:petQcScanData_processingError"){
			if(window.xnat_petQcScanData_processingError==undefined)dynamicJSLoad('xnat_petQcScanData_processingError','generated/xnat_petQcScanData_processingError.js');
			return new xnat_petQcScanData_processingError();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:YGTSS"){
			if(window.xnat_a_ygtssData==undefined)dynamicJSLoad('xnat_a_ygtssData','generated/xnat_a_ygtssData.js');
			return new xnat_a_ygtssData();
		}
		if(name=="http://nrg.wustl.edu/security:element_action_type"){
			if(window.xdat_element_action_type==undefined)dynamicJSLoad('xdat_element_action_type','generated/xdat_element_action_type.js');
			return new xdat_element_action_type();
		}
		if(name=="xnat:EPSSession"){
			if(window.xnat_epsSessionData==undefined)dynamicJSLoad('xnat_epsSessionData','generated/xnat_epsSessionData.js');
			return new xnat_epsSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:IOSession"){
			if(window.xnat_ioSessionData==undefined)dynamicJSLoad('xnat_ioSessionData','generated/xnat_ioSessionData.js');
			return new xnat_ioSessionData();
		}
		if(name=="xnat:dxScanData"){
			if(window.xnat_dxScanData==undefined)dynamicJSLoad('xnat_dxScanData','generated/xnat_dxScanData.js');
			return new xnat_dxScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:ECGScan"){
			if(window.xnat_ecgScanData==undefined)dynamicJSLoad('xnat_ecgScanData','generated/xnat_ecgScanData.js');
			return new xnat_ecgScanData();
		}
		if(name=="xnat:MEGScan"){
			if(window.xnat_megScanData==undefined)dynamicJSLoad('xnat_megScanData','generated/xnat_megScanData.js');
			return new xnat_megScanData();
		}
		if(name=="xnat:imageScanData"){
			if(window.xnat_imageScanData==undefined)dynamicJSLoad('xnat_imageScanData','generated/xnat_imageScanData.js');
			return new xnat_imageScanData();
		}
		if(name=="xnat:SubjectVariables"){
			if(window.xnat_subjectVariablesData==undefined)dynamicJSLoad('xnat_subjectVariablesData','generated/xnat_subjectVariablesData.js');
			return new xnat_subjectVariablesData();
		}
		if(name=="http://nrg.wustl.edu/security:UserGroup"){
			if(window.xdat_userGroup==undefined)dynamicJSLoad('xdat_userGroup','generated/xdat_userGroup.js');
			return new xdat_userGroup();
		}
		if(name=="xnat:abstractProtocol"){
			if(window.xnat_abstractProtocol==undefined)dynamicJSLoad('xnat_abstractProtocol','generated/xnat_abstractProtocol.js');
			return new xnat_abstractProtocol();
		}
		if(name=="xdat:infoEntry"){
			if(window.xdat_infoEntry==undefined)dynamicJSLoad('xdat_infoEntry','generated/xdat_infoEntry.js');
			return new xdat_infoEntry();
		}
		if(name=="pipe:PipelineRepository"){
			if(window.pipe_PipelineRepository==undefined)dynamicJSLoad('pipe_PipelineRepository','generated/pipe_PipelineRepository.js');
			return new pipe_PipelineRepository();
		}
		if(name=="http://nrg.wustl.edu/xnat:qcScanData"){
			if(window.xnat_qcScanData==undefined)dynamicJSLoad('xnat_qcScanData','generated/xnat_qcScanData.js');
			return new xnat_qcScanData();
		}
		if(name=="xnat:studyProtocol"){
			if(window.xnat_studyProtocol==undefined)dynamicJSLoad('xnat_studyProtocol','generated/xnat_studyProtocol.js');
			return new xnat_studyProtocol();
		}
		if(name=="http://nrg.wustl.edu/xnat:dicomSeries_image"){
			if(window.xnat_dicomSeries_image==undefined)dynamicJSLoad('xnat_dicomSeries_image','generated/xnat_dicomSeries_image.js');
			return new xnat_dicomSeries_image();
		}
		if(name=="http://nrg.wustl.edu/xnat:DXSession"){
			if(window.xnat_dxSessionData==undefined)dynamicJSLoad('xnat_dxSessionData','generated/xnat_dxSessionData.js');
			return new xnat_dxSessionData();
		}
		if(name=="xnat:FieldDefinitionSet"){
			if(window.xnat_fieldDefinitionGroup==undefined)dynamicJSLoad('xnat_fieldDefinitionGroup','generated/xnat_fieldDefinitionGroup.js');
			return new xnat_fieldDefinitionGroup();
		}
		if(name=="http://nrg.wustl.edu/xnat:rtSessionData"){
			if(window.xnat_rtSessionData==undefined)dynamicJSLoad('xnat_rtSessionData','generated/xnat_rtSessionData.js');
			return new xnat_rtSessionData();
		}
		if(name=="xnat:CRScan"){
			if(window.xnat_crScanData==undefined)dynamicJSLoad('xnat_crScanData','generated/xnat_crScanData.js');
			return new xnat_crScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:esvScanData"){
			if(window.xnat_esvScanData==undefined)dynamicJSLoad('xnat_esvScanData','generated/xnat_esvScanData.js');
			return new xnat_esvScanData();
		}
		if(name=="pipe:pipelineDetails_parameter"){
			if(window.pipe_pipelineDetails_parameter==undefined)dynamicJSLoad('pipe_pipelineDetails_parameter','generated/pipe_pipelineDetails_parameter.js');
			return new pipe_pipelineDetails_parameter();
		}
		if(name=="xnat:epsSessionData"){
			if(window.xnat_epsSessionData==undefined)dynamicJSLoad('xnat_epsSessionData','generated/xnat_epsSessionData.js');
			return new xnat_epsSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:fieldDefinitionGroup_field"){
			if(window.xnat_fieldDefinitionGroup_field==undefined)dynamicJSLoad('xnat_fieldDefinitionGroup_field','generated/xnat_fieldDefinitionGroup_field.js');
			return new xnat_fieldDefinitionGroup_field();
		}
		if(name=="http://nrg.wustl.edu/xnat:datatypeProtocol"){
			if(window.xnat_datatypeProtocol==undefined)dynamicJSLoad('xnat_datatypeProtocol','generated/xnat_datatypeProtocol.js');
			return new xnat_datatypeProtocol();
		}
		if(name=="http://nrg.wustl.edu/xnat:RFScan"){
			if(window.xnat_rfScanData==undefined)dynamicJSLoad('xnat_rfScanData','generated/xnat_rfScanData.js');
			return new xnat_rfScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:subjectData_field"){
			if(window.xnat_subjectData_field==undefined)dynamicJSLoad('xnat_subjectData_field','generated/xnat_subjectData_field.js');
			return new xnat_subjectData_field();
		}
		if(name=="xdat:user"){
			if(window.xdat_user==undefined)dynamicJSLoad('xdat_user','generated/xdat_user.js');
			return new xdat_user();
		}
		if(name=="xnat:MRScan"){
			if(window.xnat_mrScanData==undefined)dynamicJSLoad('xnat_mrScanData','generated/xnat_mrScanData.js');
			return new xnat_mrScanData();
		}
		if(name=="xnat:otherDicomSessionData"){
			if(window.xnat_otherDicomSessionData==undefined)dynamicJSLoad('xnat_otherDicomSessionData','generated/xnat_otherDicomSessionData.js');
			return new xnat_otherDicomSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:SCScan"){
			if(window.xnat_scScanData==undefined)dynamicJSLoad('xnat_scScanData','generated/xnat_scScanData.js');
			return new xnat_scScanData();
		}
		if(name=="http://nrg.wustl.edu/pipe:pipelineDetails"){
			if(window.pipe_pipelineDetails==undefined)dynamicJSLoad('pipe_pipelineDetails','generated/pipe_pipelineDetails.js');
			return new pipe_pipelineDetails();
		}
		if(name=="http://nrg.wustl.edu/xnat:eegScanData"){
			if(window.xnat_eegScanData==undefined)dynamicJSLoad('xnat_eegScanData','generated/xnat_eegScanData.js');
			return new xnat_eegScanData();
		}
		if(name=="arc:project_descendant"){
			if(window.arc_project_descendant==undefined)dynamicJSLoad('arc_project_descendant','generated/arc_project_descendant.js');
			return new arc_project_descendant();
		}
		if(name=="xnat:qcAssessmentData"){
			if(window.xnat_qcAssessmentData==undefined)dynamicJSLoad('xnat_qcAssessmentData','generated/xnat_qcAssessmentData.js');
			return new xnat_qcAssessmentData();
		}
		if(name=="xnat:abstractSubjectMetadata"){
			if(window.xnat_abstractSubjectMetadata==undefined)dynamicJSLoad('xnat_abstractSubjectMetadata','generated/xnat_abstractSubjectMetadata.js');
			return new xnat_abstractSubjectMetadata();
		}
		if(name=="arc:ArchiveSpecification_notification_type"){
			if(window.arc_ArchiveSpecification_notification_type==undefined)dynamicJSLoad('arc_ArchiveSpecification_notification_type','generated/arc_ArchiveSpecification_notification_type.js');
			return new arc_ArchiveSpecification_notification_type();
		}
		if(name=="http://nrg.wustl.edu/xnat:dxSessionData"){
			if(window.xnat_dxSessionData==undefined)dynamicJSLoad('xnat_dxSessionData','generated/xnat_dxSessionData.js');
			return new xnat_dxSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:experimentData_share"){
			if(window.xnat_experimentData_share==undefined)dynamicJSLoad('xnat_experimentData_share','generated/xnat_experimentData_share.js');
			return new xnat_experimentData_share();
		}
		if(name=="http://nrg.wustl.edu/xnat:ioScanData"){
			if(window.xnat_ioScanData==undefined)dynamicJSLoad('xnat_ioScanData','generated/xnat_ioScanData.js');
			return new xnat_ioScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:contrastBolus"){
			if(window.xnat_contrastBolus==undefined)dynamicJSLoad('xnat_contrastBolus','generated/xnat_contrastBolus.js');
			return new xnat_contrastBolus();
		}
		if(name=="xnat:hdSessionData"){
			if(window.xnat_hdSessionData==undefined)dynamicJSLoad('xnat_hdSessionData','generated/xnat_hdSessionData.js');
			return new xnat_hdSessionData();
		}
		if(name=="cat:entry"){
			if(window.cat_entry==undefined)dynamicJSLoad('cat_entry','generated/cat_entry.js');
			return new cat_entry();
		}
		if(name=="http://nrg.wustl.edu/xnat:qcManualAssessorData"){
			if(window.xnat_qcManualAssessorData==undefined)dynamicJSLoad('xnat_qcManualAssessorData','generated/xnat_qcManualAssessorData.js');
			return new xnat_qcManualAssessorData();
		}
		if(name=="http://nrg.wustl.edu/security:element_security"){
			if(window.xdat_element_security==undefined)dynamicJSLoad('xdat_element_security','generated/xdat_element_security.js');
			return new xdat_element_security();
		}
		if(name=="xnat:ImageRegionResource"){
			if(window.xnat_regionResource==undefined)dynamicJSLoad('xnat_regionResource','generated/xnat_regionResource.js');
			return new xnat_regionResource();
		}
		if(name=="http://nrg.wustl.edu/xnat:ESVSession"){
			if(window.xnat_esvSessionData==undefined)dynamicJSLoad('xnat_esvSessionData','generated/xnat_esvSessionData.js');
			return new xnat_esvSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:XCSession"){
			if(window.xnat_xcSessionData==undefined)dynamicJSLoad('xnat_xcSessionData','generated/xnat_xcSessionData.js');
			return new xnat_xcSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:demographicData"){
			if(window.xnat_demographicData==undefined)dynamicJSLoad('xnat_demographicData','generated/xnat_demographicData.js');
			return new xnat_demographicData();
		}
		if(name=="xnat:QCManualAssessment"){
			if(window.xnat_qcManualAssessorData==undefined)dynamicJSLoad('xnat_qcManualAssessorData','generated/xnat_qcManualAssessorData.js');
			return new xnat_qcManualAssessorData();
		}
		if(name=="http://nrg.wustl.edu/xnat:abstractSubjectMetadata"){
			if(window.xnat_abstractSubjectMetadata==undefined)dynamicJSLoad('xnat_abstractSubjectMetadata','generated/xnat_abstractSubjectMetadata.js');
			return new xnat_abstractSubjectMetadata();
		}
		if(name=="http://nrg.wustl.edu/xnat:QCAssessment"){
			if(window.xnat_qcAssessmentData==undefined)dynamicJSLoad('xnat_qcAssessmentData','generated/xnat_qcAssessmentData.js');
			return new xnat_qcAssessmentData();
		}
		if(name=="xdat:Info"){
			if(window.xdat_infoEntry==undefined)dynamicJSLoad('xdat_infoEntry','generated/xdat_infoEntry.js');
			return new xdat_infoEntry();
		}
		if(name=="xnat:USScan"){
			if(window.xnat_usScanData==undefined)dynamicJSLoad('xnat_usScanData','generated/xnat_usScanData.js');
			return new xnat_usScanData();
		}
		if(name=="xdat:Search"){
			if(window.xdat_Search==undefined)dynamicJSLoad('xdat_Search','generated/xdat_Search.js');
			return new xdat_Search();
		}
		if(name=="xnat:xaSessionData"){
			if(window.xnat_xaSessionData==undefined)dynamicJSLoad('xnat_xaSessionData','generated/xnat_xaSessionData.js');
			return new xnat_xaSessionData();
		}
		if(name=="xnat:imageSessionData"){
			if(window.xnat_imageSessionData==undefined)dynamicJSLoad('xnat_imageSessionData','generated/xnat_imageSessionData.js');
			return new xnat_imageSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:DXScan"){
			if(window.xnat_dxScanData==undefined)dynamicJSLoad('xnat_dxScanData','generated/xnat_dxScanData.js');
			return new xnat_dxScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:crScanData"){
			if(window.xnat_crScanData==undefined)dynamicJSLoad('xnat_crScanData','generated/xnat_crScanData.js');
			return new xnat_crScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:qcScanData_field"){
			if(window.xnat_qcScanData_field==undefined)dynamicJSLoad('xnat_qcScanData_field','generated/xnat_qcScanData_field.js');
			return new xnat_qcScanData_field();
		}
		if(name=="http://nrg.wustl.edu/xnat:reconstructedImageData_scanID"){
			if(window.xnat_reconstructedImageData_scanID==undefined)dynamicJSLoad('xnat_reconstructedImageData_scanID','generated/xnat_reconstructedImageData_scanID.js');
			return new xnat_reconstructedImageData_scanID();
		}
		if(name=="xnat_a:SCID"){
			if(window.xnat_a_scidResearchData==undefined)dynamicJSLoad('xnat_a_scidResearchData','generated/xnat_a_scidResearchData.js');
			return new xnat_a_scidResearchData();
		}
		if(name=="http://nrg.wustl.edu/xnat:CTSession"){
			if(window.xnat_ctSessionData==undefined)dynamicJSLoad('xnat_ctSessionData','generated/xnat_ctSessionData.js');
			return new xnat_ctSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:GMSession"){
			if(window.xnat_gmSessionData==undefined)dynamicJSLoad('xnat_gmSessionData','generated/xnat_gmSessionData.js');
			return new xnat_gmSessionData();
		}
		if(name=="xnat:MGScan"){
			if(window.xnat_mgScanData==undefined)dynamicJSLoad('xnat_mgScanData','generated/xnat_mgScanData.js');
			return new xnat_mgScanData();
		}
		if(name=="xnat:GMVSession"){
			if(window.xnat_gmvSessionData==undefined)dynamicJSLoad('xnat_gmvSessionData','generated/xnat_gmvSessionData.js');
			return new xnat_gmvSessionData();
		}
		if(name=="http://nrg.wustl.edu/xnat:Subject"){
			if(window.xnat_subjectData==undefined)dynamicJSLoad('xnat_subjectData','generated/xnat_subjectData.js');
			return new xnat_subjectData();
		}
		if(name=="xdat:stored_search_allowed_user"){
			if(window.xdat_stored_search_allowed_user==undefined)dynamicJSLoad('xdat_stored_search_allowed_user','generated/xdat_stored_search_allowed_user.js');
			return new xdat_stored_search_allowed_user();
		}
		if(name=="http://nrg.wustl.edu/catalog:dcmCatalog"){
			if(window.cat_dcmCatalog==undefined)dynamicJSLoad('cat_dcmCatalog','generated/cat_dcmCatalog.js');
			return new cat_dcmCatalog();
		}
		if(name=="xnat:esvScanData"){
			if(window.xnat_esvScanData==undefined)dynamicJSLoad('xnat_esvScanData','generated/xnat_esvScanData.js');
			return new xnat_esvScanData();
		}
		if(name=="http://nrg.wustl.edu/scr:screeningScanData"){
			if(window.scr_screeningScanData==undefined)dynamicJSLoad('scr_screeningScanData','generated/scr_screeningScanData.js');
			return new scr_screeningScanData();
		}
		if(name=="arc:fieldSpecification"){
			if(window.arc_fieldSpecification==undefined)dynamicJSLoad('arc_fieldSpecification','generated/arc_fieldSpecification.js');
			return new arc_fieldSpecification();
		}
		if(name=="http://nrg.wustl.edu/xnat:subjectData"){
			if(window.xnat_subjectData==undefined)dynamicJSLoad('xnat_subjectData','generated/xnat_subjectData.js');
			return new xnat_subjectData();
		}
		if(name=="http://nrg.wustl.edu/security:field_mapping_set"){
			if(window.xdat_field_mapping_set==undefined)dynamicJSLoad('xdat_field_mapping_set','generated/xdat_field_mapping_set.js');
			return new xdat_field_mapping_set();
		}
		if(name=="http://nrg.wustl.edu/xnat:mrsScanData"){
			if(window.xnat_mrsScanData==undefined)dynamicJSLoad('xnat_mrsScanData','generated/xnat_mrsScanData.js');
			return new xnat_mrsScanData();
		}
		if(name=="xnat_a:ybocsData"){
			if(window.xnat_a_ybocsData==undefined)dynamicJSLoad('xnat_a_ybocsData','generated/xnat_a_ybocsData.js');
			return new xnat_a_ybocsData();
		}
		if(name=="http://nrg.wustl.edu/xnat:epsSessionData"){
			if(window.xnat_epsSessionData==undefined)dynamicJSLoad('xnat_epsSessionData','generated/xnat_epsSessionData.js');
			return new xnat_epsSessionData();
		}
		if(name=="http://nrg.wustl.edu/security:user_groupID"){
			if(window.xdat_user_groupID==undefined)dynamicJSLoad('xdat_user_groupID','generated/xdat_user_groupID.js');
			return new xdat_user_groupID();
		}
		if(name=="arc:pathInfo"){
			if(window.arc_pathInfo==undefined)dynamicJSLoad('arc_pathInfo','generated/arc_pathInfo.js');
			return new arc_pathInfo();
		}
		if(name=="xnat:otherDicomScanData"){
			if(window.xnat_otherDicomScanData==undefined)dynamicJSLoad('xnat_otherDicomScanData','generated/xnat_otherDicomScanData.js');
			return new xnat_otherDicomScanData();
		}
		if(name=="arc:ArchiveSpecification"){
			if(window.arc_ArchiveSpecification==undefined)dynamicJSLoad('arc_ArchiveSpecification','generated/arc_ArchiveSpecification.js');
			return new arc_ArchiveSpecification();
		}
		if(name=="wrk:workflowData"){
			if(window.wrk_workflowData==undefined)dynamicJSLoad('wrk_workflowData','generated/wrk_workflowData.js');
			return new wrk_workflowData();
		}
		if(name=="xdat:stored_search_groupID"){
			if(window.xdat_stored_search_groupID==undefined)dynamicJSLoad('xdat_stored_search_groupID','generated/xdat_stored_search_groupID.js');
			return new xdat_stored_search_groupID();
		}
		if(name=="xnat:xcSessionData"){
			if(window.xnat_xcSessionData==undefined)dynamicJSLoad('xnat_xcSessionData','generated/xnat_xcSessionData.js');
			return new xnat_xcSessionData();
		}
		if(name=="xnat:eegScanData"){
			if(window.xnat_eegScanData==undefined)dynamicJSLoad('xnat_eegScanData','generated/xnat_eegScanData.js');
			return new xnat_eegScanData();
		}
		if(name=="xnat:VoiceAudioScan"){
			if(window.xnat_voiceAudioScanData==undefined)dynamicJSLoad('xnat_voiceAudioScanData','generated/xnat_voiceAudioScanData.js');
			return new xnat_voiceAudioScanData();
		}
		if(name=="xnat:EPSScan"){
			if(window.xnat_epsScanData==undefined)dynamicJSLoad('xnat_epsScanData','generated/xnat_epsScanData.js');
			return new xnat_epsScanData();
		}
		if(name=="http://nrg.wustl.edu/workflow:xnatExecutionEnvironment_notify"){
			if(window.wrk_xnatExecutionEnvironment_notify==undefined)dynamicJSLoad('wrk_xnatExecutionEnvironment_notify','generated/wrk_xnatExecutionEnvironment_notify.js');
			return new wrk_xnatExecutionEnvironment_notify();
		}
		if(name=="http://nrg.wustl.edu/xnat:petQcScanData"){
			if(window.xnat_petQcScanData==undefined)dynamicJSLoad('xnat_petQcScanData','generated/xnat_petQcScanData.js');
			return new xnat_petQcScanData();
		}
		if(name=="xnat:abstractStatistics"){
			if(window.xnat_abstractStatistics==undefined)dynamicJSLoad('xnat_abstractStatistics','generated/xnat_abstractStatistics.js');
			return new xnat_abstractStatistics();
		}
		if(name=="http://nrg.wustl.edu/xnat:MGSession"){
			if(window.xnat_mgSessionData==undefined)dynamicJSLoad('xnat_mgSessionData','generated/xnat_mgSessionData.js');
			return new xnat_mgSessionData();
		}
		if(name=="xnat:OPTScan"){
			if(window.xnat_optScanData==undefined)dynamicJSLoad('xnat_optScanData','generated/xnat_optScanData.js');
			return new xnat_optScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:EEGScan"){
			if(window.xnat_eegScanData==undefined)dynamicJSLoad('xnat_eegScanData','generated/xnat_eegScanData.js');
			return new xnat_eegScanData();
		}
		if(name=="xnat:projectParticipant"){
			if(window.xnat_projectParticipant==undefined)dynamicJSLoad('xnat_projectParticipant','generated/xnat_projectParticipant.js');
			return new xnat_projectParticipant();
		}
		if(name=="xnat:regionResource"){
			if(window.xnat_regionResource==undefined)dynamicJSLoad('xnat_regionResource','generated/xnat_regionResource.js');
			return new xnat_regionResource();
		}
		if(name=="xnat:ReconstructedImage"){
			if(window.xnat_reconstructedImageData==undefined)dynamicJSLoad('xnat_reconstructedImageData','generated/xnat_reconstructedImageData.js');
			return new xnat_reconstructedImageData();
		}
		if(name=="xnat:imageAssessorData"){
			if(window.xnat_imageAssessorData==undefined)dynamicJSLoad('xnat_imageAssessorData','generated/xnat_imageAssessorData.js');
			return new xnat_imageAssessorData();
		}
		if(name=="xnat:experimentData_share"){
			if(window.xnat_experimentData_share==undefined)dynamicJSLoad('xnat_experimentData_share','generated/xnat_experimentData_share.js');
			return new xnat_experimentData_share();
		}
		if(name=="xnat:esvSessionData"){
			if(window.xnat_esvSessionData==undefined)dynamicJSLoad('xnat_esvSessionData','generated/xnat_esvSessionData.js');
			return new xnat_esvSessionData();
		}
		if(name=="http://nrg.wustl.edu/security:role_type"){
			if(window.xdat_role_type==undefined)dynamicJSLoad('xdat_role_type','generated/xdat_role_type.js');
			return new xdat_role_type();
		}
		if(name=="http://nrg.wustl.edu/security:stored_search_allowed_user"){
			if(window.xdat_stored_search_allowed_user==undefined)dynamicJSLoad('xdat_stored_search_allowed_user','generated/xdat_stored_search_allowed_user.js');
			return new xdat_stored_search_allowed_user();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:SideEffectsPittsburgh"){
			if(window.xnat_a_sideEffectsPittsburghData==undefined)dynamicJSLoad('xnat_a_sideEffectsPittsburghData','generated/xnat_a_sideEffectsPittsburghData.js');
			return new xnat_a_sideEffectsPittsburghData();
		}
		if(name=="xnat:smScanData"){
			if(window.xnat_smScanData==undefined)dynamicJSLoad('xnat_smScanData','generated/xnat_smScanData.js');
			return new xnat_smScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:studyProtocol"){
			if(window.xnat_studyProtocol==undefined)dynamicJSLoad('xnat_studyProtocol','generated/xnat_studyProtocol.js');
			return new xnat_studyProtocol();
		}
		if(name=="http://nrg.wustl.edu/xnat:SegScan"){
			if(window.xnat_segScanData==undefined)dynamicJSLoad('xnat_segScanData','generated/xnat_segScanData.js');
			return new xnat_segScanData();
		}
		if(name=="http://nrg.wustl.edu/catalog:entry_tag"){
			if(window.cat_entry_tag==undefined)dynamicJSLoad('cat_entry_tag','generated/cat_entry_tag.js');
			return new cat_entry_tag();
		}
		if(name=="xnat:validationData"){
			if(window.xnat_validationData==undefined)dynamicJSLoad('xnat_validationData','generated/xnat_validationData.js');
			return new xnat_validationData();
		}
		if(name=="xnat:computationData"){
			if(window.xnat_computationData==undefined)dynamicJSLoad('xnat_computationData','generated/xnat_computationData.js');
			return new xnat_computationData();
		}
		if(name=="http://nrg.wustl.edu/xnat:RTImageScan"){
			if(window.xnat_rtImageScanData==undefined)dynamicJSLoad('xnat_rtImageScanData','generated/xnat_rtImageScanData.js');
			return new xnat_rtImageScanData();
		}
		if(name=="xnat:RFScan"){
			if(window.xnat_rfScanData==undefined)dynamicJSLoad('xnat_rfScanData','generated/xnat_rfScanData.js');
			return new xnat_rfScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:gmScanData"){
			if(window.xnat_gmScanData==undefined)dynamicJSLoad('xnat_gmScanData','generated/xnat_gmScanData.js');
			return new xnat_gmScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:voiceAudioScanData"){
			if(window.xnat_voiceAudioScanData==undefined)dynamicJSLoad('xnat_voiceAudioScanData','generated/xnat_voiceAudioScanData.js');
			return new xnat_voiceAudioScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:NMScan"){
			if(window.xnat_nmScanData==undefined)dynamicJSLoad('xnat_nmScanData','generated/xnat_nmScanData.js');
			return new xnat_nmScanData();
		}
		if(name=="xnat:nmScanData"){
			if(window.xnat_nmScanData==undefined)dynamicJSLoad('xnat_nmScanData','generated/xnat_nmScanData.js');
			return new xnat_nmScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:segScanData"){
			if(window.xnat_segScanData==undefined)dynamicJSLoad('xnat_segScanData','generated/xnat_segScanData.js');
			return new xnat_segScanData();
		}
		if(name=="http://nrg.wustl.edu/workflow:xnatExecutionEnvironment"){
			if(window.wrk_xnatExecutionEnvironment==undefined)dynamicJSLoad('wrk_xnatExecutionEnvironment','generated/wrk_xnatExecutionEnvironment.js');
			return new wrk_xnatExecutionEnvironment();
		}
		if(name=="http://nrg.wustl.edu/xnat:srScanData"){
			if(window.xnat_srScanData==undefined)dynamicJSLoad('xnat_srScanData','generated/xnat_srScanData.js');
			return new xnat_srScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:abstractResource"){
			if(window.xnat_abstractResource==undefined)dynamicJSLoad('xnat_abstractResource','generated/xnat_abstractResource.js');
			return new xnat_abstractResource();
		}
		if(name=="http://nrg.wustl.edu/xnat:fieldDefinitionGroup"){
			if(window.xnat_fieldDefinitionGroup==undefined)dynamicJSLoad('xnat_fieldDefinitionGroup','generated/xnat_fieldDefinitionGroup.js');
			return new xnat_fieldDefinitionGroup();
		}
		if(name=="xdat:field_mapping"){
			if(window.xdat_field_mapping==undefined)dynamicJSLoad('xdat_field_mapping','generated/xdat_field_mapping.js');
			return new xdat_field_mapping();
		}
		if(name=="xnat:SCScan"){
			if(window.xnat_scScanData==undefined)dynamicJSLoad('xnat_scScanData','generated/xnat_scScanData.js');
			return new xnat_scScanData();
		}
		if(name=="http://nrg.wustl.edu/catalog:DCMCatalog"){
			if(window.cat_dcmCatalog==undefined)dynamicJSLoad('cat_dcmCatalog','generated/cat_dcmCatalog.js');
			return new cat_dcmCatalog();
		}
		if(name=="http://nrg.wustl.edu/arc:project_descendant"){
			if(window.arc_project_descendant==undefined)dynamicJSLoad('arc_project_descendant','generated/arc_project_descendant.js');
			return new arc_project_descendant();
		}
		if(name=="http://nrg.wustl.edu/xnat:subjectMetadata"){
			if(window.xnat_subjectMetadata==undefined)dynamicJSLoad('xnat_subjectMetadata','generated/xnat_subjectMetadata.js');
			return new xnat_subjectMetadata();
		}
		if(name=="val:additionalVal"){
			if(window.val_additionalVal==undefined)dynamicJSLoad('val_additionalVal','generated/val_additionalVal.js');
			return new val_additionalVal();
		}
		if(name=="http://nrg.wustl.edu/xnat:studyProtocol_variable"){
			if(window.xnat_studyProtocol_variable==undefined)dynamicJSLoad('xnat_studyProtocol_variable','generated/xnat_studyProtocol_variable.js');
			return new xnat_studyProtocol_variable();
		}
		if(name=="xnat:mrsScanData"){
			if(window.xnat_mrsScanData==undefined)dynamicJSLoad('xnat_mrsScanData','generated/xnat_mrsScanData.js');
			return new xnat_mrsScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat_assessments:scidResearchData"){
			if(window.xnat_a_scidResearchData==undefined)dynamicJSLoad('xnat_a_scidResearchData','generated/xnat_a_scidResearchData.js');
			return new xnat_a_scidResearchData();
		}
		if(name=="http://nrg.wustl.edu/arc:ArchiveSpecification"){
			if(window.arc_ArchiveSpecification==undefined)dynamicJSLoad('arc_ArchiveSpecification','generated/arc_ArchiveSpecification.js');
			return new arc_ArchiveSpecification();
		}
		if(name=="xnat:resourceSeries"){
			if(window.xnat_resourceSeries==undefined)dynamicJSLoad('xnat_resourceSeries','generated/xnat_resourceSeries.js');
			return new xnat_resourceSeries();
		}
		if(name=="http://nrg.wustl.edu/security:action_type"){
			if(window.xdat_action_type==undefined)dynamicJSLoad('xdat_action_type','generated/xdat_action_type.js');
			return new xdat_action_type();
		}
		if(name=="xnat:rfSessionData"){
			if(window.xnat_rfSessionData==undefined)dynamicJSLoad('xnat_rfSessionData','generated/xnat_rfSessionData.js');
			return new xnat_rfSessionData();
		}
		if(name=="xnat:mrAssessorData"){
			if(window.xnat_mrAssessorData==undefined)dynamicJSLoad('xnat_mrAssessorData','generated/xnat_mrAssessorData.js');
			return new xnat_mrAssessorData();
		}
		if(name=="xnat:qcScanData"){
			if(window.xnat_qcScanData==undefined)dynamicJSLoad('xnat_qcScanData','generated/xnat_qcScanData.js');
			return new xnat_qcScanData();
		}
		if(name=="xdat:userGroup"){
			if(window.xdat_userGroup==undefined)dynamicJSLoad('xdat_userGroup','generated/xdat_userGroup.js');
			return new xdat_userGroup();
		}
		if(name=="http://nrg.wustl.edu/security:infoEntry"){
			if(window.xdat_infoEntry==undefined)dynamicJSLoad('xdat_infoEntry','generated/xdat_infoEntry.js');
			return new xdat_infoEntry();
		}
		if(name=="http://nrg.wustl.edu/security:security"){
			if(window.xdat_security==undefined)dynamicJSLoad('xdat_security','generated/xdat_security.js');
			return new xdat_security();
		}
		if(name=="http://nrg.wustl.edu/xnat:mrScanData"){
			if(window.xnat_mrScanData==undefined)dynamicJSLoad('xnat_mrScanData','generated/xnat_mrScanData.js');
			return new xnat_mrScanData();
		}
		if(name=="http://nrg.wustl.edu/xnat:xcvScanData"){
			if(window.xnat_xcvScanData==undefined)dynamicJSLoad('xnat_xcvScanData','generated/xnat_xcvScanData.js');
			return new xnat_xcvScanData();
		}
		if(name=="http://nrg.wustl.edu/security:bundle"){
			if(window.xdat_stored_search==undefined)dynamicJSLoad('xdat_stored_search','generated/xdat_stored_search.js');
			return new xdat_stored_search();
		}
		if(name=="http://nrg.wustl.edu/catalog:catalog_metaField"){
			if(window.cat_catalog_metaField==undefined)dynamicJSLoad('cat_catalog_metaField','generated/cat_catalog_metaField.js');
			return new cat_catalog_metaField();
		}
		if(name=="http://nrg.wustl.edu/xnat:Project"){
			if(window.xnat_projectData==undefined)dynamicJSLoad('xnat_projectData','generated/xnat_projectData.js');
			return new xnat_projectData();
		}
		if(name=="http://nrg.wustl.edu/workflow:Workflow"){
			if(window.wrk_workflowData==undefined)dynamicJSLoad('wrk_workflowData','generated/wrk_workflowData.js');
			return new wrk_workflowData();
		}
		if(name=="xnat:XASession"){
			if(window.xnat_xaSessionData==undefined)dynamicJSLoad('xnat_xaSessionData','generated/xnat_xaSessionData.js');
			return new xnat_xaSessionData();
		}
		if(name=="arc:project_descendant_pipeline"){
			if(window.arc_project_descendant_pipeline==undefined)dynamicJSLoad('arc_project_descendant_pipeline','generated/arc_project_descendant_pipeline.js');
			return new arc_project_descendant_pipeline();
		}
		if(name=="http://nrg.wustl.edu/xnat:VoiceAudioScan"){
			if(window.xnat_voiceAudioScanData==undefined)dynamicJSLoad('xnat_voiceAudioScanData','generated/xnat_voiceAudioScanData.js');
			return new xnat_voiceAudioScanData();
		}
		if(name=="xdat:change_info"){
			if(window.xdat_change_info==undefined)dynamicJSLoad('xdat_change_info','generated/xdat_change_info.js');
			return new xdat_change_info();
		}
		if(name=="http://nrg.wustl.edu/xnat:resource"){
			if(window.xnat_resource==undefined)dynamicJSLoad('xnat_resource','generated/xnat_resource.js');
			return new xnat_resource();
		}
		if(name=="http://nrg.wustl.edu/xnat:xa3DSessionData"){
			if(window.xnat_xa3DSessionData==undefined)dynamicJSLoad('xnat_xa3DSessionData','generated/xnat_xa3DSessionData.js');
			return new xnat_xa3DSessionData();
		}
		if(name=="xnat:petmrSessionData"){
			if(window.xnat_petmrSessionData==undefined)dynamicJSLoad('xnat_petmrSessionData','generated/xnat_petmrSessionData.js');
			return new xnat_petmrSessionData();
		}
	}
}
