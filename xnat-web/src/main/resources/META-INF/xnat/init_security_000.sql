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
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,4,'xnat:usSessionData',4)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.438',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,4)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (4,4,4,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.446',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,6)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',4,0,1,6,0,6,'xnat:usSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.449',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,7)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',4,0,1,7,0,7,'xnat:usSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.476',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,5,'xnat:crSessionData',5)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.490',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (5,5,5,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.499',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,8)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',5,0,1,8,0,8,'xnat:crSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.503',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,9)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',5,0,1,9,0,9,'xnat:crSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.539',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,6)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,6,'xnat:epsSessionData',6)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.553',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,6)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (6,6,6,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.561',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,10)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',6,0,1,10,0,10,'xnat:epsSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.566',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,11)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',6,0,1,11,0,11,'xnat:epsSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.600',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,7)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,7,'xnat:hdSessionData',7)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.613',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,7)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (7,7,7,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.624',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,12)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',7,0,1,12,0,12,'xnat:hdSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.628',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,13)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',7,0,1,13,0,13,'xnat:hdSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.659',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,8)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,8,'xnat:ecgSessionData',8)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.676',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,8)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (8,8,8,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.688',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,14)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',8,0,1,14,0,14,'xnat:ecgSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.693',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,15)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',8,0,1,15,0,15,'xnat:ecgSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.730',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,9)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,9,'xnat:ioSessionData',9)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.748',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,9)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (9,9,9,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.760',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,16)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',9,0,1,16,0,16,'xnat:ioSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.765',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,17)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',9,0,1,17,0,17,'xnat:ioSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.800',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,10)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,10,'xnat:mgSessionData',10)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.812',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,10)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (10,10,10,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.820',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,18)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',10,0,1,18,0,18,'xnat:mgSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.824',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,19)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',10,0,1,19,0,19,'xnat:mgSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.855',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,11)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,11,'xnat:dxSessionData',11)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.867',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,11)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (11,11,11,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.875',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,20)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',11,0,1,20,0,20,'xnat:dxSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.879',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,21)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',11,0,1,21,0,21,'xnat:dxSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.906',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,12)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,12,'xnat:nmSessionData',12)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.923',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,12)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (12,12,12,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.931',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,22)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',12,0,1,22,0,22,'xnat:nmSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.935',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,23)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',12,0,1,23,0,23,'xnat:nmSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.963',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,13)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,13,'xnat:srSessionData',13)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.977',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,13)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (13,13,13,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.988',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,24)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',13,0,1,24,0,24,'xnat:srSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:52.992',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,25)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',13,0,1,25,0,25,'xnat:srSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.019',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,14)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,14,'xnat:gmvSessionData',14)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.032',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,14)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (14,14,14,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.044',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,26)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',14,0,1,26,0,26,'xnat:gmvSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.049',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,27)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',14,0,1,27,0,27,'xnat:gmvSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.086',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,15)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,15,'xnat:gmSessionData',15)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.103',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,15)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (15,15,15,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.114',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,28)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',15,0,1,28,0,28,'xnat:gmSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.119',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,29)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',15,0,1,29,0,29,'xnat:gmSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.156',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,16)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,16,'xnat:esvSessionData',16)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.172',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,16)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (16,16,16,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.184',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,30)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',16,0,1,30,0,30,'xnat:esvSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.189',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,31)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',16,0,1,31,0,31,'xnat:esvSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.227',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,17)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,17,'xnat:esSessionData',17)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.241',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,17)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (17,17,17,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.252',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,32)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',17,0,1,32,0,32,'xnat:esSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.257',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,33)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',17,0,1,33,0,33,'xnat:esSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.284',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,18)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,18,'xnat:dx3DCraniofacialSessionData',18)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.296',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,18)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (18,18,18,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.304',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,34)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',18,0,1,34,0,34,'xnat:dx3DCraniofacialSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.309',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,35)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',18,0,1,35,0,35,'xnat:dx3DCraniofacialSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.335',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,19)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,19,'xnat:xa3DSessionData',19)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.348',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,19)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (19,19,19,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.356',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,36)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',19,0,1,36,0,36,'xnat:xa3DSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.360',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,37)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',19,0,1,37,0,37,'xnat:xa3DSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.389',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,20)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,20,'xnat:rfSessionData',20)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.401',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,20)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (20,20,20,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.412',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,38)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',20,0,1,38,0,38,'xnat:rfSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.417',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,39)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',20,0,1,39,0,39,'xnat:rfSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.453',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,21)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,21,'xnat:xaSessionData',21)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.465',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,21)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (21,21,21,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.474',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,40)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',21,0,1,40,0,40,'xnat:xaSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.479',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,41)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',21,0,1,41,0,41,'xnat:xaSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.508',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,22)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,22,'xnat:smSessionData',22)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.520',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,22)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (22,22,22,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.531',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,42)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',22,0,1,42,0,42,'xnat:smSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.535',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,43)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',22,0,1,43,0,43,'xnat:smSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.589',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,23)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,23,'xnat:xcSessionData',23)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.601',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,23)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (23,23,23,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.611',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,44)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',23,0,1,44,0,44,'xnat:xcSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.615',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,45)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',23,0,1,45,0,45,'xnat:xcSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.636',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,24)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,24,'xnat:xcvSessionData',24)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.648',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,24)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (24,24,24,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.658',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,46)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',24,0,1,46,0,46,'xnat:xcvSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.662',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,47)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',24,0,1,47,0,47,'xnat:xcvSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.682',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,25)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,25,'xnat:opSessionData',25)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.690',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,25)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (25,25,25,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.696',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,48)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',25,0,1,48,0,48,'xnat:opSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.699',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,49)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',25,0,1,49,0,49,'xnat:opSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.717',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,26)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,26,'xnat:optSessionData',26)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.725',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,26)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (26,26,26,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.732',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,50)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',26,0,1,50,0,50,'xnat:optSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.735',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,51)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',26,0,1,51,0,51,'xnat:optSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.752',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,27)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,27,'xnat:rtSessionData',27)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.763',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,27)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (27,27,27,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.771',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,52)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',27,0,1,52,0,52,'xnat:rtSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.776',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,53)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',27,0,1,53,0,53,'xnat:rtSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.799',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,28)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,28,'xnat:megSessionData',28)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.809',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,28)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (28,28,28,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.817',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,54)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',28,0,1,54,0,54,'xnat:megSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.821',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,55)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',28,0,1,55,0,55,'xnat:megSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.846',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,29)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,29,'xnat:eegSessionData',29)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.857',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,29)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (29,29,29,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.863',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,56)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',29,0,1,56,0,56,'xnat:eegSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.866',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,57)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',29,0,1,57,0,57,'xnat:eegSessionData/project',0,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.884',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,30)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (1,30,'xnat:otherDicomSessionData',30)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.891',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,30)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (30,30,30,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.897',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,58)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',30,0,1,58,0,58,'xnat:otherDicomSessionData/sharing/share/project',0,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:53.900',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,59)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',30,0,1,59,0,59,'xnat:otherDicomSessionData/project',0,1,'*')
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
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,40,'xnat:usSessionData',40)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.236',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,40)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (40,40,40,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.241',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,77)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',40,1,1,77,1,77,'xnat:usSessionData/sharing/share/project',1,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.244',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,78)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',40,1,1,78,1,78,'xnat:usSessionData/project',1,1,'*')
INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.263',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,41)
INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,41,'xnat:crSessionData',41)
INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.270',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,41)
INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (41,41,41,'OR')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.275',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,79)
INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',41,1,1,79,1,79,'xnat:crSessionData/sharing/share/project',1,1,'*')
INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.277',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,80)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',41,1,1,80,1,80,'xnat:crSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.292',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,42)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,42,'xnat:epsSessionData',42)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.299',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,42)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (42,42,42,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.306',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,81)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',42,1,1,81,1,81,'xnat:epsSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.309',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,82)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',42,1,1,82,1,82,'xnat:epsSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.326',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,43)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,43,'xnat:hdSessionData',43)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.333',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,43)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (43,43,43,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.338',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,83)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',43,1,1,83,1,83,'xnat:hdSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.342',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,84)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',43,1,1,84,1,84,'xnat:hdSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.359',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,44)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,44,'xnat:ecgSessionData',44)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.368',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,44)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (44,44,44,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.374',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,85)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',44,1,1,85,1,85,'xnat:ecgSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.377',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,86)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',44,1,1,86,1,86,'xnat:ecgSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.398',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,45)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,45,'xnat:ioSessionData',45)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.406',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,45)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (45,45,45,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.411',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,87)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',45,1,1,87,1,87,'xnat:ioSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.414',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,88)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',45,1,1,88,1,88,'xnat:ioSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.435',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,46)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,46,'xnat:mgSessionData',46)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.446',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,46)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (46,46,46,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.454',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,89)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',46,1,1,89,1,89,'xnat:mgSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.459',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,90)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',46,1,1,90,1,90,'xnat:mgSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.482',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,47)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,47,'xnat:dxSessionData',47)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.494',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,47)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (47,47,47,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.505',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,91)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',47,1,1,91,1,91,'xnat:dxSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.509',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,92)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',47,1,1,92,1,92,'xnat:dxSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.533',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,48)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,48,'xnat:nmSessionData',48)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.542',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,48)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (48,48,48,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.549',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,93)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',48,1,1,93,1,93,'xnat:nmSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.552',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,94)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',48,1,1,94,1,94,'xnat:nmSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.569',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,49)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,49,'xnat:srSessionData',49)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.577',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,49)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (49,49,49,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.583',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,95)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',49,1,1,95,1,95,'xnat:srSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.586',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,96)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',49,1,1,96,1,96,'xnat:srSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.606',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,50)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,50,'xnat:gmvSessionData',50)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.614',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,50)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (50,50,50,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.619',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,97)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',50,1,1,97,1,97,'xnat:gmvSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.622',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,98)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',50,1,1,98,1,98,'xnat:gmvSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.639',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,51)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,51,'xnat:gmSessionData',51)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.647',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,51)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (51,51,51,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.661',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,99)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',51,1,1,99,1,99,'xnat:gmSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.665',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,100)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',51,1,1,100,1,100,'xnat:gmSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.689',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,52)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,52,'xnat:esvSessionData',52)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.698',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,52)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (52,52,52,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.705',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,101)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',52,1,1,101,1,101,'xnat:esvSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.708',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,102)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',52,1,1,102,1,102,'xnat:esvSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.727',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,53)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,53,'xnat:esSessionData',53)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.735',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,53)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (53,53,53,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.741',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,103)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',53,1,1,103,1,103,'xnat:esSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.744',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,104)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',53,1,1,104,1,104,'xnat:esSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.766',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,54)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,54,'xnat:dx3DCraniofacialSessionData',54)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.771',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,54)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (54,54,54,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.775',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,105)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',54,1,1,105,1,105,'xnat:dx3DCraniofacialSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.777',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,106)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',54,1,1,106,1,106,'xnat:dx3DCraniofacialSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.789',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,55)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,55,'xnat:xa3DSessionData',55)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.794',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,55)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (55,55,55,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.798',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,107)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',55,1,1,107,1,107,'xnat:xa3DSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.800',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,108)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',55,1,1,108,1,108,'xnat:xa3DSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.812',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,56)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,56,'xnat:rfSessionData',56)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.817',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,56)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (56,56,56,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.823',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,109)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',56,1,1,109,1,109,'xnat:rfSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.827',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,110)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',56,1,1,110,1,110,'xnat:rfSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.845',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,57)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,57,'xnat:xaSessionData',57)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.853',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,57)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (57,57,57,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.859',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,111)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',57,1,1,111,1,111,'xnat:xaSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.861',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,112)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',57,1,1,112,1,112,'xnat:xaSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.881',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,58)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,58,'xnat:smSessionData',58)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.891',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,58)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (58,58,58,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.899',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,113)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',58,1,1,113,1,113,'xnat:smSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.902',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,114)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',58,1,1,114,1,114,'xnat:smSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.923',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,59)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,59,'xnat:xcSessionData',59)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.930',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,59)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (59,59,59,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.935',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,115)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',59,1,1,115,1,115,'xnat:xcSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.938',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,116)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',59,1,1,116,1,116,'xnat:xcSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.959',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,60)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,60,'xnat:xcvSessionData',60)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.966',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,60)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (60,60,60,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.971',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,117)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',60,1,1,117,1,117,'xnat:xcvSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.974',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,118)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',60,1,1,118,1,118,'xnat:xcvSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.991',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,61)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,61,'xnat:opSessionData',61)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:54.998',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,61)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (61,61,61,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.004',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,119)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',61,1,1,119,1,119,'xnat:opSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.008',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,120)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',61,1,1,120,1,120,'xnat:opSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.029',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,62)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,62,'xnat:optSessionData',62)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.038',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,62)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (62,62,62,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.045',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,121)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',62,1,1,121,1,121,'xnat:optSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.048',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,122)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',62,1,1,122,1,122,'xnat:optSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.065',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,63)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,63,'xnat:rtSessionData',63)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.074',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,63)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (63,63,63,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.082',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,123)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',63,1,1,123,1,123,'xnat:rtSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.085',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,124)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',63,1,1,124,1,124,'xnat:rtSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.108',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,64)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,64,'xnat:megSessionData',64)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.117',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,64)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (64,64,64,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.124',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,125)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',64,1,1,125,1,125,'xnat:megSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.126',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,126)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',64,1,1,126,1,126,'xnat:megSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.144',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,65)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,65,'xnat:eegSessionData',65)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.151',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,65)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (65,65,65,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.158',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,127)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',65,1,1,127,1,127,'xnat:eegSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.160',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,128)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',65,1,1,128,1,128,'xnat:eegSessionData/project',1,1,'*')

