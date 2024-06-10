-- To utilize this test query, create the following data context:
--
-- Create a public project (:publicProjectId)
-- Create two private projects
-- In the first private project (:sourceProjectId) create a subject (:sourceSubjectLabel and :subjectId) and an experiment (:sourceExptLabel and :experimentId)
-- Share the subject (:sharedSubjectLabel) and experiment (:sharedExptLabel) from the first private project into the second private project (:sharedProjectId)
-- Create three users and add them as members of the first private project, making them :owner, :member, and :collaborator respectively
-- Make all three users collaborators on the second private project
-- Run the query.
--
-- If all goes well, you'll get an empty string. If all DOESN'T go well, you'll get a comma-separated list of each scenario that failed.
CREATE OR REPLACE FUNCTION public.test_permissions_with_collab_access_to_shared(owner VARCHAR(255),
                                                                                member VARCHAR(255),
                                                                                collaborator VARCHAR(255),
                                                                                subjectId VARCHAR(255),
                                                                                experimentId VARCHAR(255),
                                                                                publicProjectId VARCHAR(255),
                                                                                sourceProjectId VARCHAR(255),
                                                                                sourceSubjectLabel VARCHAR(255),
                                                                                sourceExptLabel VARCHAR(255),
                                                                                sharedProjectId VARCHAR(255),
                                                                                sharedSubjectLabel VARCHAR(255),
                                                                                sharedExptLabel VARCHAR(255))
    RETURNS TEXT
AS
$$
DECLARE
    results TEXT;
