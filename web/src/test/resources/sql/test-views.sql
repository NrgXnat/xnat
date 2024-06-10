DROP VIEW IF EXISTS public.run_sanity_check;
DROP VIEW IF EXISTS public.get_reference_validity;
DROP VIEW IF EXISTS public.get_all_references;
DROP FUNCTION IF EXISTS public.get_usergroup_members(reqGroupId VARCHAR);
DROP FUNCTION IF EXISTS public.get_role_members(roleName VARCHAR);
DROP FUNCTION IF EXISTS public.get_user_groups(reqUsername VARCHAR);
DROP FUNCTION IF EXISTS public.get_user_roles(reqUsername VARCHAR);
DROP TABLE IF EXISTS test_results;

CREATE OR REPLACE FUNCTION public.get_usergroup_members(reqGroupId VARCHAR(255))
    RETURNS TABLE (
        member VARCHAR(255)
    )
AS
$_$
BEGIN
    RETURN QUERY
        SELECT
            login
        FROM
            xdat_user u
            LEFT JOIN xdat_user_groupid i ON u.xdat_user_id = i.groups_groupid_xdat_user_xdat_user_id
            LEFT JOIN xdat_usergroup g ON i.groupid = g.id
        WHERE g.id = reqGroupId;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_role_members(roleName VARCHAR(255))
    RETURNS TABLE (
        member VARCHAR(255)
    )
AS
$_$
BEGIN
    RETURN QUERY
        SELECT
            login
        FROM
            xdat_role_type r
            LEFT JOIN xdat_r_xdat_role_type_assign_xdat_user u2r ON r.role_name = u2r.xdat_role_type_role_name
            LEFT JOIN xdat_user u ON u2r.xdat_user_xdat_user_id = u.xdat_user_id
        WHERE
            r.role_name = roleName AND
            u.login IS NOT NULL
        UNION
        SELECT
            r.username AS login
        FROM
            xhbm_user_role r
        WHERE
            r.role = roleName;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_user_groups(reqUsername VARCHAR(255))
    RETURNS TABLE (
        group_name VARCHAR(255)
    )
AS
$_$
BEGIN
    RETURN QUERY
        SELECT
            g.id
        FROM
            xdat_user u
            LEFT JOIN xdat_user_groupid i ON u.xdat_user_id = i.groups_groupid_xdat_user_xdat_user_id
            LEFT JOIN xdat_usergroup g ON i.groupid = g.id
        WHERE u.login = reqUsername;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.get_user_roles(reqUsername VARCHAR(255))
    RETURNS TABLE (
        role_name VARCHAR(255)
    )
AS
$_$
BEGIN
    RETURN QUERY
        SELECT
            r.role_name
        FROM
            xdat_user u
            LEFT JOIN xdat_r_xdat_role_type_assign_xdat_user u2r ON u.xdat_user_id = u2r.xdat_user_xdat_user_id
            LEFT JOIN xdat_role_type r ON u2r.xdat_role_type_role_name = r.role_name
        WHERE
            r.role_name IS NOT NULL AND
            u.login = reqUsername
        UNION
        SELECT
            role
        FROM
            xhbm_user_role
        WHERE username = reqUsername;
END
$_$
    LANGUAGE plpgsql;

CREATE OR REPLACE VIEW public.get_all_references AS
    WITH
        experiments AS
            (WITH
                 native_expts AS
                     (SELECT
                          x.project AS project,
                          s.id AS subject_id,
                          x.id AS expt_id,
                          x.label AS expt_label
                      FROM
                          xnat_subjectassessordata a
                          LEFT JOIN xnat_experimentdata x ON a.id = x.id
                          LEFT JOIN xnat_subjectdata s ON a.subject_id = s.id
                          LEFT JOIN xdat_meta_element e ON x.extension = e.xdat_meta_element_id)
             SELECT *
             FROM
                 native_expts
             UNION
             SELECT
                 s.project,
                 e.subject_id,
                 e.expt_id,
                 s.label
             FROM
                 xnat_experimentdata_share s
                 LEFT JOIN native_expts e ON s.sharing_share_xnat_experimentda_id = e.expt_id),
        subjects AS
            (SELECT
                 s.project AS project,
                 s.id AS subject_id,
                 s.label AS subject_label
             FROM
                 xnat_subjectdata s
             UNION
             SELECT
                 p.project AS project,
                 p.subject_id AS subject_id,
                 p.label AS subject_label
             FROM
                 xnat_projectparticipant p)
    SELECT
        x.project,
        x.subject_id,
        s.subject_label,
        x.expt_id,
        x.expt_label,
        e.element_name AS data_type
    FROM
        experiments x
        LEFT JOIN subjects s ON x.project = s.project AND x.subject_id = s.subject_id
        LEFT JOIN xnat_experimentdata xd ON x.expt_id = xd.id
        LEFT JOIN xdat_meta_element e ON xd.extension = e.xdat_meta_element_id
    WHERE
        s.subject_label IS NOT NULL;

CREATE TABLE test_results (
    id         SERIAL PRIMARY KEY,
    username   VARCHAR(15)  NOT NULL,
    url        VARCHAR(255) NOT NULL,
    result     INTEGER      NOT NULL,
    project    VARCHAR(15),
    subject    VARCHAR(15),
    experiment VARCHAR(31),
    operation  VARCHAR(15)  NOT NULL);

CREATE OR REPLACE VIEW public.get_reference_validity AS
    WITH
        all_references AS (SELECT * FROM get_all_references)
    SELECT
        t.*,
        CASE WHEN r.project IS NULL THEN FALSE ELSE TRUE END AS is_valid
    FROM
        test_results t
        LEFT JOIN all_references r ON
                    (t.project IS NULL OR t.project = r.project) AND
                    (t.subject IS NULL OR (t.subject = r.subject_id OR t.subject = r.subject_label AND t.project IS NOT NULL)) AND
                    (t.experiment IS NULL OR (t.experiment = r.expt_id OR t.experiment = r.expt_label AND (t.subject = r.subject_id OR t.subject = r.subject_label AND t.project IS NOT NULL))) OR
                    t.project IS NOT NULL AND t.experiment IS NOT NULL AND t.project = r.project AND t.experiment IN (r.expt_id, r.expt_label);

CREATE OR REPLACE VIEW public.run_sanity_check AS
SELECT
    username,
    url,
    result,
    project,
    subject,
    experiment,
    operation,
    is_valid
FROM
    get_reference_validity
WHERE
    ((username IN (SELECT member FROM get_usergroup_members('ALL_DATA_ADMIN')) OR username IN (SELECT member FROM get_role_members('Administrator'))) AND
     (result = 404 AND is_valid = TRUE OR result = 200 AND is_valid = FALSE)) OR
    (username IN (SELECT member FROM get_usergroup_members('ALL_DATA_ACCESS')) AND
     (result = 404 AND is_valid = TRUE OR result = 200 AND is_valid = FALSE OR result = 403 AND operation NOT IN ('edit', 'delete') OR result != 403 AND is_valid = TRUE AND operation IN ('edit', 'delete'))) OR
    (username NOT IN ('admin', 'dataAdmin', 'dataAccess') AND result NOT IN (200, 403));
