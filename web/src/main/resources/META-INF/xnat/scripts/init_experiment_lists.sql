--
-- web: src/main/resources/META-INF/xnat/scripts/init_experiment_lists.sql
-- XNAT http://www.xnat.org
-- Copyright (c) 2020, Washington University School of Medicine and Howard Hughes Medical Institute
-- All Rights Reserved
--  
-- Released under the Simplified BSD.
--

DROP FUNCTION IF EXISTS public.get_experiment_list(username VARCHAR(255), numResults INTEGER, numDays INTEGER);
DROP FUNCTION IF EXISTS public.get_experiment_list(username VARCHAR(255), limitResults BOOLEAN, numDays INTEGER);
DROP FUNCTION IF EXISTS public.get_accessible_image_sessions(username VARCHAR(255));
DROP FUNCTION IF EXISTS public.has_all_data_admin(username VARCHAR(255));
DROP FUNCTION IF EXISTS public.has_all_data_access(username VARCHAR(255));
DROP FUNCTION IF EXISTS public.get_open_workflows(numDays INTEGER);
DROP VIEW IF EXISTS public.get_all_image_sessions;

CREATE OR REPLACE VIEW public.get_all_image_sessions AS
SELECT
    isd.id,
    xme.element_name || '/project' AS field,
    expt.project,
    expt.label
FROM
    xnat_imageSessionData isd
        LEFT JOIN xnat_experimentData expt ON isd.id = expt.id
        LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id
UNION
SELECT
    expt.id,
    xme.element_name || '/sharing/share/project',
    shr.project,
    shr.label
FROM
    xnat_experimentData_share shr
        LEFT JOIN xnat_imagesessiondata isd ON shr.sharing_share_xnat_experimentda_id = isd.id
        LEFT JOIN xnat_experimentData expt ON isd.id = expt.id
        LEFT JOIN xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id
WHERE
    isd.id IS NOT NULL;

CREATE OR REPLACE FUNCTION public.get_open_workflows(numDays INTEGER DEFAULT 60)
    RETURNS TABLE
            (
                workflow_id     VARCHAR(255),
                workflow_date   TIMESTAMP,
                pipeline_name   TEXT,
                workflow_status VARCHAR(255)
            )
AS
$$
BEGIN
    RETURN QUERY
        SELECT DISTINCT ON (w.id)
            w.id          AS workflow_id,
            w.launch_time AS workflow_date,
            CASE w.pipeline_name
                WHEN 'Transferred'::TEXT THEN 'Archived'::TEXT
                ELSE CASE xs_lastposition('/'::TEXT, w.pipeline_name::TEXT)
                         WHEN 0 THEN w.pipeline_name
                         ELSE substring(substring(w.pipeline_name::TEXT, xs_lastposition('/'::TEXT, w.pipeline_name::TEXT) + 1), 1, xs_lastposition('.'::TEXT, substring(w.pipeline_name::TEXT, xs_lastposition('/'::TEXT, w.pipeline_name::TEXT) + 1)) - 1)
                     END
            END           AS pipeline_name,
            w.status      AS workflow_status
        FROM
            wrk_workflowdata w
        WHERE
            w.category != 'SIDE_ADMIN' AND
            w.launch_time > NOW() - make_interval(days := numDays) AND
            w.status != 'Failed (Dismissed)' AND
            w.pipeline_name NOT LIKE 'xnat_tools%AutoRun.xml'
        ORDER BY
            w.id,
            w.launch_time DESC;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.has_all_data_access(username VARCHAR(255))
    RETURNS BOOLEAN AS
$$
DECLARE
    has_all_data_access BOOLEAN;
BEGIN
    SELECT
        EXISTS(
                SELECT
                    u.login
                FROM
                    xdat_usergroup g
                        LEFT JOIN xdat_user_groupid i ON g.id = i.groupid
                        LEFT JOIN xdat_user u ON i.groups_groupid_xdat_user_xdat_user_id = u.xdat_user_id
                WHERE
                    g.id IN ('ALL_DATA_ACCESS', 'ALL_DATA_ADMIN') AND
                    u.login = username)
    INTO has_all_data_access;
    RETURN has_all_data_access;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.has_all_data_admin(username VARCHAR(255))
    RETURNS BOOLEAN AS
$$
DECLARE
    has_all_data_admin BOOLEAN;
BEGIN
    SELECT
        EXISTS(
                SELECT
                    u.login
                FROM
                    xdat_usergroup g
                        LEFT JOIN xdat_user_groupid i ON g.id = i.groupid
                        LEFT JOIN xdat_user u ON i.groups_groupid_xdat_user_xdat_user_id = u.xdat_user_id
                WHERE
                    g.id = 'ALL_DATA_ADMIN' AND
                    u.login = username)
    INTO has_all_data_admin;
    RETURN has_all_data_admin;
END
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_accessible_image_sessions(username VARCHAR(255))
    RETURNS TABLE
            (
                id      VARCHAR(255),
                label   VARCHAR(255),
                project VARCHAR(255)
            )
