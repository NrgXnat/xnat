CREATE OR REPLACE VIEW public.data_type_views_element_access AS
SELECT
       coalesce(g.id, 'user:' || u.login) AS security_entity,
       a.element_name AS data_type,
       CASE m.field_value
         WHEN '*'
           THEN 'any'
         ELSE m.field_value
         END AS project,
       m.field AS security_field,
       m.active_element,
       m.read_element,
       m.edit_element,
       m.create_element,
       m.delete_element,
       m.comparison_type,
       s.method
FROM
     xdat_element_access a
       LEFT JOIN xdat_user u ON a.xdat_user_xdat_user_id = u.xdat_user_id
       LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
       LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
       LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
ORDER BY
         project,
         security_entity,
         data_type,
         security_field;

CREATE OR REPLACE VIEW public.data_type_views_mismatched_mapping_elements AS
SELECT m.xdat_field_mapping_id AS id
FROM
     xdat_field_mapping m
       LEFT JOIN xdat_field_mapping_set s ON m.xdat_field_mapping_set_xdat_field_mapping_set_id = s.xdat_field_mapping_set_id
       LEFT JOIN xdat_element_access a ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
WHERE
  m.field NOT LIKE a.element_name || '%';

CREATE OR REPLACE VIEW public.data_type_views_orphaned_field_sets AS
SELECT s.xdat_field_mapping_set_id AS id
FROM
     xdat_element_access a
       LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
       LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
WHERE
  s.xdat_field_mapping_set_id IS NOT NULL AND
  m.xdat_field_mapping_id IS NULL;

CREATE OR REPLACE FUNCTION create_new_data_type_permissions(new_element_name CHARACTER VARYING)
  RETURNS BOOLEAN
LANGUAGE plpgsql
AS $_$
  DECLARE
  has_public_projects BOOLEAN;
