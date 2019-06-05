DROP FUNCTION IF EXISTS public.permissions_create_project_groups_and_permissions(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.permissions_create_data_type_field_mappings(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.permissions_create_project_field_mappings(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.permissions_create_field_mapping_sets(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.permissions_create_element_access(projectId VARCHAR(255));
DROP FUNCTION IF EXISTS public.permissions_create_project_groups(projectId VARCHAR(255));

CREATE OR REPLACE FUNCTION public.permissions_create_project_groups(projectId VARCHAR(255))
    RETURNS BOOLEAN
AS
$$
BEGIN
    WITH project_groups AS (SELECT group_name FROM (VALUES ('Owner'), ('Member'), ('Collaborator')) AS project_groups(group_name))
    INSERT
    INTO xdat_usergroup (id, displayname, tag)
    SELECT projectId || '_' || lower(group_name),
           group_name || 's',
           projectId
    FROM project_groups;
    RETURN TRUE;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.permissions_create_element_access(projectId VARCHAR(255))
    RETURNS BOOLEAN
AS
$$
BEGIN
    WITH secured_data_types AS (SELECT element_name FROM xdat_element_security WHERE secure = 1),
         project_groups AS (SELECT xdat_usergroup_id
                            FROM xdat_usergroup
                            WHERE
                                tag = projectId AND
                                displayname IN ('Owners', 'Members', 'Collaborators'))
    INSERT
    INTO xdat_element_access (element_name, xdat_usergroup_xdat_usergroup_id)
    SELECT element_name,
           xdat_usergroup_id
    FROM secured_data_types,
         project_groups;

    RETURN TRUE;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.permissions_create_field_mapping_sets(projectId VARCHAR(255))
    RETURNS BOOLEAN
AS
$$
BEGIN
    INSERT
    INTO xdat_field_mapping_set (method, permissions_allow_set_xdat_elem_xdat_element_access_id)
    SELECT 'OR' AS method,
           xdat_element_access_id
    FROM xdat_element_access      a
         LEFT JOIN xdat_usergroup g ON a.xdat_usergroup_xdat_usergroup_id = g.xdat_usergroup_id
    WHERE
        g.tag = projectId AND
        g.displayname IN ('Owners', 'Members', 'Collaborators');

    RETURN TRUE;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.permissions_create_project_field_mappings(projectId VARCHAR(255))
    RETURNS BOOLEAN
AS
$$
BEGIN
    WITH project_perms AS (SELECT *
                           FROM (VALUES ('Owners', 1, 1, 1, 1, 1),
                                        ('Members', 1, 0, 0, 0, 0),
                                        ('Collaborators', 1, 0, 0, 0, 0)) AS project_perms(display_name, read_element, edit_element, create_element, delete_element, active_element))
    INSERT
    INTO xdat_field_mapping (field, field_value, read_element, edit_element, create_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
    SELECT 'xnat:projectData/ID' AS field,
           g.tag,
           p.read_element,
           p.edit_element,
           p.create_element,
           p.delete_element,
           p.active_element,
           'equals',
           s.xdat_field_mapping_set_id
    FROM xdat_userGroup                   g
         LEFT JOIN project_perms          p ON g.displayname = p.display_name
         LEFT JOIN xdat_element_access    a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id AND a.element_name = 'xnat:projectData'
         LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
         LEFT JOIN xdat_field_mapping     m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
    WHERE
        g.tag = projectId AND
        g.displayname IN ('Owners', 'Members', 'Collaborators') AND
        m.field IS NULL;

    RETURN TRUE;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.permissions_create_data_type_field_mappings(projectId VARCHAR(255))
    RETURNS BOOLEAN
AS
$$
BEGIN
    WITH data_type_perms AS (SELECT *
                             FROM (VALUES ('Owners', 1, 1, 1, 1, 1),
                                          ('Members', 1, 1, 1, 0, 1),
                                          ('Collaborators', 1, 0, 0, 0, 1)) AS project_perms(display_name, read_element, edit_element, create_element, delete_element, active_element))
    INSERT
    INTO xdat_field_mapping (field, field_value, read_element, edit_element, create_element, delete_element, active_element, comparison_type, xdat_field_mapping_set_xdat_field_mapping_set_id)
        (SELECT a.element_name || '/project' AS field,
                g.tag,
                p.read_element,
                p.edit_element,
                p.create_element,
                p.delete_element,
                p.active_element,
                'equals',
                s.xdat_field_mapping_set_id
         FROM xdat_userGroup                   g
              LEFT JOIN data_type_perms        p ON g.displayname = p.display_name
              LEFT JOIN xdat_element_access    a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id
              LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
              LEFT JOIN xdat_field_mapping     m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
         WHERE
             g.tag = projectId AND
             g.displayname IN ('Owners', 'Members', 'Collaborators') AND
             m.field IS NULL
         UNION
         SELECT a.element_name || '/sharing/share/project' AS field,
                g.tag,
                1,
                0,
                0,
                0,
                1,
                'equals',
                s.xdat_field_mapping_set_id
         FROM xdat_userGroup                   g
              LEFT JOIN xdat_element_access    a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id
              LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
              LEFT JOIN xdat_field_mapping     m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
         WHERE
             g.tag = projectId AND
             g.displayname IN ('Owners', 'Members', 'Collaborators') AND
             m.field IS NULL);

    RETURN TRUE;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.permissions_create_project_groups_and_permissions(projectId VARCHAR(255))
    RETURNS TABLE
            (
                group_id   VARCHAR(255),
                can_read   BOOLEAN,
                can_edit   BOOLEAN,
                can_create BOOLEAN,
                can_delete BOOLEAN,
                can_active BOOLEAN,
                fields     TEXT
            )

AS
$$
BEGIN
    PERFORM permissions_create_project_groups(projectId);
    PERFORM permissions_create_element_access(projectId);
    PERFORM permissions_create_field_mapping_sets(projectId);
    PERFORM permissions_create_project_field_mappings(projectId);
    PERFORM permissions_create_data_type_field_mappings(projectId);

    RETURN QUERY SELECT g.id                                                AS group_id,
                        f.read_element::BOOLEAN                             AS can_read,
                        f.edit_element::BOOLEAN                             AS can_edit,
                        f.create_element::BOOLEAN                           AS can_create,
                        f.delete_element::BOOLEAN                           AS can_delete,
                        f.active_element::BOOLEAN                           AS can_active,
                        array_to_string(array_agg(f.field), ', ', '<NULL>') AS fields
                 FROM xdat_usergroup                   g
                      LEFT JOIN xdat_element_access    a ON g.xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id
                      LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
                      LEFT JOIN xdat_field_mapping     f ON s.xdat_field_mapping_set_id = f.xdat_field_mapping_set_xdat_field_mapping_set_id
                 WHERE
                     g.tag = projectId AND
                     g.displayname IN ('Owners', 'Members', 'Collaborators')
                 GROUP BY g.id,
                          f.read_element,
                          f.edit_element,
                          f.create_element,
                          f.delete_element,
                          f.active_element
                 ORDER BY g.id;
END
$$
    LANGUAGE plpgsql;