INSERT INTO xdat_element_access_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.179',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,66)

INSERT INTO xdat_element_access (xdat_usergroup_xdat_usergroup_id,element_access_info,element_name,xdat_element_access_id) VALUES (2,66,'xnat:otherDicomSessionData',66)

INSERT INTO xdat_field_mapping_set_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.188',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,66)

INSERT INTO xdat_field_mapping_set (field_mapping_set_info,permissions_allow_set_xdat_elem_xdat_element_access_id,xdat_field_mapping_set_id,method) VALUES (66,66,66,'OR')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.195',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,129)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',66,1,1,129,1,129,'xnat:otherDicomSessionData/sharing/share/project',1,1,'*')

INSERT INTO xdat_field_mapping_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.198',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,130)

INSERT INTO xdat_field_mapping (comparison_type,xdat_field_mapping_set_xdat_field_mapping_set_id,create_element,active_element,xdat_field_mapping_id,delete_element,field_mapping_info,field,edit_element,read_element,field_value) VALUES ('equals',66,1,1,130,1,130,'xnat:otherDicomSessionData/project',1,1,'*')

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

INSERT INTO xdat_user (primary_password_encrypt,xdat_user_id,users_user_xdat_security_xdat_security_id,verified,firstname,lastname,enabled,email,login,user_info,primary_password) VALUES (1,1,1,1,'Admin','Admin',1,'administrator@xnat.org','admin',1,'cfokl')

