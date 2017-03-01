INSERT INTO xdat_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.189',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)
INSERT INTO xdat_security (xdat_security_id,require_login,"system",security_info) VALUES (1,1,'XNAT',1)
INSERT INTO xdat_userGroup_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.213',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)
INSERT INTO xdat_userGroup (xdat_usergroup_id,groups_group_xdat_security_xdat_security_id,id,displayname,usergroup_info) VALUES (1,1,'ALL_DATA_ACCESS','All Data Access',1)
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.233',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,1,'xnat:projectData',1)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.248',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (1,1,1,'AND')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.265',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',1,0,1,1,0,1,'xnat:projectData/ID',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.297',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,2,'xnat:petSessionData',2)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.313',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (2,2,2,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.324',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',2,0,1,2,0,2,'xnat:petSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.330',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,3)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',2,0,1,3,0,3,'xnat:petSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.362',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,3)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,3,'xnat:ctSessionData',3)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.379',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,3)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (3,3,3,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.391',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,4)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',3,0,1,4,0,4,'xnat:ctSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.396',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',3,0,1,5,0,5,'xnat:ctSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.426',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,4)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,4,'xnat:petmrSessionData',4)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.438',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,4)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (4,4,4,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.446',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,6)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',4,0,1,6,0,6,'xnat:petmrSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.449',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,7)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',4,0,1,7,0,7,'xnat:petmrSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.476',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,5,'xnat:crSessionData',5)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.490',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (5,5,5,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.499',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,8)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',5,0,1,8,0,8,'xnat:crSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.503',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,9)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',5,0,1,9,0,9,'xnat:crSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.921',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,31)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,31,'xnat:mrSessionData',31)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.928',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,31)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (31,31,31,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.934',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,60)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',31,0,1,60,0,60,'xnat:mrSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.937',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,61)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',31,0,1,61,0,61,'xnat:mrSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.954',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,32)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,32,'xnat:subjectData',32)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.964',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,32)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (32,32,32,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.970',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,62)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',32,0,1,62,0,62,'xnat:subjectData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.973',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,63)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',32,0,1,63,0,63,'xnat:subjectData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.992',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,33)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,33,'xnat:qcAssessmentData',33)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.000',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,33)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (33,33,33,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.004',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,64)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',33,0,1,64,0,64,'xnat:qcAssessmentData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.007',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,65)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',33,0,1,65,0,65,'xnat:qcAssessmentData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.021',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,34)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,34,'xnat:qcManualAssessorData',34)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.030',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,34)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (34,34,34,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.054',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,66)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',34,0,1,66,0,66,'xnat:qcManualAssessorData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.057',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,67)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',34,0,1,67,0,67,'xnat:qcManualAssessorData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.071',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,35)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,35,'val:protocolData',35)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.077',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,35)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (35,35,35,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.081',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,68)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',35,0,1,68,0,68,'val:protocolData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.084',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,69)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',35,0,1,69,0,69,'val:protocolData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.103',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,36)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,36,'xnat:pVisitData',36)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.111',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,36)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (36,36,36,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.117',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,70)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',36,0,1,70,0,70,'xnat:pVisitData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.120',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,71)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',36,0,1,71,0,71,'xnat:pVisitData/project',0,1,'*')
INSERT INTO xdat_userGroup_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.136',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)
INSERT INTO xdat_userGroup (xdat_usergroup_id,groups_group_xdat_security_xdat_security_id,id,displayname,usergroup_info) VALUES (2,1,'ALL_DATA_ADMIN','All Data Administration',2)
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.143',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,37)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,37,'xnat:projectData',37)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.151',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,37)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (37,37,37,'AND')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.157',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,72)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',37,1,1,72,1,72,'xnat:projectData/ID',1,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.173',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,38)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,38,'xnat:petSessionData',38)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.179',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,38)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (38,38,38,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.184',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,73)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',38,1,1,73,1,73,'xnat:petSessionData/sharing/share/project',1,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.187',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,74)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',38,1,1,74,1,74,'xnat:petSessionData/project',1,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.201',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,39)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,39,'xnat:ctSessionData',39)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.208',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,39)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (39,39,39,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.213',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,75)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',39,1,1,75,1,75,'xnat:ctSessionData/sharing/share/project',1,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.215',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,76)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',39,1,1,76,1,76,'xnat:ctSessionData/project',1,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.230',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,40)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,40,'xnat:petmrSessionData',40)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.236',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,40)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (40,40,40,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.241',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,77)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',40,1,1,77,1,77,'xnat:petmrSessionData/sharing/share/project',1,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.244',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,78)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',40,1,1,78,1,78,'xnat:petmrSessionData/project',1,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.263',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,41)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,41,'xnat:crSessionData',41)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.270',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,41)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (41,41,41,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.275',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,79)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',41,1,1,79,1,79,'xnat:crSessionData/sharing/share/project',1,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.277',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,80)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',41,1,1,80,1,80,'xnat:crSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.220',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,67)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,67,'xnat:mrSessionData',67)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.231',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,67)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (67,67,67,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.247',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,131)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',67,1,1,131,1,131,'xnat:mrSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.249',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,132)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',67,1,1,132,1,132,'xnat:mrSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.266',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,68)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,68,'xnat:subjectData',68)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.272',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,68)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (68,68,68,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.278',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,133)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',68,1,1,133,1,133,'xnat:subjectData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.281',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,134)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',68,1,1,134,1,134,'xnat:subjectData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.303',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,69)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,69,'xnat:qcAssessmentData',69)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.313',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,69)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (69,69,69,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.319',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,135)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',69,1,1,135,1,135,'xnat:qcAssessmentData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.322',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,136)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',69,1,1,136,1,136,'xnat:qcAssessmentData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.339',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,70)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,70,'xnat:qcManualAssessorData',70)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.346',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,70)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (70,70,70,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.352',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,137)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',70,1,1,137,1,137,'xnat:qcManualAssessorData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.354',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,138)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',70,1,1,138,1,138,'xnat:qcManualAssessorData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.371',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,71)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,71,'val:protocolData',71)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.389',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,71)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (71,71,71,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.394',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,139)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',71,1,1,139,1,139,'val:protocolData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.397',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,140)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',71,1,1,140,1,140,'val:protocolData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.415',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,72)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,72,'xnat:pVisitData',72)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.422',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,72)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (72,72,72,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.430',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,141)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',72,1,1,141,1,141,'xnat:pVisitData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.433',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,142)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',72,1,1,142,1,142,'xnat:pVisitData/project',1,1,'*')

