DROP FUNCTION IF EXISTS public.create_public_element_access_for_data_type(elementName VARCHAR(255));
DROP FUNCTION IF EXISTS public.create_new_data_type_security(elementName VARCHAR(255), singularDesc VARCHAR(255), pluralDesc VARCHAR(255), codeDesc VARCHAR(255));
DROP FUNCTION IF EXISTS public.create_new_data_type_permissions(elementName VARCHAR(255));
DROP FUNCTION IF EXISTS public.fix_missing_public_element_access_mappings();
DROP FUNCTION IF EXISTS public.fix_mismatched_data_type_permissions();
DROP FUNCTION IF EXISTS public.drop_xnat_hash_indices(recreate BOOLEAN);
DROP FUNCTION IF EXISTS public.object_exists_in_table(id VARCHAR(255), data_type TEXT);
DROP FUNCTION IF EXISTS public.find_orphaned_data();
DROP FUNCTION IF EXISTS public.resolve_orphaned_data();
DROP FUNCTION IF EXISTS public.scan_exists_in_table(xnat_imagescandata_id INTEGER, data_type TEXT);
DROP FUNCTION IF EXISTS public.find_orphaned_scans();
DROP FUNCTION IF EXISTS public.resolve_orphaned_scans();
DROP FUNCTION IF EXISTS public.fix_orphaned_scans();
DROP FUNCTION IF EXISTS public.correct_experiment_data_types();
DROP FUNCTION IF EXISTS public.data_type_fns_create_public_element_access(elementName VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_create_new_security(elementName VARCHAR(255), singularDesc VARCHAR(255), pluralDesc VARCHAR(255), codeDesc VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_create_new_permissions(elementName VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_fix_missing_public_element_access_mappings();
DROP FUNCTION IF EXISTS public.data_type_fns_fix_mismatched_permissions();
DROP FUNCTION IF EXISTS public.data_type_fns_drop_xnat_hash_indices(recreate BOOLEAN);
DROP FUNCTION IF EXISTS public.data_type_fns_object_exists_in_table(id VARCHAR(255), data_type TEXT);
DROP FUNCTION IF EXISTS public.data_type_fns_find_orphaned_data();
DROP FUNCTION IF EXISTS public.data_type_fns_resolve_orphaned_data();
DROP FUNCTION IF EXISTS public.data_type_fns_scan_exists_in_table(xnat_imagescandata_id INTEGER, data_type TEXT);
DROP FUNCTION IF EXISTS public.data_type_fns_find_orphaned_scans();
DROP FUNCTION IF EXISTS public.data_type_fns_resolve_orphaned_scans();
DROP FUNCTION IF EXISTS public.data_type_fns_fix_orphaned_scans();
DROP FUNCTION IF EXISTS public.data_type_fns_correct_experiment_extension();
DROP FUNCTION IF EXISTS public.data_type_fns_correct_group_permissions();
DROP VIEW IF EXISTS public.secured_identified_data_types;
DROP VIEW IF EXISTS public.scan_data_types;
DROP VIEW IF EXISTS public.get_xnat_hash_indices;
DROP VIEW IF EXISTS public.data_type_views_mismatched_mapping_elements;
DROP VIEW IF EXISTS public.data_type_views_missing_mapping_elements;
DROP VIEW IF EXISTS public.data_type_views_element_access;
DROP VIEW IF EXISTS public.data_type_views_orphaned_field_sets;
DROP VIEW IF EXISTS public.data_type_views_secured_identified_data_types;
DROP VIEW IF EXISTS public.data_type_views_scan_data_types;
DROP VIEW IF EXISTS public.data_type_views_get_xnat_hash_indices;
DROP VIEW IF EXISTS public.data_type_views_experiments_without_data_type;
DROP VIEW IF EXISTS public.data_type_views_member_edit_permissions;

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
SELECT
    m.xdat_field_mapping_id AS id
FROM
    xdat_field_mapping m
        LEFT JOIN xdat_field_mapping_set s ON m.xdat_field_mapping_set_xdat_field_mapping_set_id = s.xdat_field_mapping_set_id
        LEFT JOIN xdat_element_access a ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
WHERE
        m.field NOT LIKE a.element_name || '%';

CREATE OR REPLACE VIEW public.data_type_views_missing_mapping_elements AS
    WITH
        public_project_access_mappings AS (SELECT
                                               field_value,
                                               element_name,
                                               field,
                                               xdat_field_mapping_set_id
                                           FROM
                                               data_type_views_element_access
                                           WHERE
                                               element_name != 'xnat:projectData' AND
                                               entity = 'user:guest')
    SELECT
        f.primary_security_field AS field,
        m.field_value,
        m.xdat_field_mapping_set_id
    FROM
        xdat_primary_security_field f
        LEFT JOIN public_project_access_mappings m ON f.primary_security_fields_primary_element_name = element_name
    WHERE
        f.primary_security_fields_primary_element_name != 'xnat:projectData' AND
        m.xdat_field_mapping_set_id IS NOT NULL AND
        (m.field_value IS NULL OR
         (f.primary_security_fields_primary_element_name, f.primary_security_field) NOT IN (SELECT
                                                                                                m.element_name,
                                                                                                m.field
                                                                                            FROM
                                                                                                public_project_access_mappings m));

CREATE OR REPLACE VIEW public.data_type_views_orphaned_field_sets AS
SELECT
    s.xdat_field_mapping_set_id AS id
FROM
    xdat_element_access a
        LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
        LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
WHERE
    s.xdat_field_mapping_set_id IS NOT NULL AND
    m.xdat_field_mapping_id IS NULL;

CREATE OR REPLACE VIEW public.data_type_views_secured_identified_data_types AS
    WITH
        secure_elements AS (SELECT
                                s.element_name AS element_name,
                                regexp_replace(s.element_name, '[^A-z0-9]', '_', 'g') AS table_name
                            FROM
                                xdat_element_security s
                            WHERE
                                    s.secure = 1)
    SELECT
        e.element_name,
        e.table_name
    FROM
        secure_elements e
            LEFT JOIN information_schema.columns c ON lower(e.table_name) = lower(c.table_name) AND column_name = 'id'
    WHERE c.column_name IS NOT NULL;

CREATE OR REPLACE VIEW public.data_type_views_scan_data_types AS
    WITH
        data_elements AS (SELECT
                              element_name,
                              regexp_replace(element_name, '[^A-z0-9]', '_', 'g') AS table_name
                          FROM
                              xdat_meta_element
                          WHERE
                                  lower(element_name) LIKE '%scan%' AND element_name ~ '^([^:]+:[^_]+)$')
    SELECT DISTINCT
        element_name,
        table_name
    FROM
        (SELECT
             e.element_name,
             e.table_name
         FROM
             data_elements e
                 LEFT JOIN information_schema.columns c ON lower(e.table_name) = lower(c.table_name) AND (column_name = 'id' OR column_name LIKE '%_id')
         WHERE c.column_name IS NOT NULL) SOURCE;

CREATE OR REPLACE VIEW public.data_type_views_experiments_without_data_type AS
SELECT DISTINCT
    e.id AS experiment_id,
    w.data_type,
    xme.xdat_meta_element_id AS xdat_meta_element_id
FROM
    xnat_experimentdata e
        LEFT JOIN xdat_meta_element m ON e.extension = m.xdat_meta_element_id
        LEFT JOIN wrk_workflowdata w ON e.id = w.id
        LEFT JOIN xdat_meta_element xme ON w.data_type = xme.element_name
WHERE m.element_name IS NULL
GROUP BY
    e.id,
    w.data_type,
    xme.xdat_meta_element_id;

CREATE OR REPLACE VIEW public.data_type_views_member_edit_permissions AS
SELECT
    m.xdat_field_mapping_id AS field_map_id,
    m.field_value AS project_id,
    g.id AS group_id
FROM
    xdat_field_mapping m
        LEFT JOIN xdat_field_mapping_set s ON m.xdat_field_mapping_set_xdat_field_mapping_set_id = s.xdat_field_mapping_set_id
        LEFT JOIN xdat_element_access e ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = e.xdat_element_access_id
        LEFT JOIN xdat_usergroup g ON e.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
WHERE
        m.field = 'xnat:projectData/ID' AND
        m.edit_element = 1 AND
        g.id LIKE '%_member';

CREATE OR REPLACE FUNCTION public.data_type_fns_create_public_element_access(elementName VARCHAR(255))
    RETURNS BOOLEAN
    LANGUAGE plpgsql
AS
$_$
BEGIN
    -- Creates new element access entry for the element associated with the guest user.
    INSERT INTO xdat_element_access (element_name, xdat_user_xdat_user_id)
    SELECT
        elementName AS element_name,
        u.xdat_user_id
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
    SELECT
        'OR' AS method,
        a.xdat_element_access_id
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
        public_projects AS (SELECT
                                id AS project
                            FROM
                                project_access
                            WHERE
                                    accessibility = 'public')
    SELECT
        f.primary_security_field,
        p.project,
        0,
        1,
        0,
        0,
        1,
        'equals',
        s.xdat_field_mapping_set_id
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

CREATE OR REPLACE FUNCTION public.data_type_fns_create_new_security(elementName VARCHAR(255), singularDesc VARCHAR(255), pluralDesc VARCHAR(255), codeDesc VARCHAR(255))
    RETURNS VARCHAR(255)
    LANGUAGE plpgsql
AS
$_$
BEGIN
    INSERT INTO xdat_element_security (element_name, singular, plural, code, secondary_password, secure_ip, secure, browse, sequence, quarantine, pre_load, searchable, secure_read, secure_edit, secure_create, secure_delete, accessible, usage, category, element_security_set_element_se_xdat_security_id)
    SELECT
        elementName,
        singularDesc,
        pluralDesc,
        codeDesc,
        secondary_password,
        secure_ip,
        secure,
        browse,
        sequence,
        quarantine,
        pre_load,
        searchable,
        secure_read,
        secure_edit,
        secure_create,
        secure_delete,
        accessible,
        usage,
        category,
        element_security_set_element_se_xdat_security_id
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

CREATE OR REPLACE FUNCTION public.data_type_fns_create_new_permissions(elementName VARCHAR(255))
    RETURNS BOOLEAN
    LANGUAGE plpgsql
AS
$_$
DECLARE
    has_public_projects BOOLEAN;
BEGIN
    INSERT INTO xdat_element_access (element_name, xdat_usergroup_xdat_usergroup_id)
    SELECT
        elementName AS element_name,
        xdat_usergroup_id
    FROM
        xdat_usergroup g
        LEFT JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id AND a.element_name = elementName
    WHERE
        a.element_name IS NULL AND
        (g.tag IS NOT NULL OR id = 'ALL_DATA_ADMIN' OR id = 'ALL_DATA_ACCESS');

    INSERT INTO xdat_field_mapping_set (method, permissions_allow_set_xdat_elem_xdat_element_access_id)
    SELECT
        'OR' AS method,
        xdat_element_access_id
    FROM
        xdat_element_access a
        LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
    WHERE
        a.element_name = elementName AND
        s.method IS NULL;

    INSERT INTO xdat_field_mapping (field_value, field, create_element, read_element, edit_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
    SELECT
        field_value,
        field,
        CASE WHEN is_shared THEN create_shared ELSE create_element END AS create_element,
        CASE WHEN is_shared THEN read_shared ELSE read_element END AS read_element,
        CASE WHEN is_shared THEN edit_shared ELSE edit_element END AS edit_element,
        CASE WHEN is_shared THEN delete_shared ELSE delete_element END AS delete_element,
        CASE WHEN is_shared THEN active_shared ELSE active_element END AS active_element,
        comparison_type,
        xdat_field_mapping_set_id
    FROM
        (WITH
             group_permissions AS (SELECT
                                       groupNameOrId,
                                       create_element,
                                       read_element,
                                       edit_element,
                                       delete_element,
                                       active_element,
                                       create_shared,
                                       read_shared,
                                       edit_shared,
                                       delete_shared,
                                       active_shared
                                   FROM
                                       (VALUES
                                        ('Owners', 1, 1, 1, 1, 1, 0, 1, 0, 0, 1),
                                        ('Members', 1, 1, 1, 0, 0, 0, 1, 0, 0, 0),
                                        ('Collaborators', 0, 1, 0, 0, 0, 0, 1, 0, 0, 0),
                                        ('ALL_DATA_ADMIN', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1),
                                        ('ALL_DATA_ACCESS', 0, 1, 0, 0, 1, 0, 1, 0, 0, 1)) AS groupNames (groupNameOrId, create_element, read_element, edit_element, delete_element, active_element, create_shared, read_shared, edit_shared, delete_shared, active_shared))
         SELECT
             f.primary_security_field AS field,
             f.primary_security_field LIKE '%/sharing/share/project' AS is_shared,
             coalesce(g.tag, '*') AS field_value,
             p.create_element AS create_element,
             p.read_element AS read_element,
             p.edit_element AS edit_element,
             p.delete_element AS delete_element,
             p.active_element AS active_element,
             p.create_shared AS create_shared,
             p.read_shared AS read_shared,
             p.edit_shared AS edit_shared,
             p.delete_shared AS delete_shared,
             p.active_shared AS active_shared,
             'equals' AS comparison_type,
             s.xdat_field_mapping_set_id AS xdat_field_mapping_set_id
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

    SELECT
            count(*) > 0 INTO has_public_projects
    FROM
        project_access
    WHERE
            accessibility = 'public';

    IF has_public_projects
    THEN
        PERFORM data_type_fns_create_public_element_access(elementName);
    END IF;
    RETURN TRUE;
END
$_$;

CREATE OR REPLACE FUNCTION public.data_type_fns_fix_missing_public_element_access_mappings()
    RETURNS INTEGER
    LANGUAGE plpgsql
AS
$_$
DECLARE
    has_missing_mappings INTEGER;
BEGIN
    SELECT
        count(*) INTO has_missing_mappings
    FROM
        data_type_views_missing_mapping_elements;
    IF has_missing_mappings > 0
    THEN
        INSERT INTO xdat_field_mapping (field, field_value, create_element, read_element, edit_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
        SELECT
            e.field,
            e.field_value,
            0,
            1,
            0,
            0,
            1,
            'equals',
            e.xdat_field_mapping_set_id
        FROM
            data_type_views_missing_mapping_elements e
        WHERE
            e.field_value IS NOT NULL
        UNION
        SELECT
            e.field,
            a.id,
            0,
            1,
            0,
            0,
            1,
            'equals',
            e.xdat_field_mapping_set_id
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

CREATE OR REPLACE FUNCTION public.data_type_fns_fix_mismatched_permissions()
    RETURNS INTEGER
    LANGUAGE plpgsql
AS
$_$
DECLARE
    has_mismatches INTEGER;
    has_missing    INTEGER;
    data_type      VARCHAR(255);
BEGIN
    SELECT count(*) INTO has_mismatches FROM data_type_views_mismatched_mapping_elements;
    SELECT count(*) INTO has_missing FROM data_type_views_missing_mapping_elements;
    IF has_mismatches > 0 OR has_missing > 0
    THEN
        DELETE FROM xdat_field_mapping WHERE xdat_field_mapping_id IN (SELECT id FROM data_type_views_mismatched_mapping_elements);
        DELETE FROM xdat_field_mapping_set WHERE xdat_field_mapping_set_id IN (SELECT id FROM data_type_views_orphaned_field_sets);
        FOR data_type IN SELECT DISTINCT primary_security_fields_primary_element_name AS data_type FROM xdat_primary_security_field WHERE primary_security_fields_primary_element_name NOT IN (SELECT DISTINCT element_name FROM xdat_element_access)
            LOOP
                PERFORM data_type_fns_create_new_permissions(data_type);
            END LOOP;
    END IF;
    RETURN (has_mismatches + has_missing);
END
$_$;

-- Drops all hash indices as returned with the get_hash_indices view. The
-- recreate parameter is true by default and indicates whether each index
-- should be regenerated once it's been dropped.
CREATE OR REPLACE FUNCTION public.data_type_fns_drop_xnat_hash_indices(recreate BOOLEAN DEFAULT TRUE)
    RETURNS INTEGER
AS
$_$
DECLARE
    total_count INTEGER := 0;
BEGIN
    DECLARE
        current_index RECORD;
    BEGIN
        FOR current_index IN SELECT * FROM data_type_views_get_xnat_hash_indices
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

CREATE OR REPLACE FUNCTION public.data_type_fns_object_exists_in_table(id VARCHAR(255), data_type TEXT)
    RETURNS BOOLEAN AS
$$
DECLARE
    exists_in_table BOOLEAN;
BEGIN
    EXECUTE format('SELECT EXISTS(SELECT TRUE FROM %s WHERE id = ''%s'')', regexp_replace(data_type, '[^A-z0-9]', '_', 'g'), id) INTO exists_in_table;
    RETURN exists_in_table;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_find_orphaned_data()
    RETURNS TABLE (
                      project VARCHAR(255),
                      id VARCHAR(255),
                      label VARCHAR(255),
                      element_name VARCHAR(250)
                  ) AS
$$
BEGIN
    RETURN QUERY SELECT
                     x.project,
                     x.id,
                     x.label,
                     e.element_name
                 FROM
                     xnat_experimentdata x
                         LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id
                 WHERE NOT data_type_fns_object_exists_in_table(x.id, e.element_name);
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_resolve_orphaned_data()
    RETURNS TABLE (
                      project VARCHAR(255),
                      id VARCHAR(255),
                      label VARCHAR(255),
                      actual_element_name VARCHAR(250),
                      expected_element_name VARCHAR(255)
                  ) AS
$$
BEGIN
    RETURN QUERY WITH
                     data_types AS (SELECT * FROM data_type_views_secured_identified_data_types)
                 SELECT
                     o.project,
                     o.id,
                     o.label,
                     o.element_name AS actual_element_name,
                     t.element_name AS located_element_name
                 FROM
                     data_type_fns_find_orphaned_data() o
                         LEFT JOIN data_types t ON o.element_name != t.element_name
                 WHERE data_type_fns_object_exists_in_table(o.id, t.element_name);
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_scan_exists_in_table(xnat_imagescandata_id INTEGER, data_type TEXT)
    RETURNS BOOLEAN AS
$$
DECLARE
    exists_in_table BOOLEAN;
BEGIN
    EXECUTE format('SELECT EXISTS(SELECT TRUE FROM %s WHERE xnat_imagescandata_id = %s)', regexp_replace(data_type, '[^A-z0-9]', '_', 'g'), xnat_imagescandata_id) INTO exists_in_table;
    RETURN exists_in_table;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_find_orphaned_scans()
    RETURNS TABLE (
                      project VARCHAR(255),
                      label VARCHAR(255),
                      id VARCHAR(255),
                      xnat_imagescandata_id INTEGER,
                      modality VARCHAR(255),
                      type VARCHAR(255),
                      series_description VARCHAR(255),
                      element_name VARCHAR(250)
                  ) AS
$$
BEGIN
    RETURN QUERY SELECT
                     s.project,
                     x.label,
                     s.id,
                     s.xnat_imagescandata_id,
                     s.modality,
                     s.type,
                     s.series_description,
                     e.element_name
                 FROM
                     xnat_imagescandata s
                         LEFT JOIN xdat_meta_element e ON s.extension = e.xdat_meta_element_id
                         LEFT JOIN xnat_imagesessiondata i ON s.image_session_id = i.id
                         LEFT JOIN xnat_experimentdata x ON i.id = x.id
                 WHERE NOT data_type_fns_scan_exists_in_table(s.xnat_imagescandata_id, e.element_name);
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_resolve_orphaned_scans()
    RETURNS TABLE (
                      project VARCHAR(255),
                      label VARCHAR(255),
                      id VARCHAR(255),
                      xnat_imagescandata_id INTEGER,
                      modality VARCHAR(255),
                      type VARCHAR(255),
                      series_description VARCHAR(255),
                      actual_element_name VARCHAR(250),
                      expected_element_name VARCHAR(255)
                  ) AS
$$
BEGIN
    RETURN QUERY WITH
                     data_types AS (SELECT *
                                    FROM
                                        data_type_views_scan_data_types t
                                            LEFT JOIN information_schema.columns c ON lower(t.table_name) = lower(c.table_name)
                                    WHERE element_name LIKE '%ScanData%' AND element_name NOT LIKE 'xnat:imageScanData%' AND column_name = 'xnat_imagescandata_id')
                 SELECT
                     o.project,
                     o.label,
                     o.id,
                     o.xnat_imagescandata_id,
                     o.modality,
                     o.type,
                     o.series_description,
                     o.element_name AS actual_element_name,
                     t.element_name AS located_element_name
                 FROM
                     data_type_fns_find_orphaned_scans() o
                         LEFT JOIN data_types t ON o.element_name != t.element_name
                 WHERE data_type_fns_scan_exists_in_table(o.xnat_imagescandata_id, t.element_name);
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_fix_orphaned_scans()
    RETURNS INTEGER AS
$$
DECLARE
    fixed_orphan_count INTEGER;
BEGIN
    UPDATE xnat_imagescandata s
    SET
        extension = orphans.xdat_meta_element_id
    FROM
        (SELECT
             o.xnat_imagescandata_id,
             m.xdat_meta_element_id
         FROM
             data_type_fns_resolve_orphaned_scans() o
                 LEFT JOIN xdat_meta_element m ON o.expected_element_name = m.element_name) orphans
    WHERE s.xnat_imagescandata_id = orphans.xnat_imagescandata_id;
    GET DIAGNOSTICS fixed_orphan_count = ROW_COUNT;
    RETURN fixed_orphan_count;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_correct_experiment_extension()
    RETURNS TABLE (
                      orphaned_experiment VARCHAR(255),
                      original_data_type VARCHAR(255)
                  ) AS
$$
BEGIN
    WITH
        orphans AS (SELECT * FROM data_type_views_experiments_without_data_type WHERE xdat_meta_element_id IS NOT NULL)
    UPDATE xnat_experimentdata
    SET
        extension = xdat_meta_element_id
    FROM
        orphans
    WHERE id = experiment_id;

    RETURN QUERY SELECT
                     experiment_id,
                     data_type
                 FROM
                     data_type_views_experiments_without_data_type
                 WHERE xdat_meta_element_id IS NULL;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_correct_group_permissions()
    RETURNS INTEGER
AS
$_$
DECLARE
    current_index RECORD;
    total_count   INTEGER := 0;
BEGIN
    FOR current_index IN SELECT * FROM data_type_views_member_edit_permissions
        LOOP
            total_count := total_count + 1;
            RAISE NOTICE '%. Disabling edit permissions for field mapping set ID % for project % group %', total_count, current_index.field_map_id, current_index.project_id, current_index.group_id;
            UPDATE xdat_field_mapping SET edit_element = 0 WHERE xdat_field_mapping_id = current_index.field_map_id;
        END LOOP;

    RETURN total_count;
END;
$_$
    LANGUAGE plpgsql;
