-- Creates new element access entry for the element associated with the guest user.
INSERT INTO xdat_element_access (element_name, xdat_user_xdat_user_id)
SELECT :elementName AS element_name, u.xdat_user_id
FROM
     xdat_user u
       LEFT JOIN xdat_element_access a ON u.xdat_user_id = a.xdat_user_xdat_user_id AND a.element_name = :elementName
WHERE
  a.element_name IS NULL AND
  u.login = 'guest';

-- Creates a new field mapping set associated with the element access entry created above.
-- The SELECT query finds the element access entry ID by searching for the entry with the
-- correct element name but no associated field mapping set.
INSERT INTO xdat_field_mapping_set (method, permissions_allow_set_xdat_elem_xdat_element_access_id)
SELECT 'OR' AS method, a.xdat_element_access_id
FROM
     xdat_element_access a
       LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
WHERE
  a.element_name = :elementName AND
  s.method IS NULL;

-- Create the field mapping entries associated with the field mapping set created above. The WITH query
-- returns the project ID of all public projects on the system. The SELECT query finds the field mapping
-- set associated with the element access entry by data type and association with the guest user. It then
-- generates an entry for each public project and primary security field for the data type.
INSERT INTO xdat_field_mapping (field, field_value, create_element, read_element, edit_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
WITH
     public_projects AS (SELECT id AS project
FROM
     project_access
WHERE
  accessibility = 'public')
SELECT f.primary_security_field, p.project, 0, 1, 0, 0, 1, 'equals', s.xdat_field_mapping_set_id
FROM
     public_projects p,
     xdat_element_access a
       LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
       LEFT JOIN xdat_user u ON a.xdat_user_xdat_user_id = u.xdat_user_id
       LEFT JOIN xdat_primary_security_field f ON a.element_name = f.primary_security_fields_primary_element_name
WHERE
  a.element_name = :elementName AND
  u.login = 'guest';