INSERT INTO xdat_user_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.466',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)

INSERT INTO xdat_user (primary_password_encrypt,xdat_user_id,users_user_xdat_security_xdat_security_id,verified,firstname,lastname,enabled,email,login,user_info,primary_password) VALUES (1,1,1,1,'Admin','Admin',1,'${siteConfig.adminEmail}','admin',1,'admin')

INSERT INTO xdat_role_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.483',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)

INSERT INTO xdat_role_type (role_type_info,role_name,description,sequence) VALUES (1,'Administrator','can change user permissions',1)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.490',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Administration',1,'admin',1)

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('Administrator','admin')

INSERT INTO xdat_role_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.504',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)

INSERT INTO xdat_role_type (sequence,role_type_info,description,role_name) VALUES (1,2,'can insert experiments and subjects','DataManager')

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.508',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Upload XML',2,'XMLUpload',7)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.515',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,4)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('View Prearchive',4,'prearchives',11)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.518',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Add Project',5,'add_xnat_projectData',7)

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('DataManager','XMLUpload')

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('DataManager','prearchives')

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('DataManager','add_xnat_projectData')

INSERT INTO xdat_role_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.528',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,3)

INSERT INTO xdat_role_type (sequence,role_type_info,description,role_name) VALUES (1,3,'views webpages','SiteUser')

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.531',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,6)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Browse',6,'browse',4)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.534',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,7)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Super Search',7,'super_search',2)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.537',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,8)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Search',8,'search',1)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.539',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,9)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('My XNAT',9,'MyXNAT',5)

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('SiteUser','browse')

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('SiteUser','super_search')

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('SiteUser','search')

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('SiteUser','MyXNAT')

INSERT INTO xdat_r_xdat_role_type_assign_xdat_user (xdat_user_xdat_user_id,xdat_role_type_role_name) VALUES (1,'Administrator')

INSERT INTO xdat_r_xdat_role_type_assign_xdat_user (xdat_user_xdat_user_id,xdat_role_type_role_name) VALUES (1,'DataManager')

INSERT INTO xdat_r_xdat_role_type_assign_xdat_user (xdat_user_xdat_user_id,xdat_role_type_role_name) VALUES (1,'SiteUser')

INSERT INTO xdat_user_groupID_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.559',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)

INSERT INTO xdat_user_groupID (user_groupid_info,groupid,xdat_user_groupid_id,groups_groupid_xdat_user_xdat_user_id) VALUES (1,'ALL_DATA_ADMIN',1,1)