INSERT INTO xdat_role_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.483',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)

INSERT INTO xdat_role_type (role_type_info,role_name,description,sequence) VALUES (1,'Administrator','can change user permissions',1)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.490',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,1)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Administration',1,'admin',1)

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('Administrator','admin')

INSERT INTO xdat_role_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.504',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)

INSERT INTO xdat_role_type (sequence,role_type_info,description,role_name) VALUES (1,2,'can insert experiments and subjects','DataManager')

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.508',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,2)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Upload XML',2,'XMLUpload',7)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.511',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,3)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Upload Images',3,'LaunchUploadApplet',10)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.515',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,4)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('View Prearchive',4,'prearchives',11)

INSERT INTO xdat_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:55.518',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,5)

INSERT INTO xdat_action_type (display_name,action_type_info,action_name,sequence) VALUES ('Add Project',5,'add_xnat_projectData',7)

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('DataManager','XMLUpload')

INSERT INTO xdat_a_xdat_action_type_allowe_xdat_role_type (xdat_role_type_role_name,xdat_action_type_action_name) VALUES ('DataManager','LaunchUploadApplet')

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

INSERT INTO xdat_user (primary_password_encrypt,xdat_user_id,users_user_xdat_security_xdat_security_id,verified,firstname,lastname,enabled,email,login,user_info,primary_password) VALUES (0,2,1,1,'XNAT','Guest',1,'administrator@xnat.org','guest',2,'guest')

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

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,36,'xnat:mrSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',36,'View Images')

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

