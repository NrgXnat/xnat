--
-- web: src/main/resources/META-INF/xnat/scripts/init_data_type_access_functions.sql
-- XNAT http://www.xnat.org
-- Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
-- All Rights Reserved
--  
-- Released under the Simplified BSD.
--

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
DROP FUNCTION IF EXISTS public.data_type_fns_can(username VARCHAR(255), entityId VARCHAR(255), ACTION VARCHAR(15));
DROP FUNCTION IF EXISTS public.data_type_fns_can(username VARCHAR(255), ACTION VARCHAR(15), entityId VARCHAR(255), projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_get_secured_property_permissions(username VARCHAR(255), projectId VARCHAR(255), securedProperty VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_can_action_entity(username VARCHAR(255), action VARCHAR(15), entityId VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_get_entity_permissions(username VARCHAR(255), entityId VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_get_entity_permissions(username VARCHAR(255), entityId VARCHAR(255), projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_get_entity_projects(entityId VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_get_entity_projects(entityId VARCHAR(255), projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_get_all_accessible_expts_of_type(username VARCHAR(255), dataType VARCHAR(255));
DROP FUNCTION IF EXISTS public.data_type_fns_get_user_access_by_project(username VARCHAR(255));
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
DROP VIEW IF EXISTS public.data_type_views_get_all_expts;
DROP VIEW IF EXISTS public.data_type_views_missing_or_misconfigured_permissions;
DROP VIEW IF EXISTS public.data_type_views_default_access_permissions;

CREATE OR REPLACE VIEW public.data_type_views_default_access_permissions AS
SELECT
    groupNameOrId,
    is_source,
    create_element,
    read_element,
    edit_element,
    delete_element,
    active_element
FROM
    (VALUES
         ('Owners', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE),
         ('Owners', FALSE, FALSE, TRUE, FALSE, FALSE, TRUE),
         ('Members', TRUE, TRUE, TRUE, TRUE, FALSE, TRUE),
         ('Members', FALSE, FALSE, TRUE, FALSE, FALSE, TRUE),
         ('Collaborators', TRUE, FALSE, TRUE, FALSE, FALSE, TRUE),
         ('Collaborators', FALSE, FALSE, TRUE, FALSE, FALSE, TRUE),
         ('ALL_DATA_ADMIN', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE),
         ('ALL_DATA_ADMIN', FALSE, TRUE, TRUE, TRUE, TRUE, TRUE),
         ('ALL_DATA_ACCESS', TRUE, FALSE, TRUE, FALSE, FALSE, TRUE),
         ('ALL_DATA_ACCESS', FALSE, FALSE, TRUE, FALSE, FALSE, TRUE)) AS groupNames (groupNameOrId, is_source, create_element, read_element, edit_element, delete_element, active_element);

CREATE OR REPLACE VIEW public.data_type_views_missing_or_misconfigured_permissions AS
WITH
    existing_data_access AS (
        SELECT
            g.id || ':' || field AS key,
            g.id AS group_id,
            a.element_name,
            m.field,
            m.field_value,
            m.read_element::BOOLEAN,
            m.edit_element::BOOLEAN,
            m.create_element::BOOLEAN,
            m.delete_element::BOOLEAN,
            m.active_element::BOOLEAN
        FROM
            xdat_usergroup g
            LEFT JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id
            LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
            LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
        WHERE
            a.element_name != 'xnat:projectData'),
    all_groups AS (
        SELECT
            g.id,
            g.displayname
        FROM
            xdat_usergroup g
            LEFT JOIN xnat_projectdata p ON g.tag = p.id
        WHERE
            p.id IS NOT NULL AND g.displayname IN ('Owners', 'Members', 'Collaborators') OR
            g.id IN ('ALL_DATA_ADMIN', 'ALL_DATA_ACCESS')),
    all_secured_types AS (
        SELECT
            element_name
        FROM
            xdat_element_security
        WHERE
            secure = 1 AND
            element_name != 'xnat:projectData'),
    all_group_elements AS (
        SELECT
            g.id AS group_id,
            g.displayname AS displayname,
            t.element_name
        FROM
            all_groups g,
            all_secured_types t),
    required_data_access AS (
        SELECT
            group_id || ':' || element_name || '/project' AS key,
            group_id,
            displayname,
            element_name,
            element_name || '/project' AS field
        FROM
            all_group_elements
        UNION
        SELECT
            group_id || ':' || element_name || '/sharing/share/project' AS key,
            group_id,
            displayname,
            element_name,
            element_name || '/sharing/share/project' AS field
        FROM
            all_group_elements),
    unified AS (
        SELECT
            r.key AS required_key,
            r.group_id AS required_group_id,
            r.displayname AS required_displayname,
            r.element_name AS required_element_name,
            r.field AS required_field,
            CASE WHEN r.field LIKE '%/sharing/share/project' THEN FALSE ELSE TRUE END AS is_source,
            e.key AS existing_key,
            e.group_id AS existing_group_id,
            e.element_name AS existing_element_name,
            e.field AS existing_field,
            e.field_value AS existing_field_value,
            e.read_element::BOOLEAN,
            e.edit_element::BOOLEAN,
            e.create_element::BOOLEAN,
            e.delete_element::BOOLEAN,
            e.active_element::BOOLEAN
        FROM
            required_data_access r
            LEFT JOIN existing_data_access e ON r.key = e.key),
    unified_permissions AS (
        SELECT
            required_key,
            required_group_id,
            required_displayname,
            required_element_name,
            required_field,
            u.is_source,
            existing_key,
            existing_group_id,
            existing_element_name,
            existing_field,
            existing_field_value,
            u.read_element,
            u.edit_element,
            u.create_element,
            u.delete_element,
            u.active_element,
            p.read_element AS default_read,
            p.edit_element AS default_edit,
            p.create_element AS default_create,
            p.delete_element AS default_delete,
            p.active_element AS default_active
        FROM
            unified u
            LEFT JOIN data_type_views_default_access_permissions p ON p.groupNameOrId IN (u.required_group_id, u.required_displayname) AND p.is_source = u.is_source)
SELECT *
FROM
    unified_permissions
WHERE
    existing_key IS NULL OR
    existing_key IS NOT NULL AND (create_element != default_create OR read_element != default_read OR edit_element != default_edit OR delete_element != default_delete OR active_element != default_active);

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
                                            ('Members', 1, 1, 1, 0, 1, 0, 1, 0, 0, 1),
                                            ('Collaborators', 0, 1, 0, 0, 1, 0, 1, 0, 0, 1),
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
        count(*) > 0
    INTO has_public_projects
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
        count(*)
    INTO has_missing_mappings
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

-- Gets all hash indices in the public schema along with the CREATE INDEX
-- statements required to regenerate the indices.
CREATE OR REPLACE VIEW public.data_type_views_get_xnat_hash_indices AS
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
$_$
DECLARE
    exists_in_table BOOLEAN;
BEGIN
    EXECUTE format('SELECT EXISTS(SELECT TRUE FROM %s WHERE id = ''%s'')', regexp_replace(data_type, '[^A-z0-9]', '_', 'g'), id) INTO exists_in_table;
    RETURN exists_in_table;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_find_orphaned_data()
    RETURNS TABLE (
        project      VARCHAR(255),
        id           VARCHAR(255),
        label        VARCHAR(255),
        element_name VARCHAR(250))
AS
$_$
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
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_resolve_orphaned_data()
    RETURNS TABLE (
        project               VARCHAR(255),
        id                    VARCHAR(255),
        label                 VARCHAR(255),
        actual_element_name   VARCHAR(250),
        expected_element_name VARCHAR(255))
AS
$_$
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
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_scan_exists_in_table(xnat_imagescandata_id INTEGER, data_type TEXT)
    RETURNS BOOLEAN AS
$_$
DECLARE
    exists_in_table BOOLEAN;
BEGIN
    EXECUTE format('SELECT EXISTS(SELECT TRUE FROM %s WHERE xnat_imagescandata_id = %s)', regexp_replace(data_type, '[^A-z0-9]', '_', 'g'), xnat_imagescandata_id) INTO exists_in_table;
    RETURN exists_in_table;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_find_orphaned_scans()
    RETURNS TABLE (
        project               VARCHAR(255),
        label                 VARCHAR(255),
        id                    VARCHAR(255),
        xnat_imagescandata_id INTEGER,
        modality              VARCHAR(255),
        type                  VARCHAR(255),
        series_description    VARCHAR(255),
        element_name          VARCHAR(250))
AS
$_$
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
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_resolve_orphaned_scans()
    RETURNS TABLE (
        project               VARCHAR(255),
        label                 VARCHAR(255),
        id                    VARCHAR(255),
        xnat_imagescandata_id INTEGER,
        modality              VARCHAR(255),
        type                  VARCHAR(255),
        series_description    VARCHAR(255),
        actual_element_name   VARCHAR(250),
        expected_element_name VARCHAR(255))
AS
$_$
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
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_fix_orphaned_scans()
    RETURNS INTEGER AS
$_$
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
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_correct_experiment_extension()
    RETURNS TABLE (
        orphaned_experiment VARCHAR(255),
        original_data_type  VARCHAR(255))
AS
$_$
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
$_$
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

CREATE OR REPLACE FUNCTION public.data_type_fns_get_entity_projects(entityId VARCHAR(255), projectId VARCHAR(255) DEFAULT NULL)
    RETURNS TABLE (
        field_value  VARCHAR(255),
        element_name VARCHAR(255),
        field        VARCHAR(255))
AS
$_$
DECLARE
    resolvedEntityId VARCHAR(255) DEFAULT NULL;
BEGIN
    IF projectId IS NULL
    THEN
        SELECT id FROM xnat_experimentdata WHERE id = entityId INTO resolvedEntityId;
    ELSE
        SELECT
            x.id
        FROM
            xnat_experimentdata x
            LEFT JOIN xnat_experimentdata_share s ON x.id = s.sharing_share_xnat_experimentda_id
        WHERE
            (x.label = entityId OR x.id = entityId) AND x.project = projectId OR
            (s.label = entityId OR s.sharing_share_xnat_experimentda_id = entityId) AND s.project = projectId
        INTO resolvedEntityId;
    END IF;
    IF resolvedEntityId IS NOT NULL
    THEN
        RETURN QUERY
            SELECT
                x.project AS field_value,
                e.element_name::VARCHAR(255),
                (e.element_name || '/project')::VARCHAR(255) AS field
            FROM
                xnat_experimentdata x
                LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id
            WHERE id = resolvedEntityId
            UNION
            SELECT
                s.project AS field_value,
                e.element_name::VARCHAR(255),
                e.element_name || '/sharing/share/project' AS field
            FROM
                xnat_experimentdata_share s
                LEFT JOIN xnat_experimentdata x ON s.sharing_share_xnat_experimentda_id = x.id
                LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id
            WHERE sharing_share_xnat_experimentda_id = resolvedEntityId;
    END IF;

    IF projectId IS NULL
    THEN
        SELECT id FROM xnat_subjectdata WHERE id = entityId INTO resolvedEntityId;
    ELSE
        SELECT
            s.id
        FROM
            xnat_subjectdata s
            LEFT JOIN xnat_projectparticipant p ON s.id = p.subject_id
        WHERE
            (s.label = entityId OR s.id = entityId) AND s.project = projectId OR
            (p.label = entityId OR p.subject_id = entityId) AND p.project = projectId
        INTO resolvedEntityId;
    END IF;
    IF resolvedEntityId IS NOT NULL
    THEN
        RETURN QUERY
            SELECT
                project AS field_value,
                'xnat:subjectData'::VARCHAR(255) AS element_name,
                'xnat:subjectData/project'::VARCHAR(255) AS field
            FROM
                xnat_subjectdata
            WHERE id = resolvedEntityId
            UNION
            SELECT
                project AS field_value,
                'xnat:subjectData'::VARCHAR(255) AS element_name,
                'xnat:subjectData/sharing/share/project'::VARCHAR(255) AS field
            FROM
                xnat_projectparticipant
            WHERE subject_id = resolvedEntityId;
    END IF;

    SELECT id FROM xnat_projectdata WHERE id = entityId INTO resolvedEntityId;
    IF resolvedEntityId IS NOT NULL
    THEN
        RETURN QUERY
            SELECT
                id AS field_value,
                'xnat:projectData'::VARCHAR(255) AS element_name,
                'xnat:projectData/ID'::VARCHAR(255) AS field
            FROM
                xnat_projectdata
            WHERE id = resolvedEntityId;
    END IF;

    RETURN;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_get_entity_permissions(username VARCHAR(255), entityId VARCHAR(255), projectId VARCHAR(255) DEFAULT NULL)
    RETURNS TABLE (
        id          VARCHAR(255),
        field       VARCHAR(255),
        field_value VARCHAR(255),
        can_read    BOOLEAN,
        can_edit    BOOLEAN,
        can_create  BOOLEAN,
        can_delete  BOOLEAN,
        can_active  BOOLEAN)
AS
$_$
BEGIN
    RETURN QUERY
        WITH
            entity_projects AS (SELECT * FROM data_type_fns_get_entity_projects(entityId, projectId))
        SELECT
            coalesce(g.id, u.login, gu.login) AS id,
            m.field,
            m.field_value,
            m.read_element::BOOLEAN,
            m.edit_element::BOOLEAN,
            m.create_element::BOOLEAN,
            m.delete_element::BOOLEAN,
            m.active_element::BOOLEAN
        FROM
            xdat_field_mapping m
            LEFT JOIN xdat_field_mapping_set s ON m.xdat_field_mapping_set_xdat_field_mapping_set_id = s.xdat_field_mapping_set_id
            LEFT JOIN xdat_element_access a ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
            LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
            LEFT JOIN xdat_user_groupid gi ON g.id = gi.groupid
            LEFT JOIN xdat_user gu ON gi.groups_groupid_xdat_user_xdat_user_id = gu.xdat_user_id
            LEFT JOIN xdat_user u ON a.xdat_user_xdat_user_id = u.xdat_user_id
            LEFT JOIN entity_projects p ON m.field = p.field AND m.field_value IN (p.field_value, '*')
        WHERE
            p.field IS NOT NULL AND
            (gu.login = username OR u.login IN ('guest', username));
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_can_action_entity(username VARCHAR(255), action VARCHAR(15), entityId VARCHAR(255))
    RETURNS BOOLEAN
AS
$_$
DECLARE
    found_can BOOLEAN;
BEGIN
    EXECUTE format('SELECT coalesce(bool_or(can_%1$s), FALSE) AS can_%1$s FROM data_type_fns_get_entity_permissions(''%2$s'', ''%3$s'')', action, username, entityId)
        INTO found_can;
    RETURN found_can;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_get_secured_property_permissions(username VARCHAR(255), projectId VARCHAR(255), securedProperty VARCHAR(255))
    RETURNS TABLE (
        can_read   BOOLEAN,
        can_edit   BOOLEAN,
        can_create BOOLEAN,
        can_delete BOOLEAN,
        can_active BOOLEAN)
AS
$_$
BEGIN
    RETURN QUERY
        SELECT
            coalesce(bool_or(read_element::BOOLEAN), FALSE) AS can_read,
            coalesce(bool_or(edit_element::BOOLEAN), FALSE) AS can_edit,
            coalesce(bool_or(create_element::BOOLEAN), FALSE) AS can_create,
            coalesce(bool_or(delete_element::BOOLEAN), FALSE) AS can_delete,
            coalesce(bool_or(active_element::BOOLEAN), FALSE) AS can_active
        FROM
            xdat_user u
            LEFT JOIN xdat_user_groupid i ON u.xdat_user_id = i.groups_groupid_xdat_user_xdat_user_id
            LEFT JOIN xdat_usergroup g ON i.groupid = g.id
            LEFT JOIN xdat_element_access a ON u.xdat_user_id = a.xdat_user_xdat_user_id OR g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id
            LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
            LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
        WHERE
                u.login = username AND
                m.field_value IN (projectId, '*') AND
                m.field = securedProperty;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_can(username VARCHAR(255), action VARCHAR(15), entityId VARCHAR(255), projectId VARCHAR(255) DEFAULT NULL)
    RETURNS BOOLEAN
AS
$_$
DECLARE
    found_can    BOOLEAN;
    field_count  INTEGER;
    fields       TEXT;
    field_values TEXT;
BEGIN
    IF projectId IS NULL
    THEN
        SELECT * FROM data_type_fns_can_action_entity(username, action, entityId) INTO found_can;
    ELSE
        EXECUTE format('WITH ' ||
                       '    permissions AS ' ||
                       '        (SELECT ' ||
                       '             cardinality(array_agg(DISTINCT field)) AS field_count, ' ||
                       '             array_to_string(array_agg(DISTINCT field), '' '') AS fields, ' ||
                       '             array_to_string(array_agg(DISTINCT field_value), '' '') AS field_values, ' ||
                       '             coalesce(bool_or(can_%1$s), FALSE) AS can_%1$s, ' ||
                       '             array_agg(field_value) AS projects ' ||
                       '         FROM ' ||
                       '             data_type_fns_get_entity_permissions(''%2$s'', ''%3$s'', ''%4$s'')) ' ||
                       'SELECT ' ||
                       '    p.field_count, ' ||
                       '    p.fields, ' ||
                       '    p.field_values, ' ||
                       '    CASE WHEN ARRAY [''%4$s''::VARCHAR(255), ''*''::VARCHAR(255)] && projects THEN p.can_%1$s ELSE FALSE END AS can_%1$s ' ||
                       'FROM ' ||
                       '    permissions p', action, username, entityId, projectId)
            INTO field_count, fields, field_values, found_can;

        -- Delete with sharing is a special case: if the user can't delete the entity, but
        -- that entity is shared they may be able to unshare the entity if they can delete
        -- entities of the same data type in the project.
        IF action = 'delete' AND found_can = FALSE AND field_count = 1 AND fields LIKE '%/sharing/share/project'
        THEN
            EXECUTE format('SELECT can_%1$s FROM data_type_fns_get_secured_property_permissions(''%2$s'', ''%3$s'', ''%4$s'')', action, username, projectId, split_part(fields, '/', 1) || '/project')
                INTO found_can;
        END IF;
    END IF;
    RETURN found_can;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE VIEW public.data_type_views_get_all_expts AS
SELECT
    x.id,
    x.label,
    x.project,
    x.extension,
    e.element_name,
    FALSE AS shared,
    '^[^/]+/project' AS attribute
FROM
    xnat_experimentdata x
    LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id
UNION
SELECT
    x.id,
    s.label,
    s.project,
    x.extension,
    e.element_name,
    TRUE AS shared,
    '^[^/]+/sharing/share/project' AS attribute
FROM
    xnat_experimentdata_share s
    LEFT JOIN xnat_experimentdata x ON s.sharing_share_xnat_experimentda_id = x.id
    LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id;

CREATE OR REPLACE FUNCTION public.data_type_fns_get_all_accessible_expts_of_type(username VARCHAR(255), dataType VARCHAR(255))
    RETURNS TABLE (
        id           VARCHAR(255),
        label        VARCHAR(255),
        project      VARCHAR(255),
        element_name VARCHAR(255),
        shared       BOOLEAN,
        can_create   BOOLEAN,
        can_read     BOOLEAN,
        can_edit     BOOLEAN,
        can_delete   BOOLEAN,
        can_active   BOOLEAN)
AS
$_$
BEGIN
    RETURN QUERY
        WITH
            all_expts AS (SELECT * FROM data_type_views_get_all_expts)
        SELECT DISTINCT
            x.id,
            x.label,
            x.project,
            x.element_name,
            x.shared,
            m.create_element::BOOLEAN,
            m.read_element::BOOLEAN,
            m.edit_element::BOOLEAN,
            m.delete_element::BOOLEAN,
            m.active_element::BOOLEAN
        FROM
            all_expts x
            LEFT JOIN xdat_element_access e ON x.element_name = e.element_name
            LEFT JOIN xdat_field_mapping_set s ON e.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
            LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
            LEFT JOIN xdat_usergroup g ON e.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
            LEFT JOIN xdat_user_groupid gi ON g.id = gi.groupid
            LEFT JOIN xdat_user u ON e.xdat_user_xdat_user_id = u.xdat_user_id OR gi.groups_groupid_xdat_user_xdat_user_id = u.xdat_user_id
        WHERE
            m.xdat_field_mapping_id IS NOT NULL AND
            m.field_value IN (x.project, '*') AND
            m.field ~ x.attribute AND
            x.element_name = dataType AND
            u.login IN (username, 'guest')
        ORDER BY
            id;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.data_type_fns_get_user_access_by_project(username VARCHAR(255))

    RETURNS TABLE (
                      data_type    TEXT,
                      project      VARCHAR(255),
                      shared       BOOLEAN,
                      can_create   BOOLEAN,
                      can_read     BOOLEAN,
                      can_edit     BOOLEAN,
                      can_delete   BOOLEAN,
                      can_active   BOOLEAN)
AS
$_$
BEGIN
    RETURN QUERY
        WITH fa AS
                 (SELECT m.field                   AS field,
                         m.field_value             AS project,
                         m.create_element::BOOLEAN AS can_create,
                         m.read_element::BOOLEAN   AS can_read,
                         m.edit_element::BOOLEAN   AS can_edit,
                         m.delete_element::BOOLEAN AS can_delete,
                         m.active_element::BOOLEAN AS can_active
                  FROM xdat_field_mapping m
                           LEFT JOIN xdat_field_mapping_set s
                                     ON m.xdat_field_mapping_set_xdat_field_mapping_set_id =
                                        s.xdat_field_mapping_set_id
                           LEFT JOIN xdat_element_access a
                                     ON s.permissions_allow_set_xdat_elem_xdat_element_access_id =
                                        a.xdat_element_access_id
                           LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
                           LEFT JOIN xdat_user_groupid i ON g.id = i.groupid
                           LEFT JOIN xdat_user u ON a.xdat_user_xdat_user_id = u.xdat_user_id OR
                                                    i.groups_groupid_xdat_user_xdat_user_id = u.xdat_user_id
                  WHERE u.login IN ('guest', username))
        SELECT 'xnat:projectData' AS data_type,
               fa.project,
               FALSE              AS shared,
               fa.can_create,
               fa.can_read,
               fa.can_edit,
               fa.can_delete,
               fa.can_active
        FROM fa
        WHERE fa.field = 'xnat:projectData/ID'
        UNION
        SELECT REGEXP_REPLACE(fa.field, '(/sharing/share)?/project$', '') AS data_type,
               fa.project,
               fa.field ~ '/sharing/share/project$' AS shared,
               fa.can_create,
               fa.can_read,
               fa.can_edit,
               fa.can_delete,
               fa.can_active
        FROM fa
        WHERE fa.field ~ '(/sharing/share)?/project$'
    ;
END
$_$
    LANGUAGE plpgsql;