BEGIN
    SELECT
        concat_ws(', ',
                  CASE WHEN (owner_errors <> '') IS NOT TRUE THEN NULL ELSE owner_errors END,
                  CASE WHEN (member_errors <> '') IS NOT TRUE THEN NULL ELSE member_errors END,
                  CASE WHEN (collab_errors <> '') IS NOT TRUE THEN NULL ELSE collab_errors END) AS all_errors
    FROM
        (SELECT
             (SELECT
                  concat_ws(', ', owner_read_public, owner_edit_public, owner_read_source_project, owner_edit_source_project, owner_read_source_subj_label, owner_edit_source_subj_label, owner_delete_source_subj_label, owner_read_source_expt_label,
                            owner_edit_source_expt_label, owner_delete_source_expt_label, owner_read_source_project_subject_id, owner_edit_source_project_subject_id, owner_delete_source_project_subject_id, owner_read_source_project_experiment_id,
                            owner_edit_source_project_experiment_id, owner_delete_source_project_experiment_id, owner_read_shared_project, owner_edit_shared_project, owner_read_shared_subject_label, owner_edit_shared_subject_label, owner_delete_shared_subject_label,
                            owner_read_shared_expt_label, owner_edit_shared_expt_label, owner_delete_shared_expt_label, owner_read_shared_project_subject_id, owner_edit_shared_project_subject_id, owner_delete_shared_project_subject_id,
                            owner_read_shared_project_experiment_id, owner_edit_shared_project_experiment_id, owner_delete_shared_project_experiment_id, owner_read_source_project_shared_subject_label, owner_edit_source_project_shared_subject_label, owner_read_shared_project_source_subj_label,
                            owner_edit_shared_project_source_subj_label)
              FROM
                  (SELECT
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', publicProjectId) = TRUE) THEN NULL ELSE 'owner_read_public' END AS owner_read_public,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', publicProjectId) = FALSE) THEN NULL ELSE 'owner_edit_public' END AS owner_edit_public,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', sourceProjectId) = TRUE) THEN NULL ELSE 'owner_read_source_project' END AS owner_read_source_project,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', sourceProjectId) = TRUE) THEN NULL ELSE 'owner_edit_source_project' END AS owner_edit_source_project,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', sourceSubjectLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_read_source_subj_label' END AS owner_read_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', sourceSubjectLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_edit_source_subj_label' END AS owner_edit_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'delete', sourceSubjectLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_delete_source_subj_label' END AS owner_delete_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', sourceExptLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_read_source_expt_label' END AS owner_read_source_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', sourceExptLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_edit_source_expt_label' END AS owner_edit_source_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'delete', sourceExptLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_delete_source_expt_label' END AS owner_delete_source_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_read_source_project_subject_id' END AS owner_read_source_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_edit_source_project_subject_id' END AS owner_edit_source_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'delete', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_delete_source_project_subject_id' END AS owner_delete_source_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_read_source_project_experiment_id' END AS owner_read_source_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_edit_source_project_experiment_id' END AS owner_edit_source_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'delete', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'owner_delete_source_project_experiment_id' END AS owner_delete_source_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', sharedProjectId) = TRUE) THEN NULL ELSE 'owner_read_shared_project' END AS owner_read_shared_project,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', sharedProjectId) = FALSE) THEN NULL ELSE 'owner_edit_shared_project' END AS owner_edit_shared_project,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', sharedSubjectLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_read_shared_subject_label' END AS owner_read_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', sharedSubjectLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_edit_shared_subject_label' END AS owner_edit_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'delete', sharedSubjectLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_delete_shared_subject_label' END AS owner_delete_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', sharedExptLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_read_shared_expt_label' END AS owner_read_shared_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', sharedExptLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_edit_shared_expt_label' END AS owner_edit_shared_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'delete', sharedExptLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_delete_shared_expt_label' END AS owner_delete_shared_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_read_shared_project_subject_id' END AS owner_read_shared_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_edit_shared_project_subject_id' END AS owner_edit_shared_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'delete', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_delete_shared_project_subject_id' END AS owner_delete_shared_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_read_shared_project_experiment_id' END AS owner_read_shared_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_edit_shared_project_experiment_id' END AS owner_edit_shared_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'delete', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'owner_delete_shared_project_experiment_id' END AS owner_delete_shared_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', sharedSubjectLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'owner_read_source_project_shared_subject_label' END AS owner_read_source_project_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', sharedExptLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'owner_edit_source_project_shared_subject_label' END AS owner_edit_source_project_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'read', sourceSubjectLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'owner_read_shared_project_source_subj_label' END AS owner_read_shared_project_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(owner, 'edit', sourceExptLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'owner_edit_shared_project_source_subj_label' END AS owner_edit_shared_project_source_subj_label) owner_results) AS owner_errors,
             (SELECT
                  concat_ws(', ', member_read_public, member_edit_public, member_read_source_project, member_edit_source_project, member_read_source_subj_label, member_edit_source_subj_label, member_delete_source_subj_label,
                            member_read_source_expt_label, member_edit_source_expt_label, member_delete_source_expt_label, member_read_source_project_subject_id, member_edit_source_project_subject_id, member_delete_source_project_subject_id,
                            member_read_source_project_experiment_id, member_edit_source_project_experiment_id, member_delete_source_project_experiment_id, member_read_shared_project, member_edit_shared_project, member_read_shared_subject_label, member_edit_shared_subject_label,
                            member_delete_shared_subject_label, member_read_shared_expt_label, member_edit_shared_expt_label, member_delete_shared_expt_label, member_read_shared_project_subject_id, member_edit_shared_project_subject_id,
                            member_delete_shared_project_subject_id, member_read_shared_project_experiment_id, member_edit_shared_project_experiment_id, member_delete_shared_project_experiment_id, member_read_source_project_shared_subject_label, member_edit_source_project_shared_subject_label,
                            member_read_shared_project_source_subj_label, member_edit_shared_project_source_subj_label)
              FROM
                  (SELECT
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', publicProjectId) = TRUE) THEN NULL ELSE 'member_read_public' END AS member_read_public,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', publicProjectId) = FALSE) THEN NULL ELSE 'member_edit_public' END AS member_edit_public,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', sourceProjectId) = TRUE) THEN NULL ELSE 'member_read_source_project' END AS member_read_source_project,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', sourceProjectId) = FALSE) THEN NULL ELSE 'member_edit_source_project' END AS member_edit_source_project,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', sourceSubjectLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'member_read_source_subj_label' END AS member_read_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', sourceSubjectLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'member_edit_source_subj_label' END AS member_edit_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'delete', sourceSubjectLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'member_delete_source_subj_label' END AS member_delete_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', sourceExptLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'member_read_source_expt_label' END AS member_read_source_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', sourceExptLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'member_edit_source_expt_label' END AS member_edit_source_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'delete', sourceExptLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'member_delete_source_expt_label' END AS member_delete_source_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'member_read_source_project_subject_id' END AS member_read_source_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'member_edit_source_project_subject_id' END AS member_edit_source_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'delete', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'member_delete_source_project_subject_id' END AS member_delete_source_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'member_read_source_project_experiment_id' END AS member_read_source_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'member_edit_source_project_experiment_id' END AS member_edit_source_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'delete', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'member_delete_source_project_experiment_id' END AS member_delete_source_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', sharedProjectId) = TRUE) THEN NULL ELSE 'member_read_shared_project' END AS member_read_shared_project,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', sharedProjectId) = FALSE) THEN NULL ELSE 'member_edit_shared_project' END AS member_edit_shared_project,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', sharedSubjectLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'member_read_shared_subject_label' END AS member_read_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', sharedSubjectLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'member_edit_shared_subject_label' END AS member_edit_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'delete', sharedSubjectLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'member_delete_shared_subject_label' END AS member_delete_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', sharedExptLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'member_read_shared_expt_label' END AS member_read_shared_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', sharedExptLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'member_edit_shared_expt_label' END AS member_edit_shared_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'delete', sharedExptLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'member_delete_shared_expt_label' END AS member_delete_shared_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'member_read_shared_project_subject_id' END AS member_read_shared_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'member_edit_shared_project_subject_id' END AS member_edit_shared_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'member_delete_shared_project_subject_id' END AS member_delete_shared_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'member_read_shared_project_experiment_id' END AS member_read_shared_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'member_edit_shared_project_experiment_id' END AS member_edit_shared_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'member_delete_shared_project_experiment_id' END AS member_delete_shared_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', sharedSubjectLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'member_read_source_project_shared_subject_label' END AS member_read_source_project_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', sharedExptLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'member_edit_source_project_shared_subject_label' END AS member_edit_source_project_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'read', sourceSubjectLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'member_read_shared_project_source_subj_label' END AS member_read_shared_project_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(member, 'edit', sourceExptLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'member_edit_shared_project_source_subj_label' END AS member_edit_shared_project_source_subj_label) member_results) AS member_errors,
             (SELECT
                  concat_ws(', ', collab_read_public, collab_edit_public, collab_read_source_project, collab_edit_source_project, collab_read_source_subj_label, collab_edit_source_subj_label, collab_delete_source_subj_label,
                            collab_read_source_expt_label, collab_edit_source_expt_label, collab_delete_source_expt_label, collab_read_source_project_subject_id, collab_edit_source_project_subject_id, collab_delete_source_project_subject_id,
                            collab_read_source_project_experiment_id, collab_edit_source_project_experiment_id, collab_delete_source_project_experiment_id, collab_read_shared_project, collab_edit_shared_project, collab_read_shared_subject_label, collab_edit_shared_subject_label,
                            collab_delete_shared_subject_label, collab_read_shared_expt_label, collab_edit_shared_expt_label, collab_delete_shared_expt_label, collab_read_shared_project_subject_id, collab_edit_shared_project_subject_id,
                            collab_delete_shared_project_subject_id, collab_read_shared_project_experiment_id, collab_edit_shared_project_experiment_id, collab_delete_shared_project_experiment_id, collab_read_source_project_shared_subject_label,
                            collab_edit_source_project_shared_subject_label, collab_read_shared_project_source_subj_label, collab_edit_shared_project_source_subj_label)
              FROM
                  (SELECT
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', publicProjectId) = TRUE) THEN NULL ELSE 'collab_read_public' END AS collab_read_public,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', publicProjectId) = FALSE) THEN NULL ELSE 'collab_edit_public' END AS collab_edit_public,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', sourceProjectId) = TRUE) THEN NULL ELSE 'collab_read_source_project' END AS collab_read_source_project,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', sourceProjectId) = FALSE) THEN NULL ELSE 'collab_edit_source_project' END AS collab_edit_source_project,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', sourceSubjectLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'collab_read_source_subj_label' END AS collab_read_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', sourceSubjectLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_edit_source_subj_label' END AS collab_edit_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'delete', sourceSubjectLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_delete_source_subj_label' END AS collab_delete_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', sourceExptLabel, sourceProjectId) = TRUE) THEN NULL ELSE 'collab_read_source_expt_label' END AS collab_read_source_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', sourceExptLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_edit_source_expt_label' END AS collab_edit_source_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'delete', sourceExptLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_delete_source_expt_label' END AS collab_delete_source_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'collab_read_source_project_subject_id' END AS collab_read_source_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_edit_source_project_subject_id' END AS collab_edit_source_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'delete', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_delete_source_project_subject_id' END AS collab_delete_source_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'collab_read_source_project_experiment_id' END AS collab_read_source_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_edit_source_project_experiment_id' END AS collab_edit_source_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'delete', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_delete_source_project_experiment_id' END AS collab_delete_source_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', sharedProjectId) = TRUE) THEN NULL ELSE 'collab_read_shared_project' END AS collab_read_shared_project,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', sharedProjectId) = FALSE) THEN NULL ELSE 'collab_edit_shared_project' END AS collab_edit_shared_project,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', sharedSubjectLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'collab_read_shared_subject_label' END AS collab_read_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', sharedSubjectLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_edit_shared_subject_label' END AS collab_edit_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'delete', sharedSubjectLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_delete_shared_subject_label' END AS collab_delete_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', sharedExptLabel, sharedProjectId) = TRUE) THEN NULL ELSE 'collab_read_shared_expt_label' END AS collab_read_shared_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', sharedExptLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_edit_shared_expt_label' END AS collab_edit_shared_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'delete', sharedExptLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_delete_shared_expt_label' END AS collab_delete_shared_expt_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'collab_read_shared_project_subject_id' END AS collab_read_shared_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_edit_shared_project_subject_id' END AS collab_edit_shared_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_delete_shared_project_subject_id' END AS collab_delete_shared_project_subject_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'collab_read_shared_project_experiment_id' END AS collab_read_shared_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_edit_shared_project_experiment_id' END AS collab_edit_shared_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_delete_shared_project_experiment_id' END AS collab_delete_shared_project_experiment_id,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', sharedSubjectLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_read_source_project_shared_subject_label' END AS collab_read_source_project_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', sharedExptLabel, sourceProjectId) = FALSE) THEN NULL ELSE 'collab_edit_source_project_shared_subject_label' END AS collab_edit_source_project_shared_subject_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'read', sourceSubjectLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_read_shared_project_source_subj_label' END AS collab_read_shared_project_source_subj_label,
                       CASE WHEN (SELECT data_type_fns_can(collaborator, 'edit', sourceExptLabel, sharedProjectId) = FALSE) THEN NULL ELSE 'collab_edit_shared_project_source_subj_label' END AS collab_edit_shared_project_source_subj_label) collab_results) AS collab_errors) all_errors
    INTO results;
    RETURN results;