INSERT INTO xdat_element_action_type (element_action_type_info,element_actions_element_action__element_name,display_name,element_action_name,image,xdat_element_action_type_id,sequence) VALUES (44,'xnat:subjectData','Add Experiment','add_experiment','update.gif',44,2)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.084',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,45)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','LaunchUploadApplet','edit','Up.gif',3,45,'xnat:subjectData',45,'Upload Images')

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

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,50,'xnat:ctSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',50,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.149',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,51)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,51,'xnat:ctSessionData',51,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.154',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,52)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,52,'xnat:ctSessionData',52,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.160',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,53)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,53,'xnat:ctSessionData',53,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.165',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,54)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,54,'xnat:ctSessionData',54,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.174',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,12)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'ULT',0,1,1,2,0,'Ultrasound','xnat:usSessionData',1,'Ultrasounds',1,1,1,1,12,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.179',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,7)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:usSessionData/sharing/share/project','xnat:usSessionData',7,7)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.184',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,8)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:usSessionData/project','xnat:usSessionData',8,8)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.192',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,55)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,55,'xnat:usSessionData',55,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.197',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,56)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,56,'xnat:usSessionData',56,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.203',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,57)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,57,'xnat:usSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',57,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.208',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,58)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,58,'xnat:usSessionData',58,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.211',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,59)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,59,'xnat:usSessionData',59,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.215',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,60)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,60,'xnat:usSessionData',60,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.219',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,61)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,61,'xnat:usSessionData',61,'Upload Scans')

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

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,64,'xnat:crSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',64,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.253',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,65)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,65,'xnat:crSessionData',65,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.257',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,66)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,66,'xnat:crSessionData',66,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.261',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,67)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,67,'xnat:crSessionData',67,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.267',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,68)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,68,'xnat:crSessionData',68,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.275',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,14)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'EP',0,1,1,2,0,'EPS Session','xnat:epsSessionData',1,'EPS Sessions',1,1,1,1,14,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.279',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,11)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:epsSessionData/sharing/share/project','xnat:epsSessionData',11,11)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.283',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,12)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:epsSessionData/project','xnat:epsSessionData',12,12)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.290',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,69)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,69,'xnat:epsSessionData',69,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.295',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,70)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,70,'xnat:epsSessionData',70,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.299',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,71)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,71,'xnat:epsSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',71,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.304',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,72)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,72,'xnat:epsSessionData',72,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.310',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,73)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,73,'xnat:epsSessionData',73,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.316',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,74)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,74,'xnat:epsSessionData',74,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.322',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,75)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,75,'xnat:epsSessionData',75,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.331',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,15)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'HD',0,1,1,2,0,'HD Session','xnat:hdSessionData',1,'HD Sessions',1,1,1,1,15,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.336',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,13)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:hdSessionData/sharing/share/project','xnat:hdSessionData',13,13)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.342',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,14)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:hdSessionData/project','xnat:hdSessionData',14,14)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.350',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,76)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,76,'xnat:hdSessionData',76,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.355',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,77)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,77,'xnat:hdSessionData',77,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.360',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,78)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,78,'xnat:hdSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',78,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.365',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,79)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,79,'xnat:hdSessionData',79,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.372',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,80)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,80,'xnat:hdSessionData',80,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.377',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,81)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,81,'xnat:hdSessionData',81,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.381',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,82)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,82,'xnat:hdSessionData',82,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.391',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,16)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'ECG',0,1,1,2,0,'ECG Session','xnat:ecgSessionData',1,'ECG Sessions',1,1,1,1,16,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.395',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,15)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:ecgSessionData/sharing/share/project','xnat:ecgSessionData',15,15)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.401',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,16)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:ecgSessionData/project','xnat:ecgSessionData',16,16)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.412',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,83)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,83,'xnat:ecgSessionData',83,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.419',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,84)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,84,'xnat:ecgSessionData',84,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.425',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,85)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,85,'xnat:ecgSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',85,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.430',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,86)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,86,'xnat:ecgSessionData',86,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.436',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,87)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,87,'xnat:ecgSessionData',87,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.441',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,88)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,88,'xnat:ecgSessionData',88,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.447',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,89)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,89,'xnat:ecgSessionData',89,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.457',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,17)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'IO',0,1,1,2,0,'IO Session','xnat:ioSessionData',1,'IO Sessions',1,1,1,1,17,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.461',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,17)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:ioSessionData/sharing/share/project','xnat:ioSessionData',17,17)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.466',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,18)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:ioSessionData/project','xnat:ioSessionData',18,18)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.473',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,90)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,90,'xnat:ioSessionData',90,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.477',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,91)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,91,'xnat:ioSessionData',91,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.482',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,92)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,92,'xnat:ioSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',92,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.486',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,93)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,93,'xnat:ioSessionData',93,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.491',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,94)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,94,'xnat:ioSessionData',94,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.497',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,95)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,95,'xnat:ioSessionData',95,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.502',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,96)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,96,'xnat:ioSessionData',96,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.511',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,18)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'MG',0,1,1,2,0,'MG Session','xnat:mgSessionData',1,'MG Sessions',1,1,1,1,18,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.516',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,19)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:mgSessionData/sharing/share/project','xnat:mgSessionData',19,19)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.520',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,20)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:mgSessionData/project','xnat:mgSessionData',20,20)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.529',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,97)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,97,'xnat:mgSessionData',97,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.534',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,98)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,98,'xnat:mgSessionData',98,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.539',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,99)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,99,'xnat:mgSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',99,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.545',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,100)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,100,'xnat:mgSessionData',100,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.555',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,101)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,101,'xnat:mgSessionData',101,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.559',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,102)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,102,'xnat:mgSessionData',102,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.563',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,103)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,103,'xnat:mgSessionData',103,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.570',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,19)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'DX',0,1,1,2,0,'DX Session','xnat:dxSessionData',1,'DX Sessions',1,1,1,1,19,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.574',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,21)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:dxSessionData/sharing/share/project','xnat:dxSessionData',21,21)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.577',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,22)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:dxSessionData/project','xnat:dxSessionData',22,22)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.584',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,104)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,104,'xnat:dxSessionData',104,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.588',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,105)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,105,'xnat:dxSessionData',105,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.592',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,106)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,106,'xnat:dxSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',106,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.596',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,107)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,107,'xnat:dxSessionData',107,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.600',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,108)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,108,'xnat:dxSessionData',108,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.604',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,109)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,109,'xnat:dxSessionData',109,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.609',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,110)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,110,'xnat:dxSessionData',110,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.616',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,20)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'NM',0,1,1,2,0,'NM Session','xnat:nmSessionData',1,'NM Sessions',1,1,1,1,20,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.620',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,23)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:nmSessionData/sharing/share/project','xnat:nmSessionData',23,23)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.624',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,24)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:nmSessionData/project','xnat:nmSessionData',24,24)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.633',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,111)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,111,'xnat:nmSessionData',111,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.639',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,112)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,112,'xnat:nmSessionData',112,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.644',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,113)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,113,'xnat:nmSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',113,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.648',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,114)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,114,'xnat:nmSessionData',114,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.659',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,115)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,115,'xnat:nmSessionData',115,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.664',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,116)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,116,'xnat:nmSessionData',116,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.668',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,117)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,117,'xnat:nmSessionData',117,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.676',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,21)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'SR',0,1,1,2,0,'SR Session','xnat:srSessionData',1,'SR Sessions',1,1,1,1,21,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.680',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,25)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:srSessionData/sharing/share/project','xnat:srSessionData',25,25)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.684',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,26)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:srSessionData/project','xnat:srSessionData',26,26)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.691',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,118)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,118,'xnat:srSessionData',118,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.698',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,119)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,119,'xnat:srSessionData',119,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.704',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,120)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,120,'xnat:srSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',120,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.710',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,121)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,121,'xnat:srSessionData',121,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.717',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,122)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,122,'xnat:srSessionData',122,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.723',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,123)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,123,'xnat:srSessionData',123,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.729',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,124)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,124,'xnat:srSessionData',124,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.738',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,22)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'GMV',0,1,1,2,0,'GMV Session','xnat:gmvSessionData',1,'GMV Sessions',1,1,1,1,22,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.743',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,27)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:gmvSessionData/sharing/share/project','xnat:gmvSessionData',27,27)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.748',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,28)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:gmvSessionData/project','xnat:gmvSessionData',28,28)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.757',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,125)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,125,'xnat:gmvSessionData',125,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.762',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,126)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,126,'xnat:gmvSessionData',126,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.767',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,127)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,127,'xnat:gmvSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',127,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.771',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,128)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,128,'xnat:gmvSessionData',128,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.776',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,129)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,129,'xnat:gmvSessionData',129,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.780',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,130)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,130,'xnat:gmvSessionData',130,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.785',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,131)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,131,'xnat:gmvSessionData',131,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.792',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,23)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'GM',0,1,1,2,0,'GM Session','xnat:gmSessionData',1,'GM Sessions',1,1,1,1,23,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.797',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,29)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:gmSessionData/sharing/share/project','xnat:gmSessionData',29,29)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.801',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,30)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:gmSessionData/project','xnat:gmSessionData',30,30)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.809',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,132)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,132,'xnat:gmSessionData',132,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.814',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,133)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,133,'xnat:gmSessionData',133,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.821',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,134)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,134,'xnat:gmSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',134,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.825',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,135)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,135,'xnat:gmSessionData',135,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.830',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,136)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,136,'xnat:gmSessionData',136,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.835',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,137)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,137,'xnat:gmSessionData',137,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.839',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,138)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,138,'xnat:gmSessionData',138,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.847',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,24)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'ESV',0,1,1,2,0,'ESV Session','xnat:esvSessionData',1,'ESV Sessions',1,1,1,1,24,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.851',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,31)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:esvSessionData/sharing/share/project','xnat:esvSessionData',31,31)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.855',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,32)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:esvSessionData/project','xnat:esvSessionData',32,32)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.864',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,139)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,139,'xnat:esvSessionData',139,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.869',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,140)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,140,'xnat:esvSessionData',140,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.873',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,141)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,141,'xnat:esvSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',141,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.879',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,142)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,142,'xnat:esvSessionData',142,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.885',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,143)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,143,'xnat:esvSessionData',143,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.891',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,144)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,144,'xnat:esvSessionData',144,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.896',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,145)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,145,'xnat:esvSessionData',145,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.906',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,25)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'ES',0,1,1,2,0,'ES Session','xnat:esSessionData',1,'ES Sessions',1,1,1,1,25,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.911',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,33)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:esSessionData/sharing/share/project','xnat:esSessionData',33,33)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.916',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,34)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:esSessionData/project','xnat:esSessionData',34,34)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.926',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,146)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,146,'xnat:esSessionData',146,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.931',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,147)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,147,'xnat:esSessionData',147,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.936',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,148)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,148,'xnat:esSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',148,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.940',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,149)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,149,'xnat:esSessionData',149,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.946',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,150)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,150,'xnat:esSessionData',150,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.952',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,151)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,151,'xnat:esSessionData',151,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.957',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,152)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,152,'xnat:esSessionData',152,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.966',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,26)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'DX3DCRANIOFACIAL',0,1,1,2,0,'DX3DCRANIOFACIAL Session','xnat:dx3DCraniofacialSessionData',1,'DX3DCRANIOFACIAL Sessions',1,1,1,1,26,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.971',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,35)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:dx3DCraniofacialSessionData/sharing/share/project','xnat:dx3DCraniofacialSessionData',35,35)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.976',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,36)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:dx3DCraniofacialSessionData/project','xnat:dx3DCraniofacialSessionData',36,36)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.984',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,153)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,153,'xnat:dx3DCraniofacialSessionData',153,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.991',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,154)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,154,'xnat:dx3DCraniofacialSessionData',154,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:56.996',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,155)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,155,'xnat:dx3DCraniofacialSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',155,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.004',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,156)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,156,'xnat:dx3DCraniofacialSessionData',156,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.010',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,157)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,157,'xnat:dx3DCraniofacialSessionData',157,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.015',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,158)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,158,'xnat:dx3DCraniofacialSessionData',158,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.021',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,159)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,159,'xnat:dx3DCraniofacialSessionData',159,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.030',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,27)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'XA3D',0,1,1,2,0,'XA3D Session','xnat:xa3DSessionData',1,'XA3D Sessions',1,1,1,1,27,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.035',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,37)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:xa3DSessionData/sharing/share/project','xnat:xa3DSessionData',37,37)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.041',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,38)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:xa3DSessionData/project','xnat:xa3DSessionData',38,38)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.050',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,160)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,160,'xnat:xa3DSessionData',160,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.056',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,161)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,161,'xnat:xa3DSessionData',161,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.061',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,162)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,162,'xnat:xa3DSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',162,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.066',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,163)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,163,'xnat:xa3DSessionData',163,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.071',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,164)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,164,'xnat:xa3DSessionData',164,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.076',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,165)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,165,'xnat:xa3DSessionData',165,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.080',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,166)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,166,'xnat:xa3DSessionData',166,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.090',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,28)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'RF',0,1,1,2,0,'RF Session','xnat:rfSessionData',1,'RF Sessions',1,1,1,1,28,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.095',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,39)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:rfSessionData/sharing/share/project','xnat:rfSessionData',39,39)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.099',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,40)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:rfSessionData/project','xnat:rfSessionData',40,40)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.110',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,167)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,167,'xnat:rfSessionData',167,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.117',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,168)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,168,'xnat:rfSessionData',168,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.122',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,169)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,169,'xnat:rfSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',169,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.127',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,170)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,170,'xnat:rfSessionData',170,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.133',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,171)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,171,'xnat:rfSessionData',171,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.140',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,172)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,172,'xnat:rfSessionData',172,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.148',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,173)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,173,'xnat:rfSessionData',173,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.158',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,29)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'XA',0,1,1,2,0,'XA Session','xnat:xaSessionData',1,'XA Sessions',1,1,1,1,29,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.162',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,41)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:xaSessionData/sharing/share/project','xnat:xaSessionData',41,41)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.167',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,42)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:xaSessionData/project','xnat:xaSessionData',42,42)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.176',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,174)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,174,'xnat:xaSessionData',174,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.181',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,175)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,175,'xnat:xaSessionData',175,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.188',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,176)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,176,'xnat:xaSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',176,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.195',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,177)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,177,'xnat:xaSessionData',177,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.203',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,178)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,178,'xnat:xaSessionData',178,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.210',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,179)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,179,'xnat:xaSessionData',179,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.218',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,180)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,180,'xnat:xaSessionData',180,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.230',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,30)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'SM',0,1,1,2,0,'SM Session','xnat:smSessionData',1,'SM Sessions',1,1,1,1,30,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.236',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,43)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:smSessionData/sharing/share/project','xnat:smSessionData',43,43)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.242',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,44)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:smSessionData/project','xnat:smSessionData',44,44)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.253',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,181)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,181,'xnat:smSessionData',181,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.261',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,182)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,182,'xnat:smSessionData',182,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.268',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,183)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,183,'xnat:smSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',183,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.276',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,184)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,184,'xnat:smSessionData',184,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.283',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,185)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,185,'xnat:smSessionData',185,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.290',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,186)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,186,'xnat:smSessionData',186,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.296',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,187)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,187,'xnat:smSessionData',187,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.305',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,31)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'XC',0,1,1,2,0,'XC Session','xnat:xcSessionData',1,'XC Sessions',1,1,1,1,31,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.310',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,45)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:xcSessionData/sharing/share/project','xnat:xcSessionData',45,45)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.314',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,46)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:xcSessionData/project','xnat:xcSessionData',46,46)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.323',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,188)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,188,'xnat:xcSessionData',188,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.328',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,189)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,189,'xnat:xcSessionData',189,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.334',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,190)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,190,'xnat:xcSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',190,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.340',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,191)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,191,'xnat:xcSessionData',191,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.346',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,192)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,192,'xnat:xcSessionData',192,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.353',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,193)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,193,'xnat:xcSessionData',193,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.359',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,194)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,194,'xnat:xcSessionData',194,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.367',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,32)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'XCV',0,1,1,2,0,'XCV Session','xnat:xcvSessionData',1,'XCV Sessions',1,1,1,1,32,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.372',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,47)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:xcvSessionData/sharing/share/project','xnat:xcvSessionData',47,47)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.376',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,48)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:xcvSessionData/project','xnat:xcvSessionData',48,48)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.391',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,195)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,195,'xnat:xcvSessionData',195,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.399',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,196)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,196,'xnat:xcvSessionData',196,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.406',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,197)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,197,'xnat:xcvSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',197,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.412',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,198)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,198,'xnat:xcvSessionData',198,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.418',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,199)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,199,'xnat:xcvSessionData',199,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.426',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,200)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,200,'xnat:xcvSessionData',200,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.433',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,201)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,201,'xnat:xcvSessionData',201,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.442',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,33)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'OP',0,1,1,2,0,'OP Session','xnat:opSessionData',1,'OP Sessions',1,1,1,1,33,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.447',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,49)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:opSessionData/sharing/share/project','xnat:opSessionData',49,49)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.452',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,50)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:opSessionData/project','xnat:opSessionData',50,50)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.462',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,202)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,202,'xnat:opSessionData',202,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.468',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,203)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,203,'xnat:opSessionData',203,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.475',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,204)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,204,'xnat:opSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',204,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.481',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,205)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,205,'xnat:opSessionData',205,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.488',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,206)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,206,'xnat:opSessionData',206,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.494',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,207)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,207,'xnat:opSessionData',207,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.501',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,208)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,208,'xnat:opSessionData',208,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.510',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,34)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'OPT',0,1,1,2,0,'OPT Session','xnat:optSessionData',1,'OPT Sessions',1,1,1,1,34,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.517',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,51)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:optSessionData/sharing/share/project','xnat:optSessionData',51,51)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.522',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,52)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:optSessionData/project','xnat:optSessionData',52,52)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.533',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,209)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,209,'xnat:optSessionData',209,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.540',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,210)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,210,'xnat:optSessionData',210,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.546',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,211)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,211,'xnat:optSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',211,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.552',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,212)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,212,'xnat:optSessionData',212,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.558',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,213)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,213,'xnat:optSessionData',213,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.564',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,214)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,214,'xnat:optSessionData',214,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.570',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,215)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,215,'xnat:optSessionData',215,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.579',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,35)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'RT',0,1,1,2,0,'RT Session','xnat:rtSessionData',1,'RT Sessions',1,1,1,1,35,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.583',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,53)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:rtSessionData/sharing/share/project','xnat:rtSessionData',53,53)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.588',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,54)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:rtSessionData/project','xnat:rtSessionData',54,54)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.597',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,216)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,216,'xnat:rtSessionData',216,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.603',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,217)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,217,'xnat:rtSessionData',217,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.610',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,218)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,218,'xnat:rtSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',218,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.617',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,219)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,219,'xnat:rtSessionData',219,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.623',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,220)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,220,'xnat:rtSessionData',220,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.629',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,221)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,221,'xnat:rtSessionData',221,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.635',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,222)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,222,'xnat:rtSessionData',222,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.644',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,36)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'MEG',0,1,1,2,0,'MEG Session','xnat:megSessionData',1,'MEG Sessions',1,1,1,1,36,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.649',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,55)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:megSessionData/sharing/share/project','xnat:megSessionData',55,55)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.653',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,56)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:megSessionData/project','xnat:megSessionData',56,56)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.663',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,223)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,223,'xnat:megSessionData',223,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.670',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,224)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,224,'xnat:megSessionData',224,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.676',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,225)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,225,'xnat:megSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',225,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.683',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,226)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,226,'xnat:megSessionData',226,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.688',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,227)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,227,'xnat:megSessionData',227,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.693',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,228)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,228,'xnat:megSessionData',228,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.698',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,229)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,229,'xnat:megSessionData',229,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.706',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,37)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'EEG',0,1,1,2,0,'EEG Session','xnat:eegSessionData',1,'EEG Sessions',1,1,1,1,37,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.710',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,57)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:eegSessionData/sharing/share/project','xnat:eegSessionData',57,57)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.714',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,58)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:eegSessionData/project','xnat:eegSessionData',58,58)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.722',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,230)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,230,'xnat:eegSessionData',230,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.727',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,231)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,231,'xnat:eegSessionData',231,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.732',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,232)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,232,'xnat:eegSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',232,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.740',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,233)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,233,'xnat:eegSessionData',233,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.747',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,234)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,234,'xnat:eegSessionData',234,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.755',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,235)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,235,'xnat:eegSessionData',235,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.762',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,236)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,236,'xnat:eegSessionData',236,'Upload Scans')

