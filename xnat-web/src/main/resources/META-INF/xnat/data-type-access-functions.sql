CREATE OR REPLACE VIEW public.data_type_views_element_access AS
  SELECT
    coalesce(g.id, 'user:' || u.login) AS entity,
    a.element_name,
    m.field_value,
    m.field,
    m.active_element,
    m.read_element,
    m.edit_element,
    m.create_element,
    m.delete_element,
    m.comparison_type,
    s.method,
    a.xdat_element_access_id,
    s.xdat_field_mapping_set_id,
    m.xdat_field_mapping_id
  FROM
    xdat_element_access a
    LEFT JOIN xdat_user u ON a.xdat_user_xdat_user_id = u.xdat_user_id
    LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
    LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
    LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id;

CREATE OR REPLACE VIEW public.data_type_views_mismatched_mapping_elements AS
  SELECT m.xdat_field_mapping_id AS id
  FROM
    xdat_field_mapping m
    LEFT JOIN xdat_field_mapping_set s ON m.xdat_field_mapping_set_xdat_field_mapping_set_id = s.xdat_field_mapping_set_id
    LEFT JOIN xdat_element_access a ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
  WHERE
    m.field NOT LIKE a.element_name || '%';

CREATE OR REPLACE VIEW public.data_type_views_missing_mapping_elements AS
  WITH
      public_project_access_mappings AS (SELECT field_value, element_name, field, xdat_field_mapping_set_id
                                         FROM
                                           data_type_views_element_access
                                         WHERE
                                           element_name != 'xnat:projectData' AND
                                           entity = 'user:guest')
  SELECT f.primary_security_field AS field, field_value, xdat_field_mapping_set_id
  FROM
    xdat_primary_security_field f
    LEFT JOIN public_project_access_mappings m ON f.primary_security_fields_primary_element_name = element_name
  WHERE
    f.primary_security_fields_primary_element_name != 'xnat:projectData' AND
    (m.field_value IS NULL OR
     (f.primary_security_fields_primary_element_name, f.primary_security_field) NOT IN (SELECT m.element_name, public_project_access_mappings.field
                                                                                        FROM
                                                                                          public_project_access_mappings));

CREATE OR REPLACE VIEW public.data_type_views_orphaned_field_sets AS
  SELECT s.xdat_field_mapping_set_id AS id
  FROM
    xdat_element_access a
    LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
    LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
  WHERE
    s.xdat_field_mapping_set_id IS NOT NULL AND
    m.xdat_field_mapping_id IS NULL;

CREATE OR REPLACE FUNCTION create_public_element_access_for_data_type(elementName VARCHAR(255))
  RETURNS BOOLEAN
LANGUAGE plpgsql
AS $_$
BEGIN
  -- Creates new element access entry for the element associated with the guest user.
  INSERT INTO xdat_element_access (element_name, xdat_user_xdat_user_id)
  SELECT elementName AS element_name, u.xdat_user_id
  FROM
    xdat_user u
    LEFT JOIN xdat_element_access a ON u.xdat_user_id = a.xdat_user_xdat_user_id AND a.element_name = elementName
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
    a.element_name = elementName AND
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
    a.element_name = elementName AND
    u.login = 'guest';

  RETURN TRUE;
END
$_$;

CREATE OR REPLACE FUNCTION create_new_data_type_security(elementName VARCHAR(255), singularDesc VARCHAR(255), pluralDesc VARCHAR(255), codeDesc VARCHAR(255))
  RETURNS VARCHAR(255)
LANGUAGE plpgsql
AS $_$
BEGIN
  INSERT INTO xdat_element_security (element_name, singular, plural, code, secondary_password, secure_ip, secure, browse, sequence, quarantine, pre_load, searchable, secure_read, secure_edit, secure_create, secure_delete, accessible, usage, category, element_security_set_element_se_xdat_security_id)
  SELECT elementName, singularDesc, pluralDesc, codeDesc, secondary_password, secure_ip, secure, browse, sequence, quarantine, pre_load, searchable, secure_read, secure_edit, secure_create, secure_delete, accessible, usage, category, element_security_set_element_se_xdat_security_id
  FROM
    xdat_element_security
  WHERE
    element_name = 'xnat:mrSessionData';
  INSERT INTO xdat_primary_security_field (primary_security_field, primary_security_fields_primary_element_name)
  VALUES
    (elementName || '/project', elementName);
  INSERT INTO xdat_primary_security_field (primary_security_field, primary_security_fields_primary_element_name)
  VALUES
    (elementName || '/sharing/share/project', elementName);
  RETURN elementName;
