/*
 * web: ClassMapping.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

function ClassMapping(){
	this.newInstance=function(name) {
        var undefined;
        if (name.lastIndexOf('/') > 0) name = name.substring(name.lastIndexOf('/') + 1);
        var prefix = name.substring(name.lastIndexOf('/') + 1, name.indexOf(':'));
        var reservedPrefixes = [
            'arc',
            'cat',
            'catalog',
            'pipe',
			'security',
            'workflow',
            'wrk',
            'xdat'
        ];

        // special stupid hack for custom variables
        if (name === "xnat:FieldDefinitionSet") {
            if(window.xnat_fieldDefinitionGroup==undefined)dynamicJSLoad('xnat_fieldDefinitionGroup','generated/xnat_fieldDefinitionGroup.js');
            return new xnat_fieldDefinitionGroup();
        }

        if (reservedPrefixes.indexOf(prefix) < 0) {
            /*************************************
             * These types of names can be handled
             *************************************/

            // if(name=="xnat:mrSessionData"){
            // 	if(window.xnat_mrSessionData==undefined)dynamicJSLoad('xnat_mrSessionData','generated/xnat_mrSessionData.js');
            // 	return new xnat_mrSessionData();
            // }
            // if(name=="http://nrg.wustl.edu/xnat:mrSessionData"){
            // 	if(window.xnat_mrSessionData==undefined)dynamicJSLoad('xnat_mrSessionData','generated/xnat_mrSessionData.js');
            // 	return new xnat_mrSessionData();
            // }

            name = name.replace(':', '_');
            if (window[name] === undefined) {
                dynamicJSLoad(name,'generated/" + name + ".js');
            }
            return new window[name]();

            /********************************************************
             * These cannot, but they don't seem to be used in the UI
             ********************************************************/

            // if(name=="xnat:MRSession"){
            // 	if(window.xnat_mrSessionData==undefined)dynamicJSLoad('xnat_mrSessionData','generated/xnat_mrSessionData.js');
            // 	return new xnat_mrSessionData();
            // }
            // if(name=="http://nrg.wustl.edu/xnat:MRSession"){
            // 	if(window.xnat_mrSessionData==undefined)dynamicJSLoad('xnat_mrSessionData','generated/xnat_mrSessionData.js');
            // 	return new xnat_mrSessionData();
            // }
        }
        else {
            /*************************************************
             * These is where ugly hacks and legacy junk lives
             *************************************************/

            if (prefix === 'arc') {
                if (name == "arc:ArchiveSpecification") {
                    if (window.arc_ArchiveSpecification == undefined) dynamicJSLoad('arc_ArchiveSpecification', 'generated/arc_ArchiveSpecification.js');
                    return new arc_ArchiveSpecification();
                }
                if (name == "arc:ArchiveSpecification_notification_type") {
                    if (window.arc_ArchiveSpecification_notification_type == undefined) dynamicJSLoad('arc_ArchiveSpecification_notification_type', 'generated/arc_ArchiveSpecification_notification_type.js');
                    return new arc_ArchiveSpecification_notification_type();
                }
                if (name == "arc:fieldSpecification") {
                    if (window.arc_fieldSpecification == undefined) dynamicJSLoad('arc_fieldSpecification', 'generated/arc_fieldSpecification.js');
                    return new arc_fieldSpecification();
                }
                if (name == "arc:pathInfo") {
                    if (window.arc_pathInfo == undefined) dynamicJSLoad('arc_pathInfo', 'generated/arc_pathInfo.js');
                    return new arc_pathInfo();
                }
                if(name=="arc:pipelineData"){
                    if(window.arc_pipelineData==undefined)dynamicJSLoad('arc_pipelineData','generated/arc_pipelineData.js');
                    return new arc_pipelineData();
                }
                if (name == "arc:pipelineParameterData") {
                    if (window.arc_pipelineParameterData == undefined) dynamicJSLoad('arc_pipelineParameterData', 'generated/arc_pipelineParameterData.js');
                    return new arc_pipelineParameterData();
                }
                if (name == "arc:project") {
                    if (window.arc_project == undefined) dynamicJSLoad('arc_project', 'generated/arc_project.js');
                    return new arc_project();
                }
                if (name == "arc:project_descendant") {
                    if (window.arc_project_descendant == undefined) dynamicJSLoad('arc_project_descendant', 'generated/arc_project_descendant.js');
                    return new arc_project_descendant();
                }
                if (name == "arc:project_descendant_pipeline") {
                    if (window.arc_project_descendant_pipeline == undefined) dynamicJSLoad('arc_project_descendant_pipeline', 'generated/arc_project_descendant_pipeline.js');
                    return new arc_project_descendant_pipeline();
                }
                if (name == "arc:project_pipeline") {
                    if (window.arc_project_pipeline == undefined) dynamicJSLoad('arc_project_pipeline', 'generated/arc_project_pipeline.js');
                    return new arc_project_pipeline();
                }
                if(name=="arc:property"){
                    if(window.arc_property==undefined)dynamicJSLoad('arc_property','generated/arc_property.js');
                    return new arc_property();
                }
            }

            if (prefix === 'cat' || prefix === 'catalog') {
                if(name=="cat:catalog" || name == "cat:Catalog" || name=="catalog:Catalog"){
                    if(window.cat_catalog==undefined)dynamicJSLoad('cat_catalog','generated/cat_catalog.js');
                    return new cat_catalog();
                }
                if (name == "cat:catalog_metaField" || name == "catalog:catalog_metaField") {
                    if (window.cat_catalog_metaField == undefined) dynamicJSLoad('cat_catalog_metaField', 'generated/cat_catalog_metaField.js');
                    return new cat_catalog_metaField();
                }
                if(name=="cat:catalog_tag" || name == "catalog:catalog_tag"){
                    if(window.cat_catalog_tag==undefined)dynamicJSLoad('cat_catalog_tag','generated/cat_catalog_tag.js');
                    return new cat_catalog_tag();
                }
                if (name == "cat:DCMCatalog" || name == "cat:dcmCatalog" || name == "catalog:dcmCatalog") {
                    if (window.cat_dcmCatalog == undefined) dynamicJSLoad('cat_dcmCatalog', 'generated/cat_dcmCatalog.js');
                    return new cat_dcmCatalog();
                }
                if (name == "cat:dcmEntry" || name == "catalog:dcmEntry") {
                    if (window.cat_dcmEntry == undefined) dynamicJSLoad('cat_dcmEntry', 'generated/cat_dcmEntry.js');
                    return new cat_dcmEntry();
                }
                if (name == "cat:entry" || name == "catalog:entry" || name == "cat:Entry" || name == "catalog:Entry") {
                    if (window.cat_entry == undefined) dynamicJSLoad('cat_entry', 'generated/cat_entry.js');
                    return new cat_entry();
                }
                if(name=="cat:entry_metaField" || name == "catalog:entry_metaField"){
                    if(window.cat_entry_metaField==undefined)dynamicJSLoad('cat_entry_metaField','generated/cat_entry_metaField.js');
                    return new cat_entry_metaField();
                }
                if(name=="cat:entry_tag" || name == "catalog:entry_tag"){
                    if(window.cat_entry_tag==undefined)dynamicJSLoad('cat_entry_tag','generated/cat_entry_tag.js');
                    return new cat_entry_tag();
                }
            }

            if (prefix === 'pipe') {
                if (name == "pipe:pipelineDetails_parameter") {
                    if (window.pipe_pipelineDetails_parameter == undefined) dynamicJSLoad('pipe_pipelineDetails_parameter', 'generated/pipe_pipelineDetails_parameter.js');
                    return new pipe_pipelineDetails_parameter();
                }
                if (name == "pipe:PipelineRepository") {
                    if (window.pipe_PipelineRepository == undefined) dynamicJSLoad('pipe_PipelineRepository', 'generated/pipe_PipelineRepository.js');
                    return new pipe_PipelineRepository();
                }
                if (name == "pipe:pipelineDetails_element") {
                    if (window.pipe_pipelineDetails_element == undefined) dynamicJSLoad('pipe_pipelineDetails_element', 'generated/pipe_pipelineDetails_element.js');
                    return new pipe_pipelineDetails_element();
                }
                if(name=="pipe:pipelineDetails"){
                    if(window.pipe_pipelineDetails==undefined)dynamicJSLoad('pipe_pipelineDetails','generated/pipe_pipelineDetails.js');
                    return new pipe_pipelineDetails();
                }
                if(name=="pipe:PipelineRepository"){
                    if(window.pipe_PipelineRepository==undefined)dynamicJSLoad('pipe_PipelineRepository','generated/pipe_PipelineRepository.js');
                    return new pipe_PipelineRepository();
                }
                if(name=="pipe:pipelineDetails_parameter"){
                    if(window.pipe_pipelineDetails_parameter==undefined)dynamicJSLoad('pipe_pipelineDetails_parameter','generated/pipe_pipelineDetails_parameter.js');
                    return new pipe_pipelineDetails_parameter();
                }
                if(name=="pipe:pipelineDetails_element"){
                    if(window.pipe_pipelineDetails_element==undefined)dynamicJSLoad('pipe_pipelineDetails_element','generated/pipe_pipelineDetails_element.js');
                    return new pipe_pipelineDetails_element();
                }
                if(name=="pipe:pipelineDetails"){
                    if(window.pipe_pipelineDetails==undefined)dynamicJSLoad('pipe_pipelineDetails','generated/pipe_pipelineDetails.js');
                    return new pipe_pipelineDetails();
                }
            }

            if (prefix === 'security') {
                if(name=="security:action_type"){
                    if(window.xdat_action_type==undefined)dynamicJSLoad('xdat_action_type','generated/xdat_action_type.js');
                    return new xdat_action_type();
                }
                if(name=="security:bundle"){
                    if(window.xdat_stored_search==undefined)dynamicJSLoad('xdat_stored_search','generated/xdat_stored_search.js');
                    return new xdat_stored_search();
                }
                if(name=="security:infoEntry"){
                    if(window.xdat_infoEntry==undefined)dynamicJSLoad('xdat_infoEntry','generated/xdat_infoEntry.js');
                    return new xdat_infoEntry();
                }
                if(name=="security:security"){
                    if(window.xdat_security==undefined)dynamicJSLoad('xdat_security','generated/xdat_security.js');
                    return new xdat_security();
                }
                if(name=="security:role_type"){
                    if(window.xdat_role_type==undefined)dynamicJSLoad('xdat_role_type','generated/xdat_role_type.js');
                    return new xdat_role_type();
                }
                if(name=="security:stored_search_allowed_user"){
                    if(window.xdat_stored_search_allowed_user==undefined)dynamicJSLoad('xdat_stored_search_allowed_user','generated/xdat_stored_search_allowed_user.js');
                    return new xdat_stored_search_allowed_user();
                }
                if(name=="security:user_groupID"){
                    if(window.xdat_user_groupID==undefined)dynamicJSLoad('xdat_user_groupID','generated/xdat_user_groupID.js');
                    return new xdat_user_groupID();
                }
                if(name=="security:field_mapping_set"){
                    if(window.xdat_field_mapping_set==undefined)dynamicJSLoad('xdat_field_mapping_set','generated/xdat_field_mapping_set.js');
                    return new xdat_field_mapping_set();
                }
                if(name=="security:element_security"){
                    if(window.xdat_element_security==undefined)dynamicJSLoad('xdat_element_security','generated/xdat_element_security.js');
                    return new xdat_element_security();
                }
                if(name=="security:user_login"){
                    if(window.xdat_user_login==undefined)dynamicJSLoad('xdat_user_login','generated/xdat_user_login.js');
                    return new xdat_user_login();
                }
                if(name=="security:stored_search_groupID"){
                    if(window.xdat_stored_search_groupID==undefined)dynamicJSLoad('xdat_stored_search_groupID','generated/xdat_stored_search_groupID.js');
                    return new xdat_stored_search_groupID();
                }
                if(name=="security:search_field"){
                    if(window.xdat_search_field==undefined)dynamicJSLoad('xdat_search_field','generated/xdat_search_field.js');
                    return new xdat_search_field();
                }
                if(name=="security:Search"){
                    if(window.xdat_Search==undefined)dynamicJSLoad('xdat_Search','generated/xdat_Search.js');
                    return new xdat_Search();
                }
                if(name=="security:criteria_set"){
                    if(window.xdat_criteria_set==undefined)dynamicJSLoad('xdat_criteria_set','generated/xdat_criteria_set.js');
                    return new xdat_criteria_set();
                }
                if(name=="security:News"){
                    if(window.xdat_newsEntry==undefined)dynamicJSLoad('xdat_newsEntry','generated/xdat_newsEntry.js');
                    return new xdat_newsEntry();
                }
                if(name=="security:element_access"){
                    if(window.xdat_element_access==undefined)dynamicJSLoad('xdat_element_access','generated/xdat_element_access.js');
                    return new xdat_element_access();
                }
                if(name=="security:criteria"){
                    if(window.xdat_criteria==undefined)dynamicJSLoad('xdat_criteria','generated/xdat_criteria.js');
                    return new xdat_criteria();
                }
                if(name=="security:XDATUser"){
                    if(window.xdat_user==undefined)dynamicJSLoad('xdat_user','generated/xdat_user.js');
                    return new xdat_user();
                }
                if(name=="security:userGroup"){
                    if(window.xdat_userGroup==undefined)dynamicJSLoad('xdat_userGroup','generated/xdat_userGroup.js');
                    return new xdat_userGroup();
                }
                if(name=="security:field_mapping"){
                    if(window.xdat_field_mapping==undefined)dynamicJSLoad('xdat_field_mapping','generated/xdat_field_mapping.js');
                    return new xdat_field_mapping();
                }
                if(name=="security:Info"){
                    if(window.xdat_infoEntry==undefined)dynamicJSLoad('xdat_infoEntry','generated/xdat_infoEntry.js');
                    return new xdat_infoEntry();
                }
                if(name=="security:user"){
                    if(window.xdat_user==undefined)dynamicJSLoad('xdat_user','generated/xdat_user.js');
                    return new xdat_user();
                }
                if(name=="security:UserGroup"){
                    if(window.xdat_userGroup==undefined)dynamicJSLoad('xdat_userGroup','generated/xdat_userGroup.js');
                    return new xdat_userGroup();
                }
                if(name=="security:element_action_type"){
                    if(window.xdat_element_action_type==undefined)dynamicJSLoad('xdat_element_action_type','generated/xdat_element_action_type.js');
                    return new xdat_element_action_type();
                }
                if(name=="security:newsEntry"){
                    if(window.xdat_newsEntry==undefined)dynamicJSLoad('xdat_newsEntry','generated/xdat_newsEntry.js');
                    return new xdat_newsEntry();
                }
                if(name=="security:element_security_listing_action"){
                    if(window.xdat_element_security_listing_action==undefined)dynamicJSLoad('xdat_element_security_listing_action','generated/xdat_element_security_listing_action.js');
                    return new xdat_element_security_listing_action();
                }
                if(name=="security:access_log"){
                    if(window.xdat_access_log==undefined)dynamicJSLoad('xdat_access_log','generated/xdat_access_log.js');
                    return new xdat_access_log();
                }
                if(name=="security:element_access_secure_ip"){
                    if(window.xdat_element_access_secure_ip==undefined)dynamicJSLoad('xdat_element_access_secure_ip','generated/xdat_element_access_secure_ip.js');
                    return new xdat_element_access_secure_ip();
                }
                if(name=="security:stored_search"){
                    if(window.xdat_stored_search==undefined)dynamicJSLoad('xdat_stored_search','generated/xdat_stored_search.js');
                    return new xdat_stored_search();
                }
                if(name=="security:primary_security_field"){
                    if(window.xdat_primary_security_field==undefined)dynamicJSLoad('xdat_primary_security_field','generated/xdat_primary_security_field.js');
                    return new xdat_primary_security_field();
                }
                if(name=="security:change_info"){
                    if(window.xdat_change_info==undefined)dynamicJSLoad('xdat_change_info','generated/xdat_change_info.js');
                    return new xdat_change_info();
                }
            }

            if (['wrk','workflow'].indexOf(prefix) >=0) {
                if (name == "wrk:workflowData") {
                    if (window.wrk_workflowData == undefined) dynamicJSLoad('wrk_workflowData', 'generated/wrk_workflowData.js');
                    return new wrk_workflowData();
                }
                if (name == "wrk:abstractExecutionEnvironment") {
                    if (window.wrk_abstractExecutionEnvironment == undefined) dynamicJSLoad('wrk_abstractExecutionEnvironment', 'generated/wrk_abstractExecutionEnvironment.js');
                    return new wrk_abstractExecutionEnvironment();
                }
                if (name == "wrk:xnatExecutionEnvironment") {
                    if (window.wrk_xnatExecutionEnvironment == undefined) dynamicJSLoad('wrk_xnatExecutionEnvironment', 'generated/wrk_xnatExecutionEnvironment.js');
                    return new wrk_xnatExecutionEnvironment();
                }
                if (name == "wrk:xnatExecutionEnvironment_parameter") {
                    if (window.wrk_xnatExecutionEnvironment_parameter == undefined) dynamicJSLoad('wrk_xnatExecutionEnvironment_parameter', 'generated/wrk_xnatExecutionEnvironment_parameter.js');
                    return new wrk_xnatExecutionEnvironment_parameter();
                }
                if (name == "wrk:xnatExecutionEnvironment_notify") {
                    if (window.wrk_xnatExecutionEnvironment_notify == undefined) dynamicJSLoad('wrk_xnatExecutionEnvironment_notify', 'generated/wrk_xnatExecutionEnvironment_notify.js');
                    return new wrk_xnatExecutionEnvironment_notify();
                }
                if(name=="wrk:Workflow"){
                    if(window.wrk_workflowData==undefined)dynamicJSLoad('wrk_workflowData','generated/wrk_workflowData.js');
                    return new wrk_workflowData();
                }
                if(name=="workflow:Workflow"){
                    if(window.wrk_workflowData==undefined)dynamicJSLoad('wrk_workflowData','generated/wrk_workflowData.js');
                    return new wrk_workflowData();
                }
                if(name=="workflow:xnatExecutionEnvironment"){
                    if(window.wrk_xnatExecutionEnvironment==undefined)dynamicJSLoad('wrk_xnatExecutionEnvironment','generated/wrk_xnatExecutionEnvironment.js');
                    return new wrk_xnatExecutionEnvironment();
                }
                if(name=="workflow:xnatExecutionEnvironment_notify"){
                    if(window.wrk_xnatExecutionEnvironment_notify==undefined)dynamicJSLoad('wrk_xnatExecutionEnvironment_notify','generated/wrk_xnatExecutionEnvironment_notify.js');
                    return new wrk_xnatExecutionEnvironment_notify();
                }
                if(name=="workflow:workflowData"){
                    if(window.wrk_workflowData==undefined)dynamicJSLoad('wrk_workflowData','generated/wrk_workflowData.js');
                    return new wrk_workflowData();
                }
                if(name=="workflow:xnatExecutionEnvironment_parameter"){
                    if(window.wrk_xnatExecutionEnvironment_parameter==undefined)dynamicJSLoad('wrk_xnatExecutionEnvironment_parameter','generated/wrk_xnatExecutionEnvironment_parameter.js');
                    return new wrk_xnatExecutionEnvironment_parameter();
                }
                if(name=="workflow:abstractExecutionEnvironment"){
                    if(window.wrk_abstractExecutionEnvironment==undefined)dynamicJSLoad('wrk_abstractExecutionEnvironment','generated/wrk_abstractExecutionEnvironment.js');
                    return new wrk_abstractExecutionEnvironment();
                }
            }

            if (prefix === 'xdat') {
                if (name == "xdat:change_info") {
                    if (window.xdat_change_info == undefined) dynamicJSLoad('xdat_change_info', 'generated/xdat_change_info.js');
                    return new xdat_change_info();
                }
                if (name == "xdat:field_mapping") {
                    if (window.xdat_field_mapping == undefined) dynamicJSLoad('xdat_field_mapping', 'generated/xdat_field_mapping.js');
                    return new xdat_field_mapping();
                }
                if (name == "xdat:userGroup") {
                    if (window.xdat_userGroup == undefined) dynamicJSLoad('xdat_userGroup', 'generated/xdat_userGroup.js');
                    return new xdat_userGroup();
                }
                if (name == "xdat:stored_search_groupID") {
                    if (window.xdat_stored_search_groupID == undefined) dynamicJSLoad('xdat_stored_search_groupID', 'generated/xdat_stored_search_groupID.js');
                    return new xdat_stored_search_groupID();
                }
                if (name == "xdat:stored_search_allowed_user") {
                    if (window.xdat_stored_search_allowed_user == undefined) dynamicJSLoad('xdat_stored_search_allowed_user', 'generated/xdat_stored_search_allowed_user.js');
                    return new xdat_stored_search_allowed_user();
                }
                if (name == "xdat:Search") {
                    if (window.xdat_Search == undefined) dynamicJSLoad('xdat_Search', 'generated/xdat_Search.js');
                    return new xdat_Search();
                }
                if (name == "xdat:Info") {
                    if (window.xdat_infoEntry == undefined) dynamicJSLoad('xdat_infoEntry', 'generated/xdat_infoEntry.js');
                    return new xdat_infoEntry();
                }
                if (name == "xdat:user") {
                    if (window.xdat_user == undefined) dynamicJSLoad('xdat_user', 'generated/xdat_user.js');
                    return new xdat_user();
                }
                if (name == "xdat:infoEntry") {
                    if (window.xdat_infoEntry == undefined) dynamicJSLoad('xdat_infoEntry', 'generated/xdat_infoEntry.js');
                    return new xdat_infoEntry();
                }
                if (name == "xdat:role_type") {
                    if (window.xdat_role_type == undefined) dynamicJSLoad('xdat_role_type', 'generated/xdat_role_type.js');
                    return new xdat_role_type();
                }
                if (name == "xdat:stored_search") {
                    if (window.xdat_stored_search == undefined) dynamicJSLoad('xdat_stored_search', 'generated/xdat_stored_search.js');
                    return new xdat_stored_search();
                }
                if (name == "xdat:access_log") {
                    if (window.xdat_access_log == undefined) dynamicJSLoad('xdat_access_log', 'generated/xdat_access_log.js');
                    return new xdat_access_log();
                }
                if (name == "xdat:UserGroup") {
                    if (window.xdat_userGroup == undefined) dynamicJSLoad('xdat_userGroup', 'generated/xdat_userGroup.js');
                    return new xdat_userGroup();
                }
                if (name == "xdat:primary_security_field") {
                    if (window.xdat_primary_security_field == undefined) dynamicJSLoad('xdat_primary_security_field', 'generated/xdat_primary_security_field.js');
                    return new xdat_primary_security_field();
                }
                if (name == "xdat:field_mapping_set") {
                    if (window.xdat_field_mapping_set == undefined) dynamicJSLoad('xdat_field_mapping_set', 'generated/xdat_field_mapping_set.js');
                    return new xdat_field_mapping_set();
                }
                if (name == "xdat:element_access_secure_ip") {
                    if (window.xdat_element_access_secure_ip == undefined) dynamicJSLoad('xdat_element_access_secure_ip', 'generated/xdat_element_access_secure_ip.js');
                    return new xdat_element_access_secure_ip();
                }
                if (name == "xdat:element_action_type") {
                    if (window.xdat_element_action_type == undefined) dynamicJSLoad('xdat_element_action_type', 'generated/xdat_element_action_type.js');
                    return new xdat_element_action_type();
                }
                if (name == "xdat:user_groupID") {
                    if (window.xdat_user_groupID == undefined) dynamicJSLoad('xdat_user_groupID', 'generated/xdat_user_groupID.js');
                    return new xdat_user_groupID();
                }
                if (name == "xdat:criteria") {
                    if (window.xdat_criteria == undefined) dynamicJSLoad('xdat_criteria', 'generated/xdat_criteria.js');
                    return new xdat_criteria();
                }
                if(name=="xdat:XDATUser"){
                    if(window.xdat_user==undefined)dynamicJSLoad('xdat_user','generated/xdat_user.js');
                    return new xdat_user();
                }
                if(name=="xdat:bundle"){
                    if(window.xdat_stored_search==undefined)dynamicJSLoad('xdat_stored_search','generated/xdat_stored_search.js');
                    return new xdat_stored_search();
                }
                if(name=="xdat:element_security_listing_action"){
                    if(window.xdat_element_security_listing_action==undefined)dynamicJSLoad('xdat_element_security_listing_action','generated/xdat_element_security_listing_action.js');
                    return new xdat_element_security_listing_action();
                }
                if(name=="xdat:newsEntry"){
                    if(window.xdat_newsEntry==undefined)dynamicJSLoad('xdat_newsEntry','generated/xdat_newsEntry.js');
                    return new xdat_newsEntry();
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
                if(name=="xdat:criteria_set"){
                    if(window.xdat_criteria_set==undefined)dynamicJSLoad('xdat_criteria_set','generated/xdat_criteria_set.js');
                    return new xdat_criteria_set();
                }
                if(name=="xdat:action_type"){
                    if(window.xdat_action_type==undefined)dynamicJSLoad('xdat_action_type','generated/xdat_action_type.js');
                    return new xdat_action_type();
                }
                if(name=="xdat:user_login"){
                    if(window.xdat_user_login==undefined)dynamicJSLoad('xdat_user_login','generated/xdat_user_login.js');
                    return new xdat_user_login();
                }
                if(name=="xdat:security"){
                    if(window.xdat_security==undefined)dynamicJSLoad('xdat_security','generated/xdat_security.js');
                    return new xdat_security();
                }
                if(name=="xdat:search_field"){
                    if(window.xdat_search_field==undefined)dynamicJSLoad('xdat_search_field','generated/xdat_search_field.js');
                    return new xdat_search_field();
                }
            }

        }
        
    }
}