INSERT INTO xdat_element_security_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.771',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,38)

INSERT INTO xdat_element_security (secure_ip,code,secondary_password,secure_delete,searchable,sequence,quarantine,singular,element_name,secure,plural,secure_edit,browse,secure_read,accessible,element_security_info,secure_create,element_security_set_element_se_xdat_security_id,pre_load) VALUES (0,'OT',0,1,1,2,0,'Other DICOM Session','xnat:otherDicomSessionData',1,'Other DICOM Sessions',1,1,1,1,38,1,1,0)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.775',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,59)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:otherDicomSessionData/sharing/share/project','xnat:otherDicomSessionData',59,59)

INSERT INTO xdat_primary_security_field_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.779',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,60)

INSERT INTO xdat_primary_security_field (primary_security_field,primary_security_fields_primary_element_name,xdat_primary_security_field_id,primary_security_field_info) VALUES ('xnat:otherDicomSessionData/project','xnat:otherDicomSessionData',60,60)

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.787',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,237)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml','View','r.gif',2,237,'xnat:otherDicomSessionData',237,'View XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.792',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,238)

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','edit','edit','e.gif',0,238,'xnat:otherDicomSessionData',238,'Edit')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.797',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,239)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,239,'xnat:otherDicomSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',239,'View Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.802',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,240)