AS
$$
DECLARE
    has_all_data_access BOOLEAN;
BEGIN
    SELECT has_all_data_access(username) INTO has_all_data_access;

    IF has_all_data_access
    THEN
        RETURN QUERY
            SELECT DISTINCT ON (expts.id)
                expts.id,
                expts.label,
                expts.project
            FROM
                xnat_imageSessionData isd
                    LEFT JOIN xnat_experimentData expts ON isd.id = expts.id;
    ELSE
        RETURN QUERY
            WITH
                perms AS (
                    SELECT
                        a.element_name,
                        m.field,
                        m.field_value
                    FROM
                        xdat_user u
                            LEFT JOIN xdat_user_groupid i ON u.xdat_user_id = i.groups_groupid_xdat_user_xdat_user_id
                            LEFT JOIN xdat_usergroup g ON i.groupid = g.id
                            LEFT JOIN xdat_element_access a ON (xdat_usergroup_id = a.xdat_usergroup_xdat_usergroup_id OR u.xdat_user_id = a.xdat_user_xdat_user_id)
                            LEFT JOIN xdat_field_mapping_set s ON a.xdat_element_access_id = s.permissions_allow_set_xdat_elem_xdat_element_access_id
                            LEFT JOIN xdat_field_mapping m ON s.xdat_field_mapping_set_id = m.xdat_field_mapping_set_xdat_field_mapping_set_id
                    WHERE
                        m.field_value != '*' AND
                        m.read_element = 1 AND
                        u.login IN ('guest', username)),
                expts AS (SELECT * FROM get_all_image_sessions)
            SELECT DISTINCT ON (e.id)
                e.id,
                e.label,
                e.project
            FROM
                perms p
                    INNER JOIN expts e ON left(p.field, strpos(p.field, '/') - 1) = left(e.field, strpos(e.field, '/') - 1) AND p.field_value = e.project;
    END IF;
END
$$
    LANGUAGE plpgsql;

-- Gets the experiment list for a user
--
-- The numResults parameter indicates the maximum number of results that should be returned:
--  - Specifying no value uses the default limit of 60 rows
--  - Any number greater than 0 is used as the limit, so specifying 30 would return 30 rows (if available)
--  - Specifying NULL indicates no limit
CREATE OR REPLACE FUNCTION public.get_experiment_list(username VARCHAR(255), numResults INTEGER DEFAULT 60, numDays INTEGER DEFAULT 60)
    RETURNS TABLE
            (
                id              VARCHAR(255),
                label           VARCHAR(255),
                project         VARCHAR(255),
                date            DATE,
                status          VARCHAR(255),
                workflow_status VARCHAR(255),
                element_name    VARCHAR(250),
                TYPE_DESC       VARCHAR(255),
                insert_date     TIMESTAMP,
                activation_date TIMESTAMP,
                last_modified   TIMESTAMP,
                workflow_date   TIMESTAMP,
                pipeline_name   TEXT,
                action_date     TIMESTAMP,
                scanner         VARCHAR(255)
            )
AS
$$
DECLARE
    idleInterval TIMESTAMP;
BEGIN
    idleInterval := NOW() - make_interval(days := numDays);

    RETURN QUERY
        SELECT *
        FROM
            (SELECT DISTINCT ON (expt.id)
                 expt.id,
                 perm.label,
                 perm.project,
                 expt.date,
                 emd.status,
                 W.workflow_status,
                 xme.element_name,
                 COALESCE(es.code, es.singular, es.element_name)               AS TYPE_DESC,
                 emd.insert_date,
                 emd.activation_date,
                 emd.last_modified,
                 W.workflow_date,
                 W.pipeline_name,
                 COALESCE(W.workflow_date, emd.last_modified, emd.insert_date) AS action_date,
                 isd.scanner
             FROM
                 xnat_experimentData expt
                     LEFT JOIN  xdat_meta_element xme ON expt.extension = xme.xdat_meta_element_id
                     LEFT JOIN  xnat_experimentData_meta_data emd ON expt.experimentData_info = emd.meta_data_id
                     LEFT JOIN  xdat_element_security es ON xme.element_name = es.element_name
                     LEFT JOIN  (SELECT W.workflow_id, W.workflow_date, W.pipeline_name, W.workflow_status FROM get_open_workflows(numDays) W) W ON expt.id = W.workflow_id
                     RIGHT JOIN (SELECT S.id, S.label, S.project FROM get_accessible_image_sessions(username) S) perm ON expt.id = perm.id
                     RIGHT JOIN xnat_imageSessionData isd ON perm.id = isd.id
             WHERE
                 emd.status != 'obsolete' AND
                 (emd.insert_date > idleInterval OR emd.activation_date > idleInterval OR emd.last_modified > idleInterval OR W.workflow_date > idleInterval)) SEARCH
        ORDER BY
            SEARCH.action_date DESC
        LIMIT numResults;
END
$$ LANGUAGE plpgsql;