INSERT INTO xdat_user_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.570',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)

INSERT INTO xdat_user (primary_password_encrypt,xdat_user_id,users_user_xdat_security_xdat_security_id,verified,firstname,lastname,enabled,email,login,user_info,primary_password) VALUES (0,2,1,1,'XNAT','Guest',1,'${siteConfig.adminEmail}','guest',2,'guest')

INSERT INTO xdat_r_xdat_role_type_assign_xdat_user (xdat_user_xdat_user_id,xdat_role_type_role_name) VALUES (2,'SiteUser')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.607',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,element_name,secure,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,0,1,0,'xdat:element_security',0,1,0,1,0,1,1,1,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.624',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (1,'xdat:element_security','View XML','xml','r.gif',1,2)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.634',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','edit','edit','e.gif',0,2,'xdat:element_security',2,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.640',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,3)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('delete','delete','delete.gif',4,3,'xdat:element_security',3,'Delete')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.650',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,element_name,secure,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,0,1,0,'xdat:role_type',0,1,0,1,0,2,1,1,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.657',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,4)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (4,'xdat:role_type','View XML','xml','r.gif',4,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.662',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','edit','edit','e.gif',0,5,'xdat:role_type',5,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.668',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,6)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('delete','delete','delete.gif',4,6,'xdat:role_type',6,'Delete')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.677',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,3)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,element_name,secure,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,0,0,0,'xdat:stored_search',0,1,0,1,0,3,1,1,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.685',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,7)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('edit','edit','e.gif',0,7,'xdat:stored_search',7,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.691',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,8)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (8,'xdat:stored_search','View XML','xml','r.gif',8,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.697',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,9)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (9,'xdat:stored_search','Download XML','xml_file','save.gif',9,2)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.703',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,10)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('always','email_report','right2.gif',3,10,'xdat:stored_search',10,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.709',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,11)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('delete','delete','delete.gif',4,11,'xdat:stored_search',11,'Delete')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.719',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,4)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,element_name,secure,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,0,1,0,'xdat:user',0,1,0,1,0,4,1,1,0)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.727',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,12)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,xdat_element_action_type_id,sequence) VALUES (12,'xdat:user','Authorize','activate',12,0)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.733',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,13)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (13,'xdat:user','View XML','xml','r.gif',13,4)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.739',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,14)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',2,14,'xdat:user',14,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.746',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,15)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,xdat_element_action_type_id,sequence) VALUES (15,'xdat:user','Change Permissions','user_admin',15,3)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.752',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,16)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,xdat_element_action_type_id,sequence) VALUES (16,'xdat:user','Enable/Disable','enable',16,1)

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.766',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,element_name,secure,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,0,0,0,'xdat:userGroup',0,1,0,1,0,5,1,1,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.775',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,17)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('edit','edit','e.gif',0,17,'xdat:userGroup',17,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.782',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,18)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (18,'xdat:userGroup','View XML','xml','r.gif',18,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.789',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,19)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (19,'xdat:userGroup','Download XML','xml_file','save.gif',19,2)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.795',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,20)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',3,20,'xdat:userGroup',20,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.801',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,21)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('delete','delete','delete.gif',4,21,'xdat:userGroup',21,'Delete')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.810',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,6)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,0,0,0,'News','xdat:newsEntry',0,'News',1,0,1,0,6,1,1,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.819',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,22)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('edit','edit','e.gif',0,22,'xdat:newsEntry',22,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.825',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,23)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (23,'xdat:newsEntry','View XML','xml','r.gif',23,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.833',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,24)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (24,'xdat:newsEntry','Download XML','xml_file','save.gif',24,2)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.840',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,25)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('always','email_report','right2.gif',3,25,'xdat:newsEntry',25,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.847',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,26)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('delete','delete','delete.gif',4,26,'xdat:newsEntry',26,'Delete')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.858',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,7)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,0,0,0,'Info','xdat:infoEntry',0,'Info',1,0,1,0,7,1,1,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.867',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,27)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('edit','edit','e.gif',0,27,'xdat:infoEntry',27,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.875',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,28)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (28,'xdat:infoEntry','View XML','xml','r.gif',28,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.882',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,29)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (29,'xdat:infoEntry','Download XML','xml_file','save.gif',29,2)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.890',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,30)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('always','email_report','right2.gif',3,30,'xdat:infoEntry',30,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.897',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,31)

INSERT INTO xdat_element_action_type (element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('delete','delete','delete.gif',4,31,'xdat:infoEntry',31,'Delete')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.909',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,8)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,element_name,secure,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,0,1,0,'xnat:investigatorData',0,1,0,1,0,8,1,1,0)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.919',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,32)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (32,'xnat:investigatorData','View XML','xml','r.gif',32,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.929',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,33)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','edit','edit','e.gif',0,33,'xnat:investigatorData',33,'Edit')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.941',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,9)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'MR',0,1,1,1,0,'MR Session','xnat:mrSessionData',1,'MR Sessions',1,1,1,1,9,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.951',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:mrSessionData/sharing/share/project','xnat:mrSessionData',1,1)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.962',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:mrSessionData/project','xnat:mrSessionData',2,2)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.973',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,34)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,34,'xnat:mrSessionData',34,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.988',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,35)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,35,'xnat:mrSessionData',35,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.993',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,36)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.998',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,37)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,37,'xnat:mrSessionData',37,'Upload Scans')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.004',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,38)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_upload_xnat_imageSessionData','Upload','edit','Up.gif',5,38,'xnat:mrSessionData',38,'Tagged Upload')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.011',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,39)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,39,'xnat:mrSessionData',39,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.018',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,40)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,40,'xnat:mrSessionData',40,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.025',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,41)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,41,'xnat:mrSessionData',41,'Download Images')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.037',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,10)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,1,0,0,'Subject','xnat:subjectData',1,'Subjects',1,1,1,1,10,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.043',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,3)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:subjectData/sharing/share/project','xnat:subjectData',3,3)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.049',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,4)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:subjectData/project','xnat:subjectData',4,4)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.062',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,42)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (42,'xnat:subjectData','View XML','xml','r.gif',42,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.069',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,43)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,43,'xnat:subjectData',43,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.076',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,44)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence,secureaccess) VALUES (44,'xnat:subjectData','Add Experiment','add_experiment','update.gif',44,2,'edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.091',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,46)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (46,'xnat:subjectData','Download XML','xml_file','save.gif',46,4)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.098',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,47)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',5,47,'xnat:subjectData',47,'Email')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.110',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,11)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'CT',0,1,1,2,0,'CT Session','xnat:ctSessionData',1,'CT Sessions',1,1,1,1,11,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.116',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:ctSessionData/sharing/share/project','xnat:ctSessionData',5,5)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.121',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,6)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:ctSessionData/project','xnat:ctSessionData',6,6)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.130',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,48)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,48,'xnat:ctSessionData',48,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.136',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,49)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,49,'xnat:ctSessionData',49,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.142',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,50)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.149',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,51)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,51,'xnat:ctSessionData',51,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.154',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,52)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,52,'xnat:ctSessionData',52,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.160',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,53)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,53,'xnat:ctSessionData',53,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.165',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,54)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,54,'xnat:ctSessionData',54,'Upload Scans')
 
INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.174',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,12)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'PETMR',0,1,1,2,0,'PET MR Session','xnat:petmrSessionData',1,'PET MR Sessions',1,1,1,1,12,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.179',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,7)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:petmrSessionData/sharing/share/project','xnat:petmrSessionData',7,7)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.184',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,8)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:petmrSessionData/project','xnat:petmrSessionData',8,8)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.192',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,55)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,55,'xnat:petmrSessionData',55,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.197',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,56)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,56,'xnat:petmrSessionData',56,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.203',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,57)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.208',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,58)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,58,'xnat:petmrSessionData',58,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.211',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,59)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,59,'xnat:petmrSessionData',59,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.215',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,60)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,60,'xnat:petmrSessionData',60,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.219',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,61)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,61,'xnat:petmrSessionData',61,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.226',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,13)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'CR',0,1,1,2,0,'CR Session','xnat:crSessionData',1,'CR Sessions',1,1,1,1,13,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.230',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,9)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:crSessionData/sharing/share/project','xnat:crSessionData',9,9)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.235',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,10)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:crSessionData/project','xnat:crSessionData',10,10)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.241',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,62)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,62,'xnat:crSessionData',62,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.245',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,63)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,63,'xnat:crSessionData',63,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.249',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,64)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.253',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,65)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,65,'xnat:crSessionData',65,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.257',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,66)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,66,'xnat:crSessionData',66,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.261',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,67)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,67,'xnat:crSessionData',67,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.267',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,68)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,68,'xnat:crSessionData',68,'Upload Scans')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.299',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,71)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.360',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,78)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.425',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,85)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.482',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,92)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.539',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,99)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.592',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,106)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.644',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,113)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.704',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,120)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.767',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,127)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.821',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,134)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.873',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,141)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.936',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,148)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.996',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,155)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.061',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,162)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.122',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,169)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.188',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,176)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.268',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,183)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.334',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,190)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.406',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,197)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.475',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,204)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.546',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,211)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.610',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,218)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.676',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,225)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.732',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,232)

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.825',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,39)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'PET',0,1,1,2,0,'PET Session','xnat:petSessionData',1,'PET Sessions',1,1,1,1,39,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.829',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,61)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:petSessionData/sharing/share/project','xnat:petSessionData',61,61)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.833',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,62)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:petSessionData/project','xnat:petSessionData',62,62)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.841',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,244)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,244,'xnat:petSessionData',244,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.846',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,245)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,245,'xnat:petSessionData',245,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.851',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,246)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.856',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,247)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,247,'xnat:petSessionData',247,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.861',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,248)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,248,'xnat:petSessionData',248,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.867',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,249)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,249,'xnat:petSessionData',249,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.872',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,250)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,250,'xnat:petSessionData',250,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.879',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,40)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'QC',0,1,1,3,0,'Auto QC','xnat:qcAssessmentData',1,'Auto QCs',1,1,1,1,40,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.883',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,63)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:qcAssessmentData/sharing/share/project','xnat:qcAssessmentData',63,63)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.887',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,64)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:qcAssessmentData/project','xnat:qcAssessmentData',64,64)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.895',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,251)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,251,'xnat:qcAssessmentData',251,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.900',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,252)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,252,'xnat:qcAssessmentData',252,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.906',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,253)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,253,'xnat:qcAssessmentData',253,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.911',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,254)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','delete','delete','delete.gif',5,254,'xnat:qcAssessmentData',254,'Delete')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.918',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,41)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'MQC',0,1,1,3,0,'Manual QC','xnat:qcManualAssessorData',1,'Manual QCs',1,1,1,1,41,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.922',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,65)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:qcManualAssessorData/sharing/share/project','xnat:qcManualAssessorData',65,65)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.926',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,66)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:qcManualAssessorData/project','xnat:qcManualAssessorData',66,66)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.934',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,255)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,255,'xnat:qcManualAssessorData',255,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.940',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,256)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,256,'xnat:qcManualAssessorData',256,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.948',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,257)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,257,'xnat:qcManualAssessorData',257,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.953',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,258)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,258,'xnat:qcManualAssessorData',258,'Edit')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.960',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,42)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'PVAL',0,1,1,3,0,'Protocol Validation','val:protocolData',1,'Protocol Validations',1,1,1,1,42,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.964',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,67)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('val:protocolData/sharing/share/project','val:protocolData',67,67)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.970',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,68)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('val:protocolData/project','val:protocolData',68,68)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.979',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,259)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,259,'val:protocolData',259,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.985',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,260)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,260,'val:protocolData',260,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.990',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,261)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,261,'val:protocolData',261,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.995',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,262)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','delete','delete','delete.gif',5,262,'val:protocolData',262,'Delete')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.003',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,43)