INSERT INTO xdat_element_action_type (element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('xml_file','Download','save.gif',7,240,'xnat:otherDicomSessionData',240,'Download XML')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.807',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,241)

INSERT INTO xdat_element_action_type (popup,element_action_name,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','email_report','right2.gif',8,241,'xnat:otherDicomSessionData',241,'Email')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.812',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,242)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('never','XDATScreen_download_sessions','Download','download.gif',10,242,'xnat:otherDicomSessionData',242,'Download Images')

INSERT INTO xdat_element_action_type_meta_data (status,activation_date,modified,row_last_modified,insert_date,xft_version,shareable,meta_data_id) VALUES ('active','2013-09-25 18:31:57.817',0,'2013-09-25 18:31:52.129','2013-09-25 18:31:52.129','1',1,243)

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','XDATScreen_upload_scans_xnat_imageSessionData','Upload','edit','Up.gif',4,243,'xnat:otherDicomSessionData',243,'Upload Scans')

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

INSERT INTO xdat_element_action_type (popup,element_action_name,grouping,image,sequence,element_action_type_info,element_actions_element_action__element_name,parameterstring,xdat_element_action_type_id,display_name) VALUES ('always','Viewer','View','v.gif',3,246,'xnat:petSessionData','/popup_params/width=320,height=420,status=yes,resizable=yes,scrollbars=yes,toolbar=yes',246,'View Images')

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

INSERT INTO xdat_element_action_type (popup,element_action_name,secureaccess,image,sequence,element_action_type_info,element_actions_element_action__element_name,xdat_element_action_type_id,display_name) VALUES ('sometimes','delete','delete','delete.gif',5,258,'xnat:qcManualAssessorData',258,'Delete')

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
