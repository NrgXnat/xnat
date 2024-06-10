--
-- web: src/main/resources/META-INF/xnat/scripts/init_project_group_functions.sql
-- XNAT http://www.xnat.org
-- Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
-- All Rights Reserved
--  
-- Released under the Simplified BSD.
--

DROP FUNCTION IF EXISTS public.project_groups_get_groups(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_create_groups(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_get_element_accesses(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_create_element_accesses(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_get_mapping_sets(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_create_mapping_sets(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_get_mappings(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_create_mappings(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_get_groups_and_permissions(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_create_groups_and_permissions(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.project_groups_fix_irregular_settings();
DROP FUNCTION IF EXISTS public.project_groups_fix_irregular_settings(executeFixQueries BOOLEAN);
DROP FUNCTION IF EXISTS public.project_groups_get_setting_flip_value(elementName VARCHAR(255), mismatchedValue INTEGER);
DROP VIEW IF EXISTS public.project_groups_find_irregular_settings;

CREATE OR REPLACE VIEW public.project_groups_find_irregular_settings AS
SELECT
    tag,
    id,
    xdat_field_mapping_id,
    field,
    concat_ws(', ', mismatched_read, mismatched_edit, mismatched_create, mismatched_delete, mismatched_active) AS mismatched_values,
    mismatched_read_value,
    mismatched_edit_value,
    mismatched_create_value,
    mismatched_delete_value,
    mismatched_active_value
FROM
    (WITH
         secured_elements AS
             (SELECT
                  e.element_name
              FROM
                  xdat_element_security e
              WHERE secure = 1),
         project_ids AS
             (SELECT
                  id
              FROM
                  xnat_projectdata),
         default_perms AS
             (SELECT *
              FROM
                  (VALUES
                   ('project', 'Owners', 1, 1, 1, 1, 1),
                   ('project', 'Members', 1, 0, 0, 0, 0),
                   ('project', 'Collaborators', 1, 0, 0, 0, 0),
                   ('data', 'Owners', 1, 1, 1, 1, 1),
                   ('data', 'Members', 1, 1, 1, 0, 1),
                   ('data', 'Collaborators', 1, 0, 0, 0, 1),
                   ('shared', 'Owners', 1, 0, 0, 0, 1),
                   ('shared', 'Members', 1, 0, 0, 0, 1),
                   ('shared', 'Collaborators', 1, 0, 0, 0, 1)) AS default_perms(scope, display_name, read_element, edit_element, create_element, delete_element, active_element))
     SELECT
         g.tag,
         g.id,
         m.xdat_field_mapping_id,
         m.field,
         CASE WHEN m.read_element != p.read_element THEN m.read_element ELSE NULL END AS mismatched_read_value,
         CASE WHEN m.edit_element != p.edit_element THEN m.edit_element ELSE NULL END AS mismatched_edit_value,
         CASE WHEN m.create_element != p.create_element THEN m.create_element ELSE NULL END AS mismatched_create_value,
         CASE WHEN m.delete_element != p.delete_element THEN m.delete_element ELSE NULL END AS mismatched_delete_value,
         CASE WHEN m.active_element != p.active_element THEN m.active_element ELSE NULL END AS mismatched_active_value,
         CASE WHEN m.read_element != p.read_element THEN 'read' ELSE NULL END AS mismatched_read,
         CASE WHEN m.edit_element != p.edit_element THEN 'edit' ELSE NULL END AS mismatched_edit,
         CASE WHEN m.create_element != p.create_element THEN 'create' ELSE NULL END AS mismatched_create,
         CASE WHEN m.delete_element != p.delete_element THEN 'delete' ELSE NULL END AS mismatched_delete,
         CASE WHEN m.active_element != p.active_element THEN 'active' ELSE NULL END AS mismatched_active
     FROM
         xdat_field_mapping m
         LEFT JOIN xdat_field_mapping_set s ON m.xdat_field_mapping_set_xdat_field_mapping_set_id = s.xdat_field_mapping_set_id
         LEFT JOIN xdat_element_access a ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
         LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
         LEFT JOIN default_perms p ON g.displayname = p.display_name AND p.scope = 'project'
     WHERE
             g.displayname IN ('Owners', 'Members', 'Collaborators') AND
             g.tag IN (SELECT id FROM project_ids) AND
             m.field = 'xnat:projectData/ID' AND
             (m.read_element != p.read_element OR m.edit_element != p.edit_element OR m.create_element != p.create_element OR m.delete_element != p.delete_element OR m.active_element != p.active_element)
     UNION
     SELECT
         g.tag,
         g.id,
         m.xdat_field_mapping_id,
         m.field,
         CASE WHEN m.read_element != p.read_element THEN m.read_element ELSE NULL END AS mismatched_read_value,
         CASE WHEN m.edit_element != p.edit_element THEN m.edit_element ELSE NULL END AS mismatched_edit_value,
         CASE WHEN m.create_element != p.create_element THEN m.create_element ELSE NULL END AS mismatched_create_value,
         CASE WHEN m.delete_element != p.delete_element THEN m.delete_element ELSE NULL END AS mismatched_delete_value,
         CASE WHEN m.active_element != p.active_element THEN m.active_element ELSE NULL END AS mismatched_active_value,
         CASE WHEN m.read_element != p.read_element THEN 'read' ELSE NULL END AS mismatched_read,
         CASE WHEN m.edit_element != p.edit_element THEN 'edit' ELSE NULL END AS mismatched_edit,
         CASE WHEN m.create_element != p.create_element THEN 'create' ELSE NULL END AS mismatched_create,
         CASE WHEN m.delete_element != p.delete_element THEN 'delete' ELSE NULL END AS mismatched_delete,
         CASE WHEN m.active_element != p.active_element THEN 'active' ELSE NULL END AS mismatched_active
     FROM
         xdat_field_mapping m
         LEFT JOIN xdat_field_mapping_set s ON m.xdat_field_mapping_set_xdat_field_mapping_set_id = s.xdat_field_mapping_set_id
         LEFT JOIN xdat_element_access a ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
         LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
         LEFT JOIN default_perms p ON g.displayname = p.display_name AND p.scope = 'data'
     WHERE
             g.displayname IN ('Owners', 'Members', 'Collaborators') AND
             g.tag IN (SELECT id FROM project_ids) AND
             m.field != 'xnat:projectData/ID' AND
             m.field NOT LIKE '%/sharing/share/project' AND
             m.field_value != '*' AND
             (m.read_element != p.read_element OR m.edit_element != p.edit_element OR m.create_element != p.create_element OR m.delete_element != p.delete_element OR m.active_element != p.active_element)
     UNION
     SELECT
         g.tag,
         g.id,
         m.xdat_field_mapping_id,
         m.field,
         CASE WHEN m.read_element != p.read_element THEN m.read_element ELSE NULL END AS mismatched_read_value,
         CASE WHEN m.edit_element != p.edit_element THEN m.edit_element ELSE NULL END AS mismatched_edit_value,
         CASE WHEN m.create_element != p.create_element THEN m.create_element ELSE NULL END AS mismatched_create_value,
         CASE WHEN m.delete_element != p.delete_element THEN m.delete_element ELSE NULL END AS mismatched_delete_value,
         CASE WHEN m.active_element != p.active_element THEN m.active_element ELSE NULL END AS mismatched_active_value,
         CASE WHEN m.read_element != p.read_element THEN 'read' ELSE NULL END AS mismatched_read,
         CASE WHEN m.edit_element != p.edit_element THEN 'edit' ELSE NULL END AS mismatched_edit,
         CASE WHEN m.create_element != p.create_element THEN 'create' ELSE NULL END AS mismatched_create,
         CASE WHEN m.delete_element != p.delete_element THEN 'delete' ELSE NULL END AS mismatched_delete,
         CASE WHEN m.active_element != p.active_element THEN 'active' ELSE NULL END AS mismatched_active
     FROM
         xdat_field_mapping m
         LEFT JOIN xdat_field_mapping_set S ON m.xdat_field_mapping_set_xdat_field_mapping_set_id = S.xdat_field_mapping_set_id
         LEFT JOIN xdat_element_access a ON S.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
         LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
         LEFT JOIN default_perms p ON g.displayname = p.display_name AND p.scope = 'shared'
     WHERE
             g.displayname IN ('Owners', 'Members', 'Collaborators') AND
             g.tag IN (SELECT id FROM project_ids) AND
             m.field LIKE '%/sharing/share/project' AND
             m.field_value != '*' AND
             (m.read_element != p.read_element OR m.edit_element != p.edit_element OR m.create_element != p.create_element OR m.delete_element != p.delete_element OR m.active_element != p.active_element)) mismatched;

CREATE OR REPLACE FUNCTION public.project_groups_get_groups(projectId VARCHAR(255))
    RETURNS TABLE
            (
                xdat_usergroup_id INTEGER,
                id                VARCHAR(255),
                displayname       VARCHAR(255),
                tag               VARCHAR(255))
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            g.xdat_usergroup_id,
            g.id,
            g.displayname,
            g.tag
        FROM
            xdat_usergroup g
        WHERE
                g.tag = projectId AND
                g.id LIKE projectId || '_%';
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_create_groups(projectId VARCHAR(255))
    RETURNS TABLE
            (
                xdat_usergroup_id INTEGER,
                id                VARCHAR(255),
                displayname       VARCHAR(255),
                tag               VARCHAR(255))
AS
$$
BEGIN
    WITH
        group_defs AS
            (WITH
                 project_groups AS (SELECT group_name FROM (VALUES ('Owner'), ('Member'), ('Collaborator')) AS project_groups(group_name))
             SELECT
                 projectId || '_' || lower(group_name) AS group_id,
                 group_name || 's' AS group_display_name,
                 projectId AS group_tag
             FROM
                 project_groups)
    INSERT
    INTO
        xdat_usergroup (id, displayname, tag)
    SELECT
        group_id,
        group_display_name,
        group_tag
    FROM
        group_defs d
        LEFT JOIN xdat_usergroup g ON g.id = d.group_id
    WHERE
        g.id IS NULL;

    RETURN QUERY
        SELECT
            g.xdat_usergroup_id,
            g.id,
            g.displayname,
            g.tag
        FROM
            project_groups_get_groups(projectId) g;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_get_element_accesses(projectId VARCHAR(255))
    RETURNS TABLE
            (
                xdat_element_access_id INTEGER,
                element_name           VARCHAR(255),
                xdat_usergroup_id      INTEGER,
                id                     VARCHAR(255),
                displayname            VARCHAR(255),
                tag                    VARCHAR(255))
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            a.xdat_element_access_id,
            a.element_name,
            g.xdat_usergroup_id,
            g.id,
            g.displayname,
            g.tag
        FROM
            xdat_element_access a
            LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
        WHERE
                g.tag = projectId AND
                g.id LIKE projectId || '_%';
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_create_element_accesses(projectId VARCHAR(255))
    RETURNS TABLE
            (
                xdat_element_access_id INTEGER,
                element_name           VARCHAR(255),
                xdat_usergroup_id      INTEGER,
                id                     VARCHAR(255),
                displayname            VARCHAR(255),
                tag                    VARCHAR(255))
AS
$$
BEGIN
    WITH
        element_defs AS
            (WITH
                secured_data_types AS (SELECT s.element_name FROM xdat_element_security s WHERE s.secure = 1),
                project_groups AS (SELECT
                                       g.xdat_usergroup_id
                                   FROM
                                       xdat_usergroup g
                                   WHERE
                                       g.tag = projectId AND
                                       g.displayname IN ('Owners', 'Members', 'Collaborators'))
             SELECT
                 t.element_name AS group_element_name,
                 g.xdat_usergroup_id AS group_id
             FROM
                 secured_data_types t,
                 project_groups g)
    INSERT
    INTO
        xdat_element_access (element_name, xdat_usergroup_xdat_usergroup_id)
    SELECT
        group_element_name,
        group_id
    FROM
        element_defs d
        LEFT JOIN xdat_element_access a ON d.group_element_name = a.element_name AND d.group_id = a.xdat_usergroup_xdat_usergroup_id
    WHERE
        a.xdat_element_access_id IS NULL;

    RETURN QUERY
        SELECT
            a.xdat_element_access_id,
            a.element_name,
            a.xdat_usergroup_id,
            a.id,
            a.displayname,
            a.tag
        FROM
            project_groups_get_element_accesses(projectId) a;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_get_mapping_sets(projectId VARCHAR(255))
    RETURNS TABLE
            (
                xdat_field_mapping_set_id INTEGER,
                method                    VARCHAR(255),
                xdat_element_access_id    INTEGER,
                element_name              VARCHAR(255),
                xdat_usergroup_id         INTEGER,
                id                        VARCHAR(255),
                displayname               VARCHAR(255),
                tag                       VARCHAR(255))
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            s.xdat_field_mapping_set_id,
            s.method,
            a.xdat_element_access_id,
            a.element_name,
            g.xdat_usergroup_id,
            g.id,
            g.displayname,
            g.tag
        FROM
            xdat_field_mapping_set s
            LEFT JOIN xdat_element_access a ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
            LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
        WHERE
                g.tag = projectId AND
                g.displayname IN ('Owners', 'Members', 'Collaborators');
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_create_mapping_sets(projectId VARCHAR(255))
    RETURNS TABLE
            (
                xdat_field_mapping_set_id INTEGER,
                method                    VARCHAR(255),
                xdat_element_access_id    INTEGER,
                element_name              VARCHAR(255),
                xdat_usergroup_id         INTEGER,
                id                        VARCHAR(255),
                displayname               VARCHAR(255),
                tag                       VARCHAR(255))
AS
$$
BEGIN
    INSERT
    INTO
        xdat_field_mapping_set (method, permissions_allow_set_xdat_elem_xdat_element_access_id)
    SELECT
        'OR' AS method,
        a.xdat_element_access_id
    FROM
        xdat_element_access a
        LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
        LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
    WHERE
            g.tag = projectId AND
            g.displayname IN ('Owners', 'Members', 'Collaborators') AND
            s.method IS NULL;

    RETURN QUERY
        SELECT
            s.xdat_field_mapping_set_id,
            s.method,
            s.xdat_element_access_id,
            s.element_name,
            s.xdat_usergroup_id,
            s.id,
            s.displayname,
            s.tag
        FROM
            project_groups_get_mapping_sets(projectId) s;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_get_mappings(projectId VARCHAR(255))
    RETURNS TABLE
            (
                xdat_field_mapping_id     INTEGER,
                field                     VARCHAR(255),
                field_value               VARCHAR(255),
                read_element              BOOLEAN,
                edit_element              BOOLEAN,
                create_element            BOOLEAN,
                delete_element            BOOLEAN,
                active_element            BOOLEAN,
                comparison_type           VARCHAR(255),
                xdat_field_mapping_set_id INTEGER,
                method                    VARCHAR(255),
                xdat_element_access_id    INTEGER,
                element_name              VARCHAR(255),
                xdat_usergroup_id         INTEGER,
                id                        VARCHAR(255),
                displayname               VARCHAR(255),
                tag                       VARCHAR(255))
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            m.xdat_field_mapping_id,
            m.field,
            m.field_value,
            m.read_element::BOOLEAN,
            m.edit_element::BOOLEAN,
            m.create_element::BOOLEAN,
            m.delete_element::BOOLEAN,
            m.active_element::BOOLEAN,
            m.comparison_type,
            s.xdat_field_mapping_set_id,
            s.method,
            a.xdat_element_access_id,
            a.element_name,
            g.xdat_usergroup_id,
            g.id,
            g.displayname,
            g.tag
        FROM
            xdat_field_mapping m
            LEFT JOIN xdat_field_mapping_set s ON m.xdat_field_mapping_set_xdat_field_mapping_set_id = s.xdat_field_mapping_set_id
            LEFT JOIN xdat_element_access a ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
            LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
        WHERE
                g.tag = projectId AND
                g.displayname IN ('Owners', 'Members', 'Collaborators');
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_create_mappings(projectId VARCHAR(255))
    RETURNS TABLE
            (
                xdat_field_mapping_id     INTEGER,
                field                     VARCHAR(255),
                field_value               VARCHAR(255),
                read_element              BOOLEAN,
                edit_element              BOOLEAN,
                create_element            BOOLEAN,
                delete_element            BOOLEAN,
                active_element            BOOLEAN,
                comparison_type           VARCHAR(255),
                xdat_field_mapping_set_id INTEGER,
                method                    VARCHAR(255),
                xdat_element_access_id    INTEGER,
                element_name              VARCHAR(255),
                xdat_usergroup_id         INTEGER,
                id                        VARCHAR(255),
                displayname               VARCHAR(255),
                tag                       VARCHAR(255))
AS
$$
BEGIN
    WITH
        project_perms AS (SELECT *
                          FROM
                              (VALUES
                               ('Owners', 1, 1, 1, 1, 1),
                               ('Members', 1, 0, 0, 0, 0),
                               ('Collaborators', 1, 0, 0, 0, 0)) AS project_perms(display_name, read_element, edit_element, create_element, delete_element, active_element))
    INSERT
    INTO
        xdat_field_mapping (field, field_value, read_element, edit_element, create_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
    SELECT
        'xnat:projectData/ID' AS field,
        g.tag,
        p.read_element,
        p.edit_element,
        p.create_element,
        p.delete_element,
        p.active_element,
        'equals',
        s.xdat_field_mapping_set_id
    FROM
        xdat_field_mapping_set s
        LEFT JOIN xdat_element_access a ON s.permissions_allow_set_xdat_elem_xdat_element_access_id = a.xdat_element_access_id
        LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
        LEFT JOIN project_perms p ON g.displayname = p.display_name
        LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
            g.tag = projectId AND
            g.displayname IN (SELECT display_name FROM project_perms) AND
            a.element_name = 'xnat:projectData' AND
            m.field IS NULL;

    WITH
        data_type_perms AS (SELECT *
                            FROM
                                (VALUES
                                 ('Owners', 1, 1, 1, 1, 1),
                                 ('Members', 1, 1, 1, 0, 1),
                                 ('Collaborators', 1, 0, 0, 0, 1)) AS data_type_perms(display_name, read_element, edit_element, create_element, delete_element, active_element))
    INSERT
    INTO xdat_field_mapping (field, field_value, read_element, edit_element, create_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
    SELECT
            a.element_name || '/project' AS field,
            g.tag,
            p.read_element,
            p.edit_element,
            p.create_element,
            p.delete_element,
            p.active_element,
            'equals',
            s.xdat_field_mapping_set_id
    FROM
        xdat_usergroup g
        LEFT JOIN data_type_perms p ON g.displayname = p.display_name
        LEFT JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id
        LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
        LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
            g.tag = projectId AND
            g.displayname IN ('Owners', 'Members', 'Collaborators') AND
            a.element_name != 'xnat:projectData' AND
            m.field IS NULL
    UNION
    SELECT
            a.element_name || '/sharing/share/project' AS field,
            g.tag,
            1,
            0,
            0,
            0,
            1,
            'equals',
            s.xdat_field_mapping_set_id
    FROM
        xdat_usergroup g
        LEFT JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id
        LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
        LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
            g.tag = projectId AND
            g.displayname IN ('Owners', 'Members', 'Collaborators') AND
            a.element_name != 'xnat:projectData' AND
            m.field IS NULL;

    RETURN QUERY
        SELECT
            m.xdat_field_mapping_id,
            m.field,
            m.field_value,
            m.read_element,
            m.edit_element,
            m.create_element,
            m.delete_element,
            m.active_element,
            m.comparison_type,
            m.xdat_field_mapping_set_id,
            m.method,
            m.xdat_element_access_id,
            m.element_name,
            m.xdat_usergroup_id,
            m.id,
            m.displayname,
            m.tag
        FROM
            project_groups_get_mappings(projectId) m;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_get_groups_and_permissions(projectId VARCHAR(255))
    RETURNS TABLE
            (
                group_id   VARCHAR(255),
                can_read   BOOLEAN,
                can_edit   BOOLEAN,
                can_create BOOLEAN,
                can_delete BOOLEAN,
                can_active BOOLEAN,
                fields     TEXT)
AS
$$
BEGIN
    RETURN QUERY
        SELECT
            g.id AS group_id,
            f.read_element::BOOLEAN AS can_read,
            f.edit_element::BOOLEAN AS can_edit,
            f.create_element::BOOLEAN AS can_create,
            f.delete_element::BOOLEAN AS can_delete,
            f.active_element::BOOLEAN AS can_active,
            array_to_string(array_agg(f.field ORDER BY f.field), ', ', '<NULL>') AS fields
        FROM
            xdat_usergroup g
            LEFT JOIN xdat_element_access a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id
            LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
            LEFT JOIN xdat_field_mapping f ON s.xdat_field_mapping_set_id = f.xdat_field_mapping_set_xdat_field_mapping_set_id
        WHERE
                g.tag = projectId AND
                g.displayname IN ('Owners', 'Members', 'Collaborators')
        GROUP BY
            g.id,
            f.read_element,
            f.edit_element,
            f.create_element,
            f.delete_element,
            f.active_element
        ORDER BY
            g.id;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_create_groups_and_permissions(projectId VARCHAR(255))
    RETURNS TABLE
            (
                group_id   VARCHAR(255),
                can_read   BOOLEAN,
                can_edit   BOOLEAN,
                can_create BOOLEAN,
                can_delete BOOLEAN,
                can_active BOOLEAN,
                fields     TEXT)
AS
$$
BEGIN
    PERFORM project_groups_create_groups(projectId);
    PERFORM project_groups_create_element_accesses(projectId);
    PERFORM project_groups_create_mapping_sets(projectId);
    PERFORM project_groups_create_mappings(projectId);

    RETURN QUERY
        SELECT * FROM project_groups_get_groups_and_permissions(projectId);
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_get_setting_flip_value(elementName VARCHAR(255), mismatchedValue INTEGER)
    RETURNS VARCHAR(255)
AS
$$
BEGIN
    IF mismatchedValue IS NULL
    THEN
        RETURN NULL;
    ELSEIF mismatchedValue = 1
    THEN
        RETURN elementName || '_element = 0';
    END IF;
    RETURN elementName || '_element = 1';
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.project_groups_fix_irregular_settings(executeFixQueries BOOLEAN DEFAULT FALSE)
    RETURNS SETOF INTEGER
AS
$$
BEGIN
    DECLARE
        current_index RECORD;
    BEGIN
        IF executeFixQueries = FALSE
        THEN
            RAISE NOTICE 'Dry run specified, queries will be displayed but not executed.';
        END IF;
        FOR current_index IN SELECT * FROM project_groups_find_irregular_settings
            LOOP
                DECLARE
                    update_sql TEXT;
                BEGIN
                    update_sql := format('UPDATE xdat_field_mapping SET %s WHERE xdat_field_mapping_id = %s',
                                         concat_ws(', ',
                                                   project_groups_get_setting_flip_value('read', current_index.mismatched_read_value),
                                                   project_groups_get_setting_flip_value('edit', current_index.mismatched_edit_value),
                                                   project_groups_get_setting_flip_value('create', current_index.mismatched_create_value),
                                                   project_groups_get_setting_flip_value('delete', current_index.mismatched_delete_value),
                                                   project_groups_get_setting_flip_value('active', current_index.mismatched_active_value)),
                                         current_index.xdat_field_mapping_id);
                    RAISE NOTICE 'Fixing irregular permissions for field mapping % with SQL: %', current_index.xdat_field_mapping_id, update_sql;
                    IF executeFixQueries = TRUE
                    THEN
                        EXECUTE update_sql;
                    END IF;
                END;
                RETURN NEXT current_index.xdat_field_mapping_id;
            END LOOP;
    END;
    RETURN;
END;
$$
    LANGUAGE plpgsql;