BEGIN
    INSERT INTO xdat_element_access (element_name, xdat_usergroup_xdat_usergroup_id)
    SELECT new_element_name AS element_name, xdat_userGroup_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
    WHERE
      ea.element_name IS NULL AND
      ug.tag IS NOT NULL;

    INSERT INTO xdat_field_mapping_set (method, permissions_allow_set_xdat_elem_xdat_element_access_id)
    SELECT 'OR' AS method, xdat_element_access_id
    FROM
         xdat_element_access ea
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
    WHERE
      ea.element_name = new_element_name AND
      fms.method IS NULL;

    INSERT INTO xdat_field_mapping (field, field_value, create_element, read_element, edit_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
    SELECT new_element_name || '/project' AS field, ug.tag, 1, 1, 1, 1, 1, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      ug.displayname = 'Owners' AND
      fm.field IS NULL AND
      ug.tag IS NOT NULL
    UNION
    SELECT new_element_name || '/sharing/share/project' AS field, ug.tag, 0, 1, 0, 0, 1, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      ug.displayname = 'Owners' AND
      fm.field IS NULL AND
      ug.tag IS NOT NULL
    UNION
    SELECT new_element_name || '/project' AS field, ug.tag, 1, 1, 1, 0, 0, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      ug.displayname = 'Members' AND
      fm.field IS NULL AND
      ug.tag IS NOT NULL
    UNION
    SELECT new_element_name || '/sharing/share/project' AS field, ug.tag, 0, 1, 0, 0, 0, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      ug.displayname = 'Members' AND
      fm.field IS NULL AND
      ug.tag IS NOT NULL
    UNION
    SELECT new_element_name || '/project' AS field, ug.tag, 0, 1, 0, 0, 0, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      ug.displayname = 'Collaborators' AND
      fm.field IS NULL AND
      ug.tag IS NOT NULL
    UNION
    SELECT new_element_name || '/sharing/share/project' AS field, ug.tag, 0, 1, 0, 0, 0, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      ug.displayname = 'Collaborators' AND
      fm.field IS NULL AND
      ug.tag IS NOT NULL;

    INSERT INTO xdat_element_access (element_name, xdat_usergroup_xdat_usergroup_id)
    SELECT new_element_name AS element_name, xdat_userGroup_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
    WHERE
      ea.element_name IS NULL AND
      (id = 'ALL_DATA_ADMIN' OR id = 'ALL_DATA_ACCESS');

    INSERT INTO xdat_field_mapping_set (method, permissions_allow_set_xdat_elem_xdat_element_access_id)
    SELECT 'OR' AS method, xdat_element_access_id
    FROM
         xdat_element_access ea
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
    WHERE
      ea.element_name = new_element_name AND
      fms.method IS NULL;

    INSERT INTO xdat_field_mapping (field, field_value, create_element, read_element, edit_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
    SELECT new_element_name || '/project' AS field, '*', 1, 1, 1, 1, 1, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      id = 'ALL_DATA_ADMIN' AND
      fm.field IS NULL
    UNION
    SELECT new_element_name || '/sharing/share/project' AS field, '*', 1, 1, 1, 1, 1, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      id = 'ALL_DATA_ADMIN' AND
      fm.field IS NULL
    UNION
    SELECT new_element_name || '/project' AS field, '*', 0, 1, 0, 0, 1, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      id = 'ALL_DATA_ACCESS' AND
      fm.field IS NULL
    UNION
    SELECT new_element_name || '/sharing/share/project' AS field, '*', 0, 1, 0, 0, 1, 'equals', fms.xdat_field_mapping_set_id
    FROM
         xdat_userGroup ug
           LEFT JOIN xdat_element_access ea ON ug.xdat_usergroup_id = ea.xdat_usergroup_xdat_usergroup_id AND ea.element_name = new_element_name
           LEFT JOIN xdat_field_mapping_set fms ON ea.xdat_element_access_id = fms.permissions_allow_set_xdat_elem_xdat_element_access_id
           LEFT JOIN xdat_field_mapping fm ON fms.xdat_field_mapping_set_id = fm.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
      id = 'ALL_DATA_ACCESS' AND
      fm.field IS NULL;

    SELECT count(*) > 0 INTO has_public_projects
    FROM
         project_access
    WHERE
      accessibility = 'public';

    IF has_public_projects
    THEN
    -- Creates new element access entry for the element associated with the guest user.
    INSERT INTO xdat_element_access (element_name, xdat_user_xdat_user_id)
    SELECT new_element_name AS element_name, u.xdat_user_id
    FROM
         xdat_user u
           LEFT JOIN xdat_element_access a ON u.xdat_user_id = a.xdat_user_xdat_user_id AND a.element_name = new_element_name
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
      a.element_name = new_element_name AND
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
      a.element_name = new_element_name AND
      u.login = 'guest';
    END IF;
    RETURN TRUE;
  END
$_$;

CREATE OR REPLACE FUNCTION fix_mismatched_data_type_permissions()
  RETURNS BOOLEAN
LANGUAGE plpgsql
AS $_$
  DECLARE
    has_mismatches BOOLEAN;
    data_type VARCHAR(255);
  BEGIN
    SELECT count(*) > 0 INTO has_mismatches FROM data_type_views_mismatched_mapping_elements;
    IF has_mismatches THEN
      DELETE FROM xdat_field_mapping WHERE xdat_field_mapping_id IN (SELECT id FROM data_type_views_mismatched_mapping_elements);
      DELETE FROM xdat_field_mapping_set WHERE xdat_field_mapping_set_id IN (SELECT id FROM data_type_functions_orphaned_field_sets);
      FOR data_type IN SELECT DISTINCT primary_security_fields_primary_element_name AS data_type FROM xdat_primary_security_field WHERE primary_security_fields_primary_element_name NOT IN (SELECT DISTINCT element_name FROM xdat_element_access)
      LOOP
        PERFORM create_new_data_type_permissions(data_type);
      END LOOP;
    END IF;
    RETURN has_mismatches;
  END
$_$;