INSERT INTO xdat_element_security (secure_ip,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,0,1,1,0,0,'Project','xnat:projectData',1,'Projects',1,0,1,0,43,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.007',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,69)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:projectData/ID','xnat:projectData',69,69)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.015',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,263)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (263,'xnat:projectData','View XML','xml','r.gif',263,1)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.020',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,264)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,264,'xnat:projectData',264,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.025',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,265)

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (265,'xnat:projectData','Download XML','xml_file','save.gif',265,3)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.032',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,266)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',4,266,'xnat:projectData',266,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.037',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,267)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','delete','delete','delete.gif',5,267,'xnat:projectData',267,'Delete')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.045',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,44)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'V',0,1,1,3,0,'Visit','xnat:pVisitData',1,'Visits',1,1,1,1,44,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.049',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,70)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:pVisitData/sharing/share/project','xnat:pVisitData',70,70)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.053',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,71)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:pVisitData/project','xnat:pVisitData',71,71)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.062',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,268)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,268,'xnat:pVisitData',268,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.067',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,269)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,269,'xnat:pVisitData',269,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.072',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,270)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,270,'xnat:pVisitData',270,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:58.078',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,271)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,271,'xnat:pVisitData',271,'Email')

UPDATE xdat_security_meta_data SET last_modified='2013-09-25 18:31:58.091', modified=1 WHERE meta_data_id=1 

INSERT INTO xdat_change_info (xdat_change_info_id,change_date) VALUES (1,'2013-09-25 18:31:52.129')