END
$$
    LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION public.test_source_and_shared_permissions(dataAccess VARCHAR(255),
                                                                     dataAdmin VARCHAR(255),
                                                                     sourceProjectId VARCHAR(255),
                                                                     sourceOwner VARCHAR(255),
                                                                     sourceMember VARCHAR(255),
                                                                     sourceCollab VARCHAR(255),
                                                                     sharedProjectId VARCHAR(255),
                                                                     sharedOwner VARCHAR(255),
                                                                     sharedMember VARCHAR(255),
                                                                     sharedCollab VARCHAR(255),
                                                                     subjectId VARCHAR(255),
                                                                     experimentId VARCHAR(255))
    RETURNS TEXT
AS
$$
DECLARE
    results TEXT;
BEGIN
    SELECT
        concat_ws(', ',
                  CASE WHEN (source_errors <> '') IS NOT TRUE THEN NULL ELSE source_errors END,
                  CASE WHEN (shared_errors <> '') IS NOT TRUE THEN NULL ELSE shared_errors END) AS all_errors
    FROM
        (SELECT
             (SELECT
                  concat_ws(', ', admin_can_read_subject_in_source, admin_can_read_expt_in_source, admin_can_edit_subject_in_source, admin_can_edit_expt_in_source, admin_can_delete_subject_in_source, admin_can_delete_expt_in_source, guest_can_read_subject_in_source, guest_can_read_expt_in_source, guest_can_edit_subject_in_source, guest_can_edit_expt_in_source, guest_can_delete_subject_in_source, guest_can_delete_expt_in_source, dataAdmin_can_read_subject_in_source, dataAdmin_can_read_expt_in_source, dataAdmin_can_edit_subject_in_source, dataAdmin_can_edit_expt_in_source, dataAdmin_can_delete_subject_in_source, dataAdmin_can_delete_expt_in_source, dataAccess_can_read_subject_in_source, dataAccess_can_read_expt_in_source, dataAccess_can_edit_subject_in_source, dataAccess_can_edit_expt_in_source, dataAccess_can_delete_subject_in_source, dataAccess_can_delete_expt_in_source, sourceOwner_can_read_subject_in_source, sourceOwner_can_read_expt_in_source,
                            sourceOwner_can_edit_subject_in_source,
                            sourceOwner_can_edit_expt_in_source, sourceOwner_can_delete_subject_in_source, sourceOwner_can_delete_expt_in_source, sourceMember_can_read_subject_in_source, sourceMember_can_read_expt_in_source, sourceMember_can_edit_subject_in_source, sourceMember_can_edit_expt_in_source, sourceMember_can_delete_subject_in_source, sourceMember_can_delete_expt_in_source, sourceCollab_can_read_subject_in_source, sourceCollab_can_read_expt_in_source, sourceCollab_can_edit_subject_in_source, sourceCollab_can_edit_expt_in_source, sourceCollab_can_delete_subject_in_source, sourceCollab_can_delete_expt_in_source, sharedOwner_can_read_subject_in_source, sharedOwner_can_read_expt_in_source, sharedOwner_can_edit_subject_in_source, sharedOwner_can_edit_expt_in_source, sharedOwner_can_delete_subject_in_source, sharedOwner_can_delete_expt_in_source, sharedMember_can_read_subject_in_source, sharedMember_can_read_expt_in_source, sharedMember_can_edit_subject_in_source,
                            sharedMember_can_edit_expt_in_source, sharedMember_can_delete_subject_in_source, sharedMember_can_delete_expt_in_source, sharedCollab_can_read_subject_in_source, sharedCollab_can_read_expt_in_source, sharedCollab_can_edit_subject_in_source, sharedCollab_can_edit_expt_in_source, sharedCollab_can_delete_subject_in_source, sharedCollab_can_delete_expt_in_source)
              FROM
                  (SELECT
                       CASE WHEN (SELECT data_type_fns_can('admin', 'read', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'admin_can_read_subject_in_source' END AS admin_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'read', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'admin_can_read_expt_in_source' END AS admin_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'edit', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'admin_can_edit_subject_in_source' END AS admin_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'edit', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'admin_can_edit_expt_in_source' END AS admin_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'delete', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'admin_can_delete_subject_in_source' END AS admin_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'delete', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'admin_can_delete_expt_in_source' END AS admin_can_delete_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'read', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'guest_can_read_subject_in_source' END AS guest_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'read', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'guest_can_read_expt_in_source' END AS guest_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'edit', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'guest_can_edit_subject_in_source' END AS guest_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'edit', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'guest_can_edit_expt_in_source' END AS guest_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'delete', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'guest_can_delete_subject_in_source' END AS guest_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'delete', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'guest_can_delete_expt_in_source' END AS guest_can_delete_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'read', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_read_subject_in_source' END AS dataAdmin_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'read', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_read_expt_in_source' END AS dataAdmin_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'edit', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_edit_subject_in_source' END AS dataAdmin_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'edit', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_edit_expt_in_source' END AS dataAdmin_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'delete', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_delete_subject_in_source' END AS dataAdmin_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'delete', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_delete_expt_in_source' END AS dataAdmin_can_delete_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'read', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'dataAccess_can_read_subject_in_source' END AS dataAccess_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'read', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'dataAccess_can_read_expt_in_source' END AS dataAccess_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'edit', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'dataAccess_can_edit_subject_in_source' END AS dataAccess_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'edit', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'dataAccess_can_edit_expt_in_source' END AS dataAccess_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'delete', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'dataAccess_can_delete_subject_in_source' END AS dataAccess_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'delete', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'dataAccess_can_delete_expt_in_source' END AS dataAccess_can_delete_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'read', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceOwner_can_read_subject_in_source' END AS sourceOwner_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'read', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceOwner_can_read_expt_in_source' END AS sourceOwner_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'edit', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceOwner_can_edit_subject_in_source' END AS sourceOwner_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'edit', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceOwner_can_edit_expt_in_source' END AS sourceOwner_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'delete', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceOwner_can_delete_subject_in_source' END AS sourceOwner_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'delete', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceOwner_can_delete_expt_in_source' END AS sourceOwner_can_delete_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'read', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceMember_can_read_subject_in_source' END AS sourceMember_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'read', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceMember_can_read_expt_in_source' END AS sourceMember_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'edit', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceMember_can_edit_subject_in_source' END AS sourceMember_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'edit', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceMember_can_edit_expt_in_source' END AS sourceMember_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'delete', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sourceMember_can_delete_subject_in_source' END AS sourceMember_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'delete', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sourceMember_can_delete_expt_in_source' END AS sourceMember_can_delete_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'read', subjectId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceCollab_can_read_subject_in_source' END AS sourceCollab_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'read', experimentId, sourceProjectId) = TRUE) THEN NULL ELSE 'sourceCollab_can_read_expt_in_source' END AS sourceCollab_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'edit', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_edit_subject_in_source' END AS sourceCollab_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'edit', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_edit_expt_in_source' END AS sourceCollab_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'delete', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_delete_subject_in_source' END AS sourceCollab_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'delete', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_delete_expt_in_source' END AS sourceCollab_can_delete_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'read', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_read_subject_in_source' END AS sharedOwner_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'read', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_read_expt_in_source' END AS sharedOwner_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'edit', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_edit_subject_in_source' END AS sharedOwner_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'edit', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_edit_expt_in_source' END AS sharedOwner_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'delete', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_delete_subject_in_source' END AS sharedOwner_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'delete', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_delete_expt_in_source' END AS sharedOwner_can_delete_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'read', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_read_subject_in_source' END AS sharedMember_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'read', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_read_expt_in_source' END AS sharedMember_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'edit', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_edit_subject_in_source' END AS sharedMember_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'edit', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_edit_expt_in_source' END AS sharedMember_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'delete', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_delete_subject_in_source' END AS sharedMember_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'delete', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_delete_expt_in_source' END AS sharedMember_can_delete_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'read', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_read_subject_in_source' END AS sharedCollab_can_read_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'read', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_read_expt_in_source' END AS sharedCollab_can_read_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'edit', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_edit_subject_in_source' END AS sharedCollab_can_edit_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'edit', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_edit_expt_in_source' END AS sharedCollab_can_edit_expt_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'delete', subjectId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_delete_subject_in_source' END AS sharedCollab_can_delete_subject_in_source,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'delete', experimentId, sourceProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_delete_expt_in_source' END AS sharedCollab_can_delete_expt_in_source) source_results) AS source_errors,
             (SELECT
                  concat_ws(', ', admin_can_read_subject_in_shared, admin_can_read_expt_in_shared, admin_can_edit_subject_in_shared, admin_can_edit_expt_in_shared, admin_can_delete_subject_in_shared, admin_can_delete_expt_in_shared, guest_can_read_subject_in_shared, guest_can_read_expt_in_shared, guest_can_edit_subject_in_shared, guest_can_edit_expt_in_shared, guest_can_delete_subject_in_shared, guest_can_delete_expt_in_shared, dataAdmin_can_read_subject_in_shared, dataAdmin_can_read_expt_in_shared, dataAdmin_can_edit_subject_in_shared, dataAdmin_can_edit_expt_in_shared, dataAdmin_can_delete_subject_in_shared, dataAdmin_can_delete_expt_in_shared, dataAccess_can_read_subject_in_shared, dataAccess_can_read_expt_in_shared, dataAccess_can_edit_subject_in_shared, dataAccess_can_edit_expt_in_shared, dataAccess_can_delete_subject_in_shared, dataAccess_can_delete_expt_in_shared, sourceOwner_can_read_subject_in_shared, sourceOwner_can_read_expt_in_shared,
                            sourceOwner_can_edit_subject_in_shared,
                            sourceOwner_can_edit_expt_in_shared, sourceOwner_can_delete_subject_in_shared, sourceOwner_can_delete_expt_in_shared, sourceMember_can_read_subject_in_shared, sourceMember_can_read_expt_in_shared, sourceMember_can_edit_subject_in_shared, sourceMember_can_edit_expt_in_shared, sourceMember_can_delete_subject_in_shared, sourceMember_can_delete_expt_in_shared, sourceCollab_can_read_subject_in_shared, sourceCollab_can_read_expt_in_shared, sourceCollab_can_edit_subject_in_shared, sourceCollab_can_edit_expt_in_shared, sourceCollab_can_delete_subject_in_shared, sourceCollab_can_delete_expt_in_shared, sharedOwner_can_read_subject_in_shared, sharedOwner_can_read_expt_in_shared, sharedOwner_can_edit_subject_in_shared, sharedOwner_can_edit_expt_in_shared, sharedOwner_can_delete_subject_in_shared, sharedOwner_can_delete_expt_in_shared, sharedMember_can_read_subject_in_shared, sharedMember_can_read_expt_in_shared, sharedMember_can_edit_subject_in_shared,
                            sharedMember_can_edit_expt_in_shared, sharedMember_can_delete_subject_in_shared, sharedMember_can_delete_expt_in_shared, sharedCollab_can_read_subject_in_shared, sharedCollab_can_read_expt_in_shared, sharedCollab_can_edit_subject_in_shared, sharedCollab_can_edit_expt_in_shared, sharedCollab_can_delete_subject_in_shared, sharedCollab_can_delete_expt_in_shared)
              FROM
                  (SELECT
                       CASE WHEN (SELECT data_type_fns_can('admin', 'read', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'admin_can_read_subject_in_shared' END AS admin_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'read', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'admin_can_read_expt_in_shared' END AS admin_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'edit', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'admin_can_edit_subject_in_shared' END AS admin_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'edit', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'admin_can_edit_expt_in_shared' END AS admin_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'delete', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'admin_can_delete_subject_in_shared' END AS admin_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('admin', 'delete', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'admin_can_delete_expt_in_shared' END AS admin_can_delete_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'read', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'guest_can_read_subject_in_shared' END AS guest_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'read', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'guest_can_read_expt_in_shared' END AS guest_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'edit', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'guest_can_edit_subject_in_shared' END AS guest_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'edit', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'guest_can_edit_expt_in_shared' END AS guest_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'guest_can_delete_subject_in_shared' END AS guest_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can('guest', 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'guest_can_delete_expt_in_shared' END AS guest_can_delete_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'read', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_read_subject_in_shared' END AS dataAdmin_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'read', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_read_expt_in_shared' END AS dataAdmin_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'edit', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_edit_subject_in_shared' END AS dataAdmin_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'edit', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_edit_expt_in_shared' END AS dataAdmin_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'delete', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_delete_subject_in_shared' END AS dataAdmin_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAdmin, 'delete', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'dataAdmin_can_delete_expt_in_shared' END AS dataAdmin_can_delete_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'read', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'dataAccess_can_read_subject_in_shared' END AS dataAccess_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'read', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'dataAccess_can_read_expt_in_shared' END AS dataAccess_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'edit', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'dataAccess_can_edit_subject_in_shared' END AS dataAccess_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'edit', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'dataAccess_can_edit_expt_in_shared' END AS dataAccess_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'dataAccess_can_delete_subject_in_shared' END AS dataAccess_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(dataAccess, 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'dataAccess_can_delete_expt_in_shared' END AS dataAccess_can_delete_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'read', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceOwner_can_read_subject_in_shared' END AS sourceOwner_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'read', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceOwner_can_read_expt_in_shared' END AS sourceOwner_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'edit', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceOwner_can_edit_subject_in_shared' END AS sourceOwner_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'edit', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceOwner_can_edit_expt_in_shared' END AS sourceOwner_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceOwner_can_delete_subject_in_shared' END AS sourceOwner_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceOwner, 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceOwner_can_delete_expt_in_shared' END AS sourceOwner_can_delete_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'read', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceMember_can_read_subject_in_shared' END AS sourceMember_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'read', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceMember_can_read_expt_in_shared' END AS sourceMember_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'edit', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceMember_can_edit_subject_in_shared' END AS sourceMember_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'edit', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceMember_can_edit_expt_in_shared' END AS sourceMember_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceMember_can_delete_subject_in_shared' END AS sourceMember_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceMember, 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceMember_can_delete_expt_in_shared' END AS sourceMember_can_delete_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'read', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_read_subject_in_shared' END AS sourceCollab_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'read', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_read_expt_in_shared' END AS sourceCollab_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'edit', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_edit_subject_in_shared' END AS sourceCollab_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'edit', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_edit_expt_in_shared' END AS sourceCollab_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_delete_subject_in_shared' END AS sourceCollab_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sourceCollab, 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sourceCollab_can_delete_expt_in_shared' END AS sourceCollab_can_delete_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'read', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'sharedOwner_can_read_subject_in_shared' END AS sharedOwner_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'read', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'sharedOwner_can_read_expt_in_shared' END AS sharedOwner_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'edit', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_edit_subject_in_shared' END AS sharedOwner_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'edit', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_edit_expt_in_shared' END AS sharedOwner_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_delete_subject_in_shared' END AS sharedOwner_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedOwner, 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedOwner_can_delete_expt_in_shared' END AS sharedOwner_can_delete_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'read', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'sharedMember_can_read_subject_in_shared' END AS sharedMember_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'read', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'sharedMember_can_read_expt_in_shared' END AS sharedMember_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'edit', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_edit_subject_in_shared' END AS sharedMember_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'edit', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_edit_expt_in_shared' END AS sharedMember_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_delete_subject_in_shared' END AS sharedMember_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedMember, 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedMember_can_delete_expt_in_shared' END AS sharedMember_can_delete_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'read', subjectId, sharedProjectId) = TRUE) THEN NULL ELSE 'sharedCollab_can_read_subject_in_shared' END AS sharedCollab_can_read_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'read', experimentId, sharedProjectId) = TRUE) THEN NULL ELSE 'sharedCollab_can_read_expt_in_shared' END AS sharedCollab_can_read_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'edit', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_edit_subject_in_shared' END AS sharedCollab_can_edit_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'edit', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_edit_expt_in_shared' END AS sharedCollab_can_edit_expt_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'delete', subjectId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_delete_subject_in_shared' END AS sharedCollab_can_delete_subject_in_shared,
                       CASE WHEN (SELECT data_type_fns_can(sharedCollab, 'delete', experimentId, sharedProjectId) = FALSE) THEN NULL ELSE 'sharedCollab_can_delete_expt_in_shared' END AS sharedCollab_can_delete_expt_in_shared) shared_results) AS shared_errors) all_errors
    INTO results;
    RETURN results;
END
$$
    LANGUAGE plpgsql;