END
$_$;

CREATE OR REPLACE FUNCTION create_new_data_type_permissions(elementName VARCHAR(255))
  RETURNS BOOLEAN
LANGUAGE plpgsql
AS $_$
DECLARE
  has_public_projects BOOLEAN;
BEGIN
  INSERT INTO xdat_element_access (element_name, xdat_usergroup_xdat_usergroup_id)
  SELECT elementName AS element_name, xdat_usergroup_id
  FROM
    xdat_usergroup g
    LEFT JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id AND a.element_name = elementName
  WHERE
    a.element_name IS NULL AND
    (g.tag IS NOT NULL OR id = 'ALL_DATA_ADMIN' OR id = 'ALL_DATA_ACCESS');

  INSERT INTO xdat_field_mapping_set (method, permissions_allow_set_xdat_elem_xdat_element_access_id)
  SELECT 'OR' AS method, xdat_element_access_id
  FROM
    xdat_element_access a
    LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
  WHERE
    a.element_name = elementName AND
    s.method IS NULL;

  INSERT INTO xdat_field_mapping (field_value, field, create_element, read_element, edit_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
  SELECT field_value, field, CASE WHEN is_shared THEN create_shared ELSE create_element END AS create_element, CASE WHEN is_shared THEN read_shared ELSE read_element END AS read_element, CASE WHEN is_shared THEN edit_shared ELSE edit_element END AS edit_element, CASE WHEN is_shared THEN delete_shared ELSE delete_element END AS delete_element, CASE WHEN is_shared THEN active_shared ELSE active_element END AS active_element, comparison_type, xdat_field_mapping_set_id
  FROM
    (WITH
        group_permissions AS (SELECT groupNameOrId, create_element, read_element, edit_element, delete_element, active_element, create_shared, read_shared, edit_shared, delete_shared, active_shared
                              FROM
                                (VALUES
                                   ('Owners', 1, 1, 1, 1, 1, 0, 1, 0, 0, 1),
                                   ('Members', 1, 1, 1, 0, 0, 0, 1, 0, 0, 0),
                                   ('Collaborators', 0, 1, 0, 0, 0, 0, 1, 0, 0, 0),
                                   ('ALL_DATA_ADMIN', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
                                   ('ALL_DATA_ACCESS', 0, 1, 0, 0, 1, 0, 1, 0, 0, 1)) AS groupNames (groupNameOrId, create_element, read_element, edit_element, delete_element, active_element, create_shared, read_shared, edit_shared, delete_shared, active_shared))
    SELECT f.primary_security_field AS field, f.primary_security_field LIKE '%/sharing/share/project' AS is_shared, coalesce(g.tag, '*') AS field_value, p.create_element AS create_element, p.read_element AS read_element, p.edit_element AS edit_element, p.delete_element AS delete_element, p.active_element AS active_element, p.create_shared AS create_shared, p.read_shared AS read_shared, p.edit_shared AS edit_shared, p.delete_shared AS delete_shared, p.active_shared AS active_shared, 'equals' AS comparison_type, s.xdat_field_mapping_set_id AS xdat_field_mapping_set_id
    FROM
      group_permissions p,
      xdat_usergroup g
      LEFT JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id AND a.element_name = elementName
      LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
      LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
      LEFT JOIN xdat_primary_security_field f ON a.element_name = f.primary_security_fields_primary_element_name
    WHERE
      groupNameOrId IN (g.displayname, g.id) AND
      m.field IS NULL AND
      (g.tag IS NOT NULL OR g.id IN ('ALL_DATA_ADMIN', 'ALL_DATA_ACCESS'))) m;

  SELECT count(*) > 0 INTO has_public_projects
  FROM
    project_access
  WHERE
    accessibility = 'public';

  IF has_public_projects
  THEN
    PERFORM create_public_element_access_for_data_type(elementName);
  END IF;
  RETURN TRUE;
END
$_$;

CREATE OR REPLACE FUNCTION fix_missing_public_element_access_mappings()
  RETURNS BOOLEAN
LANGUAGE plpgsql
AS $_$
DECLARE
  has_missing_mappings BOOLEAN;
BEGIN
  SELECT count(*) > 0 INTO has_missing_mappings
  FROM
    data_type_views_missing_mapping_elements;
  IF has_missing_mappings
  THEN
    INSERT INTO xdat_field_mapping (field, field_value, create_element, read_element, edit_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
    SELECT e.field, e.field_value, 0, 1, 0, 0, 1, 'equals', e.xdat_field_mapping_set_id
    FROM
      data_type_views_missing_mapping_elements e
    WHERE
      e.field_value IS NOT NULL
    UNION
    SELECT e.field, a.id, 0, 1, 0, 0, 1, 'equals', e.xdat_field_mapping_set_id
    FROM
      data_type_views_missing_mapping_elements e,
      project_access a
    WHERE
      e.field_value IS NULL AND
      a.accessibility = 'public';
  END IF;
  RETURN has_missing_mappings;
END
$_$;

CREATE OR REPLACE FUNCTION fix_mismatched_data_type_permissions()
  RETURNS BOOLEAN
LANGUAGE plpgsql
AS $_$
DECLARE
  has_mismatches BOOLEAN;
  has_missing    BOOLEAN;
  data_type      VARCHAR(255);
BEGIN
    SELECT count(*) > 0 INTO has_mismatches FROM data_type_views_mismatched_mapping_elements;
    SELECT count(*) > 0 INTO has_missing FROM data_type_views_missing_mapping_elements;
    IF has_mismatches OR has_missing THEN
      DELETE FROM xdat_field_mapping WHERE xdat_field_mapping_id IN (SELECT id FROM data_type_views_mismatched_mapping_elements);
      DELETE FROM xdat_field_mapping_set WHERE xdat_field_mapping_set_id IN (SELECT id FROM data_type_views_orphaned_field_sets);
      FOR data_type IN SELECT DISTINCT primary_security_fields_primary_element_name AS data_type FROM xdat_primary_security_field WHERE primary_security_fields_primary_element_name NOT IN (SELECT DISTINCT element_name FROM xdat_element_access)
    LOOP
      PERFORM create_new_data_type_permissions(data_type);
    END LOOP;
  END IF;
    RETURN has_mismatches OR has_missing;
END
$_$;

-- Gets all hash indices in the public schema along with the CREATE INDEX
-- statements required to regenerate the indices.
CREATE OR REPLACE VIEW get_xnat_hash_indices AS
SELECT
  indexname,
  regexp_replace(indexdef, E'[\n\r]+', ' ', 'g') AS recreate
FROM
  pg_indexes
WHERE
    indexdef LIKE '%hash%' AND
    schemaname = 'public';

-- Drops all hash indices as returned with the get_hash_indices view. The
-- recreate parameter is true by default and indicates whether each index
-- should be regenerated once it's been dropped.
CREATE OR REPLACE FUNCTION drop_xnat_hash_indices(recreate BOOLEAN DEFAULT TRUE)
  RETURNS INTEGER
AS
$_$
DECLARE
  total_count INTEGER := 0;
BEGIN
  DECLARE
    current_index RECORD;
  BEGIN
    FOR current_index IN SELECT * FROM get_xnat_hash_indices
      LOOP
        total_count := total_count + 1;
        EXECUTE ('DROP INDEX ' || current_index.indexname);
        IF recreate
        THEN
          EXECUTE (current_index.recreate);
        END IF;
      END LOOP;
  END;
  RETURN total_count;
END;
$_$
  LANGUAGE plpgsql;
