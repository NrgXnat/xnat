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
        project_groups AS (SELECT group_name FROM (VALUES ('Owner'), ('Member'), ('Collaborator')) AS project_groups(group_name))
    INSERT
    INTO xdat_usergroup (id, displayname, tag
    )
    SELECT
                projectId || '_' || lower(group_name),
                group_name || 's',
                projectId
    FROM
        project_groups
    ON CONFLICT DO NOTHING;

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
        secured_data_types AS (SELECT s.element_name FROM xdat_element_security s WHERE s.secure = 1),
        project_groups AS (SELECT
                               g.xdat_usergroup_id
                           FROM
                               xdat_usergroup g
                           WHERE
                                   g.tag = projectId AND
                                   g.displayname IN ('Owners', 'Members', 'Collaborators'))
    INSERT
    INTO xdat_element_access (element_name, xdat_usergroup_xdat_usergroup_id
    )
    SELECT
        t.element_name,
        g.xdat_usergroup_id
    FROM
        secured_data_types t,
        project_groups g
    ON CONFLICT DO NOTHING;

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
    INTO xdat_field_mapping (field, field_value, read_element, edit_element, create_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id
    )
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
                                 ('Collaborators', 1, 0, 0, 0, 1)) AS project_perms(display_name, read_element, edit_element, create_element, delete_element, active_element))
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
            array_to_string(array_agg(f.field), ', ', '<NULL>') AS fields
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
